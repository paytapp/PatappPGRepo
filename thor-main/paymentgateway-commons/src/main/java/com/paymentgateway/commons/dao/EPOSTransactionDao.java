package com.paymentgateway.commons.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.EPOSTransaction;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;

/**
 * @author Amitosh Aanand
 *
 */
@Component
public class EPOSTransactionDao {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private DataEncDecTool dataEncDecTool;
	
	private static final String prefix = "MONGO_DB_";
	private static Logger logger = LoggerFactory.getLogger(EPOSTransactionDao.class.getName());

	public EPOSTransaction findByInvoiceId(String id) {
		EPOSTransaction epos = new EPOSTransaction();
		BasicDBObject finalQuery = new BasicDBObject("INVOICE_ID", id);
		try {
			MongoDatabase dbIns = getMongoClient()
					.getDatabase(PropertiesManager.propertiesMap.get(prefix + Constants.DB_NAME.getValue()));
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.EPOS_TRANSACTION_COLLECTION.getValue()));

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					doc = dataEncDecTool.decryptDocument(doc);
				} 
				
				epos.set_id(doc.getString("_id"));
				epos.setAMOUNT(doc.getString("AMOUNT"));
				epos.setTOTAL_AMOUNT(doc.getString("TOTAL_AMOUNT"));
				epos.setCURRENCY_CODE(doc.getString("CURRENCY_CODE"));
				epos.setINVOICE_ID(doc.getString("INVOICE_ID"));
				epos.setCUST_NAME(doc.getString("CUST_NAME"));
				epos.setCUST_MOBILE(doc.getString("CUST_MOBILE"));
				epos.setCUST_EMAIL(doc.getString("CUST_EMAIL"));
				epos.setCURRENCY_CODE(doc.getString("CURRENCY_CODE"));
				epos.setEPOS_PAYMENT_OPTION(doc.getString("EPOS_PAYMENT_OPTION"));
				epos.setORIG_TXNTYPE(doc.getString("ORIG_TXNTYPE"));
				epos.setSALT_KEY(doc.getString("SALT_KEY"));
				epos.setSTATUS(doc.getString("STATUS"));
				epos.setPAY_ID(doc.getString("PAY_ID"));
				epos.setPAYMENT_URL(doc.getString("PAYMENT_URL"));
				epos.setRETURN_URL(doc.getString("RETURN_URL"));
				epos.setSHORT_URL(doc.getString("SHORT_URL"));
				epos.setBUSINESS_NAME(doc.getString("BUSINESS_NAME"));
				epos.setCREATED_BY(doc.getString("CREATED_BY"));
				epos.setCREATE_DATE(doc.getString("CREATE_DATE"));
				epos.setEXPIRY_DATE(doc.getString("EXPIRY_DATE"));
				epos.setREMARKS(doc.getString("REMARKS"));
				epos.setUDF11(doc.getString("UDF11"));
				epos.setUDF12(doc.getString("UDF12"));
				epos.setUDF13(doc.getString("UDF13"));
				epos.setUDF14(doc.getString("UDF14"));
				epos.setUDF15(doc.getString("UDF15"));
				epos.setUDF16(doc.getString("UDF16"));
				epos.setUDF17(doc.getString("UDF17"));
				epos.setUDF18(doc.getString("UDF18"));
			}
		} catch (Exception e) {
			logger.info("Exception " , e);
		}
		return epos;
	}

	public void updateEposRefundTransaction(Fields fields) {
		logger.info("Inserting refunded transaction into EPOS transaction collection");
		try {
			MongoDatabase dbIns = getMongoClient()
					.getDatabase(PropertiesManager.propertiesMap.get(prefix + Constants.DB_NAME.getValue()));
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.EPOS_TRANSACTION_COLLECTION.getValue()));

			Document doc = new Document();
			doc.put("_id", TransactionManager.getNewTransactionId());
			doc.put(FieldType.INVOICE_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
			doc.put("CUST_MOBILE", fields.getPrevious().get(FieldType.CUST_PHONE.getName()));
			doc.put(FieldType.CUST_EMAIL.getName(), fields.get(FieldType.CUST_EMAIL.getName()));
			doc.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()), "356"));
			doc.append(FieldType.TOTAL_AMOUNT.getName(), Amount.toDecimal(
					fields.get(FieldType.TOTAL_AMOUNT.getName()), fields.get(FieldType.CURRENCY_CODE.getName())));
			doc.put(FieldType.CURRENCY_CODE.getName(), fields.get(FieldType.CURRENCY_CODE.getName()));
			if ((fields.getPrevious().get(FieldType.PAYMENT_TYPE.getName())
					.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode()))
					|| (fields.getPrevious().get(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.INTERNATIONAL.getCode()))
					|| (fields.getPrevious().get(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode()))) {
				doc.put(FieldType.EPOS_PAYMENT_OPTION.getName(), "CARD");
			} else {
				doc.put(FieldType.EPOS_PAYMENT_OPTION.getName(),
						fields.getPrevious().get(FieldType.PAYMENT_TYPE.getName()));
			}

			doc.put(FieldType.ORIG_TXNTYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
			doc.put("SALT_KEY", "NA");
			doc.put(FieldType.STATUS.getName(), StatusType.toBasicStatus(fields.get(FieldType.STATUS.getName())));
			doc.put(FieldType.REFUND_ORDER_ID.getName(), fields.get(FieldType.REFUND_ORDER_ID.getName()));
			doc.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
			doc.put("PAYMENT_URL", "NA");
			doc.put(FieldType.RETURN_URL.getName(), "NA");
			doc.put("SHORT_URL", "NA");
			doc.put("BUSINESS_NAME", "");
			doc.put("CREATED_BY", "NA");
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			doc.put(FieldType.CREATE_DATE.getName(), sdf.format(cal.getTime()));
			doc.put(FieldType.EXPIRY_DATE.getName(), "NA");
			coll.insertOne(doc);
		} catch (Exception e) {
			logger.error("Exception caught while inserting refund transaction into EPOS collection, " , e);
		}
	}

	MongoClient getMongoClient() {
		String mongoURL = PropertiesManager.propertiesMap.get(prefix + Constants.MONGO_URI_PREFIX.getValue())
				+ PropertiesManager.propertiesMap.get(prefix + Constants.MONGO_USERNAME.getValue()) + ":"
				+ PropertiesManager.propertiesMap.get(prefix + Constants.MONGO_PASSWORD.getValue())
				+ PropertiesManager.propertiesMap.get(prefix + Constants.MONGO_URI_SUFFIX.getValue());
		MongoClientURI mClientURI = new MongoClientURI(mongoURL);
		return new MongoClient(mClientURI);
	}

	public void updateEposCharges(Fields fields) {
		try {
			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			conditionList
					.add(new BasicDBObject(FieldType.INVOICE_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));
			BasicDBObject finalQuery = new BasicDBObject("$and", conditionList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.EPOS_TRANSACTION_COLLECTION.getValue()));

			BasicDBObject updateFields = new BasicDBObject();
			updateFields.append(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()));
			updateFields.append(FieldType.TOTAL_AMOUNT.getName(), Amount.toDecimal(
					fields.get(FieldType.TOTAL_AMOUNT.getName()), fields.get(FieldType.CURRENCY_CODE.getName())));
			updateFields.append(FieldType.PAYMENT_TYPE.getName(), fields.get(FieldType.PAYMENT_TYPE.getName()));
			updateFields.append(FieldType.MOP_TYPE.getName(), fields.get(FieldType.MOP_TYPE.getName()));
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.append("$set", updateFields);
			collection.updateMany(finalQuery, updateQuery);

			/*
			 * updateQuery.append("$set", new
			 * BasicDBObject().append(FieldType.STATUS.getName(),
			 * fields.get(FieldType.STATUS.getName()))); //updateQuery.append("$set", new
			 * BasicDBObject().append(FieldType.MOP_TYPE.getName(),
			 * fields.get(FieldType.MOP_TYPE.getName()))); collection.updateOne(finalQuery,
			 * updateQuery);
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}