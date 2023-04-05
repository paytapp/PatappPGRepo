package com.paymentgateway.crm.mongoReports;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Chandan
 *
 */

@Service
public class RefundApproveData {

	private static Logger logger = LoggerFactory.getLogger(RefundApproveData.class.getName());
	
	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private FieldsDao fieldsDao;
	
	@Autowired
	private UserDao userdao;
	
	private static final String prefix = "MONGO_DB_";
	
	public boolean isRecoExistFunc(String merchantPayId, String fromDate) throws ParseException {
		logger.info("Inside Refund Approve Data Class, In isRecoExistFunc Method !!");
		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject allParamQuery = new BasicDBObject();

			if (!fromDate.isEmpty()) {

				String currentDate = null;
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				 // add days to from date				
				Date date1= dateFormat.parse(fromDate);	
				cal.setTime(date1);
				cal.add(Calendar.DATE, 1);
				currentDate = dateFormat.format(cal.getTime());			

				dateQuery.put(FieldType.PG_DATE_TIME.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lt", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}			
					
			List<BasicDBObject> reconciledList = new ArrayList<BasicDBObject>();
			reconciledList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			reconciledList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.RECONCILED.getName()));
			BasicDBObject reconciledQuery = new BasicDBObject("$and", reconciledList);

			List<BasicDBObject> transList = new ArrayList<BasicDBObject>();
			transList.add(reconciledQuery);
			//transList.add(settledQuery);
			BasicDBObject transQuery = new BasicDBObject("$or", transList);
			
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			
			if (!transQuery.isEmpty()) {
				allConditionQueryList.add(transQuery);
			}
			
			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			logger.info("Final Query :" + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll =
			dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			int rowCount = (int)coll.count(finalquery);
			logger.info("Total No. Of Records : " + rowCount);
			if(rowCount > 0) {
				return true;
			}
			
		} catch (Exception exception) {
			logger.error("Exception in RefundPreviewData Get Data : ", exception);
		}
		return false;
	}
	
	public void getData(String merchantPayId, String fromDate) {
		logger.info("Inside Refund Approve Data Class, In getData Method !!");
		
		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject allParamQuery = new BasicDBObject();

			if (!fromDate.isEmpty()) {

				String currentDate = null;
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				 // add days to from date				
				Date date1= dateFormat.parse(fromDate);	
				cal.setTime(date1);
				cal.add(Calendar.DATE, 1);
				currentDate = dateFormat.format(cal.getTime());			

				dateQuery.put(FieldType.PG_DATE_TIME.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lt", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}
			
			List<BasicDBObject> settledList = new ArrayList<BasicDBObject>();
			settledList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			settledList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			BasicDBObject settledQuery = new BasicDBObject("$and", settledList);
			
			List<BasicDBObject> reconciledList = new ArrayList<BasicDBObject>();
			reconciledList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			reconciledList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.RECONCILED.getName()));
			BasicDBObject reconciledQuery = new BasicDBObject("$and", reconciledList);

			List<BasicDBObject> transList = new ArrayList<BasicDBObject>();
			transList.add(reconciledQuery);
			transList.add(settledQuery);
			BasicDBObject transQuery = new BasicDBObject("$or", transList);
			
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			
			if (!transQuery.isEmpty()) {
				allConditionQueryList.add(transQuery);
			}
			
			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			logger.info("Final Query :" + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll =
			dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = coll.find(finalquery).iterator();
			logger.info("Total No. Of Records :" + coll.count(finalquery));
			ArrayList<String> settledArrList = new ArrayList<String>();
			HashMap<String, Document> settledMap = new HashMap<String,Document>();
			HashMap<String, Document> reconciledMap = new HashMap<String,Document>();
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				if(doc.getString(FieldType.TXNTYPE.toString()).equals(TransactionType.RECO.getName()) && doc.getString(FieldType.STATUS.toString()).equals(StatusType.SETTLED.getName())) {
					settledArrList.add(doc.getString(FieldType.PG_REF_NUM.toString()));
					settledMap.put(doc.getString(FieldType.PG_REF_NUM.toString()), doc);
				} else if(doc.getString(FieldType.TXNTYPE.toString()).equals(TransactionType.RECO.getName()) && doc.getString(FieldType.STATUS.toString()).equals(StatusType.RECONCILED.getName())) {
					reconciledMap.put(doc.getString(FieldType.PG_REF_NUM.toString()), doc);
				}		
			}
			logger.info("No. Of Records settledArrList :" + settledArrList.size());
			logger.info("No. Of Records settledMap :" + settledMap.size());
			logger.info("No. Of Records reconciledMap :" + reconciledMap.size());
			cursor.close();
			
			processDeltaRefundRecords(settledArrList,  settledMap, reconciledMap, merchantPayId);
			
		} catch (Exception exception) {
			logger.error("Exception in RefundPreviewData Get Data : ", exception);
		}
	}	
	
	private void processDeltaRefundRecords(ArrayList<String> settledArrList, HashMap<String,Document> settledMap, HashMap<String,Document> reconciledMap, String merchantPayId) {
		logger.info("Inside Refund Approve Data Class, getDeltaRefundRecords Method !!");
		StringBuilder strBuilder = new StringBuilder();
		String seperator = "|";
		Document reconciledDoc = new Document();
		Document settledDoc = new Document();
		try {				
			for (String pgRefNum : settledArrList) {
				logger.info("Settled PG_REF_NUM :" + pgRefNum);					
				reconciledDoc =  reconciledMap.get(pgRefNum);
				if(reconciledDoc != null) {
					logger.info("\nreconciledDoc fields :" + reconciledDoc.toJson());
				} else {
					logger.info("No. Of Records reconciledDoc : 0");
				}
				if(reconciledDoc ==  null) {
					settledDoc =  settledMap.get(pgRefNum);
					logger.info("\nGetting Settle records settledMap from for PG_REF_NUM : " + pgRefNum);
					if(insertRecoTxn(settledDoc)) {
						strBuilder.append(settledDoc.getString(FieldType.ORDER_ID.toString()));
						strBuilder.append(seperator);
						strBuilder.append(CrmFieldConstants.REFUND_FLAG.getValue());
						strBuilder.append(seperator);
						strBuilder.append(Amount.removeDecimalAmount(settledDoc.getString(FieldType.AMOUNT.toString()), settledDoc.getString(FieldType.CURRENCY_CODE.toString())));
						/*strBuilder.append(doc.getString(FieldType.AMOUNT.toString()));*/
						strBuilder.append(seperator);
						strBuilder.append(settledDoc.getString(FieldType.PG_REF_NUM.toString()));
						strBuilder.append(seperator);
						strBuilder.append(DateCreater.formatDateReco(settledDoc.getString(FieldType.PG_DATE_TIME.toString())).toString());
						strBuilder.append(seperator);
						strBuilder.append(Amount.removeDecimalAmount(settledDoc.getString(FieldType.AMOUNT.toString()),settledDoc.getString(FieldType.CURRENCY_CODE.toString())));
						strBuilder.append(seperator);
						strBuilder.append(TransactionManager.getNewTransactionId());
						strBuilder.append(seperator);
						strBuilder.append(Constants.Y_FLAG.getValue());
						strBuilder.append("\r\n");
						logger.info("Inside Batch Generator : " + strBuilder.toString());
					}
				}
				reconciledDoc = null;
				settledDoc = null;
			}
		
			createBathFile(strBuilder.toString(), merchantPayId);
			
		}  catch (Exception exception) {
			logger.error("Exception in RefundPreviewData Class, processDeltaRefundRecords method : ", exception);
		}
	}
	
	private void createBathFile(String fileData, String merchantPayId) {
		logger.info("Inside Refund Approve Data Class, In createBathFile Method !!");
		DateFormat df = new SimpleDateFormat("ddMMyyyy");
		try {
			logger.info("Inside Batch Creation Start : " + fileData);
			String FILE_EXTENSION = ".txt";
			String deltaRefundUploadPath = PropertiesManager.propertiesMap.get("RecoUploadPath");
			
			//InputStream fileInputStream;
			String filename; 
			String merchantName = "";
			merchantName = userdao.getBusinessNameByPayId(merchantPayId);
			/*filename = "deltarefundletz_HDF_CC_" + df.format(new Date()) + "V1_" + RandomStringUtils.randomAlphanumeric(32).toLowerCase() + FILE_EXTENSION;*/
			filename = "DELTAREFUND_" + merchantName.replace(" ", "") +"_" + df.format(new Date()) + "_V1_" + RandomStringUtils.randomAlphanumeric(32).toLowerCase() + FILE_EXTENSION;
			File file = new File(deltaRefundUploadPath + filename);
			file.createNewFile();  
			
			// Clear all permissions for all users
			file.setReadable(false, false);
	        file.setWritable(false, false);
	        file.setExecutable(false, false);
	        
	        file.setReadable(true, false);
	        file.setWritable(true, false);
	        file.setExecutable(true, false);
	        
			//Write Content
			FileWriter writer = new FileWriter(file);
			writer.write(fileData);
			writer.close();			
			
			//fileInputStream = new FileInputStream(file);
			logger.info("Inside Batch Creation End : " + fileData);			
		} catch (Exception exception) {
			logger.error("Refund Approve Data Class, In createBathFile Method  :", exception);
		}
	}
	
	private boolean insertRecoTxn(Document doc) {
		logger.info("Inside Refund Approve Data Class, In insertRecoTxn Method !!");
		try {
			Fields fields =new Fields();
			fields =  fieldsDao.getFields(doc.getString(FieldType.TXN_ID.toString()), doc.getString(FieldType.PAY_ID.toString()));
			fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId().toString());
			fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(doc.getString(FieldType.AMOUNT.toString()), doc.getString(FieldType.CURRENCY_CODE.toString())));
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName());
			fields.put(FieldType.STATUS.getName(), StatusType.RECONCILED.getName());	
			fields.put(FieldType.TOTAL_AMOUNT.getName(), Amount.formatAmount(doc.getString(FieldType.TOTAL_AMOUNT.toString()), doc.getString(FieldType.CURRENCY_CODE.toString())));
			if(doc.get(FieldType.TXNTYPE.getName()).equals(TransactionType.RECO.getName()) && 
					doc.get(FieldType.STATUS.getName()).equals(StatusType.SETTLED.getName())) {
				fields.put(FieldType.PG_DATE_TIME.getName(), doc.get(FieldType.PG_DATE_TIME.getName()).toString());
			} else if(doc.get(FieldType.TXNTYPE.getName()).equals(TransactionType.SALE.getName()) && 
					doc.get(FieldType.STATUS.getName()).equals(StatusType.CAPTURED.getName())) {
				fields.put(FieldType.PG_DATE_TIME.getName(), doc.get(FieldType.CREATE_DATE.getName()).toString());
			} else {
				fields.put(FieldType.PG_DATE_TIME.getName(), fieldsDao.getSaleDate(doc.get(FieldType.PG_REF_NUM.getName()).toString()));
			}
			fields.put(FieldType.RESPONSE_CODE.getName(), Constants.SUCCESS_CODE.getValue());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "SUCCESS");
			fields.put(FieldType.UDF6.getName(), "Y");
			fieldsDao.insertTransaction(fields);
			return true;
		} catch (Exception exception) {
			logger.error("Refund Approve Data Class, In insertRecoTxn Method : ", exception);
			return false;
		}		
	}
}
