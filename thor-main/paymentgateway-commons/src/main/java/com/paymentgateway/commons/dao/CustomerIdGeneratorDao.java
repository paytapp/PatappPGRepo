package com.paymentgateway.commons.dao;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class CustomerIdGeneratorDao {
	private static Logger logger = LoggerFactory.getLogger(CustomerIdGeneratorDao.class.getName());

	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	@SuppressWarnings("unchecked")
	public Map<String, String> fetchLastCustomerId() {
		
		Map<String, String> cutomerIdDetail = new HashMap<String, String>();
		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CUST_ID_COLLECTION.getValue()));

			FindIterable<Document> iterDoc = coll.find();
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				cutomerIdDetail.put(FieldType.CUSTOMER_ID.getName(), dbobj.getString(FieldType.CUSTOMER_ID.getName()));
			}
			cursor.close();

		} catch (Exception ex) {
			logger.error("Exception  ", ex);

		}
		return cutomerIdDetail;
	}

	@SuppressWarnings("unchecked")
	public void insertLatestCustomerId(String cusomerId) {

		logger.info("inside insert CUSTOMER_ID " + cusomerId);
		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CUST_ID_COLLECTION.getValue()));

			Document setData = new Document();

			setData.put(FieldType.CUSTOMER_ID.getName(), cusomerId);
			coll.insertOne(setData);

		} catch (Exception ex) {
			logger.error("Exception  ", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public void updateLatestCustomerId(String oldCustomerId, String newCustomerId) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CUST_ID_COLLECTION.getValue()));

			BasicDBObject newDocument = new BasicDBObject();
			newDocument.append("$set", new BasicDBObject().append(FieldType.CUSTOMER_ID.getName(), newCustomerId));
			BasicDBObject searchQuery = new BasicDBObject().append(FieldType.CUSTOMER_ID.getName(), oldCustomerId);
			coll.updateOne(searchQuery, newDocument);

		} catch (Exception ex) {
			logger.error("Exception  ", ex);
		}
	}

}
