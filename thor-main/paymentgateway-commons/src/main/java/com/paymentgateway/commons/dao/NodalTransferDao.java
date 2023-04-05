package com.paymentgateway.commons.dao;

import java.io.File;
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
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.CibNodalTransaction;
import com.paymentgateway.commons.user.CibNodalTransferBene;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Shiva
 *
 */

@Service
public class NodalTransferDao {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(NodalTransferDao.class.getName());

	public List<CibNodalTransferBene> fetchBeneficiaryData(String merchantId, String subMerchant, String payeeType,
			String status, String bankAccountNo) {
		List<CibNodalTransferBene> beneList = new ArrayList<>();
		Map<String, User> userMap = new HashMap<String, User>();
		try {

			DateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

			/*
			 * Date startDate=format.parse(dateFrom+" 00:00:00"); Date
			 * endDate=format.parse(dateTo+" 23:59:59");
			 */

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.NODAL_BENEFICIARY_COLLECTION_NAME.getValue()));

			BasicDBObject finalQuery = new BasicDBObject();
			/*
			 * finalQuery.put("CREATE_DATE", BasicDBObjectBuilder.start("$gte",
			 * sdfFormat.format(startDate)) .add("$lte",
			 * sdfFormat.format(endDate)).get());
			 */

			if (!merchantId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.PAY_ID.getName(), merchantId);
			}
			if (StringUtils.isNotBlank(subMerchant)) {
				finalQuery.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchant);
			}
			if (!status.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.STATUS.getName(), status);
			}
			if (!payeeType.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.BENE_PAYEE_TYPE.getName(), payeeType);
			}
			if (StringUtils.isNotBlank(bankAccountNo)) {
				finalQuery.put(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNo);
			}

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				CibNodalTransferBene beneDetails = new CibNodalTransferBene();
				Document dbobj = cursor.next();

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
					beneDetails.setMerchantName(merchantUser.getBusinessName());
				} else {
					beneDetails.setMerchantName(userDao.getBusinessNameByPayId(merchantPayId));
				}

				// For SubMerchant Business Name
				if (((!merchantId.equalsIgnoreCase("All")) || (merchantId.equalsIgnoreCase("All")))
						&& dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant1 = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant1) != null && !userMap.get(subMerchant1).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant1);
					} else {
						subMerchantUser = userDao.findPayId(subMerchant1);
						userMap.put(subMerchant1, subMerchantUser);
					}
					if (subMerchantUser != null) {
						beneDetails.setSubMerchantName(subMerchantUser.getBusinessName());
					} else {
						beneDetails.setSubMerchantName(CrmFieldConstants.NA.getValue());
					}
				} else {
					beneDetails.setSubMerchantName(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
					beneDetails.setPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
				} else {
					beneDetails.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_NAME.getName()))) {
					beneDetails.setBankAccountName(dbobj.getString(FieldType.BENE_NAME.getName()));
				} else {
					beneDetails.setBankAccountName(Constants.NA.name());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()))) {
					beneDetails.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()));
				} else {
					beneDetails.setBankAccountNumber(Constants.NA.name());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.IFSC.getName()))) {
					beneDetails.setBankIfsc(dbobj.getString(FieldType.IFSC.getName()));
				} else {
					beneDetails.setBankIfsc(Constants.NA.name());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CREATE_DATE.getName()))) {
					beneDetails.setCreateDate(
							format.format(sdfFormat.parse(dbobj.getString(FieldType.CREATE_DATE.getName()))));
				} else {
					beneDetails.setCreateDate("");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_ALIAS.getName()))) {
					beneDetails.setAlias(dbobj.getString(FieldType.BENE_ALIAS.getName()));
				} else {
					beneDetails.setAlias("");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.getName()))) {
					beneDetails.setStatus(dbobj.getString(FieldType.STATUS.getName()));
				} else {
					beneDetails.setStatus("");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESPONSE_MESSAGE.getName()))) {
					beneDetails.setResponseMsg(dbobj.getString(FieldType.RESPONSE_MESSAGE.getName()));
				} else {
					beneDetails.setResponseMsg("");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_PAYEE_TYPE.getName()))) {
					beneDetails.setPayeeType(dbobj.getString(FieldType.BENE_PAYEE_TYPE.getName()));
				} else {
					beneDetails.setPayeeType("");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.BENE_DEFAULT.getName()))) {
					beneDetails.setDefaultBene(Boolean.valueOf(dbobj.getString(FieldType.BENE_DEFAULT.getName())));
				}

				beneList.add(beneDetails);
			}
			return beneList;

		} catch (Exception e) {
			logger.error("Exception in fetchBeneficiaryData() ", e);

		}
		return beneList;

	}

	public boolean isBeneExist(String merchantId, String subMerchantId) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.NODAL_BENEFICIARY_COLLECTION_NAME.getValue()));

			BasicDBObject finalQuery = new BasicDBObject();

			if (StringUtils.isNotBlank(merchantId)) {
				finalQuery.put(FieldType.PAY_ID.getName(), merchantId);
			}
			if (StringUtils.isNotBlank(subMerchantId)) {
				finalQuery.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				return true;
			}

		} catch (Exception e) {
			logger.error("Exception in isBeneExist()", e);

		}
		return false;

	}

	public CibNodalTransferBene getDefaultBene(String merchantId, String subMerchantId) {
		CibNodalTransferBene beneDetails = new CibNodalTransferBene();
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.NODAL_BENEFICIARY_COLLECTION_NAME.getValue()));

			BasicDBObject finalQuery = new BasicDBObject();

			if (StringUtils.isNotBlank(subMerchantId)) {
				finalQuery.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			} else {
				finalQuery.put(FieldType.PAY_ID.getName(), merchantId);
			}

			finalQuery.put(FieldType.BENE_DEFAULT.getName(), "true");
			// finalQuery.put(FieldType.STATUS.getName(),
			// ErrorType.SUCCESS.name());

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {

				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				beneDetails.setBankAccountName(dbobj.getString(FieldType.BENE_NAME.getName()));
				beneDetails.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()));
				beneDetails.setBankIfsc(dbobj.getString(FieldType.IFSC.getName()));
				beneDetails.setCreateDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				beneDetails.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
				beneDetails.setAlias(dbobj.getString(FieldType.BENE_ALIAS.getName()));
				beneDetails.setStatus(dbobj.getString(FieldType.STATUS.getName()));
				beneDetails.setResponseMsg(dbobj.getString(FieldType.RESPONSE_MESSAGE.getName()));
				beneDetails.setPayeeType(dbobj.getString(FieldType.BENE_PAYEE_TYPE.getName()));
				beneDetails.setDefaultBene(Boolean.valueOf(dbobj.getString(FieldType.BENE_DEFAULT.getName())));

			}
			return beneDetails;

		} catch (Exception e) {
			logger.error("Exception in getDefaultBene() ", e);

		}
		return beneDetails;

	}

	public void changeDefaultBene(String merchantId, String subMerchant, String accountNo) {
		logger.info("inside changeDefaultBene()");
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.NODAL_BENEFICIARY_COLLECTION_NAME.getValue()));
			Bson filter;

			// Update old default beneficiary
			if (StringUtils.isNotBlank(subMerchant)) {
				filter = new Document(FieldType.SUB_MERCHANT_ID.getName(), subMerchant)
						.append(FieldType.BENE_DEFAULT.getName(), "true");
			} else {
				filter = new Document(FieldType.PAY_ID.getName(), merchantId).append(FieldType.BENE_DEFAULT.getName(),
						"true");
			}
			

			Bson newValue = new Document(FieldType.BENE_DEFAULT.getName(), "false");

			Bson updateOperationDocument = new Document("$set", newValue);
			logger.info("Filter Query "+filter+" update Query "+newValue+" total record found "+coll.count(filter));
			coll.updateOne(filter, updateOperationDocument);
			
			logger.info("field Updated");

			Bson filter2;
			// Set new default beneficiary
			if (StringUtils.isNotBlank(subMerchant)) {
				filter2 = new Document(FieldType.SUB_MERCHANT_ID.getName(), subMerchant)
						.append(FieldType.BENE_ACCOUNT_NO.getName(), accountNo)
						.append(FieldType.STATUS.getName(), "SUCCESS");
			} else {
				filter2 = new Document(FieldType.PAY_ID.getName(), merchantId)
						.append(FieldType.BENE_ACCOUNT_NO.getName(), accountNo)
						.append(FieldType.STATUS.getName(), "SUCCESS");
			}

			Bson newValue2 = new Document(FieldType.BENE_DEFAULT.getName(), "true");

			Bson updateOperationDocument2 = new Document("$set", newValue2);
			logger.info("Filter Query "+filter2+" update Query "+newValue2+" total record found "+coll.count(filter2));
			coll.updateOne(filter2, updateOperationDocument2);

		} catch (Exception e) {
			logger.error("Exception in changeDefaultBene() ", e);

		}

	}

	public List<CibNodalTransaction> fetchTransactionData(String dateFrom, String dateTo, String merchantId,
			String SubMerchantId, String status, String txnId, String utrNo) {
		List<CibNodalTransaction> tranList = new ArrayList<>();
		Set<String> pgRefNo = new HashSet<String>();
		Map<String, User> userMap = new HashMap<String, User>();
		try {

			DateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

			Date startDate = format.parse(dateFrom + " 00:00:00");
			Date endDate = format.parse(dateTo + " 23:59:59");

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.NODAL_SETTLEMENT_COLLECTION_NAME.getValue()));

			BasicDBObject finalQuery = new BasicDBObject();
			finalQuery.put(FieldType.CREATE_DATE.getName(), BasicDBObjectBuilder.start("$gte", sdfFormat.format(startDate))
					.add("$lte", sdfFormat.format(endDate)).get());

			finalQuery.put(FieldType.TRANSACTION_OF.getName(), FieldType.CIB_TRANSACTION.getName());

			if (!merchantId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.PAY_ID.getName(), merchantId);
			}
			if (StringUtils.isNotBlank(SubMerchantId) && !SubMerchantId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.SUB_MERCHANT_ID.getName(), SubMerchantId);
			}
			if (!status.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.STATUS.getName(), status);
			}
			if (StringUtils.isNotBlank(txnId)) {
				finalQuery.remove(FieldType.CREATE_DATE.getName());
				finalQuery.put(FieldType.TXN_ID.getName(), txnId);
			}
			if (StringUtils.isNotBlank(utrNo)) {
				finalQuery.remove(FieldType.CREATE_DATE.getName());
				finalQuery.put(FieldType.UTR_NO.getName(), utrNo);
			}

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				pgRefNo.add(dbobj.getString(FieldType.PG_REF_NUM.getName()));
			}
			for (String refNo : pgRefNo) {
				BasicDBObject finalquery2 = new BasicDBObject(FieldType.PG_REF_NUM.getName(), refNo);
				finalquery2.put(FieldType.TRANSACTION_OF.getName(), FieldType.CIB_TRANSACTION.getName());
				BasicDBObject match = new BasicDBObject("$match", finalquery2);
				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.name(), -1));
				BasicDBObject limit = new BasicDBObject("$limit", 1);
				List<BasicDBObject> pipeline2 = Arrays.asList(match, sort, limit);
				AggregateIterable<Document> output2 = coll.aggregate(pipeline2);
				output2.allowDiskUse(true);
				MongoCursor<Document> cursor2 = output2.iterator();

				while (cursor2.hasNext()) {
					CibNodalTransaction tranDetails = new CibNodalTransaction();
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
						tranDetails.setMerchantName(merchantUser.getBusinessName());
					} else {
						tranDetails.setMerchantName(userDao.getBusinessNameByPayId(merchantPayId));
					}

					// For Sub Merchant Business Name

					if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
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
							tranDetails.setSubMerchantName(subMerchantUser.getBusinessName());
						} else {
							tranDetails.setSubMerchantName(CrmFieldConstants.NA.getValue());
						}
					} else {
						tranDetails.setSubMerchantName(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						tranDetails.setPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
					} else {
						tranDetails.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
					}
					tranDetails.setPgRefNo(dbobj.getString(FieldType.PG_REF_NUM.getName()));
					tranDetails.setTxnId(dbobj.getString(FieldType.TXN_ID.getName()));
					tranDetails.setCapturedDateFrom(dbobj.getString(FieldType.CAPTURED_DATE_FROM.getName()));
					tranDetails.setCapturedDateTo(dbobj.getString(FieldType.CAPTURED_DATE_TO.getName()));
					tranDetails.setBankAccountName(dbobj.getString(FieldType.PAYEE_NAME.getName()));
					tranDetails.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()));
					tranDetails.setBankIfsc(dbobj.getString(FieldType.IFSC.getName()));
					tranDetails.setCreateDate(
							format.format(sdfFormat.parse(dbobj.getString(FieldType.CREATE_DATE.getName()))));
					tranDetails.setCurrency(
							Currency.getAlphabaticCode(dbobj.getString(FieldType.CURRENCY_CODE.getName())));
					tranDetails.setStatus(dbobj.getString(FieldType.STATUS.getName()));
					tranDetails.setTxnType(dbobj.getString(FieldType.TXNTYPE.getName()));
					tranDetails.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
					tranDetails.setResponseMsg(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
					tranDetails.setRemarks(dbobj.getString(FieldType.REMARKS.getName()));
					tranDetails.setUtrNo(dbobj.getString(FieldType.UTR_NO.getName()));

					tranList.add(tranDetails);
				}
			}
			return tranList;

		} catch (Exception e) {
			logger.error("Exception in fetchTransactionData() ", e);

		}
		return tranList;

	}

	public List<CibNodalTransaction> fetchTransactionDataforpayemntadvice(String dateFrom, String dateTo,
			String merchantId, String SubMerchantId, String status, String txnId, String utrNo) {
		List<CibNodalTransaction> tranList = new ArrayList<>();
		Set<String> pgRefNo = new HashSet<String>();
		Map<String, User> userMap = new HashMap<String, User>();
		try {

			DateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

			Date startDate = format.parse(dateFrom + " 00:00:00");
			Date endDate = format.parse(dateTo + " 23:59:59");

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.NODAL_SETTLEMENT_COLLECTION_NAME.getValue()));

			BasicDBObject finalQuery = new BasicDBObject();
			finalQuery.put("CREATE_DATE", BasicDBObjectBuilder.start("$gte", sdfFormat.format(startDate))
					.add("$lte", sdfFormat.format(endDate)).get());

			finalQuery.put(FieldType.TRANSACTION_OF.getName(), FieldType.CIB_TRANSACTION.getName());

			if (!merchantId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.PAY_ID.getName(), merchantId);
			}
			if (StringUtils.isNotBlank(SubMerchantId)) {
				finalQuery.put(FieldType.SUB_MERCHANT_ID.getName(), SubMerchantId);
			}
			finalQuery.put(FieldType.PG_TXN_STATUS.getName(), "SUCCESS");
			finalQuery.put(FieldType.STATUS.getName(), "Captured");

			if (StringUtils.isNotBlank(txnId)) {
				finalQuery.put(FieldType.TXN_ID.getName(), txnId);
			}
			if (StringUtils.isNotBlank(utrNo)) {
				finalQuery.put(FieldType.UTR_NO.getName(), utrNo);
			}

			logger.info("final query in nodel settlement " + finalQuery);

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				pgRefNo.add(dbobj.getString(FieldType.PG_REF_NUM.getName()));
			}
			for (String refNo : pgRefNo) {
				BasicDBObject finalquery2 = new BasicDBObject(FieldType.PG_REF_NUM.getName(), refNo);
				finalquery2.put(FieldType.TRANSACTION_OF.getName(), FieldType.CIB_TRANSACTION.getName());
				BasicDBObject match = new BasicDBObject("$match", finalquery2);
				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.name(), -1));
				BasicDBObject limit = new BasicDBObject("$limit", 1);
				List<BasicDBObject> pipeline2 = Arrays.asList(match, sort, limit);
				AggregateIterable<Document> output2 = coll.aggregate(pipeline2);
				output2.allowDiskUse(true);
				MongoCursor<Document> cursor2 = output2.iterator();

				while (cursor2.hasNext()) {
					CibNodalTransaction tranDetails = new CibNodalTransaction();
					Document dbobj = cursor2.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						dbobj = dataEncDecTool.decryptDocument(dbobj);
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
							tranDetails.setSubMerchantName(subMerchantUser.getBusinessName());
						} else {
							tranDetails.setSubMerchantName(CrmFieldConstants.NA.getValue());
						}
					}
					if (StringUtils.isNotBlank(SubMerchantId)) {
						tranDetails.setPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
					} else {
						tranDetails.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
					}
					tranDetails.setPgRefNo(dbobj.getString(FieldType.PG_REF_NUM.getName()));
					tranDetails.setTxnId(dbobj.getString(FieldType.TXN_ID.getName()));
					tranDetails.setCapturedDateFrom(dbobj.getString(FieldType.CAPTURED_DATE_FROM.getName()));
					tranDetails.setCapturedDateTo(dbobj.getString(FieldType.CAPTURED_DATE_TO.getName()));
					tranDetails.setBankAccountName(dbobj.getString(FieldType.PAYEE_NAME.getName()));
					tranDetails.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()));
					tranDetails.setBankIfsc(dbobj.getString(FieldType.IFSC.getName()));
					tranDetails.setCreateDate(
							format.format(sdfFormat.parse(dbobj.getString(FieldType.CREATE_DATE.getName()))));
					tranDetails.setCurrency(
							Currency.getAlphabaticCode(dbobj.getString(FieldType.CURRENCY_CODE.getName())));
					tranDetails.setStatus(dbobj.getString(FieldType.STATUS.getName()));
					tranDetails.setTxnType(dbobj.getString(FieldType.TXNTYPE.getName()));
					tranDetails.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
					tranDetails.setResponseMsg(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
					tranDetails.setRemarks(dbobj.getString(FieldType.REMARKS.getName()));
					tranDetails.setUtrNo(dbobj.getString(FieldType.UTR_NO.getName()));
					tranList.add(tranDetails);
				}
			}
			return tranList;

		} catch (Exception e) {
			logger.error("Exception in fetchTransactionDataforpayemntadvice() ", e);

		}
		return tranList;

	}

	public List<CibNodalTransaction> fetchTransferModeandAccountByUtr(String utr) {
		List<CibNodalTransaction> tranList = new ArrayList<>();
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.NODAL_SETTLEMENT_COLLECTION_NAME.getValue()));
			BasicDBObject finalQuery = new BasicDBObject();
			if (StringUtils.isNotBlank(utr)) {
				finalQuery.put(FieldType.UTR_NO.getName(), utr);
			}
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				CibNodalTransaction tranDetails = new CibNodalTransaction();
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				tranDetails.setTxnType(dbobj.getString(FieldType.TXNTYPE.getName()));
				tranDetails.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()));
				tranList.add(tranDetails);
			}

		} catch (Exception e) {
			logger.error("Exception in fetchTransferModeandAccountByUtr() ", e);
		}
		return tranList;
	}

	public void deleteFileStatus(String dateTo, String dateFrom, String FileName) {
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.CIB_ACCOUNT_STATEMENT_STATUS_COLLECTION.getValue()));

			BasicDBObject query = new BasicDBObject().append(FieldType.DATE_TO.getName(), dateTo).append("FILENAME",
					FileName);

			coll.deleteOne(query);

		} catch (Exception ex) {
			logger.error("Exception in update delete status in deleteFileStatus for CIB", ex);
		}
	}

	public void insertFileStatus(String fromdate, String todate, String fileName, String fileLocation,
			String fileDownloadFor, String userType, String fileType) {
		try {

			SimpleDateFormat sdfcurrdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			Date currDate = new Date();

			String currentdate = sdfcurrdate.format(currDate);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.CIB_ACCOUNT_STATEMENT_STATUS_COLLECTION.getValue()));
			Document doc = new Document();

			doc.put(FieldType.DATE_FROM.getName(), fromdate);
			doc.put(FieldType.DATE_TO.getName(), todate);
			doc.put(FieldType.CREATE_DATE.getName(), currentdate);
			doc.put("LOCATION", fileLocation);
			doc.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
			doc.put("FILENAME", fileName);
			doc.put("FILE_FOR", fileDownloadFor);
			doc.put(FieldType.USER_TYPE.getName(), userType);
			doc.put(FieldType.FILE_TYPE.getName(), fileType);

			coll.insertOne(doc);

		} catch (Exception e) {
			logger.error("Exception while inserting the data in insertFileStatus ", e);
		}

	}

	public void updateFileStatus(String fromdate, String todate, String fileName, String fileLocation,
			String fileDownloadFor, String userType, String fileType) {
		try {
			logger.info("Inside updateFileStatus()");

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.CIB_ACCOUNT_STATEMENT_STATUS_COLLECTION.getValue()));
			Document doc = new Document();

			doc.put(FieldType.DATE_FROM.getName(), fromdate);
			doc.put(FieldType.DATE_TO.getName(), todate);
			doc.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
			doc.put("FILENAME", fileName);
			// doc.put(FieldType.USER_TYPE.getName(), userType);
			// doc.put(FieldType.FILE_TYPE.getName(), fileType);

			logger.info("Inside Update Account Statement Status For Ready, Filter Query " + doc.toString());

			Bson filter = doc;

			Document newDoc = new Document(
					new BasicDBObject("$set", new BasicDBObject(FieldType.STATUS.getName(), "Ready")));

			coll.updateOne(filter, newDoc);

		} catch (Exception e) {
			logger.error("Exception while inserting the data in insertFileStatus ", e);
		}

	}

	public List<CibNodalTransaction> fetchAccountStatementData(String dateTo, String dateFrom, String fileFor,
			String fileName, String userType, String fileType) {
		List<CibNodalTransaction> dataList = new ArrayList<CibNodalTransaction>();
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.CIB_ACCOUNT_STATEMENT_STATUS_COLLECTION.getValue()));
			BasicDBObject finalQuery = new BasicDBObject();

			DateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

			Date startDate = format.parse(dateFrom + " 00:00:00");
			Date endDate = format.parse(dateTo + " 23:59:59");

			finalQuery.put("CREATE_DATE", BasicDBObjectBuilder.start("$gte", sdfFormat.format(startDate))
					.add("$lte", sdfFormat.format(endDate)).get());

			if (StringUtils.isNotBlank(fileFor) && !fileFor.equalsIgnoreCase("ALL")) {
				finalQuery.put("FILE_FOR", fileFor);
			}
			if (StringUtils.isNotBlank(fileName) && !fileName.equalsIgnoreCase("ALL")) {
				finalQuery.put("FILENAME", fileName);
			}

			if (StringUtils.isNotBlank(userType) && !userType.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.USER_TYPE.getName(), userType);
			}
			if (StringUtils.isNotBlank(fileType) && !userType.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.FILE_TYPE.getName(), fileType);
			}

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			File[] files = new File("/home/Properties/cibAccountStatement/").listFiles();

			while (cursor.hasNext()) {
				CibNodalTransaction data = new CibNodalTransaction();
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				data.setCreateDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				data.setDateTo(dbobj.getString(FieldType.DATE_TO.getName()));
				data.setDateFrom(dbobj.getString(FieldType.DATE_FROM.getName()));
				data.setFileName(dbobj.getString("FILENAME"));
				data.setFileFor(dbobj.getString("FILE_FOR"));
				data.setStatus(dbobj.getString(FieldType.STATUS.getName()));

				if (data.getStatus().equalsIgnoreCase("Ready")) {
					for (File file : files) {
						if (file.getName().equalsIgnoreCase(data.getFileName())) {
							dataList.add(data);
						}
					}
				} else {
					dataList.add(data);
				}

			}

		} catch (Exception e) {
			logger.error("Exception in fetchAccountStatementData() ", e);
		}
		return dataList;
	}

	public boolean getFileStatus(String dateTo, String dateFrom, String fileName) {

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.CIB_ACCOUNT_STATEMENT_STATUS_COLLECTION.getValue()));
			BasicDBObject finalQuery = new BasicDBObject();
			if (StringUtils.isNotBlank(dateTo)) {
				finalQuery.put(FieldType.DATE_TO.getName(), dateTo);
			}
			if (StringUtils.isNotBlank(dateFrom)) {
				finalQuery.put(FieldType.DATE_FROM.getName(), dateFrom);
			}
			if (StringUtils.isNotBlank(fileName)) {
				finalQuery.put("FILENAME", fileName);
			}

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				return true;
			}
		} catch (Exception e) {
			logger.error("Exception in getFileStatus() ", e);
		}
		return false;
	}

	public void updateFailedFileStatus(String dateTo, String dateFrom, String fileName, String userType,
			String fileType) {
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.CIB_ACCOUNT_STATEMENT_STATUS_COLLECTION.getValue()));
			Document doc = new Document();

			doc.put(FieldType.DATE_FROM.getName(), dateFrom);
			doc.put(FieldType.DATE_TO.getName(), dateTo);
			doc.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
			doc.put("FILENAME", fileName);
			// doc.put(FieldType.USER_TYPE.getName(),userType);
			// doc.put(FieldType.FILE_TYPE.getName(),fileType);
			logger.info("Update Failed Account Statement Status , Query " + doc.toString());

			Bson filter = doc;

			Document newDoc = new Document(new BasicDBObject("$set",
					new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName())));

			coll.updateOne(filter, newDoc);

		} catch (Exception e) {
			logger.error("Exception while inserting the data in insertFileStatus ", e);
		}

	}

	public String checkFileStatus(String dateTo, String dateFrom, String fileFor, String fileName, String userType,
			String fileType) {

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.CIB_ACCOUNT_STATEMENT_STATUS_COLLECTION.getValue()));
			BasicDBObject finalQuery = new BasicDBObject();

			DateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

			Date startDate = format.parse(dateFrom + " 00:00:00");
			Date endDate = format.parse(dateTo + " 23:59:59");

			finalQuery.put(FieldType.DATE_FROM.getName(), dateFrom);
			finalQuery.put(FieldType.DATE_TO.getName(), dateTo);

			if (StringUtils.isNotBlank(fileFor)) {
				finalQuery.put("FILE_FOR", fileFor);
			}
			if (StringUtils.isNotBlank(fileName)) {
				finalQuery.put("FILENAME", fileName);
			}

			if (StringUtils.isNotBlank(userType)) {
				finalQuery.put(FieldType.USER_TYPE.getName(), userType);
			}

			if (StringUtils.isNotBlank(fileType)) {
				finalQuery.put(FieldType.FILE_TYPE.getName(), fileType);
			}

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				return dbobj.getString(FieldType.STATUS.getName());
			}

		} catch (Exception e) {
			logger.error("Exception in fetchAccountStatementData() ", e);
		}
		return StatusType.FAILED.getName();
	}

	public List<CibNodalTransaction> fetchNodalTopUpTransactionReport(String merchantId, String SubMerchantId,
			String paymentType, String dateFrom, String dateTo, String txnType, String rrnSearch) {
		List<CibNodalTransaction> tranList = new ArrayList<>();
		Map<String, User> userMap = new HashMap<String, User>();
		try {

			DateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

			Date startDate = format.parse(dateFrom + " 00:00:00");
			Date endDate = format.parse(dateTo + " 23:59:59");

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.NODAL_TOPUP_TRANSACTON.getValue()));

			BasicDBObject finalQuery = new BasicDBObject();
			finalQuery.put("CREATE_DATE", BasicDBObjectBuilder.start("$gte", sdfFormat.format(startDate))
					.add("$lte", sdfFormat.format(endDate)).get());

			if (!merchantId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.PAY_ID.getName(), merchantId);
			}
			if (StringUtils.isNotBlank(SubMerchantId)) {
				finalQuery.put(FieldType.SUB_MERCHANT_ID.getName(), SubMerchantId);
			}

			if (StringUtils.isNotBlank(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				List<BasicDBObject> paymentTypeQueryList = new ArrayList<BasicDBObject>();
				paymentTypeQueryList.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
				paymentTypeQueryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), paymentType));
				finalQuery.put("$or", paymentTypeQueryList);
			}

			if (StringUtils.isNotBlank(txnType) && txnType.equalsIgnoreCase("payIn")) {

				List<BasicDBObject> payInQueryList = new ArrayList<BasicDBObject>();
				BasicDBObject payInQuery = new BasicDBObject();
				payInQuery.append(FieldType.TRANSACTION_OF.getName(), new BasicDBObject("$exists", false));
				payInQueryList.add(payInQuery);
				finalQuery.put("$and", payInQueryList);

			} else if (StringUtils.isNotBlank(txnType) && txnType.equalsIgnoreCase("payOut")) {

				List<BasicDBObject> payOutQueryList = new ArrayList<BasicDBObject>();
				BasicDBObject payOutQuery = new BasicDBObject();
				payOutQuery.append(FieldType.TRANSACTION_OF.getName(), new BasicDBObject("$exists", true));
				payOutQueryList.add(payOutQuery);
				finalQuery.put("$and", payOutQueryList);
			}

			if (StringUtils.isNotBlank(rrnSearch)) {
				finalQuery.clear();
				List<BasicDBObject> rrnQueryList = new ArrayList<BasicDBObject>();
				rrnQueryList.add(new BasicDBObject(FieldType.UTR_NO.getName(), rrnSearch));
				rrnQueryList.add(new BasicDBObject(FieldType.RRN.getName(), rrnSearch));
				finalQuery.put("$or", rrnQueryList);
			}
//			finalQuery.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			logger.info("final query in fetchNodalTopUpTransactionReport " + finalQuery);

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				CibNodalTransaction tranDetails = new CibNodalTransaction();
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				User merchant = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						merchant = userMap.get(payid);
					} else {
						merchant = userDao.findPayId(payid);
						userMap.put(payid, merchant);
					}
				}
				if (merchant != null) {
					tranDetails.setMerchantName(merchant.getBusinessName());
				}
				User subMerchantUser = null;
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userDao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
				}
				if (subMerchantUser != null) {
					tranDetails.setSubMerchantName(subMerchantUser.getBusinessName());
				} else {
					tranDetails.setSubMerchantName(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.getName()))) {
					tranDetails.setPaymentType(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_OF.getName()))) {
					tranDetails.setPaymentType(dbobj.getString(FieldType.TXNTYPE.getName()));
				} else {
					tranDetails.setPaymentType(CrmFieldConstants.NA.getValue());
				}
				tranDetails.setStatus(dbobj.getString(FieldType.STATUS.getName()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.MOP_TYPE.toString()))) {
					tranDetails.setMopType(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));
				} else {
					tranDetails.setMopType(CrmFieldConstants.NA.getValue());
				}
				tranDetails.setCreateDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				tranDetails.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
				tranList.add(tranDetails);
			}
			return tranList;

		} catch (Exception e) {
			logger.error("Exception in fetchTransactionDataforpayemntadvice() ", e);

		}
		return tranList;

	}

	public Map<String, String> fetchNodalPayoutBalance(String payId) {

		Map<String, String> balanceTopup = new HashMap<String, String>();
		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			BasicDBObject query = new BasicDBObject(FieldType.PAY_ID.getName(), payId);

			MongoCollection<Document> collBalance = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.NODAL_TOPUP_BALANCE.getValue()));
			MongoCursor<Document> cursor = collBalance.find(query).iterator();

			if (cursor.hasNext()) {
				Document documentObj = cursor.next();
				BigDecimal dbTotalBalance = new BigDecimal(documentObj.getString(FieldType.TOTAL_BALANCE.getName()));
				BigDecimal dbAvailableBalance = new BigDecimal(
						documentObj.getString(FieldType.AVAILABLE_BALANCE.getName()));
				balanceTopup.put(FieldType.TOTAL_BALANCE.getName(), String.valueOf(dbTotalBalance));
				balanceTopup.put(FieldType.AVAILABLE_BALANCE.getName(), String.valueOf(dbAvailableBalance));
			} else {
				balanceTopup.put(FieldType.TOTAL_BALANCE.getName(), "0.00");
				balanceTopup.put(FieldType.AVAILABLE_BALANCE.getName(), "0.00");
			}
		} catch (Exception e) {
			logger.error("exception while fetching balance of Nodal topup ", e);
		}
		return balanceTopup;
	}
}
