package com.paymentgateway.pgui.action;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.pg.core.util.ConstantsGPay;
import com.paymentgateway.pg.core.util.ResponseCreator;

/**
 * @author VJ
 * 
 */
//to fetch the transaction status for GPay

@Service
public class SearchTransactionGpayAction {

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(SearchTransactionGpayAction.class.getName());

	Fields fields = new Fields();

	public Map<String, String> verifyUpiGpayRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			Map<String, String> reqMap) throws IOException {
		Map<String, String> responseFields = new HashMap<String, String>();
		logger.info("Inside  SearchTransactionGpayAction Execute");
		try {

			Map<String, Object> credentials = getGoogleCredentials();
			String JWTToken = createJWTToken(credentials);
			JSONObject accessTokenResponse = makePostRequest(JWTToken);
			String accessToken = accessTokenResponse.getString(ConstantsGPay.ACCESS_TOKEN);
			logger.info("token response for Gpay transactionStatus" + accessToken);

			Object fieldsObj = (Fields) httpRequest.getSession().getAttribute("FIELDS");
			if (null != fieldsObj) {
				fields.put((Fields) fieldsObj);
			}
			logger.info("fields to see the values for GPAY for searchTransaction" + fields);

			JSONObject json = transactionStatusRequest(fields);
			logger.info("Request for transactionStatus for GPAY" + json);

			JSONObject response = getResponse(json, accessToken);
			logger.info("Reponse for transactionStatus for GPAY" + response);
			if (response.length() > 0) {
				Map<String, String> resMap = new HashMap<String, String>();

				JSONObject RespValAddObject = new JSONObject();
				RespValAddObject = response.getJSONObject(ConstantsGPay.TXNSTATUS);
				String txnStatus = "";
				for (Object key : RespValAddObject.keySet()) {

					String key1 = key.toString();
					String value = RespValAddObject.get(key.toString()).toString();
					resMap.put(key1, value);

				}
				txnStatus = resMap.get(ConstantsGPay.STATUS);
				String status = getStatusType(txnStatus);
				ErrorType errorType = getErrorType(txnStatus);

				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());

				if (status.equals(ConstantsGPay.GPAYSTATUS.toString())) {
					responseFields.put("transactionStatus", StatusType.SENT_TO_BANK.getName().toString());
					responseFields.put("responseCode", fields.get(FieldType.RESPONSE_CODE.getName()));
					return responseFields;
				} else if (status.equals(ConstantsGPay.CAPTURED.toString())) {
					responseFields.put("responseCode", fields.get(FieldType.RESPONSE_CODE.getName()));
					responseFields.put("transactionStatus", StatusType.SENT_TO_BANK.getName().toString());
					return responseFields;
				} else {
					fields.put(FieldType.STATUS.getName(), status);
					fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());

					String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());

					if (fields.get(FieldType.AMOUNT.getName()) != null) {
						String amount = fields.get(FieldType.AMOUNT.getName());
						fields.put(FieldType.AMOUNT.getName(), amount);
					}

					if (fields.get(FieldType.TOTAL_AMOUNT.getName()) != null) {
						String upTotalAmount = fields.get(FieldType.TOTAL_AMOUNT.getName());
						fields.put(FieldType.TOTAL_AMOUNT.getName(), upTotalAmount);
					}
					String pgFlag = (String) httpRequest.getSession()
							.getAttribute(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName());
					fields.put(FieldType.RETURN_URL.getName(),
							httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()).toString());
					fields.remove(FieldType.HASH.getName());

					Fields responseMap = new Fields(fields);

					logger.info(
							"Response fields from searchTransaction for GPay " + responseMap.getFieldsAsBlobString());

					responseFields.put("pgRefNum", responseMap.get(FieldType.PG_REF_NUM.getName()));
					responseFields.put("responseCode", responseMap.get(FieldType.RESPONSE_CODE.getName()));

					responseFields.put("transactionStatus", responseMap.get(FieldType.STATUS.getName()));
					responseMap.removeInternalFields();
					responseMap.removeSecureFields();
					responseMap.remove(FieldType.HASH.getName());
					responseMap.remove(FieldType.ORIG_TXN_ID.getName());
					// E-ticketing
					if (StringUtils.isNotBlank(pgFlag) && pgFlag.equals(Constants.Y.getValue())) {
						responseMap.put(FieldType.INTERNAL_PAYMENT_GATEWAY_YN.getName(), pgFlag);
						String encData = responseCreator.createCrisUpiResponse(responseMap);
						responseFields.put(Constants.ENC_DATA.getValue(), encData);
						responseFields.put(FieldType.RETURN_URL.getName(),
								httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()).toString());
					} else {
						responseMap.put(FieldType.HASH.getName(), Hasher.getHash(responseMap));
						for (String key : responseMap.keySet()) {
							responseFields.put(key, responseMap.get(key));
						}
					}
					return responseFields;
				}
			}

			else {
				logger.info("response length for Status transaction for Gpay is equals to zero");
//				return ERROR;
				String path = httpRequest.getContextPath();
				logger.info(path);
				if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
					String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host")
							+ "/pgui/jsp/error";
					httpResponse.sendRedirect(resultPath);
				}
				httpResponse.sendRedirect("error");
			}
		} catch (Exception e) {
			logger.info("Exception in SearchTransactionAction ", e.getMessage());
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");

		}
		return responseFields;
	}

	@SuppressWarnings({ "unchecked" })
	private static Map<String, Object> getGoogleCredentials() throws SystemException {
		JSONParser parser = new JSONParser();
		Object jsonObject = null;
		try {
			//jsonObject = parser.parse(new FileReader(System.getenv(ConstantsGPay.PG_PROPERTIES_PATH) + ConstantsGPay.JWT_FILE_NAME));
			Map<String, Object> credentials = (Map<String, Object>) jsonObject;
			return credentials;
		} catch (Exception exception) {
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, exception,
					"JWT file not found in system for GPay");
		}
	}

	/**
	 * Create JWT token of the credentials object using SHA256withRSA
	 * 
	 * @param credentials
	 * @return JWT Token
	 */
	private static String createJWTToken(Map<String, Object> credentials) {
		String clientId = (String) credentials.get("client_email");
		String privateKey = (String) credentials.get("private_key");

		JSONObject header = new JSONObject().put("alg", "RS256").put("type", "JWT");

		JSONObject payload = new JSONObject().put("iss", clientId)
				.put("scope", PropertiesManager.propertiesMap.get(ConstantsGPay.GOOGLEPAY_SCOPE_URL))
				.put("aud", PropertiesManager.propertiesMap.get(ConstantsGPay.GOOGLEPAY_TOKEN_URL))
				.put("exp", (System.currentTimeMillis() / 1000) + 3600).put("iat", System.currentTimeMillis() / 1000);

		String data = Base64.getEncoder().encodeToString(String.valueOf(header).getBytes()) + "."
				+ Base64.getEncoder().encodeToString(String.valueOf(payload).getBytes());
		String signature = createSigature(data, privateKey);
		String jwtToken = new StringBuilder(data).append(".").append(signature).toString();
		return jwtToken;
	}

	/**
	 * Making a POST request to https://www.googleapis.com/oauth2/v4/token Fetching
	 * access token
	 * 
	 * @param jwtAssertion JWT token
	 */
	private static JSONObject makePostRequest(String jwtAssertion) throws SystemException {
		try {
			URL obj = new URL(PropertiesManager.propertiesMap.get(ConstantsGPay.GOOGLEPAY_TOKEN_URL));
			Map<String, Object> params = new LinkedHashMap<>();
			params.put("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
			params.put("assertion", jwtAssertion);
			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0)
					postData.append('&');
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}

			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setDoOutput(true);
			con.setConnectTimeout(30000);
			con.setReadTimeout(10000);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(String.valueOf(postData));
			wr.flush();
			wr.close();

			con.getResponseCode();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuilder serverResponse = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				serverResponse.append(inputLine);
			}
			in.close();
			JSONObject response = new JSONObject(serverResponse.toString());
			logger.info(" OAuth Response received from GPay" + response.toString());
			return response;

		} catch (Exception e) {
			logger.error("Exception in googlepay Communicator for token access : ", e);
			JSONObject response = new JSONObject();
			response.put(ConstantsGPay.TOKEN_FAILURE, ErrorType.GOOGLEPAY_SERVER_DOWN.getResponseCode());
			logger.info(
					"Error occured while initiating the initiate Payment transaction so created the Response for GooglePay fOR push notification"
							+ response.toString());
			return response;
		}

	}

	private static String createSigature(String input, String privateKey) {
		String formattedPrivateKey = privateKey.replaceAll("-----END PRIVATE KEY-----", "")
				.replaceAll("-----BEGIN PRIVATE KEY-----", "").replaceAll("\n", "");

		byte[] b1 = Base64.getDecoder().decode(formattedPrivateKey);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b1);
		KeyFactory keyFactory = null;
		byte[] signature = null;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			Signature privateSignature = Signature.getInstance("SHA256withRSA");
			privateSignature.initSign(keyFactory.generatePrivate(spec));
			privateSignature.update(input.getBytes("UTF-8"));
			signature = privateSignature.sign();
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | SignatureException | InvalidKeyException
				| InvalidKeySpecException e) {
			logger.error("Exception in decrypting the GPay credentials  , exception = " + e);
		}
		return Base64.getEncoder().encodeToString(signature);
	}

	public JSONObject getResponse(JSONObject request, String accessTokenResponse) throws SystemException {
		StringBuilder serverResponse = new StringBuilder();
		String hostUrl = PropertiesManager.propertiesMap.get(ConstantsGPay.GOOGLEPAY_TRANSACTION_STATUS_URL);

		logger.info("Request sent to Gpay for transaction Status in SearchTransactionGpayAction" + request);

		HttpsURLConnection connection = null;
		if (hostUrl.contains("https")) {
			try {

				// Create connection for push notification from GPay

				URL url = new URL(hostUrl);
				connection = (HttpsURLConnection) url.openConnection();

				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Authorization", "Bearer " + accessTokenResponse);
				connection.setRequestProperty("Content-Length", request.toString());
				connection.setRequestProperty("Content-Language", "en-US");

				connection.setUseCaches(false);
				connection.setDoOutput(true);
				connection.setDoInput(true);

				// Send request
				OutputStream outputStream = connection.getOutputStream();
				DataOutputStream wr = new DataOutputStream(outputStream);
				wr.writeBytes(request.toString());
				wr.close();

				// Get Response
				InputStream is = connection.getInputStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is));
				String line;

				while ((line = rd.readLine()) != null) {
					serverResponse.append(line);

				}
				rd.close();

			} catch (Exception e) {
				logger.error("Exception in SearchTransactionGpayAction getResponse for transactionStatus : ", e);

			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}
		JSONObject response = new JSONObject(serverResponse.toString());
		return response;
	}

	public String getStatusType(String transactionStatusRes) {
		String status = null;
		if (transactionStatusRes.equals(ConstantsGPay.DECLINED)) {
			status = StatusType.DECLINED.getName();
		} else if (transactionStatusRes.equals(ConstantsGPay.EXPIRED)) {
			status = StatusType.TIMEOUT.getName();
		} else if (transactionStatusRes.equals(ConstantsGPay.FAIL)) {
			status = StatusType.REJECTED.getName();
		} else if (transactionStatusRes.equals(ConstantsGPay.IN_PROGRESS)) {
			status = StatusType.PROCESSING.getName();
		} else if (transactionStatusRes.equals(ConstantsGPay.PAYMENT_NOT_INITIATED)) {
			status = StatusType.PROCESSING.getName();
		} else if (transactionStatusRes.equals(ConstantsGPay.SUCCESS)) {
			status = StatusType.CAPTURED.getName();
		} else {
			status = StatusType.REJECTED.getName();
		}

		return status;
	}

	public ErrorType getErrorType(String transactionStatusRes) {
		ErrorType error = null;

		if (transactionStatusRes.equals(ConstantsGPay.DECLINED)) {
			error = ErrorType.DECLINED;
		} else if (transactionStatusRes.equals(ConstantsGPay.EXPIRED)) {
			error = ErrorType.TIMEOUT;
		} else if (transactionStatusRes.equals(ConstantsGPay.IN_PROGRESS)) {
			error = ErrorType.PROCESSING;
		} else if (transactionStatusRes.equals(ConstantsGPay.PAYMENT_NOT_INITIATED)) {
			error = ErrorType.PROCESSING;
		} else if (transactionStatusRes.equals(ConstantsGPay.FAIL)) {
			error = ErrorType.REJECTED;
		} else if (transactionStatusRes.equals(ConstantsGPay.SUCCESS)) {
			error = ErrorType.SUCCESS;
		} else {
			error = ErrorType.DECLINED;
		}

		return error;
	}

	public JSONObject transactionStatusRequest(Fields fields) {
		String googleMerchantId = "";

		String payId = fields.get(FieldType.PAY_ID.getName());

		User user = userDao.findPayId(payId);
		String acquirerCode = AcquirerType.YESBANKCB.getCode();
		Account account = user.getAccountUsingAcquirerCode(acquirerCode);

		try {

			logger.info("GPay Status Transaction Enquiry RESPONSE DEFAULT CURRENCY  "
					+ Constants.DEFAULT_CURRENCY_CODE.getValue());
			AccountCurrency accountCurrency = account.getAccountCurrency(Constants.DEFAULT_CURRENCY_CODE.getValue());

			// values from mapping
			if (!StringUtils.isEmpty(accountCurrency.getAdf8())) {
				googleMerchantId = accountCurrency.getAdf8();
				logger.info("googleMerchantId value if payID is present  " + googleMerchantId);
			}

		} catch (Exception e) {

			logger.error("Error in YEs bank UPI callback for Gpay while getting the key from userDao = " + e);
		}
		String pgRefNum = fields.get(FieldType.PG_REF_NUM.getName());

		JSONObject merchantInfo = new JSONObject();
		merchantInfo.put(ConstantsGPay.GOOGLEMERCHANTID, googleMerchantId);

		JSONObject transactionIdentifier = new JSONObject();
		transactionIdentifier.put(ConstantsGPay.MERCHANTTRANSACTIONID, pgRefNum);

		JSONObject json = new JSONObject();
		json.put(ConstantsGPay.MERCHANTINFO, merchantInfo);
		json.put(ConstantsGPay.TRANSACTIONIDENTIFIER, transactionIdentifier);

		return json;

	}

}
