package com.paymentgateway.pg.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.ipint.Constants;

@Service
public class IpintAggregatorMerchantService {

	Logger logger = LoggerFactory.getLogger(IpintAggregatorMerchantService.class.getName());

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA384";
	private static final String TXN_KEY = "UA9TwbLc9ybHNKq79wAkSw4R6Eh86E5KZmeKwBrVuP58RE9Rc8umZZFMhVKHuYVQ";
	private static final String PASSWORD = "vNfWcejXGqDGMXLdFbY2zfNYBEU9KNATyMyFkJtnNgWa5BQ6YDEt9Uq1E1N97QfY";

	public Map<String, String> aggregatorMerchnatProcessor(Fields fields) throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		String request = ipintAggregatorMerchantService(fields);
		requestMap.put("Request", request);
		return requestMap;
	}

	public String ipintAggregatorMerchantService(Fields fields) throws Exception {
//		HashMap<String, String> parameters = new HashMap<String, String>();
//		parameters = jsonParameters(fields);
		JSONObject jsonParam = new JSONObject();
		jsonParam = jsonParameters(fields);
		String response = sendSignedRequest(jsonParam, "/aggregator/merchants", "POST", fields);
		return response;

	}

	private JSONObject jsonParameters(Fields fields) {
		JSONObject mainJson = new JSONObject();
		JSONObject businessAddressJson = new JSONObject();
		JSONObject contactJson = new JSONObject();
		JSONObject businessOwnerJson = new JSONObject();

		mainJson.put("legal_name_of_business", fields.get("legal_name_of_business"));
		mainJson.put("business_registration_country", fields.get("business_registration_country"));
		mainJson.put("legal_status_of_business", fields.get("legal_status_of_business"));
		mainJson.put("business_type", fields.get("business_type"));
		mainJson.put("industry", fields.get("industry"));
		mainJson.put("annual_revenue", fields.get("annual_revenue"));
		mainJson.put("trade_name_of_service", fields.get("trade_name_of_service"));
		mainJson.put("business_registration_number", fields.get("business_registration_number"));
		mainJson.put("main_business_activity", fields.get("main_business_activity"));
		mainJson.put("target_website", fields.get("target_website"));
		mainJson.put("date_of_incorporation", fields.get("date_of_incorporation"));
		mainJson.put("expected_maximum_amount_single_transaction",
				fields.get("expected_maximum_amount_single_transaction"));
		mainJson.put("expected_yearly_transaction_volume", fields.get("expected_yearly_transaction_volume"));
		mainJson.put("merchant_tool", fields.get("merchant_tool"));

		businessAddressJson.put("house_number", fields.get("house_number"));
		businessAddressJson.put("street_name1", fields.get("street_name1"));
		businessAddressJson.put("street_name2", fields.get("street_name2"));
		businessAddressJson.put("city", fields.get("city"));
		businessAddressJson.put("state", fields.get("state"));
		businessAddressJson.put("postal_code", fields.get("postal_code"));
		mainJson.put("legally_registered_business_address", businessAddressJson);

		contactJson.put("phone_number", fields.get("phone_number"));
		contactJson.put("country_phone_code", fields.get("country_phone_code"));
		contactJson.put("website", fields.get("website"));
		contactJson.put("support_email", fields.get("support_email"));
		contactJson.put("notification_email", fields.get("notification_email"));
		mainJson.put("contact", contactJson);

		businessOwnerJson.put("first_name", fields.get("first_name"));
		businessOwnerJson.put("last_name", fields.get("last_name"));
		businessOwnerJson.put("date_of_birth", fields.get("date_of_birth"));
		businessOwnerJson.put("relation_with_organization", fields.get("relation_with_organization"));
		businessOwnerJson.put("other_relationship", fields.get("other_relationship"));
		businessOwnerJson.put("email", fields.get("email"));
		businessOwnerJson.put("country_phone_code", fields.get("country_phone_code"));
		businessOwnerJson.put("phone_number", fields.get("phone_number"));
		mainJson.put("business_beneficial_owner", businessOwnerJson);

		return mainJson;
	}

	public String sendSignedRequest(JSONObject parameters, String urlPath, String httpMethod, Fields fields)
			throws Exception {
		String signature = "";
		String signatureMessage = ""; // to be signed
		long nonce = System.currentTimeMillis();
		String baseUrl = PropertiesManager.propertiesMap.get(Constants.STATUS_IPINT_ENQ_URL);
		try {
			logger.info("signature message parameters:" + parameters.toString());
			signatureMessage = "/api/" + nonce + urlPath + parameters.toString();
			logger.info("signature message input:" + signatureMessage);
			signature = getSignature(signatureMessage, PASSWORD);
			logger.info("Signature: " + signature);
		} catch (Exception e) {
			logger.error("Please Ensure Your Secret Key Is Set Up Correctly! ", e);
			System.exit(0);
		}

		URL obj = new URL(baseUrl + urlPath);
		logger.info("url:" + obj.toString());

		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		if (httpMethod != null) {
			con.setRequestMethod(httpMethod);
		}
		// add API_KEY to header content
		con.setRequestProperty("apikey", TXN_KEY);
		con.setRequestProperty("content-type", "application/json");
		con.setRequestProperty("nonce", String.valueOf(nonce));
		con.setRequestProperty("signature", signature);
		con.setDoOutput(true);

		String jsonInputString = parameters.toString();
		try (OutputStream os = con.getOutputStream()) {
			byte[] input = jsonInputString.getBytes("utf-8");
			os.write(input, 0, input.length);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		logger.info("Ipint Aggregator Merchant ID response" + response.toString());
		return response.toString();

	}

	// convert byte array to hex string
	private String bytesToHex(byte[] bytes) {
		final char[] hexArray = "0123456789abcdef".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0, v; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public String getSignature(String data, String key) {
		byte[] hmacSha384 = null;
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(secretKeySpec);
			hmacSha384 = mac.doFinal(data.getBytes());
		} catch (Exception e) {
			throw new RuntimeException("Failed to calculate hmac-sha384", e);
		}
		return bytesToHex(hmacSha384);
	}

}
