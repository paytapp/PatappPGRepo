package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Map;

public class EmailData implements Serializable
{

	private static final long serialVersionUID = 6376064932206197881L;
	
	private String emailTo;
	private String emailbcc;
	private String subject;
	private String body;
	private String fromName;
	private String fileContentbase64;
	private String fileName;
	private String tag;
	private String emailCc;
	private String emailFrom;
	
	private Map<String,String> multipleAttachments;
	
	
	public String getEmailTo() {
		return emailTo;
	}
	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getFromName() {
		return fromName;
	}
	public void setFromName(String fromName) {
		this.fromName = fromName;
	}
	public String getFileContentbase64() {
		return fileContentbase64;
	}
	public void setFileContentbase64(String fileContentbase64) {
		this.fileContentbase64 = fileContentbase64;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getEmailbcc() {
		return emailbcc;
	}
	public void setEmailbcc(String emailbcc) {
		this.emailbcc = emailbcc;
	}
	public Map<String, String> getMultipleAttachments() {
		return multipleAttachments;
	}
	public void setMultipleAttachments(Map<String, String> multipleAttachments) {
		this.multipleAttachments = multipleAttachments;
	}
	public String getEmailCc() {
		return emailCc;
	}
	public void setEmailCc(String emailCc) {
		this.emailCc = emailCc;
	}
	public String getEmailFrom() {
		return emailFrom;
	}
	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}

	
}
