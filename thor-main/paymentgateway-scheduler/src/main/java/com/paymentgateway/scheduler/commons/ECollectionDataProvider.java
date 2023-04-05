package com.paymentgateway.scheduler.commons;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.bson.Document;
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
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.scheduler.tasks.AcquirerStatusEnquiry;

/**
 * @author Pooja Pancholi
 *
 */

@Service
public class ECollectionDataProvider {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ConfigurationProvider configurationProvider;
	
	private static final Logger logger = LoggerFactory.getLogger(ECollectionDataProvider.class);
	
	public List<Document> fetchECollectionData(){
		
		logger.info("Inside fetchECollectionData() By scheduler");
		
		List<Document> poojaKaDB =new ArrayList<Document>();
        
        boolean flagClosing = false;
        
		List<Document> transactionList = new ArrayList<Document>();
		//SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd"); 
		 //String todayDate=sdf.format(new Date());
		 
		//closing date
		final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date date = cal.getTime();
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = formatter.format(date);
        
		 String dateFrom=strDate + " 00:00:00"; 
		 String dateTo=strDate + " 23:59:59";
		 
		 logger.info("Previous Date Start = " + dateFrom);
	        logger.info("Previous Date End = " + dateTo);
		 
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(configurationProvider.getMONGO_DB_E_Collection());
		BasicDBObject query = new BasicDBObject();
		BasicDBObject query1 = new BasicDBObject();
		BasicDBObject queryClosing = new BasicDBObject();
		BasicDBObject queryClosing1 = new BasicDBObject();
		HashSet<String> virtualAcccountEcollection = new LinkedHashSet<String>();
		HashSet<String> virtualAcccountClosing = new LinkedHashSet<String>();
		
		query.append(FieldType.CREATE_DATE.getName(), 
				BasicDBObjectBuilder.start("$gte", new  SimpleDateFormat(dateFrom).toLocalizedPattern()) .add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
		
		query.append(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
		
		MongoCursor<Document> cursor = coll.find(query).iterator();
		
		while (cursor.hasNext()) {
			flagClosing = true;
			Document dbobj = cursor.next();		
			virtualAcccountEcollection.add(dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()));		
		}
		cursor.close();
		if(flagClosing) {
		for(String va : virtualAcccountEcollection) {
			
			BigDecimal settledAmount=new BigDecimal("0").setScale(2);
			BigDecimal closingAmount=new BigDecimal("0").setScale(2);
			BigDecimal eCollectionAmount=new BigDecimal("0").setScale(2);
			BigDecimal impsAmount=new BigDecimal("0").setScale(2);
			BigDecimal finalClosingAmount=new BigDecimal("0").setScale(2);
			
			BasicDBObject finalquery1 = new BasicDBObject();
			BasicDBObject finalquery2 = new BasicDBObject();
			BasicDBObject finalquery3 = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> paramConditionLst1 = new ArrayList<BasicDBObject>();
			List<BasicDBObject> paramConditionLst2 = new ArrayList<BasicDBObject>();
			
			if (!query.isEmpty()) {
				paramConditionLst.add(query);
			}
			paramConditionLst.add(new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(),va));
			
			if(!paramConditionLst.isEmpty()) {
				finalquery1 = new BasicDBObject("$and", paramConditionLst);
			}
			
			FindIterable<Document> iterDoc = coll.find(finalquery1);
			MongoCursor<Document> cursor1 = iterDoc.iterator();
			
			while (cursor1.hasNext()) {
				Document dbobj = cursor1.next();	
				BigDecimal dbAmount=new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				settledAmount=settledAmount.add(dbAmount);	
			}
			
			MongoDatabase dbIns1 = mongoInstance.getDB();
			MongoCollection<Document> coll1 = dbIns1
					.getCollection(configurationProvider.getMONGO_DB_closingAmountCollection());
			
			paramConditionLst1.add(new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(),va));
			
			if(!paramConditionLst1.isEmpty()) {
				finalquery2 = new BasicDBObject("$and", paramConditionLst1);
			}
			//MongoCursor<Document> cursor2 = coll1.find(finalquery2).iterator();
			BasicDBObject match = new BasicDBObject("$match", finalquery2);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CLOSING_DATE.name(), -1));
			BasicDBObject limit = new BasicDBObject("$limit",1);
			List<BasicDBObject> pipeline2 = Arrays.asList(match, sort,limit);
			AggregateIterable<Document> output2 = coll1.aggregate(pipeline2);
			output2.allowDiskUse(true);
			MongoCursor<Document> cursor3 = output2.iterator();
			
			while (cursor3.hasNext()) {
				Document dbobj = cursor3.next();
				BigDecimal dbAmount=new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				closingAmount=dbAmount;	
			}
			eCollectionAmount = settledAmount.add(closingAmount);
			
			MongoDatabase dbIns3 = mongoInstance.getDB();
			MongoCollection<Document> coll3 = dbIns3
					.getCollection(configurationProvider.getMONGO_DB_ImpsSettlementCollection());
			
			paramConditionLst2.add(new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(),va));
			
			paramConditionLst2.add(new BasicDBObject(FieldType.USER_TYPE.getName(), "Merchant Initiated Direct"));
			
			if (!query.isEmpty()) {
				paramConditionLst2.add(query);
			}
			
			if(!paramConditionLst2.isEmpty()) {
				finalquery3 = new BasicDBObject("$and", paramConditionLst2);
			}
			
			
			FindIterable<Document> iterImps = coll3.find(finalquery3);
			MongoCursor<Document> cursor4 = iterImps.iterator();
			
			while (cursor4.hasNext()) {
				Document dbobj = cursor4.next();	
				BigDecimal dbAmount=new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				impsAmount=impsAmount.add(dbAmount);	
			}
			
			finalClosingAmount = eCollectionAmount.subtract(impsAmount);
			
			String finalAmount = finalClosingAmount.toString();
			logger.info("finalAmount : " + finalAmount);
			
			//create date
		    Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			
			Document doc=new Document();
			
			doc.put(FieldType.CREATE_DATE.getName(), dateNow);
			doc.put(FieldType.CLOSING_DATE.getName(), dateFrom);		
			doc.put(FieldType.VIRTUAL_AC_CODE.getName(),va);
			doc.put(FieldType.AMOUNT.getName(),finalAmount);
			coll1.insertOne(doc);

			logger.info("finalClosingData : " + doc);
			
			poojaKaDB.add(doc);			
			
		}
		}
	
			query1.append(FieldType.CREATE_DATE.getName(), 
					BasicDBObjectBuilder.start("$gte", new  SimpleDateFormat(dateFrom).toLocalizedPattern()) .add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
			MongoDatabase dbIns1 = mongoInstance.getDB();
			MongoCollection<Document> coll1 = dbIns1
					.getCollection(configurationProvider.getMONGO_DB_closingAmountCollection());
			
			FindIterable<Document> iterImps = coll1.find(query1);
			MongoCursor<Document> cursorClosing = iterImps.iterator();
			
			while (cursorClosing.hasNext()) {
				Document dbobj = cursorClosing.next();		
				virtualAcccountClosing.add(dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()));
			}
			
			for(String va : virtualAcccountClosing) {
				
				BigDecimal settledAmount=new BigDecimal("0").setScale(2);
				BigDecimal closingAmount=new BigDecimal("0").setScale(2);
				BigDecimal eCollectionAmount=new BigDecimal("0").setScale(2);
				BigDecimal impsAmount=new BigDecimal("0").setScale(2);
				BigDecimal finalClosingAmount=new BigDecimal("0").setScale(2);
				
				List<BasicDBObject> paramConditionLstClosing = new ArrayList<BasicDBObject>();
				BasicDBObject finalqueryClosing = new BasicDBObject();
				queryClosing.append(FieldType.VIRTUAL_AC_CODE.getName(), va);
				queryClosing.append(FieldType.CREATE_DATE.getName(), 
						BasicDBObjectBuilder.start("$gte", new  SimpleDateFormat(dateFrom).toLocalizedPattern()) .add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
				
				queryClosing1.append(FieldType.VIRTUAL_AC_CODE.getName(), va);
				queryClosing1.append(FieldType.CLOSING_DATE.getName(), 
						BasicDBObjectBuilder.start("$gte", new  SimpleDateFormat(dateFrom).toLocalizedPattern()) .add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
				FindIterable<Document> iterImps1 = coll1.find(queryClosing);
				MongoCursor<Document> cursorClosingData = iterImps1.iterator();
				
				FindIterable<Document> iterImps2 = coll1.find(queryClosing1);
				MongoCursor<Document> cursorClosingData1 = iterImps2.iterator();
				
				if(cursorClosingData1.hasNext()) {
					continue;
				}
				
				while (cursorClosingData.hasNext()) {
					Document dbobj1 = cursorClosingData.next();
					BigDecimal dbAmount=new BigDecimal(dbobj1.getString(FieldType.AMOUNT.getName())).setScale(2);
					closingAmount=dbAmount;
				}
				eCollectionAmount = settledAmount.add(closingAmount);
				
				MongoDatabase dbIns3 = mongoInstance.getDB();
				MongoCollection<Document> coll3 = dbIns3
						.getCollection(configurationProvider.getMONGO_DB_ImpsSettlementCollection());
				
				paramConditionLstClosing.add(new BasicDBObject(FieldType.VIRTUAL_AC_CODE.getName(),va));
				
				paramConditionLstClosing.add(new BasicDBObject(FieldType.USER_TYPE.getName(), "Merchant Initiated Direct"));
				
				if (!query.isEmpty()) {
					paramConditionLstClosing.add(query);
				}
				
				if(!paramConditionLstClosing.isEmpty()) {
					finalqueryClosing = new BasicDBObject("$and", paramConditionLstClosing);
				}
				
				
				FindIterable<Document> iterImpsClosing = coll3.find(finalqueryClosing);
				MongoCursor<Document> cursorImps = iterImpsClosing.iterator();
				
				while (cursorImps.hasNext()) {
					Document dbobjImps = cursorImps.next();	
					BigDecimal dbAmount=new BigDecimal(dbobjImps.getString(FieldType.AMOUNT.getName())).setScale(2);
					impsAmount=impsAmount.add(dbAmount);	
				}
				
				finalClosingAmount = eCollectionAmount.subtract(impsAmount);
				
				String finalAmount = finalClosingAmount.toString();
				logger.info("finalAmount : " + finalAmount);
				
				//create date
			    Date dNow = new Date();
				String dateNow = DateCreater.formatDateForDb(dNow);
				
				Document doc1=new Document();
				
				doc1.put(FieldType.CREATE_DATE.getName(), dateNow);
				doc1.put(FieldType.CLOSING_DATE.getName(), dateFrom);
				doc1.put(FieldType.VIRTUAL_AC_CODE.getName(),va);
				doc1.put(FieldType.AMOUNT.getName(),finalAmount);
				
				coll1.insertOne(doc1);

				logger.info("finalClosingData : " + doc1);
				
				poojaKaDB.add(doc1);	
			}
		return poojaKaDB;
		
	}

}
