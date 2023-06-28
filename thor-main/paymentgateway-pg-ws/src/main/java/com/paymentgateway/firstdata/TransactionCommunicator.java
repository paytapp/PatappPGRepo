package com.paymentgateway.firstdata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

@Service("firstDataTransactionCommunicator")
public class TransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	private static final String transactionUrl = ConfigurationConstants.FIRSTDATA_ENROLLMENT_URL.getValue();
	private static final String iciciAuthenticationUrl = ConfigurationConstants.ICICI_AUTHENTICATION_ID.getValue();
	private static final String icicicAuthenticationPassword = ConfigurationConstants.ICICI_AUTHENTICATION_PASSWORD
			.getValue();
	private static final String idfcAuthenticationUrl = ConfigurationConstants.IDFC_AUTHENTICATION_ID.getValue();
	private static final String idfcAuthenticationPassword = ConfigurationConstants.IDFC_AUTHENTICATION_PASSWORD
			.getValue();

	public String sendSoapMessage(SOAPMessage request, Fields fields) {
		String soapResponse = "";
		SOAPConnection conn = null;
		try {
			String acquirer = fields.get(FieldType.ACQUIRER_TYPE.getName());
			if (acquirer.equals(AcquirerType.ICICI_FIRSTDATA.getCode())) {
				Authenticator.setDefault(new MyAuthenticator(iciciAuthenticationUrl, icicicAuthenticationPassword));
			} else if (acquirer.equals(AcquirerType.IDFC_FIRSTDATA.getCode())) {
				String authUrl = fields.get(FieldType.TXN_KEY.getName());
				String authPass = fields.get(FieldType.PASSWORD.getName());
				Authenticator.setDefault(new MyAuthenticator(authUrl, authPass));
			}
			// Create the connection
			
			 URL url = new URL(transactionUrl);
			 	HttpsURLConnection httpsConnection = null;
			 // Create SSL context and trust all certificates
			 	
	            SSLContext sslContext = getSSLContext(fields);
	            HttpsURLConnection
	                    .setDefaultSSLSocketFactory(sslContext.getSocketFactory());
	            // Open HTTPS connection
	            httpsConnection = (HttpsURLConnection) url.openConnection();
	            // Trust all hosts
	            //httpsConnection.setHostnameVerifier(new TrustAllHosts());
	            // Connect
	            httpsConnection.connect();
			
			SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
			conn = scf.createConnection();

			logRequest(request, transactionUrl, fields);

			SOAPMessage rp = conn.call(request, transactionUrl);

			soapResponse = prepareSoapString(rp);

			logResponse(soapResponse, fields);

			// Close connection
			conn.close();

		} catch (Exception exception) {
			logger.error("Exception  " , exception);
		} finally {
			
			System.clearProperty("https.protocols");
			System.clearProperty("javax.net.ssl.keyStore");
			System.clearProperty("javax.net.ssl.keyStorePassword");
			System.clearProperty("java.net.useSystemProxies");
		}
		return soapResponse;
	}

	
	SSLContext getSSLContext(Fields fields) throws NoSuchAlgorithmException {
		SSLContext sc = SSLContext.getInstance("TLSv1.2");
		try {
			String jksPassword = fields.get(FieldType.ADF2.getName());
			String path = System.getenv("DTECH_PROPS")+fields.get(FieldType.ADF1.getName());
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
			logger.error("Exception " , e);
		}
		return sc;

	}
	
	public void logRequest(SOAPMessage soapRequest, String url, Fields fields) {
		log("Request message to first data: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + "Url= " + url + " " + prepareSoapString(soapRequest));
	}

	public void logResponse(String responseMessage, String url, Fields fields) {
		log("Response message from first data: TxnType = "+ fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " +  "Url= " + url + " " + responseMessage);
	}

	public void logRequest(String requestMessage, Fields fields) {
		log("Request message to first data: TxnType = "+ fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + requestMessage);
	}

	public void logResponse(String responseMessage, Fields fields) {
		log("Response message from first data: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + responseMessage);
	}

	private String prepareSoapString(SOAPMessage message) {
		ByteArrayOutputStream req = new ByteArrayOutputStream();
		try {
			message.writeTo(req);

			String reqMsg = new String(req.toByteArray());
			reqMsg = reqMsg.replaceAll(Constants.AMP, Constants.SEPARATOR);
			return reqMsg;
		} catch (SOAPException e) {
			logger.error("Exception in prepareSoapString , exsception = " , e);
		} catch (IOException e) {
			logger.error("Exception in prepareSoapString , exsception = " , e);
		}
		return "";
	}

	private void log(String message) {

		message = Pattern.compile("(CardNumber>)([\\s\\S]*?)(CardNumber>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(ExpMonth>)([\\s\\S]*?)(ExpMonth>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(ExpYear>)([\\s\\S]*?)(ExpYear>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(CardCodeValue>)([\\s\\S]*?)(CardCodeValue>)").matcher(message).replaceAll("$1$3");
		// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
		// fields.getCustomMDC());
		logger.info(message);
	}

}
