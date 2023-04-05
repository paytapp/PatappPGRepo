package com.paymentgateway.pg.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.ChargebackDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;

@Service
public class RefundUtilityDataService {

	private static Logger logger = LoggerFactory.getLogger(RefundUtilityDataService.class.getName());

	private static final String prefix = "MONGO_DB_";

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private ChargebackDao chargebackDao;
	
	
	public JSONObject createFileEntryWithStatus(String payId, String orderId, String amount, String status) {
		JSONObject json = new JSONObject();
		try {
			json.put("payId", payId);
			json.put("orderId", orderId);
			json.put("amount", amount);
			json.put("status", status);
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return json;
	}

	public void saveRefundFileDataStatusInDb(List<JSONObject> jsonObjList, List<Map<String, String>> invalidDataList,
			int totalFileEntry, int totalValidEntry, int totalInvalidEntry, int totalCapture, int totalRejected,
			int totalDeclined, int totalError, int totalDenied, int totalFailed, int totalInvalid,
			int totalAuthenticationFailed, int totalAcquirerDown, int totalFailedAtAcquirer, int totalAcquirerTimeOut,
			String userEmail, String refundType, String sessionUserString) {

		String fileName = createFile(jsonObjList, invalidDataList, refundType);
		if(refundType.equals(Constants.CHARGEBACK_REFUND.getValue())) {
			insertChargebackCloser(jsonObjList, sessionUserString);
		}

		insertInDb(totalFileEntry, totalValidEntry, totalInvalidEntry, totalCapture, totalRejected, totalDeclined,
				totalError, totalDenied, totalFailed, totalInvalid, totalAuthenticationFailed, totalAcquirerDown,
				totalFailedAtAcquirer, totalAcquirerTimeOut, fileName, userEmail, refundType);
	}

	public String createFile(List<JSONObject> jsonObjList, List<Map<String, String>> invalidDataList, String refundType) {

		String fileName = null;
		String fileLocation = null;
		try {
			if(StringUtils.isNotBlank(refundType) && refundType.equals(Constants.CHARGEBACK_REFUND.getValue())) {
				fileLocation = PropertiesManager.propertiesMap
						.get(Constants.REFUND_UTIL_FILE_LOCATION_URL.getValue()) + Constants.CHARGEBACK_CLOSER_FILE_FOLDER.getValue() + "/";
			} else {
				fileLocation = PropertiesManager.propertiesMap
						.get(Constants.REFUND_UTIL_FILE_LOCATION_URL.getValue()) + Constants.REFUND_TXN_FILE_FOLDER.getValue() + "/";
			}
			
			try {
				Files.createDirectories(Paths.get(fileLocation));
			} catch (IOException e1) {
				logger.error("Error in creating Directory ", e1);
			}
			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			Sheet sheet = wb.createSheet("RefundUtil Report");

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("pay Id");
			row.createCell(2).setCellValue("Order Id");
			row.createCell(3).setCellValue("Amount");
			row.createCell(4).setCellValue("Status");

			for (JSONObject jsonObject : jsonObjList) {
				String amount = Amount.toDecimal(jsonObject.getString("amount"), "356");
				row = sheet.createRow(rownum++);

				Object[] objectArray = new Object[5];
				objectArray[0] = rownum - 1;
				objectArray[1] = jsonObject.getString("payId");
				objectArray[2] = jsonObject.getString("orderId");
				objectArray[3] = amount;
				objectArray[4] = jsonObject.getString("status");

				int cellnum = 0;
				for (Object obj : objectArray) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);

				}
			}
			for (Map<String, String> jsonObject : invalidDataList) {
				row = sheet.createRow(rownum++);

				Object[] objectArray = new Object[5];
				objectArray[0] = rownum - 1;
				objectArray[1] = jsonObject.get(FieldType.PAY_ID.getName());
				objectArray[2] = jsonObject.get(FieldType.ORDER_ID.getName());
				objectArray[3] = jsonObject.get(FieldType.AMOUNT.getName());
				objectArray[4] = "Invalid Entry";

				int cellnum = 0;
				for (Object obj : objectArray) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);

				}
			}

			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			if(StringUtils.isNotBlank(refundType) && refundType.equals(Constants.CHARGEBACK_REFUND.getValue())) {
				fileName = "ChargebackRefundReport_" + df.format(new Date()) + FILE_EXTENSION;
			} else {
				fileName = "RefundUtilReport_" + df.format(new Date()) + FILE_EXTENSION;
			}
			
			File file = new File(fileLocation, fileName);
			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			logger.info("Files generated successfully for RefundUtils Report");
		} catch (Exception ex) {
			logger.error("Exception : ", ex);
		}
		return fileName;
	}

	public void insertInDb(int totalFileEntry, int totalValidEntryCount, int totalInvalidEntry, int totalCapture,
			int totalRejected, int totalDeclined, int totalError, int totalDenied, int totalFailed, int totalInvalid,
			int totalAuthenticationFailed, int totalAcquirerDown, int totalFailedAtAcquirer, int totalAcquirerTimeOut,
			String fileName, String userEmail, String refundType) {

		logger.info("inside insertInDb : ");
		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentDate = dateFormat.format(new Date());
			String dateIndex = DateCreater.changeDateString(currentDate);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.REFUND_UTIL_COLLECTION.getValue()));

			BasicDBObject newFieldsObj = new BasicDBObject();

			newFieldsObj.put(FieldType.TOTAL_FILE_ENTRY.getName(), totalFileEntry);
			newFieldsObj.put(FieldType.TOTAL_VALID_ENTRY.getName(), totalValidEntryCount);
			newFieldsObj.put(FieldType.TOTAL_INVALID_ENTRY.getName(), totalInvalidEntry);
			newFieldsObj.put(FieldType.TOTAL_CAPTURED.getName(), totalCapture);
			newFieldsObj.put(FieldType.TOTAL_REJECTED.getName(), totalRejected);
			newFieldsObj.put(FieldType.TOTAL_DECLINED.getName(), totalDeclined);
			newFieldsObj.put(FieldType.TOTAL_ERROR.getName(), totalError);
			newFieldsObj.put(FieldType.TOTAL_DENIED.getName(), totalDenied);
			newFieldsObj.put(FieldType.TOTAL_FAILED.getName(), totalFailed);
			newFieldsObj.put(FieldType.TOTAL_INVALID.getName(), totalInvalid);
			newFieldsObj.put(FieldType.TOTAL_AUTHENTICATION_FAILED.getName(), totalAuthenticationFailed);
			newFieldsObj.put(FieldType.TOTAL_ACQUIRER_DOWN.getName(), totalAcquirerDown);
			newFieldsObj.put(FieldType.TOTAL_FAILED_AT_ACQUIRER.getName(), totalFailedAtAcquirer);
			newFieldsObj.put(FieldType.TOTAL_ACQUIRER_TIMEOUT.getName(), totalAcquirerTimeOut);
			newFieldsObj.put(FieldType.CREATE_DATE.getName(), currentDate);
			newFieldsObj.put(FieldType.DATE_INDEX.getName(), dateIndex);
			newFieldsObj.put(FieldType.FILENAME.getName(), fileName);
			newFieldsObj.put(FieldType.CREATED_BY.getName(), userEmail);
			newFieldsObj.put(FieldType.REFUND_TXN_TYPE.getName(), refundType);
			
			Document doc = new Document(newFieldsObj);

			coll.insertOne(doc);

		} catch (Exception ex) {
			logger.error("Exception while inserting data in Util Collection : ", ex);
		}
	}
	
	public void insertChargebackCloser(List<JSONObject> jsonObjValidList, String sessionUserString) {
		try {
			for (JSONObject json : jsonObjValidList) {
				try {
					if (json.getString("status").equalsIgnoreCase(StatusType.CAPTURED.getName())) {
						Date dNow = new Date();
						String closeDateString = null;
						Date closedDate = null;
						
						Chargeback chargeback = chargebackDao.findChargebackLastStatusByOrderId(json.getString("payId"),
								json.getString("orderId"));

						switch (sessionUserString) {
						case "ADMIN":
							if (chargeback.getStatus().equalsIgnoreCase("Open")) {
								chargeback.setId(TransactionManager.getNewTransactionId());
								chargeback.setChargebackStatus("Accepted by admin");
								chargeback.setStatus("Accepted");
								chargeback.setUpdateDate(DateCreater.defaultCurrentDateTimeType(dNow));
								chargebackDao.create(chargeback);
							}

							chargeback.setId(TransactionManager.getNewTransactionId());
							chargeback.setChargebackStatus("Refunded by admin");
							chargeback.setStatus("Refunded");
							chargeback.setUpdateDate(DateCreater.defaultCurrentDateTimeType(new Date(dNow.getTime() + 10 * 1000)));
							chargebackDao.create(chargeback);

							closedDate = DateCreater.defaultCurrentDateTimeType(new Date(dNow.getTime() + 20 * 1000));
							closeDateString = DateCreater.formatDateForDb(closedDate);
							
							chargeback.setId(TransactionManager.getNewTransactionId());
							chargeback.setChargebackStatus("Closed by admin");
							chargeback.setStatus("Closed");
							chargeback.setCloseDate(closeDateString);
							chargeback.setUpdateDate(closedDate);
							chargebackDao.create(chargeback);
							break;
							
						case "SUBADMIN":
							if (chargeback.getStatus().equalsIgnoreCase("Open")) {
								chargeback.setId(TransactionManager.getNewTransactionId());
								chargeback.setChargebackStatus("Accepted by subadmin");
								chargeback.setStatus("Accepted");
								chargeback.setUpdateDate(DateCreater.defaultCurrentDateTimeType(dNow));
								chargebackDao.create(chargeback);
							}

							chargeback.setId(TransactionManager.getNewTransactionId());
							chargeback.setChargebackStatus("Refunded by subadmin");
							chargeback.setStatus("Refunded");
							chargeback.setUpdateDate(DateCreater.defaultCurrentDateTimeType(new Date(dNow.getTime() + 10 * 1000)));
							chargebackDao.create(chargeback);

							closedDate = DateCreater.defaultCurrentDateTimeType(new Date(dNow.getTime() + 20 * 1000));
							closeDateString = DateCreater.formatDateForDb(closedDate);
							
							chargeback.setId(TransactionManager.getNewTransactionId());
							chargeback.setChargebackStatus("Closed by subadmin");
							chargeback.setStatus("Closed");
							chargeback.setCloseDate(closeDateString);
							chargeback.setUpdateDate(closedDate);
							chargebackDao.create(chargeback);
							break;
						}
					}
				} catch (Exception exception) {
					logger.error("Exception while Updating chargeback status for order id - "
							+ json.getString("orderId") + " : ", exception);
				}
			}
		} catch (Exception ex) {
			logger.error("Exception in insertChargebackCloser() : ", ex);
		}

	}
}
