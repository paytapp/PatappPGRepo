package com.paymentgateway.commons.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Service
public class DataEncDecTool {

	@Autowired
	private PropertiesManager propertiesManager;

	private static SecretKeySpec secretKey;
	private static byte[] key;
	public static String secretKeyVal = null;

	private static Logger logger = LoggerFactory.getLogger(DataEncDecTool.class.getName());
	
	public List<Document> encryptDocument(List<Document> lstDoc) {

		List<Document> docList = new ArrayList<Document>();
		if (StringUtils.isBlank(secretKeyVal)) {
			getSecret();
		}

		String encDecFields = propertiesManager.propertiesMap.get("TransactionEncFields");

		if (StringUtils.isNotBlank(encDecFields)) {

			String encDecFieldsArr[] = encDecFields.split(",");
			for(Document doc : lstDoc) {
			for (String fieldName : encDecFieldsArr) {

				if (doc.get(fieldName) != null) {

					// Add Mask
					// doc.put(fieldName, mask(doc.get(fieldName).toString()));
					// Add Encrypted Record
					doc.put(fieldName, encrypt(doc.get(fieldName).toString(), secretKeyVal));
				}

			}
			docList.add(doc);
		  }
		}
		return docList;
	}

	public Document encryptDocument(Document doc) {

		if (StringUtils.isBlank(secretKeyVal)) {
			getSecret();
		}

		String encDecFields = propertiesManager.propertiesMap.get("TransactionEncFields");

		if (StringUtils.isNotBlank(encDecFields)) {

			String encDecFieldsArr[] = encDecFields.split(",");

			for (String fieldName : encDecFieldsArr) {

				if (doc.get(fieldName) != null) {
					doc.put(fieldName, encrypt(doc.get(fieldName).toString(), secretKeyVal));
				}

			}
		}

		doc.put(FieldType.IS_ENCRYPTED.getName(), "Y");
		return doc;

	}

	public Document decryptDocument(Document doc) {

		if (doc.get(FieldType.IS_ENCRYPTED.getName()) != null) {
			
		}
		
		if (StringUtils.isBlank(secretKeyVal)) {
			getSecret();
		}

		String encDecFields = propertiesManager.propertiesMap.get("TransactionEncFields");

		if (StringUtils.isNotBlank(encDecFields)) {

			String encDecFieldsArr[] = encDecFields.split(",");

			for (String fieldName : encDecFieldsArr) {

				if (doc.get(fieldName) != null) {
					doc.put(fieldName, decrypt(doc.get(fieldName).toString(), secretKeyVal));
				}

			}
		}

		doc.put(FieldType.IS_ENCRYPTED.getName(), "N");
		return doc;

	}

	public Fields encryptFields(Fields fields) {

		if (StringUtils.isBlank(secretKeyVal)) {
			getSecret();
		}

		String encDecFields = propertiesManager.propertiesMap.get("TransactionEncFields");

		if (StringUtils.isNotBlank(encDecFields)) {

			String encDecFieldsArr[] = encDecFields.split(",");

			for (String fieldName : encDecFieldsArr) {

				if (fields.get(fieldName) != null) {
					fields.put(fieldName, encrypt(fields.get(fieldName).toString(), secretKeyVal));
				}

			}
		}

		return fields;

	}

	public Fields decryptFields(Fields fields) {

		if (StringUtils.isBlank(secretKeyVal)) {
			getSecret();
		}

		String encDecFields = propertiesManager.propertiesMap.get("TransactionEncFields");

		if (StringUtils.isNotBlank(encDecFields)) {

			String encDecFieldsArr[] = encDecFields.split(",");

			for (String fieldName : encDecFieldsArr) {

				if (fields.get(fieldName) != null) {
					fields.put(fieldName, decrypt(fields.get(fieldName).toString(), secretKeyVal));
				}

			}
		}

		return fields;

	}

	public static void setKey(String myKey) {
		MessageDigest sha = null;
		try {
			key = myKey.getBytes("UTF-8");
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static String encrypt(String strToEncrypt, String secret) {
		try {
			setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}

	public static String decrypt(String strToDecrypt, String secret) {
		try {
			setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} catch (Exception e) {
			System.out.println("Error while decrypting: " + e.toString());
		}
		return null;
	}

	static String mask(String input) {
		if (input.length() <= 4)
			return input; // Nothing to mask
		char[] buf = input.toCharArray();
		Arrays.fill(buf, 0, buf.length - 4, 'X');
		return new String(buf);
	}

	private void getSecret() {

		System.setProperty("aws.accessKeyId", System.getenv("AWS_ACCESS_KEY_ID"));
		System.setProperty("aws.secretAccessKey", System.getenv("AWS_SECRET_KEY"));

		try {
			String secretName = propertiesManager.propertiesMap.get("AWSEncKeyName");
			Region region = Region.of("ap-south-1");

			SecretsManagerClient client = SecretsManagerClient.builder().region(region).build();

			String secretValue;
			JSONObject secret = new JSONObject();
			GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();
			GetSecretValueResponse getSecretValueResponse = null;

			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

			if (getSecretValueResponse.secretString() != null) {
				secret = new JSONObject(getSecretValueResponse.secretString());
				secretValue = secret.getString("MongoDbDataEncryption");
				logger.info("Secret Fetched from AWS !");
				secretKeyVal = secretValue;
			} else {
				logger.info("Unable to fetch secret from AWS !");
			}

		} catch (Exception e) {
			logger.error("Exception ", e);
		}
	}
}
