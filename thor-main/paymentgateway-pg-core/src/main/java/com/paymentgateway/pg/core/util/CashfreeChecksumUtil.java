package com.paymentgateway.pg.core.util;

import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

@Service
public class CashfreeChecksumUtil {
	private static final String HASH_ALGORITHM = "HmacSHA256";
	private static Logger logger = LoggerFactory.getLogger(CashfreeChecksumUtil.class.getName());
	private static Mac sha256_HMAC_Static;

	public String getHash(JsonObject jsonRequest, String secretKey) {

		String signature = "";
		try {

			Map<String, String> postData = new HashMap<String, String>();

			Set<Map.Entry<String, JsonElement>> entries = jsonRequest.entrySet();
			for (Map.Entry<String, JsonElement> entry : entries) {
				String key = entry.getKey();
				System.out.println(entry.getKey());
				postData.put(key, jsonRequest.get(key).toString().replace("\"", ""));
			}
			/*
			 * for (String key : jsonRequest.keySet()) { postData.put(key,
			 * jsonRequest.get(key).toString().replace("\"", "")); }
			 */
			String data = "";
			SortedSet<String> keys = new TreeSet<String>(postData.keySet());

			for (String key : keys) {
				data = data + key + postData.get(key);
			}

			Mac sha256_HMAC = null;
			if (null != sha256_HMAC_Static) {
				sha256_HMAC = sha256_HMAC_Static;
			} else {
				sha256_HMAC = Mac.getInstance("HmacSHA256");
				sha256_HMAC_Static = sha256_HMAC;
			}

			SecretKeySpec secret_key_spec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key_spec);

			signature = Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(data.getBytes()));
			return signature;
		} catch (Exception e) {

			logger.error("Error in getting hash", e);
		}
		return signature;

	}

	public String checkSaleResponseHash(JSONObject jsonRequest, String secretKey) {

		String signature = "";
		try {

			LinkedHashMap<String, String> postData = new LinkedHashMap<String, String>();

			for (String key : jsonRequest.keySet()) {
				postData.put(key, jsonRequest.get(key).toString().replace("\"", ""));
			}

			StringBuilder data = new StringBuilder();

			data.append(postData.get("orderId"));
			data.append(postData.get("orderAmount"));
			data.append(postData.get("referenceId"));
			data.append(postData.get("txStatus"));
			data.append(postData.get("paymentMode"));
			data.append(postData.get("txMsg"));
			data.append(postData.get("txTime"));

			Mac sha256_HMAC = null;
			if (null != sha256_HMAC_Static) {
				sha256_HMAC = sha256_HMAC_Static;
			} else {
				sha256_HMAC = Mac.getInstance(HASH_ALGORITHM);
				sha256_HMAC_Static = sha256_HMAC;
			}

			SecretKeySpec secret_key_spec = new SecretKeySpec(secretKey.getBytes(), HASH_ALGORITHM);
			sha256_HMAC.init(secret_key_spec);

			signature = Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(data.toString().getBytes()));
		} catch (Exception e) {

			logger.error("Error in getting sale response hash", e);
		}
		return signature;

	}

	public String checkUpiQrResponseHash(JSONObject jsonRequest, String secretKey) {

		String signature = "";
		try {

			LinkedHashMap<String, String> postData = new LinkedHashMap<String, String>();

			for (String key : jsonRequest.keySet()) {
				postData.put(key, jsonRequest.get(key).toString().replace("\"", ""));
			}
			Map<String, String> treeMap = new TreeMap<String, String>(postData);

			StringBuilder data = new StringBuilder();

			for (String key : treeMap.keySet()) {
				data.append(postData.get(key));
			}

			String secret = secretKey;
			String message = data.toString();
			Mac sha256_HMAC = Mac.getInstance(HASH_ALGORITHM);
			SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), HASH_ALGORITHM);
			sha256_HMAC.init(secret_key);
			signature = Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(message.getBytes()));
		} catch (Exception e) {

			logger.error("Error in getting sale response hash", e);
		}
		return signature;

	}

	public boolean verifyingPayoutChecksum(Fields fields) {
		logger.info("inside verifyingPayoutChecksum()");
		
		try {
			
			String secretKey = fields.get(FieldType.ADF5.getName());
			String merchantsignature = fields.get("signature");
			
			LinkedHashMap<String, String> postData = new LinkedHashMap<String, String>();

			for (String key : fields.keySet()) {
				postData.put(key, fields.get(key));
			}
			postData.remove(FieldType.PAY_ID.getName());
			postData.remove(FieldType.ADF4.getName());
			postData.remove(FieldType.ADF5.getName());
			postData.remove("signature");
			
			Map<String, String> treeMap = new TreeMap<String, String>(postData);

			StringBuilder data = new StringBuilder();

			for (String key : treeMap.keySet()) {
				data.append(postData.get(key));
			}

			String secret = secretKey;
			String message = data.toString();
			Mac sha256_HMAC = Mac.getInstance(HASH_ALGORITHM);
			SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), HASH_ALGORITHM);
			sha256_HMAC.init(secret_key);
			String signature = Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(message.getBytes()));
			
			if(signature.equals(merchantsignature)){
				return true;
			}
			
			logger.info("Calculated Signature "+signature+" recived signature "+fields.get("signature"));
			
		} catch (Exception e) {

			logger.error("Exception in verifyingPayoutChecksum() ", e);
		}
		return false;
	}

}
