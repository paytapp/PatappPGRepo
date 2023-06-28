
package com.paymentgateway.toshanidigital;

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
public class ToshanidigitalStatusEnquiryProcessor {

	@Autowired

	@Qualifier("toshanidigitalTransactionConverter")
	private TransactionConverter converter;

	@Autowired

	@Qualifier("toshanidigitalTransactionCommunicator")
	private TransactionCommunicator transactionCommunicator;

	@Autowired
	private PropertiesManager propertiesManager;

	private static Logger logger = LoggerFactory.getLogger(ToshanidigitalStatusEnquiryProcessor.class.getName());

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
			String hostUrl = propertiesManager.propertiesMap.get(Constants.TOSHANIDIGITAL_STATUS_ENQUIRY_URL);

			JSONObject reqJson = new JSONObject();
			reqJson.put(Constants.access_token, transaction.getAccess_token());
			reqJson.put(Constants.orderid, transaction.getOrderid());

			String response = transactionCommunicator.statusEnqPostRequest(hostUrl, reqJson.toString(), transaction);

			return response;

		} catch (Exception e) {

			return null;
		}
	}

	public void updateFields(Fields fields, String response) throws SystemException {

		Transaction transactionResponse = new Transaction();
		transactionResponse = converter.toStatusTransaction(response);
		ToshanidigitalTransformer toshanidigitalTransformer = new ToshanidigitalTransformer(transactionResponse);
		toshanidigitalTransformer.updateStatusResponse(fields,transactionResponse);

	}

}
