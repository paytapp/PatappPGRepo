package com.paymentgateway.qaicash;

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
public class QaicashStatusEnquiryProcessor {

	@Autowired
	@Qualifier("qaicashTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("qaicashTransactionCommunicator")
	private TransactionCommunicator transactionCommunicator;

	private QaicashTransformer qaicashTransformer = null;

	@Autowired
	private QaicashUtil qaicashUtil;
	
	private static Logger logger = LoggerFactory.getLogger(QaicashStatusEnquiryProcessor.class.getName());

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
			
			StringBuilder hmacString = new StringBuilder();
			hmacString.append(transaction.getMerchantId());
			hmacString.append("|");
			hmacString.append(transaction.getOrderId());
			
			String hmac = qaicashUtil.HMAC_SHA256(transaction.getMerchantApiKey(), hmacString.toString());
			
			StringBuilder hostUrl = new StringBuilder();
			hostUrl.append(PropertiesManager.propertiesMap.get(Constants.QAICASH_STATUS_ENQUIRY_URL));
			hostUrl.append(transaction.getMerchantId());
			hostUrl.append("/deposit/");
			hostUrl.append(transaction.getOrderId());
			hostUrl.append("/mac/");
			hostUrl.append(hmac);
			
			String response = transactionCommunicator.statusEnqPostRequest(hostUrl.toString());

			return response;

		} catch (Exception e) {

			return null;
		}
	}

	public void updateFields(Fields fields, String response) {

		Transaction transactionResponse = new Transaction();
		transactionResponse = converter.toStatusTransaction(response);
		qaicashTransformer = new QaicashTransformer(transactionResponse);
		qaicashTransformer.updateStatusResponse(fields);

	}

}
