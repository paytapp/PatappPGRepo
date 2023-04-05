package com.paymentgateway.commons.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Token;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

@Service("transactionControllerServiceProvider")
public class TransactionControllerServiceProvider {

	@Autowired
	PropertiesManager propertiesManager;

	private static Logger logger = LoggerFactory.getLogger(TransactionControllerServiceProvider.class.getName());

	public Map<String, String> decrypt(String payId, String encData) throws SystemException {

		String responseBody = "";
		String serviceUrl = ConfigurationConstants.CRYPTO_DECRYPTION_SERVICE_URL.getValue();
		Map<String, String> resMap = new HashMap<String, String>();
		try {
			JSONObject json = new JSONObject();
			json.put(FieldType.PAY_ID.getName(), payId);
			json.put(FieldType.ENCDATA.getName(), encData);
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to crypto module");
		}
		return resMap;
	}

	public Map<String, String> encrypt(String payId, String requestString) throws SystemException {

		String responseBody = "";
		String serviceUrl = ConfigurationConstants.CRYPTO_ENCRYPTION_SERVICE_URL.getValue();
		Map<String, String> resMap = new HashMap<String, String>();
		try {
			JSONObject json = new JSONObject();
			json.put(FieldType.PAY_ID.getName(), payId);
			json.put(FieldType.ENCDATA.getName(), requestString);
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to crypto module");
		}
		return resMap;
	}

	public Map<String, String> hostedDecrypt(String payId, String encData) throws SystemException {

		String responseBody = "";
		String serviceUrl = ConfigurationConstants.HOSTED_CRYPTO_DECRYPTION_SERVICE_URL.getValue();
		Map<String, String> resMap = new HashMap<String, String>();
		try {
			JSONObject json = new JSONObject();
			json.put(FieldType.PAY_ID.getName(), payId);
			json.put(FieldType.ENCDATA.getName(), encData);
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to crypto module");
		}
		return resMap;
	}

	public Map<String, String> hostedEncrypt(String payId, String requestString) throws SystemException {

		String responseBody = "";
		String serviceUrl = ConfigurationConstants.HOSTED_CRYPTO_ENCRYPTION_SERVICE_URL.getValue();
		Map<String, String> resMap = new HashMap<String, String>();
		try {
			JSONObject json = new JSONObject();
			json.put(FieldType.PAY_ID.getName(), payId);
			json.put(FieldType.ENCDATA.getName(), requestString);
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to crypto module");
		}
		return resMap;
	}

	public String amexDecrypt(String response, String encryptionKey) {

		try {

			String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSAmexUtilDecryptURL");
			StringBuilder uri = new StringBuilder();
			uri.append(serviceUrl);

			Map<String, Object> params = new LinkedHashMap<>();
			params.put("response", response);
			params.put("encryptionKey", encryptionKey);

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0)
					postData.append("&");

				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}

			uri.append(postData);
			URL url = new URL(uri.toString());

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));

			StringBuilder res = new StringBuilder();
			for (String c; (c = in.readLine()) != null;) {
				res.append(c);
			}

			return res.toString();

		} catch (Exception e) {
			logger.error("Exception : ", e);
		}
		return null;
	}

	public Map<String, String> upiEnquiry(Fields fields, String url) throws SystemException {
		String responseBody = "";

		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, fields.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception in getting upi status ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, Token> getAll(Fields fields) {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSGetAllTokenURL");
		Map<String, Token> resMap = new HashMap<String, Token>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, fields.get(fieldType));
			}

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());

			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());

			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);

		} catch (Exception e) {
			logger.error("exceptoin is ", e);
			e.printStackTrace();
		}
		return resMap;
	}

	public String validatePaymentType(Fields fields) {

		try {
			String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSValidatePaymentTypeURL");
			StringBuilder uri = new StringBuilder();
			uri.append(serviceUrl);

			URL url = new URL(uri.toString());

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));

			StringBuilder res = new StringBuilder();
			for (String c; (c = in.readLine()) != null;) {
				res.append(c);
			}

			return res.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, String> migsTransact(Fields fields, String url) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get(url);
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, fields.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> transact(Fields fields, String url) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get(url);
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, fields.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> eCollectionTransact(String decryptedString, String url) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get(url);
		Map<String, String> resMap = null;
		try {

			// JSONObject json = new JSONObject();
			// List<String> fieldTypeList = new
			// ArrayList<String>(fields.getFields().keySet());
			// for (String fieldType : fieldTypeList) {
			// json.put(fieldType, fields.get(fieldType));
			// }
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(decryptedString);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			resMap = new HashMap<String, String>();
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
			// resMap = mapper.readValue(responseBody, new
			// TypeReference<HashMap<String,String>>(){});
			logger.error("Response from Httpclient inside eCollectionTransact is " + resp);
			logger.error("ResponseBody inside eCollectionTransact is " + responseBody);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> settlementTransact(Fields fields) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSSettlementProcessor");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, fields.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> impsTransferTransact(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSImpsTransferURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> MerchantDirectInitiateTransact(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSMerchantDirectInitiateTransferURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}
	
	public Map<String, String> MerchantDirectInitiateStatusEnq(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSPayoutStatusEnquiryURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> impsStatusTransact(Map<String, String> requestMapTxnId) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSImpsStatusURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {
			JSONObject json = new JSONObject();

			List<String> fieldTypeList = new ArrayList<String>(requestMapTxnId.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, requestMapTxnId.get(fieldType));
			}

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> nodalTransferTransact(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSNodalTransferProcessor");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> nodalTopupTransferTransact(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSNodalTopupTransferURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> compositeAccountStatementTransact(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSCompositeAccountStatementURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> beneVerificationTransact(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSBeneVerificationProcessor");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> vpaBeneVerificationTransact(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSVpaBeneVerificationUrl");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> MobikwikWLVerifyUser(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSMobiwikWLVerifyUserURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> PaytmVerifyUser(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSPaytmVerifyUserURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> MobikwikWLSendOtp(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSMobiwikWLSendOtpURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> MobikwikWLGetBalance(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSMobiwikWLCheckBalanceURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> MobikwikWLAddMoneyInWallet(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSMobiwikWLAddMoneyURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	// public Map<String, String> MobikwikWLDeductMoney(Map<String,String> req)
	// throws SystemException {
	// String responseBody = "";
	// String serviceUrl =
	// PropertiesManager.propertiesMap.get("TransactionWSMobiwikWLDeductMoneyURL");
	// Map<String, String> resMap = new HashMap<String, String>();
	// try {
	//
	// JSONObject json = new JSONObject();
	// List<String> fieldTypeList = new ArrayList<String>(req.keySet());
	// for (String fieldType : fieldTypeList) {
	// json.put(fieldType, req.get(fieldType));
	// }
	// CloseableHttpClient httpClient = HttpClientBuilder.create().build();
	// HttpPost request = new HttpPost(serviceUrl);
	// StringEntity params = new StringEntity(json.toString());
	// request.addHeader("content-type", "application/json");
	// request.setEntity(params);
	// HttpResponse resp = httpClient.execute(request);
	// responseBody = EntityUtils.toString(resp.getEntity());
	// final ObjectMapper mapper = new ObjectMapper();
	// final MapType type = mapper.getTypeFactory().constructMapType(Map.class,
	// String.class, Object.class);
	// resMap = mapper.readValue(responseBody, type);
	// } catch (Exception exception) {
	// logger.error("exception is " + exception);
	// throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error
	// communicating to PG WS");
	// }
	// return resMap;
	// }

	public Map<String, String> upiTransferTransact(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSUpiTransferURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> upiStatusTransact(Map<String, String> requestMapTxnId) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSUpiStatusURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {
			JSONObject json = new JSONObject();

			List<String> fieldTypeList = new ArrayList<String>(requestMapTxnId.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, requestMapTxnId.get(fieldType));
			}

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	// Add Bene IMPS
	public Map<String, String> impsAddBeneTransferTransact(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSImpsAddBeneTransferURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	// Add Bene VPA
	public Map<String, String> vpaAddBeneTransferTransact(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionWSUpiAddBeneTransferURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public void PaymentGatewayHostedS2SAcknowledgement(Fields fields) throws SystemException {
		// String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("PaymentGatewayHostedAcknowledgement");
		// Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				if (StringUtils.isNotBlank(fields.get(fieldType))) {
					json.put(fieldType, fields.get(fieldType));
				}
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
		} catch (Exception exception) {
			logger.error("exception is >>", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
	}

	public void merchantHostedS2SAcknowledgement(Fields fields) throws SystemException {
		// String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("MerchantHostedAcknowledgement");
		// Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(fields.getFields().keySet());
			for (String fieldType : fieldTypeList) {
				if (StringUtils.isNotBlank(fields.get(fieldType))) {
					json.put(fieldType, fields.get(fieldType));
				}
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			// responseBody = EntityUtils.toString(resp.getEntity());
			// final ObjectMapper mapper = new ObjectMapper();
			// final MapType type =
			// mapper.getTypeFactory().constructMapType(Map.class, String.class,
			// Object.class);
			// resMap = mapper.readValue(responseBody, type);
			// logger.info("Response from Httpclient inside
			// merchantHostedS2SAcknowledgement is " + resp);
			// logger.info("Response from Httpclient inside
			// merchantHostedS2SAcknowledgement is " + resMap);
			// logger.info("ResponseBody inside merchantHostedS2SAcknowledgement
			// is " + responseBody);
		} catch (Exception exception) {
			logger.error("exception is >> ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
	}

	public Map<String, String> PaytmWLCreateToken(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionPaytmWLCreateTokenURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> PaytmWLSendOtp(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionPaytmWLSendOtpURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> PaytmWLVerfiyOtpAndCheckBalance(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionPaytmWLVerfiyOtpAndCheckBalanceURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> processTransaction(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionPaytmWLCreateTokenURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> paytmWalletCheck(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TransactionPaytmWLWalletBalanceURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {
			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> communicatePaytmPayoutApi(Map<String, String> requestMap, String url)
			throws SystemException {

		String responseBody = "";
		String serviceUrl = url;
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(requestMap.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, requestMap.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;

	}
	
	public void callBackforPayoutTransactions(Map<String, String> req, String url) throws SystemException {
		logger.info("inside callBackforPayoutTransactions() ");
		String responseBody = "";
		String serviceUrl = url;
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			logger.info("response code, callBackforPayoutTransactions() "+resp.getStatusLine().getStatusCode()+""
					+ " for txn Id "+req.get(FieldType.TXN_ID.getName())+" response msg "+responseBody);
			
		} catch (Exception exception) {
			logger.error("exception is callBackforPayoutTransactions()", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to To Callback Url "+url);
		}
	}

	
	public Map<String, String> emiBincheck(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("PayuEmiBinCheckApi");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Error communicating to PG WS");
		}
		return resMap;
	}

	public Map<String, String> getTransactionStatus(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("StatusEnquiryProcess");
		String key = PropertiesManager.propertiesMap.get("enquiryApiKey");
		Map<String, String> resMap = new HashMap<String, String>();
		try {
			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			json.put(FieldType.KEY_ID.getName(), key);
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
					"Error communicating to pgws, getTransactionStatus() ");
		}
		return resMap;
	}

	public String sendCallbackToMerchant(Map<String, String> req) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("TRANSACT_PGWS_SEND_CALLBACK");
		Map<String, String> resMap = new HashMap<String, String>();
		try {

			JSONObject json = new JSONObject();
			List<String> fieldTypeList = new ArrayList<String>(req.keySet());
			for (String fieldType : fieldTypeList) {
				json.put(fieldType, req.get(fieldType));
			}
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());

		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
					"Error communicating to pgws, sendCallbackToMerchant() ");
		}
		return responseBody;
	}
	
	
	public Map<String, String> submitP2PTSPUTR(String requestData, Fields fields) throws SystemException {
		String responseBody = "";
		String serviceUrl = PropertiesManager.propertiesMap.get("P2PTSPSUBMITURL");
		Map<String, String> resMap = new HashMap<String, String>();
		try {
			JSONObject json = new JSONObject(requestData);
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("merchantId", fields.get(FieldType.MERCHANT_ID.getName()));
			request.addHeader("verificationToken", fields.get(FieldType.TXN_KEY.getName()));
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
					"Error communicating to pgws, sendCallbackToMerchant() ");
		}
		return resMap;
	}
}
