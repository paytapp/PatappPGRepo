package com.paymentgateway.payout.fonePaisa;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EmptyStackException;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionManager;

@Service
public class FonePaisaRequestCreator {
	
	private static Logger logger = LoggerFactory.getLogger(FonePaisaRequestCreator.class);
	private static Stack<MessageDigest> stack = new Stack<MessageDigest>();

	public String createTransactionRequest(Fields fields, JSONObject adfFields) {
		try{
			
			String txnType = fields.get(FieldType.TXNTYPE.getName());
			String txnId = fields.get(FieldType.TXN_ID.getName());
			String bankName = fields.get(FieldType.BANK_NAME.getName());
			
			if(StringUtils.isBlank(bankName)){
				bankName = fields.get(FieldType.IFSC_CODE.getName()).substring(0,4);
			}
			
			if(StringUtils.isBlank(txnId)){
				txnId = TransactionManager.getNewTransactionId();
				fields.put(FieldType.TXN_ID.getName(),txnId);
			}
			
			JSONObject requestJson = new JSONObject();
			requestJson.put("merchant_reference_number", fields.get(FieldType.TXN_ID.getName()));
			requestJson.put("amount", fields.get(FieldType.AMOUNT.getName()));
			requestJson.put("api_key", fields.get(FieldType.MERCHANT_ID.getName()));
			
			if (txnType.equalsIgnoreCase("UPI")) {
				requestJson.put("upi_id", fields.get(FieldType.PAYER_ADDRESS.getName()));
				requestJson.put("account_name", fields.get(FieldType.PAYER_NAME.getName()));
			}else {
				requestJson.put("transfer_type", fields.get(FieldType.TXNTYPE.getName()));
				requestJson.put("account_name", fields.get(FieldType.BENE_NAME.getName()));
				requestJson.put("account_number", fields.get(FieldType.BENE_ACCOUNT_NO.getName()));
				requestJson.put("ifsc_code", fields.get(FieldType.IFSC_CODE.getName()));
				requestJson.put("bank_name", bankName);
			}
			
			requestJson.put("hash", getHash(requestJson,fields));
			
			logger.info("Fone Paisa Transaction Request "+requestJson);
			
			return requestJson.toString();
			
		}catch (Exception e) {
			logger.info("Exceptionn in createTransactionRequest() ",e);
		}
		return null;
	}
	
	public String createStatusEnqRequest(Fields fields, JSONObject adfFields) {
		try{
			
			JSONObject requestJson = new JSONObject();
			requestJson.put("merchant_reference_number", fields.get(FieldType.TXN_ID.getName()));
			requestJson.put("api_key", fields.get(FieldType.MERCHANT_ID.getName()));
			requestJson.put("hash", getHash(requestJson,fields));
			
			logger.info("Fone Paisa Status Enquiry Transaction Request "+requestJson);
			
			return requestJson.toString();
			
		}catch (Exception e) {
			logger.info("Exceptionn in createStatusEnqRequest() ",e);
		}
		return null;
	}
	

	private String getHash(JSONObject requestJson, Fields fields) throws SystemException {
		
		Map<String, String> reqMap = new TreeMap<String, String>();
		
		for(String key : requestJson.keySet()){
			reqMap.put(key, requestJson.getString(key));
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(fields.get(FieldType.TXN_KEY.getName()));
		sb.append("|");

		for (Map.Entry<String, String> entry : reqMap.entrySet()) {
			sb.append(entry.getValue());
			sb.append("|");
		}

		String hashString = sb.toString().substring(0, sb.toString().length() - 1);
		String hash = getHash(hashString);
		
		return hash.toUpperCase();
	}
	
	private MessageDigest provide() {
		MessageDigest digest = null;
		try {
			digest = stack.pop();
		} catch (EmptyStackException emptyStackException) {
			try {
				digest = MessageDigest.getInstance("SHA-512");
			} catch (NoSuchAlgorithmException noSuchAlgorithmException) {

			}
		}

		return digest;
	}

	private void consume(MessageDigest digest) {
		stack.push(digest);
	}

	private String getHash(String input) {
		String response = null;

		MessageDigest messageDigest = provide();
		messageDigest.update(input.getBytes());
		consume(messageDigest);

		response = new String(Hex.encodeHex(messageDigest.digest()));

		return response.toUpperCase();
	}// getSHA256Hex()

	
	

}
