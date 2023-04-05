package com.paymentgateway.pg.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.SystemProperties;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Shaiwal, Rahul
 *
 */

@Service
public class RefundValidationService {

	private static Logger logger = LoggerFactory.getLogger(RefundValidationService.class.getName());
	private static final String prefix = "MONGO_DB_";
	private static final Collection<String> aLLDB_Fields = SystemProperties.getDBFields();

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private UserDao userDao;

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
    private DataEncDecTool dataEncDecTool;
	
	@Autowired
	private UserSettingDao userSettingDao;

	
	public void checkRefund(Fields fields) {

 		logger.info("Inside RefundValidationService , checkRefund");

		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		UserSettingData userSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
		
		Fields previousFields = new Fields();

		BigDecimal totalRefundAmount = BigDecimal.ZERO;
		BigDecimal totalSaleAmount = BigDecimal.ZERO;
		BigDecimal chrbckAmount = BigDecimal.ZERO;
		String txnAmountInDecimal = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()), fields.get(FieldType.CURRENCY_CODE.getName()));
		BigDecimal currentRefundAmount = new BigDecimal(txnAmountInDecimal);
		
		if(StringUtils.isNotBlank(fields.get(FieldType.CHARGEBACK_AMOUNT.getName()))) {
			String chargebackAmount = Amount.toDecimal(fields.get(FieldType.CHARGEBACK_AMOUNT.getName()), fields.get(FieldType.CURRENCY_CODE.getName()));
			chrbckAmount = new BigDecimal(chargebackAmount);
		}

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			List<BasicDBObject> saleTxnQuery1 = new ArrayList<BasicDBObject>();
			
			if(StringUtils.isNotBlank(fields.get(FieldType.REFUND_TXN_TYPE.getName())) 
					&& fields.get(FieldType.REFUND_TXN_TYPE.getName()).equals("file")) {
				saleTxnQuery1.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));
				saleTxnQuery1.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
				saleTxnQuery1.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			} else {
				saleTxnQuery1.add(
						new BasicDBObject(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName())));

				if (!userSettings.isSkipOrderIdForRefund()) {
					saleTxnQuery1
					.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));
				}
			}
			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleTxnQuery1);
			MongoCursor<Document> cursorSale = coll.find(saleConditionQuery).iterator();
			logger.info("and Query in checkRefund " + saleConditionQuery);
			
			Document dbobjSale = cursorSale.next();
			String oid = dbobjSale.getString(FieldType.OID.toString());
			logger.info("OID " + oid);
			cursorSale.close();
			String pgRefNum = null;
			
			if (!StringUtils.isBlank(oid)) {

				List<BasicDBObject> saleTxnQuery2 = new ArrayList<BasicDBObject>();
				saleTxnQuery2.add(new BasicDBObject(FieldType.OID.getName(), oid));

				BasicDBObject allRecordsQuery = new BasicDBObject("$and", saleTxnQuery2);
				MongoCursor<Document> cursor = coll.find(allRecordsQuery).iterator();

				while (cursor.hasNext()) {

					Document dbObj = cursor.next();
					
					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
	                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						dbObj = dataEncDecTool.decryptDocument(dbObj);
	                }
					if(StringUtils.isNotBlank(fields.get(FieldType.TXNTYPE.getName())) 
							&& fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUND.getName()) 
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						fields.put(FieldType.SUB_MERCHANT_ID.getName(),dbObj.getString(FieldType.SUB_MERCHANT_ID.getName()));
						
					}
					if (dbObj.getString(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())
							&& dbObj.getString(FieldType.STATUS.getName())
									.equalsIgnoreCase(StatusType.CAPTURED.getName())) {

						previousFields = createAllForRefund(dbObj);
						fields.setPrevious(previousFields);
						totalSaleAmount = totalSaleAmount
								.add(new BigDecimal(dbObj.getString(FieldType.TOTAL_AMOUNT.getName())));
						pgRefNum = dbObj.getString(FieldType.PG_REF_NUM.getName());
					}

					else if (dbObj.getString(FieldType.TXNTYPE.getName())
							.equalsIgnoreCase(TransactionType.REFUND.getName())
							&& (dbObj.getString(FieldType.STATUS.getName())
									.equalsIgnoreCase(StatusType.CAPTURED.getName())
							|| dbObj.getString(FieldType.STATUS.getName())
									.equalsIgnoreCase(StatusType.INITIATED.getName()))) {

						totalRefundAmount = totalRefundAmount
								.add(new BigDecimal(dbObj.getString(FieldType.TOTAL_AMOUNT.getName())));

					}

				}

				totalRefundAmount = totalRefundAmount.add(currentRefundAmount.add(chrbckAmount));
				cursorSale.close();

				if(StringUtils.isNotBlank(fields.get(FieldType.REFUND_TXN_TYPE.getName()))) {
					fields.remove(fields.get(FieldType.REFUND_TXN_TYPE.getName()));
					fields.put(FieldType.ORIG_TXN_ID.getName(), pgRefNum);
				}
					
				if (totalSaleAmount.compareTo(totalRefundAmount) >= 0) {

					String dailyRefundResponse = checkDailyrefundLimit(fields, user);

					if (dailyRefundResponse.equalsIgnoreCase(ErrorType.SUCCESS.getCode())) {
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
					} else {
						logger.info("Refund Denied totalRefundAmount = " +totalRefundAmount +"  totalSaleAmount =  "+totalSaleAmount);
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REFUND_DENIED.getCode());
					}

				} else {
					logger.info("Refund Rejected totalRefundAmount = " +totalRefundAmount +"  totalSaleAmount =  "+totalSaleAmount);
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REFUND_REJECTED.getCode());
				}
			}

			else {
				logger.info("Refund declined , TRANSACTION_NOT_FOUND ");
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.TRANSACTION_NOT_FOUND.getCode());
			}
		}

		catch (Exception e) {

			logger.error("Refund declined , TRANSACTION_NOT_FOUND : ", e);
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.TRANSACTION_NOT_FOUND.getCode());
		}

	}

	private String checkDailyrefundLimit(Fields fields, User user) {
		BigDecimal saleTotalAmount = BigDecimal.ZERO;
		BigDecimal refundTotalAmount = BigDecimal.ZERO;
		BigDecimal totalRefundLimitUsed = BigDecimal.ZERO;
		BigDecimal zeroDecimal = BigDecimal.ZERO;
		BigDecimal refundLimitRemains = BigDecimal.ZERO;
		BigDecimal txnAmount = new BigDecimal(Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()), fields.get(FieldType.CURRENCY_CODE.getName())));
		
		saleTotalAmount = saleTxnTotalAmount(fields);
		Map<String,BigDecimal> refundMap = refundTxnTotalAmount(fields);
		refundTotalAmount = refundMap.get("refundTotalAmount");
		totalRefundLimitUsed = refundMap.get("totalRefundLimitUsed");
		if(StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))){
			refundLimitRemains = getSubMerchantReaminsLimit(fields.get(FieldType.SUB_MERCHANT_ID.getName()));
		} else {
			refundLimitRemains = new BigDecimal(user.getRefundLimitRemains());
		}
//		BigDecimal oneTimeRefundLimit = new BigDecimal(user.getOneTimeRefundLimit());
		logger.info("checkDailyrefundLimit total sale amount >>> " + saleTotalAmount.toString());
		logger.info("checkDailyrefundLimit total refund amount >>> " + refundTotalAmount.toString());
		logger.info("checkDailyrefundLimit refundLimitRemains >>> " + refundLimitRemains.toString());
		logger.info("checkDailyrefundLimit user RefundLimitRemains >>> " + user.getRefundLimitRemains());
		logger.info("checkDailyrefundLimit user ExtraRefundLimit >>> " + user.getRefundLimitRemains());
		if(user.getExtraRefundLimit() > 0) {
			BigDecimal extraRefundLimit = new BigDecimal(user.getExtraRefundLimit());
			saleTotalAmount = saleTotalAmount.add(extraRefundLimit);
			
		}else if(user.getRefundLimitRemains() > 0) {
			
//			BigDecimal refundLimitRemains = new BigDecimal(user.getRefundLimitRemains());
			int compareResult = getOneTimeRefundLimitAllowed(refundLimitRemains, saleTotalAmount, txnAmount, refundTotalAmount, totalRefundLimitUsed, fields);
			if ((compareResult >= 0)) {
				return ErrorType.SUCCESS.getCode();
			} else {
				return ErrorType.REFUND_DENIED.getCode();
			}
		}
		
		if(totalRefundLimitUsed.compareTo(zeroDecimal) > 0) {
			refundTotalAmount = refundTotalAmount.subtract(totalRefundLimitUsed); 
		}
		
		refundTotalAmount = refundTotalAmount.add(txnAmount);
		int compareResult = saleTotalAmount.compareTo(refundTotalAmount);
		if ((compareResult >= 0)) {
			return ErrorType.SUCCESS.getCode();
		} else {
			return ErrorType.REFUND_DENIED.getCode();
		}

	}
	
	private BigDecimal saleTxnTotalAmount(Fields fields) {
		
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		
		BigDecimal saleTotalAmount = BigDecimal.ZERO;
		Date dNow = new Date();
		String dateNow = DateCreater.formatDateForDb(dNow);
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
		
		BasicDBObject txnTypeQuery = new BasicDBObject();

		txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		txnTypeConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
		txnTypeConditionLst.add(new BasicDBObject(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", "")));
		txnTypeConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
		txnTypeQuery.append("$and", txnTypeConditionLst);
		dbObjList.add(txnTypeQuery);
		
		BasicDBObject paymentTypeQuery = new BasicDBObject();
		paymentTypeQuery.append(FieldType.PAYMENT_TYPE.getName(),
				new BasicDBObject("$ne", PaymentType.COD.getCode()));
		
		
		dbObjList.add(paymentTypeQuery);
		BasicDBObject finalquery = new BasicDBObject("$and", dbObjList);
		
		BasicDBObject projectElement = new BasicDBObject();
		projectElement.put(FieldType.AMOUNT.getName(), 1);
		projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
		BasicDBObject project = new BasicDBObject("$project", projectElement);
		
		BasicDBObject match = new BasicDBObject("$match", finalquery);
		List<BasicDBObject> pipeline = Arrays.asList(match, project);
		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document>  cursor = output.iterator();
		

		while (cursor.hasNext()) {
			Document documentObj = cursor.next();
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                    && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
            }
			
				BigDecimal txnReport = new BigDecimal(documentObj.getString(FieldType.AMOUNT.getName()));
				saleTotalAmount = saleTotalAmount.add(txnReport);
				
		}
		cursor.close();
		return saleTotalAmount;
		
	}
	
	@SuppressWarnings("unused")
	private Map<String,BigDecimal> refundTxnTotalAmount(Fields fields) {
		Map<String,BigDecimal> refundMap = new HashMap<String,BigDecimal>();
		BigDecimal refundTotalAmount = BigDecimal.ZERO;
		BigDecimal totalRefundLimitUsed = BigDecimal.ZERO;
		Date dNow = new Date();
		String dateNow = DateCreater.formatDateForDb(dNow);
		// PropertiesManager propManager = new PropertiesManager();
		List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
		List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
		
		BasicDBObject txnTypeQuery = new BasicDBObject();
		
		List<String> refundStatusList= new ArrayList<>();
		refundStatusList.add(StatusType.CAPTURED.getName());
		refundStatusList.add(StatusType.INITIATED.getName());

		txnTypeConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
		txnTypeConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), new BasicDBObject("$in",refundStatusList)));
		txnTypeConditionLst.add(new BasicDBObject(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", "")));
		txnTypeConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));

		txnTypeQuery.append("$and", txnTypeConditionLst);
		dbObjList.add(txnTypeQuery);
		BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		MongoCursor<Document> cursor = coll.find(andQuery).iterator();
		while (cursor.hasNext()) {
			Document documentObj = cursor.next();
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                    && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				documentObj = dataEncDecTool.decryptDocument(documentObj);
            }
			
				BigDecimal txnReport = new BigDecimal(documentObj.getString(FieldType.TOTAL_AMOUNT.getName()));
				refundTotalAmount = refundTotalAmount.add(txnReport);
				
				BigDecimal refundLimitUsed = new BigDecimal(documentObj.getString(FieldType.REFUND_LIMIT_USED.getName()));
				
				if(refundLimitUsed == null) {
					totalRefundLimitUsed = totalRefundLimitUsed.add(BigDecimal.ZERO );
				}else {
					totalRefundLimitUsed = totalRefundLimitUsed.add(refundLimitUsed);
				}
				
				
				
		}
		cursor.close();
		refundMap.put("refundTotalAmount", refundTotalAmount);
		refundMap.put("totalRefundLimitUsed", totalRefundLimitUsed);
		return refundMap;
		
	}
	

	private Fields createAllForRefund(Document documentObj) {

		Fields fields = new Fields();

		try {
			if (null != documentObj) {
				for (int j = 0; j < documentObj.size(); j++) {
					for (String columnName : aLLDB_Fields) {
						if (documentObj.get(columnName) != null) {
							fields.put(columnName, documentObj.get(columnName).toString());
						} else {

						}
					}
				}
			}

			return fields;
		}

		catch (Exception e) {
			logger.error("Exception in getting Sale transaction for previous fields : " , e);
		}
		return fields;
	}
	
	public int getOneTimeRefundLimitAllowed(BigDecimal refundLimitRemains, BigDecimal saleTotalAmount,
			BigDecimal txnAmount, BigDecimal refundTotalAmount, BigDecimal totalRefundLimitUsed, Fields fields) {
		logger.info("Inside getOneTimeRefundLimitAllowed of RefundValidationService class");

		BigDecimal zeroDecimal = BigDecimal.ZERO;
		BigDecimal minusDecimal = new BigDecimal("-1");
		BigDecimal saleRefundResultAmount = refundTotalAmount.subtract(saleTotalAmount);
		refundTotalAmount = refundTotalAmount.subtract(totalRefundLimitUsed);
		
		if (saleRefundResultAmount.compareTo(zeroDecimal) > 0) {
			BigDecimal saleMinusrefund = saleTotalAmount.subtract(refundTotalAmount);
			if(saleMinusrefund.compareTo(zeroDecimal) > 0) {
				if(txnAmount.compareTo(saleMinusrefund) > 0) {
					fields.put("reducedLimitAmount", (refundLimitRemains.subtract(txnAmount.subtract(saleMinusrefund))).toString());
					fields.put("LimitAmountUsed", (txnAmount.subtract(saleMinusrefund)).toString());
				}
//				else {
//					fields.put("reducedLimitAmount", (refundLimitRemains.subtract(saleMinusrefund.subtract(txnAmount))).toString());
//					fields.put("LimitAmountUsed", (saleMinusrefund.subtract(txnAmount)).toString());
//				}
				
			}else {
				fields.put("reducedLimitAmount", (refundLimitRemains.subtract(txnAmount)).toString());
				fields.put("LimitAmountUsed", txnAmount.toString());
			}
		} else {
			saleRefundResultAmount = saleTotalAmount.subtract(txnAmount.add(refundTotalAmount));
			if (saleRefundResultAmount.compareTo(zeroDecimal) < 0) {
				fields.put("reducedLimitAmount", (refundLimitRemains.add(saleRefundResultAmount)).toString());
				fields.put("LimitAmountUsed", (minusDecimal.multiply(saleRefundResultAmount)).toString());
			}

		}

		saleTotalAmount = saleTotalAmount.add(refundLimitRemains);
		refundTotalAmount = refundTotalAmount.add(txnAmount);

		return saleTotalAmount.compareTo(refundTotalAmount);
	}
	public BigDecimal getSubMerchantReaminsLimit(String subMerchantId) {
		BigDecimal refundLimitRemains = BigDecimal.ZERO;
		try {
			User submerchant = userDao.findPayId(subMerchantId);
			refundLimitRemains = new BigDecimal(submerchant.getRefundLimitRemains());
		} catch(Exception ex) {
			logger.error("Exception Caught while fetching subMerchant", ex);
		}
		return refundLimitRemains;
	}
}
