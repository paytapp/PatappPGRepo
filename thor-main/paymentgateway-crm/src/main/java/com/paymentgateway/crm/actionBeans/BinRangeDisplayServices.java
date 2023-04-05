package com.paymentgateway.crm.actionBeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.BinRange;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class BinRangeDisplayServices {

	private static Logger logger = LoggerFactory.getLogger(BinRangeDisplayServices.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	MongoInstance mongoInstance;

	@Autowired
	PropertiesManager propertiesManager;

	public List<BinRange> getBinRangDisplay(String cardType, String mopType, User user, int start, int length)
			throws SystemException {

		List<BinRange> binRangeList = new ArrayList<BinRange>();
		List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();
		try {
			if (!cardType.equalsIgnoreCase("ALL")) {
				finalList.add(new BasicDBObject("CARD_TYPE", cardType));
			}
			if (!mopType.equalsIgnoreCase("ALL")) {
				finalList.add(new BasicDBObject("MOP_TYPE", mopType));
			}
			BasicDBObject finalquery;
			if (!finalList.isEmpty()) {
				finalquery = new BasicDBObject("$and", finalList);
			} else {
				finalquery = new BasicDBObject();
			}
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.BIN_RANGE_COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject skip = new BasicDBObject("$skip", start);
			BasicDBObject limit = new BasicDBObject("$limit", length);
			List<BasicDBObject> pipeline = Arrays.asList(match, skip, limit);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				BinRange binRange = new BinRange();
				binRange.setBinCodeLow(dbobj.getString("BIN_CODE_LOW"));
				binRange.setBinCodeHigh(dbobj.getString("BIN_CODE_HIGH"));
				binRange.setBinRangeLow(dbobj.getString("BIN_RANGE_LOW"));
				binRange.setBinRangeHigh(dbobj.getString("BIN_RANGE_HIGH"));
				binRange.setCardType(PaymentType.getInstanceIgnoreCase(dbobj.getString("CARD_TYPE")));
				binRange.setMopType(MopType.getInstanceIgnoreCase(dbobj.getString("MOP_TYPE")));
				binRange.setIssuerBankName(dbobj.getString("ISSUER_BANK_NAME"));
				binRange.setIssuerCountry(dbobj.getString("ISSUER_COUNTRY"));
				binRange.setGroupCode(dbobj.getString("GROUP_CODE"));
				binRange.setProductName(dbobj.getString("PRODUCT_NAME"));
				binRange.setRfu1(dbobj.getString("RFU_1"));
				binRange.setRfu2(dbobj.getString("RFU_2"));
				binRangeList.add(binRange);
			}
			cursor.close();
			return binRangeList;
		} catch (Exception ex) {
			logger.error("Caught Exception while BinRange Display : " , ex);
		}

		return binRangeList;
	}

	public int getBinRangTotal(String cardType, String mopType, User user) throws SystemException {

		int total = 0;
		BasicDBObject finalquery = null;
		try {
			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			if (!cardType.equalsIgnoreCase("ALL")) {
				finalList.add(new BasicDBObject("CARD_TYPE", cardType));
			}

			if (!mopType.equalsIgnoreCase("ALL")) {
				finalList.add(new BasicDBObject("MOP_TYPE", mopType));
			}
			if (finalList.isEmpty()) {
				finalquery = new BasicDBObject();
			} else {
				finalquery = new BasicDBObject("$and", finalList);
			}
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.BIN_RANGE_COLLECTION_NAME.getValue()));
			total = (int) coll.count(finalquery);
			logger.info("Inside bin range display service query count , total records from DB  = " + total);
			return total;
		} catch (Exception exception) {
			logger.error("Caught Exception in BinRange Count : " , exception);
		}
		return total;
	}

	public int getBinRangTotalByBinCode(String binCode) throws SystemException {

		BasicDBObject finalquery = null;
		int total = 0;
		try {
			finalquery = new BasicDBObject("BIN_CODE_LOW", binCode);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.BIN_RANGE_COLLECTION_NAME.getValue()));
			total = (int) coll.count(finalquery);
			logger.info("Inside bin range display service query count , total records from DB  = " + total);
			return total;
		} catch (Exception exception) {
			logger.error("Caught Exception while count by binCode in BinRange " , exception);
		}
		return total;
	}

	public int getEMIBinRangTotalByBinCode(String binCode) throws SystemException {

		BasicDBObject finalquery = null;
		int total = 0;
		try {
			finalquery = new BasicDBObject("BIN_CODE_LOW", binCode);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.EMI_BIN_RANGE_COLLECTION_NAME.getValue()));
			total = (int) coll.count(finalquery);
			logger.info("Inside bin range display service query count , total records from DB  = " + total);
			return total;
		} catch (Exception exception) {
			logger.error("Caught Exception while count by binCode in BinRange " , exception);
		}
		return total;
	}
}