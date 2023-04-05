package com.paymentgateway.scheduler.jobs;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bson.Document;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.scheduler.commons.ConfigurationProvider;
import com.paymentgateway.scheduler.commons.PendingStatusEnquiryTransactions;
import com.paymentgateway.scheduler.commons.TransactionDataProvider;
import com.paymentgateway.scheduler.commons.TransactionStatusEnquiry;
import com.paymentgateway.scheduler.core.ServiceControllerProvider;

/**
 * @author Shaiwal
 *
 */

@SuppressWarnings("unused")
public class RetryCallbackJob extends QuartzJobBean {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ConfigurationProvider configurationProvider;

	private List<Document> transactionEnquirySet = new ArrayList<Document>();

	private static final Logger logger = LoggerFactory.getLogger(RetryCallbackJob.class);

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		retryCallback();
	}

	private void retryCallback() {

		try {

			logger.info("Started fetching retry callback data");
			Set<JSONObject> jsonSet = fetchRetryData();

			String bankStatusEnquiryUrl = configurationProvider.getRetryCallbackApiUrl();

			for (JSONObject json : jsonSet) {

				JSONObject newJson = new JSONObject();
				
				newJson.put(FieldType.ORDER_ID.getName(), json.get("orderId"));
				newJson.put(FieldType.PG_REF_NUM.getName(), json.get("transactionId"));
				newJson.put(FieldType.TOTAL_AMOUNT.getName(), json.get("amount"));
				newJson.put(FieldType.RRN.getName(), json.get("rrnno"));
				newJson.put(FieldType.PG_DATE_TIME.getName(), json.get("collectionDateTime"));
				newJson.put(FieldType.PAY_ID.getName(), json.get("PAY_ID"));
				
				logger.info("Sending retry callback for data  " + newJson);
				retryCallbackApi(newJson, bankStatusEnquiryUrl);
			}

		}

		catch (Exception e) {
			logger.error("Exception in bank status enquiry from scheduler", e);
		}

	}

	public Set<JSONObject> fetchRetryData() {

		Set<JSONObject> jsonSet = new HashSet<JSONObject>();

		try {

//			Fetch data from callbackRetry collection for failed call-backs

			BasicDBObject finalquery = new BasicDBObject();

			logger.info("Query to get data for status enquiry = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(configurationProvider.getMONGO_DB_callbackRetryCollection());

			BasicDBObject match = new BasicDBObject("$match", finalquery);

			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = collection.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				if (dbobj.get("ENCDATA") == null ) {
					logger.info("ENCDATA is null ");
					continue;
				}
				
				if (dbobj.get("PAY_ID") == null ) {
					logger.info("PAY_ID is null ");
					continue;
				}
				
				String encData = dbobj.get("ENCDATA").toString();
				String payId = dbobj.get("PAY_ID").toString();
				
				if (!encData.contains("orderId")) {
					
					logger.info("orderId is null ");
					continue;
				}
				
				if (!encData.contains("transactionId")) {
								
					logger.info("transactionId is null ");
					continue;
				}
				
				if (!encData.contains("amount")) {
					logger.info("amount is null ");
					continue;
				}
				
				if (!encData.contains("rrnno")) {
					logger.info("rrnno is null ");
					continue;
				}
				
				if (!encData.contains("collectionDateTime")) {
					logger.info("collectionDateTime is null ");
					continue;
				}
				
				JSONObject newJson = new JSONObject(encData);
				newJson.put(FieldType.PAY_ID.getName(), payId);
				jsonSet.add(newJson);

			}

			return jsonSet;

		}

		catch (Exception e) {
			logger.error("Exception in getting data for retry callback enquiry", e);
		}
		return jsonSet;

	}

	public void retryCallbackApi(JSONObject data, String retryCallbackUrl) throws SystemException {

		try {
			String serviceUrl = retryCallbackUrl;

			HttpPost request = new HttpPost(serviceUrl);
			RequestConfig config = RequestConfig.custom().setConnectTimeout(3600000)
					.setConnectionRequestTimeout(3600000).setSocketTimeout(3600000).build();
			request.setConfig(config);

			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

			try {
				StringEntity params = new StringEntity(data.toString());

				request.addHeader("content-type", "application/json");
				request.setEntity(params);

				HttpResponse resp = httpClient.execute(request);
				HttpEntity response = resp.getEntity();
			} catch (Exception e) {
				logger.error("Status Enquiry Expired " , e);
			}

		} catch (Exception exception) {
			logger.error("Error communicating with retry callback API ", exception);
		}
	}

}