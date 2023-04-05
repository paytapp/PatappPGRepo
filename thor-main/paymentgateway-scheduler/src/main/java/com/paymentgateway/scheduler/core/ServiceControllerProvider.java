package com.paymentgateway.scheduler.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.scheduler.commons.ConfigurationProvider;

@Service("serviceControllerProvider")
public class ServiceControllerProvider {

	@Autowired
	private ConfigurationProvider configurationProvider;

	private static final Logger logger = LoggerFactory.getLogger(ServiceControllerProvider.class);

	public void sendSms(JSONObject merchantData, String serviceUrl) throws SystemException {
		try {
			String responseBody = "";
			logger.info("Sending SMS, with service Url: " + serviceUrl + " and merchant data :" + merchantData);
			HttpPost request = new HttpPost(serviceUrl);
			RequestConfig config = RequestConfig.custom().setConnectTimeout(1800000)
					.setConnectionRequestTimeout(1800000).setSocketTimeout(1800000).build();
			request.setConfig(config);
			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
			try {
				StringEntity params = new StringEntity(merchantData.toString());
				request.addHeader("content-type", "application/json");
				request.setEntity(params);
				HttpResponse resp = httpClient.execute(request);
				HttpEntity response = resp.getEntity();
				responseBody = EntityUtils.toString(response);
				logger.info("Response received from SMS API " + responseBody);
			} catch (Exception e) {
				logger.error("Expired ", e);
			}
		} catch (Exception exception) {
			logger.error("Error communicating with SMS API " + exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating with SMS API");
		}
	}

	public String makeStatusEnquiry(JSONObject jsonObject, String serviceUrl) throws SystemException {
		try {
			String responseBody = "";
			HttpPost request = new HttpPost(serviceUrl);
			RequestConfig config = RequestConfig.custom().setConnectTimeout(180000).setConnectionRequestTimeout(180000)
					.setSocketTimeout(180000).build();
			request.setConfig(config);
			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
			try {
				StringEntity params = new StringEntity(jsonObject.toString());
				request.addHeader("content-type", "application/json");
				request.setEntity(params);
				HttpResponse resp = httpClient.execute(request);
				HttpEntity response = resp.getEntity();
				responseBody = EntityUtils.toString(response);
				return responseBody.toString();
			} catch (Exception e) {
				logger.error("Expired ", e);
			}
		} catch (Exception exception) {
			logger.error("Error making status enquiry ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error making status enquiry");
		}
		return null;
	}

	public void initiateRefund(String refundRequest) {
		try {
			String responseBody = "";
			HttpPost request = new HttpPost(configurationProvider.getRefundApiUrl());
			RequestConfig config = RequestConfig.custom().setConnectTimeout(60000).setConnectionRequestTimeout(60000)
					.setSocketTimeout(60000).build();
			request.setConfig(config);
			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
			try {
				StringEntity params = new StringEntity(refundRequest);
				request.addHeader("content-type", "application/json");
				request.setEntity(params);
				HttpResponse resp = httpClient.execute(request);
				HttpEntity response = resp.getEntity();
				responseBody = EntityUtils.toString(response);
				logger.info("Response received while making auto refund, " + responseBody.toString());
			} catch (Exception e) {
				logger.error("Expired ", e);
			}
		} catch (Exception exception) {
			logger.error("Error making refund from scheduler: ", exception);
		}
	}

	public void bankStatusEnquiry(JSONObject data, String bankStatusEnquiryUrl) throws SystemException {

		try {
			String serviceUrl = bankStatusEnquiryUrl;

			logger.info("Status Enquiry Service Url: " + serviceUrl);
			logger.info("Merchant data :" + data);

			HttpPost request = new HttpPost(serviceUrl);
			RequestConfig config = RequestConfig.custom().setConnectTimeout(3600000)
					.setConnectionRequestTimeout(3600000).setSocketTimeout(3600000).build();
			request.setConfig(config);

			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

			try {
				StringEntity params = new StringEntity(data.toString());

				request.addHeader("content-type", "application/json");
				request.setEntity(params);

				HttpResponse resp = httpClient.execute(request);
				HttpEntity response = resp.getEntity();
			} catch (Exception e) {
				logger.error("Status Enquiry Expired ", e);
			}

		} catch (Exception exception) {
			logger.error("Error communicating with Status Enquiry API ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating with Status Enquiry API");
		}
	}

	public Map<String, String> impsTransferTransact(JSONObject data, String impsServiceUrl) throws SystemException {
		String responseBody = "";
		String serviceUrl = impsServiceUrl;
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(data.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to Payout");
		}
		return resMap;
	}

	public Map<String, String> upiTransferTransact(JSONObject data, String upiServiceUrl) throws SystemException {
		String responseBody = "";
		String serviceUrl = upiServiceUrl;
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(data.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to Payout");
		}
		return resMap;
	}

	public String checkHealthProvider(JSONObject data, String serviceUrl) throws SystemException {
		String responseBody = "";
		String response = "400";
		JSONObject responseData = new JSONObject();
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(data.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			responseData = new JSONObject(responseBody);
			if (responseData.has("data") && ("200".equalsIgnoreCase(responseData.getString("data")))) {
				response = "200";
			}

		} catch (Exception exception) {
			logger.error("exception is ", exception);
			response = "400";
		}
		return response;
	}

//	public Map<String, String> autoRefundTxn(JSONObject data, String autoRefundServiceUrl) throws SystemException {
//	String responseBody = "";
//	String serviceUrl = autoRefundServiceUrl;
//	Map<String, String> resMap = new HashMap<String, String>();
//	try {
//
//		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//		HttpPost request = new HttpPost(serviceUrl);
//		StringEntity params = new StringEntity(data.toString());
//		request.addHeader("content-type", "application/json");
//		request.setEntity(params);
//		HttpResponse resp = httpClient.execute(request);
//		responseBody = EntityUtils.toString(resp.getEntity());
//		final ObjectMapper mapper = new ObjectMapper();
//		final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
//		resMap = mapper.readValue(responseBody, type);
//	} catch (Exception exception) {
//		logger.error("exception is " + exception);
//		throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
//	}
//	return resMap;
//}

	public String paytmSendOTP(JSONObject data, String serviceUrl) throws SystemException {
		String responseBody = "";
		String response = "400";
		JSONObject responseData = new JSONObject();
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(data.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			responseData = new JSONObject(responseBody);
			if (responseData.has("data") && ("200".equalsIgnoreCase(responseData.getString("data")))) {
				response = "200";
			}

		} catch (Exception exception) {
			logger.error("exception is ", exception);
			response = "400";
		}
		return response;
	}

	public String cryptoStatusEnquiry(JSONObject data, String serviceUrl) throws SystemException {
		String responseBody = "";
		String response = "400";
		JSONObject responseData = new JSONObject();
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(data.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			responseData = new JSONObject(responseBody);
			if (responseData.has("data") && ("200".equalsIgnoreCase(responseData.getString("data")))) {
				response = "200";
			}

		} catch (Exception exception) {
			logger.error("exception is ", exception);
			response = "400";
		}
		return response;
	}

	public void msedclRetryCallbackTransact(String data, String msedclPgWsTransactUrl) throws SystemException {
//		String responseBody = "";
		String serviceUrl = msedclPgWsTransactUrl;
		// Map<String, String> resMap = new HashMap<String, String>();
		try {

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(data);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
//			responseBody = EntityUtils.toString(resp.getEntity());
//			final ObjectMapper mapper = new ObjectMapper();
//			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
//			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PGWS");
		}
	}
}
