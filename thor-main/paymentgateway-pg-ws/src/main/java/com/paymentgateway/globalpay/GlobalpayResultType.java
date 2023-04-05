package com.paymentgateway.globalpay;


public enum GlobalpayResultType {

	GLOBAL001			("Success" , "000" , "Captured" , "Successful transaction."),
	GLOBAL002			("Pending" , "006" , "Transaction processing" , "PENDING"),
	GLOBAL003			("Initialized" , "006" , "Transaction processing" , "PENDING"),
	GLOBAL004			("Not Attempted" , "006" , "Transaction processing" , "PENDING");

	private GlobalpayResultType(String bankCode, String LetzPayCode, String statusMessage, String message) {
		this.bankCode = bankCode;
		this.LetzPayCode = LetzPayCode;
		this.statusMessage = statusMessage;
		this.message = message;
	}

	public static GlobalpayResultType getInstanceFromName(String code) {
		GlobalpayResultType[] statusTypes = GlobalpayResultType.values();
		for (GlobalpayResultType statusType : statusTypes) {
			if (String.valueOf(statusType.getBankCode()).toUpperCase().equalsIgnoreCase(code)) {
				return statusType;
			}
		}
		return null;
	}

	private final String bankCode;
	private final String LetzPayCode;
	private final String statusMessage;
	private final String message;

	public String getBankCode() {
		return bankCode;
	}
	
	public String getStatusMessage() {
		return statusMessage;
	}

	public String getMessage() {
		return message;
	}

	public String getLetzPayCode() {
		return LetzPayCode;
	}
}