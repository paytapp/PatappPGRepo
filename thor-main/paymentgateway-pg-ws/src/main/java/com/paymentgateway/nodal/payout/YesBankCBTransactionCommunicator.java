package com.paymentgateway.nodal.payout;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
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

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.SettlementTransactionType;

/**
 * @author Rahul
 *
 */
@Service("yesbankCBTransactionCommunicator")
public class YesBankCBTransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(YesBankCBTransactionCommunicator.class.getName());

	public String getFundTransferResponse(Fields fields, String request) {
		logger.info("Yes bank settlement request: " + request);
		HttpsURLConnection connection = null;
		StringBuilder serverResponse = new StringBuilder();
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		String hostUrl = "";
		if (txnType.equals(SettlementTransactionType.FUND_TRANSFER.getName())) {
			hostUrl = PropertiesManager.propertiesMap.get("YesBankCBFundTransferURL");
		} else if (txnType.equals(SettlementTransactionType.STATUS.getName())) {
			hostUrl = PropertiesManager.propertiesMap.get("YesBankCBStatusEnqURL");
		}

		try {

			SSLContext sslContext = getSSLContext(PropertiesManager.propertiesMap.get("YesBankCBJKSPath"));
			URL url = new URL(hostUrl);

			String userName = PropertiesManager.propertiesMap.get("YesBankCBBasicAuthId");
			String pass = PropertiesManager.propertiesMap.get("YesBankCBBasicAuthPassword");
			String authString = userName + ":" + pass;
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);

			connection = (HttpsURLConnection) url.openConnection();
			connection.setSSLSocketFactory(sslContext.getSocketFactory());
			connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("X-IBM-Client-Secret",
					PropertiesManager.propertiesMap.get("YesBankCBClientSecret"));
			connection.setRequestProperty("X-IBM-Client-Id", PropertiesManager.propertiesMap.get("YesBankCBClientId"));
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
			logger.info("Yes bank settlement Response: " + serverResponse.toString());
		} catch (Exception e) {
			logger.info("exception " + e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}

		}
		return serverResponse.toString();
	}

	public String getAddBeneficiaryResponse(String request) {
		logger.info("Yes bank Add beneficaiary request: " + request);
		String response = "";
		try {

			SSLContext sslContext = setupSslContext();
			String urls = PropertiesManager.propertiesMap.get("YesBankCBAddBeneficiaryURL");
			URL url = new URL(urls);
			URLConnection conn = url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setReadTimeout(20000);
			conn.setUseCaches(false);
			conn.setConnectTimeout(20000);
			HttpsURLConnection connection = (HttpsURLConnection) conn;
			connection.setRequestMethod("POST");

			// Add headers
			connection.addRequestProperty("Connection", "close");
			connection.addRequestProperty("Content-Length", String.valueOf(request.length()));
			connection.setRequestProperty("Content-Type", "application/xml");
			
			String userName = PropertiesManager.propertiesMap.get("YesBankCBBasicAuthId");
			String pass = PropertiesManager.propertiesMap.get("YesBankCBBasicAuthPassword");
			String authString = userName + ":" + pass;
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			connection.setRequestProperty("X-IBM-Client-Secret", PropertiesManager.propertiesMap.get("YesBankCBAddBeneClientSecret"));
			connection.setRequestProperty("X-IBM-Client-Id", PropertiesManager.propertiesMap.get("YesBankCBAddBeneClientId"));
			connection.setSSLSocketFactory(sslContext.getSocketFactory());
			// connection data
			DataOutputStream dataoutputstream = new DataOutputStream(connection.getOutputStream());
			dataoutputstream.writeBytes(request);
			dataoutputstream.flush();
			// dataoutputstream.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer serverResponse = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
				serverResponse.append('\r');
			}
			response = serverResponse.toString();
			logger.info("Yes bank add beneficiary response: " + response);
			rd.close();
		} catch (Exception e) {
			logger.info("Exception " + e);
		}
		return response;

	}

	public SSLContext setupSslContext() {

		try {
			String jksPassword = PropertiesManager.propertiesMap.get("YesBankCBJKSPassword");
			char[] password = jksPassword.toCharArray();
			KeyManagerFactory keyManagerFactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			KeyStore keyStore = KeyStore.getInstance("JKS");

			InputStream keyInput = new FileInputStream(new File(PropertiesManager.propertiesMap.get("YesBankCBJKSPath")));
			keyStore.load(keyInput, password);
			keyInput.close();
			keyManagerFactory.init(keyStore, password);
			// getKeyManagers
			KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

			SecureRandom secureRandom = new SecureRandom();

			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(keyManagers, null, secureRandom);
			return sc;
		} catch (Exception e) {
			logger.info("Exception " + e);
		}
		return null;
	}

	private SSLContext getSSLContext(String path) throws NoSuchAlgorithmException {
		SSLContext sc = SSLContext.getInstance("TLSv1.2");
		try {
			String jksPassword = PropertiesManager.propertiesMap.get("YesBankCBJKSPassword");
			char[] password = jksPassword.toCharArray();
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

			sc.init(keyManagers, null, secureRandom);

			return sc;
		}

		catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException
				| UnrecoverableKeyException | KeyManagementException e) {
			logger.info("Exception " + e);
		}
		return sc;

	}

}
