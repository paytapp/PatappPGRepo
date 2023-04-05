/**
 * 
 */
package com.paymentgateway.payout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class MerchantPayoutDao {

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private UserDao userDao;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(MerchantPayoutDao.class.getName());

	public boolean insertPayoutRequestIntoDB(Map<String, String> reqmap) {
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.MERCHANT_PAYOUT_COLLECTION.getValue()));
			Document doc = new Document();

			doc.put("_id", reqmap.get(FieldType.TXN_ID.getName()));
			doc.put(FieldType.TXN_ID.getName(), reqmap.get(FieldType.TXN_ID.getName()));

			if (reqmap.containsKey(FieldType.ORDER_ID.getName())) {
				doc.put(FieldType.ORDER_ID.getName(), reqmap.get(FieldType.ORDER_ID.getName()));
			}
			
			String payId = reqmap.get(FieldType.PAY_ID.getName());
			User user = userDao.findPayId(payId);
			
			if(StringUtils.isNotBlank(user.getSuperMerchantId())) {
				doc.put(FieldType.SUB_MERCHANT_ID.getName(), reqmap.get(FieldType.PAY_ID.getName()));
				doc.put(FieldType.PAY_ID.getName(),user.getSuperMerchantId());
			}else {

			if (reqmap.containsKey(FieldType.PAY_ID.getName())) {
				doc.put(FieldType.PAY_ID.getName(), reqmap.get(FieldType.PAY_ID.getName()));
			}
			}

			if (reqmap.containsKey(FieldType.BENE_NAME.getName())) {
				doc.put(FieldType.BENE_NAME.getName(), reqmap.get(FieldType.BENE_NAME.getName()));
			}

			if (reqmap.containsKey(FieldType.IFSC_CODE.getName())) {
				doc.put(FieldType.IFSC_CODE.getName(), reqmap.get(FieldType.IFSC_CODE.getName()));
			}

			if (reqmap.containsKey(FieldType.BENE_ACCOUNT_NO.getName())) {
				doc.put(FieldType.BENE_ACCOUNT_NO.getName(), reqmap.get(FieldType.BENE_ACCOUNT_NO.getName()));
			}

			if (reqmap.containsKey(FieldType.ORDER_ID.getName())) {
				doc.put(FieldType.ORDER_ID.getName(), reqmap.get(FieldType.ORDER_ID.getName()));
			}

			if (reqmap.containsKey(FieldType.AMOUNT.getName())) {
				doc.put(FieldType.AMOUNT.getName(), reqmap.get(FieldType.AMOUNT.getName()));
			}

			if (reqmap.containsKey(FieldType.CURRENCY_CODE.getName())) {
				doc.put(FieldType.CURRENCY_CODE.getName(), reqmap.get(FieldType.CURRENCY_CODE.getName()));
			}

			if (reqmap.containsKey(FieldType.PHONE_NO.getName())) {
				doc.put(FieldType.PHONE_NO.getName(), reqmap.get(FieldType.PHONE_NO.getName()));
			}

			doc.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
			doc.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PENDING.getCode());

			doc.put(FieldType.RRN.getName(), null);

			try {
				doc.put("REQUEST_RECEIVED", new Gson().toJson(reqmap));
			} catch (Exception e) {
				logger.error("Exception caugth while inserting entire payout request JSON into DB");
			}

			if (reqmap.containsKey(FieldType.CREATE_DATE.getName())) {
				doc.put(FieldType.CREATE_DATE.getName(), reqmap.get(FieldType.CREATE_DATE.getName()));
			}

			if (reqmap.containsKey(FieldType.UPDATE_DATE.getName())) {
				doc.put(FieldType.UPDATE_DATE.getName(), reqmap.get(FieldType.UPDATE_DATE.getName()));
			}

			coll.insertOne(doc);
			return true;
		} catch (Exception e) {
			logger.error("Exception caugth while inserting merchant payout request into DB : " , e);
			return false;
		}
	}

	public Document merchantPayoutEnquiry(Map<String, String> reqmap) {
		List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
		if (StringUtils.isNotBlank(reqmap.get(FieldType.PAY_ID.getName()))) {
			paramConditionLst
					.add(new BasicDBObject(FieldType.PAY_ID.getName(), reqmap.get(FieldType.PAY_ID.getName())));
		}
		if (StringUtils.isNotBlank(reqmap.get(FieldType.ORDER_ID.getName()))) {
			paramConditionLst
					.add(new BasicDBObject(FieldType.ORDER_ID.getName(), reqmap.get(FieldType.ORDER_ID.getName())));
		}
		if (StringUtils.isNotBlank(reqmap.get(FieldType.AMOUNT.getName()))) {
			paramConditionLst
					.add(new BasicDBObject(FieldType.AMOUNT.getName(), reqmap.get(FieldType.AMOUNT.getName())));
		}
		if (StringUtils.isNotBlank(reqmap.get(FieldType.CURRENCY_CODE.getName()))) {
			paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(),
					reqmap.get(FieldType.CURRENCY_CODE.getName())));
		}

		BasicDBObject query = new BasicDBObject("$and", paramConditionLst);

		logger.info("Inside MerchantPayoutDao , merchantPayoutEnquiry , query = " + query);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.MERCHANT_PAYOUT_COLLECTION.getValue()));
		BasicDBObject match = new BasicDBObject("$match", query);
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		BasicDBObject limit = new BasicDBObject("$limit", 1);

		List<BasicDBObject> pipeline = Arrays.asList(match, sort, limit);

		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();

		if (cursor.hasNext()) {
			Document data = cursor.next();
			return data;
		}
		return null;
	}

	public void checkDuplicateOrderId(Fields fields) {
		  try {
	            MongoDatabase dbIns = null;
	            String orderId = fields.get(FieldType.ORDER_ID.getName());
	            String payId = fields.get(FieldType.PAY_ID.getName());
	            String currencyString = fields.get(FieldType.CURRENCY_CODE.getName());
	            String subMerchantId="";
	            User user=userDao.findPayId(payId);
	            
	            if(StringUtils.isNotBlank(user.getSuperMerchantId()))
	            {
	            	subMerchantId=user.getPayId();
	            	payId=user.getSuperMerchantId();
	            }	            
	            
	            List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
	            conditionList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
	            conditionList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
	            conditionList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyString));
	            if(StringUtils.isNotBlank(subMerchantId)){
	            	conditionList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
	            }

	            BasicDBObject query = new BasicDBObject("$and", conditionList);
	            dbIns = mongoInstance.getDB();
	            MongoCollection<Document> collection = dbIns
	                    .getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.MERCHANT_PAYOUT_COLLECTION.getValue()));
	            MongoCursor<Document> cursor = collection.find(query).iterator();
	            if (cursor.hasNext()) {
	                fields.put(FieldType.DUPLICATE_YN.getName(),"Y");
	            }else{
	                fields.put(FieldType.DUPLICATE_YN.getName(),"N");
	            }
	            cursor.close();
	            
	        } catch (Exception exception) {
	          
	            String message = "Error while Checking orderId in database";
	            logger.error(message, exception);
	            
	        }
	        
		
	}
}
