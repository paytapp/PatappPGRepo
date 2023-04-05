package com.paymentgateway.ipint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;

@Service
public class IpintIntegrator {

	@Autowired
	@Qualifier("ipintTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("ipintTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	private TransactionFactory TransactionFactory;

	private static Logger logger = LoggerFactory.getLogger(IpintIntegrator.class.getName());
	public void process(Fields fields) throws SystemException {

		send(fields);

	}// process

	public void send(Fields fields) throws SystemException {

		Transaction transactionRequest = new Transaction();
		Transaction transactionResponse = new Transaction();
		Transaction transactionResponseError = new Transaction();
		transactionRequest = TransactionFactory.getInstance(fields);

		String request = converter.perpareRequest(fields, transactionRequest);
		String response = communicator.getResponse(request, fields);
		transactionResponseError = converter.saleResponseErrorCheck(response);
		logger.info("IpintIntegrator Request :-"+request);
		logger.info("IpintIntegrator Response :-"+response);
		
		
		transactionResponse = converter.toTransaction(response, fields);
		communicator.updateSaleResponse(fields, transactionResponse,transactionResponseError);
		
	}

}
