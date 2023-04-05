package com.paymentgateway.commons.user;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
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
import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Base64EncodeDecode;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

@Component
public class InvoiceTransactionDao extends HibernateAbstractDao {
	private static Logger logger = LoggerFactory.getLogger(InvoiceTransactionDao.class.getName());
	private static final String prefix = "MONGO_DB_";
	
	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private Base64EncodeDecode base64EncodeDecode;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;
	
	@Autowired
	private UserSettingDao userSettingDao;

	
	public InvoiceTransactionDao() {
		super();
	}

	public void create(Invoice invoiceTransaction) {
		/* super.save(invoiceTransaction); */
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				propertiesManager.propertiesMap.get(prefix + Constants.INVOICE_COLLECTION_NAME.getValue()));

		try {
			Document doc = new Document();
			doc.put("INVOICE_ID", invoiceTransaction.getInvoiceId());
			doc.put("PAY_ID", invoiceTransaction.getPayId());
			doc.put("BUSINESS_NAME", invoiceTransaction.getBusinessName());
			doc.put("INVOICE_NO", invoiceTransaction.getInvoiceNo());
			doc.put("NAME", invoiceTransaction.getName());
			doc.put("CITY", invoiceTransaction.getCity());
			doc.put("COUNTRY", invoiceTransaction.getCountry());
			doc.put("STATE", invoiceTransaction.getState());
			doc.put("ZIP", invoiceTransaction.getZip());
			doc.put("PHONE", invoiceTransaction.getPhone());
			doc.put("EMAIL", invoiceTransaction.getEmail());
			doc.put("ADDRESS", invoiceTransaction.getAddress());
			doc.put("PRODUCT_NAME", invoiceTransaction.getProductName());
			doc.put("PRODUCT_DESC", invoiceTransaction.getProductDesc());
			doc.put("QUANTITY", invoiceTransaction.getQuantity());
			doc.put("AMOUNT", invoiceTransaction.getAmount());
			doc.put("SERVICE_CHARGE", invoiceTransaction.getServiceCharge());
			doc.put("SALT_KEY", invoiceTransaction.getSaltKey());
			doc.put("TOTAL_AMOUNT", invoiceTransaction.getTotalAmount());
			doc.put("INVOICE_TYPE", invoiceTransaction.getInvoiceType());
			doc.put("CREATE_DATE", invoiceTransaction.getCreateDate());
			doc.put("CURRENCY_CODE", invoiceTransaction.getCurrencyCode());
			doc.put("EXPIRES_DAY", invoiceTransaction.getExpiresDay());
			doc.put("EXPIRES_HOUR", invoiceTransaction.getExpiresHour());
			doc.put("UPDATE_DATE", invoiceTransaction.getUpdateDate());
			doc.put("RETURN_URL", invoiceTransaction.getReturnUrl());
			doc.put("SHORT_URL", invoiceTransaction.getShortUrl());
			doc.put("STATUS", invoiceTransaction.getStatus());
			doc.put("UPDATE_FROM", invoiceTransaction.getDurationFrom());
			doc.put("UPDATE_TO", invoiceTransaction.getDurationTo());
			doc.put("EMAIL_STATUS", invoiceTransaction.isEmailStatus());
			doc.put("SMS_STATUS", invoiceTransaction.isSmsStatus());
			doc.put("FILE_NAME",invoiceTransaction.getFileName());
			doc.put("DURATION_FROM", invoiceTransaction.getDurationFrom());
			doc.put("DURATION_TO", invoiceTransaction.getDurationTo());
			doc.put("QR", invoiceTransaction.getQr());
			doc.put("LONG_URL", invoiceTransaction.getLongUrl());
			
			if(StringUtils.isNotBlank(invoiceTransaction.getSubMerchantId())) {
				doc.put("SUB_MERCHANT_ID", invoiceTransaction.getSubMerchantId());
			}
			if(StringUtils.isNotBlank(invoiceTransaction.getSubUserId())) {
				doc.put("SUB_USER_ID", invoiceTransaction.getSubUserId());
			}
			doc.put("SUBMERCHANT_BUSINESS_NAME", invoiceTransaction.getSubMerchantbusinessName());
			
			if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
				doc.put(FieldType.UDF11.getName(), invoiceTransaction.getUDF11());
			}
			if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
				doc.put(FieldType.UDF12.getName(), invoiceTransaction.getUDF12());
			}
			if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
				doc.put(FieldType.UDF13.getName(), invoiceTransaction.getUDF13());
			}
			if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
				doc.put(FieldType.UDF14.getName(), invoiceTransaction.getUDF14());
			}
			if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
				doc.put(FieldType.UDF15.getName(), invoiceTransaction.getUDF15());
			}
			if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
				doc.put(FieldType.UDF16.getName(), invoiceTransaction.getUDF16());
			}
			if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
				doc.put(FieldType.UDF17.getName(), invoiceTransaction.getUDF17());
			}
			if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
				doc.put(FieldType.UDF18.getName(), invoiceTransaction.getUDF18());
			}
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				coll.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				coll.insertOne(doc);
			}
			//coll.insertOne(doc);
		} catch (Exception e) {
			logger.error("Exception " , e);
		}

	}

	public void createMany(List<Invoice> invoice) {
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.INVOICE_COLLECTION_NAME.getValue()));

			List<Document> docList = new ArrayList<Document>();
			try {
			for (Invoice invoiceTransaction : invoice) {
				Document doc = new Document();
				doc.put("INVOICE_ID", invoiceTransaction.getInvoiceId());
				doc.put("PAY_ID", invoiceTransaction.getPayId());
				doc.put("BUSINESS_NAME", invoiceTransaction.getBusinessName());
				doc.put("INVOICE_NO", invoiceTransaction.getInvoiceNo());
				doc.put("NAME", invoiceTransaction.getName());
				doc.put("CITY", invoiceTransaction.getCity());
				doc.put("COUNTRY", invoiceTransaction.getCountry());
				doc.put("STATE", invoiceTransaction.getState());
				doc.put("ZIP", invoiceTransaction.getZip());
				doc.put("PHONE", invoiceTransaction.getPhone());
				doc.put("EMAIL", invoiceTransaction.getEmail());
				doc.put("ADDRESS", invoiceTransaction.getAddress());
				doc.put("PRODUCT_NAME", invoiceTransaction.getProductName());
				doc.put("PRODUCT_DESC", invoiceTransaction.getProductDesc());
				doc.put("QUANTITY", invoiceTransaction.getQuantity());
				doc.put("AMOUNT", invoiceTransaction.getAmount());
				doc.put("SERVICE_CHARGE", invoiceTransaction.getServiceCharge());
				doc.put("SALT_KEY", invoiceTransaction.getSaltKey());
				doc.put("TOTAL_AMOUNT", invoiceTransaction.getTotalAmount());
				doc.put("INVOICE_TYPE", invoiceTransaction.getInvoiceType());
				doc.put("CREATE_DATE", invoiceTransaction.getCreateDate());
				doc.put("CURRENCY_CODE", invoiceTransaction.getCurrencyCode());
				doc.put("EXPIRES_DAY", invoiceTransaction.getExpiresDay());
				doc.put("EXPIRES_HOUR", invoiceTransaction.getExpiresHour());
				doc.put("UPDATE_DATE", invoiceTransaction.getUpdateDate());
				doc.put("RETURN_URL", invoiceTransaction.getReturnUrl());
				doc.put("SHORT_URL", invoiceTransaction.getShortUrl());
				doc.put("STATUS", invoiceTransaction.getStatus());
				doc.put("UPDATE_FROM", invoiceTransaction.getDurationFrom());
				doc.put("UPDATE_TO", invoiceTransaction.getDurationTo());
				doc.put("EMAIL_STATUS", invoiceTransaction.isEmailStatus());
				doc.put("SMS_STATUS", invoiceTransaction.isSmsStatus());
				doc.put("FILE_NAME",invoiceTransaction.getFileName());
				doc.put("DURATION_FROM", invoiceTransaction.getDurationFrom());
				doc.put("DURATION_TO", invoiceTransaction.getDurationTo());
				doc.put("QR", invoiceTransaction.getQr());
				doc.put("LONG_URL", invoiceTransaction.getLongUrl());
				
				if(StringUtils.isNotBlank(invoiceTransaction.getSubMerchantId())) {
					doc.put("SUB_MERCHANT_ID", invoiceTransaction.getSubMerchantId());
				}
				if(StringUtils.isNotBlank(invoiceTransaction.getSubUserId())) {
					doc.put("SUB_USER_ID", invoiceTransaction.getSubUserId());
				}
				doc.put("SUBMERCHANT_BUSINESS_NAME", invoiceTransaction.getSubMerchantbusinessName());
				
				if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
					doc.put(FieldType.UDF11.getName(), invoiceTransaction.getUDF11());
				}
				if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
					doc.put(FieldType.UDF12.getName(), invoiceTransaction.getUDF12());
				}
				if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
					doc.put(FieldType.UDF13.getName(), invoiceTransaction.getUDF13());
				}
				if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
					doc.put(FieldType.UDF14.getName(), invoiceTransaction.getUDF14());
				}
				if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
					doc.put(FieldType.UDF15.getName(), invoiceTransaction.getUDF15());
				}
				if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
					doc.put(FieldType.UDF16.getName(), invoiceTransaction.getUDF16());
				}
				if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
					doc.put(FieldType.UDF17.getName(), invoiceTransaction.getUDF17());
				}
				if(StringUtils.isNotBlank(invoiceTransaction.getUDF11())) {
					doc.put(FieldType.UDF18.getName(), invoiceTransaction.getUDF18());
				}
				Document listDoc = new Document(doc);
				
				
					if (!docList.add(listDoc)) {
						logger.info(listDoc.getString("INVOICE_NO"));
					}
					
					
				 
				}
			}
			catch (Exception e) {
				logger.error("Exception " , e);
			}
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				coll.insertMany(dataEncDecTool.encryptDocument(docList));
			} else {
				coll.insertMany(docList);
			}
			//coll.insertMany(docList);
			logger.info(docList.size() + " Invoice inserted");

		} catch (Exception exception) {

			logger.error("Error while processing Multiple Invoice:" + exception);

		}

	}

	public void delete(Invoice invoiceTransaction) throws DataAccessLayerException {
		super.delete(invoiceTransaction);
	}

	public void update(Invoice invoiceTransaction) throws DataAccessLayerException {
		super.saveOrUpdate(invoiceTransaction);
	}

	public List<Invoice> filterFileByDate(String dateFrom,String dateTo,String merchantId, String subMerchantId, String subUserId){
		List<Invoice> invocieList=new ArrayList<>();
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.INVOICE_COLLECTION_NAME.getValue()));
			
			String fromDate=dateFrom+" "+"00:00:00";
			String toDate=dateTo+" "+"23:59:59";
			
			BasicDBObject finalQuery = new BasicDBObject();
			finalQuery.put("CREATE_DATE", BasicDBObjectBuilder.start("$gte", fromDate)
					.add("$lte", toDate).get());
			
			if(!merchantId.equalsIgnoreCase("ALL"))
			{
				finalQuery.put("PAY_ID",merchantId);
			}
			if(StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL"))
			{
				finalQuery.put(FieldType.SUB_MERCHANT_ID.getName(),subMerchantId);
			}
			if(StringUtils.isNotBlank(subUserId) && !subUserId.equalsIgnoreCase("ALL"))
			{
				finalQuery.put(FieldType.SUB_USER_ID.getName(),subUserId);
			}
			/*
			if(StringUtils.isNotBlank(phone))
			{
				finalQuery.put("PHONE",phone);
			}
			if(StringUtils.isNotBlank(productName))
			{
				finalQuery.put("PRODUCT_NAME",productName);
			}
			if(StringUtils.isNotBlank(customerEmail)){
				finalQuery.put("EMAIL",customerEmail);
			}
			
			if(!merchantPayId.equalsIgnoreCase("ALL"))
			{
				finalQuery.put("PAY_ID",merchantPayId);
			}
			if(!currency.equalsIgnoreCase("ALL"))
			{
				finalQuery.put("CURRENCY_CODE",currency);
			}
			if(!invoiceType.equalsIgnoreCase("ALL"))
			{
				finalQuery.put("INVOICE_TYPE",invoiceType);
			}
			if(!statusType.equalsIgnoreCase("ALL"))
			{
				finalQuery.put("STATUS",statusType);
			}*/
			
			
			
			
			
			
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
				invoice.setCurrencyCode(dbobj.getString("CURRENCY_CODE"));
				invoice.setEmail(dbobj.getString("EMAIL"));
				invoice.setExpiresDay(dbobj.getString("EXPIRES_DAY"));
				invoice.setExpiresHour(dbobj.getString("EXPIRES_HOUR"));
				invoice.setInvoiceId(dbobj.getString("INVOICE_ID"));
				invoice.setInvoiceNo(dbobj.getString("INVOICE_NO"));
				invoice.setInvoiceType(dbobj.getString("INVOICE_TYPE"));
				invoice.setName(dbobj.getString("NAME"));
				invoice.setPayId(dbobj.getString("PAY_ID"));
				invoice.setPhone(dbobj.getString("PHONE"));
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
				invoice.setDurationFrom(dbobj.getString("DURATION_FROM"));
				invoice.setDurationTo(dbobj.getString("DURATION_TO"));
				invoice.setEmailStatus(dbobj.getBoolean("EMAIL_STATUS"));
				invoice.setSmsStatus(dbobj.getBoolean("SMS_STATUS"));
				invoice.setFileName(dbobj.getString("FILE_NAME"));
				invoice.setQr(dbobj.getString("QR"));
				invoice.setLongUrl(dbobj.getString("LONG_URL"));
				invoice.setSubMerchantId(dbobj.getString("SUB_MERCHANT_ID"));
				invoice.setSubUserId(dbobj.getString("SUB_USER_ID"));
				invoice.setSubMerchantbusinessName(dbobj.getString("SUBMERCHANT_BUSINESS_NAME"));

				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
					invoice.setUDF11(dbobj.getString(FieldType.UDF11.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF12.getName()))) {
					invoice.setUDF12(dbobj.getString(FieldType.UDF12.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF13.getName()))) {
					invoice.setUDF13(dbobj.getString(FieldType.UDF13.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF14.getName()))) {
					invoice.setUDF14(dbobj.getString(FieldType.UDF14.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF15.getName()))) {
					invoice.setUDF15(dbobj.getString(FieldType.UDF15.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF16.getName()))) {
					invoice.setUDF16(dbobj.getString(FieldType.UDF16.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF17.getName()))) {
					invoice.setUDF17(dbobj.getString(FieldType.UDF17.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF18.getName()))) {
					invoice.setUDF18(dbobj.getString(FieldType.UDF18.getName()));
				}
			
				invocieList.add(invoice);
			}
			return invocieList;

		} catch (Exception e) {
			logger.error("Exception " , e);
			
		}
		return invocieList;
		
	}
	
	public String UpdateStatusByInvoiceId(String invoiceId, String status, boolean emailStatus, boolean smsStatus) {

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.INVOICE_COLLECTION_NAME.getValue()));

			Bson filter = new Document("INVOICE_ID", invoiceId);
			Bson newValue = new Document("STATUS", status).append("EMAIL_STATUS", emailStatus).append("SMS_STATUS", smsStatus);
			Bson updateOperationDocument = new Document("$set", newValue);
			coll.updateOne(filter, updateOperationDocument);
			logger.info("Updated Status for " + invoiceId);

		} catch (Exception e) {
			logger.error("Exception " , e);
			return "failed";
		}
		return invoiceId;
	}

	public Invoice findByInvoiceId(String invoiceId) {
		Invoice invoice = new Invoice();
		BasicDBObject finalQuery = new BasicDBObject("INVOICE_ID", invoiceId);
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.INVOICE_COLLECTION_NAME.getValue()));

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			while (cursor.hasNext()) {
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
				invoice.setCurrencyCode(dbobj.getString("CURRENCY_CODE"));
				invoice.setEmail(dbobj.getString("EMAIL"));
				invoice.setExpiresDay(dbobj.getString("EXPIRES_DAY"));
				invoice.setExpiresHour(dbobj.getString("EXPIRES_HOUR"));
				invoice.setInvoiceId(dbobj.getString("INVOICE_ID"));
				invoice.setInvoiceNo(dbobj.getString("INVOICE_NO"));
				invoice.setInvoiceType(dbobj.getString("INVOICE_TYPE"));
				invoice.setName(dbobj.getString("NAME"));
				invoice.setPayId(dbobj.getString("PAY_ID"));
				invoice.setPhone(dbobj.getString("PHONE"));
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
				invoice.setDurationFrom(dbobj.getString("DURATION_FROM"));
				invoice.setDurationTo(dbobj.getString("DURATION_TO"));
				invoice.setEmailStatus(dbobj.getBoolean("EMAIL_STATUS"));
				invoice.setSmsStatus(dbobj.getBoolean("SMS_STATUS"));
				invoice.setFileName(dbobj.getString("FILE_NAME"));
				invoice.setQr(dbobj.getString("QR"));
				invoice.setLongUrl(dbobj.getString("LONG_URL"));
				invoice.setSubMerchantId(dbobj.getString("SUB_MERCHANT_ID"));
				invoice.setSubUserId(dbobj.getString("SUB_USER_ID"));
				invoice.setSubMerchantbusinessName(dbobj.getString("SUBMERCHANT_BUSINESS_NAME"));
				
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
					invoice.setUDF11(dbobj.getString(FieldType.UDF11.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF12.getName()))) {
					invoice.setUDF12(dbobj.getString(FieldType.UDF12.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF13.getName()))) {
					invoice.setUDF13(dbobj.getString(FieldType.UDF13.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF14.getName()))) {
					invoice.setUDF14(dbobj.getString(FieldType.UDF14.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF15.getName()))) {
					invoice.setUDF15(dbobj.getString(FieldType.UDF15.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF16.getName()))) {
					invoice.setUDF16(dbobj.getString(FieldType.UDF16.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF17.getName()))) {
					invoice.setUDF17(dbobj.getString(FieldType.UDF17.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF18.getName()))) {
					invoice.setUDF18(dbobj.getString(FieldType.UDF18.getName()));
				}
				
				User user = userDao.findPayId(invoice.getPayId());
				UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
				if (user != null && merchantSettings.isAllowLogoInPgPage()){
					
					StringBuilder base64EncodeImage = new StringBuilder();
					String finalLogoLocation = null;
					
					File imageLocation = new File(
							propertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue()) + "/"
									+ invoice.getPayId());
					
					if (imageLocation.exists()) {
						
						String contents[] = imageLocation.list();
						finalLogoLocation = imageLocation.toString() + "/" + contents[0];
						if (contents[0].contains(".png")) {
							base64EncodeImage.append("data:image/png;base64,");
							base64EncodeImage.append(base64EncodeDecode.base64Encoder(new File(finalLogoLocation)));
						} else {
							base64EncodeImage.append("data:image/jpg;base64,");
							base64EncodeImage.append(base64EncodeDecode.base64Encoder(new File(finalLogoLocation)));
						}
						
						invoice.setLogo(base64EncodeImage.toString());
					}
					
				}
				
				break;
			}
		} catch (Exception e) {
			logger.error("Exception " , e);
		}
		return invoice;
	}
	public Invoice findByTxnId(String txnId) {
		Invoice invoice = new Invoice();
		BasicDBObject finalQuery = new BasicDBObject("TXN_ID", txnId);
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.PG_QR_REQUEST_COLLECTION.getValue()));

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				invoice.setAmount(dbobj.getString("AMOUNT"));
				invoice.setTotalAmount(dbobj.getString("AMOUNT"));
				invoice.setInvoiceId(dbobj.getString("ORDER_ID"));
				invoice.setPayId(dbobj.getString("PAY_ID"));
				invoice.setCurrencyCode(dbobj.getString("CURRENCY_CODE"));
				invoice.setReturnUrl(dbobj.getString("RETURN_URL"));
				invoice.setMop(dbobj.getString("MOP_TYPE"));
			}
		} catch (Exception e) {
			logger.error("Exception " , e);
		}
		return invoice;
	}

	public boolean findInvoiceNoIsExists(String invoiceNo, String payId) {
		boolean isDataAvailable = false;
		BasicDBObject finalQuery = new BasicDBObject("INVOICE_NO", invoiceNo);
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.INVOICE_COLLECTION_NAME.getValue()));

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			if (cursor.hasNext()) {
				isDataAvailable = false;
			} else {
				isDataAvailable = true;
			}
		} catch (Exception e) {
			logger.error("Exception " , e);
		}
		return isDataAvailable;
	}

	@SuppressWarnings("unchecked")
	public List<Invoice> findAllInvoiceByStatusAndPayID(String status, String payId, String subMerchantId, String subUserId) {

		
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		try {
			BasicDBObject finalQuery = new BasicDBObject();
			finalQuery.put(FieldType.STATUS.getName(), status);
			finalQuery.put(FieldType.PAY_ID.getName(), payId);
			if(StringUtils.isNotBlank(subMerchantId)){
				finalQuery.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}
			if(StringUtils.isNotBlank(subUserId)){
				finalQuery.put(FieldType.SUB_USER_ID.getName(), subUserId);
			}
			

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.INVOICE_COLLECTION_NAME.getValue()));

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			while (cursor.hasNext()) {
				Invoice invoice = new Invoice();
				Document dbobj = cursor.next();
				invoice.setAddress(dbobj.getString("ADDRESS"));
				invoice.setAmount(dbobj.getString("AMOUNT"));
				invoice.setBusinessName(dbobj.getString("BUSINESS_NAME"));
				invoice.setCity(dbobj.getString("CITY"));
				invoice.setCountry(dbobj.getString("COUNTRY"));
				invoice.setCreateDate(dbobj.getString("CREATE_DATE"));
				invoice.setCurrencyCode(dbobj.getString("CURRENCY_CODE"));
				invoice.setEmail(dbobj.getString("EMAIL"));
				invoice.setExpiresDay(dbobj.getString("EXPIRES_DAY"));
				invoice.setExpiresHour(dbobj.getString("EXPIRES_HOUR"));
				invoice.setInvoiceId(dbobj.getString("INVOICE_ID"));
				invoice.setInvoiceNo(dbobj.getString("INVOICE_NO"));
				invoice.setInvoiceType(dbobj.getString("INVOICE_TYPE"));
				invoice.setName(dbobj.getString("NAME"));
				invoice.setPayId(dbobj.getString("PAY_ID"));
				invoice.setPhone(dbobj.getString("PHONE"));
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
				invoice.setDurationFrom(dbobj.getString("DURATION_FROM"));
				invoice.setDurationTo(dbobj.getString("DURATION_TO"));
				invoice.setEmailStatus(dbobj.getBoolean("EMAIL_STATUS"));
				invoice.setSmsStatus(dbobj.getBoolean("SMS_STATUS"));
				invoice.setFileName(dbobj.getString("FILE_NAME"));
				invoice.setQr(dbobj.getString("QR"));
				invoice.setLongUrl(dbobj.getString("LONG_URL"));
				invoice.setSubMerchantId(dbobj.getString("SUB_MERCHANT_ID"));
				invoice.setSubUserId(dbobj.getString("SUB_USER_ID"));
				invoice.setSubMerchantbusinessName(dbobj.getString("SUBMERCHANT_BUSINESS_NAME"));
				
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
					invoice.setUDF11(dbobj.getString(FieldType.UDF11.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF12.getName()))) {
					invoice.setUDF12(dbobj.getString(FieldType.UDF12.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF13.getName()))) {
					invoice.setUDF13(dbobj.getString(FieldType.UDF13.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF14.getName()))) {
					invoice.setUDF14(dbobj.getString(FieldType.UDF14.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF15.getName()))) {
					invoice.setUDF15(dbobj.getString(FieldType.UDF15.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF16.getName()))) {
					invoice.setUDF16(dbobj.getString(FieldType.UDF16.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF17.getName()))) {
					invoice.setUDF17(dbobj.getString(FieldType.UDF17.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF18.getName()))) {
					invoice.setUDF18(dbobj.getString(FieldType.UDF18.getName()));
				}
				invoiceList.add(invoice);
			}
			return invoiceList;
		} catch (Exception ex) {
			logger.error("Exception while get Invoice Pending Status Data from MongoDB : " , ex);
		}
		return invoiceList;

	}

	@SuppressWarnings("unchecked")
	public List<String> findAllInvoiceNoByPayID(String payId) {

		
		List<String> invoiceList = new ArrayList<String>();
		try {
			BasicDBObject finalQuery = new BasicDBObject();
			finalQuery.put("PAY_ID", payId);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.INVOICE_COLLECTION_NAME.getValue()));
			
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			while (cursor.hasNext()) {
				
				Document dbobj = cursor.next();
				String invoiceNo=dbobj.getString("INVOICE_NO");
				
				invoiceList.add(invoiceNo);
			}
			return invoiceList;
		} catch (Exception ex) {
			logger.error("Exception while get Invoice Pending Status Data from MongoDB " , ex);
		}
		return invoiceList;

	}


	public String updateAllInvoiceStatus(String Status,List<Invoice> pendingInvoiceList) {
		try {
			
			for(Invoice in: pendingInvoiceList)
			{
			
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.INVOICE_COLLECTION_NAME.getValue()));

			Bson filter = new Document("INVOICE_ID", in.getInvoiceId());
			Bson newValue = new Document("STATUS", Status);
			Bson updateOperationDocument = new Document("$set", newValue);
			coll.updateOne(filter, updateOperationDocument);
		}
			logger.info("All data updated in the list ");
			return "sucess";
			
		} catch (Exception e) {
			logger.error("Exception " , e);
			return "failed";
		}
	}

	public List<Invoice> findAllInvoiceByFileName(String fileName) {
		
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		try {
			BasicDBObject finalQuery = new BasicDBObject();
			finalQuery.put("FILE_NAME", fileName);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.INVOICE_COLLECTION_NAME.getValue()));

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			while (cursor.hasNext()) {
				Invoice invoice = new Invoice();
				Document dbobj = cursor.next();
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
				invoice.setPayId(dbobj.getString("PAY_ID"));
				invoice.setPhone(dbobj.getString("PHONE"));
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
				invoice.setDurationFrom(dbobj.getString("DURATION_FROM"));
				invoice.setDurationTo(dbobj.getString("DURATION_TO"));
				invoice.setEmailStatus(dbobj.getBoolean("EMAIL_STATUS"));
				invoice.setSmsStatus(dbobj.getBoolean("SMS_STATUS"));
				invoice.setFileName(dbobj.getString("FILE_NAME"));
				invoice.setQr(dbobj.getString("QR"));
				invoice.setLongUrl(dbobj.getString("LONG_URL"));
				invoice.setSubMerchantId(dbobj.getString("SUB_MERCHANT_ID"));
				invoice.setSubUserId(dbobj.getString("SUB_USER_ID"));
				invoice.setSubMerchantbusinessName(dbobj.getString("SUBMERCHANT_BUSINESS_NAME"));
				
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
					invoice.setUDF11(dbobj.getString(FieldType.UDF11.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF12.getName()))) {
					invoice.setUDF12(dbobj.getString(FieldType.UDF12.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF13.getName()))) {
					invoice.setUDF13(dbobj.getString(FieldType.UDF13.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF14.getName()))) {
					invoice.setUDF14(dbobj.getString(FieldType.UDF14.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF15.getName()))) {
					invoice.setUDF15(dbobj.getString(FieldType.UDF15.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF16.getName()))) {
					invoice.setUDF16(dbobj.getString(FieldType.UDF16.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF17.getName()))) {
					invoice.setUDF17(dbobj.getString(FieldType.UDF17.getName()));
				}
				if(StringUtils.isNotBlank(dbobj.getString(FieldType.UDF18.getName()))) {
					invoice.setUDF18(dbobj.getString(FieldType.UDF18.getName()));
				}
				
				invoiceList.add(invoice);
			}
			return invoiceList;
		} catch (Exception ex) {
			logger.error("Exception while get Invoice Pending Status Data from MongoDB : " , ex);
		}
		return invoiceList;
	}
}
