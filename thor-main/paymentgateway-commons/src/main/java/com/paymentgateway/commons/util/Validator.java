package com.paymentgateway.commons.util;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;

@Service
public interface Validator {
	public void validate(Fields fields) throws SystemException;
}
