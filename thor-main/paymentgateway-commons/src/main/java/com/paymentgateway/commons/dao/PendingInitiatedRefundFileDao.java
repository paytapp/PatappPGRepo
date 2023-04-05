package com.paymentgateway.commons.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.util.AWSRequestMetrics.Field;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.PendingInitiatedRefundData;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class PendingInitiatedRefundFileDao {
	
	private static Logger logger = LoggerFactory.getLogger(PendingInitiatedRefundFileDao.class.getName());

	@Autowired
	private MongoInstance mongoInstance;
	
	private final String prefix = "MONGO_DB_";
	
	public List<PendingInitiatedRefundData> findAllInitiatedRefund(String acquirerType, String refundInitiatedFrom,
			String refundInitiatedTo) {
		
		List<PendingInitiatedRefundData> initiatedRefundList=new ArrayList<PendingInitiatedRefundData>();
		
		try{
			Date currentDate=new Date();
			SimpleDateFormat reportDateFormat=new SimpleDateFormat("yyMMdd");
			SimpleDateFormat inputDateFormat=new SimpleDateFormat("dd-MM-yyyy");
			SimpleDateFormat TxnDateFormat=new SimpleDateFormat("yyyy-MM-dd");

			String dateFrom = TxnDateFormat.format(inputDateFormat.parse(refundInitiatedFrom))+" 00:00:00";
			String dateTo = TxnDateFormat.format(inputDateFormat.parse(refundInitiatedTo))+" 23:59:59";
			
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject dateIndexQuery = new BasicDBObject();
			
			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
			
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();
			fianlList.add(dateQuery);
			fianlList.add(new BasicDBObject("$or",DateCreater.getDateIndex(dateFrom, dateTo)));
			
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			
			logger.info("Final query for findAllInitiatedRefund() ",finalquery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.REFUND_PENDING_TXN_DATA.getValue()));
			
			MongoCursor<Document> cursor = collection.find(finalquery).iterator();
			
			while(cursor.hasNext()){
				
				Document docObj = cursor.next();
				
				Date parseTxnDate=DateCreater.convertStringToDateTime(docObj.getString(FieldType.TXN_DATE.getName()));
				
				PendingInitiatedRefundData pendingData=new PendingInitiatedRefundData();
				
				pendingData.setTxnCode(PropertiesManager.propertiesMap.get("SBI_NB_REFUND_CODE"));
				pendingData.setBankRefNo(docObj.getString(FieldType.RRN.getName()));
				pendingData.setRefundAmount(docObj.getString(FieldType.TOTAL_REFUND_AMOUNT.getName()));
				pendingData.setTxnAmount(docObj.getString(FieldType.TOTAL_SALE_AMOUNT.getName()));
				pendingData.setRefundDate(reportDateFormat.format(currentDate));
				pendingData.setTxnDate(reportDateFormat.format(parseTxnDate));		
				
				initiatedRefundList.add(pendingData);
				
			}
			
			
			
			
		}catch (Exception e) {
			logger.info("exception in PendingInitiatedRefundFileDao() ",e);
		}
		
		return initiatedRefundList;
	}
	
	

}
