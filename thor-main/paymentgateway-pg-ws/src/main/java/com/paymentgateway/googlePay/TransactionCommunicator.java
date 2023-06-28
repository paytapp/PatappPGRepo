package com.paymentgateway.googlePay;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import java.util.LinkedHashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
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
 * @author vj
 *
 */
@Service("googlePayTransactionCommunicator")
public class TransactionCommunicator {
	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateInvalidVpaResponse(Fields fields, JSONObject response) {

		fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_VPA.getResponseCode());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_VPA.getResponseMessage());
	}

	public void updateSaleResponse(Fields fields, JSONObject response) {
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
	}
	
	public void updateGpayTokenFailureResponse(Fields fields, JSONObject accessTokenResponse) {
		fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.GOOGLEPAY_SERVER_DOWN.getResponseCode());
	}

	public JSONObject createAccessToken() throws SystemException {
		Map<String, Object> credentials = getGoogleCredentials();
		String JWTToken = createJWTToken(credentials);
		JSONObject accessTokenResponse = makePostRequest(JWTToken);

		return accessTokenResponse;
	}

	/**
	 * Read Service account file and parse it to JSONObject
	 * 
	 * @return Map<String, Object> of service account json
	 * @throws SystemException
	 */
	@SuppressWarnings({ "unchecked", "resource" })
	private static Map<String, Object> getGoogleCredentials() throws SystemException {
		JSONParser parser = new JSONParser();
		Object jsonObject = null;
		try {
			jsonObject = parser
					.parse(new FileReader(System.getenv(Constants.PG_PROPERTIES_PATH) + Constants.JWT_FILE_NAME));
			Map<String, Object> credentials = (Map<String, Object>) jsonObject;
			return credentials;
		} catch (Exception exception) {
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, exception, "JWT file not found in system for GPay");
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
				.put("scope", PropertiesManager.propertiesMap.get(Constants.GOOGLEPAY_SCOPE_URL))
				.put("aud", PropertiesManager.propertiesMap.get(Constants.GOOGLEPAY_TOKEN_URL))
				.put("exp", (System.currentTimeMillis() / 1000) + 3600).put("iat", System.currentTimeMillis() / 1000);

		String data = Base64.getEncoder().encodeToString(String.valueOf(header).getBytes()) + "."
				+ Base64.getEncoder().encodeToString(String.valueOf(payload).getBytes());
		String signature = createSigature(data, privateKey);
		String jwtToken = new StringBuilder(data).append(".").append(signature).toString();
		return jwtToken;
	}

	/**
	 * Making a POST request to access token
	 * 
	 * @param jwtAssertion
	 *            JWT token
	 */
	private static JSONObject makePostRequest(String jwtAssertion) throws SystemException {
		try {
			URL obj = new URL(PropertiesManager.propertiesMap.get(Constants.GOOGLEPAY_TOKEN_URL));
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
			response.put(Constants.TOKEN_FAILURE,ErrorType.GOOGLEPAY_SERVER_DOWN.getResponseCode());
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
			logger.error("Exception in decrypting the GPay credentials  , exception = " , e);
		}
		return Base64.getEncoder().encodeToString(signature);
	}

	@SuppressWarnings("incomplete-switch")
	public JSONObject getResponse(JSONObject request, Fields fields, String accessTokenResponse)
			throws SystemException {
		StringBuilder serverResponse = new StringBuilder();
		String hostUrl = PropertiesManager.propertiesMap.get(Constants.GOOGLEPAY_PUSH_NOTIFICATION_URL);

		logger.info("Request sent to bank for push notification" + request);

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

			} catch (FileNotFoundException file) {
				logger.error("Exception in googlepay Communicator for push notification : ", file);
				if (StringUtils.isBlank(serverResponse.toString())) {
					JSONObject response = new JSONObject();
					response.put(Constants.VPAINVALID, Constants.VPAMSG);
					logger.info(
							"Error occured while initiating the initiate Payment transaction so created the Response for GooglePay fOR push notification"
									+ response.toString());
					return response;
				} else {
					JSONObject response = new JSONObject(serverResponse.toString());
					return response;
				}

			}

			catch (Exception e) {
				logger.error("Exception in googlepay Communicator for push notification : ", e);
				JSONObject response = new JSONObject();
				response.put(Constants.VPAINVALID, Constants.VPAMSG);
				logger.info(
						"Error occured while initiating the initiate Payment transaction so created the Response for GooglePay fOR push notification"
								+ response.toString());
				return response;
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}
		JSONObject response = new JSONObject(serverResponse.toString());
		logger.info(" Response received from GooglePAy fOR push notification" + response.toString());
		return response;
	}
}
