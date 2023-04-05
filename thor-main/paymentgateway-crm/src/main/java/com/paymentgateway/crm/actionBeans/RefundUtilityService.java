package com.paymentgateway.crm.actionBeans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.RefundUtil;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;

@Service
public class RefundUtilityService {

	@Autowired
	private RefundCommunicator refundCommunicator;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	@Autowired
	private UserDao userDao;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(RefundUtilityService.class.getName());

	public List<String> generateRefundTransaction(Map<String, List<String[]>> fileValidDataList, Integer totalFileEntry,
			Integer totalInvalid, User sessionUser) {
		logger.info("inside generateRefundTransaction() ");
		List<JSONObject> validDatajsonList = new ArrayList<JSONObject>();
		List<JSONObject> invalidDatajsonList = new ArrayList<JSONObject>();
		try {
			List<String[]> validDataArrayList = fileValidDataList.get("validData");
			List<String[]> invalidDataArrayList = fileValidDataList.get("inValidData");
			if (validDataArrayList != null) {
				for (String dataArray[] : validDataArrayList) {
					JSONObject json = createJSOnObject(dataArray, sessionUser);
					validDatajsonList.add(json);
				}
			}
			if (invalidDataArrayList != null) {
				for (String dataArray[] : invalidDataArrayList) {
					JSONObject json = createInvalidDataJSOnObject(dataArray);
					invalidDatajsonList.add(json);
				}
			}
			refundCommunicator.communicatorFromFileData(validDatajsonList, invalidDatajsonList, totalFileEntry,
					totalInvalid, Constants.NORMAL_REFUND.getValue(), sessionUser);
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return null;
	}

	public JSONObject createJSOnObject(String[] dataArray, User sessionUser) {

		JSONObject jsonObj = new JSONObject();

		String payId = dataArray[0];
		String orderId = dataArray[1];
		String amount = dataArray[2];

		jsonObj.put(FieldType.ORDER_ID.getName(), orderId);
		jsonObj.put(FieldType.PAY_ID.getName(), payId);
		jsonObj.put(FieldType.REFUND_TXN_TYPE.getName(), "file");
		jsonObj.put(FieldType.CURRENCY_CODE.getName(), "356");
		jsonObj.put(FieldType.TXNTYPE.getName(), TxnType.REFUND.getName());
		// json.put(FieldType.REFUND_FLAG.getName(), getRefundFlag());
		if (StringUtils.isNotBlank(amount)) {
			jsonObj.put(FieldType.AMOUNT.getName(), Amount.formatAmount(amount, "INR"));
		}
		String refundOrderId = "LP" + TransactionManager.getNewTransactionId();
		jsonObj.put(FieldType.REFUND_ORDER_ID.getName(), refundOrderId);
		jsonObj.put(FieldType.INTERNAL_USER_EMAIL.getName(), sessionUser.getEmailId());

		return jsonObj;

	}

	public JSONObject createInvalidDataJSOnObject(String[] dataArray) {

		JSONObject jsonObj = new JSONObject();

		String payId = dataArray[0];
		String orderId = dataArray[1];
		String amount = "";
		try {
			amount = dataArray[2];
		} catch (Exception ex) {

		}

		jsonObj.put(FieldType.ORDER_ID.getName(), orderId);
		jsonObj.put(FieldType.PAY_ID.getName(), payId);
		jsonObj.put(FieldType.AMOUNT.getName(), amount);

		return jsonObj;

	}

	public int refundUtilReportcount(String dateFrom, String dateTo, User sessionUser) {
		logger.info("inside refundUtilReportcount() ");
		int total = 0;
		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.REFUND_UTIL_COLLECTION.getValue()));

			BasicDBObject finalQuery = new BasicDBObject();

			finalQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
			finalQuery.put(FieldType.REFUND_TXN_TYPE.getName(), Constants.NORMAL_REFUND.getValue());
			
			if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				finalQuery.put(FieldType.CREATED_BY.getName(), sessionUser.getEmailId());
			}

			total = (int) coll.count(finalQuery);
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return total;
	}

	public List<RefundUtil> fetchRefundUtilReport(String dateFrom, String dateTo, User sessionUser, int start,
			int length) {
		logger.info("inside refundUtilReportcount() ");
		List<RefundUtil> dataList = new ArrayList<RefundUtil>();
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			List<BasicDBObject> pipeline = null;
			BasicDBObject skip = null;
			BasicDBObject limit = null;

			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.REFUND_UTIL_COLLECTION.getValue()));

			BasicDBObject finalQuery = new BasicDBObject();

			finalQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
			finalQuery.put(FieldType.REFUND_TXN_TYPE.getName(), Constants.NORMAL_REFUND.getValue());
			
			if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				finalQuery.put(FieldType.CREATED_BY.getName(), sessionUser.getEmailId());
			}

			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			skip = new BasicDBObject("$skip", start);
			limit = new BasicDBObject("$limit", length);
			pipeline = Arrays.asList(match, sort, skip, limit);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				RefundUtil data = new RefundUtil();
				Document dbobj = (Document) cursor.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				data.setCreateDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				data.setFileName(dbobj.getString(FieldType.FILENAME.getName()));
				data.setTotalCount(String.valueOf(dbobj.getInteger(FieldType.TOTAL_FILE_ENTRY.getName())));
				data.setTotalValidEntryCount(String.valueOf(dbobj.getInteger(FieldType.TOTAL_VALID_ENTRY.getName())));
				data.setTotalInvalidEntryCount(
						String.valueOf(dbobj.getInteger(FieldType.TOTAL_INVALID_ENTRY.getName())));
				data.setTotalCapture(String.valueOf(dbobj.getInteger(FieldType.TOTAL_CAPTURED.getName())));
				data.setTotalRejected(String.valueOf(dbobj.getInteger(FieldType.TOTAL_REJECTED.getName())));
				data.setTotalDeclined(String.valueOf(dbobj.getInteger(FieldType.TOTAL_DECLINED.getName())));
				data.setTotalError(String.valueOf(dbobj.getInteger(FieldType.TOTAL_ERROR.getName())));
				data.setTotalDenied(String.valueOf(dbobj.getInteger(FieldType.TOTAL_DENIED.getName())));
				data.setTotalFailed(String.valueOf(dbobj.getInteger(FieldType.TOTAL_FAILED.getName())));
				data.setTotalInvalid(String.valueOf(dbobj.getInteger(FieldType.TOTAL_INVALID.getName())));
				data.setTotalAuthenticationFailed(
						String.valueOf(dbobj.getInteger(FieldType.TOTAL_AUTHENTICATION_FAILED.getName())));
				data.setTotalAcquirerDown(String.valueOf(dbobj.getInteger(FieldType.TOTAL_ACQUIRER_DOWN.getName())));
				data.setTotalFailedAtAcquirer(
						String.valueOf(dbobj.getInteger(FieldType.TOTAL_FAILED_AT_ACQUIRER.getName())));

				data.setTotalAcquirerTimeOut(
						String.valueOf(dbobj.getInteger(FieldType.TOTAL_ACQUIRER_TIMEOUT.getName())));
				data.setUploadedBy(String.valueOf(dbobj.getString(FieldType.CREATED_BY.getName())));

				dataList.add(data);
			}
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return dataList;
	}

	public FileInputStream fetchRefundUtilReportFile(String fileName, User sessionUser) {
		FileInputStream inputStream = null;
		try {
			String filePath = PropertiesManager.propertiesMap.get(Constants.REFUND_UTIL_FILE_LOCATION_URL.getValue()) + Constants.REFUND_TXN_FILE_FOLDER.getValue() + "/";

			String location = filePath + fileName;
			File file2 = new File(location);
			inputStream = new FileInputStream(file2);
		} catch (Exception ex) {
			logger.info("Exception : ", ex);
		}
		return inputStream;
	}

	public List<String> filterFileData(File file) throws IOException {
		List<String> csvData = new ArrayList<>();
		BufferedReader br = null;
		try {
			String line = "";
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				csvData.add(line);
			}
			return csvData;

		} catch (Exception e) {
			logger.error("exception ", e);
			return csvData;
		} finally {
			if (br != null)
				br.close();
		}

	}

	public Boolean validatePayId(String payId) {
		try {
			return userDao.checkValidPayId(payId);
		} catch (Exception ex) {
			logger.error("Exception while fetching from payId", ex);
			return false;
		}
	}

	public Boolean validateAmount(String amount) {
		try {
			if (StringUtils.isBlank(amount))
				return false;
			String regex = "[0-9]+([.][0-9]{1,2})?";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(amount);

			if (m.matches()) {
				BigDecimal fileAmount = new BigDecimal(amount);
				if(fileAmount.compareTo(BigDecimal.ZERO) == 1) {
					return true;
				}
			}
			return false;
		} catch (Exception ex) {
			logger.error("Exception while fetching from payId", ex);
			return false;
		}
	}

	public BigDecimal validateOrderId(String payId, String orderId) {
		try {
			if (StringUtils.isBlank(orderId)) {
				return null;
			}
			BigDecimal saleAmount = BigDecimal.ZERO;
			BigDecimal refundAmount = BigDecimal.ZERO;
			List<BasicDBObject> pipeline = null;
			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			BasicDBObject finalQuery = new BasicDBObject(FieldType.ORDER_ID.getName(), orderId);
			finalQuery.put(FieldType.PAY_ID.getName(), payId);
			finalQuery.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());

			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.AMOUNT.getName(), 1);
			projectElement.put(FieldType.PAY_ID.getName(), 1);
			projectElement.put(FieldType.TXNTYPE.getName(), 1);

			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject project = new BasicDBObject("$project", projectElement);
			pipeline = Arrays.asList(match, project);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				if (doc.getString(FieldType.TXNTYPE.getName()).equals(TransactionType.SALE.getName())) {
					saleAmount = new BigDecimal(doc.getString(FieldType.AMOUNT.getName()));
				} else {
					BigDecimal amount = new BigDecimal(doc.getString(FieldType.AMOUNT.getName()));
					refundAmount = refundAmount.add(amount);
				}
			}
			saleAmount = saleAmount.subtract(refundAmount);
			if (saleAmount.compareTo(BigDecimal.ZERO) == 0)
				return null;
			else
				return saleAmount;
		} catch (Exception ex) {
			logger.error("Exception Caught While fetching data for OrderId - " + orderId + " : ", ex);
			return null;
		}
	}
}
