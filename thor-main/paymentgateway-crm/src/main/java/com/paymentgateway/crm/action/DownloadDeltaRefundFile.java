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

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.DeltaRefund;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Chandan
 *
 */

public class DownloadDeltaRefundFile extends AbstractSecureAction {

	private static final long serialVersionUID = -1471532495305993899L;
	private static Logger logger = LoggerFactory.getLogger(DownloadDeltaRefundFile.class.getName());
	
	@Autowired
	private MongoInstance mongoInstance;
	
	
	@Autowired
	PropertiesManager propertiesManager;
	private static final String alphabaticFileName= "alphabatic-currencycode.properties";
	
	private List<Merchants> merchantList = new LinkedList<Merchants>();
	private Map<String, String> currencyMap = new HashMap<String, String>();
	private String deltaCurrency;
	private String deltaDateFrom;
	private String deltaDateTo;
	private String deltaMerchant;
	private String deltaRefundButtonIdentifier;
	private InputStream fileInputStream;
	private String filename;
	
	@Autowired
	private UserDao userdao;
	
	@Autowired
	private Fields fields;
	
	@Autowired
	private FieldsDao fieldsDao;
	
	private static final String prefix = "MONGO_DB_";
	
	//@SuppressWarnings("resource")
	public String execute() {
		logger.info("Inside DownloadDeltaRefundFile Class !!"); 
		StringBuilder strBuilder = new StringBuilder();
		List<DeltaRefund> deltaRefundList = new ArrayList<DeltaRefund>();
		try {
			setDeltaDateFrom(DateCreater.toDateTimeformatCreater(deltaDateFrom));
			setDeltaDateTo(DateCreater.formDateTimeformatCreater(deltaDateTo));
			try {
				deltaRefundList = getData(deltaMerchant, deltaCurrency, deltaDateFrom, deltaDateTo);
				logger.info("No. of records in refundValidationList : " + deltaRefundList.size());
			} catch (Exception exception) {
				logger.error("Inside DownloadDeltaRefundFile Class, Call getData Method : ", exception);
			}		
			
			for (DeltaRefund deltaRefund : deltaRefundList) {
				
				String seperator = "|";
					strBuilder.append(deltaRefund.getOrderId());
					strBuilder.append(seperator);
					strBuilder.append(deltaRefund.getTxnDate());
					strBuilder.append(seperator);
					strBuilder.append(deltaRefund.getPgRefNum());
					strBuilder.append(seperator);
					strBuilder.append(deltaRefund.getAmount());
					strBuilder.append(seperator);
					strBuilder.append(deltaRefund.getStatus());
					strBuilder.append(seperator);
					strBuilder.append("null");
					strBuilder.append(seperator);
					strBuilder.append(deltaRefund.getRefundTxnDate());
					strBuilder.append(seperator);
					strBuilder.append(deltaRefund.getRefundPgRefNum().toString());
					if(deltaRefundButtonIdentifier.equals("RRN")) {
						strBuilder.append(seperator);
						if(StringUtils.isBlank(deltaRefund.getArn())) {
							strBuilder.append("null");
						} else {
							strBuilder.append(deltaRefund.getArn().toString());
						}
						strBuilder.append(seperator);
						if(StringUtils.isBlank(deltaRefund.getRrn())) {
							strBuilder.append("null");
						} else {
							strBuilder.append(deltaRefund.getRrn().toString());
						}
					}
					strBuilder.append("\r\n");
			}		
			logger.info("Batch has been created successfully !!");
			
			if(StringUtils.isNotBlank(strBuilder.toString())) {
				String FILE_EXTENSION = ".txt";
				String merchantName = "";
				merchantName = userdao.getBusinessNameByPayId(getDeltaMerchant());
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "Delta_Refund_" + merchantName + "_" + df.format(new Date()) + FILE_EXTENSION;
				
				File file = new File(filename);
				file.createNewFile();  			
				 
				//Write Content
				FileWriter writer = new FileWriter(file);
				writer.write(strBuilder.toString());
				writer.close();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");
				logger.info("File has been downloaded successfully !!"); 
			} else {
				addActionMessage("There is no data available in selected date range !!");
				return INPUT;
			}
		} catch (Exception exception) {
			logger.error("Inside DownloadDeltaRefundFile Class, in execute method  : ", exception);
		}

		return SUCCESS;
	}
	
	private List<DeltaRefund> getData(String merchantPayId, String currency,String fromDate,String toDate) throws SystemException {
		logger.info("Inside DownloadDeltaRefundFile Class, in getData Method !!");
		List<DeltaRefund> deltaRefundList = new ArrayList<DeltaRefund>();
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
			
			List<BasicDBObject> deltaRefundConditionList = new ArrayList<BasicDBObject>();
			deltaRefundConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(),  TransactionType.REFUNDRECO.getName()));
			deltaRefundConditionList.add(new BasicDBObject(FieldType.STATUS.getName(),  StatusType.SETTLED.getName()));
			deltaRefundConditionList.add(new BasicDBObject(FieldType.UDF6.getName(),  "Y"));
			
			BasicDBObject deltaRefundQuery = new BasicDBObject("$and", deltaRefundConditionList);
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
			
			if (!deltaRefundQuery.isEmpty()) {
				allConditionQueryList.add(deltaRefundQuery);
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
			logger.info("DownloadDeltaRefundFile final Query : " + finalquery);
			logger.info("No. of Records : " + coll.count(finalquery));
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				//if(isDataExists(doc.getString(FieldType.PG_REF_NUM.toString()))) {
					fields = fieldsDao.getFieldsForSale(doc.getString(FieldType.OID.toString()));
					DeltaRefund deltaReport = new DeltaRefund();				
					deltaReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
					deltaReport.setTxnDate(DateCreater.formatDateReco(fields.get(FieldType.CREATE_DATE.getName())));
					deltaReport.setPgRefNum(fields.get(FieldType.PG_REF_NUM.toString()));
					/*deltaReport.setAmount(Amount.formatAmount(doc.getString(FieldType.AMOUNT.toString()),doc.getString(FieldType.CURRENCY_CODE.toString())));*/
					deltaReport.setAmount(Amount.removeDecimalAmount(doc.getString(FieldType.AMOUNT.toString()), doc.getString(FieldType.CURRENCY_CODE.toString())));
					if(doc.getString(FieldType.STATUS.toString()).equals(StatusType.SETTLED.getName())) {
						deltaReport.setStatus("SUCCESS");
					}
					else {
						deltaReport.setStatus(doc.getString(FieldType.STATUS.toString()));
					}
	
					deltaReport.setRefundTxnDate(DateCreater.formatDateReco(doc.getString(FieldType.CREATE_DATE.toString())));
					deltaReport.setRefundPgRefNum(doc.getString(FieldType.PG_REF_NUM.toString()));
					deltaReport.setOid(doc.getString(FieldType.OID.toString()));
					deltaReport.setArn(doc.getString(FieldType.ARN.toString()));
					deltaReport.setRrn(doc.getString(FieldType.RRN.toString()));
					
					deltaRefundList.add(deltaReport);
				//}
			}
			cursor.close();
		} catch (Exception exception) {
			logger.error("Inside DownloadDeltaRefundFile Class, in getdata method  : ", exception);
		}
		return deltaRefundList;
	}	
	
	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public String getDeltaCurrency() {
		return deltaCurrency;
	}

	public void setDeltaCurrency(String deltaCurrency) {
		this.deltaCurrency = deltaCurrency;
	}

	public String getDeltaDateFrom() {
		return deltaDateFrom;
	}

	public void setDeltaDateFrom(String deltaDateFrom) {
		this.deltaDateFrom = deltaDateFrom;
	}

	public String getDeltaDateTo() {
		return deltaDateTo;
	}

	public void setDeltaDateTo(String deltaDateTo) {
		this.deltaDateTo = deltaDateTo;
	}

	public String getDeltaMerchant() {
		return deltaMerchant;
	}

	public void setDeltaMerchant(String deltaMerchant) {
		this.deltaMerchant = deltaMerchant;
	}

	public String getDeltaRefundButtonIdentifier() {
		return deltaRefundButtonIdentifier;
	}

	public void setDeltaRefundButtonIdentifier(String deltaRefundButtonIdentifier) {
		this.deltaRefundButtonIdentifier = deltaRefundButtonIdentifier;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
}
