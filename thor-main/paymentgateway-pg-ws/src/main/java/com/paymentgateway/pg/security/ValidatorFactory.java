package com.paymentgateway.pg.security;

import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;

public class ValidatorFactory {

	public ValidatorFactory() {
	}

	public static Validator	getValidator(){
		return new GeneralValidator();
	}
}
