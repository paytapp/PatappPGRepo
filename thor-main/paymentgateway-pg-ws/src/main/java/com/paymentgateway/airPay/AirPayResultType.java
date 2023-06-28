package com.paymentgateway.airPay;


public enum AirPayResultType {

	AIRPAY001			("200" , "000" , "SUCCESS" , "Captured"),
	AIRPAY002			("211" , "006" , "Transaction processing" , "Processing"),
	AIRPAY003			("400" , "022" , "Failed at Acquirer" , "Failed"),
	AIRPAY004			("401" , "022" , "Dropped" , "Failed"),
	AIRPAY005			("402" , "010" , "Cancel" , "Cancelled"),
	AIRPAY006			("403" , "010" , "Incomplete" , "Cancelled"),
	AIRPAY007			("405" , "010" , "Bounced" ,"Cancelled"),
	AIRPAY008			("503" , "007" , "No Records" , "Rejected");	

	private AirPayResultType(String bankCode, String LetzPayCode, String statusMessage, String message) {
		this.bankCode = bankCode;
		this.LetzPayCode = LetzPayCode;
		this.statusMessage = statusMessage;
		this.message = message;
	}

	public static AirPayResultType getInstanceFromName(String code) {
		AirPayResultType[] statusTypes = AirPayResultType.values();
		for (AirPayResultType statusType : statusTypes) {
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