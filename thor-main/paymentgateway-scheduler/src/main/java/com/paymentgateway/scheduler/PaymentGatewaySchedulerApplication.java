package com.paymentgateway.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.paymentgateway.commons", "com.paymentgateway.pg.core", "com.paymentgateway.scheduler" })
public class PaymentGatewaySchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentGatewaySchedulerApplication.class, args);
	}
}