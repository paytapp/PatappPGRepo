package com.paymentgateway.commons.mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoDatabase;
import com.paymentgateway.scheduler.commons.ConfigurationProvider;

/**
 * @author Amitosh
 *
 */

@Service
public class MongoInstance {

	@Autowired
	private MongoClientConfiguration mongoClientConfiguration ;
	
	@Autowired
	private ConfigurationProvider configurationProvider;

	public MongoDatabase getDB() {
		return mongoClientConfiguration.getMongoClient()
				.getDatabase(configurationProvider.getMONGO_DB_dbName());
	}
}