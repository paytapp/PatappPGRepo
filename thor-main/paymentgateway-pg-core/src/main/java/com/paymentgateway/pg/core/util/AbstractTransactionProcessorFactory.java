package com.paymentgateway.pg.core.util;

import org.springframework.stereotype.Component;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.TransactionProcessor;

@Component
public interface AbstractTransactionProcessorFactory {
	
	public TransactionProcessor getInstance(Fields fields) throws SystemException;

}
