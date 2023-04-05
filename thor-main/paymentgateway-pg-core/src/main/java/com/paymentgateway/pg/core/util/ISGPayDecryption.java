package com.paymentgateway.pg.core.util;

import java.net.URLDecoder;
import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ISGPayDecryption {

	public void decrypt(final LinkedHashMap<String, String> hmDecryptedValue, final String sDecryptionKey,
			final String sSecureSecret) throws Exception {
		try {
			if (StringUtils.isNotBlank(sSecureSecret)) {
				if (StringUtils.isNotBlank(sDecryptionKey)) {
					doISGPayDecrypt(hmDecryptedValue, sDecryptionKey);
					final String sResponseHash = (hmDecryptedValue.get("SecureHash") == null) ? ""
							: hmDecryptedValue.get("SecureHash");
					if (!"".equals(sResponseHash)) {
						hmDecryptedValue.remove("SecureHash");
						final StringBuffer hexString = new StringBuffer();
						hexString.append(generateHash(hmDecryptedValue, sSecureSecret));
						if (hexString.length() != 0) {
							if (sResponseHash.equals(hexString.toString())) {
								hmDecryptedValue.put("hashValidated", "CORRECT");
							} else {
								hmDecryptedValue.put("hashValidated", "INVALID HASH");
							}
						} else {
							hmDecryptedValue.put("ErrorMessage", "Problem During Generating Hash...!!!");
						}
					} else {
						hmDecryptedValue.put("ErrorMessage", "Securehash Not Found In Response...!!!");
					}
				} else {
					hmDecryptedValue.put("ErrorMessage", "Decryption Key can not be null or empty!!!");
				}
			} else {
				hmDecryptedValue.put("ErrorMessage", "SecureSecret can not be null or empty!!!");
			}
		} catch (Exception e) {
			System.out.println("Exception Occured in ISGPayDecryption.decrypt(Collection): " + e);
			hmDecryptedValue.put("ErrorMessage", e.getMessage());
		}
	}

	private static void doISGPayDecrypt(final LinkedHashMap<String, String> hmDecryptedValue,
			final String sDecryptionKey) throws Exception {
		try {
			String decryptedValue = "";
			if (StringUtils.isBlank(hmDecryptedValue.get("EncData"))) {
				throw new Exception("Encrypted Data Not Found In Request!!!");
			}
			final byte[] keyByte = sDecryptionKey.getBytes();
			final Key key = new SecretKeySpec(keyByte, "AES");
			final Cipher c = Cipher.getInstance("AES");
			c.init(2, key);
			final byte[] decryptedByteValue = new Base64().decode(hmDecryptedValue.get("EncData").getBytes());
			final byte[] decValue = c.doFinal(decryptedByteValue);
			decryptedValue = new String(decValue);
			hmDecryptedValue.remove("EncData");
			if ("".equals(decryptedValue)) {
				throw new Exception("Problem In Decryption!!!");
			}
			final String[] pipeSplit = decryptedValue.split("::");
			for (int i = 0; i < pipeSplit.length; ++i) {
				final String[] pareValues = pipeSplit[i].split("\\|\\|");
				if (pareValues[1] != null && pareValues[1].toString().length() > 0
						&& !pareValues[1].toString().equalsIgnoreCase("null")) {
					hmDecryptedValue.put(pareValues[0], URLDecoder.decode(pareValues[1], "UTF-8"));
				}
			}
		} catch (Exception ex) {
		}
	}

	public static void isgpayDecrypt(final LinkedHashMap<String, String> hmDecryptedValue,
			final String sDecryptionKey) throws Exception {
		try {
			String decryptedValue = "";
			if (StringUtils.isBlank(hmDecryptedValue.get("EncData"))) {
				throw new Exception("Encrypted Data Not Found In Request!!!");
			}
			final byte[] keyByte = sDecryptionKey.getBytes();
			final Key key = new SecretKeySpec(keyByte, "AES");
			final Cipher c = Cipher.getInstance("AES");
			c.init(2, key);
			final byte[] decryptedByteValue = new Base64().decode(hmDecryptedValue.get("EncData").getBytes());
			final byte[] decValue = c.doFinal(decryptedByteValue);
			decryptedValue = new String(decValue);
			hmDecryptedValue.remove("EncData");
			if ("".equals(decryptedValue)) {
				throw new Exception("Problem In Decryption!!!");
			}
			final String[] pipeSplit = decryptedValue.split("::");
			for (int i = 0; i < pipeSplit.length; ++i) {
				final String[] pareValues = pipeSplit[i].split("\\|\\|");
				if (pareValues[1] != null && pareValues[1].toString().length() > 0
						&& !pareValues[1].toString().equalsIgnoreCase("null")) {
					hmDecryptedValue.put(pareValues[0], URLDecoder.decode(pareValues[1], "UTF-8"));
				}
			}
		} catch (Exception ex) {
		}
	}

	public static String generateHash(final LinkedHashMap<String, String> hmReqFields, final String sSecureSecret)
			throws Exception {
		final StringBuffer hexString = new StringBuffer();
		try {
			hmReqFields.remove("inprocess");
			final ArrayList<String> fieldNames = new ArrayList<String>(hmReqFields.keySet());
			Collections.sort(fieldNames);
			final StringBuffer buf = new StringBuffer();
			buf.append(sSecureSecret);
			final Iterator<String> itr = fieldNames.iterator();
			String hashKeys = "";
			while (itr.hasNext()) {
				final String fieldName = itr.next();
				final String fieldValue = hmReqFields.get(fieldName);
				hashKeys = String.valueOf(hashKeys) + fieldName + ", ";
				if (fieldValue != null && fieldValue.length() > 0) {
					buf.append(fieldValue);
				}
			}
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final byte[] hash = digest.digest(buf.toString().getBytes("UTF-8"));
			for (int i = 0; i < hash.length; ++i) {
				final String hex = Integer.toHexString(0xFF & hash[i]);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			hmReqFields.put("SecureHash", hexString.toString());
		} catch (Exception e) {
			System.out.println("Exception Occured in ISGPayHashGeneration.generateHash():" + e);
			throw e;
		}
		return hexString.toString();
	}
	
	public static LinkedHashMap<String, String> isgpayDecrypt(String res, final String sDecryptionKey)
			throws Exception {
		LinkedHashMap<String, String> hmDecryptedValue = new LinkedHashMap<String, String>();
		try {
			String decryptedValue = "";
			if (StringUtils.isBlank(res)) {
				throw new Exception("Encrypted Data Not Found In Request!!!");
			}
			final byte[] keyByte = sDecryptionKey.getBytes();
			final Key key = new SecretKeySpec(keyByte, "AES");
			final Cipher c = Cipher.getInstance("AES");
			c.init(2, key);
			final byte[] decryptedByteValue = new Base64().decode(res);
			final byte[] decValue = c.doFinal(decryptedByteValue);
			decryptedValue = new String(decValue);
			if ("".equals(decryptedValue)) {
				throw new Exception("Problem In Decryption!!!");
			}
			final String[] pipeSplit = decryptedValue.split("::");
			for (int i = 0; i < pipeSplit.length; ++i) {
				final String[] pareValues = pipeSplit[i].split("\\|\\|");
				if (pareValues[1] != null && pareValues[1].toString().length() > 0
						&& !pareValues[1].toString().equalsIgnoreCase("null")) {
					hmDecryptedValue.put(pareValues[0], URLDecoder.decode(pareValues[1], "UTF-8"));
				}
				
			}
			return hmDecryptedValue;
		} catch (Exception ex) {
		}
		return hmDecryptedValue;
	}
}
