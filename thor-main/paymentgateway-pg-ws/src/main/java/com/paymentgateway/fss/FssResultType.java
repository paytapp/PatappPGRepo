package com.paymentgateway.fss;

public enum FssResultType {
	HOST_TIMEOUT	("HOST TIMEOUT"),
	DENIED_BY_RISK	("DENIED BY RISK"),
	NOT_APPROVED	("NOT APPROVED"),
	NOT_CAPTURED	("NOT CAPTURED"),
	APPROVED		("APPROVED"),
	CAPTURED		("CAPTURED"),
	FSS0001			("FSS0001"),
	NOT_ENROLED		("NOT ENROLLED"),
	REJECTED		("REJECTED"),
	INITIALIZED		("INITIALIZED"),
	ENROLLED		("ENROLLED");
	
	private FssResultType(String name){
		this.name = name;
	}
	
	private final String name;

	public String getName() {
		return name;
	}
	
	public static FssResultType getInstance(String name){
		if(null == name){
			return REJECTED;
		}
		
		FssResultType[] fssResultTypes = FssResultType.values();
		
		for(FssResultType fssResultType : fssResultTypes){
			if(fssResultType.getName().startsWith(name)){
				return fssResultType;
			}
		}
		
		//Return error if unexpected value is returned in parameter "result"
		return REJECTED;
	}
}
