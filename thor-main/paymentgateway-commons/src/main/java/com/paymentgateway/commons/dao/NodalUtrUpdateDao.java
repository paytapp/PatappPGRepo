package com.paymentgateway.commons.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class NodalUtrUpdateDao {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	private static final String prefix = "MONGO_DB_";
	SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");

	private static Logger logger = LoggerFactory.getLogger(NodalUtrUpdateDao.class.getName());

	public int updateSettledTxnWithUtr(String dateFrom, String dateTo, String payOutDate, String[] acquirerCodeArray,
			String[] mopTypeArray, String[] paymentTypeArray, String payId, String subMerchantId, String utrNo)
			throws Exception {
		logger.info("Inside updateUTRAndPayoutDate : ");
		int count = 0;
		try {

			BasicDBObject acquirerTypeQuery = new BasicDBObject();
			List<BasicDBObject> acquirerTypeList = new ArrayList<BasicDBObject>();
			BasicDBObject mopeTypeQuery = new BasicDBObject();
			List<BasicDBObject> mopeTypeList = new ArrayList<BasicDBObject>();
			BasicDBObject paymentTypeQuery = new BasicDBObject();
			List<BasicDBObject> paymentTypeList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> txnTypeList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();

			if (!dateFrom.isEmpty() && !dateTo.isEmpty()) {

				dateQuery.put(FieldType.PG_DATE_TIME.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
			}

			if (acquirerCodeArray.length != 0) {
				for (String acquirerCode : acquirerCodeArray) {
					acquirerTypeList.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acquirerCode));
				}
				if (!acquirerTypeList.isEmpty()) {
					acquirerTypeQuery.put("$or", acquirerTypeList);
				}
			}
			if (mopTypeArray.length != 0) {
				for (String mopType : mopTypeArray) {
					mopeTypeList.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), mopType));
				}
				if (!mopeTypeList.isEmpty()) {
					mopeTypeQuery.put("$or", mopeTypeList);
				}
			}
			if (paymentTypeArray.length != 0) {
				for (String paymentType : paymentTypeArray) {
					paymentTypeList.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
				}
				if (!paymentTypeList.isEmpty()) {
					paymentTypeQuery.put("$or", paymentTypeList);
				}
			}

			allConditionQueryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			txnTypeList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			txnTypeList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));

			allConditionQueryList.add(new BasicDBObject("$or", txnTypeList));

			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				allConditionQueryList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}
			BasicDBObject payOutCheckQuery = new BasicDBObject();
			payOutCheckQuery.append(FieldType.PAYOUT_DATE.getName(), new BasicDBObject("$exists", false));
			allConditionQueryList.add(payOutCheckQuery);

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			if (!acquirerTypeQuery.isEmpty()) {
				allConditionQueryList.add(acquirerTypeQuery);
			}
			if (!mopeTypeQuery.isEmpty()) {
				allConditionQueryList.add(mopeTypeQuery);
			}
			if (!paymentTypeQuery.isEmpty()) {
				allConditionQueryList.add(paymentTypeQuery);
			}

			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					finalList.add(allConditionQueryObj);
				}
			}
			BasicDBObject finalquery = new BasicDBObject("$and", finalList);
			logger.info("Query Created");
			MongoDatabase dbIns = mongoInstance.getDB();
			logger.info("final Query updateSettledTxnWithUtr() " + finalquery);
			MongoCollection<Document> collection = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = collection.find(finalquery).iterator();

			while (cursor.hasNext()) {
				Document preDocument = (Document) cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					preDocument = dataEncDecTool.decryptDocument(preDocument);
				}

				BasicDBObject oldFieldsObj = new BasicDBObject();
				oldFieldsObj.put(FieldType.TXN_ID.getName(), preDocument.getString(FieldType.TXN_ID.getName()));

				Bson filter = new Document(FieldType.TXN_ID.getName(),
						preDocument.getString(FieldType.TXN_ID.getName()));

				Bson newValue;

				if (preDocument.getString(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUNDRECO.getName())) {
					newValue = new Document(FieldType.PAYOUT_DATE.getName(), payOutDate);
				} else {
					newValue = new Document(FieldType.PAYOUT_DATE.getName(), payOutDate)
							.append(FieldType.UTR_NO.getName(), utrNo);
				}

				Bson updateDocument = new Document("$set", newValue);
				collection.updateOne(filter, updateDocument);
				// updating UTR in in Sale Transaction collection.
				updateUtrInSaleTxn(preDocument.getString(FieldType.OID.getName()),
						preDocument.getString(FieldType.ORIG_TXNTYPE.getName()), payOutDate, utrNo);

				// updating UTR in transationStatus collection.
				Document latestStatusDoc = getDataForOid(preDocument.getString(FieldType.OID.getName()),
						preDocument.getString(FieldType.ORIG_TXNTYPE.getName()),
						preDocument.getString(FieldType.PG_REF_NUM.getName()));

				if (latestStatusDoc != null) {
					if (latestStatusDoc.getString(FieldType.TXNTYPE.getName())
							.equals(TransactionType.REFUND.getName())) {
						latestStatusDoc.put(FieldType.PAYOUT_DATE.getName(), payOutDate);
					} else {
						latestStatusDoc.put(FieldType.PAYOUT_DATE.getName(), payOutDate);
						latestStatusDoc.put(FieldType.UTR_NO.getName(), utrNo);
					}

					MongoCollection<Document> collection1 = dbIns.getCollection(PropertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

					Document latestOldDoc = new Document(oldFieldsObj);
					Document latestNewDoc = new Document("$set", latestStatusDoc);
					collection1.updateOne(latestOldDoc, latestNewDoc);

				}
				count++;
			}
			return count;

		} catch (Exception exception) {
			String message = "Error while Updating UTR number in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

	}

	private void updateUtrInSaleTxn(String OID, String txnType, String payOutDate, String utrNo) {

		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
		BasicDBObject payOutCheckQuery = new BasicDBObject();
		payOutCheckQuery.append(FieldType.PAYOUT_DATE.getName(), new BasicDBObject("$exists", false));

		queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));
		queryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
		queryList.add(payOutCheckQuery);
		queryList.add(new BasicDBObject(FieldType.OID.getName(), OID));
		BasicDBObject andQuery = new BasicDBObject("$and", queryList);

		MongoDatabase dbIns = mongoInstance.getDB();
		logger.info("Below MongoDatabase dbIns = mongoInstance.getDB()");

		MongoCollection<Document> collection = dbIns
				.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		MongoCursor<Document> cursor = collection.find(andQuery).iterator();
		while (cursor.hasNext()) {
			Document oldDoc = (Document) cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				oldDoc = dataEncDecTool.decryptDocument(oldDoc);
			}

			Bson filter = new Document(FieldType.TXN_ID.getName(), oldDoc.getString(FieldType.TXN_ID.getName()));
			Bson newValue;
			if (txnType.equalsIgnoreCase(TransactionType.REFUND.getName())) {
				newValue = new Document(FieldType.PAYOUT_DATE.getName(), payOutDate);
			} else {
				newValue = new Document(FieldType.PAYOUT_DATE.getName(), payOutDate).append(FieldType.UTR_NO.getName(),
						utrNo);
			}

			Bson updateDocument = new Document("$set", newValue);
			collection.updateOne(filter, updateDocument);

		}
	}

	public Document getDataForOid(String oid, String txnType, String pgRefNum) {
		logger.info("Inside getDataForOid : ");
		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
		queryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
		queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));
		queryList.add(new BasicDBObject(FieldType.SETTLEMENT_FLAG.getName(), "Y"));
		queryList.add(new BasicDBObject(FieldType.OID.getName(), oid));
		if (txnType.equalsIgnoreCase(TransactionType.REFUND.getName())) {
			queryList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
		}
		BasicDBObject andQuery = new BasicDBObject("$and", queryList);

		MongoDatabase dbIns = mongoInstance.getDB();
		logger.info("Below MongoDatabase dbIns = mongoInstance.getDB()");
		MongoCollection<Document> collection = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
		MongoCursor<Document> cursor = collection.find(andQuery).iterator();
		while (cursor.hasNext()) {
			Document documentObj = (Document) cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
			}

			return documentObj;
		}

		return null;
	}

	public void updateBulkSettledTxnWithUtr(String payOutDate, String payId, String subMerchantId, String utr,
			boolean idfcUpiSettlementFlag) {
		logger.info("inside updateBulkSettledTxnWithUtr()");

		try {

			BasicDBObject acquirerTypeQuery = new BasicDBObject();

			List<BasicDBObject> txnTypeList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();

			allConditionQueryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			txnTypeList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			txnTypeList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));

			allConditionQueryList.add(new BasicDBObject("$or", txnTypeList));

			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL") && !subMerchantId.equalsIgnoreCase("NA") ) {
				allConditionQueryList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}

			if (idfcUpiSettlementFlag) {
				acquirerTypeQuery.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.IDFCUPI.getName());
			}else{
				acquirerTypeQuery.put(FieldType.ACQUIRER_TYPE.getName(),new BasicDBObject("$ne",AcquirerType.IDFCUPI.getName()));
			}

			BasicDBObject payOutCheckQuery = new BasicDBObject();
			payOutCheckQuery.append(FieldType.PAYOUT_DATE.getName(), new BasicDBObject("$exists", false));

			allConditionQueryList.add(payOutCheckQuery);

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			if (!acquirerTypeQuery.isEmpty()) {
				allConditionQueryList.add(acquirerTypeQuery);
			}

			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					finalList.add(allConditionQueryObj);
				}
			}
			BasicDBObject finalquery = new BasicDBObject("$and", finalList);
			logger.info("Query Created");
			MongoDatabase dbIns = mongoInstance.getDB();
			logger.info("final Query updateSettledTxnWithUtr() " + finalquery);
			MongoCollection<Document> collection = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			long totalTxn = collection.count(finalquery);

			logger.info("total settled txn = " + totalTxn + " for merchant Id = " + payId + " subMerchant = "
					+ subMerchantId + " settlement date " + payOutDate);

			MongoCursor<Document> cursor = collection.find(finalquery).iterator();
			while (cursor.hasNext()) {
				Document preDocument = (Document) cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					preDocument = dataEncDecTool.decryptDocument(preDocument);
				}

				BasicDBObject oldFieldsObj = new BasicDBObject();
				oldFieldsObj.put(FieldType.TXN_ID.getName(), preDocument.getString(FieldType.TXN_ID.getName()));

				Bson filter = new Document(FieldType.TXN_ID.getName(),
						preDocument.getString(FieldType.TXN_ID.getName()));

				Bson newValue;

				if (preDocument.getString(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUNDRECO.getName())) {
					newValue = new Document(FieldType.PAYOUT_DATE.getName(), payOutDate);
				} else {
					newValue = new Document(FieldType.PAYOUT_DATE.getName(), payOutDate)
							.append(FieldType.UTR_NO.getName(), utr);
				}

				Bson updateDocument = new Document("$set", newValue);
				collection.updateOne(filter, updateDocument);
				// updating UTR in in Sale Transaction collection.
				updateUtrInSaleTxn(preDocument.getString(FieldType.OID.getName()),
						preDocument.getString(FieldType.ORIG_TXNTYPE.getName()), payOutDate, utr);

				// updating UTR in transationStatus collection.
				Document latestStatusDoc = getDataForOid(preDocument.getString(FieldType.OID.getName()),
						preDocument.getString(FieldType.ORIG_TXNTYPE.getName()),
						preDocument.getString(FieldType.PG_REF_NUM.getName()));

				if (latestStatusDoc != null) {
					if (latestStatusDoc.getString(FieldType.TXNTYPE.getName())
							.equals(TransactionType.REFUND.getName())) {
						latestStatusDoc.put(FieldType.PAYOUT_DATE.getName(), payOutDate);
					} else {
						latestStatusDoc.put(FieldType.PAYOUT_DATE.getName(), payOutDate);
						latestStatusDoc.put(FieldType.UTR_NO.getName(), utr);
					}

					MongoCollection<Document> collection1 = dbIns.getCollection(PropertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

					Document latestOldDoc = new Document(oldFieldsObj);
					Document latestNewDoc = new Document("$set", latestStatusDoc);
					collection1.updateOne(latestOldDoc, latestNewDoc);

				}
			}

		} catch (Exception e) {
			logger.error("excepton inside updateBulkSettledTxnWithUtr()", e);
		}

	}
}
