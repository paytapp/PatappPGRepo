package com.paymentgateway.notification.sms.smsCreater;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.MerchantDailySMSObject;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDao;
import com.paymentgateway.commons.user.TransactionCountSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;

@Service
public class TransactionSummaryCountServiceSMS {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userDao;

	@Autowired
	private SurchargeDao surchargeDao;

	private static Logger logger = LoggerFactory.getLogger(TransactionSummaryCountServiceSMS.class.getName());
	private static final String prefix = "MONGO_DB_";
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
	// Map<String, List<Surcharge>> surchargeMap = new HashMap<String,
	// List<Surcharge>>();
	Map<String, User> userMap = new HashMap<String, User>();
	private double postSettledTransactionCount = 0;
	List<Surcharge> surchargeList = new ArrayList<Surcharge>();

	public TransactionCountSearch getTransactionCount(String fromDate, String toDate, String payId, String paymentType,
			String acquirer, User user1, int start, int length, String paymentsRegion, String cardHolderType,
			String mopType, String transactionType) {

		List<TransactionCountSearch> transactionCountSearchList = new ArrayList<TransactionCountSearch>();
		List<String> txnTypeList = new ArrayList<String>();
		txnTypeList.add(TxnType.SALE.getName());
		txnTypeList.add(TxnType.REFUND.getName());

		try {

			surchargeList.clear();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
			Date date1 = format.parse(fromDate);
			Date date2 = format.parse(toDate);

			surchargeList = surchargeDao.findAllSurchargeByDate(date1, date2);
			postSettledTransactionCount = 0;
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
			saleSettledConditionList
					.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));

			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleSettledConditionList);
			saleSettledList.add(saleConditionQuery);

			// REFUND Settled query
			List<BasicDBObject> refundSettledConditionList = new ArrayList<BasicDBObject>();
			refundSettledConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
			refundSettledConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			refundSettledConditionList
					.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));

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

			for (String txnType : txnTypeList) {

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
				if (!dateQuery.isEmpty()) {
					allConditionQueryList.add(dateQuery);
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
						PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

				int totalSettled = (int) coll.count(finalQuery);
				// TODO remove delta count
				if (txnType.equalsIgnoreCase(TransactionType.SALE.getName())) {

					transactionCountSearch.setSaleSettledCount(String.valueOf(totalSettled));
				} else {
					transactionCountSearch.setRefundSettledCount(String.valueOf(totalSettled));
				}
				MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

				// Remove all data from an earlier map
				while (cursor.hasNext()) {
					Document dbobj = cursor.next();

					TransactionCountSearch transactionCountSearchObj = new TransactionCountSearch();
					transactionCountSearchObj = findDetails(dbobj);
					transactionCountSearchObj.setSaleSettledCount(transactionCountSearch.getSaleSettledCount());
					transactionCountSearchObj.setRefundSettledCount(transactionCountSearch.getRefundSettledCount());
					transactionCountSearchList.add(transactionCountSearchObj);

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
			for (TransactionCountSearch transactionCountSearchObj : transactionCountSearchList) {

				if (transactionCountSearchObj.getTxnType().equalsIgnoreCase(TransactionType.SALE.getName())) {

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

			double totalSettleCount = ccSettledCount + dcSettledCount + upSettledCount;
			double ccSettledTxnPercent = 0;
			double dcSettledTxnPercent = 0;
			double upSettledTxnPercent = 0;

			if (totalSettleCount > 0) {
				ccSettledTxnPercent = (ccSettledCount / totalSettleCount) * 100;
				dcSettledTxnPercent = (dcSettledCount / totalSettleCount) * 100;
				upSettledTxnPercent = (upSettledCount / totalSettleCount) * 100;
			}

			BigDecimal avgSettledAmount = new BigDecimal(0);

			if (totalSettleCount > 0) {

				BigDecimal totalSettleCountBD = new BigDecimal(totalSettleCount).setScale(2, RoundingMode.HALF_DOWN);
				;
				avgSettledAmount = saleSettledAmount.divide(totalSettleCountBD, 2, RoundingMode.HALF_UP);
				avgSettledAmount = avgSettledAmount.setScale(2, RoundingMode.HALF_DOWN);
			} else {
				avgSettledAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			}

			transactionCountSearch.setCcSettledPercentage(String.format("%.2f", ccSettledTxnPercent));
			transactionCountSearch.setDcSettledPercentage(String.format("%.2f", dcSettledTxnPercent));
			transactionCountSearch.setUpSettledPercentage(String.format("%.2f", upSettledTxnPercent));
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
			logger.error("Exception in transaction summary count service " , e);
		}
		return null;
	}

	public TransactionCountSearch findDetails(Document dbobj) {

		// logger.info("Inside , findDetails , ");

		BigDecimal merchantGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal acquirerGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
		TdrPojo tdrPojo = new TdrPojo();
		BigDecimal st = null;
		String bussinessType = null;
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

			if (StringUtils.isNotBlank(dbobj.getString(FieldType.POST_SETTLED_FLAG.toString()))) {
				if (dbobj.getString(FieldType.POST_SETTLED_FLAG.toString()).equalsIgnoreCase("Y")) {
					postSettledTransactionCount++;
				}
			}

			String amount = (dbobj.getString(FieldType.AMOUNT.toString()));
			String totalAmount = (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
			bussinessType = user.getIndustryCategory();
			bussinessName = user.getBusinessName();
			st = new BigDecimal(PropertiesManager.propertiesMap.get("SERVICE_TAX"));
			st = st.setScale(2, RoundingMode.HALF_DOWN);

			if (!StringUtils.isBlank(dbobj.getString(FieldType.SURCHARGE_FLAG.toString()))) {

				if (dbobj.getString(FieldType.SURCHARGE_FLAG.toString()).equals("Y")) {

					String txnAmount = amount;
					String surchargeAmount = totalAmount;
					BigDecimal nettxnAmount = new BigDecimal(txnAmount);

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

					StringBuilder surchargeIdentifier = new StringBuilder();
					surchargeIdentifier.append(payId);
					surchargeIdentifier.append(paymentType.getName());
					surchargeIdentifier.append(acquirerType.getName());
					surchargeIdentifier.append(mopType.getName());
					surchargeIdentifier.append(paymentsRegion);

					Date surchargeStartDate = null;
					Date surchargeEndDate = null;
					Date settlementDate = null;
					Surcharge surcharge = new Surcharge();
					String transactionRegion = null;
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
						transactionRegion = dbobj.getString(FieldType.PAYMENTS_REGION.toString());
					} else {
						transactionRegion = AccountCurrencyRegion.DOMESTIC.name();
					}
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
					try {
						for (Surcharge surchargeData : surchargeList) {

							if (AcquirerType.getInstancefromName(surchargeData.getAcquirerName()).toString()
									.equalsIgnoreCase(dbobj.getString(FieldType.ACQUIRER_TYPE.toString()))
									&& surchargeData.getPaymentType().getCode()
											.equalsIgnoreCase(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
									&& surchargeData.getMopType().getCode()
											.equalsIgnoreCase(dbobj.getString(FieldType.MOP_TYPE.toString()))
									&& surchargeData.getPaymentsRegion().name().equalsIgnoreCase(transactionRegion)
									&& surchargeData.getPayId()
											.equalsIgnoreCase(dbobj.getString(FieldType.PAY_ID.toString()))) {

								surchargeStartDate = format.parse(surchargeData.getCreatedDate().toString());
								surchargeEndDate = format.parse(surchargeData.getUpdatedDate().toString());
								if (surchargeStartDate.compareTo(surchargeEndDate) == 0) {
									surchargeEndDate = new Date();
								}

								settlementDate = format.parse(dbobj.getString(FieldType.CREATE_DATE.toString()));

								if (settlementDate.compareTo(surchargeStartDate) >= 0
										&& settlementDate.compareTo(surchargeEndDate) <= 0) {
									surcharge = surchargeData;
									break;
								} else {
									continue;
								}

							}
						}
					} catch (Exception e) {
						logger.error("Exception " , e);
					}
					BigDecimal bankSurchargeFC;
					BigDecimal bankSurchargePercent;

					if (StringUtils.isBlank(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {

						bankSurchargeFC = surcharge.getBankSurchargeAmountCustomer();
						bankSurchargePercent = surcharge.getBankSurchargePercentageCustomer();
					}

					else if ((dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()))
							.equalsIgnoreCase(CardHolderType.CONSUMER.toString())) {

						bankSurchargeFC = surcharge.getBankSurchargeAmountCustomer();
						bankSurchargePercent = surcharge.getBankSurchargePercentageCustomer();
					} else {

						bankSurchargeFC = surcharge.getBankSurchargeAmountCommercial();
						bankSurchargePercent = surcharge.getBankSurchargePercentageCommercial();
					}

					BigDecimal netsurchargeAmount = new BigDecimal(surchargeAmount);

					BigDecimal netcalculatedSurcharge = netsurchargeAmount.subtract(nettxnAmount);
					netcalculatedSurcharge = netcalculatedSurcharge.setScale(2, RoundingMode.HALF_DOWN);

					BigDecimal gstCalculate = netcalculatedSurcharge.multiply(st).divide(((ONE_HUNDRED).add(st)), 2,
							RoundingMode.HALF_DOWN);

					BigDecimal pgSurchargeAmount;
					BigDecimal acquirerSurchargeAmount;

					if (netcalculatedSurcharge.equals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN))) {
						pgSurchargeAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
						acquirerSurchargeAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
					}

					else {

						acquirerSurchargeAmount = nettxnAmount.multiply(bankSurchargePercent).divide(((ONE_HUNDRED)), 2,
								RoundingMode.HALF_DOWN);
						acquirerSurchargeAmount = acquirerSurchargeAmount.add(bankSurchargeFC);

						pgSurchargeAmount = netcalculatedSurcharge.subtract(acquirerSurchargeAmount);
						pgSurchargeAmount = pgSurchargeAmount.subtract(gstCalculate);
						pgSurchargeAmount = pgSurchargeAmount.setScale(2, RoundingMode.HALF_DOWN);

					}

					BigDecimal totalSurcharge = netcalculatedSurcharge.subtract(gstCalculate);
					BigDecimal totalAmtPaytoMerchant = netsurchargeAmount.subtract(gstCalculate.add(totalSurcharge));

					acquirerGstAmount = acquirerSurchargeAmount.multiply(st).divide(ONE_HUNDRED, 2,
							RoundingMode.HALF_DOWN);

					merchantGstAmount = pgSurchargeAmount.multiply(st).divide(ONE_HUNDRED, 2, RoundingMode.HALF_DOWN);

					String gstCalculateString = String.valueOf(gstCalculate);
					String totalSurchargeString = String.valueOf(totalSurcharge);
					String totalAmtPaytoMerchantString = String.valueOf(totalAmtPaytoMerchant);
					tdrPojo.setTotalAmtPaytoMerchant(totalAmtPaytoMerchantString);
					tdrPojo.setTotalGstOnMerchant(gstCalculateString);
					tdrPojo.setNetMerchantPayableAmount(totalAmtPaytoMerchantString);
					tdrPojo.setMerchantTdrCalculate(totalSurchargeString);
					tdrPojo.setTotalAmount(surchargeAmount);
					tdrPojo.setAcquirerSurchargeAmount(String.valueOf(acquirerSurchargeAmount));
					tdrPojo.setBankSurchargeAmount(String.valueOf(pgSurchargeAmount));

					TransactionCountSearch transactionCountSearchObj = new TransactionCountSearch();

					if (dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase(TransactionType.RECO.getName())
							|| dbobj.getString(FieldType.TXNTYPE.toString())
									.equalsIgnoreCase(TransactionType.SALE.getName())) {

						transactionCountSearchObj.setPaymentMethod(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
						transactionCountSearchObj.setTxnType(TransactionType.SALE.getName());
						transactionCountSearchObj.setSaleSettledAmount(tdrPojo.getTotalAmount());
						transactionCountSearchObj.setPgSaleSurcharge(tdrPojo.getBankSurchargeAmount());
						transactionCountSearchObj.setAcquirerSaleSurcharge(tdrPojo.getAcquirerSurchargeAmount());
						transactionCountSearchObj.setPgSaleGst(String.valueOf(merchantGstAmount));
						transactionCountSearchObj.setAcquirerSaleGst(String.valueOf(acquirerGstAmount));
					} else if (dbobj.getString(FieldType.TXNTYPE.toString())
							.equalsIgnoreCase(TransactionType.REFUNDRECO.getName())
							|| dbobj.getString(FieldType.TXNTYPE.toString())
									.equalsIgnoreCase(TransactionType.REFUND.getName())) {
						{

							transactionCountSearchObj
									.setPaymentMethod(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
							transactionCountSearchObj.setTxnType(TransactionType.REFUND.getName());
							transactionCountSearchObj.setRefundSettledAmount(tdrPojo.getTotalAmount());
							transactionCountSearchObj.setPgRefundSurcharge(tdrPojo.getBankSurchargeAmount());
							transactionCountSearchObj.setAcquirerRefundSurcharge(tdrPojo.getAcquirerSurchargeAmount());
							transactionCountSearchObj.setPgRefundGst(String.valueOf(merchantGstAmount));
							transactionCountSearchObj.setAcquirerRefundGst(String.valueOf(acquirerGstAmount));
						}

					}

					return transactionCountSearchObj;
				}
			}

			else {
				// Get TDR Mode report values here
			}

		}
		return null;
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

	public MerchantDailySMSObject getMerchantSMSDailyData(String fromDate, String toDate, String payId,
			String paymentType, String acquirer, User user, int start, int length, String paymentsRegion,
			String cardHolderType, String mopType, String transactionType) {

		logger.info("Inside TransactionSummaryCountService , getMerchantSMSData ");

		userMap.clear();

		MerchantDailySMSObject merchantSMSObject = new MerchantDailySMSObject();

		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject allParamQuery = new BasicDBObject();
			List<BasicDBObject> saleSettledList = new ArrayList<BasicDBObject>();

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
				dateIndexConditionList.add(new BasicDBObject("DATE_INDEX", dateIndex));
			}

			if (!payId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			// SALE Captured query
			List<BasicDBObject> saleSettledConditionList = new ArrayList<BasicDBObject>();
			saleSettledConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleSettledConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleSettledConditionList);
			saleSettledList.add(saleConditionQuery);

			BasicDBObject saleSettledConditionQuery = new BasicDBObject("$or", saleSettledList);

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}

			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryList.add(dateIndexConditionQuery);
			}

			allConditionQueryList.add(saleSettledConditionQuery);

			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			BasicDBObject finalQuery = new BasicDBObject("$and", fianlList);
			logger.info("getMerchantSMSData , finalQuery = " + finalQuery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			int ccCapturedCount = 0;
			int dcCapturedCount = 0;
			int upCapturedCount = 0;
			int nbCapturedCount = 0;
			int wlCapturedCount = 0;
			int emCapturedCount = 0;
			int cdCapturedCount = 0;

			BigDecimal acquirerTdrSc = BigDecimal.ZERO;
			BigDecimal acquirerGst = BigDecimal.ZERO;
			BigDecimal pgTdrSc = BigDecimal.ZERO;
			BigDecimal pgGst = BigDecimal.ZERO;

			BigDecimal ccTotalComm = BigDecimal.ZERO;
			BigDecimal ccMerchantComm = BigDecimal.ZERO;
			BigDecimal ccBankComm = BigDecimal.ZERO;
			BigDecimal ccPgComm = BigDecimal.ZERO;

			BigDecimal dcTotalComm = BigDecimal.ZERO;
			BigDecimal dcMerchantComm = BigDecimal.ZERO;
			BigDecimal dcBankComm = BigDecimal.ZERO;
			BigDecimal dcPgComm = BigDecimal.ZERO;

			BigDecimal upTotalComm = BigDecimal.ZERO;
			BigDecimal upMerchantComm = BigDecimal.ZERO;
			BigDecimal upBankComm = BigDecimal.ZERO;
			BigDecimal upPgComm = BigDecimal.ZERO;

			BigDecimal emTotalComm = BigDecimal.ZERO;
			BigDecimal emMerchantComm = BigDecimal.ZERO;
			BigDecimal emBankComm = BigDecimal.ZERO;
			BigDecimal emPgComm = BigDecimal.ZERO;

			BigDecimal wlTotalComm = BigDecimal.ZERO;
			BigDecimal wlMerchantComm = BigDecimal.ZERO;
			BigDecimal wlBankComm = BigDecimal.ZERO;
			BigDecimal wlPgComm = BigDecimal.ZERO;

			BigDecimal cdTotalComm = BigDecimal.ZERO;
			BigDecimal cdMerchantComm = BigDecimal.ZERO;
			BigDecimal cdBankComm = BigDecimal.ZERO;
			BigDecimal cdPgComm = BigDecimal.ZERO;

			BigDecimal nbTotalComm = BigDecimal.ZERO;
			BigDecimal nbMerchantComm = BigDecimal.ZERO;
			BigDecimal nbBankComm = BigDecimal.ZERO;
			BigDecimal nbPgComm = BigDecimal.ZERO;

			BigDecimal totalAmount = BigDecimal.ZERO;

			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();
			logger.info("getMerchantSMSData , query execution completed");
			while (cursor.hasNext()) {

				Document dbobj = cursor.next();

				if (userMap.get(payId) != null && !userMap.get(payId).getPayId().equals("")) {
					user = userMap.get(payId);
				} else {
					user = userDao.findPayId(payId);
					userMap.put(payId, user);
				}

				if (user == null) {
					logger.info("User not found for txn Id " + dbobj.getString(FieldType.TXN_ID.getName()));
					continue;
				}

				Date settlementDate = null;

				if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
						.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())) {

					ccCapturedCount++;
					totalAmount = totalAmount.add(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())));

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))) {
						acquirerTdrSc = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {
						acquirerGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {
						pgGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))) {
						pgTdrSc = new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName()));
					}

					BigDecimal ccComm = acquirerTdrSc.add(acquirerGst).add(pgGst).add(pgTdrSc);
					BigDecimal bankCCComm = acquirerTdrSc.add(acquirerGst);
					BigDecimal pgCCComm = pgGst.add(pgTdrSc);

					ccTotalComm = ccTotalComm.add(ccComm);
					ccBankComm = ccBankComm.add(bankCCComm);
					ccPgComm = ccPgComm.add(pgCCComm);
					ccMerchantComm = ccMerchantComm.add(totalAmount.subtract(ccComm));

				} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
						.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())) {

					dcCapturedCount++;
					totalAmount = totalAmount.add(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())));

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))) {
						acquirerTdrSc = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {
						acquirerGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {
						pgGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))) {
						pgTdrSc = new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName()));
					}

					BigDecimal dcComm = acquirerTdrSc.add(acquirerGst).add(pgGst).add(pgTdrSc);
					BigDecimal bankdcComm = acquirerTdrSc.add(acquirerGst);
					BigDecimal pgdcComm = pgGst.add(pgTdrSc);

					dcTotalComm = dcTotalComm.add(dcComm);
					dcBankComm = dcBankComm.add(bankdcComm);
					dcPgComm = dcPgComm.add(pgdcComm);
					dcMerchantComm = dcMerchantComm.add(totalAmount.subtract(dcComm));

				} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
						.equalsIgnoreCase(PaymentType.UPI.getCode())) {

					upCapturedCount++;
					totalAmount = totalAmount.add(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())));

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))) {
						acquirerTdrSc = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {
						acquirerGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {
						pgGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))) {
						pgTdrSc = new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName()));
					}

					BigDecimal upComm = acquirerTdrSc.add(acquirerGst).add(pgGst).add(pgTdrSc);
					BigDecimal bankupComm = acquirerTdrSc.add(acquirerGst);
					BigDecimal pgupComm = pgGst.add(pgTdrSc);

					upTotalComm = upTotalComm.add(upComm);
					upBankComm = upBankComm.add(bankupComm);
					upPgComm = upPgComm.add(pgupComm);
					upMerchantComm = upMerchantComm.add(totalAmount.subtract(upComm));
				}

				else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
						.equalsIgnoreCase(PaymentType.WALLET.getCode())) {

					wlCapturedCount++;
					totalAmount = totalAmount.add(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())));

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))) {
						acquirerTdrSc = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {
						acquirerGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {
						pgGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))) {
						pgTdrSc = new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName()));
					}

					BigDecimal wlComm = acquirerTdrSc.add(acquirerGst).add(pgGst).add(pgTdrSc);
					BigDecimal bankwlComm = acquirerTdrSc.add(acquirerGst);
					BigDecimal pgwlComm = pgGst.add(pgTdrSc);

					wlTotalComm = wlTotalComm.add(wlComm);
					wlBankComm = wlBankComm.add(bankwlComm);
					wlPgComm = wlPgComm.add(pgwlComm);
					wlMerchantComm = wlMerchantComm.add(totalAmount.subtract(wlComm));

				} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
						.equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {

					nbCapturedCount++;
					totalAmount = totalAmount.add(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())));

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))) {
						acquirerTdrSc = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {
						acquirerGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {
						pgGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))) {
						pgTdrSc = new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName()));
					}

					BigDecimal nbComm = acquirerTdrSc.add(acquirerGst).add(pgGst).add(pgTdrSc);
					BigDecimal banknbComm = acquirerTdrSc.add(acquirerGst);
					BigDecimal pgnbComm = pgGst.add(pgTdrSc);

					nbTotalComm = nbTotalComm.add(nbComm);
					nbBankComm = nbBankComm.add(banknbComm);
					nbPgComm = nbPgComm.add(pgnbComm);

					nbMerchantComm = nbMerchantComm.add(totalAmount.subtract(nbComm));

				} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
						.equalsIgnoreCase(PaymentType.EMI.getCode())) {
					emCapturedCount++;
					totalAmount = totalAmount.add(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())));

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))) {
						acquirerTdrSc = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {
						acquirerGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {
						pgGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))) {
						pgTdrSc = new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName()));
					}

					BigDecimal emComm = acquirerTdrSc.add(acquirerGst).add(pgGst).add(pgTdrSc);
					BigDecimal bankemComm = acquirerTdrSc.add(acquirerGst);
					BigDecimal pgemComm = pgGst.add(pgTdrSc);

					emTotalComm = emTotalComm.add(emComm);
					emBankComm = emBankComm.add(bankemComm);
					emPgComm = emPgComm.add(pgemComm);
					emMerchantComm = emMerchantComm.add(totalAmount.subtract(emComm));

				} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
						.equalsIgnoreCase(PaymentType.COD.getCode())) {
					cdCapturedCount++;
					totalAmount = totalAmount.add(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())));

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))) {
						acquirerTdrSc = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {
						acquirerGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {
						pgGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))) {
						pgTdrSc = new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName()));
					}

					BigDecimal cdComm = acquirerTdrSc.add(acquirerGst).add(pgGst).add(pgTdrSc);
					BigDecimal bankcdComm = acquirerTdrSc.add(acquirerGst);
					BigDecimal pgcdComm = pgGst.add(pgTdrSc);

					cdTotalComm = cdTotalComm.add(cdComm);
					cdBankComm = cdBankComm.add(bankcdComm);
					cdPgComm = cdPgComm.add(pgcdComm);

					cdMerchantComm = cdMerchantComm.add(totalAmount.subtract(cdComm));
				}
			}

			cursor.close();

			logger.info("getMerchantSMSData , RAW data prepared");
			merchantSMSObject.setTotalBooking(String.valueOf(ccCapturedCount + dcCapturedCount + upCapturedCount
					+ wlCapturedCount + nbCapturedCount + emCapturedCount + cdCapturedCount));
			merchantSMSObject.setTotalAmount(format(String.format("%.0f", totalAmount)));
			merchantSMSObject.setTotalCommWithGST(format(String.format("%.0f", ccTotalComm.add(dcTotalComm
					.add(upTotalComm).add(wlTotalComm).add(emTotalComm).add(nbTotalComm).add(cdTotalComm)))));
			merchantSMSObject.setBankCommWithGST(format(String.format("%.0f", ccBankComm
					.add(dcBankComm.add(upBankComm).add(wlBankComm).add(emBankComm).add(nbBankComm).add(cdBankComm)))));
			merchantSMSObject.setMerchantCommWithGST(
					format(String.format("%.0f", ccMerchantComm.add(dcMerchantComm.add(upMerchantComm)
							.add(wlMerchantComm).add(emMerchantComm).add(cdMerchantComm).add(nbMerchantComm)))));

			Double totalTxn = (Double.valueOf(ccCapturedCount + dcCapturedCount + upCapturedCount + wlCapturedCount
					+ nbCapturedCount + emCapturedCount + cdCapturedCount));
			Double ccTxn = (Double.valueOf(ccCapturedCount));
			Double dcTxn = (Double.valueOf(dcCapturedCount));
			Double upTxn = (Double.valueOf(upCapturedCount));
			Double wlTxn = (Double.valueOf(wlCapturedCount));
			Double emTxn = (Double.valueOf(emCapturedCount));
			Double nbTxn = (Double.valueOf(nbCapturedCount));
			Double cdTxn = (Double.valueOf(cdCapturedCount));

			Double hundred = 100.00;
			if (ccCapturedCount > 0) {
				merchantSMSObject.setCcPercentTicket(String.format("%.2f", (ccTxn / totalTxn) * hundred));
			} else {
				merchantSMSObject.setCcPercentTicket("0.00");
			}

			merchantSMSObject.setCcMerchantCommWithGST(format(String.format("%.0f", ccMerchantComm)));
			merchantSMSObject.setCcBankCommWithGST(format(String.format("%.0f", ccBankComm)));

			if (dcCapturedCount > 0) {
				merchantSMSObject.setDcPercentTicket(String.format("%.2f", (dcTxn / totalTxn) * hundred));
			} else {
				merchantSMSObject.setDcPercentTicket("0.00");
			}

			merchantSMSObject.setDcMerchantCommWithGST(format(String.format("%.0f", dcMerchantComm)));
			merchantSMSObject.setDcBankCommWithGST(format(String.format("%.0f", dcBankComm)));

			if (upCapturedCount > 0) {
				merchantSMSObject.setUpPercentTicket(String.format("%.2f", (upTxn / totalTxn) * hundred));
			} else {
				merchantSMSObject.setUpPercentTicket("0.00");
			}

			merchantSMSObject.setUpMerchantCommWithGST(format(String.format("%.0f", upMerchantComm)));
			merchantSMSObject.setUpBankCommWithGST(format(String.format("%.0f", upBankComm)));

			if (wlCapturedCount > 0) {
				merchantSMSObject.setWlPercentTicket(String.format("%.2f", (wlTxn / totalTxn) * hundred));
			} else {
				merchantSMSObject.setWlPercentTicket("0.00");
			}

			merchantSMSObject.setWlMerchantCommWithGST(format(String.format("%.0f", wlMerchantComm)));
			merchantSMSObject.setWlBankCommWithGST(format(String.format("%.0f", wlBankComm)));

			if (nbCapturedCount > 0) {
				merchantSMSObject.setNbPercentTicket(String.format("%.2f", (nbTxn / totalTxn) * hundred));
			} else {
				merchantSMSObject.setNbPercentTicket("0.00");
			}

			merchantSMSObject.setNbMerchantCommWithGST(format(String.format("%.0f", nbMerchantComm)));
			merchantSMSObject.setNbBankCommWithGST(format(String.format("%.0f", nbBankComm)));

			if (emCapturedCount > 0) {
				merchantSMSObject.setEmPercentTicket(String.format("%.2f", (emTxn / totalTxn) * hundred));
			} else {
				merchantSMSObject.setEmPercentTicket("0.00");
			}

			merchantSMSObject.setEmMerchantCommWithGST(format(String.format("%.0f", emMerchantComm)));
			merchantSMSObject.setEmBankCommWithGST(format(String.format("%.0f", emBankComm)));

			if (cdCapturedCount > 0) {
				merchantSMSObject.setCdPercentTicket(String.format("%.2f", (cdTxn / totalTxn) * hundred));
			} else {
				merchantSMSObject.setCdPercentTicket("0.00");
			}

			merchantSMSObject.setCdMerchantCommWithGST(format(String.format("%.0f", cdMerchantComm)));
			merchantSMSObject.setCdBankCommWithGST(format(String.format("%.0f", cdBankComm)));

			logger.info("getMerchantSMSData , Response sent");
			return merchantSMSObject;
		}

		catch (Exception e) {

			logger.error("Exception in transaction summary count service " , e);

			return merchantSMSObject;
		}

	}

}
