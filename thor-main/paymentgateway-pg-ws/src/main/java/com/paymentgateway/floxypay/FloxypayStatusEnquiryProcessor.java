package com.paymentgateway.floxypay;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class FloxypayStatusEnquiryProcessor {

	@Autowired
	@Qualifier("floxypayTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("floxypayTransactionCommunicator")
	private TransactionCommunicator transactionCommunicator;

	private FloxypayTransformer floxypayTransformer = null;

	private static Logger logger = LoggerFactory.getLogger(FloxypayStatusEnquiryProcessor.class.getName());

	public void enquiryProcessor(Fields fields) {
		Transaction transaction = statusEnquiryRequest(fields);
		String response = "";
		try {
			response = getResponse(transaction);
			updateFields(fields, response);
		} catch (SystemException exception) {
			logger.error("Exception", exception);
		}

	}

	public Transaction statusEnquiryRequest(Fields fields) {
		try {
			
			Transaction transaction = new Transaction();
			transaction.setStatusFields(fields);
			return transaction;
			
		} catch (Exception e) {
			logger.error("Exception e", e);
			return null;
		}
	}

	public String getResponse(Transaction transaction) throws SystemException {

		try {
			
			JSONObject reqJson = new JSONObject();
			reqJson.put("order_id", transaction.getOrderid());
			
			String response = transactionCommunicator.statusEnqPostRequest(reqJson.toString(),transaction);
			return response;

		} catch (Exception e) {

			return null;
		}
	}

	public void updateFields(Fields fields, String response) {

		Transaction transactionResponse = new Transaction();
		transactionResponse = converter.toStatusTransaction(response);
		floxypayTransformer = new FloxypayTransformer(transactionResponse);
		floxypayTransformer.updateStatusResponse(fields);

	}

}
