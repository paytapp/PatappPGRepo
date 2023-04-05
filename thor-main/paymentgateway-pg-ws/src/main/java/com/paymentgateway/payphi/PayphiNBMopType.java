package com.paymentgateway.payphi;

import java.util.ArrayList;
import java.util.List;

import com.paymentgateway.commons.util.Helper;
import com.paymentgateway.commons.util.PropertiesManager;


public enum PayphiNBMopType {

	// NetBanking

	ALLAHABAD_BANK("Allahabad Bank", "1117","ALLA"),
	ANDHRA_BANK("Andhra Bank", "1091", "ANDB"),
	AXIS_BANK("Axis NB", "1005", "UTIB"),
	AXIS_CORPORATE_BANK ("Axis Corporate Bank", "1099","UTIB_C"),
	BANK_OF_BAHRAIN_AND_KUWAIT ("Bank Of Bahrain And Kuwait", "1043","BBKM"),
	BANK_OF_BRAODA_CORPORATE_ACCOUNTS("Bank of Baroda Corporate Accounts", "1092","BARB_C"),
	BANK_OF_BARODA_RETAIL_ACCOUNTS("Bank of Baroda", "1093","BARB_R"),
	BANK_OF_INDIA("Bank Of India", "1009", "BKID"),
	BANK_OF_MAHARASHTRA("Bank Of Maharashtra", "1064", "MAHB"),
	CANARA_BANK("Canara Bank", "1055", "CNRB"),
	CATHOLIC_SYRIAN_BANK("Catholic Syrian Bank", "1094", "CSBK"),
	CENTRAL_BANK_OF_INDIA("Central Bank of India", "1063", "CBIN"),
	CITY_UNION_BANK("City Union Bank", "1060", "CIUB"),
	CORPORATION_BANK("Corporation Bank", "1034", "CORP"),
	COSMOS_BANK("Cosmos Bank", "1104", "COSB"),
	DCB_BANK("DCB Bank","1148","DCBL"),
	DENA_BANK("Dena Bank", "1110", "BKDN"),
	DEUTSCHE_BANK("Deutsche Bank", "1026", "DEUT"),
	DBS_BANK ("DBS Bank", "1112","DBSS"),
	DHANA_LAKSHMI_BANK	("Dhanalakshmi Bank","1105","DLXB"),
	FEDERAL_BANK ("Federal Bank", "1027","FDRL"), 
	HDFC_BANK("HDFC Bank", "1004", "HDFC"),
	ICICI_BANK("ICICI", "1013", "ICIC"),
	IDBI_BANK("IDBI Bank","1146","IBKL"),
	IDFC_FIRST_BANK("IDFC", "1107", "IDFB"),
	INDIAN_BANK("Indian Bank", "1069", "IDIB"), 
	INDIAN_OVERSEAS_BANK("Indian Overseas Bank", "1049", "IOBA"),
	INDUSIND_BANK("IndusInd Bank", "1054", "INDB"),
	JAMMU_AND_KASHMIR_BANK("Jammu and Kashmir Bank", "1041", "JAKA"),
	JANATA_SAHKARI_BANK("Janata Sahakari Bank Pune", "1072", "JSBP"),
	KARNATAKA_BANK_LTD("Karnataka Bank", "1032", "KARB"),
	KOTAK_BANK("Kotak Mahindra Bank", "1012", "KKBK"),
	LAKSHMI_VILAS_BANK_NETBANKING("Lakshmi Vilas Bank - Retail Netbanking", "1095", "LAVB_R"),
	ORIENTAL_BANK_OF_COMMERCE("Oriental Bank of commerce", "1042", "ORBC"),
	PUNJAB_AND_MAHARASHTRA_CO_OPERATIVE_BANK_LIMITED("Punjab And Maharashtra Co-operative Bank Limited", "1120","PMCB"),
	PUNJAB_AND_SIND_BANK("Punjab And Sind Bank", "1296", "PSIB"),
	PNB_CORPORATE_BANK("Punjab National Bank - Corporate Banking", "1101", "PUNB_C"),
	PUNJAB_NATIONAL_BANK_RETAIL_BANKING("Punjab National Bank - Retail Banking", "1107", "PUNB_R"),
	RBL_BANK ("RBL Bank", "1114","RATN"),
	SARASWAT_BANK("Saraswat bank", "1106", "SRCB"),
	SHAMRAO_VITHAL_CO_OPERATIVE_BANK_LTD("Shamrao Vithal Co-operative Bank Ltd", "1115", "SVCB"),
	SOUTH_INDIAN_BANK("The South Indian Bank", "1045", "SIBL"),
	STANDARD_CHARTERED_BANK("Standard Chartered Bank", "1097","SCBL"),
	STATE_BANK_OF_BIKANER_AND_JAIPUR ("State Bank Of Bikaner And Jaipur", "1050","SBBJ"),
	STATE_BANK_OF_HYDERABAD("StateBank Of Hyderabad", "1039","SBHY"), 
	STATE_BANK_OF_INDIA ("State Bank Of India", "1030","SBIN"), 
	STATE_BANK_OF_MYSORE ("State Bank Of Mysore", "1037","SBMY"), 
	STATE_BANK_OF_PATIALA ("State Bank Of Patiala", "1068","STBP"),
	STATE_BANK_OF_TRAVANCORE ("State Bank Of Travancore", "1061","SBTR"),
	SYNDICATE_BANK("Syndicate Bank", "1098", "SYNB"),
	TAMILNAD_MERCANTILE_BANK("Tamilnad Mercantile Bank", "1065", "TMBL"),
	UCO_BANK("UCO Bank", "1103", "UCBA"),
	UNION_BANK_OF_INDIA("Union Bank Of India", "1038", "UBIN"),
	UNITED_BANK_OF_INDIA("United Bank of India", "1046", "UTBI"),
	VIJAYA_BANK("Vijaya Bank", "1044", "VIJB"),
	EQUITAS_SMALL_FINANCE_BANK("Equitas Small Finance Bank","1131","EQUB"),
	ROYAL_BANK_OF_SCOTLAND("Royal Bank Of Scotland","1145","RBST"),
	BANDHAN_BANK ("Bandhan Bank", "1109","BDBL"),
	YES_BANK ("Yes Bank", "1001","YESB"),
	ESAF_SMALL_FINANCE_BANK("ESAF Small Finance Bank","1134","ESAF"),
	THE_KALUPUR_COMMERCIAL("The Kalupur Commercial Coorporative Bank","1130","KCCB"),
	CITIBANK ("CitiBank", "1010","CITI"),
	PAYTM_PAYMENTS_BANK("PAYTM PAYMENTS BANK","1155","PYTM");

	private final String bankName;
	private final String code;
	private final String bankCode;

	private PayphiNBMopType(String bankName, String code, String bankCode) {
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

	public static PayphiNBMopType getInstance(String name) {
		PayphiNBMopType[] mopTypes = PayphiNBMopType.values();
		for (PayphiNBMopType mopType : mopTypes) {
			if (mopType.getBankName().equals(name)) {
				return mopType;
			}
		}
		return null;
	}

	public static List<PayphiNBMopType> getGetMopsFromSystemProp(String mopsList) {

		List<String> mopStringList = (List<String>) Helper.parseFields(PropertiesManager.propertiesMap.get(mopsList));

		List<PayphiNBMopType> mops = new ArrayList<PayphiNBMopType>();

		for (String mopCode : mopStringList) {
			PayphiNBMopType mop = getmop(mopCode);
			mops.add(mop);
		}
		return mops;
	}

	public static String getmopName(String mopCode) {
		PayphiNBMopType mopType = PayphiNBMopType.getmop(mopCode);
		if (mopType == null) {
			return "";
		}
		return mopType.getBankName();
	}

	public static String getBankCode(String code) {
		PayphiNBMopType mopType = PayphiNBMopType.getmop(code);
		if (mopType == null) {
			return "";
		}
		return mopType.getBankCode();
	}

	public static PayphiNBMopType getmop(String mopCode) {
		PayphiNBMopType mopObj = null;
		if (null != mopCode) {
			for (PayphiNBMopType mop : PayphiNBMopType.values()) {
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
			for (PayphiNBMopType mop : PayphiNBMopType.values()) {
				if (mopCode.equals(mop.getBankName().toString())) {
					moptType = mop.getCode();
					break;
				}
			}
		}
		return moptType;
	}

	public static PayphiNBMopType getInstanceIgnoreCase(String name) {
		PayphiNBMopType[] mopTypes = PayphiNBMopType.values();
		for (PayphiNBMopType mopType : mopTypes) {
			if (mopType.getBankName().equalsIgnoreCase(name)) {
				return mopType;
			}
		}
		return null;
	}
}
