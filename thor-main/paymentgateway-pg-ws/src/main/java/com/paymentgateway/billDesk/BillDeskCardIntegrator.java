package com.paymentgateway.billDesk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class BillDeskCardIntegrator {

	@Autowired
	@Qualifier("billDeskTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("billDeskTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	private TransactionFactory TransactionFactory;

	private BillDeskTransformer billDeskTransformer = null;

	public void process(Fields fields) throws SystemException {

		send(fields);

	}// process

	public void send(Fields fields) throws SystemException {

		Transaction transactionRequest = new Transaction();
		Transaction transactionResponse = new Transaction();
		transactionRequest = TransactionFactory.getInstance(fields);
		String request = converter.perpareRequest(fields, transactionRequest);
		String response = communicator.getResponse(request, fields);
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.ENROLL.getName())) {
			transactionResponse = converter.toTransactionCard(response);
		} else if (txnType.equals(TransactionType.SALE.getName())) {
			transactionResponse = converter.toTransactionAuthorization(response, fields);
		} else {
			transactionResponse = converter.toTransaction(response, fields);
		}

		billDeskTransformer = new BillDeskTransformer(transactionResponse);
		billDeskTransformer.updateCardTxnResponse(fields);

	}

}
