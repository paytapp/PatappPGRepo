package com.paymentgateway.apexPay;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EmptyStackException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.ChecksumUtils;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TransactionType;

@Service("apexPayTransactionConverter")
public class TransactionConverter {

	private static final Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());
	private static Stack<MessageDigest> stack = new Stack<MessageDigest>();
	public static final String ALGO = "AES";
	// private static String key = null;
//	private static Key keyObj = null;

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
		}
		return request.toString();

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {
		String encData = null;

		try {

			String encKey = transaction.getEncKey();

			Map<String, String> parameters = new LinkedHashMap<String, String>();
			parameters.put("AMOUNT", transaction.getAmount());

			if (StringUtils.isNotBlank(fields.get(FieldType.PAYMENT_TYPE.getName()))
					&& (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
							|| fields.get(FieldType.PAYMENT_TYPE.getName())
									.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode()))) {

				parameters.put("CARD_EXP_DT", transaction.getExpiryDate());
				parameters.put("CARD_NUMBER", transaction.getCardNumber());
				parameters.put("CVV", transaction.getCavv());
				parameters.put("PAYMENT_TYPE", "CARD");
				parameters.put("MOP_TYPE", transaction.getMopType());
			}

			else if (StringUtils.isNotBlank(fields.get(FieldType.PAYMENT_TYPE.getName()))
					&& (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode()))) {

				parameters.put("PAYMENT_TYPE", "UP");
				parameters.put("PAYER_ADDRESS", transaction.getPayerAddress());
				parameters.put("MOP_TYPE", transaction.getMopType());

			} else if (StringUtils.isNotBlank(fields.get(FieldType.PAYMENT_TYPE.getName())) && (fields
					.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.NET_BANKING.getCode()))) {
				parameters.put("PAYMENT_TYPE", "NB");
				parameters.put("MOP_TYPE", ApexPayBankCode.getBankCode(transaction.getMopType()));
			}

			else if (StringUtils.isNotBlank(fields.get(FieldType.PAYMENT_TYPE.getName()))
					&& (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.WALLET.getCode()))) {
				parameters.put("PAYMENT_TYPE", "WL");
				parameters.put("MOP_TYPE", transaction.getMopType());
			}

			parameters.put("CURRENCY_CODE", transaction.getCurrencyCode());

			if (StringUtils.isNotBlank(transaction.getCustEmail())) {
				parameters.put("CUST_EMAIL", transaction.getCustEmail());
			}

			if (StringUtils.isNotBlank(transaction.getCustName())) {
				parameters.put("CUST_NAME", transaction.getCustName());
			}
			if (StringUtils.isNotBlank(transaction.getCustPhone())) {
				parameters.put("CUST_PHONE", transaction.getCustPhone());
			}

			// parameters.put("CARD_HOLDER_NAME", transaction.getCardHolderName());
			parameters.put("ORDER_ID", transaction.getOrderId());
			parameters.put("PAY_ID", transaction.getPayId());
			parameters.put("RETURN_URL", transaction.getRedirectUrl());
			String data = ChecksumUtils.getString(parameters);
			logger.info("ApexPay request data without hash >> " + data);
			String req = data + "~HASH=" + ChecksumUtils.generateCheckSum(parameters, transaction.getSaltKey());
			logger.info("ApexPay request data with hash >> " + req);
			encData = encrypt(encKey, encKey.substring(0, 16), req);
			logger.info("ApexPay encrypted data >>" + encData);
		}

		catch (

		Exception e) {
			logger.error("Exception in generating APEXPAY sale request ", e);
			return null;
		}
		return encData;
	}

	public static String getHash(String input) {
		String response = null;

		MessageDigest messageDigest = provide();
		messageDigest.update(input.getBytes());
		consume(messageDigest);

		response = new String(Hex.encodeHex(messageDigest.digest()));

		return response.toUpperCase();
	}

	public static MessageDigest provide() {
		MessageDigest digest = null;
		try {
			digest = stack.pop();
		} catch (EmptyStackException emptyStackException) {
			try {
				digest = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException noSuchAlgorithmException) {

			}
		}

		return digest;
	}

	public static void consume(MessageDigest digest) {
		stack.push(digest);
	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {

		try {

			JSONObject request = new JSONObject();

			request.put("REFUND_FLAG", "C");
			request.put("PAY_ID", transaction.getPayId());
			request.put("ORDER_ID", transaction.getOrderId());
			request.put("AMOUNT", transaction.getAmount());
			request.put("TXNTYPE", transaction.getTxnType());
			request.put("REFUND_ORDER_ID", transaction.getRefundOrderId());
			request.put("PG_REF_NUM", transaction.getPgRefNum());
			request.put("CURRENCY_CODE", "356");
			request.put("HASH", transaction.getHaskey());

			logger.info("APEXPAY refund request  =  " + request.toString());

			return request.toString();
		}

		catch (Exception e) {
			logger.error("Exception in generating APEXPAY refund request", e);
		}
		return null;

	}

	public Transaction toTransaction(String response, String txnType) {
		Transaction transaction = new Transaction();
		JSONObject res = new JSONObject(response);
		if (res.has("RRN") && !res.isNull("RRN")) {
			transaction.setRrn(res.getString("RRN"));
		}
		if (res.has("ACQ_ID") && !res.isNull("ACQ_ID")) {
			transaction.setAcqId(res.getString("ACQ_ID"));
		}
		if (res.has("RESPONSE_CODE") && !res.isNull("RESPONSE_CODE")) {
			transaction.setResponseCode(res.getString("RESPONSE_CODE"));
		}
		if (res.has("STATUS") && !res.isNull("STATUS")) {
			transaction.setStatus(res.getString("STATUS"));
		}
		if (res.has("RESPONSE_MESSAGE") && !res.isNull("RESPONSE_MESSAGE")) {
			transaction.setResponseMessage(res.getString("RESPONSE_MESSAGE"));
		}
		return transaction;

	}

	public TransactionConverter() {

	}

	public static String encrypt(String key, String initVector, String value) throws IllegalBlockSizeException,
			BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
			NoSuchPaddingException, UnsupportedEncodingException {
		IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		cipher.init(1, skeySpec, iv);
		byte[] encrypted = cipher.doFinal(value.getBytes());
		System.out.println("encrypted string: " + Base64.encodeBase64String(encrypted));
		return Base64.encodeBase64String(encrypted);
	}

}
