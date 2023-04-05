package com.paymentgateway.pg.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

@Service
public class IciciEnachMandateEnquiryProcessor {

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	FieldsDao fieldsDao;

	private static Logger logger = LoggerFactory.getLogger(IciciEnachMandateEnquiryProcessor.class.getName());

	@SuppressWarnings({ "unused" })
	public Map<String, String> ENachRegistrationStausEnquiry(Fields fields) {
		String serverResponse = null;
		HashMap<String, String> updateDbMap = new HashMap<String, String>();
		HashMap<String, String> response = new HashMap<String, String>();
		Fields responseFields = new Fields(fields);
		
		if (fields.contains(FieldType.HASH.getName()) && StringUtils.isNotBlank(fields.get(FieldType.HASH.getName()))) {
			try {
				String amtWithDecimal = fields.get(FieldType.AMOUNT.getName());
				String amtWithOutDecimal = Amount.removeDecimalAmount(fields.get(FieldType.AMOUNT.getName()),
						CrmFieldConstants.INR.toString());
				
				String merchantHash = fields.get("HASH");
				fields.remove("HASH");
				String calculatedHash;
				try {
					calculatedHash = Hasher.getHash(fields);
				} catch (SystemException e1) {
					logger.error("Exception while generating Hash; " + e1);
					response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PAY_ID.getInternalMessage());
					response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PAY_ID.getCode());
					return response;
				}
				logger.info("Merchant HASH: "+ merchantHash + " | Calculated HASH: " + calculatedHash);
				
				if (merchantHash.equals(calculatedHash)) {
	
					fields.remove(FieldType.AMOUNT.getName());
					fields.put(FieldType.AMOUNT.getName(), amtWithDecimal);
	
					HashMap<String, String> registrationDetailMap = fieldsDao.getENachRegistrationByTxnId(fields);
	
					JSONObject merchantIdentifierMap = new JSONObject();
					JSONObject paymentInstructionMap = new JSONObject();
					JSONObject paymentInstruction = new JSONObject();
					JSONObject transactionIdentifierMap = new JSONObject();
					JSONObject consumerMap = new JSONObject();
					JSONObject consumerIdentifierMap = new JSONObject();
	
					JSONObject finalMap = new JSONObject();
	
					String merchantId = PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_ACQUIRER_ID.getValue());
					merchantIdentifierMap.put("identifier", merchantId);
					finalMap.put("merchant", merchantIdentifierMap);
	
					paymentInstructionMap.put("instruction", paymentInstruction);
					finalMap.put("payment", paymentInstructionMap);
	
					transactionIdentifierMap.put("deviceIdentifier", "S");
					transactionIdentifierMap.put("type", "002");
					transactionIdentifierMap.put("currency", PropertiesManager.propertiesMap.get("CURRENCY_356"));
					transactionIdentifierMap.put("identifier", registrationDetailMap.get(FieldType.PG_REF_NUM.getName()));
					transactionIdentifierMap.put("dateTime", registrationDetailMap.get("START_DATE"));
					transactionIdentifierMap.put("subType", "002");
					transactionIdentifierMap.put("requestType", "TSI");
	
					finalMap.put("transaction", transactionIdentifierMap);
	
					consumerIdentifierMap.put("identifier", registrationDetailMap.get(FieldType.ORDER_ID.getName()));
					finalMap.put("consumer", consumerIdentifierMap);
	
					/*
					 * { "merchant": { "identifier": "T3239" //Provided by ingenico }, "payment": {
					 * "instruction": {} }, "transaction": { "deviceIdentifier": "S", "type": "002",
					 * "currency": "INR", "identifier": "1385401123084035", //unique ID generated by
					 * the merchant and sent during registration (PG_REF_NUM) "dateTime":
					 * "23-11-2020", "subType": "002", "requestType": "TSI" }, "consumer": {
					 * "identifier": "c964634" //consumer ID that is passed in the initial request
					 * (ORDER_ID/ TXN_ID) } }
					 */
	
					// HttpsURLConnection connection = null;
					String serviceUrl = PropertiesManager.propertiesMap
							.get(Constants.ICICI_ENACH_STATUS_ENQUIRY_URL.getValue());
	
					URL url = new URL(serviceUrl);
					int timeout = 20000;
					HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
					connection.setConnectTimeout(timeout);
					connection.setRequestMethod("POST");
					connection.setDoOutput(true);
					connection.setRequestProperty("Content-Type", "application/json");
					connection.setRequestProperty("Accept", "application/json");
	
					OutputStream os = connection.getOutputStream();
					os.write(finalMap.toString().getBytes());
					os.flush();
	
					logger.info("ICICI ENach Communicator Request : " + finalMap.toString());
	
					if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
						throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
					}
	
					BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
	
					connection.disconnect();
					serverResponse = sb.toString();
	
					String slitResponse[] = serverResponse.split("\\{");
					String finalResponseArr[] = slitResponse[3].split("\\}");
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append("{");
					stringBuilder.append(finalResponseArr[0]);
					stringBuilder.append("}");
					JSONObject finalJsonObj = new JSONObject(stringBuilder.toString());
	
					updateDbMap.put(FieldType.RESPONSE_CODE.getName(), finalJsonObj.get("statusCode").toString());
					updateDbMap.put(FieldType.RESPONSE_MESSAGE.getName(), finalJsonObj.get("statusMessage").toString());
					if (finalJsonObj.get("statusCode").equals("0300")) {
						updateDbMap.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
					} else if (finalJsonObj.get("statusCode").equals("0398")) {
						updateDbMap.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
					} else {
						updateDbMap.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					}
	
					logger.info("ICICI ENach Communicator Response : " + serverResponse);
	
					fieldsDao.updateENachRegistrationDetailByTxnId(updateDbMap, fields);
					updateDbMap.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
					updateDbMap.put(FieldType.AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
					String responseHash = Hasher.getHash(new Fields(updateDbMap));
					logger.info("status enquiry response hash " + responseHash);
					updateDbMap.put(FieldType.HASH.getName(), responseHash);
				} else {
					Map<String, String> aaData = new HashMap<String, String>();
					aaData.put(FieldType.RESPONSE.getName(), Constants.TRANSACTIONSTATE_N.getValue());
					aaData.put(FieldType.RETURN_URL.getName(), fields.get(FieldType.RETURN_URL.getName()));
					logger.info("Invalid merchant hash");
					return aaData;
				}
			} catch (Exception exception) {
				logger.error("Error communicating with ICICI Enach API, ", exception);
			}
		} else {
			List<String> fieldTypeList = new ArrayList<String>(responseFields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				response.put(fieldType, responseFields.get(fieldType));
			}
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.NO_HASH.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_HASH.getCode());
			return response;
		}
		return updateDbMap;
	}

	public boolean validateHash(String merchantHash, String calculatedHash) {

		try {
			logger.info("inside forwardAction, validateHash()");

			if (StringUtils.isNotBlank(merchantHash)) {
				logger.info("Merchant Hash == " + merchantHash);
				logger.info("Calculated Hash == " + calculatedHash);
				if (calculatedHash.equalsIgnoreCase(merchantHash)) {
					return true;
				} else {
					logger.info("Merchant Hash and Calculated Hash do not match");
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Unable to validate Hash for eMandate status enquiry : ", e);
			return false;
		}
	}

	@SuppressWarnings({ "unused" })
	public Map<String, String> ENachRegistrationDeactivate(Fields fields) {
		String serverResponse = null;
		HashMap<String, String> updateDbMap = new HashMap<String, String>();
		try {
			String amtWithDecimal = fields.get(FieldType.AMOUNT.getName());
			String amtWithOutDecimal = Amount.removeDecimalAmount(fields.get(FieldType.AMOUNT.getName()),
					CrmFieldConstants.INR.toString());
			fields.remove(FieldType.AMOUNT.getName());
			fields.put(FieldType.AMOUNT.getName(), amtWithOutDecimal);
			String hash = fields.get("HASH");
			fields.remove("HASH");
			String hashCalculate = Hasher.getHash(fields);
			if (validateHash(hash, hashCalculate)) {

				fields.remove(FieldType.AMOUNT.getName());
				fields.put(FieldType.AMOUNT.getName(), amtWithDecimal);

				HashMap<String, String> registrationDetailMap = fieldsDao.getENachRegistrationByTxnId(fields);

				JSONObject merchantIdentifierMap = new JSONObject();
				JSONObject paymentInstruction = new JSONObject();
				JSONObject transactionIdentifierMap = new JSONObject();
				JSONObject consumerIdentifierMap = new JSONObject();
				JSONObject finalMap = new JSONObject();
				JSONObject finalInstrumentMap = new JSONObject();
				JSONObject finalPaymentMap = new JSONObject();

				String merchantId = PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_ACQUIRER_ID.getValue());

				merchantIdentifierMap.put("webhookEndpointURL", "");
				merchantIdentifierMap.put("responseType", "");
				merchantIdentifierMap.put("responseEndpointURL", "");
				merchantIdentifierMap.put("description", "");
				merchantIdentifierMap.put("identifier", merchantId);
				merchantIdentifierMap.put("webhookType", "");
				finalPaymentMap.put("merchant", merchantIdentifierMap);

				JSONObject itemMap = new JSONObject();

				itemMap.put("description", "");
				itemMap.put("providerIdentifier", "");
				itemMap.put("surchargeOrDiscountAmount", "");
				itemMap.put("amount", "");
				itemMap.put("comAmt", "");
				itemMap.put("sKU", "");
				itemMap.put("reference", "");
				itemMap.put("identifier", "");

				List<JSONObject> itemList = new ArrayList<JSONObject>();
				itemList.add(itemMap);
				JSONObject cartMap = new JSONObject();
				cartMap.put("item", itemList);
				cartMap.put("reference", "");
				cartMap.put("identifier", "");
				cartMap.put("description", "");
				cartMap.put("Amount", "");
				finalPaymentMap.put("cart", cartMap);

				JSONObject paymentMap = new JSONObject();
				JSONObject methodMapIdentifier = new JSONObject();
				methodMapIdentifier.put("token", "");
				methodMapIdentifier.put("type", "");
				paymentMap.put("method", methodMapIdentifier);

				JSONObject instrumentMapIdentifier = new JSONObject();

				JSONObject expiryMapIdentifier = new JSONObject();
				expiryMapIdentifier.put("year", "");
				expiryMapIdentifier.put("month", "");
				expiryMapIdentifier.put("dateTime", "");
				instrumentMapIdentifier.put("expiry", expiryMapIdentifier);
				instrumentMapIdentifier.put("provider", "");
				instrumentMapIdentifier.put("iFSC", "");

				JSONObject holderMap = new JSONObject();

				holderMap.put("name", "");
				JSONObject addressMapIdentifier = new JSONObject();

				addressMapIdentifier.put("country", "");
				addressMapIdentifier.put("street", "");
				addressMapIdentifier.put("state", "");
				addressMapIdentifier.put("city", "");
				addressMapIdentifier.put("zipCode", "");
				addressMapIdentifier.put("county", "");

				holderMap.put("address", addressMapIdentifier);

				instrumentMapIdentifier.put("holder", holderMap);

				instrumentMapIdentifier.put("bIC", "");
				instrumentMapIdentifier.put("type", "");
				instrumentMapIdentifier.put("action", "");
				instrumentMapIdentifier.put("mICR", "");
				instrumentMapIdentifier.put("verificationCode", "");
				instrumentMapIdentifier.put("iBAN", "");
				instrumentMapIdentifier.put("processor", "");

				JSONObject issuanceMap = new JSONObject();
				issuanceMap.put("year", "");
				issuanceMap.put("month", "");
				issuanceMap.put("dateTime", "");

				instrumentMapIdentifier.put("issuance", issuanceMap);

				instrumentMapIdentifier.put("alias", "");
				instrumentMapIdentifier.put("identifier", "");
				instrumentMapIdentifier.put("token", "");

				JSONObject authenticationMap = new JSONObject();

				authenticationMap.put("token", "");
				authenticationMap.put("type", "");
				authenticationMap.put("subType", "");

				instrumentMapIdentifier.put("authentication", authenticationMap);

				instrumentMapIdentifier.put("subType", "");
				instrumentMapIdentifier.put("issuer", "");
				instrumentMapIdentifier.put("acquirer", "");

				paymentMap.put("instrument", instrumentMapIdentifier);

				JSONObject instructionMap = new JSONObject();
				instructionMap.put("occurrence", "");
				instructionMap.put("amount", "");
				instructionMap.put("frequency", "");
				instructionMap.put("type", "");
				instructionMap.put("description", "");
				instructionMap.put("action", "");
				instructionMap.put("limit", "");
				instructionMap.put("endDateTime", "");
				instructionMap.put("reference", "");
				instructionMap.put("startDateTime", "");
				instructionMap.put("validity", "");

				paymentMap.put("instruction", instructionMap);
				finalPaymentMap.put("payment", paymentMap);
				JSONObject transactionMap = new JSONObject();

				transactionMap.put("deviceIdentifier", "S");
				transactionMap.put("smsSending", "");
				transactionMap.put("amount", "");
				transactionMap.put("forced3DSCall ", "");
				transactionMap.put("type", "001");
				transactionMap.put("description", "");
				transactionMap.put("currency", "INR");
				transactionMap.put("isRegistration", "");
				transactionMap.put("identifier", "");
				transactionMap.put("dateTime", "");
				transactionMap.put("token", "");
				transactionMap.put("securityToken", "");
				transactionMap.put("subType", "005");
				transactionMap.put("requestType", "TSI");
				transactionMap.put("reference", "");
				transactionMap.put("merchantInitiated", "");
				transactionMap.put("merchantRefNo", "");

				finalPaymentMap.put("transaction", transactionMap);

				JSONObject consumerMap = new JSONObject();

				consumerMap.put("mobileNumber", "");
				consumerMap.put("emailID", "");
				consumerMap.put("identifier", "");
				consumerMap.put("accountNo", "");

				finalPaymentMap.put("consumer", consumerMap);

				/*
				 * { "merchant": { "identifier": "T3239" }, "payment": { "instruction": {} },
				 * "transaction": { "deviceIdentifier": "S", "type": "002", "currency": "INR",
				 * "identifier": "1385401123084035", "dateTime": "23-11-2020", "subType": "002",
				 * "requestType": "TSI" }, "consumer": { "identifier": "c964634" } }
				 */

				String serviceUrl = PropertiesManager.propertiesMap
						.get(Constants.ICICI_ENACH_STATUS_ENQUIRY_URL.getValue());

				URL url = new URL(serviceUrl);
				int timeout = 20000;
				HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
				connection.setConnectTimeout(timeout);
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Accept", "application/json");

				OutputStream os = connection.getOutputStream();
				os.write(finalPaymentMap.toString().getBytes());
				os.flush();

				logger.info("ICICI ENach Communicator Request : " + finalMap.toString());

				if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
					throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
				}

				BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

				connection.disconnect();
				serverResponse = sb.toString();

				String slitResponse[] = serverResponse.split("\\{");
				String finalResponseArr[] = slitResponse[3].split("\\}");
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("{");
				stringBuilder.append(finalResponseArr[0]);
				stringBuilder.append("}");
				JSONObject finalJsonObj = new JSONObject(stringBuilder.toString());

				// updateDbMap.put("response", finalJsonObj.get("errorMessage").toString());
				updateDbMap.put(FieldType.RESPONSE_CODE.getName(), finalJsonObj.get("statusCode").toString());
				updateDbMap.put(FieldType.RESPONSE_MESSAGE.getName(), finalJsonObj.get("statusMessage").toString());
				if (finalJsonObj.get("statusCode").equals("0300")) {
					updateDbMap.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
				} else if (finalJsonObj.get("statusCode").equals("0398")) {
					updateDbMap.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
				} else {
					updateDbMap.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				}

				logger.info("ICICI ENach Communicator Response for registratoin deactivation : " + serverResponse);

				// fieldsDao.updateENachRegistrationDetailByTxnId(updateDbMap, fields);
				updateDbMap.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				updateDbMap.put(FieldType.AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				String responseHash = Hasher.getHash(new Fields(updateDbMap));
				logger.info("status enquiry response hash " + responseHash);
				updateDbMap.put(FieldType.HASH.getName(), responseHash);
			} else {
				Map<String, String> aaData = new HashMap<String, String>();
				aaData.put(FieldType.RESPONSE.getName(), Constants.TRANSACTIONSTATE_N.getValue());
				aaData.put(FieldType.RETURN_URL.getName(), fields.get(FieldType.RETURN_URL.getName()));
				logger.info("Invalid merchant hash");
				return aaData;
			}
		} catch (Exception exception) {
			logger.error("Error communicating with ICICI Enach API For Deactivation of Registration : ", exception);
		}
		return updateDbMap;
	}

}
