package com.paymentgateway.sendsms;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.phonePe.PhonePeCommon;

import java.time.LocalDateTime;  

public class SendSMSKaleyra {
	static OkHttpClient client = new OkHttpClient();
	@Autowired
	PhonePeCommon common;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String baceURL="https://api.kaleyra.io/v1/<SID>";
		SendSMSKaleyra smsKaleyra=new SendSMSKaleyra();
		try {
			smsKaleyra.sendSMSOTP("TX-GMZDDY","9079382565","1234","TX-GMZDDY","OTP is 1234","Prefix",
					"GMZDDY","UNI1234","CBM_profile");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public boolean sendSMSOTP(String SENDER_ID,String TO_NUMBER,String TEMPLATE_ID,String FROM,String MESSAGE_BODY,String PRIFIX,
			String ENTITY_ID,String UNICODE,String CALLBACK_PROFILE_ID) {
		boolean sendSMSFlag=false;
		String requestStrURL="";
		try {
			String CONTENT_TYPE="OTP";
			String REF_NAME="Arvind";
			String REF_AGE="30";
			String SLUG="1234";
			
			HashMap prop = common.readProperty("emailer.properties");
			
			
			String SHORTEN_URL=prop.get("SMS_KALEYRA_SHORTENURL").toString();
			String TRACE_USER=prop.get("USER_KALEYRA").toString();
			String CALLBACK_URL=prop.get("CALLBACK_KALEYRA_URL").toString();
			String API_KEY=prop.get("KALEYRA_TEST_KEY").toString();
			String smsURL = prop.get("SMS_KALEYRA_URL").toString()+TRACE_USER;
			
			//curl --location --request POST '<URL>/v1/<SID>/messages/json' \
			 DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
			   LocalDateTime now = LocalDateTime.now();  
			  // System.out.println(dtf.format(now));  
			   String responseStr="";
			
			requestStrURL="{"+
					    "\"channel\": \"SMS\","+
					    "\"type\": \""+CONTENT_TYPE+"\","+
					    "\"source\":\"API\","+
					    "\"prefix\": \""+PRIFIX+"\","+
					    "\"time\":\""+now+"\","+
					    "\"from\": \""+FROM+"\","+
					    "\"body\":\""+MESSAGE_BODY+"\","+
					    "\"template_id\":\""+TEMPLATE_ID+"\","+
					    "\"entity_id\": \""+ENTITY_ID+"\","+
					    "\"unicode\": \""+UNICODE+"\","+
					    "\"callback_profile_id\": \""+CALLBACK_PROFILE_ID+"\","+
					    "\"url_data\": {\"shorten_url\":\""+SHORTEN_URL+"\",\"url\":\""+smsURL+"\",\"slug\":\""+SLUG+"\",\"callback_url\":\""+
					    		CALLBACK_URL+"\",\"track_user \":\""+TRACE_USER+"\"},"+
					    "\"ref_custom\": {"+
					    	"\"ref_name\": \""+REF_NAME+"\","+
					    	"\"ref_age\": \""+REF_AGE+"\""+
					    "},"+
					    "\"sms\": ["+
					        "{"+
					            "\"to\": \""+TO_NUMBER+"\","+
					            "\"from\": \""+FROM+"\","+
					            "\"body\":\""+MESSAGE_BODY+"\","+
					            "\"template_id\": \""+TEMPLATE_ID+"\","+
					            "\"entity_id\": \""+ENTITY_ID+"\","+
					            "\"unicode\": \""+UNICODE+"\","+
					            "\"callback_profile_id\": \""+CALLBACK_PROFILE_ID+"\","+
					            "\"url_data\": {\"shorten_url\":\""+SHORTEN_URL+"\",\"url\":\""+smsURL+"\",\"slug\":\""+SLUG+"\",\"callback_url\":\""+
					            CALLBACK_URL+"\",\"track_user \":\""+TRACE_USER+"\"},"+
					            "\"ref_custom\": {"+
					            	"\"ref_name\": \""+REF_NAME+"\","+
					            	"\"ref_age\": \""+REF_AGE+"\""+
					            "}"+
					         "}"+
					    "]"+
					 "}";
			
			System.out.println("Request bodu is ---->"+requestStrURL);
				
			MediaType mediaType = MediaType.parse("application/json");
    		RequestBody body = RequestBody.create(mediaType, requestStrURL);
    		Request request = new Request.Builder()
    		  .url(smsURL)
    		  .post(body)
    		  .addHeader("api-key", API_KEY)
    		  .addHeader("Content-Type", "application/json")
    		  .build();
			okhttp3.Response response = client.newCall(request).execute();
			
			System.out.println("Response body---     "+response.body());
			System.out.println("Response body---     "+response.message());
			System.out.println("Response body---     "+response.code());
			System.out.println("Response body---     "+response.toString());
			
			responseStr=response.body().string();
			if(response.code()==200) {
				sendSMSFlag=true;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return sendSMSFlag;
	}

}