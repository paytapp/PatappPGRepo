package com.paymentgateway.commons.util;

public enum EnachDCIssuerType {

	KOTAK_MAHINDRA_BANK("9540", "Kotak Mahindra Bank"),
	UJJIVAN_SMALL_FINANCE_BANK_LTD("9610", "UJJIVAN SMALL FINANCE BANK LTD"),
	YES_BANK("9570", "YES BANK"),
	INDUSIND_BANK("9630", "INDUSIND BANK"),
	EQUITAS_SMALL_FINANCE_BANK_LTD("9650", "EQUITAS SMALL FINANCE BANK LTD"),
	SOUTH_INDIAN_BANK("9680", "SOUTH INDIAN BANK"),
	ICICI_Bank("10010", "ICICI Bank"),
	IDFC_FIRST_BANK("10840", "IDFC FIRST BANK"),
	HDFC_BANK("10850", "HDFC BANK"),
	BANK_OF_MAHARASHTRA("10860", "Bank of Maharashtra"),
	AU_SMALL_FINANCE_BANK("10870", "Au Small Finance Bank"),
	DEUTSCHE_BANK_AG("10930", "DEUTSCHE BANK AG"),
	FEDERAL_BANK("11030", "FEDERAL BANK"),
	PUNJAB_NATIONAL_BANK("11140", "PUNJAB NATIONAL BANK"),
	KARNATAKA_BANK_LTD("11150", "KARNATAKA BANK LTD"),
	STATE_BANK_OF_INDIA("11060", "STATE BANK OF INDIA"),
	CITI_BANK("11220", "CITI BANK"),
	RBL_BANK_LTD("11250", "RBL BANK LTD"),
	DCB_BANK_LTD("11410", "DCB BANK LTD"),
	PAYTM_PAYMENTS_BANK_LTD("11690", "PAYTM PAYMENTS BANK LTD"),
	DHANALAXMI_BANK("11920", "DHANALAXMI BANK"),
	DBS_BANK_INDIA_LTD("11970", "DBS BANK INDIA LTD"),
	TAMILNAD_MERCANTILE_BANK_LTD("12030", "TAMILNAD MERCANTILE BANK LTD"),
	AXIS_BANK("12800", "Axis Bank"),
	BANK_OF_BARODA("12870", "Bank of Baroda"),
	JANA_SMALL_FINANCE_BANK_LTD("13630", "JANA SMALL FINANCE BANK LTD"),
	STANDARD_CHARTERED_BANK("14320", "STANDARD CHARTERED BANK");
	private final String code;
	private final String name;

	private EnachDCIssuerType(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public static String getIssuerName(String issuerCode) {
		String issuerType = null;
		if (null != issuerCode) {
			for (EnachDCIssuerType issuer : EnachDCIssuerType.values()) {
				if (issuerCode.equals(issuer.getCode().toString())) {
					issuerType = issuer.getName();
					break;
				}
			}
		}
		return issuerType;
	}

	public static String getIssuerCode(String issuerName) {
		String issuerType = null;
		if (null != issuerName) {
			for (EnachDCIssuerType issuer : EnachDCIssuerType.values()) {
				if (issuerName.equals(issuer.name().toString())) {
					issuerType = issuer.getCode();
					break;
				}
			}
		}
		return issuerType;
	}
	
	
	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
	
}
