package com.paymentgateway.pg.core.util;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KotakUtil {

	private static Logger logger = LoggerFactory.getLogger(KotakUtil.class.getName());

	public String decrypt(String encryptedData, String keySet) throws Exception {
		byte[] keyByte = keySet.getBytes();
		Key key = generateKey(keyByte);
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.DECRYPT_MODE, key);
		byte[] decryptedByteValue = new Base64().decode(encryptedData.getBytes());
		byte[] decValue = c.doFinal(decryptedByteValue);
		String decryptedValue = new String(decValue);
		return decryptedValue;
	}

	private static Key generateKey(byte[] keyByte) throws Exception {
		Key key = new SecretKeySpec(keyByte, "AES");
		return key;
	}

}
