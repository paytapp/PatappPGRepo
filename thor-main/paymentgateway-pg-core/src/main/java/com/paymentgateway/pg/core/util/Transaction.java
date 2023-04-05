package com.paymentgateway.pg.core.util;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;

public interface Transaction {
	
	public void transact(Fields fields) throws SystemException;
	
}
