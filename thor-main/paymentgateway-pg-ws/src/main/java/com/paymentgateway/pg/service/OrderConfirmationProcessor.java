package com.paymentgateway.pg.service;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.Processor;

@Service
public class OrderConfirmationProcessor {

	private static Logger logger = LoggerFactory.getLogger(OrderConfirmationProcessor.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	PropertiesManager propertiesManager;

	@SuppressWarnings("incomplete-switch")
	public void process(Fields fields) throws SystemException {

		String orderId = fields.get(FieldType.ORDER_ID.getName());

		if (StringUtils.isNotBlank(orderId)) {

			// Call Khadi API for Order Confirmation

			try {

				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns.getCollection(
						PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				
				MongoCollection<Document> prodDescColl = dbIns.getCollection(
						PropertiesManager.propertiesMap.get(prefix + Constants.PROD_DESC_COLLECTION.getValue()));

				String bearerToken = propertiesManager.propertiesMap.get("KHADI_BEARER_TOKEN");
				JSONObject json = new JSONObject();
				json.put("order_id", orderId);
				String url = propertiesManager.propertiesMap.get("KHADI_ORDER_CONFIRM_API_URL");

				CloseableHttpClient httpClient = HttpClientBuilder.create().build();
				HttpPost request = new HttpPost(url);

				StringEntity params = new StringEntity(json.toString());
				request.addHeader("content-type", "application/json");
				request.addHeader("Authorization", "Bearer " + bearerToken);
				request.setEntity(params);
				HttpResponse resp = httpClient.execute(request);

				logger.info("Order Response received with code = " + resp.getStatusLine());
				
				
				  if (!resp.getStatusLine().toString().contains("200")) {
					  logger.info("Network error while confirming order");
				  return; }
				 
				
				String responseBody = EntityUtils.toString(resp.getEntity());
				logger.info("Order Response received = " + responseBody);

				JSONObject respJson = new JSONObject(responseBody);

				if (responseBody.contains("success")) {

					boolean success = respJson.getBoolean("success");
					if (success) {

						if (responseBody.contains("data")) {
							if (responseBody.contains("no record found")) {

								logger.info("No Records found , marking as not delivered = " + responseBody);

								
								// Update transaction
								
								Document query = new Document();
								query.append(FieldType.ORDER_ID.getName(), orderId);

								Document setData = new Document();
								setData.append(FieldType.DELIVERY_CODE.getName(), "100");
								setData.append(FieldType.DELIVERY_STATUS.getName(), "NOT_DELIVERED");

								Document update = new Document();
								update.append("$set", setData);
								coll.updateMany(query, update);
								
								// Update Product Description 
								prodDescColl.updateMany(query, update);

							} else {

								// Update transaction
								JSONArray data = respJson.getJSONArray("data");
								JSONObject datObject = data.getJSONObject(0);
								logger.info("Order confirmation found for , order id = "
										+ datObject.get("order_id").toString());

								Document query = new Document();
								query.append(FieldType.ORDER_ID.getName(), orderId);

								Document setData = new Document();
								setData.append(FieldType.DELIVERY_CODE.getName(), "000");
								setData.append(FieldType.DELIVERY_STATUS.getName(), "DELIVERED");

								Document update = new Document();
								update.append("$set", setData);
								coll.updateMany(query, update);
								
								// Update Product Description 
								prodDescColl.updateMany(query, update);
								
							}

						}

					}

				}
			}

			catch (Exception e) {
				logger.error("Exception in updating delivery status for khadi ", e);
			}

		}
	}
}
