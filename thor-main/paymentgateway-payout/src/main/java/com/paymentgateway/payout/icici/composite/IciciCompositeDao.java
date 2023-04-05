package com.paymentgateway.payout.icici.composite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;

/**
 * @author Shiva
 *
 */

@Service
public class IciciCompositeDao {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private Fields field;

	@Autowired
	private IciciResponseHandler iciciResponseHandler;
	
	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(IciciCompositeDao.class.getName());

	public boolean isDuplicateBene(Fields fields) {
		logger.info("Inside checkDuplicateBene()");

		try {

			boolean flag = false;
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			BasicDBObject conditionQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.COMPOSITE_BENE_COLLECTION.getValue()));
			User user = new User();
			if (StringUtils.isNotBlank(fields.get(FieldType.PAY_ID.getName()))) {
				user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
							fields.get(FieldType.PAY_ID.getName())));
				} else {
					paramConditionLst
							.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
				}
			}
			
			if(StringUtils.isNotBlank(fields.get(FieldType.PAY_ID.getName())) && iciciResponseHandler.isPaybleMerchant(fields)){
				conditionList.add(new BasicDBObject(FieldType.IS_PAYBLE_MERCHANT.getName(),Constants.Y_FLAG.getValue()));
			}else{
				conditionList.add(new BasicDBObject(FieldType.IS_PAYBLE_MERCHANT.getName(),Constants.N_FLAG.getValue()));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.BENE_ACCOUNT_NO.getName()))) {
				conditionList.add(new BasicDBObject(FieldType.BENE_ACCOUNT_NO.getName(),
						fields.get(FieldType.BENE_ACCOUNT_NO.getName())));
				conditionList.add(
						new BasicDBObject(FieldType.IFSC_CODE.getName(), fields.get(FieldType.IFSC_CODE.getName())));
				conditionList.add(new BasicDBObject(FieldType.STATUS.getName(), "SUCCESS"));
			} else {
				conditionList.add(new BasicDBObject(FieldType.PAYER_ADDRESS.getName(),
						fields.get(FieldType.PAYER_ADDRESS.getName())));
				conditionList.add(new BasicDBObject(FieldType.STATUS.getName(), "SUCCESS"));
			}

			if (!conditionList.isEmpty()) {
				conditionQuery = new BasicDBObject("$and", conditionList);
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!conditionQuery.isEmpty()) {
				fianlList.add(conditionQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			MongoCursor<Document> cursor = coll.find(finalquery).iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				String status = dbobj.getString(FieldType.BENE_REGISTRATION.getName());
				if (dbobj.getString(FieldType.STATUS.getName()).equalsIgnoreCase("SUCCESS")) {
					flag = true;
					if (!status.equalsIgnoreCase("Active")) {
						Bson filter;
						if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
							if (fields.contains(FieldType.BENE_ACCOUNT_NO.getName())) {
								filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
										fields.get(FieldType.PAY_ID.getName()))
												.append(FieldType.BENE_ACCOUNT_NO.getName(),
														fields.get(FieldType.BENE_ACCOUNT_NO.getName()))
												.append(FieldType.IFSC_CODE.getName(),
														fields.get(FieldType.IFSC_CODE.getName()));
							} else {
								filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
										fields.get(FieldType.PAY_ID.getName())).append(
												FieldType.PAYER_ADDRESS.getName(),
												fields.get(FieldType.PAYER_ADDRESS.getName()));
							}
						} else {
							if (fields.contains(FieldType.BENE_ACCOUNT_NO.getName())) {
								filter = new Document(FieldType.PAY_ID.getName(),
										fields.get(FieldType.PAY_ID.getName()))
												.append(FieldType.BENE_ACCOUNT_NO.getName(),
														fields.get(FieldType.BENE_ACCOUNT_NO.getName()))
												.append(FieldType.IFSC_CODE.getName(),
														fields.get(FieldType.IFSC_CODE.getName()));
							} else {
								filter = new Document(FieldType.PAY_ID.getName(),
										fields.get(FieldType.PAY_ID.getName())).append(
												FieldType.PAYER_ADDRESS.getName(),
												fields.get(FieldType.PAYER_ADDRESS.getName()));
							}
						}

						Bson newValue;
						if (fields.contains(FieldType.RESELLER_ID.getName())) {
							newValue = new Document(FieldType.BENE_REGISTRATION.getName(), "Active").append(
									FieldType.RESELLER_ID.getName(), fields.get(FieldType.RESELLER_ID.getName()));
						} else {
							newValue = new Document(FieldType.BENE_REGISTRATION.getName(), "Active");
						}
						Bson updateOperationDocument = new Document("$set", newValue);
						coll.updateOne(filter, updateOperationDocument);
					}
					return true;
				}
			}

			cursor.close();

			MongoCursor<Document> cursor1 = coll.find(conditionQuery).iterator();

			if (!flag) {
				while (cursor1.hasNext()) {
					Document dbobj1 = cursor1.next();
					String status = dbobj1.getString(FieldType.BENE_REGISTRATION.getName());
					flag = true;
					if (dbobj1.getString(FieldType.STATUS.getName()).equalsIgnoreCase("SUCCESS")) {
						Document doc = new Document();

						if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
							doc.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
							doc.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						} else {
							doc.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						}

						if (StringUtils.isNotBlank(dbobj1.getString(FieldType.BENE_ACCOUNT_NO.getName()))) {
							doc.put(FieldType.BENE_ACCOUNT_NO.getName(),
									dbobj1.getString(FieldType.BENE_ACCOUNT_NO.getName()));
							doc.put(FieldType.IFSC_CODE.getName(), dbobj1.getString(FieldType.IFSC_CODE.getName()));
							doc.put(FieldType.TXNTYPE.getName(), "IMPS");
						} else {
							doc.put(FieldType.PAYER_ADDRESS.getName(),
									dbobj1.getString(FieldType.PAYER_ADDRESS.getName()));
							doc.put(FieldType.TXNTYPE.getName(), "UPI");
						}

						if (StringUtils.isNotBlank(fields.get(FieldType.RESELLER_ID.getName()))) {
							doc.put(FieldType.RESELLER_ID.getName(), fields.get(FieldType.RESELLER_ID.getName()));
						}

						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
						String autoOrderId = "LP" + sdf.format(new Date());
						doc.put(FieldType.ORDER_ID.getName(), autoOrderId);
						doc.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());
						Date dNow = new Date();
						String dateNow = DateCreater.formatDateForDb(dNow);
						doc.put(FieldType.CREATE_DATE.getName(), dateNow);
						doc.put(FieldType.IS_PAYBLE_MERCHANT.getName(), dbobj1.getString(FieldType.IS_PAYBLE_MERCHANT.getName()));
						doc.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));
						doc.put(FieldType.BENE_REGISTRATION.getName(), "Active");
						doc.put(FieldType.STATUS.getName(), dbobj1.getString(FieldType.STATUS.getName()));
						doc.put(FieldType.PHONE_NO.getName(), fields.get(FieldType.PHONE_NO.getName()));
						if (StringUtils.isNotBlank(fields.get(FieldType.EMAIL.getName()))) {
							doc.put(FieldType.EMAIL.getName(), fields.get(FieldType.EMAIL.getName()));
						}
						doc.put(FieldType.RESPONSE_CODE.getName(), dbobj1.getString(FieldType.RESPONSE_CODE.getName()));
						if (StringUtils.isNotBlank(dbobj1.getString(FieldType.PG_RESPONSE_MSG.getName()))) {
							doc.put(FieldType.PG_RESPONSE_MSG.getName(),
									dbobj1.getString(FieldType.PG_RESPONSE_MSG.getName()));
						}
						if (StringUtils.isNotBlank(dbobj1.getString(FieldType.RESPONSE_MESSAGE.getName()))) {
							doc.put(FieldType.RESPONSE_MESSAGE.getName(),
									dbobj1.getString(FieldType.RESPONSE_MESSAGE.getName()));
						}
						coll.insertOne(doc);
					}
					break;
				}
			}

			if (flag) {
				return true;
			}

			return false;

		} catch (Exception e) {
			logger.error("Exception occured in AddBene , Exception = " , e);
		}

		/*
		 * try { String bankAccountNumber =
		 * fields.get(FieldType.BENE_ACCOUNT_NO.getName()); String bankIFSC =
		 * fields.get(FieldType.IFSC_CODE.getName()); String vpa =
		 * fields.get(FieldType.PAYER_ADDRESS.getName());
		 * 
		 * MongoDatabase dbIns = mongoInstance.getDB();
		 * MongoCollection<Document> coll = dbIns.getCollection(
		 * PropertiesManager.propertiesMap.get(prefix +
		 * Constants.COMPOSITE_BENE_COLLECTION.getValue())); BasicDBObject query
		 * = new BasicDBObject();
		 * 
		 * // if request have account number means imps else vpa if
		 * (StringUtils.isNotBlank(bankAccountNumber)) {
		 * logger.info("checking bene for Account Number " + bankAccountNumber +
		 * " IFSC " + bankIFSC);
		 * query.append(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNumber);
		 * query.append(FieldType.IFSC_CODE.getName(), bankIFSC); } else if
		 * (StringUtils.isNotBlank(vpa)) { logger.info("checking bene for VPA "
		 * + vpa); query.append(FieldType.PAYER_ADDRESS.getName(), vpa); }
		 * 
		 * List<BasicDBObject> statusListQuery = new ArrayList<BasicDBObject>();
		 * 
		 * statusListQuery.add(new BasicDBObject(FieldType.STATUS.getName(),
		 * ErrorType.SUCCESS.name())); // statusListQuery.add(new //
		 * BasicDBObject(FieldType.STATUS.getName(),StatusType.FAILED.getName())
		 * ); query.append("$or", statusListQuery);
		 * 
		 * MongoCursor<Document> cursor = coll.find(query).iterator();
		 * 
		 * while (cursor.hasNext()) { logger.info("Found Beneficiary in DB ");
		 * return true; } cursor.close(); } catch (Exception e) {
		 * logger.error("Exception caugth while checkDuplicateBene, " + e); }
		 */
		logger.info("Not Found Beneficiary in DB ");
		return false;

	}

	public void updateBeneName(Fields fields) {
		//logger.info("inside the updateBeneName");
		try {
			String bankAccountNumber = fields.get(FieldType.BENE_ACCOUNT_NO.getName());
			String bankIFSC = fields.get(FieldType.IFSC_CODE.getName());
			String beneName = fields.get(FieldType.BENE_NAME.getName());

			if (StringUtils.isNotBlank(beneName)) {
				logger.info("Bene Name for Update is " + beneName);
				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns.getCollection(
						PropertiesManager.propertiesMap.get(prefix + Constants.COMPOSITE_BENE_COLLECTION.getValue()));
				BasicDBObject query = new BasicDBObject();

				Bson filter = new Document(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNumber)
						.append(FieldType.IFSC_CODE.getName(), bankIFSC);

				Bson newValue = new Document(FieldType.BENE_NAME.getName(), beneName);

				Bson updateOperationDocument = new Document("$set", newValue);
				coll.updateOne(filter, updateOperationDocument);
			} else {
				logger.info("Bene Name not updated due to empty Bene Name Recived in IMPS transaction");
			}

		} catch (Exception e) {
			logger.error("exception in updating bene Name ",e);
		}

	}

	public boolean checkNodalBeneDuplicateAccountNo(Fields fields) {
		//logger.info("Inside checkNodalBeneDuplicateAccountNo()");

		try {

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			BasicDBObject conditionQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.NODAL_BENEFICIARY_COLLECTION_NAME.getValue()));

			if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
						fields.get(FieldType.SUB_MERCHANT_ID.getName())));
			}

			paramConditionLst
					.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.BENE_ACCOUNT_NO.getName()))) {
				conditionList.add(new BasicDBObject(FieldType.BENE_ACCOUNT_NO.getName(),
						fields.get(FieldType.BENE_ACCOUNT_NO.getName())));
				conditionList.add(
						new BasicDBObject(FieldType.IFSC.getName(), fields.get(FieldType.IFSC.getName())));
				
				List<BasicDBObject> statusList= new ArrayList<BasicDBObject>();
				statusList.add(new BasicDBObject(FieldType.STATUS.getName(), ErrorType.SUCCESS.name()));
				statusList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PROCESSING.getName()));
				conditionList.add(new BasicDBObject("$or",statusList));
			}

			if (!conditionList.isEmpty()) {
				conditionQuery = new BasicDBObject("$and", conditionList);
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!conditionQuery.isEmpty()) {
				fianlList.add(conditionQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside duplicate Beneficiary check nodal account , finalquery = " + finalquery);
			MongoCursor<Document> cursor = coll.find(finalquery).iterator();
			
			//if found account number with payID else find account number
			if (cursor.hasNext()) {
				logger.info("found duplicate Account Details nodal beneficary for payId");
				Document dbobj = cursor.next();
				fields.put(FieldType.PG_RESP_CODE.getName(),dbobj.getString(FieldType.PG_RESP_CODE.getName()));
				fields.put(FieldType.ACQ_ID.getName(),dbobj.getString(FieldType.ACQ_ID.getName()));
				fields.put(FieldType.STATUS.getName(),dbobj.getString(FieldType.STATUS.getName()));
				fields.put(FieldType.RESPONSE_MESSAGE.getName(),dbobj.getString(FieldType.RESPONSE_MESSAGE.getName()));
				
				fields.remove(FieldType.REQUEST_TYPE.getName());
				
				cursor.close();
				return true;

			}else{
				cursor.close();
				MongoCursor<Document> cursor1 = coll.find(conditionQuery).iterator();
				if(cursor1.hasNext()){
					logger.info("found duplicate Account Details nodal beneficary");
					Document dbobj = cursor1.next();
					fields.put(FieldType.PG_RESP_CODE.getName(),dbobj.getString(FieldType.PG_RESP_CODE.getName()));
					fields.put(FieldType.ACQ_ID.getName(),dbobj.getString(FieldType.ACQ_ID.getName()));
					fields.put(FieldType.STATUS.getName(),dbobj.getString(FieldType.STATUS.getName()));
					fields.put(FieldType.RESPONSE_MESSAGE.getName(),dbobj.getString(FieldType.RESPONSE_MESSAGE.getName()));
					
					fields.remove(FieldType.REQUEST_TYPE.getName());
					
					
					if (Boolean.valueOf(fields.get(FieldType.BENE_DEFAULT.getName()))) {
						field.updateIciciCibDefaultBeneFields(fields);
					}
					
					field.insertIciciCibBeneficiaryFields(fields);
					
					
					
					cursor1.close();
					return true;
				}
				
			}
		} catch (Exception e) {
			logger.error("exception ",e);
		}

		logger.info("Beneficiary Not Found  in DB ");

		return false;
	}

}
