package com.paymentgateway.pg.core.acquirerDoubleVerification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class CheckDBEntryForPgref {

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;

	private static final String prefix = "MONGO_DB_";

	@SuppressWarnings("unchecked")
	public Map<String, String> searchPaymentStatus(String pgRefNum) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject allParamQuery = new BasicDBObject();

		paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));

		if (!paramConditionLst.isEmpty()) {
			allParamQuery = new BasicDBObject("$and", paramConditionLst);
		}

		BasicDBObject match = new BasicDBObject("$match", allParamQuery);
		BasicDBObject groupFields = new BasicDBObject("_id", "$PG_REF_NUM").append("entries",
				new BasicDBObject("$push", "$$ROOT"));
		BasicDBObject group = new BasicDBObject("$group", groupFields);
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);

		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
		while (cursor.hasNext()) {
			Document mydata = cursor.next();
			List<Document> courses = (List<Document>) mydata.get("entries");
			Document dbobj = courses.get(0);

			Map<String, Object> map = new HashMap<>(dbobj);
			Map<String, String> responseMap = new HashMap<String, String>();
			for (Entry<String, Object> mapObj : map.entrySet()) {
				if (mapObj.getValue() != null) {
					responseMap.put(mapObj.getKey(), mapObj.getValue().toString());
				}
			}
			cursor.close();
			return responseMap;

		}
		return null;

	}
}
