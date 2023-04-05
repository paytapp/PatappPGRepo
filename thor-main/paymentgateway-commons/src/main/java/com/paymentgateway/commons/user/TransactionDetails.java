package com.paymentgateway.commons.user;

public class TransactionDetails {

	private String custName;
	private String custMobileNum;
	private String custAddress;
	private String custCity;
	private String custState;
	private String custPin;
	private String custCountry;
	
	private String custShippingName;
	private String custShippingMobileNum;
	private String custShippingAddress;
	private String custShippingCity;
	private String custShippingState;
	private String custShippingPin;
	private String custShippingCountry;
	
	private String amount;
	
	private String tdrORSurcharge;
	private String GST;
	private String acquirerCommission; 
	private String pgCommission;
	private String acquirerGST;
	private String pgGST;
	private String acquirerName;
	private String totalAmount;
	private String paymentType;
	private String bankName;
	private String region;
	private String cardHolderType;
	private String orderId2;
	private String pgRefNum2;
	private String rrn;
	private String captureResponseMessage;
	private String settleResponseMessage;
	
	private String txnType;
	private String status;
	private String date;

	
	public String getCustName() {
		return custName;
	}
	public void setCustName(String custName) {
		this.custName = custName;
	}
	public String getCustMobileNum() {
		return custMobileNum;
	}
	public void setCustMobileNum(String custMobileNum) {
		this.custMobileNum = custMobileNum;
	}
	public String getCustAddress() {
		return custAddress;
	}
	public void setCustAddress(String custAddress) {
		this.custAddress = custAddress;
	}
	public String getCustCity() {
		return custCity;
	}
	public void setCustCity(String custCity) {
		this.custCity = custCity;
	}
	public String getCustState() {
		return custState;
	}
	public void setCustState(String custState) {
		this.custState = custState;
	}
	public String getCustPin() {
		return custPin;
	}
	public void setCustPin(String custPin) {
		this.custPin = custPin;
	}
	public String getCustCountry() {
		return custCountry;
	}
	public void setCustCountry(String custCountry) {
		this.custCountry = custCountry;
	}
	public String getCustShippingName() {
		return custShippingName;
	}
	public void setCustShippingName(String custShippingName) {
		this.custShippingName = custShippingName;
	}
	public String getCustShippingMobileNum() {
		return custShippingMobileNum;
	}
	public void setCustShippingMobileNum(String custShippingMobileNum) {
		this.custShippingMobileNum = custShippingMobileNum;
	}
	public String getCustShippingAddress() {
		return custShippingAddress;
	}
	public void setCustShippingAddress(String custShippingAddress) {
		this.custShippingAddress = custShippingAddress;
	}
	public String getCustShippingCity() {
		return custShippingCity;
	}
	public void setCustShippingCity(String custShippingCity) {
		this.custShippingCity = custShippingCity;
	}
	public String getCustShippingState() {
		return custShippingState;
	}
	public void setCustShippingState(String custShippingState) {
		this.custShippingState = custShippingState;
	}
	public String getCustShippingPin() {
		return custShippingPin;
	}
	public void setCustShippingPin(String custShippingPin) {
		this.custShippingPin = custShippingPin;
	}
	public String getCustShippingCountry() {
		return custShippingCountry;
	}
	public void setCustShippingCountry(String custShippingCountry) {
		this.custShippingCountry = custShippingCountry;
	}

	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getTdrORSurcharge() {
		return tdrORSurcharge;
	}
	public void setTdrORSurcharge(String tdrORSurcharge) {
		this.tdrORSurcharge = tdrORSurcharge;
	}
	public String getGST() {
		return GST;
	}
	public void setGST(String gST) {
		GST = gST;
	}
	public String getAcquirerCommission() {
		return acquirerCommission;
	}
	public void setAcquirerCommission(String acquirerCommission) {
		this.acquirerCommission = acquirerCommission;
	}
	public String getPgCommission() {
		return pgCommission;
	}
	public void setPgCommission(String pgCommission) {
		this.pgCommission = pgCommission;
	}
	public String getAcquirerGST() {
		return acquirerGST;
	}
	public void setAcquirerGST(String acquirerGST) {
		this.acquirerGST = acquirerGST;
	}
	public String getPgGST() {
		return pgGST;
	}
	public void setPgGST(String pgGST) {
		this.pgGST = pgGST;
	}
	public String getAcquirerName() {
		return acquirerName;
	}
	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}
	public String getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getCardHolderType() {
		return cardHolderType;
	}
	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}
	public String getOrderId2() {
		return orderId2;
	}
	public void setOrderId2(String orderId2) {
		this.orderId2 = orderId2;
	}
	public String getPgRefNum2() {
		return pgRefNum2;
	}
	public void setPgRefNum2(String pgRefNum2) {
		this.pgRefNum2 = pgRefNum2;
	}
	public String getRrn() {
		return rrn;
	}
	public void setRrn(String rrn) {
		this.rrn = rrn;
	}
	public String getCaptureResponseMessage() {
		return captureResponseMessage;
	}
	public void setCaptureResponseMessage(String captureResponseMessage) {
		this.captureResponseMessage = captureResponseMessage;
	}
	public String getSettleResponseMessage() {
		return settleResponseMessage;
	}
	public void setSettleResponseMessage(String settleResponseMessage) {
		this.settleResponseMessage = settleResponseMessage;
	}
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
}
