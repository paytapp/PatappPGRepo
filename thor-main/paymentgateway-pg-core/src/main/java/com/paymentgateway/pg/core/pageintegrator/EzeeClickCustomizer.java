package com.paymentgateway.pg.core.pageintegrator;

import org.springframework.stereotype.Service;

import com.opensymphony.xwork2.Action;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;
@Service
public class EzeeClickCustomizer implements Customizer{

	@Override
	public String integrate(Fields fields) {
		fields.put(FieldType.TXNTYPE.getName(),TransactionType.SALE.getName());
		fields.logAllFields("All Response fields Recieved");
		return Action.NONE;
	}
}
