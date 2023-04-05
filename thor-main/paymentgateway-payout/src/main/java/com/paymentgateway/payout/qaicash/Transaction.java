package com.paymentgateway.payout.qaicash;

import org.json.JSONObject;

import com.paymentgateway.commons.exception.SystemException;

public class Transaction {
	private String orderId;
	private String amount;
	private String currency;
	private String dateTime;
	private String language;
	private String userId;
	private String payoutMethod;
	private String redirectUrl;
	private String callbackUrl;
	private String beneficiaryName;
	private String bank;
	private String accountNumber;
	private String messageAuthenticationCode;
	private String bankIFSC;
	private String transactionId;
	private String dateCreated;
	private String status;
	private String dateUpdated;
	private String notes;
	private String instrumentId;

	public Transaction(String response) throws SystemException {

		JSONObject responseJson = new JSONObject(response);

		if (responseJson.has("orderId")) {
			setOrderId(responseJson.get("orderId").toString());
		}
		if (responseJson.has("transactionId")) {
			setTransactionId(responseJson.get("transactionId").toString());
		}
		if (responseJson.has("payoutMethod")) {
			setPayoutMethod(responseJson.get("payoutMethod").toString());
		}

		if (responseJson.has("status")) {
			setStatus(responseJson.get("status").toString());
		}

		if (responseJson.has("notes")) {
			setNotes(responseJson.get("notes").toString());
		}

		if (responseJson.has("instrumentId")) {
			setInstrumentId(responseJson.get("instrumentId").toString());
		}

		if (responseJson.has("dateCreated")) {
			setDateCreated(responseJson.get("dateCreated").toString());
		}

		if (responseJson.has("dateUpdated")) {
			setDateUpdated(responseJson.get("dateUpdated").toString());
		}

		if (responseJson.has("userId")) {
			setUserId(responseJson.get("userId").toString());
		}

		String amount = responseJson.get("amount").toString();
		String resAmount = null;
		if (amount.contains(".")) {
			String amountSplit[] = amount.split("\\.");
			String decimalPart = amountSplit[1]; 
			
			if (decimalPart.equalsIgnoreCase("0")) {
				resAmount = amount.concat("0");
			}
			else {
				resAmount = amount;
			}
		}
		
		
		if (responseJson.has("amount")) {
			setAmount(resAmount);
		}

		if (responseJson.has("currency")) {
			setCurrency(responseJson.get("currency").toString());
		}
		
		if (responseJson.has("messageAuthenticationCode")) {
			setMessageAuthenticationCode(responseJson.get("messageAuthenticationCode").toString());
		}
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

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPayoutMethod() {
		return payoutMethod;
	}

	public void setPayoutMethod(String payoutMethod) {
		this.payoutMethod = payoutMethod;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	public String getBeneficiaryName() {
		return beneficiaryName;
	}

	public void setBeneficiaryName(String beneficiaryName) {
		this.beneficiaryName = beneficiaryName;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getMessageAuthenticationCode() {
		return messageAuthenticationCode;
	}

	public void setMessageAuthenticationCode(String messageAuthenticationCode) {
		this.messageAuthenticationCode = messageAuthenticationCode;
	}

	public String getBankIFSC() {
		return bankIFSC;
	}

	public void setBankIFSC(String bankIFSC) {
		this.bankIFSC = bankIFSC;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(String dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getInstrumentId() {
		return instrumentId;
	}

	public void setInstrumentId(String instrumentId) {
		this.instrumentId = instrumentId;
	}

}
