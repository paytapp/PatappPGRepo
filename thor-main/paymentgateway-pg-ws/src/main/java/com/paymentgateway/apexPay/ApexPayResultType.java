package com.paymentgateway.apexPay;


public enum ApexPayResultType {
	
	LETZPAYCHECKOUT000			("000" , "000" , "Captured" , "Payment is successful"),
	LETZPAYCHECKOUT001			("007" , "007" , "Rejected" , "Payment Rejected"),
	LETZPAYCHECKOUT002			("013" , "013" , "Declined" , "Payment Declined"),
	LETZPAYCHECKOUT003			("010" , "010" , "Cancelled" , "Payment Cancelled"),
	LETZPAYCHECKOUT004			("022" , "022" , "Failed at Acquirer" , "Failed at Acquirer"),
	LETZPAYCHECKOUT005			("003" , "003" , "Timeout" , "Timeout"),
	LETZPAYCHECKOUT006			("008" , "008" , "Duplicate" , "Duplicate");
	
	private ApexPayResultType(String bankCode, String iPayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.iPayCode = iPayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static ApexPayResultType getInstanceFromName(String code) {
		ApexPayResultType[] statusTypes = ApexPayResultType.values();
		for (ApexPayResultType statusType : statusTypes) {
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