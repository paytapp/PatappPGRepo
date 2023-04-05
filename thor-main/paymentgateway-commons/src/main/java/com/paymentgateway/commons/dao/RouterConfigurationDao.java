package com.paymentgateway.commons.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.onUsOffUs;

/**
 * @author Shaiwal
 *
 */

@Service
public class RouterConfigurationDao extends HibernateAbstractDao {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(RouterConfigurationDao.class.getName());

	public void create(RouterConfiguration routerConfiguration) throws DataAccessLayerException {

		Document docToAdd = rcToDoc(routerConfiguration);
		docToAdd.put("id", TransactionManager.getNewTransactionId());
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		coll.insertOne(docToAdd);
		// super.save(routerConfiguration);
	}

	public void delete(String id) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {

			Document query = new Document();
			query.put("id", id);
			Document setData = new Document();
			setData.put("STATUS", TDRStatus.INACTIVE.getName());
			setData.put("STATUS_NAME", "Off");
			Document update = new Document();
			update.put("$set", setData);
			coll.updateOne(query, update);

		} catch (Exception e) {

			logger.error("Exception in marking router configuration as inactive", e);
		}
	}
	
	public void delete(String id, String userEmailId) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {

			Document query = new Document();
			query.put("id", id);
			Document setData = new Document();
			setData.put("STATUS", TDRStatus.INACTIVE.getName());
			setData.put("STATUS_NAME", "Off");
			setData.put("UPDATED_BY", userEmailId);
			Document update = new Document();
			update.put("$set", setData);
			coll.updateOne(query, update);

		} catch (Exception e) {

			logger.error("Exception in marking router configuration as inactive", e);
		}
	}

	public void deleteUsingIdentifier(String identifier, String onoffName) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {

			Document query = new Document();
			query.put("IDENTIFIER", identifier);
			query.put("ONUS_OFFUS_NAME", onoffName);

			Document setData = new Document();
			setData.put("STATUS", TDRStatus.INACTIVE.getName());
			setData.put("STATUS_NAME", "Off");

			Document update = new Document();
			update.put("$set", setData);
			coll.updateMany(query, update);

		} catch (Exception e) {

			logger.error("Exception in marking router configuration as inactive", e);
		}
	}

	public void deleteUsingIdentifier(String identifier, String onoffName, String acquirer) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {

			Document query = new Document();
			query.put("IDENTIFIER", identifier);
			query.put("ONUS_OFFUS_NAME", onoffName);
			query.put("ACQUIRER_TYPE", acquirer);

			Document setData = new Document();
			setData.put("STATUS", TDRStatus.INACTIVE.getName());
			setData.put("STATUS_NAME", "Off");

			Document update = new Document();
			update.put("$set", setData);
			coll.updateMany(query, update);

		} catch (Exception e) {

			logger.error("Exception in marking router configuration as inactive", e);
		}
	}
	
	public void deleteUsingIdentifier(String identifier, String onoffName, String acquirer, String merchantEmailId) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {

			Document query = new Document();
			query.put("IDENTIFIER", identifier);
			query.put("ONUS_OFFUS_NAME", onoffName);
			query.put("ACQUIRER_TYPE", acquirer);
			

			Document setData = new Document();
			setData.put("STATUS", TDRStatus.INACTIVE.getName());
			setData.put("STATUS_NAME", "Off");
			setData.put("UPDATE_BY", merchantEmailId);

			Document update = new Document();
			update.put("$set", setData);
			coll.updateMany(query, update);

		} catch (Exception e) {

			logger.error("Exception in marking router configuration as inactive", e);
		}
	}

	public void delete(String id, User user) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			Document query = new Document();
			query.append("id", id);
			Document setData = new Document();
			setData.put("STATUS", TDRStatus.INACTIVE.getName());
			setData.put("STATUS_NAME", "Off");
			setData.put("UPDATED_DATE", dateNow);
			setData.put("UDPATED_BY", user.getEmailId());
			Document update = new Document();

			update.append("$set", setData);
			coll.updateOne(query, update);

		} catch (Exception e) {

			logger.error("Exception in marking router configuration as inactive", e);
		}
	}

	public void deletePending(String id, User user) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {
			RouterConfiguration routerConfiguration = findPendingRule(id);
			if (null != routerConfiguration) {

				Date dNow = new Date();
				String dateNow = DateCreater.formatDateForDb(dNow);

				Document query = new Document();
				query.append("id", id);
				Document setData = new Document();
				setData.put("STATUS", TDRStatus.INACTIVE.getName());
				setData.put("UPDATED_DATE", dateNow);
				setData.put("STATUS_NAME", "Off");
				setData.put("UDPATED_BY", user.getEmailId());
				Document update = new Document();

				update.append("$set", setData);
				coll.updateOne(query, update);

			}
		} catch (Exception e) {

			logger.error("Exception in deletePending router config", e);
		}
	}

	public RouterConfiguration findPendingRule(String id) {

		RouterConfiguration routerConfiguration = null;

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));
		try {
			BasicDBObject idQuery = new BasicDBObject("id", id);
			BasicDBObject pendingQuery = new BasicDBObject("STATUS", TDRStatus.PENDING.getName());

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(pendingQuery);
			paramList.add(idQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();
				routerConfiguration = docToRc(doc);

			}

			cursor.close();
			return routerConfiguration;

		} catch (Exception e) {
			logger.error("Exception in fetching pending router configuration", e);
		}
		return routerConfiguration;

	}

	public void delete(RouterConfiguration routerConfiguration) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {
			if (null != routerConfiguration) {

				String id = routerConfiguration.getId();

				Document query = new Document();
				query.append("id", id);
				Document setData = new Document();
				setData.append("STATUS", TDRStatus.INACTIVE.getName());
				setData.put("STATUS_NAME", "Off");
				Document update = new Document();
				update.append("$set", setData);
				coll.updateOne(query, update);

			}
		} catch (Exception e) {
			logger.error("Exception in delete router configuration", e);
		}
	}

	public RouterConfiguration findRule(String id) {

		RouterConfiguration routerConfiguration = null;

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {

			BasicDBObject idQuery = new BasicDBObject("id", id);
			BasicDBObject pendingQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(pendingQuery);
			paramList.add(idQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();
				routerConfiguration = docToRc(doc);
			}
			cursor.close();
		} catch (Exception e) {
			logger.error("Exception in find router configuration", e);

		}
		return routerConfiguration;

	}

	public List<RouterConfiguration> findRulesByIdentifier(String identifier) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();
		try {

			BasicDBObject idQuery = new BasicDBObject("IDENTIFIER", identifier);
			BasicDBObject pendingQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(pendingQuery);
			paramList.add(idQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();
				RouterConfiguration routerConfiguration = docToRc(doc);
				rulesList.add(routerConfiguration);
			}
			cursor.close();
			return rulesList;
		} catch (Exception e) {
			logger.error("Exception in findRulesByIdentifier", e);
		}

		return rulesList;
	}

	public List<RouterConfiguration> findRulesByIdentifier(String identifier, String onOffName) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();
		try {

			BasicDBObject idQuery = new BasicDBObject("IDENTIFIER", identifier);
			BasicDBObject onOffQuery = new BasicDBObject("ONUS_OFFUS_NAME", onOffName);
			BasicDBObject pendingQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(pendingQuery);
			paramList.add(idQuery);
			paramList.add(onOffQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();
				RouterConfiguration routerConfiguration = docToRc(doc);
				rulesList.add(routerConfiguration);
			}
			cursor.close();
			return rulesList;
		} catch (Exception e) {
			logger.error("Exception in findRulesByIdentifier", e);
		}

		return rulesList;
	}

	public RouterConfiguration findActiveRulesByIdentifier(String identifier, String onOffName, String acquirer) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		RouterConfiguration routerConfiguration = new RouterConfiguration();

		try {

			BasicDBObject idQuery = new BasicDBObject("IDENTIFIER", identifier);
			BasicDBObject onOffQuery = new BasicDBObject("ONUS_OFFUS_NAME", onOffName);
			BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER_TYPE", acquirer);
			BasicDBObject statusQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(statusQuery);
			paramList.add(idQuery);
			paramList.add(onOffQuery);
			paramList.add(acquirerQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();
				routerConfiguration = docToRc(doc);
			}
			cursor.close();
			return routerConfiguration;
		} catch (Exception e) {
			logger.error("Exception in findRulesByIdentifier", e);
		}

		return routerConfiguration;
	}

	public List<RouterConfiguration> findPendingRulesByIdentifier(String identifier) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();
		try {

			BasicDBObject idQuery = new BasicDBObject("IDENTIFIER", identifier);
			BasicDBObject pendingQuery = new BasicDBObject("STATUS", TDRStatus.PENDING.getName());

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(pendingQuery);
			paramList.add(idQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();
				RouterConfiguration routerConfiguration = docToRc(doc);
				rulesList.add(routerConfiguration);
			}
			cursor.close();
			return rulesList;
		} catch (Exception e) {
			logger.error("Exception in findRulesByIdentifier", e);
		}

		return rulesList;

	}

	public List<RouterConfiguration> getPendingRouterConfiguration() {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();
		try {

			BasicDBObject pendingQuery = new BasicDBObject("STATUS", TDRStatus.PENDING.getName());

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(pendingQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();
				RouterConfiguration routerConfiguration = docToRc(doc);
				rulesList.add(routerConfiguration);
			}
			cursor.close();
			return rulesList;
		} catch (Exception e) {
			logger.error("Exception in findRulesByIdentifier", e);
		}

		return rulesList;

	}

	public List<RouterConfiguration> findSortedRulesByIdentifier(String identifier) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();
		try {

			BasicDBObject pendingQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());
			BasicDBObject currActQuery = new BasicDBObject("CURRENTLY_ACTIVE", true);
			BasicDBObject identifierQuery = new BasicDBObject("IDENTIFIER", identifier);

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(pendingQuery);
			paramList.add(currActQuery);
			paramList.add(identifierQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);

			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("PRIORITY", 1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document doc = cursor.next();
				RouterConfiguration routerConfiguration = docToRc(doc);
				rulesList.add(routerConfiguration);
			}
			cursor.close();
			return rulesList;
		} catch (Exception e) {
			logger.error("Exception in findSortedRulesByIdentifier ", e);
		}

		return rulesList;

	}

	public RouterConfiguration findNextAvailableConfigurationByIdentifier(String identifier) {
		RouterConfiguration routerConfiguration = new RouterConfiguration();

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {

			BasicDBObject statusQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());
			BasicDBObject isDownQuery = new BasicDBObject("IS_DOWN", false);
			BasicDBObject identifierQuery = new BasicDBObject("IDENTIFIER", identifier);

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(statusQuery);
			paramList.add(isDownQuery);
			paramList.add(identifierQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);

			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("PRIORITY", 1));
			BasicDBObject limit = new BasicDBObject("$limit", 1);

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, limit);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document doc = cursor.next();
				routerConfiguration = docToRc(doc);
				break;
			}
			cursor.close();
			return routerConfiguration;
		} catch (Exception e) {
			logger.error("Exception in findNextAvailableConfigurationByIdentifier ", e);
		}

		return routerConfiguration;

	}

	public RouterConfiguration findRulesByIdentifierAcquirer(String identifier, String acquirer) {

		RouterConfiguration routerConfiguration = new RouterConfiguration();

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {

			BasicDBObject statusQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());
			BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER_TYPE", acquirer);
			BasicDBObject identifierQuery = new BasicDBObject("IDENTIFIER", identifier);

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(statusQuery);
			paramList.add(acquirerQuery);
			paramList.add(identifierQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);

			BasicDBObject match = new BasicDBObject("$match", finalQuery);

			List<BasicDBObject> pipeline = Arrays.asList(match);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document doc = cursor.next();
				routerConfiguration = docToRc(doc);
				break;
			}
			cursor.close();
			return routerConfiguration;
		} catch (Exception e) {
			logger.error("Exception in findNextAvailableConfigurationByIdentifier ", e);
		}

		return routerConfiguration;
	}

	public boolean checkAvailableAcquirers(String identifier, String acquirer) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();
		try {

			BasicDBObject pendingQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());
			BasicDBObject isDownQuery = new BasicDBObject("IS_DOWN", false);
			BasicDBObject identifierQuery = new BasicDBObject("IDENTIFIER", identifier);
			BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER_TYPE", acquirer);

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(pendingQuery);
			paramList.add(isDownQuery);
			paramList.add(identifierQuery);
			paramList.add(acquirerQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);

			BasicDBObject match = new BasicDBObject("$match", finalQuery);

			List<BasicDBObject> pipeline = Arrays.asList(match);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document doc = cursor.next();
				RouterConfiguration routerConfiguration = docToRc(doc);
				rulesList.add(routerConfiguration);
			}
			cursor.close();

			if (rulesList.size() > 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			logger.error("Exception in checkAvailableAcquirers ", e);
		}

		return false;

	}

	public List<RouterConfiguration> findActiveAcquirersByIdentifier(String identifier) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();
		try {

			BasicDBObject statusQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());
			BasicDBObject currActQuery = new BasicDBObject("CURRENTLY_ACTIVE", true);
			BasicDBObject identifierQuery = new BasicDBObject("IDENTIFIER", identifier);

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(statusQuery);
			paramList.add(currActQuery);
			paramList.add(identifierQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);

			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("PRIORITY", 1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document doc = cursor.next();
				RouterConfiguration routerConfiguration = docToRc(doc);
				rulesList.add(routerConfiguration);
			}
			cursor.close();
			return rulesList;
		} catch (Exception e) {
			logger.error("Exception in findSortedRulesByIdentifier ", e);
		}

		return rulesList;

	}

	public RouterConfiguration findNextActiveAcquirer(String identifier) {

		RouterConfiguration routerConfiguration = new RouterConfiguration();

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {

			BasicDBObject statusQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());
			BasicDBObject currentActQuery = new BasicDBObject("CURRENTLY_ACTIVE", true);
			BasicDBObject identifierQuery = new BasicDBObject("IDENTIFIER", identifier);

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(statusQuery);
			paramList.add(currentActQuery);
			paramList.add(identifierQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);

			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("PRIORITY", 1));
			BasicDBObject limit = new BasicDBObject("$limit", 1);

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, limit);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document doc = cursor.next();
				routerConfiguration = docToRc(doc);
				break;
			}
			cursor.close();
			return routerConfiguration;
		} catch (Exception e) {
			logger.error("Exception in findNextActiveAcquirer ", e);
		}

		return routerConfiguration;

	}

	public List<RouterConfiguration> getActiveRules() {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();
		try {

			BasicDBObject statusQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(statusQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);

			BasicDBObject match = new BasicDBObject("$match", finalQuery);

			List<BasicDBObject> pipeline = Arrays.asList(match);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document doc = cursor.next();
				RouterConfiguration routerConfiguration = docToRc(doc);
				rulesList.add(routerConfiguration);
			}
			cursor.close();
			return rulesList;
		} catch (Exception e) {
			logger.error("Exception in getActiveRules ", e);
		}

		return rulesList;
	}

	public List<RouterConfiguration> getActiveRulesByMerchant(String merchant) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();
		try {

			BasicDBObject statusQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());
			BasicDBObject merchantQuery = new BasicDBObject("MERCHANT", merchant);

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(statusQuery);
			paramList.add(merchantQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
			BasicDBObject match = new BasicDBObject("$match", finalQuery);

			List<BasicDBObject> pipeline = Arrays.asList(match);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document doc = cursor.next();
				RouterConfiguration routerConfiguration = docToRc(doc);
				rulesList.add(routerConfiguration);
			}
			cursor.close();
			return rulesList;
		} catch (Exception e) {
			logger.error("Exception in getActiveRulesByMerchant ", e);
		}

		return rulesList;

	}

	public List<RouterConfiguration> getActiveRulesByMerchant(String merchant, String cardHolder, String paymentType,
			String acquiringMode) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();
		try {

			BasicDBObject statusQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());
			BasicDBObject merchantQuery = new BasicDBObject("MERCHANT", merchant);
			BasicDBObject cardHolderQuery = new BasicDBObject("CARD_HOLDER_TYPE", cardHolder);
			BasicDBObject paymentTypeQuery = new BasicDBObject("PAYMENT_TYPE", paymentType);
			BasicDBObject onOffQuery = new BasicDBObject("ON_US_OFF_US", false);
			BasicDBObject acquiringModeQuery = new BasicDBObject("ONUS_OFFUS_NAME", acquiringMode);

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(statusQuery);
			paramList.add(merchantQuery);
			paramList.add(cardHolderQuery);
			paramList.add(paymentTypeQuery);
			paramList.add(onOffQuery);
			paramList.add(acquiringModeQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
			BasicDBObject match = new BasicDBObject("$match", finalQuery);

			List<BasicDBObject> pipeline = Arrays.asList(match);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document doc = cursor.next();
				RouterConfiguration routerConfiguration = docToRc(doc);
				rulesList.add(routerConfiguration);
			}
			cursor.close();
			return rulesList;
		} catch (Exception e) {
			logger.error("Exception in getActiveRulesByMerchant ", e);
		}

		return rulesList;

	}

	public List<RouterConfiguration> getActiveRulesByAcquirer(String merchant, String paymentType, int start,
			int length, String type) {
		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {
			if (StringUtils.isNotBlank(type)) {

				BasicDBObject merchantQuery = new BasicDBObject("MERCHANT", merchant);
				BasicDBObject cardHolderQuery = new BasicDBObject("CARD_HOLDER_TYPE", type);
				BasicDBObject paymentTypeQuery = new BasicDBObject("PAYMENT_TYPE", paymentType);

				List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
				paramList.add(merchantQuery);
				paramList.add(cardHolderQuery);
				paramList.add(paymentTypeQuery);

				BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
				BasicDBObject match = new BasicDBObject("$match", finalQuery);
				BasicDBObject skip = new BasicDBObject("$skip", start);
				BasicDBObject limit = new BasicDBObject("$limit", length);
				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("id", -1));

				List<BasicDBObject> pipeline = Arrays.asList(match, sort, skip, limit);

				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				MongoCursor<Document> cursor = output.iterator();

				while (cursor.hasNext()) {

					Document doc = cursor.next();
					RouterConfiguration routerConfiguration = docToRc(doc);
					rulesList.add(routerConfiguration);
				}
				cursor.close();
			} else {

				BasicDBObject merchantQuery = new BasicDBObject("MERCHANT", merchant);
				BasicDBObject paymentTypeQuery = new BasicDBObject("PAYMENT_TYPE", paymentType);

				List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
				paramList.add(merchantQuery);
				paramList.add(paymentTypeQuery);

				BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
				BasicDBObject match = new BasicDBObject("$match", finalQuery);
				BasicDBObject skip = new BasicDBObject("$skip", start);
				BasicDBObject limit = new BasicDBObject("$limit", length);
				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("id", -1));

				List<BasicDBObject> pipeline = Arrays.asList(match, sort, skip, limit);

				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				MongoCursor<Document> cursor = output.iterator();

				while (cursor.hasNext()) {

					Document doc = cursor.next();
					RouterConfiguration routerConfiguration = docToRc(doc);
					rulesList.add(routerConfiguration);
				}
				cursor.close();

			}

			return rulesList;
		} catch (Exception e) {
			logger.error("Exception in getActiveRulesByAcquirer ", e);
		}

		return rulesList;
	}

	public int getActiveRulesCountByAcquirer(String merchant, String paymentType, int start, int length, String type) {
		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {
			if (StringUtils.isNotBlank(type)) {

				BasicDBObject merchantQuery = new BasicDBObject("MERCHANT", merchant);
				BasicDBObject cardHolderQuery = new BasicDBObject("CARD_HOLDER_TYPE", type);
				BasicDBObject paymentTypeQuery = new BasicDBObject("PAYMENT_TYPE", paymentType);

				List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
				paramList.add(merchantQuery);
				paramList.add(cardHolderQuery);
				paramList.add(paymentTypeQuery);

				BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
				BasicDBObject match = new BasicDBObject("$match", finalQuery);
				BasicDBObject skip = new BasicDBObject("$skip", start);
				BasicDBObject limit = new BasicDBObject("$limit", length);
				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("id", -1));

				List<BasicDBObject> pipeline = Arrays.asList(match, sort, skip, limit);

				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				MongoCursor<Document> cursor = output.iterator();

				while (cursor.hasNext()) {

					Document doc = cursor.next();
					RouterConfiguration routerConfiguration = docToRc(doc);
					rulesList.add(routerConfiguration);
				}
				cursor.close();

			} else {

				BasicDBObject merchantQuery = new BasicDBObject("MERCHANT", merchant);
				BasicDBObject paymentTypeQuery = new BasicDBObject("PAYMENT_TYPE", paymentType);

				List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
				paramList.add(merchantQuery);
				paramList.add(paymentTypeQuery);

				BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
				BasicDBObject match = new BasicDBObject("$match", finalQuery);
				BasicDBObject skip = new BasicDBObject("$skip", start);
				BasicDBObject limit = new BasicDBObject("$limit", length);
				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("id", -1));

				List<BasicDBObject> pipeline = Arrays.asList(match, sort, skip, limit);

				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				MongoCursor<Document> cursor = output.iterator();

				while (cursor.hasNext()) {

					Document doc = cursor.next();
					RouterConfiguration routerConfiguration = docToRc(doc);
					rulesList.add(routerConfiguration);
				}
				cursor.close();

			}

			return rulesList.size();
		} catch (Exception e) {
			logger.error("Exception in getActiveRulesCountByAcquirer ", e);
		}
		return rulesList.size();
	}

	public List<RouterConfiguration> getRuleListFromChargingDetails(ChargingDetails cd) {
		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {

			BasicDBObject slabQuery = new BasicDBObject("SLAB_ID", cd.getSlabId());
			BasicDBObject cardHolderQuery = new BasicDBObject("CARD_HOLDER_TYPE", cd.getCardHolderType().toString());
			BasicDBObject currencyQuery = new BasicDBObject("CURRENCY", cd.getCurrency());
			BasicDBObject merchantQuery = new BasicDBObject("MERCHANT", cd.getPayId());
			BasicDBObject mopTypeQuery = new BasicDBObject("MOP_TYPE", cd.getMopType().getCode());
			BasicDBObject paymentTypeQuery = new BasicDBObject("PAYMENT_TYPE", cd.getPaymentType().getCode());
			BasicDBObject statusQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());
			BasicDBObject onOffValueQuery = new BasicDBObject("ONUS_OFFUS_NAME", cd.getAcquiringMode().toString());
			BasicDBObject paymentRegionQuery = new BasicDBObject("PAYMENTS_REGION", cd.getPaymentsRegion().toString());
			BasicDBObject txnTypeQuery = new BasicDBObject("TRANSACTION_TYPE", cd.getTransactionType().getName());

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(slabQuery);
			paramList.add(cardHolderQuery);
			paramList.add(currencyQuery);
			paramList.add(merchantQuery);
			paramList.add(mopTypeQuery);
			paramList.add(paymentTypeQuery);
			paramList.add(paymentRegionQuery);
			paramList.add(txnTypeQuery);
			paramList.add(onOffValueQuery);
			paramList.add(statusQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
			BasicDBObject match = new BasicDBObject("$match", finalQuery);

			List<BasicDBObject> pipeline = Arrays.asList(match);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document doc = cursor.next();
				RouterConfiguration routerConfiguration = docToRc(doc);
				rulesList.add(routerConfiguration);
			}
			cursor.close();

			return rulesList;
		} catch (Exception e) {
			logger.error("Exception in getRuleListFromChargingDetails ", e);
		}
		return rulesList;
	}

	public List<RouterConfiguration> getAcqRuleFromCD(ChargingDetails cd) {
		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.ROUTER_CONFIG_COLLECTION.getValue()));

		try {

			BasicDBObject slabQuery = new BasicDBObject("SLAB_ID", cd.getSlabId());
			BasicDBObject cardHolderQuery = new BasicDBObject("CARD_HOLDER_TYPE", cd.getCardHolderType().toString());
			BasicDBObject currencyQuery = new BasicDBObject("CURRENCY", cd.getCurrency());
			BasicDBObject merchantQuery = new BasicDBObject("MERCHANT", cd.getPayId());
			BasicDBObject mopTypeQuery = new BasicDBObject("MOP_TYPE", cd.getMopType().getCode());
			BasicDBObject paymentTypeQuery = new BasicDBObject("PAYMENT_TYPE", cd.getPaymentType().getCode());
			BasicDBObject statusQuery = new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName());
			BasicDBObject onOffValueQuery = new BasicDBObject("ONUS_OFFUS_NAME", cd.getAcquiringMode().toString());
			BasicDBObject paymentRegionQuery = new BasicDBObject("PAYMENTS_REGION", cd.getPaymentsRegion().toString());
			BasicDBObject txnTypeQuery = new BasicDBObject("TRANSACTION_TYPE", cd.getTransactionType().getName());
			BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER_TYPE",
					AcquirerType.getInstancefromName(cd.getAcquirerName()).getCode());

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(slabQuery);
			paramList.add(cardHolderQuery);
			paramList.add(currencyQuery);
			paramList.add(merchantQuery);
			paramList.add(mopTypeQuery);
			paramList.add(paymentTypeQuery);
			paramList.add(paymentRegionQuery);
			paramList.add(txnTypeQuery);
			paramList.add(onOffValueQuery);
			paramList.add(statusQuery);
			paramList.add(acquirerQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
			BasicDBObject match = new BasicDBObject("$match", finalQuery);

			List<BasicDBObject> pipeline = Arrays.asList(match);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document doc = cursor.next();
				RouterConfiguration routerConfiguration = docToRc(doc);
				rulesList.add(routerConfiguration);
			}
			cursor.close();

			return rulesList;
		} catch (Exception e) {
			logger.error("Exception in getRuleListFromChargingDetails ", e);
		}
		return rulesList;
	}

	public void updateRouterByCD(ChargingDetails cd, String operation, boolean valid) {

		try {

			Comparator<RouterConfiguration> comp = (RouterConfiguration a, RouterConfiguration b) -> {

				if (b.getAcqTdr() < a.getAcqTdr()) {
					return 1;
				} else if (b.getAcqTdr() > a.getAcqTdr()) {
					return -1;
				} else {
					return 0;
				}
			};

			List<RouterConfiguration> rcList = new ArrayList<RouterConfiguration>();
			List<RouterConfiguration> rcListUpdated = new ArrayList<RouterConfiguration>();

			StringBuilder sb = new StringBuilder();
			sb.append(cd.getPayId());
			sb.append(cd.getCurrency());
			sb.append(cd.getPaymentType().getCode());
			sb.append(cd.getMopType().getCode());
			sb.append(cd.getTransactionType().toString());
			sb.append(cd.getPaymentsRegion().toString());
			sb.append(cd.getCardHolderType().toString());
			sb.append(cd.getSlabId());

			rcList = findRulesByIdentifier(sb.toString(), cd.getAcquiringMode().toString());

			if (operation.equalsIgnoreCase("created") && valid) {

				RouterConfiguration rc = new RouterConfiguration();
				rc = getRcFromCd(cd, rcList.size());

				rcList.add(rc);
				Collections.sort(rcList, comp);

				// Deactivate old rules
				deleteUsingIdentifier(sb.toString(), cd.getAcquiringMode().toString());
				int count = 1;

				// Create new rules
				for (RouterConfiguration config : rcList) {

					if (cd.getAcquiringMode().equals(onUsOffUs.ON_US)) {
						config.setPriority(String.valueOf(1));
						create(config);
					} else {
						config.setPriority(String.valueOf(count));
						count = count + 1;
						create(config);
					}

				}

			} else if (operation.equalsIgnoreCase("removed")) {

				// If on us transaction , no need for load transfer , just delete acquirer from
				// router
				if (cd.getAcquiringMode().equals(onUsOffUs.ON_US)) {

					deleteUsingIdentifier(sb.toString(), onUsOffUs.ON_US.toString(),
							AcquirerType.getInstancefromName(cd.getAcquirerName()).getCode());
				}

				else {

					Collections.sort(rcList, comp);

					int loadOnRemovedAcq = 0;
					boolean isAnyConfigOn = false;
					boolean isloadAssigned = false;

					// find load on removed acquirer and check if any other acquirer is available to
					// accept this load
					for (RouterConfiguration config : rcList) {

						if (config.getAcquirer()
								.equalsIgnoreCase(AcquirerType.getInstancefromName(cd.getAcquirerName()).getCode())) {
							loadOnRemovedAcq = config.getLoadPercentage();
						} else {
							if (config.isCurrentlyActive()) {
								isAnyConfigOn = true;
							}
						}
					}

					// Deactivate all old rules
					deleteUsingIdentifier(sb.toString(), cd.getAcquiringMode().toString());

					int count = 1;

					// Create new rules and set load from removed acquirer
					for (RouterConfiguration config : rcList) {

						// skip the acquirer for which charging details have been deactivated
						if (config.getAcquirer()
								.equalsIgnoreCase(AcquirerType.getInstancefromName(cd.getAcquirerName()).getCode())) {
							continue;
						}

						// Set priority based on Acq Tdr
						config.setPriority(String.valueOf(count));

						// Assign load to highest priority acquirer is none of the acquirer is currently
						// in On status
						if (!isAnyConfigOn && count == 1) {

							int thisLoad = config.getLoadPercentage();
							thisLoad = thisLoad + loadOnRemovedAcq;
							config.setLoadPercentage(thisLoad);
							config.setCurrentlyActive(true);
							config.setStatusName("On");
							isAnyConfigOn = true;

						} else {

							// Assign load to acquirer which is currently On and mark that load has been
							// transferred
							if (config.isCurrentlyActive() && !isloadAssigned) {

								int thisLoad = config.getLoadPercentage();
								thisLoad = thisLoad + loadOnRemovedAcq;
								config.setLoadPercentage(thisLoad);
								isloadAssigned = true;
								isAnyConfigOn = true;
							}
						}
						count = count + 1;
						create(config);
					}

				}

			} else if (operation.equalsIgnoreCase("updated")) {

				if (cd.getAcquiringMode().equals(onUsOffUs.ON_US)) {

					// No Priorities need to be updated for On Us rules as all have Priority 1
					// Check if Router Config is not present , add a config for this charging detail

					RouterConfiguration rc = findActiveRulesByIdentifier(sb.toString(),
							cd.getAcquiringMode().toString(),
							AcquirerType.getInstancefromName(cd.getAcquirerName()).getCode());
					
					// No Config Found for this charging detail so create a new one
					if (StringUtils.isBlank(rc.getId())){
						rc = getRcFromCd(cd, 0);
						rc.setPriority("1");
						create(rc);
					}

				}

				else {
					// Update Acq TDR for updated charging details of Off Us Rules
					for (RouterConfiguration rc : rcList) {

						if (rc.getAcquirer()
								.equalsIgnoreCase(AcquirerType.getInstancefromName(cd.getAcquirerName()).getCode())) {
							rc.setAcqTdr(cd.getBankTDR());
						}

						rcListUpdated.add(rc);
					}

					// Deactivate old rules
					deleteUsingIdentifier(sb.toString(), cd.getAcquiringMode().toString());

					Collections.sort(rcListUpdated, comp);
					int count = 1;

					// Assign new priorities and add new rules
					for (RouterConfiguration rc : rcListUpdated) {

						rc.setPriority(String.valueOf(count));
						count = count + 1;
						create(rc);
					}
				}

			}

		}

		catch (Exception e) {
			logger.error("Exception while updating router using Charging Details ", e);
		}

	}

	public RouterConfiguration docToRc(Document document) {

		RouterConfiguration routerConfiguration = new RouterConfiguration();

		try {

			if (document.get("id") != null) {
				routerConfiguration.setId(document.getString("id"));
			}

			if (document.get("ONUS_OFFUS_NAME") != null) {
				routerConfiguration.setOnUsoffUsName(document.getString("ONUS_OFFUS_NAME"));
			}

			if (document.get("ACQUIRER_TYPE") != null) {
				routerConfiguration.setAcquirer(document.getString("ACQUIRER_TYPE"));
			}

			if (document.get("ALLOW_AMT_BASED_ROUTING") != null) {
				routerConfiguration.setAllowAmountBasedRouting(document.getBoolean("ALLOW_AMT_BASED_ROUTING"));
			}

			if (document.get("ALLOWED_FAILURE_COUNT") != null) {
				routerConfiguration.setAllowedFailureCount(document.getInteger("ALLOWED_FAILURE_COUNT"));
			}

			if (document.get("ALWAYS_ON") != null) {
				routerConfiguration.setAlwaysOn(document.getBoolean("ALWAYS_ON"));
			}

			if (document.get("CARD_HOLDER_TYPE") != null) {

				if (document.getString("CARD_HOLDER_TYPE").equalsIgnoreCase(CardHolderType.CONSUMER.toString())) {
					routerConfiguration.setCardHolderType(CardHolderType.CONSUMER);
				} else if (document.getString("CARD_HOLDER_TYPE")
						.equalsIgnoreCase(CardHolderType.COMMERCIAL.toString())) {
					routerConfiguration.setCardHolderType(CardHolderType.COMMERCIAL);
				} else {
					routerConfiguration.setCardHolderType(CardHolderType.PREMIUM);
				}
			}

			if (document.get("CREATED_DATE") != null) {
				routerConfiguration.setCreatedDate(
						new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(document.getString("CREATED_DATE")));
			}

			if (document.get("CURRENCY") != null) {
				routerConfiguration.setCurrency(document.getString("CURRENCY"));
			}

			if (document.get("CURRENTLY_ACTIVE") != null) {
				routerConfiguration.setCurrentlyActive(document.getBoolean("CURRENTLY_ACTIVE"));
			}

			if (document.get("FAILURE_COUNT") != null) {
				routerConfiguration.setFailureCount(document.getInteger("FAILURE_COUNT"));
			}

			if (document.get("IDENTIFIER") != null) {
				routerConfiguration.setIdentifier(document.getString("IDENTIFIER"));
			}

			if (document.get("IS_DOWN") != null) {
				routerConfiguration.setDown(document.getBoolean("IS_DOWN"));
			}

			if (document.get("LOAD_PERCENT") != null) {
				routerConfiguration.setLoadPercentage(document.getInteger("LOAD_PERCENT"));
			}

			if (document.get("MAX_AMOUNT") != null) {
				routerConfiguration.setMaxAmount(document.getDouble("MAX_AMOUNT"));
			}

			if (document.get("MERCHANT") != null) {
				routerConfiguration.setMerchant(document.getString("MERCHANT"));
			}

			if (document.get("MIN_AMOUNT") != null) {
				routerConfiguration.setMinAmount(document.getDouble("MIN_AMOUNT"));
			}

			if (document.get("MODE") != null) {
				routerConfiguration.setMode(document.getString("MODE"));
			}

			if (document.get("STATUS_NAME") != null) {
				routerConfiguration.setStatusName(document.getString("STATUS_NAME"));
			}

			if (document.get("MOP_TYPE") != null) {
				routerConfiguration.setMopType(document.getString("MOP_TYPE"));
			}

			if (document.get("ON_US_OFF_US") != null) {
				routerConfiguration.setOnUsoffUs(document.getBoolean("ON_US_OFF_US"));
			}

			if (document.get("PAYMENT_TYPE") != null) {
				routerConfiguration.setPaymentType(document.getString("PAYMENT_TYPE"));
			}

			if (document.get("PAYMENTS_REGION") != null) {

				if (document.getString("PAYMENTS_REGION").equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString())) {

					routerConfiguration.setPaymentsRegion(AccountCurrencyRegion.DOMESTIC);
				} else {
					routerConfiguration.setPaymentsRegion(AccountCurrencyRegion.INTERNATIONAL);
				}

			}

			if (document.get("PRIORITY") != null) {
				routerConfiguration.setPriority(document.getString("PRIORITY"));
			}

			if (document.get("REQUESTED_BY") != null) {
				routerConfiguration.setRequestedBy(document.getString("REQUESTED_BY"));
			}
			
			if (document.get("UPDATED_BY") != null) {
				routerConfiguration.setUpdatedBy(document.getString("UPDATED_BY"));
			}

			if (document.get("SLAB_ID") != null) {
				routerConfiguration.setSlabId(document.getString("SLAB_ID"));
			}

			if (document.get("STATUS") != null) {
				if (document.getString("STATUS").equalsIgnoreCase(TDRStatus.ACTIVE.getName())) {
					routerConfiguration.setStatus(TDRStatus.ACTIVE);
				} else if (document.getString("STATUS").equalsIgnoreCase(TDRStatus.PENDING.getName())) {
					routerConfiguration.setStatus(TDRStatus.PENDING);
				} else {
					routerConfiguration.setStatus(TDRStatus.INACTIVE);
				}
			}

			if (document.get("SWITCH_ON_FAIL") != null) {
				routerConfiguration.setSwitchOnFail(document.getBoolean("SWITCH_ON_FAIL"));
			}

			if (document.get("TRANSACTION_TYPE") != null) {
				routerConfiguration.setTransactionType(document.getString("TRANSACTION_TYPE"));
			}

			if (document.get("UPDATED_DATE") != null) {
				routerConfiguration.setUpdatedDate(
						new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(document.getString("UPDATED_DATE")));
			}

			if (document.get("RETRY_MINUTES") != null) {
				routerConfiguration.setRetryMinutes(document.getString("RETRY_MINUTES"));
			}

			if (document.get("ACQ_TDR") != null) {
				routerConfiguration.setAcqTdr(Double.valueOf(document.getString("ACQ_TDR")));
			}
		} catch (Exception e) {
			logger.error("Exception in parsing document to Router configuration : ", e);
		}
		return routerConfiguration;
	}

	public Document rcToDoc(RouterConfiguration routerConfiguration) {

		Document document = new Document();

		if (routerConfiguration.getAcquirer() != null) {
			document.put("ACQUIRER_TYPE", routerConfiguration.getAcquirer());
		}

		document.put("ALLOW_AMT_BASED_ROUTING", routerConfiguration.isAllowAmountBasedRouting());
		document.put("ALLOWED_FAILURE_COUNT", routerConfiguration.getAllowedFailureCount());
		document.put("ALWAYS_ON", routerConfiguration.isAlwaysOn());

		if (routerConfiguration.getCardHolderType() != null) {
			document.put("CARD_HOLDER_TYPE", routerConfiguration.getCardHolderType().toString());
		}

		if (routerConfiguration.getStatusName() != null) {
			document.put("STATUS_NAME", routerConfiguration.getStatusName());
		}

		if (routerConfiguration.getCreatedDate() != null) {
			SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			document.put("CREATED_DATE", outputDateFormat.format(routerConfiguration.getCreatedDate()).toString());
		}

		if (routerConfiguration.getCurrency() != null) {
			document.put("CURRENCY", routerConfiguration.getCurrency());
		}

		document.put("CURRENTLY_ACTIVE", routerConfiguration.isCurrentlyActive());
		document.put("FAILURE_COUNT", routerConfiguration.getFailureCount());

		if (routerConfiguration.getIdentifier() != null) {
			document.put("IDENTIFIER", routerConfiguration.getIdentifier());
		}

		document.put("IS_DOWN", routerConfiguration.isDown());

		document.put("LOAD_PERCENT", routerConfiguration.getLoadPercentage());
		document.put("MAX_AMOUNT", routerConfiguration.getMaxAmount());

		if (routerConfiguration.getMerchant() != null) {
			document.put("MERCHANT", routerConfiguration.getMerchant());
		}

		document.put("MIN_AMOUNT", routerConfiguration.getMinAmount());

		if (routerConfiguration.getMode() != null) {
			document.put("MODE", routerConfiguration.getMode());
		}

		if (routerConfiguration.getMopType() != null) {
			document.put("MOP_TYPE", routerConfiguration.getMopType());
		}

		document.put("ON_US_OFF_US", routerConfiguration.isOnUsoffUs());

		if (routerConfiguration.getPaymentType() != null) {
			document.put("PAYMENT_TYPE", routerConfiguration.getPaymentType());
		}

		if (routerConfiguration.getPaymentsRegion() != null) {

			document.put("PAYMENTS_REGION", routerConfiguration.getPaymentsRegion().toString());

		}

		if (routerConfiguration.getPriority() != null) {
			document.put("PRIORITY", routerConfiguration.getPriority());
		}

		if (routerConfiguration.getRequestedBy() != null) {
			document.put("REQUESTED_BY", routerConfiguration.getRequestedBy());
		}
		if (routerConfiguration.getUpdatedBy() != null) {
			document.put("UPDATE_BY", routerConfiguration.getUpdatedBy());
		}

		if (routerConfiguration.getSlabId() != null) {
			document.put("SLAB_ID", routerConfiguration.getSlabId());
		}

		if (routerConfiguration.getRetryMinutes() != null) {
			document.put("RETRY_MINUTES", routerConfiguration.getRetryMinutes());
		}

		if (routerConfiguration.getStatus() != null) {

			if (routerConfiguration.getStatus().equals(TDRStatus.ACTIVE)) {
				document.put("STATUS", TDRStatus.ACTIVE.getName());
			} else if (routerConfiguration.getStatus().equals(TDRStatus.PENDING)) {
				document.put("STATUS", TDRStatus.PENDING.getName());
			} else {
				document.put("STATUS", TDRStatus.INACTIVE.getName());
			}

		}

		document.put("SWITCH_ON_FAIL", routerConfiguration.isSwitchOnFail());

		if (routerConfiguration.getTransactionType() != null) {
			document.put("TRANSACTION_TYPE", routerConfiguration.getTransactionType());
		}

		if (routerConfiguration.getUpdatedDate() != null) {
			SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			document.put("UPDATED_DATE", outputDateFormat.format(routerConfiguration.getUpdatedDate()).toString());
		}

		if (routerConfiguration.getOnUsoffUsName() != null) {
			document.put("ONUS_OFFUS_NAME", routerConfiguration.getOnUsoffUsName());
		}

		if (routerConfiguration.getAcqTdr() != null) {
			document.put("ACQ_TDR", routerConfiguration.getAcqTdr().toString());
		}

		return document;
	}

	public RouterConfiguration getRcFromCd(ChargingDetails cd, int size) {

		RouterConfiguration rc = new RouterConfiguration();

		StringBuilder sb = new StringBuilder();

		sb.append(cd.getPayId());
		sb.append(cd.getCurrency());
		sb.append(cd.getPaymentType().getCode());
		sb.append(cd.getMopType().getCode());
		sb.append(cd.getTransactionType().getName());
		sb.append(cd.getPaymentsRegion().toString());
		sb.append(cd.getCardHolderType().toString());
		sb.append(cd.getSlabId());

		rc.setId(TransactionManager.getNewTransactionId());
		rc.setAcquirer(AcquirerType.getInstancefromName(cd.getAcquirerName()).getCode());
		rc.setAllowAmountBasedRouting(false);
		rc.setAllowedFailureCount(5);
		rc.setAlwaysOn(false);
		rc.setCardHolderType(cd.getCardHolderType());
		rc.setCreatedDate(new Date());
		rc.setCurrency(cd.getCurrency());
		rc.setFailureCount(0);
		rc.setIdentifier(sb.toString());
		rc.setDown(false);
		rc.setMaxAmount(Double.valueOf(cd.getMaxTxnAmount()));
		rc.setMerchant(cd.getPayId());
		rc.setMinAmount(Double.valueOf(cd.getMinTxnAmount()));
		rc.setMopType(cd.getMopType().getCode());
		rc.setOnUsoffUs(false);
		rc.setPaymentType(cd.getPaymentType().getCode());
		rc.setPaymentsRegion(cd.getPaymentsRegion());
		rc.setRequestedBy("");
		rc.setSlabId(cd.getSlabId());
		rc.setStatus(TDRStatus.ACTIVE);
		rc.setSwitchOnFail(false);
		rc.setTransactionType(cd.getTransactionType().getName());
		rc.setUpdatedDate(new Date());
		rc.setRetryMinutes("10");
		rc.setAcqTdr(cd.getBankTDR());
		rc.setOnUsoffUsName(cd.getAcquiringMode().toString());

		if (size < 1) {
			rc.setLoadPercentage(100);
			rc.setCurrentlyActive(true);
			rc.setStatusName("On");
			rc.setMode("AUTO");
		} else {
			rc.setLoadPercentage(0);
			rc.setCurrentlyActive(false);
			rc.setStatusName("StandBy");
			rc.setMode("AUTO");

			// FOr on us txn , always load will be 100 % for all acquirers and status will
			// be on for all configurations
			if (cd.getAcquiringMode().equals(onUsOffUs.ON_US)) {

				rc.setLoadPercentage(100);
				rc.setCurrentlyActive(true);
				rc.setStatusName("On");
				rc.setMode("AUTO");
			}
		}

		return rc;

	}

	public RouterConfiguration pendingRequestForIdentifier(String identifier, String onoffName, String acquirerName, String status) {
		
		MongoCursor<Document> cursor = null;
		RouterConfiguration routerConfiguration = null;
		
		try {
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.PENDING_ROUTER_CONFIG_COLLECTION.getValue()));
		
				BasicDBObject identifierQuery = new BasicDBObject("IDENTIFIER", identifier);
				BasicDBObject onOffQuery = new BasicDBObject("ONUS_OFFUS_NAME", onoffName);
				BasicDBObject acqType = new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acquirerName);
				BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), status);
				

				List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
				paramList.add(identifierQuery);
				paramList.add(onOffQuery);
				paramList.add(acqType);
				paramList.add(statusQuery);
				
				BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
				cursor = coll.find(finalQuery).iterator();

				while (cursor.hasNext()) {
					Document doc = cursor.next();
					routerConfiguration = docToRc(doc);
				}
				cursor.close();
		} catch (Exception e) {

			logger.error("Exception in pendingRequestForIdentifier() while get pendingRequest ", e);
		}
		return routerConfiguration;
	}
	
	public void createForPendingRequest(RouterConfiguration routerConfiguration) throws DataAccessLayerException {

		Document docToAdd = rcToDoc(routerConfiguration);
		docToAdd.put("id", TransactionManager.getNewTransactionId());
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.PENDING_ROUTER_CONFIG_COLLECTION.getValue()));

		coll.insertOne(docToAdd);
		// super.save(routerConfiguration);
	}
	
	public List<RouterConfiguration> getPendingRCList() {
		
		List<RouterConfiguration> pendingList = new ArrayList<RouterConfiguration>();
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PENDING_ROUTER_CONFIG_COLLECTION.getValue()));
			BasicDBObject finalQuery = new BasicDBObject("STATUS", TDRStatus.PENDING.getName());
			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();
				pendingList.add(docToRc(doc));
			}
			cursor.close();
		} catch (MongoException mongoException) {
			logger.error("caught mongo exception in routerConfiguration dao !! ", mongoException);
		} catch (Exception ex) {
			logger.error("caught exception in routerConfiguration dao !! ", ex);
		}
		return pendingList;
	}
	public void statusUpdateRouterConfigForPendingRequest(String identifier, String onoffName, String acquirerName, String date, String emailId, String status) {
		
		if(status.equalsIgnoreCase("reject")) {
			status = TDRStatus.REJECTED.getName();
		} else if (status.equalsIgnoreCase("Accept")) {
			status = TDRStatus.ACCEPTED.getName();
		}
		
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.PENDING_ROUTER_CONFIG_COLLECTION.getValue()));

		try {
			

			Document query = new Document();
			query.put("IDENTIFIER", identifier);
			query.put("ONUS_OFFUS_NAME", onoffName);
			query.put("ACQUIRER_TYPE", acquirerName);

			Document setData = new Document();
			setData.put("STATUS", status);
			setData.put("UPDATED_BY", emailId);
			setData.put("UPDATED_DATE", date);

			Document update = new Document();
			update.put("$set", setData);
			coll.updateMany(query, update);

		} catch (Exception e) {

			logger.error("Exception in marking router configuration as inactive", e);
		}
	}
	
	public String getRequestedByEmailIdFromPendingRequest(String identifier, String onoffName, String acquirerName) {
		
		String requestedBy = null;
		MongoCursor<Document> cursor = null;
		
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.PENDING_ROUTER_CONFIG_COLLECTION.getValue()));

		try {
			BasicDBObject identifierQuery = new BasicDBObject("IDENTIFIER", identifier);
			BasicDBObject onOffQuery = new BasicDBObject("ONUS_OFFUS_NAME", onoffName);
			BasicDBObject acqType = new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acquirerName);

			List<BasicDBObject> paramList = new ArrayList<BasicDBObject>();
			paramList.add(identifierQuery);
			paramList.add(onOffQuery);
			paramList.add(acqType);
			
			BasicDBObject finalQuery = new BasicDBObject("$and", paramList);
			cursor = coll.find(finalQuery).iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();
				/*routerConfiguration = docToRc(doc);
				requestedBy = routerConfiguration.getRequestedBy();*/
				requestedBy = doc.getString("REQUESTED_BY");
			}
		} catch (Exception e) {

			logger.error("Exception while get requested by email id ", e);
		}
		return requestedBy;
	}
	
}
