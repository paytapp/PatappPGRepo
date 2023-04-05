/**
 * The FraudPreventionMongoDao program implements an application to 
 * perform CRUD operations with MongoDB for collection 'fraudPrevention'.  
 */
package com.paymentgateway.pg.core.fraudPreention.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.DataAccessObject;
import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.FraudPrevention;
import com.paymentgateway.commons.user.FraudPreventionHistory;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FraudRuleType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.SystemProperties;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionManager;

/**
 * @author Harpreet,Rahul,Dhananjay(switched to mongodb)
 * 
 */
@Service
public class FraudPreventionMongoDao {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	private static Logger logger = LoggerFactory.getLogger(FraudPreventionMongoDao.class.getName());
	private static final String PREFIX = "MONGO_DB_";

	public void create(FraudPrevention fraudPrevention) {

		try {
			fraudPrevention.setId(Long.parseLong(TransactionManager.getNewTransactionId()));
			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("unchecked")
			Map<String, Object> map = mapper.convertValue(fraudPrevention, Map.class);
			Document doc = new Document(map);

			insertData(doc);

		} catch (IllegalArgumentException e) {
			String message = "Error while inserting new data in database.";
			logger.error(message, e);
		}
	}

	public void delete(FraudPrevention fraudPrevention) throws SystemException {

		try {
			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("unchecked")
			Map<String, Object> map = mapper.convertValue(fraudPrevention, Map.class);
			Document doc = new Document(map);

			deleteData(doc);

		} catch (IllegalArgumentException e) {
			String message = "Error while deleting data in database.";
			logger.error(message, e);
			throw new SystemException(ErrorType.DATABASE_ERROR, e, message);
		}
	}

	public void update(FraudPrevention fraudPrevention) throws SystemException {

		try {

			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("unchecked")
			Map<String, Object> map = mapper.convertValue(fraudPrevention, Map.class);
			Document doc = new Document(map);

			updateData(doc);

		} catch (IllegalArgumentException e) {
			String message = "Error while deleting data in database.";
			logger.error(message, e);
			throw new SystemException(ErrorType.DATABASE_ERROR, e, message);
		}
	}

	private void insertData(Document doc) {

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(PREFIX + Constants.FRAUD_PREVENTION_COLLECTION.getValue()));
			if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
					&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			logger.info("Record inserted with id: " + doc.get("id"));
		} catch (Exception e) {
			logger.error("Exception while inserting data: " + e);
		}
	}

	private void updateData(Document doc) {

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(PREFIX + Constants.FRAUD_PREVENTION_COLLECTION.getValue()));

			Bson filter = new Document("id", doc.get("id"));
			Bson newDoc = doc;
			Bson update = new Document("$set", newDoc);

			collection.updateOne(filter, update);
			logger.info("Record updated for id: " + doc.get("id"));

		} catch (Exception e) {
			logger.error("Exception while updating data: " + e);
		}
	}

	private void deleteData(Document doc) {

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(PREFIX + Constants.FRAUD_PREVENTION_COLLECTION.getValue()));

			collection.deleteOne(doc);
			logger.info("Record deleted for id: " + doc.get("id"));
		} catch (Exception e) {
			logger.error("Exception while deleting data: " + e);
		}
	}

	private FraudPrevention getObjectByDocument(Document document) {
		FraudPrevention fraudPrevention = new FraudPrevention();

		try {
			document.remove("_id");
			ObjectMapper mapper = new ObjectMapper();
			fraudPrevention = mapper.convertValue(document, FraudPrevention.class);

		} catch (IllegalArgumentException e) {
			logger.error("Excetion while converting document to object: " + e);
		}
		return fraudPrevention;
	}

	@SuppressWarnings({ "unchecked", "static-access" })
	public List<FraudPrevention> getFraudRuleList(String payId) throws ParseException, SystemException {

		logger.info("Inside getFraudRuleList");
		List<FraudPrevention> fraudPreventionRuleList = new ArrayList<FraudPrevention>();
		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

		try {

			BasicDBObject query = new BasicDBObject("status", TDRStatus.ACTIVE.name());

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(PREFIX + Constants.FRAUD_PREVENTION_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			logger.info("Query Executed");
			try {
				while (cursor.hasNext()) {
					Document documentObj = (Document) cursor.next();
					if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
							&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						documentObj = dataEncDecTool.decryptDocument(documentObj);
					}

					if (null != documentObj) {
						// Set<String> keys = documentObj.keySet();
						FraudPrevention fraudPrevention = new FraudPrevention();
						fraudPrevention = getObjectByDocument(documentObj);

						fraudPreventionRuleList.add(fraudPrevention);
					}
				}
				logger.info("Got Fraud prevention list Data by payId with size : " + fraudPreventionRuleList.size());
			} finally {
				cursor.close();
			}
			return fraudPreventionRuleList;

		} catch (Exception exception) {
			String message = "Error while reading data from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

	}

	// to fetch rule with specific payId from fraudprevention->vj
	@SuppressWarnings("unchecked")
	public List<FraudPrevention> getFraudRuleListbyPayId(String payId) throws ParseException, SystemException {

		logger.info("Inside getFraudRuleList");
		List<FraudPrevention> fraudPreventionRuleList = new ArrayList<FraudPrevention>();
		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

		try {
			queryList.add(new BasicDBObject("payId", payId));
			queryList.add(new BasicDBObject("status", TDRStatus.ACTIVE.name()));
			BasicDBObject query = new BasicDBObject("$and", queryList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(PREFIX + Constants.FRAUD_PREVENTION_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			logger.info("Query Executed");
			try {
				while (cursor.hasNext()) {
					Document documentObj = (Document) cursor.next();
					if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
							&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						documentObj = dataEncDecTool.decryptDocument(documentObj);
					}

					if (null != documentObj) {
						// Set<String> keys = documentObj.keySet();
						FraudPrevention fraudPrevention = new FraudPrevention();
						fraudPrevention = getObjectByDocument(documentObj);

						fraudPreventionRuleList.add(fraudPrevention);
					}
				}
				logger.info("Got Fraud prevention list Data with size : " + fraudPreventionRuleList.size());
			} finally {
				cursor.close();
			}
			return fraudPreventionRuleList;

		} catch (Exception exception) {
			String message = "Error while reading data from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

	}

	public FraudPrevention getFraudRuleListbyRuleId(String id) throws ParseException {

		logger.info("Inside getFraudRuleList");
		FraudPrevention fraudPrevention = new FraudPrevention();
		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

		try {
			queryList.add(new BasicDBObject("id", Long.parseLong(id)));
			queryList.add(new BasicDBObject("status", TDRStatus.ACTIVE.name()));
			BasicDBObject query = new BasicDBObject("$and", queryList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(PREFIX + Constants.FRAUD_PREVENTION_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			logger.info("Query Executed");
			try {
				if (cursor.hasNext()) {
					Document documentObj = (Document) cursor.next();
					if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
							&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						documentObj = dataEncDecTool.decryptDocument(documentObj);
					}

					if (null != documentObj) {
						fraudPrevention = getObjectByDocument(documentObj);
					}
				}
				logger.info("Got Fraud prevention Data from database with id: " + id);
			} finally {
				cursor.close();
			}

		} catch (Exception exception) {
			String message = "Error while reading data from database";
			logger.error(message, exception);
		}
		return fraudPrevention;
	}

	// to fetch rules with specific FraudRuleType and payId
	@SuppressWarnings("unchecked")
	public List<FraudPrevention> getFraudRuleList(String payId, FraudRuleType fraudType)
			throws ParseException, SystemException {

		logger.info("Inside getFraudRuleList");
		List<FraudPrevention> fraudPreventionRuleList = new ArrayList<FraudPrevention>();
		List<BasicDBObject> payIdQueryList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

		try {
			BasicDBObject payIdQuery = new BasicDBObject();
			payIdQueryList.add(new BasicDBObject("payId", payId));
			payIdQueryList.add(new BasicDBObject("payId", "ALL"));
			payIdQuery.append("$or", payIdQueryList);

			queryList.add(payIdQuery);
			queryList.add(new BasicDBObject("fraudType", fraudType.getValue()));
			queryList.add(new BasicDBObject("status", TDRStatus.ACTIVE.name()));
			BasicDBObject query = new BasicDBObject("$and", queryList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(PREFIX + Constants.FRAUD_PREVENTION_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			logger.info("Query Executed");
			try {
				while (cursor.hasNext()) {
					Document documentObj = (Document) cursor.next();
					if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
							&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						documentObj = dataEncDecTool.decryptDocument(documentObj);
					}

					if (null != documentObj) {
						FraudPrevention fraudPrevention = new FraudPrevention();
						fraudPrevention = getObjectByDocument(documentObj);

						fraudPreventionRuleList.add(fraudPrevention);
					}
				}
				logger.info("Got Fraud prevention list Data with size : " + fraudPreventionRuleList.size());
			} finally {
				cursor.close();
			}
			return fraudPreventionRuleList;

		} catch (Exception exception) {
			String message = "Error while reading data from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

	}

	@SuppressWarnings("unchecked")
	public List<FraudPrevention> getFraudActiveRule(String payId, FraudRuleType fraudType, TDRStatus status)
			throws ParseException, SystemException {

		logger.info("Inside getFraudActiveRule");
		List<FraudPrevention> fraudPreventionRuleList = new ArrayList<FraudPrevention>();
		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

		try {
			queryList.add(new BasicDBObject("payId", payId));
			queryList.add(new BasicDBObject("fraudType", fraudType.getValue()));
			queryList.add(new BasicDBObject("status", status.getName()));
			BasicDBObject query = new BasicDBObject("$and", queryList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(PREFIX + Constants.FRAUD_PREVENTION_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			logger.info("Query Executed");
			try {
				while (cursor.hasNext()) {
					Document documentObj = (Document) cursor.next();
					if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
							&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						documentObj = dataEncDecTool.decryptDocument(documentObj);
					}

					if (null != documentObj) {
						FraudPrevention fraudPrevention = new FraudPrevention();
						fraudPrevention = getObjectByDocument(documentObj);

						fraudPreventionRuleList.add(fraudPrevention);
					}
				}
				logger.info("Got Fraud prevention list Data with size : " + fraudPreventionRuleList.size());
			} finally {
				cursor.close();
			}
			return fraudPreventionRuleList;

		} catch (Exception exception) {
			String message = "Error while reading data from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

	}

	// list of fraud rules including 'ALL MERCHANTS'
	@SuppressWarnings("unchecked")
	public List<FraudPrevention> getFullFraudRuleList(String payId, String subMerchantId)
			throws ParseException, SystemException {

		logger.info("Inside getFullFraudRuleList");
		List<FraudPrevention> fraudPreventionRuleList = new ArrayList<FraudPrevention>();
		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

		try {
			queryList.add(new BasicDBObject("payId", payId));

			if (StringUtils.isNotBlank(subMerchantId)) {
				queryList.add(new BasicDBObject("subMerchantId", subMerchantId));
			}

			queryList.add(new BasicDBObject("status", TDRStatus.ACTIVE.name()));
			BasicDBObject query = new BasicDBObject("$and", queryList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(PREFIX + Constants.FRAUD_PREVENTION_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			logger.info("Query Executed");
			try {
				while (cursor.hasNext()) {
					Document documentObj = (Document) cursor.next();
					if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
							&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						documentObj = dataEncDecTool.decryptDocument(documentObj);
					}

					if (null != documentObj) {
						FraudPrevention fraudPrevention = new FraudPrevention();
						fraudPrevention = getObjectByDocument(documentObj);

						fraudPreventionRuleList.add(fraudPrevention);
					}
				}
				logger.info("Got Fraud prevention list Data with size : " + fraudPreventionRuleList.size());
			} finally {
				cursor.close();
			}
			return fraudPreventionRuleList;

		} catch (Exception exception) {
			String message = "Error while reading data from database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

	}

	// to fetch total no of txn per merchant
	public int getPerMerchantTransactions(String payId, String startTimeStamp, String endTimeStamp) {
		int noOfTransactions = 0;
		try (Connection connecton = DataAccessObject.getBasicConnection()) {
			String sqlQuery = "SELECT count(TXN_ID) FROM transaction where (PAY_ID = ?) and  (TXNTYPE = 'AUTHORISE' or TXNTYPE = 'SALE') and (CREATE_DATE between ? and ?)";
			PreparedStatement statement = connecton.prepareStatement(sqlQuery);
			statement.setString(1, payId);
			statement.setString(2, startTimeStamp);
			statement.setString(3, endTimeStamp);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					noOfTransactions = Integer.parseInt(resultSet.getString(1));
				}
			}
		} catch (SQLException exception) {
			logger.error("Database Error while fetching getPerCardAllowedTransactions : ", exception);
		}
		return noOfTransactions;
	}

	// check txns between specific interval
	public LinkedList<Long> getSpecificIPandIntervalTransactions(String ipAddress, String payId,
			Map<String, String> timeStampMap) {
		LinkedList<Long> noOfTxnList = new LinkedList<Long>();
		try (Connection connecton = DataAccessObject.getBasicConnection()) {
			String sqlQuery = "(SELECT count(TXN_ID) FROM transaction where (PAY_ID = ?) and  (TXNTYPE = 'AUTHORISE' or TXNTYPE = 'SALE') and (CREATE_DATE >= ? and CREATE_DATE < ?) and (INTERNAL_CUST_IP = ?))"
					+ "UNION (SELECT count(TXN_ID) FROM transaction where (PAY_ID = ?) and  (TXNTYPE = 'AUTHORISE' or TXNTYPE = 'SALE') and (CREATE_DATE >= ? and CREATE_DATE < ?) and (INTERNAL_CUST_IP = ?))"
					+ "UNION (SELECT count(TXN_ID) FROM transaction where (PAY_ID = ?) and  (TXNTYPE = 'AUTHORISE' or TXNTYPE = 'SALE') and (CREATE_DATE >= ? and CREATE_DATE < ?) and (INTERNAL_CUST_IP = ?))"
					+ "UNION (SELECT count(TXN_ID) FROM transaction where (PAY_ID = ?) and  (TXNTYPE = 'AUTHORISE' or TXNTYPE = 'SALE') and (CREATE_DATE >= ? and CREATE_DATE < ?) and (INTERNAL_CUST_IP = ?))";
			PreparedStatement statement = connecton.prepareStatement(sqlQuery);
			statement.setString(1, payId);
			statement.setString(2, timeStampMap.get("hrlyStartStamp"));
			statement.setString(3, timeStampMap.get("currentStamp"));
			statement.setString(4, ipAddress);

			statement.setString(5, payId);
			statement.setString(6, timeStampMap.get("dailyStartStamp"));
			statement.setString(7, timeStampMap.get("currentStamp"));
			statement.setString(8, ipAddress);

			statement.setString(9, payId);
			statement.setString(10, timeStampMap.get("weekhlyStartStamp"));
			statement.setString(11, timeStampMap.get("currentStamp"));
			statement.setString(12, ipAddress);

			statement.setString(13, payId);
			statement.setString(14, timeStampMap.get("monthlyStartStamp"));
			statement.setString(15, timeStampMap.get("currentStamp"));
			statement.setString(16, ipAddress);

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					noOfTxnList.add(Long.parseLong(resultSet.getString(1)));
				}
			}
		} catch (SQLException exception) {
			logger.error("Database Error while fetching getPerCardAllowedTransactions : ", exception);
		}
		return noOfTxnList;
	}

	public void updateFraudRule(long id, TDRStatus status, String repetedays, String dateActiveFrom,
			String dateActiveTo, String startTime, String endTime, String ipAddress, Boolean alwaysOnFlag) {

		Document setDoc = new Document();
		setDoc.put("status", status);
		setDoc.put("repeatDays", repetedays);
		setDoc.put("dateActiveFrom", dateActiveFrom);
		setDoc.put("dateActiveTo", dateActiveTo);
		setDoc.put("startTime", startTime);
		setDoc.put("endTime", endTime);
		setDoc.put("ipAddress", ipAddress);
		setDoc.put("alwaysOnFlag", alwaysOnFlag);

		setDoc.put("id", id);

		Document doc = new Document();
		doc.put("$set", setDoc);

		updateData(doc);

	}

	public void saveFraudrule(FraudPreventionHistory fraudPreventionHistory) throws DataAccessLayerException {

		try {
			fraudPreventionHistory.setId(TransactionManager.getNewTransactionId());
			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("unchecked")
			Map<String, Object> map = mapper.convertValue(fraudPreventionHistory, Map.class);
			Document doc = new Document(map);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(PREFIX + Constants.FRAUD_PREVENTION_HISTORY_COLLECTION.getValue()));
			if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
					&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			logger.info("Record inserted with id: " + doc.get("id"));

		} catch (IllegalArgumentException e) {
			String message = "Error while inserting new data in database.";
			logger.error(message, e);
		}

	}

	// duplicate fraud rule checker
	public boolean duplicateChecker(BasicDBObject query) {

		logger.info("Inside duplicateChecker");

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> collection = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(PREFIX + Constants.FRAUD_PREVENTION_COLLECTION.getValue()));
		// int count = (int)collection.count(query);
		MongoCursor<Document> cursor = collection.find(query).iterator();
		logger.info("Query Executed");

		try {
			if (cursor.hasNext()) {
				return true;
			}
		} catch (Exception e) {
			logger.error("Exception: ", e);
		}
		return false;
	}

	public List<FraudPrevention> getFraudActiveRule(String merchantPayId, String subMerchantPayId,
			String fraudRuleType) {
		logger.info("Inside getFraudRuleList");
		List<FraudPrevention> fraudPreventionRuleList = new ArrayList<FraudPrevention>();
		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

		try {
			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) 
				queryList.add(new BasicDBObject("payId", merchantPayId));

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) 
				queryList.add(new BasicDBObject("subMerchantId", merchantPayId));
			
			
			if(StringUtils.isNotBlank(fraudRuleType) && !fraudRuleType.equalsIgnoreCase("ALL"))
				queryList.add(new BasicDBObject("fraudType",fraudRuleType));
			
			
			queryList.add(new BasicDBObject("status", TDRStatus.ACTIVE.name()));
			BasicDBObject query = new BasicDBObject("$and", queryList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(PREFIX + Constants.FRAUD_PREVENTION_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			logger.info("Query Executed");
			while (cursor.hasNext()) {
				Document documentObj = (Document) cursor.next();
				if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
						&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}
				
				documentObj.remove("_id");
				ObjectMapper mapper = new ObjectMapper();
				fraudPreventionRuleList.add(mapper.convertValue(documentObj, FraudPrevention.class));
				
			}
		} catch (Exception e) {
			logger.info("exception in getFraudActiveRule() ", e);
		}
		return fraudPreventionRuleList;
	}
}
