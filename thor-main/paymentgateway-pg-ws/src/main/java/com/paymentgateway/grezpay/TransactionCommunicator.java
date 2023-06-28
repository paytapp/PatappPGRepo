package com.paymentgateway.grezpay;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

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
 * @author Shaiwal
 *
 */
@Service("grezpayTransactionCommunicator")
public class TransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateSaleResponse(Fields fields, String paymentUrl) {

		fields.put(FieldType.GREZPAY_FINAL_REQUEST.getName(), paymentUrl);
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

	}

	public String refundPostRequest(String request, String hostUrl) throws SystemException {

		// Refund is not supported by Grezpay
		String response = "";
		return response;
	}

	public String statusEnqPostRequest(String hostUrl,String request,Transaction transaction) throws SystemException {
		
		String stringResponse = "";
		try {
			
			StringBuilder response = new StringBuilder();

			logger.info("Grezpay Status Enquiry request = {}", request);

			URL url = new URL(hostUrl.toString());
			URLConnection connection = null;
			connection = url.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("mid", transaction.getMid());
			connection.setRequestProperty("password", transaction.getPassword());
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			try (DataOutputStream dataoutputstream = new DataOutputStream(connection.getOutputStream())) {

				dataoutputstream.writeBytes(request);
				dataoutputstream.flush();

				try (BufferedReader bufferedreader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()))) {
					String decodedString;

					while ((decodedString = bufferedreader.readLine()) != null) {
						response.append(decodedString);
					}
				}

			}

			logger.info("Response received for Grezpay Status Enquiry response {} ", response.toString());
			return response.toString();

		} catch (Exception e) {
			logger.error("Exception in getting Status Enquiry Response for Grezpay", e);
		}
		return stringResponse;
	}

	public String getPaymentResponse(String request, Fields fields, Transaction transaction) throws SystemException {
		
		String stringResponse = "";
		try {

			StringBuilder response = new StringBuilder();
			String hostUrl = PropertiesManager.propertiesMap.get(Constants.GREZPAY_SALE_URL);

			logger.info("Grezpay Sale request = {}", request);

			URL url = new URL(hostUrl.toString());
			URLConnection connection = null;
			connection = url.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("mid", transaction.getMid());
			connection.setRequestProperty("password", transaction.getPassword());
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			try (DataOutputStream dataoutputstream = new DataOutputStream(connection.getOutputStream())) {

				dataoutputstream.writeBytes(request);
				dataoutputstream.flush();

				try (BufferedReader bufferedreader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()))) {
					String decodedString;

					while ((decodedString = bufferedreader.readLine()) != null) {
						response.append(decodedString);
					}
				}

			}

			logger.info("Response received for Grezpay sale URL request {} ", response.toString());
			return response.toString();

		} catch (Exception e) {
			logger.error("Exception in getting Sale URL Response for Grezpay", e);
		}
		return stringResponse;
	}

}
