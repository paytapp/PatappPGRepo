package com.paymentgateway.phonePe;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.Cipher;

import org.springframework.beans.factory.annotation.Autowired;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TestCard {

	@Autowired
	PhonePeCommon common;
	
	String PHONEPE_REQUEST_URL;
	String saltKey;
	String saltIndex;
	static OkHttpClient client = new OkHttpClient();
	
	phonePeRequestGenerator ppRequestGenerator=new phonePeRequestGenerator();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//Card
		/*
		 * try { TestCard tppe=new TestCard();
		 * tppe.initiatePhonepePayment("CARDNUMBER","MU933037302229373", "User1234",
		 * 100000,"9999999999","SBIN",
		 * "4208585190116667","Arvind Chaturvedi","06","2027","508","C-116","Sector 53"
		 * ,"Noida", "UP","302021","India","C903889349294273423","1234","1234","1234");
		 * }catch(Exception e) { e.printStackTrace(); }
		 */
		try {
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String initiatePhonepePayment(String paymentType,String purchaseId, String userId, int txnAmount,
			String phNo,String bankId,String cardNumber,String cardNo,String month,String year,
			String cvv,String addressLine1,String addressLine2,String city,String state,String pin,
			String country,String cardId,String cryptoGram,String panSuffix,String encryptedToken1) throws IOException {
	 
		txnAmount = txnAmount*100;
		String responseStr="";
		String finalRequest="";
		//String valDiff="";
		String valDiffStr="";
		
		HashMap prop = common.readProperty("phonePe.properties");
		
		String baseUrl = prop.get("PHONEPE_BASE_URL").toString();
		String payEndpoint = prop.get("PHONEPE_PAY_ENDPOINT").toString();
		String PHONEPE_MID = prop.get("PHONEPE_MID").toString();
		String PURCHASE_REDIRECT_URL = prop.get("PURCHASE_REDIRECT_URL").toString();
		String PHONEPE_CALLBACK_URL = prop.get("PHONEPE_CALLBACK_URL").toString();
		saltKey = prop.get("PHONEPE_SALTKEY").toString();
		saltIndex = prop.get("PHONEPE_SALT_INDEX").toString();
		PHONEPE_REQUEST_URL = baseUrl + payEndpoint;
		
		try {
			String phonepe_payload_card_initiate="";
			
			/*
			 * String phonepe_payload_card_initiate
			 * =ppRequestGenerator.requestCreator(paymentType,purchaseId,userId,txnAmount,
			 * phNo,
			 * bankId,encCardNumber,cardNo,month,year,cvv,addressLine1,addressLine2,city,
			 * state,pin, country,cardId,cryptoGram,panSuffix,encryptedToken1);
			 */
			 
			
			  finalRequest="{"+
					   "\"merchantId\": \""+PHONEPE_MID+"\","+
					   "\"merchantTransactionId\": \""+purchaseId+"\","+
					   "\"merchantUserId\": \""+userId+"\","+
					   "\"amount\":"+ txnAmount+","+
					   "\"callbackUrl\": \""+PURCHASE_REDIRECT_URL+"\","+
					   "\"mobileNumber\": \""+phNo+"\",";
			  
			  String valDiff="CardNumber";
			  //Sample Payload for Base64 - With Card Number
				 if(valDiff.equalsIgnoreCase("CARDNUMBER")){
				 			 
					 //Encryption Method: RSA 4096////Encrypted 16-Digit Card Number entered by the user
					 HashMap encVal=new HashMap();
					 HashMap actualVal=new HashMap();
				/*
				 * actualVal.put("CardNumber", cardNumber); actualVal.put("CVVNumber", cvv);
				 */
					 
					 encVal=ras4096Enc(cardNumber,cvv);
					 
					 valDiffStr="\"paymentInstrument\": {"+
				         "\"type\": \"CARD\","+
				         "\"authMode\": \"3DS\","+
				         "\"saveCard\": true,"+
				         "\"cardDetails\": {"+
				             "\"encryptedCardNumber\":\""+encVal.get("encText")+"\","+//Encrypted 16-Digit Card Number entered by the user.Encryption Method: RSA 4096
				             "\"encryptionKeyId\":"+encVal.get("key")+","+//We will do encryption
				             "\"cardHolderName\": \""+cardNo+"\","+
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
				                 "\"zip\": \""+pin+"\","+
				                 "\"country\": \""+country+"\""+
				            " }} } }";
				 }
				 //--------------------
				 //Sample Payload for Base64 - With Card Id
				 if(valDiff.equalsIgnoreCase("CardId")){
				 
					 HashMap encVal=new HashMap();
				/*
				 * HashMap actualVal=new HashMap(); actualVal.put("CardNumber", cardNumber);
				 * actualVal.put("CVVNumber", cvv);
				 */
					 
					 encVal=ras4096Enc(cardNumber,cvv);
					 
					 valDiffStr="\"paymentInstrument\": {"+
				     "\"type\": \"SAVED_CARD\","+
				     "\"authMode\": \"3DS\","+
				     "\"cardDetails\": {"+
				       "\"cardId\": \""+cardId+"\","+
				       "\"encryptedCvv\": \""+encVal.get("encCVVText").toString()+"\","+//We will do encryption 
				       "\"encryptionKeyId\":\""+encVal.get("key").toString()+"\""+ //We will do encryption 
				     "} } } ";
				 }
			// UPI Open Intent (ANDROID)
			/*
			 * String phonepe_payload_card_initiate = "{"+
			 * "\"merchantId\":\""+PHONEPE_MID+"\","+ "\"merchantTransactionId\":\""+
			 * purchaseId+"\","+ "\"merchantUserId\":\""+userId+"\","+ "\"amount\":\""+
			 * txnAmount+"\","+ "\"redirectUrl\":\""+ PURCHASE_REDIRECT_URL +
			 * purchaseId+"\","+ "\"redirectMode\":\"POST\","+ "\"callbackUrl\":\""+
			 * PHONEPE_CALLBACK_URL+"\","+ "\"paymentInstrument\":{"+
			 * "\"type\":\"PAY_PAGE\""+ "}}";
			 */
			
				 phonepe_payload_card_initiate=finalRequest+valDiffStr;
			
			System.out.println("PHONEPE-INITIATE-REQUEST"+ "raw_payload=====     "+ phonepe_payload_card_initiate);
			        
			//String encoded = Buffer.from(phonepe_payload_card_initiate, "utf8").toString("base64");
			String encoded =Base64.getEncoder().encodeToString(phonepe_payload_card_initiate.getBytes());

			//console.log('');
			System.out.println("PHONEPE-INITIATE-REQUEST"+ "encoded=====      "+ encoded);
			


			String checksum = encoded + "/pg/v1/pay" +saltKey;

			
			System.out.println("PHONEPE-INITIATE-REQUEST"+ "checksum====    "+ checksum);

			//String hash = crypto.createHash("sha256").update(checksum, "utf-8").digest("hex");
			String hash =HashCrypto(checksum);
			
			System.out.println("PHONEPE-INITIATE-REQUEST"+ "hash----    "+ hash);

//			String phonepe_request = "{"+
//				    "\"method\": \"POST\","+
//				    "\"url\":\""+ PHONEPE_REQUEST_URL+"\","+
//				    "\"headers\": {"+
//						"\"Content-Type\": \"application/json\","+
//						"\"X-VERIFY\":\""+ hash + "###" + saltIndex + "\""+
//				    "},"+
//				    "\"body\":{"+ 
//				        "\"request\":\""+ encoded+"\""+
//				    "}"+
//				"}";
			
			System.out.println("encoded----    "+encoded);
			
			MediaType mediaType = MediaType.parse("application/json");
    		RequestBody body = RequestBody.create(mediaType, "{\"request\":\""+encoded+"\"}");
    		Request request = new Request.Builder()
    		  .url(PHONEPE_REQUEST_URL)
    		  .post(body)
    		  .addHeader("accept", "application/json")
    		  .addHeader("Content-Type", "application/json")
    		  .addHeader("X-VERIFY", hash + "###" + saltIndex)
    		  .build();
			okhttp3.Response response = client.newCall(request).execute();
			
			System.out.println("Response body---     "+response.body());
			System.out.println("Response body---     "+response.message());
			System.out.println("Response body---     "+response.code());
			System.out.println("Response body---     "+response.toString());
			
			responseStr=response.body().string();
//			{"success":true,"code":"PAYMENT_INITIATED","message":"Payment initiated",
//			"data":{"merchantId":"GAMEZDADDYUAT","merchantTransactionId":"1006",
//			"transactionId":"T2306071554587550922237",
//			"instrumentResponse":{"type":"PAY_PAGE",
//			"redirectInfo":{"url":"https://mercury-uat.phonepe.com/transact/simulator?token=EsK1KHMD5RibgTPNRCk0KzXyGiFbmuGQp52GVhROqBV"
//			,"method":"GET"}}}}
//		
//		  ObjectMapper objectMapper = new ObjectMapper();
//		  ResponseBean bean = objectMapper.readValue(response.body().string(), ResponseBean.class);
		
			/*
			 * Gson gson = new Gson(); ResponseBean bean =
			 * gson.fromJson(response.body().string(), ResponseBean.class);
			 * 
			 * 
			 * System.out.println("bean::::::::::::::::::::     "+bean);
			 * System.out.println("bean::::::::::::::::::::     "+ bean.getCode());
			 * System.out.println("bean::::::::::::::::::::     "+ bean.getMessage());
			 * System.out.println("bean::::::::::::::::::::     "+ bean.getSuccess());
			 * System.out.println("bean::::::::::::::::::::     "+ bean.getData());
			 * System.out.println("bean::::::::::::::::::::     "+
			 * bean.getData().getMerchantId());
			 * System.out.println("bean::::::::::::::::::::     "+
			 * bean.getData().getMerchantTransactionId());
			 * System.out.println("bean::::::::::::::::::::     "+
			 * bean.getData().getTransactionId());
			 * System.out.println("bean::::::::::::::::::::     "+
			 * bean.getData().getInstrumentResponse().getType());
			 * System.out.println("bean::::::::::::::::::::     "+
			 * bean.getData().getInstrumentResponse().getRedirectInfo().getMethod());
			 * System.out.println("bean::::::::::::::::::::     "+
			 * bean.getData().getInstrumentResponse().getRedirectInfo().getUrl());
			 */
		
			System.out.println("Responser json is --->"+responseStr);
//			savePhonePeRequest(bean);
			return responseStr;
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return responseStr;
	}
	
	public HashMap encCard12(HashMap input) {
		// Get an instance of the RSA key generator
		HashMap result=new HashMap();
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(4096);
        
			// Generate the KeyPair
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
			// Get the public and private key
			PublicKey publicKey = keyPair.getPublic();
			PrivateKey privateKey = keyPair.getPrivate();
        
			System.out.println("Original Text  : "+input);
        
			// Encryption
			byte[] cipherTextArray = encrypt(input.get("CardNumber").toString(), publicKey);
			String encryptedText = Base64.getEncoder().encodeToString(cipherTextArray);
			System.out.println("Card Number : "+encryptedText);
			result.put("encText", encryptedText);
			
			byte[] cvvTextArray = encrypt(input.get("CVVNumber").toString(), publicKey);
			String encryptedCvvText = Base64.getEncoder().encodeToString(cvvTextArray);
			//System.out.println("Encrypted Text : "+encryptedCvvText);
			System.out.println("cvv Number : "+encryptedCvvText);
			result.put("encCVVText", encryptedCvvText);
        
			// Decryption
			String decryptedText = decrypt(cipherTextArray, privateKey);
			System.out.println("DeCrypted Text : "+decryptedText);
			result.put("key", decryptedText);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public HashMap ras4096Enc(String CardNo,String cvvNo) {
		HashMap finalEnc=new HashMap();
		try {
			 // Generate key pair
	        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
	        keyPairGenerator.initialize(4096); // 4096-bit key size
	        KeyPair keyPair = keyPairGenerator.generateKeyPair();

	        // Get public and private keys
	        PublicKey publicKey = keyPair.getPublic();
	        PrivateKey privateKey = keyPair.getPrivate();

	        // Original message
	        //String originalMessage = "Hello, RSA!";
	        System.out.println("Original message: " + CardNo);
	        //for card number----
	        // Encrypt the message
	        byte[] encryptedMessage = encrypt(CardNo, publicKey);
	        System.out.println("Card  Number is ---?"+encryptedMessage.toString());
	        finalEnc.put("encText", encryptedMessage);
	        //for cvv ---
	        System.out.println("Cvv no is ---"+cvvNo);
	        byte[] encryptedMessageCvv = encrypt(cvvNo, publicKey);
	        System.out.println("key ---?"+encryptedMessageCvv.toString());
	        finalEnc.put("encCVVText", encryptedMessageCvv);
	        
	        
	        // Decrypt the message for key
	        String decryptedMessage = decrypt(encryptedMessage, privateKey);
	        System.out.println("Decrypted message: " + decryptedMessage.toString());
	        finalEnc.put("key", decryptedMessage);
		}
	    
	   catch(Exception e) {
			e.printStackTrace();
		}
		return finalEnc;
	}

	 
	public static byte[] encrypt(String message, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(message.getBytes());
    }

    public static String decrypt(byte[] encryptedMessage, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedMessage);
        return new String(decryptedBytes);
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
