package com.paymentgateway.pg.core.pageintegrator;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Fields;

/**
 * @author Sunil
 *
 */
public interface Customizer {

	public String integrate(Fields fields) throws SystemException;
}
