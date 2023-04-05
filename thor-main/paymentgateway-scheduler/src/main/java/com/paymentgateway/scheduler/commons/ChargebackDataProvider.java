package com.paymentgateway.scheduler.commons;

import java.util.ArrayList;
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
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class ChargebackDataProvider {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ConfigurationProvider configurationProvider;

	private static final Logger logger = LoggerFactory.getLogger(ChargebackDataProvider.class);

	public boolean checkRefundSettledForChargeback(String pgRefNum) {
		logger.info("Inside checkRefundSettledForChargeback(), ChargebackDataProvider");
		BasicDBObject pgRefQuerry = new BasicDBObject();
		pgRefQuerry.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
		List<BasicDBObject> refundConditionQueryList = new ArrayList<BasicDBObject>();
		refundConditionQueryList
				.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
		refundConditionQueryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
		BasicDBObject refundConditionQueryObj = new BasicDBObject("$and", refundConditionQueryList);
		List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();
		finalList.add(refundConditionQueryObj);
		finalList.add(pgRefQuerry);
		BasicDBObject finalquery = new BasicDBObject("$and", finalList);

		logger.info("Final querry for checking reco refunded transaction fpr PG REF NUM: " + pgRefNum + " is: "
				+ finalquery);

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> collection = dbIns.getCollection(configurationProvider.getMONGO_DB_collectionName());

		BasicDBObject match = new BasicDBObject("$match", finalquery);

		List<BasicDBObject> pipeline = Arrays.asList(match);
		AggregateIterable<Document> output = collection.aggregate(pipeline);
		output.allowDiskUse(true);

		MongoCursor<Document> cursor = output.iterator();
		if (cursor.hasNext()) {
			return true;
		} else {
			return false;
		}
	}
}
