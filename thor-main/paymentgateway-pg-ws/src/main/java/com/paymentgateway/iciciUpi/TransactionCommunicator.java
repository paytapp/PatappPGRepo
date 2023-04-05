package com.paymentgateway.iciciUpi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Enumeration;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Rahul, Amitosh
 *
 */
@Service("iciciUpiTransactionCommunicator")
public class TransactionCommunicator {

	@Autowired
	PropertiesManager propertiesManager;

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	@SuppressWarnings("incomplete-switch")
	public String getResponse(String request, Fields fields) throws SystemException {

		String hostUrl = "";

		try {
			TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
			switch (transactionType) {
			case SALE:
			case AUTHORISE:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.ICICI_UPI_SALE_URL)
				+ fields.get(FieldType.MERCHANT_ID.getName());
				break;
			case ENROLL:
				break;
			case CAPTURE:
				break;
			case REFUND:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.ICICI_UPI_REFUND_URL)
						+ fields.get(FieldType.MERCHANT_ID.getName());
				break;
			case STATUS:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.ICICI_UPI_STATUS_ENQUIRY_URL)
						+ fields.get(FieldType.MERCHANT_ID.getName());
				break;
			}

			URL requestUrl = new URL(hostUrl);
			HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Content-Type", " text/plain");
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			logger.info("Encrypted Request is " + request);

			OutputStream os = connection.getOutputStream();
			os.write(request.getBytes());
			os.flush();
			os.close();

			int responseCode = connection.getResponseCode();
			logger.info("ICICI Status enquiry response code  " + responseCode);
			StringBuffer encryptedResponse = new StringBuffer();

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				encryptedResponse.append(inputLine);
			}
			in.close();
			logger.info("Encrypted Status enquiry response received form ICICI UPI, " + encryptedResponse);
			String decryptedResponse = decrypt(encryptedResponse.toString());
			logger.info("Decrypted Status enquiry response form ICICI UPI, " + decryptedResponse);
			return decryptedResponse;
		} catch (IOException ioException) {
			MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			logger.error("Network Exception with ICICIUPI", ioException);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, ioException,
					"Network Exception with ICICIUPI " + hostUrl.toString());
		}
	}

	public String decrypt(String response) {
		try {
			String fileName = Constants.PRIVATE_KEY_FILE_NAME;
			File file = new File(fileName);
			PrivateKey privateKey = getPrivateKey(file);
			byte[] bytes = Base64.getDecoder().decode(response.replace("\r\n", ""));
			Cipher decriptCipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
			decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

			return new String(decriptCipher.doFinal(bytes));
		} catch (Exception e) {

			return null;
		}
	}

	private PrivateKey getPrivateKey(File filename) {
		try {
			String alias = "";
			String pass = propertiesManager.propertiesMap.get("PaymentGatewayPfxFilePassword");
			char[] bytePass = pass.toCharArray();
			KeyStore ks = KeyStore.getInstance("pkcs12", "SunJSSE");
			ks.load(new FileInputStream(filename), bytePass);
			Enumeration es = ks.aliases();
			boolean isAliasWithPrivateKey = false;
			while (es.hasMoreElements()) {
				alias = (String) es.nextElement();
				if (isAliasWithPrivateKey = ks.isKeyEntry(alias)) {
					break;
				}
			}
			if (!isAliasWithPrivateKey) {
				return null;
			}
			KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias,
					new KeyStore.PasswordProtection(bytePass));
			return pkEntry.getPrivateKey();
		} catch (Exception e) {
			logger.error("Exception caugth : " , e);
			return null;
		}
	}
}
