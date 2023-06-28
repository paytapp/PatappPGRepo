package com.paymentgateway.phonePe;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.phonePe.PhonePeCommon;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.crypto.Cipher;

@Service
public class phonePeRequestGenerator {

	
	@Autowired
	PhonePeCommon common;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String request ="";
		try {
			phonePeRequestGenerator ppRequestGenerator=new phonePeRequestGenerator();
			request=ppRequestGenerator.requestCreator("CARDNUMBER","1234","MU933037302229373",100000,"9717323931",
					"SBIN","4208585190116667","Arvind Chaturvedi","06","2027","508","c116-sector 22",
					"noida","noida","U.P.","302021","india","C903889349294273423","1234","1234","1234"); 
			
			System.out.println("Request is ---->"+request);
			
		}catch(Exception e) {
			e.printStackTrace();
		}

	}

	public String requestCreator(String valDiff,String purchaseId,String userId,int amount,String phNo,String bankId,
			String encCardNumber,String cardHName,String month,String year,
			String encCvvNumber,String addressLine1,String addressLine2,String city,String state,
			String pinCode,String country,String cardId,String cryptoGram,String panSuffix,
			String encryptedToken1) {
		
		String finalRequest="";
		//String valDiff="";
		String valDiffStr="";

		HashMap prop = common.readProperty("phonePe.properties");
		
		//phonepeResponse phonepeResponseData=new phonepeResponse();
		//txnAmount = txnAmount*100;
		
		String baseUrl = prop.get("PHONEPE_BASE_URL").toString();
		String payEndpoint = prop.get("PHONEPE_PAY_ENDPOINT").toString();
		String PHONEPE_MID = prop.get("PHONEPE_MID").toString();
		String PURCHASE_REDIRECT_URL = prop.get("PURCHASE_REDIRECT_URL").toString();
		String PHONEPE_CALLBACK_URL = prop.get("PHONEPE_CALLBACK_URL").toString();
	//	saltKey = prop.get("PHONEPE_SALTKEY").toString();
	//	saltIndex = prop.get("PHONEPE_SALT_INDEX").toString();
	//	PHONEPE_REQUEST_URL = baseUrl + payEndpoint;
		String vpaStr="test-vpa@ybl";//Need to change
		
		try {
			
			finalRequest="{"+
					   "\"merchantId\": \""+PHONEPE_MID+"\","+
					   "\"merchantTransactionId\": \""+purchaseId+"\","+
					   "\"merchantUserId\": \""+userId+"\","+
					   "\"amount\":"+ amount+","+
					   "\"callbackUrl\": \""+PURCHASE_REDIRECT_URL+"\","+
					   "\"mobileNumber\": \""+phNo+"\",";
			// UPI Open Intent (ANDROID)
			if(valDiff.equalsIgnoreCase("UPIOpenIntent")){
			
				
				valDiffStr="\"deviceContext\": {"+
			     "\"deviceOS\": \"ANDROID\""+
			   "},"+
			   "\"paymentInstrument\": {"+
			   "\"type\": \"UPI_INTENT\","+
			     "\"targetApp\": \"com.phonepe.app\""+
			   "} }	";
			}
			 //-------------------------------
			// Sample Payload for Base64 - UPI Collect
			if(valDiff.equalsIgnoreCase("UPICollect")){
				
			 
				valDiffStr="\"paymentInstrument\": {"+
			     "\"type\": \"UPI_COLLECT\","+
			     "\"vpa\": \""+vpaStr+"\""+
			   "}}";
			}
			 //------------------
			 //Sample Payload for Base64 - UPI QR
			 if(valDiff.equalsIgnoreCase("UPIQR")){
			 
				 valDiffStr="\"paymentInstrument\": {"+
			     "\"type\": \"UPI_QR\""+
			  " } } ";
			 }
			 //--------------------------
			 //Sample Payload for Base64 - Web Flow
			 if(valDiff.equalsIgnoreCase("Web")){
			 
				 valDiffStr="\"paymentInstrument\": {"+
			     "\"type\": \"PAY_PAGE\""+
			   "} } ";
			 }
			 //------------------------
			 //Sample Payload for Base64 - With Card Number
			 if(valDiff.equalsIgnoreCase("CardNumber")){
			 			 
				 //Encryption Method: RSA 4096////Encrypted 16-Digit Card Number entered by the user
				 HashMap encVal=new HashMap();
				 HashMap actualVal=new HashMap();
				 actualVal.put("CardNumber", encCardNumber);
				 actualVal.put("CVVNumber", encCvvNumber);
				 
				 encVal=ras4096Enc(actualVal,"ENC");
				 
				 valDiffStr="\"paymentInstrument\": {"+
			         "\"type\": \"CARD\","+
			         "\"authMode\": \"3DS\","+
			         "\"saveCard\": true,"+
			         "\"cardDetails\": {"+
			             "\"encryptedCardNumber\": \""+encVal.get("encText")+"\","+//Encrypted 16-Digit Card Number entered by the user.Encryption Method: RSA 4096
			             "\"encryptionKeyId\":"+encVal.get("key")+","+//We will do encryption
			             "\"cardHolderName\": \""+cardHName+"\","+
			             "\"expiry\": {"+
			                 "\"month\":\""+month+"\","+
			                 "\"year\":\""+year+"\""+
			             "},"+
			             "\"encryptedCvv\": \""+encVal.get("encCVVText")+"\","+//We will do encryption 
			             "\"billingAddress\": {"+
			                 "\"line1\": \""+addressLine1+"\","+
			                 "\"line2\": \""+addressLine2+"\","+
			                 "\"city\": \""+city+"\","+
			                 "\"state\": \""+state+"\","+
			                 "\"zip\": \""+pinCode+"\","+
			                 "\"country\": \""+country+"\""+
			            " }} } }";
			 }
			 //--------------------
			 //Sample Payload for Base64 - With Card Id
			 if(valDiff.equalsIgnoreCase("CardId")){
			 
				 HashMap encVal=new HashMap();
				 HashMap actualVal=new HashMap();
				 actualVal.put("CardNumber", encCardNumber);
				 actualVal.put("CVVNumber", encCvvNumber);
				 
				 encVal=ras4096Enc(actualVal,"ENC");
				 
				 valDiffStr="\"paymentInstrument\": {"+
			     "\"type\": \"SAVED_CARD\","+
			     "\"authMode\": \"3DS\","+
			     "\"cardDetails\": {"+
			       "\"cardId\": \""+cardId+"\","+
			       "\"encryptedCvv\": \""+encVal.get("encCVVText").toString()+"\","+//We will do encryption 
			       "\"encryptionKeyId\":\""+encVal.get("key").toString()+"\""+ //We will do encryption 
			     "} } } ";
			 }
			 //-------------
			 //Sample Payload for Base64 - With Token
			 if(valDiff.equalsIgnoreCase("Token")){
			 
				 HashMap encVal=new HashMap();
				 HashMap actualVal=new HashMap();
				 actualVal.put("CardNumber", encCardNumber);
				 actualVal.put("CVVNumber", encCvvNumber);
				 actualVal.put("encryptedToken1", encryptedToken1);
				 
				 encVal=ras4096Enc(actualVal,"ENC");
				 valDiffStr="\"paymentInstrument\": {"+
			     "\"type\": \"TOKEN\","+
			     "\"authMode\": \"3DS\","+
			     "\"tokenDetails\": {"+
			      "\"encryptedCvv\": \""+encVal.get("encCVVText")+"\","+//We will do encryption 
			       "\"cryptogram\": \""+cryptoGram+"\","+
			       //"\"encryptedToken\":\""+SJk7Sazu894+cHsxsTOLML5ZYseMzdQ+"\","+
			       "\"encryptedToken\":\""+encryptedToken1+"\","+//We will do encryption 
			       "\"encryptionKeyId\":"+encVal.get("key")+","+//We will do encryption 
			       "\"expiry\": {"+
			         "\"month\": \""+month+"\","+
			         "\"year\": \""+year+"\""+
			       "},"+
			       "\"panSuffix\": \""+panSuffix+"\","+
			       "\"cardHolderName\": \""+cardHName+"\""+
			    " } } } ";
			 }
			 
			 //------------------
			 //Sample Payload for Base64 - NET BANKING
			 if(valDiff.equalsIgnoreCase("NETBANKING")){
			 
				 valDiffStr="\"paymentInstrument\": {"+
			     "\"type\": \"NET_BANKING\","+
			     "\"bankId\": \""+bankId+"\""+
			   "} } ";
			 }
			 finalRequest=finalRequest+valDiffStr;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return finalRequest;
	}
	public HashMap ras4096Enc(HashMap input,String encDec) {
		HashMap finalEnc=new HashMap();
		try {
			// Get an instance of the RSA key generator
	        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
	        keyPairGenerator.initialize(4096);
	        
	        // Generate the KeyPair
	        KeyPair keyPair = keyPairGenerator.generateKeyPair();
	        
	        // Get the public and private key
	        PublicKey publicKey = keyPair.getPublic();
	        PrivateKey privateKey = keyPair.getPrivate();
	        
	        System.out.println("Original Text  : "+input);
	        
	        // Encryption token arvind
	        
	        byte[] cipherTextArray = null;
	        byte[] CVVTextArray = null;
	        byte[] tokenTextArray = null;
	        
	        //if(encDec.equalsIgnoreCase("ENC")) {
	        	if(input.get("CardNumber")!=null) {
	        		cipherTextArray = encrypt(input.get("CardNumber").toString(), publicKey);
	        		String encryptedText = Base64.getEncoder().encodeToString(cipherTextArray);
	        		finalEnc.put("encText", encryptedText);
	        		System.out.println("Encrypted Text : "+encryptedText);
	        	}
	        	if(input.get("CVVNumber")!=null) {
	        		CVVTextArray = encrypt(input.get("CVVNumber").toString(), publicKey);
	        		String encryptedCVVText = Base64.getEncoder().encodeToString(CVVTextArray);
	        		finalEnc.put("encCVVText", encryptedCVVText);
	        	}
	        	if(input.get("encryptedToken1")!=null) {
	        		tokenTextArray = encrypt(input.get("encryptedToken1").toString(), publicKey);
	        		String encryptedTokenText = Base64.getEncoder().encodeToString(tokenTextArray);
	        		finalEnc.put("encTikenText", encryptedTokenText);
	        	}
	        	//System.out.println("Key value is -----"+publicKey);
	        	//finalEnc.put("key", publicKey);
	        	
	        	
	        
	        
	        // Decryption
	        	String decryptedText = decrypt(cipherTextArray, privateKey);
	        	System.out.println("DeCrypted Text : "+decryptedText);
	        //	finalEnc.put("dncText", decryptedText);
	        	finalEnc.put("key", decryptedText);
	        
	    }
	    
	   catch(Exception e) {
			e.printStackTrace();
		}
		return finalEnc;
	}
	
	 public static byte[] encrypt (String plainText,PublicKey publicKey ) throws Exception
	    {
	        //Get Cipher Instance RSA With ECB Mode and OAEPWITHSHA-512ANDMGF1PADDING Padding
	        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING");
	        
	        //Initialize Cipher for ENCRYPT_MODE
	        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
	        
	        //Perform Encryption
	        byte[] cipherText = cipher.doFinal(plainText.getBytes()) ;

	        return cipherText;
	    }
	    
	    public static String decrypt (byte[] cipherTextArray, PrivateKey privateKey) throws Exception
	    {
	        //Get Cipher Instance RSA With ECB Mode and OAEPWITHSHA-512ANDMGF1PADDING Padding
	        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING");
	        
	        //Initialize Cipher for DECRYPT_MODE
	        cipher.init(Cipher.DECRYPT_MODE, privateKey);
	        
	        //Perform Decryption
	        byte[] decryptedTextArray = cipher.doFinal(cipherTextArray);
	        
	        return new String(decryptedTextArray);
		}
	
}
