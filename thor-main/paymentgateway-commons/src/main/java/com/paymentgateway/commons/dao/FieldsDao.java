package com.paymentgateway.commons.dao;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.SmsControllerServiceProvider;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.CoinSwitchCustomer;
import com.paymentgateway.commons.user.CustomerAddress;
import com.paymentgateway.commons.user.PayoutAcquirer;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.P2MPayoutUtil;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.RouterConfigurationService;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.SystemProperties;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;

@Service
public class FieldsDao {

	private static Logger logger = LoggerFactory.getLogger(FieldsDao.class.getName());

	// All static fields
	private static final Collection<String> allDBRequestFields = SystemProperties.getAllDBRequestFields();
	private static final Collection<String> aLLDB_Fields = SystemProperties.getDBFields();
	private static final Collection<String> allTxnStatus_Fields = SystemProperties.getTransactionStatusFields();
	private static final String prefix = "MONGO_DB_";
	private SystemProperties systemProperties = new SystemProperties();
	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private RouterConfigurationService routerConfigurationService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private ENachDao eNachDao;

	@Autowired
	private MerchantGridViewService merchantGridViewService;

	@Autowired
	private EncryptDecryptService encryptDecryptService;

	@Autowired
	private SmsControllerServiceProvider smsController;

	@Autowired
	private P2MPayoutUtil p2MPayoutUtil;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	@Autowired
	private RefundLimitService refundLimitService;

	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@SuppressWarnings("static-access")
	public void updateNewOrderDetails(Fields fields) throws SystemException {
		try {
			String amountString = fields.get(FieldType.AMOUNT.getName());
			String surchargeAmountString = fields.get(FieldType.SURCHARGE_AMOUNT.getName());
			String currencyString = fields.get(FieldType.CURRENCY_CODE.getName());

			String amount = "0";
			if (!StringUtils.isEmpty(amountString) && !StringUtils.isEmpty(currencyString)) {
				amount = Amount.toDecimal(amountString, currencyString);
			}
			fields.put(FieldType.AMOUNT.getName(), amount);
			String surchargeAmount = "0";
			if (!StringUtils.isEmpty(surchargeAmountString) && !StringUtils.isEmpty(currencyString)) {
				surchargeAmount = Amount.toDecimal(surchargeAmountString, currencyString);
			}
			fields.put(FieldType.SURCHARGE_AMOUNT.getName(), surchargeAmount);
			BasicDBObject oldFieldsObj = new BasicDBObject();
			oldFieldsObj.put(FieldType.TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			Document oldDoc = new Document(oldFieldsObj);
			BasicDBObject newFieldsObj = new BasicDBObject();
			for (int i = 0; i < fields.size(); i++) {
				Collection<String> aLLDB_Fields = SystemProperties.getDBFields();
				for (String columnName : aLLDB_Fields) {
					newFieldsObj.put(columnName, fields.get(columnName));
				}
			}
			BasicDBObject updateObj = new BasicDBObject();
			updateObj.put("$set", newFieldsObj);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			Document newDoc = new Document(updateObj);
			collection.updateOne(oldDoc, newDoc);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void updateStatus(Fields fields) throws SystemException {
		try {
			MongoDatabase dbIns = null;
			BasicDBObject oldFieldsObj = new BasicDBObject();
			oldFieldsObj.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
			BasicDBObject newFieldsObj = new BasicDBObject();
			for (int i = 0; i < fields.size(); i++) {
				Collection<String> aLLDB_Fields = systemProperties.getDBFields();
				for (String columnName : aLLDB_Fields) {
					newFieldsObj.put(columnName, fields.get(columnName));
				}
			}
			BasicDBObject updateObj = new BasicDBObject();
			updateObj.put("$set", newFieldsObj);
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			Document newDoc = new Document(updateObj);
			Document oldDoc = new Document(oldFieldsObj);
			collection.updateOne(oldDoc, newDoc);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void updateCurrentTransaction(Fields fields) throws SystemException {
		try {
			MongoDatabase dbIns = null;
			BasicDBObject oldFieldsObj = new BasicDBObject();
			oldFieldsObj.put(FieldType.TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			BasicDBObject newFieldsObj = new BasicDBObject();
			for (int i = 0; i < fields.size(); i++) {
				for (String columnName : allDBRequestFields) {
					newFieldsObj.put(columnName, fields.get(columnName));
				}
			}
			BasicDBObject updateObj = new BasicDBObject();
			updateObj.put("$set", newFieldsObj);
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			Document newDoc = new Document(updateObj);
			Document oldDoc = new Document(oldFieldsObj);
			collection.updateOne(oldDoc, newDoc);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void updateForAuthorization(Fields fields) throws SystemException {
		try {
			MongoDatabase dbIns = null;
			BasicDBObject oldFieldsObj = new BasicDBObject();
			oldFieldsObj.put(FieldType.TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			BasicDBObject newFieldsObj = new BasicDBObject();
			for (int i = 0; i < fields.size(); i++) {
				Collection<String> aLLDB_Fields = SystemProperties.getDBFields();
				for (String columnName : aLLDB_Fields) {
					newFieldsObj.put(columnName, fields.get(columnName));
				}
			}
			BasicDBObject updateObj = new BasicDBObject();
			updateObj.put("$set", newFieldsObj);
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			Document newDoc = new Document(updateObj);
			Document oldDoc = new Document(oldFieldsObj);
			collection.updateOne(oldDoc, newDoc);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	private Fields createAllSelect(String txnId, String payId) {

		Fields fields = new Fields();
		// PropertiesManager propManager = new PropertiesManager();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.TXN_ID.getName(), txnId));
		dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		MongoCursor<Document> cursor = coll.find(andQuery).iterator();
		if (cursor.hasNext()) {

			Document documentObj = cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}

					}
				}
			}
			fields.logAllFields("Previous fields");
		}
		cursor.close();
		return fields;

	}

	@SuppressWarnings("static-access")
	private Fields createAllForRefund(String txnId, String payId, String txnType, String status) {

		Fields fields = new Fields();
		// PropertiesManager propManager = new PropertiesManager();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), txnId));
		dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		dbObjList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));
		dbObjList.add(new BasicDBObject(FieldType.STATUS.getName(), status));
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		MongoCursor<Document> cursor = coll.find(andQuery).iterator();
		if (cursor.hasNext()) {
			Document documentObj = cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}

					}
				}
			}
			fields.logAllFields("Previous fields");
		}
		cursor.close();
		return fields;

	}

	@SuppressWarnings("static-access")
	private Fields createAllOfNewOrder(String orderId, String payId, String txnType, String status) {

		Fields fields = new Fields();
		// PropertiesManager propManager = new PropertiesManager();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		dbObjList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));
		dbObjList.add(new BasicDBObject(FieldType.STATUS.getName(), status));
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		MongoCursor<Document> cursor = coll.find(andQuery).iterator();
		if (cursor.hasNext()) {
			Document documentObj = cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}

					}
				}
			}
			fields.logAllFields("Previous fields");
		}
		cursor.close();
		return fields;

	}

	// YesUpi Enquiry
	@SuppressWarnings("static-access")
	public boolean createAllForYesUpiEnquiry(String pgRefNum) {

		Fields fields = new Fields();
		boolean flag = false;
		// PropertiesManager propManager = new PropertiesManager();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
		dbObjList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		dbObjList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CANCELLED.getName()));
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		MongoCursor<Document> cursor = coll.find(andQuery).iterator();
		if (cursor.hasNext()) {
			Document documentObj = cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}

					}
				}
			}
			fields.logAllFields("Previous fields");
		}
		cursor.close();
		if (fields.size() > 0)
			flag = true;
		return flag;

	}

	@SuppressWarnings("static-access")
	private Fields createAllForFedUpi(String refId, String txnType) {

		Fields fields = new Fields();
		// PropertiesManager propManager = new PropertiesManager();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.UDF5.getName(), refId));
		dbObjList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		MongoCursor<Document> cursor = coll.find(andQuery).iterator();
		if (cursor.hasNext()) {
			Document documentObj = cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}

					}
				}
			}
			fields.logAllFields("Previous fields");
		}
		cursor.close();
		return fields;

	}

	@SuppressWarnings("static-access")
	private Fields createAllForHdfcUpi(String refId, String txnType) {

		Fields fields = new Fields();
		// PropertiesManager propManager = new PropertiesManager();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), refId));
		dbObjList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		MongoCursor<Document> cursor = coll.find(andQuery).iterator();
		if (cursor.hasNext()) {
			Document documentObj = cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}

					}
				}
			}
			fields.logAllFields("Previous fields");
		}
		cursor.close();
		return fields;

	}

	@SuppressWarnings("static-access")
	public Fields createAllSelectForStatus(String orderId, String payId, String amount, String currencyCode) {

		// Check if a capture entry is already present for SALE transaction
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		logger.info("status enqiry for orderId : " + orderId + " and payId : " + payId + " and currencyCode : "
				+ currencyCode);

		// Check if Sale Captured entry is present for this Order Id
		List<BasicDBObject> saleConList = new ArrayList<BasicDBObject>();
		saleConList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		saleConList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
		saleConList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		saleConList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		saleConList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyCode));

		BasicDBObject saleCaptureQuery = new BasicDBObject("$and", saleConList);

		logger.info("status query for orderId " + orderId + " is : " + saleCaptureQuery);

		long count = coll.count(saleCaptureQuery);

		logger.info("total txn count for orderId " + orderId + " is  = " + count);

		Fields fields = new Fields();
		// PropertiesManager propManager = new PropertiesManager();

		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();

		BasicDBObject query = new BasicDBObject();
		query.append(FieldType.RESPONSE_CODE.getName(),
				new BasicDBObject("$ne", ErrorType.DUPLICATE_ORDER_ID.getCode()));

		dbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		dbObjList.add(new BasicDBObject(FieldType.AMOUNT.getName(), amount));
		dbObjList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyCode));

		// Since Capture is already present , send capture response to merchant
		if (count > 0) {
			dbObjList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
		}

		BasicDBObject txnTypeQuery = new BasicDBObject();

		txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName()));
		txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.NEWORDER.getName()));
		// txnTypeConditionLst.add(new
		// BasicDBObject(FieldType.TXNTYPE.getName(),
		// TransactionType.RECO.getName()));

		txnTypeQuery.append("$or", txnTypeConditionLst);

		dbObjList.add(txnTypeQuery);
		dbObjList.add(query);
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);

		FindIterable<Document> iterator = coll.find(andQuery).sort(new BasicDBObject("INSERTION_DATE", -1)).limit(1);

		MongoCursor<Document> saleCursor = iterator.iterator();

		if (saleCursor.hasNext()) {
			Document documentObj = saleCursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}

					}
				}
			}

			fields.logAllFields("Previous fields");
			saleCursor.close();
		} else {
			saleCursor.close();
			txnTypeConditionLst.clear();
			dbObjList.clear();
			query.clear();
			txnTypeQuery.clear();
			andQuery.clear();

			query.append(FieldType.RESPONSE_CODE.getName(),
					new BasicDBObject("$ne", ErrorType.DUPLICATE_ORDER_ID.getCode()));

			dbObjList.add(new BasicDBObject(FieldType.REFUND_ORDER_ID.getName(), orderId));
			dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			dbObjList.add(new BasicDBObject(FieldType.AMOUNT.getName(), amount));
			dbObjList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyCode));

			txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));

			txnTypeQuery.append("$or", txnTypeConditionLst);

			dbObjList.add(txnTypeQuery);
			dbObjList.add(query);
			andQuery = new BasicDBObject("$and", dbObjList);

			FindIterable<Document> refundIterator = coll.find(andQuery).sort(new BasicDBObject("INSERTION_DATE", -1))
					.limit(1);

			MongoCursor<Document> refundCursor = refundIterator.iterator();

			if (refundCursor.hasNext()) {
				Document documentObj = refundCursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				if (null != documentObj) {
					for (int j = 0; j < documentObj.size(); j++) {
						for (String columnName : aLLDB_Fields) {
							if (documentObj.get(columnName) != null) {
								fields.put(columnName, documentObj.get(columnName).toString());
							} else {

							}

						}
					}
				}
				refundCursor.close();
			}

		}

		if (StringUtils.isBlank(fields.get(FieldType.TXNTYPE.getName()))
				|| StringUtils.isBlank(fields.get(FieldType.STATUS.getName()))) {

			return fields;
		}

		fields.put(FieldType.IS_STATUS_FINAL.getName(), Constants.Y_FLAG.getValue());
		return fields;

	}

	@SuppressWarnings("static-access")
	public Fields getPreviousTxnForPostCaptured(String orderId, String payId, String amount, String currencyCode) {

		// fetch previous entry for post captured txn
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		Fields fields = new Fields();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> statusConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		dbObjList.add(new BasicDBObject(FieldType.AMOUNT.getName(), amount));
		dbObjList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyCode));

		BasicDBObject txnTypeQuery = new BasicDBObject();

		txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName()));
		txnTypeQuery.append("$or", txnTypeConditionLst);
		dbObjList.add(txnTypeQuery);

		BasicDBObject statusQuery = new BasicDBObject();
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DECLINED.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.REJECTED.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PENDING.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ERROR.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.BROWSER_CLOSED.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CANCELLED.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ENROLLED.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.INVALID.getName()));
		statusConditionLst
				.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED_AT_ACQUIRER.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ACQUIRER_TIMEOUT.getName()));
		statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName()));
		statusQuery.append("$or", statusConditionLst);

		dbObjList.add(statusQuery);
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		FindIterable<Document> iterator = coll.find(andQuery).sort(new BasicDBObject("INSERTION_DATE", -1)).limit(1);
		MongoCursor<Document> saleCursor = iterator.iterator();

		if (saleCursor.hasNext()) {
			Document documentObj = saleCursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}
					}
				}
			}
			fields.logAllFields("Previous fields");
			saleCursor.close();
		}
		if (StringUtils.isBlank(fields.get(FieldType.TXNTYPE.getName()))
				|| StringUtils.isBlank(fields.get(FieldType.STATUS.getName()))) {
			return fields;
		}

		fields.put(FieldType.IS_STATUS_FINAL.getName(), Constants.Y_FLAG.getValue());
		return fields;
	}

	@SuppressWarnings("static-access")
	public Fields getPreviousDocForTxn(String orderId, String payId, String amount, String currencyCode) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		Fields fields = new Fields();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> nonCapturedDbObjList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> nonCapturedTxnTypeConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> nonCapturedStatusConditionLst = new ArrayList<BasicDBObject>();

		dbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		dbObjList.add(new BasicDBObject(FieldType.AMOUNT.getName(), amount));
		dbObjList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
		dbObjList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyCode));

		BasicDBObject txnTypeQuery = new BasicDBObject();
		txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName()));
		txnTypeQuery.append("$or", txnTypeConditionLst);
		dbObjList.add(txnTypeQuery);

		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		long count = coll.count(andQuery);

		FindIterable<Document> iterator = null;
		if (count > 0) {
			iterator = coll.find(andQuery).sort(new BasicDBObject("INSERTION_DATE", -1)).limit(1);
		} else {
			BasicDBObject nonCapturedStatusQuery = new BasicDBObject();

			nonCapturedDbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			nonCapturedDbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			nonCapturedDbObjList.add(new BasicDBObject(FieldType.AMOUNT.getName(), amount));
			nonCapturedDbObjList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyCode));

			BasicDBObject nonCapturedTxnTypeQuery = new BasicDBObject();
			nonCapturedTxnTypeConditionLst
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			nonCapturedTxnTypeConditionLst
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName()));
			nonCapturedTxnTypeQuery.append("$or", nonCapturedTxnTypeConditionLst);
			nonCapturedDbObjList.add(nonCapturedTxnTypeQuery);

			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DECLINED.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.REJECTED.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PENDING.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ERROR.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.BROWSER_CLOSED.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CANCELLED.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ENROLLED.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.INVALID.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED_AT_ACQUIRER.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ACQUIRER_TIMEOUT.getName()));
			nonCapturedStatusConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName()));

			nonCapturedStatusQuery.append("$or", nonCapturedStatusConditionLst);
			nonCapturedDbObjList.add(nonCapturedStatusQuery);

			BasicDBObject nonCapturedFinalQuery = new BasicDBObject("$and", nonCapturedDbObjList);
			iterator = coll.find(nonCapturedFinalQuery).sort(new BasicDBObject("INSERTION_DATE", -1)).limit(1);
		}
		MongoCursor<Document> saleCursor = iterator.iterator();

		if (saleCursor.hasNext()) {
			Document documentObj = saleCursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}
					}
				}
			}
			fields.logAllFields("Previous fields");
			saleCursor.close();
		}
		if (StringUtils.isBlank(fields.get(FieldType.TXNTYPE.getName()))
				|| StringUtils.isBlank(fields.get(FieldType.STATUS.getName()))) {
			return fields;
		}

		fields.put(FieldType.IS_STATUS_FINAL.getName(), Constants.Y_FLAG.getValue());
		return fields;
	}

	@SuppressWarnings("static-access")
	public Fields createAllSelectForVerify(String orderId, String payId, String amount, String pgRefNum,
			String status) {

		Fields fields = new Fields();
		// PropertiesManager propManager = new PropertiesManager();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		// List<BasicDBObject> txnTypeConditionLst = new
		// ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		dbObjList.add(new BasicDBObject(FieldType.AMOUNT.getName(), amount));
		dbObjList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
		dbObjList.add(new BasicDBObject(FieldType.STATUS.getName(), status));
		dbObjList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		/*
		 * BasicDBObject txnTypeQuery = new BasicDBObject();
		 * 
		 * txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(),
		 * TransactionType.SALE.getName())); txnTypeConditionLst.add(new
		 * BasicDBObject(FieldType.TXNTYPE.getName(),
		 * TransactionType.ENROLL.getName())); txnTypeConditionLst.add(new
		 * BasicDBObject(FieldType.TXNTYPE.getName(),
		 * TransactionType.NEWORDER.getName())); txnTypeConditionLst.add(new
		 * BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
		 * 
		 * txnTypeQuery.append("$or", txnTypeConditionLst);
		 * 
		 * dbObjList.add(txnTypeQuery);
		 */
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		FindIterable<Document> cursor = coll.find(andQuery);

		// MongoCursor<Document> cursor2 = cursor.iterator();

		if (cursor.iterator().hasNext()) {
			Document documentObj = cursor.iterator().next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}

					}
				}
			}
			fields.logAllFields("Previous fields");
		}
		cursor.iterator().close();

		return fields;

	}

	@SuppressWarnings("static-access")
	public Fields createAllSelectForSettlement(String custId, String oId, String status) {

		Fields fields = new Fields();
		// PropertiesManager propManager = new PropertiesManager();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		// List<BasicDBObject> txnTypeConditionLst = new
		// ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.CUSTOMER_ID.getName(), custId));
		dbObjList.add(new BasicDBObject(FieldType.TXN_ID.getName(), oId));
		dbObjList.add(new BasicDBObject(FieldType.STATUS.getName(), status));

		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				propertiesManager.propertiesMap.get(prefix + Constants.SETTLEMENT_COLLECTION_NAME.getValue()));

		FindIterable<Document> cursor = coll.find(andQuery);

		// MongoCursor<Document> cursor2 = cursor.iterator();

		if (cursor.iterator().hasNext()) {
			Document documentObj = cursor.iterator().next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}

					}
				}
			}
			fields.logAllFields("Previous fields");
		}
		cursor.iterator().close();

		return fields;

	}

	@SuppressWarnings("static-access")
	public Fields createAllSelectForSale(String orderId, String payId, String amount, String txnType, String status,
			String pgRefNum) {

		Fields fields = new Fields();
		// PropertiesManager propManager = new PropertiesManager();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		// List<BasicDBObject> txnTypeConditionLst = new
		// ArrayList<BasicDBObject>();
		List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();

		BasicDBObject txnTypeQuery = new BasicDBObject();
		txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName()));
		// txnTypeConditionLst.add(new
		// BasicDBObject(FieldType.TXNTYPE.getName(),
		// TransactionType.NEWORDER.getName()));
		txnTypeQuery.append("$or", txnTypeConditionLst);

		dbObjList.add(txnTypeQuery);
		dbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		dbObjList.add(new BasicDBObject(FieldType.AMOUNT.getName(), amount));
		// dbObjList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),
		// txnType));
		dbObjList.add(new BasicDBObject(FieldType.STATUS.getName(), status));
		dbObjList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));

		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		FindIterable<Document> cursor = coll.find(andQuery);

		// MongoCursor<Document> cursor2 = cursor.iterator();

		if (cursor.iterator().hasNext()) {
			Document documentObj = cursor.iterator().next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}

					}
				}
			}
			fields.logAllFields("Previous fields");
		}
		cursor.iterator().close();

		return fields;

	}

	@SuppressWarnings("static-access")
	public Long createAllSelectForSaleOrderId(String orderId, String payId, String currencyCode, Fields fields) {

		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			dbObjList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
					fields.get(FieldType.SUB_MERCHANT_ID.getName())));
		} else {
			dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		}

		dbObjList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyCode));
		dbObjList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		dbObjList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		long recordsCount = coll.count(andQuery);

		return recordsCount;

	}

	@SuppressWarnings("static-access")
	public Long createAllSelectForDuplicateSaleOrderId(String orderId, String payId, String currencyCode,
			Fields fields) {

		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			dbObjList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
					fields.get(FieldType.SUB_MERCHANT_ID.getName())));
		} else {
			dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		}

		dbObjList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyCode));
		dbObjList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		long recordsCount = coll.count(andQuery);
		return recordsCount;

	}

	@SuppressWarnings("static-access")
	public Long createAllSelectForDuplicateSubmit(String orderId) {

		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> dbSaleSentTobankList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> dbSaleEnrolledList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> dbSalePendingList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> dbEnrollPendingList = new ArrayList<BasicDBObject>();

		dbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));

		dbSaleSentTobankList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		dbSaleSentTobankList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));
		BasicDBObject dbSaleSentTobankQuery = new BasicDBObject("$and", dbSaleSentTobankList);

		dbSalePendingList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		dbSalePendingList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PENDING.getName()));
		BasicDBObject dbSalePendingQuery = new BasicDBObject("$and", dbSalePendingList);

		dbSaleEnrolledList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName()));
		dbSaleEnrolledList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ENROLLED.getName()));
		BasicDBObject dbSaleEnrolledTobankQuery = new BasicDBObject("$and", dbSaleEnrolledList);

		dbEnrollPendingList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName()));
		dbEnrollPendingList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PENDING.getName()));
		BasicDBObject dbEnrollPendingQuery = new BasicDBObject("$and", dbEnrollPendingList);

		List<BasicDBObject> dbTxnConditionList = new ArrayList<BasicDBObject>();
		dbTxnConditionList.add(dbSaleSentTobankQuery);
		dbTxnConditionList.add(dbSaleEnrolledTobankQuery);
		dbTxnConditionList.add(dbSalePendingQuery);
		dbTxnConditionList.add(dbEnrollPendingQuery);

		BasicDBObject andQuery = new BasicDBObject();
		andQuery.append("$and", dbObjList);
		andQuery.append("$or", dbTxnConditionList);

		logger.info("check dupicate submit for a same order Id :" + andQuery);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		long recordsCount = coll.count(andQuery);

		return recordsCount;

	}

	@SuppressWarnings("static-access")
	public Long createAllSelectForRefundOrderIdSale(String orderId, String payId, String currencyCode, Fields fields) {

		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.REFUND_ORDER_ID.getName(), orderId));
		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			dbObjList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
					fields.get(FieldType.SUB_MERCHANT_ID.getName())));
		} else {
			dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		}
		dbObjList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyCode));
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		long recordsCount = coll.count(andQuery);

		return recordsCount;

	}

	@SuppressWarnings({ "static-access" })
	public Long createAllSelectForSaleOrderIdRefund(String refundOrderId, String payId, String currencyCode,
			Fields fields) {

		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), refundOrderId));
		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			dbObjList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
					fields.get(FieldType.SUB_MERCHANT_ID.getName())));
		} else {
			dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		}
		dbObjList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyCode));
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		long recordsCount = coll.count(andQuery);

		return recordsCount;

	}

	public Long createAllSelectForOrderId(Fields fields, String orderId, String payId, String currencyCode) {

		String refundorderId = fields.get(FieldType.REFUND_ORDER_ID.getName());
		orderId = fields.get(FieldType.ORDER_ID.getName());
		payId = fields.get(FieldType.PAY_ID.getName());
		currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());

		List<BasicDBObject> duplicateConditionList = new ArrayList<BasicDBObject>();
		long recordsCount = 0;

		if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.REFUND.getName())) {

			// duplicateConditionList.add(new
			// BasicDBObject(FieldType.ORDER_ID.getName(),
			// refundorderId));
			duplicateConditionList.add(new BasicDBObject(FieldType.REFUND_ORDER_ID.getName(), refundorderId));
			duplicateConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
		} else {
			duplicateConditionList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			duplicateConditionList.add(new BasicDBObject(FieldType.REFUND_ORDER_ID.getName(), orderId));

		}

		BasicDBObject duplicateConditionsQuery = new BasicDBObject("$and", duplicateConditionList);

		List<BasicDBObject> finalList1 = new ArrayList<BasicDBObject>();
		finalList1.add(duplicateConditionsQuery);
		finalList1.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		finalList1.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyCode));

		BasicDBObject finalQuery = new BasicDBObject("$and", finalList1);
		logger.info("Check dupicate order id for Never createAllSelectForOrderId: " + finalQuery);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		recordsCount = coll.count(finalQuery);

		return recordsCount;

	}

	@SuppressWarnings("static-access")
	public Long createAllSelectForRefundOrderId(String refundOrderId, String payId, String currencyCode, String status,
			Fields fields) {

		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.REFUND_ORDER_ID.getName(), refundOrderId));
		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			dbObjList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
					fields.get(FieldType.SUB_MERCHANT_ID.getName())));
		} else {
			dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		}
		dbObjList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyCode));

		List<BasicDBObject> statusList = new ArrayList<BasicDBObject>();
		statusList.add(new BasicDBObject(FieldType.STATUS.getName(), status));
		statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PENDING.getName()));

		dbObjList.add(new BasicDBObject("$or", statusList));
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		logger.info("check dupicate refund order id individually createAllSelectForRefundOrderId :  " + andQuery);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		long recordsCount = coll.count(andQuery);

		return recordsCount;

	}

	@SuppressWarnings("static-access")
	private Fields createAllSelectForReco(String pgrefNo, String payId, String amount, String orderId, String txnType,
			String currencyCode) {

		Fields fields = new Fields();
		// PropertiesManager propManager = new PropertiesManager();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgrefNo));
		dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		dbObjList.add(new BasicDBObject(FieldType.AMOUNT.getName(), amount));
		dbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		dbObjList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));
		dbObjList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyCode));

		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		MongoCursor<Document> cursor = coll.find(andQuery).iterator();
		if (cursor.hasNext()) {
			Document documentObj = cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}

					}
				}
			}
			fields.logAllFields("Previous fields");
		}
		cursor.close();
		return fields;

	}

	@SuppressWarnings("static-access")
	public Fields createAllSelectForRefundStatus(String orderId, String payId, String amount) {

		Fields fields = new Fields();
		// PropertiesManager propManager = new PropertiesManager();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.REFUND_ORDER_ID.getName(), orderId));
		dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		dbObjList.add(new BasicDBObject(FieldType.AMOUNT.getName(), amount));
		BasicDBObject txnTypeQuery = new BasicDBObject();

		txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
		txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));

		txnTypeQuery.append("$or", txnTypeConditionLst);

		dbObjList.add(txnTypeQuery);
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		FindIterable<Document> cursor = coll.find(andQuery).sort(new BasicDBObject("CREATE_DATE", -1)).limit(1);

		MongoCursor<Document> cursor2 = cursor.iterator();

		if (cursor2.hasNext()) {
			Document documentObj = cursor2.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}

					}
				}
			}
			fields.logAllFields("Previous fields");
		}
		cursor2.close();

		return fields;

	}

	@SuppressWarnings("static-access")
	public Fields createAllSelectForSale(String oid) {
		Fields fields = new Fields();
		// PropertiesManager propManager = new PropertiesManager();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.OID.getName(), oid));
		BasicDBObject txnTypeQuery = new BasicDBObject();

		txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		txnTypeConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

		txnTypeQuery.append("$and", txnTypeConditionLst);

		dbObjList.add(txnTypeQuery);
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		FindIterable<Document> cursor = coll.find(andQuery).sort(new BasicDBObject("CREATE_DATE", -1)).limit(1);

		MongoCursor<Document> cursor2 = cursor.iterator();

		if (cursor2.hasNext()) {
			Document documentObj = cursor2.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}

					}
				}
			}
			fields.logAllFields("Previous fields");
		}
		cursor2.close();

		return fields;
	}

	@SuppressWarnings("static-access")
	public void getDuplicate(Fields fields) throws SystemException {
		try {
			MongoDatabase dbIns = null;
			String orderId = fields.get(FieldType.ORDER_ID.getName());
			String payId = fields.get(FieldType.PAY_ID.getName());
			String currencyString = fields.get(FieldType.CURRENCY_CODE.getName());
			String amount = fields.get(FieldType.AMOUNT.getName());
			if (!StringUtils.isEmpty(amount) && !StringUtils.isEmpty(currencyString)) {
				amount = Amount.toDecimal(amount, currencyString);
			}
			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			conditionList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			conditionList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			conditionList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyString));
			conditionList.add(new BasicDBObject(FieldType.AMOUNT.getName(), amount));
			BasicDBObject query = new BasicDBObject("$and", conditionList);
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			if (cursor.hasNext()) {
				Document documentObj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				if (null != documentObj) {
					for (int i = 0; i < documentObj.size(); i++) {
						String ORIG_TXN_ID = documentObj.getString(FieldType.ORIG_TXN_ID.getName());
						fields.put(FieldType.ORIG_TXN_ID.getName(), ORIG_TXN_ID);
						fields.put(FieldType.DUPLICATE_YN.getName(), "Y");
					}
				} else {
					fields.put(FieldType.DUPLICATE_YN.getName(), "N");
				}
			}
			cursor.close();
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Fields getFields(String txnId, String payId) throws SystemException {
		try {
			return createAllSelect(txnId, payId);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Fields getFieldsForRefund(String txnId, String payId, String txnType, String status) throws SystemException {
		try {
			return createAllForRefund(txnId, payId, txnType, status);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "No such transaction found";
			logger.error(message, exception);
			throw new SystemException(ErrorType.NO_SUCH_TRANSACTION, exception, message);
		}
	}

	public Fields getNewOrderFields(String orderId, String payId, String txnType, String status)
			throws SystemException {
		try {
			return createAllOfNewOrder(orderId, payId, txnType, status);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "No such transaction found";
			logger.error(message, exception);
			throw new SystemException(ErrorType.NO_SUCH_TRANSACTION, exception, message);
		}
	}

	public Fields getFieldsForFedUpi(String refId, String txnType) throws SystemException {
		try {
			return createAllForFedUpi(refId, txnType);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Fields getFieldsForHdfcUpi(String refId, String txnType) throws SystemException {
		try {
			return createAllForHdfcUpi(refId, txnType);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Fields getFieldsForReco(String pgrefNo, String payId, String amount, String orderId, String txnType,
			String currencyCode) throws SystemException {
		try {
			return createAllSelectForReco(pgrefNo, payId, amount, orderId, txnType, currencyCode);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Fields getFieldsForStatus(String orderId, String payId, String amount, String currencyCode)
			throws SystemException {
		try {
			return createAllSelectForStatus(orderId, payId, amount, currencyCode);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "No such transaction found";
			logger.error(message, exception);
			throw new SystemException(ErrorType.NO_SUCH_TRANSACTION, exception, message);
		}
	}

	public Fields getFieldsForSaleTxn(String orderId, String payId, String amount, String txnType, String status,
			String pgRefNum) throws SystemException {
		try {
			return createAllSelectForSale(orderId, payId, amount, txnType, status, pgRefNum);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "No such transaction found";
			logger.error(message, exception);
			throw new SystemException(ErrorType.NO_SUCH_TRANSACTION, exception, message);
		}
	}

	public Long validateDuplicateSaleOrderId(String orderId, String payId, String currencyCode, Fields fields)
			throws SystemException {
		try {
			return createAllSelectForSaleOrderId(orderId, payId, currencyCode, fields);
		} catch (Exception exception) {
			String message = "Error while searching transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Long validateSaleDuplicateOrderId(String orderId, String payId, String currencyCode, Fields fields)
			throws SystemException {
		try {
			return createAllSelectForDuplicateSaleOrderId(orderId, payId, currencyCode, fields);
		} catch (Exception exception) {
			String message = "Error while searching transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Long validateDuplicateSubmit(String orderId) throws SystemException {
		try {
			return createAllSelectForDuplicateSubmit(orderId);
		} catch (Exception exception) {
			String message = "Error while searching transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Long validateDuplicateSaleOrderIdInRefund(String RefundOrderId, String payId, String currencyCode,
			Fields fields) throws SystemException {
		try {
			return createAllSelectForSaleOrderIdRefund(RefundOrderId, payId, currencyCode, fields);
		} catch (Exception exception) {
			String message = "Error while searching transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Long validateDuplicateRefundOrderIdInSale(String orderId, String payId, String currencyCode, Fields fields)
			throws SystemException {
		try {
			return createAllSelectForRefundOrderIdSale(orderId, payId, currencyCode, fields);
		} catch (Exception exception) {
			String message = "Error while searching transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Long validateDuplicateOrderId(Fields fields, String orderId, String payId, String currencyCode)
			throws SystemException {
		try {
			return createAllSelectForOrderId(fields, orderId, payId, currencyCode);
		} catch (Exception exception) {

			String message = "Error while searching transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Long validateDuplicateRefundOrderId(String refundOrderId, String payId, String currencyCode, String status,
			Fields fields) throws SystemException {
		try {
			return createAllSelectForRefundOrderId(refundOrderId, payId, currencyCode, status, fields);
		} catch (Exception exception) {
			String message = "Error while searching transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Fields getFieldsForVerify(String orderId, String payId, String amount, String pgRefNum, String status)
			throws SystemException {
		try {
			return createAllSelectForVerify(orderId, payId, amount, pgRefNum, status);
		} catch (Exception exception) {
			String message = "Error while searching transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Fields getFieldsForSettlement(String custId, String oId, String status) throws SystemException {
		try {
			return createAllSelectForSettlement(custId, oId, status);
		} catch (Exception exception) {
			String message = "Error while searching transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Fields getFieldsForRefundStatus(String orderId, String payId, String amount) throws SystemException {
		try {
			return createAllSelectForRefundStatus(orderId, payId, amount);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Fields getFieldsForSale(String oid) throws SystemException {
		try {
			return createAllSelectForSale(oid);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void insertTransaction(Fields fields) throws SystemException {
		try {

			// String msedclPayId = propertiesManager.propertiesMap.get("MSEDCL_PAY_ID");
			MongoDatabase dbIns = null;
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			// Changes by Shaiwal
			// Check if status capture already added present for SALE and
			// current
			// transaction is Cancelled by User or Duplicate
			// If this is the case, then abort DB entry

			if (StringUtils.isNotBlank(fields.get(FieldType.TXNTYPE.getName()))
					&& StringUtils.isNotBlank(fields.get(FieldType.STATUS.getName()))) {

				if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName()) && (fields
						.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CANCELLED.getName())
						|| fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.DUPLICATE.getName()))) {

					// Check if Sale Captured entry is present for this Order Id
					List<BasicDBObject> saleConList = new ArrayList<BasicDBObject>();
					saleConList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
					saleConList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
					saleConList.add(
							new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));
					saleConList
							.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));

					BasicDBObject saleCaptureQuery = new BasicDBObject("$and", saleConList);

					long count = collection.count(saleCaptureQuery);

					if (count > 0) {
						logger.info("Skipping duplicate capture entry for transaction with Order ID = "
								+ fields.get(FieldType.ORDER_ID.getName()));
						return;
					}
				}
			}

			// Check to see if a transaction has PG REF NUM == 0 and status ==
			// Captured and
			// discard that entry
			if (StringUtils.isNotBlank(fields.get(FieldType.TXNTYPE.getName()))
					&& StringUtils.isNotBlank(fields.get(FieldType.STATUS.getName()))) {

				if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())
						&& fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {

					if (fields.get(FieldType.PG_REF_NUM.getName()).equalsIgnoreCase("0")) {
						logger.info("PG REF NUM is 0 for Order ID = " + fields.get(FieldType.ORDER_ID.getName())
								+ "  , Skipping entry of this txn");
						return;
					}

				}
			}

			// Check to see if a transaction is already added in DB with Same
			// RRN and status as Captured and Skip
			// that entry
			/*
			 * if (StringUtils.isNotBlank(fields.get(FieldType.TXNTYPE.getName())) &&
			 * StringUtils.isNotBlank(fields.get(FieldType.STATUS.getName())) &&
			 * StringUtils.isNotBlank(fields.get(FieldType.RRN.getName())) &&
			 * StringUtils.isNotBlank(fields.get(FieldType.MOP_TYPE.getName())) &&
			 * StringUtils.isNotBlank(fields.get(FieldType.PAYMENT_TYPE.getName()))) {
			 * 
			 * if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.
			 * SALE.getName()) &&
			 * fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.
			 * getName()) &&
			 * fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI
			 * .getCode()) &&
			 * fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.
			 * STATIC_UPI_QR.getCode())) {
			 * 
			 * List<BasicDBObject> saleConList = new ArrayList<BasicDBObject>();
			 * saleConList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),
			 * TransactionType.SALE.getName())); saleConList.add(new
			 * BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			 * saleConList.add(new BasicDBObject(FieldType.RRN.getName(),
			 * fields.get(FieldType.RRN.getName()))); saleConList .add(new
			 * BasicDBObject(FieldType.PAY_ID.getName(),
			 * fields.get(FieldType.PAY_ID.getName())));
			 * 
			 * BasicDBObject saleCaptureQuery = new BasicDBObject("$and", saleConList);
			 * 
			 * long count = collection.count(saleCaptureQuery);
			 * 
			 * if (count > 0) { logger.
			 * info("Skipping duplicate capture entry for transaction with Same RRN = " +
			 * fields.get(FieldType.RRN.getName()) + " and Order ID = " +
			 * fields.get(FieldType.ORDER_ID.getName()));
			 * fields.logAllFields("Skipped Entry of this field because of duplicate RRN");
			 * return; }
			 * 
			 * }
			 * 
			 * }
			 */

			// Check to see if a transaction is already added in DB with Same
			// ACQ_ID and status as Captured and Skip
			// that entry
			if (StringUtils.isNotBlank(fields.get(FieldType.TXNTYPE.getName()))
					&& StringUtils.isNotBlank(fields.get(FieldType.STATUS.getName()))
					&& StringUtils.isNotBlank(fields.get(FieldType.ACQ_ID.getName()))) {

				if ((fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.ENROLL.getName())
						|| fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName()))
						&& fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {

					List<BasicDBObject> saleConList = new ArrayList<BasicDBObject>();
					saleConList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
					saleConList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
					saleConList
							.add(new BasicDBObject(FieldType.ACQ_ID.getName(), fields.get(FieldType.ACQ_ID.getName())));
					saleConList
							.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));

					BasicDBObject saleCaptureQuery = new BasicDBObject("$and", saleConList);

					long count = collection.count(saleCaptureQuery);

					if (count > 0) {
						logger.info("Skipping duplicate capture entry for transaction with Same ACQ ID = "
								+ fields.get(FieldType.ACQ_ID.getName()) + " and Order ID = "
								+ fields.get(FieldType.ORDER_ID.getName()));
						fields.logAllFields("Skipped Entry of this field because of duplicate ACQ ID");
						return;
					}

				}

			}

			BasicDBObject newFieldsObj = new BasicDBObject();
			if (StringUtils.isBlank(fields.get(FieldType.TXN_ID.getName()))) {
				fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());
			}
			String amountString = fields.get(FieldType.AMOUNT.getName());
			String surchargeAmountString = fields.get(FieldType.SURCHARGE_AMOUNT.getName());
			String currencyString = fields.get(FieldType.CURRENCY_CODE.getName());
			String totalAmountString = fields.get(FieldType.TOTAL_AMOUNT.getName());

			String amount = "0";
			if (!StringUtils.isEmpty(amountString) && !StringUtils.isEmpty(currencyString)) {
				if (!amountString.contains(".")) {
					amount = Amount.toDecimal(amountString, currencyString);
				} else {
					amount = amountString;
				}
				newFieldsObj.put(FieldType.AMOUNT.getName(), amount);
			}

			String totalAmount = "0";
			if (!StringUtils.isEmpty(totalAmountString) && !StringUtils.isEmpty(currencyString)) {
				if (!totalAmountString.contains(".")) {
					totalAmount = Amount.toDecimal(totalAmountString, currencyString);
				} else {
					totalAmount = totalAmountString;
				}
				newFieldsObj.put(FieldType.TOTAL_AMOUNT.getName(), totalAmount);
			}

			String surchargeAmount = "0";
			if (!StringUtils.isEmpty(surchargeAmountString) && !StringUtils.isEmpty(currencyString)) {
				surchargeAmount = Amount.toDecimal(surchargeAmountString, currencyString);
				newFieldsObj.put(FieldType.SURCHARGE_AMOUNT.getName(), surchargeAmount);
			}
			String origTxnId = "0";
			String origTxnStr = fields.get(FieldType.ORIG_TXN_ID.getName());
			if (StringUtils.isEmpty(origTxnStr)) {
				String internalOrigTxnStr = fields.get(FieldType.INTERNAL_ORIG_TXN_ID.getName());
				if (StringUtils.isEmpty(internalOrigTxnStr)) {
					newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(), origTxnId);
				}
				if (!StringUtils.isEmpty(internalOrigTxnStr)) {
					newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(), internalOrigTxnStr);
				}
			}

			if (!StringUtils.isEmpty(origTxnStr)) {
				newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(), origTxnStr);
			}

			String origTxnType = fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
			if (!StringUtils.isEmpty(origTxnType)) {
				String txnType = fields.get(FieldType.TXNTYPE.getName());
				if ((txnType.equals(TransactionType.REFUND.getName()))
						|| (txnType.equals(TransactionType.REFUNDRECO.getName()))) {
					newFieldsObj.put(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName());
				} else {
					newFieldsObj.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
				}

			}

			if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.REFUND.getName())
					&& fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
				if (fields.contains("LimitAmountUsed")) {
					newFieldsObj.put(FieldType.REFUND_LIMIT_USED.getName(), fields.get("LimitAmountUsed"));
				} else {
					newFieldsObj.put(FieldType.REFUND_LIMIT_USED.getName(), "0.0");
				}
			}

			String pgRefNo = "0";
			String pgRefNum = fields.get(FieldType.PG_REF_NUM.getName());
			if (StringUtils.isEmpty(pgRefNum)) {
				newFieldsObj.put(FieldType.PG_REF_NUM.getName(), pgRefNo);
			}

			if (!StringUtils.isEmpty(pgRefNum)) {
				newFieldsObj.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
			}
			String acctId = "0";
			String acctIdStr = fields.get(FieldType.ACCT_ID.getName());
			if (acctIdStr != null && acctIdStr.length() > 0) {

				newFieldsObj.put(FieldType.ACCT_ID.getName(), acctIdStr);
			}
			if (acctIdStr == null) {
				newFieldsObj.put(FieldType.ACCT_ID.getName(), acctId);
			}
			String acqId = "0";
			String acqIdStr = fields.get(FieldType.ACQ_ID.getName());
			if (acqIdStr != null && acqIdStr.length() > 0) {
				newFieldsObj.put(FieldType.ACQ_ID.getName(), acqIdStr);
			}
			if (acqIdStr == null) {
				newFieldsObj.put(FieldType.ACQ_ID.getName(), acqId);
			}
			String oid = fields.get(FieldType.OID.getName());
			String longOid = "0";
			if (!StringUtils.isEmpty(oid)) {

				newFieldsObj.put(FieldType.OID.getName(), oid);
			}
			if (StringUtils.isEmpty(oid)) {
				newFieldsObj.put(FieldType.OID.getName(), longOid);
			}
			String udf1 = fields.get(FieldType.UDF1.getName());
			if (!StringUtils.isEmpty(udf1)) {
				newFieldsObj.put(FieldType.UDF1.getName(), udf1);
			}
			String udf2 = fields.get(FieldType.UDF2.getName());
			if (!StringUtils.isEmpty(udf2)) {
				newFieldsObj.put(FieldType.UDF2.getName(), udf2);
			}
			String udf3 = fields.get(FieldType.UDF3.getName());
			if (!StringUtils.isEmpty(udf3)) {
				newFieldsObj.put(FieldType.UDF3.getName(), udf3);
			}
			String udf4 = fields.get(FieldType.UDF4.getName());
			if (!StringUtils.isEmpty(udf4)) {
				newFieldsObj.put(FieldType.UDF4.getName(), udf4);
			}
			String udf5 = fields.get(FieldType.UDF5.getName());
			if (!StringUtils.isEmpty(udf5)) {
				newFieldsObj.put(FieldType.UDF5.getName(), udf5);
			}
			String udf6 = fields.get(FieldType.UDF6.getName());
			if (!StringUtils.isEmpty(udf6)) {
				newFieldsObj.put(FieldType.UDF6.getName(), udf6);
			}
			String udf7 = fields.get(FieldType.UDF7.getName());
			if (!StringUtils.isEmpty(udf7)) {
				newFieldsObj.put(FieldType.UDF7.getName(), Amount.toDecimal(udf7, currencyString));
			}
			String udf8 = fields.get(FieldType.UDF8.getName());
			if (!StringUtils.isEmpty(udf8)) {
				newFieldsObj.put(FieldType.UDF8.getName(), Amount.toDecimal(udf8, currencyString));
			}
			String udf9 = fields.get(FieldType.UDF9.getName());
			if (!StringUtils.isEmpty(udf9)) {
				newFieldsObj.put(FieldType.UDF9.getName(), Amount.toDecimal(udf9, currencyString));
			}
			String udf10 = fields.get(FieldType.UDF10.getName());
			if (!StringUtils.isEmpty(udf10)) {
				newFieldsObj.put(FieldType.UDF10.getName(), Amount.toDecimal(udf10, currencyString));
			}
			String udf11 = fields.get(FieldType.UDF11.getName());
			if (!StringUtils.isEmpty(udf11)) {
				newFieldsObj.put(FieldType.UDF11.getName(), udf11);
			}
			String udf12 = fields.get(FieldType.UDF12.getName());
			if (!StringUtils.isEmpty(udf12)) {
				newFieldsObj.put(FieldType.UDF12.getName(), udf12);
			}
			String udf13 = fields.get(FieldType.UDF13.getName());
			if (!StringUtils.isEmpty(udf13)) {
				newFieldsObj.put(FieldType.UDF13.getName(), udf13);
			}
			String udf14 = fields.get(FieldType.UDF14.getName());
			if (!StringUtils.isEmpty(udf14)) {
				newFieldsObj.put(FieldType.UDF14.getName(), udf14);
			}
			String udf15 = fields.get(FieldType.UDF15.getName());
			if (!StringUtils.isEmpty(udf15)) {
				newFieldsObj.put(FieldType.UDF15.getName(), udf15);
			}
			String udf16 = fields.get(FieldType.UDF16.getName());
			if (!StringUtils.isEmpty(udf16)) {
				newFieldsObj.put(FieldType.UDF16.getName(), udf16);
			}
			String udf17 = fields.get(FieldType.UDF17.getName());
			if (!StringUtils.isEmpty(udf17)) {
				newFieldsObj.put(FieldType.UDF17.getName(), udf17);
			}
			String udf18 = fields.get(FieldType.UDF18.getName());
			if (!StringUtils.isEmpty(udf18)) {
				newFieldsObj.put(FieldType.UDF18.getName(), udf18);
			}
			String categoryCode = fields.get(FieldType.CATEGORY_CODE.getName());
			if (!StringUtils.isEmpty(categoryCode)) {
				newFieldsObj.put(FieldType.CATEGORY_CODE.getName(), categoryCode);
			}
			String skuCode = fields.get(FieldType.SKU_CODE.getName());
			if (!StringUtils.isEmpty(skuCode)) {
				newFieldsObj.put(FieldType.SKU_CODE.getName(), skuCode);
			}
			String productAmount = fields.get(FieldType.PRODUCT_AMOUNT.getName());
			if (!StringUtils.isEmpty(productAmount)) {
				newFieldsObj.put(FieldType.PRODUCT_AMOUNT.getName(), productAmount);
			}
			String refundCycleDays = fields.get(FieldType.REFUND_CYCLE_DAYS.getName());
			if (!StringUtils.isEmpty(refundCycleDays)) {
				newFieldsObj.put(FieldType.REFUND_CYCLE_DAYS.getName(), refundCycleDays);
			}
			String paymentsRegion = fields.get(FieldType.PAYMENTS_REGION.getName());
			if (!StringUtils.isEmpty(paymentsRegion)) {
				newFieldsObj.put(FieldType.PAYMENTS_REGION.getName(), paymentsRegion);
			}

			String cardHolderType = fields.get(FieldType.CARD_HOLDER_TYPE.getName());
			if (!StringUtils.isEmpty(cardHolderType)) {
				newFieldsObj.put(FieldType.CARD_HOLDER_TYPE.getName(), cardHolderType);
			}

			String requestDate = fields.get(FieldType.REQUEST_DATE.getName());
			if (!StringUtils.isEmpty(requestDate)) {
				newFieldsObj.put(FieldType.REQUEST_DATE.getName(), requestDate);
			}

			String saleAmoiunt = fields.get(FieldType.SALE_AMOUNT.getName());
			if ((!StringUtils.isEmpty(saleAmoiunt) && !StringUtils.isEmpty(currencyString))) {
				saleAmoiunt = Amount.toDecimal(saleAmoiunt, currencyString);
				newFieldsObj.put(FieldType.SALE_AMOUNT.getName(), saleAmoiunt);
			}

			String totalSaleAmoiunt = fields.get(FieldType.SALE_TOTAL_AMOUNT.getName());
			if ((!StringUtils.isEmpty(totalSaleAmoiunt) && !StringUtils.isEmpty(currencyString))) {
				totalSaleAmoiunt = Amount.toDecimal(totalSaleAmoiunt, currencyString);
				newFieldsObj.put(FieldType.SALE_TOTAL_AMOUNT.getName(), totalSaleAmoiunt);
			}

			String acquirerMode = fields.get(FieldType.ACQUIRER_MODE.getName());
			if (!StringUtils.isEmpty(acquirerMode)) {
				newFieldsObj.put(FieldType.ACQUIRER_MODE.getName(), acquirerMode);
			}

			String tenure = fields.get(FieldType.TENURE.getName());
			if (!StringUtils.isEmpty(tenure)) {
				newFieldsObj.put(FieldType.TENURE.getName(), tenure);
			}

			String issuerBank = fields.get(FieldType.ISSUER_BANK.getName());
			if (!StringUtils.isEmpty(issuerBank)) {
				newFieldsObj.put(FieldType.ISSUER_BANK.getName(), issuerBank);
			}

			String emiPerMonth = fields.get(FieldType.EMI_PER_MONTH.getName());
			if (!StringUtils.isEmpty(emiPerMonth)) {
				newFieldsObj.put(FieldType.EMI_PER_MONTH.getName(), emiPerMonth);
			}

			String emiTotalAmount = fields.get(FieldType.EMI_TOTAL_AMOUNT.getName());
			if (!StringUtils.isEmpty(emiTotalAmount)) {
				newFieldsObj.put(FieldType.EMI_TOTAL_AMOUNT.getName(), emiTotalAmount);
			}

			String rateOfInterest = fields.get(FieldType.RATE_OF_INTEREST.getName());
			if (!StringUtils.isEmpty(rateOfInterest)) {
				newFieldsObj.put(FieldType.RATE_OF_INTEREST.getName(), rateOfInterest);
			}

			String custId = fields.get(FieldType.CUST_ID.getName());
			if (!StringUtils.isEmpty(custId)) {
				newFieldsObj.put(FieldType.CUST_ID.getName(), custId);
			}

			String emiInterest = fields.get(FieldType.EMI_INTEREST.getName());
			if (!StringUtils.isEmpty(emiInterest)) {
				newFieldsObj.put(FieldType.EMI_INTEREST.getName(), emiInterest);
			}

			String payerAddress = fields.get(FieldType.PAYER_ADDRESS.getName());
			if (!StringUtils.isEmpty(payerAddress)) {
				newFieldsObj.put(FieldType.PAYER_ADDRESS.getName(), payerAddress);
			}

			/*
			 * String cardhash = null;
			 * 
			 * if (StringUtils.isNotBlank(fields.get(FieldType.H_CARD_NUMBER. getName()))) {
			 * 
			 * cardhash = fields.get(FieldType.H_CARD_NUMBER.getName()); } else { cardhash =
			 * getCardHash(fields); }
			 */

			/*
			 * if (StringUtils.isNotBlank(cardhash)) {
			 * newFieldsObj.put(FieldType.H_CARD_NUMBER.getName(), cardhash); }
			 */

			if (!StringUtils.isEmpty(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				newFieldsObj.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.REG_NUMBER.getName()))) {
				newFieldsObj.put(FieldType.REG_NUMBER.getName(), fields.get(FieldType.REG_NUMBER.getName()));
			}

			String prodName = fields.get(FieldType.PRODUCT_NAME.getName());
			if (!StringUtils.isEmpty(prodName)) {
				newFieldsObj.put(FieldType.PRODUCT_NAME.getName(), prodName);
			}

			String quantity = fields.get(FieldType.QUANTITY.getName());
			if (!StringUtils.isEmpty(quantity)) {
				newFieldsObj.put(FieldType.QUANTITY.getName(), quantity);
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.EPOS_MERCHANT.getName()))) {
				newFieldsObj.put(FieldType.EPOS_MERCHANT.getName(), fields.get(FieldType.EPOS_MERCHANT.getName()));
				newFieldsObj.put(FieldType.TRANSACTION_MODE.getName(), Constants.EPOS_MODE.getValue());
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.IS_CUSTOM_HOSTED.getName()))) {
				newFieldsObj.put(FieldType.IS_CUSTOM_HOSTED.getName(),
						fields.get(FieldType.IS_CUSTOM_HOSTED.getName()));
				newFieldsObj.put(FieldType.TRANSACTION_MODE.getName(), Constants.EVENT_MODE.getValue());
			}

			// checking if transaction is from invoice
			String returnUrl = fields.get(FieldType.RETURN_URL.getName());

			if (StringUtils.isNotEmpty(returnUrl) && returnUrl.contains("invoiceResponse")) {
				newFieldsObj.put(FieldType.TRANSACTION_MODE.getName(), Constants.INVOICE_MODE.getValue());

			} else if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.REFUND.getName())) {

				newFieldsObj.put(FieldType.TRANSACTION_MODE.getName(),
						fields.get(FieldType.TRANSACTION_MODE.getName()));

			} else if (StringUtils.isEmpty(fields.get(FieldType.IS_CUSTOM_HOSTED.getName()))
					&& StringUtils.isEmpty(fields.get(FieldType.EPOS_MERCHANT.getName()))) {

				newFieldsObj.put(FieldType.TRANSACTION_MODE.getName(), Constants.HTML_MODE.getValue());
			}

			newFieldsObj.put(FieldType.TXN_CAPTURE_FLAG.getName(), Constants.REAL_TIME_TXN.getValue());

			for (int i = 0; i < fields.size(); i++) {

				Date dNow = new Date();
				String dateNow = DateCreater.formatDateForDb(dNow);
				for (String columnName : aLLDB_Fields) {

					if (columnName.equals(FieldType.CREATE_DATE.getName())) {
						newFieldsObj.put(columnName, dateNow);
					} else if (columnName.equals(FieldType.DATE_INDEX.getName())) {
						newFieldsObj.put(columnName, dateNow.substring(0, 10).replace("-", ""));
					} else if (columnName.equals(FieldType.UPDATE_DATE.getName())) {
						newFieldsObj.put(columnName, dateNow);
					} else if (columnName.equals(FieldType.TXN_DATE.getName())) {
						newFieldsObj.put(columnName, dateNow.substring(0, 10).replace("-", ""));
					} else if (columnName.equals("_id")) {
						newFieldsObj.put(columnName, fields.get(FieldType.TXN_ID.getName()));
					} else if (columnName.equals(FieldType.INSERTION_DATE.getName())) {
						newFieldsObj.put(columnName, dNow);
					} else if (columnName.equals(FieldType.ACQUIRER_TDR_SC.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.ACQUIRER_TDR_SC.getName()));
					} else if (columnName.equals(FieldType.ACQUIRER_GST.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.ACQUIRER_GST.getName()));
					} else if (columnName.equals(FieldType.PG_TDR_SC.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.PG_TDR_SC.getName()));
					} else if (columnName.equals(FieldType.PG_GST.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.PG_GST.getName()));
					} else if (columnName.equals(FieldType.RESELLER_CHARGES.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.RESELLER_CHARGES.getName()));
					} else if (columnName.equals(FieldType.RESELLER_GST.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.RESELLER_GST.getName()));
					} else if (columnName.equals(FieldType.PG_RESELLER_CHARGE.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.PG_RESELLER_CHARGE.getName()));
					} else if (columnName.equals(FieldType.PG_RESELLER_GST.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.PG_RESELLER_GST.getName()));
					} else if (columnName.equals(FieldType.AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.TOTAL_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.SURCHARGE_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ORIG_TXN_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.PG_REF_NUM.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ACCT_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ACQ_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.OID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF1.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF2.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF3.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF4.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF5.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF6.getName())) {
						continue;
					} else if (columnName.equals(FieldType.PAYMENTS_REGION.getName())) {
						continue;
					} else if (columnName.equals(FieldType.CARD_HOLDER_TYPE.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ACQUIRER_MODE.getName())) {
						continue;
					} else if (columnName.equals(FieldType.TENURE.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ISSUER_BANK.getName())) {
						continue;
					} else if (columnName.equals(FieldType.RATE_OF_INTEREST.getName())) {
						continue;
					} else if (columnName.equals(FieldType.EMI_PER_MONTH.getName())) {
						continue;
					} else if (columnName.equals(FieldType.EMI_TOTAL_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.EMI_INTEREST.getName())) {
						continue;
					} else if (columnName.equals(FieldType.SALE_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.SALE_TOTAL_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ORIG_TXNTYPE.getName())) {
						continue;
					} else if (columnName.equals(FieldType.CUST_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.PAYER_ADDRESS.getName())) {
						continue;
					} else if (columnName.equals(FieldType.QUANTITY.getName())) {
						continue;
					} else if (columnName.equals(FieldType.PRODUCT_NAME.getName())) {
						continue;
					} else if (columnName.equals(FieldType.TRANSACTION_MODE.getName())) {
						continue;
					} else if (columnName.equals(FieldType.TXN_CAPTURE_FLAG.getName())) {
						continue;
					} else {
						newFieldsObj.put(columnName, fields.get(columnName));
					}
				}
			}

			Document doc = new Document(newFieldsObj);

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}

			// Inserting in Nodal Transaction & updating nodal Balance
			insertPayInTransactionNodalTopupCollection(doc);

			// updating oneTimeRefund value if it has been used.
			if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.REFUND.getName())
					&& fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())
					&& fields.contains("reducedLimitAmount")) {
				User user = null;
				if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
					user = userDao.findPayId(fields.get(FieldType.SUB_MERCHANT_ID.getName()));
				} else {
					user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
				}
				user.setRefundLimitRemains(new Float(fields.get("reducedLimitAmount")));
				userDao.update(user);
				merchantGridViewService.addUserInMap(user);
				refundLimitService.updateLimitsFromTxn(user);
			}

			// Added to update existing payment request to inactive when
			// transaction is
			// completed

			if (!(fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PENDING.getName())
					|| fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.SENT_TO_BANK.getName())
					|| fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.ENROLLED.getName())
					|| fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.SETTLED.getName()))
					&& fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())) {

				updatePaymentRequest(fields);

			}

			Runnable runnable = new Runnable() {

				@Override
				public void run() {

					try {
						// updateTxnStatus(newFieldsObj);
						updateStatusColl(fields.get(FieldType.ORIG_TXNTYPE.getName()),
								fields.get(FieldType.PG_REF_NUM.getName()), fields.get(FieldType.TXNTYPE.getName()),
								fields.get(FieldType.STATUS.getName()), doc);

						// Changes done by shaiwal to send callback for BG Merchant from PGWS for all
						// transactions except Sent
						// to Bank and Pending

						// Changes done by Sandeep sending callback for all Merchant whose All payment
						// flag is on
						if (StringUtils.isNotBlank(fields.get(FieldType.STATUS.getName()))
								&& StringUtils.isNotBlank(fields.get(FieldType.PAY_ID.getName()))
								&& !fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.SENT_TO_BANK.getName())
								&& !fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.PENDING.getName())
								&& !fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.ENROLLED.getName())
								&& fields.get(FieldType.TXNTYPE.getName())
										.equalsIgnoreCase(TransactionType.SALE.getName())) {

							logger.info("Merchant PAY ID found, sending callback from PGWS , Status Type is "
									+ fields.get(FieldType.STATUS.getName()));
							upiHostedBlock(fields);
							sendCallback(fields);

						}

					} catch (Exception e) {
						logger.error("Exception ", e);
					}
				}
			};

			propertiesManager.executorImpl(runnable);

			// Send Successful Transaction SMS to Merchant for UPI Static QR
			// Transaction
			if (StringUtils.isNotBlank(fields.get(FieldType.PAYMENT_TYPE.getName()))
					&& StringUtils.isNotBlank(fields.get(FieldType.MOP_TYPE.getName()))
					&& StringUtils.isNotBlank(fields.get(FieldType.STATUS.getName()))
					&& fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())
					&& fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.STATIC_UPI_QR.getCode())
					&& fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
				User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
				sendMerchantSMS(user.isTransactionMerchantSMSFlag(), user.getMobile(),
						fields.get(FieldType.TOTAL_AMOUNT.getName()), fields.get(FieldType.ORDER_ID.getName()));
			}

		} catch (Exception exception) {
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

		if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())
				&& fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())
				&& StringUtils.isNotEmpty(fields.get(FieldType.SKU_CODE.getName()))
				&& StringUtils.isNotEmpty(fields.get(FieldType.CATEGORY_CODE.getName()))
				&& StringUtils.isNotEmpty(fields.get(FieldType.REFUND_DAYS.getName()))
				&& StringUtils.isNotEmpty(fields.get(FieldType.PRODUCT_PRICE.getName()))
				&& StringUtils.isNotEmpty(fields.get(FieldType.VENDOR_ID.getName()))) {

			Fields newFields = new Fields(fields);

			Runnable runnable = new Runnable() {

				@Override
				public void run() {

					try {
						updateProdDescForCrm(newFields);
					} catch (Exception e) {
						logger.error("Exception, ", e);
					}
				}
			};

			propertiesManager.executorImpl(runnable);

		}

	}

	private void insertPayInTransactionNodalTopupCollection(Document doc) {

		try {
			if (doc != null) {

				String payId = doc.getString(FieldType.PAY_ID.getName());
				String paymentType = doc.getString(FieldType.PAYMENT_TYPE.getName());
				String status = doc.getString(FieldType.STATUS.getName());
				String txnType = doc.getString(FieldType.TXNTYPE.getName());

				User user = userDao.findPayId(doc.getString(FieldType.PAY_ID.getName()));
				UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(user.getPayId());
				if (merchantSettings.isAllowNodalPayoutFlag() && txnType.equals(TxnType.SALE.getName())
						&& status.equals(StatusType.CAPTURED.getName())) {

					MongoDatabase dbIns = mongoInstance.getDB();
					MongoCollection<Document> collTransaction = dbIns.getCollection(
							propertiesManager.propertiesMap.get(prefix + Constants.NODAL_TOPUP_TRANSACTON.getValue()));

					MongoCollection<Document> collBalance = dbIns.getCollection(
							propertiesManager.propertiesMap.get(prefix + Constants.NODAL_TOPUP_BALANCE.getValue()));

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String dateToday = sdf.format(new Date());

					String paymentTypeFromYml = propertiesManager.propertiesMap.get("nodalTopupPaymentType");
					BigDecimal payoutProcessingFeePercentage = new BigDecimal(
							propertiesManager.propertiesMap.get("PAYOUT_PROCESSING_FEE"));
					BigDecimal payoutTotalAllowedLimitPercentage = new BigDecimal(
							propertiesManager.propertiesMap.get("PAYOUT_ALLOWED_LIMIT")).setScale(2);
					BigDecimal payoutGst = new BigDecimal(propertiesManager.propertiesMap.get("PAYOUT_GST"))
							.setScale(2);

					BigDecimal amount = new BigDecimal(doc.getString(FieldType.AMOUNT.getName()));
					BigDecimal availableBalance = new BigDecimal("0.00");
					BigDecimal processingFee = new BigDecimal("0.00");
					BigDecimal gst = new BigDecimal("0.00");

					if (paymentTypeFromYml.contains(paymentType)) {

						// insert In nodal Transaction
						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

							collTransaction.insertOne(dataEncDecTool.encryptDocument(doc));
						} else {
							collTransaction.insertOne(doc);
						}

						// update nodal balance
						BasicDBObject query = new BasicDBObject(FieldType.PAY_ID.getName(), payId);

						MongoCursor<Document> cursor = collBalance.find(query).iterator();

						if (cursor.hasNext()) {
							Document documentObj = cursor.next();

							BigDecimal dbBalance = new BigDecimal(
									documentObj.getString(FieldType.TOTAL_BALANCE.getName()));
							BigDecimal dbAvailableBalance = new BigDecimal(
									documentObj.getString(FieldType.AVAILABLE_BALANCE.getName()));
							BigDecimal dbProcessingFee = new BigDecimal(
									documentObj.getString(FieldType.PROCESSING_FEE.getName()));
							BigDecimal dbGst = new BigDecimal(documentObj.getString(FieldType.PG_GST.getName()));

							availableBalance = amount.multiply(payoutTotalAllowedLimitPercentage)
									.divide(new BigDecimal("100"));

							logger.info("availableBalance before fee deduction " + availableBalance);

							processingFee = amount.multiply(payoutProcessingFeePercentage).divide(new BigDecimal("100"))
									.setScale(2, BigDecimal.ROUND_HALF_UP);
							gst = processingFee.multiply(payoutGst).divide(new BigDecimal("100")).setScale(2,
									BigDecimal.ROUND_HALF_UP);

							availableBalance = availableBalance.subtract(processingFee).subtract(gst).setScale(2,
									BigDecimal.ROUND_HALF_UP);

							logger.info("total amount " + amount + " availableBalance after fee decution "
									+ availableBalance);

							dbBalance = dbBalance.add(amount).setScale(2);
							dbAvailableBalance = dbAvailableBalance.add(availableBalance).setScale(2);
							dbProcessingFee = dbProcessingFee.add(processingFee).setScale(2);
							dbGst = dbGst.add(gst);

							Bson filter = new Document(FieldType.PAY_ID.getName(), payId);

							Bson newValue = new Document(FieldType.TOTAL_BALANCE.getName(), String.valueOf(dbBalance))
									.append(FieldType.UPDATE_DATE.getName(), dateToday)
									.append(FieldType.AVAILABLE_BALANCE.getName(), String.valueOf(dbAvailableBalance))
									.append(FieldType.PROCESSING_FEE.getName(), String.valueOf(dbProcessingFee))
									.append(FieldType.PG_GST.getName(), String.valueOf(dbGst));

							Bson updateOperationDocument = new Document("$set", newValue);
							collBalance.updateOne(filter, updateOperationDocument);
						} else {

							availableBalance = amount.multiply(payoutTotalAllowedLimitPercentage)
									.divide(new BigDecimal("100"));

							logger.info("availableBalance before fee deduction " + availableBalance);

							processingFee = amount.multiply(payoutProcessingFeePercentage).divide(new BigDecimal("100"))
									.setScale(2, BigDecimal.ROUND_HALF_UP);
							gst = processingFee.multiply(payoutGst).divide(new BigDecimal("100")).setScale(2,
									BigDecimal.ROUND_HALF_UP);

							availableBalance = availableBalance.subtract(processingFee).subtract(gst).setScale(2,
									BigDecimal.ROUND_HALF_UP);

							logger.info("total amount " + amount + " availableBalance after fee decution "
									+ availableBalance);

							Document document = new Document();

							document.put(FieldType.CREATE_DATE.getName(), dateToday);
							document.put(FieldType.TOTAL_BALANCE.getName(), doc.getString(FieldType.AMOUNT.getName()));
							document.put(FieldType.AVAILABLE_BALANCE.getName(),
									String.valueOf(availableBalance.setScale(2)));
							document.put(FieldType.PROCESSING_FEE.getName(), String.valueOf(processingFee));
							document.put(FieldType.PG_GST.getName(), String.valueOf(gst));
							document.put(FieldType.PAY_ID.getName(), payId);

							collBalance.insertOne(document);

						}
						cursor.close();

					}

				}

			}
		} catch (Exception e) {
			logger.info("exception while inserting in nodal Transaction ", e);
		}

	}

	/*
	 * @SuppressWarnings("static-access") public String getCardHash(Fields fields)
	 * throws SystemException {
	 * 
	 * if (StringUtils.isBlank(fields.get(FieldType.PAYMENT_TYPE.getName()))) {
	 * return null; }
	 * 
	 * if (!fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(
	 * PaymentType. CREDIT_CARD.getCode()) &&
	 * !fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase( PaymentType.
	 * DEBIT_CARD.getCode()) &&
	 * !fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase( PaymentType.
	 * PREPAID_CARD.getCode())) {
	 * 
	 * return null;
	 * 
	 * }
	 * 
	 * String cardHash = null;
	 * 
	 * try {
	 * 
	 * MongoDatabase dbIns = mongoInstance.getDB(); MongoCollection<Document> coll =
	 * dbIns .getCollection(propertiesManager.propertiesMap.get(prefix +
	 * Constants.COLLECTION_NAME.getValue()));
	 * 
	 * List<BasicDBObject> queryList = new ArrayList<BasicDBObject>(); BasicDBObject
	 * query = new BasicDBObject();
	 * 
	 * queryList .add(new BasicDBObject(FieldType.PG_REF_NUM.getName(),
	 * fields.get(FieldType.PG_REF_NUM.getName()))); queryList.add(new
	 * BasicDBObject(FieldType.PAY_ID.getName(),
	 * fields.get(FieldType.PAY_ID.getName())));
	 * 
	 * query.append("$and", queryList);
	 * 
	 * if (coll.count(query) > 0) {
	 * 
	 * MongoCursor<Document> cursor = coll.find(query).iterator(); while
	 * (cursor.hasNext()) {
	 * 
	 * Document doc = cursor.next();
	 * 
	 * if (StringUtils.isNotBlank(String.valueOf(doc.get(FieldType.H_CARD_NUMBER.
	 * getName())))) { cardHash =
	 * String.valueOf(doc.get(FieldType.H_CARD_NUMBER.getName())); break; } } }
	 * 
	 * return cardHash; } catch (Exception exception) {
	 * 
	 * logger.error("Exception while fetching card hash from previous fields ",
	 * exception); return cardHash; }
	 * 
	 * }
	 */

	@SuppressWarnings("static-access")
	public void insertIciciCibFields(Fields fields) throws SystemException {
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);

			BasicDBObject newFieldsObj = new BasicDBObject();

			for (String columnName : fields.keySet()) {
				newFieldsObj.put(columnName, fields.get(columnName));

			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(propertiesManager.propertiesMap
					.get(prefix + Constants.NODAL_SETTLEMENT_COLLECTION_NAME.getValue()));
			Document doc = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}

			// insertPayoutTransactionNodalTopupCollection(doc);

			// collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error while inserting CIB Response in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void saveSessionParam(Fields fields) throws SystemException {
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);
			String rfu1 = encryptDecryptService.encrypt(
					PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue()),
					fields.get(FieldType.CARD_NUMBER.getName()));
			String rfu2 = encryptDecryptService.encrypt(
					PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue()),
					fields.get(FieldType.CARD_EXP_DT.getName()));
			String rfu3 = encryptDecryptService.encrypt(
					PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue()),
					fields.get(FieldType.CVV.getName()));
			BasicDBObject newFieldsObj = new BasicDBObject();
			newFieldsObj.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
			newFieldsObj.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
			newFieldsObj.put(FieldType.CREATE_DATE.getName(), dateNow);
			newFieldsObj.put("RFU1", rfu1);
			newFieldsObj.put("RFU2", rfu2);
			newFieldsObj.put("RFU3", rfu3);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.SESSION_PARAM_COLLECTION_NAME.getValue()));
			Document doc = new Document(newFieldsObj);
			collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error while inserting session param in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void getSessionParam(Fields fields) {

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.SESSION_PARAM_COLLECTION_NAME.getValue()));

			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList
					.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName())));
			finalList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));

			BasicDBObject finalQuery = new BasicDBObject("$and", finalList);
			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				fields.put("RFU1",
						encryptDecryptService.decrypt(
								PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue()),
								dbobj.getString("RFU1")));
				fields.put("RFU2",
						encryptDecryptService.decrypt(
								PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue()),
								dbobj.getString("RFU2")));
				fields.put("RFU3",
						encryptDecryptService.decrypt(
								PropertiesManager.propertiesMap.get(Constants.SAVE_CARD_ADMIN_PAYID.getValue()),
								dbobj.getString("RFU3")));

			}
			cursor.close();
		} catch (Exception ex) {
			logger.error("Exception while get session param from DB", ex);
		}

	}

	public void deleteSessionParam(Fields fields) throws SystemException {
		try {

			BasicDBObject newFieldsObj = new BasicDBObject();
			newFieldsObj.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
			newFieldsObj.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.SESSION_PARAM_COLLECTION_NAME.getValue()));
			Document doc = new Document(newFieldsObj);
			collection.deleteOne(doc);
		} catch (Exception exception) {
			String message = "Error while delete session param in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void insertIciciCibBeneficiaryFields(Fields fields) throws SystemException {
		try {
			logger.info("benificiary Fields are " + fields.getFields());
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);

			BasicDBObject newFieldsObj = new BasicDBObject();

			for (String columnName : fields.keySet()) {

				newFieldsObj.put(columnName, fields.get(columnName));
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(propertiesManager.propertiesMap
					.get(prefix + Constants.NODAL_BENEFICIARY_COLLECTION_NAME.getValue()));
			Document doc = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			// collection.insertOne(doc);
			logger.info("benificiary Fields are saved in DB");
		} catch (Exception exception) {
			String message = "Error while inserting CIB Add Beneficiary Response in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void updateIciciDefaultBeneFields(Fields fields) throws SystemException {
		try {

			Bson filter;
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(propertiesManager.propertiesMap
					.get(prefix + Constants.NODAL_BENEFICIARY_COLLECTION_NAME.getValue()));
			if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.name()))) {
				filter = new Document(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.name()))
						.append(FieldType.BENE_DEFAULT.getName(), "true");
			} else {
				filter = new Document(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.name()))
						.append(FieldType.BENE_DEFAULT.getName(), "true");
			}

			Bson newValue = new Document(FieldType.BENE_DEFAULT.getName(), "false");

			Bson updateOperationDocument = new Document("$set", newValue);
			collection.updateOne(filter, updateOperationDocument);
		} catch (Exception exception) {
			String message = "Error while updating Beneficiary Response in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void updateIciciCibBeneficiaryFields(Fields fields) throws SystemException {
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(propertiesManager.propertiesMap
					.get(prefix + Constants.NODAL_BENEFICIARY_COLLECTION_NAME.getValue()));
			Bson filter = new Document(FieldType.STATUS.getName(), StatusType.PROCESSING.getName())
					.append(FieldType.BENE_ACCOUNT_NO.getName(), fields.get(FieldType.BENE_ACCOUNT_NO.getName()));
			Bson newValue;

			if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase("success")) {
				newValue = new Document(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()))
						.append(FieldType.UPDATE_DATE.getName(), dateNow)
						.append(FieldType.RESPONSE_MESSAGE.getName(), fields.get(FieldType.RESPONSE_MESSAGE.getName()))
						.append(FieldType.ACQ_ID.getName(), fields.get(FieldType.ACQ_ID.getName()));
			} else {
				newValue = new Document(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()))
						.append(FieldType.UPDATE_DATE.getName(), dateNow)
						.append(FieldType.RESPONSE_MESSAGE.getName(), fields.get(FieldType.RESPONSE_MESSAGE.getName()))
						.append(FieldType.ACQ_ID.getName(), fields.get(FieldType.ACQ_ID.getName()));
			}

			Bson updateOperationDocument = new Document("$set", newValue);
			collection.updateOne(filter, updateOperationDocument);
		} catch (Exception exception) {
			String message = "Error while updating Beneficiary Response in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void insertIMPSTransaction(Fields fields) throws SystemException {
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);

			BasicDBObject newFieldsObj = new BasicDBObject();

			for (String columnName : fields.keySet()) {
				newFieldsObj.put(columnName, fields.get(columnName));
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			Document doc = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			// collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error while inserting IMPS Response in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void insertDccTransaction(Fields fields) throws SystemException {
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);

			BasicDBObject newFieldsObj = new BasicDBObject();

			for (String columnName : fields.keySet()) {
				newFieldsObj.put(columnName, fields.get(columnName));
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.DCC_COLLECTION.getValue()));
			Document doc = new Document(newFieldsObj);
			collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error while inserting DCC Response in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void insertUpiQRRequest(Fields fields) throws SystemException {
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);

			BasicDBObject newFieldsObj = new BasicDBObject();

			for (String columnName : fields.keySet()) {
				newFieldsObj.put(columnName, fields.get(columnName));
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_QR_REQUEST_COLLECTION.getValue()));
			Document doc = new Document(newFieldsObj);
			collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error while inserting UPI QR Request in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void insertPgQRRequest(Fields fields) throws SystemException {
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);

			BasicDBObject newFieldsObj = new BasicDBObject();

			for (String columnName : fields.keySet()) {
				newFieldsObj.put(columnName, fields.get(columnName));
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.PG_QR_REQUEST_COLLECTION.getValue()));
			Document doc = new Document(newFieldsObj);
			collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error while inserting PG QR Request in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void updateIMPSTransactionStatus(Fields fields) throws SystemException {
		try {
			logger.info("IMPS Status Fields" + fields.getFields() + " txn Id" + fields.get(FieldType.TXN_ID.getName()));
			logger.info("IMPS Transaction Document update for txn Id " + fields.get(FieldType.TXN_ID.getName()));
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			Bson filter = new Document(FieldType.TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			Bson newValue = new Document(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()))
					.append(FieldType.UPDATE_DATE.getName(), dateNow)
					.append(FieldType.PG_TXN_MESSAGE.getName(), fields.get(FieldType.PG_TXN_MESSAGE.getName()))
					.append(FieldType.PG_RESP_CODE.getName(), fields.get(FieldType.PG_RESP_CODE.getName()));

			if (StringUtils.isNotBlank(fields.get(FieldType.RRN.getName())))
				((Document) newValue).append(FieldType.RRN.getName(), fields.get(FieldType.RRN.getName()));
			if (StringUtils.isNotBlank(fields.get(FieldType.UTR.getName())))
				((Document) newValue).append(FieldType.UTR.getName(), fields.get(FieldType.UTR.getName()));
			if (StringUtils.isNotBlank(fields.get(FieldType.ACQ_ID.getName())))
				((Document) newValue).append(FieldType.ACQ_ID.getName(), fields.get(FieldType.ACQ_ID.getName()));
			if (StringUtils.isNotBlank(fields.get(FieldType.COMMISSION_AMOUNT.getName())))
				((Document) newValue).append(FieldType.COMMISSION_AMOUNT.getName(),
						fields.get(FieldType.COMMISSION_AMOUNT.getName()));
			if (StringUtils.isNotBlank(fields.get(FieldType.SERVICE_TAX.getName())))
				((Document) newValue).append(FieldType.SERVICE_TAX.getName(),
						fields.get(FieldType.SERVICE_TAX.getName()));

			if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
				((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode())
						.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.name())
						.append(FieldType.IS_STATUS_FINAL.getName(), Constants.Y_FLAG.getValue());
			} else if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.DECLINED.name())) {
				((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.DECLINED.getCode())
						.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DECLINED.name())
						.append(FieldType.IS_STATUS_FINAL.getName(), Constants.Y_FLAG.getValue());
			} else if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.REJECTED.name())) {
				((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode())
						.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED.name())
						.append(FieldType.IS_STATUS_FINAL.getName(), Constants.Y_FLAG.getValue());
			} else if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.TIMEOUT.name())) {
				((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.TIMEOUT.getCode())
						.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TIMEOUT.name());
			} else if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PROCESSING.name())) {
				((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.PROCESSING.getCode())
						.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PROCESSING.name());
			} else {
				((Document) newValue)
						.append(FieldType.RESPONSE_CODE.getName(), fields.get(FieldType.RESPONSE_CODE.getName()))
						.append(FieldType.RESPONSE_MESSAGE.getName(), fields.get(FieldType.RESPONSE_MESSAGE.getName()))
						.append(FieldType.IS_STATUS_FINAL.getName(), Constants.Y_FLAG.getValue());
			}

			// logger.info("Filter Query " + filter.toString() + " updated Query
			// is " + newValue.toString());

			Bson updateOperationDocument = new Document("$set", newValue);
			collection.updateOne(filter, updateOperationDocument);
			logger.info("IMPS Transaction Document updated for txn Id " + fields.get(FieldType.TXN_ID.getName()));
		} catch (Exception exception) {
			String message = "Error while inserting IMPS Response in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void insertStatusEnqTransaction(Fields fields) throws SystemException {
		try {
			MongoDatabase dbIns = null;
			BasicDBObject newFieldsObj = new BasicDBObject();

			String amountString = fields.get(FieldType.AMOUNT.getName());
			String surchargeAmountString = fields.get(FieldType.SURCHARGE_AMOUNT.getName());
			String currencyString = fields.get(FieldType.CURRENCY_CODE.getName());
			String totalAmountString = fields.get(FieldType.TOTAL_AMOUNT.getName());

			String amount = "0";
			if (!StringUtils.isEmpty(amountString) && !StringUtils.isEmpty(currencyString)) {
				amount = Amount.toDecimal(amountString, currencyString);
				newFieldsObj.put(FieldType.AMOUNT.getName(), amount);
			}

			String totalAmount = "0";
			if (!StringUtils.isEmpty(totalAmountString) && !StringUtils.isEmpty(currencyString)) {
				totalAmount = Amount.toDecimal(totalAmountString, currencyString);
				newFieldsObj.put(FieldType.TOTAL_AMOUNT.getName(), totalAmount);
			}

			String surchargeAmount = "0";
			if (!StringUtils.isEmpty(surchargeAmountString) && !StringUtils.isEmpty(currencyString)) {
				surchargeAmount = Amount.toDecimal(surchargeAmountString, currencyString);
				newFieldsObj.put(FieldType.SURCHARGE_AMOUNT.getName(), surchargeAmount);
			}
			String origTxnId = "0";
			String origTxnStr = fields.get(FieldType.ORIG_TXN_ID.getName());
			if (StringUtils.isEmpty(origTxnStr)) {
				String internalOrigTxnStr = fields.get(FieldType.INTERNAL_ORIG_TXN_ID.getName());
				if (StringUtils.isEmpty(internalOrigTxnStr)) {
					newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(), origTxnId);
				}
				if (!StringUtils.isEmpty(internalOrigTxnStr)) {
					newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(), internalOrigTxnStr);
				}
			}

			if (!StringUtils.isEmpty(origTxnStr)) {
				newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(), origTxnStr);
			}

			String origTxnType = fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
			if (!StringUtils.isEmpty(origTxnType)) {
				String txnType = fields.get(FieldType.TXNTYPE.getName());
				if ((txnType.equals(TransactionType.REFUND.getName()))
						|| (txnType.equals(TransactionType.REFUNDRECO.getName()))) {
					newFieldsObj.put(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName());
				} else {
					newFieldsObj.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
				}

			}
			String pgRefNo = "0";
			String pgRefNum = fields.get(FieldType.PG_REF_NUM.getName());
			if (StringUtils.isEmpty(pgRefNum)) {
				newFieldsObj.put(FieldType.PG_REF_NUM.getName(), pgRefNo);
			}

			if (!StringUtils.isEmpty(pgRefNum)) {
				newFieldsObj.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
			}
			String acctId = "0";
			String acctIdStr = fields.get(FieldType.ACCT_ID.getName());
			if (acctIdStr != null && acctIdStr.length() > 0) {

				newFieldsObj.put(FieldType.ACCT_ID.getName(), acctIdStr);
			}
			if (acctIdStr == null) {
				newFieldsObj.put(FieldType.ACCT_ID.getName(), acctId);
			}
			String acqId = "0";
			String acqIdStr = fields.get(FieldType.ACQ_ID.getName());
			if (acqIdStr != null && acqIdStr.length() > 0) {
				newFieldsObj.put(FieldType.ACQ_ID.getName(), acqIdStr);
			}
			if (acqIdStr == null) {
				newFieldsObj.put(FieldType.ACQ_ID.getName(), acqId);
			}
			String oid = fields.get(FieldType.OID.getName());
			String longOid = "0";
			if (!StringUtils.isEmpty(oid)) {

				newFieldsObj.put(FieldType.OID.getName(), oid);
			}
			if (StringUtils.isEmpty(oid)) {
				newFieldsObj.put(FieldType.OID.getName(), longOid);
			}
			String udf1 = fields.get(FieldType.UDF1.getName());
			if (!StringUtils.isEmpty(udf1)) {
				newFieldsObj.put(FieldType.UDF1.getName(), udf1);
			}
			String udf2 = fields.get(FieldType.UDF2.getName());
			if (!StringUtils.isEmpty(udf2)) {
				newFieldsObj.put(FieldType.UDF2.getName(), udf2);
			}
			String udf3 = fields.get(FieldType.UDF3.getName());
			if (!StringUtils.isEmpty(udf3)) {
				newFieldsObj.put(FieldType.UDF3.getName(), udf3);
			}
			String udf4 = fields.get(FieldType.UDF4.getName());
			if (!StringUtils.isEmpty(udf4)) {
				newFieldsObj.put(FieldType.UDF4.getName(), udf4);
			}
			String udf5 = fields.get(FieldType.UDF5.getName());
			if (!StringUtils.isEmpty(udf5)) {
				newFieldsObj.put(FieldType.UDF5.getName(), udf5);
			}
			String udf6 = fields.get(FieldType.UDF6.getName());
			if (!StringUtils.isEmpty(udf6)) {
				newFieldsObj.put(FieldType.UDF6.getName(), udf6);
			}

			String paymentsRegion = fields.get(FieldType.PAYMENTS_REGION.getName());
			if (!StringUtils.isEmpty(paymentsRegion)) {
				newFieldsObj.put(FieldType.PAYMENTS_REGION.getName(), paymentsRegion);
			}

			String cardHolderType = fields.get(FieldType.CARD_HOLDER_TYPE.getName());
			if (!StringUtils.isEmpty(cardHolderType)) {
				newFieldsObj.put(FieldType.CARD_HOLDER_TYPE.getName(), cardHolderType);
			}

			String requestDate = fields.get(FieldType.REQUEST_DATE.getName());
			if (!StringUtils.isEmpty(requestDate)) {
				newFieldsObj.put(FieldType.REQUEST_DATE.getName(), requestDate);
			}

			String saleAmoiunt = fields.get(FieldType.SALE_AMOUNT.getName());
			if ((!StringUtils.isEmpty(saleAmoiunt) && !StringUtils.isEmpty(currencyString))) {
				saleAmoiunt = Amount.toDecimal(saleAmoiunt, currencyString);
				newFieldsObj.put(FieldType.SALE_AMOUNT.getName(), saleAmoiunt);
			}

			String totalSaleAmoiunt = fields.get(FieldType.SALE_TOTAL_AMOUNT.getName());
			if ((!StringUtils.isEmpty(totalSaleAmoiunt) && !StringUtils.isEmpty(currencyString))) {
				totalSaleAmoiunt = Amount.toDecimal(totalSaleAmoiunt, currencyString);
				newFieldsObj.put(FieldType.SALE_TOTAL_AMOUNT.getName(), totalSaleAmoiunt);
			}

			for (int i = 0; i < fields.size(); i++) {

				Date dNow = new Date();
				String dateNow = DateCreater.formatDateForDb(dNow);

				// Set create date as pg date time received in response
				String pgDateTime = "";
				if (!StringUtils.isEmpty(fields.get(FieldType.PG_DATE_TIME.getName()))) {
					pgDateTime = fields.get(FieldType.PG_DATE_TIME.getName());
				}
				;

				String createDate = "";
				if (!StringUtils.isEmpty(pgDateTime) && pgDateTime.length() == 19) {
					String pgDate = pgDateTime.substring(0, 10);
					String pgTime = "23:59:59";
					createDate = pgDate.replace(":", "-") + " " + pgTime;
				}

				if (!StringUtils.isEmpty(createDate)) {
					dateNow = createDate;
				} else {
					dateNow = DateCreater.formatDateForDb(dNow);
				}
				for (String columnName : aLLDB_Fields) {

					if (columnName.equals(FieldType.CREATE_DATE.getName())) {
						newFieldsObj.put(columnName, dateNow);
					} else if (columnName.equals(FieldType.DATE_INDEX.getName())) {
						newFieldsObj.put(columnName, dateNow.substring(0, 10).replace("-", ""));
					} else if (columnName.equals(FieldType.UPDATE_DATE.getName())) {
						newFieldsObj.put(columnName, dateNow);
					} else if (columnName.equals("_id")) {
						newFieldsObj.put(columnName, fields.get(FieldType.TXN_ID.getName()));
					} else if (columnName.equals(FieldType.INSERTION_DATE.getName())) {
						newFieldsObj.put(columnName, dNow);
					} else if (columnName.equals(FieldType.AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.TOTAL_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.SURCHARGE_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ORIG_TXN_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.PG_REF_NUM.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ACCT_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ACQ_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.OID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF1.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF2.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF3.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF4.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF5.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF6.getName())) {
						continue;
					} else if (columnName.equals(FieldType.PAYMENTS_REGION.getName())) {
						continue;
					} else if (columnName.equals(FieldType.CARD_HOLDER_TYPE.getName())) {
						continue;
					} else if (columnName.equals(FieldType.SALE_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.SALE_TOTAL_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ORIG_TXNTYPE.getName())) {
						continue;
					} else {
						newFieldsObj.put(columnName, fields.get(columnName));
					}
				}
			}

			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			Document doc = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			// collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void insertSettlementTransaction(Fields fields) throws SystemException {
		try {
			MongoDatabase dbIns = null;
			BasicDBObject newFieldsObj = new BasicDBObject();

			String amountString = fields.get(FieldType.AMOUNT.getName());
			String currencyString = fields.get(FieldType.CURRENCY_CODE.getName());

			String amount = "0";
			if (!StringUtils.isEmpty(amountString) && !StringUtils.isEmpty(currencyString)) {
				amount = Amount.toDecimal(amountString, currencyString);
				newFieldsObj.put(FieldType.AMOUNT.getName(), amount);
			}

			String txnId = "0";
			String transId = fields.get(FieldType.TXN_ID.getName());
			if (StringUtils.isEmpty(transId)) {
				newFieldsObj.put(FieldType.TXN_ID.getName(), txnId);
			}

			if (!StringUtils.isEmpty(transId)) {
				newFieldsObj.put(FieldType.TXN_ID.getName(), transId);
			}

			String rrn = fields.get(FieldType.RRN.getName());
			if (!StringUtils.isEmpty(rrn)) {
				newFieldsObj.put(FieldType.RRN.getName(), rrn);
			}

			String acquirerType = fields.get(FieldType.NODAL_ACQUIRER.getName());
			if (!StringUtils.isEmpty(acquirerType)) {
				newFieldsObj.put(FieldType.ACQUIRER_TYPE.getName(), acquirerType);
			}

			String txnType = fields.get(FieldType.TXNTYPE.getName());
			if (!StringUtils.isEmpty(txnType)) {
				newFieldsObj.put(FieldType.TXNTYPE.getName(), txnType);
			}
			String requestBy = fields.get(FieldType.REQUESTED_BY.getName());
			if (!StringUtils.isEmpty(requestBy)) {
				newFieldsObj.put(FieldType.REQUESTED_BY.getName(), requestBy);
			}
			String oId = "0";
			String orId = fields.get(FieldType.OID.getName());
			if (StringUtils.isEmpty(orId)) {
				newFieldsObj.put(FieldType.OID.getName(), oId);
			}
			if (!StringUtils.isEmpty(orId)) {
				newFieldsObj.put(FieldType.OID.getName(), orId);
			}
			String customerId = fields.get(FieldType.CUSTOMER_ID.getName());
			if (!StringUtils.isEmpty(customerId)) {
				newFieldsObj.put(FieldType.CUSTOMER_ID.getName(), customerId);
			}
			String appId = fields.get(FieldType.APP_ID.getName());
			if (!StringUtils.isEmpty(appId)) {
				newFieldsObj.put(FieldType.APP_ID.getName(), appId);
			}
			String srcAccountNo = fields.get(FieldType.SRC_ACCOUNT_NO.getName());
			if (!StringUtils.isEmpty(srcAccountNo)) {
				newFieldsObj.put(FieldType.SRC_ACCOUNT_NO.getName(), srcAccountNo);
			}
			String beneCode = fields.get(FieldType.BENEFICIARY_CD.getName());
			if (!StringUtils.isEmpty(beneCode)) {
				newFieldsObj.put(FieldType.BENEFICIARY_CD.getName(), beneCode);
			}
			String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
			if (!StringUtils.isEmpty(paymentType)) {
				newFieldsObj.put(FieldType.PAYMENT_TYPE.getName(), paymentType);
			}
			String beneAcc = fields.get(FieldType.BENE_ACCOUNT_NO.getName());
			if (!StringUtils.isEmpty(beneAcc)) {
				newFieldsObj.put(FieldType.BENE_ACCOUNT_NO.getName(), beneAcc);
			}
			String beneName = fields.get(FieldType.BENE_NAME.getName());
			if (!StringUtils.isEmpty(beneName)) {
				newFieldsObj.put(FieldType.BENE_NAME.getName(), beneName);
			}
			for (int i = 0; i < fields.size(); i++) {

				Date dNow = new Date();
				String dateNow = DateCreater.formatDateForDb(dNow);
				for (String columnName : aLLDB_Fields) {

					if (columnName.equals(FieldType.CREATE_DATE.getName())) {
						newFieldsObj.put(columnName, dateNow);
					} else if (columnName.equals(FieldType.DATE_INDEX.getName())) {
						newFieldsObj.put(columnName, dateNow.substring(0, 10).replace("-", ""));
					} else if (columnName.equals(FieldType.UPDATE_DATE.getName())) {
						newFieldsObj.put(columnName, dateNow);
					} else if (columnName.equals("_id")) {
						newFieldsObj.put(columnName, fields.get(FieldType.TXN_ID.getName()));
					} else if (columnName.equals(FieldType.INSERTION_DATE.getName())) {
						newFieldsObj.put(columnName, dNow);
					} else if (columnName.equals(FieldType.AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ORIG_TXN_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.PG_REF_NUM.getName())) {
						continue;
					} else if (columnName.equals(FieldType.OID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.RRN.getName())) {
						continue;
					} else if (columnName.equals(FieldType.TXNTYPE.getName())) {
						continue;
					} else if (columnName.equals(FieldType.CUSTOMER_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.APP_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ACQUIRER_TYPE.getName())) {
						continue;
					} else if (columnName.equals(FieldType.REQUESTED_BY.getName())) {
						continue;
					} else if (columnName.equals(FieldType.PAYMENT_TYPE.getName())) {
						continue;
					} else if (columnName.equals(FieldType.SRC_ACCOUNT_NO.getName())) {
						continue;
					} else if (columnName.equals(FieldType.BENEFICIARY_CD.getName())) {
						continue;
					} else if (columnName.equals(FieldType.BENE_NAME.getName())) {
						continue;
					} else if (columnName.equals(FieldType.BENE_ACCOUNT_NO.getName())) {
						continue;
					} else {
						newFieldsObj.put(columnName, fields.get(columnName));
					}
				}
			}
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.SETTLEMENT_COLLECTION_NAME.getValue()));
			Document doc = new Document(newFieldsObj);
			collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("unused")
	private byte[] getInternalRequestFields(Fields fields) {
		String internalReqFields = fields.get(FieldType.INTERNAL_REQUEST_FIELDS.getName());

		if (null != internalReqFields) {
			byte[] allFields = Base64.encodeBase64(internalReqFields.getBytes());
			return allFields;
		} else {
			return null;
		}
	}

	public void insertCustomerInfo(Fields fields) throws SystemException {
		try {
			// Return for invalid transaction
			String responseCode = fields.get(FieldType.RESPONSE_CODE.getName());
			if (responseCode != null && responseCode.equals(ErrorType.VALIDATION_FAILED.getCode())) {
				return;
			}
			// Return for invalid transaction (Hash invalid)
			String invalidHash = fields.get(FieldType.INTERNAL_VALIDATE_HASH_YN.getName());
			if (null != invalidHash && invalidHash.equals("Y")) {
				return;
			}

			boolean custInfoPresent = false;// if not a new order then return
			// TODO.......
			// if fields do not contain any shipping/billing information then
			// return
			if (!fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.NEWORDER.getName())) {
				return;
			}

			Set<String> fieldsKeySet = fields.keySet();
			for (String fieldName : fieldsKeySet) {
				if (fieldName.startsWith("CUST_SHIP") || fieldName.startsWith("CUST_S")
						|| fieldName.equals("CUST_ID")) {
					custInfoPresent = true;
				}
			}
			if (custInfoPresent) {
				// execute query
				insertCustomerInfoQuery(fields);
			}
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void insertPaymentRequest(Fields fields) throws SystemException {
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TXN_REQUEST_COLLECTION.getValue()));

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			Document doc = new Document();

			doc.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
			doc.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			doc.put(FieldType.AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
			doc.put(FieldType.CUST_NAME.getName(), fields.get(FieldType.CUST_NAME.getName()));
			doc.put(FieldType.TERMINAL_ID.getName(), fields.get(FieldType.TERMINAL_ID.getName()));
			doc.put(FieldType.SERVICE_ID.getName(), fields.get(FieldType.SERVICE_ID.getName()));
			doc.put(FieldType.STATUS.getName(), TDRStatus.ACTIVE.getName());
			doc.put(FieldType.CREATE_DATE.getName(), dateNow);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				coll.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				coll.insertOne(doc);
			}
			// coll.insertOne(doc);

		} catch (Exception exception) {
			String message = "Error while inserting new payment request in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void updatePaymentRequest(Fields fields) throws SystemException {

		// Only for Punjab Pay POS solution
		if (StringUtils.isNotBlank(fields.get(FieldType.TERMINAL_ID.getName()))) {

			try {

				logger.info("Inside updatePaymentRequest to deactive payment request ");
				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.TXN_REQUEST_COLLECTION.getValue()));

				List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
				BasicDBObject query = new BasicDBObject();

				queryList
						.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));
				queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
				queryList.add(new BasicDBObject(FieldType.TERMINAL_ID.getName(),
						fields.get(FieldType.TERMINAL_ID.getName())));
				queryList.add(new BasicDBObject(FieldType.STATUS.getName(), TDRStatus.ACTIVE.getName()));

				query.append("$and", queryList);

				logger.info("Query to deactive payment request == " + query);
				if (coll.count(query) > 0) {

					query.append("$and", queryList);
					Document setData = new Document();
					setData.append(FieldType.STATUS.getName(), TDRStatus.INACTIVE.getName());

					Document update = new Document();
					update.append("$set", setData);

					coll.updateMany(query, update);

					logger.info("Updated status inactive for payment request with order Id "
							+ fields.get(FieldType.ORDER_ID.getName()));
				}

			} catch (Exception exception) {

				logger.error("Exception while updating status for transaction requests ", exception);
			}

		}

	}

	@SuppressWarnings("static-access")
	public void insertTransactionHash(Fields fields) throws SystemException {

		try {

			// Get Hash From Previous fields

			// Add hash to this transaction

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TXN_HASH_COLLECTION.getValue()));

			// Document doc = new Document();

			// doc.append("CARD_HASH", sessionMap.get);

			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			BasicDBObject query = new BasicDBObject();

			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));
			queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
			queryList.add(
					new BasicDBObject(FieldType.TERMINAL_ID.getName(), fields.get(FieldType.TERMINAL_ID.getName())));
			queryList.add(new BasicDBObject(FieldType.STATUS.getName(), TDRStatus.ACTIVE.getName()));

			query.append("$and", queryList);

			logger.info("Query to deactive payment request == " + query);
			if (coll.count(query) > 0) {

				query.append("$and", queryList);
				Document setData = new Document();
				setData.append(FieldType.STATUS.getName(), TDRStatus.INACTIVE.getName());

				Document update = new Document();
				update.append("$set", setData);

				coll.updateMany(query, update);

				logger.info("Updated status inactive for payment request with order Id "
						+ fields.get(FieldType.ORDER_ID.getName()));
			}

		} catch (Exception exception) {

			logger.error("Exception while updating status for transaction requests ", exception);
		}
	}

	@SuppressWarnings("incomplete-switch")
	public void updateNewOrder(Fields fields) throws SystemException {
		try {
			String internalOrigTxnId = fields.get(FieldType.INTERNAL_ORIG_TXN_ID.getName());
			String status = fields.get(FieldType.STATUS.getName());
			if (StringUtils.isEmpty(internalOrigTxnId) || StringUtils.isEmpty(status)) {
				return;
			}

			TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
			switch (transactionType) {
			case SALE:
				updateNewOrderDetails(fields);
				break;
			case AUTHORISE:
				updateNewOrderDetails(fields);
				break;
			case ENROLL:
				updateNewOrderDetails(fields);
				break;
			}
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public void insert(Fields fields) throws SystemException {
		try {

			if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())
					&& fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {

				// Validate if capture entry already present for the pg Ref Num

				if (validateCapturedPgRef(fields.get(FieldType.PG_REF_NUM.getName()))) {
					return;
				}

				routerConfigurationService.clearFailCount(fields);
			}

			insertTransaction(fields);
			insertCustomerInfo(fields);
			// updateNewOrder(fields);
		} catch (MongoException exception) {
			// MDC.put("MongoException", exception.toString());
			logger.error("MongoException", exception);
		}
	}

	public void insertCustomerInfoQuery(Fields fields) throws SystemException {
		// getCustInfoFields
		try {
			BasicDBObject newFieldsObj = new BasicDBObject();
			newFieldsObj.put("_id", fields.get(FieldType.TXN_ID.getName()));

			Collection<String> aLLDB_Fields = SystemProperties.getCustInfoFields();
			for (String columnName : aLLDB_Fields) {
				newFieldsObj.put(columnName, fields.get(columnName));
			}
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(Constants.BILLING_COLLECTION.getValue());
			Document doc = new Document(newFieldsObj);
			collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

	}

	public CustomerAddress fetchCustomerInfo(String orderId) {
		// getCustInfoFields

		CustomerAddress customerAddress = new CustomerAddress();
		try {

			BasicDBObject field = new BasicDBObject();
			field.put(FieldType.ORDER_ID.getName(), orderId);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(Constants.BILLING_COLLECTION.getValue());
			MongoCursor<Document> cursor = coll.find(field).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				customerAddress.setCustName(dbobj.getString(FieldType.CUST_NAME.getName()));
				customerAddress.setCustPhone(dbobj.getString(FieldType.CUST_PHONE.getName()));
				customerAddress.setCustStreetAddress1(dbobj.getString(FieldType.CUST_STREET_ADDRESS1.getName()));
				customerAddress.setCustStreetAddress2(dbobj.getString(FieldType.CUST_STREET_ADDRESS2.getName()));
				customerAddress.setCustCity(dbobj.getString(FieldType.CUST_CITY.getName()));
				customerAddress.setCustState(dbobj.getString(FieldType.CUST_STATE.getName()));
				customerAddress.setCustCountry(dbobj.getString(FieldType.CUST_COUNTRY.getName()));
				customerAddress.setCustZip(dbobj.getString(FieldType.CUST_ZIP.getName()));
				customerAddress.setCustStreetAddress1(dbobj.getString(FieldType.CUST_SHIP_STREET_ADDRESS1.getName()));
				customerAddress.setCustStreetAddress2(dbobj.getString(FieldType.CUST_SHIP_STREET_ADDRESS2.getName()));
				customerAddress.setCustCity(dbobj.getString(FieldType.CUST_SHIP_CITY.getName()));
				customerAddress.setCustState(dbobj.getString(FieldType.CUST_SHIP_STATE.getName()));
				customerAddress.setCustCountry(dbobj.getString(FieldType.CUST_SHIP_COUNTRY.getName()));
				customerAddress.setCustShipZip(dbobj.getString(FieldType.CUST_SHIP_ZIP.getName()));
				customerAddress.setDurationFrom(dbobj.getString(FieldType.DURATION_FROM.getName()));
				customerAddress.setDurationTo(dbobj.getString(FieldType.DURATION_TO.getName()));
			}
			return customerAddress;
		} catch (Exception exception) {
			logger.error("exception ", exception);
		}
		return customerAddress;

	}

	/*
	 * public void insertCustomerInfoQuery(Fields fields) throws SystemException {
	 * try { BasicDBObject newFieldsObj = new BasicDBObject(); for (int i = 0; i <
	 * fields.size(); i++) { Collection<String> aLLDB_Fields =
	 * SystemProperties.getDBFields(); for (String columnName : aLLDB_Fields) {
	 * newFieldsObj.put(columnName, fields.get(columnName)); } } MongoDatabase dbIns
	 * = mongoInstance.getDB(); MongoCollection<Document> collection =
	 * dbIns.getCollection(Constants.COLLECTION_NAME.getValue()); Document doc = new
	 * Document(newFieldsObj); collection.insertOne(doc); } catch (Exception
	 * exception) { // MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), //
	 * fields.getCustomMDC()); String message =
	 * "Error while inserting transaction in database"; logger.error(message,
	 * exception); throw new SystemException(ErrorType.DATABASE_ERROR, exception,
	 * message); }
	 * 
	 * }
	 */

	@SuppressWarnings({ "static-access", "unused" })
	public void insertNewOrder(Fields fields) throws SystemException {
		try {
			String amountString = fields.get(FieldType.AMOUNT.getName());
			String surchargeAmountString = fields.get(FieldType.SURCHARGE_AMOUNT.getName());
			String currencyString = fields.get(FieldType.CURRENCY_CODE.getName());

			String amount = "0";
			if (!StringUtils.isEmpty(amountString) && !StringUtils.isEmpty(currencyString)) {
				amount = Amount.toDecimal(amountString, currencyString);
				fields.put(FieldType.AMOUNT.getName(), amount);
			}

			String surchargeAmount = "0";
			if (!StringUtils.isEmpty(surchargeAmountString) && !StringUtils.isEmpty(currencyString)) {
				surchargeAmount = Amount.toDecimal(surchargeAmountString, currencyString);
				fields.put(FieldType.SURCHARGE_AMOUNT.getName(), surchargeAmount);
			}
			long origTxnId = 0;
			String origTxnStr = fields.get(FieldType.ORIG_TXN_ID.getName());
			if (StringUtils.isEmpty(origTxnStr)) {
				origTxnStr = fields.get(FieldType.INTERNAL_ORIG_TXN_ID.getName());
				fields.put(FieldType.ORIG_TXN_ID.getName(), origTxnStr);
			}
			if (!StringUtils.isEmpty(origTxnStr)) {
				origTxnId = Long.parseLong(origTxnStr);
				// fields.put(FieldType.ORIG_TXN_ID.getName(),origTxnId);
			}
			long acctId = 0;
			String acctIdStr = fields.get(FieldType.ACCT_ID.getName());
			if (acctIdStr != null && acctIdStr.length() > 0) {
				acctId = Long.parseLong(acctIdStr);
				// fields.put(FieldType.ACCT_ID.getName(),acctId);
			}
			String oid = fields.get(FieldType.OID.getName());
			long longOid = 0;
			if (!StringUtils.isEmpty(oid)) {
				longOid = Long.parseLong(oid);
			}
			BasicDBObject newFieldsObj = new BasicDBObject();
			for (int i = 0; i < fields.size(); i++) {
				Collection<String> aLLDB_Fields = SystemProperties.getAllDBRequestFields();
				for (String columnName : aLLDB_Fields) {
					newFieldsObj.put(columnName, fields.get(columnName));
				}
			}
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			Document doc = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			// collection.insertOne(doc);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	private List<Fields> getPreviousSaleCapturedForPgRefNum(String pgRefNum) throws SystemException {
		try {
			logger.info("Inside getPreviousSaleCapturedForPgRefNum (PG_REF_NUM): " + pgRefNum);
			List<Fields> fieldsList = new ArrayList<Fields>();

			List<BasicDBObject> settledList = new ArrayList<BasicDBObject>();
			settledList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			settledList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			settledList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			BasicDBObject saleQuery = new BasicDBObject("$and", settledList);

			/*
			 * List<BasicDBObject> transList = new ArrayList<BasicDBObject>();
			 * transList.add(saleQuery);
			 * 
			 * BasicDBObject transQuery = new BasicDBObject("$or", transList);
			 */
			logger.info("Query Created");
			MongoDatabase dbIns = mongoInstance.getDB();
			logger.info("Below MongoDatabase dbIns = mongoInstance.getDB()");
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(saleQuery).iterator();
			logger.info("Query Executed");
			try {
				while (cursor.hasNext()) {
					Document documentObj = (Document) cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						documentObj = dataEncDecTool.decryptDocument(documentObj);
					}

					if (null != documentObj) {
						Fields preFields = new Fields();
						for (int j = 0; j < documentObj.size(); j++) {
							for (String columnName : systemProperties.getDBFields()) {
								if (documentObj.get(columnName) != null) {
									preFields.put(columnName, documentObj.get(columnName).toString());
								}
							}
						}
						preFields.logAllFields(
								"Received SALE&CAPTURED transaction details for transaction with PG_REF_NUM: "
										+ pgRefNum);
						fieldsList.add(preFields);
					}
				}
				logger.info("Got Previous Data with count : " + fieldsList.size());
			} finally {
				cursor.close();
			}

			return fieldsList;
		} catch (Exception exception) {
			String message = "Error while reading list of transactions based on OID from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public List<Fields> getPreviousSaleCapturedForOrderId(String orderId) throws SystemException {
		try {
			logger.info("Inside getPreviousSaleCapturedForPgRefNum (PG_REF_NUM): " + orderId);
			List<Fields> fieldsList = new ArrayList<Fields>();

			List<BasicDBObject> settledList = new ArrayList<BasicDBObject>();
			settledList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			settledList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			settledList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			BasicDBObject saleQuery = new BasicDBObject("$and", settledList);

			/*
			 * List<BasicDBObject> transList = new ArrayList<BasicDBObject>();
			 * transList.add(saleQuery);
			 * 
			 * BasicDBObject transQuery = new BasicDBObject("$or", transList);
			 */
			logger.info("Query Created");
			MongoDatabase dbIns = mongoInstance.getDB();
			logger.info("Below MongoDatabase dbIns = mongoInstance.getDB()");
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(saleQuery).iterator();
			logger.info("Query Executed");
			try {
				while (cursor.hasNext()) {
					Document documentObj = (Document) cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						documentObj = dataEncDecTool.decryptDocument(documentObj);
					}

					if (null != documentObj) {
						Fields preFields = new Fields();
						for (int j = 0; j < documentObj.size(); j++) {
							for (String columnName : systemProperties.getDBFields()) {
								if (documentObj.get(columnName) != null) {
									preFields.put(columnName, documentObj.get(columnName).toString());
								}
							}
						}
						preFields.logAllFields(
								"Received SALE&CAPTURED transaction details for transaction with PG_REF_NUM: "
										+ orderId);
						fieldsList.add(preFields);
					}
				}
				logger.info("Got Previous Data with count : " + fieldsList.size());
			} finally {
				cursor.close();
			}

			return fieldsList;
		} catch (Exception exception) {
			String message = "Error while reading list of transactions based on OID from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	// Get Previous Sale Date
	public String getSaleDate(String pgRefNum) throws SystemException {
		List<Fields> fieldsList = new ArrayList<Fields>();
		fieldsList = getPreviousSaleCapturedForPgRefNum(pgRefNum);
		if (fieldsList.size() > 0) {
			return fieldsList.get(0).get(FieldType.CREATE_DATE.getName());
		}
		return null;
	}

	public String getSaleDateByOrderId(String orderId) throws SystemException {
		List<Fields> fieldsList = new ArrayList<Fields>();
		fieldsList = getPreviousSaleCapturedForOrderId(orderId);
		if (fieldsList.size() > 0) {
			return fieldsList.get(0).get(FieldType.CREATE_DATE.getName());
		}
		return null;
	}

	public Fields getFieldsForSettlement(String oId, String status) throws SystemException {
		try {
			return createAllSelectForSettlement(oId, status);
		} catch (Exception exception) {
			String message = "Error while searching transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public Fields createAllSelectForSettlement(String oId, String status) {

		Fields fields = new Fields();
		// PropertiesManager propManager = new PropertiesManager();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		// List<BasicDBObject> txnTypeConditionLst = new
		// ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.TXN_ID.getName(), oId));
		dbObjList.add(new BasicDBObject(FieldType.STATUS.getName(), status));

		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				propertiesManager.propertiesMap.get(prefix + Constants.SETTLEMENT_COLLECTION_NAME.getValue()));

		FindIterable<Document> cursor = coll.find(andQuery);

		// MongoCursor<Document> cursor2 = cursor.iterator();

		if (cursor.iterator().hasNext()) {
			Document documentObj = cursor.iterator().next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}

					}
				}
			}
			fields.logAllFields("Previous fields");
		}
		cursor.iterator().close();

		return fields;

	}

	public Fields getFieldsForIdfcUpi(String pgRefNum, String txnType, String status) throws SystemException {
		try {
			return createAllForidfcUpiSale(pgRefNum, txnType, status);
		} catch (Exception exception) {
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public String getECollectionDuplicateResponse(Fields fields) throws SystemException {

		String duplicacyResponse = "";
		try {
			MongoDatabase dbIns = null;

			BasicDBObject query = new BasicDBObject(FieldType.RRN.getName(), fields.get(FieldType.RRN.getName()));
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.E_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			if (cursor.hasNext()) {
				duplicacyResponse = "Y";
			} else {
				duplicacyResponse = "N";
			}
			cursor.close();
		} catch (Exception exception) {
			String message = "Error while Checking Duplicacy in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
		return duplicacyResponse;
	}

	@SuppressWarnings({ "static-access", "unused" })
	public void insertECollectionResponse(Fields fields) throws SystemException {
		logger.info("Insert in insertECollectionResponse(), FieldsDao");
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);

			BasicDBObject newFieldsObj = new BasicDBObject();
			for (String columnName : fields.keySet()) {
				newFieldsObj.put(columnName, fields.get(columnName));
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.E_COLLECTION.getValue()));
			Document doc = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			// collection.insertOne(doc);

			MongoDatabase dbIns1 = mongoInstance.getDB();
			MongoCollection<Document> collection1 = dbIns1.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
			BasicDBObject newFieldsObj1 = new BasicDBObject();

			SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String dtDate = dateFormat.format(inputDate.parse(dateNow));

			DateFormat dateFormatIndex = new SimpleDateFormat("yyyyMMdd");
			String todaysDateIndex = dateFormatIndex.format(dNow.getTime());

			String dateFrom = dtDate + " " + "00:00:00";
			String toDate = dtDate + " " + "23:59:59";
			newFieldsObj1.append(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());
			newFieldsObj1.append(FieldType.DATE_INDEX.getName(), todaysDateIndex);
			if (fields.contains(FieldType.SUB_MERCHANT_ID.getName())) {
				newFieldsObj1.append(FieldType.SUB_MERCHANT_ID.getName(),
						fields.get(FieldType.SUB_MERCHANT_ID.getName()));
			} else {
				newFieldsObj1.append(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			}
			MongoCursor<Document> cursor = collection1.find(newFieldsObj1).iterator();
			BigDecimal openingAmount = new BigDecimal("0").setScale(2);
			BigDecimal closingAmount = new BigDecimal("0").setScale(2);
			BigDecimal eCollectionAmount = new BigDecimal("0").setScale(2);
			BigDecimal creditAmount = new BigDecimal("0").setScale(2);
			BigDecimal debitAmount = new BigDecimal("0").setScale(2);
			boolean flag = false;
			while (cursor.hasNext()) {
				flag = true;
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				logger.info("Orginal data for Ecollection by Closing Collection for virtual account code = "
						+ dbobj.get("VIRTUAL_AC_CODE").toString() + " , " + dbobj);
				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				eCollectionAmount = new BigDecimal(fields.get(FieldType.AMOUNT.getName())).setScale(2);
				creditAmount = eCollectionAmount
						.add(new BigDecimal(dbobj.getString(FieldType.CREDIT_AMOUNT.getName())).setScale(2));
				closingAmount = dbAmount.add(eCollectionAmount);
			}
			cursor.close();
			if (flag) {
				Bson filter;
				if (fields.contains(FieldType.SUB_MERCHANT_ID.getName())) {
					filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
							fields.get(FieldType.SUB_MERCHANT_ID.getName())).append(FieldType.CLOSING_DATE.getName(),
									dateFrom);
				} else {
					filter = new Document(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()))
							.append(FieldType.CLOSING_DATE.getName(), dateFrom);
				}

				Bson newValue;
				if (fields.contains(FieldType.RESELLER_ID.getName())) {
					newValue = new Document(FieldType.CREDIT_AMOUNT.getName(), creditAmount.toString())
							.append(FieldType.AMOUNT.getName(), closingAmount.toString())
							.append(FieldType.UPDATE_DATE.getName(), dateNow)
							.append(FieldType.RESELLER_ID.getName(), fields.get(FieldType.RESELLER_ID.getName()));
				} else {
					newValue = new Document(FieldType.CREDIT_AMOUNT.getName(), creditAmount.toString())
							.append(FieldType.AMOUNT.getName(), closingAmount.toString())
							.append(FieldType.UPDATE_DATE.getName(), dateNow);
				}
				Bson updateOperationDocument = new Document("$set", newValue);
				collection1.updateOne(filter, updateOperationDocument);
				logger.info("Update data in Closing Collection by ecollection for virtual account code = "
						+ fields.get("VIRTUAL_AC_CODE").toString() + " , " + newValue);
			} else {

				Calendar cal = Calendar.getInstance();
				DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
				DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd 00:00:00");

				String todayDateIndex = dateFormat1.format(cal.getTime());
				String todaysDate = dateFormat2.format(cal.getTime());
				cal.add(Calendar.DATE, -1);
				String yesterdayDate = dateFormat2.format(cal.getTime());
				String yesterdayDateIndex = dateFormat1.format(cal.getTime());

				logger.info("Todays Date = " + todaysDate);
				logger.info("Yesterdays Date = " + yesterdayDate);
				BasicDBObject finalquery2 = new BasicDBObject();
				if (fields.contains(FieldType.SUB_MERCHANT_ID.getName())) {
					finalquery2.append(FieldType.SUB_MERCHANT_ID.getName(),
							fields.get(FieldType.SUB_MERCHANT_ID.getName()));
				} else {
					finalquery2.append(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				}
				finalquery2.append("DATE_INDEX", yesterdayDateIndex);
				List<BasicDBObject> fianlList1 = new ArrayList<BasicDBObject>();
				MongoCursor<Document> cursor2 = collection1.find(finalquery2).iterator();
				BasicDBObject finalquery3 = new BasicDBObject("DATE_INDEX", yesterdayDateIndex);
				BasicDBObject finalquery4 = new BasicDBObject("DATE_INDEX", todayDateIndex);
				MongoCursor<Document> cursor3 = collection1.find(finalquery3).iterator();
				int totalCurrentDate = (int) collection1.count(finalquery4);
				int totalPreviousDate = (int) collection1.count(finalquery3);
				boolean eCollectionFlag = false;
				if (cursor2.hasNext()) {
					eCollectionFlag = true;
				}
				if (eCollectionFlag == true || !(totalCurrentDate >= totalPreviousDate)) {
					while (cursor3.hasNext()) {
						Document dbobj = cursor3.next();

						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
							dbobj = dataEncDecTool.decryptDocument(dbobj);
						}

						logger.info("orginal data in Closing Collection by ecollection for virtual account code = "
								+ dbobj.get("VIRTUAL_AC_CODE").toString() + " , " + dbobj);
						Date currentDate = new Date();
						String dateNowW = DateCreater.formatDateForDb(currentDate);
						String amount = dbobj.get("AMOUNT").toString();

						Document doc1 = new Document();
						doc1.put(FieldType.CREATE_DATE.getName(), dateNowW);
						doc1.put(FieldType.UPDATE_DATE.getName(), dateNowW);
						doc1.put(FieldType.CLOSING_DATE.getName(), todaysDate);
						doc1.put(FieldType.VIRTUAL_AC_CODE.getName(),
								dbobj.get(FieldType.VIRTUAL_AC_CODE.getName()).toString());
						doc1.put(FieldType.PAY_ID.getName(), dbobj.get(FieldType.PAY_ID.getName()).toString());
						if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
							doc1.put(FieldType.SUB_MERCHANT_ID.getName(),
									dbobj.get(FieldType.SUB_MERCHANT_ID.getName()).toString());
						}

						if (dbobj.containsKey(FieldType.RESELLER_ID.getName())) {
							doc1.put(FieldType.RESELLER_ID.getName(),
									dbobj.get(FieldType.RESELLER_ID.getName()).toString());
						}
						doc1.put(FieldType.DEBIT_AMOUNT.getName(), "0.00");
						doc1.put(FieldType.CREDIT_AMOUNT.getName(), "0.00");
						doc1.put(FieldType.AMOUNT.getName(), amount);
						doc1.put(FieldType.OPENING_AMOUNT.getName(), amount);
						doc1.put(FieldType.DATE_INDEX.getName(), todaysDateIndex);
						doc1.put(FieldType.ACQUIRER_NAME.getName(), dbobj.get(FieldType.ACQUIRER_NAME.getName()));
						collection1.insertOne(doc1);
						logger.info("Insert data in Closing Collection by ecollection for virtual account code = "
								+ dbobj.get("VIRTUAL_AC_CODE").toString() + " , " + dbobj);
					}
				}
				if (eCollectionFlag) {
					MongoCursor<Document> cursor4 = collection1.find(newFieldsObj1).iterator();
					while (cursor4.hasNext()) {
						Document dbobj = cursor4.next();

						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
							dbobj = dataEncDecTool.decryptDocument(dbobj);
						}

						logger.info("orginal data from Closing Collection for ecollection, virtual account code = "
								+ dbobj.get("VIRTUAL_AC_CODE").toString() + " , " + dbobj);
						BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
						eCollectionAmount = new BigDecimal(fields.get(FieldType.AMOUNT.getName())).setScale(2);
						creditAmount = eCollectionAmount
								.add(new BigDecimal(dbobj.getString(FieldType.CREDIT_AMOUNT.getName())).setScale(2));
						closingAmount = dbAmount.add(eCollectionAmount);
					}

					if (closingAmount.compareTo(BigDecimal.ZERO) >= 0) {
						Bson filter;
						if (fields.contains(FieldType.SUB_MERCHANT_ID.getName())) {
							filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
									fields.get(FieldType.SUB_MERCHANT_ID.getName()))
											.append(FieldType.CLOSING_DATE.getName(), dateFrom);
						} else {
							filter = new Document(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()))
									.append(FieldType.CLOSING_DATE.getName(), dateFrom);
						}

						Bson newValue;
						if (fields.contains(FieldType.RESELLER_ID.getName())) {
							newValue = new Document(FieldType.CREDIT_AMOUNT.getName(), creditAmount.toString())
									.append(FieldType.AMOUNT.getName(), closingAmount.toString())
									.append(FieldType.UPDATE_DATE.getName(), dateNow)
									.append(FieldType.RESELLER_ID.getName(),
											fields.get(FieldType.RESELLER_ID.getName()));
						} else {
							newValue = new Document(FieldType.CREDIT_AMOUNT.getName(), creditAmount.toString())
									.append(FieldType.AMOUNT.getName(), closingAmount.toString())
									.append(FieldType.UPDATE_DATE.getName(), dateNow);
						}
						Bson updateOperationDocument = new Document("$set", newValue);
						collection1.updateOne(filter, updateOperationDocument);
						logger.info("update data in Closing Collection by ecollection for virtual account code = "
								+ fields.get("VIRTUAL_AC_CODE").toString() + " , " + newValue);
					}
				} else {
					Date dNowEcollection = new Date();
					String dateNowEcollection = DateCreater.formatDateForDb(dNowEcollection);
					Document doc1 = new Document();
					doc1.put(FieldType.CREATE_DATE.getName(), dateNowEcollection);
					doc1.put(FieldType.UPDATE_DATE.getName(), dateNowEcollection);
					doc1.put(FieldType.CLOSING_DATE.getName(), todaysDate);
					doc1.put(FieldType.VIRTUAL_AC_CODE.getName(), fields.get(FieldType.VIRTUAL_AC_CODE.getName()));
					if (fields.contains(FieldType.SUB_MERCHANT_ID.getName())) {
						doc1.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						doc1.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
					} else {
						doc1.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
					}

					if (fields.contains(FieldType.RESELLER_ID.getName())) {
						doc1.put(FieldType.RESELLER_ID.getName(), fields.get(FieldType.RESELLER_ID.getName()));
					}
					doc1.put(FieldType.DEBIT_AMOUNT.getName(), "0.00");
					doc1.put(FieldType.CREDIT_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					doc1.put(FieldType.AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					doc1.put(FieldType.OPENING_AMOUNT.getName(), "0.00");
					doc1.put(FieldType.DATE_INDEX.getName(), todaysDateIndex);
					if (StringUtils.isNotBlank(fields.get(FieldType.ACQUIRER_NAME.getName()))) {
						doc1.put(FieldType.ACQUIRER_NAME.getName(), fields.get(FieldType.ACQUIRER_NAME.getName()));
					} else {
						doc1.put(FieldType.ACQUIRER_NAME.getName(), PayoutAcquirer.ICICI.name());
					}
					collection1.insertOne(doc1);
					logger.info("Insert data in Closing Collection by ecollection for virtual account code = "
							+ doc1.get("VIRTUAL_AC_CODE").toString() + " , " + doc1);
				}

				/*
				 * Document doc1 = new Document();
				 * 
				 * BasicDBObject finalquery2 = new BasicDBObject();
				 * doc1.put(FieldType.CREATE_DATE.getName(), dateNow);
				 * doc1.put(FieldType.UPDATE_DATE.getName(), dateNow);
				 * doc1.put(FieldType.CLOSING_DATE.getName(), dateFrom); doc1.put("DATE_INDEX",
				 * dateNow.substring(0, 10).replace("-", ""));
				 * doc1.put(FieldType.VIRTUAL_AC_CODE.getName(),
				 * fields.get(FieldType.VIRTUAL_AC_CODE.getName())); if
				 * (fields.contains(FieldType.SUB_MERCHANT_ID.getName())) {
				 * doc1.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				 * doc1.put(FieldType.SUB_MERCHANT_ID.getName(),
				 * fields.get(FieldType.SUB_MERCHANT_ID.getName()));
				 * finalquery2.append(FieldType.SUB_MERCHANT_ID.getName(),
				 * fields.get(FieldType.SUB_MERCHANT_ID.getName())); } else {
				 * doc1.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				 * finalquery2.append(FieldType.PAY_ID.getName(),
				 * fields.get(FieldType.PAY_ID.getName())); }
				 * 
				 * if (fields.contains(FieldType.RESELLER_ID.getName())) {
				 * doc1.put(FieldType.RESELLER_ID.getName(),
				 * fields.get(FieldType.RESELLER_ID.getName())); }
				 * doc1.put(FieldType.CREDIT_AMOUNT.getName(),
				 * fields.get(FieldType.AMOUNT.getName()));
				 * 
				 * BasicDBObject match2 = new BasicDBObject("$match", finalquery2);
				 * BasicDBObject sort2 = new BasicDBObject("$sort", new
				 * BasicDBObject("CREATE_DATE", -1));
				 * 
				 * List<BasicDBObject> pipeline2 = Arrays.asList(match2, sort2);
				 * AggregateIterable<Document> output2 = collection1.aggregate(pipeline2);
				 * output2.allowDiskUse(true); MongoCursor<Document> cursor2 =
				 * output2.iterator(); while (cursor2.hasNext()) { Document dbobj1 =
				 * cursor2.next(); BigDecimal dbAmount1 = new
				 * BigDecimal(dbobj1.getString(FieldType.AMOUNT.getName())). setScale(2);
				 * eCollectionAmount = new
				 * BigDecimal(fields.get(FieldType.AMOUNT.getName())).setScale(2 ); if
				 * (dbobj1.containsKey(FieldType.DEBIT_AMOUNT.getName())) { debitAmount = new
				 * BigDecimal(dbobj1.getString(FieldType.DEBIT_AMOUNT.getName()) ).setScale(2);
				 * } closingAmount = dbAmount1.add(eCollectionAmount); openingAmount =
				 * dbAmount1; break; } doc1.put(FieldType.DEBIT_AMOUNT.getName(),
				 * debitAmount.toString()); if
				 * (closingAmount.toString().equalsIgnoreCase("0.00")) {
				 * doc1.put(FieldType.AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				 * } else { doc1.put(FieldType.AMOUNT.getName(), closingAmount.toString()); }
				 * 
				 * doc1.put(FieldType.OPENING_AMOUNT.getName(), openingAmount.toString());
				 * 
				 * collection1.insertOne(doc1);
				 */

			}

		} catch (Exception exception) {
			String message = "Error while inserting ECollection Response in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	private Fields createAllForidfcUpiSale(String pgRefNum, String txnType, String status) {

		Fields fields = new Fields();
		try {

			List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
			dbObjList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			dbObjList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));
			dbObjList.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = coll.find(andQuery).iterator();
			if (cursor.hasNext()) {
				Document documentObj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				if (null != documentObj) {
					for (int j = 0; j < documentObj.size(); j++) {
						for (String columnName : aLLDB_Fields) {
							if (documentObj.get(columnName) != null) {
								fields.put(columnName, documentObj.get(columnName).toString());
							} else {

							}

						}
					}
				}
				fields.logAllFields("Previous fields");
			}
			cursor.close();
			return fields;
		}

		catch (Exception e) {
			logger.error("Exception in getting STB transaction for IDFC UPI Transaction ", e);
			return fields;
		}

	}

	@SuppressWarnings("static-access")
	public void insertDeliveryStatus(Fields fields, Fields previousFields) throws SystemException {

		try {
			String amountString = fields.get(FieldType.AMOUNT.getName());
			// String surchargeAmountString =
			// fields.get(FieldType.SURCHARGE_AMOUNT.getName());
			String currencyString = fields.get(FieldType.CURRENCY_CODE.getName());
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			String amount = "0";
			if (!StringUtils.isEmpty(amountString) && !StringUtils.isEmpty(currencyString)) {
				amount = Amount.toDecimal(amountString, currencyString);
				fields.put(FieldType.AMOUNT.getName(), amount);
			}

			String payId = fields.get(FieldType.PAY_ID.getName());
			if (!StringUtils.isEmpty(payId)) {
				fields.put(FieldType.PAY_ID.getName(), payId);
			}

			fields.put(FieldType.CREATE_DATE.getName(), dateNow);

			String orderID = fields.get(FieldType.ORDER_ID.getName());
			if (!StringUtils.isEmpty(orderID)) {
				fields.put(FieldType.ORDER_ID.getName(), orderID);
			}

			String mopType = previousFields.get(FieldType.MOP_TYPE.getName());
			if (!StringUtils.isEmpty(mopType)) {
				fields.put(FieldType.MOP_TYPE.getName(), mopType);
			}

			String paymentType = previousFields.get(FieldType.PAYMENT_TYPE.getName());
			if (!StringUtils.isEmpty(paymentType)) {
				fields.put(FieldType.PAYMENT_TYPE.getName(), paymentType);
			}
			String pgRefNum = fields.get(FieldType.PG_REF_NUM.getName());
			if (!StringUtils.isEmpty(pgRefNum)) {
				fields.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
			}

			String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
			if (!StringUtils.isEmpty(currencyCode)) {
				fields.put(FieldType.CURRENCY_CODE.getName(), currencyCode);
			}

			String txnType = fields.get(FieldType.TXNTYPE.getName());
			if (!StringUtils.isEmpty(txnType)) {
				fields.put(FieldType.TXNTYPE.getName(), txnType);
			}

			String deliveryCode = fields.get(FieldType.DELIVERY_CODE.getName());
			if (!StringUtils.isEmpty(deliveryCode)) {
				fields.put(FieldType.RESPONSE_CODE.getName(), deliveryCode);
			}

			String deliveryStatus = fields.get(FieldType.DELIVERY_STATUS.getName());
			if (!StringUtils.isEmpty(deliveryStatus)) {
				fields.put(FieldType.STATUS.getName(), deliveryStatus);
			}

			BasicDBObject newFieldsObj = new BasicDBObject();
			for (int i = 0; i < fields.size(); i++) {
				Collection<String> aLLDB_Fields = SystemProperties.getAllDBRequestFields();
				for (String columnName : aLLDB_Fields) {
					newFieldsObj.put(columnName, fields.get(columnName));
				}
			}
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.DELIVERY_STATUS_COLLECTION_NAME.getValue()));
			Document doc = new Document(newFieldsObj);
			collection.insertOne(doc);
		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public Fields fetchAllFieldsOfIciciCib(String txnId) {
		try {
			Fields fields = new Fields();

			List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();

			dbObjList.add(new BasicDBObject(FieldType.TXN_ID.getName(), txnId));

			BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
			andQuery.put(FieldType.TRANSACTION_OF.getName(), FieldType.CIB_TRANSACTION.getName());

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap
					.get(prefix + Constants.NODAL_SETTLEMENT_COLLECTION_NAME.getValue()));

			FindIterable<Document> cursor = coll.find(andQuery);

			if (cursor.iterator().hasNext()) {
				Document documentObj = cursor.iterator().next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				if (null != documentObj) {
					fields.put(FieldType.BENE_ACCOUNT_NO.getName(),
							(String) documentObj.get(FieldType.BENE_ACCOUNT_NO.getName()));
					fields.put(FieldType.PAYEE_NAME.getName(),
							(String) documentObj.get(FieldType.PAYEE_NAME.getName()));
					fields.put(FieldType.IFSC.getName(), (String) documentObj.get(FieldType.IFSC.getName()));
					fields.put(FieldType.AMOUNT.getName(), (String) documentObj.get(FieldType.AMOUNT.getName()));
					fields.put(FieldType.CURRENCY_CODE.getName(),
							(String) documentObj.get(FieldType.CURRENCY_CODE.getName()));
					fields.put(FieldType.TXNTYPE.getName(), (String) documentObj.get(FieldType.TXNTYPE.getName()));
					fields.put(FieldType.REMARKS.getName(), (String) documentObj.get(FieldType.REMARKS.getName()));
//					fields.put(FieldType.STATUS.getName(), (String) documentObj.get(FieldType.STATUS.getName()));
					fields.put(FieldType.PAY_ID.getName(), (String) documentObj.get(FieldType.PAY_ID.getName()));
					if (StringUtils.isNotBlank((String) documentObj.get(FieldType.SUB_MERCHANT_ID.getName()))) {
						fields.put(FieldType.SUB_MERCHANT_ID.getName(),
								(String) documentObj.get(FieldType.SUB_MERCHANT_ID.getName()));
					}
				}
				fields.logAllFields("Previous fields");
			}

			cursor.iterator().close();
			return fields;
		} catch (Exception e) {
			logger.error("Error in Icici Cib Fields getting from DB : ", e);
		}
		return null;

	}

	@SuppressWarnings("static-access")
	public Fields getPreviousForPgRefNum(String pgRefNum) throws SystemException {

		Fields preFields = new Fields();

		try {
			logger.info("Inside getPreviousForPgRefNum (PG_REF_NUM): " + pgRefNum);
			List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> statusConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));

			BasicDBObject txnTypeQuery = new BasicDBObject();
			txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName()));
			txnTypeQuery.append("$or", txnTypeConditionLst);
			condList.add(txnTypeQuery);

			BasicDBObject statusQuery = new BasicDBObject();
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ENROLLED.getName()));
			statusQuery.append("$or", statusConditionLst);
			condList.add(statusQuery);

			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(saleQuery).iterator();
			try {
				if (cursor.hasNext()) {
					Document documentObj = (Document) cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						documentObj = dataEncDecTool.decryptDocument(documentObj);
					}

					if (null != documentObj) {
						for (int j = 0; j < documentObj.size(); j++) {
							for (String columnName : systemProperties.getDBFields()) {
								if (documentObj.get(columnName) != null) {
									preFields.put(columnName, documentObj.get(columnName).toString());
								}
							}
						}
					}
				}
			} finally {
				cursor.close();
			}

			// Correct Amount to decimal format

			if (StringUtils.isNotBlank(preFields.get(FieldType.AMOUNT.getName()))) {
				preFields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(preFields.get(FieldType.AMOUNT.getName()),
						preFields.get(FieldType.CURRENCY_CODE.getName())));
			}

			if (StringUtils.isNotBlank(preFields.get(FieldType.TOTAL_AMOUNT.getName()))) {
				preFields.put(FieldType.TOTAL_AMOUNT.getName(),
						Amount.formatAmount(preFields.get(FieldType.TOTAL_AMOUNT.getName()),
								preFields.get(FieldType.CURRENCY_CODE.getName())));
			}

			preFields.put(FieldType.HASH.getName(), Hasher.getHash(preFields));
			return preFields;
		} catch (Exception exception) {
			String message = "Error while previous based on Pg Ref Num from database";
			logger.error(message, exception);
			return preFields;
		}

	}

	@SuppressWarnings("static-access")
	public Fields getPreviousForAcqId(String acqId) throws SystemException {

		Fields preFields = new Fields();

		try {
			logger.info("Inside getPreviousForAcqId (ACQ_ID): " + acqId);
			List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> statusConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.ACQ_ID.getName(), acqId));

			BasicDBObject txnTypeQuery = new BasicDBObject();
			txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName()));
			txnTypeQuery.append("$or", txnTypeConditionLst);
			condList.add(txnTypeQuery);

			BasicDBObject statusQuery = new BasicDBObject();
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ENROLLED.getName()));
			statusQuery.append("$or", statusConditionLst);
			condList.add(statusQuery);

			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(saleQuery).iterator();
			try {
				if (cursor.hasNext()) {
					Document documentObj = (Document) cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						documentObj = dataEncDecTool.decryptDocument(documentObj);
					}

					if (null != documentObj) {
						for (int j = 0; j < documentObj.size(); j++) {
							for (String columnName : systemProperties.getDBFields()) {
								if (documentObj.get(columnName) != null) {
									preFields.put(columnName, documentObj.get(columnName).toString());
								}
							}
						}
					}
				}
			} finally {
				cursor.close();
			}

			// Correct Amount to decimal format

			if (StringUtils.isNotBlank(preFields.get(FieldType.AMOUNT.getName()))) {
				preFields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(preFields.get(FieldType.AMOUNT.getName()),
						preFields.get(FieldType.CURRENCY_CODE.getName())));
			}

			if (StringUtils.isNotBlank(preFields.get(FieldType.TOTAL_AMOUNT.getName()))) {
				preFields.put(FieldType.TOTAL_AMOUNT.getName(),
						Amount.formatAmount(preFields.get(FieldType.TOTAL_AMOUNT.getName()),
								preFields.get(FieldType.CURRENCY_CODE.getName())));
			}

			preFields.put(FieldType.HASH.getName(), Hasher.getHash(preFields));
			return preFields;
		} catch (Exception exception) {
			String message = "Error while previous based on Acq Id from database";
			logger.error(message, exception);
			return preFields;
		}

	}

	@SuppressWarnings("static-access")
	public Fields getPreviousForOrderId(String orderId) throws SystemException {

		Fields preFields = new Fields();

		try {
			logger.info("Inside getPreviousForOrderId (PG_REF_NUM): " + orderId);
			List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> statusConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), orderId));

			BasicDBObject txnTypeQuery = new BasicDBObject();
			txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName()));
			txnTypeQuery.append("$or", txnTypeConditionLst);
			condList.add(txnTypeQuery);

			BasicDBObject statusQuery = new BasicDBObject();
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ENROLLED.getName()));
			statusQuery.append("$or", statusConditionLst);
			condList.add(statusQuery);

			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(saleQuery).iterator();
			try {
				if (cursor.hasNext()) {
					Document documentObj = (Document) cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						documentObj = dataEncDecTool.decryptDocument(documentObj);
					}

					if (null != documentObj) {
						for (int j = 0; j < documentObj.size(); j++) {
							for (String columnName : systemProperties.getDBFields()) {
								if (documentObj.get(columnName) != null) {
									preFields.put(columnName, documentObj.get(columnName).toString());
								}
							}
						}
					}
				}
			} finally {
				cursor.close();
			}

			// Correct Amount to decimal format

			if (StringUtils.isNotBlank(preFields.get(FieldType.AMOUNT.getName()))) {
				preFields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(preFields.get(FieldType.AMOUNT.getName()),
						preFields.get(FieldType.CURRENCY_CODE.getName())));
			}

			if (StringUtils.isNotBlank(preFields.get(FieldType.TOTAL_AMOUNT.getName()))) {
				preFields.put(FieldType.TOTAL_AMOUNT.getName(),
						Amount.formatAmount(preFields.get(FieldType.TOTAL_AMOUNT.getName()),
								preFields.get(FieldType.CURRENCY_CODE.getName())));
			}

			preFields.put(FieldType.HASH.getName(), Hasher.getHash(preFields));
			return preFields;
		} catch (Exception exception) {
			String message = "Error while previous based on Pg Ref Num from database";
			logger.error(message, exception);
			return preFields;
		}

	}

	@SuppressWarnings("static-access")
	public Fields getPreviousForPgRefNumForCyrpto(String pgRefNum) throws SystemException {

		Fields preFields = new Fields();

		try {
			logger.info("Inside getPreviousForPgRefNum (PG_REF_NUM): " + pgRefNum);
			List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> statusConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));

			// BasicDBObject txnTypeQuery = new BasicDBObject();
			// txnTypeConditionLst.add(new
			// BasicDBObject(FieldType.TXNTYPE.getName(),
			// TransactionType..getName()));
			// txnTypeConditionLst.add(new
			// BasicDBObject(FieldType.TXNTYPE.getName(),
			// TransactionType.ENROLL.getName()));
			// txnTypeQuery.append("$or", txnTypeConditionLst);
			// condList.add(txnTypeQuery);

			BasicDBObject statusQuery = new BasicDBObject();
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PROCESSING.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName()));
			statusQuery.append("$or", statusConditionLst);
			condList.add(statusQuery);

			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(saleQuery).iterator();
			try {
				if (cursor.hasNext()) {
					Document documentObj = (Document) cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						documentObj = dataEncDecTool.decryptDocument(documentObj);
					}

					if (null != documentObj) {
						for (int j = 0; j < documentObj.size(); j++) {
							for (String columnName : systemProperties.getDBFields()) {
								if (documentObj.get(columnName) != null) {
									preFields.put(columnName, documentObj.get(columnName).toString());
								}
							}
						}
					}
				}
			} finally {
				cursor.close();
			}

			// Correct Amount to decimal format

			if (StringUtils.isNotBlank(preFields.get(FieldType.AMOUNT.getName()))) {
				preFields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(preFields.get(FieldType.AMOUNT.getName()),
						preFields.get(FieldType.CURRENCY_CODE.getName())));
			}

			if (StringUtils.isNotBlank(preFields.get(FieldType.TOTAL_AMOUNT.getName()))) {
				preFields.put(FieldType.TOTAL_AMOUNT.getName(),
						Amount.formatAmount(preFields.get(FieldType.TOTAL_AMOUNT.getName()),
								preFields.get(FieldType.CURRENCY_CODE.getName())));
			}

			preFields.put(FieldType.HASH.getName(), Hasher.getHash(preFields));
			return preFields;
		} catch (Exception exception) {
			String message = "Error while previous based on Pg Ref Num from database";
			logger.error(message, exception);
			return preFields;
		}

	}

	@SuppressWarnings("static-access")
	public String getPreviousForOID(String oid) throws SystemException {

		String internalRequestFields = null;
		try {
			logger.info("Inside getPreviousForOID (OID): " + oid);

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.OID.getName(), oid));
			condList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.NEWORDER.getName()));
			condList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PENDING.getName()));
			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(saleQuery).iterator();
			try {
				while (cursor.hasNext()) {

					Document doc = cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						doc = dataEncDecTool.decryptDocument(doc);
					}

					if (StringUtils.isNotBlank(String.valueOf(doc.get(FieldType.INTERNAL_REQUEST_FIELDS.getName())))) {
						internalRequestFields = String.valueOf(doc.get(FieldType.INTERNAL_REQUEST_FIELDS.getName()));
						break;
					}
				}
			} finally {
				cursor.close();
			}
			if (StringUtils.isBlank(internalRequestFields)) {
				List<BasicDBObject> saleCondList = new ArrayList<BasicDBObject>();
				saleCondList.add(new BasicDBObject(FieldType.OID.getName(), oid));
				saleCondList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
				saleCondList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));
				BasicDBObject saleNewQuery = new BasicDBObject("$and", saleCondList);

				MongoDatabase dbInsNew = mongoInstance.getDB();
				MongoCollection<Document> collectionNew = dbInsNew.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				MongoCursor<Document> cursor1 = collectionNew.find(saleNewQuery).iterator();
				try {
					while (cursor1.hasNext()) {

						Document doc = cursor1.next();

						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
							doc = dataEncDecTool.decryptDocument(doc);
						}

						if (StringUtils
								.isNotBlank(String.valueOf(doc.get(FieldType.INTERNAL_REQUEST_FIELDS.getName())))) {
							internalRequestFields = String
									.valueOf(doc.get(FieldType.INTERNAL_REQUEST_FIELDS.getName()));
							break;
						}
					}
				} finally {
					cursor.close();
				}
			}
			return internalRequestFields;
		} catch (Exception exception) {
			String message = "Error while previous based on OID from database";
			logger.error(message, exception);
			return internalRequestFields;
		}

	}

	@SuppressWarnings("static-access")
	public boolean getCapturedForPgRef(String pgRefNum) throws SystemException {

		boolean isCaptured = false;
		try {
			logger.info("Inside getPreviousForPgRef (pgRefNum): " + pgRefNum);

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			condList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			condList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			logger.info("Inside getPreviousForPgRef (pgRefNum): QUERY " + saleQuery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			long count = collection.count(saleQuery);
			if (count > 0) {
				isCaptured = true;
			}

			return isCaptured;
		} catch (Exception exception) {
			String message = "Error while checking captued via pg ref num from database";
			logger.error(message, exception);
			return isCaptured;
		}

	}

	public boolean validatePgRefNum(String pgRefNum, String acqId) throws SystemException {

		boolean isCaptured = false;
		try {
			logger.info("Inside validatePgRefNum (pgRefNum): " + pgRefNum);

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			condList.add(new BasicDBObject(FieldType.ACQ_ID.getName(), acqId));
			condList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			condList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			logger.info("Inside validatePgRefNum (pgRefNum): QUERY " + saleQuery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			long count = collection.count(saleQuery);
			if (count > 0) {
				isCaptured = true;
			}

			return isCaptured;
		} catch (Exception exception) {
			String message = "Error while checking captued via pg ref num from database";
			logger.error(message, exception);
			return isCaptured;
		}

	}

	@SuppressWarnings("static-access")
	public boolean validateDuplicateUpiQRRequest(String orderId) throws SystemException {

		boolean qrAlreadyGenerated = false;
		try {

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			condList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			condList.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));
			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			logger.info("Inside validateDuplicateUpiQRRequest (orderId): QUERY " + saleQuery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.UPI_QR_REQUEST_COLLECTION.getValue()));

			long count = collection.count(saleQuery);
			if (count > 0) {
				qrAlreadyGenerated = true;
			}

			return qrAlreadyGenerated;
		} catch (Exception exception) {
			String message = "Error while checking captued via pg ref num from database";
			logger.error(message, exception);
			return qrAlreadyGenerated;
		}

	}

	@SuppressWarnings("static-access")
	public boolean validateDuplicatePgQRRequest(String orderId) throws SystemException {

		boolean qrAlreadyGenerated = false;
		try {

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			condList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			condList.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));
			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			logger.info("Inside validateDuplicatePgQRRequest (orderId): QUERY " + saleQuery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.PG_QR_REQUEST_COLLECTION.getValue()));

			long count = collection.count(saleQuery);
			if (count > 0) {
				qrAlreadyGenerated = true;
			}

			return qrAlreadyGenerated;
		} catch (Exception exception) {
			String message = "Error while checking captued via pg ref num from database";
			logger.error(message, exception);
			return qrAlreadyGenerated;
		}

	}

	public Fields getFieldsForIciciUpi(String pgRefNum, String txnType, String status) throws SystemException {
		try {
			return createAllForIciciUpiSale(pgRefNum, txnType, status);
		} catch (Exception exception) {
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	private Fields createAllForIciciUpiSale(String pgRefNum, String txnType, String status) {
		Fields fields = new Fields();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		dbObjList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
		dbObjList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));
		dbObjList.add(new BasicDBObject(FieldType.STATUS.getName(), status));

		List<BasicDBObject> qrObjList = new ArrayList<BasicDBObject>();
		qrObjList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
		qrObjList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PENDING.getName()));
		qrObjList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.NEWORDER.getName()));

		BasicDBObject vpaQuery = new BasicDBObject("$and", dbObjList);
		BasicDBObject qrQuery = new BasicDBObject("$and", qrObjList);
		BasicDBObject finalQuery = new BasicDBObject();

		List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();
		finalList.add(vpaQuery);
		finalList.add(qrQuery);
		finalQuery.put("$or", finalList);

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		MongoCursor<Document> cursor = coll.find(finalQuery).iterator();
		if (cursor.hasNext()) {
			Document documentObj = cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}
					}
				}
			}
			fields.logAllFields("Previous fields");
		}
		cursor.close();
		return fields;
	}

	@SuppressWarnings("static-access")
	public boolean getAlreadyUpdated(String pgRefNum) throws SystemException {

		boolean isCaptured = false;
		try {
			logger.info("Inside getPreviousForPgRef (pgRefNum): " + pgRefNum);

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			condList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));

			List<BasicDBObject> statusList = new ArrayList<BasicDBObject>();
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DECLINED.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.REJECTED.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ERROR.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CANCELLED.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.INVALID.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED_AT_ACQUIRER.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ACQUIRER_TIMEOUT.getName()));
			statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName()));

			BasicDBObject statusQuery = new BasicDBObject("$or", statusList);

			condList.add(statusQuery);
			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			logger.info("Inside getPreviousForPgRef (pgRefNum): QUERY " + saleQuery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			long count = collection.count(saleQuery);
			if (count > 0) {
				isCaptured = true;
			}

			return isCaptured;
		} catch (Exception exception) {
			String message = "Error while checking captued via pg ref num from database";
			logger.error(message, exception);
			return isCaptured;
		}

	}

	@SuppressWarnings("static-access")
	public void updateRefundProdDesc(Fields fields) {

		try {

			MongoDatabase dbIns = null;
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get("MONGO_DB_" + Constants.PROD_DESC_COLLECTION.getValue()));

			List<Document> prodList = getProdDescForSKU(fields, fields.get(FieldType.SKU_CODE.getName()));

			for (Document doc : prodList) {

				doc.remove("_id");
				doc.put("TXNTYPE", TransactionType.REFUND.getName());
				doc.put("REFUND_ORDER_ID", fields.get(FieldType.REFUND_ORDER_ID.getName()));
				doc.put("PG_REF_NUM", fields.get(FieldType.REFUND_ORDER_ID.getName()));
				doc.put("ACQ_ID", fields.get(FieldType.REFUND_ORDER_ID.getName()));
				doc.put("RRN", fields.get(FieldType.REFUND_ORDER_ID.getName()));
				doc.put("TXN_ID", TransactionManager.getNewTransactionId());

				Date dNow = new Date();
				String dateNow = DateCreater.formatDateForDb(dNow);
				doc.put("CREATE_DATE", dateNow);
				doc.put("DATE_INDEX", dateNow.substring(0, 10).replace("-", ""));
				collection.insertOne(doc);
			}

		}

		catch (Exception e) {
			logger.error("Exception in updating product description", e);
		}

	}

	@SuppressWarnings("static-access")
	public String getProdDescForOID(String oid) throws SystemException {

		String prodDesc = null;
		try {
			logger.info("Inside getPreviousForOID (OID): " + oid);

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.OID.getName(), oid));
			condList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.NEWORDER.getName()));
			condList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PENDING.getName()));
			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(saleQuery).iterator();
			try {
				while (cursor.hasNext()) {

					Document doc = cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						doc = dataEncDecTool.decryptDocument(doc);
					}

					if (StringUtils.isNotBlank(String.valueOf(doc.get(FieldType.PROD_DESC.getName())))) {
						prodDesc = String.valueOf(doc.get(FieldType.PROD_DESC.getName()));
						break;
					}
				}
			} finally {
				cursor.close();
			}

			return prodDesc;
		} catch (Exception exception) {
			String message = "Error fetching prodDesc from OID from database";
			logger.error(message, exception);
			return prodDesc;
		}

	}

	@SuppressWarnings("static-access")
	public List<Document> getProdDescForSKU(Fields fields, String skuCode) throws SystemException {

		List<Document> prodList = new ArrayList<Document>();

		try {

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));
			condList.add(new BasicDBObject(FieldType.OID.getName(), fields.get(FieldType.OID.getName())));
			condList.add(new BasicDBObject(FieldType.SKU_CODE.getName(), skuCode));
			condList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.PROD_DESC_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(saleQuery).iterator();
			try {
				while (cursor.hasNext()) {

					Document doc = cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						doc = dataEncDecTool.decryptDocument(doc);
					}

					prodList.add(doc);
				}
			} finally {
				cursor.close();
			}

			return prodList;
		} catch (Exception exception) {
			String message = "Error fetching prodDesc from OID from database";
			logger.error(message, exception);
			return prodList;
		}

	}

	public String getMerchantAmount(Fields fields, String productAmt) throws SystemException {

		try {
			BigDecimal pgTdr = new BigDecimal(fields.get(FieldType.PG_TDR_SC.getName()));
			BigDecimal pgGst = new BigDecimal(fields.get(FieldType.PG_GST.getName()));
			BigDecimal acquirerTdr = new BigDecimal(fields.get(FieldType.ACQUIRER_TDR_SC.getName()));
			BigDecimal acquirerGst = new BigDecimal(fields.get(FieldType.ACQUIRER_GST.getName()));

			BigDecimal totalAmount = new BigDecimal(Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
			BigDecimal productAmount = new BigDecimal(productAmt);

			BigDecimal merchantAmount = totalAmount.subtract(pgTdr.add(pgGst).add(acquirerTdr).add(acquirerGst));

			merchantAmount = merchantAmount.divide(totalAmount);
			merchantAmount = merchantAmount.multiply(productAmount);

			return merchantAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
		} catch (Exception e) {
			logger.error("Exception : ", e);
		}
		return null;
	}

	public String getPreviousByOIDForSentToBank(String oid) throws SystemException {

		String internalRequestFields = null;
		try {
			logger.info("Inside getPreviousByOIDForSentToBank (OID): " + oid);
			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.OID.getName(), oid));
			condList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			condList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));
			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(saleQuery).iterator();
			try {
				while (cursor.hasNext()) {
					Document doc = cursor.next();
					if (StringUtils.isNotBlank(String.valueOf(doc.get(FieldType.INTERNAL_REQUEST_FIELDS.getName())))) {
						internalRequestFields = String.valueOf(doc.get(FieldType.INTERNAL_REQUEST_FIELDS.getName()));
						break;
					}
				}
			} finally {
				cursor.close();
			}
			return internalRequestFields;
		} catch (Exception exception) {
			String message = "Error while previous based on OID from database";
			logger.error(message, exception);
			return internalRequestFields;
		}
	}

	@SuppressWarnings("static-access")
	public void updateProdDescForCrm(Fields fields) {

		try {
			String refundDataArray[] = null;
			String skuDataArray[] = null;
			String categoryDataArray[] = null;
			String priceDataArray[] = null;

			MongoDatabase dbIns = null;
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get("MONGO_DB_" + Constants.PROD_DESC_COLLECTION.getValue()));

			String payId = fields.get(FieldType.PAY_ID.getName());
			String OrderId = fields.get(FieldType.ORDER_ID.getName());
			// String prodDescription =
			// getProdDescForOID(fields.get(FieldType.OID.getName()));

			// if (prodDescription == null) {
			//
			// logger.info("Product description is null for Order iD " +
			// OrderId);
			// return;
			// }
			String settlementDays = propertiesManager.propertiesMap.get("NORMAL_CRM_PAYMENT_TURN");
			Integer settlementInt = Integer.valueOf(settlementDays);
			// JSONObject prodDesc = new JSONObject(prodDescription);
			// String priceData = fields.get(FieldType.PRODUCT_PRICE.getName());
			// // prodDesc.getJSONArray(FieldType.PROD_DESC.getName());
			String skuData = fields.get(FieldType.SKU_CODE.getName());
			String refundData = fields.get(FieldType.REFUND_DAYS.getName());
			String categoryData = fields.get(FieldType.CATEGORY_CODE.getName());
			String vendorId = fields.get(FieldType.VENDOR_ID.getName());

			String amountString = fields.get(FieldType.AMOUNT.getName());
			priceDataArray = fields.get(FieldType.PRODUCT_PRICE.getName()).split(",");

			String amount = "0";
			if (!StringUtils.isEmpty(amountString)
					&& !StringUtils.isEmpty(fields.get(FieldType.CURRENCY_CODE.getName()))) {
				if (!amountString.contains(".")) {
					amount = Amount.toDecimal(amountString, fields.get(FieldType.CURRENCY_CODE.getName()));
				} else {
					amount = amountString;
				}
			}

			BigDecimal amountInDecimal = new BigDecimal(amount);
			boolean priceFlag = false;
			BigDecimal productPriceSum = new BigDecimal("0.0");
			for (String price : priceDataArray) {
				productPriceSum = productPriceSum.add(new BigDecimal(price));
			}
			if (amountInDecimal.compareTo(productPriceSum) == 0) {
				priceFlag = true;
			}
			if (StringUtils.isNotEmpty(skuData)) {
				skuDataArray = skuData.split(",");
			}
			if (StringUtils.isNotEmpty(refundData)) {
				refundDataArray = refundData.split(",");
			}
			if (StringUtils.isNotEmpty(categoryData)) {
				categoryDataArray = categoryData.split(",");
			}
			if (skuDataArray != null && priceFlag) {
				for (int i = 0; i < skuDataArray.length; i++) {

					// JSONObject data = dataArray.getJSONObject(i);

					// String objString = data.toString();

					String SKU_CODE = null;
					if (skuDataArray.length != 0) {
						SKU_CODE = skuDataArray[i];
					}

					String PRODUCT_PRICE = null;
					if (priceDataArray.length != 0) {
						PRODUCT_PRICE = priceDataArray[i];
					}

					Integer refundint = 0;
					String REFUND_DAYS = null;
					if (refundDataArray.length != 0) {
						REFUND_DAYS = refundDataArray[i];
						refundint = Integer.valueOf(REFUND_DAYS);
					}

					String VENDOR_ID = null;
					if (StringUtils.isNotEmpty(vendorId)) {
						VENDOR_ID = vendorId;
					}

					String CATEGORY_CODE = null;
					if (categoryDataArray.length != 0) {
						CATEGORY_CODE = categoryDataArray[i];
					}

					// String QUANTITY = null;
					// if (objString.contains("QTY")) {
					// QUANTITY = data.get("QTY").toString();
					// }

					// Integer qty = Integer.valueOf(QUANTITY);
					// for (int j = 0; j < qty; j++) {

					Document doc = new Document();
					doc.put("PRODUCT_ID", TransactionManager.getNewTransactionId());
					doc.put("TXN_ID", fields.get(FieldType.TXN_ID.getName()));
					doc.put("ORDER_ID", OrderId);
					doc.put("PAY_ID", payId);
					doc.put("SKU_CODE", SKU_CODE);
					doc.put("PRODUCT_PRICE", PRODUCT_PRICE);
					doc.put("REFUND_DAYS", REFUND_DAYS);
					doc.put("VENDOR_ID", VENDOR_ID);
					doc.put("CATEGORY_CODE", CATEGORY_CODE);
					doc.put("STATUS", fields.get(FieldType.STATUS.getName()));
					doc.put("TXNTYPE", TransactionType.SALE.getName());
					doc.put("PG_REF_NUM", fields.get(FieldType.PG_REF_NUM.getName()));
					doc.put("TXNTYPE", fields.get(FieldType.TXNTYPE.getName()));
					doc.put("OID", fields.get(FieldType.OID.getName()));
					doc.put("ACQ_ID", fields.get(FieldType.ACQ_ID.getName()));
					doc.put("RRN", fields.get(FieldType.RRN.getName()));
					doc.put("CUST_NAME", fields.get(FieldType.CUST_NAME.getName()));
					doc.put("CUST_PHONE", fields.get(FieldType.CUST_PHONE.getName()));
					doc.put("PAYMENTS_REGION", fields.get(FieldType.PAYMENTS_REGION.getName()));
					doc.put("CARD_HOLDER_TYPE", fields.get(FieldType.CARD_HOLDER_TYPE.getName()));
					doc.put("PAYMENT_TYPE", fields.get(FieldType.PAYMENT_TYPE.getName()));
					doc.put("MOP_TYPE", fields.get(FieldType.MOP_TYPE.getName()));
					doc.put("CURRENCY_CODE", fields.get(FieldType.CURRENCY_CODE.getName()));
					Date dNow = new Date();
					String dateNow = DateCreater.formatDateForDb(dNow);
					doc.put("CREATE_DATE", dateNow);
					doc.put("DATE_INDEX", dateNow.substring(0, 10).replace("-", ""));
					doc.put(FieldType.DELIVERY_CODE.getName(), "101");
					doc.put(FieldType.DELIVERY_STATUS.getName(), "PENDING");
					Date settlementDate = new Date();

					Calendar calendar = new GregorianCalendar();
					calendar.setTime(settlementDate);
					calendar.add(Calendar.DATE, settlementInt + refundint);
					settlementDate = calendar.getTime();

					String settlementDay = DateCreater.formatDateForDb(settlementDate);

					doc.put("SETTLEMENT_DATE", settlementDay);
					doc.put("SETTLEMENT_DATE_INDEX", settlementDay.substring(0, 10).replace("-", ""));

					String merchantAmount = getMerchantAmount(fields, PRODUCT_PRICE);
					doc.put("MERCHANT_AMOUNT", merchantAmount);

					collection.insertOne(doc);
					// }

				}
			}
		}

		catch (Exception e) {
			logger.error("Exception in updating product description from CRM ", e);
		}

	}

	@SuppressWarnings("static-access")
	public void updateTxnStatus(BasicDBObject dbBasicObject) {
		try {
			BasicDBObject findquery = null;
			List<BasicDBObject> findqueryList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> statusQuery = new ArrayList<BasicDBObject>();
			MongoDatabase dbIns = null;
			BasicDBObject newFieldsObj = new BasicDBObject();

			for (String columnName : allTxnStatus_Fields) {

				if (dbBasicObject.containsField(columnName))
					newFieldsObj.put(columnName, dbBasicObject.get(columnName));
			}

			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

			if (dbBasicObject.get(FieldType.TXNTYPE.getName()).equals(TransactionType.NEWORDER.getName())) {
				findqueryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(),
						dbBasicObject.get(FieldType.ORDER_ID.getName())));
				findqueryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.NEWORDER.getName()));
				findquery = new BasicDBObject("$and", findqueryList);

				MongoCursor<Document> cursor = collection.find(findquery).iterator();
				if (cursor.hasNext()) {
					Document documentObj = cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						documentObj = dataEncDecTool.decryptDocument(documentObj);
					}

					if (null != documentObj) {

						BasicDBObject updateObj = new BasicDBObject();
						updateObj.put("$set", newFieldsObj);
						Document newDoc = new Document(updateObj);
						collection.updateOne(findquery, newDoc);
					}
				} else {

					newFieldsObj.put("_id", dbBasicObject.get(FieldType.TXN_ID.getName()));
					Document doc = new Document(newFieldsObj);
					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

						collection.insertOne(dataEncDecTool.encryptDocument(doc));
					} else {
						collection.insertOne(doc);
					}
					// collection.insertOne(doc);
				}

			} else if (dbBasicObject.get(FieldType.TXNTYPE.getName()).equals(TransactionType.ENROLL.getName())
					&& (dbBasicObject.get(FieldType.STATUS.getName()).equals(StatusType.ENROLLED.getName())
							|| dbBasicObject.get(FieldType.STATUS.getName()).equals(StatusType.SENT_TO_BANK.getName())
							|| dbBasicObject.get(FieldType.RESPONSE_CODE.getName()).equals("000"))) {

				findqueryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(),
						dbBasicObject.get(FieldType.ORDER_ID.getName())));
				findqueryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.NEWORDER.getName()));
				findqueryList.add(
						new BasicDBObject(FieldType.AMOUNT.getName(), dbBasicObject.get(FieldType.AMOUNT.getName())));

				findquery = new BasicDBObject("$and", findqueryList);

				BasicDBObject updateObj = new BasicDBObject();
				updateObj.put("$set", newFieldsObj);
				Document newDoc = new Document(updateObj);
				collection.updateOne(findquery, newDoc);

			} else if (dbBasicObject.get(FieldType.TXNTYPE.getName()).equals(TransactionType.SALE.getName())
					&& dbBasicObject.get(FieldType.STATUS.getName()).equals(StatusType.CAPTURED.getName())) {

				findqueryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(),
						dbBasicObject.get(FieldType.ORDER_ID.getName())));

				if (dbBasicObject.get(FieldType.MOP_TYPE.getName()).equals(MopType.COD.getCode())) {

					findqueryList
							.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.NEWORDER.getName()));
				} else {

					findqueryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName()));

					statusQuery.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ENROLLED.getName()));
					statusQuery.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));

					findqueryList.add(new BasicDBObject("$or", statusQuery));
				}
				findqueryList.add(
						new BasicDBObject(FieldType.AMOUNT.getName(), dbBasicObject.get(FieldType.AMOUNT.getName())));
				findquery = new BasicDBObject("$and", findqueryList);

				BasicDBObject updateObj = new BasicDBObject();
				updateObj.put("$set", newFieldsObj);
				Document newDoc = new Document(updateObj);
				collection.updateOne(findquery, newDoc);
			}

			else if (dbBasicObject.get(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUND.getName())) {
				newFieldsObj.put("_id", dbBasicObject.get(FieldType.TXN_ID.getName()));
				Document doc = new Document(newFieldsObj);
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					collection.insertOne(dataEncDecTool.encryptDocument(doc));
				} else {
					collection.insertOne(doc);
				}
				// collection.insertOne(doc);
			} else {

				newFieldsObj.put("_id", dbBasicObject.get(FieldType.TXN_ID.getName()));
				Document doc = new Document(newFieldsObj);
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					collection.insertOne(dataEncDecTool.encryptDocument(doc));
				} else {
					collection.insertOne(doc);
				}
				// collection.insertOne(doc);

				// if(dbBasicObject.get(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUNDRECO.getName()))
				// {
				// findqueryList.add(new
				// BasicDBObject(FieldType.ORDER_ID.getName(),dbBasicObject.get(FieldType.ORDER_ID.getName())));
				// findqueryList.add(new
				// BasicDBObject(FieldType.TXNTYPE.getName(),dbBasicObject.get(TransactionType.REFUND.getName())));
				//
				// findquery = new BasicDBObject("$and", findqueryList);
				//
				// }else
				// if(dbBasicObject.get(FieldType.TXNTYPE.getName()).equals(TransactionType.RECO.getName()))
				// {
				// findqueryList.add(new
				// BasicDBObject(FieldType.ORDER_ID.getName(),dbBasicObject.get(FieldType.ORDER_ID.getName())));
				// findqueryList.add(new
				// BasicDBObject(FieldType.TXNTYPE.getName(),dbBasicObject.get(TransactionType.CAPTURE.getName())));
				//
				// findquery = new BasicDBObject("$and", findqueryList);
				//
				// }else{
				// findquery = new
				// BasicDBObject(FieldType.ORDER_ID.getName(),dbBasicObject.get(FieldType.ORDER_ID.getName()));
				// }
				// BasicDBObject updateObj = new BasicDBObject();
				// updateObj.put("$set", newFieldsObj);
				// Document newDoc = new Document(updateObj);
				// collection.updateOne(findquery, newDoc);
			}

		} catch (Exception e) {
			logger.error("Exception in updating Transaction Status Collection ", e);
		}
	}

	@SuppressWarnings("static-access")
	public void getIciciImpsDuplicateOrderId(Fields fields) throws SystemException {
		try {

			MongoDatabase dbIns = null;
			String orderId = fields.get(FieldType.ORDER_ID.getName());
			String payId = fields.get(FieldType.PAY_ID.getName());
			String currencyString = fields.get(FieldType.CURRENCY_CODE.getName());
			String subMerchantId = "";
			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantId = user.getPayId();
				payId = user.getSuperMerchantId();
			}

			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			conditionList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			conditionList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			conditionList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyString));
			if (StringUtils.isNotBlank(subMerchantId)) {
				conditionList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}

			BasicDBObject query = new BasicDBObject("$and", conditionList);
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			if (cursor.hasNext()) {
				fields.put(FieldType.DUPLICATE_YN.getName(), "Y");
			} else {
				fields.put(FieldType.DUPLICATE_YN.getName(), "N");
			}
			cursor.close();

		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);

		}

	}

	@SuppressWarnings("static-access")
	public boolean validateCapturedPgRef(String pgRefNum) throws SystemException {

		try {

			MongoDatabase dbIns = null;
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			BasicDBObject pgRefquery = new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum);
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			BasicDBObject statusTypeQuery = new BasicDBObject(FieldType.STATUS.getName(),
					StatusType.CAPTURED.getName());

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(pgRefquery);
			condList.add(txnTypeQuery);
			condList.add(statusTypeQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", condList);

			long count = collection.count(finalQuery);

			if (count > 0) {
				logger.info("Transaction already captured with PG REF NUM" + pgRefNum);
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			logger.error("Exception : ", e);
		}
		return false;
	}

	@SuppressWarnings("static-access")
	public HashMap<String, String> getENachRegistrationByTxnId(Fields fields) throws SystemException {

		HashMap<String, String> hm = new HashMap<String, String>();
		try {

			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));
			queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "Registration"));
			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

			// for sub merchant
			if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), user.getSuperMerchantId()));
				queryList.add(
						new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName())));

			} else if (user.isSuperMerchant() == true && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				// for Super Merchant
				queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
				queryList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
						fields.get(FieldType.SUB_MERCHANT_ID.getName())));

			} else {
				// for merchant
				queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
			}

			BasicDBObject finalQuery = new BasicDBObject("$and", queryList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(finalQuery).iterator();
			try {
				while (cursor.hasNext()) {

					Document doc = cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						doc = dataEncDecTool.decryptDocument(doc);
					}

					hm.put("TOKEN", doc.getString("TOKEN"));
					hm.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
					hm.put("START_DATE", doc.getString(FieldType.DATEFROM.getName()));
					hm.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
				}
			} finally {
				cursor.close();
			}
			return hm;
		} catch (Exception exception) {
			String message = "Error fetching registrationDetail from txnId from database";
			logger.error(message, exception);
			return hm;
		}
	}

	@SuppressWarnings("static-access")
	public HashMap<String, String> getENachTransactionDetailsByOrderId(Fields fields) throws SystemException {

		HashMap<String, String> processingDetails = new HashMap<String, String>();
		try {

			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

			queryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PROCESSING.getName()));
			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));
			queryList
					.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName())));

			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

			// for sub merchant
			if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), user.getSuperMerchantId()));
				queryList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId()));
			} else {
				// for merchant
				queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
			}

			BasicDBObject finalQuery = new BasicDBObject("$and", queryList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(finalQuery).iterator();
			try {
				while (cursor.hasNext()) {

					Document doc = cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						doc = dataEncDecTool.decryptDocument(doc);
					}

					processingDetails.put(FieldType.DEBIT_DATE.getName(),
							doc.getString(FieldType.DEBIT_DATE.getName()));
					processingDetails.put(FieldType.PG_REF_NUM.getName(),
							doc.getString(FieldType.PG_REF_NUM.getName()));
					processingDetails.put(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName()));
					processingDetails.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
					processingDetails.put(FieldType.REGISTRATION_DATE.getName(),
							doc.getString(FieldType.REGISTRATION_DATE.getName()));

					processingDetails.put("COM_AMT", doc.getString("COM_AMT"));
					processingDetails.put(FieldType.CUST_PHONE.getName(),
							doc.getString(FieldType.CUST_PHONE.getName()));
					processingDetails.put(FieldType.CUST_EMAIL.getName(),
							doc.getString(FieldType.CUST_EMAIL.getName()));
					processingDetails.put(FieldType.AMOUNT_TYPE.getName(),
							doc.getString(FieldType.AMOUNT_TYPE.getName()));
					processingDetails.put(FieldType.FREQUENCY.getName(), doc.getString(FieldType.FREQUENCY.getName()));
					processingDetails.put(FieldType.ORIG_TXN_ID.getName(),
							doc.getString(FieldType.ORIG_TXN_ID.getName()));
					processingDetails.put("MANDATE_REGISTRATION_ID", doc.getString("MANDATE_REGISTRATION_ID"));
					processingDetails.put(FieldType.TENURE.getName(), doc.getString(FieldType.TENURE.getName()));
					processingDetails.put(FieldType.ACQUIRER_NAME.getName(),
							doc.getString(FieldType.ACQUIRER_NAME.getName()));
					processingDetails.put(FieldType.DUE_DATE.getName(), doc.getString(FieldType.DUE_DATE.getName()));
					processingDetails.put(FieldType.UMRN_NUMBER.getName(),
							doc.getString(FieldType.UMRN_NUMBER.getName()));

					processingDetails.put(FieldType.ACCOUNT_NO.toString(),
							doc.getString(FieldType.ACCOUNT_NO.toString()));
					processingDetails.put(FieldType.IFSC_CODE.getName(), doc.getString(FieldType.IFSC_CODE.getName()));
					processingDetails.put(FieldType.ACCOUNT_HOLDER_NAME.getName(),
							doc.getString(FieldType.ACCOUNT_HOLDER_NAME.getName()));
					processingDetails.put(FieldType.ACCOUNT_TYPE.getName(),
							doc.getString(FieldType.ACCOUNT_TYPE.getName()));

					processingDetails.put(FieldType.TXNTYPE.getName(), doc.getString(FieldType.TXNTYPE.getName()));

					if (doc.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						processingDetails.put(FieldType.SUB_MERCHANT_ID.getName(),
								doc.getString(FieldType.SUB_MERCHANT_ID.getName()));
					}

					if (doc.containsKey(FieldType.RESELLER_ID.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_ID.getName()))) {
						processingDetails.put(FieldType.RESELLER_ID.getName(),
								doc.getString(FieldType.RESELLER_ID.getName()));
					}
					processingDetails.put(FieldType.DATEFROM.getName(), doc.getString(FieldType.DATEFROM.getName()));
					processingDetails.put(FieldType.DATETO.getName(), doc.getString(FieldType.DATETO.getName()));
					processingDetails.put(FieldType.CURRENCY.getName(), doc.getString(FieldType.CURRENCY.getName()));

					processingDetails.put(Constants.AMOUNT.getValue(),
							String.valueOf(new BigDecimal(doc.getString(FieldType.MONTHLY_AMOUNT.getName())).setScale(2,
									BigDecimal.ROUND_HALF_UP)));

					processingDetails.put(FieldType.MONTHLY_AMOUNT.getName(),
							doc.getString(FieldType.MONTHLY_AMOUNT.getName()));

					processingDetails.put(FieldType.TOTAL_AMOUNT.getName(),
							doc.getString(FieldType.TOTAL_AMOUNT.getName()));
					processingDetails.put(FieldType.PAYMENT_TYPE.getName(),
							doc.getString(FieldType.PAYMENT_TYPE.getName()));
					processingDetails.put(FieldType.CREATE_DATE.getName(),
							doc.getString(FieldType.CREATE_DATE.getName()));

				}
			} finally {
				cursor.close();
			}
			return processingDetails;
		} catch (Exception exception) {
			String message = "Error fetching registrationDetail from txnId from database";
			logger.error(message, exception);
			return processingDetails;
		}
	}

	@SuppressWarnings("static-access")
	public HashMap<String, String> getENachRegistrationDetailsByOrderId(Fields fields) {

		HashMap<String, String> processingDetails = new HashMap<String, String>();
		try {

			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

			queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "Registration"));
			queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));

			BasicDBObject finalQuery = new BasicDBObject("$and", queryList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(finalQuery).iterator();
			try {

				Date preDate = null;
				if (!cursor.hasNext()) {
					processingDetails.put(FieldType.STATUS.getName(), "FAIL");
					processingDetails.put(FieldType.RESPONSE_MESSAGE.getName(), "No Registration found");

				} else {
					while (cursor.hasNext()) {
						Document doc = cursor.next();

						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
							doc = dataEncDecTool.decryptDocument(doc);
						}
						Date createDate = DateCreater
								.convertStringToDateTime(doc.getString(FieldType.CREATE_DATE.getName()));

						if (preDate == null || preDate.before(createDate)) {
							processingDetails = new HashMap<String, String>();
							Set<String> set = doc.keySet();
							set.remove("_id");
							for (String key : set) {
								processingDetails.put(key, doc.getString(key));
							}
						}
						preDate = createDate;
					}
				}
			} finally {
				cursor.close();
			}
			return processingDetails;
		} catch (Exception exception) {
			logger.error("Error fetching registration detail from database", exception);
			processingDetails.put(FieldType.RESPONSE_MESSAGE.getName(), "Error while fetching registration details");
			return processingDetails;
		}
	}

	@SuppressWarnings("static-access")
	public void insertEnachRegistrationDetail(Fields registrationDetails) throws SystemException {

		logger.info("inside insertEnachRegistrationDetail to insert registration details For eMandate sign ");
		try {
			BasicDBObject newFieldsObj = new BasicDBObject();

			newFieldsObj.put(FieldType.TXNTYPE.getName(), registrationDetails.get(FieldType.TXNTYPE.getName()));
			newFieldsObj.put(FieldType.PAY_ID.getName(), registrationDetails.get(FieldType.PAY_ID.getName()));
			newFieldsObj.put(FieldType.DATEFROM.getName(), registrationDetails.get("DEBIT_START_DATE"));
			newFieldsObj.put(FieldType.DATETO.getName(), registrationDetails.get("END_DATE"));
			newFieldsObj.put(FieldType.CURRENCY.getName(), registrationDetails.get(CrmFieldType.CURRENCY.getName()));
			newFieldsObj.put(Constants.AMOUNT.getValue(),
					String.valueOf(new BigDecimal(registrationDetails.get(FieldType.AMOUNT.getName())).setScale(2,
							BigDecimal.ROUND_HALF_UP)));
			newFieldsObj.put(FieldType.MONTHLY_AMOUNT.getName(), registrationDetails.get("TRANSACTION_AMOUNT"));
			newFieldsObj.put(FieldType.TOTAL_AMOUNT.getName(),
					registrationDetails.get(FieldType.TOTAL_AMOUNT.getName()));

			if (StringUtils.isNotBlank(registrationDetails.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				newFieldsObj.put(FieldType.SUB_MERCHANT_ID.getName(),
						registrationDetails.get(FieldType.SUB_MERCHANT_ID.getName()));
			}

			if (StringUtils.isNotBlank(registrationDetails.get(FieldType.RESELLER_ID.getName()))) {
				newFieldsObj.put(FieldType.RESELLER_ID.getName(),
						registrationDetails.get(FieldType.RESELLER_ID.getName()));
			}

			if (StringUtils.isNotBlank(registrationDetails.get("PAYMENT_MODE"))
					&& !registrationDetails.get("PAYMENT_MODE").equalsIgnoreCase(Constants.NA.getValue())) {
				if (registrationDetails.get("PAYMENT_MODE").equalsIgnoreCase("netBanking")) {
					newFieldsObj.put(FieldType.PAYMENT_TYPE.getName(), "NB");
				} else {
					newFieldsObj.put(FieldType.PAYMENT_TYPE.getName(), "DC");
				}

			} else {
				newFieldsObj.put(FieldType.PAYMENT_TYPE.getName(), registrationDetails.get("PAYMENT_MODE"));
			}

			if (StringUtils.isNotBlank(registrationDetails.get("MERCHANT_LOGO"))) {
				newFieldsObj.put("MERCHANT_LOGO", registrationDetails.get("MERCHANT_LOGO"));
			}

			newFieldsObj.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));
			if (registrationDetails.contains(FieldType.STATUS.getName())
					&& StringUtils.isNotBlank(registrationDetails.get(FieldType.STATUS.getName()))) {
				newFieldsObj.put(FieldType.STATUS.getName(), registrationDetails.get(FieldType.STATUS.getName()));
			} else {
				newFieldsObj.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
			}

			newFieldsObj.put("COM_AMT", registrationDetails.get("COM_AMT"));

			newFieldsObj.put(FieldType.CUST_PHONE.getName(), registrationDetails.get("CONSUMER_MOBILE_NO"));
			newFieldsObj.put(FieldType.CUST_EMAIL.getName(), registrationDetails.get("CONSUMER_EMAIL_ID"));
			newFieldsObj.put(FieldType.AMOUNT_TYPE.getName(), registrationDetails.get("AMOUNT_TYPE"));
			newFieldsObj.put(FieldType.FREQUENCY.getName(), registrationDetails.get("FREQUENCY"));
			newFieldsObj.put(FieldType.ORDER_ID.getName(), registrationDetails.get("CONSUMER_ID"));
			newFieldsObj.put(FieldType.TXN_ID.getName(), registrationDetails.get(FieldType.TXN_ID.getName()));
			newFieldsObj.put(FieldType.PG_REF_NUM.getName(), registrationDetails.get(FieldType.PG_REF_NUM.getName()));
			newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(), registrationDetails.get(FieldType.PG_REF_NUM.getName()));
			newFieldsObj.put(FieldType.EMANDATE_URL.getName(),
					registrationDetails.get(FieldType.EMANDATE_URL.getName()));
			newFieldsObj.put("_id", registrationDetails.get(FieldType.PG_REF_NUM.getName()));

			// For net Banking
			if (registrationDetails.contains("ACCOUNT_NUMBER")
					&& StringUtils.isNotBlank(registrationDetails.get("ACCOUNT_NUMBER"))) {
				newFieldsObj.put(FieldType.ACCOUNT_NO.toString(), registrationDetails.get("ACCOUNT_NUMBER"));
			} else {
				newFieldsObj.put(FieldType.ACCOUNT_NO.toString(), Constants.NA.getValue());
			}

			if (registrationDetails.contains(CrmFieldType.IFSC_CODE.getName())
					&& StringUtils.isNotBlank(registrationDetails.get(CrmFieldType.IFSC_CODE.getName()))) {
				newFieldsObj.put(FieldType.IFSC_CODE.getName(),
						registrationDetails.get(CrmFieldType.IFSC_CODE.getName()));
			} else {
				newFieldsObj.put(FieldType.IFSC_CODE.toString(), Constants.NA.getValue());
			}

			if (registrationDetails.contains(FieldType.ACCOUNT_HOLDER_NAME.getName())
					&& StringUtils.isNotBlank(registrationDetails.get(FieldType.ACCOUNT_HOLDER_NAME.getName()))) {
				newFieldsObj.put(FieldType.ACCOUNT_HOLDER_NAME.getName(),
						registrationDetails.get(FieldType.ACCOUNT_HOLDER_NAME.getName()));
			} else {
				newFieldsObj.put(FieldType.ACCOUNT_HOLDER_NAME.toString(), Constants.NA.getValue());
			}

			if (registrationDetails.contains(FieldType.ACCOUNT_TYPE.getName())
					&& StringUtils.isNotBlank(registrationDetails.get(FieldType.ACCOUNT_TYPE.getName()))) {
				newFieldsObj.put(FieldType.ACCOUNT_TYPE.getName(),
						registrationDetails.get(FieldType.ACCOUNT_TYPE.getName()));
			} else {
				newFieldsObj.put(FieldType.ACCOUNT_TYPE.toString(), Constants.NA.getValue());
			}

			newFieldsObj.put(FieldType.TENURE.getName(), registrationDetails.get(FieldType.TENURE.getName()));
			if (StringUtils.isNotBlank(registrationDetails.get(FieldType.BANK_NAME.getName()))) {
				newFieldsObj.put(FieldType.BANK_NAME.getName(), registrationDetails.get(FieldType.BANK_NAME.getName()));
			} else {
				newFieldsObj.put(FieldType.BANK_NAME.getName(), Constants.NA.getValue());
			}

			newFieldsObj.put("MERCHANT_RETURN_URL", registrationDetails.get("MERCHANT_RETURN_URL"));
			if (registrationDetails.contains(FieldType.RESPONSE_MESSAGE.getName())
					&& StringUtils.isNotBlank(registrationDetails.get(FieldType.RESPONSE_MESSAGE.getName()))) {
				newFieldsObj.put(FieldType.RESPONSE_MESSAGE.getName(),
						registrationDetails.get(FieldType.RESPONSE_MESSAGE.getName()));
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));

			Document doc = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			// collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error insert registrationDetail to database";
			logger.error(message, exception);
		}
	}

	@SuppressWarnings("static-access")
	public void insertDebitTransactionDetail(Map<String, String> debitTransactionDetails, Fields registrationDetail)
			throws SystemException {

		logger.info("inside insertDebitTransactionDetail to insert debit details For eMandate debit transaction ");
		try {
			Fields callbackFields = new Fields(registrationDetail);

			BasicDBObject newFieldsObj = new BasicDBObject();
			String newId = TransactionManager.getNewTransactionId();

			newFieldsObj.put("_id", newId);
			newFieldsObj.put(FieldType.TXNTYPE.getName(), registrationDetail.get(FieldType.TXNTYPE.getName()));
			newFieldsObj.put(FieldType.PAY_ID.getName(), registrationDetail.get(FieldType.PAY_ID.getName()));
			if (registrationDetail.contains(FieldType.SUB_MERCHANT_ID.getName())
					&& StringUtils.isNotBlank(registrationDetail.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				newFieldsObj.put(FieldType.SUB_MERCHANT_ID.getName(),
						registrationDetail.get(FieldType.SUB_MERCHANT_ID.getName()));
			}

			if (registrationDetail.contains(FieldType.RESELLER_ID.getName())
					&& StringUtils.isNotBlank(registrationDetail.get(FieldType.RESELLER_ID.getName()))) {
				newFieldsObj.put(FieldType.RESELLER_ID.getName(),
						registrationDetail.get(FieldType.RESELLER_ID.getName()));
			}
			newFieldsObj.put(FieldType.DATEFROM.getName(), registrationDetail.get(FieldType.DATEFROM.getName()));
			newFieldsObj.put(FieldType.DATETO.getName(), registrationDetail.get(FieldType.DATETO.getName()));
			newFieldsObj.put(FieldType.CURRENCY.getName(), registrationDetail.get(FieldType.CURRENCY.getName()));

			newFieldsObj.put(Constants.AMOUNT.getValue(),
					String.valueOf(new BigDecimal(registrationDetail.get(FieldType.MONTHLY_AMOUNT.getName()))
							.setScale(2, BigDecimal.ROUND_HALF_UP)));

			newFieldsObj.put(FieldType.MONTHLY_AMOUNT.getName(),
					registrationDetail.get(FieldType.MONTHLY_AMOUNT.getName()));
			newFieldsObj.put(FieldType.DEBIT_DATE.getName(), registrationDetail.get(FieldType.DEBIT_DATE.getName()));

			newFieldsObj.put(FieldType.TOTAL_AMOUNT.getName(),
					registrationDetail.get(FieldType.TOTAL_AMOUNT.getName()));

			newFieldsObj.put(FieldType.PAYMENT_TYPE.getName(),
					registrationDetail.get(FieldType.PAYMENT_TYPE.getName()));
			newFieldsObj.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));

			if (debitTransactionDetails.get("responseCode").equalsIgnoreCase("0300")) {
				newFieldsObj.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
				newFieldsObj.put(FieldType.PG_RESP_CODE.getName(), debitTransactionDetails.get("responseCode"));
				newFieldsObj.put(FieldType.PG_RESPONSE_MSG.getName(), debitTransactionDetails.get("response"));
				newFieldsObj.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
				newFieldsObj.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

				callbackFields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
				callbackFields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
				callbackFields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

			} else if (debitTransactionDetails.get("responseCode").equalsIgnoreCase("0398")) {
				newFieldsObj.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
				newFieldsObj.put(FieldType.PG_RESP_CODE.getName(), debitTransactionDetails.get("responseCode"));
				newFieldsObj.put(FieldType.PG_RESPONSE_MSG.getName(), debitTransactionDetails.get("response"));
				newFieldsObj.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PROCESSING.getCode());
				newFieldsObj.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PROCESSING.getResponseMessage());

				callbackFields.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
				callbackFields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PROCESSING.getCode());
				callbackFields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PROCESSING.getResponseMessage());

			} else {
				newFieldsObj.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				newFieldsObj.put(FieldType.PG_RESP_CODE.getName(), debitTransactionDetails.get("responseCode"));
				newFieldsObj.put(FieldType.PG_RESPONSE_MSG.getName(), debitTransactionDetails.get("response"));
				newFieldsObj.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				newFieldsObj.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());

				callbackFields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				callbackFields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				callbackFields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());

			}
			newFieldsObj.put(FieldType.REGISTRATION_DATE.getName(),
					registrationDetail.get(FieldType.REGISTRATION_DATE.getName()));

			newFieldsObj.put("COM_AMT", registrationDetail.get("COM_AMT"));

			newFieldsObj.put(FieldType.CUST_PHONE.getName(), registrationDetail.get(FieldType.CUST_PHONE.getName()));
			newFieldsObj.put(FieldType.CUST_EMAIL.getName(), registrationDetail.get(FieldType.CUST_EMAIL.getName()));
			newFieldsObj.put(FieldType.AMOUNT_TYPE.getName(), registrationDetail.get(FieldType.AMOUNT_TYPE.getName()));
			newFieldsObj.put(FieldType.FREQUENCY.getName(), registrationDetail.get(FieldType.FREQUENCY.getName()));
			newFieldsObj.put(FieldType.ORDER_ID.getName(), registrationDetail.get(FieldType.ORDER_ID.getName()));
			newFieldsObj.put(FieldType.TXN_ID.getName(), newId);
			newFieldsObj.put(FieldType.PG_REF_NUM.getName(), debitTransactionDetails.get("pgRefNum"));
			newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(), registrationDetail.get(FieldType.ORIG_TXN_ID.getName()));
			newFieldsObj.put("MANDATE_REGISTRATION_ID", registrationDetail.get("MANDATE_REGISTRATION_ID"));
			newFieldsObj.put(FieldType.TENURE.getName(), registrationDetail.get(FieldType.TENURE.getName()));
			newFieldsObj.put(FieldType.BANK_NAME.getName(), registrationDetail.get(FieldType.BANK_NAME.getName()));
			newFieldsObj.put(FieldType.DUE_DATE.getName(), registrationDetail.get(FieldType.DUE_DATE.getName()));
			newFieldsObj.put(FieldType.UMRN_NUMBER.getName(), registrationDetail.get(FieldType.UMRN_NUMBER.getName()));
			newFieldsObj.put(FieldType.ACCOUNT_NO.toString(), registrationDetail.get(FieldType.ACCOUNT_NO.toString()));
			newFieldsObj.put(FieldType.IFSC_CODE.getName(), registrationDetail.get(FieldType.IFSC_CODE.getName()));
			newFieldsObj.put(FieldType.ACCOUNT_HOLDER_NAME.getName(),
					registrationDetail.get(FieldType.ACCOUNT_HOLDER_NAME.getName()));
			newFieldsObj.put(FieldType.ACCOUNT_TYPE.getName(),
					registrationDetail.get(FieldType.ACCOUNT_TYPE.getName()));

			sendCallbackForENachDebitTxn(callbackFields);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));

			/*
			 * Document update = new Document(); update.put("$set", newFieldsObj);
			 * collection.updateOne(queryForUpdate, update);
			 */

			Document insertPendingDebitTxn = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(insertPendingDebitTxn));
			} else {
				collection.insertOne(insertPendingDebitTxn);
			}
			// collection.insertOne(insertPendingDebitTxn);

			logger.info("debit transaction detail insert successfully " + newFieldsObj);
		} catch (Exception exception) {
			String message = "Error while insert debit transaction details in database";
			logger.error(message, exception);
		}
	}

	private void sendCallbackForENachDebitTxn(Fields fields) {
		JsonObject json = new JsonObject();
		User user = new User();

		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			user = userDao.findPayId(fields.get(FieldType.SUB_MERCHANT_ID.getName()));
		} else {
			user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		}

		UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(user.getPayId());

		try {

			Date date = new Date();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String strDate = df.format(date);

			Fields newFields = new Fields();

			Collection<String> callbackFields = SystemProperties.geteNachDebitTxnCallbackfields();
			for (String key : callbackFields) {
				if (StringUtils.isNotBlank(fields.get(key))) {
					newFields.put(key, fields.get(key));
				}
			}

			newFields.put(FieldType.RESPONSE_DATE_TIME.getName(), strDate);
			String hash = Hasher.getHash(newFields);
			newFields.put("HASH", hash);

			List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				json.addProperty(fieldType, newFields.get(fieldType));
			}

			String serviceUrl = merchantSettings.getCallBackUrl();
			if (!StringUtils.isNotBlank(serviceUrl))
				serviceUrl = propertiesManager.propertiesMap.get("ENACHDEBIT_CALLBACK_URL");

			logger.info("Callback url: " + serviceUrl);
			HttpPost request = new HttpPost(serviceUrl);

			int CONNECTION_TIMEOUT_MS = 30 * 1000;
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
					.setConnectTimeout(CONNECTION_TIMEOUT_MS).setSocketTimeout(CONNECTION_TIMEOUT_MS).build();

			request.setConfig(requestConfig);

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();

			logger.info("Callback Request for PG_REF_NUM " + newFields.get(FieldType.PG_REF_NUM.getName()) + " : "
					+ json.toString());
			StringEntity params = new StringEntity(json.toString());

			request.addHeader("content-type", "application/json");
			request.setEntity(params);

			HttpResponse resp = httpClient.execute(request);
			HttpEntity response = resp.getEntity();
			String responseBody = EntityUtils.toString(response);
			logger.info("Callback Response from eNach :" + responseBody.toString());

		} catch (Exception e) {
			logger.error(
					"Exception in sending eNach call back response for RRN == " + fields.get(FieldType.RRN.getName())
							+ " and PG Ref Num == " + fields.get(FieldType.PG_REF_NUM.getName()),
					e);
		}
	}

	@SuppressWarnings("static-access")
	public void updateENachRegistrationDetailByTxnId(Map<String, String> updatedFields, Fields fields)
			throws SystemException {

		try {
			logger.info("inside fieldsDao for update Enach Registration Details ");
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));

			List<BasicDBObject> queryForUpdate = new ArrayList<BasicDBObject>();

			queryForUpdate
					.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));
			queryForUpdate.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "Registration"));

			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

			// for sub merchant
			if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				queryForUpdate.add(new BasicDBObject(FieldType.PAY_ID.getName(), user.getSuperMerchantId()));
				queryForUpdate.add(
						new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
			} else if (user.isSuperMerchant() == true && StringUtils.isNotBlank(user.getSuperMerchantId())) {

				queryForUpdate
						.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
				queryForUpdate.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
						fields.get(FieldType.SUB_MERCHANT_ID.getName())));

			} else {
				// for merchant
				queryForUpdate
						.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
			}

			// queryForUpdate.add(new BasicDBObject(FieldType.PAY_ID.getName(),
			// fields.get(FieldType.PAY_ID.getName())));
			/*
			 * queryForUpdate.add(new BasicDBObject(FieldType.AMOUNT.getName(),
			 * String.valueOf( new
			 * BigDecimal(fields.get(FieldType.AMOUNT.getName())).setScale(2,
			 * BigDecimal.ROUND_HALF_UP))));
			 */

			BasicDBObject conditionForupdate = new BasicDBObject();
			conditionForupdate.put("$and", queryForUpdate);

			Document setData = new Document();
			setData.put(FieldType.STATUS.getName(), updatedFields.get(FieldType.STATUS.getName()));
			setData.put(FieldType.RESPONSE_CODE.getName(), updatedFields.get(FieldType.RESPONSE_CODE.getName()));
			setData.put(FieldType.RESPONSE_MESSAGE.getName(), updatedFields.get(FieldType.RESPONSE_MESSAGE.getName()));
			setData.put(FieldType.UPDATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));

			Document update = new Document();
			update.put("$set", setData);
			collection.updateOne(conditionForupdate, update);

		} catch (Exception exception) {
			String message = "Error fetching registrationDetail from database";
			logger.error(message, exception);
		}
	}

	@SuppressWarnings("static-access")
	public void updateENachRegistrationDetailByResponse(Map<String, String> updatedFields, String txnId)
			throws SystemException {

		try {
			logger.info("inside fieldsDao for update Enach Registration Details for response ");
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));

			Document queryForUpdate = new Document();
			queryForUpdate.put(FieldType.PG_REF_NUM.getName(), txnId);

			Document setData = new Document();
			if (updatedFields.get("responseCode").equalsIgnoreCase("0300")) {
				setData.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
				setData.put(FieldType.PG_RESP_CODE.getName(), updatedFields.get("responseCode"));
				setData.put(FieldType.PG_RESPONSE_MSG.getName(),
						ErrorType.SUCCESS.getResponseMessage()/*
																 * updatedFields.get( "responseMessage")
																 */);
				setData.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
				setData.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

			} else if (updatedFields.get("responseCode").equalsIgnoreCase("0398")
					|| updatedFields.get("responseCode").equalsIgnoreCase("0396")) {
				setData.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
				setData.put(FieldType.PG_RESP_CODE.getName(), updatedFields.get("responseCode"));
				setData.put(FieldType.PG_RESPONSE_MSG.getName(), updatedFields.get("responseMessage"));
				setData.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PENDING.getCode());
				setData.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PENDING.getResponseMessage());
				setData.put(FieldType.EMANDATE_URL.getName(), updatedFields.get(FieldType.EMANDATE_URL.getName()));

			} else if (updatedFields.get("responseCode").equalsIgnoreCase("0399")) {
				setData.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				setData.put(FieldType.PG_RESP_CODE.getName(), updatedFields.get("responseCode"));
				setData.put(FieldType.PG_RESPONSE_MSG.getName(), updatedFields.get("responseMessage"));
				setData.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				setData.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());

			} else if (updatedFields.get("responseCode").equalsIgnoreCase("0392")) {
				setData.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				setData.put(FieldType.PG_RESP_CODE.getName(), updatedFields.get("responseCode"));
				setData.put(FieldType.PG_RESPONSE_MSG.getName(), updatedFields.get("responseMessage"));
				setData.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				setData.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());

			} else {
				setData.put(FieldType.STATUS.getName(), updatedFields.get("response"));
				setData.put(FieldType.PG_RESP_CODE.getName(), updatedFields.get("responseCode"));
				setData.put(FieldType.PG_RESPONSE_MSG.getName(), updatedFields.get("responseMessage"));
				setData.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				setData.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
			}
			if (updatedFields.containsKey("UMRNNumber") && StringUtils.isNotBlank(updatedFields.get("UMRNNumber"))) {
				setData.put("UMRN_NUMBER", updatedFields.get("UMRNNumber"));
			} else {
				setData.put("UMRN_NUMBER", Constants.NA.getValue());
			}

			if (updatedFields.containsKey("MANDATE_REGISTRATION_ID")
					&& StringUtils.isNotBlank(updatedFields.get("MANDATE_REGISTRATION_ID"))) {
				setData.put("MANDATE_REGISTRATION_ID", updatedFields.get("MANDATE_REGISTRATION_ID"));
			} else {
				setData.put("MANDATE_REGISTRATION_ID", Constants.NA.getValue());
			}
			// setData.put(FieldType.RESPONSE_MESSAGE.getName(),
			// updatedFields.get("responseMessage"));
			setData.put(FieldType.UPDATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));

			Document update = new Document();
			update.put("$set", setData);
			collection.updateOne(queryForUpdate, update);

		} catch (Exception exception) {
			String message = "Error fetching registrationDetail from pgRefNum from database for response";
			logger.error(message, exception);
		}
	}

	@SuppressWarnings("static-access")
	public void updateENachTransactionDetailByStatusEnquiry(Map<String, String> updatedFields,
			Map<String, String> processingDetails) throws SystemException {
		boolean getStatus = true;

		try {
			logger.info("inside fieldsDao for update Enach Registration Details for response ");
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));

			List<BasicDBObject> queryForCaptured = new ArrayList<BasicDBObject>();
			queryForCaptured.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(),
					processingDetails.get(FieldType.PG_REF_NUM.getName())));
			queryForCaptured.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			BasicDBObject capturedQuery = new BasicDBObject("$and", queryForCaptured);

			List<BasicDBObject> queryForSettled = new ArrayList<BasicDBObject>();
			queryForSettled.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(),
					processingDetails.get(FieldType.PG_REF_NUM.getName())));
			queryForSettled.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			BasicDBObject settledQuery = new BasicDBObject("$and", queryForSettled);

			List<BasicDBObject> queryForFailed = new ArrayList<BasicDBObject>();
			queryForFailed.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(),
					processingDetails.get(FieldType.PG_REF_NUM.getName())));
			queryForFailed.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
			BasicDBObject failedQuery = new BasicDBObject("$and", queryForFailed);

			List<BasicDBObject> debitTransList = new ArrayList<BasicDBObject>();
			debitTransList.add(capturedQuery);
			debitTransList.add(settledQuery);
			debitTransList.add(failedQuery);

			BasicDBObject debitTransQuery = new BasicDBObject("$or", debitTransList);
			MongoCursor<Document> cursor = collection.find(debitTransQuery).iterator();
			while (cursor.hasNext()) {
				getStatus = false;
				break;
			}

			if (getStatus) {
				BasicDBObject newFieldsObj = new BasicDBObject();

				String newId = TransactionManager.getNewTransactionId();
				newFieldsObj.put("_id", newId);
				newFieldsObj.put(FieldType.TXNTYPE.getName(), processingDetails.get(FieldType.TXNTYPE.getName()));
				newFieldsObj.put(FieldType.PAY_ID.getName(), processingDetails.get(FieldType.PAY_ID.getName()));
				if (processingDetails.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(processingDetails.get(FieldType.SUB_MERCHANT_ID.getName()))) {
					newFieldsObj.put(FieldType.SUB_MERCHANT_ID.getName(),
							processingDetails.get(FieldType.SUB_MERCHANT_ID.getName()));
				}

				if (processingDetails.containsKey(FieldType.RESELLER_ID.getName())
						&& StringUtils.isNotBlank(processingDetails.get(FieldType.RESELLER_ID.getName()))) {
					newFieldsObj.put(FieldType.RESELLER_ID.getName(),
							processingDetails.get(FieldType.RESELLER_ID.getName()));
				}
				newFieldsObj.put(FieldType.DATEFROM.getName(), processingDetails.get(FieldType.DATEFROM.getName()));
				newFieldsObj.put(FieldType.DATETO.getName(), processingDetails.get(FieldType.DATETO.getName()));
				newFieldsObj.put(FieldType.CURRENCY.getName(), processingDetails.get(FieldType.CURRENCY.getName()));

				newFieldsObj.put(Constants.AMOUNT.getValue(),
						String.valueOf(new BigDecimal(processingDetails.get(FieldType.MONTHLY_AMOUNT.getName()))
								.setScale(2, BigDecimal.ROUND_HALF_UP)));

				newFieldsObj.put(FieldType.MONTHLY_AMOUNT.getName(),
						processingDetails.get(FieldType.MONTHLY_AMOUNT.getName()));
				newFieldsObj.put(FieldType.DEBIT_DATE.getName(), processingDetails.get(FieldType.DEBIT_DATE.getName()));

				newFieldsObj.put(FieldType.TOTAL_AMOUNT.getName(),
						processingDetails.get(FieldType.TOTAL_AMOUNT.getName()));

				newFieldsObj.put(FieldType.PAYMENT_TYPE.getName(),
						processingDetails.get(FieldType.PAYMENT_TYPE.getName()));
				newFieldsObj.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));

				// Document setData = new Document();
				if (updatedFields.get("responseCode").equalsIgnoreCase("0300")
						&& updatedFields.get("responseMessage").equalsIgnoreCase("S")) {
					newFieldsObj.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
					newFieldsObj.put(FieldType.PG_RESP_CODE.getName(), updatedFields.get("responseCode"));
					newFieldsObj.put(FieldType.PG_RESPONSE_MSG.getName(), updatedFields.get("responseMessage"));
					newFieldsObj.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
					newFieldsObj.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

				} else if (updatedFields.get("responseCode").equalsIgnoreCase("0300")
						|| updatedFields.get("responseMessage").equalsIgnoreCase("I")) {
					newFieldsObj.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
					newFieldsObj.put(FieldType.PG_RESP_CODE.getName(), updatedFields.get("responseCode"));
					newFieldsObj.put(FieldType.PG_RESPONSE_MSG.getName(), updatedFields.get("responseMessage"));
					newFieldsObj.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PROCESSING.getCode());
					newFieldsObj.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PROCESSING.getResponseMessage());

				} else if (updatedFields.get("responseCode").equalsIgnoreCase("0399")) {
					newFieldsObj.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					newFieldsObj.put(FieldType.PG_RESP_CODE.getName(), updatedFields.get("responseCode"));
					newFieldsObj.put(FieldType.PG_RESPONSE_MSG.getName(), updatedFields.get("response"));
					newFieldsObj.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
					newFieldsObj.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
				} else {
					newFieldsObj.put(FieldType.STATUS.getName(), updatedFields.get("response"));
					newFieldsObj.put(FieldType.PG_RESP_CODE.getName(), updatedFields.get("responseCode"));
					newFieldsObj.put(FieldType.PG_RESPONSE_MSG.getName(), updatedFields.get("response"));
					newFieldsObj.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
					newFieldsObj.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
				}
				if (updatedFields.containsKey("UMRNNumber")
						&& StringUtils.isNotBlank(updatedFields.get("UMRNNumber"))) {
					newFieldsObj.put("UMRN_NUMBER", updatedFields.get("UMRNNumber"));
				}

				if (updatedFields.containsKey("MANDATE_REGISTRATION_ID")
						&& StringUtils.isNotBlank("MANDATE_REGISTRATION_ID")) {
					newFieldsObj.put("MANDATE_REGISTRATION_ID", updatedFields.get("MANDATE_REGISTRATION_ID"));
				}
				// setData.put(FieldType.RESPONSE_MESSAGE.getName(),
				// updatedFields.get("responseMessage"));
				// setData.put(FieldType.UPDATE_DATE.getName(),
				// DateCreater.formatDateForDb(new Date()));

				newFieldsObj.put(FieldType.REGISTRATION_DATE.getName(),
						processingDetails.get(FieldType.REGISTRATION_DATE.getName()));
				newFieldsObj.put("COM_AMT", processingDetails.get("COM_AMT"));
				newFieldsObj.put(FieldType.CUST_PHONE.getName(), processingDetails.get(FieldType.CUST_PHONE.getName()));
				newFieldsObj.put(FieldType.CUST_EMAIL.getName(), processingDetails.get(FieldType.CUST_EMAIL.getName()));
				newFieldsObj.put(FieldType.AMOUNT_TYPE.getName(),
						processingDetails.get(FieldType.AMOUNT_TYPE.getName()));
				newFieldsObj.put(FieldType.FREQUENCY.getName(), processingDetails.get(FieldType.FREQUENCY.getName()));
				newFieldsObj.put(FieldType.ORDER_ID.getName(), processingDetails.get(FieldType.ORDER_ID.getName()));
				newFieldsObj.put(FieldType.TXN_ID.getName(), newId);
				newFieldsObj.put(FieldType.PG_REF_NUM.getName(), processingDetails.get(FieldType.PG_REF_NUM.getName()));
				newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(),
						processingDetails.get(FieldType.ORIG_TXN_ID.getName()));
				newFieldsObj.put("MANDATE_REGISTRATION_ID", processingDetails.get("MANDATE_REGISTRATION_ID"));
				newFieldsObj.put(FieldType.TENURE.getName(), processingDetails.get(FieldType.TENURE.getName()));
				newFieldsObj.put(FieldType.BANK_NAME.getName(), processingDetails.get(FieldType.BANK_NAME.getName()));
				newFieldsObj.put(FieldType.DUE_DATE.getName(), processingDetails.get(FieldType.DUE_DATE.getName()));
				newFieldsObj.put(FieldType.UMRN_NUMBER.getName(),
						processingDetails.get(FieldType.UMRN_NUMBER.getName()));
				newFieldsObj.put(FieldType.ACCOUNT_NO.toString(),
						processingDetails.get(FieldType.ACCOUNT_NO.toString()));
				newFieldsObj.put(FieldType.IFSC_CODE.getName(), processingDetails.get(FieldType.IFSC_CODE.getName()));
				newFieldsObj.put(FieldType.ACCOUNT_HOLDER_NAME.getName(),
						processingDetails.get(FieldType.ACCOUNT_HOLDER_NAME.getName()));
				newFieldsObj.put(FieldType.ACCOUNT_TYPE.getName(),
						processingDetails.get(FieldType.ACCOUNT_TYPE.getName()));

				/*
				 * Document update = new Document(); update.put("$set", setData);
				 * collection.updateOne(queryForUpdate, update);
				 */

				Document insertFinalDebitTxn = new Document(newFieldsObj);
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					collection.insertOne(dataEncDecTool.encryptDocument(insertFinalDebitTxn));
				} else {
					collection.insertOne(insertFinalDebitTxn);
				}
				// collection.insertOne(insertFinalDebitTxn);

			}

		} catch (Exception exception) {
			String message = "Error fetching transaction Detail by pgRefNum from database ";
			logger.error(message, exception);
		}
	}

	@SuppressWarnings("static-access")
	public HashMap<String, String> getEnachMandateDetailsByTxnId(String pgRefNum) {

		logger.info("inside getEnachMandateDetailsByTxnId get data from DB by pgRefNum " + pgRefNum);
		HashMap<String, String> mandateDetails = new HashMap<String, String>();
		try {
			BasicDBObject query = new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			try {
				while (cursor.hasNext()) {

					Document doc = cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						doc = dataEncDecTool.decryptDocument(doc);
					}

					if (doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(Constants.NA.getValue())) {
						mandateDetails.put("paymentMode", Constants.NA.getValue());
					} else {
						if (doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("NB")) {
							mandateDetails.put("paymentMode", "Net Banking");
						} else {
							mandateDetails.put("paymentMode", "Debit Card");
						}
					}
					mandateDetails.put("accountType", doc.getString(FieldType.ACCOUNT_TYPE.getName()));
					mandateDetails.put("accountNumber", doc.getString(FieldType.ACCOUNT_NO.toString()));
					mandateDetails.put("accountHolderName", doc.getString(FieldType.ACCOUNT_HOLDER_NAME.getName()));
					mandateDetails.put("ifscCode", doc.getString(FieldType.IFSC_CODE.getName()));

					/*
					 * if (doc.containsKey("MERCHANT_LOGO") &&
					 * StringUtils.isNotBlank(doc.getString("MERCHANT_LOGO"))) { hm.put("LOGO",
					 * doc.getString("MERCHANT_LOGO")); }
					 */

					if (doc.containsKey(FieldType.UMRN_NUMBER.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.UMRN_NUMBER.getName()))) {
						mandateDetails.put("umrnNumber", doc.getString(FieldType.UMRN_NUMBER.getName()));
					} else {
						mandateDetails.put("umrnNumber", Constants.NA.getValue());
					}

					if (doc.containsKey(FieldType.RESPONSE_MESSAGE.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESPONSE_MESSAGE.getName()))) {
						mandateDetails.put("responseMessage", doc.getString(FieldType.RESPONSE_MESSAGE.getName()));
					} else {
						mandateDetails.put("responseMessage", Constants.NA.getValue());
					}

					mandateDetails.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
					mandateDetails.put("bankName", doc.getString(FieldType.BANK_NAME.getName()));
					mandateDetails.put("mobileNumber", doc.getString(FieldType.CUST_PHONE.getName()));
					mandateDetails.put("emailId", doc.getString(FieldType.CUST_EMAIL.getName()));
					mandateDetails.put("amount", doc.getString(FieldType.AMOUNT.getName()));
					mandateDetails.put("maxAmount", doc.getString(FieldType.MONTHLY_AMOUNT.getName()));
					mandateDetails.put("totalAmount", doc.getString(FieldType.TOTAL_AMOUNT.getName()));
					mandateDetails.put("frequency", doc.getString(FieldType.FREQUENCY.getName()));
					mandateDetails.put("tenure", doc.getString(FieldType.TENURE.getName()));
					mandateDetails.put("startDate", doc.getString(FieldType.DATEFROM.getName()));
					mandateDetails.put("endDate", doc.getString(FieldType.DATETO.getName()));
					mandateDetails.put("returnUrl", doc.getString(FieldType.RETURN_URL.getName()));

					if (StringUtils.isNotBlank(doc.getString("MERCHANT_RETURN_URL"))) {
						mandateDetails.put("merchantReturnUrl", doc.getString("MERCHANT_RETURN_URL"));
					} else {
						mandateDetails.put("merchantReturnUrl", Constants.NA.getValue());
					}

					if (doc.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						mandateDetails.put("payId", doc.getString(FieldType.SUB_MERCHANT_ID.getName()));
						mandateDetails.put("merchantName",
								userDao.getBusinessNameByPayId(doc.getString(FieldType.SUB_MERCHANT_ID.getName())));
						mandateDetails.put("merchantEmail",
								userDao.getEmailIdByPayId(doc.getString(FieldType.SUB_MERCHANT_ID.getName())));
					} else {
						mandateDetails.put("payId", doc.getString(FieldType.PAY_ID.getName()));
						mandateDetails.put("merchantName",
								userDao.getBusinessNameByPayId(doc.getString(FieldType.PAY_ID.getName())));
						mandateDetails.put("merchantEmail",
								userDao.getEmailIdByPayId(doc.getString(FieldType.PAY_ID.getName())));
					}
					mandateDetails.put("logo", doc.getString("MERCHANT_LOGO"));
					mandateDetails.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
					mandateDetails.put("responseCode", doc.getString(FieldType.RESPONSE_CODE.getName()));
					mandateDetails.put("updatedDate", doc.getString(FieldType.UPDATE_DATE.getName()));
					mandateDetails.put("status", doc.getString(FieldType.STATUS.getName()));

				}
			} finally {
				cursor.close();
			}

		} catch (Exception exception) {
			String message = "Error fetching registrationDetail from txnId from database ";
			logger.error(message, exception);
		}
		return mandateDetails;
	}

	@SuppressWarnings("static-access")
	public HashMap<String, String> getEnachMandateDetailsByPgRefNum(String pgRefNum) {

		logger.info("inside getEnachMandateDetailsByTxnId get data from DB by pgRefNum ");
		HashMap<String, String> hm = new HashMap<String, String>();
		try {
			BasicDBObject query = new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			try {
				while (cursor.hasNext()) {

					Document doc = cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						doc = dataEncDecTool.decryptDocument(doc);
					}

					hm.put("paymentMode", doc.getString(FieldType.PAYMENT_TYPE.getName()));

					if (doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("NB")) {
						hm.put("accountType", doc.getString("ACCOUNT_TYPE"));
						hm.put("accountNumber", doc.getString(FieldType.ACCOUNT_NO.toString()));
						hm.put("accountHolderName", doc.getString("ACCOUNT_HOLDER_NAME"));
						hm.put("ifscCode", doc.getString(FieldType.IFSC_CODE.getName()));
					} else {
						hm.put("cardNumber", doc.getString(FieldType.CARD_MASK.getName()));
						hm.put("nameOnCard", doc.getString(FieldType.CARD_HOLDER_NAME.getName()));
					}

					if (doc.containsKey("MERCHANT_LOGO") && StringUtils.isNotBlank(doc.getString("MERCHANT_LOGO"))) {
						hm.put("LOGO", doc.getString("MERCHANT_LOGO"));
					}

					if (doc.containsKey("UMRN_NUMBER") && StringUtils.isNotBlank(doc.getString("UMRN_NUMBER"))) {
						hm.put("umrnNumber", doc.getString("UMRN_NUMBER"));
					} else {
						hm.put("umrnNumber", Constants.NA.getValue());
					}

					if (doc.containsKey(FieldType.RESPONSE_MESSAGE.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESPONSE_MESSAGE.getName()))) {
						hm.put("responseMessage", doc.getString(FieldType.RESPONSE_MESSAGE.getName()));
					} else {
						hm.put("responseMessage", Constants.NA.getValue());
					}

					hm.put("bankName", doc.getString(FieldType.BANK_NAME.getName()));
					hm.put("mobileNumber", doc.getString(FieldType.CUST_PHONE.getName()));
					hm.put("emailId", doc.getString(FieldType.CUST_EMAIL.getName()));
					hm.put("amount", doc.getString(FieldType.AMOUNT.getName()));
					hm.put("maxAmount", doc.getString(FieldType.MONTHLY_AMOUNT.getName()));
					hm.put("totalAmount", doc.getString(FieldType.TOTAL_AMOUNT.getName()));
					hm.put("frequency", doc.getString("FREQUENCY"));
					hm.put("tenure", doc.getString("TENURE"));
					hm.put("startDate", doc.getString(FieldType.DATEFROM.getName()));
					hm.put("endDate", doc.getString(FieldType.DATETO.getName()));
					hm.put("returnUrl", doc.getString(FieldType.RETURN_URL.getName()));

					hm.put("merchantReturnUrl", doc.getString("MERCHANT_RETURN_URL"));
					hm.put("payId", doc.getString(FieldType.PAY_ID.getName()));
					hm.put("merchantName", userDao.getBusinessNameByPayId(doc.getString(FieldType.PAY_ID.getName())));
					hm.put("merchantEmail", userDao.getEmailIdByPayId(doc.getString(FieldType.PAY_ID.getName())));
					hm.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
					hm.put("responseCode", doc.getString(FieldType.RESPONSE_CODE.getName()));
					hm.put("updatedDate", doc.getString(FieldType.UPDATE_DATE.getName()));
					hm.put("status", doc.getString(FieldType.STATUS.getName()));

				}
			} finally {
				cursor.close();
			}

		} catch (Exception exception) {
			String message = "Error fetching registrationDetail from txnId from database ";
			logger.error(message, exception);
		}
		return hm;
	}

	@SuppressWarnings("static-access")
	public void updateUTRAndPayoutDate(Fields fields, String response) throws SystemException {
		logger.info("Inside updateUTRAndPayoutDate : ");
		try {
			String utr = null;
			if (response == null) {
				utr = fields.get(FieldType.RRN.getName());
			} else {
				JSONObject responseJson = new JSONObject(response);

				if (responseJson.has("UTRNUMBER")) {
					utr = responseJson.getString("UTRNUMBER");
				}
			}
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			Set<String> OidSet = getAllDataForAMerchant(fields);
			List<Fields> fieldsList = getSettledTxnByOID(OidSet);

			for (Fields preFields : fieldsList) {

				BasicDBObject oldFieldsObj = new BasicDBObject();
				oldFieldsObj.put(FieldType.TXN_ID.getName(), preFields.get(FieldType.TXN_ID.getName()));
				BasicDBObject newFieldsObj = new BasicDBObject();
				for (int i = 0; i < preFields.size(); i++) {
					Collection<String> aLLDB_Fields = systemProperties.getDBFields();
					for (String columnName : aLLDB_Fields) {
						newFieldsObj.put(columnName, preFields.get(columnName));
					}
				}
				newFieldsObj.put(FieldType.PAYOUT_DATE.getName(), dateNow);
				newFieldsObj.put(FieldType.UTR_NO.getName(), utr);

				BasicDBObject updateObj = new BasicDBObject();
				updateObj.put("$set", newFieldsObj);
				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> collection = dbIns.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				Document newDoc = new Document(updateObj);
				Document oldDoc = new Document(oldFieldsObj);
				collection.updateOne(oldDoc, newDoc);

				// updating UTR in transationStatus collection.
				Document latestStatusDoc = getDataForOid(preFields.get(FieldType.OID.getName()));

				if (latestStatusDoc != null) {
					latestStatusDoc.put(FieldType.PAYOUT_DATE.getName(), dateNow);
					latestStatusDoc.put(FieldType.UTR_NO.getName(), utr);

					MongoCollection<Document> collection1 = dbIns.getCollection(propertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

					Document latestOldDoc = new Document(oldFieldsObj);
					Document latestNewDoc = new Document("$set", latestStatusDoc);
					collection1.updateOne(latestOldDoc, latestNewDoc);
				}

			}

		} catch (Exception exception) {
			String message = "Error while Updating UTR number in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

	}

	@SuppressWarnings("static-access")
	public Set<String> getAllDataForAMerchant(Fields fields) throws SystemException {
		try {
			logger.info("Inside getAllDataForAMerchant : ");

			Set<String> OidSet = new HashSet<String>();
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();

			String dateFrom = DateCreater.toDateTimeformatCreater(fields.get(FieldType.CAPTURED_DATE_FROM.getName()));
			String dateTo = DateCreater.formDateTimeformatCreater(fields.get(FieldType.CAPTURED_DATE_TO.getName()));

			if (!dateFrom.isEmpty() && !dateTo.isEmpty()) {

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
			}

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(dateFrom).toLocalizedPattern();
			String endString = new SimpleDateFormat(dateTo).toLocalizedPattern();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(startString);
			Date dateEnd = format.parse(endString);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}
			BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndexConditionQuery.append("DATE_INDEX", dateIndexIn);
			}

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryList.add(dateIndexConditionQuery);
			}

			String subMerchantId = fields.get(FieldType.SUB_MERCHANT_ID.getName());
			if (StringUtils.isNotBlank(subMerchantId)) {
				allConditionQueryList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}

			allConditionQueryList
					.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
			allConditionQueryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					finalList.add(allConditionQueryObj);
				}
			}
			BasicDBObject finalquery = new BasicDBObject("$and", finalList);
			logger.info("Query Created");
			MongoDatabase dbIns = mongoInstance.getDB();
			logger.info("Below MongoDatabase dbIns = mongoInstance.getDB()");
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(finalquery).iterator();
			logger.info("Query Executed");
			while (cursor.hasNext()) {
				Document documentObj = (Document) cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				if (null != documentObj) {
					OidSet.add(documentObj.get(FieldType.OID.getName()).toString());
				}
			}
			logger.info("Got Previous Data with count : " + OidSet.size());

			return OidSet;
		} catch (Exception exception) {
			String message = "Error while reading list of transactions based on OID from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public List<Fields> getSettledTxnByOID(Set<String> OidSet) {
		logger.info("Inside getSettledTxnByOID : ");
		List<Fields> fieldsList = new ArrayList<Fields>();

		for (String OID : OidSet) {

			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			queryList.add(new BasicDBObject(FieldType.OID.getName(), OID));
			BasicDBObject andQuery = new BasicDBObject("$and", queryList);

			MongoDatabase dbIns = mongoInstance.getDB();
			logger.info("Below MongoDatabase dbIns = mongoInstance.getDB()");
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(andQuery).iterator();
			while (cursor.hasNext()) {
				Document documentObj = (Document) cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				if (null != documentObj) {
					Fields preFields = new Fields();
					// for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : systemProperties.getDBFields()) {
						if (documentObj.get(columnName) != null) {
							preFields.put(columnName, documentObj.get(columnName).toString());
						}
					}
					// }
					preFields.logAllFields("Fetched Settled data for OID : " + OID);
					fieldsList.add(preFields);
				}
			}

		}
		logger.info("Got Settled Data with count : " + fieldsList.size());

		return fieldsList;
	}

	@SuppressWarnings("static-access")
	public Document getDataForOid(String oid) {
		logger.info("Inside getSettledTxnByOID : ");
		List<Fields> fieldsList = new ArrayList<Fields>();

		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
		queryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
		queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		queryList.add(new BasicDBObject(FieldType.OID.getName(), oid));
		BasicDBObject andQuery = new BasicDBObject("$and", queryList);

		MongoDatabase dbIns = mongoInstance.getDB();
		logger.info("Below MongoDatabase dbIns = mongoInstance.getDB()");
		MongoCollection<Document> collection = dbIns.getCollection(
				propertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
		MongoCursor<Document> cursor = collection.find(andQuery).iterator();
		while (cursor.hasNext()) {
			Document documentObj = (Document) cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			return documentObj;
		}

		logger.info("Got Settled Data with count : " + fieldsList.size());

		return null;
	}

	@SuppressWarnings("static-access")
	public boolean checkSentToBankUpi(String orderId) {

		logger.info("Inside checkSentToBankUpi : ");

		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
		queryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));
		queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		BasicDBObject andQuery = new BasicDBObject("$and", queryList);

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> collection = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		long count = collection.count(andQuery);

		if (count > 0) {
			return true;
		} else {
			return false;
		}

	}

	@SuppressWarnings("static-access")
	public void updateStatusColl(String origTxnType, String pgRefNum, String txnType, String status,
			Document document) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			String orderId = document.getString(FieldType.ORDER_ID.getName());
			String oid = document.get(FieldType.OID.getName()).toString();

			if (StringUtils.isNotBlank(txnType) && (txnType.equalsIgnoreCase(TransactionType.INVALID.getName())
					|| txnType.equalsIgnoreCase(TransactionType.NEWORDER.getName())
					|| txnType.equalsIgnoreCase(TransactionType.RECO.getName())
					|| txnType.equalsIgnoreCase(TransactionType.ENROLL.getName()))) {
				txnType = TransactionType.SALE.getName();
			}

			if (StringUtils.isNotBlank(txnType) && (txnType.equalsIgnoreCase(TransactionType.REFUND.getName())
					|| txnType.equalsIgnoreCase(TransactionType.REFUNDRECO.getName()))) {
				txnType = TransactionType.REFUND.getName();
			}

			if (StringUtils.isBlank(txnType) && StringUtils.isNotBlank(origTxnType)) {
				txnType = origTxnType;
			}

			if (StringUtils.isBlank(origTxnType)) {
				origTxnType = txnType;
			}

			if (StringUtils.isNotBlank(origTxnType)
					&& origTxnType.equalsIgnoreCase(TransactionType.INVALID.getName())) {
				origTxnType = TransactionType.SALE.getName();
			}

			if (StringUtils.isNotBlank(origTxnType) && origTxnType.equalsIgnoreCase(TransactionType.STATUS.getName())) {
				origTxnType = txnType;
			}

			// SALE
			if (StringUtils.isNotBlank(origTxnType) && origTxnType.equalsIgnoreCase(TransactionType.SALE.getName())) {
				if (StringUtils.isBlank(orderId) || StringUtils.isBlank(oid) || StringUtils.isBlank(origTxnType)) {

					logger.info("Cannot update transaction status collection for combination " + " Order Id = "
							+ orderId + " OID = " + oid + " orig Txn Type = " + origTxnType);
					logger.info("Txn cannot be added , moving to transactionStatusException ");

					Document doc = new Document();
					doc.put(FieldType.ORDER_ID.getName(), orderId);
					doc.put(FieldType.OID.getName(), oid);
					doc.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);

					MongoCollection<Document> excepColl = dbIns.getCollection(PropertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_EXCEP_COLLECTION.getValue()));

					excepColl.insertOne(doc);
				} else {

					MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

					List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
					dbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
					dbObjList.add(new BasicDBObject(FieldType.OID.getName(), oid));

					dbObjList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), origTxnType));

					BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);

					FindIterable<Document> cursor = coll.find(andQuery);

					if (cursor.iterator().hasNext()) {

						String transactionStatusFields = PropertiesManager.propertiesMap.get("TransactionStatusFields");
						String transactionStatusFieldsArr[] = transactionStatusFields.split(",");

						MongoCollection<Document> txnStatusColl = dbIns.getCollection(propertiesManager.propertiesMap
								.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

						BasicDBObject searchQuery = andQuery;
						BasicDBObject updateFields = new BasicDBObject();

						if (StringUtils.isBlank(status)) {
							logger.info("Status is null, Setting status as timeout for order Id = " + orderId);
							status = StatusType.TIMEOUT.getName();
						}

						String statusALias = resolveStatus(status);

						// Added to avoid exception in report
						if (StringUtils.isBlank(statusALias)) {
							logger.info("Alias Status not resolved for order Id = " + orderId);
							statusALias = StatusType.TIMEOUT.getName();
							status = StatusType.TIMEOUT.getName();
							document.put(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName());
						}

						for (String key : transactionStatusFieldsArr) {

							if (status.equalsIgnoreCase(StatusType.SETTLED.getName())) {

								if ((key.equalsIgnoreCase(FieldType.DATE_INDEX.getName()))
										|| (key.equalsIgnoreCase(FieldType.CREATE_DATE.getName()))
										|| (key.equalsIgnoreCase(FieldType.UPDATE_DATE.getName()))
										|| (key.equalsIgnoreCase(FieldType.INSERTION_DATE.getName()))) {
									continue;
								}

								if ((key.equalsIgnoreCase(FieldType.SETTLEMENT_DATE.getName()))) {
									updateFields.put(key, document.get(FieldType.CREATE_DATE.getName()));
									continue;
								}

								if ((key.equalsIgnoreCase(FieldType.SETTLEMENT_FLAG.getName()))) {
									updateFields.put(key, "Y");
									continue;
								}

								if ((key.equalsIgnoreCase(FieldType.SETTLEMENT_DATE_INDEX.getName()))) {
									updateFields.put(key, document.get(FieldType.DATE_INDEX.getName()));
									continue;
								}

								if ((key.equalsIgnoreCase(FieldType.CREATE_DATE.getName()))) {
									updateFields.remove(FieldType.STATUS.getName());
									continue;
								}

							}

							if (document.get(key) != null) {
								updateFields.put(key, document.get(key).toString());
							} else {
								updateFields.put(key, document.get(key));
							}

						}

						updateFields.put(FieldType.ALIAS_STATUS.getName(), statusALias);
						updateFields.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
						txnStatusColl.updateOne(searchQuery, new BasicDBObject("$set", updateFields));

					} else {

						MongoCollection<Document> txnStatusColl = dbIns.getCollection(propertiesManager.propertiesMap
								.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

						String statusALias = resolveStatus(status);

						// Added to avoid exception in report
						if (StringUtils.isBlank(statusALias)) {
							logger.info("Alias Status not resolved for order Id = " + orderId);
							statusALias = StatusType.TIMEOUT.getName();
							status = StatusType.TIMEOUT.getName();
							document.put(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName());
						}

						document.put(FieldType.ALIAS_STATUS.getName(), statusALias);
						document.put(FieldType.STATUS.getName(), status);
						document.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
						txnStatusColl.insertOne(document);

					}

				}
			}

			// REFUND

			if (StringUtils.isNotBlank(origTxnType) && origTxnType.equalsIgnoreCase(TransactionType.REFUND.getName())) {
				if (StringUtils.isBlank(pgRefNum) || StringUtils.isBlank(oid) || StringUtils.isBlank(origTxnType)) {

					logger.info("Cannot update transaction status collection for combination " + " PG REF NUM = "
							+ pgRefNum + " OID = " + oid + " orig Txn Type = " + origTxnType);
					logger.info("Txn cannot be added , moving to transactionStatusException ");

					Document doc = new Document();
					doc.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
					doc.put(FieldType.OID.getName(), oid);
					doc.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);

					MongoCollection<Document> excepColl = dbIns.getCollection(PropertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_EXCEP_COLLECTION.getValue()));

					excepColl.insertOne(doc);
				} else {

					MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

					List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
					dbObjList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
					dbObjList.add(new BasicDBObject(FieldType.OID.getName(), oid));
					dbObjList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), origTxnType));

					BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);

					FindIterable<Document> cursor = coll.find(andQuery);

					if (cursor.iterator().hasNext()) {

						String transactionStatusFields = PropertiesManager.propertiesMap.get("TransactionStatusFields");
						String transactionStatusFieldsArr[] = transactionStatusFields.split(",");

						MongoCollection<Document> txnStatusColl = dbIns.getCollection(propertiesManager.propertiesMap
								.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

						BasicDBObject searchQuery = andQuery;
						BasicDBObject updateFields = new BasicDBObject();

						for (String key : transactionStatusFieldsArr) {

							if (document.get(key) != null) {
								updateFields.put(key, document.get(key).toString());
							} else {
								updateFields.put(key, document.get(key));
							}

						}

						String statusALias = resolveStatus(status);
						updateFields.put(FieldType.ALIAS_STATUS.getName(), statusALias);
						updateFields.put(FieldType.STATUS.getName(), status);
						updateFields.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
						txnStatusColl.updateOne(searchQuery, new BasicDBObject("$set", updateFields));

					} else {

						MongoCollection<Document> txnStatusColl = dbIns.getCollection(propertiesManager.propertiesMap
								.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

						String statusALias = resolveStatus(status);
						document.put(FieldType.ALIAS_STATUS.getName(), statusALias);
						document.put(FieldType.STATUS.getName(), status);
						document.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
						txnStatusColl.insertOne(document);

					}

				}
			}

		}

		catch (Exception e) {
			logger.error("Exception in adding txn to transaction status", e);
		}

	}

	public String resolveStatus(String status) {

		if (StringUtils.isBlank(status)) {
			return status;
		} else {
			if (status.equals(StatusType.CAPTURED.getName())) {
				return "Captured";

			} else if (status.equals(StatusType.SETTLED.getName())) {
				return "Captured";

			} else if (status.equals(StatusType.PENDING.getName()) || status.equals(StatusType.SENT_TO_BANK.getName())
					|| status.equals(StatusType.ENROLLED.getName()) || status.equals(StatusType.PROCESSING.getName())) {
				return "Pending";

			} else if (status.equals(StatusType.BROWSER_CLOSED.getName())
					|| status.equals(StatusType.CANCELLED.getName())) {
				return "Cancelled";

			} else if (status.equals(StatusType.INVALID.getName()) || status.equals(StatusType.DUPLICATE.getName())) {
				return "Invalid";

			} else {
				return "Failed";
			}

		}
	}

	@SuppressWarnings("static-access")
	public void insertPayoutTransactionFields(Fields fields) throws SystemException {
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);

			BasicDBObject newFieldsObj = new BasicDBObject();

			for (String columnName : fields.keySet()) {
				newFieldsObj.put(columnName, fields.get(columnName));

			}

			newFieldsObj.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));

			// fields.remove(FieldType.DATE_INDEX.getName());

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			Document doc = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			// collection.insertOne(doc);
			logger.info("Fields Inserted " + fields.maskFieldsRequest(fields.getFields()) + " txn id "
					+ fields.get(FieldType.TXN_ID.getName()));
		} catch (Exception exception) {
			String message = "Error while inserting Composite Response in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void insertIciciCompositeBeneFields(Map<String, String> fields) throws SystemException {
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);

			fields.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));

			BasicDBObject newFieldsObj = new BasicDBObject();

			for (String columnName : fields.keySet()) {
				newFieldsObj.put(columnName, fields.get(columnName));
			}

			fields.remove(FieldType.DATE_INDEX.getName());

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COMPOSITE_BENE_COLLECTION.getValue()));
			Document doc = new Document(newFieldsObj);
			collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error while inserting Composite Bene Response in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public List<String> insertTransactionPendingDocEnach(String pgRefNum) {

		logger.info("inside insertTransactionPendingDocEnach according to tenure ");
		HashMap<String, String> mandateDetailMap = new HashMap<String, String>();
		List<String> startDateEndDateList = new ArrayList<String>();
		try {
			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			queryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			BasicDBObject query = new BasicDBObject("$and", queryList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			try {
				while (cursor.hasNext()) {

					Document doc = cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						doc = dataEncDecTool.decryptDocument(doc);
					}

					mandateDetailMap.put("paymentMode", doc.getString(FieldType.PAYMENT_TYPE.getName()));

					if (doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("NB")
							|| doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("DC")) {
						mandateDetailMap.put("accountType", doc.getString(FieldType.ACCOUNT_TYPE.getName()));
						mandateDetailMap.put("accountNumber", doc.getString(FieldType.ACCOUNT_NO.toString()));
						mandateDetailMap.put("accountHolderName",
								doc.getString(FieldType.ACCOUNT_HOLDER_NAME.getName()));
						mandateDetailMap.put("ifscCode", doc.getString(FieldType.IFSC_CODE.getName()));
					}
					if (doc.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						mandateDetailMap.put("subMerchantId", doc.getString(FieldType.SUB_MERCHANT_ID.getName()));
					}

					if (doc.containsKey(FieldType.RESELLER_ID.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_ID.getName()))) {
						mandateDetailMap.put("resellerId", doc.getString(FieldType.RESELLER_ID.getName()));
					}

					if (doc.containsKey("MERCHANT_LOGO") && StringUtils.isNotBlank(doc.getString("MERCHANT_LOGO"))) {
						mandateDetailMap.put("LOGO", doc.getString("MERCHANT_LOGO"));
					}

					if (doc.containsKey(FieldType.UMRN_NUMBER.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.UMRN_NUMBER.getName()))) {
						mandateDetailMap.put("umrnNumber", doc.getString(FieldType.UMRN_NUMBER.getName()));
					} else {
						mandateDetailMap.put("umrnNumber", Constants.NA.getValue());
					}

					if (doc.containsKey(FieldType.RESPONSE_MESSAGE.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESPONSE_MESSAGE.getName()))) {
						mandateDetailMap.put("responseMessage", doc.getString(FieldType.RESPONSE_MESSAGE.getName()));
					} else {
						mandateDetailMap.put("responseMessage", Constants.NA.getValue());
					}

					mandateDetailMap.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
					mandateDetailMap.put("bankName", doc.getString(FieldType.BANK_NAME.getName()));
					mandateDetailMap.put("mobileNumber", doc.getString(FieldType.CUST_PHONE.getName()));
					mandateDetailMap.put("emailId", doc.getString(FieldType.CUST_EMAIL.getName()));
					mandateDetailMap.put("amount", doc.getString(FieldType.AMOUNT.getName()));
					mandateDetailMap.put("maxAmount", doc.getString(FieldType.MONTHLY_AMOUNT.getName()));
					mandateDetailMap.put("totalAmount", doc.getString(FieldType.TOTAL_AMOUNT.getName()));
					mandateDetailMap.put("frequency", doc.getString(FieldType.FREQUENCY.getName()));
					mandateDetailMap.put("tenure", doc.getString(FieldType.TENURE.getName()));
					mandateDetailMap.put("startDate", doc.getString(FieldType.DATEFROM.getName()));
					mandateDetailMap.put("endDate", doc.getString(FieldType.DATETO.getName()));
					mandateDetailMap.put("returnUrl", doc.getString(FieldType.RETURN_URL.getName()));
					mandateDetailMap.put("MANDATE_REGISTRATION_ID", doc.getString("MANDATE_REGISTRATION_ID"));
					mandateDetailMap.put("COM_AMT", doc.getString("COM_AMT"));
					mandateDetailMap.put("merchantReturnUrl", doc.getString("MERCHANT_RETURN_URL"));
					mandateDetailMap.put("payId", doc.getString(FieldType.PAY_ID.getName()));
					mandateDetailMap.put("merchantName",
							userDao.getBusinessNameByPayId(doc.getString(FieldType.PAY_ID.getName())));
					mandateDetailMap.put("merchantEmail",
							userDao.getEmailIdByPayId(doc.getString(FieldType.PAY_ID.getName())));
					mandateDetailMap.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
					mandateDetailMap.put("responseCode", doc.getString(FieldType.RESPONSE_CODE.getName()));
					mandateDetailMap.put("updatedDate", doc.getString(FieldType.UPDATE_DATE.getName()));
					mandateDetailMap.put("status", doc.getString(FieldType.STATUS.getName()));
					mandateDetailMap.put("currency", doc.getString(FieldType.CURRENCY.getName()));
					mandateDetailMap.put("origTxnId", doc.getString(FieldType.ORIG_TXN_ID.getName()));
					mandateDetailMap.put("amountType", doc.getString(FieldType.AMOUNT_TYPE.getName()));
					mandateDetailMap.put("createDate", doc.getString(FieldType.CREATE_DATE.getName()));
					mandateDetailMap.put("txnId", doc.getString(FieldType.TXN_ID.getName()));
				}

				Date startDate = new SimpleDateFormat("dd-MM-yyyy").parse(mandateDetailMap.get("startDate"));
				Date endDate = new SimpleDateFormat("dd-MM-yyyy").parse(mandateDetailMap.get("endDate"));
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				mandateDetailMap.remove("startDate");
				mandateDetailMap.remove("endDate");
				mandateDetailMap.put("startDate", formatter.format(startDate));
				mandateDetailMap.put("endDate", formatter.format(endDate));

				List<String> dateList = eNachDao.getDueDateList(mandateDetailMap.get("startDate"),
						mandateDetailMap.get("tenure"), mandateDetailMap.get("payId"),
						mandateDetailMap.get("subMerchantId"), mandateDetailMap.get("frequency"));

				startDateEndDateList.add(dateList.get(0));
				startDateEndDateList.add(dateList.get(dateList.size() - 1));

				for (String dueDate : dateList) {

					String newPgRefNum = TransactionManager.getNewTransactionId();
					BasicDBObject newFieldsObj = new BasicDBObject();

					newFieldsObj.put(FieldType.TXNTYPE.getName(), "Sale");
					newFieldsObj.put(FieldType.PAY_ID.getName(), mandateDetailMap.get("payId"));
					newFieldsObj.put(FieldType.DATEFROM.getName(), mandateDetailMap.get("startDate"));
					newFieldsObj.put(FieldType.DATETO.getName(), mandateDetailMap.get("endDate"));
					newFieldsObj.put(FieldType.CURRENCY.getName(), mandateDetailMap.get("currency"));

					newFieldsObj.put(Constants.AMOUNT.getValue(), String.valueOf(
							new BigDecimal(mandateDetailMap.get("maxAmount")).setScale(2, BigDecimal.ROUND_HALF_UP)));

					newFieldsObj.put(FieldType.MONTHLY_AMOUNT.getName(), mandateDetailMap.get("maxAmount"));
					// newFieldsObj.put(FieldType.DEBIT_DATE.getName(), "NA");

					newFieldsObj.put(FieldType.TOTAL_AMOUNT.getName(), mandateDetailMap.get("totalAmount"));

					if (mandateDetailMap.containsKey("subMerchantId")
							&& StringUtils.isNotBlank(mandateDetailMap.get("subMerchantId"))) {
						newFieldsObj.put(FieldType.SUB_MERCHANT_ID.getName(), mandateDetailMap.get("subMerchantId"));
					}

					if (mandateDetailMap.containsKey("resellerId")
							&& StringUtils.isNotBlank(mandateDetailMap.get("resellerId"))) {
						newFieldsObj.put(FieldType.RESELLER_ID.getName(), mandateDetailMap.get("resellerId"));
					}

					newFieldsObj.put(FieldType.PAYMENT_TYPE.getName(), mandateDetailMap.get("paymentMode"));
					newFieldsObj.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));
					newFieldsObj.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
					newFieldsObj.put(FieldType.REGISTRATION_DATE.getName(), mandateDetailMap.get("createDate"));
					newFieldsObj.put("COM_AMT", mandateDetailMap.get("COM_AMT"));
					newFieldsObj.put(FieldType.CUST_PHONE.getName(), mandateDetailMap.get("mobileNumber"));
					newFieldsObj.put(FieldType.CUST_EMAIL.getName(), mandateDetailMap.get("emailId"));
					newFieldsObj.put(FieldType.AMOUNT_TYPE.getName(), mandateDetailMap.get("amountType"));
					newFieldsObj.put(FieldType.FREQUENCY.getName(), mandateDetailMap.get("frequency"));
					newFieldsObj.put(FieldType.ORDER_ID.getName(), mandateDetailMap.get(FieldType.ORDER_ID.getName()));
					newFieldsObj.put(FieldType.TXN_ID.getName(), newPgRefNum);
					newFieldsObj.put(FieldType.PG_REF_NUM.getName(), newPgRefNum);
					newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(), mandateDetailMap.get("origTxnId"));
					newFieldsObj.put("_id", newPgRefNum);
					newFieldsObj.put("MANDATE_REGISTRATION_ID", mandateDetailMap.get("MANDATE_REGISTRATION_ID"));
					newFieldsObj.put(FieldType.TENURE.getName(), mandateDetailMap.get("tenure"));
					newFieldsObj.put(FieldType.BANK_NAME.getName(), mandateDetailMap.get("bankName"));
					newFieldsObj.put(FieldType.DUE_DATE.getName(), dueDate);
					newFieldsObj.put(FieldType.UMRN_NUMBER.getName(), mandateDetailMap.get("umrnNumber"));

					if (mandateDetailMap.get("paymentMode").equalsIgnoreCase("NB")
							|| mandateDetailMap.get("paymentMode").equalsIgnoreCase("DC")) {

						newFieldsObj.put(FieldType.ACCOUNT_NO.toString(), mandateDetailMap.get("accountNumber"));
						newFieldsObj.put(FieldType.IFSC_CODE.getName(), mandateDetailMap.get("ifscCode"));
						newFieldsObj.put(FieldType.ACCOUNT_HOLDER_NAME.getName(),
								mandateDetailMap.get("accountHolderName"));
						newFieldsObj.put(FieldType.ACCOUNT_TYPE.getName(), mandateDetailMap.get("accountType"));

					}
					Document insertPendingDebitTxn = new Document(newFieldsObj);
					MongoCollection<Document> coll = dbIns.getCollection(
							propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

						coll.insertOne(dataEncDecTool.encryptDocument(insertPendingDebitTxn));
					} else {
						coll.insertOne(insertPendingDebitTxn);
					}
					// coll.insertOne(insertPendingDebitTxn);
				}

			} finally {
				cursor.close();
			}

		} catch (Exception exception) {
			String message = "Error insert pending debit txn in database ";
			logger.error(message, exception);
		}
		return startDateEndDateList;
	}

	public List<String> getDueDateList(String startDate, String endDate, String tenure, String payId,
			String subMerchantPayId, String debitDurationType) {
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

			int finalTenure = Integer.parseInt(tenure);
			LocalDate end = start.plusMonths(finalTenure - 1);

			LocalDate dateFrom = start;
			boolean flag = true;

			String startDateArr[] = start.toString().split("-");
			if (startDateArr[2].contains("31")) {

				while (!dateFrom.isAfter(end) && flag == true) {
					Date date = dateFormat.parse(dateFrom.toString());
					Calendar currentMonth = Calendar.getInstance();
					currentMonth.setTime(date);
					dateList.add(dateFormat.format(currentMonth.getTime()));
					flag = false;
				}

				while (!dateFrom.isAfter(end)) {

					String firstDate = dateList.get(0);
					String firstDateArr[] = firstDate.split("-");

					int lastDate = Integer.parseInt(firstDateArr[2]);

					Date date = dateFormat.parse(dateFrom.toString());
					Calendar currentMonth = Calendar.getInstance();
					currentMonth.setTime(date);
					currentMonth.add(Calendar.MONTH, 1);

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

						dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());
					}

				}

			} else if (startDateArr[2].contains("29") || startDateArr[2].contains("30")) {

				while (!dateFrom.isAfter(end) && flag == true) {
					Date date = dateFormat.parse(dateFrom.toString());
					Calendar currentMonth = Calendar.getInstance();
					currentMonth.setTime(date);
					dateList.add(dateFormat.format(currentMonth.getTime()));

					flag = false;
				}

				while (!dateFrom.isAfter(end)) {

					String firstDate = dateList.get(0);
					String firstDateArr[] = firstDate.split("-");

					int lastDate = Integer.parseInt(firstDateArr[2]);

					Date date = dateFormat.parse(dateFrom.toString());
					Calendar currentMonth = Calendar.getInstance();
					currentMonth.setTime(date);
					currentMonth.add(Calendar.MONTH, 1);

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

						dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());
					}

				}

			} else {

				while (!dateFrom.isAfter(end) && flag == true) {
					Date date = dateFormat.parse(dateFrom.toString());
					Calendar currentMonth = Calendar.getInstance();
					currentMonth.setTime(date);
					dateList.add(dateFormat.format(currentMonth.getTime()));
					flag = false;
				}
				while (!dateFrom.isAfter(end)) {

					String firstDate = dateList.get(0);
					String firstDateArr[] = firstDate.split("-");

					int lastDate = Integer.parseInt(firstDateArr[2]);

					Date date = dateFormat.parse(dateFrom.toString());
					Calendar currentMonth = Calendar.getInstance();
					currentMonth.setTime(date);
					currentMonth.add(Calendar.MONTH, 1);

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

						dateFrom = LocalDate.parse(dateFormat.format(currentMonth.getTime()).toString());
					}
				}
			}

		} catch (Exception ex) {
			logger.info("exception caught while create due date list ", ex);
		}
		dateList.remove(dateList.size() - 1);
		return dateList;
	}

	public HashMap<String, String> getEnachDebitDetailsForStatusEnquiry(String merchantPayId, String subMerchantPayId,
			String orderId, String debitDate) {

		HashMap<String, String> tranMap = new HashMap<String, String>();
		try {
			String dueAmount = null;
			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			queryList.add(new BasicDBObject(FieldType.DEBIT_DATE.getName(), debitDate)); // yyyy-mm-dd
																							// 2021-04-23
			queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "Sale"));

			if (StringUtils.isNotBlank(subMerchantPayId)) {
				queryList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			BasicDBObject finalQuery = new BasicDBObject("$and", queryList);
			logger.info("inside getEnachDebitDetailsForStatusEnquiry final query " + finalQuery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {

				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				Set<String> set = dbobj.keySet();
				set.remove("_id");
				for (String key : set) {
					tranMap.put(key, dbobj.getString(key));
				}

				if (dueAmount == null) {
					dueAmount = String.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
							.subtract(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName()))));
				} else {
					dueAmount = String.valueOf(
							new BigDecimal(dueAmount).add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName()))));
				}

				if (dbobj.getString(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PROCESSING.getName())) {
					tranMap.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
					tranMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Capture In-progress");

				} else if (dbobj.getString(FieldType.STATUS.getName())
						.equalsIgnoreCase(StatusType.CAPTURED.getName())) {
					tranMap.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
					tranMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Settlement In-progress");

				} else if (dbobj.getString(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.SETTLED.getName())) {
					tranMap.put(FieldType.STATUS.getName(), dbobj.getString(FieldType.STATUS.getName()));
					tranMap.put(FieldType.RESPONSE_MESSAGE.getName(),
							dbobj.getString(FieldType.PG_RESPONSE_MSG.getName()));

				} else {
					tranMap.put(FieldType.STATUS.getName(), dbobj.getString(FieldType.STATUS.getName()));
					tranMap.put(FieldType.RESPONSE_MESSAGE.getName(),
							dbobj.getString(FieldType.PG_RESPONSE_MSG.getName()));
				}
				tranMap.put("DUE_AMOUNT", dueAmount);
				tranMap.put(FieldType.ORDER_ID.getName(), orderId);
			}

			if (tranMap.size() == 0) {

				tranMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_TRANSACTION_AVAILABLE.getCode());
				tranMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.NO_TRANSACTION_AVAILABLE.getResponseMessage());
				return tranMap;
			}

		} catch (Exception ex) {
			logger.info("caught exception ", ex);
			HashMap<String, String> errorMap = new HashMap<String, String>();
			errorMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DATABASE_ERROR.getCode());
			errorMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DATABASE_ERROR.getResponseMessage());
			return errorMap;

		}
		return tranMap;
	}

	public void sendMerchantSMS(boolean allowSms, String mobile, String totalAmount, String orderId) {

		if (allowSms) {
			logger.info("Sending Transaction Successful SMS to Merchant for Static QR , Order Id = " + orderId);
			smsController.transactionSmsForMerchant(mobile, totalAmount, orderId);
		}

	}

	@SuppressWarnings("static-access")
	public void sendCallback(Fields fields) {

		User user = new User();
		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			user = userDao.findPayId(fields.get(FieldType.SUB_MERCHANT_ID.getName()));
		} else {
			user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		}
		UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(user.getPayId());

		if (StringUtils.isNotBlank(merchantSettings.getCallBackUrl())) {
			if (merchantSettings.isAllCallBackFlag()) {
				JsonObject json = new JsonObject();
				try {

					// Check to see if a transaction is already added in DB with
					// Same
					// RRN and status
					// as Captured and Skip that entry when sending callback
					if (StringUtils.isNotBlank(fields.get(FieldType.TXNTYPE.getName()))
							&& StringUtils.isNotBlank(fields.get(FieldType.STATUS.getName()))) {

						if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())
								&& fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.CAPTURED.getName())) {

							List<BasicDBObject> saleConList = new ArrayList<BasicDBObject>();
							saleConList.add(
									new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
							saleConList
									.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
							saleConList.add(new BasicDBObject(FieldType.ACQ_ID.getName(),
									fields.get(FieldType.ACQ_ID.getName())));
							saleConList.add(new BasicDBObject(FieldType.PAY_ID.getName(),
									fields.get(FieldType.PAY_ID.getName())));

							BasicDBObject duplicateRRNQuery = new BasicDBObject("$and", saleConList);

							MongoDatabase dbIns = null;
							dbIns = mongoInstance.getDB();
							MongoCollection<Document> collection = dbIns.getCollection(propertiesManager.propertiesMap
									.get("MONGO_DB_" + Constants.COLLECTION_NAME.getValue()));

							long count = collection.count(duplicateRRNQuery);

							if (count > 1) {
								logger.info("Skipping duplicate capture entry for transaction with Same ACQ_ID = "
										+ fields.get(FieldType.ACQ_ID.getName()) + " and Order ID = "
										+ fields.get(FieldType.ORDER_ID.getName()));
								return;
							}

						}

					}
					Date date = new Date();
					SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
					String strDate = df.format(date);

					Fields newFields = new Fields();
					for (int i = 0; i < fields.size(); i++) {
						Collection<String> callbackFields = SystemProperties.getCallbackfields();
						for (String key : callbackFields) {
							if (StringUtils.isNotBlank(fields.get(key))) {
								newFields.put(key, fields.get(key));
							}
						}
					}
					newFields.put(FieldType.RESPONSE_DATE_TIME.getName(), strDate);
					String hash = Hasher.getHash(newFields);
					newFields.put("HASH", hash);

					List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
					for (String fieldType : fieldTypeList) {
						json.addProperty(fieldType, newFields.get(fieldType));
					}

					String serviceUrl = merchantSettings.getCallBackUrl();
					logger.info("Callback url >>> " + serviceUrl);
					HttpPost request = new HttpPost(serviceUrl);

					int CONNECTION_TIMEOUT_MS = 30 * 1000; // Timeout in millis.
					RequestConfig requestConfig = RequestConfig.custom()
							.setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS).setConnectTimeout(CONNECTION_TIMEOUT_MS)
							.setSocketTimeout(CONNECTION_TIMEOUT_MS).build();

					request.setConfig(requestConfig);

					CloseableHttpClient httpClient = HttpClientBuilder.create().build();

					logger.info("Callback Request for PG_REF_NUM " + newFields.get(FieldType.PG_REF_NUM.getName())
							+ " to " + user.getBusinessName() + " >>> " + json.toString());
					StringEntity params = new StringEntity(json.toString());

					request.addHeader("content-type", "application/json");
					request.setEntity(params);

					HttpResponse resp = httpClient.execute(request);
					HttpEntity response = resp.getEntity();
					String responseBody = EntityUtils.toString(response);
					logger.info("Callback Response from " + user.getBusinessName() + " >>> " + responseBody.toString()
							+ " For Order Id " + fields.get(FieldType.ORDER_ID.getName()));

				} catch (Exception e) {
					logger.error("Exception in sending call back response to " + user.getBusinessName()
							+ " for Order Id == " + fields.get(FieldType.ORDER_ID.getName()) + " and PG Ref Num == "
							+ fields.get(FieldType.PG_REF_NUM.getName()), e);
				}
			}

			if (!merchantSettings.isAllCallBackFlag()
					&& StringUtils.isNotBlank(fields.get(FieldType.PAYMENT_TYPE.getName()))
					&& StringUtils.isNotBlank(fields.get(FieldType.MOP_TYPE.getName()))
					&& fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())
					&& fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.STATIC_UPI_QR.getCode())) {
				JsonObject json = new JsonObject();
				try {

					// Check to see if a transaction is already added in DB with
					// Same
					// RRN and status
					// as Captured and Skip that entry when sending callback
					if (StringUtils.isNotBlank(fields.get(FieldType.TXNTYPE.getName()))
							&& StringUtils.isNotBlank(fields.get(FieldType.STATUS.getName()))) {

						if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())
								&& fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.CAPTURED.getName())) {

							List<BasicDBObject> saleConList = new ArrayList<BasicDBObject>();
							saleConList.add(
									new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
							saleConList
									.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
							saleConList.add(new BasicDBObject(FieldType.ACQ_ID.getName(),
									fields.get(FieldType.ACQ_ID.getName())));
							saleConList.add(new BasicDBObject(FieldType.PAY_ID.getName(),
									fields.get(FieldType.PAY_ID.getName())));

							BasicDBObject duplicateRRNQuery = new BasicDBObject("$and", saleConList);

							MongoDatabase dbIns = null;
							dbIns = mongoInstance.getDB();
							MongoCollection<Document> collection = dbIns.getCollection(propertiesManager.propertiesMap
									.get("MONGO_DB_" + Constants.COLLECTION_NAME.getValue()));

							long count = collection.count(duplicateRRNQuery);

							if (count > 1) {
								logger.info("Skipping duplicate capture entry for transaction with Same ACQ_ID = "
										+ fields.get(FieldType.ACQ_ID.getName()) + " and Order ID = "
										+ fields.get(FieldType.ORDER_ID.getName()));
								return;
							}

						}

					}
					Date date = new Date();
					SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
					String strDate = df.format(date);

					Fields newFields = new Fields();
					for (int i = 0; i < fields.size(); i++) {
						Collection<String> callbackFields = SystemProperties.getCallbackfields();
						for (String key : callbackFields) {
							if (StringUtils.isNotBlank(fields.get(key))) {
								newFields.put(key, fields.get(key));
							}
						}
					}
					newFields.put(FieldType.RESPONSE_DATE_TIME.getName(), strDate);
					String hash = Hasher.getHash(newFields);
					newFields.put("HASH", hash);

					List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
					for (String fieldType : fieldTypeList) {
						json.addProperty(fieldType, newFields.get(fieldType));
					}

					String serviceUrl = merchantSettings.getCallBackUrl();
					logger.info("Callback url >>> " + serviceUrl);
					HttpPost request = new HttpPost(serviceUrl);

					int CONNECTION_TIMEOUT_MS = 30 * 1000; // Timeout in millis.
					RequestConfig requestConfig = RequestConfig.custom()
							.setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS).setConnectTimeout(CONNECTION_TIMEOUT_MS)
							.setSocketTimeout(CONNECTION_TIMEOUT_MS).build();

					request.setConfig(requestConfig);

					CloseableHttpClient httpClient = HttpClientBuilder.create().build();

					logger.info("Callback Request for PG_REF_NUM " + newFields.get(FieldType.PG_REF_NUM.getName())
							+ " to " + user.getBusinessName() + " >>> " + json.toString());
					StringEntity params = new StringEntity(json.toString());

					request.addHeader("content-type", "application/json");
					request.setEntity(params);

					HttpResponse resp = httpClient.execute(request);
					HttpEntity response = resp.getEntity();
					String responseBody = EntityUtils.toString(response);
					logger.info("Callback Response from " + user.getBusinessName() + " >>> " + responseBody.toString());

				} catch (Exception e) {
					logger.error("Exception in sending call back response to " + user.getBusinessName()
							+ " for Order Id == " + fields.get(FieldType.ORDER_ID.getName()) + " and PG Ref Num == "
							+ fields.get(FieldType.PG_REF_NUM.getName()), e);
				}
			}

		}
	}

	@SuppressWarnings("static-access")
	public void sendCallbackAfterTxnEquiry(Fields fields) {
		User user = new User();
		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			user = userDao.findPayId(fields.get(FieldType.SUB_MERCHANT_ID.getName()));
		} else {
			user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		}
		UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(user.getPayId());

		if (merchantSettings.isCallBackFlag() && StringUtils.isNotBlank(merchantSettings.getCallBackUrl())) {
			JsonObject json = new JsonObject();
			try {

				// Check to see if a transaction is already added in DB with
				// Same
				// RRN and status
				// as Captured and Skip that entry when sending callback
				if (StringUtils.isNotBlank(fields.get(FieldType.TXNTYPE.getName()))
						&& StringUtils.isNotBlank(fields.get(FieldType.STATUS.getName()))) {

					if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())
							&& fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {

						List<BasicDBObject> saleConList = new ArrayList<BasicDBObject>();
						saleConList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
						saleConList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
						saleConList.add(
								new BasicDBObject(FieldType.ACQ_ID.getName(), fields.get(FieldType.ACQ_ID.getName())));
						saleConList.add(
								new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));

						BasicDBObject duplicateRRNQuery = new BasicDBObject("$and", saleConList);

						MongoDatabase dbIns = null;
						dbIns = mongoInstance.getDB();
						MongoCollection<Document> collection = dbIns.getCollection(propertiesManager.propertiesMap
								.get("MONGO_DB_" + Constants.COLLECTION_NAME.getValue()));

						long count = collection.count(duplicateRRNQuery);

						if (count > 1) {
							logger.info("Skipping duplicate capture entry for transaction with Same ACQ_ID = "
									+ fields.get(FieldType.ACQ_ID.getName()) + " and Order ID = "
									+ fields.get(FieldType.ORDER_ID.getName()));
							return;
						}

					}

				}
				Date date = new Date();
				SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
				String strDate = df.format(date);

				Fields newFields = new Fields();
				for (int i = 0; i < fields.size(); i++) {
					Collection<String> callbackFields = SystemProperties.getCallbackfields();
					for (String key : callbackFields) {
						if (StringUtils.isNotBlank(fields.get(key))) {
							newFields.put(key, fields.get(key));
						}
					}
				}
				newFields.put(FieldType.RESPONSE_DATE_TIME.getName(), strDate);
				String hash = Hasher.getHash(newFields);
				newFields.put("HASH", hash);

				List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
				for (String fieldType : fieldTypeList) {
					json.addProperty(fieldType, newFields.get(fieldType));
				}

				String serviceUrl = merchantSettings.getCallBackUrl();
				logger.info("Callback url >>> " + serviceUrl);
				HttpPost request = new HttpPost(serviceUrl);

				int CONNECTION_TIMEOUT_MS = 30 * 1000; // Timeout in millis.
				RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
						.setConnectTimeout(CONNECTION_TIMEOUT_MS).setSocketTimeout(CONNECTION_TIMEOUT_MS).build();

				request.setConfig(requestConfig);

				CloseableHttpClient httpClient = HttpClientBuilder.create().build();

				logger.info("Callback Request for PG_REF_NUM " + newFields.get(FieldType.PG_REF_NUM.getName()) + " to "
						+ user.getBusinessName() + " >>> " + json.toString());
				StringEntity params = new StringEntity(json.toString());

				request.addHeader("content-type", "application/json");
				request.setEntity(params);

				HttpResponse resp = httpClient.execute(request);
				HttpEntity response = resp.getEntity();
				String responseBody = EntityUtils.toString(response);
				logger.info("Callback Response from " + user.getBusinessName() + " >>> " + responseBody.toString());

			} catch (Exception e) {
				logger.error("Exception in sending call back response to " + user.getBusinessName()
						+ " for Order Id == " + fields.get(FieldType.ORDER_ID.getName()) + " and PG Ref Num == "
						+ fields.get(FieldType.PG_REF_NUM.getName()), e);
			}
		}
	}

	@SuppressWarnings("static-access")
	public void updateIMPSBulkTransactionStatus(Fields fields) throws SystemException {
		logger.info("Inside updateIMPSBulkTransactionStatus(), FieldsDao");
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);

			fields.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));
			BasicDBObject newFieldsObj = new BasicDBObject();

			for (String columnName : fields.keySet()) {
				newFieldsObj.put(columnName, fields.get(columnName));

			}

			fields.remove(FieldType.DATE_INDEX.getName());
			Bson filter;

			if (fields.contains(FieldType.SUB_MERCHANT_ID.getName())) {
				filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
						fields.get(FieldType.SUB_MERCHANT_ID.getName())).append(FieldType.ORDER_ID.getName(),
								fields.get(FieldType.ORDER_ID.getName()));
			} else {
				filter = new Document(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()))
						.append(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
			}

			BasicDBObject updateObj = new BasicDBObject();
			updateObj.put("$set", newFieldsObj);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			Document newDoc = new Document(updateObj);
			collection.updateOne(filter, newDoc);
			logger.info("Fields Updated for bulk" + fields.maskFieldsRequest(fields.getFields()) + " txn id "
					+ fields.get(FieldType.TXN_ID.getName()));
		} catch (Exception exception) {
			String message = "Error while inserting Composite Response in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public boolean fetchAcceptPostCaptureInStatus(String payId) {
		return userDao.getAcceptPostCaptureInStatus(payId);
	}

	public void validateAndPayout(Fields fields) {

		try {

			p2MPayoutUtil.p2mPayout(fields);

		}

		catch (Exception e) {
			logger.error("Exception in payout to merchant after UPI STATIC QR Transaction ", e);
		}

	}

	@SuppressWarnings("static-access")
	public int getDoubleVerificationOfEnquiryTransaction(Fields fields) {
		logger.info("inside getDoubleVerificationOfEnquiryTransaction");
		try {
			int totalCount = 0;
			logger.info("double verification for orderId : " + fields.get(FieldType.ORDER_ID.getName())
					+ " and payId : " + fields.get(FieldType.PAY_ID.getName()));
			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

			queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));
			queryList.add(new BasicDBObject(FieldType.AMOUNT.getName(), Amount
					.toDecimal(fields.get(FieldType.AMOUNT.getName()), fields.get(FieldType.CURRENCY_CODE.getName()))));
			queryList.add(new BasicDBObject(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName())));

			BasicDBObject finalQuery = new BasicDBObject("$and", queryList);

			logger.info("double verification query for orderId " + fields.get(FieldType.ORDER_ID.getName()) + " is : "
					+ finalQuery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			totalCount = (int) collection.count(finalQuery);
			logger.info(
					"total txn count for orderId " + fields.get(FieldType.ORDER_ID.getName()) + " is  = " + totalCount);
			return totalCount;

		} catch (Exception ex) {
			logger.info("exception caught while get double verification of transaction ", ex);
			return 0;
		}

	}

	@SuppressWarnings("static-access")
	public CoinSwitchCustomer getCustomerByVirtualAccNo(String virtualAccNo) {

		CoinSwitchCustomer custDetail = new CoinSwitchCustomer();
		try {
			MongoDatabase dbIns = null;

			BasicDBObject query = new BasicDBObject(FieldType.VIRTUAL_ACC_NUM.getName(), virtualAccNo);
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COINSWITCH_ACCOUNTS.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				custDetail.setCustId(dbobj.getString(FieldType.CUST_ID.getName()));
				custDetail.setCustName(dbobj.getString(FieldType.CUST_NAME.getName()));
				custDetail.setPhoneNo(dbobj.getString(FieldType.PHONE_NO.getName()));
				custDetail.setEmailId(dbobj.getString(FieldType.CUST_EMAIL.getName()));
				custDetail.setAadhar(dbobj.getString(FieldType.AADHAR.getName()));
				custDetail.setPan(dbobj.getString(FieldType.PAN.getName()));
				custDetail.setAddress(dbobj.getString(FieldType.ADDRESS.getName()));
				custDetail.setDob(dbobj.getString(FieldType.DOB.getName()));
				custDetail.setAccountNo(dbobj.getString(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
				custDetail.setBankName(dbobj.getString(FieldType.BANK_NAME.getName()));
				custDetail.setBankIfsc(dbobj.getString(FieldType.IFSC_CODE.getName()));
				custDetail.setBankAccountHolderName(dbobj.getString(FieldType.ACCOUNT_HOLDER_NAME.getName()));
				custDetail.setStatus(dbobj.getString(FieldType.STATUS.getName()));

			}
			cursor.close();
		} catch (Exception exception) {
			logger.error("Exception while finding coinswitch customer from DB >>> ", exception);
		}
		return custDetail;
	}

	@SuppressWarnings("static-access")
	public void insertCoinSwitchTxnResponse(Fields fields) throws SystemException {
		logger.info("Insert in insertECollectionResponse(), FieldsDao");
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);
			fields.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));

			BasicDBObject newFieldsObj = new BasicDBObject();
			for (String columnName : fields.keySet()) {
				newFieldsObj.put(columnName, fields.get(columnName));
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COINSWITCH_TRANSACTION_DATA.getValue()));
			Document doc = new Document(newFieldsObj);
			collection.insertOne(doc);
		} catch (Exception exception) {
			logger.error("Exception while inserting coinswitch customer txn into DB with RRN >>> "
					+ fields.get(FieldType.RRN.getName()) + " >>>> ", exception);
		}
	}

	@SuppressWarnings("static-access")
	public String getCoinSwitchDuplicateTxnResponse(Fields fields) throws SystemException {

		String duplicacyResponse = "";
		try {
			MongoDatabase dbIns = null;

			BasicDBObject query = new BasicDBObject(FieldType.RRN.getName(), fields.get(FieldType.RRN.getName()));
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COINSWITCH_TRANSACTION_DATA.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			if (cursor.hasNext()) {
				duplicacyResponse = "Y";
			} else {
				duplicacyResponse = "N";
			}
			cursor.close();
		} catch (Exception exception) {
			String message = "Error while Checking Duplicacy in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
		return duplicacyResponse;
	}

	/*
	 * @SuppressWarnings("static-access") public void
	 * sendCallbackToCoinSwitch(Fields fields) { JsonObject json = new JsonObject();
	 * try { Date date = new Date(); SimpleDateFormat df = new
	 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); String strDate = df.format(date);
	 * fields.put(FieldType.PAY_ID.getName(),
	 * propertiesManager.propertiesMap.get("CoinSwitch_Merchant_PayId")); Fields
	 * newFields = new Fields(); for (int i = 0; i < fields.size(); i++) {
	 * Collection<String> callbackFields =
	 * SystemProperties.getCoinswitchcallbackfields(); for (String key :
	 * callbackFields) { if (StringUtils.isNotBlank(fields.get(key))) {
	 * newFields.put(key, fields.get(key)); } } }
	 * newFields.put(FieldType.RESPONSE_DATE_TIME.getName(), strDate); String hash =
	 * Hasher.getHash(newFields); newFields.put("HASH", hash);
	 * 
	 * List<String> fieldTypeList = new
	 * ArrayList<String>(newFields.getFields().keySet()); for (String fieldType :
	 * fieldTypeList) { json.addProperty(fieldType, newFields.get(fieldType)); }
	 * 
	 * String serviceUrl =
	 * propertiesManager.propertiesMap.get("COINSWITCH_CALLBACK_URL");
	 * logger.info("Callback url >>> " + serviceUrl); HttpPost request = new
	 * HttpPost(serviceUrl);
	 * 
	 * int CONNECTION_TIMEOUT_MS = 30 * 1000; // Timeout in millis. RequestConfig
	 * requestConfig =
	 * RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
	 * .setConnectTimeout(CONNECTION_TIMEOUT_MS).setSocketTimeout(
	 * CONNECTION_TIMEOUT_MS).build();
	 * 
	 * request.setConfig(requestConfig);
	 * 
	 * CloseableHttpClient httpClient = HttpClientBuilder.create().build();
	 * 
	 * logger.info("Callback Request for PG_REF_NUM " +
	 * newFields.get(FieldType.PG_REF_NUM.getName()) + " to CoinSwitch >>> " +
	 * json.toString()); StringEntity params = new StringEntity(json.toString());
	 * 
	 * request.addHeader("content-type", "application/json");
	 * request.setEntity(params);
	 * 
	 * HttpResponse resp = httpClient.execute(request); HttpEntity response =
	 * resp.getEntity(); String responseBody = EntityUtils.toString(response);
	 * logger.info("Callback Response from CoinSwitch >>> " +
	 * responseBody.toString());
	 * 
	 * } catch (Exception e) { logger.
	 * error("Exception in sending call back response to CoinSwitch for RRN == " +
	 * fields.get(FieldType.RRN.getName()) + " and PG Ref Num == " +
	 * fields.get(FieldType.PG_REF_NUM.getName()), e); } }
	 */

	public void sendCallbackAfterUpiAutoPayAmountDebit(Fields fields) {
		User user = new User();
		user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(user.getPayId());

		if (merchantSettings.isCallBackFlag() && StringUtils.isNotBlank(merchantSettings.getCallBackUrl())) {
			JsonObject json = new JsonObject();
			try {

				Date date = new Date();
				SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
				String strDate = df.format(date);

				Fields newFields = new Fields();
				for (int i = 0; i < fields.size(); i++) {
					Collection<String> upiAutoPayCallbackFields = SystemProperties.getUpiAutoPayTxnCallbackFields();
					for (String key : upiAutoPayCallbackFields) {
						if (StringUtils.isNotBlank(fields.get(key))) {
							newFields.put(key, fields.get(key));
						}
					}
				}
				newFields.put(FieldType.RESPONSE_DATE_TIME.getName(), strDate);
				String hash = Hasher.getHash(newFields);
				newFields.put("HASH", hash);

				List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
				for (String fieldType : fieldTypeList) {
					json.addProperty(fieldType, newFields.get(fieldType));
				}

				String serviceUrl = merchantSettings.getCallBackUrl();
				logger.info("Callback url >>> " + serviceUrl);
				HttpPost request = new HttpPost(serviceUrl);

				int CONNECTION_TIMEOUT_MS = 30 * 1000; // Timeout in millis.
				RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
						.setConnectTimeout(CONNECTION_TIMEOUT_MS).setSocketTimeout(CONNECTION_TIMEOUT_MS).build();

				request.setConfig(requestConfig);

				CloseableHttpClient httpClient = HttpClientBuilder.create().build();

				logger.info("Callback Request for PG_REF_NUM " + newFields.get(FieldType.PG_REF_NUM.getName()) + " to "
						+ user.getBusinessName() + " >>> " + json.toString());
				StringEntity params = new StringEntity(json.toString());

				request.addHeader("content-type", "application/json");
				request.setEntity(params);

				HttpResponse resp = httpClient.execute(request);
				HttpEntity response = resp.getEntity();
				String responseBody = EntityUtils.toString(response);
				logger.info("Callback Response from " + user.getBusinessName() + " >>> " + responseBody.toString());

			} catch (Exception e) {
				logger.error("Exception in sending call back response to " + user.getBusinessName()
						+ " for Order Id == " + fields.get(FieldType.ORDER_ID.getName()) + " and PG Ref Num == "
						+ fields.get(FieldType.PG_REF_NUM.getName()), e);
			}
		}
	}

	public void insertRefundNBTxnForManualRefund(Map<String, String> refundDataRequest) {
		logger.info("Insert in insertRefundNBTxnForManualRefund(), FieldsDao");
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			refundDataRequest.put(FieldType.CREATE_DATE.getName(), dateNow);
			refundDataRequest.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));

			BasicDBObject newFieldsObj = new BasicDBObject();
			for (String columnName : refundDataRequest.keySet()) {
				newFieldsObj.put(columnName, refundDataRequest.get(columnName));
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.REFUND_PENDING_TXN_DATA.getValue()));
			Document doc = new Document(newFieldsObj);
			collection.insertOne(doc);
		} catch (Exception exception) {
			logger.error("Exception while inserting insertRefundNBTxnForManualRefund() ", exception);
		}

	}

	public void updateCashfreePayoutCallbackTxn(Fields fields) throws SystemException {

		try {
			logger.info("inside updateCashfreePayoutCallbackTxn()");

			String event = fields.get("event");
			String txnId = fields.get("transferId");
			String refId = fields.get("referenceId");
			String acknowledged = fields.get("acknowledged");
			String eventTime = fields.get("eventTime");
			String utr = fields.get("utr");
			String reason = fields.get("reason");

			String status = null;
			String pgTxnMsg = null;
			String pgResponsecode = null;

			if (event.equalsIgnoreCase("TRANSFER_SUCCESS")) {
				status = StatusType.CAPTURED.getName();
				pgTxnMsg = ErrorType.SUCCESS.name();
				pgResponsecode = ErrorType.SUCCESS.getCode();
			} else {
				status = StatusType.FAILED.getName();
				pgTxnMsg = reason;
				pgResponsecode = ErrorType.FAILED.getCode();
			}

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject filterQuery = new BasicDBObject(FieldType.TXN_ID.getName(), txnId);

			MongoCursor<Document> cursor = collection.find(filterQuery).iterator();

			if (cursor.hasNext()) {

				Document doc = cursor.next();

				fields.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName()));
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), doc.getString(FieldType.SUB_MERCHANT_ID.getName()));
				fields.put(FieldType.CREATE_DATE.getName(), doc.getString(FieldType.CREATE_DATE.getName()));
				fields.put(FieldType.AMOUNT.getName(), doc.getString(FieldType.AMOUNT.getName()));
				fields.put(FieldType.ACQUIRER_NAME.getName(), doc.getString(FieldType.ACQUIRER_NAME.getName()));
				fields.put(FieldType.VIRTUAL_AC_CODE.getName(), doc.getString(FieldType.VIRTUAL_AC_CODE.getName()));
				fields.put(FieldType.CURRENCY_CODE.getName(), doc.getString(FieldType.CURRENCY_CODE.getName()));
				fields.put(FieldType.TXNTYPE.getName(), doc.getString(FieldType.TXNTYPE.getName()));
				fields.put(FieldType.TXN_ID.getName(), txnId);
				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
				fields.put(FieldType.PG_RESP_CODE.getName(), pgResponsecode);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), pgTxnMsg);
				fields.put(FieldType.RESPONSE_CODE.getName(), pgResponsecode);

				Bson newValue = new Document(FieldType.STATUS.getName(), status)
						.append(FieldType.UPDATE_DATE.getName(), dateNow)
						.append(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg)
						.append(FieldType.PG_RESP_CODE.getName(), pgResponsecode)
						.append(FieldType.PG_TXN_STATUS.getName(), event)
						.append(FieldType.IS_STATUS_FINAL.getName(), Constants.Y_FLAG.getValue());

				if (StringUtils.isNotBlank(utr)) {
					((Document) newValue).append(FieldType.RRN.getName(), utr);
					fields.put(FieldType.RRN.getName(), utr);

				}
				if (StringUtils.isNotBlank(utr)) {
					((Document) newValue).append(FieldType.ACQ_ID.getName(), refId);
					fields.put(FieldType.ACQ_ID.getName(), utr);
				}

				if (status.equalsIgnoreCase(StatusType.CAPTURED.getName())) {
					((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode())
							.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.name())
							.append(FieldType.PG_DATE_TIME.getName(), eventTime);
				} else {
					((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode())
							.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.name())
							.append(FieldType.PG_DATE_TIME.getName(), eventTime);
				}

				Bson updateOperationDocument = new Document("$set", newValue);
				collection.updateOne(filterQuery, updateOperationDocument);
				logger.info("Cashfree payout Callback Transaction Document updated for txn Id " + txnId);
			}
		} catch (Exception exception) {
			String message = "Error while inserting Cashfree Payout Callback Response in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void updateIciciCibFields(Fields fields) throws SystemException {
		try {
			logger.info("updateIciciCibFields() Status Fields" + fields.getFields() + " txn Id"
					+ fields.get(FieldType.TXN_ID.getName()));
			logger.info("updateIciciCibFields() Transaction Document update for txn Id "
					+ fields.get(FieldType.TXN_ID.getName()));
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(propertiesManager.propertiesMap
					.get(prefix + Constants.NODAL_SETTLEMENT_COLLECTION_NAME.getValue()));
			Bson filter = new Document(FieldType.TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			Bson newValue = new Document(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()))
					.append(FieldType.UPDATE_DATE.getName(), dateNow)
					.append(FieldType.RRN.getName(), fields.get(FieldType.RRN.getName()))
					.append(FieldType.UTR.getName(), fields.get(FieldType.UTR.getName()))
					.append(FieldType.PG_TXN_MESSAGE.getName(), fields.get(FieldType.PG_TXN_MESSAGE.getName()))
					.append(FieldType.PG_RESP_CODE.getName(), fields.get(FieldType.PG_RESP_CODE.getName()));

			if (StringUtils.isNotBlank(fields.get(FieldType.ACQ_ID.getName())))
				((Document) newValue).append(FieldType.ACQ_ID.getName(), fields.get(FieldType.ACQ_ID.getName()));

			if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
				((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode())
						.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.name());
			} else if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.DECLINED.name())) {
				((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.DECLINED.getCode())
						.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DECLINED.name());

			} else if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.REJECTED.name())) {
				((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode())
						.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED.name());

			} else if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.TIMEOUT.name())) {
				((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.TIMEOUT.getCode())
						.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TIMEOUT.name());
			} else if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PROCESSING.name())) {
				((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.PROCESSING.getCode())
						.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PROCESSING.name());
			} else {
				((Document) newValue)
						.append(FieldType.RESPONSE_CODE.getName(), fields.get(FieldType.RESPONSE_CODE.getName()))
						.append(FieldType.RESPONSE_MESSAGE.getName(), fields.get(FieldType.RESPONSE_MESSAGE.getName()));

			}

			// logger.info("Filter Query " + filter.toString() + " updated Query
			// is " + newValue.toString());

			Bson updateOperationDocument = new Document("$set", newValue);
			collection.updateOne(filter, updateOperationDocument);
			logger.info("updateIciciCibFields() Transaction Document updated for txn Id "
					+ fields.get(FieldType.TXN_ID.getName()));
		} catch (Exception exception) {
			String message = "Error while updating txn Response in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

	}

	public String getPayoutTxnStatus(String txnId) {

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject filterQuery = new BasicDBObject(FieldType.TXN_ID.getName(), txnId);

			MongoCursor<Document> cursor = collection.find(filterQuery).iterator();

			if (cursor.hasNext()) {

				Document doc = cursor.next();

				return doc.getString(FieldType.STATUS.getName());
			}
		} catch (Exception e) {
			logger.info("exception in getPayoutTxnStatus()", e);
		}
		return null;
	}

	public String sentToBankCreateDate(Fields fields) {
		String dateTime = "";

		try {
			logger.info("Inside sentToBankCreateDate (PG_REF_NUM): " + fields.get(FieldType.PG_REF_NUM.getName()));
			List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> statusConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName())));
			condList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));
			BasicDBObject txnTypeQuery = new BasicDBObject();
			txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.ENROLL.getName()));
			txnTypeQuery.append("$or", txnTypeConditionLst);
			condList.add(txnTypeQuery);

			BasicDBObject statusQuery = new BasicDBObject();
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ENROLLED.getName()));
			statusQuery.append("$or", statusConditionLst);
			condList.add(statusQuery);

			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(saleQuery).iterator();
			try {
				if (cursor.hasNext()) {
					Document documentObj = (Document) cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						documentObj = dataEncDecTool.decryptDocument(documentObj);
					}

					if (null != documentObj) {
						dateTime = documentObj.get(FieldType.CREATE_DATE.getName()).toString();
					}
				}
			} finally {
				cursor.close();
			}

		} catch (Exception exception) {
			String message = "Error while previous based on Pg Ref Num from database";
			logger.error(message, exception);
		}

		return dateTime;
	}

	// This method is for auto refund utility for UPI transaction having IDFC
	// acquirer
	public void upiHostedBlock(Fields fields) {
		// This block is for UPI hosted model
		try {
			if (StringUtils.isNotBlank(fields.get(FieldType.STATUS.getName()))
					&& fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())
					&& StringUtils.isNotBlank(fields.get(FieldType.PAYMENT_TYPE.getName()))
					&& fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())
					&& StringUtils.isNotBlank(fields.get(FieldType.ACQUIRER_TYPE.getName()))
					&& fields.get(FieldType.ACQUIRER_TYPE.getName()).equalsIgnoreCase(AcquirerType.IDFCUPI.getCode())) {
				UserSettingData user = null;
				if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
					user = userSettingDao.fetchDataUsingPayId(fields.get(FieldType.SUB_MERCHANT_ID.getName()));
				} else {
					user = userSettingDao.fetchDataUsingPayId(fields.get(FieldType.PAY_ID.getName()));
				}

				if (user.isUpiHostedFlag()) {
					if (user.isConfigurableFlag() && StringUtils.isNotBlank(user.getConfigurableTime())) {

						int configurableTime = Integer.parseInt(user.getConfigurableTime());
						Date date = new Date();
						SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyhhmmss");
						String currentDate = df.format(date);
						logger.info("CurrentDateTime >> " + currentDate);
						// fetching send To Bank Entry

						String sentToBankTime = sentToBankCreateDate(fields);

						Date date1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(sentToBankTime);
						sentToBankTime = df.format(date1);

						logger.info("sentToBankDateTime >> " + sentToBankTime);
						long timeDifference = Long.valueOf(currentDate) - Long.valueOf(sentToBankTime);
						logger.info("TimeDifference >> " + timeDifference + " s");
						if (timeDifference > configurableTime) {
							logger.info(
									"Process the refund for pgRefnum >> " + fields.get(FieldType.PG_REF_NUM.getName()));
							// process the refund
							Fields newFields = new Fields();

							newFields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
							newFields.put(FieldType.CURRENCY_CODE.getName(),
									fields.get(FieldType.CURRENCY_CODE.getName()));
							newFields.put(FieldType.AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
							newFields.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
							newFields.put(FieldType.REFUND_FLAG.getName(), "R");
							newFields.put(FieldType.REFUND_ORDER_ID.getName(),
									TransactionManager.getNewTransactionId());
							newFields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
							newFields.put(FieldType.TXNTYPE.getName(), "REFUND");
							String hash = Hasher.getHash(newFields);
							newFields.put(FieldType.HASH.getName(), hash);

							Map<String, String> res = transactionControllerServiceProvider.transact(newFields,
									"TransactionWSTransactURL");

							Fields callbackFields = new Fields(res);

							logger.info("Auto refund transaction with order id == "
									+ callbackFields.get(FieldType.ORDER_ID.getName())
									+ " with response received from pg ws == "
									+ callbackFields.get(FieldType.PG_REF_NUM.getName()));
							logger.info("send callback after refund process for pgRefnum >> "
									+ callbackFields.get(FieldType.PG_REF_NUM.getName()));
							// send callback to merchant
//							if (StringUtils.isNotBlank(callbackFields.get(FieldType.STATUS.getName()))
//									&& StringUtils.isNotBlank(callbackFields.get(FieldType.PAY_ID.getName()))) {
//								sendCallback(callbackFields);
//							}
							fields.put(FieldType.REFUND_ORDER_ID.getName(),
									callbackFields.get(FieldType.REFUND_ORDER_ID.getName()));
							fields.put("REFUND_INITIATED", "Y");
							fields.put("REFUND_STATUS", callbackFields.get(FieldType.STATUS.getName()));
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception in UPIHOSTED BLOCK >> ", e);
		}
	}

	@SuppressWarnings("static-access")
	public boolean checkAlreadyCaptured(String pgRefNum) {

		try {

			boolean response = false;
			Fields fields = new Fields();
			// PropertiesManager propManager = new PropertiesManager();
			List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
			dbObjList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			BasicDBObject txnTypeQuery = new BasicDBObject();

			txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			txnTypeConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			txnTypeQuery.append("$and", txnTypeConditionLst);

			dbObjList.add(txnTypeQuery);
			BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			long txnCount = coll.count(andQuery);

			if (txnCount > 0) {
				response = true;
			}
			return response;

		} catch (Exception e) {
			logger.error("Exception in checking duplicate capture", e);
			return false;
		}

	}

	public boolean getLatestSaleTransaction(Fields fields) throws SystemException {
		try {

			List<BasicDBObject> saleList = new ArrayList<BasicDBObject>();
			saleList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));

			if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				saleList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
						fields.get(FieldType.SUB_MERCHANT_ID.getName())));
			} else {
				saleList.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
			}

			saleList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));

			BasicDBObject saleQuery = new BasicDBObject("$and", saleList);

			logger.info("Sale Query for getLatestSaleTransaction() " + saleQuery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			FindIterable<Document> cursor = collection.find(saleQuery).sort(new BasicDBObject("CREATE_DATE", -1))
					.limit(1);

			MongoCursor<Document> cursor2 = cursor.iterator();

			if (cursor2.hasNext()) {

				Document doc = cursor2.next();

				fields.put(FieldType.TXN_ID.getName(), doc.getString(FieldType.TXN_ID.getName()));
				fields.put(FieldType.RESPONSE_CODE.getName(), doc.getString(FieldType.RESPONSE_CODE.getName()));
				fields.put(FieldType.MOP_TYPE.getName(), doc.getString(FieldType.MOP_TYPE.getName()));
				fields.put(FieldType.CARD_MASK.getName(), doc.getString(FieldType.CARD_MASK.getName()));
				fields.put(FieldType.ACQ_ID.getName(), doc.getString(FieldType.ACQ_ID.getName()));
				fields.put(FieldType.TXNTYPE.getName(), doc.getString(FieldType.TXNTYPE.getName()));
				fields.put(FieldType.RRN.getName(), doc.getString(FieldType.RRN.getName()));
				fields.put(FieldType.SURCHARGE_FLAG.getName(), doc.getString(FieldType.SURCHARGE_FLAG.getName()));
				fields.put(FieldType.PAYMENT_TYPE.getName(), doc.getString(FieldType.PAYMENT_TYPE.getName()));
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), doc.getString(FieldType.PG_TXN_MESSAGE.getName()));
				fields.put(FieldType.STATUS.getName(), doc.getString(FieldType.STATUS.getName()));
				fields.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(doc.getString(FieldType.AMOUNT.getName()),
						doc.getString(FieldType.CURRENCY_CODE.getName())));
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), doc.getString(FieldType.RESPONSE_MESSAGE.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), doc.getString(FieldType.ORIG_TXN_ID.getName()));
				fields.put(FieldType.TOTAL_AMOUNT.getName(),
						Amount.formatAmount(doc.getString(FieldType.TOTAL_AMOUNT.getName()),
								doc.getString(FieldType.CURRENCY_CODE.getName())));
				fields.put(FieldType.CUST_NAME.getName(), doc.getString(FieldType.CUST_NAME.getName()));
				fields.put(FieldType.IS_STATUS_FINAL.getName(), doc.getString(FieldType.IS_STATUS_FINAL.getName()));
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), doc.getString(FieldType.SUB_MERCHANT_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName()));
				fields.put(FieldType.PARENT_PAY_ID.getName(), doc.getString(FieldType.PARENT_PAY_ID.getName()));

				return true;
			}

		} catch (Exception exception) {
			String message = "Error while geting getLatestSaleTransaction()";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

		return false;
	}

	public boolean checkRazorpayDuplicateCapture(String acq_id, String rrn) throws SystemException {
		try {

			List<BasicDBObject> saleList = new ArrayList<BasicDBObject>();
			saleList.add(new BasicDBObject(FieldType.ACQ_ID.getName(), acq_id));
			saleList.add(new BasicDBObject(FieldType.RRN.getName(), rrn));
			saleList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			saleList.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.RAZORPAY.getCode()));
			BasicDBObject saleQuery = new BasicDBObject("$and", saleList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			long count = collection.count(saleQuery);

			if (count > 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception exception) {
			String message = "Error while fetching Sale capture count for Razorpay from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public boolean checkFloxypayDuplicateCapture(String acq_id, String payId) throws SystemException {
		try {

			List<BasicDBObject> saleList = new ArrayList<BasicDBObject>();
			saleList.add(new BasicDBObject(FieldType.ACQ_ID.getName(), acq_id));
			saleList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			saleList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			saleList.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.FLOXYPAY.getCode()));
			BasicDBObject saleQuery = new BasicDBObject("$and", saleList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			long count = collection.count(saleQuery);

			if (count > 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception exception) {
			String message = "Error while fetching Sale capture count for Floxypay from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public boolean checkQaicashDuplicateCapture(String pgRefNum, String payId) throws SystemException {
		try {

			List<BasicDBObject> saleList = new ArrayList<BasicDBObject>();
			saleList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			saleList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			saleList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			saleList.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.QAICASH.getCode()));
			BasicDBObject saleQuery = new BasicDBObject("$and", saleList);
			logger.info("Qaicash Duplicate Captured query {}", saleQuery.toString());
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			long count = collection.count(saleQuery);

			if (count > 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception exception) {
			String message = "Error while fetching Sale capture count for Qaicash from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public boolean checkGlobalpayDuplicateCapture(String pgRefNum, String payId) throws SystemException {
		try {

			List<BasicDBObject> saleList = new ArrayList<BasicDBObject>();
			saleList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			saleList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			saleList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			saleList.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.GLOBALPAY.getCode()));
			BasicDBObject saleQuery = new BasicDBObject("$and", saleList);
			logger.info("Globalpay Duplicate Captured query {}", saleQuery.toString());
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			long count = collection.count(saleQuery);

			if (count > 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception exception) {
			String message = "Error while fetching Sale capture count for Globalpay from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public boolean checkGrezpayDuplicateCapture(String pgRefNum, String payId) throws SystemException {
		try {

			List<BasicDBObject> saleList = new ArrayList<BasicDBObject>();
			saleList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			saleList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			saleList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			saleList.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.GREZPAY.getCode()));
			BasicDBObject saleQuery = new BasicDBObject("$and", saleList);
			logger.info("Grezpay Duplicate Captured query {}", saleQuery.toString());
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			long count = collection.count(saleQuery);

			if (count > 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception exception) {
			String message = "Error while fetching Sale capture count for Grezpay from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public boolean checkUpigatewayDuplicateCapture(String pgRefNum, String payId) throws SystemException {
		try {

			List<BasicDBObject> saleList = new ArrayList<BasicDBObject>();
			saleList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			saleList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			saleList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			saleList.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.UPIGATEWAY.getCode()));
			BasicDBObject saleQuery = new BasicDBObject("$and", saleList);
			logger.info("UpiGateway Duplicate Captured query {}", saleQuery.toString());
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			long count = collection.count(saleQuery);

			if (count > 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception exception) {
			String message = "Error while fetching Sale capture count for UpiGateway from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public boolean checkToshaniDigitalDuplicateCapture(String acqId, String payId) throws SystemException {
		try {

			List<BasicDBObject> saleList = new ArrayList<BasicDBObject>();
			saleList.add(new BasicDBObject(FieldType.ACQ_ID.getName(), acqId));
			saleList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			saleList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			saleList.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.TOSHANIDIGITAL.getCode()));
			BasicDBObject saleQuery = new BasicDBObject("$and", saleList);
			logger.info("TOSHANIDIGITAL Duplicate Captured query {}", saleQuery.toString());
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			long count = collection.count(saleQuery);

			if (count > 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception exception) {
			String message = "Error while fetching Sale capture count for TOSHANIDIGITAL from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public boolean checkDigSolDupCapture(String pgRefNum, String payId) throws SystemException {
		try {

			List<BasicDBObject> saleList = new ArrayList<BasicDBObject>();
			saleList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			saleList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			saleList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			saleList.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.DIGITALSOLUTIONS.getCode()));
			BasicDBObject saleQuery = new BasicDBObject("$and", saleList);
			logger.info("DigitalSolution Duplicate Captured query {}", saleQuery.toString());
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			long count = collection.count(saleQuery);

			if (count > 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception exception) {
			String message = "Error while fetching Sale capture count for DigitalSolution from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public boolean getCapturedForPgRrnAndPayId(String payId, String rrn) throws SystemException {

		boolean isCaptured = false;
		try {
			logger.info("Inside getCapturedForPgRrnAndPayId payid " + payId + " rrn" + rrn);

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			condList.add(new BasicDBObject(FieldType.RRN.getName(), rrn));
			condList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			condList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			long count = collection.count(saleQuery);
			if (count > 0) {
				isCaptured = true;
			}

			return isCaptured;
		} catch (Exception exception) {
			String message = "Error while checking captued via pg ref num from database";
			logger.error(message, exception);
			return isCaptured;
		}

	}

	@SuppressWarnings("static-access")
	public boolean checkDuplicateOrderId(Fields fields) throws SystemException {

		boolean isExist = false;
		try {
			logger.info("Inside checkDuplicateOrderId()");

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
			condList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));
			condList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(),
					fields.get(FieldType.CURRENCY_CODE.getName())));
			BasicDBObject searchQuery = new BasicDBObject("$and", condList);

			logger.info("Inside checkDuplicateOrderId: QUERY " + searchQuery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

			long count = collection.count(searchQuery);
			if (count > 0) {
				isExist = true;
			}

			return isExist;
		} catch (Exception exception) {
			String message = "Error while check Duplicate Order via OrderId from database";
			logger.error(message, exception);
			return isExist;
		}

	}

	@SuppressWarnings("static-access")
	public void updateSentToBankTransaction(Fields fields) throws SystemException {
		try {
			MongoDatabase dbIns = null;
			BasicDBObject oldFieldsObj = new BasicDBObject();
			oldFieldsObj.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
			oldFieldsObj.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
			BasicDBObject newFieldsObj = new BasicDBObject();
			newFieldsObj.put(FieldType.ACQ_ID.getName(), fields.get(FieldType.ACQ_ID.getName()));
			BasicDBObject updateObj = new BasicDBObject();
			updateObj.put("$set", newFieldsObj);
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			Document newDoc = new Document(updateObj);
			Document oldDoc = new Document(oldFieldsObj);
			collection.updateOne(oldDoc, newDoc);
			MongoDatabase dbIns1 = mongoInstance.getDB();
			MongoCollection<Document> collection1 = dbIns1.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			newDoc = new Document(updateObj);
			oldDoc = new Document(oldFieldsObj);
			collection.updateOne(oldDoc, newDoc);
			collection1.updateOne(oldDoc, newDoc);
		} catch (Exception exception) {
			String message = "Error while Updating transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	@SuppressWarnings("static-access")
	public void updateUTRInSentToBankTransaction(Fields fields) throws SystemException {
		try {
			MongoDatabase dbIns = null;
			BasicDBObject oldFieldsObj = new BasicDBObject();
			oldFieldsObj.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
			oldFieldsObj.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
			BasicDBObject newFieldsObj = new BasicDBObject();
			newFieldsObj.put(FieldType.ACQ_ID.getName(), fields.get(FieldType.UTR.getName()));
			newFieldsObj.put(FieldType.RRN.getName(), fields.get(FieldType.UTR.getName()));
			BasicDBObject updateObj = new BasicDBObject();
			updateObj.put("$set", newFieldsObj);
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			Document newDoc = new Document(updateObj);
			Document oldDoc = new Document(oldFieldsObj);
			collection.updateOne(oldDoc, newDoc);
			MongoDatabase dbIns1 = mongoInstance.getDB();
			MongoCollection<Document> collection1 = dbIns1.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			newDoc = new Document(updateObj);
			oldDoc = new Document(oldFieldsObj);
			collection.updateOne(oldDoc, newDoc);
			collection1.updateOne(oldDoc, newDoc);
		} catch (Exception exception) {
			String message = "Error while Updating transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public Fields getPreviousDataFromSentToBank(Fields fields) throws SystemException {

		try {
			logger.info("Inside getPreviousDataFromSentToBank (): ");
			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName())));
			condList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			condList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));
			BasicDBObject saleQuery = new BasicDBObject("$and", condList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(saleQuery).iterator();
			try {
				while (cursor.hasNext()) {
					Document doc = cursor.next();
					if (!doc.isEmpty()) {
						if (StringUtils.isNotBlank(String.valueOf(doc.get(FieldType.ACQ_ID.getName())))) {
							fields.put(FieldType.ACQ_ID.getName(), String.valueOf(doc.get(FieldType.ACQ_ID.getName())));
						}
						if (StringUtils.isNotBlank(String.valueOf(doc.get(FieldType.RRN.getName())))) {
							fields.put(FieldType.RRN.getName(), String.valueOf(doc.get(FieldType.RRN.getName())));
						}
						if (StringUtils.isNotBlank(String.valueOf(doc.get(FieldType.ACQUIRER_TYPE.getName())))) {
							fields.put(FieldType.ACQUIRER_TYPE.getName(),
									String.valueOf(doc.get(FieldType.ACQUIRER_TYPE.getName())));
						}
					}
				}
			} finally {
				cursor.close();
			}
			return fields;
		} catch (Exception exception) {
			String message = "Error while previous based on orderId from database";
			logger.error(message, exception);
			return null;
		}
	}

}