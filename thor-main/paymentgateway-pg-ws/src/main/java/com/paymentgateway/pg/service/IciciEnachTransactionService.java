package com.paymentgateway.pg.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.owasp.esapi.reference.validation.DateValidationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.ENachDao;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.SystemProperties;

/**
 * @author Rajit
 */

@Service
public class IciciEnachTransactionService {

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	FieldsDao fieldsDao;

	@Autowired
	ENachDao enachDao;

	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(IciciEnachTransactionService.class.getName());

	// For Transaction Schedule
	public Map<String, String> eNachTransactionSchedule(Fields fields) {
		try {
			logger.info("inside IciciEnachTransactionService for transaction schedule ");

			Map<String, String> response = new HashMap<String, String>();
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date Registrationdate = null;
			Date currentDate = null;
			Registrationdate = format.parse(fields.get(FieldType.REGISTRATION_DATE.getName()));
			currentDate = format.parse(dateNow);
			long diff = currentDate.getTime() - Registrationdate.getTime();
			long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

			if (30 < minutes) {

				LocalDate debitScheduleDate = LocalDate.now().plusDays(2);
				String[] splitDate = debitScheduleDate.toString().split("-");
				StringBuilder date = new StringBuilder();
				date.append(splitDate[2]);
				date.append(splitDate[1]);
				date.append(splitDate[0]);
				fields.put(FieldType.DEBIT_DATE.getName(), debitScheduleDate.toString());
				fields.put(FieldType.CREATE_DATE.getName(), dateNow);

				LinkedHashMap<String, String> merchantIdentifierMap = new LinkedHashMap<String, String>();
				LinkedHashMap<String, LinkedHashMap<String, String>> paymentInstructionMap = new LinkedHashMap<String, LinkedHashMap<String, String>>();
				LinkedHashMap<String, String> paymentInstruction = new LinkedHashMap<String, String>();
				LinkedHashMap<String, String> paymentInstrument = new LinkedHashMap<String, String>();
				LinkedHashMap<String, String> transactionIdentifierMap = new LinkedHashMap<String, String>();
				LinkedHashMap<String, Object> finalMap = new LinkedHashMap<String, Object>();

				String merchantId = PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_ACQUIRER_ID.getValue());
				merchantIdentifierMap.put("identifier", merchantId);
				finalMap.put("merchant", merchantIdentifierMap);

				paymentInstrument.put("identifier",
						PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_ITEM_ID.getValue()));
				paymentInstructionMap.put("instrument", paymentInstrument);

				paymentInstruction.put("amount",
						/* Amount.removeDecimalAmount( */fields.get(FieldType.MONTHLY_AMOUNT.getName())/* , "365") */);
				paymentInstruction.put("endDateTime", date.toString()); // when
																		// the
																		// txn
																		// debitted
																		// DD-MM-YYYY
																		// Formate.
				paymentInstruction.put("identifier", fields.get("MANDATE_REGISTRATION_ID"));// This
																							// is
																							// the
																							// unique
				// mandate registration
				// id sent by Paynimo
				// system in successful
				// mandate registration
				// request.

				paymentInstructionMap.put("instruction", paymentInstruction);

				finalMap.put("payment", paymentInstructionMap);

				transactionIdentifierMap.put("deviceIdentifier", "S");
				transactionIdentifierMap.put("type", "001");
				transactionIdentifierMap.put("currency", PropertiesManager.propertiesMap.get("CURRENCY_356"));
				transactionIdentifierMap.put("identifier", fields.get(FieldType.PG_REF_NUM.getName()));
				transactionIdentifierMap.put("subType", "003");
				transactionIdentifierMap.put("requestType", "TSI");
				finalMap.put("transaction", transactionIdentifierMap);

				String requestParam = finalMap.toString().replace("{", "{\"").replace("=", "\":").replace(", ", ", \"")
						.replace("\":", "\":\"").replace("}", "\"}").replace("}\"}", "}}").replace(":\"{", ":{")
						.replace(", \"", "\", \"").replace("}\",", "},");
				/*
				 * { "merchant": { "identifier": "T3239" }, "payment": { "instrument": {
				 * "identifier": "test" }, "instruction": { "amount": "10", "endDateTime":
				 * "17012018", "identifier": "752410399" } }, "transaction": {
				 * "deviceIdentifier": "S", "type": "001", "currency": "INR", "identifier":
				 * "2441510402153131", "subType": "003", "requestType": "TSI" } }
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

				OutputStream os = connection.getOutputStream();
				os.write(requestParam.toString().getBytes());
				os.flush();
				logger.info("ICICI ENach Communicator Request For Debit transaction : " + finalMap.toString());

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
				String serverResponse = sb.toString();

				String slitResponse[] = serverResponse.split("\\{");
				String finalResponseArr[] = slitResponse[3].split("\\}");
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("{");
				stringBuilder.append(finalResponseArr[0]);
				stringBuilder.append("}");
				JSONObject finalJsonObj = new JSONObject(stringBuilder.toString());

				Map<String, String> newEntry = new HashMap<String, String>();
				if (finalJsonObj.get("errorMessage").toString().equalsIgnoreCase("null")) {
					newEntry.put("response", finalJsonObj.get("statusMessage").toString());
				} else {
					newEntry.put("response", finalJsonObj.get("errorMessage").toString());
				}
				newEntry.put("responseCode", finalJsonObj.get("statusCode").toString());
				newEntry.put("responseMessage", finalJsonObj.get("statusMessage").toString());
				newEntry.put("pgRefNum", fields.get(FieldType.PG_REF_NUM.getName()));
				newEntry.put("debitDate", date.toString());

				response.put(FieldType.RESPONSE_CODE.getName(), newEntry.get("responseCode").toString());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), newEntry.get("response").toString());

				if (finalJsonObj.get("errorMessage").toString().equalsIgnoreCase("null")) {
					newEntry.put("response", finalJsonObj.get("statusMessage").toString());
				} else {
					newEntry.put("response", finalJsonObj.get("errorMessage").toString());
				}
				logger.info("ICICI ENach Communicator Response For Debit Transaction : " + serverResponse);

				// insert debit transaction details in DB.
				fieldsDao.insertDebitTransactionDetail(newEntry, fields);

			} else {
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DECLINED.getCode());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), CrmFieldConstants.PAY_NOW_VALIDATION.getValue());
			}
			return response;
		} catch (Exception exception) {
			logger.error("Error communicating with ICICI ENach Debit Transaction API, ", exception);
			return null;
		}
	}

	// For debitted Transaction Status Enquiry
	public Map<String, String> eNachTransactionStausEnquiry(Fields fields) {

		try {
			logger.info("inside IciciEnachTransactionService for get enach transaction status enquiry ");
			// String txnId = fields.get(FieldType.TXN_ID.getName());
			HashMap<String, String> transactionDetailMap = fieldsDao.getENachTransactionDetailsByOrderId(fields);

			StringBuilder date = new StringBuilder();
			String dateArr[] = transactionDetailMap.get(FieldType.DEBIT_DATE.getName()).split("-");
			date.append(dateArr[2]).append("-").append(dateArr[1]).append("-").append(dateArr[0]);

			LinkedHashMap<String, String> merchantIdentifierMap = new LinkedHashMap<String, String>();
			LinkedHashMap<String, LinkedHashMap<String, String>> paymentInstructionMap = new LinkedHashMap<String, LinkedHashMap<String, String>>();
			LinkedHashMap<String, String> paymentInstruction = new LinkedHashMap<String, String>();
			LinkedHashMap<String, String> transactionIdentifierMap = new LinkedHashMap<String, String>();
			LinkedHashMap<String, Object> finalMap = new LinkedHashMap<String, Object>();

			String merchantId = PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_ACQUIRER_ID.getValue());
			merchantIdentifierMap.put("identifier", merchantId);
			finalMap.put("merchant", merchantIdentifierMap);

			paymentInstructionMap.put("instruction", paymentInstruction);
			finalMap.put("payment", paymentInstructionMap);

			transactionIdentifierMap.put("deviceIdentifier", "S");
			transactionIdentifierMap.put("type", "001");
			transactionIdentifierMap.put("currency", PropertiesManager.propertiesMap.get("CURRENCY_356"));
			transactionIdentifierMap.put("identifier", transactionDetailMap.get(FieldType.PG_REF_NUM.getName()));
			transactionIdentifierMap.put("dateTime", date.toString());
			transactionIdentifierMap.put("subType", "004");
			transactionIdentifierMap.put("requestType", "TSI");

			finalMap.put("transaction", transactionIdentifierMap);

			String requestParam = finalMap.toString().replace("{", "{\"").replace("=", "\":").replace(", ", ", \"")
					.replace("\":", "\":\"").replace("}", "\"}").replace("}\"}", "}}").replace(":\"{", ":{")
					.replace("{\"\"}", "{}").replace(", \"", "\", \"").replace("}\",", "},");

			/*   //Demo
			 * 	{ 
			 * 		"merchant": { 
			 * 			"identifier": "T3239" 
			 * 		},
			 * 		"payment": {
			 * 			"instruction": {
			 * 			 }
			 * 		 },
			 * 		"transaction": { 
			 * 			"deviceIdentifier": "S",
			 * 			"type": "001", 
			 * 			"currency": "INR",
			 * 			"identifier": "1516163889656", //Unique Id PgRefNum 
			 * 			"dateTime": "17-01-2018", //due Date 
			 * 			"subType": "004", 
			 * 			"requestType": "TSI" 
			 * 		} 
			 * }
			 */

			String serviceUrl = PropertiesManager.propertiesMap
					.get(Constants.ICICI_ENACH_SALE_TRANSACTION_STATUS_ENQUIRY_URL.getValue());

			URL url = new URL(serviceUrl);
			int timeout = 20000;
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setConnectTimeout(timeout);
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");

			OutputStream os = connection.getOutputStream();
			os.write(requestParam.toString().getBytes());
			os.flush();

			logger.info(
					"ICICI ENach Communicator Request for debit transaction status enquiry : " + finalMap.toString());

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
			String serverResponse = sb.toString();

			String slitResponse[] = serverResponse.split("\\{");
			String finalResponseArr[] = slitResponse[3].split("\\}");
			String finalResponseMessageArr[] = slitResponse[4].split("\\}");
			StringBuilder stringBuilder = new StringBuilder();
			StringBuilder stringBuilder1 = new StringBuilder();
			stringBuilder.append("{");
			stringBuilder.append(finalResponseArr[0]);
			stringBuilder.append("}");

			stringBuilder1.append("{");
			stringBuilder1.append(finalResponseMessageArr[0]);
			stringBuilder1.append("}");

			JSONObject finalJsonObj = new JSONObject(stringBuilder.toString());
			JSONObject finalJsonObj1 = new JSONObject(stringBuilder1.toString());

			HashMap<String, String> updateDbMap = new HashMap<String, String>();
			updateDbMap.put("response", finalJsonObj1.get("desc").toString());
			updateDbMap.put("responseCode", finalJsonObj.get("statusCode").toString());
			updateDbMap.put("responseMessage", finalJsonObj.get("statusMessage").toString());

			logger.info("ICICI ENach Communicator Response for debit transaction status enquiry : " + serverResponse);

			if (updateDbMap.get("responseCode").equalsIgnoreCase("0300")
					&& updateDbMap.get("responseMessage").equalsIgnoreCase("S")
					|| updateDbMap.get("responseCode").equalsIgnoreCase("0399")) {
				fieldsDao.updateENachTransactionDetailByStatusEnquiry(updateDbMap, transactionDetailMap);

				if (updateDbMap.get("response").equalsIgnoreCase("Transaction Verification Success")) {
					updateDbMap.put("response", "Transaction Success");
				}
				return updateDbMap;
			} else {
				if (updateDbMap.get("response").equalsIgnoreCase("Transaction Verification Success")) {
					updateDbMap.put("response", "Transaction is Processing");
				}
				return updateDbMap;
			}

		} catch (Exception ex) {
			logger.error("Error communicating with ICICI Enach debit transaction status enquiry API, ", ex);
			return null;
		}
	}

	public Map<String, Map<String, String>> merchantENachTransactionStausEnquiry_OldCode(Fields fields) {

		logger.info("inside merchantENachTransactionStausEnquiry ");
		Map<String, Map<String, String>> aaData = new HashMap<String, Map<String, String>>();
		try {

			String merchantPayId = null;
			String subMerchantPayId = null;
			String merchantHash = fields.get("HASH");
			fields.remove("HASH");
			String calculatedHash = Hasher.getHash(fields);
			logger.info("calculated hash: " + calculatedHash);
			if (merchantHash.equals(calculatedHash)) {

				logger.info("merchant hash matched");
				User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

				// for Sub Merchant
				if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {

					merchantPayId = user.getSuperMerchantId();
					subMerchantPayId = fields.get(FieldType.PAY_ID.getName());
				} else {
					// for merhcant and super merchant
					merchantPayId = fields.get(FieldType.PAY_ID.getName());
					subMerchantPayId = "";
				}
//				aaData = fieldsDao.getEnachDebitDetailsForStatusEnquiry(merchantPayId, subMerchantPayId,
//						fields.get(FieldType.ORDER_ID.getName()), fields.get(FieldType.DEBIT_DATE.getName()));

			} else {

				logger.info("merchant hash not match");
				StringBuilder hashMessage = new StringBuilder("Merchant hash = ");
				hashMessage.append(merchantHash);
				hashMessage.append(", Calculated Hash = ");
				hashMessage.append(calculatedHash);
				MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
				logger.error(hashMessage.toString());

				Map<String, String> authMap = new HashMap<String, String>();
				authMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getCode());
				authMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Hash");
				aaData.put(ErrorType.AUTHENTICATION_FAILED.getResponseMessage(), authMap);

			}
			return aaData;
		} catch (Exception ex) {
			logger.error("Error communicating with ICICI Enach debit transaction status enquiry API, ", ex);
			return null;
		}
	}

	public Map<String,String> merchantENachTransactionStausEnquiry(Fields fields) {

		logger.info("inside merchantENachTransactionStausEnquiry ");
		HashMap<String, String> response = new HashMap<String, String>();
		Fields newFields = new Fields(fields);
		Fields responseFields = new Fields();

		HashMap<String, String> responseMap = new HashMap<String, String>();

		String merchantPayId = null;
		String subMerchantPayId = null;
		
		if (fields.contains(FieldType.HASH.getName()) 
				&& StringUtils.isNotBlank(fields.get(FieldType.HASH.getName()))) {
			List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				responseMap.put(fieldType, newFields.get(fieldType));
			}

			String merchantHash = fields.get(FieldType.HASH.getName());
			fields.remove(FieldType.HASH.getName());
			String calculatedHash;
			if (!fields.contains(FieldType.PAY_ID.getName())) {
				logger.error("PAY_ID not available ");
				response.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PAY_ID.getInternalMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PAY_ID.getCode());
				return response;
			}
			try {
				calculatedHash = Hasher.getHash(fields);
			} catch (SystemException e1) {
				logger.error("Exception while generating Hash; " + e1);
				response.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PAY_ID.getInternalMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PAY_ID.getCode());
				return response;
			}
			logger.info("Merchant HASH: " + merchantHash + " | Calculated HASH: " + calculatedHash);
			if (merchantHash.equals(calculatedHash)) {
				if (fields.contains(FieldType.ORDER_ID.getName())) {
					if (StringUtils.isNotBlank(fields.get(FieldType.ORDER_ID.getName()))
							&& StringUtils.isAlphanumeric(fields.get(FieldType.ORDER_ID.getName()))) {

						User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
						
						// for Sub Merchant
						if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
			
							merchantPayId = user.getSuperMerchantId();
							subMerchantPayId = fields.get(FieldType.PAY_ID.getName());
						} else {
							// for merhcant and super merchant
							merchantPayId = fields.get(FieldType.PAY_ID.getName());
							subMerchantPayId = "";
						}
						responseMap = enachDao.getEnachTransactionDetailsForStatusEnquiry(merchantPayId, subMerchantPayId,
								fields.get(FieldType.ORDER_ID.getName()), fields.get(FieldType.PG_REF_NUM.getName()));
						
						responseMap.remove(FieldType.HASH.getName());
						
						try {
							responseFields.putAll((responseMap));
							responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
							responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getInternalMessage());							
							responseMap.put(FieldType.HASH.getName(), Hasher.getHash(responseFields));
						} catch (SystemException e) {
							responseMap = new HashMap<String, String>();
							responseMap.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
							responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), "HASH ERROR");
							e.printStackTrace();
							return responseMap;				
						}
					
					} else {
						logger.info("Valid ORDER_ID Required");
						responseMap.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
						responseMap.put(FieldType.RESPONSE_MESSAGE.getName(),
								ErrorType.INVALID_ORDER_ID.getResponseMessage());
						responseMap.put(FieldType.RESPONSE_CODE.getName(),
								ErrorType.INVALID_ORDER_ID.getResponseCode());
						return responseMap;
					}
				} else {
					logger.info("ORDER_ID Required");
					responseMap.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.NO_ORDER_ID.getResponseMessage());
					responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_ORDER_ID.getResponseCode());
					return responseMap;
				}
				
			} else {
				responseMap.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_HASH.getInternalMessage());
				responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
			}
		} else {
			List<String> fieldTypeList = new ArrayList<String>(newFields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				response.put(fieldType, newFields.get(fieldType));
			}
			responseMap.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.NO_HASH.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_HASH.getCode());
			return response;
		}
		return responseMap;

	}



}
