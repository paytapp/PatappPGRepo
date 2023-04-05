package com.paymentgateway.commons.user;

import java.io.Serializable;

public class BatchTransactionObj implements Serializable{

	private static final long serialVersionUID = -5508382146390809111L;
	
	private String origTxnId;
	private String amount;
	private String payId;
	private String currencyCode;
	private String txnType;
	private String orderId;
	private String mopType;
	private String custEmail;
	public BatchTransactionObj(){}

	
	public BatchTransactionObj(String origTxnId, String txnType, String amount, String payId, String currencyCode,String orderId) {

		this.origTxnId = origTxnId;
		this.amount = amount;
		this.payId = payId;
		this.currencyCode = currencyCode;
		this.txnType = txnType;
		this.orderId = orderId;
			}
	
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public String getOrigTxnId() {
		return origTxnId;
	}
	public void setOrigTxnId(String origTxnId) {
		this.origTxnId = origTxnId;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
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
	public String getMopType() {
		return mopType;
	}
	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
	public String getCustEmail() {
		return custEmail;
	}
	public void setCustEmail(String custEmail) {
		this.custEmail = custEmail;
	}
	

}
