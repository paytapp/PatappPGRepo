package com.paymentgateway.pg.core.fraudPrevention.util;

import org.apache.commons.lang3.StringUtils;

import com.paymentgateway.commons.util.FieldType;

/**
 * @author Harpreet
 *
 */
public enum FraudRuleType {
	
	//These ids will directly help to identify the nature/type/behaviour of fraud rules
	BLOCK_NO_OF_TXNS			("5", "BLOCK_NO_OF_TXNS"),
	BLOCK_TXN_AMOUNT			("6", "BLOCK_TXN_AMOUNT", FieldType.AMOUNT.getName()),
	BLOCK_IP_ADDRESS			("1","BLOCK_IP_ADDRESS",FieldType.INTERNAL_CUST_IP.getName()),
	BLOCK_DOMAIN_NAME			("3", "BLOCK_DOMAIN_NAME"),
	BLOCK_EMAIL_ID				("4","BLOCK_EMAIL_ID", FieldType.CUST_EMAIL.getName()),
	BLOCK_USER_COUNTRY			("2","BLOCK_USER_COUNTRY", FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()),
	BLOCK_CARD_ISSUER_COUNTRY	("7","BLOCK_CARD_ISSUER_COUNTRY",FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()),
	BLOCK_CARD_BIN				("8", "BLOCK_CARD_BIN", FieldType.CARD_NUMBER.getName()),
	BLOCK_CARD_NO				("9", "BLOCK_CARD_NO",FieldType.CARD_NUMBER.getName()),
	BLOCK_CARD_TXN_THRESHOLD	("10", "BLOCK_CARD_TXN_THRESHOLD",FieldType.CARD_NUMBER.getName());
	
	private final String code;
	private final String value;
	private final String fieldName;
	
	private FraudRuleType(String code, String value){
		this.code = code;
		this.value = value;
		this.fieldName = null;
	}
	private FraudRuleType(String code, String value, String fieldName){
		this.code = code;
		this.value = value;
		this.fieldName = fieldName;
	}
	
	public String getCode(){
		return code;
	}
	
	public String getValue(){
		return value;
	}
	
	public String getFieldName(){
		return fieldName;
	}
	
	public static FraudRuleType getInstance(String fraudRule){
		FraudRuleType fraudType = null;
		if(!StringUtils.isBlank(fraudRule)){
			for(FraudRuleType fraudObj: FraudRuleType.values()){
				if(fraudRule.equals(fraudObj.getValue())){
					fraudType = fraudObj;
					break;
				}
			}
		}
		return fraudType;
	}
}