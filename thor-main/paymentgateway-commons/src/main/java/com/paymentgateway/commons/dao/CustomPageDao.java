package com.paymentgateway.commons.dao;

import java.util.Date;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CustomPage;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;

/**
 * @author Rajit
 */


@Service
public class CustomPageDao {

	private static Logger logger = LoggerFactory.getLogger(CustomPageDao.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;	
	
	public void insert(CustomPage customPage) {
		
		try{
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.STUDENT_COLLECTION.getValue()));
		
		Document doc = createDoc(customPage);
		doc.put("_id", TransactionManager.getNewTransactionId());
		Date date = new Date();
		doc.put("CREATE_DATE", DateCreater.formatDateForDb(date));
		coll.insertOne(doc);
		} catch(Exception ex) {
			logger.error("Exception while insert custom page " , ex);
		}
	}
	
	public Document createDoc(CustomPage customPage) {
		Document document = new Document();

		document.put("PAY_ID", customPage.getPayId());
		/*document.put("MERCHANT_LOGO", customPage.isMerchantLogo());
		document.put("PG_LOGO", customPage.isPgLogo());
		document.put("MERCHANT_BANNER", customPage.isMerchantBanner());
		document.put("HEADER_TEXT", customPage.getHeaderText());
		document.put("HEADER_1", customPage.getHeader1());
		document.put("PARAGRAPH_1", customPage.getParagraph1());
		document.put("HEADER_2", customPage.getHeader2());
		document.put("PARAGRAPH_2", customPage.getParagraph2());
		document.put("MERCHANT_ADDRESS", customPage.getMerchantAddress());
		document.put("FORM_HEADER", customPage.getFormHeader());
		document.put("FIELD_1", customPage.getField1());
		document.put("FIELD_2", customPage.getField2());
		document.put("FIELD_3", customPage.getField3());
		document.put("FIELD_4", customPage.getField4());
		document.put("FIELD_5", customPage.getField5());
		document.put("FIELD_6", customPage.getField6());
		document.put("FIELD_7", customPage.getField7());
		document.put("FIELD_8", customPage.getField8());
		document.put("T&C", customPage.getTc());
		document.put("REFUND_POLICY", customPage.getRefundPolicy());
		document.put("PRIVACY_POLICY", customPage.getPrivacyPolicy());
		document.put("PG_ADDRESS", customPage.getPgAddress());
		document.put("PAY_BUTTON", customPage);*/
		document.put("CREATED_BY", customPage.getCreatedBy());
		document.put("USER_TYPE", customPage.getUserType());

		return document;
	}
}
