package com.paymentgateway.pg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;

@Service
public class PaymentRequestProcessor {

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private GeneralValidator generalValidator;

	@Autowired
	private UserDao userDao;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(PaymentRequestProcessor.class.getName());
	private static Map<String, User> userMap = new HashMap<String, User>();

	public Map<String, String> process(Fields fields) throws SystemException {

		if (StringUtils.isNotBlank(fields.get(FieldType.PAY_ID.getName()))) {

			String payId = fields.get(FieldType.PAY_ID.getName());

			if (!validateUser(payId)) {
				
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Merchant is inactive");
				return fields.getFields();
			}
			
		}
		else {
			
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "PAY_ID not found");
			return fields.getFields();
		}
		
		
		if (StringUtils.isNotBlank(fields.get(FieldType.ORDER_ID.getName()))) {

			String payId = fields.get(FieldType.PAY_ID.getName());
			String orderId = fields.get(FieldType.ORDER_ID.getName());
			
			if (!validateOrderId(orderId, payId)) {
				
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Order Id Duplicate");
				return fields.getFields();
			};
			
		}
	

		if (validateHash(fields)) {

			if (!validatePayRequest(fields)) {

				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Transaction already in process for this terminal");
				return fields.getFields();
			}

			if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Missing Pay Id in Request");
				return fields.getFields();
			}

			if (StringUtils.isBlank(fields.get(FieldType.ORDER_ID.getName()))) {
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Missing Order Id in Request");
				return fields.getFields();
			}

			if (StringUtils.isBlank(fields.get(FieldType.AMOUNT.getName()))) {
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Missing Amount in Request");
				return fields.getFields();
			}

			if (StringUtils.isBlank(fields.get(FieldType.TERMINAL_ID.getName()))) {
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Missing Terminal Id in Request");
				return fields.getFields();
			}

			if (StringUtils.isBlank(fields.get(FieldType.SERVICE_ID.getName()))) {
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Missing Service Id in Request");
				return fields.getFields();
			}

			if (StringUtils.isBlank(fields.get(FieldType.CUST_NAME.getName()))) {
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Missing Customer Name in Request");
				return fields.getFields();
			}

			validateFields(fields);

			if (StringUtils.isNotBlank(fields.get(FieldType.RESPONSE_CODE.getName()))
					&& fields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase(ErrorType.REJECTED.getCode())) {
				return fields.getFields();
			}

			try {

				fieldsDao.insertPaymentRequest(fields);
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				return fields.getFields();
			}

			catch (SystemException e) {

				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), e.getLocalizedMessage());
			}

		} else {

			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Hash");
			return fields.getFields();
		}

		return fields.getFields();
	}

	public boolean validateHash(Fields fields) {

		try {

			if (propertiesManager.propertiesMap.get("AllowFailedHash").equalsIgnoreCase("1")) {
				return true;
			}

			String merchantHash = fields.get(FieldType.HASH.getName());

			if (StringUtils.isNotBlank(merchantHash)) {
				fields.remove(FieldType.HASH.getName());
				String calculatedHash = Hasher.getHash(fields);

				logger.info("New Payment Request , merchant Hash == " + merchantHash);
				logger.info("New Payment Request , calculated Hash == " + calculatedHash);
				if (calculatedHash.equalsIgnoreCase(merchantHash)) {
					return true;
				} else {
				logger.info("Merchant Hash and Calculated Hash donot match");
					return false;
				}
			} else {
				return false;
			}

		}

		catch (Exception e) {
			logger.error("Unable to validate Hash : " , e);
			return false;
		}

	}

	public boolean validatePayRequest(Fields fields) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TXN_REQUEST_COLLECTION.getValue()));

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject terminalQuery = new BasicDBObject(FieldType.TERMINAL_ID.getName(),
					fields.get(FieldType.TERMINAL_ID.getName()));
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), TDRStatus.ACTIVE.getName());

			paramConditionLst.add(statusQuery);
			paramConditionLst.add(terminalQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramConditionLst);

			long count = collection.count(finalQuery);

			if (count > 0) {

				return false;
			} else {
				return true;
			}

		}

		catch (Exception e) {
			logger.error("Unable to Check Pending Transactions : " , e);
			return false;
		}

	}

	public void validateFields(Fields fields) {

		try {
			generalValidator.validateField(FieldType.PAY_ID, FieldType.PAY_ID.getName(), fields);
			generalValidator.validateField(FieldType.ORDER_ID, FieldType.ORDER_ID.getName(), fields);
			generalValidator.validateField(FieldType.TERMINAL_ID, FieldType.TERMINAL_ID.getName(), fields);
			generalValidator.validateField(FieldType.SERVICE_ID, FieldType.SERVICE_ID.getName(), fields);
			generalValidator.validateField(FieldType.AMOUNT, FieldType.AMOUNT.getName(), fields);
			generalValidator.validateField(FieldType.CUST_NAME, FieldType.CUST_NAME.getName(), fields);

		} catch (SystemException e) {
			logger.error("Exception in validation of fields : " , e);
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), e.getMessage());
		}
	}

	public boolean validateUser(String payId) {

		User merchant = null;

		if (userMap.get(payId) != null) {
			merchant = userMap.get(payId);
		} else {

			merchant = userDao.findPayId(payId);
			userMap.put(payId, merchant);
		}

		if (merchant == null) {
			return false;
		} else if (merchant.getUserStatus().equals(UserStatusType.ACTIVE)) {
			return true;
		} else {
			return false;
		}

	}
	
	
	public boolean validateOrderId(String orderId , String payId) {


		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> collection = dbIns.getCollection(
				propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		
		
		List<BasicDBObject> capturedList = new ArrayList<BasicDBObject>();
		capturedList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		capturedList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		capturedList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		capturedList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
		BasicDBObject saleQuery = new BasicDBObject("$and", capturedList);
		
		if (collection.count(saleQuery) > 0) {
			return false;
		}
		else {
			return true;
		}

		
	}

}
