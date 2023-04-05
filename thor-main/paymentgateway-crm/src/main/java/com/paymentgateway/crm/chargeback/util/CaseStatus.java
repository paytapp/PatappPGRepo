package com.paymentgateway.crm.chargeback.util;

public enum CaseStatus {

	OPEN						("Open","Open"),
	CLOSE						("Closed","Closed");
		
	private final String name;
	private final String code;
	private CaseStatus(String name, String code){
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
