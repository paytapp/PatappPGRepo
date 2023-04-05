package com.paymentgateway.commons.mongo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;

@Configuration
public class MongoClientConfiguration {

	private static final String prefix = "MONGO_DB_";

	@Bean
	public MongoClient getMongoClient() {
		
		String mongoURL = PropertiesManager.propertiesMap.get(prefix+Constants.MONGO_URI_PREFIX.getValue())+
				PropertiesManager.propertiesMap.get(prefix+Constants.MONGO_USERNAME.getValue())+":"+
				PropertiesManager.propertiesMap.get(prefix+Constants.MONGO_PASSWORD.getValue())+
				PropertiesManager.propertiesMap.get(prefix+Constants.MONGO_URI_SUFFIX.getValue());
		
		MongoClientURI mClientURI = new MongoClientURI(
				mongoURL);
		return new MongoClient(mClientURI);
	}
}
