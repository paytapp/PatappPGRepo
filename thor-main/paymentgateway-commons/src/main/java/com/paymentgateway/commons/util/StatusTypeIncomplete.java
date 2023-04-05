package com.paymentgateway.commons.util;

public enum StatusTypeIncomplete {

	PENDING			(0, "Pending"),
	TIMEOUT			(1, "Timeout"),
	ENROLLED		(2, "Enrolled"),	
	SENT_TO_BANK    (3, "Sent to Bank");
	
	private final int code;
	private final String name;
	
	private StatusTypeIncomplete(int code, String name){
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
}
