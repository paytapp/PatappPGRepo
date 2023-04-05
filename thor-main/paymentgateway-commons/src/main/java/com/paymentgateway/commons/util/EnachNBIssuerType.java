package com.paymentgateway.commons.util;

public enum EnachNBIssuerType {

	KOTAK_MAHINDRA_BANK("9530", "Kotak Mahindra Bank"),
	IDFC_BANK_LTD("9520", "IDFC Bank Ltd"),
	AXIS_BANK("9480", "Axis Bank"),
	CENTRAL_BANK_OF_INDIA("9510", "Central Bank of India"),
	PUNJAB_NATIONAL_BANK("9500", "Punjab National Bank"),
	INDUS_IND_Bank("9470", "INDUS IND Bank"),
	ICICI_BANK("9460", "ICICI Bank"),
	UJJIVAN_SMALL_FINANCE_BANK_LTD("9600", "UJJIVAN SMALL FINANCE BANK LTD"),
	BANK_OF_BARODA("9490", "Bank of Baroda"),
	YES_BANK("9560", "YES BANK"),
	IDBI_BANK("9620", "IDBI BANK"),
	TAMILNAD_MERCANTILE_BANK_LTD("9640", "TAMILNAD MERCANTILE BANK LTD"),
	HDFC_BANK_LTD("9660", "HDFC BANK LTD"),
	INDIAN_OVERSEAS_BANK("9670", "INDIAN OVERSEAS BANK"),
	RBL_BANK_LTD("9780", "RBL BANK LTD"),
	BANK_OF_MAHARASHTRA("9820", "Bank Of Maharashtra"),
	DEUTSCHE_BANK_AG("9830", "DEUTSCHE BANK AG"),
	PAYTM_PAYMENTS_BANK_LTD("9880", "PAYTM PAYMENTS BANK LTD"),
	CITY_UNION_BANK_LTD("9890", "CITY UNION BANK LTD"),
	FEDERAL_BANK("10000", "FEDERAL BANK"),
	EQUITAS_SMALL_FINANCE_BANK_LTD("10020", "EQUITAS SMALL FINANCE BANK LTD"),
	KARNATAKA_BANK_LTD("10970", "KARNATAKA BANK LTD"),
	STANDARD_CHARTERED_BANK("11020", "STANDARD CHARTERED BANK"),
	CANARA_BANK("11130", "CANARA BANK"),
	STATE_BANK_OF_INDIA("11050", "STATE BANK OF INDIA"),
	DHANALAXMI_BANK("11230", "DHANALAXMI BANK"),
	UNION_BANK_OF_INDIA("11390", "UNION BANK OF INDIA"),
	SOUTH_INDIAN_BANK("11940", "SOUTH INDIAN BANK"),
	HSBC("11950", "HSBC"),
	DBS_BANK_INDIA_LTD("11960", "DBS BANK INDIA LTD"),
	THE_COSMOS_COOPERATIVE_BANK_LTD("11240", "THE COSMOS CO-OPERATIVE BANK LTD"),
	PUNJAB_AND_SIND_BANK("12840", "PUNJAB AND SIND BANK"),
	KARUR_VYSA_BANK("12860", "KARUR VYSA BANK"),
	JANA_SMALL_FINANCE_BANK_LTD("13640", "JANA SMALL FINANCE BANK LTD"),
	CITI_BANK("13970", "CITI BANK"),
	BANDHAN_BANK_LTD("13980", "BANDHAN BANK LTD"),
	UCO("14330", "UCO"),
	INDIAN_BANK("13900", "INDIAN BANK"),
	DCB_BANK_LTD("14760", "DCB BANK LTD");
	

	private final String code;
	private final String name;

	private EnachNBIssuerType(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public static String getIssuerName(String issuerCode) {
		String issuerType = null;
		if (null != issuerCode) {
			for (EnachNBIssuerType issuer : EnachNBIssuerType.values()) {
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
			for (EnachNBIssuerType issuer : EnachNBIssuerType.values()) {
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
