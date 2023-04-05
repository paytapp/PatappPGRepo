package com.paymentgateway.globalpay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Shaiwal
 *
 */
@Service("globalpayTransactionCommunicator")
public class TransactionCommunicator {

	
	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateSaleResponse(Fields fields, String paymentUrl) {

		fields.put(FieldType.GLOBALPAY_FINAL_REQUEST.getName(), paymentUrl);
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

	}


	public String refundPostRequest(String request, String hostUrl) throws SystemException {
		
		// Refund is not supported by Globalpay
		String response = "";
		return response;
	}

	public String statusEnqPostRequest(String requestJson , String hostUrl,Transaction transaction) throws SystemException {

		String stringResponse = "";
		try {

			logger.info("Globalpay Status request = {}", requestJson);
			
			OkHttpClient client = new OkHttpClient();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, requestJson);
			Request request = new Request.Builder().url(hostUrl).post(body)
					.addHeader("content-type", "application/json")
					.addHeader("cache-control", "no-cache")
					.addHeader("MERCHANT-KEY", transaction.getMerchant_key())
					.addHeader("MERCHANT-ID", transaction.getMerchant_id())
					.build();
			Response response = client.newCall(request).execute();
			stringResponse = response.body().string();
			logger.info("Response received for Globalpay Status request {} ", stringResponse.toString());
			
			return stringResponse.toString();

		} catch (Exception e) {
			logger.error("Exception in getting Status Response for Globalpay", e);
		}
		return stringResponse;
	}
	
	public String getPaymentResponse(String requestJson, Fields fields, Transaction transaction) throws SystemException {
		
		String stringResponse = "";
		try {

			String hostUrl = PropertiesManager.propertiesMap.get(Constants.GLOBALPAY_SALE_URL);

			logger.info("Globalpay Sale request = {}", requestJson);
			
			OkHttpClient client = new OkHttpClient();
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, requestJson);
			Request request = new Request.Builder().url(hostUrl).post(body)
					.addHeader("content-type", "application/json")
					.addHeader("cache-control", "no-cache")
					.addHeader("MERCHANT-KEY", transaction.getMerchant_key())
					.addHeader("MERCHANT-ID", transaction.getMerchant_id())
					.build();
			Response response = client.newCall(request).execute();
			stringResponse = response.body().string();
			
			logger.info("Response received for Globalpay sale URL request {} ", stringResponse.toString());
			return stringResponse.toString();

		} catch (Exception e) {
			logger.error("Exception in getting Sale URL Response for Globalpay", e);
		}
		return stringResponse;
	}
	

}
