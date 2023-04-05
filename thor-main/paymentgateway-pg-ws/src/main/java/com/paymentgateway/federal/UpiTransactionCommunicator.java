package com.paymentgateway.federal;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

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

@Service("federalUpiTransactionCommunicator")
public class UpiTransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(UpiTransactionCommunicator.class.getName());

	public void updateInvalidVpaResponse(Fields fields, Transaction transaction) {

		fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_VPA.getResponseCode());
		//fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_VPA.getResponseMessage());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getResponse());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());
	}

	public void updateSaleResponse(Fields fields, Transaction transactionResponse) {

		String responseCode = transactionResponse.getResponseCode();
		if (responseCode.equals(Constants.UPI_SUCCESS_CODE)) {
			fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		} else {
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED.getResponseMessage());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), transactionResponse.getResponse());
			fields.put(FieldType.PG_RESP_CODE.getName(), transactionResponse.getResponseCode());

		}

	}
	
	public void updateRefundResponse(Fields fields, Transaction transactionResponse) {

		String responseCode = transactionResponse.getResponseCode();
		if (responseCode.equals(Constants.UPI_SUCCESS_CODE)) {
			fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		} else {
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED.getResponseMessage());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), transactionResponse.getResponse());
			fields.put(FieldType.PG_RESP_CODE.getName(), transactionResponse.getResponseCode());

		}

	}

	@SuppressWarnings("incomplete-switch")
	public JSONObject getResponse(JSONObject request, Fields fields) throws SystemException {
		
		StringBuilder serverResponse = new StringBuilder();
		
		String hostUrl = "";

		TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
		switch (transactionType) {
		case SALE:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.FEDERAL_UPI_SALE_URL);
			break;
		case REFUND:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.FEDERAL_UPI_REFUND_URL);
			break;
		case STATUS:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.FEDERAL_UPI_STATUS_ENQ_URL);

		}

		HttpsURLConnection connection = null;
		HttpURLConnection simulatorConn = null;

		if (hostUrl.contains("https")) {
			try {

				// provide jks path here as string here
				// SSLContext sc =
				// getSSLContext("C:/Users/Rahul/Desktop/FedUPILib/FedUPILib/src/certs/store.jks");
				SSLContext sc = getSSLContext(System.getenv(Constants.PG_PROPERTIES_PATH) + Constants.JKS_FILE_NAME);

				// Create connection

				URL url = new URL(hostUrl);
				connection = (HttpsURLConnection) url.openConnection();
				connection.setSSLSocketFactory(sc.getSocketFactory());

				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Content-Length", request.toString());
				connection.setRequestProperty("Content-Language", "en-US");

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

				while ((line = rd.readLine()) != null) {
					serverResponse.append(line);
					serverResponse.append('\r');
				}
				rd.close();

			} catch (Exception e) {
				logger.info("Exception in UpiTransactionCommunicator " , e);
			} finally {
				if (connection != null) {
					connection.disconnect();
				}

				System.clearProperty("https.protocols");
				System.clearProperty("javax.net.ssl.trustStore");
			}
		} else {

			try {

				// Create connection

				URL url = new URL(hostUrl);
				simulatorConn = (HttpURLConnection) url.openConnection();

				simulatorConn.setRequestMethod("POST");
				simulatorConn.setRequestProperty("Content-Type", "application/json");
				simulatorConn.setRequestProperty("Content-Length", request.toString());
				simulatorConn.setRequestProperty("Content-Language", "en-US");

				simulatorConn.setUseCaches(false);
				simulatorConn.setDoOutput(true);
				simulatorConn.setDoInput(true);

				// Send request
				OutputStream outputStream = simulatorConn.getOutputStream();
				DataOutputStream wr = new DataOutputStream(outputStream);
				wr.writeBytes(request.toString());
				wr.close();

				// Get Response
				InputStream is = simulatorConn.getInputStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is));
				String line;

				while ((line = rd.readLine()) != null) {
					serverResponse.append(line);

				}
				rd.close();

			} catch (Exception e) {
				logger.error("Exception in getResponse , exception = " , e);
			} finally {
				if (simulatorConn != null) {
					simulatorConn.disconnect();
				}
			}

		}
		JSONObject response = new JSONObject(serverResponse.toString());
		logger.info("Response received from federal bank " + response.toString());
		return response;
	}

	private SSLContext getSSLContext(String path) throws Exception {

		try {
			// jks password
			// char[] password = "fedtst123".toCharArray();
			char[] password = Constants.JKS_PASSWORD.toCharArray();
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			KeyStore keyStore = KeyStore.getInstance("JKS");

			InputStream keyInput = new FileInputStream(new File(path));
			keyStore.load(keyInput, password);
			keyInput.close();
			keyManagerFactory.init(keyStore, password);

			KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();// getKeyManagers(,
																			// password);
			SecureRandom secureRandom = new SecureRandom();

			SSLContext sc = SSLContext.getInstance("TLSv1.2");
			sc.init(keyManagers, null, secureRandom);

			return sc;
		}
		
		catch(Exception e) {
			
			logger.error("Exception in getSSLContext , exsception = " , e);
			return null;
		}
		
	}

}
