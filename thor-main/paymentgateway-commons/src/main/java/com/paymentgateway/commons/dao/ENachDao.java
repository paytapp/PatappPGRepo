package com.paymentgateway.commons.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;

@Service
public class ENachDao {

	@Autowired
	private UserDao userDao;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	private static Logger logger = LoggerFactory.getLogger(ENachDao.class.getName());
	private static final String prefix = "MONGO_DB_";

	public List<String> getDueDateList(String startDATE, String valueTenure, String payId, String subMerchantPayId,
			String frequency) {

		logger.info("inside getDueDateList!");

		List<String> dateList = new ArrayList<String>();

		try {
			int tenure = Integer.parseInt(valueTenure);

			LocalDate startDate = LocalDate.parse(startDATE);

			switch (frequency) {

			case "DAIL":// Daily
				dateList = getDueDateListDaily(startDate, tenure);
				break;
			case "WEEK":// Weekly
				dateList = getDueDateListWeekly(startDate, tenure);
				break;
			case "BIMN":// Bi- Monthly
				dateList = getDueDateListByBiMonthly(startDate, tenure);
				break;
			case "MNTH":// Monthly
				dateList = getDueDateListByMonth(startDate, tenure);
				break;
			case "ADHO":// As and when presented (ADHO)
				dateList = getDueDateListWhenPresented(startDate, tenure);
				break;
			case "QURT":// Quarterly
				dateList = getDueDateListQuarterly(startDate, tenure);
				break;
			case "MIAN":// Semi Annually
				dateList = getDueDateListSemiAnnual(startDate, tenure);
				break;
			case "YEAR":// Yearly
				dateList = getDueDateListYearly(startDate, tenure);
				break;

			}
		} catch (NumberFormatException e) {
			logger.error("Error while fetching due date list, " + e);
		}
		logger.info("due date list: " + dateList);
		return dateList;
	}

	private List<String> getDueDateListWhenPresented(LocalDate date, int tenure) {
		date = date.plusYears(100);
		List<String> dateList = new ArrayList<String>();
		for (int i = 0; i < tenure; i++) {
			dateList.add(date.toString());
		}
		return dateList;
	}

	private List<String> getDueDateListYearly(LocalDate date, int tenure) {
		List<String> dateList = new ArrayList<String>();

		if (29 <= date.getDayOfMonth()) {

			for (int i = 0; i < tenure; i++) {
				if (i == 0) {
					dateList.add(date.toString());
					continue;
				}
				LocalDate nextDate = date.plusYears(1);
				int maxDateOfMonth = nextDate.lengthOfMonth();
				nextDate = nextDate.withDayOfMonth(maxDateOfMonth);
				dateList.add(nextDate.toString());
				date = nextDate;
			}
			return dateList;
		} else {
			for (int i = 0; i < tenure; i++) {
				dateList.add(date.toString());
				LocalDate nextDate = date.plusYears(1);
				date = nextDate;
			}
			return dateList;
		}
	}

	private List<String> getDueDateListSemiAnnual(LocalDate date, int tenure) {
		List<String> dateList = new ArrayList<String>();

		if (28 <= date.getDayOfMonth()) {

			for (int i = 0; i < tenure; i++) {
				if (i == 0) {
					dateList.add(date.toString());
					continue;
				}
				LocalDate nextDate = date.plusMonths(6);
				int maxDateOfMonth = nextDate.lengthOfMonth();
				nextDate = nextDate.withDayOfMonth(maxDateOfMonth);
				dateList.add(nextDate.toString());
				date = nextDate;
				continue;
			}
			return dateList;
		} else {
			for (int i = 0; i < tenure; i++) {
				dateList.add(date.toString());
				LocalDate nextDate = date.plusMonths(6);
				date = nextDate;
			}
			return dateList;
		}
	}

	private List<String> getDueDateListQuarterly(LocalDate date, int tenure) {
		List<String> dateList = new ArrayList<String>();

		if (28 <= date.getDayOfMonth()) {

			for (int i = 0; i < tenure; i++) {
				if (i == 0) {
					dateList.add(date.toString());
					continue;
				}
				LocalDate nextDate = date.plusMonths(3);
				int maxDateOfMonth = nextDate.lengthOfMonth();
				nextDate = nextDate.withDayOfMonth(maxDateOfMonth);
				dateList.add(nextDate.toString());
				date = nextDate;
			}
			return dateList;
		} else {
			for (int i = 0; i < tenure; i++) {
				dateList.add(date.toString());
				LocalDate nextDate = date.plusMonths(3);
				date = nextDate;
			}
			return dateList;
		}
	}

	private List<String> getDueDateListByMonth(LocalDate date, int tenure) {
		List<String> dateList = new ArrayList<String>();

		if (28 <= date.getDayOfMonth()) {

			for (int i = 0; i < tenure; i++) {
				if (i == 0) {
					dateList.add(date.toString());
					continue;
				}
				LocalDate nextDate = date.plusMonths(1);
				int maxDateOfMonth = nextDate.lengthOfMonth();
				nextDate = nextDate.withDayOfMonth(maxDateOfMonth);
				dateList.add(nextDate.toString());
				date = nextDate;
			}
			return dateList;
		} else {
			for (int i = 0; i < tenure; i++) {
				dateList.add(date.toString());
				LocalDate nextDate = date.plusMonths(1);
				date = nextDate;
			}
			return dateList;
		}

	}

	private List<String> getDueDateListByBiMonthly(LocalDate date, int tenure) {

		List<String> dateList = new ArrayList<String>();
		int unitDate = date.getDayOfMonth();

		if (14 == unitDate || unitDate == 15 || 29 <= unitDate) {
			int unitDateNew = unitDate;
			unitDate = (30 <= unitDate) ? 15 : 14;

			for (int i = 0; i < tenure; i++) {
				if (i == 0) {
					dateList.add(date.toString());
					continue;
				}

				if (28 <= unitDateNew) {
					LocalDate nextDate = date.plusMonths(1);
					nextDate = nextDate.withDayOfMonth(unitDate);
					dateList.add(nextDate.toString());
					date = nextDate;
					unitDateNew = date.getDayOfMonth();
					continue;
				}

				if (unitDateNew == 14 || unitDateNew == 15) {
					int maxDateOfMonth = date.lengthOfMonth();
					LocalDate nextDate = date.withDayOfMonth(maxDateOfMonth);
					dateList.add(nextDate.toString());
					date = nextDate;
					unitDateNew = date.getDayOfMonth();
					continue;
				}
			}
		} else { // 1-13 & 16-18
			for (int i = 0; i < tenure; i++) {
				if (i == 0) {
					dateList.add(date.toString());
					continue;
				}

				if (1 <= unitDate && unitDate <= 13) {
					LocalDate nextDate = date.plusDays(15);
					dateList.add(nextDate.toString());
					date = nextDate;
					unitDate = date.getDayOfMonth();
					continue;
				}

				if (16 <= unitDate && unitDate <= 28) {
					LocalDate nextDate = date.plusMonths(1);
					nextDate = nextDate.minusDays(15);
					dateList.add(nextDate.toString());
					date = nextDate;
					unitDate = date.getDayOfMonth();
					continue;
				}

				dateList.add(date.toString());
				LocalDate nextDate = date.plusDays(15);
				date = nextDate;

			}
		}
		return dateList;
	}

	private List<String> getDueDateListWeekly(LocalDate date, int tenure) {

		List<String> dateList = new ArrayList<String>();
		for (int i = 0; i < tenure; i++) {
			dateList.add(date.toString());
			LocalDate nextDate = date.plusDays(7);
			date = nextDate;
		}
		return dateList;
	}

	private List<String> getDueDateListDaily(LocalDate date, int tenure) {

		List<String> dateList = new ArrayList<String>();
		for (int i = 0; i < tenure; i++) {
			dateList.add(date.toString());
			LocalDate nextDate = date.plusDays(1);
			date = nextDate;
		}
		return dateList;
	}

	public void insertEnachRegistrationLinkDetail(Fields fields) {

		logger.info("inside insertEnachRegistrationLinkDetail to insert registration Link details For eMandate sign ");
		String pgRefNum = TransactionManager.getNewTransactionId();
		try {
			BasicDBObject newFieldsObj = new BasicDBObject();

			newFieldsObj.put(FieldType.RETURN_URL.getName(), fields.get("RETURN_URL"));
			newFieldsObj.put(FieldType.TENURE.getName(), fields.get(FieldType.TENURE.getName()));
			newFieldsObj.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));

			newFieldsObj.put(FieldType.MONTHLY_AMOUNT.getName(),
					String.valueOf(new BigDecimal(fields.get(FieldType.MONTHLY_AMOUNT.getName())).setScale(2,
							BigDecimal.ROUND_HALF_UP)));
			newFieldsObj.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));
			newFieldsObj.put(FieldType.CUST_PHONE.getName(), fields.get("CUST_MOBILE"));
			newFieldsObj.put(FieldType.CUST_EMAIL.getName(), fields.get(FieldType.CUST_EMAIL.getName()));
			newFieldsObj.put(FieldType.FREQUENCY.getName(), fields.get(FieldType.FREQUENCY.getName()));
			newFieldsObj.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
			newFieldsObj.put(FieldType.HASH.getName(), fields.get(FieldType.HASH.getName()));
			newFieldsObj.put(FieldType.AMOUNT.getName(), String.valueOf(
					new BigDecimal(fields.get(FieldType.AMOUNT.getName())).setScale(2, BigDecimal.ROUND_HALF_UP)));
			newFieldsObj.put(FieldType.EMANDATE_URL.getName(), fields.get(FieldType.EMANDATE_URL.getName()));

			newFieldsObj.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
			newFieldsObj.put(FieldType.TXNTYPE.getName(), "Registration");
			newFieldsObj.put("_id", pgRefNum);
			newFieldsObj.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
			newFieldsObj.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PENDING.getCode());
			newFieldsObj.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PENDING.getInternalMessage());

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));

			Document doc = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			// collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error while inserting mandate Link Detail to database";
			logger.error(message, exception);
		}
	}

	@SuppressWarnings("static-access")
	public boolean checkDuplicateOrderIdForRegistration(String orderId, String payId, String subMerchantPayId) {
		try {
			String dbOrderId;

			List<BasicDBObject> queryForCaptured = new ArrayList<BasicDBObject>();
			queryForCaptured.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			queryForCaptured.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				queryForCaptured.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			queryForCaptured.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			BasicDBObject capturedQuery = new BasicDBObject("$and", queryForCaptured);

			List<BasicDBObject> queryForFailed = new ArrayList<BasicDBObject>();
			queryForFailed.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			queryForFailed.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				queryForFailed.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			queryForFailed.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
			BasicDBObject failedQuery = new BasicDBObject("$and", queryForFailed);

			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(capturedQuery);
			queryList.add(failedQuery);

			BasicDBObject query = new BasicDBObject("$or", queryList);

//			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
//			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
//			queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
//			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
//				queryList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
//			}
//			queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "Registration"));
//			queryList.add(new BasicDBObject(FieldType.STATUS.getName(), "Failed"));

//			BasicDBObject query = new BasicDBObject("$and", queryList);
//			BasicDBObject query = new BasicDBObject("$or", queryList);

			logger.info(
					"Inside checkDuplicateOrderIdForRegistration, check duplicate orderId final query For Registration = "
							+ query);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
			MongoCursor<Document> cursor = coll.find(query).sort(new BasicDBObject(FieldType.CREATE_DATE.getName(), -1))
					.iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();

				dbOrderId = doc.getString(FieldType.ORDER_ID.toString());
				if (StringUtils.isNotBlank(dbOrderId) && dbOrderId.equalsIgnoreCase(orderId)) {
					return true;
				}
			}
		} catch (Exception ex) {
			logger.error("exception caught while check duplicate orderId for registration ", ex);
			return false;
		}
		return false;
	}

	public boolean duplicateOrderIdForMandateLink(String orderId, String payId, String subMerchantPayId) {

		boolean flag = false;
		try {
			String dbOrderId;
			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				queryList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}

			BasicDBObject query = new BasicDBObject("$and", queryList);

			logger.info("Inside duplicateOrderIdForMandateLink, check duplicate orderId final query For mandate link = "
					+ query);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
			MongoCursor<Document> cursor = coll.find(query).iterator();

			while (cursor.hasNext()) {
				flag = true;
				break;
			}
		} catch (Exception ex) {
			logger.error("exception caught while check duplicate orderId for registration ", ex);
			return flag;
		}
		return flag;
	}

	public String getEMandateUrlByOrderId(String consumerId, String payId) {

		String eMandateUrl = null;

		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

		queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), consumerId));
		queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "Registration"));

		BasicDBObject query = new BasicDBObject("$and", queryList);
		logger.info("Inside getEMandateUrlByOrderId , query = " + query);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
		BasicDBObject match = new BasicDBObject("$match", query);
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		List<BasicDBObject> pipeline = Arrays.asList(match, sort);

		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
		while (cursor.hasNext()) {
			Document dbObj = cursor.next();
			if (StringUtils.isNotBlank(dbObj.getString("IS_ENCRYPTED"))
					&& dbObj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
				dbObj = dataEncDecTool.decryptDocument(dbObj);
			}
			eMandateUrl = dbObj.getString(FieldType.EMANDATE_URL.getName());
			if (eMandateUrl != null)
				break;
		}

		return eMandateUrl;
	}

	public HashMap<String, String> getEnachTransactionDetailsForStatusEnquiry(String merchantPayId,
			String subMerchantPayId, String orderId, String pgRefNum) {

		HashMap<String, String> tranMap = new HashMap<String, String>();
		try {
			String dueAmount = null;
			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			queryList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum)); 
			queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
//			queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "Sale"));

			if (StringUtils.isNotBlank(subMerchantPayId)) {
				queryList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			BasicDBObject finalQuery = new BasicDBObject("$and", queryList);
			logger.info("inside getEnachTransactionDetailsForStatusEnquiry final query " + finalQuery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
			//BasicDBObject match = new BasicDBObject("$match", finalQuery);
			MongoCursor<Document> cursor = coll.find(finalQuery).sort(new BasicDBObject(FieldType.CREATE_DATE.getName(), -1))
				.iterator();
			//List<BasicDBObject> pipeline = Arrays.asList(match);
			//AggregateIterable<Document> output = coll.aggregate(pipeline);
			//output.allowDiskUse(true);
			//MongoCursor<Document> cursor = output.iterator();
			
			while (cursor.hasNext()) {

				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				Set<String> set = dbobj.keySet();
				set.remove("_id");
				for (String key : set) {
					tranMap.put(key, dbobj.getString(key));
				}

				if (dueAmount == null) {
					dueAmount = String.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
							.subtract(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName()))));
				} else {
					dueAmount = String.valueOf(
							new BigDecimal(dueAmount).add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName()))));
				}

				if (dbobj.getString(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PROCESSING.getName())) {
					tranMap.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
					tranMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PROCESSING.getInternalMessage());

				} else if (dbobj.getString(FieldType.STATUS.getName())
						.equalsIgnoreCase(StatusType.CAPTURED.getName())) {
					tranMap.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
					tranMap.put(FieldType.RESPONSE_MESSAGE.getName(), StatusType.CAPTURED.getName());

				} else if (dbobj.getString(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PENDING.getName())) {
					tranMap.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
					tranMap.put(FieldType.RESPONSE_MESSAGE.getName(),ErrorType.PENDING.getInternalMessage());

				}	else if (dbobj.getString(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.FAILED.getName())) {
					tranMap.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					tranMap.put(FieldType.RESPONSE_MESSAGE.getName(),ErrorType.FAILED.getInternalMessage());

				} else {
					tranMap.put(FieldType.STATUS.getName(), dbobj.getString(FieldType.STATUS.getName()));
					tranMap.put(FieldType.RESPONSE_MESSAGE.getName(),
							dbobj.getString(FieldType.PG_RESPONSE_MSG.getName()));
				}
				tranMap.put("DUE_AMOUNT", dueAmount);
				tranMap.put(FieldType.ORDER_ID.getName(), orderId);
				
				return tranMap;
			}

			if (tranMap.size() == 0) {

				tranMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_TRANSACTION_AVAILABLE.getCode());
				tranMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.NO_TRANSACTION_AVAILABLE.getResponseMessage());
				return tranMap;
			}

		} catch (Exception ex) {
			logger.info("caught exception ", ex);
			HashMap<String, String> errorMap = new HashMap<String, String>();
			errorMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DATABASE_ERROR.getCode());
			errorMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DATABASE_ERROR.getResponseMessage());
			return errorMap;

		}
		return tranMap;
	}

}
