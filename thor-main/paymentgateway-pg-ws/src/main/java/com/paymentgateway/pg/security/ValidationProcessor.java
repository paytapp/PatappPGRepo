package com.paymentgateway.pg.security;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.pg.core.util.Processor;
@Service
public class ValidationProcessor implements Processor {

	public void preProcess(Fields fields) {
	}

	public void process(Fields fields) throws SystemException {
		Validator validator = ValidatorFactory.getValidator();
		validator.validate(fields);
	}

	public void postProcess(Fields fields) {
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.REFUND.getName())) {
			fields.put(FieldType.DELTA_REFUND_FLAG.getName(), "N");
			String refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
			if ((refundFlag == null)) {
				fields.put(FieldType.REFUND_FLAG.getName(), "C");
			}
		}
	}
}
