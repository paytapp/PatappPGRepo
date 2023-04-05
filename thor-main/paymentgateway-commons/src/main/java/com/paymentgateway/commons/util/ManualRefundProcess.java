package com.paymentgateway.commons.util;

import java.io.Serializable;

public class ManualRefundProcess implements Serializable {

	private static final long serialVersionUID = 8941165877471520289L;
	
	private String pgRefNum;
	private String orderId;
	private String amount;
	private String currencyCode;
	private String txnType;
	private String merchantName;
	private String payId;
	private String surchargeFlag;
	private String refundedAmount;
	private String refundAvailable;
	private String chargebackAmount;
	private String regNumber;
	private String objectId;
	private String chargebackStatus;
	
	
	
	public String getChargebackStatus() {
		return chargebackStatus;
	}
	public void setChargebackStatus(String chargebackStatus) {
		this.chargebackStatus = chargebackStatus;
	}
	public String getRegNumber() {
		return regNumber;
	}
	public void setRegNumber(String regNumber) {
		this.regNumber = regNumber;
	}
	public String getSurchargeFlag() {
		return surchargeFlag;
	}
	public void setSurchargeFlag(String surchargeFlag) {
		this.surchargeFlag = surchargeFlag;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getPgRefNum() {
		return pgRefNum;
	}
	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public String getRefundedAmount() {
		return refundedAmount;
	}
	public void setRefundedAmount(String refundedAmount) {
		this.refundedAmount = refundedAmount;
	}
	public String getRefundAvailable() {
		return refundAvailable;
	}
	public void setRefundAvailable(String refundAvailable) {
		this.refundAvailable = refundAvailable;
	}
	public String getChargebackAmount() {
		return chargebackAmount;
	}
	public void setChargebackAmount(String chargebackAmount) {
		this.chargebackAmount = chargebackAmount;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
}