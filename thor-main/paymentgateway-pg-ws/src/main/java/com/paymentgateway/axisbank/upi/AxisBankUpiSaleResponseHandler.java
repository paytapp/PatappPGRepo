package com.paymentgateway.axisbank.upi;

import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;

@Service
public class AxisBankUpiSaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(AxisBankUpiSaleResponseHandler.class.getName());

	@Autowired
	private Validator generalValidator;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	public static final String PIPE = "|";
	public static final String DOUBLE_PIPE = "||";

	public Map<String, String> process(Fields fields) throws SystemException {
		
		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		generalValidator.validate(fields);
		Transaction transactionResponse = new Transaction();
		String jsonResponse = fields.get(FieldType.AXISBANK_UPI_RESPONSE_FIELD.getName());
		transactionResponse = toTransaction(jsonResponse);

		AxisBankUpiTransformer axisBankUpiTransformer = new AxisBankUpiTransformer(transactionResponse);
		axisBankUpiTransformer.updateSaleResponse(fields,transactionResponse);

		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		fields.remove(FieldType.AXISBANK_UPI_RESPONSE_FIELD.getName());
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();

	}

	public Transaction toTransaction(String jsonResponse) {

		Transaction transaction = new Transaction();
		
		JSONObject response = new JSONObject(jsonResponse);
		
		transaction.setGatewayTransactionId((String) response.get("gatewayTransactionId"));
		transaction.setGatewayResponseCode((String) response.get("gatewayResponseCode"));
		transaction.setGatewayResponseMessage((String) response.get("gatewayResponseMessage"));
		transaction.setRrn((String) response.get("rrn"));
		
		return transaction;
	}// toTransaction()


}
