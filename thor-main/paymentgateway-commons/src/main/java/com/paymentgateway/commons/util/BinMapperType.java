package com.paymentgateway.commons.util;

/**
 * @author Sunil
 *
 */

public enum BinMapperType {

	//Cards
	VISA		("VISA", "VI"),
	AMEX		("AMERICAN EXPRESS", "AX"),
	DISCOVER	("DISCOVER", "DI"),
	JCB			("JCB", "JC"),
	MASTERCARD	("MASTERCARD", "MC"),
	MAESTRO		("MAESTRO", "MS"),
	DINERS		("DINERS CLUB INTERNATIONAL", "DN"),
	RUPAY		("RUPAY", "RU"),
	
	//Method of Payment
	CREDITCARD	("CREDIT", "CC"),
	DEBITCARD	("DEBIT", "DC"),
	
	//Cards Citrus BIN API
	VISA_CITRUS			("VISA", "VI"),
	AMEX_CITRUS			("AMEX", "AX"),
	DISCOVER_CITRUS		("DISCOVER", "DI"),
	JCB_CITRUS			("JCB", "JC"),
	MASTERCARD_CITRUS	("MCRD", "MC"),
	MAESTRO_CITRUS		("MTRO", "MS"),
	DINERS_CITRUS		("DINERS", "DN"),
	RUPAY_CITRUS		("RUPAY", "RU"),
	
	//Method of Payment
	CREDITCARD_CITRUS	("Credit Card", "CC"),
	DEBITCARD_CITRUS	("Debit Card", "DC");
	
	private final String name;
	private final String code;
	
	
	private BinMapperType(String name, String code){
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}
	
	public String getCode(){
		return code;
	}

	public static String getCodeUsingName(String name){
		return getInstance(name).getCode();
	}
	public static BinMapperType getInstance(String name){
		BinMapperType[] BinMapperTypes = BinMapperType.values();
		for(BinMapperType mopType : BinMapperTypes){
			if(mopType.getName().equals(name)){
				return mopType;
			}
		}		
		return null;
	}
	
	public static String getmopName(String mopCode){
			BinMapperType mopType = BinMapperType.getmop(mopCode);		
			if(mopType == null) {
				return "";
			}
		return mopType.getName();
	}
	
	public static BinMapperType getmop(String mopCode){
		BinMapperType mopObj = null;
		if(null!=mopCode){
		for(BinMapperType mop:BinMapperType.values()){
			if(mopCode.equals(mop.getCode().toString())){
				mopObj=mop;
				break;
			}
		  }
		}
		return mopObj;
	}
}
