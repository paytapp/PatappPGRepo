package com.paymentgateway.phonePe;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class PhonePeTransactionStatusCheck {

	@Autowired
	PhonePeCommon common;
	
	    public static void main(String[] args) throws Exception {
	    	PhonePeTransactionStatusCheck phonePeStatusCheck = new PhonePeTransactionStatusCheck();
	    	phonePeStatusCheck.checkPaymentStatus("1006");
	    }
	    
	    public String checkPaymentStatus(String merchantTransactionId) throws Exception {
	    	
    		HashMap prop = common.readProperty("phonePe.properties");
		    final String PHONEPE_SALTKEY = prop.get("PHONEPE_SALTKEY").toString();
		    final String PHONEPE_SALT_INDEX = prop.get("PHONEPE_SALT_INDEX").toString();
		    final String PHONEPE_MID = prop.get("PHONEPE_MID").toString();
		    final String PHONEPE_HOST = prop.get("PHONEPE_BASE_URL").toString();
		    final String PHONEPE_STATUS_CHECK_ENDPOINT = prop.get("PHONEPE_STATUS_CHECK_ENDPOINT").toString();
		    final String PHONEPE_STATUS_CHECK_API_URL = PHONEPE_HOST + PHONEPE_STATUS_CHECK_ENDPOINT + PHONEPE_MID + "/";
		    String responseStr = null;	
		    
		    try {
		        String status_payload = "/pg/v1/status/" + PHONEPE_MID + "/" + merchantTransactionId + PHONEPE_SALTKEY;
		        System.out.println("");
		        System.out.println("PHONEPE-STATUS-CHECK" + " status_payload " + status_payload);
		        
		        String hash =HashCrypto(status_payload);  
		        
		        System.out.println("");
		        System.out.println("PHONEPE-STATUS-CHECK" + " hash " + hash);
		
		        String url = PHONEPE_STATUS_CHECK_API_URL + merchantTransactionId;
		        System.out.println("");
		        System.out.println("PHONEPE-STATUS-CHECK" + " url " + url);
		        
		        System.out.println("X-VERIFY             " + hash + "###" + PHONEPE_SALT_INDEX );
		        System.out.println("PHONEPE_MID         " + PHONEPE_MID);
		        System.out.println("url        " + url);
		        System.out.println("");
		        System.out.println("");
		        
		        OkHttpClient client = new OkHttpClient();
		        Request request = new Request.Builder()
		          	  .url(url)
		          	  .get()
		          	  .addHeader("accept", "application/json")
		          	  .addHeader("Content-Type", "application/json")
		          	  .addHeader("X-VERIFY", hash + "###" + PHONEPE_SALT_INDEX)
		          	  .addHeader("X-MERCHANT-ID", PHONEPE_MID)
		          	  .build();
		        Response response = client.newCall(request).execute();
		        
		        
		        System.out.println("PHONEPE-STATUS-CHECK" + " phonepe_response " + response);
		        
				responseStr=response.body().string();

		        Gson gson = new Gson();
		        StatusCheckResponseBean bean = gson.fromJson(responseStr, StatusCheckResponseBean.class);
				
		        System.out.println("bean::::::::::::::::::::     "+ bean);
				System.out.println("bean::::::::::::::::::::     "+ bean.getCode());
				System.out.println("bean::::::::::::::::::::     "+ bean.getMessage());
				System.out.println("bean::::::::::::::::::::     "+ bean.getSuccess());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getMerchantId());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getMerchantTransactionId());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getTransactionId());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getAmount());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getState());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getResponseCode());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getPaymentInstrument().getType());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getPaymentInstrument().getUtr());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getPaymentInstrument().getCardType());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getPaymentInstrument().getPgTransactionId());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getPaymentInstrument().getBankTransactionId());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getPaymentInstrument().getPgAuthorizationCode());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getPaymentInstrument().getArn());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getPaymentInstrument().getBankId());
				System.out.println("bean::::::::::::::::::::     "+ bean.getData().getPaymentInstrument().getPgServiceTransactionId());
			
		        
		        
		        return responseStr;
		        
	    	} catch (java.io.IOException e1) {
				e1.printStackTrace();
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
	    
	}
