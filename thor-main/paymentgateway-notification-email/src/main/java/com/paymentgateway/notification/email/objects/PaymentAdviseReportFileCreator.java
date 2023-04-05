package com.paymentgateway.notification.email.objects;

import java.math.BigDecimal;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.MongoQueryException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.MerchantPaymentAdviseDownloadObject;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.PaymentSearchDownloadObject;
import com.paymentgateway.commons.user.TransactionSearchDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;

@Component
public class PaymentAdviseReportFileCreator {

	@Autowired
	private UserDao userDao;

	@Autowired
	@Qualifier("propertiesManager")
	private PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(PaymentAdviseReportFileCreator.class.getName());

	@SuppressWarnings({ "unchecked", "static-access" })
	public List<MerchantPaymentAdviseDownloadObject> merchantPaymentAdviseDownloadAutoSendForSale(String merchantPayId,
			String subMerchantPayId, String payoutDate, User user, String currency) {

		List<MerchantPaymentAdviseDownloadObject> paymentAdviseList = new ArrayList<MerchantPaymentAdviseDownloadObject>();

		List<String> merchantPayIdQueryList = new ArrayList<String>();
		BasicDBObject merchantPayIdQueryObject = null;
		BasicDBObject merchantPayIdQuery = new BasicDBObject();
		try {
			logger.info("Inside TxnReport, MerchantPaymentAdvice");
			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			String startTime = payoutDate + " 00:00:00";
			String endTime = payoutDate + " 23:59:59";

			dateQuery.put(FieldType.PAYOUT_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startTime).toLocalizedPattern())
							.add("$lt", new SimpleDateFormat(endTime).toLocalizedPattern()).get());

			if (merchantPayId.equalsIgnoreCase("All")) {
				
				if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
					List<Merchants> allMerchanList = userDao.getActiveMerchantList();

					for (Merchants merchant : allMerchanList) {
						merchantPayIdQueryList.add(merchant.getPayId());
					}
					merchantPayIdQueryObject = new BasicDBObject("$in", merchantPayIdQueryList);
					merchantPayIdQuery.append(FieldType.PAY_ID.getName(), merchantPayIdQueryObject);
				}

				if (merchantPayIdQueryObject != null) {
					finalList.add(merchantPayIdQuery);
				}
			} else {
				finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
				finalList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}

			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("All")) {
				finalList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}
			
			List<BasicDBObject> refundConditionQueryList = new ArrayList<BasicDBObject>();
			refundConditionQueryList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
			refundConditionQueryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			BasicDBObject refundConditionQueryObj = new BasicDBObject("$and", refundConditionQueryList);

			List<BasicDBObject> saleConditionQueryList = new ArrayList<BasicDBObject>();
			saleConditionQueryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			saleConditionQueryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			BasicDBObject saleConditionQueryObj = new BasicDBObject("$and", saleConditionQueryList);

			List<BasicDBObject> saleAndRefundConditionQueryList = new ArrayList<BasicDBObject>();
			saleAndRefundConditionQueryList.add(saleConditionQueryObj);
			saleAndRefundConditionQueryList.add(refundConditionQueryObj);
			BasicDBObject saleAndRefundConditionQueryObj = new BasicDBObject("$or", saleAndRefundConditionQueryList);

			if (!saleAndRefundConditionQueryObj.isEmpty()) {
				finalList.add(saleAndRefundConditionQueryObj);
			}
			if (!dateQuery.isEmpty()) {
				finalList.add(dateQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", finalList);

			logger.info("Inside TxnReports , searchPayment , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			Document firstGroup;
			firstGroup = new Document("_id", new Document("_id", "$_id"));

			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("PG_DATE_TIME", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document mydata = cursor.next();
				List<Document> courses = (List<Document>) mydata.get("entries");
				Document dbobj = courses.get(0);
				MerchantPaymentAdviseDownloadObject merchantPaymentReport = new MerchantPaymentAdviseDownloadObject();

				String mopType = dbobj.getString(FieldType.MOP_TYPE.getName());
				if (StringUtils.isBlank(mopType)) {
					mopType = "NA";
				}
				if (StringUtils.isNumeric(mopType)
						|| dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("WL")) {
					merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
				} else {
					switch (mopType) {
					case "VI":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "MC":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "RU":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "DN":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "CD":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "UP":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "WL":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "NB":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "NEFT":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;
					case "IMPS":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;
					case "RTGS":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;
					}
				}

				String paymentType = dbobj.getString(FieldType.PAYMENT_TYPE.getName());

				switch (paymentType) {
				case "NB":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;

				case "UP":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;

				case "WL":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;
				case "CD":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;

				case "CC":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;

				case "DC":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;

				case "PC":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;

				case "AD":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;

				case "DP":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;

				case "EX":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;

				case "RP":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;

				case "EM":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;

				case "NEFT":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;

				case "IMPS":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;

				case "RTGS":
					merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
					break;
				}

				// String paymentType = dbobj.getString(FieldType.PAYMENT_TYPE.getName());
				// merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.getName()))) {
					merchantPaymentReport.setPaymentRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));
				} else {
					merchantPaymentReport.setPaymentRegion(CrmFieldConstants.NA.getValue());
					logger.info("Inside txnReport for download payment advise Report and Payment Region set "
							+ CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_MODE.getName()))) {
					merchantPaymentReport.setAcquirerMode(dbobj.getString(FieldType.ACQUIRER_MODE.toString()));
				} else {
					merchantPaymentReport.setAcquirerMode(CrmFieldConstants.NA.getValue());
					logger.info("Inside txnReport for download payment advise report and acquirer mode set"
							+ CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))) {
					merchantPaymentReport.setGrossAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()));
				} else {
					merchantPaymentReport.setGrossAmount("0.00");
					logger.info("inside txnReport for download Payment Advise report and total amount set 0.00");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.AMOUNT.getName()))) {
					merchantPaymentReport.setBaseAmount(dbobj.getString(FieldType.AMOUNT.getName()));
				} else {
					merchantPaymentReport.setBaseAmount("0.00");
					logger.info("inside txnReport for download Payment Advise report and base amount set 0.00");
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SURCHARGE_FLAG.getName()))) {
					merchantPaymentReport.setSurcharge_flag(dbobj.getString(FieldType.SURCHARGE_FLAG.getName()));
				} else {
					merchantPaymentReport.setSurcharge_flag("0.00");
					logger.info("inside txnReport for download Payment Advise report and set surcharge flag");
				}

				merchantPaymentReport.setOrigTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()));

				if (dbobj.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.SALE.getName()) && (dbobj
						.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode()))) {
					if (user.getUserType().equals(UserType.RESELLER) && user.isPartnerFlag()) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setResellerCharges(String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))));

								merchantPaymentReport.setResellerGst(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName()))));

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName())))));
								merchantPaymentReport.setGst(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_GST.getName())))));

								merchantPaymentReport.setNetAmount("-" + String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))));

							}
						} else {
							merchantPaymentReport.setResellerCharges(String.valueOf(0.00));
							merchantPaymentReport.setResellerGst(String.valueOf(0.00));
							merchantPaymentReport.setTdr(String.valueOf(0.00));
							merchantPaymentReport.setGst(String.valueOf(0.00));
							merchantPaymentReport.setNetAmount(String.valueOf(0.00));
							logger.info(
									"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
						}
					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName())))));
								merchantPaymentReport.setGst(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.PG_RESELLER_GST.getName())))));

								merchantPaymentReport.setNetAmount("-" + String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.PG_RESELLER_GST.getName())))));

							} else {
								merchantPaymentReport.setTdr(String.valueOf(0.00));
								merchantPaymentReport.setGst(String.valueOf(0.00));
								merchantPaymentReport.setNetAmount(String.valueOf(0.00));
								logger.info(
										"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
							}

						} else {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))));
								merchantPaymentReport.setGst(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))));

								merchantPaymentReport.setNetAmount("-" + String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))));

							} else {
								merchantPaymentReport.setTdr(String.valueOf(0.00));
								merchantPaymentReport.setGst(String.valueOf(0.00));
								merchantPaymentReport.setNetAmount(String.valueOf(0.00));
								logger.info(
										"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
							}
						}
					}

				} else if (dbobj.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName())
						&& (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode()))) {

					merchantPaymentReport.setNetAmount("0.00");
					if (user.getUserType().equals(UserType.RESELLER) && user.isPartnerFlag()) {
						merchantPaymentReport.setResellerCharges("0.00");
						merchantPaymentReport.setResellerGst("0.00");
						merchantPaymentReport.setTdr("0.00");
						merchantPaymentReport.setGst("0.00");
					} else {
						merchantPaymentReport.setTdr("0.00");
						merchantPaymentReport.setGst("0.00");
					}

				} else {

					if (user.getUserType().equals(UserType.RESELLER) && user.isPartnerFlag()) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setResellerCharges(String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))));

								merchantPaymentReport.setResellerGst(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName()))));

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName())))));
								merchantPaymentReport.setGst(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_GST.getName())))));

								merchantPaymentReport.setNetAmount(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
												.subtract(new BigDecimal(merchantPaymentReport.getTdr()))
												.subtract(new BigDecimal(merchantPaymentReport.getGst()))));
							}
						} else {
							merchantPaymentReport.setResellerCharges("0.00");
							merchantPaymentReport.setResellerGst("0.00");
							merchantPaymentReport.setTdr("0.00");
							merchantPaymentReport.setGst("0.00");
							merchantPaymentReport.setNetAmount(
									String.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
											.subtract(new BigDecimal(merchantPaymentReport.getTdr()))
											.subtract(new BigDecimal(merchantPaymentReport.getGst()))));
						}

					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName())))));
								merchantPaymentReport.setGst(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.PG_RESELLER_GST.getName())))));

								merchantPaymentReport.setNetAmount(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
												.subtract(new BigDecimal(merchantPaymentReport.getTdr()))
												.subtract(new BigDecimal(merchantPaymentReport.getGst()))));
							} else {

								if (dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("RTGS")
										|| dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("IMPS")
										|| dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("NEFT")) {

									merchantPaymentReport.setTdr(String.valueOf(0.00));
									merchantPaymentReport.setGst(String.valueOf(0.00));
									merchantPaymentReport.setNetAmount(String.valueOf(
											new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))));

								} else {

									merchantPaymentReport.setTdr(String.valueOf(0.00));
									merchantPaymentReport.setGst(String.valueOf(0.00));
									merchantPaymentReport.setNetAmount(String.valueOf(0.00));
									logger.info(
											"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
								}
							}

						} else {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))));
								merchantPaymentReport.setGst(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))));

								merchantPaymentReport.setNetAmount(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
												.subtract(new BigDecimal(merchantPaymentReport.getTdr()))
												.subtract(new BigDecimal(merchantPaymentReport.getGst()))));
							} else {

								if (dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("RTGS")
										|| dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("IMPS")
										|| dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("NEFT")) {

									merchantPaymentReport.setTdr(String.valueOf(0.00));
									merchantPaymentReport.setGst(String.valueOf(0.00));
									merchantPaymentReport.setNetAmount(String.valueOf(
											new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))));

								} else {

									merchantPaymentReport.setTdr(String.valueOf(0.00));
									merchantPaymentReport.setGst(String.valueOf(0.00));
									merchantPaymentReport.setNetAmount(String.valueOf(0.00));
									logger.info(
											"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
								}
							}
						}
					}
				}
				merchantPaymentReport.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
				merchantPaymentReport.setOid(dbobj.getString(FieldType.OID.getName()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_HOLDER_TYPE.getName()))) {
					merchantPaymentReport.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.getName()));
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UTR_NO.getName()))) {
					merchantPaymentReport.setUtrNo(dbobj.getString(FieldType.UTR_NO.getName()));
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_DATE_TIME.getName()))) {
					merchantPaymentReport.setCreateDate(dbobj.getString(FieldType.PG_DATE_TIME.getName()));
				}
				paymentAdviseList.add(merchantPaymentReport);
			}
			cursor.close();
			return paymentAdviseList;
		} catch (MongoExecutionTimeoutException exception) {
			logger.error("mongo timeout exception caught while fetch data for payment advise report " , exception);
		} catch (MongoQueryException exception) {
			logger.error("mongoQuery Exception caught while fetch data for payment advise report " , exception);
		} catch (Exception exception) {
			logger.error("Exception caught while fetch data for payment advise report" , exception);
		}
		return paymentAdviseList;
	}

	public List<PaymentSearchDownloadObject> searchPaymentForDownload(String merchantPayId, String subMerchantPayId,
			String fromDate, String toDate, User user, String currency) {

		logger.info("Inside TxnReports , searchPayment");
		Map<String, User> userMap = new HashMap<String, User>();
		boolean isParameterised = false;

		List<PaymentSearchDownloadObject> transactionList = new ArrayList<PaymentSearchDownloadObject>();
		List<String> merchantPayIdQueryList = new ArrayList<String>();
		BasicDBObject merchantPayIdQueryObject = null;
		BasicDBObject merchantPayIdQuery = new BasicDBObject();
		try {

			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();
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

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			BasicDBObject dateIndex = new BasicDBObject();
			List<String> dateList = new ArrayList<String>();
			BasicDBObject dateIndexQuery = new BasicDBObject();
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

			for (String date : allDatesIndex) {
				dateList.add(date);
			}

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndex.append("$in", dateList);
			}

			if (!dateIndex.isEmpty()) {
				dateIndexQuery.append(FieldType.DATE_INDEX.getName(), dateIndex);
			}
			if (merchantPayId.equalsIgnoreCase("All")) {
				if (user.getUserType().equals(UserType.RESELLER)) {
					List<Merchants> resellerMerchanList = userDao.getResellerMerchantList(user.getResellerId());

					for (Merchants resellerMerchant : resellerMerchanList) {
						merchantPayIdQueryList.add(resellerMerchant.getPayId());
					}
					merchantPayIdQueryObject = new BasicDBObject("$in", merchantPayIdQueryList);
					merchantPayIdQuery.append(FieldType.PAY_ID.getName(), merchantPayIdQueryObject);
				}
				if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
					List<Merchants> allMerchanList = userDao.getActiveMerchantList();

					for (Merchants merchant : allMerchanList) {
						merchantPayIdQueryList.add(merchant.getPayId());
					}
					merchantPayIdQueryObject = new BasicDBObject("$in", merchantPayIdQueryList);
					merchantPayIdQuery.append(FieldType.PAY_ID.getName(), merchantPayIdQueryObject);
				}

				if (merchantPayIdQueryObject != null) {
					finalList.add(merchantPayIdQuery);
				}
			} else {
				finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
				finalList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				finalList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}
			List<BasicDBObject> refundConditionQueryList = new ArrayList<BasicDBObject>();
			refundConditionQueryList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
			refundConditionQueryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			BasicDBObject refundConditionQueryObj = new BasicDBObject("$and", refundConditionQueryList);

			List<BasicDBObject> saleConditionQueryList = new ArrayList<BasicDBObject>();
			saleConditionQueryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			saleConditionQueryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			BasicDBObject saleConditionQueryObj = new BasicDBObject("$and", saleConditionQueryList);

			List<BasicDBObject> saleAndRefundConditionQueryList = new ArrayList<BasicDBObject>();
			saleAndRefundConditionQueryList.add(saleConditionQueryObj);
			saleAndRefundConditionQueryList.add(refundConditionQueryObj);
			BasicDBObject saleAndRefundConditionQueryObj = new BasicDBObject("$or", saleAndRefundConditionQueryList);

			if (!saleAndRefundConditionQueryObj.isEmpty()) {
				finalList.add(saleAndRefundConditionQueryObj);
			}
			if (!dateQuery.isEmpty()) {
				finalList.add(dateQuery);
			}
			if (!dateIndexQuery.isEmpty()) {
				finalList.add(dateIndexQuery);
			}
			BasicDBObject finalquery = new BasicDBObject("$and", finalList);

			logger.info("Inside TxnReports , searchPayment , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			// Now the aggregate operation ()In case any parameter is passed in
			// search query
			// , then show all records

			Document firstGroup;
			if (isParameterised) {
				firstGroup = new Document("_id", new Document("_id", "$_id"));
			} else {
				firstGroup = new Document("_id", new Document("OID", "$OID").append("ORIG_TXNTYPE", "$ORIG_TXNTYPE"));
			}

			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			int count = 0;
			while (cursor.hasNext()) {
				Document mydata = cursor.next();
				List<Document> courses = (List<Document>) mydata.get("entries");
				Document dbobj = courses.get(0);
				PaymentSearchDownloadObject transReport = new PaymentSearchDownloadObject();
				transReport.setTransactionId(dbobj.getString(FieldType.TXN_ID.toString()));
				transReport.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.toString()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.getName()))) {
					transReport.setTransactionRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));
				} else {
					transReport.setTransactionRegion(CrmFieldConstants.NA.getValue());
				}
				transReport.setMerchants(dbobj.getString(CrmFieldType.BUSINESS_NAME.getName()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
					transReport.setTxnSettledType(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
				} else {
					transReport.setTxnSettledType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_MODE.getName()))) {
					transReport.setTransactionMode(dbobj.getString(FieldType.TRANSACTION_MODE.getName()));
				} else {
				}

//				if (StringUtils.isNotBlank(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()))) {
//					transReport.setPostSettledFlag(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()));
//				} else {
//					transReport.setPostSettledFlag(CrmFieldConstants.NA.getValue());
//				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PART_SETTLE.toString()))
						&& dbobj.getString(FieldType.PART_SETTLE.toString()).equalsIgnoreCase(("Y"))) {
					transReport.setPartSettle(dbobj.getString(FieldType.PART_SETTLE.toString()));
				} else {
					transReport.setPartSettle(CrmFieldConstants.NA.getValue());
				}

				// transReport.setCardMask(dbobj.getString(FieldType.CARD_MASK.getName()));
				if (null != dbobj.getString(FieldType.PAYMENT_TYPE.toString())) {
					if (null != dbobj.getString(FieldType.CARD_MASK.toString())) {
						transReport.setCardMask(dbobj.getString(FieldType.CARD_MASK.toString()));
					} else if (null != dbobj.getString(FieldType.PAYER_ADDRESS.getName())) {

						if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
							transReport.setCardMask(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
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

							transReport.setCardMask(vpastrBuilder.toString());
						}
					} else {
						transReport.setCardMask(CrmFieldConstants.NA.getValue());
					}
//					else if ((dbobj.getString(FieldType.PAYMENT_TYPE.getName()))
//							.equals(PaymentType.WALLET.getCode())) {
//						transReport.setCardMask(CrmFieldConstants.WALLET.getValue());
//					}
				} else {
					transReport.setCardMask(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_NAME.getName()))) {
					transReport.setCustName(dbobj.getString(FieldType.CUST_NAME.getName()));
				} else {
					transReport.setCustName("NA");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_EMAIL.getName()))) {
					transReport.setCustEmail(dbobj.getString(FieldType.CUST_EMAIL.getName()));
				} else {
					transReport.setCustEmail("NA");
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYOUT_DATE.toString()))) {
					transReport.setPayOutDate((dbobj.getString(FieldType.PAYOUT_DATE.toString())));
				} else {
					transReport.setPayOutDate(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UTR_NO.toString()))) {
					transReport.setUtrNo(dbobj.getString(FieldType.UTR_NO.toString()));
				} else {
					transReport.setUtrNo(CrmFieldConstants.NA.getValue());
				}
				/*
				 * if (dbobj.containsKey("CUSTOM_FLAG") &&
				 * StringUtils.isNotBlank(dbobj.getString("CUSTOM_FLAG"))) {
				 * transReport.setCustomFlag(((Document) dbobj).getString("CUSTOM_FLAG")); }
				 * else { transReport.setCustomFlag("N"); }
				 */

				User user1 = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						user1 = userMap.get(payid);
					} else {
						user1 = userDao.findPayId(payid);
						userMap.put(payid, user1);
					}
				}

				transReport.setMerchants(user1.getBusinessName());

				if (merchantPayId.equalsIgnoreCase("All")) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
						transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
					} else {
						transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
						transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
					} else {
						transReport.setProductPrice(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
						transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
					} else {
						transReport.setVendorID(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
						transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
					} else {
						transReport.setSKUCode(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
						transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
					} else {
						transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
					}
				} else {

					if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
							|| user.getUserType().equals(UserType.RESELLER)) {
						User merhant = userMap.get(merchantPayId);

						if (merhant.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}
					} else {

						if (user.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}

					}

				}

				// if (!merchantPayId.equalsIgnoreCase("All") &&
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
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
						transReport.setSubMerchantId(subMerchantUser.getBusinessName());
					} else {
						transReport.setSubMerchantId(CrmFieldConstants.NA.getValue());
					}

				}

				transReport.setDeliveryStatus("");

				if (null != dbobj.getString(FieldType.ORIG_TXNTYPE.toString())) {
					transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
				} else {
					// If ORIG_TXN_TYPE is not available incase of a timeout ,
					// set TXNTYPE instead
					// of ORIG_TXN_TYPE
					if (dbobj.getString(FieldType.STATUS.toString()).equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
						transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					} else {
						transReport.setTxnType(CrmFieldConstants.NA.getValue());
					}

				}
				if (null != dbobj.getString(FieldType.ACQUIRER_TYPE.toString())) {
					transReport.setAcquirerType(dbobj.getString(FieldType.ACQUIRER_TYPE.toString()));
				} else {
					transReport.setAcquirerType(CrmFieldConstants.NA.getValue());
				}

				transReport.setAcquirerMode(dbobj.getString(FieldType.ACQUIRER_MODE.getName()));
				if (null != dbobj.getString(FieldType.PAYMENT_TYPE.toString())) {
					transReport.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transReport.setPaymentMethods(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
					transReport.setUDF11(dbobj.getString(FieldType.UDF11.getName()));
				} else {
					transReport.setUDF11(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF12.getName()))) {
					transReport.setUDF12(dbobj.getString(FieldType.UDF12.getName()));
				} else {
					transReport.setUDF12(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF13.getName()))) {
					transReport.setUDF13(dbobj.getString(FieldType.UDF13.getName()));
				} else {
					transReport.setUDF13(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF14.getName()))) {
					transReport.setUDF14(dbobj.getString(FieldType.UDF14.getName()));
				} else {
					transReport.setUDF14(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF15.getName()))) {
					transReport.setUDF15(dbobj.getString(FieldType.UDF15.getName()));
				} else {
					transReport.setUDF15(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF16.getName()))) {
					transReport.setUDF16(dbobj.getString(FieldType.UDF16.getName()));
				} else {
					transReport.setUDF16(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF17.getName()))) {
					transReport.setUDF17(dbobj.getString(FieldType.UDF17.getName()));
				} else {
					transReport.setUDF17(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF18.getName()))) {
					transReport.setUDF18(dbobj.getString(FieldType.UDF18.getName()));
				} else {
					transReport.setUDF18(CrmFieldConstants.NA.getValue());
				}

				transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
				if (StatusType.CAPTURED.getName().equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName()))) {
					transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				} else {
					transReport.setSettledDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
					transReport.setDateFrom(dbobj.getString(FieldType.PG_DATE_TIME.getName()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))
							&& !dbobj.getString(FieldType.RRN.getName()).equalsIgnoreCase("-")) {
						transReport.setRrn(dbobj.getString(FieldType.RRN.getName()));
					} else {
						transReport.setRrn(CrmFieldConstants.NA.getValue());
					}
				}

				transReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				transReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
				if (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()) != null) {
					transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transReport.setTotalAmount("");
				}

				// changes regarding partner reseller
				if (user.getUserType().equals(UserType.RESELLER) && user.isPartnerFlag()) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {

						transReport.setResellerCharges(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

					} else {
						transReport.setResellerCharges("0.00");
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						transReport.setResellerGST(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

					} else {
						transReport.setResellerGST("0.00");
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdrOrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(
													dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

						} else {
							transReport.setTdrOrSurcharge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdrOrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

						} else {
							transReport.setTdrOrSurcharge("0.00");
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

						} else {
							transReport.setGst("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

						} else {
							transReport.setGst("0.00");
						}
					}

				} // END

				else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdrOrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(
													dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

						} else {
							transReport.setTdrOrSurcharge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdrOrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

						} else {
							transReport.setTdrOrSurcharge("0.00");
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

						} else {
							transReport.setGst("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

						} else {
							transReport.setGst("0.00");
						}
					}

				}
				// end else

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
						&& dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))
						&& dbobj.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.SALE.getName())) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

							transReport.setMerchantAmount(
									"-" + String.valueOf(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName()))
											.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))));
						} else {
							transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
						}

					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

							transReport.setMerchantAmount(
									"-" + String.valueOf(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName()))
											.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))));
						} else {
							transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
						}
					}

				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
						&& dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))
						&& dbobj.getString(FieldType.ORIG_TXNTYPE.getName())
								.equalsIgnoreCase(TxnType.REFUND.getName())) {
					transReport.setMerchantAmount("0.00");
					transReport.setTdrOrSurcharge("0.00");
					transReport.setGst("0.00");
				} else {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString())) && dbobj
							.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName())) {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								transReport.setMerchantAmount("-" + String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
												new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
														.add(new BigDecimal(
																dbobj.getString(FieldType.ACQUIRER_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_TDR_SC.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.RESELLER_GST.getName()))))));
							} else {
								transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
							}

						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								transReport.setMerchantAmount("-" + String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
												new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
														.add(new BigDecimal(
																dbobj.getString(FieldType.ACQUIRER_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_TDR_SC.getName()))
																		.add(new BigDecimal(dbobj.getString(
																				FieldType.PG_GST.getName())))))));
							} else {
								transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
							}
						}

					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								transReport.setMerchantAmount(String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
												new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
														.add(new BigDecimal(
																dbobj.getString(FieldType.ACQUIRER_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_TDR_SC.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.RESELLER_GST.getName()))))));
							} else {
								transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
							}
						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								transReport.setMerchantAmount(String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
												new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
														.add(new BigDecimal(
																dbobj.getString(FieldType.ACQUIRER_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_TDR_SC.getName()))
																		.add(new BigDecimal(dbobj.getString(
																				FieldType.PG_GST.getName())))))));
							} else {
								transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
							}
						}

					}
				}

				if (null != dbobj.getString(FieldType.MOP_TYPE.toString())) {
					transReport.setMoptype(dbobj.getString(FieldType.MOP_TYPE.toString()));
				} else {
					transReport.setMoptype(CrmFieldConstants.NA.getValue());
				}

				if (null != dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString())) {
					transReport.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()));
				} else {
					transReport.setCardHolderType(CrmFieldConstants.NA.toString());
				}

				transactionList.add(transReport);
			}
			logger.info("transactionList created and size = " + transactionList.size());
			cursor.close();
			logger.info("Inside TxnReports , searchPayment , transactionListSize = " + transactionList.size());
			Comparator<PaymentSearchDownloadObject> comp = (PaymentSearchDownloadObject a,
					PaymentSearchDownloadObject b) -> {
				if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
					return -1;
				} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
					return 1;
				} else {
					return 0;
				}
			};
			Collections.sort(transactionList, comp);
			logger.info("transactionList created and Sorted");
			return transactionList;
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPayment , Exception = " , e);
			return transactionList;
		}
	}

	public List<TransactionSearchDownloadObject> customSettledDownload(String fromDate, String toDate, String payId,
			User sessionUser, String currency) {
		logger.info("Inside TxnReports , customSettledDownload");
		boolean isParameterised = false;
		List<TransactionSearchDownloadObject> transactionList = new ArrayList<TransactionSearchDownloadObject>();
		try {
			PropertiesManager propManager = new PropertiesManager();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			Set<String> orderIdSet = new HashSet<String>();
			String payOutDate = "";
			String utrNo = "";

			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());

			if (StringUtils.isNotBlank(payId) && (!payId.equalsIgnoreCase("ALL"))) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
				isParameterised = true;
			}
			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
				isParameterised = true;
			}
			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside TxnReports , customSettledDownloadReport , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort/* , group */);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {

				Document dbobj = cursor.next();
				TransactionSearchDownloadObject transReport = new TransactionSearchDownloadObject();

				String payid = (String) dbobj.get(FieldType.PAY_ID.getName());
				orderIdSet.add(dbobj.getString(FieldType.ORDER_ID.toString()));

				if (null != dbobj.getString(FieldType.ORIG_TXNTYPE.toString()) && dbobj
						.getString(FieldType.ORIG_TXNTYPE.toString()).equalsIgnoreCase(TxnType.SALE.getName())) {
					transReport.setTxnType("payment");

				} else {
					transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
				}
				transReport.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.toString()));

				if (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()) != null) {
					transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transReport.setTotalAmount("");
				}

				transReport.setCurrency(propManager.getAlphabaticCurrencyCode(dbobj.getString(FieldType.CURRENCY_CODE.toString())));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
						&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

					transReport.setFeeExclusiveTax(String.format("%.2f",
							(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

				} else {
					transReport.setFeeExclusiveTax("0.00");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
						&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
					transReport.setTax(String.format("%.2f",
							(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

				} else {
					transReport.setTax("0.00");
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
					
					if(StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString())) && 
							dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.COD.getCode())) {
						
						Double tdrScr = (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
								+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
								+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString())));
						if(String.valueOf(tdrScr).equals("0.0")) {
							transReport.setCreditAmount(String.format("%.2f",tdrScr));
						}else {
							transReport.setCreditAmount("-" + String.format("%.2f",tdrScr));
						}
						
						
						
					}else {
					
					transReport.setCreditAmount(String.format("%.2f",
							Double.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString())) - (Double
									.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));
					}
				} else {
					transReport.setCreditAmount(transReport.getTotalAmount());
				}
				
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString())) 
						&& dbobj.getString(FieldType.ORIG_TXNTYPE.toString()).equalsIgnoreCase(TxnType.SALE.getName())) {
					transReport.setRefundAmount("");
					transReport.setDebitAmount("");
				}else {
					transReport.setRefundAmount("");
				}
				
				if (null != dbobj.getString(FieldType.PAYMENT_TYPE.toString())
						&& (dbobj.getString(FieldType.PAYMENT_TYPE.toString()).equalsIgnoreCase("CC") || 
								dbobj.getString(FieldType.PAYMENT_TYPE.toString()).equalsIgnoreCase("DC"))) {
					transReport.setPaymentMethods("card");
					transReport.setCardType(PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				}else {
					transReport.setCardType("");
					transReport.setPaymentMethods(PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				}
				transReport.setIssuerName("-");
				transReport.setDateFrom(dbobj.getString(FieldType.PG_DATE_TIME.getName()));
				
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_NAME.getName()))) {
					transReport.setPaymentNotes(dbobj.getString(FieldType.CUST_NAME.toString()));
				} else {
					transReport.setRrn("");
				}
				transReport.setRefundNotes("");
				
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))) {
					transReport.setRrn(dbobj.getString(FieldType.RRN.toString()));
				} else {
					transReport.setRrn("");
				}
				
				transReport.setEntityDiscription("Order Payment");
				transReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
				if (dbobj.containsKey(FieldType.CUST_PHONE.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_PHONE.getName()))) {
					transReport.setCustomerMobile(dbobj.getString(FieldType.CUST_PHONE.getName()));
				} else {
					transReport.setCustomerMobile("");
				}
				transReport.setDesputeId("-");
				transReport.setDesputeCreatedAt("-");
				transReport.setDesputeReason("-");
				transReport.setSettlementId("-");
				transReport.setSettlementDateAt(dbobj.getString(FieldType.PAYOUT_DATE.getName()));
				transReport.setUtrNo(dbobj.getString(FieldType.UTR_NO.getName()));
				utrNo = dbobj.getString(FieldType.UTR_NO.getName());
				payOutDate = dbobj.getString(FieldType.PAYOUT_DATE.getName());
				transReport.setSettlementBy("Payment GateWay");
				
				transactionList.add(transReport);
			}
			logger.info("transactionList created and size = " + transactionList.size());
			cursor.close();
			List<TransactionSearchDownloadObject> refundTxnList = getRefundData(orderIdSet, payOutDate, utrNo);
			if(refundTxnList.size()>0)
				transactionList.addAll(refundTxnList);
			
			logger.info("Inside TxnReports , customSettledDownload , transactionListSize = " + transactionList.size());
			Comparator<TransactionSearchDownloadObject> comp = (TransactionSearchDownloadObject a,
					TransactionSearchDownloadObject b) -> {

				if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
					return -1;
				} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
					return 1;
				} else {
					return 0;
				}
			};
			Collections.sort(transactionList, comp);
			logger.info("transactionList created and Sorted");
			return transactionList;
		}

		catch (Exception e) {
			logger.error("Exception occured in TxnReports , customSettledDownload , Exception = " , e);
			return transactionList;
		}
	}

	public List<TransactionSearchDownloadObject> getRefundData(Set<String> orderIdSet, String payOutDate,
			String utrNo) {

		logger.info("Inside TxnReports , getRefundData");

		PropertiesManager propManager = new PropertiesManager();
		List<TransactionSearchDownloadObject> transactionList = new ArrayList<TransactionSearchDownloadObject>();

		try {

			for (String orderId : orderIdSet) {
				List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
				paramConditionLst
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));

				BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);

				logger.info("Inside TxnReports , customSettledDownloadReport , finalquery = " + finalquery);
				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				BasicDBObject match = new BasicDBObject("$match", finalquery);

				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

				List<BasicDBObject> pipeline = Arrays.asList(match, sort/* , group */);

				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				MongoCursor<Document> cursor = output.iterator();
				while (cursor.hasNext()) {

					Document dbobj = cursor.next();
					TransactionSearchDownloadObject transReport = new TransactionSearchDownloadObject();

					if (null != dbobj.getString(FieldType.ORIG_TXNTYPE.toString()) && dbobj
							.getString(FieldType.ORIG_TXNTYPE.toString()).equalsIgnoreCase(TxnType.SALE.getName())) {
						transReport.setTxnType("payment");

					} else {

						transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
					}
					transReport.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.toString()));

					if (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()) != null) {
						transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
					} else {
						transReport.setTotalAmount("");
					}

					transReport.setCurrency(
							propManager.getAlphabaticCurrencyCode(dbobj.getString(FieldType.CURRENCY_CODE.toString())));
					transReport.setFeeExclusiveTax("0");
					transReport.setTax("0");
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString())) && dbobj
							.getString(FieldType.ORIG_TXNTYPE.toString()).equalsIgnoreCase(TxnType.REFUND.getName())) {
						transReport.setRefundAmount(dbobj.getString(FieldType.AMOUNT.toString()));
						transReport.setDebitAmount(transReport.getRefundAmount());
						transReport.setCreditAmount("");
					} else {
						transReport.setRefundAmount("");
						transReport.setDebitAmount("");
					}

					if (null != dbobj.getString(FieldType.PAYMENT_TYPE.toString())
							&& (dbobj.getString(FieldType.PAYMENT_TYPE.toString()).equalsIgnoreCase("CC")
									|| dbobj.getString(FieldType.PAYMENT_TYPE.toString()).equalsIgnoreCase("DC"))) {
						transReport.setPaymentMethods("card");
						transReport.setCardType(
								PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
					} else {
						transReport.setCardType("");
						transReport.setPaymentMethods(
								PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
					}
					transReport.setIssuerName("-");
					transReport.setDateFrom(dbobj.getString(FieldType.PG_DATE_TIME.getName()));

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_NAME.getName()))) {
						transReport.setPaymentNotes(dbobj.getString(FieldType.CUST_NAME.toString()));
					} else {
						transReport.setRrn("");
					}
					transReport.setRefundNotes("");

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))) {
						transReport.setRrn(dbobj.getString(FieldType.RRN.toString()));
					} else {
						transReport.setRrn("");
					}

					transReport.setEntityDiscription("Order Payment");
					transReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
					if (dbobj.containsKey(FieldType.CUST_PHONE.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_PHONE.getName()))) {
						transReport.setCustomerMobile(dbobj.getString(FieldType.CUST_PHONE.getName()));
					} else {
						transReport.setCustomerMobile("");
					}
					transReport.setDesputeId("-");
					transReport.setDesputeCreatedAt("-");
					transReport.setDesputeReason("-");
					transReport.setSettlementId("-");
					transReport.setSettlementDateAt(payOutDate);
					transReport.setUtrNo(utrNo);
					transReport.setSettlementBy("Payment GateWay");

					transactionList.add(transReport);
				}
			}
			return transactionList;
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , customSettledDownload , Exception = " , e);
			return transactionList;
		}
	}

}
