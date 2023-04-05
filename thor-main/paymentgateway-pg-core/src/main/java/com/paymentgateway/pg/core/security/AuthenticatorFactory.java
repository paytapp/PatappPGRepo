package com.paymentgateway.pg.core.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticatorFactory {

	@Bean
	public static Authenticator getAuthenticator(){
		return new AuthenticatorImpl();
	}
}
