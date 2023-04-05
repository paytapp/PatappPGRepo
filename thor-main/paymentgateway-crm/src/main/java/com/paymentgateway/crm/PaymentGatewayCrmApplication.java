package com.paymentgateway.crm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.paymentgateway.commons.user.UserDao;

@SpringBootApplication
@ComponentScan({"com.paymentgateway.commons","com.paymentgateway.crm","com.paymentgateway.pg"})
public class PaymentGatewayCrmApplication {

	private static Logger logger = LoggerFactory.getLogger(PaymentGatewayCrmApplication.class.getName());

	{
		logger.info("static block called upon invocation");
		UserDao userDao = new UserDao();
		userDao.getUserActiveList();
	}
	
	
	public static void main(String[] args) {
		SpringApplication.run(PaymentGatewayCrmApplication.class, args);
		
	}

	
}
