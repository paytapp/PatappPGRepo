package com.paymentgateway.axisbank.upi;


public enum AxisBankUpiStatusEnqResultType {
	
	AXISBANKUPI001			("00" , "000" , "SUCCESS" , "Deemed."),
	AXISBANKUPI002			("01" , "006" , "Processing" , "Pending Case Request from Bank."),
	AXISBANKUPI003			("U69" , "004" , "Declined" , "Transaction Expired."),
	AXISBANKUPI004			("ZA" , "007" , "Rejected" , "Case Rejected by Bank."),
	AXISBANKUPI005			("U48" , "022" , "Failed at Acquirer" , "Transaction Failed.");
	
	
	private AxisBankUpiStatusEnqResultType(String bankCode, String iPayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.iPayCode = iPayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static AxisBankUpiStatusEnqResultType getInstanceFromName(String code) {
		AxisBankUpiStatusEnqResultType[] statusTypes = AxisBankUpiStatusEnqResultType.values();
		for (AxisBankUpiStatusEnqResultType statusType : statusTypes) {
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