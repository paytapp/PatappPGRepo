package com.paymentgateway.pg.core.util;

import java.net.URLEncoder;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FssPayUtil {

	private static Logger logger = LoggerFactory.getLogger(FssPayUtil.class.getName());

	public String encryptRequest(String tranrequest, String key) throws Exception {
		byte[] encryptedText = null;
		String encryptedData = null;
		IvParameterSpec ivspec = null;
		SecretKeySpec skeySpec = null;
		Cipher cipher = null;
		byte[] text = null;
		try {
			ivspec = new IvParameterSpec(key.getBytes("UTF-8"));
			skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);
			text = tranrequest.getBytes("UTF-8");
			encryptedText = cipher.doFinal(text);
			encryptedData = Base64.encodeBase64String(encryptedText);
			encryptedData = URLEncoder.encode(encryptedData, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			encryptedText = null;
			ivspec = null;
			skeySpec = null;
			cipher = null;
			text = null;
		}
		return encryptedData;

	}
	
	public String decryptResponse(String encResponse, String key) throws Exception {
		String decryptedData = null;
		byte[] decrypted = null;
		SecretKeySpec skeySpec = null;
		IvParameterSpec ivspec = null;
		Cipher cipher = null;
		byte[] textDecrypted = null;
		try {
			decrypted = Base64.decodeBase64(encResponse);
			skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
			ivspec = new IvParameterSpec(key.getBytes("UTF-8"));
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);
			textDecrypted = cipher.doFinal(decrypted);
		} catch (Exception e) {

		} finally {
			decryptedData = null;
			decrypted = null;
			skeySpec = null;
			ivspec = null;
			cipher = null;
		}

		return (new String(textDecrypted));
		
	
	}
	
	
	public static String encrypt(String Data, String keySet) throws Exception {
		byte[] keyByte = keySet.getBytes();
		Key key = generateKey(keyByte);
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encVal = c.doFinal(Data.getBytes());
		byte[] encryptedByteValue = new Base64().encode(encVal);
		String encryptedValue = new String(encryptedByteValue);
		return encryptedValue;
	}

	private static Key generateKey(byte[] keyByte) throws Exception {
		Key key = new SecretKeySpec(keyByte, "AES");
		return key;
	}

}
