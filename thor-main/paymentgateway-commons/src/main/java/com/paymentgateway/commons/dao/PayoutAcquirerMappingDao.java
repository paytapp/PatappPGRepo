package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.PayoutAcquireMapping;
import com.paymentgateway.commons.user.PayoutVpa;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PayoutVpaUtils;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class PayoutAcquirerMappingDao {

	private static Logger logger = LoggerFactory.getLogger(PayoutAcquirerMappingDao.class.getName());

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private EncryptDecryptService encryptDecryptService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	@Autowired
	private PayoutVpaUtils payoutVpaUtils;

	private static final String prefix = "MONGO_DB_";
	private static final String Active = "Active";
	private static final String Deactivated = "Deactivated";

	public void saveMapping(PayoutAcquireMapping acqMapping) throws SystemException {
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_ACQUIERE_MAPPING.getValue()));

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			BasicDBObject newFieldsObj = new BasicDBObject();

			if (StringUtils.isNotBlank(acqMapping.getUserType())) {
				newFieldsObj.put(FieldType.USER_TYPE.getName(), acqMapping.getUserType());
			}
			if (StringUtils.isNotBlank(acqMapping.getBankName())) {
				newFieldsObj.put(FieldType.ACQUIRER_NAME.getName(), acqMapping.getBankName());
			}

			if (StringUtils.isNotBlank(acqMapping.getAccountType())) {
				newFieldsObj.put(FieldType.ACCOUNT_TYPE.getName(), acqMapping.getAccountType());
			}

			// checking already data in DB
			MongoCursor<Document> cursor = collection.find(newFieldsObj).iterator();

			String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");
			if (StringUtils.isNotBlank(acqMapping.getAdfFields())) {
				JSONObject adfData = new JSONObject(acqMapping.getAdfFields());

				for (String key : adfData.keySet()) {
					if (StringUtils.isNotBlank(adfData.getString(key)))
						newFieldsObj.put(key, encryptDecryptService.encrypt(adminPayId, adfData.getString(key)));
					else
						newFieldsObj.put(key, "");
				}
			}

			Document doc = new Document(newFieldsObj);

			// if value found in DB then it will update else insert new
			if (cursor.hasNext()) {
				Bson filter = new Document(FieldType.ACCOUNT_TYPE.getName(), acqMapping.getAccountType())
						.append(FieldType.ACQUIRER_NAME.getName(), acqMapping.getBankName())
						.append(FieldType.USER_TYPE.getName(), acqMapping.getUserType());
				doc.put(FieldType.UPDATE_DATE.getName(), dateNow);
				Bson newValue = doc;
				logger.info("updating acq mapping fields " + doc + " for " + filter);

				Bson updateOperationDocument = new Document("$set", newValue);
				collection.updateOne(filter, updateOperationDocument);
			} else {
				doc.put(FieldType.CREATE_DATE.getName(), dateNow);
				doc.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));
				logger.info("Inserting acq mapping fields " + doc);
				collection.insertOne(doc);
			}

		} catch (Exception exception) {
			String message = "Error while inserting acquirer Saving Payout Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

	}

	public void fetchSavedMapping(PayoutAcquireMapping acqMapping) throws SystemException {

		try {

			BasicDBObject fetchQuery = new BasicDBObject();

			if (StringUtils.isNotBlank(acqMapping.getUserType())) {
				fetchQuery.put(FieldType.USER_TYPE.getName(), acqMapping.getUserType());
			}
			if (StringUtils.isNotBlank(acqMapping.getBankName())) {
				fetchQuery.put(FieldType.ACQUIRER_NAME.getName(), acqMapping.getBankName());
			}

			if (StringUtils.isNotBlank(acqMapping.getAccountType())) {
				fetchQuery.put(FieldType.ACCOUNT_TYPE.getName(), acqMapping.getAccountType());
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_ACQUIERE_MAPPING.getValue()));
			MongoCursor<Document> cursor = collection.find(fetchQuery).iterator();

			String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");

			if (cursor.hasNext()) {
				Document documentObj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				acqMapping.setAccountType(documentObj.getString(FieldType.ACCOUNT_TYPE.getName()));
				acqMapping.setBankName(documentObj.getString(FieldType.ACQUIRER_NAME.getName()));
				acqMapping.setUserType(documentObj.getString(FieldType.USER_TYPE.getName()));
				
				JSONObject adfField = new JSONObject();
				for (String entry : documentObj.keySet()) {
					if (entry.contains("ADF")) {
						if(StringUtils.isNotBlank(documentObj.getString(entry)))
							adfField.put(entry, encryptDecryptService.decrypt(adminPayId, documentObj.getString(entry)));
						else
							adfField.put(entry, "");
					}	
				}

				acqMapping.setAdfFields(adfField.toString());

			}

		} catch (Exception exception) {
			String message = "Error while inserting fetching acquirer Payout Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

	}

	public void saveMerchantMapping(PayoutAcquireMapping acqMapping) throws SystemException {
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_MERCHANT_MAPPING.getValue()));

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			BasicDBObject newFieldsObj = new BasicDBObject();

			if (StringUtils.isNotBlank(acqMapping.getPayId())) {
				newFieldsObj.put(FieldType.PAY_ID.getName(), acqMapping.getPayId());
			}
			
			if (StringUtils.isNotBlank(acqMapping.getSubMerchantPayId()) && !acqMapping.getSubMerchantPayId().equalsIgnoreCase("ALL")) {
				newFieldsObj.put(FieldType.SUB_MERCHANT_ID.getName(), acqMapping.getSubMerchantPayId());
			}

			newFieldsObj.put(FieldType.STATUS.getName(), Active);

			MongoCursor<Document> cursor = collection.find(newFieldsObj).iterator();

			Document doc = new Document(newFieldsObj);

			//Inactive old mapping
			if (cursor.hasNext()) {
				Bson filter = doc;

				Bson newValue = new Document(FieldType.STATUS.getName(), Deactivated)
						.append(FieldType.UPDATE_DATE.getName(), dateNow);
				logger.info("updating acq mapping fields " + doc + " for " + filter);

				Bson updateOperationDocument = new Document("$set", newValue);

				collection.updateOne(filter, updateOperationDocument);
			}
			
			PayoutVpa payoutVpa=null;
			
			if(payoutVpaUtils.isVpaSaved(acqMapping)){
				payoutVpa= payoutVpaUtils.fetchVpaDetail(acqMapping);
			}else{
				payoutVpa=payoutVpaUtils.createVpa(acqMapping);
			}
			
			acqMapping.setStatus(payoutVpa.getStatus());
			
			if(payoutVpa.getStatus().equalsIgnoreCase(ErrorType.SUCCESS.getResponseMessage())){
				//update user
				User user=null;
				if(StringUtils.isBlank(acqMapping.getSubMerchantPayId())){
					user= userDao.findPayId(acqMapping.getPayId());
				}else{
					user= userDao.findPayId(acqMapping.getSubMerchantPayId());
				}
				
				user.setVirtualAccountNo(payoutVpa.getVan());
				user.setVirtualIfscCode(payoutVpa.getVanIfsc());
				user.setVirtualBeneficiaryName(payoutVpa.getVanBeneficiaryName());
				
				if (StringUtils.isNotBlank(payoutVpa.getVpa()))
					user.setMerchantVPA(payoutVpa.getVpa());
				else
					user.setMerchantVPA("");
				
				userDao.update(user);
				
				//update in payout mapping
				doc.put(FieldType.CREATE_DATE.getName(), dateNow);
				doc.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));
				doc.put(FieldType.ACCOUNT_TYPE.getName(), acqMapping.getAccountType());
				doc.put(FieldType.USER_TYPE.getName(), acqMapping.getUserType());
				doc.put(FieldType.ACQUIRER_NAME.getName(), acqMapping.getBankName());
				
				if(StringUtils.isNotBlank(payoutVpa.getSubWalletId()))
					doc.put(FieldType.SUB_WALLET_ID.getName(), payoutVpa.getSubWalletId());
					
				doc.put(FieldType.VIRTUAL_ACC_NUM.getName(), payoutVpa.getVan());
				doc.put(FieldType.VIRTUAL_BENEFICIARY_NAME.getName(), payoutVpa.getVanBeneficiaryName());
				doc.put(FieldType.IFSC_CODE.getName(), payoutVpa.getVanIfsc());
				
				logger.info("updating merchant mapping fields " + doc);
				collection.insertOne(doc);
			}else{
				logger.info("Didn't update in table");
			}
			
		} catch (Exception exception) {
			String message = "Error while inserting Saving Payout Merchant Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

	}

	public void fetchSavedMerchantMapping(PayoutAcquireMapping acqMapping) throws SystemException {

		try {

			BasicDBObject fetchQuery = new BasicDBObject();

			// if (StringUtils.isNotBlank(acqMapping.getUserType())) {
			// fetchQuery.put(FieldType.USER_TYPE.getName(),
			// acqMapping.getUserType());
			// }
			// if (StringUtils.isNotBlank(acqMapping.getBankName())) {
			// fetchQuery.put(FieldType.ACQUIRER_NAME.getName(),
			// acqMapping.getBankName());
			// }
			//
			// if (StringUtils.isNotBlank(acqMapping.getAccountType())) {
			// fetchQuery.put(FieldType.ACCOUNT_TYPE.getName(),
			// acqMapping.getAccountType());
			// }

			if (StringUtils.isNotBlank(acqMapping.getPayId())) {
				fetchQuery.put(FieldType.PAY_ID.getName(), acqMapping.getPayId());
			}
			if (StringUtils.isNotBlank(acqMapping.getSubMerchantPayId()) && !acqMapping.getSubMerchantPayId().equalsIgnoreCase("ALL")) {
				fetchQuery.put(FieldType.SUB_MERCHANT_ID.getName(), acqMapping.getSubMerchantPayId());
			}
			

			fetchQuery.put(FieldType.STATUS.getName(), Active);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_MERCHANT_MAPPING.getValue()));
			MongoCursor<Document> cursor = collection.find(fetchQuery).iterator();

			if (cursor.hasNext()) {
				Document documentObj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				acqMapping.setPayId(documentObj.getString(FieldType.PAY_ID.getName()));
				acqMapping.setMerchantName(
						userDao.getBusinessNameByPayId(documentObj.getString(FieldType.PAY_ID.getName())));
				acqMapping.setAccountType(documentObj.getString(FieldType.ACCOUNT_TYPE.getName()));
				acqMapping.setUserType(documentObj.getString(FieldType.USER_TYPE.getName()));
				acqMapping.setBankName(documentObj.getString(FieldType.ACQUIRER_NAME.getName()));
				acqMapping.setVan(documentObj.getString(FieldType.VIRTUAL_ACC_NUM.getName()));
				acqMapping.setVanIfsc(documentObj.getString(FieldType.IFSC_CODE.getName()));
				
				if(StringUtils.isNotBlank(acqMapping.getSubMerchantName())){
					acqMapping.setSubMerchantName(
							userDao.getBusinessNameByPayId(documentObj.getString(FieldType.SUB_MERCHANT_ID.getName())));
				}
			}

		} catch (Exception exception) {
			String message = "Error while fetching Payout Merchant Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

	}

	public List<PayoutAcquireMapping> fetchAllSavedMerchantMapping(PayoutAcquireMapping acqMapping)
			throws SystemException {

		try {

			List<PayoutAcquireMapping> mappedDataList = new ArrayList<PayoutAcquireMapping>();

			BasicDBObject fetchQuery = new BasicDBObject();

			if (StringUtils.isNotBlank(acqMapping.getPayId()) && !acqMapping.getPayId().equalsIgnoreCase("ALL")) {
				fetchQuery.put(FieldType.PAY_ID.getName(), acqMapping.getPayId());
			}
			if (StringUtils.isNotBlank(acqMapping.getSubMerchantPayId()) && !acqMapping.getSubMerchantPayId().equalsIgnoreCase("ALL")) {
				fetchQuery.put(FieldType.SUB_MERCHANT_ID.getName(), acqMapping.getSubMerchantPayId());
			}

			fetchQuery.put(FieldType.STATUS.getName(), Active);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_MERCHANT_MAPPING.getValue()));
			MongoCursor<Document> cursor = collection.find(fetchQuery).iterator();

			while (cursor.hasNext()) {
				Document documentObj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				PayoutAcquireMapping record = new PayoutAcquireMapping();

				record.setPayId(documentObj.getString(FieldType.PAY_ID.getName()));
				record.setMerchantName(
						userDao.getBusinessNameByPayId(documentObj.getString(FieldType.PAY_ID.getName())));
				record.setAccountType(documentObj.getString(FieldType.ACCOUNT_TYPE.getName()));
				record.setUserType(documentObj.getString(FieldType.USER_TYPE.getName()));
				record.setBankName(documentObj.getString(FieldType.ACQUIRER_NAME.getName()));
				record.setVan(documentObj.getString(FieldType.VIRTUAL_ACC_NUM.getName()));
				record.setVanIfsc(documentObj.getString(FieldType.IFSC_CODE.getName()));
				
				if(StringUtils.isNotBlank(documentObj.getString(FieldType.SUB_MERCHANT_ID.getName()))){
					record.setSubMerchantPayId(documentObj.getString(FieldType.SUB_MERCHANT_ID.getName()));
					record.setSubMerchantName(
							userDao.getBusinessNameByPayId(documentObj.getString(FieldType.SUB_MERCHANT_ID.getName())));
				}
				
				mappedDataList.add(record);
			}

			acqMapping.setMerchantMappedData(mappedDataList);

		} catch (Exception exception) {
			String message = "Error while fetching Payout Merchant Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
		return null;

	}

	public void deleteMerchantMapping(PayoutAcquireMapping acqMapping) throws SystemException {

		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			BasicDBObject fetchQuery = new BasicDBObject();

			if (StringUtils.isNotBlank(acqMapping.getPayId())) {
				fetchQuery.put(FieldType.PAY_ID.getName(), acqMapping.getPayId());
			}
			if (StringUtils.isNotBlank(acqMapping.getSubMerchantPayId()) && !acqMapping.getSubMerchantPayId().equalsIgnoreCase("null")) {
				fetchQuery.put(FieldType.SUB_MERCHANT_ID.getName(), acqMapping.getSubMerchantPayId());
			}

			fetchQuery.put(FieldType.STATUS.getName(), Active);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_MERCHANT_MAPPING.getValue()));

			Bson filter = new Document(fetchQuery);

			Bson newValue = new Document(FieldType.STATUS.getName(), Deactivated)
					.append(FieldType.UPDATE_DATE.getName(), dateNow);
			logger.info("deleting acq mapping fields " + newValue + " for " + filter);

			Bson updateOperationDocument = new Document("$set", newValue);
			collection.updateOne(filter, updateOperationDocument);

		} catch (Exception exception) {
			String message = "Error while Deleting Payout Merchant Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public PayoutAcquireMapping fetchSavedMappingByPayId(String payId) throws SystemException {
		logger.info("inside fetchSavedMappingByPayId()");
		PayoutAcquireMapping acqMapping = new PayoutAcquireMapping();
		try {

			BasicDBObject fetchQuery = new BasicDBObject();
			
			User user=userDao.findPayId(payId);
			
			if(StringUtils.isNotBlank(user.getSuperMerchantId())){
				fetchQuery.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				fetchQuery.put(FieldType.SUB_MERCHANT_ID.getName(), payId);
			}else{
				fetchQuery.put(FieldType.PAY_ID.getName(), payId);
			}

			fetchQuery.put(FieldType.STATUS.getName(), Active);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_MERCHANT_MAPPING.getValue()));
			MongoCursor<Document> cursor = collection.find(fetchQuery).iterator();

			if (cursor.hasNext()) {
				Document documentObj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				acqMapping.setPayId(documentObj.getString(FieldType.PAY_ID.getName()));
				acqMapping.setAccountType(documentObj.getString(FieldType.ACCOUNT_TYPE.getName()));
				acqMapping.setUserType(documentObj.getString(FieldType.USER_TYPE.getName()));
				acqMapping.setBankName(documentObj.getString(FieldType.ACQUIRER_NAME.getName()));
				acqMapping.setSubWalletId(documentObj.getString(FieldType.SUB_WALLET_ID.getName()));

				cursor.close();

				// fetch ACQ details
				BasicDBObject fetchQueryAcqMapping = new BasicDBObject();

				if (StringUtils.isNotBlank(acqMapping.getUserType())) {
					fetchQueryAcqMapping.put(FieldType.USER_TYPE.getName(), acqMapping.getUserType());
				}
				if (StringUtils.isNotBlank(acqMapping.getBankName())) {
					fetchQueryAcqMapping.put(FieldType.ACQUIRER_NAME.getName(), acqMapping.getBankName());
				}

				if (StringUtils.isNotBlank(acqMapping.getAccountType())) {
					fetchQueryAcqMapping.put(FieldType.ACCOUNT_TYPE.getName(), acqMapping.getAccountType());
				}

				MongoCollection<Document> coll = dbIns.getCollection(
						PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_ACQUIERE_MAPPING.getValue()));
				MongoCursor<Document> cursorAcq = coll.find(fetchQueryAcqMapping).iterator();

				String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");

				if (cursorAcq.hasNext()) {
					Document docObj = cursorAcq.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						docObj = dataEncDecTool.decryptDocument(docObj);
					}

					acqMapping.setAccountType(docObj.getString(FieldType.ACCOUNT_TYPE.getName()));
					acqMapping.setBankName(docObj.getString(FieldType.ACQUIRER_NAME.getName()));
					acqMapping.setUserType(docObj.getString(FieldType.USER_TYPE.getName()));

					JSONObject adfField = new JSONObject();
					for (String entry : docObj.keySet()) {
						if (entry.contains("ADF")) {
							adfField.put(entry, encryptDecryptService.decrypt(adminPayId, docObj.getString(entry)));
						}
					}

					acqMapping.setAdfFields(adfField.toString());
				}
				cursorAcq.close();
			}
			
		} catch (Exception exception) {
			String message = "Error while fetching Payout Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

		return acqMapping;
	}
	
	public PayoutAcquireMapping findMappingwithAcquirerNameAndVirtualAcNo(String payId, String acquirerName, String virtualAcNo) throws SystemException {
		logger.info("inside findMappingwithAcquirerNameAndVirtualAcNo()");
		PayoutAcquireMapping acqMapping = new PayoutAcquireMapping();
		try {

			BasicDBObject fetchQuery = new BasicDBObject();
			
			User user=userDao.findPayId(payId);
			
			if(StringUtils.isNotBlank(user.getSuperMerchantId())){
				fetchQuery.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				fetchQuery.put(FieldType.SUB_MERCHANT_ID.getName(), payId);
			}else{
				fetchQuery.put(FieldType.PAY_ID.getName(), payId);
			}
			
			fetchQuery.put(FieldType.VIRTUAL_ACC_NUM.getName(), virtualAcNo);
			fetchQuery.put(FieldType.ACQUIRER_NAME.getName(), acquirerName);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_MERCHANT_MAPPING.getValue()));
			MongoCursor<Document> cursor = collection.find(fetchQuery).iterator();

			if (cursor.hasNext()) {
				Document documentObj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				acqMapping.setPayId(documentObj.getString(FieldType.PAY_ID.getName()));
				acqMapping.setAccountType(documentObj.getString(FieldType.ACCOUNT_TYPE.getName()));
				acqMapping.setUserType(documentObj.getString(FieldType.USER_TYPE.getName()));
				acqMapping.setBankName(documentObj.getString(FieldType.ACQUIRER_NAME.getName()));
				
				if(StringUtils.isNotBlank(documentObj.getString(FieldType.SUB_WALLET_ID.getName())))
					acqMapping.setSubWalletId(documentObj.getString(FieldType.SUB_WALLET_ID.getName()));

				cursor.close();

				// fetch ACQ details
				BasicDBObject fetchQueryAcqMapping = new BasicDBObject();

				if (StringUtils.isNotBlank(acqMapping.getUserType())) {
					fetchQueryAcqMapping.put(FieldType.USER_TYPE.getName(), acqMapping.getUserType());
				}
				if (StringUtils.isNotBlank(acqMapping.getBankName())) {
					fetchQueryAcqMapping.put(FieldType.ACQUIRER_NAME.getName(), acqMapping.getBankName());
				}

				if (StringUtils.isNotBlank(acqMapping.getAccountType())) {
					fetchQueryAcqMapping.put(FieldType.ACCOUNT_TYPE.getName(), acqMapping.getAccountType());
				}

				MongoCollection<Document> coll = dbIns.getCollection(
						PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_ACQUIERE_MAPPING.getValue()));
				MongoCursor<Document> cursorAcq = coll.find(fetchQueryAcqMapping).iterator();

				String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");

				if (cursorAcq.hasNext()) {
					Document docObj = cursorAcq.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						docObj = dataEncDecTool.decryptDocument(docObj);
					}

					acqMapping.setAccountType(docObj.getString(FieldType.ACCOUNT_TYPE.getName()));
					acqMapping.setBankName(docObj.getString(FieldType.ACQUIRER_NAME.getName()));
					acqMapping.setUserType(docObj.getString(FieldType.USER_TYPE.getName()));

					JSONObject adfField = new JSONObject();
					for (String entry : docObj.keySet()) {
						if (entry.contains("ADF")) {
							adfField.put(entry, encryptDecryptService.decrypt(adminPayId, docObj.getString(entry)));
						}
					}

					acqMapping.setAdfFields(adfField.toString());
				}
				cursorAcq.close();
			}
			
		} catch (Exception exception) {
			String message = "Error while fetching Payout Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

		return acqMapping;
	}

	public PayoutAcquireMapping fetchAcqAdfFields(String bankName, String userType, String accountType)
			throws SystemException {

		PayoutAcquireMapping acqMapping = new PayoutAcquireMapping();
		try {

			BasicDBObject fetchQuery = new BasicDBObject();

			if (StringUtils.isNotBlank(bankName)) {
				fetchQuery.put(FieldType.ACQUIRER_NAME.getName(), bankName);
			}

			if (StringUtils.isNotBlank(userType)) {
				fetchQuery.put(FieldType.USER_TYPE.getName(), userType);
			}

			if (StringUtils.isNotBlank(accountType)) {
				fetchQuery.put(FieldType.ACCOUNT_TYPE.getName(), accountType);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_ACQUIERE_MAPPING.getValue()));
			MongoCursor<Document> cursor = collection.find(fetchQuery).iterator();

			if (cursor.hasNext()) {
				Document documentObj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				acqMapping.setAccountType(documentObj.getString(FieldType.ACCOUNT_TYPE.getName()));
				acqMapping.setUserType(documentObj.getString(FieldType.USER_TYPE.getName()));
				acqMapping.setBankName(documentObj.getString(FieldType.ACQUIRER_NAME.getName()));

				String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");

				JSONObject adfField = new JSONObject();
				for (String entry : documentObj.keySet()) {
					if (entry.contains("ADF")) {
						adfField.put(entry, encryptDecryptService.decrypt(adminPayId, documentObj.getString(entry)));
					}
				}

				acqMapping.setAdfFields(adfField.toString());
			}

		} catch (Exception exception) {
			String message = "Error while fetching Payout Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

		return acqMapping;
	}

	public PayoutAcquireMapping fetchMerchantMappingByPayId(String payId) throws SystemException {

		PayoutAcquireMapping acqMapping = new PayoutAcquireMapping();
		try {

			BasicDBObject fetchQuery = new BasicDBObject();

			if (StringUtils.isNotBlank(payId)) {
				fetchQuery.put(FieldType.PAY_ID.getName(), payId);
			}

			fetchQuery.put(FieldType.STATUS.getName(), Active);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_MERCHANT_MAPPING.getValue()));
			MongoCursor<Document> cursor = collection.find(fetchQuery).iterator();

			if (cursor.hasNext()) {
				Document documentObj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				acqMapping.setPayId(documentObj.getString(FieldType.PAY_ID.getName()));
				acqMapping.setAccountType(documentObj.getString(FieldType.ACCOUNT_TYPE.getName()));
				acqMapping.setUserType(documentObj.getString(FieldType.USER_TYPE.getName()));
				acqMapping.setBankName(documentObj.getString(FieldType.ACQUIRER_NAME.getName()));

			}

			cursor.close();

		} catch (Exception exception) {
			String message = "Error while fetching Payout Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

		return acqMapping;
	}

	public void addVirtualAccountDetails(PayoutVpa payoutVpa) throws SystemException {
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_VIRTUAL_ACCOUNT_DETAILS.getValue()));

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			BasicDBObject newFieldsObj = new BasicDBObject();

			if (StringUtils.isNotBlank(payoutVpa.getBankName())) {
				newFieldsObj.put(FieldType.ACQUIRER_NAME.getName(), payoutVpa.getBankName());
			}

			if (StringUtils.isNotBlank(payoutVpa.getPayId())) {
				newFieldsObj.put(FieldType.PAY_ID.getName(), payoutVpa.getPayId());
			}
			if (StringUtils.isNotBlank(payoutVpa.getSubMerchantPayId())) {
				newFieldsObj.put(FieldType.SUB_MERCHANT_ID.getName(), payoutVpa.getSubMerchantPayId());
			}
			if (StringUtils.isNotBlank(payoutVpa.getVpa())) {
				newFieldsObj.put(FieldType.VPA.getName(), payoutVpa.getVpa());
			}
			if (StringUtils.isNotBlank(payoutVpa.getVan())) {
				newFieldsObj.put(FieldType.VIRTUAL_ACC_NUM.getName(), payoutVpa.getVan());
			}
			if (StringUtils.isNotBlank(payoutVpa.getVanIfsc())) {
				newFieldsObj.put(FieldType.IFSC_CODE.getName(), payoutVpa.getVanIfsc());
			}
			if (StringUtils.isNotBlank(payoutVpa.getVanBeneficiaryName())) {
				newFieldsObj.put(FieldType.VIRTUAL_BENEFICIARY_NAME.getName(), payoutVpa.getVanBeneficiaryName());
			}
			if (StringUtils.isNotBlank(payoutVpa.getSubWalletId())) {
				newFieldsObj.put(FieldType.SUB_WALLET_ID.getName(), payoutVpa.getSubWalletId());
			}
			if (StringUtils.isNotBlank(payoutVpa.getStatus())) {
				newFieldsObj.put(FieldType.STATUS.getName(), payoutVpa.getStatus());
			}
			if (StringUtils.isNotBlank(payoutVpa.getResponseMsg())) {
				newFieldsObj.put(FieldType.PG_RESPONSE_MSG.getName(), payoutVpa.getResponseMsg());
			}

			Document doc = new Document(newFieldsObj);
			doc.put(FieldType.CREATE_DATE.getName(), dateNow);
			doc.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));
			
			logger.info("Inserting acq mapping fields " + doc);
			collection.insertOne(doc);

		} catch (

		Exception exception) {
			String message = "Error while inserting acquirer Saving Payout Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

	}
	
	public PayoutAcquireMapping fetchSavedMappingByPayId(String payId, User user) throws SystemException {
		logger.info("inside fetchSavedMappingByPayId()");
		PayoutAcquireMapping acqMapping = new PayoutAcquireMapping();
		try {

			BasicDBObject fetchQuery = new BasicDBObject();
			
			if(user==null)
				user=userDao.findPayId(payId);
			
			if(StringUtils.isNotBlank(user.getSuperMerchantId())){
				fetchQuery.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				fetchQuery.put(FieldType.SUB_MERCHANT_ID.getName(), payId);
			}else{
				fetchQuery.put(FieldType.PAY_ID.getName(), payId);
			}

			fetchQuery.put(FieldType.STATUS.getName(), Active);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_MERCHANT_MAPPING.getValue()));
			MongoCursor<Document> cursor = collection.find(fetchQuery).iterator();

			if (cursor.hasNext()) {
				Document documentObj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				acqMapping.setPayId(documentObj.getString(FieldType.PAY_ID.getName()));
				acqMapping.setAccountType(documentObj.getString(FieldType.ACCOUNT_TYPE.getName()));
				acqMapping.setUserType(documentObj.getString(FieldType.USER_TYPE.getName()));
				acqMapping.setBankName(documentObj.getString(FieldType.ACQUIRER_NAME.getName()));
				acqMapping.setSubWalletId(documentObj.getString(FieldType.SUB_WALLET_ID.getName()));

				cursor.close();

				// fetch ACQ details
				BasicDBObject fetchQueryAcqMapping = new BasicDBObject();

				if (StringUtils.isNotBlank(acqMapping.getUserType())) {
					fetchQueryAcqMapping.put(FieldType.USER_TYPE.getName(), acqMapping.getUserType());
				}
				if (StringUtils.isNotBlank(acqMapping.getBankName())) {
					fetchQueryAcqMapping.put(FieldType.ACQUIRER_NAME.getName(), acqMapping.getBankName());
				}

				if (StringUtils.isNotBlank(acqMapping.getAccountType())) {
					fetchQueryAcqMapping.put(FieldType.ACCOUNT_TYPE.getName(), acqMapping.getAccountType());
				}

				MongoCollection<Document> coll = dbIns.getCollection(
						PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_ACQUIERE_MAPPING.getValue()));
				MongoCursor<Document> cursorAcq = coll.find(fetchQueryAcqMapping).iterator();

				String adminPayId = PropertiesManager.propertiesMap.get("SAVE_CARD_ADMIN_PAYID");

				if (cursorAcq.hasNext()) {
					Document docObj = cursorAcq.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						docObj = dataEncDecTool.decryptDocument(docObj);
					}

					acqMapping.setAccountType(docObj.getString(FieldType.ACCOUNT_TYPE.getName()));
					acqMapping.setBankName(docObj.getString(FieldType.ACQUIRER_NAME.getName()));
					acqMapping.setUserType(docObj.getString(FieldType.USER_TYPE.getName()));

					JSONObject adfField = new JSONObject();
					for (String entry : docObj.keySet()) {
						if (entry.contains("ADF")) {
							adfField.put(entry, encryptDecryptService.decrypt(adminPayId, docObj.getString(entry)));
						}
					}

					acqMapping.setAdfFields(adfField.toString());
				}
				cursorAcq.close();
			}
			
		} catch (Exception exception) {
			String message = "Error while fetching Payout Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

		return acqMapping;
	}

	public String findPayoutAcquirerNameByPayId(String payId, String subMerchantpayId) throws SystemException {
		
		
		try {

			BasicDBObject fetchQuery = new BasicDBObject();

			if (StringUtils.isNotBlank(payId) && !payId.equalsIgnoreCase("ALL")) {
				fetchQuery.put(FieldType.PAY_ID.getName(), payId);
			}
			if (StringUtils.isNotBlank(subMerchantpayId) && !payId.equalsIgnoreCase("ALL")) {
				fetchQuery.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantpayId);
			}
			
			if(!fetchQuery.containsField(FieldType.PAY_ID.getName())){
				return null;
			}
			if(StringUtils.isNotBlank(subMerchantpayId)){
				if(!fetchQuery.containsField(FieldType.SUB_MERCHANT_ID.getName())){
					return null;
				}
			}
			
			fetchQuery.put(FieldType.STATUS.getName(), "Active");
			
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_MERCHANT_MAPPING.getValue()));
			MongoCursor<Document> cursor = collection.find(fetchQuery).iterator();

			if (cursor.hasNext()) {
				Document documentObj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				return documentObj.getString(FieldType.ACQUIRER_NAME.getName());

			}

			cursor.close();

		} catch (Exception exception) {
			String message = "Error while fetching Payout Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
		return null;
	}

	public PayoutAcquireMapping findMappingwithVirtualAcNo(String virtualAccNo) throws SystemException {
		
		logger.info("inside findMappingwithVirtualAcNo()");
		PayoutAcquireMapping acqMapping = new PayoutAcquireMapping();
		try {

			BasicDBObject fetchQuery = new BasicDBObject();
			
			fetchQuery.put(FieldType.VIRTUAL_ACC_NUM.getName(), virtualAccNo);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_MERCHANT_MAPPING.getValue()));
			MongoCursor<Document> cursor = collection.find(fetchQuery).iterator();

			if (cursor.hasNext()) {
				Document documentObj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				acqMapping.setPayId(documentObj.getString(FieldType.PAY_ID.getName()));
				acqMapping.setAccountType(documentObj.getString(FieldType.ACCOUNT_TYPE.getName()));
				acqMapping.setUserType(documentObj.getString(FieldType.USER_TYPE.getName()));
				acqMapping.setBankName(documentObj.getString(FieldType.ACQUIRER_NAME.getName()));
				
				if(StringUtils.isNotBlank(documentObj.getString(FieldType.SUB_WALLET_ID.getName())))
					acqMapping.setSubWalletId(documentObj.getString(FieldType.SUB_WALLET_ID.getName()));

				
			}
			cursor.close();
			
		} catch (Exception exception) {
			String message = "Error while fetching Payout Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
		return acqMapping;
	}

	public PayoutAcquireMapping findMappingwithAcquirerNameAndPayId(String payId, String acquirerName) throws SystemException {
		PayoutAcquireMapping acqMapping = new PayoutAcquireMapping();
		try {

			BasicDBObject fetchQuery = new BasicDBObject();
			
			User user=userDao.findPayId(payId);
			
			if(StringUtils.isNotBlank(user.getSuperMerchantId())){
				fetchQuery.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				fetchQuery.put(FieldType.SUB_MERCHANT_ID.getName(), payId);
			}else{
				fetchQuery.put(FieldType.PAY_ID.getName(), payId);
			}

			fetchQuery.put(FieldType.ACQUIRER_NAME.getName(), acquirerName);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PAYOUT_MERCHANT_MAPPING.getValue()));
			MongoCursor<Document> cursor = collection.find(fetchQuery).iterator();

			if (cursor.hasNext()) {
				Document documentObj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					documentObj = dataEncDecTool.decryptDocument(documentObj);
				}

				acqMapping.setPayId(documentObj.getString(FieldType.PAY_ID.getName()));
				acqMapping.setAccountType(documentObj.getString(FieldType.ACCOUNT_TYPE.getName()));
				acqMapping.setUserType(documentObj.getString(FieldType.USER_TYPE.getName()));
				acqMapping.setBankName(documentObj.getString(FieldType.ACQUIRER_NAME.getName()));
				acqMapping.setVan(documentObj.getString(FieldType.VIRTUAL_ACC_NUM.getName()));
				if(StringUtils.isNotBlank(documentObj.getString(FieldType.SUB_WALLET_ID.getName())))
					acqMapping.setSubWalletId(documentObj.getString(FieldType.SUB_WALLET_ID.getName()));

			}
			cursor.close();
			
		} catch (Exception exception) {
			String message = "Error while fetching Payout Mapping in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}

		return acqMapping;
	}
}
