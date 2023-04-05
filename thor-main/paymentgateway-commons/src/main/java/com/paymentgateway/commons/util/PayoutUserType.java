package com.paymentgateway.commons.util;

public enum PayoutUserType {
	
	PAYMENT_GATEWAY (1,"Payment Gateway"),
	PAYBLE (2,"Payble");
	
	private final int code;
	private final String name;
	
	private PayoutUserType(int code, String name){
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
