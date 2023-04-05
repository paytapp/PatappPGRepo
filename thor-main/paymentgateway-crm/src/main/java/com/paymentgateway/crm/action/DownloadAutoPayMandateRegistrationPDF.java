package com.paymentgateway.crm.action;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.UpiAutoPayDao;
import com.paymentgateway.commons.util.PDFCreator;
/**
 * @author Rajit
 */
public class DownloadAutoPayMandateRegistrationPDF extends AbstractSecureAction {
	
	private static final long serialVersionUID = 5371810619445675513L;

	@Autowired
	private PDFCreator pdfCreator;
	
	@Autowired
	private UpiAutoPayDao upiAutoPayDao;
	
	private static Logger logger = LoggerFactory.getLogger(DownloadAutoPayMandateRegistrationPDF.class.getName());
	
	private String orderId;
	
	private InputStream fileInputStream;
	private String fileName;

	public String execute() {
		try {
			logger.info("creating autoPay mandate PDF file");
			
			Map<String, String> registrationDetails = upiAutoPayDao.getAutoPayMandateDetailsByOrderId(orderId);
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			fileName = "Upi_AutoPay_Mandate_" + df.format(new Date()) + ".pdf";
			File file = new File(fileName);
			
			fileInputStream = pdfCreator.createAutoPayRegistrationPdf(registrationDetails, file);
		} catch (Exception ex) {
			logger.info("Exception caught " ,ex);
			return ERROR;
		}
		return SUCCESS;
	}

	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}
}