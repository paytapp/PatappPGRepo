package com.paymentgateway.federal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Rahul
 *
 */
@Service
public class FederalIntegrator {

	@Autowired
	@Qualifier("federalTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("federalTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	private TransactionFactory TransactionFactory;

	private FederalTransformer federalTransformer = null;

	public void process(Fields fields) throws SystemException {

		send(fields);
	}// process

	public void send(Fields fields) throws SystemException {

		Transaction transactionRequest = new Transaction();
		Transaction transactionResponse = new Transaction();

		transactionRequest = TransactionFactory.getInstance(fields);

		String request = converter.perpareRequest(fields, transactionRequest);
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.ENROLL.getName())) {
			communicator.updateEnrollResponse(fields, request);
		} else {
			String response = communicator.getResponse(request, fields);

			transactionResponse = converter.toTransaction(response);
			federalTransformer = new FederalTransformer(transactionResponse);
			federalTransformer.updateResponse(fields);
		}

	}

	public TransactionConverter getConverter() {
		return converter;
	}

	public void setConverter(TransactionConverter converter) {
		this.converter = converter;
	}

	public TransactionCommunicator getCommunicator() {
		return communicator;
	}

	public void setCommunicator(TransactionCommunicator communicator) {
		this.communicator = communicator;
	}

	public FederalTransformer getFederalTransformer() {
		return federalTransformer;
	}

	public void setFederalTransformer(FederalTransformer federalTransformer) {
		this.federalTransformer = federalTransformer;
	}


}
