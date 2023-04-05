package com.paymentgateway.pg.core.util;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class YesBankUpiUtil {

	private static Logger logger = LoggerFactory.getLogger(YesBankUpiUtil.class.getName());
	private SecretKeySpec skeySpec;
	private Cipher cipher;

	public YesBankUpiUtil() {
		skeySpec = null;
		cipher = null;
	}

	public void initEncrypt(String key) throws Exception {
		try {
			skeySpec = new SecretKeySpec(HexUtil.HexfromString(key),"AES");
			cipher = Cipher.getInstance("AES");
			cipher.init(1, skeySpec);
		} catch (NoSuchAlgorithmException nsae) {
			throw new Exception("Invalid Java Version");
		} catch (NoSuchPaddingException nse) {
			throw new Exception("Invalid Key");
		}
	}

	public void initDecrypt(String key) throws Exception {
		try {
			skeySpec = new SecretKeySpec(HexUtil.HexfromString(key),"AES");
			cipher = Cipher.getInstance("AES");
			cipher.init(2, skeySpec);

		} catch (NoSuchAlgorithmException nsae) {
			throw new Exception("Invalid Java Version");
		} catch (NoSuchPaddingException nse) {
			throw new Exception("Invalid Key");
		}
	}

	public String decrypt(String message, String dec_key) throws Exception {
		try {

			initDecrypt(dec_key);

			byte encstr[] = cipher.doFinal(HexUtil.HexfromString(message));
			String str = new String(encstr);
			return str;
		} catch (Exception e) {
			logger.error("Exception in decryption " ,e);
		}
		return null;
	}

	

	public String encrypt(String message, String enc_key) throws Exception {
		try {
			initEncrypt(enc_key);

			byte encstr[] = cipher.doFinal(message.getBytes());
			return HexUtil.HextoString(encstr);
		} catch (Exception e) {
			logger.error("Exception in decryption " ,e);
		}
		return null;
	}
}
