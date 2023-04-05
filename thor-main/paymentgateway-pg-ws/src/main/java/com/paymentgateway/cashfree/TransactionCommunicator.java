package com.paymentgateway.cashfree;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service("cashfreeTransactionCommunicator")
public class TransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateSaleResponse(Fields fields, String request) {

		fields.put(FieldType.CASHFREE_FINAL_REQUEST.getName(), request);
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
	}

	@SuppressWarnings("incomplete-switch")
	public String getResponse(String request, Fields fields) throws SystemException {

		String hostUrl = "";
		String response = "";

		TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
		switch (transactionType) {
		case SALE:
		case AUTHORISE:
			break;
		case ENROLL:
			break;
		case CAPTURE:
			break;
		case REFUND:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.REFUND_REQUEST_URL);
			response = refundPostRequest(request, hostUrl);
			break;
		case STATUS:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.STATUS_ENQ_REQUEST_URL);
			response = statusEnqPostRequest(request, hostUrl);
			break;
		}
		return response;

	}

	public String refundPostRequest(String request, String hostUrl) throws SystemException {
		String response = "";

		try {

			HttpURLConnection connection = null;
			URL url;
			url = new URL(hostUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setConnectTimeout(60000);
			connection.setReadTimeout(60000);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(request);
			wr.flush();
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();

			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(is));
			String decodedString;

			while ((decodedString = bufferedreader.readLine()) != null) {
				response = response + decodedString;
			}

			bufferedreader.close();

			logger.info("Response for refund transaction >> " + response);

		} catch (Exception e) {
			logger.error("Exception in getting Refund respose for Cashfree", e);
			response = "{\"message\":\"Error in Refund.\",\"refundId\":NA,\"status\":\"FAILED\"}";
			return response;
		}
		return response;
	}

	public String statusEnqPostRequest(String request, String hostUrl) throws SystemException {
		String response = "";

		try {

			logger.info("Cashfree Status Enquiry Request " + request);
			HttpURLConnection connection = null;
			URL url;
			url = new URL(hostUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setConnectTimeout(60000);
			connection.setReadTimeout(60000);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(request);
			wr.flush();
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();

			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(is));
			String decodedString;

			while ((decodedString = bufferedreader.readLine()) != null) {
				response = response + decodedString;
			}

			bufferedreader.close();

			logger.info("Response for Cashfree Status Enquiry transaction >> " + response);

		} catch (Exception e) {
			logger.error("Exception in getting Status Enquiry respose for Cashfree", e);
		}
		return response;
	}

	public String vpaValidationResponse(Fields fields) {
		
		String CashfreeClientid = PropertiesManager.propertiesMap.get("CashfreeClientid");
		String CashfreeClientsecret = PropertiesManager.propertiesMap.get("CashfreeClientsecret");
		
		String responseString = "";
		try {
			String url = PropertiesManager.propertiesMap.get(Constants.VPA_VERIFY_URL)
					+ fields.get(FieldType.PAYER_ADDRESS.getName());
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			Request request = new Request.Builder().url(url).method("GET", null).addHeader("Cache-Control", "no-cache")
					.addHeader("x-client-id", CashfreeClientid)
					.addHeader("x-client-secret", CashfreeClientsecret).build();
			Response response = client.newCall(request).execute();

			responseString = response.body().string();
			logger.info("\nresponse " + responseString);
			client.dispatcher().executorService().shutdown();

		} catch (Exception e) {
			logger.error("Exception in vpaValidationResponse respose for Cashfree", e);
		}
		

		return responseString;
	}

	public String orderIdResponse(String data, Fields fields) {
		String responseString = "";
		try {
			String url = PropertiesManager.propertiesMap.get(Constants.CREATE_ORDER_TOKEN_URL);
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, data);
			Request request = new Request.Builder().url(url).method("POST", body)
					.addHeader("Content-Type", "application/json")
					.addHeader("x-client-id", fields.get(FieldType.MERCHANT_ID.getName()))
					.addHeader("x-client-secret", fields.get(FieldType.TXN_KEY.getName()))
					.addHeader("x-api-version", "2021-05-21").build();
			Response response = client.newCall(request).execute();

			responseString = response.body().string();
			logger.info("response from orderIdResponse >> " + responseString);
		} catch (Exception e) {
			logger.error("Exception in orderIdResponse respose for Cashfree", e);
		}

		return responseString;
	}

	public String payOrderResponse(String data) {
		String responseString = "";
		try {
			String url = PropertiesManager.propertiesMap.get(Constants.PAY_ORDER_URL);
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, data);
			Request request = new Request.Builder().url(url).method("POST", body)
					.addHeader("Content-Type", "application/json").build();
			Response response = client.newCall(request).execute();
			responseString = response.body().string();
			logger.info("response from payOrderResponse >> " + responseString);
		} catch (Exception e) {
			logger.error("Exception in payOrderResponse respose for Cashfree", e);
		}

		return responseString;
	}

	public void updateVpaFailedResponse(Fields fields) {

		fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_VPA.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_VPA.getResponseMessage());
	}
	
	public void updateFailedResponse(Fields fields) {

		fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
	}
}
