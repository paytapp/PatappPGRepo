package com.paymentgateway.commons.util;

public enum TDRStatus {
	
	ACTIVE		("Active","Active"),
	INACTIVE	("InActive","InActive"),
	CANCELLED	("Cancelled","Cancelled"),
	REJECTED	("Rejected","Rejected"),
	PENDING		("Pending","Pending"),
	ACCEPTED	("Accepted","Accepted");
	
	private final String name;
	private final String code;

	
	private TDRStatus(String name, String code){
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}
	
	public String getCode(){
		return code;
	}

}
