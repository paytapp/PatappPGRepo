package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.EventPages;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TDRStatus;

@Service
public class EventPagesDao {

	private static Logger logger = LoggerFactory.getLogger(EventPagesDao.class.getName());

	private static final String prefix = "MONGO_DB_";

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;

	public EventPages fetchuserDetails(String uniqueNo, String payId) {
		EventPages info = new EventPages();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		try {
			dbObjList.add(new BasicDBObject(FieldType.UNIQUE_NO.getName(), uniqueNo));
			dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			dbObjList.add(new BasicDBObject(FieldType.STATUS.getName(), TDRStatus.ACTIVE.getName()));
			BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.EVENT_PAGES_COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = coll.find(andQuery).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				info.setName(dbobj.getString("NAME"));
				info.setEmailId(dbobj.getString("EMAIL_ID"));
				info.setAddress(dbobj.getString("ADDRESS"));
				info.setMobileNo(dbobj.getString("MOBILE_NO"));
				info.setAmount(dbobj.getString("AMOUNT"));
				info.setRemarks(dbobj.getString("REMARKS"));
				info.setUniqueNo(dbobj.getString("UNIQUE_NO"));

			}

			return info;
		} catch (Exception ex) {
			logger.error("Exception while get the event pages details from MongoDB : " , ex);
		}
		return info;

	}

}
