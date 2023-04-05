package com.paymentgateway.commons.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EmptyStackException;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Hex;


public class ChecksumUtils {

	private static Stack<MessageDigest> stack = new Stack<MessageDigest>();
	private final static String separator = "~";
	private final static String equator = "=";
	private final static String hashingAlgo = "SHA-256";

	// Hash calculation from request map
	public static String generateCheckSum(Map<String, String> parameters,
			String secretKey) throws NoSuchAlgorithmException {
		Map<String, String> treeMap = new TreeMap<String, String>(parameters);

		StringBuilder allFields = new StringBuilder();
		for (String key : treeMap.keySet()) {
			allFields.append(separator);
			allFields.append(key);
			allFields.append(equator);
			allFields.append(treeMap.get(key));
		}

		allFields.deleteCharAt(0); // Remove first FIELD_SEPARATOR
		allFields.append(secretKey);
		// Calculate hash
		return getHash(allFields.toString());
	}
	public static String getString(Map<String, String> parameters) {
		Map<String, String> treeMap = new TreeMap<String, String>(parameters);

		StringBuilder allFields = new StringBuilder();
		for (String key : treeMap.keySet()) {
			allFields.append(separator);
			allFields.append(key);
			allFields.append(equator);
			allFields.append(treeMap.get(key));
		}

		allFields.deleteCharAt(0);
		return allFields.toString();
	}
	

	// Response hash validation
	public static boolean validateResponseChecksum(
			Map<String, String> responseParameters, String secretKey,
			String responseHash) throws NoSuchAlgorithmException {
		boolean flag = false;
		String generatedHash = generateCheckSum(responseParameters, secretKey);
		if (generatedHash.equals(responseHash)) {
			flag = true;
		}
		return flag;
	}

	// Generate hash from the supplied string
	public static String getHash(String input) throws NoSuchAlgorithmException {
		String response = null;

		MessageDigest messageDigest = provide();
		messageDigest.update(input.getBytes());
		consume(messageDigest);

		response = new String(Hex.encodeHex(messageDigest.digest()));

		return response.toUpperCase();
	}// getSHA256Hex()

	private static MessageDigest provide() throws NoSuchAlgorithmException {
		MessageDigest digest = null;

		try {
			digest = stack.pop();
		} catch (EmptyStackException emptyStackException) {
			digest = MessageDigest.getInstance(hashingAlgo);
		}
		return digest;
	}

	private static void consume(MessageDigest digest) {
		stack.push(digest);
	}
}
