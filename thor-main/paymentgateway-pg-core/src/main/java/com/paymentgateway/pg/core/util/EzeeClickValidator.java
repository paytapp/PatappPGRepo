package com.paymentgateway.pg.core.util;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.Validator;

@Service
public class EzeeClickValidator implements Validator{

	@Override
	public void validate(Fields fields) throws SystemException {

	}

}
