package com.paymentgateway.crm.action;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.InvoiceTransactionDao;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.QRCodeCreator;

public class DownloadQRCodeAction extends AbstractSecureAction {
	
	@Autowired
	private InvoiceTransactionDao invoiceTransactionDao;
	
	@Autowired
	private QRCodeCreator qRCodeCreator;
	@Autowired
	private CrmValidator validator;
	

	private static final long serialVersionUID = -5708726455052826940L;
	private InputStream fileInputStream;
	private static Logger logger = LoggerFactory
			.getLogger(DownloadQRCodeAction.class.getName());
	private String invoiceId;

	public String downloadQRCode() {
		try {

			Invoice invoiceDB = invoiceTransactionDao
					.findByInvoiceId(getInvoiceId());

			BufferedImage image = qRCodeCreator.generateQRCode(invoiceDB);
			File file = new File("qrcode.jpg");
			ImageIO.write(image, "jpg", file);
			fileInputStream = new FileInputStream(file);

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	public void validate(){

	if ((validator.validateBlankField(getInvoiceId()))) {
		addFieldError(CrmFieldType.INVOICE_ID.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.INVOICE_ID, getInvoiceId()))) {
		addFieldError(CrmFieldType.INVOICE_ID.getName(), validator.getResonseObject().getResponseMessage());
	}
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

}
