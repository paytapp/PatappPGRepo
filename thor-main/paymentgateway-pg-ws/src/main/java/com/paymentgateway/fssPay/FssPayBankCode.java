package com.paymentgateway.fssPay;

public enum FssPayBankCode {
	//Netbanking
	HDFC_BANK ("HDFC Bank", "1004", "1007"),
	JAMMU_AND_KASHMIR_BANK ("Jammu And Kashmir Bank", "1041", "1015"),
	AXIS_BANK		("AXIS bank", "1005" , "2001"),
	FEDERAL_BANK ("Federal Bank", "1027", "1029"),
	KARNATAKA_BANK_LTD ("Karnatka Bank Ltd", "1032", "1133"),
	CORPORATION_BANK ("Corporation Bank", "1034", "1135"),
	INDIAN_BANK ("Indian Bank", "1069", "1143"),
	YES_BANK ("Yes Bank", "1001", "2004"),
	CENTRAL_BANK_OF_INDIA ("Central Bank Of India", "1063", "1147"),
	KOTAK_BANK ("Kotak Bank", "1012", "1148"),
	ORIENTAL_BANK_OF_COMMERCE ("Oriental Bank Of Commerce", "1042", "1154"),
	UNITED_BANK_OF_INDIA ("United Bank Of India", "1046", "1212"),
	INDIAN_OVERSEAS_BANK ("Indian Overseas Bank", "1049", "1213"),
	UNION_BANK_OF_INDIA ("Union Bank Of India", "1038", "1216"),
	CANARA_BANK ("Canara Bank", "1055", "1224"),
	BANK_OF_MAHARASHTRA ("Bank Of Maharashtra", "1064", "1229"),
	CATHOLIC_SYRIAN_BANK("Catholic Syrian Bank", "1094", "1272"),
	DHANA_LAKSHMI_BANK	("Dhanalakshmi Bank","1105", "1273"),
	ANDHRA_BANK ("Andhra Bank", "1091", "1378"),
	VIJAYA_BANK ("Vijaya Bank", "1044", "1379"),
	SARASWAT_BANK ("Saraswat Bank", "1106", "1380"),
	PUNJAB_NATIONAL_BANK ("Punjab National Bank", "1107", "1381"),
	UCO_BANK ("UCO Bank", "1103", "1383"),
	PUNJAB_AND_SIND_BANK ("Punjab & Sind Bank", "1108", "1421"),
	INDUSIND_BANK ("IndusInd Bank", "1054", "1431"),
	LAKSHMI_VILAS_BANK_NETBANKING("Lakshmi Vilas Bank", "1095", "1433"),
	TAMILNAD_MERCANTILE_BANK ("Tamilnad Mercantile Bank", "1065", "1439"),
	BANDHAN_BANK ("Bandhan bank", "1109", "1441"),
	BANK_OF_BARODA_RETAIL_ACCOUNTS("Bank of Baroda", "1093", "1442"),
	CITIBANK ("CitiBank", "1010", "1443"),
	DENA_BANK ("Dena Bank", "1110", "1444"),
	IDFC_BANK ("IDFC Bank", "1111", "1445"),
	INDUSTRIAL_DEVELOPMENT_BANK_OF_INDIA ("IDBI Bank", "1003", "1446"),
	ING_VYSYA_BANK ("ING Vysya Bank", "1062", "1447"),
	COSMOS_BANK ("COSMOS Bank", "1104", "1448"),
	DBS_BANK ("DBS Bank", "1112", "1449"),
	NKGSB_BANK ("NKGSB Bank", "1113", "1450"),
	RBL_BANK ("RBL Bank", "1114", "1451"),
	SHAMRAO_VITTHAL_BANK ("Shamrao Vitthal Co-operative Bank", "1115", "1452"),
	STANDARD_CHARTERED_BANK("Standard Chartered Bank", "1097", "1453"),
	SYNDICATE_BANK ("Syndicate Bank", "1098", "1460"),
	KARUR_VYSYA_BANK ("Karur Vysya Bank", "1048", "1461"),
	SOUTH_INDIAN_BANK ("South Indian Bank", "1045", "1462"),
	DEUTSCHE_BANK ("Deutsche Bank", "1026" ,"1463"),
	JANTA_SAHAKARI_BANK ("Janta Sahakari Bank", "1116", "2002"),
	ALLAHABAD_BANK("Allahabad Bank", "1117", "1464"),
	STATE_BANK_OF_INDIA ("State Bank Of India", "1030", "1032"), 
	ICICI_BANK 		("ICICI bank", "1013" , "2003" ),
	
	//Wallet
	AMAZON_PAY		("Amazon pay", "APWL" , "3001"),
	AIRTEL_MONEY	("Airtel Money", "AWL" , "3008"),
	FREE_CHARGE		("Free Charge", "FCWL" , "3002"),
	//GOOGLE_PAY		("Googlepay", "GPWL" , ""),
	//ITZ_CASH		("Itz Cash", "ICWL" , ""),
	JIO_MONEY		("Jio Money", "JMWL" , "3004"),
	MOBIKWIK		("MobiKwik", "MWL" , "3006"),
	//M_PESA			("Mpesa", "MPWL" , ""),
	OLA_MONEY		("Ola money", "OLAWL" , "3005"),
	//OXYZEN_WALLET	("Oxyzen Wallet", "OXWL" , ""),
	PAYTM			("Paytm", "PPL", "3003"),
	PHONE_PE		("Phonepe", "PPWL" , "3009"),
	//SBI_BUDDY		("SBI Buddy ", "SBWL" , ""),
	PAYZAPP			("PayZapp", "PZP", "3007");
	//ZIP_CASH		("Zipcash", "ZCWL" , "");
	
	
	private FssPayBankCode(String bankName, String code, String bankCode) {
		this.code = code;
		this.bankCode = bankCode;
		this.bankName = bankName;
		
	}

	public static FssPayBankCode getInstanceFromCode(String code) {
		FssPayBankCode[] statusTypes = FssPayBankCode.values();
		for (FssPayBankCode statusType : statusTypes) {
			if (String.valueOf(statusType.getCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}
	
	public static String getBankCode(String code) {
		String bankCode = null;
		if (null != code) {
			for (FssPayBankCode bank : FssPayBankCode.values()) {
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