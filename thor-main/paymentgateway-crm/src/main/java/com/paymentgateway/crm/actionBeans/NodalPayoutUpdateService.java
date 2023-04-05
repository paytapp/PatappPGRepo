package com.paymentgateway.crm.actionBeans;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.SummaryReportObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;

@Service
public class NodalPayoutUpdateService {

	private static Logger logger = LoggerFactory.getLogger(NodalPayoutUpdateService.class.getName());

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private UserDao userDao;

	@Autowired
	private PropertiesManager propertiesManager;

	private static final String prefix = "MONGO_DB_";

	public String updateNodalTransactions(String merchant, String acquirer,String nodalSettlementDate,
			String nodalType, String paymentType, String fromDate, String toDate,
			User user) {

		logger.info("Inside NodalPayoutUpdateService , updateNodalTransactions");
		DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date = new Date();
		String dateToday = dateFormat1.format(date);

		String status = "";

		if (nodalType.equalsIgnoreCase("nodalSettlement")) {
			status = StatusType.NODAL_SETTLED.getName();
		} else {
			status = StatusType.NODAL_PAYOUT.getName();
		}

		try {

			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject allParamQuery = new BasicDBObject();

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

			if (!merchant.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchant));
			}

			if (!paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (!acquirer.equalsIgnoreCase("ALL")) {

				paramConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acquirer));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
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

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			logger.info("finalquery for updateNodalTransactions = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.SETTLED_TRANSACTIONS_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			List<Document> documents = new ArrayList<>();
			int updatedCount = 0;
			int totalCount = 0;
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				totalCount++;
				// Check if record already entered
				String pgRefNum = doc.getString(FieldType.PG_REF_NUM.getName());
				List<BasicDBObject> saleTxnQuery1 = new ArrayList<BasicDBObject>();
				saleTxnQuery1.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
				saleTxnQuery1.add(new BasicDBObject(FieldType.STATUS.getName(), status));
				BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleTxnQuery1);
				long count = coll.count(saleConditionQuery);
				if (count > 0) {
					// Record is already entered , move to next
				} else {
					String txnId = TransactionManager.getNewTransactionId();
					TimeUnit.MILLISECONDS.sleep(2);

					Document document = new Document();
					document.put("_id", txnId);
					document.put(FieldType.TXN_ID.getName(), txnId);
					document.put(FieldType.PG_REF_NUM.getName(), doc.get(FieldType.PG_REF_NUM.getName()));
					document.put(FieldType.PAYMENTS_REGION.getName(), doc.get(FieldType.PAYMENTS_REGION.getName()));
					document.put(FieldType.POST_SETTLED_FLAG.getName(), doc.get(FieldType.POST_SETTLED_FLAG.getName()));
					document.put(FieldType.TXNTYPE.getName(), doc.get(FieldType.TXNTYPE.getName()));
					document.put(FieldType.ACQUIRER_TYPE.getName(), doc.get(FieldType.ACQUIRER_TYPE.getName()));
					document.put(FieldType.PAYMENT_TYPE.getName(), doc.get(FieldType.PAYMENT_TYPE.getName()));
					document.put(FieldType.CREATE_DATE.getName(), dateToday);
					document.put(FieldType.ORDER_ID.getName(), doc.get(FieldType.ORDER_ID.getName()));
					document.put(FieldType.PAY_ID.getName(), doc.get(FieldType.PAY_ID.getName()));
					document.put(FieldType.MOP_TYPE.getName(), doc.get(FieldType.MOP_TYPE.getName()));
					document.put(FieldType.CURRENCY_CODE.getName(), doc.get(FieldType.CURRENCY_CODE.getName()));
					document.put(FieldType.CARD_HOLDER_TYPE.getName(), doc.get(FieldType.CARD_HOLDER_TYPE.getName()));
					document.put(FieldType.PG_DATE_TIME.getName(), doc.get(FieldType.PG_DATE_TIME.getName()));
					document.put(FieldType.ACQ_ID.getName(), doc.get(FieldType.ACQ_ID.getName()));
					document.put(FieldType.RRN.getName(), doc.get(FieldType.RRN.getName()));
					document.put(FieldType.UDF6.getName(), doc.get(FieldType.UDF6.getName()));
					document.put(FieldType.SURCHARGE_FLAG.getName(), doc.get(FieldType.SURCHARGE_FLAG.getName()));
					document.put(FieldType.REFUND_FLAG.getName(), doc.get(FieldType.REFUND_FLAG.getName()));
					document.put(FieldType.ARN.getName(), doc.get(FieldType.ARN.getName()));
					document.put(FieldType.ORIG_TXN_ID.getName(), doc.get(FieldType.ORIG_TXN_ID.getName()));
					document.put(FieldType.TOTAL_AMOUNT.getName(), doc.get(FieldType.TOTAL_AMOUNT.getName()));
					document.put(FieldType.AMOUNT.getName(), doc.get(FieldType.AMOUNT.getName()));
					document.put(FieldType.SURCHARGE_ACQ.getName(), doc.get(FieldType.SURCHARGE_ACQ.getName()));
					document.put(FieldType.SURCHARGE_PG.getName(), doc.get(FieldType.SURCHARGE_PG.getName()));
					document.put(FieldType.SURCHARGE_PAYMENT_GATEWAY.getName(), doc.get(FieldType.SURCHARGE_PAYMENT_GATEWAY.getName()));
					document.put(FieldType.GST_ACQ.getName(), doc.get(FieldType.GST_ACQ.getName()));
					document.put(FieldType.GST_PG.getName(), doc.get(FieldType.GST_PG.getName()));
					document.put(FieldType.GST_PAYMENT_GATEWAY.getName(), doc.get(FieldType.GST_PAYMENT_GATEWAY.getName()));
					document.put(FieldType.STATUS.getName(), status);

					if (status.equalsIgnoreCase(StatusType.NODAL_SETTLED.getName())) {
						document.put(FieldType.NODAL_SETTLEMENT_DATE.getName(), nodalSettlementDate+" 00:00:00");
					} else if (status.equalsIgnoreCase(StatusType.NODAL_PAYOUT.getName())) {
						document.put(FieldType.NODAL_PAYOUT_DATE.getName(), nodalSettlementDate+" 00:00:00");
					} else {
						logger.info("Inside nodalPayoutUpdateService , no status type found");
						continue;
					}

					documents.add(document);
					updatedCount++;
				}

			}

			cursor.close();

			if (totalCount == 0) {
				logger.info(
						"No transactions in DB for settlements , payouts");
				
				return "Found 0 settled transactions in DB for selected date" ;
			}
			
			if (updatedCount == 0) {
				logger.info(
						"No transactions in DB for updating , payouts");
				
				return "Found total settled transactions " + totalCount + " , updated " + updatedCount
						+ " transactions with status " + status;
			}
			
			logger.info(
					"Adding " + documents.size() + " transactions in mongo document for nodal settlements , payouts");
			coll.insertMany(documents);

			return "Found total settled transactions " + totalCount + " , updated " + updatedCount
					+ " transactions with status " + status;
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return "Unable to update transactions!";
	}

	public List<SummaryReportObject> downloadNodalReport(String merchant, String acquirer,String nodalSettlementDate,
			String nodalType, String paymentType, String fromDate, String toDate,
			User user) throws SystemException {
		List<SummaryReportObject> transactionList = new ArrayList<SummaryReportObject>();
		
		logger.info("Inside SummaryReportQuery summaryReportDownload");
		Map<String,User> userMap = new HashMap<String,User>();
		
		String status = "";
		String dateType = "";
		if (nodalType.equalsIgnoreCase("nodalSettlement")) {
			status = StatusType.NODAL_SETTLED.getName();
			dateType = FieldType.NODAL_SETTLEMENT_DATE.getName();
		} else {
			status = StatusType.NODAL_PAYOUT.getName();
			dateType = FieldType.NODAL_PAYOUT_DATE.getName();
		}
		
		try {
			
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

				dateQuery.put(dateType,
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}
			
			if (!merchant.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchant));
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
			
			if (!status.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
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
			logger.info("finalquery for downloadNodalSettlementReport = "+finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.SETTLED_TRANSACTIONS_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();
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
				transReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
				String surchargeFlag = doc.getString(FieldType.SURCHARGE_FLAG.toString());
				if (StringUtils.isNotBlank(surchargeFlag)) {
					transReport.setSurchargeFlag(surchargeFlag);
				}
				
				transReport.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.toString()));
				if (StringUtils.isBlank(doc.getString(FieldType.PAYMENTS_REGION.toString()))) {
					
					transReport.setTransactionRegion(AccountCurrencyRegion.DOMESTIC.toString());
				}
				else {
					transReport.setTransactionRegion(doc.getString(FieldType.PAYMENTS_REGION.toString()));
				}
				
				if (StringUtils.isBlank(doc.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {
					
					transReport.setCardHolderType(CardHolderType.CONSUMER.toString());
				}
				else {
					transReport.setCardHolderType(doc.getString(FieldType.CARD_HOLDER_TYPE.toString()));
				}
				
				transReport.setTotalAmount(String.valueOf(doc.getDouble(FieldType.TOTAL_AMOUNT.toString())));
				transReport.setAcquirerType(doc.getString(FieldType.ACQUIRER_TYPE.toString()));
				
				if (null != doc.getString(FieldType.MOP_TYPE.toString())) {
					transReport.setMopType(MopType.getmopName(doc.getString(FieldType.MOP_TYPE.toString())));
				} else {
					transReport.setMopType(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				
				if (null != doc.getString(FieldType.PAYMENT_TYPE.toString())) {
					transReport.setPaymentMethods(doc.getString(FieldType.PAYMENT_TYPE.toString()));
				} else {
					transReport.setPaymentMethods(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				
				if (null != doc.getString(FieldType.STATUS.toString())) {
					transReport.setStatus(doc.getString(FieldType.STATUS.toString()));
				} else {
					transReport.setStatus(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				
				transReport.setNodalDate(doc.getString(dateType));
				
				/*if (null != doc.getString(FieldType.NODAL_SETTLEMENT_DATE.toString())) {
					transReport.setNodalDate(doc.getString(dateType));
				} else {
					transReport.setNodalDate(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				
				if (null != doc.getString(FieldType.NODAL_PAYOUT_DATE.toString())) {
					transReport.setNodalDate(doc.getString(FieldType.NODAL_PAYOUT_DATE.toString()));
				} else {
					transReport.setNodalDate(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				*/
			
				transReport.setCaptureDate(doc.getString(FieldType.PG_DATE_TIME.toString()));
				transReport.setTxnType(doc.getString(FieldType.TXNTYPE.toString()));
				transReport.setMerchants(doc.getString(CrmFieldType.BUSINESS_NAME.getName()));
				transReport.setAcqId(doc.getString(FieldType.ACQ_ID.toString()));
				transReport.setRrn(doc.getString(FieldType.RRN.toString()));
				transReport.setPostSettledFlag(doc.getString(FieldType.POST_SETTLED_FLAG.toString()));
				transReport.setDeltaRefundFlag(doc.getString(FieldType.UDF6.toString()));
				transReport.setDateFrom(doc.getString(FieldType.CREATE_DATE.toString()));
				
				
				transReport.setTdrScAcquirer(String.valueOf(doc.getDouble(FieldType.SURCHARGE_ACQ.toString())));
				transReport.setGstScAcquirer(String.valueOf(doc.getDouble(FieldType.GST_ACQ.toString())));
				
				Double surcharge = doc.getDouble(FieldType.SURCHARGE_PG.toString()) + doc.getDouble(FieldType.SURCHARGE_PAYMENT_GATEWAY.toString());
				Double gst = doc.getDouble(FieldType.GST_PG.toString()) + doc.getDouble(FieldType.GST_PAYMENT_GATEWAY.toString());
				Double divisor = 2.00;
				
				surcharge = surcharge / divisor;
				gst = gst / divisor;
				
				transReport.setTdrScPg(String.format("%.3f", surcharge));
				transReport.setGstScPg(String.format("%.3f", gst));
				transReport.setTdrScPaymentGateway(String.format("%.3f", surcharge));
				transReport.setGstScPaymentGateway(String.format("%.3f", gst));
				
							

				if (userMap.get(doc.getString(FieldType.PAY_ID.toString())) != null) {
					User userThis = userMap.get(doc.getString(FieldType.PAY_ID.toString()));
					transReport.setMerchants(userThis.getBusinessName());
				}
				else {
					User userThis = userDao.findPayId(doc.getString(FieldType.PAY_ID.toString()));
					transReport.setMerchants(userThis.getBusinessName());
					userMap.put(userThis.getPayId(), userThis);
				}
				transactionList.add(transReport);
			}

			cursor.close();

			
		} catch (Exception exception) {
			logger.error("Exception",exception);
		}
		return transactionList;
	}
}
