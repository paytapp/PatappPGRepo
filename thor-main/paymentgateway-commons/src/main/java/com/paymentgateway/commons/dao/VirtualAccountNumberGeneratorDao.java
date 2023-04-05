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

/*
 * @auther Sandeep Sharma 
 */

@Service("virtualAccountNumberGeneratorDao")
public class VirtualAccountNumberGeneratorDao {

	private static Logger logger = LoggerFactory.getLogger(VirtualAccountNumberGeneratorDao.class.getName());

	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	public Map<String, String> fatchVirtualAccountNo() {
		Map<String, String> details = new HashMap<String, String>();

		MongoDatabase dbIns = mongoInstance.getDB();

		MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
				.get(prefix + Constants.VIRTUAL_ACCOUNT_NUM_GENERATOR_COLLECTION.getValue()));

		FindIterable<Document> iterDoc = coll.find();
		MongoCursor<Document> cursor = iterDoc.iterator();

		while (cursor.hasNext()) {
			Document dbobj = cursor.next();
			details.put(FieldType.VIRTUAL_ACC_NUM.getName(), dbobj.getString(FieldType.VIRTUAL_ACC_NUM.getName()));
		}
		cursor.close();

		return details;
	}

	public void insertLatestVirtualAccountNo(String virtualAccountNo) {
		logger.info("inside insert insertLatestVirtualAccountNo " + virtualAccountNo);
		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.VIRTUAL_ACCOUNT_NUM_GENERATOR_COLLECTION.getValue()));

			Document setData = new Document();
			setData.put(FieldType.VIRTUAL_ACC_NUM.getName(), virtualAccountNo);
			coll.insertOne(setData);

		} catch (Exception e) {
			logger.error("Exception = " , e);
		}

	}

	public void updateLatestVirtualAccountNo(String oldVirtualAccountNo, String newVirtualAccountNo) {
		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.VIRTUAL_ACCOUNT_NUM_GENERATOR_COLLECTION.getValue()));
			BasicDBObject query = new BasicDBObject().append(FieldType.VIRTUAL_ACC_NUM.getName(), oldVirtualAccountNo);
			BasicDBObject setData = new BasicDBObject();
			setData.append("$set", new BasicDBObject().append(FieldType.VIRTUAL_ACC_NUM.getName(), newVirtualAccountNo));

			coll.updateOne(query, setData);

		} catch (Exception ex) {
			logger.error("Exception  ", ex);
		}
	}

}
