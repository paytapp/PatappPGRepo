package com.paymentgateway.idfcUpi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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

@Service("idfcUpiTransactionCommunicator")
public class TransactionCommunicator {
	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	@SuppressWarnings("incomplete-switch")
	public JSONObject getVpaResponse(JSONObject request, Fields fields) throws SystemException {

		StringBuilder serverResponse = new StringBuilder();

		String hostUrl = PropertiesManager.propertiesMap.get(Constants.IDFC_UPI_REQUEST_URL);
		HttpsURLConnection connection = null;
		HttpURLConnection simulatorConn = null;

		if (hostUrl.contains("https")) {
			try {
				SSLContext sc = getSSLContext(System.getenv(Constants.PG_PROPERTIES_PATH)
						+ PropertiesManager.propertiesMap.get(Constants.JKS_FILE_NAME));
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
				logger.info("Request to IDFC UPI : " + request);
				URL url = new URL(hostUrl);
				connection = (HttpsURLConnection) url.openConnection();

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

				int code = ((HttpURLConnection) connection).getResponseCode();
				int firstDigitOfCode = Integer.parseInt(Integer.toString(code).substring(0, 1));
				if (firstDigitOfCode == 4 || firstDigitOfCode == 5) {
					fields.put(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName());
					logger.error("Response code of txn :" + code);
					throw new SystemException(ErrorType.ACUIRER_DOWN,
							"Network Exception with IDFC bank upi " + hostUrl.toString());
				}

				while ((line = rd.readLine()) != null) {
					serverResponse.append(line);

				}
				logger.info("Response received from IDFC bank : " + serverResponse);
				rd.close();

			} catch (Exception e) {
				logger.error("Exception in idfc bank upi communicator : " , e);
				int code = 0;
				try {
					code = ((HttpURLConnection) connection).getResponseCode();
				} catch (IOException e1) {
					logger.error("IO Exception  :" , e1);
				}
				int firstDigitOfCode = Integer.parseInt(Integer.toString(code).substring(0, 1));
				if (firstDigitOfCode == 4 || firstDigitOfCode == 5) {
					fields.put(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName());
					logger.error("Response code of txn :" + code);
					throw new SystemException(ErrorType.ACUIRER_DOWN,
							"Network Exception with IDFC Bank Upi" + hostUrl.toString());
				} else {
					logger.error("Exception in getSSLContext , exsception = " + e);
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
							"ssl connection Exception with IDFC Bank Upi" + hostUrl.toString());
				}
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
				logger.info("Response received from IDFC bank : " + serverResponse);
				rd.close();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (simulatorConn != null) {
					simulatorConn.disconnect();
				}
			}

		}
		logger.info("idfc UPI  VPA response :: " + serverResponse.toString());
		if (serverResponse.toString().contains(Constants.XML_RESPONSE)) {
			throw new SystemException(ErrorType.ACUIRER_DOWN,
					"Network Exception with IDFC Bank Upi for vpa validation response in XML format"
							+ hostUrl.toString());
		} else {
			JSONObject response = new JSONObject(serverResponse.toString());
			return response;
		}
	}

	@SuppressWarnings("incomplete-switch")
	public JSONObject getResponse(JSONObject request, Fields fields) throws SystemException {

		StringBuilder serverResponse = new StringBuilder();

		String hostUrl = "";

		TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
		switch (transactionType) {
		case SALE:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.IDFC_UPI_REQUEST_URL);
			break;
		case REFUND:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.IDFC_UPI_REQUEST_URL);
			break;
		case ENQUIRY:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.IDFC_UPI_REQUEST_URL);

		}
		HttpsURLConnection connection = null;
		HttpURLConnection simulatorConn = null;
		if (hostUrl.contains("https")) {
			try {

				SSLContext sc = getSSLContext(System.getenv(Constants.PG_PROPERTIES_PATH)
						+ PropertiesManager.propertiesMap.get(Constants.JKS_FILE_NAME));
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

				URL url = new URL(hostUrl);
				connection = (HttpsURLConnection) url.openConnection();

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
				// TO HANDLE EXCEPTION IF BANK SERVER IS DOWN
				int code = ((HttpURLConnection) connection).getResponseCode();
				int firstDigitOfCode = Integer.parseInt(Integer.toString(code).substring(0, 1));
				if (firstDigitOfCode == 4 || firstDigitOfCode == 5) {
					fields.put(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName());
					logger.error("Response code of txn :" + code);
					throw new SystemException(ErrorType.ACUIRER_DOWN,
							"Network Exception with IDFC Bank Upi" + hostUrl.toString());
				}
				while ((line = rd.readLine()) != null) {
					serverResponse.append(line);
				}
				logger.info("Response from IDFC bank : " + serverResponse);
				rd.close();

			} catch (Exception e) {
				logger.error("Exception in idfc bank upi communicator : " , e);
				int code = 0;
				try {
					code = ((HttpURLConnection) connection).getResponseCode();
				} catch (IOException e1) {
					logger.error("Response  of txn IN CATCH BLOCK :" , e1);
				}
				int firstDigitOfCode = Integer.parseInt(Integer.toString(code).substring(0, 1));
				if (firstDigitOfCode == 4 || firstDigitOfCode == 5) {
					fields.put(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName());
					logger.error("Response code of txn :" + code);
					throw new SystemException(ErrorType.ACUIRER_DOWN,
							"Network Exception with Yes Bank Upi" + hostUrl.toString());
				} else {
					logger.error("Exception in getSSLContext , exsception = " , e);
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
							"ssl connection Exception with IDFC Bank Upi" + hostUrl.toString());
				}
			} finally {
				if (connection != null) {
					connection.disconnect();
				}

				System.clearProperty("https.protocols");
				System.clearProperty("javax.net.ssl.trustStore");
			}
		} else {
			try {
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

			} catch (Exception exception) {
				logger.error("Exception", exception);
			} finally {
				if (simulatorConn != null) {
					simulatorConn.disconnect();
				}

			}
		}
		logger.info("idfc UPI response :: " + serverResponse.toString());
		if (serverResponse.toString().contains(Constants.XML_RESPONSE)) {
			throw new SystemException(ErrorType.ACUIRER_DOWN,
					"Network Exception with IDFC Bank Upi for collect  response in XML format" + hostUrl.toString());
		} else {
			JSONObject response = new JSONObject(serverResponse.toString());
			return response;
		}
	}

	private SSLContext getSSLContext(String path) throws SystemException {

		try {

			char[] password = PropertiesManager.propertiesMap.get(Constants.JKS_PASSWORD).toCharArray();
			KeyManagerFactory keyManagerFactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			KeyStore keyStore = KeyStore.getInstance("JKS");

			InputStream keyInput = new FileInputStream(new File(path));
			keyStore.load(keyInput, password);
			keyInput.close();
			keyManagerFactory.init(keyStore, password);
			// getKeyManagers
			KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

			SecureRandom secureRandom = new SecureRandom();

			SSLContext sc = SSLContext.getInstance("TLSv1.2");
			sc.init(keyManagers, null, secureRandom);

			return sc;
		}

		catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException
				| UnrecoverableKeyException | KeyManagementException e) {
			logger.error("Exception in getSSLContext , exsception = " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in getSSLContext  for IDFC upi in TransactionCommunicator");
		}

	}

	TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}
	} };

	@SuppressWarnings("incomplete-switch")
	public JSONObject getStatusResponse(JSONObject request, Fields fields) throws SystemException {

		StringBuilder serverResponse = new StringBuilder();

		String hostUrl = "";

		TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
		hostUrl = PropertiesManager.propertiesMap.get(Constants.IDFC_UPI_REQUEST_URL);

		HttpsURLConnection connection = null;
		HttpURLConnection simulatorConn = null;
		if (hostUrl.contains("https")) {
			try {

				SSLContext sc = getSSLContext(System.getenv(Constants.PG_PROPERTIES_PATH)
						+ PropertiesManager.propertiesMap.get(Constants.JKS_FILE_NAME));
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

				URL url = new URL(hostUrl);
				connection = (HttpsURLConnection) url.openConnection();

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
				// TO HANDLE EXCEPTION IF BANK SERVER IS DOWN
				int code = ((HttpURLConnection) connection).getResponseCode();
				int firstDigitOfCode = Integer.parseInt(Integer.toString(code).substring(0, 1));
				if (firstDigitOfCode == 4 || firstDigitOfCode == 5) {
					fields.put(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName());
					logger.error("Response code of txn :" + code);
					throw new SystemException(ErrorType.ACUIRER_DOWN,
							"Network Exception with IDFC Bank Upi" + hostUrl.toString());
				}
				while ((line = rd.readLine()) != null) {
					serverResponse.append(line);
				}
				logger.info("Response from IDFC bank for status enquiry : " + serverResponse);
				rd.close();

			} catch (Exception e) {
				logger.error("Exception in idfc bank upi status enquiry communicator : " , e);
				int code = 0;
				try {
					code = ((HttpURLConnection) connection).getResponseCode();
				} catch (IOException e1) {
					logger.error("Response  of txn IN CATCH BLOCK :" , e1);
				}
				int firstDigitOfCode = Integer.parseInt(Integer.toString(code).substring(0, 1));
				if (firstDigitOfCode == 4 || firstDigitOfCode == 5) {
					fields.put(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName());
					logger.error("Response code of txn :" + code);
					throw new SystemException(ErrorType.ACUIRER_DOWN,
							"Network Exception with Yes Bank Upi" + hostUrl.toString());
				} else {
					logger.error("Exception in getSSLContext , exsception = " , e);
					throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
							"ssl connection Exception with IDFC Bank Upi" + hostUrl.toString());
				}
			} finally {
				if (connection != null) {
					connection.disconnect();
				}

				System.clearProperty("https.protocols");
				System.clearProperty("javax.net.ssl.trustStore");
			}
		} else {
			try {
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

			} catch (Exception exception) {
				logger.error("Exception", exception);
			} finally {
				if (simulatorConn != null) {
					simulatorConn.disconnect();
				}

			}
		}
		logger.info("idfc UPI response :: " + serverResponse.toString());
		if (serverResponse.toString().contains(Constants.XML_RESPONSE)) {
			throw new SystemException(ErrorType.ACUIRER_DOWN,
					"Network Exception with IDFC Bank Upi for collect  response in XML format" + hostUrl.toString());
		} else {
			JSONObject response = new JSONObject(serverResponse.toString());
			return response;
		}
	}

}
