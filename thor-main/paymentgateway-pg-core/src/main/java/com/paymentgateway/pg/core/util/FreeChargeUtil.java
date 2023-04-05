package com.paymentgateway.pg.core.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.MessageDigestProvider;
import com.paymentgateway.commons.exception.SystemException;

@Service
public class FreeChargeUtil {

	private static Logger logger = LoggerFactory.getLogger(FreeChargeUtil.class.getName());

	// Method for creating Checksum
	public String generateChecksum(String jsonString, String merchantKey) throws SystemException{
		MessageDigest md = MessageDigestProvider.provide();
		String plainText = jsonString.concat(merchantKey);
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception in creating checksum for freecharge = ",e);
		}
		md.update(plainText.getBytes(Charset.defaultCharset()));
		byte[] mdbytes = md.digest();
		// convert the byte to hex format method 1
		StringBuffer checksum = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++) {
			checksum.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		logger.info("CheckSum for freecharge Parameter = "+checksum.toString());
		return checksum.toString();
	}

}
