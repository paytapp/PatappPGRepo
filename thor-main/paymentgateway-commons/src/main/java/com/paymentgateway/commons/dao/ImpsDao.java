package com.paymentgateway.commons.dao;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Shiva
 *
 */

@Service
public class ImpsDao {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userDao;

	@Autowired
	private ChargebackDao chargebackDao;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;
	
	@Autowired
	private PropertiesManager propertiesManager;

	private Fields fields;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(ImpsDao.class.getName());

	public List<ImpsDownloadObject> fetchImpsReportData(String dateFrom, String dateTo, String merchantId,
			String subMerchantPayId, String status, String channel, User user) {
		List<ImpsDownloadObject> impsList = new ArrayList<>();
		Map<String, User> userMap = new HashMap<String, User>();
		Set<String> orderId = new HashSet<String>();
		try {
			logger.info("Inside fetchImpsReportData(), ImpsDao");
			boolean isParameterised = false;
			
			DateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

			Date startDate = format.parse(dateFrom + " 00:00:00");
			Date endDate = format.parse(dateTo + " 23:59:59");

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));

			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			dateQuery.put("CREATE_DATE", BasicDBObjectBuilder.start("$gte", sdfFormat.format(startDate))
					.add("$lte", sdfFormat.format(endDate)).get());

			if (StringUtils.isNotBlank(merchantId)) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}

			if (StringUtils.isNotBlank(channel) && !channel.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.USER_TYPE.getName(), channel));
				isParameterised = true;
			}

			if (!status.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}

			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);

			BasicDBObject allParamQuery = new BasicDBObject();
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			FindIterable<Document> iterDoc = coll.find(finalquery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}
				
				if (dbobj.containsKey(FieldType.USER_TYPE.getName())
						&& !dbobj.getString(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Verification"))
					orderId.add(dbobj.getString(FieldType.ORDER_ID.getName()));
			}
			for (String id : orderId) {
				BasicDBObject finalquery2 = new BasicDBObject(FieldType.ORDER_ID.getName(), id);

				BasicDBObject match = new BasicDBObject("$match", finalquery2);
				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.name(), -1));
				BasicDBObject limit = new BasicDBObject("$limit", 1);
				List<BasicDBObject> pipeline2 = Arrays.asList(match, sort, limit);
				AggregateIterable<Document> output2 = coll.aggregate(pipeline2);
				output2.allowDiskUse(true);
				MongoCursor<Document> cursor2 = output2.iterator();

				while (cursor2.hasNext()) {

					ImpsDownloadObject imps = new ImpsDownloadObject();
					Document dbobj = cursor2.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						dbobj = dataEncDecTool.decryptDocument(dbobj);
					} 

					// For Merchant Business Name
					String merchantPayId = dbobj.getString(FieldType.PAY_ID.getName());
					User merchantUser = new User();

					if (userMap.get(merchantPayId) != null && !userMap.get(merchantPayId).getPayId().isEmpty()) {
						merchantUser = userMap.get(merchantUser);
					} else {
						merchantUser = userDao.findPayId(merchantPayId);
						userMap.put(merchantPayId, merchantUser);
					}

					if (merchantUser != null) {
						imps.setMerchant(merchantUser.getBusinessName());
					} else {
						imps.setMerchant(userDao.getBusinessNameByPayId(merchantPayId));
					}
					

					if (!merchantId.equalsIgnoreCase("All") && dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
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
							imps.setSubMerchant(subMerchantUser.getBusinessName());
						} else {
							imps.setSubMerchant(CrmFieldConstants.NA.getValue());
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						imps.setMerchantPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
					} else {
						imps.setMerchantPayId(dbobj.getString(FieldType.PAY_ID.getName()));
					}

					imps.setTxnId(dbobj.getString(FieldType.TXN_ID.getName()));
					imps.setOrderId(dbobj.getString(FieldType.ORDER_ID.getName()));
					imps.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
					imps.setBankAccountName(dbobj.getString(FieldType.BENE_NAME.getName()));
					imps.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()));
					imps.setBankIFSC(dbobj.getString(FieldType.IFSC_CODE.getName()));
					imps.setDate(format.format(sdfFormat.parse(dbobj.getString(FieldType.CREATE_DATE.getName()))));

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))) {
						imps.setImpsRefNum(dbobj.getString(FieldType.RRN.getName()));
					} else {
						imps.setImpsRefNum(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CAPTURED_DATE_FROM.getName()))) {
						imps.setTxnsCapturedFrom(dbobj.getString(FieldType.CAPTURED_DATE_FROM.getName()));
					} else {
						imps.setTxnsCapturedFrom(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CAPTURED_DATE_TO.getName()))) {
						imps.setTxnsCapturedTo(dbobj.getString(FieldType.CAPTURED_DATE_TO.getName()));
					} else {
						imps.setTxnsCapturedTo(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLED_DATE.getName()))) {
						imps.setSystemSettlementDate(dbobj.getString(FieldType.SETTLED_DATE.getName()));
					} else {
						imps.setSystemSettlementDate(CrmFieldConstants.NA.getValue());
					}

					imps.setStatus(dbobj.getString(FieldType.STATUS.getName()));

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()))) {
						imps.setResponseMsg(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
					} else {
						imps.setResponseMsg(CrmFieldConstants.NA.getValue());
					}

					logger.info("response mg " + imps.getResponseMsg());

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PHONE_NO.getName()))) {
						imps.setPhoneNo((dbobj.getString(FieldType.PHONE_NO.getName())));
					} else {
						imps.setPhoneNo(CrmFieldConstants.NA.getValue());
					}

					if (dbobj.containsKey(FieldType.USER_TYPE.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.USER_TYPE.getName()))) {
						imps.setUserType((dbobj.getString(FieldType.USER_TYPE.getName())));
					} else {
						imps.setUserType(CrmFieldConstants.NA.getValue());
					}
					impsList.add(imps);
				}
			}
			logger.info("Total data in impsList is " + impsList.size());
			return impsList;

		} catch (Exception e) {
			logger.error("Exception " , e);

		}
		return impsList;

	}

	public boolean isDailyLimitExceed(ImpsDownloadObject impsData) {
		logger.info("Inside  isDailyLimitExceed()");
		String fieldAmount = impsData.getAmount();

		BigDecimal settledTransactionDiff = getSettleTransactionDiff(impsData);

		if (settledTransactionDiff.compareTo(BigDecimal.ZERO) <= 0) {
			return true;
		}

		BigDecimal totalImpsTransactionAmount = getImpsTransactionAmount(impsData);
		BigDecimal totalChargebackAmount = getChargebackTransactionAmount(impsData);

		BigDecimal checkDiff = new BigDecimal(0);
		checkDiff = checkDiff.add(settledTransactionDiff).setScale(2);
		checkDiff = checkDiff.subtract(totalChargebackAmount).setScale(2);
		checkDiff = checkDiff.subtract(totalImpsTransactionAmount).setScale(2);
		checkDiff = checkDiff.subtract(new BigDecimal(fieldAmount).setScale(2));

		logger.info("Calculated final settled amount is " + checkDiff);

		if (checkDiff.compareTo(BigDecimal.ZERO) >= 0) {
			return false;
		}

		return true;

	}
	
	

	public List<ImpsDownloadObject> fetchBeneVerificationReportData(String dateFrom, String dateTo, String merchantId,
			String subMerchantPayId, String status, String bankAccountNumber, User sessionUser) {
		List<ImpsDownloadObject> impsList = new ArrayList<>();
		Map<String, User> userMap = new HashMap<String, User>();
		Set<String> orderIdd = new HashSet<String>();
		Set<String> payIdd = new HashSet<String>();
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject userTypeQuery = new BasicDBObject();
			BasicDBObject accountNumberQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject resellerPayIdQuery = new BasicDBObject();
			
			logger.info("Inside fetchBeneVerificationReportData(), ImpsDao");
			boolean isParameterised = false;
			
			DateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

			Date startDate = format.parse(dateFrom + " 00:00:00");
			Date endDate = format.parse(dateTo + " 23:59:59");

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));

			BasicDBObject finalQueryDate = new BasicDBObject();

			finalQueryDate.put(FieldType.CREATE_DATE.getName(), BasicDBObjectBuilder
					.start("$gte", sdfFormat.format(startDate)).add("$lte", sdfFormat.format(endDate)).get());

			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));
				isParameterised = true;
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			if (!status.equalsIgnoreCase("ALL")) {
				if (status.equalsIgnoreCase("Captured")) {
					statusQuery.put(FieldType.STATUS.getName(), status);
				} else if (status.equalsIgnoreCase("Timeout")) {
					List<BasicDBObject> pendingStatusList = new ArrayList<BasicDBObject>();
					pendingStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
					pendingStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PROCESSING.getName()));
					
					statusQuery.put("$or", pendingStatusList);
				} else {
					List<BasicDBObject> otherStatusList = new ArrayList<BasicDBObject>();
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DECLINED.getName()));
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.INVALID.getName()));
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName()));
					
					statusQuery.put("$or", otherStatusList);
				}
			}
			
			if(sessionUser.getUserType().equals(UserType.RESELLER) && !sessionUser.isPartnerFlag()){
				if(StringUtils.isNotBlank(merchantId) && merchantId.equalsIgnoreCase("ALL") ){
					List<Merchants> resellerMerchantList = new UserDao().getActiveResellerMerchants(sessionUser.getResellerId());
					if(!resellerMerchantList.isEmpty()){
						List<BasicDBObject> resellerMerchantPayId = new ArrayList<BasicDBObject>();
						for(Merchants merchant: resellerMerchantList){
							resellerMerchantPayId.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchant.getPayId()));
						}
						resellerPayIdQuery.put("$or", resellerMerchantPayId);
					}else{
						resellerPayIdQuery.put(FieldType.PAY_ID.getName(),sessionUser.getPayId() );
					}
				}
			} 
			

			if (StringUtils.isNotBlank(bankAccountNumber)) {
				accountNumberQuery.put(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNumber);
			}

			userTypeQuery.put(FieldType.USER_TYPE.getName(), "Verification");

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			if (accountNumberQuery.isEmpty()) {
				if (!finalQueryDate.isEmpty()) {
					allConditionQueryList.add(finalQueryDate);
				}
			}

			if (!userTypeQuery.isEmpty()) {
				allConditionQueryList.add(userTypeQuery);
			}
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();
			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			
			if (!resellerPayIdQuery.isEmpty()) {
				fianlList.add(resellerPayIdQuery);
			}

			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}
			if (!accountNumberQuery.isEmpty()) {
				fianlList.add(accountNumberQuery);
			}
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			FindIterable<Document> iterDoc = coll.find(finalquery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				} 

				
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
					payIdd.add(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
				} else {
					payIdd.add(dbobj.getString(FieldType.PAY_ID.getName()));
				}
			}
			cursor.close();
			User user1 = new User();
			for (String pyId : payIdd) {
				orderIdd.clear();
				BasicDBObject finalquery2 = new BasicDBObject();
				List<BasicDBObject> paramConditionLst1 = new ArrayList<BasicDBObject>();
				user1 = userDao.findPayId(pyId);

				if (StringUtils.isNotBlank(user1.getSuperMerchantId())) {
					paramConditionLst1.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), pyId));
				} else {

					paramConditionLst1.add(new BasicDBObject(FieldType.PAY_ID.getName(), pyId));
				}
				if (accountNumberQuery.isEmpty()) {
					if (!finalQueryDate.isEmpty()) {
						paramConditionLst1.add(finalQueryDate);
					}
				}

				if (!userTypeQuery.isEmpty()) {
					paramConditionLst1.add(userTypeQuery);
				}
				if (!statusQuery.isEmpty()) {
					paramConditionLst1.add(statusQuery);
				}
				if (!accountNumberQuery.isEmpty()) {
					paramConditionLst1.add(accountNumberQuery);
				}
				if (!paramConditionLst1.isEmpty()) {
					finalquery2 = new BasicDBObject("$and", paramConditionLst1);
				}

				FindIterable<Document> iterDoc1 = coll.find(finalquery2);
				MongoCursor<Document> cursor1 = iterDoc1.iterator();
				
				while (cursor1.hasNext()) {
					Document dbobj = cursor1.next();
					
					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						dbobj = dataEncDecTool.decryptDocument(dbobj);
					} 
					
					
					orderIdd.add(dbobj.getString(FieldType.ORDER_ID.getName()));
				}
				cursor1.close();

				for (String id : orderIdd) {
					List<BasicDBObject> paramConditionLst3 = new ArrayList<BasicDBObject>();
					if (StringUtils.isNotBlank(user1.getSuperMerchantId())) {
						paramConditionLst3.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), pyId));
					} else {
						paramConditionLst3.add(new BasicDBObject(FieldType.PAY_ID.getName(), pyId));
					}
					paramConditionLst3.add(new BasicDBObject(FieldType.ORDER_ID.getName(), id));
					BasicDBObject finalquery3 = new BasicDBObject();
					if (!paramConditionLst3.isEmpty()) {
						finalquery3 = new BasicDBObject("$and", paramConditionLst3);
					}

					BasicDBObject match = new BasicDBObject("$match", finalquery3);
					BasicDBObject sort = new BasicDBObject("$sort",
							new BasicDBObject(FieldType.CREATE_DATE.name(), -1));
					BasicDBObject limit = new BasicDBObject("$limit", 1);
					List<BasicDBObject> pipeline2 = Arrays.asList(match, sort, limit);
					AggregateIterable<Document> output2 = coll.aggregate(pipeline2);
					output2.allowDiskUse(true);
					MongoCursor<Document> cursor3 = output2.iterator();

					while (cursor3.hasNext()) {

						ImpsDownloadObject imps = new ImpsDownloadObject();
						Document dbobj = cursor3.next();
						
						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
							dbobj = dataEncDecTool.decryptDocument(dbobj);
						} 

						if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
							if ((("Invalid").equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName())))
									|| (("Declined").equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName())))
									|| (("Failed").equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName())))
									|| (("Duplicate").equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName())))) {

							} else {
								if (!(status.equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName())))) {
									break;
								}
							}
						}
						
						// For Merchant Business Name
						String merchantPayId = dbobj.getString(FieldType.PAY_ID.getName());
						User merchantUser = new User();

						if (userMap.get(merchantPayId) != null && !userMap.get(merchantPayId).getPayId().isEmpty()) {
							merchantUser = userMap.get(merchantUser);
						} else {
							merchantUser = userDao.findPayId(merchantPayId);
							userMap.put(merchantPayId, merchantUser);
						}

						if (merchantUser != null) {
							imps.setMerchant(merchantUser.getBusinessName());
						} else {
							imps.setMerchant(userDao.getBusinessNameByPayId(merchantPayId));
						}


						if (((!merchantId.equalsIgnoreCase("All")) || (merchantId.equalsIgnoreCase("All"))
								|| (merchantId.equalsIgnoreCase("")))
								&& dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
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
								imps.setSubMerchant(subMerchantUser.getBusinessName());
							} else {
								imps.setSubMerchant(CrmFieldConstants.NA.getValue());
							}
						} else {
							if ((merchantId.equalsIgnoreCase("All")) && (StringUtils.isNotBlank(subMerchantPayId) && subMerchantPayId.equalsIgnoreCase("All"))) {
								imps.setSubMerchant(CrmFieldConstants.NA.getValue());
							}
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
							imps.setMerchantPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
						} else {
							imps.setMerchantPayId(dbobj.getString(FieldType.PAY_ID.getName()));
						}
						imps.setTxnId(dbobj.getString(FieldType.TXN_ID.getName()));
						imps.setOrderId(dbobj.getString(FieldType.ORDER_ID.getName()));
						imps.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
						imps.setBankAccountName(dbobj.getString(FieldType.BENE_NAME.getName()));
						imps.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()));
						imps.setBankIFSC(dbobj.getString(FieldType.IFSC_CODE.getName()));
						imps.setCurrencyCode(
								Currency.getAlphabaticCode(dbobj.getString(FieldType.CURRENCY_CODE.getName())));
						imps.setDate(format.format(sdfFormat.parse(dbobj.getString(FieldType.CREATE_DATE.getName()))));
						imps.setStatus(dbobj.getString(FieldType.STATUS.getName()));
						imps.setResponseMsg(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
						if (StringUtils.isNoneBlank(dbobj.getString(FieldType.PHONE_NO.getName()))) {
							imps.setPhoneNo(dbobj.getString(FieldType.PHONE_NO.getName()));
						} else {
							imps.setPhoneNo("NA");
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_NAME_REQUEST.getName()))) {
							imps.setBankAccountNameReq((dbobj.getString(FieldType.BENE_NAME_REQUEST.getName())));
						} else {
							imps.setBankAccountNameReq(CrmFieldConstants.NA.getValue());
						}
						impsList.add(imps);
					}
				}

			}
			logger.info("Total data in impsList is " + impsList.size());
			return impsList;

		} catch (Exception e) {
			logger.error("Exception " , e);

		}
		return impsList;

	}
	
	public List<ImpsDownloadObject> fetchVpaBeneVerificationReportData(String dateFrom, String dateTo, String merchantId,
			String subMerchantPayId, String status, String vpa, String accountType, User sessionUser) {
		List<ImpsDownloadObject> impsList = new ArrayList<>();
		Map<String, User> userMap = new HashMap<String, User>();
		Set<String> orderIdd = new HashSet<String>();
		Set<String> payIdd = new HashSet<String>();
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject userTypeQuery = new BasicDBObject();
			BasicDBObject accountNumberQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject resellerPayIdQuery = new BasicDBObject();
			
			logger.info("Inside fetchBeneVerificationReportData(), ImpsDao");
			boolean isParameterised = false;
			
			DateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

			Date startDate = format.parse(dateFrom + " 00:00:00");
			Date endDate = format.parse(dateTo + " 23:59:59");

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.BENE_VERIFICATION_COLLECTION.getValue()));

			BasicDBObject finalQueryDate = new BasicDBObject();

			finalQueryDate.put(FieldType.CREATE_DATE.getName(), BasicDBObjectBuilder
					.start("$gte", sdfFormat.format(startDate)).add("$lte", sdfFormat.format(endDate)).get());

			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));
				isParameterised = true;
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				if (status.equalsIgnoreCase("Captured")) {
					statusQuery.put(FieldType.STATUS.getName(), status);
				} else if (status.equalsIgnoreCase("Timeout")) {
					statusQuery.put(FieldType.STATUS.getName(), status);
				} else {
					List<BasicDBObject> otherStatusList = new ArrayList<BasicDBObject>();
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DECLINED.getName()));
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.INVALID.getName()));
					otherStatusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName()));

					statusQuery.put("$or", otherStatusList);
				}
			}
			
			if(sessionUser.getUserType().equals(UserType.RESELLER) && !sessionUser.isPartnerFlag()){
				if(StringUtils.isNotBlank(merchantId) && merchantId.equalsIgnoreCase("ALL") ){
					List<Merchants> resellerMerchantList = new UserDao().getActiveResellerMerchants(sessionUser.getResellerId());
					if(!resellerMerchantList.isEmpty()){
						List<BasicDBObject> resellerMerchantPayId = new ArrayList<BasicDBObject>();
						for(Merchants merchant: resellerMerchantList){
							resellerMerchantPayId.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchant.getPayId()));
						}
						resellerPayIdQuery.put("$or", resellerMerchantPayId);
					}else{
						resellerPayIdQuery.put(FieldType.PAY_ID.getName(),sessionUser.getPayId() );
					}
				}
			} 
			
			if (StringUtils.isNotBlank(accountType) && !accountType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.ACCOUNT_TYPE.getName(), accountType));
			}
			if (StringUtils.isNotBlank(vpa)) {
				accountNumberQuery.put(FieldType.PAYER_ADDRESS.getName(), vpa);
			}


			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			if (accountNumberQuery.isEmpty()) {
				if (!finalQueryDate.isEmpty()) {
					allConditionQueryList.add(finalQueryDate);
				}
			}

			if (!userTypeQuery.isEmpty()) {
				allConditionQueryList.add(userTypeQuery);
			}
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();
			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			
			if (!resellerPayIdQuery.isEmpty()) {
				fianlList.add(resellerPayIdQuery);
			}

			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}
			if (!accountNumberQuery.isEmpty()) {
				fianlList.add(accountNumberQuery);
			}
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			FindIterable<Document> iterDoc = coll.find(finalquery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			
			//seprate PAYID
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				} 
				
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
					payIdd.add(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
				} else {
					payIdd.add(dbobj.getString(FieldType.PAY_ID.getName()));
				}
			}
			cursor.close();
			User user1 = new User();
			for (String pyId : payIdd) {
				orderIdd.clear();
				BasicDBObject finalquery2 = new BasicDBObject();
				List<BasicDBObject> paramConditionLst1 = new ArrayList<BasicDBObject>();
				user1 = userDao.findPayId(pyId);

				if (StringUtils.isNotBlank(user1.getSuperMerchantId())) {
					paramConditionLst1.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), pyId));
				} else {

					paramConditionLst1.add(new BasicDBObject(FieldType.PAY_ID.getName(), pyId));
				}
				if (accountNumberQuery.isEmpty()) {
					if (!finalQueryDate.isEmpty()) {
						paramConditionLst1.add(finalQueryDate);
					}
				}

				if (!userTypeQuery.isEmpty()) {
					paramConditionLst1.add(userTypeQuery);
				}
				if (!statusQuery.isEmpty()) {
					paramConditionLst1.add(statusQuery);
				}
				if (!accountNumberQuery.isEmpty()) {
					paramConditionLst1.add(accountNumberQuery);
				}
				if (!paramConditionLst1.isEmpty()) {
					finalquery2 = new BasicDBObject("$and", paramConditionLst1);
				}

				FindIterable<Document> iterDoc1 = coll.find(finalquery2);
				MongoCursor<Document> cursor1 = iterDoc1.iterator();
				
				//checking order id's for payId
				while (cursor1.hasNext()) {
					Document dbobj = cursor1.next();
					
					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						dbobj = dataEncDecTool.decryptDocument(dbobj);
					} 
					
					orderIdd.add(dbobj.getString(FieldType.ORDER_ID.getName()));
				}
				cursor1.close();

				for (String id : orderIdd) {
					List<BasicDBObject> paramConditionLst3 = new ArrayList<BasicDBObject>();
					if (StringUtils.isNotBlank(user1.getSuperMerchantId())) {
						paramConditionLst3.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), pyId));
					} else {
						paramConditionLst3.add(new BasicDBObject(FieldType.PAY_ID.getName(), pyId));
					}
					paramConditionLst3.add(new BasicDBObject(FieldType.ORDER_ID.getName(), id));
					BasicDBObject finalquery3 = new BasicDBObject();
					if (!paramConditionLst3.isEmpty()) {
						finalquery3 = new BasicDBObject("$and", paramConditionLst3);
					}

					BasicDBObject match = new BasicDBObject("$match", finalquery3);
					BasicDBObject sort = new BasicDBObject("$sort",
							new BasicDBObject(FieldType.CREATE_DATE.name(), -1));
					BasicDBObject limit = new BasicDBObject("$limit", 1);
					List<BasicDBObject> pipeline2 = Arrays.asList(match, sort, limit);
					AggregateIterable<Document> output2 = coll.aggregate(pipeline2);
					output2.allowDiskUse(true);
					MongoCursor<Document> cursor3 = output2.iterator();

					while (cursor3.hasNext()) {

						ImpsDownloadObject imps = new ImpsDownloadObject();
						Document dbobj = cursor3.next();

						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
							dbobj = dataEncDecTool.decryptDocument(dbobj);
						} 
						
						if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
							if ((("Invalid").equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName())))
									|| (("Declined").equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName())))
									|| (("Failed").equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName())))
									|| (("Duplicate").equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName())))) {

							} else {
								if (!(status.equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName())))) {
									break;
								}
							}
						}
						
						// For Merchant Business Name
						String merchantPayId = dbobj.getString(FieldType.PAY_ID.getName());
						User merchantUser = new User();

						if (userMap.get(merchantPayId) != null && !userMap.get(merchantPayId).getPayId().isEmpty()) {
							merchantUser = userMap.get(merchantUser);
						} else {
							merchantUser = userDao.findPayId(merchantPayId);
							userMap.put(merchantPayId, merchantUser);
						}

						if (merchantUser != null) {
							imps.setMerchant(merchantUser.getBusinessName());
						} else {
							imps.setMerchant(userDao.getBusinessNameByPayId(merchantPayId));
						}


						if (((!merchantId.equalsIgnoreCase("All")) || (merchantId.equalsIgnoreCase("All"))
								|| (merchantId.equalsIgnoreCase("")))
								&& dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
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
								imps.setSubMerchant(subMerchantUser.getBusinessName());
							} else {
								imps.setSubMerchant(CrmFieldConstants.NA.getValue());
							}
						} else {
							if ((merchantId.equalsIgnoreCase("All")) && (StringUtils.isNotBlank(subMerchantPayId) && subMerchantPayId.equalsIgnoreCase("All"))) {
								imps.setSubMerchant(CrmFieldConstants.NA.getValue());
							}
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
							imps.setMerchantPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
						} else {
							imps.setMerchantPayId(dbobj.getString(FieldType.PAY_ID.getName()));
						}
						imps.setTxnId(dbobj.getString(FieldType.BANK_REF_NUM.getName()));
						imps.setRrn(dbobj.getString(FieldType.RRN.getName()));
						imps.setOrderId(dbobj.getString(FieldType.ORDER_ID.getName()));
						imps.setPayerName(dbobj.getString(FieldType.PAYER_NAME.getName()));
						imps.setPayerAddress(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
						imps.setBankIFSC(dbobj.getString(FieldType.PAYER_IFSC.getName()));
						imps.setDate(format.format(sdfFormat.parse(dbobj.getString(FieldType.CREATE_DATE.getName()))));
						
						imps.setAccountType(dbobj.getString(FieldType.ACCOUNT_TYPE.getName()));
						imps.setOwnerType(dbobj.getString(FieldType.USER_TYPE.getName()));
						imps.setResponseMsg(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
						if (StringUtils.isNoneBlank(dbobj.getString(FieldType.PHONE_NO.getName()))) {
							imps.setPhoneNo(dbobj.getString(FieldType.PHONE_NO.getName()));
						} else {
							imps.setPhoneNo("NA");
						}
						
						if(dbobj.getString(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())){
							imps.setStatus(StatusType.VERIFIED.getName());
						}else if(dbobj.getString(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.TIMEOUT.getName()) 
								|| dbobj.getString(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PROCESSING.getName())){
							imps.setStatus(StatusType.PENDING.getName());
						}else{
							imps.setStatus(StatusType.FAILED.getName());
						}

						impsList.add(imps);
					}
				}

			}
			logger.info("Total data in impsList is " + impsList.size());
			return impsList;

		} catch (Exception e) {
			logger.error("Exception " , e);

		}
		return impsList;

	}

	public ImpsDownloadObject getImpsTransactionWithTxnId(String txnId) {
		ImpsDownloadObject imps = new ImpsDownloadObject();

		try {
			boolean isParameterised = false;

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));

			BasicDBObject finalQuery = new BasicDBObject();

			finalQuery.put(FieldType.TXN_ID.getName(), txnId);

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				} 

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
					imps.setMerchantPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
				} else {
					imps.setMerchantPayId(dbobj.getString(FieldType.PAY_ID.getName()));
				}

				imps.setOrderId(dbobj.getString(FieldType.ORDER_ID.getName()));
				imps.setTxnId(dbobj.getString(FieldType.TXN_ID.getName()));
				imps.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
				imps.setBankAccountName(dbobj.getString(FieldType.BENE_NAME.getName()));
				imps.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()));
				imps.setBankIFSC(dbobj.getString(FieldType.IFSC_CODE.getName()));
				imps.setDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				imps.setImpsRefNum(dbobj.getString(FieldType.RRN.getName()));
				imps.setSystemSettlementDate(dbobj.getString(FieldType.SETTLED_DATE.getName()));
				imps.setTxnsCapturedFrom(dbobj.getString(FieldType.CAPTURED_DATE_FROM.getName()));
				imps.setTxnsCapturedTo(dbobj.getString(FieldType.CAPTURED_DATE_TO.getName()));
				imps.setStatus(dbobj.getString(FieldType.STATUS.getName()));
				imps.setResponseMsg(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
				imps.setPhoneNo((dbobj.getString(FieldType.PHONE_NO.getName())));
				imps.setUserType((dbobj.getString(FieldType.USER_TYPE.getName())));

			}
			return imps;

		} catch (Exception e) {
			logger.error("Exception " , e);

		}
		return imps;

	}

	public boolean checkImpsDuplicateOrderId(String orderId) {
		ImpsDownloadObject imps = new ImpsDownloadObject();

		try {
			boolean isParameterised = false;

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));

			BasicDBObject finalQuery = new BasicDBObject();

			finalQuery.put(FieldType.ORDER_ID.getName(), orderId);
			finalQuery.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				return true;
			}

		} catch (Exception e) {
			logger.error("Exception " , e);

		}
		return false;

	}

	public BigDecimal getSettleTransactionDiff(ImpsDownloadObject fields) {
		logger.info("Inside  getSettleTransactionDiff()");
		BigDecimal diffrenceAmount = new BigDecimal("0").setScale(2);
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject query = new BasicDBObject();

			String payId = fields.getMerchantPayId();
			String subMerchantId = null;

			BigDecimal saleAmount = new BigDecimal("0").setScale(2);
			BigDecimal refundAmount = new BigDecimal("0").setScale(2);

			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantId = user.getPayId();
				payId = user.getSuperMerchantId();
			}

			if (StringUtils.isNotBlank(payId)) {
				query.append(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantId)) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			query.append(FieldType.STATUS.getName(), StatusType.SETTLED.getName());
			query.append(FieldType.DATE_INDEX.getName(),
					DateCreater.formatDateForDb(new Date()).substring(0, 10).replace("-", ""));

			MongoCursor<Document> cursor = coll.find(query).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				} 
				
				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).setScale(2);
				BigDecimal pgTdr = new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())).setScale(2);
				BigDecimal pgGst = new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())).setScale(2);
				BigDecimal acqTdr = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())).setScale(2);
				BigDecimal acqGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())).setScale(2);

				if (StringUtils.isBlank(String.valueOf(pgTdr))) {
					pgTdr.add(BigDecimal.ZERO);
				}
				if (StringUtils.isBlank(String.valueOf(pgGst))) {
					pgGst.add(BigDecimal.ZERO);
				}
				if (StringUtils.isBlank(String.valueOf(acqTdr))) {
					acqTdr.add(BigDecimal.ZERO);
				}
				if (StringUtils.isBlank(String.valueOf(acqGst))) {
					acqGst.add(BigDecimal.ZERO);
				}

				if (dbobj.getString(FieldType.TXNTYPE.getName()).equalsIgnoreCase("RECO")) {

					saleAmount = saleAmount.add(dbAmount).subtract(pgTdr).subtract(pgGst).subtract(acqTdr)
							.subtract(acqGst);
				} else if (dbobj.getString(FieldType.TXNTYPE.getName()).equalsIgnoreCase("RECOREFUND")) {

					refundAmount = refundAmount.add(dbAmount).subtract(pgTdr).subtract(pgGst).subtract(acqTdr)
							.subtract(acqGst);
				}

			}
			diffrenceAmount = saleAmount.subtract(refundAmount);
			logger.info("Amount after Settled (Sale-Refund) = " + diffrenceAmount);
		} catch (Exception e) {
			logger.error("Exception caugth while fetching transaction settled amount, " , e);
		}
		return diffrenceAmount;

	}

	public BigDecimal getImpsTransactionAmount(ImpsDownloadObject fields) {
		logger.info("Inside getImpsTransactionAmount()");
		BigDecimal totalAmount = new BigDecimal("0").setScale(2);
		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String todayDate = sdf.format(new Date());

			String dateFrom = todayDate + " " + "00:00:00";
			String dateTo = todayDate + " " + "23:59:59";

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject query = new BasicDBObject();

			query.append(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());

			String payId = fields.getMerchantPayId();
			String subMerchantId = null;

			BigDecimal settledAmount = new BigDecimal("0").setScale(2);

			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantId = user.getPayId();
				payId = user.getSuperMerchantId();
			}

			if (StringUtils.isNotBlank(payId)) {
				query.append(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantId)) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			query.append(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());

			MongoCursor<Document> cursor = coll.find(query).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				} 
				
				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				settledAmount = settledAmount.add(dbAmount);
			}
			totalAmount = settledAmount;
			logger.info("Total today's Transactions Amount  " + totalAmount);
		} catch (Exception e) {
			logger.error("Exception caugth fetching imps transaction, " , e);
		}
		return totalAmount;

	}
	
	 public BigDecimal getChargebackTransactionAmount(ImpsDownloadObject fields){
	        logger.info("inside getChargebackTransactionAmount()");
	        List<Chargeback> totalChargeBackData=null;
	        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	        BigDecimal chargebackAmount=new BigDecimal(0).setScale(2);
	        
	        try{
	        String todayDate=sdf.format(new Date());
	        
	        String dateFrom=todayDate+" "+"00:00:00";
	        String dateTo=todayDate+" "+"23:59:59";
	        
	        String payId=fields.getMerchantPayId();
	        String subMerchantId=null;

	 

	        User user=userDao.findPayId(payId);
	        
	        if(StringUtils.isNotBlank(user.getSuperMerchantId())){
	            subMerchantId=payId;
	            payId=user.getSuperMerchantId();
	        }
	        
	        totalChargeBackData=chargebackDao.findChargebackByPayid(payId, dateFrom, dateTo);/*(dateFrom, dateTo, payId, subMerchantId, "ALL", "Accepted");*/
	        
	        if(totalChargeBackData!=null){
	            for(Chargeback c:totalChargeBackData){
	                if(StringUtils.isNotBlank(subMerchantId)){
	                    if(c.getSubMerchantId().equals(subMerchantId) && (c.getStatus().equalsIgnoreCase("Accepted") || c.getStatus().equalsIgnoreCase("Open")))
	                        chargebackAmount=chargebackAmount.add(new BigDecimal(c.getCapturedAmount()));
	                }else{
	                    if(c.getStatus().equalsIgnoreCase("Accepted") || c.getStatus().equalsIgnoreCase("Open"))
	                        chargebackAmount=chargebackAmount.add(new BigDecimal(c.getCapturedAmount()));
	                    }
	            }
	        }
	        logger.info("Total chargeback amount is "+chargebackAmount);
	        }catch (Exception e) {
	            logger.error("Exception " , e);
	        }
	        return chargebackAmount;
	        
	    }

}
