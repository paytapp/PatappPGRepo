package com.paymentgateway.pg.core.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;

@Service("responseProcessor")
public class ResponseProcessor implements Processor {

	@Autowired
	private TransactionResponser transactionResponser;

	public void preProcess(Fields fields) {
	}

	public void process(Fields fields) throws SystemException {

		transactionResponser.getResponse(fields);

	}

	public void postProcess(Fields fields) {
		fields.logAllFields("Sending Response to Client");
	}
}
