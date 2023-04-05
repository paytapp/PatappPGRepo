package com.paymentgateway.commons.util;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.TransactionDetailsService;
import com.paymentgateway.commons.user.EPOSTransaction;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.sun.mail.smtp.SMTPTransport;

/**
 * @author Pooja Pancholi
 */
@Service
public class AWSSESEmailService {
	private static Logger logger = LoggerFactory.getLogger(AWSSESEmailService.class.getName());
	
	@Autowired
	private Fields field;
	
	@Autowired
	private CrmEmailer crmEmailer; 
	
	@Autowired
	private TransactionDetailsService transactionDetailsService;
	
	@Autowired
	@Qualifier("propertiesManager")
	private PropertiesManager propertiesManager;
	
	final String SMTP_USERNAME = PropertiesManager.propertiesMap.get(Constants.SMTP_USERNAME.getValue());
	final String SMTP_PASSWORD = PropertiesManager.propertiesMap.get(Constants.SMTP_PASSWORD.getValue());
	final String CONFIGSET = PropertiesManager.propertiesMap.get(Constants.AWS_CONFIGSET.getValue());
	final String HOST = PropertiesManager.propertiesMap.get(Constants.AWS_HOST.getValue());
	final String PORT = PropertiesManager.propertiesMap.get(Constants.AWS_PORT.getValue());

	  
	  final String mailSender = propertiesManager.propertiesMap.get("MailSender");
		final String username = propertiesManager.propertiesMap.get("MailUserName");
	 

	public String sendEmail(String body, String subject, String mailTo, String mailBcc) {
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);
		Transport transport = null;
		String eMailResponse = null;
		try {
			// Create a message with the specified information.
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			msg.setSubject(subject);
			msg.setDataHandler(new DataHandler(new HTMLDataSource(body)));
			// Create a transport.
			transport = session.getTransport();
			logger.info("Sending...");

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			eMailResponse = "Email Sent to "+ field.maskEmail(mailTo);
			logger.info(eMailResponse);
		} catch (Exception e) {
			logger.error("The email was not sent.");
			logger.error("Error message: ", e);
		} finally {
			// Close and terminate the connection.
			try {
				transport.close();
			} catch (MessagingException e) {
				logger.error("Exception cought in : ", e);
			}
		}
		return eMailResponse;
	}
	
	public boolean sendEmailThroughApi(String body, String subject, String mailTo, String mailBcc) {
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);
		Transport transport = null;
		String eMailResponse = null;
		try {
			// Create a message with the specified information.
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			msg.setSubject(subject);
			msg.setDataHandler(new DataHandler(new HTMLDataSource(body)));
			// Create a transport.
			transport = session.getTransport();
			logger.info("Sending mail via API...");

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			eMailResponse = field.maskEmail(mailTo);
			logger.info(eMailResponse);
			return true;
		} catch (Exception e) {
			logger.error("The email was not sent.");
			logger.error("Error message: ", e);
			return false;
		} finally {
			// Close and terminate the connection.
			try {
				transport.close();
			} catch (MessagingException e) {
				logger.error("Exception cought in : ", e);
			}
		}
	}

	public void sendEmailWithAttch(String body, String subject, String mailTo, String mailBcc, File file) {
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);
		Transport transport = null;
		try {
			// Create a message with the specified information.
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username));
			
			if (!StringUtils.isBlank(mailTo) && !StringUtils.isBlank(mailBcc)) {
				
				msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
				msg.setRecipient(Message.RecipientType.BCC, new InternetAddress(mailBcc));
			} else if (!StringUtils.isBlank(mailTo)) {
				msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			} else {
				msg.setRecipient(Message.RecipientType.BCC, new InternetAddress(mailBcc));
			}
			msg.setSubject(subject);
			// msg.setContent(body,"text/html");

			// Create a multipart/alternative child container.
			MimeMultipart msg_body = new MimeMultipart("alternative");

			// Create a wrapper for the HTML and text parts.
			MimeBodyPart wrap = new MimeBodyPart();

			// Define the HTML part.
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(body, "text/html; charset=UTF-8");

			// Add the text and HTML parts to the child container.
			// msg_body.addBodyPart(textPart);
			msg_body.addBodyPart(htmlPart);

			// Add the child container to the wrapper object.
			wrap.setContent(msg_body);

			// Create a multipart/mixed parent container.
			MimeMultipart msga = new MimeMultipart("mixed");

			// Add the parent container to the message.
			msg.setContent(msga);

			// Add the multipart/alternative part to the message.
			msga.addBodyPart(wrap);

			// Define the attachment
			MimeBodyPart att = new MimeBodyPart();
			DataSource fds = new FileDataSource(file);
			att.setDataHandler(new DataHandler(fds));
			att.setFileName(fds.getName());

			// Add the attachment to the message.
			msga.addBodyPart(att);

			// Create a transport.
			transport = session.getTransport();
			logger.info("Sending...");

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			logger.info("Email sent!");
		} catch (Exception e) {
			logger.error("The email was not sent.");
			logger.error("Exception cought in : ", e);
		} finally {
			// Close and terminate the connection.
			try {
				transport.close();
			} catch (MessagingException e) {
				logger.error("Exception cought in : ", e);
			}
		}

	}
	public boolean invoiceEmail(String body, String subject, String mailTo, String mailBcc,
			boolean emailExceptionHandlerFlag) throws Exception {

		if (StringUtils.isBlank(mailTo) && StringUtils.isBlank(mailBcc)) {
			return false;
		}
		
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);
		Transport transport = null;
		String eMailResponse = null;
		try {
			// Create a message with the specified information.
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			msg.setSubject(subject);
			msg.setDataHandler(new DataHandler(new HTMLDataSource(body)));
			// Create a transport.
			transport = session.getTransport();
			logger.info("Sending...");

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			logger.info("Email Sent to " + field.maskEmail(mailTo));
			return true;
		} catch (Exception e) {
			logger.error("The email was not sent.");
			logger.error("Exception cought in : ", e);
			return false;
		} finally {
			// Close and terminate the connection.
			try {
				transport.close();
			} catch (MessagingException e) {
				logger.error("Exception cought in : ", e);
				return false;
			}
		}

	}

	
	public void invoiceEmailWithAttachment(String body, String subject, String mailTo, String mailBcc, File file,
			boolean emailExceptionHandlerFlag) throws Exception {

		if (StringUtils.isBlank(mailTo) && StringUtils.isBlank(mailBcc)) {
			return;
		}
		
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);
		Transport transport = null;
		try {
			// Create a message with the specified information.
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			msg.setSubject(subject);
			// msg.setContent(body,"text/html");

			// Create a multipart/alternative child container.
			MimeMultipart msg_body = new MimeMultipart("alternative");

			// Create a wrapper for the HTML and text parts.
			MimeBodyPart wrap = new MimeBodyPart();

			// Define the HTML part.
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(body, "text/html; charset=UTF-8");

			// Add the text and HTML parts to the child container.
			// msg_body.addBodyPart(textPart);
			msg_body.addBodyPart(htmlPart);

			// Add the child container to the wrapper object.
			wrap.setContent(msg_body);

			// Create a multipart/mixed parent container.
			MimeMultipart msga = new MimeMultipart("mixed");

			// Add the parent container to the message.
			msg.setContent(msga);

			// Add the multipart/alternative part to the message.
			msga.addBodyPart(wrap);

			// Define the attachment
			MimeBodyPart att = new MimeBodyPart();
			DataSource fds = new FileDataSource(file);
			att.setDataHandler(new DataHandler(fds));
			att.setFileName(fds.getName());

			// Add the attachment to the message.
			msga.addBodyPart(att);

			// Create a transport.
			transport = session.getTransport();
			logger.info("Sending...");

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			logger.info("Email sent!");
		} catch (Exception e) {
			logger.error("The email was not sent.");
			logger.error("Exception cought in : ", e);
		} finally {
			// Close and terminate the connection.
			try {
				transport.close();
			} catch (MessagingException e) {
				logger.error("Exception cought in : ", e);
			}
		}
	}
	
	public void sendEmailWithAttachment(String subject, String body, String attachmentFileName, byte[] bytes,
			String mailTo, String mailFrom, String filenameexcel, byte[] bytesexcel) {

		InternetAddress[] toAddresses = null;
		
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);
		Transport transport = null;
		try {
			// Create a message with the specified information.
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			msg.setSubject(subject);
			// msg.setContent(body,"text/html");

			// Create a multipart/alternative child container.
			MimeMultipart msg_body = new MimeMultipart("alternative");

			// Create a wrapper for the HTML and text parts.
			MimeBodyPart wrap = new MimeBodyPart();

			// Define the HTML part.
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(body, "text/html; charset=UTF-8");

			// Add the text and HTML parts to the child container.
			// msg_body.addBodyPart(textPart);
			msg_body.addBodyPart(htmlPart);

			// Add the child container to the wrapper object.
			wrap.setContent(msg_body);

			// Create a multipart/mixed parent container.
			MimeMultipart msga = new MimeMultipart("mixed");

			// Add the parent container to the message.
			msg.setContent(msga);

			// Add the multipart/alternative part to the message.
			msga.addBodyPart(wrap);

			// Define the attachment
			MimeBodyPart att = new MimeBodyPart();
			DataSource fds = new ByteArrayDataSource(bytes, "application/pdf");
			att.setDataHandler(new DataHandler(fds));
			att.setFileName(attachmentFileName);

			// Add the attachment to the message.
			msga.addBodyPart(att);
			
			MimeBodyPart att1 = new MimeBodyPart();
			DataSource source = new ByteArrayDataSource(bytesexcel,"application/xlsx");
			att1.setDataHandler(new DataHandler(source));
			att1.setFileName(filenameexcel);
			

			// Add the attachment to the message.
			msga.addBodyPart(att1);

			// Create a transport.
			transport = session.getTransport();
			logger.info("Sending...");

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			logger.info("Email sent!");
		} catch (Exception e) {
			logger.error("The email was not sent.");
			logger.error("Exception cought in : ", e);
		} finally {
			// Close and terminate the connection.
			try {
				transport.close();
			} catch (MessagingException e) {
				logger.error("Exception cought in : ", e);
			}
		}
	}
	
	public void sendTransactionEmailToMerchant(Fields fields, User user) throws Exception {

		if (StringUtils.isBlank(user.getEmailId()) && StringUtils.isBlank(user.getTransactionEmailId())) {
			logger.info("Merchant Email Not Found");
			return;
		}
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);
		Transport transport = null;
		String eMailResponse = null;
		try {
			// Create a message with the specified information.
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmailId()));
			msg.setSubject("Transaction Successful | " + fields.get(FieldType.ORDER_ID.getName()).toString() + " | Payment GateWay");
			msg.setDataHandler(new DataHandler(new HTMLDataSource(crmEmailer.merchantTransactionEmailBody(fields, user))));
			//msg.setContent(body, "text/html");
			// Create a transport.
			transport = session.getTransport();
			logger.info("Sending...");

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			eMailResponse = "Email Sent to "+ field.maskEmail(user.getEmailId());
			logger.info(eMailResponse);
		
		} catch (Exception exception) {
			logger.error("Exception in sending email ", exception);

		}finally {
			// Close and terminate the connection.
			try {
				transport.close();
			} catch (MessagingException e) {
				logger.error("Exception cought in : ", e);
			}
		}

	}
	
	public void sendTransactionEmailToCustomer(Fields fields, User user) throws Exception {

			if (StringUtils.isBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
				logger.info("Customer Email Not Found");
				return;

			}
			
			Properties props = System.getProperties();
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.port", PORT);
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.auth", "true");

			// Create a Session object to represent a mail session with the specified
			// properties.
			Session session = Session.getDefaultInstance(props);
			Transport transport = null;
			String eMailResponse = null;
			try {
				// Create a message with the specified information.
				MimeMessage msg = new MimeMessage(session);
				msg.setFrom(new InternetAddress(username));
				msg.setRecipient(Message.RecipientType.TO, new InternetAddress(fields.get(FieldType.CUST_EMAIL.getName())));
				msg.setSubject("Your Transaction is Successful | " + user.getBusinessName() + " | "
						+ fields.get(FieldType.ORDER_ID.getName()) + "");
				msg.setDataHandler(new DataHandler(new HTMLDataSource(crmEmailer.customerTransactionEmailBody(fields, user))));
				//msg.setContent(body, "text/html");
				// Create a transport.
				transport = session.getTransport();
				logger.info("Sending...");

				// Connect to Amazon SES using the SMTP username and password you specified
				// above.
				transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

				// Send the email.
				transport.sendMessage(msg, msg.getAllRecipients());
				eMailResponse = "Email Sent to "+ field.maskEmail(fields.get(FieldType.CUST_EMAIL.getName()));
				logger.info(eMailResponse);
			
			}catch (Exception exception) {
			logger.error("Exception in sending email ", exception);

		}finally {
			// Close and terminate the connection.
			try {
				transport.close();
			} catch (MessagingException e) {
				logger.error("Exception cought in : ", e);
			}
		}

	}
	
	public void sendTransactionFailedEmailToCustomer(Fields fields, User user) throws Exception {
		
			if (StringUtils.isBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
				logger.info("Customer Email Not Found");
				return;

			}Properties props = System.getProperties();
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.port", PORT);
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.auth", "true");

			// Create a Session object to represent a mail session with the specified
			// properties.
			Session session = Session.getDefaultInstance(props);
			Transport transport = null;
			String eMailResponse = null;
			try {
				// Create a message with the specified information.
				MimeMessage msg = new MimeMessage(session);
				msg.setFrom(new InternetAddress(username));
				msg.setRecipient(Message.RecipientType.TO, new InternetAddress(fields.get(FieldType.CUST_EMAIL.getName())));
				msg.setSubject("OOPS! Your transaction couldn't get through | " + user.getBusinessName() + " | "
						+ fields.get(FieldType.ORDER_ID.getName()) + "");
				msg.setDataHandler(new DataHandler(new HTMLDataSource(crmEmailer.customerTransactionFailedEmailBody(fields, user))));
				//msg.setContent(body, "text/html");
				// Create a transport.
				transport = session.getTransport();
				logger.info("Sending...");

				// Connect to Amazon SES using the SMTP username and password you specified
				// above.
				transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

				// Send the email.
				transport.sendMessage(msg, msg.getAllRecipients());
				eMailResponse = "Email Sent to "+ field.maskEmail(fields.get(FieldType.CUST_EMAIL.getName()));
				logger.info(eMailResponse);
			
			}catch (Exception exception) {
			logger.error("Exception in sending email ", exception);

		}finally {
			// Close and terminate the connection.
			try {
				transport.close();
			} catch (MessagingException e) {
				logger.error("Exception in sending email ", e);
			}
		}
       }
	
	public void sendEMandateEmailToUser(Map<String, String> response) throws Exception {
		
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);
		Transport transport = null;
		String eMailResponse = null;
		try {
			// Create a message with the specified information.
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(response.get(FieldType.CUST_EMAIL.getName())));
			if (response.get("responseCode").equals("0300")) {
				msg.setSubject(
						"eNACH Registration Successful | " + response.get(FieldType.MERCHANT_NAME.getName()));
			} else {
				msg.setSubject(
						"eNACH Registration Unsuccessful | " + response.get(FieldType.MERCHANT_NAME.getName()));
			}
			
			msg.setDataHandler(new DataHandler(new HTMLDataSource(crmEmailer.eMandateRegistrationEmailBody(response))));
			//msg.setContent(body, "text/html");
			// Create a transport.
			transport = session.getTransport();
			logger.info("Sending...");

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			eMailResponse = "Email Sent to "+ field.maskEmail(response.get(FieldType.CUST_EMAIL.getName()));
			logger.info(eMailResponse);
		
		}catch (Exception exception) {
			logger.error("Exception in sending email ", exception);

		}

	}

	public void sendTransactionFailedEmailToMerchant(Fields fields, User user) throws Exception {

		if (StringUtils.isBlank(user.getEmailId()) && StringUtils.isBlank(user.getTransactionEmailId())) {
			logger.info("Customer Email Not Found");
			return;

		}Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);
		Transport transport = null;
		String eMailResponse = null;
		try {
			// Create a message with the specified information.
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmailId()));
			if (StringUtils.isNotBlank(user.getTransactionEmailId())) {
				msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(user.getTransactionEmailId()));
			}
			msg.setSubject("Transaction Not Successful | " + fields.get(FieldType.ORDER_ID.getName()).toString()
					+ " | Payment GateWay");
			msg.setDataHandler(new DataHandler(new HTMLDataSource(crmEmailer.merchantTransactionEmailBody(fields, user))));
			//msg.setContent(body, "text/html");
			// Create a transport.
			transport = session.getTransport();
			logger.info("Sending...");

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			eMailResponse = "Email Sent to "+ field.maskEmail(fields.get(FieldType.CUST_EMAIL.getName()));
			logger.info(eMailResponse);
		
		}catch (Exception exception) {
		logger.error("Exception in sending email ", exception);

	}finally {
		// Close and terminate the connection.
		try {
			transport.close();
		} catch (MessagingException e) {
			logger.error("Exception in sending email ", e);
		}
	}
   }
	
	public void sendRefundTransactionEmailToMerchant(Fields fields, User user) throws Exception {

		if (StringUtils.isBlank(user.getEmailId()) && StringUtils.isBlank(user.getTransactionEmailId())) {
			logger.info("Merchant Email Not Found");
			return;
		}
		
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);
		Transport transport = null;
		String eMailResponse = null;
		try {
			// Create a message with the specified information.
			TransactionSearch transactionDetail = transactionDetailsService
					.getSaleCaptureTransactionByOrderId(fields.get(FieldType.ORDER_ID.getName()));
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmailId()));
			msg.setSubject("Refund Successful | " + fields.get(FieldType.ORDER_ID.getName()).toString() + " | Payment GateWay");
			msg.setDataHandler(new DataHandler(new HTMLDataSource(crmEmailer.merchantRefundTransactionEmailBody(fields, user, transactionDetail.gettDate()))));
			//msg.setContent(body, "text/html");
			// Create a transport.
			transport = session.getTransport();
			logger.info("Sending...");

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			eMailResponse = "Email Sent to "+ field.maskEmail(fields.get(FieldType.CUST_EMAIL.getName()));
			logger.info(eMailResponse);
		
		}catch (Exception exception) {
		logger.error("Exception in sending email ", exception);

	}finally {
		// Close and terminate the connection.
		try {
			transport.close();
		} catch (MessagingException e) {
			logger.error("Exception in sending email ", e);
		}
	}
	}

	public void sendRefundTransactionEmailToCustomer(Fields fields, User user) throws Exception {
		
			TransactionSearch transactionDetail = transactionDetailsService
					.getSaleCaptureTransactionByOrderId(fields.get(FieldType.ORDER_ID.getName()));

			if (StringUtils.isBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
				logger.info("Customer Email Not Found");
				return;

			}


			Properties props = System.getProperties();
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.port", PORT);
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.auth", "true");

			// Create a Session object to represent a mail session with the specified
			// properties.
			Session session = Session.getDefaultInstance(props);
			Transport transport = null;
			String eMailResponse = null;
			try {
				// Create a message with the specified information.
				MimeMessage msg = new MimeMessage(session);
				msg.setFrom(new InternetAddress(username));
				msg.setRecipient(Message.RecipientType.TO, new InternetAddress(fields.get(FieldType.CUST_EMAIL.getName())));
				msg.setSubject("Refund Successful | " + fields.get(FieldType.ORDER_ID.getName()).toString() + " | Payment GateWay");
				msg.setDataHandler(new DataHandler(new HTMLDataSource(crmEmailer.customerRefundTransactionEmailBody(fields, user, transactionDetail))));
				//msg.setContent(body, "text/html");
				// Create a transport.
				transport = session.getTransport();
				logger.info("Sending...");

				// Connect to Amazon SES using the SMTP username and password you specified
				// above.
				transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

				// Send the email.
				transport.sendMessage(msg, msg.getAllRecipients());
				eMailResponse = "Email Sent to "+ field.maskEmail(fields.get(FieldType.CUST_EMAIL.getName()));
				logger.info(eMailResponse);
			
			}catch (Exception exception) {
			logger.error("Exception in sending email ", exception);

		}finally {
			// Close and terminate the connection.
			try {
				transport.close();
			} catch (MessagingException e) {
				logger.error("Exception in sending email ", e);
			}
		}
      }
	
	public boolean beneVerificationLinkEmail(String body, String subject, String mailTo, String mailBcc,
			boolean emailExceptionHandlerFlag) throws Exception {

		if (StringUtils.isBlank(mailTo) && StringUtils.isBlank(mailBcc)) {
			return false;
		}
		
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);
		Transport transport = null;
		String eMailResponse = null;
		try {
			// Create a message with the specified information.
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			msg.setSubject(subject);
			msg.setDataHandler(new DataHandler(new HTMLDataSource(body)));
			//msg.setContent(body, "text/html");
			// Create a transport.
			transport = session.getTransport();
			logger.info("Sending...");

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			eMailResponse = "Email Sent to "+ field.maskEmail(mailTo);
			logger.info(eMailResponse);
			return true;
		
		}catch (Exception exception) {
		logger.error("Exception in sending email ", exception);
		return false;
	}finally {
		// Close and terminate the connection.
		try {
			transport.close();
		} catch (MessagingException e) {
			logger.error("Exception in sending email ", e);
		}
	 }
	}
	
	public void eposEmailWithAttachmentToCustomer(EPOSTransaction epos, String subject, String mailTo, boolean status,
			File file) throws Exception {
		logger.info("Sending success EPOS transaction Email to Customer with Email Id: " + mailTo);
		if (StringUtils.isBlank(mailTo)) {
			return;
		}
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);
		Transport transport = null;
		try {
			// Create a message with the specified information.
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			msg.setSubject(subject);
			// msg.setContent(body,"text/html");

			// Create a multipart/alternative child container.
			MimeMultipart msg_body = new MimeMultipart("alternative");

			// Create a wrapper for the HTML and text parts.
			MimeBodyPart wrap = new MimeBodyPart();

			// Define the HTML part.
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setDataHandler(new DataHandler(new HTMLDataSource(crmEmailer.getEposEmailBodyForCustomer(status, epos))));

			// Add the text and HTML parts to the child container.
			// msg_body.addBodyPart(textPart);
			msg_body.addBodyPart(htmlPart);

			// Add the child container to the wrapper object.
			wrap.setContent(msg_body);

			// Create a multipart/mixed parent container.
			MimeMultipart msga = new MimeMultipart("mixed");

			// Add the parent container to the message.
			msg.setContent(msga);

			// Add the multipart/alternative part to the message.
			msga.addBodyPart(wrap);

			// Define the attachment
			MimeBodyPart att = new MimeBodyPart();
			DataSource fds = new FileDataSource(file);
			att.setDataHandler(new DataHandler(fds));
			att.setFileName(file.getName());

			// Add the attachment to the message.
			msga.addBodyPart(att);

			// Create a transport.
			transport = session.getTransport();
			logger.info("Sending...");

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			logger.info("Email Sent to " + field.maskEmail(mailTo));
		} catch (Exception e) {
			logger.error("The email was not sent.");
			logger.error("Error message: ", e.getMessage());
		} finally {
			// Close and terminate the connection.
			try {
				transport.close();
			} catch (MessagingException e) {
				logger.error("Exception in sending email ", e);
			}
		}
	}
	
	public void eposEmailWithAttachmentToMerchant(EPOSTransaction epos, String subject, String mailTo, File file)
			throws Exception {
		logger.info("Sending success EPOS transaction Email to Merchant with Email Id: " + mailTo);
		if (StringUtils.isBlank(mailTo)) {
			return;
		}
		
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);
		Transport transport = null;
		try {
			// Create a message with the specified information.
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			msg.setSubject(subject);
			// msg.setContent(body,"text/html");

			// Create a multipart/alternative child container.
			MimeMultipart msg_body = new MimeMultipart("alternative");

			// Create a wrapper for the HTML and text parts.
			MimeBodyPart wrap = new MimeBodyPart();

			// Define the HTML part.
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setDataHandler(new DataHandler(new HTMLDataSource(crmEmailer.getEposEmailBodyForMerchant(epos))));

			// Add the text and HTML parts to the child container.
			// msg_body.addBodyPart(textPart);
			msg_body.addBodyPart(htmlPart);

			// Add the child container to the wrapper object.
			wrap.setContent(msg_body);

			// Create a multipart/mixed parent container.
			MimeMultipart msga = new MimeMultipart("mixed");

			// Add the parent container to the message.
			msg.setContent(msga);

			// Add the multipart/alternative part to the message.
			msga.addBodyPart(wrap);

			// Define the attachment
			MimeBodyPart att = new MimeBodyPart();
			DataSource fds = new FileDataSource(file);
			att.setDataHandler(new DataHandler(fds));
			att.setFileName(file.getName());

			// Add the attachment to the message.
			msga.addBodyPart(att);

			// Create a transport.
			transport = session.getTransport();
			logger.info("Sending...");

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			logger.info("Email Sent to " + field.maskEmail(mailTo));
		} catch (Exception e) {
			logger.error("The email was not sent.");
			logger.error("Error message: ", e.getMessage());
		} finally {
			// Close and terminate the connection.
			try {
				transport.close();
			} catch (MessagingException e) {
				logger.error("Exception in sending email ", e);
			}
		}
	}
	
	public void sendAttachmentFileEmail(String body, String subject, String mailTo, String mailBcc,
			boolean emailExceptionHandlerFlag) throws Exception {
		if (StringUtils.isBlank(mailTo) && StringUtils.isBlank(mailBcc)) {
			return;
		}
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(props);
		String emailactiveflag = propertiesManager.getEmailProperty("EmailActiveFlag");
		Transport transport = null;
		try {
			// Create a message with the specified information.
			if (emailactiveflag != "true") {
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(mailSender));
			
			if (!StringUtils.isBlank(mailTo) && !StringUtils.isBlank(mailBcc)) {
				
				msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
				msg.setRecipient(Message.RecipientType.BCC, new InternetAddress(mailBcc));
			} else if (!StringUtils.isBlank(mailTo)) {
				msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			} else {
				msg.setRecipient(Message.RecipientType.BCC, new InternetAddress(mailBcc));
			}
			msg.setSubject(subject);
			msg.setSentDate(new Date());
			// Create a multipart/mixed parent container.
			Multipart msga = new MimeMultipart();
			// Define the attachment
			MimeBodyPart att = new MimeBodyPart();
			DataSource fds = new FileDataSource(body);
			att.setDataHandler(new DataHandler(fds));
			att.setFileName(body);

			// Add the attachment to the message.
			msga.addBodyPart(att);
			msg.setContent(msga);

			// Create a transport.
			transport = session.getTransport();
			logger.info("Sending...");

			// Connect to Amazon SES using the SMTP username and password you specified
			// above.
			transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

			// Send the email.
			transport.sendMessage(msg, msg.getAllRecipients());
			logger.info("Email sent!");
			}else {
				logger.info("Emailing Feature Disabled : " + "Email Recipient :" + field.maskEmail(mailTo) + "Mail Subject :" + subject
						+ "Email Body :" + body);
			}
		} catch (Exception e) {
			logger.error("The email was not sent.");
			logger.error("Error message: ", e.getMessage());
		} finally {
			// Close and terminate the connection.
			try {
				transport.close();
			} catch (MessagingException e) {
				logger.error("Exception in sending email ", e);
			}
		}
		if (emailExceptionHandlerFlag) {
			// throw new SystemException(ErrorType.EMAIL_ERROR,
			// ErrorType.EMAIL_ERROR.getResponseMessage());
		}
	}
}
