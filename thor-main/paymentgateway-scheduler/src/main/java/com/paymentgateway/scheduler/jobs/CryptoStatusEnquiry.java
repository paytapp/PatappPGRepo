package com.paymentgateway.scheduler.jobs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.scheduler.commons.ConfigurationProvider;
import com.paymentgateway.scheduler.core.ServiceControllerProvider;

public class CryptoStatusEnquiry extends QuartzJobBean {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ConfigurationProvider configurationProvider;
	
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;
	
	@Autowired
	private ServiceControllerProvider serviceControllerProvider;
	
	private static final Logger logger = LoggerFactory.getLogger(CryptoStatusEnquiry.class);
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		cryptoStatusEnquiryCheck();
		
	}

	private void cryptoStatusEnquiryCheck() {
		
		try {
			
			logger.info("inside CryptoStatusEnquiryJob for status enquiry ");
			
			SimpleDateFormat timeFormate = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
			 
			int addMinuteTime = 1;
			Calendar startTime = Calendar.getInstance();
			startTime.setTime(new Date());
			startTime.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE) - addMinuteTime);
			startTime.set(Calendar.SECOND, 0);
			
			Calendar endTime = Calendar.getInstance();
			endTime.set(Calendar.SECOND, 59);
			
			//String st =  timeFormate.format(startTime.getTimeInMillis());
			//String et =  timeFormate.format(endTime.getTimeInMillis());
			//for(int i=0; i<status.length; i++) {
			//queryList.put(FieldType.STATUS.getName(),StatusType.PROCESSING.getName());
			//queryList.put(FieldType.STATUS.getName(),StatusType.TIMEOUT.getName());
		//}
		
			
			String [] status =configurationProvider.getCRYPTO_STATUS().split(",");
			BasicDBObject queryList = new BasicDBObject();
			BasicDBObject dateTimeQuery = new BasicDBObject();
			
			//dateTimeQuery.put(FieldType.CREATE_DATE.getName(),BasicDBObjectBuilder.start("$lte", st).get());
			
			
			queryList.put(FieldType.PAYMENT_TYPE.getName(), "CR");
			queryList.put(FieldType.MOP_TYPE.getName(), "CR");

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			
			List<BasicDBObject> statusConditionLst1 = new ArrayList<BasicDBObject>();
			
			BasicDBObject statusQuery1 = new BasicDBObject();
			statusConditionLst1.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PROCESSING.getName()));
			statusConditionLst1.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName()));
			statusQuery1.append("$or", statusConditionLst1);
			allConditionQueryList.add(statusQuery1);	
		
			allConditionQueryList.add(queryList);
			BasicDBObject finalquery = new BasicDBObject("$and", allConditionQueryList);
			logger.info("Query to get data for Crypto Status Enquiry = " + finalquery);
			
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(configurationProvider.getMONGO_DB_collectionName());
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = collection.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Fields fields = new Fields();
				Document doc = cursor.next();
				logger.info(FieldType.ORDER_ID.getName()+"-------------"+ doc.getString(FieldType.ORDER_ID.getName()));
				
				List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
				List<BasicDBObject> statusConditionLst = new ArrayList<BasicDBObject>();
				condList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName())));
				BasicDBObject statusQuery = new BasicDBObject();
				statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
				statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
				statusQuery.append("$or", statusConditionLst);
				condList.add(statusQuery);
				BasicDBObject saleQuery = new BasicDBObject("$and", condList);
				boolean flag=true;
				MongoDatabase dbIns1 = mongoInstance.getDB();
				MongoCollection<Document> collection1 = dbIns1.getCollection(configurationProvider.getMONGO_DB_collectionName());
				MongoCursor<Document> cursor1 = collection1.find(saleQuery).iterator();
				
				while (cursor1.hasNext()) {
						Document documentObj = (Document) cursor1.next();
						if(documentObj!=null) {
							flag=false;
							break;
						}
				}	
				
			if(flag) {
					fields.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
					fields.put(FieldType.ACQ_ID.getName(), doc.getString(FieldType.ACQ_ID.getName()));
					fields.put(FieldType.RRN.getName(), doc.getString(FieldType.RRN.getName()));
					fields.put(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName()));
					fields.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
					fields.put(FieldType.INVOICE_ID.getName(), doc.getString(FieldType.INVOICE_ID.getName()));
					fields.put(FieldType.CURRENCY_CODE.getName(), doc.getString(FieldType.CURRENCY_CODE.getName()));
					fields.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
					
					Map<String, String> res = transactionControllerServiceProvider.transact(fields, configurationProvider.getCryptoTransactionStatusEnquiryUrl());
				}
				
			}
		} catch(Exception ex) {
			logger.info("Exception caught while status enquiry for Crypto transaction by scheduler ",ex);
		}
		
		
		
		
	}
}

