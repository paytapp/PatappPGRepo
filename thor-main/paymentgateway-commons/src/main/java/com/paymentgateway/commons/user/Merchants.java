package com.paymentgateway.commons.user;

import java.io.Serializable;

public class Merchants implements Serializable {

	public Merchants() {

	}

	private static final long serialVersionUID = -5829924589073475754L;

	private String emailId;
	private String payId;
	private String businessName;
	private String firstName;
	private String lastName;
	private String mobile;
	private String paymentType;
	private String txnType;
	private Boolean isActive;
	private String resellerId;
	private String superMerchantId;
	private String parentPayId;
	private Boolean isSuperMerchant;
	private String superMerchantName;
	private String status;
	private String registrationDate;
	private String updationDate;
	private String createdBy;
	private String updatedBy;
	private String activationDate;
	private String virtualAccountNo;
	private String merchantVPA;
	public boolean retailMerchantFlag;
	private boolean paymentAdviceFlag;
	private boolean partnerFlag;
	private String subUserType;
	private String userTypeOrName;
	private String modeType;
	private boolean customerQrFlag;
	private String mpaStage;

	public void setMerchant(User user) {
		setEmailId(user.getEmailId());
		setBusinessName(user.getBusinessName());
		setPayId(user.getPayId());
	}

	public String getUserTypeOrName() {
		return userTypeOrName;
	}

	public void setUserTypeOrName(String userTypeOrName) {
		this.userTypeOrName = userTypeOrName;
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

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getSuperMerchantId() {
		return superMerchantId;
	}

	public void setSuperMerchantId(String superMerchantId) {
		this.superMerchantId = superMerchantId;
	}

	public Boolean getIsSuperMerchant() {
		return isSuperMerchant;
	}

	public void setIsSuperMerchant(Boolean isSuperMerchant) {
		this.isSuperMerchant = isSuperMerchant;
	}

	public String getSuperMerchantName() {
		return superMerchantName;
	}

	public void setSuperMerchantName(String superMerchantName) {
		this.superMerchantName = superMerchantName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getResellerId() {
		return resellerId;
	}

	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
	}

	public String getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}

	public String getUpdationDate() {
		return updationDate;
	}

	public void setUpdationDate(String updationDate) {
		this.updationDate = updationDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(String activationDate) {
		this.activationDate = activationDate;
	}

	public String getParentPayId() {
		return parentPayId;
	}

	public void setParentPayId(String parentPayId) {
		this.parentPayId = parentPayId;
	}

	public boolean isRetailMerchantFlag() {
		return retailMerchantFlag;
	}

	public void setRetailMerchantFlag(boolean retailMerchantFlag) {
		this.retailMerchantFlag = retailMerchantFlag;
	}

	public String getSubUserType() {
		return subUserType;
	}

	public void setSubUserType(String subUserType) {
		this.subUserType = subUserType;
	}

	public boolean isPaymentAdviceFlag() {
		return paymentAdviceFlag;
	}

	public void setPaymentAdviceFlag(boolean paymentAdviceFlag) {
		this.paymentAdviceFlag = paymentAdviceFlag;
	}

	public String getMerchantVPA() {
		return merchantVPA;
	}

	public void setMerchantVPA(String merchantVPA) {
		this.merchantVPA = merchantVPA;
	}

	public String getVirtualAccountNo() {
		return virtualAccountNo;
	}

	public void setVirtualAccountNo(String virtualAccountNo) {
		this.virtualAccountNo = virtualAccountNo;
	}

	public boolean isPartnerFlag() {
		return partnerFlag;
	}

	public void setPartnerFlag(boolean partnerFlag) {
		this.partnerFlag = partnerFlag;
	}

	public String getModeType() {
		return modeType;
	}

	public void setModeType(String modeType) {
		this.modeType = modeType;
	}

	public boolean isCustomerQrFlag() {
		return customerQrFlag;
	}

	public void setCustomerQrFlag(boolean customerQrFlag) {
		this.customerQrFlag = customerQrFlag;
	}

	public String getMpaStage() {
		return mpaStage;
	}

	public void setMpaStage(String mpaStage) {
		this.mpaStage = mpaStage;
	}

	
}
