package com.paymentgateway.airPay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class AirPayIntegrator {

	@Autowired
	@Qualifier("airPayTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("airPayTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	@Qualifier("airPayFactory")
	private TransactionFactory transactionFactory;

	private AirPayTransformer airPayTransformer = null;

	public void process(Fields fields) throws SystemException {

		send(fields);

	}

	public void send(Fields fields) throws SystemException {

		Transaction transactionRequest = new Transaction();
		Transaction transactionResponse = new Transaction();

		transactionRequest = transactionFactory.getInstance(fields);

		String request = converter.perpareRequest(fields, transactionRequest);

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.SALE.getName())) {
			communicator.updateSaleResponse(fields, request.toString());
		} else {
			String response = communicator.getResponse(request.toString(), fields);

			if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.STATUS.getName())) {
				transactionResponse = converter.toStatusTransaction(response);
				airPayTransformer = new AirPayTransformer(transactionResponse);
				airPayTransformer.updateStatusResponse(fields);
			}

			if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.REFUND.getName())) {
				transactionResponse = converter.toTransactionRefund(response);
				airPayTransformer = new AirPayTransformer(transactionResponse);
				airPayTransformer.updateRefundResponse(fields);
			}

		}
	}

}
