package com.paymentgateway.vepay;

import java.util.ArrayList;
import java.util.List;

import com.paymentgateway.commons.util.Helper;
import com.paymentgateway.commons.util.PropertiesManager;

public enum VepayMopType {

	// NetBanking
	DEMO_BANK("Demo Bank", "1025", "63"),
	ICICI_BANK("Icici Bank", "1013", "5"),
	HDFC_BANK("Hdfc Bank", "1004", "6"),
	AXIS_BANK("Axis Bank", "1005", "49"), 
	YES_BANK("Yes Bank", "1001", "47"),
	STATE_BANK_OF_INDIA("State Bank Of India", "1030", "211"),
	KOTAK_BANK("Kotak Bank", "1012", "47"),
	JAMMU_AND_KASHMIR_BANK("Jammu And Kashmir Bank", "1041", "48"),
	FEDERAL_BANK("Federal Bank", "1027", "299"),
	SOUTH_INDIAN_BANK("South Indian Bank", "1045", "601"),
	COSMOS_BANK("COSMOS Bank", "1104", "1001"),
	EQUITAS_BANK("Equitas Bank", "1131", "1003"),
	CATHOLIC_SYRIAN_BANK("Catholic Syrian Bank", "1094", "1004"),
	IDFC_FIRST_BANK("IDFC FIRST Bank Limited", "1111", "1179"),
	UNION_BANK_OF_INDIA("Union Bank Of India", "1038", "1183"),
	BANK_OF_MAHARASHTRA("Bank Of Maharashtra", "1064", "1187"),
	//BHARAT_CO_OP_BANK("Bharat Co-Op Bank", "2003", "1188"),
	CANARA_BANK("Canara Bank", "1055", "1189"),
	CENTRAL_BANK_OF_INDIA("Central Bank Of India", "1063", "1190"),
	CITY_UNION_BANK("City Union Bank", "1060", "1191"),
	DENA_BANK("Dena Bank", "1110", "1193"),
	DEUTSCHE_BANK("Deutsche Bank", "1026", "1194"),
	DEVELOPMENT_CREDIT_BANK("Development Credit Bank", "1040", "1195"),
	DHANLAXMI_BANK("Dhanlaxmi Bank", "1105", "1196"),
	INDIAN_BANK("Indian Bank", "1069", "1197"),
	INDIAN_OVERSEAS_BANK("Indian Overseas Bank", "1049", "1198"),
	INDUSIND_BANK("Indusind Bank", "1054", "1199"),
	IDBI_BANK("IDBI Bank", "1003", "1200"), 
	JANATA_SAHKARI_BANK("Janata Sahakari Bank Pune", "1072", "1201"),
	KARNATAKA_BANK_LTD("Karnatka Bank Ltd", "1032", "1202"),
	//KARUR_VYSYA_CORPORATE_NETBANKING("Karur Vysya - Corporate Netbanking", "1048", "1203"),
	LAKSHMI_VILAS_BANK_NETBANKING("Lakshmi Vilas Bank NetBanking", "1095", "1205"),
	NAINITAL_BANK("Nainital Bank", "1157", "1206"),
	PUNJAB_AND_MAHARASHTRA_CO_OPERATIVE_BANK_LIMITED("Punjab And Maharashtra Co-operative Bank Limited", "1120", "1207"),
	PUNJAB_AND_SIND_BANK("Punjab and Sind Bank", "1108", "1208"),
	PUNJAB_NATIONAL_BANK_RETAIL_BANKING("Punjab National Bank - Retail Banking", "1107", "1210"),
	PUNJAB_NATIONAL_BANK_CORPORATE_ACCOUNTS("Punjab National Bank Corporate Accounts", "1096", "1209"),
	SARASWAT_BANK("SaraSwat Bank", "1106", "1211"),
	SHAMRAO_VITHAL_CO_OPERATIVE_BANK_LTD("Shamrao Vithal Co-operative Bank Ltd", "1115", "1212"),
	SYNDICATE_BANK("Syndicate Bank", "1098", "1213"),
	TAMILNAD_MERCANTILE_BANK("Tamilnad Mercantile Bank", "1065", "1214"),
	KARUR_VYSYA_BANK("KarurVysya Bank", "1048", "1215"),
	UCO_BANK("UCO Bank", "1103", "1217"),
	UNION_BANK_CORPORATE_NETBANKING("Union Bank - Corporate Netbanking", "1158", "1218"),
	BANK_OF_INDIA("Bank Of India", "1009", "1220"),
	PAYTM_BANK("Paytm Bank", "1155", "1309"),
	
	MOBIKWIK_WALLET("MobikwikWallet", "MWL", "3"),
	OLAMONEY_WALLET("OlaMoneyWallet", "OLAWL", "46"),
	FREECHARGE_WALLET("FreeChargeWallet", "FCWL", "59"),
	JIO_MONEY_WALLET("JioMoneyWallet", "JMWL", "61"),
	OXYZEN_WALLET("OxyzenWallet", "OXWL", "7"),
	PAYTM_WALLET("PaytmWallet", "PPL", "1352"),
	PHONE_PE_WALLET("PhonePeWallet", "PPWL", "2181");
	
	
	private final String bankName;
	private final String code;
	private final String bankCode;

	private VepayMopType(String bankName, String code, String bankCode) {
		this.bankName = bankName;
		this.code = code;
		this.bankCode = bankCode;
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

	public static VepayMopType getInstance(String name) {
		VepayMopType[] mopTypes = VepayMopType.values();
		for (VepayMopType mopType : mopTypes) {
			if (mopType.getBankName().equals(name)) {
				return mopType;
			}
		}
		return null;
	}

	public static List<VepayMopType> getGetMopsFromSystemProp(String mopsList) {

		List<String> mopStringList = (List<String>) Helper.parseFields(PropertiesManager.propertiesMap.get(mopsList));

		List<VepayMopType> mops = new ArrayList<VepayMopType>();

		for (String mopCode : mopStringList) {
			VepayMopType mop = getmop(mopCode);
			mops.add(mop);
		}
		return mops;
	}

	public static String getmopName(String mopCode) {
		VepayMopType mopType = VepayMopType.getmop(mopCode);
		if (mopType == null) {
			return "";
		}
		return mopType.getBankName();
	}

	public static String getBankCode(String code) {
		VepayMopType mopType = VepayMopType.getmop(code);
		if (mopType == null) {
			return "";
		}
		return mopType.getBankCode();
	}

	public static VepayMopType getmop(String mopCode) {
		VepayMopType mopObj = null;
		if (null != mopCode) {
			for (VepayMopType mop : VepayMopType.values()) {
				if (mopCode.equals(mop.getCode().toString())) {
					mopObj = mop;
					break;
				}
			}
		}
		return mopObj;
	}

	public static String getMopTypeName(String mopCode) {
		String moptType = null;
		if (null != mopCode) {
			for (VepayMopType mop : VepayMopType.values()) {
				if (mopCode.equals(mop.getBankName().toString())) {
					moptType = mop.getCode();
					break;
				}
			}
		}
		return moptType;
	}

	public static VepayMopType getInstanceIgnoreCase(String name) {
		VepayMopType[] mopTypes = VepayMopType.values();
		for (VepayMopType mopType : mopTypes) {
			if (mopType.getBankName().equalsIgnoreCase(name)) {
				return mopType;
			}
		}
		return null;
	}
}
