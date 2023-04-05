package com.paymentgateway.notification.sms.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
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
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class DashboardDataService {

	private static Logger logger = LoggerFactory.getLogger(DashboardDataService.class.getName());

	@Autowired
	private MongoInstance mongoInstance;

	private static final String prefix = "MONGO_DB_";

	
	public void generateData(String reqDate) throws Exception {
		
		List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

		DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		Date dateStart = format.parse(reqDate);

		LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		String dateIndex = startDate.toString().replaceAll("-", "");
		
		allConditionQueryList.add(new BasicDBObject(FieldType.DATE_INDEX.getName(), dateIndex));
		
		List<BasicDBObject> txnTypeQueryList = new ArrayList<BasicDBObject>();
		BasicDBObject txnTypeQuery = new BasicDBObject();
		txnTypeQueryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		txnTypeQueryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
		txnTypeQuery.put("$or", txnTypeQueryList);
		
		allConditionQueryList.add(txnTypeQuery);
		
		BasicDBObject projectElement = new BasicDBObject();
		projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
		projectElement.put(FieldType.AMOUNT.getName(), 1);
		projectElement.put(FieldType.STATUS.getName(), 1);
		projectElement.put(FieldType.TXNTYPE.getName(), 1);
		projectElement.put(FieldType.ORIG_TXNTYPE.getName(), 1);
		projectElement.put(FieldType.PAY_ID.getName(), 1);
		projectElement.put(FieldType.SUB_MERCHANT_ID.getName(), 1);
		projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);
		
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		BasicDBObject project = new BasicDBObject("$project", projectElement);

		BasicDBObject finalQueryForSale = new BasicDBObject("$and", allConditionQueryList);
		BasicDBObject match = new BasicDBObject("$match", finalQueryForSale);

		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("PAY_ID", -1));
		BasicDBObject sort2 = new BasicDBObject("$sort", new BasicDBObject("SUB_MERCHANT_ID", -1));
		List<BasicDBObject> pipeline = Arrays.asList(match, project, sort, sort2);
		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
		String merchantId = null;
		String subMerchantId = null;
		List<Document> transactionsList = new ArrayList<Document>();
		
		while (cursor.hasNext()) {
			Document dbobj = cursor.next();
			
			if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.toString()))) {
				if (subMerchantId == null) {
					merchantId = dbobj.getString(FieldType.PAY_ID.toString());
					subMerchantId = dbobj.getString(FieldType.SUB_MERCHANT_ID.toString());
				} else if (!subMerchantId.equals(dbobj.getString(FieldType.SUB_MERCHANT_ID.toString()))) {
					generateSaleData(transactionsList, merchantId, subMerchantId, dateIndex);
					subMerchantId = dbobj.getString(FieldType.SUB_MERCHANT_ID.toString());
					merchantId = dbobj.getString(FieldType.PAY_ID.toString());
					transactionsList.clear();
				}
				transactionsList.add(dbobj);
			} else {
				if (merchantId == null) {
					merchantId = dbobj.getString(FieldType.PAY_ID.toString());
				} else if (!merchantId.equals(dbobj.getString(FieldType.PAY_ID.toString()))) {
					generateSaleData(transactionsList, merchantId, subMerchantId, dateIndex);
					merchantId = dbobj.getString(FieldType.PAY_ID.toString());
					subMerchantId = null;
					transactionsList.clear();
				}
				transactionsList.add(dbobj);
			}
			
		}
		if (!transactionsList.isEmpty()) {
			generateSaleData(transactionsList, merchantId, subMerchantId, dateIndex);
		}
	}
	
	public void generateSaleData(List<Document> settledTxns, String merchantId, String subMerchantId, String dateIndex) throws Exception {

		Statistics statistics = new Statistics();
		BigDecimal totalSaleApproved = BigDecimal.ZERO;
		BigDecimal totalRefunded = BigDecimal.ZERO;

		int totalSaleSuccess = 0;
		int totalRefundSuccess = 0;
		
		int totalSalefail = 0;
		int totalSaleRejectedDeclined = 0;
		int totalSaleCancelled = 0;
		int totalSaleFraud = 0;
		int totalSaleInvalid = 0;
		int totalSaleDropped = 0;
	
		int totalRefundfail = 0;
		int totalRefundRejectedDeclined = 0;
		int totalRefundCancelled = 0;
		int totalRefundFraud = 0;
		int totalRefundInvalid = 0;
		int totalRefundDropped = 0;
		
		statistics.setTotalSuccess(String.valueOf(totalSaleSuccess));
		logger.info("Statistics Approved amount calculation started ");
		for (Document dbobj : settledTxns) {

			if (dbobj.getString(FieldType.ORIG_TXNTYPE.toString()).equals(TransactionType.SALE.getName())) {

				String approvedAmount = "";
				if (dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.CAPTURED.getName())) {
					totalSaleSuccess++;
					approvedAmount = (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
					if (StringUtils.isBlank(approvedAmount)) {
						approvedAmount = (dbobj.getString(FieldType.AMOUNT.toString()));
					}

					BigDecimal addApproved = new BigDecimal(approvedAmount);
					totalSaleApproved = totalSaleApproved.add(addApproved).setScale(2, RoundingMode.HALF_DOWN);

				} else if(dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.FAILED.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.ERROR.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.ACQUIRER_DOWN.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.FAILED_AT_ACQUIRER.getName())) {
					totalSalefail++;
					
				} else if(dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.REJECTED.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.DECLINED.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.AUTHENTICATION_FAILED.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.REJECTED_BY_PG.getName())) {
					totalSaleRejectedDeclined++;
					
				} else if(dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.CANCELLED.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.BROWSER_CLOSED.getName())) {
					totalSaleCancelled++;
					
				} else if(dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.CANCELLED.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.BROWSER_CLOSED.getName())) {
					totalSaleCancelled++;
					
				} else if(dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.DENIED.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.DENIED_BY_FRAUD.getName())) {
					totalSaleFraud++;
					
				} else if(dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.INVALID.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.DUPLICATE.getName())) {
					totalSaleInvalid++;
					
				} else if(dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.TIMEOUT.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.ACQUIRER_TIMEOUT.getName())) {
					totalSaleDropped++;
					
				}

				

			} else {
				
				String approvedAmount = "";
				if (dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.CAPTURED.getName())) {
					totalRefundSuccess++;
					approvedAmount = (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
					if (StringUtils.isBlank(approvedAmount)) {
						approvedAmount = (dbobj.getString(FieldType.AMOUNT.toString()));
					}
					totalRefunded = totalRefunded.add(new BigDecimal(approvedAmount)).setScale(2,
							RoundingMode.HALF_DOWN);
				} else if(dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.FAILED.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.ERROR.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.ACQUIRER_DOWN.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.FAILED_AT_ACQUIRER.getName())) {
					totalRefundfail++;
					
				}else if(dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.REJECTED.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.DECLINED.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.AUTHENTICATION_FAILED.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.REJECTED_BY_PG.getName())) {
					totalRefundRejectedDeclined++;
					
				} else if(dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.CANCELLED.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.BROWSER_CLOSED.getName())) {
					totalRefundCancelled++;
					
				} else if(dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.CANCELLED.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.BROWSER_CLOSED.getName())) {
					totalRefundCancelled++;
					
				} else if(dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.DENIED.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.DENIED_BY_FRAUD.getName())) {
					totalRefundFraud++;
					
				} else if(dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.INVALID.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.DUPLICATE.getName())) {
					totalRefundInvalid++;
					
				} else if(dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.TIMEOUT.getName())
						|| dbobj.getString(FieldType.STATUS.toString()).equals(StatusType.ACQUIRER_TIMEOUT.getName())) {
					totalRefundDropped++;
					
				}
				
			}

		}
		logger.info("Statistics Approved amount calculation end ");
		statistics.setTotalSuccess(String.valueOf(totalSaleSuccess));
		statistics.setApprovedAmount(String.valueOf(totalSaleApproved));
		statistics.setTotalRefunded(String.valueOf(totalRefundSuccess));
		statistics.setRefundedAmount(String.valueOf(totalRefunded));
		
		statistics.setTotalSaleFailed((String.valueOf(totalSalefail)));
		statistics.setTotalSaleRejectedDeclined(String.valueOf(totalSaleRejectedDeclined));
		statistics.setTotalSaleDropped(String.valueOf(totalSaleDropped));
		statistics.setTotalSaleCancelled(String.valueOf(totalSaleCancelled));
		statistics.setTotalSaleFraud(String.valueOf(totalSaleFraud));
		statistics.setTotalSaleInvalid(String.valueOf(totalSaleInvalid));
		
		statistics.setTotalRefundFailed((String.valueOf(totalRefundfail)));
		statistics.setTotalRefundRejectedDeclined(String.valueOf(totalRefundRejectedDeclined));
		statistics.setTotalRefundDropped(String.valueOf(totalRefundDropped));
		statistics.setTotalRefundCancelled(String.valueOf(totalRefundCancelled));
		statistics.setTotalRefundFraud(String.valueOf(totalRefundFraud));
		statistics.setTotalRefundInvalid(String.valueOf(totalRefundInvalid));
		
		statistics.setPayId(merchantId);
		statistics.setSubMerchantId(subMerchantId);
		statistics.setDateIndex(dateIndex);
		
		insertIntoCollection(statistics);
	}

	private void insertIntoCollection(Statistics statistics) throws Exception{
		
		BasicDBObject newFieldsObj = new BasicDBObject();
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> collection = dbIns
				.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.DASHBOARD_COLLECTION.getValue()));

		newFieldsObj.put(FieldType.PAY_ID.toString(), statistics.getPayId());
		newFieldsObj.put(FieldType.SUB_MERCHANT_ID.toString(), statistics.getSubMerchantId());
		newFieldsObj.put(FieldType.DATE_INDEX.toString(), statistics.getDateIndex());
		
		newFieldsObj.put(FieldType.TOTAL_SALE_SUCCESS.toString(), statistics.getTotalSuccess());
		newFieldsObj.put(FieldType.TOTAL_REFUND_SUCCESS.toString(), statistics.getTotalRefunded());
		newFieldsObj.put(FieldType.TOTAL_SALE_FAILED.toString(), statistics.getTotalSaleFailed());
		newFieldsObj.put(FieldType.TOTAL_REFUND_FAILED.toString(), statistics.getTotalRefundFailed());
		newFieldsObj.put(FieldType.TOTAL_SALE_REJECTED_DECLINED.toString(), statistics.getTotalSaleRejectedDeclined());
		newFieldsObj.put(FieldType.TOTAL_REFUND_REJECTED_DECLINED.toString(), statistics.getTotalRefundRejectedDeclined());
		newFieldsObj.put(FieldType.TOTAL_SALE_DROPPED.toString(), statistics.getTotalSaleDropped());
		newFieldsObj.put(FieldType.TOTAL_REFUND_DROPPED.toString(), statistics.getTotalRefundDropped());
		newFieldsObj.put(FieldType.TOTAL_SALE_CANCELLED.toString(), statistics.getTotalSaleCancelled());
		newFieldsObj.put(FieldType.TOTAL_REFUND_CANCELLED.toString(), statistics.getTotalRefundCancelled());
		newFieldsObj.put(FieldType.TOTAL_SALE_FRAUD.toString(), statistics.getTotalSaleFraud());
		newFieldsObj.put(FieldType.TOTAL_REFUND_FRAUD.toString(), statistics.getTotalRefundFraud());
		newFieldsObj.put(FieldType.TOTAL_SALE_INVALID.toString(), statistics.getTotalSaleInvalid());
		newFieldsObj.put(FieldType.TOTAL_REFUND_INVALID.toString(), statistics.getTotalRefundInvalid());
		newFieldsObj.put(FieldType.TOTAL_SALE_AMOUNT.toString(), statistics.getApprovedAmount());
		newFieldsObj.put(FieldType.TOTAL_REFUND_AMOUNT.toString(), statistics.getRefundedAmount());
		
		
		Document doc = new Document(newFieldsObj);
		collection.insertOne(doc);
		
		
		
	}

//	private void getAllOtherStatusData(Statistics statistics, BasicDBObject dateIndexConditionQuery) {
//
//		List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
//		
//
//		BasicDBObject saleConditionQuery = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getName());
//		BasicDBObject refundConditionQuery = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName());
//
//		allConditionQueryList.add(dateIndexConditionQuery);
//
//// failed status query
//
//		List<BasicDBObject> failConditionList = new ArrayList<BasicDBObject>();
//		List<BasicDBObject> failTypeConditionLst = new ArrayList<BasicDBObject>();
//		failTypeConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
//		failTypeConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ERROR.getName()));
//		failTypeConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName()));
//		failTypeConditionLst
//				.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED_AT_ACQUIRER.getName()));
//
//		BasicDBObject failQuery2 = new BasicDBObject();
//		failQuery2.append("$or", failTypeConditionLst);
//
//		failConditionList.add(failQuery2);
//
//		BasicDBObject failConditionQuery = new BasicDBObject("$and", failConditionList);
//		
//		List<BasicDBObject> allConditionQueryListForfail = new ArrayList<BasicDBObject>();
//
//		if (refundConditionQuery != null && !refundConditionQuery.isEmpty()) {
//			allConditionQueryListForfail.add(failConditionQuery);
//			allConditionQueryListForfail.add(refundConditionQuery);
//		}
//		if (saleConditionQuery != null && !saleConditionQuery.isEmpty()) {
//			allConditionQueryListForfail.add(failConditionQuery);
//			allConditionQueryListForfail.add(saleConditionQuery);
//		}
//
//		BasicDBObject allConditionQueryObjforfail = new BasicDBObject("$and", allConditionQueryListForfail);
//
//// Rejected status Query
//
//		List<BasicDBObject> rejectedConditionList = new ArrayList<BasicDBObject>();
//		BasicDBObject rejectedQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
//		BasicDBObject delicnedQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.DECLINED.getName());
//		BasicDBObject authFailedQuery = new BasicDBObject(FieldType.STATUS.getName(),
//				StatusType.AUTHENTICATION_FAILED.getName());
//		BasicDBObject rejectedByPgQuery = new BasicDBObject(FieldType.STATUS.getName(),
//				StatusType.REJECTED_BY_PG.getName());
//		rejectedConditionList.add(rejectedQuery);
//		rejectedConditionList.add(delicnedQuery);
//		rejectedConditionList.add(authFailedQuery);
//		rejectedConditionList.add(rejectedByPgQuery);
//		BasicDBObject rejectedConditionQuery = new BasicDBObject("$or", rejectedConditionList);
//
//		List<BasicDBObject> allConditionQueryListForReject = new ArrayList<BasicDBObject>();
//
//		if (refundConditionQuery != null && !refundConditionQuery.isEmpty()) {
//			allConditionQueryListForReject.add(rejectedConditionQuery);
//			allConditionQueryListForReject.add(refundConditionQuery);
//		}
//		if (saleConditionQuery != null && !saleConditionQuery.isEmpty()) {
//			allConditionQueryListForReject.add(rejectedConditionQuery);
//			allConditionQueryListForReject.add(saleConditionQuery);
//		}
//
//		BasicDBObject allConditionQueryObjforReject = new BasicDBObject("$and", allConditionQueryListForReject);
//
//// cancelled status query		
//
//		List<BasicDBObject> cancelledConditionList = new ArrayList<BasicDBObject>();
//		BasicDBObject cancelledQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
//		BasicDBObject browserClosedQuery = new BasicDBObject(FieldType.STATUS.getName(),
//				StatusType.BROWSER_CLOSED.getName());
//		cancelledConditionList.add(cancelledQuery);
//		cancelledConditionList.add(browserClosedQuery);
//		BasicDBObject cancelledConditionQuery = new BasicDBObject("$or", cancelledConditionList);
//
//		List<BasicDBObject> allConditionQueryListForCancelled = new ArrayList<BasicDBObject>();
//
//		if (refundConditionQuery != null && !refundConditionQuery.isEmpty()) {
//			allConditionQueryListForCancelled.add(cancelledConditionQuery);
//			allConditionQueryListForCancelled.add(refundConditionQuery);
//		}
//		if (saleConditionQuery != null && !saleConditionQuery.isEmpty()) {
//			allConditionQueryListForCancelled.add(cancelledConditionQuery);
//			allConditionQueryListForCancelled.add(saleConditionQuery);
//		}
//		
//		BasicDBObject allConditionQueryObjforCancelled = new BasicDBObject("$and", allConditionQueryListForCancelled);
//
//// fraud status query.	
//
//		List<BasicDBObject> fraudConditionList = new ArrayList<BasicDBObject>();
//		BasicDBObject fraudQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName());
//		BasicDBObject deniedQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED.getName());
//		fraudConditionList.add(fraudQuery);
//		fraudConditionList.add(deniedQuery);
//		BasicDBObject fraudConditionQuery = new BasicDBObject("$or", fraudConditionList);
//
//		List<BasicDBObject> allConditionQueryListForFraud = new ArrayList<BasicDBObject>();
//
//		if (refundConditionQuery != null && !refundConditionQuery.isEmpty()) {
//			allConditionQueryListForFraud.add(fraudConditionQuery);
//			allConditionQueryListForFraud.add(refundConditionQuery);
//		}
//		if (saleConditionQuery != null && !saleConditionQuery.isEmpty()) {
//			allConditionQueryListForFraud.add(fraudConditionQuery);
//			allConditionQueryListForFraud.add(saleConditionQuery);
//		}
//
//		BasicDBObject allConditionQueryObjforFraud = new BasicDBObject("$and", allConditionQueryListForFraud);
//
//// invalid status query	
//
//		List<BasicDBObject> invalidConditionList = new ArrayList<BasicDBObject>();
//		BasicDBObject invalidQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.INVALID.getName());
//		BasicDBObject duplicateQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());
//		invalidConditionList.add(invalidQuery);
//		invalidConditionList.add(duplicateQuery);
//		BasicDBObject invalidConditionQuery = new BasicDBObject("$or", invalidConditionList);
//
//		List<BasicDBObject> allConditionQueryListForInvalid = new ArrayList<BasicDBObject>();
//
//		if (refundConditionQuery != null && !refundConditionQuery.isEmpty()) {
//			allConditionQueryListForInvalid.add(invalidConditionQuery);
//			allConditionQueryListForInvalid.add(refundConditionQuery);
//		}
//		if (saleConditionQuery != null && !saleConditionQuery.isEmpty()) {
//			allConditionQueryListForInvalid.add(invalidConditionQuery);
//			allConditionQueryListForInvalid.add(saleConditionQuery);
//		}
//		BasicDBObject allConditionQueryObjforInvalid = new BasicDBObject("$and", allConditionQueryListForInvalid);
//
//// Dropped status query
//
//		List<BasicDBObject> droppedConditionList = new ArrayList<BasicDBObject>();
//		BasicDBObject droppedQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName());
//		BasicDBObject AcqTimeoutQuery = new BasicDBObject(FieldType.STATUS.getName(),
//				StatusType.ACQUIRER_TIMEOUT.getName());
//
//		droppedConditionList.add(droppedQuery);
//		droppedConditionList.add(AcqTimeoutQuery);
//
//		BasicDBObject droppedConditionQuery = new BasicDBObject("$or", droppedConditionList);
//
//		List<BasicDBObject> allConditionQueryListForDropped = new ArrayList<BasicDBObject>();
//
//		if (refundConditionQuery != null && !refundConditionQuery.isEmpty()) {
//			allConditionQueryListForDropped.add(droppedConditionQuery);
//			allConditionQueryListForDropped.add(refundConditionQuery);
//		}
//		if (saleConditionQuery != null && !saleConditionQuery.isEmpty()) {
//			allConditionQueryListForDropped.add(droppedConditionQuery);
//			allConditionQueryListForDropped.add(saleConditionQuery);
//		}
//		
//		BasicDBObject allConditionQueryObjforDropped = new BasicDBObject("$and", allConditionQueryListForDropped);
//
//		MongoDatabase dbIns = mongoInstance.getDB();
//		MongoCollection<Document> coll = dbIns
//				.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
//
//		BasicDBObject finalQueryForRefund = new BasicDBObject("$and", allConditionQueryList);
//		BasicDBObject match = new BasicDBObject("$match", finalQueryForRefund);
//
//		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("PAY_ID", -1));
//		List<BasicDBObject> pipeline = Arrays.asList(sort);
//		AggregateIterable<Document> output = coll.aggregate(pipeline);
//		output.allowDiskUse(true);
//		MongoCursor<Document> cursor = output.iterator();
//
//		logger.info("Statistics All query start ");
//
//		long totalfail = 0;
//		long totalrejectedDeclined = 0;
//		long totalcancelled = 0;
//		long totalfraud = 0;
//		long totalinvalid = 0;
//		long totaldropped = 0;
//
//		totalfail = coll.countDocuments(allConditionQueryObjforfail);
//		totalrejectedDeclined = coll.countDocuments(allConditionQueryObjforReject);
//		totalcancelled = coll.countDocuments(allConditionQueryObjforCancelled);
//		totalfraud = coll.countDocuments(allConditionQueryObjforFraud);
//		totalinvalid = coll.countDocuments(allConditionQueryObjforInvalid);
//		totaldropped = coll.countDocuments(allConditionQueryObjforDropped);
//
//		logger.info("Statistics All query end ");
//
//
//	}
}
