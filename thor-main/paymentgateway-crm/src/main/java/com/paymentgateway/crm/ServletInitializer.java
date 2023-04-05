package com.paymentgateway.crm;

import org.springframework.boot.builder.SpringApplicationBuilder;

public class ServletInitializer {

	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(PaymentGatewayCrmApplication.class);
	}
}
