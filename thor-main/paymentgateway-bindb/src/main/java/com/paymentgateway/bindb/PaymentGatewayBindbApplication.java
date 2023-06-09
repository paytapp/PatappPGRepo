package com.paymentgateway.bindb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.PropertiesManager;

@SpringBootApplication
@ComponentScan({"com.paymentgateway"})
@ServletComponentScan("com.paymentgateway")
public class PaymentGatewayBindbApplication extends SpringBootServletInitializer implements CommandLineRunner{

	private static Logger logger = LoggerFactory.getLogger(PaymentGatewayBindbApplication.class.getName());

	{
		logger.info("static block called upon invocation");
		UserDao userDao = new UserDao();
		List<User> user = userDao.getUserActiveList();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(PaymentGatewayBindbApplication.class, args);
	}
	
	 @Override
	  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
	      return builder.sources(PaymentGatewayBindbApplication.class);
	  }

	@Override
	public void run(String... args) throws Exception {
		PropertiesManager propertiesManager = new PropertiesManager();
		
	}
}
