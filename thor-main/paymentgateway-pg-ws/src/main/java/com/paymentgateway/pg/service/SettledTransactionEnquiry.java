package com.paymentgateway.pg.service;

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
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class SettledTransactionEnquiry {
	
	private static Logger logger = LoggerFactory.getLogger(SettledTransactionEnquiry.class.getName());
	private static final String prefix = "MONGO_DB_";
	
	@Autowired
	private MongoInstance mongoInstance;
					
	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
    private DataEncDecTool dataEncDecTool;
	
	@SuppressWarnings("static-access")
	public Map<String, String> verifySettledTransaction(Fields fields) {
	
		logger.info("inside SettledTransactionEnquiry, verifySettledTransaction() ");
		Map<String, String >transactionMap = new HashMap<String, String>();
		try {
			
			Fields newFields = new Fields();
			newFields.put(fields);
			newFields.remove(FieldType.HASH.getName());
			
			String hashCalculate = Hasher.getHash(newFields);
			
			if(validateHash(fields, hashCalculate)) {
				
				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns
						.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				
				
				if (StringUtils.isBlank(fields.get(FieldType.TXNTYPE.getName()))) {
					transactionMap = paramMissingResponse(FieldType.TXNTYPE.getName(), newFields);
					return transactionMap;
				}
				
				
				if (StringUtils.isNotBlank(fields.get(FieldType.TXNTYPE.getName())) && fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("ENQUIRY")) {
					
					boolean isAllSettled = false;
					
					if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
						transactionMap = paramMissingResponse(FieldType.PAY_ID.getName(), newFields);
						return transactionMap;
					}
					
					if (StringUtils.isBlank(fields.get(FieldType.DATE_FROM.getName()))) {
						transactionMap = paramMissingResponse(FieldType.DATE_FROM.getName(), newFields);
						return transactionMap;
					}
					
					if (StringUtils.isBlank(fields.get(FieldType.DATE_TO.getName()))) {
						transactionMap = paramMissingResponse(FieldType.DATE_TO.getName(), newFields);
						return transactionMap;
					}
					
					if (StringUtils.isBlank(fields.get(FieldType.CURRENCY_CODE.getName()))) {
						transactionMap = paramMissingResponse(FieldType.CURRENCY_CODE.getName(), newFields);
						return transactionMap;
					}

					String fromDate = fields.get(FieldType.DATE_FROM.getName()) + " 00:00:00";
					String toDate = fields.get(FieldType.DATE_TO.getName()) + " 23:59:59";
					
					BasicDBObject dateQuery = new BasicDBObject();
					
					dateQuery.put(FieldType.CREATE_DATE.getName(),
							BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
									.add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());
					
					BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),fields.get(FieldType.PAY_ID.getName()));
					BasicDBObject currencyQuery = new BasicDBObject(FieldType.CURRENCY_CODE.getName(),fields.get(FieldType.CURRENCY_CODE.getName()));
					
					BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(),TransactionType.SALE.getName());
					BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(),StatusType.CAPTURED.getName());
					List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
					
					conditionList.add(statusQuery);
					conditionList.add(payIdQuery);
					conditionList.add(txnTypeQuery);
					conditionList.add(dateQuery);
					conditionList.add(currencyQuery);
					
					BasicDBObject finalQuery = new BasicDBObject("$and",conditionList);
					
					Long countTxn = coll.countDocuments(finalQuery);
					
					if (countTxn < 1) {
					
						Date dNow = new Date();
						String dateNow = DateCreater.formatDateForDb(dNow);
						newFields.put(FieldType.RESPONSE_DATE_TIME.getName(), dateNow);
						newFields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.TRANSACTION_NOT_FOUND.getCode());
						newFields.put(FieldType.RESPONSE_MESSAGE.getName(), "No Captured Data for this date range.");
						newFields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
						
						newFields.put(FieldType.HASH.getName(),Hasher.getHash(newFields));
						
						transactionMap.put(FieldType.RESPONSE_DATE_TIME.getName(), newFields.get(FieldType.RESPONSE_DATE_TIME.getName()));
						transactionMap.put(FieldType.RESPONSE_CODE.getName(), newFields.get(FieldType.RESPONSE_CODE.getName()));
						transactionMap.put(FieldType.TXNTYPE.getName(), newFields.get(FieldType.TXNTYPE.getName()));
						transactionMap.put(FieldType.STATUS.getName(), newFields.get(FieldType.STATUS.getName()));
						transactionMap.put(FieldType.CURRENCY_CODE.getName(), newFields.get(FieldType.CURRENCY_CODE.getName()));
						transactionMap.put(FieldType.HASH.getName(), newFields.get(FieldType.HASH.getName()));
						transactionMap.put(FieldType.DATE_FROM.getName(), newFields.get(FieldType.DATE_FROM.getName()));
						transactionMap.put(FieldType.DATE_TO.getName(), newFields.get(FieldType.DATE_TO.getName()));
						transactionMap.put(FieldType.RESPONSE_MESSAGE.getName(), newFields.get(FieldType.RESPONSE_MESSAGE.getName()));
						transactionMap.put(FieldType.PAY_ID.getName(), newFields.get(FieldType.PAY_ID.getName()));
						return transactionMap;
					}
					
					FindIterable<Document> itr = coll.find(finalQuery);
					MongoCursor<Document> cursor = itr.iterator();
					
					while (cursor.hasNext()){
						
						Document dbobj = cursor.next();
						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
		                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
		                    dbobj = dataEncDecTool.decryptDocument(dbobj);
		                }
						BasicDBObject pgRefQueryQuery = new BasicDBObject(FieldType.PG_REF_NUM.getName(),dbobj.get(FieldType.PG_REF_NUM.getName()));
						BasicDBObject oidQueryQuery = new BasicDBObject(FieldType.OID.getName(),dbobj.get(FieldType.OID.getName()));
						BasicDBObject pgRefTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(),TransactionType.RECO.getName());
						BasicDBObject pgRefStatusQuery = new BasicDBObject(FieldType.STATUS.getName(),StatusType.SETTLED.getName());
						
						List<BasicDBObject> txnCondList = new ArrayList<BasicDBObject>();
						
						
						txnCondList.add(pgRefQueryQuery);
						txnCondList.add(oidQueryQuery);
						txnCondList.add(pgRefStatusQuery);
						txnCondList.add(pgRefTypeQuery);
						
						BasicDBObject txnFinalQuery = new BasicDBObject("$and",txnCondList);
						
						Long count = coll.countDocuments(txnFinalQuery);
						if (count > 0) {
							isAllSettled = true;
						}
						else {
							isAllSettled = false;
						}
						
						if (!isAllSettled) {
							
							Date dNow = new Date();
							String dateNow = DateCreater.formatDateForDb(dNow);
							newFields.put(FieldType.RESPONSE_DATE_TIME.getName(), dateNow);
							
							
							newFields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.TRANSACTION_NOT_FOUND.getCode());
							newFields.put(FieldType.RESPONSE_MESSAGE.getName(), "Not Yet Settled");
							newFields.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
							
							newFields.put(FieldType.HASH.getName(),Hasher.getHash(newFields));
							
							transactionMap.put(FieldType.RESPONSE_DATE_TIME.getName(), newFields.get(FieldType.RESPONSE_DATE_TIME.getName()));
							transactionMap.put(FieldType.RESPONSE_CODE.getName(), newFields.get(FieldType.RESPONSE_CODE.getName()));
							transactionMap.put(FieldType.TXNTYPE.getName(), newFields.get(FieldType.TXNTYPE.getName()));
							transactionMap.put(FieldType.STATUS.getName(), newFields.get(FieldType.STATUS.getName()));
							transactionMap.put(FieldType.CURRENCY_CODE.getName(), newFields.get(FieldType.CURRENCY_CODE.getName()));
							transactionMap.put(FieldType.HASH.getName(), newFields.get(FieldType.HASH.getName()));
							transactionMap.put(FieldType.DATE_FROM.getName(), newFields.get(FieldType.DATE_FROM.getName()));
							transactionMap.put(FieldType.DATE_TO.getName(), newFields.get(FieldType.DATE_TO.getName()));
							transactionMap.put(FieldType.RESPONSE_MESSAGE.getName(), newFields.get(FieldType.RESPONSE_MESSAGE.getName()));
							transactionMap.put(FieldType.PAY_ID.getName(), newFields.get(FieldType.PAY_ID.getName()));
							return transactionMap;
							
						}
						
						
					}
					
					if (isAllSettled) {
						
						Date dNow = new Date();
						String dateNow = DateCreater.formatDateForDb(dNow);
						newFields.put(FieldType.RESPONSE_DATE_TIME.getName(), dateNow);
						newFields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
						newFields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getInternalMessage());
						newFields.put(FieldType.STATUS.getName(), StatusType.SETTLED.getName());
						
						newFields.put(FieldType.HASH.getName(),Hasher.getHash(newFields));
						
						transactionMap.put(FieldType.RESPONSE_DATE_TIME.getName(), newFields.get(FieldType.RESPONSE_DATE_TIME.getName()));
						transactionMap.put(FieldType.RESPONSE_CODE.getName(), newFields.get(FieldType.RESPONSE_CODE.getName()));
						transactionMap.put(FieldType.TXNTYPE.getName(), newFields.get(FieldType.TXNTYPE.getName()));
						transactionMap.put(FieldType.STATUS.getName(), newFields.get(FieldType.STATUS.getName()));
						transactionMap.put(FieldType.CURRENCY_CODE.getName(), newFields.get(FieldType.CURRENCY_CODE.getName()));
						transactionMap.put(FieldType.HASH.getName(), newFields.get(FieldType.HASH.getName()));
						transactionMap.put(FieldType.DATE_FROM.getName(), newFields.get(FieldType.DATE_FROM.getName()));
						transactionMap.put(FieldType.DATE_TO.getName(), newFields.get(FieldType.DATE_TO.getName()));
						transactionMap.put(FieldType.RESPONSE_MESSAGE.getName(), newFields.get(FieldType.RESPONSE_MESSAGE.getName()));
						transactionMap.put(FieldType.PAY_ID.getName(), newFields.get(FieldType.PAY_ID.getName()));
						return transactionMap;
						
					}
					
					
				} else {
					
					
					if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
						transactionMap = paramMissingResponse(FieldType.PAY_ID.getName(), newFields);
						return transactionMap;
					}
					
					if (StringUtils.isBlank(fields.get(FieldType.AMOUNT.getName()))) {
						transactionMap = paramMissingResponse(FieldType.AMOUNT.getName(), newFields);
						return transactionMap;
					}
					
					if (StringUtils.isBlank(fields.get(FieldType.ORDER_ID.getName()))) {
						transactionMap = paramMissingResponse(FieldType.ORDER_ID.getName(), newFields);
						return transactionMap;
					}
					
					if (StringUtils.isBlank(fields.get(FieldType.CURRENCY_CODE.getName()))) {
						transactionMap = paramMissingResponse(FieldType.CURRENCY_CODE.getName(), newFields);
						return transactionMap;
					}

					String decimalAmount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()), fields.get(FieldType.CURRENCY_CODE.getName()));
					BasicDBObject amountQuery = new BasicDBObject(FieldType.AMOUNT.getName(),decimalAmount);
					BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(),fields.get(FieldType.PAY_ID.getName()));
					BasicDBObject currencyCodeQuery = new BasicDBObject(FieldType.CURRENCY_CODE.getName(),fields.get(FieldType.CURRENCY_CODE.getName()));
					BasicDBObject orderIdQuery = new BasicDBObject(FieldType.ORDER_ID.getName(),fields.get(FieldType.ORDER_ID.getName()));
					
					BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(),TransactionType.RECO.getName());
					BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(),StatusType.SETTLED.getName());
					List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
					
					conditionList.add(statusQuery);
					conditionList.add(amountQuery);
					conditionList.add(payIdQuery);
					conditionList.add(currencyCodeQuery);
					conditionList.add(txnTypeQuery);
					conditionList.add(orderIdQuery);
					
					BasicDBObject finalQuery = new BasicDBObject("$and",conditionList);
					
					long count = coll.count(finalQuery);
					if (count < 1) {
						
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.TRANSACTION_NOT_FOUND.getCode());
						fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Settled Record Not Found");
						String hash = Hasher.getHash(fields);
						
						fields.put(FieldType.HASH.getName(),hash);
						transactionMap.putAll(fields.getFields());
						
					}
					
					FindIterable<Document> itr = coll.find(finalQuery);
					MongoCursor<Document> cursor = itr.iterator();
					
					while (cursor.hasNext()){
						
						Document dbobj = cursor.next();
						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
		                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
		                    dbobj = dataEncDecTool.decryptDocument(dbobj);
		                }
						
						Fields newField = new Fields();
						
						newField.put(FieldType.ORDER_ID.getName(), dbobj.getString(FieldType.ORDER_ID.getName()));
						newField.put(FieldType.PG_REF_NUM.getName(), dbobj.getString(FieldType.PG_REF_NUM.getName()));
						newField.put(FieldType.PAY_ID.getName(), dbobj.getString(FieldType.PAY_ID.getName()));
						newField.put(FieldType.CREATE_DATE.getName(), dbobj.getString(FieldType.CREATE_DATE.getName()));
						newField.put(FieldType.PAYMENT_TYPE.getName(), dbobj.getString(FieldType.PAYMENT_TYPE.getName()));
						newField.put(FieldType.MOP_TYPE.getName(), dbobj.getString(FieldType.MOP_TYPE.getName()));
						newField.put(FieldType.PAYMENTS_REGION.getName(), dbobj.getString(FieldType.PAYMENTS_REGION.getName()));
						newField.put(FieldType.STATUS.getName(), dbobj.getString(FieldType.STATUS.getName()));
						newField.put(FieldType.TXNTYPE.getName(), dbobj.getString(FieldType.TXNTYPE.getName()));
						newField.put(FieldType.AMOUNT.getName(), dbobj.getString(FieldType.AMOUNT.getName()));
						newField.put(FieldType.TOTAL_AMOUNT.getName(), dbobj.getString(FieldType.TOTAL_AMOUNT.getName()));
						newField.put(FieldType.CURRENCY_CODE.getName(), dbobj.getString(FieldType.CURRENCY_CODE.getName()));
						
						if (dbobj.get(FieldType.POST_SETTLED_FLAG.getName()) != null) {
							newField.put(FieldType.POST_SETTLED_FLAG.getName(), dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()));
						}
						else {
							newField.put(FieldType.POST_SETTLED_FLAG.getName(), "N");
						}
						

						newField.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
						newField.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getInternalMessage());
						
						Date dNow = new Date();
						String dateNow = DateCreater.formatDateForDb(dNow);
						newField.put(FieldType.RESPONSE_DATE_TIME.getName(), dateNow);
						
						String hash = Hasher.getHash(newFields);
						newField.put(FieldType.HASH.getName(),hash);
						transactionMap.putAll(newField.getFields());
						return transactionMap;
					}
				}
					
				} else {
					
					transactionMap.putAll(fields.getFields());
					transactionMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_INPUT.getCode());
					transactionMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Hash");
					return transactionMap;
				}

				
		} catch(Exception ex) {
			logger.error("Exception caught in getSaleCaptureTransaction : " , ex);
		}
		return transactionMap;
	}

	public boolean validateHash(Fields fields, String calculatedHash) {

		try {
			logger.info("inside CustomTransactionEnquiry, validateHash()");
			String merchantHash = fields.get(FieldType.HASH.getName());

			if (StringUtils.isNotBlank(merchantHash)) {
				fields.remove(FieldType.HASH.getName());
				//String calculatedHash = Hasher.getHash(fields);

				logger.info("Merchant Hash == " + merchantHash);
				logger.info("Calculated Hash == " + calculatedHash);
				if (calculatedHash.equalsIgnoreCase(merchantHash)) {
					return true;
				} else {
					logger.info("Merchant Hash and Calculated Hash do not match");
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Unable to validate Hash : " , e);
			return false;
		}
	}
	
	public Map<String, String> paramMissingResponse(String missingField,Fields fields) {
		
		Map<String, String> transactionMap = new HashMap<String, String>();
		try {	
		fields.put(FieldType.RESPONSE_CODE.getName(),ErrorType.INVALID_INPUT.getCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(),"Parameter Missing " + missingField);
		fields.put(FieldType.STATUS.getName(),StatusType.INVALID.getName());
		
		String hash = null;
		
			hash = Hasher.getHash(fields);
		
		fields.put(FieldType.HASH.getName(),hash);
		transactionMap.putAll(fields.getFields());
		return transactionMap;
		} catch (Exception e) {
			logger.error("Exception ",e);
			return transactionMap;
		}
	}
	
}
