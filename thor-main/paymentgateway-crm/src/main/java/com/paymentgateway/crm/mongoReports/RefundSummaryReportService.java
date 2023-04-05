package com.paymentgateway.crm.mongoReports;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.RefundSummary;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Vijaya
 *
 */

@Service
public class RefundSummaryReportService {

	private static Logger logger = LoggerFactory.getLogger(RefundSummaryReportService.class.getName());

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private UserDao userdao;
	private static final String prefix = "MONGO_DB_";

	public List<RefundSummary> getData(String merchantPayId,String subMerchantPayId, String refundRequestDate, String acquirer,
			String paymentType, String mode, String currency) {
		logger.info("Inside RefundSummaryReportService Class, in getData Method !!");
		List<RefundSummary> lstRefundSummary = new ArrayList<RefundSummary>();
		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject allParamQuery = new BasicDBObject();

			if (!refundRequestDate.isEmpty()) {
				String currentDate = null;

				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				// add days to from date
				Date date1 = dateFormat.parse(refundRequestDate);
				cal.setTime(date1);
				cal.add(Calendar.DATE, 1);
				currentDate = dateFormat.format(cal.getTime());

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(refundRequestDate).toLocalizedPattern())
								.add("$lt", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}
			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			if (!acquirer.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acquirer));
			}
			if (!paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}
			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}
			
			List<BasicDBObject> refundSummaryConditionList = new ArrayList<BasicDBObject>();
			refundSummaryConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));

			ArrayList<String> statusList = new ArrayList<>();
			statusList.add(StatusType.CAPTURED.getName());
			statusList.add(StatusType.REJECTED.getName());
			statusList.add(StatusType.DECLINED.getName());
			statusList.add(StatusType.PENDING.getName());
			statusList.add(StatusType.ERROR.getName());
			statusList.add(StatusType.TIMEOUT.getName());
			statusList.add(StatusType.FAILED.getName());
			statusList.add(StatusType.INVALID.getName());
			statusList.add(StatusType.ACQUIRER_DOWN.getName());
			statusList.add(StatusType.FAILED_AT_ACQUIRER.getName());
			statusList.add(StatusType.ACQUIRER_TIMEOUT.getName());
			refundSummaryConditionList
					.add(new BasicDBObject(FieldType.STATUS.getName(), new BasicDBObject("$in", statusList)));

			BasicDBObject refundSummaryQuery = new BasicDBObject("$and", refundSummaryConditionList);

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}

			if (!refundSummaryQuery.isEmpty()) {
				allConditionQueryList.add(refundSummaryQuery);
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

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);

			logger.info("RefundSummaryReportService final Query : " + finalquery);
			logger.info("No. of Records : " + coll.count(finalquery));
			// Now the aggregate operation
			Document firstGroup = new Document("_id", new Document("REFUND_ORDER_ID", "$REFUND_ORDER_ID"));

			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);

			MongoCursor<Document> cursor = output.iterator();

			logger.info("RefundSummaryReportService final Query : " + finalquery);
			logger.info("No. of Records : " + coll.count(finalquery));
			List<Document> lstDocuments = new ArrayList<Document>();
			Boolean capturedFlag = false;
			String[] arrAcquirer, arrPaymentType, arrMopType;
			StringBuilder sbAcquirer = new StringBuilder();
			StringBuilder sbPaymentType = new StringBuilder();
			StringBuilder sbMopType = new StringBuilder();
			
			int cntInitiated = 0, cntCaptured = 0, cntRejected = 0, cntDeclined = 0, cntPending = 0, cntError = 0,
					cntTimeout = 0, cntFailed = 0, cntInvalid = 0, cntAcqDown = 0, cntFailedAtAcq = 0,
					cntAcqTimeout = 0;

			BigDecimal amtInitiated, amtCaptured, amtRejected, amtDeclined, amtPending, amtError, amtTimeout, amtFailed,
					amtInvalid, amtAcqDown, amtFailedAtAcq, amtAcqTimeout;

			while (cursor.hasNext()) {
				cntInitiated++;
				Document dbobj = cursor.next();
				List<Document> lstDoc = (List<Document>) dbobj.get("entries");
				capturedFlag = false;
				for (int i = 0; i < lstDoc.size(); i++) {
					if (lstDoc.get(i).getString(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUND.getName())) {   
						lstDocuments.add(lstDoc.get(i));
						if (!sbAcquirer.toString()
								.contains(lstDoc.get(i).getString(FieldType.ACQUIRER_TYPE.getName()))) {
							sbAcquirer.append(lstDoc.get(i).getString(FieldType.ACQUIRER_TYPE.getName()));
							sbAcquirer.append(",");
						}
						if (!sbPaymentType.toString()
								.contains(lstDoc.get(i).getString(FieldType.PAYMENT_TYPE.getName()))) {
							sbPaymentType.append(lstDoc.get(i).getString(FieldType.PAYMENT_TYPE.getName()));
							sbPaymentType.append(",");
						}
						if (!sbMopType.toString().contains(lstDoc.get(i).getString(FieldType.MOP_TYPE.getName()))) {
							sbMopType.append(lstDoc.get(i).getString(FieldType.MOP_TYPE.getName()));
							sbMopType.append(",");
						}
						capturedFlag = true;
					
					}
					if (!capturedFlag) {
						lstDocuments.add(lstDoc.get(0));
						if (!sbAcquirer.toString().contains(lstDoc.get(0).getString(FieldType.ACQUIRER_TYPE.getName()))) {
							sbAcquirer.append(lstDoc.get(0).getString(FieldType.ACQUIRER_TYPE.getName()));
							sbAcquirer.append(",");
						}
						if (!sbPaymentType.toString().contains(lstDoc.get(0).getString(FieldType.PAYMENT_TYPE.getName()))) {
							sbPaymentType.append(lstDoc.get(0).getString(FieldType.PAYMENT_TYPE.getName()));
							sbPaymentType.append(",");
						}
						if (!sbMopType.toString().contains(lstDoc.get(0).getString(FieldType.MOP_TYPE.getName()))) {
							sbMopType.append(lstDoc.get(0).getString(FieldType.MOP_TYPE.getName()));
							sbMopType.append(",");
						}
					}
				}

				
			}
			cursor.close();

			arrAcquirer = sbAcquirer.toString().split(",");
			arrPaymentType = sbPaymentType.toString().split(",");
			arrMopType = sbMopType.toString().split(",");

			

			for (String acqName : arrAcquirer) {

				for (String paymType : arrPaymentType) {

					for (String mop : arrMopType) {

						RefundSummary refundSummary = new RefundSummary();

						refundSummary.setAcquirer(acqName);
						refundSummary.setPaymentType(paymType);
						refundSummary.setMop(mop);
						refundSummary.setTxnInitiate("0");
						refundSummary.setCaptured("0");
						refundSummary.setRejected("0");
						refundSummary.setDeclined("0");
						refundSummary.setPending("0");
						refundSummary.setError("0");
						refundSummary.setTimeout("0");
						refundSummary.setFailed("0");
						refundSummary.setInvalid("0");
						refundSummary.setAcqDown("0");
						refundSummary.setFailedAtAcq("0");
						refundSummary.setAcqTimeout("0");

						lstRefundSummary.add(refundSummary);

					}
				}
			}
			if (mode.equalsIgnoreCase("count")) {
				for (Document doc : lstDocuments) {
					for (RefundSummary refSummary : lstRefundSummary) {
						if (doc.getString(FieldType.ACQUIRER_TYPE.getName()).equals(refSummary.getAcquirer())
								&& doc.getString(FieldType.PAYMENT_TYPE.getName()).equals(refSummary.getPaymentType())
								&& doc.getString(FieldType.MOP_TYPE.getName()).equals(refSummary.getMop())) {
							cntInitiated = Integer.parseInt(refSummary.getTxnInitiate());
							cntInitiated++;
							refSummary.setTxnInitiate(String.valueOf(cntInitiated));
							if (doc.getString(FieldType.STATUS.toString()).equals(StatusType.CAPTURED.getName())) {
								cntCaptured = Integer.parseInt(refSummary.getCaptured());
								cntCaptured++;
								refSummary.setCaptured(String.valueOf(cntCaptured));
							} else if (doc.getString(FieldType.STATUS.toString())
									.equals(StatusType.REJECTED.getName())) {
								cntRejected = Integer.parseInt(refSummary.getRejected());
								cntRejected++;
								refSummary.setRejected(String.valueOf(cntRejected));
							} else if (doc.getString(FieldType.STATUS.toString())
									.equals(StatusType.DECLINED.getName())) {
								cntDeclined = Integer.parseInt(refSummary.getDeclined());
								cntDeclined++;
								refSummary.setDeclined(String.valueOf(cntDeclined));
							} else if (doc.getString(FieldType.STATUS.toString())
									.equals(StatusType.PENDING.getName())) {
								cntPending = Integer.parseInt(refSummary.getPending());
								cntPending++;
								refSummary.setPending(String.valueOf(cntPending));

							} else if (doc.getString(FieldType.STATUS.toString()).equals(StatusType.ERROR.getName())) {
								cntError = Integer.parseInt(refSummary.getError());
								cntError++;
								refSummary.setError(String.valueOf(cntError));
							} else if (doc.getString(FieldType.TXNTYPE.toString())
									.equals(TransactionType.REFUND.getName())
									&& doc.getString(FieldType.STATUS.toString())
											.equals(StatusType.TIMEOUT.getName())) {
								cntTimeout = Integer.parseInt(refSummary.getTimeout());
								cntTimeout++;
								refSummary.setTimeout(String.valueOf(cntTimeout));
							} else if (doc.getString(FieldType.STATUS.toString()).equals(StatusType.FAILED.getName())) {
								cntFailed = Integer.parseInt(refSummary.getFailed());
								cntFailed++;
								refSummary.setFailed(String.valueOf(cntFailed));
							} else if (doc.getString(FieldType.STATUS.toString())
									.equals(StatusType.INVALID.getName())) {
								cntInvalid = Integer.parseInt(refSummary.getInvalid());
								cntInvalid++;
								refSummary.setInvalid(String.valueOf(cntInvalid));
							} else if (doc.getString(FieldType.STATUS.toString())
									.equals(StatusType.ACQUIRER_DOWN.getName())) {
								cntAcqDown = Integer.parseInt(refSummary.getAcqDown());
								cntAcqDown++;
								refSummary.setAcqDown(String.valueOf(cntAcqDown));
							} else if (doc.getString(FieldType.STATUS.toString())
									.equals(StatusType.FAILED_AT_ACQUIRER.getName())) {
								cntFailedAtAcq = Integer.parseInt(refSummary.getFailedAtAcq());
								cntFailedAtAcq++;
								refSummary.setFailedAtAcq(String.valueOf(cntFailedAtAcq));
							} else if (doc.getString(FieldType.STATUS.toString())
									.equals(StatusType.ACQUIRER_TIMEOUT.getName())) {
								cntAcqTimeout = Integer.parseInt(refSummary.getAcqTimeout());
								cntAcqTimeout++;
								refSummary.setAcqTimeout(String.valueOf(cntAcqTimeout));
							}
						}
					}
				}

			} else if (mode.equalsIgnoreCase("amount")) {
				for (Document doc : lstDocuments) {
					for (RefundSummary refSummary : lstRefundSummary) {
						if (doc.getString(FieldType.ACQUIRER_TYPE.getName()).equals(refSummary.getAcquirer())
								&& doc.getString(FieldType.PAYMENT_TYPE.getName()).equals(refSummary.getPaymentType())
								&& doc.getString(FieldType.MOP_TYPE.getName()).equals(refSummary.getMop())) {
							amtInitiated = new BigDecimal(refSummary.getTxnInitiate());
							amtInitiated = amtInitiated.add(new BigDecimal((doc.getString(FieldType.TOTAL_AMOUNT.getName()))));
							refSummary.setTxnInitiate(String.valueOf(amtInitiated));
							if (doc.getString(FieldType.STATUS.toString()).equals(StatusType.CAPTURED.getName())) {
								amtCaptured = new BigDecimal(refSummary.getCaptured());
								amtCaptured = amtCaptured.add(new BigDecimal(doc.getString(FieldType.TOTAL_AMOUNT.getName())));
								refSummary.setCaptured(String.valueOf(amtCaptured));
							} else if (doc.getString(FieldType.STATUS.toString())
									.equals(StatusType.REJECTED.getName())) {
								amtRejected =  new BigDecimal(refSummary.getRejected());
								amtRejected = amtRejected.add(new BigDecimal(doc.getString(FieldType.TOTAL_AMOUNT.getName())));
								refSummary.setRejected(String.valueOf(amtRejected));
							} else if (doc.getString(FieldType.STATUS.toString())
									.equals(StatusType.DECLINED.getName())) {
								amtDeclined = new BigDecimal(refSummary.getDeclined());
								amtDeclined=  amtDeclined.add(new BigDecimal(doc.getString(FieldType.TOTAL_AMOUNT.getName())));
								refSummary.setDeclined(String.valueOf(amtDeclined));
							} else if (doc.getString(FieldType.STATUS.toString())
									.equals(StatusType.PENDING.getName())) {
								amtPending = new BigDecimal(refSummary.getPending());
								amtPending= amtPending.add(new BigDecimal(doc.getString(FieldType.TOTAL_AMOUNT.getName())));
								refSummary.setPending(String.valueOf(amtPending));

							} else if (doc.getString(FieldType.STATUS.toString()).equals(StatusType.ERROR.getName())) {
								amtError = new BigDecimal(refSummary.getError());
								amtError= amtError.add(new BigDecimal(doc.getString(FieldType.TOTAL_AMOUNT.getName())));
								refSummary.setError(String.valueOf(amtError));
							} else if (doc.getString(FieldType.TXNTYPE.toString())
									.equals(TransactionType.REFUND.getName())
									&& doc.getString(FieldType.STATUS.toString())
											.equals(StatusType.TIMEOUT.getName())) {
								amtTimeout =  new BigDecimal(refSummary.getTimeout());
								amtTimeout= amtTimeout.add(new BigDecimal(doc.getString(FieldType.TOTAL_AMOUNT.getName())));
								refSummary.setTimeout(String.valueOf(amtTimeout));
							} else if (doc.getString(FieldType.STATUS.toString()).equals(StatusType.FAILED.getName())) {
								amtFailed =  new BigDecimal(refSummary.getFailed());
								amtFailed = amtFailed.add(new BigDecimal(doc.getString(FieldType.TOTAL_AMOUNT.getName())));
								refSummary.setFailed(String.valueOf(amtFailed));
							} else if (doc.getString(FieldType.STATUS.toString())
									.equals(StatusType.INVALID.getName())) {
								amtInvalid = new BigDecimal(refSummary.getInvalid());
								amtInvalid= amtInvalid.add(new BigDecimal(doc.getString(FieldType.TOTAL_AMOUNT.getName())));
								refSummary.setInvalid(String.valueOf(amtInvalid));
							} else if (doc.getString(FieldType.STATUS.toString())
									.equals(StatusType.ACQUIRER_DOWN.getName())) {
								amtAcqDown = new BigDecimal(refSummary.getAcqDown());
								amtAcqDown= amtAcqDown.add(new BigDecimal(doc.getString(FieldType.TOTAL_AMOUNT.getName())));
								refSummary.setAcqDown(String.valueOf(amtAcqDown));
							} else if (doc.getString(FieldType.STATUS.toString())
									.equals(StatusType.FAILED_AT_ACQUIRER.getName())) {
								amtFailedAtAcq = new BigDecimal(refSummary.getFailedAtAcq());
								amtFailedAtAcq = amtFailedAtAcq.add(new BigDecimal(doc.getString(FieldType.TOTAL_AMOUNT.getName())));
								refSummary.setFailedAtAcq(String.valueOf(amtFailedAtAcq));
							} else if (doc.getString(FieldType.STATUS.toString())
									.equals(StatusType.ACQUIRER_TIMEOUT.getName())) {
								amtAcqTimeout = new BigDecimal(refSummary.getAcqTimeout());
								amtAcqTimeout=  amtAcqTimeout.add(new BigDecimal(doc.getString(FieldType.TOTAL_AMOUNT.getName())));
								refSummary.setAcqTimeout(String.valueOf(amtAcqTimeout));
							}
						}
					}
					
				}
			}
			for(int i = lstRefundSummary.size() -1 ; i>=0; i--) {
				if(lstRefundSummary.get(i).getTxnInitiate().equals("0")) {
					lstRefundSummary.remove(i);
				}
			}
		} catch (Exception exception) {
			logger.error("Inside RefundSummaryReportService Class, in getCountData method  : ", exception);
		}

		return lstRefundSummary;

	}

}
