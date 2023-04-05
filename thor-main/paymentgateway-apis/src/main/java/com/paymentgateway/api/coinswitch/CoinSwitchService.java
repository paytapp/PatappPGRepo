package com.paymentgateway.api.coinswitch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;

/*
*@auther Sandeep Sharma
*/

@Service
public class CoinSwitchService {

	@Autowired
	private GeneralValidator generalValidator;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private CrmValidator crmValidator;

	private static Logger logger = LoggerFactory.getLogger(CoinSwitchService.class.getName());
	private static final String prefix = "MONGO_DB_";

	public boolean validateHash(Fields fields) throws SystemException {
		String recievedHash = fields.remove(FieldType.HASH.getName());
		if (StringUtils.isEmpty(recievedHash)) {
			return false;
		}
		String calculateHash = Hasher.getHash(fields);
		if (!calculateHash.equalsIgnoreCase(recievedHash)) {
			StringBuilder hashMessage = new StringBuilder("Recieved hash >>>> ");
			hashMessage.append(recievedHash);
			hashMessage.append(", Calculated Hash >>>> ");
			hashMessage.append(calculateHash);
			logger.error(hashMessage.toString());
			return false;
		}
		return true;

	}

	@SuppressWarnings("static-access")
	public boolean checkUserExist(Fields fields) {
		Boolean exist = false;
		long count = 0;

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> collection = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COINSWITCH_ACCOUNTS.getValue()));

		List<BasicDBObject> cond = new ArrayList<BasicDBObject>();
	
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_ID.getName()))) {
			BasicDBObject accNoQuery = new BasicDBObject(FieldType.CUST_ID.getName(),
					fields.get(FieldType.CUST_ID.getName()));
			cond.add(accNoQuery);
		}
		BasicDBObject finalquery = new BasicDBObject("$and", cond);
		logger.info("Inside checkUserExist >>> " + finalquery);
		count = collection.countDocuments(finalquery);
		if (count > 0) {
			exist = true;
			return exist;
		}
		return exist;

	}


	public Map<String, String> validateFields(Fields fields) {
		Map<String, String> response = new HashMap<String, String>();
		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))
				|| !(crmValidator.validateField(CrmFieldType.PAY_ID, fields.get(FieldType.PAY_ID.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.ADDRESS.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_CUST_ADD.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_CUST_ADD.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.DOB.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_CUST_DOB.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_CUST_DOB.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.CUST_NAME.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_CUST_NAME.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_CUST_NAME.getResponseCode());
			return response;
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			Boolean isEmail = generalValidator.isValidEmailId(fields.get(FieldType.CUST_EMAIL.getName()));
			if (!isEmail) {
				response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_EMAILID.getResponseMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_EMAILID.getResponseCode());
				return response;
			}
		}
		if (StringUtils.isBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_EMAIL.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_EMAIL.getResponseCode());
			return response;
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.PAN.getName()))) {
			Boolean isPan = generalValidator.isValidPan(fields.get(FieldType.PAN.getName()));
			if (!isPan) {
				response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAN.getResponseMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAN.getResponseCode());
				return response;
			}
		}

		if (StringUtils.isBlank(fields.get(FieldType.PHONE_NO.getName()))
				|| !(crmValidator.validateField(CrmFieldType.MOBILE, fields.get(FieldType.PHONE_NO.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PHONE.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PHONE.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName())) || !(crmValidator
				.validateField(CrmFieldType.ACCOUNT_NO, fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACCOUNT_NO.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACCOUNT_NO.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.IFSC_CODE.getName()))
				|| !(crmValidator.validateField(CrmFieldType.IFSC_CODE, fields.get(FieldType.IFSC_CODE.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.IFSC_CODE.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.IFSC_CODE.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.BANK_NAME.getName()))
				|| !(crmValidator.validateField(CrmFieldType.BANK_NAME, fields.get(FieldType.BANK_NAME.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.BANK_NAME.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.BANK_NAME.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.ACCOUNT_HOLDER_NAME.getName())) || !(crmValidator
				.validateField(CrmFieldType.ACC_HOLDER_NAME, fields.get(FieldType.ACCOUNT_HOLDER_NAME.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_ACC_HOLDER_NAME.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_ACC_HOLDER_NAME.getResponseCode());
			return response;
		}
		response.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		return response;
	}

	public Map<String, String> validateUpdateStatusFields(Fields fields) {
		Map<String, String> response = new HashMap<String, String>();
		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))
				|| !(crmValidator.validateField(CrmFieldType.PAY_ID, fields.get(FieldType.PAY_ID.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			return response;
		}
		
		if (StringUtils.isBlank(fields.get(FieldType.CUST_ID.getName())) || !(crmValidator
				.validateField(CrmFieldType.CUST_ID, fields.get(FieldType.CUST_ID.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(),
					ErrorType.REQUIRED_CUST_ID.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_CUST_ID.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.STATUS.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_STATUS.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_STATUS.getResponseCode());
			return response;
		}
		response.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		return response;
	}

	public Map<String, String> validateUpdateUserFields(Fields fields) {
		Map<String, String> response = new HashMap<String, String>();
		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))
				|| !(crmValidator.validateField(CrmFieldType.PAY_ID, fields.get(FieldType.PAY_ID.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			return response;
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			Boolean isEmail = generalValidator.isValidEmailId(fields.get(FieldType.CUST_EMAIL.getName()));
			if (!isEmail) {
				response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_EMAILID.getResponseMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_EMAILID.getResponseCode());
				return response;
			}
		}
		if (StringUtils.isBlank(fields.get(FieldType.VIRTUAL_ACC_NUM.getName())) || !(crmValidator
				.validateField(CrmFieldType.VIRTUAL_ACCOUNT_NO, fields.get(FieldType.VIRTUAL_ACC_NUM.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(),
					ErrorType.REQUIRED_VIRTUAL_ACCOUNT_NUM.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_VIRTUAL_ACCOUNT_NUM.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.PHONE_NO.getName()))
				|| !(crmValidator.validateField(CrmFieldType.MOBILE, fields.get(FieldType.PHONE_NO.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PHONE.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PHONE.getResponseCode());
			return response;
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()))) {
			if (!(crmValidator.validateField(CrmFieldType.ACCOUNT_NO,
					fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName())))) {
				response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACCOUNT_NO.getResponseMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACCOUNT_NO.getResponseCode());
				return response;
			}
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.IFSC_CODE.getName()))) {
			if (!(crmValidator.validateField(CrmFieldType.IFSC_CODE, fields.get(FieldType.IFSC_CODE.getName())))) {
				response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.IFSC_CODE.getResponseMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.IFSC_CODE.getResponseCode());
				return response;
			}
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.BANK_NAME.getName()))) {
			if (!(crmValidator.validateField(CrmFieldType.BANK_NAME, fields.get(FieldType.BANK_NAME.getName())))) {
				response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.BANK_NAME.getResponseMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.BANK_NAME.getResponseCode());
				return response;
			}
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.ACCOUNT_HOLDER_NAME.getName()))) {
			if (!(crmValidator.validateField(CrmFieldType.ACC_HOLDER_NAME,
					fields.get(FieldType.ACCOUNT_HOLDER_NAME.getName())))) {
				response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACC_HOLDER_NAME.getResponseMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACC_HOLDER_NAME.getResponseCode());
				return response;
			}
		}
		response.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		return response;
	}

	public Map<String, String> validateUserTransactionFields(Fields fields) {
		Map<String, String> response = new HashMap<String, String>();
		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))
				|| !(crmValidator.validateField(CrmFieldType.PAY_ID, fields.get(FieldType.PAY_ID.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			return response;
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			Boolean isEmail = generalValidator.isValidEmailId(fields.get(FieldType.CUST_EMAIL.getName()));
			if (!isEmail) {
				response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_EMAILID.getResponseMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_EMAILID.getResponseCode());
				return response;
			}
		}
		if (StringUtils.isBlank(fields.get(FieldType.PHONE_NO.getName()))
				|| !(crmValidator.validateField(CrmFieldType.MOBILE, fields.get(FieldType.PHONE_NO.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PHONE.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PHONE.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.VIRTUAL_ACC_NUM.getName())) || !(crmValidator
				.validateField(CrmFieldType.VIRTUAL_ACCOUNT_NO, fields.get(FieldType.VIRTUAL_ACC_NUM.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(),
					ErrorType.REQUIRED_VIRTUAL_ACCOUNT_NUM.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_VIRTUAL_ACCOUNT_NUM.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.DATE_FROM.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_DATEFROM.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_DATEFROM.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.DATE_TO.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_DATETO.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_DATETO.getResponseCode());
			return response;
		}
		response.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		return response;
	}

	@SuppressWarnings("static-access")
	public Map<String, String> cashfreeAccountGeneration(Fields fields) {

		Map<String, String> cashFreeResponse = new HashMap<String, String>();
		try {
			String responseBody = "";
			String serviceUrl = propertiesManager.propertiesMap.get("VA_Generate_Request");
			try {

				JSONObject json = new JSONObject();
				List<String> fieldTypeList = new ArrayList<String>(fields.keySet());
				for (String fieldType : fieldTypeList) {
					json.put(fieldType, fields.get(fieldType));
				}
				CloseableHttpClient httpClient = HttpClientBuilder.create().build();
				HttpPost request = new HttpPost(serviceUrl);
				StringEntity params = new StringEntity(json.toString());
				request.addHeader("content-type", "application/json");
				request.setEntity(params);
				HttpResponse resp = httpClient.execute(request);
				responseBody = EntityUtils.toString(resp.getEntity());
				final ObjectMapper mapper = new ObjectMapper();
				final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
				cashFreeResponse = mapper.readValue(responseBody, type);
			} catch (Exception exception) {
				logger.error("exception is ", exception);
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
			}

		} catch (Exception e) {
			logger.info("Cashfree VA generation methof exception >> ", e);
		}

		return cashFreeResponse;
	}
	
	@SuppressWarnings("static-access")
	public Map<String, String> cashfreeStatusUpdateVA(Fields fields) {

		Map<String, String> cashFreeResponse = new HashMap<String, String>();
		try {
			String responseBody = "";
			String serviceUrl = propertiesManager.propertiesMap.get("TransactionWSCashfreeVAStatusUpdateURL");
			try {

				JSONObject json = new JSONObject();
				List<String> fieldTypeList = new ArrayList<String>(fields.keySet());
				for (String fieldType : fieldTypeList) {
					json.put(fieldType, fields.get(fieldType));
				}
				CloseableHttpClient httpClient = HttpClientBuilder.create().build();
				HttpPost request = new HttpPost(serviceUrl);
				StringEntity params = new StringEntity(json.toString());
				request.addHeader("content-type", "application/json");
				request.setEntity(params);
				HttpResponse resp = httpClient.execute(request);
				responseBody = EntityUtils.toString(resp.getEntity());
				final ObjectMapper mapper = new ObjectMapper();
				final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
				cashFreeResponse = mapper.readValue(responseBody, type);
			} catch (Exception exception) {
				logger.error("exception is ", exception);
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
			}

		} catch (Exception e) {
			logger.info("Cashfree VA generation methof exception >> ", e);
		}

		return cashFreeResponse;
	}
}
