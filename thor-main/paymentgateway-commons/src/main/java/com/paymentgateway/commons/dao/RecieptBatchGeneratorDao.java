package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class RecieptBatchGeneratorDao {

	/**
	 * Sandeep Sharma
	 */

	private static Logger logger = LoggerFactory.getLogger(RecieptBatchGeneratorDao.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	public Map<String, String> fatchLastRecieptNo() {
		Map<String, String> details = new HashMap<String, String>();

		MongoDatabase dbIns = mongoInstance.getDB();

		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECIEPT_BATCH_GENERATOR_COLLECTION.getValue()));

		FindIterable<Document> iterDoc = coll.find();
		MongoCursor<Document> cursor = iterDoc.iterator();

		while (cursor.hasNext()) {
			Document dbobj = cursor.next();
			details.put(FieldType.RECIEPT_NO.getName(), dbobj.getString(FieldType.RECIEPT_NO.getName()));
			details.put(FieldType.ALPHA_SERIES.getName(), dbobj.getString(FieldType.ALPHA_SERIES.getName()));

		}
		cursor.close();

		return details;
	}

	public void insertLatestRecieptNo(String newRecieptNo, String alpha) {
		logger.info("inside insert insertLatestRecieptNo " + newRecieptNo);
		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.RECIEPT_BATCH_GENERATOR_COLLECTION.getValue()));

			Document setData = new Document();
			setData.put(FieldType.RECIEPT_NO.getName(), newRecieptNo);
			setData.put(FieldType.ALPHA_SERIES.getName(), alpha);
			setData.put(FieldType.BATCH_NO.getName(), "000000");

			coll.insertOne(setData);

		} catch (Exception e) {
			logger.error("Exception = ", e);
		}

	}

	public void updateLatestRecieptNo(String oldRecieptNo, String newRecieptNo, String alpha) {

		// logger.info("inside Update reciept No old recieptNo " + oldRecieptNo + " new
		// recieptNo " + newRecieptNo);

		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.RECIEPT_BATCH_GENERATOR_COLLECTION.getValue()));
			BasicDBObject query = new BasicDBObject().append(FieldType.RECIEPT_NO.getName(), oldRecieptNo);
			BasicDBObject setData = new BasicDBObject();
			setData.append("$set", new BasicDBObject().append(FieldType.RECIEPT_NO.getName(), newRecieptNo)
					.append(FieldType.ALPHA_SERIES.getName(), alpha));

			coll.updateOne(query, setData);

		} catch (Exception ex) {
			logger.error("Exception  ", ex);
		}
	}

	public Map<String, String> fatchLastBatchNo() {
		Map<String, String> details = new HashMap<String, String>();

		MongoDatabase dbIns = mongoInstance.getDB();

		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECIEPT_BATCH_GENERATOR_COLLECTION.getValue()));

		FindIterable<Document> iterDoc = coll.find();
		MongoCursor<Document> cursor = iterDoc.iterator();

		while (cursor.hasNext()) {
			Document dbobj = cursor.next();
			details.put(FieldType.BATCH_NO.getName(), dbobj.getString(FieldType.BATCH_NO.getName()));
		}
		cursor.close();

		return details;
	}

	public void updateLatestBatchNo(String oldBatchNo, String newBatchNo) {

		// logger.info("inside Update batch No old batchNo " + oldBatchNo + " new
		// batchNo " + newBatchNo);

		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.RECIEPT_BATCH_GENERATOR_COLLECTION.getValue()));

			BasicDBObject query = new BasicDBObject().append(FieldType.BATCH_NO.getName(), oldBatchNo);

			BasicDBObject setData = new BasicDBObject();
			setData.append("$set", new BasicDBObject().append(FieldType.BATCH_NO.getName(), newBatchNo));

			coll.updateOne(query, setData);
		} catch (Exception ex) {
			logger.error("Exception  ", ex);
		}
	}

	public void insertLatestBatchNo(String newBatchNo) {
		logger.info("inside insert insertLatestBatchNo " + newBatchNo);
		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.RECIEPT_BATCH_GENERATOR_COLLECTION.getValue()));

			Document setData = new Document();
			setData.put(FieldType.RECIEPT_NO.getName(), "A000000");
			setData.put(FieldType.ALPHA_SERIES.getName(), "A");
			setData.put(FieldType.BATCH_NO.getName(), newBatchNo);

			coll.insertOne(setData);

		} catch (Exception e) {
			logger.error("Exception = ", e);
		}

	}

	public void updateLatestRecieptBatchNo(String oldBatchNo, String newBatchNo, String oldRecieptNo,
			String newRecieptNo, String alpha) {

		// logger.info("inside Update from MSCDCL class");

		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.RECIEPT_BATCH_GENERATOR_COLLECTION.getValue()));

			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(new BasicDBObject(FieldType.BATCH_NO.getName(), oldBatchNo));
			queryList.add(new BasicDBObject(FieldType.RECIEPT_NO.getName(), oldRecieptNo));

			BasicDBObject query = new BasicDBObject();
			query.append("$and", queryList);

			BasicDBObject setData = new BasicDBObject();
			setData.append("$set",
					new BasicDBObject().append(FieldType.BATCH_NO.getName(), newBatchNo)
							.append(FieldType.RECIEPT_NO.getName(), newRecieptNo)
							.append(FieldType.ALPHA_SERIES.getName(), alpha));
			coll.updateOne(query, setData);

		} catch (Exception ex) {
			logger.error("Exception  ", ex);
		}
	}

}
