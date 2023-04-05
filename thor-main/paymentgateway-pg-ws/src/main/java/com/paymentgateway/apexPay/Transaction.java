package com.paymentgateway.apexPay;

import org.springframework.stereotype.Service;

@Service("apexPayTransaction")
public class Transaction {

	
	// SALE
	private String request;
	private String saltKey;
	private String encKey;
	private String payId;
	private String orderId;
	private String refundOrderId;
	private String currencyCode;
	private String txnType;
	private String haskey;
	private String cardNumber;
	private String expiryDate;
	private String cavv;
	private String custName;
	private String custEmail;
	private String custPhone;
	private String paymentType;
	private String mopType;
	private String payerAddress;
	private String redirectUrl;
	private String amount;
	private String status;
	private String responseCode;
	private String responseMessage;
	private String pgRefNum;
	private String acqId;
	private String rrn;
	private String cardHolderName;
	private String pgTxnMsg;
	
	
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public String getSaltKey() {
		return saltKey;
	}
	public void setSaltKey(String saltKey) {
		this.saltKey = saltKey;
	}
	public String getEncKey() {
		return encKey;
	}
	public void setEncKey(String encKey) {
		this.encKey = encKey;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public String getHaskey() {
		return haskey;
	}
	public void setHaskey(String haskey) {
		this.haskey = haskey;
	}
	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	public String getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}
	public String getCavv() {
		return cavv;
	}
	public void setCavv(String cavv) {
		this.cavv = cavv;
	}
	public String getCustName() {
		return custName;
	}
	public void setCustName(String custName) {
		this.custName = custName;
	}
	public String getCustEmail() {
		return custEmail;
	}
	public void setCustEmail(String custEmail) {
		this.custEmail = custEmail;
	}
	public String getCustPhone() {
		return custPhone;
	}
	public void setCustPhone(String custPhone) {
		this.custPhone = custPhone;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getMopType() {
		return mopType;
	}
	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
	public String getPayerAddress() {
		return payerAddress;
	}
	public void setPayerAddress(String payerAddress) {
		this.payerAddress = payerAddress;
	}
	public String getRedirectUrl() {
		return redirectUrl;
	}
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
	public String getRefundOrderId() {
		return refundOrderId;
	}
	public void setRefundOrderId(String refundOrderId) {
		this.refundOrderId = refundOrderId;
	}
	public String getPgRefNum() {
		return pgRefNum;
	}
	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}
	public String getAcqId() {
		return acqId;
	}
	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}
	public String getRrn() {
		return rrn;
	}
	public void setRrn(String rrn) {
		this.rrn = rrn;
	}
	public String getCardHolderName() {
		return cardHolderName;
	}
	public void setCardHolderName(String cardHolderName) {
		this.cardHolderName = cardHolderName;
	}
	public String getPgTxnMsg() {
		return pgTxnMsg;
	}
	public void setPgTxnMsg(String pgTxnMsg) {
		this.pgTxnMsg = pgTxnMsg;
	}
	
	
}
