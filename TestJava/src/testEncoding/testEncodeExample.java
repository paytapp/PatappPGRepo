package testEncoding;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class testEncodeExample {

	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	private static final String OUTPUT_FORMAT = "%-20s:%s";
	public static void main(String[] args) {
		String phonepePayload="";
		String checksum="";
		try {
			phonepePayload = "{"+
					  "\"merchantId\": \"GAMEZDADDYUAT\",\""+
					  "\"merchantTransactionId\": \"MT7850590068188104\","+
					  "\"merchantUserId\": \"PAYTAPP1\","+
					  "\"amount\": 10000,"+
					  "\"redirectUrl\": \"https://webhook.site/redirect-url\","+
					  "\"redirectMode\": \"POST\","+
					  "\"callbackUrl\": \"https://webhook.site/callback-url\","+
					  "\"mobileNumber\": \"9999999999\","+
					  "\"paymentInstrument\": {"+
					  "\"type\": \"WINDOWS\""+
					  "}"+
					"}";
	 
	        // Encode into Base64 format
	        String BasicBase64format
	            = Base64.getEncoder()
	                  .encodeToString(phonepePayload.getBytes());
	        
	        System.out.println("Base value is --->"+BasicBase64format);
	 
	        // print encoded String
	        checksum = BasicBase64format + "/pg/v1/pay" + 1;
	        System.out.println("Checksum is ----->"+checksum);
	        
	        String algorithm = "sha256";
	        
	        byte[] shaInBytes = testEncodeExample.digest(checksum.getBytes(UTF_8), algorithm);
	        
	        String output=String.format(OUTPUT_FORMAT, algorithm + " (hex) ", bytesToHex(shaInBytes));
	        
	      System.out.println("Output is ---> "+output);  
		}catch(Exception e) {
			e.printStackTrace();
		}
		//return phonepePayload;
	
}
	 public static byte[] digest(byte[] input, String algorithm) {
	        MessageDigest md;
	        try {
	            md = MessageDigest.getInstance(algorithm);
	        } catch (NoSuchAlgorithmException e) {
	            throw new IllegalArgumentException(e);
	        }
	        byte[] result = md.digest(input);
	        return result;
	    }
	 public static String bytesToHex(byte[] bytes) {
	        StringBuilder sb = new StringBuilder();
	        for (byte b : bytes) {
	            sb.append(String.format("%02x", b));
	        }
	        return sb.toString();
	    }
}
