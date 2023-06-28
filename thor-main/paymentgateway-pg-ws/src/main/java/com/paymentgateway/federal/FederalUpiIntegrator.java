package com.paymentgateway.federal;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Rahul
 *
 */
@Service
public class FederalUpiIntegrator {

	@Autowired
	@Qualifier("federalUPITransactionConverter")
	private UpiTranssactionConverter converter;

	@Autowired
	@Qualifier("upiFederalFactory")
	private UpiTransactionFactory TransactionFactory;

	@Autowired
	@Qualifier("federalUpiTransactionCommunicator")
	private UpiTransactionCommunicator communicator;

	private FederalUpiTransformer federalUpiTransformer = null;

	public void process(Fields fields) throws SystemException {
		Transaction transactionRequest = TransactionFactory.getInstance(fields);
		String vpaValidationFlag = PropertiesManager.propertiesMap.get(Constants.VPA_VALIDATOR_FLAG);
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (vpaValidationFlag.equals(Constants.VPA_VALIDATE_FLAG) && txnType.equals(TransactionType.SALE.getName())) {
			Transaction response = vpaAddressValidator(fields);
			String responseCode = response.getResponseCode();
			if (responseCode.equals(Constants.UPI_SUCCESS_CODE)) {
				send(fields);
			} else {
				communicator.updateInvalidVpaResponse(fields, response);
			}
		}
		else {
			send(fields);
		}

	}// process

	public Transaction vpaAddressValidator(Fields fields) throws SystemException {

		JSONObject request = converter.vpaValidatorRequest(fields);
		JSONObject response = communicator.getResponse(request, fields);

		Transaction transactionResponse = converter.toTransactionForVpaValidator(response);
		return transactionResponse;

	}

	public void send(Fields fields) throws SystemException {

		Transaction transactionResponse = new Transaction();

		JSONObject request = converter.perpareRequest(fields);

		JSONObject response = communicator.getResponse(request, fields);

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.SALE.getName())) {
			transactionResponse = converter.toTransactionForCollect(response);
			communicator.updateSaleResponse(fields, transactionResponse);
		} else if (txnType.equals(TransactionType.REFUND.getName())) {
			transactionResponse = converter.toTransactionForCollect(response);
			communicator.updateRefundResponse(fields, transactionResponse);
		} else {
			transactionResponse = converter.toTransaction(response);
			federalUpiTransformer = new FederalUpiTransformer(transactionResponse);
			federalUpiTransformer.updateResponse(fields);
		}
	}

}
