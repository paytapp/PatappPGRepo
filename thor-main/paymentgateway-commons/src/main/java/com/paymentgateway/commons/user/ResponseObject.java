package com.paymentgateway.commons.user;

import org.springframework.stereotype.Service;

@Service
public class ResponseObject {

	private String responseCode;
	private String responseMessage;
	private String accountValidationID;
	private String payId;
	private String email;
	private String receiverNumber;
	private String phoneNumber;
	private String creationDate;
	private String userType;
	private String name;
    private String batuaPage;
	
	public ResponseObject() {

	}

	public String getCreationDate() {
		return creationDate;
	}


	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}


	public String getUserType() {
		return userType;
	}


	public void setUserType(String userType) {
		this.userType = userType;
	}


	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getAccountValidationID() {
		return accountValidationID;
	}

	public void setAccountValidationID(String accountValidationID) {
		this.accountValidationID = accountValidationID;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}	

	public String getReceiverNumber() {
		return receiverNumber;
	}

	public void setReceiverNumber(String receiverNumber) {
		this.receiverNumber = receiverNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBatuaPage() {
		return batuaPage;
	}

	public void setBatuaPage(String batuaPage) {
		this.batuaPage = batuaPage;
	}

}