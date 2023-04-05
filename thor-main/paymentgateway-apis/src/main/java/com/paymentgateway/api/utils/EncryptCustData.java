package com.paymentgateway.api.utils;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class EncryptCustData {

	@Autowired
	private DataEncDecTool dataEncDecTool;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	private static final String prefix = "MONGO_DB_";
	private static Logger logger = LoggerFactory.getLogger(EncryptCustData.class.getName());

	public void encryptData(String collname) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(collname);

		BasicDBObject query = new BasicDBObject();
		query.append(FieldType.IS_ENCRYPTED.getName(), new BasicDBObject("$ne", "Y"));

		long count = coll.count(query);
		logger.info("Total Records to Encrypt = " + count);

		int processedDoc = 0;

		String encDecFields = propertiesManager.propertiesMap.get("TransactionEncFields");
		String encDecFieldsArr[] = encDecFields.split(",");

		BasicDBObject projectElement = new BasicDBObject();

		for (String field : encDecFieldsArr) {
			projectElement.put(field, 1);
		}

		BasicDBObject project = new BasicDBObject("$project", projectElement);
		BasicDBObject match = new BasicDBObject("$match", query);

		List<BasicDBObject> pipeline = null;
		pipeline = Arrays.asList(match, project);
		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);

		MongoCursor<Document> cursor = output.iterator();

		while (cursor.hasNext()) {

			Document doc = cursor.next();

			if (doc.get(FieldType.IS_ENCRYPTED.getName()) != null
					&& doc.get(FieldType.IS_ENCRYPTED.getName()).toString().equalsIgnoreCase("Y")) {
				continue;
			}

			Document encDoc = dataEncDecTool.encryptDocument(doc);
			encDoc.put(FieldType.IS_ENCRYPTED.getName(), "Y");

			BasicDBObject oldFieldsObj = new BasicDBObject();
			oldFieldsObj.put("_id", doc.get("_id"));

			BasicDBObject updateObj = new BasicDBObject();
			updateObj.put("$set", encDoc);

			Document newDoc = new Document(updateObj);
			Document oldDoc = new Document(oldFieldsObj);
			coll.updateOne(oldDoc, newDoc);

			processedDoc = processedDoc + 1;
			if (processedDoc == 10000) {
				processedDoc = 0;
				count = count - 10000;
				logger.info("Total records remaining for encryption  >> " + count);
			}
		}

	}

	public void decryptData(String collname) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(collname);

		long count = coll.count();
		logger.info("Total Records to Decrypt = " + count);

		int processedDoc = 0;

		String encDecFields = propertiesManager.propertiesMap.get("TransactionEncFields");
		String encDecFieldsArr[] = encDecFields.split(",");

		BasicDBObject projectElement = new BasicDBObject();

		for (String field : encDecFieldsArr) {
			projectElement.put(field, 1);
		}

		BasicDBObject finalquery = new BasicDBObject(FieldType.IS_ENCRYPTED.getName(), "Y");

		BasicDBObject project = new BasicDBObject("$project", projectElement);
		BasicDBObject match = new BasicDBObject("$match", finalquery);

		List<BasicDBObject> pipeline = null;
		pipeline = Arrays.asList(match, project);
		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);

		MongoCursor<Document> cursor = output.iterator();

		while (cursor.hasNext()) {
			Document doc = cursor.next();

			Document encDoc = dataEncDecTool.decryptDocument(doc);
			encDoc.put(FieldType.IS_ENCRYPTED.getName(), "N");

			BasicDBObject oldFieldsObj = new BasicDBObject();
			oldFieldsObj.put("_id", doc.get("_id").toString());

			BasicDBObject updateObj = new BasicDBObject();
			updateObj.put("$set", encDoc);

			Document newDoc = new Document(updateObj);
			Document oldDoc = new Document(oldFieldsObj);
			coll.updateOne(oldDoc, newDoc);

			processedDoc = processedDoc + 1;
			if (processedDoc == 10000) {
				processedDoc = 0;
				count = count - 10000;
				logger.info("Total records remaining for decryption  >> " + count);
			}
		}

	}
}
