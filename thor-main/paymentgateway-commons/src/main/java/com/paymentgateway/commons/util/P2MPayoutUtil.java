package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;

@Service
public class P2MPayoutUtil {

	private static Logger logger = LoggerFactory.getLogger(P2MPayoutUtil.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;

	public void p2mPayout(Fields fields) {

		try {

			String responseBody = "";
			Map<String, String> resMap = new HashMap<String, String>();

			JSONObject json = new JSONObject();

			logger.info("Generating request for P2M Payouts");
			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.MERCHANT_P2M_COLLECTION.getValue()));

			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),
					fields.get(FieldType.PAY_ID.getName()));
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), "ACTIVE");

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(payIdQuery);
			condList.add(statusQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", condList);
			logger.info("Final Query to get Active VPA for Merchant >> " + finalQuery);

			MongoCursor<Document> cursor = collection.find(finalQuery).iterator();
			while (cursor.hasNext()) {
				Document documentObj = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				json.put(FieldType.PAY_ID.getName(), documentObj.get(FieldType.PAY_ID.getName()).toString());
				json.put(FieldType.ORDER_ID.getName(), TransactionManager.getNewTransactionId());
				json.put(FieldType.PAYER_NAME.getName(), documentObj.get(FieldType.PAYEE_NAME.getName()).toString());
				json.put(FieldType.PAYER_ADDRESS.getName(),
						documentObj.get(FieldType.PAYEE_ADDRESS.getName()).toString());
				json.put(FieldType.CURRENCY_CODE.getName(), "356");
				json.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()), "356"));
				json.put(FieldType.PHONE_NO.getName(), documentObj.get(FieldType.CUST_PHONE.getName()).toString());

				
				fields.put(FieldType.PAYER_NAME.getName(),fields.get(FieldType.PAYER_ADDRESS.getName()));
				fields.put(FieldType.PAYEE_NAME.getName(),documentObj.get(FieldType.PAYEE_NAME.getName()).toString());
				fields.put(FieldType.CUST_PHONE.getName(),documentObj.get(FieldType.CUST_PHONE.getName()).toString());
				fields.put(FieldType.PAYEE_ADDRESS.getName(),documentObj.get(FieldType.PAYEE_ADDRESS.getName()).toString());
				
				
				Fields hashFields = new Fields();

				hashFields.put(FieldType.PAY_ID.getName(), json.get(FieldType.PAY_ID.getName()).toString());
				hashFields.put(FieldType.ORDER_ID.getName(), json.get(FieldType.ORDER_ID.getName()).toString());
				hashFields.put(FieldType.PAYER_NAME.getName(),json.get(FieldType.PAYER_NAME.getName()).toString());
				hashFields.put(FieldType.PAYER_ADDRESS.getName(),json.get(FieldType.PAYER_ADDRESS.getName()).toString());
				hashFields.put(FieldType.CURRENCY_CODE.getName(), "356");
				hashFields.put(FieldType.AMOUNT.getName(), fields.get(FieldType.TOTAL_AMOUNT.getName()));
				hashFields.put(FieldType.PHONE_NO.getName(),json.get(FieldType.PHONE_NO.getName()).toString());

				String hash = Hasher.getHash(hashFields);

				json.put(FieldType.HASH.getName(), hash);

				break;
			}
			cursor.close();

			String serviceUrl = propertiesManager.propertiesMap.get("TransactionWSUpiTransferURL");
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse resp = httpClient.execute(request);
			responseBody = EntityUtils.toString(resp.getEntity());
			final ObjectMapper mapper = new ObjectMapper();
			final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
			resMap = mapper.readValue(responseBody, type);

			logger.info("Response for P2M payout ");

			for (Map.Entry<String, String> entry : resMap.entrySet()) {
				logger.info(entry.getKey() + " = " + entry.getValue());
			}
			
			// Update In DB -- Collection - merchantP2MPayout

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.MERCHANT_P2M_PAYOUT.getValue()));
			
			Document doc = new Document();
			
			doc.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			doc.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
			doc.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()), "356"));
			doc.put(FieldType.CREATE_DATE.getName(), dateNow);
			doc.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));
			doc.put(FieldType.CUST_NAME.getName(), fields.get(FieldType.CUST_NAME.getName()));
			doc.put(FieldType.CUST_PHONE.getName(), fields.get(FieldType.CUST_PHONE.getName()));
			doc.put(FieldType.ACQ_ID.getName(), fields.get(FieldType.ACQ_ID.getName()));
			doc.put(FieldType.RRN.getName(), fields.get(FieldType.RRN.getName()));
			doc.put(FieldType.PAYER_NAME.getName(), fields.get(FieldType.PAYER_NAME.getName()));
			doc.put(FieldType.PAYEE_NAME.getName(), fields.get(FieldType.PAYEE_NAME.getName()));
			doc.put(FieldType.CUST_PHONE.getName(), fields.get(FieldType.CUST_PHONE.getName()));
			doc.put(FieldType.PAYEE_ADDRESS.getName(),fields.get(FieldType.PAYEE_ADDRESS.getName()));

			doc.put(FieldType.STATUS.getName(),resMap.get(FieldType.STATUS.getName()));
			doc.put(FieldType.RESPONSE_CODE.getName(),resMap.get(FieldType.RESPONSE_CODE.getName()));
			doc.put(FieldType.RESPONSE_MESSAGE.getName(),resMap.get(FieldType.RESPONSE_MESSAGE.getName()));
			
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				doc = dataEncDecTool.encryptDocument(doc);
			}
			
			coll.insertOne(doc);
			
		}

		catch (Exception e) {

			logger.error("Exception in sending p2m payout request", e);
		}

	}

	public Map<String, String> updateMercVPAForP2mPayout(Fields fields) {

		Map<String, String> respMap = new HashMap<String, String>();

		try {

			// Validate Hash
			String merchantHash = fields.get(FieldType.HASH.getName());
			fields.remove(FieldType.HASH.getName());

			String calculatedHash = Hasher.getHash(fields);

			if (!merchantHash.equalsIgnoreCase(calculatedHash)) {

				logger.info("HASH Mismatch in P2M VPA Update Request");
				logger.info("Merchant Hash  >>>  " + merchantHash);
				logger.info("Calculated Hash  >>>  " + calculatedHash);

				respMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
				respMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_HASH.getResponseMessage());

				return respMap;
			}

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.MERCHANT_P2M_COLLECTION.getValue()));

			// First Set the current VPA as InActive

			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),
					fields.get(FieldType.PAY_ID.getName()));
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), "ACTIVE");

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(payIdQuery);
			condList.add(statusQuery);

			BasicDBObject findquery = new BasicDBObject("$and", condList);

			long count = collection.count(findquery);

			if (count > 0) {

				Document setData = new Document();

				setData.put("STATUS", "INACTIVE");
				Document latestNewDoc = new Document("$set", setData);

				collection.updateOne(findquery, latestNewDoc);

			}

			// Now Add a new VPA for Merchant P2M Payout

			Document newDoc = new Document();
			newDoc.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			newDoc.put(FieldType.PAYEE_ADDRESS.getName(), fields.get(FieldType.PAYEE_ADDRESS.getName()));
			newDoc.put(FieldType.CUST_PHONE.getName(), fields.get(FieldType.CUST_PHONE.getName()));
			newDoc.put(FieldType.PAYEE_NAME.getName(), fields.get(FieldType.PAYEE_NAME.getName()));
			newDoc.put(FieldType.STATUS.getName(), "ACTIVE");
			
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				newDoc = dataEncDecTool.encryptDocument(newDoc);
			}

			logger.info("Adding new entry for P2M Payout " + newDoc);
			collection.insertOne(newDoc);

			respMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
			respMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

			return respMap;
		}

		catch (Exception e) {
			logger.error("Exception in updating merchant VPA for p2m payout ", e);
			return respMap;
		}

	}
}
