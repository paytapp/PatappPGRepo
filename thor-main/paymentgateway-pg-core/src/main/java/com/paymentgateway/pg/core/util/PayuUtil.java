package com.paymentgateway.pg.core.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

/**
 * @author Rahul
 *
 */
public class PayuUtil {

	private static String digestHash(String str) {
		StringBuilder hash = new StringBuilder();
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
			messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
			byte[] mdbytes = messageDigest.digest();
			for (byte hashByte : mdbytes) {
				hash.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
			}
			return hash.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static String payuSaleRequestHash(TreeMap<String, String> paytmParams) {

		String template = "%s|%s|%s|%s|%s|%s|||||||||||%s";
		String str = String.format(template, paytmParams.get("key"), paytmParams.get("txnid"),
				paytmParams.get("amount"), paytmParams.get("productinfo"), paytmParams.get("firstname"),
				paytmParams.get("email"), paytmParams.get("salt"));
		return digestHash(str);

	}


	public static String payuRefundAndStatusEnqHash(TreeMap<String, String> paytmParams) {
		// sha512(key|command|var1|var2|var3|salt)
		String template = "%s|%s|%s|%s";
		String str = String.format(template, paytmParams.get("key"), paytmParams.get("command"),
				paytmParams.get("var1"), paytmParams.get("salt"));
		return digestHash(str);

	}

	public static String payuStatusEnqHash(Fields paytmParams) {
		String command = "verify_payment";
		// sha512(key|command|var1|var2|var3|salt)
		String template = "%s|%s|%s|%s";
		String str = String.format(template, paytmParams.get(FieldType.MERCHANT_ID.getName()), command,
				paytmParams.get(FieldType.PG_REF_NUM.getName()), paytmParams.get(FieldType.PASSWORD.getName()));
		return digestHash(str);

	}

	public static String payuResponseHash(Map<String, String> reqMap) {
		// sha512(SALT|status||||||udf5|udf4|udf3|udf2|udf1|email|firstname|productinfo|amount|txnid|key)
		String productInfo = "Payment Gateway Product";
		
		String template = "%s|%s|||||||||||%s|%s|%s|%s|%s|%s";
		String str = String.format(template,reqMap.get("salt") , reqMap.get("status"),
				reqMap.get("email"), reqMap.get("firstname"), productInfo, reqMap.get("amount"),
				reqMap.get("txnid"), reqMap.get("key"));
		return digestHash(str);

	}
	
	public static String payuEmiBinEnqHash(String command,String var1,String key ,String salt) {
		// sha512(key|command|var1|salt)
		String template = "%s|%s|%s|%s";
		String str = String.format(template,key, command,
				var1, salt);
		return digestHash(str);

	}

	public PayuUtil() {

	}
}
