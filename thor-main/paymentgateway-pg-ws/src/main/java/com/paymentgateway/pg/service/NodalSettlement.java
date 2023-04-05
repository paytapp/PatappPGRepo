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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
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
public class NodalSettlement {
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
    private DataEncDecTool dataEncDecTool;
	
	private static Logger logger = LoggerFactory.getLogger(NodalSettlement.class.getName());
	private static final String prefix = "MONGO_DB_";
	
	@SuppressWarnings("static-access")
	public Map<String, Map<String, String>> getnodalSettlementTransaction(Fields fields) {
	
		logger.info("inside NodalSettlement, getnodalSettlementTransaction() ");
		Map<String, Map<String, String>> transactionMap = new HashMap<String, Map<String, String>>();
		try {
				
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

					createDateQuery.put(FieldType.CREATE_DATE.getName(),
							BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startDate).toLocalizedPattern())
									.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
				}

				String payId = fields.get(FieldType.PAY_ID.getName());
				User user = userDao.findPayId(payId);
				
				if(StringUtils.isNotBlank(user.getSuperMerchantId())) {
					queryList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
					queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(),user.getSuperMerchantId()));
				}else {
						
					queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));				   
				}
				queryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
				
				allDateQueryList.add(createDateQuery);
				
				BasicDBObject allDateConditionQueryObj = new BasicDBObject("$and", allDateQueryList);
				
				allParamQuery = new BasicDBObject("$and", queryList);
				
				finalQueryList.add(allParamQuery);
				finalQueryList.add(allDateConditionQueryObj);
				
				BasicDBObject finalquery = new BasicDBObject("$and", finalQueryList);
				
				logger.info("Inside NodalSettlement, getnodalSettlementTransaction, finalquery = " + finalquery);
				
				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns
						.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.NODAL_SETTLEMENT_COLLECTION_NAME.getValue()));
				
				BasicDBObject match = new BasicDBObject("$match", finalquery);
				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
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
					
					tranMap.put(FieldType.TXN_ID.getName(), dbobj.getString(FieldType.TXN_ID.getName()));
					tranMap.put(FieldType.PAYEE_NAME.getName(), dbobj.getString(FieldType.PAYEE_NAME.getName()));
					tranMap.put(FieldType.TXNTYPE.getName(), dbobj.getString(FieldType.TXNTYPE.getName()));
					tranMap.put("DATE", dbobj.getString(FieldType.CREATE_DATE.getName()));
					tranMap.put(FieldType.CURRENCY_CODE.getName(), dbobj.getString(FieldType.CURRENCY_CODE.getName()));
					tranMap.put(FieldType.IFSC.getName(), dbobj.getString(FieldType.IFSC.getName()));
					tranMap.put(FieldType.STATUS.getName(), dbobj.getString(FieldType.STATUS.getName()));
					tranMap.put(FieldType.PAY_ID.getName(), dbobj.getString(FieldType.PAY_ID.getName()));
					tranMap.put(FieldType.PG_REF_NUM.getName(), dbobj.getString(FieldType.PG_REF_NUM.getName()));
					tranMap.put(FieldType.AMOUNT.getName(), dbobj.getString(FieldType.AMOUNT.getName()));
					tranMap.put(FieldType.BENE_ACCOUNT_NO.getName(), dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()));
					tranMap.put(FieldType.TRANSACTION_OF.getName(), dbobj.getString(FieldType.TRANSACTION_OF.getName()));				
					tranMap.put(FieldType.REQID.getName(), dbobj.getString(FieldType.REQID.getName()));
					tranMap.put(FieldType.UTR_NO.getName(), dbobj.getString(FieldType.UTR_NO.getName()));
					
					String payIdd = dbobj.getString(FieldType.PAY_ID.getName());
					User user1 = userDao.findPayId(payIdd);
					
					if(StringUtils.isNotBlank(user1.getSuperMerchantId())) {
						tranMap.put(FieldType.SUB_MERCHANT_ID.getName(), dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
						tranMap.put(FieldType.PAY_ID.getName(), dbobj.getString(FieldType.PAY_ID.getName()));
					}else {
							
						tranMap.put(FieldType.PAY_ID.getName(), dbobj.getString(FieldType.PAY_ID.getName()));				   
					}
					
					transactionMap.put(dbobj.getString(FieldType.TXN_ID.getName()), tranMap);
				}
				
				if(transactionMap.size() == 0) {
					
					Map<String, String> blankMap = new HashMap<String, String>();
					blankMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_TRANSACTION_AVAILABLE.getCode());
					blankMap.put(FieldType.RESPONSE_MESSAGE.getName(), "No Transaction Found");
					transactionMap.put(ErrorType.NO_TRANSACTION_AVAILABLE.getResponseMessage(), blankMap);
					return transactionMap;
					
				}
		} catch(Exception ex) {
			logger.error("Exception caught in getnodalSettlementTransaction : " , ex);
		}
		return transactionMap;
	}
	
	public boolean isMendatoryFieldEmpty(Fields fields) throws SystemException {
		
		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
			return true;
		}

		if (StringUtils.isBlank(fields.get(FieldType.DATEFROM.getName()))) {
				return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.DATETO.getName()))) {
	     		return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.HASH.getName()))) {
				return true;
		}
				return false;
	}
	
	public boolean validateHashForApi(Fields fields) throws SystemException {
		
		try {
		String fieldHash = fields.get(FieldType.HASH.getName());
		
		if (StringUtils.isNotBlank(fieldHash)) {
		fields.remove(FieldType.HASH.getName());
		
		logger.info("Hash from Merchant :" + fieldHash);
		String hash = Hasher.getHash(fields);
		logger.info("Hash :" + hash);
		if (!hash.equalsIgnoreCase(fieldHash)) {
			return false;
		}else {
		return true;
		}
	  }else {
		return false;
	 }
	}catch (Exception e) {
			logger.error("Unable to validate Hash : ", e);
			return false;
	}
  }
}
