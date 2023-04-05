package com.paymentgateway.floxypay;


public enum FloxypayResultType {

	FLOXY001			("FAILED" , "007" , "Transaction Failed" , "Failed"),
	FLOXY002			("PENDING" , "006" , "Transaction processing" , "PENDING");

	private FloxypayResultType(String bankCode, String LetzPayCode, String statusMessage, String message) {
		this.bankCode = bankCode;
		this.LetzPayCode = LetzPayCode;
		this.statusMessage = statusMessage;
		this.message = message;
	}

	public static FloxypayResultType getInstanceFromName(String code) {
		FloxypayResultType[] statusTypes = FloxypayResultType.values();
		for (FloxypayResultType statusType : statusTypes) {
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