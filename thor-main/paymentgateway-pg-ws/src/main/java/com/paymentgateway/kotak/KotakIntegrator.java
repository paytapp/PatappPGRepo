package com.paymentgateway.kotak;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class KotakIntegrator {
	
	@Autowired
	@Qualifier("kotakTransactionConverter")
	private TransactionConverter converter;
	
	@Autowired
	@Qualifier("kotakTransactionCommunicator")
	private TransactionCommunicator communicator;
	
	@Autowired
	private TransactionFactory TransactionFactory;
	
	private KotakTransformer kotakTransformer = null;

	public void process(Fields fields) throws SystemException {

		send(fields);

	}//process
	
	public void send(Fields fields) throws SystemException {
		
		Transaction transactionRequest = new Transaction();
		Transaction transactionResponse = new Transaction();
		
		transactionRequest = TransactionFactory.getInstance(fields);

		String request = converter.perpareRequest(fields, transactionRequest);

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.SALE.getName())) {
			communicator.updateSaleResponse(fields, request);
		} else {
			String response = communicator.getResponse(request, fields);
			 transactionResponse = converter.toTransaction(response);
			 kotakTransformer = new KotakTransformer(transactionResponse);
			 kotakTransformer.updateResponse(fields);
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

	/*public Transaction getTransactionRequest() {
		return transactionRequest;
	}

	public void setTransactionRequest(Transaction transactionRequest) {
		this.transactionRequest = transactionRequest;
	}

	public Transaction getTransactionResponse() {
		return transactionResponse;
	}

	public void setTransactionResponse(Transaction transactionResponse) {
		this.transactionResponse = transactionResponse;
	}*/

	public KotakTransformer getKotakTransformer() {
		return kotakTransformer;
	}

	public void setKotakTransformer(KotakTransformer kotakTransformer) {
		this.kotakTransformer = kotakTransformer;
	}

}
