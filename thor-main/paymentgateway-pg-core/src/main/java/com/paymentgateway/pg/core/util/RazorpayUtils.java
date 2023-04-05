package com.paymentgateway.pg.core.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;

@Service
public class RazorpayUtils {

	public boolean verifyPaymentSignature(JSONObject attributes, String apiSecret) throws SystemException {
		String expectedSignature = attributes.getString("razorpay_signature");
		String orderId = attributes.getString("razorpay_order_id");
		String paymentId = attributes.getString("razorpay_payment_id");
		String payload = orderId + '|' + paymentId;
		return verifySignature(payload, expectedSignature, apiSecret);
	}

	public boolean verifyWebhookSignature(String payload, String expectedSignature, String webhookSecret)
			throws SystemException {
		return verifySignature(payload, expectedSignature, webhookSecret);
	}

	public boolean verifySignature(String payload, String expectedSignature, String secret)
			throws SystemException {
		String actualSignature = getHash(payload, secret);
		return isEqual(actualSignature.getBytes(), expectedSignature.getBytes());
	}

	public static String getHash(String payload, String secret) throws SystemException {
		Mac sha256_HMAC;
		try {
			sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			byte[] hash = sha256_HMAC.doFinal(payload.getBytes());
			return new String(Hex.encodeHex(hash));
		} catch (Exception e) {
			throw new SystemException(ErrorType.VALIDATION_FAILED, e.getMessage());
		}
	}

	private boolean isEqual(byte[] a, byte[] b) {
		if (a.length != b.length) {
			return false;
		}
		int result = 0;
		for (int i = 0; i < a.length; i++) {
			result |= a[i] ^ b[i];
		}
		return result == 0;
	}
}
