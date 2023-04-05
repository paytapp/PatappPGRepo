package com.paymentgateway.apexPay;

public enum ApexPayBankCode {
	//Netbanking
	ANDHRA_BANK 									("Andhra Bank", "1091", "1091"),
	ALLAHABAD_BANK 									("Allahabad Bank", "1117", "1110"),
	AXIS_BANK										("AXIS bank", "1005" , "1005"),
	AXIS_CORPORATE_BANK								("Axis Corporate Bank", "1099" , "1099"),
	
	BANK_OF_BAHRAIN_AND_KUWAIT						("Bank of Bahrain and kuwait","1043","1043"),
	BANK_OF_BRAODA_CORPORATE_ACCOUNTS				("Bank of Baroda Corporate Accounts", "1092","1092"),
	BANK_OF_BARODA_RETAIL_ACCOUNTS					("Bank of Baroda", "1093","1093"),
	BANK_OF_INDIA 									("Bank Of India", "1009","1009"),
	BANK_OF_MAHARASHTRA 							("Bank Of Maharashtra", "1064", "1064"),
	CANARA_BANK 									("Canara Bank", "1055", "1055"),
	CATHOLIC_SYRIAN_BANK							("Catholic Syrian Bank", "1094", "1094"),
	CENTRAL_BANK_OF_INDIA 							("Central Bank Of India", "1063","1063"),
	CITIBANK 										("CitiBank", "1010", "1010"),
	CITY_UNION_BANK 								("City Union Bank", "1060","1060"),
	CORPORATION_BANK 								("Corporation Bank", "1034", "1034"),
	COSMOS_BANK 									("COSMOS Bank", "1104", "1103"),
	DEVELOPMENT_CREDIT_BANK 						("Development Credit Bank", "1040","1040"),
	
	DEUTSCHE_BANK 									("Deutsche Bank", "1026" ,"1026"),
	DHANA_LAKSHMI_BANK								("Dhanalakshmi Bank","1105", "1070"),
	EQUITAS_SMALL_FINANCE_BANK						("Equitas Small Finance Bank","1131","1106"),
	FEDERAL_BANK 									("Federal Bank", "1027", "1027"),
	HDFC_BANK 										("HDFC Bank", "1004", "1004"),
	HSBC_BANK 										("HSBC Bank", "1102", "1102"),
	ICICI_BANK 										("ICICI bank", "1013" , "1013" ),
	ICICI_CORPORATE_BANK 							("ICICI Corporate Bank", "1100","1100"),
	IDFC_BANK 										("IDFC Bank", "1111", "1107"),
	INDIAN_BANK 									("Indian Bank", "1069", "1069"),
	INDIAN_OVERSEAS_BANK 							("Indian Overseas Bank", "1049", "10491"),
	INDUSIND_BANK 									("IndusInd Bank", "1054", "1054"),
	INDUSTRIAL_DEVELOPMENT_BANK_OF_INDIA			("IDBI Bank","1003","1003"),
	ING_VYSYA_BANK 									("ING Vysya Bank", "1062", "1062"),
	JAMMU_AND_KASHMIR_BANK 							("Jammu And Kashmir Bank", "1041", "1041"),
	JANATA_SAHKARI_BANK								("Janata Sahakari Bank", "1072", "1072"),
	KARNATAKA_BANK_LTD 								("Karnatka Bank Ltd", "1032", "1032"),
	KARUR_VYSYA_BANK 								("Karur Vysya Bank", "1048", "1048"),
	KOTAK_BANK										("Kotak Bank", "1012" ,"1012"),
	LAKSHMI_VILAS_BANK_NETBANKING					("Lakshmi Vilas Bank", "1095","1095"),
	ORIENTAL_BANK_OF_COMMERCE 						("Oriental Bank Of Commerce", "1042", "1042"),
	PUNJAB_AND_SIND_BANK 							("Punjab & Sind Bank", "1108", "1296"),
	PUNJAB_NATIONAL_BANK 							("Punjab National Bank", "1107", "1002"),
	PUNJAB_NATIONAL_BANK_CORPORATE_ACCOUNTS			("Punjab National Bank Corporate Accounts", "1096","1101"),
	RBL_BANK 										("RBL Bank", "1114", "1053"),
	SARASWAT_BANK 									("Saraswat Bank", "1106", "1056"),
	SOUTH_INDIAN_BANK 								("South Indian Bank", "1045", "1045"),
	STANDARD_CHARTERED_BANK							("Standard Chartered Bank", "1097", "1097"),
	STATE_BANK_OF_BIKANER_AND_JAIPUR 				("State Bank Of Bikaner And Jaipur", "1050", "1050"),
	STATE_BANK_OF_HYDERABAD							("StateBank Of Hyderabad", "1039", "1039"), 
	STATE_BANK_OF_INDIA 							("State Bank Of India", "1030", "1030"),
	STATE_BANK_OF_MYSORE 							("State Bank Of Mysore", "1037", "1037"), 
	STATE_BANK_OF_PATIALA 							("State Bank Of Patiala", "1068", "1068"),
	STATE_BANK_OF_TRAVANCORE 						("State Bank Of Travancore", "1061", "1061"),
	SYNDICATE_BANK 									("Syndicate Bank", "1098", "1098"),
	TAMILNAD_MERCANTILE_BANK 						("Tamilnad Mercantile Bank", "1065", "1065"),
	UCO_BANK 										("UCO Bank", "1103", "1103"),
	UNION_BANK_OF_INDIA 							("Union Bank Of India", "1038", "1038"),
	UNITED_BANK_OF_INDIA 							("United Bank Of India", "1046", "1046"),
	VIJAYA_BANK 									("Vijaya Bank", "1044", "1044"),
	YES_BANK 										("Yes Bank", "1001", "1001");
	
	private ApexPayBankCode(String bankName, String code, String bankCode) {
		this.code = code;
		this.bankCode = bankCode;
		this.bankName = bankName;
		
	}

	public static ApexPayBankCode getInstanceFromCode(String code) {
		ApexPayBankCode[] statusTypes = ApexPayBankCode.values();
		for (ApexPayBankCode statusType : statusTypes) {
			if (String.valueOf(statusType.getCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}
	
	public static String getBankCode(String code) {
		String bankCode = null;
		if (null != code) {
			for (ApexPayBankCode bank : ApexPayBankCode.values()) {
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
