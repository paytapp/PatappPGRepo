package com.paymentgateway.crm.mongoReports;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.RefundPreview;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Chandan
 *
 */

@Service
public class RefundPreviewData {
	
	private static Logger logger = LoggerFactory.getLogger(RefundPreviewData.class.getName());
	
	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;
	
	private static final String prefix = "MONGO_DB_";

	public List<RefundPreview> getData(String merchantPayId, String fromDate)  {
		logger.info("Inside Refund Preview Data Class, getData Method !!");
		List<RefundPreview> refundPreviewList = new ArrayList<RefundPreview>();
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

			//BasicDBObject autoRefundQuery = new BasicDBObject("$and", refundPreviewConditionList);
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
			
			refundPreviewList = getDeltaRefundRecords(settledArrList,  settledMap, reconciledMap);
			logger.info("No. Of Records refundPreviewList (Delta Refund) :" + refundPreviewList.size());
			
		} catch (Exception exception) {
			logger.error("Exception in RefundPreviewData Get Data : ", exception);
		}
		
		return refundPreviewList;
	}	
	
	private List<RefundPreview> getDeltaRefundRecords(ArrayList<String> settledArrList, HashMap<String,Document> settledMap, HashMap<String,Document> reconciledMap) {
		logger.info("Inside Refund Preview Data Class, getDeltaRefundRecords Method !!");
		List<RefundPreview> refundPreviewList = new ArrayList<RefundPreview>();
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
				if(reconciledDoc == null) {
					settledDoc =  settledMap.get(pgRefNum);
					logger.info("\nGetting Settle records settledMap from for PG_REF_NUM : " + pgRefNum);
					RefundPreview refundPreviewReport = new RefundPreview();
	
					refundPreviewReport.setPgRefNo(settledDoc.getString(FieldType.PG_REF_NUM.toString()));
					refundPreviewReport.setRefundFlag(CrmFieldConstants.REFUND_FLAG.getValue());
					refundPreviewReport.setAmount(settledDoc.getString(FieldType.AMOUNT.toString()));
					refundPreviewReport.setOrderId(settledDoc.getString(FieldType.ORDER_ID.toString()));
					refundPreviewReport.setPayId(settledDoc.getString(FieldType.PAY_ID.toString()));
					refundPreviewReport.setSaleDate(settledDoc.getString(FieldType.PG_DATE_TIME.toString()));
					refundPreviewReport.setSettledDate(settledDoc.getString(FieldType.CREATE_DATE.toString()));
					refundPreviewReport.setOid(settledDoc.getString(FieldType.OID.toString()));
	
					refundPreviewList.add(refundPreviewReport);
					logger.info("No. Of Records refundPreviewList in getDeltaRefundRecords method :" + refundPreviewList.size());
				}
				reconciledDoc = null;
				settledDoc = null;
			}
		} catch (Exception exception) {
			logger.error("Exception in RefundPreviewData Class, getDeltaRefundRecords Method : ", exception);
		}
		return refundPreviewList;
	}
}
