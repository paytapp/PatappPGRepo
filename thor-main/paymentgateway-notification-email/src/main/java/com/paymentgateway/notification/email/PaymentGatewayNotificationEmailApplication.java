package com.paymentgateway.notification.email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.paymentgateway.commons", "com.paymentgateway.notification.email" })
public class PaymentGatewayNotificationEmailApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(PaymentGatewayNotificationEmailApplication.class, args);
		
	}
	
	 @Override
	  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
	      return builder.sources(PaymentGatewayNotificationEmailApplication.class);
	  }
}
