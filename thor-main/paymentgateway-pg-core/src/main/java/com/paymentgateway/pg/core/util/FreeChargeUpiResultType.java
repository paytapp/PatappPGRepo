package com.paymentgateway.pg.core.util;


public enum FreeChargeUpiResultType {

	FREECHARGE001			("E018" , "001" , "Failed at Acquirer" , "Application error occured"),
	FREECHARGE002			("E005" , "021" , "Invalid" , "Invalid checksum/Checksum mismatch"),
	FREECHARGE003			("ER-009" , "021" , "Invalid" , "Merchant id is blank"),
	FREECHARGE004			("ER-037" , "004" , "Declined" , "Merchant Transaction id length exceeded."),
	FREECHARGE005			("ER-034" , "021" , "Invalid" , "Merchant transaction id is blank"),
	FREECHARGE006			("ER-002" , "021" , "Invalid" , "Invalid amount"),
	FREECHARGE007			("ER-073" , "021" , "Invalid" ,"Payment mode is blank or null"),
	FREECHARGE008			("ER-074" , "021" , "Invalid" , "Payment mode is invalid"),
	FREECHARGE009			("ER-075" , "021" , "Invalid" , "User VPA is blank or null"),
	FREECHARGE010			("ER-076" , "022" , "Declined" , "Invalid user vpa"),
	FREECHARGE011			("ES-0058" , "022" , "Declined" , "Merchant Status is INVALID for transaction"),
	FREECHARGE012			("E004" , "022" , "Declined" , "Duplicate merchant transaction id"),
	FREECHARGE013			("INITIATED","006" , "Processing","Pending or Abandoned"),;
	
		
	private FreeChargeUpiResultType(String bankCode, String PGCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.PGCode = PGCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static FreeChargeUpiResultType getInstanceFromName(String name) {
		FreeChargeUpiResultType[] statusTypes = FreeChargeUpiResultType.values();
		for (FreeChargeUpiResultType statusType : statusTypes) {
			if (String.valueOf(statusType.getStatusCode()).equals(name)) {
				return statusType;
			}
		}
		return null;
	}
	
	public static FreeChargeUpiResultType getInstanceFrombankCode(String code) {
		FreeChargeUpiResultType[] statusTypes = FreeChargeUpiResultType.values();
		for (FreeChargeUpiResultType statusType : statusTypes) {
			if (String.valueOf(statusType.getBankCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}

	private final String bankCode;
	private final String PGCode;
	private final String statusCode;
	private final String message;

	public String getBankCode() {
		return bankCode;
	}
	
	public String getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}

	public String getPGCode() {
		return PGCode;
	}
}