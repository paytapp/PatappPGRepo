/**
 * This will update final status of status inquire and also initiate auto refunds
 */
package com.paymentgateway.scheduler.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.CurrencyTypes;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.scheduler.core.ServiceControllerProvider;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class UpdateRefundTransactions {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ConfigurationProvider configurationProvider;

	@Autowired
	private ServiceControllerProvider serviceControllerProvider;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;

	private static final Logger logger = LoggerFactory.getLogger(UpdateRefundTransactions.class);

	public void updateAndRefund(List<Document> finalStatusList, List<Document> autoRefundableList) {
		logger.info("Inside updateAndRefund(), UpdateRefundTransactions");
		if (!finalStatusList.isEmpty()) {
			logger.info("Updating latest status of " + finalStatusList.size() + " transaction/s into DB");
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(configurationProvider.getMONGO_DB_collectionName());
			for (Document doc : finalStatusList) {
				List<BasicDBObject> conditions = new ArrayList<BasicDBObject>();
				conditions.add(new BasicDBObject(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName())));
				conditions.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName())));
				conditions.add(new BasicDBObject(FieldType.STATUS.getName(), doc.getString(FieldType.STATUS.getName())));
				if (StringUtils.isNotBlank(doc.getString(FieldType.ORIG_TXNTYPE.getName()))) {
					if (doc.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase("SALE")) {
						conditions.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), "SALE"));
						conditions.add(new BasicDBObject(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName())));
					}else {
						conditions.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), "REFUND"));
						conditions.add(new BasicDBObject(FieldType.REFUND_ORDER_ID.getName(), doc.getString(FieldType.REFUND_ORDER_ID.getName())));
					}
				} else {
					if (doc.getString(FieldType.TXNTYPE.getName()).equalsIgnoreCase("SALE")) {
						conditions.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "SALE"));
						conditions.add(new BasicDBObject(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName())));
					}else {
						conditions.add(new BasicDBObject(FieldType.REFUND_ORDER_ID.getName(), doc.getString(FieldType.REFUND_ORDER_ID.getName())));
						conditions.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "REFUND"));
					}
				}
				BasicDBObject query = new BasicDBObject("$and", conditions);
				BasicDBObject match = new BasicDBObject("$match", query);
				List<BasicDBObject> pipeline = Arrays.asList(match);
				AggregateIterable<Document> output = collection.aggregate(pipeline);
				output.allowDiskUse(true);
				MongoCursor<Document> cursor = output.iterator();
				if (!cursor.hasNext()) {
					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

						collection.insertOne(dataEncDecTool.encryptDocument(doc));
					} else {
						collection.insertOne(doc);
					}
					//collection.insertOne(doc);
				}
				cursor.close();
			}
		}

		if (!autoRefundableList.isEmpty()) {
			logger.info("Initiating auto refunds of " + autoRefundableList.size() + " transactions");
			for (Document refundableData : autoRefundableList) {
				String refundRequest = getRefundRequest(refundableData);
				serviceControllerProvider.initiateRefund(refundRequest);
			}
		}
	}

	private String getRefundRequest(Document refundableData) {
		JSONObject refundRequest = new JSONObject();
		refundRequest.put(FieldType.PG_REF_NUM.getName(), refundableData.getString(FieldType.PG_REF_NUM.getName()));
		refundRequest.put(FieldType.REFUND_FLAG.getName(), "R");
		refundRequest.put(FieldType.AMOUNT.getName(), refundableData.getString(FieldType.TOTAL_AMOUNT.getName()));
		refundRequest.put(FieldType.CURRENCY_CODE.getName(), CurrencyTypes.INR.getCode());
		refundRequest.put(FieldType.TXNTYPE.getName(), TransactionType.REFUND.toString());
		refundRequest.put(FieldType.ORDER_ID.getName(), refundableData.getString(FieldType.ORDER_ID.getName()));
		refundRequest.put(FieldType.REFUND_ORDER_ID.getName(), TransactionManager.getNewTransactionId());
		refundRequest.put(FieldType.ORDER_ID.getName(), refundableData.getString(FieldType.ORDER_ID.getName()));
		refundRequest.put(FieldType.PAY_ID.getName(), refundableData.getString(FieldType.PAY_ID.getName()));
		return refundRequest.toString();
	}
}
