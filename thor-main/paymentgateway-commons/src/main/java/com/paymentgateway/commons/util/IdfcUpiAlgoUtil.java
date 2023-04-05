package com.paymentgateway.commons.util;

/**
 * @author VJ
 *
 */

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;

@Service("idfcUpiUtilAlgo")
public class IdfcUpiAlgoUtil {

	private static Logger logger = LoggerFactory.getLogger(IdfcUpiAlgoUtil.class.getName());

	// private static String kek =
	// PropertiesManager.propertiesMap.get(Constants.KEK.getValue());
	private static String encryptedDEK = "";
	private static String decryptedDEK = "";
	private static String merchantCredential = "";

	private String key;

	public String generateDEK(Fields fields) throws SystemException {
		try {
			this.key = fields.get(FieldType.ADF7.getName());
			encryptedDEK = encryptTextusingAES(generateKey(), key);
			logger.info("inside the IdfcUpiAlgoUtil class in generateDEK method encDEK is:   " + encryptedDEK);

		} catch (Exception e) {
			logger.error("Exception in generateDEK , exception = " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in generateDEK  for idfc upi in IdfcUpiAlgoUtil");
		}
		return encryptedDEK;
	}

	public String decryptDEK(String encryptedDek, Fields fields) throws SystemException {
		try {
			this.key = fields.get(FieldType.ADF7.getName());
			logger.info("inside the IdfcUpiAlgoUtil class in decryptDEK method decryptedDEK is:  key " + key);
			decryptedDEK = decryptTextusingAES(encryptedDek, key);
			logger.info("inside the IdfcUpiAlgoUtil class in decryptDEK method decryptedDEK is: " + decryptedDEK);

		} catch (Exception e) {
			logger.error("Exception in decryptDEK , exception = " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in decryptDEK  for idfc upi in IdfcUpiAlgoUtil");
		}
		return decryptedDEK;
	}

	public String generateMerchantCredential(String transactionData, Fields fields, String encDEK)
			throws SystemException {
		try {
			encryptedDEK = encDEK;
			try {
				decryptedDEK = decryptDEK(encryptedDEK, fields);
				logger.info(
						"inside the IdfcUpiAlgoUtil class in generateMerchantCredential method merchantCredential is decryptedDEK:   "
								+ decryptedDEK);
				this.key = decryptedDEK;
			} catch (Exception e) {
				logger.error("Exception in generateMerchantCredential , exception = " , e);
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
						"unknown exception in generateMerchantCredential  for idfc upi in IdfcUpiAlgoUtil");
			}
			try {
				merchantCredential = encryptTextusingAES(transactionData, key);
				merchantCredential = merchantCredential.replaceAll("\r\n", "");
				logger.info(
						"inside the IdfcUpiAlgoUtil class in generateMerchantCredential method merchantCredential is 5:   "
								+ merchantCredential);
			} catch (Exception e) {
				logger.error("Exception in generateMerchantCredential , exception = " , e);
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
						"unknown exception in generateMerchantCredential  for idfc upi in IdfcUpiAlgoUtil");
			}
			return merchantCredential;
		} catch (Exception e) {
			logger.error("Exception in generateMerchantCredential , exception = " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in generateMerchantCredential  for idfc upi in IdfcUpiAlgoUtil");
		}
	}

	public String decryptMerchantCredential( Fields fields, String encDEK) throws SystemException {
		try {
			encryptedDEK = encDEK;

			decryptedDEK = decryptDEK(encryptedDEK, fields);
			this.key = decryptedDEK;
			try {
				String decryptedDEKRE = decryptTextusingAES(encryptedDEK, key);
				logger.info(
						"inside the IdfcUpiAlgoUtil class in decryptMerchantCredential method merchantCredential is :   "
								+ decryptedDEKRE);

			} catch (Exception e) {
				logger.error("Exception in decryptMerchantCredential , exception = " , e);
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
						"unknown exception in decryptMerchantCredential  for idfc upi in IdfcUpiAlgoUtil");
			}
		} catch (Exception e) {
			logger.error("Exception in decryptMerchantCredential , exception " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in decryptMerchantCredential  for idfc upi in IdfcUpiAlgoUtil");
		}
		return decryptedDEK;
	}

	public String encryptTextusingAES(String text, String kek) throws SystemException {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] keyBytes = new byte[16];
			byte[] b = kek.getBytes("UTF-8");
			int len = b.length;
			if (len > keyBytes.length)
				len = keyBytes.length;
			System.arraycopy(b, 0, keyBytes, 0, len);
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
			try {
				byte[] results = cipher.doFinal(text.getBytes("UTF-8"));
				BASE64Encoder encoder = new BASE64Encoder();
				return encoder.encode(results);
			} catch (Exception e) {
				logger.error("Exception in encryptTextusingAES , exception " , e);
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
						"unknown exception in encryptTextusingAES  for idfc upi in IdfcUpiAlgoUtil");
			}

		} catch (Exception e) {
			logger.error("Exception in encryptTextusingAES , exception " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in encryptTextusingAES  for idfc upi in IdfcUpiAlgoUtil");
		}
	}

	public String decryptTextusingAES(String text, String kek) throws SystemException {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] keyBytes = new byte[16];
			byte[] b = kek.getBytes("UTF-8");
			int len = b.length;
			if (len > keyBytes.length)
				len = keyBytes.length;
			System.arraycopy(b, 0, keyBytes, 0, len);
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
			try {
				BASE64Decorder decoder = new BASE64Decorder();
				byte[] results = cipher.doFinal(decoder.decodeBuffer(text));
				return new String(results, "UTF-8");
			} catch (Exception e) {
				logger.error("Exception in decryptTextusingAES , exception " , e);
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
						"unknown exception in decryptTextusingAES  for idfc upi in IdfcUpiAlgoUtil");
			}
		} catch (Exception e) {
			logger.error("Exception in decryptTextusingAES , exception " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in decryptTextusingAES  for idfc upi in IdfcUpiAlgoUtil");
		}
	}

	public String stringToHex(String str) throws SystemException {
		try {
			byte[] abyte0 = str.getBytes();

			StringBuffer stringbuffer = new StringBuffer(abyte0.length * 2);

			for (int i = 0; i < abyte0.length; i++) {
				char c = Character.forDigit(abyte0[i] >>> 4 & 0xf, 16);
				char c1 = Character.forDigit(abyte0[i] & 0xf, 16);
				stringbuffer.append(Character.toUpperCase(c));
				stringbuffer.append(Character.toUpperCase(c1));
			}

			return stringbuffer.toString();
		} catch (Exception e) {
			logger.error("Exception in stringToHex , exception ", e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in stringToHex  for idfc upi in IdfcUpiAlgoUtil");
		}
	}

	public String hexToString(String s) throws SystemException {
		try {
			int i = s.length() / 2;
			byte abyte0[] = new byte[i];
			int j = 0;

			for (int k = 0; k < s.length(); k += 2) {
				byte byte0 = (byte) Integer.parseInt(s.substring(k, k + 2), 16);
				abyte0[j] = byte0;
				j++;
			}

			return new String(abyte0);
		} catch (Exception e) {
			logger.error("Exception in hexToString , exception ", e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in hexToString  for idfc upi in IdfcUpiAlgoUtil");
		}
	}

	public String generateKey() throws SystemException {
		String key = null;

		try {
			key = RandomStringUtils.randomNumeric(24);

			logger.info("inside the IdfcUpiAlgoUtil class in generateKey method key is :   " + key);
		} catch (Exception e) {
			logger.error("Exception in generateKey , exception = " , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in generateKey  for idfc upi in IdfcUpiAlgoUtil");
		}

		return key;

	}
}
