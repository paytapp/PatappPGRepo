package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.List;

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
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.CustomerCategory;
import com.paymentgateway.commons.user.ParentMerchantMapping;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class ParentMerchantMappingDao {

	@Autowired
	private MongoInstance mongoInstance;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(ParentMerchantMappingDao.class.getName());

	private static final String ACTIVE = "Active";
	private static final String INACTIVE = "Inactive";
	private static final String TERMINATED = "Terminated";

	public void createOrUpdate(ParentMerchantMapping parentMerchantMapping, User sessionUser) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PARENT_MERCHANT_MAPPING.getValue()));

			BasicDBObject query = new BasicDBObject();

			query.append("parentPayId", parentMerchantMapping.getParentPayId());
			query.append("merchantPayId", parentMerchantMapping.getMerchantPayId());

			List<BasicDBObject> statusList = new ArrayList<BasicDBObject>();

			statusList.add(new BasicDBObject("status", ACTIVE));
			statusList.add(new BasicDBObject("status", INACTIVE));

			query.append("$or", statusList);

			MongoCursor<Document> curser = collection.find(query).iterator();

			ObjectMapper objMapper = new ObjectMapper();

			if (curser.hasNext()) {

				Document dbDoc = curser.next();

				parentMerchantMapping.setCreateDate(dbDoc.getString("createDate"));
				parentMerchantMapping.setCreatedBy(dbDoc.getString("createdBy"));

				parentMerchantMapping.setUpdateDate(DateCreater.defaultCurrentDateTime());
				parentMerchantMapping.setUpdatedBy(sessionUser.getBusinessName());

				parentMerchantMapping.setStatus(TERMINATED);

				Document doc = objMapper.convertValue(parentMerchantMapping, Document.class);

				Bson filter = query;
				Bson newValue = doc;

				Bson updateOperationDocument = new Document("$set", newValue);
				collection.updateOne(filter, updateOperationDocument);

			}

			parentMerchantMapping.setCreateDate(DateCreater.defaultCurrentDateTime());
			parentMerchantMapping.setCreatedBy(sessionUser.getBusinessName());

			if (parentMerchantMapping.isActiveFlag()) {
				parentMerchantMapping.setStatus(ACTIVE);
			} else {
				parentMerchantMapping.setStatus(INACTIVE);
			}

			Document doc = objMapper.convertValue(parentMerchantMapping, Document.class);

			if (!doc.isEmpty())
				collection.insertOne(doc);
			else
				logger.info("empty doc " + doc);

		} catch (Exception e) {
			logger.error("Exception while inserting data createOrUpdate() : " + e);
		}

	}

	public List<ParentMerchantMapping> getMappingListUsingParentPayId(String parentPayId) {

		List<ParentMerchantMapping> mappingList = new ArrayList<ParentMerchantMapping>();
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PARENT_MERCHANT_MAPPING.getValue()));

			BasicDBObject query = new BasicDBObject();

			query.append("parentPayId", parentPayId);
			query.append("status", new BasicDBObject("$ne", TERMINATED));

			MongoCursor<Document> curser = collection.find(query).iterator();

			ObjectMapper objMapper = new ObjectMapper();

			while (curser.hasNext()) {

				Document dbDoc = curser.next();

				dbDoc.remove("_id");

				ParentMerchantMapping parentMerchantMapping = new ParentMerchantMapping();

				parentMerchantMapping = objMapper.convertValue(dbDoc, ParentMerchantMapping.class);

				mappingList.add(parentMerchantMapping);

			}

		} catch (Exception e) {
			logger.error("Exception while inserting data createOrUpdate() : " + e);
		}

		return mappingList;

	}

	public boolean deleteMapping(String parentPayId, String merchantPayId) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PARENT_MERCHANT_MAPPING.getValue()));

			BasicDBObject query = new BasicDBObject();

			query.append("parentPayId", parentPayId);

			List<BasicDBObject> statusList = new ArrayList<BasicDBObject>();

			statusList.add(new BasicDBObject("status", ACTIVE));
			statusList.add(new BasicDBObject("status", INACTIVE));

			query.append("$or", statusList);
			query.append("merchantPayId", merchantPayId);

			long count = collection.count(query);
			logger.info("found parent mapping count > " + count);

			if (count > 0) {

				Bson filter = query;
				Bson newValue = new BasicDBObject("status", TERMINATED);

				Bson updateOperationDocument = new Document("$set", newValue);
				collection.updateOne(filter, updateOperationDocument);
			}

			count = collection.count(query);

			if (count <= 0) {
				logger.info("Parent Mapping deleted successfully!!, Query " + query);
				return true;
			} else {
				logger.info("Parent Mapping deleted Failed!!, Query " + query);
				return false;
			}

		} catch (Exception e) {
			logger.error("Exception while inserting data deleteMapping() : " + e);
		}

		return false;
	}

	public void deleteMappingOldMapping(String parentPayId, User sessionUser) {
		logger.info("inside deleteMappingOldMapping");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PARENT_MERCHANT_MAPPING.getValue()));

			BasicDBObject query = new BasicDBObject();

			query.append("parentPayId", parentPayId);

			List<BasicDBObject> statusList = new ArrayList<BasicDBObject>();

			statusList.add(new BasicDBObject("status", ACTIVE));
			statusList.add(new BasicDBObject("status", INACTIVE));

			query.append("$or", statusList);

			long count = collection.count(query);
			logger.info("found parent mapping count > " + count);

			if (count > 0) {

				Bson filter = query;
				Bson newValue = new BasicDBObject("status", TERMINATED)
						.append("updatedBy", sessionUser.getBusinessName())
						.append("updateDate", DateCreater.defaultCurrentDateTime());

				Bson updateOperationDocument = new Document("$set", newValue);
				collection.updateMany(filter, updateOperationDocument);
			}

			logger.info("deleted all mapping for parent ID deleteMappingOldMapping() " + parentPayId);

		} catch (Exception e) {
			logger.error("Exception while inserting data deleteMappingOldMapping() : " + e);
		}

	}

	public List<ParentMerchantMapping> findActiveMerchantByPayId(String parentPayId) {

		List<ParentMerchantMapping> mappingList = new ArrayList<ParentMerchantMapping>();
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PARENT_MERCHANT_MAPPING.getValue()));

			BasicDBObject query = new BasicDBObject();

			query.append("parentPayId", parentPayId);
			query.append("status", ACTIVE);

			MongoCursor<Document> curser = collection.find(query).iterator();

			ObjectMapper objMapper = new ObjectMapper();

			while (curser.hasNext()) {

				Document dbDoc = curser.next();

				dbDoc.remove("_id");

				ParentMerchantMapping parentMerchantMapping = new ParentMerchantMapping();

				parentMerchantMapping = objMapper.convertValue(dbDoc, ParentMerchantMapping.class);

				mappingList.add(parentMerchantMapping);

			}

		} catch (Exception e) {
			logger.error("Exception while inserting data createOrUpdate() : " + e);
		}

		return mappingList;

	}

	public List<ParentMerchantMapping> findActiveMerchantByPayId(String parentPayId, String custCategory) {

		List<ParentMerchantMapping> mappingList = new ArrayList<ParentMerchantMapping>();
		try {
			logger.info("Customer category Type for parent Merchant >>" + custCategory);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PARENT_MERCHANT_MAPPING.getValue()));

			BasicDBObject query = new BasicDBObject();

			query.append("parentPayId", parentPayId);
			query.append("status", ACTIVE);
			if (StringUtils.isNotBlank(custCategory)) {
				if (custCategory.equalsIgnoreCase(CustomerCategory.SILVER.toString())) {
					query.append("customerCategory", CustomerCategory.SILVER.toString());
				} else if (custCategory.equalsIgnoreCase(CustomerCategory.GOLD.toString())) {
					query.append("customerCategory", CustomerCategory.GOLD.toString());
				} else if (custCategory.equalsIgnoreCase(CustomerCategory.DIAMOND.toString())) {
					query.append("customerCategory", CustomerCategory.DIAMOND.toString());
				} else if (custCategory.equalsIgnoreCase(CustomerCategory.PLATINUM.toString())) {
					query.append("customerCategory", CustomerCategory.PLATINUM.toString());
				} else if (custCategory.equalsIgnoreCase(CustomerCategory.DEFAULT.toString())) {
					query.append("customerCategory", CustomerCategory.DEFAULT.toString());
				} else {
					query.append("customerCategory", CustomerCategory.DEFAULT.toString());
				}
			} else {
				query.append("customerCategory", CustomerCategory.DEFAULT.toString());
			}
			MongoCursor<Document> curser = collection.find(query).iterator();

			ObjectMapper objMapper = new ObjectMapper();

			while (curser.hasNext()) {

				Document dbDoc = curser.next();

				dbDoc.remove("_id");

				ParentMerchantMapping parentMerchantMapping = new ParentMerchantMapping();

				parentMerchantMapping = objMapper.convertValue(dbDoc, ParentMerchantMapping.class);

				mappingList.add(parentMerchantMapping);

			}

		} catch (Exception e) {
			logger.error("Exception while inserting data createOrUpdate() : " + e);
		}

		return mappingList;

	}
}
