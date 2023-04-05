package com.paymentgateway.scheduler.commons;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.scheduler.tasks.TransactionReportSmsJob;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class StatusEnquiryDataProvider {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ConfigurationProvider configurationProvider;

	private static final Logger logger = LoggerFactory.getLogger(TransactionReportSmsJob.class);

	public List<Document> fetchData(String startTime, String endTime, String merchantPayId, String acquirerType,
			String transactionType, String paymentType, JSONArray statusTypeArray) {
		try {
			List<Document> saleTransactionList = new ArrayList<Document>();
			List<Document> refundTransactionList = new ArrayList<Document>();
			List<Document> saleRefundTransactionList = new ArrayList<Document>();
			for (int i = 0; i < statusTypeArray.length(); i++) {
				String statusType = statusTypeArray.getString(i);
				logger.info("Started fetching data for status enquiry, where start time is " + startTime
						+ " and end time is " + endTime + " also the merchant Pay ID is " + merchantPayId
						+ " along with acquirer " + acquirerType + " and transaction type " + transactionType
						+ " payment type " + paymentType + " and status type " + statusType);

				Set<String> oidSet = new HashSet<String>();
				Set<String> refundOidSet = new HashSet<String>();

				List<Document> transactionList = new ArrayList<Document>();
				List<BasicDBObject> firstConditionList = new ArrayList<BasicDBObject>();
				List<BasicDBObject> secondConditionList = new ArrayList<BasicDBObject>();

				BasicDBObject dateTimeQuery = new BasicDBObject();
				BasicDBObject firstStatusConditionalQuery = new BasicDBObject();
				BasicDBObject secondStatusConditionalQuery = new BasicDBObject();
				BasicDBObject firstQuery = new BasicDBObject();
				BasicDBObject secondQuery = new BasicDBObject();

				dateTimeQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startTime).toLocalizedPattern())
								.add("$lt", new SimpleDateFormat(endTime).toLocalizedPattern()).get());
				firstStatusConditionalQuery.put("STATUS", new BasicDBObject("$ne", "Captured"));
				secondStatusConditionalQuery.put("STATUS", new BasicDBObject("$ne", "Settled"));
				secondConditionList.add(firstStatusConditionalQuery);
				secondConditionList.add(secondStatusConditionalQuery);

				if (!transactionType.equalsIgnoreCase("ALL")) {
					firstConditionList.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
				}

				if (!transactionType.equalsIgnoreCase("ALL")) {
					firstConditionList.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acquirerType));
				}

				if (!transactionType.equalsIgnoreCase("ALL")) {
					firstConditionList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), transactionType));
				}

				if (!paymentType.equalsIgnoreCase("ALL")) {
					firstConditionList.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
				}

				if (!transactionType.equalsIgnoreCase("ALL")) {
					firstConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), statusType));
				}

				if (!firstConditionList.isEmpty()) {
					firstQuery = new BasicDBObject("$and", firstConditionList);
				}
				if (!firstConditionList.isEmpty()) {
					secondQuery = new BasicDBObject("$and", secondConditionList);
				}

				List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

				if (!dateTimeQuery.isEmpty()) {
					allConditionQueryList.add(dateTimeQuery);
					allConditionQueryList.add(secondQuery);
				}

				List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

				if (!firstQuery.isEmpty()) {
					fianlList.add(firstQuery);
				}
				if (!allConditionQueryList.isEmpty()) {
					BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
					if (!allConditionQueryObj.isEmpty()) {
						fianlList.add(allConditionQueryObj);
					}
				}

				BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> collection = dbIns
						.getCollection(configurationProvider.getMONGO_DB_collectionName());

				BasicDBObject match = new BasicDBObject("$match", finalquery);

				List<BasicDBObject> pipeline = Arrays.asList(match);
				AggregateIterable<Document> output = collection.aggregate(pipeline);
				output.allowDiskUse(true);
				logger.info("Query prepared for fetching unique OID from DB, query : " + finalquery.toString());
				MongoCursor<Document> cursor = output.iterator();
				while (cursor.hasNext()) {
					Document dbobj = cursor.next();
					oidSet.add(dbobj.getString(FieldType.OID.getName()));
				}
				cursor.close();

				logger.info("Set of unique OIDs prepared with total number of OID : " + oidSet.size());

				List<BasicDBObject> transactionConditionList = new ArrayList<BasicDBObject>();
				BasicDBObject transactionConditionQuery = new BasicDBObject();

				for (String oid : oidSet) {
					transactionConditionList.clear();
					transactionConditionQuery.clear();

					transactionConditionList.add(new BasicDBObject(FieldType.OID.getName(), oid));

					if (!paymentType.equalsIgnoreCase("ALL")) {
						firstConditionList.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
					}

					transactionConditionQuery = new BasicDBObject("$and", transactionConditionList);

					MongoDatabase dbIns1 = mongoInstance.getDB();
					MongoCollection<Document> collection1 = dbIns1
							.getCollection(configurationProvider.getMONGO_DB_collectionName());

					BasicDBObject match1 = new BasicDBObject("$match", transactionConditionQuery);
					BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
					BasicDBObject limit = new BasicDBObject("$limit", 1);

					List<BasicDBObject> pipeline1 = Arrays.asList(match1, sort, limit);
					AggregateIterable<Document> output1 = collection1.aggregate(pipeline1);
					output1.allowDiskUse(true);
					statusTypeArray.toString();
					MongoCursor<Document> cursor1 = output1.iterator();
					while (cursor1.hasNext()) {
						Document dbobj = cursor1.next();
						if (statusTypeArray.toString().contains(dbobj.getString(FieldType.STATUS.getName()))) {
							transactionList.add(dbobj);
						}
					}
					cursor1.close();
				}
				if (!transactionList.isEmpty()) {
					if (transactionType.equalsIgnoreCase(TransactionType.SALE.getName())) {
						for (Document document : transactionList) {
							String txnType = null;
							if (document.getString(FieldType.ORIG_TXNTYPE.getName()) != null
									&& !document.getString(FieldType.ORIG_TXNTYPE.getName()).isEmpty()) {
								txnType = document.getString(FieldType.ORIG_TXNTYPE.getName());
							} else {
								txnType = document.getString(FieldType.TXNTYPE.getName());
							}
							if (txnType.equalsIgnoreCase(TransactionType.SALE.getName())) {
								saleTransactionList.add(document);
							}
						}
					}

					if (transactionType.equalsIgnoreCase(TransactionType.REFUND.getName())) {
						String txnType;
						for (Document document : transactionList) {
							if (document.getString(FieldType.ORIG_TXNTYPE.getName()) != null
									&& !document.getString(FieldType.ORIG_TXNTYPE.getName()).isEmpty()) {
								txnType = document.getString(FieldType.ORIG_TXNTYPE.getName());
							} else {
								txnType = document.getString(FieldType.TXNTYPE.getName());
							}
							if (txnType.equalsIgnoreCase(TransactionType.REFUND.getName())) {
								refundOidSet.add(document.getString(FieldType.REFUND_ORDER_ID.getName()));
							}
						}

						logger.info("Set of unique REFUND_ORDER_ID created with size : " + refundOidSet.size());
						List<BasicDBObject> refundConditionList = new ArrayList<BasicDBObject>();
						BasicDBObject refundConditionQuery = new BasicDBObject();

						for (String rOid : refundOidSet) {
							refundConditionList.clear();
							refundConditionQuery.clear();

							refundConditionList.add(new BasicDBObject(FieldType.REFUND_ORDER_ID.getName(), rOid));

							if (!paymentType.equalsIgnoreCase("ALL")) {
								refundConditionList
										.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
							}
							if (!paymentType.equalsIgnoreCase("ALL")) {
								refundConditionList
										.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), transactionType));
							}

							refundConditionQuery = new BasicDBObject("$and", refundConditionList);

							MongoDatabase dbIns1 = mongoInstance.getDB();
							MongoCollection<Document> collection1 = dbIns1
									.getCollection(configurationProvider.getMONGO_DB_collectionName());

							BasicDBObject match1 = new BasicDBObject("$match", refundConditionQuery);
							BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
							BasicDBObject limit = new BasicDBObject("$limit", 1);

							List<BasicDBObject> pipeline1 = Arrays.asList(match1, sort, limit);
							AggregateIterable<Document> output1 = collection1.aggregate(pipeline1);
							output1.allowDiskUse(true);

							MongoCursor<Document> cursor1 = output1.iterator();
							while (cursor1.hasNext()) {
								Document dbobj = cursor1.next();
								if (statusTypeArray.toString().contains(dbobj.getString(FieldType.STATUS.getName()))) {
									transactionList.add(dbobj);
								}
							}
							cursor1.close();
						}
					}
				}
			}
			if (transactionType.equalsIgnoreCase(TransactionType.SALE.getName())) {
				return saleTransactionList;
			} else if (transactionType.equalsIgnoreCase(TransactionType.REFUND.getName())) {
				return refundTransactionList;
			} else if (transactionType.equalsIgnoreCase("ALL")) {
				saleRefundTransactionList.addAll(saleTransactionList);
				saleRefundTransactionList.addAll(refundTransactionList);
				return saleRefundTransactionList;
			}
		} catch (Exception e) {
			logger.error("Exception caught while fetching data for status enquiry, " , e);
			return null;
		}
		return null;
	}
}