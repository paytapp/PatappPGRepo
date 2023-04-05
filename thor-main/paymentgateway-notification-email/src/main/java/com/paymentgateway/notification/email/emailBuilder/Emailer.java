package com.paymentgateway.notification.email.emailBuilder;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
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
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.util.Base64EncodeDecode;
import com.paymentgateway.commons.util.CrmEmailer;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.HTMLDataSource;
import com.paymentgateway.commons.util.PropertiesManager;
import com.sun.mail.smtp.SMTPTransport;

/**
 * @author Neeraj
 *
 */
@Component
public class Emailer {

	private Logger logger = LoggerFactory.getLogger(Emailer.class.getName());

	@Autowired
	private Fields field;
	
	@Autowired
	@Qualifier("propertiesManager")
	private PropertiesManager propertiesManager;

	final String username = propertiesManager.propertiesMap.get("MailUserName");
	final String password = propertiesManager.propertiesMap.get("Password");
	
	Properties props = new Properties();
	{
		props.put("mail.smtp.auth", "true");	
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "outlook.office365.com");
		props.put("mail.smtp.port", "587");
	}

	Session session = Session.getInstance(props, new javax.mail.Authenticator() {
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username, password);
		}
	});

	public String sendEmail(String body, String subject, String mailTo, String mailBcc, boolean emailExceptionHandlerFlag)
			throws Exception {
		String eMailResponse;

		if (StringUtils.isBlank(mailTo) && StringUtils.isBlank(mailBcc)) {
			return null;
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
			eMailResponse = "Email Sent to "+ field.maskEmail(mailTo);
			logger.info(eMailResponse);
		} catch (Exception exception) {
			eMailResponse = "Exception in sending email " +exception;
			logger.error("Exception in sending email ", exception);
		}
		return eMailResponse;
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

	/*public void sendEmailWithAttachment(String recipients, String subject, String body,String attachmentFileName, HSSFWorkbook hssWorkBook, String  mailTo, String mailFrom) {
		 
		Properties properties = new Properties();
		 properties.put("mail.smtp.host", host);
		 properties.put("mail.smtp.port", port);
		 
		 props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.office365.com");
			props.put("mail.smtp.port", "587");
			InternetAddress[] toAddresses = null;
			InternetAddress[] bccAddresses = null;
		 
		 
		String mailSender = propertiesManager.getEmailProperty("MailSender");
		String username = propertiesManager.getEmailProperty("MailUserName");
		String password = propertiesManager.getEmailProperty("Password");
		 
		 
		 if (recipients.equals("") || subject.equals("") || body.equals("") || attachmentFileName.equals("") || hssWorkBook.getBytes().length < 0 ) {
		 System.out.println("Usage: sendEmailAttachemt() method parameter might be missing, you may check for the /test/local/surveyToExcel-test.properties for any recent changes");
		 System.exit(1);
		 }
		 
		 Session session = Session.getDefaultInstance(props, null);
		 //Session session = Session.getInstance(properties, null);
		 try {
		 // create a message
		 Message msg = new MimeMessage(session);
		 DataSource ds = null;
		 
		toAddresses = InternetAddress.parse(mailTo);
		//bccAddresses = InternetAddress.parse(mailBcc);
		msg.setRecipients(Message.RecipientType.TO, toAddresses);
		//msg.setRecipients(Message.RecipientType.BCC, bccAddresses);
		 
		 msg.setFrom(new InternetAddress(mailFrom));
		 
		// ArrayList recipientsArray = new ArrayList();
		// StringTokenizer stringTokenizer = new StringTokenizer(recipients, ",");
		 
		 //while (stringTokenizer.hasMoreTokens()) {
		 //recipientsArray.add(stringTokenizer.nextToken());
		 //}
		 //int sizeTo = recipientsArray.size();
		 //InternetAddress[] addressTo = new InternetAddress[sizeTo];
		 //for (int i = 0; i < sizeTo; i++) {
		 //addressTo[i] = new InternetAddress(recipientsArray.get(i).toString());
		 //}
		 //msg.setRecipients(Message.RecipientType.TO, addressTo);
		 
		 // Parse a comma-separated list of email addresses. Be strict.
//		            msg.setRecipients(Message.RecipientType.CC,
//		                    InternetAddress.parse(<a href="mailto:test@aa.test">test@aa.test</a>, true));
		 
		 msg.setSubject(subject);
		 
		 // create and fill the first message part
		 MimeBodyPart mimeBodyPart1 = new MimeBodyPart();
		 mimeBodyPart1.setText(body);
		 
		 // create the second message part
		 MimeBodyPart mimeBodyPart2 = new MimeBodyPart();
		 
		 ByteArrayOutputStream baos = new ByteArrayOutputStream();
		 try{
		 hssWorkBook.write(baos);
		 byte[] bytes = baos.toByteArray();
		 ds = new ByteArrayDataSource(bytes, "application/excel");
		 }catch (IOException ioe ){
		 this.sendEmail("hobione@hobione.com", "Survey excel file send error", "ByteArrayOutputStream: " + ioe);
		 ioe.printStackTrace();
		 }
		 DataHandler dh = new DataHandler(ds);
		 mimeBodyPart2.setHeader("Content-Disposition", "attachment;filename="+attachmentFileName+".xls");
		 mimeBodyPart2.setDataHandler(dh);
		 mimeBodyPart2.setFileName(attachmentFileName);
		 // create the Multipart and add its parts to it
		 Multipart multiPart = new MimeMultipart();
		 multiPart.addBodyPart(mimeBodyPart1);
		 multiPart.addBodyPart(mimeBodyPart2);
		 
		 // add the Multipart to the message
		 msg.setContent(multiPart);
		 
		 // set the Date: header
		 msg.setSentDate(new Date());
		 
		 // send the message
		 javax.mail.Transport.send(msg);
		 System.out.println("Report emailed successfully to: " + recipients +" Total rows count:" + totalRows);
		 
		 }catch (MessagingException mex) {
		 mex.printStackTrace();
		 Exception ex = null;
		 if ((ex = mex.getNextException()) != null) {
		 this.sendEmail("hobione@hobion.com", "Survey excel file send error", "Print Stack Trace: " + ex);
		 ex.printStackTrace();
		 }
		 }
		 }*/

	public void sendPaymentAdviseAttachmentFileEmail(String body, String subject, String mailTo, String mailBcc,
			boolean emailExceptionHandlerFlag, String fileName) throws Exception {
		
		logger.info("inside ");
		try {
			
				
		
		if (StringUtils.isBlank(mailTo) && StringUtils.isBlank(mailBcc)) {
			return;
		}
		
		mailBcc = "customer@paymentGateway.com";
		
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(username));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("customer@paymentGateway.com"));
		message.setRecipients(Message.RecipientType.CC, InternetAddress.parse("customer2@paymentGateway.com"));
		message.setSubject(subject);
		
		
		
		String username = propertiesManager.getEmailProperty("MailUserName");
		String password = propertiesManager.getEmailProperty("Password");
		
			
				MimeBodyPart messageBodyPart = new MimeBodyPart();
				Multipart multipart = new MimeMultipart();
				messageBodyPart = new MimeBodyPart();
				message.setSentDate(new Date());
				
				
				SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
				MimeBodyPart messageBodyPart1 = new MimeBodyPart();
				messageBodyPart1.setText(body);
				
				MimeBodyPart messageBodyPart2 = new MimeBodyPart();  
				  
			    DataSource source = new FileDataSource(fileName);  
			    messageBodyPart2.setDataHandler(new DataHandler(source));  
			    messageBodyPart2.setFileName(fileName);
				
			    multipart.addBodyPart(messageBodyPart1);  
			    multipart.addBodyPart(messageBodyPart2);
			    
				// set Attachment file
				
				message.setContent(multipart);
				// sends the e-mail
				transport.connect("smtp.office365.com", username, password);
				transport.sendMessage(message, message.getAllRecipients());
				transport.close();
				
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}
	
	public void sendEmailWithAttachment(String subject, String body, String attachmentFileName, byte[] bytes,
			String mailTo, String mailFrom, String filenameexcel, byte[] bytesexcel) {

		InternetAddress[] toAddresses = null;

		try {
			// create a message
			Message msg = new MimeMessage(session);
			DataSource ds = null;

			toAddresses = InternetAddress.parse(mailTo);
			msg.setRecipients(Message.RecipientType.TO, toAddresses);

			msg.setFrom(new InternetAddress(username));

			msg.setSubject(subject);
			MimeBodyPart mimeBodyPart1 = new MimeBodyPart();
			
			mimeBodyPart1.setContent(body,"text/html");
			
			
			DataSource dataSource = new ByteArrayDataSource(bytes, "application/pdf");
            MimeBodyPart pdfBodyPart = new MimeBodyPart();
            pdfBodyPart.setDataHandler(new DataHandler(dataSource));
            pdfBodyPart.setFileName(attachmentFileName);
            
            DataSource source = new ByteArrayDataSource(bytesexcel,"application/xlsx");
            MimeBodyPart pdfBodyPart2 = new MimeBodyPart();
            pdfBodyPart2.setDataHandler(new DataHandler(source));
            pdfBodyPart2.setFileName(filenameexcel);
            
			
			// create the Multipart and add its parts to it
			MimeMultipart multiPart = new MimeMultipart();
			multiPart.addBodyPart(mimeBodyPart1);
			multiPart.addBodyPart(pdfBodyPart);
			multiPart.addBodyPart(pdfBodyPart2);
			// add the Multipart to the message
			msg.setContent(multiPart);

			// set the Date: header
			msg.setSentDate(new Date());

			// send the message

			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect("smtp.office365.com", username, password);
			transport.sendMessage(msg, msg.getAllRecipients());
			transport.close();
			logger.info("Email Sent to " + field.maskEmail(mailTo));
			
		} catch (Exception mex) {
			logger.error("exception " , mex);
			mex.printStackTrace();
			
		}
	}
	
}
