package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.CustomerQR;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Pooja Pancholi
 *
 */

@Service
public class CustomerQRDao {
	
	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userDao;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(CustomerQRDao.class.getName());
	
	@SuppressWarnings("static-access")
	public int CustomerQRReportCount(String merchantId, String customerAccountNumber, String customerId,
			String status,User user, List<String> PayIdList) {
		
		logger.info("Inside CustomerQRDao , CustomerQRReportCount()");
		try {
			int total = 0;
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject customerAccountNumberQuery = new BasicDBObject();
			BasicDBObject customerIdQuery = new BasicDBObject();
			List<BasicDBObject> payIdQueryList = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQury = new BasicDBObject();


			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));
			}

			if (!PayIdList.isEmpty()) {
				for(String payId : PayIdList) {
					payIdQueryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
				}
				payIdQury = new BasicDBObject("$or", payIdQueryList);
			}
			
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				statusQuery = new BasicDBObject(FieldType.STATUS.getName(), status);
			}


			if (StringUtils.isNotBlank(customerAccountNumber) && !customerAccountNumber.equalsIgnoreCase("ALL")) {
				customerAccountNumberQuery = new BasicDBObject(FieldType.CUSTOMER_ACCOUNT_NO.getName(), customerAccountNumber);
			}
			
			if (StringUtils.isNotBlank(customerId) && !customerId.equalsIgnoreCase("ALL")) {
				customerIdQuery = new BasicDBObject(FieldType.CUSTOMER_ID.getName(), customerId);
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}

			if (!customerAccountNumberQuery.isEmpty()) {
				fianlList.add(customerAccountNumberQuery);
			}
			
			if (!customerIdQuery.isEmpty()) {
				fianlList.add(customerIdQuery);
			}

			if (!payIdQury.isEmpty()) {
				fianlList.add(payIdQury);
			}

			BasicDBObject finalquery;
			if(!fianlList.isEmpty()) {
			finalquery = new BasicDBObject("$and", fianlList);
			}else {
				finalquery = new BasicDBObject();	
			}

			logger.info("Inside CustomerQRDao , CustomerQRReportCount , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CASHFREE_QRCODE.getValue()));
			
			total = (int) coll.count(finalquery);
			logger.info("Inside CustomerQRReportCount , total records from DB  = " + total);
			return total;
		} catch (Exception e) {
			logger.error("Exception occured in CustomerQRDao , CustomerQRReportCount , Exception = " , e);
			return 0;
		}
	}
	
	@SuppressWarnings("static-access")
	public List<CustomerQR> CustomerQRReportData(String merchantId, String customerAccountNumber, String customerId,
			String status,User user, List<String> PayIdList, int start, int length) {
		
		logger.info("Inside CustomerQRDao , CustomerQRReportData()");
		List<CustomerQR> customerQrList = new ArrayList<CustomerQR>();
		Map<String, User> userMap = new HashMap<String, User>();
		boolean batuwaMerchant = false;
		
		if(user.getUserType().equals(UserType.MERCHANT) && StringUtils.isNotBlank(user.getResellerId())) {
			String rslrId = PropertiesManager.propertiesMap.get("BATUWA_RESELLER_ID");
			
			if(StringUtils.isNotBlank(rslrId) && rslrId.equals(user.getResellerId())) {
				batuwaMerchant = true;
			}
		}
			
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject customerAccountNumberQuery = new BasicDBObject();
			BasicDBObject customerIdQuery = new BasicDBObject();
			List<BasicDBObject> payIdQueryList = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQuery = new BasicDBObject();


			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));
			}

//			if (user.getUserType().equals(UserType.RESELLER)) {
//				resellerMerchantsQueryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), PayIdList));
//				resellerMerchantsQuery = new BasicDBObject("$or", resellerMerchantsQueryList);
//			}
			
			if (!PayIdList.isEmpty()) {
				for(String payId : PayIdList) {
					payIdQueryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
				}
				payIdQuery = new BasicDBObject("$or", payIdQueryList);
			}
			
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				statusQuery = new BasicDBObject(FieldType.STATUS.getName(), status);
			}


			if (StringUtils.isNotBlank(customerAccountNumber) && !customerAccountNumber.equalsIgnoreCase("ALL")) {
				customerAccountNumberQuery = new BasicDBObject(FieldType.CUSTOMER_ACCOUNT_NO.getName(), customerAccountNumber);
			}
			
			if (StringUtils.isNotBlank(customerId) && !customerId.equalsIgnoreCase("ALL")) {
				customerIdQuery = new BasicDBObject(FieldType.CUSTOMER_ID.getName(), customerId);
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}

			if (!customerAccountNumberQuery.isEmpty()) {
				fianlList.add(customerAccountNumberQuery);
			}
			
			if (!customerIdQuery.isEmpty()) {
				fianlList.add(customerIdQuery);
			}
			
			if (!payIdQuery.isEmpty()) {
				fianlList.add(payIdQuery);
			}

			BasicDBObject finalquery = new BasicDBObject();;
			if(fianlList.isEmpty()) {
				if(user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
				}else {
					finalquery = new BasicDBObject("$and", fianlList);
				}
			}else {
				finalquery = new BasicDBObject("$and", fianlList);
					
			}

			logger.info("Inside CustomerQRDao , CustomerQRReportData , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CASHFREE_QRCODE.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			BasicDBObject skip = new BasicDBObject("$skip", start);
			BasicDBObject limit = new BasicDBObject("$limit", length);
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, skip, limit);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				CustomerQR customerQRReport = new CustomerQR();
				
				if (userMap.get(dbobj.get(FieldType.PAY_ID.getName())) != null) {
					
					User usr = userMap.get(dbobj.get(FieldType.PAY_ID.getName()).toString());
					customerQRReport.setPayId(dbobj.get(FieldType.PAY_ID.getName()).toString());
					customerQRReport.setMerchantName(usr.getBusinessName());
					
				}
				else {
					
					User usr = userDao.findPayId(dbobj.get(FieldType.PAY_ID.getName()).toString());
					customerQRReport.setPayId(dbobj.get(FieldType.PAY_ID.getName()).toString());
					customerQRReport.setMerchantName(usr.getBusinessName());
					userMap.put(dbobj.get(FieldType.PAY_ID.getName()).toString(), usr);
					
				}
				
				if(batuwaMerchant) {
					customerQRReport.setBatuwaMerchant(true);
				}
				
				if(dbobj.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()) != null ) {
					customerQRReport.setCustomerAccountNumber(dbobj.getString(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
				}else {
					customerQRReport.setCustomerAccountNumber(CrmFieldConstants.NA.getValue());
				}
				
				if(dbobj.get(FieldType.CUSTOMER_ID.getName()) != null ) {
					customerQRReport.setCustomerId(dbobj.getString(FieldType.CUSTOMER_ID.getName()));
					}else {
						customerQRReport.setCustomerId(CrmFieldConstants.NA.getValue());	
					}
				
				if(dbobj.getString(FieldType.CREATE_DATE.getName()) != null) {
					customerQRReport.setDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				}else {
					customerQRReport.setDate(CrmFieldConstants.NA.getValue());
				}
				
				if(dbobj.getString(FieldType.UPI_QR_CODE.getName()) != null ) {
					customerQRReport.setUpiQrCode(dbobj.getString(FieldType.UPI_QR_CODE.getName()));
				}else {
					customerQRReport.setUpiQrCode(CrmFieldConstants.NA.getValue());	
				}
				
				if(dbobj.get(FieldType.STATUS.getName()) != null) {
					customerQRReport.setStatus(dbobj.getString(FieldType.STATUS.getName()));	
				}else {
					customerQRReport.setStatus(CrmFieldConstants.NA.getValue());
				}
				
				if(dbobj.get(FieldType.CUST_NAME.getName()) != null ) {
					customerQRReport.setCustomerName(dbobj.getString(FieldType.CUST_NAME.getName()));
				}else {
					customerQRReport.setCustomerName(CrmFieldConstants.NA.getValue());
				}
				
				if(dbobj.getString(FieldType.CUST_PHONE.getName()) != null ) {
					customerQRReport.setCustomerPhone(dbobj.getString(FieldType.CUST_PHONE.getName()));
				}else {
					customerQRReport.setCustomerPhone(CrmFieldConstants.NA.getValue());
				}
				
				if(dbobj.getString(FieldType.AMOUNT.getName()) != null) {
					customerQRReport.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
				}else {
					customerQRReport.setAmount(CrmFieldConstants.NA.getValue());
				}
				if(dbobj.getString("VPA") != null) {
					customerQRReport.setVpa(dbobj.getString("VPA"));
				}else {
					customerQRReport.setVpa(CrmFieldConstants.NA.getValue());
				}
				
				customerQrList.add(customerQRReport);
			}
			cursor.close();
			
			logger.info("Total data in customer QR is " + customerQrList.size());
			return customerQrList;
		} catch (Exception e) {
			logger.error("Exception occured in CustomerQRDao , CustomerQRReportData , Exception = " , e);
			return customerQrList;
		}
	}

	
	public boolean activeOrInactiveCustomerData(String merchantId, String customerAccountNumber, String customerId, String status, User user) {
		logger.info("Inside  activeOrInactiveCustomerData()");

		try {
			Map<String, String> requestMap = new HashMap<>();
		if (StringUtils.isNotBlank(merchantId)) {
			requestMap.put(FieldType.PAY_ID.getName(), merchantId);
		}
		
		if (StringUtils.isNotBlank(customerAccountNumber)) {
			requestMap.put(FieldType.CUSTOMER_ACCOUNT_NO.getName(), customerAccountNumber);
		}
		
		if (StringUtils.isNotBlank(customerId)) {
			requestMap.put(FieldType.CUSTOMER_ID.getName(), customerId);
		}
		
		if (StringUtils.isNotBlank(status)) {
			requestMap.put(FieldType.STATUS.getName(), status);
		}
		
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.CASHFREE_QRCODE.getValue()));
		
		
		Bson filter = new Document(FieldType.PAY_ID.getName(),
						requestMap.get(FieldType.PAY_ID.getName())).append(FieldType.CUSTOMER_ACCOUNT_NO.getName(),
								requestMap.get(FieldType.CUSTOMER_ACCOUNT_NO.getName())).append(FieldType.CUSTOMER_ID.getName(),
										requestMap.get(FieldType.CUSTOMER_ID.getName())).append(FieldType.STATUS.getName(),
												requestMap.get(FieldType.STATUS.getName()));
		Bson newValue;
		if(requestMap.get(FieldType.STATUS.getName()).equalsIgnoreCase("Active")) {
			newValue = new Document(FieldType.STATUS.getName(), "InActive");	
		}else {
			newValue = new Document(FieldType.STATUS.getName(), "Active");
		}

		Bson updateOperationDocument = new Document("$set", newValue);
		coll.updateOne(filter, updateOperationDocument);
		return true;
		
		}catch(Exception e) {
			logger.error("Exception occured in CustomerQRDao activeOrInactiveCustomerData(), Exception = " , e);
			return false;
		}

	}
	
	@SuppressWarnings("static-access")
	public List<CustomerQR> CustomerQRDownloadReportData(String merchantId, String customerAccountNumber, String customerId,
			String status,User user, List<String> PayIdList) {
		
		logger.info("Inside CustomerQRDao , CustomerQRDownloadReportData()");
		List<CustomerQR> customerQrList = new ArrayList<CustomerQR>();
		Map<String, User> userMap = new HashMap<String, User>();

		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject customerAccountNumberQuery = new BasicDBObject();
			BasicDBObject customerIdQuery = new BasicDBObject();
			List<BasicDBObject> payIdQueryList = new ArrayList<BasicDBObject>();
			BasicDBObject payIdQuery = new BasicDBObject();


			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantId));
			}

//			if (user.getUserType().equals(UserType.RESELLER)) {
//				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
//			}
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			
			if (!PayIdList.isEmpty()) {
				for(String payId : PayIdList) {
					payIdQueryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
				}
				payIdQuery = new BasicDBObject("$or", payIdQueryList);
			}
			
			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				statusQuery = new BasicDBObject(FieldType.STATUS.getName(), status);
			}


			if (StringUtils.isNotBlank(customerAccountNumber) && !customerAccountNumber.equalsIgnoreCase("ALL")) {
				customerAccountNumberQuery = new BasicDBObject(FieldType.CUSTOMER_ACCOUNT_NO.getName(), customerAccountNumber);
			}
			
			if (StringUtils.isNotBlank(customerId) && !customerId.equalsIgnoreCase("ALL")) {
				customerIdQuery = new BasicDBObject(FieldType.CUSTOMER_ID.getName(), customerId);
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}

			if (!customerAccountNumberQuery.isEmpty()) {
				fianlList.add(customerAccountNumberQuery);
			}
			
			if (!customerIdQuery.isEmpty()) {
				fianlList.add(customerIdQuery);
			}
			
			if (!payIdQuery.isEmpty()) {
				fianlList.add(payIdQuery);
			}

			BasicDBObject finalquery = new BasicDBObject();;
			if(fianlList.isEmpty()) {
				if(user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
				}else {
					finalquery = new BasicDBObject("$and", fianlList);
				}
			}else {
				finalquery = new BasicDBObject("$and", fianlList);
					
			}

			logger.info("Inside CustomerQRDao , CustomerQRDownloadReportData , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CASHFREE_QRCODE.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				CustomerQR customerQRReport = new CustomerQR();
				
				if (userMap.get(dbobj.get(FieldType.PAY_ID.getName())) != null) {
					
					User usr = userMap.get(dbobj.get(FieldType.PAY_ID.getName()).toString());
					customerQRReport.setPayId(dbobj.get(FieldType.PAY_ID.getName()).toString());
					customerQRReport.setMerchantName(usr.getBusinessName());
				}
				else {
					
					User usr = userDao.findPayId(dbobj.get(FieldType.PAY_ID.getName()).toString());
					customerQRReport.setPayId(dbobj.get(FieldType.PAY_ID.getName()).toString());
					customerQRReport.setMerchantName(usr.getBusinessName());
					userMap.put(dbobj.get(FieldType.PAY_ID.getName()).toString(), usr);
				}

				
				if(dbobj.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()) != null ) {
					customerQRReport.setCustomerAccountNumber(dbobj.getString(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
				}else {
					customerQRReport.setCustomerAccountNumber(CrmFieldConstants.NA.getValue());
				}
				
				if(dbobj.get(FieldType.CUSTOMER_ID.getName()) != null ) {
					customerQRReport.setCustomerId(dbobj.getString(FieldType.CUSTOMER_ID.getName()));
					}else {
						customerQRReport.setCustomerId(CrmFieldConstants.NA.getValue());	
					}
				
				if(dbobj.getString(FieldType.CREATE_DATE.getName()) != null) {
					customerQRReport.setDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				}else {
					customerQRReport.setDate(CrmFieldConstants.NA.getValue());
				}
				
				if(dbobj.getString(FieldType.UPI_QR_CODE.getName()) != null ) {
					customerQRReport.setUpiQrCode(dbobj.getString(FieldType.UPI_QR_CODE.getName()));
				}else {
					customerQRReport.setUpiQrCode(CrmFieldConstants.NA.getValue());	
				}
				
				if(dbobj.get(FieldType.STATUS.getName()) != null) {
					customerQRReport.setStatus(dbobj.getString(FieldType.STATUS.getName()));	
				}else {
					customerQRReport.setStatus(CrmFieldConstants.NA.getValue());
				}
				
				if(dbobj.get(FieldType.CUST_NAME.getName()) != null ) {
					customerQRReport.setCustomerName(dbobj.getString(FieldType.CUST_NAME.getName()));
				}else {
					customerQRReport.setCustomerName(CrmFieldConstants.NA.getValue());
				}
				
				if(dbobj.getString(FieldType.CUST_PHONE.getName()) != null ) {
					customerQRReport.setCustomerPhone(dbobj.getString(FieldType.CUST_PHONE.getName()));
				}else {
					customerQRReport.setCustomerPhone(CrmFieldConstants.NA.getValue());
				}
				
				if(dbobj.getString(FieldType.AMOUNT.getName()) != null) {
					customerQRReport.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
				}else {
					customerQRReport.setAmount(CrmFieldConstants.NA.getValue());
				}
				
				if(StringUtils.isNotBlank(dbobj.getString("VPA"))) {
					customerQRReport.setVpa(dbobj.getString("VPA"));
				}else {
					customerQRReport.setVpa(CrmFieldConstants.NA.getValue());
				}
				
				customerQrList.add(customerQRReport);
			}
			cursor.close();
			
			logger.info("Total data in customer QR is " + customerQrList.size());
			return customerQrList;
		} catch (Exception e) {
			logger.error("Exception occured in CustomerQRDao , CustomerQRDownloadReportData , Exception = " , e);
			return customerQrList;
		}
	}
	
	public CustomerQR downloadStaticUpiQrPDFData(String payId, String customerAccountNumber, String customerId) {

		logger.info("Inside CustomerQRDao , downloadStaticUpiQrPDFData function");
		CustomerQR customerQRReport = new CustomerQR();
		try {

			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			finalList.add(new BasicDBObject(FieldType.CUSTOMER_ACCOUNT_NO.getName(), customerAccountNumber));
			finalList.add(new BasicDBObject(FieldType.CUSTOMER_ID.getName(), customerId));
			BasicDBObject finalquery = new BasicDBObject();
			finalquery = new BasicDBObject("$and", finalList);

			logger.info("Inside CustomerQRDao , downloadStaticUpiQrPDFData , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CASHFREE_QRCODE.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (dbobj.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()) != null) {
					customerQRReport.setCustomerAccountNumber(dbobj.getString(FieldType.CUSTOMER_ACCOUNT_NO.getName()));
				} else {
					customerQRReport.setCustomerAccountNumber(CrmFieldConstants.NA.getValue());
				}

				if (dbobj.get(FieldType.CUSTOMER_ID.getName()) != null) {
					customerQRReport.setCustomerId(dbobj.getString(FieldType.CUSTOMER_ID.getName()));
				} else {
					customerQRReport.setCustomerId(CrmFieldConstants.NA.getValue());
				}

				if (dbobj.getString(FieldType.COMPANY_NAME.getName()) != null) {
					customerQRReport.setCompanyName(dbobj.getString(FieldType.COMPANY_NAME.getName()));
				} else {
					customerQRReport.setCompanyName(CrmFieldConstants.NA.getValue());
				}

				if (dbobj.getString(FieldType.UPI_QR_CODE.getName()) != null) {
					customerQRReport.setUpiQrCode(dbobj.getString(FieldType.UPI_QR_CODE.getName()));
				} else {
					customerQRReport.setUpiQrCode(CrmFieldConstants.NA.getValue());
				}
				
				if (dbobj.getString(FieldType.VPA.getName()) != null) {
					customerQRReport.setVpa(dbobj.getString(FieldType.VPA.getName()));
				} else {
					customerQRReport.setVpa(CrmFieldConstants.NA.getValue());
				}
			}
			cursor.close();
			return customerQRReport;
		} catch (Exception ex) {
			logger.error("Exception caught in CustomerQRDao , downloadStaticUpiQrPDFData , Exception = ", ex);
			return customerQRReport;
		}
	}

}
