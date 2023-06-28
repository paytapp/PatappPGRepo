package com.paymentgateway.apexPay;

import java.util.Map;

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
public class ApexPaySaleResponseHandler {

	private static Logger logger = LoggerFactory.getLogger(ApexPaySaleResponseHandler.class.getName());

	@Autowired
	private Validator generalValidator;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	public Map<String, String> process(Fields fields) throws SystemException {

		String newTxnId = TransactionManager.getNewTransactionId();
		fields.put(FieldType.TXN_ID.getName(), newTxnId);
		//generalValidator.validate(fields);
		Transaction transactionResponse = new Transaction();
		String response = fields.get(FieldType.APEXPAY_RESPONSE_FIELD.getName());
		transactionResponse = toTransaction(response,fields);

		ApexPayTransformer letzpaycheckoutTransformer = new ApexPayTransformer(transactionResponse);
		letzpaycheckoutTransformer.updateResponse(fields);

		fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
		fields.remove(FieldType.APEXPAY_RESPONSE_FIELD.getName());
		ProcessManager.flow(updateProcessor, fields, true);
		return fields.getFields();

	}

	public Transaction toTransaction(String response,Fields fields) {

		Transaction transaction = new Transaction();

		String respSplit [] = response.split("~");
		
		for (String data : respSplit) {
			
			String dataSplit [] = data.split("=");
			
			String key = dataSplit[0].trim();
			String value = dataSplit[1].trim();
			
			if (key.equalsIgnoreCase("RESPONSE_CODE")) {
				transaction.setResponseCode(value);
			}
			
			else if (key.equalsIgnoreCase("STATUS")) {
				transaction.setStatus(value);
			}
			
			else if (key.equalsIgnoreCase("ACQ_ID")) {
				transaction.setAcqId(value);
				transaction.setRrn(value);
			}
			
			// Set both as same -- Quick Fix by Shaiwal
			/*
			 * else if (key.equalsIgnoreCase("RRN")) { transaction.setRrn(value); }
			 */
			
			else if (key.equalsIgnoreCase("PG_TXN_MESSAGE")) {
				transaction.setPgTxnMsg(value);
			}
			
		}
		
		return transaction;
		
	}// toTransaction()

}
