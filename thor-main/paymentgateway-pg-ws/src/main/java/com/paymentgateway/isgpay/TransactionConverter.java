package com.paymentgateway.isgpay;

import java.net.URLEncoder;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
import com.paymentgateway.pg.core.util.ISGPayDecryption;
import com.paymentgateway.pg.core.util.ISGPayEncDecUtil;
import com.paymentgateway.pg.core.util.ISGPayEncryption;

/**
 * @author Rahul
 *
 */
@Service("iSGPayTransactionConverter")
public class TransactionConverter {

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	@Autowired
	private ISGPayEncryption iSGPayEncryption;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private ISGPayDecryption iSGPayDecryption;

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
			String returnUrl = null;
			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId((fields.get(FieldType.PAY_ID.getName())));
			if (userSetting.isAllowCustomHostedUrl()) {
				returnUrl = (userSetting.getCustomHostedUrl() + "/pgui/jsp/isgPayResponse?pgRefNo=");
			} else {
				returnUrl = PropertiesManager.propertiesMap.get(Constants.ISGPAY_RETURN_URL);
			}
			returnUrl = returnUrl + fields.get(FieldType.PG_REF_NUM.getName());
			fields.put(FieldType.TERMINAL_ID.getName(), transaction.getTerminalId());
			fields.put(FieldType.BANK_ID.getName(), transaction.getBankId());

			LinkedHashMap<String, String> hmReqFields = new LinkedHashMap<String, String>();
			hmReqFields.put(Constants.PASS_CODE, transaction.getAccessCode());
			hmReqFields.put(Constants.AMOUNT, amount);
			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
				hmReqFields.put(Constants.BANK_CODE,
						ISGPayBankCode.getBankCode(fields.get(FieldType.MOP_TYPE.getName())));
			} else if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())) {
				hmReqFields.put(Constants.VPA, (fields.get(FieldType.PAYER_ADDRESS.getName())));
			} else {
				hmReqFields.put(Constants.CARD_NUMBER, transaction.getCardNumber());
				hmReqFields.put(Constants.EXPIRY_DATE, transaction.getExpDate());
				hmReqFields.put(Constants.CVV, transaction.getCvv());
			}
			hmReqFields.put(Constants.MERCHANT_ID, transaction.getMerchantId());
			hmReqFields.put(Constants.TXN_REF_NO, transaction.getTxnRefNum());
			hmReqFields.put(Constants.TERMINAL_ID, transaction.getTerminalId());
			hmReqFields.put(Constants.TXN_TYPE, transaction.getTxnType());
			hmReqFields.put(Constants.MCC, transaction.getMcc());
			hmReqFields.put(Constants.RETURN_URL, returnUrl);
			/*
			 * hmReqFields.put(Constants.MERCHANT_NAME, Constants.MERCHANT_NAME_VALUE);
			 * hmReqFields.put(Constants.MERCHANT_CITY, Constants.MERCHANT_CITY_VALUE);
			 * hmReqFields.put(Constants.MERCHANT_STATE, Constants.MERCHANT_STATE_VALLUE);
			 * hmReqFields.put(Constants.MERCHANT_POSTAL_CODE,
			 * Constants.MERCHANT_POSTAL_CODE_VALUE);
			 * hmReqFields.put(Constants.MERCHANT_PHONE, Constants.MERCHANT_PHONE_VALUE);
			 */
			hmReqFields.put(Constants.CURRENCY, transaction.getCurrency());
			hmReqFields.put(Constants.PAY_OPT, transaction.getPayOpt());

			Map<String, String> ENC_DATA_MAP = iSGPayEncryption.encrypt(hmReqFields, transaction.getMerchantId(),
					transaction.getTerminalId(), transaction.getBankId(), transaction.getVersion(),
					transaction.getEncryptionKey(), transaction.getSalt());

			// Remove params before logging
			hmReqFields.put(Constants.CARD_NUMBER, "");
			hmReqFields.put(Constants.EXPIRY_DATE, "");
			hmReqFields.put(Constants.CVV, "");
			logger.info("ISGPAY payment request >>> " + hmReqFields);
			return ENC_DATA_MAP.get("ENC_DATA").toString();
		}

		catch (Exception e) {
			logger.error("Exception in ISGPAY Sale Request : ", e);
		}
		return null;
	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {

		StringBuilder requestString = new StringBuilder();

		try {
			String amount = acquirerTxnAmountProvider.amountProvider(fields);
			fields.put(FieldType.TERMINAL_ID.getName(), transaction.getTerminalId());
			fields.put(FieldType.BANK_ID.getName(), transaction.getBankId());

			LinkedHashMap<String, String> hmReqFields = new LinkedHashMap<String, String>();

			hmReqFields.put(Constants.PASS_CODE, transaction.getAccessCode());
			hmReqFields.put(Constants.AUTH_CODE, transaction.getAuthCode());
			hmReqFields.put(Constants.REFUND_AMOUNT, amount);
			hmReqFields.put(Constants.MERCHANT_ID, transaction.getMerchantId());
			hmReqFields.put(Constants.TXN_REF_NO, transaction.getTxnRefNum());
			hmReqFields.put(Constants.TERMINAL_ID, transaction.getTerminalId());
			hmReqFields.put(Constants.RET_REF_NO, transaction.getRetRefNo());
			hmReqFields.put(Constants.TXN_TYPE, transaction.getTxnType());
			hmReqFields.put(Constants.BANK_ID, transaction.getBankId());

			String hashCode = iSGPayEncryption.generateHash(hmReqFields, transaction.getSalt());
			hmReqFields.put(Constants.HASH, hashCode);

			requestString.append(createPostDataFromMap(hmReqFields));
			return requestString.toString();
		}

		catch (Exception e) {
			logger.error("Exception in preparing ISGPay refund request : ", e);
		}

		return requestString.toString();

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
			logger.error("Exception in preapring isgpay status enquiry request : ", e);
		}
		return requestString.toString();
	}

	@SuppressWarnings("null")
	public Transaction toTransaction(String ampResponse, String txnType) {

		Transaction transaction = new Transaction();
		Map<String, String> responseMap = new HashMap<String, String>();

		if (txnType.equalsIgnoreCase(TransactionType.REFUND.getName())) {

			String[] ampResponseArray = ampResponse.split("&");

			if (ampResponseArray.length < 2) {
				return transaction;
			}

			for (String element : ampResponseArray) {
				String[] elementArray = element.split("=");
				responseMap.put(elementArray[0], elementArray[1]);
			}

			if (StringUtils.isNotBlank(responseMap.get(Constants.STATUS))) {
				transaction.setStatus(responseMap.get(Constants.STATUS));
			}

			if (StringUtils.isNotBlank(responseMap.get(Constants.STATUS))) {
				transaction.setResponseCode(responseMap.get(Constants.STATUS));
			}

			if (StringUtils.isNotBlank(responseMap.get(Constants.PASS_CODE))) {
				transaction.setAccessCode(responseMap.get(Constants.PASS_CODE));
			}

			if (StringUtils.isNotBlank(responseMap.get(Constants.TXN_TYPE))) {
				transaction.setTxnType(responseMap.get(Constants.TXN_TYPE));
			}

			if (StringUtils.isNotBlank(responseMap.get(Constants.AUTH_CODE))) {
				transaction.setAuthCode(responseMap.get(Constants.AUTH_CODE));
			}

			if (StringUtils.isNotBlank(responseMap.get(Constants.MESSAGE))) {
				transaction.setMessage(responseMap.get(Constants.MESSAGE));
			}

			if (StringUtils.isNotBlank(responseMap.get(Constants.AMOUNT))) {
				transaction.setAmount(responseMap.get(Constants.AMOUNT));
			}

			if (StringUtils.isNotBlank(responseMap.get(Constants.RET_REF_NO))) {
				transaction.setRetRefNo(responseMap.get(Constants.RET_REF_NO));
			}

			if (StringUtils.isNotBlank(responseMap.get(Constants.TERMINAL_ID))) {
				transaction.setTerminalId(responseMap.get(Constants.TERMINAL_ID));
			}

			if (StringUtils.isNotBlank(responseMap.get(Constants.REFUND_AMOUNT))) {
				transaction.setRefundAmount(responseMap.get(Constants.REFUND_AMOUNT));
			}

			if (StringUtils.isNotBlank(responseMap.get(Constants.MERCHANT_ID))) {
				transaction.setMerchantId(responseMap.get(Constants.MERCHANT_ID));
			}

			if (StringUtils.isNotBlank(responseMap.get(Constants.TXN_REF_NO))) {
				transaction.setTxnRefNo(responseMap.get(Constants.TXN_REF_NO));
			}

			if (StringUtils.isNotBlank(responseMap.get(Constants.BANK_ID))) {
				transaction.setBankId(responseMap.get(Constants.BANK_ID));
			}
		}
		return transaction;
	}

	public TransactionConverter() {
	}

	public Transaction toVpaTransaction(String vpaResponse, Fields fields) {
		Transaction transaction = new Transaction();
		LinkedHashMap<String, String> decrytedString = null;
		try {
			decrytedString = iSGPayDecryption.isgpayDecrypt(vpaResponse, fields.get(FieldType.TXN_KEY.getName()));
		} catch (Exception e) {
			logger.error("Exception while decrypting vpa valiation response" + e);
		}
		
		if (StringUtils.isNotBlank(decrytedString.get(Constants.RESPONSE_CODE))) {
			transaction.setResponseCode(decrytedString.get(Constants.RESPONSE_CODE));
		}
		if (StringUtils.isNotBlank(decrytedString.get(Constants.MESSAGE))) {
			transaction.setMessage(decrytedString.get(Constants.MESSAGE));
		}
		return transaction;
		
	}

}
