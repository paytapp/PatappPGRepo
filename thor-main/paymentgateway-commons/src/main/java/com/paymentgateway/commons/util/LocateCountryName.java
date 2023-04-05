package com.paymentgateway.commons.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.stereotype.Service;
@Service
public class LocateCountryName {
	private final static String USER_AGENT = "Mozilla/5.0";

	public String findCountryCode(String remoteAddr)  {
		String countryNameCode = null;
		try{
			String url = (ConfigurationConstants.LOCATE_COUNTRY_NAME.getValue());
			String completeUrl=url.concat(remoteAddr);
			URL obj = new URL(completeUrl);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;

			StringBuilder response=new StringBuilder();

			while ((inputLine = bufferedReader.readLine()) != null) {
				response.append(inputLine);
			}
			bufferedReader.close();

			//print result
			String[] parts = response.toString().split(";");
			if(parts.length > 0)
				countryNameCode=parts[parts.length-1].replace("-", "");

		}catch(Exception exception){

		}
		return countryNameCode;
	}

}
