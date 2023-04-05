package com.paymentgateway.pg.core.util;
import java.security.NoSuchAlgorithmException;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class IdbiUtil {

	private SecretKeySpec skeySpec;
	private Cipher cipher;


	
	
	public IdbiUtil() {
	}

	
	public void initDecrypt(String key) throws Exception {
		try {
			skeySpec = new SecretKeySpec(HexUtil.HexfromString(key), "AES");
			cipher = Cipher.getInstance("AES");
			cipher.init(2, skeySpec);

		} catch (NoSuchAlgorithmException nsae) {
			throw new Exception("Invalid Java Version");
		} catch (NoSuchPaddingException nse) {
			throw new Exception("Invalid Key");
		}
	}
	
	
	public String decryptMEssage(String resMsg) throws Exception {
		try {
			byte encstr[] = cipher.doFinal(HexUtil.HexfromString(resMsg));
			return new String(encstr);
		} catch (BadPaddingException nse) {
			throw new Exception("Invalid input String");
		}
	}
	

	public void initEncrypt(String key) throws Exception {
		try {
			skeySpec = new SecretKeySpec(HexUtil.HexfromString(key), "AES");
			cipher = Cipher.getInstance("AES");
			cipher.init(1, skeySpec);
		} catch (NoSuchAlgorithmException nsae) {
			throw new Exception("Invalid Java Version");
		} catch (NoSuchPaddingException nse) {
			throw new Exception("Invalid Key");
		}
	}

	public String encryptMEMessage(String reqMsg) throws Exception {
		try {
			byte encstr[] = cipher.doFinal(reqMsg.getBytes());
			return HexUtil.HextoString(encstr);
		} catch (BadPaddingException nse) {
			throw new Exception("Invalid input String");
		}
	}
	
	
}
