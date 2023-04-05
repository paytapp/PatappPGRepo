package com.paymentgateway.pg.core.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class SbiUtil {

	private static Logger logger = LoggerFactory.getLogger(SbiUtil.class.getName());

	@Autowired
	private PropertiesManager propertiesManager;

	public String encrypt(String data) {

		String path = System.getenv("DTECH_PROPS") + "" + propertiesManager.propertiesMap.get("SBIEncKeyFileName");
		byte[] key = null;
		try {
			key = returnbyte(path);
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		String encData = null;
		try {
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			int blockSize = cipher.getBlockSize();
			byte[] iv = new byte[cipher.getBlockSize()];
			byte[] dataBytes = data.getBytes();
			int plaintextLength = dataBytes.length;
			int remainder = plaintextLength % blockSize;
			if (remainder != 0) {
				plaintextLength += blockSize - remainder;
			}
			byte[] plaintext = new byte[plaintextLength];
			System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
			SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
			SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
			randomSecureRandom.nextBytes(iv);
			GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
			cipher.init(1, keySpec, parameterSpec);
			byte[] results = cipher.doFinal(plaintext);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			outputStream.write(iv);
			outputStream.write(results);
			byte[] encrypteddata = outputStream.toByteArray();
			encData = Base64.encodeBase64String(encrypteddata);
			encData = encData.replace("\n", "").replace("\r", "");
		} catch (Exception exception) {
			logger.error("Exception occured", exception);
		}
		return encData;
	}

	public String decrypt(String encData) {
		String decdata = null;
		String path = System.getenv("DTECH_PROPS") + "" + propertiesManager.propertiesMap.get("SBIEncKeyFileName");
		byte[] key = null;
		key = returnbyte(path);
		try {
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
			byte[] results = Base64.decodeBase64(encData);
			byte[] iv = Arrays.copyOfRange(results, 0, cipher.getBlockSize());
			cipher.init(2, keySpec, new GCMParameterSpec(128, iv));
			byte[] results1 = Arrays.copyOfRange(results, cipher.getBlockSize(), results.length);
			byte[] ciphertext = cipher.doFinal(results1);
			decdata = new String(ciphertext).trim();
		} catch (Exception exception) {
			logger.error("Exception occured", exception);
		}
		return decdata;
	}

	public byte[] returnbyte(String path) {
		FileInputStream fileinputstream;
		byte[] abyte = null;
		try {
			fileinputstream = new FileInputStream(path);
			abyte = new byte[fileinputstream.available()];
			fileinputstream.read(abyte);
			fileinputstream.close();
		} catch (FileNotFoundException e1) {
			logger.error("Exception occured", e1);
		} catch (IOException e) {
			logger.error("Exception occured", e);
		}
		return abyte;
	}

	public String encryptText(String key, String valueToBeEncrypted) throws Exception {
		String enc1 = null;
		String encadd = "";
		try {
			if (valueToBeEncrypted.length() % 8 != 0) {
				valueToBeEncrypted = rightPadZeros(valueToBeEncrypted);
			}
			enc1 = getHexValue(alpha2Hex(valueToBeEncrypted), key);
			encadd = encadd + enc1;
			return encadd;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			enc1 = null;
			encadd = null;
		}
	}

	public static String rightPadZeros(String Str) {
		if (Str == null) {
			return null;
		}
		StringBuilder padStr = new StringBuilder(Str);
		for (int i = Str.length(); i % 8 != 0; i++) {
			padStr.append('^');
		}
		return padStr.toString();
	}

	public static String alpha2Hex(String data) {
		char[] alpha = data.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < alpha.length; i++) {
			int count = Integer.toHexString(alpha[i]).toUpperCase().length();
			if (count <= 1) {
				sb.append("0").append(Integer.toHexString(alpha[i]).toUpperCase());
			} else {
				sb.append(Integer.toHexString(alpha[i]).toUpperCase());
			}
		}
		return sb.toString();
	}

	public String getHexValue(String pin, String key) throws Exception {
		String algorithm = null;
		String mode = null;
		String padding = null;
		Cipher cipher = null;
		javax.crypto.spec.SecretKeySpec skeySpec = null;
		try {
			algorithm = "AES";
			mode = "ECB";
			padding = "PKCS5PADDING";
			skeySpec = new javax.crypto.spec.SecretKeySpec(key.getBytes("UTF-8"), algorithm);
			cipher = Cipher.getInstance(algorithm + "/" + mode + "/" + padding);
			cipher.init(1, skeySpec);
			byte[] encrypted = cipher.doFinal(pin.getBytes());
			return cryptix.util.core.Hex.toString(encrypted);
		} catch (Exception e) {
			throw e;
		} finally {
			algorithm = null;
			mode = null;
			padding = null;
			cipher = null;
			skeySpec = null;
		}
	}
	
	public static String decryptText(String key, String valueToBeDecrypted) throws Exception {
		MAC mac = null;
		try {
			mac = new MAC();
			return mac.getAESValue(valueToBeDecrypted, key);
		} catch (Exception e) {
			return null;
		} finally {
			mac = null;
		}
	}
	
	
}
