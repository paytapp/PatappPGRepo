package com.paymentgateway.axisbank.upi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AxisBankUpiEncDecService;

/**
 * @author Rahul
 *
 */
@Service("axisBankUpiTransactionCommunicator")
public class TransactionCommunicator {
	
	@Autowired
	private AxisBankUpiEncDecService axisBankUpiChecksumGenerator;

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateSaleResponse(Fields fields, String request) {
		
		//fields.put(FieldType.AXISBANK_FINAL_REQUEST.getName(), request.toString());
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

	}

	
	// For Sale Reponse 
	@SuppressWarnings("incomplete-switch")
	public String getSaleResponse(String token, Fields fields) throws SystemException {

		String hostUrl = "";
		
			TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
			switch (transactionType) {
			case SALE:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.AXISBANK_UPI_SALE_REQUEST_URL);
				break;
			case AUTHORISE:
				break;
			case ENROLL:
				break;
			case CAPTURE:
				break;
			case REFUND:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.AXISBANK_UPI_REFUND_URL);
				break;
			case STATUS:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.AXISBANK_UPI_STATUS_ENQ_URL);
				break;
			}
			
			try {
				
				if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())) {
					String urlToRead = hostUrl + token;
					
					StringBuilder result = new StringBuilder();
					URL url = new URL(urlToRead);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String line;
					while ((line = rd.readLine()) != null) {
						result.append(line);
					}
					rd.close();
					
					logger.info("Response received for Axis Bank UPI Collect " + result.toString());
					return result.toString();
				}
				
				if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.REFUND.getName())) {
					
				}
				
				if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.ENQUIRY.getName())) {
					
				}
				
			}
			catch(Exception e) {
				logger.error("Exception in Axis Bank UPI Collect Request",e);
			}
			
		return null;		
			
	}
	
	// For Refund and status Enquiry Response
	@SuppressWarnings("incomplete-switch")
	public String getResponse(String request , Fields fields) throws SystemException {

		String hostUrl = "";
		
			TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
			switch (transactionType) {
			case SALE:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.AXISBANK_UPI_SALE_REQUEST_URL);
				break;
			case AUTHORISE:
				break;
			case ENROLL:
				break;
			case CAPTURE:
				break;
			case REFUND:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.AXISBANK_UPI_REFUND_URL);
				break;
			case STATUS:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.AXISBANK_UPI_STATUS_ENQ_URL);
				break;
			}
			
			StringBuilder serverResponse = new StringBuilder();
			HttpsURLConnection connection = null;
			
			try {
			

				URL url = new URL(hostUrl);
				connection = (HttpsURLConnection) url.openConnection();

				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Content-Length", request.toString());

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

				int code = ((HttpURLConnection) connection).getResponseCode();
				while ((line = rd.readLine()) != null) {
					serverResponse.append(line);

				}
				rd.close();
				logger.info(" Response received For Axis UPI Refund >> " + serverResponse.toString());
			} catch (Exception e) {
				logger.error("Exception in Axis UPI Refund / Status Request " ,e);
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
			return serverResponse.toString();	
			
	}
	
	
	@SuppressWarnings("incomplete-switch")
	public String getToken(JSONObject request, Fields fields) throws SystemException {


		StringBuilder serverResponse = new StringBuilder();
		
		try {
			String hostUrl = "";
			
			TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
			switch (transactionType) {
			case SALE:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.AXISBANK_UPI_TOKEN_URL);
				break;
			case AUTHORISE:
				break;
			case ENROLL:
				break;
			case CAPTURE:
				break;
			case REFUND:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.AXISBANK_UPI_REFUND_URL);
				break;
			case STATUS:
				hostUrl = PropertiesManager.propertiesMap.get(Constants.AXISBANK_UPI_STATUS_ENQ_URL);
				break;
			}
			
			String merchId = request.getString(Constants.MERCH_ID);
			String merchChanId = request.getString(Constants.MERCH_CHAN_ID);
			String unqTxnId =  request.getString(Constants.UNQ_TXN_ID);
			String unqCustId =  request.getString(Constants.UNQ_CUST_ID);
			String amount =  request.getString(Constants.AMOUNT);
			String txnDtl =  request.getString(Constants.TXN_DTL);
			String currency =  request.getString(Constants.CURRENCY);
			String orderId =  request.getString(Constants.ORDER_ID);
			String customerVpa =  request.getString(Constants.CUSTOMER_VPA);
			String expiry =  request.getString(Constants.EXPIRY);
			String sid =  request.getString(Constants.SID);
			
			String checkSumString = merchId + merchChanId + unqTxnId + unqCustId + amount + txnDtl + currency + orderId
					+ customerVpa + expiry;
			
			String checkSum = axisBankUpiChecksumGenerator.generateCheckSum(checkSumString);
			request.put(Constants.CHECKSUM, checkSum);
			
			HttpsURLConnection connection = null;

			URL url = new URL(hostUrl);
			connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Length", request.toString());

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

			int code = ((HttpURLConnection) connection).getResponseCode();
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);

			}
			rd.close();
			
			logger.info("Axis Bank UPI Token response " + serverResponse.toString());
			
			ObjectMapper mapper = new ObjectMapper();
			Map<String, String> map = mapper.readValue(serverResponse.toString(), Map.class);
			String token = map.get("data");
			return token;
			
		}
		
		catch(Exception e) {
			logger.error("Exception in Token Request for Axis Bank UPI",e);
		}
		
		return null;		
			
	}
	
	public static String getTextBetweenTags(String text, String tag1, String tag2) {

		int leftIndex = text.indexOf(tag1);
		if (leftIndex == -1) {
			return null;
		}

		int rightIndex = text.indexOf(tag2);
		if (rightIndex != -1) {
			leftIndex = leftIndex + tag1.length();
			return text.substring(leftIndex, rightIndex);
		}

		return null;
	}
	
	
	public String getVpaResponse(JSONObject request , Fields fields) {

		StringBuilder serverResponse = new StringBuilder();
		String hostUrl = PropertiesManager.propertiesMap.get(Constants.AXISBANK_UPI_VPA_VALIDATION_URL);

		HttpsURLConnection connection = null;

			try {

				URL url = new URL(hostUrl);
				connection = (HttpsURLConnection) url.openConnection();

				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/json");
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
				
				int code = ((HttpURLConnection) connection).getResponseCode();
				int firstDigitOfCode = Integer.parseInt(Integer.toString(code).substring(0, 1));
				if(firstDigitOfCode == 4 || firstDigitOfCode == 5){
					 fields.put(FieldType.STATUS.getName(),StatusType.ACQUIRER_DOWN.getName());
					 logger.error("Response code of txn :" + code);
					 throw new SystemException(ErrorType.ACUIRER_DOWN,
								 "Network Exception with hdfc Upi "
										+ hostUrl.toString());
					}
				

				while ((line = rd.readLine()) != null) {
					serverResponse.append(line);

				}
				rd.close();

			} catch (Exception e) {
				logger.error("Exception in VPA Validation for Axis Bank UPI ",e);
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		
		
		return serverResponse.toString();
	}

}
