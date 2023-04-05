package com.paymentgateway.crm.mpa;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class MPAServiceController {

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	MPAFileEncoder encoder;

	private static Logger logger = LoggerFactory.getLogger(MPAServiceController.class.getName());

	public String authenticationLoggingIn() throws SystemException {
		try {
			String responseBody = "";
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.AUTHENTICATION_LOGIN_URL);
			HttpPost request = new HttpPost(serviceUrl);
			RequestConfig config = RequestConfig.custom().setConnectTimeout(Constants.TIME_OUT)
					.setConnectionRequestTimeout(Constants.TIME_OUT).setSocketTimeout(Constants.TIME_OUT).build();
			request.setConfig(config);
			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("username", PropertiesManager.propertiesMap.get(Constants.USERNAME));
			jsonObject.put("password", PropertiesManager.propertiesMap.get(Constants.PASSWORD));
			try {
				StringEntity params = new StringEntity(jsonObject.toString());
				request.addHeader("content-type", "application/json");
				request.setEntity(params);
				HttpResponse resp = httpClient.execute(request);
				HttpEntity response = resp.getEntity();
				responseBody = EntityUtils.toString(response);
				logger.info("Response received from Signzy : " + responseBody);
				return responseBody;
			} catch (Exception e) {
				logger.error("Expired " , e);
			}
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}

	public String eSignAuthenticationLoggingIn() throws SystemException {
		try {
			String responseBody = "";
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.ESIGN_AUTHENTICATION_LOGIN_URL);
			HttpPost request = new HttpPost(serviceUrl);
			RequestConfig config = RequestConfig.custom().setConnectTimeout(Constants.TIME_OUT)
					.setConnectionRequestTimeout(Constants.TIME_OUT).setSocketTimeout(Constants.TIME_OUT).build();
			request.setConfig(config);
			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("username", PropertiesManager.propertiesMap.get(Constants.USERNAME));
			jsonObject.put("password", PropertiesManager.propertiesMap.get(Constants.PASSWORD));
			try {
				StringEntity params = new StringEntity(jsonObject.toString());
				request.addHeader("content-type", "application/json");
				request.setEntity(params);
				HttpResponse resp = httpClient.execute(request);
				HttpEntity response = resp.getEntity();
				responseBody = EntityUtils.toString(response);
				logger.info("Response received from Signzy : " + responseBody);
				return responseBody;
			} catch (Exception e) {
				logger.error("Expired " , e);
			}
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}
	
	public String authenticationLogOut(JSONObject authenticationResponse) throws SystemException {
		try {
			String responseBody = "";
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.AUTHENTICATION_LOGOUT_URL)
					.replace("ACCESSTOKEN", authenticationResponse.getString("id"));
			HttpPost request = new HttpPost(serviceUrl);
			RequestConfig config = RequestConfig.custom().setConnectTimeout(Constants.TIME_OUT)
					.setConnectionRequestTimeout(Constants.TIME_OUT).setSocketTimeout(Constants.TIME_OUT).build();
			request.setConfig(config);
			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
			try {
				HttpResponse resp = httpClient.execute(request);
				HttpEntity response = resp.getEntity();
				responseBody = EntityUtils.toString(response);
				logger.info("Response received from Signzy : " + responseBody);
				return responseBody;
			} catch (Exception e) {
				logger.error("Expired " + e);
			}
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}
	
	public String eSignAuthenticationLogOut(JSONObject authenticationResponse) throws SystemException {
		try {
			String responseBody = "";
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.ESIGN_AUTHENTICATION_LOGOUT_URL)
					.replace("ACCESSTOKEN", authenticationResponse.getString("id"));
			HttpPost request = new HttpPost(serviceUrl);
			RequestConfig config = RequestConfig.custom().setConnectTimeout(Constants.TIME_OUT)
					.setConnectionRequestTimeout(Constants.TIME_OUT).setSocketTimeout(Constants.TIME_OUT).build();
			request.setConfig(config);
			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
			try {
				HttpResponse resp = httpClient.execute(request);
				HttpEntity response = resp.getEntity();
				responseBody = EntityUtils.toString(response);
				logger.info("Response received from Signzy : " + responseBody);
				return responseBody;
			} catch (Exception e) {
				logger.error("Expired " + e);
			}
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}
	

	public JSONObject rocCompanyNameSearch(String companyName, JSONObject companyObject) {
		try {
			HttpsURLConnection connection = null;
			StringBuilder serverResponse = new StringBuilder();
			String hostUrl = PropertiesManager.propertiesMap.get(Constants.SIMPLE_CIN_URL);
			URL url = new URL(hostUrl);
			
			JSONObject request=new JSONObject();
			request.put("target", "Organization");
			request.put("itemId", companyObject.get("id"));
			request.put("accessToken", companyObject.get("accessToken"));
			request.put("task", "searchCompaniesByName");
			
			
			
			JSONObject essentialsJsonObj=new JSONObject();
			essentialsJsonObj.put("name", companyName);
			
			request.put("essentials", essentialsJsonObj);
			
			logger.info("request for rocCompanyNameSearch()");

			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();
			
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}
			
			rd.close();
			String str = serverResponse.toString();
			
			logger.info("Response received from CIN by Company Name API : " + str);
			
			JSONObject responseJson=new JSONObject(str);
			
			JSONObject companyResultData=(JSONObject) responseJson.get("result");
			JSONArray JsonResultArrayObj=(JSONArray) companyResultData.get("companiesList");
			
			if(JsonResultArrayObj.length()!=0){
				return JsonResultArrayObj.getJSONObject(0);
			}
			return new JSONObject();
		} catch (IOException e) {
			logger.error("Error communicating with Signzy API, " , e);
		}
		return null;
	}
	
	public JSONObject rocCompanyObject(JSONObject authenticationResponse) {
		try {
			HttpsURLConnection connection = null;
			StringBuilder serverResponse = new StringBuilder();
			String hostUrl = PropertiesManager.propertiesMap.get(Constants.ORGANIZATION_OBJECT_URL).replace("PATRONID",
					PropertiesManager.propertiesMap.get(Constants.SIGNZY_USERID));
			URL url = new URL(hostUrl);
			
			String callbackUrl = PropertiesManager.propertiesMap.get(Constants.MPA_CALLBACK_URL);;
			JSONObject request=new JSONObject();
			request.put("identifier", Constants.ORGANIZATION);
			request.put("service",Constants.ROC);
			request.put("callbackUrl", callbackUrl);
			
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", authenticationResponse.getString("id"));
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);

			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}
			
			rd.close();
			String str = serverResponse.toString();
			
			logger.info("Response received from ROC Company Object by API : " + str);
			return new JSONObject(str.toString());
		} catch (IOException e) {
			logger.error("Error communicating with Signzy API, " , e);
		}
		return null;
	}

	public JSONObject identitiesFlow(JSONObject authenticationResponseMap, String serviceType, String emailId,
			String imageUrl) throws SystemException {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.IDENTITIES_FLOW_URL).replace("PATRONID",
					authenticationResponseMap.getString("userId"));

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			connection.setRequestProperty(Constants.Authorization, authenticationResponseMap.getString("id"));

			JSONObject request = new JSONObject();
			request.put(Constants.TYPE, serviceType);
			request.put(Constants.EMAIL, emailId);
			request.put(Constants.CALLBACK_URL, PropertiesManager.propertiesMap.get(Constants.MPA_CALLBACK_URL));

			String imageUrls[] = imageUrl.split(",");
			if (imageUrls.length > 0 && StringUtils.isNotBlank(imageUrls[0])) {
				JSONArray urlArray = new JSONArray();
				for (int i = 0; i < imageUrls.length; i++) {
					urlArray.put(imageUrls[i]);
				}
				request.put(Constants.IMAGES, urlArray);
			}

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}

			logger.info("Response received from Signzy : " + serverResponse);
			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}

	public JSONObject eSignUrlGenerationFlow(JSONObject authenticationResponseMap,
			String uidName, String fileUrl) throws SystemException {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.ESIGN_FLOW_URL).replace("CUSTOMERID",
					authenticationResponseMap.getString("userId"));

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			connection.setRequestProperty(Constants.Authorization, authenticationResponseMap.getString("id"));

			JSONObject request = new JSONObject();
			request.put(Constants.TASK, "url");
			request.put(Constants.CALLBACK_URL, PropertiesManager.propertiesMap.get(Constants.ESIGN_CALLBACK_URL));
			request.put(Constants.REDIRECT_URL, "");
			request.put("inputFile", fileUrl);
			request.put("name", uidName);
			request.put("multiPages", "true");
			request.put("signaturePosition", "Customize");
			request.put("pageNo", "5");
			request.put("signatureType", "aadhaaresign");
			request.put("xCoordinate", "250");
			request.put("yCoordinate", "-200");
			request.put("height", "250");
			request.put("width", "150");
			
			
			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}

			logger.info("Response received from Signzy : " + serverResponse);
			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}
	public JSONObject panVerification1(JSONObject identitiesResponse, String panNumber, String name) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.IDENTITY_VERIFICATION_URL);

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");

			JSONObject essentials = new JSONObject();
			JSONObject request = new JSONObject();
			request.put(Constants.SERVICE, Constants.IDENTITY);
			request.put(Constants.ITEM_ID, identitiesResponse.getString("id"));
			request.put(Constants.TASK, Constants.VERIFICATION);
			request.put(Constants.ACCESS_TOKEN, identitiesResponse.getString("accessToken"));

			essentials.put(Constants.NUMBER, panNumber);
			essentials.put(Constants.NAME, name);
			essentials.put(Constants.FUZZY, "true");

			request.put(Constants.ESSENTIALS, essentials);

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}

			logger.info("Response received from Signzy : " + serverResponse);
			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}

	public String panVerification2(JSONObject identitiesResponse, String identity, String verification2, String emailId,
			String imageUrl) {
		// TODO Auto-generated method stub

		return null;
	}

	public JSONObject createOrganizationObjectForCin(JSONObject authenticationResponseMap, String cin) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.ORGANIZATION_OBJECT_URL)
					.replace("PATRONID", authenticationResponseMap.getString("userId"));

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			connection.setRequestProperty(Constants.Authorization, authenticationResponseMap.getString("id"));

			JSONObject request = new JSONObject();
			request.put(Constants.IDENTIFIER, cin);
			request.put(Constants.SERVICE, Constants.ROC);
			request.put(Constants.CALLBACK_URL, PropertiesManager.propertiesMap.get(Constants.MPA_CALLBACK_URL));

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}

			logger.info("Response received from Signzy : " + serverResponse);
			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}

	public JSONObject simpleSearchByCin(JSONObject organizationObject, String cin) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.SIMPLE_CIN_URL);

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");

			JSONObject request = new JSONObject();
			request.put(Constants.TARGET, Constants.ORGANIZATION);
			request.put(Constants.ITEM_ID, organizationObject.getString("id"));
			request.put(Constants.ACCESS_TOKEN, organizationObject.getString("accessToken"));
			request.put(Constants.TASK, Constants.SIMPLE_SEARCH_BY_CIN);
			JSONObject cinObject = new JSONObject();
			cinObject.put(Constants.CIN, cin);
			request.put(Constants.ESSENTIALS, cinObject);

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}

			logger.info("Response received from Signzy : " + serverResponse);
			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;

	}

	public JSONObject serachGstByPanNumber(JSONObject authenticationResponse, String businessPan, String companyEmailId,
			String state) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.PAN_TO_GST_URL).replace("PATRONID",
					PropertiesManager.propertiesMap.get(Constants.SIGNZY_USERID));

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			connection.setRequestProperty(Constants.Authorization, authenticationResponse.getString("id"));

			JSONObject essentials = new JSONObject();
			essentials.put(Constants.PAN_NUMBER, businessPan);
			essentials.put(Constants.STATE, state);
			essentials.put(Constants.EMAIL, companyEmailId);
			JSONObject request = new JSONObject();
			request.put(Constants.TASK, Constants.PAN_SEARCH);
			request.put(Constants.ESSENTIALS, essentials);

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}

			logger.info("Response received from Signzy : " + serverResponse);
			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}

	public JSONObject extractElectricityBillByCANo(JSONObject authenticationResponse, String consumerNumber,
			String electricityProvider) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.ELECTRICITY_BILL_EXTRACTION_URL)
					.replace("PATRONID", authenticationResponse.getString("userId"));

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			connection.setRequestProperty(Constants.Authorization, authenticationResponse.getString("id"));

			JSONObject essentials = new JSONObject();
			essentials.put(Constants.CONSUMER_NO, consumerNumber);
			essentials.put(Constants.ELECTRICITY_PROVIDER, electricityProvider);
			JSONObject request = new JSONObject();
			request.put(Constants.ESSENTIALS, essentials);

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}
			logger.info("Response received from Signzy for Address Proofs by Electricity Bill with CA Number: "
					+ consumerNumber + " response: " + serverResponse);
			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}

	public JSONObject uploadImageForExtraction(JSONObject authenticationResponse, String encodedData) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.FILE_UPLOAD_URL);
			String imageDataBytes = encodedData.substring(encodedData.indexOf(",") + 1);
			byte[] bytes = Base64.decodeBase64(imageDataBytes);
			String mimeType = encoder.getImageType(bytes);

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");

			JSONObject request = new JSONObject();
			request.put("base64String", encodedData);
			request.put("mimetype", mimeType);
			request.put("protected", true);
			request.put("ttl", "10 mins");

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}

			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}
	

	public JSONObject cancelledChequeExtraction(JSONObject identitiesResponse) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.IDENTITY_VERIFICATION_URL);
			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			JSONObject essentials = new JSONObject();
			JSONObject request = new JSONObject();
			request.put(Constants.SERVICE, Constants.IDENTITY);
			request.put(Constants.ITEM_ID, identitiesResponse.getString("id"));
			request.put(Constants.TASK, Constants.AUTO_RECOGNITION);
			request.put(Constants.ACCESS_TOKEN, identitiesResponse.getString("accessToken"));
			request.put(Constants.ESSENTIALS, essentials);
			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}
			logger.info("Response received from Signzy for uploaded cheque" + serverResponse);
			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}
	
	public JSONObject drivingLicenseExtraction(JSONObject identitiesResponse) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.IDENTITY_VERIFICATION_URL);
			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			JSONObject essentials = new JSONObject();
			JSONObject request = new JSONObject();
			request.put(Constants.SERVICE, Constants.IDENTITY);
			request.put(Constants.ITEM_ID, identitiesResponse.getString("id"));
			request.put(Constants.TASK, Constants.ADVANCE_EXTRACTION);
			request.put(Constants.ACCESS_TOKEN, identitiesResponse.getString("accessToken"));
			request.put(Constants.ESSENTIALS, essentials);
			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}
			logger.info("Response received from Signzy for uploaded cheque" + serverResponse);
			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}

	public JSONObject drivingLicenseVerification(JSONObject identitiesResponse, String licenceNumber, String dob,
			String doi) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.IDENTITY_VERIFICATION_URL);

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");

			JSONObject essentials = new JSONObject();
			JSONObject request = new JSONObject();

			request.put(Constants.SERVICE, Constants.IDENTITY);
			request.put(Constants.ITEM_ID, identitiesResponse.getString("id"));
			request.put(Constants.TASK, Constants.VERIFICATION);
			request.put(Constants.ACCESS_TOKEN, identitiesResponse.getString("accessToken"));

			essentials.put(Constants.NUMBER, licenceNumber);
			essentials.put(Constants.DOB, dob);
			essentials.put(Constants.ISSUE_DATE, doi);

			request.put(Constants.ESSENTIALS, essentials);

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}

			logger.info("Response received from Signzy : " + serverResponse);
			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}

	public String entityNegativeList(JSONObject authenticationResponse, String cin) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.AML_CFT).replace("PATRONID",
					authenticationResponse.getString("userId"));

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			connection.setRequestProperty(Constants.Authorization, authenticationResponse.getString("id"));

			JSONObject essentials = new JSONObject();
			essentials.put(Constants.CIN, cin);

			JSONObject request = new JSONObject();
			request.put(Constants.TASK, Constants.ENTITY_NEGATIVE_LIST);
			request.put(Constants.ESSENTIALS, essentials);

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}
			logger.info("Response received from Signzy for entityNegativeList from CIN: " + cin + " response, "
					+ serverResponse.toString());
			rd.close();
			connection.disconnect();
			return serverResponse.toString();
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}

	public String directorNegativeList(JSONObject authenticationResponse, String din) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.AML_CFT).replace("PATRONID",
					authenticationResponse.getString("userId"));

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			connection.setRequestProperty(Constants.Authorization, authenticationResponse.getString("id"));

			JSONObject essentials = new JSONObject();
			essentials.put(Constants.DIN, din);

			JSONObject request = new JSONObject();
			request.put(Constants.TASK, Constants.DIRECTOR_NEGATIVE_LIST);
			request.put(Constants.ESSENTIALS, essentials);

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}
			logger.info("Response received from Signzy for Director/Secretary Negative List from DIN: " + din
					+ " response, " + serverResponse.toString());
			rd.close();
			connection.disconnect();
			return serverResponse.toString();
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}

	public String verifyBankAccount(JSONObject authenticationResponse, JSONObject chequeExtractionResponse) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.BANK_ACCOUNT_VERIFICATION_URL)
					.replace("PATRONID", authenticationResponse.getString("userId"));

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			connection.setRequestProperty(Constants.Authorization, authenticationResponse.getString("id"));

			JSONObject response = chequeExtractionResponse.getJSONObject("response");
			JSONObject result = response.getJSONObject("result");

			String accountNumber = "";
			String ifsc = "";
			String name = "";
			String bankContact = "";

			if (result.has("accountNumber") && (StringUtils.isNotBlank(result.getString("accountNumber")))) {
				accountNumber = result.getString("accountNumber");
			}
			if (result.has("ifsc") && (StringUtils.isNotBlank(result.getString("ifsc")))) {
				ifsc = result.getString("ifsc");
			}
			if (result.has("name") && (StringUtils.isNotBlank(result.getString("name")))) {
				name = result.getString("name");
			}
			if (result.has("contact") && (StringUtils.isNotBlank(result.getString("contact")))) {
				bankContact = result.getString("contact");
			}

			JSONObject essentials = new JSONObject();
			if (StringUtils.isNotBlank(accountNumber)) {
				essentials.put(Constants.BENEFICIARY_ACCOUNT, accountNumber);
			}
			if (StringUtils.isNotBlank(ifsc)) {
				essentials.put(Constants.BENEFICIARY_IFSC, ifsc);
			}
			if (StringUtils.isNotBlank(bankContact)) {
				// essentials.put(Constants.BENEFICIARY_MOBILE, bankContact);
			}
			if (StringUtils.isNotBlank(name)) {
				essentials.put(Constants.BENEFICIARY_NAME, name);
			}

			essentials.put(Constants.FUZZY, true);

			JSONObject request = new JSONObject();
			request.put(Constants.TASK, Constants.BANK_TRANSFER);
			request.put(Constants.ESSENTIALS, essentials);

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}
			logger.info("Response received from Signzy for Bank Account Transfer for accountNumber: " + accountNumber
					+ ", IFSC: " + ifsc + ", Name: " + name + ", Contact: " + bankContact + " response, "
					+ serverResponse.toString());
			rd.close();
			connection.disconnect();
			return serverResponse.toString();
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}

	public JSONObject initializeGSTROTPRequest(JSONObject authenticationResponse, String gstin, String gstnUsername) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.GSTR_INITIATION_URL).replace("PATRONID",
					authenticationResponse.getString("userId"));

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			connection.setRequestProperty(Constants.Authorization, authenticationResponse.getString("id"));

			JSONObject essentials = new JSONObject();
			JSONObject request = new JSONObject();

			essentials.put(Constants.GSTIN, gstin);
			essentials.put(Constants.GSTR_USERNAME, gstnUsername);

			request.put(Constants.TASK, Constants.GET_OTP);
			request.put(Constants.ESSENTIALS, essentials);

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}

			logger.info("Response received from Signzy for GSTR OTP Request: " + serverResponse);
			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}

	public JSONObject initializeGSTRAuthTokenRequest(JSONObject authenticationResponse, String gstin, String gstinOtp,
			String gstinUsername, String appKey) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.GSTR_INITIATION_URL).replace("PATRONID",
					authenticationResponse.getString("userId"));

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			connection.setRequestProperty(Constants.Authorization, authenticationResponse.getString("id"));

			JSONObject essentials = new JSONObject();
			JSONObject request = new JSONObject();

			essentials.put(Constants.GSTIN, gstin);
			essentials.put(Constants.GSTR_USERNAME, gstinUsername);
			essentials.put(Constants.OTP, gstinOtp);
			essentials.put(Constants.APP_KEY, appKey);

			request.put(Constants.TASK, Constants.GET_AUTH_TOKEN);
			request.put(Constants.ESSENTIALS, essentials);

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}

			logger.info("Response received from Signzy for GSTR Auth Token Request: " + serverResponse);
			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}

	public JSONObject initializeGSTR3bSummaryRequest(String gstin, String gstinUsername,
			JSONObject authenticationResponse, String appKey, JSONObject gstrAuthTokenResponse, int attempt) {
		try {
			attempt++;
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.GSTR3B_URL).replace("PATRONID",
					authenticationResponse.getString("userId"));

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			connection.setRequestProperty(Constants.Authorization, authenticationResponse.getString("id"));

			JSONObject result = gstrAuthTokenResponse.getJSONObject("result");
			String authToken = result.getString("authToken");
			String sek = result.getString("sek");

			JSONObject essentials = new JSONObject();
			JSONObject request = new JSONObject();

			SimpleDateFormat format = new SimpleDateFormat("MM/yyyy");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -2);
			String retPeriod = format.format(cal.getTime());

			essentials.put(Constants.GSTIN, gstin);
			essentials.put(Constants.GSTR_USERNAME, gstinUsername);
			essentials.put(Constants.AUTH_TOKEN, authToken);
			essentials.put(Constants.APP_KEY, appKey);
			essentials.put(Constants.SEK, sek);
			essentials.put(Constants.RET_PERIOD, retPeriod);

			request.put(Constants.TASK, Constants.SUMMARY);
			request.put(Constants.ESSENTIALS, essentials);

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}

			logger.info("Response received from Signzy for GSTR3b Summary Request: " + serverResponse);
			rd.close();
			connection.disconnect();
			JSONObject response = new JSONObject(serverResponse.toString());
			if (response.has("error") && attempt < 2) {
				initializeGSTR3bSummaryRequest(gstin, gstinUsername, authenticationResponse, appKey,
						gstrAuthTokenResponse, attempt);
			} else if (response.has("error") && attempt >= 2) {
				return new JSONObject().put("ALLOW", "allow manual entry");
			} else {
				return response;
			}
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
			return new JSONObject().put("ALLOW", "allow manual entry");
		}
		return null;
	}

	public JSONObject shopsAndEstablishmentCertificate(JSONObject authenticationResponse, String registrationNumber,
			String tradingState) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.SNECS_URL).replace("PATRONID",
					authenticationResponse.getString("userId"));

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");
			connection.setRequestProperty(Constants.Authorization, authenticationResponse.getString("id"));

			JSONObject essentials = new JSONObject();
			JSONObject request = new JSONObject();

			essentials.put(Constants.STATE, tradingState);
			essentials.put(Constants.REGISTRATION_NUMBER, registrationNumber);
			request.put(Constants.ESSENTIALS, essentials);

			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}

			logger.info("Response received from Signzy for Shops and Establishment Certificate Summary Request: "
					+ serverResponse);
			rd.close();
			connection.disconnect();
			return new JSONObject(serverResponse.toString());
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}

	public Boolean verifyDrivingLicense(JSONObject identitiesResponse, JSONObject dlExtractionResponse) {
		try {
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			String serviceUrl = PropertiesManager.propertiesMap.get(Constants.IDENTITY_VERIFICATION_URL);

			URL url = new URL(serviceUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty(Constants.CONTENT_TYPE, "application/json");

			String dlNumber = "";
			String dob = "";
			String issueDate = "";

			if (dlExtractionResponse.has("response")) {
				JSONObject response = dlExtractionResponse.getJSONObject("response");
				if (response.has("result")) {
					JSONObject result = response.getJSONObject("result");
					if (result.has("extractionResponse")) {
						JSONObject extractionResponse = result.getJSONObject("extractionResponse");
						if (extractionResponse.has("issueDate")) {
							issueDate = extractionResponse.getString("issueDate");
						} else {
							return false;
						}
						if (extractionResponse.has("dob")) {
							dob = extractionResponse.getString("dob");
						} else {
							return false;
						}
						if (extractionResponse.has("number")) {
							dlNumber = extractionResponse.getString("number");
						} else {
							return false;
						}
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
			JSONObject request = new JSONObject();
			request.put(Constants.SERVICE, Constants.IDENTITY);
			request.put(Constants.ITEM_ID, identitiesResponse.getString("id"));
			request.put(Constants.TASK, Constants.VERIFICATION);
			request.put(Constants.ACCESS_TOKEN, identitiesResponse.getString("accessToken"));
			JSONObject essentials = new JSONObject();
			essentials.put(Constants.NUMBER, dlNumber);
			essentials.put(Constants.DOB, dob);
			essentials.put(Constants.ISSUE_DATE, issueDate);
			request.put(Constants.ESSENTIALS, essentials);
			connection.setRequestProperty("Content-Length", request.toString());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outputStream);
			wr.writeBytes(request.toString());
			wr.close();
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
			}
			logger.info("Response received from Signzy for DL verification: " + serverResponse.toString());
			rd.close();
			connection.disconnect();
			JSONObject dlResponse = new JSONObject(serverResponse.toString());
			if (!(dlResponse == null || dlResponse.length() < 0 || dlResponse.has("error"))) {
				if (dlResponse.has("response")) {
					JSONObject response = dlResponse.getJSONObject("response");
					if (response.has("result")) {
						JSONObject result = response.getJSONObject("result");
						if (result.has("verified")) {
							return (Boolean) result.get("verified");
						} else {
							return false;
						}
					} else {
						return false;
					}
				} else {
					return false;
				}

			} else {
				return false;
			}
		} catch (Exception exception) {
			logger.error("Error communicating with Signzy API, " , exception);
		}
		return null;
	}
}
