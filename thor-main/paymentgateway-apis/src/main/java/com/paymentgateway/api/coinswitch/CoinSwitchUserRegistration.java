package com.paymentgateway.api.coinswitch;

import java.util.ArrayList;
import java.util.Date;
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
import com.paymentgateway.api.cashFree.CashFreeRequestHandler;
import com.paymentgateway.commons.dao.VirtualAccountNumberGeneratorDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.commons.util.VirtualAccountNumberGenerator;

/*
*@auther Sandeep Sharma
*/

@Service
public class CoinSwitchUserRegistration {

	private static Logger logger = LoggerFactory.getLogger(CoinSwitchUserRegistration.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private VirtualAccountNumberGenerator virtualAccountNumberGenerator;

	@Autowired
	private VirtualAccountNumberGeneratorDao virtualAccountNumberGeneratorDao;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;
	
	@Autowired
	private CashFreeRequestHandler cashFreeRequestHandler;
	
	@Autowired
	private CoinSwitchService coinSwitchService;

	@SuppressWarnings("static-access")
	public Map<String, String> saveUserDetail(Fields fields) {
		Map<String, String> saveResponse = new HashMap<String, String>();
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COINSWITCH_ACCOUNTS.getValue()));

			Document userDataObj = new Document();
			userDataObj.put("_id", TransactionManager.getNewTransactionId());
			userDataObj.put(FieldType.CUST_NAME.getName(), fields.get(FieldType.CUST_NAME.getName()));
			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_ID.getName()))) {
				userDataObj.put(FieldType.CUST_ID.getName(), fields.get(FieldType.CUST_ID.getName()));
			}
			userDataObj.put(FieldType.PHONE_NO.getName(), fields.get(FieldType.PHONE_NO.getName()));
			userDataObj.put(FieldType.CUST_EMAIL.getName(), fields.get(FieldType.CUST_EMAIL.getName()));
			if (StringUtils.isNotBlank(fields.get(FieldType.ADDRESS.getName()))) {
				userDataObj.put(FieldType.ADDRESS.getName(), fields.get(FieldType.ADDRESS.getName()));
			}
			if (StringUtils.isNotBlank(fields.get(FieldType.AADHAR.getName()))) {
				userDataObj.put(FieldType.AADHAR.getName(), fields.get(FieldType.AADHAR.getName()));
			}
			if (StringUtils.isNotBlank(fields.get(FieldType.PAN.getName()))) {
				userDataObj.put(FieldType.PAN.getName(), fields.get(FieldType.PAN.getName()));
			}
			if (StringUtils.isNotBlank(fields.get(FieldType.DOB.getName()))) {
				userDataObj.put(FieldType.DOB.getName(), fields.get(FieldType.DOB.getName()));
			}
			userDataObj.put(FieldType.VIRTUAL_ACC_NUM.getName(), fields.get(FieldType.VIRTUAL_ACC_NUM.getName()));
			userDataObj.put(FieldType.VIRTUAL_ACC_IFSC.getName(), fields.get(FieldType.VIRTUAL_ACC_IFSC.getName()));
			userDataObj.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(),
					fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
			userDataObj.put(FieldType.BANK_NAME.getName(), fields.get(FieldType.BANK_NAME.getName()));
			userDataObj.put(FieldType.IFSC_CODE.getName(), fields.get(FieldType.IFSC_CODE.getName()));
			userDataObj.put(FieldType.ACCOUNT_HOLDER_NAME.getName(),
					fields.get(FieldType.ACCOUNT_HOLDER_NAME.getName()));
			userDataObj.put(FieldType.CREATE_DATE.getName(), dateNow);
			userDataObj.put(FieldType.UPDATE_DATE.getName(), dateNow);
			userDataObj.put(FieldType.STATUS.getName(), UserStatusType.ACTIVE.getStatus());
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				coll.insertOne(dataEncDecTool.encryptDocument(userDataObj));
			} else {
				coll.insertOne(userDataObj);
			}
			//coll.insertOne(userDataObj);
			
			saveResponse.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
			saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
			saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
			saveResponse.put(FieldType.VIRTUAL_ACC_NUM.getName(), fields.get(FieldType.VIRTUAL_ACC_NUM.getName()));
			saveResponse.put(FieldType.VIRTUAL_ACC_IFSC.getName(), fields.get(FieldType.VIRTUAL_ACC_IFSC.getName()));
			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_ID.getName()))) {
				saveResponse.put(FieldType.CUST_ID.getName(), fields.get(FieldType.CUST_ID.getName()));
			}
			saveResponse.put(FieldType.CUST_NAME.getName(), fields.get(FieldType.CUST_NAME.getName()));
			saveResponse.put(FieldType.CUST_EMAIL.getName(), fields.get(FieldType.CUST_EMAIL.getName()));
			if (StringUtils.isNotBlank(fields.get(FieldType.PAN.getName()))) {
				saveResponse.put(FieldType.PAN.getName(), fields.get(FieldType.PAN.getName()));
			}
			saveResponse.put(FieldType.PHONE_NO.getName(), fields.get(FieldType.PHONE_NO.getName()));
			if (StringUtils.isNotBlank(fields.get(FieldType.ADDRESS.getName()))) {
				saveResponse.put(FieldType.ADDRESS.getName(), fields.get(FieldType.ADDRESS.getName()));
			}
			if (StringUtils.isNotBlank(fields.get(FieldType.AADHAR.getName()))) {
				saveResponse.put(FieldType.AADHAR.getName(), fields.get(FieldType.AADHAR.getName()));
			}
			if (StringUtils.isNotBlank(fields.get(FieldType.DOB.getName()))) {
				saveResponse.put(FieldType.DOB.getName(), fields.get(FieldType.DOB.getName()));
			}
			saveResponse.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(),
					fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
			saveResponse.put(FieldType.BANK_NAME.getName(), fields.get(FieldType.BANK_NAME.getName()));
			saveResponse.put(FieldType.IFSC_CODE.getName(), fields.get(FieldType.IFSC_CODE.getName()));
			saveResponse.put(FieldType.ACCOUNT_HOLDER_NAME.getName(),
					fields.get(FieldType.ACCOUNT_HOLDER_NAME.getName()));

		} catch (Exception e) {
			logger.error("Exception in saving Data of coin Switch's Customer >>> ", e);
			saveResponse.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DATABASE_ERROR.getResponseMessage());
			saveResponse.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DATABASE_ERROR.getResponseCode());
		}
		return saveResponse;
	}

	@SuppressWarnings("static-access")
	public Map<String, String> updateUserStatus(Fields fields) {
		Map<String, String> response = new HashMap<String, String>();
		try {
			List<BasicDBObject> paramCondition = new ArrayList<BasicDBObject>();
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			BasicDBObject accNoQuery = new BasicDBObject(FieldType.CUST_ID.getName(),
					fields.get(FieldType.CUST_ID.getName()));
			paramCondition.add(accNoQuery);
			BasicDBObject oldData = new BasicDBObject("$and", paramCondition);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COINSWITCH_ACCOUNTS.getValue()));
			MongoCursor<Document> cursor = coll.find(oldData).iterator();
			if (cursor.hasNext()) {
				Document oldDoc = (Document) cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					oldDoc = dataEncDecTool.decryptDocument(oldDoc);
				} 
				
				 // update Cashfree VA				
				Map<String,String> responseMap = updateCashfreeVAStatus(fields.get(FieldType.CUST_ID.getName()),fields.get(FieldType.STATUS.getName()));
				logger.info("response recived for CUST_ID "+fields.get(FieldType.CUST_ID.getName())+" response Map "+responseMap);
                
                if(StringUtils.isNotBlank(responseMap.get(FieldType.STATUS.getName())) && responseMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(ErrorType.SUCCESS.name())){
                    Bson oldUserData = new Document("_id", oldDoc.getString("_id"))
                            .append(FieldType.PHONE_NO.getName(), oldDoc.getString(FieldType.PHONE_NO.getName()))
                            .append(FieldType.CUST_ID.getName(), oldDoc.getString(FieldType.CUST_ID.getName()));

                    Bson setData = new Document(FieldType.STATUS.getName(), responseMap.get(FieldType.STATUS.getName()))
                            .append(FieldType.UPDATE_DATE.getName(), dateNow);
                    Bson newUserData = new BasicDBObject("$set", setData);
                    coll.updateOne(oldUserData, newUserData);
                    
                    response.put(FieldType.PG_RESPONSE_MSG.getName(),responseMap.get(FieldType.PG_RESPONSE_MSG.getName()));
                    
                    response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
                    response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
                    response.put(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()));
                }else{
                    response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
                    response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getResponseCode());
                    response.put(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()));
                }

				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
				response.put(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()));
				
				response.put(FieldType.CUST_ID.getName(), fields.get(FieldType.CUST_ID.getName()));
			}

		} catch (Exception e) {
			logger.error("Exception in saving Data of coin Switch's Customer >>> ", e);
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DATABASE_ERROR.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DATABASE_ERROR.getResponseCode());
		}
		return response;
	}
    private Map<String,String> updateCashfreeVAStatus(String custId, String status) {
        logger.info("inside updateCashfreeVAStatus");
        Fields fields = new Fields();
        Map<String,String> responseMap=new HashMap<>();
        try {
        	
        	String payId=PropertiesManager.propertiesMap.get("COINDCX_PAYID");

            fields.put(FieldType.UPDATE_STATUS.getName(), status);
            fields.put(FieldType.CUST_ID.getName(), custId);
            fields.put(FieldType.PAY_ID.getName(), payId);
            
            logger.info("Update Request for CUST_ID  "+custId+ " status "+status);
            
            responseMap = coinSwitchService.cashfreeStatusUpdateVA(fields); 
            logger.info("Update Response for Virtual Account  "+responseMap);
            
        } catch (Exception e) {
            logger.info("exception in updateCashfreeVAStatus ", e);
        }

        return responseMap;

    }

	@SuppressWarnings("static-access")
	public Map<String, String> updateUserBankDetails(Fields fields) {
		Map<String, String> response = new HashMap<String, String>();
		try {
			List<BasicDBObject> paramCondition = new ArrayList<BasicDBObject>();
			Document setData = new Document();
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			if (StringUtils.isNotBlank(fields.get(FieldType.PHONE_NO.getName()))) {
				BasicDBObject phoneQuery = new BasicDBObject(FieldType.PHONE_NO.getName(),
						fields.get(FieldType.PHONE_NO.getName()));
				paramCondition.add(phoneQuery);
			}
			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
				BasicDBObject emailQuery = new BasicDBObject(FieldType.CUST_EMAIL.getName(),
						fields.get(FieldType.CUST_EMAIL.getName()));
				paramCondition.add(emailQuery);
			}
			BasicDBObject accNoQuery = new BasicDBObject(FieldType.VIRTUAL_ACC_NUM.getName(),
					fields.get(FieldType.VIRTUAL_ACC_NUM.getName()));

			paramCondition.add(accNoQuery);
			BasicDBObject oldData = new BasicDBObject("$and", paramCondition);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COINSWITCH_ACCOUNTS.getValue()));
			MongoCursor<Document> cursor = coll.find(oldData).iterator();
			if (cursor.hasNext()) {
				Document oldDoc = (Document) cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					oldDoc = dataEncDecTool.decryptDocument(oldDoc);
				} 
				
				Bson oldUserData = new Document("_id", oldDoc.getString("_id"))
						.append(FieldType.PHONE_NO.getName(), oldDoc.getString(FieldType.PHONE_NO.getName()))
						.append(FieldType.CUST_EMAIL.getName(), oldDoc.getString(FieldType.CUST_EMAIL.getName()));

				setData = new Document(FieldType.UPDATE_DATE.getName(), dateNow);

				if (StringUtils.isNotBlank(fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()))) {
					setData.append(FieldType.CUSTOMER_ACCOUNT_NO.getName(),
							fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
				}
				if (StringUtils.isNotBlank(fields.get(FieldType.BANK_NAME.getName()))) {
					setData.append(FieldType.BANK_NAME.getName(), fields.get(FieldType.BANK_NAME.getName()));
				}
				if (StringUtils.isNotBlank(fields.get(FieldType.IFSC_CODE.getName()))) {
					setData.append(FieldType.IFSC_CODE.getName(), fields.get(FieldType.IFSC_CODE.getName()));
				}
				if (StringUtils.isNotBlank(fields.get(FieldType.ACCOUNT_HOLDER_NAME.getName()))) {
					setData.append(FieldType.ACCOUNT_HOLDER_NAME.getName(),
							fields.get(FieldType.ACCOUNT_HOLDER_NAME.getName()));
				}
				Bson newUserData = new BasicDBObject("$set", setData);

				coll.updateOne(oldUserData, newUserData);

				response.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
				if (StringUtils.isNotBlank(fields.get(FieldType.PHONE_NO.getName()))) {
					response.put(FieldType.PHONE_NO.getName(), fields.get(FieldType.PHONE_NO.getName()));
				}
				if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
					response.put(FieldType.CUST_EMAIL.getName(), fields.get(FieldType.CUST_EMAIL.getName()));
				}
				response.put(FieldType.VIRTUAL_ACC_NUM.getName(), fields.get(FieldType.VIRTUAL_ACC_NUM.getName()));
				if (StringUtils.isNotBlank(fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()))) {
					response.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(),
							fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
				}
				if (StringUtils.isNotBlank(fields.get(FieldType.BANK_NAME.getName()))) {
					response.put(FieldType.BANK_NAME.getName(), fields.get(FieldType.BANK_NAME.getName()));
				}
				if (StringUtils.isNotBlank(fields.get(FieldType.IFSC_CODE.getName()))) {
					response.put(FieldType.IFSC_CODE.getName(), fields.get(FieldType.IFSC_CODE.getName()));
				}
				if (StringUtils.isNotBlank(fields.get(FieldType.ACCOUNT_HOLDER_NAME.getName()))) {
					response.put(FieldType.ACCOUNT_HOLDER_NAME.getName(),
							fields.get(FieldType.ACCOUNT_HOLDER_NAME.getName()));
				}
			}

		} catch (Exception e) {
			logger.error("Exception in saving Data of coin Switch's Customer >>> ", e);
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DATABASE_ERROR.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DATABASE_ERROR.getResponseCode());
		}
		return response;
	}

}
