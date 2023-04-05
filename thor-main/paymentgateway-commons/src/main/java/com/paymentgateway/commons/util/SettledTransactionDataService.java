package com.paymentgateway.commons.util;

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
import com.paymentgateway.commons.dao.SettledTransactionDataDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.SettledTransactionDataObject;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;

@Service
public class SettledTransactionDataService {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userdao;

	@Autowired
	private SettledTransactionDataDao settledTransactionDataDao;

	@Autowired
	private SurchargeDao surchargeDao;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;

	@Autowired
	PropertiesManager propertiesManager;
	private static Logger logger = LoggerFactory.getLogger(SettledTransactionDataService.class.getName());
	private static final String prefix = "MONGO_DB_";

	public void uploadSettledData(String fromDate, String toDate) {

		try {
			Map<String, User> userMap = new HashMap<String, User>();

			List<Surcharge> surchargeList = new ArrayList<Surcharge>();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
			try {

				Date date1 = format.parse(fromDate);
				Date date2 = format.parse(toDate);

				if (surchargeDao.findAllSurchargeByDate(date1, date2) == null) {
					logger.info("No surcharge data found");
				} else {
					surchargeList = surchargeDao.findAllSurchargeByDate(date1, date2);
				}

			} catch (Exception e) {
				logger.error("Exception 1 " , e);
			}

			List<SettledTransactionDataObject> settledTransactionDataList = new ArrayList<SettledTransactionDataObject>();

			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject acquirerQuery = new BasicDBObject();

			BasicDBObject allParamQuery = new BasicDBObject();
			List<BasicDBObject> saleOrAuthList = new ArrayList<BasicDBObject>();

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

			List<BasicDBObject> saleConditionList = new ArrayList<BasicDBObject>();
			saleConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			// saleConditionList.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(),
			// ErrorType.SUCCESS.getCode()));

			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleConditionList);

			List<BasicDBObject> authConditionList = new ArrayList<BasicDBObject>();
			authConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.AUTHORISE.getName()));
			authConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.APPROVED.getName()));

			BasicDBObject authConditionQuery = new BasicDBObject("$and", authConditionList);

			List<BasicDBObject> recoConditionList = new ArrayList<BasicDBObject>();
			recoConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			recoConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

			BasicDBObject recoConditionQuery = new BasicDBObject("$and", recoConditionList);

			List<BasicDBObject> refundConditionList = new ArrayList<BasicDBObject>();
			refundConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
			refundConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

			BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundConditionList);

			List<BasicDBObject> recoRefundConditionList = new ArrayList<BasicDBObject>();
			recoRefundConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
			recoRefundConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

			BasicDBObject recoRefundConditionQuery = new BasicDBObject("$and", recoRefundConditionList);

			saleOrAuthList.add(saleConditionQuery);
			saleOrAuthList.add(authConditionQuery);
			saleOrAuthList.add(recoConditionQuery);
			saleOrAuthList.add(refundConditionQuery);
			saleOrAuthList.add(recoRefundConditionQuery);

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

			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			logger.info("finalquery for settlement data upload = " + finalquery);
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

				if (!StringUtils.isBlank(doc.getString(FieldType.SURCHARGE_FLAG.getName()))
						&& doc.getString(FieldType.SURCHARGE_FLAG.getName()).equalsIgnoreCase("Y")) {
					SettledTransactionDataObject settledTransactionData = new SettledTransactionDataObject();

					settledTransactionData.set_id(doc.getString(FieldType.TXN_ID.getName()));
					settledTransactionData.setTransactionId(doc.getString(FieldType.TXN_ID.getName()));
					settledTransactionData.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.getName()));

					if (StringUtils.isBlank(doc.getString(FieldType.PAYMENTS_REGION.getName()))) {
						settledTransactionData.setTransactionRegion(AccountCurrencyRegion.DOMESTIC.name());
					} else {
						settledTransactionData.setTransactionRegion(doc.getString(FieldType.PAYMENTS_REGION.getName()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.POST_SETTLED_FLAG.getName()))) {
						settledTransactionData.setPostSettledFlag("");
					} else {
						settledTransactionData.setPostSettledFlag(doc.getString(FieldType.POST_SETTLED_FLAG.getName()));
					}

					settledTransactionData.setTxnType(doc.getString(FieldType.TXNTYPE.getName()));
					settledTransactionData.setAcquirerType(doc.getString(FieldType.ACQUIRER_TYPE.getName()));
					settledTransactionData.setPaymentMethods(doc.getString(FieldType.PAYMENT_TYPE.getName()));
					settledTransactionData.setCreateDate(doc.getString(FieldType.CREATE_DATE.getName()));
					settledTransactionData.setOrderId(doc.getString(FieldType.ORDER_ID.getName()));
					settledTransactionData.setPayId(doc.getString(FieldType.PAY_ID.getName()));
					settledTransactionData.setMopType(doc.getString(FieldType.MOP_TYPE.getName()));
					settledTransactionData.setCurrency(doc.getString(FieldType.CURRENCY_CODE.getName()));
					settledTransactionData.setStatus(doc.getString(FieldType.STATUS.getName()));

					if (StringUtils.isBlank(doc.getString(FieldType.CARD_HOLDER_TYPE.getName()))) {
						settledTransactionData.setCardHolderType(CardHolderType.CONSUMER.name());
					} else {
						settledTransactionData.setCardHolderType(doc.getString(FieldType.CARD_HOLDER_TYPE.getName()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.PG_DATE_TIME.getName()))) {
						settledTransactionData.setCaptureDate("");
					} else {
						settledTransactionData.setCaptureDate(doc.getString(FieldType.PG_DATE_TIME.getName()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.RRN.getName()))) {
						settledTransactionData.setRrn("");
					} else {
						settledTransactionData.setRrn(doc.getString(FieldType.RRN.getName()));
					}

					settledTransactionData.setAcqId(doc.getString(FieldType.ACQ_ID.getName()));

					if (StringUtils.isBlank(doc.getString(FieldType.ARN.getName()))) {
						settledTransactionData.setArn("");
					} else {
						settledTransactionData.setArn(doc.getString(FieldType.ARN.getName()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.UDF6.getName()))) {
						settledTransactionData.setDeltaRefundFlag("");
					} else {
						settledTransactionData.setDeltaRefundFlag(doc.getString(FieldType.UDF6.getName()));
					}

					settledTransactionData.setSurchargeFlag(doc.getString(FieldType.SURCHARGE_FLAG.getName()));

					if (StringUtils.isBlank(doc.getString(FieldType.REFUND_FLAG.getName()))) {
						settledTransactionData.setRefundFlag("");
					} else {
						settledTransactionData.setRefundFlag(doc.getString(FieldType.REFUND_FLAG.getName()));
					}

					settledTransactionData.setAmount(Double.valueOf(doc.getString(FieldType.AMOUNT.getName())));
					settledTransactionData
							.setTotalAmount(Double.valueOf(doc.getString(FieldType.TOTAL_AMOUNT.getName())));

					BigDecimal amount = new BigDecimal(doc.getString(FieldType.AMOUNT.getName()));
					BigDecimal totalAmount = new BigDecimal(doc.getString(FieldType.TOTAL_AMOUNT.getName()));
					BigDecimal totalSurcharge = totalAmount.subtract(amount);

					if (totalAmount.equals(amount)) {

						settledTransactionData.setTdrScAcquirer(Double.valueOf(0));
						settledTransactionData.setGstScAcquirer(Double.valueOf(0));

						settledTransactionData.setTdrScPg(Double.valueOf(0));
						settledTransactionData.setGstScPg(Double.valueOf(0));

						settledTransactionData.setTdrScPaymentGateway(Double.valueOf(0));
						settledTransactionData.setGstScPaymentGateway(Double.valueOf(0));

					} else {
						String payId = doc.getString(FieldType.PAY_ID.getName());
						if (StringUtils.isBlank(payId)) {
							logger.info("Pay Id not present for " + doc.getString(FieldType.TXN_ID.getName()));
							continue;
						}

						User user = null;
						if (userMap.get(payId) != null && !userMap.get(payId).getPayId().equals("")) {
							user = userMap.get(payId);
						} else {
							user = userdao.findPayId(payId);
							userMap.put(payId, user);
						}

						if (user == null) {
							logger.info("User not found for txn Id " + doc.getString(FieldType.TXN_ID.getName()));
							continue;
						}

						BigDecimal st = null;

						st = new BigDecimal(PropertiesManager.propertiesMap.get("SERVICE_TAX"));
						st = st.setScale(2, RoundingMode.HALF_DOWN);

						Date surchargeStartDate = null;
						Date surchargeEndDate = null;
						Date settlementDate = null;
						Surcharge surcharge = new Surcharge();

						try {
							for (Surcharge surchargeData : surchargeList) {

								if (AcquirerType.getInstancefromName(surchargeData.getAcquirerName()).getCode()
										.equalsIgnoreCase(settledTransactionData.getAcquirerType())
										&& surchargeData.getPaymentType().getCode()
												.equalsIgnoreCase(settledTransactionData.getPaymentMethods())
										&& surchargeData.getMopType().getCode()
												.equalsIgnoreCase(settledTransactionData.getMopType())
										&& surchargeData.getPaymentsRegion().name()
												.equalsIgnoreCase(settledTransactionData.getTransactionRegion())
										&& surchargeData.getPayId().equalsIgnoreCase(payId)) {

									surchargeStartDate = format.parse(surchargeData.getCreatedDate().toString());
									surchargeEndDate = format.parse(surchargeData.getUpdatedDate().toString());
									if (surchargeStartDate.compareTo(surchargeEndDate) == 0) {
										surchargeEndDate = new Date();
									}

									settlementDate = format.parse(doc.getString(FieldType.CREATE_DATE.getName()));

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

							logger.info("Surcharge is null for payId = " + payId + " acquirer = "
									+ doc.getString(FieldType.ACQUIRER_TYPE.getName()) + " mop = "
									+ doc.getString(FieldType.MOP_TYPE.getName()) + "  paymentType = "
									+ doc.getString(FieldType.PAYMENT_TYPE.getName()) + "  paymentRegion = "
									+ doc.getString(FieldType.PAYMENTS_REGION.getName()) + "  date = "
									+ doc.getString(FieldType.CREATE_DATE.getName()));
							continue;
						}

						BigDecimal bankFixCharge = BigDecimal.ZERO;
						BigDecimal bankChargePr = BigDecimal.ZERO;

						if (settledTransactionData.getCardHolderType().equals(CardHolderType.COMMERCIAL.toString())) {

							bankFixCharge = surcharge.getBankSurchargeAmountCommercial();
							bankChargePr = surcharge.getBankSurchargePercentageCommercial();
						} else {
							bankFixCharge = surcharge.getBankSurchargeAmountCustomer();
							bankChargePr = surcharge.getBankSurchargePercentageCustomer();
						}

						BigDecimal acquirerSurcharge = amount.multiply(bankChargePr.divide(BigDecimal.valueOf(100)));
						acquirerSurcharge = acquirerSurcharge.add(bankFixCharge).setScale(2, RoundingMode.HALF_DOWN);
						BigDecimal acquirerGst = acquirerSurcharge.multiply(st.divide(BigDecimal.valueOf(100)))
								.setScale(2, RoundingMode.HALF_DOWN);
						BigDecimal totalAcquirerSurcharge = acquirerSurcharge.add(acquirerGst);

						BigDecimal totalPgSurcharge = totalSurcharge.subtract(totalAcquirerSurcharge);
						BigDecimal divisor = new BigDecimal("1")
								.add(st.divide(new BigDecimal("100"), 2, RoundingMode.HALF_DOWN));
						BigDecimal divisor2 = new BigDecimal("2");

						BigDecimal pgSurcharge = totalPgSurcharge.divide(divisor, 2, RoundingMode.HALF_DOWN);
						BigDecimal pgGst = totalPgSurcharge.subtract(pgSurcharge);

						pgGst = pgGst.divide(divisor2, 2, RoundingMode.HALF_DOWN);
						pgSurcharge = pgSurcharge.divide(divisor2, 2, RoundingMode.HALF_DOWN);

						settledTransactionData.setTdrScAcquirer(acquirerSurcharge.doubleValue());
						settledTransactionData.setGstScAcquirer(acquirerGst.doubleValue());

						settledTransactionData.setTdrScPg(pgSurcharge.doubleValue());
						settledTransactionData.setGstScPg(pgGst.doubleValue());

						settledTransactionData.setTdrScPaymentGateway(pgSurcharge.doubleValue());
						settledTransactionData.setGstScPaymentGateway(pgGst.doubleValue());
					}

					settledTransactionDataList.add(settledTransactionData);
				}

				else {
					logger.info("No surcharge based transaction for date range " + fromDate + " TO " + toDate);
				}
			}
			logger.info("Found " + settledTransactionDataList.size()
					+ " transactions in Payment Gateway Transactions for date range " + fromDate + " TO " + toDate);

			settledTransactionDataDao.insertTransaction(settledTransactionDataList);
		} catch (Exception e) {
			logger.error("Exception occured " , e);
		}
	}
}
