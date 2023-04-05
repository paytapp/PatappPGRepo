package com.paymentgateway.api;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Hex;

public class QRHashGen {

	private static Stack<MessageDigest> stack = new Stack<MessageDigest>();
	private static String salt = "42bd82b13dde40ce";

	public static void main(String[] args) {

		Map<String, String> paramMap = new HashMap<String, String>();

		paramMap.put("PAY_ID", "1141530207153042");
		paramMap.put("ORDER_ID", "ORD-20230207001");
		paramMap.put("AMOUNT", "1000");
		paramMap.put("TXNTYPE", "SALE");
		paramMap.put("CURRENCY_CODE", "356");
		paramMap.put("RETURN_URL", "https://uat.letzpay.com/pgui/jsp/response"); // Your return URL here
		paramMap.put("CUST_NAME", "Demo Merchant");
		paramMap.put("CUST_PHONE", "9988776655");
		paramMap.put("CUST_EMAIL", "test@testmail.com");
		
		try {

			String hashString = genHashReqString(paramMap, salt);
			String hash = getHash(hashString);

			System.out.println("Generated Hash = " + hash);

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public static String genHashReqString(Map<String, String> parameters, String secretKey)
			throws NoSuchAlgorithmException {
		Map<String, String> treeMap = new TreeMap<String, String>(parameters);

		StringBuilder allFields = new StringBuilder();
		for (String key : treeMap.keySet()) {
			allFields.append("~");
			allFields.append(key);
			allFields.append("=");
			allFields.append(treeMap.get(key));
		}

		allFields.deleteCharAt(0); // Remove first FIELD_SEPARATOR
		allFields.append(secretKey);

		return allFields.toString();
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

	public static String getHash(String input) {
		String response = null;

		MessageDigest messageDigest = provide();
		messageDigest.update(input.getBytes());
		consume(messageDigest);

		response = new String(Hex.encodeHex(messageDigest.digest()));

		return response.toUpperCase();
	}

}
