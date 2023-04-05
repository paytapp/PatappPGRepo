package com.paymentgateway.notification.sms.sendSms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.notification.sms.smsCreater.UrlCreater;

/**
 * @author Amitosh
 */
@Component
public class SmsSenderApi {

	private static Logger logger = LoggerFactory.getLogger(SmsSenderApi.class.getName());

	@Autowired
	@Qualifier("userDao")
	private UserDao userDao;

	@Autowired
	private UrlCreater urlCreater;

	public String sendSMS(String mobile, String message) throws IOException {
		String authKey = PropertiesManager.propertiesMap.get(Constants.AUTH_KEY.getValue());
		String smsUrl = PropertiesManager.propertiesMap.get(Constants.SMS_URL.getValue());
		String senderId = PropertiesManager.propertiesMap.get(Constants.SENDER_ID.getValue());
		String country = PropertiesManager.propertiesMap.get(Constants.SMS_TO_COUNTRY.getValue());
		String response = "";
		try {
			if (mobile.length() == 11) {
				if (mobile.charAt(0) == 0) {
					mobile = mobile.substring(1, 10);
				} else {
					return null;
				}
			}
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
				return serverResponse.getString("type");
			} else {
				return null;
			}
		} catch (Exception exception) {
			logger.error("", exception);
			return null;
		}
	}

	// below is older code
	public void sendPromoSMS(Invoice invoice, String url) {
		try {
			// Bind on the basis of Merchant Contact Number
			String requestUrl = urlCreater.createPromoURL(invoice, url);
			URL msgUrl = new URL(requestUrl);
			HttpURLConnection urlconnection = (HttpURLConnection) msgUrl.openConnection();
			if (urlconnection.getResponseMessage().equals(Constants.CONTANT_OK.getValue())) {
				logger.info("Invoice sms sent:" + invoice.getInvoiceId());
			} else {

				logger.info("Error sending SMS for Invoice:" + invoice.getInvoiceId());
			}
			urlconnection.disconnect();
		} catch (Exception exception) {
			logger.error("Error sending SMS for Invoice:" + invoice.getInvoiceId());
		}
	}

	public void sendSMS(Map<String, String> responseMap) {
		// TODO Auto-generated method stub

	}
}
