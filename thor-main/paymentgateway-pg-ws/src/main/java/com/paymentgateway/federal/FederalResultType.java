package com.paymentgateway.federal;

public enum FederalResultType {
	
	HOST_TIMEOUT	("50017"),
	NOT_APPROVED	("NOT APPROVED"),
	NOT_CAPTURED	("50011"),
	APPROVED		("50020"),
	CAPTURED		("50020"),
	FEDERAL0001		("FEDERAL0001"),
	NOT_ENROLED		("NOT ENROLLED"),
	REJECTED		("REJECTED"),
	FAILED			("50021"),
	INITIALIZED		("50010"),
	ENROLLED		("ENROLLED"),
	SUCCESS					("000"),
	INVALID_FIELDS			("001"),
	UNDER_MAINTENANCE		("002"),
	INTERNAL_SYSTEM_ERROR	("003"),
	DB_ERROR				("004"),
	PROTOCOL_ERROR			("005"),
	PERMISSION_ERROR		("006"),
	CARD_NOT_SUPPORT_3DS	("007");
	
	
	
	private FederalResultType(String name){
		this.name = name;
	}
	
	private final String name;

	public String getName() {
		return name;
	}
	
	public static FederalResultType getInstance(String name){
		if(null == name){
			return REJECTED;
		}
		
		FederalResultType[] federalResultTypes = FederalResultType.values();
		
		for(FederalResultType federalResultType : federalResultTypes){
			if(federalResultType.getName().startsWith(name)){
				return federalResultType;
			}
		}
		
		//Return error if unexpected value is returned in parameter "result"
		return REJECTED;
	}
}
