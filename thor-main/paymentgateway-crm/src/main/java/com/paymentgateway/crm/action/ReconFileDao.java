package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.introspect.TypeResolutionContext.Basic;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.ReconFile;
import com.paymentgateway.commons.util.TransactionManager;

@Service
public class ReconFileDao {

	private static Logger logger = LoggerFactory.getLogger(ReconFileDao.class.getName());

	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;

	// Add filename

	// Get Booking / Refund file names

	// Get MPR file Names

	// check if file already present

	public String insert(String filename, String fileType, String acquirer) {

		StringBuilder message = new StringBuilder();

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			Document doc = new Document();

			doc.put("id", TransactionManager.getNewTransactionId());
			doc.put("FILE_NAME", filename);
			doc.put("UPLOAD_DATE", dateNow);
			doc.put("FILE_TYPE", fileType);
			doc.put("STATUS", "Processing");

			if (StringUtils.isNotBlank(acquirer)) {
				doc.put("ACQUIRER", acquirer);
			}

			coll.insertOne(doc);

		} catch (Exception exception) {
			message.append(ErrorType.CSV_NOT_SUCCESSFULLY_UPLOAD.getResponseMessage());
			logger.error("Error while processing hotel Inventory ", exception);
		}
		return message.toString();
	}

	public String validateRefundFile(File file) {

		try {
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file.getAbsolutePath()));
			Sheet sheet = workbook.getSheetAt(0);

			int totalRows = 0;

			for (Row row : sheet) { // iterate over all rows in the sheet except 1

				if (totalRows == 0) {
					totalRows++;
					continue;
				}

				if (row.getCell(0) == null || row.getCell(1) == null || row.getCell(2) == null
						|| row.getCell(5) == null) {
					return "ERROR";
				}

			}

		} catch (Exception exception) {
			logger.error("Error while processing Refund file ", exception);
			return "ERROR";
		}

		return "SUCCESS";
	}

	public boolean checkAlreadyProcessing() {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));
			BasicDBObject statusQuery = new BasicDBObject("STATUS", "Processing");
			BasicDBObject fileQuery = new BasicDBObject("FILE_TYPE", "MPR");

			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(fileQuery);
			queryList.add(statusQuery);

			BasicDBObject query = new BasicDBObject("$and", queryList);
			long count = coll.countDocuments(query);

			if (count > 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception ex) {
			logger.error("Exception", ex);
			return false;
		}

	}

	public String validateBookingFile(File file) {

		try {
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file.getAbsolutePath()));
			Sheet sheet = workbook.getSheetAt(0);

			int totalRows = 0;

			for (Row row : sheet) { // iterate over all rows in the sheet except 1

				if (totalRows == 0) {
					totalRows++;
					continue;
				}

				if (row.getCell(0) == null || row.getCell(1) == null || row.getCell(2) == null
						|| row.getCell(4) == null) {
					return "ERROR";
				}

			}

		} catch (Exception exception) {
			logger.error("Error while processing booking file ", exception);
			return "ERROR";
		}

		return "SUCCESS";
	}

	@SuppressWarnings("unchecked")
	public boolean checkFilePresent(String filename) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

			BasicDBObject query = new BasicDBObject("FILE_NAME", filename);
			long count = coll.countDocuments(query);

			if (count > 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception ex) {
			logger.error("Exception", ex);
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	public List<ReconFile> getReconFiles(String fileType, int start, int length) {

		List<ReconFile> fileList = new ArrayList<ReconFile>();

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

			BasicDBObject finalQuery = new BasicDBObject("FILE_TYPE", fileType);

			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("UPLOAD_DATE", -1));
			BasicDBObject skip = new BasicDBObject("$skip", start);
			BasicDBObject limit = new BasicDBObject("$limit", length);
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, skip, limit);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				ReconFile inv = new ReconFile();
				inv.setId(dbobj.getString("id"));
				inv.setFilename(dbobj.get("FILE_NAME").toString());
				inv.setFileType(dbobj.get("FILE_TYPE").toString());
				inv.setStatus(dbobj.get("STATUS").toString());
				inv.setUploadDate(dbobj.get("UPLOAD_DATE").toString());
				if (fileType.equalsIgnoreCase("MPR") || fileType.equalsIgnoreCase("STATEMENT")) {
					inv.setAcquirer(dbobj.get("ACQUIRER").toString());
				}

				if (dbobj.get("TOTAL_COUNT") != null) {
					inv.setTotalCount(dbobj.get("TOTAL_COUNT").toString());
				} else {
					inv.setTotalCount("-");
				}

				if (dbobj.get("ERROR_COUNT") != null) {
					inv.setErrorCount(dbobj.get("ERROR_COUNT").toString());
				} else {
					inv.setErrorCount("-");
				}

				if (dbobj.get("SUCCESS_COUNT") != null) {
					inv.setSuccessCount(dbobj.get("SUCCESS_COUNT").toString());
				} else {
					inv.setSuccessCount("-");
				}

				fileList.add(inv);
			}
			cursor.close();
			return fileList;
		} catch (Exception ex) {
			logger.error("Exception getReconFiles ", ex);
		}
		return fileList;

	}

	@SuppressWarnings("unchecked")
	public int getReconFilesCount(String fileType) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

			BasicDBObject finalQuery = new BasicDBObject("FILE_TYPE", fileType);
			long count = coll.count(finalQuery);
			int cnt = Integer.valueOf(String.valueOf(count));
			return cnt;
		} catch (Exception ex) {
			logger.error("Exception getReconFilesCount ", ex);
		}
		return 0;

	}

	@SuppressWarnings("unchecked")
	public void uploadBookings(File file, String fileFileName, String acquirer) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

		MongoCollection<Document> excepColl = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECON_EXCEPTION_COLLECTION.getValue()));

		MongoCollection<Document> fileColl = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

		int totalRows = 0;
		int errorRows = 0;
		int successRows = 0;
		SimpleDateFormat sdf_ddMMyyyy = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat sdf_yyyyMMddhhmmss = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		ArrayList<Document> docList = new ArrayList<Document>();
		Date dNow = new Date();
		String dateNow = DateCreater.formatDateForDb(dNow);
		try {

			List<Row> results = new ArrayList<Row>();

			DataFormatter dataFormatter = new DataFormatter();
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file.getAbsolutePath()));
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = workbook.getSheetAt(0);

			for (Row row : sheet) { // iterate over all rows in the sheet except 1

				if (totalRows == 0) {
					totalRows++;
					continue;
				}
				totalRows++;
				results.add(row);
			}

			for (Row row : results) {

				Document doc = new Document();

				if (StringUtils.isBlank(dataFormatter.formatCellValue(row.getCell(0), formulaEvaluator))
						|| StringUtils.isBlank(dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator))
						|| StringUtils.isBlank(dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator))
						|| StringUtils.isBlank(dataFormatter.formatCellValue(row.getCell(4), formulaEvaluator))) {

					String createDate = sdf_yyyyMMddhhmmss.format(
							sdf_ddMMyyyy.parse(dataFormatter.formatCellValue(row.getCell(0), formulaEvaluator)));

					doc.put("CREATE_DATE", createDate);
					doc.put("DATE_INDEX", createDate.substring(0, 10).replace("-", ""));
					doc.put("TXN_DATE", createDate);
					doc.put("RESERVATION_ID", dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator));
					doc.put("BANK_TXN_NUMBER", dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator));
					doc.put("AMOUNT", dataFormatter.formatCellValue(row.getCell(4), formulaEvaluator));
					doc.put("SID", dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator));
					doc.put("FILE_NAME", fileFileName);
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("ACQUIRER", acquirer);
					doc.put("TXNTYPE", "SALE");
					doc.put("FILE_TYPE", "SALE");
					doc.put("STATUS", "Invalid");
					doc.put("RESPONSE_MESSAGE", "Row Data Invalid");
					errorRows++;
					excepColl.insertOne(doc);
					continue;
				}

				String amount = dataFormatter.formatCellValue(row.getCell(4), formulaEvaluator);

				if (dataFormatter.formatCellValue(row.getCell(4), formulaEvaluator).contains(".")) {

					String amountCellValueSplit[] = dataFormatter.formatCellValue(row.getCell(4), formulaEvaluator)
							.split("\\.");
					String amountCellValueDecimal = amountCellValueSplit[1];

					if (amountCellValueDecimal.length() == 1) {
						amount = dataFormatter.formatCellValue(row.getCell(4), formulaEvaluator) + "0";
					}
				} else {
					amount = dataFormatter.formatCellValue(row.getCell(4), formulaEvaluator) + ".00";
				}

				List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
				condList.add(new BasicDBObject("RESERVATION_ID",
						dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator)));
				condList.add(new BasicDBObject("BANK_TXN_NUMBER",
						dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator)));
				condList.add(new BasicDBObject("TXNTYPE", "SALE"));

				long count = coll.countDocuments(new BasicDBObject("$and", condList));
				int cnt = Integer.valueOf(String.valueOf(count));

				if (cnt > 0) {

					SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
					SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					String createDate = sdf2
							.format(sdf.parse(dataFormatter.formatCellValue(row.getCell(0), formulaEvaluator)));
					doc.put("CREATE_DATE", createDate);
					doc.put("TXN_DATE", createDate);
					doc.put("DATE_INDEX", createDate.substring(0, 10).replace("-", ""));
					doc.put("RESERVATION_ID", dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator));
					doc.put("BANK_TXN_NUMBER", dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator));
					doc.put("SID", dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator));
					doc.put("AMOUNT", amount);
					doc.put("FILE_NAME", fileFileName);
					doc.put("FILE_TYPE", "SALE");
					doc.put("TXNTYPE", "SALE");
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("STATUS", "Duplicate");
					doc.put("ACQUIRER", acquirer);
					doc.put("RESPONSE_MESSAGE", "Booking data found with duplicate Reservation id and Bank Txn Number");
					// errorRows++;
					excepColl.insertOne(doc);
					// continue;
				}

				String createDate = sdf_yyyyMMddhhmmss
						.format(sdf_ddMMyyyy.parse(dataFormatter.formatCellValue(row.getCell(0), formulaEvaluator)));

				doc.put("CREATE_DATE", createDate);
				doc.put("DATE_INDEX", createDate.substring(0, 10).replace("-", ""));
				doc.put("RESERVATION_ID", dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator));
				doc.put("BANK_TXN_NUMBER", dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator));
				doc.put("SID", dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator));
				doc.put("AMOUNT", amount);
				doc.put("FILE_NAME", fileFileName);
				doc.put("UPLOAD_DATE", dateNow);
				doc.put("STATUS", "Captured");
				doc.put("TXNTYPE", "SALE");
				doc.put("RESPONSE_MESSAGE", "SUCCESS");
				doc.put("SETTLEMENT_FLAG", "N");
				doc.put("SETTLEMENT_DATE", null);
				doc.put("SETTLEMENT_DATE_INDEX", null);
				doc.put("POST_SETTLED_FLAG", "N");
				doc.put("ACQUIRER", acquirer);
				successRows++;
				// coll.insertOne(doc);
				docList.add(doc);
			}

			if (docList.size() < 100000) {
				coll.insertMany(docList);
			} else {

				List<List<Document>> listOfDocList = new ArrayList<List<Document>>();
				listOfDocList = splitArray(docList, 100000);

				for (List<Document> docArr : listOfDocList) {
					coll.insertMany(docArr);
				}

			}

			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Processed");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);

		} catch (Exception ex) {
			logger.error("Exception uploadBookings ", ex);

			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Error");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);
		}
	}

	@SuppressWarnings("unchecked")
	public void uploadRefunds(File file, String fileFileName, String acquirer) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			MongoCollection<Document> excepColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_EXCEPTION_COLLECTION.getValue()));

			MongoCollection<Document> fileColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

			List<Row> results = new ArrayList<Row>();
			SimpleDateFormat sdf_ddMMyyyy = new SimpleDateFormat("dd-MM-yyyy");
			SimpleDateFormat sdf_yyyyMMddhhmmss = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			ArrayList<Document> docList = new ArrayList<Document>();
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			DataFormatter dataFormatter = new DataFormatter();
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file.getAbsolutePath()));
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = workbook.getSheetAt(0);

			int totalRows = 0;
			int errorRows = 0;
			int successRows = 0;

			for (Row row : sheet) { // iterate over all rows in the sheet except 1

				if (totalRows == 0) {
					totalRows++;
					continue;
				}

				if (row.getCell(0) == null || row.getCell(0) == null || row.getCell(0) == null
						|| row.getCell(0) == null) {
				}
				totalRows++;
				results.add(row);

			}

			for (Row row : results) {

				Document doc = new Document();
				// Cell dateCell = row.getCell(0);
				// Cell reservationIdCell = row.getCell(1);
				// Cell bankTxnNumberCell = row.getCell(2);
				// Cell sidCell = row.getCell(3);
				// Cell cancelDateCell = row.getCell(4);
				// Cell amountCell = row.getCell(5);

				// String dateCellValue = dataFormatter.formatCellValue(row.getCell(0),
				// formulaEvaluator);
				// String reservationIdCellValue = dataFormatter.formatCellValue(row.getCell(1),
				// formulaEvaluator);
				// String bankTxnNumberCellValue = dataFormatter.formatCellValue(row.getCell(2),
				// formulaEvaluator);
				// String sidCellValue = dataFormatter.formatCellValue(row.getCell(3),
				// formulaEvaluator);
				// String amountCellValue = dataFormatter.formatCellValue(row.getCell(5),
				// formulaEvaluator);
				// String cancelDateCellValue = dataFormatter.formatCellValue(row.getCell(4),
				// formulaEvaluator);

				if (StringUtils.isBlank(dataFormatter.formatCellValue(row.getCell(0), formulaEvaluator))
						|| StringUtils.isBlank(dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator))
						|| StringUtils.isBlank(dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator))
						|| StringUtils.isBlank(dataFormatter.formatCellValue(row.getCell(5), formulaEvaluator))) {

					String createDate = sdf_yyyyMMddhhmmss.format(
							sdf_ddMMyyyy.parse(dataFormatter.formatCellValue(row.getCell(0), formulaEvaluator)));

					doc.put("CREATE_DATE", createDate);
					doc.put("TXN_DATE", createDate);
					doc.put("DATE_INDEX", createDate.substring(0, 10).replace("-", ""));
					doc.put("RESERVATION_ID", dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator));
					doc.put("BANK_TXN_NUMBER", dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator));
					doc.put("AMOUNT", dataFormatter.formatCellValue(row.getCell(5), formulaEvaluator));
					doc.put("SID", dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator));
					doc.put("CANCEL_DATE", dataFormatter.formatCellValue(row.getCell(4), formulaEvaluator));
					doc.put("FILE_NAME", fileFileName);
					doc.put("FILE_TYPE", "REFUND");
					doc.put("TXNTYPE", "REFUND");
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("STATUS", "Invalid");
					doc.put("RESPONSE_MESSAGE", "Row Data Invalid");
					doc.put("ACQUIRER", acquirer);
					errorRows++;
					excepColl.insertOne(doc);
					continue;
				}

				String amount = dataFormatter.formatCellValue(row.getCell(5), formulaEvaluator);
				if (dataFormatter.formatCellValue(row.getCell(5), formulaEvaluator).contains(".")) {

					String amountCellValueSplit[] = dataFormatter.formatCellValue(row.getCell(5), formulaEvaluator)
							.split("\\.");
					String amountCellValueDecimal = amountCellValueSplit[1];

					if (amountCellValueDecimal.length() == 1) {
						amount = dataFormatter.formatCellValue(row.getCell(5), formulaEvaluator) + "0";
					}
				} else {
					amount = dataFormatter.formatCellValue(row.getCell(5), formulaEvaluator) + ".00";
				}

				// BasicDBObject resIdQuery = new BasicDBObject("RESERVATION_ID",
				// dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator));
				// BasicDBObject bankTxnQuery = new BasicDBObject("BANK_TXN_NUMBER",
				// dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator));
				// BasicDBObject txnTypeQuery = new BasicDBObject("TXNTYPE", "REFUND");
				List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
				condList.add(new BasicDBObject("RESERVATION_ID",
						dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator)));
				condList.add(new BasicDBObject("BANK_TXN_NUMBER",
						dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator)));
				condList.add(new BasicDBObject("TXNTYPE", "REFUND"));

				String createDate = sdf_yyyyMMddhhmmss
						.format(sdf_ddMMyyyy.parse(dataFormatter.formatCellValue(row.getCell(0), formulaEvaluator)));
				String cancelDate = null;

				if (StringUtils.isNotBlank(dataFormatter.formatCellValue(row.getCell(4), formulaEvaluator))
						&& !dataFormatter.formatCellValue(row.getCell(4), formulaEvaluator).equalsIgnoreCase("NA")) {
					cancelDate = sdf_yyyyMMddhhmmss.format(
							sdf_ddMMyyyy.parse(dataFormatter.formatCellValue(row.getCell(4), formulaEvaluator)));
				}

				doc.put("CREATE_DATE", createDate);
				doc.put("DATE_INDEX", createDate.substring(0, 10).replace("-", ""));
				doc.put("RESERVATION_ID", dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator));
				doc.put("CANCEL_DATE", cancelDate);
				doc.put("BANK_TXN_NUMBER", dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator));
				doc.put("SID", dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator));
				doc.put("AMOUNT", amount);
				doc.put("FILE_NAME", fileFileName);
				doc.put("UPLOAD_DATE", dateNow);
				doc.put("STATUS", "Captured");
				doc.put("TXNTYPE", "REFUND");
				doc.put("ACQUIRER", acquirer);
				doc.put("RESPONSE_MESSAGE", "SUCCESS");
				doc.put("SETTLEMENT_FLAG", "N");
				doc.put("SETTLEMENT_DATE", null);
				doc.put("SETTLEMENT_DATE_INDEX", null);
				doc.put("POST_SETTLED_FLAG", "N");
				successRows++;
				docList.add(doc);

			}

			if (docList.size() < 100000) {
				coll.insertMany(docList);
			} else {

				List<List<Document>> listOfDocList = new ArrayList<List<Document>>();
				listOfDocList = splitArray(docList, 100000);

				for (List<Document> docArr : listOfDocList) {
					coll.insertMany(docArr);
				}

			}

			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Processed");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);

		} catch (Exception ex) {
			logger.error("Exception refundBookings ", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public void uploadAmexMpr(File file, String fileFileName) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			MongoCollection<Document> excepColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_EXCEPTION_COLLECTION.getValue()));

			MongoCollection<Document> fileColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

			List<Row> results = new ArrayList<Row>();

			DataFormatter dataFormatter = new DataFormatter();
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file.getAbsolutePath()));
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = workbook.getSheetAt(0);
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			SimpleDateFormat sdfddMMyyyyhhmmss = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
			SimpleDateFormat sdfyyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdfddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");

			int totalRows = 0;
			int errorRows = 0;
			int successRows = 0;

			for (Row row : sheet) { // iterate over all rows in the sheet except 1

				if (totalRows == 0) {
					totalRows++;
					continue;
				}

				results.add(row);
				totalRows++;
			}

			for (Row row : results) {

				// Cell paymentDateCell = row.getCell(1);
				// Cell bankTxnNumberCell = row.getCell(2);
				// Cell recordTypeCell = row.getCell(3);
				// Cell terminalIdCell = row.getCell(6);
				// Cell txnDateCell = row.getCell(9);
				// Cell cardMaskCell = row.getCell(10);
				// Cell amountCell = row.getCell(18);

				// String paymentDateCellValue = dataFormatter.formatCellValue(row.getCell(1),
				// formulaEvaluator).trim();
				// String bankTxnNumberCellValue = dataFormatter.formatCellValue(row.getCell(2),
				// formulaEvaluator).trim();
				// String recordTypeCellValue = dataFormatter.formatCellValue(row.getCell(3),
				// formulaEvaluator).trim();
				// String terminalIdCellValue = dataFormatter.formatCellValue(row.getCell(6),
				// formulaEvaluator).trim();
				// String txnDateCellValue = dataFormatter.formatCellValue(row.getCell(9),
				// formulaEvaluator).trim();
				// String cardMaskCellValue = dataFormatter.formatCellValue(row.getCell(10),
				// formulaEvaluator).trim();
				// String amountCellValue = dataFormatter.formatCellValue(row.getCell(18),
				// formulaEvaluator).trim();

				if (StringUtils.isBlank(dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator).trim())
						|| !dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator).trim()
								.equalsIgnoreCase("ROC")) {
					continue;
				}

				if (StringUtils.isBlank(dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator).trim())
						|| StringUtils.isBlank(dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator).trim())
						|| StringUtils.isBlank(dataFormatter.formatCellValue(row.getCell(6), formulaEvaluator).trim())
						|| StringUtils.isBlank(dataFormatter.formatCellValue(row.getCell(9), formulaEvaluator).trim())
						|| StringUtils.isBlank(dataFormatter.formatCellValue(row.getCell(18), formulaEvaluator).trim())
						|| StringUtils
								.isBlank(dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator).trim())) {

					Document doc = new Document();

					doc.put("PAYMENT_DATE", dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator).trim());
					doc.put("BANK_TXN_NUMBER", dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator).trim());
					doc.put("RECORD_TYPE", dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator).trim());
					doc.put("AMOUNT", dataFormatter.formatCellValue(row.getCell(18), formulaEvaluator).trim());
					doc.put("TERMINAL_ID", dataFormatter.formatCellValue(row.getCell(6), formulaEvaluator).trim());
					doc.put("TXN_DATE", dataFormatter.formatCellValue(row.getCell(9), formulaEvaluator).trim());
					doc.put("FILE_NAME", fileFileName);
					doc.put("FILE_TYPE", "MPR");
					doc.put("CARD_MASK", dataFormatter.formatCellValue(row.getCell(10), formulaEvaluator).trim());
					if (dataFormatter.formatCellValue(row.getCell(18), formulaEvaluator).trim().contains("-")) {
						doc.put("TXNTYPE", "REFUND");
					} else {
						doc.put("TXNTYPE", "SALE");
					}
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("STATUS", "Invalid");
					doc.put("ACQUIRER", "AMEX");
					doc.put("RESPONSE_MESSAGE", "Row Data Invalid");
					errorRows++;
					excepColl.insertOne(doc);
					continue;
				}

				String mprAmount = dataFormatter.formatCellValue(row.getCell(18), formulaEvaluator).trim();

				String txnType = null;
				if (!dataFormatter.formatCellValue(row.getCell(18), formulaEvaluator).trim().contains("-")) {
					txnType = "SALE";
				} else {
					txnType = "REFUND";
				}
				mprAmount = mprAmount.replace(",", "").replace("-", "");
				String txnDate = null;

				txnDate = sdfyyyyMMdd.format(sdfddMMyyyyhhmmss
						.parse(dataFormatter.formatCellValue(row.getCell(9), formulaEvaluator).trim()));
				txnDate = txnDate + " 12:00:00";

				// BasicDBObject bankTxnQueryCount = new BasicDBObject("BANK_TXN_NUMBER",
				// dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator).trim());
				// BasicDBObject acquirerQueryCount = new BasicDBObject("ACQUIRER", "AMEX");
				// BasicDBObject txnTypeQueryCount = new BasicDBObject("TXNTYPE", txnType);
				// BasicDBObject txnDateQueryCount = new BasicDBObject("CREATE_DATE", txnDate);

				List<BasicDBObject> condListCount = new ArrayList<BasicDBObject>();
				condListCount.add(new BasicDBObject("BANK_TXN_NUMBER",
						dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator).trim()));
				condListCount.add(new BasicDBObject("ACQUIRER", "AMEX"));
				condListCount.add(new BasicDBObject("TXNTYPE", txnType));
				condListCount.add(new BasicDBObject("CREATE_DATE", txnDate));

				// BasicDBObject finalQuery = new BasicDBObject("$and", condListCount);

				long count = coll.countDocuments(new BasicDBObject("$and", condListCount));

				if (count > 1) {

					// Check if the amount is for single txn or clubbed

					List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
					condList.add(new BasicDBObject("BANK_TXN_NUMBER",
							dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator).trim()));
					condList.add(new BasicDBObject("ACQUIRER", "AMEX"));
					condList.add(new BasicDBObject("TXNTYPE", txnType));
					condList.add(new BasicDBObject("CREATE_DATE", txnDate));
					condList.add(new BasicDBObject("AMOUNT", mprAmount));

					// BasicDBObject finalCountQuery = new BasicDBObject("$and", condList);

					if (coll.countDocuments(new BasicDBObject("$and", condList)) == 1) {

						FindIterable<Document> itr = coll.find(new BasicDBObject("$and", condList));
						MongoCursor<Document> cursor = itr.iterator();

						while (cursor.hasNext()) {
							Document obj = cursor.next();

							Document setData = new Document();
							setData.append("TERMINAL_ID",
									dataFormatter.formatCellValue(row.getCell(6), formulaEvaluator).trim());
							setData.append("CARD_MASK",
									dataFormatter.formatCellValue(row.getCell(10), formulaEvaluator).trim());
							setData.append("RECORD_TYPE",
									dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator).trim());
							setData.append("SETTLEMENT_FLAG", "Y");
							setData.append("POST_SETTLE_CAPTURE", "N");
							setData.append("POST_SETTLED_FLAG", "N");

							String settleDate = sdfyyyyMMdd.format(sdfddMMyyyy
									.parse(dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator).trim()));
							setData.append("SETTLEMENT_DATE", settleDate + " 12:00:00");

							Document update = new Document();

							BasicDBObject idQuery = new BasicDBObject("_id", obj.get("_id"));
							update.append("$set", setData);
							coll.updateOne(idQuery, update);
							successRows++;
						}
						cursor.close();
						continue;
					}

					// If amount is clubbed , we have to update 2 entries and and match amount of
					// total wth MPR

					FindIterable<Document> itr = coll.find(new BasicDBObject("$and", condListCount));
					MongoCursor<Document> cursor = itr.iterator();

					double totalTxnAmount = 0.00;
					double totalMprAmount = Double.valueOf(mprAmount);

					while (cursor.hasNext()) {
						Document obj = cursor.next();
						String amount = obj.get("AMOUNT").toString();
						totalTxnAmount = totalTxnAmount + Double.valueOf(amount);

						Document setData = new Document();
						setData.append("TERMINAL_ID",
								dataFormatter.formatCellValue(row.getCell(6), formulaEvaluator).trim());
						setData.append("CARD_MASK",
								dataFormatter.formatCellValue(row.getCell(10), formulaEvaluator).trim());
						setData.append("RECORD_TYPE",
								dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator).trim());
						setData.append("SETTLEMENT_FLAG", "Y");
						setData.append("POST_SETTLE_CAPTURE", "N");
						setData.append("POST_SETTLED_FLAG", "N");

						String settleDate = sdfyyyyMMdd.format(sdfddMMyyyy
								.parse(dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator).trim()));
						setData.append("SETTLEMENT_DATE", settleDate + " 12:00:00");

						Document update = new Document();

						// BasicDBObject idQuery = new BasicDBObject("_id", obj.get("_id"));
						update.append("$set", setData);
						coll.updateOne(new BasicDBObject("_id", obj.get("_id")), update);
						successRows++;
					}
					cursor.close();
					if (totalTxnAmount != totalMprAmount) {

						Document doc = new Document();

						doc.put("PAYMENT_DATE", dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator).trim());
						doc.put("BANK_TXN_NUMBER",
								dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator).trim());
						doc.put("RECORD_TYPE", dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator).trim());
						doc.put("AMOUNT", totalTxnAmount);
						doc.put("MPR_AMOUNT", totalMprAmount);
						doc.put("TERMINAL_ID", dataFormatter.formatCellValue(row.getCell(6), formulaEvaluator).trim());
						doc.put("TXN_DATE", dataFormatter.formatCellValue(row.getCell(9), formulaEvaluator).trim());
						doc.put("FILE_NAME", fileFileName);
						doc.put("FILE_TYPE", "MPR");
						doc.put("CARD_MASK", dataFormatter.formatCellValue(row.getCell(10), formulaEvaluator).trim());
						doc.put("STATUS", "Captured");
						if (dataFormatter.formatCellValue(row.getCell(18), formulaEvaluator).trim().contains("-")) {
							doc.put("TXNTYPE", "REFUND");
						} else {
							doc.put("TXNTYPE", "SALE");
						}
						doc.put("UPLOAD_DATE", dateNow);
						doc.put("STATUS", "Invalid");
						doc.put("ACQUIRER", "AMEX");
						doc.put("RESPONSE_MESSAGE", "Amount Mismatch");
						excepColl.insertOne(doc);

					}

					continue;
				}

				if (count < 1) {

					Document doc = new Document();

					doc.put("CREATE_DATE", txnDate);
					doc.put("DATE_INDEX", txnDate.substring(0, 10).replace("-", ""));
					doc.put("RESERVATION_ID", "");
					doc.put("BANK_TXN_NUMBER", dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator).trim());
					doc.put("TERMINAL_ID", dataFormatter.formatCellValue(row.getCell(6), formulaEvaluator).trim());
					doc.put("AMOUNT", dataFormatter.formatCellValue(row.getCell(18), formulaEvaluator).trim()
							.replace(",", "").replace("-", ""));
					doc.put("FILE_NAME", fileFileName);
					doc.put("CARD_MASK", dataFormatter.formatCellValue(row.getCell(10), formulaEvaluator).trim());
					doc.put("RECORD_TYPE", dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator).trim());
					doc.put("ACQUIRER", "AMEX");
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("STATUS", "Captured");
					if (dataFormatter.formatCellValue(row.getCell(18), formulaEvaluator).trim().contains("-")) {
						doc.put("TXNTYPE", "REFUND");
					} else {
						doc.put("TXNTYPE", "SALE");
					}

					doc.put("RESPONSE_MESSAGE", "SUCCESS");
					doc.put("SETTLEMENT_FLAG", "Y");
					doc.put("POST_SETTLE_CAPTURE", "Y");
					doc.put("POST_SETTLED_FLAG", "Y");

					String settleDate = sdfyyyyMMdd.format(
							sdfddMMyyyy.parse(dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator).trim()));
					doc.put("SETTLEMENT_DATE", settleDate + " 12:00:00");
					doc.put("SETTLEMENT_DATE_INDEX", settleDate.replace("-", ""));
					coll.insertOne(doc);
					successRows++;
					continue;
				}

				FindIterable<Document> itr = coll.find(new BasicDBObject("$and", condListCount));
				MongoCursor<Document> cursor = itr.iterator();

				while (cursor.hasNext()) {

					Document obj = cursor.next();
					// String dbAmount = obj.get("AMOUNT").toString();
					// String mprAmount1 = dataFormatter.formatCellValue(row.getCell(18),
					// formulaEvaluator).trim().replace(",", "").replace("-", "");

					// Create exception
					if (!mprAmount.equalsIgnoreCase(obj.get("AMOUNT").toString())) {

						Document doc = new Document();

						doc.put("PAYMENT_DATE", dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator).trim());
						doc.put("BANK_TXN_NUMBER",
								dataFormatter.formatCellValue(row.getCell(2), formulaEvaluator).trim());
						doc.put("RECORD_TYPE", dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator).trim());
						doc.put("AMOUNT", obj.get("AMOUNT").toString());
						doc.put("MPR_AMOUNT", dataFormatter.formatCellValue(row.getCell(18), formulaEvaluator).trim()
								.replace(",", "").replace("-", ""));
						doc.put("TERMINAL_ID", dataFormatter.formatCellValue(row.getCell(6), formulaEvaluator).trim());
						doc.put("TXN_DATE", dataFormatter.formatCellValue(row.getCell(9), formulaEvaluator).trim());
						doc.put("FILE_NAME", fileFileName);
						doc.put("FILE_TYPE", "MPR");
						doc.put("CARD_MASK", dataFormatter.formatCellValue(row.getCell(10), formulaEvaluator).trim());
						doc.put("STATUS", "Captured");
						if (dataFormatter.formatCellValue(row.getCell(18), formulaEvaluator).trim().contains("-")) {
							doc.put("TXNTYPE", "REFUND");
						} else {
							doc.put("TXNTYPE", "SALE");
						}
						doc.put("UPLOAD_DATE", dateNow);
						doc.put("STATUS", "Invalid");
						doc.put("ACQUIRER", "AMEX");
						doc.put("RESPONSE_MESSAGE", "Amount Mismatch");
						excepColl.insertOne(doc);

					}

					// String settleFlag = obj.get("SETTLEMENT_FLAG").toString();

					if (obj.get("SETTLEMENT_FLAG").toString().equalsIgnoreCase("Y")) {
						continue;
					} else {
						Document setData = new Document();
						setData.append("TERMINAL_ID",
								dataFormatter.formatCellValue(row.getCell(6), formulaEvaluator).trim());
						setData.append("CARD_MASK",
								dataFormatter.formatCellValue(row.getCell(10), formulaEvaluator).trim());
						setData.append("RECORD_TYPE",
								dataFormatter.formatCellValue(row.getCell(3), formulaEvaluator).trim());
						setData.append("SETTLEMENT_FLAG", "Y");
						setData.append("POST_SETTLE_CAPTURE", "N");
						setData.append("POST_SETTLED_FLAG", "N");
						String settleDate = sdfyyyyMMdd.format(sdfddMMyyyy
								.parse(dataFormatter.formatCellValue(row.getCell(1), formulaEvaluator).trim()));
						setData.append("SETTLEMENT_DATE", settleDate + " 12:00:00");
						setData.append("SETTLEMENT_DATE_INDEX", settleDate.replace("-", ""));

						// BasicDBObject idQuery = new BasicDBObject("_id", obj.get("_id"));

						List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
						conditionList.add(new BasicDBObject("_id", obj.get("_id")));

						BasicDBObject updateQuery = new BasicDBObject("$and", conditionList);

						Document update = new Document();
						update.append("$set", setData);
						coll.updateOne(updateQuery, update);
						successRows++;
					}

				}
				cursor.close();
			}

			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Processed");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);

		} catch (Exception ex) {
			logger.error("Exception refundBookings ", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public void uploadRupayMpr(File file, String fileFileName) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

		MongoCollection<Document> excepColl = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECON_EXCEPTION_COLLECTION.getValue()));

		MongoCollection<Document> fileColl = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

		int totalRows = 0;
		int errorRows = 0;
		int successRows = 0;

		try {

			String fileFileNameSplit[] = fileFileName.split("_");
			String fileSettlementDate = fileFileNameSplit[3];
			SimpleDateFormat sdf3 = new SimpleDateFormat("ddMMyyyy");
			SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MM-dd");
			String settleDate = sdf4.format(sdf3.parse(fileSettlementDate));

			List<Row> results = new ArrayList<Row>();

			DataFormatter dataFormatter = new DataFormatter();
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file.getAbsolutePath()));
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = workbook.getSheetAt(0);

			for (Row row : sheet) { // iterate over all rows in the sheet except 1

				if (totalRows == 0) {
					totalRows++;
					continue;
				}

				results.add(row);
				totalRows++;
			}

			for (Row row : results) {

				Cell reservationIdCell = row.getCell(1);
				Cell bankTxnNumberCell = row.getCell(8);
				Cell recordTypeCell = row.getCell(4);
				Cell terminalIdCell = row.getCell(5);
				Cell txnDateCell = row.getCell(11);
				Cell cardMaskCell = row.getCell(2);
				Cell amountCell = row.getCell(13);
				Cell txnTypeCell = row.getCell(12);

				String bankTxnNumberCellValue = dataFormatter.formatCellValue(bankTxnNumberCell, formulaEvaluator)
						.trim();
				String recordTypeCellValue = dataFormatter.formatCellValue(recordTypeCell, formulaEvaluator).trim();
				String terminalIdCellValue = dataFormatter.formatCellValue(terminalIdCell, formulaEvaluator).trim();
				String txnDateCellValue = dataFormatter.formatCellValue(txnDateCell, formulaEvaluator).trim();
				String cardMaskCellValue = dataFormatter.formatCellValue(cardMaskCell, formulaEvaluator).trim();
				String amountCellValue = dataFormatter.formatCellValue(amountCell, formulaEvaluator).trim();
				String reservationIdCellValue = dataFormatter.formatCellValue(reservationIdCell, formulaEvaluator)
						.trim();
				String txnTypeCellValue = dataFormatter.formatCellValue(txnTypeCell, formulaEvaluator).trim();

				if (StringUtils.isBlank(bankTxnNumberCellValue) || StringUtils.isBlank(recordTypeCellValue)
						|| StringUtils.isBlank(terminalIdCellValue) || StringUtils.isBlank(txnDateCellValue)
						|| StringUtils.isBlank(amountCellValue)) {

					Date dNow = new Date();
					String dateNow = DateCreater.formatDateForDb(dNow);

					Document doc = new Document();
					doc.put("PAYMENT_DATE", settleDate + " 12:00:00");
					doc.put("RESERVATION_ID", reservationIdCellValue);
					doc.put("BANK_TXN_NUMBER", bankTxnNumberCellValue);
					doc.put("RECORD_TYPE", recordTypeCellValue);
					doc.put("AMOUNT", amountCellValue);
					doc.put("TERMINAL_ID", terminalIdCellValue);
					doc.put("TXN_DATE", txnDateCellValue);
					doc.put("FILE_NAME", fileFileName);
					doc.put("FILE_TYPE", "MPR");
					doc.put("CARD_MASK", cardMaskCellValue);
					doc.put("ACQUIRER", "RUPAY");
					if (txnTypeCellValue.equalsIgnoreCase("01")) {
						doc.put("TXNTYPE", "SALE");
					} else {
						doc.put("TXNTYPE", "REFUND");
					}
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("STATUS", "Invalid");
					doc.put("RESPONSE_MESSAGE", "Row Data Invalid");
					errorRows++;
					excepColl.insertOne(doc);
					continue;
				}

				String mprAmount = amountCellValue;

				if (amountCellValue.contains(".")) {

					String amountCellValueSplit[] = amountCellValue.split("\\.");
					String amountCellValueDecimal = amountCellValueSplit[1];

					if (amountCellValueDecimal.length() == 1) {
						mprAmount = amountCellValue + "0";
					}
				} else {
					mprAmount = amountCellValue + ".00";
				}

				String txnType = null;
				if (txnTypeCellValue.equalsIgnoreCase("01")) {
					txnType = "SALE";
				} else {
					txnType = "REFUND";
				}

				String txnDate = null;

				SimpleDateFormat sdfTxnDate1 = new SimpleDateFormat("dd-MMM-yy hh:mm:ss");
				SimpleDateFormat sdfTxnDate2 = new SimpleDateFormat("yyyy-MM-dd");
				txnDate = sdfTxnDate2.format(sdfTxnDate1.parse(txnDateCellValue));
				txnDate = txnDate + " 12:00:00";

				// BasicDBObject bankTxnQuery = new BasicDBObject("BANK_TXN_NUMBER",
				// bankTxnNumberCellValue);
				BasicDBObject reservationIdQuery = new BasicDBObject("RESERVATION_ID", reservationIdCellValue);
				BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", "RUPAY");
				BasicDBObject txnTypeQuery = new BasicDBObject("TXNTYPE", txnType);
				BasicDBObject txnDateQuery = new BasicDBObject("CREATE_DATE", txnDate);

				List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
				// condList.add(bankTxnQuery);
				condList.add(acquirerQuery);
				condList.add(reservationIdQuery);
				condList.add(txnTypeQuery);
				condList.add(txnDateQuery);

				BasicDBObject finalQuery = new BasicDBObject("$and", condList);

				// check no record found
				long count = coll.countDocuments(finalQuery);

				if (count > 1) {

					// Query with amount
					List<BasicDBObject> condQueryList = condList;
					condQueryList.add(new BasicDBObject("AMOUNT", mprAmount));
					BasicDBObject finalAmountQuery = new BasicDBObject("$and", condList);

					long amountQueryCount = coll.countDocuments(finalAmountQuery);

					if (amountQueryCount == 1) {

						FindIterable<Document> itr = coll.find(finalAmountQuery);
						MongoCursor<Document> cursor = itr.iterator();

						while (cursor.hasNext()) {
							Document obj = cursor.next();
							String amount = obj.get("AMOUNT").toString();
							String settledFlag = obj.get("SETTLEMENT_FLAG").toString();

							if (settledFlag.equalsIgnoreCase("Y")) {
								continue;
							}

							BasicDBObject idQuery = new BasicDBObject("_id", obj.get("_id"));

							Document setData = new Document();
							setData.append("TERMINAL_ID", terminalIdCellValue);
							setData.append("CARD_MASK", cardMaskCellValue);
							setData.append("RECORD_TYPE", recordTypeCellValue);
							setData.append("SETTLEMENT_FLAG", "Y");
							setData.append("POST_SETTLE_CAPTURE", "N");
							setData.append("POST_SETTLED_FLAG", "N");

							setData.append("SETTLEMENT_DATE", settleDate + " 12:00:00");
							setData.append("SETTLEMENT_DATE_INDEX", settleDate.replace("-", ""));

							if (!amount.equalsIgnoreCase(mprAmount)) {
								setData.append("AMOUNT", mprAmount);
							}

							Document update = new Document();
							update.append("$set", setData);
							coll.updateOne(idQuery, update);
							successRows++;
							continue;
						}
					}

					FindIterable<Document> itr = coll.find(finalQuery);
					MongoCursor<Document> cursor = itr.iterator();

					while (cursor.hasNext()) {
						Document obj = cursor.next();
						String amount = obj.get("AMOUNT").toString();
						String settledFlag = obj.get("SETTLEMENT_FLAG").toString();

						if (settledFlag.equalsIgnoreCase("Y")) {
							continue;
						}

						BasicDBObject idQuery = new BasicDBObject("_id", obj.get("_id"));

						Document setData = new Document();
						setData.append("TERMINAL_ID", terminalIdCellValue);
						setData.append("CARD_MASK", cardMaskCellValue);
						setData.append("RECORD_TYPE", recordTypeCellValue);
						setData.append("SETTLEMENT_FLAG", "Y");
						setData.append("POST_SETTLE_CAPTURE", "N");
						setData.append("POST_SETTLED_FLAG", "N");

						setData.append("SETTLEMENT_DATE", settleDate + " 12:00:00");
						setData.append("SETTLEMENT_DATE_INDEX", settleDate.replace("-", ""));

						if (!amount.equalsIgnoreCase(mprAmount)) {
							setData.append("AMOUNT", mprAmount);
						}

						Document update = new Document();
						update.append("$set", setData);
						coll.updateOne(idQuery, update);
						successRows++;

						if (!amount.equalsIgnoreCase(mprAmount)) {
							Date dNow = new Date();
							String dateNow = DateCreater.formatDateForDb(dNow);

							Document doc = new Document();
							doc.put("PAYMENT_DATE", settleDate + " 12:00:00");
							doc.put("RESERVATION_ID", reservationIdCellValue);
							doc.put("BANK_TXN_NUMBER", bankTxnNumberCellValue);
							doc.put("RECORD_TYPE", recordTypeCellValue);
							doc.put("AMOUNT", amount);
							doc.put("AMOUNT_MPR", mprAmount);
							doc.put("TERMINAL_ID", terminalIdCellValue);
							doc.put("TXN_DATE", txnDateCellValue);
							doc.put("FILE_NAME", fileFileName);
							doc.put("FILE_TYPE", "MPR");
							doc.put("CARD_MASK", cardMaskCellValue);
							doc.put("ACQUIRER", "RUPAY");
							doc.put("TXNTYPE", txnType);
							doc.put("UPLOAD_DATE", dateNow);
							doc.put("STATUS", "Invalid");
							doc.put("RESPONSE_MESSAGE", "Amount Mismatch");
							excepColl.insertOne(doc);
						}
					}

					continue;
				}

				if (count < 1) {

					Document doc = new Document();
					Date dNow = new Date();
					String dateNow = DateCreater.formatDateForDb(dNow);

					doc.put("CREATE_DATE", txnDate);
					doc.put("DATE_INDEX", txnDate.substring(0, 10).replace("-", ""));
					doc.put("RESERVATION_ID", reservationIdCellValue);
					doc.put("BANK_TXN_NUMBER", bankTxnNumberCellValue);
					doc.put("TERMINAL_ID", terminalIdCellValue);
					doc.put("AMOUNT", mprAmount);
					doc.put("FILE_NAME", fileFileName);
					doc.put("CARD_MASK", cardMaskCellValue);
					doc.put("RECORD_TYPE", recordTypeCellValue);
					doc.put("ACQUIRER", "RUPAY");
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("STATUS", "Captured");
					if (txnTypeCellValue.equalsIgnoreCase("04")) {
						doc.put("TXNTYPE", "REFUND");
					} else {
						doc.put("TXNTYPE", "SALE");
					}

					doc.put("RESPONSE_MESSAGE", "SUCCESS");
					doc.put("SETTLEMENT_FLAG", "Y");
					doc.put("POST_SETTLE_CAPTURE", "Y");
					doc.put("POST_SETTLED_FLAG", "Y");
					doc.put("SETTLEMENT_DATE", settleDate + " 12:00:00");
					doc.put("SETTLEMENT_DATE_INDEX", settleDate.replace("-", ""));
					coll.insertOne(doc);
					successRows++;
					continue;
				}

				FindIterable<Document> itr = coll.find(finalQuery);

				MongoCursor<Document> cursor = itr.iterator();

				while (cursor.hasNext()) {

					Document doc = new Document();
					Document obj = cursor.next();

					BasicDBObject updateQuery = new BasicDBObject("_id", obj.get("_id"));
					String amount = obj.get("AMOUNT").toString();
					String settleFlag = obj.get("SETTLEMENT_FLAG").toString();

					if (settleFlag.equalsIgnoreCase("Y")) {

						String trxnType = obj.get("TXNTYPE").toString();
						if (trxnType.equalsIgnoreCase("SALE")) {

							Document newDoc = new Document();
							Date dNow = new Date();
							String dateNow = DateCreater.formatDateForDb(dNow);

							newDoc.put("CREATE_DATE", txnDate);
							newDoc.put("DATE_INDEX", txnDate.substring(0, 10).replace("-", ""));
							newDoc.put("RESERVATION_ID", reservationIdCellValue);
							newDoc.put("BANK_TXN_NUMBER", bankTxnNumberCellValue);
							newDoc.put("TERMINAL_ID", terminalIdCellValue);
							newDoc.put("AMOUNT", mprAmount);
							newDoc.put("FILE_NAME", fileFileName);
							newDoc.put("CARD_MASK", cardMaskCellValue);
							newDoc.put("RECORD_TYPE", recordTypeCellValue);
							newDoc.put("ACQUIRER", "RUPAY");
							newDoc.put("UPLOAD_DATE", dateNow);
							newDoc.put("STATUS", "Captured");
							if (txnTypeCellValue.equalsIgnoreCase("04")) {
								newDoc.put("TXNTYPE", "REFUND");
							} else {
								newDoc.put("TXNTYPE", "SALE");
							}

							newDoc.put("RESPONSE_MESSAGE", "SUCCESS");
							newDoc.put("SETTLEMENT_FLAG", "Y");
							newDoc.put("POST_SETTLE_CAPTURE", "Y");
							newDoc.put("POST_SETTLED_FLAG", "Y");
							newDoc.put("SETTLEMENT_DATE", settleDate + " 12:00:00");
							newDoc.put("SETTLEMENT_DATE_INDEX", settleDate.replace("-", ""));
							coll.insertOne(newDoc);
							successRows++;
							continue;

						}

					}

					if (!amount.equalsIgnoreCase(mprAmount)) {
						Date dNow = new Date();
						String dateNow = DateCreater.formatDateForDb(dNow);

						doc.put("PAYMENT_DATE", settleDate + " 12:00:00");
						doc.put("RESERVATION_ID", reservationIdCellValue);
						doc.put("BANK_TXN_NUMBER", bankTxnNumberCellValue);
						doc.put("RECORD_TYPE", recordTypeCellValue);
						doc.put("AMOUNT", amount);
						doc.put("AMOUNT_MPR", mprAmount);
						doc.put("TERMINAL_ID", terminalIdCellValue);
						doc.put("TXN_DATE", txnDateCellValue);
						doc.put("FILE_NAME", fileFileName);
						doc.put("FILE_TYPE", "MPR");
						doc.put("CARD_MASK", cardMaskCellValue);
						doc.put("ACQUIRER", "RUPAY");
						doc.put("TXNTYPE", txnType);
						doc.put("UPLOAD_DATE", dateNow);
						doc.put("STATUS", "Invalid");
						doc.put("RESPONSE_MESSAGE", "Amount Mismatch");
						excepColl.insertOne(doc);
					}

					Document setData = new Document();
					setData.append("TERMINAL_ID", terminalIdCellValue);
					setData.append("CARD_MASK", cardMaskCellValue);
					setData.append("RECORD_TYPE", recordTypeCellValue);
					setData.append("SETTLEMENT_FLAG", "Y");
					setData.append("POST_SETTLE_CAPTURE", "N");
					setData.append("POST_SETTLED_FLAG", "N");

					setData.append("SETTLEMENT_DATE", settleDate + " 12:00:00");
					setData.append("SETTLEMENT_DATE_INDEX", settleDate.replace("-", ""));

					if (!amount.equalsIgnoreCase(mprAmount)) {
						setData.append("AMOUNT", mprAmount);
					}

					Document update = new Document();
					update.append("$set", setData);
					coll.updateOne(updateQuery, update);
					successRows++;

				}

			}

			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Processed");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);

		} catch (Exception ex) {
			logger.error("Exception refundBookings ", ex);

			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Error");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);

		}
	}

	@SuppressWarnings("unchecked")
	public void uploadAmexStatement(File file, String fileFileName) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> fileColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

			MongoCollection<Document> statementColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

			List<Row> results = new ArrayList<Row>();

			DataFormatter dataFormatter = new DataFormatter();
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file.getAbsolutePath()));
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = workbook.getSheetAt(0);

			int totalRows = 0;
			int errorRows = 0;
			int successRows = 0;

			for (Row row : sheet) { // iterate over all rows in the sheet except 1

				if (totalRows == 0) {
					totalRows++;
					continue;
				}

				results.add(row);
				totalRows++;
			}

			for (Row row : results) {

				Document doc = new Document();
				Cell paymentDateCell = row.getCell(0);
				Cell bankTxnNumberCell = row.getCell(1);
				Cell amountCell = row.getCell(3);

				String paymentDateCellValue = dataFormatter.formatCellValue(paymentDateCell, formulaEvaluator).trim();
				String bankTxnNumberCellValue = dataFormatter.formatCellValue(bankTxnNumberCell, formulaEvaluator)
						.trim();
				String amountCellValue = dataFormatter.formatCellValue(amountCell, formulaEvaluator).trim();

				if (StringUtils.isBlank(bankTxnNumberCellValue) || !bankTxnNumberCellValue.contains("SIN")) {
					continue;
				}

				if (StringUtils.isBlank(paymentDateCellValue) || StringUtils.isBlank(bankTxnNumberCellValue)
						|| StringUtils.isBlank(amountCellValue)) {

					Date dNow = new Date();
					String dateNow = DateCreater.formatDateForDb(dNow);

					doc.put("PAYMENT_DATE", paymentDateCellValue);
					doc.put("TXN_DATE", paymentDateCellValue);
					doc.put("BANK_TXN_NUMBER", bankTxnNumberCellValue);
					doc.put("AMOUNT", amountCellValue);
					doc.put("FILE_NAME", fileFileName);
					doc.put("TXNTYPE", "STATEMENT");
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("STATUS", "Invalid");
					doc.put("RESPONSE_MESSAGE", "Row Data Invalid");
					errorRows++;
					// excepColl.insertOne(doc);
					continue;
				}

				String bankTxnNumberCellValueSplit[] = bankTxnNumberCellValue.split(" ");
				String settleDatePart = bankTxnNumberCellValueSplit[1];
				String dateSettleString = settleDatePart.substring(5, 11);

				SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				String dateSettle = sdf2.format(sdf.parse(dateSettleString));

				successRows++;
				BasicDBObject bankTxnQuery = new BasicDBObject("BANK_TXN_NUMBER", bankTxnNumberCellValue);
				BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", "AMEX");

				List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
				condList.add(bankTxnQuery);
				condList.add(acquirerQuery);

				BasicDBObject finalQuery = new BasicDBObject("$and", condList);
				// check no record found
				long count = fileColl.countDocuments(finalQuery);

				if (count < 1) {

					Date dNow = new Date();
					String dateNow = DateCreater.formatDateForDb(dNow);

					doc.put("BANK_TXN_NUMBER", bankTxnNumberCellValue);
					doc.put("AMOUNT", amountCellValue.replace(",", "").replace("-", ""));
					doc.put("FILE_NAME", fileFileName);
					doc.put("ACQUIRER", "AMEX");
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("SETTLEMENT_DATE", dateSettle);
					SimpleDateFormat sdf3 = new SimpleDateFormat("dd/MM/yyyy");
					SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MM-dd");

					String settleDate = sdf4.format(sdf3.parse(paymentDateCellValue));
					doc.put("PAYOUT_DATE", settleDate + " 12:00:00");
					statementColl.insertOne(doc);
					continue;
				} else {
					Document setData = new Document();
					setData.append("BANK_TXN_NUMBER", bankTxnNumberCellValue);
					setData.append("AMOUNT", amountCellValue.replace(",", "").replace("-", ""));
					setData.append("FILE_NAME", fileFileName);
					SimpleDateFormat sdf3 = new SimpleDateFormat("dd/MM/yyyy");
					SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MM-dd");
					String payoutDate = sdf4.format(sdf3.parse(paymentDateCellValue));
					setData.append("PAYOUT_DATE", payoutDate + " 12:00:00");
					setData.append("SETTLEMENT_DATE", dateSettle);

					Document update = new Document();
					update.append("$set", setData);
					statementColl.updateMany(finalQuery, update);
				}

			}

			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Processed");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);

		} catch (Exception ex) {
			logger.error("Exception refundBookings ", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public void uploadRupayStatement(File file, String fileFileName) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> fileColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

			MongoCollection<Document> statementColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

			List<Row> results = new ArrayList<Row>();

			DataFormatter dataFormatter = new DataFormatter();
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file.getAbsolutePath()));
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = workbook.getSheetAt(0);

			int totalRows = 0;
			int errorRows = 0;
			int successRows = 0;

			for (Row row : sheet) { // iterate over all rows in the sheet except 1

				if (totalRows == 0) {
					totalRows++;
					continue;
				}

				results.add(row);
				totalRows++;
			}

			for (Row row : results) {

				Document doc = new Document();
				Cell paymentDateCell = row.getCell(1);
				Cell bankTxnNumberCell = row.getCell(2);
				Cell amountCell = row.getCell(4);

				String paymentDateCellValue = dataFormatter.formatCellValue(paymentDateCell, formulaEvaluator).trim();
				String bankTxnNumberCellValue = dataFormatter.formatCellValue(bankTxnNumberCell, formulaEvaluator)
						.trim();
				String amountCellValue = dataFormatter.formatCellValue(amountCell, formulaEvaluator).trim();

				if (StringUtils.isBlank(bankTxnNumberCellValue)
						|| !bankTxnNumberCellValue.contains("RUPAY ECOM ACQ TXN")) {
					continue;
				}

				if (StringUtils.isBlank(paymentDateCellValue) || StringUtils.isBlank(bankTxnNumberCellValue)
						|| StringUtils.isBlank(amountCellValue)) {

					Date dNow = new Date();
					String dateNow = DateCreater.formatDateForDb(dNow);

					doc.put("PAYMENT_DATE", paymentDateCellValue);
					doc.put("BANK_TXN_NUMBER", bankTxnNumberCellValue);
					doc.put("AMOUNT", amountCellValue);
					doc.put("FILE_NAME", fileFileName);
					doc.put("TXNTYPE", "STATEMENT");
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("STATUS", "Invalid");
					doc.put("RESPONSE_MESSAGE", "Row Data Invalid");
					errorRows++;
					// excepColl.insertOne(doc);
					continue;
				}

				successRows++;
				BasicDBObject bankTxnQuery = new BasicDBObject("BANK_TXN_NUMBER", bankTxnNumberCellValue);
				BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", "RUPAY");

				List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
				condList.add(bankTxnQuery);
				condList.add(acquirerQuery);

				BasicDBObject finalQuery = new BasicDBObject("$and", condList);
				// check no record found
				long count = fileColl.countDocuments(finalQuery);

				if (count < 1) {

					String bankTxnNumberCellValueSplit[] = bankTxnNumberCellValue.split(" ");

					String txnDate = bankTxnNumberCellValueSplit[5];
					SimpleDateFormat sdf1 = new SimpleDateFormat("dd.MM.yy");
					SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					String actualTxnDate = sdf2.format(sdf1.parse(txnDate));
					doc.put("CAPTURE_DATE", actualTxnDate);

					Date dNow = new Date();
					String dateNow = DateCreater.formatDateForDb(dNow);

					doc.put("BANK_TXN_NUMBER", bankTxnNumberCellValue);
					doc.put("AMOUNT", amountCellValue.replace(",", "").replace("-", ""));
					doc.put("FILE_NAME", fileFileName);
					doc.put("ACQUIRER", "RUPAY");
					doc.put("UPLOAD_DATE", dateNow);
					SimpleDateFormat sdf3 = new SimpleDateFormat("MM/dd/yy");
					SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

					String settleDate = sdf4.format(sdf3.parse(paymentDateCellValue));
					doc.put("SETTLEMENT_DATE", settleDate);
					doc.put("PAYOUT_DATE", settleDate);
					statementColl.insertOne(doc);
					continue;
				} else {
					Document setData = new Document();

					String bankTxnNumberCellValueSplit[] = bankTxnNumberCellValue.split(" ");

					String txnDate = bankTxnNumberCellValueSplit[5];
					SimpleDateFormat sdf1 = new SimpleDateFormat("dd.MM.yy");
					SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
					String actualTxnDate = sdf2.format(sdf1.parse(txnDate));
					doc.put("CAPTURE_DATE", actualTxnDate);

					setData.append("BANK_TXN_NUMBER", bankTxnNumberCellValue);
					setData.append("AMOUNT", amountCellValue.replace(",", "").replace("-", ""));
					setData.append("FILE_NAME", fileFileName);
					SimpleDateFormat sdf3 = new SimpleDateFormat("MM/dd/yyyy");
					SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MM-dd");
					String settleDate = sdf4.format(sdf3.parse(paymentDateCellValue));
					setData.append("SETTLEMENT_DATE", settleDate + " 12:00:00");
					setData.append("PAYOUT_DATE", settleDate + " 12:00:00");

					Document update = new Document();
					update.append("$set", setData);
					statementColl.updateMany(finalQuery, update);
				}

			}

			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Processed");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);

		} catch (Exception ex) {
			logger.error("Exception refundBookings ", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public void uploadIPAYMpr(File file, String fileFileName) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

		MongoCollection<Document> excepColl = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECON_EXCEPTION_COLLECTION.getValue()));

		MongoCollection<Document> fileColl = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

		int totalRows = 0;
		int errorRows = 0;
		int successRows = 0;

		try {

			List<Row> results = new ArrayList<Row>();

			DataFormatter dataFormatter = new DataFormatter();
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file.getAbsolutePath()));
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = workbook.getSheetAt(0);

			for (Row row : sheet) { // iterate over all rows in the sheet except 1

				if (totalRows == 0) {
					totalRows++;
					continue;
				}

				results.add(row);
				totalRows++;

				logger.info("Total rows for Daddytech = " + totalRows);
			}

			Cell merchantNameCell = null;
			Cell terminalIdCell = null;
			Cell reservationIdCell = null;
			Cell txnDateCell = null;
			Cell txnSettleCell = null;
			Cell txnTypeCell = null;
			Cell amountCell = null;
			Cell bankNameCell = null;
			Cell payoutDateCell = null;

			String terminalIdCellValue = null;
			String txnDateCellValue = null;
			String amountCellValue = null;
			String reservationIdCellValue = null;
			String merchantNameCellValue = null;
			String bankNameCellValue = null;
			String txnTypeCellValue = null;
			String txnSettleCellValue = null;
			String payoutDateCellValue = null;

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			SimpleDateFormat sdfyyyyMMddhhmmss = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			SimpleDateFormat sdfyyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdfddMMMyy = new SimpleDateFormat("dd-MMM-yy");

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();

			for (Row row : results) {

				condList.clear();

				merchantNameCell = row.getCell(0);
				terminalIdCell = row.getCell(1);
				reservationIdCell = row.getCell(2);
				txnDateCell = row.getCell(3);
				txnSettleCell = row.getCell(4);
				txnTypeCell = row.getCell(5);
				amountCell = row.getCell(6);
				bankNameCell = row.getCell(7);
				payoutDateCell = row.getCell(8);

				terminalIdCellValue = dataFormatter.formatCellValue(terminalIdCell, formulaEvaluator).trim();
				txnDateCellValue = dataFormatter.formatCellValue(txnDateCell, formulaEvaluator).trim();
				amountCellValue = dataFormatter.formatCellValue(amountCell, formulaEvaluator).trim();
				reservationIdCellValue = dataFormatter.formatCellValue(reservationIdCell, formulaEvaluator);
				merchantNameCellValue = dataFormatter.formatCellValue(merchantNameCell, formulaEvaluator).trim();
				bankNameCellValue = dataFormatter.formatCellValue(bankNameCell, formulaEvaluator).trim();
				txnTypeCellValue = dataFormatter.formatCellValue(txnTypeCell, formulaEvaluator).trim();
				txnSettleCellValue = dataFormatter.formatCellValue(txnSettleCell, formulaEvaluator).trim();
				payoutDateCellValue = dataFormatter.formatCellValue(payoutDateCell, formulaEvaluator).trim();

				String settleDate = sdfyyyyMMdd.format(sdfyyyyMMddhhmmss.parse(txnSettleCellValue));

				String payoutDate = sdfyyyyMMdd.format(sdfddMMMyy.parse(payoutDateCellValue));

				if (StringUtils.isBlank(merchantNameCellValue) || StringUtils.isBlank(reservationIdCellValue)
						|| StringUtils.isBlank(amountCellValue) || StringUtils.isBlank(bankNameCellValue)
						|| StringUtils.isBlank(txnTypeCellValue)) {

					Document doc = new Document();
					doc.put("SETTLEMENT_DATE", settleDate + " 12:00:00");
					doc.put("RESERVATION_ID", reservationIdCellValue);
					doc.put("RECORD_TYPE", txnTypeCellValue);
					doc.put("AMOUNT", amountCellValue);
					doc.put("TERMINAL_ID", terminalIdCellValue);
					doc.put("TXN_DATE", txnDateCellValue);
					doc.put("FILE_NAME", fileFileName);
					doc.put("FILE_TYPE", "MPR");
					doc.put("ACQUIRER", "IPAY");
					doc.put("TXNTYPE", txnTypeCellValue);
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("STATUS", "Invalid");
					doc.put("RESPONSE_MESSAGE", "Row Data Invalid");
					doc.put("MERCHANT_NAME", merchantNameCellValue);
					doc.put("BANK_NAME", bankNameCellValue);
					doc.put("PAYOUT_DATE", payoutDate);
					errorRows++;
					excepColl.insertOne(doc);
					continue;
				}

				String mprAmount = amountCellValue;

				mprAmount = mprAmount.replace("-", "");

				if (amountCellValue.contains(".")) {

					String amountCellValueSplit[] = amountCellValue.split("\\.");
					String amountCellValueDecimal = amountCellValueSplit[1];

					if (amountCellValueDecimal.length() == 1) {
						mprAmount = amountCellValue + "0";
					}
				} else {
					mprAmount = amountCellValue + ".00";
				}

				String txnType = txnTypeCellValue;

				String txnDate = null;

				txnDate = sdfyyyyMMdd.format(sdfyyyyMMddhhmmss.parse(txnDateCellValue));
				txnDate = txnDate + " 12:00:00";

				successRows++;

				if (successRows % 500 == 0) {
					logger.info("Processed" + successRows + " Rows for IPAY");
				}

				// BasicDBObject reservationIdQuery = new BasicDBObject("RESERVATION_ID",
				// reservationIdCellValue);
				// BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", "IPAY");
				// BasicDBObject txnTypeQuery = new BasicDBObject("TXNTYPE", txnType);
				// BasicDBObject txnDateQuery = new BasicDBObject("CREATE_DATE", txnDate);

				condList = new ArrayList<BasicDBObject>();
				condList.add(new BasicDBObject("ACQUIRER", "IPAY"));
				condList.add(new BasicDBObject("RESERVATION_ID", reservationIdCellValue));
				condList.add(new BasicDBObject("TXNTYPE", txnType));
				condList.add(new BasicDBObject("CREATE_DATE", txnDate));

				// BasicDBObject finalQuery = new BasicDBObject("$and", condList);

				long count = coll.countDocuments(new BasicDBObject("$and", condList));

				if (count > 1) {

					FindIterable<Document> itr = coll.find(new BasicDBObject("$and", condList));
					MongoCursor<Document> cursor = itr.iterator();

					while (cursor.hasNext()) {
						Document obj = cursor.next();
						String amount = obj.get("AMOUNT").toString();
						String settledFlag = obj.get("SETTLEMENT_FLAG").toString();

						if (settledFlag.equalsIgnoreCase("Y")) {
							continue;
						}

						BasicDBObject idQuery = new BasicDBObject("_id", obj.get("_id"));

						Document setData = new Document();
						setData.append("TERMINAL_ID", terminalIdCellValue);
						setData.append("MERCHANT_NAME", merchantNameCellValue);
						setData.append("BANK_NAME", bankNameCellValue);
						setData.append("SETTLEMENT_FLAG", "Y");
						setData.append("POST_SETTLE_CAPTURE", "N");
						setData.append("POST_SETTLED_FLAG", "N");

						setData.append("SETTLEMENT_DATE", settleDate + " 12:00:00");
						setData.append("SETTLEMENT_DATE_INDEX", settleDate.replace("-", ""));
						setData.append("PAYOUT_DATE", payoutDate + " 12:00:00");
						setData.append("PAYOUT_DATE_INDEX", payoutDate.replace("-", ""));
						setData.append("MPR_AMOUNT", mprAmount);
						if (!amount.equalsIgnoreCase(mprAmount)) {
							setData.append("AMOUNT", mprAmount);
						}

						Document update = new Document();
						update.append("$set", setData);
						coll.updateOne(idQuery, update);

					}
					cursor.close();
					continue;
				}

				if (count < 1) {

					Document doc = new Document();

					doc.put("CREATE_DATE", txnDate);
					doc.put("DATE_INDEX", txnDate.substring(0, 10).replace("-", ""));
					doc.put("RESERVATION_ID", reservationIdCellValue);
					doc.put("MERCHANT_NAME", merchantNameCellValue);
					doc.put("TERMINAL_ID", terminalIdCellValue);
					doc.put("AMOUNT", mprAmount);
					doc.put("MPR_AMOUNT", mprAmount);
					doc.put("FILE_NAME", fileFileName);
					doc.put("BANK_NAME", bankNameCellValue);
					doc.put("ACQUIRER", "IPAY");
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("STATUS", "Captured");
					doc.put("TXNTYPE", txnTypeCellValue);

					doc.put("RESPONSE_MESSAGE", "SUCCESS");
					doc.put("SETTLEMENT_FLAG", "Y");
					doc.put("POST_SETTLE_CAPTURE", "Y");
					doc.put("POST_SETTLED_FLAG", "Y");
					doc.put("SETTLEMENT_DATE", settleDate + " 12:00:00");
					doc.put("SETTLEMENT_DATE_INDEX", settleDate.replace("-", ""));
					doc.put("PAYOUT_DATE", payoutDate + " 12:00:00");
					doc.put("PAYOUT_DATE_INDEX", payoutDate.replace("-", ""));
					coll.insertOne(doc);
					continue;
				}

				FindIterable<Document> itr = coll.find(new BasicDBObject("$and", condList));

				MongoCursor<Document> cursor = itr.iterator();

				while (cursor.hasNext()) {

					Document obj = cursor.next();
					String amount = obj.get("AMOUNT").toString();
					BasicDBObject idQuery = new BasicDBObject("_id", obj.get("_id"));

					Document setData = new Document();
					setData.append("TERMINAL_ID", terminalIdCellValue);
					setData.append("MERCHANT_NAME", merchantNameCellValue);
					setData.append("BANK_NAME", bankNameCellValue);
					setData.append("SETTLEMENT_FLAG", "Y");
					setData.append("POST_SETTLE_CAPTURE", "N");
					setData.append("POST_SETTLED_FLAG", "N");

					setData.append("SETTLEMENT_DATE", settleDate + " 12:00:00");
					setData.append("SETTLEMENT_DATE_INDEX", settleDate.replace("-", ""));

					setData.append("PAYOUT_DATE", payoutDate + " 12:00:00");
					setData.append("PAYOUT_DATE_INDEX", payoutDate.replace("-", ""));
					setData.append("MPR_AMOUNT", mprAmount);
					if (!amount.equalsIgnoreCase(mprAmount)) {
						setData.append("AMOUNT", mprAmount);
					}

					Document update = new Document();
					update.append("$set", setData);
					coll.updateOne(idQuery, update);

				}
				cursor.close();

			}

			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Processed");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);

		} catch (Exception ex) {
			logger.error("Exception refundBookings ", ex);

			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Error");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);

		}
	}

	public List<List<Document>> splitArray(List<Document> list, int chunkSize) {
		if (chunkSize <= 0) {
			throw new IllegalArgumentException("Invalid chunk size: " + chunkSize);
		}
		List<List<Document>> chunkList = new ArrayList<>(list.size() / chunkSize);
		for (int i = 0; i < list.size(); i += chunkSize) {
			chunkList.add(list.subList(i, i + chunkSize >= list.size() ? list.size() - 1 : i + chunkSize));
		}
		return chunkList;
	}

	@SuppressWarnings("unchecked")
	public void uploadBOBStatement(File file, String fileFileName) {

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

		MongoCollection<Document> excepColl = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECON_EXCEPTION_COLLECTION.getValue()));

		MongoCollection<Document> fileColl = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

		MongoCollection<Document> stmtColl = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

		int totalRows = 0;
		int errorRows = 0;
		int successRows = 0;

		List<BasicDBObject> conList = new ArrayList<BasicDBObject>();

		try {

			List<Row> results = new ArrayList<Row>();

			DataFormatter dataFormatter = new DataFormatter();
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file.getAbsolutePath()));
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = workbook.getSheetAt(0);

			for (Row row : sheet) { // iterate over all rows in the sheet except 1

				if (totalRows == 0) {
					totalRows++;
					continue;
				}

				results.add(row);
				totalRows++;

			}
			logger.info("Total rows for BOB = " + totalRows);

			Cell detailsCell = null;
			String detailsCellValue = null;

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			SimpleDateFormat sdfyyyyMMddhhmmss = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			SimpleDateFormat sdfddMMyyyy = new SimpleDateFormat("dd-MM-yyyy");

			List<BasicDBObject> condList = new ArrayList<BasicDBObject>();

			String txnDate = null;
			String settleDate = null;
			String bankTxnNum = null;
			String amount = null;

			String txnDateFormatted = null;
			String settleDateFormatted = null;
			String formatAmt = null;

			for (Row row : results) {

				successRows++;
				if (successRows % 500 == 0) {
					logger.info("Processed rows for BOB = " + successRows);
				}

				condList.clear();
				conList.clear();

				detailsCell = row.getCell(0);
				detailsCellValue = dataFormatter.formatCellValue(detailsCell, formulaEvaluator).trim();

				// SALE ENTRIES
				if (detailsCellValue.contains("EBANK")) {

					String formatLineArr[] = detailsCellValue.split(" ");
					List<String> stmtList = new ArrayList<String>();

					for (String arr : formatLineArr) {
						if (arr.length() > 1) {
							stmtList.add(arr);
						}

					}

					txnDate = stmtList.get(1);
					settleDate = stmtList.get(0);

					String statementArr[] = stmtList.get(2).split("/");
					bankTxnNum = statementArr[1];
					amount = stmtList.get(3).replace(" ", "");
					amount = amount.replace(",", "");

					txnDateFormatted = sdfyyyyMMddhhmmss.format(sdfddMMyyyy.parse(txnDate));
					settleDateFormatted = sdfyyyyMMddhhmmss.format(sdfddMMyyyy.parse(settleDate));
					bankTxnNum = bankTxnNum.trim();

					// Find Sale Entries from Recon DATA and mark as settled

					condList = new ArrayList<BasicDBObject>();
					condList.add(new BasicDBObject("ACQUIRER", "BOB"));
					condList.add(new BasicDBObject("BANK_TXN_NUMBER", bankTxnNum));
					condList.add(new BasicDBObject("TXNTYPE", "SALE"));

					long count = coll.countDocuments(new BasicDBObject("$and", condList));

					if (count < 1) {

						Document doc = new Document();

						doc.put("CREATE_DATE", txnDateFormatted);
						doc.put("DATE_INDEX", txnDateFormatted.substring(0, 10).replace("-", ""));
						doc.put("RESERVATION_ID", "");
						doc.put("BANK_TXN_NUMBER", bankTxnNum);
						doc.put("MERCHANT_NAME", "");
						doc.put("TERMINAL_ID", "");
						doc.put("AMOUNT", amount);
						doc.put("MPR_AMOUNT", amount);
						doc.put("FILE_NAME", fileFileName);
						doc.put("BANK_NAME", "");
						doc.put("ACQUIRER", "BOB");
						doc.put("UPLOAD_DATE", dateNow);
						doc.put("STATUS", "Captured");
						doc.put("TXNTYPE", "SALE");
						doc.put("RESPONSE_MESSAGE", "SUCCESS");
						doc.put("SETTLEMENT_FLAG", "Y");
						doc.put("POST_SETTLE_CAPTURE", "Y");
						doc.put("POST_SETTLED_FLAG", "Y");
						doc.put("SETTLEMENT_DATE", settleDateFormatted);
						doc.put("SETTLEMENT_DATE_INDEX", settleDateFormatted.substring(0, 10).replace("-", ""));
						doc.put("PAYOUT_DATE", settleDateFormatted);
						doc.put("PAYOUT_DATE_INDEX", settleDateFormatted.substring(0, 10).replace("-", ""));
						coll.insertOne(doc);
						// continue;

					}

					else {
						FindIterable<Document> itr = coll.find(new BasicDBObject("$and", condList));
						MongoCursor<Document> cursor = itr.iterator();

						while (cursor.hasNext()) {

							Document doc = new Document();
							doc = cursor.next();

							String dbAmt = doc.get("AMOUNT").toString();
							BasicDBObject idQuery = new BasicDBObject("_id", doc.get("_id"));

							Document setData = new Document();
							setData.append("SETTLEMENT_FLAG", "Y");
							setData.append("SETTLEMENT_DATE", settleDateFormatted);
							setData.append("SETTLEMENT_DATE_INDEX",
									settleDateFormatted.substring(0, 10).replace("-", ""));
							setData.append("PAYOUT_DATE", settleDateFormatted);
							setData.append("PAYOUT_DATE_INDEX", settleDateFormatted.substring(0, 10).replace("-", ""));
							setData.append("MPR_AMOUNT", amount);

							if (!amount.equalsIgnoreCase(dbAmt)) {
								setData.append("AMOUNT_MISMATCH", "Y");
							}

							Document update = new Document();
							update.append("$set", setData);
							coll.updateOne(idQuery, update);

						}
					}

					conList.add(new BasicDBObject("CAPTURE_DATE", txnDateFormatted));
					conList.add(new BasicDBObject("SETTLEMENT_DATE", settleDateFormatted));
					conList.add(new BasicDBObject("TXNTYPE", "SALE"));
					conList.add(new BasicDBObject("ACQUIRER", "BOB"));

					long recCount = stmtColl.countDocuments(new BasicDBObject("$and", conList));

					if (recCount < 1) {

						Document doc = new Document();

						doc.put("CAPTURE_DATE", txnDateFormatted);
						doc.put("BANK_TXN_NUMBER", "");
						doc.put("AMOUNT", amount);
						doc.put("FILE_NAME", fileFileName);
						doc.put("ACQUIRER", "BOB");
						doc.put("UPLOAD_DATE", dateNow);
						doc.put("TXNTYPE", "SALE");
						doc.put("SETTLEMENT_DATE", settleDateFormatted);
						doc.put("PAYOUT_DATE", settleDateFormatted);
						stmtColl.insertOne(doc);
						continue;
					}

					else {

						FindIterable<Document> itr1 = stmtColl.find(new BasicDBObject("$and", conList));
						MongoCursor<Document> cursor1 = itr1.iterator();

						Document doc = cursor1.next();

						String stmtAmount = doc.get("AMOUNT").toString();
						double stmtAmountdbl = Double.valueOf(stmtAmount);
						double amountdbl = Double.valueOf(amount);

						stmtAmountdbl = stmtAmountdbl + amountdbl;
						formatAmt = String.format("%.2f", stmtAmountdbl);

						Document query = new Document();
						query.append("_id", doc.get("_id"));
						Document setData = new Document();
						setData.append("AMOUNT", formatAmt);
						Document update = new Document();
						update.append("$set", setData);
						stmtColl.updateOne(query, update);
					}

				}

				// REVERSAL
				else if (detailsCellValue.contains("Reversal") || detailsCellValue.contains("REVERSAL")) {

					System.out.println("\n");

					// String formatLine = detailsCellValue.replace(" ", "");
					String formatLineArr[] = detailsCellValue.split(" ");

					List<String> stmtList = new ArrayList<String>();

					for (String arr : formatLineArr) {
						if (arr.length() > 1) {
							stmtList.add(arr);
						}

					}

					txnDate = stmtList.get(1);
					settleDate = stmtList.get(0);
					amount = stmtList.get(4).replace(",", "");

					// Update all refund entries as settled

					conList.clear();
					conList.add(new BasicDBObject("CREATE_DATE", txnDateFormatted));
					conList.add(new BasicDBObject("TXNTYPE", "REFUND"));
					conList.add(new BasicDBObject("ACQUIRER", "BOB"));

					Document setData = new Document();
					setData.append("SETTLEMENT_FLAG", "Y");
					setData.append("SETTLEMENT_DATE", settleDateFormatted);
					setData.append("SETTLEMENT_DATE_INDEX", settleDateFormatted.substring(0, 10).replace("-", ""));
					setData.append("PAYOUT_DATE", settleDateFormatted);
					setData.append("PAYOUT_DATE_INDEX", settleDateFormatted.substring(0, 10).replace("-", ""));

					Document update = new Document();
					update.append("$set", setData);
					coll.updateMany(new BasicDBObject("$and", conList), update);

					// Add reversal to Collection

					Document doc = new Document();
					doc.put("CAPTURE_DATE", txnDateFormatted);
					doc.put("BANK_TXN_NUMBER", "");
					doc.put("AMOUNT", amount);
					doc.put("FILE_NAME", fileFileName);
					doc.put("ACQUIRER", "BOB");
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("TXNTYPE", "REVERSAL");
					doc.put("SETTLEMENT_DATE", settleDateFormatted);
					doc.put("PAYOUT_DATE", settleDateFormatted);
					stmtColl.insertOne(doc);
				}

			}

			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Processed");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);
			logger.info("Processed rows for BOB = " + successRows);
		} catch (Exception ex) {
			logger.error("Exception refundBookings ", ex);

			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Error");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);

		}
	}

	@SuppressWarnings("unchecked")
	public void uploadAllahabadStatement(File file, String fileFileName) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> fileColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

			MongoCollection<Document> statementColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			List<Row> results = new ArrayList<Row>();

			DataFormatter dataFormatter = new DataFormatter();
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file.getAbsolutePath()));
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = workbook.getSheetAt(0);

			int totalRows = 0;
			int errorRows = 0;
			int successRows = 0;

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			for (Row row : sheet) { // iterate over all rows in the sheet except 1

				if (totalRows == 0) {
					totalRows++;
					continue;
				}

				results.add(row);
				totalRows++;
			}

			for (Row row : results) {

				Cell paymentDateCell = row.getCell(3);
				Cell amountCell = row.getCell(7);
				Cell txnTypeCell = row.getCell(8);
				Cell bankTxnNumberCell = row.getCell(13);
				Cell nameCell = row.getCell(2);

				String paymentDateCellValue = dataFormatter.formatCellValue(paymentDateCell, formulaEvaluator).trim();
				String bankTxnNumberCellValue = dataFormatter.formatCellValue(bankTxnNumberCell, formulaEvaluator)
						.trim();
				String amountCellValue = dataFormatter.formatCellValue(amountCell, formulaEvaluator).trim();

				String txnTypeCellValue = dataFormatter.formatCellValue(txnTypeCell, formulaEvaluator).trim();
				String nameCellValue = dataFormatter.formatCellValue(nameCell, formulaEvaluator).trim();

				if (StringUtils.isBlank(txnTypeCellValue)) {
					continue;
				}

				if (!txnTypeCellValue.equalsIgnoreCase("CR") && !txnTypeCellValue.equalsIgnoreCase("DR")) {
					continue;
				}

				if (StringUtils.isBlank(paymentDateCellValue) || StringUtils.isBlank(bankTxnNumberCellValue)
						|| StringUtils.isBlank(amountCellValue)) {

					Document doc = new Document();
					doc.put("PAYMENT_DATE", paymentDateCellValue);
					doc.put("BANK_TXN_NUMBER", bankTxnNumberCellValue);
					doc.put("AMOUNT", amountCellValue);
					doc.put("FILE_NAME", fileFileName);
					if (txnTypeCellValue.equalsIgnoreCase("CR")) {
						doc.put("TXNTYPE", "SALE");
					}
					if (txnTypeCellValue.equalsIgnoreCase("DR")) {
						doc.put("TXNTYPE", "REFUND");
					}

					doc.put("UPLOAD_DATE", dateNow);
					doc.put("STATUS", "Invalid");
					doc.put("RESPONSE_MESSAGE", "Row Data Invalid");
					errorRows++;
					// excepColl.insertOne(doc);
					continue;
				}

				successRows++;

				SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yy");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				SimpleDateFormat sdf3 = new SimpleDateFormat("dd/MM/yy");
				SimpleDateFormat sdf4 = new SimpleDateFormat("dd/MM/yyyy");
				String payDateFormat = sdf2.format(sdf1.parse(paymentDateCellValue));

				String mprAmount = amountCellValue;
				mprAmount = mprAmount.replace("-", "");

				if (amountCellValue.contains(".")) {

					String amountCellValueSplit[] = amountCellValue.split("\\.");
					String amountCellValueDecimal = amountCellValueSplit[1];

					if (amountCellValueDecimal.length() == 1) {
						mprAmount = amountCellValue + "0";
					}
				} else {
					mprAmount = amountCellValue + ".00";
				}
				mprAmount = mprAmount.replace("-", "");

				if (txnTypeCellValue.equalsIgnoreCase("CR")) {
					
					
					String txnDateFormat = null;
					String bankTxnNumberCellValueSplit[] = bankTxnNumberCellValue.split("\\.");
					String txnDate = bankTxnNumberCellValueSplit[1].trim();
					String year = txnDate.split("/")[2];
					if (year.length() > 2) {
						txnDateFormat = sdf2.format(sdf4.parse(txnDate));
					}
					else {
						txnDateFormat = sdf2.format(sdf3.parse(txnDate));
					}
					

					Document doc = new Document();
					doc.put("CAPTURE_DATE", txnDateFormat);
					doc.put("BANK_TXN_NUMBER", bankTxnNumberCellValue);
					doc.put("AMOUNT", mprAmount);
					doc.put("FILE_NAME", fileFileName);
					doc.put("ACQUIRER", "ALLAHABAD BANK");
					doc.put("NAME", nameCellValue);
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("TXNTYPE", "SALE");
					doc.put("SETTLEMENT_DATE", payDateFormat);
					doc.put("PAYOUT_DATE", payDateFormat);
					statementColl.insertOne(doc);
					
					
					// update all sale transactions with settlement date
					BasicDBObject acqQuery = new BasicDBObject("ACQUIRER","ALLAHABAD BANK");
					BasicDBObject capDateQuery = new BasicDBObject("CREATE_DATE",txnDateFormat);
					BasicDBObject txnTypQuery = new BasicDBObject("TXNTYPE","SALE");
					
					
					List<BasicDBObject> saleConList = new ArrayList<BasicDBObject>();
					saleConList.add(acqQuery);
					saleConList.add(capDateQuery);
					saleConList.add(txnTypQuery);
					
					BasicDBObject txnUpdateQuery = new BasicDBObject("$and",saleConList);
					
					Document setData = new Document();
					setData.append("SETTLEMENT_FLAG", "Y");
					setData.append("SETTLEMENT_DATE", payDateFormat);
					setData.append("SETTLEMENT_DATE_INDEX", payDateFormat.substring(0, 10).replace("-", ""));

					Document update = new Document();
					update.append("$set", setData);
					coll.updateMany(txnUpdateQuery, update);

				}

				else if (txnTypeCellValue.equalsIgnoreCase("DR")) {

					String bankTxnNumberCellValueSplit[] = bankTxnNumberCellValue.split("-");
					String resId = bankTxnNumberCellValueSplit[bankTxnNumberCellValueSplit.length - 1].trim();

					// Find and update or create refund entry

					BasicDBObject resIdQuery = new BasicDBObject("RESERVATION_ID", resId);
					BasicDBObject txnTypeQuery = new BasicDBObject("TXNTYPE", "REFUND");
					BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", "ALLAHABAD BANK");
					BasicDBObject settleQuery = new BasicDBObject("SETTLEMENT_FLAG", "N");
					
					List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
					condList.add(acquirerQuery);
					condList.add(txnTypeQuery);
					condList.add(resIdQuery);
					condList.add(settleQuery);
					
					BasicDBObject finalQuery = new BasicDBObject("$and", condList);

					long count = coll.countDocuments(finalQuery);

					if (count == 0) {

						Document doc = new Document();

						doc.put("CREATE_DATE", payDateFormat);
						doc.put("DATE_INDEX", payDateFormat.substring(0, 10).replace("-", ""));
						doc.put("RESERVATION_ID", resId);
						doc.put("BANK_TXN_NUMBER", "");
						doc.put("TERMINAL_ID", "");
						doc.put("AMOUNT", mprAmount);
						doc.put("MPR_AMOUNT", mprAmount);
						doc.put("FILE_NAME", fileFileName);
						doc.put("CARD_MASK", "");
						doc.put("RECORD_TYPE", txnTypeCellValue);
						doc.put("ACQUIRER", "ALLAHABAD BANK");
						doc.put("UPLOAD_DATE", dateNow);
						doc.put("TXNTYPE", "REFUND");

						doc.put("RESPONSE_MESSAGE", "SUCCESS");
						doc.put("SETTLEMENT_FLAG", "Y");
						doc.put("POST_SETTLE_CAPTURE", "Y");
						doc.put("POST_SETTLED_FLAG", "Y");

						doc.put("SETTLEMENT_DATE", payDateFormat);
						doc.put("SETTLEMENT_DATE_INDEX", payDateFormat.substring(0, 10).replace("-", ""));
						coll.insertOne(doc);

					} else {
						FindIterable<Document> itr = coll.find(new BasicDBObject("$and", condList));
						MongoCursor<Document> cursor = itr.iterator();

						while (cursor.hasNext()) {

							Document doc = new Document();
							doc = cursor.next();
							
							// Skip transactions already settled
							if (doc.get("SETTLEMENT_FLAG") != null) {
								String settlementFlag = doc.get("SETTLEMENT_FLAG").toString();
								if (settlementFlag.equalsIgnoreCase("Y")) {
									continue;
								}
							}
							String dbamt = doc.get("AMOUNT").toString();

							Document query = new Document();
							query.append("_id", doc.get("_id"));
							Document setData = new Document();
							setData.append("MPR_AMOUNT", mprAmount);
							setData.append("SETTLEMENT_FLAG", "Y");
							setData.append("SETTLEMENT_DATE", payDateFormat);
							setData.append("SETTLEMENT_DATE_INDEX", payDateFormat.substring(0, 10).replace("-", ""));

							if (!mprAmount.equalsIgnoreCase(dbamt)) {
								setData.append("AMOUNT_MISMATCH", "Y");
							}

							Document update = new Document();
							update.append("$set", setData);
							coll.updateOne(query, update);
						}

					}

					// Create new entry or update value in Statement coll for Refund entry for the
					// payout date

					BasicDBObject payDateQuery = new BasicDBObject("SETTLEMENT_DATE", payDateFormat);
					BasicDBObject txnTypeStmQuery = new BasicDBObject("TXNTYPE", "REFUND");
					BasicDBObject acquirerStmtQuery = new BasicDBObject("ACQUIRER", "ALLAHABAD BANK");

					List<BasicDBObject> stmtConList = new ArrayList<BasicDBObject>();
					stmtConList.add(payDateQuery);
					stmtConList.add(txnTypeStmQuery);
					stmtConList.add(acquirerStmtQuery);

					BasicDBObject stmtQuery = new BasicDBObject("$and", stmtConList);

					long countStmt = statementColl.countDocuments(stmtQuery);

					if (countStmt == 0) {

						Document doc = new Document();
						doc.put("CAPTURE_DATE", "");
						doc.put("BANK_TXN_NUMBER", bankTxnNumberCellValue);
						doc.put("AMOUNT", mprAmount);
						doc.put("FILE_NAME", fileFileName);
						doc.put("ACQUIRER", "ALLAHABAD BANK");
						doc.put("NAME", nameCellValue);
						doc.put("UPLOAD_DATE", dateNow);
						doc.put("TXNTYPE", "REFUND");
						doc.put("SETTLEMENT_DATE", payDateFormat);
						doc.put("PAYOUT_DATE", payDateFormat);
						statementColl.insertOne(doc);

					}

					else {

						FindIterable<Document> itr = statementColl.find(stmtQuery);
						MongoCursor<Document> cursor = itr.iterator();

						while (cursor.hasNext()) {

							Document doc = new Document();
							doc = cursor.next();
							String dbamt = doc.get("AMOUNT").toString();

							double updateAmt = Double.valueOf(mprAmount) + Double.valueOf(dbamt);
							String updateAmtStr = String.format("%.2f", updateAmt);

							Document query = new Document();
							query.append("_id", doc.get("_id"));
							Document setData = new Document();
							setData.append("AMOUNT", updateAmtStr);
							Document update = new Document();
							update.append("$set", setData);
							statementColl.updateOne(query, update);
						}

					}

				}

			}

			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Processed");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);

		} catch (Exception ex) {
			logger.error("Exception refundBookings ", ex);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public void uploadMaharashtraStatement(File file, String fileFileName) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> fileColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

			MongoCollection<Document> statementColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			List<Row> results = new ArrayList<Row>();

			DataFormatter dataFormatter = new DataFormatter();
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file.getAbsolutePath()));
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = workbook.getSheetAt(0);

			int totalRows = 0;
			int errorRows = 0;
			int successRows = 0;

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			for (Row row : sheet) { // iterate over all rows in the sheet except 1

				if (totalRows == 0) {
					totalRows++;
					continue;
				}

				results.add(row);
				totalRows++;
			}

			for (Row row : results) {

				Cell paymentDateCell = row.getCell(0);
				Cell bankTxnNumberCell = row.getCell(2);
				Cell amountDebitCell = row.getCell(5);
				Cell amountCreditCell = row.getCell(6);

				String paymentDateCellValue = dataFormatter.formatCellValue(paymentDateCell, formulaEvaluator).trim();
				String bankTxnNumberCellValue = dataFormatter.formatCellValue(bankTxnNumberCell, formulaEvaluator)
						.trim();
				String amountDebitCellValue = dataFormatter.formatCellValue(amountDebitCell, formulaEvaluator).trim();
				if (StringUtils.isNotBlank(amountDebitCellValue)) {
					amountDebitCellValue = amountDebitCellValue.replace(",", "");
				}
				String amountCreditCellValue = dataFormatter.formatCellValue(amountCreditCell, formulaEvaluator).trim();
				if (StringUtils.isNotBlank(amountCreditCellValue)) {
					amountCreditCellValue = amountCreditCellValue.replace(",", "");
				}

				if (StringUtils.isBlank(paymentDateCellValue)) {
					continue;
				}

				if (StringUtils.isBlank(paymentDateCellValue) || StringUtils.isBlank(bankTxnNumberCellValue)) {
					continue;
				}

				successRows++;

				SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MMM-yy");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				SimpleDateFormat sdf3 = new SimpleDateFormat("dd/MM/yyyy");
				
				// Payment Date
				String payDateFormat = sdf2.format(sdf3.parse(paymentDateCellValue));
				
				// SALE
				if (!StringUtils.isBlank(amountCreditCellValue)) {
					
					String txnDate = bankTxnNumberCellValue.replace("MP-IRCTC-NG-Online BOM ", "");
					txnDate = txnDate.replace(" FRM BOM ONLINE PAYMENT POOL ACCOUNT", "");
					txnDate = txnDate.replace(" ", "-");
					String txnDateFormat = sdf2.format(sdf1.parse(txnDate));
					
					logger.info("BANK OF MAHARASHTRA , Update sale for txn date" + txnDate);
					Document doc = new Document();
					doc.put("CAPTURE_DATE", txnDateFormat);
					doc.put("BANK_TXN_NUMBER", bankTxnNumberCellValue);
					doc.put("AMOUNT", amountCreditCellValue);
					doc.put("FILE_NAME", fileFileName);
					doc.put("ACQUIRER", "BANK OF MAHARASHTRA");
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("TXNTYPE", "SALE");
					doc.put("SETTLEMENT_DATE", payDateFormat);
					doc.put("PAYOUT_DATE", payDateFormat);
					statementColl.insertOne(doc);
					
					
					// Mark all sale as settled
					
					BasicDBObject acqQuery = new BasicDBObject("ACQUIRER","BANK OF MAHARASHTRA");
					BasicDBObject capDateQuery = new BasicDBObject("CREATE_DATE",txnDateFormat);
					BasicDBObject txnTypQuery = new BasicDBObject("TXNTYPE","SALE");
					
					
					List<BasicDBObject> saleConList = new ArrayList<BasicDBObject>();
					saleConList.add(acqQuery);
					saleConList.add(capDateQuery);
					saleConList.add(txnTypQuery);
					
					BasicDBObject txnUpdateQuery = new BasicDBObject("$and",saleConList);
					
					Document setData = new Document();
					setData.append("SETTLEMENT_FLAG", "Y");
					setData.append("SETTLEMENT_DATE", payDateFormat);
					setData.append("SETTLEMENT_DATE_INDEX", payDateFormat.substring(0, 10).replace("-", ""));

					Document update = new Document();
					update.append("$set", setData);
					coll.updateMany(txnUpdateQuery, update);
					
				}
				
				
				// REFUND
				if (!StringUtils.isBlank(amountDebitCellValue)) {
				
					// Update entry for Refund from Data collection
					
					
					String resIdArr [] = bankTxnNumberCellValue.replace("MP-IRCTC-NG-IRCTC Ref No:", "").split("\r\n");
					String resId = resIdArr [0].trim().substring(0, 15);
					
					logger.info("BANK OF MAHARASHTRA , Update refund for res id " + resId);
					BasicDBObject resIdQuery = new BasicDBObject("RESERVATION_ID", resId);
					BasicDBObject txnTypeQuery = new BasicDBObject("TXNTYPE", "REFUND");
					BasicDBObject acquirerQuery = new BasicDBObject("ACQUIRER", "BANK OF MAHARASHTRA");

					List<BasicDBObject> condList = new ArrayList<BasicDBObject>();
					condList.add(acquirerQuery);
					condList.add(txnTypeQuery);
					condList.add(resIdQuery);

					BasicDBObject finalQuery = new BasicDBObject("$and", condList);

					long count = coll.countDocuments(finalQuery);

					if (count == 0) {

						Document doc = new Document();

						doc.put("CREATE_DATE", payDateFormat);
						doc.put("DATE_INDEX", payDateFormat.substring(0, 10).replace("-", ""));
						doc.put("RESERVATION_ID", resId);
						doc.put("BANK_TXN_NUMBER", "");
						doc.put("TERMINAL_ID", "");
						doc.put("AMOUNT", amountDebitCellValue);
						doc.put("MPR_AMOUNT", amountDebitCellValue);
						doc.put("FILE_NAME", fileFileName);
						doc.put("CARD_MASK", "");
						doc.put("ACQUIRER", "BANK OF MAHARASHTRA");
						doc.put("UPLOAD_DATE", dateNow);
						doc.put("TXNTYPE", "REFUND");

						doc.put("RESPONSE_MESSAGE", "SUCCESS");
						doc.put("SETTLEMENT_FLAG", "Y");
						doc.put("POST_SETTLE_CAPTURE", "Y");
						doc.put("POST_SETTLED_FLAG", "Y");

						doc.put("SETTLEMENT_DATE", payDateFormat);
						doc.put("SETTLEMENT_DATE_INDEX", payDateFormat.substring(0, 10).replace("-", ""));
						coll.insertOne(doc);

					} else {
						FindIterable<Document> itr = coll.find(new BasicDBObject("$and", condList));
						MongoCursor<Document> cursor = itr.iterator();

						while (cursor.hasNext()) {

							Document doc = new Document();
							doc = cursor.next();
							String dbamt = doc.get("AMOUNT").toString();

							Document query = new Document();
							query.append("_id", doc.get("_id"));
							Document setData = new Document();
							setData.append("MPR_AMOUNT", amountDebitCellValue);
							setData.append("SETTLEMENT_FLAG", "Y");
							setData.append("SETTLEMENT_DATE", payDateFormat);
							setData.append("SETTLEMENT_DATE_INDEX", payDateFormat.substring(0, 10).replace("-", ""));

							if (!amountDebitCellValue.equalsIgnoreCase(dbamt)) {
								setData.append("AMOUNT_MISMATCH", "Y");
							}

							Document update = new Document();
							update.append("$set", setData);
							coll.updateOne(query, update);
						}
						
					}
						
						// Update refund in Statement collection
					
					
						BasicDBObject payDateQuery = new BasicDBObject("SETTLEMENT_DATE", payDateFormat);
						BasicDBObject txnTypeStmQuery = new BasicDBObject("TXNTYPE", "REFUND");
						BasicDBObject acquirerStmtQuery = new BasicDBObject("ACQUIRER", "BANK OF MAHARASHTRA");

						List<BasicDBObject> stmtConList = new ArrayList<BasicDBObject>();
						stmtConList.add(payDateQuery);
						stmtConList.add(txnTypeStmQuery);
						stmtConList.add(acquirerStmtQuery);

						BasicDBObject stmtQuery = new BasicDBObject("$and", stmtConList);

						long countStmt = statementColl.countDocuments(stmtQuery);

						if (countStmt == 0) {

							Document doc = new Document();
							doc.put("CAPTURE_DATE", "");
							doc.put("BANK_TXN_NUMBER", bankTxnNumberCellValue);
							doc.put("AMOUNT", amountDebitCellValue);
							doc.put("FILE_NAME", fileFileName);
							doc.put("ACQUIRER", "BANK OF MAHARASHTRA");
							doc.put("UPLOAD_DATE", dateNow);
							doc.put("TXNTYPE", "REFUND");
							doc.put("SETTLEMENT_DATE", payDateFormat);
							doc.put("PAYOUT_DATE", payDateFormat);
							statementColl.insertOne(doc);

						}

						else {

							FindIterable<Document> payoutItr = statementColl.find(stmtQuery);
							MongoCursor<Document> payoutCursor = payoutItr.iterator();

							while (payoutCursor.hasNext()) {

								Document doc = new Document();
								doc = payoutCursor.next();
								String dbamt = doc.get("AMOUNT").toString();

								double updateAmt = Double.valueOf(amountDebitCellValue) + Double.valueOf(dbamt);
								String updateAmtStr = String.format("%.2f", updateAmt);

								Document query = new Document();
								query.append("_id", doc.get("_id"));
								Document setData = new Document();
								setData.append("AMOUNT", updateAmtStr);
								Document update = new Document();
								update.append("$set", setData);
								statementColl.updateOne(query, update);
							}
						}
						
					}

			}
			
			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Processed");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);
			
		} catch (Exception ex) {
			logger.error("Exception refundBookings ", ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void uploadCorporationStatement(File file, String fileFileName) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> fileColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

			MongoCollection<Document> statementColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			List<Row> results = new ArrayList<Row>();

			DataFormatter dataFormatter = new DataFormatter();
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file.getAbsolutePath()));
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = workbook.getSheetAt(0);

			int totalRows = 0;
			int errorRows = 0;
			int successRows = 0;

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			for (Row row : sheet) { // iterate over all rows in the sheet except 1

				if (totalRows == 0) {
					totalRows++;
					continue;
				}

				results.add(row);
				totalRows++;
			}

			for (Row row : results) {

				Cell paymentDateCell = row.getCell(0);
				Cell particularCell = row.getCell(1);
				Cell amountDebitCell = row.getCell(4);
				Cell amountCreditCell = row.getCell(5);

				String paymentDateCellValue = dataFormatter.formatCellValue(paymentDateCell, formulaEvaluator).trim();
				String particularCellValue = dataFormatter.formatCellValue(particularCell, formulaEvaluator)
						.trim();
				String amountDebitCellValue = dataFormatter.formatCellValue(amountDebitCell, formulaEvaluator).trim();
				if (StringUtils.isNotBlank(amountDebitCellValue)) {
					amountDebitCellValue = amountDebitCellValue.replace(",", "");
				}
				String amountCreditCellValue = dataFormatter.formatCellValue(amountCreditCell, formulaEvaluator).trim();
				if (StringUtils.isNotBlank(amountCreditCellValue)) {
					amountCreditCellValue = amountCreditCellValue.replace(",", "");
				}

				if (StringUtils.isBlank(paymentDateCellValue)) {
					continue;
				}

				if (StringUtils.isBlank(particularCellValue) || StringUtils.isBlank(particularCellValue)) {
					continue;
				}

				if (!particularCellValue.contains("CRIS BKG AMT DATED") && !particularCellValue.contains("CRIS REFUND AMOUNT DATED")  ) {
					continue;
				}
				
				successRows++;

				SimpleDateFormat sdf1 = new SimpleDateFormat("ddMMyyyy");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				SimpleDateFormat sdf3 = new SimpleDateFormat("dd/MM/yyyy");
				
				// Payment Date
				String payDateFormat = sdf2.format(sdf3.parse(paymentDateCellValue));
				
				// SALE
				if (particularCellValue.contains("CRIS BKG AMT DATED")) {
					
					String statementSale = particularCellValue.replace("\r\n", "");
					statementSale = statementSale.replace("\n", "");
					statementSale = statementSale.replace("CRIS BKG AMT DATED ","");
					statementSale = statementSale.replace(" ", "");
					String line1 = statementSale.substring(0, 8);
					
					String txnDateFormat = sdf2.format(sdf1.parse(line1));
					
					Document doc = new Document();
					doc.put("CAPTURE_DATE", txnDateFormat);
					doc.put("BANK_TXN_NUMBER", particularCellValue);
					doc.put("AMOUNT", amountCreditCellValue);
					doc.put("FILE_NAME", fileFileName);
					doc.put("ACQUIRER", "CORPORATION BANK");
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("TXNTYPE", "SALE");
					doc.put("SETTLEMENT_DATE", payDateFormat);
					doc.put("PAYOUT_DATE", payDateFormat);
					statementColl.insertOne(doc);
					
					
					// Mark all sale as settled
					
					BasicDBObject acqQuery = new BasicDBObject("ACQUIRER","CORPORATION BANK");
					BasicDBObject capDateQuery = new BasicDBObject("CREATE_DATE",txnDateFormat);
					BasicDBObject txnTypQuery = new BasicDBObject("TXNTYPE","SALE");
					
					
					List<BasicDBObject> saleConList = new ArrayList<BasicDBObject>();
					saleConList.add(acqQuery);
					saleConList.add(capDateQuery);
					saleConList.add(txnTypQuery);
					
					BasicDBObject txnUpdateQuery = new BasicDBObject("$and",saleConList);
					
					Document setData = new Document();
					setData.append("SETTLEMENT_FLAG", "Y");
					setData.append("SETTLEMENT_DATE", payDateFormat);
					setData.append("SETTLEMENT_DATE_INDEX", payDateFormat.substring(0, 10).replace("-", ""));

					Document update = new Document();
					update.append("$set", setData);
					coll.updateMany(txnUpdateQuery, update);
					
				}
				
				
				// REFUND
				if (particularCellValue.contains("CRIS REFUND AMOUNT DATED")) {
				
					String statementRefund = particularCellValue;
					statementRefund = statementRefund.replace("\r\n", "");
					statementRefund = statementRefund.replace("\n", "");
					statementRefund = statementRefund.replace("CRIS REFUND AMOUNT DATED", "");
					statementRefund = statementRefund.replace(" ", "");
					String line2 = statementRefund.substring(0, 8);
					String txnDateFormat = sdf2.format(sdf1.parse(line2));
					
					Document doc = new Document();
					doc.put("CAPTURE_DATE", txnDateFormat);
					doc.put("BANK_TXN_NUMBER", particularCellValue);
					doc.put("AMOUNT", amountDebitCellValue);
					doc.put("FILE_NAME", fileFileName);
					doc.put("ACQUIRER", "CORPORATION BANK");
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("TXNTYPE", "REFUND");
					doc.put("SETTLEMENT_DATE", payDateFormat);
					doc.put("PAYOUT_DATE", payDateFormat);
					statementColl.insertOne(doc);
					
					
					// Mark all refund as settled
					
					BasicDBObject acqQuery = new BasicDBObject("ACQUIRER","CORPORATION BANK");
					BasicDBObject capDateQuery = new BasicDBObject("CREATE_DATE",txnDateFormat);
					BasicDBObject txnTypQuery = new BasicDBObject("TXNTYPE","REFUND");
					
					
					List<BasicDBObject> refundConList = new ArrayList<BasicDBObject>();
					refundConList.add(acqQuery);
					refundConList.add(capDateQuery);
					refundConList.add(txnTypQuery);
					
					BasicDBObject txnUpdateQuery = new BasicDBObject("$and",refundConList);
					
					Document setData = new Document();
					setData.append("SETTLEMENT_FLAG", "Y");
					setData.append("SETTLEMENT_DATE", payDateFormat);
					setData.append("SETTLEMENT_DATE_INDEX", payDateFormat.substring(0, 10).replace("-", ""));

					Document update = new Document();
					update.append("$set", setData);
					coll.updateMany(txnUpdateQuery, update);
						
					}

			}
			
			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Processed");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);
			
		} catch (Exception ex) {
			logger.error("Exception refundBookings ", ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void uploadKarurStatement(File file, String fileFileName) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> fileColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_FILE_COLLECTION.getValue()));

			MongoCollection<Document> statementColl = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_STATEMENT_COLLECTION.getValue()));

			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.RECON_DATA_COLLECTION.getValue()));

			List<Row> results = new ArrayList<Row>();

			DataFormatter dataFormatter = new DataFormatter();
			Workbook workbook = WorkbookFactory.create(new FileInputStream(file.getAbsolutePath()));
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = workbook.getSheetAt(0);

			int totalRows = 0;
			int errorRows = 0;
			int successRows = 0;

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			for (Row row : sheet) { // iterate over all rows in the sheet except 1

				if (totalRows == 0) {
					totalRows++;
					continue;
				}

				results.add(row);
				totalRows++;
			}

			String settleDate = null;
			String payoutDate = null;
			String captureDate = null;
			String statementAmount = null;
			String settleDateFormat = null;
			String payoutDateFormat = null;
			String captureDateFormat = null;
			
			for (Row row : results) {

				Cell particularCell = row.getCell(0);

				String particularCellValue = dataFormatter.formatCellValue(particularCell, formulaEvaluator)
						.trim();

				if (StringUtils.isBlank(particularCellValue)) {
					continue;
				}

				if (StringUtils.isBlank(particularCellValue) || StringUtils.isBlank(particularCellValue)) {
					continue;
				}

				if (!particularCellValue.contains("IRCTC BOOKING") && !particularCellValue.contains("IRCTC REFUND")  ) {
					continue;
				}
				
				successRows++;

				SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yy");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				SimpleDateFormat sdf3 = new SimpleDateFormat("dd/MM/yyyy");
				
				// SALE
				if (particularCellValue.contains("IRCTC BOOKING")) {
					
					String statementSale = particularCellValue;
					statementSale = statementSale.replace("IRCTC BOOKING", "");
					
					String statementSaleSplit [] = statementSale.split(" ");
					List<String> statementSaleClean = new ArrayList<String>();
					
					for (String data : statementSaleSplit) {
						if(data.length() < 2) {
							continue;
						}
						statementSaleClean.add(data);
					}
					
					
					settleDate = statementSaleClean.get(0);
					payoutDate = statementSaleClean.get(1);
					captureDate = statementSaleClean.get(3);
					statementAmount = statementSaleClean.get(5);
					statementAmount = statementAmount.replace(",", "");
					
					settleDateFormat = sdf2.format(sdf1.parse(settleDate));
					payoutDateFormat = sdf2.format(sdf1.parse(payoutDate));
					captureDateFormat = sdf2.format(sdf3.parse(captureDate));
					
					Document doc = new Document();
					doc.put("CAPTURE_DATE", captureDateFormat);
					doc.put("BANK_TXN_NUMBER", particularCellValue);
					doc.put("AMOUNT", statementAmount);
					doc.put("FILE_NAME", fileFileName);
					doc.put("ACQUIRER", "KARUR BANK");
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("TXNTYPE", "SALE");
					doc.put("SETTLEMENT_DATE", settleDateFormat);
					doc.put("PAYOUT_DATE", payoutDateFormat);
					statementColl.insertOne(doc);
					
					
					// Mark all sale as settled
					
					BasicDBObject acqQuery = new BasicDBObject("ACQUIRER","KARUR BANK");
					BasicDBObject capDateQuery = new BasicDBObject("CREATE_DATE",captureDateFormat);
					BasicDBObject txnTypQuery = new BasicDBObject("TXNTYPE","SALE");
					
					
					List<BasicDBObject> saleConList = new ArrayList<BasicDBObject>();
					saleConList.add(acqQuery);
					saleConList.add(capDateQuery);
					saleConList.add(txnTypQuery);
					
					BasicDBObject txnUpdateQuery = new BasicDBObject("$and",saleConList);
					
					Document setData = new Document();
					setData.append("SETTLEMENT_FLAG", "Y");
					setData.append("SETTLEMENT_DATE", payoutDateFormat);
					setData.append("SETTLEMENT_DATE_INDEX", payoutDateFormat.substring(0, 10).replace("-", ""));

					Document update = new Document();
					update.append("$set", setData);
					coll.updateMany(txnUpdateQuery, update);
					
				}
				
				// REFUND
				if (particularCellValue.contains("IRCTC REFUND")) {
				
					String statementRefund = particularCellValue;
					statementRefund = statementRefund.replace("IRCTC REFUND", "");
					
					String statementRefundSplit [] = statementRefund.split(" ");
					List<String> statementRefundClean = new ArrayList<String>();
					
					for (String data : statementRefundSplit) {
						if(data.length() < 2) {
							continue;
						}
						
						statementRefundClean.add(data);
					}
					
					
					
					settleDate = statementRefundClean.get(0);
					payoutDate = statementRefundClean.get(1);
					captureDate = statementRefundClean.get(3);
					statementAmount = statementRefundClean.get(5);
					statementAmount = statementAmount.replace(",", "");
					
					settleDateFormat = sdf2.format(sdf1.parse(settleDate));
					payoutDateFormat = sdf2.format(sdf1.parse(payoutDate));
					captureDateFormat = sdf2.format(sdf3.parse(captureDate));
					
					Document doc = new Document();
					doc.put("CAPTURE_DATE", captureDateFormat);
					doc.put("BANK_TXN_NUMBER", particularCellValue);
					doc.put("AMOUNT", statementAmount);
					doc.put("FILE_NAME", fileFileName);
					doc.put("ACQUIRER", "KARUR BANK");
					doc.put("UPLOAD_DATE", dateNow);
					doc.put("TXNTYPE", "REFUND");
					doc.put("SETTLEMENT_DATE", settleDateFormat);
					doc.put("PAYOUT_DATE", payoutDateFormat);
					statementColl.insertOne(doc);
					
					
					// Mark all Refund as settled
					
					BasicDBObject acqQuery = new BasicDBObject("ACQUIRER","KARUR BANK");
					BasicDBObject capDateQuery = new BasicDBObject("CREATE_DATE",captureDateFormat);
					BasicDBObject txnTypQuery = new BasicDBObject("TXNTYPE","REFUND");
					
					
					List<BasicDBObject> saleConList = new ArrayList<BasicDBObject>();
					saleConList.add(acqQuery);
					saleConList.add(capDateQuery);
					saleConList.add(txnTypQuery);
					
					BasicDBObject txnUpdateQuery = new BasicDBObject("$and",saleConList);
					
					Document setData = new Document();
					setData.append("SETTLEMENT_FLAG", "Y");
					setData.append("SETTLEMENT_DATE", payoutDateFormat);
					setData.append("SETTLEMENT_DATE_INDEX", payoutDateFormat.substring(0, 10).replace("-", ""));

					Document update = new Document();
					update.append("$set", setData);
					coll.updateMany(txnUpdateQuery, update);
						
					}

			}
			
			Document query = new Document();
			query.append("FILE_NAME", fileFileName);
			Document setData = new Document();
			setData.append("STATUS", "Processed");
			setData.append("TOTAL_COUNT", String.valueOf(totalRows - 1));
			setData.append("ERROR_COUNT", String.valueOf(errorRows));
			setData.append("SUCCESS_COUNT", String.valueOf(successRows));
			Document update = new Document();
			update.append("$set", setData);
			fileColl.updateOne(query, update);
			
		} catch (Exception ex) {
			logger.error("Exception refundBookings ", ex);
		}
	}
}
