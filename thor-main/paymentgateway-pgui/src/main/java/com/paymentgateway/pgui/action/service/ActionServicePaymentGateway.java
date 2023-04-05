package com.paymentgateway.pgui.action.service;

import java.util.Map;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;

public interface ActionServicePaymentGateway {
	public Fields prepareFieldspg(Map<String, String[]> map) throws SystemException;
	
	
}
