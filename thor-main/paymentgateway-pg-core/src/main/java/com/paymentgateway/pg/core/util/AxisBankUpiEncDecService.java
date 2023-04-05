package com.paymentgateway.pg.core.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.BASE64Decorder;
import com.paymentgateway.commons.util.Constants;

@Service
public class AxisBankUpiEncDecService {

	private static Logger logger = LoggerFactory.getLogger(AxisBankUpiEncDecService.class.getName());

	public static PublicKey publicKeyGlobalStatic = null;
	public static Cipher cipherGlobal = null;

	public String generateCheckSum(String checksum) {

		if (publicKeyGlobalStatic == null || cipherGlobal == null) {
			generateCipher();
		}

		try {
			logger.info("Plain Text Request = " + checksum);
			byte[] cipherText = null;
			getCipherGlobal().init(Cipher.ENCRYPT_MODE, getPublicKeyGlobalStatic());
			cipherText = getCipherGlobal().doFinal(checksum.getBytes());
			return javax.xml.bind.DatatypeConverter.printHexBinary(cipherText);
		} catch (Exception e) {
			logger.error("Error in generating encrypted request ", e);
		}
		return null;

	}

	public static void generateCipher() {

		KeyFactory keyFactory;
		BufferedReader reader;
		StringBuilder data = new StringBuilder();
		
		String fileLocation = System.getenv(Constants.PG_PROPERTIES_PATH.getValue())+Constants.AXIS_UPI_KEY.getValue();
		try {
			reader = new BufferedReader(new FileReader(fileLocation));
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				if (line != null) {
					data.append("\n" + line);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String str = String.valueOf(data);
		try {
			byte[] keyBytes;
			str = str.replaceAll("(-+BEGIN PUBLIC KEY-+\\r?\\n|-+END PUBLIC KEY-+\\r?\\n?)", "");
			BASE64Decorder decoder = new BASE64Decorder();
			keyBytes = decoder.decodeBuffer(str);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
			keyFactory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = keyFactory.generatePublic(spec);
			final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			setCipherGlobal(cipher);
			setPublicKeyGlobalStatic(publicKey);
		} catch (Exception e) {
			logger.error("Error in generating cipher and public key", e);
		}

	}

	public  String decrypt(String encryptedData, String keyStr) throws Exception {
		Key key = generateKey(keyStr);
		Cipher c = Cipher.getInstance("AES");
		c.init(2, key);
		byte[] decordedValue = java.util.Base64.getDecoder().decode(encryptedData);
		byte[] decValue = c.doFinal(decordedValue);
		return new String(decValue);
	}
	
	private  Key generateKey(String key) throws Exception {
		byte[] keyValue = key.getBytes();
		return new SecretKeySpec(keyValue, "AES");
	}
	
	public static Cipher getCipherGlobal() {
		return cipherGlobal;
	}

	public static void setCipherGlobal(Cipher cipherGlobal) {
		AxisBankUpiEncDecService.cipherGlobal = cipherGlobal;
	}

	public static PublicKey getPublicKeyGlobalStatic() {
		return publicKeyGlobalStatic;
	}

	public static void setPublicKeyGlobalStatic(PublicKey publicKeyGlobalStatic) {
		AxisBankUpiEncDecService.publicKeyGlobalStatic = publicKeyGlobalStatic;
	}

}
