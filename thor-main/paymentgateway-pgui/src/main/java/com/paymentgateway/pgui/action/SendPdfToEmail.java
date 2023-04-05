package com.paymentgateway.pgui.action;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.EPOSTransactionDao;
import com.paymentgateway.commons.dao.TransactionDetailsService;
import com.paymentgateway.commons.email.PepipostEmailSender;
import com.paymentgateway.commons.user.EPOSTransaction;
import com.paymentgateway.commons.util.AWSSESEmailService;
import com.paymentgateway.commons.util.CrmEmailer;
import com.paymentgateway.commons.util.PDFCreator;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class SendPdfToEmail {

	private String bookingId;
	private String orderId;

	@Autowired
	private PDFCreator pdfCreator;

	@Autowired
	private EPOSTransactionDao eposTransactionDao;
	

	@Autowired
	private CrmEmailer crmEmailer; 
	
	@Autowired
	private TransactionDetailsService transactionDetailsService;

	@Autowired
	AWSSESEmailService awsSESEmailService;
	
	@Autowired
	PepipostEmailSender pepipostEmailSender; 

	private String fileName;
	private String response;
	private static final long serialVersionUID = 6914325261447336533L;

	private static Logger logger = LoggerFactory.getLogger(SendPdfToEmail.class.getName());

	public String eposDetailsEmail(String requestOrderId) {
		try {
			setOrderId(requestOrderId);
			logger.info("Inside eposDetailsEmail() of SendPdfToEmail class, sending EPOS PDF to email");
			if (StringUtils.isBlank(orderId)) {
				return "failed";
			}
			String subject = "EPOS Payment Success -- Invoice ID " + bookingId;
			EPOSTransaction epos = eposTransactionDao.findByInvoiceId(orderId);
			if (StringUtils.isNotBlank(epos.getCUST_EMAIL())) {
				fileName = "Invoice_" + orderId + ".pdf";
				File file = new File(fileName);
				pdfCreator.createEposPdf(epos, file);
				String emailBody = crmEmailer.getEposEmailBodyForCustomer(true, epos);
				 return  pepipostEmailSender.eposEmailWithAttachmentToCustomer(emailBody, subject, epos.getCUST_EMAIL(), file);
				
			}
		

		} catch (Exception e) {
			logger.error("Exception caught, " + e);
			return "failed";
		}
		return "failed";
	}

	public String getBookingId() {
		return bookingId;
	}

	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
}
