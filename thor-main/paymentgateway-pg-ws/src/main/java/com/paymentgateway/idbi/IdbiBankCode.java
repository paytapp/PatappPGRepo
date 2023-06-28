package com.paymentgateway.idbi;

public enum IdbiBankCode {
	//Netbanking
	AXIS_BANK		("Axis bank", "1005" , "201"),
	ICICI_BANK 		("ICICI bank", "1007" , "411" ),
	CATHOLIC_SYRIAN_BANK	("Catholic Syrian Bank", "1094", "CSB");
	
	
	
	
	private IdbiBankCode(String bankName, String code, String bankCode) {
		this.code = code;
		this.bankCode = bankCode;
		this.bankName = bankName;
		
	}

	public static IdbiBankCode getInstanceFromCode(String code) {
		IdbiBankCode[] statusTypes = IdbiBankCode.values();
		for (IdbiBankCode statusType : statusTypes) {
			if (String.valueOf(statusType.getCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}
	
	public static String getBankCode(String code) {
		String bankCode = null;
		if (null != code) {
			for (IdbiBankCode bank : IdbiBankCode.values()) {
				if (code.equals(bank.getCode().toString())) {
					bankCode = bank.getBankCode();
					break;
				}
			}
		}
		return bankCode;
	}

	private final String code;
	private final String bankCode;
	private final String bankName;
	
	
	public String getBankCode() {
		return bankCode;
	}

	public String getBankName() {
		return bankName;
	}
	
	public String getCode() {
		return code;
	}
}
