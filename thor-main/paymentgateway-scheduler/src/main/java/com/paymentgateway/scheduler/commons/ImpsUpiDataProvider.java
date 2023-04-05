package com.paymentgateway.scheduler.commons;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.PayoutAcquirer;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.scheduler.core.ServiceControllerProvider;

/**
 * @author Pooja Pancholi
 *
 */

@Service
public class ImpsUpiDataProvider {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ConfigurationProvider configurationProvider;

	@Autowired
	private ServiceControllerProvider serviceControllerProvider;

	private static final Logger logger = LoggerFactory.getLogger(ImpsUpiDataProvider.class);

	@SuppressWarnings("unchecked")
	public void fetchImpsUpiData() {
		logger.info("Inside fetchImpsUpiData() By scheduler");
		try {
			String flagBulk = null;
			Map<String, String> respMap = null;
			String merchantDirectTransactionUrl = configurationProvider.getMerchantInititatedDirectUrl();
			// String upiUrl = configurationProvider.getUpiUrl();

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(configurationProvider.getMONGO_DB_ImpsSettlementCollection());
			BasicDBObject query = new BasicDBObject();

			query.append(FieldType.STATUS.getName(), StatusType.PENDING.getName());
			MongoCursor<Document> cursor = coll.find(query).iterator();

			List<Document> docList = new ArrayList<>();

			while (cursor.hasNext()) {
				docList.add(cursor.next());
			}
			logger.info("Total data size is " + docList.size());

			// Updating status from pending to initiate
			if (!docList.isEmpty()) {

				Bson filter = new Document(query);

				Bson newValue = new Document(FieldType.STATUS.getName(), StatusType.INITIATED.getName())
						.append(FieldType.UPDATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));
				Bson updateOperationDocument = new Document("$set", newValue);
				coll.updateMany(filter, updateOperationDocument);
				logger.info("document status updated for total txn count " + coll.count(query));

				for (Document dbobj : docList) {
					// Document dbobj = cursor.next();
					logger.info("Orginal request for Bulk uploaded by CRM, proccess in scheduler, Txn Id"
							+ dbobj.get(FieldType.TXN_ID.getName()).toString() + " , " + dbobj);
					dbobj.append("flagBulk", "true");
					if (isDailyLimitExceed(dbobj)) {
						Date dNow = new Date();
						String dateNow = DateCreater.formatDateForDb(dNow);

						if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
							Bson filterTxn = new Document(FieldType.SUB_MERCHANT_ID.getName(),
									dbobj.getString(FieldType.SUB_MERCHANT_ID.getName())).append(
											FieldType.ORDER_ID.getName(),
											dbobj.getString(FieldType.ORDER_ID.getName()));
							Bson newValueForTxn = new Document(FieldType.STATUS.getName(),
									StatusType.DECLINED.getName())
											.append(FieldType.UPDATE_DATE.getName(), dateNow)
											.append(FieldType.PG_TXN_MESSAGE.getName(),
													ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseMessage())
											.append(FieldType.RESPONSE_MESSAGE.getName(),
													ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseMessage())
											.append(FieldType.RESPONSE_CODE.getName(),
													ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getCode())
											.append(FieldType.IS_STATUS_FINAL.getName(), "Y");
							Bson updateOperationDocument1 = new Document("$set", filterTxn);
							coll.updateOne(filterTxn, updateOperationDocument1);
							logger.info("Updated request for Bulk uploaded by CRM, proccess in scheduler, Txn Id"
									+ dbobj.get(FieldType.TXN_ID.getName()).toString() + " , " + newValueForTxn);
						} else {
							Bson filterTxn = new Document(FieldType.PAY_ID.getName(),
									dbobj.getString(FieldType.PAY_ID.getName())).append(FieldType.ORDER_ID.getName(),
											dbobj.getString(FieldType.ORDER_ID.getName()));
							Bson newValueForTxn = new Document(FieldType.STATUS.getName(),
									StatusType.DECLINED.getName())
											.append(FieldType.UPDATE_DATE.getName(), dateNow)
											.append(FieldType.PG_TXN_MESSAGE.getName(),
													ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseMessage())
											.append(FieldType.RESPONSE_MESSAGE.getName(),
													ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseMessage())
											.append(FieldType.RESPONSE_CODE.getName(),
													ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getCode())
											.append(FieldType.IS_STATUS_FINAL.getName(), "Y");
							Bson updateOperationDocument1 = new Document("$set", newValueForTxn);
							coll.updateOne(filterTxn, updateOperationDocument1);
							logger.info("Updated request for Bulk uploaded by CRM, proccess in scheduler, Txn Id"
									+ dbobj.get(FieldType.TXN_ID.getName()).toString() + " , " + newValueForTxn);
						}
					} else {

						JSONObject data = new JSONObject();

						data.put(FieldType.ORDER_ID.getName(), dbobj.getString(FieldType.ORDER_ID.getName()));

						if (dbobj.getString(FieldType.TXNTYPE.getName()).equalsIgnoreCase("IMPS")
								|| dbobj.getString(FieldType.TXNTYPE.getName()).equalsIgnoreCase("NEFT")
								|| dbobj.getString(FieldType.TXNTYPE.getName()).equalsIgnoreCase("RTGS")) {
							data.put(FieldType.BENE_NAME.getName(), dbobj.getString(FieldType.BENE_NAME.getName()));
							data.put(FieldType.BENE_ACCOUNT_NO.getName(),
									dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()));
							// data.put(FieldType.BANK_NAME.getName(),
							// dbobj.getString(FieldType.BANK_NAME.getName()));
							data.put(FieldType.IFSC_CODE.getName(), dbobj.getString(FieldType.IFSC_CODE.getName()));
						} else if (dbobj.getString(FieldType.TXNTYPE.getName()).equalsIgnoreCase("UPI")) {
							data.put(FieldType.PAYER_ADDRESS.getName(),
									dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
							data.put(FieldType.PAYER_NAME.getName(), dbobj.getString(FieldType.PAYER_NAME.getName()));
						}

						BigDecimal amt = new BigDecimal((String) dbobj.getString(FieldType.AMOUNT.getName()))
								.setScale(2);
						data.put(FieldType.PHONE_NO.getName(), dbobj.getString(FieldType.PHONE_NO.getName()));
						data.put(FieldType.AMOUNT.getName(), Amount.formatAmount(amt.toString(), "356"));
						data.put(FieldType.REMARKS.getName(), dbobj.getString(FieldType.REMARKS.getName()));
						data.put(FieldType.PURPOSE.getName(), dbobj.getString(FieldType.PURPOSE.getName()));
						data.put(FieldType.TXN_ID.getName(), dbobj.getString(FieldType.TXN_ID.getName()));
						data.put(FieldType.ACQ_ID.getName(), dbobj.getString(FieldType.ACQ_ID.getName()));
						data.put(FieldType.VIRTUAL_AC_CODE.getName(),dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()));
						data.put(FieldType.ACQUIRER_NAME.getName(),dbobj.getString(FieldType.ACQUIRER_NAME.getName()));

						if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
							data.put(FieldType.PAY_ID.getName(), dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
						} else {
							if (dbobj.containsKey(FieldType.PAY_ID.getName())) {
								data.put(FieldType.PAY_ID.getName(), dbobj.getString(FieldType.PAY_ID.getName()));
							}
						}
						data.put(FieldType.CURRENCY_CODE.getName(), "356");
						data.put("flagBulk", "true");
						Map<String, String> requestMap = new ObjectMapper().readValue(data.toString(), HashMap.class);
						data.put(FieldType.HASH.getName(), getHash(requestMap));
						logger.info("Request send to ws for transaction Id : "
								+ dbobj.get(FieldType.TXN_ID.getName()).toString());

						respMap = serviceControllerProvider.impsTransferTransact(data, merchantDirectTransactionUrl);
					}
					logger.info("respMap : " + respMap);
				}
			}

		} catch (Exception e) {
			logger.error("exception ", e);

		}
	}

	public boolean isDailyLimitExceed(Document dbobj) {
		logger.info("Inside  isDailyLimitExceed()");

		String fieldAmount = (String) dbobj.get(FieldType.AMOUNT.getName());

		BigDecimal closingTransactionDiff = getClosingTransactionAmount(dbobj);
		if (closingTransactionDiff.compareTo(BigDecimal.ZERO) <= 0) {
			return true;
		}

		BigDecimal checkDiff = new BigDecimal(0);
		checkDiff = checkDiff.add(closingTransactionDiff).setScale(2);
		checkDiff = checkDiff.subtract(new BigDecimal(fieldAmount).setScale(2));

		logger.info("Calculated settled amount is " + checkDiff);

		if (checkDiff.compareTo(BigDecimal.ZERO) >= 0) {
			return false;
		}

		return true;

	}

	public BigDecimal getClosingTransactionAmount(Document dbobj2) {
		logger.info("Inside getImpsTransactionAmount()");
		BigDecimal totalAmount = new BigDecimal("0").setScale(2);
		try {

			BigDecimal closingAmount = new BigDecimal("0").setScale(2);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(configurationProvider.getMONGO_DB_closingAmountCollection());
			BasicDBObject query = new BasicDBObject();

			String virtualAccountCode = (String) dbobj2.get(FieldType.VIRTUAL_AC_CODE.getName());

			if (StringUtils.isNotBlank(virtualAccountCode)) {
				query.append(FieldType.VIRTUAL_AC_CODE.getName(), virtualAccountCode);
			}

			BasicDBObject match2 = new BasicDBObject("$match", query);
			BasicDBObject sort2 = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline2 = Arrays.asList(match2, sort2);
			AggregateIterable<Document> output2 = coll.aggregate(pipeline2);
			output2.allowDiskUse(true);
			MongoCursor<Document> cursor = output2.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				closingAmount = (dbAmount);
				break;
			}
			cursor.close();

			totalAmount = closingAmount;
			logger.info("Total today's Transactions Amount  " + totalAmount);
		} catch (Exception e) {
			logger.error("Exception caugth fetching imps transaction, ", e);
		}
		return totalAmount;

	}

	public BigDecimal getECollectionTransactionAmount(Document dbobj2) {
		logger.info("Inside getImpsTransactionAmount()");
		BigDecimal totalAmount = new BigDecimal("0").setScale(2);
		try {

			BigDecimal closingAmount = new BigDecimal("0").setScale(2);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String todayDate = sdf.format(new Date());

			String dateFrom = todayDate + " " + "00:00:00";
			String dateTo = todayDate + " " + "23:59:59";

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(configurationProvider.getMONGO_DB_E_Collection());
			BasicDBObject query = new BasicDBObject();
			BasicDBObject queryClosing = new BasicDBObject();

			query.append(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());

			BigDecimal settledAmount = new BigDecimal("0").setScale(2);

			if (dbobj2.containsKey(FieldType.PAY_ID.getName())) {
				query.append(FieldType.PAY_ID.getName(), (String) dbobj2.get(FieldType.PAY_ID.getName()));
			}

			if (dbobj2.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(),
						(String) dbobj2.get(FieldType.SUB_MERCHANT_ID.getName()));
			}

			query.append(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			MongoCursor<Document> cursor = coll.find(query).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				settledAmount = settledAmount.add(dbAmount);
			}
			cursor.close();
			logger.info("settledAmount Amount  " + settledAmount);
			MongoDatabase dbIns1 = mongoInstance.getDB();

			MongoCollection<Document> coll1 = dbIns1
					.getCollection(configurationProvider.getMONGO_DB_closingAmountCollection());

			queryClosing.append(FieldType.VIRTUAL_AC_CODE.getName(),
					dbobj2.getString(FieldType.VIRTUAL_AC_CODE.getName()));

			BasicDBObject match = new BasicDBObject("$match", queryClosing);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CLOSING_DATE.name(), -1));
			BasicDBObject limit = new BasicDBObject("$limit", 1);
			List<BasicDBObject> pipeline2 = Arrays.asList(match, sort, limit);
			AggregateIterable<Document> output2 = coll1.aggregate(pipeline2);
			output2.allowDiskUse(true);
			MongoCursor<Document> cursor2 = output2.iterator();

			while (cursor2.hasNext()) {
				Document dbobj = cursor2.next();
				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				closingAmount = closingAmount.add(dbAmount);
			}

			cursor2.close();

			totalAmount = closingAmount.add(settledAmount);
			logger.info("Total today's Transactions Amount  " + totalAmount);
		} catch (Exception e) {
			logger.error("Exception caugth fetching imps transaction, ", e);
		}
		return totalAmount;

	}

	public BigDecimal getImpsTransactionAmount(Document dbobj2) {
		logger.info("Inside getImpsTransactionAmount()");
		BigDecimal totalAmount = new BigDecimal("0").setScale(2);
		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String todayDate = sdf.format(new Date());

			String dateFrom = todayDate + " " + "00:00:00";
			String dateTo = todayDate + " " + "23:59:59";

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns
					.getCollection(configurationProvider.getMONGO_DB_ImpsSettlementCollection());

			BasicDBObject query = new BasicDBObject();

			query.append(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());

			BigDecimal settledAmount = new BigDecimal("0").setScale(2);

			if (dbobj2.containsKey(FieldType.PAY_ID.getName())) {
				query.append(FieldType.PAY_ID.getName(), (String) dbobj2.get(FieldType.PAY_ID.getName()));
			}

			if (dbobj2.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(),
						(String) dbobj2.get(FieldType.SUB_MERCHANT_ID.getName()));
			}

			query.append(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			query.append(FieldType.USER_TYPE.getName(), "Merchant Initiated Direct");
			MongoCursor<Document> cursor = coll.find(query).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				settledAmount = settledAmount.add(dbAmount);
			}
			totalAmount = settledAmount;
			logger.info("Total today's Transactions Amount  " + totalAmount);
		} catch (Exception e) {
			logger.error("Exception caugth fetching imps transaction, ", e);
		}
		return totalAmount;

	}

	public String getHash(Map<String, String> data) throws SystemException {

		// Append salt of merchant
		String salt = null;
		salt = PropertiesManager.saltStore.get(data.get(FieldType.PAY_ID.getName()));
		if (StringUtils.isBlank(salt)) {
			salt = (new PropertiesManager()).getSalt(data.get(FieldType.PAY_ID.getName()));
			if (salt != null) {
				logger.info("Salt found from propertiesManager for payId = " + data.get(FieldType.PAY_ID.getName()));
			}

		} else {
			logger.info("Salt found from static map in propertiesManager for payId = "
					+ data.get(FieldType.PAY_ID.getName()));
		}

		if (null == salt) {
			logger.info("Inside Hasher , salt = null , fields = " + ((Fields) data).getFieldsAsBlobString());
			data.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getCode());
			throw new SystemException(ErrorType.INVALID_PAYID_ATTEMPT, "Invalid " + FieldType.PAY_ID.getName());
		}
		// Sort the request map5

		Map<String, String> treeMap = new TreeMap<String, String>(data);

		// Calculate the hash string
		StringBuilder allFields = new StringBuilder();
		for (String key : treeMap.keySet()) {
			allFields.append(ConfigurationConstants.FIELD_SEPARATOR.getValue());
			allFields.append(key);
			allFields.append(ConfigurationConstants.FIELD_EQUATOR.getValue());
			allFields.append(data.get(key));
		}

		allFields.deleteCharAt(0); // Remove first FIELD_SEPARATOR
		allFields.append(salt);

		allFields.toString();
		// Calculate hash at server side
		return Hasher.getHash(allFields.toString());
	}

	public void fetchPayoutData() {
		logger.info("Inside fetchPayoutData() By scheduler");
		try {
			Map<String, String> respMap = null;

			String statusEnquiryUrl = configurationProvider.getMerchantInititatedDirectStatusEnqUrl();
			// String upiStatusEnquiryUrl =
			// configurationProvider.getUpiStatusEnquiryUrl();
			String minutesBefore = configurationProvider.getPayoutMinutesBefore();
			String minutesInterval = configurationProvider.getPayoutMinutesInterval();

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String timeNow = sdf.format(new Date()).toString();

			LocalDateTime datetime = LocalDateTime.parse(timeNow, formatter);
			LocalDateTime datetime2 = LocalDateTime.parse(timeNow, formatter);

			datetime = datetime.minusMinutes(Integer.valueOf(minutesBefore));
			String endTime = datetime.format(formatter);

			datetime2 = datetime2.minusMinutes(Integer.valueOf(minutesInterval) + Integer.valueOf(minutesBefore));
			String startTime = datetime2.format(formatter);

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(startTime);
			DateFormat dateFormatIndex = new SimpleDateFormat("yyyyMMdd");
			String todaysDateIndex = dateFormatIndex.format(dateStart.getTime());

			logger.info("Scheduler status enquiry Start Time = " + startTime);
			logger.info("Scheduler status enquiry End  Time = " + endTime);

			BasicDBObject dateTimeQuery = new BasicDBObject();
			BasicDBObject txnTypQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject flagQuery = new BasicDBObject();
			BasicDBObject acqQuery = new BasicDBObject();

			dateTimeQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startTime).toLocalizedPattern())
							.add("$lt", new SimpleDateFormat(endTime).toLocalizedPattern()).get());

			txnTypQuery.put(FieldType.DATE_INDEX.getName(), todaysDateIndex);
			txnTypQuery.put(FieldType.USER_TYPE.getName(), "Merchant Initiated Direct");
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			List<String> statusList = new ArrayList<String>();
			statusList.add("Captured");
			statusList.add("Pending");
			statusList.add("Initiated");
			statusQuery.append("$nin", statusList);
			txnTypQuery.put(FieldType.STATUS.getName(), statusQuery);
			flagQuery.append("$ne", "Y");
			txnTypQuery.put(FieldType.IS_STATUS_FINAL.getName(), flagQuery);
			acqQuery.put(FieldType.ACQUIRER_NAME.getName(), new BasicDBObject("$ne",PayoutAcquirer.CASHFREE.name()));
			if (!(dateTimeQuery.isEmpty())) {
				allConditionQueryList.add(dateTimeQuery);
			}
			if (!(txnTypQuery.isEmpty())) {
				allConditionQueryList.add(txnTypQuery);
			}
			
			allConditionQueryList.add(acqQuery);

			BasicDBObject finalquery = new BasicDBObject("$and", allConditionQueryList);

			logger.info("Query to get data for status enquiry = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(configurationProvider.getMONGO_DB_ImpsSettlementCollection());
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			AggregateIterable<Document> output = collection.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				logger.info("Orginal request for status enquiry by scheduler , pay Id"
						+ dbobj.get(FieldType.PAY_ID.getName()).toString() + " and Txn Id"
						+ dbobj.getString(FieldType.TXN_ID.getName()) + " , " + dbobj);
				JSONObject data = new JSONObject();
				data.put(FieldType.ORDER_ID.getName(), dbobj.getString(FieldType.ORDER_ID.getName()));
				data.put(FieldType.TXN_ID.getName(), dbobj.getString(FieldType.TXN_ID.getName()));
				data.put(FieldType.TXNTYPE.getName(), dbobj.getString(FieldType.TXNTYPE.getName()));
				data.put(FieldType.CREATE_DATE.getName(), dbobj.getString(FieldType.CREATE_DATE.getName()));
				data.put(FieldType.USER_TYPE.getName(), dbobj.getString(FieldType.USER_TYPE.getName()));
				data.put(FieldType.ACQUIRER_NAME.getName(), dbobj.getString(FieldType.ACQUIRER_NAME.getName()));
				data.put(FieldType.VIRTUAL_AC_CODE.getName(), dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()));
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
					data.put(FieldType.PAY_ID.getName(), dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
				} else {
					if (dbobj.containsKey(FieldType.PAY_ID.getName())) {
						data.put(FieldType.PAY_ID.getName(), dbobj.getString(FieldType.PAY_ID.getName()));
					}
				}
				data.put(FieldType.AMOUNT.getName(),
						Amount.formatAmount(dbobj.getString(FieldType.AMOUNT.getName()), "356"));
				data.put(FieldType.CURRENCY_CODE.getName(), "356");
				Map<String, String> requestMap = new ObjectMapper().readValue(data.toString(), HashMap.class);
				data.put(FieldType.HASH.getName(), getHash(requestMap));

				// Common URL for all
				respMap = serviceControllerProvider.impsTransferTransact(data, statusEnquiryUrl);

				logger.info("Final response for status enquiry by scheduler : " + respMap);
			}

		} catch (Exception e) {
			logger.error("Exception caugth in payout Status enquiry : ", e);
		}
	}
}
