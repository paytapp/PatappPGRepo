package com.paymentgateway.fss;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service("fssTransactionCommunicator")
public class TransactionCommunicator {
	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());
	
	private static final String enrollmentUrl = ConfigurationConstants.FSS_ENROLLMENT_URL
			.getValue();
	private static final String paresAuthorizationUrl = ConfigurationConstants.FSS_PARES_AUTHENTICATION_URL
			.getValue();
	private static final String authorizationUrl = ConfigurationConstants.FSS_AUTHORIZATION_URL
			.getValue();
	private static final String supportTransactionUrl = ConfigurationConstants.FSS_SUPPORT_TRANSACTION_URL.getValue();

	@SuppressWarnings("incomplete-switch")
	public String getResponse(String request, Fields fields)
			throws SystemException {

		String hostUrl = "";

		try {

			TransactionType transactionType = TransactionType
					.getInstance(fields.get(FieldType.TXNTYPE.getName()));
			switch (transactionType) {
			case SALE:
			case AUTHORISE:
				hostUrl = paresAuthorizationUrl;
				String pares = fields.get(FieldType.PARES.getName());
				if (null == pares || pares.isEmpty()) {
					hostUrl = authorizationUrl;
				}
				break;
			case ENROLL:
				hostUrl = enrollmentUrl;
				break;
			case CAPTURE:
				hostUrl = supportTransactionUrl;
				break;
			case REFUND:
				hostUrl = supportTransactionUrl;
				break;
			case STATUS:
				hostUrl = supportTransactionUrl;
			}
			
			URL url = new URL(hostUrl);
			logRequest(request, hostUrl,fields);
			URLConnection connection = null;
			if (ConfigurationConstants.IS_DEBUG.getValue().equals("1")) {
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestProperty("Content-Type", "application/xml");
			} else {
				connection = (HttpsURLConnection) url.openConnection();
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			}
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			
			DataOutputStream dataoutputstream = new DataOutputStream(
					connection.getOutputStream());
			dataoutputstream.writeBytes(request);
			dataoutputstream.flush();
			dataoutputstream.close();
			BufferedReader bufferedreader = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			String decodedString;
			String response = "";
			int code = ((HttpURLConnection) connection).getResponseCode();
			int firstDigitOfCode = Integer.parseInt(Integer.toString(code).substring(0, 1));
			if(firstDigitOfCode == 4 || firstDigitOfCode == 5){
				 fields.put(FieldType.STATUS.getName(),StatusType.ACQUIRER_DOWN.getName());
				 logger.error("Response code of txn :" + code);
				 throw new SystemException(ErrorType.ACUIRER_DOWN,
							 "Network Exception with FSS "
									+ hostUrl.toString());
				}
			while ((decodedString = bufferedreader.readLine()) != null) {
				response = response + decodedString;
			}
			logResponse(response, hostUrl,fields);
			return response;
		} catch (IOException ioException) {
			MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
			fields.put(FieldType.STATUS.getName(),StatusType.ERROR.getName());
			logger.error("Network Exception with FSS", ioException);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
					ioException, "Network Exception with FSS "
							+ hostUrl.toString());
		}			
	}
	
	public void logRequest(String requestMessage, String url,Fields fields){
		log("Request message to FSS: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName())+ " " + "Url= "+url +" "+ requestMessage, fields);
	}
	
	public void logResponse(String responseMessage, String url,Fields fields){
		log("Response message from FSS: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id" + fields.get(FieldType.TXN_ID.getName()) + " " + "Url= " +url+" "+responseMessage, fields);
	}
	
	public void logRequest(String requestMessage, Fields fields){
		log("Request message to FSS: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " " + requestMessage, fields);
	}
	
	public void logResponse(String responseMessage, Fields fields){
		log("Response message from FSS: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " " +"Txn id = " + fields.get(FieldType.TXN_ID.getName()) + " "  + responseMessage, fields);
	}
	
	private void log(String message, Fields fields){
		message = Pattern.compile("(<card>)([\\s\\S]*?)(</card>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<pan>)([\\s\\S]*?)(</pan>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<expmonth>)([\\s\\S]*?)(</expmonth>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<expyear>)([\\s\\S]*?)(</expyear>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<cvv2>)([\\s\\S]*?)(</cvv2>)").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(<password>)([\\s\\S]*?)(</password>)").matcher(message).replaceAll("$1$3");
		MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
		logger.info(message);
	}
}
