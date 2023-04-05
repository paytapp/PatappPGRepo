package com.paymentgateway.commons.user;

import com.paymentgateway.commons.util.UserStatusType;

public class MerchantDetails {
	
	public MerchantDetails(){
		
	}
	public MerchantDetails(String payId,String resellerId,String businessName, String emailId, UserStatusType status,String mobile, String registrationDate,String userType,String businessType){
		this.payId = payId;
		this.resellerId = resellerId;
		this.setBusinessName(businessName);
		this.emailId = emailId;
		this.mobile = mobile;
		this.registrationDate = registrationDate;
		this.status = status;
		this.userType = userType;
		this.businessType = businessType;
	}
	private String makerName;
	private String checkerName;
	private String makerStatus;
	private String checkerStatus;
	private String makerStatusUpDate;
	private String checkerStatusUpDate;
	private String checkerComments;
	private String makerComments;
	private String comments;
	private String payId;
	private String resellerId;
	private String businessName;
	private String emailId;
	private String mobile;
	private String registrationDate;
	private UserStatusType status;
	private String userType;
	private String businessType;
	private String createdBy;
	private String updatedDate;
	private String mpaStage;
	private String superMerchantId;
	private boolean isSuperMerchant;
	private String adminStatus;
	private String industryCategory;
	
	public String getIndustryCategory() {
		return industryCategory;
	}
	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}
	public String getAdminStatus() {
		return adminStatus;
	}
	public void setAdminStatus(String adminStatus) {
		this.adminStatus = adminStatus;
	}
	public boolean isSuperMerchant() {
		return isSuperMerchant;
	}
	public void setSuperMerchant(boolean isSuperMerchant) {
		this.isSuperMerchant = isSuperMerchant;
	}
	public String getSuperMerchantId() {
		return superMerchantId;
	}
	public void setSuperMerchantId(String superMerchantId) {
		this.superMerchantId = superMerchantId;
	}
	public String getMpaStage() {
		return mpaStage;
	}
	public void setMpaStage(String mpaStage) {
		this.mpaStage = mpaStage;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
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
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
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
	public UserStatusType getStatus() {
		return status;
	}
	public void setStatus(UserStatusType status) {
		this.status = status;
	}
	public String getPayId() {
		return payId;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getRegistrationDate() {
		return registrationDate;
	}
	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public String getResellerId() {
		return resellerId;
	}
	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
	}
	public String getBusinessType() {
		return businessType;
	}
	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}
	public String getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}
	

}
