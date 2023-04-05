package com.paymentgateway.notification.email;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSenderTest {

	static Properties properties = new Properties();

	public static void main(String[] args) {

		{
			properties.put("mail.smtp.host", "smtp.office365.com");
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.port", "25");
			properties.put("mail.smtp.starttls.enable", "true");
			properties.put("mail.smtp.ssl.trust", "*");
		}

		sendEmail("Hi, This is a test email", "testMail", "rahul@paymentgateway.com", "siddhartha.sharma@paymentgateway.com");

	}

	public static void sendEmail(String body, String subject, String mailTo, String mailBcc) {

		String mailSender = "shaiwal.mallick@paymentgateway.com"; // Enter your mail Id
		String username = "shaiwal.mallick@paymentgateway.com"; // Enter your username , email ID
		String password = ""; // Enter your password
		InternetAddress[] toAddresses = null;
		InternetAddress[] bccAddresses = null;
		try {
			Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(mailSender));
			// Sending Bulk Email
			toAddresses = InternetAddress.parse(mailTo);
			bccAddresses = InternetAddress.parse(mailBcc);
			message.setRecipients(Message.RecipientType.TO, toAddresses);
			message.setRecipients(Message.RecipientType.BCC, bccAddresses);
			message.setSubject(subject);
			message.setSentDate(new Date());
			// set plain text message
			message.setContent(body, "text/html");
			// sends the e-mail
			Transport.send(message);

			System.out.println("DONE");
		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}
}
