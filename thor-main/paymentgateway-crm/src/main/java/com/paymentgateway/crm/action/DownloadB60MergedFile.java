package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

public class DownloadB60MergedFile extends AbstractSecureAction {

	/**
	 * Download file from the location
	 */
	private static final long serialVersionUID = -7859561528027058198L;
	private static Logger logger = LoggerFactory.getLogger(DownloadB60MergedFile.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	private InputStream fileInputStream;
	private String filename;
	private String fromDate;
	private String toDate;
//	private String ccCode;

	private long contentLength;

	public String execute() {
		List<String> filenames = new ArrayList<String>();

		boolean dbexist = getFileStatus(fromDate, toDate, filename);
		String zipLocation = "";
		zipLocation = "/home/Properties/tempFileLocation/zip/mergedZip/";

		File[] files = new File(zipLocation).listFiles();
		for (File file : files) {
			filenames.add(file.getName());
		}
		if (filenames.contains(filename) && dbexist == true) {
			try {

				String mainzip = zipLocation + filename;
				File file = new File(mainzip);
				FileInputStream inputStream = new FileInputStream(file);
				contentLength = file.length();

				setFileInputStream(inputStream);

			} catch (IOException ioe) {
				logger.error("Error in getting saved file: ", ioe);
			}

		}

		return SUCCESS;
	}

	public boolean getFileStatus(String fromdate, String todate, String zipname) {

		boolean recordexist = false;

		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject datefrom = new BasicDBObject(FieldType.DATE_FROM.getName(), fromdate);
			BasicDBObject dateto = new BasicDBObject(FieldType.DATE_TO.getName(), todate);
			BasicDBObject name = new BasicDBObject("FILENAME", zipname);
			BasicDBObject status = new BasicDBObject(FieldType.STATUS.getName(), "Ready");
			BasicDBObject mergedquery = new BasicDBObject(FieldType.FILE_TYPE.getName(), "merged");

			paramConditionLst.add(mergedquery);
			paramConditionLst.add(datefrom);
			paramConditionLst.add(dateto);
			paramConditionLst.add(name);
			paramConditionLst.add(status);

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);

			logger.info("Inside MSEDCLDataFileStatus finalquery = " + finalquery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.MSEDCL_DATA_FILE_STATUS_COLLECTION.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);

			List<BasicDBObject> pipeline = Arrays.asList(match);
			logger.info(pipeline.toString());

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				cursor.next();
				recordexist = true;
				break;
			}
			cursor.close();
		} catch (Exception e) {
			logger.error("exception ", e);
		}

		return recordexist;

	}

	public long getContentLength() {
		return contentLength;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

}
