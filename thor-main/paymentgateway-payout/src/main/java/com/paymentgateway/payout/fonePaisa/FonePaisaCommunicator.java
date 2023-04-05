package com.paymentgateway.payout.fonePaisa;

import java.util.concurrent.TimeUnit;

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
public class FonePaisaCommunicator {
	
	private static final Logger logger = LoggerFactory.getLogger(FonePaisaCommunicator.class);
	
	public String communication(String requestPayload, String url, Fields fields) {

		String responseBody = null;

		try {
			logger.info("Fone Paisa Transaction Request Payload = " + requestPayload + " ORDER ID "
					+ fields.get(FieldType.ORDER_ID.getName()));

			OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS)
					.writeTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

			MediaType mediaType = MediaType.parse("application/json");

			RequestBody body = RequestBody.create(mediaType, requestPayload);
			Request request = new Request.Builder().url(url).method("POST", body)
					.addHeader("Content-Type", "application/json").build();
			Response response = client.newCall(request).execute();

			String responseCode = String.valueOf(response.code());

			fields.put(FieldType.PG_RESP_CODE.getName(), responseCode);

			responseBody = response.body().string();

			logger.info("Transaction Fone Paisa response Code == " + responseCode + " Response Body " + responseBody
					+ " ORDER_ID " + fields.get(FieldType.ORDER_ID.getName()));

			return responseBody;

		} catch (Exception e) {
			logger.info("exception in communicate() , ORDER_ID"+fields.get(FieldType.ORDER_ID.getName())+" ",e);
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
			fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.INTERNAL_SYSTEM_ERROR.getResponseCode());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
		}
		return responseBody;

	}

	

}
