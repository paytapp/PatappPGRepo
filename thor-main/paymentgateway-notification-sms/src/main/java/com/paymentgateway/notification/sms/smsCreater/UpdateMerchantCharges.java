package com.paymentgateway.notification.sms.smsCreater;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class UpdateMerchantCharges {

	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	private static Logger logger = LoggerFactory.getLogger(UpdateMerchantCharges.class.getName());

	public void updateCharges(String dateFrom, String dateTo, String payId) {

		try {

			logger.info("Updating Merchant charges");

			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();

			String currentDate = null;

			if (!dateFrom.isEmpty()) {

				if (!dateTo.isEmpty()) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(), payId);
			BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName());
			BasicDBObject txnTypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName());

			condList.add(payIdQuery);
			condList.add(statusQuery);
			condList.add(dateQuery);
			condList.add(txnTypeQuery);

			allParamQuery.append("$and", condList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", allParamQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = null;
			pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			logger.info("Query for getting OID set " + pipeline.toString());

			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			HashSet<String> oidSet = new LinkedHashSet<String>();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				String oId = dbobj.getString(FieldType.OID.getName());
				oidSet.add(oId);
			}

			cursor.close();

			logger.info("OID Set Size =  " + oidSet.size());

			for (String oid : oidSet) {

				logger.info("Updating Merchant charges for OID " + oid);

				BasicDBObject oidQuery = new BasicDBObject(FieldType.OID.getName(), oid);
				BasicDBObject stbStatus = new BasicDBObject(FieldType.STATUS.getName(),
						StatusType.SENT_TO_BANK.getName());

				BasicDBObject innerParamQuery = new BasicDBObject();
				List<BasicDBObject> innerCondList = new ArrayList<BasicDBObject>();

				innerCondList.add(oidQuery);
				innerCondList.add(stbStatus);

				innerParamQuery.append("$and", innerCondList);

				FindIterable<Document> itr = coll.find(innerParamQuery);

				MongoCursor<Document> innerCursor = itr.iterator();

				while (innerCursor.hasNext()) {

					Document dbobj = innerCursor.next();

					String pgTdr = dbobj.getString(FieldType.PG_TDR_SC.getName());
					String pgGst = dbobj.getString(FieldType.PG_GST.getName());
					String acquirerTdr = dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName());
					String acquirerGst = dbobj.getString(FieldType.ACQUIRER_GST.getName());

					Document query = new Document();
					query.append(FieldType.OID.getName(), oid);
					query.append(FieldType.STATUS.getName(), StatusType.SETTLED.getName());
					query.append(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName());

					Document setData = new Document();
					setData.append(FieldType.PG_TDR_SC.getName(), pgTdr);
					setData.append(FieldType.PG_GST.getName(), pgGst);
					setData.append(FieldType.ACQUIRER_TDR_SC.getName(), acquirerTdr);
					setData.append(FieldType.ACQUIRER_GST.getName(), acquirerGst);

					Document update = new Document();
					update.append("$set", setData);
					coll.updateOne(query, update);

					Document queryCapture = new Document();
					queryCapture.append(FieldType.OID.getName(), oid);
					queryCapture.append(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
					query.append(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());

					logger.info("Updating Merchant charges for OID for Settled Transaction , OID = " + oid);

					Document setDataCaptured = new Document();
					setDataCaptured.append(FieldType.PG_TDR_SC.getName(), pgTdr);
					setDataCaptured.append(FieldType.PG_GST.getName(), pgGst);
					setDataCaptured.append(FieldType.ACQUIRER_TDR_SC.getName(), acquirerTdr);
					setDataCaptured.append(FieldType.ACQUIRER_GST.getName(), acquirerGst);

					Document updateCaptured = new Document();
					updateCaptured.append("$set", setData);
					coll.updateOne(queryCapture, updateCaptured);

					logger.info("Updating Merchant charges for OID for Captured Transaction , OID = " + oid);

				}

				// innerCursor.close();

			}

			logger.info("Merchant charges update for All OID ");
		}

		catch (Exception e) {
			logger.error("Exception in updating merchant charges", e);
		}

	}

}
