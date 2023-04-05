package com.paymentgateway.crm.chargeback.util;

public enum ChargebackConstants {
	SPACE					("\n"),
	COMMENTED_BY 			("Commented By: "),
	AT						(" at "),
	TILT					("~"),
	SEPERATOR				("################################################################################################################################################################################");
	
	
	private final String value; 

	private ChargebackConstants(String value){
		this.value = value;
	}

	public String getValue() {
		return value;
	}	
}

