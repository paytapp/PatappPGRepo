package com.paymentgateway.floxypay;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@Service("floxypayTransactionCommunicator")
public class TransactionCommunicator {

	@Autowired
	private PropertiesManager propertiesManager;

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateSaleResponse(Fields fields, String paymentUrl) {

		fields.put(FieldType.FLOXYPAY_FINAL_REQUEST.getName(), paymentUrl);
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

	}

	public String refundPostRequest(String request, String hostUrl) throws SystemException {

		// Refund is not supported by Floxypay
		String response = "";
		return response;
	}

	public String statusEnqPostRequest(String request, Transaction transaction) throws SystemException {
		String stringResponse = "";
		try {

			StringBuilder response = new StringBuilder();
			String hostUrl = PropertiesManager.propertiesMap.get(Constants.FLOXYPAY_STATUS_ENQUIRY_URL);

			logger.info("Floxypay Status Enquiry request = {}", request);

			URL url = new URL(hostUrl.toString());
			URLConnection connection = null;
			connection = url.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("x-key", transaction.getXkey());
			connection.setRequestProperty("x-secret", transaction.getXsecret());
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

			logger.info("Response received for Floxypay status enquiry {} ", response.toString());
			return response.toString();

		} catch (FileNotFoundException fnf) {
			logger.error("Exception in getting Status Enquiry respose for Floxypay", fnf);

		} catch (Exception e) {
			logger.error("Exception in getting Status Enquiry respose for Floxypay", e);
		}
		return stringResponse;
	}

	public String getPaymentResponse(String request, Fields fields, Transaction transaction) throws SystemException {
		String stringResponse = "";

		try {

			StringBuilder response = new StringBuilder();
			String hostUrl = PropertiesManager.propertiesMap.get(Constants.FLOXYPAY_SALE_URL);

			StringBuilder saleUrl = new StringBuilder();

			saleUrl.append(hostUrl);

			logger.info("Floxypay saleUrl URL={}", saleUrl.toString());

			URL url = new URL(saleUrl.toString());
			URLConnection connection = null;
			connection = url.openConnection();
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("x-key", transaction.getXkey());
			connection.setRequestProperty("x-secret", transaction.getXsecret());
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
			logger.info("Response received for Floxypay Sale {} for Pg Ref Num {}", response.toString(),
					fields.get(FieldType.PG_REF_NUM.getName()));
			return response.toString();

		} catch (Exception e) {
			logger.error("Exception in getting payment URL response for Floxypay", e);
		}
		return stringResponse;
	}

}
