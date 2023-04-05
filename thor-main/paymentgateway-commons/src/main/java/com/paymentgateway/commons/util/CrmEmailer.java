package com.paymentgateway.commons.util;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.TransactionDetailsService;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.EPOSTransaction;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.sun.mail.smtp.SMTPTransport;

/**
 * @author Shaiwal
 *
 */
@Component
public class CrmEmailer {

	private Logger logger = LoggerFactory.getLogger(CrmEmailer.class.getName());

	@Autowired
	@Qualifier("propertiesManager")
	private PropertiesManager propertiesManager;

	@Autowired
	private Base64EncodeDecode base64EncodeDecode;
	
	@Autowired
	private TransactionDetailsService transactionDetailsService;
	
	@Autowired
	private Fields field;

	final String username = propertiesManager.propertiesMap.get("MailUserName");
	final String password = propertiesManager.propertiesMap.get("Password");
	final String emailPort = propertiesManager.propertiesMap.get("EmailPort");

	Properties props = new Properties();
	{
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.office365.com");
		props.put("mail.smtp.port", emailPort);
	}

	Session session = Session.getInstance(props, new javax.mail.Authenticator() {
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username, password);
		}
	});

	public void sendEmail(String body, String subject, String mailTo, String mailBcc, boolean emailExceptionHandlerFlag)
			throws Exception {

		if (StringUtils.isBlank(mailTo) && StringUtils.isBlank(mailBcc)) {
			return;
		}
		try {

			Message message = new MimeMessage(session);

			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
			message.setDataHandler(new DataHandler(new HTMLDataSource(body)));
			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect("smtp.office365.com", username, password);
			message.setSubject(subject);
			message.setText(body);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
			message.setSubject(subject);
			message.setText(body);

			Transport.send(message);

			logger.info("Email Sent to " + field.maskEmail(mailTo));
		} catch (Exception exception) {
			logger.error("Exception in sending email ", exception);
		}
	}

	public boolean invoiceEmail(String body, String subject, String mailTo, String mailBcc,
			boolean emailExceptionHandlerFlag) throws Exception {

		if (StringUtils.isBlank(mailTo) && StringUtils.isBlank(mailBcc)) {
			return false;
		}
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
			message.setSubject(subject);
			message.setDataHandler(new DataHandler(new HTMLDataSource(body)));
			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect("smtp.office365.com", username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			logger.info("Email Sent to " + field.maskEmail(mailTo));
			return true;
		} catch (Exception exception) {
			logger.error("Exception in sending email ", exception);
			return false;
		}

	}

	public void invoiceEmailWithAttachment(String body, String subject, String mailTo, String mailBcc, File file,
			boolean emailExceptionHandlerFlag) throws Exception {

		if (StringUtils.isBlank(mailTo) && StringUtils.isBlank(mailBcc)) {
			return;
		}
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
			message.setSubject(subject);
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setDataHandler(new DataHandler(new HTMLDataSource(body)));
			Multipart multipart = new MimeMultipart();

			multipart.addBodyPart(messageBodyPart);

			messageBodyPart = new MimeBodyPart();

			DataSource source = new FileDataSource(file);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(file.getName());
			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);

			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect("smtp.office365.com", username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			logger.info("Email Sent to " + field.maskEmail(mailTo));

		} catch (Exception exception) {
			logger.error("Exception in sending email ", exception);
		}

	}

	public void sendAttachmentFileEmail(String body, String subject, String mailTo, String mailBcc,
			boolean emailExceptionHandlerFlag) throws Exception {
		if (StringUtils.isBlank(mailTo) && StringUtils.isBlank(mailBcc)) {
			return;
		}

		String mailSender = propertiesManager.getEmailProperty("MailSender");
		String username = propertiesManager.getEmailProperty("MailUserName");
		String password = propertiesManager.getEmailProperty("Password");
		String emailactiveflag = propertiesManager.getEmailProperty("EmailActiveFlag");
		InternetAddress[] toAddresses = null;
		InternetAddress[] bccAddresses = null;
		try {
			if (emailactiveflag != "true") {
				Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
				MimeBodyPart messageBodyPart = new MimeBodyPart();
				Multipart multipart = new MimeMultipart();
				messageBodyPart = new MimeBodyPart();
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(mailSender));
				// Sending Bulk Email
				if (!StringUtils.isBlank(mailTo) && !StringUtils.isBlank(mailBcc)) {
					toAddresses = InternetAddress.parse(mailTo);
					bccAddresses = InternetAddress.parse(mailBcc);
					message.setRecipients(Message.RecipientType.TO, toAddresses);
					message.setRecipients(Message.RecipientType.BCC, bccAddresses);
				} else if (!StringUtils.isBlank(mailTo)) {
					toAddresses = InternetAddress.parse(mailTo);
					message.setRecipients(Message.RecipientType.TO, toAddresses);
				} else {
					bccAddresses = InternetAddress.parse(mailBcc);
					message.setRecipients(Message.RecipientType.BCC, bccAddresses);
				}
				message.setSubject(subject);
				message.setSentDate(new Date());
				// set Attachment file
				DataSource source = new FileDataSource(body);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(body);
				multipart.addBodyPart(messageBodyPart);
				message.setContent(multipart);
				// sends the e-mail
				Transport.send(message);
			} else {
				logger.info("Emailing Feature Disabled : " + "Email Recipient :" + field.maskEmail(mailTo) + "Mail Subject :" + subject
						+ "Email Body :" + body);
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		if (emailExceptionHandlerFlag) {
			// throw new SystemException(ErrorType.EMAIL_ERROR,
			// ErrorType.EMAIL_ERROR.getResponseMessage());
		}
	}

	public void eposEmailWithAttachmentToCustomer(EPOSTransaction epos, String subject, String mailTo, boolean status,
			File file) throws Exception {
		logger.info("Sending success EPOS transaction Email to Customer with Email Id: " + mailTo);
		if (StringUtils.isBlank(mailTo)) {
			return;
		}
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
			message.setSubject(subject);
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart
					.setDataHandler(new DataHandler(new HTMLDataSource(getEposEmailBodyForCustomer(status, epos))));
			Multipart multipart = new MimeMultipart();

			multipart.addBodyPart(messageBodyPart);

			messageBodyPart = new MimeBodyPart();

			DataSource source = new FileDataSource(file);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(file.getName());
			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);

			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect("smtp.office365.com", username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			logger.info("Email Sent to " + field.maskEmail(mailTo));

		} catch (Exception exception) {
			logger.error("Exception in sending email ", exception);
		}

	}

	public String getEposEmailBodyForCustomer(boolean status, EPOSTransaction epos) {
		StringBuilder body = new StringBuilder();
		body.append(
				"<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		body.append("<title>EPOS Invoice | Payment Success</title>");
		body.append("<style>body{font-family: Arial, Helvetica, sans-serif;}</style>");
		body.append("</head><body>");
		body.append(
				"<table width=\"450\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 20px;padding-top: 5px\">");
		body.append("<td align=\"center\"><img src=" + propertiesManager.getSystemProperty("emailerLogoURL")
				+ " alt=\"\" ></td></tr>");
		body.append(
				"<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">");
		body.append("Your Payment Invoice");
		body.append("</td></tr><tr><td>");
		body.append(
				" <table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");

		if (status) {
			body.append(
					"<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\"><td style=\"font-weight: 600; width:200px;color: #888888;\">Payment Status</td>");
			body.append("<td><b>Success</b></td></tr>");
		} else {
			body.append(
					"<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\"><td style=\"font-weight: 600; width:200px;color: #888888;\">Payment Status</td>");
			body.append("<td> <b>Failed</b></td></tr>");
		}

		body.append(
				"<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\"><td style=\"font-weight: 600; width:200px;color: #888888;\">Invoice ID</td>");
		body.append("<td>" + epos.getINVOICE_ID() + "</td></tr>");
		body.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
		body.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Mobile</td>");
		if (StringUtils.isNotBlank(epos.getCUST_MOBILE())) {
			body.append("<td>" + epos.getCUST_MOBILE() + "</td>");
		} else {
			body.append("<td> NA </td>");
		}
		body.append(
				"</tr><tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\"><td style=\"font-weight: 600; width:200px;color: #888888;\">Email ID</td>");
		if (StringUtils.isNotBlank(epos.getCUST_EMAIL())) {
			body.append("<td>" + epos.getCUST_EMAIL() + "</td>");
		} else {
			body.append("<td> NA </td>");
		}
		body.append("</tr><tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
		body.append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Amount INR</td>");
		body.append("<td>" + epos.getAMOUNT() + "</td></tr>");
		body.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
		body.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Business Name</td>");
		body.append("<td>" + epos.getBUSINESS_NAME() + "</td></tr>");
		body.append("</table></td></tr><tr> <td>");
		body.append(
				"<table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"> <tr>");
		body.append(
				"<td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;\">Thanks<br><span style=\"display: block;\">Team Payment Gateway</span></td>");
		body.append(
				"</tr> <tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">&copy; 2020 www.paymentgateway.com All rights reserved.</td></tr>");
		body.append(
				"<tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">Please reach us at support@paymentgateway.com in case of queries.</td>");
		body.append(" </tr></table></td></tr></tbody></table></body></html>");

		return body.toString();
	}

	public void eposEmailWithAttachmentToMerchant(EPOSTransaction epos, String subject, String mailTo, File file)
			throws Exception {
		logger.info("Sending success EPOS transaction Email to Merchant with Email Id: " + mailTo);
		if (StringUtils.isBlank(mailTo)) {
			return;
		}
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
			message.setSubject(subject);
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setDataHandler(new DataHandler(new HTMLDataSource(getEposEmailBodyForMerchant(epos))));
			Multipart multipart = new MimeMultipart();

			multipart.addBodyPart(messageBodyPart);

			messageBodyPart = new MimeBodyPart();

			DataSource source = new FileDataSource(file);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(file.getName());
			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);

			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect("smtp.office365.com", username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			logger.info("Email Sent to " + mailTo);

		} catch (Exception exception) {
			logger.error("Exception in sending email ", exception);
		}
	}

	public String getEposEmailBodyForMerchant(EPOSTransaction epos) {
		StringBuilder body = new StringBuilder();

		body.append(
				"<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		body.append("<title>EPOS Invoice | Payment Received</title>");
		body.append("<style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
		body.append(
				"<body><table width=\"450\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 20px;padding-top: 5px\">");
		body.append("<tbody><tr>");
		body.append("<td align=\"center\"><img src=" + propertiesManager.getSystemProperty("emailerLogoURL")
				+ " alt=\"\" ></td></tr>");
		body.append(
				"<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">");
		body.append("Invoice Payment Received");
		body.append(
				"</td></tr><tr><td><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
		body.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
		body.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Customer Mobile</td>");
		if (StringUtils.isNotBlank(epos.getCUST_MOBILE())) {
			body.append("<td>" + epos.getCUST_MOBILE() + "</td>");
		} else {
			body.append("<td> NA </td>");
		}
		body.append(
				"</tr><tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\"><td style=\"font-weight: 600; width:200px;color: #888888;\">Customer Email ID</td>");
		if (StringUtils.isNotBlank(epos.getCUST_EMAIL())) {
			body.append("<td>" + epos.getCUST_EMAIL() + "</td>");
		} else {
			body.append("<td> NA </td>");
		}
		body.append("</tr><tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
		body.append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Amount INR</td>");
		body.append("<td>" + epos.getAMOUNT() + "</td></tr>");
		body.append("</table></td></tr><tr> <td>");
		body.append(
				"<table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"> <tr>");
		body.append(
				"<td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;\">Thanks<br><span style=\"display: block;\">Team Payment Gateway</span></td>");
		body.append(
				"</tr> <tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">&copy; 2020 www.paymentgateway.com All rights reserved.</td></tr>");
		body.append(
				"<tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">Please reach us at support@paymentgateway.com in case of queries.</td>");
		body.append(" </tr></table></td></tr></tbody></table></body></html>");

		return body.toString();
	}
	
	
	public void sendTransactionEmailToCustomer(Fields fields,User user) throws Exception {

		try {
			
		if (StringUtils.isBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			logger.info("Customer Email Not Found");
			return;
			
		}
		
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(fields.get(FieldType.CUST_EMAIL.getName())));
			message.setSubject("Your Transaction is Successful | "+user.getBusinessName()+" | "+fields.get(FieldType.ORDER_ID.getName())+"");
			message.setDataHandler(new DataHandler(new HTMLDataSource(customerTransactionEmailBody(fields,user))));
			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect("smtp.office365.com", username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			logger.info("Email Sent to " + field.maskEmail(fields.get(FieldType.CUST_EMAIL.getName())));
			
		} catch (Exception exception) {
			logger.error("Exception in sending email ", exception);
			
		}

	}
	
	
	
	public String customerTransactionEmailBody(Fields fields,User user) throws SystemException {
		
		TransactionSearch transactionDetail=transactionDetailsService.getTransactionForInvoicePdf(fields.get(FieldType.ORDER_ID.getName()));
		
		String logoUrl = propertiesManager.propertiesMap.get("logoForEmail");
		String totalAmount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),fields.get(FieldType.CURRENCY_CODE.getName()));
		
		String[] createDate=fields.get(FieldType.RESPONSE_DATE_TIME.getName()).split(" ");
		String date=changeDateFormat(createDate[0]);
		StringBuilder body = new StringBuilder();
		
		body.append("<!DOCTYPE html>");
		body.append("<html lang='en'> <head> <meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
		body.append("<title>Transaction Email</title>");
		body.append("<style> body{ font-family: Arial, Helvetica, sans-serif; } </style>");
		body.append("</head>");
		body.append("<body bgcolor='#ccc'>");
		body.append("<table width='700' border='0' border-collapse='collapse' cellpadding='0' cellspacing='0' bgcolor='#fff' align='center'>");
		body.append("<tbody> <tr>");
		body.append("<td width='70%' bgcolor='#002664' style='padding: 10px;color: #fff;font-size: 24px;'>E-mail notification</td>");
		body.append("<td width='30%' style='padding: 10px;' align='right'> <img src="+logoUrl+" alt='' width='150'> </td>");
		body.append("</tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px;padding-bottom: 0; padding-top: 20px;'>");
		body.append("<span>SHOP : <b>"+user.getBusinessName()+"</b></span>");
		body.append("<br> <span style='display: block;margin-top: 8px;width:100%'>URL Address : <a href='https://www.paymentgateway.com'>paymentgateway.com</a></span> </td> </tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px;'>");
		body.append("<h2 style='margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;'>");
		if(StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))){
			body.append("Hello "+fields.get(FieldType.CUST_NAME.getName())+", <br><br>");
		}else{
			body.append("Dear Customer, <br><br>");
		}
		
		body.append("This e-mail confirms that we have recorded your request for the payment of INR "+totalAmount+" for your Order ID "+fields.get(FieldType.ORDER_ID.getName())+" in "+user.getBusinessName()+".</h2> </td> </tr>");
		body.append("<tr> <td colspan='3'> <h2 style='margin: 0;padding-left: 10px;'>Payment Details</h2> </td> </tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px'>");
		body.append("<table width='100%' cellpadding='10' border='0' bgColor='#f5f5f5'> <tr>");
		body.append("<td>Order ID</td> <td>"+fields.get(FieldType.ORDER_ID.getName())+"</td> </tr>");
		body.append("<tr> <td colspan='2'> <b>"+MopType.getmopName(transactionDetail.getMopType())+": INR "+totalAmount+" </b><hr></td> </tr>");
		body.append("<tr> <td>Date/Time</td> <td>"+date+" | "+createDate[1]+"</td> </tr>");
		if(StringUtils.isNotBlank(transactionDetail.getCardNumber()))
			body.append("<tr> <td>Card Number</td> <td>"+transactionDetail.getCardNumber()+"</td> </tr>");
		else
			body.append("<tr> <td>Card Number</td> <td>NA</td> </tr>");
		body.append("<tr> <td>Type</td> <td>"+PaymentType.getpaymentName(transactionDetail.getPaymentMethods())+"</td> </tr>");
		body.append("<tr> <td>PG Ref Number</td> <td>"+transactionDetail.getPgRefNum()+"</td> </tr> </table> </td> </tr>");
		body.append("<tr><td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'>Please connect at <a href ='mailto:support@paymentgateway.com'>support@paymentgateway.com</a> for any information pertaining to the status of your payment.</td></tr>");
		body.append("<tr><td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'>You may also connect with <b>"+user.getBusinessName()+"</b> for further queries, if any, on status of your order.</td></tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'> Please do not reply to this e-mail. The address <a href='#'>paymentgateway.com</a> cannot process replies. </td> </tr>");
		body.append("</tbody> </table> </body> </html>");
		
		return body.toString();
	}
	
	public void sendTransactionFailedEmailToCustomer(Fields fields,User user) throws Exception {

		try {
			
		if (StringUtils.isBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			logger.info("Customer Email Not Found");
			return;
			
		}
		
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(fields.get(FieldType.CUST_EMAIL.getName())));
			message.setSubject("OOPS! Your transaction couldn't get through | "+user.getBusinessName()+" | "+fields.get(FieldType.ORDER_ID.getName())+"");
			message.setDataHandler(new DataHandler(new HTMLDataSource(customerTransactionFailedEmailBody(fields,user))));
			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect("smtp.office365.com", username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			logger.info("Email Sent to " + field.maskEmail(fields.get(FieldType.CUST_EMAIL.getName())));
			
		} catch (Exception exception) {
			logger.error("Exception in sending email ", exception);
			
		}

	}
	
	public String customerTransactionFailedEmailBody(Fields fields,User user) throws SystemException {
		TransactionSearch transactionDetail=transactionDetailsService.getTransactionForInvoicePdf(fields.get(FieldType.ORDER_ID.getName()));

		String logoUrl = propertiesManager.propertiesMap.get("logoForEmail");
		String totalAmount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),fields.get(FieldType.CURRENCY_CODE.getName()));
		
		String[] createDate=fields.get(FieldType.RESPONSE_DATE_TIME.getName()).split(" ");
		String date=changeDateFormat(createDate[0]);
		StringBuilder body = new StringBuilder();
		
		
		body.append("<!DOCTYPE html>");
		body.append("<html lang='en'> <head> <meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
		body.append("<title>Transaction Email</title>");
		body.append("<style> body{ font-family: Arial, Helvetica, sans-serif; } </style>");
		body.append("</head>");
		body.append("<body bgcolor='#ccc'>");
		body.append("<table width='700' border='0' border-collapse='collapse' cellpadding='0' cellspacing='0' bgcolor='#fff' align='center'>");
		body.append("<tbody> <tr>");
		body.append("<td width='70%' bgcolor='#002664' style='padding: 10px;color: #fff;font-size: 24px;'>E-mail notification</td>");
		body.append("<td width='30%' style='padding: 10px;' align='right'> <img src="+logoUrl+" alt='' width='150'> </td>");
		body.append("</tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px;padding-bottom: 0; padding-top: 20px;'>");
		body.append("<span>SHOP : <b>"+user.getBusinessName()+"</b></span>");
		body.append("<br> <span style='display: block;margin-top: 8px;width:100%'>URL Address : <a href='https://www.paymentgateway.com'>paymentgateway.com</a></span> </td> </tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px;'>");
		body.append("<h2 style='margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;'>");
		if(StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))){
			body.append("Hello "+fields.get(FieldType.CUST_NAME.getName())+", <br><br>");
		}else{
			body.append("Dear Customer,<br><br>");
		}
		
		body.append("Your payment of INR "+totalAmount+" for your Order ID "+fields.get(FieldType.ORDER_ID.getName())+" in "+user.getBusinessName()+" couldn't get through. Please try again.</h2> </td> </tr>");
		body.append("<tr> <td colspan='3'> <h2 style='margin: 0;padding-left: 10px;'>Payment Details</h2> </td> </tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px'>");
		body.append("<table width='100%' cellpadding='10' border='0' bgColor='#f5f5f5'> <tr>");
		body.append("<td>Order ID</td> <td>"+fields.get(FieldType.ORDER_ID.getName())+"</td> </tr>");
		body.append("<tr> <td colspan='2'> ");
		if(StringUtils.isNotBlank(transactionDetail.getMopType())){
			body.append("<b>"+MopType.getmopName(transactionDetail.getMopType())+": INR "+totalAmount+" </b><hr></td> </tr>");
		}else{
			body.append("<b> NA: INR "+totalAmount+" </b><hr></td> </tr>");
		}
		body.append("<tr> <td>Date/Time</td> <td>"+date+" | "+createDate[1]+"</td> </tr>");
		if(StringUtils.isNotBlank(transactionDetail.getCardNumber()))
			body.append("<tr> <td>Card Number</td> <td>"+transactionDetail.getCardNumber()+"</td> </tr>");
		else
			body.append("<tr> <td>Card Number</td> <td>NA</td> </tr>");
		if(StringUtils.isNotBlank(transactionDetail.getPaymentMethods())){
			body.append("<tr> <td>Type</td> <td>"+PaymentType.getpaymentName(transactionDetail.getPaymentMethods())+"</td> </tr>");
		}else{
			body.append("<tr> <td>Type</td> <td>NA</td> </tr>");
		}
		body.append("<tr> <td>PG Ref Number</td> <td>"+transactionDetail.getPgRefNum()+"</td> </tr> ");
		body.append("<tr> <td>Status</td> <td>"+fields.get(FieldType.STATUS.getName())+"</td> </tr> </table> </td> </tr>");
		body.append("<tr><td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'>Please connect at <a href ='mailto:support@paymentgateway.com'>support@paymentgateway.com</a> for any information pertaining to the status of your payment.</td></tr>");
		body.append("<tr><td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'>You may also connect with <b>"+user.getBusinessName()+"</b> for further queries, if any, on status of your order.</td></tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'> Please do not reply to this e-mail. The address <a href='#'>paymentgateway.com</a> cannot process replies. </td> </tr>");
		body.append("</tbody> </table> </body> </html>");
		
		return body.toString();
	}
	

	public void sendTransactionEmailToMerchant(Fields fields,User user) throws Exception {

		if (StringUtils.isBlank(user.getEmailId()) && StringUtils.isBlank(user.getTransactionEmailId())) {
			logger.info("Merchant Email Not Found");
			return;
		}
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmailId()));
//			if(StringUtils.isNotBlank(user.getTransactionEmailId())){
//				message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(user.getTransactionEmailId()));
//			}
			message.setSubject("Transaction Successful | "+fields.get(FieldType.ORDER_ID.getName()).toString()+" | paymentgateway");
			message.setDataHandler(new DataHandler(new HTMLDataSource(merchantTransactionEmailBody(fields,user))));
			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect("smtp.office365.com", username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			logger.info("Email Sent to " + field.maskEmail(user.getEmailId()));
		} catch (Exception exception) {
			logger.error("Exception in sending email ", exception);
			
		}

	}
	
	public void sendEMandateEmailToUser(Map<String, String> response) throws Exception {

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(response.get(FieldType.CUST_EMAIL.getName())));
			if(response.get("responseCode").equals("0300")) {
				message.setSubject("eNACH Registration Successful | "+response.get(FieldType.MERCHANT_NAME.getName()));
			} else {
				message.setSubject("eNACH Registration Unsuccessful | "+response.get(FieldType.MERCHANT_NAME.getName()));
			}
			
			message.setDataHandler(new DataHandler(new HTMLDataSource(eMandateRegistrationEmailBody(response))));
			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect("smtp.office365.com", username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			logger.info("Email Sent to " + field.maskEmail(response.get(FieldType.CUST_EMAIL.getName())));
		} catch (Exception exception) {
			logger.error("Exception in sending email ", exception);
			
		}

	}
	
	public void sendTransactionFailedEmailToMerchant(Fields fields,User user) throws Exception {

		if (StringUtils.isBlank(user.getEmailId()) && StringUtils.isBlank(user.getTransactionEmailId())) {
			logger.info("Merchant Email Not Found");
			return;
		}
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmailId()));
			if(StringUtils.isNotBlank(user.getTransactionEmailId())){
				message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(user.getTransactionEmailId()));
			}
			message.setSubject("Transaction Not Successful | "+fields.get(FieldType.ORDER_ID.getName()).toString()+" | paymentgateway");
			message.setDataHandler(new DataHandler(new HTMLDataSource(merchantTransactionEmailBody(fields,user))));
			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect("smtp.office365.com", username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			logger.info("Email Sent to " + field.maskEmail(user.getEmailId()));
		} catch (Exception exception) {
			logger.error("Exception in sending email ", exception);
			
		}

	}
	
	
	
	public String merchantTransactionEmailBody(Fields fields,User user) {
		
		String logoUrl = propertiesManager.propertiesMap.get("logoForEmail");
		String totalAmount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),fields.get(FieldType.CURRENCY_CODE.getName()));
		
		String[] createDate=fields.get(FieldType.RESPONSE_DATE_TIME.getName()).split(" ");
		String date=changeDateFormat(createDate[0]);
		StringBuilder body = new StringBuilder();
		String status= fields.get(FieldType.STATUS.getName());

		
		
		body.append("<!DOCTYPE html>");
		body.append("<html lang='en'> <head> <meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
		body.append("<title>Transaction Email</title>");
		body.append("<style> body{ font-family: Arial, Helvetica, sans-serif; } </style>");
		body.append("</head>");
		body.append("<body bgcolor='#ccc'>");
		body.append("<table width='700' border='0' border-collapse='collapse' cellpadding='0' cellspacing='0' bgcolor='#fff' align='center'>");
		body.append("<tbody> <tr>");
		body.append("<td width='70%' bgcolor='#002664' style='padding: 10px;color: #fff;font-size: 24px;'>E-mail notification</td>");
		body.append("<td width='30%' style='padding: 10px;' align='right'> <img src="+logoUrl+" alt='' width='150'> </td>");
		body.append("</tr>");
//		body.append("<tr> <td colspan='3' style='padding: 10px;padding-bottom: 0; padding-top: 20px;'>");
//		body.append("<span>SHOP : <b>"+user.getBusinessName()+"</b></span>");
//		body.append("<br> <span style='display: block;margin-top: 8px;width:100%'>URL Address : <a href='https://www.paymentgateway.com'>paymentgateway.com</a></span> </td> </tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px;'>");
		body.append("<h2 style='margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;'>");
		body.append("Dear Partner,<br><br>");
		if(status.equals(StatusType.CAPTURED.getName())){
			body.append("Transaction with "+fields.get(FieldType.ORDER_ID.getName())+" is successful at paymentgateway.com.</h2> </td> </tr>");
		}else{
			body.append("Transaction with "+fields.get(FieldType.ORDER_ID.getName())+" couldn't get through at paymentgateway.com.</h2> </td> </tr>");
		}
		
		body.append("<tr> <td colspan='3'> <h2 style='margin: 0;padding-left: 10px;'>Payment Details</h2> </td> </tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px'>");
		body.append("<table width='100%' cellpadding='10' border='0' bgColor='#f5f5f5'> <tr>");
		body.append("<td>Order ID</td> <td>"+fields.get(FieldType.ORDER_ID.getName())+"</td> </tr>");
//		body.append("<tr> <td colspan='2'> <b>"+MopType.getmopName(transactionDetail.getMopType())+": INR "+totalAmount+" </b><hr></td> </tr>");
		body.append("<tr> <td>Date/Time</td> <td>"+date+" | "+createDate[1]+"</td> </tr>");
		body.append("<tr> <td>Amount</td> <td>"+totalAmount+"</td> </tr> </table> </td> </tr>");
//		if(StringUtils.isNotBlank(transactionDetail.getCardNumber()))
//			body.append("<tr> <td>Card Number</td> <td>"+transactionDetail.getCardNumber()+"</td> </tr>");
//		else
//			body.append("<tr> <td>Card Number</td> <td>NA</td> </tr>");
//		body.append("<tr> <td>Type</td> <td>"+PaymentType.getpaymentName(transactionDetail.getPaymentMethods())+"</td> </tr>");
//		body.append("<tr> <td>PG Ref Number</td> <td>"+transactionDetail.getPgRefNum()+"</td> </tr> ");
		body.append("<tr><td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'>Please connect with us at <a href = \"mailto:support@paymentgateway.com\">support@paymentgateway.com</a> for any further information pertaining to the transaction.</td></tr>");
//		body.append("<tr><td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'>You may also connect with <b>"+user.getBusinessName()+"</b> for further queries, if any, on status of your order.</td></tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'> Please do not reply to this e-mail. The address <a href='#'>paymentgateway.com</a> cannot process replies. </td> </tr>");
		body.append("</tbody> </table> </body> </html>");
		return body.toString();
	}
	
	public String eMandateRegistrationEmailBody(Map<String, String> response) {
		
		String logoUrl = response.get("LOGO");
		String amount = response.get(FieldType.AMOUNT.getName());
		String maxAmount = response.get(FieldType.MAX_AMOUNT.getName());
		String totalAmount = response.get(FieldType.TOTAL_AMOUNT.getName());
		
		String logopaymentgatewayUrl = propertiesManager.propertiesMap.get("logoForEmail");
		String[] createDate = response.get(FieldType.TXN_DATE.getName()).split(" ");
		String date=changeDateFormat(createDate[0]);
		StringBuilder body = new StringBuilder();
		String responseCode = response.get("responseCode");

		body.append("<!DOCTYPE html>");
		body.append("<html lang='en'> <head> <meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
		body.append("<title>E-Mandate Registration Email</title>");
		body.append("<style> body{ font-family: Arial, Helvetica, sans-serif; } </style>");
		body.append("</head>");
		body.append("<body bgcolor='#ccc'>");
		body.append("<table width='700' border='0' border-collapse='collapse' cellpadding='0' cellspacing='0' bgcolor='#fff' align='center'>");
		body.append("<tbody> <tr>");
		body.append("<td width='80%' hight='10%' bgcolor='#002664' style='padding: 10px;color: #fff;font-size: 24px;'>E-mail notification</td>");
		body.append("<td width='50%' style='padding: 10px;' align='right'> <img src="+logoUrl+" alt='' width='150'> </td>");
		body.append("</tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px;'>");
		body.append("<h2 style='margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;'>");
		
		if(responseCode.equals("0300")) {
			body.append("Dear Customer, your eNACH Registration has been successfully completed, at "+response.get(FieldType.MERCHANT_NAME.getName())+"<br><br>");
		} else if(responseCode.equals("0398")) {
			body.append("Dear Customer, your eNACH Registration under process at "+response.get(FieldType.MERCHANT_NAME.getName())+"<br><br>");
		} else {
			body.append("Dear Customer, we couldn't process your eNACH Registration at "+response.get(FieldType.MERCHANT_NAME.getName())+"<br><br>");
		}
		
		body.append("<tr> <td colspan='3'> <h2 style='margin: 0;padding-left: 10px;'>Registration Details</h2> </td> </tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px'>");
		body.append("<table width='100%' cellpadding='10' border='0' bgColor='#f5f5f5'> <tr>");
		body.append("<tr> <td>Registration Mode</td> <td>"+response.get(FieldType.PAYMENT_TYPE.toString())+"</td> </tr>");
		body.append("<td>Transaction ID</td> <td>"+response.get(FieldType.TXN_ID.getName())+"</td> </tr>");
		body.append("<tr> <td>UMRN Number</td> <td>"+response.get(FieldType.UMRN_NUMBER.getName())+"</td> </tr>");
		body.append("<tr> <td>Date/Time</td> <td>"+date+" | "+createDate[1]+"</td> </tr>");
		body.append("<tr> <td>Registration Amount</td> <td>"+amount+"</td> </tr>");
		body.append("<tr> <td>Debit Amount</td> <td>"+maxAmount+"</td> </tr>");
		body.append("<tr> <td>Total Amount</td> <td>"+totalAmount+"</td> </tr>");
		body.append("<tr> <td>Status</td> <td>"+response.get(FieldType.STATUS.getName())+"</td> </tr>");
		body.append("<tr> <td>Account Number</td> <td>"+response.get(FieldType.ACCOUNT_NO.toString())+"</td> </tr>");
		body.append("<tr> <td>Account Type</td> <td>"+response.get(FieldType.ACCOUNT_TYPE.getName())+"</td> </tr>");
		body.append("<tr> <td>Account Holder Name</td> <td>"+response.get(FieldType.ACCOUNT_HOLDER_NAME.getName())+"</td> </tr>");
		body.append("<tr> <td>Bank Name</td> <td>"+response.get(FieldType.BANK_NAME.getName())+"</td> </tr>");
		body.append("<tr> <td>Mobile Number</td> <td>"+response.get(FieldType.CUST_PHONE.getName())+"</td> </tr>");
		body.append("<tr> <td>Email ID</td> <td>"+response.get(FieldType.CUST_EMAIL.getName())+"</td> </tr>");
		body.append("<tr> <td>Frequency</td> <td>"+response.get(FieldType.FREQUENCY.getName())+"</td> </tr>");
		body.append("<tr> <td>Tenure</td> <td>"+response.get(FieldType.TENURE.getName())+"</td> </tr>");
		body.append("<tr> <td>Debit Start Date</td> <td>"+response.get(FieldType.DATEFROM.getName())+"</td> </tr>");
		body.append("<tr> <td>Debit End Date</td> <td>"+response.get(FieldType.DATETO.getName())+"</td> </tr> </table> </td> </tr>");
		body.append("<tr><td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0; text-align: right'>Powered by <img src="+logopaymentgatewayUrl+" alt='' width='80'></td></tr>");
		body.append("</tbody> </table> </body> </html>");
		return body.toString();
	}
	
	public void sendRefundTransactionEmailToMerchant(Fields fields,User user) throws Exception {

		if (StringUtils.isBlank(user.getEmailId()) && StringUtils.isBlank(user.getTransactionEmailId())) {
			logger.info("Merchant Email Not Found");
			return;
		}
		try {
			
			TransactionSearch transactionDetail=transactionDetailsService.getSaleCaptureTransactionByOrderId(fields.get(FieldType.ORDER_ID.getName()));
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmailId()));
//			if(StringUtils.isNotBlank(user.getTransactionEmailId())){
//				message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(user.getTransactionEmailId()));
//			}
			message.setSubject("Refund Successful | "+fields.get(FieldType.ORDER_ID.getName()).toString()+" | paymentgateway");
			message.setDataHandler(new DataHandler(new HTMLDataSource(merchantRefundTransactionEmailBody(fields,user,transactionDetail.gettDate()))));
			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect("smtp.office365.com", username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			logger.info("Email Sent to " + field.maskEmail(user.getEmailId()));
		} catch (Exception exception) {
			logger.error("Exception in sending email ", exception);
			
		}

	}
	
	
	
	public String merchantRefundTransactionEmailBody(Fields fields,User user,String orderCaptureDate) {
		
		String logoUrl = propertiesManager.propertiesMap.get("logoForEmail");
		String totalAmount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),fields.get(FieldType.CURRENCY_CODE.getName()));
		
		String[] captureDate=orderCaptureDate.split(" ");
		String saleCaptureTxnDate=changeDateFormat(captureDate[0]);
		
		String[] createDate=fields.get(FieldType.RESPONSE_DATE_TIME.getName()).split(" ");
		String date=changeDateFormat(createDate[0]);
		StringBuilder body = new StringBuilder();
		String status= fields.get(FieldType.STATUS.getName());
		
		
		body.append("<!DOCTYPE html>");
		body.append("<html lang='en'> <head> <meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
		body.append("<title>Refund Transaction Email</title>");
		body.append("<style> body{ font-family: Arial, Helvetica, sans-serif; } </style>");
		body.append("</head>");
		body.append("<body bgcolor='#ccc'>");
		body.append("<table width='700' border='0' border-collapse='collapse' cellpadding='0' cellspacing='0' bgcolor='#fff' align='center'>");
		body.append("<tbody> <tr>");
		body.append("<td width='70%' bgcolor='#002664' style='padding: 10px;color: #fff;font-size: 24px;'>E-mail notification</td>");
		body.append("<td width='30%' style='padding: 10px;' align='right'> <img src="+logoUrl+" alt='' width='150'> </td>");
		body.append("</tr>");
//		body.append("<tr> <td colspan='3' style='padding: 10px;padding-bottom: 0; padding-top: 20px;'>");
//		body.append("<span>SHOP : <b>"+user.getBusinessName()+"</b></span>");
//		body.append("<br> <span style='display: block;margin-top: 8px;width:100%'>URL Address : <a href='https://www.paymentgateway.com'>paymentgateway.com</a></span> </td> </tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px;'>");
		body.append("<h2 style='margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;'>");
		body.append("Dear Partner,<br><br>");
		
		body.append("Refund transaction against Order ID "+fields.get(FieldType.ORDER_ID.getName())+" is successful at <a href='http://www.paymentgateway.com'>paymentgateway.com</a>.</h2> </td> </tr>");
		body.append("<tr> <td colspan='3'> <h2 style='margin: 0;padding-left: 10px;'>Payment Details</h2> </td> </tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px'>");
		body.append("<table width='100%' cellpadding='10' border='0' bgColor='#f5f5f5'>");
		body.append("<tr><td>Order ID</td> <td>"+fields.get(FieldType.ORDER_ID.getName())+"</td> </tr>");
		body.append("<tr><td>Amount</td> <td>INR "+totalAmount+"</td> </tr>");
		body.append("<tr> <td>Order Date/Time</td> <td>"+saleCaptureTxnDate+" | "+captureDate[1]+"</td> </tr>");
		body.append("<tr> <td>Refund Date/Time</td> <td>"+date+" | "+createDate[1]+"</td> </tr> </table> </td> </tr>");
		body.append("<tr><td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'>Please connect with us at <a href ='mailto:support@paymentgateway.com'>support@paymentgateway.com</a> for any further information pertaining to the transaction.</td></tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'> Please do not reply to this e-mail. The address <a href='#'>paymentgateway.com</a> cannot process replies. </td> </tr>");
		body.append("</tbody> </table> </body> </html>");

		return body.toString();
	}
	
	public void sendRefundTransactionEmailToCustomer(Fields fields,User user) throws Exception {

		try {
		
			TransactionSearch transactionDetail=transactionDetailsService.getSaleCaptureTransactionByOrderId(fields.get(FieldType.ORDER_ID.getName()));
			
		if (StringUtils.isBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			logger.info("Customer Email Not Found");
			return;
			
		}
		
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(fields.get(FieldType.CUST_EMAIL.getName())));
			message.setSubject("Refund Successful | "+fields.get(FieldType.ORDER_ID.getName()).toString()+" | paymentgateway");
			message.setDataHandler(new DataHandler(new HTMLDataSource(customerRefundTransactionEmailBody(fields,user,transactionDetail))));
			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect("smtp.office365.com", username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			logger.info("Email Sent to " + field.maskEmail(fields.get(FieldType.CUST_EMAIL.getName())));
			
		} catch (Exception exception) {
			logger.error("Exception in sending email ", exception);
			
		}

	}
	
	public String customerRefundTransactionEmailBody(Fields fields,User user,TransactionSearch transactionDetail) {
		
		

		String logoUrl = propertiesManager.propertiesMap.get("logoForEmail");
		String totalAmount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),fields.get(FieldType.CURRENCY_CODE.getName()));
		
		String[] captureDate=transactionDetail.gettDate().split(" ");
		String saleCaptureTxnDate=changeDateFormat(captureDate[0]);
		
		String[] createDate=fields.get(FieldType.RESPONSE_DATE_TIME.getName()).split(" ");
		String date=changeDateFormat(createDate[0]);
		StringBuilder body = new StringBuilder();
		
		body.append("<!DOCTYPE html>");
		body.append("<html lang='en'> <head> <meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
		body.append("<title>Transaction Email</title>");
		body.append("<style> body{ font-family: Arial, Helvetica, sans-serif; } </style>");
		body.append("</head>");
		body.append("<body bgcolor='#ccc'>");
		body.append("<table width='700' border='0' border-collapse='collapse' cellpadding='0' cellspacing='0' bgcolor='#fff' align='center'>");
		body.append("<tbody> <tr>");
		body.append("<td width='70%' bgcolor='#002664' style='padding: 10px;color: #fff;font-size: 24px;'>E-mail notification</td>");
		body.append("<td width='30%' style='padding: 10px;' align='right'> <img src="+logoUrl+" alt='' width='150'> </td>");
		body.append("</tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px;padding-bottom: 0; padding-top: 20px;'>");
		body.append("<span>SHOP : <b>"+user.getBusinessName()+"</b></span>");
		body.append("<br> <span style='display: block;margin-top: 8px;width:100%'>URL Address : <a href='https://www.paymentgateway.com'>paymentgateway.com</a></span> </td> </tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px;'>");
		body.append("<h2 style='margin: 0;font-size: 16px;font-weight: 400;color: #333;margin-bottom: 10px;line-height: 24px;margin-top: 10px;'>");
		if(StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))){
			body.append("Hello "+fields.get(FieldType.CUST_NAME.getName())+", <br><br>");
		}else{
			body.append("Dear Customer");
		}
		
		body.append("Your refund of INR "+totalAmount+" with "+user.getBusinessName()+" has been successfully processed. The amount will be credited in 3-5 working days.</h2> </td> </tr>");
		body.append("<tr> <td colspan='3'> <h2 style='margin: 0;padding-left: 10px;'>Payment Details</h2> </td> </tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px'>");
		body.append("<table width='100%' cellpadding='10' border='0' bgColor='#f5f5f5'> <tr>");
		body.append("<td>Order ID</td> <td>"+fields.get(FieldType.ORDER_ID.getName())+"</td> </tr>");
		body.append("<td>Amount</td> <td>INR "+totalAmount+"</td> </tr>");
//		body.append("<tr> <td colspan='2'> <b>"+MopType.getmopName(transactionDetail.getMopType())+": INR "+totalAmount+" </b><hr></td> </tr>");
		body.append("<tr> <td>Order Date/Time</td> <td>"+saleCaptureTxnDate+" | "+captureDate[1]+"</td> </tr>");
		body.append("<tr> <td>Refund Date/Time</td> <td>"+date+" | "+createDate[1]+"</td> </tr>");
		if(StringUtils.isNotBlank(transactionDetail.getCardNumber()))
			body.append("<tr> <td>Card Number</td> <td>"+transactionDetail.getCardNumber()+"</td> </tr>");
		else
			body.append("<tr> <td>Card Number</td> <td>NA</td> </tr>");
		body.append("<tr> <td>Mop Type</td> <td>"+MopType.getmopName(transactionDetail.getMopType())+"</td> </tr>");
		body.append("<tr> <td>Type</td> <td>"+PaymentType.getpaymentName(transactionDetail.getPaymentMethods())+"</td> </tr> </table> </td> </tr>");
//		body.append("<tr> <td>PG Ref Number</td> <td>"+transactionDetail.getPgRefNum()+"</td> </tr> </table> </td> </tr>");
		body.append("<tr><td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'>Please connect at <a href ='mailto:support@paymentgateway.com'>support@paymentgateway.com</a> for any information pertaining to the status of your payment refund.</td></tr>");
		body.append("<tr><td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'>You may also connect with <b>"+user.getBusinessName()+"</b> for further queries, if any, on status of your order refund.</td></tr>");
		body.append("<tr> <td colspan='3' style='padding: 10px;font-size: 14px;color: #a0a0a0'> Please do not reply to this e-mail. The address <a href='#'>paymentgateway.com</a> cannot process replies. </td> </tr>");
		body.append("</tbody> </table> </body> </html>");
		
		return body.toString();
	}
	
	public boolean beneVerificationLinkEmail(String body, String subject, String mailTo, String mailBcc,
			boolean emailExceptionHandlerFlag) throws Exception {

		if (StringUtils.isBlank(mailTo) && StringUtils.isBlank(mailBcc)) {
			return false;
		}
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
			message.setSubject(subject);
			message.setDataHandler(new DataHandler(new HTMLDataSource(body)));
			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect("smtp.office365.com", username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			logger.info("Email Sent to " + field.maskEmail(mailTo));
			return true;
		} catch (Exception exception) {
			logger.error("Exception in sending email ", exception);
			return false;
		}

	}
	
	private String changeDateFormat(String date) {
			Date strDate = null;
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");  
			try {
				strDate = formatter.parse(date);
			} catch (ParseException e) {
				logger.error("excption in changing date format " , e);
			} 
		    
		    return sdf.format(strDate); 
	}
}