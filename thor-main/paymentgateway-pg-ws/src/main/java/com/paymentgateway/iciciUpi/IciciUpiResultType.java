package com.paymentgateway.iciciUpi;

/**
 * @author Amitosh
 *
 */
public enum IciciUpiResultType {

	ICICIUPI0001("92", "006", "Sent to Bank", "Transaction Initiated"),
	ICICIUPI0002("0", "000", "Captured", "Transaction successful"),
	ICICIUPI0003("1", "007", "Rejected", "User profile not found"),
	ICICIUPI0004("4", "004", "Declined", "Response parsing error"),
	ICICIUPI0005("9", "007", "Rejected", "Transaction rejected"),
	ICICIUPI0006("10", "002", "Denied by risk", "Insufficient data"),
	ICICIUPI0007("99", "004", "Declined", "Transaction cannot be processed"),
	ICICIUPI0008("5000", "021", "Invalid", "Invalid Request"),
	ICICIUPI0009("5001", "021", "Invalid", "Invalid Merchant  ID"),
	ICICIUPI0010("5002", "008", "Duplicate", "Duplicate MerchantTranId"),
	ICICIUPI0011("5003", "004", "Declined", "Merchant Transaction Id is mandatory"),
	ICICIUPI0012("5004", "021", "Invalid", "Invalid Data"),
	ICICIUPI0013("5005", "004", "Declined", "Collect By date should be greater than or equal to Current date"),
	ICICIUPI0014("5006", "004", "Declined", "Merchant TranId is not available"),
	ICICIUPI0015("5007", "366", "Rejected", "Virtual address not present"),
	ICICIUPI0018("5008", "004", "Declined", "PSP is not registered"),
	ICICIUPI0019("5009", "004", "Declined", "Service unavailable. Please try  later."),
	ICICIUPI0020("5011", "008", "Duplicate", "This transaction is already processed - Duplicate Transaction"),
	ICICIUPI0021("5012", "008", "Duplicate", "Request has already been initiated for this transaction - Offline Duplicate Transaction"),
	ICICIUPI0022("5013", "366", "Rejected", "Invalid VPA"),
	ICICIUPI0023("5014", "004", "Declined", "Insufficient Amount"),
	ICICIUPI0024("8000", "021", "Invalid", "Invalid Encrypted Request"),
	ICICIUPI0025("8001", "021", "Invalid", "JSON IS EMPTY"),
	ICICIUPI0026("8002", "021", "Invalid", "INVALID_JSON"),
	ICICIUPI0027("8003", "021", "Invalid", "INVALID_FIELD FORMAT OR LENGTH"),
	ICICIUPI0028("8004", "021", "Invalid", "MISSING_REQUIRED_FIELD_DATA"),
	ICICIUPI0029("8005", "021", "Invalid", "MISSING_REQUIRED_FIELD"),
	ICICIUPI0030("8006", "021", "Invalid", "INVALID_FIELD_LENGTH"),
	ICICIUPI0031("8007", "021", "Invalid", "Invalid JSON, OPEN CURLY BRACE MISSING"),
	ICICIUPI0032("8008", "021", "Invalid", "Invalid JSON, END CURLY BRACE MISSING"),
	ICICIUPI0033("8009", "001", "Error", "Internal Server Error"),
	ICICIUPI0034("8010", "001", "Error", "Internal Service Failure"),
	ICICIUPI0035("8011", "001", "Error", "INTERNAL_SERVICE_FAILURE");

	private IciciUpiResultType(String bankCode, String lPayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.lPayCode = lPayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static IciciUpiResultType getInstanceFromCode(String code) {
		IciciUpiResultType[] statusTypes = IciciUpiResultType.values();
		for (IciciUpiResultType statusType : statusTypes) {
			if (String.valueOf(statusType.getBankCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}

	private final String bankCode;
	private final String lPayCode;
	private final String statusCode;
	private final String message;

	public String getBankCode() {
		return bankCode;
	}

	public String getlPayCode() {
		return lPayCode;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}
}