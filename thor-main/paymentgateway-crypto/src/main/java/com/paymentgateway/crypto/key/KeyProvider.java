package com.paymentgateway.crypto.key;

import com.paymentgateway.commons.exception.SystemException;

public interface KeyProvider {

	public String generateKey(String payId) throws SystemException;

	public String generateHostedKey(String payId) throws SystemException;
	
}
