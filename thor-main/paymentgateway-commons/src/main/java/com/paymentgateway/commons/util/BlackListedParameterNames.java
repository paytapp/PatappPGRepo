package com.paymentgateway.commons.util;

public enum BlackListedParameterNames {

	//Action return strings
	SESSION        ("session"),
	REQUEST        ("request");

	private final String name; 
	
	private BlackListedParameterNames(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}
