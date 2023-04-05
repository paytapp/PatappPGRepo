package com.paymentgateway.crm.dashboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;

@Component
public class BarChartQuery {
	private static Logger logger = LoggerFactory.getLogger(BarChartQuery.class.getName());
	private static final String alphabaticFileName = "alphabatic-currencycode.properties";
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;
	@Autowired
	private PropertiesManager propertiesManager;
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;

	public HashMap<String, String> chartTotalSummary(String payId, String currency, String dateFrom, String dateTo) {
		int noOfTransactionsvi = 0;
		int noOfTransactionsmc = 0;
		int noOfTransactionsAmex = 0;
		int noOfTransactionsMestro = 0;
		int noOfTransactionsEzee = 0;
		int noOfTransactionsWallet = 0;
		int noOfTransactionsNb = 0;
		int noOfTransactionsOther = 0;

		BasicDBObject dateQuery = new BasicDBObject();
		BasicDBObject mopQuery = new BasicDBObject();
		List<BasicDBObject> saleAndCaptureList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> mopConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> authAndApprovedList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> dateIndexConditionList = new ArrayList<BasicDBObject>();
		Map<String, String> moplist = new HashMap<String, String>();
		try {
			if (!dateFrom.isEmpty()) {

				String currentDate = null;
				if (!dateTo.isEmpty()) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				
				
				BasicDBObject dateIndexConditionQuery = new BasicDBObject();
				String startString = new SimpleDateFormat(dateFrom).toLocalizedPattern();
				String endString = new SimpleDateFormat(dateTo).toLocalizedPattern();

				DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
				Date dateStart = format.parse(startString);
				Date dateEnd = format.parse(endString);

				LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

				List<String> allDatesIndex = new ArrayList<String>();

				while (!incrementingDate.isAfter(endDate)) {
					allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
					incrementingDate = incrementingDate.plusDays(1);
				}
				
				for (String index : allDatesIndex) {
					dateIndexConditionList.add(new BasicDBObject(FieldType.DATE_INDEX.getName(),index));
				}

				dateIndexConditionQuery.append("or", dateIndexConditionList);

				BasicDBObject payIdquery = new BasicDBObject();
				if (!payId.equalsIgnoreCase("ALL MERCHANTS")) {
					payIdquery.put(FieldType.PAY_ID.getName(), payId);
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
				
				saleAndCaptureList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "SALE"));
				saleAndCaptureList.add(new BasicDBObject(FieldType.STATUS.getName(), "Capture"));
				BasicDBObject query1 = new BasicDBObject("$and", saleAndCaptureList);
				authAndApprovedList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "AUTHORISE"));
				authAndApprovedList.add(new BasicDBObject(FieldType.STATUS.getName(), "Approved"));
				BasicDBObject query2 = new BasicDBObject("$and", authAndApprovedList);
				BasicDBObject query = new BasicDBObject();
				query.put(FieldType.CURRENCY_CODE.getName(), currency);
				mopConditionLst.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), MopType.VISA.getCode()));
				mopConditionLst.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), MopType.MASTERCARD.getCode()));
				mopConditionLst.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), MopType.DINERS.getCode()));
				mopConditionLst.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), MopType.MAESTRO.getCode()));
				mopQuery.append("$or", mopConditionLst);
				List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();
				List<BasicDBObject> orConditionQueryList = new ArrayList<BasicDBObject>();
				orConditionQueryList.add(query1);
				orConditionQueryList.add(query2);
				BasicDBObject orConditionQueryObj = new BasicDBObject("$or", orConditionQueryList);
				if (!payIdquery.isEmpty()) {
					fianlList.add(payIdquery);
				}

				fianlList.add(dateQuery);
				fianlList.add(dateIndexConditionQuery);
				fianlList.add(query);
				fianlList.add(mopQuery);
				fianlList.add(orConditionQueryObj);
				BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns.getCollection(
						PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				
				BasicDBObject projectElement = new BasicDBObject();
				projectElement.put(FieldType.MOP_TYPE.getName(), 1);
				projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);
				
				BasicDBObject project = new BasicDBObject("$project", projectElement);
				
				BasicDBObject match = new BasicDBObject("$match", finalquery);
				List<BasicDBObject> pipeline = Arrays.asList(match, project);
				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				MongoCursor<Document>  cursor = output.iterator();
				
				
				//MongoCursor<Document> cursor = coll.find(finalquery).iterator();
				while (cursor.hasNext()) {
					Document dbobj = cursor.next();

					if ((dbobj.getString(FieldType.MOP_TYPE.toString()).equals(MopType.VISA.getCode()))) {

						noOfTransactionsvi++;

					} else if ((dbobj.getString(FieldType.MOP_TYPE.toString()).equals(MopType.MASTERCARD.getCode()))) {

						noOfTransactionsmc++;

					} else if ((dbobj.getString(FieldType.MOP_TYPE.toString()).equals(MopType.AMEX.getCode()))) {

						noOfTransactionsAmex++;

					} else if ((dbobj.getString(FieldType.MOP_TYPE.toString()).equals(MopType.MAESTRO.getCode()))) {

						noOfTransactionsMestro++;

					} else if ((dbobj.getString(FieldType.MOP_TYPE.toString()).equals(MopType.EZEECLICK.getCode()))) {

						noOfTransactionsEzee++;

					} else if ((dbobj.getString(FieldType.MOP_TYPE.toString()).equals(PaymentType.WALLET.getCode()))) {

						noOfTransactionsWallet++;

					} else if ((dbobj.getString(FieldType.MOP_TYPE.toString())
							.equals(PaymentType.NET_BANKING.getCode()))) {

						noOfTransactionsNb++;

					} else if ((!dbobj.getString(FieldType.MOP_TYPE.toString())
							.equals(PaymentType.NET_BANKING.getCode()))
							|| (!dbobj.getString(FieldType.MOP_TYPE.toString()).equals(PaymentType.WALLET.getCode()))
							|| (!dbobj.getString(FieldType.MOP_TYPE.toString()).equals(MopType.EZEECLICK.getCode()))
							|| (!dbobj.getString(FieldType.MOP_TYPE.toString()).equals(MopType.MAESTRO.getCode())
									|| (!dbobj.getString(FieldType.MOP_TYPE.toString()).equals(MopType.VISA.getCode())))
							|| (!dbobj.getString(FieldType.MOP_TYPE.toString()).equals(MopType.MASTERCARD.getCode()))
							|| (!dbobj.getString(FieldType.MOP_TYPE.toString()).equals(MopType.AMEX.getCode()))) {
						noOfTransactionsOther++;
					}
				}
				moplist.put(MopType.VISA.getName(), String.valueOf(noOfTransactionsvi));
				moplist.put(MopType.MASTERCARD.getName(), String.valueOf(noOfTransactionsmc));
				moplist.put(MopType.AMEX.getName(), String.valueOf(noOfTransactionsAmex));
				moplist.put(MopType.MAESTRO.getName(), String.valueOf(noOfTransactionsMestro));
				moplist.put(MopType.EZEECLICK.getName(), String.valueOf(noOfTransactionsEzee));
				moplist.put(PaymentType.WALLET.getName(), String.valueOf(noOfTransactionsWallet));
				moplist.put(PaymentType.NET_BANKING.getName(), String.valueOf(noOfTransactionsNb));
				moplist.put("Other", String.valueOf(noOfTransactionsOther));
				cursor.close();
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);

		}
		return (HashMap<String, String>) moplist;
	}

	public PieChart barChartTotalSummary(String payId, String currency, String dateFrom, String dateTo,
			User sessionUser) {
		int totalCredit = 0;
		int totalDebit = 0;
		int totalNet = 0;
		int other = 0;
		PieChart pieChart = new PieChart();

		BasicDBObject payIdquery = new BasicDBObject();
		BasicDBObject resellerquery = new BasicDBObject();
		BasicDBObject mopQuery = new BasicDBObject();
		BasicDBObject dateQuery = new BasicDBObject();
		BasicDBObject txnTypeConditionQuery = new BasicDBObject();
		BasicDBObject paymentTypeQuery = new BasicDBObject();
		List<BasicDBObject> paymentTypeConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> dateIndexConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> mopConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> txnTypeConditionList = new ArrayList<BasicDBObject>();
		DateFormat df = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		try {
			String startDate = sdf1.format(df.parse(dateFrom));
			String endDate = sdf1.format(df.parse(dateTo));
			if (!startDate.isEmpty()) {

				String currentDate = null;
				if (!endDate.isEmpty()) {
					currentDate = endDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}
			
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(dateFrom).toLocalizedPattern();
			String endString = new SimpleDateFormat(dateTo).toLocalizedPattern();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(startString);
			Date dateEnd = format.parse(endString);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDateVal = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDateVal)) {
				allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}

			for (String index :allDatesIndex ) {
				dateIndexConditionLst.add(new BasicDBObject(FieldType.DATE_INDEX.getName(),index));
			}
			
			
			dateIndexConditionQuery.append("$or", dateIndexConditionLst);
			
			if (sessionUser.getUserType().equals(UserType.PARENTMERCHANT)) {
				payIdquery.put(FieldType.PARENT_PAY_ID.getName(), sessionUser.getPayId());
				
			}else if (!payId.equalsIgnoreCase("ALL MERCHANTS")) {
				payIdquery.put(FieldType.PAY_ID.getName(), payId);
			}
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerquery.put(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId());
			}

			BasicDBObject query = new BasicDBObject();
			query.put(FieldType.CURRENCY_CODE.getName(), currency);

			List<BasicDBObject> saleAndCapList = new ArrayList<BasicDBObject>();
			saleAndCapList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleAndCapList.add(
					new BasicDBObject(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName())));
			BasicDBObject saleAndCapquery2 = new BasicDBObject("$and", saleAndCapList);

			List<BasicDBObject> authAndAllList = new ArrayList<BasicDBObject>();
			authAndAllList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.AUTHORISE.getName()));
			authAndAllList.add(
					new BasicDBObject(new BasicDBObject(FieldType.STATUS.getName(), StatusType.APPROVED.getName())));
			BasicDBObject authAndAppquery3 = new BasicDBObject("$and", authAndAllList);
			txnTypeConditionList.add(saleAndCapquery2);
			txnTypeConditionList.add(authAndAppquery3);
			txnTypeConditionQuery.append("$or", txnTypeConditionList);
			mopConditionLst.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), MopType.VISA.getCode()));
			mopConditionLst.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), MopType.MASTERCARD.getCode()));
			mopConditionLst.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), MopType.DINERS.getCode()));
			mopConditionLst.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), MopType.MAESTRO.getCode()));
			mopQuery.append("$or", mopConditionLst);

			paymentTypeConditionLst
					.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.CREDIT_CARD.getCode()));
			paymentTypeConditionLst
					.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.DEBIT_CARD.getCode()));
			paymentTypeConditionLst
					.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.NET_BANKING.getCode()));
			paymentTypeConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.EMI.getCode()));
			paymentTypeConditionLst
					.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.WALLET.getCode()));
			paymentTypeConditionLst
					.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.RECURRING_PAYMENT.getCode()));
			paymentTypeConditionLst
					.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.EXPRESS_PAY.getCode()));
			paymentTypeQuery.append("$in", paymentTypeConditionLst);

			if (!dateQuery.isEmpty()) {
				finalList.add(dateQuery);
			}
			
			if (!dateIndexConditionQuery.isEmpty()) {
				finalList.add(dateIndexConditionQuery);
			}

			if (!mopQuery.isEmpty()) {
				finalList.add(mopQuery);
			}
			if (!query.isEmpty()) {
				finalList.add(query);
			}
			if (!payIdquery.isEmpty()) {
				finalList.add(payIdquery);
			}

			if (!txnTypeConditionQuery.isEmpty()) {
				finalList.add(txnTypeConditionQuery);
			}

			BasicDBObject finalobjectQuery = new BasicDBObject("$and", finalList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			
			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
			BasicDBObject project = new BasicDBObject("$project", projectElement);
			
			BasicDBObject match = new BasicDBObject("$match", finalobjectQuery);
			List<BasicDBObject> pipeline = Arrays.asList(match, project);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			
		//	MongoCursor<Document> cursor = coll.find(finalobjectQuery).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString()).equals(PaymentType.CREDIT_CARD.getCode()))) {
					totalCredit++;

				} else if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
						.equals(PaymentType.DEBIT_CARD.getCode()))) {
					totalDebit++;

				} else if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
						.equals(PaymentType.NET_BANKING.getCode()))) {
					totalNet++;

				} else if ((!dbobj.getString(FieldType.PAYMENT_TYPE.toString())
						.equals(PaymentType.CREDIT_CARD.getCode()))
						|| (!dbobj.getString(FieldType.PAYMENT_TYPE.toString())
								.equals(PaymentType.DEBIT_CARD.getCode()))
						|| (!dbobj.getString(FieldType.PAYMENT_TYPE.toString())
								.equals(PaymentType.NET_BANKING.getCode()))) {
					other++;
				}

				pieChart.setTotalCredit(String.valueOf(totalCredit));
				pieChart.setTotalDebit(String.valueOf(totalDebit));
				pieChart.setNet(String.valueOf(totalNet));
				pieChart.setOther(String.valueOf(other));

			}

			cursor.close();
		} catch (ParseException exception) {
			logger.error("", exception);
		}

		return pieChart;
	}

	public Map<String, PieChart> totalTransactionRecord(String payId, String currency, String dateFrom, String dateTo,
			User sessionUser, String dayType, String subMerchantId, String paymentRegion, boolean saleReportFlag)
			throws SystemException {
		Map<String, PieChart> dateChart = new HashMap<String, PieChart>();

		int totalSuccess = 0;
		int totalFailed = 0;
		int totalRefund = 0;
		BasicDBObject txnTypeQuery = new BasicDBObject();
		List<BasicDBObject> statusConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> dateIndexConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject dateQuery = new BasicDBObject();
		BasicDBObject statusQuery = new BasicDBObject();
		BasicDBObject payIdquery = new BasicDBObject();
		BasicDBObject subUserIdQuery = new BasicDBObject();
		BasicDBObject paymentRegionQuery = new BasicDBObject();
		BasicDBObject subMerchantIdQuery = new BasicDBObject();
		try {
			if (!dateFrom.isEmpty()) {

				String currentDate = null;
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

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(dateFrom).toLocalizedPattern();
			String endString = new SimpleDateFormat(dateTo).toLocalizedPattern();

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

			for (String index : allDatesIndex) {
				dateIndexConditionLst.add(new BasicDBObject(FieldType.DATE_INDEX.getName(), index));
			}
			dateIndexConditionQuery.append("$or", dateIndexConditionLst);

			if (!payId.equalsIgnoreCase("ALL MERCHANTS")) {
				payIdquery.put(FieldType.PAY_ID.getName(), payId);

			}
			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				subMerchantIdQuery.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			if (!paymentRegion.equalsIgnoreCase("ALL")) {
				paymentRegionQuery.put(FieldType.PAYMENTS_REGION.getName(), paymentRegion);

			}
			// For reseller query , if all merchants are selected
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				payIdquery.put(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId());
			}

			if (sessionUser.getUserType().equals(UserType.SUBUSER)
					&& sessionUser.getSubUserType().equalsIgnoreCase("eposType")) {
				subUserIdQuery.put(FieldType.SUB_USER_ID.getName(), sessionUser.getPayId());
			}

			if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				payIdquery.put(FieldType.PAY_ID.getName(), sessionUser.getSuperMerchantId());
				subMerchantIdQuery.put(FieldType.SUB_MERCHANT_ID.getName(), sessionUser.getPayId());
			}

			BasicDBObject query = new BasicDBObject();
			
			if(StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				query.put(FieldType.CURRENCY_CODE.getName(), currency);
			}
			if (saleReportFlag) {
				txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
				txnTypeConditionLst
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.AUTHORISE.getName()));
			} else {
				// txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(),
				// TransactionType.CAPTURE.getName()));
				txnTypeConditionLst
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
			}
			txnTypeQuery.append("$or", txnTypeConditionLst);
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.APPROVED.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ERROR.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.BROWSER_CLOSED.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.INVALID.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName()));
			statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED_AT_ACQUIRER.getName()));

			statusQuery.append("$or", statusConditionLst);
			
			
			if (!query.isEmpty()) {
				allConditionQueryList.add(query);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryList.add(dateIndexConditionQuery);
			}
			if (!txnTypeQuery.isEmpty()) {
				allConditionQueryList.add(txnTypeQuery);
			}
			if (!statusQuery.isEmpty()) {
				allConditionQueryList.add(statusQuery);
			}
			if (!payIdquery.isEmpty()) {
				allConditionQueryList.add(payIdquery);
			}
			if (!subUserIdQuery.isEmpty()) {
				allConditionQueryList.add(subUserIdQuery);
			}
			if (!subMerchantIdQuery.isEmpty()) {
				allConditionQueryList.add(subMerchantIdQuery);
			}
			if (!paymentRegionQuery.isEmpty()) {
				allConditionQueryList.add(paymentRegionQuery);
			}

			BasicDBObject allCondi = new BasicDBObject("$and", allConditionQueryList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			MongoCursor<Document> cursor;

			// temp added till transaction collection will not have SUB_USER_ID field or any
			// epos flag
			if (sessionUser.getUserType().equals(UserType.SUBUSER)
					&& sessionUser.getSubUserType().equalsIgnoreCase("eposType")) {
				Set<String> orderIdSet = findBySubUserId(sessionUser.getPayId(), sessionUser.getParentPayId(), dateFrom,
						dateTo);
				BasicDBObject subQuery = new BasicDBObject();
				List<BasicDBObject> subQueryList = new ArrayList<BasicDBObject>();

				for (String orderId : orderIdSet) {
					subQueryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
				}
				subQuery.put("$or", subQueryList);

				BasicDBObject projectElement = new BasicDBObject();
				projectElement.put(FieldType.TXNTYPE.getName(), 1);
				projectElement.put(FieldType.RESPONSE_CODE.getName(), 1);
				projectElement.put(FieldType.STATUS.getName(), 1);
				projectElement.put(FieldType.CREATE_DATE.getName(), 1);
				projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
				projectElement.put(FieldType.AMOUNT.getName(), 1);
				projectElement.put(FieldType.TXN_ID.getName(), 1);
				projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);
				
				BasicDBObject project = new BasicDBObject("$project", projectElement);

				BasicDBObject match = new BasicDBObject("$match", subQuery);
				List<BasicDBObject> pipeline = Arrays.asList(match, project);
				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				cursor = output.iterator();

				// cursor = coll.find(subQuery).iterator();

			} else {

				BasicDBObject projectElement = new BasicDBObject();
				projectElement.put(FieldType.TXNTYPE.getName(), 1);
				projectElement.put(FieldType.RESPONSE_CODE.getName(), 1);
				projectElement.put(FieldType.STATUS.getName(), 1);
				projectElement.put(FieldType.CREATE_DATE.getName(), 1);
				projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
				projectElement.put(FieldType.AMOUNT.getName(), 1);
				projectElement.put(FieldType.TXN_ID.getName(), 1);
				projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);
				
				BasicDBObject project = new BasicDBObject("$project", projectElement);

				BasicDBObject match = new BasicDBObject("$match", allCondi);
				List<BasicDBObject> pipeline = Arrays.asList(match, project);
				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				cursor = output.iterator();

				// cursor = coll.find(allCondi).iterator();
			}

			while (cursor.hasNext()) {

				Document dbobj = cursor.next();

				String searchKey = null;
				String date = null;
				String txndat = dbobj.getString(FieldType.CREATE_DATE.getName());
				if (StringUtils.isNotBlank(txndat)) {
					String[] splitdate = txndat.split(" ");
					date = splitdate[0];
					String time = splitdate[1];

					if (dayType.equalsIgnoreCase("day") || dayType.equals("previousDay")) {
						String[] timeSplit = time.split(":");
						String hour = timeSplit[0];
						searchKey = hour;
					} else if (dayType.equalsIgnoreCase("year")) {
						String[] dateSplit = date.split("-");
						String month = dateSplit[1];
						searchKey = month;
					} else {

						searchKey = date;
					}
				}
				BigDecimal totalAmount = new BigDecimal("0.0");
				try {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))) {
						totalAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).setScale(2);
					} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.AMOUNT.getName()))) {
						totalAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
					}
				}catch(Exception ex) {
					logger.error("Amount is Not valid : ", ex);
				}
				if (!StringUtils.isEmpty(searchKey)) {
					if (dateChart.containsKey(searchKey)) {
						PieChart piechart1 = dateChart.get(searchKey);
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString()))) {
							switch (dbobj.getString(FieldType.TXNTYPE.toString())) {
							case "SALE":

								if ((dbobj.getString(FieldType.TXNTYPE.toString())
										.equals(TransactionType.SALE.getName())
										&& (StringUtils.isNotBlank(dbobj.getString(FieldType.RESPONSE_CODE.toString()))
												&& dbobj.getString(FieldType.RESPONSE_CODE.toString())
														.equals(ErrorType.SUCCESS.getCode()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
										&& dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.CAPTURED.getName()))
										|| (dbobj.getString(FieldType.TXNTYPE.toString())
												.equals(TransactionType.AUTHORISE.getName())
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
												&& dbobj.getString(FieldType.STATUS.toString())
														.equals(StatusType.APPROVED.getName()))) {

									String successTxn = piechart1.getTotalSuccess();
									if (successTxn == null || successTxn.equals("0")) {

										totalSuccess = 1;
										piechart1.setTotalSuccess(String.valueOf(totalSuccess));
										piechart1.setCreateDate(txndat);
										piechart1.setTotalAmountSuccess(String.valueOf(totalAmount));
										dateChart.put(searchKey, piechart1);
									} else {

										BigDecimal prevAmount = new BigDecimal(piechart1.getTotalAmountSuccess());
										prevAmount = prevAmount.add(totalAmount).setScale(2);

										totalSuccess = Integer.parseInt(piechart1.getTotalSuccess());

										totalSuccess++;
										piechart1.setTotalSuccess(String.valueOf(totalSuccess));
										piechart1.setTotalAmountSuccess(String.valueOf(prevAmount));
										piechart1.setCreateDate(txndat);

										dateChart.put(searchKey, piechart1);
									}

								} else if ((dbobj.getString(FieldType.TXNTYPE.toString())
										.equals(TransactionType.SALE.getName())
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
										&& (dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.FAILED.getName()) ||
												dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.ACQUIRER_DOWN.getName()) ||
												dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.FAILED_AT_ACQUIRER.getName()) ||
												dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.ERROR.getName())))
										
										|| (dbobj.getString(FieldType.TXNTYPE.toString())
												.equals(TransactionType.AUTHORISE.getName())
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
												&& (dbobj.getString(FieldType.STATUS.toString())
														.equals(StatusType.FAILED.getName()) ||
														dbobj.getString(FieldType.STATUS.toString())
														.equals(StatusType.ACQUIRER_DOWN.getName()) ||
														dbobj.getString(FieldType.STATUS.toString())
														.equals(StatusType.FAILED_AT_ACQUIRER.getName()) ||
														dbobj.getString(FieldType.STATUS.toString())
														.equals(StatusType.ERROR.getName())))) {
									String failTxn = piechart1.getTotalFailed();

									if (failTxn == null || failTxn.equals("0")) {
										totalFailed = 1;
										piechart1.setTotalFailed(String.valueOf(totalFailed));
										piechart1.setTxndate(date);
										piechart1.setCreateDate(txndat);
										piechart1.setTotalAmountFailed(String.valueOf(totalAmount.setScale(2)));
										dateChart.put(searchKey, piechart1);
									} else {

										BigDecimal prevAmount = new BigDecimal(piechart1.getTotalAmountFailed());
										prevAmount = prevAmount.add(totalAmount).setScale(2);

										totalFailed = Integer.parseInt(piechart1.getTotalFailed());
										totalFailed++;
										piechart1.setTotalFailed(String.valueOf(totalFailed));
										piechart1.setTxndate(date);
										piechart1.setCreateDate(txndat);
										piechart1.setTotalAmountFailed(String.valueOf(prevAmount));
										dateChart.put(searchKey, piechart1);
									}

								}

								else {
									// Do Nothing
								}

								break;

							case "REFUND":
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
										&& dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.CAPTURED.getName())
										&& dbobj.getString(FieldType.TXNTYPE.toString())
												.equals(TransactionType.REFUND.getName())) {

									String successTxn = piechart1.getTotalSuccess();
									// String refundTxn = piechart1.getTotalRefunded();
									if (successTxn == null || successTxn.equals("0")) {
										totalSuccess = 1;
										piechart1.setTotalSuccess(String.valueOf(totalSuccess));
										piechart1.setCreateDate(txndat);
										piechart1.setTotalAmountSuccess(String.valueOf(totalAmount.setScale(2)));
										dateChart.put(searchKey, piechart1);

									} else {
										BigDecimal prevAmount = new BigDecimal(piechart1.getTotalAmountSuccess());
										prevAmount = prevAmount.add(totalAmount).setScale(2);

										totalSuccess = Integer.parseInt(piechart1.getTotalSuccess());
										totalSuccess++;
										piechart1.setTotalSuccess(String.valueOf(totalSuccess));
										piechart1.setCreateDate(txndat);
										piechart1.setTotalAmountSuccess(String.valueOf(prevAmount));
										dateChart.put(searchKey, piechart1);
									}

								} else if (dbobj.getString(FieldType.TXNTYPE.toString())
										.equals(TransactionType.REFUND.getName())
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
										&& (dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.FAILED.getName()) ||
												dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.ACQUIRER_DOWN.getName()) ||
												dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.FAILED_AT_ACQUIRER.getName()) ||
												dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.ERROR.getName()))) {
									String failTxn = piechart1.getTotalFailed();
									if (failTxn == null || failTxn.equals("0")) {
										totalFailed = 1;
										piechart1.setTotalFailed(String.valueOf(totalFailed));
										piechart1.setTxndate(date);
										piechart1.setCreateDate(txndat);
										piechart1.setTotalAmountFailed(String.valueOf(totalAmount.setScale(2)));
										dateChart.put(searchKey, piechart1);
									} else {
										BigDecimal prevAmount = new BigDecimal(piechart1.getTotalAmountFailed());
										prevAmount = prevAmount.add(totalAmount).setScale(2);

										totalFailed = Integer.parseInt(piechart1.getTotalFailed());
										totalFailed++;
										piechart1.setTotalFailed(String.valueOf(totalFailed));
										piechart1.setTxndate(date);
										piechart1.setCreateDate(txndat);
										piechart1.setTotalAmountFailed(String.valueOf(prevAmount));
										dateChart.put(searchKey, piechart1);
									}

								}

								else {
									// Do Nothing
								}

								break;

							}
						}

					} else {
						PieChart piechart1 = new PieChart();
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString()))) {
							switch (dbobj.getString(FieldType.TXNTYPE.toString())) {
							case "SALE":
								if ((dbobj.getString(FieldType.TXNTYPE.toString())
										.equals(TransactionType.SALE.getName())
										&& (StringUtils.isNotBlank(dbobj.getString(FieldType.RESPONSE_CODE.toString()))
												&& dbobj.getString(FieldType.RESPONSE_CODE.toString())
														.equals(ErrorType.SUCCESS.getCode()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
										&& dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.CAPTURED.getName()))
										|| (dbobj.getString(FieldType.TXNTYPE.toString())
												.equals(TransactionType.AUTHORISE.getName())
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
												&& dbobj.getString(FieldType.STATUS.toString())
														.equals(StatusType.APPROVED.getName()))) {
									totalSuccess = 1;
									piechart1.setTotalSuccess(String.valueOf(totalSuccess));
									piechart1.setTotalFailed(String.valueOf(0));
									piechart1.setTotalRefunded(String.valueOf(0));
									piechart1.setTxndate(date);
									piechart1.setCreateDate(txndat);
									piechart1.setTotalAmountSuccess(String.valueOf(totalAmount));
									dateChart.put(searchKey, piechart1);

								} else if ((dbobj.getString(FieldType.TXNTYPE.toString())
										.equals(TransactionType.SALE.getName())
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
										&& (dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.FAILED.getName()) ||
												dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.ACQUIRER_DOWN.getName()) ||
												dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.FAILED_AT_ACQUIRER.getName()) ||
												dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.ERROR.getName())))
										|| (dbobj.getString(FieldType.TXNTYPE.toString())
												.equals(TransactionType.AUTHORISE.getName())
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
												&& (dbobj.getString(FieldType.STATUS.toString())
														.equals(StatusType.FAILED.getName()) ||
														dbobj.getString(FieldType.STATUS.toString())
														.equals(StatusType.ACQUIRER_DOWN.getName()) ||
														dbobj.getString(FieldType.STATUS.toString())
														.equals(StatusType.FAILED_AT_ACQUIRER.getName()) ||
														dbobj.getString(FieldType.STATUS.toString())
														.equals(StatusType.ERROR.getName())))) {
									String failTxn = piechart1.getTotalFailed();
									if (failTxn == null || failTxn.equals("0")) {
										totalFailed = 1;
										piechart1.setTotalFailed(String.valueOf(totalFailed));
										piechart1.setTotalSuccess(String.valueOf(0));
										piechart1.setTotalRefunded(String.valueOf(0));
										piechart1.setTxndate(date);
										piechart1.setCreateDate(txndat);
										piechart1.setTotalAmountFailed(String.valueOf(totalAmount));
										dateChart.put(searchKey, piechart1);
									} else {
										BigDecimal prevAmount = new BigDecimal(piechart1.getTotalAmountFailed());
										prevAmount = prevAmount.add(totalAmount).setScale(2);

										totalFailed = Integer.parseInt(piechart1.getTotalFailed());
										totalFailed++;
										piechart1.setTotalFailed(String.valueOf(totalFailed));
										piechart1.setTotalSuccess(String.valueOf(0));
										piechart1.setTotalRefunded(String.valueOf(0));
										piechart1.setTxndate(date);
										piechart1.setTotalAmountFailed(String.valueOf(prevAmount));
										piechart1.setCreateDate(txndat);

										dateChart.put(searchKey, piechart1);
									}

								}

								else {
									// Do Nothing
								}
								break;

							case "REFUND":

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
										&& dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.CAPTURED.getName())
										&& dbobj.getString(FieldType.TXNTYPE.toString())
												.equals(TransactionType.REFUND.getName())) {
									// totalRefund = 1;
									totalSuccess = 1;
									piechart1.setTotalRefunded(String.valueOf(0));
									piechart1.setTotalFailed(String.valueOf(0));
									piechart1.setTotalSuccess(String.valueOf(totalSuccess));
									piechart1.setTxndate(date);
									piechart1.setCreateDate(txndat);
									piechart1.setTotalAmountSuccess(String.valueOf(totalAmount));
									dateChart.put(searchKey, piechart1);
								} else if (dbobj.getString(FieldType.TXNTYPE.toString())
										.equals(TransactionType.REFUND.getName())
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
										&& (dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.FAILED.getName()) ||
												dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.ACQUIRER_DOWN.getName()) ||
												dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.FAILED_AT_ACQUIRER.getName()) ||
												dbobj.getString(FieldType.STATUS.toString())
												.equals(StatusType.ERROR.getName()))) {
									String failTxn = piechart1.getTotalFailed();
									if (failTxn == null || failTxn.equals("0")) {
										totalFailed = 1;
										piechart1.setTotalRefunded(String.valueOf(0));
										piechart1.setTotalFailed(String.valueOf(totalFailed));
										piechart1.setTotalSuccess(String.valueOf(0));
										piechart1.setTxndate(date);
										piechart1.setCreateDate(txndat);
										piechart1.setTotalAmountFailed(String.valueOf(totalAmount.setScale(2)));
										dateChart.put(searchKey, piechart1);

									} else {
										BigDecimal prevAmount = new BigDecimal(piechart1.getTotalAmountFailed());
										prevAmount = prevAmount.add(totalAmount).setScale(2);

										totalSuccess = Integer.parseInt(piechart1.getTotalSuccess());
										totalSuccess++;
										piechart1.setTotalRefunded(String.valueOf(0));
										piechart1.setTotalFailed(String.valueOf(0));
										piechart1.setTotalSuccess(String.valueOf(totalSuccess));
										piechart1.setTxndate(date);
										piechart1.setCreateDate(txndat);
										piechart1.setTotalAmountFailed(String.valueOf(prevAmount));

										dateChart.put(searchKey, piechart1);
									}

								}

								else {
									// Do Nothing
								}

								break;

							}
						}
					}

				}

			}

			cursor.close();
		} catch (Exception exception) {
			logger.error("Exception", exception);

		}
		return dateChart;
	}

	public Statistics statisticsSummary(String payId, String currency, String fromDate, String toDate, User sessionUser,
			Statistics statistics, String subMerchantId, String paymentRegion, boolean saleReportFlag) {

		logger.info("Statistics query generation started ");
		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> saleOrAuthList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> dateIndexConditionLst = new ArrayList<BasicDBObject>();
		
		fromDate += " 00:00:00";
		toDate += " 23:59:59";
		BasicDBObject currencyQuery = new BasicDBObject();

		BasicDBObject payIdquery = new BasicDBObject();
		BasicDBObject paymentRegionQuery = new BasicDBObject();
		BasicDBObject subUserIdQuery = new BasicDBObject();

		BasicDBObject subMerchantIdQuery = new BasicDBObject();
		BasicDBObject resellerIdQuery = new BasicDBObject();
		try {

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
			
			
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(fromDate).toLocalizedPattern();
			String endString = new SimpleDateFormat(toDate).toLocalizedPattern();

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

			for (String index : allDatesIndex) {
				dateIndexConditionLst.add(new BasicDBObject(FieldType.DATE_INDEX.getName(),index));
			}
			
			dateIndexConditionQuery.append("$or", dateIndexConditionLst);

			// temp added till transaction collection will not have SUB_USER_ID field or any
			// epos flag
			if (sessionUser.getUserType().equals(UserType.SUBUSER)
					&& sessionUser.getSubUserType().equalsIgnoreCase("eposType")) {
				Set<String> orderIdSet = findBySubUserId(sessionUser.getPayId(), sessionUser.getParentPayId(), fromDate,
						toDate);
				BasicDBObject subQuery = new BasicDBObject();
				List<BasicDBObject> subQueryList = new ArrayList<BasicDBObject>();

				for (String orderId : orderIdSet) {
					subQueryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
				}
				if (subQueryList.isEmpty()) {
					subUserIdQuery.put(FieldType.SUB_USER_ID.getName(), sessionUser.getPayId());
				} else {
					subUserIdQuery.put("$or", subQueryList);
				}

			} else {

				if (!payId.equalsIgnoreCase("ALL MERCHANTS")) {
					payIdquery.put(FieldType.PAY_ID.getName(), payId);
				}
				if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
					subMerchantIdQuery.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
				}

				if (sessionUser.getUserType().equals(UserType.RESELLER)) {
					resellerIdQuery.put(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId());
				}
				if (sessionUser.getUserType().equals(UserType.PARENTMERCHANT)) {
					payIdquery.put(FieldType.PARENT_PAY_ID.getName(), sessionUser.getPayId());
				}
//			if (sessionUser.getUserType().equals(UserType.SUBUSER) && sessionUser.getSubUserType().equalsIgnoreCase("eposType")) {
//				subUserIdQuery.put(FieldType.SUB_USER_ID.getName(), sessionUser.getPayId());
//			}
				if (!paymentRegion.equalsIgnoreCase("ALL")) {
					paymentRegionQuery.put(FieldType.PAYMENTS_REGION.getName(), paymentRegion);
				}

			}

			if (!currency.equalsIgnoreCase("ALL")) {
				currencyQuery.put(FieldType.CURRENCY_CODE.getName(), currency);
			} else {
				PropertiesManager propertiesManager = new PropertiesManager();
				Map<String, String> allCurrencyMap;
				allCurrencyMap = propertiesManager.getAllProperties(alphabaticFileName);
				for (Map.Entry<String, String> entry : allCurrencyMap.entrySet()) {
					currencyConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), entry.getKey()));
				}
				currencyQuery.append("$or", currencyConditionLst);
			}
			
			
			BasicDBObject refundConditionQuery = null;
			BasicDBObject saleConditionQuery = null;
			if(saleReportFlag) {
				//List<BasicDBObject> saleConditionList = new ArrayList<BasicDBObject>();
				//saleConditionList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getName()));
				//saleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
//				saleConditionList.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));

				saleConditionQuery = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getName());
				saleOrAuthList.add(saleConditionQuery);
			}else {
//				List<BasicDBObject> refundConditionList = new ArrayList<BasicDBObject>();
//				refundConditionList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName()));
				//refundConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
	
				refundConditionQuery = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName());
			}
			// FAIL QUERY START //

			List<BasicDBObject> failConditionList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> failTypeConditionLst = new ArrayList<BasicDBObject>();
			failTypeConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
			failTypeConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ERROR.getName()));
			failTypeConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName()));
			failTypeConditionLst
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED_AT_ACQUIRER.getName()));

			BasicDBObject failQuery2 = new BasicDBObject();
			failQuery2.append("$or", failTypeConditionLst);

			failConditionList.add(failQuery2);

			BasicDBObject failConditionQuery = new BasicDBObject("$and", failConditionList);
			// FAIL QUERY END //

			// REJECTED QUERY START //

			List<BasicDBObject> rejectedConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject rejectedQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
			BasicDBObject delicnedQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.DECLINED.getName());
			BasicDBObject authFailedQuery = new BasicDBObject(FieldType.STATUS.getName(),
					StatusType.AUTHENTICATION_FAILED.getName());
			BasicDBObject rejectedByPgQuery = new BasicDBObject(FieldType.STATUS.getName(),
					StatusType.REJECTED_BY_PG.getName());
			rejectedConditionList.add(rejectedQuery);
			rejectedConditionList.add(delicnedQuery);
			rejectedConditionList.add(authFailedQuery);
			rejectedConditionList.add(rejectedByPgQuery);
			BasicDBObject rejectedConditionQuery = new BasicDBObject("$or", rejectedConditionList);

			// REJECTED QUERY END //

			// DROPPED QUERY START //
			List<BasicDBObject> droppedConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject droppedQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName());
			BasicDBObject AcqTimeoutQuery = new BasicDBObject(FieldType.STATUS.getName(),
					StatusType.ACQUIRER_TIMEOUT.getName());

			droppedConditionList.add(droppedQuery);
			droppedConditionList.add(AcqTimeoutQuery);

			BasicDBObject droppedConditionQuery = new BasicDBObject("$or", droppedConditionList);

			// DROPPED QUERY END //

			// CANCELLED QUERY START //

			List<BasicDBObject> cancelledConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject cancelledQuery = new BasicDBObject(FieldType.STATUS.getName(),
					StatusType.CANCELLED.getName());
			BasicDBObject browserClosedQuery = new BasicDBObject(FieldType.STATUS.getName(),
					StatusType.BROWSER_CLOSED.getName());
			cancelledConditionList.add(cancelledQuery);
			cancelledConditionList.add(browserClosedQuery);
			BasicDBObject cancelledConditionQuery = new BasicDBObject("$or", cancelledConditionList);

			// CANCELLED QUERY END //

			// FRAUD QUERY START //

			List<BasicDBObject> fraudConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject fraudQuery = new BasicDBObject(FieldType.STATUS.getName(),
					StatusType.DENIED_BY_FRAUD.getName());
			BasicDBObject deniedQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED.getName());
			fraudConditionList.add(fraudQuery);
			fraudConditionList.add(deniedQuery);
			BasicDBObject fraudConditionQuery = new BasicDBObject("$or", fraudConditionList);

			// FRAUD QUERY END //

			// INVALID QUERY START //

			List<BasicDBObject> invalidConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject invalidQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			BasicDBObject duplicateQuery = new BasicDBObject(FieldType.STATUS.getName(),
					StatusType.DUPLICATE.getName());
			invalidConditionList.add(invalidQuery);
			invalidConditionList.add(duplicateQuery);
			BasicDBObject invalidConditionQuery = new BasicDBObject("$or", invalidConditionList);

			// INVALID QUERY END //

			List<BasicDBObject> allConditionQueryListForfail = new ArrayList<BasicDBObject>();
			if (!currencyQuery.isEmpty()) {
				allConditionQueryListForfail.add(currencyQuery);
			}
			
			if (saleConditionQuery != null && !saleConditionQuery.isEmpty()) {
				allConditionQueryListForfail.add(failConditionQuery);
				allConditionQueryListForfail.add(saleConditionQuery);
				
			}
			
			if (refundConditionQuery != null && !refundConditionQuery.isEmpty()) {
				allConditionQueryListForfail.add(failConditionQuery);
				allConditionQueryListForfail.add(refundConditionQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryListForfail.add(dateQuery);
			}
			
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryListForfail.add(dateIndexConditionQuery);
			}
			
			if (!payIdquery.isEmpty()) {
				allConditionQueryListForfail.add(payIdquery);
			}
			if (!subMerchantIdQuery.isEmpty()) {
				allConditionQueryListForfail.add(subMerchantIdQuery);
			}
			if (!subUserIdQuery.isEmpty()) {
				allConditionQueryListForfail.add(subUserIdQuery);
			}
			if (!resellerIdQuery.isEmpty()) {
				allConditionQueryListForfail.add(resellerIdQuery);
			}
			if (!paymentRegionQuery.isEmpty()) {
				allConditionQueryListForfail.add(paymentRegionQuery);
			}

			BasicDBObject allConditionQueryObjforfail = new BasicDBObject("$and", allConditionQueryListForfail);

			List<BasicDBObject> allConditionQueryListForReject = new ArrayList<BasicDBObject>();
			if (!currencyQuery.isEmpty()) {
				allConditionQueryListForReject.add(currencyQuery);
			}
			
			if (saleConditionQuery != null && !saleConditionQuery.isEmpty()) {
				allConditionQueryListForReject.add(rejectedConditionQuery);
				allConditionQueryListForReject.add(saleConditionQuery);
			}
			
			if (refundConditionQuery != null && !refundConditionQuery.isEmpty()) {
				allConditionQueryListForReject.add(rejectedConditionQuery);
				allConditionQueryListForReject.add(refundConditionQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryListForReject.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryListForReject.add(dateIndexConditionQuery);
			}
			if (!payIdquery.isEmpty()) {
				allConditionQueryListForReject.add(payIdquery);
			}
			if (!subMerchantIdQuery.isEmpty()) {
				allConditionQueryListForReject.add(subMerchantIdQuery);
			}
			if (!subUserIdQuery.isEmpty()) {
				allConditionQueryListForReject.add(subUserIdQuery);
			}
			if (!resellerIdQuery.isEmpty()) {
				allConditionQueryListForReject.add(resellerIdQuery);
			}
			if (!paymentRegionQuery.isEmpty()) {
				allConditionQueryListForReject.add(paymentRegionQuery);
			}

			BasicDBObject allConditionQueryObjforReject = new BasicDBObject("$and", allConditionQueryListForReject);

			List<BasicDBObject> allConditionQueryListForDropped = new ArrayList<BasicDBObject>();
			if (!currencyQuery.isEmpty()) {
				allConditionQueryListForDropped.add(currencyQuery);
			}
			
			if (saleConditionQuery != null && !saleConditionQuery.isEmpty()) {
				allConditionQueryListForDropped.add(droppedConditionQuery);
				allConditionQueryListForDropped.add(saleConditionQuery);
			}
			
			if (refundConditionQuery != null && !refundConditionQuery.isEmpty()) {
				allConditionQueryListForDropped.add(droppedConditionQuery);
				allConditionQueryListForDropped.add(refundConditionQuery);
			}

			if (!dateQuery.isEmpty()) {
				allConditionQueryListForDropped.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryListForDropped.add(dateIndexConditionQuery);
			}
			
			if (!payIdquery.isEmpty()) {
				allConditionQueryListForDropped.add(payIdquery);
			}
			if (!subMerchantIdQuery.isEmpty()) {
				allConditionQueryListForDropped.add(subMerchantIdQuery);
			}
			if (!subUserIdQuery.isEmpty()) {
				allConditionQueryListForDropped.add(subUserIdQuery);
			}
			if (!resellerIdQuery.isEmpty()) {
				allConditionQueryListForDropped.add(resellerIdQuery);
			}
			if (!paymentRegionQuery.isEmpty()) {
				allConditionQueryListForDropped.add(paymentRegionQuery);
			}

			BasicDBObject allConditionQueryObjforDropped = new BasicDBObject("$and", allConditionQueryListForDropped);

			List<BasicDBObject> allConditionQueryListForCancelled = new ArrayList<BasicDBObject>();
			if (!currencyQuery.isEmpty()) {
				allConditionQueryListForCancelled.add(currencyQuery);
			}
			
			if (saleConditionQuery != null && !saleConditionQuery.isEmpty()) {
				allConditionQueryListForCancelled.add(cancelledConditionQuery);
				allConditionQueryListForCancelled.add(saleConditionQuery);
			}
			
			if (refundConditionQuery != null && !refundConditionQuery.isEmpty()) {
				allConditionQueryListForCancelled.add(cancelledConditionQuery);
				allConditionQueryListForCancelled.add(refundConditionQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryListForCancelled.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryListForCancelled.add(dateIndexConditionQuery);
			}
			if (!payIdquery.isEmpty()) {
				allConditionQueryListForCancelled.add(payIdquery);
			}
			if (!subMerchantIdQuery.isEmpty()) {
				allConditionQueryListForCancelled.add(subMerchantIdQuery);
			}
			if (!subUserIdQuery.isEmpty()) {
				allConditionQueryListForCancelled.add(subUserIdQuery);
			}
			if (!resellerIdQuery.isEmpty()) {
				allConditionQueryListForCancelled.add(resellerIdQuery);
			}
			if (!paymentRegionQuery.isEmpty()) {
				allConditionQueryListForCancelled.add(paymentRegionQuery);
			}

			BasicDBObject allConditionQueryObjforCancelled = new BasicDBObject("$and",
					allConditionQueryListForCancelled);

			List<BasicDBObject> allConditionQueryListForFraud = new ArrayList<BasicDBObject>();
			if (!currencyQuery.isEmpty()) {
				allConditionQueryListForFraud.add(currencyQuery);
			}
			
			if (saleConditionQuery != null && !saleConditionQuery.isEmpty()) {
				allConditionQueryListForFraud.add(fraudConditionQuery);
				allConditionQueryListForFraud.add(saleConditionQuery);
			}
			
			if (refundConditionQuery != null && !refundConditionQuery.isEmpty()) {
				allConditionQueryListForFraud.add(fraudConditionQuery);
				allConditionQueryListForFraud.add(refundConditionQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryListForFraud.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryListForFraud.add(dateIndexConditionQuery);
			}
			if (!payIdquery.isEmpty()) {
				allConditionQueryListForFraud.add(payIdquery);
			}
			if (!subMerchantIdQuery.isEmpty()) {
				allConditionQueryListForFraud.add(subMerchantIdQuery);
			}
			if (!subUserIdQuery.isEmpty()) {
				allConditionQueryListForFraud.add(subUserIdQuery);
			}
			if (!resellerIdQuery.isEmpty()) {
				allConditionQueryListForFraud.add(resellerIdQuery);
			}
			if (!paymentRegionQuery.isEmpty()) {
				allConditionQueryListForFraud.add(paymentRegionQuery);
			}

			BasicDBObject allConditionQueryObjforFraud = new BasicDBObject("$and", allConditionQueryListForFraud);

			List<BasicDBObject> allConditionQueryListForInvalid = new ArrayList<BasicDBObject>();
//			if (!currencyQuery.isEmpty()) {
//				allConditionQueryListForInvalid.add(currencyQuery);
//			}
			
			if (saleConditionQuery != null && !saleConditionQuery.isEmpty()) {
				allConditionQueryListForInvalid.add(invalidConditionQuery);
				allConditionQueryListForInvalid.add(saleConditionQuery);
			}
			
			if (refundConditionQuery != null && !refundConditionQuery.isEmpty()) {
				allConditionQueryListForInvalid.add(invalidConditionQuery);
				allConditionQueryListForInvalid.add(refundConditionQuery);
			}
			
//			if (!invalidConditionQuery.isEmpty()) {
//				allConditionQueryListForInvalid.add(invalidConditionQuery);
//			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryListForInvalid.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryListForInvalid.add(dateIndexConditionQuery);
			}
			if (!payIdquery.isEmpty()) {
				allConditionQueryListForInvalid.add(payIdquery);
			}
			if (!subMerchantIdQuery.isEmpty()) {
				allConditionQueryListForInvalid.add(subMerchantIdQuery);
			}
			if (!subUserIdQuery.isEmpty()) {
				allConditionQueryListForInvalid.add(subUserIdQuery);
			}
			if (!resellerIdQuery.isEmpty()) {
				allConditionQueryListForInvalid.add(resellerIdQuery);
			}
			if (!paymentRegionQuery.isEmpty()) {
				allConditionQueryListForInvalid.add(paymentRegionQuery);
			}

			BasicDBObject allConditionQueryObjforInvalid = new BasicDBObject("$and", allConditionQueryListForInvalid);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

			logger.info("Statistics All query start ");

			long totalfail = 0;
			long totalrejectedDeclined = 0;
			long totalcancelled = 0;
			long totalfraud = 0;
			long totalinvalid = 0;
			long totaldropped = 0;

			totalfail = coll.countDocuments(allConditionQueryObjforfail);
			totalrejectedDeclined = coll.countDocuments(allConditionQueryObjforReject);
			totalcancelled = coll.countDocuments(allConditionQueryObjforCancelled);
			totalfraud = coll.countDocuments(allConditionQueryObjforFraud);
			totalinvalid = coll.countDocuments(allConditionQueryObjforInvalid);
			totaldropped = coll.countDocuments(allConditionQueryObjforDropped);

			logger.info("Statistics All query end ");

			statistics.setTotalFailed((String.valueOf(totalfail)));
			statistics.setTotalRejectedDeclined(String.valueOf(totalrejectedDeclined));
			statistics.setTotalDropped(String.valueOf(totaldropped));
			statistics.setTotalCancelled(String.valueOf(totalcancelled));
			statistics.setTotalFraud(String.valueOf(totalfraud));
			statistics.setTotalInvalid(String.valueOf(totalinvalid));
		} catch (Exception exception) {
			logger.error("Exception", exception);

		} finally {

		}
		return statistics;

	}

	public Statistics statisticsSummaryCapture(String payId, String currency, String fromDate, String toDate,
			User sessionUser, Statistics statistics, String subMerchantId, String paymentRegion) {

		logger.info("Statistics capture query generation started ");
		BigDecimal totalApproved = BigDecimal.ZERO;
		BigDecimal grossTotalApproved = BigDecimal.ZERO;

		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> saleOrAuthList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> dateIndexCondList = new ArrayList<BasicDBObject>();
		BasicDBObject currencyQuery = new BasicDBObject();
		BasicDBObject payIdquery = new BasicDBObject();
		BasicDBObject subUserIdQuery = new BasicDBObject();
		BasicDBObject paymentRegionQuery = new BasicDBObject();
		BasicDBObject subMerchantIdQuery = new BasicDBObject();
		BasicDBObject resellerQuery = new BasicDBObject();
		fromDate = fromDate + " 00:00:00";
		toDate = toDate + " 23:59:59";
		try {

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
			
			
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(fromDate).toLocalizedPattern();
			String endString = new SimpleDateFormat(toDate).toLocalizedPattern();

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

			for (String index : allDatesIndex) {
				dateIndexCondList.add(new BasicDBObject(FieldType.DATE_INDEX.getName(),index));
			}
			
			dateIndexConditionQuery.append("$or", dateIndexCondList);
			
			if (sessionUser.getUserType().equals(UserType.PARENTMERCHANT)) {
				payIdquery.put(FieldType.PARENT_PAY_ID.getName(), sessionUser.getPayId());
			} else if (!payId.equalsIgnoreCase("ALL MERCHANTS")) {
					payIdquery.put(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				subMerchantIdQuery.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerQuery.put(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId());
			}
			if (sessionUser.getUserType().equals(UserType.SUBUSER)
					&& sessionUser.getSubUserType().equalsIgnoreCase("eposType")) {
				subUserIdQuery.put(FieldType.SUB_USER_ID.getName(), sessionUser.getPayId());
			}

			if (StringUtils.isNotBlank(paymentRegion) && !paymentRegion.equalsIgnoreCase("ALL")) {
				paymentRegionQuery.put(FieldType.PAYMENTS_REGION.getName(), paymentRegion);
			}

			if (!currency.equalsIgnoreCase("ALL")) {
				currencyQuery.put(FieldType.CURRENCY_CODE.getName(), currency);
			} else {
				PropertiesManager propertiesManager = new PropertiesManager();
				Map<String, String> allCurrencyMap;
				allCurrencyMap = propertiesManager.getAllProperties(alphabaticFileName);
				for (Map.Entry<String, String> entry : allCurrencyMap.entrySet()) {
					currencyConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), entry.getKey()));
				}
				currencyQuery.append("$or", currencyConditionLst);
			}
			List<BasicDBObject> saleConditionList = new ArrayList<BasicDBObject>();
			saleConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleConditionList);
			saleOrAuthList.add(saleConditionQuery);
			BasicDBObject authndSaleConditionQuery = new BasicDBObject("$and", saleOrAuthList);

			List<BasicDBObject> approvedAmountList = new ArrayList<BasicDBObject>();
			approvedAmountList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			approvedAmountList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			if (!currencyQuery.isEmpty()) {
				allConditionQueryList.add(currencyQuery);
			}

			if (!authndSaleConditionQuery.isEmpty()) {
				allConditionQueryList.add(authndSaleConditionQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryList.add(dateIndexConditionQuery);
			}
			if (!payIdquery.isEmpty()) {
				allConditionQueryList.add(payIdquery);
			}
			if (!resellerQuery.isEmpty()) {
				allConditionQueryList.add(resellerQuery);
			}
			if (!subUserIdQuery.isEmpty()) {
				allConditionQueryList.add(subUserIdQuery);
			}

			if (!subMerchantIdQuery.isEmpty()) {
				allConditionQueryList.add(subMerchantIdQuery);
			}

			if (!paymentRegionQuery.isEmpty()) {
				allConditionQueryList.add(paymentRegionQuery);
			}

			BasicDBObject allConditionQueryObjforsale = new BasicDBObject("$and", allConditionQueryList);

			List<BasicDBObject> allConditionQueryListForApproved = new ArrayList<BasicDBObject>();
			if (!currencyQuery.isEmpty()) {
				allConditionQueryListForApproved.add(currencyQuery);
			}

			if (!dateQuery.isEmpty()) {
				allConditionQueryListForApproved.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryListForApproved.add(dateIndexConditionQuery);
			}
			if (!payIdquery.isEmpty()) {
				allConditionQueryListForApproved.add(payIdquery);
			}
			if (!subUserIdQuery.isEmpty()) {
				allConditionQueryListForApproved.add(subUserIdQuery);
			}
			if (!subMerchantIdQuery.isEmpty()) {
				allConditionQueryListForApproved.add(subMerchantIdQuery);
			}
			if (!paymentRegionQuery.isEmpty()) {
				allConditionQueryListForApproved.add(paymentRegionQuery);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			MongoCursor<Document> cursor;
			MongoCursor<Document> cursor2 = null;

			// temp added till transaction collection will not have SUB_USER_ID field or any
			// epos flag
			if (sessionUser.getUserType().equals(UserType.SUBUSER)
					&& sessionUser.getSubUserType().equalsIgnoreCase("eposType")) {
				Set<String> orderIdSet = findBySubUserId(sessionUser.getPayId(), sessionUser.getParentPayId(), fromDate,
						toDate);
				BasicDBObject subQuery = new BasicDBObject();
				List<BasicDBObject> finalQueryList = new ArrayList<BasicDBObject>();
				BasicDBObject finalQuery = new BasicDBObject();
				List<BasicDBObject> orderIdList = new ArrayList<BasicDBObject>();

				for (String orderId : orderIdSet) {
					orderIdList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
				}
				if (orderIdList.isEmpty()) {
					cursor = null;
				} else {
					subQuery.put("$or", orderIdList);
					finalQueryList.add(subQuery);
					finalQueryList.add(saleConditionQuery);
					finalQuery.put("$and", finalQueryList);
					
					BasicDBObject groupQuery=new BasicDBObject();
					groupQuery.put("_id", "TOTAL_SALE");
					groupQuery.put(FieldType.TOTAL_AMOUNT.getName(), new BasicDBObject("$sum",new BasicDBObject("$toDouble","$TOTAL_AMOUNT")));
					groupQuery.put(FieldType.AMOUNT.getName(), new BasicDBObject("$sum",new BasicDBObject("$toDouble","$AMOUNT")));
					groupQuery.put("count", new BasicDBObject("$sum",1));
					
					BasicDBObject projectElement = new BasicDBObject();
					projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
					projectElement.put(FieldType.AMOUNT.getName(), 1);
					
					BasicDBObject project = new BasicDBObject("$project", projectElement);
					BasicDBObject group = new BasicDBObject("$group", groupQuery);
					
					BasicDBObject match = new BasicDBObject("$match", finalQuery);
					List<BasicDBObject> pipeline = Arrays.asList(match,group, project);
					AggregateIterable<Document> output = coll.aggregate(pipeline);
					output.allowDiskUse(true);
					cursor = output.iterator();
//					cursor = coll.find(finalQuery).iterator();
				}

			} else {
				
				List<BasicDBObject> finalQueryList = new ArrayList<BasicDBObject>();
				BasicDBObject finalQuery = new BasicDBObject();
				BasicDBObject realTimeQuery = new BasicDBObject();
				realTimeQuery.put(FieldType.TXN_CAPTURE_FLAG.toString(), Constants.REAL_TIME_TXN.getValue());
				
				BasicDBObject query=new BasicDBObject("$and",allConditionQueryList);
				finalQueryList.add(realTimeQuery);
				finalQueryList.add(query);
				finalQuery.put("$and", finalQueryList);
				
//				BasicDBObject projectElement = new BasicDBObject();
//				projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
//				projectElement.put(FieldType.AMOUNT.getName(), 1);
//				projectElement.put(FieldType.TXN_CAPTURE_FLAG.getName(), 1);
				
				BasicDBObject groupQuery=new BasicDBObject();
				groupQuery.put("_id", null);
				groupQuery.put(FieldType.TOTAL_AMOUNT.getName(), new BasicDBObject("$sum",new BasicDBObject("$toDouble","$TOTAL_AMOUNT")));
				groupQuery.put(FieldType.AMOUNT.getName(), new BasicDBObject("$sum",new BasicDBObject("$toDouble","$AMOUNT")));
				groupQuery.put(FieldType.COUNT.getName(), new BasicDBObject("$sum",1));
				
				
				BasicDBObject group = new BasicDBObject("$group", groupQuery);
				
				BasicDBObject match = new BasicDBObject("$match", allConditionQueryObjforsale);
				List<BasicDBObject> pipeline = Arrays.asList(match, group);
				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				cursor = output.iterator();
				
				
				
				BasicDBObject match2 = new BasicDBObject("$match", finalQuery);
				List<BasicDBObject> pipeline2 = Arrays.asList(match2, group);
				AggregateIterable<Document> output2 = coll.aggregate(pipeline2);
				output2.allowDiskUse(true);
				cursor2 = output2.iterator();
				
			}

			int totalsucess = 0;
			int totalGrossSucess = 0;
			statistics.setTotalGrossSuccess(String.valueOf(totalGrossSucess));
			logger.info("Statistics Approved amount calculation started ");
			if (cursor != null) {
				if (cursor.hasNext()) {
					
					Document dbobj = cursor.next();
					logger.info("total Gross Amount and count "+dbobj);

					BigDecimal grossAddApproved = new BigDecimal(dbobj.getDouble(FieldType.TOTAL_AMOUNT.getName()));
					grossTotalApproved = grossTotalApproved.add(grossAddApproved).setScale(2, RoundingMode.HALF_DOWN);
					
					statistics.setTotalGrossSuccess(String.valueOf(dbobj.getInteger(FieldType.COUNT.getName(),0)));
					statistics.setGrossApprovedAmount(String.valueOf(grossTotalApproved));

				}else{
					statistics.setTotalGrossSuccess("0");
					statistics.setGrossApprovedAmount("0.00");
				}
				cursor.close();
			}else{
				statistics.setTotalGrossSuccess("0");
				statistics.setGrossApprovedAmount("0.00");
			}
			
			if (cursor2 != null) {
				if (cursor2.hasNext()) {
					
					Document dbobj = cursor2.next();
					
					BigDecimal addApproved = new BigDecimal(dbobj.getDouble(FieldType.TOTAL_AMOUNT.getName()));
					totalApproved = totalApproved.add(addApproved).setScale(2, RoundingMode.HALF_DOWN);
					
					statistics.setApprovedAmount(String.valueOf(totalApproved));
					statistics.setTotalSuccess(String.valueOf(dbobj.getInteger(FieldType.COUNT.getName())));
				}else{
					statistics.setApprovedAmount("0.00");
					statistics.setTotalSuccess("0");
				}
				cursor2.close();
			}else{
				
				statistics.setApprovedAmount("0.00");
				statistics.setTotalSuccess("0");
			}

			logger.info("Statistics Approved amount calculation end ");
			
		} catch (Exception exception) {
			logger.error("Exception", exception);

		}
		return statistics;

	}

	public Statistics statisticsSummaryRefund(String payId, String currency, String fromDate, String toDate,
			User sessionUser, Statistics statistics, String subMerchantId, String paymentRegion) {

		logger.info("Statistics refund query generation started ");
		BigDecimal totalRefunded = BigDecimal.ZERO;

		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> saleOrAuthList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> dateIndexCondList = new ArrayList<BasicDBObject>();
		BasicDBObject currencyQuery = new BasicDBObject();
		BasicDBObject payIdquery = new BasicDBObject();
		BasicDBObject subUserIdQuery = new BasicDBObject();
		BasicDBObject paymentRegionQuery = new BasicDBObject();
		BasicDBObject subMerchantIdQuery = new BasicDBObject();
		BasicDBObject resellerIdquery = new BasicDBObject();
		fromDate = fromDate + " 00:00:00";
		toDate = toDate + " 23:59:59";
		try {

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
			
			
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(fromDate).toLocalizedPattern();
			String endString = new SimpleDateFormat(toDate).toLocalizedPattern();

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
			
			for (String index : allDatesIndex) {
				dateIndexCondList.add(new BasicDBObject(FieldType.DATE_INDEX.getName(),index));
			}
			
			dateIndexConditionQuery.append("$or", dateIndexCondList);
			
			if (sessionUser.getUserType().equals(UserType.PARENTMERCHANT)) {
				payIdquery.put(FieldType.PARENT_PAY_ID.getName(), sessionUser.getPayId());
				
			}else if (!payId.equalsIgnoreCase("ALL MERCHANTS")) {
				payIdquery.put(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				subMerchantIdQuery.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerIdquery.put(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId());
			}
			if (sessionUser.getUserType().equals(UserType.SUBUSER)
					&& sessionUser.getSubUserType().equalsIgnoreCase("eposType")) {
				subUserIdQuery.put(FieldType.SUB_USER_ID.getName(), sessionUser.getPayId());
			}

			if (StringUtils.isNotBlank(paymentRegion) && !paymentRegion.equalsIgnoreCase("ALL")) {
				paymentRegionQuery.put(FieldType.PAYMENTS_REGION.getName(), paymentRegion);
			}

			if (!currency.equalsIgnoreCase("ALL")) {
				currencyQuery.put(FieldType.CURRENCY_CODE.getName(), currency);
			} else {
				PropertiesManager propertiesManager = new PropertiesManager();
				Map<String, String> allCurrencyMap;
				allCurrencyMap = propertiesManager.getAllProperties(alphabaticFileName);
				for (Map.Entry<String, String> entry : allCurrencyMap.entrySet()) {
					currencyConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), entry.getKey()));
				}
				currencyQuery.append("$or", currencyConditionLst);
			}
			List<BasicDBObject> saleConditionList = new ArrayList<BasicDBObject>();
			saleConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
			saleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleConditionList);
			saleOrAuthList.add(saleConditionQuery);
			BasicDBObject authndSaleConditionQuery = new BasicDBObject("$and", saleOrAuthList);

			List<BasicDBObject> approvedAmountList = new ArrayList<BasicDBObject>();
			approvedAmountList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
			approvedAmountList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			if (!currencyQuery.isEmpty()) {
				allConditionQueryList.add(currencyQuery);
			}

			if (!authndSaleConditionQuery.isEmpty()) {
				allConditionQueryList.add(authndSaleConditionQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryList.add(dateIndexConditionQuery);
			}
			if (!payIdquery.isEmpty()) {
				allConditionQueryList.add(payIdquery);
			}
			if (!resellerIdquery.isEmpty()) {
				allConditionQueryList.add(resellerIdquery);
			}
			if (!subUserIdQuery.isEmpty()) {
				allConditionQueryList.add(subUserIdQuery);
			}
			if (!subMerchantIdQuery.isEmpty()) {
				allConditionQueryList.add(subMerchantIdQuery);
			}
			if (!paymentRegionQuery.isEmpty()) {
				allConditionQueryList.add(paymentRegionQuery);
			}
			BasicDBObject allConditionQueryObjforsale = new BasicDBObject("$and", allConditionQueryList);

			List<BasicDBObject> allConditionQueryListForApproved = new ArrayList<BasicDBObject>();
			if (!currencyQuery.isEmpty()) {
				allConditionQueryListForApproved.add(currencyQuery);
			}

			if (!dateQuery.isEmpty()) {
				allConditionQueryListForApproved.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryListForApproved.add(dateIndexConditionQuery);
			}
			
			if (!subUserIdQuery.isEmpty()) {
				allConditionQueryListForApproved.add(subUserIdQuery);
			}
			if (!subMerchantIdQuery.isEmpty()) {
				allConditionQueryListForApproved.add(subMerchantIdQuery);
			}
			if (!payIdquery.isEmpty()) {
				allConditionQueryListForApproved.add(payIdquery);
			}
			if (!paymentRegionQuery.isEmpty()) {
				allConditionQueryListForApproved.add(paymentRegionQuery);
			}
			
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

			MongoCursor<Document> cursor;

			// temp added till transaction collection will not have SUB_USER_ID field or any
			// epos flag
			if (sessionUser.getUserType().equals(UserType.SUBUSER)
					&& sessionUser.getSubUserType().equalsIgnoreCase("eposType")) {
				Set<String> orderIdSet = findBySubUserId(sessionUser.getPayId(), sessionUser.getParentPayId(), fromDate,
						toDate);
				BasicDBObject subQuery = new BasicDBObject();
				List<BasicDBObject> finalQueryList = new ArrayList<BasicDBObject>();
				BasicDBObject finalQuery = new BasicDBObject();
				List<BasicDBObject> orderIdList = new ArrayList<BasicDBObject>();

				for (String orderId : orderIdSet) {
					orderIdList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
				}
				if (orderIdList.isEmpty()) {
					cursor = null;
				} else {
					subQuery.put("$or", orderIdList);
					finalQueryList.add(subQuery);
					finalQueryList.add(saleConditionQuery);
					finalQuery.put("$and", finalQueryList);

					BasicDBObject projectElement = new BasicDBObject();
					projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
					projectElement.put(FieldType.AMOUNT.getName(), 1);
					
					BasicDBObject project = new BasicDBObject("$project", projectElement);
					
					BasicDBObject match = new BasicDBObject("$match", finalQuery);
					List<BasicDBObject> pipeline = Arrays.asList(match, project);
					AggregateIterable<Document> output = coll.aggregate(pipeline);
					output.allowDiskUse(true);
					cursor = output.iterator();
				//	cursor = coll.find(finalQuery).iterator();
				}

			} else {
				
				BasicDBObject projectElement = new BasicDBObject();
				projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
				projectElement.put(FieldType.AMOUNT.getName(), 1);
				
				BasicDBObject project = new BasicDBObject("$project", projectElement);
				BasicDBObject match = new BasicDBObject("$match", allConditionQueryObjforsale);
				List<BasicDBObject> pipeline = Arrays.asList(match, project);
				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				cursor = output.iterator();
			//	cursor = coll.find(allConditionQueryObjforsale).iterator();
			}

			int totalRefund = 0;
			if (cursor != null) {
				logger.info("Statistics Refund amount calculation started ");
				while (cursor.hasNext()) {
					totalRefund++;
					Document dbobj = cursor.next();
					
					String approvedAmount = "";
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
						
						approvedAmount = (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
						if (StringUtils.isBlank(approvedAmount)) {
							approvedAmount = (dbobj.getString(FieldType.AMOUNT.toString()));
						}
					}

					else {
						approvedAmount = (dbobj.getString(FieldType.AMOUNT.toString()));
					}
					totalRefunded = totalRefunded.add(new BigDecimal(approvedAmount)).setScale(2,
							RoundingMode.HALF_DOWN);

				}

			}

			logger.info("Statistics refundedAmount amount calculation end ");
			statistics.setTotalSuccess(String.valueOf(totalRefund));
			statistics.setApprovedAmount(String.valueOf(totalRefunded));
		} catch (Exception exception) {
			logger.error("Exception", exception);

		}
		return statistics;

	}

	public PieChart salePieChartTotalRecords(String dateFrom, String dateTo, User sessionUser, String payId,
			String subMerchantId, String paymentRegion, String currency) {

		PieChart pieChart = new PieChart();
		

		List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject currencyQuery = new BasicDBObject();
		BasicDBObject payIdquery = new BasicDBObject();
		BasicDBObject subMerchantIdQuery = new BasicDBObject();
		BasicDBObject paymentRegionQuery = new BasicDBObject();
		BasicDBObject resellerIdQuery = new BasicDBObject();
		BasicDBObject subUserIdQuery = new BasicDBObject();
		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> dateIndexCondList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

		try {

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());

			
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(dateFrom).toLocalizedPattern();
			String endString = new SimpleDateFormat(dateTo).toLocalizedPattern();

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
			
			for (String index : allDatesIndex) {
				dateIndexCondList.add(new BasicDBObject(FieldType.DATE_INDEX.getName(),index));
			}
			
			dateIndexConditionQuery.append("$or", dateIndexCondList);
			
			
			if(sessionUser.getUserType().equals(UserType.PARENTMERCHANT)){
				payIdquery.put(FieldType.PARENT_PAY_ID.getName(), sessionUser.getPayId());
				
			}else if (StringUtils.isNotBlank(payId) && !payId.equalsIgnoreCase("ALL MERCHANTS")) {
				payIdquery.put(FieldType.PAY_ID.getName(), payId);
			}
			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				subMerchantIdQuery.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}
			
			if (StringUtils.isNotBlank(paymentRegion) && !paymentRegion.equalsIgnoreCase("ALL")) {
				paymentRegionQuery.put(FieldType.PAYMENTS_REGION.getName(), paymentRegion);
			}
			
			if (!currency.equalsIgnoreCase("ALL")) {
				currencyQuery.put(FieldType.CURRENCY_CODE.getName(), currency);
			} else {
				PropertiesManager propertiesManager = new PropertiesManager();
				Map<String, String> allCurrencyMap;
				allCurrencyMap = propertiesManager.getAllProperties(alphabaticFileName);
				for (Map.Entry<String, String> entry : allCurrencyMap.entrySet()) {
					currencyConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), entry.getKey()));
				}
				currencyQuery.append("$or", currencyConditionLst);
			}
			
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerIdQuery.put(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId());
			}

			if (sessionUser.getUserType().equals(UserType.SUBUSER)
					&& sessionUser.getSubUserType().equalsIgnoreCase("eposType")) {
				subUserIdQuery.put(FieldType.SUB_USER_ID.getName(), sessionUser.getPayId());
			}

			List<BasicDBObject> saleAndCapList = new ArrayList<BasicDBObject>();
			saleAndCapList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleAndCapList.add(
					new BasicDBObject(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName())));
			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleAndCapList);

			if (!dateQuery.isEmpty()) {
				finalList.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				finalList.add(dateIndexConditionQuery);
			}
			if (!payIdquery.isEmpty()) {
				finalList.add(payIdquery);
			}
			if (!subMerchantIdQuery.isEmpty()) {
				finalList.add(subMerchantIdQuery);
			}
			if (!paymentRegionQuery.isEmpty()) {
				finalList.add(paymentRegionQuery);
			}
			if (!currencyQuery.isEmpty()) {
				finalList.add(currencyQuery);
			}
			if (!resellerIdQuery.isEmpty()) {
				finalList.add(resellerIdQuery);
			}
			if (!subUserIdQuery.isEmpty()) {
				finalList.add(subUserIdQuery);
			}

			finalList.add(saleConditionQuery);

			BasicDBObject finalobjectQuery = new BasicDBObject("$and", finalList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			MongoCursor<Document> cursor;

			// temp added till transaction collection will not have SUB_USER_ID field or any
			// epos flag
			if (sessionUser.getUserType().equals(UserType.SUBUSER)
					&& sessionUser.getSubUserType().equalsIgnoreCase("eposType")) {
				Set<String> orderIdSet = findBySubUserId(sessionUser.getPayId(), sessionUser.getParentPayId(), dateFrom,
						dateTo);
				BasicDBObject subQuery = new BasicDBObject();
				List<BasicDBObject> finalQueryList = new ArrayList<BasicDBObject>();
				BasicDBObject finalQuery = new BasicDBObject();
				List<BasicDBObject> orderIdList = new ArrayList<BasicDBObject>();

				for (String orderId : orderIdSet) {
					orderIdList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
				}
				if (orderIdList.isEmpty()) {
					cursor = null;
				} else {
					subQuery.put("$or", orderIdList);
					finalQueryList.add(subQuery);
					finalQueryList.add(saleConditionQuery);
					finalQuery.put("$and", finalQueryList);
					
					BasicDBObject projectElement = new BasicDBObject();
					projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
					projectElement.put(FieldType.AMOUNT.getName(), 1);
					
					BasicDBObject project = new BasicDBObject("$project", projectElement);
					
					BasicDBObject match = new BasicDBObject("$match", finalQuery);
					List<BasicDBObject> pipeline = Arrays.asList(match, project);
					AggregateIterable<Document> output = coll.aggregate(pipeline);
					output.allowDiskUse(true);
					cursor = output.iterator();
					
					//cursor = coll.find(finalQuery).iterator();
				}

			} else {
				
				BasicDBObject projectElement = new BasicDBObject();
				projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
				projectElement.put(FieldType.AMOUNT.getName(), 1);
				projectElement.put(FieldType.PAYMENTS_REGION.getName(), 1);
				projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
				
				BasicDBObject project = new BasicDBObject("$project", projectElement);
				
				BasicDBObject match = new BasicDBObject("$match", finalobjectQuery);
				List<BasicDBObject> pipeline = Arrays.asList(match, project);
				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				cursor = output.iterator();
				//cursor = coll.find(finalobjectQuery).iterator();
			}
			if (cursor != null) {
				while (cursor.hasNext()) {
					Document dbobj = cursor.next();
					String fetchAmount = dbobj.getString(FieldType.TOTAL_AMOUNT.toString());
					BigDecimal DBAmount;
					if (StringUtils.isBlank(fetchAmount)) {
						DBAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.toString()));
					} else {
						DBAmount = new BigDecimal(fetchAmount);
					}

					if ((StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.getName()))) && (dbobj
							.getString(FieldType.PAYMENTS_REGION.getName()).equalsIgnoreCase("INTERNATIONAL"))) {

						String totalIn = pieChart.getTotalInternationalTransaction();
						if (StringUtils.isNotBlank(totalIn)) {
							pieChart.setTotalInternationalTransaction(String.valueOf(Long.parseLong(totalIn) + 1));
						} else {
							pieChart.setTotalInternationalTransaction(String.valueOf(1));
						}

						String totalTxnAmt = pieChart.getTotalInternationalTxnAmount();
						if (StringUtils.isNotBlank(totalTxnAmt)) {
							BigDecimal txnAmount = new BigDecimal(totalTxnAmt);
							txnAmount = txnAmount.add(DBAmount).setScale(2);
							pieChart.setTotalInternationalTxnAmount(String.valueOf(txnAmount));
						} else {
							pieChart.setTotalInternationalTxnAmount(String.valueOf(DBAmount));
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.getName()))) {
							if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
									.equals(PaymentType.CREDIT_CARD.getCode()))) {

								String totalCC = pieChart.getTotalCreditCardsTransaction();
								if (StringUtils.isNotBlank(totalCC)) {
									pieChart.setTotalCreditCardsTransaction(
											String.valueOf(Long.parseLong(totalCC) + 1));
								} else {
									pieChart.setTotalCreditCardsTransaction(String.valueOf(1));
								}

								String totalTxnAmt = pieChart.getTotalCreditCardsTxnAmount();
								if (StringUtils.isNotBlank(totalTxnAmt)) {
									BigDecimal txnAmount = new BigDecimal(totalTxnAmt);
									txnAmount = txnAmount.add(DBAmount).setScale(2);
									pieChart.setTotalCreditCardsTxnAmount(String.valueOf(txnAmount));
								} else {
									pieChart.setTotalCreditCardsTxnAmount(String.valueOf(DBAmount));
								}

							} else if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
									.equals(PaymentType.DEBIT_CARD.getCode()))) {

								String totalDC = pieChart.getTotalDebitCardsTransaction();
								if (StringUtils.isNotBlank(totalDC)) {
									pieChart.setTotalDebitCardsTransaction(String.valueOf(Long.parseLong(totalDC) + 1));
								} else {
									pieChart.setTotalDebitCardsTransaction(String.valueOf(1));
								}

								String totalTxnAmt = pieChart.getTotalDebitCardsTxnAmount();
								if (StringUtils.isNotBlank(totalTxnAmt)) {
									BigDecimal txnAmount = new BigDecimal(totalTxnAmt);
									txnAmount = txnAmount.add(DBAmount).setScale(2);
									pieChart.setTotalDebitCardsTxnAmount(String.valueOf(txnAmount));
								} else {
									pieChart.setTotalDebitCardsTxnAmount(String.valueOf(DBAmount));
								}

							} else if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
									.equals(PaymentType.NET_BANKING.getCode()))) {

								String totalNB = pieChart.getTotalNetBankingTransaction();
								if (StringUtils.isNotBlank(totalNB)) {
									pieChart.setTotalNetBankingTransaction(String.valueOf(Long.parseLong(totalNB) + 1));
								} else {
									pieChart.setTotalNetBankingTransaction(String.valueOf(1));
								}

								String totalTxnAmt = pieChart.getTotalNetBankingTxnAmount();
								if (StringUtils.isNotBlank(totalTxnAmt)) {
									BigDecimal txnAmount = new BigDecimal(totalTxnAmt);
									txnAmount = txnAmount.add(DBAmount).setScale(2);
									pieChart.setTotalNetBankingTxnAmount(String.valueOf(txnAmount));
								} else {
									pieChart.setTotalNetBankingTxnAmount(String.valueOf(DBAmount));
								}

							} else if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
									.equals(PaymentType.UPI.getCode()))) {

								String totalUpi = pieChart.getTotalUpiTransaction();
								if (StringUtils.isNotBlank(totalUpi)) {
									pieChart.setTotalUpiTransaction(String.valueOf(Long.parseLong(totalUpi) + 1));
								} else {
									pieChart.setTotalUpiTransaction(String.valueOf(1));
								}

								String totalTxnAmt = pieChart.getTotalUpiTxnAmount();
								if (StringUtils.isNotBlank(totalTxnAmt)) {
									BigDecimal txnAmount = new BigDecimal(totalTxnAmt);
									txnAmount = txnAmount.add(DBAmount).setScale(2);
									pieChart.setTotalUpiTxnAmount(String.valueOf(txnAmount));
								} else {
									pieChart.setTotalUpiTxnAmount(String.valueOf(DBAmount));
								}

							} else if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
									.equals(PaymentType.WALLET.getCode()))) {

								String totalWl = pieChart.getTotalWalletTransaction();
								if (StringUtils.isNotBlank(totalWl)) {
									pieChart.setTotalWalletTransaction(String.valueOf(Long.parseLong(totalWl) + 1));
								} else {
									pieChart.setTotalWalletTransaction(String.valueOf(1));
								}

								String totalTxnAmt = pieChart.getTotalWalletTxnAmount();
								if (StringUtils.isNotBlank(totalTxnAmt)) {
									BigDecimal txnAmount = new BigDecimal(totalTxnAmt);
									txnAmount = txnAmount.add(DBAmount).setScale(2);
									pieChart.setTotalWalletTxnAmount(String.valueOf(txnAmount));
								} else {
									pieChart.setTotalWalletTxnAmount(String.valueOf(DBAmount));
								}

							} else if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
									.equals(PaymentType.INTERNATIONAL.getCode()))) {


							} else if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
									.equals(PaymentType.COD.getCode()))) {

								String totalCd = pieChart.getTotalCodTransaction();
								if (StringUtils.isNotBlank(totalCd)) {
									pieChart.setTotalCodTransaction(String.valueOf(Long.parseLong(totalCd) + 1));
								} else {
									pieChart.setTotalCodTransaction(String.valueOf(1));
								}

								String totalTxnAmt = pieChart.getTotalCodTxnAmount();
								if (StringUtils.isNotBlank(totalTxnAmt)) {
									BigDecimal txnAmount = new BigDecimal(totalTxnAmt);
									txnAmount = txnAmount.add(DBAmount).setScale(2);
									pieChart.setTotalCodTxnAmount(String.valueOf(txnAmount));
								} else {
									pieChart.setTotalCodTxnAmount(String.valueOf(DBAmount));
								}
							}
						}

					}
				}
			}
			cursor.close();

			BigDecimal ONE_HUNDRED = new BigDecimal(100);

			BigDecimal totalTxnCount = new BigDecimal(0);

			if (StringUtils.isNotBlank(pieChart.getTotalCodTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalCodTransaction()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalCreditCardsTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalCreditCardsTransaction()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalDebitCardsTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalDebitCardsTransaction()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalEmiTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalEmiTransaction()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalInternationalTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalInternationalTransaction()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalNetBankingTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalNetBankingTransaction()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalUpiTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalUpiTransaction()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalWalletTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalWalletTransaction()));
			}

			if (!totalTxnCount.equals(BigDecimal.ZERO)) {

				if (StringUtils.isNotBlank(pieChart.getTotalCodTransaction())) {
					pieChart.setTotalCodTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalCodTransaction()).multiply(ONE_HUNDRED)
									.divide(totalTxnCount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalCodTransactionPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalCreditCardsTransaction())) {
					pieChart.setTotalCreditCardsTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalCreditCardsTransaction())
									.multiply(ONE_HUNDRED).divide(totalTxnCount, 2, RoundingMode.HALF_UP)));
				} else {
					pieChart.setTotalCreditCardsTransactionPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalDebitCardsTransaction())) {
					pieChart.setTotalDebitCardsTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalDebitCardsTransaction())
									.multiply(ONE_HUNDRED).divide(totalTxnCount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalDebitCardsTransactionPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalEmiTransaction())) {
					pieChart.setTotalEmiTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalEmiTransaction()).multiply(ONE_HUNDRED)
									.divide(totalTxnCount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalEmiTransactionPercentage("0.00");
				}
				if (StringUtils.isNotBlank(pieChart.getTotalInternationalTransaction())) {
					pieChart.setTotalInternationalTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalInternationalTransaction())
									.multiply(ONE_HUNDRED).divide(totalTxnCount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalInternationalTransactionPercentage("0.00");
				}
				if (StringUtils.isNotBlank(pieChart.getTotalNetBankingTransaction())) {
					pieChart.setTotalNetBankingTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalNetBankingTransaction())
									.multiply(ONE_HUNDRED).divide(totalTxnCount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalNetBankingTransactionPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalUpiTransaction())) {
					pieChart.setTotalUpiTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalUpiTransaction()).multiply(ONE_HUNDRED)
									.divide(totalTxnCount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalUpiTransactionPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalWalletTransaction())) {
					pieChart.setTotalWalletTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalWalletTransaction()).multiply(ONE_HUNDRED)
									.divide(totalTxnCount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalWalletTransactionPercentage("0.00");
				}

			}

			BigDecimal totalTxnAmount = new BigDecimal(0);

			if (StringUtils.isNotBlank(pieChart.getTotalCodTxnAmount())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalCodTxnAmount()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalCreditCardsTxnAmount())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalCreditCardsTxnAmount()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalDebitCardsTxnAmount())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalDebitCardsTxnAmount()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalEmiTxnAmount())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalEmiTxnAmount()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalInternationalTxnAmount())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalInternationalTxnAmount()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalNetBankingTxnAmount())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalNetBankingTxnAmount()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalUpiTxnAmount())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalUpiTxnAmount()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalWalletTransaction())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalWalletTxnAmount()));
			}

			if (!totalTxnAmount.equals(BigDecimal.ZERO)) {

				if (StringUtils.isNotBlank(pieChart.getTotalCodTxnAmount())) {
					pieChart.setTotalCodTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalCodTxnAmount()).multiply(ONE_HUNDRED)
									.divide(totalTxnAmount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalCodTxnAmountPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalCreditCardsTxnAmount())) {
					pieChart.setTotalCreditCardsTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalCreditCardsTxnAmount()).multiply(ONE_HUNDRED)
									.divide(totalTxnAmount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalCreditCardsTxnAmountPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalDebitCardsTxnAmount())) {
					pieChart.setTotalDebitCardsTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalDebitCardsTxnAmount()).multiply(ONE_HUNDRED)
									.divide(totalTxnAmount, 2, RoundingMode.HALF_UP)));
				} else {
					pieChart.setTotalDebitCardsTxnAmountPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalEmiTxnAmount())) {
					pieChart.setTotalEmiTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalEmiTxnAmount()).multiply(ONE_HUNDRED)
									.divide(totalTxnAmount, 2, RoundingMode.HALF_UP)));
				} else {
					pieChart.setTotalEmiTxnAmountPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalInternationalTxnAmount())) {
					pieChart.setTotalInternationalTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalInternationalTxnAmount())
									.multiply(ONE_HUNDRED).divide(totalTxnAmount, 2, RoundingMode.HALF_UP)));
				} else {
					pieChart.setTotalInternationalTxnAmountPercentage("0.00");
				}
				if (StringUtils.isNotBlank(pieChart.getTotalNetBankingTxnAmount())) {
					pieChart.setTotalNetBankingTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalNetBankingTxnAmount()).multiply(ONE_HUNDRED)
									.divide(totalTxnAmount, 2, RoundingMode.HALF_UP)));
				} else {
					pieChart.setTotalNetBankingTxnAmountPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalUpiTxnAmount())) {
					pieChart.setTotalUpiTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalUpiTxnAmount()).multiply(ONE_HUNDRED)
									.divide(totalTxnAmount, 2, RoundingMode.HALF_UP)));
				} else {
					pieChart.setTotalUpiTxnAmountPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalWalletTxnAmount())) {
					pieChart.setTotalWalletTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalWalletTxnAmount()).multiply(ONE_HUNDRED)
									.divide(totalTxnAmount, 2, RoundingMode.HALF_UP)));
				} else {
					pieChart.setTotalWalletTxnAmountPercentage("0.00");
				}

			}

		} catch (Exception exception) {
			logger.error("Exception : ", exception);
		}

		return pieChart;
	}

	public PieChart refundPieChartTotalRecords(String dateFrom, String dateTo, User sessionUser, String payId,
			String subMerchantId, String paymentRegion, String currency) {

		PieChart pieChart = new PieChart();
		
		List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject currencyQuery = new BasicDBObject();
		BasicDBObject payIdquery = new BasicDBObject();
		BasicDBObject subMerchantIdQuery = new BasicDBObject();
		BasicDBObject paymentRegionQuery = new BasicDBObject();
		BasicDBObject resellerIdQuery = new BasicDBObject();
		BasicDBObject subUserIdQuery = new BasicDBObject();
		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> dateIndexConditionList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

		try {

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(dateFrom).toLocalizedPattern();
			String endString = new SimpleDateFormat(dateTo).toLocalizedPattern();

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

			for (String index : allDatesIndex) {
				dateIndexConditionList.add(new BasicDBObject(FieldType.DATE_INDEX.getName(),index));
			}
			
			dateIndexConditionQuery.append("$or", dateIndexConditionList);
			
			if(sessionUser.getUserType().equals(UserType.PARENTMERCHANT)){
				payIdquery.put(FieldType.PARENT_PAY_ID.getName(), sessionUser.getPayId());
				
			}else if (StringUtils.isNotBlank(payId) && !payId.equalsIgnoreCase("ALL MERCHANTS")) {
				payIdquery.put(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				subMerchantIdQuery.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}
			
			if (StringUtils.isNotBlank(paymentRegion) && !paymentRegion.equalsIgnoreCase("ALL")) {
				paymentRegionQuery.put(FieldType.PAYMENTS_REGION.getName(), paymentRegion);
			}
			
			if (!currency.equalsIgnoreCase("ALL")) {
				currencyQuery.put(FieldType.CURRENCY_CODE.getName(), currency);
			} else {
				PropertiesManager propertiesManager = new PropertiesManager();
				Map<String, String> allCurrencyMap;
				allCurrencyMap = propertiesManager.getAllProperties(alphabaticFileName);
				for (Map.Entry<String, String> entry : allCurrencyMap.entrySet()) {
					currencyConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), entry.getKey()));
				}
				currencyQuery.append("$or", currencyConditionLst);
			}
			
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerIdQuery.put(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId());
			}

			if (sessionUser.getUserType().equals(UserType.SUBUSER)
					&& sessionUser.getSubUserType().equalsIgnoreCase("eposType")) {
				subUserIdQuery.put(FieldType.SUB_USER_ID.getName(), sessionUser.getPayId());
			}

			List<BasicDBObject> saleAndCapList = new ArrayList<BasicDBObject>();
			saleAndCapList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
			saleAndCapList.add(
					new BasicDBObject(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName())));
			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleAndCapList);

			if (!dateQuery.isEmpty()) {
				finalList.add(dateQuery);
			}
			
			if (!dateIndexConditionQuery.isEmpty()) {
				finalList.add(dateIndexConditionQuery);
			}

			if (!payIdquery.isEmpty()) {
				finalList.add(payIdquery);
			}
			if (!subMerchantIdQuery.isEmpty()) {
				finalList.add(subMerchantIdQuery);
			}
			
			if (!paymentRegionQuery.isEmpty()) {
				finalList.add(paymentRegionQuery);
			}
			if (!currencyQuery.isEmpty()) {
				finalList.add(currencyQuery);
			}
			if (!resellerIdQuery.isEmpty()) {
				finalList.add(resellerIdQuery);
			}
			if (!subUserIdQuery.isEmpty()) {
				finalList.add(subUserIdQuery);
			}
			finalList.add(saleConditionQuery);

			BasicDBObject finalobjectQuery = new BasicDBObject("$and", finalList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			MongoCursor<Document> cursor;
			// temp added till transaction collection will not have SUB_USER_ID field or any
			// epos flag
			if (sessionUser.getUserType().equals(UserType.SUBUSER)
					&& sessionUser.getSubUserType().equalsIgnoreCase("eposType")) {
				Set<String> orderIdSet = findBySubUserId(sessionUser.getPayId(), sessionUser.getParentPayId(), dateFrom,
						dateTo);
				BasicDBObject subQuery = new BasicDBObject();
				List<BasicDBObject> finalQueryList = new ArrayList<BasicDBObject>();
				BasicDBObject finalQuery = new BasicDBObject();
				List<BasicDBObject> orderIdList = new ArrayList<BasicDBObject>();

				for (String orderId : orderIdSet) {
					orderIdList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
				}
				if (orderIdList.isEmpty()) {
					cursor = null;
				} else {
					subQuery.put("$in", orderIdList);
					finalQueryList.add(subQuery);
					finalQueryList.add(saleConditionQuery);
					finalQuery.put("$and", finalQueryList);

					BasicDBObject projectElement = new BasicDBObject();
					projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
					projectElement.put(FieldType.AMOUNT.getName(), 1);
					projectElement.put(FieldType.PAYMENTS_REGION.getName(), 1);
					projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
					
					BasicDBObject project = new BasicDBObject("$project", projectElement);
					
					BasicDBObject match = new BasicDBObject("$match", finalQuery);
					List<BasicDBObject> pipeline = Arrays.asList(match, project);
					AggregateIterable<Document> output = coll.aggregate(pipeline);
					output.allowDiskUse(true);
					cursor = output.iterator();
					
				//	cursor = coll.find(finalQuery).iterator();
				}

			} else {
				
				BasicDBObject projectElement = new BasicDBObject();
				projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
				projectElement.put(FieldType.AMOUNT.getName(), 1);
				projectElement.put(FieldType.PAYMENTS_REGION.getName(), 1);
				projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
				
				BasicDBObject project = new BasicDBObject("$project", projectElement);
				
				BasicDBObject match = new BasicDBObject("$match", finalobjectQuery);
				List<BasicDBObject> pipeline = Arrays.asList(match, project);
				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				cursor = output.iterator();
				
				//cursor = coll.find(finalobjectQuery).iterator();
			}

			if (cursor != null) {
				while (cursor.hasNext()) {
					Document dbobj = cursor.next();
					String fetchAmount = dbobj.getString(FieldType.TOTAL_AMOUNT.toString());
					BigDecimal DBAmount;
					if (StringUtils.isBlank(fetchAmount)) {
						DBAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.toString()));
					} else {
						DBAmount = new BigDecimal(fetchAmount);
					}

					if ((StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.getName()))) && (dbobj
							.getString(FieldType.PAYMENTS_REGION.getName()).equalsIgnoreCase("INTERNATIONAL"))) {

						String totalIn = pieChart.getTotalInternationalTransaction();
						if (StringUtils.isNotBlank(totalIn)) {
							pieChart.setTotalInternationalTransaction(String.valueOf(Long.parseLong(totalIn) + 1));
						} else {
							pieChart.setTotalInternationalTransaction(String.valueOf(1));
						}

						String totalTxnAmt = pieChart.getTotalInternationalTxnAmount();
						if (StringUtils.isNotBlank(totalTxnAmt)) {
							BigDecimal txnAmount = new BigDecimal(totalTxnAmt);
							txnAmount = txnAmount.add(DBAmount).setScale(2);
							pieChart.setTotalInternationalTxnAmount(String.valueOf(txnAmount));
						} else {
							pieChart.setTotalInternationalTxnAmount(String.valueOf(DBAmount));
						}
					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.getName()))) {
							if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
									.equals(PaymentType.CREDIT_CARD.getCode()))) {

								String totalCC = pieChart.getTotalCreditCardsTransaction();
								if (StringUtils.isNotBlank(totalCC)) {
									pieChart.setTotalCreditCardsTransaction(
											String.valueOf(Long.parseLong(totalCC) + 1));
								} else {
									pieChart.setTotalCreditCardsTransaction(String.valueOf(1));
								}

								String totalTxnAmt = pieChart.getTotalCreditCardsTxnAmount();
								if (StringUtils.isNotBlank(totalTxnAmt)) {
									BigDecimal txnAmount = new BigDecimal(totalTxnAmt);
									txnAmount = txnAmount.add(DBAmount).setScale(2);
									pieChart.setTotalCreditCardsTxnAmount(String.valueOf(txnAmount));
								} else {
									pieChart.setTotalCreditCardsTxnAmount(String.valueOf(DBAmount));
								}

							} else if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
									.equals(PaymentType.DEBIT_CARD.getCode()))) {

								String totalDC = pieChart.getTotalDebitCardsTransaction();
								if (StringUtils.isNotBlank(totalDC)) {
									pieChart.setTotalDebitCardsTransaction(String.valueOf(Long.parseLong(totalDC) + 1));
								} else {
									pieChart.setTotalDebitCardsTransaction(String.valueOf(1));
								}

								String totalTxnAmt = pieChart.getTotalDebitCardsTxnAmount();
								if (StringUtils.isNotBlank(totalTxnAmt)) {
									BigDecimal txnAmount = new BigDecimal(totalTxnAmt);
									txnAmount = txnAmount.add(DBAmount).setScale(2);
									pieChart.setTotalDebitCardsTxnAmount(String.valueOf(txnAmount));
								} else {
									pieChart.setTotalDebitCardsTxnAmount(String.valueOf(DBAmount));
								}

							} else if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
									.equals(PaymentType.NET_BANKING.getCode()))) {

								String totalNB = pieChart.getTotalNetBankingTransaction();
								if (StringUtils.isNotBlank(totalNB)) {
									pieChart.setTotalNetBankingTransaction(String.valueOf(Long.parseLong(totalNB) + 1));
								} else {
									pieChart.setTotalNetBankingTransaction(String.valueOf(1));
								}

								String totalTxnAmt = pieChart.getTotalNetBankingTxnAmount();
								if (StringUtils.isNotBlank(totalTxnAmt)) {
									BigDecimal txnAmount = new BigDecimal(totalTxnAmt);
									txnAmount = txnAmount.add(DBAmount).setScale(2);
									pieChart.setTotalNetBankingTxnAmount(String.valueOf(txnAmount));
								} else {
									pieChart.setTotalNetBankingTxnAmount(String.valueOf(DBAmount));
								}

							} else if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
									.equals(PaymentType.UPI.getCode()))) {

								String totalUpi = pieChart.getTotalUpiTransaction();
								if (StringUtils.isNotBlank(totalUpi)) {
									pieChart.setTotalUpiTransaction(String.valueOf(Long.parseLong(totalUpi) + 1));
								} else {
									pieChart.setTotalUpiTransaction(String.valueOf(1));
								}

								String totalTxnAmt = pieChart.getTotalUpiTxnAmount();
								if (StringUtils.isNotBlank(totalTxnAmt)) {
									BigDecimal txnAmount = new BigDecimal(totalTxnAmt);
									txnAmount = txnAmount.add(DBAmount).setScale(2);
									pieChart.setTotalUpiTxnAmount(String.valueOf(txnAmount));
								} else {
									pieChart.setTotalUpiTxnAmount(String.valueOf(DBAmount));
								}

							} else if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
									.equals(PaymentType.WALLET.getCode()))) {

								String totalWl = pieChart.getTotalWalletTransaction();
								if (StringUtils.isNotBlank(totalWl)) {
									pieChart.setTotalWalletTransaction(String.valueOf(Long.parseLong(totalWl) + 1));
								} else {
									pieChart.setTotalWalletTransaction(String.valueOf(1));
								}

								String totalTxnAmt = pieChart.getTotalWalletTxnAmount();
								if (StringUtils.isNotBlank(totalTxnAmt)) {
									BigDecimal txnAmount = new BigDecimal(totalTxnAmt);
									txnAmount = txnAmount.add(DBAmount).setScale(2);
									pieChart.setTotalWalletTxnAmount(String.valueOf(txnAmount));
								} else {
									pieChart.setTotalWalletTxnAmount(String.valueOf(DBAmount));
								}

							} else if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
									.equals(PaymentType.INTERNATIONAL.getCode()))) {

							} else if ((dbobj.getString(FieldType.PAYMENT_TYPE.toString())
									.equals(PaymentType.COD.getCode()))) {

								String totalCd = pieChart.getTotalCodTransaction();
								if (StringUtils.isNotBlank(totalCd)) {
									pieChart.setTotalCodTransaction(String.valueOf(Long.parseLong(totalCd) + 1));
								} else {
									pieChart.setTotalCodTransaction(String.valueOf(1));
								}

								String totalTxnAmt = pieChart.getTotalCodTxnAmount();
								if (StringUtils.isNotBlank(totalTxnAmt)) {
									BigDecimal txnAmount = new BigDecimal(totalTxnAmt);
									txnAmount = txnAmount.add(DBAmount).setScale(2);
									pieChart.setTotalCodTxnAmount(String.valueOf(txnAmount));
								} else {
									pieChart.setTotalCodTxnAmount(String.valueOf(DBAmount));
								}
							}
						}

					}
				}
			}

			cursor.close();

			BigDecimal ONE_HUNDRED = new BigDecimal(100);

			BigDecimal totalTxnCount = new BigDecimal(0);

			if (StringUtils.isNotBlank(pieChart.getTotalCodTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalCodTransaction()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalCreditCardsTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalCreditCardsTransaction()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalDebitCardsTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalDebitCardsTransaction()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalEmiTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalEmiTransaction()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalInternationalTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalInternationalTransaction()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalNetBankingTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalNetBankingTransaction()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalUpiTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalUpiTransaction()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalWalletTransaction())) {
				totalTxnCount = totalTxnCount.add(new BigDecimal(pieChart.getTotalWalletTransaction()));
			}

			if (!totalTxnCount.equals(BigDecimal.ZERO)) {

				if (StringUtils.isNotBlank(pieChart.getTotalCodTransaction())) {
					pieChart.setTotalCodTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalCodTransaction()).multiply(ONE_HUNDRED)
									.divide(totalTxnCount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalCodTransactionPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalCreditCardsTransaction())) {
					pieChart.setTotalCreditCardsTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalCreditCardsTransaction())
									.multiply(ONE_HUNDRED).divide(totalTxnCount, 2, RoundingMode.HALF_UP)));
				} else {
					pieChart.setTotalCreditCardsTransactionPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalDebitCardsTransaction())) {
					pieChart.setTotalDebitCardsTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalDebitCardsTransaction())
									.multiply(ONE_HUNDRED).divide(totalTxnCount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalDebitCardsTransactionPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalEmiTransaction())) {
					pieChart.setTotalEmiTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalEmiTransaction()).multiply(ONE_HUNDRED)
									.divide(totalTxnCount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalEmiTransactionPercentage("0.00");
				}
				if (StringUtils.isNotBlank(pieChart.getTotalInternationalTransaction())) {
					pieChart.setTotalInternationalTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalInternationalTransaction())
									.multiply(ONE_HUNDRED).divide(totalTxnCount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalInternationalTransactionPercentage("0.00");
				}
				if (StringUtils.isNotBlank(pieChart.getTotalNetBankingTransaction())) {
					pieChart.setTotalNetBankingTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalNetBankingTransaction())
									.multiply(ONE_HUNDRED).divide(totalTxnCount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalNetBankingTransactionPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalUpiTransaction())) {
					pieChart.setTotalUpiTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalUpiTransaction()).multiply(ONE_HUNDRED)
									.divide(totalTxnCount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalUpiTransactionPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalWalletTransaction())) {
					pieChart.setTotalWalletTransactionPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalWalletTransaction()).multiply(ONE_HUNDRED)
									.divide(totalTxnCount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalWalletTransactionPercentage("0.00");
				}

			}

			BigDecimal totalTxnAmount = new BigDecimal(0);

			if (StringUtils.isNotBlank(pieChart.getTotalCodTxnAmount())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalCodTxnAmount()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalCreditCardsTxnAmount())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalCreditCardsTxnAmount()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalDebitCardsTxnAmount())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalDebitCardsTxnAmount()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalEmiTxnAmount())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalEmiTxnAmount()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalInternationalTxnAmount())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalInternationalTxnAmount()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalNetBankingTxnAmount())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalNetBankingTxnAmount()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalUpiTxnAmount())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalUpiTxnAmount()));
			}
			if (StringUtils.isNotBlank(pieChart.getTotalWalletTransaction())) {
				totalTxnAmount = totalTxnAmount.add(new BigDecimal(pieChart.getTotalWalletTxnAmount()));
			}

			if (!totalTxnAmount.equals(BigDecimal.ZERO)) {

				if (StringUtils.isNotBlank(pieChart.getTotalCodTxnAmount())) {
					pieChart.setTotalCodTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalCodTxnAmount()).multiply(ONE_HUNDRED)
									.divide(totalTxnAmount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalCodTxnAmountPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalCreditCardsTxnAmount())) {
					pieChart.setTotalCreditCardsTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalCreditCardsTxnAmount()).multiply(ONE_HUNDRED)
									.divide(totalTxnAmount, 2, RoundingMode.HALF_UP).setScale(2)));
				} else {
					pieChart.setTotalCreditCardsTxnAmountPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalDebitCardsTxnAmount())) {
					pieChart.setTotalDebitCardsTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalDebitCardsTxnAmount()).multiply(ONE_HUNDRED)
									.divide(totalTxnAmount, 2, RoundingMode.HALF_UP)));
				} else {
					pieChart.setTotalDebitCardsTxnAmountPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalEmiTxnAmount())) {
					pieChart.setTotalEmiTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalEmiTxnAmount()).multiply(ONE_HUNDRED)
									.divide(totalTxnAmount, 2, RoundingMode.HALF_UP)));
				} else {
					pieChart.setTotalEmiTxnAmountPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalInternationalTxnAmount())) {
					pieChart.setTotalInternationalTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalInternationalTxnAmount())
									.multiply(ONE_HUNDRED).divide(totalTxnAmount, 2, RoundingMode.HALF_UP)));
				} else {
					pieChart.setTotalInternationalTxnAmountPercentage("0.00");
				}
				if (StringUtils.isNotBlank(pieChart.getTotalNetBankingTxnAmount())) {
					pieChart.setTotalNetBankingTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalNetBankingTxnAmount()).multiply(ONE_HUNDRED)
									.divide(totalTxnAmount, 2, RoundingMode.HALF_UP)));
				} else {
					pieChart.setTotalNetBankingTxnAmountPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalUpiTxnAmount())) {
					pieChart.setTotalUpiTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalUpiTxnAmount()).multiply(ONE_HUNDRED)
									.divide(totalTxnAmount, 2, RoundingMode.HALF_UP)));
				} else {
					pieChart.setTotalUpiTxnAmountPercentage("0.00");
				}

				if (StringUtils.isNotBlank(pieChart.getTotalWalletTxnAmount())) {
					pieChart.setTotalWalletTxnAmountPercentage(
							String.valueOf(new BigDecimal(pieChart.getTotalWalletTxnAmount()).multiply(ONE_HUNDRED)
									.divide(totalTxnAmount, 2, RoundingMode.HALF_UP)));
				} else {
					pieChart.setTotalWalletTxnAmountPercentage("0.00");
				}

			}

		} catch (Exception exception) {
			logger.error("exception : ", exception);
		}

		return pieChart;
	}

	public MerchantTransaction higestMerchantInAmountAndVolumne(String dateFrom, String dateTo, String merchantPayId, boolean saleReportFlag, User sessionUser, String currency) {

		Map<String, MerchantTransaction> dataList = new HashMap<String, MerchantTransaction>();
		Map<String, BigDecimal> highestInTransaction = new HashMap<String, BigDecimal>();
		Map<String, BigDecimal> sortedhighestInTransaction = new LinkedHashMap<String, BigDecimal>();
		
		Map<String, Long> highestInVolume = new HashMap<String, Long>();
		Map<String, Long> sortedhighestInVolumne = new LinkedHashMap<String, Long>();
		
		List<BasicDBObject> dateIndexConditionList = new ArrayList<BasicDBObject>();
		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();
		BasicDBObject saleConditionQuery = new BasicDBObject();
		BasicDBObject refundConditionQuery = new BasicDBObject();
		BasicDBObject resellerIdQuery = new BasicDBObject();
		BasicDBObject superMerchantIdQuery = new BasicDBObject();

		try {

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());

			
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(dateFrom).toLocalizedPattern();
			String endString = new SimpleDateFormat(dateTo).toLocalizedPattern();

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

			for (String index : allDatesIndex) {
				dateIndexConditionList.add(new BasicDBObject(FieldType.DATE_INDEX.getName(),index));
			}
			
			dateIndexConditionQuery.append("$or", dateIndexConditionList);
			
			if(sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerIdQuery.put(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId());
			}
			
			if(sessionUser.getUserType().equals(UserType.MERCHANT) & sessionUser.isSuperMerchant()) {
				resellerIdQuery.put(FieldType.PAY_ID.getName(), sessionUser.getPayId());
			} else {
				if(StringUtils.isNotBlank(merchantPayId)) {
					finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
				}
			}
			
			if(StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				finalList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}
			
			if(saleReportFlag) {
				List<BasicDBObject> saleAndCapList = new ArrayList<BasicDBObject>();
				saleAndCapList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
				saleAndCapList.add(
						new BasicDBObject(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName())));
				saleConditionQuery = new BasicDBObject("$and", saleAndCapList);
			}else {
			
				List<BasicDBObject> refundAndCapList = new ArrayList<BasicDBObject>();
				refundAndCapList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
				refundAndCapList.add(
						new BasicDBObject(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName())));
				refundConditionQuery = new BasicDBObject("$and", refundAndCapList);
			}
			if (!dateQuery.isEmpty()) {
				finalList.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				finalList.add(dateIndexConditionQuery);
			}
			if(!saleConditionQuery.isEmpty()) {
				finalList.add(saleConditionQuery);
			}
			
			if(!refundConditionQuery.isEmpty()) {
				finalList.add(refundConditionQuery);
			}
			if(!resellerIdQuery.isEmpty()) {
				finalList.add(resellerIdQuery);
			}
			
			if(!superMerchantIdQuery.isEmpty()) {
				finalList.add(superMerchantIdQuery);
			}

			BasicDBObject finalobjectQuery = new BasicDBObject("$and", finalList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			
			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
			projectElement.put(FieldType.AMOUNT.getName(), 1);
			projectElement.put(FieldType.SUB_MERCHANT_ID.getName(), 1);
			projectElement.put(FieldType.PAY_ID.getName(), 1);
			
			BasicDBObject project = new BasicDBObject("$project", projectElement);
			
			BasicDBObject match = new BasicDBObject("$match", finalobjectQuery);
			List<BasicDBObject> pipeline = Arrays.asList(match, project);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			//MongoCursor<Document> cursor = coll.find(finalobjectQuery).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				MerchantTransaction record = new MerchantTransaction();

				String SubMerchantId = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
				String payId = dbobj.getString(FieldType.PAY_ID.getName());
				String amount = dbobj.getString(FieldType.TOTAL_AMOUNT.getName());
				if (StringUtils.isBlank(amount)) {
					amount = dbobj.getString(FieldType.AMOUNT.getName());
				}

				if (StringUtils.isNotBlank(SubMerchantId)) {
					if (dataList.containsKey(SubMerchantId)) {
						record = dataList.get(SubMerchantId);

						BigDecimal amountInList = new BigDecimal(record.getTotalTransactionAmount());
						BigDecimal DBAmount = new BigDecimal(amount);
						DBAmount = DBAmount.add(amountInList).setScale(2);
						long totalTxn = Long.parseLong(record.getTotalTransactionVolume()) + 1;
						record.setTotalTransactionAmount(String.valueOf(DBAmount));
						record.setTotalTransactionVolume(String.valueOf(totalTxn));
						dataList.put(SubMerchantId, record);
						highestInTransaction.put(SubMerchantId, DBAmount);
						highestInVolume.put(SubMerchantId, totalTxn);
					} else {
						record.setMerchantPayId(payId);
						record.setSubMerchantPayid(SubMerchantId);
						record.setTotalTransactionAmount(amount);
						record.setTotalTransactionVolume(String.valueOf(1));
						record.setSubMerchantFlag(true);
						dataList.put(SubMerchantId, record);
						highestInTransaction.put(SubMerchantId, new BigDecimal(amount));
						highestInVolume.put(SubMerchantId, (long) 1);
					}
				} else {
					if (dataList.containsKey(payId)) {
						record = dataList.get(payId);

						BigDecimal amountInList = new BigDecimal(record.getTotalTransactionAmount());
						BigDecimal DBAmount = new BigDecimal(amount);
						DBAmount = DBAmount.add(amountInList).setScale(2);
						long totalTxn = Long.parseLong(record.getTotalTransactionVolume()) + 1;
						record.setTotalTransactionAmount(String.valueOf(DBAmount));
						record.setTotalTransactionVolume(String.valueOf(totalTxn));
						dataList.put(payId, record);
						highestInTransaction.put(payId, DBAmount);
						highestInVolume.put(payId, totalTxn);
					} else {
						record.setMerchantPayId(payId);
						record.setTotalTransactionAmount(amount);
						record.setTotalTransactionVolume(String.valueOf(1));
						record.setSubMerchantFlag(false);
						dataList.put(payId, record);
						highestInTransaction.put(payId, new BigDecimal(amount));
						highestInVolume.put(payId, (long) 1);
					}
				}

			}

			cursor.close();
			if (!highestInTransaction.isEmpty() && !highestInVolume.isEmpty()) {
//			String higerTxnValue = Collections.max(highestInTransaction.entrySet(), Map.Entry.comparingByValue()).getKey();
//			String higerTxnVolumne = Collections.max(highestInVolumne.entrySet(), Map.Entry.comparingByValue()).getKey();
				highestInVolume.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
						.forEachOrdered(x -> sortedhighestInVolumne.put(x.getKey(), x.getValue()));
				highestInVolume = sortedhighestInVolumne;

				String higerTxnVolume = null;
				String secondHigerTxnVolume = null;
				String thirdHigerTxnVolume = null;
				String fourthHigerTxnVolume = null;
				String fifthHigerTxnVolume = null;

				// value

				highestInTransaction.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
						.forEachOrdered(x -> sortedhighestInTransaction.put(x.getKey(), x.getValue()));
				highestInTransaction = sortedhighestInTransaction;

				String higerTxnValue = null;
				String secondHigerTxnValue = null;
				String thirdHigerTxnValue = null;
				String fourthHigerTxnValue = null;
				String fifthHigerTxnValue = null;

//				List<String> maxTotalVolumnMerchant = new ArrayList<>();

				List<Map.Entry<String, Long>> higherInTxnVolumneList = new LinkedList<Map.Entry<String, Long>>(
						highestInVolume.entrySet());
				List<Map.Entry<String, BigDecimal>> higherInTxnValueList = new LinkedList<Map.Entry<String, BigDecimal>>(
						highestInTransaction.entrySet());

//				maxTotalVolumnMerchant.add(higerTxnVolume);

				int countVolume = 0;
				for (Map.Entry<String, Long> entry : higherInTxnVolumneList) {

					if (countVolume == 0) {
						higerTxnVolume = entry.getKey();
					} else if (countVolume == 1) {
						secondHigerTxnVolume = entry.getKey();
					} else if (countVolume == 2) {
						thirdHigerTxnVolume = entry.getKey();
					} else if (countVolume == 3) {
						fourthHigerTxnVolume = entry.getKey();
					} else if (countVolume == 4) {
						fifthHigerTxnVolume = entry.getKey();
					}
					countVolume++;
				}

				int countTxn = 0;
				for (Map.Entry<String, BigDecimal> entry : higherInTxnValueList) {

					if (countTxn == 0) {
						higerTxnValue = entry.getKey();
					} else if (countTxn == 1) {
						secondHigerTxnValue = entry.getKey();
					} else if (countTxn == 2) {
						thirdHigerTxnValue = entry.getKey();
					} else if (countTxn == 3) {
						fourthHigerTxnValue = entry.getKey();
					} else if (countTxn == 4) {
						fifthHigerTxnValue = entry.getKey();
					}
					countTxn++;
				}

//			for (Map.Entry<String, Long> entry : highestInVolume.entrySet()) {
//				if (!entry.getKey().equals(higerTxnVolume)) {
//					if (entry.getValue().equals(highestInVolume.get(higerTxnVolume))) {
//						maxTotalVolumnMerchant.add(entry.getKey());
//					}
//				}
//			}
//			BigDecimal amount = highestInTransaction.get(higerTxnVolume);
//			if (maxTotalVolumnMerchant.size() > 1) {
//				for (String id : maxTotalVolumnMerchant) {
//					BigDecimal amountToCheck = highestInTransaction.get(id);
//					if (amountToCheck.compareTo(amount) > 0) {
//						higerTxnVolume = id;
//					}
//				}
//			}

				MerchantTransaction higerTxnValueMerchant = null;
				if(higerTxnValue != null)
					higerTxnValueMerchant = dataList.get(higerTxnValue);
				MerchantTransaction higerTxnVolumeMerchant = null;
				if(higerTxnVolume != null)
					higerTxnVolumeMerchant = dataList.get(higerTxnVolume);

				MerchantTransaction secondHigerTxnValueMerchant = null;
				if(secondHigerTxnValue != null)
					secondHigerTxnValueMerchant = dataList.get(secondHigerTxnValue);
				MerchantTransaction secondHigerTxnVolumeMerchant = null;
				if(secondHigerTxnVolume != null)
					secondHigerTxnVolumeMerchant = dataList.get(secondHigerTxnVolume);

				MerchantTransaction thirdHigerTxnValueMerchant = null;
				if(thirdHigerTxnValue !=  null)
					thirdHigerTxnValueMerchant = dataList.get(thirdHigerTxnValue);
				MerchantTransaction thirdHigerTxnVolumeMerchant = null;
				if(thirdHigerTxnVolume != null)
					thirdHigerTxnVolumeMerchant = dataList.get(thirdHigerTxnVolume);

				MerchantTransaction fourthHigerTxnValueMerchant = null;
				if(fourthHigerTxnValue != null)
					fourthHigerTxnValueMerchant = dataList.get(fourthHigerTxnValue);
				MerchantTransaction fourthHigerTxnVolumeMerchant = null;
				if(fourthHigerTxnVolume != null)
					fourthHigerTxnVolumeMerchant = dataList.get(fourthHigerTxnVolume);

				MerchantTransaction fifthHigerTxnValueMerchant = null;
				if(fifthHigerTxnValue != null)
					fifthHigerTxnValueMerchant = dataList.get(fifthHigerTxnValue);
				MerchantTransaction fifthHigerTxnVolumeMerchant = null;
				if(fifthHigerTxnVolume != null)
					fifthHigerTxnVolumeMerchant = dataList.get(fifthHigerTxnVolume);

				MerchantTransaction finalData = new MerchantTransaction();

				if (higerTxnValueMerchant != null && higerTxnValueMerchant.isSubMerchantFlag()) {
					// GetSuper & sub merchant Business Name
					finalData.setTxnFirstAmountSuperMerchantBusinessName(
							userDao.getBusinessNameByPayId(higerTxnValueMerchant.getSubMerchantPayid()));

					finalData.setTxnFirstAmountMerchantBusinessName(
							userDao.getBusinessNameByPayId(higerTxnValueMerchant.getMerchantPayId()));

				} else {
					if(higerTxnValueMerchant != null) {
					finalData.setTxnFirstAmountMerchantBusinessName(
							userDao.getBusinessNameByPayId(higerTxnValueMerchant.getMerchantPayId()));
					finalData.setTxnFirstAmountSuperMerchantBusinessName("NA");
					}
				}

				if ( higerTxnVolumeMerchant != null && higerTxnVolumeMerchant.isSubMerchantFlag()) {
					finalData.setTxnFirstVolumeSuperMerchantBusinessName(
							userDao.getBusinessNameByPayId(higerTxnVolumeMerchant.getSubMerchantPayid()));
					finalData.setTxnFirstVolumeMerchantBusinessName(
							userDao.getBusinessNameByPayId(higerTxnVolumeMerchant.getMerchantPayId()));
				} else {
					if(higerTxnVolumeMerchant != null) {
					finalData.setTxnFirstVolumeMerchantBusinessName(
							userDao.getBusinessNameByPayId(higerTxnVolumeMerchant.getMerchantPayId()));
					finalData.setTxnFirstVolumeSuperMerchantBusinessName("NA");
					}
				}

				if (secondHigerTxnValueMerchant != null && secondHigerTxnValueMerchant.isSubMerchantFlag()) {
					// GetSuper & sub merchant Business Name
					finalData.setTxnSecondAmountSuperMerchantBusinessName(
							userDao.getBusinessNameByPayId(secondHigerTxnValueMerchant.getSubMerchantPayid()));

					finalData.setTxnSecondAmountMerchantBusinessName(
							userDao.getBusinessNameByPayId(secondHigerTxnValueMerchant.getMerchantPayId()));

				} else {
					if(secondHigerTxnValueMerchant != null) {
						finalData.setTxnSecondAmountMerchantBusinessName(
							userDao.getBusinessNameByPayId(secondHigerTxnValueMerchant.getMerchantPayId()));
						finalData.setTxnSecondAmountSuperMerchantBusinessName("NA");
					}
				}

				if (secondHigerTxnVolumeMerchant != null && secondHigerTxnVolumeMerchant.isSubMerchantFlag()) {
					finalData.setTxnSecondVolumeSuperMerchantBusinessName(
							userDao.getBusinessNameByPayId(secondHigerTxnVolumeMerchant.getSubMerchantPayid()));
					finalData.setTxnSecondVolumeMerchantBusinessName(
							userDao.getBusinessNameByPayId(secondHigerTxnVolumeMerchant.getMerchantPayId()));
				} else {
					if(secondHigerTxnVolumeMerchant != null) {
					finalData.setTxnSecondVolumeMerchantBusinessName(
							userDao.getBusinessNameByPayId(secondHigerTxnVolumeMerchant.getMerchantPayId()));
					finalData.setTxnSecondVolumeSuperMerchantBusinessName("NA");
					}
				}

				if (thirdHigerTxnValueMerchant != null && thirdHigerTxnValueMerchant.isSubMerchantFlag()) {
					// GetSuper & sub merchant Business Name
					finalData.setTxnThirdAmountSuperMerchantBusinessName(
							userDao.getBusinessNameByPayId(thirdHigerTxnValueMerchant.getSubMerchantPayid()));

					finalData.setTxnThirdAmountMerchantBusinessName(
							userDao.getBusinessNameByPayId(thirdHigerTxnValueMerchant.getMerchantPayId()));

				} else {
					if(thirdHigerTxnValueMerchant != null) {
					finalData.setTxnThirdAmountMerchantBusinessName(
							userDao.getBusinessNameByPayId(thirdHigerTxnValueMerchant.getMerchantPayId()));
					finalData.setTxnThirdAmountSuperMerchantBusinessName("NA");
					}
				}

				if (thirdHigerTxnVolumeMerchant != null && thirdHigerTxnVolumeMerchant.isSubMerchantFlag()) {
					finalData.setTxnThirdVolumeSuperMerchantBusinessName(
							userDao.getBusinessNameByPayId(thirdHigerTxnVolumeMerchant.getSubMerchantPayid()));
					finalData.setTxnThirdVolumeMerchantBusinessName(
							userDao.getBusinessNameByPayId(thirdHigerTxnVolumeMerchant.getMerchantPayId()));
				} else {
					if(thirdHigerTxnVolumeMerchant != null) {
					finalData.setTxnThirdVolumeMerchantBusinessName(
							userDao.getBusinessNameByPayId(thirdHigerTxnVolumeMerchant.getMerchantPayId()));
					finalData.setTxnThirdVolumeSuperMerchantBusinessName("NA");
					}
				}

				if (fourthHigerTxnValueMerchant != null && fourthHigerTxnValueMerchant.isSubMerchantFlag()) {
					// GetSuper & sub merchant Business Name
					finalData.setTxnFourthAmountSuperMerchantBusinessName(
							userDao.getBusinessNameByPayId(fourthHigerTxnValueMerchant.getSubMerchantPayid()));

					finalData.setTxnFourthAmountMerchantBusinessName(
							userDao.getBusinessNameByPayId(fourthHigerTxnValueMerchant.getMerchantPayId()));

				} else {
					if(fourthHigerTxnValueMerchant != null) {
					finalData.setTxnFourthAmountMerchantBusinessName(
							userDao.getBusinessNameByPayId(fourthHigerTxnValueMerchant.getMerchantPayId()));
					finalData.setTxnFourthAmountSuperMerchantBusinessName("NA");
					}
				}

				if (fourthHigerTxnVolumeMerchant != null && fourthHigerTxnVolumeMerchant.isSubMerchantFlag()) {
					finalData.setTxnFourthVolumeSuperMerchantBusinessName(
							userDao.getBusinessNameByPayId(fourthHigerTxnVolumeMerchant.getSubMerchantPayid()));
					finalData.setTxnFourthVolumeMerchantBusinessName(
							userDao.getBusinessNameByPayId(fourthHigerTxnVolumeMerchant.getMerchantPayId()));
				} else {
					if(fourthHigerTxnVolumeMerchant != null) {
					finalData.setTxnFourthVolumeMerchantBusinessName(
							userDao.getBusinessNameByPayId(fourthHigerTxnVolumeMerchant.getMerchantPayId()));
					finalData.setTxnFourthVolumeSuperMerchantBusinessName("NA");
					}
				}

				if (fifthHigerTxnValueMerchant != null && fifthHigerTxnValueMerchant.isSubMerchantFlag()) {
					// GetSuper & sub merchant Business Name
					finalData.setTxnFifthAmountSuperMerchantBusinessName(
							userDao.getBusinessNameByPayId(fifthHigerTxnValueMerchant.getSubMerchantPayid()));

					finalData.setTxnFifthAmountMerchantBusinessName(
							userDao.getBusinessNameByPayId(fifthHigerTxnValueMerchant.getMerchantPayId()));

				} else {
					if(fifthHigerTxnValueMerchant != null) {
					finalData.setTxnFifthAmountMerchantBusinessName(
							userDao.getBusinessNameByPayId(fifthHigerTxnValueMerchant.getMerchantPayId()));
					finalData.setTxnFifthAmountSuperMerchantBusinessName("NA");
					}
				}

				if (fifthHigerTxnVolumeMerchant != null && fifthHigerTxnVolumeMerchant.isSubMerchantFlag()) {
					finalData.setTxnFifthVolumeSuperMerchantBusinessName(
							userDao.getBusinessNameByPayId(fifthHigerTxnVolumeMerchant.getSubMerchantPayid()));
					finalData.setTxnFifthVolumeMerchantBusinessName(
							userDao.getBusinessNameByPayId(fifthHigerTxnVolumeMerchant.getMerchantPayId()));
				} else {
					if(fifthHigerTxnVolumeMerchant != null) {
					finalData.setTxnFifthVolumeMerchantBusinessName(
							userDao.getBusinessNameByPayId(fifthHigerTxnVolumeMerchant.getMerchantPayId()));
					finalData.setTxnFifthVolumeSuperMerchantBusinessName("NA");
					}
				}
				if(higerTxnValueMerchant != null) {
					finalData.setTotalFirstTransactionAmount(higerTxnValueMerchant.getTotalTransactionAmount());
					finalData.setTotalTransactionAmount(higerTxnValueMerchant.getTotalTransactionAmount());
				}
				if(higerTxnVolumeMerchant != null) {
					finalData.setTotalFirstTransactionVolume(higerTxnVolumeMerchant.getTotalTransactionVolume());
					finalData.setTotalTransactionVolume(higerTxnVolumeMerchant.getTotalTransactionVolume());
				}

				if(secondHigerTxnValueMerchant != null)
					finalData.setTotalSecondTransactionAmount(secondHigerTxnValueMerchant.getTotalTransactionAmount());
				if(secondHigerTxnVolumeMerchant != null)
					finalData.setTotalSecondTransactionVolume(secondHigerTxnVolumeMerchant.getTotalTransactionVolume());

				if(thirdHigerTxnValueMerchant != null)
					finalData.setTotalThirdTransactionAmount(thirdHigerTxnValueMerchant.getTotalTransactionAmount());
				if(thirdHigerTxnVolumeMerchant != null)
					finalData.setTotalThirdTransactionVolume(thirdHigerTxnVolumeMerchant.getTotalTransactionVolume());

				if(fourthHigerTxnValueMerchant != null)
					finalData.setTotalFourthTransactionAmount(fourthHigerTxnValueMerchant.getTotalTransactionAmount());
				if(fourthHigerTxnVolumeMerchant != null)
					finalData.setTotalFourthTransactionVolume(fourthHigerTxnVolumeMerchant.getTotalTransactionVolume());

				if(fifthHigerTxnValueMerchant != null)
					finalData.setTotalFifthTransactionAmount(fifthHigerTxnValueMerchant.getTotalTransactionAmount());
				if(fifthHigerTxnVolumeMerchant != null)
					finalData.setTotalFifthTransactionVolume(fifthHigerTxnVolumeMerchant.getTotalTransactionVolume());

				return finalData;
			}
		} catch (Exception exception) {
			logger.error("Exception in geting Higer Merchant", exception);
		}

		return null;
	}

	public MerchantTransaction lowestMerchantInAmountAndVolumne(String dateFrom, String dateTo, String merchantPayId, boolean saleReportFlag, User sessionUser, String currency) {

		Map<String, MerchantTransaction> dataList = new HashMap<String, MerchantTransaction>();
		Map<String, BigDecimal> lowestInTransaction = new HashMap<String, BigDecimal>();
		Map<String, Long> lowestInVolumne = new HashMap<String, Long>();
		List<BasicDBObject> dateIndexConditionList = new ArrayList<BasicDBObject>();
		
		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();
		BasicDBObject saleConditionQuery = new BasicDBObject();
		BasicDBObject refundConditionQuery = new BasicDBObject();
		BasicDBObject resellerIdQuery = new BasicDBObject();
		BasicDBObject superMerchantIdQuery = new BasicDBObject();

		try {

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(dateFrom).toLocalizedPattern();
			String endString = new SimpleDateFormat(dateTo).toLocalizedPattern();

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

			for (String index : allDatesIndex) {
				dateIndexConditionList.add(new BasicDBObject(FieldType.DATE_INDEX.getName(),index));
			}
			
			dateIndexConditionQuery.append("$or", dateIndexConditionList);
			
			if(sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerIdQuery.put(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId());
			}
			
			if(sessionUser.getUserType().equals(UserType.MERCHANT) & sessionUser.isSuperMerchant()) {
				resellerIdQuery.put(FieldType.PAY_ID.getName(), sessionUser.getPayId());
			} else {
				if(StringUtils.isNotBlank(merchantPayId)) {
					finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
				}
			}
			
			if(saleReportFlag) {
				List<BasicDBObject> saleAndCapList = new ArrayList<BasicDBObject>();
				saleAndCapList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
				saleAndCapList.add(
						new BasicDBObject(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName())));
				saleConditionQuery = new BasicDBObject("$and", saleAndCapList);
			}else {
			
				List<BasicDBObject> refundAndCapList = new ArrayList<BasicDBObject>();
				refundAndCapList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
				refundAndCapList.add(
						new BasicDBObject(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName())));
				refundConditionQuery = new BasicDBObject("$and", refundAndCapList);
			}
			
			if(StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				finalList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}
			
			if (!dateQuery.isEmpty()) {
				finalList.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				finalList.add(dateIndexConditionQuery);
			}
			
			if(!saleConditionQuery.isEmpty()) {
				finalList.add(saleConditionQuery);
			}
			
			if(!refundConditionQuery.isEmpty()) {
				finalList.add(refundConditionQuery);
			}
			
			if(!resellerIdQuery.isEmpty()) {
				finalList.add(resellerIdQuery);
			}
			
			if(!superMerchantIdQuery.isEmpty()) {
				finalList.add(superMerchantIdQuery);
			}

			BasicDBObject finalobjectQuery = new BasicDBObject("$and", finalList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			
			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
			projectElement.put(FieldType.AMOUNT.getName(), 1);
			projectElement.put(FieldType.SUB_MERCHANT_ID.getName(), 1);
			projectElement.put(FieldType.PAY_ID.getName(), 1);
			
			BasicDBObject project = new BasicDBObject("$project", projectElement);
			
			BasicDBObject match = new BasicDBObject("$match", finalobjectQuery);
			List<BasicDBObject> pipeline = Arrays.asList(match, project);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			
			
			//MongoCursor<Document> cursor = coll.find(finalobjectQuery).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				MerchantTransaction record = new MerchantTransaction();

				String SubMerchantId = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
				String payId = dbobj.getString(FieldType.PAY_ID.getName());
				String amount = dbobj.getString(FieldType.TOTAL_AMOUNT.getName());
				if (StringUtils.isBlank(amount)) {
					amount = dbobj.getString(FieldType.AMOUNT.getName());
				}

				if (StringUtils.isNotBlank(SubMerchantId)) {
					if (dataList.containsKey(SubMerchantId)) {
						record = dataList.get(SubMerchantId);

						BigDecimal amountInList = new BigDecimal(record.getTotalTransactionAmount());
						BigDecimal DBAmount = new BigDecimal(amount);
						DBAmount = DBAmount.add(amountInList).setScale(2);
						long totalTxn = Long.parseLong(record.getTotalTransactionVolume()) + 1;
						record.setTotalTransactionAmount(String.valueOf(DBAmount));
						record.setTotalTransactionVolume(String.valueOf(totalTxn));
						dataList.put(SubMerchantId, record);
						lowestInTransaction.put(SubMerchantId, DBAmount);
						lowestInVolumne.put(SubMerchantId, totalTxn);
					} else {
						record.setMerchantPayId(payId);
						record.setSubMerchantPayid(SubMerchantId);
						record.setTotalTransactionAmount(amount);
						record.setTotalTransactionVolume(String.valueOf(1));
						record.setSubMerchantFlag(true);
						dataList.put(SubMerchantId, record);
						lowestInTransaction.put(SubMerchantId, new BigDecimal(amount));
						lowestInVolumne.put(SubMerchantId, (long) 1);
					}
				} else {
					if (dataList.containsKey(payId)) {
						record = dataList.get(payId);

						BigDecimal amountInList = new BigDecimal(record.getTotalTransactionAmount());
						BigDecimal DBAmount = new BigDecimal(amount);
						DBAmount = DBAmount.add(amountInList).setScale(2);
						long totalTxn = Long.parseLong(record.getTotalTransactionVolume()) + 1;
						record.setTotalTransactionAmount(String.valueOf(DBAmount));
						record.setTotalTransactionVolume(String.valueOf(totalTxn));

						dataList.put(payId, record);
						lowestInTransaction.put(payId, DBAmount);
						lowestInVolumne.put(payId, totalTxn);
					} else {
						record.setMerchantPayId(payId);
						record.setTotalTransactionAmount(amount);
						record.setTotalTransactionVolume(String.valueOf(1));
						record.setSubMerchantFlag(false);
						dataList.put(payId, record);
						lowestInTransaction.put(payId, new BigDecimal(amount));
						lowestInVolumne.put(payId, (long) 1);
					}
				}

			}

			cursor.close();
			if(!lowestInTransaction.isEmpty() && !lowestInVolumne.isEmpty()) {
//				String minTxnValueId = Collections.min(lowestInTransaction.entrySet(), Map.Entry.comparingByValue()).getKey();
//				BigDecimal minTxnValue = lowestInTransaction.get(minTxnValueId);
				
				String minTxnValueId = null;
				String secondMinTxnValueId = null;
				String thirdMinTxnValueId = null;
				String fourthMinTxnValueId = null;
				String fifthMinTxnValueId = null;
				
//				String minTxnVolumneId = Collections.min(lowestInVolumne.entrySet(), Map.Entry.comparingByValue()).getKey();
//				long minTxnVolume = lowestInVolumne.get(minTxnVolumneId);
				
				String minTxnVolumeId = null;
				String secondMinTxnVolumeId = null;
				String thirdMinTxnVolumeId = null;
				String fourthMinTxnVolumeId = null;
				String fifthMinTxnVolumeId = null;
				
//				List<String> minTotalVolumnMerchant = new ArrayList<>();
			
			
				List<Map.Entry<String, Long> > lowestInVolumneList = new LinkedList<Map.Entry<String, Long> >(lowestInVolumne.entrySet());
				List<Map.Entry<String, BigDecimal> > lowestInTransactionList = new LinkedList<Map.Entry<String, BigDecimal> >(lowestInTransaction.entrySet());
		 
		        // Sort the list using lambda expression
		        Collections.sort(lowestInVolumneList,(i1,i2) -> i1.getValue().compareTo(i2.getValue()));
		        Collections.sort(lowestInTransactionList,(i1,i2) -> i1.getValue().compareTo(i2.getValue()));
 
		        int countVolume = 0;
				for (Map.Entry<String, Long> entry : lowestInVolumneList) {

					if (countVolume == 0) {
						minTxnVolumeId = entry.getKey();
					} else if (countVolume == 1) {
						secondMinTxnVolumeId = entry.getKey();
					} else if (countVolume == 2) {
						thirdMinTxnVolumeId = entry.getKey();
					} else if (countVolume == 3) {
						fourthMinTxnVolumeId = entry.getKey();
					} else if (countVolume == 4) {
						fifthMinTxnVolumeId = entry.getKey();
					}
					countVolume++;
				}
				
				int countTxn = 0;
				for (Map.Entry<String, BigDecimal> entry : lowestInTransactionList) {

					if (countTxn == 0) {
						minTxnValueId = entry.getKey();
					} else if (countTxn == 1) {
						secondMinTxnValueId = entry.getKey();
					} else if (countTxn == 2) {
						thirdMinTxnValueId = entry.getKey();
					} else if (countTxn == 3) {
						fourthMinTxnValueId = entry.getKey();
					} else if (countTxn == 4) {
						fifthMinTxnValueId = entry.getKey();
					}
					countTxn++;
				}
			
//			for (Map.Entry<String, Long> entry : lowestInVolumne.entrySet()) {
//				if (!entry.getKey().equals(minTxnVolumeId)) {
//					if (entry.getValue().equals(lowestInVolumne.get(minTxnVolumeId))) {
//						minTotalVolumnMerchant.add(entry.getKey());
//					}
//				}
//			}
//			BigDecimal amount = lowestInTransaction.get(minTxnVolumeId);
//			if (minTotalVolumnMerchant.size() > 0) {
//				for (String id : minTotalVolumnMerchant) {
//					BigDecimal amountToCheck = lowestInTransaction.get(id);
//					if (amountToCheck.compareTo(amount) < 0) {
//						minTxnVolumeId = id;
//					}
//				}
//			}

			MerchantTransaction lowerTxnValueMerchant = null;
			if(minTxnValueId != null)
				lowerTxnValueMerchant = dataList.get(minTxnValueId);
			MerchantTransaction lowerTxnVolumeMerchant = null;
			if(minTxnVolumeId != null)
				lowerTxnVolumeMerchant = dataList.get(minTxnVolumeId);
			
			MerchantTransaction secondLowerTxnValueMerchant = null;
			if(secondMinTxnValueId != null)
				secondLowerTxnValueMerchant = dataList.get(secondMinTxnValueId);
			MerchantTransaction secondLowerTxnVolumeMerchant = null;
			if(secondMinTxnVolumeId != null)
				secondLowerTxnVolumeMerchant = dataList.get(secondMinTxnVolumeId);
			
			MerchantTransaction thirdLowerTxnValueMerchant = null;
			if(thirdMinTxnValueId != null)
				thirdLowerTxnValueMerchant = dataList.get(thirdMinTxnValueId);
			MerchantTransaction thirdLowerTxnVolumeMerchant = null;
			if(thirdMinTxnVolumeId != null)
				thirdLowerTxnVolumeMerchant = dataList.get(thirdMinTxnVolumeId);
			
			MerchantTransaction fourthLowerTxnValueMerchant = null;
			if(fourthMinTxnValueId != null)
				fourthLowerTxnValueMerchant = dataList.get(fourthMinTxnValueId);
			MerchantTransaction fourthLowerTxnVolumeMerchant = null;
			if(fourthMinTxnVolumeId != null)
				fourthLowerTxnVolumeMerchant = dataList.get(fourthMinTxnVolumeId);
			
			MerchantTransaction fifthLowerTxnValueMerchant = null;
			if(fifthMinTxnValueId != null)
				fifthLowerTxnValueMerchant = dataList.get(fifthMinTxnValueId);
			MerchantTransaction fifthLowerTxnVolumeMerchant = null;
			if(fifthMinTxnVolumeId != null)
				fifthLowerTxnVolumeMerchant = dataList.get(fifthMinTxnVolumeId);

			MerchantTransaction finalData = new MerchantTransaction();

			if (lowerTxnValueMerchant != null && lowerTxnValueMerchant.isSubMerchantFlag()) {
				// GetSuper & sub merchant Business Name
				finalData.setTxnFirstAmountSuperMerchantBusinessName(
						userDao.getBusinessNameByPayId(lowerTxnValueMerchant.getSubMerchantPayid()));

				finalData.setTxnFirstAmountMerchantBusinessName(
						userDao.getBusinessNameByPayId(lowerTxnValueMerchant.getMerchantPayId()));

//				finalData.setTxnAmountSuperMerchantBusinessName(
//						userDao.getBusinessNameByPayId(lowerTxnValueMerchant.getMerchantPayId()));
//				finalData.setTxnAmountMerchantBusinessName(
//						userDao.getBusinessNameByPayId(lowerTxnValueMerchant.getSubMerchantPayid()));
			} else {
				if(lowerTxnValueMerchant != null) {
				finalData.setTxnFirstAmountMerchantBusinessName(
						userDao.getBusinessNameByPayId(lowerTxnValueMerchant.getMerchantPayId()));
				finalData.setTxnFirstAmountSuperMerchantBusinessName("NA");
				}
			}

			if (lowerTxnVolumeMerchant != null && lowerTxnVolumeMerchant.isSubMerchantFlag()) {

				finalData.setTxnFirstVolumeSuperMerchantBusinessName(
						userDao.getBusinessNameByPayId(lowerTxnVolumeMerchant.getSubMerchantPayid()));
				finalData.setTxnFirstVolumeMerchantBusinessName(
						userDao.getBusinessNameByPayId(lowerTxnVolumeMerchant.getMerchantPayId()));
//				finalData.setTxnVolumeSuperMerchantBusinessName(
//						userDao.getBusinessNameByPayId(lowerTxnVolumeMerchant.getMerchantPayId()));
//				finalData.setTxnVolumeMerchantBusinessName(
//						userDao.getBusinessNameByPayId(lowerTxnVolumeMerchant.getSubMerchantPayid()));
			} else {
				if(lowerTxnVolumeMerchant != null) {
				finalData.setTxnFirstVolumeMerchantBusinessName(
						userDao.getBusinessNameByPayId(lowerTxnVolumeMerchant.getMerchantPayId()));
				finalData.setTxnFirstVolumeSuperMerchantBusinessName("NA");
				}
			}
			
			if (secondLowerTxnValueMerchant != null && secondLowerTxnValueMerchant.isSubMerchantFlag()) {
				// GetSuper & sub merchant Business Name
				finalData.setTxnSecondAmountSuperMerchantBusinessName(
						userDao.getBusinessNameByPayId(secondLowerTxnValueMerchant.getSubMerchantPayid()));

				finalData.setTxnSecondAmountMerchantBusinessName(
						userDao.getBusinessNameByPayId(secondLowerTxnValueMerchant.getMerchantPayId()));

			} else {
				if(secondLowerTxnValueMerchant != null) {
				finalData.setTxnSecondAmountMerchantBusinessName(
						userDao.getBusinessNameByPayId(secondLowerTxnValueMerchant.getMerchantPayId()));
				finalData.setTxnSecondAmountSuperMerchantBusinessName("NA");
				}
			}

			if (secondLowerTxnVolumeMerchant != null && secondLowerTxnVolumeMerchant.isSubMerchantFlag()) {

				finalData.setTxnSecondVolumeSuperMerchantBusinessName(
						userDao.getBusinessNameByPayId(secondLowerTxnVolumeMerchant.getSubMerchantPayid()));
				finalData.setTxnSecondVolumeMerchantBusinessName(
						userDao.getBusinessNameByPayId(secondLowerTxnVolumeMerchant.getMerchantPayId()));
			} else {
				if(secondLowerTxnVolumeMerchant != null) {
				finalData.setTxnSecondVolumeMerchantBusinessName(
						userDao.getBusinessNameByPayId(secondLowerTxnVolumeMerchant.getMerchantPayId()));
				finalData.setTxnSecondVolumeSuperMerchantBusinessName("NA");
				}
			}
			
			if (thirdLowerTxnValueMerchant != null && thirdLowerTxnValueMerchant.isSubMerchantFlag()) {
				// GetSuper & sub merchant Business Name
				finalData.setTxnThirdAmountSuperMerchantBusinessName(
						userDao.getBusinessNameByPayId(thirdLowerTxnValueMerchant.getSubMerchantPayid()));

				finalData.setTxnThirdAmountMerchantBusinessName(
						userDao.getBusinessNameByPayId(thirdLowerTxnValueMerchant.getMerchantPayId()));

			} else {
				if(thirdLowerTxnValueMerchant != null) {
				finalData.setTxnThirdAmountMerchantBusinessName(
						userDao.getBusinessNameByPayId(thirdLowerTxnValueMerchant.getMerchantPayId()));
				finalData.setTxnThirdAmountSuperMerchantBusinessName("NA");
				}
			}

			if (thirdLowerTxnVolumeMerchant != null && thirdLowerTxnVolumeMerchant.isSubMerchantFlag()) {

				finalData.setTxnThirdVolumeSuperMerchantBusinessName(
						userDao.getBusinessNameByPayId(thirdLowerTxnVolumeMerchant.getSubMerchantPayid()));
				finalData.setTxnThirdVolumeMerchantBusinessName(
						userDao.getBusinessNameByPayId(thirdLowerTxnVolumeMerchant.getMerchantPayId()));
			} else {
				if(thirdLowerTxnVolumeMerchant != null) {
				finalData.setTxnThirdVolumeMerchantBusinessName(
						userDao.getBusinessNameByPayId(thirdLowerTxnVolumeMerchant.getMerchantPayId()));
				finalData.setTxnThirdVolumeSuperMerchantBusinessName("NA");
				}
			}
			
			if (fourthLowerTxnValueMerchant != null && fourthLowerTxnValueMerchant.isSubMerchantFlag()) {
				// GetSuper & sub merchant Business Name
				finalData.setTxnFourthAmountSuperMerchantBusinessName(
						userDao.getBusinessNameByPayId(fourthLowerTxnValueMerchant.getSubMerchantPayid()));

				finalData.setTxnFourthAmountMerchantBusinessName(
						userDao.getBusinessNameByPayId(fourthLowerTxnValueMerchant.getMerchantPayId()));

			} else {
				if(fourthLowerTxnValueMerchant != null) {
				finalData.setTxnFourthAmountMerchantBusinessName(
						userDao.getBusinessNameByPayId(fourthLowerTxnValueMerchant.getMerchantPayId()));
				finalData.setTxnFourthAmountSuperMerchantBusinessName("NA");
				}
			}

			if (fourthLowerTxnVolumeMerchant != null && fourthLowerTxnVolumeMerchant.isSubMerchantFlag()) {

				finalData.setTxnFourthVolumeSuperMerchantBusinessName(
						userDao.getBusinessNameByPayId(fourthLowerTxnVolumeMerchant.getSubMerchantPayid()));
				finalData.setTxnFourthVolumeMerchantBusinessName(
						userDao.getBusinessNameByPayId(fourthLowerTxnVolumeMerchant.getMerchantPayId()));
			} else {
				if(fourthLowerTxnVolumeMerchant != null) {
				finalData.setTxnFourthVolumeMerchantBusinessName(
						userDao.getBusinessNameByPayId(fourthLowerTxnVolumeMerchant.getMerchantPayId()));
				finalData.setTxnFourthVolumeSuperMerchantBusinessName("NA");
				}
			}
			
			if (fifthLowerTxnValueMerchant != null && fifthLowerTxnValueMerchant.isSubMerchantFlag()) {
				// GetSuper & sub merchant Business Name
				finalData.setTxnFifthAmountSuperMerchantBusinessName(
						userDao.getBusinessNameByPayId(fifthLowerTxnValueMerchant.getSubMerchantPayid()));

				finalData.setTxnFifthAmountMerchantBusinessName(
						userDao.getBusinessNameByPayId(fifthLowerTxnValueMerchant.getMerchantPayId()));

			} else {
				if(fifthLowerTxnValueMerchant != null) {
				finalData.setTxnFifthAmountMerchantBusinessName(
						userDao.getBusinessNameByPayId(fifthLowerTxnValueMerchant.getMerchantPayId()));
				finalData.setTxnFifthAmountSuperMerchantBusinessName("NA");
				}
			}

			if (fifthLowerTxnVolumeMerchant != null && fifthLowerTxnVolumeMerchant.isSubMerchantFlag()) {

				finalData.setTxnFifthVolumeSuperMerchantBusinessName(
						userDao.getBusinessNameByPayId(fifthLowerTxnVolumeMerchant.getSubMerchantPayid()));
				finalData.setTxnFifthVolumeMerchantBusinessName(
						userDao.getBusinessNameByPayId(fifthLowerTxnVolumeMerchant.getMerchantPayId()));
			} else {
				if(fifthLowerTxnVolumeMerchant != null) {
				finalData.setTxnFifthVolumeMerchantBusinessName(
						userDao.getBusinessNameByPayId(fifthLowerTxnVolumeMerchant.getMerchantPayId()));
				finalData.setTxnFifthVolumeSuperMerchantBusinessName("NA");
				}
			}

			if(lowerTxnValueMerchant != null) {
				finalData.setTotalFirstTransactionAmount(lowerTxnValueMerchant.getTotalTransactionAmount());
				finalData.setTotalTransactionAmount(lowerTxnValueMerchant.getTotalTransactionAmount());
			}
			if(lowerTxnVolumeMerchant != null) {
				finalData.setTotalFirstTransactionVolume(lowerTxnVolumeMerchant.getTotalTransactionVolume());
				finalData.setTotalTransactionVolume(lowerTxnVolumeMerchant.getTotalTransactionVolume());
			}
			
			if(secondLowerTxnValueMerchant != null)
				finalData.setTotalSecondTransactionAmount(secondLowerTxnValueMerchant.getTotalTransactionAmount());
			if(secondLowerTxnVolumeMerchant != null)
				finalData.setTotalSecondTransactionVolume(secondLowerTxnVolumeMerchant.getTotalTransactionVolume());
			
			if(thirdLowerTxnValueMerchant != null)
				finalData.setTotalThirdTransactionAmount(thirdLowerTxnValueMerchant.getTotalTransactionAmount());
			if(thirdLowerTxnVolumeMerchant != null)
				finalData.setTotalThirdTransactionVolume(thirdLowerTxnVolumeMerchant.getTotalTransactionVolume());
			
			if(fourthLowerTxnValueMerchant != null)
				finalData.setTotalFourthTransactionAmount(fourthLowerTxnValueMerchant.getTotalTransactionAmount());
			if(fourthLowerTxnVolumeMerchant != null)
				finalData.setTotalFourthTransactionVolume(fourthLowerTxnVolumeMerchant.getTotalTransactionVolume());
			
			if(fifthLowerTxnValueMerchant != null)
				finalData.setTotalFifthTransactionAmount(fifthLowerTxnValueMerchant.getTotalTransactionAmount());
			if(fifthLowerTxnVolumeMerchant != null)
				finalData.setTotalFifthTransactionVolume(fifthLowerTxnVolumeMerchant.getTotalTransactionVolume());

			return finalData;
		}
		} catch (Exception exception) {
			logger.error("Exception in geting Lowest Merchant", exception);
		}

		return null;
	}

	private Set<String> findBySubUserId(String subuserId, String parentPayId, String dateFrom, String dateTo)
			throws ParseException {
		Set<String> invoiceIdSet = new HashSet<String>();

		BasicDBObject finalQuery = new BasicDBObject();
		try {
			finalQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", dateFrom).add("$lte", dateTo).get());

			if (StringUtils.isNotBlank(subuserId)) {
				finalQuery.put("CREATED_BY", subuserId);

			} else {
				finalQuery.put("PAY_ID", parentPayId);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.EPOS_TRANSACTION_COLLECTION.getValue()));

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				invoiceIdSet.add((String) doc.getString("INVOICE_ID"));
			}
			cursor.close();
		} catch (Exception e) {
			logger.error("exception " + e);
		}
		return invoiceIdSet;
	}

	public List<TransactionSearch> transactionListByTransactionType(String merchantId, String subMerchantPayId,
			String paymentRegion, String currency, String transactionType, String dateFrom, String dateTo, User user, boolean saleReportFlag) {
		Map<String, User> userMap = new HashMap<String, User>();
		logger.info("Inside BarChartQuery , transactionListByTransactionType");
		Set<String> orderId = new HashSet<String>();
		Map<String, User> usrMap = new HashMap<String, User>();
		
		List<TransactionSearch> transactionResult = new ArrayList<TransactionSearch>();
		try {

			boolean isParameterised = false;
			boolean isStatus = false;
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject txnType = new BasicDBObject();

			dateFrom = dateFrom + " 00:00:00";
			dateTo = dateTo + " 23:59:59";

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
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(dateFrom).toLocalizedPattern();
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

			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL MERCHANTS")) {
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

			if (user.getUserType().equals(UserType.SUBUSER) && user.getSubUserType().equalsIgnoreCase("eposType")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_USER_ID.getName(), user.getPayId()));
			}

			if (!paymentRegion.equalsIgnoreCase("ALL") && StringUtils.isNotBlank(paymentRegion)) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentRegion));
			} else {
			}

			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				if(StringUtils.isNotBlank(transactionType) && !transactionType.equalsIgnoreCase("Invalid")){
					paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
				}
			}

			if (StringUtils.isNotBlank(transactionType)) {
				if (transactionType.equalsIgnoreCase("Success") || transactionType.equalsIgnoreCase("grossSuccess")) {

					List<BasicDBObject> otherStatusList = new ArrayList<BasicDBObject>();
					otherStatusList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
					
					if(transactionType.equalsIgnoreCase("Success")) {
						paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), Constants.REAL_TIME_TXN.getValue()));
					}
					/*
					 * if(!otherStatusList.isEmpty()) { isStatus = true; }
					 */
					statusQuery.put("$and", otherStatusList);
				} else if (transactionType.equalsIgnoreCase("Refunded")) {
					List<BasicDBObject> otherStatusList = new ArrayList<BasicDBObject>();
					otherStatusList
							.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
					statusQuery.put("$and", otherStatusList);
				} else if (transactionType.equalsIgnoreCase("Rejected")) {
					List<BasicDBObject> otherStatusList = new ArrayList<BasicDBObject>();
					if(saleReportFlag) {
						txnType = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getName());
					}else {
						txnType = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName());
					}
					
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DECLINED.getName()));
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.REJECTED.getName()));
					otherStatusList.add(
							new BasicDBObject(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName()));
					otherStatusList
							.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName()));
					statusQuery.put("$or", otherStatusList);
				} else if (transactionType.equalsIgnoreCase("Dropped")) {
					List<BasicDBObject> otherStatusList = new ArrayList<BasicDBObject>();
					if(saleReportFlag) {
						txnType = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getName());
					}else {
						txnType = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName());
					}
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName()));
					otherStatusList
							.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ACQUIRER_TIMEOUT.getName()));
					statusQuery.put("$or", otherStatusList);
				} else if (transactionType.equalsIgnoreCase("Failed")) {
					List<BasicDBObject> otherStatusList = new ArrayList<BasicDBObject>();
					if(saleReportFlag) {
						txnType = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getName());
					}else {
						txnType = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName());
					}
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ERROR.getName()));
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
					otherStatusList
							.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName()));
					otherStatusList.add(
							new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED_AT_ACQUIRER.getName()));
					statusQuery.put("$or", otherStatusList);
				} else if (transactionType.equalsIgnoreCase("Cancelled")) {
					List<BasicDBObject> otherStatusList = new ArrayList<BasicDBObject>();
					otherStatusList
							.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.BROWSER_CLOSED.getName()));
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CANCELLED.getName()));
					statusQuery.put("$or", otherStatusList);
				} else if (transactionType.equalsIgnoreCase("Failed")) {
					List<BasicDBObject> otherStatusList = new ArrayList<BasicDBObject>();
					if(saleReportFlag) {
						txnType = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getName());
					}else {
						txnType = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName());
					}
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ERROR.getName()));
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
					otherStatusList
							.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName()));
					otherStatusList.add(
							new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED_AT_ACQUIRER.getName()));
					statusQuery.put("$or", otherStatusList);
				} else if (transactionType.equalsIgnoreCase("Fraud")) {
					List<BasicDBObject> otherStatusList = new ArrayList<BasicDBObject>();
					if(saleReportFlag) {
						txnType = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getName());
					}else {
						txnType = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName());
					}
					otherStatusList
							.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName()));
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED.getName()));
					statusQuery.put("$or", otherStatusList);
				} else if (transactionType.equalsIgnoreCase("Invalid")) {
					List<BasicDBObject> otherStatusList = new ArrayList<BasicDBObject>();
					if(saleReportFlag) {
						txnType = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getName());
					}else {
						txnType = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName());
					}
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName()));
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.INVALID.getName()));
					statusQuery.put("$or", otherStatusList);
				}
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryList.add(dateIndexConditionQuery);
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
			if (!txnType.isEmpty()) {
				fianlList.add(txnType);
			}
			
			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.PAY_ID.getName(), 1);
			projectElement.put(FieldType.SUB_MERCHANT_ID.getName(), 1);
			projectElement.put(FieldType.TXN_ID.getName(), 1);
			projectElement.put(FieldType.PG_REF_NUM.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_TYPE.getName(), 1);
			projectElement.put(FieldType.ORDER_ID.getName(), 1);
			projectElement.put(FieldType.CREATE_DATE.getName(), 1);
			projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
			projectElement.put(FieldType.MOP_TYPE.getName(), 1);
			projectElement.put(FieldType.PAYMENTS_REGION.getName(), 1);
			projectElement.put(FieldType.CARD_HOLDER_TYPE.getName(), 1);
			projectElement.put(FieldType.CARD_MASK.getName(), 1);
			projectElement.put(FieldType.PAYER_ADDRESS.getName(), 1);
			projectElement.put(FieldType.CUST_NAME.getName(), 1);
			projectElement.put(FieldType.TXNTYPE.getName(), 1);
			projectElement.put(FieldType.STATUS.getName(), 1);
			projectElement.put(FieldType.AMOUNT.getName(), 1);
			projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
			projectElement.put(FieldType.TRANSACTION_MODE.getName(), 1);
			projectElement.put(FieldType.TXN_CAPTURE_FLAG.getName(), 1);
			projectElement.put(FieldType.PG_TXN_MESSAGE.getName(), 1);
			projectElement.put(FieldType.RESPONSE_MESSAGE.getName(), 1);
			projectElement.put(FieldType.RESELLER_CHARGES.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_TDR_SC.getName(), 1);
			projectElement.put(FieldType.PG_TDR_SC.getName(), 1);
			projectElement.put(FieldType.RESELLER_GST.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_GST.getName(), 1);
			projectElement.put(FieldType.PG_GST.getName(), 1);
			projectElement.put(FieldType.ORIG_TXNTYPE.getName(), 1);
			projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);
			
			BasicDBObject project = new BasicDBObject("$project", projectElement);
			
			
			logger.info("Inside BarChartQuery , transactionListByTransactionType , fianlList = " + fianlList);
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			BasicDBObject match1 = new BasicDBObject("$match", finalquery);
			BasicDBObject sort1 = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match1, sort1, project);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				TransactionSearch transationList = new TransactionSearch();

				if (usrMap.get(dbobj.get(FieldType.PAY_ID.getName())) != null) {
					
					User usr = usrMap.get(dbobj.get(FieldType.PAY_ID.getName()).toString());
					transationList.setMerchants(usr.getBusinessName());
				}
				else {
					
					User usr = userDao.findPayId(dbobj.get(FieldType.PAY_ID.getName()).toString());
					transationList.setMerchants(usr.getBusinessName());
					usrMap.put(dbobj.get(FieldType.PAY_ID.getName()).toString(), usr);
				}

				if (((!merchantId.equalsIgnoreCase("All")) || (merchantId.equalsIgnoreCase("All")))
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
						transationList.setSubMerchantId(subMerchantUser.getBusinessName());
					} else {
						transationList.setSubMerchantId(CrmFieldConstants.NA.getValue());
					}
				} else {
					transationList.setSubMerchantId(CrmFieldConstants.NA.getValue());
				}

				transationList.setTransactionIdString(dbobj.getString(FieldType.TXN_ID.toString()));

				transationList.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.toString()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()))) {
					transationList.setAcquirerType(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()));
				} else {
					transationList.setAcquirerType(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				transationList.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
				transationList.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					transationList.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transationList.setPaymentMethods(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.MOP_TYPE.toString()))) {
					transationList.setMopType(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));
				} else {
					transationList.setMopType(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
					transationList.setPaymentRegion(dbobj.getString(FieldType.PAYMENTS_REGION.toString()));

				} else {
					transationList.setPaymentRegion(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {
					transationList.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()));

				} else {
					transationList.setCardHolderType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_MASK.toString()))) {
						transationList.setCardNumber(dbobj.getString(FieldType.CARD_MASK.toString()));
					} else if (null != dbobj.getString(FieldType.PAYER_ADDRESS.getName())) {

						if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
							transationList.setCardNumber(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
						} else {
							String vpaString = dbobj.getString(FieldType.PAYER_ADDRESS.getName());
							String[] vpaArray = vpaString.split("@");
							char[] vpaChar = vpaArray[0].toCharArray();
							StringBuilder vpastrBuilder = new StringBuilder();

							if (vpaChar.length > 3) {
								for (int i = 0; i < vpaChar.length - 3; i++) {
									vpastrBuilder.append(vpaChar[i]);
								}
								vpastrBuilder.append("***@");
								vpastrBuilder.append(vpaArray[1]);
							} else {
								vpastrBuilder.append(vpaChar[0]);
								vpastrBuilder.append("**@");
								vpastrBuilder.append(vpaArray[1]);
							}

							transationList.setCardNumber(vpastrBuilder.toString());
						}
					} else {
						transationList.setCardNumber(CrmFieldConstants.NA.getValue());
					}
				} else {
					transationList.setCardNumber(CrmFieldConstants.NA.getValue());
				}

				if (null != ((Document) dbobj).getString(FieldType.CUST_NAME.toString())) {
					transationList.setCustomerName(((Document) dbobj).getString(FieldType.CUST_NAME.toString()));
				} else {
					transationList.setCustomerName(CrmFieldConstants.NA.getValue());
				}

				transationList.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
				transationList.setStatus(dbobj.getString(FieldType.STATUS.toString()));
				if(StringUtils.isNotBlank((dbobj.getString(FieldType.AMOUNT.toString())))){
					transationList.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				}else {
					transationList.setAmount("NA");
				}
				
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
					transationList.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transationList.setTotalAmount("NA");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_MODE.getName()))) {
					transationList.setTransactionMode(dbobj.getString(FieldType.TRANSACTION_MODE.getName()));
				} else {
					transationList.setTransactionMode(CrmFieldConstants.NA.getValue());
				}

				if (transactionType.equalsIgnoreCase("Success") || transactionType.equalsIgnoreCase("grossSuccess")) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
						transationList.setTxnSettledType(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
					} else {
						transationList.setTxnSettledType(CrmFieldConstants.NA.getValue());
					}
				} else {
					transationList.setTxnSettledType("");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()))) {
					transationList.setPgTxnMessage(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
				} else {
					transationList.setPgTxnMessage(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESPONSE_MESSAGE.getName()))) {
					transationList.setResponseMessage(dbobj.getString(FieldType.RESPONSE_MESSAGE.getName()));
				} else {
					transationList.setResponseMessage(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
							&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

						transationList.setTdr_Surcharge(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

					} else {
						transationList.setTdr_Surcharge("0.00");
					}
				} else {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
							&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

						transationList.setTdr_Surcharge(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

					} else {
						transationList.setTdr_Surcharge("0.00");
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
							&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
						transationList.setGst_charge(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

					} else {
						transationList.setGst_charge("0.00");
					}
				} else {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
							&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
						transationList.setGst_charge(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

					} else {
						transationList.setGst_charge("0.00");
					}
				}

				if (TxnType.SALE.getName().equalsIgnoreCase(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {

						if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transationList.setTotalAmtPayable("-" + String.format("%.2f", (Double
											.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));
								} else {
									transationList.setTotalAmtPayable("NA");
								}

							} else {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transationList.setTotalAmtPayable("-" + String.format("%.2f", (Double
											.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString())))));
								} else {
									transationList.setTotalAmtPayable("NA");
								}
							}

						} else {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))) {
									transationList.setTotalAmtPayable(String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.RESELLER_GST.toString()))

											)));
								} else {
									if (dbobj.getString(FieldType.PAYMENT_TYPE.toString()).equalsIgnoreCase("RTGS")
											|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
													.equalsIgnoreCase("NEFT")
											|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
													.equalsIgnoreCase("IMPS")) {

										transationList
												.setTotalAmtPayable(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
									} else {
										transationList.setTotalAmtPayable("NA");
									}
								}

							} else {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))) {
									transationList.setTotalAmtPayable(String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.PG_GST.toString())))));
								} else {
									if (dbobj.getString(FieldType.PAYMENT_TYPE.toString()).equalsIgnoreCase("RTGS")
											|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
													.equalsIgnoreCase("NEFT")
											|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
													.equalsIgnoreCase("IMPS")) {

										transationList
												.setTotalAmtPayable(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
									} else {
										transationList.setTotalAmtPayable("NA");
									}
								}
							}
						}

					} else {
						transationList.setTotalAmtPayable("NA");
					}

				} else if (TxnType.REFUND.getName()
						.equalsIgnoreCase(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.getName()))) {

						if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())) {
							transationList.setGst_charge("0.00");
							transationList.setTdr_Surcharge("0.00");
							transationList.setTotalAmtPayable("0.00");
						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
								transationList.setTotalAmtPayable(String.format("%.2f",
										Double.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString())) * -1));
							} else {
								transationList.setTotalAmtPayable("NA");
							}
						}

					} else {
						transationList.setTotalAmtPayable("NA");
					}
				} else {
					transationList.setTotalAmtPayable("NA");
				}

				transactionResult.add(transationList);

			}

			logger.info("transactionList created and size = " + transactionResult.size());
			cursor.close();
			return transactionResult;
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , DashBoardReport, Exception = " + e);
			return transactionResult;
		}
	}
	
	public Statistics pauOutDataReportSummary(String merchantId, String dateFrom, String dateTo, User sessionUser,
			String subMerchantId) {
		logger.info("Inside pauOutDataReportSummary()");
		Statistics payOut = new Statistics();
		BigDecimal totalTxnAmount = BigDecimal.ZERO;
		BigDecimal totalCapturedAmount = BigDecimal.ZERO;

		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();

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

			paramConditionLst.add(dateQuery);

			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));
			}

			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}

			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId()));
			}

			paramConditionLst.add(new BasicDBObject(FieldType.USER_TYPE.name(), "Merchant Initiated Direct"));

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			int totalTxnCount = 0;
			int totalCapturedCount = 0;
			int totalPendingOrTimeOutCount = 0;
			int totalOtherTxnCount = 0;

			while (cursor.hasNext()) {
				try {
					Document dbobj = cursor.next();
					totalTxnCount++;

					String txnAmount = "0.00";
					String capturedAmount = "0.00";

					txnAmount = (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
					if (StringUtils.isBlank(txnAmount)) {
						txnAmount = (dbobj.getString(FieldType.AMOUNT.toString()));
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString())) && dbobj
							.getString(FieldType.STATUS.toString()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
						totalCapturedCount++;
						capturedAmount = dbobj.getString(FieldType.TOTAL_AMOUNT.toString());

						if (StringUtils.isBlank(capturedAmount)) {
							capturedAmount = (dbobj.getString(FieldType.AMOUNT.toString()));
						}
					} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString())) && (dbobj
							.getString(FieldType.STATUS.toString()).equalsIgnoreCase(StatusType.PENDING.getName())
							|| dbobj.getString(FieldType.STATUS.toString())
									.equalsIgnoreCase(StatusType.TIMEOUT.getName()))) {
						totalPendingOrTimeOutCount++;

					} else {
						totalOtherTxnCount++;
					}

					BigDecimal grossAddApproved = new BigDecimal(txnAmount);
					totalTxnAmount = totalTxnAmount.add(grossAddApproved).setScale(2, RoundingMode.HALF_DOWN);

					BigDecimal addApproved = new BigDecimal(capturedAmount);
					totalCapturedAmount = totalCapturedAmount.add(addApproved).setScale(2, RoundingMode.HALF_DOWN);
				} catch (Exception ex) {
					logger.error("exception in loop : ", ex);
				}
			}
			payOut.setTotalTransaction(String.valueOf(totalTxnCount));
			payOut.setTotalSuccess(String.valueOf(totalCapturedCount));
			payOut.setTotalPending(String.valueOf(totalPendingOrTimeOutCount));
			payOut.setTotalFailed(String.valueOf(totalOtherTxnCount));
			payOut.setTotalAmount(String.valueOf(totalTxnAmount));
			payOut.setTotalCapturedAmount(String.valueOf(totalCapturedAmount));

			return payOut;
		} catch (Exception e) {
			logger.error("Exception in pauOutDataReportSummary : ", e);
			return payOut;
		}
	}
	
	public List<TransactionSearch> pauOutDataReportForDownload(String merchantId, String dateFrom, String dateTo, User sessionUser,
			String subMerchantId, String transactionType) {
		Map<String, User> userMap = new HashMap<String, User>();
		logger.info("Inside BarChartQuery , transactionListByTransactionType");
		Set<String> orderId = new HashSet<String>();
		Map<String, User> usrMap = new HashMap<String, User>();
		
		List<TransactionSearch> transactionResult = new ArrayList<TransactionSearch>();
		try {
			
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateQuery = new BasicDBObject();

			dateFrom = dateFrom + " 00:00:00";
			dateTo = dateTo + " 23:59:59";

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
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(dateFrom).toLocalizedPattern();
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

			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL MERCHANTS")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));
			}

			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}
			
			if (StringUtils.isNotBlank(transactionType)) {
				if(transactionType.equalsIgnoreCase("Captured")) {
					paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
				} else if(transactionType.equalsIgnoreCase("Pending")) {
					List<BasicDBObject> statusList = new ArrayList<BasicDBObject>();
					statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PENDING.getName()));
					statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName()));
					
					paramConditionLst.add(new BasicDBObject("$or" , statusList));
				} else if(transactionType.equalsIgnoreCase("Failed")){
					List<String> statusList = new ArrayList<String>();
					statusList.add(StatusType.PENDING.getName());
					statusList.add(StatusType.TIMEOUT.getName());
					statusList.add(StatusType.CAPTURED.getName());
					BasicDBObject statusOblect = new BasicDBObject();
					statusOblect.put("$nin", statusList);
					
					paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName() , statusOblect));
				}
			}

			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId()));
			}
//			if(!dateIndexConditionQuery.isEmpty()) {
//				paramConditionLst.add(dateIndexConditionQuery);
//			}
			if(!dateQuery.isEmpty()) {
				paramConditionLst.add(dateQuery);
			}
			paramConditionLst.add(new BasicDBObject(FieldType.USER_TYPE.name(), "Merchant Initiated Direct"));
			
			
			logger.info("Inside BarChartQuery , transactionListByTransactionType , fianlList = " + paramConditionLst);
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
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

				TransactionSearch transationList = new TransactionSearch();

				if (usrMap.get(dbobj.get(FieldType.PAY_ID.getName())) != null) {
					
					User usr = usrMap.get(dbobj.get(FieldType.PAY_ID.getName()).toString());
					transationList.setMerchants(usr.getBusinessName());
				} else {
					
					User usr = userDao.findPayId(dbobj.get(FieldType.PAY_ID.getName()).toString());
					transationList.setMerchants(usr.getBusinessName());
					usrMap.put(dbobj.get(FieldType.PAY_ID.getName()).toString(), usr);
				}

				if (((!merchantId.equalsIgnoreCase("All")) || (merchantId.equalsIgnoreCase("All")))
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
						transationList.setSubMerchantId(subMerchantUser.getBusinessName());
					} else {
						transationList.setSubMerchantId(CrmFieldConstants.NA.getValue());
					}
				} else {
					transationList.setSubMerchantId(CrmFieldConstants.NA.getValue());
				}

				transationList.setTransactionIdString(dbobj.getString(FieldType.TXN_ID.toString()));
				
				transationList.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
				transationList.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));		

				
				transationList.setStatus(dbobj.getString(FieldType.STATUS.toString()));
				transationList.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));

				transactionResult.add(transationList);

			}

			logger.info("transactionList created and size = " + transactionResult.size());
			cursor.close();
			return transactionResult;
		} catch (Exception e) {
			logger.error("Exception in PayOutDashBoardReport, Exception = " + e);
			return transactionResult;
		}
	}
}
