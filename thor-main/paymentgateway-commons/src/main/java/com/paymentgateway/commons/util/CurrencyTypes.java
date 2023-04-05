package com.paymentgateway.commons.util;

/**
 * @author Shaiwal
 *
 */
public enum CurrencyTypes {

	// NodalPaymentTypes
	INR("INR", "356");
	/*UPI("UPI", "UPI"), 
	FT("FT", "FT"), 
	IMPS("IMPS", "IMPS")*/

	private final String code;
	private final String name;

	private CurrencyTypes(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static CurrencyTypes getInstancefromCode(String currencyCode) {
		CurrencyTypes currencyType = null;
		for (CurrencyTypes currency : CurrencyTypes.values()) {
			if (currencyCode.equals(currency.getCode().toString())) {
				currencyType = currency;
				break;
			}
		}
		return currencyType;
	}

	public static CurrencyTypes getInstancefromName(String currencyName) {
		CurrencyTypes currencyType = null;
		for (CurrencyTypes currency : CurrencyTypes.values()) {

			if (currencyName.equals(currency.getName())) {
				currencyType = currency;
				break;
			}
		}
		return currencyType;
	}
	public static String getNamefromCode(String currencyCode) {
		String currencyType = null;
		for (CurrencyTypes currency : CurrencyTypes.values()) {
			if (currencyCode.equals(currency.getName().toString())) {
				currencyType = currency.getCode();
				break;
			}
		}
		return currencyType;
	}
	
	public static String getCurrencyCodeFromName(String currencyName) {
		String currencyType = null;
		for (CurrencyTypes currency : CurrencyTypes.values()) {
			if (currencyName.equals(currency.toString())) {
				currencyType = currency.getName();
				break;
			}
		}
		return currencyType;
	}
	
}