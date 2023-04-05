package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

public class DispatchSlipPdfFileDownloadAction extends AbstractSecureAction {

	private static final long serialVersionUID = 4572411979343169801L;
	private static Logger logger = LoggerFactory.getLogger(DispatchSlipPdfFileDownloadAction.class.getName());
	
	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;
	
	private String orderId;
	private Map<String,String> aaData;
	
	private static final String prefix = "MONGO_DB_";
	
	public String execute() {
		
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.DISPATCH_SLIP_COLLECTION_NAME.getValue()));

			/*List<BasicDBObject> saleList = new ArrayList<BasicDBObject>();
			saleList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			saleList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			BasicDBObject saleQuery = new BasicDBObject("$and", saleList);*/
			BasicDBObject saleQuery = new BasicDBObject(FieldType.ORDER_ID.getName(), orderId);
			
			FindIterable<Document> iterDoc = coll.find(saleQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();

			while (cursor.hasNext()) {

				Document doc = cursor.next();
				Map<String, String> dataMap = new HashMap<String, String>();
				if(doc.containsKey("base64_Receipt") && StringUtils.isNotBlank(doc.getString("base64_Receipt"))) {
					dataMap.put("base64Data", doc.getString("base64_Receipt"));
					dataMap.put(FieldType.ORDER_ID.getName(), orderId);
				} else {
					dataMap.put("base64Data", CrmFieldConstants.NA.getValue());
				}
				setAaData(dataMap);
				break;
			}
			return SUCCESS;
		} catch (Exception ex) {
			logger.error("Exception in getting base64 from db", ex);
			return SUCCESS;
		}
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public Map<String, String> getAaData() {
		return aaData;
	}
	public void setAaData(Map<String, String> aaData) {
		this.aaData = aaData;
	}
}