package com.paymentgateway.pg.history;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.Processor;

@Service("historyProcessor")
public class HistoryProcessor implements Processor {

	@Autowired
	private Historian historian;

	public void preProcess(Fields fields) throws SystemException {
		historian.findPrevious(fields);
	}// preProcess()

	public void process(Fields fields) throws SystemException {
		historian.populateFieldsFromPrevious(fields);

		historian.validateSupportTransaction(fields);

		// Check duplicate authorization transaction
		historian.detectDuplicate(fields);
		
		//Add NewOrder fields in Sale and Enroll transactions
		historian.findNewOrderPreviousFields(fields);
	}

	public void postProcess(Fields fields) {
		if (fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.NEWORDER.getName())
				&& fields.get(FieldType.STATUS.getName()).equals(StatusType.PENDING.getName())) {
			String responseCode = fields.get(FieldType.RESPONSE_CODE.getName());
			if (null == responseCode) {
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
			}
		}
	}
}
