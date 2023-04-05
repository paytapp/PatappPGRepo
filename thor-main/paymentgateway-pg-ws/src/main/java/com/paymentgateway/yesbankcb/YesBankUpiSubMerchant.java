package com.paymentgateway.yesbankcb;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.HdfcUpiUtil;
import com.paymentgateway.pg.service.Transact;

@RestController
public class YesBankUpiSubMerchant {
	
	@Autowired
	@Qualifier("yesBankCbTransactionConverter")
	private TransactionConverter yesBankConverter;

	@Autowired
	private HdfcUpiUtil hdfcUpiUtil;
	@Autowired
	@Qualifier("yesBankCbFactory")
	private TransactionFactory transactionFactory;

	@Autowired
	@Qualifier("yesBankCbTransformer")
	private YesBankCbTransformer upiTransformer;

	@Autowired
	@Qualifier("yesBankCbTransactionCommunicator")
	private TransactionCommunicator yesBankCommunicator;
	
	private static Logger logger = LoggerFactory.getLogger(Transact.class.getName());
	
	@RequestMapping(value = "/yesUpiMerchantOnboarding", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> yesUpiSubMerchantOnboarding(@RequestBody Map<String, String> reqmap, HttpServletRequest req) {
		try {
			
			/*
			 * String uptimeToken = PropertiesManager.propertiesMap.get("UPTIME_TOKEN");
			 * String reqToken = req.getHeader("Authorization");
			 * 
			 * if(uptimeToken == null || !uptimeToken.equals(reqToken)) {
			 * logger.info("Request Token : " + reqToken); logger.error("Token mismatch");
			 * return new ResponseEntity<>("Invalid token", HttpStatus.BAD_REQUEST); }
			 */
			
			logger.info("yesUpiMerchantOnboarding test : " + reqmap);
			String encReq = encrypt(reqmap.get("requestMsg"), "d6f7e34d69ecbea251433b59035b4f78");
			JSONObject json = new JSONObject();
			json.put(Constants.REQUEST_MESSAGE, encReq);
			json.put(Constants.PG_MERCHANT_ID, PropertiesManager.propertiesMap.get("YesBankUpiPayoutMID"));
			String encResponse = getYesBankUPIResponse(json.toString(),
					PropertiesManager.propertiesMap.get("yesUpiSubMerchantOnboardingUrl"));
			JSONObject jsonResponse = new JSONObject(encResponse);

			String decResponse = decrypt(jsonResponse.getString("resp"), "d6f7e34d69ecbea251433b59035b4f78");
			return new ResponseEntity<>(decResponse, HttpStatus.OK);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ResponseEntity<>("Failed", HttpStatus.BAD_REQUEST);
	}
	
	public String getYesBankUPIResponse(String req, String serviceUrl) {
		StringBuilder responseBody = new StringBuilder();
		String clientId = PropertiesManager.propertiesMap.get("YESBANK_UPI_HEADER_CLIENT_ID");
		String clientSecret = PropertiesManager.propertiesMap.get("YESBANK_UPI_HEADER_CLIENT_SECRET");
		
		try {
			logger.info("Yes bank UPI Payout request : " + req);
			logger.info("Service URL : " + serviceUrl);
			HttpsURLConnection connection = null;
			SSLContext sc = getSSLContext();
			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setSSLSocketFactory(sc.getSocketFactory());

			connection.setRequestMethod("POST");
			connection.setRequestProperty("X-IBM-Client-ID", clientId);
			connection.setRequestProperty("X-IBM-Client-Secret", clientSecret);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Length", req);
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);

			// Send request
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(req.toString());
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			int code = ((HttpURLConnection) connection).getResponseCode();
			logger.info("Yes bank UPI Payout response code : " + code);
			while ((line = rd.readLine()) != null) {
				responseBody.append(line);
			}

			rd.close();

			logger.info("Yes bank Payout UPI response : " + responseBody.toString());
			return responseBody.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private SSLContext getSSLContext() {
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
	
	public String encrypt(String encryptedRequest, String key) {
		try {
			if (StringUtils.isBlank(key)) {
				key = PropertiesManager.propertiesMap.get(Constants.YES_BANKCB_UPI_KEY);
			}
			String encryptedString = "";
			try {
				encryptedString = hdfcUpiUtil.encrypt(encryptedRequest.toString(), key);
			} catch (Exception e) {
				logger.error("Exception : " + e.getMessage());
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
						"unknown exception in encrypt method for yes upi in TransactionConverter");
			}
			return encryptedString;
		} catch(Exception e) {
			logger.info("Exception while encrypting payout UPI request : ");
		}
		return "";
	}
	
	public String decrypt(String encryptedRequest, String key) throws SystemException {
		String decryptedString = "";
		if (StringUtils.isBlank(key)) {
			logger.info("Key not found in ADF 7");
			key = PropertiesManager.propertiesMap.get(Constants.YES_BANKCB_UPI_KEY);
		}
		try {
			decryptedString = hdfcUpiUtil.decrypt(encryptedRequest, key);
		} catch (Exception e) {
			logger.error("Exception : " + e.getMessage());
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in decrypt method for yes upi in TransactionConverter");
		}
		return decryptedString;

	}
	

}
