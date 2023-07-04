package com.paymentgateway.phonePe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;

@RestController
public class CallBackController {

	@Autowired
	PhonePeCommon common;
	
	@Autowired
	PhonePeService phonePeService;
	
	HashMap prop = common.readProperty("phonePe.properties");
	
    private final String CALLBACK_SALT_KEY = prop.get("PHONEPE_SALTKEY").toString();
    private final int CALLBACK_SALT_INDEX = 1;

	@RequestMapping(method = RequestMethod.GET, value = "/callback")
    public ResponseEntity<String> handleCallback(
            @RequestBody CallbackPayload payload,
            @RequestHeader("X-VERIFY") String verifyHeader ) {
        try {
            // Verify the callback using the X-VERIFY header
            if (!verifyCallback(payload.getResponse(), verifyHeader)) {
              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid callback.");
            }

            byte[] decodedBytes = Base64.getDecoder().decode(payload.getResponse());
            String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);

            System.out.println("decodedPayload:::::::::::" + decodedPayload);
            
//            String reponseFromPaytappCallback = paytappCallbackUrl(decodedPayload);
//            System.out.println("reponseFromPaytappCallback:::::::::::" + reponseFromPaytappCallback);
            
            Gson gson = new Gson();
			StatusCheckResponseBean bean = gson.fromJson(decodedPayload , StatusCheckResponseBean.class);
			
			System.out.println("msg ::::::::::::::::::::::::::::::::::::::     :::"+bean.getMessage());
			phonePeService.saveTransactionStatusResponse(bean);	
			
            return ResponseEntity.ok(decodedPayload);
        } catch (Exception e) {

            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while processing the callback.");
        }
    }

    private boolean verifyCallback(String response, String verifyHeader) throws Exception {
        // Generate the expected checksum using the response and salt key
    	String encoded = response + CALLBACK_SALT_KEY;
        String expectedChecksum = HashCrypto(encoded);

        String expectedVerifyHeader = expectedChecksum + "###" + CALLBACK_SALT_INDEX;
        
        return expectedVerifyHeader.equals(verifyHeader);
    }
    
	public String HashCrypto(String input)throws Exception{
			
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        md.update(input.getBytes());
	
	        byte byteData[] = md.digest();
	        StringBuffer sb=new StringBuffer();
	
	        //convert the byte to hex format method 1
	       // byte[] byteArray=input.getBytes();
	        
	        for (int i = 1; i < byteData.length-1; i++) {
	        	sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	        }
	
	        System.out.println("Hex format : " + sb.toString());
	
	        //convert the byte to hex format method 2
	        StringBuffer hexString = new StringBuffer();
	        for (int i=0;i<byteData.length;i++) {
	            String hex=Integer.toHexString(0xff & byteData[i]);
	            if(hex.length()==1) hexString.append('0');
	            hexString.append(hex);
	        }
	        return hexString.toString();
	    }
	

}
