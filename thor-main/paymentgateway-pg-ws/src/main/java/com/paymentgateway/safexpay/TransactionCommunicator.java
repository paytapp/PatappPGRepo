package com.paymentgateway.safexpay;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
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

@Service("safexpayTransactionCommunicator")
public class TransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateSaleResponse(Fields fields, String request) {

		fields.put(FieldType.SAFEXPAY_FINAL_REQUEST.getName(), request.toString());
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

	}

	@SuppressWarnings("incomplete-switch")
	public String getResponse(String request, Fields fields) throws SystemException {

		String hostUrl = "";

		try {

				hostUrl = PropertiesManager.propertiesMap.get(Constants.SAFEXPAY_REFUND_URL);
				logger.info("Safex Pay Refund URL >> " +hostUrl);
				
				URL url = new URL(hostUrl);
				URLConnection connection = null;
				connection = url.openConnection();
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setUseCaches(false);

				DataOutputStream dataoutputstream = new DataOutputStream(connection.getOutputStream());
				dataoutputstream.writeBytes(request);
				dataoutputstream.flush();
				dataoutputstream.close();
				BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String decodedString;
				String response = "";
				while ((decodedString = bufferedreader.readLine()) != null) {
					response = response + decodedString;
				}

				logger.info("Response received for Safex Pay Refund  >> " + response);
				 
				return response;
			} catch (IOException ioException) {
				fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, ioException,
						"Network Exception with Safexpay for refund request " + hostUrl.toString());
				
			}

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

}
