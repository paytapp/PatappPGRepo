package com.paymentgateway.pg.core.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PayphiUtil {

	private static Logger logger = LoggerFactory.getLogger(PayphiUtil.class.getName());

	public String generateHash(String msg, String keyString) {
		String digest = null;
		try {
			SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(key);
			byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));
			StringBuffer hash = new StringBuffer();
			for (int i = 0; i < bytes.length; i++) {
				String hex = Integer.toHexString(0xFF & bytes[i]);
				if (hex.length() == 1) {
					hash.append('0');
				}
				hash.append(hex);
			}
			digest = hash.toString();
		} catch (UnsupportedEncodingException e) {
			logger.error("Exception in generating payphi request hash " , e);
		} catch (InvalidKeyException e) {
			logger.error("Exception in generating payphi request hash " , e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception in generating payphi request hash " , e);
		}
		return digest;
	}

}
