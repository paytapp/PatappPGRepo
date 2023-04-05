package com.paymentgateway.payout.FloxyPay;

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
public class FloxyPayComunicator {

	private static Logger logger = LoggerFactory.getLogger(FloxyPayComunicator.class);

	public String communication(String requestPayload, String url, Fields fields) {

		String responseBody = null;

		try {
			logger.info("FloxyPayComunicator() Transaction Request Payload >> {}  ORDER ID = {}", requestPayload,
					fields.get(FieldType.ORDER_ID.getName()));

			OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(15, TimeUnit.SECONDS)
					.writeTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();

			MediaType mediaType = MediaType.parse("application/json");

			RequestBody body = RequestBody.create(mediaType, requestPayload);
			Request request = new Request.Builder().url(url).method("POST", body)
					.addHeader("Content-Type", "application/json")
					.addHeader("x-key", fields.get(FieldType.ADF1.getName()))
					.addHeader("x-secret", fields.get(FieldType.ADF2.getName())).build();
			Response response = client.newCall(request).execute();

			String responseCode = String.valueOf(response.code());

			fields.put(FieldType.PG_RESP_CODE.getName(), responseCode);

			responseBody = response.body().string();

			logger.info("Transaction FloxyPay response Code >> {}  Response Body >> {} , For OrderID : {}",
					responseCode, responseBody, fields.get(FieldType.ORDER_ID.getName()));

			return responseBody;

		} catch (Exception e) {
			logger.info("exception in FloxyPay communicate() , ORDER_ID == {} , Exception  == {}",
					fields.get(FieldType.ORDER_ID.getName()), e);
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
			fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.INTERNAL_SYSTEM_ERROR.getResponseCode());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
		}
		return responseBody;

	}

}
