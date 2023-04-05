package com.paymentgateway.crm.chargeback.util;

public enum ChargebackStatus {

	OPEN							("Open", "Open"),
	//NEW							("New", "New"),
	ACCEPTED					("Accepted", "Accepted"),
	REJECTED					("Rejected", "Rejected"),
	REFUNDED					("Refunded", "Refunded"),
	CLOSED						("Closed", "Closed");
	
 // ACCEPTED_BY_MERCHANT		("Accepted by merchant", "Accepted by merchant"),
 //	DISPUTES					("Disputes", "Disputes"),
 //	CONTESTED					("Contested","Contested"),
 //	Charge_Back                 ("Charge Back","Charge Back");
		
	private final String name;
	private final String code;
	private ChargebackStatus(String name, String code){
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
