package com.paymentgateway.payout.cashfreePayout;

public class Transaction {
	
	private String bnfId;
	private String errorCode;
	
	private String utr;
	private String reqId;
	private String uniqueId;
	private String responseCode;
	
	// TRAMO Payout Fields
	private String ifsc;
	private String ClientId;
	private String accountNumber;
	private String amount;
	
	private String agentCharge;
	private String status;
	private String message;
	private String txnId;
	private String refId;
	private String bankRef;
	private String msg;
	private String bankRefNo;
	
	private String subCode;
	private String referenceId;
	private String acknowledged;
	public String getBnfId() {
		return bnfId;
	}
	public void setBnfId(String bnfId) {
		this.bnfId = bnfId;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getUtr() {
		return utr;
	}
	public void setUtr(String utr) {
		this.utr = utr;
	}
	public String getReqId() {
		return reqId;
	}
	public void setReqId(String reqId) {
		this.reqId = reqId;
	}
	public String getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public String getIfsc() {
		return ifsc;
	}
	public void setIfsc(String ifsc) {
		this.ifsc = ifsc;
	}
	public String getClientId() {
		return ClientId;
	}
	public void setClientId(String clientId) {
		ClientId = clientId;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getAgentCharge() {
		return agentCharge;
	}
	public void setAgentCharge(String agentCharge) {
		this.agentCharge = agentCharge;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
	public String getRefId() {
		return refId;
	}
	public void setRefId(String refId) {
		this.refId = refId;
	}
	public String getBankRef() {
		return bankRef;
	}
	public void setBankRef(String bankRef) {
		this.bankRef = bankRef;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getBankRefNo() {
		return bankRefNo;
	}
	public void setBankRefNo(String bankRefNo) {
		this.bankRefNo = bankRefNo;
	}
	public String getSubCode() {
		return subCode;
	}
	public void setSubCode(String subCode) {
		this.subCode = subCode;
	}
	public String getReferenceId() {
		return referenceId;
	}
	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}
	public String getAcknowledged() {
		return acknowledged;
	}
	public void setAcknowledged(String acknowledged) {
		this.acknowledged = acknowledged;
	}
	
	

}
