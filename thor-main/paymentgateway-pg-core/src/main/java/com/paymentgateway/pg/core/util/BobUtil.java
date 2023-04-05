package com.paymentgateway.pg.core.util;

import java.security.Security;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import cryptix.provider.key.RawSecretKey;
import cryptix.util.core.Hex;
import xjava.security.Cipher;

@Service
public class BobUtil {
	
	private static Logger logger = LoggerFactory.getLogger(BobUtil.class.getName());
	
	// Method for request data encryption
		public String encryptText(String key, String valueToBeEncrypted) throws Exception {

			String enc1 = "";
			String value = "";
			String encadd = "";
			String key1 = "";
			String key2 = "";
			String key3 = "";
			String checking = "";
			try {

				key1 = alpha2Hex(key.substring(0, 8));
				key2 = alpha2Hex(key.substring(8, 16));
				key3 = alpha2Hex(key.substring(16, 24));

				if ((valueToBeEncrypted.length() % 8) != 0) {
					valueToBeEncrypted = rightPadZeros(valueToBeEncrypted);
				}
				for (int i = 0; i < valueToBeEncrypted.length(); i = i + 8) {

					value = valueToBeEncrypted.substring(i, i + 8);
					checking = checking + alpha2Hex(value);
					enc1 = getTripleHexValue(alpha2Hex(value), key1, key2, key3);
					encadd = encadd + enc1;
				}

				return encadd;
			} catch (Exception e) {
				e.printStackTrace();

			} finally {
				enc1 = null;
				value = null;
				encadd = null;
				key1 = null;
				key2 = null;
				key3 = null;
			}
			return null;

		}

		public String decryptText(String key, String valueToBeDecrypted) throws Exception {

			String key1 = "";
			String key2 = "";
			String key3 = "";
			try {
				key1 = alpha2Hex(key.substring(0, 8));
				key2 = alpha2Hex(key.substring(8, 16));
				key3 = alpha2Hex(key.substring(16, 24));
				String decryptedStr = getTripleDesValue(valueToBeDecrypted, key3, key2, key1);
				decryptedStr = hexToString(decryptedStr);
				if (decryptedStr.startsWith("<")) {
					decryptedStr = decryptedStr.substring(0, decryptedStr.lastIndexOf('>') + 1);
				} else {
					decryptedStr = decryptedStr.substring(0, decryptedStr.lastIndexOf('&') + 1);
				}
				return decryptedStr;
			} catch (Exception exception) {
				logger.error("Exception", exception);

			}
			return null;
		}

		public String getTripleHexValue(final String pin, final String key1, final String key2, final String key3)
				throws Exception {

			try {
				String encryptedKey = getHexValue(pin, key1);
				encryptedKey = getDexValue(encryptedKey, key2);
				encryptedKey = binary2hex(asciiChar2binary(encryptedKey)).toUpperCase();
				encryptedKey = getHexValue(encryptedKey, key3);

				return encryptedKey;
			} catch (final Exception exception) {
				logger.error("Exception", exception);
				throw exception;
			}
		}

		static {
			//Security.insertProviderAt(new BouncyCastleProvider(), 1);
		}

		public String getHexValue(final String pin, final String key) throws Exception {
			try {
				//Security.insertProviderAt(new BouncyCastleProvider(), 1);
				Security.addProvider(new cryptix.provider.Cryptix());
				Cipher des = Cipher.getInstance("DES/ECB/NONE", "Cryptix");
				RawSecretKey desKey = new RawSecretKey("DES", Hex.fromString(key));
				des.initEncrypt(desKey);
				final byte[] pinInByteArray = Hex.fromString(pin);
				final byte[] ciphertext = des.crypt(pinInByteArray);
				return (Hex.toString(ciphertext));
			} catch (final Exception exception) {
				logger.error("Exception", exception);
				throw exception;
			}
		}

		public String getDexValue(final String pin, final String key) throws Exception {
			try {
				//Security.insertProviderAt(new BouncyCastleProvider(),1);
				Security.addProvider(new cryptix.provider.Cryptix());
				Cipher des = Cipher.getInstance("DES/ECB/NONE", "Cryptix");
				RawSecretKey desKey = new RawSecretKey("DES", Hex.fromString(key));

				des.initDecrypt(desKey);

				final byte[] pinInByteArray = Hex.fromString(pin);
				final byte[] ciphertext = des.crypt(pinInByteArray);
				return toString(ciphertext);
			} catch (final Exception exception) {
				logger.error("Exception", exception);
			}
			return null;
		}

		public String binary2hex(final String binaryString) {
			if (binaryString == null) {
				return null;
			}
			String hexString = "";
			for (int i = 0; i < binaryString.length(); i += 8) {
				String temp = binaryString.substring(i, i + 8);

				int intValue = 0;
				for (int k = 0, j = temp.length() - 1; j >= 0; j--, k++) {
					intValue += Integer.parseInt("" + temp.charAt(j)) * Math.pow(2, k);
				}
				temp = "0" + Integer.toHexString(intValue);
				hexString += temp.substring(temp.length() - 2);
			}
			return hexString;
		}

		public static String rightPadZeros(String Str) {
			if (null == Str) {
				return null;
			}
			String PadStr = new String(Str);

			for (int i = Str.length(); (i % 8) != 0; i++) {
				PadStr = PadStr + '^';
			}
			return PadStr;
		}

		public static String alpha2Hex(String data) {
			char[] alpha = data.toCharArray();
			StringBuffer sb = new StringBuffer();
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

		public String hexToString(String txtInHex) {

			byte[] txtInByte = new byte[txtInHex.length() / 2];

			int j = 0;
			for (int i = 0; i < txtInHex.length(); i += 2)
			{
				txtInByte[j++] = Byte.parseByte(txtInHex.substring(i, i + 2), 16);
			}

			return new String(txtInByte);
		}

		public String getTripleDesValue(final String pin, final String key1, final String key2, final String key3) {

			try {
				String decryptedKey = getDexValue(pin, key1);
				decryptedKey = binary2hex(asciiChar2binary(decryptedKey)).toUpperCase();
				decryptedKey = getHexValue(decryptedKey, key2);
				decryptedKey = getDexValue(decryptedKey, key3);
				decryptedKey = binary2hex(asciiChar2binary(decryptedKey)).toUpperCase();

				return decryptedKey;
			} catch (final Exception exception) {
				logger.error("Exception", exception);

			}
			return null;
		}

		public String asciiChar2binary(final String asciiString) {
			if (asciiString == null) {
				return null;
			}
			String binaryString = "";
			String temp = "";
			int intValue = 0;
			for (int i = 0; i < asciiString.length(); i++) {
				intValue = (int) asciiString.charAt(i);

				temp = "00000000" + Integer.toBinaryString(intValue);
				binaryString += temp.substring(temp.length() - 8);
			}
			return binaryString;

		}
		
		public String toString(final byte[] temp) {
			final char ch[] = new char[temp.length];
			for (int i = 0; i < temp.length; i++) {
				ch[i] = (char) temp[i];
			}
			final String s = new String(ch);
			return s;
		}


}
