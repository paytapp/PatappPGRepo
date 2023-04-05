package com.paymentgateway.qaicash;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class QaicashIntegrator {

	@Autowired
	@Qualifier("qaicashTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("qaicashTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	@Qualifier("qaicashFactory")
	private TransactionFactory transactionFactory;

	public void process(Fields fields) throws SystemException {

		send(fields);

	}

	public void send(Fields fields) throws SystemException {

		int amount = Integer.valueOf(fields.get(FieldType.AMOUNT.getName()));
		if (amount < 20000) {
			throw new SystemException(ErrorType.MIN_AMOUNT_ERROR, "Minimum Transaction amount is INR 200");
		}
		
		
		Transaction transactionRequest = new Transaction();

		transactionRequest = transactionFactory.getInstance(fields);

		String request = converter.perpareRequest(fields, transactionRequest);

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.SALE.getName())) {
			
			String paymentResponse = communicator.getPaymentResponse(request,fields);
			String paymentUrl = converter.getPaymentUrl(paymentResponse,fields);
			communicator.updateSaleResponse(fields, paymentUrl);
			
		} 
	}

}
