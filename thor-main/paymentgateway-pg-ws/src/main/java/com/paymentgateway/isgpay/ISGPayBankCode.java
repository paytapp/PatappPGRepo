package com.paymentgateway.isgpay;

public enum ISGPayBankCode {
	
	ALLAHABAD_BANK("Allahabad Bank", "1117", "1056"),
	ANDHRA_BANK ("Andhra Bank", "1091", "1058"),
	AXIS_BANK ("AXIS bank", "1005", "1003"),
	BANK_OF_INDIA ("Bank Of India", "1009","1012"),
	BANK_OF_MAHARASHTRA ("Bank Of Maharashtra", "1064", "1033"),
	BANK_OF_BRAODA_CORPORATE_ACCOUNTS("Bank of Baroda Corporate Accounts", "1092","1075"),
	BANK_OF_BARODA_RETAIL_ACCOUNTS("Bank of Baroda", "1093", "1076"),
	CANARA_BANK ("Canara Bank", "1055", "1030"),
	CATHOLIC_SYRIAN_BANK("Catholic Syrian Bank", "1094", "1031"),
	CENTRAL_BANK_OF_INDIA ("Central Bank Of India", "1063", "1028"),
	CITIBANK ("CitiBank", "1010", "1010"),
	CITY_UNION_BANK ("City Union Bank", "1060","1020"),
	CORPORATION_BANK ("Corporation Bank", "1034", "1004"),
	DBS_BANK ("DBS Bank", "1112", "1047"),
	DCB_BANK("DCB Bank","1148","1027"),
	DHANA_LAKSHMI_BANK	("Dhanalakshmi Bank","1105", "1038"),
	DEUTSCHE_BANK ("Deutsche Bank", "1026" ,"1024"),
	EQUITAS_SMALL_FINANCE_BANK("Equitas Small Finance Bank","1131","1063"),
	FEDERAL_BANK ("Federal Bank", "1027", "1019"),
	HDFC_BANK ("HDFC Bank", "1004", "1006"),
	ICICI_BANK 		("ICICI bank", "1013" , "1002"),
	IDBI_BANK("IDBI Bank","1146","1007"),
	IDFC_BANK ("IDFC Bank", "1111", "1073"),
	INDIAN_BANK ("Indian Bank", "1069", "1026"),
	INDUSIND_BANK ("IndusInd Bank", "1054", "1015"),
	INDIAN_OVERSEAS_BANK ("Indian Overseas Bank", "1049", "1029"),
	JAMMU_AND_KASHMIR_BANK ("Jammu And Kashmir Bank", "1041", "1001"),
	JANTA_SAHAKARI_BANK ("Janta Sahakari Bank", "1116", "1072"),
	KARUR_VYSYA_BANK ("Karur Vysya Bank", "1048", "1018"),
	KARNATAKA_BANK_LTD ("Karnatka Bank Ltd", "1032", "1008"),
	LAKSHMI_VILAS_BANK_NETBANKING("Lakshmi Vilas Bank", "1095", "1009"),
	ORIENTAL_BANK_OF_COMMERCE ("Oriental Bank Of Commerce", "1042", "1035"),
	PUNJAB_NATIONAL_BANK_CORPORATE_ACCOUNTS("Punjab National Bank Corporate Accounts", "1096", "1048"),
	PUNJAB_NATIONAL_BANK ("Punjab National Bank", "1107","1049"),
	PUNJAB_AND_SIND_BANK ("Punjab & Sind Bank", "1108", "1055"),
	RBL_BANK ("RBL Bank", "1114", "1066"),
	ROYAL_BANK_OF_SCOTLAND("Royal Bank Of Scotland","1145","1050"),
	SARASWAT_BANK ("Saraswat Bank", "1106", "1053"),
	STATE_BANK_OF_INDIA ("State Bank Of India", "1030", "1014"), 
	STANDARD_CHARTERED_BANK("Standard Chartered Bank", "1097", "1051"),
	TAMILNAD_MERCANTILE_BANK ("Tamilnad Mercantile Bank", "1065", "1044"),
	UNION_BANK_OF_INDIA ("Union Bank Of India", "1038", "1016"),
	UNITED_BANK_OF_INDIA ("United Bank Of India", "1046", "1041"),
	VIJAYA_BANK ("Vijaya Bank", "1044", "1039"),
	UCO_BANK ("UCO Bank", "1103", "1057"),
	YES_BANK ("Yes Bank", "1001", "1005");
		

	
	private ISGPayBankCode(String bankName, String code, String bankCode) {
		this.code = code;
		this.bankCode = bankCode;
		this.bankName = bankName;
	}
	
	public static ISGPayBankCode getInstanceFromCode(String code) {
		ISGPayBankCode[] statusTypes = ISGPayBankCode.values();
		for (ISGPayBankCode statusType : statusTypes) {
			if (String.valueOf(statusType.getCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}
	
	public static String getBankCode(String code) {
		String bankCode = null;
		if (null != code) {
			for (ISGPayBankCode bank : ISGPayBankCode.values()) {
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
