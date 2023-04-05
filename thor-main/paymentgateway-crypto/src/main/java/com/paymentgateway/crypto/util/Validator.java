package com.paymentgateway.crypto.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;

@Service
public class Validator {

	public void validateRequest(String payId, String data) throws SystemException {
		if(StringUtils.isEmpty(payId) || StringUtils.isEmpty(data)) {
			throw new SystemException(ErrorType.INVALID_REQUEST_FIELD,"Invalid PAY_ID or Data: ");
		}
	}
}
