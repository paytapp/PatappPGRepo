package com.paymentgateway.pg.core.pageintegrator;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.Processor;

public class MerchantHostedIntegrater implements Processor{

	@Override
	public void preProcess(Fields fields) throws SystemException {
		GeneralValidator generalValidator = new GeneralValidator();
		generalValidator.validateHash(fields);
		generalValidator.validateReturnUrl(fields);		
	}

	@Override
	public void process(Fields fields) throws SystemException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postProcess(Fields fields) throws SystemException {
		// TODO Auto-generated method stub
		
	}

}
