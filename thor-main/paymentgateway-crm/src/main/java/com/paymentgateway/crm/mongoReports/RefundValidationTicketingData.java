package com.paymentgateway.crm.mongoReports;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.RefundValidationDetailsDao;
import com.paymentgateway.commons.dao.RefundValidationTicketingDataDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.RefundValidation;
import com.paymentgateway.commons.user.RefundValidationDetails;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Chandan
 *
 */

@Service
public class RefundValidationTicketingData {

	private static Logger logger = LoggerFactory.getLogger(RefundValidationTicketingData.class.getName());
	
	@Autowired
	PropertiesManager propertiesManager;
	
	@Autowired
	private MongoInstance mongoInstance;
		
	@Autowired
	private FieldsDao fieldsDao;
	
	@Autowired
	private UserDao userdao;
	
	@Autowired
	private RefundValidationDetailsDao refundValidationDetailsDao;
	
	@Autowired 
	private RefundValidationTicketingDataDao refundValidationTicketingDataDao;
	
	private static final String prefix = "MONGO_DB_";
	
	private static final String alphabaticFileName= "alphabatic-currencycode.properties";
	
	//Map<String, Document> documents = new HashMap<>();
	List<String> lstTxnId = new ArrayList<String>();
	//private Document saleDoc = new Document();
	
	public List<RefundValidation> getCapturedData(String merchantPayId, String fromDate) {
		logger.info("Inside RefundValidationTicketingData Class, in getCapturedData Method !!"); 
		List<RefundValidation> refundValidationList = new ArrayList<RefundValidation>();
		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject allParamQuery = new BasicDBObject();
			
			if (!fromDate.isEmpty()) {
				String currentDate = null;
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				 // add days to from date				
				Date date1= dateFormat.parse(fromDate);	
				cal.setTime(date1);
				cal.add(Calendar.DATE, 1);
				currentDate = dateFormat.format(cal.getTime());	

				dateQuery.put(FieldType.REQUEST_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lt", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}
				
			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}
						
			List<BasicDBObject> refundValidationConditionList = new ArrayList<BasicDBObject>();
			refundValidationConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),  TransactionType.REFUND.getName()));
			ArrayList<String> statusList=new ArrayList<>();
			statusList.add(StatusType.CAPTURED.getName());
			//statusList.add(StatusType.RECONCILED.getName());
			refundValidationConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), new BasicDBObject("$in",statusList)));
			
			BasicDBObject refundValidationQuery = new BasicDBObject("$and", refundValidationConditionList);
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			
			if (!refundValidationQuery.isEmpty()) {
				allConditionQueryList.add(refundValidationQuery);
			}
			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();
			
			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}			
			
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			
			MongoDatabase dbIns = mongoInstance.getDB();  
			MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			
			logger.info("RefundValidationTicketingData final Query (Captured)  : " + finalquery);
			logger.info("No. of Records : " + coll.count(finalquery));
						
			MongoCursor<Document> cursor = coll.find(finalquery).iterator();
			List<Document> lstDoc = new ArrayList<Document>();	
			lstTxnId.clear();
			int count = 0;
			while (cursor.hasNext()) {
				
				RefundValidation validationReport = new RefundValidation();
				Document doc = cursor.next();
				validationReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
				validationReport.setRefundTag(doc.getString(FieldType.REFUND_FLAG.toString()));
				validationReport.setRefundAmt(doc.getString(FieldType.AMOUNT.toString()));
				validationReport.setBankTxnId(doc.getString(FieldType.ORIG_TXN_ID.toString()));
				validationReport.setRefundStatus(Constants.SUCCESS.getValue());
				validationReport.setRefundProcessDate(DateCreater.formatDateReco(doc.getString(FieldType.CREATE_DATE.toString())));
				validationReport.setRefundBankTxnId(doc.getString(FieldType.PG_REF_NUM.toString()));
				validationReport.setCancelTxnDate(DateCreater.formatDateReco(doc.getString(FieldType.REQUEST_DATE.toString())));
				validationReport.setBookingTxnAmt(doc.getString(FieldType.SALE_AMOUNT.toString()));
				validationReport.setCancelTxnId(doc.getString(FieldType.REFUND_ORDER_ID.toString()));
				validationReport.setRemarks(Constants.SUCCESS.getValue());
				
				refundValidationList.add(validationReport);
				
				Date dNow = new Date();
				String dateNow = DateCreater.formatDateForDb(dNow);
				String txnId = generateId();
				
				doc.put(FieldType.TXN_ID.getName(), txnId);
				doc.put("_id", txnId);
				doc.put(FieldType.STATUS.getName(), StatusType.RECONCILED.getName());
				doc.put(FieldType.CREATE_DATE.getName(), dateNow);
				doc.put(FieldType.UPDATE_DATE.getName(), dateNow);
				doc.put(FieldType.INSERTION_DATE.getName(), dNow);
				lstDoc.add(doc);
				lstTxnId.add(txnId);
				count ++;
				logger.info("Count : "+ count );
				if(count == 500) {
					TimeUnit.SECONDS.sleep(1);
					logger.info("Time :" + dateNow);
					count  = 0;
				}			
			}
			cursor.close();
			
			if(lstDoc.size() > 0) {
				logger.info("Added "+ lstDoc.size()+" transactions in mongo document for upload to Refund/Reconciled");
				refundValidationTicketingDataDao.insertTransaction(lstDoc);
				lstDoc.clear();
				lstTxnId.clear();
			}			
		} catch (Exception exception) {
			logger.error("Inside DownloadRefundValidationFile Class, in getCapturedData method  : ", exception);
		}
		return refundValidationList;
	}
	
	private String generateId() {
		String txnId = TransactionManager.getNewTransactionId();
		if(lstTxnId.contains(txnId)) {
			generateId();
		}
		return txnId;
	}
	
	
	public List<RefundValidation> getOthersData(String merchantPayId, String fromDate) {
		logger.info("Inside DownloadRefundValidationFile Class, in getData Method !!"); 
		List<RefundValidation> refundValidationList = new ArrayList<RefundValidation>();
		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			//BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			//List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
			
			if (!fromDate.isEmpty()) {
				String currentDate = null;
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				 // add days to from date				
				Date date1= dateFormat.parse(fromDate);	
				cal.setTime(date1);
				cal.add(Calendar.DATE, 1);
				currentDate = dateFormat.format(cal.getTime());	

				dateQuery.put(FieldType.REQUEST_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lt", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}
	
			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}
			
			List<BasicDBObject> refundValidationConditionList = new ArrayList<BasicDBObject>();
			refundValidationConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),  TransactionType.REFUND.getName()));
			ArrayList<String> statusList=new ArrayList<>();
			statusList.add(StatusType.CAPTURED.getName());
			//statusList.add(StatusType.RECONCILED.getName());
			refundValidationConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), new BasicDBObject("$nin",statusList)));
			
			BasicDBObject refundValidationQuery = new BasicDBObject("$and", refundValidationConditionList);
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			
			/*if (!currencyQuery.isEmpty()) {
				allConditionQueryList.add(currencyQuery);
			}*/
			
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			
			if (!refundValidationQuery.isEmpty()) {
				allConditionQueryList.add(refundValidationQuery);
			}
			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();
			
			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}			
			
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			
			MongoDatabase dbIns = mongoInstance.getDB();  
			MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			
			logger.info("RefundValidationTicketingData final Query : " + finalquery);
			logger.info("No. of Records : " + coll.count(finalquery));
			
			// Now the aggregate operation
			Document firstGroup = new Document("_id",
					new Document("REFUND_ORDER_ID", "$REFUND_ORDER_ID"));
			
			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			
			MongoCursor<Document> cursor = output.iterator();
			Boolean reconciledFlag = false;
			
			logger.info("DownloadRefundValidationFile final Query : " + finalquery);
			logger.info("No. of Records : " + coll.count(finalquery));
			while (cursor.hasNext()) {
				reconciledFlag = false;	
				Document dbobj = cursor.next();
				List<Document> lstDoc = (List<Document>) dbobj.get("entries");
				for(int i=0; i<lstDoc.size();i++) {
					if(lstDoc.get(i).getString(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUND.getName())
							&& lstDoc.get(i).getString(FieldType.STATUS.getName()).equals(StatusType.RECONCILED.getName())) {
						reconciledFlag = true;
						break;
					}
				}
				if(!reconciledFlag) {
						Document doc = lstDoc.get(0);
						RefundValidation validationReport = new RefundValidation();
						//saleDoc = getSaleTxn(doc.getString(FieldType.OID.toString()));
						/*if(insertRecoTxn(doc)) {*/	
						validationReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
						validationReport.setRefundTag(doc.getString(FieldType.REFUND_FLAG.toString()));
						validationReport.setRefundAmt(doc.getString(FieldType.AMOUNT.toString()));
						//validationReport.setRefundAmt(Amount.removeDecimalAmount(doc.getString(FieldType.AMOUNT.toString()),doc.getString(FieldType.CURRENCY_CODE.toString())));
						validationReport.setBankTxnId(doc.getString(FieldType.ORIG_TXN_ID.toString()));
						validationReport.setRefundStatus(doc.getString(FieldType.STATUS.toString()));
						/*validationReport.setRefundStatus(doc.getString(FieldType.STATUS.toString()));*/
						//validationReport.setRefundProcessDate(doc.getString(FieldType.CREATE_DATE.toString()));
						validationReport.setRefundProcessDate(DateCreater.formatDateReco(doc.getString(FieldType.CREATE_DATE.toString())));
						validationReport.setRefundBankTxnId(doc.getString(FieldType.PG_REF_NUM.toString()));
						/*validationReport.setCancelTxnDate(DateCreater.formatDateReco(getRefundDate(doc.getString(FieldType.OID.toString()))));*/
						validationReport.setCancelTxnDate(DateCreater.formatDateReco(doc.getString(FieldType.REQUEST_DATE.toString())));
						validationReport.setBookingTxnAmt(doc.getString(FieldType.SALE_AMOUNT.toString()));
						//validationReport.setBookingTxnAmt(Amount.removeDecimalAmount(getSaleAmt(doc.getString(FieldType.OID.toString())),doc.getString(FieldType.CURRENCY_CODE.toString())));
						validationReport.setCancelTxnId(doc.getString(FieldType.REFUND_ORDER_ID.toString()));
						validationReport.setRemarks(doc.getString(FieldType.RESPONSE_MESSAGE.toString()));
						
						refundValidationList.add(validationReport);
					//}
				}
			}
			cursor.close();
		} catch (Exception exception) {
			logger.error("Inside DownloadRefundValidationFile Class, in getOthersData method  : ", exception);
		}
		return refundValidationList;
	}
	
	public List<RefundValidation> getPostSettledData(String merchantPayId, String fromDate) {
		logger.info("Inside RefundValidationTicketingData Class, in getCapturedData Method !!"); 
		List<RefundValidation> refundValidationList = new ArrayList<RefundValidation>();
		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			//BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			//List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
			
			if (!fromDate.isEmpty()) {
				String currentDate = null;
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				 // add days to from date				
				Date date1= dateFormat.parse(fromDate);	
				cal.setTime(date1);
				cal.add(Calendar.DATE, 1);
				currentDate = dateFormat.format(cal.getTime());	

				dateQuery.put(FieldType.REQUEST_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lt", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}
	
			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}
			List<BasicDBObject> refundValidationConditionList = new ArrayList<BasicDBObject>();
			refundValidationConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),  TransactionType.REFUND.getName()));
			ArrayList<String> statusList=new ArrayList<>();
			statusList.add(StatusType.CAPTURED.getName());
			statusList.add(StatusType.RECONCILED.getName());
			refundValidationConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), new BasicDBObject("$in",statusList)));
			
			BasicDBObject refundValidationQuery = new BasicDBObject("$and", refundValidationConditionList);
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			
			/*if (!currencyQuery.isEmpty()) {
				allConditionQueryList.add(currencyQuery);
			}*/
			
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			
			if (!refundValidationQuery.isEmpty()) {
				allConditionQueryList.add(refundValidationQuery);
			}
			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();
			
			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}			
			
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			
			MongoDatabase dbIns = mongoInstance.getDB();  
			MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			
			// Now the aggregate operation
			Document firstGroup = new Document("_id",
					new Document("REFUND_ORDER_ID", "$REFUND_ORDER_ID"));
			
			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			
			MongoCursor<Document> cursor = output.iterator();
			Boolean reconciledFlag = false;
			
			logger.info("Post Settle Refund Validation final Query : " + pipeline);
			
			
			List<Document> lstDoc = new ArrayList<Document>();	
			lstTxnId.clear();
			int count = 0;
			while (cursor.hasNext()) {
				reconciledFlag = false;	
				Document dbobj = cursor.next();
				List<Document> lstDocument = (List<Document>) dbobj.get("entries");
				for(int i=0; i<lstDocument.size();i++) {
					if(lstDocument.get(i).getString(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUND.getName())
							&& lstDocument.get(i).getString(FieldType.STATUS.getName()).equals(StatusType.RECONCILED.getName())) {
						reconciledFlag = true;
						break;
					}
				}
				if(!reconciledFlag) {
					Document doc = lstDocument.get(0);
					RefundValidation validationReport = new RefundValidation();
					//if(insertRecoTxn(doc)) {
						//saleDoc = getSaleTxn(doc.getString(FieldType.OID.toString()));
						validationReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
						validationReport.setRefundTag(doc.getString(FieldType.REFUND_FLAG.toString()));
						validationReport.setRefundAmt(doc.getString(FieldType.AMOUNT.toString()));
						validationReport.setBankTxnId(doc.getString(FieldType.ORIG_TXN_ID.toString()));
						validationReport.setRefundStatus(Constants.SUCCESS.getValue());
						/*validationReport.setRefundStatus(doc.getString(FieldType.STATUS.toString()));*/
						//validationReport.setRefundProcessDate(doc.getString(FieldType.CREATE_DATE.toString()));
						validationReport.setRefundProcessDate(DateCreater.formatDateReco(doc.getString(FieldType.CREATE_DATE.toString())));
						validationReport.setRefundBankTxnId(doc.getString(FieldType.PG_REF_NUM.toString()));
						/*validationReport.setCancelTxnDate(DateCreater.formatDateReco(getRefundDate(doc.getString(FieldType.OID.toString()))));*/
						validationReport.setCancelTxnDate(DateCreater.formatDateReco(doc.getString(FieldType.REQUEST_DATE.toString())));
						validationReport.setBookingTxnAmt(doc.getString(FieldType.SALE_AMOUNT.toString()));
						validationReport.setCancelTxnId(doc.getString(FieldType.REFUND_ORDER_ID.toString()));
						validationReport.setRemarks(Constants.SUCCESS.getValue());
						
						refundValidationList.add(validationReport);
						
						Date dNow = new Date();
						String dateNow = DateCreater.formatDateForDb(dNow);
						String txnId = TransactionManager.getNewTransactionId();
						doc.put(FieldType.TXN_ID.getName(), txnId);
						doc.put("_id", txnId);
						doc.put(FieldType.STATUS.getName(), StatusType.RECONCILED.getName());
						doc.put(FieldType.CREATE_DATE.getName(), dateNow);
						doc.put(FieldType.UPDATE_DATE.getName(), dateNow);
						doc.put(FieldType.INSERTION_DATE.getName(), dNow);
						lstDoc.add(doc);
						lstTxnId.add(txnId);
						count ++;
						logger.info("Count : "+ count );
						if(count == 500) {
							TimeUnit.SECONDS.sleep(1);
							logger.info("Time :" + dateNow);
							count  = 0;
						}										
					
				}
			}
			cursor.close();
			
			if(lstDoc.size() > 0) {
				logger.info("Added "+ lstDoc.size()+" transactions in mongo document for upload to Refund/Reconciled");
				refundValidationTicketingDataDao.insertTransaction(lstDoc);
				lstDoc.clear();
				lstTxnId.clear();
			}	
			/*else {
				return null;
			}*/
		} catch (Exception exception) {
			logger.error("Inside DownloadRefundValidationFile Class, in getPostSettledData method  : ", exception);
		}
		return refundValidationList;
	}
	
	public List<RefundValidation> getFinalVersionData(String merchantPayId, String fromDate) {
		logger.info("Inside RefundValidationTicketingData Class, in getCapturedData Method !!"); 
		List<RefundValidation> refundValidationList = new ArrayList<RefundValidation>();
		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			//BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			//List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
			
			if (!fromDate.isEmpty()) {
				String currentDate = null;
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				 // add days to from date				
				Date date1= dateFormat.parse(fromDate);	
				cal.setTime(date1);
				cal.add(Calendar.DATE, 1);
				currentDate = dateFormat.format(cal.getTime());	

				dateQuery.put(FieldType.REQUEST_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lt", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}
	
			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}
			List<BasicDBObject> refundValidationConditionList = new ArrayList<BasicDBObject>();
			refundValidationConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),  TransactionType.REFUND.getName()));
			ArrayList<String> statusList=new ArrayList<>();
			statusList.add(StatusType.CAPTURED.getName());
			statusList.add(StatusType.RECONCILED.getName());
			statusList.add(StatusType.DECLINED.getName());
			statusList.add(StatusType.REJECTED.getName());
			statusList.add(StatusType.ERROR.getName());
			statusList.add(StatusType.TIMEOUT.getName());
			statusList.add(StatusType.DENIED.getName());
			statusList.add(StatusType.FAILED.getName());
			statusList.add(StatusType.INVALID.getName());
			statusList.add(StatusType.DENIED_BY_FRAUD.getName());
			statusList.add(StatusType.PENDING.getName());
			refundValidationConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), new BasicDBObject("$in",statusList)));
			
			BasicDBObject refundValidationQuery = new BasicDBObject("$and", refundValidationConditionList);
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			
			/*if (!currencyQuery.isEmpty()) {
				allConditionQueryList.add(currencyQuery);
			}*/
			
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			
			if (!refundValidationQuery.isEmpty()) {
				allConditionQueryList.add(refundValidationQuery);
			}
			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();
			
			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}			
			
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			
			MongoDatabase dbIns = mongoInstance.getDB();  
			MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			
			logger.info("RefundValidationTicketingData final Query : " + finalquery);
			logger.info("No. of Records : " + coll.count(finalquery));
			
			// Now the aggregate operation
			Document firstGroup = new Document("_id",
					new Document("REFUND_ORDER_ID", "$REFUND_ORDER_ID"));
			
			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			
			MongoCursor<Document> cursor = output.iterator();
			Boolean reconciledFlag = false;
			
			logger.info("DownloadRefundValidationFile final Query : " + finalquery);
			logger.info("No. of Records : " + coll.count(finalquery));
			while (cursor.hasNext()) {
				reconciledFlag = false;	
				Document dbobj = cursor.next();
				List<Document> lstDoc = (List<Document>) dbobj.get("entries");
				for(int i=0; i<lstDoc.size();i++) {
					if(lstDoc.get(i).getString(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUND.getName())
							&& lstDoc.get(i).getString(FieldType.STATUS.getName()).equals(StatusType.RECONCILED.getName())) {
						reconciledFlag = true;
						break;
					}
				}
				if(!reconciledFlag) {
						Document doc = lstDoc.get(0);
						RefundValidation validationReport = new RefundValidation();
						if(insertRecoTxn(doc)) {
							//saleDoc = getSaleTxn(doc.getString(FieldType.OID.toString()));
							validationReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
							validationReport.setRefundTag(doc.getString(FieldType.REFUND_FLAG.toString()));
							validationReport.setRefundAmt(doc.getString(FieldType.AMOUNT.toString()));
							validationReport.setBankTxnId(doc.getString(FieldType.ORIG_TXN_ID.toString()));
							if(doc.getString(FieldType.TXNTYPE.toString()).equals(TransactionType.REFUND.getName()) && doc.getString(FieldType.STATUS.toString()).equals(StatusType.CAPTURED.getName())) {
								validationReport.setRefundStatus(Constants.SUCCESS.getValue());
							} else {
								validationReport.setRefundStatus(Constants.FAIL.getValue());
							}
							/*validationReport.setRefundStatus(doc.getString(FieldType.STATUS.toString()));*/
							//validationReport.setRefundProcessDate(doc.getString(FieldType.CREATE_DATE.toString()));
							validationReport.setRefundProcessDate(DateCreater.formatDateReco(doc.getString(FieldType.CREATE_DATE.toString())));
							validationReport.setRefundBankTxnId(doc.getString(FieldType.PG_REF_NUM.toString()));
							/*validationReport.setCancelTxnDate(DateCreater.formatDateReco(getRefundDate(doc.getString(FieldType.OID.toString()))));*/
							validationReport.setCancelTxnDate(DateCreater.formatDateReco(doc.getString(FieldType.REQUEST_DATE.toString())));
							validationReport.setBookingTxnAmt(doc.getString(FieldType.SALE_AMOUNT.toString()));
							validationReport.setCancelTxnId(doc.getString(FieldType.REFUND_ORDER_ID.toString()));
							if(doc.getString(FieldType.TXNTYPE.toString()).equals(TransactionType.REFUND.getName()) && doc.getString(FieldType.STATUS.toString()).equals(StatusType.CAPTURED.getName())) {
								validationReport.setRemarks(Constants.SUCCESS.getValue());
							} else {
								validationReport.setRemarks(doc.getString(FieldType.RESPONSE_MESSAGE.toString()));
							}
							
							
							refundValidationList.add(validationReport);
					}
				}
			}
			cursor.close();
		} catch (Exception exception) {
			logger.error("Inside DownloadRefundValidationFile Class, in getFinalVersionData method  : ", exception);
		}
		return refundValidationList;
	}
	
	public List<RefundValidationDetails> getAllData(String merchantPayId, Date fromDate) {
		logger.info("Inside DownloadRefundValidationFile Class, in getData Method !!"); 
		List<RefundValidationDetails> refundValidationList = new ArrayList<RefundValidationDetails>();
		try {
				refundValidationList = refundValidationDetailsDao.getAllRefundValidationDetailsList(fromDate, merchantPayId);
		} catch (Exception exception) {
			logger.error("Inside DownloadRefundValidationFile Class, in getdata method  : ", exception);
		}
		return refundValidationList;
	}
	
	private Document getSaleTxn(String oid) {
		Document doc =  new Document();
		
		try {
			MongoDatabase dbIns = mongoInstance.getDB();  
			MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			List<BasicDBObject> saleAmtConditionList = new ArrayList<BasicDBObject>();
			saleAmtConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),  TransactionType.SALE.getName()));
			saleAmtConditionList.add(new BasicDBObject(FieldType.STATUS.getName(),  StatusType.CAPTURED.getName()));
			saleAmtConditionList.add(new BasicDBObject(FieldType.OID.getName(),oid));
			
			BasicDBObject finalquery = new BasicDBObject("$and", saleAmtConditionList);
			 doc = coll.find(finalquery).first();
		} catch (Exception exception) {
			logger.error("Inside DownloadRefundValidationFile Class, in getSaleAmt method  : ", exception);
		}
		return doc;
	}
	
	public String getTotalNumOfRefundTxnsStatus(String merchantPayId, String fromDate) {
		logger.info("Inside RefundValidationTicketingData Class, in getTotalNumOfRefundTxnsStatus Method !!");
		Integer rowCount = 0;
		//List<RefundValidation> refundValidationList = new ArrayList<RefundValidation>();
		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			//BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			//List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
			
			if (!fromDate.isEmpty()) {
				String currentDate = null;
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				 // add days to from date				
				Date date1= dateFormat.parse(fromDate);	
				cal.setTime(date1);
				cal.add(Calendar.DATE, 1);
				currentDate = dateFormat.format(cal.getTime());	

				dateQuery.put(FieldType.REQUEST_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lt", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}
	
			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}
			List<BasicDBObject> refundValidationConditionList = new ArrayList<BasicDBObject>();
			refundValidationConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),  TransactionType.REFUND.getName()));
			/*ArrayList<String> statusList=new ArrayList<>();
			statusList.add(StatusType.CAPTURED.getName());
			statusList.add(StatusType.RECONCILED.getName());
			statusList.add(StatusType.REJECTED.getName());
			refundValidationConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), new BasicDBObject("$in",statusList)));*/
			
			BasicDBObject refundValidationQuery = new BasicDBObject("$and", refundValidationConditionList);
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			
			/*if (!currencyQuery.isEmpty()) {
				allConditionQueryList.add(currencyQuery);
			}*/
			
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			
			if (!refundValidationQuery.isEmpty()) {
				allConditionQueryList.add(refundValidationQuery);
			}
			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();
			
			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}			
			
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			
			MongoDatabase dbIns = mongoInstance.getDB();  
			MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			
			logger.info("RefundValidationTicketingData final Query : " + finalquery);
			logger.info("No. of Records : " + coll.count(finalquery));
			
			// Now the aggregate operation
			Document firstGroup = new Document("_id",
					new Document("REFUND_ORDER_ID", "$REFUND_ORDER_ID"));
			
			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			
			MongoCursor<Document> cursor = output.iterator();
			//Boolean reconciledFlag = false;
			
			
			logger.info("DownloadRefundValidationFile final Query : " + pipeline);
			//logger.info("No. of Records : " + coll.count(finalquery));
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				rowCount ++;
			}
			cursor.close();
			logger.info("No. of Records : " + rowCount);

		} catch (Exception exception) {
			logger.error("Inside DownloadRefundValidationFile Class, in getFinalVersionData method  : ", exception);
		}
		return rowCount.toString();
	}
	
	/*private String getRefundDate(String oid) {
	String refundDate = "";
	try {
		MongoDatabase dbIns = mongoInstance.getDB();  
		MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
		List<BasicDBObject> refundDateConditionList = new ArrayList<BasicDBObject>();
		refundDateConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),  TransactionType.REFUND.getName()));
		refundDateConditionList.add(new BasicDBObject(FieldType.STATUS.getName(),  StatusType.CAPTURED.getName()));
		refundDateConditionList.add(new BasicDBObject(FieldType.OID.getName(),oid));
		
		BasicDBObject finalquery = new BasicDBObject("$and", refundDateConditionList);
		MongoCursor<Document> cursor = coll.find(finalquery).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			refundDate = doc.getString(FieldType.CREATE_DATE.getName());
		}
	} catch (Exception exception) {
		logger.error("Inside DownloadRefundValidationFile Class, in getRefundDate method  : ", exception);
	}
	return refundDate;
}*/

private boolean insertRecoTxn(Document doc) {
		logger.info("Inside DownloadRefundValidationTicketingFile Class, In insertRecoTxn Method !!");
		try {
			Fields fields =new Fields();
			fields =  fieldsDao.getFields(doc.getString(FieldType.TXN_ID.toString()), doc.getString(FieldType.PAY_ID.toString()));
			fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId().toString());
			fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.REFUND.getName());
			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(doc.getString(FieldType.AMOUNT.toString()), doc.getString(FieldType.CURRENCY_CODE.toString())));
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName());
			fields.put(FieldType.STATUS.getName(), StatusType.RECONCILED.getName());	
			fields.put(FieldType.TOTAL_AMOUNT.getName(), Amount.formatAmount(doc.getString(FieldType.TOTAL_AMOUNT.toString()), doc.getString(FieldType.CURRENCY_CODE.toString())));
			fields.put(FieldType.REQUEST_DATE.getName(), doc.getString(FieldType.REQUEST_DATE.toString()));
			if(doc.get(FieldType.TXNTYPE.getName()).equals(TransactionType.RECO.getName()) && 
					doc.get(FieldType.STATUS.getName()).equals(StatusType.SETTLED.getName())) {
				fields.put(FieldType.PG_DATE_TIME.getName(), doc.get(FieldType.PG_DATE_TIME.getName()).toString());
			} else if(doc.get(FieldType.TXNTYPE.getName()).equals(TransactionType.SALE.getName()) && 
					doc.get(FieldType.STATUS.getName()).equals(StatusType.CAPTURED.getName())) {
				fields.put(FieldType.PG_DATE_TIME.getName(), doc.get(FieldType.CREATE_DATE.getName()).toString());
			} else {
				fields.put(FieldType.PG_DATE_TIME.getName(), fieldsDao.getSaleDate(doc.get(FieldType.PG_REF_NUM.getName()).toString()));
			}
			fields.put(FieldType.RESPONSE_CODE.getName(), Constants.SUCCESS_CODE.getValue());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "SUCCESS");
			fieldsDao.insertTransaction(fields);
			return true;
		} catch (Exception exception) {
			logger.error("DownloadRefundValidationTicketingFile Class, In insertRecoTxn Method : ", exception);
			return false;
		}		
	}

	public boolean isRefundReconcilationExists(String merchantPayId, String refundRequestDate) {
		boolean countFlag = false;
		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			//BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			//List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
			
			if (!refundRequestDate.isEmpty()) {
				String currentDate = null;
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				 // add days to from date				
				Date date1= dateFormat.parse(refundRequestDate);	
				cal.setTime(date1);
				cal.add(Calendar.DATE, 1);
				currentDate = dateFormat.format(cal.getTime());	

				dateQuery.put(FieldType.REQUEST_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(refundRequestDate).toLocalizedPattern())
								.add("$lt", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}
	
			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}
			List<BasicDBObject> refundValidationConditionList = new ArrayList<BasicDBObject>();
			refundValidationConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),  TransactionType.REFUND.getName()));
			ArrayList<String> statusList=new ArrayList<>();
			statusList.add(StatusType.RECONCILED.getName());
			refundValidationConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), new BasicDBObject("$in",statusList)));
			
			BasicDBObject refundValidationQuery = new BasicDBObject("$and", refundValidationConditionList);
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			
			if (!refundValidationQuery.isEmpty()) {
				allConditionQueryList.add(refundValidationQuery);
			}
			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();
			
			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}	
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}			
			
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			
			MongoDatabase dbIns = mongoInstance.getDB();  
			MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			int rowCount = (int)coll.count(finalquery);			
			
			if(rowCount>0) {
				countFlag = true;				
			} else {
				countFlag = false;
			}
			
			logger.info("RefundValidationTicketingData final Query : " + finalquery);
			logger.info("No. of Records : " + rowCount);
			
		} catch (Exception exception) {
			logger.error("Inside DownloadRefundValidationFile Class, in getFinalVersionData method  : ", exception);
			
		}
		
		return countFlag;
	}
}
