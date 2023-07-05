package com.paymentgateway.pg.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.primitives.Bytes;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;

/**
 * @author Shiva, Sandeep
 *
 */

@Service
public class IciciUtil {

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired		
	private Fields fields;

	private static Logger logger = LoggerFactory.getLogger(IciciUtil.class.getName());
	
	private static final String RSA_ALGO_ENC_DEC = "RSA/ECB/PKCS1Padding";
	private static final String AES_ALGO_ENC_DEC = "AES/CBC/PKCS5Padding";
	private static final String ADF_35 = "ADF_35";
	private static final String ADF_31 = "ADF_31";
	private static final String ADF_32 = "ADF_32";
	private static final String ADF_33 = "ADF_33";
	private static final String ADF_34 = "ADF_34";

	// Encrypt using ICICI RSA public key
	public String encrypt(String request) {

		try {
			logger.info("Icici Encryption Start for " + request);
			String fileName = System.getenv("PG_PROPS") + ""
					+ propertiesManager.propertiesMap.get("IciciPublicKeyName");
			File file = new File(fileName);
			PublicKey publicKey = getPublicKey(file);
			logger.info("Raw Request " + request);
			Cipher cipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			logger.info("Icici Encryption Finished");
			return Base64.getEncoder().encodeToString(cipher.doFinal(request.getBytes()));
		} catch (Exception e) {
			logger.error("Exception", e);
			return null;
		}
	}

	// Decrypt using Payment Gateway private key
	public String decrypt(String response) {
		try {
			// logger.info("Icici decryption Start");
			String fileName = System.getenv("PG_PROPS") + ""
					+ propertiesManager.propertiesMap.get("PaymentGatewayPrivateKeyName");
			File file = new File(fileName);
			PrivateKey privateKey = getPrivateKey(file);
			byte[] bytes = Base64.getDecoder().decode(response.replace("\r\n", ""));
			Cipher decriptCipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
			decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
			// logger.info("Icici decryption Finished");
			return new String(decriptCipher.doFinal(bytes));
		} catch (Exception e) {
			logger.error("Exception in decrypt()", e);
			return null;
		}
	}

	public String decryptCustom(String response) {
		logger.info("Inside decryptCustom()");
		try {
			// put response in json Object
			JSONObject json = new JSONObject(response);

			// get the sessionKey from Json
			String sessionKey = compositeBeneDecryptPayble(json.get("encryptedKey").toString());

			// Get the encrypted Data from JSON and decode in base64
			byte[] encryptedData = Base64.getDecoder().decode(json.get("encryptedData").toString());

			// Get Iv and data from encryptedData Hybrid IV + DATA
			byte[] getIv = Arrays.copyOfRange(encryptedData, 0, 16);
			byte[] getData = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);

			IvParameterSpec iv = new IvParameterSpec(getIv);
			SecretKeySpec skeySpec = new SecretKeySpec(sessionKey.getBytes(), "AES");

			// Started Decryption
			Cipher cipher = Cipher.getInstance(AES_ALGO_ENC_DEC);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] original = cipher.doFinal(getData);

			logger.info("Finished Decryption for ECollection Payble API , Data is " + new String(original));
			return new String(original);

		} catch (Exception e) {
			logger.error("Exception in ecollection , decryptCustom() for bebuoy merchant ", e);
			return null;
		}
	}

	public String decryptForAccountStatement(String response) {
		logger.info("Starting Decryption for Account Statement ");
		try {
			// put response in json Object
			JSONObject json = new JSONObject(response);

			// get the EncryptedKey from Json
			String decryptTheEncryptKey = decrypt(json.get("encryptedKey").toString());

			// Get the encrypted Data from JSON and decode in base64
			byte[] encryptedData = Base64.getDecoder().decode(json.get("encryptedData").toString());

			// Get Iv and data from encryptedData
			byte[] getIv = Arrays.copyOfRange(encryptedData, 0, 16);
			byte[] getData = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);

			IvParameterSpec iv = new IvParameterSpec(getIv);
			SecretKeySpec skeySpec = new SecretKeySpec(decryptTheEncryptKey.getBytes(), "AES");

			// Started Decryption
			Cipher cipher = Cipher.getInstance(AES_ALGO_ENC_DEC);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] original = cipher.doFinal(getData);

			logger.info("Finished Decryption for Account Statement orignal Data is " + new String(original));
			return new String(original);

		} catch (Exception e) {
			logger.error("exception in AES decryption ", e);
			return response;
		}
	}

	// Get ICICI Public Key
	private static PublicKey getPublicKey(File filename) {
		try {
			FileInputStream inputStream = new FileInputStream(filename);
			CertificateFactory cert = CertificateFactory.getInstance("X.509");
			X509Certificate cer = (X509Certificate) cert.generateCertificate(inputStream);
			inputStream.close();
			return cer.getPublicKey();

		} catch (Exception e) {
			logger.error("Exception : ", e);
			return null;
		}
	}

	public String eCollectionEncrypt(String plainText) throws Exception {
		// logger.info("Icici eCollection Encryption Started");
		try {
			String fileName = System.getenv("PG_PROPS") + propertiesManager.propertiesMap.get("ICICI_PUB_KEY");
			File file = new File(fileName);
			PublicKey publicKey = getPublicKey(file);
			Cipher cipher = Cipher.getInstance(RSA_ALGO_ENC_DEC);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return null;
		}
	}

	// Get Payment Gateway Private key from PFX file
	private PrivateKey getPrivateKey(File filename) {

		try {
			String alias = "";
			String pass = propertiesManager.propertiesMap.get("PaymentGatewayPfxFilePassword");
			char[] bytePass = pass.toCharArray();

			KeyStore ks = KeyStore.getInstance("pkcs12", "SunJSSE");
			ks.load(new FileInputStream(filename), bytePass);

			// iterate over all aliases
			Enumeration es = ks.aliases();

			boolean isAliasWithPrivateKey = false;
			while (es.hasMoreElements()) {
				alias = (String) es.nextElement();
				// if alias refers to a private key break at that point as we
				// want to use that certificate
				if (isAliasWithPrivateKey = ks.isKeyEntry(alias)) {
					break;
				}
			}
			if (!isAliasWithPrivateKey) {
				logger.info("No Private key found in file");
				return null;
			}
			KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias,
					new KeyStore.PasswordProtection(bytePass));

			return pkEntry.getPrivateKey();

		} catch (Exception e) {
			logger.error("Exception : ", e);
			return null;
		}

	}

	// Get Payble Private key from PFX file
	private PrivateKey getPrivateKeyCustom(File filename) {

		try {
			String alias = "";
			String pass = propertiesManager.propertiesMap.get("PayblePfxFilePassword");
			char[] bytePass = pass.toCharArray();

			KeyStore ks = KeyStore.getInstance("pkcs12", "SunJSSE");
			ks.load(new FileInputStream(filename), bytePass);

			// iterate over all aliases
			Enumeration es = ks.aliases();

			boolean isAliasWithPrivateKey = false;
			while (es.hasMoreElements()) {
				alias = (String) es.nextElement();
				// if alias refers to a private key break at that point as we
				// want to use that certificate
				if (isAliasWithPrivateKey = ks.isKeyEntry(alias)) {
					break;
				}
			}
			if (!isAliasWithPrivateKey) {
				logger.info("No Private key found in file");
				return null;
			}
			KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias,
					new KeyStore.PasswordProtection(bytePass));

			return pkEntry.getPrivateKey();

		} catch (Exception e) {
			logger.error("Exception : ", e);
			return null;
		}

	}

	public void IciciCibResponseHandler(JSONObject json, String txnId) {
		logger.info("Inside IciciCibResponseHandler()");
		logger.info("Inside IciciCibResponseHandler() callback transactionId " + txnId);
		logger.info("Inside IciciCibResponseHandler() callback Json " + json);
		try {
			fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());

			if (json.has("TXNSTATUS")) {
				String status = (String) json.get("TXNSTATUS");
				fields.put(fields.fetchPreviousCibFields(txnId));

				if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PROCESSING.name())) {
					fields.put(FieldType.REQID.getName(), (String) json.get("REQUESTID"));
					fields.put(FieldType.PG_REF_NUM.getName(), (String) json.get(FieldType.UNIQUE_ID.getName()));

					if (status.equalsIgnoreCase("SUCCESS")) {

						fields.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.name());
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.SUCCESS.name());
						fields.put(FieldType.PG_TXN_STATUS.getName(), status);
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
						fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.SUCCESS.getResponseCode());

					} else if (status.equalsIgnoreCase("FAILURE")) {

						fields.put(FieldType.STATUS.getName(), ErrorType.FAILED.name());
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.FAILED.name());
						fields.put(FieldType.PG_TXN_STATUS.getName(), status);
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getResponseCode());
						fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.FAILED.getResponseCode());

					} else if (status.equalsIgnoreCase("PENDING")) {

						fields.put(FieldType.STATUS.getName(), ErrorType.PENDING.name());
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.PENDING.name());
						fields.put(FieldType.PG_TXN_STATUS.getName(), status);
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PENDING.getResponseCode());
						fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.PENDING.getResponseCode());

					} else if (status.equalsIgnoreCase("Pending For Processing")) {

						fields.put(FieldType.STATUS.getName(), ErrorType.PROCESSING.name());
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.PROCESSING.name());
						fields.put(FieldType.PG_TXN_STATUS.getName(), status);
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PROCESSING.getResponseCode());
						fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.PROCESSING.getResponseCode());

					} else if (status.equalsIgnoreCase("DUPLICATE")) {

						fields.put(FieldType.STATUS.getName(), ErrorType.DUPLICATE.name());
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.DUPLICATE.name());
						fields.put(FieldType.PG_TXN_STATUS.getName(), status);
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getResponseCode());
						fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.DUPLICATE.getResponseCode());

					}
					fields.put(FieldType.UTR_NO.getName(), (String) json.get("UTRNUMBER"));
					fields.put(FieldType.REQID.getName(), (String) json.get("REQUESTID"));

					fields.insertIciciCibFields(fields);

				} else if (fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PENDING.name())) {

					fields.put(FieldType.REQID.getName(), (String) json.get("REQUESTID"));
					fields.put(FieldType.PG_REF_NUM.getName(), (String) json.get(FieldType.UNIQUE_ID.getName()));

					if (status.equalsIgnoreCase("SUCCESS")) {

						fields.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.name());
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.SUCCESS.name());
						fields.put(FieldType.PG_TXN_STATUS.getName(), status);
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
						fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.SUCCESS.getResponseCode());

					} else if (status.equalsIgnoreCase("FAILURE")) {

						fields.put(FieldType.STATUS.getName(), ErrorType.FAILED.name());
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.FAILED.name());
						fields.put(FieldType.PG_TXN_STATUS.getName(), status);
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getResponseCode());
						fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.FAILED.getResponseCode());

					} else if (status.equalsIgnoreCase("PENDING")) {

						fields.put(FieldType.STATUS.getName(), ErrorType.PENDING.name());
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.PENDING.name());
						fields.put(FieldType.PG_TXN_STATUS.getName(), status);
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PENDING.getResponseCode());
						fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.PENDING.getResponseCode());

					} else if (status.equalsIgnoreCase("Pending For Processing")) {

						fields.put(FieldType.STATUS.getName(), ErrorType.PROCESSING.name());
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.PROCESSING.name());
						fields.put(FieldType.PG_TXN_STATUS.getName(), status);
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PROCESSING.getResponseCode());
						fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.PROCESSING.getResponseCode());

					} else if (status.equalsIgnoreCase("DUPLICATE")) {

						fields.put(FieldType.STATUS.getName(), ErrorType.DUPLICATE.name());
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.DUPLICATE.name());
						fields.put(FieldType.PG_TXN_STATUS.getName(), status);
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getResponseCode());
						fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.DUPLICATE.getResponseCode());

					}
					fields.put(FieldType.UTR_NO.getName(), (String) json.get("UTRNUMBER"));
					fields.put(FieldType.REQID.getName(), (String) json.get("REQUESTID"));
				}

				fields.insertIciciCibFields(fields);
			} else {
				return;
			}

			logger.info("Response Storeing in DB");
			fields.insertIciciCibFields(fields);
			logger.info("Response Stored in DB");
		} catch (Exception e) {
			logger.error("Exception in creating Fields for DB in ICICI CIB : ", e);

		}
	}

	public String compositeApiEncryption(String request, boolean isPaybleMerchant) {
		logger.info("Starting Encryption for Composite Api request " + request);
		try {
			JSONObject jsonPayload = new JSONObject();

			String sessionKey = getRandomSecretKey();
			// logger.info("session Key " + sessionKey);

			// Creating Base64 EncryptedKey using session key
			String encryptedKey;

			if (isPaybleMerchant) {
				encryptedKey = compositeEncryptPayble(sessionKey);
			} else {
				encryptedKey = compositeEncrypt(sessionKey);
			}

			IvParameterSpec ivParameterSpec = getIV();

			SecretKeySpec secretKeySpec = new SecretKeySpec(sessionKey.getBytes(), "AES");

			Cipher cipher = Cipher.getInstance(AES_ALGO_ENC_DEC);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

			// Encrypt input text
			byte[] encrypted = cipher.doFinal(request.getBytes());
			byte[] totalData = Bytes.concat(ivParameterSpec.getIV(), encrypted);
			String encryptedData = Base64.getEncoder().encodeToString(totalData);

			jsonPayload.put("encryptedKey", encryptedKey);
			jsonPayload.put("encryptedData", encryptedData);
			jsonPayload.put("iv", "");
			jsonPayload.put("requestId", "");
			jsonPayload.put("service", "PaymentApi");
			jsonPayload.put("oaepHashingAlgorithm", "NONE");
			jsonPayload.put("clientInfo", "");
			jsonPayload.put("optionalParam", "");
			logger.info("Finished Encryption for Composite API orignal Data is " + request + " Encrypted Request is "
					+ jsonPayload.toString());
			return jsonPayload.toString();

		} catch (Exception e) {
			logger.error("Exception in Composite AES Encryption for Request " + request + " ", e);
			return null;
		}
	}
	public String CIBCompositeApiEncryption(String request) {
		logger.info("Starting Encryption for CIBCompositeApiEncryption() request " + request);
		try {
			JSONObject jsonPayload = new JSONObject();

			String sessionKey = getRandomSecretKey();
			// logger.info("session Key " + sessionKey);

			// Creating Base64 EncryptedKey using session key
			String encryptedKey = encrypt(sessionKey);
			
			if(StringUtils.isBlank(encryptedKey)){
				logger.info("empty enc key recived CIBCompositeApiEncryption()");
				return null;
			}

			IvParameterSpec ivParameterSpec = getIV();

			SecretKeySpec secretKeySpec = new SecretKeySpec(sessionKey.getBytes(), "AES");

			Cipher cipher = Cipher.getInstance(AES_ALGO_ENC_DEC);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

			// Encrypt input text
			byte[] encrypted = cipher.doFinal(request.getBytes());
			byte[] totalData = Bytes.concat(ivParameterSpec.getIV(), encrypted);
			String encryptedData = Base64.getEncoder().encodeToString(totalData);

			jsonPayload.put("encryptedKey", encryptedKey);
			jsonPayload.put("encryptedData", encryptedData);
			jsonPayload.put("iv", "");
			jsonPayload.put("requestId", "");
			jsonPayload.put("service", "PaymentApi");
			jsonPayload.put("oaepHashingAlgorithm", "NONE");
			jsonPayload.put("clientInfo", "");
			jsonPayload.put("optionalParam", "");
			logger.info("Finished Encryption for Composite API orignal Data is " + request + " Encrypted Request is "
					+ jsonPayload.toString());
			return jsonPayload.toString();

		} catch (Exception e) {
			logger.error("Exception in Composite AES Encryption for Request " + request + " ", e);
			return null;
		}
	}
	
	public String compositeApiEncryption(String request, JSONObject adfFields) {
		logger.info("Starting Encryption for Composite Api request " + request);
		try {
			JSONObject jsonPayload = new JSONObject();

			String sessionKey = getRandomSecretKey();
			// logger.info("session Key " + sessionKey);

			// Creating Base64 EncryptedKey using session key
			String encryptedKey = compositeEncrypt(sessionKey,adfFields);
			
			IvParameterSpec ivParameterSpec = getIV();

			SecretKeySpec secretKeySpec = new SecretKeySpec(sessionKey.getBytes(), "AES");

			Cipher cipher = Cipher.getInstance(AES_ALGO_ENC_DEC);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

			// Encrypt input text
			byte[] encrypted = cipher.doFinal(request.getBytes());
			byte[] totalData = Bytes.concat(ivParameterSpec.getIV(), encrypted);
			String encryptedData = Base64.getEncoder().encodeToString(totalData);

			jsonPayload.put("encryptedKey", encryptedKey);
			jsonPayload.put("encryptedData", encryptedData);
			jsonPayload.put("iv", "");
			jsonPayload.put("requestId", "");
			jsonPayload.put("service", "PaymentApi");
			jsonPayload.put("oaepHashingAlgorithm", "NONE");
			jsonPayload.put("clientInfo", "");
			jsonPayload.put("optionalParam", "");
			logger.info("Finished Encryption for Composite API orignal Data is " + request + " Encrypted Request is "
					+ jsonPayload.toString());
			return jsonPayload.toString();

		} catch (Exception e) {
			logger.error("Exception in Composite AES Encryption for Request " + request + " ", e);
			return null;
		}
	}

	public String ecollectionApiEncryptionPayble(String request, boolean isPaybleMerchant) {
		logger.info("Starting Encryption for Composite Api request " + request);
		try {
			JSONObject jsonPayload = new JSONObject();

			String sessionKey = getRandomSecretKey();
			// logger.info("session Key " + sessionKey);

			// Creating Base64 EncryptedKey using session key
			String encryptedKey = eCollectionEncryptPayble(sessionKey);

			IvParameterSpec ivParameterSpec = getIV();

			SecretKeySpec secretKeySpec = new SecretKeySpec(sessionKey.getBytes(), "AES");

			Cipher cipher = Cipher.getInstance(AES_ALGO_ENC_DEC);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

			// Encrypt input text
			byte[] encrypted = cipher.doFinal(request.getBytes());
			byte[] totalData = Bytes.concat(ivParameterSpec.getIV(), encrypted);
			String encryptedData = Base64.getEncoder().encodeToString(totalData);

			jsonPayload.put("encryptedKey", encryptedKey);
			jsonPayload.put("encryptedData", encryptedData);
			jsonPayload.put("iv", "");
			jsonPayload.put("requestId", "");
			jsonPayload.put("service", "PaymentApi");
			jsonPayload.put("oaepHashingAlgorithm", "NONE");
			jsonPayload.put("clientInfo", "");
			jsonPayload.put("optionalParam", "");
			logger.info("Finished Encryption for Composite API orignal Data is " + request + " Encrypted Request is "
					+ jsonPayload.toString());
			return jsonPayload.toString();

		} catch (Exception e) {
			logger.error("Exception in Composite AES Encryption for Request " + request + " ", e);
			return null;
		}
	}

	public String ecollectionApiEncryptionComposite(String request) {
		logger.info("Starting Encryption for eCollection Composite Api request " + request);
		try {
			JSONObject jsonPayload = new JSONObject();

			String sessionKey = getRandomSecretKey();
			// logger.info("session Key " + sessionKey);

			// Creating Base64 EncryptedKey using session key
			String encryptedKey = eCollectionEncryptComposite(sessionKey);

			IvParameterSpec ivParameterSpec = getIV();

			SecretKeySpec secretKeySpec = new SecretKeySpec(sessionKey.getBytes(), "AES");

			Cipher cipher = Cipher.getInstance(AES_ALGO_ENC_DEC);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

			// Encrypt input text
			byte[] encrypted = cipher.doFinal(request.getBytes());
			byte[] totalData = Bytes.concat(ivParameterSpec.getIV(), encrypted);
			String encryptedData = Base64.getEncoder().encodeToString(totalData);

			jsonPayload.put("encryptedKey", encryptedKey);
			jsonPayload.put("encryptedData", encryptedData);
			jsonPayload.put("iv", "");
			jsonPayload.put("requestId", "");
			jsonPayload.put("service", "PaymentApi");
			jsonPayload.put("oaepHashingAlgorithm", "NONE");
			jsonPayload.put("clientInfo", "");
			jsonPayload.put("optionalParam", "");
			logger.info("Finished Encryption for Composite API orignal Data is " + request + " Encrypted Request is "
					+ jsonPayload.toString());
			return jsonPayload.toString();

		} catch (Exception e) {
			logger.error("Exception in Composite AES Encryption for Request " + request + " ", e);
			return null;
		}
	}
	
	public String CibCompositeApiDecryption(String response) {
		logger.info("Starting Decryption CibCompositeApiDecryption() " + response);
		try {
			// put response in json Object
			JSONObject json = new JSONObject(response);

			// get the sessionKey from Json
			String sessionKey = CibCompositeDecrypt(json.get("encryptedKey").toString());

			logger.info("Session key of ICICI " + sessionKey);

			// Get the encrypted Data from JSON and decode in base64
			byte[] encryptedData = Base64.getDecoder().decode(json.get("encryptedData").toString());

			// Get Iv and data from encryptedData
			byte[] getIv = Arrays.copyOfRange(encryptedData, 0, 16);
			byte[] getData = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);

			IvParameterSpec iv = new IvParameterSpec(getIv);
			SecretKeySpec skeySpec = new SecretKeySpec(sessionKey.getBytes(), "AES");

			// Started Decryption
			Cipher cipher = Cipher.getInstance(AES_ALGO_ENC_DEC);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] original = cipher.doFinal(getData);

			logger.info("Finished Decryption for Composite API , Data is " + new String(original));
			return new String(original);

		} catch (Exception e) {
			logger.error("exception in AES decryption in Composite API ", e);
			return response;
		}
	}

	public String compositeApiDecryption(String response, boolean isPaybleMerchant) {
		logger.info("Starting Decryption for Composite Api" + response);
		try {
			// put response in json Object
			JSONObject json = new JSONObject(response);

			// get the sessionKey from Json
			String sessionKey;

			if (isPaybleMerchant) {
				sessionKey = compositeDecryptPayble(json.get("encryptedKey").toString());
			} else {
				sessionKey = compositeDecrypt(json.get("encryptedKey").toString());
			}

			logger.info("Session key of ICICI " + sessionKey);

			// Get the encrypted Data from JSON and decode in base64
			byte[] encryptedData = Base64.getDecoder().decode(json.get("encryptedData").toString());

			// Get Iv and data from encryptedData
			byte[] getIv = Arrays.copyOfRange(encryptedData, 0, 16);
			byte[] getData = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);

			IvParameterSpec iv = new IvParameterSpec(getIv);
			SecretKeySpec skeySpec = new SecretKeySpec(sessionKey.getBytes(), "AES");

			// Started Decryption
			Cipher cipher = Cipher.getInstance(AES_ALGO_ENC_DEC);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] original = cipher.doFinal(getData);

			logger.info("Finished Decryption for Composite API , Data is " + new String(original));
			return new String(original);

		} catch (Exception e) {
			logger.error("exception in AES decryption in Composite API ", e);
			return response;
		}
	}

	public String compositeApiBeneDecryption(String response, boolean isPaybleMerchant) {
		logger.info("Starting Decryption for Composite Bene Api" + response);
		try {
			// put response in json Object
			JSONObject json = new JSONObject(response);

			// get the sessionKey from Json
			String sessionKey;

			if (isPaybleMerchant) {
				sessionKey = compositeBeneDecryptPayble(json.get("encryptedKey").toString());
			} else {
				sessionKey = compositeBeneDecrypt(json.get("encryptedKey").toString());
			}

			logger.info("Session key of ICICI " + sessionKey);

			// Get the encrypted Data from JSON and decode in base64
			byte[] encryptedData = Base64.getDecoder().decode(json.get("encryptedData").toString());

			// Get Iv and data from encryptedData
			byte[] getIv = Arrays.copyOfRange(encryptedData, 0, 16);
			byte[] getData = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);

			IvParameterSpec iv = new IvParameterSpec(getIv);
			SecretKeySpec skeySpec = new SecretKeySpec(sessionKey.getBytes(), "AES");

			// Started Decryption
			Cipher cipher = Cipher.getInstance(AES_ALGO_ENC_DEC);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] original = cipher.doFinal(getData);

			logger.info("Finished Decryption for Composite API , Data is " + new String(original));
			return new String(original);

		} catch (Exception e) {
			logger.error("exception in AES decryption in Composite API ", e);
			return response;
		}
	}
	
	public String cibCompositeApiBeneDecryption(String response) {
		logger.info("Starting Decryption for cibCompositeApiBeneDecryption() " + response);
		try {
			// put response in json Object
			JSONObject json = new JSONObject(response);

			// get the sessionKey from Json
			String sessionKey = decrypt(json.get("encryptedKey").toString());
			
			logger.info("Session key of ICICI " + sessionKey);

			// Get the encrypted Data from JSON and decode in base64
			byte[] encryptedData = Base64.getDecoder().decode(json.get("encryptedData").toString());

			// Get Iv and data from encryptedData
			byte[] getIv = Arrays.copyOfRange(encryptedData, 0, 16);
			byte[] getData = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);

			IvParameterSpec iv = new IvParameterSpec(getIv);
			SecretKeySpec skeySpec = new SecretKeySpec(sessionKey.getBytes(), "AES");

			// Started Decryption
			Cipher cipher = Cipher.getInstance(AES_ALGO_ENC_DEC);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] original = cipher.doFinal(getData);

			logger.info("Finished Decryption for Composite API , Data is " + new String(original));
			return new String(original);

		} catch (Exception e) {
			logger.error("exception in AES decryption in Composite API ", e);
			return response;
		}
	}
	
	public String compositeApiDecryption(String response, JSONObject adfFields) {
		logger.info("Starting Decryption for Composite Api" + response);
		try {
			// put response in json Object
			JSONObject json = new JSONObject(response);

			// get the sessionKey from Json
			String sessionKey = compositeDecrypt(json.get("encryptedKey").toString(),adfFields);

			logger.info("Session key of ICICI " + sessionKey);

			// Get the encrypted Data from JSON and decode in base64
			byte[] encryptedData = Base64.getDecoder().decode(json.get("encryptedData").toString());

			// Get Iv and data from encryptedData
			byte[] getIv = Arrays.copyOfRange(encryptedData, 0, 16);
			byte[] getData = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);

			IvParameterSpec iv = new IvParameterSpec(getIv);
			SecretKeySpec skeySpec = new SecretKeySpec(sessionKey.getBytes(), "AES");

			// Started Decryption
			Cipher cipher = Cipher.getInstance(AES_ALGO_ENC_DEC);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] original = cipher.doFinal(getData);

			logger.info("Finished Decryption for Composite API , Data is " + new String(original));
			return new String(original);

		} catch (Exception e) {
			logger.error("exception in AES decryption in Composite API ", e);
			return response;
		}
	}

	public String compositeApiBeneDecryption(String response, JSONObject adfFields) {
		logger.info("Starting Decryption for Composite Bene Api" + response);
		try {
			// put response in json Object
			JSONObject json = new JSONObject(response);

			// get the sessionKey from Json
			String sessionKey = compositeBeneDecrypt(json.get("encryptedKey").toString(),adfFields);
			

			logger.info("Session key of ICICI " + sessionKey);

			// Get the encrypted Data from JSON and decode in base64
			byte[] encryptedData = Base64.getDecoder().decode(json.get("encryptedData").toString());

			// Get Iv and data from encryptedData
			byte[] getIv = Arrays.copyOfRange(encryptedData, 0, 16);
			byte[] getData = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);

			IvParameterSpec iv = new IvParameterSpec(getIv);
			SecretKeySpec skeySpec = new SecretKeySpec(sessionKey.getBytes(), "AES");

			// Started Decryption
			Cipher cipher = Cipher.getInstance(AES_ALGO_ENC_DEC);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] original = cipher.doFinal(getData);

			logger.info("Finished Decryption for Composite API , Data is " + new String(original));
			return new String(original);

		} catch (Exception e) {
			logger.error("exception in AES decryption in Composite API ", e);
			return response;
		}
	}

	// Encrypt using ICICI RSA public key
	public String compositeEncrypt(String request) {

		try {
			logger.info("Icici Encryption Start for ");
			String fileName = System.getenv("PG_PROPS") + ""
					+ propertiesManager.propertiesMap.get("ICICI_COMPOSITE_PUBLIC_KEY");
			File file = new File(fileName);
			PublicKey publicKey = getPublicKey(file);
			logger.info("Raw Request " + request);
			Cipher cipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			logger.info("Icici Encryption Finished ");
			return Base64.getEncoder().encodeToString(cipher.doFinal(request.getBytes()));
		} catch (Exception e) {
			logger.error("Exception in Composite Api encryption", e);
			return null;
		}
	}

	public String compositeDecrypt(String response) {
		try {
			logger.info("Icici Composite decryption Start");
			String fileName = System.getenv("PG_PROPS") + ""
					+ propertiesManager.propertiesMap.get("ICICI_COMPOSITE_PAYMENT_GATEWAY_PRIVATE_KEY");
			logger.info("Private Key Name " + fileName);
			File file = new File(fileName);
			String pass = propertiesManager.propertiesMap.get("ICICI_COMPOSITE_PAYMENT_GATEWAY_PFX_PWD");
			PrivateKey privateKey = getCompositePrivateKey(file, pass);
			byte[] bytes = Base64.getDecoder().decode(response.replace("\r\n", ""));
			Cipher decriptCipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
			decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
			logger.info("Icici Composite decryption Finished");
			return new String(decriptCipher.doFinal(bytes));
		} catch (Exception e) {
			logger.error("Exception in Composite Decryption ", e);
			return null;

		}
	}
	
	// Encrypt using ICICI RSA public key
	public String CibCompositeEncrypt(String request) {

		try {
			logger.info("Icici Encryption Start for ");
			String fileName = System.getenv("PG_PROPS") + ""
					+ propertiesManager.propertiesMap.get("NODAL_PUBLIC_KEY");
			File file = new File(fileName);
			PublicKey publicKey = getPublicKey(file);
			logger.info("Raw Request " + request);
			Cipher cipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			logger.info("CibCompositeEncrypt() Encryption Finished ");
			return Base64.getEncoder().encodeToString(cipher.doFinal(request.getBytes()));
		} catch (Exception e) {
			logger.error("Exception in CibCompositeEncrypt() encryption", e);
			return null;
		}
	}

	public String CibCompositeDecrypt(String response) {
		try {
			logger.info("Icici Composite decryption Start");
			String fileName = System.getenv("PG_PROPS") + ""
					+ propertiesManager.propertiesMap.get("NODAL_TXN_PRIVATE_KEY");
			logger.info("Private Key Name " + fileName);
			File file = new File(fileName);
			String pass = propertiesManager.propertiesMap.get("NODAL_TXN_PRIVATE_KEY_PASSWORD");
			PrivateKey privateKey = getCompositePrivateKey(file, pass);
			byte[] bytes = Base64.getDecoder().decode(response.replace("\r\n", ""));
			Cipher decriptCipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
			decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
			logger.info("Icici Composite decryption Finished");
			return new String(decriptCipher.doFinal(bytes));
		} catch (Exception e) {
			logger.error("Exception in Composite Decryption ", e);
			return null;

		}
	}
	
	// Encrypt using ICICI RSA public key
		public String compositeEncrypt(String request,JSONObject adfFields) {

			try {
				logger.info("Icici Encryption Start for ");
				String fileName = System.getenv("PG_PROPS") + ""
						+ adfFields.getString(ADF_35);
				File file = new File(fileName);
				PublicKey publicKey = getPublicKey(file);
				logger.info("Raw Request " + request);
				Cipher cipher = Cipher.getInstance(RSA_ALGO_ENC_DEC);
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				logger.info("Icici Encryption Finished ");
				return Base64.getEncoder().encodeToString(cipher.doFinal(request.getBytes()));
			} catch (Exception e) {
				logger.error("Exception in Composite Api encryption", e);
				return null;
			}
		}

		public String compositeDecrypt(String response,JSONObject adfFields) {
			try {
				logger.info("Icici Composite decryption Start");
				String fileName = System.getenv("PG_PROPS") + ""
						+ adfFields.getString(ADF_31);
				logger.info("Private Key Name " + fileName);
				File file = new File(fileName);
				String pass = adfFields.getString(ADF_32);
				PrivateKey privateKey = getCompositePrivateKey(file, pass);
				byte[] bytes = Base64.getDecoder().decode(response.replace("\r\n", ""));
				Cipher decriptCipher = Cipher.getInstance(RSA_ALGO_ENC_DEC);
				decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
				logger.info("Icici Composite decryption Finished");
				return new String(decriptCipher.doFinal(bytes));
			} catch (Exception e) {
				logger.error("Exception in Composite Decryption ", e);
				return null;

			}
		}
		
		public String compositeBeneDecrypt(String response, JSONObject adfFields) {
			try {
				logger.info("Icici Composite Bene decryption Start");
				String fileName = System.getenv("PG_PROPS") + ""
						+  adfFields.getString(ADF_33);
				logger.info("Private Key Name " + fileName);
				File file = new File(fileName);
				String pass =  adfFields.getString(ADF_34);
				PrivateKey privateKey = getCompositePrivateKey(file, pass);
				byte[] bytes = Base64.getDecoder().decode(response.replace("\r\n", ""));
				Cipher decriptCipher = Cipher.getInstance(propertiesManager.propertiesMap.get(RSA_ALGO_ENC_DEC));
				decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
				logger.info("Icici Composite decryption Finished");
				return new String(decriptCipher.doFinal(bytes));
			} catch (Exception ex) {
				logger.error("Exception in Composite Bene Decryption ", ex);
				return null;
			}
		}

	public String compositeBeneDecrypt(String response) {
		try {
			logger.info("Icici Composite Bene decryption Start");
			String fileName = System.getenv("PG_PROPS") + ""
					+ propertiesManager.propertiesMap.get("ICICI_COMPOSITE_PAYMENT_GATEWAY_BENE_PRIVATE_KEY");
			logger.info("Private Key Name " + fileName);
			File file = new File(fileName);
			String pass = propertiesManager.propertiesMap.get("ICICI_COMPOSITE_PAYMENT_GATEWAY_BENE_BENE_PFX_PWD");
			PrivateKey privateKey = getCompositePrivateKey(file, pass);
			byte[] bytes = Base64.getDecoder().decode(response.replace("\r\n", ""));
			Cipher decriptCipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
			decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
			logger.info("Icici Composite decryption Finished");
			return new String(decriptCipher.doFinal(bytes));
		} catch (Exception ex) {
			logger.error("Exception in Composite Bene Decryption ", ex);
			return null;
		}
	}

	// PAYBLE ENCRYPTION
	public String compositeEncryptPayble(String request) {
		logger.info("inside compositeEncryptPayble()");
		try {
			logger.info("Icici Encryption Start for ");
			String fileName = System.getenv("PG_PROPS") + ""
					+ propertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_PUBLIC_KEY");
			File file = new File(fileName);
			PublicKey publicKey = getPublicKey(file);
			logger.info("Raw Request " + request);
			Cipher cipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			logger.info("Icici Encryption Finished ");
			return Base64.getEncoder().encodeToString(cipher.doFinal(request.getBytes()));
		} catch (Exception e) {
			logger.error("Exception in Composite Payble Api encryption", e);
			return null;
		}
	}

	// PAYBLE ENCRYPTION For eCollection
	public String eCollectionEncryptPayble(String request) {
		logger.info("inside eCollectionEncryptPayble()");
		try {
			logger.info("Icici Encryption Start for ");
			String fileName = System.getenv("PG_PROPS") + ""
					+ propertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_PUBLIC_KEY_ECOLLECTION");
			File file = new File(fileName);
			PublicKey publicKey = getPublicKey(file);
			logger.info("Raw Request " + request);
			Cipher cipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			logger.info("Icici Encryption Finished ");
			return Base64.getEncoder().encodeToString(cipher.doFinal(request.getBytes()));
		} catch (Exception e) {
			logger.error("Exception in eCollection Composite Payble Api encryption", e);
			return null;
		}
	}

	// ENCRYPTION For eCollection
	public String eCollectionEncryptComposite(String request) {
		logger.info("inside eCollectionEncryptComposite()");
		try {
			logger.info("Icici Encryption Start for ");
			String fileName = System.getenv("PG_PROPS") + ""
					+ propertiesManager.propertiesMap.get("ICICI_COMPOSITE_PUBLIC_KEY_ECOLLECTION");
			File file = new File(fileName);
			PublicKey publicKey = getPublicKey(file);
			logger.info("Raw Request " + request);
			Cipher cipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			logger.info("Icici Encryption Finished ");
			return Base64.getEncoder().encodeToString(cipher.doFinal(request.getBytes()));
		} catch (Exception e) {
			logger.error("Exception in eCollection Composite Api encryption", e);
			return null;
		}
	}

	// PAYBLE Decryption
	public String compositeDecryptPayble(String response) {
		try {
			logger.info("Icici payble Composite decryption Start");
			String fileName = System.getenv("PG_PROPS") + ""
					+ propertiesManager.propertiesMap.get("ICICI_COMPOSITE_PAYMENT_GATEWAY_PRIVATE_KEY");
			logger.info("Private Key Name " + fileName);
			File file = new File(fileName);
			String pass = propertiesManager.propertiesMap.get("ICICI_COMPOSITE_PAYMENT_GATEWAY_PFX_PWD");
			PrivateKey privateKey = getCompositePrivateKey(file, pass);
			byte[] bytes = Base64.getDecoder().decode(response.replace("\r\n", ""));
			Cipher decriptCipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
			decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
			logger.info("Icici Composite decryption Finished");
			return new String(decriptCipher.doFinal(bytes));
		} catch (Exception e) {
			logger.error("Exception in payble Composite Decryption ", e);
			return null;
		}

	}

	// PAYBLE Decryption
	public String compositeBeneDecryptPayble(String response) {
		try {
			logger.info("Icici Payble Composite Bene decryption Start");
			String fileName = System.getenv("PG_PROPS") + ""
					+ propertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_PAYMENT_GATEWAY_BENE_PRIVATE_KEY");
			logger.info("Private Key Name " + fileName);
			File file = new File(fileName);
			String pass = propertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_PAYMENT_GATEWAY_BENE_PFX_PWD");
			PrivateKey privateKey = getCompositePrivateKey(file, pass);
			byte[] bytes = Base64.getDecoder().decode(response.replace("\r\n", ""));
			Cipher decriptCipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
			decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
			logger.info("Icici Composite decryption Finished");
			return new String(decriptCipher.doFinal(bytes));
		} catch (Exception ex) {
			logger.error("Exception in Composite Payble Bene Decryption ", ex);
			return null;
		}

	}

	private PrivateKey getCompositePrivateKey(File filename, String pass) {

		try {
			String alias = "";
			char[] bytePass = pass.toCharArray();

			KeyStore ks = KeyStore.getInstance("pkcs12", "SunJSSE");
			ks.load(new FileInputStream(filename), bytePass);

			// iterate over all aliases
			Enumeration es = ks.aliases();

			boolean isAliasWithPrivateKey = false;
			while (es.hasMoreElements()) {
				alias = (String) es.nextElement();
				// if alias refers to a private key break at that point as we
				// want to use that certificate
				if (isAliasWithPrivateKey = ks.isKeyEntry(alias)) {
					break;
				}
			}
			if (!isAliasWithPrivateKey) {
				logger.info("No Private key found in file");
				return null;
			}
			KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias,
					new KeyStore.PasswordProtection(bytePass));

			return pkEntry.getPrivateKey();

		} catch (Exception e) {
			logger.error("Exception in getting Payment Gateway private key ", e);
			return null;
		}

	}

	

	private IvParameterSpec getIV() throws UnsupportedEncodingException {

		byte[] iv = new byte[16];
		new SecureRandom().nextBytes(iv);
		return new IvParameterSpec(iv);

	}

	private static String getRandomSecretKey() {

		String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
		int size = 16;
		
		StringBuilder sb = new StringBuilder(size);

		for (int i = 0; i < size; i++) {
			int index = (int) (AlphaNumericString.length() * Math.random());
			sb.append(AlphaNumericString.charAt(index));
		}

		return sb.toString();
	}

	@SuppressWarnings("static-access")
	public Map<String, String> coinSwitchPayout(Fields fields) {
		Map<String, String> resMap = new HashMap<String, String>();
		String responseBody = "";
		JSONObject json = new JSONObject();
		try {

			BigDecimal amount = new BigDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()))
					.multiply(new BigDecimal(100));

			json.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			json.put(FieldType.ORDER_ID.getName(), "LPAY" + TransactionManager.getNewTransactionId());
			json.put(FieldType.BENE_NAME.getName(), fields.get(FieldType.PAYER_NAME.getName()));
			json.put(FieldType.BENE_ACCOUNT_NO.getName(), fields.get(FieldType.PAYER_ACCOUNT_NO.getName()));
			json.put(FieldType.IFSC_CODE.getName(), fields.get(FieldType.IFSC_CODE.getName()));
			json.put(FieldType.CURRENCY_CODE.getName(), "356");
			json.put(FieldType.AMOUNT.getName(), Amount.removeDecimalAmount(amount.toString(), "356"));
			json.put(FieldType.PHONE_NO.getName(), fields.get(FieldType.PHONE_NO.getName()));

			Fields hashFields = new Fields();

			hashFields.put(FieldType.PAY_ID.getName(), json.get(FieldType.PAY_ID.getName()).toString());
			hashFields.put(FieldType.ORDER_ID.getName(), json.get(FieldType.ORDER_ID.getName()).toString());
			hashFields.put(FieldType.BENE_NAME.getName(), json.get(FieldType.BENE_NAME.getName()).toString());
			hashFields.put(FieldType.BENE_ACCOUNT_NO.getName(),
					json.get(FieldType.BENE_ACCOUNT_NO.getName()).toString());
			hashFields.put(FieldType.IFSC_CODE.getName(), json.get(FieldType.IFSC_CODE.getName()).toString());
			hashFields.put(FieldType.CURRENCY_CODE.getName(), "356");
			hashFields.put(FieldType.AMOUNT.getName(), json.get(FieldType.AMOUNT.getName()).toString());
			hashFields.put(FieldType.PHONE_NO.getName(), json.get(FieldType.PHONE_NO.getName()).toString());

			String hash = Hasher.getHash(hashFields);

			json.put(FieldType.HASH.getName(), hash);

			String serviceUrl = propertiesManager.propertiesMap.get("TransactionWSImpsTransferURL");
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);

		} catch (Exception e) {
			logger.error("Exception in transaction request for CoinSwitch Customer payout >> " , e);
		}
		return resMap;
	}

}
