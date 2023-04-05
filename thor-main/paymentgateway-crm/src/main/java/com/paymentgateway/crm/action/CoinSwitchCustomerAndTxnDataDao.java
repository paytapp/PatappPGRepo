package com.paymentgateway.crm.action;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.CoinSwitchCustomer;
import com.paymentgateway.commons.user.CoinSwitchTransactionObject;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @auther Sandeep Sharma
 */
@Service
public class CoinSwitchCustomerAndTxnDataDao {

	private static Logger logger = LoggerFactory.getLogger(CoinSwitchCustomerAndTxnDataDao.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;

	private int totalCount;

	@SuppressWarnings("static-access")
	public int fetchCustomerCount(String custName, String custEmail, String custPhone, String virtualAccountNo,
			String status) {
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			if (StringUtils.isNotBlank(custName)) {
				BasicDBObject custNameObj = new BasicDBObject(FieldType.CUST_NAME.getName(), custName);
				paramConditionLst.add(custNameObj);
			}
			if (StringUtils.isNotBlank(custEmail)) {
				BasicDBObject custEmailObj = new BasicDBObject(FieldType.CUST_EMAIL.getName(), custEmail);
				paramConditionLst.add(custEmailObj);
			}
			if (StringUtils.isNotBlank(custPhone)) {
				BasicDBObject custPhoneObj = new BasicDBObject(FieldType.PHONE_NO.getName(), custPhone);
				paramConditionLst.add(custPhoneObj);
			}
			if (StringUtils.isNotBlank(virtualAccountNo)) {
				BasicDBObject virtualAccNoObj = new BasicDBObject(FieldType.VIRTUAL_ACC_NUM.getName(),
						virtualAccountNo);
				paramConditionLst.add(virtualAccNoObj);
			}
			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				BasicDBObject statusObj = new BasicDBObject(FieldType.STATUS.getName(), status);
				paramConditionLst.add(statusObj);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COINSWITCH_ACCOUNTS.getValue()));
			BasicDBObject finalquery = new BasicDBObject();
			if (!paramConditionLst.isEmpty()) {
				finalquery = new BasicDBObject("$and", paramConditionLst);
			} else {
			}
			totalCount = (int) coll.countDocuments(finalquery);
		} catch (Exception e) {
			logger.error("Exception in fetching Customer data >> ", e);
		}
		return totalCount;
	}

	@SuppressWarnings("static-access")
	public List<CoinSwitchCustomer> fetchCustomerData(String custName, String custEmail, String custPhone,
			String virtualAccountNo, String status, int start, int length) {
		List<CoinSwitchCustomer> custData = new ArrayList<CoinSwitchCustomer>();
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			if (StringUtils.isNotBlank(custName)) {
				BasicDBObject custNameObj = new BasicDBObject(FieldType.CUST_NAME.getName(), custName);
				paramConditionLst.add(custNameObj);
			}
			if (StringUtils.isNotBlank(custEmail)) {
				BasicDBObject custEmailObj = new BasicDBObject(FieldType.CUST_EMAIL.getName(), custEmail);
				paramConditionLst.add(custEmailObj);
			}
			if (StringUtils.isNotBlank(custPhone)) {
				BasicDBObject custPhoneObj = new BasicDBObject(FieldType.PHONE_NO.getName(), custPhone);
				paramConditionLst.add(custPhoneObj);
			}
			if (StringUtils.isNotBlank(virtualAccountNo)) {
				BasicDBObject virtualAccNoObj = new BasicDBObject(FieldType.VIRTUAL_ACC_NUM.getName(),
						virtualAccountNo);
				paramConditionLst.add(virtualAccNoObj);
			}
			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				BasicDBObject statusObj = new BasicDBObject(FieldType.STATUS.getName(), status);
				paramConditionLst.add(statusObj);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COINSWITCH_ACCOUNTS.getValue()));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));
			BasicDBObject skip = new BasicDBObject("$skip", start);
			BasicDBObject limit = new BasicDBObject("$limit", length);
			List<BasicDBObject> pipeline;
			if (!paramConditionLst.isEmpty()) {
				BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
				logger.info("final query for Coin Switch Customer data = " + finalquery);
				BasicDBObject match = new BasicDBObject("$match", finalquery);
				pipeline = Arrays.asList(match, skip, limit, sort);
			} else {
				pipeline = Arrays.asList(skip, limit, sort);
			}

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
						&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
				dbobj = dataEncDecTool.decryptDocument(dbobj);
				}
				CoinSwitchCustomer coinSwitchTCustomerObject = new CoinSwitchCustomer();

				coinSwitchTCustomerObject.setCustId(dbobj.getString(FieldType.CUST_ID.getName()));
				coinSwitchTCustomerObject.setPhoneNo(dbobj.getString(FieldType.PHONE_NO.getName()));
				coinSwitchTCustomerObject.setCustName(dbobj.getString(FieldType.CUST_NAME.getName()));
				coinSwitchTCustomerObject.setVirtualAccountNo(dbobj.getString(FieldType.VIRTUAL_ACC_NUM.getName()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.AADHAR.getName()))) {
					coinSwitchTCustomerObject.setAadhar(dbobj.getString(FieldType.AADHAR.getName()));
				} else {
					coinSwitchTCustomerObject.setAadhar("NA");
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAN.getName()))) {
					coinSwitchTCustomerObject.setPan(dbobj.getString(FieldType.PAN.getName()));
				} else {
					coinSwitchTCustomerObject.setPan("NA");
				}
				coinSwitchTCustomerObject.setAddress(dbobj.getString(FieldType.ADDRESS.getName()));
				coinSwitchTCustomerObject.setEmailId(dbobj.getString(FieldType.CUST_EMAIL.getName()));
				coinSwitchTCustomerObject.setDob(dbobj.getString(FieldType.DOB.getName()));
				coinSwitchTCustomerObject.setAccountNo(dbobj.getString(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
				coinSwitchTCustomerObject.setBankName(dbobj.getString(FieldType.BANK_NAME.getName()));
				coinSwitchTCustomerObject.setBankIfsc(dbobj.getString(FieldType.IFSC_CODE.getName()));
				coinSwitchTCustomerObject
						.setBankAccountHolderName(dbobj.getString(FieldType.ACCOUNT_HOLDER_NAME.getName()));
				coinSwitchTCustomerObject.setCreateDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				coinSwitchTCustomerObject.setStatus(dbobj.getString(FieldType.STATUS.getName()));

				custData.add(coinSwitchTCustomerObject);
			}

		} catch (Exception e) {
			logger.error("Exception in fetching Customer data >> ", e);
		}
		return custData;
	}

	@SuppressWarnings("static-access")
	public int fetchCustomerTransactionCount(String custName, String custEmail, String custPhone,
			String virtualAccountNo, String status, String rrn, String pgRefNo, String purpose, String txnType,
			String dateFrom, String dateTo) {
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			DateFormat formatdate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
			Date dateStart = null;
			Date dateEnd = null;
			try {
				dateStart = formatdate.parse(dateFrom);
				dateEnd = formatdate.parse(dateTo);
			} catch (ParseException e) {
				logger.error("Exception in date parsing ", e);
			}

			String startString = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + " 00:00:00";
			String endString = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + " 23:59:59";

			BasicDBObject dateQuery = new BasicDBObject();
			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startString).toLocalizedPattern())
							.add("$lt", new SimpleDateFormat(endString).toLocalizedPattern()).get());
			paramConditionLst.add(dateQuery);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}
			BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);
			BasicDBObject dateIndexQuery = new BasicDBObject(FieldType.DATE_INDEX.getName(), dateIndexIn);
			paramConditionLst.add(dateIndexQuery);

			if (StringUtils.isNotBlank(custName)) {
				BasicDBObject custNameObj = new BasicDBObject(FieldType.CUST_NAME.getName(), custName);
				paramConditionLst.add(custNameObj);
			}
			if (StringUtils.isNotBlank(virtualAccountNo)) {
				BasicDBObject virtualAccNoObj = new BasicDBObject(FieldType.VIRTUAL_ACC_NUM.getName(),
						virtualAccountNo);
				paramConditionLst.add(virtualAccNoObj);
			}
			if (StringUtils.isNotBlank(custPhone)) {
				BasicDBObject phnNoObj = new BasicDBObject(FieldType.PHONE_NO.getName(), custPhone);
				paramConditionLst.add(phnNoObj);
			}
			if (StringUtils.isNotBlank(custEmail)) {
				BasicDBObject custEmailObj = new BasicDBObject(FieldType.CUST_EMAIL.getName(), custEmail);
				paramConditionLst.add(custEmailObj);
			}
			if (StringUtils.isNotBlank(rrn)) {
				BasicDBObject rrnObj = new BasicDBObject(FieldType.RRN.getName(), rrn);
				paramConditionLst.add(rrnObj);
			}
			if (StringUtils.isNotBlank(pgRefNo)) {
				BasicDBObject pgRefNoObj = new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNo);
				paramConditionLst.add(pgRefNoObj);
			}

			if (StringUtils.isNotBlank(txnType) && !txnType.equalsIgnoreCase("ALL")) {
				BasicDBObject statusObj = new BasicDBObject(FieldType.TXNTYPE.getName(), txnType);
				paramConditionLst.add(statusObj);
			}

			if (StringUtils.isNotBlank(purpose) && !purpose.equalsIgnoreCase("ALL")) {
				BasicDBObject statusObj = new BasicDBObject(FieldType.PURPOSE.getName(), purpose);
				paramConditionLst.add(statusObj);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COINSWITCH_TRANSACTION_DATA.getValue()));

			BasicDBObject finalquery = new BasicDBObject();
			if (!paramConditionLst.isEmpty()) {
				finalquery = new BasicDBObject("$and", paramConditionLst);
				logger.info("final query for Coin Switch Customer data = " + finalquery);
			} else {
			}

			totalCount = (int) coll.countDocuments(finalquery);

		} catch (Exception e) {
			logger.error("Exception in fetching transaction data >> ", e);
		}

		return totalCount;
	}

	@SuppressWarnings("static-access")
	public List<CoinSwitchTransactionObject> fetchCustomerTransaction(String custName, String custEmail,
			String custPhone, String virtualAccountNo, String status, String rrn, String pgRefNo, String purpose,
			String txnType, String dateFrom, String dateTo, int start, int length) {
		List<CoinSwitchTransactionObject> transactDate = new ArrayList<CoinSwitchTransactionObject>();
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			DateFormat formatdate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
			Date dateStart = null;
			Date dateEnd = null;
			try {
				dateStart = formatdate.parse(dateFrom);
				dateEnd = formatdate.parse(dateTo);
			} catch (ParseException e) {
				logger.error("Exception in date parsing ", e);
			}

			String startString = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + " 00:00:00";
			String endString = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + " 23:59:59";

			BasicDBObject dateQuery = new BasicDBObject();
			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startString).toLocalizedPattern())
							.add("$lt", new SimpleDateFormat(endString).toLocalizedPattern()).get());
			paramConditionLst.add(dateQuery);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}
			BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);
			BasicDBObject dateIndexQuery = new BasicDBObject(FieldType.DATE_INDEX.getName(), dateIndexIn);
			paramConditionLst.add(dateIndexQuery);

			if (StringUtils.isNotBlank(custName)) {
				BasicDBObject custNameObj = new BasicDBObject(FieldType.CUST_NAME.getName(), custName);
				paramConditionLst.add(custNameObj);
			}
			if (StringUtils.isNotBlank(virtualAccountNo)) {
				BasicDBObject virtualAccNoObj = new BasicDBObject(FieldType.VIRTUAL_ACC_NUM.getName(),
						virtualAccountNo);
				paramConditionLst.add(virtualAccNoObj);
			}
			if (StringUtils.isNotBlank(custPhone)) {
				BasicDBObject phnNoObj = new BasicDBObject(FieldType.PHONE_NO.getName(), custPhone);
				paramConditionLst.add(phnNoObj);
			}
			if (StringUtils.isNotBlank(custEmail)) {
				BasicDBObject custEmailObj = new BasicDBObject(FieldType.CUST_EMAIL.getName(), custEmail);
				paramConditionLst.add(custEmailObj);
			}
			if (StringUtils.isNotBlank(rrn)) {
				BasicDBObject rrnObj = new BasicDBObject(FieldType.RRN.getName(), rrn);
				paramConditionLst.add(rrnObj);
			}
			if (StringUtils.isNotBlank(txnType) && !txnType.equalsIgnoreCase("ALL")) {
				BasicDBObject statusObj = new BasicDBObject(FieldType.TXNTYPE.getName(), txnType);
				paramConditionLst.add(statusObj);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COINSWITCH_TRANSACTION_DATA.getValue()));

			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));
			BasicDBObject skip = new BasicDBObject("$skip", start);
			BasicDBObject limit = new BasicDBObject("$limit", length);
			List<BasicDBObject> pipeline;
			if (!paramConditionLst.isEmpty()) {
				BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
				logger.info("final query for Coin Switch Customer data = " + finalquery);
				BasicDBObject match = new BasicDBObject("$match", finalquery);
				pipeline = Arrays.asList(match, skip, limit, sort);
			} else {
				pipeline = Arrays.asList(skip, limit, sort);
			}
			// logger.info(pipeline.toString());

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				CoinSwitchTransactionObject coinSwitchTransactionObject = new CoinSwitchTransactionObject();

				coinSwitchTransactionObject.setCustId(dbobj.getString(FieldType.CUST_ID.getName()));
				coinSwitchTransactionObject.setPhoneNo(dbobj.getString(FieldType.PHONE_NO.getName()));
				coinSwitchTransactionObject.setCustName(dbobj.getString(FieldType.CUST_NAME.getName()));
				coinSwitchTransactionObject.setVirtualAccountNo(dbobj.getString(FieldType.VIRTUAL_ACC_NUM.getName()));
				coinSwitchTransactionObject.setAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()));
				coinSwitchTransactionObject.setTxnType(dbobj.getString(FieldType.TXNTYPE.getName()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))) {
					coinSwitchTransactionObject.setRrn(dbobj.getString(FieldType.RRN.getName()));
				} else {
					coinSwitchTransactionObject.setRrn("NA");
				}
				coinSwitchTransactionObject.setPaymentType(dbobj.getString(FieldType.PAYMENT_TYPE.getName()));
				coinSwitchTransactionObject.setCreateDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				coinSwitchTransactionObject.setStatus(dbobj.getString(FieldType.STATUS.getName()));

				transactDate.add(coinSwitchTransactionObject);
			}

		} catch (Exception e) {
			logger.error("Exception in fetching transaction data >> ", e);
		}

		return transactDate;
	}
}
