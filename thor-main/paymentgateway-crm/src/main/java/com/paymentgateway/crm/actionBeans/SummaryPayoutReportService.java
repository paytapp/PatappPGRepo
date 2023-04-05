package com.paymentgateway.crm.actionBeans;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.Document;
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
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.SummaryPayoutData;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDao;
import com.paymentgateway.commons.user.TransactionCountSearch;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.crm.action.TdrPojo;

@Service
public class SummaryPayoutReportService {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userDao;

	@Autowired
	private SurchargeDao surchargeDao;

	@Autowired
	private PropertiesManager propertiesManager;

	private static Logger logger = LoggerFactory.getLogger(SummaryPayoutReportService.class.getName());

	private static final String prefix = "MONGO_DB_";
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
	Map<String, List<Surcharge>> surchargeMap = new HashMap<String, List<Surcharge>>();
	List<Surcharge> surchargeList = new ArrayList<Surcharge>();
	Map<String, User> userMap = new HashMap<String, User>();

	public List<SummaryPayoutData> getTransactionCount(String fromDate, String toDate, String payId, String paymentType,
			String acquirer) {

		List<SummaryPayoutData> summaryPayoutDataList = new ArrayList<SummaryPayoutData>();

		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

			Date date1 = format.parse(fromDate);
			Date date2 = format.parse(toDate);

			surchargeList = surchargeDao.findAllSurchargeByDate(date1, date2);
			List<String> merchantList = new ArrayList<String>();
			List<String> acquirerList = new ArrayList<String>();
			List<String> paymentTypeList = new ArrayList<String>();

			List<String> txnTypeList = new ArrayList<String>();
			txnTypeList.add(TxnType.SALE.getName());
			txnTypeList.add(TxnType.REFUND.getName());

			// Merchant List
			if (payId.equalsIgnoreCase("ALL")) {

				List<Merchants> userList = new ArrayList<Merchants>();
				userList = userDao.getActiveMerchantList();
				for (Merchants user : userList) {
					merchantList.add(user.getPayId());
				}

			} else {
				merchantList.add(payId);
			}

			// Acquirer list
			if (acquirer.equalsIgnoreCase("ALL")) {

			}

			else {
				List<String> acqList = Arrays.asList(acquirer.split(","));
				for (String acq : acqList) {
					acquirerList.add(acq.replace(" ", ""));
				}
			}

			// Acquirer list
			if (paymentType.equalsIgnoreCase("ALL")) {

				paymentTypeList.add("CCDC");
				paymentTypeList.add("UP");
			}

			else {
				paymentTypeList.add(paymentType);
			}
			for (String merchant : merchantList) {
				for (String acquirers : acquirerList) {
					for (String payType : paymentTypeList) {

						List<TransactionCountSearch> transactionCountSearchList = new ArrayList<TransactionCountSearch>();
						BasicDBObject dateQuery = new BasicDBObject();
						List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
						BasicDBObject acquirerQuery = new BasicDBObject();
						BasicDBObject paymentTypeQuery = new BasicDBObject();
						BasicDBObject allParamQuery = new BasicDBObject();
						List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
						List<BasicDBObject> paymentTypeConditionLst = new ArrayList<BasicDBObject>();

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
									BasicDBObjectBuilder
											.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
											.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
						}

						paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchant));

						acquirerConditionLst
								.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acquirers.replace(" ", "")));
						acquirerQuery.append("$or", acquirerConditionLst);

						// paymentType Query

						if (payType.contains("CC")) {
							paymentTypeConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
									PaymentType.CREDIT_CARD.getCode()));
							paymentTypeConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
									PaymentType.DEBIT_CARD.getCode()));
							paymentTypeQuery.append("$or", paymentTypeConditionLst);

						} else if (payType.contains("UP")) {
							List<String> payTypeList = Arrays.asList(payType.split(","));
							for (String payTyp : payTypeList) {
								paymentTypeConditionLst.add(
										new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getCode()));
							}
							paymentTypeQuery.append("$or", paymentTypeConditionLst);

						} else {

						}

						// Sale settled condition
						List<BasicDBObject> saleConditionList = new ArrayList<BasicDBObject>();
						saleConditionList
								.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
						saleConditionList
								.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

						BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleConditionList);

						// Refund settled condition
						List<BasicDBObject> refundConditionList = new ArrayList<BasicDBObject>();
						refundConditionList.add(
								new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
						refundConditionList
								.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
						BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundConditionList);

						List<BasicDBObject> bothConditionList = new ArrayList<BasicDBObject>();
						bothConditionList.add(saleConditionQuery);
						bothConditionList.add(refundConditionQuery);
						BasicDBObject addConditionListQuery = new BasicDBObject("$or", bothConditionList);
						if (!paramConditionLst.isEmpty()) {
							allParamQuery = new BasicDBObject("$and", paramConditionLst);
						}

						List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();
						if (!paymentTypeQuery.isEmpty()) {
							fianlList.add(paymentTypeQuery);
						}
						if (!acquirerQuery.isEmpty()) {
							fianlList.add(acquirerQuery);
						}
						if (!dateQuery.isEmpty()) {
							fianlList.add(dateQuery);
						}

						if (!allParamQuery.isEmpty()) {
							fianlList.add(allParamQuery);
						}
						if (!addConditionListQuery.isEmpty()) {
							fianlList.add(addConditionListQuery);
						}

						BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

						MongoDatabase dbIns = mongoInstance.getDB();
						MongoCollection<Document> coll = dbIns.getCollection(
								propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

						// create our pipeline operations, first with the $match
						MongoCursor<Document> cursor = coll.find(finalquery).iterator();

						while (cursor.hasNext()) {
							Document dbobj = cursor.next();

							TransactionCountSearch transactionCountSearchObj = new TransactionCountSearch();
							transactionCountSearchObj = findDetails(dbobj);
							transactionCountSearchList.add(transactionCountSearchObj);

						}

						cursor.close();

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

						for (TransactionCountSearch transactionCountSearchObj : transactionCountSearchList) {

							if (transactionCountSearchObj.getTxnType()
									.equalsIgnoreCase(TransactionType.SALE.getName())) {

								saleSettledAmount = saleSettledAmount
										.add(new BigDecimal(transactionCountSearchObj.getSaleSettledAmount()));
								pgSaleSurcharge = pgSaleSurcharge
										.add(new BigDecimal(transactionCountSearchObj.getPgSaleSurcharge()));
								pgSaleGst = pgSaleGst.add(new BigDecimal(transactionCountSearchObj.getPgSaleGst()));
								acquirerSaleSurcharge = acquirerSaleSurcharge
										.add(new BigDecimal(transactionCountSearchObj.getAcquirerSaleSurcharge()));
								acquirerSaleGst = acquirerSaleGst
										.add(new BigDecimal(transactionCountSearchObj.getAcquirerSaleGst()));

							} else if (transactionCountSearchObj.getTxnType()
									.equalsIgnoreCase(TransactionType.REFUND.getName())) {

								refundSettledAmount = refundSettledAmount
										.add(new BigDecimal(transactionCountSearchObj.getRefundSettledAmount()));
								pgRefundSurcharge = pgRefundSurcharge
										.add(new BigDecimal(transactionCountSearchObj.getPgRefundSurcharge()));
								pgRefundGst = pgRefundGst
										.add(new BigDecimal(transactionCountSearchObj.getPgRefundGst()));
								acquirerRefundSurcharge = acquirerRefundSurcharge
										.add(new BigDecimal(transactionCountSearchObj.getAcquirerRefundSurcharge()));
								acquirerRefundGst = acquirerRefundGst
										.add(new BigDecimal(transactionCountSearchObj.getAcquirerRefundGst()));

							}
						}

						// POJOS

						// BigDecimal totalMerchantAmt =
						// saleSettledAmount.subtract(refundSettledAmount).setScale(2,
						// RoundingMode.HALF_DOWN);

						/*
						 * BigDecimal pgbaseAmt = pgSaleSurcharge.subtract(pgRefundSurcharge); pgbaseAmt
						 * = pgbaseAmt.subtract(pgSaleGst).setScale(2, RoundingMode.HALF_DOWN);
						 * BigDecimal pgGst = pgSaleGst.subtract(pgRefundGst).setScale(2,
						 * RoundingMode.HALF_DOWN); BigDecimal totalPgFee =
						 * pgbaseAmt.add(pgGst).setScale(2, RoundingMode.HALF_DOWN);
						 * 
						 * 
						 * BigDecimal acquirerbaseAmt =
						 * acquirerSaleSurcharge.subtract(acquirerRefundSurcharge); acquirerbaseAmt =
						 * acquirerbaseAmt.subtract(acquirerSaleGst).setScale(2,
						 * RoundingMode.HALF_DOWN); BigDecimal acquirerGst =
						 * acquirerSaleGst.subtract(acquirerRefundGst).setScale(2,
						 * RoundingMode.HALF_DOWN); BigDecimal totalaAquirerFee =
						 * acquirerbaseAmt.add(acquirerGst).setScale(2, RoundingMode.HALF_DOWN);
						 */

						BigDecimal totalPgFee = pgSaleSurcharge.subtract(pgRefundSurcharge).setScale(2,
								RoundingMode.HALF_DOWN);
						totalPgFee = totalPgFee.add(pgSaleGst.subtract(pgRefundGst)).setScale(2,
								RoundingMode.HALF_DOWN);

						BigDecimal totalaAquirerFee = acquirerSaleSurcharge.subtract(acquirerRefundSurcharge)
								.setScale(2, RoundingMode.HALF_DOWN);
						totalaAquirerFee = totalaAquirerFee.add(acquirerSaleGst.subtract(acquirerRefundGst)).setScale(2,
								RoundingMode.HALF_DOWN);

						SummaryPayoutData summaryPayoutData = new SummaryPayoutData();

						summaryPayoutData.setAcquirerName(acquirers);
						summaryPayoutData.setPaymentype(payType.replace(",", ""));
						summaryPayoutData.setNoOfTxn(String.valueOf(transactionCountSearchList.size()));
						summaryPayoutData.setMerchantDiffAmt(String.valueOf(
								saleSettledAmount.subtract(refundSettledAmount).setScale(2, RoundingMode.HALF_DOWN)));
						summaryPayoutData.setPgBaseAmt(String.valueOf(
								pgSaleSurcharge.subtract(pgRefundSurcharge).setScale(2, RoundingMode.HALF_DOWN)));
						summaryPayoutData.setGstPg(
								String.valueOf(pgSaleGst.subtract(pgRefundGst).setScale(2, RoundingMode.HALF_DOWN)));
						summaryPayoutData.setTotalPgFee(String.valueOf(totalPgFee));
						summaryPayoutData.setBusinessName(merchant);
						summaryPayoutData.setAcquirerBaseAmt(String.valueOf(acquirerSaleSurcharge
								.subtract(acquirerRefundSurcharge).setScale(2, RoundingMode.HALF_DOWN)));
						summaryPayoutData.setGstAcquirer(String.valueOf(
								acquirerSaleGst.subtract(acquirerRefundGst).setScale(2, RoundingMode.HALF_DOWN)));
						summaryPayoutData.setTotalAcquirerFee(String.valueOf(totalaAquirerFee));

						summaryPayoutDataList.add(summaryPayoutData);
					}
				}
			}
			return summaryPayoutDataList;
		} catch (Exception e) {
			logger.error("Exception inpayout report " , e);
		}
		return summaryPayoutDataList;

	}

	public TransactionCountSearch findDetails(Document dbobj) {

		BigDecimal merchantGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal acquirerGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);

		TdrPojo tdrPojo = new TdrPojo();
		BigDecimal st = null;
		String bussinessType = null;
		String payId = (dbobj.getString(FieldType.PAY_ID.toString()));
		if (!StringUtils.isBlank(payId)) {

			User user = new User();

			if (userMap.get(payId) != null) {
				user = userMap.get(payId);
			} else {
				user = userDao.findPayId(payId);
				userMap.put(payId, user);
			}

			String amount = (dbobj.getString(FieldType.AMOUNT.toString()));
			String totalAmount = (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));

			bussinessType = user.getIndustryCategory();

			st = new BigDecimal(PropertiesManager.propertiesMap.get("SERVICE_TAX"));
			st = st.setScale(2, RoundingMode.HALF_DOWN);

			if (!StringUtils.isBlank(dbobj.getString(FieldType.SURCHARGE_FLAG.toString()))) {

				if (dbobj.getString(FieldType.SURCHARGE_FLAG.toString()).equals("Y")) {

					String txnAmount = amount;
					String txnTotalAmount = totalAmount;

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

					Date surchargeStartDate = null;
					Date surchargeEndDate = null;
					Date settlementDate = null;
					Surcharge surcharge = new Surcharge();

					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
					try {
						for (Surcharge surchargeData : surchargeList) {

							if (AcquirerType.getInstancefromName(surchargeData.getAcquirerName()).toString()
									.equalsIgnoreCase(dbobj.getString(FieldType.ACQUIRER_TYPE.toString()))
									&& surchargeData.getPaymentType().getCode()
											.equalsIgnoreCase(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
									&& surchargeData.getMopType().getCode()
											.equalsIgnoreCase(dbobj.getString(FieldType.MOP_TYPE.toString()))
									&& surchargeData.getPaymentsRegion().name().equalsIgnoreCase(paymentsRegion)
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

					if (surcharge.getBankSurchargeAmountCustomer() == null
							|| surcharge.getBankSurchargePercentageCustomer() == null
							|| surcharge.getBankSurchargeAmountCommercial() == null
							|| surcharge.getBankSurchargePercentageCommercial() == null) {

						logger.info("Surcharge is null for apyid = " + dbobj.getString(FieldType.PAY_ID.toString())
								+ " acquirer = " + dbobj.getString(FieldType.ACQUIRER_TYPE.toString()) + " mop = "
								+ dbobj.getString(FieldType.MOP_TYPE.toString()) + "  paymentType = "
								+ dbobj.getString(FieldType.PAYMENT_TYPE.toString()) + "  paymentRegion = "
								+ paymentsRegion);
						// continue;
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

					BigDecimal netTxnAmount = new BigDecimal(txnAmount);
					BigDecimal netTxnTotalAmount = new BigDecimal(txnTotalAmount);

					BigDecimal netcalculatedSurchargeWithGst = netTxnTotalAmount.subtract(netTxnAmount);
					netcalculatedSurchargeWithGst = netcalculatedSurchargeWithGst.setScale(2, RoundingMode.HALF_DOWN);

					BigDecimal totalGst = netcalculatedSurchargeWithGst.multiply(st).divide(((ONE_HUNDRED).add(st)), 2,
							RoundingMode.HALF_DOWN);

					BigDecimal pgSurchargeAmountWithoutGst;
					BigDecimal acquirerSurchargeAmountWithoutGst;

					if (netcalculatedSurchargeWithGst.equals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN))) {
						pgSurchargeAmountWithoutGst = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
						acquirerSurchargeAmountWithoutGst = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
					}

					else {

						acquirerSurchargeAmountWithoutGst = netTxnAmount.multiply(bankSurchargePercent)
								.divide(((ONE_HUNDRED)), 2, RoundingMode.HALF_DOWN);
						acquirerSurchargeAmountWithoutGst = acquirerSurchargeAmountWithoutGst.add(bankSurchargeFC);

						pgSurchargeAmountWithoutGst = netcalculatedSurchargeWithGst
								.subtract(acquirerSurchargeAmountWithoutGst);
						pgSurchargeAmountWithoutGst = pgSurchargeAmountWithoutGst.subtract(totalGst);
						pgSurchargeAmountWithoutGst = pgSurchargeAmountWithoutGst.setScale(2, RoundingMode.HALF_DOWN);

					}

					acquirerGstAmount = acquirerSurchargeAmountWithoutGst.multiply(st).divide(ONE_HUNDRED, 2,
							RoundingMode.HALF_DOWN);

					merchantGstAmount = pgSurchargeAmountWithoutGst.multiply(st).divide(ONE_HUNDRED, 2,
							RoundingMode.HALF_DOWN);

					TransactionCountSearch transactionCountSearchObj = new TransactionCountSearch();

					if (dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase(TransactionType.RECO.getName())
							|| dbobj.getString(FieldType.TXNTYPE.toString())
									.equalsIgnoreCase(TransactionType.SALE.getName())) {

						transactionCountSearchObj.setPaymentMethod(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
						transactionCountSearchObj.setTxnType(TransactionType.SALE.getName());
						transactionCountSearchObj.setSaleSettledAmount(String.valueOf(netTxnAmount));
						transactionCountSearchObj.setPgSaleSurcharge(String.valueOf(pgSurchargeAmountWithoutGst));
						transactionCountSearchObj
								.setAcquirerSaleSurcharge(String.valueOf(acquirerSurchargeAmountWithoutGst));
						transactionCountSearchObj.setPgSaleGst(String.valueOf(merchantGstAmount));
						transactionCountSearchObj.setAcquirerSaleGst(String.valueOf(acquirerGstAmount));
					}

					else if (dbobj.getString(FieldType.TXNTYPE.toString())
							.equalsIgnoreCase(TransactionType.REFUNDRECO.getName())
							|| dbobj.getString(FieldType.TXNTYPE.toString())
									.equalsIgnoreCase(TransactionType.REFUND.getName())) {
						{

							transactionCountSearchObj
									.setPaymentMethod(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
							transactionCountSearchObj.setTxnType(TransactionType.REFUND.getName());
							transactionCountSearchObj.setRefundSettledAmount(String.valueOf(netTxnAmount));
							transactionCountSearchObj.setPgRefundSurcharge(String.valueOf(pgSurchargeAmountWithoutGst));
							transactionCountSearchObj
									.setAcquirerRefundSurcharge(String.valueOf(acquirerSurchargeAmountWithoutGst));
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

}
