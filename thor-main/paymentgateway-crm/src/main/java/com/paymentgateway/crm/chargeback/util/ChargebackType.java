package com.paymentgateway.crm.chargeback.util;

public enum ChargebackType {
	
	CHARGEBACK			("Charge Back", "Charge Back"),
	PRE_ARBITRATION		("Pre Arbitration", "Pre Arbitration"),
	FRAUD_DISPUTES		("Fraud Disputes", "Fraud Disputes");
		
	
	
	private final String name;
	private final String code;

	private ChargebackType(String name, String code){
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
