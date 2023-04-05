package com.paymentgateway.commons.user;

public class AccountStatement {
	
	private String txnDate;
	private String remarks;
	private String amount;
	private String valueDate;
	private String txnType;
	private String txnId;
	private String balance;
	
	public String getTxnDate() {
		return txnDate;
	}
	public void setTxnDate(String txnDate) {
		this.txnDate = txnDate;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getValueDate() {
		return valueDate;
	}
	public void setValueDate(String valueDate) {
		this.valueDate = valueDate;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
	public String getBalance() {
		return balance;
	}
	public void setBalance(String balance) {
		this.balance = balance;
	}
	public Object[] csvMethodForDownloadFile() {
		  Object[] objectArray = new Object[7];
		  
		 
		  
		  objectArray[0] = txnId;
		  objectArray[1] = txnDate;
		  objectArray[2] = txnType;
		  objectArray[3] = valueDate;
		  objectArray[4] = amount;
		  objectArray[5] = remarks;
		  objectArray[6] = balance;
		  
		  return objectArray;
		}
	
	
	

}
