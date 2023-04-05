package com.paymentgateway.toshanidigital;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Shaiwal
 *
 */
@Service("toshanidigitalTransactionCommunicator")
public class TransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateSaleResponse(Fields fields, String paymentUrl) {

		fields.put(FieldType.TOSHANIDIGITAL_FINAL_REQUEST.getName(), paymentUrl);
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

	}

	public String refundPostRequest(String request, String hostUrl) throws SystemException {

		// Refund is not supported by Toshani
		String response = "";
		return response;
	}

	public String statusEnqPostRequest(String hostUrl, String request, Transaction transaction) throws SystemException {

		String stringResponse = "";
		try {

			logger.info("Toshani Status request = {}", request);

			List<NameValuePair> urlParameters = new ArrayList<>();
			
			if (StringUtils.isNotBlank(transaction.getOrderid())) {
				urlParameters.add(new BasicNameValuePair(Constants.orderid, transaction.getOrderid()));
			}
			else if (StringUtils.isNotBlank(transaction.getOrder_id())) {
				urlParameters.add(new BasicNameValuePair(Constants.orderid, transaction.getOrder_id()));
			}
			
			urlParameters.add(new BasicNameValuePair(Constants.access_token, transaction.getAccess_token()));
			
			HttpResponse response = postWithFormData(hostUrl, urlParameters);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity);
			JSONObject responseObject = new JSONObject(responseString);
			logger.info("Toshani Status Response = {}", responseObject.toString());

			return responseObject.toString();

		} catch (Exception e) {
			logger.error("Exception in getting Status Enquiry Response for Toshani", e);
		}
		return stringResponse;
	}

	public String getPaymentResponse(String request, Fields fields, Transaction transaction) throws SystemException {

		String stringResponse = "";
		try {

			logger.info("Toshani Sale request = {}", request);
			String hostUrl = PropertiesManager.propertiesMap.get(Constants.TOSHANIDIGITAL_SALE_URL);

			List<NameValuePair> urlParameters = new ArrayList<>();

			JSONObject jsonObject = new JSONObject(request);
			Iterator<String> keys = jsonObject.keys();

			while(keys.hasNext()) {
			    String key = keys.next();
			    urlParameters.add(new BasicNameValuePair(key, jsonObject.get(key).toString()));
			}
			
			HttpResponse response = postWithFormData(hostUrl, urlParameters);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity);
			JSONObject responseObject = new JSONObject(responseString);
			logger.info("Toshani Sale Response = {}", responseObject.toString());

			return responseObject.toString();

		} catch (Exception e) {
			logger.error("Exception in getting Sale Response for Toshani", e);
		}
		return stringResponse;
	}

	public HttpResponse postWithFormData(String url, List<NameValuePair> params) throws IOException {
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(url);

		request.setEntity(new UrlEncodedFormEntity(params));
		return httpClient.execute(request);
	}
}
