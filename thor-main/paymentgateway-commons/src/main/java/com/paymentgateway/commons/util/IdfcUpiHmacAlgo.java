package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import antlr.collections.List;

@Service("idfcUpiHmacAlgo")
public class IdfcUpiHmacAlgo {

	private static Logger logger = LoggerFactory.getLogger(IdfcUpiHmacAlgo.class.getName());

	private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

	private static final String encryptedFlag = "3";

	public String verifyHMACvalue(String message, String reqKey) throws Exception {
		String validHmac = "";
		try {

			validHmac = verifyHmacValue(message, reqKey);
			logger.info("Hmac generated string in  verifyHMACvalue methord:  " + validHmac);
		} catch (Exception ex) {
			logger.error("Exception in decryption " , ex);
			throw new Exception("Invalid input String");
		}
		return validHmac;
	}

	public String verifyHmacValue(String message, String reqKey) throws Exception {
		boolean validHmac = false;
		String hashedData = "";
		try {

			hashedData = calculateRFC2104HMAC(message, reqKey);
			logger.info("Hmac generated string  in verifyHmacValue method:  " + hashedData);
		} catch (RuntimeException ex) {
			logger.error("Exception in verifyHmacValue " , ex);
			throw new Exception("Invalid input String");
		} catch (Exception ex) {
			logger.error("Exception in verifyHmacValue " , ex);
			throw new Exception("Invalid input String");
		}
		return hashedData;
	}

	public String calculateRFC2104HMAC(String data, String key) throws Exception {

		SecretKeySpec signingKey;
		String result = "";

		try {
			JSONObject json = new JSONObject(data);
			if (json.has("HMAC")) {
				json.remove("HMAC");
				Map<String, Object> retMap = new HashMap<String, Object>();
				if (json != JSONObject.NULL) {
					retMap = toMap(json);
				}
				Map<String, Object> treeMap = new TreeMap<String, Object>();
				treeMap.putAll(retMap);
				logger.info("Hmac generated string  in calculateRFC2104HMAC method:  " + treeMap.toString());
				String hmac = json.toString();
				key = json.getString("TxnId");
				signingKey = new SecretKeySpec(key.getBytes("ISO-8859-1"), HMAC_SHA256_ALGORITHM);
				Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
				mac.init(signingKey);
				result = toHexString(mac.doFinal(treeMap.toString().getBytes("ISO-8859-1")));
			} else {
				result = "HMAC field is missing in the Request";
			}

		} catch (Exception e) {
			logger.error("Exception in verifyHmacValue " , e);
			throw new Exception("Invalid input String");
		}
		return result;

	}

	@SuppressWarnings("resource")
	private String toHexString(byte[] bytes) {

		Formatter formatter = new Formatter();

		for (byte b : bytes) {
			formatter.format("%02x", b);
		}

		return formatter.toString();
	}

	public Map<String, Object> toMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();

		Iterator<String> keysItr = object.keys();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}

	public List toList(JSONArray array) throws JSONException {
		List list = (List) new ArrayList();
		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}

}
