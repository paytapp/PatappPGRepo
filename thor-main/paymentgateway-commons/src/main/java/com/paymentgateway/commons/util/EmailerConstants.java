package com.paymentgateway.commons.util;

public enum EmailerConstants {

	CONTACT_US_EMAIL ("support@paymentGateway.com"),
	GATEWAY ("Payment Gateway"),
	COMPANY ("Payment Gateway Solution Private Limited"),
	WEBSITE ("www.paymentGateway.com"),
	PHONE_NO ("+91 120 9999999");

	private final String value;

	public String getValue() {
		return value;
	}

	private EmailerConstants(String value){
		this.value = value;
	}
}
