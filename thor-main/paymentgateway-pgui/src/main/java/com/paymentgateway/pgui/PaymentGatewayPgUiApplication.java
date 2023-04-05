package com.paymentgateway.pgui;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.StaticDataProvider;

@SpringBootApplication
@ComponentScan({ "com.paymentgateway" })
public class PaymentGatewayPgUiApplication {

	{
		UserDao userDao = new UserDao();
		userDao.getUserActiveList();
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleAtFixedRate(StaticDataProvider::updateMapValues, 0, 300, TimeUnit.SECONDS);
	}

	public static void main(String[] args) {
		SpringApplication.run(PaymentGatewayPgUiApplication.class, args);
	}

}
