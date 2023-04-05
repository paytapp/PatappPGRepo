package com.paymentgateway.crm.actionBeans;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
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
import com.paymentgateway.commons.user.ReportGenerateObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class DownloadReportGenerateService {

	@Autowired
	private MongoInstance mongoInstance;

	private static final String prefix = "MONGO_DB_";
	private static Logger logger = LoggerFactory.getLogger(DownloadReportGenerateService.class.getName());

	public boolean getFileStatus(String dateFrom, String dateTo, String fileName, String merchantPayId,
			String subMerchantPayId, String subUserPayId, String createdBy) {
		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.GENERATED_REPORT_FILE_COLLECTION.getValue()));
			BasicDBObject finalQuery = new BasicDBObject();
			if (StringUtils.isNotBlank(dateFrom)) {
				finalQuery.put(FieldType.DATE_FROM.getName(), dateFrom);
			}
			if (StringUtils.isNotBlank(dateTo)) {
				finalQuery.put(FieldType.DATE_TO.getName(), dateTo);
			}
			if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.SUB_USER_ID.getName(), subUserPayId);
			}
			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.PAY_ID.getName(), merchantPayId);
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId);
			}
			if (StringUtils.isNotBlank(fileName)) {
				finalQuery.put(FieldType.FILENAME.getName(), fileName);
			}
			if (StringUtils.isNotBlank(createdBy)) {
				finalQuery.put(FieldType.CREATED_BY.getName(), createdBy);
			}
			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			if (cursor.hasNext()) {
				return true;
			}
		} catch (Exception e) {
			logger.info("Exception in getFileStatus() ", e);
		}
		return false;
	}

	
	public int fetchFileListCount(String dateFrom, String dateTo, String sessionPayId, String subMerchantPayId,
			String subUserPayId, String reportTypeName, User sessionUser) {
		File[] files = null;
		List<ReportGenerateObject> dataList = new ArrayList<ReportGenerateObject>();
		try {
			String dateFolder = dateFrom.split(" ")[0];
			
			MongoDatabase dbIns = mongoInstance.getDB();
			List<BasicDBObject> pipeline = null;

			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.GENERATED_REPORT_FILE_COLLECTION.getValue()));

			BasicDBObject finalQuery = new BasicDBObject();

			finalQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
			
			if (StringUtils.isNotBlank(reportTypeName) && !reportTypeName.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.FILE_TYPE.getName(), reportTypeName);
			}
			if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.SUB_USER_ID.getName(), subUserPayId);
			}
			if (StringUtils.isNotBlank(sessionPayId) && !sessionPayId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.PAY_ID.getName(), sessionPayId);
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId);
			}
			finalQuery.put(FieldType.CREATED_BY.getName(), sessionUser.getPayId());
			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			pipeline = Arrays.asList(match, sort);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			if (sessionUser.getUserType().equals(UserType.ADMIN)) {
				files = new File(PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
						+ "AdminCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/").listFiles();
			} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				files = new File(PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
						+ "SubAdminCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/").listFiles();
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				files = new File(PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
						+ "ResellerCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/").listFiles();
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (sessionUser.isSuperMerchant()) {
					files = new File(PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
							+ "SuperMerchantCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/").listFiles();
				} else if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					files = new File(PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
							+ "SubMerchantCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/").listFiles();
				} else {
					files = new File(PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
							+ "MerchantCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/").listFiles();
				}
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				files = new File(PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
						+ "SubUserCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/").listFiles();
			}
			while (cursor.hasNext()) {
				ReportGenerateObject data = new ReportGenerateObject();
				Document dbobj = (Document) cursor.next();
				data.setCreateDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				data.setReportFileName(dbobj.getString(FieldType.FILENAME.getName()));
				data.setDateFrom(dbobj.getString(FieldType.DATE_FROM.getName()));
				data.setDateTo(dbobj.getString(FieldType.DATE_TO.getName()));
				data.setReportTypeName(dbobj.getString(FieldType.FILE_TYPE.getName()));
				for (File file : files) {
					if (file.getName().equalsIgnoreCase(data.getReportFileName())) {
						dataList.add(data);
					}
				}
			}
		} catch (

		Exception e) {
			logger.info("Exception in fetchNetSettledFiles() ", e);
		}
		return dataList.size();
	}

	
	public List<ReportGenerateObject> fetchFileList(String dateFrom, String dateTo, String sessionPayId,
			String subMerchantPayId, String subUserPayId, String reportTypeName, int start, int length,
			User sessionUser) {
		List<ReportGenerateObject> dataList = new ArrayList<ReportGenerateObject>();
		File[] files = null;
		try {
			String dateFolder = dateFrom.split(" ")[0];
			
			MongoDatabase dbIns = mongoInstance.getDB();
			List<BasicDBObject> pipeline = null;
			BasicDBObject skip = null;
			BasicDBObject limit = null;

			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.GENERATED_REPORT_FILE_COLLECTION.getValue()));

			BasicDBObject finalQuery = new BasicDBObject();
			
			finalQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
			if (StringUtils.isNotBlank(reportTypeName) && !reportTypeName.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.FILE_TYPE.getName(), reportTypeName);
			}
			if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.SUB_USER_ID.getName(), subUserPayId);
			}
			if (StringUtils.isNotBlank(sessionPayId) && !sessionPayId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.PAY_ID.getName(), sessionPayId);
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				finalQuery.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId);
			}
			finalQuery.put(FieldType.CREATED_BY.getName(), sessionUser.getPayId());
			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			skip = new BasicDBObject("$skip", start);
			limit = new BasicDBObject("$limit", length);
			pipeline = Arrays.asList(match, sort, skip, limit);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			if (sessionUser.getUserType().equals(UserType.ADMIN)) {
				files = new File(PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
						+ "AdminCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/").listFiles();
			} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				files = new File(PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
						+ "SubAdminCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/").listFiles();
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				files = new File(PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
						+ "ResellerCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/").listFiles();
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (sessionUser.isSuperMerchant()) {
					files = new File(PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
							+ "SuperMerchantCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/").listFiles();
				} else if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					files = new File(PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
							+ "SubMerchantCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/").listFiles();
				} else {
					files = new File(PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
							+ "MerchantCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/").listFiles();
				}
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				files = new File(PropertiesManager.propertiesMap.get(Constants.REPORTS_FILE_LOCATION_URL.getValue())
						+ "SubUserCreated/" + dateFolder + "/" + sessionUser.getPayId() + "/").listFiles();
			}
			while (cursor.hasNext()) {
				ReportGenerateObject data = new ReportGenerateObject();
				Document dbobj = (Document) cursor.next();
				data.setCreateDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				data.setReportFileName(dbobj.getString(FieldType.FILENAME.getName()));
				data.setDateFrom(dbobj.getString(FieldType.DATE_FROM.getName()));
				data.setDateTo(dbobj.getString(FieldType.DATE_TO.getName()));
				data.setReportTypeName(dbobj.getString(FieldType.FILE_TYPE.getName()));
				for (File file : files) {
					if (file.getName().equalsIgnoreCase(data.getReportFileName())) {
						dataList.add(data);
					}
				}
			}
		} catch (Exception e) {
			logger.info("Exception in fetchNetSettledFiles() ", e);
		}
		return dataList;
	}

	public void insertFileStatusInDB(String fileType, String fileName, String fileLocation, String sessionPayId,
			String subMerchantPayId, String subUserPayId, String dateFrom, String dateTo, String createdby)
			throws Exception {

		SimpleDateFormat sdfcurrdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date currDate = new Date();
		String fromDate = dateFrom.split(" ")[0];
		String toDate = dateTo.split(" ")[0];
		String currentdate = sdfcurrdate.format(currDate);
		String dateIndex = DateCreater.changeDateString(currentdate);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.GENERATED_REPORT_FILE_COLLECTION.getValue()));
		BasicDBObject finalQuery = new BasicDBObject();
		if (StringUtils.isNotBlank(fileName)) {
			finalQuery.put(FieldType.FILENAME.getName(), fileName);
		}
		if (StringUtils.isNotBlank(fileLocation)) {
			finalQuery.put(FieldType.LOCATION.getName(), fileLocation);
		}

		FindIterable<Document> iterDoc = coll.find(finalQuery);
		MongoCursor<Document> cursor = iterDoc.iterator();
		if (cursor.hasNext()) {
			Document oldDoc = (Document) cursor.next();
			Bson filter = new Document(FieldType.FILENAME.getName(), oldDoc.getString(FieldType.FILENAME.getName()));
			Bson newValue = new Document(FieldType.CREATE_DATE.getName(), currentdate)
					.append(FieldType.DATE_INDEX.getName(), dateIndex);
			Bson updateDocument = new Document("$set", newValue);
			coll.updateOne(filter, updateDocument);
		} else {
			Document doc = new Document();
			if (StringUtils.isNotBlank(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
				doc.put(FieldType.SUB_USER_ID.getName(), subUserPayId);
			}
			if (StringUtils.isNotBlank(sessionPayId) && !sessionPayId.equalsIgnoreCase("ALL")) {
				doc.put(FieldType.PAY_ID.getName(), sessionPayId);
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				doc.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId);
			}
			doc.put(FieldType.CREATED_BY.getName(), createdby);
			doc.put(FieldType.FILE_TYPE.getName(), fileType);
			doc.put(FieldType.CREATE_DATE.getName(), currentdate);
			doc.put(FieldType.DATE_FROM.getName(), fromDate);
			doc.put(FieldType.DATE_TO.getName(), toDate);
			doc.put(FieldType.LOCATION.getName(), fileLocation);
			doc.put(FieldType.STATUS.getName(), "Ready");
			doc.put(FieldType.FILENAME.getName(), fileName);
			doc.put(FieldType.DATE_INDEX.getName(), dateIndex);
			coll.insertOne(doc);
		}
	}
}
