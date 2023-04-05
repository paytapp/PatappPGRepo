package com.paymentgateway.digitalsolution;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.QaicashUtil;

@Service
public class DigitalsolutionStatusEnquiryProcessor {

	@Autowired
	@Qualifier("digitalsolutionTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("digitalsolutionTransactionCommunicator")
	private TransactionCommunicator transactionCommunicator;

	private DigitalsolutionTransformer digitalsolutionTransformer = null;

	private static Logger logger = LoggerFactory.getLogger(DigitalsolutionStatusEnquiryProcessor.class.getName());

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

			String hostUrl = PropertiesManager.propertiesMap.get(Constants.DIGITALSOLUTION_STATUS_ENQUIRY_URL);
			JSONObject reqJson = new JSONObject();

			reqJson.put(Constants.token, transaction.getToken());
			reqJson.put(Constants.clint_ref_id, transaction.getClint_ref_id());

			String response = transactionCommunicator.statusEnqPostRequest(hostUrl.toString(), reqJson.toString());

			return response;

		} catch (Exception e) {

			return null;
		}
	}

	public void updateFields(Fields fields, String response) {

		Transaction transactionResponse = new Transaction();
		transactionResponse = converter.toStatusTransaction(response);
		digitalsolutionTransformer = new DigitalsolutionTransformer(transactionResponse);
		digitalsolutionTransformer.updateStatusResponse(fields);

	}

}
