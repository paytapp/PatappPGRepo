package com.paymentgateway.kotak.upi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

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


@Service("kotakUpiTransactionCommunicator")
public class TransactionCommunicator {
	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateInvalidVpaResponse(Fields fields, String response) {

		fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_VPA.getResponseCode());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_VPA.getResponseMessage());
	}
	public void updateSaleResponse(Fields fields, String response) {

		String statusType = response;
		if (response!= null) {
			if (statusType.equals(Constants.SUCCESS_RESPONSE)) {
				fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED.getResponseMessage());
			}
		} else {
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED.getResponseMessage());
		}

	}
	@SuppressWarnings("incomplete-switch")
	public JSONObject getAuthoriseResponse(String request, Fields fields) throws SystemException {
		StringBuilder serverResponse = new StringBuilder();
		String hostUrl = PropertiesManager.propertiesMap.get("KotakUpiAuthUrl");

		// logger.info("Request sent to bank " + request);
		HttpsURLConnection connection = null;
		try {

			// Create connection

			URL url = new URL(hostUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);

			// Send request
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			int code = ((HttpURLConnection) connection).getResponseCode();
			int firstDigitOfCode = Integer.parseInt(Integer.toString(code).substring(0, 1));
			if(firstDigitOfCode == 4 || firstDigitOfCode == 5){
				 fields.put(FieldType.STATUS.getName(),StatusType.ACQUIRER_DOWN.getName());
				 logger.error("Response code of txn :" + code);
				 throw new SystemException(ErrorType.ACUIRER_DOWN,
							 "Network Exception with Kotak Upi "
									+ hostUrl.toString());
				}
			
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);

			}
			rd.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		JSONObject response = new JSONObject(serverResponse.toString());
		logger.info(" OAuth Response received from KotakUpi" + response.toString());
		return response;
	}

	//@SuppressWarnings("incomplete-switch")
	public JSONObject getVPAResponse(JSONObject request, Fields fields, String accessTokenResponse)
			throws SystemException {
		StringBuilder serverResponse = new StringBuilder();
		String hostUrl = PropertiesManager.propertiesMap.get("KotakUpiVPAUrl");
		HttpsURLConnection connection = null;

		try {

			// Create connection

			URL url = new URL(hostUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Authorization" , "Bearer "+ accessTokenResponse);

			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);

			// Send request
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			
			int code = ((HttpURLConnection) connection).getResponseCode();
			int firstDigitOfCode = Integer.parseInt(Integer.toString(code).substring(0, 1));
			if(firstDigitOfCode == 4 || firstDigitOfCode == 5){
				 fields.put(FieldType.STATUS.getName(),StatusType.ACQUIRER_DOWN.getName());
				 logger.error("Response code of txn :" + code);
				 throw new SystemException(ErrorType.ACUIRER_DOWN,
							 "Network Exception with Kotak Upi "
									+ hostUrl.toString());
				}
			
			
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);

			}
			rd.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				//connection.close();
			}
		}

		JSONObject response = new JSONObject(serverResponse.toString());
		logger.info(" VPA Validation Response received from kotakUpi" + response.toString());
		return response;
	}

	@SuppressWarnings("incomplete-switch")
	public JSONObject getResponse(JSONObject request, Fields fields , String accessTokenResponse, String xHeader) throws SystemException {
		StringBuilder serverResponse = new StringBuilder();
		String hostUrl = "";

		TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
		switch (transactionType) {
		case SALE:
			hostUrl = PropertiesManager.propertiesMap.get("KotakUpiSaleUrl");
			break;
		case REFUND:
			hostUrl = PropertiesManager.propertiesMap.get("KotakUpiRefundUrl");
			break;
		case ENQUIRY:
			hostUrl = PropertiesManager.propertiesMap.get("KotakUpiStatusEnqUrl");

		}

		HttpsURLConnection connection = null;
		try {

			// Create connection

			URL url = new URL(hostUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization" , "Bearer "+ accessTokenResponse);
			connection.setRequestProperty("X-Check" , xHeader);

			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);

			// Send request
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			
			int code = ((HttpURLConnection) connection).getResponseCode();
			int firstDigitOfCode = Integer.parseInt(Integer.toString(code).substring(0, 1));
			if(firstDigitOfCode == 4 || firstDigitOfCode == 5){
				 fields.put(FieldType.STATUS.getName(),StatusType.ACQUIRER_DOWN.getName());
				 logger.error("Response code of txn :" + code);
				 throw new SystemException(ErrorType.ACUIRER_DOWN,
							 "Network Exception with Kotak Upi "
									+ hostUrl.toString());
				}
			
			
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);

			}
			rd.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		JSONObject response = new JSONObject(serverResponse.toString());
		logger.info(" Response received from kotakUpi" + response.toString());
		return response;
	}
}
