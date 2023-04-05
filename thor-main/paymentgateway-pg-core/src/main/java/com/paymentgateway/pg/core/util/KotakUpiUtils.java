package com.paymentgateway.pg.core.util;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;


@Service("kotakUpiUtil")
public class KotakUpiUtils {
	private static Logger logger = LoggerFactory.getLogger(KotakUpiUtils.class.getName());
	// to generate checkSum header
	public String CheckSum(String request ,Fields fields) {
	byte[] digest = null;
	try {
		digest = SHA256(request.toString());
	} catch (Exception e) {
		logger.error("Exception kotak upi : " + e.getMessage());
	}
	//String key = fields.get(FieldType.ADF7.getName());
	String key = "7A0D7DE6B5B0503A8044402B9653AB202887DD233378B9F3B4E72A71544B7AC0";
	byte[] encData = null;
	try {
		encData = encrypt(hexStringToByteArray(key), digest);
	} catch (Exception e) {
		logger.error("Exception kotak upi : " , e.getMessage());
	}

	StringBuffer hexString = new StringBuffer();
	for (int i = 0; i < digest.length; i++) {
		String hex = Integer.toHexString(0xff & digest[i]);
		if (hex.length() == 1)
			hexString.append('0');
		hexString.append(hex);
	}

	StringBuffer encrString = new StringBuffer();
	for (int i = 0; i < encData.length; i++) {
		String hex = Integer.toHexString(0xff & encData[i]);
		if (hex.length() == 1)
			encrString.append('0');
		encrString.append(hex);
	}

	String checkSumval = Base64.encodeBase64String(encData);
	return checkSumval;

	
	}
	
	public static byte[] SHA256(String paramString) throws Exception {
		MessageDigest localMessageDigest = MessageDigest.getInstance("SHA-256");
		localMessageDigest.update(paramString.getBytes("UTF-8"));
		byte[] digest = localMessageDigest.digest();
		return digest;
	}

	public static byte[] hexStringToByteArray(String s) {
		byte[] b = new byte[s.length() / 2];
		for (int i = 0; i < b.length; i++) {
			int index = i * 2;
			int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}

	public static byte[] encrypt(byte[] key, byte[] checkSum) throws Exception {
		System.out.println(">>>>>>>>>KEY::" + key.length);
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
		byte[] iv = new byte[16];
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		Cipher acipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		acipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
		byte[] arrayOfByte1 = acipher.doFinal(checkSum);
		return arrayOfByte1;
	}
	
	public String matchChecksum (JSONObject res) {
		///JSONObject res = new JSONObject(res);
		//JSONObject txnRes = res.getString("jsonText");
		String mechantCode = res.getString("merchantcode");
		String payeevpa = res.getString("payeevpa");
		String payervpa = res.getString("payervpa");
		String transactionid = res.getString("transactionid");
		String transactionTimestamp = res.getString("transactionTimestamp");
		String amount = res.getString("amount");
		String refid = res.getString("refid");
		String statusCode = res.getString("statusCode");
		String remarks = res.getString("remarks");
		String rrn = res.getString("rrn");
		StringBuilder request = new StringBuilder();
		
		request.append(mechantCode);
		request.append(payeevpa);
		request.append(payervpa);
		request.append(transactionid);
		request.append(transactionTimestamp);
		request.append(amount);
		request.append(refid);
		request.append(statusCode);
		request.append(remarks);
		request.append(rrn);
		
		String checkSum = sha256(request.toString());
		
		return checkSum;
		
		
	}
	
	
//call back checkSum logic
public static String sha256(String base) 
{ 
try 
{ 
MessageDigest digest = MessageDigest.getInstance("SHA-256"); 
byte[] hash = digest.digest(base.getBytes("UTF-8")); 
StringBuffer hexString = new StringBuffer(); 
for (int i = 0; i < hash.length; i++) 
{ 
String hex = Integer.toHexString(0xff & hash[i]); 
if(hex.length() == 1) hexString.append('0'); 
hexString.append(hex); 
} 

return hexString.toString(); 
} 
catch(Exception ex) 
{ 
throw new RuntimeException(ex); 
} 
} 
	
	
}
