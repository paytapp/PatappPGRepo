package com.paymentgateway.commons.api;
/*
package com.paymentgateway.commons.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class CryptoControllerServiceProvider {

	PropertiesManager propertiesManager = new PropertiesManager();
	
	
	public String decryptPassword(String password) {

		try {
			String serviceUrl = propertiesManager.getservicesURL("CryptoDecryptURL");
			StringBuilder uri = new StringBuilder();
			uri.append(serviceUrl);

			Map<String, Object> params = new LinkedHashMap<>();
			params.put("data", password);

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0)
					postData.append("&");

				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}

			uri.append(postData);
			URL url = new URL(uri.toString());

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));

			StringBuilder res = new StringBuilder();
			for (String c; (c = in.readLine()) !=null;) {
				res.append(c);
			}
			
			
			return res.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String encryptPassword(String password) {
		try {
			
			String serviceUrl = propertiesManager.getservicesURL("CryptoEncryptURL");
			StringBuilder uri = new StringBuilder();
			uri.append(serviceUrl);

			Map<String, Object> params = new LinkedHashMap<>();
			params.put("data", password);

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0)
					postData.append("&");

				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}

			uri.append(postData);
			URL url = new URL(uri.toString());

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));

			StringBuilder res = new StringBuilder();
			for (String c; (c = in.readLine()) !=null;) {
				res.append(c);
			}
			
			
			return res.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
}
*/