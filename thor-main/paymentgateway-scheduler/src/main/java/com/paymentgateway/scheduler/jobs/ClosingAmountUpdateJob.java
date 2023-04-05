package com.paymentgateway.scheduler.jobs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

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
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.scheduler.commons.ConfigurationProvider;

/**
 * @author Shaiwal
 *
 */

@SuppressWarnings("unused")
public class ClosingAmountUpdateJob extends QuartzJobBean {

	@Autowired
	private ConfigurationProvider configurationProvider;

	private static final Logger logger = LoggerFactory.getLogger(ClosingAmountUpdateJob.class);

	@Autowired
	private MongoInstance mongoInstance;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		updateClosingAmount(context);

	}

	protected void updateClosingAmount(JobExecutionContext context) throws JobExecutionException {

		try {
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		
		String todaysDateIndex = dateFormat.format(cal.getTime());
		String todaysDate = dateFormat2.format(cal.getTime());
		cal.add(Calendar.DATE, -1);
		String yesterdayDate = dateFormat2.format(cal.getTime());
		String yesterdayDateIndex = dateFormat.format(cal.getTime());
		
		logger.info("Todays Date = " + todaysDate);
		logger.info("Yesterdays Date = " + yesterdayDate);
		
		BasicDBObject finalquery = new BasicDBObject("DATE_INDEX", yesterdayDateIndex);
		BasicDBObject finalqueryCurrent = new BasicDBObject("DATE_INDEX", todaysDateIndex);
		
		logger.info("Query to get data for status enquiry = " + finalquery);
		logger.info("Query to get data for status enquiry = " + finalqueryCurrent);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> collection = dbIns.getCollection(configurationProvider.getCLOSING_AMOUNT_COLLECTION());
		int totalCount=0;
		totalCount = (int) collection.count(finalqueryCurrent);
		if(totalCount == 0) {
		BasicDBObject match = new BasicDBObject("$match", finalquery);

		List<BasicDBObject> pipeline = Arrays.asList(match);
		AggregateIterable<Document> output = collection.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
		while (cursor.hasNext()) {
			Document dbobj = cursor.next();
			logger.info("Orginal entry for closing collection by scheduler for virtual account code = " + dbobj.get("VIRTUAL_AC_CODE").toString() + " , " + dbobj);
			String amount = dbobj.get("AMOUNT").toString();
			
			Document doc = new Document();
			doc.put("CREATE_DATE", todaysDate);
			doc.put("UPDATE_DATE", todaysDate);
			doc.put("CLOSING_DATE", todaysDate);
			doc.put("VIRTUAL_AC_CODE", dbobj.get("VIRTUAL_AC_CODE").toString());
			doc.put("PAY_ID", dbobj.get("PAY_ID").toString());
			if(dbobj.containsKey("SUB_MERCHANT_ID")){
			doc.put("SUB_MERCHANT_ID", dbobj.get("SUB_MERCHANT_ID").toString());
		    }
			
			if(dbobj.containsKey("RESELLER_ID") && dbobj.get("RESELLER_ID") != null){
				doc.put("RESELLER_ID", dbobj.get("RESELLER_ID").toString());
			    }
			doc.put("DEBIT_AMOUNT", "0.00");
			doc.put("CREDIT_AMOUNT", "0.00");
			doc.put("AMOUNT",amount);
			doc.put("OPENING_AMOUNT", amount);
			doc.put("DATE_INDEX", todaysDateIndex);
			doc.put(FieldType.ACQUIRER_NAME.getName(), dbobj.get(FieldType.ACQUIRER_NAME.getName()));
			
			collection.insertOne(doc);
			logger.info("Insertion in closing collection by scheduler at 12:00 for virtual account code = " + dbobj.get("VIRTUAL_AC_CODE").toString() + " , " + dbobj);
			logger.info("Updated closing amount for Pay Id = " + dbobj.get("PAY_ID").toString());
		}
		}
		
	}catch(Exception e) {
		logger.info("Exception e : ", e);
	}
		
	}

}