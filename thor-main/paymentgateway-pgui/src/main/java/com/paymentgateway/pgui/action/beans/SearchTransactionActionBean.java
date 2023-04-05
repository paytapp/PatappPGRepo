package com.paymentgateway.pgui.action.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
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
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TxnType;

@Service
public class SearchTransactionActionBean {

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;

	private static final String prefix = "MONGO_DB_";

	public Map<String, String> searchPayment(String pgRefNum, String oid) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject allParamQuery = new BasicDBObject();

		if (StringUtils.isNotBlank(pgRefNum)) {
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
				String status = (dbobj).getString(FieldType.STATUS.toString());
				if ((status.equals(StatusType.SENT_TO_BANK.getName().toString()))
						|| (status.equals(StatusType.PENDING.getName().toString()))) {
					continue;
				} else {
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
			}
			return null;

		} else if (StringUtils.isNotBlank(oid)) {
			paramConditionLst.add(new BasicDBObject(FieldType.OID.getName(), oid));
			paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TxnType.SALE.getName()));
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			BasicDBObject match = new BasicDBObject("$match", allParamQuery);

			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document mydata = cursor.next();
				/*
				 * List<Document> courses = (List<Document>) mydata.get("entries"); Document
				 * dbobj = mydata.get(0);
				 */
				String status = mydata.getString(FieldType.STATUS.toString());
				if ((status.equals(StatusType.SENT_TO_BANK.getName().toString()))
						|| (status.equals(StatusType.PENDING.getName().toString()))) {
					continue;
				} else {
					Map<String, Object> map = new HashMap<>(mydata);
					Map<String, String> responseMap = new HashMap<String, String>();
					for (Entry<String, Object> mapObj : map.entrySet()) {
						if (mapObj.getValue() != null) {
							responseMap.put(mapObj.getKey(), mapObj.getValue().toString());
						}
					}
					cursor.close();
					return responseMap;
				}
			}
			return null;
		}
		return null;

	}

}
