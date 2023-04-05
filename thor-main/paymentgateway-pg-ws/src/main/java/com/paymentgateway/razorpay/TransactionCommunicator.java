package com.paymentgateway.razorpay;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

@Service("razorpayTransactionCommunicator")
final class TransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateSaleResponse(Fields fields, String request) {

		fields.put(FieldType.RAZORPAY_FINAL_REQUEST.getName(), request);
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

	}

	public String getRefundResponse(String request, Fields fields) throws SystemException {

		try {

			StringBuilder response = new StringBuilder();
			String hostUrl = PropertiesManager.propertiesMap.get(Constants.RAZORPAY_REFUND_URL);
			

			StringBuilder refundUrl = new StringBuilder();
			
			refundUrl.append(hostUrl);
			refundUrl.append(fields.get(FieldType.ACQ_ID.getName()));
			refundUrl.append("/refund");
			
			logger.info("Razorpay Refund URL={}", refundUrl.toString());
			
			String userpass = fields.get(FieldType.MERCHANT_ID.getName()) + ":" + fields.get(FieldType.TXN_KEY.getName());
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
			
			URL url = new URL(refundUrl.toString());
			URLConnection connection = null;
			connection = url.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", basicAuth);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			try (DataOutputStream dataoutputstream = new DataOutputStream(connection.getOutputStream())) {

				dataoutputstream.writeBytes(request);
				dataoutputstream.flush();
				
				try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
					String decodedString;

					while ((decodedString = bufferedreader.readLine()) != null) {
						response.append(decodedString);
					}
				}
				
			}

			logger.info("Response received for Razorpay Refund {} ", response.toString());
			return response.toString();

		} catch (IOException ioException) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, ioException,
					"Network Exception with Razorpay for refund request ");

		}

	}
	
	public String getSaleOrderId(String request, Fields fields) throws SystemException {

		try {

			StringBuilder response = new StringBuilder();
			String hostUrl = PropertiesManager.propertiesMap.get(Constants.RAZORPAY_ORDER_GEN_URL);
			logger.info("Razorpay Get Order URL={}", hostUrl);

			String userpass = fields.get(FieldType.MERCHANT_ID.getName()) + ":" + fields.get(FieldType.TXN_KEY.getName());
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
			
			URL url = new URL(hostUrl);
			URLConnection connection = null;
			connection = url.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", basicAuth);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			try (DataOutputStream dataoutputstream = new DataOutputStream(connection.getOutputStream())) {

				dataoutputstream.writeBytes(request);
				dataoutputstream.flush();
				
				try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
					String decodedString;

					while ((decodedString = bufferedreader.readLine()) != null) {
						response.append(decodedString);
					}
				}
				
			}

			logger.info("Response received for Razorpay Sale Generator Request {} ", response.toString());
			return response.toString();

		} catch (IOException ioException) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, ioException,
					"Network Exception with Razorpay for refund request ");

		}

	}
	
	
	public String getSaleHtml(String request, Fields fields) throws SystemException {

		try {

			StringBuilder response = new StringBuilder();
			String hostUrl = PropertiesManager.propertiesMap.get(Constants.RAZORPAY_SALE_REQUEST_URL);
			logger.info("Razorpay Sale request URL={}", hostUrl);

			String userpass = fields.get(FieldType.MERCHANT_ID.getName()) + ":" + fields.get(FieldType.TXN_KEY.getName());
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
			
			URL url = new URL(hostUrl);
			URLConnection connection = null;
			connection = url.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", basicAuth);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			try (DataOutputStream dataoutputstream = new DataOutputStream(connection.getOutputStream())) {

				dataoutputstream.writeBytes(request);
				dataoutputstream.flush();
				
				try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
					String decodedString;

					while ((decodedString = bufferedreader.readLine()) != null) {
						response.append(decodedString);
						response.append("\n"); 
					}
				}
				
			}

			logger.info("Response received for Razorpay Sale HTML {} ", response.toString());
			return response.toString();

		} catch (IOException ioException) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, ioException,
					"Network Exception with Razorpay for sale request ");

		}

	}

}
