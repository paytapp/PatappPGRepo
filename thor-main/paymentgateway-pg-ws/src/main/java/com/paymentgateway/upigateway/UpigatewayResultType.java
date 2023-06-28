package com.paymentgateway.upigateway;


public enum UpigatewayResultType {

	UPG001			("created" , "006" , "Transaction processing" , "PENDING");

	private UpigatewayResultType(String bankCode, String LetzPayCode, String statusMessage, String message) {
		this.bankCode = bankCode;
		this.LetzPayCode = LetzPayCode;
		this.statusMessage = statusMessage;
		this.message = message;
	}

	public static UpigatewayResultType getInstanceFromName(String code) {
		UpigatewayResultType[] statusTypes = UpigatewayResultType.values();
		for (UpigatewayResultType statusType : statusTypes) {
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