package com.paymentgateway.notification.email.emailApi;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.user.EmailData;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;

@Component("emailApi")
public class EmailApi {
	
	private final Logger logger = LoggerFactory.getLogger(EmailApi.class.getName());
	
	
	
	public String sendEmail(EmailData email){
		
		String statusName;
		try {
			JSONObject outer = new JSONObject();

			String url =   PropertiesManager.propertiesMap.get("ApiUrl");
			String apiKey =   PropertiesManager.propertiesMap.get("ApiKey");
			String emailFrom =   PropertiesManager.propertiesMap.get("MailSender");
			
			if(email==null){
				logger.info("Email class is empty");
				return null;
			}
			
			if(StringUtils.isBlank(email.getEmailTo())){
				logger.info("EmailTo is empty");
				return null;
			}
			
			
			JSONObject personalizationsObj = new JSONObject();
			JSONArray middle = new JSONArray();
			personalizationsObj.put("recipient", email.getEmailTo());
			
			middle.put(personalizationsObj);
			outer.put("personalizations", middle);
			outer.put("tags", email.getTag());
			
			JSONObject fromObj = new JSONObject();
			fromObj.put("fromEmail", emailFrom);
			fromObj.put("fromName", email.getFromName());
			outer.put("from", fromObj);
			
			outer.put("subject", email.getSubject());
			outer.put("content", email.getBody());
			
			if(StringUtils.isNotBlank(email.getFileContentbase64()) && StringUtils.isNotBlank(email.getFileName())){
				JSONArray middle1 = new JSONArray();
				JSONObject inside = new JSONObject();
				inside.put("fileContent",email.getFileContentbase64());
				inside.put("fileName",email.getFileName());
				middle1.put(inside);
				outer.put("attachments", middle1);
				
			}
		
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(url);
			request.toString();
			
			StringEntity params = new StringEntity(outer.toString());
			request.addHeader(Constants.CONTENT_TYPE.getValue(), Constants.APPLICATION_JSON.getValue());
			request.addHeader("api_key", apiKey);
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			StatusLine statusLine = resp.getStatusLine();
			int statusCode = resp.getStatusLine().getStatusCode();
			statusName = resp.getStatusLine().toString();
			logger.info("email Status code "+statusCode);
		} catch (Exception exception) {
			logger.error("error : " , exception);
			return null;
		}
				  
		return statusName;
	}

}
