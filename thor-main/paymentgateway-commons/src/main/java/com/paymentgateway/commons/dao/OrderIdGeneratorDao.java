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
public class OrderIdGeneratorDao {
	private static Logger logger = LoggerFactory.getLogger(OrderIdGeneratorDao.class.getName());

	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;
	
	
	@SuppressWarnings("unchecked")
	public Map<String, String> fetchLastOrderId() {
		Map<String, String> orderIdDetail=new HashMap<String, String>();
		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.ORDER_ID_GENERATOR_COLLECTION.getValue()));

			FindIterable<Document> iterDoc = coll.find();
			MongoCursor<Document> cursor = iterDoc.iterator();
			
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				orderIdDetail.put(FieldType.ORDER_ID.getName(), dbobj.getString(FieldType.ORDER_ID.getName()));
				orderIdDetail.put(FieldType.ALPHA_SERIES.getName(), dbobj.getString(FieldType.ALPHA_SERIES.getName()));
				orderIdDetail.put(FieldType.NUMERIC_SERIES.getName(), dbobj.getString(FieldType.NUMERIC_SERIES.getName()));
			}
			cursor.close();

		} catch (Exception ex) {
			logger.error("Exception  ", ex);
			
		}
		return orderIdDetail;

	}
	
	@SuppressWarnings("unchecked")
	public void insertLatestOrderId(String orderId, String alphaSeries, String numericSeries) {
		
		logger.info("inside insert orderId "+orderId);
		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.ORDER_ID_GENERATOR_COLLECTION.getValue()));

			
			
			Document setData = new Document();
			
			setData.put(FieldType.ORDER_ID.getName(), orderId);
			setData.put(FieldType.ALPHA_SERIES.getName(), alphaSeries);
			setData.put(FieldType.NUMERIC_SERIES.getName(), numericSeries);

			coll.insertOne(setData);

		} catch (Exception ex) {
			logger.error("Exception  ", ex);	
		}
	}
	
	@SuppressWarnings("unchecked")
	public void updateLatestOrderId(String oldOrderId, String newOrderId, String alphaSeries, String numericSeries) {
		
		logger.info("inside Update orderId old OrderID "+oldOrderId+" new OrderId "+newOrderId);
		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.ORDER_ID_GENERATOR_COLLECTION.getValue()));

			
			BasicDBObject newDocument = new BasicDBObject();
		    newDocument
		    .append("$set", new BasicDBObject().append(FieldType.ORDER_ID.getName(), newOrderId)
		    .append(FieldType.ALPHA_SERIES.getName(), alphaSeries)
		    .append(FieldType.NUMERIC_SERIES.getName(), numericSeries));
		            
		    BasicDBObject searchQuery = new BasicDBObject().append(FieldType.ORDER_ID.getName(), oldOrderId);

		    coll.updateOne(searchQuery, newDocument);

		} catch (Exception ex) {
			logger.error("Exception  ", ex);	
		}
	}


}
