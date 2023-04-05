package com.paymentgateway.commons.util;

import org.scribe.model.Response;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;
import org.springframework.stereotype.Service;

@Service
public class GoogleURLShortener {

	public Response getResponse (String longUrl){
		OAuthRequest oAuthRequest = new OAuthRequest(
				Verb.POST,PropertiesManager.propertiesMap.get(CrmFieldConstants.GOOGLE_URL_SHORTENER.getValue()));

		oAuthRequest.addHeader("Content-Type", "application/json");
		String json = "{\"longUrl\": \"" + longUrl + "\"}";
		oAuthRequest.addPayload(json);
		return oAuthRequest.send();
		
		
	}
}
