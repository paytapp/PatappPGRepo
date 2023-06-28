package com.paymentgateway.icici.composite.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.IciciUtil;

@Service("iciciTransactionCommunicator")
public class IciciCommunicator {

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private IciciUtil iciciUtil;

	@Autowired
	private IciciResponseHandler iciciResponseHandler;

	private static Logger logger = LoggerFactory.getLogger(IciciCommunicator.class.getName());

	public String getCibResponse(String request, String url, Fields fields) throws IOException {
		String decryptedResponse = null;
		logger.info("inside getCibResponse, fields are  " + fields.getFields());
		try {

			String requestType = fields.get(FieldType.REQUEST_TYPE.getName());

			logger.info("CIB URL Calling Start on " + url);
			URL requestUrl = new URL(url);
			HttpURLConnection con = (HttpURLConnection) requestUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", " text/plain");
			con.setRequestProperty("accept", "*/*");
			con.setRequestProperty("content-length", "684");
			con.setRequestProperty("apikey", propertiesManager.propertiesMap.get("Icici_Api_Key"));
			con.setRequestProperty("host", propertiesManager.propertiesMap.get("ICICI_host"));
			con.setRequestProperty("x-forwarded-for", propertiesManager.propertiesMap.get("Payment_Gateway_IP"));

			// Encrypt The Request
			String encryptedRequest = iciciUtil.encrypt(request);
			logger.info("Encrypted Request is " + encryptedRequest + " Txn Id " + fields.get(FieldType.TXN_ID.getName())
					+ " " + fields.getFields());

			// For POST only - START
			con.setDoOutput(true);
			con.setUseCaches(false);
			OutputStream os = con.getOutputStream();
			os.write(encryptedRequest.getBytes());
			os.flush();
			os.close();
			// For POST only - END

			int responseCode = con.getResponseCode();
			logger.info("CIB Response Code " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				logger.info("Encrypted CIB response recived is " + response,
						toString() + " Txn Id = " + fields.get(FieldType.TXN_ID.getName()) + " " + fields.getFields());

				// Decrypt The Response
				if (requestType.equalsIgnoreCase(FieldType.REQ_ACCOUNT_STATEMENT.getName())) {
					decryptedResponse = iciciUtil.decryptForAccountStatement(response.toString());
				} else {
					decryptedResponse = iciciUtil.decrypt(response.toString());
				}

				logger.info("Decrypted Response of ICICI CIB is " + decryptedResponse + " Txn Id =  "
						+ fields.get(FieldType.TXN_ID.getName()));
				return decryptedResponse;

			} else {
				logger.info("Error while connecting. Response Code is " + con.getResponseCode() + " Txn Id = "
						+ fields.get(FieldType.TXN_ID.getName()));
				return decryptedResponse;

			}
		} catch (Exception e) {
			logger.error("Exception in CIB connection , Txn Id = " + fields.get(FieldType.TXN_ID.getName()), e);
			return null;
		}

	}

	public String getCibCompositeResponse(String request, String url, Fields fields, String apiKey, String priorty)
			throws IOException {
		String decryptedResponse = null;

		String requestType = fields.get(FieldType.REQUEST_TYPE.getName());

		logger.info("CIB URL Calling Start on " + url);
		HttpURLConnection con = null;
		StringBuffer response = new StringBuffer();
		logger.info("inside getCibCompositeResponse(), fields are  " + fields.getFields());
		try {

			URL requestUrl = new URL(url);
			con = (HttpURLConnection) requestUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("apikey", apiKey);

			if (StringUtils.isNotBlank(priorty)) {
				con.setRequestProperty("x-priority", priorty);
			}

			// Encrypt The Request

			String encryptedRequest = iciciUtil.CIBCompositeApiEncryption(request);
			logger.info("getCibCompositeResponse() Composite Encrypted Request is " + encryptedRequest + " Txn Id =  "
					+ fields.get(FieldType.TXN_ID.getName()));

			// For POST only - START
			con.setDoOutput(true);
			con.setUseCaches(false);
			OutputStream os = con.getOutputStream();
			os.write(encryptedRequest.getBytes());
			os.flush();
			os.close();
			// For POST only - END

			int responseCode = con.getResponseCode();
			logger.info(
					"Composite Response Code " + responseCode + " Txn Id =  " + fields.get(FieldType.TXN_ID.getName()));

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				logger.info("Encrypted Composite response recived is " + response,
						toString() + "  Txn Id =  " + fields.get(FieldType.TXN_ID.getName()));

				if (StringUtils.isNotBlank(requestType) 
						&& (requestType.equalsIgnoreCase(Constants.REQ_ADD_BENE)
								|| requestType.equalsIgnoreCase(Constants.REQ_VALIDATE_BENE)
								|| requestType.equalsIgnoreCase(Constants.REQ_BALANCE_INQUIRY))) {
					
					fields.remove(FieldType.REQUEST_TYPE.getName());

					decryptedResponse = iciciUtil.cibCompositeApiBeneDecryption(response.toString());
				} else {
					decryptedResponse = iciciUtil.CibCompositeApiDecryption(response.toString());
				}

				logger.info("Decrypted Response of ICICI Composite is " + decryptedResponse + "  Txn Id = "
						+ fields.get(FieldType.TXN_ID.getName()));
				return decryptedResponse;

			} else {
				logger.info("Error while connecting Composite Api. Response Code is " + con.getResponseCode()
						+ " txn id " + fields.get(FieldType.TXN_ID.getName()));
				return null;

			}
		} catch (Exception e) {
			logger.error("Exception in Composite connection , Txn Id = " + fields.get(FieldType.TXN_ID.getName()), e);
			return response.toString();
		} finally {
			if (con != null) {
				con.disconnect();
			}
			fields.remove(FieldType.REQUEST_TYPE.getName());
		}

	}

	public String getIMPSResponse(String request) throws IOException {
		try {
			String url = propertiesManager.propertiesMap.get("IMPS_REQUEST_URL");

			URL requestUrl = new URL(url);
			HttpURLConnection con = (HttpURLConnection) requestUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			con.setDoOutput(true);
			con.setUseCaches(false);
			OutputStream os = con.getOutputStream();
			os.write(request.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();
			logger.info("IMPS Response Code " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				logger.info("IMPS response recived is " + response.toString());

				return response.toString();

			} else {
				logger.info("Error while connecting IMPS URL. Response Code is " + con.getResponseCode());
				return String.valueOf(con.getResponseCode());

			}
		} catch (Exception e) {
			logger.error("Exception in IMPS connection ", e);
			return null;
		}

	}

	public String getIMPSStatusEnqResponse(String request) throws IOException {
		try {
			String url = propertiesManager.propertiesMap.get("IMPS_STATUS_ENQ_REQUEST_URL");

			URL requestUrl = new URL(url);
			HttpURLConnection con = (HttpURLConnection) requestUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			con.setDoOutput(true);
			con.setUseCaches(false);
			OutputStream os = con.getOutputStream();
			os.write(request.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();
			logger.info("IMPS status enq Response Code " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				logger.info("IMPS staus enq response recived is " + response.toString());

				return response.toString();

			} else {
				logger.info("Error while connecting IMPS URL. Response Code is " + con.getResponseCode());
				return String.valueOf(con.getResponseCode());

			}
		} catch (Exception e) {
			logger.error("Exception in IMPS connection ", e);
			return null;
		}

	}

	public String getIciciCompositeApiResponse(String request, String url, Fields fields, String priorty, String apiKey)
			throws IOException {
		String decryptedResponse = null;
		HttpURLConnection con = null;
		StringBuffer response = new StringBuffer();
		try {

			// logger.info("Composite Calling Start on " + url);
			// logger.info("Composite Bank API KEY " + apiKey);
			// logger.info("Composite Bank Priorty " + priorty);

			URL requestUrl = new URL(url);
			con = (HttpURLConnection) requestUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("apikey", apiKey);

			if (StringUtils.isNotBlank(priorty)) {
				con.setRequestProperty("x-priority", priorty);
			}

			// Encrypt The Request
			boolean isPaybleMerchant = iciciResponseHandler.isPaybleMerchant(fields);
			String encryptedRequest = iciciUtil.compositeApiEncryption(request, isPaybleMerchant);
			logger.info("Composite Encrypted Request is " + encryptedRequest + " Txn Id =  "
					+ fields.get(FieldType.TXN_ID.getName()));

			// For POST only - START
			con.setDoOutput(true);
			con.setUseCaches(false);
			OutputStream os = con.getOutputStream();
			os.write(encryptedRequest.getBytes());
			os.flush();
			os.close();
			// For POST only - END

			int responseCode = con.getResponseCode();
			logger.info(
					"Composite Response Code " + responseCode + " Txn Id =  " + fields.get(FieldType.TXN_ID.getName()));

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				logger.info("Encrypted Composite response recived is " + response,
						toString() + "  Txn Id =  " + fields.get(FieldType.TXN_ID.getName()));

				if (StringUtils.isNotBlank(fields.get(FieldType.REQUEST_TYPE.getName()))) {
					fields.remove(FieldType.REQUEST_TYPE.getName());

					decryptedResponse = iciciUtil.compositeApiBeneDecryption(response.toString(), isPaybleMerchant);
				} else {
					decryptedResponse = iciciUtil.compositeApiDecryption(response.toString(), isPaybleMerchant);
				}

				logger.info("Decrypted Response of ICICI Composite is " + decryptedResponse + "  Txn Id = "
						+ fields.get(FieldType.TXN_ID.getName()));
				return decryptedResponse;

			} else {
				logger.info("Error while connecting Composite Api. Response Code is " + con.getResponseCode()
						+ " txn id " + fields.get(FieldType.TXN_ID.getName()));
				return null;

			}
		} catch (Exception e) {
			logger.error("Exception in Composite connection , Txn Id = " + fields.get(FieldType.TXN_ID.getName()), e);
			return response.toString();
		} finally {
			if (con != null) {
				con.disconnect();
			}
			fields.remove(FieldType.REQUEST_TYPE.getName());
		}

	}

	public String getIciciCompositeAccountStatementResponse(String request, String url, Fields fields, String apiKey)
			throws IOException {
		String decryptedResponse = null;
		HttpURLConnection con = null;
		StringBuffer response = new StringBuffer();
		try {

			URL requestUrl = new URL(url);
			con = (HttpURLConnection) requestUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("apikey", apiKey);

			// Encrypt The Request
			String userType = fields.get(FieldType.USER_TYPE.getName());
			String fileType = fields.get(FieldType.FILE_TYPE.getName());
			boolean isPaybleMerchant = false;

			if (StringUtils.isNotBlank(userType) && userType.equalsIgnoreCase("Payble")) {
				isPaybleMerchant = true;
			}

			String encryptedRequest = iciciUtil.compositeApiEncryption(request, isPaybleMerchant);
			logger.info("Composite Encrypted Request is " + encryptedRequest + " " + request);

			// For POST only - START
			con.setDoOutput(true);
			con.setUseCaches(false);
			OutputStream os = con.getOutputStream();
			os.write(encryptedRequest.getBytes());
			os.flush();
			os.close();
			// For POST only - END

			int responseCode = con.getResponseCode();
			logger.info("Composite Response Code " + responseCode + " " + request);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				logger.info("Encrypted Composite response recived is " + response, toString() + " " + request);
				if (StringUtils.isNotBlank(fileType) && fileType.equalsIgnoreCase("current"))
					decryptedResponse = iciciUtil.compositeApiDecryption(response.toString(), isPaybleMerchant);
				else
					decryptedResponse = iciciUtil.compositeApiBeneDecryption(response.toString(), isPaybleMerchant);

				logger.info("Decrypted Response of ICICI Composite is " + decryptedResponse + " " + request);
				return decryptedResponse;

			} else {
				logger.info("Error while connecting Composite Api. Response Code is " + con.getResponseCode() + " "
						+ request);
				return null;

			}
		} catch (Exception e) {
			logger.error("Exception in Composite connection , " + request, e);
			return response.toString();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

	}

	public String getIciciCompositeNeftRtgsApiResponse(String request, String url, Fields fields, String priorty,
			String apiKey) throws IOException {
		String decryptedResponse = null;
		HttpURLConnection con = null;
		StringBuffer response = new StringBuffer();
		try {

			// logger.info("Composite Calling Start on " + url);
			// logger.info("Composite Bank API KEY " + apiKey);
			// logger.info("Composite Bank Priorty " + priorty);

			URL requestUrl = new URL(url);
			con = (HttpURLConnection) requestUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("apikey", apiKey);

			if (StringUtils.isNotBlank(priorty)) {
				con.setRequestProperty("x-priority", priorty);
			}

			// Encrypt The Request
			boolean isPaybleMerchant = iciciResponseHandler.isPaybleMerchant(fields);
			String encryptedRequest = iciciUtil.compositeApiEncryption(request, isPaybleMerchant);
			logger.info("Composite Encrypted Request is " + encryptedRequest + " Txn Id =  "
					+ fields.get(FieldType.TXN_ID.getName()));

			// For POST only - START
			con.setDoOutput(true);
			con.setUseCaches(false);
			OutputStream os = con.getOutputStream();
			os.write(encryptedRequest.getBytes());
			os.flush();
			os.close();
			// For POST only - END

			int responseCode = con.getResponseCode();
			logger.info(
					"Composite Response Code " + responseCode + " Txn Id =  " + fields.get(FieldType.TXN_ID.getName()));

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				logger.info("Encrypted Composite response recived is " + response,
						toString() + "  Txn Id =  " + fields.get(FieldType.TXN_ID.getName()));

				if (StringUtils.isNotBlank(fields.get(FieldType.REQUEST_TYPE.getName()))) {
					fields.remove(FieldType.REQUEST_TYPE.getName());

					decryptedResponse = iciciUtil.compositeApiBeneDecryption(response.toString(), isPaybleMerchant);
				} else {
					decryptedResponse = iciciUtil.compositeApiDecryption(response.toString(), isPaybleMerchant);
				}

				logger.info("Decrypted Response of ICICI Composite is " + decryptedResponse + "  Txn Id = "
						+ fields.get(FieldType.TXN_ID.getName()));
				return decryptedResponse;

			} else {
				logger.info("Error while connecting Composite Api. Response Code is " + con.getResponseCode()
						+ " txn id " + fields.get(FieldType.TXN_ID.getName()));
				return String.valueOf(con.getResponseCode());

			}
		} catch (Exception e) {
			logger.error("Exception in Composite connection , Txn Id = " + fields.get(FieldType.TXN_ID.getName()), e);
			return response.toString();
		} finally {
			if (con != null) {
				con.disconnect();
			}
			fields.remove(FieldType.REQUEST_TYPE.getName());
		}

	}
}
