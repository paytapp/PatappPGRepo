package com.paymentgateway.crm.action;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;


@Service
public class EventInvoiceFetchService extends AbstractSecureAction{
	
	private static final long serialVersionUID = -5237691376891519457L;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;
	
	private File eventInvoice;
	private String orderId;
	private String payId;
	private Map<String,String> aaData;
	
	private static final String prefix = "MONGO_DB_";
	private static Logger logger = LoggerFactory.getLogger(EventInvoiceFetchService.class.getName());
	
	public String execute() {
		
try {
			
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.EVENT_INVOICE_COLLECTION.getValue()));
			
			BasicDBObject orderIdQuery = new BasicDBObject("ORDER_ID",orderId);
			BasicDBObject payIdQuery = new BasicDBObject("PAY_ID",payId);
			
			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			
			conditionList.add(payIdQuery);
			conditionList.add(orderIdQuery);
			
			BasicDBObject finalQuery = new BasicDBObject("$and",conditionList);
			
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			
			while (cursor.hasNext()) {
				
				Document doc = cursor.next();
				
				Map<String,String> dataMap = new HashMap<String,String>();
				
				dataMap.put("base64Data",doc.getString("FILE_BASE_64"));
				dataMap.put("dataType",doc.getString("DATA_TYPE"));
				
				setAaData(dataMap);
				
 				break;
			}
			
			return SUCCESS;
			
		}
		
		catch(Exception e) {
			logger.error("Exception in getting event page invoice ",e);
			return SUCCESS;
		}
		
		
	}

	
	
	public File getEventInvoice() {
		return eventInvoice;
	}

	public void setEventInvoice(File eventInvoice) {
		this.eventInvoice = eventInvoice;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public Map<String, String> getAaData() {
		return aaData;
	}

	public void setAaData(Map<String, String> aaData) {
		this.aaData = aaData;
	}

}
