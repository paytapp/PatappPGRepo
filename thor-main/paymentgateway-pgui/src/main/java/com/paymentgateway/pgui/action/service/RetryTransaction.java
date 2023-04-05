package com.paymentgateway.pgui.action.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Neeraj
 *
 */
@Service
public class RetryTransaction {

//	private Integer count;
	private boolean TransactionFailFlag;
	private static Logger logger = LoggerFactory.getLogger(RetryTransaction.class.getName());

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private UserSettingDao userSettingDao;

	private static final String prefix = "MONGO_DB_";
	public void retryPayment(Fields responseMap,
			HttpServletRequest sessionMap, User user) {
		Integer count ;
		try {
			UserSettingData userSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
			String respCode = responseMap
					.get(FieldType.RESPONSE_CODE.getName());
			if (userSettings.isRetryTransactionCustomeFlag()
					&& !respCode.equals(ErrorType.SUCCESS.getCode())) {
				Object obj = userSettings.getAttemptTrasacation();
				if (null == obj) {
					setTransactionFailFlag(false);
					return;
				}
				Integer counter = Integer
						.parseInt(userSettings.getAttemptTrasacation());
//				Integer attemptCounter = (Integer) sessionMap
//						.get(Constants.COUNT.getValue());
				Integer attemptCounter = getRetryAttemptCount(sessionMap);
				
				if (null == attemptCounter) {
					count = 1;
//					sessionMap.put(Constants.COUNT.getValue(), count);
					updateRetryCount(sessionMap, count);
					setTransactionFailFlag(true);
				} else if (attemptCounter < counter) {
					attemptCounter = attemptCounter + 1;
//					sessionMap.put(Constants.COUNT.getValue(), attemptCounter);
					updateRetryCount(sessionMap,attemptCounter);
					setTransactionFailFlag(true);
				}else {
					setTransactionFailFlag(false);
				}
				// Call method for remove secureField
				removeFields(responseMap, sessionMap);
				
			} else if(!userSettings.isRetryTransactionCustomeFlag()){
				setTransactionFailFlag(false);
			}
		} catch (Exception exception) {
			logger.error("Exception in retry payment: ", exception);

		}

	}

	// remove secureField
	public void removeFields(Fields responseMap,
			HttpServletRequest sessionMap) {
		responseMap.remove(FieldType.CARD_EXP_DT.getName());
		responseMap.remove(FieldType.CVV.getName());
		//responseMap.remove(FieldType.CARD_MASK.getName());
		//responseMap.remove(FieldType.MOP_TYPE.getName());
		responseMap.remove(FieldType.ACQUIRER_TYPE.getName());
		//responseMap.remove(FieldType.RESPONSE_CODE.getName());
		responseMap.remove(FieldType.MERCHANT_ID.getName());
		//responseMap.remove(FieldType.PG_TXN_MESSAGE.getName());
		//responseMap.remove(FieldType.AUTH_CODE.getName());
		//responseMap.remove(FieldType.RESPONSE_CODE.getName());
		//responseMap.remove(FieldType.PG_RESP_CODE.getName());
		responseMap.put((FieldType.CARD_MASK.getName()),
				responseMap.get(FieldType.CARD_MASK.getName()));
		responseMap.put((FieldType.CARD_EXP_DT.getName()),
				responseMap.get(FieldType.CARD_EXP_DT.getName()));
		responseMap.put((FieldType.CVV.getName()),
				responseMap.get(FieldType.CVV.getName()));
		responseMap.put((FieldType.RESPONSE_CODE.getName()),
				responseMap.get(FieldType.RESPONSE_CODE.getName()));
		responseMap.put((FieldType.ACQUIRER_TYPE.getName()),
				responseMap.get(FieldType.ACQUIRER_TYPE.getName()));
		Object previousFields = (Fields) sessionMap.getSession().getAttribute(Constants.FIELDS.getValue());
		Fields sessionFields = null;
		if (null != previousFields) {
			sessionFields = (Fields) previousFields;
		} else {
		}
		sessionFields.put(responseMap);
	}

	private Integer getRetryAttemptCount(HttpServletRequest sessionMap) {
		Integer count = null;
		String orederId = (String) sessionMap.getSession().getAttribute(FieldType.ORDER_ID.getName());
		String payId = (String) sessionMap.getSession().getAttribute(FieldType.PAY_ID.getName());

		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

		BasicDBObject projectElement = new BasicDBObject();
		projectElement.put(FieldType.RETRY_COUNT.getName(), 1);
		projectElement.put(FieldType.TXN_ID.getName(), 1);
		projectElement.put(FieldType.PAY_ID.getName(), 1);
		projectElement.put(FieldType.SUB_MERCHANT_ID.getName(), 1);
		projectElement.put(FieldType.STATUS.getName(), 1);

		queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orederId));
		queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		queryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.REJECTED.getName()));

		BasicDBObject query = new BasicDBObject("$and", queryList);
		BasicDBObject match = new BasicDBObject("$match", query);
		try {
			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			BasicDBObject project = new BasicDBObject("$project", projectElement);

			List<BasicDBObject> pipeline = Arrays.asList(match, project);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			int entryCount = (int) coll.count(query);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (dbobj.containsKey(FieldType.RETRY_COUNT.getName())) {
					sessionMap.getSession().setAttribute("TXNID", dbobj.getString(FieldType.TXN_ID.getName()));
					return Integer.valueOf(dbobj.getInteger(FieldType.RETRY_COUNT.getName()));
				} else if(entryCount == 1) {
					sessionMap.getSession().setAttribute("TXNID", dbobj.getString(FieldType.TXN_ID.getName()));
				}
			}
		} catch (Exception exception) {
			logger.error("Exception in Fetching txn for retry count: ", exception);
		}
		return count;
	}
	
	private void updateRetryCount(HttpServletRequest sessionMap, int count) {
		try {
			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			Bson filter = new Document(FieldType.TXN_ID.getName(), sessionMap.getSession().getAttribute("TXNID"));

			Bson newValue = new Document(FieldType.RETRY_COUNT.getName(), count);

			Bson updateDocument = new Document("$set", newValue);
			coll.updateOne(filter, updateDocument);
			sessionMap.removeAttribute("TXNID");
			
		} catch (Exception exception) {
			logger.error("Exception in updating retry count: ", exception);

		}
	}

	public boolean isTransactionFailFlag() {
		return TransactionFailFlag;
	}

	public void setTransactionFailFlag(boolean TransactionFailFlag) {
		this.TransactionFailFlag = TransactionFailFlag;
	}

}
