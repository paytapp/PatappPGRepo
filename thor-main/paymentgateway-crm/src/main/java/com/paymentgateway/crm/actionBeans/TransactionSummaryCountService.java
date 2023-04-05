package com.paymentgateway.crm.actionBeans;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.MerchantSMSObject;
import com.paymentgateway.commons.user.TransactionCountSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.crm.action.TdrPojo;

@Service
public class TransactionSummaryCountService {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userDao;

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private UserSettingDao userSettingDao;

	private static Logger logger = LoggerFactory.getLogger(TransactionSummaryCountService.class.getName());
	private static final String prefix = "MONGO_DB_";
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
	Map<String, User> userMap = new HashMap<String, User>();
	private double postSettledTransactionCount = 0;

	public MerchantSMSObject getMerchantSMSData(String fromDate, String toDate, String payId, String paymentType,
			String acquirer, User user, int start, int length, String paymentsRegion, String cardHolderType,
			String mopType, String transactionType) {

		MerchantSMSObject merchantSMSObject = new MerchantSMSObject();

		try {

			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject acquirerQuery = new BasicDBObject();

			BasicDBObject allParamQuery = new BasicDBObject();
			List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> saleSettledList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> refundSettledList = new ArrayList<BasicDBObject>();

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

			if (!payId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			}

			if (!paymentsRegion.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentsRegion));
			}

			if (!cardHolderType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CARD_HOLDER_TYPE.getName(), cardHolderType));
			}

			if (!mopType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), mopType));
			}

			if (!acquirer.equalsIgnoreCase("ALL")) {

				List<String> acquirerList = Arrays.asList(acquirer.split(","));
				for (String acq : acquirerList) {
					acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acq));
				}
				acquirerQuery.append("$or", acquirerConditionLst);
			}

			if (!paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			// SALE Settled query
			List<BasicDBObject> saleSettledConditionList = new ArrayList<BasicDBObject>();
			saleSettledConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			saleSettledConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleSettledConditionList);
			saleSettledList.add(saleConditionQuery);

			// REFUND Settled query
			List<BasicDBObject> refundSettledConditionList = new ArrayList<BasicDBObject>();
			refundSettledConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
			refundSettledConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

			BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundSettledConditionList);
			saleSettledList.add(refundConditionQuery);

			BasicDBObject saleSettledConditionQuery = new BasicDBObject("$or", saleSettledList);
			BasicDBObject refundSettledConditionQuery = new BasicDBObject("$or", refundSettledList);

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			if (!currencyQuery.isEmpty()) {
				allConditionQueryList.add(currencyQuery);
			}
			if (!acquirerQuery.isEmpty()) {
				allConditionQueryList.add(acquirerQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			allConditionQueryList.add(saleSettledConditionQuery);
			// allConditionQueryList.add(refundSettledConditionQuery);

			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			BasicDBObject finalQuery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			BigDecimal saleSettledAmount = BigDecimal.ZERO;
			BigDecimal refundSettledAmount = BigDecimal.ZERO;
			BigDecimal settledAmount = BigDecimal.ZERO;

			int ccSettledCount = 0;
			int dcSettledCount = 0;
			int upSettledCount = 0;
			int nbSettledCount = 0;
			int wlSettledCount = 0;
			int emSettledCount = 0;
			int cdSettledCount = 0;

			Double ccTxnPer = 0.00;
			Double ccAmtPer = 0.00;
			BigDecimal ccTotalAmt = BigDecimal.ZERO;
			Double dcTxnPer = 0.00;
			Double dcAmtPer = 0.00;
			BigDecimal dcTotalAmt = BigDecimal.ZERO;
			Double upTxnPer = 0.00;
			Double upAmtPer = 0.00;
			BigDecimal upTotalAmt = BigDecimal.ZERO;
			Double nbTxnPer = 0.00;
			Double nbAmtPer = 0.00;
			BigDecimal nbTotalAmt = BigDecimal.ZERO;
			Double wlTxnPer = 0.00;
			Double wlAmtPer = 0.00;
			BigDecimal wlTotalAmt = BigDecimal.ZERO;
			Double emTxnPer = 0.00;
			Double emAmtPer = 0.00;
			BigDecimal emTotalAmt = BigDecimal.ZERO;
			Double cdTxnPer = 0.00;
			Double cdAmtPer = 0.00;
			BigDecimal cdTotalAmt = BigDecimal.ZERO;

			List<String> captureDateArray = new ArrayList<String>();

			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (dbobj.getString(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.RECO.getName())) {
					saleSettledAmount = saleSettledAmount
							.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));

					if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())) {
						ccSettledCount++;
						ccTotalAmt = ccTotalAmt.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())) {
						dcSettledCount++;
						dcTotalAmt = dcTotalAmt.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.UPI.getCode())) {
						upSettledCount++;
						upTotalAmt = upTotalAmt.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
						nbSettledCount++;
						nbTotalAmt = nbTotalAmt.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.EMI.getCode())) {
						emSettledCount++;
						emTotalAmt = emTotalAmt.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.WALLET.getCode())) {
						wlSettledCount++;
						wlTotalAmt = wlTotalAmt.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.COD.getCode())) {
						cdSettledCount++;
						cdTotalAmt = cdTotalAmt.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					}

					captureDateArray.add(dbobj.getString(FieldType.PG_DATE_TIME.getName()));

				} else {
					refundSettledAmount = refundSettledAmount
							.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
				}

			}

			cursor.close();
			Collections.sort(captureDateArray);

			if ((ccSettledCount + dcSettledCount + upSettledCount) > 0) {

				ccTxnPer = (Double.valueOf(ccSettledCount) / Double.valueOf(ccSettledCount + dcSettledCount
						+ upSettledCount + nbSettledCount + wlSettledCount + emSettledCount + cdSettledCount)) * 100;
				dcTxnPer = (Double.valueOf(dcSettledCount) / Double.valueOf(ccSettledCount + dcSettledCount
						+ upSettledCount + nbSettledCount + wlSettledCount + emSettledCount + cdSettledCount)) * 100;
				upTxnPer = (Double.valueOf(upSettledCount) / Double.valueOf(ccSettledCount + dcSettledCount
						+ upSettledCount + nbSettledCount + wlSettledCount + emSettledCount + cdSettledCount)) * 100;
				nbTxnPer = (Double.valueOf(nbSettledCount) / Double.valueOf(ccSettledCount + dcSettledCount
						+ upSettledCount + nbSettledCount + wlSettledCount + emSettledCount + cdSettledCount)) * 100;
				wlTxnPer = (Double.valueOf(wlSettledCount) / Double.valueOf(ccSettledCount + dcSettledCount
						+ upSettledCount + nbSettledCount + wlSettledCount + emSettledCount + cdSettledCount)) * 100;
				emTxnPer = (Double.valueOf(emSettledCount) / Double.valueOf(ccSettledCount + dcSettledCount
						+ upSettledCount + nbSettledCount + wlSettledCount + emSettledCount + cdSettledCount)) * 100;
				cdTxnPer = (Double.valueOf(cdSettledCount) / Double.valueOf(ccSettledCount + dcSettledCount
						+ upSettledCount + nbSettledCount + wlSettledCount + emSettledCount + cdSettledCount)) * 100;

				ccAmtPer = (ccTotalAmt.divide(saleSettledAmount, 2, RoundingMode.HALF_DOWN))
						.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
				dcAmtPer = (dcTotalAmt.divide(saleSettledAmount, 2, RoundingMode.HALF_DOWN))
						.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
				upAmtPer = (upTotalAmt.divide(saleSettledAmount, 2, RoundingMode.HALF_DOWN))
						.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
				wlAmtPer = (wlTotalAmt.divide(saleSettledAmount, 2, RoundingMode.HALF_DOWN))
						.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
				emAmtPer = (emTotalAmt.divide(saleSettledAmount, 2, RoundingMode.HALF_DOWN))
						.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
				cdAmtPer = (cdTotalAmt.divide(saleSettledAmount, 2, RoundingMode.HALF_DOWN))
						.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
				nbAmtPer = (nbTotalAmt.divide(saleSettledAmount, 2, RoundingMode.HALF_DOWN))
						.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
				settledAmount = saleSettledAmount.subtract(refundSettledAmount);
			}

			SimpleDateFormat outFormat = new SimpleDateFormat("dd-MMM-yyyy");

			Date dateCapFrom = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.ENGLISH)
					.parse(captureDateArray.get(0));
			Date dateCapTo = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.ENGLISH)
					.parse(captureDateArray.get(captureDateArray.size() - 1));

			String dateCapFromString = outFormat.format(dateCapFrom);
			String dateCapToString = outFormat.format(dateCapTo);

			if (captureDateArray.size() > 0) {

				if (dateCapFromString.equalsIgnoreCase(dateCapToString)) {

					merchantSMSObject.setDateCaptured(dateCapFromString);
				} else {
					merchantSMSObject.setDateCaptured(dateCapFromString + " to " + dateCapToString);
				}

			} else {
				merchantSMSObject.setDateCaptured("No Data Found");
			}

			Date dateSettleFrom = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.ENGLISH).parse(fromDate);
			Date dateSettleTo = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.ENGLISH).parse(toDate);

			String dateSettleFromString = outFormat.format(dateSettleFrom);
			String dateSettleToString = outFormat.format(dateSettleTo);

			if (dateSettleFromString.equalsIgnoreCase(dateSettleToString)) {
				merchantSMSObject.setDateSettled(dateSettleFromString);
			} else {
				merchantSMSObject.setDateSettled(dateSettleFromString + " to " + dateSettleToString);
			}

			merchantSMSObject.setTotalSettledAmount(format(String.format("%.0f", saleSettledAmount)) + ".00");

			merchantSMSObject.setCcTxnPer(String.format("%.2f", ccTxnPer));
			merchantSMSObject.setCcAmtPer(String.format("%.2f", ccAmtPer));
			merchantSMSObject.setCcSettledAmt(format(String.format("%.0f", ccTotalAmt)) + ".00");

			merchantSMSObject.setDcTxnPer(String.format("%.2f", dcTxnPer));
			merchantSMSObject.setDcAmtPer(String.format("%.2f", dcAmtPer));
			merchantSMSObject.setDcSettledAmt(format(String.format("%.0f", dcTotalAmt)) + ".00");

			merchantSMSObject.setUpTxnPer(String.format("%.2f", upTxnPer));
			merchantSMSObject.setUpAmtPer(String.format("%.2f", upAmtPer));
			merchantSMSObject.setUpSettledAmt(format(String.format("%.0f", upTotalAmt)) + ".00");

			merchantSMSObject.setNbTxnPer(String.format("%.2f", nbTxnPer));
			merchantSMSObject.setNbAmtPer(String.format("%.2f", nbAmtPer));
			merchantSMSObject.setNbSettledAmt(format(String.format("%.0f", nbTotalAmt)) + ".00");

			merchantSMSObject.setWlTxnPer(String.format("%.2f", wlTxnPer));
			merchantSMSObject.setWlAmtPer(String.format("%.2f", wlAmtPer));
			merchantSMSObject.setWlSettledAmt(format(String.format("%.0f", wlTotalAmt)) + ".00");

			merchantSMSObject.setEmTxnPer(String.format("%.2f", emTxnPer));
			merchantSMSObject.setEmAmtPer(String.format("%.2f", emAmtPer));
			merchantSMSObject.setEmSettledAmt(format(String.format("%.0f", emTotalAmt)) + ".00");

			merchantSMSObject.setCdTxnPer(String.format("%.2f", cdTxnPer));
			merchantSMSObject.setCdAmtPer(String.format("%.2f", cdAmtPer));
			merchantSMSObject.setCdSettledAmt(format(String.format("%.0f", cdTotalAmt)) + ".00");

			merchantSMSObject.setSettledAmount(format(String.format("%.0f", settledAmount)) + ".00");
			merchantSMSObject.setTotalTxnCount(String.valueOf(ccSettledCount + dcSettledCount + upSettledCount
					+ nbSettledCount + wlSettledCount + emSettledCount + cdSettledCount));

			return merchantSMSObject;
		}

		catch (Exception e) {

			logger.error("Exception in transaction summary count service " , e);
			merchantSMSObject.setDateCaptured("Invalid Date Range");
			merchantSMSObject.setDateSettled("Invalid Date Range");

			merchantSMSObject.setTotalSettledAmount("0.00");

			merchantSMSObject.setCcTxnPer("0.00");
			merchantSMSObject.setCcAmtPer("0.00");
			merchantSMSObject.setCcSettledAmt("0.00");

			merchantSMSObject.setDcTxnPer("0.00");
			merchantSMSObject.setDcAmtPer("0.00");
			merchantSMSObject.setDcSettledAmt("0.00");

			merchantSMSObject.setUpTxnPer("0.00");
			merchantSMSObject.setUpAmtPer("0.00");
			merchantSMSObject.setUpSettledAmt("0.00");

			merchantSMSObject.setNbTxnPer("0.00");
			merchantSMSObject.setNbAmtPer("0.00");
			merchantSMSObject.setNbSettledAmt("0.00");

			merchantSMSObject.setWlTxnPer("0.00");
			merchantSMSObject.setWlAmtPer("0.00");
			merchantSMSObject.setWlSettledAmt("0.00");

			merchantSMSObject.setEmTxnPer("0.00");
			merchantSMSObject.setEmAmtPer("0.00");
			merchantSMSObject.setEmSettledAmt("0.00");

			merchantSMSObject.setCdTxnPer("0.00");
			merchantSMSObject.setCdAmtPer("0.00");
			merchantSMSObject.setCdSettledAmt("0.00");

			merchantSMSObject.setSettledAmount("0.00");

			return merchantSMSObject;
		}
	}

	public String format(String amount) {
		StringBuilder stringBuilder = new StringBuilder();
		char amountArray[] = amount.toCharArray();
		int a = 0, b = 0;
		for (int i = amountArray.length - 1; i >= 0; i--) {
			if (a < 3) {
				stringBuilder.append(amountArray[i]);
				a++;
			} else if (b < 2) {
				if (b == 0) {
					stringBuilder.append(",");
					stringBuilder.append(amountArray[i]);
					b++;
				} else {
					stringBuilder.append(amountArray[i]);
					b = 0;
				}
			}
		}
		return stringBuilder.reverse().toString();
	}

	public TransactionCountSearch getTransactionCount(String fromDate, String toDate, String payId, String subMerchantPayId, String paymentType,
			String acquirer, User user, int start, int length, String paymentsRegion, String cardHolderType,
			String mopType, String transactionType,String statusType, String partSettleFlag, String currency) {

		List<TransactionCountSearch> transactionCountSearchList = new ArrayList<TransactionCountSearch>();
		List<String> txnTypeList = new ArrayList<String>();
		txnTypeList.add(TxnType.SALE.getName());
		txnTypeList.add(TxnType.REFUND.getName());
		try {
			postSettledTransactionCount = 0;
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject acquirerQuery = new BasicDBObject();
			BasicDBObject settledDateQuery = new BasicDBObject();

			BasicDBObject allParamQuery = new BasicDBObject();
			List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> saleSettledList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> refundSettledList = new ArrayList<BasicDBObject>();

			String currentDate = null;
			if (!fromDate.isEmpty()) {
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				if(statusType.equalsIgnoreCase("Captured")) {
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
				}
				if(statusType.equalsIgnoreCase("Settled")) {
					settledDateQuery.put(FieldType.SETTLEMENT_DATE.getName(),
							BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
									.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
				}
			}

			List<BasicDBObject> dateIndexConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(fromDate).toLocalizedPattern();
			String endString = new SimpleDateFormat(currentDate).toLocalizedPattern();

			DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format1.parse(startString);
			Date dateEnd = format1.parse(endString);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}

			for (String dateIndex : allDatesIndex) {
				if(statusType.equalsIgnoreCase("Captured")) {
					dateIndexConditionList.add(new BasicDBObject("DATE_INDEX", dateIndex));
				}
				if(statusType.equalsIgnoreCase("Settled")) {
					dateIndexConditionList.add(new BasicDBObject("SETTLEMENT_DATE_INDEX", dateIndex));
				}
			}
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndexConditionQuery.append("$or", dateIndexConditionList);
			}

			if (!payId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			}
			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}

			if (!paymentsRegion.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentsRegion));
			}

			if (!cardHolderType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CARD_HOLDER_TYPE.getName(), cardHolderType));
			}

			if (!mopType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), mopType));
			}

			if (!acquirer.equalsIgnoreCase("ALL")) {

				List<String> acquirerList = Arrays.asList(acquirer.split(","));
				for (String acq : acquirerList) {

					acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acq));
				}
				acquirerQuery.append("$or", acquirerConditionLst);
			}

			if (!paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}
			if (!partSettleFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			
			if (!transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), transactionType));
			}

			// SALE Settled query
			List<BasicDBObject> saleSettledConditionList = new ArrayList<BasicDBObject>();
			if(statusType.equalsIgnoreCase("Settled")){
					saleSettledConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
				saleSettledConditionList.add(new BasicDBObject(FieldType.SETTLEMENT_FLAG.getName(), "Y"));
				saleSettledConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			}else {
				saleSettledConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), statusType));
			}
			if(statusType.equalsIgnoreCase("Captured")){
				saleSettledConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			}
			
			/*saleSettledConditionList
					.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));*/

			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleSettledConditionList);
			saleSettledList.add(saleConditionQuery);

			// REFUND Settled query
			List<BasicDBObject> refundSettledConditionList = new ArrayList<BasicDBObject>();
			if(statusType.equalsIgnoreCase("Settled")){
				refundSettledConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
				refundSettledConditionList.add(new BasicDBObject(FieldType.SETTLEMENT_FLAG.getName(), "Y"));
			//	saleSettledConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			}else {
				refundSettledConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), statusType));
			}
			if(statusType.equalsIgnoreCase("Captured")){
				refundSettledConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
			}
			
			/*refundSettledConditionList
					.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));*/

			BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundSettledConditionList);
			refundSettledList.add(refundConditionQuery);

			BasicDBObject saleSettledConditionQuery = new BasicDBObject("$or", saleSettledList);
			BasicDBObject refundSettledConditionQuery = new BasicDBObject("$or", refundSettledList);

			TransactionCountSearch transactionCountSearch = new TransactionCountSearch();
			transactionCountSearch.setAcquirer(acquirer);

			if (!paymentType.equalsIgnoreCase("ALL")) {
				transactionCountSearch.setPaymentMethod(PaymentType.getpaymentName(paymentType));
			} else {
				transactionCountSearch.setPaymentMethod(paymentType);
			}
			

			List<BasicDBObject> dateIndexQueryList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> dateConditionQueryList = new ArrayList<BasicDBObject>();

			for (String txnType : txnTypeList) {

				List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
				if (!currencyQuery.isEmpty()) {
					allConditionQueryList.add(currencyQuery);
				}
				if (!acquirerQuery.isEmpty()) {
					allConditionQueryList.add(acquirerQuery);
				}
//				if (!dateQuery.isEmpty()) {
//					allConditionQueryList.add(dateQuery);
//				}
//				if (!dateIndexConditionQuery.isEmpty()) {
//					allConditionQueryList.add(dateIndexConditionQuery);
//				}
				
				if (!dateQuery.isEmpty()) {
					dateConditionQueryList.add(dateQuery);
				}
				
				if (!settledDateQuery.isEmpty()) {
					dateConditionQueryList.add(settledDateQuery);
				}
				
				
				if (!dateConditionQueryList.isEmpty()) {
					BasicDBObject dateConditionQueryObj = new BasicDBObject("$or", dateConditionQueryList);
					allConditionQueryList.add(dateConditionQueryObj);
				}
				
				
				if (!dateIndexConditionQuery.isEmpty()) {
					dateIndexQueryList.add(dateIndexConditionQuery);
				}
				
				
				if (!dateIndexQueryList.isEmpty()) {
					BasicDBObject dateIndexQueryObj = new BasicDBObject("$or", dateIndexQueryList);
					allConditionQueryList.add(dateIndexQueryObj);
				}

				if (txnType.equalsIgnoreCase(TransactionType.SALE.getName())) {
					if (!saleSettledConditionQuery.isEmpty()) {
						allConditionQueryList.add(saleSettledConditionQuery);
					}
				} else {
					if (!refundSettledConditionQuery.isEmpty()) {
						allConditionQueryList.add(refundSettledConditionQuery);
					}
				}

				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);

				List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

				if (!allParamQuery.isEmpty()) {
					fianlList.add(allParamQuery);
				}
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}

				BasicDBObject finalQuery = new BasicDBObject("$and", fianlList);
				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns.getCollection(
						PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
				
                BasicDBObject projectElement = new BasicDBObject();
                projectElement.put(FieldType.PAY_ID.getName(), 1);
                projectElement.put(FieldType.POST_SETTLED_FLAG.getName(), 1);
                projectElement.put(FieldType.AMOUNT.getName(), 1);
                projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
                projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
                projectElement.put(FieldType.ACQUIRER_TYPE.getName(), 1);
                projectElement.put(FieldType.MOP_TYPE.getName(), 1);
                projectElement.put(FieldType.PAYMENTS_REGION.getName(), 1);
                projectElement.put(FieldType.ACQUIRER_GST.getName(), 1);
                projectElement.put(FieldType.ACQUIRER_TDR_SC.getName(), 1);
                projectElement.put(FieldType.PG_TDR_SC.getName(), 1);
                projectElement.put(FieldType.PG_GST.getName(), 1);
                projectElement.put(FieldType.TXNTYPE.getName(), 1);
                projectElement.put(FieldType.RESELLER_CHARGES.getName(), 1);
                projectElement.put(FieldType.RESELLER_GST.getName(), 1);
                projectElement.put(FieldType.SETTLEMENT_FLAG.getName(), 1);
                projectElement.put(FieldType.SETTLEMENT_DATE.getName(), 1);
                projectElement.put(FieldType.ORDER_ID.getName(), 1);
                projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);
                
                BasicDBObject project = new BasicDBObject("$project", projectElement);
                
                BasicDBObject match = new BasicDBObject("$match", finalQuery);
                List<BasicDBObject> pipeline = Arrays.asList(match, project);
                AggregateIterable<Document> output = coll.aggregate(pipeline);
                output.allowDiskUse(true);
                MongoCursor<Document> cursor = output.iterator();
                

				int totalSettled = (int) coll.count(finalQuery);
				// TODO remove delta count
				if (txnType.equalsIgnoreCase(TransactionType.SALE.getName())) {

					transactionCountSearch.setSaleSettledCount(String.valueOf(totalSettled));
				} else {
					transactionCountSearch.setRefundSettledCount(String.valueOf(totalSettled));
				}
			//	MongoCursor<Document> cursor = coll.find(finalQuery).iterator();
		
				// Remove all data from an earlier map
				while (cursor.hasNext()) {
					Document dbobj = cursor.next();
					
					TransactionCountSearch transactionCountSearchObj = new TransactionCountSearch();
					transactionCountSearchObj = findDetails(dbobj);
					if(transactionCountSearchObj != null) {
						transactionCountSearchObj.setSaleSettledCount(transactionCountSearch.getSaleSettledCount());
						transactionCountSearchObj.setRefundSettledCount(transactionCountSearch.getRefundSettledCount());
						transactionCountSearchList.add(transactionCountSearchObj);
					}

				}

				cursor.close();
			}

			BigDecimal saleSettledAmount = BigDecimal.ZERO;
			BigDecimal pgSaleSurcharge = BigDecimal.ZERO;
			BigDecimal acquirerSaleSurcharge = BigDecimal.ZERO;
			BigDecimal pgSaleGst = BigDecimal.ZERO;
			BigDecimal acquirerSaleGst = BigDecimal.ZERO;

			BigDecimal refundSettledAmount = BigDecimal.ZERO;
			BigDecimal pgRefundSurcharge = BigDecimal.ZERO;
			BigDecimal acquirerRefundSurcharge = BigDecimal.ZERO;
			BigDecimal pgRefundGst = BigDecimal.ZERO;
			BigDecimal acquirerRefundGst = BigDecimal.ZERO;

			BigDecimal totalMerchantAmount = BigDecimal.ZERO;
			BigDecimal merchantSaleSettledAmount = BigDecimal.ZERO;
			BigDecimal merchantRefundSettledAmount = BigDecimal.ZERO;

			BigDecimal totalSettledAmountActual = BigDecimal.ZERO;
			BigDecimal totalSettledAmountDelta = BigDecimal.ZERO;

			double ccSettledCount = 0;
			double dcSettledCount = 0;
			double upSettledCount = 0;
			double nbSettledCount = 0;
			double wlSettledCount = 0;
			double emSettledCount = 0;
			double cdSettledCount = 0;
			for (TransactionCountSearch transactionCountSearchObj : transactionCountSearchList) {

				if (StringUtils.isNotBlank(transactionCountSearchObj.getTxnType()) && transactionCountSearchObj.getTxnType().equalsIgnoreCase(TransactionType.SALE.getName())) {

					BigDecimal saleSettledAmountObj = new BigDecimal(transactionCountSearchObj.getSaleSettledAmount());
					BigDecimal pgSaleSurchargeObj = new BigDecimal(transactionCountSearchObj.getPgSaleSurcharge());
					BigDecimal acquirerSaleSurchargeObj = new BigDecimal(
							transactionCountSearchObj.getAcquirerSaleSurcharge());
					BigDecimal pgSaleGstObj = new BigDecimal(transactionCountSearchObj.getPgSaleGst());
					BigDecimal acquirerSaleGstObj = new BigDecimal(transactionCountSearchObj.getAcquirerSaleGst());

					saleSettledAmount = saleSettledAmount.add(saleSettledAmountObj).setScale(2, RoundingMode.HALF_DOWN);
					pgSaleSurcharge = pgSaleSurcharge.add(pgSaleSurchargeObj).setScale(2, RoundingMode.HALF_DOWN);
					acquirerSaleSurcharge = acquirerSaleSurcharge.add(acquirerSaleSurchargeObj).setScale(2,
							RoundingMode.HALF_DOWN);
					pgSaleGst = pgSaleGst.add(pgSaleGstObj).setScale(2, RoundingMode.HALF_DOWN);
					acquirerSaleGst = acquirerSaleGst.add(acquirerSaleGstObj).setScale(2, RoundingMode.HALF_DOWN);

					if (transactionCountSearchObj.getPaymentMethod()
							.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())) {
						ccSettledCount++;
					} else if (transactionCountSearchObj.getPaymentMethod()
							.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())) {
						dcSettledCount++;
					} else if (transactionCountSearchObj.getPaymentMethod()
							.equalsIgnoreCase(PaymentType.UPI.getCode())) {
						upSettledCount++;
					} else if (transactionCountSearchObj.getPaymentMethod()
							.equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
						nbSettledCount++;
					} else if (transactionCountSearchObj.getPaymentMethod()
							.equalsIgnoreCase(PaymentType.EMI.getCode())) {
						emSettledCount++;
					} else if (transactionCountSearchObj.getPaymentMethod()
							.equalsIgnoreCase(PaymentType.WALLET.getCode())) {
						wlSettledCount++;
					} else if (transactionCountSearchObj.getPaymentMethod()
							.equalsIgnoreCase(PaymentType.COD.getCode())) {
						cdSettledCount++;
					}
					// totalMerchantAmount = totalMerchantAmount.add(saleSettledAmountObj);

				} else {

					BigDecimal refundSettledAmountObj = new BigDecimal(
							transactionCountSearchObj.getRefundSettledAmount());
					BigDecimal pgRefundSurchargeObj = new BigDecimal(transactionCountSearchObj.getPgRefundSurcharge());
					BigDecimal acquirerRefundSurchargeObj = new BigDecimal(
							transactionCountSearchObj.getAcquirerRefundSurcharge());
					BigDecimal pgRefundGstObj = new BigDecimal(transactionCountSearchObj.getPgRefundGst());
					BigDecimal acquirerRefundGstObj = new BigDecimal(transactionCountSearchObj.getAcquirerRefundGst());

					refundSettledAmount = refundSettledAmount.add(refundSettledAmountObj).setScale(2,
							RoundingMode.HALF_DOWN);
					pgRefundSurcharge = pgRefundSurcharge.add(pgRefundSurchargeObj).setScale(2, RoundingMode.HALF_DOWN);
					acquirerRefundSurcharge = acquirerRefundSurcharge.add(acquirerRefundSurchargeObj).setScale(2,
							RoundingMode.HALF_DOWN);
					pgRefundGst = pgRefundGst.add(pgRefundGstObj).setScale(2, RoundingMode.HALF_DOWN);
					acquirerRefundGst = acquirerRefundGst.add(acquirerRefundGstObj).setScale(2, RoundingMode.HALF_DOWN);

					// totalMerchantAmount = totalMerchantAmount.add(refundSettledAmountObj);

				}
			}

			if (payId.equalsIgnoreCase("ALL")) {
				transactionCountSearch.setMerchantName(payId);
			} else {
				transactionCountSearch.setMerchantName(userDao.findPayId(payId).getBusinessName());
			}

			merchantSaleSettledAmount = saleSettledAmount
					.subtract(pgSaleSurcharge.add(acquirerSaleSurcharge).add(pgSaleGst).add(acquirerSaleGst))
					.setScale(2, RoundingMode.HALF_DOWN);
			merchantRefundSettledAmount = refundSettledAmount
					.subtract(pgRefundSurcharge.add(acquirerRefundSurcharge).add(pgRefundGst).add(acquirerRefundGst))
					.setScale(2, RoundingMode.HALF_DOWN);

			transactionCountSearch.setSaleSettledAmount(String.valueOf(saleSettledAmount));
			transactionCountSearch
					.setPgSaleSurcharge(String.valueOf(pgSaleSurcharge.setScale(2, RoundingMode.HALF_DOWN)));
			transactionCountSearch.setAcquirerSaleSurcharge(
					String.valueOf(acquirerSaleSurcharge.setScale(2, RoundingMode.HALF_DOWN)));
			transactionCountSearch.setPgSaleGst(String.valueOf(pgSaleGst.setScale(2, RoundingMode.HALF_DOWN)));
			transactionCountSearch
					.setAcquirerSaleGst(String.valueOf(acquirerSaleGst.setScale(2, RoundingMode.HALF_DOWN)));

			transactionCountSearch
					.setRefundSettledAmount(String.valueOf(refundSettledAmount.setScale(2, RoundingMode.HALF_DOWN)));
			transactionCountSearch
					.setPgRefundSurcharge(String.valueOf(pgRefundSurcharge.setScale(2, RoundingMode.HALF_DOWN)));
			transactionCountSearch.setAcquirerRefundSurcharge(
					String.valueOf(acquirerRefundSurcharge.setScale(2, RoundingMode.HALF_DOWN)));
			transactionCountSearch.setPgRefundGst(String.valueOf(pgRefundGst.setScale(2, RoundingMode.HALF_DOWN)));
			transactionCountSearch
					.setAcquirerRefundGst(String.valueOf(acquirerRefundGst.setScale(2, RoundingMode.HALF_DOWN)));

			totalMerchantAmount = merchantSaleSettledAmount
					.subtract(merchantRefundSettledAmount.setScale(2, RoundingMode.HALF_DOWN));

			transactionCountSearch.setTotalProfit(String.valueOf(pgSaleSurcharge.add(pgSaleGst)
					.subtract(pgRefundSurcharge).subtract(pgRefundGst).setScale(2, RoundingMode.HALF_DOWN)));

			transactionCountSearch.setTotalMerchantAmount(String.valueOf(totalMerchantAmount));

			transactionCountSearch.setMerchantSaleSettledAmount(
					String.valueOf(merchantSaleSettledAmount.setScale(2, RoundingMode.HALF_DOWN)));
			transactionCountSearch.setMerchantRefundSettledAmount(
					String.valueOf(merchantRefundSettledAmount.setScale(2, RoundingMode.HALF_DOWN)));

			double totalSettleCount = ccSettledCount + dcSettledCount + upSettledCount + nbSettledCount + wlSettledCount
					+ emSettledCount + cdSettledCount;
			double ccSettledTxnPercent = 0;
			double dcSettledTxnPercent = 0;
			double upSettledTxnPercent = 0;
			double nbSettledTxnPercent = 0;
			double wlSettledTxnPercent = 0;
			double emSettledTxnPercent = 0;
			double cdSettledTxnPercent = 0;

			if (totalSettleCount > 0) {
				ccSettledTxnPercent = (ccSettledCount / totalSettleCount) * 100;
				dcSettledTxnPercent = (dcSettledCount / totalSettleCount) * 100;
				upSettledTxnPercent = (upSettledCount / totalSettleCount) * 100;
				nbSettledTxnPercent = (nbSettledCount / totalSettleCount) * 100;
				wlSettledTxnPercent = (wlSettledCount / totalSettleCount) * 100;
				emSettledTxnPercent = (emSettledCount / totalSettleCount) * 100;
				cdSettledTxnPercent = (cdSettledCount / totalSettleCount) * 100;
			}

			BigDecimal avgSettledAmount = new BigDecimal(0);

			if (totalSettleCount > 0) {
				BigDecimal totalSettleCountBD = new BigDecimal(totalSettleCount).setScale(2, RoundingMode.HALF_DOWN);
				avgSettledAmount = saleSettledAmount.divide(totalSettleCountBD, 2, RoundingMode.HALF_UP);
				avgSettledAmount = avgSettledAmount.setScale(2, RoundingMode.HALF_DOWN);
			} else {
				avgSettledAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			}

			transactionCountSearch.setCcSettledPercentage(String.format("%.2f", ccSettledTxnPercent));
			transactionCountSearch.setDcSettledPercentage(String.format("%.2f", dcSettledTxnPercent));
			transactionCountSearch.setUpSettledPercentage(String.format("%.2f", upSettledTxnPercent));
			transactionCountSearch.setNbSettledPercentage(String.format("%.2f", nbSettledTxnPercent));
			transactionCountSearch.setWlSettledPercentage(String.format("%.2f", wlSettledTxnPercent));
			transactionCountSearch.setEmSettledPercentage(String.format("%.2f", emSettledTxnPercent));
			transactionCountSearch.setCdSettledPercentage(String.format("%.2f", cdSettledTxnPercent));
			transactionCountSearch.setAvgSettlementAmount(String.format("%.2f", avgSettledAmount));

			List<BasicDBObject> deltaConditionsList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> deltaConditionList = new ArrayList<BasicDBObject>();
			deltaConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			deltaConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.RECONCILED.getName()));
			deltaConditionList.add(new BasicDBObject(FieldType.UDF6.getName(), Constants.Y.getValue()));

			BasicDBObject deltaSettledQuery = new BasicDBObject("$and", deltaConditionList);

			deltaConditionsList.add(deltaSettledQuery);
			BasicDBObject deltaConditionQuery = new BasicDBObject("$or", deltaConditionsList);

			List<BasicDBObject> allConditionQueryList1 = new ArrayList<BasicDBObject>();

			if (!acquirerQuery.isEmpty()) {
				allConditionQueryList1.add(acquirerQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList1.add(dateQuery);
			}
			allConditionQueryList1.add(deltaConditionQuery);

			BasicDBObject allConditionQueryObj1 = new BasicDBObject("$and", allConditionQueryList1);

			List<BasicDBObject> finalList1 = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				finalList1.add(allParamQuery);
			}
			if (!allConditionQueryObj1.isEmpty()) {
				finalList1.add(allConditionQueryObj1);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			BasicDBObject finalQuery1 = new BasicDBObject("$and", finalList1);

			double totalDelta = (double) coll.count(finalQuery1);

			MongoCursor<Document> cursor = coll.find(finalQuery1).iterator();

			// Remove all data from an earlier map
			while (cursor.hasNext()) {

				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
					totalSettledAmountDelta = totalSettledAmountDelta
							.add(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString())));
				}
			}

			totalSettledAmountActual = saleSettledAmount.subtract(totalSettledAmountDelta);

			String totalSettledAmountActualString = String
					.valueOf(totalSettledAmountActual.setScale(2, RoundingMode.HALF_DOWN));

			transactionCountSearch.setPostSettledTransactionCount(String.valueOf(totalDelta));
			transactionCountSearch.setActualSettlementAmount(totalSettledAmountActualString);

			return transactionCountSearch;
		}

		catch (Exception e) {
			logger.error("Exception in transaction summary count service ", e);
		}
		return null;
	}

	@SuppressWarnings("unlikely-arg-type")
	public TransactionCountSearch findDetails(Document dbobj) {
		BigDecimal merchantGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal acquirerGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal pgGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
		TdrPojo tdrPojo = new TdrPojo();
		BigDecimal st = null;
		String bussinessType = "";
		String bussinessName = "";
		String payId = (dbobj.getString(FieldType.PAY_ID.toString()));

		if (!StringUtils.isBlank(payId)) {
			User user = new User();
			if (userMap.get(payId) != null) {
				user = userMap.get(payId);
			} else {
				user = userDao.findPayId(payId);
				userMap.put(payId, user);
			}
			
			UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(payId);

			if (StringUtils.isNotBlank(dbobj.getString(FieldType.POST_SETTLED_FLAG.toString()))) {
				if (dbobj.getString(FieldType.POST_SETTLED_FLAG.toString()).equalsIgnoreCase("Y")) {
					postSettledTransactionCount++;
				}
			}
			String amount = "0.00";
			String totalAmount = "0.00";
			if(StringUtils.isNotBlank(dbobj.getString(FieldType.AMOUNT.toString()))) {
				amount = dbobj.getString(FieldType.AMOUNT.toString());
			}
			if(StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
				totalAmount = dbobj.getString(FieldType.TOTAL_AMOUNT.toString());
			}
			bussinessType = user.getIndustryCategory();
			bussinessName = user.getBusinessName();

			st = new BigDecimal(PropertiesManager.propertiesMap.get("SERVICE_TAX"));
			st = st.setScale(2, RoundingMode.HALF_DOWN);
			if (merchantSettings.isSurchargeFlag()) {
				// Surcharge
				PaymentType paymentType = PaymentType
						.getInstanceUsingCode(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));

				if (paymentType == null) {
					return null;
				}

				AcquirerType acquirerType = AcquirerType
						.getInstancefromCode(dbobj.getString(FieldType.ACQUIRER_TYPE.toString()));

				if (acquirerType == null) {
					return null;
				}

				MopType mopType = MopType.getmop(dbobj.getString(FieldType.MOP_TYPE.toString()));

				if (mopType == null) {
					return null;
				}

				String paymentsRegion = (dbobj.getString(FieldType.PAYMENTS_REGION.toString()));

				if (paymentsRegion == null) {
					paymentsRegion = AccountCurrencyRegion.DOMESTIC.toString();
				}

				String txnAmount = amount;
				String surchargeAmount = totalAmount;

				BigDecimal netAmount = new BigDecimal(txnAmount);
				BigDecimal netsurchargeAmount = new BigDecimal(surchargeAmount);

				BigDecimal netcalculatedSurcharge = netsurchargeAmount.subtract(netAmount);
				netcalculatedSurcharge = netcalculatedSurcharge.setScale(2, RoundingMode.HALF_DOWN);
				
				BigDecimal gstCalculate = new BigDecimal("0.00");
				if(!paymentType.getName().equals("NEFT") && !paymentType.getName().equals("IMPS") && !paymentType.getName().equals("RTGS")) {
					
					if(StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))){
						gstCalculate = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
							.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.toString())));
					}
				}
				BigDecimal pgSurchargeAmount = new BigDecimal("0.00");
				BigDecimal acquirerSurchargeAmount = new BigDecimal("0.00");

				if (netcalculatedSurcharge.equals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN))) {
					pgSurchargeAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
					acquirerSurchargeAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
				}

				else {
					if(!paymentType.getName().equals("NEFT") && !paymentType.getName().equals("IMPS") && !paymentType.getName().equals("RTGS")) {
						
						if(StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))){
								acquirerSurchargeAmount = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()));
								pgSurchargeAmount = new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.toString()));
								pgSurchargeAmount = pgSurchargeAmount.setScale(2, RoundingMode.HALF_DOWN);
						}
						
					}
				}
				BigDecimal totalSurcharge = new BigDecimal("0.00");;
				BigDecimal totalAmtPaytoMerchant = new BigDecimal("0.00");;
				if(!paymentType.getName().equals("NEFT") && !paymentType.getName().equals("IMPS") && !paymentType.getName().equals("RTGS")) {
					
					if(StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))){
						
						totalSurcharge = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.toString())));
						totalAmtPaytoMerchant = netsurchargeAmount.subtract(gstCalculate.add(totalSurcharge));
		
						acquirerGstAmount = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.toString()));
						pgGstAmount = new BigDecimal(dbobj.getString(FieldType.PG_GST.toString()));
						merchantGstAmount = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.toString())));
						merchantGstAmount.setScale(2, RoundingMode.HALF_DOWN);
						acquirerGstAmount.setScale(2, RoundingMode.HALF_DOWN);
						pgGstAmount.setScale(2, RoundingMode.HALF_DOWN);
					}
					
				}
				String gstCalculateString = String.valueOf(gstCalculate);
				String totalSurchargeString = String.valueOf(totalSurcharge);
				String totalAmtPaytoMerchantString = String.valueOf(totalAmtPaytoMerchant);
				tdrPojo.setTotalAmtPaytoMerchant(totalAmtPaytoMerchantString);
				tdrPojo.setTotalGstOnMerchant(gstCalculateString);
				tdrPojo.setNetMerchantPayableAmount(totalAmtPaytoMerchantString);
				tdrPojo.setMerchantTdrCalculate(totalSurchargeString);
				tdrPojo.setTotalAmount(surchargeAmount);
				tdrPojo.setAcquirerSurchargeAmount(String.valueOf(acquirerSurchargeAmount));
				tdrPojo.setPgSurchargeAmount(String.valueOf(pgSurchargeAmount));

				TransactionCountSearch transactionCountSearchObj = new TransactionCountSearch();

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString()))
						&& (dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase(TransactionType.RECO.getName())
						|| dbobj.getString(FieldType.TXNTYPE.toString())
								.equalsIgnoreCase(TransactionType.SALE.getName()))) {
					transactionCountSearchObj.setPaymentMethod(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
					transactionCountSearchObj.setTxnType(TransactionType.SALE.getName());
					transactionCountSearchObj.setSaleSettledAmount(tdrPojo.getTotalAmount());
					transactionCountSearchObj.setPgSaleSurcharge(tdrPojo.getPgSurchargeAmount());
					transactionCountSearchObj.setAcquirerSaleSurcharge(tdrPojo.getAcquirerSurchargeAmount());
					transactionCountSearchObj.setPgSaleGst(String.valueOf(pgGstAmount));
					transactionCountSearchObj.setAcquirerSaleGst(String.valueOf(acquirerGstAmount));
				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString()))
						&& (dbobj.getString(FieldType.TXNTYPE.toString())
						.equalsIgnoreCase(TransactionType.REFUNDRECO.getName())
						|| dbobj.getString(FieldType.TXNTYPE.toString())
								.equalsIgnoreCase(TransactionType.REFUND.getName()))) {
					{
						transactionCountSearchObj.setPaymentMethod(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
						transactionCountSearchObj.setTxnType(TransactionType.REFUND.getName());
						transactionCountSearchObj.setRefundSettledAmount(tdrPojo.getTotalAmount());
						transactionCountSearchObj.setPgRefundSurcharge(tdrPojo.getPgSurchargeAmount());
						transactionCountSearchObj.setAcquirerRefundSurcharge(tdrPojo.getAcquirerSurchargeAmount());
						transactionCountSearchObj.setPgRefundGst(String.valueOf(pgGstAmount));
						transactionCountSearchObj.setAcquirerRefundGst(String.valueOf(acquirerGstAmount));
					}
				}
				return transactionCountSearchObj;
			} else {
				// TDR

				PaymentType paymentType = PaymentType
						.getInstanceUsingCode(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
				if (paymentType == null) {
					return null;
				}

				AcquirerType acquirerType = AcquirerType
						.getInstancefromCode(dbobj.getString(FieldType.ACQUIRER_TYPE.toString()));
				if (acquirerType == null) {
					return null;
				}

				MopType mopType = MopType.getmop(dbobj.getString(FieldType.MOP_TYPE.toString()));
				if (mopType == null) {
					return null;
				}

				String paymentsRegion = (dbobj.getString(FieldType.PAYMENTS_REGION.toString()));
				if (paymentsRegion == null) {
					paymentsRegion = AccountCurrencyRegion.DOMESTIC.toString();
				}

				String txnAmount = totalAmount;

				BigDecimal netAmount = new BigDecimal(txnAmount);
				BigDecimal netcalculatedTdr = new BigDecimal("0.00");
				BigDecimal gstCalculate = new BigDecimal("0.00");
				if(!paymentType.getName().equals("NEFT") && !paymentType.getName().equals("IMPS") && !paymentType.getName().equals("RTGS")) {
					
					if(dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))) {
						
						netcalculatedTdr = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.toString())))
								.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName())));
						
						netcalculatedTdr = netcalculatedTdr.setScale(2, RoundingMode.HALF_DOWN);
						
						
					} else if(StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))){
					
					netcalculatedTdr = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
							.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.toString())));
					netcalculatedTdr = netcalculatedTdr.setScale(2, RoundingMode.HALF_DOWN);
					
					}
					
					if(dbobj.containsKey(FieldType.RESELLER_GST.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {
						
						gstCalculate = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.toString())))
								.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.toString())));
						
					} else if(StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))){
					
					gstCalculate = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
							.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.toString())));
					}
				}

				BigDecimal pgTdrAmount = new BigDecimal("0.0");
				BigDecimal acquirerTdrAmount = new BigDecimal("0.0");

				if (netcalculatedTdr.equals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN))) {
					pgTdrAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
					acquirerTdrAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
				} else {
					
					if(StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))){
						
						acquirerTdrAmount = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()));
						acquirerTdrAmount = acquirerTdrAmount.setScale(2, RoundingMode.HALF_DOWN);
					}
					
					if(dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))) {
						
						pgTdrAmount = new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.toString()))
								.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName())));
						pgTdrAmount = pgTdrAmount.setScale(2, RoundingMode.HALF_DOWN);
					} else {
						if(StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))){
							pgTdrAmount = new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.toString()));
							pgTdrAmount = pgTdrAmount.setScale(2, RoundingMode.HALF_DOWN);
						}
						
					}
				}
				
				BigDecimal totalTdr = new BigDecimal("0.00");
				BigDecimal totalAmtPaytoMerchant = new BigDecimal("0.00");
				if(!paymentType.getName().equals("NEFT") && !paymentType.getName().equals("IMPS") && !paymentType.getName().equals("RTGS")) {
					
					if(dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))) {
						
						totalTdr = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.toString())))
								.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.toString())));
						totalAmtPaytoMerchant = netAmount.subtract(gstCalculate.add(totalTdr));
						
					} else {
						if(StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))){

							totalTdr = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
									.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.toString())));
							totalAmtPaytoMerchant = netAmount.subtract(gstCalculate.add(totalTdr));
						}
						
						
					}
					if(StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
						acquirerGstAmount = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.toString()));
					}
					
					if(dbobj.containsKey(FieldType.RESELLER_GST.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {
						
						pgGstAmount = new BigDecimal(dbobj.getString(FieldType.PG_GST.toString()))
								.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())));
						
					} else {
						if(StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName())))
							pgGstAmount = new BigDecimal(dbobj.getString(FieldType.PG_GST.toString()));
					}
					
					if(dbobj.containsKey(FieldType.RESELLER_GST.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {
						
						merchantGstAmount = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.toString())))
										.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())));
					} else {
						if(StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {
						merchantGstAmount = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.toString())));
						}
					}
					
					merchantGstAmount.setScale(2, RoundingMode.HALF_DOWN);
					pgGstAmount.setScale(2, RoundingMode.HALF_DOWN);
					acquirerGstAmount.setScale(2, RoundingMode.HALF_DOWN);
				}
				
				String gstCalculateString = String.valueOf(gstCalculate);
				String totalSurchargeString = String.valueOf(totalTdr);
				String totalAmtPaytoMerchantString = String.valueOf(totalAmtPaytoMerchant);
				tdrPojo.setTotalAmtPaytoMerchant(totalAmtPaytoMerchantString);
				tdrPojo.setTotalGstOnMerchant(gstCalculateString);
				tdrPojo.setNetMerchantPayableAmount(totalAmtPaytoMerchantString);
				tdrPojo.setMerchantTdrCalculate(totalSurchargeString);
				tdrPojo.setTotalAmount(txnAmount);
				tdrPojo.setAcquirerSurchargeAmount(String.valueOf(acquirerTdrAmount));
				tdrPojo.setPgSurchargeAmount(String.valueOf(pgTdrAmount));

				TransactionCountSearch transactionCountSearchObj = new TransactionCountSearch();

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString()))
						&& (dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase(TransactionType.RECO.getName())
						|| dbobj.getString(FieldType.TXNTYPE.toString())
								.equalsIgnoreCase(TransactionType.SALE.getName()))) {
					transactionCountSearchObj.setPaymentMethod(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
					transactionCountSearchObj.setTxnType(TransactionType.SALE.getName());
					transactionCountSearchObj.setSaleSettledAmount(tdrPojo.getTotalAmount());
					transactionCountSearchObj.setPgSaleSurcharge(tdrPojo.getPgSurchargeAmount());
					transactionCountSearchObj.setAcquirerSaleSurcharge(tdrPojo.getAcquirerSurchargeAmount());
					transactionCountSearchObj.setPgSaleGst(String.valueOf(pgGstAmount));
					transactionCountSearchObj.setAcquirerSaleGst(String.valueOf(acquirerGstAmount));
				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString()))
						&& (dbobj.getString(FieldType.TXNTYPE.toString())
						.equalsIgnoreCase(TransactionType.REFUNDRECO.getName())
						|| dbobj.getString(FieldType.TXNTYPE.toString())
								.equalsIgnoreCase(TransactionType.REFUND.getName()))) {
					{
						transactionCountSearchObj.setPaymentMethod(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
						transactionCountSearchObj.setTxnType(TransactionType.REFUND.getName());
						transactionCountSearchObj.setRefundSettledAmount(tdrPojo.getTotalAmount());
						transactionCountSearchObj.setPgRefundSurcharge(tdrPojo.getPgSurchargeAmount());
						transactionCountSearchObj.setAcquirerRefundSurcharge(tdrPojo.getAcquirerSurchargeAmount());
						transactionCountSearchObj.setPgRefundGst(String.valueOf(pgGstAmount));
						transactionCountSearchObj.setAcquirerRefundGst(String.valueOf(acquirerGstAmount));
					}
				}
				return transactionCountSearchObj;
			}
		}
		return null;
	}
}