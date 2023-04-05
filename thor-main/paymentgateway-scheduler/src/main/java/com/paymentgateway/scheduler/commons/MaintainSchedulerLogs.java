/**
 * It will make log of jobs run by Scheduler
 */
package com.paymentgateway.scheduler.commons;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.SchedulerJobs;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.scheduler.core.TaskManager;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class MaintainSchedulerLogs implements TaskManager {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ConfigurationProvider configurationProvider;

	private static final Logger logger = LoggerFactory.getLogger(MaintainSchedulerLogs.class);

	@Override
	public void taskLogger(SchedulerJobs job, String request, String response, Boolean jobStatus) {
		try {
			logger.info("Logging: " + job.getJobType() + " JOB with job time: " + job.getJobTime() + " into DB");
			Document executionlog = new Document();
			executionlog.put("_id", TransactionManager.getNewTransactionId());
			executionlog.put("JOB_TYPE", job.getJobType());
			executionlog.put("JOB_TIME", job.getJobTime());
			if (jobStatus) {
				executionlog.put("JOB_STATUS", "SUCCESS");
			} else {
				executionlog.put("JOB_STATUS", "FAILURE");
			}
			executionlog.put("JOB_DETAILS", job.getJobDetails());
			executionlog.put("NEXT_EXECUTION", job.getJobFrequency());
			if (StringUtils.isNotBlank(request)) {
				executionlog.put("REQUEST", request);
			} else {
				executionlog.put("REQUEST", "NA");
			}
			if (StringUtils.isNotBlank(response)) {
				executionlog.put("RESPONSE", response);
			} else {
				executionlog.put("RESPONSE", "NA");
			}
			executionlog.put("LOG_TIME", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(configurationProvider.getMONGO_DB_schedulerLogsCollection());
			collection.insertOne(executionlog);
		} catch (Exception e) {
			logger.error("Exception caught while inserting scheduler logs : ", e);
		}
	}
}
