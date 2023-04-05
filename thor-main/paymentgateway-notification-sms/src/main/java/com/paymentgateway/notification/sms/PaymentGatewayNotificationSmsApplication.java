package com.paymentgateway.notification.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;



@SpringBootApplication
@ComponentScan({ "com.paymentgateway.commons", "com.paymentgateway.notification.sms" })
public class PaymentGatewayNotificationSmsApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(PaymentGatewayNotificationSmsApplication.class, args);
		
	}
	
	 @Override
	  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
	      return builder.sources(PaymentGatewayNotificationSmsApplication.class);
	  }
}