package com.paymentgateway.pgui.action;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.TransactionDetailsService;
import com.paymentgateway.commons.email.PepipostEmailSender;
import com.paymentgateway.commons.user.CustomerAddress;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.InvoiceTransactionDao;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.util.AWSSESEmailService;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmEmailer;
import com.paymentgateway.commons.util.PDFCreator;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Shiva
 */
@Service
public class SendInvoicePdfToEmail {

	@Autowired
	CrmEmailer crmEmailer;

	@Autowired
	private InvoiceTransactionDao invoiceTransactionDao;

	@Autowired
	private PDFCreator pdfCreator;
	
	@Autowired
	private TransactionDetailsService transactionServiceDao;
	
	@Autowired
	FieldsDao fieldsDao;

	@Autowired
	private PropertiesManager propertiesManager;
	
	
	
	@Autowired
	PepipostEmailSender pepipostEmailSender;
	
	public String orderId;

	private static final long serialVersionUID = 2833274410595144130L;
	private static Logger logger = LoggerFactory.getLogger(SendInvoicePdfToEmail.class.getName());

	public String SendInvoicePdfToEmail(String requestOrderId) {
		try {
			this.setOrderId(requestOrderId);
			Invoice invoice = new Invoice();
//			String orderId = (String) sessionMap.get(FieldType.ORDER_ID.getName());
			TransactionSearch transactionSearch=transactionServiceDao.getTransactionForInvoicePdf(orderId);
			CustomerAddress customerAddress=fieldsDao.fetchCustomerInfo(orderId);
			invoice = invoiceTransactionDao.findByInvoiceId(orderId);
			String subject = "Payment GateWay Order -- Order ID " + orderId;
			String emailBody = getInvoiceBody(invoice);
			String fileName = "Invoice_" + requestOrderId + ".pdf";
			File file = new File(fileName);
			pdfCreator.createCustomInvoicePdf(transactionSearch,customerAddress,file);
			pepipostEmailSender.invoiceEmailWithAttachment(emailBody, subject, invoice.getEmail(), invoice.getEmail(), file,
					false);
			logger.info("pdf send to email");
		} catch (Exception e) {
			logger.error("Exception caught while sending email, " + e);
		}
		return "SUCCESS";
	}
	public String customInvoice(String requestOrderId) {
		try {
			this.setOrderId(requestOrderId);
			TransactionSearch transactionSearch=transactionServiceDao.getTransactionForInvoicePdf(orderId);
			CustomerAddress customerAddress=fieldsDao.fetchCustomerInfo(orderId);
			String subject = "Payment GateWay Order -- Order ID " + orderId;
			String emailBody = getCustomInvoiceBody(transactionSearch,customerAddress);
			String fileName = "Invoice_" + orderId + ".pdf";
			File file = new File(fileName);
			pdfCreator.createCustomInvoicePdf(transactionSearch,customerAddress,file);
			
			return pepipostEmailSender.invoiceEmailWithAttachment(emailBody, subject, transactionSearch.getCustomerEmail(), transactionSearch.getCustomerEmail(), file,
					false);
			
		} catch (Exception e) {
			logger.error("Exception caught while sending email, " + e);
		}
		return null;
	}

	public String getInvoiceBody(Invoice invoice) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>Invoice</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			content.append("<body>");
			content.append(
					"<table width=\"450\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 20px;padding-top: 5px\">");
			content.append("<tbody><tr>");
			content.append("<td align=\"center\"><img src=\"" + propertiesManager.getSystemProperty("emailerLogoURL")
					+ "\" alt=\"\" ></td></tr>");
			content.append(
					"<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">Your Invoice</td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Name</td>");
			content.append("<td>" + invoice.getName() + "</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append(
					"<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Product Name</td>");
			content.append("<td>" + invoice.getProductName() + "</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Mobile</td>");
			if (invoice.getPhone() != null) {
				content.append("<td>" + invoice.getPhone() + "</td></tr>");
			} else {
				content.append("<td>" + "" + "</td></tr>");
			}
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Email</td>");
			content.append("<td>" + invoice.getEmail() + "</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append(
					"<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Amount INR</td>");
			content.append("<td>" + invoice.getTotalAmount() + "</td></tr>");

			content.append("</table></td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append(
					"<td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;\">Thanks<br>");
			content.append("<span style=\"display: block;\">Team Payment GateWay</span></td></tr>");
			content.append("<tr><td style=\"font-size: 12px;\">");
			content.append(
					"For any queries feel free to connect with us at +91 120 433 4884. You may also drop your query to us at ");
			content.append("<a href=\"mailto:support@paymentgateway.com\">support@paymentgateway.com</a></td></tr>");
			content.append(
					"<tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">&copy; 2020 www.paymentgateway.com All rights reserved.</td></tr>");
			content.append("</table></td></tr></tbody></table></body></html>");
			body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}
	
	public String getCustomInvoiceBody(TransactionSearch transaction, CustomerAddress customerAddress) {
		String body = null;
		StringBuilder content = new StringBuilder();
		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>Invoice</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			content.append("<body>");
			content.append(
					"<table width=\"450\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 20px;padding-top: 5px\">");
			content.append("<tbody><tr>");
			content.append("<td align=\"center\"><img src=\"" + propertiesManager.getSystemProperty("emailerLogoURL")
					+ "\" alt=\"\" ></td></tr>");
			content.append(
					"<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">Your Invoice</td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Name</td>");
			content.append("<td>" + transaction.getCustomerName() + "</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Mobile</td>");
			if (transaction.getCustomerMobile()!= null) {
				content.append("<td>" + transaction.getCustomerMobile() + "</td></tr>");
			} else if (customerAddress.getCustPhone()!= null) {
				content.append("<td>" + customerAddress.getCustPhone()+ "</td></tr>");
			}else{
				content.append("<td>" +""+ "</td></tr>");
			}
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Email</td>");
			content.append("<td>" + transaction.getCustomerEmail()+ "</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append(
					"<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Amount INR</td>");
			content.append("<td>" + transaction.getTotalAmount() + "</td></tr>");

			content.append("</table></td></tr>");
			content.append(
					"<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append(
					"<td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;\">Thanks<br>");
			content.append("<span style=\"display: block;\">Team Payment GateWay</span></td></tr>");
			content.append("<tr><td style=\"font-size: 12px;\">");
			content.append(
					"For any queries feel free to connect with us at +91 120 433 4884. You may also drop your query to us at ");
			content.append("<a href=\"mailto:support@paymentgateway.com\">support@paymentgateway.com</a></td></tr>");
			content.append(
					"<tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">&copy; 2020 www.paymentgateway.com All rights reserved.</td></tr>");
			content.append("</table></td></tr></tbody></table></body></html>");
			body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}
	
	
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	
}