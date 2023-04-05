package com.paymentgateway.payphi;

/**
 * @author Shaiwal
 *
 */

public enum PayphiResultType {

	PAYPHI001			("0000" , "000" , "Captured" , "Successful transaction."),
	PAYPHI002			("000" , "000" , "Captured" , "Successful transaction."),
	PAYPHI003			("R1000" , "010" , "Cancelled" , "Transaction cancelled by user."),
	PAYPHI004			("037" , "010", "Cancelled","Transaction cancelled by user"),
	PAYPHI005			("P0039" , "007" , "Rejected" , "Transaction Not available in system"),
	PAYPHI006			("039" , "007", "Rejected","Transaction rejected");
	
	private PayphiResultType(String bankCode, String iPayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.iPayCode = iPayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static PayphiResultType getInstanceFromName(String code) {
		PayphiResultType[] statusTypes = PayphiResultType.values();
		for (PayphiResultType statusType : statusTypes) {
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