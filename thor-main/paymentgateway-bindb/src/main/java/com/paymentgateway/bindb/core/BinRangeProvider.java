package com.paymentgateway.bindb.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.BinRangeDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.BinRange;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class BinRangeProvider {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private BinRangeDao binRangeDao;

	/*
	 * @Autowired private ApiBinRangeProvider defaultBinRage;
	 */
	@Autowired
	private BinRangeParser binRangeParser;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(BinRangeProvider.class.getName());

	@SuppressWarnings("unused")
	public Map<String, String> findBinRange(String cardBin, String payId) {
		Map<String, String> binMap = new HashMap<String, String>();
		try {
			// use dataBase binRange
			// BinRange binRange = new BinRange();

			// First Hit on Bin Server with 6 digits
			// Second Hit on Bin Server with 9 digits

			if (cardBin.length() == 9) {
				List<BinRange> binRangeList = findByBinCodeLow(cardBin.substring(0, 6), payId);
				if (binRangeList.size() == 0) {
					logger.info("Bin not found in DB for Card Bin: " + cardBin);
					return binMap;
				} else if (binRangeList.size() == 1) {
					for (BinRange binRangeDB : binRangeList) {
						binMap = binRangeParser.parseToMap(binRangeDB);
						return binMap;
					}
				} else {
					for (BinRange binRangeDB : binRangeList) {
						int lowBinRange = Integer.valueOf(binRangeDB.getBinRangeLow().substring(0, 9));
						int highBinRange = Integer.valueOf(binRangeDB.getBinRangeHigh().substring(0, 9));
						int cardBinValue = Integer.valueOf(cardBin);

						if (cardBinValue >= lowBinRange && cardBinValue <= highBinRange) {
							binMap = binRangeParser.parseToMap(binRangeDB);
							return binMap;
						}
					}
				}
			} else if (cardBin.length() == 6) {
				List<BinRange> binRangeList = binRangeDao.findByBinCodeLow(cardBin);

				// If only one result found , send response
				if (binRangeList.size() == 1) {

					for (BinRange binRangeDB : binRangeList) {
						// binMap = binRangeParser.parseToMap(binRangeDB);
						return binMap;
					}
				} else {
					return binMap;
				}
			} else {
				return binMap;
			}
		} catch (Exception exception) {
			logger.error("Unable to Process Bin API Request", exception);
		}
		return binMap;
	}

	@SuppressWarnings("unused")
	public Map<String, String> findEMIBinRange(String cardBin) {
		Map<String, String> binMap = new HashMap<String, String>();
		try {
			// use dataBase binRange
			// BinRange binRange = new BinRange();

			// First Hit on Bin Server with 6 digits
			// Second Hit on Bin Server with 9 digits

			if (cardBin.length() == 9) {
				List<BinRange> binRangeList = findByEMIBinCodeLow(cardBin.substring(0, 6));
				if (binRangeList.size() == 0) {
					logger.info("Bin not found in DB for Card Bin: " + cardBin);
					return binMap;
				} else if (binRangeList.size() == 1) {
					for (BinRange binRangeDB : binRangeList) {
						binMap = binRangeParser.parseToMap(binRangeDB);
						return binMap;
					}
				} else {
					for (BinRange binRangeDB : binRangeList) {
						int lowBinRange = Integer.valueOf(binRangeDB.getBinRangeLow().substring(0, 9));
						int highBinRange = Integer.valueOf(binRangeDB.getBinRangeHigh().substring(0, 9));
						int cardBinValue = Integer.valueOf(cardBin);

						if (cardBinValue >= lowBinRange && cardBinValue <= highBinRange) {
							binMap = binRangeParser.parseToMap(binRangeDB);
							return binMap;
						}
					}
				}
			} else if (cardBin.length() == 6) {
				List<BinRange> binRangeList = binRangeDao.findEMIBinCodeLow(cardBin);

				// If only one result found , send response
				if (binRangeList.size() == 1) {

					for (BinRange binRangeDB : binRangeList) {
						// binMap = binRangeParser.parseToMap(binRangeDB);
						return binMap;
					}
				} else {
					return binMap;
				}
			} else {
				return binMap;
			}
		} catch (Exception exception) {
			logger.error("Unable to Process EMI Bin API Request", exception);
		}
		return binMap;
	}

	private List<BinRange> findByBinCodeLow(String binCodeLow, String payId) {
		List<BinRange> binRangeList = new ArrayList<BinRange>();
		try {
			BasicDBObject finalQuery = new BasicDBObject("BIN_CODE_LOW", binCodeLow);
			MongoDatabase dbIns = mongoInstance.getDB();
			String binCollectionName = null;
			if(payId.equalsIgnoreCase(PropertiesManager.propertiesMap.get("FREEBIE_PAY_ID"))) {
				binCollectionName = PropertiesManager.propertiesMap.get(prefix + Constants.FREEBIE_BIN_RANGE_COLLECTION_NAME.getValue());
			} else {
				binCollectionName = PropertiesManager.propertiesMap.get(prefix + Constants.BIN_RANGE_COLLECTION_NAME.getValue());
			}
			MongoCollection<Document> coll = dbIns.getCollection(binCollectionName);
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			while (cursor.hasNext()) {
				BinRange bin = new BinRange();
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
				binRangeList.add(bin);
			}
			cursor.close();
			return binRangeList;
		} catch (Exception ex) {
			logger.error("Exception while get the bin Code Low from MongoDB : " , ex);
		}
		return binRangeList;
	}

	private List<BinRange> findByEMIBinCodeLow(String binCodeLow) {
		List<BinRange> binRangeList = new ArrayList<BinRange>();
		try {
			BasicDBObject finalQuery = new BasicDBObject("BIN_CODE_LOW", binCodeLow);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.EMI_BIN_RANGE_COLLECTION_NAME.getValue()));
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			while (cursor.hasNext()) {
				BinRange bin = new BinRange();
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
				binRangeList.add(bin);
			}
			return binRangeList;
		} catch (Exception ex) {
			logger.error("Exception while get the EMI bin Code Low from MongoDB : " , ex);
		}
		return binRangeList;
	}
}
