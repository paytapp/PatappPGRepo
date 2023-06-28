package com.paymentgateway.pg.ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.StaticDataProvider;

@SpringBootApplication
@ComponentScan("com.paymentgateway")
@ServletComponentScan("com.paymentgateway")
public class PaymentGatewayPgWsApplication extends SpringBootServletInitializer{

	public static void main(String[] args) {
		{
			UserDao userDao = new UserDao();
			userDao.getUserActiveList();
			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
			executorService.scheduleAtFixedRate(StaticDataProvider::updateMapValues, 0, 300, TimeUnit.SECONDS);
			
			SpringApplication.run(PaymentGatewayPgWsApplication.class, args);
		}
		
	}

	 @Override
	  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
	      return builder.sources(PaymentGatewayPgWsApplication.class);
	  }
}
