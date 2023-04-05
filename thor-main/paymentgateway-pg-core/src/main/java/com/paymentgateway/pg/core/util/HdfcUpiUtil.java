package com.paymentgateway.pg.core.util;

import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.PropertiesManager;

@Service("hdfcUpiUtil")
public class HdfcUpiUtil {

	private static Logger logger = LoggerFactory.getLogger(HdfcUpiUtil.class.getName());
	private SecretKeySpec skeySpec;
	private Cipher cipher;

	public HdfcUpiUtil() {
		skeySpec = null;
		cipher = null;
	}

	public void initEncrypt(String key) throws Exception {
		try {
			skeySpec = new SecretKeySpec(HexUtil.HexfromString(key),PropertiesManager.propertiesMap.get(ConstantsPaymentGateway.HDFC_UPI_AES_UTIL));
			cipher = Cipher.getInstance(PropertiesManager.propertiesMap.get(ConstantsPaymentGateway.HDFC_UPI_AES_UTIL));
			cipher.init(1, skeySpec);
		} catch (NoSuchAlgorithmException nsae) {
			throw new Exception("Invalid Java Version");
		} catch (NoSuchPaddingException nse) {
			throw new Exception("Invalid Key");
		}
	}

	public void initDecrypt(String key) throws Exception {
		try {
			skeySpec = new SecretKeySpec(HexUtil.HexfromString(key),PropertiesManager.propertiesMap.get(ConstantsPaymentGateway.HDFC_UPI_AES_UTIL));
			cipher = Cipher.getInstance(PropertiesManager.propertiesMap.get(ConstantsPaymentGateway.HDFC_UPI_AES_UTIL));
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
			logger.info("decrept string :  " + str);
			return str;
		} catch (BadPaddingException nse) {
			logger.error("Exception in decryption " , nse);
			throw new Exception("Invalid input String");
		}
	}

	

	public String encrypt(String message, String enc_key) throws Exception {
		try {
			initEncrypt(enc_key);

			byte encstr[] = cipher.doFinal(message.getBytes());
			logger.info("encrypt string :  " + HexUtil.HextoString(encstr));
			return HexUtil.HextoString(encstr);
		} catch (BadPaddingException nse) {
			throw new Exception("Invalid input String");
		}
	}
}
