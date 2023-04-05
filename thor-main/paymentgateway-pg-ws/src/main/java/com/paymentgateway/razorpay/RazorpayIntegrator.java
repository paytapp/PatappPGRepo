package com.paymentgateway.razorpay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service
final class RazorpayIntegrator {

	@Autowired
	@Qualifier("razorpayTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("razorpayTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	private TransactionFactory transactionFactory;

	@Autowired
	private RazorpayTransformer razorpayTransformer;

	public void process(Fields fields) throws SystemException {

		send(fields);

	}

	public void send(Fields fields) throws SystemException {

		Transaction transactionRequest = transactionFactory.getInstance(fields);

		String request = converter.perpareRequest(fields, transactionRequest);

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.SALE.getName())) {
			communicator.updateSaleResponse(fields, request);
		} else {
			String response = communicator.getRefundResponse(request, fields);

			Transaction transactionResponse = converter.toTransaction(response, fields);
			razorpayTransformer.updateRefundResponse(fields, transactionResponse);
		}

	}

}
