package com.paymentgateway.commons.util;

public enum AccountStatus {
	
	ACTIVE		("Active","Active"),
	INACTIVE	("InActive","InActive"),
	CANCELLED	("Cancelled","Cancelled"),
	REJECTED	("Rejected","Rejected"),
	PENDING		("Pending","Pending"),
	ACCEPTED	("Accepted","Accepted"),
	SETTLED		("Settled", "Settled"),
	SENT_TO_BENEFICIARY ("Sent to beneficiary", "Sent to beneficiary"),
	IN_PROCESS		("IN_PROCESS", "IN_PROCESS"),
	FAILED		("Failed", "failed");
	
	private final String name;
	private final String code;

	
	private AccountStatus(String name, String code){
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
