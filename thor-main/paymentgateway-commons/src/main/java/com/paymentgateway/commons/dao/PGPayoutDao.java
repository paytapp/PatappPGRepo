package com.paymentgateway.commons.dao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

@Service
public class PGPayoutDao {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(PGPayoutDao.class.getName());

	@SuppressWarnings("static-access")
	public List<ImpsDownloadObject> fetchVendorPayOutReportData(String merchantId, String subMerchantPayId,
			String orderId, String status, String reportDateFrom, String reportDateTo, User user) {
		List<ImpsDownloadObject> transactionList = new ArrayList<ImpsDownloadObject>();
		Map<String, User> userMap = new HashMap<String, User>();
		Set<String> orderIdd = new HashSet<String>();
		Set<String> payIdd = new HashSet<String>();
		boolean isParameterised = false;
		boolean isOrderId = false;

		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> statusConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();

			String currentDate = null;
			if (!reportDateFrom.isEmpty()) {
				if (!reportDateTo.isEmpty()) {
					currentDate = reportDateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(reportDateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));

				isParameterised = true;
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}

			if (!orderId.isEmpty()) {
				isOrderId = true;
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				String statusArr[] = status.split(",");
				for (String sts : statusArr) {
					statusConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), sts.trim()));
				}
				statusQuery.append("$or", statusConditionList);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateQuery.isEmpty()) {
					allConditionQueryList.add(dateQuery);
				}
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

			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside TxnReports , downloadSearchPayment , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.MERCHANT_PAYOUT_COLLECTION.getValue()));
			BasicDBObject match1 = new BasicDBObject("$match", finalquery);
			BasicDBObject sort1 = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match1, sort1);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			if (isOrderId) {
				while (cursor.hasNext()) {
					Document dbobj = cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						dbobj = dataEncDecTool.decryptDocument(dbobj);
					}

					ImpsDownloadObject impsReport = new ImpsDownloadObject();

					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

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
						impsReport.setMerchant(merchantUser.getBusinessName());
					} else {
						impsReport.setMerchant(userDao.getBusinessNameByPayId(merchantPayId));
					}

					if (((!merchantId.equalsIgnoreCase("All")) || (merchantId.equalsIgnoreCase("All")))
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
							impsReport.setSubMerchant(subMerchantUser.getBusinessName());
						} else {
							impsReport.setSubMerchant(CrmFieldConstants.NA.getValue());
						}
					} else {
						impsReport.setSubMerchant(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_ID.toString()))) {
						impsReport.setTxnId(dbobj.getString(FieldType.TXN_ID.toString()));
					} else {
						impsReport.setTxnId(CrmFieldConstants.NA.getValue());
					}
					impsReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
					impsReport.setDate(dbobj.getString(FieldType.CREATE_DATE.toString()));
					impsReport.setBankAccountName(dbobj.getString(FieldType.BENE_NAME.toString()));
					impsReport.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.toString()));
					impsReport.setBankIFSC(dbobj.getString(FieldType.IFSC_CODE.toString()));
					impsReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
					impsReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TXN_MESSAGE.toString()))) {
						impsReport.setResponseMsg(dbobj.getString(FieldType.PG_TXN_MESSAGE.toString()));
					} else {
						impsReport.setResponseMsg(CrmFieldConstants.NA.getValue());
					}
					impsReport.setPhoneNo(dbobj.getString(FieldType.PHONE_NO.toString()));
					transactionList.add(impsReport);
				}
				logger.info("transactionList created and size = " + transactionList.size());
				cursor.close();
				return transactionList;
			} else {

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
					if (!dateQuery.isEmpty()) {
						paramConditionLst1.add(dateQuery);
					}
					if (!statusQuery.isEmpty()) {
						paramConditionLst1.add(statusQuery);
					}
					if (!paramConditionLst1.isEmpty()) {
						finalquery2 = new BasicDBObject("$and", paramConditionLst1);
					}

					FindIterable<Document> iterDoc = coll.find(finalquery2);
					MongoCursor<Document> cursor1 = iterDoc.iterator();
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

							ImpsDownloadObject impsReport = new ImpsDownloadObject();
							Document dbobj = cursor3.next();
							
							if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
									&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
								dbobj = dataEncDecTool.decryptDocument(dbobj);
							} 
							
							if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
								if (!(status.equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName())))) {
									break;
								}
							}

							// For Merchant Business Name
							String merchantPayId = dbobj.getString(FieldType.PAY_ID.getName());
							User merchantUser = new User();

							if (userMap.get(merchantPayId) != null
									&& !userMap.get(merchantPayId).getPayId().isEmpty()) {
								merchantUser = userMap.get(merchantUser);
							} else {
								merchantUser = userDao.findPayId(merchantPayId);
								userMap.put(merchantPayId, merchantUser);
							}

							if (merchantUser != null) {
								impsReport.setMerchant(merchantUser.getBusinessName());
							} else {
								impsReport.setMerchant(userDao.getBusinessNameByPayId(merchantPayId));
							}

							if (((!merchantId.equalsIgnoreCase("All")) || (merchantId.equalsIgnoreCase("All"))
									|| (merchantId.equalsIgnoreCase("")))
									&& dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

								String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
								User subMerchantUser = new User();

								if (userMap.get(subMerchant) != null
										&& !userMap.get(subMerchant).getPayId().isEmpty()) {
									subMerchantUser = userMap.get(subMerchant);
								} else {
									subMerchantUser = userDao.findPayId(subMerchant);
									userMap.put(subMerchant, subMerchantUser);
								}
								if (subMerchantUser != null) {
									impsReport.setSubMerchant(subMerchantUser.getBusinessName());
								} else {
									impsReport.setSubMerchant(CrmFieldConstants.NA.getValue());
								}
							} else {
								impsReport.setSubMerchant(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_ID.toString()))) {
								impsReport.setTxnId(dbobj.getString(FieldType.TXN_ID.toString()));
							} else {
								impsReport.setTxnId(CrmFieldConstants.NA.getValue());
							}
							impsReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
							impsReport.setDate(dbobj.getString(FieldType.CREATE_DATE.toString()));
							impsReport.setBankAccountName(dbobj.getString(FieldType.BENE_NAME.toString()));
							impsReport.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.toString()));
							impsReport.setBankIFSC(dbobj.getString(FieldType.IFSC_CODE.toString()));
							impsReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
							impsReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TXN_MESSAGE.toString()))) {
								impsReport.setResponseMsg(dbobj.getString(FieldType.PG_TXN_MESSAGE.toString()));
							} else {
								impsReport.setResponseMsg(CrmFieldConstants.NA.getValue());
							}
							impsReport.setPhoneNo(dbobj.getString(FieldType.PHONE_NO.toString()));
							transactionList.add(impsReport);
						}
					}

				}

				logger.info("Total data in impsList is " + transactionList.size());
				return transactionList;
			}
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPayment , Exception = " , e);
			return transactionList;
		}
	}

	public ImpsDownloadObject getVendorTransactionWithTxnId(String txnId) {
		ImpsDownloadObject imps = new ImpsDownloadObject();

		try {
			boolean isParameterised = false;

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.MERCHANT_PAYOUT_COLLECTION.getValue()));

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
				imps.setStatus(dbobj.getString(FieldType.STATUS.getName()));
				imps.setResponseMsg(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
				imps.setPhoneNo((dbobj.getString(FieldType.PHONE_NO.getName())));
				imps.setUserType("Merchant Initiated Indirect");
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
					PropertiesManager.propertiesMap.get(prefix + Constants.MERCHANT_PAYOUT_COLLECTION.getValue()));

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

	public void insertResonseFieldsInDB(Map<String, String> reqmap) {
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			reqmap.put(FieldType.CREATE_DATE.getName(), dateNow);

			BasicDBObject newFieldsObj = new BasicDBObject();

			for (String columnName : reqmap.keySet()) {
				newFieldsObj.put(columnName, reqmap.get(columnName));
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.MERCHANT_PAYOUT_COLLECTION.getValue()));
			Document doc = new Document(newFieldsObj);
			collection.insertOne(doc);
		} catch (Exception exception) {
			String message = "Error while inserting CIB Response in database";
			logger.error(message, exception);
		}
	}

	public void updateIMPSTransactionStatus(Map<String, String> reqmap) throws SystemException {
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.MERCHANT_PAYOUT_COLLECTION.getValue()));
			Bson filter = new Document(FieldType.TXN_ID.getName(), reqmap.get(FieldType.TXN_ID.getName()))
					.append(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName());
			Bson newValue = new Document(FieldType.STATUS.getName(), reqmap.get(FieldType.STATUS.getName()))
					.append(FieldType.UPDATE_DATE.getName(), dateNow)
					.append(FieldType.RRN.getName(), reqmap.get(FieldType.RRN.getName()))
					.append(FieldType.PG_TXN_MESSAGE.getName(), reqmap.get(FieldType.PG_TXN_MESSAGE.getName()))
					.append(FieldType.PG_RESP_CODE.getName(), reqmap.get(FieldType.PG_RESP_CODE.getName()));

			if (reqmap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
				((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode())
						.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.name());
			} else if (reqmap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.DECLINED.name())) {
				((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.DECLINED.getCode())
						.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DECLINED.name());
			} else if (reqmap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.REJECTED.name())) {
				((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED.getCode())
						.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED.name());
			} else if (reqmap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.TIMEOUT.name())) {
				((Document) newValue).append(FieldType.RESPONSE_CODE.getName(), ErrorType.TIMEOUT.getCode())
						.append(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TIMEOUT.name());
			}

			Bson updateOperationDocument = new Document("$set", newValue);
			collection.updateOne(filter, updateOperationDocument);
		} catch (Exception exception) {
			String message = "Error while inserting IMPS Response in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

}
