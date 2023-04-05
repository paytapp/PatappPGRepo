package com.paymentgateway.commons.util;

public enum onUsOffUs {

	OFF_US		("0", "Off Us"),
	ON_US		("1", "On Us"),
	ALL			    ("All", "All");
	
	private final String name;
	private final String code;
	
	private onUsOffUs(String name, String code){
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
