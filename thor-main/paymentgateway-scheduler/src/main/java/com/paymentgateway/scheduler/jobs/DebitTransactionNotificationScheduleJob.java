package com.paymentgateway.scheduler.jobs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.scheduler.commons.ConfigurationProvider;

/**
 * @author Rajit Sharma
 */
public class DebitTransactionNotificationScheduleJob extends QuartzJobBean {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ConfigurationProvider configurationProvider;
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;
	
	private static final Logger logger = LoggerFactory.getLogger(DebitTransactionNotificationScheduleJob.class);
	
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		fetchNotificationDebitTransaction();
		
	}

	private void fetchNotificationDebitTransaction() {

		logger.info("start fetching all transaction notification for a day");
		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String notificationDate = dateFormat.format(new Date()).toString();
			logger.info("Debit Transaction Notification Date = " + notificationDate);

			BasicDBObject queryList = new BasicDBObject();

			queryList.put(FieldType.NOTIFICATION_DATE.getName(), notificationDate);
			queryList.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			queryList.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			allConditionQueryList.add(queryList);
			BasicDBObject finalquery = new BasicDBObject("$and", allConditionQueryList);

			logger.info("Query to get data Notification Date for debit transactions = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(configurationProvider.getMONGO_DB_upiAutoPayCollectionName());
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = collection.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {

				Fields fields = new Fields();
				Document doc = cursor.next();

				fields.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
				Map<String, String> res = transactionControllerServiceProvider.transact(fields,
						configurationProvider.getTransactionNotificationScheduleUrl());
				logger.info("debit API response received from pg ws " + res.get(FieldType.RESPONSE_MESSAGE.getName()));

			}
			cursor.close();
		} catch (Exception ex) {
			logger.info("Exception caught while notification debit transaction "+ex);
		}

	}
}
