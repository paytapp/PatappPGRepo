package com.paymentgateway.commons.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Amitosh
 *
 */

@Service("smsControllerServiceProvider")
public class SmsControllerServiceProvider {

	@Autowired
	private SmsSender smsSender;

	private static Logger logger = LoggerFactory.getLogger(SmsControllerServiceProvider.class.getName());

	public void invoiceLink(String paymentUrl, Invoice invoice) {

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
			String message = "Kindly click on the link below to make your payment via Payment Geteway \nInvoice Id : "
					+ invoice.getInvoiceId() + "\nAmount : " + invoice.getAmount() + "\nService charge : "
					+ invoice.getServiceCharge() + "\nQuantity : " + invoice.getQuantity() + "\nTotal Amount : "
					+ invoice.getTotalAmount() + "\n\n" + invoice.getShortUrl() + "\n\nAbove link will expire on : "
					+ invoice.getExpiresDay();

			String mobile = invoice.getPhone();

			if (StringUtils.isBlank(mobile)) {
				return;
			}
			if (mobile.length() == 11) {
				if (mobile.charAt(0) == 0) {
					mobile = mobile.substring(1, 10);
				} else {

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

				String line;
				while ((line = rd.readLine()) != null) {
					str1.append(line);
				}

				rd.close();
				response = str1.toString();

				JSONObject serverResponse = new JSONObject(response);
				logger.info("Server Response = " + smsSender.maskFields(serverResponse.toString()));
				logger.info("Message = " + serverResponse.get("ErrorMessage"));

				if (serverResponse.getString("ErrorCode").equalsIgnoreCase("000")) {
					logger.info("SMS send by InnuvisSolution");
				} else {
				}

			} catch (Exception exception) {
				logger.error("Exception Caught while sending InnuvisSolution sms :", exception);
				// return null;
			}

		} else {
			String authKey = PropertiesManager.propertiesMap.get(Constants.AUTH_KEY.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID.getValue());
			String country = PropertiesManager.propertiesMap.get(Constants.SMS_TO_COUNTRY.getValue());
			String response = "";
			try {
				String message = "Kindly click on the link below to make your payment via Payment Geteway \nInvoice Id : "
						+ invoice.getInvoiceId() + "\nAmount : " + invoice.getAmount() + "\nService charge : "
						+ invoice.getServiceCharge() + "\nQuantity : " + invoice.getQuantity() + "\nTotal Amount : "
						+ invoice.getTotalAmount() + "\n\n" + invoice.getShortUrl() + "\n\nAbove link will expire on : "
						+ invoice.getExpiresDay();
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
				smsToArray.put(invoice.getPhone());
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
			} catch (Exception exception) {
				logger.error("Exception Caught while sending sms : ", exception);
			}
		}
	}

	public boolean invoiceSms(String paymentUrl, Invoice invoice) {

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
			String message = "Kindly click on the link below to make your payment via Payment Geteway \nInvoice Id : "
					+ invoice.getInvoiceId() + "\nAmount : " + invoice.getAmount() + "\nService charge : "
					+ invoice.getServiceCharge() + "\nQuantity : " + invoice.getQuantity() + "\nTotal Amount : "
					+ invoice.getTotalAmount() + "\n\n" + invoice.getShortUrl() + "\n\nAbove link will expire on : "
					+ invoice.getExpiresDay();
			String mobile = invoice.getPhone();

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
				logger.info("Server Response = " + smsSender.maskFields(serverResponse.toString()));
				logger.info("Message = " + serverResponse.get("ErrorMessage"));
				// System.out.println(serverResponse);

				if (serverResponse.getString("ErrorCode").equalsIgnoreCase("000")) {
					logger.info("SMS send by InnuvisSolution");
					return true;
				} else {
					return false;
				}

			} catch (Exception exception) {
				logger.error("exception : ", exception);
				return false;
			}
		} else {
			String authKey = PropertiesManager.propertiesMap.get(Constants.AUTH_KEY.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID.getValue());
			String country = PropertiesManager.propertiesMap.get(Constants.SMS_TO_COUNTRY.getValue());
			String response = "";
			try {
				String message = "Kindly click on the link below to make your payment via Payment Geteway \nInvoice Id : "
						+ invoice.getInvoiceId() + "\nAmount : " + invoice.getAmount() + "\nService charge : "
						+ invoice.getServiceCharge() + "\nQuantity : " + invoice.getQuantity() + "\nTotal Amount : "
						+ invoice.getTotalAmount() + "\n\n" + invoice.getShortUrl() + "\n\nAbove link will expire on : "
						+ invoice.getExpiresDay();

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
				smsToArray.put(invoice.getPhone());
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
				}
				return false;
			} catch (Exception exception) {
				logger.error("Exception Caught while sending sms : ", exception);
				return false;
			}
		}
	}

	public void transactionSmsForCustomer(String phone, String totalAmount, String orderId, String businessName) {

		String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
		if (StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {

			logger.info("Inside InnuvisSolution SMS");
			String user = PropertiesManager.propertiesMap.get(Constants.USER_INNUVISSOLUTION.getValue());
			String password = PropertiesManager.propertiesMap.get(Constants.PASSWORD_INNUVISSOLUTION.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVISSOLUTION_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID_INNUVISSOLUTION.getValue());
			String channel = PropertiesManager.propertiesMap.get(Constants.CHANNEL_INNUVISSOLUTION.getValue());
			String DCS = PropertiesManager.propertiesMap.get(Constants.DCS.getValue());
			String flashsms = PropertiesManager.propertiesMap.get(Constants.FLASHSMS.getValue());
			String peid = PropertiesManager.propertiesMap.get(Constants.PEID.getValue());
			String response = "";

			String message = "Dear Customer" + "\nINR " + totalAmount
					+ " has been successfully debited against your transaction (" + orderId + ") at " + businessName
					+ "." + "\nThanks" + "\nTeam Payment Geteway";

			if (StringUtils.isBlank(phone)) {
				return;
			}

			if (phone.length() == 11) {
				if (phone.charAt(0) == 0) {
					phone = phone.substring(1, 10);
				} else {
					return;
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
			str.append(phone);
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
				logger.info("Server Response = " + smsSender.maskFields(serverResponse.toString()));
				logger.info("Message = " + serverResponse.get("ErrorMessage"));
				// System.out.println(serverResponse);

				if (serverResponse.getString("ErrorCode").equalsIgnoreCase("000")) {
					logger.info("SMS send by InnuvisSolution");
				} else {
					return;
				}

			} catch (Exception exception) {
				logger.error("Exception : ", exception);
			}
		} else {
			String authKey = PropertiesManager.propertiesMap.get(Constants.AUTH_KEY.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID.getValue());
			String country = PropertiesManager.propertiesMap.get(Constants.SMS_TO_COUNTRY.getValue());
			String response = "";
			try {
				if (StringUtils.isBlank(phone)) {
					return;
				}

				String message = "Dear Customer" + "\nINR " + totalAmount
						+ " has been successfully debited against your transaction (" + orderId + ") at " + businessName
						+ "." + "\nThanks" + "\nTeam Payment Geteway";
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
				smsToArray.put(phone);
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
			} catch (Exception exception) {
				logger.error("Exception Caught while sending sms : ", exception);
			}
		}
	}

	public void transactionFailedSmsForCustomer(String phone, String totalAmount, String orderId, String businessName) {

		String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
		if (StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {

			logger.info("Inside InnuvisSolution SMS");

			String user = PropertiesManager.propertiesMap.get(Constants.USER_INNUVISSOLUTION.getValue());
			String password = PropertiesManager.propertiesMap.get(Constants.PASSWORD_INNUVISSOLUTION.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVISSOLUTION_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID_INNUVISSOLUTION.getValue());
			String channel = PropertiesManager.propertiesMap.get(Constants.CHANNEL_INNUVISSOLUTION.getValue());
			String DCS = PropertiesManager.propertiesMap.get(Constants.DCS.getValue());
			String flashsms = PropertiesManager.propertiesMap.get(Constants.FLASHSMS.getValue());
			String peid = PropertiesManager.propertiesMap.get(Constants.PEID.getValue());
			String response = "";

			if (StringUtils.isBlank(phone)) {
				return;
			}

			if (phone.length() == 11) {
				if (phone.charAt(0) == 0) {
					phone = phone.substring(1, 10);
				} else {
					return;
				}
			}

			String message = "Dear Customer" + "\nYour transaction of INR " + totalAmount + " against transaction ("
					+ orderId + ") at " + businessName + " couldn't get through. Please try again." + "\nThanks"
					+ "\nTeam Payment Geteway";

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
			str.append(phone);
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
				logger.info("Server Response = " + smsSender.maskFields(serverResponse.toString()));
				logger.info("Message = " + serverResponse.get("ErrorMessage"));
				// System.out.println(serverResponse);

				if (serverResponse.getString("ErrorCode").equalsIgnoreCase("000")) {
					logger.info("SMS send by InnuvisSolution");
				} else {
					return;
				}

			} catch (Exception exception) {
				logger.error("Exception cought by sending InnuvisSolution sms", exception);
			}
		} else {
			String authKey = PropertiesManager.propertiesMap.get(Constants.AUTH_KEY.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID.getValue());
			String country = PropertiesManager.propertiesMap.get(Constants.SMS_TO_COUNTRY.getValue());
			String response = "";
			try {
				if (StringUtils.isBlank(phone)) {
					return;
				}

				String message = "Dear Customer" + "\nYour transaction of INR " + totalAmount + " against transaction ("
						+ orderId + ") at " + businessName + " couldn't get through. Please try again." + "\nThanks"
						+ "\nTeam Payment Geteway";

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
				smsToArray.put(phone);
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
			} catch (Exception exception) {
				logger.error("Exception Caught while sending sms : ", exception);
			}
		}
	}

	public void transactionSmsForMerchant(String phone, String totalAmount, String orderId) {

		String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
		if (StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {

			logger.info("Inside InnuvisSolution SMS");

			String user = PropertiesManager.propertiesMap.get(Constants.USER_INNUVISSOLUTION.getValue());
			String password = PropertiesManager.propertiesMap.get(Constants.PASSWORD_INNUVISSOLUTION.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVISSOLUTION_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID_INNUVISSOLUTION.getValue());
			String channel = PropertiesManager.propertiesMap.get(Constants.CHANNEL_INNUVISSOLUTION.getValue());
			String DCS = PropertiesManager.propertiesMap.get(Constants.DCS.getValue());
			String flashsms = PropertiesManager.propertiesMap.get(Constants.FLASHSMS.getValue());
			String peid = PropertiesManager.propertiesMap.get(Constants.PEID.getValue());
			String response = "";

			if (StringUtils.isBlank(phone)) {
				return;
			}
			if (phone.length() == 11) {
				if (phone.charAt(0) == 0) {
					phone = phone.substring(1, 10);
				} else {
					return;
				}
			}

			String message = "Dear Partner" + "\nINR " + totalAmount
					+ " has been successfully debited against transaction (" + orderId + ") at Payment Geteway.com."
					+ "\nThanks" + "\nTeam Payment Geteway";

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
			str.append(phone);
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
				logger.info("Server Response = " + serverResponse);
				logger.info("Message = " + serverResponse.get("ErrorMessage"));
				// System.out.println(serverResponse);

				if (serverResponse.getString("ErrorCode").equalsIgnoreCase("000")) {
					logger.info("SMS send by InnuvisSolution");

				} else {
					return;
				}

			} catch (Exception exception) {
				logger.error("Exception cought by sending innuvis Solution SMS", exception);
			}
		} else {
			String authKey = PropertiesManager.propertiesMap.get(Constants.AUTH_KEY.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID.getValue());
			String country = PropertiesManager.propertiesMap.get(Constants.SMS_TO_COUNTRY.getValue());
			String response = "";
			try {
				if (StringUtils.isBlank(phone)) {
					return;
				}
				String message = "Dear Partner" + "\nINR " + totalAmount
						+ " has been successfully debited against transaction (" + orderId + ") at Payment Geteway.com."
						+ "\nThanks" + "\nTeam Payment Geteway";
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
				smsToArray.put(phone);
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
			} catch (Exception exception) {
				logger.error("Exception Caught while sending sms : ", exception);
			}
		}
	}

	public void transactionFailedSmsForMerchant(String phone, String totalAmount, String orderId) {

		String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
		if (StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {

			logger.info("Inside InnuvisSolution SMS");
			String user = PropertiesManager.propertiesMap.get(Constants.USER_INNUVISSOLUTION.getValue());
			String password = PropertiesManager.propertiesMap.get(Constants.PASSWORD_INNUVISSOLUTION.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVISSOLUTION_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID_INNUVISSOLUTION.getValue());
			String channel = PropertiesManager.propertiesMap.get(Constants.CHANNEL_INNUVISSOLUTION.getValue());
			String DCS = PropertiesManager.propertiesMap.get(Constants.DCS.getValue());
			String flashsms = PropertiesManager.propertiesMap.get(Constants.FLASHSMS.getValue());
			String peid = PropertiesManager.propertiesMap.get(Constants.PEID.getValue());
			String response = "";

			if (StringUtils.isBlank(phone)) {
				return;
			}

			if (phone.length() == 11) {
				if (phone.charAt(0) == 0) {
					phone = phone.substring(1, 10);
				} else {
					return;
				}
			}

			String message = "Dear Partner" + "\nTransaction INR " + totalAmount + " against transaction (" + orderId
					+ ") at Payment Geteway.com couldn't get through." + "\nThanks" + "\nTeam Payment Geteway";

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
			str.append(phone);
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
				logger.info("Server Response = " + smsSender.maskFields(serverResponse.toString()));
				logger.info("Message = " + serverResponse.get("ErrorMessage"));
				// System.out.println(serverResponse);

				if (serverResponse.getString("ErrorCode").equalsIgnoreCase("000")) {
					logger.info("SMS send by InnuvisSolution");
				} else {
					return;
				}

			} catch (Exception exception) {
				logger.error("Exception Caught while sending InnuvisSolution sms:", exception);
			}
		} else {
			String authKey = PropertiesManager.propertiesMap.get(Constants.AUTH_KEY.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID.getValue());
			String country = PropertiesManager.propertiesMap.get(Constants.SMS_TO_COUNTRY.getValue());
			String response = "";
			try {
				if (StringUtils.isBlank(phone)) {
					return;
				}
				String message = "Dear Partner" + "\nTransaction INR " + totalAmount + " against transaction ("
						+ orderId + ") at Payment Geteway.com couldn't get through." + "\nThanks" + "\nTeam Payment Geteway";
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
				smsToArray.put(phone);
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
			} catch (Exception exception) {
				logger.error("Exception Caught while sending sms : ", exception);
			}
		}
	}

	public void transactionRefundSmsForCustomer(String phone, String totalAmount, String orderId) {

		String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
		if (StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {

			logger.info("Inside InnuvisSolution SMS");

			String user = PropertiesManager.propertiesMap.get(Constants.USER_INNUVISSOLUTION.getValue());
			String password = PropertiesManager.propertiesMap.get(Constants.PASSWORD_INNUVISSOLUTION.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVISSOLUTION_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID_INNUVISSOLUTION.getValue());
			String channel = PropertiesManager.propertiesMap.get(Constants.CHANNEL_INNUVISSOLUTION.getValue());
			String DCS = PropertiesManager.propertiesMap.get(Constants.DCS.getValue());
			String flashsms = PropertiesManager.propertiesMap.get(Constants.FLASHSMS.getValue());
			String peid = PropertiesManager.propertiesMap.get(Constants.PEID.getValue());
			String response = "";

			if (StringUtils.isBlank(phone)) {
				return;
			}

			if (phone.length() == 11) {
				if (phone.charAt(0) == 0) {
					phone = phone.substring(1, 10);
				} else {
					return;
				}
			}

			String message = "Dear Customer" + "\nRefund of INR " + totalAmount
					+ " has been successfully processed against your transaction (" + orderId
					+ "). The amount shall be credited in 3-5 days." + "\nThanks" + "\nTeam Payment Geteway";

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
			str.append(phone);
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
				logger.info("Server Response = " + smsSender.maskFields(serverResponse.toString()));
				logger.info("Message = " + serverResponse.get("ErrorMessage"));
				// System.out.println(serverResponse);

				if (serverResponse.getString("ErrorCode").equalsIgnoreCase("000")) {
					logger.info("SMS send by InnuvisSolution");

				} else {
					return;
				}

			} catch (Exception exception) {
				logger.error("Exception Caught while sending InnuvisSolution sms", exception);

			}
		} else {
			String authKey = PropertiesManager.propertiesMap.get(Constants.AUTH_KEY.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID.getValue());
			String country = PropertiesManager.propertiesMap.get(Constants.SMS_TO_COUNTRY.getValue());
			String response = "";
			try {
				if (StringUtils.isBlank(phone)) {
					return;
				}

				String message = "Dear Customer" + "\nRefund of INR " + totalAmount
						+ " has been successfully processed against your transaction (" + orderId
						+ "). The amount shall be credited in 3-5 days." + "\nThanks" + "\nTeam Payment Geteway";
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
				smsToArray.put(phone);
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
			} catch (Exception exception) {
				logger.error("Exception Caught while sending sms : ", exception);
			}
		}
	}

	public void transactionRefundSmsForMerchant(String phone, String totalAmount, String orderId) {

		String smsInnuvisolutions = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
		if (StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {

			logger.info("Inside refund InnuvisSolution SMS");
			String user = PropertiesManager.propertiesMap.get(Constants.USER_INNUVISSOLUTION.getValue());
			String password = PropertiesManager.propertiesMap.get(Constants.PASSWORD_INNUVISSOLUTION.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_INNUVISSOLUTION_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID_INNUVISSOLUTION.getValue());
			String channel = PropertiesManager.propertiesMap.get(Constants.CHANNEL_INNUVISSOLUTION.getValue());
			String DCS = PropertiesManager.propertiesMap.get(Constants.DCS.getValue());
			String flashsms = PropertiesManager.propertiesMap.get(Constants.FLASHSMS.getValue());
			String peid = PropertiesManager.propertiesMap.get(Constants.PEID.getValue());
			String response = "";

			if (StringUtils.isBlank(phone)) {
				return;
			}

			if (phone.length() == 11) {
				if (phone.charAt(0) == 0) {
					phone = phone.substring(1, 10);
				} else {
					return;
				}
			}

			String message = "Dear Partner" + "\nRefund of INR " + totalAmount
					+ " has been successfully processed against transaction (" + orderId + ")." + "\nThanks"
					+ "\nTeam Payment Geteway";

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
			str.append(phone);
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
				logger.info("Server Response = " + smsSender.maskFields(serverResponse.toString()));
				logger.info("Message = " + serverResponse.get("ErrorMessage"));
				// System.out.println(serverResponse);

				if (serverResponse.getString("ErrorCode").equalsIgnoreCase("000")) {
					logger.info("SMS send by InnuvisSolution");
				} else {
					return;
				}

			} catch (Exception exception) {
				logger.error("Exception Caught while sending InnuvisSolution sms", exception);
			}
		} else {
			String authKey = PropertiesManager.propertiesMap.get(Constants.AUTH_KEY.getValue());
			String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_URL.getValue());
			String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID.getValue());
			String country = PropertiesManager.propertiesMap.get(Constants.SMS_TO_COUNTRY.getValue());
			String response = "";
			try {
				if (StringUtils.isBlank(phone)) {
					return;
				}
				String message = "Dear Partner" + "\nRefund of INR " + totalAmount
						+ " has been successfully processed against transaction (" + orderId + ")." + "\nThanks"
						+ "\nTeam Payment Geteway";
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
				smsToArray.put(phone);
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
			} catch (Exception exception) {
				logger.error("Exception Caught while sending sms : ", exception);
			}
		}
	}

	// old code below
	public void sendPromoSMS(Invoice invoiceDB, String shortUrl) {
		try {
			JSONObject json = new JSONObject();
			json.put("invoiceId", invoiceDB.getInvoiceId());
			json.put("payId", invoiceDB.getPayId());
			json.put("businessName", invoiceDB.getBusinessName());
			json.put("invoiceNo", invoiceDB.getInvoiceNo());
			json.put("name", invoiceDB.getName());
			json.put("city", invoiceDB.getCity());
			json.put("country", invoiceDB.getCountry());
			json.put("state", invoiceDB.getState());
			json.put("zip", invoiceDB.getZip());
			json.put("phone", invoiceDB.getPhone());
			json.put("email", invoiceDB.getEmail());
			json.put("address", invoiceDB.getAddress());
			json.put("productName", invoiceDB.getProductName());
			json.put("productDesc", invoiceDB.getProductDesc());
			json.put("quantity", invoiceDB.getQuantity());
			json.put("amount", invoiceDB.getAmount());
			json.put("serviceCharge", invoiceDB.getServiceCharge());
			json.put("totalAmount", invoiceDB.getTotalAmount());
			json.put("currencyCode", invoiceDB.getCurrencyCode());
			json.put("expiresDay", invoiceDB.getExpiresDay());
			json.put("expiresHour", invoiceDB.getExpiresHour());
			json.put("createDate", invoiceDB.getCreateDate());
			json.put("updateDate", invoiceDB.getUpdateDate());
			json.put("saltKey", invoiceDB.getSaltKey());
			json.put("returnUrl", invoiceDB.getReturnUrl());
			json.put("recipientMobile", invoiceDB.getRecipientMobile());
			json.put("messageBody", invoiceDB.getMessageBody());
			json.put("shortUrl", invoiceDB.getShortUrl());
			json.put("invoiceType", invoiceDB.getInvoiceType());

			String serviceUrl = PropertiesManager.propertiesMap.get("SMSServiceSendPromoSMSURL");
			String url = serviceUrl + shortUrl;
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("Content-Type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			StatusLine statusLine = resp.getStatusLine();
			int statusCode = resp.getStatusLine().getStatusCode();
			System.out.println("request sent  resp code" + statusCode);
		} catch (Exception exception) {
			logger.error("error : ", exception);
		}

	}

	public boolean sendVerificationLinkSMS(String mobile, String shortUrl) throws Exception {

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
			String message = "Dear Customer,\n" + "Please click on the link to verify your bank account. " + shortUrl
					+ "\nTeam Payment Geteway";

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
				logger.info("Server Response = " + smsSender.maskFields(serverResponse.toString()));
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

			String message = "Dear User \\n" + "Please click on the link below To verify your account ";
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

	public boolean healthCheckUpSMS(String mobile, String shortUrl) throws Exception {

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
			String message = "Dear Customer,\n" + "Please click on the link to verify your bank account. " + shortUrl
					+ "\nTeam Payment Geteway";

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
				logger.info("Server Response = " + smsSender.maskFields(serverResponse.toString()));
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

			String message = "Dear User \\n" + "Please click on the link below To verify your account ";
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

	public boolean sendUpiAutoPayLinkViaSMS(String mobile, String shortUrl) throws Exception {

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
			String message = "Dear Customer \n"
					+ "Please click on the link below to register for UPI Autopay mandate. INR 1 will be deducted from your account to verify your bank account details. "
					+ shortUrl + "\n Team Payment Geteway";

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
				logger.info("Server Response = " + smsSender.maskFields(serverResponse.toString()));
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

			String message = "Dear User \\n" + "Please click on the link below To verify your account ";
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

}
