package com.paymentgateway.federal;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Rahul
 *
 */
@Service("federalTransactionCommunicator")
public class TransactionCommunicator {
	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateEnrollResponse(Fields fields, String request) {
		fields.put(FieldType.FEDERAL_ENROLL_FINAL_REQUEST.getName(), request);
		fields.put(FieldType.STATUS.getName(), StatusType.ENROLLED.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		fields.logAllFields("Authentication request to federal");
	}

	@SuppressWarnings("incomplete-switch")
	public String getResponse(String request, Fields fields) throws SystemException {

		String response = "";
		String hostUrl = "";

		TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
		switch (transactionType) {
		case SALE:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.FEDERAL_SALE_URL);
			break;
		case CAPTURE:
			hostUrl = "";
			break;
		case REFUND:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.FEDERAL_REFUND_URL);
			break;
		case STATUS:
			hostUrl = PropertiesManager.propertiesMap.get(Constants.FEDERAL_STATUS_ENQ_URL);
		}
		try {
			response = executePost(request, hostUrl, fields);
		} catch (Exception exception) {
			logger.error("Exception : " , exception);
		}

		return response;
	}

	public String executePost(String urlParameters, String targetURL, Fields fields) throws Exception {
		logRequest(urlParameters, targetURL,fields);
		byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
		int postDataLength = postData.length;
		String request = targetURL;
		URL url = new URL(request);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("charset", "UTF-8");
		conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
		conn.setUseCaches(false);

		try {

			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.write(postData);
			wr.flush();
			wr.close();

			// Get Response
			InputStream is = conn.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			 int code = ((HttpURLConnection) conn).getResponseCode();
			 int firstDigitOfCode = Integer.parseInt(Integer.toString(code).substring(0, 1));
			 if(firstDigitOfCode == 4 || firstDigitOfCode == 5){
				 fields.put(FieldType.STATUS.getName(),StatusType.ACQUIRER_DOWN.getName());
				 logger.error("Response code of txn :" + code);
				 throw new SystemException(ErrorType.ACUIRER_DOWN,
						 "Network Exception with Federal ");
				}
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();

			String resStr = response.toString();
			logResponse(resStr, targetURL,fields);

			if (resStr != null)
				return resStr.trim();
			else
				return null;

		}

		catch (Exception exception) {
			logger.error("exception",exception);
			throw new SystemException(ErrorType.ACQUIRER_ERROR, "Unable to get respose from federal");
		} finally {

			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	
	public void logRequest(String requestMessage, String url,Fields fields){
		log("Request message to Federal: Url= "+url +" "+ requestMessage, fields);
	}
	
	public void logResponse(String responseMessage, String url,Fields fields){
		log("Response message from Federal: Url= " +url+" "+responseMessage, fields);
	}
	
	public void logRequest(String requestMessage, Fields fields){
		log("Request message to Federal:" + requestMessage, fields);
	}
	
	public void logResponse(String responseMessage, Fields fields){
		log("Response message from Federal:" + responseMessage, fields);
	}
	
	private void log(String message, Fields fields){
		message = Pattern.compile("(pan)([\\s\\S]*?)(\\&)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(exp_date_yyyy)([\\s\\S]*?)(\\&)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(exp_date_mm)([\\s\\S]*?)(\\&)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(cvv2)([\\s\\S]*?)(\\&)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(pg_instance_id)([\\s\\S]*?)(\\&)").matcher(message).replaceAll("$1$3");
		MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
		logger.info(message);
	}

}
