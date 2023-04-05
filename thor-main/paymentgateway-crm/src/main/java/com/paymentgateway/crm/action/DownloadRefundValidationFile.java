package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.RefundValidation;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Chandan
 *
 */

public class DownloadRefundValidationFile extends AbstractSecureAction {
	
	private static final long serialVersionUID = -1471532495305993899L;
	private static Logger logger = LoggerFactory.getLogger(DownloadRefundValidationFile.class.getName());
	
	@Autowired
	private MongoInstance mongoInstance;
	
	
	@Autowired
	PropertiesManager propertiesManager;
	private static final String alphabaticFileName= "alphabatic-currencycode.properties";
	
	private List<Merchants> merchantList = new LinkedList<Merchants>();
	private Map<String, String> currencyMap = new HashMap<String, String>();
	private String validationCurrency;
	private String validationDateFrom;
	private String validationDateTo;
	private String validationMerchant;
	private String refundValidationButtonIdentifier;
	private InputStream fileInputStream;
	private String filename;
	
	@Autowired
	private UserDao userdao;
	
	private static final String prefix = "MONGO_DB_";
	
	//@SuppressWarnings("resource")
	public String execute() {
		logger.info("Inside DownloadRefundValidationFile Class !!"); 
		StringBuilder strBuilder = new StringBuilder();
		List<RefundValidation> refundValidationList = new ArrayList<RefundValidation>();
		try {
			setValidationDateFrom(DateCreater.toDateTimeformatCreater(validationDateFrom));
			setValidationDateTo(DateCreater.formDateTimeformatCreater(validationDateTo));
			try {
				refundValidationList = getData(validationMerchant, validationCurrency, validationDateFrom, validationDateTo);
				logger.info("No. of records in refundValidationList : " + refundValidationList.size());
			} catch (Exception exception) {
				logger.error("Inside DownloadRefundValidationFile Class, Call getData Method : ", exception);
			}			
			
			for (RefundValidation refundValidation : refundValidationList) {
				
				String seperator = "|";				
				
				strBuilder.append(refundValidation.getOrderId());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundTag());
				strBuilder.append(seperator);
				//strBuilder.append(Amount.removeDecimalAmount(refundValidation.getRefundAmt(),doc.getString(FieldType.CURRENCY_CODE.toString())));
				strBuilder.append(refundValidation.getRefundAmt());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getBankTxnId());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundStatus());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundProcessDate());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundBankTxnId().toString());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getCancelTxnDate());
				strBuilder.append(seperator);
				//strBuilder.append(Amount.removeDecimalAmount(doc.getString(FieldType.AMOUNT.toString()),doc.getString(FieldType.CURRENCY_CODE.toString())));
				strBuilder.append(refundValidation.getBookingTxnAmt());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getCancelTxnId().toString());
				strBuilder.append(seperator);
				if(StringUtils.isBlank(refundValidation.getRemarks())) {
					strBuilder.append("null");
				} else {
					strBuilder.append(refundValidation.getRemarks());
				}
				if(refundValidationButtonIdentifier.equals("RRN")) {
					strBuilder.append(seperator);
					if(StringUtils.isBlank(refundValidation.getArn())) {
						strBuilder.append("null");
					} else {
						strBuilder.append(refundValidation.getArn());
					}
					strBuilder.append(seperator);
					if(StringUtils.isBlank(refundValidation.getRrn())) {
						strBuilder.append("null");
					} else {
						strBuilder.append(refundValidation.getRrn());
					}
				}
				strBuilder.append("\r\n");
			}
			
			logger.info("Batch has been created successfully !!");
			if(StringUtils.isNotBlank(strBuilder.toString())) {
				String FILE_EXTENSION = ".txt";
				String merchantName = "";
				merchantName = userdao.getBusinessNameByPayId(getValidationMerchant());
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "Refund_Validation_" + merchantName + "_" + df.format(new Date()) + FILE_EXTENSION;
				
				File file = new File(filename);
				file.createNewFile();
				//Write Content
				FileWriter writer = new FileWriter(file);
				writer.write(strBuilder.toString());
				writer.close();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");
			} else {
				addActionMessage("There is no data available in selected date range !!");
				return INPUT;
			}
			logger.info("File has been downloaded successfully !!");
			
		} catch (Exception exception) {
			logger.error("Inside DownloadRefundValidationFile Class, in execute method  : ", exception);
		}

		return SUCCESS;
	}
	
	private List<RefundValidation> getData(String merchantPayId, String currency,String fromDate,String toDate) {
		logger.info("Inside DownloadRefundValidationFile Class, in getData Method !!"); 
		List<RefundValidation> refundValidationList = new ArrayList<RefundValidation>();
		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
			
			if (!fromDate.isEmpty()) {
				
				String currentDate = null;
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern()).add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}
	
			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}
			if (!currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			} else {
				PropertiesManager propertiesManager= new PropertiesManager();
				Map<String,String> allCurrencyMap;
				allCurrencyMap = propertiesManager.getAllProperties(alphabaticFileName);
				 for (Map.Entry<String, String> entry : allCurrencyMap.entrySet()) {
								 currencyConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), entry.getKey()));
				 }
			
				currencyQuery.append("$or", currencyConditionLst);
			}
			
			List<BasicDBObject> refundValidationConditionList = new ArrayList<BasicDBObject>();
			refundValidationConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),  TransactionType.REFUNDRECO.getName()));
			refundValidationConditionList.add(new BasicDBObject(FieldType.STATUS.getName(),  StatusType.SETTLED.getName()));
			refundValidationConditionList.add(new BasicDBObject(FieldType.UDF6.getName(),  null));
			
			BasicDBObject refundValidationQuery = new BasicDBObject("$and", refundValidationConditionList);
			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			
			if (!currencyQuery.isEmpty()) {
				allConditionQueryList.add(currencyQuery);
			}
			
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			
			if (!refundValidationQuery.isEmpty()) {
				allConditionQueryList.add(refundValidationQuery);
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
			MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			
			MongoCursor<Document> cursor = coll.find(finalquery).iterator();
			logger.info("DownloadRefundValidationFile final Query : " + finalquery);
			logger.info("No. of Records : " + coll.count(finalquery));
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				RefundValidation validationReport = new RefundValidation();
				/*Document saleDoc = new Document();
				saleDoc = getSaleTxn(doc.getString(FieldType.OID.toString()));*/
					
				validationReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
				validationReport.setRefundTag(doc.getString(FieldType.REFUND_FLAG.toString()));
				validationReport.setRefundAmt(Amount.removeDecimalAmount(doc.getString(FieldType.AMOUNT.toString()),doc.getString(FieldType.CURRENCY_CODE.toString())));
				validationReport.setBankTxnId(doc.getString(FieldType.ORIG_TXN_ID.toString()));
				validationReport.setRefundStatus(Constants.SUCCESS.getValue());
				/*validationReport.setRefundStatus(doc.getString(FieldType.STATUS.toString()));*/
				//validationReport.setRefundProcessDate(doc.getString(FieldType.CREATE_DATE.toString()));
				validationReport.setRefundProcessDate(DateCreater.formatDateReco(doc.getString(FieldType.CREATE_DATE.toString())));
				validationReport.setRefundBankTxnId(doc.getString(FieldType.PG_REF_NUM.toString()));
				validationReport.setCancelTxnDate(DateCreater.formatDateReco(doc.getString(FieldType.REQUEST_DATE.toString())));
				validationReport.setBookingTxnAmt(Amount.removeDecimalAmount((doc.getString(FieldType.SALE_AMOUNT.toString())),doc.getString(FieldType.CURRENCY_CODE.toString())));
				validationReport.setCancelTxnId(doc.getString(FieldType.REFUND_ORDER_ID.toString()));
				validationReport.setRemarks("");
				validationReport.setOid(doc.getString(FieldType.OID.toString()));
				validationReport.setArn(doc.getString(FieldType.ARN.toString()));
				validationReport.setRrn(doc.getString(FieldType.RRN.toString()));
				refundValidationList.add(validationReport);
			}
			cursor.close();
		} catch (Exception exception) {
			logger.error("Inside DownloadRefundValidationFile Class, in getdata method  : ", exception);
		}
		return refundValidationList;
	}
	
	/*private Document getSaleTxn(String oid) {
		Document doc =  new Document();
		
		try {
			MongoDatabase dbIns = mongoInstance.getDB();  
			MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			List<BasicDBObject> saleAmtConditionList = new ArrayList<BasicDBObject>();
			saleAmtConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),  TransactionType.SALE.getName()));
			saleAmtConditionList.add(new BasicDBObject(FieldType.STATUS.getName(),  StatusType.CAPTURED.getName()));
			saleAmtConditionList.add(new BasicDBObject(FieldType.OID.getName(),oid));
			
			BasicDBObject finalquery = new BasicDBObject("$and", saleAmtConditionList);
			 doc = coll.find(finalquery).first();
		} catch (Exception exception) {
			logger.error("Inside DownloadRefundValidationFile Class, in getSaleAmt method  : ", exception);
		}
		return doc;
	}
	
	private String getRefundDate(String oid) {
		String refundDate = "";
		try {
			MongoDatabase dbIns = mongoInstance.getDB();  
			MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap.get(prefix+Constants.COLLECTION_NAME.getValue()));
			List<BasicDBObject> refundDateConditionList = new ArrayList<BasicDBObject>();
			refundDateConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),  TransactionType.REFUND.getName()));
			refundDateConditionList.add(new BasicDBObject(FieldType.STATUS.getName(),  StatusType.CAPTURED.getName()));
			refundDateConditionList.add(new BasicDBObject(FieldType.OID.getName(),oid));
			
			BasicDBObject finalquery = new BasicDBObject("$and", refundDateConditionList);
			MongoCursor<Document> cursor = coll.find(finalquery).iterator();
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				refundDate = doc.getString(FieldType.CREATE_DATE.getName());
			}
		} catch (Exception exception) {
			logger.error("Inside DownloadRefundValidationFile Class, in getRefundDate method  : ", exception);
		}
		return refundDate;
	}	*/
	
	public String getValidationCurrency() {
		return validationCurrency;
	}
	public void setValidationCurrency(String validationCurrency) {
		this.validationCurrency = validationCurrency;
	}
	public String getValidationDateFrom() {
		return validationDateFrom;
	}
	public void setValidationDateFrom(String validationDateFrom) {
		this.validationDateFrom = validationDateFrom;
	}
	public String getValidationDateTo() {
		return validationDateTo;
	}
	public void setValidationDateTo(String validationDateTo) {
		this.validationDateTo = validationDateTo;
	}
	public String getValidationMerchant() {
		return validationMerchant;
	}
	public void setValidationMerchant(String validationMerchant) {
		this.validationMerchant = validationMerchant;
	}

	public String getRefundValidationButtonIdentifier() {
		return refundValidationButtonIdentifier;
	}

	public void setRefundValidationButtonIdentifier(String refundValidationButtonIdentifier) {
		this.refundValidationButtonIdentifier = refundValidationButtonIdentifier;
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
}
