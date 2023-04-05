package com.paymentgateway.bindb.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service
@Scope(value="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
public class BinRangeCommunicator {
	private final static String USER_AGENT = "Mozilla/5.0";

	public StringBuilder getCommunicator(String requestUrl, String cardBin) throws IOException {
		String requestString = requestUrl.concat(cardBin);
		URL url = new URL(requestString);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

		httpURLConnection.setRequestMethod("GET");

		// add request header
		httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = bufferedReader.readLine()) != null) {
			response.append(inputLine);
		}
		bufferedReader.close();
		return response;
	}

}
