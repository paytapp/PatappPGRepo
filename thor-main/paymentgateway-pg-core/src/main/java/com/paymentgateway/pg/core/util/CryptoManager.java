package com.paymentgateway.pg.core.util;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;

@Service
public interface CryptoManager {
	public void secure(Fields fields) throws SystemException;

	public void removeSecureFields(Fields fields);

	public void hashCardDetails(Fields fields) throws SystemException;
	
	public void encryptCardDetails(Fields fields);
	
	public String maskCardNumber(String cardNumber);
}
