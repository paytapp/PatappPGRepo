package com.paymentgateway.commons.util;

import java.util.List;

public class MailersObject {

	private List<String> toEmailList;
	private String messageBody;
	private String subject;
	private String mailContent;
	
	
	
	public List<String> getToEmailList() {
		return toEmailList;
	}
	public void setToEmailList(List<String> toEmailList) {
		this.toEmailList = toEmailList;
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
	public String getMailContent() {
		return mailContent;
	}
	public void setMailContent(String mailContent) {
		this.mailContent = mailContent;
	}
	
	
	
	
}
