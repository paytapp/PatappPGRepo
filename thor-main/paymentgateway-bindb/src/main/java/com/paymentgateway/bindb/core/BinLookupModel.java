package com.paymentgateway.bindb.core;

public class BinLookupModel {
	
	private String merchantReferenceCode;
	private String requestID;
	private String decision;
	private String reasonCode;

    private String requestDateTime;
	private String transactionResult;
	private String transactionTime;
	private String requestToken;
	
	private String cardType;
	private String cardTypeName;
	private String cardSubType;
	private String productCategory;
    private String issuerName;
    private String country;
    private String countryNumericCode;
    private String phoneNumber;
    


	
	
	public String getMerchantReferenceCode() {
		return merchantReferenceCode;
	}
	public void setMerchantReferenceCode(String merchantReferenceCode) {
		this.merchantReferenceCode = merchantReferenceCode;
	}
	public String getRequestID() {
		return requestID;
	}
	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}
	public String getDecision() {
		return decision;
	}
	public void setDecision(String decision) {
		this.decision = decision;
	}
	public String getReasonCode() {
		return reasonCode;
	}
	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}
	public String getRequestDateTime() {
		return requestDateTime;
	}
	public void setRequestDateTime(String requestDateTime) {
		this.requestDateTime = requestDateTime;
	}
	public String getTransactionResult() {
		return transactionResult;
	}
	public void setTransactionResult(String transactionResult) {
		this.transactionResult = transactionResult;
	}
	public String getTransactionTime() {
		return transactionTime;
	}
	public void setTransactionTime(String transactionTime) {
		this.transactionTime = transactionTime;
	}
	public String getRequestToken() {
		return requestToken;
	}
	public void setRequestToken(String requestToken) {
		this.requestToken = requestToken;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	public String getCardTypeName() {
		return cardTypeName;
	}
	public void setCardTypeName(String cardTypeName) {
		this.cardTypeName = cardTypeName;
	}
	public String getCardSubType() {
		return cardSubType;
	}
	public void setCardSubType(String cardSubType) {
		this.cardSubType = cardSubType;
	}
	public String getProductCategory() {
		return productCategory;
	}
	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}
	
	public String getIssuerName() {
		return issuerName;
	}
	public void setIssuerName(String issuerName) {
		this.issuerName = issuerName;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCountryNumericCode() {
		return countryNumericCode;
	}
	public void setCountryNumericCode(String countryNumericCode) {
		this.countryNumericCode = countryNumericCode;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	

}
