package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Base64EncodeDecode;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

public class DownloadQRImageAction extends AbstractSecureAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5110061341599435693L;

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private Base64EncodeDecode base64EncodeDecode;
	
	private static Logger logger = LoggerFactory.getLogger(DownloadQRCodeAction.class.getName());
	private InputStream fileInputStream;
	private static final String prefix = "MONGO_DB_";
	private String invoiceId;
	private String qrType;
	private String payId;
	private String fileName;
	
	
	@SuppressWarnings("static-access")
	public String downloadStaticQRCode() {
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			String base64String = getBase64StaticQr(getPayId());
			if(StringUtils.isNotBlank(qrType) && qrType.equalsIgnoreCase("PGQR")) {
				setFileName(getPayId() + ".png");
			}else {
				setFileName("upi" + getPayId() + ".png");
			}
			File file = new File(fileName);
			if(StringUtils.isNotBlank(base64String)) {
				file = base64EncodeDecode.decoder(base64String,getFileName());
			}
			fileInputStream = new FileInputStream(file);

		} catch(FileNotFoundException excp) {
			logger.error("Exception", excp);
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return SUCCESS;

	}
	public String getBase64StaticQr(String payId) throws Exception{
		String base64String = null;
//		
		MongoDatabase dbIns = mongoInstance.getDB();
		
		MongoCollection<Document> collection = dbIns.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.QR_COLLECTION.getValue()));

		MongoCursor<Document> cursor = collection.find(new BasicDBObject(FieldType.PAY_ID.getName(),payId)).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			if(StringUtils.isNotBlank(qrType) && qrType.equalsIgnoreCase("PGQR")) {
				base64String = doc.getString(FieldType.PG_QR_CODE.getName());
			}else {
				base64String = doc.getString(FieldType.UPI_QR_CODE.getName());
			}
		}
		
		return base64String;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getQrType() {
		return qrType;
	}
	public void setQrType(String qrType) {
		this.qrType = qrType;
	}

}
