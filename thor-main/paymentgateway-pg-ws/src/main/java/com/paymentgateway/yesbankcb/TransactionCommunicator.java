package com.paymentgateway.yesbankcb;

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
@Service("yesBankCbTransactionCommunicator")
public class TransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	@SuppressWarnings("incomplete-switch")
	public String getVpaResponse(JSONObject request, Fields fields) throws SystemException {

		StringBuilder serverResponse = new StringBuilder();

		String hostUrl = PropertiesManager.propertiesMap.get(Constants.YES_BANK_UPI_VPA_URL);
		HttpsURLConnection connection = null;

		try {

//			SSLContext sc = getSSLContext(System.getenv(Constants.PG_PROPERTIES_PATH) + PropertiesManager.propertiesMap.get("YesBankJksName"));
			SSLContext sc = getSSLContextUpi();
			URL url = new URL(hostUrl);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setSSLSocketFactory(sc.getSocketFactory());

			connection.setRequestMethod("POST");
			connection.setRequestProperty("X-IBM-Client-Secret",
					PropertiesManager.propertiesMap.get("YESBANK_UPI_HEADER_CLIENT_SECRET"));
			connection.setRequestProperty("X-IBM-Client-ID",
					PropertiesManager.propertiesMap.get("YESBANK_UPI_HEADER_CLIENT_ID"));
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
						"Network Exception with Yes bank upi " + hostUrl.toString());
			}

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);

			}

			rd.close();

		} catch (Exception e) {
			logger.error("Exception in Yes Bank VPA Validation Request : ", e);

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		logger.info("Yes Bank UPI VPA Validation Response " + serverResponse.toString());
		return serverResponse.toString();
	}

	@SuppressWarnings("incomplete-switch")
	public String getResponse(JSONObject request, Fields fields) throws SystemException {

		StringBuilder serverResponse = new StringBuilder();

		String hostUrl = "";

		TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
		switch (transactionType) {
		case SALE:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.YES_BANKCB_UPI_SALE_URL);
			break;
		case REFUND:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.YES_BANKCB_UPI_REFUND_URL);
			break;
		case STATUS:
		case ENQUIRY:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.YES_BANKCB_UPI_STATUS_ENQUIRY_URL);
			
		}
		HttpsURLConnection connection = null;

		try {

//			SSLContext sc = getSSLContext(System.getenv(Constants.PG_PROPERTIES_PATH) + PropertiesManager.propertiesMap.get("YesBankJksName"));
			SSLContext sc = getSSLContextUpi();
			URL url = new URL(hostUrl);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setSSLSocketFactory(sc.getSocketFactory());

			connection.setRequestMethod("POST");
			connection.setRequestProperty("X-IBM-Client-Secret",
					PropertiesManager.propertiesMap.get("YESBANK_UPI_HEADER_CLIENT_SECRET"));
			connection.setRequestProperty("X-IBM-Client-ID",
					PropertiesManager.propertiesMap.get("YESBANK_UPI_HEADER_CLIENT_ID"));
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
						"Network Exception with Yes Bank Upi" + hostUrl.toString());
			}

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);

			}

			rd.close();

		} catch (Exception e) {

			logger.error("Exception occured while sending collect request with Yes Bank : ", e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		logger.info("Yes Bank UPI "+transactionType+" response " + serverResponse.toString() + " for Order Id = " + fields.get(FieldType.ORDER_ID.getName()));
		return serverResponse.toString();
	}

	private SSLContext getSSLContext(String path) throws SystemException {

		try {

			char[] password = PropertiesManager.propertiesMap.get("YesBankJksPassword").toCharArray();

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
			logger.error("Exception in getSSLContext , exsception = ", e);
		}
		return null;

	}
	
	private SSLContext getSSLContextUpi() {
		try {
			String pfxPath = PropertiesManager.propertiesMap.get(Constants.PAYMENT_GATEWAY_PFX_PATH);
			char[] passphrase = PropertiesManager.propertiesMap.get(Constants.PAYMENT_GATEWAY_PASSPHRASE).toCharArray();
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(new FileInputStream(pfxPath), passphrase);
			kmf.init(keyStore, passphrase);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
			return sslContext;
		} catch (Exception e) {
			logger.error("Error while creating ssl context");
			logger.error(e.getMessage());
			return null;
		}
	}

}
