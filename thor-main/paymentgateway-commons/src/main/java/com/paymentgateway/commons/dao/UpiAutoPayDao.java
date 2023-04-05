package com.paymentgateway.commons.dao;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.json.simple.JSONObject;
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
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.UpiAutoPay;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AutoPayFrequency;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.Frequency;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class UpiAutoPayDao {

	private static Logger logger = LoggerFactory.getLogger(UpiAutoPayDao.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private Fields fields;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	@SuppressWarnings("static-access")
	public void insert(LinkedHashMap<String, String> rowRequest, JSONObject json) {

		logger.info("Inside insert registration details for autoPay");
		try {
			BasicDBObject newObject = new BasicDBObject();

			newObject.put("_id", rowRequest.get("merchantTranId"));
			newObject.put(FieldType.DATE_FROM.getName(), rowRequest.get(FieldType.DATE_FROM.getName()));
			newObject.put(FieldType.DATE_TO.getName(), rowRequest.get(FieldType.DATE_TO.getName()));
			newObject.put(FieldType.PAY_ID.getName(), rowRequest.get(FieldType.PAY_ID.getName()));
			newObject.put(FieldType.AMOUNT.getName(), rowRequest.get("amount"));
			newObject.put(FieldType.MONTHLY_AMOUNT.getName(), rowRequest.get(FieldType.MONTHLY_AMOUNT.getName()));
			newObject.put(FieldType.TOTAL_AMOUNT.getName(), rowRequest.get(FieldType.TOTAL_AMOUNT.getName()));
			newObject.put(FieldType.PAYER_ADDRESS.getName(), rowRequest.get("payerVa"));
			newObject.put(FieldType.NOTE.getName(), rowRequest.get("note"));
			newObject.put(FieldType.CREATE_DATE.getName(), rowRequest.get(FieldType.CREATE_DATE.getName()));
			newObject.put(FieldType.TXN_ID.getName(), rowRequest.get("merchantTranId"));
			newObject.put(FieldType.PG_REF_NUM.getName(), rowRequest.get("merchantTranId"));
			newObject.put(FieldType.ORDER_ID.getName(), rowRequest.get(FieldType.ORDER_ID.getName()));
			newObject.put(FieldType.ORIG_TXN_ID.getName(), rowRequest.get("merchantTranId"));
			newObject.put("MERCHANT_LOGO", rowRequest.get("MERCHANT_LOGO"));

			newObject.put(FieldType.TENURE.getName(), rowRequest.get(FieldType.TENURE.getName()));
			newObject.put(FieldType.FREQUENCY.getName(), rowRequest.get("frequency"));
			newObject.put(FieldType.PURPOSE.getName(), rowRequest.get("purpose"));
			newObject.put(FieldType.AMOUNT_LIMIT.getName(), rowRequest.get("amountLimit"));
			newObject.put(FieldType.REMARKS.getName(), rowRequest.get("remark"));
			newObject.put(FieldType.REQUEST_TYPE.toString(), rowRequest.get("requestType"));
			newObject.put(FieldType.DEBIT_DAY.getName(), rowRequest.get("debitDay"));
			newObject.put(FieldType.DEBIT_RULE.getName(), rowRequest.get("debitRule"));
			newObject.put(FieldType.CUST_EMAIL.getName(), rowRequest.get(FieldType.CUST_EMAIL.getName()));
			newObject.put(FieldType.CUST_PHONE.getName(), rowRequest.get(FieldType.CUST_PHONE.getName()));
			newObject.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getCode());

			/*
			 * { "response":"92", "merchantId":"401425", "subMerchantId":"401425",
			 * "terminalId":"5411", "success":"true", "message":"Transaction Initiated",
			 * "merchantTranId":"1342564712132445", "BankRRN":"112400185511" }
			 */

			newObject.put(FieldType.TXNTYPE.getName(), TransactionType.REGISTRATION.getName());
			if (json.get("message").toString().equalsIgnoreCase("Transaction Initiated")) {
				newObject.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
				newObject.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PROCESSING.getCode());
				newObject.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PROCESSING.getResponseMessage());
				newObject.put(FieldType.RRN.getName(), json.get("BankRRN").toString());
			} else {
				newObject.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				newObject.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				newObject.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
			}

			newObject.put(FieldType.PG_RESP_CODE.getName(), json.get("response").toString());
			newObject.put(FieldType.PG_RESPONSE_MSG.getName(), json.get("message").toString());
			newObject.put(FieldType.PG_RESPONSE_STATUS.getName(), json.get("success").toString());

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));

			Document doc = new Document(newObject);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			// collection.insertOne(doc);

		} catch (Exception ex) {
			logger.error("Exception Caught in insert upi autoPay Registration details ", ex);
		}
	}

	@SuppressWarnings("static-access")
	public void updateByCallBackResponse(JSONObject json) {
		logger.info("inside updateByCallBackResponse to update registration DB fields");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));

			Document queryForUpdate = new Document();
			queryForUpdate.put(FieldType.TXN_ID.getName(), json.get("merchantTranId").toString());

			Document setData = new Document();

			

			if (json.get("TxnStatus").toString().equalsIgnoreCase("CREATE-SUCCESS")) {

				setData.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());

				if (StringUtils.isNotBlank(json.get("UMN").toString())) {
					setData.put(FieldType.UMN.getName(), json.get("UMN").toString());
				}
				if (StringUtils.isNotBlank(json.get("BankRRN").toString())) {
					setData.put(FieldType.RRN.getName(), json.get("BankRRN").toString());
				}
				setData.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				setData.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
				setData.put(FieldType.PG_RESPONSE_STATUS.getName(), json.get("TxnStatus").toString());

			} else {
				setData.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				setData.put(FieldType.UMN.getName(), Constants.NA.getValue());
				setData.put(FieldType.RRN.getName(), json.get("BankRRN").toString());
				setData.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
				setData.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				setData.put(FieldType.PG_RESPONSE_STATUS.getName(), json.get("TxnStatus").toString());
			}
			setData.put(FieldType.UPDATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));

			Document update = new Document();
			update.put("$set", setData);
			collection.updateOne(queryForUpdate, update);

		} catch (Exception ex) {
			logger.error("Exception caught while update fields according callback response ", ex);
		}
	}

	@SuppressWarnings("static-access")
	public void updateByNotificationResponse(JSONObject responseJson, Map<String, String> debitPendingTransaction) {
		logger.info("inside updateByNotificationResponse to update debit transaction DB fields");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));

			Document queryForUpdate = new Document();
			queryForUpdate.put(FieldType.PG_REF_NUM.getName(), responseJson.get("merchantTranId").toString());
			Document setData = new Document();

			/*
			 * { "response" : "0", "merchantId" : "106161", "subMerchantId" : "12234",
			 * "terminalId" : "5411", "BankRRN" : "615519221396", "merchantTranId" :
			 * "612411454593", "amount" : "12", "success" : "true", "message" :
			 * "Transaction Successful" }
			 */

			BasicDBObject newFieldsObj = new BasicDBObject();
			String newId = TransactionManager.getNewTransactionId();

			newFieldsObj.put("_id", newId);
			newFieldsObj.put(FieldType.TXNTYPE.getName(), debitPendingTransaction.get(FieldType.TXNTYPE.getName()));
			newFieldsObj.put(FieldType.PAY_ID.getName(), debitPendingTransaction.get(FieldType.PAY_ID.getName()));

			if (debitPendingTransaction.containsKey(FieldType.SUB_MERCHANT_ID.getName())
					&& StringUtils.isNotBlank(debitPendingTransaction.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				newFieldsObj.put(FieldType.SUB_MERCHANT_ID.getName(),
						debitPendingTransaction.get(FieldType.SUB_MERCHANT_ID.getName()));
			}

			if (debitPendingTransaction.containsKey(FieldType.RESELLER_ID.getName())
					&& StringUtils.isNotBlank(debitPendingTransaction.get(FieldType.RESELLER_ID.getName()))) {
				newFieldsObj.put(FieldType.RESELLER_ID.getName(),
						debitPendingTransaction.get(FieldType.RESELLER_ID.getName()));
			}

			newFieldsObj.put(FieldType.DATE_FROM.getName(), debitPendingTransaction.get(FieldType.DATE_FROM.getName()));
			newFieldsObj.put(FieldType.DATE_TO.getName(), debitPendingTransaction.get(FieldType.DATE_TO.getName()));

			newFieldsObj.put(Constants.AMOUNT.getValue(),
					String.valueOf(new BigDecimal(debitPendingTransaction.get(FieldType.MONTHLY_AMOUNT.getName()))
							.setScale(2, BigDecimal.ROUND_HALF_UP)));
			newFieldsObj.put(FieldType.MONTHLY_AMOUNT.getName(),
					debitPendingTransaction.get(FieldType.MONTHLY_AMOUNT.getName()));
			newFieldsObj.put(FieldType.TOTAL_AMOUNT.getName(),
					debitPendingTransaction.get(FieldType.TOTAL_AMOUNT.getName()));
			newFieldsObj.put(FieldType.PAYMENT_TYPE.getName(),
					debitPendingTransaction.get(FieldType.PAYMENT_TYPE.getName()));
			newFieldsObj.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));
			newFieldsObj.put(FieldType.UMN.getName(), debitPendingTransaction.get(FieldType.UMN.getName()));
			newFieldsObj.put(FieldType.REMARKS.getName(), debitPendingTransaction.get(FieldType.REMARKS.getName()));
			newFieldsObj.put(FieldType.NUMBER_OF_RETRY.getName(),
					debitPendingTransaction.get(FieldType.NUMBER_OF_RETRY.getName()));
			newFieldsObj.put(FieldType.SEQUENCE_NO.getName(),
					debitPendingTransaction.get(FieldType.SEQUENCE_NO.getName()));
			newFieldsObj.put(FieldType.PURPOSE.getName(), debitPendingTransaction.get(FieldType.PURPOSE.getName()));
			newFieldsObj.put(FieldType.PAYER_ADDRESS.getName(),
					debitPendingTransaction.get(FieldType.PAYER_ADDRESS.getName()));
			newFieldsObj.put(FieldType.DUE_DATE.getName(), debitPendingTransaction.get(FieldType.DUE_DATE.getName()));
			newFieldsObj.put(FieldType.REGISTRATION_DATE.getName(),
					debitPendingTransaction.get(FieldType.REGISTRATION_DATE.getName()));
			newFieldsObj.put(FieldType.PG_REF_NUM.getName(),
					debitPendingTransaction.get(FieldType.PG_REF_NUM.getName()));
			newFieldsObj.put(FieldType.ORDER_ID.getName(), debitPendingTransaction.get(FieldType.ORDER_ID.getName()));
			newFieldsObj.put(FieldType.TXN_ID.getName(), newId);
			newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(),
					debitPendingTransaction.get(FieldType.ORIG_TXN_ID.getName()));
			newFieldsObj.put(FieldType.TENURE.getName(), debitPendingTransaction.get(FieldType.TENURE.getName()));
			newFieldsObj.put(FieldType.NOTIFICATION_DATE.getName(),
					debitPendingTransaction.get(FieldType.NOTIFICATION_DATE.getName()));
			newFieldsObj.put(FieldType.FREQUENCY.getName(), debitPendingTransaction.get(FieldType.FREQUENCY.getName()));
			newFieldsObj.put(FieldType.CUST_EMAIL.getName(),
					debitPendingTransaction.get(FieldType.CUST_EMAIL.getName()));
			newFieldsObj.put(FieldType.CUST_PHONE.getName(),
					debitPendingTransaction.get(FieldType.CUST_PHONE.getName()));

			if (responseJson.get("response").toString().equalsIgnoreCase("0")
					&& responseJson.get("success").toString().equalsIgnoreCase("true")) {

				newFieldsObj.put(FieldType.STATUS.getName(), StatusType.NOTIFIED.getName());
				newFieldsObj.put(FieldType.RRN.getName(), responseJson.get("BankRRN").toString());
				newFieldsObj.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				newFieldsObj.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
				newFieldsObj.put(FieldType.PG_RESPONSE_STATUS.getName(), responseJson.get("success").toString());
				newFieldsObj.put(FieldType.PG_RESPONSE_MSG.getName(), responseJson.get("message").toString());
				newFieldsObj.put(FieldType.PG_RESP_CODE.getName(), responseJson.get("response").toString());
				Document insertPendingDebitTxn = new Document(newFieldsObj);
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					collection.insertOne(dataEncDecTool.encryptDocument(insertPendingDebitTxn));
				} else {
					collection.insertOne(insertPendingDebitTxn);
				}
				// collection.insertOne(insertPendingDebitTxn);
				logger.info("Notification transaction for debit insert successfully " + newFieldsObj);

			} else {
				setData.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				setData.put(FieldType.RRN.getName(), responseJson.get("BankRRN").toString());
				setData.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
				setData.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				setData.put(FieldType.PG_RESPONSE_STATUS.getName(), responseJson.get("success").toString());
				setData.put(FieldType.PG_RESPONSE_MSG.getName(), responseJson.get("message").toString());
				setData.put(FieldType.PG_RESP_CODE.getName(), responseJson.get("response").toString());
				setData.put(FieldType.NUMBER_OF_RETRY.getName(), String.valueOf(
						Integer.parseInt(debitPendingTransaction.get(FieldType.NUMBER_OF_RETRY.getName())) + 1));
				setData.put(FieldType.UPDATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));

				Document update = new Document();
				update.put("$set", setData);
				collection.updateOne(queryForUpdate, update);
				logger.info("Notification transaction for debit  failed " + setData);
			}

		} catch (Exception ex) {
			logger.error("Exception caught while update fields by notification response ", ex);
		}
	}

	@SuppressWarnings({ "static-access", "unchecked" })
	public void updateByDebitTransactionResponse(JSONObject responseJson,
			Map<String, String> debitNotificationTransaction) {
		logger.info("inside updateByNotificationResponse to update debit transaction DB fields");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));

			Document queryForUpdate = new Document();
			queryForUpdate.put(FieldType.PG_REF_NUM.getName(), responseJson.get("merchantTranId").toString());
			Document setData = new Document();

			/*
			 * { "response" : "0", "merchantId" : "106161", "subMerchantId" : "12234",
			 * "terminalId" : "5411", "BankRRN" : "615519221396", "merchantTranId" :
			 * "612411454593", "amount" : "12", "success" : "true", "message" :
			 * "Transaction Successful" }
			 */

			BasicDBObject newFieldsObj = new BasicDBObject();
			String newId = TransactionManager.getNewTransactionId();

			newFieldsObj.put("_id", newId);
			newFieldsObj.put(FieldType.TXNTYPE.getName(),
					debitNotificationTransaction.get(FieldType.TXNTYPE.getName()));
			newFieldsObj.put(FieldType.PAY_ID.getName(), debitNotificationTransaction.get(FieldType.PAY_ID.getName()));

			if (debitNotificationTransaction.containsKey(FieldType.SUB_MERCHANT_ID.getName())
					&& StringUtils.isNotBlank(debitNotificationTransaction.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				newFieldsObj.put(FieldType.SUB_MERCHANT_ID.getName(),
						debitNotificationTransaction.get(FieldType.SUB_MERCHANT_ID.getName()));
			}

			if (debitNotificationTransaction.containsKey(FieldType.RESELLER_ID.getName())
					&& StringUtils.isNotBlank(debitNotificationTransaction.get(FieldType.RESELLER_ID.getName()))) {
				newFieldsObj.put(FieldType.RESELLER_ID.getName(),
						debitNotificationTransaction.get(FieldType.RESELLER_ID.getName()));
			}

			newFieldsObj.put(FieldType.DATE_FROM.getName(),
					debitNotificationTransaction.get(FieldType.DATE_FROM.getName()));
			newFieldsObj.put(FieldType.DATE_TO.getName(),
					debitNotificationTransaction.get(FieldType.DATE_TO.getName()));

			newFieldsObj.put(Constants.AMOUNT.getValue(),
					String.valueOf(new BigDecimal(debitNotificationTransaction.get(FieldType.MONTHLY_AMOUNT.getName()))
							.setScale(2, BigDecimal.ROUND_HALF_UP)));
			newFieldsObj.put(FieldType.MONTHLY_AMOUNT.getName(),
					debitNotificationTransaction.get(FieldType.MONTHLY_AMOUNT.getName()));
			newFieldsObj.put(FieldType.TOTAL_AMOUNT.getName(),
					debitNotificationTransaction.get(FieldType.TOTAL_AMOUNT.getName()));
			newFieldsObj.put(FieldType.PAYMENT_TYPE.getName(),
					debitNotificationTransaction.get(FieldType.PAYMENT_TYPE.getName()));
			newFieldsObj.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));
			newFieldsObj.put(FieldType.UMN.getName(), debitNotificationTransaction.get(FieldType.UMN.getName()));
			newFieldsObj.put(FieldType.REMARKS.getName(),
					debitNotificationTransaction.get(FieldType.REMARKS.getName()));
			newFieldsObj.put(FieldType.NUMBER_OF_RETRY.getName(),
					debitNotificationTransaction.get(FieldType.NUMBER_OF_RETRY.getName()));
			newFieldsObj.put(FieldType.SEQUENCE_NO.getName(),
					debitNotificationTransaction.get(FieldType.SEQUENCE_NO.getName()));
			newFieldsObj.put(FieldType.PURPOSE.getName(),
					debitNotificationTransaction.get(FieldType.PURPOSE.getName()));
			newFieldsObj.put(FieldType.PAYER_ADDRESS.getName(),
					debitNotificationTransaction.get(FieldType.PAYER_ADDRESS.getName()));
			newFieldsObj.put(FieldType.DUE_DATE.getName(),
					debitNotificationTransaction.get(FieldType.DUE_DATE.getName()));
			newFieldsObj.put(FieldType.REGISTRATION_DATE.getName(),
					debitNotificationTransaction.get(FieldType.REGISTRATION_DATE.getName()));
			newFieldsObj.put(FieldType.PG_REF_NUM.getName(),
					debitNotificationTransaction.get(FieldType.PG_REF_NUM.getName()));
			newFieldsObj.put(FieldType.ORDER_ID.getName(),
					debitNotificationTransaction.get(FieldType.ORDER_ID.getName()));
			newFieldsObj.put(FieldType.TXN_ID.getName(), newId);
			newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(),
					debitNotificationTransaction.get(FieldType.ORIG_TXN_ID.getName()));
			newFieldsObj.put(FieldType.TENURE.getName(), debitNotificationTransaction.get(FieldType.TENURE.getName()));
			newFieldsObj.put(FieldType.NOTIFICATION_DATE.getName(),
					debitNotificationTransaction.get(FieldType.NOTIFICATION_DATE.getName()));
			newFieldsObj.put(FieldType.FREQUENCY.getName(),
					debitNotificationTransaction.get(FieldType.FREQUENCY.getName()));
			newFieldsObj.put(FieldType.CUST_EMAIL.getName(),
					debitNotificationTransaction.get(FieldType.CUST_EMAIL.getName()));
			newFieldsObj.put(FieldType.CUST_PHONE.getName(),
					debitNotificationTransaction.get(FieldType.CUST_PHONE.getName()));

			if (responseJson.get("response").toString().equalsIgnoreCase("0")
					&& responseJson.get("success").toString().equalsIgnoreCase("true")) {

				newFieldsObj.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
				newFieldsObj.put(FieldType.RRN.getName(), responseJson.get("BankRRN").toString());
				newFieldsObj.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				newFieldsObj.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
				newFieldsObj.put(FieldType.PG_RESPONSE_STATUS.getName(), responseJson.get("success").toString());
				newFieldsObj.put(FieldType.PG_RESPONSE_MSG.getName(), responseJson.get("message").toString());
				newFieldsObj.put(FieldType.PG_RESP_CODE.getName(), responseJson.get("response").toString());
				Document insertPendingDebitTxn = new Document(newFieldsObj);
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					collection.insertOne(dataEncDecTool.encryptDocument(insertPendingDebitTxn));
				} else {
					collection.insertOne(insertPendingDebitTxn);
				}
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						try {
							fieldsDao.sendCallbackAfterUpiAutoPayAmountDebit(new Fields(newFieldsObj.toMap()));
						} catch (Exception e) {
							logger.error("Exception ", e);
						}
					}
				};

				propertiesManager.executorImpl(runnable);
				// collection.insertOne(insertPendingDebitTxn);

			} else {
				setData.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				setData.put(FieldType.RRN.getName(), responseJson.get("BankRRN").toString());
				setData.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
				setData.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				setData.put(FieldType.PG_RESPONSE_STATUS.getName(), responseJson.get("success").toString());
				setData.put(FieldType.PG_RESPONSE_MSG.getName(), responseJson.get("message").toString());
				setData.put(FieldType.PG_RESP_CODE.getName(), responseJson.get("response").toString());
				setData.put(FieldType.NUMBER_OF_RETRY.getName(), String.valueOf(
						Integer.parseInt(debitNotificationTransaction.get(FieldType.NUMBER_OF_RETRY.getName())) + 1));
				setData.put(FieldType.UPDATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));
				Document update = new Document();
				update.put("$set", setData);
				collection.updateOne(queryForUpdate, update);
				newFieldsObj.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				newFieldsObj.put(FieldType.RRN.getName(), responseJson.get("BankRRN").toString());
				newFieldsObj.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
				newFieldsObj.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						try {
							fieldsDao.sendCallbackAfterUpiAutoPayAmountDebit(new Fields(newFieldsObj.toMap()));
						} catch (Exception e) {
							logger.error("Exception ", e);
						}
					}
				};

				propertiesManager.executorImpl(runnable);
			}

		} catch (Exception ex) {
			logger.error("Exception caught while update fields by debit txn response ", ex);
		}
	}

	@SuppressWarnings("static-access")
	public void insertTransactionPendingDocAutoPay(String pgRefNum) {

		logger.info("inside insertTransactionPendingDocAutoPay according to tenure ");
		int count = 1;
		HashMap<String, String> mandateDetailMap = new HashMap<String, String>();
		// List<String> startDateEndDateList = new ArrayList<String>();
		try {
			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REGISTRATION.getName()));
			queryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			BasicDBObject query = new BasicDBObject("$and", queryList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			try {
				while (cursor.hasNext()) {

					Document doc = cursor.next();
					mandateDetailMap.put(FieldType.DATE_FROM.getName(), doc.getString(FieldType.DATE_FROM.getName()));
					mandateDetailMap.put(FieldType.DATE_TO.getName(), doc.getString(FieldType.DATE_TO.getName()));
					mandateDetailMap.put(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName()));
					mandateDetailMap.put(FieldType.AMOUNT.getName(), doc.getString(FieldType.AMOUNT.getName()));
					mandateDetailMap.put(FieldType.MONTHLY_AMOUNT.getName(),
							doc.getString(FieldType.MONTHLY_AMOUNT.getName()));
					mandateDetailMap.put(FieldType.TOTAL_AMOUNT.getName(),
							doc.getString(FieldType.TOTAL_AMOUNT.getName()));
					mandateDetailMap.put(FieldType.PAYER_ADDRESS.getName(),
							doc.getString(FieldType.PAYER_ADDRESS.getName()));
					mandateDetailMap.put(FieldType.NOTE.getName(), doc.getString(FieldType.NOTE.getName()));
					mandateDetailMap.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
					mandateDetailMap.put(FieldType.ORIG_TXN_ID.getName(),
							doc.getString(FieldType.ORIG_TXN_ID.getName()));
					mandateDetailMap.put(FieldType.TENURE.getName(), doc.getString(FieldType.TENURE.getName()));
					mandateDetailMap.put(FieldType.FREQUENCY.getName(), doc.getString(FieldType.FREQUENCY.getName()));
					mandateDetailMap.put(FieldType.PURPOSE.getName(), doc.getString(FieldType.PURPOSE.getName()));
					mandateDetailMap.put(FieldType.AMOUNT_LIMIT.getName(),
							doc.getString(FieldType.AMOUNT_LIMIT.getName()));
					mandateDetailMap.put(FieldType.REMARKS.getName(), doc.getString(FieldType.REMARKS.getName()));
					mandateDetailMap.put(FieldType.REQUEST_TYPE.toString(),
							doc.getString(FieldType.REQUEST_TYPE.toString()));
					mandateDetailMap.put(FieldType.DEBIT_DAY.getName(), doc.getString(FieldType.DEBIT_DAY.getName()));
					mandateDetailMap.put(FieldType.DEBIT_RULE.getName(), doc.getString(FieldType.DEBIT_RULE.getName()));
					mandateDetailMap.put(FieldType.CUST_EMAIL.getName(), doc.getString(FieldType.CUST_EMAIL.getName()));
					mandateDetailMap.put(FieldType.CUST_PHONE.getName(), doc.getString(FieldType.CUST_PHONE.getName()));
					mandateDetailMap.put(FieldType.CREATE_DATE.getName(),
							doc.getString(FieldType.CREATE_DATE.getName()));

					if (doc.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						mandateDetailMap.put(FieldType.SUB_MERCHANT_ID.getName(),
								doc.getString(FieldType.SUB_MERCHANT_ID.getName()));
					} else {
						mandateDetailMap.put(FieldType.SUB_MERCHANT_ID.getName(), "");
					}

					if (doc.containsKey(FieldType.RESELLER_ID.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_ID.getName()))) {
						mandateDetailMap.put(FieldType.RESELLER_ID.getName(),
								doc.getString(FieldType.RESELLER_ID.getName()));
					}

					if (doc.containsKey(FieldType.UMN.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.UMN.getName()))) {
						mandateDetailMap.put(FieldType.UMN.getName(), doc.getString(FieldType.UMN.getName()));
					} else {
						mandateDetailMap.put(FieldType.UMN.getName(), Constants.NA.getValue());
					}
					if (doc.containsKey(FieldType.RRN.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.RRN.getName()))) {
						mandateDetailMap.put(FieldType.RRN.getName(), doc.getString(FieldType.RRN.getName()));
					} else {
						mandateDetailMap.put(FieldType.RRN.getName(), Constants.NA.getValue());
					}

				}

				if (!mandateDetailMap.isEmpty()) {

					List<String> dateList = null;

					if (mandateDetailMap.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("MT")
							|| mandateDetailMap.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("AS")) {
						dateList = fieldsDao.getDueDateList(mandateDetailMap.get(FieldType.DATE_FROM.getName()), "",
								mandateDetailMap.get(FieldType.TENURE.getName()),
								mandateDetailMap.get(FieldType.PAY_ID.getName()),
								mandateDetailMap.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");

					} else if (mandateDetailMap.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("QT")) {
						dateList = getDueDateListForMonth(mandateDetailMap.get(FieldType.DATE_FROM.getName()), "",
								mandateDetailMap.get(FieldType.TENURE.getName()), 3,
								mandateDetailMap.get(FieldType.PAY_ID.getName()),
								mandateDetailMap.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");

					} else if (mandateDetailMap.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("HY")) {
						dateList = getDueDateListForMonth(mandateDetailMap.get(FieldType.DATE_FROM.getName()), "",
								mandateDetailMap.get(FieldType.TENURE.getName()), 6,
								mandateDetailMap.get(FieldType.PAY_ID.getName()),
								mandateDetailMap.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");

					} else if (mandateDetailMap.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("YR")) {
						dateList = getDueDateListForMonth(mandateDetailMap.get(FieldType.DATE_FROM.getName()), "",
								mandateDetailMap.get(FieldType.TENURE.getName()), 12,
								mandateDetailMap.get(FieldType.PAY_ID.getName()),
								mandateDetailMap.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");

					} else if (mandateDetailMap.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("BM")) {
						dateList = getDueDateListForDays(mandateDetailMap.get(FieldType.DATE_FROM.getName()), "",
								mandateDetailMap.get(FieldType.TENURE.getName()), 15,
								mandateDetailMap.get(FieldType.PAY_ID.getName()),
								mandateDetailMap.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");

					} else if (mandateDetailMap.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("WK")) {
						dateList = getDueDateListForDays(mandateDetailMap.get(FieldType.DATE_FROM.getName()), "",
								mandateDetailMap.get(FieldType.TENURE.getName()), 7,
								mandateDetailMap.get(FieldType.PAY_ID.getName()),
								mandateDetailMap.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");

					} else if (mandateDetailMap.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("DL")) {
						dateList = getDueDateListForDays(mandateDetailMap.get(FieldType.DATE_FROM.getName()), "",
								mandateDetailMap.get(FieldType.TENURE.getName()), 1,
								mandateDetailMap.get(FieldType.PAY_ID.getName()),
								mandateDetailMap.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");

					} else {
						dateList = getDueDateListForOneTime(mandateDetailMap.get(FieldType.DATE_FROM.getName()), "",
								mandateDetailMap.get(FieldType.PAY_ID.getName()),
								mandateDetailMap.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");
					}

					/*
					 * startDateEndDateList.add(dateList.get(0));
					 * startDateEndDateList.add(dateList.get(dateList.size()-1)) ;
					 */

					for (String dueDate : dateList) {

						String newPgRefNum = TransactionManager.getNewTransactionId();
						BasicDBObject newFieldsObj = new BasicDBObject();

						newFieldsObj.put("_id", newPgRefNum);
						newFieldsObj.put(FieldType.DATE_FROM.getName(),
								mandateDetailMap.get(FieldType.DATE_FROM.getName()));
						newFieldsObj.put(FieldType.DATE_TO.getName(),
								mandateDetailMap.get(FieldType.DATE_TO.getName()));
						newFieldsObj.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
						newFieldsObj.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());

						newFieldsObj.put(Constants.AMOUNT.getValue(),
								String.valueOf(new BigDecimal(mandateDetailMap.get(FieldType.MONTHLY_AMOUNT.getName()))
										.setScale(2, BigDecimal.ROUND_HALF_UP)));

						newFieldsObj.put(FieldType.MONTHLY_AMOUNT.getName(),
								mandateDetailMap.get(FieldType.MONTHLY_AMOUNT.getName()));
						newFieldsObj.put(FieldType.TOTAL_AMOUNT.getName(),
								mandateDetailMap.get(FieldType.TOTAL_AMOUNT.getName()));
						newFieldsObj.put(FieldType.PAY_ID.getName(), mandateDetailMap.get(FieldType.PAY_ID.getName()));

						if (mandateDetailMap.containsKey(FieldType.SUB_MERCHANT_ID.getName())
								&& StringUtils.isNotBlank(mandateDetailMap.get(FieldType.SUB_MERCHANT_ID.getName()))) {
							newFieldsObj.put(FieldType.SUB_MERCHANT_ID.getName(),
									mandateDetailMap.get(FieldType.SUB_MERCHANT_ID.getName()));
						}

						if (mandateDetailMap.containsKey(FieldType.RESELLER_ID.getName())
								&& StringUtils.isNotBlank(mandateDetailMap.get(FieldType.RESELLER_ID.getName()))) {
							newFieldsObj.put(FieldType.RESELLER_ID.getName(),
									mandateDetailMap.get(FieldType.RESELLER_ID.getName()));
						}

						newFieldsObj.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));
						newFieldsObj.put(FieldType.REGISTRATION_DATE.getName(),
								mandateDetailMap.get(FieldType.CREATE_DATE.getName()));
						newFieldsObj.put(FieldType.PAYER_ADDRESS.getName(),
								mandateDetailMap.get(FieldType.PAYER_ADDRESS.getName()));
						newFieldsObj.put(FieldType.NOTE.getName(), mandateDetailMap.get(FieldType.NOTE.getName()));
						newFieldsObj.put(FieldType.CUST_EMAIL.getName(),
								mandateDetailMap.get(FieldType.CUST_EMAIL.getName()));
						newFieldsObj.put(FieldType.CUST_PHONE.getName(),
								mandateDetailMap.get(FieldType.CUST_PHONE.getName()));
						newFieldsObj.put(FieldType.SEQUENCE_NO.getName(), String.valueOf(count++));
						newFieldsObj.put(FieldType.NUMBER_OF_RETRY.getName(), "0");
						newFieldsObj.put(FieldType.FREQUENCY.getName(),
								mandateDetailMap.get(FieldType.FREQUENCY.getName()));
						newFieldsObj.put(FieldType.ORDER_ID.getName(),
								mandateDetailMap.get(FieldType.ORDER_ID.getName()));
						newFieldsObj.put(FieldType.PG_REF_NUM.getName(), newPgRefNum);
						newFieldsObj.put(FieldType.TXN_ID.getName(), newPgRefNum);
						newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(),
								mandateDetailMap.get(FieldType.ORIG_TXN_ID.getName()));
						newFieldsObj.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getCode());
						newFieldsObj.put(FieldType.PURPOSE.getName(),
								mandateDetailMap.get(FieldType.PURPOSE.getName()));
						newFieldsObj.put(FieldType.REMARKS.getName(),
								mandateDetailMap.get(FieldType.REMARKS.getName()));
						newFieldsObj.put(FieldType.RRN.getName(), mandateDetailMap.get(FieldType.RRN.getName()));

						newFieldsObj.put(FieldType.UMN.getName(), mandateDetailMap.get(FieldType.UMN.getName()));
						newFieldsObj.put(FieldType.TENURE.getName(), mandateDetailMap.get(FieldType.TENURE.getName()));
						newFieldsObj.put(FieldType.NOTIFICATION_DATE.getName(), calculateNotificationDate(dueDate));
						newFieldsObj.put(FieldType.DUE_DATE.getName(), dueDate);

						Document insertPendingDebitTxn = new Document(newFieldsObj);
						MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap
								.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));
						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

							coll.insertOne(dataEncDecTool.encryptDocument(insertPendingDebitTxn));
						} else {
							coll.insertOne(insertPendingDebitTxn);
						}
						// coll.insertOne(insertPendingDebitTxn);
					}
				}
			} finally {
				cursor.close();
			}

		} catch (Exception exception) {
			String message = "Exception while insert pending debit txn in DB ";
			logger.error(message, exception);
		}
		// return startDateEndDateList;
	}

	@SuppressWarnings("static-access")
	public Map<String, String> getTransactionByPgRefNum(String pgRefNum, String txnType, String status) {

		HashMap<String, String> debitTransactionMap = new HashMap<String, String>();
		try {

			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));
			queryList.add(new BasicDBObject(FieldType.STATUS.getName(), status));

			BasicDBObject query = new BasicDBObject("$and", queryList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();

				debitTransactionMap.put(FieldType.AMOUNT.getName(), doc.getString(FieldType.AMOUNT.getName()));
				debitTransactionMap.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
				debitTransactionMap.put(FieldType.REMARKS.getName(), doc.getString(FieldType.REMARKS.getName()));
				debitTransactionMap.put(FieldType.NUMBER_OF_RETRY.getName(),
						doc.getString(FieldType.NUMBER_OF_RETRY.getName()));
				debitTransactionMap.put(FieldType.SEQUENCE_NO.getName(),
						doc.getString(FieldType.SEQUENCE_NO.getName()));
				debitTransactionMap.put(FieldType.PURPOSE.getName(), doc.getString(FieldType.PURPOSE.getName()));
				debitTransactionMap.put(FieldType.UMN.getName(), doc.getString(FieldType.UMN.getName()));
				debitTransactionMap.put(FieldType.PAYER_ADDRESS.getName(),
						doc.getString(FieldType.PAYER_ADDRESS.getName()));
				debitTransactionMap.put(FieldType.DUE_DATE.getName(), doc.getString(FieldType.DUE_DATE.getName()));
				debitTransactionMap.put(FieldType.REGISTRATION_DATE.getName(),
						doc.getString(FieldType.REGISTRATION_DATE.getName()));
				debitTransactionMap.put(FieldType.DATE_FROM.getName(), doc.getString(FieldType.DATE_FROM.getName()));
				debitTransactionMap.put(FieldType.DATE_TO.getName(), doc.getString(FieldType.DATE_TO.getName()));
				debitTransactionMap.put(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName()));
				debitTransactionMap.put(FieldType.MONTHLY_AMOUNT.getName(),
						doc.getString(FieldType.MONTHLY_AMOUNT.getName()));
				debitTransactionMap.put(FieldType.TOTAL_AMOUNT.getName(),
						doc.getString(FieldType.TOTAL_AMOUNT.getName()));
				debitTransactionMap.put(FieldType.NOTE.getName(), doc.getString(FieldType.NOTE.getName()));
				debitTransactionMap.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
				debitTransactionMap.put(FieldType.ORIG_TXN_ID.getName(),
						doc.getString(FieldType.ORIG_TXN_ID.getName()));
				debitTransactionMap.put(FieldType.TENURE.getName(), doc.getString(FieldType.TENURE.getName()));
				debitTransactionMap.put(FieldType.NOTIFICATION_DATE.getName(),
						doc.getString(FieldType.NOTIFICATION_DATE.getName()));
				debitTransactionMap.put(FieldType.FREQUENCY.getName(), doc.getString(FieldType.FREQUENCY.getName()));
				debitTransactionMap.put(FieldType.CUST_EMAIL.getName(), doc.getString(FieldType.CUST_EMAIL.getName()));
				debitTransactionMap.put(FieldType.CUST_PHONE.getName(), doc.getString(FieldType.CUST_PHONE.getName()));
			}
		} catch (Exception ex) {
			logger.error("Exception caught while get transaction by pgRefNum from DB ", ex);
		}
		return debitTransactionMap;
	}

	public String calculateNotificationDate(String dueDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar currentMonth = Calendar.getInstance();
		try {
			LocalDate start = LocalDate.parse(dueDate);
			LocalDate notifiedDate = start.minusDays(1);
			Date date = dateFormat.parse(notifiedDate.toString());
			currentMonth.setTime(date);

		} catch (Exception ex) {
			logger.error("exception while calculate notification date ", ex);
			return null;
		}
		return dateFormat.format(currentMonth.getTime());
	}

	@SuppressWarnings("static-access")
	public void updateByStatusEnquiry(JSONObject json) {

		logger.info("inside updateByCallBackResponse to update registration DB fields");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));

			Document queryForUpdate = new Document();
			queryForUpdate.put(FieldType.PG_REF_NUM.getName(), json.get("merchantTranId").toString());

			Document setData = new Document();

			/*
			 * { "response" : "0", "merchantId" : "106161", "subMerchantId" : "12234",
			 * "terminalId" : "5411", "OriginalBankRRN" : "615519221396", "merchantTranId" :
			 * "612411454593", "amount" : "12", "success" : "true", "message" :
			 * "Transaction Successful", "status" : "SUCCESS", "UMN" :
			 * "8fbadaeb18ff49fdbae7793faa8178d3@upi" }
			 */

			if (json.get("status").toString().equalsIgnoreCase("CREATE-SUCCESS")
					&& json.get("response").toString().equalsIgnoreCase("0")) {

				setData.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
				setData.put(FieldType.UMN.getName(), json.get("UMN").toString());
				setData.put(FieldType.RRN.getName(), json.get("OriginalBankRRN").toString());
				setData.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				setData.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
				setData.put(FieldType.PG_RESPONSE_STATUS.getName(), json.get("status").toString());
				setData.put(FieldType.PG_RESP_CODE.getName(), json.get("response").toString());
				setData.put(FieldType.PG_RESPONSE_MSG.getName(), json.get("message").toString());

			} else {
				setData.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				setData.put(FieldType.UMN.getName(), Constants.NA.getValue());
				setData.put(FieldType.RRN.getName(), json.get("OriginalBankRRN").toString());
				setData.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
				setData.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				setData.put(FieldType.PG_RESPONSE_STATUS.getName(), json.get("status").toString());
				setData.put(FieldType.PG_RESP_CODE.getName(), json.get("response").toString());
				setData.put(FieldType.PG_RESPONSE_MSG.getName(), json.get("message").toString());
			}
			setData.put(FieldType.UPDATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));

			Document update = new Document();
			update.put("$set", setData);
			collection.updateOne(queryForUpdate, update);
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						Map<String, String> newFieldsObj = getTransactionByPgRefNum(
								json.get("merchantTranId").toString(), TransactionType.SALE.getName(),
								setData.get(FieldType.STATUS.getName()).toString());
						newFieldsObj.put(FieldType.STATUS.getName(),
								setData.get(FieldType.STATUS.getName()).toString());
						fieldsDao.sendCallbackAfterUpiAutoPayAmountDebit(new Fields(newFieldsObj));

					} catch (Exception e) {
						logger.error("Exception ", e);
					}
				}
			};

			propertiesManager.executorImpl(runnable);

		} catch (Exception ex) {
			logger.error("Exception caught while update fields by status equiry response ", ex);
		}
	}

	@SuppressWarnings("static-access")
	public void updateStatusEnquiryByCriteria(JSONObject json) {

		logger.info("inside updateByCallBackResponse to update registration DB fields");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));

			Document queryForUpdate = new Document();
			queryForUpdate.put(FieldType.PG_REF_NUM.getName(), json.get("merchantTranId").toString());

			Document setData = new Document();

			/*
			 * { "response" : "0", "merchantId" : "106161", "subMerchantId" : "12234",
			 * "terminalId" : "5411", "OriginalBankRRN" : "615519221396", "merchantTranId" :
			 * "612411454593", "Amount" : "12", 0 "payerVA" : " testing1@imobile ",
			 * "success" : "true", "message" : "Transaction Successful", "status" :
			 * "SUCCESS", 0 "TxnInitDate" : "20160715142352", 0 "TxnCompletionDate" :
			 * "20160715142352", "UMN" : "8fbadaeb18ff49fdbae7793faa8178d3@upi" }
			 */

			if (json.get("status").toString().equalsIgnoreCase("CREATE-SUCCESS")
					&& json.get("response").toString().equalsIgnoreCase("0")) {

				setData.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
				setData.put(FieldType.UMN.getName(), json.get("UMN").toString());
				setData.put(FieldType.RRN.getName(), json.get("OriginalBankRRN").toString());
				setData.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				setData.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
				setData.put(FieldType.PG_RESPONSE_STATUS.getName(), json.get("status").toString());
				setData.put(FieldType.PG_RESP_CODE.getName(), json.get("response").toString());
				setData.put(FieldType.PG_RESPONSE_MSG.getName(), json.get("message").toString());

			} else {
				setData.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				setData.put(FieldType.UMN.getName(), Constants.NA.getValue());
				setData.put(FieldType.RRN.getName(), json.get("OriginalBankRRN").toString());
				setData.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
				setData.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				setData.put(FieldType.PG_RESPONSE_STATUS.getName(), json.get("status").toString());
				setData.put(FieldType.PG_RESP_CODE.getName(), json.get("response").toString());
				setData.put(FieldType.PG_RESPONSE_MSG.getName(), json.get("message").toString());
			}
			setData.put(FieldType.UPDATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));

			Document update = new Document();
			update.put("$set", setData);
			collection.updateOne(queryForUpdate, update);
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						Map<String, String> newFieldsObj = getTransactionByPgRefNum(
								json.get("merchantTranId").toString(), TransactionType.SALE.getName(),
								setData.get(FieldType.STATUS.getName()).toString());
						newFieldsObj.put(FieldType.STATUS.getName(),
								setData.get(FieldType.STATUS.getName()).toString());
						fieldsDao.sendCallbackAfterUpiAutoPayAmountDebit(new Fields(newFieldsObj));

					} catch (Exception e) {
						logger.error("Exception ", e);
					}
				}
			};

			propertiesManager.executorImpl(runnable);
		} catch (Exception ex) {
			logger.error("Exception caught while update fields status enquiry by criteria response ", ex);
		}
	}

	public List<String> getDueDateListForMonth(String startDate, String endDate, String tenure, int monthDuration,
			String payId, String subMerchantPayId, String debitDurationType) {
		logger.info("Inside getDueDateListForQuarterly calculate quarterly/Half/year debit dates ");
		List<String> dateList = new ArrayList<String>();
		try {
			boolean durationFlag = true;
			boolean flag = true;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			int finalTenure = Integer.parseInt(tenure);
			while (finalTenure != 0) {

				LocalDate start = LocalDate.parse(startDate);
				if (durationFlag) {
					String debitDuration;

					if (StringUtils.isNotBlank(subMerchantPayId)) {
						if (debitDurationType.equalsIgnoreCase("autoPay")) {
							debitDuration = userDao.getUpiAutoPayDebitDurationByPayId(subMerchantPayId);
						} else {
							debitDuration = userDao.getDebitDurationByPayId(subMerchantPayId);
						}
						if (StringUtils.isNotBlank(debitDuration)) {
							start = start.plusDays(Long.parseLong(debitDuration));
						} else {
							start = start.plusDays(30);
						}

					} else {
						if (debitDurationType.equalsIgnoreCase("autoPay")) {
							debitDuration = userDao.getUpiAutoPayDebitDurationByPayId(payId);
						} else {
							debitDuration = userDao.getDebitDurationByPayId(payId);
						}
						if (StringUtils.isNotBlank(debitDuration)) {
							start = start.plusDays(Long.parseLong(debitDuration));
						} else {
							start = start.plusDays(30);
						}
					}
					durationFlag = false;
				}
				LocalDate end = start.plusMonths(monthDuration);
				LocalDate dateFrom = start;

				String startDateArr[] = start.toString().split("-");
				if (startDateArr[2].contains("31")) {

					while (!dateFrom.isAfter(end) && flag == true) {
						Date date = dateFormat.parse(dateFrom.toString());
						Calendar currentMonth = Calendar.getInstance();
						currentMonth.setTime(date);
						dateList.add(dateFormat.format(currentMonth.getTime()));
						finalTenure--;
						flag = false;
					}

					while (!dateFrom.isAfter(end) && finalTenure != 0) {

						String firstDate = dateList.get(0);
						String firstDateArr[] = firstDate.split("-");

						int lastDate = Integer.parseInt(firstDateArr[2]);

						Date date = dateFormat.parse(dateFrom.toString());
						Calendar currentMonth = Calendar.getInstance();
						currentMonth.setTime(date);
						currentMonth.add(Calendar.MONTH, monthDuration);

						int dd = currentMonth.getActualMaximum(Calendar.DATE);

						if (dd >= lastDate) {
							String ddd = dateFormat.format(currentMonth.getTime()).toString();
							String dddArr[] = ddd.split("-");
							dddArr[2] = String.valueOf(lastDate);

							StringBuilder finalDateBuilder = new StringBuilder();
							for (String ss : dddArr) {
								finalDateBuilder.append(ss).append("-");
							}
							finalDateBuilder.deleteCharAt(finalDateBuilder.length() - 1);
							String finalDate = finalDateBuilder.toString();
							dateList.add(finalDate);
							finalTenure--;
							dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());

						} else {

							String addDate = dateFormat.format(currentMonth.getTime());
							String dateArr[] = addDate.split("-");

							dateArr[2] = String.valueOf(dd);
							StringBuilder finalDateBuilder = new StringBuilder();
							for (String ss : dateArr) {
								finalDateBuilder.append(ss).append("-");
							}
							finalDateBuilder.deleteCharAt(finalDateBuilder.length() - 1);
							String finalDate = finalDateBuilder.toString();
							dateList.add(finalDate);
							finalTenure--;
							dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());
						}
						break;
					}

				} else if (startDateArr[2].contains("29") || startDateArr[2].contains("30")) {

					while (!dateFrom.isAfter(end) && flag == true) {
						Date date = dateFormat.parse(dateFrom.toString());
						Calendar currentMonth = Calendar.getInstance();
						currentMonth.setTime(date);
						dateList.add(dateFormat.format(currentMonth.getTime()));
						finalTenure--;
						flag = false;
					}

					while (!dateFrom.isAfter(end) && finalTenure != 0) {

						String firstDate = dateList.get(0);
						String firstDateArr[] = firstDate.split("-");

						int lastDate = Integer.parseInt(firstDateArr[2]);

						Date date = dateFormat.parse(dateFrom.toString());
						Calendar currentMonth = Calendar.getInstance();
						currentMonth.setTime(date);
						currentMonth.add(Calendar.MONTH, monthDuration);

						int maxDayInMonth = currentMonth.getActualMaximum(Calendar.DATE);

						if (maxDayInMonth >= lastDate) {
							String nextDate = dateFormat.format(currentMonth.getTime()).toString();
							String nextDateArr[] = nextDate.split("-");
							nextDateArr[2] = String.valueOf(lastDate);

							StringBuilder finalDateBuilder = new StringBuilder();
							for (String dt : nextDateArr) {
								finalDateBuilder.append(dt).append("-");
							}
							finalDateBuilder.deleteCharAt(finalDateBuilder.length() - 1);
							String finalDate = finalDateBuilder.toString();
							dateList.add(finalDate);
							finalTenure--;
							dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());

						} else {

							String addDate = dateFormat.format(currentMonth.getTime());
							String dateArr[] = addDate.split("-");

							dateArr[2] = String.valueOf(maxDayInMonth);
							StringBuilder finalDateBuilder = new StringBuilder();
							for (String dt : dateArr) {
								finalDateBuilder.append(dt).append("-");
							}
							finalDateBuilder.deleteCharAt(finalDateBuilder.length() - 1);
							String finalDate = finalDateBuilder.toString();
							dateList.add(finalDate);
							finalTenure--;
							dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());
						}
						break;
					}

				} else {

					while (!dateFrom.isAfter(end) && flag == true) {
						Date date = dateFormat.parse(dateFrom.toString());
						Calendar currentMonth = Calendar.getInstance();
						currentMonth.setTime(date);
						dateList.add(dateFormat.format(currentMonth.getTime()));
						finalTenure--;
						flag = false;
					}
					while (!dateFrom.isAfter(end) && finalTenure != 0) {

						String firstDate = dateList.get(0);
						String firstDateArr[] = firstDate.split("-");

						int lastDate = Integer.parseInt(firstDateArr[2]);

						Date date = dateFormat.parse(dateFrom.toString());
						Calendar currentMonth = Calendar.getInstance();
						currentMonth.setTime(date);
						currentMonth.add(Calendar.MONTH, monthDuration);

						int maxDayInMonth = currentMonth.getActualMaximum(Calendar.DATE);

						if (maxDayInMonth >= lastDate) {
							String nextMonthDate = dateFormat.format(currentMonth.getTime()).toString();
							String nextMonthDateArr[] = nextMonthDate.split("-");
							int length = (int) (Math.log10(lastDate));
							if (length == 0) {
								nextMonthDateArr[2] = "0" + String.valueOf(lastDate);
							} else {
								nextMonthDateArr[2] = String.valueOf(lastDate);
							}

							StringBuilder finalDateBuilder = new StringBuilder();
							for (String yymmdd : nextMonthDateArr) {
								finalDateBuilder.append(yymmdd).append("-");
							}
							finalDateBuilder.deleteCharAt(finalDateBuilder.length() - 1);
							String finalDate = finalDateBuilder.toString();
							dateList.add(finalDate);
							finalTenure--;
							dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());

						} else {

							String addDate = dateFormat.format(currentMonth.getTime());
							String dateArr[] = addDate.split("-");

							dateArr[2] = String.valueOf(maxDayInMonth);
							StringBuilder finalDateBuilder = new StringBuilder();
							for (String ss : dateArr) {
								finalDateBuilder.append(ss).append("-");
							}
							finalDateBuilder.deleteCharAt(finalDateBuilder.length() - 1);
							String finalDate = finalDateBuilder.toString();
							dateList.add(finalDate);
							finalTenure--;
							dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());
						}
						break;
					}
				}
				startDate = end.toString();
			}
		} catch (Exception ex) {
			logger.error("exception caught while create quarterly/Half/year date list ", ex);
		}
		return dateList;
	}

	public List<String> getDueDateListForDays(String startDate, String endDate, String tenure, int monthDuration,
			String payId, String subMerchantPayId, String debitDurationType) {
		logger.info("Inside getDueDateListForBiMonthly calculate bi-monthly/weekly/daily debit dates");
		List<String> dateList = new ArrayList<String>();
		try {
			boolean durationFlag = true;
			boolean flag = true;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			int finalTenure = Integer.parseInt(tenure);
			while (finalTenure != 0) {

				LocalDate start = LocalDate.parse(startDate);
				if (durationFlag) {
					String debitDuration;
					if (StringUtils.isNotBlank(endDate)) {
						if (StringUtils.isNotBlank(subMerchantPayId)) {

							if (debitDurationType.equalsIgnoreCase("autoPay")) {
								debitDuration = userDao.getUpiAutoPayDebitDurationByPayId(subMerchantPayId);
							} else {
								debitDuration = userDao.getDebitDurationByPayId(subMerchantPayId);
							}
							if (StringUtils.isNotBlank(debitDuration)) {
								start = start.plusDays(Long.parseLong(debitDuration));
							} else {
								start = start.plusDays(30);
							}
						} else {

							if (debitDurationType.equalsIgnoreCase("autoPay")) {
								debitDuration = userDao.getUpiAutoPayDebitDurationByPayId(payId);
							} else {
								debitDuration = userDao.getDebitDurationByPayId(payId);
							}
							if (StringUtils.isNotBlank(debitDuration)) {
								start = start.plusDays(Long.parseLong(debitDuration));
							} else {
								start = start.plusDays(30);
							}
						}
					}
					durationFlag = false;
				}
				LocalDate end = start.plusDays(monthDuration);
				LocalDate dateFrom = start;

				String startDateArr[] = start.toString().split("-");
				if (startDateArr[2].contains("31")) {

					while (!dateFrom.isAfter(end) && flag == true) {
						Date date = dateFormat.parse(dateFrom.toString());
						Calendar currentMonth = Calendar.getInstance();
						currentMonth.setTime(date);
						dateList.add(dateFormat.format(currentMonth.getTime()));
						finalTenure--;
						flag = false;
					}

					while (!dateFrom.isAfter(end) && finalTenure != 0) {

						String firstDate = dateList.get(0);
						String firstDateArr[] = firstDate.split("-");

						int lastDate = Integer.parseInt(firstDateArr[2]);

						Date date = dateFormat.parse(dateFrom.toString());
						Calendar currentMonth = Calendar.getInstance();
						currentMonth.setTime(date);
						currentMonth.add(Calendar.DAY_OF_MONTH, monthDuration);

						int maxDayInMonth = currentMonth.getActualMaximum(Calendar.DATE);

						if (maxDayInMonth >= lastDate) {
							String nextDate = dateFormat.format(currentMonth.getTime()).toString();
							dateList.add(nextDate);
							finalTenure--;
							dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());

						} else {

							String addDate = dateFormat.format(currentMonth.getTime());
							dateList.add(addDate);
							finalTenure--;
							dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());
						}
						break;
					}

				} else if (startDateArr[2].contains("29") || startDateArr[2].contains("30")) {

					while (!dateFrom.isAfter(end) && flag == true) {
						Date date = dateFormat.parse(dateFrom.toString());
						Calendar currentMonth = Calendar.getInstance();
						currentMonth.setTime(date);
						dateList.add(dateFormat.format(currentMonth.getTime()));
						finalTenure--;
						flag = false;
					}

					while (!dateFrom.isAfter(end) && finalTenure != 0) {

						String firstDate = dateList.get(0);
						String firstDateArr[] = firstDate.split("-");

						int lastDate = Integer.parseInt(firstDateArr[2]);

						Date date = dateFormat.parse(dateFrom.toString());
						Calendar currentMonth = Calendar.getInstance();
						currentMonth.setTime(date);
						currentMonth.add(Calendar.DAY_OF_MONTH, monthDuration);

						int maxDayInMonth = currentMonth.getActualMaximum(Calendar.DATE);

						if (maxDayInMonth >= lastDate) {
							String nextDate = dateFormat.format(currentMonth.getTime()).toString();
							dateList.add(nextDate);
							finalTenure--;
							dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());

						} else {

							String addDate = dateFormat.format(currentMonth.getTime());
							dateList.add(addDate);
							finalTenure--;
							dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());
						}
						break;
					}

				} else {

					while (!dateFrom.isAfter(end) && flag == true) {
						Date date = dateFormat.parse(dateFrom.toString());
						Calendar currentMonth = Calendar.getInstance();
						currentMonth.setTime(date);
						dateList.add(dateFormat.format(currentMonth.getTime()));
						finalTenure--;
						flag = false;
					}
					while (!dateFrom.isAfter(end) && finalTenure != 0) {

						String firstDate = dateList.get(0);
						String firstDateArr[] = firstDate.split("-");

						int lastDate = Integer.parseInt(firstDateArr[2]);

						Date date = dateFormat.parse(dateFrom.toString());
						Calendar currentMonth = Calendar.getInstance();
						currentMonth.setTime(date);
						currentMonth.add(Calendar.DAY_OF_MONTH, monthDuration);

						int maxDayInMonth = currentMonth.getActualMaximum(Calendar.DATE);

						if (maxDayInMonth >= lastDate) {
							String nextMonthDate = dateFormat.format(currentMonth.getTime()).toString();
							dateList.add(nextMonthDate);
							finalTenure--;
							dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());

						} else {

							String addDate = dateFormat.format(currentMonth.getTime());
							dateList.add(addDate);
							finalTenure--;
							dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());
						}
						break;
					}
				}
				startDate = end.toString();
			}
		} catch (Exception ex) {
			logger.error("exception caught while create bi-monthly/weekly/daily debit date list ", ex);
		}
		return dateList;
	}

	public List<String> getDueDateListForOneTime(String startDate, String endDate, String payId,
			String subMerchantPayId, String debitDurationType) {

		logger.info("Inside getDueDateListForOneTime calculate debit date list ");
		List<String> dateList = new ArrayList<String>();
		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

			LocalDate start = LocalDate.parse(startDate);
			if (StringUtils.isNotBlank(endDate)) {
				String debitDuration;
				if (StringUtils.isNotBlank(subMerchantPayId)) {
					if (debitDurationType.equalsIgnoreCase("autoPay")) {
						debitDuration = userDao.getUpiAutoPayDebitDurationByPayId(subMerchantPayId);
					} else {
						debitDuration = userDao.getDebitDurationByPayId(subMerchantPayId);
					}
					if (StringUtils.isNotBlank(debitDuration)) {
						start = start.plusDays(Long.parseLong(userDao.getDebitDurationByPayId(subMerchantPayId)));
					} else {
						start = start.plusDays(30);
					}
				} else {
					if (debitDurationType.equalsIgnoreCase("autoPay")) {
						debitDuration = userDao.getUpiAutoPayDebitDurationByPayId(payId);
					} else {
						debitDuration = userDao.getDebitDurationByPayId(payId);
					}
					if (StringUtils.isNotBlank(debitDuration)) {
						start = start.plusDays(Long.parseLong(userDao.getDebitDurationByPayId(payId)));
					} else {
						start = start.plusDays(30);
					}
				}
			}
			LocalDate dateFrom = start;

			Date date = dateFormat.parse(dateFrom.toString());
			Calendar currentMonth = Calendar.getInstance();
			currentMonth.setTime(date);
			dateList.add(dateFormat.format(currentMonth.getTime()));

		} catch (Exception ex) {
			logger.error("exception caught while create one time debit date list ", ex);
		}

		return dateList;
	}

	@SuppressWarnings({ "static-access", "unused" })
	public List<UpiAutoPay> fetchRegistrationDetails(String orderId, String umnNumber, String merchantPayId,
			String subMerchantPayId, String resellerId, String status, String txnType, String dateFrom, String dateTo) {
		logger.info("inside fetch Registration Details ");
		List<UpiAutoPay> registrationDetailsList = new ArrayList<UpiAutoPay>();

		try {
			String acqCharge = propertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_ACQUIRER_CHARGES.getValue());
			boolean isParameterised = false;

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			String currentDate = null;
			if (!dateFrom.isEmpty()) {

				if (!dateTo.isEmpty()) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REGISTRATION.getName()));

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
				isParameterised = true;
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}

			if (StringUtils.isNotBlank(resellerId) && !resellerId.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), resellerId));
			}

			if (!orderId.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			}

			if (!umnNumber.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.UMN.getName(), umnNumber));
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			BasicDBObject finalQuery = new BasicDBObject("$and", fianlList);

			logger.info("finalquery = " + finalQuery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			
			String tempPayId="";
			String tempOrderId="";			
			try {
				while (cursor.hasNext()) {
					Document dbObj = cursor.next();
					if (StringUtils.isNotBlank(dbObj.getString("IS_ENCRYPTED"))
							&& dbObj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
						dbObj = dataEncDecTool.decryptDocument(dbObj);
					}
					UpiAutoPay upiAutoPay = new UpiAutoPay();

					upiAutoPay.setMerchantName(
							userDao.getBusinessNameByPayId(dbObj.getString(FieldType.PAY_ID.getName())));

					if (dbObj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						upiAutoPay.setSubMerchantName(
								userDao.getBusinessNameByPayId(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName())));

						upiAutoPay.setSubMerchantPayId(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName()));
					} else {
						upiAutoPay.setSubMerchantName(Constants.NA.getValue());

						upiAutoPay.setSubMerchantPayId(Constants.NA.getValue());
					}
					upiAutoPay.setPayId(dbObj.getString(FieldType.PAY_ID.getName()));

					upiAutoPay.setOrderId(dbObj.getString(FieldType.ORDER_ID.getName()));

					if(tempPayId.equalsIgnoreCase(upiAutoPay.getPayId()) && tempOrderId.equalsIgnoreCase(upiAutoPay.getOrderId())) {
						continue;
					}else {
						tempPayId = upiAutoPay.getPayId();
						tempOrderId = upiAutoPay.getOrderId();
					}
					
					upiAutoPay.setPgRefNum(dbObj.getString(FieldType.PG_REF_NUM.getName()));
					upiAutoPay.setRrn(dbObj.getString(FieldType.RRN.getName()));

					if (dbObj.containsKey(FieldType.UMN.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.UMN.getName()))) {
						upiAutoPay.setUmnNumber(dbObj.getString(FieldType.UMN.getName()));
					} else {
						upiAutoPay.setUmnNumber(Constants.NA.getValue());
					}
					upiAutoPay.setCreateDate(dbObj.getString(FieldType.CREATE_DATE.getName()));

					// - Debit Duration (In days)
					
					if (dbObj.containsKey(FieldType.PAYER_ADDRESS.getName()) && StringUtils.isNotBlank(dbObj.getString(FieldType.PAYER_ADDRESS.getName()))) {
						upiAutoPay.setPayerAddress(fields.maskEmail(dbObj.getString(FieldType.PAYER_ADDRESS.getName())));
					}
					upiAutoPay.setCustPhone(fields.fieldMask(dbObj.getString(FieldType.CUST_PHONE.getName())));
					upiAutoPay.setCustEmail(fields.maskEmail(dbObj.getString(FieldType.CUST_EMAIL.getName())));
					upiAutoPay.setFrequency(
							AutoPayFrequency.getAutoPayFrequencyName(dbObj.getString(FieldType.FREQUENCY.getName())));
					upiAutoPay.setTenure(dbObj.getString(FieldType.TENURE.getName()));
					// - Total Debits
					upiAutoPay.setStartDate(dbObj.getString(FieldType.DATE_FROM.getName()));
					upiAutoPay.setEndDate(dbObj.getString(FieldType.DATE_TO.getName()));

					upiAutoPay.setAmount(dbObj.getString(FieldType.AMOUNT.getName()));
					upiAutoPay.setMaxAmount(dbObj.getString(FieldType.MONTHLY_AMOUNT.getName()));

					// - SUF
					upiAutoPay.setPaymentType(
							PaymentType.getpaymentName(dbObj.getString(FieldType.PAYMENT_TYPE.getName())));
					upiAutoPay.setAcquirerCharges(acqCharge);
					upiAutoPay.setTotalAmount(dbObj.getString(FieldType.TOTAL_AMOUNT.getName()));
					upiAutoPay.setStatus(dbObj.getString(FieldType.STATUS.getName()));
					upiAutoPay.seteMandateUrl(dbObj.getString(FieldType.EMANDATE_URL.getName()));

					registrationDetailsList.add(upiAutoPay);
				}
			} finally {
				cursor.close();
			}
		} catch (Exception ex) {
			logger.error("exception caught while upi autopay fetch registration details from DB ", ex);

		}
		return registrationDetailsList;
	}

	@SuppressWarnings("static-access")
	public boolean checkDuplicateOrderId(String orderId, String payId, String subMerchantPayId, String collectionName) {
		try {
			String dbOrderId;
			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				queryList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			if (collectionName.equalsIgnoreCase("upiAutoPayCollection")) {
				queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REGISTRATION.getName()));
			} else {
				queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "Registration"));
			}

			BasicDBObject query = new BasicDBObject("$and", queryList);

			logger.info("Inside upiautopayDao, check duplicate orderId final query For Registration = " + query);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + collectionName));
			MongoCursor<Document> cursor = coll.find(query).sort(new BasicDBObject(FieldType.CREATE_DATE.getName(), -1))
					.iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();

				dbOrderId = doc.getString(FieldType.ORDER_ID.toString());
				if (StringUtils.isNotBlank(dbOrderId) && dbOrderId.equalsIgnoreCase(orderId)) {
					return true;
				}
			}
		} catch (Exception ex) {
			logger.error("exception caught while check duplicate orderId for registration ", ex);
			return false;
		}
		return false;
	}

	@SuppressWarnings({ "static-access", "unused" })
	public List<UpiAutoPay> fetchAutopayTransactionReport(String orderId, String umnNumber, String merchantPayId,
			String subMerchantPayId, String resellerId, String status, String txnType, String fromDate, String toDate) {

		logger.info("Inside fetchAutopayTransactionReport get data from DB for transaction report ");
		boolean isParameterised = false;

		String acqCharge = propertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_ACQUIRER_CHARGES.getValue());
		List<UpiAutoPay> autoPayList = new ArrayList<UpiAutoPay>();
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			String currentDate = null;
			if (!fromDate.isEmpty()) {

				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
				isParameterised = true;
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}

			if (StringUtils.isNotBlank(resellerId) && !resellerId.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), resellerId));
			}

			if (!orderId.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			}

			if (!umnNumber.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.UMN.getName(), umnNumber));
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (StringUtils.isNotBlank(umnNumber) || StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateQuery.isEmpty()) {
					allConditionQueryList.add(dateQuery);
				}
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			try {
				while (cursor.hasNext()) {
					Document dbObj = cursor.next();
					if (StringUtils.isNotBlank(dbObj.getString("IS_ENCRYPTED"))
							&& dbObj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
						dbObj = dataEncDecTool.decryptDocument(dbObj);
					}
					UpiAutoPay autoPayDetailsMap = new UpiAutoPay();

					autoPayDetailsMap.setMerchantName(
							userDao.getBusinessNameByPayId(dbObj.getString(FieldType.PAY_ID.getName())));
					autoPayDetailsMap.setAcquirerCharges(acqCharge);

					if (dbObj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						autoPayDetailsMap.setSubMerchantName(
								userDao.getBusinessNameByPayId(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName())));

						autoPayDetailsMap.setSubMerchantPayId(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName()));
					}
					autoPayDetailsMap.setPayId(dbObj.getString(FieldType.PAY_ID.getName()));
					autoPayDetailsMap.setOrderId(dbObj.getString(FieldType.ORDER_ID.getName()));
					if (dbObj.containsKey(FieldType.UMN.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.UMN.getName()))) {

						autoPayDetailsMap.setUmnNumber(dbObj.getString(FieldType.UMN.getName()));
					} else {
						autoPayDetailsMap.setUmnNumber(Constants.NA.getValue());
					}
					autoPayDetailsMap.setCreateDate(dbObj.getString(FieldType.CREATE_DATE.getName()));
					autoPayDetailsMap
							.setPayerAddress(fields.maskEmail(dbObj.getString(FieldType.PAYER_ADDRESS.getName())));
					autoPayDetailsMap.setPgRefNum(dbObj.getString(FieldType.PG_REF_NUM.getName()));
					autoPayDetailsMap.setCustPhone(fields.fieldMask(dbObj.getString(FieldType.CUST_PHONE.getName())));
					autoPayDetailsMap.setCustEmail(fields.maskEmail(dbObj.getString(FieldType.CUST_EMAIL.getName())));
					autoPayDetailsMap.setPaymentType(
							PaymentType.getpaymentName(dbObj.getString(FieldType.PAYMENT_TYPE.getName())));
					autoPayDetailsMap
							.setFrequency(Frequency.getFrequencyName(dbObj.getString(FieldType.FREQUENCY.getName())));
					autoPayDetailsMap.setTenure(dbObj.getString(FieldType.TENURE.getName()));
					autoPayDetailsMap.setAmount(dbObj.getString(FieldType.AMOUNT.getName()));
					autoPayDetailsMap.setStartDate(dbObj.getString(FieldType.DATE_FROM.getName()));
					autoPayDetailsMap.setEndDate(dbObj.getString(FieldType.DATE_TO.getName()));
					autoPayDetailsMap.setMaxAmount(dbObj.getString(FieldType.MONTHLY_AMOUNT.getName()));
					autoPayDetailsMap.setTotalAmount(dbObj.getString(FieldType.TOTAL_AMOUNT.getName()));
					autoPayDetailsMap.setStatus(dbObj.getString(FieldType.STATUS.getName()));
					autoPayDetailsMap.setDueDate(dbObj.getString(FieldType.DUE_DATE.getName()));
					autoPayDetailsMap.setNotificationDate(dbObj.getString(FieldType.NOTIFICATION_DATE.getName()));
					autoPayDetailsMap.setRegDate(dbObj.getString(FieldType.REGISTRATION_DATE.getName()));

					if (dbObj.containsKey(FieldType.DEBIT_DATE.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.DEBIT_DATE.getName()))) {
						autoPayDetailsMap.setDebitDate(dbObj.getString(FieldType.DEBIT_DATE.getName()));
					} else {
						autoPayDetailsMap.setDebitDate(Constants.NA.getValue());
					}
					autoPayList.add(autoPayDetailsMap);
				}
			} finally {
				cursor.close();
			}
		} catch (Exception exception) {
			String message = "Error fetching fetchAutopayTransactionReport from database ";
			logger.error(message, exception);
		}
		return autoPayList;
	}

	@SuppressWarnings({ "unused", "static-access" })
	public List<UpiAutoPay> fetchAutoPayDebitTransactionDetails(String orderId, String pgRefNum) {

		List<UpiAutoPay> autoPayDebitList = new ArrayList<UpiAutoPay>();
		try {

			logger.info("inside fetchAutoPayDebitTransactionDetails to get all debit transaction list");
			String tenure = null;
			String payId = null;
			String debitAmount = null;
			String regPgRefNum = null;
			String regDate = null;
			// String custName = null;
			String custEmail = null;
			String custMobile = null;
			String totalAmount = null;
			String paymentType = null;
			String payerVPA = null;
			String origTxnId = null;

			Map<String, String> debitTxnMap = new HashMap<String, String>();
			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));

			if (StringUtils.isNotBlank(pgRefNum)) {
				queryList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}

			queryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REGISTRATION.getName()));

			BasicDBObject query = new BasicDBObject("$and", queryList);
			logger.info("Inside TxnReports , eNach Registration, query = " + query);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", query);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbObj = cursor.next();
				if (StringUtils.isNotBlank(dbObj.getString("IS_ENCRYPTED"))
						&& dbObj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
					dbObj = dataEncDecTool.decryptDocument(dbObj);
				}
				tenure = dbObj.getString(FieldType.TENURE.getName());
				payId = dbObj.getString(FieldType.PAY_ID.getName());
				debitAmount = dbObj.getString(FieldType.MONTHLY_AMOUNT.getName());
				regPgRefNum = dbObj.getString(FieldType.PG_REF_NUM.getName());
				regDate = dbObj.getString(FieldType.CREATE_DATE.getName());
				payerVPA = dbObj.getString(FieldType.PAYER_ADDRESS.getName());
				custEmail = dbObj.getString(FieldType.CUST_EMAIL.getName());
				custMobile = dbObj.getString(FieldType.CUST_PHONE.getName());
				totalAmount = dbObj.getString(FieldType.TOTAL_AMOUNT.getName());
				paymentType = dbObj.getString(FieldType.PAYMENT_TYPE.getName());
				origTxnId = dbObj.getString(FieldType.ORIG_TXN_ID.getName());

			}

			List<BasicDBObject> saleQueryList = new ArrayList<BasicDBObject>();
			saleQueryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			saleQueryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			saleQueryList.add(new BasicDBObject(FieldType.ORIG_TXN_ID.getName(), origTxnId));
			saleQueryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));

			BasicDBObject finalQuery = new BasicDBObject("$and", saleQueryList);
			logger.info("Inside upi autoPay dao , debit transaction details, finalquery = " + finalQuery);
			match = new BasicDBObject("$match", finalQuery);
			sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));
			pipeline = Arrays.asList(match, sort);
			output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			cursor = output.iterator();
			while (cursor.hasNext()) {

				Document dbObj = cursor.next();
				if (!debitTxnMap.containsKey(dbObj.getString(FieldType.PG_REF_NUM.getName()))) {
					debitTxnMap.put(dbObj.getString(FieldType.PG_REF_NUM.getName()),
							dbObj.getString(FieldType.TXN_ID.getName()));
				}
			}

			List<BasicDBObject> debitQueryList = new ArrayList<BasicDBObject>();

			debitQueryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			debitQueryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));

			List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject txnTypeQuery = new BasicDBObject();

			for (Map.Entry<String, String> txnId : debitTxnMap.entrySet()) {
				txnTypeConditionLst.add(new BasicDBObject(FieldType.TXN_ID.getName(), txnId.getValue()));
			}

			txnTypeQuery.append("$or", txnTypeConditionLst);

			debitQueryList.add(txnTypeQuery);

			BasicDBObject debitFinalQuery = new BasicDBObject("$and", debitQueryList);
			logger.info("Inside upi autoPay dao , autoPay Debit Transaction , debitFinalQuery = " + debitFinalQuery);
			match = new BasicDBObject("$match", debitFinalQuery);
			sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));
			pipeline = Arrays.asList(match, sort);
			output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			cursor = output.iterator();

			while (cursor.hasNext()) {

				Document dbObj = cursor.next();
				UpiAutoPay upiAutoPayMap = new UpiAutoPay();

				if (dbObj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
					upiAutoPayMap.setSubMerchantName(
							userDao.getBusinessNameByPayId(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName())));
					upiAutoPayMap.setSubMerchantPayId(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName()));
				} else {
					upiAutoPayMap.setSubMerchantName(Constants.NA.getValue());
					upiAutoPayMap.setSubMerchantPayId(Constants.NA.getValue());
				}

				if (dbObj.containsKey(FieldType.UMN.getName())
						&& StringUtils.isNotBlank(dbObj.getString(FieldType.UMN.getName()))) {
					upiAutoPayMap.setUmnNumber(dbObj.getString(FieldType.UMN.getName()));
				} else {
					upiAutoPayMap.setUmnNumber(Constants.NA.getValue());
				}

				upiAutoPayMap
						.setMerchantName(userDao.getBusinessNameByPayId(dbObj.getString(FieldType.PAY_ID.getName())));
				upiAutoPayMap.setPayId(dbObj.getString(FieldType.PAY_ID.getName()));
				upiAutoPayMap.setOrderId(dbObj.getString(FieldType.ORDER_ID.getName()));
				upiAutoPayMap.setRegPgRefNum(regPgRefNum);
				upiAutoPayMap.setRegDate(regDate);
				upiAutoPayMap.setTotalAmount(totalAmount);

				upiAutoPayMap.setPgRefNum(dbObj.getString(FieldType.PG_REF_NUM.getName()));
				upiAutoPayMap.setPayerAddress(fields.maskEmail(payerVPA));
				upiAutoPayMap.setCustEmail(fields.maskEmail(custEmail));
				upiAutoPayMap.setCustPhone(fields.fieldMask(custMobile));
				upiAutoPayMap.setAmount(dbObj.getString(FieldType.AMOUNT.getName()));
				upiAutoPayMap.setMaxAmount(dbObj.getString(FieldType.AMOUNT.getName()));
				upiAutoPayMap.setCreateDate(dbObj.getString(FieldType.CREATE_DATE.getName()));
				upiAutoPayMap.setStatus(dbObj.getString(FieldType.STATUS.getName()));
				upiAutoPayMap.setPaymentType(PaymentType.getpaymentName(paymentType));

				if (dbObj.containsKey(FieldType.DEBIT_DATE.getName())
						&& StringUtils.isNotBlank(dbObj.getString(FieldType.DEBIT_DATE.getName()))) {
					upiAutoPayMap.setDebitDate(dbObj.getString(FieldType.DEBIT_DATE.getName()));
				} else {
					upiAutoPayMap.setDebitDate(Constants.NA.getValue());
				}
				upiAutoPayMap.setDueDate(dbObj.getString(FieldType.DUE_DATE.getName()));
				upiAutoPayMap.setNotificationDate(dbObj.getString(FieldType.NOTIFICATION_DATE.getName()));

				autoPayDebitList.add(upiAutoPayMap);
			}

		} catch (Exception ex) {
			logger.error("exception caught while get debit transaction details for notify and pay", ex);
		}
		return autoPayDebitList;
	}

	@SuppressWarnings("static-access")
	public Fields fetchUpiAutoPayRegistrationDetails(String orderId, String merchantPayId, String pgRefNum) {

		Fields fields = new Fields();
		logger.info("inside TxnReport fetchUpiAutoPayRegistrationDetails function");
		try {

			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			queryList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));

			BasicDBObject query = new BasicDBObject("$and", queryList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));
			MongoCursor<Document> cursor = coll.find(query).sort(new BasicDBObject(FieldType.CREATE_DATE.getName(), -1))
					.iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();

				fields.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
				fields.put(FieldType.TXNTYPE.getName(), doc.getString(FieldType.TXNTYPE.getName()));
				fields.put(FieldType.DATE_FROM.getName(), doc.getString(FieldType.DATE_FROM.getName()));
				fields.put(FieldType.DATE_TO.getName(), doc.getString(FieldType.DATE_TO.getName()));
				/*
				 * fields.put(FieldType.CURRENCY.getName(),
				 * doc.getString(FieldType.CURRENCY.getName()));
				 */
				fields.put(FieldType.AMOUNT.getName(), doc.getString(FieldType.AMOUNT.getName()));
				fields.put(FieldType.MONTHLY_AMOUNT.getName(), doc.getString(FieldType.MONTHLY_AMOUNT.getName()));
				fields.put(FieldType.TOTAL_AMOUNT.getName(), doc.getString(FieldType.TOTAL_AMOUNT.getName()));
				fields.put(FieldType.PAYMENT_TYPE.getName(), doc.getString(FieldType.PAYMENT_TYPE.getName()));
				fields.put(FieldType.CUST_PHONE.getName(), doc.getString(FieldType.CUST_PHONE.getName()));
				fields.put(FieldType.CUST_EMAIL.getName(), doc.getString(FieldType.CUST_EMAIL.getName()));
				fields.put(FieldType.AMOUNT_LIMIT.getName(), doc.getString(FieldType.AMOUNT_LIMIT.getName()));
				fields.put(FieldType.FREQUENCY.getName(), doc.getString(FieldType.FREQUENCY.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), doc.getString(FieldType.ORIG_TXN_ID.getName()));
				fields.put(FieldType.TENURE.getName(), doc.getString(FieldType.TENURE.getName()));
				/*
				 * fields.put(FieldType.BANK_NAME.getName(),
				 * doc.getString(FieldType.BANK_NAME.getName()));
				 */
				fields.put(FieldType.UMN.getName(), doc.getString(FieldType.UMN.getName()));
				/*
				 * fields.put("MANDATE_REGISTRATION_ID",
				 * doc.getString("MANDATE_REGISTRATION_ID"));
				 */
				fields.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName()));
				fields.put(FieldType.REGISTRATION_DATE.getName(), doc.getString(FieldType.REGISTRATION_DATE.getName()));
				fields.put(FieldType.DUE_DATE.getName(), doc.getString(FieldType.DUE_DATE.getName()));
				fields.put(FieldType.PAYER_ADDRESS.toString(), doc.getString(FieldType.PAYER_ADDRESS.toString()));

				if (doc.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), doc.getString(FieldType.SUB_MERCHANT_ID.getName()));
				}

				if (doc.containsKey(FieldType.RESELLER_ID.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_ID.getName()))) {
					fields.put(FieldType.RESELLER_ID.getName(), doc.getString(FieldType.RESELLER_ID.getName()));
				}	
			}
		} catch (Exception ex) {
			logger.error("caught exception getEnachRegistrationDetails : ", ex);
		}

		return fields;
	}

	@SuppressWarnings("static-access")
	public Map<String, String> fetchTransactionByOrderId(String orderId, String txnType) {
		logger.info("inside fetchTransactionByOrderId for registration reponse data");
		HashMap<String, String> debitTransactionMap = new HashMap<String, String>();
		try {
			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));

			BasicDBObject query = new BasicDBObject("$and", queryList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();

				debitTransactionMap.put(FieldType.AMOUNT.getName(), doc.getString(FieldType.AMOUNT.getName()));
				debitTransactionMap.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
				debitTransactionMap.put(FieldType.REMARKS.getName(), doc.getString(FieldType.REMARKS.getName()));

				debitTransactionMap.put(FieldType.PURPOSE.getName(), doc.getString(FieldType.PURPOSE.getName()));
				if (StringUtils.isNotBlank(doc.getString(FieldType.UMN.getName()))) {
					debitTransactionMap.put(FieldType.UMN.getName(), doc.getString(FieldType.UMN.getName()));
				} else {
					debitTransactionMap.put(FieldType.UMN.getName(), Constants.NA.getValue());
				}
				debitTransactionMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						doc.getString(FieldType.RESPONSE_MESSAGE.getName()));
				debitTransactionMap.put(FieldType.PAYMENT_TYPE.getName(),
						PaymentType.getpaymentName(doc.getString(FieldType.PAYMENT_TYPE.getName())));
				debitTransactionMap.put(FieldType.STATUS.getName(), doc.getString(FieldType.STATUS.getName()));
				debitTransactionMap.put(FieldType.PAYER_ADDRESS.getName(),
						fields.maskEmail(doc.getString(FieldType.PAYER_ADDRESS.getName())));
				debitTransactionMap.put(FieldType.DATE_FROM.getName(), doc.getString(FieldType.DATE_FROM.getName()));
				debitTransactionMap.put(FieldType.DATE_TO.getName(), doc.getString(FieldType.DATE_TO.getName()));
				debitTransactionMap.put(FieldType.MONTHLY_AMOUNT.getName(),
						doc.getString(FieldType.MONTHLY_AMOUNT.getName()));
				debitTransactionMap.put(FieldType.TOTAL_AMOUNT.getName(),
						doc.getString(FieldType.TOTAL_AMOUNT.getName()));
				debitTransactionMap.put(FieldType.NOTE.getName(), doc.getString(FieldType.NOTE.getName()));
				debitTransactionMap.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
				debitTransactionMap.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
				debitTransactionMap.put(FieldType.ORIG_TXN_ID.getName(),
						doc.getString(FieldType.ORIG_TXN_ID.getName()));
				debitTransactionMap.put(FieldType.TENURE.getName(), doc.getString(FieldType.TENURE.getName()));
				debitTransactionMap.put(FieldType.FREQUENCY.getName(),
						AutoPayFrequency.getAutoPayFrequencyName(doc.getString(FieldType.FREQUENCY.getName())));
				debitTransactionMap.put(FieldType.CUST_EMAIL.getName(),
						fields.maskEmail(doc.getString(FieldType.CUST_EMAIL.getName())));
				debitTransactionMap.put(FieldType.CUST_PHONE.getName(),
						fields.fieldMask(doc.getString(FieldType.CUST_PHONE.getName())));
			}
		} catch (Exception ex) {
			logger.error("Exception caught while get transaction by orderId from DB ", ex);
		}
		return debitTransactionMap;
	}

	@SuppressWarnings({ "static-access", "unchecked" })
	public void insertAutoPayCancelledRegistrationDetail(Fields autoPayRegistrationDetails) throws SystemException {

		logger.info("inside insertAutoPayRegistrationDetail to insert registration details For eMandate sign ");
		try {
			BasicDBObject newFieldsObj = new BasicDBObject();

			newFieldsObj.put("_id", autoPayRegistrationDetails.get(FieldType.PG_REF_NUM.getName()));
			newFieldsObj.put(FieldType.TXNTYPE.getName(), autoPayRegistrationDetails.get(FieldType.TXNTYPE.getName()));
			if (autoPayRegistrationDetails.contains(FieldType.STATUS.getName())
					&& StringUtils.isNotBlank(autoPayRegistrationDetails.get(FieldType.STATUS.getName()))) {
				newFieldsObj.put(FieldType.STATUS.getName(),
						autoPayRegistrationDetails.get(FieldType.STATUS.getName()));
			} else {
				newFieldsObj.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
			}
			newFieldsObj.put(FieldType.PAY_ID.getName(), autoPayRegistrationDetails.get(FieldType.PAY_ID.getName()));
			newFieldsObj.put(FieldType.DATE_FROM.getName(),
					autoPayRegistrationDetails.get(FieldType.DATE_FROM.getName()));
			newFieldsObj.put(FieldType.DATE_TO.getName(), autoPayRegistrationDetails.get(FieldType.DATE_TO.getName()));
			newFieldsObj.put(Constants.AMOUNT.getValue(),
					String.valueOf(new BigDecimal(autoPayRegistrationDetails.get(FieldType.AMOUNT.getName()))
							.setScale(2, BigDecimal.ROUND_HALF_UP)));
			newFieldsObj.put(FieldType.MONTHLY_AMOUNT.getName(), autoPayRegistrationDetails.get("TRANSACTION_AMOUNT"));
			newFieldsObj.put(FieldType.TOTAL_AMOUNT.getName(),
					autoPayRegistrationDetails.get(FieldType.TOTAL_AMOUNT.getName()));

			if (StringUtils.isNotBlank(autoPayRegistrationDetails.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				newFieldsObj.put(FieldType.SUB_MERCHANT_ID.getName(),
						autoPayRegistrationDetails.get(FieldType.SUB_MERCHANT_ID.getName()));
			}

			if (StringUtils.isNotBlank(autoPayRegistrationDetails.get(FieldType.RESELLER_ID.getName()))) {
				newFieldsObj.put(FieldType.RESELLER_ID.getName(),
						autoPayRegistrationDetails.get(FieldType.RESELLER_ID.getName()));
			}
			newFieldsObj.put(FieldType.PAYMENT_TYPE.getName(),
					autoPayRegistrationDetails.get(FieldType.PAYMENT_TYPE.getName()));
			newFieldsObj.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));
			newFieldsObj.put(FieldType.CUST_PHONE.getName(),
					autoPayRegistrationDetails.get(FieldType.CUST_PHONE.getName()));
			newFieldsObj.put(FieldType.CUST_EMAIL.getName(),
					autoPayRegistrationDetails.get(FieldType.CUST_EMAIL.getName()));
			newFieldsObj.put(FieldType.FREQUENCY.getName(),
					autoPayRegistrationDetails.get(FieldType.FREQUENCY.getName()));
			newFieldsObj.put(FieldType.ORDER_ID.getName(),
					autoPayRegistrationDetails.get(FieldType.ORDER_ID.getName()));
			newFieldsObj.put(FieldType.TXN_ID.getName(), autoPayRegistrationDetails.get(FieldType.TXN_ID.getName()));
			newFieldsObj.put(FieldType.PG_REF_NUM.getName(),
					autoPayRegistrationDetails.get(FieldType.PG_REF_NUM.getName()));
			newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(),
					autoPayRegistrationDetails.get(FieldType.ORIG_TXN_ID.getName()));
			newFieldsObj.put(FieldType.TENURE.getName(), autoPayRegistrationDetails.get(FieldType.TENURE.getName()));
			newFieldsObj.put("MERCHANT_LOGO", autoPayRegistrationDetails.get("MERCHANT_LOGO"));
			newFieldsObj.put(FieldType.EMANDATE_URL.getName(),
					autoPayRegistrationDetails.get(FieldType.EMANDATE_URL.getName()));
			
			if (autoPayRegistrationDetails.contains(FieldType.PAYER_ADDRESS.getName())
					&& StringUtils.isNotBlank(autoPayRegistrationDetails.get(FieldType.PAYER_ADDRESS.getName()))) {
				newFieldsObj.put(FieldType.PAYER_ADDRESS.getName(),
						autoPayRegistrationDetails.get(FieldType.PAYER_ADDRESS.getName()));
			} else {
				newFieldsObj.put(FieldType.PAYER_ADDRESS.getName(), Constants.NA.getValue());
			}

			if (autoPayRegistrationDetails.contains(FieldType.RESPONSE_MESSAGE.getName())
					&& StringUtils.isNotBlank(autoPayRegistrationDetails.get(FieldType.RESPONSE_MESSAGE.getName()))) {
				newFieldsObj.put(FieldType.RESPONSE_MESSAGE.getName(),
						autoPayRegistrationDetails.get(FieldType.RESPONSE_MESSAGE.getName()));
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));

			Document doc = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						fieldsDao.sendCallbackAfterUpiAutoPayAmountDebit(new Fields(newFieldsObj.toMap()));
					} catch (Exception e) {
						logger.error("Exception ", e);
					}
				}
			};

			propertiesManager.executorImpl(runnable);
			// collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error insert cancel autoPayRegistrationDetail to database";
			logger.error(message, exception);
		}
	}

	@SuppressWarnings("static-access")
	public HashMap<String, String> getAutoPayMandateDetailsByOrderId(String orderId) {

		logger.info("inside getAutoPayMandateDetailsByOrderId get data from DB by orderId " + orderId);
		HashMap<String, String> mandateDetails = new HashMap<String, String>();
		try {

			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REGISTRATION.getName()));

			BasicDBObject query = new BasicDBObject("$and", queryList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			try {
				while (cursor.hasNext()) {

					Document doc = cursor.next();
					if (StringUtils.isNotBlank(doc.getString(FieldType.PAYMENT_TYPE.getName()))) {
						mandateDetails.put(FieldType.PAYMENT_TYPE.getName(),
								doc.getString(FieldType.PAYMENT_TYPE.getName()));
					} else {
						mandateDetails.put(FieldType.PAYMENT_TYPE.getName(), "NA");
					}

					if (doc.containsKey(FieldType.UMN.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.UMN.getName()))) {
						mandateDetails.put(FieldType.UMN.getName(), doc.getString(FieldType.UMN.getName()));
					} else {
						mandateDetails.put(FieldType.UMN.getName(), Constants.NA.getValue());
					}

					if (doc.containsKey(FieldType.RESPONSE_MESSAGE.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESPONSE_MESSAGE.getName()))) {
						mandateDetails.put(FieldType.RESPONSE_MESSAGE.getName(),
								doc.getString(FieldType.RESPONSE_MESSAGE.getName()));
					} else {
						mandateDetails.put(FieldType.RESPONSE_MESSAGE.getName(), Constants.NA.getValue());
					}

					mandateDetails.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
					mandateDetails.put(FieldType.CUST_PHONE.getName(), doc.getString(FieldType.CUST_PHONE.getName()));
					mandateDetails.put(FieldType.CUST_EMAIL.getName(), doc.getString(FieldType.CUST_EMAIL.getName()));
					mandateDetails.put(FieldType.AMOUNT.getName(), doc.getString(FieldType.AMOUNT.getName()));
					mandateDetails.put(FieldType.MONTHLY_AMOUNT.getName(),
							doc.getString(FieldType.MONTHLY_AMOUNT.getName()));
					mandateDetails.put(FieldType.TOTAL_AMOUNT.getName(),
							doc.getString(FieldType.TOTAL_AMOUNT.getName()));
					mandateDetails.put(FieldType.FREQUENCY.getName(), doc.getString(FieldType.FREQUENCY.getName()));
					mandateDetails.put(FieldType.TENURE.getName(), doc.getString(FieldType.TENURE.getName()));
					mandateDetails.put(FieldType.DATE_FROM.getName(), doc.getString(FieldType.DATE_FROM.getName()));
					mandateDetails.put(FieldType.DATE_TO.getName(), doc.getString(FieldType.DATE_TO.getName()));

					if (StringUtils.isNotBlank(doc.getString("MERCHANT_LOGO"))) {
						mandateDetails.put("logo", doc.getString("MERCHANT_LOGO"));
					} else {
						mandateDetails.put("logo", Constants.NA.getValue());
					}

					if (doc.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						mandateDetails.put(FieldType.PAY_ID.getName(),
								doc.getString(FieldType.SUB_MERCHANT_ID.getName()));
						mandateDetails.put(FieldType.MERCHANT_NAME.getName(),
								userDao.getBusinessNameByPayId(doc.getString(FieldType.SUB_MERCHANT_ID.getName())));
						mandateDetails.put(FieldType.MERCHANT_EMAIL.getName(),
								userDao.getEmailIdByPayId(doc.getString(FieldType.SUB_MERCHANT_ID.getName())));
					} else {
						mandateDetails.put(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName()));
						mandateDetails.put(FieldType.MERCHANT_NAME.getName(),
								userDao.getBusinessNameByPayId(doc.getString(FieldType.PAY_ID.getName())));
						mandateDetails.put(FieldType.MERCHANT_EMAIL.getName(),
								userDao.getEmailIdByPayId(doc.getString(FieldType.PAY_ID.getName())));
					}
					mandateDetails.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
					mandateDetails.put(FieldType.RESPONSE_CODE.getName(),
							doc.getString(FieldType.RESPONSE_CODE.getName()));
					mandateDetails.put(FieldType.CREATE_DATE.getName(), doc.getString(FieldType.CREATE_DATE.getName()));
					mandateDetails.put(FieldType.STATUS.getName(), doc.getString(FieldType.STATUS.getName()));
				}
			} finally {
				cursor.close();
			}

		} catch (Exception exception) {
			String message = "Error fetching registrationDetail from orderId from database ";
			logger.error(message, exception);
		}
		return mandateDetails;
	}

	@SuppressWarnings("static-access")
	public List<String> fetchAllCapturedRegistrationOrderId(String orderId, String umnNumber, String merchantPayId,
			String subMerchantPayId, String resellerId, String status, String txnType, String dateFrom, String dateTo) {

		logger.info("Inside getAllCapturedRegistrationOrderId get captured registration orderId ");
		boolean isParameterised = false;

		List<String> orderIdList = new ArrayList<String>();
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			String currentDate = null;
			if (!dateFrom.isEmpty()) {

				if (!dateTo.isEmpty()) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
				isParameterised = true;
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}
			if (StringUtils.isNotBlank(resellerId) && !resellerId.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), resellerId));
			}
			if (!orderId.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			}

			if (!umnNumber.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.UMN.getName(), umnNumber));
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (StringUtils.isNotBlank(umnNumber) || StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateQuery.isEmpty()) {
					allConditionQueryList.add(dateQuery);
				}
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside UpiAutoPayDao , fetchAllCapturedRegistrationOrderId , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				orderIdList.add(dbobj.getString(FieldType.ORDER_ID.getName()));
			}
		} catch (Exception exception) {
			String message = "Error fetching fetchAllCapturedRegistrationOrderId from database ";
			logger.error(message, exception);
		}

		return orderIdList;
	}

	@SuppressWarnings("static-access")
	public Map<String, String> getUpiAutoPayMandateDetailsByOrderId(Fields fields) throws SystemException {

		Map<String, String> mandateDetail = new HashMap<String, String>();
		try {

			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

			queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "REGISTRATION"));
			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));

			BasicDBObject finalQuery = new BasicDBObject("$and", queryList);
			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(finalQuery).iterator();
			try {

				if (!cursor.hasNext()) {

					mandateDetail.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
					mandateDetail.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
					mandateDetail.put(FieldType.RESPONSE_CODE.getName(), ErrorType.MANDATE_NOT_EXIST.getResponseCode());
					mandateDetail.put(FieldType.RESPONSE_MESSAGE.getName(),
							ErrorType.MANDATE_NOT_EXIST.getResponseMessage());

				} else {
					while (cursor.hasNext()) {

						Document doc = cursor.next();

						mandateDetail.put(FieldType.AMOUNT.getName(), doc.getString(FieldType.AMOUNT.getName()));
						if (StringUtils.isNotBlank(doc.getString(FieldType.UMN.getName()))) {
							mandateDetail.put(FieldType.UMN.getName(), doc.getString(FieldType.UMN.getName()));
						} else {
							mandateDetail.put(FieldType.UMN.getName(), Constants.NA.getValue());
						}
						mandateDetail.put(FieldType.RESPONSE_MESSAGE.getName(),
								doc.getString(FieldType.RESPONSE_MESSAGE.getName()));
						mandateDetail.put(FieldType.PAYMENT_TYPE.getName(),
								PaymentType.getpaymentName(doc.getString(FieldType.PAYMENT_TYPE.getName())));
						mandateDetail.put(FieldType.STATUS.getName(), doc.getString(FieldType.STATUS.getName()));
						mandateDetail.put(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName()));
						mandateDetail.put(FieldType.DATE_FROM.getName(), doc.getString(FieldType.DATE_FROM.getName()));
						mandateDetail.put(FieldType.DATE_TO.getName(), doc.getString(FieldType.DATE_TO.getName()));
						mandateDetail.put(FieldType.MONTHLY_AMOUNT.getName(),
								doc.getString(FieldType.MONTHLY_AMOUNT.getName()));
						mandateDetail.put(FieldType.TOTAL_AMOUNT.getName(),
								doc.getString(FieldType.TOTAL_AMOUNT.getName()));
						mandateDetail.put(FieldType.PG_REF_NUM.getName(),
								doc.getString(FieldType.PG_REF_NUM.getName()));
						mandateDetail.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
						mandateDetail.put(FieldType.TENURE.getName(), doc.getString(FieldType.TENURE.getName()));
						mandateDetail.put(FieldType.FREQUENCY.getName(),
								AutoPayFrequency.getAutoPayFrequencyName(doc.getString(FieldType.FREQUENCY.getName())));
						mandateDetail.put(FieldType.CUST_EMAIL.getName(),
								fields.maskEmail(doc.getString(FieldType.CUST_EMAIL.getName())));
						mandateDetail.put("CUST_MOBILE",
								fields.fieldMask(doc.getString(FieldType.CUST_PHONE.getName())));

					}
				}
			} finally {
				cursor.close();
			}
			return mandateDetail;
		} catch (Exception exception) {
			String message = "Error fetching registrationDetail from txnId from database";
			logger.error(message, exception);
			return mandateDetail;
		}
	}

	public List<String> getDueDateListForWhenPresented(String date, String tenure, String payId,
			String subMerchantPayId) {

		logger.info("Inside getDueDateListForWhenPresented, calculates As and when presented due date.");
		List<String> dateList = new ArrayList<String>();

		int finalTenure = Integer.parseInt(tenure);
		LocalDate startDate = LocalDate.parse(date);
		String debitDuration;
		
		if (StringUtils.isNotBlank(subMerchantPayId))
			debitDuration = userDao.getUpiAutoPayDebitDurationByPayId(subMerchantPayId);
		else
			debitDuration = userDao.getUpiAutoPayDebitDurationByPayId(payId);

		if (StringUtils.isNotBlank(debitDuration))
			startDate = startDate.plusDays(Long.parseLong(debitDuration));
		else
			startDate = startDate.plusDays(30);

		startDate = startDate.plusYears(100);

		for (int i = 0; i < finalTenure; i++) {
			dateList.add(startDate.toString());
		}
		return dateList;
	}

	public void insertUpiAutopayRegistrationLinkDetail(Fields reqFields) {
		logger.info("inside insertUpiAutopayRegistrationLinkDetail to insert registration Link details For eMandate sign ");
		String pgRefNum = TransactionManager.getNewTransactionId();
		try {
			BasicDBObject newFieldsObj = new BasicDBObject();

			newFieldsObj.put(FieldType.RETURN_URL.getName(), reqFields.get("RETURN_URL"));
			newFieldsObj.put(FieldType.TENURE.getName(), reqFields.get(FieldType.TENURE.getName()));
			newFieldsObj.put(FieldType.PAY_ID.getName(), reqFields.get(FieldType.PAY_ID.getName()));

			newFieldsObj.put(FieldType.MONTHLY_AMOUNT.getName(),
					String.valueOf(new BigDecimal(reqFields.get(FieldType.MONTHLY_AMOUNT.getName())).setScale(2,
							BigDecimal.ROUND_HALF_UP)));
			newFieldsObj.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));
			newFieldsObj.put(FieldType.CUST_PHONE.getName(), reqFields.get("CUST_MOBILE"));
			newFieldsObj.put(FieldType.CUST_EMAIL.getName(), reqFields.get(FieldType.CUST_EMAIL.getName()));
			newFieldsObj.put(FieldType.FREQUENCY.getName(), reqFields.get(FieldType.FREQUENCY.getName()));
			newFieldsObj.put(FieldType.ORDER_ID.getName(), reqFields.get(FieldType.ORDER_ID.getName()));
			newFieldsObj.put(FieldType.HASH.getName(), reqFields.get(FieldType.HASH.getName()));
			newFieldsObj.put(FieldType.AMOUNT.getName(), String.valueOf(
					new BigDecimal(reqFields.get(FieldType.AMOUNT.getName())).setScale(2, BigDecimal.ROUND_HALF_UP)));
			newFieldsObj.put(FieldType.EMANDATE_URL.getName(), reqFields.get(FieldType.EMANDATE_URL.getName()));

			newFieldsObj.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
			newFieldsObj.put(FieldType.TXNTYPE.getName(), TransactionType.REGISTRATION.getName());
			newFieldsObj.put("_id", pgRefNum);
			newFieldsObj.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
			newFieldsObj.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PENDING.getCode());
			newFieldsObj.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PENDING.getInternalMessage());
			newFieldsObj.put(FieldType.PURPOSE.getName(), reqFields.get(FieldType.PURPOSE.getName()));
			
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));

			Document doc = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			// collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error while inserting mandate Link Detail to database";
			logger.error(message, exception);
		}
	}

	public boolean checkDuplicateOrderIdForRegistration(String orderId, String payId, String subMerchantPayId) {
		try {
			String dbOrderId;

			List<BasicDBObject> queryForCaptured = new ArrayList<BasicDBObject>();
			queryForCaptured.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			queryForCaptured.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				queryForCaptured.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			queryForCaptured.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			BasicDBObject capturedQuery = new BasicDBObject("$and", queryForCaptured);

			List<BasicDBObject> queryForFailed = new ArrayList<BasicDBObject>();
			queryForFailed.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			queryForFailed.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				queryForFailed.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			queryForFailed.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
			BasicDBObject failedQuery = new BasicDBObject("$and", queryForFailed);

			List<BasicDBObject> queryForProcessing = new ArrayList<BasicDBObject>();
			queryForProcessing.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			queryForProcessing.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				queryForProcessing.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			queryForProcessing.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PROCESSING.getName()));
			BasicDBObject processingQuery = new BasicDBObject("$and", queryForProcessing);

			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(capturedQuery);
			queryList.add(failedQuery);
			queryList.add(processingQuery);

			BasicDBObject query = new BasicDBObject("$or", queryList);

			logger.info(
					"Inside checkDuplicateOrderIdForRegistration, check duplicate orderId final query For Registration = "
							+ query);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));
			MongoCursor<Document> cursor = coll.find(query).sort(new BasicDBObject(FieldType.CREATE_DATE.getName(), -1))
					.iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();

				dbOrderId = doc.getString(FieldType.ORDER_ID.toString());
				if (StringUtils.isNotBlank(dbOrderId) && dbOrderId.equalsIgnoreCase(orderId)) {
					return true;
				}
			}
		} catch (Exception ex) {
			logger.error("exception caught while check duplicate orderId for registration ", ex);
			return false;
		}
		return false;
	}

	public String getEMandateUrlByOrderId(String orderId, String payId) {

		String eMandateUrl = null;

		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

		queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "Registration"));

		BasicDBObject query = new BasicDBObject("$and", queryList);
		logger.info("Inside getEnachRegistrationDataToResendLink , query = " + query);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));
		BasicDBObject match = new BasicDBObject("$match", query);
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		List<BasicDBObject> pipeline = Arrays.asList(match, sort);

		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
		while (cursor.hasNext()) {
			Document dbObj = cursor.next();
			if (StringUtils.isNotBlank(dbObj.getString("IS_ENCRYPTED"))
					&& dbObj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
				dbObj = dataEncDecTool.decryptDocument(dbObj);
			}
			eMandateUrl = dbObj.getString(FieldType.EMANDATE_URL.getName());
			if (eMandateUrl != null)
				break;
		}

		return eMandateUrl;
	}

}
