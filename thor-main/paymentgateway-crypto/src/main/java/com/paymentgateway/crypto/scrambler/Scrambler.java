package com.paymentgateway.crypto.scrambler;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.SystemConstants;

public class Scrambler {
	public static final String ALGO = "AES";
	private String key = null;
	private Key keyObj = null;

	public Scrambler(String key) {
		this.key = key;
		this.keyObj = new SecretKeySpec(key.getBytes(), ALGO);
	}

	public String encrypt(String data) throws SystemException {
		try {
			IvParameterSpec iv = new IvParameterSpec(key.getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8));
			Cipher cipher = Cipher.getInstance(ALGO + "/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, keyObj, iv);

			byte[] encValue = cipher.doFinal(data.getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8));

			Base64.Encoder base64Encoder = Base64.getEncoder().withoutPadding();
			String base64EncodedData = base64Encoder.encodeToString(encValue);
			return base64EncodedData;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException
				| IllegalBlockSizeException | BadPaddingException
				| InvalidAlgorithmParameterException scramblerExceptionException) {
			throw new SystemException(ErrorType.CRYPTO_ERROR, scramblerExceptionException,
					"Error during encryption process");
		}
	}

	public String decrypt(String data) throws SystemException {
		try {
			IvParameterSpec iv = new IvParameterSpec(key.getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8));
			Cipher cipher = Cipher.getInstance(ALGO + "/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, keyObj, iv);

			byte[] decodedData = Base64.getDecoder().decode(data);
			byte[] decValue = cipher.doFinal(decodedData);

			return new String(decValue);

		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException
				| IllegalBlockSizeException | BadPaddingException
				| InvalidAlgorithmParameterException scramblerExceptionException) {
			throw new SystemException(ErrorType.CRYPTO_ERROR, scramblerExceptionException,
					"Error during decryption process");

		}
	}

	public String hostedEncrypt(String data) throws SystemException {
		try {

			String ivString = key.substring(0, 16);
			IvParameterSpec iv = new IvParameterSpec(ivString.getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8));

			Cipher cipher = Cipher.getInstance(ALGO + "/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, keyObj, iv);

			byte[] encValue = cipher.doFinal(data.getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8));

			Base64.Encoder base64Encoder = Base64.getEncoder().withoutPadding();
			String base64EncodedData = base64Encoder.encodeToString(encValue);
			return base64EncodedData;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException
				| IllegalBlockSizeException | BadPaddingException
				| InvalidAlgorithmParameterException scramblerExceptionException) {
			throw new SystemException(ErrorType.CRYPTO_ERROR, scramblerExceptionException,
					"Error during encryption process");
		}
	}

	public String hostedDecrypt(String data) throws SystemException {
		try {

			String ivString = key.substring(0, 16);
			IvParameterSpec iv = new IvParameterSpec(ivString.getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8));
			Cipher cipher = Cipher.getInstance(ALGO + "/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, keyObj, iv);
			byte[] decodedData = Base64.getDecoder().decode(data);
			byte[] decValue = cipher.doFinal(decodedData);

			return new String(decValue);

		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException
				| IllegalBlockSizeException | BadPaddingException
				| InvalidAlgorithmParameterException scramblerExceptionException) {
			throw new SystemException(ErrorType.CRYPTO_ERROR, scramblerExceptionException,
					"Error during decryption process");

		}
	}
}