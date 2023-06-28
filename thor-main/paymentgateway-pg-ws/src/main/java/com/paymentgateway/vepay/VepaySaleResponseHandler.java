package com.paymentgateway.vepay;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
public class VepaySaleResponseHandler {
	private static Logger logger = LoggerFactory.getLogger(VepaySaleResponseHandler.class.getName());
	
	@Autowired
	private Validator generalValidator;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	public Map<String, String> process(Fields fields) throws SystemException {

		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		generalValidator.validate(fields);
		String response = fields.get(FieldType.VEPAY_RESPONSE_FIELD.getName());

		Transaction transactionResponse = toTransaction(response, fields);

		VepayTransformer vepayTransformer = new VepayTransformer(transactionResponse);
		vepayTransformer.updateResponse(fields);

		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		fields.remove(FieldType.VEPAY_RESPONSE_FIELD.getName());
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();

	}

	public Transaction toTransaction(String response, Fields fields) {

		Transaction transaction = new Transaction();

		if (StringUtils.isBlank(response)) {
			logger.info("Empty response received from Vepay");
			return transaction;
		}

		String respArray[] = response.split("&&");
		
		String other_details = respArray[0];
		String pg_details = respArray[1];
		String txn_response = respArray[2];
		String fraud_details = respArray[3];
		
		String txn_response_Arr[] = txn_response.split("\\|");
		
		/*
		 * transaction.setAg_ref(txn_response_Arr[8]);
		 * transaction.setPg_ref(txn_response_Arr[9]);
		 * transaction.setStatus(txn_response_Arr[10]);
		 * transaction.setRes_code(txn_response_Arr[11]);
		 * transaction.setRes_message(txn_response_Arr[12]);
		 */

		return transaction;
	}

}
