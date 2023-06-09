package com.paymentgateway.cashfree;

import java.util.ArrayList;
import java.util.List;

import com.paymentgateway.commons.util.Helper;
import com.paymentgateway.commons.util.PropertiesManager;

public enum CashfreeMopType {

	
	//CASHFREE LIVE
		ALLAHABAD_BANK("Allahabad Bank", "1117","3001"),
		KOTAK_BANK ("Kotak Bank", "1012","3032"),
		ANDHRA_BANK ("Andhra Bank", "1091","3002"),
		AXIS_BANK("Axis Bank", "1005", "3003"),
		BANK_OF_INDIA("Bank Of India", "1009", "3006"),
		CITY_UNION_BANK("City Union Bank", "1060", "3012"),
		BANK_OF_BRAODA_CORPORATE_ACCOUNTS ("Bank of Baroda Corporate Accounts", "1092","3060"),
		BANK_OF_BARODA_RETAIL_ACCOUNTS ("Bank of Baroda Retail Accounts","1093","3005"),
		BANK_OF_MAHARASHTRA ("Bank Of Maharashtra", "1064","3007"),
		CENTRAL_BANK_OF_INDIA("Central Bank Of India", "1063","3011"),
		CANARA_BANK("Canara Bank", "1055", "3009"),
		DBS_BANK_LTD ("DBS Bank Ltd", "1112", "3017"),
		DBS_BANK_LTD_PERSONAL ("DCB Bank Personal", "1148", "3018"),
		CORPORATION_BANK("Corporation Bank", "1034","3013"),
		CATHOLIC_SYRIAN_BANK("Catholic Syrian Bank", "1094" ,"3010"),
		DEUTSCHE_BANK("Deutsche Bank", "1026","3016"),
		DENA_BANK("Dena Bank", "1110","3015"),
		DHANLAXMI_BANK("Dhanlaxmi Bank", "1105","3019"),
		FEDERAL_BANK("Federal Bank", "1027","3020"),
		IDBI_BANK("IDBI Bank", "1146","3023"),
		INDUSIND_BANK("Indusind Bank", "1054", "3028"),
		INDIAN_BANK("Indian Bank", "1069", "3026"),
		INDIAN_OVERSEAS_BANK("Indian Overseas Bank", "1049", "3027"), 
		JAMMU_AND_KASHMIR_BANK("Jammu And Kashmir Bank", "1041", "3029"),
		KARNATAKA_BANK_LTD("Karnatka Bank Ltd", "1032", "3030"), 
		KARUR_VYSYA_BANK("KarurVysya Bank", "1048", "3031"),
		LAKSHMI_VILAS_BANK_NETBANKING("Lakshmi Vilas Bank NetBanking", "1095", "3033"),
		ORIENTAL_BANK_OF_COMMERCE("Oriental Bank Of Commerce", "1042", "3035"),
		PUNJAB_NATIONAL_BANK_RETAIL_BANKING("Punjab National Bank Retail Banking", "1107", "3038"),
		PUNJAB_NATIONAL_BANK_CORPORATE_ACCOUNTS("Punjab National Bank Corporate Accounts", "1096" ,"3065"),
		PUNJAB_AND_SIND_BANK("Punjab and Sind Bank", "1108", "3037"),
		STANDARD_CHARTERED_BANK("Standard Chartered Bank", "1097", "3043"),
		SOUTH_INDIAN_BANK("South Indian Bank", "1045", "3042"),
		SHAMRAO_VITHAL_CO_OPERATIVE_BANK_LTD("Shamrao Vithal Co operative Bank Ltd", "1115", "3075"),
		SARASWAT_BANK("SaraSwat Bank", "1106", "3040"),
		SYNDICATE_BANK("Syndicate Bank", "1098", "3050"),
		TAMILNAD_MERCANTILE_BANK("Tamilnad Mercantile Bank", "1065", "3052"),
		TN_STATE_COOP_BANK("Tamil Nadu State Co-operative Bank", "1121", "3051"),
		UNION_BANK_OF_INDIA("Union Bank Of India", "1038", "3055"),
		UCO_BANK("UCO Bank", "1103", "3054"),
		UNITED_BANK_OF_INDIA("United Bank Of India", "1046", "3056"),
		VIJAYA_BANK("Vijaya Bank", "1044", "3057"),
		YES_BANK("Yes Bank", "1001", "3058"),
		BANDHAN_BANK ("Bandhan Bank", "1109", "3079"),
		IDFC_FIRST_BANK("IDFC FIRST Bank Limited", "1111", "3024"),
		RBL_BANK("RBL Bank Limited", "1114", "3039"),
		EQUITAS_BANK("Equitas Bank", "1131", "3076"),
		STATE_BANK_OF_INDIA("State Bank Of India", "1030", "3044"),
		HDFC_BANK("Hdfc Bank", "1004", "3021"),
		ICICI_BANK("Icici Bank", "1013", "3022"),
		//CASHFREE_DEMO_BANK("Cashfree Demo Bank", "3333", "3333"),
		
		FREECHARGE_WALLET("FreeChargeWallet", "FCWL", "4001"),
		MOBIKWIK_WALLET("MobikwikWallet", "MWL", "4002"),
		OLAMONEY_WALLET("OlaMoneyWallet", "OLAWL", "4003"),
		JIO_MONEY_WALLET("JioMoneyWallet", "JMWL", "4004"),
		AIRTEL_PAY_WALLET("AirtelPayWallet", "AWL", "4006"),
		PAYTM_WALLET("PaytmWallet", "PPL", "4007"),
		AMAZON_PAY_WALLET("AmazonPayWallet", "APWL", "4008"),
		PHONE_PE_WALLET("PhonePeWallet", "PPWL", "4009");
		

	private final String bankName;
	private final String code;
	private final String bankCode;
	
	private CashfreeMopType(String bankName, String code, String bankCode){
		this.bankName = bankName;
		this.code = code;
		this.bankCode = bankCode;
	}

	public String getBankName() {
		return bankName;
	}

	public String getCode(){
		return code;
	}

	public String getBankCode() {
		return bankCode;
	}

	public static CashfreeMopType getInstance(String name){
		CashfreeMopType[] mopTypes = CashfreeMopType.values();
		for(CashfreeMopType mopType : mopTypes){
			if(mopType.getBankName().equals(name)){
				return mopType;
			}
		}		
		return null;
	}

	
	public static List<CashfreeMopType> getGetMopsFromSystemProp(String mopsList){

		List<String> mopStringList= (List<String>) Helper.parseFields(PropertiesManager.propertiesMap.get(mopsList));

		List<CashfreeMopType> mops = new ArrayList<CashfreeMopType>();

		for(String mopCode:mopStringList){
			CashfreeMopType mop = getmop(mopCode);
			mops.add(mop);
		}
		return mops;
	}

	public static String getmopName(String mopCode){
		CashfreeMopType mopType = CashfreeMopType.getmop(mopCode);		
		if(mopType == null) {
			return "";
		}
		return mopType.getBankName();
	}
	
	
	public static String getBankCode(String code){
		CashfreeMopType mopType = CashfreeMopType.getmop(code);		
		if(mopType == null) {
			return "";
		}
		return mopType.getBankCode();
	}

	public static CashfreeMopType getmop(String mopCode){
		CashfreeMopType mopObj = null;
		if(null!=mopCode){
			for(CashfreeMopType mop:CashfreeMopType.values()){
				if(mopCode.equals(mop.getCode().toString())){
					mopObj=mop;
					break;
				}
			}
		}
		return mopObj;
	}	
	
	public static String getMopTypeName(String mopCode) {
		String moptType = null;
		if (null != mopCode) {
			for (CashfreeMopType mop : CashfreeMopType.values()) {
				if (mopCode.equals(mop.getBankName().toString())) {
					moptType = mop.getCode();
					break;
				}
			}
		}
		return moptType;
	}
	
	public static CashfreeMopType getInstanceIgnoreCase(String name){
		CashfreeMopType[] mopTypes = CashfreeMopType.values();
		for(CashfreeMopType mopType : mopTypes){
			if(mopType.getBankName().equalsIgnoreCase(name)){
				return mopType;
			}
		}		
		return null;
	}
}
