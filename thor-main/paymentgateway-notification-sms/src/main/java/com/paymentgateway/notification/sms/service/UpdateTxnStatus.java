package com.paymentgateway.notification.sms.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class UpdateTxnStatus {

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;

	private static final String prefix = "MONGO_DB_";

	private final Logger logger = LoggerFactory.getLogger(UpdateTxnStatus.class.getName());

	public Map<String, String> updateTxnStatus(Map<String, String> reqmap) {
		int failCount = 0;
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			String dateFrom = "";
			String dateTo = "";

			if (reqmap.containsKey("DATE_FROM")) {
				dateFrom = reqmap.get("DATE_FROM");
			} else {
				responseMap.put("STATUS", "DATE_FROM is missing from the request");
			}

			if (reqmap.containsKey("DATE_TO")) {
				dateTo = reqmap.get("DATE_TO");
			} else {
				responseMap.put("STATUS", "DATE_TO is missing from the request");
			}
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());

			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));

			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PENDING.getName()));

			allParamQuery = new BasicDBObject("$or", paramConditionLst);

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			fianlList.add(allParamQuery);
			fianlList.add(dateQuery);

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("INSERTION_DATE", -1));
			BasicDBObject limit = new BasicDBObject("$limit", 1);

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, limit);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			List<Document> documentList = new ArrayList<Document>();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				documentList.add(dbobj);
			}
			cursor.close();
			logger.info("total transaction status to be updated: " + documentList.size());

			for (Document doc : documentList) {
				try {
					insertIntoDatabase(doc);
				} catch (Exception e) {
					logger.error("Exception, " + e);
					failCount++;
				}
			}
		} catch (Exception e) {
			logger.error("Exception, " + e);
			failCount++;
		}
		responseMap.put("STATUS", "000");
		responseMap.put("TOTAL_FAILED_UPDATION", String.valueOf(failCount));
		return responseMap;
	}

	public void insertIntoDatabase(Document document) throws Exception {
		try {
			BasicDBObject newFieldsObj = new BasicDBObject();

			Map<String, String> objectMap = getMapFromDoc(document);

			for (Entry<String, String> entry : objectMap.entrySet()) {
				newFieldsObj.put(entry.getKey(), entry.getValue());
			}

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			String txnId = TransactionManager.getNewTransactionId();

			if (StringUtils.isBlank(newFieldsObj.getString(FieldType.PG_REF_NUM.getName()))
					|| "0".equalsIgnoreCase(newFieldsObj.getString(FieldType.PG_REF_NUM.getName()))) {
				newFieldsObj.put(FieldType.PG_REF_NUM.getName(), txnId);
			}

			newFieldsObj.put("_id", txnId);
			newFieldsObj.put(FieldType.TXN_ID.getName(), txnId);

			newFieldsObj.put(FieldType.CREATE_DATE.getName(), dateNow);
			newFieldsObj.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));
			newFieldsObj.put(FieldType.UPDATE_DATE.getName(), dateNow);
			newFieldsObj.put(FieldType.INSERTION_DATE.getName(), dNow);

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			Document doc = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			//collection.insertOne(doc);

			MongoCollection<Document> collection1 = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			Document doc1 = new Document(newFieldsObj);
			
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection1.insertOne(dataEncDecTool.encryptDocument(doc1));
			} else {
				collection1.insertOne(doc1);
			}
			//collection1.insertOne(doc1);

		} catch (Exception exception) {
			throw new SystemException(ErrorType.DATABASE_ERROR, exception,
					"Unable to insert virtual account transaction");
		}
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	private Map<String, String> getMapFromDoc(Document doc) {
		try {
			doc.remove(FieldType.INSERTION_DATE.getName());
			JsonWriterSettings writerSettings = new JsonWriterSettings(JsonMode.SHELL, true);
			JSONObject data = new JSONObject(doc.toJson(writerSettings));
			Map<String, String> map = new ObjectMapper().readValue(data.toString(), HashMap.class);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, String> updateCaptureToSettled(Map<String, String> reqmap)  {

		Map<String, String> responseMap = new HashMap<String, String>();
		
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> txnStatusColl = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
		
		MongoCollection<Document> txnColl = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		
		try {

			String dateFrom = "";
			String dateTo = "";

			if (reqmap.containsKey("DATE_FROM")) {
				dateFrom = reqmap.get("DATE_FROM");
			} else {
				responseMap.put("STATUS", "DATE_FROM is missing from the request");
			}

			if (reqmap.containsKey("DATE_TO")) {
				dateTo = reqmap.get("DATE_TO");
			} else {
				responseMap.put("STATUS", "DATE_TO is missing from the request");
			}

			BasicDBObject dateQuery = new BasicDBObject();
			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());

			BasicDBObject statusQuery = new BasicDBObject(FieldType.ALIAS_STATUS.getName(),
					StatusType.CAPTURED.getName());
			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(statusQuery);
			finalList.add(dateQuery);

			BasicDBObject finalquery = new BasicDBObject("$and", finalList);

			BasicDBObject match = new BasicDBObject("$match", finalquery);

			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.TXNTYPE.toString(), 1);
			projectElement.put(FieldType.ORDER_ID.toString(), 1);
			projectElement.put(FieldType.PG_REF_NUM.toString(), 1);

			BasicDBObject project = new BasicDBObject("$project", projectElement);

			List<BasicDBObject> pipeline = Arrays.asList(match, project);

			AggregateIterable<Document> output = txnStatusColl.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				
				logger.info("Updating Order Id >> " + dbobj.get(FieldType.ORDER_ID.getName()).toString());

				List<BasicDBObject> settleList = new ArrayList<BasicDBObject>();
				settleList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				if (dbobj.get(FieldType.TXNTYPE.getName()).toString().equalsIgnoreCase("SALE")) {
					settleList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
				} else {
					settleList
							.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
				}

				settleList.add(new BasicDBObject(FieldType.ORDER_ID.getName(),
						dbobj.get(FieldType.ORDER_ID.getName()).toString()));
				settleList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(),
						dbobj.get(FieldType.PG_REF_NUM.getName()).toString()));

				BasicDBObject finalSettleQuery = new BasicDBObject("$and", settleList);
				
				Iterable<Document>  itr = txnColl.find(finalSettleQuery);
				Iterator<Document> cursorSettle = itr.iterator();
				
				while(cursorSettle.hasNext()) {
					
					Document dbobjSettle = cursorSettle.next();
					
					List<BasicDBObject> settleConList = new ArrayList<BasicDBObject>();
					
					settleConList.add(new BasicDBObject(FieldType.ORDER_ID.getName(),dbobjSettle.get(FieldType.ORDER_ID.getName()).toString()));
					settleConList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(),dbobjSettle.get(FieldType.PG_REF_NUM.getName()).toString()));
					if (dbobjSettle.get(FieldType.TXNTYPE.getName()).toString().equalsIgnoreCase(TransactionType.RECO.getName())) {
						settleConList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),TransactionType.SALE.getName()));
					}
					else {
						settleConList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),TransactionType.REFUND.getName()));
					}
					
					BasicDBObject searchQuery = new BasicDBObject("$and", settleConList);
					BasicDBObject updateFields = new BasicDBObject();
					
					updateFields.put(FieldType.SETTLEMENT_DATE.getName(), dbobjSettle.get(FieldType.CREATE_DATE.getName()));
					updateFields.put(FieldType.SETTLEMENT_DATE_INDEX.getName(), dbobjSettle.get(FieldType.DATE_INDEX.getName()));
					updateFields.put(FieldType.SETTLEMENT_FLAG.getName(), "Y");
					updateFields.put(FieldType.ALIAS_STATUS.getName(), StatusType.SETTLED.getName());
					updateFields.put(FieldType.STATUS.getName(), StatusType.SETTLED.getName());
					updateFields.put(FieldType.TXNTYPE.getName(), dbobjSettle.get(FieldType.TXNTYPE.getName()).toString());
					
					txnStatusColl.updateOne(searchQuery, new BasicDBObject("$set", updateFields));
					
					break;
				}

			}
			cursor.close();
			responseMap.put("STATUS", "SUCCESS");
			return responseMap;
		} catch (Exception exception) {
			logger.error("Exception in updating settled entry for captured transactions : ", exception);
			responseMap.put("STATUS", "FAILED");
			return responseMap;
		}
	}
}