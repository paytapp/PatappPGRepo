package com.paymentgateway.billDesk;

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
import com.paymentgateway.pg.history.Historian;

@Service
public class BillDeskSaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(BillDeskSaleResponseHandler.class.getName());

	@Autowired
	private Validator generalValidator;
	
	@Autowired
	private Historian historian;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	private BillDeskTransformer billDeskTransformer;

	
	public Map<String, String> process(Fields fields) throws SystemException {
		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		generalValidator.validate(fields);
		Transaction transactionResponse = new Transaction();
		transactionResponse = toTransaction(fields);
		billDeskTransformer = new BillDeskTransformer(transactionResponse);
		billDeskTransformer.updateResponse(fields);

		historian.addPreviousSaleFields(fields);
		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();

	}
	
	public Transaction toTransaction(Fields fields) {

		Transaction transaction = new Transaction();
		transaction.setResponseCode(fields.get(FieldType.PG_RESP_CODE.getName()));

		return transaction;
	}// toTransaction()

	
}
