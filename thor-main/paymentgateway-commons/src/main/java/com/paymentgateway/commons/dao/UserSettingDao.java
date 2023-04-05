package com.paymentgateway.commons.dao;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.UserSettingUtils;

@Service
public class UserSettingDao {

	private static final Logger logger = LoggerFactory.getLogger(UserSettingDao.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserSettingUtils userSettingUtils;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	@Autowired
	private UserDao userDao;

	public void saveOrUpdate(UserSettingData userSetting) {
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.USER_SETTINGS_COLLECTION.getValue()));

			Document userSettingDoc = userSettingUtils.objectToDocument(userSetting);

			if (!userSettingDoc.isEmpty() && StringUtils.isNotBlank(userSetting.getPayId())) {

				if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
						&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					userSettingDoc = dataEncDecTool.encryptDocument(userSettingDoc);
				}

				if (isDataSavedForMerchant(userSetting.getPayId())) {

					// Updating logo
					userSettingUtils.saveMerchantLogo(userSetting);

					updateData(userSettingDoc);

				} else {

					// Updating logo
					userSettingUtils.saveMerchantLogo(userSetting);

					coll.insertOne(userSettingDoc);
				}
			}

		} catch (Exception e) {
			logger.info("exception in save userSettings ", e);
		}
	}

	public UserSettingData fetchDataUsingPayId(String payId) {

		UserSettingData userSettingData = new UserSettingData();

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.USER_SETTINGS_COLLECTION.getValue()));

			BasicDBObject query = new BasicDBObject("payId", payId);

			MongoCursor<Document> cursor = coll.find(query).iterator();

			if (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
						&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				// check variable if any new added
				verifyObjectVariable(dbobj, userSettingData);

				userSettingData = userSettingUtils.documentToObject(dbobj);

				// getting logo
				userSettingData.setMerchantLogo(userSettingUtils.getBase64LogoPerMerchant(userSettingData));

			}

		} catch (Exception e) {
			logger.info("exception in save userSettings ", e);
		}
		return userSettingData;
	}

	public UserSettingData fetchData(UserSettingData userSettingData) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.USER_SETTINGS_COLLECTION.getValue()));

			BasicDBObject query = new BasicDBObject("payId", userSettingData.getPayId());

			MongoCursor<Document> cursor = coll.find(query).iterator();

			if (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
						&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				// check variable if any new added
				verifyObjectVariable(dbobj, userSettingData);

				userSettingData = userSettingUtils.documentToObject(dbobj);

				// getting logo
				userSettingData.setMerchantLogo(userSettingUtils.getBase64LogoPerMerchant(userSettingData));

			}

		} catch (Exception e) {
			logger.info("exception in save userSettings ", e);
		}
		return userSettingData;
	}

	private void verifyObjectVariable(Document dbobj, UserSettingData userSettingData) {
		// logger.info("inside verifyObjectVariable");

		try {
			Field[] variableField = userSettingData.getClass().getDeclaredFields();

			for (Field field : variableField) {
				if (!dbobj.containsKey(field.getName())) {
					if (field.getType().equals(String.class)) {
						dbobj.put(field.getName(), "");
					} else if (field.getType().equals(boolean.class)) {
						dbobj.put(field.getName(), false);
					}
				}
			}
		} catch (Exception e) {
			logger.info("Exception in verifyObjectVariable() ", e);
		}

	}

	public void updateData(Document userSettingDoc) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.USER_SETTINGS_COLLECTION.getValue()));

			Bson filter = new Document("payId", userSettingDoc.getString("payId"));

			Bson newValue = userSettingDoc;

			Bson updateOperationDocument = new Document("$set", newValue);
			coll.updateOne(filter, updateOperationDocument);

		} catch (Exception e) {
			logger.info("exception in save userSettings ", e);
		}

	}

	public boolean isDataSavedForMerchant(String payId) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.USER_SETTINGS_COLLECTION.getValue()));

			BasicDBObject query = new BasicDBObject("payId", payId);

			long dataCount = coll.count(query);

			if (dataCount > 0) {
				return true;
			}

		} catch (Exception e) {
			logger.info("exception in save userSettings ", e);
		}

		return false;
	}

	public void addUserFlagsInMongoDb() {
		try {
			List<User> userList = userDao.getAllMerchantList();

			for (User user : userList) {

				UserSettingData userSetting = new UserSettingData();

				userSetting.setPayId(user.getPayId());
				userSetting.setBusinessName(user.getBusinessName());

				if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
					userSetting.setSuperMerchantId(user.getSuperMerchantId());
					userSetting.setSuperMerchantName(userDao.getBusinessNameByPayId(user.getSuperMerchantId()));
				}
				if (StringUtils.isNotBlank(user.getAttemptTrasacation()))
					userSetting.setAttemptTrasacation(user.getAttemptTrasacation());
				if (StringUtils.isNotBlank(user.getCallBackUrl()))
					userSetting.setCallBackUrl(user.getCallBackUrl());
				if (StringUtils.isNotBlank(user.getCardSaveParam()))
					userSetting.setCardSaveParam(user.getCardSaveParam());
				if (StringUtils.isNotBlank(user.getCodName()))
					userSetting.setCodName(user.getCodName());
				if (StringUtils.isNotBlank(user.getCustomHostedUrl()))
					userSetting.setCustomHostedUrl(user.getCustomHostedUrl());
				if (StringUtils.isNotBlank(user.getDeviation()))
					userSetting.setDeviation(user.getDeviation());
				if (StringUtils.isNotBlank(user.getNbSaveParam()))
					userSetting.setNbSaveParam(user.getNbSaveParam());
				if (StringUtils.isNotBlank(user.getVpaSaveParam()))
					userSetting.setVpaSaveParam(user.getVpaSaveParam());
				if (StringUtils.isNotBlank(user.getWhiteListReturnUrl()))
					userSetting.setWhiteListReturnUrl(user.getWhiteListReturnUrl());
				if (StringUtils.isNotBlank(user.getWlSaveParam()))
					userSetting.setWlSaveParam(user.getWlSaveParam());
				if (StringUtils.isNotBlank(user.getmCC()))
					userSetting.setmCC(user.getmCC());
				if (StringUtils.isNotBlank(user.getDefaultCurrency()))
					userSetting.setDefaultCurrency(user.getDefaultCurrency());

				userSetting.setAcceptPostSettledInEnquiry(user.isAcceptPostSettledInEnquiry());
				userSetting.setAccountVerificationFlag(user.isAccountVerificationFlag());
				userSetting.setAllCallBackFlag(user.isAllCallBackFlag());
				userSetting.setAllowCustomHostedUrl(user.isAllowCustomHostedUrl());
				userSetting.setAllowDuplicateNot(user.isAllowDuplicateNot());
				userSetting.setAllowECollectionFee(user.isAllowECollectionFee());
				userSetting.setAllowLogoInPgPage(user.isAllowLogoInPgPage());
				userSetting.setAllowNodalPayoutFlag(user.isAllowNodalPayoutFlag());
				userSetting.setAllowPartSettle(user.isAllowPartSettle());
				userSetting.setAllowPayoutUpdateStatus(user.isAllowPayoutUpdateStatus());
				userSetting.setAllowQRScanFlag(user.isAllowQRScanFlag());
				userSetting.setAllowRefundDuplicate(user.isAllowRefundDuplicate());
				userSetting.setAllowRefundInSale(user.isAllowRefundInSale());
				userSetting.setAllowSaleDuplicate(user.isAllowSaleDuplicate());
				userSetting.setAllowSaleInRefund(user.isAllowSaleInRefund());
				userSetting.setAllowSubtractValue(user.isAllowSubtractValue());
				userSetting.setAllowUpiQRFlag(user.isAllowUpiQRFlag());
				userSetting.setAutoRefund(user.isAutoRefund());
				userSetting.setBookingRecord(user.isBookingRecord());
				userSetting.setCallBackFlag(user.isCallBackFlag());
				userSetting.setCapturedMerchantFlag(user.isCapturedMerchantFlag());
				userSetting.setCheckOutJsFlag(user.isCheckOutJsFlag());
				userSetting.setCustomerQrFlag(user.isCustomerQrFlag());
				userSetting.setCustomTransactionStatus(user.isCustomTransactionStatus());
				userSetting.setDiscountingFlag(user.isDiscountingFlag());
				userSetting.seteNachReportFlag(user.iseNachReportFlag());
				userSetting.setEposMerchant(user.isEposMerchant());
				userSetting.setExpressPayFlag(user.isExpressPayFlag());
				userSetting.setIframePaymentFlag(user.isIframePaymentFlag());
				userSetting.setLoadWalletFlag(user.isLoadWalletFlag());
				userSetting.setLogoFlag(user.isLogoFlag());
				userSetting.setLyraPay(user.isLyraPay());
				userSetting.setMerchantHostedFlag(user.isMerchantHostedFlag());
				userSetting.setMerchantInitiatedDirectFlag(user.isMerchantInitiatedDirectFlag());
				userSetting.setNetSettledFlag(user.isNetSettledFlag());
				userSetting.setNodalReportFlag(user.isNodalReportFlag());
				userSetting.setNon3dsTxn(user.isNon3dsTxn());
				userSetting.setPaymentAdviceFlag(user.isPaymentAdviceFlag());
				userSetting.setPaymentMessageSlab(user.getPaymentMessageSlab());
				userSetting.setRetailMerchantFlag(user.isRetailMerchantFlag());
				userSetting.setRetryTransactionCustomeFlag(user.isRetryTransactionCustomeFlag());
				userSetting.setSaveNBFlag(user.isSaveNBFlag());
				userSetting.setSaveVPAFlag(user.isSaveVPAFlag());
				userSetting.setSaveWLFlag(user.isSaveWLFlag());
				userSetting.setSkipOrderIdForRefund(user.isSkipOrderIdForRefund());
				userSetting.setStatementFlag(user.isStatementFlag());
				userSetting.setSurchargeFlag(user.isSurchargeFlag());
				userSetting.setTopupFlag(user.isTopupFlag());
				userSetting.setUpiAutoPayReportFlag(user.isUpiAutoPayReportFlag());
				userSetting.setVirtualAccountFlag(user.isVirtualAccountFlag());
				userSetting.setVpaVerificationFlag(user.isVpaVerificationFlag());
				userSetting.setWhiteListReturnUrlFlag(user.isWhiteListReturnUrlFlag());

				saveOrUpdate(userSetting);
			}

		} catch (Exception e) {
			logger.info("exception in adding addUserFlagsInMongoDb() ", e);
		}

	}

	public void updatePayIdInMongo() {
		try {
			List<User> userList = userDao.getAllMerchantList();

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.USER_SETTINGS_COLLECTION.getValue()));

			for (User user : userList) {

				UserSettingData userSetting = new UserSettingData();

				userSetting.setPayId(user.getPayId());
				userSetting.setBusinessName(user.getBusinessName());

				if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
					userSetting.setSuperMerchantId(user.getSuperMerchantId());
					userSetting.setSuperMerchantName(userDao.getBusinessNameByPayId(user.getSuperMerchantId()));
				}

				BasicDBObject queryPayId = new BasicDBObject("payId", user.getPayId());

				MongoCursor<Document> cursor = coll.find(queryPayId).iterator();

				if (cursor.hasNext()) {
					continue;
				} else {
					logger.info("not Found PayId in USERSETTING >> {}, searching with businessName == {} ",
							user.getPayId(), user.getBusinessName());

					BasicDBObject businessNameQuery = new BasicDBObject("businessName", user.getBusinessName());

					long count = coll.count(businessNameQuery);

					if (count == 1) {
						MongoCursor<Document> cursor1 = coll.find(businessNameQuery).iterator();

						if (cursor1.hasNext()) {
							Document doc = cursor1.next();
							String payIdDb = doc.getString("payId");

							if (!payIdDb.equals(user.getPayId())) {

								Bson filter = businessNameQuery;

								Bson newValue = new Document("payId", user.getPayId());

								Bson updateOperationDocument = new Document("$set", newValue);

								coll.updateOne(filter, updateOperationDocument);

								logger.info(
										"Updated payId for Usersetting  Old PayId == {} , filterQuery == {}, new Value = {} ",
										payIdDb, filter, newValue);

							}
						}
						cursor1.close();
					}

				}
			}

		} catch (Exception e) {
			logger.info("exception in adding addUserFlagsInMongoDb() ", e);
		}

	}

	public Map<String, Merchants> getActiveMerchantByPaymentAdvice() {
		Map<String, Merchants> merchantMap = new HashMap<String, Merchants>();

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.USER_SETTINGS_COLLECTION.getValue()));

			BasicDBObject query = new BasicDBObject("paymentAdviceFlag", true);

			MongoCursor<Document> cursor = coll.find(query).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
						&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				Merchants merchant = new Merchants();
				merchant.setPayId((String) dbobj.getString("payId"));
				merchant.setBusinessName((String) dbobj.getString("businessName"));
				merchant.setPaymentAdviceFlag(true);
				merchantMap.put(dbobj.getString("payId"), merchant);
			}

		} catch (Exception e) {
			logger.info("exception in getActiveMerchantByPaymentAdvice() ", e);
		}

		return merchantMap;
	}

	public UserSettingData fetchDataUsingIsgPayMID(String merchantID) {

		UserSettingData userSettingData = new UserSettingData();

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.USER_SETTINGS_COLLECTION.getValue()));

			BasicDBObject query = new BasicDBObject("isgPayMerchantID", merchantID);
			MongoCursor<Document> cursor = coll.find(query).iterator();
			if (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("EncryptDBData"))
						&& PropertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				// check variable if any new added
				verifyObjectVariable(dbobj, userSettingData);
				userSettingData = userSettingUtils.documentToObject(dbobj);
				// getting logo
				userSettingData.setMerchantLogo(userSettingUtils.getBase64LogoPerMerchant(userSettingData));

			}

		} catch (Exception e) {
			logger.info("exception in save userSettings ", e);
		}
		return userSettingData;
	}

}
