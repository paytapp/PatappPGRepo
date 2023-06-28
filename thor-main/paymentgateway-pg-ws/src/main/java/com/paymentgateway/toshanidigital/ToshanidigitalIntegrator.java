
package com.paymentgateway.toshanidigital;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class ToshanidigitalIntegrator {

	@Autowired

	@Qualifier("toshanidigitalTransactionConverter")
	private TransactionConverter converter;

	@Autowired

	@Qualifier("toshanidigitalTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired

	@Qualifier("toshanidigitalFactory")
	private TransactionFactory transactionFactory;

	@Autowired
	private ToshanidigitalTransformer toshaniTransformer;
	
	public void process(Fields fields) throws SystemException {

		send(fields);

	}

	public void send(Fields fields) throws SystemException {

		Transaction transactionRequest = new Transaction();

		transactionRequest = transactionFactory.getInstance(fields);

		String request = converter.perpareRequest(fields, transactionRequest);

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.SALE.getName())) {

			String paymentResponse = communicator.getPaymentResponse(request, fields, transactionRequest);
			JSONObject jsonResponse = new JSONObject(paymentResponse);
			Transaction transactionResponse = converter.toTransactionCollect(jsonResponse);
			
			toshaniTransformer.updateCollectResponse(fields, transactionResponse);

		}
	}

}
