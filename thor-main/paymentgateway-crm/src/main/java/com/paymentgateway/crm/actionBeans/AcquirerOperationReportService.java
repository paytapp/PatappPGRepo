package com.paymentgateway.crm.actionBeans;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.DateFormat;
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
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class AcquirerOperationReportService {
	private static Logger logger = LoggerFactory.getLogger(AcquirerOperationReportService.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	public List<TransactionSearch> getRefundData(String fromDate, String toDate, String acqType, String paymentType) {

		List<TransactionSearch> transactionObj = new ArrayList<TransactionSearch>();
		List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

		try {
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDateIndex = startDate.toString().replaceAll("-", "");
			String toDateIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDateIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDateIndex).toLocalizedPattern()).get());

			paramConditionLst.add(dateIndexConditionQuery);
			paramConditionLst
					.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName()));
			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.PENDING.getName()));
			
			if (StringUtils.isNotBlank(acqType)) {
				paramConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acqType));
			}

			if (StringUtils.isNotBlank(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			BasicDBObject finalQuery = new BasicDBObject("$and", paramConditionLst);
			MongoDatabase dbIns = mongoInstance.getDB();

			List<BasicDBObject> pipeline = null;

			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
			projectElement.put(FieldType.CREATE_DATE.getName(), 1);
			projectElement.put(FieldType.ACQ_ID.getName(), 1);
			projectElement.put(FieldType.ORDER_ID.getName(), 1);
			projectElement.put(FieldType.PG_REF_NUM.getName(), 1);
			projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);
			
			BasicDBObject project = new BasicDBObject("$project", projectElement);
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			pipeline = Arrays.asList(match, project, sort);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				TransactionSearch transReport = new TransactionSearch();

				transReport.setRefundDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				transReport.setRefundAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()));
				transReport.setAcqId(dbobj.getString(FieldType.ACQ_ID.getName()));
				transReport.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.getName()));
				
				List<BasicDBObject> saleQueryArray = new ArrayList<BasicDBObject>();

				saleQueryArray.add(
						new BasicDBObject(FieldType.ORDER_ID.getName(), dbobj.getString(FieldType.ORDER_ID.getName())));
				saleQueryArray.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
				saleQueryArray.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));

				BasicDBObject saleQuery = new BasicDBObject("$and", saleQueryArray);

				BasicDBObject project2 = new BasicDBObject("$project", projectElement);

				BasicDBObject match2 = new BasicDBObject("$match", saleQuery);
				BasicDBObject sort2 = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

				List<BasicDBObject> pipeline2 = Arrays.asList(match2, project2, sort2);
				AggregateIterable<Document> output2 = coll.aggregate(pipeline2);
				MongoCursor<Document> cursor2 = output2.iterator();

				Document dbobj2 = cursor2.next();

				transReport.setTransactionCaptureDate(dbobj2.getString(FieldType.CREATE_DATE.getName()));
				transReport.setAmount(dbobj2.getString(FieldType.TOTAL_AMOUNT.getName()));
				
				transactionObj.add(transReport);
			}

		} catch (Exception ex) {
			logger.error("Exception Cought in AcquireOperationReportService : ", ex);
		}
		return transactionObj;
	}

	public String getFileChecksum(MessageDigest digest, FileInputStream fis) throws IOException {

		// Create byte array to read data in chunks
		byte[] byteArray = new byte[1024];
		int bytesCount = 0;

		// Read file data and update in message digest
		while ((bytesCount = fis.read(byteArray)) != -1) {
			digest.update(byteArray, 0, bytesCount);
		}

		// Get the hash's bytes
		byte[] bytes = digest.digest();

		// This bytes[] has bytes in decimal format;
		// Convert it to hexadecimal format
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		// return complete hash
		return sb.toString().toUpperCase();
	}
}
