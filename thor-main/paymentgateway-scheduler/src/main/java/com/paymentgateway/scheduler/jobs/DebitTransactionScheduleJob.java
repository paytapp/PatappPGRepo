package com.paymentgateway.scheduler.jobs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import com.paymentgateway.scheduler.commons.ConfigurationProvider;

/**
 * @author Rajit Sharma
 */

public class DebitTransactionScheduleJob extends QuartzJobBean {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ConfigurationProvider configurationProvider;
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;
	
	private static final Logger logger = LoggerFactory.getLogger(DebitTransactionScheduleJob.class);
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		fetchDebitTransaction();
	}
	
	private void fetchDebitTransaction() {
		
		try {
			logger.info("start fetching all scheduled transaction for a day");
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String dueDate = dateFormat.format(new Date()).toString();
			logger.info("Scheduler Transaction Start Date = " +dueDate );
			
			BasicDBObject queryList = new BasicDBObject();

			queryList.put(FieldType.DUE_DATE.getName(), dueDate);
			queryList.put(FieldType.TXNTYPE.getName(), "Sale");
			queryList.put(FieldType.STATUS.getName(), "Pending");
			
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			
			allConditionQueryList.add(queryList);
			BasicDBObject finalquery = new BasicDBObject("$and", allConditionQueryList);
			
			logger.info("Query to get data for Due debit transactions = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(configurationProvider.getMONGO_DB_eNachCollectionName());
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = collection.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				
				Fields fields = new Fields();
				Document doc = cursor.next();

				fields.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
				fields.put(FieldType.MONTHLY_AMOUNT.getName(), doc.getString(FieldType.MONTHLY_AMOUNT.getName()));
				fields.put(FieldType.TXNTYPE.getName(), "Sale");
				fields.put(FieldType.DATEFROM.getName(), doc.getString(FieldType.DATEFROM.getName()));
				fields.put(FieldType.DATETO.getName(), doc.getString(FieldType.DATETO.getName()));
				fields.put(FieldType.CURRENCY.getName(), doc.getString(FieldType.CURRENCY.getName()));
				fields.put(FieldType.AMOUNT.getName(), doc.getString(FieldType.AMOUNT.getName()));
				fields.put(FieldType.MONTHLY_AMOUNT.getName(), doc.getString(FieldType.MONTHLY_AMOUNT.getName()));
				fields.put(FieldType.TOTAL_AMOUNT.getName(), doc.getString(FieldType.TOTAL_AMOUNT.getName()));
				fields.put(FieldType.PAYMENT_TYPE.getName(), doc.getString(FieldType.PAYMENT_TYPE.getName()));
				fields.put(FieldType.CUST_PHONE.getName(), doc.getString(FieldType.CUST_PHONE.getName()));
				fields.put(FieldType.CUST_EMAIL.getName(), doc.getString(FieldType.CUST_EMAIL.getName()));
				fields.put(FieldType.AMOUNT_TYPE.getName(), doc.getString(FieldType.AMOUNT_TYPE.getName()));
				fields.put(FieldType.FREQUENCY.getName(), doc.getString(FieldType.FREQUENCY.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), doc.getString(FieldType.ORIG_TXN_ID.getName()));
				fields.put(FieldType.TENURE.getName(), doc.getString(FieldType.TENURE.getName()));
				fields.put(FieldType.BANK_NAME.getName(), doc.getString(FieldType.BANK_NAME.getName()));
				fields.put(FieldType.UMRN_NUMBER.getName(), doc.getString(FieldType.UMRN_NUMBER.getName()));
				fields.put("MANDATE_REGISTRATION_ID", doc.getString("MANDATE_REGISTRATION_ID"));
				fields.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
				fields.put("COM_AMT", doc.getString("COM_AMT"));
				fields.put(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName()));
				
				if(doc.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), doc.getString(FieldType.SUB_MERCHANT_ID.getName()));
				}
				
				if(doc.containsKey(FieldType.RESELLER_ID.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_ID.getName()))) {
					fields.put(FieldType.RESELLER_ID.getName(), doc.getString(FieldType.RESELLER_ID.getName()));
				}
				
				if (doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("NB")
						|| doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("DC")) {

					fields.put(FieldType.ACCOUNT_NO.toString(), doc.getString(FieldType.ACCOUNT_NO.toString()));
					fields.put(FieldType.IFSC_CODE.getName(), doc.getString(FieldType.IFSC_CODE.getName()));
					fields.put(FieldType.ACCOUNT_HOLDER_NAME.getName(),
							doc.getString(FieldType.ACCOUNT_HOLDER_NAME.getName()));
					fields.put(FieldType.ACCOUNT_TYPE.getName(), doc.getString(FieldType.ACCOUNT_TYPE.getName()));
				}
				
				Map<String, String> res = transactionControllerServiceProvider.transact(fields, configurationProvider.getIciciEnachTransactionScheduleUrl());
				logger.info("debit API response received from pg ws "+res.get(FieldType.RESPONSE_MESSAGE.getName()));
			}
		} catch (Exception ex) {
			logger.info("Exception caught in fetchDueTransaction while schedule transaction "+ex);
		}
		
	}
	
	

}
