package com.paymentgateway.pg.core.acquirerDoubleVerification;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.MessageDigestProvider;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.PayuUtil;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class CheckBankResponseUsingStatusEnquiry {

	private Logger logger = LoggerFactory.getLogger(CheckBankResponseUsingStatusEnquiry.class.getName());

	public String payuBankStatusEnquiry(Map<String, String> statusEnquiryFields) throws SystemException {

		Fields fields = new Fields();
		fields.put(FieldType.PG_REF_NUM.getName(), statusEnquiryFields.get(FieldType.PG_REF_NUM.getName()));
		fields.put(FieldType.MERCHANT_ID.getName(), statusEnquiryFields.get(FieldType.MERCHANT_ID.getName()));
		fields.put(FieldType.PASSWORD.getName(), statusEnquiryFields.get(FieldType.PASSWORD.getName()));

		String request = statusEnquiryRequest(fields);
		String response = "";
		try {

			response = getResponse(request);

		} catch (SystemException exception) {
			logger.error("Exception", exception);
		} catch (Exception e) {
			logger.error("Exception in decrypting status enquiry response for payu ", e);
		}
		return response;

	}

	public String statusEnquiryRequest(Fields fields) {

		try {
			String hash = PayuUtil.payuStatusEnqHash(fields);

			StringBuilder request = new StringBuilder();

			request.append("key=");
			request.append(fields.get(FieldType.MERCHANT_ID.getName()));
			request.append("&");
			request.append("command=");
			request.append("verify_payment");
			request.append("&");
			request.append("hash=");
			request.append(hash);
			request.append("&");
			request.append("var1=");
			request.append(fields.get(FieldType.PG_REF_NUM.getName()));

			String post_data = request.toString();
			return post_data;

		}

		catch (Exception e) {
			logger.error("Exception in preparing Payu Status Enquiry Request", e);
		}

		return null;

	}
	
	public String eazyPaymentzBankStatusEnquiry(String appId, String secret, String orderId) throws SystemException {

		String stringResponse = "";
		String hostUrl = PropertiesManager.propertiesMap.get("EAZYPAYMENTZStatusEnqUrl");

		try {
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("appId", appId)
					.addFormDataPart("secret", sha256ConvertedSecret(secret)).addFormDataPart("orderId", orderId)
					.build();
			Request requestAirPay = new Request.Builder().url(hostUrl).method("POST", body).build();
			Response responseAirPay = client.newCall(requestAirPay).execute();
			stringResponse = responseAirPay.body().string();

			System.out.println("Eazypaymentz Status Enquiry Response >> " + stringResponse);

			return stringResponse;

		} catch (Exception e) {
			logger.error("Exception in getting Status Enquiry respose for Eazypaymentz", e);
		}
		return stringResponse;

	}

	public String getResponse(String request) throws SystemException {

		if (StringUtils.isBlank(request)) {
			logger.info("Request is empty for Payu status enquiry");
			return null;
		}

		String hostUrl = "";
		Fields fields = new Fields();
		fields.put(FieldType.TXNTYPE.getName(), "STATUS");

		try {

			hostUrl = PropertiesManager.propertiesMap.get("PAYUStatusEnqUrl");

			logger.info("Status Enquiry Request to Payu : TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
					+ "Order Id = " + fields.get(FieldType.PG_REF_NUM.getName()) + " ");
			log(request, fields);
			URL url = new URL(hostUrl);

			String[] arrOfStr = request.split("&", 4);
			Map<String, String> reqMap = new HashMap<String, String>();
			for (String reqst : arrOfStr) {
				String[] reqStr = reqst.split("=");
				reqMap.put(reqStr[0], reqStr[1]);
			}

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, String> param : reqMap.entrySet()) {
				if (postData.length() != 0)
					postData.append('&');
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			connection.setDoOutput(true);
			connection.setDoOutput(true);
			connection.getOutputStream().write(postDataBytes);

			DataOutputStream requestWriter = new DataOutputStream(connection.getOutputStream());
			requestWriter.writeBytes(request);
			requestWriter.close();
			String responseData = "";
			InputStream is = connection.getInputStream();
			BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));
			if ((responseData = responseReader.readLine()) != null) {
				// logger.info("Response received for payu status enq : " + responseData);
				log("Response received for payu status enq : " + responseData, fields);
			}

			responseReader.close();

			return responseData;

		} catch (Exception e) {
			logger.error("Exception in Payu Status Enquiry ", e);
		}
		return null;
	}

	private void log(String message, Fields fields) {
		message = Pattern.compile("(<card>)([\\s\\S]*?)(</card>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<pan>)([\\s\\S]*?)(</pan>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<expmonth>)([\\s\\S]*?)(</expmonth>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<expyear>)([\\s\\S]*?)(</expyear>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<cvv2>)([\\s\\S]*?)(</cvv2>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<password>)([\\s\\S]*?)(</password>)").matcher(message).replaceAll("$1$3");
		// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
		logger.info(message);
	}
	
	public static String sha256ConvertedSecret(String input) throws SystemException {
		String response = null;

		MessageDigest messageDigest = MessageDigestProvider.provide();
		messageDigest.update(input.getBytes());
		MessageDigestProvider.consume(messageDigest);

		response = new String(Hex.encodeHex(messageDigest.digest()));

		return response;
	}


}
