package com.paymentgateway.crm.action;

import com.paymentgateway.commons.action.AbstractSecureAction;


public class SettlementApiAction extends AbstractSecureAction{

	private static final long serialVersionUID = -1722238293623573804L;
	
	private String acquirer;
	private String accountNo;
	private String txnType;
	private String customerId;
	private String amount;
	private String currencyCode;
	private String beneficiaryCode;
	private String user;
	
	
	public String execute() {
		
		
		return SUCCESS;
		
	}
	public String getAcquirer() {
		return acquirer;
	}
	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
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
	public String getBeneficiaryCode() {
		return beneficiaryCode;
	}
	public void setBeneficiaryCode(String beneficiaryCode) {
		this.beneficiaryCode = beneficiaryCode;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getAccountNo() {
		return accountNo;
	}
	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

}
