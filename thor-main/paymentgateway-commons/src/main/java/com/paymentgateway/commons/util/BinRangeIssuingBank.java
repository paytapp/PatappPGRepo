package com.paymentgateway.commons.util;

public enum BinRangeIssuingBank {
	
	
 HDFC("HDFC", "HDFC Bank"), ICICI("ICICI", "ICICI Bank"), SBI("SBI", "SBI Bank"), VIJAYA ("VIJAYA",
			"VIJAYA Bank"), AXIS ("AXIS", "AXIS Bank"), ALLAHABAD ("ALLAHABAD", "ALLAHABAD Bank"), CANARA ("CANARA", "CANARA Bank");
	
	private final String name;
	private final String code;

	private BinRangeIssuingBank(String name, String code) {
		this.name = name;
		this.code = code;
	}

	
	
	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public static BinRangeIssuingBank getInstance(String name) {
		BinRangeIssuingBank[] binRangeIssuingBank = BinRangeIssuingBank.values();
		for (BinRangeIssuingBank binRangeIssuing : binRangeIssuingBank) {
			if (binRangeIssuing.getName().toString().equals(name)) {
				return binRangeIssuing;
			}
		}
		return null;
	}
}
