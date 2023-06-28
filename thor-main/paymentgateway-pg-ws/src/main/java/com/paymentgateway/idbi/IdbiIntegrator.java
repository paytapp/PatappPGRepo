package com.paymentgateway.idbi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class IdbiIntegrator {

	private static Logger logger = LoggerFactory.getLogger(IdbiIntegrator.class.getName());
	
	@Autowired
	@Qualifier("idbiTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("idbiTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	private TransactionFactory TransactionFactory;

	private IdbiTransformer idbiTransformer = null;

	public void process(Fields fields) throws SystemException {

		send(fields);

	} // process

	public void send(Fields fields) throws SystemException {

		Transaction transactionRequest = new Transaction();
		Transaction transactionResponse = new Transaction();

		transactionRequest = TransactionFactory.getInstance(fields);
		String request = converter.perpareRequest(fields, transactionRequest);

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.SALE.getName())) {
			communicator.updateSaleResponse(fields, request);
		} else if (txnType.equals(TransactionType.REFUND.getName())) {
			String response = communicator.getResponse(request, fields);
			transactionResponse = converter.toTransaction(fields,response);
			idbiTransformer = new IdbiTransformer(transactionResponse);
			idbiTransformer.updateResponse(fields);
		} else {
			String response = communicator.getResponse(request, fields);
			transactionResponse = converter.toStatusTransaction(fields,response);
			idbiTransformer = new IdbiTransformer(transactionResponse);
			idbiTransformer.updateResponse(fields);
		}
	}

}
