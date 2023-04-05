package com.paymentgateway.commons.dao;

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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.BinRange;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class BinRangeDao {

	private static Logger logger = LoggerFactory.getLogger(BinRangeDao.class.getName());

	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	public List<BinRange> findBinCodeHigh(String binCodeHigh) {
		return findByBinCodeHigh(binCodeHigh);

	}

	public List<BinRange> findBinCodeLow(String binCodeLow) {
		return findByBinCodeLow(binCodeLow);

	}

	@SuppressWarnings("unchecked")
	public List<BinRange> findByBinCodeLow(String binCodeLow) {
		BinRange bin = new BinRange();
		List<BinRange> binRangeList = new ArrayList<BinRange>();
		try {

			BasicDBObject finalQuery = new BasicDBObject("BIN_CODE_LOW", binCodeLow);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.BIN_RANGE_COLLECTION_NAME.getValue()));
			/*
			 * List<BasicDBObject> pipeline = Arrays.asList(finalQuery);
			 * AggregateIterable<Document> output = coll.aggregate(pipeline);
			 * output.allowDiskUse(true);
			 */
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				bin.setBinCodeHigh(dbobj.getString("BIN_CODE_HIGH"));
				bin.setBinCodeLow(dbobj.getString("BIN_CODE_LOW"));
				bin.setBinRangeHigh(dbobj.getString("BIN_RANGE_HIGH"));
				bin.setBinRangeLow(dbobj.getString("BIN_RANGE_LOW"));
				bin.setCardType(PaymentType.getInstanceIgnoreCase(dbobj.getString("CARD_TYPE")));
				bin.setGroupCode(dbobj.getString("GROUP_CODE"));
				bin.setIssuerBankName(dbobj.getString("ISSUER_BANK_NAME"));
				bin.setIssuerCountry(dbobj.getString("ISSUER_COUNTRY"));
				bin.setMopType(MopType.getInstanceIgnoreCase(dbobj.getString("MOP_TYPE")));
				bin.setCardHolder(dbobj.getString("RFU_2"));
				bin.setPaymentRegion(dbobj.getString("RFU_1"));
				
				/*bin.setRfu1(dbobj.getString("RFU_1"));
				bin.setRfu2(dbobj.getString("RFU_2"));*/

				binRangeList.add(bin);
			}
			return binRangeList;
		} catch (Exception ex) {
			logger.error("Exception while get the bin Code Low from MongoDB : " , ex);
		}
		return binRangeList;

	}
	
	@SuppressWarnings("unchecked")
	public List<BinRange> findEMIBinByBinCodeLow(String binCodeLow) {
		BinRange bin = new BinRange();
		List<BinRange> binRangeList = new ArrayList<BinRange>();
		try {

			BasicDBObject finalQuery = new BasicDBObject("BIN_CODE_LOW", binCodeLow);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.EMI_BIN_RANGE_COLLECTION_NAME.getValue()));
			/*
			 * List<BasicDBObject> pipeline = Arrays.asList(finalQuery);
			 * AggregateIterable<Document> output = coll.aggregate(pipeline);
			 * output.allowDiskUse(true);
			 */
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				bin.setBinCodeHigh(dbobj.getString("BIN_CODE_HIGH"));
				bin.setBinCodeLow(dbobj.getString("BIN_CODE_LOW"));
				bin.setBinRangeHigh(dbobj.getString("BIN_RANGE_HIGH"));
				bin.setBinRangeLow(dbobj.getString("BIN_RANGE_LOW"));
				bin.setCardType(PaymentType.getInstanceIgnoreCase(dbobj.getString("CARD_TYPE")));
				bin.setGroupCode(dbobj.getString("GROUP_CODE"));
				bin.setIssuerBankName(dbobj.getString("ISSUER_BANK_NAME"));
				bin.setIssuerCountry(dbobj.getString("ISSUER_COUNTRY"));
				bin.setMopType(MopType.getInstanceIgnoreCase(dbobj.getString("MOP_TYPE")));
				bin.setCardHolder(dbobj.getString("RFU_2"));
				bin.setPaymentRegion(dbobj.getString("RFU_1"));
				
				/*bin.setRfu1(dbobj.getString("RFU_1"));
				bin.setRfu2(dbobj.getString("RFU_2"));*/

				binRangeList.add(bin);
			}
			return binRangeList;
		} catch (Exception ex) {
			logger.error("Exception while get the bin Code Low from MongoDB : " , ex);
		}
		return binRangeList;

	}

	@SuppressWarnings("unchecked")
	public List<BinRange> findEMIBinCodeLow(String binCodeLow) {
		BinRange bin = new BinRange();
		List<BinRange> binRangeList = new ArrayList<BinRange>();
		try {

			BasicDBObject finalQuery = new BasicDBObject("BIN_CODE_LOW", binCodeLow);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.EMI_BIN_RANGE_COLLECTION_NAME.getValue()));
			/*
			 * List<BasicDBObject> pipeline = Arrays.asList(finalQuery);
			 * AggregateIterable<Document> output = coll.aggregate(pipeline);
			 * output.allowDiskUse(true);
			 */
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				bin.setBinCodeHigh(dbobj.getString("BIN_CODE_HIGH"));
				bin.setBinCodeLow(dbobj.getString("BIN_CODE_LOW"));
				bin.setBinRangeHigh(dbobj.getString("BIN_RANGE_HIGH"));
				bin.setBinRangeLow(dbobj.getString("BIN_RANGE_LOW"));
				bin.setCardType(PaymentType.getInstanceIgnoreCase(dbobj.getString("CARD_TYPE")));
				bin.setGroupCode(dbobj.getString("GROUP_CODE"));
				bin.setIssuerBankName(dbobj.getString("ISSUER_BANK_NAME"));
				bin.setIssuerCountry(dbobj.getString("ISSUER_COUNTRY"));
				bin.setMopType(MopType.getInstanceIgnoreCase(dbobj.getString("MOP_TYPE")));
				bin.setCardHolder(dbobj.getString("RFU_2"));
				bin.setPaymentRegion(dbobj.getString("RFU_1"));
				
				/*bin.setRfu1(dbobj.getString("RFU_1"));
				bin.setRfu2(dbobj.getString("RFU_2"));*/

				binRangeList.add(bin);
			}
			return binRangeList;
		} catch (Exception ex) {
			logger.error("Exception while get the EMI bin Code Low from MongoDB : " , ex);
		}
		return binRangeList;

	}
	
	@SuppressWarnings("unchecked")
	public List<BinRange> findByBinCodeHigh(String binCodeHigh) {

		BinRange bin = new BinRange();
		List<BinRange> binRangeList = new ArrayList<BinRange>();
		try {
			BasicDBObject finalQuery = new BasicDBObject("BIN_CODE_HIGH", binCodeHigh);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + "binRange"));
			List<BasicDBObject> pipeline = Arrays.asList(finalQuery);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				bin.setBinCodeHigh(dbobj.getString("BIN_CODE_HIGH"));
				bin.setBinCodeLow(dbobj.getString("BIN_CODE_LOW"));
				bin.setBinRangeHigh(dbobj.getString("BIN_RANGE_HIGH"));
				bin.setBinRangeLow(dbobj.getString("BIN_RANGE_LOW"));
				bin.setCardType(PaymentType.getInstanceIgnoreCase(dbobj.getString("CARD_TYPE")));
				bin.setGroupCode(dbobj.getString("GROUP_CODE"));
				bin.setIssuerBankName(dbobj.getString("ISSUER_BANK_NAME"));
				bin.setIssuerCountry(dbobj.getString("ISSUER_COUNTRY"));
				bin.setMopType(MopType.getInstanceIgnoreCase(dbobj.getString("MOP_TYPE")));
				bin.setProductName(dbobj.getString("PRODUCT_NAME"));
				bin.setRfu1(dbobj.getString("RFU_1"));
				bin.setRfu2(dbobj.getString("RFU_2"));
			}
			return binRangeList;
		} catch (Exception ex) {
			logger.error("Exception while get the bin Code Low from MongoDB : " , ex);
		}
		return binRangeList;

	}

	public String insertAll(List<BinRange> binListObj) {

		StringBuilder message = new StringBuilder();
	
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.BIN_RANGE_COLLECTION_NAME.getValue()));
			
			List<Document> docList = new ArrayList<Document>();
			int count = 0 ;
			for (BinRange binRangeObj : binListObj) {
				Document bin = new Document();
			//	bin.put("_id", TransactionManager.getNewTransactionId());
				bin.put("BIN_CODE_HIGH", binRangeObj.getBinCodeHigh());
				bin.put("BIN_CODE_LOW", binRangeObj.getBinCodeLow());
				bin.put("BIN_RANGE_HIGH", binRangeObj.getBinRangeHigh());
				bin.put("BIN_RANGE_LOW", binRangeObj.getBinRangeLow());
				bin.put("CARD_TYPE", binRangeObj.getCardType().toString());
				bin.put("GROUP_CODE", binRangeObj.getGroupCode());
				bin.put("ISSUER_BANK_NAME", binRangeObj.getIssuerBankName());
				bin.put("ISSUER_COUNTRY", binRangeObj.getIssuerCountry());
				bin.put("MOP_TYPE", binRangeObj.getMopType().toString());
				bin.put("PRODUCT_NAME", binRangeObj.getProductName());
				bin.put("RFU_1", binRangeObj.getRfu1());
				bin.put("RFU_2", binRangeObj.getRfu2());
				Document doc = new Document(bin);
				
				docList.add(doc);
				message.append((CrmFieldConstants.PROCESS_INITIATED_SUCCESSFULLY.getValue()));
				count = count + 1;
				if (count == 100000) {
					coll.insertMany(docList);
					docList.clear();
					logger.info("100000 bins inserted");
					count = 0;
				}
			}
			if (docList.size() > 0) {
				coll.insertMany(docList);
				logger.info(docList.size()+" bins inserted");
			}
			
			message.append("Inserted all bins Successfully");
		} catch (Exception exception) {
			message.append(ErrorType.CSV_NOT_SUCCESSFULLY_UPLOAD.getResponseMessage());
			logger.error("Error while processing binRange : " , exception);
		}
		return message.toString();
	}
	
	public String emiInsertAll(List<BinRange> binListObj) {

		StringBuilder message = new StringBuilder();
	
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.EMI_BIN_RANGE_COLLECTION_NAME.getValue()));
			
			List<Document> docList = new ArrayList<Document>();
			int count = 0 ;
			for (BinRange binRangeObj : binListObj) {
				Document bin = new Document();
			//	bin.put("_id", TransactionManager.getNewTransactionId());
				bin.put("BIN_CODE_HIGH", binRangeObj.getBinCodeHigh());
				bin.put("BIN_CODE_LOW", binRangeObj.getBinCodeLow());
				bin.put("BIN_RANGE_HIGH", binRangeObj.getBinRangeHigh());
				bin.put("BIN_RANGE_LOW", binRangeObj.getBinRangeLow());
				bin.put("CARD_TYPE", binRangeObj.getCardType().toString());
				bin.put("GROUP_CODE", binRangeObj.getGroupCode());
				bin.put("ISSUER_BANK_NAME", binRangeObj.getIssuerBankName());
				bin.put("ISSUER_COUNTRY", binRangeObj.getIssuerCountry());
				bin.put("MOP_TYPE", binRangeObj.getMopType().toString());
				bin.put("PRODUCT_NAME", binRangeObj.getProductName());
				bin.put("RFU_1", binRangeObj.getRfu1());
				bin.put("RFU_2", binRangeObj.getRfu2());
				Document doc = new Document(bin);
				
				docList.add(doc);
				message.append((CrmFieldConstants.PROCESS_INITIATED_SUCCESSFULLY.getValue()));
				count = count + 1;
				if (count == 100000) {
					coll.insertMany(docList);
					docList.clear();
					logger.info("100000 bins inserted");
					count = 0;
				}
			}
			if (docList.size() > 0) {
				coll.insertMany(docList);
				logger.info(docList.size()+" bins inserted");
			}
			
			message.append("Inserted all bins Successfully");
		} catch (Exception exception) {
			message.append(ErrorType.CSV_NOT_SUCCESSFULLY_UPLOAD.getResponseMessage());
			logger.error("Error while processing binRange: " , exception);
		}
		return message.toString();
	}
}
