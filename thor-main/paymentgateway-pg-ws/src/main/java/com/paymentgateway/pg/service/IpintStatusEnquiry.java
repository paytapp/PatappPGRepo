package com.paymentgateway.pg.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.ipint.IpintStatusEnquiryProcessor;
import com.paymentgateway.pg.security.SecurityProcessor;

@RestController
@CrossOrigin
public class IpintStatusEnquiry {
	private static Logger logger = LoggerFactory.getLogger(IpintStatusEnquiry.class.getName());




	@Autowired
	private IpintStatusEnquiryProcessor ipintStatusEnquiryProcessor;

	
	@Autowired
	private SecurityProcessor securityProcessor; 
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	EncryptDecryptService encryptDecryptService;
	
	@RequestMapping(method = RequestMethod.POST, value = "/ipintStatusEnquiry", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String ipintStatusEnquiry(@RequestBody Map<String, String> reqmap) {
		JSONObject response = new JSONObject();
		try {
			Fields fields = new Fields(reqmap);
			fields =getTxnKey(fields);
			logger.info("Ipint Status Enquiry  This PR_PEF_NUM : "+fields.get(FieldType.PG_REF_NUM.getName()));
			ipintStatusEnquiryProcessor.enquiryProcessor(fields);
			//cryptoStatusEnquiryCheck();
		} catch (Exception exception) {
			logger.error("Exception ipintStatusEnquiry  >>>> ", exception);
		}
		return response.toString();
	}	
	
	
	public Fields getTxnKey(Fields fields) throws SystemException {

		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		Account account = null;
		Set<Account> accounts = user.getAccounts();

		if (accounts == null || accounts.size() == 0) {
			logger.info("No account found for Pay ID = " + fields.get(FieldType.PAY_ID.getName()) + " and ORDER ID = "
					+ fields.get(FieldType.ORDER_ID.getName()));
		} else {
			for (Account accountThis : accounts) {
				if (accountThis.getAcquirerName()
						.equalsIgnoreCase(AcquirerType.getInstancefromCode(AcquirerType.IPINT.getCode()).getName())) {
					account = accountThis;
					break;
				}
			}
		}

		AccountCurrency accountCurrency = account.getAccountCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));
		String txnKey = accountCurrency.getTxnKey();
		
		if (!StringUtils.isEmpty(accountCurrency.getPassword())) {
			String decryptedPassword = encryptDecryptService.decrypt(fields.get(FieldType.PAY_ID.getName()),
					accountCurrency.getPassword());
			fields.put(FieldType.PASSWORD.getName(), decryptedPassword);
		}
		
		fields.put(FieldType.TXN_KEY.getName(), accountCurrency.getTxnKey());

		
		return fields;

	}
	
	
	
	private void cryptoStatusEnquiryCheck() {
		
		try {
			
			logger.info("inside CryptoStatusEnquiryJob for status enquiry ");
			
			SimpleDateFormat timeFormate = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
			 
			int addMinuteTime = 1;
			Calendar startTime = Calendar.getInstance();
			startTime.setTime(new Date());
			startTime.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE) - addMinuteTime);
			startTime.set(Calendar.SECOND, 0);
			
			Calendar endTime = Calendar.getInstance();
			endTime.set(Calendar.SECOND, 59);
			
			//String st =  timeFormate.format(startTime.getTimeInMillis());
			
			//String et =  timeFormate.format(endTime.getTimeInMillis());
			
			
			//String [] status =configurationProvider.getCRYPTO_STATUS().split(",");
			BasicDBObject queryList = new BasicDBObject();
			BasicDBObject dateTimeQuery = new BasicDBObject();
			
			//dateTimeQuery.put(FieldType.CREATE_DATE.getName(),BasicDBObjectBuilder.start("$lte", st).get());
			
			
			queryList.put(FieldType.PAYMENT_TYPE.getName(), "CR");
			queryList.put(FieldType.MOP_TYPE.getName(), "CR");

			//for(int i=0; i<status.length; i++) {
				queryList.put(FieldType.STATUS.getName(),StatusType.PROCESSING.getName());
			//}
			
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			
			//allConditionQueryList.add(dateTimeQuery);
			allConditionQueryList.add(queryList);
			
			BasicDBObject finalquery = new BasicDBObject("$and", allConditionQueryList);
			logger.info("Query to get data for Crypto Status Enquiry = " + finalquery);
			
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection("letzpaytransaction");
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			List<BasicDBObject> pipeline = Arrays.asList(match);
			AggregateIterable<Document> output = collection.aggregate(pipeline);
			output.allowDiskUse(true);
			JSONObject sendJobData = new JSONObject();
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Fields fields = new Fields();
				Document doc = cursor.next();
				logger.info(FieldType.ORDER_ID.getName()+"-------------"+ doc.getString(FieldType.ORDER_ID.getName()));
				logger.info(FieldType.PG_REF_NUM.getName()+"-------------"+ doc.getString(FieldType.PG_REF_NUM.getName()));		
				
				List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
				List<BasicDBObject> statusConditionLst = new ArrayList<BasicDBObject>();
				condList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName())));
				
				BasicDBObject statusQuery = new BasicDBObject();
				statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName()));
				statusConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName()));
				statusQuery.append("$or", statusConditionLst);
				condList.add(statusQuery);
				
				BasicDBObject saleQuery = new BasicDBObject("$and", condList);
				boolean flag=true;
				
				
				MongoDatabase dbIns1 = mongoInstance.getDB();
				MongoCollection<Document> collection1 = dbIns1.getCollection("letzpaytransaction");
				MongoCursor<Document> cursor1 = collection1.find(saleQuery).iterator();
				
				while (cursor1.hasNext()) {
					//if (cursor.hasNext()) {
						Document documentObj = (Document) cursor1.next();
						if(documentObj!=null) {
							flag=false;
							break;
						}
				//	}
				}	

				if(flag) {
					fields.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
					fields.put(FieldType.ACQ_ID.getName(), doc.getString(FieldType.ACQ_ID.getName()));
					fields.put(FieldType.RRN.getName(), doc.getString(FieldType.RRN.getName()));
					fields.put(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName()));
					fields.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
					fields.put(FieldType.INVOICE_ID.getName(), doc.getString(FieldType.INVOICE_ID.getName()));
					fields.put(FieldType.CURRENCY_CODE.getName(), doc.getString(FieldType.CURRENCY_CODE.getName()));
					fields.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
					
					//Map<String, String> res = transactionControllerServiceProvider.transact(fields, configurationProvider.getCryptoTransactionStatusEnquiryUrl());
				}
				
			}
		} catch(Exception ex) {
			logger.info("Exception caught while status enquiry for Crypto transaction by scheduler ",ex);
		}
		
	}
		
		
	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
