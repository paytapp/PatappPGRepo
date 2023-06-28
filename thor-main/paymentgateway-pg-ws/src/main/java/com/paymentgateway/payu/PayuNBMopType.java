package com.paymentgateway.payu;

import java.util.ArrayList;
import java.util.List;

import com.paymentgateway.commons.util.Helper;
import com.paymentgateway.commons.util.PropertiesManager;

public enum PayuNBMopType {

	// NetBanking

	AIRTEL_PAYMENTS_BANK("Airtel Payments Bank", "1156", "AIRNB"), 
	ANDHRA_BANK("Andhra Bank", "1091", "ADBB"),
	AXIS_BANK("Axis NB", "1005", "AXIB"), 
	BANK_OF_INDIA("Bank Of India", "1009", "BOIB"),
	BANK_OF_MAHARASHTRA("Bank Of Maharashtra", "1064", "BOMB"),
	//BHARAT_CO_OP_BANK("Bharat Co-Op Bank", "2003", "BHNB"),
	CANARA_BANK("Canara Bank", "1055", "CABB"), 
	CATHOLIC_SYRIAN_BANK("Catholic Syrian Bank", "1094", "CSBN"),
	CENTRAL_BANK_OF_INDIA("Central Bank of India", "1063", "CBIB"), 
	CITY_UNION_BANK("City Union Bank", "1060", "CUBB"),
	CORPORATION_BANK("Corporation Bank", "1034", "CRBP"), 
	COSMOS_BANK("Cosmos Bank", "1104", "CSMSNB"),
	DENA_BANK("Dena Bank", "1110", "DENN"),
	DEUTSCHE_BANK("Deutsche Bank", "1026", "DSHB"),
	DEVELOPMENT_CREDIT_BANK("Development Credit Bank", "1040", "DCBB"),
	DHANLAXMI_BANK("Dhanlaxmi Bank", "1105", "DLSB"),
	HDFC_BANK("HDFC Bank", "1004", "HDFB"),
	ICICI_BANK("ICICI", "1013", "ICIB"), 
	IDFC_FIRST_BANK("IDFC", "1111", "IDFCNB"),
	INDIAN_BANK("Indian Bank", "1069", "INDB"), 
	INDIAN_OVERSEAS_BANK("Indian Overseas Bank", "1049", "INOB"),
	INDUSIND_BANK("IndusInd Bank", "1054", "INIB"),
	IDBI_BANK("Industrial Development Bank of India", "1003", "IDBB"),
	JAMMU_AND_KASHMIR_BANK("Jammu and Kashmir Bank", "1041", "JAKB"),
	JANATA_SAHKARI_BANK("Janata Sahakari Bank Pune", "1116", "JSBNB"),
	KARNATAKA_BANK_LTD("Karnataka Bank", "1032", "KRKB"),
//	KARUR_VYSYA_CORPORATE_NETBANKING("Karur Vysya - Corporate Netbanking", "1048", "KRVBC"),
	KOTAK_BANK("Kotak Mahindra Bank", "1012", "162B"),
	LAKSHMI_VILAS_BANK_CORPORATE_NETBANKING("Lakshmi Vilas Bank - Corporate Netbanking", "1127", "LVCB"),
	LAKSHMI_VILAS_BANK_NETBANKING("Lakshmi Vilas Bank - Retail Netbanking", "1095", "LVRB"),
	NAINITAL_BANK("Nainital Bank", "1157", "TBON"),
	ORIENTAL_BANK_OF_COMMERCE("Oriental Bank of commerce", "1042", "OBCB"),
	PUNJAB_AND_MAHARASHTRA_CO_OPERATIVE_BANK_LIMITED("Punjab And Maharashtra Co-operative Bank Limited", "1120",
			"PMNB"),
	PUNJAB_AND_SIND_BANK("Punjab And Sind Bank", "1108", "PSBNB"),
	PNB_CORPORATE_BANK("Punjab National Bank - Corporate Banking", "1101", "CPNB"),
	PUNJAB_NATIONAL_BANK_RETAIL_BANKING("Punjab National Bank - Retail Banking", "1107", "PNBB"),
	SARASWAT_BANK("Saraswat bank", "1106", "SRSWT"), 
	STATE_BANK_OF_INDIA("SBI NB", "1030", "SBIB"),
	SHAMRAO_VITHAL_CO_OPERATIVE_BANK_LTD("Shamrao Vithal Co-operative Bank Ltd", "1115", "SVCNB"),
	SYNDICATE_BANK("Syndicate Bank", "1098", "SYNDB"),
	TAMILNAD_MERCANTILE_BANK("Tamilnad Mercantile Bank", "1065", "TMBB"),
	FEDERAL_BANK("The Federal Bank", "1027", "FEDB"),
	KARUR_VYSYA_BANK("The Karur Vysya Bank", "1048", "KRVB"),
	SOUTH_INDIAN_BANK("The South Indian Bank", "1045", "SOIB"),
	UCO_BANK("UCO Bank", "1103", "UCOB"),
	UNION_BANK_CORPORATE_NETBANKING("Union Bank - Corporate Netbanking", "1158", "UBIBC"),
	UNION_BANK_OF_INDIA("Union Bank Of India", "1038", "UBIB"),
	UNITED_BANK_OF_INDIA("United Bank of India", "1046", "UNIB"),
	VIJAYA_BANK("Vijaya Bank", "1044", "VJYB"),

	// wallets
	ITZ_CASH_WALLET("ItzCash", "ICWL", "ITZC"),
	AIRTEL_PAY_WALLET("Airtel Money", "AWL", "AMON"),
	FREECHARGE_WALLET("freecharge", "FCWL", "FREC"), 
	OXYZEN_WALLET("Oxigen", "OXWL", "OXICASH"),
	HDFC_PAYZAPP_WALLET("HDFC PayZapp", "PZP", "PAYZ"), 
	//AMEX_EASY_CLICK_WALLET("Amex easy click", "AECWL", "AMEXZ"),
	YESBANK_WALLET("YesPayWallet", "YBWL", "YESW"),
	OLAMONEY_WALLET("OlaMoneyWallet", "OLAWL", "OLAM"),
	PAYCASH_WALLET("paycash", "PCWL", "PAYCASH"),
	JIO_MONEY_WALLET("jio Money", "JMWL", "JIOM"),
	//CITIBANK_WALLET("Citibank Reward Points", "CBWL", "CPMC"),
	AMAZON_PAY_WALLET("Amazon Pay", "APWL", "AMZPAY"),
	PAYTM_WALLET("Paytm wallet", "PPL", "PAYTM"), 
	PHONE_PE_WALLET("PhonePe", "PPWL", "PHONEPE");

	private final String bankName;
	private final String code;
	private final String bankCode;

	private PayuNBMopType(String bankName, String code, String bankCode) {
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

	public static PayuNBMopType getInstance(String name) {
		PayuNBMopType[] mopTypes = PayuNBMopType.values();
		for (PayuNBMopType mopType : mopTypes) {
			if (mopType.getBankName().equals(name)) {
				return mopType;
			}
		}
		return null;
	}

	public static List<PayuNBMopType> getGetMopsFromSystemProp(String mopsList) {

		List<String> mopStringList = (List<String>) Helper.parseFields(PropertiesManager.propertiesMap.get(mopsList));

		List<PayuNBMopType> mops = new ArrayList<PayuNBMopType>();

		for (String mopCode : mopStringList) {
			PayuNBMopType mop = getmop(mopCode);
			mops.add(mop);
		}
		return mops;
	}

	public static String getmopName(String mopCode) {
		PayuNBMopType mopType = PayuNBMopType.getmop(mopCode);
		if (mopType == null) {
			return "";
		}
		return mopType.getBankName();
	}

	public static String getBankCode(String code) {
		PayuNBMopType mopType = PayuNBMopType.getmop(code);
		if (mopType == null) {
			return "";
		}
		return mopType.getBankCode();
	}

	public static PayuNBMopType getmop(String mopCode) {
		PayuNBMopType mopObj = null;
		if (null != mopCode) {
			for (PayuNBMopType mop : PayuNBMopType.values()) {
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
			for (PayuNBMopType mop : PayuNBMopType.values()) {
				if (mopCode.equals(mop.getBankName().toString())) {
					moptType = mop.getCode();
					break;
				}
			}
		}
		return moptType;
	}

	public static PayuNBMopType getInstanceIgnoreCase(String name) {
		PayuNBMopType[] mopTypes = PayuNBMopType.values();
		for (PayuNBMopType mopType : mopTypes) {
			if (mopType.getBankName().equalsIgnoreCase(name)) {
				return mopType;
			}
		}
		return null;
	}
}
