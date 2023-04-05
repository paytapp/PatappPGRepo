package com.paymentgateway.crm.action;

import java.math.BigDecimal;
import java.math.BigInteger;
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
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.SUFDetailDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.SUFDetail;
import com.paymentgateway.commons.user.SummaryReportObject;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;

import bsh.StringUtil;

@Component
public class SummaryReportQuery {
	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userdao;

	@Autowired
	PropertiesManager propertiesManager;
	
	@Autowired
	private SUFDetailDao sufDetailDao;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;
	
	private static Logger logger = LoggerFactory.getLogger(SummaryReportQuery.class.getName());
	private static final String alphabaticFileName = "alphabatic-currencycode.properties";
	private static final String prefix = "MONGO_DB_";
	private final String CANCELLATION_PARTIAL_REFUND_FLAG = "C";
	private final String FULL_REFUND_FLAG = "R";

	public List<TransactionSearch> summaryReport(String fromDate, String toDate, String payId, String subMerchantPayId, String paymentType,
			String acquirer, String currency, User user, int start, int length, String paymentsRegion,
			String cardHolderType, String pgRefNum, String mopType, String transactionType, String partSettleFlag, String transactionFlag) throws SystemException {

		logger.info("Inside search summary report query");
		Map<String, User> userMap = new HashMap<String, User>();
		
		List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
		try {

			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject acquirerQuery = new BasicDBObject();
			BasicDBObject txnCapturedFlag = new BasicDBObject();

			BasicDBObject allParamQuery = new BasicDBObject();
			List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> saleOrAuthList = new ArrayList<BasicDBObject>();

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

			for (String dateIndex : allDatesIndex) {
				dateIndexConditionList.add(new BasicDBObject("DATE_INDEX", dateIndex));
			}
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndexConditionQuery.append("$or", dateIndexConditionList);
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}
			if (!payId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			}
			
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			
			if (!currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
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
			if (StringUtils.isNotBlank(transactionFlag) && !transactionFlag.equalsIgnoreCase("ALL")) {
				 txnCapturedFlag.append("$in", transactionFlag.split(","));
		            paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), txnCapturedFlag));
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

			if (transactionType.equalsIgnoreCase("ALL")) {

				List<BasicDBObject> saleConditionList = new ArrayList<BasicDBObject>();
				saleConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
				saleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
				saleConditionList
						.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));

				BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleConditionList);

				List<BasicDBObject> authConditionList = new ArrayList<BasicDBObject>();
				authConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.AUTHORISE.getName()));
				authConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.APPROVED.getName()));

				BasicDBObject authConditionQuery = new BasicDBObject("$and", authConditionList);

				List<BasicDBObject> recoConditionList = new ArrayList<BasicDBObject>();
				recoConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
				recoConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject recoConditionQuery = new BasicDBObject("$and", recoConditionList);

				List<BasicDBObject> refundConditionList = new ArrayList<BasicDBObject>();
				refundConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
				refundConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundConditionList);

				List<BasicDBObject> recoRefundConditionList = new ArrayList<BasicDBObject>();
				recoRefundConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
				recoRefundConditionList
						.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject recoRefundConditionQuery = new BasicDBObject("$and", recoRefundConditionList);

				saleOrAuthList.add(saleConditionQuery);
				saleOrAuthList.add(authConditionQuery);
				saleOrAuthList.add(recoConditionQuery);
				saleOrAuthList.add(refundConditionQuery);
				saleOrAuthList.add(recoRefundConditionQuery);

			}

			else if (transactionType.equalsIgnoreCase("SALE")) {

				List<BasicDBObject> saleConditionList = new ArrayList<BasicDBObject>();
				saleConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
				saleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
				saleConditionList
						.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));

				BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleConditionList);

				List<BasicDBObject> authConditionList = new ArrayList<BasicDBObject>();
				authConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.AUTHORISE.getName()));
				authConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.APPROVED.getName()));

				BasicDBObject authConditionQuery = new BasicDBObject("$and", authConditionList);

				List<BasicDBObject> recoConditionList = new ArrayList<BasicDBObject>();
				recoConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
				recoConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject recoConditionQuery = new BasicDBObject("$and", recoConditionList);

				saleOrAuthList.add(saleConditionQuery);
				saleOrAuthList.add(authConditionQuery);
				saleOrAuthList.add(recoConditionQuery);

			}

			else {

				List<BasicDBObject> refundConditionList = new ArrayList<BasicDBObject>();
				refundConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
				refundConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundConditionList);

				List<BasicDBObject> recoRefundConditionList = new ArrayList<BasicDBObject>();
				recoRefundConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
				recoRefundConditionList
						.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject recoRefundConditionQuery = new BasicDBObject("$and", recoRefundConditionList);

				saleOrAuthList.add(refundConditionQuery);
				saleOrAuthList.add(recoRefundConditionQuery);

			}

			BasicDBObject authndSaleConditionQuery = new BasicDBObject("$or", saleOrAuthList);

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

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
			if (!authndSaleConditionQuery.isEmpty()) {
				allConditionQueryList.add(authndSaleConditionQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryList.add(dateIndexConditionQuery);
			}

			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside search summary report query , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
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
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					doc = dataEncDecTool.decryptDocument(doc);
				}
				
				TransactionSearch transReport = new TransactionSearch();
				transReport.setCustomerName(doc.getString(FieldType.CUST_NAME.toString()));
				BigInteger txnId = new BigInteger(doc.getString(FieldType.TXN_ID.toString()));
				transReport.setTransactionId(txnId);
				transReport.setTransactionIdString(String.valueOf(txnId));
				transReport.setDateFrom(doc.getString(FieldType.CREATE_DATE.getName()));
				transReport.setInternalCardIssusserBank(doc.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.toString()));
				transReport.setInternalCardIssusserCountry(
						doc.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.toString()));
				if (null != doc.getString(FieldType.CURRENCY_CODE.toString())) {
					transReport.setCurrency(propertiesManager
							.getAlphabaticCurrencyCode(doc.getString(FieldType.CURRENCY_CODE.toString())));
				} else {
					transReport.setCurrency(CrmFieldConstants.NA.getValue());
				}
				transReport.setPayId(doc.getString(FieldType.PAY_ID.toString()));
				
				if(doc.containsKey(FieldType.SUB_MERCHANT_ID.getName()) && StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
					transReport.setSubMerchantId(userdao.getBusinessNameByPayId(doc.getString(FieldType.SUB_MERCHANT_ID.getName())));
				}
				
				String payid = (String) doc.get(FieldType.PAY_ID.getName());
				User user1 = new User();
				if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
					user1 = userMap.get(payid);
				} else {
					user1 = userdao.findPayId(payid);
					userMap.put(payid, user1);
				}
				
				transReport.setBusinessName(user1.getBusinessName());
				
				if (StringUtils.isNotBlank(doc.getString(FieldType.REFUND_ORDER_ID.toString()))) {
					transReport.setRefundOrderId(doc.getString(FieldType.REFUND_ORDER_ID.toString()));
				} else {
					transReport.setRefundOrderId(CrmFieldConstants.NA.getValue());
				}
				transReport.setTxnSettledType(doc.getString(FieldType.TXN_CAPTURE_FLAG.toString()));
				transReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
				String surchargeFlag = doc.getString(FieldType.SURCHARGE_FLAG.toString());
				transReport.setSurchargeFlag(surchargeFlag);
				transReport.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.toString()));
				transReport.setAmount(doc.getString(FieldType.AMOUNT.toString()));
				transReport.setTransactionRegion(doc.getString(FieldType.PAYMENTS_REGION.toString()));
				transReport.setCardHolderType(doc.getString(FieldType.CARD_HOLDER_TYPE.toString()));
				transReport.setTotalAmount(doc.getString(FieldType.TOTAL_AMOUNT.toString()));
				transReport.setAcquirerType(doc.getString(FieldType.ACQUIRER_TYPE.toString()));
				transReport.setMopType(MopType.getmopName(doc.getString(FieldType.MOP_TYPE.toString())));
				transReport.setPaymentMethods(
						PaymentType.getpaymentName(doc.getString(FieldType.PAYMENT_TYPE.toString())));
				transReport.setTransactionCaptureDate(doc.getString(FieldType.PG_DATE_TIME.toString()));
				transReport.setTxnType(doc.getString(FieldType.ORIG_TXNTYPE.toString()));
				transReport.setInternalCardIssusserCountry(
						doc.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.toString()));
				if (StringUtils.isNotBlank(doc.getString(FieldType.PART_SETTLE.toString()))
						&& doc.getString(FieldType.PART_SETTLE.toString()).equalsIgnoreCase(("Y"))) {
					transReport.setPartSettle(doc.getString(FieldType.PART_SETTLE.toString()));
				} else {
					transReport.setPartSettle(CrmFieldConstants.NA.getValue());
				}
				
				if (StringUtils.isNotBlank(doc.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
					transReport.setTxnSettledType(doc.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
				} else {
					transReport.setTxnSettledType(CrmFieldConstants.NA.getValue());
				}
//				transReport.setPartSettle(doc.getString(FieldType.PART_SETTLE.toString()));
				
				if (null != doc.getString(FieldType.CURRENCY_CODE.toString())) {
					transReport.setCurrency(propertiesManager
							.getAlphabaticCurrencyCode(doc.getString(FieldType.CURRENCY_CODE.toString())));
				} else {
					transReport.setCurrency(CrmFieldConstants.NA.getValue());
				}

				transReport.setAcquirerMode(doc.getString(FieldType.ACQUIRER_MODE.getName()));

				if (doc.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.SALE.getName())) {

					transReport.setAcquirerSurchargeAmount(
							String.valueOf(Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))));
					transReport.setTotalGstOnAcquirer(
							String.valueOf(Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString()))));

					transReport.setPgSurchargeAmount(
							String.valueOf(Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))));
					transReport.setTotalGstOnMerchant(
							String.valueOf(Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))));
					
					if(doc.containsKey(FieldType.RESELLER_CHARGES.getName()) && StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName()))) {
						transReport.setResellerCharges(doc.getString(FieldType.RESELLER_CHARGES.getName()));
					} else {
						transReport.setResellerCharges("0.00");
					}
					
					if(doc.containsKey(FieldType.RESELLER_GST.getName()) && StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_GST.getName()))) {
						transReport.setResellerGST(doc.getString(FieldType.RESELLER_GST.getName()));
					} else {
						transReport.setResellerGST("0.00");
					}
					

					if (doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode())) {

						
						
						transReport.setNetMerchantPayableAmount("-" + String.format("%.2f",
								(Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString()))
										+ Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))
										+ Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))
										+ Double.parseDouble(transReport.getResellerCharges())
										+ Double.parseDouble(transReport.getResellerGST()))));

					} else {

						transReport.setNetMerchantPayableAmount(String.format("%.2f",
								(Double.parseDouble(doc.getString(FieldType.TOTAL_AMOUNT.toString()))
										- (Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString()))
												+ Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))
												+ Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))
												+ Double.parseDouble(transReport.getResellerCharges())
												+ Double.parseDouble(transReport.getResellerGST())))));
					}
				} else if (doc.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName())) {

					transReport.setTotalAmount(String.format("%.2f",
							Double.parseDouble(doc.getString(FieldType.TOTAL_AMOUNT.toString())) * -1));
					transReport.setAcquirerSurchargeAmount("0.00");
					transReport.setTotalGstOnAcquirer("0.00");
					transReport.setPgSurchargeAmount("0.00");
					transReport.setTotalGstOnMerchant("0.00");
					transReport.setResellerCharges("0.00");
					transReport.setResellerGST("0.00");

					if (doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode())) {

						transReport.setNetMerchantPayableAmount("0.00");

					} else {
						transReport.setNetMerchantPayableAmount(String.format("%.2f",
								Double.parseDouble(doc.getString(FieldType.TOTAL_AMOUNT.toString())) * -1));

					}

				}
				
				//Merchant TDR/SC and Merhcant GST
				if(doc.containsKey(FieldType.MERCHANT_TDR_SC.getName())
						&& doc.containsKey(FieldType.MERCHANT_GST.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.MERCHANT_TDR_SC.getName()))
						&& StringUtils.isNotBlank(doc.getString(FieldType.MERCHANT_GST.getName()))) {
					
					transReport.setMerchantTdrOrSc(doc.getString(FieldType.MERCHANT_TDR_SC.getName()));
					transReport.setMerchantGst(doc.getString(FieldType.MERCHANT_GST.getName()));
				
				} else if(doc.containsKey(FieldType.PG_TDR_SC.getName())
							&& doc.containsKey(FieldType.PG_GST.getName())
							&& doc.containsKey(FieldType.ACQUIRER_TDR_SC.getName())
							&& doc.containsKey(FieldType.ACQUIRER_GST.getName())
							&& doc.containsKey(FieldType.RESELLER_CHARGES.getName())
							&& doc.containsKey(FieldType.RESELLER_GST.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.PG_TDR_SC.getName()))
							&& StringUtils.isNotBlank(doc.getString(FieldType.PG_GST.getName()))
							&& StringUtils.isNotBlank(doc.getString(FieldType.ACQUIRER_TDR_SC.getName()))
							&& StringUtils.isNotBlank(doc.getString(FieldType.ACQUIRER_GST.getName()))
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName()))
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_GST.getName()))) {
					
					transReport.setMerchantTdrOrSc(String.format("%.2f", ((Double
							.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString())))
							+ (Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString())))
							+ (Double.parseDouble(doc.getString(FieldType.RESELLER_CHARGES.toString()))))));
					
					transReport.setMerchantGst(String.format("%.2f", ((Double
							.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString())))
							+ (Double.parseDouble(doc.getString(FieldType.PG_GST.toString())))
							+ (Double.parseDouble(doc.getString(FieldType.RESELLER_GST.toString()))))));
					
				} else {
					
					if(doc.containsKey(FieldType.PG_TDR_SC.getName())
							&& doc.containsKey(FieldType.ACQUIRER_TDR_SC.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.PG_TDR_SC.getName()))
							&& StringUtils.isNotBlank(doc.getString(FieldType.ACQUIRER_TDR_SC.getName()))) {
						
						transReport.setMerchantTdrOrSc(String.format("%.2f", ((Double
								.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString())))
								+ (Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))))));
					} else {
						transReport.setMerchantTdrOrSc(CrmFieldConstants.NA.getValue());
					}
					
					if(doc.containsKey(FieldType.PG_GST.getName())
							&& doc.containsKey(FieldType.ACQUIRER_GST.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.PG_GST.getName()))
							&& StringUtils.isNotBlank(doc.getString(FieldType.ACQUIRER_GST.getName()))) {
						
						transReport.setMerchantGst(String.format("%.2f", ((Double
								.parseDouble(doc.getString(FieldType.PG_GST.toString())))
								+ (Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString()))))));
					} else {
						transReport.setMerchantGst(CrmFieldConstants.NA.getValue());
					}
					
				}

				transactionList.add(transReport);
			}

			logger.info("Inside search summary report query , transactionList size = " + transactionList.size());
			cursor.close();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return transactionList;
	}

	public int summaryReportRecord(String fromDate, String toDate, String payId, String subMerchantPayId, String paymentType, String acquirer,
			String currency, User user, String paymentsRegion, String cardHolderType, String pgRefNum, String mopType,
			String transactionType, String transactionFlag) throws SystemException {

		int total = 0;
		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject acquirerQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject txnCapturedFlag = new BasicDBObject();
			List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> saleOrAuthList = new ArrayList<BasicDBObject>();

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

			for (String dateIndex : allDatesIndex) {
				dateIndexConditionList.add(new BasicDBObject("DATE_INDEX", dateIndex));
			}
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndexConditionQuery.append("$or", dateIndexConditionList);
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}

			if (!payId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
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

			if (!currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}

			if (!mopType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), mopType));
			}
			if (StringUtils.isNotBlank(transactionFlag) && !transactionFlag.equalsIgnoreCase("ALL")) {
				 txnCapturedFlag.append("$in", transactionFlag.split(","));
		            paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), txnCapturedFlag));
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
			} else {
			}

			if (transactionType.equalsIgnoreCase("ALL")) {

				List<BasicDBObject> saleConditionList = new ArrayList<BasicDBObject>();
				saleConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
				saleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
				saleConditionList
						.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));

				BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleConditionList);

				List<BasicDBObject> authConditionList = new ArrayList<BasicDBObject>();
				authConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.AUTHORISE.getName()));
				authConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.APPROVED.getName()));

				BasicDBObject authConditionQuery = new BasicDBObject("$and", authConditionList);

				List<BasicDBObject> recoConditionList = new ArrayList<BasicDBObject>();
				recoConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
				recoConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject recoConditionQuery = new BasicDBObject("$and", recoConditionList);

				List<BasicDBObject> refundConditionList = new ArrayList<BasicDBObject>();
				refundConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
				refundConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundConditionList);

				List<BasicDBObject> recoRefundConditionList = new ArrayList<BasicDBObject>();
				recoRefundConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
				recoRefundConditionList
						.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject recoRefundConditionQuery = new BasicDBObject("$and", recoRefundConditionList);

				saleOrAuthList.add(saleConditionQuery);
				saleOrAuthList.add(authConditionQuery);
				saleOrAuthList.add(recoConditionQuery);
				saleOrAuthList.add(refundConditionQuery);
				saleOrAuthList.add(recoRefundConditionQuery);

			}

			else if (transactionType.equalsIgnoreCase("SALE")) {

				List<BasicDBObject> saleConditionList = new ArrayList<BasicDBObject>();
				saleConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
				saleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
				saleConditionList
						.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));

				BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleConditionList);

				List<BasicDBObject> authConditionList = new ArrayList<BasicDBObject>();
				authConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.AUTHORISE.getName()));
				authConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.APPROVED.getName()));

				BasicDBObject authConditionQuery = new BasicDBObject("$and", authConditionList);

				List<BasicDBObject> recoConditionList = new ArrayList<BasicDBObject>();
				recoConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
				recoConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject recoConditionQuery = new BasicDBObject("$and", recoConditionList);

				saleOrAuthList.add(saleConditionQuery);
				saleOrAuthList.add(authConditionQuery);
				saleOrAuthList.add(recoConditionQuery);

			}

			else {

				List<BasicDBObject> refundConditionList = new ArrayList<BasicDBObject>();
				refundConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
				refundConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundConditionList);

				List<BasicDBObject> recoRefundConditionList = new ArrayList<BasicDBObject>();
				recoRefundConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
				recoRefundConditionList
						.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject recoRefundConditionQuery = new BasicDBObject("$and", recoRefundConditionList);

				saleOrAuthList.add(refundConditionQuery);
				saleOrAuthList.add(recoRefundConditionQuery);

			}

			BasicDBObject authndSaleConditionQuery = new BasicDBObject("$or", saleOrAuthList);

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

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
			if (!authndSaleConditionQuery.isEmpty()) {
				allConditionQueryList.add(authndSaleConditionQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryList.add(dateIndexConditionQuery);
			}

			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			total = (int) coll.count(finalquery);

			logger.info("Inside search summary report query count , total records from DB  = " + total);
		}

		catch (Exception e) {
			logger.error("Exception " , e);
		}
		return total;

	}

	public List<TransactionSearch> summaryReportRecordMerchant(String fromDate, String toDate, String payId,
			String paymentType, String currency, User user) throws SystemException {

		// TdrPojo tdrPojo = new TdrPojo();
		List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject currencyQuery = new BasicDBObject();
		BasicDBObject acquirerQuery = new BasicDBObject();
		BasicDBObject allParamQuery = new BasicDBObject();
		// List<BasicDBObject> acquirerConditionLst = new
		// ArrayList<BasicDBObject>();
		List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> paymentTypeConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject paymentTypeQuery = new BasicDBObject();

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
		if (!currency.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
		}

		if (!paymentType.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
		}
		if (!paramConditionLst.isEmpty()) {
			allParamQuery = new BasicDBObject("$and", paramConditionLst);
		}

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

		BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
		List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

		if (!allParamQuery.isEmpty()) {
			fianlList.add(allParamQuery);
		}
		if (!allConditionQueryObj.isEmpty()) {
			fianlList.add(allConditionQueryObj);
		}

		BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		// create our pipeline operations, first with the $match
		MongoCursor<Document> cursor = coll.find(finalquery).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				doc = dataEncDecTool.decryptDocument(doc);
			}
			TransactionSearch transReport = new TransactionSearch();
			transReport.setCustomerName(doc.getString(FieldType.CUST_NAME.toString()));
			BigInteger txnId = new BigInteger(doc.getString(FieldType.TXN_ID.toString()));
			transReport.setTransactionId(txnId);
			transReport.setTransactionIdString(String.valueOf(txnId));
			transReport.setDateFrom(doc.getString(FieldType.CREATE_DATE.getName()));
			transReport.setInternalCardIssusserBank(doc.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.toString()));
			transReport
					.setInternalCardIssusserCountry(doc.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.toString()));
			if (null != doc.getString(FieldType.CURRENCY_CODE.toString())) {
				transReport.setCurrency(
						propertiesManager.getAlphabaticCurrencyCode(doc.getString(FieldType.CURRENCY_CODE.toString())));
			} else {
				transReport.setCurrency(CrmFieldConstants.NA.getValue());
			}
			transReport.setPayId(doc.getString(FieldType.PAY_ID.toString()));
			transReport.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.toString()));
			transReport.setPaymentMethods(PaymentType.getpaymentName(doc.getString(FieldType.PAYMENT_TYPE.toString())));
			transReport.setTransactionCaptureDate(doc.getString(FieldType.PG_DATE_TIME.toString()));
			transReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
			String surchargeFlag = doc.getString(FieldType.SURCHARGE_FLAG.toString());
			transReport.setSurchargeFlag(surchargeFlag);
			if (!StringUtils.isBlank(surchargeFlag)) {
				if (surchargeFlag.equalsIgnoreCase("Y")) {
					transReport.setAmount(doc.getString(FieldType.TOTAL_AMOUNT.toString()));

				} else {
					transReport.setAmount(doc.getString(FieldType.AMOUNT.toString()));
				}
			} else {
				transReport.setAmount(doc.getString(FieldType.AMOUNT.toString()));
			}
			transReport.setAcquirerType(doc.getString(FieldType.ACQUIRER_TYPE.toString()));
			transReport.setMopType(MopType.getmopName(doc.getString(FieldType.MOP_TYPE.toString())));
			transReport.setTxnType(doc.getString(FieldType.TXNTYPE.toString()));

			transReport.setCurrency(doc.getString(FieldType.CURRENCY_CODE.toString()));
			transactionList.add(transReport);
		}
		cursor.close();
		return transactionList;

	}

	public List<TransactionSearch> summaryReportMerchant(String fromDate, String toDate, String payId, String subMerchantPayId,
			String paymentType, String currency, User user, int start, int length, String transactionFlag) throws SystemException {

		double bankTdr = 0;
		double PgFixCharge = 0;
		double pgTdr = 0;
		double bankFixCharge = 0;
		double merchantFixCharge = 0;
		double merchantTdr = 0;
		double merchantServiceTax = 0;

		TdrPojo tdrPojo = new TdrPojo();
		List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject currencyQuery = new BasicDBObject();
		BasicDBObject acquirerQuery = new BasicDBObject();
		BasicDBObject allParamQuery = new BasicDBObject();
		BasicDBObject txnCapturedFlag = new BasicDBObject();
		List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> userTypeLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> paymentTypeConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject paymentTypeQuery = new BasicDBObject();

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
		
		if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
		}
		if (StringUtils.isNotBlank(transactionFlag) && !transactionFlag.equalsIgnoreCase("ALL")) {
			 txnCapturedFlag.append("$in", transactionFlag.split(","));
	            paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), txnCapturedFlag));
		}
		if (!currency.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
		} else {
			PropertiesManager propertiesManager = new PropertiesManager();
			Map<String, String> allCurrencyMap;
			allCurrencyMap = propertiesManager.getAllProperties(alphabaticFileName);
			for (Map.Entry<String, String> entry : allCurrencyMap.entrySet()) {

				currencyConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), entry.getKey()));
			}

			currencyQuery.append("$or", currencyConditionLst);
		}

		if (!paymentType.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
		} else {
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
			paymentTypeConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), null));
			paymentTypeQuery.append("$or", paymentTypeConditionLst);
		}

		if (!paramConditionLst.isEmpty()) {
			allParamQuery = new BasicDBObject("$and", paramConditionLst);
		}

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

		BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
		List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

		if (!allParamQuery.isEmpty()) {
			fianlList.add(allParamQuery);
		}
		if (!allConditionQueryObj.isEmpty()) {
			fianlList.add(allConditionQueryObj);
		}

		BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		BasicDBObject match = new BasicDBObject("$match", finalquery);

		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		BasicDBObject skip = new BasicDBObject("$skip", start);
		BasicDBObject limit = new BasicDBObject("$limit", length);

		// run aggregation

		List<BasicDBObject> pipeline = Arrays.asList(match, sort, skip, limit);
		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
		// create our pipeline operations, first with the $match
		// MongoCursor<Document> cursor = coll.find(finalquery).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				doc = dataEncDecTool.decryptDocument(doc);
			}
			
			TransactionSearch transReport = new TransactionSearch();
			transReport.setCustomerName(doc.getString(FieldType.CUST_NAME.toString()));
			BigInteger txnId = new BigInteger(doc.getString(FieldType.TXN_ID.toString()));
			transReport.setTransactionId(txnId);
			transReport.setTransactionIdString(String.valueOf(txnId));
			transReport.setDateFrom(doc.getString(FieldType.CREATE_DATE.getName()));
			transReport.setInternalCardIssusserBank(doc.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.toString()));
			transReport
					.setInternalCardIssusserCountry(doc.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.toString()));
			if (null != doc.getString(FieldType.CURRENCY_CODE.toString())) {
				transReport.setCurrency(
						propertiesManager.getAlphabaticCurrencyCode(doc.getString(FieldType.CURRENCY_CODE.toString())));
			} else {
				transReport.setCurrency(CrmFieldConstants.NA.getValue());
			}
			transReport.setPayId(doc.getString(FieldType.PAY_ID.toString()));
			
			if(doc.containsKey(FieldType.SUB_MERCHANT_ID.getName()) && StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
				transReport.setSubMerchantId(doc.getString(FieldType.SUB_MERCHANT_ID.getName()));
			}
			transReport.setTxnSettledType(doc.getString(FieldType.TXN_CAPTURE_FLAG.toString()));
			transReport.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.toString()));
			transReport.setPaymentMethods(PaymentType.getpaymentName(doc.getString(FieldType.PAYMENT_TYPE.toString())));
			transReport.setTransactionCaptureDate(doc.getString(FieldType.PG_DATE_TIME.toString()));
			transReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
			String surchargeFlag = doc.getString(FieldType.SURCHARGE_FLAG.toString());
			transReport.setSurchargeFlag(surchargeFlag);
			if (!StringUtils.isBlank(surchargeFlag)) {
				if (surchargeFlag.equalsIgnoreCase("Y")) {
					transReport.setAmount(doc.getString(FieldType.TOTAL_AMOUNT.toString()));

				} else {
					transReport.setAmount(doc.getString(FieldType.AMOUNT.toString()));
				}
			} else {
				transReport.setAmount(doc.getString(FieldType.AMOUNT.toString()));
			}
			transReport.setAcquirerType(doc.getString(FieldType.ACQUIRER_TYPE.toString()));
			transReport.setMopType(MopType.getmopName(doc.getString(FieldType.MOP_TYPE.toString())));
			transReport.setTxnType(doc.getString(FieldType.TXNTYPE.toString()));
			transReport.setCurrency(doc.getString(FieldType.CURRENCY_CODE.toString()));
			transactionList.add(transReport);
		}

		return transactionList;

	}

	public List<SummaryReportObject> summaryReportDownload(String fromDate, String toDate, String payId, String subMerchantPayId,
			String paymentType, String acquirer, String currency, User user, String paymentsRegion,
			String cardHolderType, String pgRefNum, String mopType, String transactionType, String partSettleFlag, String transactionFlag) throws SystemException {
		List<SummaryReportObject> transactionList = new ArrayList<SummaryReportObject>();

		logger.info("Inside SummaryReportQuery summaryReportDownload");
		Map<String, User> userMap = new HashMap<String, User>();
		List<SUFDetail> sufCharge = new ArrayList<SUFDetail>();
	try {	
			sufCharge = sufDetailDao.getAllActiveSufDetails();
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject acquirerQuery = new BasicDBObject();
			BasicDBObject txnCapturedFlag = new BasicDBObject();

			BasicDBObject allParamQuery = new BasicDBObject();
			List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> saleOrAuthList = new ArrayList<BasicDBObject>();
			
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

			for (String dateIndex : allDatesIndex) {
				dateIndexConditionList.add(new BasicDBObject("DATE_INDEX", dateIndex));
			}
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndexConditionQuery.append("$or", dateIndexConditionList);
			}

			if (!payId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
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

			if (!currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}
			if (!partSettleFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
			}
			if (StringUtils.isNotBlank(transactionFlag) && !transactionFlag.equalsIgnoreCase("ALL")) {
				 txnCapturedFlag.append("$in", transactionFlag.split(","));
		            paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), txnCapturedFlag));
			}
			if (!acquirer.equalsIgnoreCase("ALL")) {

				List<String> acquirerList = Arrays.asList(acquirer.split(","));
				for (String acq : acquirerList) {

					acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acq.trim()));
				}
				acquirerQuery.append("$or", acquirerConditionLst);

			}
			if (!paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (transactionType.equalsIgnoreCase("ALL")) {

				List<BasicDBObject> saleConditionList = new ArrayList<BasicDBObject>();
				saleConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
				saleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleConditionList);

				List<BasicDBObject> authConditionList = new ArrayList<BasicDBObject>();
				authConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.AUTHORISE.getName()));
				authConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.APPROVED.getName()));

				BasicDBObject authConditionQuery = new BasicDBObject("$and", authConditionList);

				List<BasicDBObject> recoConditionList = new ArrayList<BasicDBObject>();
				recoConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
				recoConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject recoConditionQuery = new BasicDBObject("$and", recoConditionList);

				List<BasicDBObject> refundConditionList = new ArrayList<BasicDBObject>();
				refundConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
				refundConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundConditionList);

				List<BasicDBObject> recoRefundConditionList = new ArrayList<BasicDBObject>();
				recoRefundConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
				recoRefundConditionList
						.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject recoRefundConditionQuery = new BasicDBObject("$and", recoRefundConditionList);

				saleOrAuthList.add(saleConditionQuery);
				saleOrAuthList.add(authConditionQuery);
				saleOrAuthList.add(recoConditionQuery);
				saleOrAuthList.add(refundConditionQuery);
				saleOrAuthList.add(recoRefundConditionQuery);

			} else if (transactionType.equalsIgnoreCase("SALE")) {

				List<BasicDBObject> saleConditionList = new ArrayList<BasicDBObject>();
				saleConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
				saleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
				saleConditionList
						.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));

				BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleConditionList);

				List<BasicDBObject> authConditionList = new ArrayList<BasicDBObject>();
				authConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.AUTHORISE.getName()));
				authConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.APPROVED.getName()));

				BasicDBObject authConditionQuery = new BasicDBObject("$and", authConditionList);

				List<BasicDBObject> recoConditionList = new ArrayList<BasicDBObject>();
				recoConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
				recoConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject recoConditionQuery = new BasicDBObject("$and", recoConditionList);

				saleOrAuthList.add(saleConditionQuery);
				saleOrAuthList.add(authConditionQuery);
				saleOrAuthList.add(recoConditionQuery);

			} else {

				List<BasicDBObject> refundConditionList = new ArrayList<BasicDBObject>();
				refundConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
				refundConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundConditionList);

				List<BasicDBObject> recoRefundConditionList = new ArrayList<BasicDBObject>();
				recoRefundConditionList
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
				recoRefundConditionList
						.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				BasicDBObject recoRefundConditionQuery = new BasicDBObject("$and", recoRefundConditionList);

				saleOrAuthList.add(refundConditionQuery);
				saleOrAuthList.add(recoRefundConditionQuery);

			}

			BasicDBObject authndSaleConditionQuery = new BasicDBObject("$or", saleOrAuthList);

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

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
			if (!authndSaleConditionQuery.isEmpty()) {
				allConditionQueryList.add(authndSaleConditionQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryList.add(dateIndexConditionQuery);
			}
			
			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			logger.info("finalquery for downloadSummaryReport = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					doc = dataEncDecTool.decryptDocument(doc);
				}
				
				SummaryReportObject transReport = new SummaryReportObject();
				transReport.setTransactionId(doc.getString(FieldType.TXN_ID.toString()));
				transReport.setDateFrom(doc.getString(FieldType.CREATE_DATE.getName()));
				if (null != doc.getString(FieldType.CURRENCY_CODE.toString())) {
					transReport.setCurrency(propertiesManager
							.getAlphabaticCurrencyCode(doc.getString(FieldType.CURRENCY_CODE.toString())));
				} else {
					transReport.setCurrency(CrmFieldConstants.NA.getValue());
				}
				transReport.setPayId(doc.getString(FieldType.PAY_ID.toString()));
				
				/*if(doc.containsKey(FieldType.SUB_MERCHANT_ID.getName()) && StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
					
					transReport.setSubMerchantId(doc.getString(FieldType.SUB_MERCHANT_ID.getName()));
				}*/
				
				if (!payId.equalsIgnoreCase("All") && doc.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
					
					String subMerchant = doc.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					transReport.setSubMerchantId(subMerchantUser.getBusinessName());
				}
				if (StringUtils.isNotBlank(doc.getString(FieldType.REFUND_ORDER_ID.toString()))) {
					transReport.setRefundOrderId(doc.getString(FieldType.REFUND_ORDER_ID.toString()));
				} else {
					transReport.setRefundOrderId(CrmFieldConstants.NA.getValue());
				}
				
				transReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
				String surchargeFlag = doc.getString(FieldType.SURCHARGE_FLAG.toString());
				if (StringUtils.isNotBlank(surchargeFlag)) {
					transReport.setSurchargeFlag(surchargeFlag);
				}

				transReport.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.toString()));
				if (StringUtils.isBlank(doc.getString(FieldType.PAYMENTS_REGION.toString()))) {

					transReport.setTransactionRegion(AccountCurrencyRegion.DOMESTIC.toString());
				} else {
					transReport.setTransactionRegion(doc.getString(FieldType.PAYMENTS_REGION.toString()));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {

					transReport.setCardHolderType(CardHolderType.CONSUMER.toString());
				} else {
					transReport.setCardHolderType(doc.getString(FieldType.CARD_HOLDER_TYPE.toString()));
				}

				transReport.setTotalAmount(doc.getString(FieldType.TOTAL_AMOUNT.toString()));
				transReport.setAcquirerType(doc.getString(FieldType.ACQUIRER_TYPE.toString()));
				
				if (StringUtils.isNotBlank(doc.getString(FieldType.PART_SETTLE.toString()))
						&& doc.getString(FieldType.PART_SETTLE.toString()).equalsIgnoreCase(("Y"))) {
					transReport.setPartSettledFlag(doc.getString(FieldType.PART_SETTLE.toString()));
				} else {
					transReport.setPartSettledFlag(CrmFieldConstants.NA.getValue());
				}
				
//				transReport.setPartSettledFlag(doc.getString(FieldType.PART_SETTLE.toString()));

				if (null != doc.getString(FieldType.MOP_TYPE.toString())) {
					transReport.setMopType(MopType.getmopName(doc.getString(FieldType.MOP_TYPE.toString())));
				} else {
					transReport.setMopType(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}

				if (null != doc.getString(FieldType.PAYMENT_TYPE.toString())) {
					transReport.setPaymentMethods(
							PaymentType.getpaymentName(doc.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transReport.setPaymentMethods(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				transReport.setTxnSettledType(doc.getString(FieldType.TXN_CAPTURE_FLAG.toString()));
				transReport.setCaptureDate(doc.getString(FieldType.PG_DATE_TIME.toString()));
				transReport.setTxnType(doc.getString(FieldType.ORIG_TXNTYPE.toString()));
				transReport.setMerchants(doc.getString(CrmFieldType.BUSINESS_NAME.getName()));
				transReport.setAcqId(doc.getString(FieldType.ACQ_ID.toString()));
				if(StringUtils.isNotBlank(doc.getString(FieldType.RRN.getName()))) {
					transReport.setRrn(doc.getString(FieldType.RRN.toString()));
				} else {
					transReport.setRrn(CrmFieldConstants.NA.getValue());
				}
				
				if (StringUtils.isNotBlank(doc.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
					transReport.setTxnSettledType(doc.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
				} else {
					transReport.setTxnSettledType(CrmFieldConstants.NA.getValue());
				}
				//transReport.setPostSettledFlag(doc.getString(FieldType.POST_SETTLED_FLAG.toString()));
				transReport.setDeltaRefundFlag(doc.getString(FieldType.UDF6.toString()));
				transReport.setDateFrom(doc.getString(FieldType.CREATE_DATE.toString()));
				transReport.setAcquirerMode(doc.getString(FieldType.ACQUIRER_MODE.getName()));

				if (doc.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.SALE.getName())) {
					
					String sufCharges = String.valueOf(getSufCharge(doc.getString(FieldType.PAY_ID.getName()), doc.getString(FieldType.ORIG_TXNTYPE.getName()),
							PaymentType.getpaymentName(doc.getString(FieldType.PAYMENT_TYPE.getName())), MopType.getmopName(doc.getString(FieldType.MOP_TYPE.getName())), 
							doc.getString(FieldType.AMOUNT.getName()), doc.getString(FieldType.PAYMENTS_REGION.getName()), sufCharge));
					if(StringUtils.isBlank(sufCharges) || sufCharges.equalsIgnoreCase("0.000")) {
						transReport.setSufCharge("");
					} else {
					
					transReport.setSufCharge("-"+sufCharges);
					}
					
					transReport.setTdrScAcquirer(
							String.valueOf(Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))));
					transReport.setGstScAcquirer(
							String.valueOf(Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString()))));

					transReport.setTdrScPaymentGateway(
							String.valueOf(Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))));
					transReport.setGstScPaymentGateway(
							String.valueOf(Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))));
					
					if(doc.containsKey(FieldType.RESELLER_CHARGES.getName()) && StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName()))) { 
						transReport.setResellerCharges(doc.getString(FieldType.RESELLER_CHARGES.getName()));
					} else {
						transReport.setResellerCharges("0.00");
					}
					
					if(doc.containsKey(FieldType.RESELLER_GST.getName()) && StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_GST.getName()))) { 
						transReport.setResellerGst(doc.getString(FieldType.RESELLER_GST.getName()));
					} else {
						transReport.setResellerGst("0.00");
					}

					if (doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode())) {

						transReport.setMerchantAmount("-" + String.format("%.2f",
								(Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString()))
										+ Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))
										+ Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))
										+ Double.parseDouble(transReport.getResellerCharges())
										+ Double.parseDouble(transReport.getResellerGst()))));

					} else {

						transReport.setMerchantAmount(String.format("%.2f",
								(Double.parseDouble(doc.getString(FieldType.TOTAL_AMOUNT.toString()))
										- (Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString()))
												+ Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))
												+ Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))
												+ Double.parseDouble(transReport.getResellerCharges())
												+ Double.parseDouble(transReport.getResellerGst())))));
					}
				} else if (doc.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName())) {

					transReport.setSufCharge(String.format("%.3f", getSufCharge(doc.getString(FieldType.PAY_ID.getName()), doc.getString(FieldType.ORIG_TXNTYPE.getName()),
							PaymentType.getpaymentName(doc.getString(FieldType.PAYMENT_TYPE.getName())),MopType.getmopName(doc.getString(FieldType.MOP_TYPE.getName())), 
							doc.getString(FieldType.AMOUNT.getName()), doc.getString(FieldType.PAYMENTS_REGION.getName()), sufCharge)));
					
					transReport.setTotalAmount(String.format("%.2f",
							Double.parseDouble(doc.getString(FieldType.TOTAL_AMOUNT.toString())) * -1));
					transReport.setTdrScAcquirer("0.00");
					transReport.setGstScAcquirer("0.00");
					transReport.setTdrScPaymentGateway("0.00");
					transReport.setGstScPaymentGateway("0.00");
					transReport.setResellerCharges("0.00");
					transReport.setResellerGst("0.00");
					

					if (doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode())) {

						transReport.setMerchantAmount("0.00");

					} else {
						transReport.setMerchantAmount(String.format("%.2f",
								Double.parseDouble(doc.getString(FieldType.TOTAL_AMOUNT.toString())) * -1));

					}

				}

				if (userMap.get(doc.getString(FieldType.PAY_ID.toString())) != null) {
					User userThis = userMap.get(doc.getString(FieldType.PAY_ID.toString()));
					transReport.setMerchants(userThis.getBusinessName());
				} else {
					User userThis = userdao.findPayId(doc.getString(FieldType.PAY_ID.toString()));
					transReport.setMerchants(userThis.getBusinessName());
					userMap.put(userThis.getPayId(), userThis);
				}
				
				//Merchant TDR/SC and Merhcant GST
				if(doc.containsKey(FieldType.MERCHANT_TDR_SC.getName())
						&& doc.containsKey(FieldType.MERCHANT_GST.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.MERCHANT_TDR_SC.getName()))
						&& StringUtils.isNotBlank(doc.getString(FieldType.MERCHANT_GST.getName()))) {
					
					transReport.setMerchantTdrOrSc(doc.getString(FieldType.MERCHANT_TDR_SC.getName()));
					transReport.setMerchantGst(doc.getString(FieldType.MERCHANT_GST.getName()));
				
				} else if(doc.containsKey(FieldType.PG_TDR_SC.getName())
							&& doc.containsKey(FieldType.PG_GST.getName())
							&& doc.containsKey(FieldType.ACQUIRER_TDR_SC.getName())
							&& doc.containsKey(FieldType.ACQUIRER_GST.getName())
							&& doc.containsKey(FieldType.RESELLER_CHARGES.getName())
							&& doc.containsKey(FieldType.RESELLER_GST.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.PG_TDR_SC.getName()))
							&& StringUtils.isNotBlank(doc.getString(FieldType.PG_GST.getName()))
							&& StringUtils.isNotBlank(doc.getString(FieldType.ACQUIRER_TDR_SC.getName()))
							&& StringUtils.isNotBlank(doc.getString(FieldType.ACQUIRER_GST.getName()))
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName()))
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_GST.getName()))) {
					
					transReport.setMerchantTdrOrSc(String.format("%.2f", ((Double
							.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString())))
							+ (Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString())))
							+ (Double.parseDouble(doc.getString(FieldType.RESELLER_CHARGES.toString()))))));
					
					transReport.setMerchantGst(String.format("%.2f", ((Double
							.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString())))
							+ (Double.parseDouble(doc.getString(FieldType.PG_GST.toString())))
							+ (Double.parseDouble(doc.getString(FieldType.RESELLER_GST.toString()))))));
					
				} else {
					
					transReport.setMerchantTdrOrSc(String.format("%.2f", ((Double
							.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString())))
							+ (Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))))));
					
					transReport.setMerchantGst(String.format("%.2f", ((Double
							.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString())))
							+ (Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))))));
					
				}
				
				transactionList.add(transReport);
			}

			cursor.close();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return transactionList;
	}

	public List<ChargingDetails> ChargingDetailsReport() {
		List<ChargingDetails> chDetails = null;
		return chDetails = userdao.findChargingDetail();
	}

	public List<TransactionSearch> downloadSettlementReport(String merchantPayId, String currency, String saleDate,
			User user, String acquirer) {
		logger.info("Inside TxnReports , searchPayment");
		Map<String, User> userMap = new HashMap<String, User>();
		// boolean isParameterised = false;
		try {
			List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();

			PropertiesManager propManager = new PropertiesManager();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject acquirerQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			if (!saleDate.isEmpty()) {
				String currentDate = null;

				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				// add days to from date
				Date date1 = dateFormat.parse(saleDate);
				cal.setTime(date1);
				cal.add(Calendar.DATE, 1);
				currentDate = dateFormat.format(cal.getTime());

				dateQuery.put(FieldType.PG_DATE_TIME.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(saleDate).toLocalizedPattern())
								.add("$lt", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));

			if (!currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			if (!acquirer.equalsIgnoreCase("ALL")) {
				List<String> acquirerList = Arrays.asList(acquirer.split(","));
				for (String acq : acquirerList) {

					acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acq));
				}
				acquirerQuery.append("$or", acquirerConditionLst);

			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}

			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			if (!acquirerQuery.isEmpty()) {
				fianlList.add(acquirerQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside Settlement Reprort , searchPayment , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = coll.find(finalquery).iterator();
			while (cursor.hasNext()) {
				Document doc = cursor.next();

				TransactionSearch transReport = new TransactionSearch();
				transReport.setPgRefNum(((Document) doc).getString(FieldType.PG_REF_NUM.toString()));
				transReport.setAmount(doc.getString(FieldType.AMOUNT.toString()));
				transReport
						.setDateFrom(DateCreater.formatSaleDateTime(doc.getString(FieldType.PG_DATE_TIME.getName())));
				transReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
				transReport.setPaymentMethods(
						PaymentType.getpaymentName(doc.getString(FieldType.PAYMENT_TYPE.toString())));
				transactionList.add(transReport);
			}
			cursor.close();
			logger.info("Inside Download Settlement Reports , transactionListSize = " + transactionList.size());
			return transactionList;
		}

		catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPayment , Exception = " , e);
			return null;
		}
	}
	
	public BigDecimal getSufCharge(String payId, String txnType, String paymentType, String mopType, String amount, String paymentRegion,
			List<SUFDetail> sufCharge) {
		try {

			BigDecimal fixedCharge = null;
			BigDecimal percentageCharge = null;
			for (SUFDetail suf : sufCharge) {

				if (suf.getPayId().equalsIgnoreCase(payId) && suf.getTxnType().equalsIgnoreCase(txnType)
						&& suf.getPaymentType().equalsIgnoreCase(paymentType)
						&& suf.getPaymentRegion().equalsIgnoreCase(paymentRegion)
						&& suf.getMopType().equalsIgnoreCase(mopType)) {

					fixedCharge = new BigDecimal(suf.getFixedCharge());
					/*percentageCharge = String.format("%.3f",
							(new BigDecimal(suf.getPercentageAmount())
									.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
									.multiply(new BigDecimal(amount))));*/
					percentageCharge = (new BigDecimal(suf.getPercentageAmount())
							.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
							.multiply(new BigDecimal(amount))).setScale(3, RoundingMode.FLOOR);
					break;
				}
			}
			if (fixedCharge == null && percentageCharge == null) {
				fixedCharge = new BigDecimal("0.000");
				percentageCharge = new BigDecimal("0.000");
			} else if (fixedCharge == null && !(percentageCharge == null)) {
				fixedCharge = new BigDecimal("0.000");
			} else if (!(fixedCharge == null) && percentageCharge == null) {
				percentageCharge = new BigDecimal("0.000");
			}
			return fixedCharge.add(percentageCharge);
		} catch (Exception ex) {
			logger.error("exception caught while calculate suf charges for summary report : ", ex);
			return null;
		}
	}
}