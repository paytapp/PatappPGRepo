package com.paymentgateway.billDesk;

public enum BillDeskBankCode {
	//Netbanking
	//ANDHRA_BANK ("Andhra Bank", "1091", "ADB"),
	BANK_OF_BRAODA_CORPORATE_ACCOUNTS("Bank of Baroda Corporate Accounts", "1092","BBC"),
	BANK_OF_BARODA_RETAIL_ACCOUNTS("Bank of Baroda", "1093","BBR"),
	BANK_OF_MAHARASHTRA ("Bank Of Maharashtra", "1064", "BOM"),
	CENTRAL_BANK_OF_INDIA ("Central Bank Of India", "1063","CBI"),
	CANARA_BANK ("Canara Bank", "1055", "CNB"),
	COSMOS_BANK ("COSMOS Bank", "1104", "COB"),
	//CORPORATION_BANK ("Corporation Bank", "1034", "CRP"),
	CATHOLIC_SYRIAN_BANK("Catholic Syrian Bank", "1094", "CSB"),
	DEUTSCHE_BANK ("Deutsche Bank", "1026" ,"DBK"),
	PUNJAB_NATIONAL_BANK_CORPORATE_ACCOUNTS("Punjab National Bank Corporate Accounts", "1096","CPN"),
	DEVELOPMENT_CREDIT_BANK ("Development Credit Bank", "1040","DCB"),
	//DENA_BANK ("Dena Bank", "1110", "DEN"),
	DHANA_LAKSHMI_BANK	("Dhanalakshmi Bank","1105", "DLB"),
	FEDERAL_BANK ("Federal Bank", "1027", "FBK"),
	INDUSIND_BANK ("IndusInd Bank", "1054", "IDS"),
	INDIAN_BANK ("Indian Bank", "1069", "INB"),
	INDIAN_OVERSEAS_BANK ("Indian Overseas Bank", "1049", "IOB"),
	JAMMU_AND_KASHMIR_BANK ("Jammu And Kashmir Bank", "1041", "JKB"),
	KARNATAKA_BANK_LTD ("Karnatka Bank Ltd", "1032", "KBL"),
	KARUR_VYSYA_BANK ("Karur Vysya Bank", "1048", "KVB"),
	LAKSHMI_VILAS_BANK_NETBANKING("Lakshmi Vilas Bank", "1095","LVR"),
	ORIENTAL_BANK_OF_COMMERCE ("Oriental Bank Of Commerce", "1042", "OBC"),
	PUNJAB_NATIONAL_BANK ("Punjab National Bank", "1107", "PNB"),
	PUNJAB_AND_SIND_BANK ("Punjab & Sind Bank", "1108", "PSB"),
	STANDARD_CHARTERED_BANK("Standard Chartered Bank", "1097", "SCB"),
	SOUTH_INDIAN_BANK ("South Indian Bank", "1045", "SIB"),
	SHAMRAO_VITTHAL_BANK ("Shamrao Vitthal Co-operative Bank", "1115", "SVC"),
	SARASWAT_BANK ("Saraswat Bank", "1106", "SWB"),
	//SYNDICATE_BANK ("Syndicate Bank", "1098", "SYD"),
	TAMILNAD_MERCANTILE_BANK ("Tamilnad Mercantile Bank", "1065", "TMB"),
	UNION_BANK_OF_INDIA ("Union Bank Of India", "1038", "UBI"),
	UCO_BANK ("UCO Bank", "1103", "UCO"),
	UNITED_BANK_OF_INDIA ("United Bank Of India", "1046", "UNI"),
	//VIJAYA_BANK ("Vijaya Bank", "1044", "VJB"),
	YES_BANK ("Yes Bank", "1001", "YBK"),
	JANTA_SAHAKARI_BANK ("Janta Sahakari Bank", "1116", "JSB"),
	BANDHAN_BANK ("Bandhan bank", "1109", "BDN"),
	IDFC_BANK ("IDFC Bank", "1111", "IDN"),
	NKGSB_BANK ("NKGSB Bank", "1113", "NKB"),
	RBL_BANK ("RBL Bank", "1114", "RBL"),
	YES_BANK_CORPORATE("Yes Bank Corporate", "1022", "YBC"),//we have to change in mob also
	ALLAHABAD_BANK("Allahabad Bank", "1117", "ALB"),
	CITY_UNION_BANK ("City Union Bank", "1060","CUB"),
	ICICI_BANK 		("ICICI bank", "1013" , "ICI" ),
	HDFC_BANK ("HDFC Bank", "1004", "HDF"),
	STATE_BANK_OF_INDIA ("State Bank Of India", "1030", "SBI"),
	AXIS_BANK		("AXIS bank", "1005" , "UTI"),
	KOTAK_BANK("Kotak Bank", "1012" ,"162"),
	ANDHRA_BANK_CORPORATE("Andhra Bank Corporate","1118","ADC"),
	BANK_OF_BAHRAIN_AND_KUWAIT("Bank of Bahrain and kuwait","1043","BBK"),
	INDUSTRIAL_DEVELOPMENT_BANK_OF_INDIA("IDBI Bank","1003","IDB"),
	KARNATAKA_GRAIMA_BANK("Karnataka Graima Bank","1119","PKB"),
	PANJAB_AND_MAHARASHTRA_CORPORATE_BANK("Panjab and Maharashtra Corporate Bank","1120","PMC"),
	TAMIL_NADU_STATE_COORPORATIVE_BANK("Tamil Nadu State Coorporative Bank","1121","TNC"),
	TJSB_BANK("TJSB Bank","1122","TJB"),
	KALYAN_JANTA_SAHAKARI_BANK ("Kalyan Janta Sahakari Bank", "1123", "KJB"),
	MEHSANA_URBAN_COORPORATIVE_BANK("Mehsana Urban Coorporative Bank","1124","MSB"),
	DBS("DBS","1112","DBS"),
	RBL_BANK_LIMITED_COORPORATIVE_BANKING ("RBL Bank Limited Coorporative Banking", "1125", "RTC"),
	SHAMRAO_VITTHAL_COORPORATIVE_BANK_CORPORATE ("Shamrao Vitthal Co-operative Bank Corporate", "1126", "SV2"),
	DHANLAKSHMI_BANK_CORPORATE("Dhanlakshmi Bank Corporate","1127", "DL2"),
	BASSIEN_CATHOLIC_COORPORATIVE_BANK("Bassien Catholic Coorporative Bank","1128","BCB"),
	PNB_YUVA_NETBANKING("PNB Yuva NetBanking","1129","PNY"),
	THE_KALUPUR_COMMERCIAL("The Kalupur Commercial Coorporative Bank","1130","KLB"),
	EQUITAS_SMALL_FINANCE_BANK("Equitas Small Finance Bank","1131","EQB"),
	THANE_BHARAT_SAHAKARI_BANK_LTD("Thane Bharat Sahakari Bank Ltd","1132","TBB"),
	SURYODAY_SMALL_FINANCE_BANK("Suryoday Small Finance Bank","1133","SRB"),
	ESAF_SMALL_FINANCE_BANK("ESAF Small Finance Bank","1134","ESF"),
	VARACHHA_COORPORATIVE_BANK_LIMITED("Varachha Coorporative Bank Limited","1135","VRB"),
	NORTH_EAST_SMALL_FINANCE_BANK_LTD("North East Small finance Bank Ltd","1136","NEB"),
	CORPORATION_BANK_CORPORATE("Corporation Bank Corporate", "1137","CR2"),
	BARCLAYS_CORPORATE_BANKING("Barclays Corporate Banking","1138","BRL"),
	ZOROASTRIAN_COORPORATIVE_BANK("Zoroastrian Coorporative Bank","1139","ZOB"),
	AU_SMALL_FINANCE_BANK("Au Small Finance Bank","1140","AUB"),
	BANDHAN_BANK_CORPORATE_BANKING("Bandhan Bank Corporate Banking","1141","BDC"),
	FINCARE_BANK("Fincare Bank","1142","FNC"),
	ANDHRA_PRAGATHI_GRAMEENA_BANK ("Andhra Pragathi Grameena Bank", "1143", "APG"),
	SHIVALIK_MERCANTILE_COORPORATIVE_BANK_LTD("Shivalik Mercantile Coorporative Bank Ltd","1144","SHB");
	
	
	private BillDeskBankCode(String bankName, String code, String bankCode) {
		this.code = code;
		this.bankCode = bankCode;
		this.bankName = bankName;
		
	}

	public static BillDeskBankCode getInstanceFromCode(String code) {
		BillDeskBankCode[] statusTypes = BillDeskBankCode.values();
		for (BillDeskBankCode statusType : statusTypes) {
			if (String.valueOf(statusType.getCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}
	
	public static String getBankCode(String code) {
		String bankCode = null;
		if (null != code) {
			for (BillDeskBankCode bank : BillDeskBankCode.values()) {
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
