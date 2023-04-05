package com.paymentgateway.commons.util;

public enum DynamicCssConstants {
	PAGE_TITTLE 				("Payment Page"),
	BACKGROUND_IMAGE			("ffffff"),
	BACKGROUND_COLOR			("f2f6f9"),
	TEXT_STYLE					("Arial, Helvetica, sans-serif"),
	TEXT_COLOR					("333333"),
	HYPERLINK_COLOR				("428bca"),
	BOX_BACKGROUND_COLOR		("ffffff"),
	TOP_BAR_COLOR				("2b6dd1"),
	TAB_BACKGROUND_COLOR		("ededed"),
	TAB_TEXT_COLOR				("333333"),
	ACTIVE_TAB_COLOR			("ffffff"),
	ACTIVE_TAB_TEXT_COLOR		("2b6dd1"),
	BUTTON_BACKGROUND_COLOR		("f7931d"),
	BUTTON_TEXT_COLOR			("ffffff"),
	BORDER_COLOR				("dddddd") ;
	
	private final String value;

	private DynamicCssConstants(String value){
		this.value = value;
	}

	public String getValue() {
		return value;
	} 
}
