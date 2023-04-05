package com.paymentgateway.pg.core.fraudPrevention.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class FraudTxnDao {
	private static Logger logger = LoggerFactory.getLogger(FraudTxnDao.class.getName());

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
    private DataEncDecTool dataEncDecTool;
	
	@Autowired
    private PropertiesManager propertiesManager;
	
	private static final String prefix = "MONGO_DB_";
	// running
	@SuppressWarnings("unchecked")
	public long getPerCardTransactions(String payId, String cardHash) throws SystemException {
		long noOfTransactions = 0;

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
			obj.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			obj.add(new BasicDBObject(FieldType.H_CARD_NUMBER.getName(), cardHash));
			BasicDBObject query1 = new BasicDBObject("$and", obj);
			
			List<BasicDBObject> saleConditionList = new ArrayList<BasicDBObject>();
			saleConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),  TransactionType.SALE.getName()));
			saleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(),StatusType.CAPTURED.getName()));
			
			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleConditionList);
			List<BasicDBObject> authoriseConditionList = new ArrayList<BasicDBObject>();
			authoriseConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),TransactionType.AUTHORISE.getName()));
			authoriseConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.APPROVED.getName()));
			
			BasicDBObject authoriseConditionQuery = new BasicDBObject("$and", saleConditionList);
			
			List<BasicDBObject> bothConditionList = new ArrayList<BasicDBObject>();
			bothConditionList.add(saleConditionQuery);
			bothConditionList.add(authoriseConditionQuery);
			BasicDBObject addConditionListQuery = new BasicDBObject("$or", bothConditionList);
			List<BasicDBObject> obj3 = new ArrayList<BasicDBObject>();
			obj3.add(query1);
			obj3.add(addConditionListQuery);

			BasicDBObject finalquery = new BasicDBObject("$and", obj3);
			noOfTransactions= collection.count(finalquery);
			
			
		} catch (MongoException exception) {
			logger.error("Database Error while fetching getPerCardAllowedTransactions : " , exception);
		}

		return noOfTransactions;

	}

	// running
	public int getPerMerchantTransactions(String payId, String startTimeStamp, String endTimeStamp) {
		int noOfTransactions = 0;
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject query = new BasicDBObject();
			query.put(FieldType.PAY_ID.getName(), payId);
			List<BasicDBObject> obj2 = new ArrayList<BasicDBObject>();

			obj2.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "sale"));
			obj2.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "AUTHORISE"));
			BasicDBObject query2 = new BasicDBObject("$or", obj2);
			if (startTimeStamp != null) {
				String currentDate = null;
				if (endTimeStamp != null) {
					currentDate = endTimeStamp;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", startTimeStamp).add("$lte", endTimeStamp).get());

				List<BasicDBObject> obj3 = new ArrayList<BasicDBObject>();
				obj3.add(query);
				obj3.add(query2);
				obj3.add(dateQuery);

				BasicDBObject finalquery = new BasicDBObject("$and", obj3);
				MongoCursor<Document> cursor = collection.find(finalquery).iterator();
				while (cursor.hasNext()) {
					noOfTransactions++;
				}
			}
		} catch (MongoException exception) {
			logger.error("Database Error while fetching getPerCardAllowedTransactions : " , exception);
		}
		return noOfTransactions;
	}

	// running but ipAddress convert in string problem
	public LinkedList<Long> getSpecificIPandIntervalTransactions(String ipAddress, String payId,
			Map<String, String> timeStampMap) {
		LinkedList<Long> noOfTxnList = new LinkedList<Long>();

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject dateQuery2 = new BasicDBObject();
			BasicDBObject dateQuery3 = new BasicDBObject();
			BasicDBObject dateQuery4 = new BasicDBObject();
			BasicDBObject query = new BasicDBObject();
			query.put(FieldType.PAY_ID.getName(), payId);
			BasicDBObject ipAddressquery = new BasicDBObject();
			ipAddressquery.put(FieldType.INTERNAL_CUST_IP.getName(), ipAddressquery);
			List<BasicDBObject> obj2 = new ArrayList<BasicDBObject>();

			obj2.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "sale"));
			obj2.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "AUTHORISE"));
			BasicDBObject query2 = new BasicDBObject("$or", obj2);

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", timeStampMap.get("hrlyStartStamp"))
							.add("$lte", timeStampMap.get("currentStamp")).get());

			List<BasicDBObject> obj3 = new ArrayList<BasicDBObject>();
			obj3.add(query);
			obj3.add(query2);
			obj3.add(dateQuery);

			BasicDBObject finalquery1 = new BasicDBObject("$or", obj3);

			dateQuery2.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", timeStampMap.get("dailyStartStamp"))
							.add("$lte", timeStampMap.get("currentStamp")).get());

			List<BasicDBObject> secondObj = new ArrayList<BasicDBObject>();
			secondObj.add(query);
			secondObj.add(query2);
			secondObj.add(dateQuery2);
			secondObj.add(ipAddressquery);

			BasicDBObject finalquery2 = new BasicDBObject("$or", secondObj);
			//
			dateQuery3.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", timeStampMap.get("weekhlyStartStamp"))
							.add("$lte", timeStampMap.get("currentStamp")).get());
			List<BasicDBObject> thirdObj = new ArrayList<BasicDBObject>();
			thirdObj.add(query);
			thirdObj.add(query2);
			thirdObj.add(dateQuery3);
			thirdObj.add(ipAddressquery);
			BasicDBObject finalquery3 = new BasicDBObject("$or", thirdObj);
			//
			dateQuery4.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", timeStampMap.get("monthlyStartStamp"))
							.add("$lte", timeStampMap.get("currentStamp")).get());

			List<BasicDBObject> fourthObj = new ArrayList<BasicDBObject>();
			fourthObj.add(query);
			fourthObj.add(query2);
			fourthObj.add(dateQuery4);
			fourthObj.add(ipAddressquery);

			BasicDBObject finalquery4 = new BasicDBObject("$and", fourthObj);
			List<BasicDBObject> resultQuery = new ArrayList<BasicDBObject>();

			resultQuery.add(finalquery1);
			resultQuery.add(finalquery2);
			resultQuery.add(finalquery3);
			resultQuery.add(finalquery4);

			BasicDBObject finalquery = new BasicDBObject("$and", resultQuery);
			MongoCursor<Document> cursor = collection.find(finalquery).iterator();
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					doc = dataEncDecTool.decryptDocument(doc);
                }
				
				List list = new ArrayList(doc.values());
				if (null != list) {
					for (long i = 0; i < list.size(); i++) {
						noOfTxnList.add(i);
					}
				}
			}
			return noOfTxnList;
		} catch (MongoException exception) {
			logger.error("Database Error while fetching getPerCardAllowedTransactions : " , exception);
		}
		return noOfTxnList;

	}
	
	@SuppressWarnings("unchecked")
	public long getPerVpaTransactions(String payId, String vpa) throws SystemException {
		long noOfTransactions = 0;

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
			obj.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			obj.add(new BasicDBObject(FieldType.PAYER_ADDRESS.getName(), vpa));
			BasicDBObject query1 = new BasicDBObject("$and", obj);
			
			List<BasicDBObject> saleConditionList = new ArrayList<BasicDBObject>();
			saleConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),  TransactionType.SALE.getName()));
			saleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(),StatusType.CAPTURED.getName()));
			
			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleConditionList);
			List<BasicDBObject> authoriseConditionList = new ArrayList<BasicDBObject>();
			authoriseConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),TransactionType.AUTHORISE.getName()));
			authoriseConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.APPROVED.getName()));
			
			BasicDBObject authoriseConditionQuery = new BasicDBObject("$and", saleConditionList);
			
			List<BasicDBObject> bothConditionList = new ArrayList<BasicDBObject>();
			bothConditionList.add(saleConditionQuery);
			bothConditionList.add(authoriseConditionQuery);
			BasicDBObject addConditionListQuery = new BasicDBObject("$or", bothConditionList);
			List<BasicDBObject> obj3 = new ArrayList<BasicDBObject>();
			obj3.add(query1);
			obj3.add(addConditionListQuery);

			BasicDBObject finalquery = new BasicDBObject("$and", obj3);
			noOfTransactions= collection.count(finalquery);
			
			
		} catch (MongoException exception) {
			logger.error("Database Error while fetching getPerVpaTransactions :" , exception);
		}

		return noOfTransactions;

	}
}
