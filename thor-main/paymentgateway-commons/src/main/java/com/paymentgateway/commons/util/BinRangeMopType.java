package com.paymentgateway.commons.util;

public enum BinRangeMopType {
	VISA("Visa", "VI"), AMEX("Amex", "AX"), DISCOVER("Discover", "DI"), JCB("JCB", "JC"), MASTERCARD("MasterCard",
			"MC"), MAESTRO("Maestro", "MS"), DINERS("Diners", "DN"), RUPAY("Rupay", "RU"), EZEECLICK("EzeeClick", "EZ");

	private final String name;
	private final String code;

	private BinRangeMopType(String name, String code) {
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public static BinRangeMopType getInstance(String name) {
		BinRangeMopType[] binMopTypes = BinRangeMopType.values();
		for (BinRangeMopType binMopType : binMopTypes) {
			if (binMopType.getCode().toString().equals(name)) {
				return binMopType;
			}
		}
		return null;
	}
}
