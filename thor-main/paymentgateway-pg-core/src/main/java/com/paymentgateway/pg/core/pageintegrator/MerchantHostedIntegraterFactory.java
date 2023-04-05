package com.paymentgateway.pg.core.pageintegrator;

import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.Processor;

public class MerchantHostedIntegraterFactory {

	public static Processor instance(Fields fields){
		return new MerchantHostedIntegrater();
	}
}
