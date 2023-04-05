package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

/**
 * @auther Sandeep Sharma
 */

public class DownloadMonthlyInvoiceAction extends AbstractSecureAction {

	private static final long serialVersionUID = 7955841346961860470L;
	private static Logger logger = LoggerFactory.getLogger(DownloadMonthlyInvoiceAction.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	private InputStream fileInputStream;
	private String filename;
	private String invoiceMonth;
	private String invoiceNo;
	private String payId;
	private String subMerchantPayId;
	private long contentLength;

	public String execute() {

		List<String> filenames = new ArrayList<String>();

		boolean dbexist = getFileStatus(payId, subMerchantPayId, invoiceMonth, invoiceNo, filename);
		String invoiceLocation = "/home/Properties/tempFileLocation/Invoice/";
		File[] files = new File(invoiceLocation).listFiles();
		for (File file : files) {
			filenames.add(file.getName());
		}
		if (filenames.contains(filename) && dbexist == true) {
			try {

				String pdfFile = invoiceLocation + filename;
				File file = new File(pdfFile);
				FileInputStream inputStream = new FileInputStream(file);
				contentLength = file.length();

				setFileInputStream(inputStream);

			} catch (IOException ioe) {
				logger.error("Error in getting saved file: ", ioe);
			}

		}
		return SUCCESS;
	}

	public boolean getFileStatus(String payId, String submerchnatPayId, String invoiceMonth, String invoiceNo,
			String filename) {

		boolean recordexist = false;

		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject payid = new BasicDBObject(FieldType.MERCHANT_ID.getName(), payId);
			if (StringUtils.isNotBlank(submerchnatPayId)) {
				BasicDBObject submerchantpayid = new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
						submerchnatPayId);
				paramConditionLst.add(submerchantpayid);
			}
			BasicDBObject fileName = new BasicDBObject("FILENAME", filename);
			BasicDBObject invoicemonth = new BasicDBObject("INVOICE_MONTH", invoiceMonth);
			BasicDBObject invoiceno = new BasicDBObject("INVOICE_NO", invoiceNo);
			BasicDBObject status = new BasicDBObject(FieldType.STATUS.getName(), "Ready");
			paramConditionLst.add(payid);
			paramConditionLst.add(fileName);
			paramConditionLst.add(invoicemonth);
			paramConditionLst.add(invoiceno);
			paramConditionLst.add(status);

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);

			logger.info("Inside MSEDCLDataFileStatus finalquery = " + finalquery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.MONTHLY_INVOICE_FILE_STATUS_COLLECTION.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);

			List<BasicDBObject> pipeline = Arrays.asList(match);
			logger.info(pipeline.toString());

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				recordexist = true;

			}
			cursor.close();
		} catch (Exception e) {
			logger.error("Exception ", e);
		}

		return recordexist;

	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getInvoiceMonth() {
		return invoiceMonth;
	}

	public void setInvoiceMonth(String invoiceMonth) {
		this.invoiceMonth = invoiceMonth;
	}

	public String getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

}
