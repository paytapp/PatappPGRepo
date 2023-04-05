package com.paymentgateway.commons.mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.paymentgateway.scheduler.commons.ConfigurationProvider;

/**
 * @author Amitosh
 *
 */

@Configuration
public class MongoClientConfiguration {

	@Autowired
	private ConfigurationProvider configurationProvider;

	@Bean
	public MongoClient getMongoClient() {
		
		String mongoURL = configurationProvider.getMONGO_DB_mongoURIprefix()
				+ configurationProvider.getMONGO_DB_username() +":"+ configurationProvider.getMONGO_DB_password()
				+ configurationProvider.getMONGO_DB_mongoURIsuffix();

		MongoClientURI mClientURI = new MongoClientURI(mongoURL);
		return new MongoClient(mClientURI);
	}
}