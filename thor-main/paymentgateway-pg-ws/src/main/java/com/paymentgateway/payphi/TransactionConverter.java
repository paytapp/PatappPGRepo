package com.paymentgateway.payphi;

import java.net.URLEncoder;
import java.security.spec.KeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
import com.paymentgateway.pg.core.util.ISGPayEncDecUtil;
import com.paymentgateway.pg.core.util.PayphiUtil;

/**
 * @author Shaiwal
 *
 */
@Service("payphiTransactionConverter")
public class TransactionConverter {

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	@Autowired
	private PayphiUtil payphiUtil;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private UserSettingDao userSettingDao;


	private static byte[] SALT = new byte[] { -57, -75, -103, -12, 75, 124, -127, 119 };
	private static Map<String, SecretKey> encDecMap = new HashMap<String, SecretKey>();

	private static final Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	public static final String DOUBLE_PIPE = "||";
	public static final String PIPE = "|";

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
		return request.toString();

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {

		try {
			String amount = acquirerTxnAmountProvider.amountProvider(fields);
			if (!amount.contains(".")) {
				amount = Amount.toDecimal(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
			}
			transaction.setAmount(amount);
			String returnUrl = null;
			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId((fields.get(FieldType.PAY_ID.getName())));
			if (userSetting.isAllowCustomHostedUrl()) {
				returnUrl = (userSetting.getCustomHostedUrl() + "/pgui/jsp/payphiResponse");
			} else {
				returnUrl = propertiesManager.propertiesMap.get(Constants.PAYPHI_RETURN_URL);
			}

			Map<String, String> hmReqFields = new TreeMap<String, String>();

			hmReqFields.put(Constants.MERCHANT_ID, transaction.getMerchantID());
			hmReqFields.put(Constants.MERCH_TXN_NO, transaction.getMerchantTxnNo());
			hmReqFields.put(Constants.AGG_ID, transaction.getAggregatorID());
			hmReqFields.put(Constants.AMOUNT, transaction.getAmount());
			hmReqFields.put(Constants.CURRENCY_CODE, transaction.getCurrencyCode());
			hmReqFields.put(Constants.PAY_TYPE, transaction.getPayType());
			
			hmReqFields.put(Constants.PAYMENT_MODE, transaction.getPaymentMode());
			hmReqFields.put(Constants.CUST_EMAIL_ID, transaction.getCustomerEmailID());
			hmReqFields.put(Constants.TXN_TYPE, transaction.getTransactionType());
			hmReqFields.put(Constants.TXN_DATE, transaction.getTxnDate());
			hmReqFields.put(Constants.RETURN_URL, returnUrl);
			hmReqFields.put(Constants.CUST_MOBILE_NO, transaction.getCustomerMobileNo());
			hmReqFields.put(Constants.CALL_TYPE, transaction.getCallType());
			
			if (transaction.getPaymentMode().equalsIgnoreCase("CARD")) {
				
				hmReqFields.put(Constants.CARD_NO, transaction.getCardNo());
				hmReqFields.put(Constants.CARD_EXP, transaction.getCardExp());
				hmReqFields.put(Constants.NAME_ON_CARD, transaction.getNameOnCard());
				hmReqFields.put(Constants.CVV, transaction.getCvv());
				hmReqFields.put(Constants.PAY_OPT_CODE, transaction.getPaymentOptionCodes());
				
			}
			else if (transaction.getPaymentMode().equalsIgnoreCase("UPI")) {
				hmReqFields.put(Constants.CUST_UPI_ALIAS, transaction.getCustomerUPIAlias());
			}
			else if (transaction.getPaymentMode().equalsIgnoreCase("NB")) {
				hmReqFields.put(Constants.PAY_OPT_CODE, transaction.getPaymentOptionCodes());
			}

			StringBuilder sb = new StringBuilder();

			for (Entry<String, String> entry : hmReqFields.entrySet()) {
				sb.append(entry.getValue());
			}

			String hashValue = payphiUtil.generateHash(sb.toString(), transaction.getTxnKey());
			hmReqFields.put(Constants.SECURE_HASH, hashValue);

			StringBuilder finalReq = new StringBuilder();

			for (Entry<String, String> entry : hmReqFields.entrySet()) {
				
				finalReq.append(entry.getKey());
				finalReq.append("=");
				finalReq.append(entry.getValue());
				finalReq.append("&");
			}
			
			String finalRequest = finalReq.toString();
			
			// Remove params before logging
			if (transaction.getPaymentMode().equalsIgnoreCase("CARD")) {
				hmReqFields.put(Constants.CARD_NO, "");
				hmReqFields.put(Constants.CARD_EXP, "");
				hmReqFields.put(Constants.CVV, "");
			}
		
						
			StringBuilder logBuilder = new StringBuilder();

			for (Entry<String, String> entry : hmReqFields.entrySet()) {
				
				logBuilder.append(entry.getKey());
				logBuilder.append("=");
				logBuilder.append(entry.getValue());
				logBuilder.append("&");
			}
			
			logger.info("Payphi Final Request == " + logBuilder.toString());
			return finalRequest;
		}

		catch (Exception e) {
			logger.error("Exception in Payphi Sale Request",e);
		}
		return null;
	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {

		Map<String, String> hmReqFields = new TreeMap<String, String>();
		StringBuilder finalReq = new StringBuilder();
		
		try {

			hmReqFields.put(Constants.MERCHANT_ID, transaction.getMerchantID());
			hmReqFields.put(Constants.AGG_ID, transaction.getAggregatorID());
			hmReqFields.put(Constants.MERCH_TXN_NO, transaction.getMerchantTxnNo());
			hmReqFields.put(Constants.ORIG_TXN_NO,transaction.getOriginalTxnNo());
			hmReqFields.put(Constants.AMOUNT, transaction.getAmount());
			hmReqFields.put(Constants.TXN_TYPE, transaction.getTransactionType());

			StringBuilder sb = new StringBuilder();

			for (Entry<String, String> entry : hmReqFields.entrySet()) {
				sb.append(entry.getValue());
			}

			String hashValue = payphiUtil.generateHash(sb.toString(), transaction.getTxnKey());
			hmReqFields.put(Constants.SECURE_HASH, hashValue);

			for (Entry<String, String> entry : hmReqFields.entrySet()) {
				
				finalReq.append(entry.getKey());
				finalReq.append("=");
				finalReq.append(entry.getValue());
				finalReq.append("&");
			}
			
			return finalReq.toString();
		}

		catch (Exception e) {
			logger.error("Exception in preparing payphi refund request", e);
		}
		return finalReq.toString();
	}

	private String createPostDataFromMap(final Map<String, String> fields) {
		final StringBuffer buf = new StringBuffer();
		String ampersand = "";
		for (final String key : fields.keySet()) {
			final String value = fields.get(key);
			if (value != null && value.length() > 0) {
				buf.append(ampersand);
				buf.append(URLEncoder.encode(key));
				buf.append('=');
				buf.append(URLEncoder.encode(value));
			}
			ampersand = "&";
		}
		return buf.toString();
	}

	public String statusEnquiryRequest(Fields fields, Transaction transaction) {

		StringBuilder requestString = new StringBuilder();

		try {

			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss");  
		    String strDate= formatter.format(date);  
		    
		    
			requestString.append(fields.get(FieldType.MERCHANT_ID.getName()));
			requestString.append("||");
			requestString.append(fields.get(FieldType.ADF1.getName()));
			requestString.append("||");

			StringBuilder enquiryString = new StringBuilder();

			// Outer BEI to show one FEI present
			enquiryString.append("1");
			enquiryString.append("||");

			// FEI-1 BEI to show 2 or 1 fields present

			if (StringUtils.isNotBlank(fields.get(FieldType.ACQ_ID.getName()))) {
				enquiryString.append("110");
				enquiryString.append("|");
				enquiryString.append(fields.get(FieldType.ACQ_ID.getName()));
				enquiryString.append("|");
				enquiryString.append(fields.get(FieldType.PG_REF_NUM.getName()));
			} else {
				enquiryString.append("010");
				enquiryString.append("|");
				enquiryString.append(fields.get(FieldType.PG_REF_NUM.getName()));
			}

			String encryptionKey = fields.get(FieldType.TXN_KEY.getName());
			SecretKey secretKey = null;

			if (encDecMap.get(encryptionKey) != null) {
				secretKey = encDecMap.get(encryptionKey);
			} else {
				try {
					SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
					KeySpec keySpec = new PBEKeySpec(encryptionKey.toCharArray(), SALT, 65536, 256);
					SecretKey secretKeyTemp = secretKeyFactory.generateSecret(keySpec);
					secretKey = new SecretKeySpec(secretKeyTemp.getEncoded(), "AES");
				} catch (Exception e) {
					e.printStackTrace();
				}
				encDecMap.put(encryptionKey, secretKey);
			}

			ISGPayEncDecUtil aesEncrypt = new ISGPayEncDecUtil(encryptionKey, secretKey);
			logger.info("ISGPay Status Enquiry Request before encryption : " + enquiryString.toString());
			String encRequest = aesEncrypt.encrypt(enquiryString.toString());
			requestString.append(encRequest);
			return requestString.toString();
		}

		catch (Exception e) {
			logger.error("Exception in preapring isgpay status enquiry request", e);
		}
		return requestString.toString();
	}

	@SuppressWarnings("null")
	public Transaction toTransaction(String ampResponse, String txnType) {

		Transaction transaction = new Transaction();
		try {
			logger.info("API Response for payphi == "+ampResponse);
			
			if (txnType.equalsIgnoreCase(TransactionType.REFUND.getName())) {
				
				JSONObject resJson = new JSONObject(ampResponse);
				
				if (ampResponse.contains("respDescription")) {
					transaction.setRespDescription(resJson.get("respDescription").toString());
				}
				
				if (ampResponse.contains("merchantTxnNo")) {
					transaction.setMerchantTxnNo(resJson.get("merchantTxnNo").toString());
				}
				
				if (ampResponse.contains("txnID")) {
					transaction.setTxnID(resJson.get("txnID").toString());
				}
				
				if (ampResponse.contains("responseCode")) {
					transaction.setResponseCode(resJson.get("responseCode").toString());
				}
				
				
			}
			
			else if  (txnType.equalsIgnoreCase(TransactionType.STATUS.getName())) {
				
				JSONObject resJson = new JSONObject(ampResponse);
				

				if (ampResponse.contains("txnRespDescription")) {
					transaction.setTxnRespDescription(resJson.get("txnRespDescription").toString());
				}
				
				if (ampResponse.contains("txnStatus")) {
					transaction.setTxnStatus(resJson.get("txnStatus").toString());
				}
				
				if (ampResponse.contains("txnResponseCode")) {
					transaction.setTxnResponseCode(resJson.get("txnResponseCode").toString());
				}
				
				if (ampResponse.contains("txnAuthID")) {
					transaction.setTxnAuthID(resJson.get("txnAuthID").toString());
				}
				
				if (ampResponse.contains("merchantTxnNo")) {
					transaction.setMerchantTxnNo(resJson.get("merchantTxnNo").toString());
				}
				
				if (ampResponse.contains("txnID")) {
					transaction.setTxnID(resJson.get("txnID").toString());
				}
				
				if (ampResponse.contains("responseCode")) {
					transaction.setResponseCode(resJson.get("responseCode").toString());
				}
				
			}
			
		}
		
		catch(Exception e) {
			logger.error("Exception in parsing response for payphi" ,e);
		}
		
		return transaction;
	}

	public TransactionConverter() {
	}

}
