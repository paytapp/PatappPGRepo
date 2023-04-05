package com.paymentgateway.safexpay;
 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class SafexpayIntegrator {

	@Autowired
	@Qualifier("safexpayTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("safexpayTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	private TransactionFactory transactionFactory;

	public void process(Fields fields) throws SystemException {

		send(fields);

	}// process

	public void send(Fields fields) throws SystemException {

		Transaction transactionRequest = new Transaction();
		Transaction transactionResponse = new Transaction();

		transactionRequest = transactionFactory.getInstance(fields);

		String request = converter.perpareRequest(fields, transactionRequest);

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.SALE.getName())) {
			communicator.updateSaleResponse(fields, request);
		} else {
			String response = communicator.getResponse(request, fields);

			 transactionResponse = converter.toTransaction(response,fields);
			 SafexpayTransformer  safexpayTransformer = new SafexpayTransformer(transactionResponse);
			 safexpayTransformer.updateRefundResponse(fields);
		}

	}

}
