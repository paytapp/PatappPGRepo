package com.paymentgateway.crm.action;

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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.MISReportObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
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

/**
 * @author Amitosh
 */

@Component
public class SettlementReportQuery {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;
	
	private static final String prefix = "MONGO_DB_";
	private final String CANCELLATION_PARTIAL_REFUND_FLAG = "C";
	private final String FULL_REFUND_FLAG = "R";
	private static Logger logger = LoggerFactory.getLogger(SettlementReportQuery.class.getName());
	public List<MISReportObject> settlementReportDownload(String merchantPayId, String subMerchantPayId, String acquirer, String currency,
			String fromDate, String toDate, String partSettle, String transactionFlag) {
		
		List<MISReportObject> transactionList = new ArrayList<MISReportObject>();
				try {
		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject currencyQuery = new BasicDBObject();
		BasicDBObject acquirerQuery = new BasicDBObject();
		BasicDBObject allParamQuery = new BasicDBObject();
		BasicDBObject txnCapturedFlag = new BasicDBObject();
		List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
		Map<String, User> userMap = new HashMap<String, User>();
		
		List<BasicDBObject> payIdConditionList = new ArrayList<BasicDBObject>();
		List<String> payIdList = new ArrayList<String>();
		BasicDBObject payIdQuery = new BasicDBObject();
		BasicDBObject payIdOrQuery = new BasicDBObject();
		BasicDBObject dateIndexConditionQuery = new BasicDBObject();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
		Date dateStart = format.parse(fromDate);
		Date dateEnd = format.parse(toDate);

		LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		String fromDatesIndex = startDate.toString().replaceAll("-", "");
		String toDatesIndex = endDate.toString().replaceAll("-", "");

		dateIndexConditionQuery.put("DATE_INDEX",
				BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDatesIndex).toLocalizedPattern())
						.add("$lte", new SimpleDateFormat(toDatesIndex).toLocalizedPattern()).get());
		
		if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
			String payIdArr[] = merchantPayId.split(",");
			for (String payId : payIdArr) {
				payIdList.add(payId.trim());
			}
			payIdQuery.append("$in", payIdList);
			payIdConditionList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payIdQuery));
			payIdConditionList.add(new BasicDBObject(FieldType.SUPER_MERCHANT_ID.getName(), payIdQuery));
			
			payIdOrQuery = new BasicDBObject("$or", payIdConditionList);
			paramConditionLst.add(payIdOrQuery);
		}

		if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
		}
		
		if (!currency.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
		}
		
		if (!partSettle.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettle));
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

		List<BasicDBObject> saleRefundTransactionList = new ArrayList<BasicDBObject>();
		saleRefundTransactionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
		saleRefundTransactionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));

		BasicDBObject saleRefundTransactioQuery = new BasicDBObject("$or", saleRefundTransactionList);
		paramConditionLst.add(saleRefundTransactioQuery);
		paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
		
		if (!dateIndexConditionQuery.isEmpty()) {
			paramConditionLst.add(dateIndexConditionQuery);
		}
		
		if (!currencyQuery.isEmpty()) {
			paramConditionLst.add(currencyQuery);
		}
		if (!acquirerQuery.isEmpty()) {
			paramConditionLst.add(acquirerQuery);
		}

		BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
		logger.info(finalquery.toString());
		
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
			
			MISReportObject transReport = new MISReportObject();

			if (userMap.get(doc.getString(FieldType.PAY_ID.toString())) != null) {

				transReport.setMerchants(userMap.get(doc.getString(FieldType.PAY_ID.toString())).getBusinessName());
			} else {
				User user = userDao.findPayId(doc.getString(FieldType.PAY_ID.toString()));
				transReport.setMerchants(user.getBusinessName());
				userMap.put(user.getPayId(), user);
			}
			
			if (/*!merchantPayId.equalsIgnoreCase("All") && */doc.containsKey(FieldType.SUB_MERCHANT_ID.getName())
					&& StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
				
				String subMerchant = doc.getString(FieldType.SUB_MERCHANT_ID.getName());
				User subMerchantUser = new User();

				if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
					subMerchantUser = userMap.get(subMerchant);
				} else {
					subMerchantUser = userDao.findPayId(subMerchant);
					userMap.put(subMerchant, subMerchantUser);
				}
				transReport.setSubMerchant(subMerchantUser.getBusinessName());
				transReport.setSubMerchantPayId(subMerchantUser.getPayId());
			} else {
				transReport.setSubMerchant(CrmFieldConstants.NA.getValue());
				transReport.setSubMerchantPayId(CrmFieldConstants.NA.getValue());
			}
			
			transReport.setTransactionId(doc.getString(FieldType.TXN_ID.toString()));
			transReport.setDateFrom(doc.getString(FieldType.CREATE_DATE.getName()));
			transReport.setPayId(doc.getString(FieldType.PAY_ID.toString()));
			transReport.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.toString()));
			transReport.setMopType(MopType.getmopName(doc.getString(FieldType.MOP_TYPE.toString())));
			transReport.setAcquirerType(doc.getString(FieldType.ACQUIRER_TYPE.toString()));
			transReport.setCardHolderType(doc.getString(FieldType.CARD_HOLDER_TYPE.getName()));

			if (doc.getString(FieldType.TXNTYPE.toString()).contains(TransactionType.REFUND.getName())) {
				transReport.setTxnType(TransactionType.REFUND.getName());
			} else {
				transReport.setTxnType(TransactionType.SALE.getName());
			}
			transReport.setTxnSettledType(doc.getString(FieldType.TXN_CAPTURE_FLAG.toString()));
			transReport.setPaymentMethods(PaymentType.getpaymentName(doc.getString(FieldType.PAYMENT_TYPE.toString())));
			transReport.setTotalAmount(doc.getString(FieldType.TOTAL_AMOUNT.toString()));
			transReport.setAmount(doc.getString(FieldType.AMOUNT.toString()));
			
			if(StringUtils.isNotBlank(doc.getString(FieldType.ACQUIRER_MODE.getName()))) {
				transReport.setAcquirerMode(doc.getString(FieldType.ACQUIRER_MODE.getName()));
			} else {
				transReport.setAcquirerMode("OFF_US");
			}
			
			//For sale transaction
			if (doc.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.SALE.getName())) {

				transReport.setGrossTransactionAmt(
						String.format("%.2f", Double.parseDouble(doc.getString(FieldType.TOTAL_AMOUNT.toString()))));

				if(doc.containsKey(FieldType.RESELLER_CHARGES.getName()) && doc.containsKey(FieldType.RESELLER_GST.getName()) 
						&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName())) && StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_GST.getName()))) {
					
					transReport.setAggregatorCommissionAMT(
							String.format("%.2f", (Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))
									+ Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))
									+ Double.parseDouble(doc.getString(FieldType.RESELLER_CHARGES.getName()))
									+ Double.parseDouble(doc.getString(FieldType.RESELLER_GST.getName())))));
					
				} else {
					
					transReport.setAggregatorCommissionAMT(
							String.format("%.2f", (Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))
									+ Double.parseDouble(doc.getString(FieldType.PG_GST.toString())))));
					
				}
				
				
				transReport.setAcquirerCommissionAMT(String.format("%.2f",
						(Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString())))));
				

				if (doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode())) {

					
					if(doc.containsKey(FieldType.RESELLER_CHARGES) && doc.containsKey(FieldType.RESELLER_GST.getName()) 
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName())) && StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_GST.getName()))) {
						
						transReport.setTotalAmtPayable("-"
								+ String.format("%.2f", (Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))
										+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))
										+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString()))
										+ Double.parseDouble(doc.getString(FieldType.RESELLER_CHARGES.getName()))
										+ Double.parseDouble(doc.getString(FieldType.RESELLER_GST.getName())))));

					} else {
						
						transReport.setTotalAmtPayable("-"
								+ String.format("%.2f", (Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))
										+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))
										+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString())))));						
					}
					
					if(doc.containsKey(FieldType.RESELLER_CHARGES) && doc.containsKey(FieldType.RESELLER_GST.getName()) 
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName())) && StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_GST.getName()))) {
						
						transReport.setTotalPayoutNodalAccount("-"
								+ String.format("%.2f", (Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))
										+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))
										+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString()))
										+ Double.parseDouble(doc.getString(FieldType.RESELLER_CHARGES.getName()))
										+ Double.parseDouble(doc.getString(FieldType.RESELLER_GST.getName())))));
					} else {
						
						transReport.setTotalPayoutNodalAccount("-"
								+ String.format("%.2f", (Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))
										+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))
										+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString())))));
					}
					
				
					
					

				} else {
					
					if(doc.containsKey(FieldType.RESELLER_CHARGES.getName()) && doc.containsKey(FieldType.RESELLER_GST.getName()) 
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName())) && StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_GST.getName()))) {
						
						transReport.setTotalAmtPayable(
								String.format("%.2f", Double.parseDouble(doc.getString(FieldType.TOTAL_AMOUNT.toString()))
										- (Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))
												+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												+ Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))
												+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString()))
												+ Double.parseDouble(doc.getString(FieldType.RESELLER_CHARGES.getName()))
												+ Double.parseDouble(doc.getString(FieldType.RESELLER_GST.getName())))));
						
					} else {
						transReport.setTotalAmtPayable(
								String.format("%.2f", Double.parseDouble(doc.getString(FieldType.TOTAL_AMOUNT.toString()))
										- (Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))
												+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												+ Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))
												+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString())))));
					}
					

					transReport.setTotalPayoutNodalAccount(String.format("%.2f",
							Double.parseDouble(doc.getString(FieldType.TOTAL_AMOUNT.toString()))
									- (Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString())))));
				}

				String sufTdr = doc.getString(FieldType.SUF_TDR.toString());
				String sufGST = doc.getString(FieldType.SUF_GST.toString());
				if( sufGST!=null && !sufGST.equalsIgnoreCase("false") && sufTdr!=null && !sufTdr.equalsIgnoreCase("false") && transReport.getTotalAmtPayable()!=null && transReport.getTotalAmtPayable()!="NA") {
					transReport.setSufCharges( String.format("%.2f", (Double.parseDouble(doc.getString(FieldType.SUF_TDR.toString()))
								+ Double.parseDouble(doc.getString(FieldType.SUF_GST.toString())))));
					transReport.setTotalAmtPayable((Double.parseDouble(transReport.getTotalAmtPayable())-Double.parseDouble(transReport.getSufCharges()))+"");
				}
				
			//	for refund
			} else if (doc.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName())) {
					
					transReport.setGrossTransactionAmt(String.format("%.2f",
							Double.parseDouble(doc.getString(FieldType.TOTAL_AMOUNT.toString())) * -1));
					
					transReport.setAggregatorCommissionAMT("0.00");
					transReport.setAcquirerCommissionAMT("0.00");
					
					if(doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode())) {
						
						transReport.setTotalAmtPayable("0.00");
						transReport.setTotalPayoutNodalAccount("0.00");
					} else {
					transReport.setTotalAmtPayable(String.format("%.2f", Double.parseDouble(doc.getString(FieldType.TOTAL_AMOUNT.toString())) * -1));
					
					if(doc.containsKey(FieldType.RESELLER_CHARGES.getName()) && doc.containsKey(FieldType.RESELLER_GST.getName()) 
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName())) && StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_GST.getName()))) {
						
						transReport.setTotalPayoutNodalAccount(String.format("%.2f",(Double.parseDouble(doc.getString(FieldType.TOTAL_AMOUNT.toString()))
								- (Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString()))
								+ Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))
								+ Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))
								+ Double.parseDouble(doc.getString(FieldType.RESELLER_CHARGES.getName()))
								+ Double.parseDouble(doc.getString(FieldType.RESELLER_GST.getName()))) )* -1));
						
					} else {
						
						transReport.setTotalPayoutNodalAccount(String.format("%.2f",(Double.parseDouble(doc.getString(FieldType.TOTAL_AMOUNT.toString()))
								- (Double.parseDouble(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(doc.getString(FieldType.ACQUIRER_GST.toString()))
								+ Double.parseDouble(doc.getString(FieldType.PG_TDR_SC.toString()))
								+ Double.parseDouble(doc.getString(FieldType.PG_GST.toString()))) )* -1));
						
					}
					
					}
				}
			
			

			if (StringUtils.isBlank(doc.getString(FieldType.PG_DATE_TIME.toString()))) {
				transReport.setTransactionDate("NA");
			} else {
				transReport.setTransactionDate(doc.getString(FieldType.PG_DATE_TIME.toString()));
			}
			
			if (StringUtils.isNotBlank(doc.getString(FieldType.REFUND_ORDER_ID.toString()))) {
				transReport.setRefundOrderId(doc.getString(FieldType.REFUND_ORDER_ID.toString()));
			} else {
				transReport.setRefundOrderId(CrmFieldConstants.NA.getValue());
			}
			
			transReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
			transReport.setRefundFlag(doc.getString(FieldType.REFUND_FLAG.toString()));

			if (StringUtils.isNotBlank(doc.getString(FieldType.PAYMENTS_REGION.toString()))) {
				transReport.setTransactionRegion(doc.getString(FieldType.PAYMENTS_REGION.toString()));
			} else {
				transReport.setTransactionRegion(AccountCurrencyRegion.DOMESTIC.toString());
			}
			transactionList.add(transReport);
		}
		cursor.close();
	} catch (Exception e) {
		logger.error("Exception " , e);
	}
	logger.info("Cursor closed for Settlement report query , transactionList Size = " + transactionList.size());
	return transactionList;
	}

	public List<MISReportObject> settlementReport(String merchantPayId, String acquirer, String currency,
			String fromDate, String toDate) {

		List<MISReportObject> transactionList = new ArrayList<MISReportObject>();

		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject currencyQuery = new BasicDBObject();
		BasicDBObject acquirerQuery = new BasicDBObject();
		BasicDBObject allParamQuery = new BasicDBObject();
		List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();

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
		if (!currency.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
		}

		if (!acquirer.equalsIgnoreCase("ALL")) {
			List<String> acquirerList = Arrays.asList(acquirer.split(","));
			for (String acq : acquirerList) {
				acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acq.trim()));
			}
			acquirerQuery.append("$or", acquirerConditionLst);
		}

		List<BasicDBObject> saleConditionList = new ArrayList<BasicDBObject>();
		saleConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
		saleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
		BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleConditionList);
		List<BasicDBObject> refundConditionList = new ArrayList<BasicDBObject>();
		refundConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
		refundConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

		BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundConditionList);

		List<BasicDBObject> bothConditionList = new ArrayList<BasicDBObject>();
		bothConditionList.add(saleConditionQuery);
		bothConditionList.add(refundConditionQuery);
		BasicDBObject addConditionListQuery = new BasicDBObject("$or", bothConditionList);
		if (!paramConditionLst.isEmpty()) {
			allParamQuery = new BasicDBObject("$and", paramConditionLst);
		}

		List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();
		if (!currencyQuery.isEmpty()) {
			fianlList.add(currencyQuery);
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
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		MongoCursor<Document> cursor = coll.find(finalquery).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			MISReportObject transReport = new MISReportObject();
			transReport.setTransactionId(doc.getString(FieldType.TXN_ID.toString()));
			transReport.setDateFrom(doc.getString(FieldType.CREATE_DATE.getName()));
			transReport.setPayId(doc.getString(FieldType.PAY_ID.toString()));
			transReport.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.toString()));
			transReport.setMopType(MopType.getmopName(doc.getString(FieldType.MOP_TYPE.toString())));
			transReport.setAcquirerType(doc.getString(FieldType.ACQUIRER_TYPE.toString()));

			if (doc.getString(FieldType.TXNTYPE.toString()).contains(TransactionType.REFUND.getName())) {
				transReport.setTxnType(TransactionType.REFUND.getName());
			} else {
				transReport.setTxnType(TransactionType.SALE.getName());
			}

			transReport.setPaymentMethods(PaymentType.getpaymentName(doc.getString(FieldType.PAYMENT_TYPE.toString())));
			transReport.setTotalAmount(doc.getString(FieldType.TOTAL_AMOUNT.toString()));
			transReport.setAmount(doc.getString(FieldType.AMOUNT.toString()));

			if (StringUtils.isBlank(doc.getString(FieldType.SURCHARGE_FLAG.toString()))) {
			} else {
				transReport.setSurchargeFlag("Y");
			}

			if (StringUtils.isBlank(doc.getString(FieldType.PG_DATE_TIME.toString()))) {
				transReport.setTransactionDate("NA");
			} else {
				transReport.setTransactionDate(doc.getString(FieldType.PG_DATE_TIME.toString()));
			}

			transReport.setTxnType(doc.getString(FieldType.ORIG_TXNTYPE.toString()));
			transReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
			transReport.setRefundFlag(doc.getString(FieldType.REFUND_FLAG.toString()));
			if (StringUtils.isNotBlank(doc.getString(FieldType.PAYMENTS_REGION.toString()))) {
				transReport.setTransactionRegion(doc.getString(FieldType.PAYMENTS_REGION.toString()));
			} else {
				transReport.setTransactionRegion(AccountCurrencyRegion.DOMESTIC.toString());
			}
			transactionList.add(transReport);
		}
		cursor.close();
		return transactionList;
	}

	public List<ChargingDetails> ChargingDetailsReport() {
		List<ChargingDetails> chDetails = null;
		return chDetails = userDao.findChargingDetail();
	}
}