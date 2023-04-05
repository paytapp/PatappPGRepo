package com.paymentgateway.commons.util;

public enum PromotionalPaymentType {
	
	INVOICE_PAYMENT 			("INVOICE PAYMENT"),
	PROMOTIONAL_PAYMENT 		("PROMOTIONAL PAYMENT");
	
	
	
private final String name; 
	
	private PromotionalPaymentType(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}	
}

