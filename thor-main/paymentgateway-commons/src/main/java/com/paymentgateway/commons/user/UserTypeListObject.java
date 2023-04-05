package com.paymentgateway.commons.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserTypeListObject {

	private String srNo;
	private String payId;
	private String businessName;
	private String status;
	private String mobile;
	private String registrationDate;
	private String updatedDate;
	private String emailId;
	private String userType;
	private String makerName;
	private String makerStatus;
	private String makerStatusUpDate;
	private String checkerName;
	private String checkerStatus;
	private String checkerStatusUpDate;
	private String superMerchantId;
	private String superMerchantName;
	private String parentName;
	private String resellerType;
	private String subUserType;

	public String getSrNo() {
		return srNo;
	}

	public void setSrNo(String srNo) {
		this.srNo = srNo;
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

	public String getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getMakerName() {
		return makerName;
	}

	public void setMakerName(String makerName) {
		this.makerName = makerName;
	}

	public String getMakerStatus() {
		return makerStatus;
	}

	public void setMakerStatus(String makerStatus) {
		this.makerStatus = makerStatus;
	}

	public String getMakerStatusUpDate() {
		return makerStatusUpDate;
	}

	public void setMakerStatusUpDate(String makerStatusUpDate) {
		this.makerStatusUpDate = makerStatusUpDate;
	}

	public String getCheckerName() {
		return checkerName;
	}

	public void setCheckerName(String checkerName) {
		this.checkerName = checkerName;
	}

	public String getCheckerStatus() {
		return checkerStatus;
	}

	public void setCheckerStatus(String checkerStatus) {
		this.checkerStatus = checkerStatus;
	}

	public String getCheckerStatusUpDate() {
		return checkerStatusUpDate;
	}

	public void setCheckerStatusUpDate(String checkerStatusUpDate) {
		this.checkerStatusUpDate = checkerStatusUpDate;
	}

	public String getSuperMerchantId() {
		return superMerchantId;
	}

	public void setSuperMerchantId(String superMerchantId) {
		this.superMerchantId = superMerchantId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSuperMerchantName() {
		return superMerchantName;
	}

	public void setSuperMerchantName(String superMerchantName) {
		this.superMerchantName = superMerchantName;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getResellerType() {
		return resellerType;
	}

	public void setResellerType(String resellerType) {
		this.resellerType = resellerType;
	}
	
	public String getSubUserType() {
		return subUserType;
	}

	public void setSubUserType(String subUserType) {
		this.subUserType = subUserType;
	}

	
	public Object[] downloadMerchantOrSuperMerchantListReport() {
		Object[] objectArray = new Object[15];

		objectArray[0] = srNo;
		objectArray[1] = payId;
		objectArray[2] = businessName;
		objectArray[3] = status;
		objectArray[4] = mobile;
		objectArray[5] = registrationDate;
		objectArray[6] = updatedDate;
		objectArray[7] = emailId;
		objectArray[8] = userType;
		objectArray[9] = makerName;
		objectArray[10] = makerStatus;
		objectArray[11] = makerStatusUpDate;
		objectArray[12] = checkerName;
		objectArray[13] = checkerStatus;
		objectArray[14] = checkerStatusUpDate;

		return objectArray;
	}

	public Object[] downloadSubMerchantListReport() {
		Object[] objectArray = new Object[10];

		objectArray[0] = srNo;
		objectArray[1] = payId;
		objectArray[2] = businessName;
		objectArray[3] = superMerchantName;
		objectArray[4] = status;
		objectArray[5] = mobile;
		objectArray[6] = registrationDate;
		objectArray[7] = updatedDate;
		objectArray[8] = emailId;
		objectArray[9] = userType;

		return objectArray;
	}

	public Object[] downloadSubUserListReport() {
		Object[] objectArray = new Object[11];

		objectArray[0] = srNo;
		objectArray[1] = payId;
		objectArray[2] = businessName;
		objectArray[3] = parentName;
		objectArray[4] = status;
		objectArray[5] = mobile;
		objectArray[6] = registrationDate;
		objectArray[7] = updatedDate;
		objectArray[8] = emailId;
		objectArray[9] = userType;
		objectArray[10] = subUserType;

		return objectArray;
	}

	public Object[] downloadResellerListReport() {
		Object[] objectArray = new Object[10];

		objectArray[0] = srNo;
		objectArray[1] = payId;
		objectArray[2] = businessName;
		objectArray[3] = resellerType;
		objectArray[4] = status;
		objectArray[5] = mobile;
		objectArray[6] = registrationDate;
		objectArray[7] = updatedDate;
		objectArray[8] = emailId;
		objectArray[9] = userType;

		return objectArray;
	}

}
