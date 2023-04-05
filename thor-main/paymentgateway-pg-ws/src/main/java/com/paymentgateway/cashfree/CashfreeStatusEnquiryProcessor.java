package com.paymentgateway.cashfree;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class CashfreeStatusEnquiryProcessor {

	@Autowired
	@Qualifier("cashfreeTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("cashfreeTransactionCommunicator")
	private TransactionCommunicator transactionCommunicator;

	private CashfreeTransformer cashfreeTransformer = null;

	private static Logger logger = LoggerFactory.getLogger(CashfreeStatusEnquiryProcessor.class.getName());

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

		String request = null;
		try {
			request = converter.statusEnquiryRequest(fields, null);
			return request;
		} catch (Exception e) {
			logger.error("Exception e", e);
			return request;
		}

	}

	public String getResponse(String request) throws SystemException {

		try {
			String hostUrl = PropertiesManager.propertiesMap.get(Constants.STATUS_ENQ_REQUEST_URL);
			String response = transactionCommunicator.statusEnqPostRequest(request, hostUrl);
			
			return response;

		} catch (Exception e) {

			return null;
		}
	}

	public void updateFields(Fields fields, String jsonResponse) {

		Transaction transactionResponse = new Transaction();
		transactionResponse = converter.toTransactionStatus(jsonResponse);
		cashfreeTransformer = new CashfreeTransformer(transactionResponse);
		cashfreeTransformer.updateStatusResponse(fields);

	}// toTransaction()

}
