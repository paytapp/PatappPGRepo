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
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
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
public class CustomTransactionEnquiry {
	
	private static Logger logger = LoggerFactory.getLogger(CustomTransactionEnquiry.class.getName());
	private static final String prefix = "MONGO_DB_";
	
	@Autowired
	private MongoInstance mongoInstance;
					
	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
    private DataEncDecTool dataEncDecTool;
	
	@SuppressWarnings("static-access")
	public Map<String, Map<String, String>> getSaleCaptureTransaction(Fields fields) {
	
		logger.info("inside CustomTransactionEnquiry, getSaleCaptureTransaction() ");
		Map<String, Map<String, String>> transactionMap = new HashMap<String, Map<String, String>>();
		try {
			
			String merchantHash = fields.get(FieldType.HASH.getName());
			fields.remove(FieldType.HASH.getName());
			String calculatedHash = Hasher.getHash(fields);
			
			if (!calculatedHash.equals(merchantHash)) {
				StringBuilder hashMessage = new StringBuilder("Merchant hash =");
				hashMessage.append(merchantHash);
				hashMessage.append(", Calculated Hash=");
				hashMessage.append(calculatedHash);
				MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
				logger.error(hashMessage.toString());
				
				Map<String, String> authMap = new HashMap<String, String>();
				authMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getCode());
				authMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Hash");
				transactionMap.put(ErrorType.AUTHENTICATION_FAILED.getResponseMessage(), authMap);
				return transactionMap;
				
			} else {
			
				BasicDBObject createDateQuery = new BasicDBObject();
				List<BasicDBObject> finalQueryList = new ArrayList<BasicDBObject>();
				BasicDBObject allParamQuery = new BasicDBObject();
				List<BasicDBObject> allDateQueryList = new ArrayList<BasicDBObject>();
				List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
				
				String currentDate = null;
				String startDate = null;
				
				if (!fields.get(FieldType.DATEFROM.getName()).isEmpty()) {
					
					startDate = DateCreater.formatDate(fields.get(FieldType.DATEFROM.getName()));
					StringBuilder endDateBuilder = new StringBuilder(startDate);
					endDateBuilder.append("00:00:00");
					startDate = endDateBuilder.toString();
					
					if (!fields.get(FieldType.DATETO.getName()).isEmpty()) {
						currentDate = DateCreater.formatDate(fields.get(FieldType.DATETO.getName()));
						StringBuilder dateBuilder = new StringBuilder(currentDate);
						dateBuilder.append("23:59:59");
						currentDate = dateBuilder.toString();
					} else {
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Calendar cal = Calendar.getInstance();
						currentDate = dateFormat.format(cal.getTime());
					}

					createDateQuery.put(FieldType.UPDATE_DATE.getName(),
							BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
									.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
				}
				
				BasicDBObject dateIndexConditionQuery = new BasicDBObject();
				String startString = new SimpleDateFormat(startDate).toLocalizedPattern();
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
				BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
						&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
					dateIndexConditionQuery.append(FieldType.TXN_DATE.getName(), dateIndexIn);
				}
				
				queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
				queryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
				queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
				
				allDateQueryList.add(createDateQuery);
				allDateQueryList.add(dateIndexConditionQuery);
				
				BasicDBObject allDateConditionQueryObj = new BasicDBObject("$and", allDateQueryList);
				
				allParamQuery = new BasicDBObject("$and", queryList);
				
				finalQueryList.add(allParamQuery);
				finalQueryList.add(allDateConditionQueryObj);
				
				BasicDBObject finalquery = new BasicDBObject("$and", finalQueryList);
				
				logger.info("Inside CustomTransactionEnquiry, getSaleCaptureTransaction, finalquery = " + finalquery);
				
				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns
						.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				
				BasicDBObject match = new BasicDBObject("$match", finalquery);
				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("UPDATE_DATE", -1));
				List<BasicDBObject> pipeline = Arrays.asList(match, sort);
				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				MongoCursor<Document> cursor = output.iterator();
				while (cursor.hasNext()) {
					
					Document dbobj = cursor.next();
					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
	                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
	                    dbobj = dataEncDecTool.decryptDocument(dbobj);
	                }
					
					Map<String, String> tranMap = new HashMap<String, String>();
					
					tranMap.put(FieldType.ORDER_ID.getName(), dbobj.getString(FieldType.ORDER_ID.getName()));
					tranMap.put(FieldType.PG_REF_NUM.getName(), dbobj.getString(FieldType.PG_REF_NUM.getName()));
					tranMap.put("DATE", dbobj.getString(FieldType.CREATE_DATE.getName()));
					tranMap.put(FieldType.PAYMENT_TYPE.getName(), dbobj.getString(FieldType.PAYMENT_TYPE.getName()));
					tranMap.put(FieldType.MOP_TYPE.getName(), dbobj.getString(FieldType.MOP_TYPE.getName()));
					tranMap.put(FieldType.PAYMENTS_REGION.getName(), dbobj.getString(FieldType.PAYMENTS_REGION.getName()));
					tranMap.put(FieldType.CARD_HOLDER_TYPE.getName(), dbobj.getString(FieldType.CARD_HOLDER_TYPE.getName()));
					tranMap.put(FieldType.STATUS.getName(), dbobj.getString(FieldType.STATUS.getName()));
					tranMap.put(FieldType.AMOUNT.getName(), dbobj.getString(FieldType.AMOUNT.getName()));
					tranMap.put(FieldType.TOTAL_AMOUNT.getName(), dbobj.getString(FieldType.TOTAL_AMOUNT.getName()));
					
					if(StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_MASK.getName()))) {
						tranMap.put(FieldType.CARD_MASK.getName(), dbobj.getString(FieldType.CARD_MASK.getName()));
					} else {
						tranMap.put(FieldType.CARD_MASK.getName(), CrmFieldConstants.NA.getValue());
					}
					
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
							&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

						tranMap.put("TDR/SURCHARGE", String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

					} else {
						tranMap.put("TDR/SURCHARGE", "0.00");
					}
					
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
							&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
						tranMap.put("GST", String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

					} else {
						tranMap.put("GST", "0.00");
					}
					
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
						
						tranMap.put("TOTAL_AMOUNT_PAYABLE", String.format("%.2f",
								Double.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString())) 
										- (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));
					} else {
						tranMap.put("TOTAL_AMOUNT_PAYABLE", "0.00");
					}
					
					if(StringUtils.isNotBlank(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()))) {
						tranMap.put(FieldType.POST_SETTLED_FLAG.getName(), dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()));
					} else {
						tranMap.put(FieldType.POST_SETTLED_FLAG.getName(), Constants.N_FLAG.getValue());
					}
					transactionMap.put(dbobj.getString(FieldType.ORDER_ID.getName()), tranMap);
					
				}
				
				if(transactionMap.size() == 0) {
					
					Map<String, String> blankMap = new HashMap<String, String>();
					blankMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_TRANSACTION_AVAILABLE.getCode());
					blankMap.put(FieldType.RESPONSE_MESSAGE.getName(), "No Transaction Found");
					transactionMap.put(ErrorType.NO_TRANSACTION_AVAILABLE.getResponseMessage(), blankMap);
					return transactionMap;
					
				}
				
			}
		} catch(Exception ex) {
			logger.error("Exception caught in getSaleCaptureTransaction : " , ex);
		}
		return transactionMap;
	}
}
