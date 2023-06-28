package com.paymentgateway.kotak;

import java.io.IOException;
import java.security.Key;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

@Service("kotakTransactionConverter")
public class TransactionConverter {

	private static Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	@SuppressWarnings("incomplete-switch")
	public String perpareRequest(Fields fields, Transaction transaction) throws SystemException {

		String request = null;

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case AUTHORISE:
		case ENROLL:
			break;
		case REFUND:
			request = refundRequest(fields, transaction);
			break;
		case SALE:
			request = saleRequest(fields, transaction);
			break;
		case CAPTURE:
			break;
		case STATUS:
			request = statusEnquiryRequest(fields, transaction);
			break;
		}
		return request;

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		String returnUrl = PropertiesManager.propertiesMap.get(Constants.SALE_RETURN_URL);

		StringBuilder request = new StringBuilder();
		request.append(Constants.ORDER_INFO);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.ORDER_ID.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.PASS_CODE);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.ADF3.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.MERCHANT_ID);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.MERCHANT_ID.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.TXN_REF_NO);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.TERMINAL_ID);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.ADF1.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.TXN_TYPE);
		request.append(Constants.EQUATOR);
		request.append(transaction.getTxnType());
		request.append(Constants.SEPARATOR);
		request.append(Constants.AMOUNT);
		request.append(Constants.EQUATOR);
		request.append(amount);
		request.append(Constants.SEPARATOR);
		request.append(Constants.MCC);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.ADF2.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.RETURN_URL);
		request.append(Constants.EQUATOR);
		request.append(returnUrl);
		request.append(Constants.SEPARATOR);
		request.append(Constants.CARD_NUMBER);
		request.append(Constants.EQUATOR);
		request.append(transaction.getCardNumber());
		request.append(Constants.SEPARATOR);
		request.append(Constants.EXPIRY_DATE);
		request.append(Constants.EQUATOR);
		request.append(transaction.getExpiryDate());
		request.append(Constants.SEPARATOR);
		request.append(Constants.CVV);
		request.append(Constants.EQUATOR);
		request.append(transaction.getCvv());
		request.append(Constants.SEPARATOR);
		request.append(Constants.MERCHANT_NAME);
		request.append(Constants.EQUATOR);
		request.append(Constants.MERCHANT_NAME_VALUE);
		request.append(Constants.SEPARATOR);
		request.append(Constants.MERCHANT_CITY);
		request.append(Constants.EQUATOR);
		request.append(Constants.MERCHANT_CITY_VALUE);
		request.append(Constants.SEPARATOR);
		request.append(Constants.MERCHANT_STATE);
		request.append(Constants.EQUATOR);
		request.append(Constants.MERCHANT_STATE_VALLUE);
		request.append(Constants.SEPARATOR);
		request.append(Constants.MERCHANT_POSTAL_CODE);
		request.append(Constants.EQUATOR);
		request.append(Constants.MERCHANT_POSTAL_CODE_VALUE);
		request.append(Constants.SEPARATOR);
		request.append(Constants.MERCHANT_PHONE);
		request.append(Constants.EQUATOR);
		request.append(Constants.MERCHANT_PHONE_VALUE);
		request.append(Constants.SEPARATOR);
		request.append(Constants.CURRENCY);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.CURRENCY_CODE.getName()));
		request.append(Constants.SEPARATOR);

		String hash = "";
		String hashrequest = hashRequest(fields, transaction);
		try {
			hash = Hasher.getHash(hashrequest);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}

		request.append(Constants.HASH);
		request.append(Constants.EQUATOR);
		request.append(hash);
		
		//logRequest(request.toString(), fields);

		String encryptionKey = fields.get(FieldType.TXN_KEY.getName());
		String encryptedString = null;
		try {
			encryptedString = encrypt(request.toString(), encryptionKey);
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

		return encryptedString;

	}

	public String hashRequest(Fields fields, Transaction transaction) throws SystemException {
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		String returnUrl = PropertiesManager.propertiesMap.get(Constants.SALE_RETURN_URL);
		StringBuilder hashrequest = new StringBuilder();
		hashrequest.append(fields.get(FieldType.PASSWORD.getName()));
		hashrequest.append(amount);
		hashrequest.append(transaction.getCardNumber());
		hashrequest.append(transaction.getCvv());
		hashrequest.append(fields.get(FieldType.CURRENCY_CODE.getName()));
		hashrequest.append(transaction.getExpiryDate());
		hashrequest.append(fields.get(FieldType.ADF2.getName()));
		hashrequest.append(Constants.MERCHANT_PHONE_VALUE);
		hashrequest.append(Constants.MERCHANT_POSTAL_CODE_VALUE);
		hashrequest.append(Constants.MERCHANT_CITY_VALUE);
		hashrequest.append(fields.get(FieldType.MERCHANT_ID.getName()));
		hashrequest.append(Constants.MERCHANT_NAME_VALUE);
		hashrequest.append(Constants.MERCHANT_STATE_VALLUE);
		hashrequest.append(fields.get(FieldType.ORDER_ID.getName()));
		hashrequest.append(fields.get(FieldType.ADF3.getName()));
		hashrequest.append(returnUrl);
		hashrequest.append(fields.get(FieldType.ADF1.getName()));
		hashrequest.append(fields.get(FieldType.PG_REF_NUM.getName()));
		hashrequest.append(transaction.getTxnType());
		return hashrequest.toString();

	}

	public static String encrypt(String Data, String keySet) throws Exception {
		byte[] keyByte = keySet.getBytes();
		Key key = generateKey(keyByte);
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encVal = c.doFinal(Data.getBytes());
		byte[] encryptedByteValue = new Base64().encode(encVal);
		String encryptedValue = new String(encryptedByteValue);
		return encryptedValue;
	}

	private static Key generateKey(byte[] keyByte) throws Exception {
		Key key = new SecretKeySpec(keyByte, "AES");
		return key;
	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {

		String amount = acquirerTxnAmountProvider.amountProvider(fields);

		StringBuilder request = new StringBuilder();
		request.append(Constants.TXN_REF_NO);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.ORIG_TXN_ID.getName()));
		request.append(Constants.REFUND_SEPARATOR);

		request.append(Constants.TXN_TYPE);
		request.append(Constants.EQUATOR);
		request.append(transaction.getTxnType());
		request.append(Constants.REFUND_SEPARATOR);

		request.append(Constants.MERCHANT_ID);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.MERCHANT_ID.getName()));
		request.append(Constants.REFUND_SEPARATOR);

		request.append(Constants.PASS_CODE);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.ADF3.getName()));
		request.append(Constants.REFUND_SEPARATOR);

		request.append(Constants.AMOUNT);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.SALE_TOTAL_AMOUNT.getName()));
		request.append(Constants.REFUND_SEPARATOR);

		request.append(Constants.TERMINAL_ID);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.ADF1.getName()));
		request.append(Constants.REFUND_SEPARATOR);

		request.append(Constants.RET_REF_NO);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.ACQ_ID.getName()));
		request.append(Constants.REFUND_SEPARATOR);

		request.append(Constants.REFUND_AMOUNT);
		request.append(Constants.EQUATOR);
		request.append(amount);
		request.append(Constants.REFUND_SEPARATOR);

		request.append(Constants.REF_CANCEL_ID);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.REFUND_SEPARATOR);

		String hash = "";
		String hashrequest = hashforRefundRequest(fields, request.toString());
		try {
			hash = Hasher.getHash(hashrequest);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}
		
		fields.put(FieldType.HASH.getName(), hash);		
		
		return request.toString();

	}

	public String hashforRefundRequest(Fields fields, String request) throws SystemException {

		String[] ary = request.split(Constants.REFUND_SEPARATOR);
		Arrays.sort(ary);
		StringBuilder hashString = new StringBuilder();
		Map<String, String> myMap = new TreeMap<String, String>();

		for (int i = 0; i < ary.length; i++) {

			String key = ary[i].split("=")[0];
			String value = ary[i].split("=")[1];

			myMap.put(key, value);
		}
		hashString.append(fields.get(FieldType.PASSWORD.getName()));
		for (Map.Entry<String, String> param : myMap.entrySet()) {

			if (param.getKey().equalsIgnoreCase(Constants.HASH)) {
				continue;
			} else {

				hashString.append(param.getValue());
			}
		}
		return hashString.toString();
	}

	public String statusEnquiryRequest(Fields fields, Transaction transaction) throws SystemException {

		StringBuilder request = new StringBuilder();
		request.append(Constants.TXN_REF_NO);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.ORDER_ID.getName()));
		request.append(Constants.REFUND_SEPARATOR);

		request.append(Constants.TXN_TYPE);
		request.append(Constants.EQUATOR);
		request.append(transaction.getTxnType());
		request.append(Constants.REFUND_SEPARATOR);

		request.append(Constants.MERCHANT_ID);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.MERCHANT_ID.getName()));
		request.append(Constants.REFUND_SEPARATOR);

		request.append(Constants.PASS_CODE);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.ADF3.getName()));
		request.append(Constants.REFUND_SEPARATOR);

		request.append(Constants.TERMINAL_ID);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.ADF1.getName()));
		request.append(Constants.REFUND_SEPARATOR);

		String hash = "";
		String hashrequest = hashforRefundRequest(fields, request.toString());
		try {
			hash = Hasher.getHash(hashrequest);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}
		
		fields.put(FieldType.HASH.getName(), hash);
		return request.toString();

	}
	
	public String transact(HttpMethod httpMethod, String hostUrl)
			throws SystemException {
		String response = "";

		try {
			HttpClient httpClient = new HttpClient();
			httpClient.executeMethod(httpMethod);

			if (httpMethod.getStatusCode() == HttpStatus.SC_OK) {
				response = httpMethod.getResponseBodyAsString();
				logger.info("Response from mobikwik: " + response);
			} else {
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
						"Network Exception with Mobikwik " + hostUrl.toString()
								+ "recieved response code"
								+ httpMethod.getStatusCode());
			}
		} catch (IOException ioException) {
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
					ioException, "Network Exception with Mobikwik "
							+ hostUrl.toString());
		}
		logger.info("Response message from mobikwik: " + response);
		return response;
	}
	
	public Transaction toTransaction(String response) {

		Transaction transaction = new Transaction();

		String[] keyValuePairs = response.split(Constants.REFUND_SEPARATOR);
		Map<String, String> requestMap = new HashMap<String, String>();

		for (String pair : keyValuePairs) {
			String[] keyValue = pair.split(Constants.EQUATOR);
			int arrayLength = keyValue.length;
			if (arrayLength == 1) {
				requestMap.put(keyValue[0], Constants.ZERO_VALUE);
			} else {
				requestMap.put(keyValue[0], keyValue[1]);
			}

		}

		transaction.setResponseCode(requestMap.get(Constants.RESPONSE_CODE));
		transaction.setAcqId(requestMap.get(Constants.RET_REF_NO));
		transaction.setMessage(requestMap.get(Constants.MESSAGE));
		transaction.setAuthCode(requestMap.get(Constants.AUTH_CODE));
		transaction.setStatus(requestMap.get(Constants.STATUS));
		return transaction;
	}
	
	public void logRequest(String requestMessage, Fields fields){
		log("Request message to KOTAK bank: Url= "+ requestMessage, fields);
	}
	
	private void log(String message, Fields fields){
		message = Pattern.compile("(<CardNumber>)([\\s\\S]*?)(</card>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<ExpiryDate>)([\\s\\S]*?)(</pan>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<CardSecurityCode>)([\\s\\S]*?)(</expmonth>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<TerminalId>)([\\s\\S]*?)(</expyear>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<PassCode>)([\\s\\S]*?)(</cvv2>)").matcher(message).replaceAll("$1$3");
		//message = Pattern.compile("(<password>)([\\s\\S]*?)(</password>)").matcher(message).replaceAll("$1$3");
		MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
		logger.info(message);
	}


}
