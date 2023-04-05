package com.paymentgateway.crm.chargeback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
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
import com.paymentgateway.commons.dao.ChargebackDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.RefundUtil;
import com.paymentgateway.commons.user.TransactionHistory;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.crm.actionBeans.RefundCommunicator;
import com.paymentgateway.crm.chargeback.util.CaseStatus;

@Service
public class ChargebackUtilityService {

	@Autowired
	private RefundCommunicator refundCommunicator;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ChargebackDao chargebackDao;
	
	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	@Autowired
	private UserDao userDao;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(ChargebackUtilityService.class.getName());
	
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
					totalInvalid, Constants.CHARGEBACK_REFUND.getValue(), sessionUser);
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

	public int chargebackRefundUtilReportcount(String dateFrom, String dateTo, User sessionUser) {
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
			finalQuery.put(FieldType.REFUND_TXN_TYPE.getName(), Constants.CHARGEBACK_REFUND.getValue());
			if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				finalQuery.put(FieldType.CREATED_BY.getName(), sessionUser.getEmailId());
			}

			total = (int) coll.count(finalQuery);
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return total;
	}

	public List<RefundUtil> fetchChargebackRefundUtilReport(String dateFrom, String dateTo, User sessionUser, int start,
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
			finalQuery.put(FieldType.REFUND_TXN_TYPE.getName(), Constants.CHARGEBACK_REFUND.getValue());
			
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
			String filePath = PropertiesManager.propertiesMap.get(Constants.REFUND_UTIL_FILE_LOCATION_URL.getValue()) + Constants.CHARGEBACK_CLOSER_FILE_FOLDER.getValue() + "/";

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
	public Document validateChargebackAmount(String payId, String orderId, String amount) {
		try {
			if (StringUtils.isBlank(orderId)) {
				return null;
			}
			BigDecimal fileAmount = new BigDecimal(amount);
			BigDecimal saleAmount = BigDecimal.ZERO;
			BigDecimal totalRefundAmount = BigDecimal.ZERO;
			List<BasicDBObject> pipeline = null;
			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			BasicDBObject finalQuery = new BasicDBObject(FieldType.ORDER_ID.getName(), orderId);
			finalQuery.put(FieldType.PAY_ID.getName(), payId);
			finalQuery.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());

			BasicDBObject projectElement = new BasicDBObject();
			
			projectElement.put(FieldType.AMOUNT.getName(), 1);
			projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
			projectElement.put(FieldType.PAY_ID.getName(), 1);
			projectElement.put(FieldType.PG_REF_NUM.getName(), 1);
			projectElement.put(FieldType.TXNTYPE.getName(), 1);
			projectElement.put(FieldType.ORDER_ID.getName(), 1);
			projectElement.put(FieldType.SUB_MERCHANT_ID.getName(), 1);
			projectElement.put(FieldType.TXN_ID.getName(), 1);
			projectElement.put(FieldType.CARD_MASK.getName(), 1);
			projectElement.put(FieldType.MOP_TYPE.getName(), 1);
			projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
			projectElement.put(FieldType.CUST_EMAIL.getName(), 1);
			projectElement.put(FieldType.INTERNAL_CUST_IP.getName(), 1);
			projectElement.put(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName(), 1);
			projectElement.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), 1);
			projectElement.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), 1);
			projectElement.put(FieldType.ACQ_ID.getName(), 1);
			projectElement.put(FieldType.CURRENCY_CODE.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_TYPE.getName(), 1);
			projectElement.put(FieldType.PG_TDR_SC.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_TDR_SC.getName(), 1);
			

			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject project = new BasicDBObject("$project", projectElement);
			pipeline = Arrays.asList(match, project);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			
			Document saleDoc = null;
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				if (doc.getString(FieldType.TXNTYPE.getName()).equals(TransactionType.SALE.getName())) {
					saleAmount = new BigDecimal(doc.getString(FieldType.AMOUNT.getName()));
					saleDoc = doc;
				} else {
					BigDecimal refundAmount = new BigDecimal(doc.getString(FieldType.AMOUNT.getName()));
					totalRefundAmount = totalRefundAmount.add(refundAmount);
				}
			}
			saleAmount = saleAmount.subtract(totalRefundAmount);
			if (saleAmount.compareTo(fileAmount) == -1)
				return null;
			else
				return saleDoc;
		} catch (Exception ex) {
			logger.error("Exception Caught While fetching data for OrderId - " + orderId + " : ", ex);
			return null;
		}
	}
	public BigDecimal validateOrderId(String payId, String orderId) {
		try {
			if (StringUtils.isBlank(orderId)) {
				return null;
			}
			Chargeback chargeback = chargebackDao.findChargebackLastStatusByOrderId(payId, orderId);
			
			if (chargeback != null) {

				switch (chargeback.getStatus()) {
					case "Open":
					case "Accepted":
					return chargeback.getChargebackAmount();
				}

			}
			return null;
		} catch (Exception ex) {
			logger.error("Exception Caught While fetching data for OrderId - " + orderId + " : ", ex);
			return null;
		}
	}
	
	public Boolean validateOrderIdForClosure(String payId, String orderId, User sessionUser) {
		try {
			
			Chargeback chargeback = chargebackDao.findChargebackLastStatusByOrderId(payId, orderId);

			if (chargeback != null) {
				return updateChargebackCloserStatus(chargeback, sessionUser);
			}
			return false;
		} catch (Exception ex) {
			logger.error("Exception Caught While fetching data for OrderId - " + orderId + " : ", ex);
			return null;
		}
	}

	public Boolean isExistingChargeback(String payId, String orderId) {
		try {
			
			Chargeback chargeback = chargebackDao.findChargebackLastStatusByOrderId(payId, orderId);

			if (chargeback != null) {
				return true;
			}
			return false;
		} catch (Exception ex) {
			logger.error("Exception Caught While fetching data for OrderId - " + orderId + " : ", ex);
			return false;
		}
	}
	public Boolean updateChargebackCloserStatus(Chargeback chargeback, User sessionUser) {
		try {
			Date dNow = new Date();
			boolean isValidToClose = false;
			String closeDateString = null;
			Date closedDate = null;
			if (sessionUser.getUserType().equals(UserType.ADMIN)) {
				switch (chargeback.getStatus()) {

				case "Accepted":
					chargeback.setId(TransactionManager.getNewTransactionId());
					chargeback.setChargebackStatus("Refunded by admin");
					chargeback.setStatus("Refunded");
					chargeback.setUpdateDate(DateCreater.defaultCurrentDateTimeType(dNow));
					chargebackDao.create(chargeback);

					closedDate = DateCreater.defaultCurrentDateTimeType(new Date(dNow.getTime() + 10 * 1000));
					closeDateString = DateCreater.formatDateForDb(closedDate);
					
					chargeback.setId(TransactionManager.getNewTransactionId());
					chargeback.setChargebackStatus("Closed by admin");
					chargeback.setStatus("Closed");
					chargeback.setCloseDate(closeDateString);
					chargeback.setUpdateDate(closedDate);
					chargebackDao.create(chargeback);
					
					isValidToClose = true;
					break;
					
				case "Rejected":
					
					chargeback.setId(TransactionManager.getNewTransactionId());
					chargeback.setChargebackStatus("Refunded by admin");
					chargeback.setStatus("Refunded");
					chargeback.setUpdateDate(DateCreater.defaultCurrentDateTimeType(dNow));
					chargebackDao.create(chargeback);
					
					closedDate = DateCreater.defaultCurrentDateTimeType(new Date(dNow.getTime() + 10 * 1000));
					closeDateString = DateCreater.formatDateForDb(closedDate);
					
					chargeback.setId(TransactionManager.getNewTransactionId());
					chargeback.setChargebackStatus("Closed by admin");
					chargeback.setStatus("Closed");
					chargeback.setCloseDate(closeDateString);
					chargeback.setUpdateDate(closedDate);
					chargebackDao.create(chargeback);
					
					isValidToClose = true;
					break;
					
				case "Refunded":
					
					closeDateString = DateCreater.formatDateForDb(dNow);
					
					chargeback.setId(TransactionManager.getNewTransactionId());
					chargeback.setChargebackStatus("Closed by admin");
					chargeback.setStatus("Closed");
					chargeback.setCloseDate(closeDateString);
					chargeback.setUpdateDate(DateCreater.defaultCurrentDateTimeType(dNow));
					chargebackDao.create(chargeback);
					
					isValidToClose = true;
					break;
					
				}
				return isValidToClose;
			} else {
				switch (chargeback.getStatus()) {

				case "Accepted":
					chargeback.setId(TransactionManager.getNewTransactionId());
					chargeback.setChargebackStatus("Refunded by subadmin");
					chargeback.setStatus("Refunded");
					chargeback.setUpdateDate(DateCreater.defaultCurrentDateTimeType(dNow));
					chargebackDao.create(chargeback);
					
					closedDate = DateCreater.defaultCurrentDateTimeType(new Date(dNow.getTime() + 10 * 1000));
					closeDateString = DateCreater.formatDateForDb(closedDate);
					
					chargeback.setId(TransactionManager.getNewTransactionId());
					chargeback.setChargebackStatus("Closed by subadmin");
					chargeback.setStatus("Closed");
					chargeback.setCloseDate(closeDateString);
					chargeback.setUpdateDate(closedDate);
					chargebackDao.create(chargeback);
					
					isValidToClose = true;
					break;
					
				case "Rejected":
					
					chargeback.setId(TransactionManager.getNewTransactionId());
					chargeback.setChargebackStatus("Refunded by subadmin");
					chargeback.setStatus("Refunded");
					chargeback.setUpdateDate(DateCreater.defaultCurrentDateTimeType(dNow));
					chargebackDao.create(chargeback);
					
					closedDate = DateCreater.defaultCurrentDateTimeType(new Date(dNow.getTime() + 10 * 1000));
					closeDateString = DateCreater.formatDateForDb(closedDate);
					
					chargeback.setId(TransactionManager.getNewTransactionId());
					chargeback.setChargebackStatus("Closed by subadmin");
					chargeback.setStatus("Closed");
					chargeback.setCloseDate(closeDateString);
					chargeback.setUpdateDate(closedDate);
					chargebackDao.create(chargeback);
					
					isValidToClose = true;
					break;
					
				case "Refunded":
					
					closeDateString = DateCreater.formatDateForDb(dNow);
					
					chargeback.setId(TransactionManager.getNewTransactionId());
					chargeback.setChargebackStatus("Closed by subadmin");
					chargeback.setStatus("Closed");
					chargeback.setCloseDate(closeDateString);
					chargeback.setUpdateDate(DateCreater.defaultCurrentDateTimeType(dNow));
					chargebackDao.create(chargeback);
					
					isValidToClose = true;
					break;
				}
				return isValidToClose;
			}
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
			return null;
		}

	}

	public void createDataReportForClosure(List<String[]> fileDataList, Map<String, Integer> dataValidationMap, User sessionUser) {
		
		String fileName = createFileForClosure(fileDataList);
		
		insertClosureDataInDb(dataValidationMap, sessionUser.getEmailId(), fileName);
	}

	
	@SuppressWarnings("unchecked")
	public void createDataReportForChargebackCreation(Map<String, Object> fileDataFilterMap,
			Map<String, Integer> dataValidationMap, User sessionUser) {
		List<String[]> invalidDataList = (List<String[]>) fileDataFilterMap.get("inValidData");
		List<Document> documentList = (List<Document>) fileDataFilterMap.get("documentList");
		List<String[]> validDataArrayList = new ArrayList<>();
		if(documentList != null && !documentList.isEmpty()) {
			validDataArrayList = createChargeback(documentList, sessionUser);
		}
		
		String fileName = createFileForChargebackCreation(validDataArrayList, invalidDataList);
		
		insertInDbForChargebackCreation(dataValidationMap, validDataArrayList, sessionUser.getEmailId(), fileName);
	}

	private String createFileForClosure(List<String[]> fileDataList) {

		String fileName = null;
		try {
			String fileLocation = PropertiesManager.propertiesMap
					.get(Constants.REFUND_UTIL_FILE_LOCATION_URL.getValue()) + Constants.CHARGEBACK_CLOSER_FILE_FOLDER.getValue() + "/";
			try {
				Files.createDirectories(Paths.get(fileLocation));
			} catch (IOException e1) {
				logger.error("Error in creating Directory ", e1);
			}
			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			Sheet sheet = wb.createSheet("Chargeback Closer Report");

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Pay Id");
			row.createCell(2).setCellValue("Order Id");
			row.createCell(3).setCellValue("Status");

			for (String[] dataArray : fileDataList) {
				row = sheet.createRow(rownum++);

				Object[] objectArray = new Object[5];
				objectArray[0] = rownum - 1;
				objectArray[1] = dataArray[0];
				objectArray[2] = dataArray[1];
				objectArray[3] = dataArray[2];

				int cellnum = 0;
				for (Object obj : objectArray) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);

				}
			}

			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			fileName = "ChargebackClosureReport" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(fileLocation, fileName);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			logger.info("Files generated successfully for Chargeback Closer Report");
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return fileName;
	}

	private void insertClosureDataInDb(Map<String, Integer> dataValidationMap, String userEmail, String fileName) {
		logger.info("inside insertInDb : ");
		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentDate = dateFormat.format(new Date());
			String dateIndex = DateCreater.changeDateString(currentDate);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.REFUND_UTIL_COLLECTION.getValue()));

			BasicDBObject newFieldsObj = new BasicDBObject();

			newFieldsObj.put(FieldType.TOTAL_FILE_ENTRY.getName(), dataValidationMap.get("totalCount"));
			newFieldsObj.put(FieldType.TOTAL_VALID_ENTRY.getName(), dataValidationMap.get("totalValid"));
			newFieldsObj.put(FieldType.TOTAL_INVALID_ENTRY.getName(), dataValidationMap.get("totalInvalid"));
			newFieldsObj.put(FieldType.TOTAL_CLOSED.getName(), dataValidationMap.get("totalClosed"));
			newFieldsObj.put(FieldType.TOTAL_EXCEPTION.getName(), dataValidationMap.get("totalException"));
			newFieldsObj.put(FieldType.CHARGEBACK_TYPE.getName(), Constants.CHARGEBACK_CLOSURE.getValue());
			newFieldsObj.put(FieldType.CREATE_DATE.getName(), currentDate);
			newFieldsObj.put(FieldType.DATE_INDEX.getName(), dateIndex);
			newFieldsObj.put(FieldType.FILENAME.getName(), fileName);
			newFieldsObj.put(FieldType.CREATED_BY.getName(), userEmail);
			
			Document doc = new Document(newFieldsObj);

			coll.insertOne(doc);

		} catch (Exception ex) {
			logger.error("Exception while inserting data in Util Collection : ", ex);
		}
	}
	
	public int chargebackClosureReportcount(String dateFrom, String dateTo, User sessionUser) {
		logger.info("inside chargebackClosureReportcount() ");
		int total = 0;
		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.REFUND_UTIL_COLLECTION.getValue()));

			BasicDBObject finalQuery = new BasicDBObject();

			finalQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
			
			finalQuery.put(FieldType.CHARGEBACK_TYPE.getName(), Constants.CHARGEBACK_CLOSURE.getValue());
			
			if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				finalQuery.put(FieldType.CREATED_BY.getName(), sessionUser.getEmailId());
			}

			total = (int) coll.count(finalQuery);
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return total;
	}

	public List<RefundUtil> fetchchargebackClosureReport(String dateFrom, String dateTo, User sessionUser, int start,
			int length) {
		logger.info("inside fetchchargebackClosureReport() ");
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
			finalQuery.put(FieldType.CHARGEBACK_TYPE.getName(), Constants.CHARGEBACK_CLOSURE.getValue());
			
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
				data.setTotalClosed(String.valueOf(dbobj.getInteger(FieldType.TOTAL_CLOSED.getName())));
				data.setTotalException(String.valueOf(dbobj.getInteger(FieldType.TOTAL_EXCEPTION.getName())));
				data.setUploadedBy(String.valueOf(dbobj.getString(FieldType.CREATED_BY.getName())));

				dataList.add(data);
			}
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return dataList;
	}
	
	public FileInputStream fetchChargebackClosureReportFile(String fileName, User sessionUser) {
		logger.info("inside fetchChargebackClosureReportFile() ");
		FileInputStream inputStream = null;
		try {
			String filePath = PropertiesManager.propertiesMap.get(Constants.REFUND_UTIL_FILE_LOCATION_URL.getValue()) + Constants.CHARGEBACK_CLOSER_FILE_FOLDER.getValue() + "/";

			String location = filePath + fileName;
			File file2 = new File(location);
			inputStream = new FileInputStream(file2);
		} catch (Exception ex) {
			logger.info("Exception : ", ex);
		}
		return inputStream;
	}

	private String createFileForChargebackCreation(List<String[]> validDataArrayList, List<String[]> invalidDataList) {

		String fileName = null;
		String fileLocation = null;
		try {
				fileLocation = PropertiesManager.propertiesMap
						.get(Constants.REFUND_UTIL_FILE_LOCATION_URL.getValue()) + Constants.CHARGEBACK_CREATION_FILE_FOLDER.getValue() + "/";
				
			try {
				Files.createDirectories(Paths.get(fileLocation));
			} catch (IOException e1) {
				logger.error("Error in creating Directory ", e1);
			}
			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			Sheet sheet = wb.createSheet("Chargeback Creation Report");

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("pay Id");
			row.createCell(2).setCellValue("Order Id");
			row.createCell(3).setCellValue("Amount");
			row.createCell(4).setCellValue("Status");

			for (String[] dataArray : validDataArrayList) {
				//String amount = Amount.toDecimal(dataArray[2], "356");
				row = sheet.createRow(rownum++);

				Object[] objectArray = new Object[5];
				objectArray[0] = rownum - 1;
				objectArray[1] = dataArray[0]; //payId
				objectArray[2] = dataArray[1]; //orderId
				objectArray[3] = dataArray[2]; 
				objectArray[4] = dataArray[3]; // status

				int cellnum = 0;
				for (Object obj : objectArray) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);

				}
			}
			for (String[] dataArray : invalidDataList) {
				row = sheet.createRow(rownum++);

				Object[] objectArray = new Object[5];
				objectArray[0] = rownum - 1;
				objectArray[1] = dataArray[0]; //payId
				objectArray[2] = dataArray[1]; //orderId
				objectArray[3] = dataArray[2]; //amount
				objectArray[4] = "Invalid Entry";

				int cellnum = 0;
				for (Object obj : objectArray) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);

				}
			}

			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			fileName = "ChargebackCreationReport_" + df.format(new Date()) + FILE_EXTENSION;
			
			File file = new File(fileLocation, fileName);
			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			logger.info("Files generated successfully for RefundUtils Report");
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return fileName;
	}
	
	private List<String[]> createChargeback(List<Document> documentList, User sessionUser) {
		List<String[]> validDataArrayList = new ArrayList<String[]>();
		String targetDate = null;
		Date newDate = new Date();
		int milisecIncrement = 0;
		for (Document doc : documentList) {
			try {
				Chargeback chargeback = new Chargeback();
				BigDecimal totalChargeBackAmount = new BigDecimal(doc.getString("chargebackAmount"));
				
				if (StringUtils.isBlank(targetDate)) {
					targetDate = DateCreater.chargebackTargetDate();
				}
				chargeback.setCaseId(TransactionManager.getNewTransactionId());
				Date currentDate = DateCreater.defaultCurrentDateTimeType(new Date(newDate.getTime() + milisecIncrement * 1000));
				chargeback.setUpdateDate(currentDate);
				// chargeback.setUpdateDate(new Date());
				chargeback.setTargetDate(targetDate);
				chargeback.setChargebackType("Charge Back");
				chargeback.setChargebackStatus("New");
				chargeback.setCommentedBy(sessionUser.getBusinessName());
				chargeback.setComments("Auto Generated Chargeback");
				chargeback.setId(TransactionManager.getNewTransactionId());
				chargeback.setHoldAmountFlag(false);
				// from database

				chargeback.setOrderId(doc.getString(FieldType.ORDER_ID.getName()));
				chargeback.setPayId(doc.getString(FieldType.PAY_ID.getName()));
				chargeback.setSubMerchantId(doc.getString(FieldType.SUB_MERCHANT_ID.getName()));
				chargeback.setTransactionId(doc.getString(FieldType.TXN_ID.getName()));
				chargeback.setCreateDate(currentDate);
				chargeback.setCardNumber(doc.getString(FieldType.CARD_MASK.getName()));
				chargeback.setMopType(MopType.getmopName(doc.getString(FieldType.MOP_TYPE.getName())));
				// chargeback.setStatus(transDetails.getStatus());
				chargeback.setPaymentType(PaymentType.getpaymentName(doc.getString(FieldType.PAYMENT_TYPE.getName())));
				chargeback.setCustEmail(doc.getString(FieldType.CUST_EMAIL.getName()));
				chargeback.setInternalCustIP(doc.getString(FieldType.INTERNAL_CUST_IP.getName()));
				chargeback.setInternalCustCountryName(doc.getString(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()));
				chargeback.setInternalCardIssusserBank(doc.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
				chargeback.setInternalCardIssusserCountry(
						doc.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
				chargeback.setCurrencyCode(doc.getString(FieldType.CURRENCY_CODE.getName()));
				chargeback.setCurrencyNameCode(
						Currency.getAlphabaticCode(doc.getString(FieldType.CURRENCY_CODE.getName())));
				// chargeback.setAmount(new BigDecimal(transDetails.getAmount()));
				chargeback.setCapturedAmount(doc.getString(FieldType.TOTAL_AMOUNT.getName()));
				chargeback.setAuthorizedAmount(new BigDecimal(doc.getString(FieldType.AMOUNT.getName())));
//				chargeback.setFixedTxnFee(transDetails.getFixedTxnFee());
//				chargeback.setTdr(transDetails.getTdr());
//				chargeback.setServiceTax(transDetails.getServiceTax());
				chargeback.setChargebackAmount(totalChargeBackAmount);
//				chargeback.setNetAmount(transDetails.getNetAmount());
//				chargeback.setPercentecServiceTax(transDetails.getPercentecServiceTax());
				chargeback.setMerchantTDR(new BigDecimal(doc.getString(FieldType.PG_TDR_SC.getName()))
						.add(new BigDecimal(doc.getString(FieldType.ACQUIRER_TDR_SC.getName()))));
				chargeback.setChargebackAmount(totalChargeBackAmount);
//				chargeback.setOtherAmount(otherAmount);
				chargeback.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.getName()));
				chargeback.setTotalchargebackAmount(totalChargeBackAmount);
				chargeback.setStatus(CaseStatus.OPEN.getName());

				chargebackDao.create(chargeback);
				
				String[] dataArray = new String[4];
				dataArray[0] = doc.getString(FieldType.PAY_ID.getName());
				dataArray[1] = doc.getString(FieldType.ORDER_ID.getName());
				dataArray[2] = doc.getString("chargebackAmount");
				dataArray[3] = "Created";
				validDataArrayList.add(dataArray);
				
			} catch (Exception exception) {
				String[] dataArray = new String[4];
				dataArray[0] = doc.getString(FieldType.PAY_ID.getName());
				dataArray[1] = doc.getString(FieldType.ORDER_ID.getName());
				dataArray[2] = doc.getString("chargebackAmount");
				dataArray[3] = "Exception";
				validDataArrayList.add(dataArray);
				logger.error("Exception while inserting chargeback for OrederId : ", doc.getString(FieldType.ORDER_ID.getName()));

			}
			milisecIncrement += 2; 
		}
		return validDataArrayList;
	}
	
	private void insertInDbForChargebackCreation(Map<String, Integer> dataValidationMap, List<String[]> validDataArrayList, String userEmail, String fileName) {
		logger.info("inside insertInDb : ");
		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentDate = dateFormat.format(new Date());
			String dateIndex = DateCreater.changeDateString(currentDate);
			long totalException = 0;
			//int totalCreated = (Integer)dataValidationMap.get("totalValid");
			
			long totalCreated = validDataArrayList.stream().filter(a -> a[3].equals("Created")).count();
			totalException = validDataArrayList.stream().filter(a -> a[3].equals("Exception")).count();
			
//			if(validDataArrayList != null && !validDataArrayList.isEmpty()) {
//				totalException = validDataArrayList.size();
//				totalCreated = totalCreated - totalException;
//			}
			
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.REFUND_UTIL_COLLECTION.getValue()));
			
			BasicDBObject newFieldsObj = new BasicDBObject();

			newFieldsObj.put(FieldType.TOTAL_FILE_ENTRY.getName(), dataValidationMap.get("totalCount"));
			newFieldsObj.put(FieldType.TOTAL_VALID_ENTRY.getName(), dataValidationMap.get("totalValid"));
			newFieldsObj.put(FieldType.TOTAL_INVALID_ENTRY.getName(), dataValidationMap.get("totalInvalid"));
			newFieldsObj.put(FieldType.TOTAL_CREATED.getName(), totalCreated);
			newFieldsObj.put(FieldType.TOTAL_EXCEPTION.getName(), totalException);
			newFieldsObj.put(FieldType.CHARGEBACK_TYPE.getName(), Constants.CHARGEBACK_CREATION.getValue());
			newFieldsObj.put(FieldType.CREATE_DATE.getName(), currentDate);
			newFieldsObj.put(FieldType.DATE_INDEX.getName(), dateIndex);
			newFieldsObj.put(FieldType.FILENAME.getName(), fileName);
			newFieldsObj.put(FieldType.CREATED_BY.getName(), userEmail);
			
			Document doc = new Document(newFieldsObj);

			coll.insertOne(doc);

		} catch (Exception ex) {
			logger.error("Exception while inserting data in Util Collection : ", ex);
		}
	}

	public int chargebackCreationReportcount(String dateFrom, String dateTo, User sessionUser) {
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
			
			finalQuery.put(FieldType.CHARGEBACK_TYPE.getName(), Constants.CHARGEBACK_CREATION.getValue());
			
			if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				finalQuery.put(FieldType.CREATED_BY.getName(), sessionUser.getEmailId());
			}

			total = (int) coll.count(finalQuery);
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return total;
	}

	public List<RefundUtil> fetchchargebackCreationReport(String dateFrom, String dateTo, User sessionUser, int start,
			int length) {
		logger.info("inside fetchchargebackCreationReport() ");
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
			finalQuery.put(FieldType.CHARGEBACK_TYPE.getName(), Constants.CHARGEBACK_CREATION.getValue());
			
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
				data.setTotalCreated(String.valueOf(dbobj.getLong(FieldType.TOTAL_CREATED.getName())));
				data.setTotalException(String.valueOf(dbobj.getLong(FieldType.TOTAL_EXCEPTION.getName())));
				data.setUploadedBy(String.valueOf(dbobj.getString(FieldType.CREATED_BY.getName())));

				dataList.add(data);
			}
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return dataList;
	}

	public InputStream fetchChargebackCreationReportFile(String fileName, User sessionUser) {
		logger.info("inside fetchChargebackCreationReportFile() ");
		FileInputStream inputStream = null;
		try {
			String filePath = PropertiesManager.propertiesMap.get(Constants.REFUND_UTIL_FILE_LOCATION_URL.getValue()) + Constants.CHARGEBACK_CREATION_FILE_FOLDER.getValue() + "/";

			String location = filePath + fileName;
			File file2 = new File(location);
			inputStream = new FileInputStream(file2);
		} catch (Exception ex) {
			logger.info("Exception : ", ex);
		}
		return inputStream;
	}
}
