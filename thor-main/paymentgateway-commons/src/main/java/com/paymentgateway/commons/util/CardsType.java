package com.paymentgateway.commons.util;

public enum CardsType {

	CREDIT_CARD("Credit Card", "CC"), 
	DEBIT_CARD("Debit Card", "DC");

	private final String name;
	private final String code;

	private CardsType(String name, String code) {
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}
	
	public static String getCodeUsingName(String name){
		return getInstance(name).getCode();
	} 
	
	public static CardsType getInstance(String name) {
		CardsType[] cardTypes = CardsType.values();
		for (CardsType cardType : cardTypes) {
			if (cardType.getCode().toString().equals(name)) {
				return cardType;
			}
		}
		return null;
	}

}
