package com.paymentgateway.axisbank.upi;


public enum AxisBankUpiResultType {
	
	AXISBANKUPI000			("00" , "000" , "SUCCESS" , "Accepted Collect Request."),
	AXISBANKUPI001			("000" , "000" , "SUCCESS" , "SUCCESS."),
	AXISBANKUPI002			("500" , "022" , "Failed at Acquirer" , "Internal Server Error."),
	AXISBANKUPI003			("444" , "004" , "Declined" , "CheckSum Error."),
	AXISBANKUPI004			("111" , "004" , "Declined" , "Missing or Empty Parameter."),
	AXISBANKUPI005			("125" , "004" , "Declined" , "Validation Error."),
	AXISBANKUPI006			("303" , "004" , "Declined" , "Duplicate Transaction Id."),
	AXISBANKUPI007			("ZH" , "004" , "Declined" , "Invalid VPA."),
	AXISBANKUPI009			("NA" , "007" , "Rejected" , "Acquirer Error."),
	AXISBANKUPI010			("A79" , "004" , "Declined" , "Refund Amount cannot be zero or negative."),
	AXISBANKUPI011			("A78" , "004" , "Declined" , "Duplicate Refund Id."),
	AXISBANKUPI012			("444" , "022" , "Failed at Acquirer" , "Internal Server Error."),
	AXISBANKUPI013			("404" , "004" , "Declined" , "Record Not Available."),
	AXISBANKUPI014			("U69" , "004" , "Declined" , "Request Expired."),
	AXISBANKUPI015			("ZA" , "010" , "Cancelled" , "Payment cancelled by User."),
	AXISBANKUPI016			("ZM" , "004" , "Declined" , "Declined due to invalid PIN."),
	AXISBANKUPI017			("96" , "004" , "Declined" , "Timeout.");
	
	
	private AxisBankUpiResultType(String bankCode, String iPayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.iPayCode = iPayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static AxisBankUpiResultType getInstanceFromName(String code) {
		AxisBankUpiResultType[] statusTypes = AxisBankUpiResultType.values();
		for (AxisBankUpiResultType statusType : statusTypes) {
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