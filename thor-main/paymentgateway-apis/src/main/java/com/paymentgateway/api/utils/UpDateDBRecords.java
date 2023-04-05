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
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class UpDateDBRecords {

	
	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private UserDao userdao;

	private static final String prefix = "MONGO_DB_";
	private static Logger logger = LoggerFactory.getLogger(UpDateDBRecords.class.getName());
	private static String suceess = "Sucess";
	private static String fail = "Fail";
	int count =0;

	public void upDateMongoRecords(String tableName ,String  collName) {
		count=0;
		logger.info("Mongo DB updating start >>>>>>>>>");
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(tableName);
		MongoCursor<Document> cursor = coll.find().iterator();
		while(cursor.hasNext()) {
			Document documentObj = cursor.next();
			Document setData = new Document();
			BasicDBObject query = new BasicDBObject();
			query.put("_id", documentObj.get("_id"));
			if(documentObj.get(collName)!=null && documentObj.get(collName).toString().contains("LPAY")) {
				setData.put(collName, updateRecords(documentObj.get(collName)));	
				Document update = new Document();
				update.put("$set", setData);
				coll.updateOne(query, update);
				count++;
			}
		}
		
		logger.info("Mongo DB Total update account count : "+count);
		logger.info("Mongo DB updateing End >>>>>>>>>");
	}

	public void upDateSQLRecords(String tableName ,String  collName) {
		count=0;
		logger.info("SQL DB updateing Start >>>>>>>>>");
		List<User> userList= userdao.getAllUserFetchOneColumn(tableName, collName);
		if(userList.size()!=0 && !userList.isEmpty()) {
			for(User user : userList) {
				if(user.getVirtualAccountNo().contains("LETZ")){
				user.setVirtualAccountNo(updateRecords(user.getVirtualAccountNo()));
				userdao.update(user);
				count++;
				}
			}
		}
		logger.info("SQL DB Total update account count : "+count);
		logger.info("SQL DB updateing END >>>>>>>>>");
	}

	public static String updateRecords (String replaceColl) {
		String virtualAccountNo=replaceColl.replace("LETZ", "LTZ");
		return virtualAccountNo;
		
	}
	
	public static Object updateRecords (Object inputDate) {
		Object customerAccountNo =inputDate.toString().replace("LPAY", "LTZ");	
		return customerAccountNo;
	}
}
