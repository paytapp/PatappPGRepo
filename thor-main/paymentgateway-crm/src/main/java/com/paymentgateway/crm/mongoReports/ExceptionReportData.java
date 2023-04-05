package com.paymentgateway.crm.mongoReports;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
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
import com.paymentgateway.commons.user.ExceptionReport;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class ExceptionReportData {

	private static Logger logger = LoggerFactory.getLogger(TxnReports.class.getName());
	private static final String alphabaticFileName = "alphabatic-currencycode.properties";

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	UserDao userDao;
	
	private static final String prefix = "MONGO_DB_";

	@SuppressWarnings("deprecation")
	public int getDataCount(String merchantPayId, String acquirer, String status, String fromDate, String toDate, String DB_USER_TYPE, String settledFlag) {
		try {
		int total;
		//settledFlag = "ALL";
		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject allParamQuery = new BasicDBObject();
		/*List<BasicDBObject> statusConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject statusQuery = new BasicDBObject();
		BasicDBObject acquirerQuery = new BasicDBObject();*/
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

		if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.DB_PAY_ID.getName(), merchantPayId));
		}
		
		if (StringUtils.isNotBlank(acquirer) && !acquirer.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.DB_ACQUIRER_TYPE.getName(), acquirer));			
		}
		
		if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.EXCEPTION_STATUS.getName(), status));
		}
		if (StringUtils.isNotBlank(settledFlag) && !settledFlag.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.SETTLED_FLAG.getName(), settledFlag));
		}
		
		paramConditionLst.add(new BasicDBObject(FieldType.DB_USER_TYPE.getName(), DB_USER_TYPE));
		
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

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll =
		dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.REPORTING_COLLECTION_NAME.getValue()));
		//MongoCollection<Document> coll = dbIns.getCollection("reporting");
		
		/*BasicDBObject match = new BasicDBObject("$match", finalquery);
		// Now the aggregate operation
		Document firstGroup = new Document("_id",
				new Document("DB_PG_REF_NUM", "$DB_PG_REF_NUM"));
		BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
		BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
		BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		BasicDBObject count = new BasicDBObject("$count", "totalCount");
		List<BasicDBObject> pipeline = Arrays.asList(match, group, sort, count);
		Document output = coll.aggregate(pipeline).first();
		total = (int) output.get("totalCount");*/
		total = (int) coll.count(finalquery);
		logger.info("Inside search Exception report count ,total records from DB  = " + total);
		
		//MongoCursor<Document> cursor = output.iterator();
		//MongoCollection<Document> coll = dbIns.getCollection("reporting");
		//total = (int) coll.count(output);

		return total;
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPaymentCount n exception = " , e);
			return 0;
		}
	}

	public List<ExceptionReport> getData(String merchantPayId, String acquirer, String status, String fromDate,
			String toDate, int start, int length, String DB_USER_TYPE, String settledFlag) {
		try {
		List<ExceptionReport> exceptionReportList = new ArrayList<ExceptionReport>();

		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject allParamQuery = new BasicDBObject();
		/*List<BasicDBObject> statusConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject statusQuery = new BasicDBObject();
		BasicDBObject acquirerQuery = new BasicDBObject();*/
		

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

		if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.DB_PAY_ID.getName(), merchantPayId));
		}
		
		if (StringUtils.isNotBlank(acquirer) && !acquirer.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.DB_ACQUIRER_TYPE.getName(), acquirer));			
		}
		
		if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.EXCEPTION_STATUS.getName(), status));
		}
		if (StringUtils.isNotBlank(settledFlag) && !settledFlag.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.SETTLED_FLAG.getName(), settledFlag));
		}
		
		paramConditionLst.add(new BasicDBObject(FieldType.DB_USER_TYPE.getName(), DB_USER_TYPE));
		
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

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll =
		dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.REPORTING_COLLECTION_NAME.getValue()));
		//MongoCollection<Document> coll = dbIns.getCollection("reporting");
		
		BasicDBObject match = new BasicDBObject("$match", finalquery);
		// Now the aggregate operation
/*		Document firstGroup = new Document("_id",
				new Document("DB_PG_REF_NUM", "$DB_PG_REF_NUM"));
		BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
		BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
		BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));*/
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		BasicDBObject skip = new BasicDBObject("$skip", start);
		BasicDBObject limit = new BasicDBObject("$limit", length);
		List<BasicDBObject> pipeline = Arrays.asList(match, sort, skip, limit);
		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
				
		//MongoCursor<Document> cursor = coll.find(finalquery).iterator();
		while (cursor.hasNext()) {
			/*Document myDoc = cursor.next();
			List<Document> lstDoc = (List<Document>) myDoc.get("entries");
			Document doc = lstDoc.get(0);*/
			Document doc = cursor.next();
			ExceptionReport exceptionReport = new ExceptionReport();

			exceptionReport.setPgRefNo(doc.getString(FieldType.DB_PG_REF_NUM.toString()));
			//BigInteger txnId = new BigInteger(doc.getString(FieldType.TXN_ID.toString()));
			exceptionReport.setTxnId(doc.getString(FieldType.TXN_ID.toString()));
			exceptionReport.setOrderId(doc.getString(FieldType.DB_ORDER_ID.toString()));
			exceptionReport.setAcqId(doc.getString(FieldType.DB_ACQUIRER_TYPE.toString()));
			exceptionReport.setCreatedDate(doc.getString(FieldType.CREATE_DATE.toString()));
			
			if(StringUtils.isNotBlank(doc.getString(FieldType.SETTLED_FLAG.getName()))) {
				exceptionReport.setSettledFlag(doc.getString(FieldType.SETTLED_FLAG.getName()));
			} else {
				exceptionReport.setSettledFlag(Constants.N_FLAG.getValue());
			}
			if(StringUtils.isNotBlank(doc.getString(FieldType.DB_PG_SETTLED_AMOUNT.getName()))) {
				exceptionReport.setPgSettledAmount(doc.getString(FieldType.DB_PG_SETTLED_AMOUNT.getName()));
			} else {
				exceptionReport.setPgSettledAmount(Constants.NA.getValue());
			}
			
			if(StringUtils.isNotBlank(doc.getString(FieldType.DB_ACQUIRER_SETTLED_AMOUNT.getName()))) {
				exceptionReport.setAcqSettledAmount(doc.getString(FieldType.DB_ACQUIRER_SETTLED_AMOUNT.getName()));
			} else {
				exceptionReport.setAcqSettledAmount(Constants.NA.getValue());
			}
			if(StringUtils.isNotBlank(doc.getString(FieldType.DB_DIFFERENCE_AMOUNT.getName()))) {
				exceptionReport.setDiffAmount(doc.getString(FieldType.DB_DIFFERENCE_AMOUNT.getName()));
			} else {
				exceptionReport.setDiffAmount(Constants.NA.getValue());
			}
			
			if (doc.getString(FieldType.DB_PAY_ID.toString()) == null) {
				exceptionReport.setMerchant("");
			} else {
			exceptionReport.setMerchant(userDao.getMerchantByPayId(doc.getString(FieldType.DB_PAY_ID.toString())));
			}
			exceptionReport.setStatus(doc.getString(FieldType.EXCEPTION_STATUS.toString()));
			exceptionReport.setException(doc.getString(FieldType.RESPONSE_MESSAGE.toString()));

			Comparator<ExceptionReport> comp = (ExceptionReport a, ExceptionReport b) -> {

				if (a.getCreatedDate().compareTo(b.getCreatedDate()) > 0) {
					return -1;
				} else if (a.getCreatedDate().compareTo(b.getCreatedDate()) < 0) {
					return 1;
				} else {
					return 0;
				}
			};
			
			exceptionReportList.add(exceptionReport);
			Collections.sort(exceptionReportList, comp);
		}
		cursor.close();
		return exceptionReportList;
		
		} catch (Exception e) {
			logger.error("Exception occured in ExceptionReportData , ExceptionReport , Exception = " , e);
			return null;
		}
	}
	
	public List<ExceptionReport> getDataForDownload(String merchantPayId, String acquirer, String status, String fromDate,
			String toDate, String DB_USER_TYPE, String settledFlag) {
		try {
		List<ExceptionReport> exceptionReportList = new ArrayList<ExceptionReport>();

		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject allParamQuery = new BasicDBObject();
		/*List<BasicDBObject> statusConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject statusQuery = new BasicDBObject();
		BasicDBObject acquirerQuery = new BasicDBObject();*/
		

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

		if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.DB_PAY_ID.getName(), merchantPayId));
		}
		
		if (StringUtils.isNotBlank(acquirer) && !acquirer.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.DB_ACQUIRER_TYPE.getName(), acquirer));			
		}
		
		if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.EXCEPTION_STATUS.getName(), status));
		}
		if (StringUtils.isNotBlank(settledFlag) && !settledFlag.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.SETTLED_FLAG.getName(), settledFlag));
		}
		
		paramConditionLst.add(new BasicDBObject(FieldType.DB_USER_TYPE.getName(), DB_USER_TYPE));
		
		
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

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll =
		dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.REPORTING_COLLECTION_NAME.getValue()));
		//MongoCollection<Document> coll = dbIns.getCollection("reporting");
		
		BasicDBObject match = new BasicDBObject("$match", finalquery);
		// Now the aggregate operation
/*		Document firstGroup = new Document("_id",
				new Document("DB_PG_REF_NUM", "$DB_PG_REF_NUM"));
		BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
		BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
		BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));*/
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		List<BasicDBObject> pipeline = Arrays.asList(match, sort);
		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
				
		//MongoCursor<Document> cursor = coll.find(finalquery).iterator();
		while (cursor.hasNext()) {
			/*Document myDoc = cursor.next();
			List<Document> lstDoc = (List<Document>) myDoc.get("entries");
			Document doc = lstDoc.get(0);*/
			Document doc = cursor.next();
			ExceptionReport exceptionReport = new ExceptionReport();

			exceptionReport.setPgRefNo(doc.getString(FieldType.DB_PG_REF_NUM.toString()));
			//String txnId = new String(doc.getString(FieldType.TXN_ID.toString()));
			exceptionReport.setTxnId(doc.getString(FieldType.TXN_ID.toString()));
			exceptionReport.setOrderId(doc.getString(FieldType.DB_ORDER_ID.toString()));
			exceptionReport.setAcqId(doc.getString(FieldType.DB_ACQUIRER_TYPE.toString()));
			exceptionReport.setCreatedDate(doc.getString(FieldType.CREATE_DATE.toString()));
			exceptionReport.setSettledFlag(doc.getString(FieldType.SETTLED_FLAG.getName()));
			exceptionReport.setPgSettledAmount(doc.getString(FieldType.DB_PG_SETTLED_AMOUNT.getName()));
			exceptionReport.setAcqSettledAmount(doc.getString(FieldType.DB_ACQUIRER_SETTLED_AMOUNT.getName()));
			exceptionReport.setDiffAmount(doc.getString(FieldType.DB_DIFFERENCE_AMOUNT.getName()));
			if (doc.getString(FieldType.DB_PAY_ID.toString()) == null) {
				exceptionReport.setMerchant("");
			} else {
			exceptionReport.setMerchant(userDao.getMerchantByPayId(doc.getString(FieldType.DB_PAY_ID.toString())));
			}
			exceptionReport.setStatus(doc.getString(FieldType.EXCEPTION_STATUS.toString()));
			exceptionReport.setException(doc.getString(FieldType.RESPONSE_MESSAGE.toString()));

			Comparator<ExceptionReport> comp = (ExceptionReport a, ExceptionReport b) -> {

				if (a.getCreatedDate().compareTo(b.getCreatedDate()) > 0) {
					return -1;
				} else if (a.getCreatedDate().compareTo(b.getCreatedDate()) < 0) {
					return 1;
				} else {
					return 0;
				}
			};
			
			exceptionReportList.add(exceptionReport);
			Collections.sort(exceptionReportList, comp);
		}
		cursor.close();
		return exceptionReportList;
		
		} catch (Exception e) {
			logger.error("Exception occured in ExceptionReportData , ExceptionReport , Exception = " , e);
			return null;
		}
	}
}
