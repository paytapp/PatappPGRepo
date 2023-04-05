package com.paymentgateway.pgui.action;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
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
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class SendMail {
	
	
	@Autowired
	SendInvoicePdfToEmail sendInvoicePdfToEmail;
	
	
	@Autowired
	SendPdfToEmail sendPdfToEmail;
	
	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	
	private static final String prefix = "MONGO_DB_";

	
	//SendPdfToEmail to invoice mail send 
	
	private static final long serialVersionUID = 2833274410595144130L;
	private static Logger logger = LoggerFactory.getLogger(SendMail.class.getName());
	
	
	
	public  Map<String, String> sendMailHandler(Map<String, String> reqmap ,HttpServletRequest httpRequest){

		String orderId=null;
		orderId = reqmap.get("ORDER_ID");
		logger.info("Inside Handle Pay Response");
			
		Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
		Map<String, String> requestMap = new HashMap<String, String>();
		Map<String, String> responseMap = new HashMap<String, String>();
		
		for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
			try {
				requestMap.put(entry.getKey(), ((String[]) entry.getValue())[0]);
			} catch (ClassCastException classCastException) {
				logger.error("Exception", classCastException);
			}
		}
		
		
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
		BasicDBObject finalQuery = new BasicDBObject();
		finalQuery.put(FieldType.ORDER_ID.getName(), orderId);
		
		BasicDBObject match = new BasicDBObject("$match", finalQuery);
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.name(), -1));
		BasicDBObject limit = new BasicDBObject("$limit",1);
		List<BasicDBObject> pipeline = Arrays.asList(match, sort,limit);
		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
		if (cursor != null) {
			String status=null;
			Document document = cursor.next();
			// TRANSACTION_MODE according mail set (Invoice mail)
			if(StringUtils.isNotBlank(document.getString(FieldType.TRANSACTION_MODE.getName()))
					&& document.getString(FieldType.TRANSACTION_MODE.getName()).equalsIgnoreCase("invoice")) {
				status = sendInvoicePdfToEmail.customInvoice(orderId);
			}else if(StringUtils.isNotBlank(document.getString(FieldType.TRANSACTION_MODE.getName()))
					&& document.getString(FieldType.TRANSACTION_MODE.getName()).equalsIgnoreCase("ePOS")) {
				status = sendPdfToEmail.eposDetailsEmail(orderId);
			}
			
			responseMap.put("status", status);
			responseMap.put("responseMsg", (status.equalsIgnoreCase("success")?"Your pdf has been sent to your registered email id":"Unable to send email. Try again."));
		}
		
		return responseMap;
		
		
	}

	

	
}
