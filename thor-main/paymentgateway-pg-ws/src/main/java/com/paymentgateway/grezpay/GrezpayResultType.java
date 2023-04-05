package com.paymentgateway.grezpay;


public enum GrezpayResultType {

	GRZ001			("Failed" , "007" , "Transaction Failed" , "Failed"),
	GRZ002			("PENDING" , "006" , "Transaction processing" , "PENDING");

	private GrezpayResultType(String bankCode, String LetzPayCode, String statusMessage, String message) {
		this.bankCode = bankCode;
		this.LetzPayCode = LetzPayCode;
		this.statusMessage = statusMessage;
		this.message = message;
	}

	public static GrezpayResultType getInstanceFromName(String code) {
		GrezpayResultType[] statusTypes = GrezpayResultType.values();
		for (GrezpayResultType statusType : statusTypes) {
			if (String.valueOf(statusType.getBankCode()).toUpperCase().equals(code)) {
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