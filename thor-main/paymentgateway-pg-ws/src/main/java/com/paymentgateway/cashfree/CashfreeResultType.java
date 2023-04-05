package com.paymentgateway.cashfree;


public enum CashfreeResultType {
	CASHFREE001		("SUCCESS","000","Captured","Successful Payment"),
	CASHFREE002		("FAILED","022","Failed at Acquirer","Payment Failed"),
	CASHFREE003		("PENDING","007","Rejected","Payment Processing Timed Out"),
	CASHFREE004		("CANCELLED","010" , "Cancelled by user","Payment cancelled by user"),
	CASHFREE005		("FLAGGED","022","Failed at Acquirer","Payment successful but kept on hold by risk system"),
	CASHFREE006		("Invalid inputs","007" , "Rejected","VALIDATION ERROR"),
	CASHFREE007		("CANCELLED","010" , "Cancelled by user","No Payment Attempted"),
	CASHFREE008		("ERROR","007" , "Rejected","Refund Failed");

	

	private CashfreeResultType(String bankCode, String iPayCode, String statusName, String message) {
		this.bankCode = bankCode;
		this.iPayCode = iPayCode;
		this.statusName = statusName;
		this.message = message;
	}

	public static CashfreeResultType getInstanceFromName(String code) {
		CashfreeResultType[] statusTypes = CashfreeResultType.values();
		for (CashfreeResultType statusType : statusTypes) {
			if (String.valueOf(statusType.getBankCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}

	private final String bankCode;
	private final String iPayCode;
	private final String message;
	private final String statusName;
	public String getBankCode() {
		return bankCode;
	}

	public String getiPayCode() {
		return iPayCode;
	}


	public String getMessage() {
		return message;
	}

	public String getStatusName() {
		return statusName;
	}
}
