package com.paymentgateway.toshanidigital;

public enum ToshanidigitalResultType {

	UPG001("created", "006", "Transaction processing", "PENDING");

	private ToshanidigitalResultType(String bankCode, String LetzPayCode, String statusMessage, String message) {
		this.bankCode = bankCode;
		this.LetzPayCode = LetzPayCode;
		this.statusMessage = statusMessage;
		this.message = message;
	}

	public static ToshanidigitalResultType getInstanceFromName(String code) {
		ToshanidigitalResultType[] statusTypes = ToshanidigitalResultType.values();
		for (ToshanidigitalResultType statusType : statusTypes) {
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