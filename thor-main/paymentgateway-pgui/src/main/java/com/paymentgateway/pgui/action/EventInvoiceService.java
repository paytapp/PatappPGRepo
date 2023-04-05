package com.paymentgateway.pgui.action;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;


@Service
public class EventInvoiceService extends AbstractSecureAction{
	
	private static final long serialVersionUID = -5237691376891519457L;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;
	
	private File eventInvoice;
	private String orderId;
	private String payId;
	private String fileName;
	
	private static final String prefix = "MONGO_DB_";
	private static Logger logger = LoggerFactory.getLogger(EventInvoiceService.class.getName());
	
	public String execute() {
		
		try {
			
			String [] fileNameSplit = fileName.split("\\.");
			int lastindex = fileNameSplit.length - 1;
			String extension = fileNameSplit[lastindex];
			
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.EVENT_INVOICE_COLLECTION.getValue()));
			
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			
				FileInputStream imageInFile = new FileInputStream(eventInvoice);
				byte fileData[] = new byte[(int) eventInvoice.length()];
				imageInFile.read(fileData);
				String base64File = Base64.getEncoder().encodeToString(fileData);
				
				imageInFile.close();
				FileUtils.forceDelete(eventInvoice);
			// Search if file already exists for this Order Id , if yes replace with new invoice
			BasicDBObject orderIdQuery = new BasicDBObject("ORDER_ID",orderId);
			
			if (coll.countDocuments(orderIdQuery) > 0) {
				
				logger.info("Invoice already uploaded for Order Id = " +orderId +" , replacing previous file" );
				
				Document query = new Document();
				query.append("ORDER_ID", orderId);
				Document setData = new Document();
			
				setData.put("FILE_BASE_64", base64File);
				setData.put("UPDATE_DATE", dateNow);
				setData.put("PAY_ID", payId);
				setData.put("DATA_TYPE", extension);
				
				Document update = new Document();
				update.append("$set", setData);
				coll.updateOne(query, update);
				
			}
			
			else {
				
				Document doc = new Document();
				doc.put("id", TransactionManager.getNewTransactionId());
				doc.put("ORDER_ID", orderId);
				doc.put("CREATE_DATE", dateNow);
				doc.put("FILE_BASE_64", base64File);
				doc.put("PAY_ID", payId);
				doc.put("DATA_TYPE", extension);
				coll.insertOne(doc);
			}
			
		}
		
		catch(Exception e) {
			logger.error("Exception in uploading event page invoice ",e);
			return SUCCESS;
		}
		return SUCCESS;
		
	}

	
	public File getEventInvoice() {
		return eventInvoice;
	}

	public void setEventInvoice(File eventInvoice) {
		this.eventInvoice = eventInvoice;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}


	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	
}
