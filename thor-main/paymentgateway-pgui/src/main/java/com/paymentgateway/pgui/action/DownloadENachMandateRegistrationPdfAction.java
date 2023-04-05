package com.paymentgateway.pgui.action;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.util.PDFCreator;

/**
 * @author Rajit
 */
public class DownloadENachMandateRegistrationPdfAction extends AbstractSecureAction {
	
	@Autowired
	private PDFCreator pdfCreator;

	@Autowired
	private FieldsDao fieldsDao;
	
	private static final long serialVersionUID = 1603680920716509422L;

private static Logger logger = LoggerFactory.getLogger(DownloadENachMandateRegistrationPdfAction.class.getName());
	
	private String txnId;
	private InputStream fileInputStream;
	private String fileName;

	public String execute() {
		try {
			logger.info("Creating E-Mandate PDF file");
			
			Map<String, String> registrationDetails = fieldsDao.getEnachMandateDetailsByTxnId(txnId);
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			fileName = "E_Mandate_" + df.format(new Date()) + ".pdf";
			File file = new File(fileName);
			
			fileInputStream = pdfCreator.createEMandateRegistrationPdf(registrationDetails, file);
		} catch (Exception ex) {
			logger.info("Exception caught " + ex);
			return ERROR;
		}
		return SUCCESS;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
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
