package com.paymentgateway.payout.globalPay;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class GlobalPayCommunicator {

	private static final Logger logger = LoggerFactory.getLogger(GlobalPayCommunicator.class);

	public String communication(String requestPayload, String url, Fields fields) {

		String responseBody = null;

		try {
			logger.info("Globalpay Payout request = {}", requestPayload);

			OkHttpClient client = new OkHttpClient();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, requestPayload);
			Request request = new Request.Builder().url(url).post(body).addHeader("content-type", "application/json")
					.addHeader("cache-control", "no-cache")
					.addHeader("MERCHANT-KEY", fields.get(FieldType.TXN_KEY.getName()))
					.addHeader("MERCHANT-ID", fields.get(FieldType.MERCHANT_ID.getName())).build();
			Response response = client.newCall(request).execute();
			responseBody = response.body().string();

			logger.info("Response received for Globalpay Payout request {} ", responseBody);
			return responseBody;

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
