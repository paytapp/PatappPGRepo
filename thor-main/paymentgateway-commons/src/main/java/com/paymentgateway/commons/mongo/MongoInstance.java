package com.paymentgateway.commons.mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class MongoInstance {

	@Autowired
	private MongoClientConfiguration mongoClientConfiguration ;

	private static final String prefix = "MONGO_DB_";

	public MongoDatabase getDB() {
		return mongoClientConfiguration.getMongoClient()
				.getDatabase(PropertiesManager.propertiesMap.get(prefix+Constants.DB_NAME.getValue()));
	}

	//private static MongoClient mClient;
/*
	private MongoClient getMongoClient() {

		if (mClient == null) {
			MongoClientURI mClientURI = new MongoClientURI(
					propertiesManager.getmongoDbParam(Constants.MONGO_URI.getValue()));
			mClient = new MongoClient(mClientURI);
		}
		return mClient;
	}

	public MongoDatabase getDB() {
		return getMongoClient().getDatabase(propertiesManager.getmongoDbParam(Constants.DB_NAME.getValue()));
	}*/

}
