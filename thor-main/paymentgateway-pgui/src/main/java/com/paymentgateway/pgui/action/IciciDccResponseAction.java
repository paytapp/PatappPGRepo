package com.paymentgateway.pgui.action;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

public class IciciDccResponseAction extends AbstractSecureAction implements ServletRequestAware {

	private static final long serialVersionUID = 5016655893890084657L;
	private static Logger logger = LoggerFactory.getLogger(IciciDccResponseAction.class.getName());
	private HttpServletRequest httpRequest;
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	public IciciDccResponseAction() {

	}

	public void setServletRequest(HttpServletRequest hReq) {
		this.httpRequest = hReq;
	}

	public String execute() {
		try {
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			Map<String, String> requestMap = new HashMap<String, String>();

			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					requestMap.put(entry.getKey(), ((String[]) entry.getValue())[0]);

				} catch (ClassCastException classCastException) {
					logger.error("Exception", classCastException);
				}
			}
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			requestMap.put(FieldType.CREATE_DATE.getName(), dateNow);
			requestMap.put(FieldType.TXN_ID.getName(), requestMap.get("oid"));

			insert(requestMap);

			return SUCCESS;

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public void insert(Map<String, String> response) {

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.ICICI_DCC_TRANSACTION.getValue()));
			BasicDBObject newFieldsObj = new BasicDBObject();

			for (String columnName : response.keySet()) {
				newFieldsObj.put(columnName, response.get(columnName));
			}
			Document doc = new Document(newFieldsObj);
			coll.insertOne(doc);
			
		} catch (Exception exception) {

			logger.error("Error when inserting data in iciciDccTransaction db", exception);
		}

	}
}
