package com.paymentgateway.crm.action;

import com.paymentgateway.commons.action.AbstractSecureAction;

public class PayoutAction extends AbstractSecureAction{

	private static final long serialVersionUID = 248257633099171076L;
	
	private String accountNo;
	private String acquirer;
	private String ifscCode;
	private String userType;
	private String amount;
	private String currencyCode;
	private String txnType;
	private String beneficiaryCode;
		
	
	public String execute() {
				
		return SUCCESS;
		
	}

	public String getAccountNo() {
		return accountNo;
	}


	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}


	public String getAcquirer() {
		return acquirer;
	}


	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}


	public String getIfscCode() {
		return ifscCode;
	}


	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}


	public String getUserType() {
		return userType;
	}


	public void setUserType(String userType) {
		this.userType = userType;
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


	public String getTxnType() {
		return txnType;
	}


	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}


	public String getBeneficiaryCode() {
		return beneficiaryCode;
	}


	public void setBeneficiaryCode(String beneficiaryCode) {
		this.beneficiaryCode = beneficiaryCode;
	}
	
	

}
