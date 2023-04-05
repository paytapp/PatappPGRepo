package com.paymentgateway.pg.core.util;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;

public interface Processor {

	public abstract void preProcess(Fields fields) throws SystemException;

	public abstract void process(Fields fields) throws SystemException;

	public abstract void postProcess(Fields fields) throws SystemException;

}