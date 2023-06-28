package com.paymentgateway.axisbank.upi;


public enum AxisBankUpiRefundResultType {
	
	AXISBANKUPI001			("000" , "000" , "SUCCESS" , "SUCCESS."),
	AXISBANKUPI002			("A79" , "004" , "Declined" , "Refund Amount cannot be zero or negative."),
	AXISBANKUPI003			("111" , "004" , "Declined" , "Refund Limit Crossed."),
	AXISBANKUPI004			("A78" , "004" , "Declined" , "Duplicate Refund Id."),
	AXISBANKUPI005			("444" , "022" , "Failed at Acquirer" , "Internal Server Error."),
	AXISBANKUPI006			("404" , "004" , "Declined" , "Record Not Available."),
	AXISBANKUPI007			("500" , "022" , "Failed at Acquirer" , "Internal Server Error."),
	AXISBANKUPI008			("125" , "004" , "Declined" , "Validation Error."),
	AXISBANKUPI009			("303" , "004" , "Declined" , "Duplicate Transaction Id."),
	AXISBANKUPI010			("ZH" , "004" , "Declined" , "Invalid VPA."),
	AXISBANKUPI011			("NA" , "007" , "Rejected" , "Acquirer Error."),
	AXISBANKUPI012			("444" , "022" , "Failed at Acquirer" , "Internal Server Error."),
	AXISBANKUPI013			("U69" , "004" , "Declined" , "Request Expired."),
	AXISBANKUPI014			("ZA" , "010" , "Cancelled" , "Payment cancelled by User."),
	AXISBANKUPI015			("ZM" , "004" , "Declined" , "Declined due to invalid PIN.");
	
	
	private AxisBankUpiRefundResultType(String bankCode, String iPayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.iPayCode = iPayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static AxisBankUpiRefundResultType getInstanceFromName(String code) {
		AxisBankUpiRefundResultType[] statusTypes = AxisBankUpiRefundResultType.values();
		for (AxisBankUpiRefundResultType statusType : statusTypes) {
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