package com.paymentgateway.airPay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class AirPayStatusEnquiryProcessor {

	@Autowired
	@Qualifier("airPayTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("airPayTransactionCommunicator")
	private TransactionCommunicator transactionCommunicator;

	private AirPayTransformer airPayTransformer = null;

	private static Logger logger = LoggerFactory.getLogger(AirPayStatusEnquiryProcessor.class.getName());

	public void enquiryProcessor(Fields fields) {
		String request = statusEnquiryRequest(fields);
		String response = "";
		try {
			response = getResponse(request);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}

		updateFields(fields, response);

	}

	public String statusEnquiryRequest(Fields fields) {
		String jsonRequest = "";
		try {
			Transaction transaction = new Transaction();
			jsonRequest = converter.statusEnquiryRequest(fields, transaction);
			return jsonRequest.toString();
		} catch (Exception e) {
			logger.error("Exception e", e);
			return jsonRequest.toString();
		}
	}

	public String getResponse(String request) throws SystemException {

		try {
			String hostUrl = PropertiesManager.propertiesMap.get(Constants.AIRPAY_STATUS_ENQUIRY_URL);
			String response = transactionCommunicator.statusEnqPostRequest(request, hostUrl);

			return response;

		} catch (Exception e) {

			return null;
		}
	}

	public void updateFields(Fields fields, String response) {

		Transaction transactionResponse = new Transaction();
		transactionResponse = converter.toStatusTransaction(response);
		airPayTransformer = new AirPayTransformer(transactionResponse);
		airPayTransformer.updateStatusResponse(fields);

	}

}
