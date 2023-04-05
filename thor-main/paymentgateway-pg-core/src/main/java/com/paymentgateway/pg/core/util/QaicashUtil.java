package com.paymentgateway.pg.core.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class QaicashUtil {

	private static Logger logger = LoggerFactory.getLogger(QaicashUtil.class.getName());

	public String HMAC_SHA256(String key, String data) {
	    try {

	        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
	        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	        sha256_HMAC.init(secret_key);

	        return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));

	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (InvalidKeyException e) {
	        e.printStackTrace();
	    }
	    
	    return null;
	}
	
}
