package com.paymentgateway.payout;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EmptyStackException;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;

import com.paymentgateway.commons.util.TransactionManager;

public class PayoutFonePaisaTest {

	static String api_key = "ec9588d7-a02f-4f66-ac6f-313c918334cd";
	static String salt = "f4c662dbbd4440337f8219ea63c7fb4699603769";
	private static Stack<MessageDigest> stack = new Stack<MessageDigest>();

	public static void main(String[] args) {

		// TO GET BALANCE
		// String balanceRequest = getBalance();
		// System.out.println("balanceRequest == " + balanceRequest);

		
		// FOR PAYMENT REUEST
		String merchant_reference_number = TransactionManager.getNewTransactionId();
		String amount = "50";
		String upi_id = "shaiwal16@okhdfcbank";
		String account_name = "Shiva Bhati";
		String account_number = "418401000079";
		String ifsc_code = "BARB0KARAWA";
		String bank_name = ifsc_code.substring(0, 4);
		//String bank_branch = "Main";
		String transfer_type = "IMPS";
		
		Map<String, String> payReqMap = new TreeMap<String, String>();

		if (transfer_type.equalsIgnoreCase("UPI")) {
			
			payReqMap.put("merchant_reference_number", merchant_reference_number);
			payReqMap.put("amount", amount);
			payReqMap.put("upi_id", upi_id);
			payReqMap.put("account_name", account_name);
			payReqMap.put("api_key", api_key);
		}
		
		else {
			payReqMap.put("merchant_reference_number", merchant_reference_number);
			payReqMap.put("amount", amount);
			payReqMap.put("api_key", api_key);
			payReqMap.put("transfer_type", transfer_type);
			payReqMap.put("account_name", account_name);
			payReqMap.put("account_number", account_number);
			payReqMap.put("ifsc_code", ifsc_code);
			payReqMap.put("bank_name", bank_name);
			//payReqMap.put("bank_branch", bank_branch);
		}

		String payReq = paymentReq(payReqMap);
		//System.out.println(payReq);
		
		Map<String, String> statusReqMap = new TreeMap<String, String>();
		statusReqMap.put("merchant_reference_number", "1003020902114102");
		statusReqMap.put("api_key", api_key);
		
		String statusReq = statusReq(statusReqMap);
		System.out.println(statusReq);
		
		
	}

	public static String getBalance() {

		try {
			StringBuilder sb = new StringBuilder();
			sb.append(salt);
			sb.append("|");
			sb.append(api_key);

			String hash = getHash(sb.toString());

			JSONObject requestJson = new JSONObject();
			requestJson.put("api_key", api_key);
			requestJson.put("hash", hash);

			return requestJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String paymentReq(Map<String, String> payReqMap) {

		try {

			StringBuilder sb = new StringBuilder();
			sb.append(salt);
			sb.append("|");

			for (Map.Entry<String, String> entry : payReqMap.entrySet()) {
				sb.append(entry.getValue());
				sb.append("|");
			}

			String req = method(sb.toString());
			String hash = getHash(req);
			payReqMap.put("hash", hash);

			JSONObject requestJson = new JSONObject();

			for (Map.Entry<String, String> entry : payReqMap.entrySet()) {
				requestJson.put(entry.getKey(), entry.getValue());
			}

			return requestJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String statusReq(Map<String, String> payReqMap) {

		try {

			StringBuilder sb = new StringBuilder();
			sb.append(salt);
			sb.append("|");

			for (Map.Entry<String, String> entry : payReqMap.entrySet()) {
				sb.append(entry.getValue());
				sb.append("|");
			}

			String req = method(sb.toString());
			String hash = getHash(req);
			payReqMap.put("hash", hash);

			JSONObject requestJson = new JSONObject();

			for (Map.Entry<String, String> entry : payReqMap.entrySet()) {
				requestJson.put(entry.getKey(), entry.getValue());
			}

			return requestJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static MessageDigest provide() {
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

	public static void consume(MessageDigest digest) {
		stack.push(digest);
	}

	public static String getHash(String input) {
		String response = null;

		MessageDigest messageDigest = provide();
		messageDigest.update(input.getBytes());
		consume(messageDigest);

		response = new String(Hex.encodeHex(messageDigest.digest()));

		return response.toUpperCase();
	}// getSHA256Hex()

	public static String method(String str) {
		str = str.substring(0, str.length() - 1);
		return str;
	}

}
