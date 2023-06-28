package com.paymentgateway.digitalsolution;


public enum DigitalsolutionResultType {

	DIGSOL001			("Failed" , "007" , "Transaction Failed" , "Failed"),
	DIGSOL002			("PENDING" , "006" , "Transaction processing" , "PENDING");

	private DigitalsolutionResultType(String bankCode, String LetzPayCode, String statusMessage, String message) {
		this.bankCode = bankCode;
		this.LetzPayCode = LetzPayCode;
		this.statusMessage = statusMessage;
		this.message = message;
	}

	public static DigitalsolutionResultType getInstanceFromName(String code) {
		DigitalsolutionResultType[] statusTypes = DigitalsolutionResultType.values();
		for (DigitalsolutionResultType statusType : statusTypes) {
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