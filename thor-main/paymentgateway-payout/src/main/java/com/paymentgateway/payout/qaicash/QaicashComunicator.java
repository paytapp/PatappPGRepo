package com.paymentgateway.payout.qaicash;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class QaicashComunicator {

	private static Logger logger = LoggerFactory.getLogger(QaicashComunicator.class);

	public String communication(String requestPayload, String url, Fields fields) {

		StringBuffer response = new StringBuffer();
		try {
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			StringBuilder sb = new StringBuilder();
			JSONObject object = new JSONObject(requestPayload);
			Iterator<String> keys = object.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				String value = object.getString(key);
				sb.append(key + "=" + value + "&");
			}

			String urlParams = sb.toString();
			urlParams = urlParams.substring(0, urlParams.length() - 1);

			con.setDoOutput(true);
			con.setDoInput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParams);
			wr.flush();
			wr.close();
			logger.info("Sending Qaicash POST request for Payout : " + url);
			int responseCode = con.getResponseCode();
			logger.info("Qaicash POST request responseCode {} for Pg Ref Num {}", responseCode,
					fields.get(FieldType.PG_REF_NUM.getName()));
			logger.info("Post parameters : " + urlParams);
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// print result
			logger.info(response.toString());
			return response.toString();

		} catch (Exception e) {
			logger.info("exception in Qaicash communicate() , ORDER_ID == {} , Exception  == {}",
					fields.get(FieldType.ORDER_ID.getName()), e);
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
			fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.INTERNAL_SYSTEM_ERROR.getResponseCode());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
		}
		return response.toString();

	}
	
	
	public String statusEnquiry(String hostUrl, Fields fields) {

		String stringResponse = "";
		HttpsURLConnection connection = null;
		try {
			
			logger.info("Status Enquiry Request URL for Qaicash Payout : " + hostUrl);
			StringBuilder serverResponse = new StringBuilder();
			URL url = new URL(hostUrl);

			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			int status = connection.getResponseCode();

			// No deposit found with this order id
			if (status == 404) {
				serverResponse.append("No Such Transaction Found");
				connection.disconnect();
				return serverResponse.toString();
			}
			
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
				serverResponse.append('\r');
			}
			rd.close();
			String str = serverResponse.toString();
			logger.info("Qaicash payout status enquiry response : {} ", str);
			return str;

		} 
		catch (Exception e) {
			logger.error("Exception in getting payout Status Enquiry respose for Qaicash", e);
		}
		return stringResponse;

	}


}
