package com.paymentgateway.commons.util;

import java.lang.reflect.Type;
import java.util.Map;
import org.scribe.model.Response;
import org.springframework.stereotype.Service;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;

@Service
public class UrlShortener {

	public String shortenUrl(String longUrl) {

		Response response = new GoogleURLShortener().getResponse(longUrl);
		
		Type typeOfMap = new TypeToken<Map<String, String>>() {
			private static final long serialVersionUID = -5804690425921813742L;
		}.getType();
		Map<String, String> responseMap = new GsonBuilder().create().fromJson(
				response.getBody(), typeOfMap);
		return responseMap.get("id");

	}
}
