package com.paymentgateway.commons.util;

public class MPAStatusEmail {

	private String toEmail;
	private String messageBody;
	private String subject;
	private String bccMail;
	
	public MPAStatusEmail() {
	}
	
	public String getToEmail() {
		return toEmail;
	}
	public void setToEmail(String toEmail) {
		this.toEmail = toEmail;
	}
	public String getMessageBody() {
		return messageBody;
	}
	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBccMail() {
		return bccMail;
	}

	public void setBccMail(String bccMail) {
		this.bccMail = bccMail;
	}
	
	
}
