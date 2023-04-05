package com.paymentgateway.requestrouter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.paymentgateway.commons.util.Fields;

@Configuration
public class RequestRouterConfig {

	@Bean
	public RequestRouter getRouter(Fields fields){
		return new RequestRouter(fields);
	}
}
