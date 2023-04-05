package com.paymentgateway.commons.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.TransactionHistory;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;
@Service
public class TransactionDetailsService {

	private static Logger logger = LoggerFactory.getLogger(TransactionDetailsService.class.getName());

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;
	
	@Autowired
	private PropertiesManager propertiesManager;

	private Connection getConnection() throws SQLException {
		return DataAccessObject.getBasicConnection();
	}

	private static final String prefix = "MONGO_DB_";
	public final static String query = "Select ORIG_TXN_ID ,ORDER_ID, TXN_ID, CREATE_DATE, PAY_ID,ACQUIRER_TYPE, "
			+ "CARD_MASK, MOP_TYPE, PAYMENT_TYPE, STATUS, TXNTYPE, CUST_EMAIL, "
			+ "INTERNAL_CUST_IP, INTERNAL_CUST_COUNTRY_NAME,INTERNAL_CARD_ISSUER_BANK,INTERNAL_CARD_ISSUER_COUNTRY, ACQ_ID, CURRENCY_CODE, AMOUNT,OID from "
			+ "TRANSACTION where OID in (select OID from TRANSACTION where TXN_ID=?)";

	public final static String orderIdSearchQuery = "Select ORIG_TXN_ID ,ORDER_ID, TXN_ID, CREATE_DATE, PAY_ID,ACQUIRER_TYPE, CARD_MASK, MOP_TYPE, PAYMENT_TYPE,"
			+ " STATUS, TXNTYPE, CUST_EMAIL, INTERNAL_CUST_IP,  ACQ_ID, CURRENCY_CODE, AMOUNT from "
			+ " TRANSACTION T where ORDER_ID in (select ORDER_ID from TRANSACTION where TXN_ID = ?) and "
			+ " PAY_ID in (select PAY_ID from TRANSACTION where TXN_ID = ?)";

	public final static String getOIDQuery = "Select MIN(OID) As 'OID' from TRANSACTION where TXN_ID = ?";
	public final static String txnAuthenticationQuery = "Select INTERNAL_TXN_AUTHENTICATION from TRANSACTION where TXN_ID = ?";
	public final static String updateAuthenticationQuery = "Update TRANSACTION Set INTERNAL_TXN_AUTHENTICATION = ? where TXN_ID = ?";

	public TransactionDetailsService() {

	}

	public List<TransactionHistory> getTransaction(String pgRefNum) throws SystemException {
		
		List<TransactionHistory> transactions = new ArrayList<TransactionHistory>();
	//	BasicDBObject finalQuery = new BasicDBObject();
		List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();
		fianlList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
		fianlList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		fianlList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
		BasicDBObject finalQuery = new BasicDBObject("$and", fianlList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(PropertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
		MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			try {
				transactions = setTransactionList(cursor);
			
			} catch (Exception exception) {
				logger.error("Exception Cought in getTransaction : ", exception);
			}
			
		return transactions;
	}

	private List<TransactionHistory> setTransactionList(MongoCursor<Document> rs) throws SQLException {
		List<TransactionHistory> transactions = new ArrayList<TransactionHistory>();
		while (rs.hasNext()) {
			Document docByOid = rs.next();
			
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				docByOid = dataEncDecTool.decryptDocument(docByOid);
			} 
			
			TransactionHistory transaction = new TransactionHistory();

			transaction.setOrigTxnId(docByOid.getString(FieldType.ORIG_TXN_ID.getName()));
			transaction.setOrderId(docByOid.getString(FieldType.ORDER_ID.getName()));
			transaction.setTxnId(docByOid.getString(FieldType.TXN_ID.getName()));			
			transaction.setCreateDate(DateCreater.createDateTimeFormat(docByOid.getString(CrmFieldConstants.CREATE_DATE.getValue())));
			transaction.setPayId(docByOid.getString(FieldType.PAY_ID.getName()));
			
			if(docByOid.containsKey(FieldType.SUB_MERCHANT_ID.getName())) {
				transaction.setSubMerchantPayId(docByOid.getString(FieldType.SUB_MERCHANT_ID.getName()));
			}
			
			transaction.setCardNumber(docByOid.getString(FieldType.CARD_MASK.getName()));
			transaction.setMopType(docByOid.getString(FieldType.MOP_TYPE.getName()));
			transaction.setStatus(docByOid.getString(FieldType.STATUS.getName()));
			transaction.setTxnType(docByOid.getString(FieldType.TXNTYPE.getName()));
			transaction.setCustEmail(docByOid.getString(FieldType.CUST_EMAIL.getName()));
			transaction.setInternalCustIP(docByOid.getString(FieldType.INTERNAL_CUST_IP.getName()));
			transaction.setInternalCustCountryName(docByOid.getString(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()));
			transaction.setInternalCardIssusserBank(docByOid.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
			transaction.setInternalCardIssusserCountry(docByOid.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));

			transaction.setAcqId(docByOid.getString(FieldType.ACQ_ID.getName()));
			transaction.setCurrencyCode(docByOid.getString(FieldType.CURRENCY_CODE.getName()));
			transaction.setAmount(docByOid.getString(FieldType.TOTAL_AMOUNT.getName()));
			transaction.setAcquirerCode(docByOid.getString(FieldType.ACQUIRER_TYPE.getName()));
			transaction.setPaymentType(docByOid.getString(FieldType.PAYMENT_TYPE.getName()));
			
			transaction.setMerchantTDR(new BigDecimal(docByOid.getString(FieldType.PG_TDR_SC.getName()))
					.add(new BigDecimal(docByOid.getString(FieldType.ACQUIRER_TDR_SC.getName()))));
//			Float float1=Float.parseFloat(docByOid.getString(FieldType.PG_TDR_SC))+Float.parseFloat(docByOid.getString(FieldType.ACQUIRER_TDR_SC));
//			transaction.setTdr(new BigDecimal(float1));

			transactions.add(transaction);
		}
		return transactions;
	}

	public String getOID(String txnId) throws SystemException {
		try (Connection connection = getConnection()) {
			try (PreparedStatement prepStament = connection.prepareStatement(getOIDQuery)) {
				prepStament.setString(1, txnId);
				try (ResultSet rs = prepStament.executeQuery()) {
					rs.next();
					return rs.getString(FieldType.OID.getName());
				}
			}

		} catch (SQLException sQLException) {
			MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), Constants.CRM_LOG_PREFIX.getValue());
			logger.error("Unable to close connection", sQLException);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getInternalMessage());
		}
	}

	public String getTransactionAuthentication(String txnId) throws SystemException {
		String txnAuthentication = "";
		try (Connection connection = getConnection()) {
			try (PreparedStatement prepStament = connection.prepareStatement(txnAuthenticationQuery)) {
				prepStament.setString(1, getOID(txnId));
				try (ResultSet rs = prepStament.executeQuery()) {
					rs.next();
					txnAuthentication = rs.getString(FieldType.INTERNAL_TXN_AUTHENTICATION.getName());
				}
			}
		} catch (SQLException sQLException) {
			MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), Constants.CRM_LOG_PREFIX.getValue());
			logger.error("Unable to close connection", sQLException);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getInternalMessage());
		}

		return txnAuthentication;
	}

	public String updateTransactionAuthentication(String internalTxnAuthentication, String txnId)
			throws SystemException {
		String txnAuthentication = "";
		try (Connection connection = getConnection()) {
			try (PreparedStatement prepStament = connection.prepareStatement(updateAuthenticationQuery)) {
				prepStament.setString(1, internalTxnAuthentication);
				prepStament.setString(2, getOID(txnId));
				prepStament.executeUpdate();
			}
		} catch (SQLException sQLException) {
			MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), Constants.CRM_LOG_PREFIX.getValue());
			logger.error("Unable to close connection", sQLException);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getInternalMessage());
		}

		return txnAuthentication;
	}
	
	public TransactionSearch getTransactionForInvoicePdf(String orderId) throws SystemException {
		
		TransactionSearch transactions = new TransactionSearch();
		
		try{
		
		MongoDatabase dbIns = mongoInstance.getDB();
		
		MongoCollection<Document> coll = dbIns
				.getCollection(PropertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
		
		BasicDBObject finalQuery = new BasicDBObject();
		finalQuery.put(FieldType.ORDER_ID.getName(), orderId);
		
		
		BasicDBObject match = new BasicDBObject("$match", finalQuery);
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.name(), -1));
		BasicDBObject limit = new BasicDBObject("$limit",1);
		List<BasicDBObject> pipeline = Arrays.asList(match, sort,limit);
		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();

		while (cursor.hasNext()) {
			Document dbobj = cursor.next();
			
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				dbobj = dataEncDecTool.decryptDocument(dbobj);
			} 
			
			transactions.setOrderId(dbobj.getString(FieldType.ORDER_ID.getName()));
			transactions.setTransactionIdString(dbobj.getString(FieldType.TXN_ID.getName()));
			transactions.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
			transactions.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
			transactions.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()));
			transactions.setCurrencyCode(dbobj.getString(FieldType.CURRENCY_CODE.getName()));
			transactions.setProductDesc(dbobj.getString(FieldType.PRODUCT_DESC.getName()));
			transactions.settDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
			transactions.setCustomerEmail(dbobj.getString(FieldType.CUST_EMAIL.getName()));
			transactions.setCustomerMobile(dbobj.getString(FieldType.CUST_PHONE.getName()));
			transactions.setCustomerName(dbobj.getString(FieldType.CUST_NAME.getName()));
			transactions.setQuantity(dbobj.getString(FieldType.QUANTITY.getName()));
			transactions.setServiceCharge(dbobj.getString(FieldType.SERVICE_CHARGE.getName()));
			transactions.setProductName(dbobj.getString(FieldType.PRODUCT_NAME.getName()));
			transactions.setPaymentMethods(dbobj.getString(FieldType.PAYMENT_TYPE.getName()));
			transactions.setMopType(dbobj.getString(FieldType.MOP_TYPE.getName()));
			transactions.setCardNumber(dbobj.getString(FieldType.CARD_MASK.getName()));
			transactions.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.getName()));
			transactions.setStatus(dbobj.getString(FieldType.STATUS.getName()));
			transactions.setSubMerchantId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
		}
		
		return transactions;
		
	} catch (Exception e) {
			
				logger.error("Exception in DB : " , e);
		}
		return transactions;
	}
	
	public TransactionSearch getSaleCaptureTransactionByOrderId(String orderId) throws SystemException {
		
		TransactionSearch transactions = new TransactionSearch();
		try{
		
		MongoDatabase dbIns = mongoInstance.getDB();
		
		MongoCollection<Document> coll = dbIns
				.getCollection(PropertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
		
		BasicDBObject finalQuery = new BasicDBObject();
		finalQuery.put(FieldType.ORDER_ID.getName(), orderId);
		finalQuery.put(FieldType.TXNTYPE.getName(), TxnType.SALE.getName());
		finalQuery.put(FieldType.STATUS.getName(),  StatusType.CAPTURED.getName());
		
		BasicDBObject match = new BasicDBObject("$match", finalQuery);
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.name(), -1));
		BasicDBObject limit = new BasicDBObject("$limit",1);
		List<BasicDBObject> pipeline = Arrays.asList(match, sort,limit);
		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();

		while (cursor.hasNext()) {
			Document dbobj = cursor.next();
			
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				dbobj = dataEncDecTool.decryptDocument(dbobj);
			} 
			
			transactions.setOrderId(dbobj.getString(FieldType.ORDER_ID.getName()));
			transactions.setTransactionIdString(dbobj.getString(FieldType.TXN_ID.getName()));
			transactions.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
			transactions.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
			transactions.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()));
			transactions.setCurrencyCode(dbobj.getString(FieldType.CURRENCY_CODE.getName()));
			transactions.setProductDesc(dbobj.getString(FieldType.PRODUCT_DESC.getName()));
			transactions.settDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
			transactions.setCustomerEmail(dbobj.getString(FieldType.CUST_EMAIL.getName()));
			transactions.setCustomerMobile(dbobj.getString(FieldType.CUST_PHONE.getName()));
			transactions.setCustomerName(dbobj.getString(FieldType.CUST_NAME.getName()));
			transactions.setQuantity(dbobj.getString(FieldType.QUANTITY.getName()));
			transactions.setServiceCharge(dbobj.getString(FieldType.SERVICE_CHARGE.getName()));
			transactions.setProductName(dbobj.getString(FieldType.PRODUCT_NAME.getName()));
			transactions.setPaymentMethods(dbobj.getString(FieldType.PAYMENT_TYPE.getName()));
			transactions.setMopType(dbobj.getString(FieldType.MOP_TYPE.getName()));
			transactions.setCardNumber(dbobj.getString(FieldType.CARD_MASK.getName()));
			transactions.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.getName()));
			transactions.setStatus(dbobj.getString(FieldType.STATUS.getName()));
			transactions.setSubMerchantId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
		}
		
		return transactions;
		
		} catch (Exception e) {
			
				logger.error("Exception in DB " , e);
		}
		return transactions;
	}
}
