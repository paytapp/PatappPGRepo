package com.paymentgateway.pg.core.fraudPrevention.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.Processor;

/**
 * @author Harpreet, Rahul
 *
 */
@Service("fraudProcessor")
public class FraudPreventionProcessor implements Processor {

	@Autowired
	private FraudRuleImplementor fraudRuleImplementor;

	@Override
	public void preProcess(Fields fields) throws SystemException {

	}

	@Override
	public void process(Fields fields) throws SystemException {

		// fraud rule switch
		fraudRuleImplementor.applyRule(fields);
	}

	@Override
	public void postProcess(Fields fields) throws SystemException {

	}
}
