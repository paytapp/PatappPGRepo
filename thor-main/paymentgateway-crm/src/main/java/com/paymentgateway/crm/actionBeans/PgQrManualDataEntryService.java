package com.paymentgateway.crm.actionBeans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;


@Service
public class PgQrManualDataEntryService {

	private static Logger logger = LoggerFactory.getLogger(PgQrManualDataEntryService.class.getName());

	@Autowired
	private DataEncDecTool dataEncDecTool;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	private static final String prefix = "MONGO_DB_";

	public String generatePgQrDataInDb(File csvFile, User sessionUser) {
		logger.info("inside generatePgQrDataInDb : ");
		Map<String, Document> customerIdMap = new HashMap<String, Document>();
		Map<String, Document> dataMap = new HashMap<String, Document>();
		try {

			List<String> fileData = filterFileData(csvFile);

			for (int i = 1; i < fileData.size(); i++) {
				String dataArray[] = fileData.get(i).split(",");
				String merchantTranID = dataArray[0];
				String key = dataArray[0] + "-" + dataArray[1] + "-" + dataArray[2] + "-" + dataArray[3] + "-"
						+ dataArray[4];
				if (customerIdMap.get(merchantTranID) == null) {
					Document doc = getCustomerQrData(merchantTranID);
					if (doc != null) {
						customerIdMap.put(merchantTranID, doc);
						dataMap.put(key, doc);
					}
				} else {
					dataMap.put(key, customerIdMap.get(merchantTranID));
				}
			}
			createDataEntryInDb(dataMap);
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
			return "error";
		}
		return "success";
	}

	private Document getCustomerQrData(String merchantTranID) {
		try {
			List<BasicDBObject> pipeline = null;
			BasicDBObject finalquery = new BasicDBObject(FieldType.CUSTOMER_ID.getName(), merchantTranID);

			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.CUSTOMER_ID.getName(), 1);
			projectElement.put(FieldType.PAY_ID.getName(), 1);
			projectElement.put(FieldType.CUST_PHONE.getName(), 1);
			projectElement.put(FieldType.CUST_NAME.getName(), 1);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CUST_QR_COLLECTION.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject project = new BasicDBObject("$project", projectElement);
			pipeline = Arrays.asList(match, project);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
						&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}
				return dbobj;
			}
		} catch (Exception ex) {
			logger.error("Exception Caught While fetching data for Customer Id - " + merchantTranID + " : ", ex);
		}
		return null;
	}

	public List<String> filterFileData(File file) throws IOException {
		logger.info("inside filterFileData : ");
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

	private void createDataEntryInDb(Map<String, Document> customerIdMap) {
		logger.info("inside createDataEntryInDb : ");
		DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
		Date dNow = new Date();
		String dateNow = DateCreater.formatDateForDb(dNow);

		MongoDatabase dbIns = null;
		dbIns = mongoInstance.getDB();
		MongoCollection<Document> txnCollect = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		MongoCollection<Document> statusCollection = dbIns.getCollection(
				propertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

		for (String key : customerIdMap.keySet()) {
			try {
				String satinOrderId = null;
				String pgDateTime = df.format(new Date());
				String newTxnId = TransactionManager.getNewTransactionId();

				Document doc = customerIdMap.get(key);
				String array[] = key.split("-");
				String merchantTranID = array[0];
				String bankTranID = array[1];
				String date = array[2];
				String time = array[3];
				String amount = array[4];
				
				if(!amount.contains("."))
					amount = Amount.toDecimal(amount + "00", "356");
				
				
				date = date.replace("/", "-");
				String createDate = DateCreater.formatDateTime(date + " " + time + ":00");

				BasicDBObject newObject = new BasicDBObject();
				newObject.put("_id", newTxnId);
				newObject.put(FieldType.AMOUNT.getName(), amount);
				newObject.put(FieldType.TOTAL_AMOUNT.getName(), amount);
				newObject.put(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.name());
				newObject.put(FieldType.PG_REF_NUM.getName(), newTxnId);
				newObject.put(FieldType.ACCT_ID.getName(), "0");
				newObject.put(FieldType.ACQ_ID.getName(), bankTranID);
				newObject.put(FieldType.OID.getName(), newTxnId);
				newObject.put(FieldType.UDF3.getName(), "");
				newObject.put(FieldType.PAYMENTS_REGION.getName(), "DOMESTIC");
				newObject.put(FieldType.CARD_HOLDER_TYPE.getName(), "CONSUMER");
				newObject.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
				newObject.put(FieldType.PAYER_ADDRESS.getName(), "");
				newObject.put(FieldType.TRANSACTION_MODE.getName(), "Direct");
				newObject.put(FieldType.TXN_CAPTURE_FLAG.getName(), "Post Captured");
				newObject.put(FieldType.CARD_MASK.getName(), "");
				newObject.put(FieldType.ORIG_TXN_ID.getName(), newTxnId);
				newObject.put(FieldType.TXN_ID.getName(), newTxnId);
				newObject.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.name());
				if (StringUtils.isNotBlank(satinOrderId)) {
					newObject.put(FieldType.ORDER_ID.getName(), satinOrderId);
				} else {
					newObject.put(FieldType.ORDER_ID.getName(), newTxnId);
				}
				newObject.put(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName()));
				newObject.put(FieldType.MOP_TYPE.getName(), "STATIC_UPI_QR");
				newObject.put(FieldType.CURRENCY_CODE.getName(), "356");
				newObject.put(FieldType.STATUS.getName(), "Captured");
				newObject.put(FieldType.RESPONSE_CODE.getName(), "000");
				newObject.put(FieldType.RESPONSE_MESSAGE.getName(), "SUCCESS");
				newObject.put(FieldType.CUST_EMAIL.getName(), "");
				newObject.put(FieldType.PAYMENT_TYPE.getName(), "UP");
				newObject.put(FieldType.ACQUIRER_TYPE.getName(), "ICICIUPI");
				newObject.put(FieldType.PG_DATE_TIME.getName(), pgDateTime);
				newObject.put(FieldType.PG_RESP_CODE.getName(), "000");
				newObject.put(FieldType.PG_TXN_MESSAGE.getName(), "SUCCESS");
				newObject.put(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName(), "INDIA");
				newObject.put(FieldType.RRN.getName(), bankTranID);
				newObject.put(FieldType.SURCHARGE_FLAG.getName(), "N");
				newObject.put(FieldType.CREATE_DATE.getName(), createDate);
				newObject.put(FieldType.DATE_INDEX.getName(), createDate.substring(0, 10).replace("-", ""));
				newObject.put(FieldType.UPDATE_DATE.getName(), dateNow);
				newObject.put(FieldType.TXN_DATE.getName(), dateNow.substring(0, 10).replace("-", ""));

				newObject.put(FieldType.ACQUIRER_TDR_SC.getName(), "0.00");
				newObject.put(FieldType.ACQUIRER_GST.getName(), "0.00");
				newObject.put(FieldType.PG_GST.getName(), "0.00");
				newObject.put(FieldType.PG_TDR_SC.getName(), "0.00");

				if (doc.getString(FieldType.CUST_PHONE.getName()) != null) {
					newObject.put(FieldType.CUST_PHONE.getName(), doc.getString(FieldType.CUST_PHONE.getName()));
				}
				if (doc.getString(FieldType.CUST_NAME.getName()) != null) {
					newObject.put(FieldType.CUST_NAME.getName(), doc.getString(FieldType.CUST_NAME.getName()));
				}
				newObject.put(FieldType.RESELLER_CHARGES.getName(), "0.00");
				newObject.put(FieldType.RESELLER_GST.getName(), "0.00");
				newObject.put(FieldType.MERCHANT_TDR_SC.getName(), "0.00");
				newObject.put(FieldType.MERCHANT_GST.getName(), "0.00");
				newObject.put(FieldType.POST_SETTLED_FLAG.getName(), "Y");

				Document document = new Document(newObject);

				txnCollect.insertOne(document);

				statusCollection.insertOne(document);
			} catch (Exception ex) {
				logger.error("Exception while entring PgQr Static entry with key : " + key + " : ", ex);
			}
		}

	}

	public String generateOrderId(String customerAccountNo, String capturedDate, String time) {
// (CUSTOMER_ACCOUNT_NO/ddmmyyyyhhmmssSSS)
		StringBuilder builder = new StringBuilder();
		int rndAmount = 100 + (int) (Math.random() * (900));
		try {
			String timeArray[] = time.split(":");
			builder.append(customerAccountNo);
			builder.append("/");
			builder.append(DateCreater.formatPgQrDateTime(capturedDate));
			builder.append(timeArray[0] + timeArray[1]);
			builder.append("00");
			builder.append(rndAmount);
		} catch (Exception ex) {
			logger.error("Exception in generateOrderId : ", ex);
		}
		return builder.toString();
	}
}
