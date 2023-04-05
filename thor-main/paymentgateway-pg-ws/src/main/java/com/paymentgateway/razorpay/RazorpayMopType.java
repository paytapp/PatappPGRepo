package com.paymentgateway.razorpay;

import java.util.HashMap;
import java.util.Map;

public enum RazorpayMopType {

	// NetBanking
	AIRTEL_PAYMENTS_BANK("Airtel Payments Bank", "1156", "AIRP"),
	ALLAHABAD_BANK("Allahabad Bank", "1117", "ALLA"),
	ANDHRA_BANK("Andhra Bank", "1091", "ANDB"),
	BANK_OF_BARODA_RETAIL_ACCOUNTS("Bank of Baroda Retail Accounts", "1093", "BARB_R"),
	AU_SMALL_FIN_BANK ("AU small finance bank", "1140", "AUBL"),
	BANK_OF_BAHRAIN_AND_KUWAIT("Bank Of Bahrain And Kuwait", "1043", "BBKM"),
	ICICI_BANK("Icici Bank", "1013", "ICIC"), 
	HDFC_BANK("Hdfc Bank", "1004", "HDFC"),
	AXIS_BANK("Axis Bank", "1005", "UTIB"), 
	YES_BANK("Yes Bank", "1001", "YESB"),
	STATE_BANK_OF_INDIA("State Bank Of India", "1030", "SBIN"), 
	KOTAK_BANK("Kotak Bank", "1012", "KKBK"),
	JAMMU_AND_KASHMIR_BANK("Jammu And Kashmir Bank", "1041", "JAKA"), 
	FEDERAL_BANK("Federal Bank", "1027", "FDRL"),
	SOUTH_INDIAN_BANK("South Indian Bank", "1045", "SIBL"),
	EQUITAS_BANK("Equitas Bank", "1131", "ESFB"), 
	CATHOLIC_SYRIAN_BANK("Catholic Syrian Bank", "1094", "CSBK"),
	IDFC_FIRST_BANK("IDFC FIRST Bank Limited", "1111", "IDFB"),
	UNION_BANK_OF_INDIA("Union Bank Of India", "1038", "UBIN"),
	COSMOS_BANK("COSMOS Bank", "1104", "COSB"),
	DIGIBANK_BY_DBS ("Digibank by DBS", "1112", "DBSS"),
	DCB_BANK("DCB Bank", "1148", "DCBL"),
	HSBC_BANK("HSBC Bank", "1102", "HSBC"),
	FINCARE_BANK ("Fincare Bank", "1142", "FSFB"),
	CORPORATION_BANK("Corporation Bank", "1034", "CORP"),
	ESAF_BANK ("ESAF Small Finance Bank", "1134", "ESAF"),
	KALUPUR_COMM_COOP_BANK ("The Kalupur Commercial Co Operative Bank","1130", "KCCB"),
	BANK_OF_MAHARASHTRA("Bank Of Maharashtra", "1064", "MAHB"), 
	CANARA_BANK("Canara Bank", "1055", "CNRB"), 
	CENTRAL_BANK_OF_INDIA("Central Bank Of India", "1063", "CBIN"),
	CITY_UNION_BANK("City Union Bank", "1060", "CIUB"), 
	DENA_BANK("Dena Bank", "1110", "BKDN"),
	DEUTSCHE_BANK("Deutsche Bank", "1026", "DEUT"),
	DHANLAXMI_BANK("Dhanlaxmi Bank", "1105", "DLXB"), 
	INDIAN_BANK("Indian Bank", "1069", "IDIB"),
	INDIAN_OVERSEAS_BANK("Indian Overseas Bank", "1049", "IOBA"), 
	INDUSIND_BANK("Indusind Bank", "1054", "INDB"),
	IDBI_BANK("IDBI Bank", "1146", "IBKL"), 
	JANATA_SAHKARI_BANK("Janata Sahakari Bank Pune", "1072", "KJSB"),
	KARNATAKA_BANK_LTD("Karnatka Bank Ltd", "1032", "KARB"),
	MEHSANA_URBAN_COOP_BANK ("Mehsana urban Co-op Bank", "1124", "MSNU"),
	LAKSHMI_VILAS_BANK_NETBANKING("Lakshmi Vilas Bank NetBanking", "1095", "LAVB_R"),
	NE_SMALL_FIN_BANK ("North East Small Finance Bank Ltd", "1136", "NESF"),
	NKGSB_COOP_BANK("NKGSB Co op Bank", "1113", "NKGS"),
	ORIENTAL_BANK_OF_COMMERCE("Oriental Bank Of Commerce", "1042", "ORBC"),
	PUNJAB_AND_SIND_BANK("Punjab and Sind Bank", "1108", "PSIB"),
	PUNJAB_NATIONAL_BANK ("Punjab National Bank", "1107", "PUNB_R"),
	RBL_BANK("RBL Bank Limited", "1114", "RATN"),
	SARASWAT_BANK("SaraSwat Bank", "1106", "SRCB"),
	SHAMRAO_VITHAL_CO_OPERATIVE_BANK_LTD("Shamrao Vithal Co-operative Bank Ltd", "1115", "SVCB"),
	SYNDICATE_BANK("Syndicate Bank", "1098", "SYNB"),
	STATE_BANK_OF_BIKANER_AND_JAIPUR("State Bank Of Bikaner And Jaipur", "1050", "SBBJ"),
	STATE_BANK_OF_HYDERABAD("State Bank Of Hyderabad", "1039", "SBHY"),
	STATE_BANK_OF_MYSORE("State Bank Of Mysore", "1037", "SBMY"),
	STATE_BANK_OF_TRAVANCORE("State Bank Of Travancore", "1061", "SBTR"),
	STATE_BANK_OF_PATIALA("State Bank Of Patiala", "1068", "STBP"),
	STANDARD_CHARTERED_BANK("Standard Chartered Bank", "1097", "SCBL"),
	TAMILNAD_MERCANTILE_BANK("Tamilnad Mercantile Bank", "1065", "TMBL"),
	KARUR_VYSYA_BANK("KarurVysya Bank", "1048", "KVBL"), 
	UCO_BANK("UCO Bank", "1103", "UCBA"),
	BANK_OF_INDIA("Bank Of India", "1009", "BKID"),
	UNITED_BANK_OF_INDIA("United Bank Of India", "1046", "UTBI"),
	MOBIKWIK_WALLET("MobikwikWallet", "MWL", "mobikwik"),
	OLAMONEY_WALLET("OlaMoneyWallet", "OLAWL", "olamoney"),
	FREECHARGE_WALLET("FreeChargeWallet", "FCWL", "freecharge"),
	JIO_MONEY_WALLET("JioMoneyWallet", "JMWL", "jiomoney"),
	AIRTEL_PAY_WALLET("AirtelPayWallet", "AWL", "airtelmoney"),
	AMAZON_PAY_WALLET("AmazonPayWallet", "APWL", "amazonpay"),
	HDFC_PAYZAPP_WALLET("HdfcPayZappWallet", "PZP", "payzapp"),
	PHONE_PE_WALLET("PhonePeWallet", "PPWL", "phonepe");

	private final String bankName;
	private final String code;
	private final String bankCode;

	private RazorpayMopType(String bankName, String code, String bankCode) {
		this.bankName = bankName;
		this.code = code;
		this.bankCode = bankCode;
	}

	private static final Map<String, RazorpayMopType> razorpayMopTypes = new HashMap<>();

	static {
		for (RazorpayMopType mopType : RazorpayMopType.values()) {
			razorpayMopTypes.put(mopType.getCode().toUpperCase(), mopType);
		}
	}

	public static RazorpayMopType getInstanceFromCode(String code) {
		return razorpayMopTypes.get(code.toUpperCase());
	}

	public String getBankName() {
		return bankName;
	}

	public String getCode() {
		return code;
	}

	public String getBankCode() {
		return bankCode;
	}

}
