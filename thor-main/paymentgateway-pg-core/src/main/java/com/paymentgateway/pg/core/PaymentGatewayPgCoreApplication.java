package com.paymentgateway.pg.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.paymentgateway.commons"})
public class PaymentGatewayPgCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentGatewayPgCoreApplication.class, args);
	}
}
