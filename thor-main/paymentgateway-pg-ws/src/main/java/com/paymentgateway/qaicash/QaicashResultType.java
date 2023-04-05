package com.paymentgateway.qaicash;


public enum QaicashResultType {

	AIRPAY001			("Failed" , "007" , "Transaction Failed" , "Failed"),
	AIRPAY002			("PENDING" , "006" , "Transaction processing" , "PENDING");

	private QaicashResultType(String bankCode, String LetzPayCode, String statusMessage, String message) {
		this.bankCode = bankCode;
		this.LetzPayCode = LetzPayCode;
		this.statusMessage = statusMessage;
		this.message = message;
	}

	public static QaicashResultType getInstanceFromName(String code) {
		QaicashResultType[] statusTypes = QaicashResultType.values();
		for (QaicashResultType statusType : statusTypes) {
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