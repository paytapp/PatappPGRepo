package com.paymentgateway.pg.core.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BillDeskUtil {

	private static Logger logger = LoggerFactory.getLogger(BillDeskUtil.class.getName());
	
	public String generateChecksum(String request, String key) {
		
		try {

			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			byte raw[] = sha256_HMAC.doFinal(request.getBytes());
			StringBuffer ls_sb = new StringBuffer();
			for (int i = 0; i < raw.length; i++)
				ls_sb.append(char2hex(raw[i]));
			return ls_sb.toString(); // step 6
		} catch (Exception e) {
			logger.error("Exception while generating checksum: " , e);
			return null;
		}

	}

	public static String char2hex(byte x){
		char arr[] = {

				'0', '1', '2', '3',

				'4', '5', '6', '7',

				'8', '9', 'A', 'B',

				'C', 'D', 'E', 'F'

		};
		char c[] = { arr[(x & 0xF0) >> 4], arr[x & 0x0F] };
		return (new String(c));
	}
}
