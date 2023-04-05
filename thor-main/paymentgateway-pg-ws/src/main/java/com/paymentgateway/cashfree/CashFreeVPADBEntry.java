package com.paymentgateway.cashfree;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class CashFreeVPADBEntry {

	@Autowired
	private PropertiesManager propertiesManager;
	@Autowired
	private MongoInstance mongoInstance;

	private static final String prefix = "MONGO_DB_";
	private static Logger logger = LoggerFactory.getLogger(CashFreeVPADBEntry.class.getName());

	public void addNewEntryData(Fields fields, String entryType) {

		try {
			BasicDBObject complaintObj = new BasicDBObject();
			complaintObj = dbEntry(fields, entryType);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.CASHFREE_QRCODE.getValue()));

			Document complaintDoc = new Document(complaintObj);
			collection.insertOne(complaintDoc);

		} catch (Exception e) {
			logger.error("Exception in db in cashfree db ", e);
		}

	}

	public void updateEntryData(Fields fields, String entryType) {

		try {
			BasicDBObject fileObj = new BasicDBObject();
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.CASHFREE_QRCODE.getValue()));

			List<BasicDBObject> cond = new ArrayList<BasicDBObject>();
			BasicDBObject complanintId = new BasicDBObject(FieldType.CUST_PHONE.getName(),
					fields.get(FieldType.CUST_PHONE.getName()));
			BasicDBObject statusId = new BasicDBObject(FieldType.CUST_EMAIL.getName(),
					fields.get(FieldType.CUST_EMAIL.getName()));
			cond.add(complanintId);
			cond.add(statusId);
			BasicDBObject finalquerydata = new BasicDBObject("$and", cond);
			Document documentresult = coll.find(finalquerydata).first();

			BasicDBObject complaintObj = dbEntry(fields, entryType);

			Bson updateDocument = new Document("$set", complaintObj);
			coll.updateOne(finalquerydata, updateDocument);

		} catch (Exception e) {
			logger.error("Exception in db in cashfree db ", e);
		}

	}

	public static BasicDBObject dbEntry(Fields fields, String entryType) {
		BasicDBObject complaintObj = new BasicDBObject();
		try {
			String currentDate = DateCreater.formatDateForDb(new Date());

			switch (entryType) {

			case "virtualVPA":
				complaintObj.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
					complaintObj.put(FieldType.SUB_MERCHANT_ID.getName(),
							fields.get(FieldType.SUB_MERCHANT_ID.getName()));
				}
				if (StringUtils.isNotBlank(fields.get(FieldType.RESELLER_ID.getName()))) {
					complaintObj.put(FieldType.RESELLER_ID.getName(), fields.get(FieldType.RESELLER_ID.getName()));
				}
				complaintObj.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(),
						fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
				complaintObj.put(FieldType.COMPANY_NAME.getName(), fields.get(FieldType.COMPANY_NAME.getName()));
				complaintObj.put(FieldType.CUST_NAME.getName(), fields.get(FieldType.CUST_NAME.getName()));
				complaintObj.put(FieldType.CUST_PHONE.getName(), fields.get(FieldType.CUST_PHONE.getName()));
				complaintObj.put(FieldType.CUST_EMAIL.getName(), fields.get(FieldType.CUST_EMAIL.getName()));
				complaintObj.put(FieldType.CREATE_DATE.getName(), currentDate);
				complaintObj.put(FieldType.CUST_ID.getName(), fields.get(FieldType.CUST_ID.getName()));
				complaintObj.put("VPA", fields.get(FieldType.VPA.getName()));
				complaintObj.put("VPA_PG_RESP_CODE", fields.get(FieldType.PG_RESP_CODE.getName()));
				complaintObj.put("VPA_PG_RESPONSE_STATUS", fields.get(FieldType.PG_RESPONSE_STATUS.getName()));
				complaintObj.put("VPA_PG_RESPONSE_MSG", fields.get(FieldType.PG_RESPONSE_MSG.getName()));
				break;
			case "virtualQR":
				complaintObj.put(FieldType.UPI_QR_CODE.getName(), fields.get(FieldType.UPI_QR_CODE.getName()));
				complaintObj.put("QR_CODE_PG_RESP_CODE", fields.get(FieldType.PG_RESP_CODE.getName()));
				complaintObj.put("QR_CODE_PG_RESPONSE_STATUS", fields.get(FieldType.PG_RESPONSE_STATUS.getName()));
				complaintObj.put("QR_CODE_PG_RESPONSE_MSG", fields.get(FieldType.PG_RESPONSE_MSG.getName()));
				break;
			}
		} catch (Exception e) {
			logger.error("Exception in dbEntry in cashfree db ", e);
		}
		return complaintObj;
	}

	public void updateQrCode(Fields fields, String entryType, String vpa) {

		try {
			BasicDBObject fileObj = new BasicDBObject();
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.CASHFREE_QRCODE.getValue()));

			BasicDBObject finalquery = new BasicDBObject(FieldType.VPA.getName(), vpa);
			Document document = (Document) coll.find(finalquery).first();
			Bson filter = new Document(FieldType.VPA.getName(), document.getString(FieldType.VPA.getName()));
			BasicDBObject complaintObj = dbEntry(fields, entryType);

			Bson updateDocument = new Document("$set", complaintObj);
			coll.updateOne(filter, updateDocument);
		} catch (Exception e) {
			logger.error("Exception in db in cashfree db ", e);
		}

	}

	public boolean qrCodeValidation(Fields fields, String entryType, String vpa) {

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.CASHFREE_QRCODE.getValue()));

			BasicDBObject finalquery = new BasicDBObject(FieldType.VPA.getName(), vpa);
			Document documentresult = coll.find(finalquery).first();
			if (documentresult == null) {
				return false;
			}

		} catch (Exception e) {
			logger.error("Exception in db in cashfree db ", e);
			return false;
		}
		return true;
	}

	public void updateDataEntry(Fields fields, String entryType, String virtualAccountNo) {

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.CASHFREE_QRCODE.getValue()));
			BasicDBObject finalquery = new BasicDBObject(FieldType.CUST_ID.getName(), virtualAccountNo);
			Document document = (Document) coll.find(finalquery).first();
			Bson filter = new Document(FieldType.CUST_ID.getName(), document.getString(FieldType.CUST_ID.getName()));
			BasicDBObject complaintObj = dbEntry(fields, entryType);

			Bson updateDocument = new Document("$set", complaintObj);
			coll.updateOne(filter, updateDocument);
		} catch (Exception e) {
			logger.error("Exception in db in cashfree db ", e);
		}

	}

	public Map<String, String> getDataByAccountNo(Fields fields) {
		Map<String, String> saveResponse = new HashMap<String, String>();
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.CASHFREE_QRCODE.getValue()));
			BasicDBObject finalquery = new BasicDBObject(FieldType.CUSTOMER_ACCOUNT_NO.getName(),
					fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
			Document documentresult = (Document) coll.find(finalquery).first();
			if (documentresult != null) {
				saveResponse.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(),
						documentresult.getString(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
				saveResponse.put(FieldType.CUST_ID.getName(), documentresult.getString(FieldType.CUST_ID.getName()));
				saveResponse.put(FieldType.VPA.getName(), documentresult.getString(FieldType.VPA.getName()));
				saveResponse.put(FieldType.COMPANY_NAME.getName(),
						documentresult.getString(FieldType.COMPANY_NAME.getName()));
				String pngImage = documentresult.getString(FieldType.UPI_QR_CODE.getName());
				pngImage = pngImage.replace("data:image/png;base64,", "");

				saveResponse.put(FieldType.UPI_QR_CODE.getName(), pngImage);
				saveResponse.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
				saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
			}
		} catch (Exception e) {
			logger.error("Exception in db in cashfree db ", e);
		}
		return saveResponse;
	}

	@SuppressWarnings("static-access")
	public int cashFreeAccountValidationByAccountNo(Fields fields) {
		int i = 0;

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.CASHFREE_QRCODE.getValue()));
			BasicDBObject finalquery = new BasicDBObject(FieldType.CUSTOMER_ACCOUNT_NO.getName(),
					fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()));

			logger.info("Inside cashFreeAccountValidationByAccountNo finalquery = " + finalquery);

			Document documentresult = (Document) coll.find(finalquery).first();
			if (documentresult != null) {
				if (documentresult.getString("VPA_PG_RESP_CODE") != null
						&& documentresult.getString("VPA_PG_RESP_CODE").equalsIgnoreCase("200")) {
					i = 2;
				}
				if (documentresult.getString("QR_CODE_PG_RESP_CODE") != null
						&& documentresult.getString("QR_CODE_PG_RESP_CODE").equalsIgnoreCase("200")) {
					i = 3;
				}
				return i;
			} else {
				return i = 1;
			}

		} catch (Exception e) {
			logger.error("Exception in db in cashfree db ", e);

		}
		return i = 5;
	}

}
