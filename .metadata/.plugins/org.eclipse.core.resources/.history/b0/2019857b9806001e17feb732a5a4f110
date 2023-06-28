package com.paymentgateway.phonepe;

/*
 * Author Arvind Chaturvedi
 * 
 * For Phonepe payment 
 */


import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.PayuUtil;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.paymentgateway.payu.Constants;

@Service
public class phonepeService {
	
	private static Logger logger = LoggerFactory.getLogger(phonepeService.class.getName());
	
	public Map<String, String> getResponse(Map<String, String> reqMap) throws SystemException {
		String hostUrl = "";
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			hostUrl = PropertiesManager.propertiesMap.get(Constants.PHONEPE_REQUEST_URL);
			URL url = new URL(hostUrl);

			OkHttpClient client = new OkHttpClient();

			Request request = new Request.Builder()
			  .url(Constants.PHONEPE_REQUEST_URL)
			  .post(null)
			  .addHeader("accept", "application/json")
			  .addHeader("Content-Type", "application/json")
			  .build();

			Response response = client.newCall(request).execute();
			
			if(response!=null) {
				if(response.code()==200) {
					
					responseMap.put("status", "success");
				}else{
					responseMap.put("status", "fail");
				}
			}else {
				responseMap.put("status", "fail");
			}
			
			
			
			
			//responseMap=

			//responseMap = emiBinResoinseHandle(emiBinResponse, reqMap.get("txnAmount"), reqMap.get("bankName"));

			
			
			
		}catch(Exception e) {
			logger.error("Exception in Payu emiBinRequest ", e);
			responseMap.put("status", "fail");
		}
		
		
		return responseMap;
		
	}

}
