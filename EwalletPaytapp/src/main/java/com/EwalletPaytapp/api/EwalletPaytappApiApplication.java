package com.EwalletPaytapp.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan({ "com.paymentgateway.commons", "com.paymentgateway.api","com.paymentgateway" })
public class EwalletPaytappApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(EwalletPaytappApiApplication.class, args);
		
	}
	
	 protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
	      return builder.sources(EwalletPaytappApiApplication.class);
	  }
}
