package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Proxy;

@Entity
@Proxy(lazy= false)
public class MPAMerchant implements Serializable{

	
	private static final long serialVersionUID = 8277104267988868652L;
	@Id
	@Column(nullable=false,unique=true)
	private String emailId;
	private String payId;
	private String businessName;
	private String mobile;
	private Date registrationDate;
	private String industryCategory;
	
	private String makerName;
	private String checkerName;
	private String makerPayId;
	private String checkerPayId;
	private String makerStatus;
	private String checkerStatus;
	private String makerStatusUpDate;
	private String checkerStatusUpDate;
	private String adminStatus;
	private String adminStatusUpDate;
	private String adminFileName;
	private String adminComment;
	private String userStatus;
	private String checkerComments;
	private String makerComments;
	private String makerFileName;
	private String checkerFileName;
	private String formSubmissionDate;
	@Transient
	private String checkerMakerType;
	@Transient
	private String isMpaOnlineFlag;
	
	@Enumerated(EnumType.STRING)
	private UserType userType;

	
	public String getIsMpaOnlineFlag() {
		return isMpaOnlineFlag;
	}

	public void setIsMpaOnlineFlag(String isMpaOnlineFlag) {
		this.isMpaOnlineFlag = isMpaOnlineFlag;
	}

	public String getAdminFileName() {
		return adminFileName;
	}

	public void setAdminFileName(String adminFileName) {
		this.adminFileName = adminFileName;
	}

	public String getAdminComment() {
		return adminComment;
	}

	public void setAdminComment(String adminComment) {
		this.adminComment = adminComment;
	}

	public String getFormSubmissionDate() {
		return formSubmissionDate;
	}

	public void setFormSubmissionDate(String formSubmissionDate) {
		this.formSubmissionDate = formSubmissionDate;
	}

	public String getCheckerMakerType() {
		return checkerMakerType;
	}

	public void setCheckerMakerType(String checkerMakerType) {
		this.checkerMakerType = checkerMakerType;
	}

	public String getMakerFileName() {
		return makerFileName;
	}

	public void setMakerFileName(String makerFileName) {
		this.makerFileName = makerFileName;
	}

	public String getCheckerFileName() {
		return checkerFileName;
	}

	public void setCheckerFileName(String checkerFileName) {
		this.checkerFileName = checkerFileName;
	}

	public String getCheckerComments() {
		return checkerComments;
	}

	public void setCheckerComments(String checkerComments) {
		this.checkerComments = checkerComments;
	}

	public String getMakerComments() {
		return makerComments;
	}

	public void setMakerComments(String makerComments) {
		this.makerComments = makerComments;
	}

	public String getIndustryCategory() {
		return industryCategory;
	}

	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public String getMakerName() {
		return makerName;
	}

	public void setMakerName(String makerName) {
		this.makerName = makerName;
	}

	public String getCheckerName() {
		return checkerName;
	}

	public void setCheckerName(String checkerName) {
		this.checkerName = checkerName;
	}

	public String getMakerPayId() {
		return makerPayId;
	}

	public void setMakerPayId(String makerPayId) {
		this.makerPayId = makerPayId;
	}

	public String getCheckerPayId() {
		return checkerPayId;
	}

	public void setCheckerPayId(String checkerPayId) {
		this.checkerPayId = checkerPayId;
	}

	public String getMakerStatus() {
		return makerStatus;
	}

	public void setMakerStatus(String makerStatus) {
		this.makerStatus = makerStatus;
	}

	public String getCheckerStatus() {
		return checkerStatus;
	}

	public void setCheckerStatus(String checkerStatus) {
		this.checkerStatus = checkerStatus;
	}

	public String getMakerStatusUpDate() {
		return makerStatusUpDate;
	}

	public void setMakerStatusUpDate(String makerStatusUpDate) {
		this.makerStatusUpDate = makerStatusUpDate;
	}

	public String getCheckerStatusUpDate() {
		return checkerStatusUpDate;
	}

	public void setCheckerStatusUpDate(String checkerStatusUpDate) {
		this.checkerStatusUpDate = checkerStatusUpDate;
	}

	public String getAdminStatus() {
		return adminStatus;
	}

	public void setAdminStatus(String adminStatus) {
		this.adminStatus = adminStatus;
	}

	public String getAdminStatusUpDate() {
		return adminStatusUpDate;
	}

	public void setAdminStatusUpDate(String adminStatusUpDate) {
		this.adminStatusUpDate = adminStatusUpDate;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}
	
	
}
