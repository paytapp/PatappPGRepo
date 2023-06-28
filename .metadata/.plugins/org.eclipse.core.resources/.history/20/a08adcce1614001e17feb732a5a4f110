package com.paymentgateway.commons.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

/**
 * @author Amitosh
 */
@Service
public class SmsSender {

	@Autowired
	private Fields field;

	private static Logger logger = LoggerFactory.getLogger(SmsSender.class.getName());

	
	public String sendSMS(String mobile, String message) throws IOException {

		try {
			String response = smsWithTwilio("+91"+mobile, message);
			return response;
		} catch (Exception e) {
			logger.error("Exception in sending SMS", e);
			return "Error in sending SMS";
		}

	}
	
	public String sendSMSMsg91(String mobile, String message) throws IOException {

		logger.info("Message == " + message);
		String authKey = PropertiesManager.propertiesMap.get(Constants.AUTH_KEY.getValue());
		String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_URL.getValue());
		String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID.getValue());
		String country = PropertiesManager.propertiesMap.get(Constants.SMS_TO_COUNTRY.getValue());
		String response = "";
		if (mobile.length() == 11) {
			if (mobile.charAt(0) == 0) {
				mobile = mobile.substring(1, 10);
			} else {
				return null;
			}
		}
		try {
			URL url = new URL(smsUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.setRequestMethod("POST");
			conn.setRequestProperty("authkey", authKey);
			conn.setRequestProperty("Content-Type", "application/json");

			JSONObject json = new JSONObject();
			json.put("sender", senderId);
			json.put("route", "4");
			json.put("country", country);
			JSONArray smsArray = new JSONArray();
			JSONObject object = new JSONObject();
			smsArray.put(object);
			object.put("message", message);
			JSONArray smsToArray = new JSONArray();
			smsToArray.put(mobile);
			object.put("to", smsToArray);
			json.put("sms", smsArray);

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(json.toString());
			wr.flush();
			InputStream is = conn.getInputStream();

			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(is));
			String decodedString;

			while ((decodedString = bufferedreader.readLine()) != null) {
				response = response + decodedString;
			}
			bufferedreader.close();

			JSONObject serverResponse = new JSONObject(response);
			if (serverResponse.has("type")) {
				logger.info("Normal sms send");
				return serverResponse.getString("type");
			} else {
				return null;
			}
		} catch (Exception exception) {
			logger.error("Exception : ", exception);
			return null;
		}
	}

	public String sendSMSByInnvisSolution(String mobile, String message) throws IOException {
		String user = PropertiesManager.propertiesMap.get(Constants.USER_INNUVISSOLUTION.getValue());
		String password = PropertiesManager.propertiesMap.get(Constants.PASSWORD_INNUVISSOLUTION.getValue());
		String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVISSOLUTION_URL.getValue());
		String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID_INNUVISSOLUTION.getValue());
		String channel = PropertiesManager.propertiesMap.get(Constants.CHANNEL_INNUVISSOLUTION.getValue());
		String DCS = PropertiesManager.propertiesMap.get(Constants.DCS.getValue());
		String flashsms = PropertiesManager.propertiesMap.get(Constants.FLASHSMS.getValue());
		String peid = PropertiesManager.propertiesMap.get(Constants.PEID.getValue());
		String response = "";
		if (mobile.length() == 11) {
			if (mobile.charAt(0) == 0) {
				mobile = mobile.substring(1, 10);
			} else {
				return null;
			}
		}

		StringBuilder str = new StringBuilder();
		str.append(smsUrl);
		str.append("user=");
		str.append(user);
		str.append("&password=");
		str.append(password);
		str.append("&senderid=");
		str.append(senderId);
		str.append("&channel=");
		str.append(channel);
		str.append("&DCS=");
		str.append(DCS);
		str.append("&flashsms=");
		str.append(flashsms);
		str.append("&number=");
		str.append(mobile);
		message = message.trim();
		message = message.replaceAll("\\s", "%20");
		str.append("&text=");
		str.append(message);
		str.append("&route=");
		str.append("2");
		str.append("&peid=");
		str.append(peid);
		try {
			// System.out.println("string : " + str);
			URL url = new URL(str.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("GET");

			StringBuilder str1 = new StringBuilder();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			// System.out.print(rd);

			String line;
			while ((line = rd.readLine()) != null) {
				str1.append(line);
			}

			rd.close();
			response = str1.toString();
			// System.out.println(response);

			JSONObject serverResponse = new JSONObject(response);
			logger.info("Server Response = " + maskFields(serverResponse.toString()));
			logger.info("Message = " + serverResponse.get("ErrorMessage"));
			// System.out.println(serverResponse);

			if (serverResponse.getString("ErrorCode").equalsIgnoreCase("000")) {
				logger.info("SMS send by InnvisSolution");
				return serverResponse.getString("ErrorCode");
			} else {
				return null;
			}

		} catch (Exception exception) {
			logger.error("Exception : ", exception);
			return null;
		}
	}

	public boolean healthCheckUpSMS(String mobile, String msg) throws Exception {

		String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
		if (StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
			String user = PropertiesManager.propertiesMap.get(Constants.USER_INNUVISSOLUTION.getValue());
			String password = PropertiesManager.propertiesMap.get(Constants.PASSWORD_INNUVISSOLUTION.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVISSOLUTION_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID_INNUVISSOLUTION.getValue());
			String channel = PropertiesManager.propertiesMap.get(Constants.CHANNEL_INNUVISSOLUTION.getValue());
			String DCS = PropertiesManager.propertiesMap.get(Constants.DCS.getValue());
			String flashsms = PropertiesManager.propertiesMap.get(Constants.FLASHSMS.getValue());
			String peid = PropertiesManager.propertiesMap.get(Constants.PEID.getValue());
			String response = "";

			logger.info("Inside InnuvisSolution SMS");
			String message = msg;

			if (StringUtils.isBlank(mobile)) {
				return false;
			}

			if (mobile.length() == 11) {
				if (mobile.charAt(0) == 0) {
					mobile = mobile.substring(1, 10);
				} else {
					return false;
				}
			}

			StringBuilder str = new StringBuilder();
			str.append(smsUrl);
			str.append("user=");
			str.append(user);
			str.append("&password=");
			str.append(password);
			str.append("&senderid=");
			str.append(senderId);
			str.append("&channel=");
			str.append(channel);
			str.append("&DCS=");
			str.append(DCS);
			str.append("&flashsms=");
			str.append(flashsms);
			str.append("&number=");
			str.append(mobile);
			message = message.trim();
			message = message.replaceAll("\\s", "%20");
			str.append("&text=");
			str.append(message);
			str.append("&route=");
			str.append("2");
			str.append("&peid=");
			str.append(peid);
			try {
				// System.out.println("string : " + str);
				URL url = new URL(str.toString());
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();

				conn.setRequestMethod("GET");

				StringBuilder str1 = new StringBuilder();
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				// System.out.print(rd);

				String line;
				while ((line = rd.readLine()) != null) {
					str1.append(line);
				}

				rd.close();
				response = str1.toString();
				// System.out.println(response);

				JSONObject serverResponse = new JSONObject(response);
				logger.info("Server Response = " + serverResponse.toString());
				logger.info("Message = " + serverResponse.get("ErrorMessage"));
				// System.out.println(serverResponse);
				if (serverResponse.getString("ErrorCode").equalsIgnoreCase("000")) {
					logger.info("SMS send by InnuvisSolution");
					return true;
				} else {
					return false;
				}

			} catch (Exception exception) {
				logger.error("Exception Cought in sending SMS : ", exception);
				return false;
			}
		} else {
			String authKey = PropertiesManager.propertiesMap.get(Constants.AUTH_KEY.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID.getValue());
			String country = PropertiesManager.propertiesMap.get(Constants.SMS_TO_COUNTRY.getValue());
			String response = "";

			String message = msg;
			if (mobile.length() == 11) {
				if (mobile.charAt(0) == 0) {
					mobile = mobile.substring(1, 10);
				} else {
					return false;
				}
			}
			try {
				URL url = new URL(smsUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();

				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);

				conn.setRequestMethod("POST");
				conn.setRequestProperty("authkey", authKey);
				conn.setRequestProperty("Content-Type", "application/json");

				JSONObject json = new JSONObject();
				json.put("sender", senderId);
				json.put("route", "4");
				json.put("country", country);
				JSONArray smsArray = new JSONArray();
				JSONObject object = new JSONObject();
				smsArray.put(object);
				object.put("message", message);
				JSONArray smsToArray = new JSONArray();
				smsToArray.put(mobile);
				object.put("to", smsToArray);
				json.put("sms", smsArray);

				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(json.toString());
				wr.flush();
				InputStream is = conn.getInputStream();

				BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(is));
				String decodedString;

				while ((decodedString = bufferedreader.readLine()) != null) {
					response = response + decodedString;
				}
				bufferedreader.close();

				JSONObject serverResponse = new JSONObject(response);
				if (serverResponse.has("type")) {
					if (serverResponse.getString("type").equalsIgnoreCase("success")) {
						return true;
					}
				} else {
					return false;
				}
			} catch (Exception exception) {
				logger.error("Exception : ", exception);
				return false;
			}
			return false;
		}
	}

	public String maskFields(String request) {
		JSONObject jsonReq = new JSONObject(request);
		JSONArray lang = jsonReq.getJSONArray("MessageData");
		JSONObject firstSport = lang.getJSONObject(0);
		for (String key : firstSport.keySet()) {
			if (key.equalsIgnoreCase("Number")) {
				String reqValue = field.fieldMask(firstSport.getString(key));
				firstSport.put("Number", reqValue);
			}
		}
		return jsonReq.toString();
	}

	public String sendSMSByTwilio(String mobile, String message) throws IOException {

		try {
			String response = smsWithTwilio(mobile, message);
			return response;
		} catch (Exception e) {
			logger.error("Exception in sending SMS", e);
			return "Error in sending SMS";
		}

	}

	private static String smsWithTwilio(String smsTo, String body) {
		String response;
		try {

			String ACCOUNT_SID = PropertiesManager.propertiesMap.get("TwilioAccountSID");
			String AUTH_TOKEN = PropertiesManager.propertiesMap.get("TwilioAuthToken");
			String smsFrom = PropertiesManager.propertiesMap.get("TwilioSenderNum");

			Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
			Message message = Message.creator(new com.twilio.type.PhoneNumber(smsTo), // SMS send To
					new com.twilio.type.PhoneNumber(smsFrom), // SMS send From
					body).create();

			logger.info(message.toString());
			if (StringUtils.isNotBlank(message.getStatus().toString())) {
				response = message.getStatus().toString(); // EXPECTED RESPONSE: queued/failed/sent/delivered/undelivered
				StringBuilder log = new StringBuilder();
				log.append("Message >>> status: ");
				log.append(message.getStatus());
				log.append(" | From: ");
				log.append(smsFrom);
				log.append(" | To: ");
				log.append(smsTo);
				log.append(" | Body: ");
				log.append(body);
				log.append(" | sid: ");
				log.append(message.getSid());
				logger.info("SMS Sent : " + log.toString());
			} else {
				response = "Something went wrong while sending sms.";
			}
		} catch (Exception e) {
			response = "Something went wrong while sending sms.";
			logger.error(response, e);
		}
		return response;
	}
}