package com.paymentgateway.phonePe;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.paymentgateway.commons.mongo.MongoClientConfiguration;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

@Service
public class initiatePhonepe {

	@Autowired
	PhonePeCommon common;
	
	@Autowired
	PhonePeService phonePeService;
	
	@Autowired
	PhonePeDao dao;
	
	@Autowired
	MongoClientConfiguration mongoClientConfiguration;
	
	@Autowired
	PhonePeDaoImpl daoImpl;
	
//	@Autowired
//	MPAMerchantDao mpaMerchantDao;
	
	String PHONEPE_REQUEST_URL;
	String saltKey;
	String saltIndex;
	static OkHttpClient client = new OkHttpClient();
	
	
	public static void main(String[] args) {
		initiatePhonepe ip=new initiatePhonepe();
		String purchaseId = "1006";
		String userId = "629388";
		int txnAmount = 116;
		try {
			ip.initiatePhonepePayment(purchaseId, userId, txnAmount);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String initiatePhonepePayment(String purchaseId, String userId, int txnAmount) throws IOException {
		//Properties prop = common.getAllProperties();
		
//		String purchaseId = "1006";
//		String userId = "629388";
//		int txnAmount = 116;
		txnAmount = txnAmount*100;
		String responseStr="";
		
		/*
		 * String baseUrl = prop.getProperty("PHONEPE_BASE_URL"); String payEndpoint =
		 * prop.getProperty("PHONEPE_PAY_ENDPOINT"); String PHONEPE_MID =
		 * prop.getProperty("PHONEPE_MID"); String PURCHASE_REDIRECT_URL =
		 * prop.getProperty("PURCHASE_REDIRECT_URL"); String PHONEPE_CALLBACK_URL =
		 * prop.getProperty("PHONEPE_CALLBACK_URL"); saltKey =
		 * prop.getProperty("PHONEPE_SALTKEY"); saltIndex =
		 * prop.getProperty("PHONEPE_SALT_INDEX"); PHONEPE_REQUEST_URL = baseUrl +
		 * payEndpoint;
		 */
		HashMap prop = common.readProperty("phonePe.properties");
		
		//phonepeResponse phonepeResponseData=new phonepeResponse();
		//txnAmount = txnAmount*100;
		
		String baseUrl = prop.get("PHONEPE_BASE_URL").toString();
		String payEndpoint = prop.get("PHONEPE_PAY_ENDPOINT").toString();
		String PHONEPE_MID = prop.get("PHONEPE_MID").toString();
		String PURCHASE_REDIRECT_URL = prop.get("PURCHASE_REDIRECT_URL").toString();
		String PHONEPE_CALLBACK_URL = prop.get("PHONEPE_CALLBACK_URL").toString();
		saltKey = prop.get("PHONEPE_SALTKEY").toString();
		saltIndex = prop.get("PHONEPE_SALT_INDEX").toString();
		PHONEPE_REQUEST_URL = baseUrl + payEndpoint;
		System.out.println("PHONEPEMID        "+ ":"+PHONEPE_MID+":");
//		MPAMerchant mpaMerchant = null;
		try {
		
//		mpaMerchant = mpaMerchantDao.findByPayId(PHONEPE_MID);
		
		
//		if(mpaMerchant != null) {
		
			String  phonepe_payload_card_initiate = "{"+
				    "\"merchantId\":\""+PHONEPE_MID+"\","+
				    "\"merchantTransactionId\":\""+ purchaseId+"\","+
				    "\"merchantUserId\":\""+userId+"\","+
				    "\"amount\":\""+ txnAmount+"\","+
				    "\"redirectUrl\":\""+ PURCHASE_REDIRECT_URL + purchaseId+"\","+
				    "\"redirectMode\":\"POST\","+
				    "\"callbackUrl\":\""+ PHONEPE_CALLBACK_URL+"\","+
				    "\"paymentInstrument\":{"+
						"\"type\":\"PAY_PAGE\""+	
				    "}}";
			
			
			System.out.println("PHONEPE-INITIATE-REQUEST"+ "raw_payload=====     "+ phonepe_payload_card_initiate);
			        
			String encoded =Base64.getEncoder().encodeToString(phonepe_payload_card_initiate.getBytes());

			System.out.println("PHONEPE-INITIATE-REQUEST"+ "encoded=====      "+ encoded);
			


			String checksum = encoded + "/pg/v1/pay" +saltKey;

			
			System.out.println("PHONEPE-INITIATE-REQUEST"+ "checksum====    "+ checksum);

			String hash =HashCrypto(checksum);
			
			System.out.println("PHONEPE-INITIATE-REQUEST"+ "hash----    "+ hash);
			
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
			System.out.println("responseStr::::::::::::::::::::     "+ responseStr);
			
			String[] splitStr = responseStr.split("(?=\"data)");
			responseStr = splitStr[0] + "\"amount\":\"" + (txnAmount/100) + "\"," + splitStr[1];
		
			Gson gson = new Gson();
			ResponseBean bean = gson.fromJson(responseStr , ResponseBean.class);
	        
			
			System.out.println("bean::::::::::::::::::::     "+ bean);
			System.out.println("bean::::::::::::::::::::     "+ bean.getCode());
			System.out.println("bean::::::::::::::::::::     "+ bean.getMessage());
			System.out.println("bean::::::::::::::::::::     "+ bean.getSuccess());
			System.out.println("bean::::::::::::::::::::     "+ bean.getAmount());
			System.out.println("bean::::::::::::::::::::     "+ bean.getData());
			System.out.println("bean::::::::::::::::::::     "+ bean.getData().getMerchantId());
			System.out.println("bean::::::::::::::::::::     "+ bean.getData().getMerchantTransactionId());
			System.out.println("bean::::::::::::::::::::     "+ bean.getData().getTransactionId());
			System.out.println("bean::::::::::::::::::::     "+ bean.getData().getInstrumentResponse().getType());
			System.out.println("bean::::::::::::::::::::     "+ bean.getData().getInstrumentResponse().getRedirectInfo().getMethod());
			System.out.println("bean::::::::::::::::::::     "+ bean.getData().getInstrumentResponse().getRedirectInfo().getUrl());
		
			
			System.out.println("converted responseStr::::::::::::::::::::     "+ responseStr);

			
			savePhonePeRequest(bean, purchaseId, userId);
			return responseStr;
			
		
			
//		}else {
//			return "Merchant not Registered";
//			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		 return responseStr;
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
	
	
	public void savePhonePeRequest(ResponseBean bean, String purchaseId, String userId) {
		
		try {
		Transaction transaction = new Transaction();
		PaymentResponse response = new PaymentResponse();		
		
		response.setSuccess(bean.getSuccess());
		response.setCode(bean.getCode());
		response.setMessage(bean.getMessage());
		response.setMerchantId(bean.getData().getMerchantId());
		response.setMerchantTransactionId(bean.getData().getMerchantTransactionId());
		response.setTransactionId(bean.getData().getTransactionId());
		response.setType(bean.getData().getInstrumentResponse().getType());
		response.setUrl(bean.getData().getInstrumentResponse().getRedirectInfo().getUrl());
		response.setMethod(bean.getData().getInstrumentResponse().getRedirectInfo().getMethod());
		response.setCreatedDate(new Date());
		response.setCreatedBy("");
		 
		transaction.setPurchaseId(purchaseId);
		transaction.setUserId(userId);
		transaction.setTransactionAmount(bean.getAmount());
		transaction.setTransactionSuccess(bean.getSuccess());
		transaction.setPaymentResponseIdRef(response);
		transaction.setMerchantId(bean.getData().getMerchantId());
		transaction.setCreatedDate(new Date());
		transaction.setCreatedBy("");
		
		
		
		System.out.println("response.setSuccess               :::::::     "+ response.getSuccess());
		System.out.println("response.setCode                  :::::::     "+ response.getCode());
		System.out.println("response.setMessage               :::::::     "+ response.getMessage());
		System.out.println("response.setMerchantId            :::::::     "+ response.getMerchantId());
		System.out.println("response.setMerchantTransactionId :::::::     "+ response.getMerchantTransactionId());
		System.out.println("response.setTransactionId         :::::::     "+ response.getTransactionId());
		System.out.println("response.setType     			  :::::::     "+ response.getType());
		System.out.println("response.setUrl  			      :::::::     "+ response.getUrl());
		System.out.println("response.setMethod    			  :::::::     "+ response.getMethod());
		System.out.println("response.setCreatedDate   	 	  :::::::     "+ response.getCreatedDate());
		System.out.println("response.setCreatedBy   		  :::::::     "+ response.getCreatedBy());
		
		System.out.println("transaction.setPurchaseId   		:::::::   "+ transaction.getPurchaseId());
		System.out.println("transaction.setUserId   		    :::::::   "+ transaction.getUserId());
		System.out.println("transaction.setTransactionAmount   	:::::::   "+ transaction.getTransactionAmount());
		System.out.println("transaction.setTransactionSuccess   :::::::   "+ transaction.getTransactionSuccess());
		System.out.println("transaction.setPaymentResponseIdRef :::::::   "+ transaction.getPaymentResponseIdRef());
		System.out.println("transaction.setMerchantId   		:::::::   "+ transaction.getMerchantId());
		System.out.println("transaction.setCreatedDate   		:::::::   "+ transaction.getCreatedDate());
		System.out.println("transaction.setCreatedBy   		    :::::::   "+ transaction.getCreatedBy());

		
		
		
		
		
//		daoImpl.getMerchantById("GAMEZDADDYUAT");
//		daoImpl.findAllTransaction();
		phonePeService.savePhonePeTransaction(transaction);
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		

		 }

}
