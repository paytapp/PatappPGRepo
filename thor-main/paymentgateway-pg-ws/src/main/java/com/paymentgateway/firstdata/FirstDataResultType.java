package com.paymentgateway.firstdata;

public enum FirstDataResultType {
	
	HOST_TIMEOUT	("HOST TIMEOUT"),
	DENIED_BY_RISK	("DENIED BY RISK"),
	NOT_APPROVED	("NOT APPROVED"),
	NOT_CAPTURED	("NOT CAPTURED"),
	APPROVED		("APPROVED"),
	CAPTURED		("CAPTURED"),
	FIRST_DATA_0001	("FIRSTDATA0001"),
	NOT_ENROLED		("NOT ENROLLED"),
	REJECTED		("REJECTED"),
	INITIALIZED		("INITIALIZED"),
	ENROLLED		("?:waiting 3dsecure");
	
	private FirstDataResultType(String name){
		this.name = name;
	}
	
	private final String name;

	public String getName() {
		return name;
	}
	
	public static FirstDataResultType getInstance(String name){
		if(null == name){
			return REJECTED;
		}
		
		FirstDataResultType[] firstDataResultTypes = FirstDataResultType.values();
		
		for(FirstDataResultType firstDataResultType : firstDataResultTypes){
			if(firstDataResultType.getName().startsWith(name)){
				return firstDataResultType;
			}
		}
		
		//Return error if unexpected value is returned in parameter "result"
		return REJECTED;
	}

}
