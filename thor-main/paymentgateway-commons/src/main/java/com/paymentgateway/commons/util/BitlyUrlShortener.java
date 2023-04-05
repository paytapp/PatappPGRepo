package com.paymentgateway.commons.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;


@Service
public class BitlyUrlShortener {
	
	
	public String createShortUrlUsingBitly(String longUrl) {
		String responseBody = "";
		try {
			
			String serviceUrl   =  PropertiesManager.propertiesMap.get("BIT_LY_URL");// URL from application.yml file
			String accessToken = "Bearer " + PropertiesManager.propertiesMap.get("BIT_LY_ACCESS_TOKEN");// Token from application.yml file
			
			HttpPost request = new HttpPost(serviceUrl);
			RequestConfig config = RequestConfig.custom().setConnectTimeout(1800000)
					.setConnectionRequestTimeout(1800000).setSocketTimeout(1800000).build();
			request.setConfig(config);
			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

			JSONObject json = new JSONObject();
			json.put(Constants.DOMAIN.getValue(), "bit.ly"); 
			json.put(Constants.LONG_URL.getValue(), longUrl);

			StringEntity params = new StringEntity(json.toString());
			request.addHeader(Constants.AUTHORIZATION.getValue(), accessToken);
			request.addHeader(Constants.CONTENT_TYPE.getValue(), "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			HttpEntity response = resp.getEntity();
			responseBody = EntityUtils.toString(response);
			JSONObject jsonResponse = new JSONObject(responseBody);
			
			//If response is empty or null it will return long URL 
			if(StringUtils.isNotBlank(jsonResponse.getString("id"))){
				return jsonResponse.getString("id");
			}else{
				return longUrl;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return longUrl;
		}
		
	}

}
