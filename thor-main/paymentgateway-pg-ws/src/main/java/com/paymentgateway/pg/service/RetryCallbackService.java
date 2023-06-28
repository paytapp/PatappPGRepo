package com.paymentgateway.pg.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class RetryCallbackService {

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private FieldsDao fieldDao;

	private static Logger logger = LoggerFactory.getLogger(RetryCallbackService.class.getName());

	public String sendCallback(Fields fields ) {

		try {

			String satinPayId = propertiesManager.propertiesMap.get("SATIN_CREDITCARE_PAY_ID");
			
			if (fields.get(FieldType.PAY_ID.getName()).equalsIgnoreCase(satinPayId)) {
				 sendSatinCallback(fields) ;
			}
			
			return "DONE";
		} catch (Exception e) {

			logger.error("Exception in sending callback for retry : " , e);
			return "ERROR";
		}

	}

	@SuppressWarnings("static-access")
	public void sendSatinCallback(Fields fields) {

		JsonObject json = new JsonObject();

		try {


			Date date = new Date();
			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
			String strDate = df.format(date);

			String serviceUrl = propertiesManager.propertiesMap.get("SATIN_CREDITCARE_CALLBACK_URL");
			HttpPost request = new HttpPost(serviceUrl);

			int CONNECTION_TIMEOUT_MS = 30 * 1000; // Timeout in millis.
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
					.setConnectTimeout(CONNECTION_TIMEOUT_MS).setSocketTimeout(CONNECTION_TIMEOUT_MS).build();

			request.setConfig(requestConfig);

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();

			json.addProperty("orderId", fields.get(FieldType.ORDER_ID.getName()));
			json.addProperty("transactionId", fields.get(FieldType.PG_REF_NUM.getName()));
			json.addProperty("amount", fields.get(FieldType.TOTAL_AMOUNT.getName()));
			json.addProperty("status", "SUCCESS");
			json.addProperty("message", "Transaction is successful");

			if (StringUtils.isNotBlank(fields.get(FieldType.RRN.getName()))) {
				json.addProperty("rrnno", fields.get(FieldType.RRN.getName()));
			} else if (StringUtils.isNotBlank(fields.get(FieldType.ACQ_ID.getName()))) {
				json.addProperty("rrnno", fields.get(FieldType.ACQ_ID.getName()));
			} else {
				json.addProperty("rrnno", fields.get(FieldType.PG_REF_NUM.getName()));
			}

			json.addProperty("collectionDateTime", fields.get(FieldType.PG_DATE_TIME.getName()));

			logger.info("Callback Retry Request to Satin  >>> " + json.toString());
			StringEntity params = new StringEntity(json.toString());

			request.addHeader("content-type", "application/json");
			request.addHeader("x-lms-access-key-id",
					propertiesManager.propertiesMap.get("SATIN_CREDITCARE_HEADER_VALUE_1"));
			request.addHeader("x-lms-secret-access-key",
					propertiesManager.propertiesMap.get("SATIN_CREDITCARE_HEADER_VALUE_2"));
			request.addHeader("vendorName", propertiesManager.propertiesMap.get("SATIN_CREDITCARE_HEADER_VALUE_3"));

			request.setEntity(params);

			HttpResponse resp = httpClient.execute(request);
			HttpEntity response = resp.getEntity();
			String responseBody = EntityUtils.toString(response);
			logger.info("Callback Response from Satin  >>> " + responseBody.toString());

			
			// delete transaction from retry callback collection
			if (responseBody.contains("Transaction Synced Successfully")) {
				
				MongoDatabase dbIns = mongoInstance.getDB();

				MongoCollection<Document> coll = dbIns.getCollection(
						PropertiesManager.propertiesMap.get("MONGO_DB_" + Constants.CALLBACK_RETRY_COLLECTION.getValue()));
				
				BasicDBObject query = new BasicDBObject();
				query.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
				
				coll.deleteOne(query);
				logger.info("Deleted retry callback request  >>> " + query.toString());
			}
			
			// Update retry count and try again after 5 minutes
			else {
				
			}
		}

		catch (Exception e) {
			logger.error("Exception in sending retry call back response to Satin for Order Id == "
					+ fields.get(FieldType.ORDER_ID.getName()), e);

			
			// Update retry count and try again after 5 minutes
			/*
			 * logger.info("Updating retry count for Order Id = " +
			 * fields.get(FieldType.ORDER_ID.getName()));
			 * 
			 * MongoDatabase dbIns = mongoInstance.getDB();
			 * 
			 * MongoCollection<Document> coll = dbIns.getCollection(
			 * PropertiesManager.propertiesMap.get("MONGO_DB_" +
			 * Constants.CALLBACK_RETRY_COLLECTION.getValue()));
			 * 
			 * 
			 * Document query = new Document(); query.append(FieldType.ORDER_ID.getName(),
			 * fields.get(FieldType.ORDER_ID.getName())); Document setData = new Document();
			 * setData.append("NUMBER_OF_RETRY","2"); Document update = new Document();
			 * update.append("$set", setData); coll.updateOne(query, update);
			 */


		}

	}

	public String sendCallbackToMerchant(Fields fields ) {

		try {
			boolean isRecordFound = fieldDao.getLatestSaleTransaction(fields);

			if(isRecordFound)
				fieldDao.sendCallback(fields);
			else
				return "NO RECORD FOUND";
			
			return "DONE";
		} catch (Exception e) {

			logger.error("Exception in sending callback for retry : " , e);
			return "ERROR";
		}

	}

}
