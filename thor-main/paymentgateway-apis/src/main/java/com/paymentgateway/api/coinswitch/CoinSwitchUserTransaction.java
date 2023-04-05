package com.paymentgateway.api.coinswitch;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.CoinSwitchTransactionObject;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class CoinSwitchUserTransaction {

	private static Logger logger = LoggerFactory.getLogger(CoinSwitchUserTransaction.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	@SuppressWarnings("static-access")
	public List<CoinSwitchTransactionObject> fetchCustomerTransaction(Fields fields) {
		List<CoinSwitchTransactionObject> fetchResponse = new ArrayList<CoinSwitchTransactionObject>();
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			DateFormat formatdate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
			Date dateStart = null;
			Date dateEnd = null;
			try {
				dateStart = formatdate.parse(fields.get(FieldType.DATE_FROM.getName()));
				dateEnd = formatdate.parse(fields.get(FieldType.DATE_TO.getName()));
			} catch (ParseException e) {
				logger.error("Exception in date parsing ", e);
			}

			String startString = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + " 00:00:00";
			String endString = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + " 23:59:59";

			BasicDBObject dateQuery = new BasicDBObject();
			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startString).toLocalizedPattern())
							.add("$lt", new SimpleDateFormat(endString).toLocalizedPattern()).get());
			paramConditionLst.add(dateQuery);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}
			BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);
			BasicDBObject dateIndexQuery = new BasicDBObject(FieldType.DATE_INDEX.getName(), dateIndexIn);
			paramConditionLst.add(dateIndexQuery);

			BasicDBObject virtualAccNoObj = new BasicDBObject(FieldType.VIRTUAL_ACC_NUM.getName(),
					fields.get(FieldType.VIRTUAL_ACC_NUM.getName()));
			paramConditionLst.add(virtualAccNoObj);

			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_ID.getName()))) {
				BasicDBObject custIdObj = new BasicDBObject(FieldType.CUST_ID.getName(),
						fields.get(FieldType.CUST_ID.getName()));
				paramConditionLst.add(custIdObj);
			}
			if (StringUtils.isNotBlank(fields.get(FieldType.PHONE_NO.getName()))) {
				BasicDBObject phnNoObj = new BasicDBObject(FieldType.PHONE_NO.getName(),
						fields.get(FieldType.PHONE_NO.getName()));
				paramConditionLst.add(phnNoObj);
			}
			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
				BasicDBObject custEmailObj = new BasicDBObject(FieldType.CUST_EMAIL.getName(),
						fields.get(FieldType.CUST_EMAIL.getName()));
				paramConditionLst.add(custEmailObj);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final query for Coin Switch Txn data = " + finalquery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.COINSWITCH_TRANSACTION_DATA.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			// logger.info(pipeline.toString());

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				} 
				
				CoinSwitchTransactionObject coinSwitchTransactionObject = new CoinSwitchTransactionObject();

				coinSwitchTransactionObject.setCustId(dbobj.getString(FieldType.CUST_ID.getName()));
				coinSwitchTransactionObject.setPhoneNo(dbobj.getString(FieldType.PHONE_NO.getName()));
				coinSwitchTransactionObject.setCustName(dbobj.getString(FieldType.CUST_NAME.getName()));
				coinSwitchTransactionObject.setVirtualAccountNo(dbobj.getString(FieldType.VIRTUAL_ACC_NUM.getName()));
				coinSwitchTransactionObject.setAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()));
				coinSwitchTransactionObject.setTxnType(dbobj.getString(FieldType.TXNTYPE.getName()));
				coinSwitchTransactionObject.setRrn(dbobj.getString(FieldType.RRN.getName()));
				coinSwitchTransactionObject.setPaymentType(dbobj.getString(FieldType.PAYMENT_TYPE.getName()));
				coinSwitchTransactionObject.setCreateDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				coinSwitchTransactionObject.setStatus(dbobj.getString(FieldType.STATUS.getName()));

				fetchResponse.add(coinSwitchTransactionObject);
			}

		} catch (Exception e) {
			logger.error("Exception in fetching transaction data >> ", e);
		}

		return fetchResponse;
	}

}
