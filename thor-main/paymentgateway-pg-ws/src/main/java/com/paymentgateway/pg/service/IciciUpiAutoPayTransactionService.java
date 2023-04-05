package com.paymentgateway.pg.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.SmsControllerServiceProvider;
import com.paymentgateway.commons.dao.UpiAutoPayDao;
import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.BitlyUrlShortener;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;
import com.paymentgateway.pg.core.util.IciciUpiAutoPayUtil;

@Service
public class IciciUpiAutoPayTransactionService {

	@Autowired
	private UpiAutoPayDao upiAutoPayDao;

	@Autowired
	private IciciUpiAutoPayUtil iciciUpiAutoPayUtil;

	@Autowired
	private GeneralValidator generalValidator;

	@Autowired
	private EmailServiceProvider emailServiceProvider;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private BitlyUrlShortener bitlyUrlShortener;

	@Autowired
	private SmsControllerServiceProvider smsControllerServiceProvider;

	private static Logger logger = LoggerFactory.getLogger(IciciUpiAutoPayTransactionService.class.getName());

	// Notification Debit Transaction
	@SuppressWarnings("deprecation")
	public Map<String, String> notificationDebitTransaction(Fields fields) {

		logger.info("inside debitTransactionNotification for notification");
		String decryptedResponse = null;
		HttpURLConnection con = null;
		Map<String, String> res = new HashMap<String, String>();
		SimpleDateFormat ddmmyyyy = new SimpleDateFormat(CrmFieldConstants.INPUT_DATE_FOR_MIS_REPORT.getValue());
		try {
			String txnType = TransactionType.SALE.getName();
			String status = StatusType.PENDING.getName();
			Map<String, String> pendingDebitTransaction = upiAutoPayDao
					.getTransactionByPgRefNum(fields.get(FieldType.PG_REF_NUM.getName()), txnType, status);

			if (pendingDebitTransaction.size() != 0) {

				LinkedHashMap<String, String> requestMap = new LinkedHashMap<String, String>();

				requestMap.put("merchantId", PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MID.getValue()));
				requestMap.put("subMerchantId",
						PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MID.getValue()));
				requestMap.put("terminalId",
						PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_TERMINAL_ID.getValue()));
				requestMap.put("merchantName",
						PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MERCHANT_NAME.getValue()));
				requestMap.put("subMerchantName",
						PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_SUB_MERCHANT_NAME.getValue()));
				requestMap.put("payerVA", pendingDebitTransaction.get(FieldType.PAYER_ADDRESS.getName()));
				requestMap.put("amount", pendingDebitTransaction.get(FieldType.AMOUNT.getName()));
				requestMap.put("note", "Debit Request");
				requestMap.put("executionDate",
						ddmmyyyy.format(new Date(
								pendingDebitTransaction.get(FieldType.DUE_DATE.getName()).replace("-", "/").toString()))
								+ " 10:00 AM");
				requestMap.put("merchantTranId", pendingDebitTransaction.get(FieldType.PG_REF_NUM.getName()));
				requestMap.put("mandateSeqNo", pendingDebitTransaction.get(FieldType.SEQUENCE_NO.getName()));
				requestMap.put("key", FieldType.UMN.getName());
				requestMap.put("value", pendingDebitTransaction.get(FieldType.UMN.getName()));

				String requestParam = requestMap.toString().replace("{", "{\"").replace("=", "\":")
						.replace(", ", ", \"").replace("\":", "\":\"").replace("}", "\"}").replace(", \"", "\", \"");

				logger.info("Upi AutoPay debit transaction notification Request " + requestParam);
				String serviceUrl = PropertiesManager.propertiesMap
						.get(Constants.UPI_AUTOPAY_NOTIFICATION_URL.getValue());

				URL requestUrl = new URL(serviceUrl);
				con = (HttpURLConnection) requestUrl.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Type", "application/json");
				con.setRequestProperty("apikey",
						PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_API_KEY.getValue()));

				// Encrypt The Request
				String encryptedRequest = iciciUpiAutoPayUtil.upiAutoPayEncryption(requestParam);
				logger.info("Encrypted Request debit transaction notification is " + encryptedRequest);
				con.setDoOutput(true);
				con.setUseCaches(false);
				OutputStream os = con.getOutputStream();
				os.write(encryptedRequest.getBytes());
				os.flush();
				os.close();

				int responseCode = con.getResponseCode();
				logger.info("Upi AutoPay debit transaction notification Response Code " + responseCode);

				if (responseCode == HttpURLConnection.HTTP_OK) {
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					StringBuffer response = new StringBuffer();

					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					logger.info("Encrypted Response recived debit transaction notification is " + response.toString());
					decryptedResponse = iciciUpiAutoPayUtil.upiAutoPayDecryption(response.toString());
					logger.info("Decrypted Response of ICICI Upi AutoPay debit transaction notification is "
							+ decryptedResponse);

					JSONParser parser = new JSONParser();
					JSONObject responseJson = (JSONObject) parser.parse(decryptedResponse);

					/*
					 * { "response" : "0", "merchantId" : "106161", "subMerchantId" : "12234",
					 * "terminalId" : "5411", "BankRRN" : "615519221396", "merchantTranId" :
					 * "612411454593", "amount" : "12", "success" : "true", "message" :
					 * "Transaction Successful" }
					 */

					if (responseJson.get("response").toString().equalsIgnoreCase("0")
							&& responseJson.get("success").toString().equalsIgnoreCase("true")) {
						res.put(FieldType.STATUS.getName(), StatusType.NOTIFIED.getName());
						res.put(FieldType.RESPONSE_MESSAGE.getName(), responseJson.get("message").toString());
					} else {
						res.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
						res.put(FieldType.RESPONSE_MESSAGE.getName(), responseJson.get("message").toString());
					}
					upiAutoPayDao.updateByNotificationResponse(responseJson, pendingDebitTransaction);
					return res;
				} else {
					logger.info("Error while connecting Response Code is " + con.getResponseCode());
					res.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					res.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.NOTIFICATION_FAILED.getResponseMessage());
					// return null;
				}

			} else {
				res.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				res.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TRANSACTION_NOT_FOUND.getResponseMessage());
			}
		} catch (Exception ex) {
			logger.info("Exception caught while notification of debit transaction ", ex);
		}
		return res;
	}

	// Debit Transaction
	public Map<String, String> debitTransaction(Fields fields) {

		logger.info("inside debitTransaction for initiate transaction");
		String decryptedResponse = null;
		HttpURLConnection con = null;
		Map<String, String> res = new HashMap<String, String>();
		try {
			String txnType = TransactionType.SALE.getName();
			String status = StatusType.NOTIFIED.getName();
			Map<String, String> notifiedDebitTransaction = upiAutoPayDao
					.getTransactionByPgRefNum(fields.get(FieldType.PG_REF_NUM.getName()), txnType, status);

			if (notifiedDebitTransaction.size() != 0) {
				LinkedHashMap<String, String> requestMap = new LinkedHashMap<String, String>();

				requestMap.put("merchantId", PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MID.getValue()));
				requestMap.put("subMerchantId",
						PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MID.getValue()));
				requestMap.put("terminalId",
						PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_TERMINAL_ID.getValue()));
				requestMap.put("merchantName",
						PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MERCHANT_NAME.getValue()));
				requestMap.put("subMerchantName",
						PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_SUB_MERCHANT_NAME.getValue()));
				requestMap.put("amount", notifiedDebitTransaction.get(FieldType.AMOUNT.getName()));
				requestMap.put("merchantTranId", notifiedDebitTransaction.get(FieldType.PG_REF_NUM.getName()));
				requestMap.put("billNumber", notifiedDebitTransaction.get(FieldType.PG_REF_NUM.getName()));
				requestMap.put("remark", notifiedDebitTransaction.get(FieldType.REMARKS.getName()));
				requestMap.put("retryCount", notifiedDebitTransaction.get(FieldType.NUMBER_OF_RETRY.getName()));
				requestMap.put("mandateSeqNo", notifiedDebitTransaction.get(FieldType.SEQUENCE_NO.getName()));
				requestMap.put("UMN", notifiedDebitTransaction.get(FieldType.UMN.getName()));
				requestMap.put("purpose", notifiedDebitTransaction.get(FieldType.PURPOSE.getName()));

				String requestParam = requestMap.toString().replace("{", "{\"").replace("=", "\":")
						.replace(", ", ", \"").replace("\":", "\":\"").replace("}", "\"}").replace(", \"", "\", \"");

				logger.info("Upi AutoPay debit transaction Request " + requestParam);
				String serviceUrl = PropertiesManager.propertiesMap
						.get(Constants.UPI_AUTOPAY_TRANSACTION_URL.getValue());

				URL requestUrl = new URL(serviceUrl);
				con = (HttpURLConnection) requestUrl.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Type", "application/json");
				con.setRequestProperty("apikey",
						PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_API_KEY.getValue()));

				// Encrypt The Request
				String encryptedRequest = iciciUpiAutoPayUtil.upiAutoPayEncryption(requestParam);
				logger.info("Encrypted Request debit transaction is " + encryptedRequest);
				con.setDoOutput(true);
				con.setUseCaches(false);
				OutputStream os = con.getOutputStream();
				os.write(encryptedRequest.getBytes());
				os.flush();
				os.close();

				int responseCode = con.getResponseCode();
				logger.info("Upi AutoPay debit transaction Response Code " + responseCode);

				if (responseCode == HttpURLConnection.HTTP_OK) {
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					StringBuffer response = new StringBuffer();

					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					logger.info("Encrypted Response recived debit transaction is " + response.toString());
					decryptedResponse = iciciUpiAutoPayUtil.upiAutoPayDecryption(response.toString());
					logger.info("Decrypted Response of ICICI Upi AutoPay debit transaction is " + decryptedResponse);

					JSONParser parser = new JSONParser();
					JSONObject responseJson = (JSONObject) parser.parse(decryptedResponse);

					/*
					 * { "response" : "0", "merchantId" : "106161", "subMerchantId" : "12234",
					 * "terminalId" : "5411", "BankRRN" : "615519221396", "merchantTranId" :
					 * "612411454593", "amount" : "12", "success" : "true", "message" :
					 * "Transaction Successful" }
					 */

					if (responseJson.get("message").toString().equalsIgnoreCase("Transaction Initiated")) {
						res.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
						res.put(FieldType.RESPONSE_MESSAGE.getName(), responseJson.get("message").toString());
					} else {
						res.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
						res.put(FieldType.RESPONSE_MESSAGE.getName(), responseJson.get("message").toString());
					}

					upiAutoPayDao.updateByDebitTransactionResponse(responseJson, notifiedDebitTransaction);
					return res;
				} else {
					logger.info("Error while connecting Response Code is " + con.getResponseCode());
					res.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					res.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TRANSACTION_FAILED.getResponseMessage());
					// return null;
				}
			} else {
				res.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				res.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TRANSACTION_NOT_FOUND.getResponseMessage());
			}

		} catch (Exception ex) {
			logger.info("Exception caught while debit transaction " + ex);
		}
		return res;
	}

	// status enquiry
	@SuppressWarnings("unchecked")
	public Map<String, String> statusEnquiry(Fields fields) {

		logger.info("inside statusEnquiry for status Enquiry");
		String decryptedResponse = null;
		HttpURLConnection con = null;
		Map<String, String> res = new HashMap<String, String>();
		try {
			JSONObject request = new JSONObject();

			request.put("merchantId", PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MID.getValue()));
			request.put("subMerchantId", PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MID.getValue()));
			request.put("terminalId",
					PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_TERMINAL_ID.getValue()));
			request.put("merchantTranId", fields.get(FieldType.PG_REF_NUM.getName()));

			/*
			 * { "merchantId": "118449", "subMerchantId": "118449", "terminalId": "5411",
			 * "merchantTranId": "p0nillp0k9lqlp091p17" }
			 */

			String serviceUrl = PropertiesManager.propertiesMap
					.get(Constants.UPI_AUTOPAY_TRANSACTION_STATUS_ENQUIRY_URL.getValue());

			URL requestUrl = new URL(serviceUrl);
			con = (HttpURLConnection) requestUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("apikey",
					PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_API_KEY.getValue()));

			// Encrypt The Request
			String encryptedRequest = iciciUpiAutoPayUtil.upiAutoPayEncryption(request.toJSONString());
			logger.info("Encrypted Request is " + encryptedRequest);
			con.setDoOutput(true);
			con.setUseCaches(false);
			OutputStream os = con.getOutputStream();
			os.write(encryptedRequest.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();
			logger.info("Upi AutoPay status enquiry Response Code " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				logger.info("Encrypted Response recived is " + response.toString());
				decryptedResponse = iciciUpiAutoPayUtil.upiAutoPayDecryption(response.toString());
				logger.info("Decrypted Response of ICICI Upi AutoPay is " + decryptedResponse);
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(decryptedResponse);
				/*
				 * { "response" : "0", "merchantId" : "106161", "subMerchantId" : "12234",
				 * "terminalId" : "5411", "OriginalBankRRN" : "615519221396", "merchantTranId" :
				 * "612411454593", "amount" : "12", "success" : "true", "message" :
				 * "Transaction Successful", "status" : "SUCCESS", "UMN" :
				 * "8fbadaeb18ff49fdbae7793faa8178d3@upi" }
				 */

				upiAutoPayDao.updateByStatusEnquiry(json);
				res.put(FieldType.STATUS.getName(), json.get("status").toString());
				res.put(FieldType.RESPONSE_MESSAGE.getName(), json.get("message").toString());

				return res;
			} else {
				logger.info("Error while connecting Response Code is " + con.getResponseCode());
				res.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				res.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TRANSACTION_FAILED.getResponseMessage());
				// return null;
			}
		} catch (Exception ex) {
			logger.info("Exception caught while status enquiry " + ex);
		}
		return res;

	}

	// status enquiry by criteria
	@SuppressWarnings("unchecked")
	public Map<String, String> statusEnquiryByCriteria(Fields fields) {

		logger.info("inside statusEnquiryByCriteria for status enquiry by criteria");
		String decryptedResponse = null;
		HttpURLConnection con = null;
		Map<String, String> res = new HashMap<String, String>();
		try {

			JSONObject request = new JSONObject();
			request.put("merchantId", PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MID.getValue()));
			request.put("subMerchantId", PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MID.getValue()));
			request.put("terminalId",
					PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_TERMINAL_ID.getValue()));
			request.put("transactionType", "M");
			request.put("merchantTranId", fields.get(FieldType.PG_REF_NUM.getName()));

			/*
			 * { "merchantId": "118449", "subMerchantId": "118449", "terminalId": "5411",
			 * "transactionType": "M", "merchantTranId": "p0nillp0k9lqlp091p17" }
			 */

			String serviceUrl = PropertiesManager.propertiesMap
					.get(Constants.UPI_AUTOPAY_TRANSACTION_STATUS_ENQUIRY_URL.getValue());

			URL requestUrl = new URL(serviceUrl);
			con = (HttpURLConnection) requestUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("apikey",
					PropertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_API_KEY.getValue()));

			// Encrypt The Request
			String encryptedRequest = iciciUpiAutoPayUtil.upiAutoPayEncryption(request.toJSONString());
			logger.info("Encrypted Request is " + encryptedRequest);
			con.setDoOutput(true);
			con.setUseCaches(false);
			OutputStream os = con.getOutputStream();
			os.write(encryptedRequest.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();
			logger.info("Upi AutoPay status enquiry  by criteria Response Code " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				logger.info("Encrypted Response recived is " + response.toString());
				decryptedResponse = iciciUpiAutoPayUtil.upiAutoPayDecryption(response.toString());
				logger.info("Decrypted Response of ICICI Upi AutoPay is " + decryptedResponse);

				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(decryptedResponse);
				/*
				 * { "response" : "0", "merchantId" : "106161", "subMerchantId" : "12234",
				 * "terminalId" : "5411", "OriginalBankRRN" : "615519221396", "merchantTranId" :
				 * "612411454593", "Amount" : "12", "payerVA" : " testing1@imobile ", "success"
				 * : "true", "message" : "Transaction Successful", "status" : "SUCCESS",
				 * "TxnInitDate" : "20160715142352", "TxnCompletionDate" : "20160715142352",
				 * "UMN" : "8fbadaeb18ff49fdbae7793faa8178d3@upi" }
				 */

				upiAutoPayDao.updateStatusEnquiryByCriteria(json);
				res.put(FieldType.STATUS.getName(), json.get("status").toString());
				res.put(FieldType.RESPONSE_MESSAGE.getName(), json.get("message").toString());

				return res;
			} else {
				logger.info("Error while connecting Response Code is " + con.getResponseCode());
				res.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				res.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TRANSACTION_FAILED.getResponseMessage());
				// return null;
			}

		} catch (Exception ex) {
			logger.info("Exception caught while status enquiry by criteria " + ex);
		}
		return res;
	}

	public Map<String, String> upiAutoPayRegistrationLink(Fields fields) {
		Map<String, String> requestFields = new HashMap<String, String>();
		try {
			// validate fields && validating Hash

			Map<String, String> validateFieldsResult = ValidateUpiAutoPayApiFields(fields);

			if (validateFieldsResult.get(FieldType.RESPONSE_CODE.getName())
					.equalsIgnoreCase(ErrorType.SUCCESS.getResponseCode())) {
				// send email and sms to customer

				Map<String, String> sendLinkResult = sendUpiAutoPayLinks(fields);

				if (sendLinkResult.get(FieldType.RESPONSE_CODE.getName())
						.equalsIgnoreCase(ErrorType.SUCCESS.getResponseCode())) {

					// return link url to merchant

					requestFields.put(FieldType.RESPONSE_MESSAGE.getName(),
							sendLinkResult.get(FieldType.RESPONSE_MESSAGE.getName()));
					requestFields.put(FieldType.RESPONSE_CODE.getName(),
							sendLinkResult.get(FieldType.RESPONSE_CODE.getName()));
					requestFields.put("SEND_EMAIL", sendLinkResult.get("SEND_EMAIL"));
					requestFields.put("SEND_SMS", sendLinkResult.get("SEND_SMS"));
					requestFields.put("MANDATE_URL", sendLinkResult.get("MANDATE_URL"));
					return requestFields;
				} else {
					logger.error("Error in UPI autopay validation or sending email or sms");
					requestFields.put(FieldType.RESPONSE_MESSAGE.getName(),
							sendLinkResult.get(FieldType.RESPONSE_MESSAGE.getName()));
					requestFields.put(FieldType.RESPONSE_CODE.getName(),
							sendLinkResult.get(FieldType.RESPONSE_CODE.getName()));
					requestFields.put("SEND_EMAIL", sendLinkResult.get("SEND_EMAIL"));
					requestFields.put("SEND_SMS", sendLinkResult.get("SEND_SMS"));
					requestFields.put("MANDATE_URL", sendLinkResult.get("MANDATE_URL"));
					return requestFields;
				}
			} else {
				requestFields = validateFieldsResult;
				return requestFields;
			}
		} catch (Exception ex) {
			logger.error("Exception caught in UpiAutoPay mandate registration Request via API >>> ", ex);
			requestFields.put(FieldType.RESPONSE_MESSAGE.getName(), ex.getMessage());
			requestFields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAY_ID.getResponseCode());
			return requestFields;
		}
	}

	private Map<String, String> ValidateUpiAutoPayApiFields(Fields fields) {

		Map<String, String> validationMap = new HashMap<String, String>();

		if (fields.contains(FieldType.MONTHLY_AMOUNT.getName())
				&& StringUtils.isNotBlank(fields.get(FieldType.MONTHLY_AMOUNT.getName()))
				&& NumberUtils.isNumber(fields.get(FieldType.MONTHLY_AMOUNT.getName()))) {

			if (fields.contains(FieldType.FREQUENCY.getName())
					&& StringUtils.isNotBlank(fields.get(FieldType.FREQUENCY.getName()))
					&& StringUtils.isAlpha(fields.get(FieldType.FREQUENCY.getName()))) {

				if (fields.contains(FieldType.TENURE.getName())
						&& StringUtils.isNotBlank(fields.get(FieldType.TENURE.getName()))
						&& StringUtils.isNumeric(fields.get(FieldType.TENURE.getName()))) {

					if (fields.contains(FieldType.PAY_ID.getName())
							&& StringUtils.isNotBlank(fields.get(FieldType.PAY_ID.getName()))
							&& StringUtils.isNumeric(fields.get(FieldType.PAY_ID.getName()))) {

						if (fields.contains("CUST_MOBILE") && StringUtils.isNotBlank(fields.get("CUST_MOBILE"))
								&& StringUtils.isNumeric(fields.get("CUST_MOBILE"))) {

							if (fields.contains(FieldType.CUST_EMAIL.getName())
									&& StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))
									&& generalValidator.isValidEmailId(fields.get(FieldType.CUST_EMAIL.getName()))) {

								if (fields.contains(FieldType.HASH.getName())
										&& StringUtils.isNotBlank(fields.get(FieldType.HASH.getName()))) {
									try {
										String merchantHash = fields.get(FieldType.HASH.getName());
										fields.remove(FieldType.HASH.getName());
										String calculatedHash = Hasher.getHash(fields);

										if (merchantHash.equals(calculatedHash)) {
											// return link url
											logger.info("all request fields are valid");
											validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
													ErrorType.SUCCESS.getResponseMessage());
											validationMap.put(FieldType.RESPONSE_CODE.getName(),
													ErrorType.SUCCESS.getResponseCode());
											return validationMap;
										}
										logger.info("merchant hash not match");
										StringBuilder hashMessage = new StringBuilder("Merchant hash = ");
										hashMessage.append(merchantHash);
										hashMessage.append(", Calculated Hash = ");
										hashMessage.append(calculatedHash);
										logger.error(hashMessage.toString());

										validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
												ErrorType.INVALID_HASH_UPI.getResponseMessage());
										validationMap.put(FieldType.RESPONSE_CODE.getName(),
												ErrorType.INVALID_HASH_UPI.getResponseCode());
										return validationMap;
									} catch (Exception ex) {
										logger.error(
												"Exception caught in UpiAutoPay mandate registration Request via API >>> ",
												ex);
										validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
												ErrorType.UPI_AUTOPAY_ERROR.getResponseMessage());
										validationMap.put(FieldType.RESPONSE_CODE.getName(),
												ErrorType.UPI_AUTOPAY_ERROR.getResponseCode());
										return validationMap;
									}
								}
								logger.info("Invalid HASH");
								validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
										ErrorType.INVALID_HASH_UPI.getResponseMessage());
								validationMap.put(FieldType.RESPONSE_CODE.getName(),
										ErrorType.INVALID_HASH_UPI.getResponseCode());
								return validationMap;
							}
							logger.info("Invalid CUST_EMAIL");
							validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
									ErrorType.INVALID_CUST_EMAIL.getResponseMessage());
							validationMap.put(FieldType.RESPONSE_CODE.getName(),
									ErrorType.INVALID_CUST_EMAIL.getResponseCode());
							return validationMap;
						}
						logger.info("Invalid CUST_MOBILE");
						validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
								ErrorType.INVALID_CUST_MOBILE.getResponseMessage());
						validationMap.put(FieldType.RESPONSE_CODE.getName(),
								ErrorType.INVALID_CUST_MOBILE.getResponseCode());
						return validationMap;
					}
					logger.info("Invalid PAY_ID");
					validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
							ErrorType.INVALID_PAY_ID.getResponseMessage());
					validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAY_ID.getResponseCode());
					return validationMap;
				}
				logger.info("Invalid TENURE");
				validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_TENURE.getResponseMessage());
				validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_TENURE.getResponseCode());
				return validationMap;
			}
			logger.info("Invalid FREQUENCY");
			validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FREQUENCY.getResponseMessage());
			validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FREQUENCY.getResponseCode());
			return validationMap;
		}
		logger.info("Invalid MONTHLY_AMOUNT");
		validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_MONTHLY_AMOUNT.getResponseMessage());
		validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_MONTHLY_AMOUNT.getResponseCode());
		return validationMap;
	}

	private Map<String, String> sendUpiAutoPayLinks(Fields fields) {
		Map<String, String> sendingLinksMap = new HashMap<String, String>();
		try {
			String orderId = "CI" + TransactionManager.getNewTransactionId();
			Map<String, String> requestFields = new HashMap<String, String>();

			requestFields.put(FieldType.AMOUNT.getName(), "1");
			requestFields.put(FieldType.MONTHLY_AMOUNT.getName(), fields.get(FieldType.MONTHLY_AMOUNT.getName()));
			requestFields.put(FieldType.FREQUENCY.getName(), fields.get(FieldType.FREQUENCY.getName()));
			requestFields.put(FieldType.TENURE.getName(), fields.get(FieldType.TENURE.getName()));
			requestFields.put(FieldType.PURPOSE.getName(), "RECURRING");
			requestFields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			requestFields.put(FieldType.CUST_EMAIL.getName(), fields.get(FieldType.CUST_EMAIL.getName()));
			requestFields.put("CUST_MOBILE", fields.get("CUST_MOBILE"));
			requestFields.put(FieldType.ORDER_ID.getName(), orderId);
			requestFields.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestFields)));

			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
				try {
					boolean response = emailServiceProvider.upiAutoPayMandateSignThroughApi(new Fields(requestFields));
					if (response == true) {
						sendingLinksMap.put("SEND_EMAIL_TO", fields.maskEmail(FieldType.CUST_EMAIL.getName()));
						sendingLinksMap.put("SEND_EMAIL", "SUCCESS");
					} else {
						sendingLinksMap.put("SEND_EMAIL_TO", fields.maskEmail(FieldType.CUST_EMAIL.getName()));
						sendingLinksMap.put("SEND_EMAIL", "FAIL");
					}
				} catch (Exception exception) {
					logger.error("exception is >>>>", exception);
					sendingLinksMap.put("SEND_EMAIL_TO", fields.maskEmail(FieldType.CUST_EMAIL.getName()));
					sendingLinksMap.put("SEND_EMAIL", "FAIL");
//					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to emailer");
				}
			}
			if (StringUtils.isNotBlank(fields.get("CUST_MOBILE"))) {
				try {

					String url = propertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MANDATE_SIGN.getValue());
					String mandate_url = url + "?ORDER_ID=:" + requestFields.get(FieldType.ORDER_ID.getName()) + ","
							+ "?AMOUNT=:" + requestFields.get(FieldType.AMOUNT.getName()) + "," + "?MONTHLY_AMOUNT=:"
							+ requestFields.get(FieldType.MONTHLY_AMOUNT.getName()) + "," + "?FREQUENCY=:"
							+ requestFields.get(FieldType.FREQUENCY.getName()) + "," + "?TENURE=:"
							+ requestFields.get(FieldType.TENURE.getName()) + "," + "?PAY_ID=:"
							+ requestFields.get(FieldType.PAY_ID.getName()) + "," + "?CUST_MOBILE=:"
							+ requestFields.get("CUST_MOBILE") + "," + "?CUST_EMAIL=:"
							+ requestFields.get(FieldType.CUST_EMAIL.getName()) + "," + "?PURPOSE=:"
							+ requestFields.get(FieldType.PURPOSE.getName()) + "," + "?HASH=:"
							+ requestFields.get(FieldType.HASH.getName());
					String bitly_url = bitlyUrlShortener.createShortUrlUsingBitly(mandate_url);

					boolean isSmsSend = smsControllerServiceProvider
							.sendUpiAutoPayLinkViaSMS(requestFields.get("CUST_MOBILE"), bitly_url);
					if (isSmsSend == true) {
						sendingLinksMap.put("SEND_SMS_TO", fields.fieldMask("CUST_MOBILE"));
						sendingLinksMap.put("SEND_SMS", "SUCCESS");
					} else {
						sendingLinksMap.put("SEND_SMS_TO", fields.fieldMask("CUST_MOBILE"));
						sendingLinksMap.put("SEND_SMS", "FAIL");
					}
				} catch (Exception exception) {
					logger.error("exception is ", exception);
					sendingLinksMap.put("SEND_SMS_TO", fields.fieldMask("CUST_MOBILE"));
					sendingLinksMap.put("SEND_SMS", "FAIL");
//					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to SMS ");
				}
			}
			sendingLinksMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
			sendingLinksMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

			String url = propertiesManager.propertiesMap.get(Constants.UPI_AUTOPAY_MANDATE_SIGN.getValue());
			String mandate_url = url + "?ORDER_ID=:" + requestFields.get(FieldType.ORDER_ID.getName()) + ","
					+ "?AMOUNT=:" + requestFields.get(FieldType.AMOUNT.getName()) + "," + "?MONTHLY_AMOUNT=:"
					+ requestFields.get(FieldType.MONTHLY_AMOUNT.getName()) + "," + "?FREQUENCY=:"
					+ requestFields.get(FieldType.FREQUENCY.getName()) + "," + "?TENURE=:"
					+ requestFields.get(FieldType.TENURE.getName()) + "," + "?PAY_ID=:"
					+ requestFields.get(FieldType.PAY_ID.getName()) + "," + "?CUST_MOBILE=:"
					+ requestFields.get("CUST_MOBILE") + "," + "?CUST_EMAIL=:"
					+ requestFields.get(FieldType.CUST_EMAIL.getName()) + "," + "?PURPOSE=:"
					+ requestFields.get(FieldType.PURPOSE.getName()) + "," + "?HASH=:"
					+ requestFields.get(FieldType.HASH.getName());
			String bitly_url = bitlyUrlShortener.createShortUrlUsingBitly(mandate_url);
			sendingLinksMap.put("MANDATE_URL", bitly_url);
		} catch (Exception ex) {
			logger.error("Exception in UpiAutoPay mandate registration Request via API >>> ", ex);
			sendingLinksMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.UPI_AUTOPAY_ERROR.getResponseMessage());
			sendingLinksMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.UPI_AUTOPAY_ERROR.getResponseCode());
		}

		return sendingLinksMap;
	}

	public Map<String, String> upiAutoPayMandateEnquiry(Fields fields) {
		Map<String, String> requestFields = new HashMap<String, String>();
		try {
			// validate fields && validating Hash
			Map<String, String> validateFieldsResult = ValidateUpiAutoPayMandateEnquiryApiFields(fields);
			if (validateFieldsResult.get(FieldType.RESPONSE_CODE.getName())
					.equalsIgnoreCase(ErrorType.SUCCESS.getResponseCode())) {
				// return status of mandate to merchant

				requestFields = upiAutoPayDao.getUpiAutoPayMandateDetailsByOrderId(fields);
				requestFields.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestFields)));

				return requestFields;
			} else {
				requestFields = validateFieldsResult;
				return requestFields;
			}
		} catch (Exception ex) {
			logger.error("Exception caught in UpiAutoPay mandate registration Request via API >>> ", ex);
			requestFields.put(FieldType.RESPONSE_MESSAGE.getName(), ex.getMessage());
			requestFields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAY_ID.getResponseCode());
			return requestFields;
		}
	}

	private Map<String, String> ValidateUpiAutoPayMandateEnquiryApiFields(Fields fields) {

		Map<String, String> validationMap = new HashMap<String, String>();

		if (fields.contains(FieldType.PAY_ID.getName())
				&& StringUtils.isNotBlank(fields.get(FieldType.PAY_ID.getName()))
				&& StringUtils.isNumeric(fields.get(FieldType.PAY_ID.getName()))) {

			if (fields.contains(FieldType.ORDER_ID.getName())
					&& StringUtils.isNotBlank(fields.get(FieldType.ORDER_ID.getName()))) {

				if (fields.contains(FieldType.HASH.getName())
						&& StringUtils.isNotBlank(fields.get(FieldType.HASH.getName()))) {
					try {
						String merchantHash = fields.get(FieldType.HASH.getName());
						fields.remove(FieldType.HASH.getName());
						String calculatedHash = Hasher.getHash(fields);

						if (merchantHash.equals(calculatedHash)) {
							// return link url
							logger.info("all request fields are valid");
							validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
									ErrorType.SUCCESS.getResponseMessage());
							validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
							return validationMap;
						}
						logger.info("merchant hash not match");
						StringBuilder hashMessage = new StringBuilder("Merchant hash = ");
						hashMessage.append(merchantHash);
						hashMessage.append(", Calculated Hash = ");
						hashMessage.append(calculatedHash);
						logger.error(hashMessage.toString());

						validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
								ErrorType.INVALID_HASH_UPI.getResponseMessage());
						validationMap.put(FieldType.RESPONSE_CODE.getName(),
								ErrorType.INVALID_HASH_UPI.getResponseCode());
						return validationMap;
					} catch (Exception ex) {
						logger.error("Exception caught in UpiAutoPay mandate registration Request via API >>> ", ex);
						validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
								ErrorType.UPI_AUTOPAY_ERROR.getResponseMessage());
						validationMap.put(FieldType.RESPONSE_CODE.getName(),
								ErrorType.UPI_AUTOPAY_ERROR.getResponseCode());
						return validationMap;
					}
				}
				logger.info("Invalid HASH");
				validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.INVALID_HASH_UPI.getResponseMessage());
				validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH_UPI.getResponseCode());
				return validationMap;
			}
			logger.info("Invalid ORDER ID");
			validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_ORDER_ID.getResponseMessage());
			validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_ORDER_ID.getResponseCode());
			return validationMap;
		}
		logger.info("Invalid PAY_ID");
		validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAY_ID.getResponseMessage());
		validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAY_ID.getResponseCode());
		return validationMap;
	}

}
