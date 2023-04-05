package com.paymentgateway.billDesk;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;
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

/**
 * @author Rahul
 *
 */
@Service("billDeskTransactionCommunicator")
public class TransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateSaleResponse(Fields fields, String request) {

		fields.put(FieldType.BILLDESK_FINAL_REQUEST.getName(), request);
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

	}

	@SuppressWarnings("incomplete-switch")
	public String getResponse(String request, Fields fields) throws SystemException {
		String response = "";
		String hostUrl = "";

		TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
		switch (transactionType) {
		case SALE:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.AUTHORIZATION_REQUEST_URL);
			try {
				response = executeAuthorizationPost(request, hostUrl, fields);
			} catch (Exception exception) {
				logger.error("Exception : " , exception);
			}
			break;
		case AUTHORISE:
			break;
		case ENROLL:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.ENROLLMENT_REQUEST_URL);
			try {
				response = executeEnrollementPost(request, hostUrl, fields);
			} catch (Exception exception) {
				logger.error("Exception : " , exception);
			}
			break;
		case CAPTURE:
			break;
		case REFUND:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.REFUND_REQUEST_URL);
			try {
				response = executePost(request, hostUrl, fields);
			} catch (Exception exception) {
				logger.error("Exception : " , exception);
			}
			break;
		case STATUS:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.ENQUIRY_REQUEST_URL);
			try {
				response = executePost(request, hostUrl, fields);
			} catch (Exception exception) {
				logger.error("Exception : " , exception);
			}
			break;
		}

		return response;
	}

	public String executeAuthorizationPost(String urlParameters, String hostUrl, Fields fields) throws SystemException {
		PostMethod postMethod = new PostMethod(hostUrl);
		JSONObject request = new JSONObject(urlParameters);
		String billdeskResponse = fields.get(FieldType.BILLDESK_RESPONSE_FIELD.getName());
		String[] paramaters = billdeskResponse.split(",");
		Map<String, String> paramMap = new HashMap<String, String>();
		for (String param : paramaters) {
			String[] parameterPair = param.split("=");
			if (parameterPair.length > 1) {
				paramMap.put(parameterPair[0].trim(), parameterPair[1].trim());
				postMethod.addParameter(parameterPair[0].trim(), parameterPair[1].trim());
			}
		}

		StringBuilder requestParams = new StringBuilder();
		NameValuePair[] requestFields = postMethod.getParameters();
		for (NameValuePair nameValuePair : requestFields) {
			requestParams.append(nameValuePair.toString());
			requestParams.append("&");
		}
		requestParams.append("paydata" + "");
		logger.info("Sale Request message to BillDesk: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
				+ "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + requestParams.toString());

		postMethod.addParameter("paydata", request.get("paydata").toString());

		return transact(postMethod, hostUrl, fields);

	}

	public String executeEnrollementPost(String urlParameters, String hostUrl, Fields fields) throws SystemException {
		PostMethod postMethod = new PostMethod(hostUrl);
		JSONObject request = new JSONObject(urlParameters);
		postMethod.addParameter("msg", request.get("msg").toString());
		postMethod.addParameter("paydata", request.get("paydata").toString());
		postMethod.addParameter("ipaddress", request.get("ipaddress").toString());
		postMethod.addParameter("useragent", request.get("useragent").toString());

		return transact(postMethod, hostUrl, fields);

	}

	public String transact(HttpMethod httpMethod, String hostUrl, Fields fields) throws SystemException {
		String response = "";

		try {
			HttpClient httpClient = new HttpClient();
			httpClient.executeMethod(httpMethod);

			if (httpMethod.getStatusCode() == HttpStatus.SC_OK) {
				response = httpMethod.getResponseBodyAsString();
				logger.info("Response from BillDesk: " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = "
						+ fields.get(FieldType.TXN_ID.getName()) + response);
			} else {
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Network Exception with billdesk "
						+ hostUrl.toString() + "recieved response code" + httpMethod.getStatusCode());
			}
		} catch (IOException ioException) {
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, ioException,
					"Network Exception with BillDesk  " + hostUrl.toString());
		}
		return response;

	}
	/*
	 * public String executeCardPost(String urlParameters, String hostUrl, Fields
	 * fields) throws Exception {
	 * logger.info("Refund Request for BillDesk: Txn id = " +
	 * fields.get(FieldType.TXN_ID.getName()) + " " + urlParameters); String
	 * response = "";
	 * 
	 * JSONObject request = new JSONObject(urlParameters);
	 * 
	 * 
	 * String[] resArray = urlParameters.split(","); String msg = resArray[0];
	 * String paydata = resArray[1]; String ip = resArray[2]; String agent =
	 * resArray[3];
	 * 
	 * 
	 * HttpURLConnection connection = null; URL url; try { // Create connection url
	 * = new URL(hostUrl);
	 * 
	 * connection = (HttpURLConnection) url.openConnection();
	 * connection.setRequestMethod("POST");
	 * connection.setRequestProperty("Content-Type",
	 * "application/x-www-form-urlencoded");
	 * 
	 * connection.setRequestProperty("Content-Length", "msg" +
	 * Integer.toString(request.get("msg").toString().getBytes().length));
	 * connection.setRequestProperty("Content-Length", "paydata" +
	 * Integer.toString(request.get("paydata").toString().getBytes().length));
	 * connection.setRequestProperty("Content-Length", "ipaddress" +
	 * Integer.toString(request.get("ip").toString().getBytes().length));
	 * connection.setRequestProperty("Content-Length", "useragent" +
	 * Integer.toString(request.get("userAgent").toString().getBytes().length));
	 * connection.setRequestProperty("Content-Language", "en-US");
	 * 
	 * connection.setUseCaches(false); connection.setDoInput(true);
	 * connection.setDoOutput(true); connection.setConnectTimeout(60000);
	 * connection.setReadTimeout(60000);
	 * 
	 * // Send request DataOutputStream wr = new
	 * DataOutputStream(connection.getOutputStream()); wr.writeBytes(urlParameters);
	 * wr.flush(); wr.close();
	 * 
	 * // Get Response InputStream is = connection.getInputStream(); BufferedReader
	 * bufferedreader = new BufferedReader(new InputStreamReader(is)); String
	 * decodedString;
	 * 
	 * while ((decodedString = bufferedreader.readLine()) != null) { response =
	 * response + decodedString; } bufferedreader.close();
	 * logger.info("Response Received from BillDesk: TxnType = " +
	 * fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " +
	 * fields.get(FieldType.TXN_ID.getName()) + " " + response); return response; }
	 * 
	 * catch (Exception exception) { logger.error("Exception : ", exception); throw
	 * new SystemException(ErrorType.ACQUIRER_ERROR,
	 * "Unable to get respose from BillDesk"); } finally {
	 * 
	 * if (connection != null) { connection.disconnect(); } } }
	 */

	public String executePost(String urlParameters, String hostUrl, Fields fields) throws Exception {
		logger.info("Refund Request for BillDesk: Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " "
				+ urlParameters);
		String response = "";

		HttpURLConnection connection = null;
		URL url;
		try {
			// Create connection
			url = new URL(hostUrl);

			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setConnectTimeout(60000);
			connection.setReadTimeout(60000);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters);
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
			logger.info("Response Received from BillDesk: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
					+ "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + response);
			return response;
		}

		catch (Exception exception) {
			logger.error("Exception : ", exception);
			throw new SystemException(ErrorType.ACQUIRER_ERROR, "Unable to get respose from BillDesk");
		} finally {

			if (connection != null) {
				connection.disconnect();
			}
		}
	}

}
