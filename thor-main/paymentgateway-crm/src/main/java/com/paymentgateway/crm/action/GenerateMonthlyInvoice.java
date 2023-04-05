package com.paymentgateway.crm.action;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MonthlyInvoiceObject;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @auther Sandeep Sharma
 */

public class GenerateMonthlyInvoice extends AbstractSecureAction {

	private static final long serialVersionUID = -7199723893903067264L;
	private static Logger logger = LoggerFactory.getLogger(GenerateMonthlyInvoice.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private MonthlyInvoiceAction monthlyInvoice;

	@Autowired
	private UserDao userdao;

	@Autowired
	private PropertiesManager propertiesManager;

	private List<MonthlyInvoiceObject> aaData = new ArrayList<MonthlyInvoiceObject>();
	private String date;
	private String payId;
	private String subMerchantPayId;
	private String invoiceNo;
	private String hsnSac;
	private String status;
	private String newfile;
	private String monthFrom;
	private String monthTo;

	public String execute() {
		logger.info("inside generate monthly invoice method");
		if (newfile != null) {
			try {
				String filename = null;
				if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
					filename = "MonthlyInvoice_" + subMerchantPayId + "_" + date + ".pdf";
				} else {
					filename = "MonthlyInvoice_" + payId + "_" + date + ".pdf";
				}
				String invoiceLocation = "/home/Properties/tempFileLocation/Invoice/";
				File[] files = new File(invoiceLocation).listFiles();
				for (File file : files) {
					if (file.getName().equalsIgnoreCase(filename)) {
						file.delete();
					}
				}
				updateFileStatusDelete(payId, subMerchantPayId, date, invoiceNo, filename);
				setStatus("");
				Runnable runnable = new Runnable() {
					@Override
					public void run() {

						try {
							insertFileStatusProcessing(payId, subMerchantPayId, date, invoiceNo, hsnSac);
							monthlyInvoice.generate(payId, subMerchantPayId, date, invoiceNo, hsnSac);
						} catch (Exception e) {
							logger.error("Exception in runnable class inside generateB60File ", e);
						}
					}
				};

				propertiesManager.executorImpl(runnable);

			} catch (Exception e1) {
				logger.error("Exception in parsing date in GenerateB60File ", e1);
			}

		} else {
			logger.info("hello this is an execute method of GenerateB60File class");
			List<String> filenames = new ArrayList<String>();
			Map<String, String> getdbStatus = getFileStatus(payId, subMerchantPayId, date, invoiceNo);
			String invoiceLocation = "/home/Properties/tempFileLocation/Invoice/";
			if (!getdbStatus.isEmpty()) {
				if (getdbStatus.get("Status").equalsIgnoreCase("Ready")) {
					File[] files = new File(invoiceLocation).listFiles();
					for (File file : files) {
						filenames.add(file.getName());
					}
					if (filenames.contains(getdbStatus.get("Filename"))) {
						setStatus("READY");
					} else {
						updateFileStatusDelete(payId, subMerchantPayId, date, invoiceNo, getdbStatus.get("Filename"));
						setStatus("");
						Runnable runnable = new Runnable() {

							@Override
							public void run() {

								try {
									insertFileStatusProcessing(payId, subMerchantPayId, date, invoiceNo, hsnSac);
									monthlyInvoice.generate(payId, subMerchantPayId, date, invoiceNo, hsnSac);
								} catch (Exception e) {
									logger.error("Exception in runnable class inside generateB60File ", e);
								}
							}
						};

						propertiesManager.executorImpl(runnable);

					}
				} else if (getdbStatus.get("Status").equalsIgnoreCase("Processing")) {
					setStatus("PROCESSING");
				} else {
					setStatus("");

					Runnable runnable = new Runnable() {

						@Override
						public void run() {

							try {
								insertFileStatusProcessing(payId, subMerchantPayId, date, invoiceNo, hsnSac);
								monthlyInvoice.generate(payId, subMerchantPayId, date, invoiceNo, hsnSac);
							} catch (Exception e) {
								logger.error("Exception in runnable class inside generateB60File ", e);
							}
						}
					};

					propertiesManager.executorImpl(runnable);

				}
			} else {
				setStatus("");

				Runnable runnable = new Runnable() {

					@Override
					public void run() {

						try {
							insertFileStatusProcessing(payId, subMerchantPayId, date, invoiceNo, hsnSac);
							monthlyInvoice.generate(payId, subMerchantPayId, date, invoiceNo, hsnSac);
						} catch (Exception e) {
							logger.error("Exception in runnable class inside generateB60File ", e);
						}
					}
				};

				propertiesManager.executorImpl(runnable);

			}
		}
		return SUCCESS;

	}

	public Map<String, String> getFileStatus(String payId, String submerchnatPayId, String invoiceMonth,
			String invoiceNo) {

		Map<String, String> fileStatus = new HashMap<String, String>();
		try {
			String filename = null;
			if (StringUtils.isNotBlank(submerchnatPayId) && !submerchnatPayId.equalsIgnoreCase("All")) {
				filename = "MonthlyInvoice_" + submerchnatPayId + "_" + invoiceMonth + ".pdf";
			} else {
				filename = "MonthlyInvoice_" + payId + "_" + invoiceMonth + ".pdf";
			}
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject payid = new BasicDBObject(FieldType.MERCHANT_ID.getName(), payId);
			if (StringUtils.isNotBlank(submerchnatPayId)) {
				BasicDBObject submerchantpayid = new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
						submerchnatPayId);
				paramConditionLst.add(submerchantpayid);
			}
			BasicDBObject fileName = new BasicDBObject("FILENAME", filename);
			BasicDBObject invoicemonth = new BasicDBObject("INVOICE_MONTH", invoiceMonth);

			paramConditionLst.add(payid);
			paramConditionLst.add(fileName);
			paramConditionLst.add(invoicemonth);

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);

			logger.info("Inside MonthlyInvoiceFileStatus finalquery = " + finalquery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.MONTHLY_INVOICE_FILE_STATUS_COLLECTION.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("INVOICE_NO", -1));
			BasicDBObject limit = new BasicDBObject("$limit", 1);

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, limit);
			logger.info(pipeline.toString());

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				fileStatus.put("Status", dbobj.getString(FieldType.STATUS.getName()));
				if (StringUtils.isNotBlank(dbobj.getString("FILENAME"))) {
					fileStatus.put("Filename", dbobj.getString("FILENAME"));
				}

			}
			cursor.close();
		} catch (Exception e) {
			logger.error("exception", e);
		}

		return fileStatus;

	}

	public void insertFileStatusProcessing(String merchantPayId, String subMerchantPayId, String invoiceMonth,
			String invoiceNo, String hsnNo) {
		try {
			SimpleDateFormat setdate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date currDate = new Date();
			String currentdate = setdate1.format(currDate);
			String filename = null;
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
				filename = "MonthlyInvoice_" + subMerchantPayId + "_" + invoiceMonth + ".pdf";
			} else {
				filename = "MonthlyInvoice_" + merchantPayId + "_" + invoiceMonth + ".pdf";
			}
			String invoiceLocation = "";
			invoiceLocation = "/home/Properties/tempFileLocation/Invoice/" + filename;
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.MONTHLY_INVOICE_FILE_STATUS_COLLECTION.getValue()));
			Document doc = new Document();

			doc.put(FieldType.MERCHANT_ID.getName(), merchantPayId);
			doc.put(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId);
			doc.put(FieldType.CREATE_DATE.getName(), currentdate);
			doc.put("LOCATION", invoiceLocation);
			doc.put("INVOICE_NO", invoiceNo);
			doc.put("INVOICE_MONTH", invoiceMonth);
			doc.put("HSN/SAC_NUMBER", hsnNo);
			doc.put(FieldType.STATUS.getName(), "Processing");
			doc.put("FILENAME", filename);

			coll.insertOne(doc);

		} catch (Exception e) {
			logger.error("Exception while inserting the data in insertFileStatusProcessing ", e);
		}

	}

	public void updateFileStatusDelete(String payId, String submerchnatPayId, String invoiceMonth, String invoiceNo,
			String filename) {
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.MONTHLY_INVOICE_FILE_STATUS_COLLECTION.getValue()));

			BasicDBObject query = new BasicDBObject().append(FieldType.MERCHANT_ID.getName(), payId)
					.append("INVOICE_MONTH", invoiceMonth).append("FILENAME", filename);
			if (StringUtils.isNotBlank(submerchnatPayId) && !submerchnatPayId.equalsIgnoreCase("All")) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(), submerchnatPayId);
			}
			coll.deleteOne(query);

		} catch (Exception ex) {
			logger.error("Exception in update delete status in updateFileStatusDelete ", ex);
		}
	}

	public String fetchAllReadyStatusData() {
		List<MonthlyInvoiceObject> listdata = new ArrayList<MonthlyInvoiceObject>();
		try {

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			if (StringUtils.isNotBlank(payId) && !payId.equalsIgnoreCase("All")) {
				BasicDBObject payIdObj = new BasicDBObject(FieldType.MERCHANT_ID.getName(), payId);
				paramConditionLst.add(payIdObj);
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
				BasicDBObject subMerchPayIdObj = new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
						subMerchantPayId);
				paramConditionLst.add(subMerchPayIdObj);
			}
			if (StringUtils.isNotBlank(monthFrom) && StringUtils.isNotBlank(monthTo)) {
				BasicDBObject monthQuery = new BasicDBObject();

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM-yyyy", Locale.ENGLISH);
				YearMonth startMonth = YearMonth.parse(monthFrom, formatter);
				YearMonth endMonth = YearMonth.parse(monthTo, formatter);
				List<String> allIndex = new ArrayList<>();
				while (!startMonth.isAfter(endMonth)) {
					allIndex.add(startMonth.format(formatter));
//				    logger.info(startDate.format(formatter));
					startMonth = startMonth.plusMonths(1);
				}
				BasicDBObject monthConditionQuery = new BasicDBObject("$in", allIndex);

				monthQuery.put("INVOICE_MONTH", monthConditionQuery);
				if (!monthQuery.isEmpty()) {
					paramConditionLst.add(monthQuery);
				}
			}
			if (StringUtils.isNotBlank(invoiceNo) && invoiceNo.length() == 9) {
				BasicDBObject invoiceNoObj = new BasicDBObject("INVOICE_NO", invoiceNo);
				paramConditionLst.add(invoiceNoObj);
			}
			if (StringUtils.isNotBlank(hsnSac)) {
				BasicDBObject hsnSacObj = new BasicDBObject("HSN/SAC_NUMBER", hsnSac);
				paramConditionLst.add(hsnSacObj);
			}

			BasicDBObject filestatus = new BasicDBObject(FieldType.STATUS.getName(), "Ready");
			paramConditionLst.add(filestatus);
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
					.get(prefix + Constants.MONTHLY_INVOICE_FILE_STATUS_COLLECTION.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			logger.info(pipeline.toString());

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			File[] files = new File("/home/Properties/tempFileLocation/Invoice/").listFiles();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				MonthlyInvoiceObject data = new MonthlyInvoiceObject();
				data.setMerchantName(userdao.getBusinessNameByPayId(dbobj.getString(FieldType.MERCHANT_ID.toString())));
				data.setMerchantPayId(dbobj.getString(FieldType.MERCHANT_ID.toString()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.toString())) && !dbobj.getString(FieldType.SUB_MERCHANT_ID.toString()).equalsIgnoreCase("All")) {
					data.setSubMerchantName(
							userdao.getBusinessNameByPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.toString())));
					data.setSubMerchantPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.toString()));
				} else {
					data.setSubMerchantName("NA");
					data.setSubMerchantPayId("");
				}
				data.setCreateDate(dbobj.getString(FieldType.CREATE_DATE.toString()));
				data.setInvoiceNo(dbobj.getString("INVOICE_NO"));
				data.setInvoiceMonth(dbobj.getString("INVOICE_MONTH"));
				data.setHsnNo(dbobj.getString("HSN/SAC_NUMBER"));
				data.setFileName(dbobj.getString("FILENAME"));
				for (File file : files) {
					if (file.getName().equalsIgnoreCase(data.getFileName())) {
						listdata.add(data);
					}
				}

			}
			cursor.close();
		} catch (Exception e) {
			logger.error("exception", e);
		}
		setAaData(listdata);
		return SUCCESS;

	}

	public List<MonthlyInvoiceObject> getAaData() {
		return aaData;
	}

	public void setAaData(List<MonthlyInvoiceObject> aaData) {
		this.aaData = aaData;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public String getHsnSac() {
		return hsnSac;
	}

	public void setHsnSac(String hsnSac) {
		this.hsnSac = hsnSac;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNewfile() {
		return newfile;
	}

	public void setNewfile(String newfile) {
		this.newfile = newfile;
	}

	public String getMonthFrom() {
		return monthFrom;
	}

	public void setMonthFrom(String monthFrom) {
		this.monthFrom = monthFrom;
	}

	public String getMonthTo() {
		return monthTo;
	}

	public void setMonthTo(String monthTo) {
		this.monthTo = monthTo;
	}

}
