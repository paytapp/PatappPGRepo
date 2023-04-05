package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Amitosh
 *
 */
@Component
public class InvoiceDao extends HibernateAbstractDao {
	
	
	@Autowired
	private MongoInstance mongoInstance; 
	
	@Autowired
	private DataEncDecTool dataEncDecTool;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	private static final String prefix = "MONGO_DB_";
	private static Logger logger = LoggerFactory.getLogger(InvoiceDao.class.getName());
	
	public InvoiceDao() {
		super();
	}

	@SuppressWarnings({ "unchecked", "unused" })
	public List<Invoice> getInvoiceList(String fromDate, String toDate, String merchantPayId, String userType,
			/*String invoiceNo,*/ String customerEmail, String currency, String invoiceType,String statusType, String phone, String productName,String subMerchantId, String subUserId) {
		
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		String dateFrom=fromDate+" "+"00:00:00";
		String dateTo=toDate+" "+"23:59:59";
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.INVOICE_COLLECTION_NAME.getValue()));
			
			BasicDBObject finalQuery = new BasicDBObject();
			finalQuery.put(FieldType.CREATE_DATE.getName(), BasicDBObjectBuilder.start("$gte", dateFrom)
					.add("$lte", dateTo).get());
			
			/*if(StringUtils.isNotBlank(invoiceNo))
			{
				finalQuery.put("INVOICE_NO",invoiceNo);
			}*/
			if(StringUtils.isNotBlank(phone))
			{
				finalQuery.put(FieldType.PHONE.getName(),phone);
			}
			if(StringUtils.isNotBlank(productName))
			{
				finalQuery.put(FieldType.PRODUCT_NAME.getName(),productName);
			}
			if(StringUtils.isNotBlank(customerEmail)){
				finalQuery.put(FieldType.EMAIL.getName(),customerEmail);
			}
			
			if(!merchantPayId.equalsIgnoreCase("ALL"))
			{
				finalQuery.put(FieldType.PAY_ID.getName(),merchantPayId);
			}
			if(!currency.equalsIgnoreCase("ALL"))
			{
				finalQuery.put(FieldType.CURRENCY_CODE.getName(),currency);
			}
			if(!invoiceType.equalsIgnoreCase("ALL"))
			{
				finalQuery.put("INVOICE_TYPE",invoiceType);
			}
			if(!statusType.equalsIgnoreCase("ALL"))
			{
				finalQuery.put(FieldType.STATUS.getName(),statusType);
			}
			
			if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.SUB_MERCHANT_ID.getName(),subMerchantId);
			}
			if (StringUtils.isNotBlank(subUserId) && !subUserId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.SUB_USER_ID.getName(),subUserId);
			}
			
			
			
			/*
			 * List<BasicDBObject> pipeline = Arrays.asList(finalQuery);
			 * AggregateIterable<Document> output = coll.aggregate(pipeline);
			 * output.allowDiskUse(true);
			 */
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				Invoice invoice=new Invoice();
				Document dbobj = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				} 
				
				invoice.setAddress(dbobj.getString("ADDRESS"));
				invoice.setAmount(dbobj.getString("AMOUNT"));
				invoice.setBusinessName(dbobj.getString("BUSINESS_NAME"));
				invoice.setCity(dbobj.getString("CITY"));
				invoice.setCountry(dbobj.getString("COUNTRY"));
				invoice.setCreateDate(dbobj.getString("CREATE_DATE"));
				invoice.setCurrencyCode(Currency.getAlphabaticCode(dbobj.getString("CURRENCY_CODE")));
				invoice.setEmail(dbobj.getString("EMAIL"));
				invoice.setExpiresDay(dbobj.getString("EXPIRES_DAY"));
				invoice.setExpiresHour(dbobj.getString("EXPIRES_HOUR"));
				invoice.setInvoiceId(dbobj.getString("INVOICE_ID"));
				invoice.setInvoiceNo(dbobj.getString("INVOICE_NO"));
				invoice.setInvoiceType(dbobj.getString("INVOICE_TYPE"));
				invoice.setName(dbobj.getString("NAME"));
				invoice.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
				invoice.setPhone(dbobj.getString(FieldType.PHONE.getName()));
				invoice.setProductDesc(dbobj.getString("PRODUCT_DESC"));
				invoice.setProductName(dbobj.getString("PRODUCT_NAME"));
				invoice.setQuantity(dbobj.getString("QUANTITY"));
				invoice.setReturnUrl(dbobj.getString("RETURN_URL"));
				invoice.setSaltKey(dbobj.getString("SALT_KEY"));
				invoice.setServiceCharge(dbobj.getString("SERVICE_CHARGE"));
				invoice.setShortUrl(dbobj.getString("SHORT_URL"));
				invoice.setState(dbobj.getString("STATE"));
				invoice.setStatus(dbobj.getString("STATUS"));
				invoice.setTotalAmount(dbobj.getString("TOTAL_AMOUNT"));
				invoice.setZip(dbobj.getString("ZIP"));
				invoice.setSubMerchantbusinessName(dbobj.getString("SUBMERCHANT_BUSINESS_NAME"));
				invoice.setSubMerchantId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
				invoice.setSubUserId(dbobj.getString(FieldType.SUB_USER_ID.getName()));
				invoice.setUDF11(dbobj.getString("UDF11"));
				invoice.setUDF12(dbobj.getString("UDF12"));
				invoice.setUDF13(dbobj.getString("UDF13"));
				invoice.setUDF14(dbobj.getString("UDF14"));
				invoice.setUDF15(dbobj.getString("UDF15"));
				invoice.setUDF16(dbobj.getString("UDF16"));
				invoice.setUDF17(dbobj.getString("UDF17"));
				invoice.setUDF18(dbobj.getString("UDF18"));
				
				invoiceList.add(invoice);
			}
			return invoiceList;
		} catch (Exception ex) {
			logger.error("Exception while get the bin Code Low from MongoDB : " , ex);
		}
		
		return invoiceList;
	}

	
	public List<Invoice> getInvoiceListBySubUserId(String fromDate, String toDate, String merchantPayId,
			String statusType, String subUserId) {
		
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		String dateFrom=fromDate+" "+"00:00:00";
		String dateTo=toDate+" "+"23:59:59";
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.INVOICE_COLLECTION_NAME.getValue()));
			
			BasicDBObject finalQuery = new BasicDBObject();
			finalQuery.put(FieldType.CREATE_DATE.getName(), BasicDBObjectBuilder.start("$gte", dateFrom)
					.add("$lte", dateTo).get());
			
			if (StringUtils.isNotBlank(subUserId)) {
				finalQuery.put(FieldType.SUB_USER_ID.getName(),subUserId);
			}
			
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {
				Invoice invoice=new Invoice();
				Document dbobj = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbobj = dataEncDecTool.decryptDocument(dbobj);
				} 
				
				invoice.setAddress(dbobj.getString("ADDRESS"));
				invoice.setAmount(dbobj.getString("AMOUNT"));
				invoice.setBusinessName(dbobj.getString("BUSINESS_NAME"));
				invoice.setCity(dbobj.getString("CITY"));
				invoice.setCountry(dbobj.getString("COUNTRY"));
				invoice.setCreateDate(dbobj.getString("CREATE_DATE"));
				invoice.setCurrencyCode(Currency.getAlphabaticCode(dbobj.getString("CURRENCY_CODE")));
				invoice.setEmail(dbobj.getString("EMAIL"));
				invoice.setExpiresDay(dbobj.getString("EXPIRES_DAY"));
				invoice.setExpiresHour(dbobj.getString("EXPIRES_HOUR"));
				
				invoice.setInvoiceId(dbobj.getString("INVOICE_ID"));
				invoice.setInvoiceNo(dbobj.getString("INVOICE_NO"));
				invoice.setInvoiceType(dbobj.getString("INVOICE_TYPE"));
				invoice.setName(dbobj.getString("NAME"));
				invoice.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
				invoice.setPhone(dbobj.getString(FieldType.PHONE.getName()));
				invoice.setProductDesc(dbobj.getString("PRODUCT_DESC"));
				invoice.setProductName(dbobj.getString("PRODUCT_NAME"));
				invoice.setQuantity(dbobj.getString("QUANTITY"));
				invoice.setReturnUrl(dbobj.getString("RETURN_URL"));
				invoice.setSaltKey(dbobj.getString("SALT_KEY"));
				invoice.setServiceCharge(dbobj.getString("SERVICE_CHARGE"));
				invoice.setShortUrl(dbobj.getString("SHORT_URL"));
				invoice.setState(dbobj.getString("STATE"));
				invoice.setStatus(dbobj.getString("STATUS"));
				invoice.setTotalAmount(dbobj.getString("TOTAL_AMOUNT"));
				invoice.setZip(dbobj.getString("ZIP"));
				invoice.setSubMerchantbusinessName(dbobj.getString("SUBMERCHANT_BUSINESS_NAME"));
				invoice.setSubMerchantId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
				invoice.setSubUserId(dbobj.getString(FieldType.SUB_USER_ID.getName()));
				invoice.setUDF11(dbobj.getString("UDF11"));
				invoice.setUDF12(dbobj.getString("UDF12"));
				invoice.setUDF13(dbobj.getString("UDF13"));
				invoice.setUDF14(dbobj.getString("UDF14"));
				invoice.setUDF15(dbobj.getString("UDF15"));
				invoice.setUDF16(dbobj.getString("UDF16"));
				invoice.setUDF17(dbobj.getString("UDF17"));
				invoice.setUDF18(dbobj.getString("UDF18"));
				invoiceList.add(invoice);
			}
			return invoiceList;
		} catch (Exception ex) {
			logger.error("Exception while get the bin Code Low from MongoDB" , ex);
		}
	
		return invoiceList;
	}
}
