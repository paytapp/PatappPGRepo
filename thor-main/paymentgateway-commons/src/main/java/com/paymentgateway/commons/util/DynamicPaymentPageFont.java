package com.paymentgateway.commons.util;

public enum DynamicPaymentPageFont {
		ARIAL 	 ("Arial, Helvetica, sans-serif"),
		COURIER	 ("Courier New, Courier, monospace"),
		GEORGIA	 ("Georgia, Times New Roman, Times, serif"),
		TAHOMA	 ("Tahoma, Geneva, sans-serif"),
	    VERDANA	 ("Verdana, Geneva, sans-serif");
    
     

private final String name; 

	private DynamicPaymentPageFont(String name){
	this.name = name;
	}

	public String getName() {
	return name;
	}	
	

}
