package com.paymentgateway.commons.util;


/**
 * @author Surender
 *
 */
public enum ModeType {

	AUTH_CAPTURE	("AUTH_CAPTURE"),
	SALE			("SALE");
	
	private final String name;

	private ModeType(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public static ModeType defaultModeType(){
		return SALE;
	}
	
	public static TransactionType getDefaultPurchaseTransaction(){
		String mode = defaultModeType().getName();
		if(mode.equals(AUTH_CAPTURE.getName())){
			return TransactionType.AUTHORISE;
		} else {
			return TransactionType.SALE;
		}
	}
	public static TransactionType getDefaultPurchaseTransaction(ModeType modeType){
		if (null == modeType) {
			return TransactionType.SALE;
		}
		
		String mode = modeType.getName();
		if (mode.equals(AUTH_CAPTURE.getName())) {
			return TransactionType.AUTHORISE;
		} else {
			return TransactionType.SALE;
		}
	}
}
