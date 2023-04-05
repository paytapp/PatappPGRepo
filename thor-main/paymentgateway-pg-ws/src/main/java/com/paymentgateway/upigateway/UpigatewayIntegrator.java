package com.paymentgateway.upigateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class UpigatewayIntegrator {

	@Autowired
	@Qualifier("upigatewayTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("upigatewayTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	@Qualifier("upigatewayFactory")
	private TransactionFactory transactionFactory;

	public void process(Fields fields) throws SystemException {

		send(fields);

	}

	public void send(Fields fields) throws SystemException {

		Transaction transactionRequest = new Transaction();

		transactionRequest = transactionFactory.getInstance(fields);

		String request = converter.perpareRequest(fields, transactionRequest);

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.SALE.getName())) {
			
			String paymentResponse = communicator.getPaymentResponse(request,fields,transactionRequest);
			String paymentUrl = converter.getPaymentUrl(paymentResponse,fields);
			communicator.updateSaleResponse(fields, paymentUrl);
			
		} 
	}

}
