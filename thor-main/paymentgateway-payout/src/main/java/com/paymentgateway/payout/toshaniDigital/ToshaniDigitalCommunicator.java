package com.paymentgateway.payout.toshaniDigital;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class ToshaniDigitalCommunicator {

	private static final Logger logger = LoggerFactory.getLogger(ToshaniDigitalCommunicator.class);

	public String communication(String requestPayload, String url, Fields fields) {

		String responseBody = null;

		try {

			List<NameValuePair> urlParameters = new ArrayList<>();

			JSONObject jsonObject = new JSONObject(requestPayload);
			Iterator<String> keys = jsonObject.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				urlParameters.add(new BasicNameValuePair(key, jsonObject.get(key).toString()));
			}

			HttpResponse response = postWithFormData(url, urlParameters);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity);
			JSONObject responseObject = new JSONObject(responseString);
			logger.info("Toshani Payout Response = {}", responseObject.toString());

			return responseObject.toString();

		} catch (Exception e) {
			logger.error("exception in communication() , ORDER_ID = {}", fields.get(FieldType.ORDER_ID.getName()) + " ",
					e);
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
			fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.INTERNAL_SYSTEM_ERROR.getResponseCode());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
		}
		return responseBody;

	}

	public HttpResponse postWithFormData(String url, List<NameValuePair> params) throws IOException {
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(url);

		request.setEntity(new UrlEncodedFormEntity(params));
		return httpClient.execute(request);
	}

}
