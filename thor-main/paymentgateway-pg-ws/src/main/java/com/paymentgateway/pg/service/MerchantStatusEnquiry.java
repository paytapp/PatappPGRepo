package com.paymentgateway.pg.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.ModeType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StaticDataProvider;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.SystemProperties;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;
import com.paymentgateway.pg.core.util.CryptoUtil;

@Service
public class MerchantStatusEnquiry {

	private static Logger logger = LoggerFactory.getLogger(MerchantStatusEnquiry.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private GeneralValidator generalValidator;

	private static final Collection<String> aLLDB_Fields = SystemProperties.getDBFields();

	@Autowired
	private StaticDataProvider staticDataProvider;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public Map<String, String> process(Fields fields) throws SystemException {
		try {
			generalValidator.validate(fields);
			validateSuperMerchantDetails(fields);
			validateParentMerchants(fields);
			Fields previous = refreshPreviousForStatus(fields);
			populateFieldsFromPrevious(fields, previous);
			addResponseFields(fields);
			updateSubMerchantDetails(fields);
			updateParentMerchantDetails(fields);
			removeInvalidResponseFields(fields);
			secureResponse(fields);
			updateError(fields);
			addHash(fields);
			return fields.getFields();
		} catch (Exception exception) {
			logger.error("Exception", exception);
			fields.remove(FieldType.INTERNAL_VALIDATE_HASH_YN.getName());
			fields.remove(FieldType.INTERNAL_REQUEST_FIELDS.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), exception.getLocalizedMessage());
			if (!fields.get(FieldType.RESPONSE_MESSAGE.getName()).equalsIgnoreCase("Invalid PAY_ID")) {
				addHash(fields);
			}

			return fields.getFields();
		}

	}

	public Fields createAllSelectForStatus(String orderId, String payId, String amount, String currencyCode) {

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

	public Fields refreshPreviousForStatus(Fields fields) throws SystemException {
		Fields previous = null;
		String orderId = fields.get(FieldType.ORDER_ID.getName());
		String payId = fields.get(FieldType.PAY_ID.getName());
		String amount = fields.get(FieldType.AMOUNT.getName());
		amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
		String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
		previous = createAllSelectForStatus(orderId, payId, amount, currencyCode);
		UserDao userDao = new UserDao();
		if (previous != null && previous.get("TXN_CAPTURE_FLAG") != null
				&& previous.get("TXN_CAPTURE_FLAG").equalsIgnoreCase("Post Captured")) {
			if (userDao.getAcceptPostCaptureInStatus(payId)) {
				previous = getPreviousTxnForPostCaptured(orderId, payId, amount, currencyCode);
			}
		} else {
			if (userDao.getAcceptPostCaptureInStatus(payId)) {
				Fields checkLatest = null;
				checkLatest = getPreviousDocForTxn(orderId, payId, amount, currencyCode);
				if (checkLatest != null) {
					previous = checkLatest;
				}
			}
		}

		return previous;
	}

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

	public void validateSuperMerchantDetails(Fields fields) throws SystemException {
		String txntype = fields.get(FieldType.TXNTYPE.getName());
		if (txntype.equals(TransactionType.STATUS.getName()) || txntype.equals(TransactionType.REFUND.getName())) {

			User user = null;
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				user = staticDataProvider.getUserData(fields.get(FieldType.SUB_MERCHANT_ID.getName()));
			} else {
				UserDao userDao = new UserDao();
				user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			}
			if (user != null && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId());
				fields.put(FieldType.IS_SUB_MERCHANT.getName(), "Y");
			}
		}

	}

	public void validateParentMerchants(Fields fields) throws SystemException {
		String txntype = fields.get(FieldType.TXNTYPE.getName());
		if (txntype.equals(TransactionType.STATUS.getName()) || txntype.equals(TransactionType.REFUND.getName())) {

			User user = null;
			UserDao userDao = new UserDao();
			user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

			if (user != null && user.getUserType() == UserType.PARENTMERCHANT) {
				FieldsDao fieldsDao = new FieldsDao();
				Fields newFields = fieldsDao.getPreviousForPgRefNum(fields.get(FieldType.PG_REF_NUM.getName()));
				fields.put(FieldType.PARENT_PAY_ID.getName(), newFields.get(FieldType.PARENT_PAY_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), newFields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.IS_PARENT_MERCHANT.getName(), "Y");
			}
		}

	}

	public void addHash(Fields fields) throws SystemException {
		String internalFlag = fields.get(FieldType.IS_INTERNAL_REQUEST.getName());
		if (!(StringUtils.isNotEmpty(internalFlag) && internalFlag.equals(Constants.Y_FLAG.getValue()))) {
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
		}
	}

	public void secureResponse(Fields fields) {
		CryptoUtil.truncateCardNumber(fields);
	}

	public void updateError(Fields fields) {
		String responseCode = fields.get(FieldType.RESPONSE_CODE.getName());
		if (null == responseCode) {
			responseCode = ErrorType.UNKNOWN.getCode();
			fields.put(FieldType.RESPONSE_CODE.getName(), responseCode);
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.UNKNOWN.getResponseMessage());
		}

		if (!responseCode.equals(ErrorType.SUCCESS.getCode())) {
			String status = fields.get(FieldType.STATUS.getName());

			if (null == status) {
				status = StatusType.ERROR.getName();
			} else if (status.equals(StatusType.APPROVED.getName()) || status.equals(StatusType.CAPTURED.getName())
					|| status.equals(StatusType.PENDING.getName())) {
				fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			}
		}
	}

	public void removeInvalidResponseFields(Fields fields) {

		String createDate = null;
		if (StringUtils.isNotBlank(fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName())) && fields
				.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equalsIgnoreCase(TransactionType.STATUS.getName())) {

			createDate = fields.get(FieldType.CREATE_DATE.getName());
		}
		;

		Fields responseFields = new Fields();
		String internalFlag = fields.get(FieldType.IS_INTERNAL_REQUEST.getName());
		Collection<String> validResponseFields = null;
		if (StringUtils.isNotEmpty(internalFlag) && internalFlag.equals(Constants.Y_FLAG.getValue())) {
			validResponseFields = SystemProperties.getInternalResponsefields();
		} else {
			validResponseFields = SystemProperties.getResponseFields();
		}
		for (String key : validResponseFields) {
			String value = fields.get(key);
			if (null != value) {
				responseFields.put(key, value);
			}
		}
		fields.clear();
		fields.putAll(responseFields.getFields());

		// Added for setting create date in status enquiry response

		if (StringUtils.isNotBlank(createDate)) {
			fields.put(FieldType.CREATE_DATE.getName(), createDate);
		}

	}

	public void addResponseFields(Fields fields) {
		final Date date = new Date();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
		fields.put(FieldType.RESPONSE_DATE_TIME.getName(), simpleDateFormat.format(date));
	}

	public void updateSubMerchantDetails(Fields fields) throws SystemException {
		String isSubMerchant = fields.get(FieldType.IS_SUB_MERCHANT.getName());
		if (StringUtils.isNotBlank(isSubMerchant) && isSubMerchant.equalsIgnoreCase("Y")) {
			if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
				fields.remove(FieldType.SUB_MERCHANT_ID.getName());
			}
		}

	}

	public void updateParentMerchantDetails(Fields fields) throws SystemException {
		String isParentMerchant = fields.get(FieldType.IS_PARENT_MERCHANT.getName());
		if (StringUtils.isNotBlank(isParentMerchant) && isParentMerchant.equalsIgnoreCase("Y")) {
			if (StringUtils.isNotBlank(fields.get(FieldType.PARENT_PAY_ID.getName()))) {
				fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PARENT_PAY_ID.getName()));
				fields.remove(FieldType.PARENT_PAY_ID.getName());
			}
		}
	}

	public void populateFieldsFromPrevious(Fields fields, Fields previous) throws SystemException {
		// Fields previous = fields.getPrevious();
		if (null != previous && previous.size() > 0) {

			fields.put(FieldType.ORDER_ID.getName(), previous.get(FieldType.ORDER_ID.getName()));
			String currencyCode = previous.get(FieldType.CURRENCY_CODE.getName());
			if (null != currencyCode) {
				fields.put(FieldType.CURRENCY_CODE.getName(), currencyCode);
			}

			String is_Status_Final = previous.get(FieldType.IS_STATUS_FINAL.getName());

			fields.put(FieldType.IS_STATUS_FINAL.getName(), Constants.Y_FLAG.getValue());
			// Acquirer of original transaction
			String acquirerType = previous.get(FieldType.ACQUIRER_TYPE.getName());
			if (null != acquirerType) {
				fields.put(FieldType.ACQUIRER_TYPE.getName(), acquirerType);
			}

			// get PG_REF_NO for capture
			String pgRefNo = previous.get(FieldType.PG_REF_NUM.getName());
			if (null != pgRefNo) {
				fields.put(FieldType.PG_REF_NUM.getName(), pgRefNo);
			}

			// OID of original transaction
			String oid = previous.get(FieldType.OID.getName());
			if (null != oid) {
				fields.put(FieldType.OID.getName(), oid);
			}

			// Acquirer of original transaction
			String acq_id = previous.get(FieldType.ACQ_ID.getName());
			if (StringUtils.isBlank(acq_id) || !(acq_id.equals("0"))) {
				fields.put(FieldType.ACQ_ID.getName(), acq_id);
			}

			// Mop type of original transaction
			String mopType = previous.get(FieldType.MOP_TYPE.getName());
			if (null != mopType) {
				fields.put(FieldType.MOP_TYPE.getName(), mopType);
			}

			String categoryCode = previous.get(FieldType.CATEGORY_CODE.getName());
			if (null != categoryCode) {
				fields.put(FieldType.CATEGORY_CODE.getName(), categoryCode);
			}

			String skuCode = previous.get(FieldType.SKU_CODE.getName());
			if (null != skuCode) {
				fields.put(FieldType.SKU_CODE.getName(), skuCode);
			}

			String prodName = previous.get(FieldType.PRODUCT_NAME.getName());
			if (null != prodName) {
				fields.put(FieldType.PRODUCT_NAME.getName(), prodName);
			}

			String quantity = previous.get(FieldType.QUANTITY.getName());
			if (null != quantity) {
				fields.put(FieldType.QUANTITY.getName(), quantity);
			}

			String productPrice = previous.get(FieldType.PRODUCT_PRICE.getName());
			if (null != productPrice) {
				fields.put(FieldType.PRODUCT_PRICE.getName(), productPrice);
			}
			String refundDays = previous.get(FieldType.REFUND_DAYS.getName());
			if (null != refundDays) {
				fields.put(FieldType.REFUND_DAYS.getName(), refundDays);
			}
			String vendorId = previous.get(FieldType.VENDOR_ID.getName());
			if (null != vendorId) {
				fields.put(FieldType.VENDOR_ID.getName(), vendorId);
			}

			String productAmount = previous.get(FieldType.PRODUCT_AMOUNT.getName());
			if (null != productAmount) {
				fields.put(FieldType.PRODUCT_AMOUNT.getName(), productAmount);
			}

			String refundCycleDays = previous.get(FieldType.REFUND_CYCLE_DAYS.getName());
			if (null != refundCycleDays) {
				fields.put(FieldType.REFUND_CYCLE_DAYS.getName(), refundCycleDays);
			}

			if (StringUtils.isNotBlank(previous.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), previous.get(FieldType.SUB_MERCHANT_ID.getName()));
			}

			if (StringUtils.isNotBlank(previous.get(FieldType.EPOS_MERCHANT.getName()))) {
				fields.put(FieldType.EPOS_MERCHANT.getName(), previous.get(FieldType.EPOS_MERCHANT.getName()));
			}
			if (StringUtils.isNotBlank(previous.get(FieldType.TRANSACTION_MODE.getName()))) {
				fields.put(FieldType.TRANSACTION_MODE.getName(), previous.get(FieldType.TRANSACTION_MODE.getName()));
			}
			if (StringUtils.isNotBlank(previous.get(FieldType.TXN_CAPTURE_FLAG.getName()))) {
				fields.put(FieldType.TXN_CAPTURE_FLAG.getName(), previous.get(FieldType.TXN_CAPTURE_FLAG.getName()));
			}

			// Payment type of original transaction
			String paymentType = previous.get(FieldType.PAYMENT_TYPE.getName());
			if (null != paymentType) {
				fields.put(FieldType.PAYMENT_TYPE.getName(), paymentType);
				if (paymentType.equals(PaymentType.UPI.getCode())) {
					String udf1 = previous.get(FieldType.UDF1.getName());
					if (null != udf1) {
						fields.put(FieldType.UDF1.getName(), udf1);
					}
					String udf2 = previous.get(FieldType.UDF2.getName());
					if (null != udf2) {
						fields.put(FieldType.UDF2.getName(), udf2);
					}
					String udf3 = previous.get(FieldType.UDF3.getName());
					if (null != udf3) {
						fields.put(FieldType.UDF3.getName(), udf3);
					}
					String udf4 = previous.get(FieldType.UDF4.getName());
					if (null != udf4) {
						fields.put(FieldType.UDF4.getName(), udf4);
					}
					String payerAddress = previous.get(FieldType.PAYER_ADDRESS.getName());
					if (null != payerAddress) {
						fields.put(FieldType.PAYER_ADDRESS.getName(), payerAddress);
					}
				}
			}

			String cardMask = previous.get(FieldType.CARD_MASK.getName());
			if (null != cardMask) {
				fields.put(FieldType.CARD_MASK.getName(), cardMask);
			}
			// Date & Time of original transaction
			String pgDateTime = previous.get(FieldType.PG_DATE_TIME.getName());
			if (null != pgDateTime) {
				fields.put(FieldType.PG_DATE_TIME.getName(), pgDateTime);
			}

			String txnType = fields.get(FieldType.TXNTYPE.getName());
			if ((txnType.equals(TransactionType.STATUS.getName())) || (txnType.equals(TransactionType.VERIFY.getName()))
					|| (txnType.equals(TransactionType.RECO.getName()))
					|| (txnType.equals(TransactionType.REFUNDRECO.getName()))) {
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), txnType);

				String preTxnType = previous.get(FieldType.ORIG_TXNTYPE.getName());
				if ((StringUtils.isNotBlank(preTxnType)) && (preTxnType.equals(TransactionType.RECO.getName())
						|| preTxnType.equals(TransactionType.REFUND.getName())
						|| preTxnType.equals(TransactionType.REFUNDRECO.getName()))) {
					fields.put(FieldType.TXNTYPE.getName(), preTxnType);
				} else {
					UserDao userDao = new UserDao();
					// User user = authenticator.getUserFromPayId(fields);
					User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
					ModeType previoustxnType = user.getModeType();
					if (null != previoustxnType) {
						fields.put(FieldType.TXNTYPE.getName(), previoustxnType.toString());
					}
				}

				String amount = previous.get(FieldType.AMOUNT.getName());
				if (null != amount) {
					fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(amount, currencyCode));
				}

				String txnMsg = previous.get(FieldType.PG_TXN_MESSAGE.getName());
				if (null != txnMsg) {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), txnMsg);
				}

				String responseCode = previous.get(FieldType.RESPONSE_CODE.getName());
				if (null != responseCode) {
					fields.put(FieldType.RESPONSE_CODE.getName(), responseCode);
				}

				String previoustxnId = previous.get(FieldType.PG_REF_NUM.getName());
				if (null != previoustxnId) {
					fields.put(FieldType.TXN_ID.getName(), previoustxnId);
				}

				String rrn = previous.get(FieldType.RRN.getName());
				if (null != rrn) {
					fields.put(FieldType.RRN.getName(), rrn);
				}

				String responseTime = previous.get(FieldType.CREATE_DATE.getName());
				if (null != responseTime) {
					fields.put(FieldType.RESPONSE_DATE_TIME.getName(), responseTime);
				}

				String responseMessage = previous.get(FieldType.RESPONSE_MESSAGE.getName());
				if (null != responseMessage) {
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), responseMessage);
				}

				String authCode = previous.get(FieldType.AUTH_CODE.getName());
				if (null != authCode) {
					fields.put(FieldType.AUTH_CODE.getName(), authCode);
				}

				String custPhone = previous.get(FieldType.CUST_PHONE.getName());
				if (null != custPhone) {
					fields.put(FieldType.CUST_PHONE.getName(), custPhone);
				}

				String desc = previous.get(FieldType.PRODUCT_DESC.getName());
				if (null != desc) {
					fields.put(FieldType.PRODUCT_DESC.getName(), desc);
				}

				String email = previous.get(FieldType.CUST_EMAIL.getName());
				if (null != email) {
					fields.put(FieldType.CUST_EMAIL.getName(), email);
				}

				String name = previous.get(FieldType.CUST_NAME.getName());
				if (null != name) {
					fields.put(FieldType.CUST_NAME.getName(), name);
				}

				String returnUrl = previous.get(FieldType.RETURN_URL.getName());
				if (null != returnUrl) {
					fields.put(FieldType.RETURN_URL.getName(), returnUrl);
				}

				String avr = previous.get(FieldType.AVR.getName());
				if (null != avr) {
					fields.put(FieldType.AVR.getName(), avr);
				}

				String status = previous.get(FieldType.STATUS.getName());
				if (null != status) {
					fields.put(FieldType.STATUS.getName(), status);
				}

				String totalAmount = previous.get(FieldType.TOTAL_AMOUNT.getName());
				if (null != totalAmount) {
					fields.put(FieldType.TOTAL_AMOUNT.getName(), Amount.formatAmount(totalAmount, currencyCode));
				}

				String createDate = previous.get(FieldType.CREATE_DATE.getName());
				if (null != createDate) {
					fields.put(FieldType.CREATE_DATE.getName(), createDate);
				}

			} else {
				String rrn = previous.get(FieldType.RRN.getName());
				if (null != rrn) {
					fields.put(FieldType.RRN.getName(), rrn);
				}

				String email = previous.get(FieldType.CUST_EMAIL.getName());
				if (null != email) {
					fields.put(FieldType.CUST_EMAIL.getName(), email);
				}

				String acquirerMode = previous.get(FieldType.ACQUIRER_MODE.getName());
				if (null != acquirerMode) {
					fields.put(FieldType.ACQUIRER_MODE.getName(), acquirerMode);
				}

				String name = previous.get(FieldType.CUST_NAME.getName());
				if (null != name) {
					fields.put(FieldType.CUST_NAME.getName(), name);
				}

				String totalAmount = previous.get(FieldType.TOTAL_AMOUNT.getName());
				if (null != totalAmount) {
					fields.put(FieldType.TOTAL_AMOUNT.getName(), Amount.formatAmount(totalAmount, currencyCode));
				}

				String saleAmount = previous.get(FieldType.AMOUNT.getName());
				if (null != saleAmount) {
					fields.put(FieldType.SALE_AMOUNT.getName(), Amount.formatAmount(saleAmount, currencyCode));
				}

				String saleTotalAmount = previous.get(FieldType.TOTAL_AMOUNT.getName());
				if (null != saleTotalAmount) {
					fields.put(FieldType.SALE_TOTAL_AMOUNT.getName(),
							Amount.formatAmount(saleTotalAmount, currencyCode));
				}

				String custPhone = previous.get(FieldType.CUST_PHONE.getName());
				if (null != custPhone) {
					fields.put(FieldType.CUST_PHONE.getName(), custPhone);
				}

				String udf7 = previous.get(FieldType.UDF7.getName());
				if (null != udf7) {
					fields.put(FieldType.UDF7.getName(), Amount.toDecimal(udf7, currencyCode));
				}
				String udf8 = previous.get(FieldType.UDF8.getName());
				if (null != udf8) {
					fields.put(FieldType.UDF8.getName(), Amount.toDecimal(udf8, currencyCode));
				}
				String udf9 = previous.get(FieldType.UDF9.getName());
				if (null != udf9) {
					fields.put(FieldType.UDF9.getName(), Amount.toDecimal(udf9, currencyCode));
				}
				String udf10 = previous.get(FieldType.UDF10.getName());
				if (null != udf10) {
					fields.put(FieldType.UDF10.getName(), Amount.toDecimal(udf10, currencyCode));
				}
				String udf11 = previous.get(FieldType.UDF11.getName());
				if (null != udf11) {
					fields.put(FieldType.UDF11.getName(), udf11);
				}
				String udf12 = previous.get(FieldType.UDF12.getName());
				if (null != udf12) {
					fields.put(FieldType.UDF12.getName(), udf12);
				}
				String udf13 = previous.get(FieldType.UDF13.getName());
				if (null != udf13) {
					fields.put(FieldType.UDF13.getName(), udf13);
				}
				String udf14 = previous.get(FieldType.UDF14.getName());
				if (null != udf14) {
					fields.put(FieldType.UDF14.getName(), udf14);
				}
				String udf15 = previous.get(FieldType.UDF15.getName());
				if (null != udf15) {
					fields.put(FieldType.UDF15.getName(), udf15);
				}
				String udf16 = previous.get(FieldType.UDF16.getName());
				if (null != udf16) {
					fields.put(FieldType.UDF16.getName(), udf16);
				}
				String udf17 = previous.get(FieldType.UDF17.getName());
				if (null != udf17) {
					fields.put(FieldType.UDF17.getName(), udf17);
				}
				String udf18 = previous.get(FieldType.UDF18.getName());
				if (null != udf18) {
					fields.put(FieldType.UDF18.getName(), udf18);
				}
				String cardHolderType = previous.get(FieldType.CARD_HOLDER_TYPE.getName());
				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), cardHolderType);

				String transactionsRegion = previous.get(FieldType.PAYMENTS_REGION.getName());
				fields.put(FieldType.PAYMENTS_REGION.getName(), transactionsRegion);

				String surchargeFlag = previous.get(FieldType.SURCHARGE_FLAG.getName());
				fields.put(FieldType.SURCHARGE_FLAG.getName(), surchargeFlag);

				String createDate = previous.get(FieldType.CREATE_DATE.getName());
				if (null != createDate) {
					fields.put(FieldType.CREATE_DATE.getName(), createDate);
				}

				String zName = previous.get(FieldType.Z_NAME.getName());
				if (null != zName) {
					fields.put(FieldType.Z_NAME.getName(), zName);
				}

				String cName = previous.get(FieldType.C_NAME.getName());
				if (null != cName) {
					fields.put(FieldType.C_NAME.getName(), cName);
				}

				String dName = previous.get(FieldType.D_NAME.getName());
				if (null != dName) {
					fields.put(FieldType.D_NAME.getName(), dName);
				}

				String receiptNo = previous.get(FieldType.RECIEPT_NO.getName());
				if (null != receiptNo) {
					fields.put(FieldType.RECIEPT_NO.getName(), receiptNo);
				}
			}
			String origTxnId = previous.get(FieldType.ORIG_TXN_ID.getName());
			if (null != origTxnId) {
				fields.put(FieldType.ORIG_TXN_ID.getName(), origTxnId);
			}
		}

	}

}
