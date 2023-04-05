package com.paymentgateway.crm.mongoReports;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.NodalTransactions;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;

@Component
public class NodalTxnReports {

	private static Logger logger = LoggerFactory.getLogger(NodalTxnReports.class.getName());
	private static final String alphabaticFileName = "alphabatic-currencycode.properties";
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private UserDao userdao;
	
	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private FieldsDao fieldsDao;
	
	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;

	public List<NodalTransactions> searchPayment(String txnId, String oid, String status,
			String paymentType, String fromDate,
			String toDate, int start, int length) {

		Map<String, User> userMap = new HashMap<String, User>();

		logger.info("Inside NodalTxnReports , searchPayment");

		boolean isParameterised = false;
		try {
			List<NodalTransactions> transactionList = new ArrayList<NodalTransactions>();

			PropertiesManager propManager = new PropertiesManager();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			if (!fromDate.isEmpty()) {
				String currentDate = null;
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (!txnId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.TXN_ID.getName(), txnId));

				isParameterised = true;
			}
			if (!oid.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.OID.getName(), oid));
			}
			if (!status.equalsIgnoreCase("ALL")){
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			}
			if (!paymentType.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}
			
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
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

			logger.info("Inside NodalTxnReports , searchPayment , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.SETTLEMENT_COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			Document firstGroup;
			if (isParameterised) {
				firstGroup = new Document("_id", new Document("_id", "$_id"));
			} else {
				firstGroup = new Document("_id", new Document("OID", "$OID").append("ORIG_TXNTYPE", "$ORIG_TXNTYPE"));
			}

			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("INSERTION_DATE", -1));
			BasicDBObject skip = new BasicDBObject("$skip", start);
			BasicDBObject limit = new BasicDBObject("$limit", length);

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group, skip, limit);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document mydata = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					mydata = dataEncDecTool.decryptDocument(mydata);
				}
				List<Document> courses = (List<Document>) mydata.get("entries");
				Document dbobj = courses.get(0);

				NodalTransactions transReport = new NodalTransactions();
				BigInteger txnID = new BigInteger(((Document) dbobj).getString(FieldType.TXN_ID.toString()));
				transReport.setTxnId((String.valueOf(txnID)));
				transReport.setOid(((Document) dbobj).getString(FieldType.OID.toString()));
				transReport.setCreatedDate(((Document) dbobj).getString(FieldType.CREATE_DATE.toString()));
				transReport.setCustomerId(((Document) dbobj).getString(FieldType.CUSTOMER_ID.toString()));
				transReport.setSrcAccNo(((Document) dbobj).getString(FieldType.SRC_ACCOUNT_NO.toString()));
				transReport.setAmount(((Document) dbobj).getString(FieldType.AMOUNT.toString()));
				transReport.setTxnType(((Document) dbobj).getString(FieldType.TXNTYPE.toString()));
				transReport.setStatus(((Document) dbobj).getString(FieldType.STATUS.toString()));
				transReport.setComments(((Document) dbobj).getString(FieldType.PRODUCT_DESC.toString()));
				transReport.setAcquirer(((Document) dbobj).getString(FieldType.ACQUIRER_TYPE.toString()));
				transReport.setPaymentType(((Document) dbobj).getString(FieldType.PAYMENT_TYPE.toString()));
				transReport.setBeneAccNo(((Document) dbobj).getString(FieldType.BENE_ACCOUNT_NO.toString()));
				transReport.setBeneficiaryName(((Document) dbobj).getString(FieldType.BENE_NAME.toString()));
				transReport.setBeneficiaryCode(((Document) dbobj).getString(FieldType.BENEFICIARY_CD.toString()));
				transactionList.add(transReport);
				
			//	Collections.sort(transactionList, comp);
			}
			cursor.close();
			logger.info("Inside NodalTxnReports , searchPayment , transactionListSize = " + transactionList.size());
			return transactionList;
		}

		catch (Exception e) {
			logger.error("Exception occured in NodalTxnReports , searchPayment , Exception = " , e);
			return null;
		}
	}

	public int searchPaymentCount(String txnId, String oid, String status,
			String paymentType, String fromDate,
			String toDate) {

		logger.info("Inside NodalTxnReports , searchPaymentCount");
		boolean isParameterised = false;
		try {
			int total;
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			if (!fromDate.isEmpty()) {

				String currentDate = null;
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (!txnId.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.TXN_ID.getName(), txnId));
			}
			if (!oid.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.OID.getName(), oid));
			}
			if (!paymentType.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}
			if (!status.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			} else {

			}
			
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}

			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			logger.info("Inside NodalTxnReports , searchPaymentCount , finalList = " + fianlList);
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.SETTLEMENT_COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			// Now the aggregate operation

			Document firstGroup;
			if (isParameterised) {
				firstGroup = new Document("_id", new Document("_id", "$_id"));
			}

			else {
				firstGroup = new Document("_id", new Document("OID", "$OID").append("ORIG_TXNTYPE", "$ORIG_TXNTYPE"));
			}
			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject groupObject = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("INSERTION_DATE", -1));
			BasicDBObject count = new BasicDBObject("$count", "totalCount");
			List<BasicDBObject> pipeline;
			if (isParameterised) {
				pipeline = Arrays.asList(match, sort, count);
			} else {
				pipeline = Arrays.asList(match, groupObject, sort, count);
			}

			Document output = coll.aggregate(pipeline).first();
			total = (int) output.get("totalCount");
			return total;

		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPaymentCount n exception = " , e);
			return 0;
		}
	}
	
	public List<TransactionSearch> searchPaymentForDownload(String merchantPayId, String paymentType, String status,
			String currency, String transactionType, String fromDate, String toDate, User user, String paymentsRegion,
			String acquirer) {
		logger.info("Inside TxnReports , searchPayment");
		Map<String, User> userMap = new HashMap<String, User>();
		boolean isParameterised = false;
		try {
			List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();

			PropertiesManager propManager = new PropertiesManager();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject acquirerQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			if (!fromDate.isEmpty()) {
				String currentDate = null;
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));

				isParameterised = true;
			}
			if (!status.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			} else {

			}

			if (!currency.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			} else {

			}

			if (!transactionType.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), transactionType));
			} else {

			}
			if (!paymentType.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			} else {

			}
			
			if (!paymentsRegion.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentsRegion));
			} else {

			}
			
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			if (!acquirer.equalsIgnoreCase("ALL")) {
				List<String> acquirerList = Arrays.asList(acquirer.split(","));
				for (String acq : acquirerList) {

					acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acq));
				}
				acquirerQuery.append("$or", acquirerConditionLst);

			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}

			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			if (!acquirerQuery.isEmpty()) {
				fianlList.add(acquirerQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside TxnReports , searchPayment , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			// Now the aggregate operation ()In case any parameter is passed in search query
			// , then show all records

			Document firstGroup;
			if (isParameterised) {
				firstGroup = new Document("_id", new Document("_id", "$_id"));
			} else {
				firstGroup = new Document("_id", new Document("OID", "$OID").append("ORIG_TXNTYPE", "$ORIG_TXNTYPE"));
			}

			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("INSERTION_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document mydata = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					mydata = dataEncDecTool.decryptDocument(mydata);
				}
				
				List<Document> courses = (List<Document>) mydata.get("entries");
				Document dbobj = courses.get(0);

				TransactionSearch transReport = new TransactionSearch();
				BigInteger txnID = new BigInteger(((Document) dbobj).getString(FieldType.TXN_ID.toString()));
				transReport.setTransactionId((txnID));
				transReport.setTransactionIdString((String.valueOf(txnID)));
				transReport.setPgRefNum(((Document) dbobj).getString(FieldType.PG_REF_NUM.toString()));
				transReport.setPayId(((Document) dbobj).getString(FieldType.PAY_ID.toString()));
				transReport.setCustomerName(((Document) dbobj).getString(FieldType.CUST_NAME.toString()));
				transReport.setTransactionRegion(((Document) dbobj).getString(FieldType.PAYMENTS_REGION.toString()));
				transReport.setMerchants(dbobj.getString(CrmFieldType.BUSINESS_NAME.getName()));
				transReport.setPostSettledFlag(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()));
				if (dbobj.getString(FieldType.UDF6.getName()) != null) {
					transReport.setDeltaRefundFlag(dbobj.getString(FieldType.UDF6.getName()));
				} else {
					transReport.setDeltaRefundFlag("");
				}

				String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

				User user1 = new User();

				if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
					user1 = userMap.get(payid);
				} else {
					user1 = userdao.findPayId(payid);
					userMap.put(payid, user1);
				}

				transReport.setMerchants(user1.getBusinessName());
				if (null != dbobj.getString(FieldType.ORIG_TXNTYPE.toString())) {
					transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
				} else {

					// If ORIG_TXN_TYPE is not available incase of a timeout , set TXNTYPE instead
					// of ORIG_TXN_TYPE
					if (dbobj.getString(FieldType.STATUS.toString()).equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
						transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					} else {
						transReport.setTxnType(CrmFieldConstants.NA.getValue());
					}

				}

				if (null != dbobj.getString(FieldType.MOP_TYPE.toString())) {
					transReport.setMopType(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));
				} else {
					transReport.setMopType(CrmFieldConstants.NA.getValue());
				}

				if (null != dbobj.getString(FieldType.ACQUIRER_TYPE.toString())) {
					transReport.setAcquirerType(dbobj.getString(FieldType.ACQUIRER_TYPE.toString()));
				} else {
					transReport.setAcquirerType(CrmFieldConstants.NA.getValue());
				}

				if (null != dbobj.getString(FieldType.PAYMENT_TYPE.toString())) {
					transReport.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transReport.setPaymentMethods(CrmFieldConstants.NA.getValue());
				}

				transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
				transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				transReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				transReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
				transReport.setoId(dbobj.getString(FieldType.OID.toString()));
				transReport.setTransactionCaptureDate(dbobj.getString(FieldType.PG_DATE_TIME.toString()));
				if (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()) != null) {
					transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transReport.setTotalAmount("");
				}

				if (dbobj.getString(FieldType.ACQ_ID.toString()) != null) {
					transReport.setAcqId(dbobj.getString(FieldType.ACQ_ID.toString()));
				} else {
					transReport.setAcqId(CrmFieldConstants.NA.getValue());
				}

				if (dbobj.getString(FieldType.RRN.toString()) != null) {
					transReport.setRrn(dbobj.getString(FieldType.RRN.toString()));
				} else {
					transReport.setRrn(CrmFieldConstants.NA.getValue());
				}

				if (null != dbobj.getString(FieldType.CURRENCY_CODE.toString())) {
					transReport.setCurrency(
							propManager.getAlphabaticCurrencyCode(dbobj.getString(FieldType.CURRENCY_CODE.toString())));
				} else {
					transReport.setCurrency(CrmFieldConstants.NA.getValue());
				}

				transactionList.add(transReport);

			}
			cursor.close();
			logger.info("Inside TxnReports , searchPayment , transactionListSize = " + transactionList.size());
			Comparator<TransactionSearch> comp = (TransactionSearch a, TransactionSearch b) -> {

				if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
					return -1;
				} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
					return 1;
				} else {
					return 0;
				}
			};
			Collections.sort(transactionList, comp);
			return transactionList;
		}

		catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPayment , Exception = " , e);
			return null;
		}
	}

	public List<TransactionSearch> searchRejectedRefund(String merchant, String orderId,
			String fromDate, String toDate, String refundType) {

		List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
		try {

			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject acquirerQuery = new BasicDBObject();

			BasicDBObject allParamQuery = new BasicDBObject();
			List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> saleOrAuthList = new ArrayList<BasicDBObject>();

			if (!fromDate.isEmpty()) {

				String currentDate = null;
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (!merchant.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchant));
			}
			
			if (StringUtils.isNotBlank(orderId)) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			}
			
			
			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
			ArrayList<String> list=new ArrayList<>();
			list.add(StatusType.CAPTURED.getName());
			list.add(StatusType.DECLINED.getName());
			list.add(StatusType.REJECTED.getName());
			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), new BasicDBObject("$in",list)));
			if(refundType.equals("DELTAREFUND")) {
				paramConditionLst.add(new BasicDBObject(FieldType.UDF6.getName(), Constants.Y.name()));
			}
			    
			
			BasicDBObject refundConditionQuery = new BasicDBObject("$and", paramConditionLst);

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			
			if (!acquirerQuery.isEmpty()) {
				allConditionQueryList.add(acquirerQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
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

			logger.info("Inside search summary report query , finalquery = " + finalquery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			// Now the aggregate operation
			Document firstGroup = new Document("_id",
					new Document("PG_REF_NUM", "$PG_REF_NUM"));
			
			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
	
			MongoCursor<Document> cursor = output.iterator();
			Boolean capturedFlag = false;
			while (cursor.hasNext()) {
				capturedFlag = false;	
				Document dbobj = cursor.next();
				List<Document> lstDoc = (List<Document>) dbobj.get("entries");
				for(int i=0; i<lstDoc.size();i++) {
					if(lstDoc.get(i).getString(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUND.getName())
							&& lstDoc.get(i).getString(FieldType.STATUS.getName()).equals(StatusType.CAPTURED.getName())) {
						capturedFlag = true;
						break;
					}
				}
				
				if(!capturedFlag) {
					Document doc = lstDoc.get(0);
					TransactionSearch transactionSearch = new TransactionSearch();

					List<Fields> fieldsList = new ArrayList<Fields>();
					fieldsList = fieldsDao.getPreviousSaleCapturedForOrderId(doc.getString(FieldType.ORDER_ID.toString()));
					
					transactionSearch.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
					transactionSearch.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.toString()));
					transactionSearch.setRefundDate(doc.getString(FieldType.CREATE_DATE.toString()));
	 				transactionSearch.setRefundAmount(doc.getString(FieldType.AMOUNT.toString()));	
					transactionSearch.setTotalAmount(fieldsList.get(0).get(FieldType.AMOUNT.getName()));
					transactionSearch.setRefundFlag(doc.getString(FieldType.REFUND_FLAG.toString()));	
					transactionSearch.setDateFrom(fieldsList.get(0).get(FieldType.CREATE_DATE.getName()));
					transactionList.add(transactionSearch);
				}
			}

			cursor.close();
			Comparator<TransactionSearch> comp = (TransactionSearch a, TransactionSearch b) -> {

				if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
					return -1;
				} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
					return 1;
				} else {
					return 0;
				}
			};
			
			return transactionList;
		

		}

		catch (Exception e) {
			logger.error("Exception in getting records for refund reject report " , e);
		}

		return transactionList;
	}

}
