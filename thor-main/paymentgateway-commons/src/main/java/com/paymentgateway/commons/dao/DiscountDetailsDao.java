package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.Discount;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionManager;
/*
 * @author Rajit
*/
@Service
public class DiscountDetailsDao {
	
	private static Logger logger = LoggerFactory.getLogger(DiscountDetailsDao.class.getName());

	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userDao;
	
	public void insert(Discount discount) {
		
		logger.info("insert new document ");
		
		Date date = new Date();
		String currentDate = DateCreater.formatDateForDb(date);
		
		Document docToInsert = discountTodoc(discount);
		docToInsert.put("_id", TransactionManager.getNewTransactionId());
		docToInsert.put("CREATE_DATE", currentDate);
		
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.DISCOUNT_COLLECTION.getValue()));
		
		coll.insertOne(docToInsert);
	}
	
	
	public Boolean checkAlreadyExist(String discountApplicableOn, String discount, /*String discountType,*/ String paymentType, String issuerBank, String mopType,
			String paymentRegion, String cardHolderType, String amountSlab, String emiDuration, String status) {
		
		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
		BasicDBObject finalQuery = new BasicDBObject();
		
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.DISCOUNT_COLLECTION.getValue()));
		
		queryList.add(new BasicDBObject("DISCOUNT_APPLICABLE_ON", discountApplicableOn));
		if (discountApplicableOn.equalsIgnoreCase("PaymentGateway")) {
		} else {
			queryList.add(new BasicDBObject("DISCOUNT", discount));
		}
		//queryList.add(new BasicDBObject("DISCOUNT_TYPE", discountType));
		queryList.add(new BasicDBObject("PAYMENT_TYPE", paymentType));
		queryList.add(new BasicDBObject("MOP_TYPE", mopType));
		queryList.add(new BasicDBObject("PAYMENT_REGION", paymentRegion));
		queryList.add(new BasicDBObject("AMOUNT_SLAB", amountSlab));
		queryList.add(new BasicDBObject("STATUS", status));
		
		if (StringUtils.isNotBlank(emiDuration) && !emiDuration.equalsIgnoreCase("NA")) {
			queryList.add(new BasicDBObject("EMI_DURATION", emiDuration));
		}
		
		if (StringUtils.isNotBlank(issuerBank) && !issuerBank.equalsIgnoreCase("NA")) {
			queryList.add(new BasicDBObject("ISSUER_BANK", issuerBank));
		}
		
		if (StringUtils.isNotBlank(cardHolderType) && !cardHolderType.equalsIgnoreCase("NA")) {
			queryList.add(new BasicDBObject("CARD_HOLDER_TYPE", cardHolderType));
		}
		
		finalQuery.append("$and", queryList);
		
		FindIterable<Document> iterDoc = coll.find(finalQuery);
		MongoCursor<Document> cursor = iterDoc.iterator();
		while (cursor.hasNext()) {
			return true;
		}
		return false;
	}
	
	public List<Discount> getAllActiveDetails() {
		
		List<Discount> discountList = new ArrayList<Discount>();
		BasicDBObject finalQuery = new BasicDBObject();
		
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.DISCOUNT_COLLECTION.getValue()));
		
		finalQuery.put("STATUS", TDRStatus.ACTIVE.getName());
		
		BasicDBObject match = new BasicDBObject("$match", finalQuery);
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		
		List<BasicDBObject> pipeline = Arrays.asList(match, sort);

		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
		
		while (cursor.hasNext()) {
			Document dbObj = cursor.next();
			Discount discount = new Discount();
			
			discount.setDiscountApplicableOn(dbObj.getString("DISCOUNT_APPLICABLE_ON"));
			discount.setDiscountType(dbObj.getString("DISCOUNT_TYPE"));
			discount.setPaymentType(dbObj.getString("PAYMENT_TYPE"));
			discount.setFixedCharges(dbObj.getString("FIXED_CHARGES"));
			discount.setPercentageCharges(dbObj.getString("PERCENTAGE_CHARGES"));
			discount.setIssuerBank(dbObj.getString("ISSUER_BANK"));
			discount.setMopType(dbObj.getString("MOP_TYPE"));
			discount.setPaymentRegion(dbObj.getString("PAYMENT_REGION"));
			discount.setCardHolderType(dbObj.getString("CARD_HOLDER_TYPE"));
			discount.setSlab(dbObj.getString("AMOUNT_SLAB"));
			discount.setEmiDuration(dbObj.getString("EMI_DURATION"));
			
			if(StringUtils.isNotBlank(dbObj.getString("DISCOUNT_APPLICABLE_ON")) && dbObj.getString("DISCOUNT_APPLICABLE_ON").equalsIgnoreCase("MERCHANT")) {
				
				discount.setDiscount(userDao.getBusinessNameByPayId(dbObj.getString("DISCOUNT")));
			
			} else {
				discount.setDiscount(dbObj.getString("DISCOUNT"));
			}
			
			
			discountList.add(discount);
		}
		
		return discountList;
	}
	
	public void inActiveDiscountDetail(String applicableOn, String discount, String discountType, String paymentType, String issuerBank, String mopType, String paymentRegion, String cardHolderType,
			String amountSlab, String emiDuration, String loginEmailId) {
		
		String id = null;
		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
		
		if(applicableOn.equalsIgnoreCase("PaymentGateway")) {
			discount = applicableOn;
			queryList.add(new BasicDBObject("DISCOUNT", discount));
		
		} else {
			queryList.add(new BasicDBObject("DISCOUNT", discount));
		}
		queryList.add(new BasicDBObject("DISCOUNT_TYPE", discountType));
		queryList.add(new BasicDBObject("PAYMENT_TYPE", paymentType));
		if (!issuerBank.equalsIgnoreCase("NA")) {
			queryList.add(new BasicDBObject("ISSUER_BANK", issuerBank));
		}
		queryList.add(new BasicDBObject("MOP_TYPE", mopType));
		queryList.add(new BasicDBObject("PAYMENT_REGION", paymentRegion));
		queryList.add(new BasicDBObject("CARD_HOLDER_TYPE", cardHolderType));
		queryList.add(new BasicDBObject("AMOUNT_SLAB", amountSlab));

		if (!emiDuration.equalsIgnoreCase("NA")) {
			queryList.add(new BasicDBObject("EMI_DURATION", emiDuration));
		}
		queryList.add(new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName()));
		
		BasicDBObject finalQuery = new BasicDBObject("$and", queryList);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.DISCOUNT_COLLECTION.getValue()));

		FindIterable<Document> iterDoc = coll.find(finalQuery);
		MongoCursor<Document> cursor = iterDoc.iterator();

		while (cursor.hasNext()) {
			Document dbobj = cursor.next();
				id = dbobj.getString("_id");
				break;		
		}
		cursor.close();
		
		Date date = new Date();
		String currentDate = DateCreater.formatDateForDb(date);
		Document query = new Document();
		query.append("_id", id);
		Document setData = new Document();
		setData.append("STATUS",TDRStatus.INACTIVE.getName());
		setData.append("UPDATED_BY", loginEmailId);
		setData.append("UPDATE_DATE", currentDate);
		Document update = new Document();
		update.append("$set", setData);
		coll.updateOne(query, update);
	}
	
	public List<Discount> getFilteredDetails(String discountApplicableOn, String  filteredValue) {
		
		List<Discount> discountList = new ArrayList<Discount>();
		BasicDBObject finalQuery = new BasicDBObject();
		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
		
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.DISCOUNT_COLLECTION.getValue()));
		
		queryList.add(new BasicDBObject("DISCOUNT_APPLICABLE_ON", discountApplicableOn));
		if (!filteredValue.equalsIgnoreCase("All") && StringUtils.isNotBlank(filteredValue)) {

				queryList.add(new BasicDBObject("DISCOUNT", filteredValue));
			
		}
		
		queryList.add(new BasicDBObject("STATUS", TDRStatus.ACTIVE.getName()));
		finalQuery.put("$and", queryList);
		
		BasicDBObject match = new BasicDBObject("$match", finalQuery);
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		
		List<BasicDBObject> pipeline = Arrays.asList(match, sort);

		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
		
		while (cursor.hasNext()) {
			Document dbObj = cursor.next();
			Discount discount = new Discount();
			
			//discount.setMerchant(userDao.getBusinessNameByPayId(dbObj.getString("MERCHANT")));
			discount.setDiscountType(dbObj.getString("DISCOUNT_TYPE"));
			discount.setPaymentType(dbObj.getString("PAYMENT_TYPE"));
			discount.setFixedCharges(dbObj.getString("FIXED_CHARGES"));
			discount.setPercentageCharges(dbObj.getString("PERCENTAGE_CHARGES"));
			if(StringUtils.isNotBlank(dbObj.getString("ISSUER_BANK"))) {
				discount.setIssuerBank(dbObj.getString("ISSUER_BANK"));
			} else {
				discount.setIssuerBank("NA");
			}
			discount.setMopType(dbObj.getString("MOP_TYPE"));
			discount.setPaymentRegion(dbObj.getString("PAYMENT_REGION"));
			discount.setCardHolderType(dbObj.getString("CARD_HOLDER_TYPE"));
			discount.setSlab(dbObj.getString("AMOUNT_SLAB"));
			if(StringUtils.isNotBlank(dbObj.getString("EMI_DURATION"))) {
				discount.setEmiDuration(dbObj.getString("EMI_DURATION"));
			} else {
				discount.setEmiDuration("NA");
			}
			
			if(dbObj.getString("DISCOUNT_APPLICABLE_ON").equalsIgnoreCase("MERCHANT")) {
				discount.setDiscount(userDao.getBusinessNameByPayId(dbObj.getString("DISCOUNT")));
			} else {
				discount.setDiscount(dbObj.getString("DISCOUNT"));
			}
			
			discount.setDiscountApplicableOn(dbObj.getString("DISCOUNT_APPLICABLE_ON"));
			
			discountList.add(discount);
		}
		
		return discountList;
	}
	
	
	public Document discountTodoc(Discount dis) {
		
		Document document = new Document();
		
		if(StringUtils.isNotBlank(dis.getDiscountApplicableOn())) {
			document.put("DISCOUNT_APPLICABLE_ON", dis.getDiscountApplicableOn());
		} else {
			document.put("DISCOUNT_APPLICABLE_ON", "NA");
		}
		
		if(StringUtils.isNotBlank(dis.getDiscount())) {
			document.put("DISCOUNT", dis.getDiscount());
		}
		
		if(StringUtils.isNotBlank(dis.getDiscountType())) {
			document.put("DISCOUNT_TYPE", dis.getDiscountType());
		} else {
			document.put("DISCOUNT_TYPE", "NA");
		}
		
		if(StringUtils.isNotBlank(dis.getPaymentType())) {
			document.put("PAYMENT_TYPE", dis.getPaymentType());
		} else {
			document.put("PAYMENT_TYPE", "NA");
		}
		
		if(StringUtils.isNotBlank(dis.getIssuerBank())) {
			document.put("ISSUER_BANK", dis.getIssuerBank());
		} else {
			document.put("ISSUER_BANK", "NA");
		}

		if(StringUtils.isNotBlank(dis.getMopType())) {
			document.put("MOP_TYPE", dis.getMopType());
		} else {
			document.put("MOP_TYPE", "NA");
		}
		
		if(StringUtils.isNotBlank(dis.getPaymentRegion())) {
			document.put("PAYMENT_REGION", dis.getPaymentRegion());
		} else {
			document.put("PAYMENT_REGION", "NA");
		}
		
		if(StringUtils.isNotBlank(dis.getCardHolderType())) {
			document.put("CARD_HOLDER_TYPE", dis.getCardHolderType());
		} else {
			document.put("CARD_HOLDER_TYPE", "NA");
		}
		
		if(StringUtils.isNotBlank(dis.getSlab())) {
			document.put("AMOUNT_SLAB", dis.getSlab());
		} else {
			document.put("AMOUNT_SLAB", "NA");
		}
		
		if(StringUtils.isNotBlank(dis.getFixedCharges())) {
			document.put("FIXED_CHARGES", dis.getFixedCharges());
		} else {
			document.put("FIXED_CHARGES", "0.00");
		}
		
		if(StringUtils.isNotBlank(dis.getPercentageCharges())) {
			document.put("PERCENTAGE_CHARGES", dis.getPercentageCharges());
		} else {
			document.put("PERCENTAGE_CHARGES", "0.00");
		}
		
		if(StringUtils.isNotBlank(dis.getStatus())) {
			document.put("STATUS", dis.getStatus());
		} else {
			document.put("STATUS", "NA");
		}
		
		if(StringUtils.isNotBlank(dis.getRequestedBy())) {
			document.put("REQUESTED_BY", dis.getRequestedBy());
		} else {
			document.put("REQUESTED_BY", "NA");
		}
		
		if(StringUtils.isNotBlank(dis.getEmiDuration())) {
			document.put("EMI_DURATION", dis.getEmiDuration());
		} else {
			document.put("EMI_DURATION", "NA");
		}
		
		return document;
	}
}
