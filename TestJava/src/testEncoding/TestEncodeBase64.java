package testEncoding;

import java.util.Base64;

public class TestEncodeBase64 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String phonepePayload="";
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
	        
	        System.out.println("Base value is ----"+BasicBase64format);
	 
	        // print encoded String
	        System.out.println("Encoded String:\n" + BasicBase64format);
		}catch(Exception e) {
			e.printStackTrace();
		}
		//return phonepePayload;
	

	}

}
