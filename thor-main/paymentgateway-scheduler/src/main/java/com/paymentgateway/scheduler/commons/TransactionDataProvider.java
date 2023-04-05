package com.paymentgateway.scheduler.commons;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.SystemProperties;

/**
 * @author Shaiwal
 *
 */

@Service
public class TransactionDataProvider {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ConfigurationProvider configurationProvider;

	private SystemProperties systemProperties = new SystemProperties();

	private static final Logger logger = LoggerFactory.getLogger(TransactionDataProvider.class);

	public Set<String> fetchTransactionData() {

		Set<String> pgRefSet = new HashSet<String>();
		try {

//			using minutes as the new parameter
			// String hoursBefore = configurationProvider.getHoursBefore();
			// String hoursInterval = configurationProvider.getHoursInterval();
			String minutesBefore = configurationProvider.getMinutesBefore();
			String minutesInterval = configurationProvider.getMinutesInterval();

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String timeNow = sdf.format(new Date()).toString();

			LocalDateTime datetime = LocalDateTime.parse(timeNow, formatter);
			LocalDateTime datetime2 = LocalDateTime.parse(timeNow, formatter);

			datetime = datetime.minusMinutes(Integer.valueOf(minutesBefore));
			String endTime = datetime.format(formatter);

			datetime2 = datetime2.minusMinutes(Integer.valueOf(minutesInterval) + Integer.valueOf(minutesBefore));
			String startTime = datetime2.format(formatter);

			logger.info("Scheduler status enquiry Start Time = " + startTime);
			logger.info("Scheduler status enquiry End  Time = " + endTime);
			Set<String> allPgRefSet = new HashSet<String>();

			BasicDBObject dateTimeQuery = new BasicDBObject();
			BasicDBObject txnTypQuery = new BasicDBObject();

			dateTimeQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startTime).toLocalizedPattern())
							.add("$lt", new SimpleDateFormat(endTime).toLocalizedPattern()).get());

			txnTypQuery.put(FieldType.TXNTYPE.getName(), "SALE");
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			String merchantPayId = configurationProvider.getMerchantPayId();
			String acquirer = configurationProvider.getAcquirerName();

			BasicDBObject merchantQuery = new BasicDBObject();
			BasicDBObject acquirerQuery = new BasicDBObject();

			List<BasicDBObject> merchantQueryList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> acquirerQueryList = new ArrayList<BasicDBObject>();

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {

				String merchantPayIdArr[] = merchantPayId.split(",");

				for (String merchant : merchantPayIdArr) {
					merchantQueryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchant));
				}
			}

			if (StringUtils.isNotBlank(acquirer) && !acquirer.equalsIgnoreCase("ALL")) {

				String acquirerArr[] = acquirer.split(",");

				for (String acq : acquirerArr) {
					acquirerQueryList.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acq));
				}

			}

			merchantQuery.put("$or", merchantQueryList);
			acquirerQuery.put("$or", acquirerQueryList);

			allConditionQueryList.add(dateTimeQuery);
			allConditionQueryList.add(txnTypQuery);

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				allConditionQueryList.add(merchantQuery);
			}

			if (StringUtils.isNotBlank(acquirer) && !acquirer.equalsIgnoreCase("ALL")) {
				allConditionQueryList.add(acquirerQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", allConditionQueryList);

			logger.info("Query to get data for status enquiry = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(configurationProvider.getMONGO_DB_collectionName());

			BasicDBObject match = new BasicDBObject("$match", finalquery);

			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = collection.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (null != dbobj.get(FieldType.PG_REF_NUM.getName())
						&& !dbobj.getString(FieldType.PG_REF_NUM.getName()).equalsIgnoreCase("0")) {
					allPgRefSet.add(dbobj.getString(FieldType.PG_REF_NUM.getName()));
				}

			}

			logger.info("Set of All allPgRefSet prepared with total number of OID : " + allPgRefSet.size());
			cursor.close();

			for (String pgRefNum : allPgRefSet) {

				boolean isCaptured = false;
				boolean isenrolled = false;
				String pgRef = null;

				BasicDBObject pgRefNumQuery = new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum);
				BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), "SALE");

				List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
				conditionList.add(pgRefNumQuery);
				conditionList.add(txnTypeQuery);

				BasicDBObject query = new BasicDBObject("$and", conditionList);

				MongoDatabase dbIns1 = mongoInstance.getDB();
				MongoCollection<Document> collection1 = dbIns1
						.getCollection(configurationProvider.getMONGO_DB_collectionName());

				BasicDBObject match1 = new BasicDBObject("$match", query);

				List<BasicDBObject> pipeline1 = Arrays.asList(match1);
				AggregateIterable<Document> output1 = collection1.aggregate(pipeline1);
				output1.allowDiskUse(true);

				MongoCursor<Document> cursor1 = output1.iterator();
				while (cursor1.hasNext()) {
					Document dbobj = cursor1.next();

					if (dbobj.get(FieldType.STATUS.getName()) != null
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.getName()))) {

						if (dbobj.getString(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())
								|| dbobj.getString(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.SETTLED.getName())) {
							isCaptured = true;
						}

						if (dbobj.getString(FieldType.STATUS.getName())
								.equalsIgnoreCase(StatusType.SENT_TO_BANK.getName())
								|| dbobj.getString(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.ENROLLED.getName())) {

							pgRef = dbobj.getString(FieldType.PG_REF_NUM.getName());
						}

					}

					if (dbobj.get(FieldType.ACQUIRER_TYPE.getName()) != null
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()))) {
						isenrolled = true;
					}

				}
				cursor1.close();

				if (!isCaptured && isenrolled) {

					if (StringUtils.isNotBlank(pgRef)) {
						pgRefSet.add(pgRef);
					}
				}
			}

			return pgRefSet;
		}

		catch (Exception e) {
			logger.error("Exception in getting data for status enquiry", e);
		}
		return pgRefSet;

	}

	public Set<String> fetchTransactionCapturedData() {

		Set<String> orderIdSet = new HashSet<String>();
		try {

//			using minutes as the new parameter
			// String hoursBefore = configurationProvider.getHoursBefore();
			// String hoursInterval = configurationProvider.getHoursInterval();
			String minutesBefore = configurationProvider.getOrderMinutesBefore();
			String minutesInterval = configurationProvider.getOrderMinutesInterval();

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String timeNow = sdf.format(new Date()).toString();

			LocalDateTime datetime = LocalDateTime.parse(timeNow, formatter);
			LocalDateTime datetime2 = LocalDateTime.parse(timeNow, formatter);

			datetime = datetime.minusMinutes(Integer.valueOf(minutesBefore));
			String endTime = datetime.format(formatter);

			datetime2 = datetime2.minusMinutes(Integer.valueOf(minutesInterval) + Integer.valueOf(minutesBefore));
			String startTime = datetime2.format(formatter);

			logger.info("Scheduler status enquiry Start Time = " + startTime);
			logger.info("Scheduler status enquiry End  Time = " + endTime);

			BasicDBObject dateTimeQuery = new BasicDBObject();
			BasicDBObject txnTypQuery = new BasicDBObject();
			BasicDBObject statusTypQuery = new BasicDBObject();
			BasicDBObject merchantQuery = new BasicDBObject();

			dateTimeQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startTime).toLocalizedPattern())
							.add("$lt", new SimpleDateFormat(endTime).toLocalizedPattern()).get());

			txnTypQuery.put(FieldType.TXNTYPE.getName(), "SALE");
			statusTypQuery.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			merchantQuery.put(FieldType.PAY_ID.getName(), configurationProvider.getOrderMerchantPayId());
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			allConditionQueryList.add(dateTimeQuery);
			allConditionQueryList.add(txnTypQuery);
			allConditionQueryList.add(statusTypQuery);
			allConditionQueryList.add(merchantQuery);

			BasicDBObject finalquery = new BasicDBObject("$and", allConditionQueryList);

			logger.info("Query to get data for order status enquiry = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(configurationProvider.getMONGO_DB_collectionName());

			BasicDBObject match = new BasicDBObject("$match", finalquery);

			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = collection.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				orderIdSet.add(dbobj.getString(FieldType.ORDER_ID.getName()));
			}

			logger.info("Set of All orderIdSet prepared with total number of Order Id : " + orderIdSet.size());
			cursor.close();

			return orderIdSet;
		}

		catch (Exception e) {
			logger.error("Exception in getting data for order confirm enquiry", e);
		}
		return orderIdSet;

	}

	public List<AutoRefundTransactions> fetchPostCapturedTxnData() {
		List<AutoRefundTransactions> postSettledData = new ArrayList<AutoRefundTransactions>();
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
			DateFormat df1 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
			String date = df.format(new Date());
			BasicDBObject dateQuery = new BasicDBObject();

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", date + " 00:00:00").add("$lt", date + " 23:59:59").get());

			BasicDBObject dateIndex = new BasicDBObject(FieldType.DATE_INDEX.getName(), df1.format(new Date()));

			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), "Captured");
			BasicDBObject txntypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), "SALE");
			BasicDBObject txnCapFlagQuery = new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), "Post Captured");
			BasicDBObject autoRefundFlagQuery = new BasicDBObject(FieldType.AUTO_REFUND_FLAG.getName(), "Y");
			if (!dateQuery.isEmpty()) {
				paramConditionLst.add(dateQuery);
			}
			paramConditionLst.add(dateIndex);
			paramConditionLst.add(statusQuery);
			paramConditionLst.add(txntypeQuery);
			paramConditionLst.add(txnCapFlagQuery);
			paramConditionLst.add(autoRefundFlagQuery);
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final query for Fetching Post Captured Txns " + finalquery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(configurationProvider.getMONGO_DB_collectionName());
			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.PAY_ID.getName(), 1);
			projectElement.put(FieldType.AMOUNT.getName(), 1);
			projectElement.put(FieldType.PG_REF_NUM.getName(), 1);
			projectElement.put(FieldType.CURRENCY_CODE.getName(), 1);
			projectElement.put(FieldType.ORDER_ID.getName(), 1);
			projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);

			BasicDBObject project = new BasicDBObject("$project", projectElement);

			BasicDBObject match = new BasicDBObject("$match", finalquery);

			List<BasicDBObject> pipeline = Arrays.asList(match, project);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				AutoRefundTransactions obj = new AutoRefundTransactions();

				obj.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
				obj.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
				obj.setCurrencyCode(dbobj.getString(FieldType.CURRENCY_CODE.getName()));
				obj.setOrderId(dbobj.getString(FieldType.ORDER_ID.getName()));
				obj.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.getName()));
				postSettledData.add(obj);

			}
			cursor.close();
			return postSettledData;
		} catch (Exception e) {
			logger.error("Exception in Fetching post captured txns == ", e);
		}

		return postSettledData;
	}

	public Set<String> fetchTransactionCapturedPgRefData() {

		Set<String> pgRefSet = new HashSet<String>();
		try {

//			using minutes as the new parameter
			// String hoursBefore = configurationProvider.getHoursBefore();
			// String hoursInterval = configurationProvider.getHoursInterval();
			String minutesBefore = configurationProvider.getOrderMinutesBefore();
			String minutesInterval = configurationProvider.getOrderMinutesInterval();

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String timeNow = sdf.format(new Date()).toString();

			LocalDateTime datetime = LocalDateTime.parse(timeNow, formatter);
			LocalDateTime datetime2 = LocalDateTime.parse(timeNow, formatter);

			datetime = datetime.minusMinutes(Integer.valueOf(minutesBefore));
			String endTime = datetime.format(formatter);

			datetime2 = datetime2.minusMinutes(Integer.valueOf(minutesInterval) + Integer.valueOf(minutesBefore));
			String startTime = datetime2.format(formatter);

			logger.info("Scheduler status enquiry Start Time = " + startTime);
			logger.info("Scheduler status enquiry End  Time = " + endTime);

			BasicDBObject dateTimeQuery = new BasicDBObject();
			BasicDBObject txnTypQuery = new BasicDBObject();
			BasicDBObject statusTypQuery = new BasicDBObject();
			BasicDBObject merchantQuery = new BasicDBObject();

			dateTimeQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startTime).toLocalizedPattern())
							.add("$lt", new SimpleDateFormat(endTime).toLocalizedPattern()).get());

			txnTypQuery.put(FieldType.TXNTYPE.getName(), "SALE");
			statusTypQuery.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			merchantQuery.put(FieldType.PAY_ID.getName(), configurationProvider.getOrderMerchantPayId());
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			allConditionQueryList.add(dateTimeQuery);
			allConditionQueryList.add(txnTypQuery);
			allConditionQueryList.add(statusTypQuery);
			allConditionQueryList.add(merchantQuery);

			BasicDBObject finalquery = new BasicDBObject("$and", allConditionQueryList);

			logger.info("Query to get data for order status enquiry = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(configurationProvider.getMONGO_DB_collectionName());

			BasicDBObject match = new BasicDBObject("$match", finalquery);

			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = collection.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				pgRefSet.add(dbobj.getString(FieldType.PG_REF_NUM.getName()));
			}

			logger.info("Set of All orderIdSet prepared with total number of Order Id : " + pgRefSet.size());
			cursor.close();

			return pgRefSet;
		}

		catch (Exception e) {
			logger.error("Exception in getting data for order confirm enquiry", e);
		}
		return pgRefSet;

	}

	public Set<String> fetchMsedclCallbackData() {

		Set<String> pgRefSet = new HashSet<String>();
		try {
			BasicDBObject msedclPayId = new BasicDBObject();

			msedclPayId.put(FieldType.PAY_ID.getName(), PropertiesManager.propertiesMap.get("MSEDCL_PAY_ID"));

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(configurationProvider.getMONGO_DB_callbackRetryCollection());

			MongoCursor<Document> cursor = collection.find(msedclPayId).iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				pgRefSet.add(dbobj.getString(FieldType.PG_REF_NUM.getName()));
			}

			logger.info("Set of All pgrefnum : " + pgRefSet.size());
			cursor.close();

			return pgRefSet;
		}

		catch (Exception e) {
			logger.error("Exception in getting data for order confirm enquiry", e);
		}
		return pgRefSet;

	}

	public Set<String> fetchPendingTransactionData() {

		Set<String> orderIdSet = new HashSet<String>();
		try {

			String minutesBefore = configurationProvider.getPendingMinutesBefore();
			String minutesInterval = configurationProvider.getPendingMinutesInterval();

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String timeNow = sdf.format(new Date()).toString();

			LocalDateTime datetime = LocalDateTime.parse(timeNow, formatter);
			LocalDateTime datetime2 = LocalDateTime.parse(timeNow, formatter);

			datetime = datetime.minusMinutes(Integer.valueOf(minutesBefore));
			String endTime = datetime.format(formatter);

			datetime2 = datetime2.minusMinutes(Integer.valueOf(minutesInterval));
			String startTime = datetime2.format(formatter);

			logger.info("Scheduler fetch Pending Transaction Start Time = " + startTime);
			logger.info("Scheduler fetch Pending Transaction  End Time = " + endTime);

			BasicDBObject dateTimeQuery = new BasicDBObject();
			BasicDBObject txnTypQuery = new BasicDBObject();
			BasicDBObject statusTypQuery = new BasicDBObject();
			List<String> statusConditionList = new ArrayList<String>();

			dateTimeQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startTime).toLocalizedPattern())
							.add("$lt", new SimpleDateFormat(endTime).toLocalizedPattern()).get());

			txnTypQuery.put(FieldType.ORIG_TXNTYPE.getName(), "SALE");

			statusConditionList.add(StatusType.PENDING.getName());

			statusTypQuery.append("$in", statusConditionList);
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			allConditionQueryList.add(dateTimeQuery);
			allConditionQueryList.add(txnTypQuery);
			allConditionQueryList.add(new BasicDBObject(FieldType.STATUS.getName(), statusTypQuery));

			BasicDBObject finalquery = new BasicDBObject("$and", allConditionQueryList);

			logger.info("Query to get data for Pending Status Order Id = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(configurationProvider.getMONGO_DB_transactionStatus());

			BasicDBObject match = new BasicDBObject("$match", finalquery);

			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = collection.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				orderIdSet.add(dbobj.getString(FieldType.ORDER_ID.getName()));
			}

			logger.info("Set of All orderIdSet prepared with total number of Order Id : " + orderIdSet.size());
			cursor.close();

			return orderIdSet;
		}

		catch (Exception e) {
			logger.error("Exception in getting data for Pending transaction", e);
		}
		return orderIdSet;

	}

	@SuppressWarnings("static-access")
	public Fields fetchPendingTransactionDataByOrderId(String orderId) throws SystemException {

		Fields preFields = new Fields();

		try {
			logger.info("Inside getPreviousForOrderId: " + orderId);
			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));

			BasicDBObject saleQuery = new BasicDBObject("$and", condList);
			logger.info("Query to get data for Order Id = " + orderId + " Query >>> " + saleQuery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(configurationProvider.getMONGO_DB_collectionName());

			BasicDBObject match = new BasicDBObject("$match", saleQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			AggregateIterable<Document> output = collection.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (null != dbobj) {
					for (int j = 0; j < dbobj.size(); j++) {
						for (String columnName : systemProperties.getDBFields()) {
							if (dbobj.get(columnName) != null) {
								preFields.put(columnName, dbobj.get(columnName).toString());
							}
						}
					}
					if ((dbobj.get(FieldType.STATUS.getName()).toString()
							.equalsIgnoreCase(StatusType.CAPTURED.getName())
							|| dbobj.get(FieldType.STATUS.getName()).toString()
									.equalsIgnoreCase(StatusType.FAILED.getName())
							|| dbobj.get(FieldType.STATUS.getName()).toString()
									.equalsIgnoreCase(StatusType.REJECTED.getName())
							|| dbobj.get(FieldType.STATUS.getName()).toString()
									.equalsIgnoreCase(StatusType.CANCELLED.getName())
							|| dbobj.get(FieldType.STATUS.getName()).toString()
									.equalsIgnoreCase(StatusType.DECLINED.getName())
							|| dbobj.get(FieldType.STATUS.getName()).toString()
									.equalsIgnoreCase(StatusType.TIMEOUT.getName()))) {
						break;
					}
				}
			}

			// Correct Amount to decimal format

			if (StringUtils.isNotBlank(preFields.get(FieldType.AMOUNT.getName()))) {
				preFields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(preFields.get(FieldType.AMOUNT.getName()),
						preFields.get(FieldType.CURRENCY_CODE.getName())));
			}

			if (StringUtils.isNotBlank(preFields.get(FieldType.TOTAL_AMOUNT.getName()))) {
				preFields.put(FieldType.TOTAL_AMOUNT.getName(),
						Amount.formatAmount(preFields.get(FieldType.TOTAL_AMOUNT.getName()),
								preFields.get(FieldType.CURRENCY_CODE.getName())));
			}

			return preFields;
		} catch (Exception exception) {
			String message = "Error while previous based on Pg Ref Num from database";
			logger.error(message, exception);
			return preFields;
		}

	}
}
