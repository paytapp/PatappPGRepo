package com.paymentgateway.isgpay;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Rahul
 *
 */

@Service
public class ISGPayIntegrator {

	@Autowired
	@Qualifier("iSGPayTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("iSGPayTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	private TransactionFactory transactionFactory;

	private ISGPayTransformer iSGPayTransformer = null;

	public void process(Fields fields) throws SystemException {

		send(fields);

	}// process

	public void send(Fields fields) throws SystemException {

		// For Rupay Transactions, separate Details are present

		String rupayFlag = PropertiesManager.propertiesMap.get(Constants.SELECT_MID_FOR_RUPAY);

		Transaction transactionRequest = new Transaction();
		Transaction transactionResponse = new Transaction();

		transactionRequest = transactionFactory.getInstance(fields, rupayFlag);
		String request = converter.perpareRequest(fields, transactionRequest);
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.SALE.getName())) {
			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())) {
				String vpaResponse = communicator.vpaValidationResponse(fields, request);
				transactionResponse = converter.toVpaTransaction(vpaResponse, fields);
				iSGPayTransformer = new ISGPayTransformer(transactionResponse);
				iSGPayTransformer.updateVpaValidationResponse(fields);
			} else {
				communicator.updateSaleResponse(fields, request);
			}
		} else {
			String response = communicator.getResponse(request, fields);

			transactionResponse = converter.toTransaction(response, fields.get(FieldType.TXNTYPE.getName()));
			iSGPayTransformer = new ISGPayTransformer(transactionResponse);
			iSGPayTransformer.updateResponse(fields);
		}

	}

}
