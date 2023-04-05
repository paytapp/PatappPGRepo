package com.paymentgateway.hdfc.upi;


public enum HdfcUpiResultType {
	CAPTURED						("00"),
	INVALID_XML_FORMAT				("F100"),
	VALIDATION_ERROR				("F101"),
	DUPLICATE_REQUEST				("F102"),
	INVALID_SENDER_CREDENTIALS		("F103"),
	iNVALID_API_TYPE				("F104"),
	API_NOT_ALLOWED_FOR_MERCHANT	("F105"),
	INVALID_SENDER					("F106"),
	MAX_AMOUNT_LIMIT_EXCEED			("F107"),
	INVALID_HANDLE					("F108"),
	VALIDATION_ERROR_REQUEST		("F109"),
	VIRTUAL_ID_EXIST				("F110"),
	INVALID_OPERATION_TYPE			("F111"),
	INVALID_BANK_CODE				("F120"),
	INVALID_OPERATION				("F123"),
	INVALID_MERCHANT_CATEGORY_CODE	("F122"),
	INVALID_XML_SIGN				("F121"),
	ACCOUNT_NOT_lINKED				("F124"),
	INVALID_VIRTUAL_ID				("F113"),
	ACCOUNT_NOT_EXIST				("F112"),
	REJECTED						("999");
	
	private HdfcUpiResultType(String name){
		this.name = name;
	}
	
	private final String name;

	public String getName() {
		return name;
	}
	
	public static HdfcUpiResultType getInstance(String name){
		if(null == name){
			return REJECTED;
		}
		
		HdfcUpiResultType[] hdfcUpiResultTypes = HdfcUpiResultType.values();
		
		for(HdfcUpiResultType hdfcUpiResultType : hdfcUpiResultTypes){
			if(hdfcUpiResultType.getName().startsWith(name)){
				return hdfcUpiResultType;
			}
		}
		
		//Return error if unexpected value is returned in parameter "result"
		return REJECTED;
	}
}
