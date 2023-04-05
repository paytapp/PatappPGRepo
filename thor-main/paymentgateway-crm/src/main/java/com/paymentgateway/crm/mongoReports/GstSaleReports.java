package com.paymentgateway.crm.mongoReports;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.paymentgateway.commons.GstRSaleReportDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.GstRSaleReport;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.SurchargeDetailsDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.crm.action.GstSaleReportAction;

@Component
public class GstSaleReports {

	private static Logger logger = LoggerFactory.getLogger(GstSaleReports.class.getName());
	private static final String alphabaticFileName = "alphabatic-currencycode.properties";

	@Autowired
	private UserDao userdao;

	@Autowired
	private GstRSaleReportDao gstRSaleReportDao;

	@Autowired
	private ChargingDetailsDao chargingdetailsDao;

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private SurchargeDetailsDao surchargeDetailsDao;

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private UserSettingDao userSettingDao;

	private static final String prefix = "MONGO_DB_";

	private static BigDecimal ONE_HUNDRED = new BigDecimal(100);

	public List<GstRSaleReport> searchGstRSale(String merchant, String paymentType, String Userstatus, String currency,
			String transactionType, String month, String year, User user, int start, int length) {

		List<Merchants> activeMerchantPayIdList = userdao.getMerchantActive(merchant);
		List<GstRSaleReport> merchantList = new ArrayList<GstRSaleReport>();

		int maxDay = GstSaleReportAction.day(month, year);
		String day = (String.valueOf(maxDay));
		month = GstSaleReportAction.monthno(month);

		String fromDate = DateCreater.toDateTimeformatCreater("01" + "-" + month + "-" + year);
		String toDate = DateCreater.formDateTimeformatCreater(day + "-" + month + "-" + year);

		for (Merchants merchants : activeMerchantPayIdList) {
			String merchantPayId = merchants.getPayId();

			PropertiesManager propManager = new PropertiesManager();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
			// List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> paymentTypeConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject acquirerQuery = new BasicDBObject();
			BasicDBObject txnTypeQuery = new BasicDBObject();
			BasicDBObject paymentTypeQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			List<BasicDBObject> statusConditionLst = new ArrayList<BasicDBObject>();

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

			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			if (!Userstatus.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), Userstatus));
			} else {
				statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
				statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.APPROVED.getName()));
				statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
				statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ERROR.getName()));
				statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName()));
				statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
				statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.INVALID.getName()));
				statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DECLINED.getName()));
				statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.REJECTED.getName()));
				statusConditionLst
						.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName()));
				statusQuery.append("$or", statusConditionLst);
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

			if (!transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), transactionType));
			} else {
				txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
				txnTypeConditionLst
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
				/*
				 * txnTypeConditionLst .add(new BasicDBObject(FieldType.TXNTYPE.getName(),
				 * TransactionType.AUTHORISE.getName())); txnTypeConditionLst.add(new
				 * BasicDBObject(FieldType.TXNTYPE.getName(),
				 * TransactionType.CAPTURE.getName()));
				 * 
				 * txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(),
				 * TransactionType.RECO.getName())); txnTypeConditionLst.add(new
				 * BasicDBObject(FieldType.TXNTYPE.getName(),
				 * TransactionType.INVALID.getName())); txnTypeConditionLst.add(new
				 * BasicDBObject(FieldType.TXNTYPE.getName(),
				 * TransactionType.REFUNDRECO.getName()));
				 */

				txnTypeQuery.append("$or", txnTypeConditionLst);
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
				paymentTypeConditionLst
						.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.EMI.getCode()));
				paymentTypeConditionLst
						.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.WALLET.getCode()));
				paymentTypeConditionLst.add(
						new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.RECURRING_PAYMENT.getCode()));
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
			if (!paymentTypeQuery.isEmpty()) {
				allConditionQueryList.add(paymentTypeQuery);
			}

			if (!txnTypeQuery.isEmpty()) {
				allConditionQueryList.add(txnTypeQuery);
			}
			if (!statusQuery.isEmpty()) {
				allConditionQueryList.add(statusQuery);
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
			// AUTOWIRED

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			// create our pipeline operations, first with the $match
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			// Now the $group operation
			BasicDBObject groupFields = new BasicDBObject("_id", "$PG_REF_NUM").append("entries",
					new BasicDBObject("$push", "$$ROOT"));

			BasicDBObject group = new BasicDBObject("$group", groupFields);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			/*
			 * BasicDBObject skip = new BasicDBObject("$skip", start); BasicDBObject limit =
			 * new BasicDBObject("$limit", length);
			 */

			// run aggregation

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			String payid = null;
			String acquirerName = null;
			String paymentType1 = null;
			String mopType = null;
			double refundNetamt = 0.0;
			double refundTxtamount = 0.0;
			double saleNetamt = 0.0;
			double salegstamount = 0.0;
			double refundgstamount = 0.0;
			double saleTxtamount = 0.0;
			String businessName = null;
			String invoiceNo = null;
			String operationState = null;

			String merhantGstNo = null;
			int count = 0;
			String adminstate = user.getState();

			while (cursor.hasNext()) {
				Document mydata = cursor.next();
				List<Document> courses = (List<Document>) mydata.get("entries");
				Document dbobj = courses.get(0);

				// AUTOWIRED
				payid = ((Document) dbobj).getString(FieldType.PAY_ID.toString());
				String acquirer = ((Document) dbobj).getString(FieldType.ACQUIRER_TYPE.toString());
				acquirerName = AcquirerType.getAcquirerName(acquirer);
				// acquirerName ="HDFC Bank";
				String payment = ((Document) dbobj).getString(FieldType.PAYMENT_TYPE.toString());

				String transaction = ((Document) dbobj).getString(FieldType.TXNTYPE.toString());
				// paymentType1 ="DEBIT_CARD";
				String mop = ((Document) dbobj).getString(FieldType.MOP_TYPE.toString());
				mopType = MopType.getmopName(mop);
				// String date = ((Document) dbobj).getString(FieldType.UPDATE_DATE.toString());
				String date = ((Document) dbobj).getString(FieldType.CREATE_DATE.toString());
				double amount = Double.parseDouble(((Document) dbobj).getString(FieldType.AMOUNT.toString()));
				double total_amount = Double
						.parseDouble(((Document) dbobj).getString(FieldType.TOTAL_AMOUNT.toString()));
				User user1 = userdao.findPayId(payid);
				UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(user1.getPayId());
				businessName = user1.getBusinessName();
				invoiceNo = TransactionManager.getId();
				operationState = user1.getOperationState();
				merhantGstNo = user1.getMerchantGstNo();

				// BigDecimal merchantServiceTax1 =
				// serviceTaxDao.findServiceTaxByPayId(payid,date);
				BigDecimal merchantServiceTax1 = new BigDecimal(PropertiesManager.propertiesMap.get("SERVICE_TAX"));
				double servicetax = merchantServiceTax1.doubleValue();

				if (transaction.equals(TransactionType.REFUND.getName())) {
					String rFlag = ((Document) dbobj).getString(FieldType.REFUND_FLAG.toString());
					if (rFlag.equalsIgnoreCase("R")) {
						if (merchantSettings.isSurchargeFlag() == false) {
							paymentType1 = PaymentType.getInstanceUsingCode(payment).toString();
							List<ChargingDetails> chargingDetails = chargingdetailsDao.findDetail(date, payid,
									acquirerName, paymentType1, mopType, currency, transaction);
							if (chargingDetails
									.size() > 0) {/*
													 * double merTdr =0.0; double txtamo =0.0; double gsttxtamo =0.0;
													 * double gstamount =0.0; double tdramount =0.0; for(ChargingDetails
													 * chargingDetails2 : chargingDetails){ merTdr =
													 * chargingDetails2.getMerchantTDR(); } if(merTdr!=0){ tdramount
													 * =merTdr/100; txtamo = amount*tdramount; }else{ tdramount =merTdr;
													 * }
													 * 
													 * 
													 * if(servicetax!=0){ gsttxtamo=txtamo/100;
													 * gstamount=gstamount*servicetax; }else{
													 * 
													 * }
													 */
								BigDecimal totalamount = new BigDecimal(amount);
								double merchantPrSurchargeGstOnMerchant = 0.0;
								double merchantPrSurchargeCalculate = 0.0;
								for (ChargingDetails chargingDetail : chargingDetails) {
									BigDecimal merchantTDR = new BigDecimal(chargingDetail.getMerchantTDR());
									BigDecimal merchantTDRAFC = new BigDecimal(chargingDetail.getMerchantTDRAFC());
									BigDecimal merchantPrTdr = (totalamount.multiply(merchantTDR).divide(ONE_HUNDRED));
									BigDecimal merchantTdrCalculate = merchantPrTdr.add(merchantTDRAFC);
									BigDecimal totalGstOnMerchant = (merchantTdrCalculate.multiply(merchantServiceTax1)
											.divide(ONE_HUNDRED));
									BigDecimal merchantPayout = merchantTdrCalculate.add(totalGstOnMerchant);
									merchantPrSurchargeCalculate = merchantTdrCalculate.doubleValue();
									merchantPrSurchargeGstOnMerchant = totalGstOnMerchant.doubleValue();
								}

								refundTxtamount += merchantPrSurchargeCalculate;

								refundgstamount += merchantPrSurchargeGstOnMerchant;
							}
						} else {
							// paymentType1 = PaymentType.getpaymentName(payment);
							/*
							 * List<SurchargeDetails> surchargeDetails =
							 * surchargeDetailsDao.findSurchargeDetails(payid, payment,date);
							 * 
							 * 
							 * if(surchargeDetails.size()>0){
							 */
							double samount = total_amount - amount;
							BigDecimal totalamount = new BigDecimal(samount);
							double ta = totalamount.doubleValue();
							BigDecimal surcharge = null;
							double tgm = 0.0;
							if (servicetax != 0) {

								BigDecimal totalGstOnMerchant = ((merchantServiceTax1).divide(ONE_HUNDRED));
								tgm = 1 + totalGstOnMerchant.doubleValue();

								BigDecimal surchargetgm = new BigDecimal(tgm);
								surcharge = ((surchargetgm).divide(ONE_HUNDRED));
							}
							double surchargeamount = surcharge.doubleValue();
							double merchantPrSurchargeGstOnMerchant = samount - surchargeamount;
							double merchantPrSurchargeCalculate = tgm;

							saleTxtamount += merchantPrSurchargeCalculate;

							salegstamount += merchantPrSurchargeGstOnMerchant;
							// }

						}
					}
				} else {

					if (merchantSettings.isSurchargeFlag() == false) {
						paymentType1 = PaymentType.getInstanceUsingCode(payment).toString();
						List<ChargingDetails> chargingDetails = chargingdetailsDao.findDetail(date, payid, acquirerName,
								paymentType1, mopType, currency, transaction);
						if (chargingDetails
								.size() > 0) {/*
												 * 
												 * double merTdr =0.0; double txtamo =0.0; double tdramount =0.0; double
												 * gsttxtamo =0.0; double gstamount =0.0; for(ChargingDetails
												 * chargingDetails2 : chargingDetails){ merTdr =
												 * chargingDetails2.getMerchantTDR(); } if(merTdr!=0){ tdramount
												 * =merTdr/100; txtamo = amount*tdramount; }else{ tdramount =merTdr; }
												 * 
												 * 
												 * 
												 * if(servicetax!=0){ gsttxtamo=txtamo/100;
												 * gstamount=gstamount*servicetax; }
												 */
							BigDecimal totalamount = new BigDecimal(amount);
							double merchantPrSurchargeGstOnMerchant = 0.0;
							double merchantPrSurchargeCalculate = 0.0;
							for (ChargingDetails chargingDetail : chargingDetails) {
								BigDecimal merchantTDR = new BigDecimal(chargingDetail.getMerchantTDR());
								BigDecimal merchantTDRAFC = new BigDecimal(chargingDetail.getMerchantTDRAFC());
								BigDecimal merchantPrTdr = (totalamount.multiply(merchantTDR).divide(ONE_HUNDRED));
								BigDecimal merchantTdrCalculate = merchantPrTdr.add(merchantTDRAFC);
								BigDecimal totalGstOnMerchant = (merchantTdrCalculate.multiply(merchantServiceTax1)
										.divide(ONE_HUNDRED));
								BigDecimal merchantPayout = merchantTdrCalculate.add(totalGstOnMerchant);
								merchantPrSurchargeCalculate = merchantTdrCalculate.doubleValue();
								merchantPrSurchargeGstOnMerchant = totalGstOnMerchant.doubleValue();
							}
							saleTxtamount += merchantPrSurchargeGstOnMerchant;

							salegstamount += merchantPrSurchargeCalculate;
						}
					} else {
						// paymentType1 = PaymentType.getpaymentName(payment);
						/*
						 * List<SurchargeDetails> surchargeDetails =
						 * surchargeDetailsDao.findSurchargeDetails(payid, payment,date);
						 * if(surchargeDetails.size()>0){ BigDecimal totalamount = new
						 * BigDecimal(amount); double merchantPrSurchargeGstOnMerchant=0.0; double
						 * merchantPrSurchargeCalculate =0.0; for(SurchargeDetails surchargeDetail :
						 * surchargeDetails){ BigDecimal merchantPrTdr =
						 * (totalamount.multiply(surchargeDetail.getSurchargePercentage()).divide(
						 * ONE_HUNDRED)); BigDecimal merchantTdrCalculate =
						 * merchantPrTdr.add(surchargeDetail.getSurchargeAmount()); BigDecimal
						 * totalGstOnMerchant = (merchantTdrCalculate.multiply( merchantServiceTax1
						 * ).divide(ONE_HUNDRED)); BigDecimal merchantPayout = merchantTdrCalculate
						 * .add(totalGstOnMerchant); merchantPrSurchargeCalculate=
						 * merchantTdrCalculate.doubleValue(); merchantPrSurchargeGstOnMerchant=
						 * totalGstOnMerchant.doubleValue(); }
						 */

						double samount = total_amount - amount;
						BigDecimal totalamount = new BigDecimal(samount);
						double ta = totalamount.doubleValue();
						BigDecimal surcharge = null;
						double tgm = 0.0;
						if (servicetax != 0) {

							BigDecimal totalGstOnMerchant = ((merchantServiceTax1).divide(ONE_HUNDRED));
							tgm = 1 + totalGstOnMerchant.doubleValue();

							BigDecimal surchargetgm = new BigDecimal(tgm);
							surcharge = ((surchargetgm).divide(ONE_HUNDRED));
						}
						double surchargeamount = surcharge.doubleValue();
						double merchantPrSurchargeGstOnMerchant = samount - surchargeamount;
						double merchantPrSurchargeCalculate = tgm;

						saleTxtamount += merchantPrSurchargeCalculate;

						salegstamount += merchantPrSurchargeGstOnMerchant;
						// }

					}

				}

				count++;
			}
			if (count != 0) {
				double gstnetamt = salegstamount - refundgstamount;
				double txtamount = saleTxtamount - refundTxtamount;
				double totalamount = txtamount + gstnetamt;

				GstRSaleReport gstrsalereport = new GstRSaleReport();
				gstrsalereport.setPayId(payid);
				gstrsalereport.setBusinessName(businessName);
				gstrsalereport.setInvoiceNo(invoiceNo);
				gstrsalereport.setGstNo(merhantGstNo);
				gstrsalereport.setGoodOrService("S");
				gstrsalereport.setServicesDescription("PG Charges");
				gstrsalereport.setHsn_code("");

				double txtamountrounded = (double) Math.round(txtamount * 100) / 100;
				gstrsalereport.setTxn_value(txtamountrounded);

				if (operationState.equalsIgnoreCase(adminstate)) {
					double txtValue = gstnetamt / 2;
					double txtrounded = (double) Math.round(txtValue * 100) / 100;
					gstrsalereport.setcGst(txtrounded);
					gstrsalereport.setSGst(txtrounded);
				} else {
					double gstnetamtrounded = (double) Math.round(gstnetamt * 100) / 100;
					gstrsalereport.setiGst(gstnetamtrounded);
				}

				gstrsalereport.setOperationState(operationState);
				gstrsalereport.setMonth(month);
				gstrsalereport.setYear(year);
				double totalamountrounded = (double) Math.round(totalamount * 100) / 100;
				gstrsalereport.setNetAmt(totalamountrounded);
				Date date = new Date();
				gstrsalereport.setCreatedDate(date);
				GstRSaleReport gstRSaleReport2 = gstRSaleReportDao.findBymonthByPayId(payid, month, year);
				if (gstRSaleReport2 == null) {
					gstRSaleReportDao.create(gstrsalereport);
					merchantList.add(gstrsalereport);
				}
				merchantList.add(gstRSaleReport2);
			}

			cursor.close();

		}

		return merchantList;

	}

	public static int day(String month, String year) {
		Calendar calendar = Calendar.getInstance();

		int yearno = Integer.parseInt(year);

		// int monthno = Calendar.SEPTEMBER;
		int monthno = Integer.parseInt(month);
		int date = 1;
		calendar.set(yearno, monthno, date);
		System.out.println(month);
		int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

		return maxDay;
	}

}

/*
 * public List<GstRSaleReport> searchGstRPerchance(String merchant, String
 * paymentType, String Userstatus, String currency, String transactionType,
 * String month, String year, User user, int start, int length){
 * 
 * List<Merchants> activeMerchantPayIdList =
 * userdao.getMerchantActive(merchant); List<GstRSaleReport> merchantList = new
 * ArrayList<GstRSaleReport>();
 * 
 * int maxDay = GstSaleReportAction.day(month, year); String
 * day=(String.valueOf(maxDay)); month = GstSaleReportAction.monthno(month);
 * String fromDate = DateCreater.toDateTimeformatCreater("01"+ "-" + month + "-"
 * + year); String toDate = DateCreater.formDateTimeformatCreater(day + "-" +
 * month + "-" + year);
 * 
 * for(Merchants merchants : activeMerchantPayIdList){ String merchantPayId =
 * merchants.getPayId();
 * 
 * PropertiesManager propManager = new PropertiesManager(); List<BasicDBObject>
 * paramConditionLst = new ArrayList<BasicDBObject>(); List<BasicDBObject>
 * currencyConditionLst = new ArrayList<BasicDBObject>(); //List<BasicDBObject>
 * acquirerConditionLst = new ArrayList<BasicDBObject>(); List<BasicDBObject>
 * txnTypeConditionLst = new ArrayList<BasicDBObject>(); List<BasicDBObject>
 * paymentTypeConditionLst = new ArrayList<BasicDBObject>(); BasicDBObject
 * currencyQuery = new BasicDBObject(); BasicDBObject acquirerQuery = new
 * BasicDBObject(); BasicDBObject txnTypeQuery = new BasicDBObject();
 * BasicDBObject paymentTypeQuery = new BasicDBObject(); BasicDBObject
 * statusQuery = new BasicDBObject(); BasicDBObject dateQuery = new
 * BasicDBObject(); BasicDBObject allParamQuery = new BasicDBObject();
 * List<BasicDBObject> statusConditionLst = new ArrayList<BasicDBObject>();
 * 
 * if (!fromDate.isEmpty()) {
 * 
 * String currentDate = null; if (!toDate.isEmpty()) { currentDate = toDate; }
 * else { DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 * Calendar cal = Calendar.getInstance(); currentDate =
 * dateFormat.format(cal.getTime()); }
 * 
 * dateQuery.put(FieldType.CREATE_DATE.getName(),
 * BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(
 * fromDate).toLocalizedPattern()).add("$lte", new
 * SimpleDateFormat(currentDate).toLocalizedPattern()).get()); }
 * 
 * if (!merchantPayId.equalsIgnoreCase("ALL")) { paramConditionLst.add(new
 * BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId)); }
 * 
 * if (!Userstatus.equalsIgnoreCase("ALL")) { paramConditionLst.add(new
 * BasicDBObject(FieldType.STATUS.getName(), Userstatus)); } else {
 * statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(),
 * StatusType.CAPTURED.getName())); statusConditionLst.add(new
 * BasicDBObject(FieldType.STATUS.getName(), StatusType.APPROVED.getName()));
 * statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(),
 * StatusType.FAILED.getName())); statusConditionLst.add(new
 * BasicDBObject(FieldType.STATUS.getName(), StatusType.ERROR.getName()));
 * statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(),
 * StatusType.TIMEOUT.getName())); statusConditionLst.add(new
 * BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
 * statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(),
 * StatusType.INVALID.getName())); statusConditionLst.add(new
 * BasicDBObject(FieldType.STATUS.getName(), StatusType.DECLINED.getName()));
 * statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(),
 * StatusType.REJECTED.getName())); statusConditionLst.add(new
 * BasicDBObject(FieldType.STATUS.getName(),
 * StatusType.DENIED_BY_FRAUD.getName())); statusQuery.append("$or",
 * statusConditionLst); }
 * 
 * if (!currency.equalsIgnoreCase("ALL")) { paramConditionLst.add(new
 * BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency)); } else {
 * PropertiesManager propertiesManager= new PropertiesManager();
 * Map<String,String> allCurrencyMap; allCurrencyMap =
 * propertiesManager.getAllProperties(alphabaticFileName); for
 * (Map.Entry<String, String> entry : allCurrencyMap.entrySet()) {
 * 
 * currencyConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(),
 * entry.getKey())); }
 * 
 * currencyQuery.append("$or", currencyConditionLst); }
 * 
 * 
 * 
 * if (!transactionType.equalsIgnoreCase("ALL")) { paramConditionLst.add(new
 * BasicDBObject(FieldType.TXNTYPE.getName(), transactionType)); } else {
 * txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(),
 * TransactionType.SALE.getName())); txnTypeConditionLst .add(new
 * BasicDBObject(FieldType.TXNTYPE.getName(),
 * TransactionType.AUTHORISE.getName())); txnTypeConditionLst.add(new
 * BasicDBObject(FieldType.TXNTYPE.getName(),
 * TransactionType.CAPTURE.getName())); txnTypeConditionLst.add(new
 * BasicDBObject(FieldType.TXNTYPE.getName(),
 * TransactionType.REFUND.getName())); txnTypeConditionLst.add(new
 * BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
 * txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(),
 * TransactionType.INVALID.getName())); txnTypeConditionLst.add(new
 * BasicDBObject(FieldType.TXNTYPE.getName(),
 * TransactionType.REFUNDRECO.getName()));
 * 
 * txnTypeQuery.append("$or", txnTypeConditionLst); } if
 * (!paymentType.equalsIgnoreCase("ALL")) { paramConditionLst.add(new
 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType)); } else {
 * paymentTypeConditionLst .add(new
 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
 * PaymentType.CREDIT_CARD.getCode())); paymentTypeConditionLst .add(new
 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
 * PaymentType.DEBIT_CARD.getCode())); paymentTypeConditionLst .add(new
 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
 * PaymentType.NET_BANKING.getCode())); paymentTypeConditionLst.add(new
 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.EMI.getCode()));
 * paymentTypeConditionLst .add(new
 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
 * PaymentType.WALLET.getCode())); paymentTypeConditionLst .add(new
 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
 * PaymentType.RECURRING_PAYMENT.getCode())); paymentTypeConditionLst .add(new
 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
 * PaymentType.EXPRESS_PAY.getCode())); paymentTypeConditionLst .add(new
 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(), null));
 * paymentTypeQuery.append("$or", paymentTypeConditionLst); } if
 * (!paramConditionLst.isEmpty()) { allParamQuery = new BasicDBObject("$and",
 * paramConditionLst); }
 * 
 * List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
 * if (!currencyQuery.isEmpty()) { allConditionQueryList.add(currencyQuery); }
 * if (!acquirerQuery.isEmpty()) { allConditionQueryList.add(acquirerQuery); }
 * if (!paymentTypeQuery.isEmpty()) {
 * allConditionQueryList.add(paymentTypeQuery); }
 * 
 * if (!txnTypeQuery.isEmpty()) { allConditionQueryList.add(txnTypeQuery); } if
 * (!statusQuery.isEmpty()) { allConditionQueryList.add(statusQuery); } if
 * (!dateQuery.isEmpty()) { allConditionQueryList.add(dateQuery); }
 * 
 * BasicDBObject allConditionQueryObj = new BasicDBObject("$and",
 * allConditionQueryList); List<BasicDBObject> fianlList = new
 * ArrayList<BasicDBObject>();
 * 
 * if (!allParamQuery.isEmpty()) { fianlList.add(allParamQuery); } if
 * (!allConditionQueryObj.isEmpty()) { fianlList.add(allConditionQueryObj); }
 * 
 * BasicDBObject finalquery = new BasicDBObject("$and", fianlList); // AUTOWIRED
 * 
 * MongoDatabase dbIns = mongoInstance.getDB(); MongoCollection<Document> coll =
 * dbIns.getCollection(propertiesManager.getmongoDbParam(Constants.
 * COLLECTION_NAME.getValue()));
 * 
 * // create our pipeline operations, first with the $match BasicDBObject match
 * = new BasicDBObject("$match", finalquery );
 * 
 * 
 * 
 * 
 * // Now the $group operation BasicDBObject groupFields = new BasicDBObject(
 * "_id","$PG_REF_NUM").append("entries", new BasicDBObject( "$push", "$$ROOT"
 * ));
 * 
 * BasicDBObject group = new BasicDBObject("$group", groupFields); BasicDBObject
 * sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1) );
 * BasicDBObject skip = new BasicDBObject("$skip", start); BasicDBObject limit =
 * new BasicDBObject("$limit", length);
 * 
 * // run aggregation
 * 
 * List<BasicDBObject> pipeline = Arrays.asList(match,sort,group);
 * AggregateIterable<Document> output = coll.aggregate(pipeline);
 * MongoCursor<Document> cursor = output.iterator();
 * 
 * 
 * 
 * String payid = null; String acquirerName = null; String paymentType1 = null;
 * String mopType = null; double netamt=0.0; String businessName = null; String
 * invoiceNo = null; String operationState = null; double txtamount=0.0; String
 * merhantGstNo = null; int count =0; String adminstate = user.getState();
 * 
 * while (cursor.hasNext()) { Document mydata = cursor.next(); List<Document>
 * courses = (List<Document>) mydata.get("entries"); Document
 * dbobj=courses.get(0);
 * 
 * // AUTOWIRED payid = ((Document)
 * dbobj).getString(FieldType.PAY_ID.toString()); acquirerName = ((Document)
 * dbobj).getString(FieldType.ACQ_ID.toString()); //acquirerName ="HDFC Bank";
 * paymentType1 = ((Document)
 * dbobj).getString(FieldType.PAYMENT_TYPE.toString()); // paymentType1
 * ="DEBIT_CARD"; mopType = ((Document)
 * dbobj).getString(FieldType.MOP_TYPE.toString()); // mopType="VISA"; String
 * date = ((Document) dbobj).getString(FieldType.UPDATE_DATE.toString()); double
 * amount = Double.parseDouble(((Document)
 * dbobj).getString(FieldType.AMOUNT.toString())); User user1 =
 * userdao.findPayId(payid); businessName=user1.getBusinessName();
 * invoiceNo=TransactionManager.getId();
 * operationState=user1.getOperationState(); merhantGstNo
 * =user1.getMerchantGstNo();
 * 
 * //gstrsalereport.setInvoiceNo(TransactionManager.getId());
 * gstrsalereport.setPayId(); //String payid = (String)
 * dbobj.get(FieldType.PAY_ID.getName());
 * gstrsalereport.setBusinessName(user1.getBusinessName());
 * gstrsalereport.setcGst(user1.getMerchantGstNo());
 * gstrsalereport.setOperationState(user1.getOperationState());
 * gstrsalereport.setGoodOrService("S");
 * gstrsalereport.setServicesDescription("PG Charges");
 * gstrsalereport.setHsn_code("");
 * 
 * 
 * 
 * List<ChargingDetails> chargingDetails = chargingdetailsDao.findDetail(date,
 * payid, acquirerName, paymentType1, mopType, currency,transactionType);
 * if(user1.isSurchargeFlag()==false){ if(chargingDetails.size()>0){
 * 
 * double merTdr =0.0; double txtamo =0.0; double tdramount =0.0;
 * for(ChargingDetails chargingDetails2 : chargingDetails){ merTdr =
 * chargingDetails2.getMerchantTDR(); } tdramount =merTdr/100; txtamo =
 * amount*tdramount;
 * 
 * txtamount += txtamo;
 * 
 * netamt += amount; } } else{ double merTdr =0.0; if(chargingDetails.size()>0){
 * for(ChargingDetails chargingDetails2 : chargingDetails){ merTdr =
 * chargingDetails2.getMerchantServiceTax(); } txtamount += merTdr; netamt +=
 * amount; }
 * 
 * }
 * 
 * 
 * 
 * count++; } if(count!=0){ GstRSaleReport gstrsalereport = new
 * GstRSaleReport(); gstrsalereport.setPayId(payid);
 * gstrsalereport.setBusinessName(businessName);
 * gstrsalereport.setInvoiceNo(invoiceNo);
 * gstrsalereport.setGstNo(merhantGstNo); gstrsalereport.setGoodOrService("S");
 * gstrsalereport.setServicesDescription("PG Charges");
 * gstrsalereport.setHsn_code(""); gstrsalereport.setNetAmt(netamt);
 * gstrsalereport.setTxn_value(txtamount);
 * 
 * if(operationState.equalsIgnoreCase(adminstate)){ double txtValue =
 * txtamount/2; gstrsalereport.setcGst(txtValue);
 * gstrsalereport.setSGst(txtValue); } else{ gstrsalereport.setiGst(txtamount);
 * }
 * 
 * gstrsalereport.setOperationState(operationState);
 * gstrsalereport.setMonth(month); gstrsalereport.setYear(year); Date date = new
 * Date(); gstrsalereport.setCreatedDate(date); GstRSaleReport gstRSaleReport2 =
 * gstRSaleReportDao.findBymonthByPayId(payid, month, year); if(gstRSaleReport2
 * == null){ gstRSaleReportDao.create(gstrsalereport);
 * merchantList.add(gstrsalereport); } merchantList.add(gstRSaleReport2); }
 * 
 * 
 * cursor.close();
 * 
 * }
 * 
 * return merchantList;
 * 
 * 
 * 
 * }
 * 
 * 
 * }
 */
