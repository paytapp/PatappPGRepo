package com.paymentgateway.crm.mongoReports;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mchange.io.IOSequentialByteArrayMap.Cursor;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.TransactionReconSearch;
import com.paymentgateway.commons.user.TransactionSearchDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Component
public class ReconTxnReports {

	private static Logger logger = LoggerFactory.getLogger(ReconTxnReports.class.getName());
	private static final String alphabaticFileName = "alphabatic-currencycode.properties";
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private UserDao userdao;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;

	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchPayment(String reservationId, String banktxnId, String sid,
			String acquirer, String transactionType, String status, String fromDate, String toDate, int start,
			int length, String operationFlag) {

		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();
		logger.info("Inside TxnReconReports , searchPayment");
		boolean isParameterised = false;
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();

			String currentDate = null;
			if (!fromDate.isEmpty()) {
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				if (StringUtils.isNotBlank(operationFlag) && operationFlag.equalsIgnoreCase("Settled")) {
					dateQuery.put(FieldType.SETTLEMENT_DATE.getName(),
							BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
									.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
				} else {
					dateQuery.put(FieldType.CREATE_DATE.getName(),
							BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
									.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
				}

			}

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(fromDate).toLocalizedPattern();
			String endString = new SimpleDateFormat(currentDate).toLocalizedPattern();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(startString);
			Date dateEnd = format.parse(endString);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}

			BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {

				if (StringUtils.isNotBlank(operationFlag) && operationFlag.equalsIgnoreCase("Settled")) {
					dateIndexConditionQuery.append("SETTLEMENT_DATE_INDEX", dateIndexIn);
				} else {
					dateIndexConditionQuery.append("DATE_INDEX", dateIndexIn);
				}

			}

			if (!reservationId.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject("RESERVATION_ID", reservationId));
			}
			if (!banktxnId.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject("BANK_TXN_NUMBER", banktxnId));
			}

			if (!transactionType.isEmpty() && !transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject("TXNTYPE", transactionType));
			}

			if (!acquirer.isEmpty() && !acquirer.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject("ACQUIRER", acquirer));
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {

				if (status.equalsIgnoreCase("Captured")) {
					paramConditionLst.add(new BasicDBObject("STATUS", "Captured"));
				}

				else if (status.equalsIgnoreCase("Settled")) {
					paramConditionLst.add(new BasicDBObject("SETTLEMENT_FLAG", "Y"));
				}

				else if (status.equalsIgnoreCase("Captured and Settled")) {
					paramConditionLst.add(new BasicDBObject("STATUS", "Captured"));
					paramConditionLst.add(new BasicDBObject("SETTLEMENT_FLAG", "Y"));
				}

				else if (status.equalsIgnoreCase("Captured-Not Settled")) {
					paramConditionLst.add(new BasicDBObject("STATUS", "Captured"));
					paramConditionLst.add(new BasicDBObject("SETTLEMENT_FLAG", "N"));
				} else if (status.equalsIgnoreCase("Post Settle Captured")) {
					paramConditionLst.add(new BasicDBObject("POST_SETTLED_FLAG", "Y"));
				}else if (status.equalsIgnoreCase("Amount Mismatch")) {
					paramConditionLst.add(new BasicDBObject("AMOUNT_MISMATCH", "Y"));
				}
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (isParameterised) {

			} else {
				if (!dateQuery.isEmpty()) {
					allConditionQueryList.add(dateQuery);
				}
				if (!dateIndexConditionQuery.isEmpty()) {
					allConditionQueryList.add(dateIndexConditionQuery);
				}
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}

			logger.info("Inside TxnReports , searchPaymentCount , fianlList = " + fianlList);
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			BasicDBObject skip = new BasicDBObject("$skip", start);
			BasicDBObject limit = new BasicDBObject("$limit", length);
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, skip, limit);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {

				Document doc = cursor.next();
				TransactionReconSearch trs = new TransactionReconSearch();

				if (doc.get("CREATE_DATE") != null) {

					trs.setCreateDate(doc.get("CREATE_DATE").toString().substring(0, 10));
				} else {
					trs.setCreateDate("");
				}

				if (doc.get("RESERVATION_ID") != null) {

					trs.setReservationId(doc.get("RESERVATION_ID").toString());
				} else {
					trs.setReservationId("");
				}

				if (doc.get("RESERVATION_ID") != null) {

					trs.setReservationId(doc.get("RESERVATION_ID").toString());
				} else {
					trs.setReservationId("");
				}

				if (doc.get("BANK_TXN_NUMBER") != null) {

					trs.setBankTxnId(doc.get("BANK_TXN_NUMBER").toString());
				} else {
					trs.setBankTxnId("");
				}

				if (doc.get("SID") != null) {

					trs.setSid(doc.get("SID").toString());
				} else {
					trs.setSid("");
				}

				if (doc.get("AMOUNT") != null) {

					trs.setAmount(doc.get("AMOUNT").toString());
				} else {
					//trs.setAmount("");
					if (doc.get("MPR_AMOUNT") != null) {

						trs.setAmount(doc.get("MPR_AMOUNT").toString());
					} else {
						trs.setAmount("");
					}
				}
				
				if (doc.get("MPR_AMOUNT") != null) {

					trs.setMprAmount(doc.get("MPR_AMOUNT").toString());
				} else {
					trs.setMprAmount("");
				}

				if (doc.get("STATUS") != null) {

					trs.setStatus(doc.get("STATUS").toString());
				} else {
					trs.setStatus("");
				}

				if (doc.get("TXNTYPE") != null) {

					trs.setTxnType(doc.get("TXNTYPE").toString());
				} else {
					trs.setTxnType("");
				}

				if (doc.get("SETTLEMENT_FLAG") != null) {

					trs.setSettlementFlag(doc.get("SETTLEMENT_FLAG").toString());
				} else {
					trs.setSettlementFlag("");
				}

				if (doc.get("SETTLEMENT_DATE") != null) {

					trs.setSettlementDate(doc.get("SETTLEMENT_DATE").toString().substring(0, 10));
				} else {
					trs.setSettlementDate("");
				}

				if (doc.get("POST_SETTLED_FLAG") != null) {

					trs.setPostSettledFlag(doc.get("POST_SETTLED_FLAG").toString());
				} else {
					trs.setPostSettledFlag("");
				}

				if (doc.get("ACQUIRER") != null) {

					trs.setAcquirer(doc.get("ACQUIRER").toString());
				} else {
					trs.setAcquirer("");
				}

				if (doc.get("POST_SETTLE_CAPTURE") != null) {

					trs.setPostSettledCapture(doc.get("POST_SETTLE_CAPTURE").toString());
				} else {
					trs.setPostSettledCapture("");
				}

				txnList.add(trs);

			}
			return txnList;

		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchPayment n exception = " , e);
			return txnList;
		}
	}

	@SuppressWarnings("static-access")
	public int searchPaymentCount(String reservationId, String banktxnId, String sid, String acquirer,
			String transactionType, String status, String fromDate, String toDate, String operationFlag) {

		logger.info("Inside TxnReconReports , searchPaymentCount");
		boolean isParameterised = false;
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();

			String currentDate = null;
			if (!fromDate.isEmpty()) {
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				/*
				 * dateQuery.put(FieldType.CREATE_DATE.getName(),
				 * BasicDBObjectBuilder.start("$gte", new
				 * SimpleDateFormat(fromDate).toLocalizedPattern()) .add("$lte", new
				 * SimpleDateFormat(currentDate).toLocalizedPattern()).get());
				 */
				if (StringUtils.isNotBlank(operationFlag) && operationFlag.equalsIgnoreCase("Settled")) {
					dateQuery.put(FieldType.SETTLEMENT_DATE.getName(),
							BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
									.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
				} else {
					dateQuery.put(FieldType.CREATE_DATE.getName(),
							BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
									.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
				}
			}

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(fromDate).toLocalizedPattern();
			String endString = new SimpleDateFormat(currentDate).toLocalizedPattern();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(startString);
			Date dateEnd = format.parse(endString);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}

			BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndexConditionQuery.append("DATE_INDEX", dateIndexIn);
			}

			if (!reservationId.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject("RESERVATION_ID", reservationId));
			}
			if (!banktxnId.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject("BANK_TXN_NUMBER", banktxnId));
			}

			if (!acquirer.isEmpty() && !acquirer.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject("ACQUIRER", acquirer));
			}

			if (!transactionType.isEmpty() && !transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject("TXNTYPE", transactionType));
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {

				if (status.equalsIgnoreCase("Captured")) {
					paramConditionLst.add(new BasicDBObject("STATUS", "Captured"));
				}

				else if (status.equalsIgnoreCase("Settled")) {
					paramConditionLst.add(new BasicDBObject("SETTLEMENT_FLAG", "Y"));
				}

				else if (status.equalsIgnoreCase("Captured and Settled")) {
					paramConditionLst.add(new BasicDBObject("STATUS", "Captured"));
					paramConditionLst.add(new BasicDBObject("SETTLEMENT_FLAG", "Y"));
				}

				else if (status.equalsIgnoreCase("Captured-Not Settled")) {
					paramConditionLst.add(new BasicDBObject("STATUS", "Captured"));
					paramConditionLst.add(new BasicDBObject("SETTLEMENT_FLAG", "N"));
				} else if (status.equalsIgnoreCase("Post Settle Captured")) {
					paramConditionLst.add(new BasicDBObject("POST_SETTLED_FLAG", "Y"));
				}
				else if (status.equalsIgnoreCase("Amount Mismatch")) {
					paramConditionLst.add(new BasicDBObject("AMOUNT_MISMATCH", "Y"));
				}
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (isParameterised) {

			} else {
				if (!dateQuery.isEmpty()) {
					allConditionQueryList.add(dateQuery);
				}
				if (!dateIndexConditionQuery.isEmpty()) {
					allConditionQueryList.add(dateIndexConditionQuery);
				}
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}

			logger.info("Inside TxnReports , searchPaymentCount , fianlList = " + fianlList);
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			long count = coll.countDocuments(finalquery);
			return (Integer.valueOf(String.valueOf(count)));

		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPaymentCount n exception = " , e);
			return 0;
		}
	}

	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> downloadRecon(String reservationId, String banktxnId, String sid,
			String acquirer, String transactionType, String status, String fromDate, String toDate) {

		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();
		logger.info("Inside TxnReconReports , searchPayment");
		boolean isParameterised = false;
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();

			String currentDate = null;
			if (!fromDate.isEmpty()) {
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

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(fromDate).toLocalizedPattern();
			String endString = new SimpleDateFormat(currentDate).toLocalizedPattern();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(startString);
			Date dateEnd = format.parse(endString);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}

			BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndexConditionQuery.append("DATE_INDEX", dateIndexIn);
			}

			if (!reservationId.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject("RESERVATION_ID", reservationId));
			}
			if (!banktxnId.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject("BANK_TXN_NUMBER", banktxnId));
			}

			if (!acquirer.isEmpty() && !acquirer.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject("ACQUIRER", acquirer));
			}

			if (!transactionType.isEmpty() && !transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject("TXNTYPE", transactionType));
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {

				if (status.equalsIgnoreCase("Captured")) {
					paramConditionLst.add(new BasicDBObject("STATUS", "Captured"));
				}

				else if (status.equalsIgnoreCase("Settled")) {
					paramConditionLst.add(new BasicDBObject("SETTLEMENT_FLAG", "Y"));
				}

				else if (status.equalsIgnoreCase("Captured and Settled")) {
					paramConditionLst.add(new BasicDBObject("STATUS", "Captured"));
					paramConditionLst.add(new BasicDBObject("SETTLEMENT_FLAG", "Y"));
				}

				else if (status.equalsIgnoreCase("Captured-Not Settled")) {
					paramConditionLst.add(new BasicDBObject("STATUS", "Captured"));
					paramConditionLst.add(new BasicDBObject("SETTLEMENT_FLAG", "N"));
				} else if (status.equalsIgnoreCase("Post Settle Captured")) {
					paramConditionLst.add(new BasicDBObject("POST_SETTLED_FLAG", "Y"));
				}
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (isParameterised) {

			} else {
				if (!dateQuery.isEmpty()) {
					allConditionQueryList.add(dateQuery);
				}
				if (!dateIndexConditionQuery.isEmpty()) {
					allConditionQueryList.add(dateIndexConditionQuery);
				}
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}

			logger.info("Inside TxnReports , searchPaymentCount , fianlList = " + fianlList);
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {

				Document doc = cursor.next();
				TransactionReconSearch trs = new TransactionReconSearch();

				if (doc.get("CREATE_DATE") != null) {

					trs.setCreateDate(doc.get("CREATE_DATE").toString().substring(0, 10));
				} else {
					trs.setCreateDate("");
				}

				if (doc.get("RESERVATION_ID") != null) {

					trs.setReservationId(doc.get("RESERVATION_ID").toString());
				} else {
					trs.setReservationId("");
				}

				if (doc.get("RESERVATION_ID") != null) {

					trs.setReservationId(doc.get("RESERVATION_ID").toString());
				} else {
					trs.setReservationId("");
				}

				if (doc.get("BANK_TXN_NUMBER") != null) {

					trs.setBankTxnId(doc.get("BANK_TXN_NUMBER").toString());
				} else {
					trs.setBankTxnId("");
				}

				if (doc.get("SID") != null) {

					trs.setSid(doc.get("SID").toString());
				} else {
					trs.setSid("");
				}

				if (doc.get("AMOUNT") != null) {

					trs.setAmount(doc.get("AMOUNT").toString());
				} else {
					trs.setAmount("");
				}

				if (doc.get("STATUS") != null) {

					trs.setStatus(doc.get("STATUS").toString());
				} else {
					trs.setStatus("");
				}

				if (doc.get("TXNTYPE") != null) {

					trs.setTxnType(doc.get("TXNTYPE").toString());
				} else {
					trs.setTxnType("");
				}

				if (doc.get("SETTLEMENT_FLAG") != null) {

					trs.setSettlementFlag(doc.get("SETTLEMENT_FLAG").toString());
				} else {
					trs.setSettlementFlag("");
				}

				if (doc.get("SETTLEMENT_DATE") != null) {

					trs.setSettlementDate(doc.get("SETTLEMENT_DATE").toString().substring(0, 10));
				} else {
					trs.setSettlementDate("");
				}

				if (doc.get("POST_SETTLED_FLAG") != null) {

					trs.setPostSettledFlag(doc.get("POST_SETTLED_FLAG").toString());
				} else {
					trs.setPostSettledFlag("");
				}

				if (doc.get("ACQUIRER") != null) {

					trs.setAcquirer(doc.get("ACQUIRER").toString());
				} else {
					trs.setAcquirer("");
				}

				if (doc.get("POST_SETTLE_CAPTURE") != null) {

					trs.setPostSettledCapture(doc.get("POST_SETTLE_CAPTURE").toString());
				} else {
					trs.setPostSettledCapture("");
				}

				txnList.add(trs);

			}
			return txnList;

		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , download exception = " , e);
			return txnList;
		}
	}

	@SuppressWarnings("static-access")
	public int searchExceptionsCount(String reservationId, String banktxnId, String sid, String acquirer,
			String transactionType, String fromDate, String toDate) {

		logger.info("Inside ReconTxnReports , searchExceptionsCount");
		boolean isParameterised = false;
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();

			String currentDate = null;
			if (!fromDate.isEmpty()) {
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put("PAYMENT_DATE",
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (!reservationId.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject("RESERVATION_ID", reservationId));
			}
			if (!banktxnId.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject("BANK_TXN_NUMBER", banktxnId));
			}

			if (!acquirer.isEmpty() && !acquirer.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject("ACQUIRER", acquirer));
			}

			if (!transactionType.isEmpty() && !transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject("TXNTYPE", transactionType));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (isParameterised) {

			} else {
				if (!dateQuery.isEmpty()) {
					allConditionQueryList.add(dateQuery);
				}
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}

			logger.info("Inside TxnReports , searchPaymentCount , fianlList = " + fianlList);
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_EXCEPTION_COLLECTION.getValue()));

			long count = coll.countDocuments(finalquery);
			return (Integer.valueOf(String.valueOf(count)));

		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , getExceptions n exception = " , e);
			return 0;
		}
	}

	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchExceptions(String reservationId, String banktxnId, String sid,
			String acquirer, String transactionType, String fromDate, String toDate, int start, int length) {

		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();
		logger.info("Inside TxnReconReports , getExceptions");
		boolean isParameterised = false;
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();

			String currentDate = null;
			if (!fromDate.isEmpty()) {
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put("PAYMENT_DATE",
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (StringUtils.isNotBlank(reservationId)) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject("RESERVATION_ID", reservationId));
			}
			if (StringUtils.isNotBlank(banktxnId)) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject("BANK_TXN_NUMBER", banktxnId));
			}

			if (StringUtils.isNotBlank(transactionType) && !transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject("TXNTYPE", transactionType));
			}

			if (StringUtils.isNotBlank(acquirer) && !acquirer.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject("ACQUIRER", acquirer));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (isParameterised) {

			} else {
				if (!dateQuery.isEmpty()) {
					allConditionQueryList.add(dateQuery);
				}
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}

			logger.info("Inside ReconTxnReports , get exceptiions , fianlList = " + fianlList);
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_EXCEPTION_COLLECTION.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			BasicDBObject skip = new BasicDBObject("$skip", start);
			BasicDBObject limit = new BasicDBObject("$limit", length);
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, skip, limit);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {

				Document doc = cursor.next();
				TransactionReconSearch trs = new TransactionReconSearch();

				if (doc.get("RESERVATION_ID") != null) {

					trs.setReservationId(doc.get("RESERVATION_ID").toString());
				} else {
					trs.setReservationId("");
				}

				if (doc.get("BANK_TXN_NUMBER") != null) {

					trs.setBankTxnId(doc.get("BANK_TXN_NUMBER").toString());
				} else {
					trs.setBankTxnId("");
				}

				if (doc.get("SID") != null) {

					trs.setSid(doc.get("SID").toString());
				} else {
					trs.setSid("");
				}

				if (doc.get("AMOUNT") != null) {

					trs.setFileAmount(doc.get("AMOUNT").toString());
				} else {
					trs.setFileAmount("NA");
				}

				if (doc.get("AMOUNT_MPR") != null) {

					trs.setMprAmount(doc.get("AMOUNT_MPR").toString());
				} else {
					trs.setMprAmount("NA");
				}

				if (doc.get("TXNTYPE") != null) {

					trs.setTxnType(doc.get("TXNTYPE").toString());
				} else {
					trs.setTxnType("NA");
				}

				if (doc.get("STATUS") != null) {

					trs.setStatus(doc.get("STATUS").toString());
				} else {
					trs.setStatus("NA");
				}

				if (doc.get("RESPONSE_MESSAGE") != null) {

					trs.setResponseMessage(doc.get("RESPONSE_MESSAGE").toString());
				} else {
					trs.setResponseMessage("NA");
				}

				if (doc.get("PAYMENT_DATE") != null) {

					trs.setPaymentDate(doc.get("PAYMENT_DATE").toString());
				} else {
					trs.setPaymentDate("");
				}

				if (doc.get("TXN_DATE") != null) {

					trs.setTxnDate(doc.get("TXN_DATE").toString().substring(0, 10));
				} else {
					trs.setTxnDate("NA");
				}

				if (doc.get("FILE_NAME") != null) {

					trs.setFileName(doc.get("FILE_NAME").toString());
				} else {
					trs.setFileName("NA");
				}

				if (doc.get("FILE_TYPE") != null) {

					trs.setFileType(doc.get("FILE_TYPE").toString());
				} else {
					trs.setFileType("NA");
				}

				if (doc.get("ACQUIRER") != null) {

					trs.setAcquirer(doc.get("ACQUIRER").toString());
				} else {
					trs.setAcquirer("");
				}

				txnList.add(trs);

			}
			return txnList;

		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchPayment n exception = " , e);
			return txnList;
		}
	}

	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchSettleSummary(String acquirer, String transactionType, String fromDate,
			String toDate) {

		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , searchSettleSummary");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			List<String> dateList = new ArrayList<>();
			List<String> acquirerList = new ArrayList<String>();

			if (StringUtils.isNotBlank(acquirer) && acquirer.equalsIgnoreCase("ALL")) {

				acquirerList.add("AMEX");
				acquirerList.add("RUPAY");
				acquirerList.add("IPAY");

			} else {
				acquirerList.add(acquirer);
			}

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			while (!incrementingDate.isAfter(endDate)) {
				dateList.add(incrementingDate.toString() + " 12:00:00");
				incrementingDate = incrementingDate.plusDays(1);
			}

			for (String settlementDate : dateList) {

				for (String acq : acquirerList) {

					if (acq.equalsIgnoreCase("IPAY")) {

						// Conditions for IPAY

						List<String> merchantList = new ArrayList<>();
						merchantList.add("IRCTC e ticketing");
						merchantList.add("IRCTC ETicketing APP");

						List<String> bankList = new ArrayList<String>();
						bankList.add("IDBIBANK");
						bankList.add("KOTAK");
						bankList.add("MATCHMOVE");
						bankList.add("YESBANK");

						for (String merchant : merchantList) {

							for (String bank : bankList) {

								BasicDBObject dateIndexQuery = new BasicDBObject("SETTLEMENT_DATE", settlementDate);
								BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", acq);
								BasicDBObject merchantQuery = new BasicDBObject("MERCHANT_NAME", merchant);
								BasicDBObject bankQuery = new BasicDBObject("BANK_NAME", bank);

								List<BasicDBObject> condList = new ArrayList<BasicDBObject>();

								condList.add(acquirerQuery);
								condList.add(dateIndexQuery);
								condList.add(merchantQuery);
								condList.add(bankQuery);

								BasicDBObject finalQuery = new BasicDBObject("$and", condList);

								BasicDBObject projectElement = new BasicDBObject();
								projectElement.put("AMOUNT", 1);
								projectElement.put("TXNTYPE", 1);
								BasicDBObject project = new BasicDBObject("$project", projectElement);

								BasicDBObject match = new BasicDBObject("$match", finalQuery);

								List<BasicDBObject> pipeline = Arrays.asList(match, project);
								AggregateIterable<Document> output = coll.aggregate(pipeline);
								output.allowDiskUse(true);
								MongoCursor<Document> cursor = output.iterator();

								double saleAmount = 0.00;
								double refundAmount = 0.00;

								int saleCount = 0;
								int refundCount = 0;

								while (cursor.hasNext()) {

									Document doc = cursor.next();

									String txnType = doc.getString("TXNTYPE");
									String amount = doc.getString("AMOUNT");

									if (txnType.equalsIgnoreCase("SALE")) {
										saleCount++;
										saleAmount = saleAmount + Double.valueOf(amount);
									} else {
										refundCount++;
										refundAmount = refundAmount + Double.valueOf(amount);
									}
								}

								cursor.close();

								double totalAmount = saleAmount - refundAmount;

								TransactionReconSearch trs = new TransactionReconSearch();
								trs.setAcquirer(acq);
								trs.setSaleCount(String.valueOf(saleCount));
								trs.setSaleAmount(String.format("%.2f", saleAmount));
								trs.setRefundCount(String.valueOf(refundCount));
								trs.setRefundAmount(String.format("%.2f", refundAmount));
								trs.setTotalAmount(String.format("%.2f", totalAmount));
								trs.setMerchantName(merchant);
								trs.setBankName(bank);
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy");
								String settleDate = sdf2.format(sdf.parse(settlementDate));
								trs.setSettlementDate(settleDate);

								txnList.add(trs);

							}

						}

					} else {

						BasicDBObject dateIndexQuery = new BasicDBObject("SETTLEMENT_DATE", settlementDate);
						BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", acq);

						List<BasicDBObject> condList = new ArrayList<BasicDBObject>();

						condList.add(acquirerQuery);
						condList.add(dateIndexQuery);

						BasicDBObject finalQuery = new BasicDBObject("$and", condList);

						BasicDBObject projectElement = new BasicDBObject();
						projectElement.put("AMOUNT", 1);
						projectElement.put("TXNTYPE", 1);
						BasicDBObject project = new BasicDBObject("$project", projectElement);

						BasicDBObject match = new BasicDBObject("$match", finalQuery);

						List<BasicDBObject> pipeline = Arrays.asList(match, project);
						AggregateIterable<Document> output = coll.aggregate(pipeline);
						output.allowDiskUse(true);
						MongoCursor<Document> cursor = output.iterator();

						double saleAmount = 0.00;
						double refundAmount = 0.00;

						int saleCount = 0;
						int refundCount = 0;

						while (cursor.hasNext()) {

							Document doc = cursor.next();

							String txnType = doc.getString("TXNTYPE");
							String amount = doc.getString("AMOUNT");

							if (txnType.equalsIgnoreCase("SALE")) {
								saleCount++;
								saleAmount = saleAmount + Double.valueOf(amount);
							} else {
								refundCount++;
								refundAmount = refundAmount + Double.valueOf(amount);
							}
						}

						cursor.close();

						double totalAmount = saleAmount - refundAmount;

						TransactionReconSearch trs = new TransactionReconSearch();
						trs.setAcquirer(acq);
						trs.setSaleCount(String.valueOf(saleCount));
						trs.setSaleAmount(String.format("%.2f", saleAmount));
						trs.setRefundCount(String.valueOf(refundCount));
						trs.setRefundAmount(String.format("%.2f", refundAmount));
						trs.setTotalAmount(String.format("%.2f", totalAmount));
						trs.setMerchantName("All");
						trs.setBankName("All");
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy");
						String settleDate = sdf2.format(sdf.parse(settlementDate));
						trs.setSettlementDate(settleDate);

						txnList.add(trs);
					}

				}

			}

			return txnList;

		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchSettleSummary ", e);
			return txnList;
		}
	}

	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchAccountSummary(String acquirer, String fromDate, String toDate) {

		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , searchSettleSummary");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			MongoCollection<Document> statementColl = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

			BasicDBObject dateQuery = new BasicDBObject();

			String currentDate = null;
			if (!fromDate.isEmpty()) {
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put("PAYOUT_DATE",
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());

			}

			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			conditionList.add(dateQuery);

			if (StringUtils.isNotBlank(acquirer) && !acquirer.equalsIgnoreCase("All")) {
				BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", acquirer);
				conditionList.add(acquirerQuery);
			}

			BasicDBObject payoutQuery = new BasicDBObject("$and", conditionList);

			FindIterable<Document> payoutItr = statementColl.find(payoutQuery);
			MongoCursor<Document> payoutCursor = payoutItr.iterator();

			while (payoutCursor.hasNext()) {

				TransactionReconSearch trs = new TransactionReconSearch();
				Document doc = payoutCursor.next();

				String acq = doc.getString("ACQUIRER");
				String settlementDate = doc.getString("SETTLEMENT_DATE");
				String payoutDate = doc.getString("PAYOUT_DATE");
				String captureDate = doc.getString("CAPTURE_DATE");
				String narration = doc.getString("BANK_TXN_NUMBER");
				String statementAmount = doc.getString("AMOUNT");

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy");

				String settleDate = sdf2.format(sdf.parse(settlementDate));
				trs.setSettlementDate(settleDate);

				String payoutDateString = sdf2.format(sdf.parse(payoutDate));
				trs.setPaymentDate(payoutDateString);

				if (acq.equalsIgnoreCase("RUPAY")) {

					String captureDateString = sdf2.format(sdf.parse(captureDate));
					trs.setCreateDate(captureDateString);

				} else {
					trs.setCreateDate("NA");
				}

				trs.setAcquirer(acq);

				trs.setNarration(narration);
				trs.setStatementAmount(statementAmount);

				double statementAmt = Double.valueOf(statementAmount);

				if (acq.equalsIgnoreCase("AMEX")) {

					BasicDBObject settlementDateQuery = new BasicDBObject("SETTLEMENT_DATE", settlementDate);
					BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", acq);

					List<BasicDBObject> amexCondList = new ArrayList<BasicDBObject>();
					amexCondList.add(settlementDateQuery);
					amexCondList.add(acquirerQuery);

					BasicDBObject amexFinalQuery = new BasicDBObject("$and", amexCondList);

					BasicDBObject projectElement = new BasicDBObject();
					projectElement.put("AMOUNT", 1);
					projectElement.put("TXNTYPE", 1);
					BasicDBObject project = new BasicDBObject("$project", projectElement);
					BasicDBObject match = new BasicDBObject("$match", amexFinalQuery);

					List<BasicDBObject> pipeline = Arrays.asList(match, project);
					AggregateIterable<Document> output = coll.aggregate(pipeline);
					output.allowDiskUse(true);
					MongoCursor<Document> cursor = output.iterator();

					double saleAmount = 0.00;
					double refundAmount = 0.00;

					int saleCount = 0;
					int refundCount = 0;

					while (cursor.hasNext()) {

						Document amexDoc = cursor.next();

						String txnType = amexDoc.getString("TXNTYPE");
						String amount = amexDoc.getString("AMOUNT");

						if (txnType.equalsIgnoreCase("SALE")) {
							saleCount++;
							saleAmount = saleAmount + Double.valueOf(amount);
						} else {
							refundCount++;
							refundAmount = refundAmount + Double.valueOf(amount);
						}
					}

					cursor.close();
					double totalAmount = saleAmount - refundAmount;

					trs.setSaleCount(String.valueOf(saleCount));
					trs.setRefundCount(String.valueOf(refundCount));
					trs.setRefundAmount(String.format("%.2f", refundAmount));
					trs.setTotalAmount(String.format("%.2f", totalAmount));
					trs.setSaleAmount(String.format("%.2f", saleAmount));

					double diffAmount = totalAmount - statementAmt;
					trs.setDiffAmount(String.format("%.2f", diffAmount));
				}

				else if (acq.equalsIgnoreCase("RUPAY")) {

					BasicDBObject createDateIndexQuery = new BasicDBObject("DATE_INDEX",
							captureDate.substring(0, 10).replace("-", ""));
					BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", acq);
					BasicDBObject settlementQuery = new BasicDBObject("SETTLEMENT_FLAG", "Y");

					List<BasicDBObject> rupayCondList = new ArrayList<BasicDBObject>();
					rupayCondList.add(createDateIndexQuery);
					rupayCondList.add(acquirerQuery);
					rupayCondList.add(settlementQuery);

					BasicDBObject rupayFinalQuery = new BasicDBObject("$and", rupayCondList);

					BasicDBObject projectElement = new BasicDBObject();
					projectElement.put("AMOUNT", 1);
					projectElement.put("TXNTYPE", 1);
					BasicDBObject project = new BasicDBObject("$project", projectElement);
					BasicDBObject match = new BasicDBObject("$match", rupayFinalQuery);

					List<BasicDBObject> pipeline = Arrays.asList(match, project);
					AggregateIterable<Document> output = coll.aggregate(pipeline);
					output.allowDiskUse(true);
					MongoCursor<Document> cursor = output.iterator();

					double saleAmount = 0.00;
					double refundAmount = 0.00;

					int saleCount = 0;
					int refundCount = 0;

					while (cursor.hasNext()) {

						Document amexDoc = cursor.next();

						String txnType = amexDoc.getString("TXNTYPE");
						String amount = amexDoc.getString("AMOUNT");

						if (txnType.equalsIgnoreCase("SALE")) {
							saleCount++;
							saleAmount = saleAmount + Double.valueOf(amount);
						} else {
							refundCount++;
							refundAmount = refundAmount + Double.valueOf(amount);
						}
					}

					cursor.close();
					double totalAmount = saleAmount - refundAmount;
					trs.setSaleCount(String.valueOf(saleCount));
					trs.setRefundCount(String.valueOf(refundCount));
					trs.setRefundAmount(String.format("%.2f", refundAmount));
					trs.setTotalAmount(String.format("%.2f", totalAmount));
					trs.setSaleAmount(String.format("%.2f", saleAmount));

					double diffAmount = totalAmount - statementAmt;
					trs.setDiffAmount(String.format("%.2f", diffAmount));

				}

				txnList.add(trs);
			}

			return txnList;

		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchSettleSummary ", e);
			return txnList;
		}
	}

	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchAgent(String reservationId, String banktxnId) {

		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , searchSettleSummary");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();

			if (StringUtils.isNotBlank(reservationId)) {
				condList.add(new BasicDBObject("RESERVATION_ID", reservationId));
			}

			if (StringUtils.isNotBlank(banktxnId)) {
				condList.add(new BasicDBObject("BANK_TXN_NUMBER", banktxnId));
			}

			BasicDBObject finalQuery = new BasicDBObject("$and", condList);

			FindIterable<Document> output = coll.find(finalQuery);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document doc = cursor.next();

				TransactionReconSearch trs = new TransactionReconSearch();

				if (doc.get("CREATE_DATE") != null) {

					trs.setCreateDate(doc.get("CREATE_DATE").toString().substring(0, 10));
				} else {
					trs.setCreateDate("");
				}

				if (doc.get("RESERVATION_ID") != null) {

					trs.setReservationId(doc.get("RESERVATION_ID").toString());
				} else {
					trs.setReservationId("");
				}

				if (doc.get("RESERVATION_ID") != null) {

					trs.setReservationId(doc.get("RESERVATION_ID").toString());
				} else {
					trs.setReservationId("");
				}

				if (doc.get("BANK_TXN_NUMBER") != null) {

					trs.setBankTxnId(doc.get("BANK_TXN_NUMBER").toString());
				} else {
					trs.setBankTxnId("");
				}

				if (doc.get("SID") != null) {

					trs.setSid(doc.get("SID").toString());
				} else {
					trs.setSid("");
				}

				if (doc.get("AMOUNT") != null) {

					trs.setAmount(doc.get("AMOUNT").toString());
				} else {
					trs.setAmount("");
				}

				if (doc.get("STATUS") != null) {

					trs.setStatus(doc.get("STATUS").toString());
				} else {
					trs.setStatus("");
				}

				if (doc.get("TXNTYPE") != null) {

					trs.setTxnType(doc.get("TXNTYPE").toString());
				} else {
					trs.setTxnType("");
				}

				if (doc.get("SETTLEMENT_FLAG") != null) {

					trs.setSettlementFlag(doc.get("SETTLEMENT_FLAG").toString());
				} else {
					trs.setSettlementFlag("");
				}

				if (doc.get("SETTLEMENT_DATE") != null) {

					trs.setSettlementDate(doc.get("SETTLEMENT_DATE").toString().substring(0, 10));
				} else {
					trs.setSettlementDate("");
				}

				if (doc.get("POST_SETTLED_FLAG") != null) {

					trs.setPostSettledFlag(doc.get("POST_SETTLED_FLAG").toString());
				} else {
					trs.setPostSettledFlag("");
				}

				if (doc.get("ACQUIRER") != null) {

					trs.setAcquirer(doc.get("ACQUIRER").toString());
				} else {
					trs.setAcquirer("");
				}

				if (doc.get("POST_SETTLE_CAPTURE") != null) {

					trs.setPostSettledCapture(doc.get("POST_SETTLE_CAPTURE").toString());
				} else {
					trs.setPostSettledCapture("");
				}

				txnList.add(trs);

				txnList.add(trs);
			}

			return txnList;

		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchSettleSummary ", e);
			return txnList;
		}
	}

	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchAccountSummaryIPAY(String bank, String merchant, String fromDate,
			String toDate) {

		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , searchSettleSummary");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			MongoCollection<Document> statementColl = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

			BasicDBObject dateQuery = new BasicDBObject();

			String currentDate = null;
			if (!fromDate.isEmpty()) {
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put("PAYOUT_DATE",
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());

			}

			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			conditionList.add(dateQuery);

			BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", "IPAY");
			BasicDBObject bankQuery = new BasicDBObject("BANK_NAME", bank);
			BasicDBObject merchantQuery = new BasicDBObject("MERCHANT_NAME", merchant);

			conditionList.add(acquirerQuery);
			conditionList.add(bankQuery);
			conditionList.add(merchantQuery);
			BasicDBObject payoutQuery = new BasicDBObject("$and", conditionList);

			FindIterable<Document> payoutItr = statementColl.find(payoutQuery);
			MongoCursor<Document> payoutCursor = payoutItr.iterator();

			while (payoutCursor.hasNext()) {

				TransactionReconSearch trs = new TransactionReconSearch();
				Document doc = payoutCursor.next();

				String acq = doc.getString("ACQUIRER");
				String settlementDate = doc.getString("SETTLEMENT_DATE");
				String payoutDate = doc.getString("PAYOUT_DATE");
				String captureDate = doc.getString("CAPTURE_DATE");
				String narration = doc.getString("BANK_TXN_NUMBER");
				String statementAmount = doc.getString("AMOUNT");

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy");

				String settleDate = sdf2.format(sdf.parse(settlementDate));
				trs.setSettlementDate(settleDate);

				String payoutDateString = sdf2.format(sdf.parse(payoutDate));
				trs.setPaymentDate(payoutDateString);

				if (acq.equalsIgnoreCase("RUPAY")) {

					String captureDateString = sdf2.format(sdf.parse(captureDate));
					trs.setCreateDate(captureDateString);

				} else {
					trs.setCreateDate("NA");
				}

				trs.setAcquirer(acq);

				trs.setNarration(narration);
				trs.setStatementAmount(statementAmount);

				double statementAmt = Double.valueOf(statementAmount);

				txnList.add(trs);
			}

			return txnList;

		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchSettleSummary ", e);
			return txnList;
		}
	}

	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchAccountSummaryBOB(String acquirer, String fromDate, String toDate) {

		acquirer = "BOB";
		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , searchAccountSummaryBOB");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			MongoCollection<Document> statementColl = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

			BasicDBObject dateQuery = new BasicDBObject();

			String startDateString = fromDate;
			String endDateString = toDate;

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(startDateString);
			Date dateEnd = format.parse(endDateString);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString() + " 12:00:00");
				incrementingDate = incrementingDate.plusDays(1);

			}

			for (String captureDate : allDatesIndex) {

				int saleCount = 0;
				double saleAmount = 0;
				double stmtAmt = 0;
				dateQuery.put("CAPTURE_DATE", captureDate);
				BasicDBObject txnTypeQuery = new BasicDBObject("TXNTYPE", "SALE");
				BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", "BOB");

				List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
				condList.add(dateQuery);
				condList.add(txnTypeQuery);
				condList.add(acquirerQuery);

				BasicDBObject finalQuery = new BasicDBObject("$and", condList);

				FindIterable<Document> payoutItr = statementColl.find(finalQuery);
				MongoCursor<Document> payoutCursor = payoutItr.iterator();

				while (payoutCursor.hasNext()) {

					TransactionReconSearch trs = new TransactionReconSearch();

					Document doc = new Document();
					doc = payoutCursor.next();
					
					String statementAmount = doc.get("AMOUNT").toString();
					stmtAmt = Double.valueOf(statementAmount);
					trs.setStatementAmount(statementAmount);
					trs.setAcquirer("BOB");
					trs.setPaymentDate(doc.get("PAYOUT_DATE").toString());
					trs.setSettlementDate(doc.get("SETTLEMENT_DATE").toString());
					trs.setCreateDate(doc.get("CAPTURE_DATE").toString());

					List<BasicDBObject> txnCondList = new ArrayList<BasicDBObject>();
					BasicDBObject settlementDateQuery = new BasicDBObject("SETTLEMENT_DATE",
							doc.get("SETTLEMENT_DATE").toString());
					BasicDBObject txnDateIndexQuery = new BasicDBObject("DATE_INDEX",
							doc.get("CAPTURE_DATE").toString().substring(0, 10).replace("-", ""));

					txnCondList.add(txnTypeQuery);
					txnCondList.add(acquirerQuery);
					txnCondList.add(settlementDateQuery);
					txnCondList.add(txnDateIndexQuery);

					BasicDBObject txnfinalQuery = new BasicDBObject("$and", txnCondList);

					BasicDBObject projectElement = new BasicDBObject();
					projectElement.put("AMOUNT", 1);
					BasicDBObject project = new BasicDBObject("$project", projectElement);
					BasicDBObject match = new BasicDBObject("$match", txnfinalQuery);

					List<BasicDBObject> pipeline = Arrays.asList(match, project);
					AggregateIterable<Document> output = coll.aggregate(pipeline);
					output.allowDiskUse(true);
					MongoCursor<Document> cursor = output.iterator();

					saleCount = 0;
					saleAmount = 0;
					while (cursor.hasNext()) {

						Document obj = new Document();
						obj = cursor.next();
						saleCount++;
						String amount = obj.get("AMOUNT").toString();

						saleAmount = saleAmount + Double.valueOf(amount);

					}

					trs.setSaleCount(String.valueOf(saleCount));
					trs.setSaleAmount(String.format("%.2f", saleAmount));
					trs.setTotalAmount(String.format("%.2f", saleAmount));
					trs.setDiffAmount(String.format("%.2f", (saleAmount - stmtAmt)));
					txnList.add(trs);
				}

			}
			return txnList;
		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchSettleSummary ", e);
			return txnList;
		}
	}
	
	
	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchAccountSummaryAllahabad(String acquirer, String fromDate, String toDate) {

		acquirer = "ALLAHABAD BANK";
		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , searchAccountSummaryAllahabad");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			MongoCollection<Document> statementColl = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

			BasicDBObject dateQuery = new BasicDBObject();

			String startDateString = fromDate;
			String endDateString = toDate;

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(startDateString);
			Date dateEnd = format.parse(endDateString);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString() + " 12:00:00");
				incrementingDate = incrementingDate.plusDays(1);

			}

			for (String settlementDate : allDatesIndex) {

				int saleCount = 0;
				double saleAmount = 0;
				double stmtAmt = 0;
				dateQuery.put("PAYOUT_DATE", settlementDate);
				BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", "ALLAHABAD BANK");

				List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
				condList.add(dateQuery);
				condList.add(acquirerQuery);

				BasicDBObject finalQuery = new BasicDBObject("$and", condList);

				FindIterable<Document> payoutItr = statementColl.find(finalQuery);
				MongoCursor<Document> payoutCursor = payoutItr.iterator();

				while (payoutCursor.hasNext()) {

					TransactionReconSearch trs = new TransactionReconSearch();

					Document doc = new Document();
					doc = payoutCursor.next();
					
					String statementAmount = doc.get("AMOUNT").toString();
					stmtAmt = Double.valueOf(statementAmount);
					trs.setStatementAmount(statementAmount);
					trs.setAcquirer("ALLAHABAD BANK");
					if (doc.get("CAPTURE_DATE") != null) {
						trs.setCreateDate(doc.get("CAPTURE_DATE").toString());
					}
					else {
						trs.setCreateDate("");
					}
					trs.setPaymentDate(doc.get("PAYOUT_DATE").toString());
					trs.setSettlementDate(doc.get("SETTLEMENT_DATE").toString());

					String txnType = doc.get("TXNTYPE").toString();

					List<BasicDBObject> txnCondList = new ArrayList<BasicDBObject>();
					BasicDBObject settlementDateQuery = new BasicDBObject("SETTLEMENT_DATE",
							doc.get("SETTLEMENT_DATE").toString());
					BasicDBObject settleDateIndexQuery = new BasicDBObject("SETTLEMENT_DATE_INDEX",
							doc.get("SETTLEMENT_DATE").toString().substring(0, 10).replace("-", ""));
					
					txnCondList.add(new BasicDBObject("TXNTYPE",txnType));
					txnCondList.add(acquirerQuery);
					txnCondList.add(settlementDateQuery);
					txnCondList.add(settleDateIndexQuery);
					
					if (txnType.equalsIgnoreCase(TransactionType.SALE.getName())) {
						BasicDBObject captureDateIndexQuery = new BasicDBObject("DATE_INDEX",
								doc.get("CAPTURE_DATE").toString().substring(0, 10).replace("-", ""));
						txnCondList.add(captureDateIndexQuery);
					}

					BasicDBObject txnfinalQuery = new BasicDBObject("$and", txnCondList);

					BasicDBObject projectElement = new BasicDBObject();
					projectElement.put("AMOUNT", 1);
					BasicDBObject project = new BasicDBObject("$project", projectElement);
					BasicDBObject match = new BasicDBObject("$match", txnfinalQuery);

					List<BasicDBObject> pipeline = Arrays.asList(match, project);
					AggregateIterable<Document> output = coll.aggregate(pipeline);
					output.allowDiskUse(true);
					MongoCursor<Document> cursor = output.iterator();

					saleCount = 0;
					saleAmount = 0;
					while (cursor.hasNext()) {

						Document obj = new Document();
						obj = cursor.next();
						saleCount++;
						String amount = obj.get("AMOUNT").toString();

						saleAmount = saleAmount + Double.valueOf(amount);

					}

					trs.setSaleCount(String.valueOf(saleCount));
					trs.setSaleAmount(String.format("%.2f", saleAmount));
					trs.setTotalAmount(String.format("%.2f", saleAmount));
					trs.setDiffAmount(String.format("%.2f", (saleAmount - stmtAmt)));
					trs.setTxnType(txnType);
					txnList.add(trs);
				}

			}
			return txnList;
		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchSettleSummary ", e);
			return txnList;
		}
	}
	
	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchSettleSummaryBob(String acquirer, String transactionType, String fromDate,
			String toDate) {

		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , searchSettleSummaryBob");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			List<String> dateList = new ArrayList<>();
			List<String> acquirerList = new ArrayList<String>();

				acquirerList.add(acquirer);

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			while (!incrementingDate.isAfter(endDate)) {
				dateList.add(incrementingDate.toString() + " 12:00:00");
				incrementingDate = incrementingDate.plusDays(1);
			}

			for (String settlementDate : dateList) {

				for (String acq : acquirerList) {


						BasicDBObject dateIndexQuery = new BasicDBObject("DATE_INDEX", settlementDate.toString().substring(0, 10).replace("-", ""));
						BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", acq);

						List<BasicDBObject> condList = new ArrayList<BasicDBObject>();

						condList.add(acquirerQuery);
						condList.add(dateIndexQuery);

						BasicDBObject finalQuery = new BasicDBObject("$and", condList);

						BasicDBObject projectElement = new BasicDBObject();
						projectElement.put("AMOUNT", 1);
						projectElement.put("TXNTYPE", 1);
						BasicDBObject project = new BasicDBObject("$project", projectElement);

						BasicDBObject match = new BasicDBObject("$match", finalQuery);

						List<BasicDBObject> pipeline = Arrays.asList(match, project);
						AggregateIterable<Document> output = coll.aggregate(pipeline);
						output.allowDiskUse(true);
						MongoCursor<Document> cursor = output.iterator();

						double saleAmount = 0.00;
						double refundAmount = 0.00;

						int saleCount = 0;
						int refundCount = 0;

						while (cursor.hasNext()) {

							Document doc = cursor.next();

							String txnType = doc.getString("TXNTYPE");
							String amount = doc.getString("AMOUNT");

							if (txnType.equalsIgnoreCase("SALE")) {
								saleCount++;
								saleAmount = saleAmount + Double.valueOf(amount);
							} else {
								refundCount++;
								refundAmount = refundAmount + Double.valueOf(amount);
							}
						}

						cursor.close();

						double totalAmount = saleAmount - refundAmount;

						TransactionReconSearch trs = new TransactionReconSearch();
						trs.setAcquirer(acq);
						trs.setSaleCount(String.valueOf(saleCount));
						trs.setSaleAmount(String.format("%.2f", saleAmount));
						trs.setRefundCount(String.valueOf(refundCount));
						trs.setRefundAmount(String.format("%.2f", refundAmount));
						trs.setTotalAmount(String.format("%.2f", totalAmount));
						trs.setMerchantName("All");
						trs.setBankName("All");
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy");
						String settleDate = sdf2.format(sdf.parse(settlementDate));
						trs.setSettlementDate(settleDate);

						txnList.add(trs);
					}

			}

			return txnList;

		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchSettleSummary ", e);
			return txnList;
		}
	}
	
	
	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchSettleSummaryAllahabad(String acquirer, String transactionType, String fromDate,
			String toDate) {

		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , searchSettleSummaryAllahabad");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			List<String> dateList = new ArrayList<>();
			List<String> acquirerList = new ArrayList<String>();

				acquirerList.add(acquirer);

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			while (!incrementingDate.isAfter(endDate)) {
				dateList.add(incrementingDate.toString() + " 12:00:00");
				incrementingDate = incrementingDate.plusDays(1);
			}

			for (String settlementDate : dateList) {

				for (String acq : acquirerList) {


						BasicDBObject dateIndexQuery = new BasicDBObject("DATE_INDEX", settlementDate.toString().substring(0, 10).replace("-", ""));
						BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", acq);

						List<BasicDBObject> condList = new ArrayList<BasicDBObject>();

						condList.add(acquirerQuery);
						condList.add(dateIndexQuery);

						BasicDBObject finalQuery = new BasicDBObject("$and", condList);

						BasicDBObject projectElement = new BasicDBObject();
						projectElement.put("AMOUNT", 1);
						projectElement.put("MPR_AMOUNT", 1);
						projectElement.put("TXNTYPE", 1);
						BasicDBObject project = new BasicDBObject("$project", projectElement);

						BasicDBObject match = new BasicDBObject("$match", finalQuery);

						List<BasicDBObject> pipeline = Arrays.asList(match, project);
						AggregateIterable<Document> output = coll.aggregate(pipeline);
						output.allowDiskUse(true);
						MongoCursor<Document> cursor = output.iterator();

						double saleAmount = 0.00;
						double refundAmount = 0.00;

						int saleCount = 0;
						int refundCount = 0;

						while (cursor.hasNext()) {

							Document doc = cursor.next();

							String txnType = doc.getString("TXNTYPE");
							
							String amount = "0";
							if (doc.get("AMOUNT") != null) {
								amount = doc.getString("AMOUNT");
							}
							else if (doc.get("MPR_AMOUNT") != null) {
								amount = doc.getString("MPR_AMOUNT");
							}
							
							

							if (txnType.equalsIgnoreCase("SALE")) {
								saleCount++;
								saleAmount = saleAmount + Double.valueOf(amount);
							} else {
								refundCount++;
								refundAmount = refundAmount + Double.valueOf(amount);
							}
						}

						cursor.close();

						double totalAmount = saleAmount - refundAmount;

						TransactionReconSearch trs = new TransactionReconSearch();
						trs.setAcquirer(acq);
						trs.setSaleCount(String.valueOf(saleCount));
						trs.setSaleAmount(String.format("%.2f", saleAmount));
						trs.setRefundCount(String.valueOf(refundCount));
						trs.setRefundAmount(String.format("%.2f", refundAmount));
						trs.setTotalAmount(String.format("%.2f", totalAmount));
						trs.setMerchantName("All");
						trs.setBankName("All");
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy");
						String settleDate = sdf2.format(sdf.parse(settlementDate));
						trs.setSettlementDate(settleDate);

						txnList.add(trs);
					}

			}

			return txnList;

		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchSettleSummary ", e);
			return txnList;
		}
	}
	
	
	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchSettleSummaryMaharashtra(String acquirer, String transactionType, String fromDate,
			String toDate) {

		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , searchSettleSummaryMaharashtra");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			List<String> dateList = new ArrayList<>();
			List<String> acquirerList = new ArrayList<String>();

				acquirerList.add(acquirer);

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			while (!incrementingDate.isAfter(endDate)) {
				dateList.add(incrementingDate.toString() + " 12:00:00");
				incrementingDate = incrementingDate.plusDays(1);
			}

			for (String settlementDate : dateList) {

				for (String acq : acquirerList) {


						BasicDBObject dateIndexQuery = new BasicDBObject("DATE_INDEX", settlementDate.toString().substring(0, 10).replace("-", ""));
						BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", acq);

						List<BasicDBObject> condList = new ArrayList<BasicDBObject>();

						condList.add(acquirerQuery);
						condList.add(dateIndexQuery);

						BasicDBObject finalQuery = new BasicDBObject("$and", condList);

						BasicDBObject projectElement = new BasicDBObject();
						projectElement.put("AMOUNT", 1);
						projectElement.put("TXNTYPE", 1);
						projectElement.put("MPR_AMOUNT", 1);
						BasicDBObject project = new BasicDBObject("$project", projectElement);

						BasicDBObject match = new BasicDBObject("$match", finalQuery);

						List<BasicDBObject> pipeline = Arrays.asList(match, project);
						AggregateIterable<Document> output = coll.aggregate(pipeline);
						output.allowDiskUse(true);
						MongoCursor<Document> cursor = output.iterator();

						double saleAmount = 0.00;
						double refundAmount = 0.00;

						int saleCount = 0;
						int refundCount = 0;

						while (cursor.hasNext()) {

							Document doc = cursor.next();

							String txnType = doc.getString("TXNTYPE");
							String amount = null;
							
							if (doc.get("AMOUNT") != null) {
								amount = doc.getString("AMOUNT");
							}
							else {
								amount = doc.getString("MPR_AMOUNT");
							}

							if (txnType.equalsIgnoreCase("SALE")) {
								saleCount++;
								saleAmount = saleAmount + Double.valueOf(amount);
							} else {
								refundCount++;
								refundAmount = refundAmount + Double.valueOf(amount);
							}
						}

						cursor.close();

						double totalAmount = saleAmount - refundAmount;

						TransactionReconSearch trs = new TransactionReconSearch();
						trs.setAcquirer(acq);
						trs.setSaleCount(String.valueOf(saleCount));
						trs.setSaleAmount(String.format("%.2f", saleAmount));
						trs.setRefundCount(String.valueOf(refundCount));
						trs.setRefundAmount(String.format("%.2f", refundAmount));
						trs.setTotalAmount(String.format("%.2f", totalAmount));
						trs.setMerchantName("All");
						trs.setBankName("All");
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy");
						String settleDate = sdf2.format(sdf.parse(settlementDate));
						trs.setSettlementDate(settleDate);

						txnList.add(trs);
					}

			}

			return txnList;

		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchSettleSummary ", e);
			return txnList;
		}
	}
	
	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> bobReversals(String acquirer, String transactionType, String fromDate,
			String toDate) {

		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , bobReversals");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> statementColl = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject txnQuery = new BasicDBObject("TXNTYPE","REVERSAL");
			BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER","BOB");
			
			String currentDate = null;
			if (!fromDate.isEmpty()) {
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put("SETTLEMENT_DATE",
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());

			}

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
			condList.add(txnQuery);
			condList.add(dateQuery);
			condList.add(acquirerQuery);
			
			BasicDBObject finalQuery = new BasicDBObject("$and",condList);
			
			FindIterable<Document> payoutItr = statementColl.find(finalQuery);
			MongoCursor<Document> payoutCursor = payoutItr.iterator();

			while (payoutCursor.hasNext()) {

				TransactionReconSearch trs = new TransactionReconSearch();
				Document doc = payoutCursor.next();

				trs.setTotalAmount(doc.get("AMOUNT").toString());
				trs.setAcquirer("BOB");
				trs.setTxnType("REVERSAL");
				trs.setSettlementDate(doc.get("SETTLEMENT_DATE").toString());
				txnList.add(trs);
			}
			
			return txnList;

		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchSettleSummary ", e);
			return txnList;
		}
	}
	
	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchAccountSummaryMaharashtra(String acquirer, String fromDate, String toDate) {

		acquirer = "BANK OF MAHARASHTRA";
		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , searchAccountSummaryMaharashtra");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			MongoCollection<Document> statementColl = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

			BasicDBObject dateQuery = new BasicDBObject();

			String startDateString = fromDate;
			String endDateString = toDate;

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(startDateString);
			Date dateEnd = format.parse(endDateString);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString() + " 12:00:00");
				incrementingDate = incrementingDate.plusDays(1);

			}

			for (String settlementDate : allDatesIndex) {

				int saleCount = 0;
				double saleAmount = 0;
				double stmtAmt = 0;
				dateQuery.put("PAYOUT_DATE", settlementDate);
				BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", "BANK OF MAHARASHTRA");

				List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
				condList.add(dateQuery);
				condList.add(acquirerQuery);

				BasicDBObject finalQuery = new BasicDBObject("$and", condList);

				FindIterable<Document> payoutItr = statementColl.find(finalQuery);
				MongoCursor<Document> payoutCursor = payoutItr.iterator();

				while (payoutCursor.hasNext()) {

					TransactionReconSearch trs = new TransactionReconSearch();

					Document doc = new Document();
					doc = payoutCursor.next();
					
					String statementAmount = doc.get("AMOUNT").toString();
					stmtAmt = Double.valueOf(statementAmount);
					trs.setStatementAmount(statementAmount);
					trs.setAcquirer("BANK OF MAHARASHTRA");
					trs.setPaymentDate(doc.get("PAYOUT_DATE").toString());
					trs.setSettlementDate(doc.get("SETTLEMENT_DATE").toString());

					String txnType = doc.get("TXNTYPE").toString();

					List<BasicDBObject> txnCondList = new ArrayList<BasicDBObject>();
					BasicDBObject settlementDateQuery = new BasicDBObject("SETTLEMENT_DATE",
							doc.get("SETTLEMENT_DATE").toString());
					BasicDBObject settleDateIndexQuery = new BasicDBObject("SETTLEMENT_DATE_INDEX",
							doc.get("SETTLEMENT_DATE").toString().substring(0, 10).replace("-", ""));

					txnCondList.add(new BasicDBObject("TXNTYPE",txnType));
					txnCondList.add(acquirerQuery);
					txnCondList.add(settlementDateQuery);
					txnCondList.add(settleDateIndexQuery);
					
					if (txnType.equalsIgnoreCase(TransactionType.SALE.getName())) {
						BasicDBObject captureDateIndexQuery = new BasicDBObject("DATE_INDEX",
								doc.get("CAPTURE_DATE").toString().substring(0, 10).replace("-", ""));
						txnCondList.add(captureDateIndexQuery);
					}

					BasicDBObject txnfinalQuery = new BasicDBObject("$and", txnCondList);

					BasicDBObject projectElement = new BasicDBObject();
					projectElement.put("AMOUNT", 1);
					projectElement.put("MPR_AMOUNT", 1);
					BasicDBObject project = new BasicDBObject("$project", projectElement);
					BasicDBObject match = new BasicDBObject("$match", txnfinalQuery);

					List<BasicDBObject> pipeline = Arrays.asList(match, project);
					AggregateIterable<Document> output = coll.aggregate(pipeline);
					output.allowDiskUse(true);
					MongoCursor<Document> cursor = output.iterator();

					saleCount = 0;
					saleAmount = 0;
					while (cursor.hasNext()) {

						Document obj = new Document();
						obj = cursor.next();
						saleCount++;
						
						String amount = "0";
						if (obj.get("AMOUNT")  != null) {
							amount = obj.get("AMOUNT").toString();
						}
						else if (obj.get("MPR_AMOUNT")  != null) {
							amount = obj.get("MPR_AMOUNT").toString();
						}
						

						saleAmount = saleAmount + Double.valueOf(amount);

					}

					trs.setSaleCount(String.valueOf(saleCount));
					trs.setSaleAmount(String.format("%.2f", saleAmount));
					trs.setTotalAmount(String.format("%.2f", saleAmount));
					trs.setDiffAmount(String.format("%.2f", (saleAmount - stmtAmt)));
					trs.setTxnType(txnType);
					txnList.add(trs);
				}

			}
			return txnList;
		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchAccountSummaryMaharashtra ", e);
			return txnList;
		}
	}
	
	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchAccountSummaryCorporation(String acquirer, String fromDate, String toDate) {

		acquirer = "CORPORATION BANK";
		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , searchAccountSummaryAllahabad");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			MongoCollection<Document> statementColl = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

			BasicDBObject dateQuery = new BasicDBObject();

			String startDateString = fromDate;
			String endDateString = toDate;

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(startDateString);
			Date dateEnd = format.parse(endDateString);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString() + " 12:00:00");
				incrementingDate = incrementingDate.plusDays(1);

			}

			for (String settlementDate : allDatesIndex) {

				int saleCount = 0;
				double saleAmount = 0;
				double stmtAmt = 0;
				dateQuery.put("PAYOUT_DATE", settlementDate);
				BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", "CORPORATION BANK");

				List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
				condList.add(dateQuery);
				condList.add(acquirerQuery);

				BasicDBObject finalQuery = new BasicDBObject("$and", condList);

				FindIterable<Document> payoutItr = statementColl.find(finalQuery);
				MongoCursor<Document> payoutCursor = payoutItr.iterator();

				while (payoutCursor.hasNext()) {

					TransactionReconSearch trs = new TransactionReconSearch();

					Document doc = new Document();
					doc = payoutCursor.next();
					
					String statementAmount = doc.get("AMOUNT").toString();
					stmtAmt = Double.valueOf(statementAmount);
					trs.setStatementAmount(statementAmount);
					trs.setAcquirer("CORPORATION BANK");
					trs.setPaymentDate(doc.get("PAYOUT_DATE").toString());
					trs.setSettlementDate(doc.get("SETTLEMENT_DATE").toString());
					trs.setCreateDate(doc.get("CAPTURE_DATE").toString());
					String txnType = doc.get("TXNTYPE").toString();

					List<BasicDBObject> txnCondList = new ArrayList<BasicDBObject>();
					BasicDBObject settlementDateQuery = new BasicDBObject("SETTLEMENT_DATE",
							doc.get("SETTLEMENT_DATE").toString());
					BasicDBObject settleDateIndexQuery = new BasicDBObject("SETTLEMENT_DATE_INDEX",
							doc.get("SETTLEMENT_DATE").toString().substring(0, 10).replace("-", ""));
					BasicDBObject captureDateIndexQuery = new BasicDBObject("DATE_INDEX",
							doc.get("CAPTURE_DATE").toString().substring(0, 10).replace("-", ""));
					txnCondList.add(new BasicDBObject("TXNTYPE",txnType));
					txnCondList.add(acquirerQuery);
					txnCondList.add(settlementDateQuery);
					txnCondList.add(settleDateIndexQuery);
					txnCondList.add(captureDateIndexQuery);
					
					BasicDBObject txnfinalQuery = new BasicDBObject("$and", txnCondList);

					BasicDBObject projectElement = new BasicDBObject();
					projectElement.put("AMOUNT", 1);
					projectElement.put("MPR_AMOUNT", 1);
					BasicDBObject project = new BasicDBObject("$project", projectElement);
					BasicDBObject match = new BasicDBObject("$match", txnfinalQuery);

					List<BasicDBObject> pipeline = Arrays.asList(match, project);
					AggregateIterable<Document> output = coll.aggregate(pipeline);
					output.allowDiskUse(true);
					MongoCursor<Document> cursor = output.iterator();

					saleCount = 0;
					saleAmount = 0;
					while (cursor.hasNext()) {

						Document obj = new Document();
						obj = cursor.next();
						saleCount++;
						
						String amount = null;
						if (obj.get("AMOUNT") != null) {
							 amount = obj.get("AMOUNT").toString();
						}
						else {
							 amount = obj.get("MPR_AMOUNT").toString();
						}

						saleAmount = saleAmount + Double.valueOf(amount);

					}

					trs.setSaleCount(String.valueOf(saleCount));
					trs.setSaleAmount(String.format("%.2f", saleAmount));
					trs.setTotalAmount(String.format("%.2f", saleAmount));
					trs.setDiffAmount(String.format("%.2f", (saleAmount - stmtAmt)));
					trs.setTxnType(txnType);
					txnList.add(trs);
				}

			}
			return txnList;
		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchSettleSummary ", e);
			return txnList;
		}
	}
	
	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchSettleSummaryCorporation(String acquirer, String transactionType, String fromDate,
			String toDate) {

		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , searchSettleSummaryCorporation");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			List<String> dateList = new ArrayList<>();
			List<String> acquirerList = new ArrayList<String>();

				acquirerList.add(acquirer);

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			while (!incrementingDate.isAfter(endDate)) {
				dateList.add(incrementingDate.toString() + " 12:00:00");
				incrementingDate = incrementingDate.plusDays(1);
			}

			for (String settlementDate : dateList) {

				for (String acq : acquirerList) {


						BasicDBObject dateIndexQuery = new BasicDBObject("DATE_INDEX", settlementDate.toString().substring(0, 10).replace("-", ""));
						BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", acq);

						List<BasicDBObject> condList = new ArrayList<BasicDBObject>();

						condList.add(acquirerQuery);
						condList.add(dateIndexQuery);

						BasicDBObject finalQuery = new BasicDBObject("$and", condList);

						BasicDBObject projectElement = new BasicDBObject();
						projectElement.put("AMOUNT", 1);
						projectElement.put("MPR_AMOUNT", 1);
						projectElement.put("TXNTYPE", 1);
						BasicDBObject project = new BasicDBObject("$project", projectElement);

						BasicDBObject match = new BasicDBObject("$match", finalQuery);

						List<BasicDBObject> pipeline = Arrays.asList(match, project);
						AggregateIterable<Document> output = coll.aggregate(pipeline);
						output.allowDiskUse(true);
						MongoCursor<Document> cursor = output.iterator();

						double saleAmount = 0.00;
						double refundAmount = 0.00;

						int saleCount = 0;
						int refundCount = 0;

						while (cursor.hasNext()) {

							Document doc = cursor.next();

							String txnType = doc.getString("TXNTYPE");
							
							String amount = "0";
							if (doc.get("AMOUNT") != null) {
								amount = doc.getString("AMOUNT");
							}
							else if (doc.get("MPR_AMOUNT") != null) {
								amount = doc.getString("MPR_AMOUNT");
							}
							
							

							if (txnType.equalsIgnoreCase("SALE")) {
								saleCount++;
								saleAmount = saleAmount + Double.valueOf(amount);
							} else {
								refundCount++;
								refundAmount = refundAmount + Double.valueOf(amount);
							}
						}

						cursor.close();

						double totalAmount = saleAmount - refundAmount;

						TransactionReconSearch trs = new TransactionReconSearch();
						trs.setAcquirer(acq);
						trs.setSaleCount(String.valueOf(saleCount));
						trs.setSaleAmount(String.format("%.2f", saleAmount));
						trs.setRefundCount(String.valueOf(refundCount));
						trs.setRefundAmount(String.format("%.2f", refundAmount));
						trs.setTotalAmount(String.format("%.2f", totalAmount));
						trs.setMerchantName("All");
						trs.setBankName("All");
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy");
						String settleDate = sdf2.format(sdf.parse(settlementDate));
						trs.setSettlementDate(settleDate);

						txnList.add(trs);
					}

			}

			return txnList;

		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchSettleSummary ", e);
			return txnList;
		}
	}
	
	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchAccountSummaryKarur(String acquirer, String fromDate, String toDate) {

		acquirer = "KARUR BANK";
		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , searchAccountSummaryKarur");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			MongoCollection<Document> statementColl = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

			BasicDBObject dateQuery = new BasicDBObject();

			String startDateString = fromDate;
			String endDateString = toDate;

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(startDateString);
			Date dateEnd = format.parse(endDateString);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString() + " 12:00:00");
				incrementingDate = incrementingDate.plusDays(1);

			}

			for (String settlementDate : allDatesIndex) {

				int saleCount = 0;
				double saleAmount = 0;
				double stmtAmt = 0;
				dateQuery.put("PAYOUT_DATE", settlementDate);
				BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", "KARUR BANK");

				List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
				condList.add(dateQuery);
				condList.add(acquirerQuery);

				BasicDBObject finalQuery = new BasicDBObject("$and", condList);

				FindIterable<Document> payoutItr = statementColl.find(finalQuery);
				MongoCursor<Document> payoutCursor = payoutItr.iterator();

				while (payoutCursor.hasNext()) {

					TransactionReconSearch trs = new TransactionReconSearch();

					Document doc = new Document();
					doc = payoutCursor.next();
					
					String statementAmount = doc.get("AMOUNT").toString();
					stmtAmt = Double.valueOf(statementAmount);
					trs.setStatementAmount(statementAmount);
					trs.setAcquirer("KARUR BANK");
					trs.setPaymentDate(doc.get("PAYOUT_DATE").toString());
					trs.setSettlementDate(doc.get("SETTLEMENT_DATE").toString());
					trs.setCreateDate(doc.get("CAPTURE_DATE").toString());
					String txnType = doc.get("TXNTYPE").toString();

					List<BasicDBObject> txnCondList = new ArrayList<BasicDBObject>();
					BasicDBObject settlementDateQuery = new BasicDBObject("SETTLEMENT_DATE",
							doc.get("SETTLEMENT_DATE").toString());
					BasicDBObject settleDateIndexQuery = new BasicDBObject("SETTLEMENT_DATE_INDEX",
							doc.get("SETTLEMENT_DATE").toString().substring(0, 10).replace("-", ""));
					BasicDBObject captureDateIndexQuery = new BasicDBObject("DATE_INDEX",
							doc.get("CAPTURE_DATE").toString().substring(0, 10).replace("-", ""));
					txnCondList.add(new BasicDBObject("TXNTYPE",txnType));
					txnCondList.add(acquirerQuery);
					txnCondList.add(settlementDateQuery);
					txnCondList.add(settleDateIndexQuery);
					txnCondList.add(captureDateIndexQuery);
					
					BasicDBObject txnfinalQuery = new BasicDBObject("$and", txnCondList);

					BasicDBObject projectElement = new BasicDBObject();
					projectElement.put("AMOUNT", 1);
					projectElement.put("MPR_AMOUNT", 1);
					BasicDBObject project = new BasicDBObject("$project", projectElement);
					BasicDBObject match = new BasicDBObject("$match", txnfinalQuery);

					List<BasicDBObject> pipeline = Arrays.asList(match, project);
					AggregateIterable<Document> output = coll.aggregate(pipeline);
					output.allowDiskUse(true);
					MongoCursor<Document> cursor = output.iterator();

					saleCount = 0;
					saleAmount = 0;
					while (cursor.hasNext()) {

						Document obj = new Document();
						obj = cursor.next();
						saleCount++;
						
						String amount = null;
						if (obj.get("AMOUNT") != null) {
							 amount = obj.get("AMOUNT").toString();
						}
						else {
							 amount = obj.get("MPR_AMOUNT").toString();
						}

						saleAmount = saleAmount + Double.valueOf(amount);

					}

					trs.setSaleCount(String.valueOf(saleCount));
					trs.setSaleAmount(String.format("%.2f", saleAmount));
					trs.setTotalAmount(String.format("%.2f", saleAmount));
					trs.setDiffAmount(String.format("%.2f", (saleAmount - stmtAmt)));
					trs.setTxnType(txnType);
					txnList.add(trs);
				}

			}
			return txnList;
		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchSettleSummary ", e);
			return txnList;
		}
	}
	
	@SuppressWarnings("static-access")
	public List<TransactionReconSearch> searchSettleSummaryKarur(String acquirer, String transactionType, String fromDate,
			String toDate) {

		List<TransactionReconSearch> txnList = new ArrayList<TransactionReconSearch>();

		logger.info("Inside TxnReconReports , searchSettleSummaryKarur");
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			List<String> dateList = new ArrayList<>();
			List<String> acquirerList = new ArrayList<String>();

				acquirerList.add(acquirer);

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			while (!incrementingDate.isAfter(endDate)) {
				dateList.add(incrementingDate.toString() + " 12:00:00");
				incrementingDate = incrementingDate.plusDays(1);
			}

			for (String settlementDate : dateList) {

				for (String acq : acquirerList) {


						BasicDBObject dateIndexQuery = new BasicDBObject("DATE_INDEX", settlementDate.toString().substring(0, 10).replace("-", ""));
						BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", acq);

						List<BasicDBObject> condList = new ArrayList<BasicDBObject>();

						condList.add(acquirerQuery);
						condList.add(dateIndexQuery);

						BasicDBObject finalQuery = new BasicDBObject("$and", condList);

						BasicDBObject projectElement = new BasicDBObject();
						projectElement.put("AMOUNT", 1);
						projectElement.put("MPR_AMOUNT", 1);
						projectElement.put("TXNTYPE", 1);
						BasicDBObject project = new BasicDBObject("$project", projectElement);

						BasicDBObject match = new BasicDBObject("$match", finalQuery);

						List<BasicDBObject> pipeline = Arrays.asList(match, project);
						AggregateIterable<Document> output = coll.aggregate(pipeline);
						output.allowDiskUse(true);
						MongoCursor<Document> cursor = output.iterator();

						double saleAmount = 0.00;
						double refundAmount = 0.00;

						int saleCount = 0;
						int refundCount = 0;

						while (cursor.hasNext()) {

							Document doc = cursor.next();

							String txnType = doc.getString("TXNTYPE");
							
							String amount = "0";
							if (doc.get("AMOUNT") != null) {
								amount = doc.getString("AMOUNT");
							}
							else if (doc.get("MPR_AMOUNT") != null) {
								amount = doc.getString("MPR_AMOUNT");
							}
							
							

							if (txnType.equalsIgnoreCase("SALE")) {
								saleCount++;
								saleAmount = saleAmount + Double.valueOf(amount);
							} else {
								refundCount++;
								refundAmount = refundAmount + Double.valueOf(amount);
							}
						}

						cursor.close();

						double totalAmount = saleAmount - refundAmount;

						TransactionReconSearch trs = new TransactionReconSearch();
						trs.setAcquirer(acq);
						trs.setSaleCount(String.valueOf(saleCount));
						trs.setSaleAmount(String.format("%.2f", saleAmount));
						trs.setRefundCount(String.valueOf(refundCount));
						trs.setRefundAmount(String.format("%.2f", refundAmount));
						trs.setTotalAmount(String.format("%.2f", totalAmount));
						trs.setMerchantName("All");
						trs.setBankName("All");
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy");
						String settleDate = sdf2.format(sdf.parse(settlementDate));
						trs.setSettlementDate(settleDate);

						txnList.add(trs);
					}

			}

			return txnList;

		} catch (Exception e) {
			logger.error("Exception occured in ReconTxnReports , searchSettleSummaryKarur ", e);
			return txnList;
		}
	}
}
