package com.paymentgateway.pgui.action.service;

import java.util.Map;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;

public interface ActionService {

	public Fields prepareFields(Map<String, String[]> map) throws SystemException;
	public Fields prepareFieldsMerchantHosted(Map<String, String[]> map) throws SystemException;
	
}
