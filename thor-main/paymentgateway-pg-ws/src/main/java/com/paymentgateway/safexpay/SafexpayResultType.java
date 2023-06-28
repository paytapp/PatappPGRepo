package com.paymentgateway.safexpay;

public enum SafexpayResultType {

	SAFEXPAY01("00001", "004", "Declined", "Invalid Merchant ID."),
	SAFEXPAY02("00002", "004", "Declined", "Encryption key not found"),
	SAFEXPAY03("00003", "004", "Declined", "Encryption key not found"),
	SAFEXPAY04("00004", "004", "Declined", "Incorrect Order Number."),
	SAFEXPAY05("00005", "004", "Declined", "Transaction amount is less than minimum allowed limit."),
	SAFEXPAY06("00006", "007", "Rejected", "Daily Transaction Amount Limit Exceeded"),
	SAFEXPAY07("00007", "007", "Rejected", "Weekly Transaction Amount Limit Exceeded"),
	SAFEXPAY08("00008", "007", "Rejected", "Monthly Transaction Amount Limit Exceeded"),
	SAFEXPAY09("00009", "007", "Rejected", "Quarterly Transaction Amount Limit Exceeded"),
	SAFEXPAY10("00055", "007", "Rejected", "Invalid Transaction ID. Please try again with valid transaction details"),
	SAFEXPAY11("00053", "007", "Rejected", "The given amount is not valid");

	private SafexpayResultType(String bankCode, String iPayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.iPayCode = iPayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static SafexpayResultType getInstanceFromName(String code) {
		SafexpayResultType[] statusTypes = SafexpayResultType.values();
		for (SafexpayResultType statusType : statusTypes) {
			if (String.valueOf(statusType.getBankCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}

	private final String bankCode;
	private final String iPayCode;
	private final String statusCode;
	private final String message;

	public String getBankCode() {
		return bankCode;
	}

	public String getiPayCode() {
		return iPayCode;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}
}