package com.paymentgateway.commons.dao;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;

/**
 * @author Pooja Pancholi, Shiva
 *
 */

@Service
public class MerchantInitiatedDirectDao {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userDao;

	@Autowired
	private Fields field;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	@Autowired
	private PayoutAcquirerMappingDao payoutAcquirerMappingDao;
	
	@Autowired
	private UserSettingDao userSettingDao;
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(MerchantInitiatedDirectDao.class.getName());

	public boolean isDailyLimitExceed(Map<String, String> requestMap) {
		logger.info("Inside  isDailyLimitExceed()");

		try {
			String fieldAmount = Amount.toDecimal(requestMap.get(FieldType.AMOUNT.getName()),
					requestMap.get(FieldType.CURRENCY_CODE.getName()));

			BigDecimal closingTransactionDiff = getClosingTransactionAmount(requestMap);
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
		} catch (Exception e) {
			logger.error("Exception isDailyLimitExceed : ", e);
		}
		return true;
	}

	public boolean isDailyLimitExceeded(Map<String, String> requestMap) {
		logger.info("Inside  isDailyLimitExceed()");

		try {
			String fieldAmount = Amount.toDecimal(requestMap.get(FieldType.AMOUNT.getName()),
					requestMap.get(FieldType.CURRENCY_CODE.getName()));

			BigDecimal closingTransactionDiff = getClosingTransactionAmount(requestMap);
			if (closingTransactionDiff.compareTo(BigDecimal.ZERO) <= 0) {
				insertInsufficientData(requestMap, fieldAmount);
				return true;
			}

			BigDecimal checkDiff = new BigDecimal(0);
			checkDiff = checkDiff.add(closingTransactionDiff).setScale(2);
			checkDiff = checkDiff.subtract(new BigDecimal(fieldAmount).setScale(2));

			logger.info("Calculated settled amount is " + checkDiff);

			if (checkDiff.compareTo(BigDecimal.ZERO) >= 0) {
				return false;
			} else {
				insertInsufficientData(requestMap, fieldAmount);
			}
			return true;
		} catch (Exception e) {
			logger.error("Exception isDailyLimitExceed : ", e);
		}
		return true;
	}

	public void insertInsufficientData(Map<String, String> requestMap, String fieldAmount) {
		logger.info("Inside insertInsufficientData()");
		try {
			requestMap.replace(FieldType.AMOUNT.getName(), requestMap.get(FieldType.AMOUNT.getName()), fieldAmount);
			Fields fields = new Fields(requestMap);
			String payId = fields.get(FieldType.PAY_ID.getName());
			fields.put(FieldType.TXN_ID.getName(), TransactionManager.getId());
			fields.put(FieldType.STATUS.getName(), StatusType.DECLINED.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(),
					ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseMessage());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(),
					ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getCode());
			fields.put(FieldType.IS_STATUS_FINAL.getName(), "Y");
			User user = userDao.findPayId(payId);

			fields.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo());
			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
			}
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);
			fields.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));
			fields.put(FieldType.UPDATE_DATE.getName(), dateNow);
			field.insertIciciCompositeFields(fields);
			logger.info("InsertInsufficientData by CRM : Txn Id= " + fields.get(FieldType.TXN_ID.getName()) + " , "
					+ fields);
		} catch (Exception e) {
			logger.error("Exception Declined due to insufficient balance : ", e);
		}
	}

	public BigDecimal getClosingTransactionAmount(Map<String, String> requestMap) {
		logger.info("Inside getImpsTransactionAmount()");
		BigDecimal totalAmount = new BigDecimal("0").setScale(2);
		try {

			BigDecimal closingAmount = new BigDecimal("0").setScale(2);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
			BasicDBObject query = new BasicDBObject();

			String payId = requestMap.get(FieldType.PAY_ID.getName());
			String subMerchantId = null;

			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantId = user.getPayId();
				payId = user.getSuperMerchantId();
			}

			if (StringUtils.isNotBlank(payId)) {
				query.append(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantId)) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			BasicDBObject match2 = new BasicDBObject("$match", query);
			BasicDBObject sort2 = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline2 = Arrays.asList(match2, sort2);
			AggregateIterable<Document> output2 = coll.aggregate(pipeline2);
			output2.allowDiskUse(true);
			MongoCursor<Document> cursor = output2.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				closingAmount = (dbAmount);
				break;
			}
			cursor.close();

			totalAmount = closingAmount;
			logger.info("Total today's Transactions Amount  " + totalAmount);
		} catch (Exception e) {
			logger.error("Exception caugth fetching imps transaction : ", e);
		}
		return totalAmount;

	}

	public BigDecimal getECollectionTransactionAmount(Map<String, String> requestMap) {
		logger.info("Inside getImpsTransactionAmount()");
		BigDecimal totalAmount = new BigDecimal("0").setScale(2);
		try {

			BigDecimal closingAmount = new BigDecimal("0").setScale(2);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String todayDate = sdf.format(new Date());

			String dateFrom = todayDate + " " + "00:00:00";
			String dateTo = todayDate + " " + "23:59:59";

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.E_COLLECTION.getValue()));
			BasicDBObject query = new BasicDBObject();
			BasicDBObject queryClosing = new BasicDBObject();

			query.append(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
			String payId = requestMap.get(FieldType.PAY_ID.getName());
			String subMerchantId = null;

			BigDecimal settledAmount = new BigDecimal("0").setScale(2);

			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantId = user.getPayId();
				payId = user.getSuperMerchantId();
			}

			if (StringUtils.isNotBlank(payId)) {
				query.append(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantId)) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			query.append(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			MongoCursor<Document> cursor = coll.find(query).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				settledAmount = settledAmount.add(dbAmount);
			}
			cursor.close();
			logger.info("settledAmount Amount  " + settledAmount);
			MongoDatabase dbIns1 = mongoInstance.getDB();
			MongoCollection<Document> coll1 = dbIns1.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));

			queryClosing.append(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo());

			BasicDBObject match = new BasicDBObject("$match", queryClosing);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CLOSING_DATE.name(), -1));
			BasicDBObject limit = new BasicDBObject("$limit", 1);
			List<BasicDBObject> pipeline2 = Arrays.asList(match, sort, limit);
			AggregateIterable<Document> output2 = coll1.aggregate(pipeline2);
			output2.allowDiskUse(true);
			MongoCursor<Document> cursor2 = output2.iterator();

			while (cursor2.hasNext()) {
				Document dbobj = cursor2.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				closingAmount = closingAmount.add(dbAmount);
			}

			cursor2.close();

			totalAmount = closingAmount.add(settledAmount);
			logger.info("Total today's Transactions Amount  " + totalAmount);
		} catch (Exception e) {
			logger.error("Exception caugth fetching imps transaction : ", e);
		}
		return totalAmount;

	}

	public BigDecimal getImpsTransactionAmount(Map<String, String> requestMap) {
		logger.info("Inside getImpsTransactionAmount()");
		BigDecimal totalAmount = new BigDecimal("0").setScale(2);
		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String todayDate = sdf.format(new Date());

			String dateFrom = todayDate + " " + "00:00:00";
			String dateTo = todayDate + " " + "23:59:59";

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject query = new BasicDBObject();

			query.append(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());

			String payId = requestMap.get(FieldType.PAY_ID.getName());
			String subMerchantId = null;

			BigDecimal settledAmount = new BigDecimal("0").setScale(2);

			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantId = user.getPayId();
				payId = user.getSuperMerchantId();
			}

			if (StringUtils.isNotBlank(payId)) {
				query.append(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantId)) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			query.append(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			query.append(FieldType.USER_TYPE.getName(), "Merchant Initiated Direct");
			MongoCursor<Document> cursor = coll.find(query).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				settledAmount = settledAmount.add(dbAmount);
			}
			totalAmount = settledAmount;
			logger.info("Total today's Transactions Amount  " + totalAmount);
		} catch (Exception e) {
			logger.error("Exception caugth fetching imps transaction : ", e);
		}
		return totalAmount;

	}

	public void insertUpdateForClosing(Map<String, String> requestMap) throws ParseException {

		String fieldAmount = Amount.toDecimal(requestMap.get(FieldType.AMOUNT.getName()),
				requestMap.get(FieldType.CURRENCY_CODE.getName()));
		MongoDatabase dbIns1 = mongoInstance.getDB();
		MongoCollection<Document> collection1 = dbIns1.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
		BasicDBObject newFieldsObj1 = new BasicDBObject();

		Date dNow = new Date();
		String dateNow = DateCreater.formatDateForDb(dNow);
		SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dtDate = dateFormat.format(inputDate.parse(dateNow));

		String dateFrom = dtDate + " " + "00:00:00";
		String toDate = dtDate + " " + "23:59:59";
		newFieldsObj1.append(FieldType.CREATE_DATE.getName(),
				BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
						.add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());

		User user = userDao.findPayId(requestMap.get(FieldType.PAY_ID.getName()));
		if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
			newFieldsObj1.append(FieldType.SUB_MERCHANT_ID.getName(), requestMap.get(FieldType.PAY_ID.getName()));
		} else {
			newFieldsObj1.append(FieldType.PAY_ID.getName(), requestMap.get(FieldType.PAY_ID.getName()));
		}
		MongoCursor<Document> cursor = collection1.find(newFieldsObj1).iterator();
		BigDecimal closingAmount = new BigDecimal("0").setScale(2);
		BigDecimal openingAmount = new BigDecimal("0").setScale(2);
		BigDecimal impsAmount = new BigDecimal("0").setScale(2);
		BigDecimal creditAmount = new BigDecimal("0").setScale(2);
		BigDecimal debitAmount = new BigDecimal("0").setScale(2);
		boolean flag = false;
		while (cursor.hasNext()) {
			flag = true;
			Document dbobj = cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				dbobj = dataEncDecTool.decryptDocument(dbobj);
			}

			BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
			impsAmount = new BigDecimal(fieldAmount).setScale(2);
			debitAmount = impsAmount.add(new BigDecimal(dbobj.getString(FieldType.DEBIT_AMOUNT.getName())).setScale(2));
			closingAmount = dbAmount.subtract(impsAmount);
		}
		cursor.close();
		if (flag) {
			Bson filter;
			if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				filter = new Document(FieldType.SUB_MERCHANT_ID.getName(), requestMap.get(FieldType.PAY_ID.getName()))
						.append(FieldType.CLOSING_DATE.getName(), dateFrom);
			} else {
				filter = new Document(FieldType.PAY_ID.getName(), requestMap.get(FieldType.PAY_ID.getName()))
						.append(FieldType.CLOSING_DATE.getName(), dateFrom);
			}

			Bson newValue = new Document(FieldType.DEBIT_AMOUNT.getName(), debitAmount.toString())
					.append(FieldType.AMOUNT.getName(), closingAmount.toString())
					.append(FieldType.UPDATE_DATE.getName(), dateNow);
			Bson updateOperationDocument = new Document("$set", newValue);
			collection1.updateOne(filter, updateOperationDocument);
		} else {
			Document doc1 = new Document();

			BasicDBObject finalquery2 = new BasicDBObject();
			doc1.put(FieldType.CREATE_DATE.getName(), dateNow);
			doc1.put(FieldType.UPDATE_DATE.getName(), dateNow);
			doc1.put(FieldType.CLOSING_DATE.getName(), dateFrom);
			doc1.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo());
			if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				doc1.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				doc1.put(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId());
				finalquery2.append(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId());
			} else {
				doc1.put(FieldType.PAY_ID.getName(), requestMap.get(FieldType.PAY_ID.getName()));
				finalquery2.append(FieldType.PAY_ID.getName(), requestMap.get(FieldType.PAY_ID.getName()));
			}

			BasicDBObject match2 = new BasicDBObject("$match", finalquery2);
			BasicDBObject sort2 = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline2 = Arrays.asList(match2, sort2);
			AggregateIterable<Document> output2 = collection1.aggregate(pipeline2);
			output2.allowDiskUse(true);
			MongoCursor<Document> cursor2 = output2.iterator();
			while (cursor2.hasNext()) {
				Document dbobj1 = cursor2.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj1 = dataEncDecTool.decryptDocument(dbobj1);
				}

				BigDecimal dbAmount1 = new BigDecimal(dbobj1.getString(FieldType.AMOUNT.getName())).setScale(2);
				impsAmount = new BigDecimal(fieldAmount).setScale(2);

				if (impsAmount.toString().compareTo(dbAmount1.toString()) >= 0) {
					closingAmount = dbAmount1.subtract(impsAmount);
					openingAmount = dbAmount1;
				}
				break;
			}
			doc1.put(FieldType.DEBIT_AMOUNT.getName(), fieldAmount);
			if (closingAmount.toString().equalsIgnoreCase("0.00")) {
				doc1.put(FieldType.AMOUNT.getName(), fieldAmount);
			} else {
				doc1.put(FieldType.AMOUNT.getName(), closingAmount.toString());
			}
			doc1.put(FieldType.CREDIT_AMOUNT.getName(), creditAmount.toString());
			doc1.put(FieldType.OPENING_AMOUNT.getName(), openingAmount.toString());
			doc1.put(FieldType.CREATE_DATE.getName(), dateNow);

			collection1.insertOne(doc1);
		}
	}

	public void UpdateForClosing(Map<String, String> respMap) throws ParseException {
		MongoDatabase dbIns1 = mongoInstance.getDB();
		MongoCollection<Document> collection1 = dbIns1.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
		BasicDBObject newFieldsObj1 = new BasicDBObject();

		Date dNow = new Date();
		String dateNow = DateCreater.formatDateForDb(dNow);
		SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dtDate = dateFormat.format(inputDate.parse(dateNow));

		String dateFrom = dtDate + " " + "00:00:00";
		String toDate = dtDate + " " + "23:59:59";
		newFieldsObj1.append(FieldType.CREATE_DATE.getName(),
				BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
						.add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());
		User user = null;
		if (respMap.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
			user = userDao.findPayId(respMap.get(FieldType.SUB_MERCHANT_ID.getName()));
			newFieldsObj1.append(FieldType.SUB_MERCHANT_ID.getName(), respMap.get(FieldType.SUB_MERCHANT_ID.getName()));
		} else {
			user = userDao.findPayId(respMap.get(FieldType.PAY_ID.getName()));
			newFieldsObj1.append(FieldType.PAY_ID.getName(), respMap.get(FieldType.PAY_ID.getName()));
		}
		MongoCursor<Document> cursor = collection1.find(newFieldsObj1).iterator();
		BigDecimal closingAmount = new BigDecimal("0").setScale(2);
		BigDecimal impsAmount = new BigDecimal("0").setScale(2);
		BigDecimal creditAmount = new BigDecimal("0").setScale(2);
		BigDecimal debitAmount = new BigDecimal("0").setScale(2);
		boolean flag = false;
		while (cursor.hasNext()) {
			flag = true;
			Document dbobj = cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				dbobj = dataEncDecTool.decryptDocument(dbobj);
			}

			BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
			impsAmount = new BigDecimal(respMap.get(FieldType.AMOUNT.getName())).setScale(2);
			debitAmount = new BigDecimal(dbobj.getString(FieldType.DEBIT_AMOUNT.getName())).subtract(impsAmount)
					.setScale(2);
			closingAmount = dbAmount.add(impsAmount);
		}
		cursor.close();
		if (flag) {
			Bson filter;
			if (respMap.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
				filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
						respMap.get(FieldType.SUB_MERCHANT_ID.getName())).append(FieldType.CLOSING_DATE.getName(),
								dateFrom);
			} else {
				filter = new Document(FieldType.PAY_ID.getName(), respMap.get(FieldType.PAY_ID.getName()))
						.append(FieldType.CLOSING_DATE.getName(), dateFrom);
			}

			Bson newValue = new Document(FieldType.DEBIT_AMOUNT.getName(), debitAmount.toString())
					.append(FieldType.AMOUNT.getName(), closingAmount.toString())
					.append(FieldType.UPDATE_DATE.getName(), dateNow);
			Bson updateOperationDocument = new Document("$set", newValue);
			collection1.updateOne(filter, updateOperationDocument);
		} else {
			Document doc1 = new Document();

			BasicDBObject finalquery2 = new BasicDBObject();
			doc1.put(FieldType.CREATE_DATE.getName(), dateNow);
			doc1.put(FieldType.UPDATE_DATE.getName(), dateNow);
			doc1.put(FieldType.CLOSING_DATE.getName(), dateFrom);
			if (respMap.containsKey(FieldType.VIRTUAL_AC_CODE.getName())) {
				doc1.put(FieldType.VIRTUAL_AC_CODE.getName(), respMap.get(FieldType.VIRTUAL_AC_CODE.getName()));
			} else {
				doc1.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getAccountNo());
			}
			if (respMap.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
				doc1.put(FieldType.PAY_ID.getName(), respMap.get(FieldType.PAY_ID.getName()));
				doc1.put(FieldType.SUB_MERCHANT_ID.getName(), respMap.get(FieldType.SUB_MERCHANT_ID.getName()));
				finalquery2.append(FieldType.SUB_MERCHANT_ID.getName(),
						respMap.get(FieldType.SUB_MERCHANT_ID.getName()));
			} else {
				doc1.put(FieldType.PAY_ID.getName(), respMap.get(FieldType.PAY_ID.getName()));
				finalquery2.append(FieldType.PAY_ID.getName(), respMap.get(FieldType.PAY_ID.getName()));
			}

			BasicDBObject match2 = new BasicDBObject("$match", finalquery2);
			BasicDBObject sort2 = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline2 = Arrays.asList(match2, sort2);
			AggregateIterable<Document> output2 = collection1.aggregate(pipeline2);
			output2.allowDiskUse(true);
			MongoCursor<Document> cursor2 = output2.iterator();
			while (cursor2.hasNext()) {
				Document dbobj1 = cursor2.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj1 = dataEncDecTool.decryptDocument(dbobj1);
				}

				BigDecimal dbAmount1 = new BigDecimal(dbobj1.getString(FieldType.AMOUNT.getName())).setScale(2);
				impsAmount = new BigDecimal(respMap.get(FieldType.AMOUNT.getName())).setScale(2);

				if (impsAmount.toString().compareTo(dbAmount1.toString()) >= 0) {
					closingAmount = dbAmount1.subtract(impsAmount);
				}
				break;
			}
			doc1.put(FieldType.DEBIT_AMOUNT.getName(), respMap.get(FieldType.AMOUNT.getName()));
			if (closingAmount.toString().equalsIgnoreCase("0.00")) {
				doc1.put(FieldType.AMOUNT.getName(), respMap.get(FieldType.AMOUNT.getName()));
			} else {
				doc1.put(FieldType.AMOUNT.getName(), closingAmount.toString());
			}
			doc1.put(FieldType.CREDIT_AMOUNT.getName(), creditAmount.toString());
			doc1.put(FieldType.CREATE_DATE.getName(), dateNow);

			collection1.insertOne(doc1);
		}
	}

	public boolean isDuplicateOrderId(String payId, String subMerchantPayId, String orderId, String virtualAccount,
			String txnType) {

		logger.info("Faching Duplicate order Id in Bulk by orderId : " + orderId);
		try {
			User user = null;
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject query = new BasicDBObject();

			if (StringUtils.isNotBlank(subMerchantPayId)) {
				user = userDao.findPayId(subMerchantPayId);
				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					subMerchantPayId = user.getPayId();
					payId = user.getSuperMerchantId();
				}
			}

			if (StringUtils.isNotBlank(payId)) {
				query.append(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantPayId)) {

				query.append(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId);
			}

			if (StringUtils.isNotBlank(orderId)) {
				query.append(FieldType.ORDER_ID.getName(), orderId);
			}

			if (StringUtils.isNotBlank(virtualAccount)) {
				query.append(FieldType.VIRTUAL_AC_CODE.getName(), virtualAccount);
			}

//			if (StringUtils.isNotBlank(txnType)) {
//				query.append(FieldType.TXNTYPE.getName(), txnType);
//			}

			MongoCursor<Document> cursor = coll.find(query).iterator();

			if (cursor.hasNext()) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			logger.error("Exception caugth by fatching duplicate order id in bulk : ", e);
		}
		return false;
	}

	public void insertImpsBulkData(ImpsDownloadObject impsData) {
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
		try {
			Document doc = new Document();
			doc.put(FieldType.ORDER_ID.getName(), impsData.getOrderId());
			doc.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());
			doc.put(FieldType.BENE_NAME.getName(), impsData.getBeneAccountName());
			doc.put(FieldType.BENE_ACCOUNT_NO.getName(), impsData.getBankAccountNumber());
			doc.put(FieldType.BANK_NAME.getName(), impsData.getBankAccountName());
			doc.put(FieldType.IFSC_CODE.getName(), impsData.getBankIFSC());
			doc.put(FieldType.PHONE_NO.getName(), impsData.getPhoneNo());
			doc.put(FieldType.AMOUNT.getName(), impsData.getAmount());
			doc.put(FieldType.REMARKS.getName(), impsData.getRemarks());
			doc.put(FieldType.VIRTUAL_AC_CODE.getName(), impsData.getVirtualAccount());
			doc.put(FieldType.STATUS.getName(), "Pending");
			doc.put(FieldType.PURPOSE.getName(), impsData.getPurpose());
			doc.put(FieldType.TXNTYPE.getName(), impsData.getTxnType());
			doc.put(FieldType.USER_TYPE.getName(), "Merchant Initiated Direct");
			if (StringUtils.isNotBlank(impsData.getSubMerchant())) {
				doc.put(FieldType.SUB_MERCHANT_ID.getName(), impsData.getSubMerchant());
			}
			logger.info("insert");
			if (StringUtils.isNotBlank(impsData.getMerchantPayId())) {
				doc.put(FieldType.PAY_ID.getName(), impsData.getMerchantPayId());
			}
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			doc.put(FieldType.CREATE_DATE.getName(), dateNow);
			doc.put("DATE_INDEX", dateNow.substring(0, 10).replace("-", ""));
			doc.put(FieldType.UPDATE_DATE.getName(), dateNow);

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				coll.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				coll.insertOne(doc);
			}
			// coll.insertOne(doc);

		} catch (Exception e) {
			logger.error("Exception caugth by fatching duplicate order id in bulk : ", e);
		}
	}

	public void insertUPIBulkData(ImpsDownloadObject impsData) {
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
		try {
			Document doc = new Document();
			doc.put(FieldType.ORDER_ID.getName(), impsData.getOrderId());
			doc.put(FieldType.TXN_ID.getName(), TransactionManager.getId());
			doc.put(FieldType.PAYER_ADDRESS.getName(), impsData.getPayerAddress());
			doc.put(FieldType.PAYER_NAME.getName(), impsData.getPayerName());
			doc.put(FieldType.PHONE_NO.getName(), impsData.getPhoneNo());
			doc.put(FieldType.AMOUNT.getName(), impsData.getAmount());
			doc.put(FieldType.REMARKS.getName(), impsData.getRemarks());
			doc.put(FieldType.VIRTUAL_AC_CODE.getName(), impsData.getVirtualAccount());
			doc.put(FieldType.STATUS.getName(), "Pending");
			doc.put(FieldType.PURPOSE.getName(), impsData.getPurpose());
			doc.put(FieldType.TXNTYPE.getName(), impsData.getTxnType());
			doc.put(FieldType.USER_TYPE.getName(), "Merchant Initiated Direct");
			if (StringUtils.isNotBlank(impsData.getSubMerchant())) {
				doc.put(FieldType.SUB_MERCHANT_ID.getName(), impsData.getSubMerchant());
			}
			logger.info("insert");
			if (StringUtils.isNotBlank(impsData.getMerchantPayId())) {
				doc.put(FieldType.PAY_ID.getName(), impsData.getMerchantPayId());
			}
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			doc.put(FieldType.CREATE_DATE.getName(), dateNow);
			doc.put("DATE_INDEX", dateNow.substring(0, 10).replace("-", ""));
			doc.put(FieldType.UPDATE_DATE.getName(), dateNow);

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				coll.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				coll.insertOne(doc);
			}
			// coll.insertOne(doc);

		} catch (Exception e) {
			logger.error("Exception caugth by fatching duplicate order id in bulk ", e);
		}
	}

	@SuppressWarnings("static-access")
	public List<ImpsDownloadObject> merchantInitiatedDirectReportData(String dateFrom, String dateTo, String merchantId,
			String subMerchantPayId, String status, String orderId, String beneAccountNumber, String payerAddress,
			String mode, User user) {

		logger.info("Inside MerchantInitiatedDirectDao, merchantInitiatedDirectReportData()");

		List<ImpsDownloadObject> payOutList = new ArrayList<ImpsDownloadObject>();
		Map<String, User> userMap = new HashMap<String, User>();
		Set<String> orderIdd = new HashSet<String>();
		Set<String> payIdd = new HashSet<String>();
		boolean isParameterised = false;

		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> statusConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject modeQuery = new BasicDBObject();
			BasicDBObject userQuery = new BasicDBObject();
			BasicDBObject orderIdQuery = new BasicDBObject();
			BasicDBObject beneAccountQuery = new BasicDBObject();
			BasicDBObject payerAddressQuery = new BasicDBObject();

			String currentDate = null;
			if (!dateFrom.isEmpty()) {
				if (!dateTo.isEmpty()) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));

				isParameterised = true;
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}

			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				statusQuery = new BasicDBObject(FieldType.STATUS.name(), status);
			}

			if (StringUtils.isNotBlank(orderId) && !orderId.equalsIgnoreCase("ALL")) {
				orderIdQuery = new BasicDBObject(FieldType.ORDER_ID.name(), orderId);
			}

			if (StringUtils.isNotBlank(beneAccountNumber) && !beneAccountNumber.equalsIgnoreCase("ALL")) {
				beneAccountQuery = new BasicDBObject(FieldType.BENE_ACCOUNT_NO.name(), beneAccountNumber);
			}

			if (StringUtils.isNotBlank(payerAddress) && !payerAddress.equalsIgnoreCase("ALL")) {
				payerAddressQuery = new BasicDBObject(FieldType.PAYER_ADDRESS.name(), payerAddress);
			}

			userQuery = new BasicDBObject(FieldType.USER_TYPE.name(), "Merchant Initiated Direct");

			if (StringUtils.isNotBlank(mode) && !mode.equalsIgnoreCase("ALL")) {
				modeQuery = new BasicDBObject(FieldType.TXNTYPE.name(), mode);
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}

			if (!orderIdQuery.isEmpty()) {
				fianlList.add(orderIdQuery);
			}

			if (!beneAccountQuery.isEmpty()) {
				fianlList.add(beneAccountQuery);
			}

			if (!payerAddressQuery.isEmpty()) {
				fianlList.add(payerAddressQuery);
			}

			if (!userQuery.isEmpty()) {
				fianlList.add(userQuery);
			}

			if (!modeQuery.isEmpty()) {
				fianlList.add(modeQuery);
			}

			if (!dateQuery.isEmpty()) {
				fianlList.add(dateQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject match1 = new BasicDBObject("$match", finalquery);
			BasicDBObject sort1 = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match1, sort1);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
					payIdd.add(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
				} else {
					payIdd.add(dbobj.getString(FieldType.PAY_ID.getName()));
				}
			}
			cursor.close();
			User user1 = new User();
			for (String pyId : payIdd) {
				orderIdd.clear();
				BasicDBObject finalquery2 = new BasicDBObject();
				List<BasicDBObject> paramConditionLst1 = new ArrayList<BasicDBObject>();
				user1 = userDao.findPayId(pyId);

				if (StringUtils.isNotBlank(user1.getSuperMerchantId())) {
					paramConditionLst1.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), pyId));
				} else {

					paramConditionLst1.add(new BasicDBObject(FieldType.PAY_ID.getName(), pyId));
				}
				if (!dateQuery.isEmpty()) {
					paramConditionLst1.add(dateQuery);
				}
				if (!statusQuery.isEmpty()) {
					paramConditionLst1.add(statusQuery);
				}

				if (!orderIdQuery.isEmpty()) {
					paramConditionLst1.add(orderIdQuery);
				}

				if (!beneAccountQuery.isEmpty()) {
					paramConditionLst1.add(beneAccountQuery);
				}

				if (!payerAddressQuery.isEmpty()) {
					paramConditionLst1.add(payerAddressQuery);
				}

				if (!userQuery.isEmpty()) {
					paramConditionLst1.add(userQuery);
				}

				if (!modeQuery.isEmpty()) {
					paramConditionLst1.add(modeQuery);
				}
				if (!paramConditionLst1.isEmpty()) {
					finalquery2 = new BasicDBObject("$and", paramConditionLst1);
				}

				FindIterable<Document> iterDoc = coll.find(finalquery2);
				MongoCursor<Document> cursor1 = iterDoc.iterator();
				while (cursor1.hasNext()) {
					Document dbobj = cursor1.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

						dbobj = dataEncDecTool.decryptDocument(dbobj);
					}

					orderIdd.add(dbobj.getString(FieldType.ORDER_ID.getName()));
				}
				cursor1.close();

				for (String id : orderIdd) {
					List<BasicDBObject> paramConditionLst3 = new ArrayList<BasicDBObject>();
					if (StringUtils.isNotBlank(user1.getSuperMerchantId())) {
						paramConditionLst3.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), pyId));
					} else {
						paramConditionLst3.add(new BasicDBObject(FieldType.PAY_ID.getName(), pyId));
					}
					paramConditionLst3.add(new BasicDBObject(FieldType.ORDER_ID.getName(), id));

					if (!dateQuery.isEmpty()) {
						paramConditionLst3.add(dateQuery);
					}

					if (!modeQuery.isEmpty()) {
						paramConditionLst3.add(modeQuery);
					}
					BasicDBObject finalquery3 = new BasicDBObject();
					if (!paramConditionLst3.isEmpty()) {
						finalquery3 = new BasicDBObject("$and", paramConditionLst3);
					}

					BasicDBObject match = new BasicDBObject("$match", finalquery3);
					BasicDBObject sort = new BasicDBObject("$sort",
							new BasicDBObject(FieldType.CREATE_DATE.name(), -1));
					BasicDBObject limit = new BasicDBObject("$limit", 1);
					List<BasicDBObject> pipeline2 = Arrays.asList(match, sort, limit);
					AggregateIterable<Document> output2 = coll.aggregate(pipeline2);
					output2.allowDiskUse(true);
					MongoCursor<Document> cursor3 = output2.iterator();

					while (cursor3.hasNext()) {

						ImpsDownloadObject payoutReport = new ImpsDownloadObject();
						Document dbobj = cursor3.next();

						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

							dbobj = dataEncDecTool.decryptDocument(dbobj);
						}

						if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
							if (!(status.equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName())))) {
								break;
							}
						}

						User userr = new User();

						if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
							String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

							if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
								userr = userMap.get(payid);
							} else {
								userr = userDao.findPayId(payid);
								userMap.put(payid, user1);
							}
						}
						if (userr != null) {
							payoutReport.setMerchant(userr.getBusinessName());
						}

						if (((!merchantId.equalsIgnoreCase("All")) || (merchantId.equalsIgnoreCase("All"))
								|| (merchantId.equalsIgnoreCase("")))
								&& dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

							String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
							User subMerchantUser = new User();

							if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
								subMerchantUser = userMap.get(subMerchant);
							} else {
								subMerchantUser = userDao.findPayId(subMerchant);
								userMap.put(subMerchant, subMerchantUser);
							}
							if (subMerchantUser != null) {
								payoutReport.setSubMerchant(subMerchantUser.getBusinessName());
							} else {
								payoutReport.setSubMerchant(CrmFieldConstants.NA.getValue());
							}
						} else {
							payoutReport.setSubMerchant(CrmFieldConstants.NA.getValue());
						}

						if (dbobj.containsKey(FieldType.ACQ_ID.toString())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQ_ID.toString()))) {
								payoutReport.setImpsRefNum(dbobj.getString(FieldType.ACQ_ID.toString()));
							}
						} else {
							payoutReport.setImpsRefNum(CrmFieldConstants.NA.getValue());
						}

						if ((dbobj.getString(FieldType.TXNTYPE.toString())).equalsIgnoreCase("IMPS")) {
							if (dbobj.containsKey(FieldType.BANK_NAME.toString())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.BANK_NAME.toString()))) {
									payoutReport.setBankAccountName(dbobj.getString(FieldType.BANK_NAME.toString()));
								}
							} else {
								payoutReport.setBankAccountName(CrmFieldConstants.NA.getValue());
							}
						}

						if ((dbobj.getString(FieldType.TXNTYPE.toString())).equalsIgnoreCase("UPI")) {
							if (dbobj.containsKey(FieldType.PAYER_ADDRESS.toString())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYER_ADDRESS.toString()))) {
									payoutReport
											.setBankAccountName(dbobj.getString(FieldType.PAYER_ADDRESS.toString()));
								}
							} else {
								payoutReport.setBankAccountName(CrmFieldConstants.NA.getValue());
							}
						}

						if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.toString())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.toString()))) {
								payoutReport.setMerchantPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.toString()));
							}
						} else {
							payoutReport.setMerchantPayId(dbobj.getString(FieldType.PAY_ID.toString()));
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_ID.toString()))) {
							payoutReport.setTxnId(dbobj.getString(FieldType.TXN_ID.toString()));
						} else {
							payoutReport.setTxnId(CrmFieldConstants.NA.getValue());
						}

						if (dbobj.containsKey(FieldType.USER_TYPE.toString())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.USER_TYPE.toString()))) {
								payoutReport.setUserType(dbobj.getString(FieldType.USER_TYPE.toString()));
							} else {
								payoutReport.setUserType(CrmFieldConstants.NA.getValue());
							}
						}

						if (dbobj.containsKey(FieldType.RRN.toString())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.toString()))) {
								payoutReport.setRrn(dbobj.getString(FieldType.RRN.toString()));
							} else {
								payoutReport.setRrn(CrmFieldConstants.NA.getValue());
							}
						}
						payoutReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
						payoutReport.setDate(dbobj.getString(FieldType.CREATE_DATE.toString()));

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_ACCOUNT_NO.toString()))) {
							payoutReport.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.toString()));
						} else {
							payoutReport.setBankAccountNumber(CrmFieldConstants.NA.getValue());
						}

						if ((dbobj.getString(FieldType.TXNTYPE.toString())).equalsIgnoreCase("UPI")) {
							if (dbobj.containsKey(FieldType.PAYER_NAME.toString())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYER_NAME.toString()))) {
									payoutReport.setBeneAccountName(dbobj.getString(FieldType.PAYER_NAME.toString()));
								}
							} else {
								payoutReport.setBeneAccountName(CrmFieldConstants.NA.getValue());
							}
						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_NAME.getName()))) {
								payoutReport.setBeneAccountName(dbobj.getString(FieldType.BENE_NAME.getName()));
							} else {
								payoutReport.setBeneAccountName(CrmFieldConstants.NA.getValue());
							}
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.IFSC_CODE.toString()))) {
							payoutReport.setBankIFSC(dbobj.getString(FieldType.IFSC_CODE.toString()));
						} else {
							payoutReport.setBankIFSC(CrmFieldConstants.NA.getValue());
						}

						payoutReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
						payoutReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));

						if (dbobj.containsKey(FieldType.TXNTYPE.toString())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString()))) {
								payoutReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
							} else {
								payoutReport.setTxnType(CrmFieldConstants.NA.getValue());
							}
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TXN_MESSAGE.toString()))) {
							payoutReport.setResponseMsg(dbobj.getString(FieldType.PG_TXN_MESSAGE.toString()));
						} else {
							payoutReport.setResponseMsg(CrmFieldConstants.NA.getValue());
						}
						payoutReport.setPhoneNo(dbobj.getString(FieldType.PHONE_NO.toString()));
						payOutList.add(payoutReport);
					}
				}

			}

			logger.info("Total data in payOutList is " + payOutList.size());
			return payOutList;
		} catch (Exception e) {
			logger.error(
					"Exception occured in MerchantInitiatedDirectDao , merchantInitiatedDirectReportData(), Exception = ",
					e);
			return payOutList;
		}
	}

	@SuppressWarnings("static-access")
	public List<ImpsDownloadObject> merchantInitiatedDirectReportDataView(String dateFrom, String dateTo,
			String merchantId, String subMerchantPayId, String status, String orderId, String beneAccountNumber,
			String payerAddress, String rrn, String mode, User user, int start, int length, String acqName, String finalStatus) {

		logger.info("Inside MerchantInitiatedDirectDao, merchantInitiatedDirectReportData()");

		List<ImpsDownloadObject> payOutList = new ArrayList<ImpsDownloadObject>();
		Map<String, User> userMap = new HashMap<String, User>();
		Set<String> orderIdd = new HashSet<String>();
		Set<String> payIdd = new HashSet<String>();
		boolean isParameterised = false;

		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> statusConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject modeQuery = new BasicDBObject();
			BasicDBObject userQuery = new BasicDBObject();
			BasicDBObject orderIdQuery = new BasicDBObject();
			BasicDBObject beneAccountQuery = new BasicDBObject();
			BasicDBObject payerAddressQuery = new BasicDBObject();
			BasicDBObject rrnQuery = new BasicDBObject();
			BasicDBObject acquirerNameQuery = new BasicDBObject();
			BasicDBObject finalStatusQuery = new BasicDBObject();

			String currentDate = null;
			if (!dateFrom.isEmpty()) {
				if (!dateTo.isEmpty()) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));

				isParameterised = true;
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}

			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				statusQuery = new BasicDBObject(FieldType.STATUS.name(), status);
			}

			if (StringUtils.isNotBlank(acqName) && !acqName.equalsIgnoreCase("ALL")) {
				acquirerNameQuery = new BasicDBObject(FieldType.ACQUIRER_NAME.name(), acqName);
			}
			
			if (StringUtils.isNotBlank(finalStatus) && !finalStatus.equalsIgnoreCase("ALL")) {
				if(finalStatus.equalsIgnoreCase("true"))
					finalStatusQuery = new BasicDBObject(FieldType.IS_STATUS_FINAL.name(), "Y");
				else
					finalStatusQuery = new BasicDBObject(FieldType.IS_STATUS_FINAL.name(), new BasicDBObject("$ne","Y"));
			}

			if (StringUtils.isNotBlank(orderId) && !orderId.equalsIgnoreCase("ALL")) {
				orderIdQuery = new BasicDBObject(FieldType.ORDER_ID.name(), orderId);
			}

			if (StringUtils.isNotBlank(beneAccountNumber) && !beneAccountNumber.equalsIgnoreCase("ALL")) {
				beneAccountQuery = new BasicDBObject(FieldType.BENE_ACCOUNT_NO.name(), beneAccountNumber);
			}

			if (StringUtils.isNotBlank(payerAddress) && !payerAddress.equalsIgnoreCase("ALL")) {
				payerAddressQuery = new BasicDBObject(FieldType.PAYER_ADDRESS.name(), payerAddress);
			}

			if (StringUtils.isNotBlank(rrn) && !rrn.equalsIgnoreCase("ALL")) {
				rrnQuery = new BasicDBObject(FieldType.RRN.name(), rrn);
			}

			userQuery = new BasicDBObject(FieldType.USER_TYPE.name(), "Merchant Initiated Direct");

			if (StringUtils.isNotBlank(mode) && !mode.equalsIgnoreCase("ALL")) {
				modeQuery = new BasicDBObject(FieldType.TXNTYPE.name(), mode);
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}

			if (!(orderIdQuery.isEmpty()) || !(beneAccountQuery.isEmpty()) || !(payerAddressQuery.isEmpty())
					|| !(rrnQuery.isEmpty())) {
				if (!orderIdQuery.isEmpty()) {
					fianlList.add(orderIdQuery);
				}
				if (!beneAccountQuery.isEmpty()) {
					fianlList.add(beneAccountQuery);
				}
				if (!payerAddressQuery.isEmpty()) {
					fianlList.add(payerAddressQuery);
				}
				if (!rrnQuery.isEmpty()) {
					fianlList.add(rrnQuery);
				}
			} else {
				if (!dateQuery.isEmpty()) {
					fianlList.add(dateQuery);
				}
			}

			if (!userQuery.isEmpty()) {
				fianlList.add(userQuery);
			}

			if (!modeQuery.isEmpty()) {
				fianlList.add(modeQuery);
			}
			
			if (!finalStatusQuery.isEmpty()) {
				fianlList.add(finalStatusQuery);
			}
			
			if (!acquirerNameQuery.isEmpty()) {
				fianlList.add(acquirerNameQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			BasicDBObject skip = new BasicDBObject("$skip", start);
			BasicDBObject limit = new BasicDBObject("$limit", length);
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, skip, limit);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				ImpsDownloadObject payoutReport = new ImpsDownloadObject();
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
					if (!(status.equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName())))) {
						break;
					}
				}

				User userr = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						userr = userMap.get(payid);
					} else {
						userr = userDao.findPayId(payid);
						userMap.put(payid, userr);
					}
				}
				if (userr != null) {
					payoutReport.setMerchant(userr.getBusinessName());
				}

				if (((!merchantId.equalsIgnoreCase("All")) || (merchantId.equalsIgnoreCase("All"))
						|| (merchantId.equalsIgnoreCase(""))) && dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userDao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						payoutReport.setSubMerchant(subMerchantUser.getBusinessName());
					} else {
						payoutReport.setSubMerchant(CrmFieldConstants.NA.getValue());
					}
				}

				if (dbobj.containsKey(FieldType.ACQ_ID.toString())) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQ_ID.toString()))) {
						payoutReport.setImpsRefNum(dbobj.getString(FieldType.ACQ_ID.toString()));
					}
				} else {
					payoutReport.setImpsRefNum(CrmFieldConstants.NA.getValue());
				}

				if ((dbobj.getString(FieldType.TXNTYPE.toString())).equalsIgnoreCase("IMPS") 
						|| dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase("RTGS")
						|| dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase("NEFT")) {
					if (dbobj.containsKey(FieldType.BANK_NAME.toString())) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.BANK_NAME.toString()))) {
							payoutReport.setBankAccountName(dbobj.getString(FieldType.BANK_NAME.toString()));
						}
					} else {
						payoutReport.setBankAccountName(CrmFieldConstants.NA.getValue());
					}
				}

				if ((dbobj.getString(FieldType.TXNTYPE.toString())).equalsIgnoreCase("UPI")) {
					if (dbobj.containsKey(FieldType.PAYER_ADDRESS.toString())) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYER_ADDRESS.toString()))) {
							payoutReport.setBankAccountName(dbobj.getString(FieldType.PAYER_ADDRESS.toString()));
						}
					} else {
						payoutReport.setBankAccountName(CrmFieldConstants.NA.getValue());
					}
				}

				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.toString())) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.toString()))) {
						payoutReport.setMerchantPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.toString()));
					}
				} else {
					payoutReport.setMerchantPayId(dbobj.getString(FieldType.PAY_ID.toString()));
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_ID.toString()))) {
					payoutReport.setTxnId(dbobj.getString(FieldType.TXN_ID.toString()));
				} else {
					payoutReport.setTxnId(CrmFieldConstants.NA.getValue());
				}

				if (dbobj.containsKey(FieldType.USER_TYPE.toString())) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.USER_TYPE.toString()))) {
						payoutReport.setUserType(dbobj.getString(FieldType.USER_TYPE.toString()));
					} else {
						payoutReport.setUserType(CrmFieldConstants.NA.getValue());
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.toString()))) {
					payoutReport.setRrn(dbobj.getString(FieldType.RRN.toString()));
				} else {
					payoutReport.setRrn(CrmFieldConstants.NA.getValue());
				}

				payoutReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
				payoutReport.setDate(dbobj.getString(FieldType.CREATE_DATE.toString()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_ACCOUNT_NO.toString()))) {
					payoutReport.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.toString()));
				} else {
					payoutReport.setBankAccountNumber(CrmFieldConstants.NA.getValue());
				}

				if ((dbobj.getString(FieldType.TXNTYPE.toString())).equalsIgnoreCase("UPI")) {
					if (dbobj.containsKey(FieldType.PAYER_NAME.toString())) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYER_NAME.toString()))) {
							payoutReport.setBeneAccountName(dbobj.getString(FieldType.PAYER_NAME.toString()));
						}
					} else {
						payoutReport.setBeneAccountName(CrmFieldConstants.NA.getValue());
					}
				} else {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_NAME.getName()))) {
						payoutReport.setBeneAccountName(dbobj.getString(FieldType.BENE_NAME.getName()));
					} else {
						payoutReport.setBeneAccountName(CrmFieldConstants.NA.getValue());
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.IFSC_CODE.toString()))) {
					payoutReport.setBankIFSC(dbobj.getString(FieldType.IFSC_CODE.toString()));
				} else {
					payoutReport.setBankIFSC(CrmFieldConstants.NA.getValue());
				}

				payoutReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				payoutReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));

				if (dbobj.containsKey(FieldType.TXNTYPE.toString())) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString()))) {
						payoutReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					} else {
						payoutReport.setTxnType(CrmFieldConstants.NA.getValue());
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TXN_MESSAGE.toString()))) {
					payoutReport.setResponseMsg(dbobj.getString(FieldType.PG_TXN_MESSAGE.toString()));
				} else {
					payoutReport.setResponseMsg(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.IS_STATUS_FINAL.toString()))) {
					payoutReport.setFinalStatus(dbobj.getString(FieldType.IS_STATUS_FINAL.toString()));
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PURPOSE.toString()))) {
					payoutReport.setPurpose(dbobj.getString(FieldType.PURPOSE.toString()));
				} else {
					payoutReport.setPurpose(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_NAME.toString()))) {
					payoutReport.setAcquirerName(dbobj.getString(FieldType.ACQUIRER_NAME.toString()));
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.COMMISSION_AMOUNT.toString()))) {
					payoutReport.setCommissionAmount(dbobj.getString(FieldType.COMMISSION_AMOUNT.toString()));
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SERVICE_TAX.toString()))) {
					payoutReport.setServiceTax(dbobj.getString(FieldType.SERVICE_TAX.toString()));
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.VIRTUAL_AC_CODE.toString()))) {
					payoutReport.setVirtualAccount(dbobj.getString(FieldType.VIRTUAL_AC_CODE.toString()));
				}
				payoutReport.setPhoneNo(dbobj.getString(FieldType.PHONE_NO.toString()));
				payOutList.add(payoutReport);
			}

			logger.info("Total data in payOutList is " + payOutList.size());
			return payOutList;
		} catch (Exception e) {
			logger.error(
					"Exception occured in MerchantInitiatedDirectDao , merchantInitiatedDirectReportData(), Exception = ",
					e);
			return payOutList;
		}
	}

	@SuppressWarnings("static-access")
	public List<ImpsDownloadObject> merchantInitiatedDirectReportDataDownload(String dateFrom, String dateTo,
			String merchantId, String subMerchantPayId, String status, String orderId, String beneAccountNumber,
			String payerAddress, String rrn, String mode, User user,String finalStatus) {

		logger.info("Inside MerchantInitiatedDirectDao, merchantInitiatedDirectReportDataDownload()");

		List<ImpsDownloadObject> payOutList = new ArrayList<ImpsDownloadObject>();
		Map<String, User> userMap = new HashMap<String, User>();
		Set<String> orderIdd = new HashSet<String>();
		Set<String> payIdd = new HashSet<String>();
		boolean isParameterised = false;

		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> statusConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject modeQuery = new BasicDBObject();
			BasicDBObject userQuery = new BasicDBObject();
			BasicDBObject orderIdQuery = new BasicDBObject();
			BasicDBObject beneAccountQuery = new BasicDBObject();
			BasicDBObject payerAddressQuery = new BasicDBObject();
			BasicDBObject rrnQuery = new BasicDBObject();
			BasicDBObject finalStatusQuery = new BasicDBObject();

			String currentDate = null;
			if (!dateFrom.isEmpty()) {
				if (!dateTo.isEmpty()) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));

				isParameterised = true;
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}

			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				statusQuery = new BasicDBObject(FieldType.STATUS.name(), status);
			}

			if (StringUtils.isNotBlank(orderId) && !orderId.equalsIgnoreCase("ALL")) {
				orderIdQuery = new BasicDBObject(FieldType.ORDER_ID.name(), orderId);
			}

			if (StringUtils.isNotBlank(beneAccountNumber) && !beneAccountNumber.equalsIgnoreCase("ALL")) {
				beneAccountQuery = new BasicDBObject(FieldType.BENE_ACCOUNT_NO.name(), beneAccountNumber);
			}

			if (StringUtils.isNotBlank(payerAddress) && !payerAddress.equalsIgnoreCase("ALL")) {
				payerAddressQuery = new BasicDBObject(FieldType.PAYER_ADDRESS.name(), payerAddress);
			}

			if (StringUtils.isNotBlank(rrn) && !rrn.equalsIgnoreCase("ALL")) {
				rrnQuery = new BasicDBObject(FieldType.RRN.name(), rrn);
			}

			userQuery = new BasicDBObject(FieldType.USER_TYPE.name(), "Merchant Initiated Direct");

			if (StringUtils.isNotBlank(mode) && !mode.equalsIgnoreCase("ALL")) {
				modeQuery = new BasicDBObject(FieldType.TXNTYPE.name(), mode);
			}
			if (StringUtils.isNotBlank(finalStatus) && !finalStatus.equalsIgnoreCase("ALL")) {
				if(finalStatus.equalsIgnoreCase("true"))
					finalStatusQuery = new BasicDBObject(FieldType.IS_STATUS_FINAL.name(), "Y");
				else
					finalStatusQuery = new BasicDBObject(FieldType.IS_STATUS_FINAL.name(), new BasicDBObject("$ne","Y"));
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}
			
			

			if (!(orderIdQuery.isEmpty()) || !(beneAccountQuery.isEmpty()) || !(payerAddressQuery.isEmpty())
					|| !(rrnQuery.isEmpty())) {
				if (!orderIdQuery.isEmpty()) {
					fianlList.add(orderIdQuery);
				}
				if (!beneAccountQuery.isEmpty()) {
					fianlList.add(beneAccountQuery);
				}
				if (!payerAddressQuery.isEmpty()) {
					fianlList.add(payerAddressQuery);
				}
				if (!rrnQuery.isEmpty()) {
					fianlList.add(rrnQuery);
				}
			} else {
				if (!dateQuery.isEmpty()) {
					fianlList.add(dateQuery);
				}
			}

			if (!userQuery.isEmpty()) {
				fianlList.add(userQuery);
			}

			if (!modeQuery.isEmpty()) {
				fianlList.add(modeQuery);
			}
			
			if (!finalStatusQuery.isEmpty()) {
				fianlList.add(finalStatusQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				ImpsDownloadObject payoutReport = new ImpsDownloadObject();
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
					if (!(status.equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName())))) {
						break;
					}
				}

				User userr = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						userr = userMap.get(payid);
					} else {
						userr = userDao.findPayId(payid);
						userMap.put(payid, userr);
					}
				}
				if (userr != null) {
					payoutReport.setMerchant(userr.getBusinessName());
				}

				if (((!merchantId.equalsIgnoreCase("All")) || (merchantId.equalsIgnoreCase("All"))
						|| (merchantId.equalsIgnoreCase(""))) && dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userDao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						payoutReport.setSubMerchant(subMerchantUser.getBusinessName());
					} else {
						payoutReport.setSubMerchant(CrmFieldConstants.NA.getValue());
					}
				} else {
					payoutReport.setSubMerchant(CrmFieldConstants.NA.getValue());
				}

				if (dbobj.containsKey(FieldType.ACQ_ID.toString())) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQ_ID.toString()))) {
						payoutReport.setImpsRefNum(dbobj.getString(FieldType.ACQ_ID.toString()));
					}
				} else {
					payoutReport.setImpsRefNum(CrmFieldConstants.NA.getValue());
				}

				if ((dbobj.getString(FieldType.TXNTYPE.toString())).equalsIgnoreCase("IMPS")) {
					if (dbobj.containsKey(FieldType.BANK_NAME.toString())) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.BANK_NAME.toString()))) {
							payoutReport.setBankAccountName(dbobj.getString(FieldType.BANK_NAME.toString()));
						}
					} else {
						payoutReport.setBankAccountName(CrmFieldConstants.NA.getValue());
					}
				}

				if ((dbobj.getString(FieldType.TXNTYPE.toString())).equalsIgnoreCase("UPI")) {
					if (dbobj.containsKey(FieldType.PAYER_ADDRESS.toString())) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYER_ADDRESS.toString()))) {
							payoutReport.setBankAccountName(dbobj.getString(FieldType.PAYER_ADDRESS.toString()));
						}
					} else {
						payoutReport.setBankAccountName(CrmFieldConstants.NA.getValue());
					}
				}

				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.toString())) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.toString()))) {
						payoutReport.setMerchantPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.toString()));
					}
				} else {
					payoutReport.setMerchantPayId(dbobj.getString(FieldType.PAY_ID.toString()));
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_ID.toString()))) {
					payoutReport.setTxnId(dbobj.getString(FieldType.TXN_ID.toString()));
				} else {
					payoutReport.setTxnId(CrmFieldConstants.NA.getValue());
				}

				if (dbobj.containsKey(FieldType.USER_TYPE.toString())) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.USER_TYPE.toString()))) {
						payoutReport.setUserType(dbobj.getString(FieldType.USER_TYPE.toString()));
					} else {
						payoutReport.setUserType(CrmFieldConstants.NA.getValue());
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.toString()))) {
					payoutReport.setRrn(dbobj.getString(FieldType.RRN.toString()));
				} else {
					payoutReport.setRrn(CrmFieldConstants.NA.getValue());
				}

				payoutReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
				payoutReport.setDate(dbobj.getString(FieldType.CREATE_DATE.toString()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_ACCOUNT_NO.toString()))) {
					payoutReport.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.toString()));
				} else {
					payoutReport.setBankAccountNumber(CrmFieldConstants.NA.getValue());
				}

				if ((dbobj.getString(FieldType.TXNTYPE.toString())).equalsIgnoreCase("UPI")) {
					if (dbobj.containsKey(FieldType.PAYER_NAME.toString())) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYER_NAME.toString()))) {
							payoutReport.setBeneAccountName(dbobj.getString(FieldType.PAYER_NAME.toString()));
						}
					} else {
						payoutReport.setBeneAccountName(CrmFieldConstants.NA.getValue());
					}
				} else {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_NAME.getName()))) {
						payoutReport.setBeneAccountName(dbobj.getString(FieldType.BENE_NAME.getName()));
					} else {
						payoutReport.setBeneAccountName(CrmFieldConstants.NA.getValue());
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.IFSC_CODE.toString()))) {
					payoutReport.setBankIFSC(dbobj.getString(FieldType.IFSC_CODE.toString()));
				} else {
					payoutReport.setBankIFSC(CrmFieldConstants.NA.getValue());
				}

				payoutReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				payoutReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));

				if (dbobj.containsKey(FieldType.TXNTYPE.toString())) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString()))) {
						payoutReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					} else {
						payoutReport.setTxnType(CrmFieldConstants.NA.getValue());
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TXN_MESSAGE.toString()))) {
					payoutReport.setResponseMsg(dbobj.getString(FieldType.PG_TXN_MESSAGE.toString()));
				} else {
					payoutReport.setResponseMsg(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PURPOSE.toString()))) {
					payoutReport.setPurpose(dbobj.getString(FieldType.PURPOSE.toString()));
				} else {
					payoutReport.setPurpose(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.COMMISSION_AMOUNT.toString()))) {
					payoutReport.setCommissionAmount(dbobj.getString(FieldType.COMMISSION_AMOUNT.toString()));
				} else {
					payoutReport.setCommissionAmount(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SERVICE_TAX.toString()))) {
				} else {
					payoutReport.setServiceTax(dbobj.getString(FieldType.SERVICE_TAX.toString()));
					payoutReport.setServiceTax(CrmFieldConstants.NA.getValue());
				}
				payoutReport.setPhoneNo(dbobj.getString(FieldType.PHONE_NO.toString()));
				payOutList.add(payoutReport);
			}

			logger.info("Total data in payOutList is " + payOutList.size());
			return payOutList;
		} catch (Exception e) {
			logger.error(
					"Exception occured in MerchantInitiatedDirectDao , merchantInitiatedDirectReportDataDownload(), Exception = ",
					e);
			return payOutList;
		}
	}

	@SuppressWarnings("static-access")
	public int merchantInitiatedDirectReportCount(String dateFrom, String dateTo, String merchantId,
			String subMerchantPayId, String status, String orderId, String beneAccountNumber, String payerAddress,
			String rrn, String mode, User user, String acquirerName, String finalStatus) {

		logger.info("Inside MerchantInitiatedDirectDao, merchantInitiatedDirectReportCount()");

		List<ImpsDownloadObject> payOutList = new ArrayList<ImpsDownloadObject>();
		Map<String, User> userMap = new HashMap<String, User>();
		Set<String> orderIdd = new HashSet<String>();
		Set<String> payIdd = new HashSet<String>();
		boolean isParameterised = false;

		try {
			int total = 0;
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> statusConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject modeQuery = new BasicDBObject();
			BasicDBObject userQuery = new BasicDBObject();
			BasicDBObject orderIdQuery = new BasicDBObject();
			BasicDBObject beneAccountQuery = new BasicDBObject();
			BasicDBObject payerAddressQuery = new BasicDBObject();
			BasicDBObject rrnQuery = new BasicDBObject();
			BasicDBObject acquirerNameQuery = new BasicDBObject();
			BasicDBObject finalStatusQuery = new BasicDBObject();

			String currentDate = null;
			if (!dateFrom.isEmpty()) {
				if (!dateTo.isEmpty()) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));

				isParameterised = true;
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}

			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				statusQuery = new BasicDBObject(FieldType.STATUS.name(), status);
			}

			if (StringUtils.isNotBlank(acquirerName) && !status.equalsIgnoreCase("ALL")) {
				acquirerNameQuery = new BasicDBObject(FieldType.ACQUIRER_NAME.name(), acquirerName);
			}

			if (StringUtils.isNotBlank(orderId) && !orderId.equalsIgnoreCase("ALL")) {
				orderIdQuery = new BasicDBObject(FieldType.ORDER_ID.name(), orderId);
			}

			if (StringUtils.isNotBlank(beneAccountNumber) && !beneAccountNumber.equalsIgnoreCase("ALL")) {
				beneAccountQuery = new BasicDBObject(FieldType.BENE_ACCOUNT_NO.name(), beneAccountNumber);
			}

			if (StringUtils.isNotBlank(payerAddress) && !payerAddress.equalsIgnoreCase("ALL")) {
				payerAddressQuery = new BasicDBObject(FieldType.PAYER_ADDRESS.name(), payerAddress);
			}

			if (StringUtils.isNotBlank(rrn) && !rrn.equalsIgnoreCase("ALL")) {
				rrnQuery = new BasicDBObject(FieldType.RRN.name(), rrn);
			}

			userQuery = new BasicDBObject(FieldType.USER_TYPE.name(), "Merchant Initiated Direct");

			if (StringUtils.isNotBlank(mode) && !mode.equalsIgnoreCase("ALL")) {
				modeQuery = new BasicDBObject(FieldType.TXNTYPE.name(), mode);
			}
			
			if (StringUtils.isNotBlank(finalStatus) && !finalStatus.equalsIgnoreCase("ALL")) {
				if(finalStatus.equalsIgnoreCase("true"))
					finalStatusQuery = new BasicDBObject(FieldType.IS_STATUS_FINAL.name(), "Y");
				else
					finalStatusQuery = new BasicDBObject(FieldType.IS_STATUS_FINAL.name(), new BasicDBObject("$ne","Y"));
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}

			if (!(orderIdQuery.isEmpty()) || !(beneAccountQuery.isEmpty()) || !(payerAddressQuery.isEmpty())
					|| !(rrnQuery.isEmpty())) {
				if (!orderIdQuery.isEmpty()) {
					fianlList.add(orderIdQuery);
				}
				if (!beneAccountQuery.isEmpty()) {
					fianlList.add(beneAccountQuery);
				}
				if (!payerAddressQuery.isEmpty()) {
					fianlList.add(payerAddressQuery);
				}

				if (!rrnQuery.isEmpty()) {
					fianlList.add(rrnQuery);
				}
			} else {
				if (!dateQuery.isEmpty()) {
					fianlList.add(dateQuery);
				}
			}

			if (!userQuery.isEmpty()) {
				fianlList.add(userQuery);
			}

			if (!modeQuery.isEmpty()) {
				fianlList.add(modeQuery);
			}
			
			if (!finalStatusQuery.isEmpty()) {
				fianlList.add(finalStatusQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));

			total = (int) coll.count(finalquery);
			logger.info("Inside CustomerQRReportCount , total records from DB  = " + total);
			return total;
		} catch (Exception e) {
			logger.error(
					"Exception occured in MerchantInitiatedDirectDao , merchantInitiatedDirectReportCount(), Exception = ",
					e);
			return 0;
		}
	}

	public Map<String, String> getMerchantInitiatedTransactionWithTxnId(String txnId) {

		Map<String, String> midList = new HashMap<>();

		try {
			boolean isParameterised = false;

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));

			BasicDBObject finalQuery = new BasicDBObject();

			finalQuery.put(FieldType.TXN_ID.getName(), txnId);

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				midList.put(FieldType.PAY_ID.getName(), dbobj.getString(FieldType.PAY_ID.getName()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
					midList.put(FieldType.PAY_ID.getName(), dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
				} else {
					midList.put(FieldType.PAY_ID.getName(), dbobj.getString(FieldType.PAY_ID.getName()));
				}

				if ((dbobj.getString(FieldType.TXNTYPE.toString())).equalsIgnoreCase("IMPS")) {
					if (dbobj.containsKey(FieldType.BENE_NAME.toString())) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_NAME.toString()))) {
							midList.put(FieldType.BENE_NAME.getName(), dbobj.getString(FieldType.BENE_NAME.getName()));
						}
					}
				}

				if ((dbobj.getString(FieldType.TXNTYPE.toString())).equalsIgnoreCase("UPI")) {
					if (dbobj.containsKey(FieldType.PAYER_NAME.toString())) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYER_NAME.toString()))) {
							midList.put(FieldType.PAYER_NAME.getName(),
									dbobj.getString(FieldType.PAYER_NAME.getName()));
						}
					}
				}

				if ((dbobj.getString(FieldType.TXNTYPE.toString())).equalsIgnoreCase("IMPS")) {
					if (dbobj.containsKey(FieldType.BANK_NAME.toString())) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.BANK_NAME.toString()))) {
							midList.put(FieldType.BANK_NAME.getName(), dbobj.getString(FieldType.BANK_NAME.getName()));
						}
					}
				}
				if ((dbobj.getString(FieldType.TXNTYPE.toString())).equalsIgnoreCase("UPI")) {
					if (dbobj.containsKey(FieldType.PAYER_ADDRESS.toString())) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYER_ADDRESS.toString()))) {
							midList.put(FieldType.PAYER_ADDRESS.getName(),
									dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
						}
					}
				}

				midList.put(FieldType.ORDER_ID.getName(), dbobj.getString(FieldType.ORDER_ID.getName()));
				midList.put(FieldType.TXN_ID.getName(), dbobj.getString(FieldType.TXN_ID.getName()));
				midList.put(FieldType.BENE_ACCOUNT_NO.getName(), dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()));
				midList.put(FieldType.REMARKS.getName(), dbobj.getString(FieldType.REMARKS.getName()));
				midList.put(FieldType.IFSC_CODE.getName(), dbobj.getString(FieldType.IFSC_CODE.getName()));
				midList.put(FieldType.CREATE_DATE.getName(), dbobj.getString(FieldType.CREATE_DATE.getName()));
				midList.put(FieldType.RRN.getName(), dbobj.getString(FieldType.RRN.getName()));
				midList.put(FieldType.STATUS.getName(), dbobj.getString(FieldType.STATUS.getName()));
				midList.put(FieldType.PG_TXN_MESSAGE.getName(), dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));

				midList.put(FieldType.PHONE_NO.getName(), dbobj.getString(FieldType.PHONE_NO.getName()));
				midList.put(FieldType.TXNTYPE.getName(), dbobj.getString(FieldType.TXNTYPE.getName()));
				midList.put(FieldType.AMOUNT.getName(),
						Amount.formatAmount(dbobj.getString(FieldType.AMOUNT.getName()), "356"));
				midList.put(FieldType.USER_TYPE.getName(), dbobj.getString(FieldType.USER_TYPE.getName()));
				midList.put(FieldType.VIRTUAL_AC_CODE.getName(), dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()));
				midList.put(FieldType.ACQUIRER_NAME.getName(), dbobj.getString(FieldType.ACQUIRER_NAME.getName()));

			}
			return midList;

		} catch (Exception e) {
			logger.error("Exception : ", e);

		}
		return midList;

	}

	@SuppressWarnings("static-access")
	public List<ImpsDownloadObject> viewLedgerReportData(String dateFrom, String dateTo, String merchantId,
			String subMerchantPayId, User user, boolean downloadFlag, String acquirerName) {
		logger.info("Inside viewLedgerReportData()");
		Map<String, User> userMap = new HashMap<String, User>();
		List<ImpsDownloadObject> transactionList = new ArrayList<ImpsDownloadObject>();

		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> vaConditionList = new ArrayList<BasicDBObject>();
			Set<String> dateList = new HashSet<String>();
			Set<String> virtualList = new HashSet<String>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject userQuery = new BasicDBObject();
			BasicDBObject vaQuery = new BasicDBObject();
			BasicDBObject acqNameQuery = new BasicDBObject();
			String currentDate = null;
			if (StringUtils.isNotBlank(dateFrom)) {
				if (StringUtils.isNotBlank(dateTo)) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
			User user2 = null;

			if (StringUtils.isNotBlank(acquirerName))
				acqNameQuery.put(FieldType.ACQUIRER_NAME.getName(), acquirerName);

			if (StringUtils.isNotBlank(subMerchantPayId)) {

				if (!subMerchantPayId.equalsIgnoreCase("ALL")) {
					user2 = userDao.findPayId(subMerchantPayId);
					if (StringUtils.isNotBlank(user2.getVirtualAccountNo())) {
						paramConditionLst.add(
								new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(), user2.getVirtualAccountNo()));
					}
				} else {
					List<Merchants> subMerchantList = userDao.getSubMerchantListBySuperPayId(merchantId);
					for (Merchants subMerchant : subMerchantList) {

						vaConditionList.add((new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(),
								subMerchant.getVirtualAccountNo())));
					}

					vaQuery.append("$or", vaConditionList);
				}
			} else {

				if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
					user2 = userDao.findPayId(merchantId);

					if (user2.isSuperMerchant() == true && StringUtils.isNotBlank(user2.getSuperMerchantId())) {
						List<Merchants> subMerchantList = userDao.getSubMerchantListBySuperPayId(merchantId);
						for (Merchants subMerchant : subMerchantList) {

							vaConditionList.add((new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(),
									subMerchant.getVirtualAccountNo())));
						}

						vaQuery.append("$or", vaConditionList);
					} else {

						if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {

							paramConditionLst.add(new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(),
									user2.getVirtualAccountNo()));

						}
					}
				}
			}

			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			userQuery = new BasicDBObject(FieldType.USER_TYPE.name(), "Merchant Initiated Direct");

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!dateQuery.isEmpty()) {
				fianlList.add(dateQuery);
			}

			if (!vaQuery.isEmpty()) {
				fianlList.add(vaQuery);
			}
			if (!acqNameQuery.isEmpty()) {
				fianlList.add(acqNameQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			logger.info("Inside TxnReports , downloadSearchPayment , finalquery = " + finalquery);

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("UPDATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				// String payoutAcqName=
				// payoutAcquirerMappingDao.findPayoutAcquirerNameByPayId(dbobj.getString(FieldType.PAY_ID.getName()),dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));

				SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String dtDate = dateFormat.format(inputDate.parse(dbobj.getString(FieldType.CREATE_DATE.getName())));

				ImpsDownloadObject impsReport = new ImpsDownloadObject();
				if (dbobj.containsKey(FieldType.OPENING_AMOUNT.getName())) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.OPENING_AMOUNT.getName()))) {
						impsReport.setOpeningBalance(dbobj.getString(FieldType.OPENING_AMOUNT.getName()));
					}
				}

				if (dbobj.containsKey(FieldType.AMOUNT.getName())) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.AMOUNT.getName()))) {
						impsReport.setClosingBalance(dbobj.getString(FieldType.AMOUNT.getName()));
					}
				}

				if (dbobj.containsKey(FieldType.CREDIT_AMOUNT.getName())) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CREDIT_AMOUNT.getName()))) {
						impsReport.setTotalCredit(dbobj.getString(FieldType.CREDIT_AMOUNT.getName()));
					}
				}

				if (dbobj.containsKey(FieldType.DEBIT_AMOUNT.getName())) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.DEBIT_AMOUNT.getName()))) {
						impsReport.setTotalDebit(dbobj.getString(FieldType.DEBIT_AMOUNT.getName()));
					}
				}

				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						impsReport.setMerchant(
								userDao.getBusinessNameByPayId(dbobj.getString(FieldType.PAY_ID.getName())));
						impsReport.setMerchantPayId(dbobj.getString(FieldType.PAY_ID.getName()));
						impsReport.setSubMerchant(
								userDao.getBusinessNameByPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName())));
						impsReport.setSubMerchantPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
					}
				} else {

					if (merchantId.equalsIgnoreCase("ALL")) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAY_ID.getName()))) {
							impsReport.setMerchant(
									userDao.getBusinessNameByPayId(dbobj.getString(FieldType.PAY_ID.getName())));
							impsReport.setMerchantPayId(dbobj.getString(FieldType.PAY_ID.getName()));
							if (downloadFlag == true) {
								impsReport.setSubMerchant(CrmFieldConstants.NA.getValue());
							}
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAY_ID.getName()))) {
							impsReport.setMerchant(
									userDao.getBusinessNameByPayId(dbobj.getString(FieldType.PAY_ID.getName())));
							impsReport.setMerchantPayId(dbobj.getString(FieldType.PAY_ID.getName()));
						}
					}
				}

				if (dbobj.containsKey(FieldType.ACQUIRER_NAME.getName())) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_NAME.getName()))) {
						impsReport.setAcquirerName(dbobj.getString(FieldType.ACQUIRER_NAME.getName()));
					}
				}

				impsReport.setDate(dtDate);

				transactionList.add(impsReport);
			}
			return transactionList;

		} catch (Exception e) {
			logger.error("Exception ", e);
		}

		return transactionList;
	}

	@SuppressWarnings("static-access")
	public Map<String, String> getCurrentBalanceData(String dateFrom, String dateTo, String merchantId,
			String subMerchantPayId, User user) {

		Map<String, String> amount = new HashMap<>();

		BigDecimal closingTransaction = getClosingTransactionAmountForLedger(dateFrom, dateTo, merchantId,
				subMerchantPayId, user);

		amount.put("checkAmount", closingTransaction.toString());

		return amount;

	}

	public void addTopup(String payId, String subMerchantpayId, String amount, User sessionUser, String remarks) {
		logger.info("Inside addTopup() ,MerchantInitiatedDirectDao");
		try {
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			DateFormat dateFormatIndex = new SimpleDateFormat("yyyyMMdd");
			String todaysDateIndex = dateFormatIndex.format(dNow.getTime());
			User user;
			if (StringUtils.isNotBlank(subMerchantpayId)) {
				user = userDao.findPayId(subMerchantpayId);
			} else {
				user = userDao.findPayId(payId);
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String todayDate = sdf.format(new Date());
			todayDate = todayDate + " " + "00:00:00";
			List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
			dbObjList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			dbObjList.add(new BasicDBObject(FieldType.DATE_INDEX.getName(), todaysDateIndex));
			if (StringUtils.isNotBlank(subMerchantpayId)) {
				dbObjList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantpayId));
			}
			// ADD ACQ_NAME in filter

			String payoutAcqName = payoutAcquirerMappingDao.findPayoutAcquirerNameByPayId(payId, subMerchantpayId);

			if (StringUtils.isNotBlank(payoutAcqName)) {
				dbObjList.add(new BasicDBObject(FieldType.ACQUIRER_NAME.getName(), payoutAcqName));
			}

			BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
			MongoCursor<Document> cursor = coll.find(andQuery).iterator();
			if (cursor.hasNext()) {
				Document documentObj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				logger.info("Orginal data for closing collection by Top for virtual account code = "
						+ documentObj.get("VIRTUAL_AC_CODE").toString() + " , " + documentObj);
				BigDecimal dbAmount = new BigDecimal(documentObj.getString("AMOUNT"));
				dbAmount = dbAmount.add(new BigDecimal(amount));
				BigDecimal dbCreditAmount = new BigDecimal(documentObj.getString("CREDIT_AMOUNT"));
				dbCreditAmount = dbCreditAmount.add(new BigDecimal(amount));
				BasicDBObject setData = new BasicDBObject();
				setData.put(FieldType.AMOUNT.getName(), dbAmount.toString());
				setData.put(FieldType.CREDIT_AMOUNT.getName(), dbCreditAmount.toString());
				setData.put(FieldType.UPDATE_DATE.getName(), dateNow);
				Bson updateOperationDocument = new Document("$set", setData);
				coll.updateOne(andQuery, updateOperationDocument);
				logger.info("Updated data for closing collection by Top for virtual account code = "
						+ documentObj.get("VIRTUAL_AC_CODE").toString() + " , " + setData);
			} else {
				BasicDBObject newFieldsObj = new BasicDBObject();
				BigDecimal amt = new BigDecimal(amount).setScale(2);
				newFieldsObj.put(FieldType.AMOUNT.getName(), String.valueOf(amt));
				newFieldsObj.put(FieldType.CREATE_DATE.getName(), dateNow);
				newFieldsObj.put(FieldType.UPDATE_DATE.getName(), dateNow);
				newFieldsObj.put(FieldType.CLOSING_DATE.getName(), todayDate);
				newFieldsObj.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo());
				newFieldsObj.put(FieldType.PAY_ID.getName(), payId);

				if (StringUtils.isNotBlank(subMerchantpayId)) {
					newFieldsObj.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantpayId);
				}
				if (StringUtils.isNotBlank(user.getResellerId())) {
					newFieldsObj.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
				}
				newFieldsObj.put(FieldType.DEBIT_AMOUNT.getName(), "0.00");
				newFieldsObj.put(FieldType.CREDIT_AMOUNT.getName(), String.valueOf(amt));
				newFieldsObj.put(FieldType.OPENING_AMOUNT.getName(), "0.00");
				newFieldsObj.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));

				newFieldsObj.put(FieldType.ACQUIRER_NAME.getName(), payoutAcqName);
				Document doc = new Document(newFieldsObj);
				coll.insertOne(doc);
				logger.info("Insert data in closing collection by Top for virtual account code = "
						+ newFieldsObj.get("VIRTUAL_AC_CODE").toString() + " , " + newFieldsObj);
			}
			cursor.close();

			MongoCollection<Document> collEcollection = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.E_COLLECTION.getValue()));
			BigDecimal amt = new BigDecimal(amount).setScale(2);
			Document doc = new Document();
			doc.put("_id", TransactionManager.getNewTransactionId());
			String txnId = TransactionManager.getNewTransactionId();
			doc.put(FieldType.TXN_ID.getName(), txnId);
			doc.put(FieldType.PG_REF_NUM.getName(), txnId);
			doc.put(FieldType.OID.getName(), txnId);
			doc.put(FieldType.ORIG_TXN_ID.getName(), txnId);
			doc.put(FieldType.CREATE_DATE.getName(), dateNow);
			doc.put(FieldType.UPDATE_DATE.getName(), dateNow);
			doc.put(FieldType.DATE_INDEX.getName(), todaysDateIndex);
			doc.put(FieldType.ORDER_ID.getName(), "LP" + txnId);
			doc.put(FieldType.PAY_ID.getName(), payId);
			if (StringUtils.isNotBlank(subMerchantpayId)) {
				doc.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantpayId);
			}
			doc.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo());
			if (StringUtils.isNotBlank(user.getResellerId())) {
				doc.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
			}
			doc.put(FieldType.TXNTYPE.getName(), "Topup");
			doc.put(FieldType.ORIG_TXNTYPE.getName(), "Topup");
			doc.put(FieldType.PAYMENT_TYPE.getName(), "Topup");
			doc.put(FieldType.MOP_TYPE.getName(), "Topup");
			doc.put(FieldType.CURRENCY_CODE.getName(), "356");
			doc.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			doc.put(FieldType.AMOUNT.getName(), String.valueOf(amt));
			doc.put(FieldType.TOTAL_AMOUNT.getName(), String.valueOf(amt));
			doc.put(FieldType.RESPONSE_CODE.getName(), "000");
			doc.put(FieldType.RESPONSE_MESSAGE.getName(), "SUCCESS");
			if (StringUtils.isNotBlank(remarks)) {
				doc.put(FieldType.SENDER_REMARK.getName(), remarks);
			}

			doc.put(FieldType.ACQUIRER_NAME.getName(), payoutAcqName);
			collEcollection.insertOne(doc);
			logger.info("Insert data in Ecollection collection by Top for virtual account code = "
					+ doc.get("VIRTUAL_AC_CODE").toString() + " , " + doc);
		} catch (

		Exception e) {
			logger.error("Exception occured in topUp : ", e);
		}

	}

	public BigDecimal getECollectionTransactionAmountForLedger(String dateFrom, String dateTo, String merchantId,
			String subMerchantPayId, User user) {
		logger.info("Inside getECollectionTransactionAmountForLedger()");
		Map<String, User> userMap = new HashMap<String, User>();
		BigDecimal totalEcollectionAmount = new BigDecimal("0").setScale(2);
		try {

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BigDecimal closingAmount = new BigDecimal("0").setScale(2);
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject userQuery = new BasicDBObject();
			BigDecimal settledAmount = new BigDecimal("0").setScale(2);
			String currentDate = null;
			if (StringUtils.isNotBlank(dateFrom)) {
				if (StringUtils.isNotBlank(dateTo)) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String todayDate = sdf.format(new Date());

				dateFrom = todayDate + " " + "00:00:00";
				dateTo = todayDate + " " + "23:59:59";
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.E_COLLECTION.getValue()));
			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));

			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));

			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			userQuery = new BasicDBObject(FieldType.USER_TYPE.name(), "Merchant Initiated Direct");

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!dateQuery.isEmpty()) {
				fianlList.add(dateQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside TxnReports , downloadSearchPayment , finalquery = " + finalquery);

			finalquery.append(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());

			MongoCursor<Document> cursor = coll.find(finalquery).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				settledAmount = settledAmount.add(dbAmount);
			}
			totalEcollectionAmount = settledAmount;
			logger.info("Total today's Transactions Amount  " + totalEcollectionAmount);

		} catch (Exception e) {
			logger.error("Exception ", e);
		}

		return totalEcollectionAmount;
	}

	public BigDecimal getClosingTransactionAmountForLedger(String dateFrom, String dateTo, String merchantId,
			String subMerchantPayId, User user) {
		logger.info("Inside getClosingTransactionAmountForLedger()");
		Map<String, User> userMap = new HashMap<String, User>();
		BigDecimal totalClosingAmount = new BigDecimal("0").setScale(2);
		try {

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> vaConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject userQuery = new BasicDBObject();
			BasicDBObject vaQuery = new BasicDBObject();
			BasicDBObject acqNameQuery = new BasicDBObject();
			BigDecimal settledAmount = new BigDecimal("0").setScale(2);
			String currentDate = null;
			if (StringUtils.isNotBlank(dateFrom)) {
				if (StringUtils.isNotBlank(dateTo)) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String todayDate = sdf.format(new Date());

				dateFrom = todayDate + " " + "00:00:00";
				dateTo = todayDate + " " + "23:59:59";
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
			}

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			if (StringUtils.isNotBlank(dateFrom)) {
				Date dateStart = format.parse(dateFrom);
				DateFormat dateFormatIndexx = new SimpleDateFormat("yyyyMMdd");
				String todaysIndex = dateFormatIndexx.format(dateStart.getTime());
				dateQuery.append(FieldType.DATE_INDEX.getName(), todaysIndex);
			}

			// String payoutAcqName =
			// payoutAcquirerMappingDao.findPayoutAcquirerNameByPayId(merchantId,
			// subMerchantPayId);
			//
			// if (StringUtils.isNotBlank(payoutAcqName)) {
			// acqNameQuery.put(FieldType.ACQUIRER_NAME.getName(),
			// payoutAcqName);
			// }

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));

			if (StringUtils.isNotBlank(subMerchantPayId)) {

				if (!subMerchantPayId.equalsIgnoreCase("ALL")) {
					User vCode = userDao.findPayId(subMerchantPayId);
					if (StringUtils.isNotBlank(vCode.getVirtualAccountNo())) {
						paramConditionLst.add(
								new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(), vCode.getVirtualAccountNo()));
					}else{
						paramConditionLst.add(
								new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(), ""));
					}
				} else {
					List<Merchants> subMerchantList = userDao.getSubMerchantListBySuperPayId(merchantId);
					for (Merchants subMerchant : subMerchantList) {

						vaConditionList.add((new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(),
								subMerchant.getVirtualAccountNo())));
					}

					vaQuery.append("$or", vaConditionList);
				}
			} else {
				if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
					User vCode = userDao.findPayId(merchantId);
					if (vCode.isSuperMerchant() == true && StringUtils.isNotBlank(vCode.getSuperMerchantId())) {
						List<Merchants> subMerchantList = userDao.getSubMerchantListBySuperPayId(merchantId);
						for (Merchants subMerchant : subMerchantList) {

							vaConditionList.add((new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(),
									subMerchant.getVirtualAccountNo())));
						}

						vaQuery.append("$or", vaConditionList);
					} else {
						if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
							if (StringUtils.isNotBlank(vCode.getVirtualAccountNo())) {
								paramConditionLst.add(new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(),
										vCode.getVirtualAccountNo()));
							}else{
								paramConditionLst.add(new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(),""));
							}
						}
					}

				}
			}

			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			userQuery = new BasicDBObject(FieldType.USER_TYPE.name(), "Merchant Initiated Direct");

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!dateQuery.isEmpty()) {
				fianlList.add(dateQuery);
			}

			if (!vaQuery.isEmpty()) {
				fianlList.add(vaQuery);
			}
			if (!acqNameQuery.isEmpty()) {
				fianlList.add(acqNameQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside getClosingTransactionAmountForLedger() , finalquery = " + finalquery);

			MongoCursor<Document> cursor = coll.find(finalquery).iterator();

			if (cursor.hasNext()) {
				while (cursor.hasNext()) {
					Document dbobj = cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

						dbobj = dataEncDecTool.decryptDocument(dbobj);
					}

					BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
					settledAmount = settledAmount.add(dbAmount);
				}
				totalClosingAmount = settledAmount;
			} else {
				Calendar cal = Calendar.getInstance();
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd 00:00:00");

				String todaysDateIndex = dateFormat.format(cal.getTime());
				String todaysDate = dateFormat2.format(cal.getTime());
				cal.add(Calendar.DATE, -1);
				String yesterdayDate = dateFormat2.format(cal.getTime());
				String yesterdayDateIndex = dateFormat.format(cal.getTime());

				logger.info("Todays Date = " + todaysDate);
				logger.info("Yesterdays Date = " + yesterdayDate);

				BasicDBObject dateQuery1 = new BasicDBObject(FieldType.DATE_INDEX.getName(), yesterdayDateIndex);
				List<BasicDBObject> fianlList1 = new ArrayList<BasicDBObject>();

				if (!allParamQuery.isEmpty()) {
					fianlList1.add(allParamQuery);
				}

				if (!dateQuery.isEmpty()) {
					fianlList1.add(dateQuery1);
				}

				if (!vaQuery.isEmpty()) {
					fianlList1.add(vaQuery);
				}
				BasicDBObject finalquery1 = new BasicDBObject("$and", fianlList1);
				MongoCursor<Document> cursor1 = coll.find(finalquery1).iterator();

				if (cursor1.hasNext()) {
					MongoCursor<Document> cursor2 = coll.find(dateQuery1).iterator();
					while (cursor2.hasNext()) {
						Document dbobj = cursor2.next();

						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

							dbobj = dataEncDecTool.decryptDocument(dbobj);
						}

						Date currentDate1 = new Date();
						String dateNowW = DateCreater.formatDateForDb(currentDate1);
						String amount = dbobj.get("AMOUNT").toString();

						Document doc1 = new Document();
						doc1.put(FieldType.CREATE_DATE.getName(), dateNowW);
						doc1.put(FieldType.UPDATE_DATE.getName(), dateNowW);
						doc1.put(FieldType.CLOSING_DATE.getName(), todaysDate);
						doc1.put(FieldType.VIRTUAL_AC_CODE.getName(),
								dbobj.get(FieldType.VIRTUAL_AC_CODE.getName()).toString());
						doc1.put(FieldType.PAY_ID.getName(), dbobj.get(FieldType.PAY_ID.getName()).toString());
						if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
							doc1.put(FieldType.SUB_MERCHANT_ID.getName(),
									dbobj.get(FieldType.SUB_MERCHANT_ID.getName()).toString());
						}

						if (dbobj.containsKey(FieldType.RESELLER_ID.getName())) {
							doc1.put(FieldType.RESELLER_ID.getName(),
									dbobj.get(FieldType.RESELLER_ID.getName()).toString());
						}
						doc1.put(FieldType.DEBIT_AMOUNT.getName(), "0.00");
						doc1.put(FieldType.CREDIT_AMOUNT.getName(), "0.00");
						doc1.put(FieldType.AMOUNT.getName(), amount);
						doc1.put(FieldType.OPENING_AMOUNT.getName(), amount);
						doc1.put(FieldType.DATE_INDEX.getName(), todaysDateIndex);
						doc1.put(FieldType.ACQUIRER_NAME.getName(), dbobj.get(FieldType.ACQUIRER_NAME.getName()));
						coll.insertOne(doc1);
						logger.info("Updated closing amount for Pay Id = "
								+ dbobj.get(FieldType.PAY_ID.getName()).toString());
					}
				}
				MongoCursor<Document> cursor3 = coll.find(finalquery).iterator();

				while (cursor3.hasNext()) {
					Document dbobj = cursor3.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

						dbobj = dataEncDecTool.decryptDocument(dbobj);
					}

					BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
					settledAmount = settledAmount.add(dbAmount);
				}
				totalClosingAmount = settledAmount;

			}
			logger.info("Total today's Transactions Amount  " + totalClosingAmount);

		} catch (Exception e) {
			logger.error("Exception ", e);
		}

		return totalClosingAmount;
	}

	public BigDecimal getPayOutTransactionAmountForLedger(String dateFrom, String dateTo, String merchantId,
			String subMerchantPayId, User user) {
		logger.info("Inside getImpsTransactionAmount()");
		Map<String, User> userMap = new HashMap<String, User>();
		BigDecimal totalPayOutAmount = new BigDecimal("0").setScale(2);
		try {

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject userQuery = new BasicDBObject();
			BigDecimal settledAmount = new BigDecimal("0").setScale(2);
			String currentDate = null;
			if (StringUtils.isNotBlank(dateFrom)) {
				if (StringUtils.isNotBlank(dateTo)) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String todayDate = sdf.format(new Date());

				dateFrom = todayDate + " " + "00:00:00";
				dateTo = todayDate + " " + "23:59:59";
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));

			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));

			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			userQuery = new BasicDBObject(FieldType.USER_TYPE.name(), "Merchant Initiated Direct");

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!dateQuery.isEmpty()) {
				fianlList.add(dateQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside TxnReports , downloadSearchPayment , finalquery = " + finalquery);

			finalquery.append(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());

			MongoCursor<Document> cursor = coll.find(finalquery).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				settledAmount = settledAmount.add(dbAmount);
			}
			totalPayOutAmount = settledAmount;
			logger.info("Total today's Transactions Amount  " + totalPayOutAmount);

		} catch (Exception e) {
			logger.error("Exception ", e);
		}

		return totalPayOutAmount;
	}

	@SuppressWarnings("static-access")
	public List<ImpsDownloadObject> downloadLedgerReportData(String dateFrom, String dateTo, String merchantId,
			String subMerchantPayId, User user, String closingBalance, String openingBalance, String acquirerName) {
		logger.info("Inside viewLedgerReportData()");
		Map<String, User> userMap = new HashMap<String, User>();

		// TreeSet<ImpsDownloadObject> transactionList=new
		// TreeSet<ImpsDownloadObject>();
		List<ImpsDownloadObject> transactionList = new ArrayList<ImpsDownloadObject>();
		boolean ECollection = false;
		try {

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> vaConditionList = new ArrayList<BasicDBObject>();
			Set<String> dateList = new HashSet<String>();
			Set<String> virtualList = new HashSet<String>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject userQuery = new BasicDBObject();
			BasicDBObject acqNameQuery = new BasicDBObject();
			BasicDBObject vaQuery = new BasicDBObject();
			String toDate = null;
			if (StringUtils.isNotBlank(dateTo)) {
				dateFrom = dateTo + " " + "00:00:00";
				toDate = dateTo + " " + "23:59:59";
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());
			}

			if (StringUtils.isNotBlank(acquirerName)) {
				acqNameQuery.put(FieldType.ACQUIRER_NAME.getName(), acquirerName);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
			User user2 = null;

			if (!subMerchantPayId.equalsIgnoreCase("null") && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				user2 = userDao.findPayId(subMerchantPayId);
				if (StringUtils.isNotBlank(user2.getVirtualAccountNo())) {
					paramConditionLst
							.add(new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(), user2.getVirtualAccountNo()));
				}
			} else {

				if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
					user2 = userDao.findPayId(merchantId);
					paramConditionLst
							.add(new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(), user2.getVirtualAccountNo()));

				}
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			userQuery = new BasicDBObject(FieldType.USER_TYPE.name(), "Merchant Initiated Direct");

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!dateQuery.isEmpty()) {
				fianlList.add(dateQuery);
			}

			if (!vaQuery.isEmpty()) {
				fianlList.add(vaQuery);
			}
			if (!acqNameQuery.isEmpty()) {
				fianlList.add(acqNameQuery);
			}

			/*
			 * if (!userQuery.isEmpty()) { fianlList.add(userQuery); }
			 */

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside TxnReports , downloadSearchPayment , finalquery = " + finalquery);

			// finalquery.append(FieldType.STATUS.getName(),
			// StatusType.CAPTURED.getName());

			MongoCursor<Document> cursor = coll.find(finalquery).iterator();
			SortedSet<String> ts = new TreeSet<String>();
			String va = null;

			ImpsDownloadObject impsReport = new ImpsDownloadObject();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				impsReport.setOpeningBalance(dbobj.getString(FieldType.OPENING_AMOUNT.getName()));

				String payIdReport1 = "";
				String subMerchantPayIdReport1 = "";
				if (user2.isSuperMerchant() == false && StringUtils.isNotBlank(user2.getSuperMerchantId())) {
					payIdReport1 = user2.getSuperMerchantId();
					subMerchantPayIdReport1 = user2.getPayId();
					impsReport.setMerchant(userDao.getBusinessNameByPayId(payIdReport1));
					impsReport.setMerchantPayId(payIdReport1);
					impsReport.setSubMerchant(userDao.getBusinessNameByPayId(subMerchantPayIdReport1));
					impsReport.setSubMerchantPayId(subMerchantPayIdReport1);
				} else {
					payIdReport1 = user2.getPayId();
					if (merchantId.equalsIgnoreCase("ALL")) {
						impsReport.setMerchant(userDao.getBusinessNameByPayId(payIdReport1));
						impsReport.setMerchantPayId(payIdReport1);
						impsReport.setSubMerchant(CrmFieldConstants.NA.getValue());
					} else {
						impsReport.setMerchantPayId(payIdReport1);
						impsReport.setMerchant(userDao.getBusinessNameByPayId(payIdReport1));
					}
				}

				impsReport.setRrn(CrmFieldConstants.NA.getValue());
				/*
				 * Calendar cal1 = Calendar.getInstance(); SimpleDateFormat sdf1
				 * = new SimpleDateFormat("HH:mm:ss");
				 * 
				 * String currentdate1 = dateTo + " " +
				 * sdf1.format(cal1.getTime());
				 */
				impsReport.setDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				impsReport.setClosingBalance(dbobj.getString(FieldType.OPENING_AMOUNT.getName()));
				transactionList.add(impsReport);
			}

			BasicDBObject finalquery1 = new BasicDBObject();
			BasicDBObject finalquery3 = new BasicDBObject();
			BasicDBObject finalquery2 = new BasicDBObject();
			BasicDBObject finalquery4 = new BasicDBObject();
			List<BasicDBObject> ConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> ConditionLstIMPS = new ArrayList<BasicDBObject>();
			List<BasicDBObject> ConditionLstClosing = new ArrayList<BasicDBObject>();

			MongoDatabase dbIns1 = mongoInstance.getDB();
			MongoCollection<Document> coll1 = dbIns1
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.E_COLLECTION.getValue()));

			ConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			if (!dateQuery.isEmpty()) {
				ConditionLst.add(dateQuery);
			}

			if (!allParamQuery.isEmpty()) {
				ConditionLst.add(allParamQuery);
			}
			if (!acqNameQuery.isEmpty()) {
				ConditionLst.add(acqNameQuery);
			}

			if (!ConditionLst.isEmpty()) {
				finalquery1 = new BasicDBObject("$and", ConditionLst);
			}
			FindIterable<Document> iterDoc1 = coll1.find(finalquery1);
			MongoCursor<Document> cursor2 = iterDoc1.iterator();
			// BigDecimal openingBalance =new BigDecimal("0").setScale(2);

			while (cursor2.hasNext()) {
				Document dbobj = cursor2.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				ts.add(dbobj.getString(FieldType.CREATE_DATE.getName()));
			}

			MongoDatabase dbIns2 = mongoInstance.getDB();
			MongoCollection<Document> coll2 = dbIns2.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));

			ConditionLstIMPS.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			if (!dateQuery.isEmpty()) {
				ConditionLstIMPS.add(dateQuery);
			}

			if (!userQuery.isEmpty()) {
				ConditionLstIMPS.add(userQuery);
			}
			if (!acqNameQuery.isEmpty()) {
				ConditionLstIMPS.add(acqNameQuery);
			}

			if (!allParamQuery.isEmpty()) {
				ConditionLstIMPS.add(allParamQuery);
			}

			if (!ConditionLstIMPS.isEmpty()) {
				finalquery2 = new BasicDBObject("$and", ConditionLstIMPS);
			}

			FindIterable<Document> iterDoc2 = coll2.find(finalquery2);
			MongoCursor<Document> cursor3 = iterDoc2.iterator();

			while (cursor3.hasNext()) {
				Document dbobj = cursor3.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				ts.add(dbobj.getString(FieldType.CREATE_DATE.getName()));
			}

			BigDecimal finalClosing = new BigDecimal(openingBalance).setScale(2);
			for (String dateVc : ts) {
				List<BasicDBObject> ConditionLstDateEcollection = new ArrayList<BasicDBObject>();
				List<BasicDBObject> ConditionLstDateIMPS = new ArrayList<BasicDBObject>();
				ConditionLstDateEcollection
						.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
				ConditionLstDateEcollection.add(new BasicDBObject(FieldType.CREATE_DATE.getName(), dateVc));

				if (!allParamQuery.isEmpty()) {
					ConditionLstDateEcollection.add(allParamQuery);
				}

				if (!ConditionLstDateEcollection.isEmpty()) {
					finalquery3 = new BasicDBObject("$and", ConditionLstDateEcollection);
				}

				FindIterable<Document> iterDoc3 = coll1.find(finalquery3);
				MongoCursor<Document> cursor4 = iterDoc3.iterator();

				while (cursor4.hasNext()) {
					Document dbobj = cursor4.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

						dbobj = dataEncDecTool.decryptDocument(dbobj);
					}

					BigDecimal totalCredit = new BigDecimal("0").setScale(2);
					BigDecimal totalTopUp = new BigDecimal("0").setScale(2);
					BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
					if ((dbobj.getString(FieldType.TXNTYPE.getName())).equalsIgnoreCase("COLLECTION")) {
						totalCredit = (dbAmount);
					} else {
						totalCredit = (dbAmount);
					}

					ImpsDownloadObject impsReport1 = new ImpsDownloadObject();
					if (StringUtils.isNotBlank(totalCredit.toString())) {
						if ((dbobj.getString(FieldType.TXNTYPE.getName())).equalsIgnoreCase("COLLECTION")) {
							impsReport1.setTotalCredit(totalCredit.toString());
						} else {
							impsReport1.setTotalTopup(totalCredit.toString());
						}
						User user1 = null;

						user1 = userDao.findByVirtualAcc(dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()));
						String payIdReport = "";
						String subMerchantPayIdReport = "";
						if (user1.isSuperMerchant() == false && StringUtils.isNotBlank(user1.getSuperMerchantId())) {
							payIdReport = user1.getSuperMerchantId();
							subMerchantPayIdReport = user1.getPayId();
							impsReport1.setMerchant(userDao.getBusinessNameByPayId(payIdReport));
							impsReport1.setMerchantPayId(payIdReport);
							impsReport1.setSubMerchant(userDao.getBusinessNameByPayId(subMerchantPayIdReport));
							impsReport1.setSubMerchantPayId(subMerchantPayIdReport);
						} else {
							payIdReport = user1.getPayId();
							if (merchantId.equalsIgnoreCase("ALL")) {
								impsReport1.setMerchant(userDao.getBusinessNameByPayId(payIdReport));
								impsReport1.setMerchantPayId(payIdReport);
								impsReport1.setSubMerchant(CrmFieldConstants.NA.getValue());
							} else {
								impsReport1.setMerchantPayId(payIdReport);
								impsReport1.setMerchant(userDao.getBusinessNameByPayId(payIdReport));
							}
						}

						finalClosing = finalClosing.add(totalCredit);
						impsReport1.setClosingBalance(finalClosing.toString());
						impsReport1.setDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
						if (dbobj.containsKey(FieldType.RRN.getName())) {
							impsReport1.setRrn(dbobj.getString(FieldType.RRN.getName()));
						} else {
							impsReport1.setRrn(CrmFieldConstants.NA.getValue());
						}
						transactionList.add(impsReport1);
					}
				}

				ConditionLstDateIMPS.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
				ConditionLstDateIMPS.add(new BasicDBObject(FieldType.CREATE_DATE.getName(), dateVc));

				if (!allParamQuery.isEmpty()) {
					ConditionLstDateIMPS.add(allParamQuery);
				}

				if (!userQuery.isEmpty()) {
					ConditionLstDateIMPS.add(userQuery);
				}
				if (!acqNameQuery.isEmpty()) {
					ConditionLstDateIMPS.add(acqNameQuery);
				}
				if (!ConditionLstDateIMPS.isEmpty()) {
					finalquery4 = new BasicDBObject("$and", ConditionLstDateIMPS);
				}

				FindIterable<Document> iterDoc4 = coll2.find(finalquery3);
				MongoCursor<Document> cursor5 = iterDoc4.iterator();

				while (cursor5.hasNext()) {
					Document dbobj = cursor5.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

						dbobj = dataEncDecTool.decryptDocument(dbobj);
					}

					BigDecimal totalDebit = new BigDecimal("0").setScale(2);
					BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
					totalDebit = (dbAmount);
					ImpsDownloadObject impsReport2 = new ImpsDownloadObject();
					if (StringUtils.isNotBlank(totalDebit.toString())) {
						impsReport2.setTotalDebit(totalDebit.toString());
						User user1 = null;

						user1 = userDao.findByVirtualAcc(dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()));
						String payIdReport = "";
						String subMerchantPayIdReport = "";
						if (user1.isSuperMerchant() == false && StringUtils.isNotBlank(user1.getSuperMerchantId())) {
							payIdReport = user1.getSuperMerchantId();
							subMerchantPayIdReport = user1.getPayId();
							impsReport2.setMerchant(userDao.getBusinessNameByPayId(payIdReport));
							impsReport2.setMerchantPayId(payIdReport);
							impsReport2.setSubMerchant(userDao.getBusinessNameByPayId(subMerchantPayIdReport));
							impsReport2.setSubMerchantPayId(subMerchantPayIdReport);
						} else {
							payIdReport = user1.getPayId();
							if (merchantId.equalsIgnoreCase("ALL")) {
								impsReport2.setMerchant(userDao.getBusinessNameByPayId(payIdReport));
								impsReport2.setMerchantPayId(payIdReport);
								impsReport2.setSubMerchant(CrmFieldConstants.NA.getValue());
							} else {
								impsReport2.setMerchantPayId(payIdReport);
								impsReport2.setMerchant(userDao.getBusinessNameByPayId(payIdReport));
							}
						}

						/*
						 * SimpleDateFormat inputDate= new
						 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						 * SimpleDateFormat dateFormat = new
						 * SimpleDateFormat("yyyy-MM-dd"); String dtDate =
						 * dateFormat.format(inputDate.parse(dbobj.getString(
						 * FieldType.CREATE_DATE. getName())));
						 */
						finalClosing = finalClosing.subtract(totalDebit);
						impsReport2.setClosingBalance(finalClosing.toString());
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))) {
							impsReport2.setRrn(dbobj.getString(FieldType.RRN.getName()));
						} else {
							impsReport2.setRrn(CrmFieldConstants.NA.getValue());
						}
						impsReport2.setDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
						transactionList.add(impsReport2);
					}
				}
			}

			/*
			 * ImpsDownloadObject impsReport3 = new ImpsDownloadObject(); if
			 * (StringUtils.isNotBlank(closingBalance)) {
			 * impsReport3.setClosingBalance(closingBalance);
			 * 
			 * String payIdReport = ""; String subMerchantPayIdReport = ""; if
			 * (user2.isSuperMerchant() == false &&
			 * StringUtils.isNotBlank(user2.getSuperMerchantId())) { payIdReport
			 * = user2.getSuperMerchantId(); subMerchantPayIdReport =
			 * user2.getPayId();
			 * impsReport3.setMerchant(userDao.getBusinessNameByPayId(
			 * payIdReport)); impsReport3.setMerchantPayId(payIdReport);
			 * impsReport3.setSubMerchant(userDao.getBusinessNameByPayId(
			 * subMerchantPayIdReport));
			 * impsReport3.setSubMerchantPayId(subMerchantPayIdReport); } else {
			 * payIdReport = user2.getPayId(); if
			 * (merchantId.equalsIgnoreCase("ALL")) {
			 * impsReport3.setMerchant(userDao.getBusinessNameByPayId(
			 * payIdReport)); impsReport3.setMerchantPayId(payIdReport);
			 * impsReport3.setSubMerchant(CrmFieldConstants.NA.getValue()); }
			 * else { impsReport3.setMerchantPayId(payIdReport);
			 * impsReport3.setMerchant(userDao.getBusinessNameByPayId(
			 * payIdReport)); } }
			 * 
			 * impsReport3.setRrn(CrmFieldConstants.NA.getValue());
			 * 
			 * Calendar cal = Calendar.getInstance(); SimpleDateFormat sdf = new
			 * SimpleDateFormat("HH:mm:ss");
			 * 
			 * String currentdate = dateTo + " " + sdf.format(cal.getTime());
			 * impsReport3.setDate(currentdate);
			 * transactionList.add(impsReport3); }
			 */
			return transactionList;

		} catch (Exception e) {
			logger.error("Exception cought in downloadLedgerReportData() ", e);
		}

		return transactionList;
	}

	// Graph Data
	@SuppressWarnings("static-access")
	public List<ImpsDownloadObject> viewGrahData(String dateFrom, String dateTo, String merchantId,
			String subMerchantPayId, User user) {
		logger.info("Inside viewLedgerReportData()");
		Map<String, User> userMap = new HashMap<String, User>();
		List<ImpsDownloadObject> transactionList = new ArrayList<ImpsDownloadObject>();

		try {

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> vaConditionList = new ArrayList<BasicDBObject>();
			Set<String> dateList = new HashSet<String>();
			Set<String> virtualList = new HashSet<String>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject userQuery = new BasicDBObject();
			BasicDBObject acqNameQuery = new BasicDBObject();
			BasicDBObject vaQuery = new BasicDBObject();
			String currentDate = null;

			dateFrom = dateFrom + " " + "00:00:00";
			dateTo = dateTo + " " + "23:59:59";
			if (StringUtils.isNotBlank(dateFrom)) {
				if (StringUtils.isNotBlank(dateTo)) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			String payoutAcqName = payoutAcquirerMappingDao.findPayoutAcquirerNameByPayId(merchantId, subMerchantPayId);

			if (StringUtils.isNotBlank(payoutAcqName)) {
				acqNameQuery.put(FieldType.ACQUIRER_NAME.getName(), payoutAcqName);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
			User user2 = null;
			if (StringUtils.isNotBlank(merchantId)) {
				user2 = userDao.findPayId(merchantId);
			}

			if (StringUtils.isNotBlank(merchantId) && StringUtils.isNotBlank(subMerchantPayId)) {
				User user3 = userDao.findPayId(subMerchantPayId);
				if (StringUtils.isNotBlank(user3.getVirtualAccountNo())) {
					paramConditionLst
							.add(new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(), user3.getVirtualAccountNo()));
				}
			} else if (StringUtils.isNotBlank(merchantId)) {
				if (user2.isSuperMerchant()) {
					List<Merchants> subMerchantList = userDao.getSubMerchantListBySuperPayId(merchantId);
					for (Merchants subMerchant : subMerchantList) {

						vaConditionList.add((new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(),
								subMerchant.getVirtualAccountNo())));
					}

					vaQuery.append("$or", vaConditionList);
				} else {
					paramConditionLst
							.add(new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(), user2.getVirtualAccountNo()));

				}
			}

			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			userQuery = new BasicDBObject(FieldType.USER_TYPE.name(), "Merchant Initiated Direct");

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!dateQuery.isEmpty()) {
				fianlList.add(dateQuery);
			}

			if (!vaQuery.isEmpty()) {
				fianlList.add(vaQuery);
			}
			if (!acqNameQuery.isEmpty()) {
				fianlList.add(acqNameQuery);
			}

			/*
			 * if (!userQuery.isEmpty()) { fianlList.add(userQuery); }
			 */

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside TxnReports , downloadSearchPayment , finalquery = " + finalquery);

			// finalquery.append(FieldType.STATUS.getName(),
			// StatusType.CAPTURED.getName());

			MongoCursor<Document> cursor = coll.find(finalquery).iterator();
			TreeSet<String> myTreeSet = (TreeSet<String>) new TreeSet<String>().descendingSet();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String dtDate = dateFormat.format(inputDate.parse(dbobj.getString(FieldType.CREATE_DATE.getName())));
				dateList.add(dtDate);
			}
			myTreeSet.addAll(dateList);
			cursor.close();
			for (String dt : myTreeSet) {
				BasicDBObject finalquery1 = new BasicDBObject();
				BasicDBObject finalquery2 = new BasicDBObject();
				BasicDBObject dateQuery1 = new BasicDBObject();
				BasicDBObject dateQuery2 = new BasicDBObject();
				List<BasicDBObject> ConditionLstIMPS = new ArrayList<BasicDBObject>();
				List<BasicDBObject> ConditionLstClosing = new ArrayList<BasicDBObject>();

				String fromDate = dt + " 00:00:00";
				String toDate = dt + " 23:59:59";

				dateQuery1.put(FieldType.CLOSING_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());

				if (!dateQuery1.isEmpty()) {
					ConditionLstClosing.add(dateQuery1);
				}

				if (!allParamQuery.isEmpty()) {
					ConditionLstClosing.add(allParamQuery);
				}

				if (!vaQuery.isEmpty()) {
					ConditionLstClosing.add(vaQuery);
				}
				if (!acqNameQuery.isEmpty()) {
					ConditionLstClosing.add(acqNameQuery);
				}
				/*
				 * if(!userQuery.isEmpty()) { ConditionLstIMPS.add(userQuery); }
				 */

				if (!ConditionLstClosing.isEmpty()) {
					finalquery1 = new BasicDBObject("$and", ConditionLstClosing);
				}

				FindIterable<Document> iterDoc = coll.find(finalquery1);
				MongoCursor<Document> cursor1 = iterDoc.iterator();

				BigDecimal openingBalance = new BigDecimal("0").setScale(2);
				BigDecimal totalDebit = new BigDecimal("0").setScale(2);
				while (cursor1.hasNext()) {
					Document dbobj = cursor1.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

						dbobj = dataEncDecTool.decryptDocument(dbobj);
					}

					BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
					BigDecimal debitAmount = new BigDecimal(dbobj.getString(FieldType.DEBIT_AMOUNT.getName()))
							.setScale(2);
					openingBalance = openingBalance.add(dbAmount);
					totalDebit = totalDebit.add(debitAmount);
				}

				ImpsDownloadObject impsReport = new ImpsDownloadObject();

				if (StringUtils.isNotBlank(openingBalance.toString())) {
					impsReport.setOpeningBalance(openingBalance.toString());
				}

				if (StringUtils.isNotBlank(totalDebit.toString())) {
					impsReport.setTotalDebit(totalDebit.toString());
				}

				impsReport.setDate(dt);

				transactionList.add(impsReport);

			}

			return transactionList;

		} catch (Exception e) {
			logger.error("Exception cought in viewGrahData() ", e);
		}

		return transactionList;
	}

	@SuppressWarnings("static-access")
	public List<ImpsDownloadObject> BeneRegistrationReportData(String dateFrom, String dateTo, String merchantId,
			String subMerchantPayId, String status, String channel, User user) {
		List<ImpsDownloadObject> transactionList = new ArrayList<ImpsDownloadObject>();
		Map<String, User> userMap = new HashMap<String, User>();
		Set<String> orderIdd = new HashSet<String>();
		Set<String> payIdd = new HashSet<String>();
		boolean isParameterised = false;

		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> statusConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject channelQuery = new BasicDBObject();
			BasicDBObject userQuery = new BasicDBObject();
			BasicDBObject orderIdQuery = new BasicDBObject();
			BasicDBObject beneAccountQuery = new BasicDBObject();

			String currentDate = null;
			if (!dateFrom.isEmpty()) {
				if (!dateTo.isEmpty()) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));

				isParameterised = true;
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}

			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				statusQuery = new BasicDBObject(FieldType.STATUS.getName(), status);
			}

			if (StringUtils.isNotBlank(channel) && !channel.equalsIgnoreCase("ALL")) {
				channelQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), channel);
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}

			if (!channelQuery.isEmpty()) {
				fianlList.add(channelQuery);
			}

			if (!userQuery.isEmpty()) {
				fianlList.add(userQuery);
			}

			if (!dateQuery.isEmpty()) {
				fianlList.add(dateQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside MerchantInitiatedDirectDao , BeneRegistrationReportData , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.COMPOSITE_BENE_COLLECTION.getValue()));
			BasicDBObject match1 = new BasicDBObject("$match", finalquery);
			BasicDBObject sort1 = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match1, sort1);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				ImpsDownloadObject impsReport = new ImpsDownloadObject();

				// For Merchant Business Name
				String merchantPayId = dbobj.getString(FieldType.PAY_ID.getName());
				User merchantUser = new User();

				if (userMap.get(merchantPayId) != null && !userMap.get(merchantPayId).getPayId().isEmpty()) {
					merchantUser = userMap.get(merchantUser);
				} else {
					merchantUser = userDao.findPayId(merchantPayId);
					userMap.put(merchantPayId, merchantUser);
				}

				if (merchantUser != null) {
					impsReport.setMerchant(merchantUser.getBusinessName());
					impsReport.setMerchantPayId(merchantUser.getPayId());
				} else {
					impsReport.setMerchant(userDao.getBusinessNameByPayId(merchantPayId));
					impsReport.setMerchantPayId(merchantPayId);
				}

				// Sub Merchant Set
				if (((!merchantId.equalsIgnoreCase("All")) || (merchantId.equalsIgnoreCase("All"))
						|| (merchantId.equalsIgnoreCase(""))) && dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userDao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						impsReport.setSubMerchant(subMerchantUser.getBusinessName());
						impsReport.setSubMerchantPayId(subMerchantUser.getPayId());
					} else {
						impsReport.setSubMerchant(CrmFieldConstants.NA.getValue());
					}
				} else {
					impsReport.setSubMerchant(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()))) {
					impsReport.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()));
				} else {
					impsReport.setBankAccountNumber(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORDER_ID.getName()))) {
					impsReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.getName()));
				} else {
					impsReport.setOrderId(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_NAME.getName()))) {
					impsReport.setBeneAccountName(dbobj.getString(FieldType.BENE_NAME.getName()));
				} else {
					impsReport.setBeneAccountName(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.IFSC_CODE.getName()))) {
					impsReport.setBankIFSC(dbobj.getString(FieldType.IFSC_CODE.getName()));
				} else {
					impsReport.setBankIFSC(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.getName()))) {
					impsReport.setStatus(dbobj.getString(FieldType.STATUS.getName()));
				} else {
					impsReport.setStatus(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_REGISTRATION.getName()))) {
					impsReport.setBeneRegistration(dbobj.getString(FieldType.BENE_REGISTRATION.getName()));
				} else {
					impsReport.setBeneRegistration(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.getName()))) {
					impsReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.getName()));
				} else {
					impsReport.setTxnType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PHONE_NO.getName()))) {
					impsReport.setPhoneNo(dbobj.getString(FieldType.PHONE_NO.getName()));
				} else {
					impsReport.setPhoneNo(CrmFieldConstants.NA.getValue());
				}
				transactionList.add(impsReport);
			}
			cursor.close();

			logger.info("Total data in Bene Registration is " + transactionList.size());
			return transactionList;
		} catch (Exception e) {
			logger.error("Exception occured in MerchantInitiatedDirectDao , BeneRegistrationReportData , Exception = ",
					e);
			return transactionList;
		}
	}

	public boolean deleteBeneficiaryData(String merchantId, String subMerchantPayId, String bankAccountNumber,
			String bankIFSC, User user) {
		logger.info("Inside  deleteBeneficiaryData()");

		try {
			Map<String, String> requestMap = new HashMap<>();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject userQuery = new BasicDBObject();
			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				requestMap.put(FieldType.PAY_ID.getName(), merchantId);
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				requestMap.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId);
			}

			if (StringUtils.isNotBlank(bankIFSC) && !bankIFSC.equalsIgnoreCase("NA")) {
				requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNumber);
				requestMap.put(FieldType.IFSC_CODE.getName(), bankIFSC);
			} else {
				requestMap.put(FieldType.PAYER_ADDRESS.getName(), bankAccountNumber);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.COMPOSITE_BENE_COLLECTION.getValue()));

			Bson filter;
			if (requestMap.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
				if (requestMap.containsKey(FieldType.BENE_ACCOUNT_NO.getName())) {
					filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
							requestMap.get(FieldType.SUB_MERCHANT_ID.getName()))
									.append(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNumber)
									.append(FieldType.IFSC_CODE.getName(), bankIFSC);
				} else {
					filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
							requestMap.get(FieldType.SUB_MERCHANT_ID.getName()))
									.append(FieldType.PAYER_ADDRESS.getName(), bankAccountNumber);
				}
			} else {
				if (requestMap.containsKey(FieldType.BENE_ACCOUNT_NO.getName())) {
					filter = new Document(FieldType.PAY_ID.getName(), requestMap.get(FieldType.PAY_ID.getName()))
							.append(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNumber)
							.append(FieldType.IFSC_CODE.getName(), bankIFSC);
				} else {
					filter = new Document(FieldType.PAY_ID.getName(), requestMap.get(FieldType.PAY_ID.getName()))
							.append(FieldType.PAYER_ADDRESS.getName(), bankAccountNumber);
				}
			}

			Bson newValue = new Document(FieldType.BENE_REGISTRATION.getName(), "inActive");
			Bson updateOperationDocument = new Document("$set", newValue);
			coll.updateOne(filter, updateOperationDocument);
			return true;

		} catch (Exception e) {
			logger.error("Exception occured in MerchantInitiatedDirectDao , deleteBeneficiaryData , Exception = ", e);
			return false;
		}

	}

	// Add Bene

	public Map<String, String> isBeneAccountNumber(Map<String, String> requestMap) {
		logger.info("Inside  isBeneAccountNumber()");

		Map<String, String> res = new HashMap<>();
		try {

			boolean flag = false;
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			BasicDBObject conditionQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.COMPOSITE_BENE_COLLECTION.getValue()));
			User user = new User();
			if (StringUtils.isNotBlank(requestMap.get(FieldType.PAY_ID.getName()))) {
				user = userDao.findPayId(requestMap.get(FieldType.PAY_ID.getName()));
				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
							requestMap.get(FieldType.PAY_ID.getName())));
				} else {
					paramConditionLst.add(
							new BasicDBObject(FieldType.PAY_ID.getName(), requestMap.get(FieldType.PAY_ID.getName())));
				}
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			if (StringUtils.isNotBlank(requestMap.get(FieldType.BENE_ACCOUNT_NO.getName()))) {
				conditionList.add(new BasicDBObject(FieldType.BENE_ACCOUNT_NO.getName(),
						requestMap.get(FieldType.BENE_ACCOUNT_NO.getName())));
				conditionList.add(new BasicDBObject(FieldType.IFSC_CODE.getName(),
						requestMap.get(FieldType.IFSC_CODE.getName())));
				conditionList.add(new BasicDBObject(FieldType.STATUS.getName(), "SUCCESS"));
			} else {
				conditionList.add(new BasicDBObject(FieldType.PAYER_ADDRESS.getName(),
						requestMap.get(FieldType.PAYER_ADDRESS.getName())));
				conditionList.add(new BasicDBObject(FieldType.STATUS.getName(), "SUCCESS"));
			}

			if (!conditionList.isEmpty()) {
				conditionQuery = new BasicDBObject("$and", conditionList);
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!conditionQuery.isEmpty()) {
				fianlList.add(conditionQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			MongoCursor<Document> cursor = coll.find(finalquery).iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				String status = dbobj.getString(FieldType.BENE_REGISTRATION.getName());
				if (dbobj.getString(FieldType.STATUS.getName()).equalsIgnoreCase("SUCCESS")) {
					flag = true;
					if (!status.equalsIgnoreCase("Active")) {
						Bson filter;
						if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
							if (requestMap.containsKey(FieldType.BENE_ACCOUNT_NO.getName())) {
								filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
										requestMap.get(FieldType.PAY_ID.getName()))
												.append(FieldType.BENE_ACCOUNT_NO.getName(),
														requestMap.get(FieldType.BENE_ACCOUNT_NO.getName()))
												.append(FieldType.IFSC_CODE.getName(),
														requestMap.get(FieldType.IFSC_CODE.getName()));
							} else {
								filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
										requestMap.get(FieldType.PAY_ID.getName())).append(
												FieldType.PAYER_ADDRESS.getName(),
												requestMap.get(FieldType.PAYER_ADDRESS.getName()));
							}
						} else {
							if (requestMap.containsKey(FieldType.BENE_ACCOUNT_NO.getName())) {
								filter = new Document(FieldType.PAY_ID.getName(),
										requestMap.get(FieldType.PAY_ID.getName()))
												.append(FieldType.BENE_ACCOUNT_NO.getName(),
														requestMap.get(FieldType.BENE_ACCOUNT_NO.getName()))
												.append(FieldType.IFSC_CODE.getName(),
														requestMap.get(FieldType.IFSC_CODE.getName()));
							} else {
								filter = new Document(FieldType.PAY_ID.getName(),
										requestMap.get(FieldType.PAY_ID.getName())).append(
												FieldType.PAYER_ADDRESS.getName(),
												requestMap.get(FieldType.PAYER_ADDRESS.getName()));
							}
						}

						User user2 = userDao.findPayId(requestMap.get(FieldType.PAY_ID.getName()));
						if (StringUtils.isNotBlank(user2.getSuperMerchantId())) {
							User user1 = userDao.findPayId(user2.getSuperMerchantId());
							if (StringUtils.isNotBlank(user1.getResellerId())) {
								requestMap.put(FieldType.RESELLER_ID.getName(), user1.getResellerId());
							}
						} else {
							if (StringUtils.isNotBlank(user2.getResellerId())) {
								requestMap.put(FieldType.RESELLER_ID.getName(), user2.getResellerId());
							}
						}

						Bson newValue;
						if (requestMap.containsKey(FieldType.RESELLER_ID.getName())) {
							newValue = new Document(FieldType.BENE_REGISTRATION.getName(), "Active").append(
									FieldType.RESELLER_ID.getName(), requestMap.get(FieldType.RESELLER_ID.getName()));
						} else {
							newValue = new Document(FieldType.BENE_REGISTRATION.getName(), "Active");
						}

						Bson updateOperationDocument = new Document("$set", newValue);
						coll.updateOne(filter, updateOperationDocument);

						res.put(FieldType.RESPONSE_MESSAGE.getName(), "Beneficiary has been added successfully!");
						res.put("Flag", "true");

					} else {
						if (StringUtils.isNotBlank(requestMap.get(FieldType.BENE_ACCOUNT_NO.getName()))) {
							res.put(FieldType.RESPONSE_MESSAGE.getName(), "This Account number is already exist");
							res.put("Flag", "true");
							res.put("FlagStatus", "false");
						} else {
							res.put(FieldType.RESPONSE_MESSAGE.getName(), "This VPA is already exist");
							res.put("Flag", "true");
							res.put("FlagStatus", "false");
						}
					}
				}
			}

			cursor.close();

			MongoCursor<Document> cursor1 = coll.find(conditionQuery).iterator();

			if (!flag) {
				while (cursor1.hasNext()) {
					Document dbobj1 = cursor1.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

						dbobj1 = dataEncDecTool.decryptDocument(dbobj1);
					}

					String status = dbobj1.getString(FieldType.BENE_REGISTRATION.getName());
					flag = true;
					if (dbobj1.getString(FieldType.STATUS.getName()).equalsIgnoreCase("SUCCESS")) {
						Document doc = new Document();

						if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
							doc.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
							doc.put(FieldType.SUB_MERCHANT_ID.getName(), requestMap.get(FieldType.PAY_ID.getName()));
							User user1 = userDao.findPayId(user.getSuperMerchantId());
							if (StringUtils.isNotBlank(user1.getResellerId())) {
								doc.put(FieldType.RESELLER_ID.getName(), user1.getResellerId());
							}
						} else {
							doc.put(FieldType.PAY_ID.getName(), requestMap.get(FieldType.PAY_ID.getName()));
							if (StringUtils.isNotBlank(user.getResellerId())) {
								doc.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
							}
						}

						if (StringUtils.isNotBlank(dbobj1.getString(FieldType.BENE_ACCOUNT_NO.getName()))) {
							doc.put(FieldType.BENE_ACCOUNT_NO.getName(),
									dbobj1.getString(FieldType.BENE_ACCOUNT_NO.getName()));
							doc.put(FieldType.IFSC_CODE.getName(), dbobj1.getString(FieldType.IFSC_CODE.getName()));
							doc.put(FieldType.TXNTYPE.getName(), "IMPS");
						} else {
							doc.put(FieldType.PAYER_ADDRESS.getName(),
									dbobj1.getString(FieldType.PAYER_ADDRESS.getName()));
							doc.put(FieldType.TXNTYPE.getName(), "UPI");
						}

						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
						// String autoOrderId = "LP" + sdf.format(new Date());
						doc.put(FieldType.ORDER_ID.getName(), requestMap.get(FieldType.ORDER_ID.getName()));
						doc.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());
						Date dNow = new Date();
						String dateNow = DateCreater.formatDateForDb(dNow);
						doc.put(FieldType.CREATE_DATE.getName(), dateNow);
						doc.put("DATE_INDEX", dateNow.substring(0, 10).replace("-", ""));
						doc.put(FieldType.BENE_REGISTRATION.getName(), "Active");
						doc.put(FieldType.STATUS.getName(), dbobj1.getString(FieldType.STATUS.getName()));
						doc.put(FieldType.PHONE_NO.getName(), requestMap.get(FieldType.PHONE_NO.getName()));
						if (StringUtils.isNotBlank(requestMap.get(FieldType.EMAIL.getName()))) {
							doc.put(FieldType.EMAIL.getName(), requestMap.get(FieldType.EMAIL.getName()));
						}
						doc.put(FieldType.RESPONSE_CODE.getName(), dbobj1.getString(FieldType.RESPONSE_CODE.getName()));
						if (StringUtils.isNotBlank(dbobj1.getString(FieldType.PG_RESPONSE_MSG.getName()))) {
							doc.put(FieldType.PG_RESPONSE_MSG.getName(),
									dbobj1.getString(FieldType.PG_RESPONSE_MSG.getName()));
						}
						if (StringUtils.isNotBlank(dbobj1.getString(FieldType.RESPONSE_MESSAGE.getName()))) {
							doc.put(FieldType.RESPONSE_MESSAGE.getName(),
									dbobj1.getString(FieldType.RESPONSE_MESSAGE.getName()));
						}
						coll.insertOne(doc);
					}
					res.put(FieldType.RESPONSE_MESSAGE.getName(), "Beneficiary has been added successfully!");
					res.put("Flag", "true");
					break;
				}
			}

			if (!flag) {
				res.put("Flag", "false");
			}
			return res;
		} catch (Exception e) {
			logger.error("Exception occured in AddBene , Exception = ", e);
		}
		return res;
	}

	public void insertImpsUPIBulkDataForPurpose(ImpsDownloadObject impsData) {
		logger.info("Inside insertImpsBulkDataForPurpose()");
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
		try {
			Document doc = new Document();
			doc.put(FieldType.ORDER_ID.getName(), impsData.getOrderId());
			doc.put(FieldType.TXN_ID.getName(), TransactionManager.getId());
			if (StringUtils.isBlank(impsData.getBankAccountNumber())) {
				doc.put(FieldType.PAYER_ADDRESS.getName(), impsData.getPayerAddress());
				doc.put(FieldType.PAYER_NAME.getName(), impsData.getPayerName());
			} else {
				doc.put(FieldType.BENE_NAME.getName(), impsData.getBeneAccountName());
				doc.put(FieldType.BENE_ACCOUNT_NO.getName(), impsData.getBankAccountNumber());
				doc.put(FieldType.BANK_NAME.getName(), impsData.getBankAccountName());
				doc.put(FieldType.IFSC_CODE.getName(), impsData.getBankIFSC());
			}
			doc.put(FieldType.PHONE_NO.getName(), impsData.getPhoneNo());
			doc.put(FieldType.AMOUNT.getName(), impsData.getAmount());
			doc.put(FieldType.REMARKS.getName(), impsData.getRemarks());
			doc.put(FieldType.VIRTUAL_AC_CODE.getName(), impsData.getVirtualAccount());
			doc.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			doc.put(FieldType.PURPOSE.getName(), impsData.getPurpose());
			doc.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYOUT_PURPOSE.getResponseMessage());
			doc.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_PAYOUT_PURPOSE.getResponseMessage());
			doc.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYOUT_PURPOSE.getCode());
			doc.put(FieldType.IS_STATUS_FINAL.getName(), "Y");
			doc.put(FieldType.TXNTYPE.getName(), impsData.getTxnType());
			doc.put(FieldType.USER_TYPE.getName(), "Merchant Initiated Direct");
			if (StringUtils.isNotBlank(impsData.getSubMerchant())) {
				doc.put(FieldType.SUB_MERCHANT_ID.getName(), impsData.getSubMerchant());
			}
			logger.info("insert");
			if (StringUtils.isNotBlank(impsData.getMerchantPayId())) {
				doc.put(FieldType.PAY_ID.getName(), impsData.getMerchantPayId());
			}
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			doc.put(FieldType.CREATE_DATE.getName(), dateNow);
			doc.put("DATE_INDEX", dateNow.substring(0, 10).replace("-", ""));
			doc.put(FieldType.UPDATE_DATE.getName(), dateNow);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				coll.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				coll.insertOne(doc);
			}
			// coll.insertOne(doc);

		} catch (Exception e) {
			logger.error("Exception cought by Inserting data for purpose in case of bulk, ", e);
		}
	}

	public void updateStatus(String updatePayId, String updateSubmerchantId, String amount, String txnId, String status,
			String updateStatus, String updateRrn, String dateFrom) {
		logger.info("Inside updateStatus()");
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			String dateIndex = dateFrom.substring(0, 10).replace("-", "");
			Bson filter;
			if (StringUtils.isNotBlank(updateSubmerchantId)) {
				filter = new Document(FieldType.SUB_MERCHANT_ID.getName(), updateSubmerchantId)
						.append(FieldType.PAY_ID.getName(), updatePayId)
						.append(FieldType.DATE_INDEX.getName(), dateIndex).append(FieldType.TXN_ID.getName(), txnId);
			} else {
				filter = new Document(FieldType.PAY_ID.getName(), updatePayId)
						.append(FieldType.DATE_INDEX.getName(), dateIndex).append(FieldType.TXN_ID.getName(), txnId);
			}
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			Bson newValue;
			if (StringUtils.isNotBlank(updateRrn)) {
				logger.info("Updated RRN = " + updateRrn);
				if (updateStatus.equalsIgnoreCase("Captured")) {
					newValue = new Document(FieldType.RRN.getName(), updateRrn)
							.append(FieldType.STATUS.getName(), updateStatus)
							.append(FieldType.UPDATE_DATE.getName(), dateNow)
							.append(FieldType.RESPONSE_CODE.getName(), "000")
							.append(FieldType.RESPONSE_MESSAGE.getName(), "Transaction Successful")
							.append(FieldType.PG_TXN_MESSAGE.getName(), "Transaction Successful")
							.append(FieldType.IS_STATUS_FINAL.getName(), "Y");
				} else {
					newValue = new Document(FieldType.RRN.getName(), updateRrn)
							.append(FieldType.STATUS.getName(), updateStatus)
							.append(FieldType.UPDATE_DATE.getName(), dateNow)
							.append(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode())
							.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage())
							.append(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage())
							.append(FieldType.IS_STATUS_FINAL.getName(), "Y");
				}
			} else {
				if (updateStatus.equalsIgnoreCase("Captured")) {
					newValue = new Document(FieldType.STATUS.getName(), updateStatus)
							.append(FieldType.UPDATE_DATE.getName(), dateNow)
							.append(FieldType.RESPONSE_CODE.getName(), "000")
							.append(FieldType.RESPONSE_MESSAGE.getName(), "Transaction Successful")
							.append(FieldType.PG_TXN_MESSAGE.getName(), "Transaction Successful")
							.append(FieldType.IS_STATUS_FINAL.getName(), "Y");
				} else {
					newValue = new Document(FieldType.STATUS.getName(), updateStatus)
							.append(FieldType.UPDATE_DATE.getName(), dateNow)
							.append(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode())
							.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage())
							.append(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage())
							.append(FieldType.IS_STATUS_FINAL.getName(), "Y");
				}
			}
			logger.info("Status = " + status);
			logger.info("Updated Status = " + updateStatus);
			Bson updateOperationDocument = new Document("$set", newValue);
			coll.updateOne(filter, updateOperationDocument);
			logger.info("Update Status final request" + newValue);
			
			
			MongoCursor<Document> cursor = coll.find(filter).iterator();
			if (cursor.hasNext()) {
				Document dbobj = cursor.next();

				//sending Callback to merchant
				sendCallbackToMerchant(dbobj);
			}
			
		} catch (Exception e) {
			logger.error("Exception cought by updateStatus, ", e);
		}
	}

	private void sendCallbackToMerchant(Document doc) {
		
		logger.info("inside sendCallbackToMerchant(), fields ", doc);
		try {
			String payId;
			
			if(doc==null){
				logger.info("empty document merchant callback not sending, sendCallbackToMerchant() ");
				return ; 
			}
			
			if(StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))){
				payId=doc.getString(FieldType.SUB_MERCHANT_ID.getName());
			}else{
				payId=doc.getString(FieldType.PAY_ID.getName());
			}
			
			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);

			if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
				logger.info("sending callback to merchant on URL " + userSetting.getPayoutCallbackUrl() + " payId "
						+ doc.getString(FieldType.PAY_ID.getName()) + " Txn id " + doc.getString(FieldType.TXN_ID.getName()));
				
				String amount=doc.getString(FieldType.AMOUNT.getName());
				
				if(amount.contains(".")){
					amount=Amount.formatAmount(amount, doc.getString(FieldType.CURRENCY_CODE.getName()));
					doc.put(FieldType.AMOUNT.getName(),amount);
				}

				Map<String, String> callbackResponse = new HashMap<String, String>();

				callbackResponse.put(FieldType.RESPONSE_DATE_TIME.getName(),(String) DateCreater.formatDateForDb(new Date()));
				callbackResponse.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
				callbackResponse.put(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName()));
				callbackResponse.put(FieldType.TXN_ID.getName(), doc.getString(FieldType.TXN_ID.getName()));
				
				if(StringUtils.isNotBlank(doc.getString(FieldType.PG_RESP_CODE.getName()))){
					callbackResponse.put(FieldType.PG_RESP_CODE.getName(), doc.getString(FieldType.PG_RESP_CODE.getName()));
				}
				if(StringUtils.isNotBlank(doc.getString(FieldType.RRN.getName()))){
					callbackResponse.put(FieldType.RRN.getName(), doc.getString(FieldType.RRN.getName()));
				}
				if(StringUtils.isNotBlank(doc.getString(FieldType.PG_TXN_MESSAGE.getName()))){
					callbackResponse.put(FieldType.PG_TXN_MESSAGE.getName(), doc.getString(FieldType.PG_TXN_MESSAGE.getName()));
				}
				if(StringUtils.isNotBlank(doc.getString(FieldType.UTR.getName()))){
					callbackResponse.put(FieldType.UTR.getName(), doc.getString(FieldType.UTR.getName()));
				}

				callbackResponse.put(FieldType.CURRENCY_CODE.getName(), doc.getString(FieldType.CURRENCY_CODE.getName()));
				callbackResponse.put(FieldType.TXNTYPE.getName(), doc.getString(FieldType.TXNTYPE.getName()));
				callbackResponse.put(FieldType.STATUS.getName(), doc.getString(FieldType.STATUS.getName()));
				callbackResponse.put(FieldType.RESPONSE_CODE.getName(), doc.getString(FieldType.RESPONSE_CODE.getName()));
				callbackResponse.put(FieldType.RESPONSE_MESSAGE.getName(), doc.getString(FieldType.RESPONSE_MESSAGE.getName()));
				callbackResponse.put(FieldType.AMOUNT.getName(), amount);

				callbackResponse.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(callbackResponse)));

				logger.info("Final Callback Response to Merchant >> "+callbackResponse);
				
				transactionControllerServiceProvider.callBackforPayoutTransactions(callbackResponse,
						userSetting.getPayoutCallbackUrl());

			}
		} catch (Exception e) {
			logger.info("exception in payoutCallbackToMerchant(), for txnId " + doc.getString(FieldType.TXN_ID.getName())
					+ " , ", e);
		}
		
	}

	public void updateClosingCollection(String updatePayId, String updateSubmerchantId, String amount, String status,
			String updateStatus, String dateFrom, String acqName) {
		logger.info("Inside updateClosingCollection()");
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
			BasicDBObject paramConditionLst = new BasicDBObject();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject acqNameQuery = new BasicDBObject();
			if (StringUtils.isNotBlank(updatePayId)) {
				paramConditionLst.append(FieldType.PAY_ID.getName(), updatePayId);
			}
			if (StringUtils.isNotBlank(updateSubmerchantId)) {
				paramConditionLst.append(FieldType.SUB_MERCHANT_ID.getName(), updateSubmerchantId);
			}
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			String currentDate = dateFormat.format(cal.getTime());

			SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat dateFormatt = new SimpleDateFormat("yyyy-MM-dd");
			String fromDate = dateFormatt.format(inputDate.parse(dateFrom));
			String toDate = dateFormatt.format(inputDate.parse(currentDate));
			String froDate = fromDate + " 00:00:00";
			String tDate = toDate + " 23:59:59";
			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(froDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(tDate).toLocalizedPattern()).get());
			
			acqNameQuery.put(FieldType.ACQUIRER_NAME.getName(), acqName);
			
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!paramConditionLst.isEmpty()) {
				fianlList.add(paramConditionLst);
			}

			if (!dateQuery.isEmpty()) {
				fianlList.add(dateQuery);
			}
			
			if (!acqNameQuery.isEmpty()) {
				fianlList.add(acqNameQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", 1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			boolean flag = false;
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				logger.info("orginal request for closing: virtual account code = "
						+ dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()) + " Request ," + dbobj);
				if (!flag) {
					if (((status.equalsIgnoreCase("Captured")) && !(updateStatus.equalsIgnoreCase("Captured")))
							|| ((status.equalsIgnoreCase("Timeout")) && !(updateStatus.equalsIgnoreCase("Timeout")))
							|| ((status.equalsIgnoreCase(StatusType.PROCESSING.getName())) 
									&& !(updateStatus.equalsIgnoreCase(StatusType.PROCESSING.getName())))) {
						flag = true;
						Bson filter;
						if (StringUtils.isNotBlank(updateSubmerchantId)) {
							filter = new Document(FieldType.SUB_MERCHANT_ID.getName(), updateSubmerchantId)
									.append(FieldType.PAY_ID.getName(), updatePayId)
									.append(FieldType.DATE_INDEX.getName(),
											dbobj.getString(FieldType.CREATE_DATE.getName()).substring(0, 10)
													.replace("-", "")).append(FieldType.ACQUIRER_NAME.getName(), dbobj.get(FieldType.ACQUIRER_NAME.getName()));
						} else {
							filter = new Document(FieldType.PAY_ID.getName(), updatePayId).append(
									FieldType.DATE_INDEX.getName(),
									dbobj.getString(FieldType.CREATE_DATE.getName()).substring(0, 10).replace("-", ""))
									.append(FieldType.ACQUIRER_NAME.getName(), dbobj.get(FieldType.ACQUIRER_NAME.getName()));
						}
						BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName()));
						dbAmount = dbAmount.add(new BigDecimal(amount));
						BigDecimal dbDebitAmount = new BigDecimal(dbobj.getString(FieldType.DEBIT_AMOUNT.getName()));
						dbDebitAmount = dbDebitAmount.subtract(new BigDecimal(amount));
						Bson newValue = new Document(FieldType.AMOUNT.getName(), dbAmount.toString())
								.append(FieldType.DEBIT_AMOUNT.getName(), dbDebitAmount.toString())
								.append(FieldType.UPDATE_DATE.getName(), currentDate);
						Bson updateOperationDocument = new Document("$set", newValue);
						logger.info("update request for closing: virtual account code = "
								+ dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()) + "update value, "
								+ newValue.toString());
						coll.updateOne(filter, updateOperationDocument);
					} else if ((!(status.equalsIgnoreCase("Captured")) && (updateStatus.equalsIgnoreCase("Captured")))
							|| (!(status.equalsIgnoreCase("Timeout")) && (updateStatus.equalsIgnoreCase("Timeout")))
							|| (!(status.equalsIgnoreCase(StatusType.PROCESSING.getName())) && (updateStatus.equalsIgnoreCase(StatusType.PROCESSING.getName())))) {
						flag = true;
						Bson filter;
						if (StringUtils.isNotBlank(updateSubmerchantId)) {
							filter = new Document(FieldType.SUB_MERCHANT_ID.getName(), updateSubmerchantId)
									.append(FieldType.PAY_ID.getName(), updatePayId)
									.append(FieldType.DATE_INDEX.getName(),
											dbobj.getString(FieldType.CREATE_DATE.getName()).substring(0, 10)
													.replace("-", ""))
									.append(FieldType.ACQUIRER_NAME.getName(), dbobj.get(FieldType.ACQUIRER_NAME.getName()));
						} else {
							filter = new Document(FieldType.PAY_ID.getName(), updatePayId).append(
									FieldType.DATE_INDEX.getName(),
									dbobj.getString(FieldType.CREATE_DATE.getName()).substring(0, 10).replace("-", ""))
									.append(FieldType.ACQUIRER_NAME.getName(), dbobj.get(FieldType.ACQUIRER_NAME.getName()));
						}
						BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName()));
						BigDecimal updateAmount = dbAmount.subtract(new BigDecimal(amount));
						BigDecimal dbDebitAmount = new BigDecimal(dbobj.getString(FieldType.DEBIT_AMOUNT.getName()));
						dbDebitAmount = dbDebitAmount.add(new BigDecimal(amount));
						Bson newValue = new Document(FieldType.AMOUNT.getName(), updateAmount.toString())
								.append(FieldType.DEBIT_AMOUNT.getName(), dbDebitAmount.toString())
								.append(FieldType.UPDATE_DATE.getName(), currentDate);
						Bson updateOperationDocument = new Document("$set", newValue);
						logger.info("update request for closing: virtual account code = "
								+ dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()) + "update value, "
								+ newValue.toString());
						coll.updateOne(filter, updateOperationDocument);
					}
				} else {
					if (((status.equalsIgnoreCase("Captured")) && !(updateStatus.equalsIgnoreCase("Captured")))
							|| ((status.equalsIgnoreCase("Timeout")) && !(updateStatus.equalsIgnoreCase("Timeout")))
									|| ((status.equalsIgnoreCase(StatusType.PROCESSING.getName())) 
											&& !(updateStatus.equalsIgnoreCase(StatusType.PROCESSING.getName())))) {
						Bson filter;
						if (StringUtils.isNotBlank(updateSubmerchantId)) {
							filter = new Document(FieldType.SUB_MERCHANT_ID.getName(), updateSubmerchantId)
									.append(FieldType.PAY_ID.getName(), updatePayId)
									.append(FieldType.DATE_INDEX.getName(),
											dbobj.getString(FieldType.CREATE_DATE.getName()).substring(0, 10)
													.replace("-", ""))
									.append(FieldType.ACQUIRER_NAME.getName(), dbobj.get(FieldType.ACQUIRER_NAME.getName()));
						} else {
							filter = new Document(FieldType.PAY_ID.getName(), updatePayId).append(
									FieldType.DATE_INDEX.getName(),
									dbobj.getString(FieldType.CREATE_DATE.getName()).substring(0, 10).replace("-", ""))
									.append(FieldType.ACQUIRER_NAME.getName(), dbobj.get(FieldType.ACQUIRER_NAME.getName()));
						}
						BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName()));
						dbAmount = dbAmount.add(new BigDecimal(amount));
						BigDecimal dbOpeningAmount = new BigDecimal(
								dbobj.getString(FieldType.OPENING_AMOUNT.getName()));
						dbOpeningAmount = dbOpeningAmount.add(new BigDecimal(amount));
						Bson newValue = new Document(FieldType.AMOUNT.getName(), dbAmount.toString())
								.append(FieldType.OPENING_AMOUNT.getName(), dbOpeningAmount.toString())
								.append(FieldType.UPDATE_DATE.getName(), currentDate);
						Bson updateOperationDocument = new Document("$set", newValue);
						logger.info("update request for closing: virtual account code = "
								+ dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()) + "update value, "
								+ newValue.toString());
						coll.updateOne(filter, updateOperationDocument);
					} else if ((!(status.equalsIgnoreCase("Captured")) && (updateStatus.equalsIgnoreCase("Captured")))
							|| (!(status.equalsIgnoreCase("Timeout")) && (updateStatus.equalsIgnoreCase("Timeout")))
							|| (!(status.equalsIgnoreCase(StatusType.PROCESSING.getName())) 
									&& (updateStatus.equalsIgnoreCase(StatusType.PROCESSING.getName())))) {
						Bson filter;
						if (StringUtils.isNotBlank(updateSubmerchantId)) {
							filter = new Document(FieldType.SUB_MERCHANT_ID.getName(), updateSubmerchantId)
									.append(FieldType.PAY_ID.getName(), updatePayId)
									.append(FieldType.DATE_INDEX.getName(),
											dbobj.getString(FieldType.CREATE_DATE.getName()).substring(0, 10)
													.replace("-", ""))
									.append(FieldType.ACQUIRER_NAME.getName(), dbobj.get(FieldType.ACQUIRER_NAME.getName()));
						} else {
							filter = new Document(FieldType.PAY_ID.getName(), updatePayId).append(
									FieldType.DATE_INDEX.getName(),
									dbobj.getString(FieldType.CREATE_DATE.getName()).substring(0, 10).replace("-", ""))
									.append(FieldType.ACQUIRER_NAME.getName(), dbobj.get(FieldType.ACQUIRER_NAME.getName()));
						}
						BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName()));
						dbAmount = dbAmount.subtract(new BigDecimal(amount));
						BigDecimal dbOpeningAmount = new BigDecimal(
								dbobj.getString(FieldType.OPENING_AMOUNT.getName()));
						dbOpeningAmount = dbOpeningAmount.subtract(new BigDecimal(amount));
						Bson newValue = new Document(FieldType.AMOUNT.getName(), dbAmount.toString())
								.append(FieldType.OPENING_AMOUNT.getName(), dbOpeningAmount.toString())
								.append(FieldType.UPDATE_DATE.getName(), currentDate);
						Bson updateOperationDocument = new Document("$set", newValue);
						logger.info("update request for closing: virtual account code = "
								+ dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()) + " update value, "
								+ newValue.toString());
						coll.updateOne(filter, updateOperationDocument);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception cought by updateClosingCollection, ", e);
		}
	}

	public void getUpdateTransactionByTxnId(String txnId, String updateStatus) {
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject finalQuery = new BasicDBObject();

			finalQuery.put(FieldType.TXN_ID.getName(), txnId);
			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", 1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				logger.info("orginal request for update status : Txn Id= " + txnId + "request, " + dbobj);
				try {

					String dateIndex = dbobj.getString(FieldType.CREATE_DATE.getName()).substring(0, 10).replace("-",
							"");
					Bson filter;
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
								dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))
										.append(FieldType.PAY_ID.getName(), dbobj.getString(FieldType.PAY_ID.getName()))
										.append(FieldType.DATE_INDEX.getName(), dateIndex)
										.append(FieldType.TXN_ID.getName(), txnId);
					} else {
						filter = new Document(FieldType.PAY_ID.getName(), dbobj.getString(FieldType.PAY_ID.getName()))
								.append(FieldType.DATE_INDEX.getName(), dateIndex)
								.append(FieldType.TXN_ID.getName(), txnId);
					}
					Date dNow = new Date();
					String dateNow = DateCreater.formatDateForDb(dNow);
					Bson newValue;
					if (updateStatus.equalsIgnoreCase("Captured")) {
						newValue = new Document(FieldType.STATUS.getName(), updateStatus)
								.append(FieldType.UPDATE_DATE.getName(), dateNow)
								.append(FieldType.RESPONSE_CODE.getName(), "000")
								.append(FieldType.RESPONSE_MESSAGE.getName(), "Transaction Successful")
								.append(FieldType.PG_TXN_MESSAGE.getName(), "Transaction Successful")
								.append(FieldType.IS_STATUS_FINAL.getName(), "Y");
					} else {
						newValue = new Document(FieldType.STATUS.getName(), updateStatus)
								.append(FieldType.UPDATE_DATE.getName(), dateNow)
								.append(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode())
								.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage())
								.append(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage())
								.append(FieldType.IS_STATUS_FINAL.getName(), "Y");
					}

					Bson updateOperationDocument = new Document("$set", newValue);
					logger.info("update request for update status : Txn Id= " + txnId + "update request, "
							+ newValue.toString());
					coll.updateOne(filter, updateOperationDocument);
				} catch (Exception e) {
					logger.error("Exception cought by updateStatus, ", e);
				}
				String status = "";
				String dateFrom = "";
				String amount = "";
				String updatePayId = "";
				String updateSubmerchantId = "";
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.getName()))) {
					status = dbobj.getString(FieldType.STATUS.getName());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CREATE_DATE.getName()))) {
					dateFrom = dbobj.getString(FieldType.CREATE_DATE.getName());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.AMOUNT.getName()))) {
					amount = dbobj.getString(FieldType.AMOUNT.getName());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAY_ID.getName()))) {
					updatePayId = dbobj.getString(FieldType.PAY_ID.getName());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
					updateSubmerchantId = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
				}
				if (!(status.equalsIgnoreCase(updateStatus))) {
					if ((!(status.equalsIgnoreCase("Timeout") || status.equalsIgnoreCase(StatusType.PROCESSING.getName()))
							&& updateStatus.equalsIgnoreCase("Captured"))
							|| (status.equalsIgnoreCase("Captured") && !updateStatus.equalsIgnoreCase("Timeout"))){
						logger.info("Updated closing Collection for select All ,Pay Id : " + updatePayId
								+ "SubMerchant Id : " + updateSubmerchantId + "dateFrom : " + dateFrom + "Status : "
								+ status);
						updateClosingCollection(updatePayId, updateSubmerchantId, amount, status, updateStatus,
								dateFrom, dbobj.getString(FieldType.ACQUIRER_NAME.getName()));
					} else {
						if (((status.equalsIgnoreCase("Timeout") || status.equalsIgnoreCase(StatusType.PROCESSING.getName()) 
								&& !updateStatus.equalsIgnoreCase("Captured"))
								|| (!status.equalsIgnoreCase("Captured") && updateStatus.equalsIgnoreCase("Timeout")))) {
							logger.info("Updated closing Collection for select All ,Pay Id : " + updatePayId
									+ "SubMerchant Id : " + updateSubmerchantId + "dateFrom : " + dateFrom + "Status : "
									+ status);
							updateClosingCollection(updatePayId, updateSubmerchantId, amount, status, updateStatus,
									dateFrom, dbobj.getString(FieldType.ACQUIRER_NAME.getName()));
						}
					}
				}
				sendCallbackToMerchant(dbobj);
			}

		} catch (Exception e) {
			logger.info("Exception ", e);

		}
	}

	public String getTransactionAcquirerName(String txnId) {
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject finalQuery = new BasicDBObject();

			finalQuery.put(FieldType.TXN_ID.getName(), txnId);
		
			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			if (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}
				
				return dbobj.getString(FieldType.ACQUIRER_NAME.getName());
			}
		}catch (Exception e) {
			logger.info("Exception in getTransactionAcquirerName()",e);
		}
		
		return null;
	}

	public void insertBulkFileRecordInfo(String newFileName, long totalRecords, long successData, long failedData,
			long duplicateData, String payId, String subMerchantPayId) {
		try{
			
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_UPI_BULK_COLLECTION.getValue()));
			BasicDBObject finalQuery = new BasicDBObject();
			
			Document doc=new Document();
			
			doc.put(FieldType.PAY_ID.getName(), payId);
			
			if(StringUtils.isNotBlank(subMerchantPayId)){
				doc.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId);
			}
			if(StringUtils.isNotBlank(newFileName)){
				doc.put(FieldType.FILENAME.getName(), newFileName);
			}
			doc.put("TOTAL_RECORDS", totalRecords);
			doc.put("TOTAL_SUCCESS", successData);
			doc.put("TOTAL_FAILED", failedData);
			doc.put("TOTAL_DUPLICATE", duplicateData);
			doc.put(FieldType.CREATE_DATE.getName(), DateCreater.formatDateForDb(new Date()));
			
			coll.insertOne(doc);
			
		}catch (Exception e) {
			logger.info("exception in insertBulkFileRecordInfo ",e);
		}
		
	}
}
