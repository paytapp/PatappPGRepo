package com.paymentgateway.commons.dao;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.NodalTransactions;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class NetSettledReportDao {
	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private ChargebackDao chargebackDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	private static final String prefix = "MONGO_DB_";
	private static Logger logger = LoggerFactory.getLogger(NetSettledReportDao.class.getName());

	SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");

	public List<NodalTransactions> fetchUpdatedData(String payId, String subMerchantId, String fromDate, String toDate,
			User sessionUser) {
		logger.info("Inside fetchUpdatedData ");
		List<NodalTransactions> utrTransactionList = new ArrayList<NodalTransactions>();
		
		try {

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject txnTypeQuery = new BasicDBObject();
			List<String> txnTypeConditionList = new ArrayList<String>();

			dateQuery.put(FieldType.PAYOUT_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());
			paramConditionLst.add(dateQuery);

			if (StringUtils.isNotBlank(payId) && !payId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			}

			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}

			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId()));
			}

			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

			txnTypeConditionList.add(TransactionType.RECO.getName());
			txnTypeConditionList.add(TransactionType.REFUNDRECO.getName());

			txnTypeQuery.append("$in", txnTypeConditionList);
			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnTypeQuery));

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			logger.info("final query in fetchUpdatedData : " + finalquery);
			BasicDBObject sort = null;
			if (StringUtils.isNotBlank(subMerchantId) && subMerchantId.equalsIgnoreCase("ALL")) {
				sort = new BasicDBObject("$sort", new BasicDBObject("SUB_MERCHANT_ID", -1));
			} else {
				sort = new BasicDBObject("$sort", new BasicDBObject("PAY_ID", -1));
			}
			
			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
			projectElement.put(FieldType.CREATE_DATE.getName(), 1);
			projectElement.put(FieldType.OID.getName(), 1);
			projectElement.put(FieldType.TXN_CAPTURE_FLAG.getName(), 1);
			projectElement.put(FieldType.DELTA_REFUND_FLAG.getName(), 1);
			projectElement.put(FieldType.TXNTYPE.getName(), 1);
			projectElement.put(FieldType.PAY_ID.getName(), 1);
			projectElement.put(FieldType.SUB_MERCHANT_ID.getName(), 1);
			projectElement.put(FieldType.PAYOUT_DATE.getName(), 1);
			projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);
			
			BasicDBObject project = new BasicDBObject("$project", projectElement);
			
			List<BasicDBObject> pipeline = Arrays.asList(match, project, sort);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			String dbPayId = null;
			String dbSubMerchantId = null;
			List<Document> settledTxns = new ArrayList<Document>();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				} 
				
				if (StringUtils.isNotBlank(subMerchantId) && subMerchantId.equalsIgnoreCase("ALL")) {
					if (dbSubMerchantId == null) {
						dbPayId = dbobj.getString(FieldType.PAY_ID.toString());
						dbSubMerchantId = dbobj.getString(FieldType.SUB_MERCHANT_ID.toString());
					} else if (!dbSubMerchantId.equals(dbobj.getString(FieldType.SUB_MERCHANT_ID.toString()))) {
						utrTransactionList.add(getCapturedTransactionByOid(settledTxns, dbPayId, dbSubMerchantId,
								fromDate, toDate, sessionUser));
						dbSubMerchantId = dbobj.getString(FieldType.SUB_MERCHANT_ID.toString());
						settledTxns.clear();
					}
					settledTxns.add(dbobj);
				} else {
					if (dbPayId == null) {
						dbPayId = dbobj.getString(FieldType.PAY_ID.toString());
					} else if (!dbPayId.equals(dbobj.getString(FieldType.PAY_ID.toString()))) {
						utrTransactionList.add(getCapturedTransactionByOid(settledTxns, dbPayId, subMerchantId,
								fromDate, toDate, sessionUser));
						dbPayId = dbobj.getString(FieldType.PAY_ID.toString());
						settledTxns.clear();
					}
					settledTxns.add(dbobj);
				}

			}
			if (StringUtils.isNotBlank(dbPayId) && !settledTxns.isEmpty()) {
				if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
					dbSubMerchantId = subMerchantId;
				}
				utrTransactionList.add(getCapturedTransactionByOid(settledTxns, dbPayId, dbSubMerchantId, fromDate,
						toDate, sessionUser));
			} else {
				utrTransactionList = getPreviousChargeback(payId, subMerchantId, fromDate, toDate);

			}
		} catch (Exception exp) {
			logger.error("Exception caught in fetchUpdatedData : ", exp);
		}

		return utrTransactionList;
	}

	private NodalTransactions getCapturedTransactionByOid(List<Document> settledTxns, String payId,
			String subMerchantId, String fromDate, String toDate, User sessionUser) {
		logger.info("Inside getCapturedTransactionByOid ");
		NodalTransactions utrTransaction = new NodalTransactions();
		String merchantName = null;
		String subMerchantName = null;
		Set<String> OIDset = new HashSet<String>();
		int saleCaptureCount = 0;
		int saleSettledCount = 0;
		int refundCaptureCount = 0;
		int refundSettledCount = 0;

		String captureFromDate = null;
		String captureToDate = null;

		BigDecimal chargebackCrAmount = new BigDecimal("0.00");
		BigDecimal chargebackDrAmount = new BigDecimal("0.00");

		BigDecimal saleCaptureAmount = new BigDecimal("0.00");
		BigDecimal saleSettledAmount = new BigDecimal("0.00");
		BigDecimal refundCaptureAmount = new BigDecimal("0.00");
		BigDecimal refundSettledAmount = new BigDecimal("0.00");

		BigDecimal netSettledAmount = new BigDecimal("0.00");
		
		String dbPayoutDate = null;
		try {
			for (Document docObj : settledTxns) {
				String oid = docObj.getString(FieldType.OID.getName());

				if (!OIDset.contains(oid)) {
					List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
					queryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
					queryList
							.add(new BasicDBObject(FieldType.OID.getName(), docObj.getString(FieldType.OID.getName())));

					if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
						queryList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
					}
					BasicDBObject andQuery = new BasicDBObject("$and", queryList);
					MongoDatabase dbIns = mongoInstance.getDB();
					
					MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
					
					BasicDBObject match = new BasicDBObject("$match", andQuery);
					
					BasicDBObject projectElement = new BasicDBObject();
					projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
					projectElement.put(FieldType.CREATE_DATE.getName(), 1);
					projectElement.put(FieldType.TXNTYPE.getName(), 1);
					projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);
					
				BasicDBObject project = new BasicDBObject("$project", projectElement);
					
					List<BasicDBObject> pipeline = Arrays.asList(match, project);
					AggregateIterable<Document> output = coll.aggregate(pipeline);
					output.allowDiskUse(true);
				MongoCursor<Document> cursor = output.iterator();

					while (cursor.hasNext()) {
						Document dbobj = cursor.next();

						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

							dbobj = dataEncDecTool.decryptDocument(dbobj);
						} 
						
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString())) && dbobj
								.getString(FieldType.TXNTYPE.toString()).equals(TransactionType.SALE.getName())) {
							saleCaptureCount++;
							saleCaptureAmount = saleCaptureAmount
								.add(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString())));
							
							if(captureFromDate == null) {
								captureFromDate = dbobj.getString(FieldType.CREATE_DATE.toString());
							}
						if(captureToDate == null) {
								captureToDate = dbobj.getString(FieldType.CREATE_DATE.toString());
							}
//							logger.info("_id : " + dbobj.getString("_id"));
							Date d1 = sdformat.parse(captureFromDate.split(" ")[0]);
							Date d2 = sdformat.parse(captureToDate.split(" ")[0]);
							Date d3 = sdformat.parse(dbobj.getString(FieldType.CREATE_DATE.toString()).split(" ")[0]);
							
						if(d1.compareTo(d3) > 0) {
								captureFromDate = dbobj.getString(FieldType.CREATE_DATE.toString());
							}
							
							if(d2.compareTo(d3) < 0) {
								captureToDate = dbobj.getString(FieldType.CREATE_DATE.toString());
							}
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString())) && dbobj
								.getString(FieldType.TXNTYPE.toString()).equals(TransactionType.REFUND.getName())) {
							refundCaptureCount++;
							refundCaptureAmount = refundCaptureAmount
									.add(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString())));
						}
					}

				}
				if ((StringUtils.isNotBlank(docObj.getString(FieldType.TXN_CAPTURE_FLAG.toString()))
						&& docObj.getString(FieldType.TXN_CAPTURE_FLAG.toString())
								.equals(Constants.POST_CAPTURED_TXN.getValue()))
						|| (StringUtils.isNotBlank(docObj.getString(FieldType.DELTA_REFUND_FLAG.toString()))
								&& docObj.getString(FieldType.DELTA_REFUND_FLAG.toString()).equals("Y"))) {
					
				} else {
					if (StringUtils.isNotBlank(docObj.getString(FieldType.TXNTYPE.toString()))
							&& docObj.getString(FieldType.TXNTYPE.toString()).equals(TransactionType.RECO.getName())) {
						saleSettledCount++;
						saleSettledAmount = saleSettledAmount
								.add(new BigDecimal(docObj.getString(FieldType.TOTAL_AMOUNT.toString())));
					}

					if (StringUtils.isNotBlank(docObj.getString(FieldType.TXNTYPE.toString())) && docObj
							.getString(FieldType.TXNTYPE.toString()).equals(TransactionType.REFUNDRECO.getName())) {
						refundSettledCount++;
						refundSettledAmount = refundSettledAmount
								.add(new BigDecimal(docObj.getString(FieldType.TOTAL_AMOUNT.toString())));
					}
				}
				if (merchantName == null) {
				merchantName = userDao.getBusinessNameByPayId(docObj.getString(FieldType.PAY_ID.toString()));
				}

				if (subMerchantName == null && StringUtils.isNotBlank(subMerchantId)) {
					subMerchantName = userDao
							.getBusinessNameByPayId(docObj.getString(FieldType.SUB_MERCHANT_ID.toString()));
				}
				if (dbPayoutDate == null) {
					dbPayoutDate = docObj.getString(FieldType.PAYOUT_DATE.toString());
				}
				OIDset.add(oid);
			}

			List<Chargeback> chargebackList = getCharbackList(payId, subMerchantId, fromDate, toDate, sessionUser); //chargebackDao.findChargebackForPayout(payId, subMerchantId, fromDate, toDate);

			for (Chargeback chargeback : chargebackList) {

				if (chargeback.isHoldAmountFlag()) {
					if (chargeback.getStatus().equalsIgnoreCase("Rejected")
							&& StringUtils.isNotBlank(chargeback.getAdminStatus())
							&& (chargeback.getAdminStatus().contains("admin")
									|| chargeback.getAdminStatus().contains("subAdmin"))) {
						chargebackCrAmount = chargebackCrAmount.add(chargeback.getTotalchargebackAmount());
					} else {
						chargebackDrAmount = chargebackDrAmount.add(chargeback.getTotalchargebackAmount());
					}
//					else if (!(chargeback.getStatus().equalsIgnoreCase("Closed")
//							|| chargeback.getStatus().equalsIgnoreCase("Refunded"))) {
//						chargebackDrAmount = chargebackDrAmount.add(chargeback.getTotalchargebackAmount());
//					}
				}
			}

			utrTransaction.setMerchantName(merchantName);
			if (StringUtils.isNotBlank(subMerchantName)) {
				utrTransaction.setSubMerchantName(subMerchantName);
			} else {
				utrTransaction.setSubMerchantName(CrmFieldConstants.NA.getValue());
			}

			utrTransaction.setPayId(payId);
			if (StringUtils.isNotBlank(subMerchantId)) {
				utrTransaction.setSubMerchantId(subMerchantId);
			}

			utrTransaction.setSaleCaptureTxn(String.valueOf(saleCaptureCount));
			utrTransaction.setRefundCaptureTxn(String.valueOf(refundCaptureCount));
			utrTransaction.setSaleSettledTxn(String.valueOf(saleSettledCount));
			utrTransaction.setRefundSettledTxn(String.valueOf(refundSettledCount));

			utrTransaction.setSaleCaptureAmnt(String.valueOf(saleCaptureAmount));
			utrTransaction.setSaleSettledAmnt(String.valueOf(saleSettledAmount));
			utrTransaction.setRefundCaptureAmnt(String.valueOf(refundCaptureAmount));
			utrTransaction.setRefundSettledAmnt(String.valueOf(refundSettledAmount));

			if (chargebackDrAmount.toString().equals("0.00")) {
				utrTransaction.setChargebackDr(CrmFieldConstants.NA.getValue());
			} else {
				utrTransaction.setChargebackDr(String.valueOf(chargebackDrAmount));
			}
			if (chargebackCrAmount.toString().equals("0.00")) {
				utrTransaction.setChargebackCr(CrmFieldConstants.NA.getValue());
			} else {
				utrTransaction.setChargebackCr(String.valueOf(chargebackCrAmount));
			}

			utrTransaction.setPayOutDate(dbPayoutDate);

			utrTransaction.setCaptureFromDate(captureFromDate);
			utrTransaction.setCaptureToDate(captureToDate);

			Document dbobj = getOtehrAdjustmentAmount(payId, subMerchantId, fromDate);
			if (dbobj != null) {
				if (StringUtils.isBlank(dbobj.getString(FieldType.ADJUSTMENT_AMOUNT_CR.getName()))) {
					utrTransaction.setOtherAdjustmentCr("");
				} else {
					utrTransaction.setOtherAdjustmentCr(dbobj.getString(FieldType.ADJUSTMENT_AMOUNT_CR.getName()));
				}

				if (StringUtils.isBlank(dbobj.getString(FieldType.ADJUSTMENT_AMOUNT_DR.getName()))) {
					utrTransaction.setOtherAdjustmentDr("");
				} else {
					utrTransaction.setOtherAdjustmentDr(dbobj.getString(FieldType.ADJUSTMENT_AMOUNT_DR.getName()));
				}

			} else {
				utrTransaction.setOtherAdjustmentCr("");
				utrTransaction.setOtherAdjustmentDr("");
			}
			// Net Settled Calculation.

			BigDecimal saleSum = saleSettledAmount
					.add(chargebackCrAmount.add(new BigDecimal(utrTransaction.getOtherAdjustmentCr().equals("") ? "0.00" : utrTransaction.getOtherAdjustmentCr())));
			BigDecimal refundSum = refundSettledAmount
					.add(chargebackDrAmount.add(new BigDecimal(utrTransaction.getOtherAdjustmentDr().equals("") ? "0.00" : utrTransaction.getOtherAdjustmentDr())));
			netSettledAmount = saleSum.subtract(refundSum);
			utrTransaction.setNetSettled(String.valueOf(netSettledAmount));
		} catch (Exception ex) {
			logger.error("Exception caught in getCapturedTransactionByOid : ", ex);
		}
		return utrTransaction;
	}

	private Document getOtehrAdjustmentAmount(String payId, String subMerchantId, String payOutDate) {
		logger.info("Inside getOtehrAdjustmentAmount ");
		try {
			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();

			String payOutDateArray[] = payOutDate.split(" ");
			String fromDate = payOutDateArray[0] + " 00:00:00";
			String toDate = payOutDateArray[0] + " 23:59:59";

			dateQuery.put(FieldType.PAYOUT_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());

			queryList.add(dateQuery);
			queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			if (StringUtils.isNotBlank(subMerchantId)) {
				queryList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}
			BasicDBObject andQuery = new BasicDBObject("$and", queryList);
			logger.info("Query in getOtehrAdjustmentAmount : " + andQuery);
			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.NET_SETTLED_ADJUSTMENT_COLLECTION.getValue()));
			MongoCursor<Document> cursor = coll.find(andQuery).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				} 
				
				return dbobj;
			}
		} catch (Exception ex) {
			logger.error("Exception caught in getOtehrAdjustmentAmount : ", ex);
			return null;
		}
		return null;
	}

	public void editOtherAdjustmentAmount(String payId, String subMerchantId, String payOutDate,
			String otherAdjustmentCr, String otherAdjustmentDr) throws SystemException {
		logger.info("Inside editOtherAdjustmentAmount ");
		try {
			BasicDBObject dateQuery = new BasicDBObject();
			String[] payOutDateArray = payOutDate.split(" ");
			String fromDate = payOutDateArray[0] + " 00:00:00";
			String toDate = payOutDateArray[0] + " 23:59:59";
			dateQuery.put(FieldType.PAYOUT_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());
			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(dateQuery);
			queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			if (StringUtils.isNotBlank(subMerchantId)) {
				queryList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}
			BasicDBObject andQuery = new BasicDBObject("$and", queryList);
			logger.info("Query in editOtherAdjustmentAmount : " + andQuery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.NET_SETTLED_ADJUSTMENT_COLLECTION.getValue()));
			MongoCursor<Document> cursor = coll.find(andQuery).iterator();
			if (cursor.hasNext()) {
				Document oldDoc = (Document) cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					oldDoc = dataEncDecTool.decryptDocument(oldDoc);
				} 
				
				Bson filter;
				if (StringUtils.isNotBlank(oldDoc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
					filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
							oldDoc.getString(FieldType.SUB_MERCHANT_ID.getName())).append(
									FieldType.ADJUSTMENT_TXN_ID.getName(),
									oldDoc.getString(FieldType.ADJUSTMENT_TXN_ID.getName()));
				} else {
					filter = new Document(FieldType.PAY_ID.getName(), oldDoc.getString(FieldType.PAY_ID.getName()))
							.append(FieldType.ADJUSTMENT_TXN_ID.getName(),
									oldDoc.getString(FieldType.ADJUSTMENT_TXN_ID.getName()));
				}
				Bson newValue = new Document(FieldType.ADJUSTMENT_AMOUNT_CR.getName(), otherAdjustmentCr)
						.append(FieldType.ADJUSTMENT_AMOUNT_DR.getName(), otherAdjustmentDr);
				Bson updateDocument = new Document("$set", newValue);
				coll.updateOne(filter, updateDocument);
			} else {
				String txnId = TransactionManager.getNewTransactionId();
				BasicDBObject newFieldsObj = new BasicDBObject();
				newFieldsObj.put(FieldType.ADJUSTMENT_TXN_ID.getName(), txnId);
				newFieldsObj.put(FieldType.PAY_ID.getName(), payId);
				newFieldsObj.put(FieldType.PAYOUT_DATE.getName(), payOutDate);
				newFieldsObj.put(FieldType.ADJUSTMENT_AMOUNT_CR.getName(), otherAdjustmentCr);
				newFieldsObj.put(FieldType.ADJUSTMENT_AMOUNT_DR.getName(), otherAdjustmentDr);
				if (StringUtils.isNotBlank(subMerchantId)) {
					newFieldsObj.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
				}
				Document doc = new Document(newFieldsObj);
				coll.insertOne(doc);
			}
		} catch (Exception ex) {
			String message = "Exception in editOtherAdjustmentAmount : ";
			logger.error(message, ex);
			throw new SystemException(ErrorType.DATABASE_ERROR, ex, message);
		}
	}

	public List<Chargeback> getCharbackList(String payId, String subMerchantId, String fromDate, String toDate,
			User sessionUser) throws ParseException {
		logger.info("Inside getCharbackList ");
		List<Chargeback> chargebackList = new ArrayList<Chargeback>();
		int count = 0;
		int iterations = 0;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(df.parse(fromDate));
		calendar.add(Calendar.DATE, -1);
		String dateTo = sdformat.format(calendar.getTime()) + " 23:59:59";

		while (iterations < 3) {
			calendar.add(Calendar.DATE, -1);
			String dateFrom = sdformat.format(calendar.getTime()) + " 00:00:00";
			count = getPreviousSettledCount(payId, subMerchantId, dateFrom, dateTo, sessionUser);
			if (count == 0) {
				chargebackList = chargebackDao.findChargebackForPayout(payId, subMerchantId, dateFrom, dateTo);
				break;
			}
			iterations++;
		}

		chargebackList.addAll(chargebackDao.findChargebackForPayout(payId, subMerchantId, fromDate, toDate));

		return chargebackList;
	}

	private int getPreviousSettledCount(String payId, String subMerchantId, String fromDate, String toDate,
			User sessionUser) {
		logger.info("Inside getPreviousSettledCount ");
		int count = 0;
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject txnTypeQuery = new BasicDBObject();
			List<String> txnTypeConditionList = new ArrayList<String>();

			dateQuery.put(FieldType.PAYOUT_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());
			paramConditionLst.add(dateQuery);

			if (StringUtils.isNotBlank(payId) && !payId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			}

			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}

			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId()));
			}

			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

			txnTypeConditionList.add(TransactionType.RECO.getName());
			txnTypeConditionList.add(TransactionType.REFUNDRECO.getName());

			txnTypeQuery.append("$in", txnTypeConditionList);
			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnTypeQuery));

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("Query in getPreviousSettledCount : " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			count = (int) coll.count(finalquery);
		} catch (Exception ex) {
			logger.error("Exception in getPreviousSettledCount : ", ex);
		}
		return count;
	}

	public List<NodalTransactions> getPreviousChargeback(String payId, String subMerchantId, String fromDate,
			String toDate) {
		logger.info("Inside getPreviousChargeback ");
		List<NodalTransactions> chargebackTxnList = new ArrayList<NodalTransactions>();
		List<Chargeback> chargebackList = chargebackDao.findChargebackForPayout(payId, subMerchantId, fromDate, toDate);
		List<Chargeback> Listchargeback = new ArrayList<Chargeback>();
		BigDecimal chargebackCrAmount = new BigDecimal("0.00");
		BigDecimal chargebackDrAmount = new BigDecimal("0.00");
		BigDecimal zeroDecimal = new BigDecimal("0.00");
		String dbPayId = null;
		String dbSubMerchantPayId = null;
		for (Chargeback chargeback : chargebackList) {

			if (chargeback.isHoldAmountFlag()) {

				if ((StringUtils.isNotBlank(subMerchantId) && subMerchantId.equalsIgnoreCase("ALL"))
						|| StringUtils.isNotBlank(chargeback.getSubMerchantId())) {

					if (dbSubMerchantPayId == null) {
						dbPayId = chargeback.getPayId();
						dbSubMerchantPayId = chargeback.getSubMerchantId();
					} else if (dbSubMerchantPayId.equals(chargeback.getSubMerchantId())) {

					} else {
						chargebackTxnList.addAll(getPreviousChargebachObject(Listchargeback, payId, dbSubMerchantPayId,
								chargebackCrAmount, chargebackDrAmount, fromDate, toDate));

						chargebackCrAmount = new BigDecimal("0.00");
						chargebackDrAmount = new BigDecimal("0.00");
						dbSubMerchantPayId = chargeback.getSubMerchantId();
						Listchargeback.clear();
					}

					if (chargeback.getStatus().equalsIgnoreCase("Rejected")
							&& StringUtils.isNotBlank(chargeback.getAdminStatus())
							&& (chargeback.getAdminStatus().contains("admin")
									|| chargeback.getAdminStatus().contains("subAdmin"))) {
						chargebackCrAmount = chargebackCrAmount.add(chargeback.getTotalchargebackAmount());
					} else {
						chargebackDrAmount = chargebackDrAmount.add(chargeback.getTotalchargebackAmount());
					}
					Listchargeback.add(chargeback);

				} else if (payId.equalsIgnoreCase("ALL")) {

					if (dbPayId == null) {
						dbPayId = chargeback.getPayId();
					} else if (dbPayId.equals(chargeback.getPayId())) {

					} else if (!dbPayId.equals(chargeback.getPayId())) {
						chargebackTxnList.addAll(getPreviousChargebachObject(Listchargeback, dbPayId, "",
								chargebackCrAmount, chargebackDrAmount, fromDate, toDate));

						chargebackCrAmount = new BigDecimal("0.00");
						chargebackDrAmount = new BigDecimal("0.00");
						dbPayId = chargeback.getPayId();
						Listchargeback.clear();
					}

					if (chargeback.getStatus().equalsIgnoreCase("Rejected")
							&& StringUtils.isNotBlank(chargeback.getAdminStatus())
							&& (chargeback.getAdminStatus().contains("admin")
									|| chargeback.getAdminStatus().contains("subAdmin"))) {
						chargebackCrAmount = chargebackCrAmount.add(chargeback.getTotalchargebackAmount());
					} else {
						chargebackDrAmount = chargebackDrAmount.add(chargeback.getTotalchargebackAmount());
					}
					Listchargeback.add(chargeback);
				} else {
					if (chargeback.getStatus().equalsIgnoreCase("Rejected")
							&& StringUtils.isNotBlank(chargeback.getAdminStatus())
							&& (chargeback.getAdminStatus().contains("admin")
									|| chargeback.getAdminStatus().contains("subAdmin"))) {
						chargebackCrAmount = chargebackCrAmount.add(chargeback.getTotalchargebackAmount());
					} else {
						chargebackDrAmount = chargebackDrAmount.add(chargeback.getTotalchargebackAmount());
					}
					Listchargeback.add(chargeback);
				}

			}

		}
		if (!Listchargeback.isEmpty() && chargebackDrAmount.add(chargebackCrAmount).compareTo(zeroDecimal) > 0) {
			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				dbSubMerchantPayId = subMerchantId;
			}
			if (StringUtils.isNotBlank(payId) && !payId.equalsIgnoreCase("ALL")) {
				dbPayId = payId;
			}
			chargebackTxnList.addAll(getPreviousChargebachObject(Listchargeback, dbPayId, dbSubMerchantPayId,
					chargebackCrAmount, chargebackDrAmount, fromDate, toDate));
		}

		return chargebackTxnList;
	}

	public List<NodalTransactions> getPreviousChargebachObject(List<Chargeback> Listchargeback, String payId,
			String subMerchantId, BigDecimal chargebackCrAmount, BigDecimal chargebackDrAmount, String fromDate,
			String toDate) {
		logger.info("Inside getPreviousChargebachObject ");
		List<NodalTransactions> transactionObjectList = new ArrayList<NodalTransactions>();
		NodalTransactions utrTransaction = new NodalTransactions();

		utrTransaction.setMerchantName(userDao.getBusinessNameByPayId(payId));
		if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
			utrTransaction.setSubMerchantName(userDao.getBusinessNameByPayId(subMerchantId));
		} else {
			utrTransaction.setSubMerchantName(CrmFieldConstants.NA.getValue());
		}

		utrTransaction.setChargebackCr(String.valueOf(chargebackCrAmount));
		utrTransaction.setChargebackDr(String.valueOf(chargebackDrAmount));
		utrTransaction.setCaptureFromDate(fromDate);
		utrTransaction.setCaptureToDate(toDate);
		utrTransaction.setOtherAdjustmentCr("");
		utrTransaction.setOtherAdjustmentDr("");
		utrTransaction.setPayOutDate("NA");
		utrTransaction.setSaleCaptureAmnt("0.00");
		utrTransaction.setSaleCaptureTxn("0");
		utrTransaction.setRefundCaptureTxn("0");
		utrTransaction.setRefundCaptureAmnt("0.00");
		utrTransaction.setSaleSettledTxn("0");
		utrTransaction.setSaleSettledAmnt("0.00");
		utrTransaction.setRefundSettledTxn("0");
		utrTransaction.setRefundSettledAmnt("0.00");
		if (!utrTransaction.getChargebackDr().equals("0.00")) {
			utrTransaction.setNetSettled("-" + utrTransaction.getChargebackDr());
		} else {
			utrTransaction.setNetSettled(utrTransaction.getChargebackCr());
		}

		transactionObjectList.add(utrTransaction);

		return transactionObjectList;
	}

	public void insertFileStatusInDB(String payOutDate, String fileName, String fileLocation, String sessionPayId) {
		logger.info("Inside insertFileStatusInDB ");
		try {
			SimpleDateFormat sdfcurrdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			payOutDate = payOutDate.split(" ")[0];
			Date currDate = new Date();
			String currentdate = sdfcurrdate.format(currDate);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.NET_SETTLED_FILE_COLLECTION.getValue()));
			BasicDBObject finalQuery = new BasicDBObject();
			if (StringUtils.isNotBlank(fileName)) {
				finalQuery.put("FILENAME", fileName);
			}
			if (StringUtils.isNotBlank(payOutDate)) {
				finalQuery.put(FieldType.PAYOUT_DATE.getName(), payOutDate);
			}
			logger.info("Query in insertFileStatusInDB : " + finalQuery);
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			if (cursor.hasNext()) {
				Document oldDoc = (Document) cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					oldDoc = dataEncDecTool.decryptDocument(oldDoc);
				} 
				
				if (oldDoc.getString(FieldType.STATUS.getName()).equals(StatusType.PROCESSING.getName())) {
					Bson filter = new Document("FILENAME", oldDoc.getString("FILENAME"));
					Bson newValue = new Document(FieldType.CREATE_DATE.getName(), currentdate).append(FieldType.STATUS.getName(), "Ready");
					Bson updateDocument = new Document("$set", newValue);
					coll.updateOne(filter, updateDocument);
				}
			} else {
				Document doc = new Document();
				if (StringUtils.isNotBlank(sessionPayId)) {
					doc.put(FieldType.PAY_ID.getName(), sessionPayId);
				}
				doc.put(FieldType.PAYOUT_DATE.getName(), payOutDate);
				doc.put(FieldType.CREATE_DATE.getName(), currentdate);
				doc.put("LOCATION", fileLocation);
				doc.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
				doc.put("FILENAME", fileName);
				coll.insertOne(doc);
			}
		} catch (Exception e) {
			logger.error("Exception while inserting the data in insertFileStatusInDB ", e);
		}
	}

	public List<NodalTransactions> fetchNetSettledFiles(String createDate, String sessionPayId, String fileLocation) {
		logger.info("Inside fetchNetSettledFiles ");
		List<NodalTransactions> dataList = new ArrayList<NodalTransactions>();
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.NET_SETTLED_FILE_COLLECTION.getValue()));
			BasicDBObject finalQuery = new BasicDBObject();
			if (StringUtils.isNotBlank(createDate)) {
				String setDateFrom = DateCreater.toDateTimeformatCreater(createDate);
				String setDateTo = DateCreater.formDateTimeformatCreater(createDate);
				finalQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(setDateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(setDateTo).toLocalizedPattern()).get());
			}
			if (StringUtils.isNotBlank(sessionPayId)) {
				finalQuery.put(FieldType.PAY_ID.getName(), sessionPayId);
			}
			logger.info("Query in fetchNetSettledFiles : " + finalQuery);
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			File[] files = new File(fileLocation).listFiles();
			
			while (cursor.hasNext()) {
				NodalTransactions data = new NodalTransactions();
				Document dbobj = (Document) cursor.next();
				
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}
				
				data.setCreatedDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				data.setPayOutDate(dbobj.getString(FieldType.PAYOUT_DATE.getName()));
				data.setFileName(dbobj.getString("FILENAME"));
				data.setStatus(dbobj.getString(FieldType.STATUS.getName()));
				if(data.getStatus().equalsIgnoreCase("Ready")){
				for (File file : files) {
					if (file.getName().equalsIgnoreCase(data.getFileName())) {
						dataList.add(data);
					}
				}
				}else {
					dataList.add(data);
				}
			}
		} catch (Exception e) {
			logger.error("Exception in fetchNetSettledFiles() ", e);
		}
		return dataList;
	}

	public boolean getFileStatus(String createDate, String fileName) {
		logger.info("Inside getFileStatus ");
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.NET_SETTLED_FILE_COLLECTION.getValue()));
			BasicDBObject finalQuery = new BasicDBObject();
			if (StringUtils.isNotBlank(createDate)) {
				finalQuery.put(FieldType.CREATE_DATE.getName(), createDate);
			}
			if (StringUtils.isNotBlank(fileName)) {
				finalQuery.put("FILENAME", fileName);
			}
			logger.info("Query in getFileStatus : " + finalQuery);
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			if (cursor.hasNext()) {
				return true;
			}
		} catch (Exception e) {
			logger.error("Exception in getFileStatus() ", e);
		}
		return false;
	}
	
	public String checkFileStatus(String payOutDate, String fileName) {
		logger.info("Inside checkFileStatus ");
		try {
			String dateFrom = DateCreater.toDateTimeformatCreater(payOutDate);
			String dateTo = DateCreater.formDateTimeformatCreater(payOutDate);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.NET_SETTLED_FILE_COLLECTION.getValue()));
			BasicDBObject finalQuery = new BasicDBObject();
			finalQuery.put(FieldType.PAYOUT_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());

			finalQuery.put("FILENAME", fileName);

			logger.info("Query in checkFileStatus : " + finalQuery);
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			if (cursor.hasNext()) {
				Document dbobj = cursor.next();
				return dbobj.getString(FieldType.STATUS.getName());
			}
		} catch (Exception e) {
			logger.error("Exception in getFileStatus() ", e);
		}
		return StatusType.FAILED.getName();
	}
	
	public void updateFailedFileStatus(String dateTo, String dateFrom, String fileName, String userType, String fileType) {
		logger.info("Inside updateFailedFileStatus ");
		try {
			
			
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.NET_SETTLED_FILE_COLLECTION.getValue()));
			Document doc = new Document();

			
			doc.put(FieldType.DATE_FROM.getName(), dateFrom);
			doc.put(FieldType.DATE_TO.getName(), dateTo);
			doc.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
			doc.put("FILENAME", fileName);
			logger.info("Update Failed NetSettled Status , Query "+doc.toString());

			Bson filter=doc;
			
			Document newDoc = new Document(new BasicDBObject("$set", new BasicDBObject(FieldType.STATUS.getName(),StatusType.FAILED.getName())));
			
			coll.updateOne(filter, newDoc);
			

		} catch (Exception e) {
			logger.error("Exception while inserting the data in updateFailedFileStatus " , e);
		}
		
	}
	
	public void deleteFileStatus(String payOutDate, String fileName) {
		logger.info("Inside deleteFileStatus ");
		try {
			payOutDate = payOutDate.split(" ")[0];
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.NET_SETTLED_FILE_COLLECTION.getValue()));

			BasicDBObject query = new BasicDBObject().append(FieldType.PAYOUT_DATE.getName(), payOutDate).append("FILENAME",
					fileName).append(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());

			coll.deleteOne(query);

		} catch (Exception ex) {
			logger.error("Exception in update delete status in deleteFileStatus for CIB", ex);
		}
	}
}