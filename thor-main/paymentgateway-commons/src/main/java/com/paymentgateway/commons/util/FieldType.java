package com.paymentgateway.commons.util;

import java.util.HashMap;
import java.util.Map;

/*
 * This type serves as the central validation information for different fields which can be submitted in a request
 */
public enum FieldType {

	//Internal use fields
	ACQUIRER_TYPE				("ACQUIRER_TYPE", 3, 50, false, FieldFormatType.ALPHANUM, false),
	ACQUIRER_MODE				("ACQUIRER_MODE", 5, 10, false, FieldFormatType.SPECIAL, false),
	INTERNAL_ACQUIRER_TYPE		("INTERNAL_ACQUIRER_TYPE", 3, 10, false, FieldFormatType.ALPHA, false),
	INTERNAL_VALIDATE_HASH_YN	("INTERNAL_VALIDATE_HASH_YN", 1, 1, false, FieldFormatType.NONE, false),	
	INTERNAL_ORIG_TXN_TYPE		("INTERNAL_ORIG_TXN_TYPE", 3, 20, false, FieldFormatType.NONE, false),
	INTERNAL_CUST_IP			("INTERNAL_CUST_IP", 7, 15, false, FieldFormatType.NONE, true),
	INTERNAL_HEADER_ACEEPT		("INTERNAL_HEADER_ACEEPT", 2, 1000, false, FieldFormatType.NONE, false),
	INTERNAL_HEADER_USER_AGENT	("INTERNAL_HEADER_USER_AGENT", 2, 1000, false, FieldFormatType.NONE, false),
	INTERNAL_CUST_COUNTRY_NAME	("INTERNAL_CUST_COUNTRY_NAME", 2, 50, false, FieldFormatType.ALPHANUM, true),
	INTERNAL_CUSTOM_MDC			("INTERNAL_CUSTOM_MDC", 1, 256, false, FieldFormatType.NONE, false),
	INTERNAL_REQUEST_FIELDS		("INTERNAL_REQUEST_FIELDS", 1, 6000, false, FieldFormatType.NONE, true),
	INTERNAL_INVALID_HASH_YN	("INTERNAL_INVALID_HASH_YN", 1, 1, false, FieldFormatType.NONE, false),
	INTERNAL_ORIG_TXN_ID		("INTERNAL_ORIG_TXN_ID", 16, 16, false, FieldFormatType.SPECIAL, false),
	INTERNAL_SHOPIFY_YN 		("INTERNAL_SHOPIFY_YN", 1, 1, false, FieldFormatType.NONE, false),
	INTERNAL_PAYMENT_GATEWAY_YN ("INTERNAL_PAYMENT_GATEWAY_YN", 1, 1, false, FieldFormatType.NONE, false),
	OID							("OID", 16, 16, false, FieldFormatType.NUMBER, false),
	SLAB_ID						("SLAB_ID", 2, 2, false, FieldFormatType.NUMBER, false),
	VPC_LOCALE	                ("VPC_LOCALE", 2, 5, false, FieldFormatType.ALPHANUM, true),
	INTERNAL_TXN_AUTHENTICATION	("INTERNAL_TXN_AUTHENTICATION", 0, 16, false, FieldFormatType.ALPHA, false),
	CREATE_DATE                 ("CREATE_DATE", 19, 19, false, FieldFormatType.SPECIAL, false),
	SETTLEMENT_DATE             ("SETTLEMENT_DATE", 19, 19, false, FieldFormatType.SPECIAL, false),
	PAYOUT_DATE                 ("PAYOUT_DATE", 19, 19, false, FieldFormatType.SPECIAL, false),
	UPDATE_DATE                 ("UPDATE_DATE", 19, 19, false, FieldFormatType.SPECIAL, false),
	DATE_FROM                   ("DATE_FROM", 1, 19, false, FieldFormatType.SPECIAL, false),
	DATE_TO                     ("DATE_TO", 1, 19, false, FieldFormatType.SPECIAL, false),
	REQUEST_DATE				("REQUEST_DATE", 19, 19, false, FieldFormatType.SPECIAL, false),
	DATE_INDEX     				("DATE_INDEX", 1, 19, false, FieldFormatType.ALPHANUM, false),
	SETTLEMENT_DATE_INDEX		("SETTLEMENT_DATE_INDEX", 1, 19, false, FieldFormatType.ALPHANUM, false),
	PG_DATE_TIME_INDEX   ("PG_DATE_TIME_INDEX", 1, 19, false, FieldFormatType.ALPHANUM, false),
	ORIG_TXNTYPE				("ORIG_TXNTYPE", 4, 50, false, FieldFormatType.ALPHA, false),
	IS_ENROLLED					("IS_ENROLLED", 1, 50, false, FieldFormatType.ALPHA, false),
	DEBIT_DATE     				("DEBIT_DATE", 1, 19, false, FieldFormatType.ALPHANUM, false),
	IS_ENCRYPTED     			("IS_ENCRYPTED", 1, 2, false, FieldFormatType.ALPHANUM, false),
	FILENAME					("FILENAME", 4, 100, false, FieldFormatType.ALPHA, false),
	LOCATION					("LOCATION", 4, 100, false, FieldFormatType.ALPHA, false),
	//Response fields
	DUPLICATE_YN				("DUPLICATE_YN", 1, 1, false, FieldFormatType.ALPHA, false),
	RESPONSE_CODE				("RESPONSE_CODE", 1, 10, false, FieldFormatType.ALPHANUM, false),		
	RESPONSE_MESSAGE			("RESPONSE_MESSAGE", 1, 256,false, FieldFormatType.ALPHANUM, false),
	ORIG_TXN_ID					("ORIG_TXN_ID", 16, 16, false, FieldFormatType.SPECIAL, false),
	STATUS						("STATUS", 5, 30, false, FieldFormatType.SPECIAL, false),
	ALIAS_STATUS				("ALIAS_STATUS", 5, 30, false, FieldFormatType.SPECIAL, false),
	LAST_STATUS					("LAST_STATUS", 1, 30, false, FieldFormatType.SPECIAL, false),
	ACS_URL						("ACS_URL", 5, 256, false, FieldFormatType.SPECIAL, false),
	ACS_RETURN_URL				("ACS_RETURN_URL", 5, 256, false, FieldFormatType.SPECIAL, false),
	TERM_URL					("TERM_URL", 5, 256, false, FieldFormatType.SPECIAL, false),
	PAREQ						("PAREQ", 1, 1000, false, FieldFormatType.SPECIAL, false),
	PAYMENT_ID					("PAYMENT_ID", 1, 300, false, FieldFormatType.SPECIAL, false),
	ACS_REQ_MAP					("ACS_REQ_MAP", 1, 10000, false, FieldFormatType.SPECIAL, false),
	ECI							("ECI", 0, 2, false, FieldFormatType.SPECIAL, false),
	AUTH_CODE					("AUTH_CODE", 0, 300, false, FieldFormatType.ALPHANUM, false),
	ARN							("ARN", 0, 300, false, FieldFormatType.SPECIAL, false),
	RRN							("RRN", 0, 300, false, FieldFormatType.SPECIAL, false),
	AVR							("AVR", 0, 100, false, FieldFormatType.ALPHANUM, false),
	POST_DATE					("POST_DATE", 0, 20, false, FieldFormatType.SPECIAL, false),
	ACQ_ID						("ACQ_ID", 0, 300, false, FieldFormatType.ALPHANUM, false),
	MD							("MD", 0, 10000, false, FieldFormatType.REGEX, false),
	RESPONSE_DATE				("RESPONSE_DATE", 10, 10, false, FieldFormatType.SPECIAL, false),
	RESPONSE_TIME				("RESPONSE_TIME", 6, 10, false, FieldFormatType.SPECIAL, false),
	RESPONSE_DATE_TIME			("RESPONSE_DATE_TIME", 19, 19, false, FieldFormatType.SPECIAL, false),
	IS_STATUS_FINAL    			("IS_STATUS_FINAL", 1, 19, false, FieldFormatType.SPECIAL, false),
	REFUND_TXN_TYPE    			("REFUND_TXN_TYPE", 1, 19, false, FieldFormatType.ALPHA, false),
	PAYMENT_FlOW				("PAYMENT_FlOW", 1, 19, false, FieldFormatType.ALPHA, false),
	
	//Card Information
	//Card number minimum length is 13, because FSS UAT contains a test case with card length 13
	CARD_NUMBER					("CARD_NUMBER", 13, 19, false, FieldFormatType.NUMBER, false),
	CARD_MASK					("CARD_MASK", 13, 19, false, FieldFormatType.SPECIAL, true),
	S_CARD_NUMBER				("S_CARD_NUMBER", 28, 28, false, FieldFormatType.SPECIAL, false),
	H_CARD_NUMBER				("H_CARD_NUMBER", 64, 64, false, FieldFormatType.ALPHANUM, false),
	CARD_EXP_DT					("CARD_EXP_DT", 6, 6, false, FieldFormatType.NUMBER, false),
	S_CARD_EXP_DT				("S_CARD_EXP_DT", 28, 28, false, FieldFormatType.SPECIAL, false),
	H_CARD_EXP_DT				("H_CARD_EXP_DT", 64, 64, false, FieldFormatType.ALPHANUM, false),
	CVV							("CVV", 3, 4, false, FieldFormatType.NUMBER, false),
	PARES						("PARES", 1, 10000, false, FieldFormatType.SPECIAL, false),
	INTERNAL_CARD_ISSUER_BANK	("INTERNAL_CARD_ISSUER_BANK", 1, 256, false, FieldFormatType.ALPHANUM, true),
	INTERNAL_CARD_ISSUER_COUNTRY("INTERNAL_CARD_ISSUER_COUNTRY", 2, 100, false, FieldFormatType.ALPHANUM, true),
	CARD_ISSUER_BANK			("CARD_ISSUER_BANK", 1, 256, false, FieldFormatType.ALPHANUM, true),
	CARD_ISSUER_COUNTRY			("CARD_ISSUER_COUNTRY", 2, 100, false, FieldFormatType.ALPHANUM, true),
	CARD_HOLDER_NAME            ("CARD_HOLDER_NAME", 1, 150, false, FieldFormatType.ALPHA, true),
    SAVED_CARD_FLAG             ("SAVED_CARD_FLAG",1,1,false, FieldFormatType.ALPHA, false),

	//Customer billing information
	CUST_ID						("CUST_ID", 5, 256, false, FieldFormatType.SPECIAL, true),
	CUST_NAME					("CUST_NAME", 1, 150, false, FieldFormatType.ALPHA, true),
	CUST_FIRST_NAME				("CUST_FIRST_NAME", 2, 150, false, FieldFormatType.ALPHA, true),
	CUST_LAST_NAME				("CUST_LAST_NAME", 2, 150, false, FieldFormatType.ALPHA, true),
	CUST_PHONE					("CUST_PHONE", 8, 15, false, FieldFormatType.NUMBER, true),
	CUST_MOBILE					("CUST_MOBILE", 8, 15, false, FieldFormatType.NUMBER, true),
	CUST_STREET_ADDRESS1		("CUST_STREET_ADDRESS1", 2, 250, false, FieldFormatType.ALPHANUM, true),
	CUST_STREET_ADDRESS2		("CUST_STREET_ADDRESS2", 2, 250, false, FieldFormatType.ALPHANUM, true),
	CUST_CITY					("CUST_CITY", 2, 50, false, FieldFormatType.ALPHANUM, true),
	CUST_STATE					("CUST_STATE", 2, 100, false, FieldFormatType.ALPHANUM, true),
	CUST_COUNTRY				("CUST_COUNTRY", 2, 100, false, FieldFormatType.ALPHANUM, true),
	CUST_EMAIL					("CUST_EMAIL", 6, 120, false, FieldFormatType.EMAIL, true),
	INTERNAL_USER_EMAIL			("INTERNAL_USER_EMAIL", 6, 120, false, FieldFormatType.EMAIL, true),
	CUST_ZIP					("CUST_ZIP", 6, 9, false, FieldFormatType.ALPHANUM, true),	
	REFUNDABLE_AMOUNT			("refundableAmount", 3, 12, false, FieldFormatType.NUMBER, false),
	UNIQUE_NO                   ("UNIQUE_NO", 1, 100, false, FieldFormatType.ALPHANUM, false),
	REG_NUMBER 				   ("REG_NUMBER", 1, 32, false, FieldFormatType.ALPHANUM, false),
	//Customer shipping information
	CUST_SHIP_NAME				("CUST_SHIP_NAME", 2, 150, false, FieldFormatType.ALPHA, true),
	CUST_SHIP_FIRST_NAME		("CUST_SHIP_FIRST_NAME", 2, 150, false, FieldFormatType.ALPHA, true),
	CUST_SHIP_LAST_NAME			("CUST_SHIP_LAST_NAME", 2, 150, false, FieldFormatType.ALPHA, true),
	CUST_SHIP_PHONE				("CUST_SHIP_PHONE", 8, 15, false, FieldFormatType.NUMBER, true),
	CUST_SHIP_STREET_ADDRESS1	("CUST_SHIP_STREET_ADDRESS1", 2, 250, false, FieldFormatType.ALPHANUM, true),
	CUST_SHIP_STREET_ADDRESS2	("CUST_SHIP_STREET_ADDRESS2", 2, 250, false, FieldFormatType.ALPHANUM, true),
	CUST_SHIP_CITY				("CUST_SHIP_CITY", 2, 50, false, FieldFormatType.ALPHANUM, true),
	CUST_SHIP_STATE				("CUST_SHIP_STATE", 2, 100, false, FieldFormatType.ALPHANUM, true),
	CUST_SHIP_COUNTRY			("CUST_SHIP_COUNTRY", 2, 100, false, FieldFormatType.ALPHANUM, true),
	CUST_SHIP_EMAIL				("CUST_SHIP_EMAIL", 6, 120, false, FieldFormatType.EMAIL, true),
	CUST_SHIP_ZIP				("CUST_SHIP_ZIP", 6, 9, false, FieldFormatType.NUMBER, true),
	COMPANY_NAME				("COMPANY_NAME", 2, 150, false, FieldFormatType.ALPHASPACENUM, true),

	//Order information
	REQUEST_URL				("REQUEST_URL", 5, 1024, false, FieldFormatType.URL, false),
	RETURN_URL				("RETURN_URL", 5, 1024, false, FieldFormatType.URL, false),
	RETRY_URL				("RETRY_URL", 5, 1024, false, FieldFormatType.URL, false),
	CANCEL_URL				("CANCEL_URL", 5, 1024, false, FieldFormatType.URL, false),
	CURRENCY_CODE			("CURRENCY_CODE", 3, 3, true,  FieldFormatType.NUMBER, false),
	KEY_ID					("KEY_ID", 1, 2, false, FieldFormatType.NUMBER, false),
	ORDER_ID 				("ORDER_ID", 1, 50, true, FieldFormatType.SPECIAL, false),
	AMOUNT					("AMOUNT", 3, 12, true, FieldFormatType.AMOUNT, false),
	CHARGEBACK_AMOUNT		("CHARGEBACK_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	REFUNDAMOUNT			("REFUNDAMOUNT", 3, 12, false, FieldFormatType.NUMBER, false),
	ADJUSTMENT_AMOUNT_CR	("ADJUSTMENT_AMOUNT_CR", 3, 12, false, FieldFormatType.AMOUNT, false),
	ADJUSTMENT_AMOUNT_DR	("ADJUSTMENT_AMOUNT_DR", 3, 12, false, FieldFormatType.AMOUNT, false),
	REFUND_DATE_TIME		("REFUND_DATE_TIME", 1, 50, false, FieldFormatType.SPECIAL, false),
	TXNTYPE					("TXNTYPE", 4, 50, true, FieldFormatType.ALPHA, false),
	MERCHANT_TXN_ID			("MERCHANT_TXN_ID", 6, 50, false, FieldFormatType.SPECIAL, false),
	TXN_ID					("TXN_ID", 16, 16, false, FieldFormatType.NUMBER, false),
	ADJUSTMENT_TXN_ID		("ADJUSTMENT_TXN_ID", 16, 16, false, FieldFormatType.NUMBER, false),
	CATEGORY_CODE			("CATEGORY_CODE", 1, 256, false, FieldFormatType.SPECIAL, false),
	SKU_CODE 				("SKU_CODE", 1, 256, false, FieldFormatType.SPECIAL, false),
	REFUND_DAYS				("REFUND_DAYS", 1, 256, false, FieldFormatType.SPECIAL, false),
//	PRODUCT_PRICE			("Product_price", 1, 256, false, FieldFormatType.SPECIAL, false),
//	VENDOR_ID 				("Vendor_id", 1, 256, false, FieldFormatType.SPECIAL, false),
	PRODUCT_AMOUNT			("PRODUCT_AMOUNT", 1, 256, false, FieldFormatType.SPECIAL, false),
	REFUND_CYCLE_DAYS		("REFUND_CYCLE_DAYS", 1, 256, false, FieldFormatType.SPECIAL, false),
	TXN_SOURCE				("TXN_SOURCE", 6, 8, false, FieldFormatType.ALPHA, false),
	APP_CODE				("APP_CODE", 2, 8, false, FieldFormatType.ALPHA, false),
	PAY_ID					("PAY_ID", 2, 36, true, FieldFormatType.NUMBER, false),
	RESELLER_ID             ("RESELLER_ID", 2, 36, false, FieldFormatType.NUMBER, false),
	MERCHANT_ID				("MERCHANT_ID", 1, 300, false, FieldFormatType.SPECIAL, false),
	SUB_MERCHANT_ID			("SUB_MERCHANT_ID", 1, 30, false, FieldFormatType.SPECIAL, false),
	SUPER_MERCHANT_ID		("SUPER_MERCHANT_ID", 1, 30, false, FieldFormatType.SPECIAL, false),
	PARENT_PAY_ID			("PARENT_PAY_ID", 2, 36, false, FieldFormatType.NUMBER, false),
	VENDOR_ID				("VENDOR_ID", 1, 30, false, FieldFormatType.SPECIAL, false),
	MOP_TYPE				("MOP_TYPE", 2, 15, false, FieldFormatType.MOPTYPE, false),
	PASSWORD				("PASSWORD", 6, 100, false, FieldFormatType.SPECIAL, false),
	ACCT_ID					("ACCT_ID", 1, 20, false, FieldFormatType.NUMBER, false),
	PAYMENT_TYPE			("PAYMENT_TYPE", 2, 4, false, FieldFormatType.ALPHA, false),
	PG_REF_NUM				("PG_REF_NUM", 1, 100, false, FieldFormatType.ALPHANUM, false),
	PG_RESP_CODE			("PG_RESP_CODE", 1, 10, false, FieldFormatType.ALPHASPACENUM, true),
	PG_TXN_MESSAGE			("PG_TXN_MESSAGE", 1, 500, false, FieldFormatType.SPECIAL, false),
	PG_TXN_STATUS			("PG_TXN_STATUS", 1, 500, false, FieldFormatType.SPECIAL, false),	
	PG_DATE_TIME			("PG_DATE_TIME", 1, 50, false, FieldFormatType.SPECIAL, false),
	PG_GATEWAY				("PG_GATEWAY", 1, 100, false, FieldFormatType.ALPHANUM, false),	
	DELIVERY_STATUS			("DELIVERY_STATUS", 9, 15, false, FieldFormatType.ALPHASPACENUM, false),
	DELIVERY_CODE			("DELIVERY_CODE", 3, 3, false, FieldFormatType.NUMBER, true),
	HASH					("HASH", 64, 64, false, FieldFormatType.ALPHANUM, false),
	SIGNATURE				("SIGNATURE", 64, 64, false, FieldFormatType.ALPHANUM, false),
	INTERNAL_BANK_NAME		("BANK_NAME", 3, 255, false, FieldFormatType.ALPHA, false),
	INTERNAL_BANK_CODE		("BANK_CODE", 3, 6, false, FieldFormatType.NUMBER, false),
	WALLET_NAME				("WALLET_NAME", 3, 6, false, FieldFormatType.ALPHA, false),
	PRODUCT_DESC			("PRODUCT_DESC", 1, 1024, false, FieldFormatType.SPECIAL, true),
	TXN_KEY				    ("TXN_KEY", 6, 100, false, FieldFormatType.SPECIAL, false),
	TOKEN_ID				("TOKEN_ID", 16, 16, false, FieldFormatType.SPECIAL, false),
	TERMINAL_ID				("TERMINAL_ID", 2, 36, true, FieldFormatType.SPECIAL, false),
	BANK_ID					("BANK_ID", 2, 36, false, FieldFormatType.SPECIAL, false),
	SERVICE_ID				("SERVICE_ID", 1, 36, true, FieldFormatType.SPECIAL, false),
	TXN_DATE				("TXN_DATE", 1, 36, false, FieldFormatType.SPECIAL, false),
	ACCOUNT_TYPE			("ACCOUNT_TYPE", 3, 50, false, FieldFormatType.ALPHA, false),
	ACCOUNT_HOLDER_NAME		("ACCOUNT_HOLDER_NAME", 1, 150, false, FieldFormatType.ALPHASPACE, true),
	FREQUENCY				("FREQUENCY", 4, 6, false, FieldFormatType.ALPHA, false),
	MERCHANT_NAME			("MERCHANT_NAME", 1, 150, false, FieldFormatType.ALPHASPACENUM, true),
	MERCHANT_EMAIL			("MERCHANT_EMAIL", 6, 120, false, FieldFormatType.EMAIL, true),
	UMRN_NUMBER				("UMRN_NUMBER", 1, 250, false, FieldFormatType.ALPHANUM, false),
	AMOUNT_TYPE				("AMOUNT_TYPE", 1, 5, false, FieldFormatType.ALPHA, true),
	DEBIT_DAY				("DEBIT_DAY", 1, 2, false, FieldFormatType.NUMBER, false),
	DEBIT_RULE				("DEBIT_RULE", 2, 2, false, FieldFormatType.ALPHA, false),
	AMOUNT_LIMIT			("AMOUNT_LIMIT", 1, 1, false, FieldFormatType.ALPHA, true),
	TOKEN					("TOKEN", 0, 128, false, FieldFormatType.SPECIAL, false),
	//merchant Captured and Refunded Amount 
	TODAY_CAPTURED_AMOUNT	("todayCapturedAmount",2,12,false,FieldFormatType.NUMBER,false),
	TODAY_REFUND 			("todayRefundAmount",2,12,false,FieldFormatType.NUMBER,false),
	//Txn Information
	IS_MERCHANT_HOSTED                      ("IS_MERCHANT_HOSTED", 1, 1, false, FieldFormatType.ALPHA, false),
	IS_SUB_MERCHANT                      	("IS_SUB_MERCHANT", 1, 1, false, FieldFormatType.ALPHA, false),
	IS_PARENT_MERCHANT                     	("IS_PARENT_MERCHANT", 1, 1, false, FieldFormatType.ALPHA, false),
	IS_CUSTOM_HOSTED                   	    ("IS_CUSTOM_HOSTED", 1, 1, false, FieldFormatType.ALPHA, false),
	IS_RECURRING     						("IS_RECURRING", 1, 1, false, FieldFormatType.ALPHA, true),
	IS_INTERNAL_REQUEST     				("IS_INTERNAL_REQUEST", 1, 1, false, FieldFormatType.ALPHA, false),
	RECURRING_TRANSACTION_INTERVAL    		("RECURRING_TRANSACTION_INTERVAL", 1, 25, false, FieldFormatType.ALPHA, false),
	RECURRING_TRANSACTION_COUNT			    ("RECURRING_TRANSACTION_COUNT", 1, 2, false, FieldFormatType.NUMBER, false),
	RETRY_FLAG								("RETRY_FLAG",1,1,false, FieldFormatType.ALPHA, false),
	RETRY_COUNT							    ("RETRY_COUNT", 1, 2, false, FieldFormatType.NUMBER, false),
	BOOKING_MERCHANT_FLAG					("BOOKING_MERCHANT_FLAG",1,1,false, FieldFormatType.ALPHA, false),
	NUMBER_OF_RETRY							("NUMBER_OF_RETRY",1,1,false, FieldFormatType.NUMBER, false),
	COUNT			                        ("recordsTotal", 0, 100000000, false, FieldFormatType.NUMBER, false),
	RECURRING_TRANSACTION_ID				("RECURRING_TRANSACTION_ID",16,16,false, FieldFormatType.NUMBER, false),
	ENABLE_SURCHARGE						("ENABLE_SURCHARGE",1,1,false, FieldFormatType.ALPHA, false),
	
	SURCHARGE_FLAG							("SURCHARGE_FLAG",1,1,false, FieldFormatType.ALPHA, false),
	CC_CONSUMER_SURCHARGE					("CC_CONSUMER_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	CC_COMMERCIAL_SURCHARGE					("CC_COMMERCIAL_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	CC_PREMIUM_SURCHARGE					("CC_PREMIUM_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	CC_AMEX_SURCHARGE					("CC_AMEX_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	WL_SURCHARGE							("WL_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	DC_VISA_SURCHARGE						("DC_VISA_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	DC_MASTERCARD_SURCHARGE					("DC_MASTERCARD_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	DC_RUPAY_SURCHARGE						("DC_RUPAY_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	CC_SURCHARGE_INTERNATIONAL				("CC_SURCHARGE_INTERNATIONAL", 3, 12, false, FieldFormatType.AMOUNT, false),
	DC_SURCHARGE_INTERNATIONAL				("DC_SURCHARGE_INTERNATIONAL", 3, 12, false, FieldFormatType.AMOUNT, false),
	NB_SURCHARGE							("NB_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	PC_SURCHARGE							("PC_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	CD_SURCHARGE							("CD_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	CR_SURCHARGE							("CR_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	IN_SURCHARGE							("IN_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	EMI_CC_SURCHARGE							("EM_CC_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	EMI_DC_SURCHARGE							("EM_DC_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	MQR_SURCHARGE							("MQR_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	AD_SURCHARGE							("AD_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	UP_SURCHARGE							("UP_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	AP_SURCHARGE							("AP_SURCHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	CC_CONSUMER_TOTAL_AMOUNT				("CC_CONSUMER_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	CC_COMMERCIAL_TOTAL_AMOUNT				("CC_COMMERCIAL_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	CC_PREMIUM_TOTAL_AMOUNT					("CC_PREMIUM_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	CC_AMEX_TOTAL_AMOUNT					("CC_AMEX_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	CC_TOTAL_AMOUNT_INTERNATIONAL			("CC_TOTAL_AMOUNT_INTERNATIONAL", 3, 12, false, FieldFormatType.AMOUNT, false),
	DC_VISA_TOTAL_AMOUNT					("DC_VISA_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	DC_MASTERCARD_TOTAL_AMOUNT				("DC_MASTERCARD_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	DC_RUPAY_TOTAL_AMOUNT					("DC_RUPAY_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	DC_TOTAL_AMOUNT_INTERNATIONAL			("DC_TOTAL_AMOUNT_INTERNATIONAL", 3, 12, false, FieldFormatType.AMOUNT, false),
	NB_TOTAL_AMOUNT							("NB_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	UP_TOTAL_AMOUNT							("UP_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	AD_TOTAL_AMOUNT							("AD_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	WL_TOTAL_AMOUNT							("WL_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	PC_TOTAL_AMOUNT							("PC_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	CD_TOTAL_AMOUNT							("CD_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	AP_TOTAL_AMOUNT							("AP_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	CR_TOTAL_AMOUNT							("CR_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	IN_TOTAL_AMOUNT							("IN_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	EMI_CC_TOTAL_AMOUNT						("EMI_CC_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	EMI_DC_TOTAL_AMOUNT						("EMI_DC_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	MQR_TOTAL_AMOUNT						("MQR_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	PC_TOTAL_AMOUNT_INTERNATIONAL			("PC_TOTAL_AMOUNT_INTERNATIONAL", 3, 12, false, FieldFormatType.AMOUNT, false),
	SURCHARGE_AMOUNT						("SURCHARGE_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	TOTAL_AMOUNT						    ("TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	PRODUCT_PRICE						    ("PRODUCT_PRICE", 2, 12, false, FieldFormatType.SPECIAL, false),
	SALE_AMOUNT						    	("SALE_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	SALE_TOTAL_AMOUNT						("SALE_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	SERVICE_TAX								("SERVICE_TAX", 3, 12, false, FieldFormatType.AMOUNT, false),
	DB_PG_SETTLED_AMOUNT					("DB_PG_SETTLED_AMOUNT", 1, 100, false, FieldFormatType.AMOUNT, false),	
	DB_ACQUIRER_SETTLED_AMOUNT				("DB_ACQUIRER_SETTLED_AMOUNT", 1, 100, false, FieldFormatType.AMOUNT, false),
	DB_DIFFERENCE_AMOUNT					("DB_DIFFERENCE_AMOUNT", 1, 100, false, FieldFormatType.AMOUNT, false),	
	REFUND_LIMIT_USED						("REFUND_LIMIT_USED", 1, 100, false, FieldFormatType.AMOUNT, false),	
	REFUND_FLAG 							("REFUND_FLAG",1,1,false, FieldFormatType.ALPHA, false),
	DELTA_REFUND_FLAG 						("DELTA_REFUND_FLAG",1,3,false, FieldFormatType.ALPHA, false),
	REFUND_ORDER_ID							("REFUND_ORDER_ID", 1, 50, false, FieldFormatType.SPECIAL, false),
	DISCOUNT								("DISCOUNT", 1, 30, false, FieldFormatType.NUMBER, false),
	DISCOUNT_FLAG							("DISCOUNT", 1, 1, false, FieldFormatType.ALPHA, false),
	//txn channel
	INTERNAL_TXN_CHANNEL					("INTERNAL_TXN_CHANNEL",3,3,false, FieldFormatType.NUMBER, false),
	POST_SETTLED_FLAG						("POST_SETTLED_FLAG",0,30,false, FieldFormatType.SPECIAL, false),
	SETTLED_FLAG							("SETTLED_FLAG",0,30,false, FieldFormatType.SPECIAL, false),
	//Part Settle
    PART_SETTLE                				("PART_SETTLE", 1, 1, false, FieldFormatType.ALPHA, false),
	
    //txn Flag
	TRANSACTION_EMAILER_FLAG			    ("transactionEmailerFlag",1,1,false, FieldFormatType.NUMBER, false),
	//MIGS Processing
	MIGS_FINAL_REQUEST			            ("MIGS_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
	
	//Reco Refund
	DB_PG_REF_NUM				("DB_PG_REF_NUM", 1, 100, false, FieldFormatType.ALPHANUM, false),
	DB_ORDER_ID 				("DB_ORDER_ID", 1, 50, false, FieldFormatType.SPECIAL, false),
	DB_ACQ_ID					("DB_ACQ_ID", 0, 19, false, FieldFormatType.ALPHANUM, false),
	DB_USER_TYPE				("DB_USER_TYPE", 0, 19, false, FieldFormatType.ALPHANUM, false),
		
	//Payment Gateway Processing - Net Banking
	PAYMENT_GATEWAY_FINAL_REQUEST			("PAYMENT_GATEWAY_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
	PAYMENT_GATEWAY_FINAL_ENC_RESPONSE		("PAYMENT_GATEWAY_FINAL_ENC_RESPONSE", 1, 1000, false, FieldFormatType.SPECIAL, false),
	INDUSTRY_ID					("INDUSTRY_ID",2,350,false,FieldFormatType.ALPHA,false),
	
	// UPI 
	UPI_RESPONSE_MESSAGE		("UPI_RESPONSE_MESSAGE", 1, 256,false, FieldFormatType.ALPHANUM, false),
	UPI_RESPONSE_CODE			("UPI_RESPONSE_CODE", 1, 10, false, FieldFormatType.ALPHANUM, false),
	UPI_STATUS					("UPI_STATUS", 2, 30, false, FieldFormatType.SPECIAL, false),
	UPI_PG_RESPONSE_MESSAGE		("UPI_PG_RESPONSE_MESSAGE", 1, 256,false, FieldFormatType.ALPHANUM, false),
	UPI_PG_RESPONSE_CODE		("UPI_PG_RESPONSE_CODE", 1, 10, false, FieldFormatType.ALPHANUM, false),
	PAYER_ADDRESS				("PAYER_ADDRESS", 1, 255, false, FieldFormatType.UPIADDRESS, false),
	PAYER_PHONE				    ("PAYER_PHONE", 8, 15, false, FieldFormatType.GOOGLEPAYSPECIAL, true),
	PAYER_NAME					("PAYER_NAME", 1, 90, false, FieldFormatType.ALPHASPACE, true),
	PAYEE_ADDRESS				("PAYEE_ADDRESS", 1, 255, false, FieldFormatType.UPIADDRESS, false),
	VPA_MASK					("VPA_MASK",1, 255, false, FieldFormatType.SPECIAL, true),
	
	
	MCC_CODE					("MCC_CODE",1, 255, false, FieldFormatType.SPECIAL, true),
	
	//walletType
	CARD_INFO					("CARD_INFO", 0, 128, false, FieldFormatType.ALPHASPACENUM, false),
	PAYTMENT_MODE				("PAYTMENT_MODE", 0, 128, false, FieldFormatType.ALPHASPACENUM, false),
	
	//Federal fields
	FEDERAL_CAVV				("FEDERAL_CAVV", 1, 100, false, FieldFormatType.SPECIAL, false),
	FEDERAL_XID					("FEDERAL_XID", 1, 100, false, FieldFormatType.SPECIAL, false),
	FEDERAL_STATUS				("FEDERAL_STATUS",1,1,false, FieldFormatType.ALPHA, false),
	FEDERAL_ECI					("FEDERAL_ECI",1,5,false, FieldFormatType.NUMBER, false),
	FEDERAL_MD					("FEDERAL_MD",16,20,false, FieldFormatType.NUMBER, false),
	FEDERAL_MPIERROR_CODE		("FEDERAL_MPIERROR_CODE", 1, 10, false, FieldFormatType.ALPHANUM, false),
	FEDERAL_RESPONSE_MESSAGE	("FEDERAL_RESPONSE_MESSAGE", 1, 256,false, FieldFormatType.ALPHASPACENUM, false),
	FEDERAL_MPI_ID				("FEDERAL_MPI_ID",1,100,false, FieldFormatType.NUMBER, false),
	FEDERAL_ENROLL_FINAL_REQUEST("FEDERAL_ENROLL_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
	PHONEPE_RESPONSE_FIELD("PHONEPE_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.PAYTMSPECIAL, true),
	PHONEPE_FINAL_REQUEST("PHONEPE_FINAL_REQUEST", 1, 2000, false, FieldFormatType.PAYTMSPECIAL, true),
	
	UDF1						("UDF1", 1, 255, false, FieldFormatType.UPIADDRESS, false),
	UDF2						("UDF2", 1, 90, false, FieldFormatType.ALPHA, false),
	UDF3						("UDF3", 1, 255, false, FieldFormatType.UPIADDRESS, false),
	UDF4						("UDF4", 1, 90, false, FieldFormatType.ALPHASPACENUM, false),
	UDF5						("UDF5", 1, 500, false, FieldFormatType.SPECIAL, false),
	UDF6						("UDF6", 1, 90, false, FieldFormatType.SPECIAL, false),
	UDF7						("UDF7", 1, 90, false, FieldFormatType.PERIODNUM, false),
	UDF8						("UDF8", 1, 90, false, FieldFormatType.PERIODNUM, false),
	UDF9						("UDF9", 1, 90, false, FieldFormatType.PERIODNUM, false),
	UDF10						("UDF10", 1, 90, false, FieldFormatType.PERIODNUM, false),
	UDF11						("UDF11", 1, 500, false, FieldFormatType.SPECIAL, false),
	UDF12						("UDF12", 1, 500, false, FieldFormatType.SPECIAL, false),
	UDF13						("UDF13", 1, 500, false, FieldFormatType.SPECIAL, false),
	UDF14						("UDF14", 1, 500, false, FieldFormatType.SPECIAL, false),
	UDF15						("UDF15", 1, 500, false, FieldFormatType.SPECIAL, false),
	UDF16						("UDF16", 1, 500, false, FieldFormatType.SPECIAL, false),
	UDF17						("UDF17", 1, 500, false, FieldFormatType.SPECIAL, false),
	UDF18						("UDF18", 1, 500, false, FieldFormatType.SPECIAL, false),
	
	ADF1						("ADF1", 1, 250, false, FieldFormatType.SPECIAL, false),
	ADF2						("ADF2", 1, 250, false, FieldFormatType.SPECIAL, false),
	ADF3						("ADF3", 1, 250, false, FieldFormatType.SPECIAL, false),
	ADF4						("ADF4", 1, 250, false, FieldFormatType.SPECIAL, false),
	ADF5						("ADF5", 1, 250, false, FieldFormatType.SPECIAL, false),
	ADF6						("ADF6", 1, 250, false, FieldFormatType.SPECIAL, false),
	ADF7						("ADF7", 1, 250, false, FieldFormatType.SPECIAL, false),
	ADF8						("ADF8", 1, 250, false, FieldFormatType.SPECIAL, false),
	ADF9						("ADF9", 1, 250, false, FieldFormatType.SPECIAL, false),
	ADF10						("ADF10", 1, 250, false, FieldFormatType.SPECIAL, false),
	ADF11						("ADF11", 1, 250, false, FieldFormatType.SPECIAL, false),
	ENCDATA						("ENCDATA",1,2000,false,FieldFormatType.SPECIAL, false),
	IV							("IV", 1, 200, false, FieldFormatType.SPECIAL, false),
	RESP_IV						("RESP_IV", 1, 200, false, FieldFormatType.SPECIAL, false),
	RESP_TXN_KEY				("RESP_TXN_KEY", 6, 100, false, FieldFormatType.SPECIAL, false),
	BOB_FINAL_REQUEST			("BOB_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
	BOB_RESPONSE_FIELD			("BOB_RESPONSE_FIELD", 1, 1000, false, FieldFormatType.BOBSPECIAL, false),
	HDFC_FINAL_REQUEST			("HDFC_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
	HDFC_RESPONSE_FIELD			("HDFC_RESPONSE_FIELD", 1, 1000, false, FieldFormatType.BOBSPECIAL, false),
	LYRA_FINAL_REQUEST			("LYRA_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
	LYRA_RESPONSE_FIELD			("LYRA_RESPONSE_FIELD", 1, 1000, false, FieldFormatType.BOBSPECIAL, false),
	FSS_PAY_FINAL_REQUEST		("FSS_PAY_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
	FSS_PAY_RESPONSE_FIELD		("FSS_PAY_RESPONSE_FIELD", 1, 1000, false, FieldFormatType.BOBSPECIAL, false),
	BILLDESK_FINAL_REQUEST		("BILLDESK_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	BILLDESK_RESPONSE_FIELD		("BILLDESK_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.BOBSPECIAL, false),
	KOTAK_FINAL_REQUEST			("KOTAK_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
	KOTAK_RESPONSE_FIELD		("KOTAK_RESPONSE_FIELD", 1, 1000, false, FieldFormatType.SPECIAL, false),
	IDBI_FINAL_REQUEST			("IDBI_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
	IDBI_RESPONSE_FIELD		    ("IDBI_RESPONSE_FIELD", 1, 1000, false, FieldFormatType.SPECIAL, false),
	MATCH_MOVE_FINAL_REQUEST	("MATCH_MOVE_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
	MATCH_MOVE_RESPONSE_FIELD   ("MATCH_MOVE_RESPONSE_FIELD", 1, 1000, false, FieldFormatType.SPECIAL, false),
	ISGPAY_FINAL_REQUEST		("ISGPAY_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
	ISGPAY_RESPONSE_FIELD		("ISGPAY_RESPONSE_FIELD", 1, 10000, false, FieldFormatType.ISGPAYSPECIAL, false),
	UPI_QR_CODE					("UPI_QR_CODE", 1, 100000, false, FieldFormatType.SPECIAL, false),
	PG_QR_CODE                  ("PG_QR_CODE", 1, 100000, false, FieldFormatType.SPECIAL, false),
	MQR_QR_CODE                  ("MQR_QR_CODE", 1, 100000, false, FieldFormatType.SPECIAL, false),
	PAYPHI_FINAL_REQUEST		("PAYPHI_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
	PAYPHI_RESPONSE_FIELD		("PAYPHI_RESPONSE_FIELD", 1, 10000, false, FieldFormatType.SPECIAL, false),
	AKONTOPAY_FINAL_REQUEST		("AKONTOPAY_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
	AKONTOPAY_RESPONSE_FIELD	("AKONTOPAY_RESPONSE_FIELD", 1, 10000, false, FieldFormatType.SPECIAL, false),
	SBI_FINAL_REQUEST			("SBI_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	SBI_RESPONSE_FIELD			("SBI_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.SPECIAL, false),
	PAYU_FINAL_REQUEST			("PAYU_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	PAYU_RESPONSE_FIELD			("PAYU_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.SPECIAL, false),
	APEXPAY_FINAL_REQUEST		("APEXPAY_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	APEXPAY_RESPONSE_FIELD		("APEXPAY_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.SPECIAL, false),
	SAFEXPAY_FINAL_REQUEST		("SAFEXPAY_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	SAFEXPAY_RESPONSE_FIELD		("SAFEXPAY_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.BOBSPECIAL, false),
	PAYTM_RESPONSE_FIELD		("PAYTM_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.PAYTMSPECIAL, true),
	PAYTM_FINAL_REQUEST			("PAYTM_FINAL_REQUEST", 1, 2000, false, FieldFormatType.PAYTMSPECIAL, true),
	IPINT_RESPONSE_FIELD		("IPINT_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.PAYTMSPECIAL, true),
	IPINT_FINAL_REQUEST			("IPINT_FINAL_REQUEST", 1, 2000, false, FieldFormatType.PAYTMSPECIAL, true),
	AAMARPAY_RESPONSE_FIELD		("AAMARPAY_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.PAYTMSPECIAL, true),
	AAMARPAY_FINAL_REQUEST		("AAMARPAY_FINAL_REQUEST", 1, 2000, false, FieldFormatType.PAYTMSPECIAL, true),
	CASHFREE_RESPONSE_FIELD		("CASHFREE_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.PAYTMSPECIAL, true),
	CASHFREE_FINAL_REQUEST 		("CASHFREE_FINAL_REQUEST", 1, 2000, false, FieldFormatType.PAYTMSPECIAL, true),
	AXISBANK_NB_FINAL_REQUEST		("AXISBANK_NB_FINAL_REQUEST", 1, 2000, false, FieldFormatType.DIRECPAYSPECIAL, true),
	AXISBANK_NB_RESPONSE_FIELD		("AXISBANK_NB_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.DIRECPAYSPECIAL, true),
	AXISBANK_UPI_RESPONSE_FIELD		("AXISBANK_UPI_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.DIRECPAYSPECIAL, true),
	PAYG_FINAL_REQUEST			("PAYG_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	PAYG_RESPONSE_FIELD			("PAYG_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.SPECIAL, false),
	ZAAKPAY_FINAL_REQUEST		("ZAAKPAY_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
	ZAAKPAY_RESPONSE_FIELD		("ZAAKPAY_RESPONSE_FIELD", 1, 10000, false, FieldFormatType.SPECIAL, false),
	VEPAY_FINAL_REQUEST		("VEPAYPAY_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	VEPAY_RESPONSE_FIELD		("VEPAYPAY_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.BOBSPECIAL, false),
	SUPPORTED_PAYMENT_TYPE		("SUPPORTED_PAYMENT_TYPE", 1, 250, false, FieldFormatType.SPECIAL, false),
	MERCHANT_PAYMENT_TYPE		("MERCHANT_PAYMENT_TYPE", 1, 250, false, FieldFormatType.ALPHANUM, false),
	INSERTION_DATE				("INSERTION_DATE", 1, 30, false, FieldFormatType.SPECIAL, false),
	PAYMENTS_REGION				("PAYMENTS_REGION", 1, 90, false, FieldFormatType.ALPHA, false),
	CARD_HOLDER_TYPE			("CARD_HOLDER_TYPE", 1, 90, false, FieldFormatType.ALPHA, false),
	EXCEPTION_STATUS			("EXCEPTION_STATUS", 5, 30, false, FieldFormatType.SPECIAL, false),
	DB_ACQUIRER_TYPE			("DB_ACQUIRER_TYPE", 3, 10, false, FieldFormatType.ALPHA, false),
	DB_PAY_ID					("DB_PAY_ID", 2, 36, false, FieldFormatType.NUMBER, false),
	
	// Add Beneficiary Parameters
	CUST_ID_BENEFICIARY			("CUST_ID_BENEFICIARY", 1, 250, false, FieldFormatType.SPECIAL, false),
	BENEFICIARY_CD				("BENEFICIARY_CD", 1, 250, false, FieldFormatType.SPECIAL, false),
	BENE_NAME					("BENE_NAME", 1, 250, false, FieldFormatType.SPECIAL, false),
	BENE_ACCOUNT_NO				("BENE_ACCOUNT_NO", 1, 250, false, FieldFormatType.ALPHANUM, false),
	IFSC_CODE					("IFSC_CODE", 1, 250, false, FieldFormatType.ALPHANUM, false),
	PAYMENT_TYPE_BANK			("PAYMENT_TYPE_BANK", 1, 250, false, FieldFormatType.SPECIAL, false),
	BENE_TYPE					("BENE_TYPE", 1, 250, false, FieldFormatType.ALPHA, false),
	CURRENCY_CD					("CURRENCY_CD", 1, 250, false, FieldFormatType.NUMBER, false),
	NODAL_ACQUIRER				("NODAL_ACQUIRER", 1, 250, false, FieldFormatType.SPECIAL, false),
	BANK_NAME					("BANK_NAME", 1, 250, false, FieldFormatType.SPECIAL, false),
	SRC_ACCOUNT_NO				("SRC_ACCOUNT_NO", 1, 250, false, FieldFormatType.NUMBER, false),
	CAPTURED_DATE_FROM			("CAPTURED_DATE_FROM", 1, 250, false, FieldFormatType.SPECIAL, false),
	CAPTURED_DATE_TO				("CAPTURED_DATE_TO", 1, 250, false, FieldFormatType.SPECIAL, false),
	SETTLED_DATE					("SETTLED_DATE", 1, 250, false, FieldFormatType.SPECIAL, false),
	ACQUIRER_NAME					("ACQUIRER_NAME", 1, 250, false, FieldFormatType.SPECIAL, false),
	
	//Settlement TXN parameters
	ATTEMPT_NO				("ATTEMPT_NO", 1, 250, false, FieldFormatType.NUMBER, false),
	APP_ID					("APP_ID", 1, 250, false, FieldFormatType.ALPHANUM, false),
	CUSTOMER_ID				("CUSTOMER_ID", 1, 250, false, FieldFormatType.ALPHANUM, false),
	PURPOSE_CODE			("PURPOSE_CODE", 1, 250, false, FieldFormatType.ALPHANUM, false),
	REQUESTED_BY			("REQUESTED_BY", 1, 250, false, FieldFormatType.SPECIAL, false),
	NODAL_SETTLEMENT_DATE	("NODAL_SETTLEMENT_DATE", 1, 250, false, FieldFormatType.SPECIAL, false),
	NODAL_PAYOUT_DATE		("NODAL_PAYOUT_DATE", 1, 250, false, FieldFormatType.SPECIAL, false),
	
	//Settlement Report parameters
	ACQUIRER_TDR_SC ("ACQUIRER_TDR_SC", 1, 12, false, FieldFormatType.SPECIAL, false),
	ACQUIRER_GST ("ACQUIRER_GST", 1, 12, false, FieldFormatType.SPECIAL, false), 
	PG_GST ("PG_GST", 1, 12, false, FieldFormatType.SPECIAL, false),
	PG_TDR_SC ("PG_TDR_SC", 1, 12, false, FieldFormatType.SPECIAL, false), 
	MERCHANT_TDR_SC ("MERCHANT_TDR_SC", 1, 12, false, FieldFormatType.SPECIAL, false),
	MERCHANT_GST ("MERCHANT_GST", 1, 12, false, FieldFormatType.SPECIAL, false), 
	
	SUF_TDR("SUF_TDR", 1, 12, false, FieldFormatType.SPECIAL, false),
	SUF_GST ("SUF_GST", 1, 12, false, FieldFormatType.SPECIAL, false), 
	
	
	RESELLER_CHARGES ("RESELLER_CHARGES", 1, 12, false, FieldFormatType.SPECIAL, false),
	RESELLER_GST ("RESELLER_GST", 1, 12, false, FieldFormatType.SPECIAL, false), 
	PG_RESELLER_CHARGE ("PG_RESELLER_CHARGE", 1, 12, false, FieldFormatType.SPECIAL, false),
	PG_RESELLER_GST ("PG_RESELLER_GST", 1, 12, false, FieldFormatType.SPECIAL, false), 
	
	//old remove later
	SURCHARGE_ACQ				("SURCHARGE_ACQ", 3, 12, false, FieldFormatType.AMOUNT, false),
	GST_ACQ						("GST_ACQ", 3, 12, false, FieldFormatType.AMOUNT, false),
	SURCHARGE_PG			("SURCHARGE_PG", 3, 12, false, FieldFormatType.AMOUNT, false),
	GST_PG				("GST_PG", 3, 12, false, FieldFormatType.AMOUNT, false),
	SURCHARGE_PAYMENT_GATEWAY	("SURCHARGE_PAYMENT_GATEWAY", 3, 12, false, FieldFormatType.AMOUNT, false),
	GST_PAYMENT_GATEWAY			("GST_PAYMENT_GATEWAY", 3, 12, false, FieldFormatType.AMOUNT, false),
	
	//EMI
	TENURE					("TENURE", 1, 10, false, FieldFormatType.NUMBER, false),
	RATE_OF_INTEREST		("RATE_OF_INTEREST", 1, 10, false, FieldFormatType.NUMBER, false),
	EMI_TOTAL_AMOUNT		("EMI_TOTAL_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	EMI_PER_MONTH			("EMI_PER_MONTH", 3, 12, false, FieldFormatType.AMOUNT, false),
	ISSUER_BANK				("ISSUER_BANK", 1, 256, false, FieldFormatType.ALPHANUM, true),
	EMI_INTEREST			("EMI_INTEREST", 3, 12, false, FieldFormatType.AMOUNT, false),

	//ICICI-ECOLLECTION RESPONSE DATA
	CUSTOMER_CODE						("CUSTOMER_CODE", 1, 5, false, FieldFormatType.ALPHA, true),
	VIRTUAL_AC_CODE						("VIRTUAL_AC_CODE", 1, 14, false, FieldFormatType.ALPHANUM, false),
	UTR									("UTR", 1, 14, false, FieldFormatType.ALPHANUM, false),
	SENDER_REMARK						("SENDER_REMARK", 1, 256, false, FieldFormatType.ALPHA, true),
	CUSTOMER_ACCOUNT_NO					("CUSTOMER_ACCOUNT_NO", 1, 250, false, FieldFormatType.SPECIAL, false),
	PAYEE_NAME							("PAYEE_NAME", 1, 150, false, FieldFormatType.ALPHA, true),
	PAYEE_ACCOUNT_NUMBER				("PAYEE_ACCOUNT_NUMBER", 1, 250, false, FieldFormatType.NUMBER, false),
	PAYEE_BANK_IFSC						("PAYEE_BANK_IFSC", 1, 150, false, FieldFormatType.ALPHANUM, false),
	CLOSING_DATE                        ("CLOSING_DATE", 19, 19, false, FieldFormatType.SPECIAL, false),
	OPENING_AMOUNT					    ("OPENING_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	CREDIT_AMOUNT                       ("CREDIT_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	DEBIT_AMOUNT                        ("DEBIT_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
    //ICICI-CIB
    
    AGGR_ID                 ("AGGRID", 1, 100, false, FieldFormatType.ALPHANUM, false),
    AGGR_NAME               ("AGGRNAME", 1, 256, false, FieldFormatType.ALPHANUM, false),
    CORP_ID                 ("CORPID", 1, 32, false, FieldFormatType.ALPHANUM, false),
    USER_ID                 ("USERID", 1, 32, false, FieldFormatType.ALPHANUM, false),
    URN                     ("URN", 1, 40, false, FieldFormatType.ALPHANUM, true),
    ALIAS_ID                ("ALIASID", 1, 40, false, FieldFormatType.ALPHANUM, true),
    UNIQUE_ID               ("UNIQUEID", 1, 40, false, FieldFormatType.ALPHANUM, false),
    DEBIT_AC                ("DEBITACC", 1, 34, false, FieldFormatType.NUMBER, false),
    CREDIT_AC               ("CREDITACC", 1, 34, false, FieldFormatType.NUMBER, false),
    IFSC                    ("IFSC", 3, 20, false, FieldFormatType.ALPHANUM, false),
    PAYEENAME               ("PAYEENAME", 1, 80, false, FieldFormatType.ALPHASPACENUM, false),
    PAYERNAME               ("PAYERNAME", 1, 80, false, FieldFormatType.ALPHASPACENUM, false),
    REMARKS                 ("REMARKS", 1, 255, false, FieldFormatType.ALPHASPACENUM, false),
    ACCOUNT_NO              ("ACCOUNTNO", 1, 200, false, FieldFormatType.ALPHANUM, false),
    PAYER_ACCOUNT_NO        ("PAYER_ACOUNT_NO", 1, 50, false, FieldFormatType.ALPHANUM, false),
    PAYER_IFSC                ("PAYER_IFSC", 1, 50, false, FieldFormatType.ALPHANUM, false),
    PAYEE_IFSC                ("PAYEE_IFSC", 1, 50, false, FieldFormatType.ALPHANUM, false),
    OTP                        ("OTP", 1, 10, false, FieldFormatType.ALPHANUM, false),
    UTR_NO                    ("UTR_NO", 1, 50, false, FieldFormatType.ALPHANUM, false),
    
    TRANSACTION_OF         ("TRANSACTION_OF", 1, 50, false, FieldFormatType.ALPHA, false),
    CIB_TRANSACTION         ("CIB_TRANSACTION", 1, 50, false, FieldFormatType.ALPHA, false),
    CIB_COMPOSITE            ("CIB_COMPOSITE", 1, 50, false, FieldFormatType.ALPHA, false),

    
    BENE_AGGR_ID            ("AGGR_ID", 1, 100, false, FieldFormatType.ALPHANUM, false),
    BENE_CORP_ID            ("CrpId", 1, 32, false, FieldFormatType.ALPHANUM, false),
    BENE_USER_ID            ("CrpUsr", 1, 32, false, FieldFormatType.ALPHANUM, false),
    BENE_ACCOUNT               ("BnfAccNo", 1, 32, false, FieldFormatType.ALPHANUM, false),
    BENE_ACCOUNTNAME        ("BnfName", 1, 32, false, FieldFormatType.ALPHANUM, false),
    BENE_NICKNAME           ("BnfNickName", 1, 32, false, FieldFormatType.ALPHANUM, false),
    BENE_PAYEETYPE         ("PayeeType", 1, 1, false, FieldFormatType.ALPHA, false),
    DATEFROM                 ("FROMDATE", 1, 20, false, FieldFormatType.ALPHA, false),
    DATETO                     ("TODATE", 1, 20, false, FieldFormatType.ALPHA, false),
    BENE_ALIAS				("ALIAS", 1, 32, false, FieldFormatType.ALPHANUM, false),
    BENE_PAYEE_TYPE         ("PAYEE_TYPE", 1, 1, false, FieldFormatType.ALPHA, false),
    BENE_DEFAULT         	("BENE_DEFAULT", 1, 10, false, FieldFormatType.ALPHANUM, false),
    
    REQUEST_TYPE            ("REQUESTTYPE", 1, 30, false, FieldFormatType.ALPHA, false),
    REQ_REGISTRATION        ("registration", 1, 30, false, FieldFormatType.ALPHA, false),
    REQ_REGISTRATION_STATUS ("registrationStatus", 1, 30, false, FieldFormatType.ALPHA, false),
    REQ_ADDBENE                ("addBene", 1, 30, false, FieldFormatType.ALPHA, false),
    REQ_VALIDBENE           ("validateBene", 1, 30, false, FieldFormatType.ALPHA, false),
    REQ_BALANCE_INQUIRY     ("balanceInquiry", 1, 30, false, FieldFormatType.ALPHA, false),
    REQ_ACCOUNT_STATEMENT   ("accountStatement", 1, 30, false, FieldFormatType.ALPHA, false),
    REQ_TRANSACTION         ("transaction", 1, 30, false, FieldFormatType.ALPHA, false),
    REQ_TRANSACTION_INQUIRY ("transactionInquiry", 1, 30, false, FieldFormatType.ALPHA, false),
    
    PG_RESPONSE_MSG			("PG_RESPONSE_MSG", 1, 500, false, FieldFormatType.SPECIAL, false),
    PG_RESPONSE_STATUS		("PG_RESPONSE_STATUS", 1, 50, false, FieldFormatType.ALPHASPACE, false),
    PG_ERROR_CODE			("PG_ERROR_CODE", 1, 50, false, FieldFormatType.NUMBER, false),
    RESPONSE                ("RESPONSE", 1, 500, false, FieldFormatType.ALPHANUM, false),
    
    REQID                    ("REQID", 1, 100, false, FieldFormatType.ALPHANUM, false),
    
    PHONE_NO					("PHONE_NO", 8, 15, false, FieldFormatType.NUMBER, false),
    USER_TYPE					("USER_TYPE", 8, 15, false, FieldFormatType.ALPHA, false),
    BENE_NAME_REQUEST			("BENE_NAME_REQUEST", 1, 250, false, FieldFormatType.SPECIAL, false),
    BANK_REF_NUM                ("BANK_REF_NUM", 1, 100, false, FieldFormatType.ALPHANUM, false),
	BENE_REGISTRATION 			("BENE_REGISTRATION", 1, 50, false, FieldFormatType.ALPHANUM, false),
	
	MPA_FLAG 					("MPA_FLAG", 1, 1, false, FieldFormatType.ALPHA, false),

	// Invoice
	NAME					("NAME", 3, 50, false, FieldFormatType.ALPHASPACENUM, false),
	PRODUCT_NAME 			("PRODUCT_NAME", 1, 100, false, FieldFormatType.SPECIAL, false),
	PRODUCT_DESCRIPTION 	("PRODUCT_DESCRIPTION", 1, 100, false, FieldFormatType.SPECIAL, false),
	PHONE					("PHONE", 8, 15, false, FieldFormatType.NUMBER, false),
	DURATION_FROM			("DURATION_FROM", 1, 100, false, FieldFormatType.SPECIAL, false),
	DURATION_TO				("DURATION_TO", 1, 100, false, FieldFormatType.SPECIAL, false),
	EXPIRY_DATE				("EXPIRY_DATE", 1, 20, false, FieldFormatType.SPECIAL, false),
	EXPIRY_TIME				("EXPIRY_TIME", 1, 20, false, FieldFormatType.SPECIAL, false),
	CURRENCY				("CURRENCY", 3, 3, false, FieldFormatType.ALPHA, false),
	QUANTITY				("QUANTITY", 1, 3, false, FieldFormatType.NUMBER, false),
	SERVICE_CHARGE			("SERVICE_CHARGE", 3, 12, false, FieldFormatType.AMOUNT, false),
	ADDRESS					("ADDRESS", 2, 250, false, FieldFormatType.SPECIAL, true),
	COUNTRY					("COUNTRY", 2, 250, false, FieldFormatType.ALPHASPACE, true),
	STATE					("STATE", 2, 50, false, FieldFormatType.ALPHASPACE, true),
	CITY					("CITY", 2, 100, false, FieldFormatType.ALPHASPACE, true),
	PIN						("PIN", 2, 100, false, FieldFormatType.ALPHANUM, true),
	EMAIL					("EMAIL", 6, 120, false, FieldFormatType.EMAIL, false),	
	SUB_USER_ID             ("SUB_USER_ID", 1, 30, false, FieldFormatType.SPECIAL, false),
	MAX_AMOUNT				("MAX_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	SETTLEMENT_FLAG			("SETTLEMENT_FLAG", 1, 12, false, FieldFormatType.ALPHASPACE, false),
	MONTHLY_AMOUNT			("MONTHLY_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	
	//IDFC NetBanking
	IDFC_NETBANKING_FINAL_REQUEST		("IDFC_NETBANKING_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false), 
	IDFC_NB_RESPONSE_FIELDS				("IDFC_NB_RESPONSE_FIELDS", 1, 10000, false, FieldFormatType.ISGPAYSPECIAL, false),
	
	EPOS_PAYMENT_OPTION					("EPOS_PAYMENT_OPTION", 1, 20, false, FieldFormatType.ALPHASPACE, true), 
	EPOS_MERCHANT						("EPOS_MERCHANT", 1, 5, false, FieldFormatType.ALPHA, true),
	INVOICE_ID          			  	("INVOICE_ID", 2, 20, false, FieldFormatType.NUMBER, true),
	TRANSACTION_MODE					("TRANSACTION_MODE", 1, 20, false, FieldFormatType.SPECIAL, false),
	TXN_CAPTURE_FLAG					("TXN_CAPTURE_FLAG", 1, 20, false, FieldFormatType.SPECIAL, false),
	
	PAYMENT_MODE                        ("MODE",1, 20, false, FieldFormatType.ALPHA, false),
	PAYEE_PAYMENT_DATE                  ("PAYEEPAYMENTDATE",1, 250, false, FieldFormatType.SPECIAL, false),
	BANK_INTERNAL_TRANSACTION_NUMBER    ("BANK_INTERNAL_TRANSACTION_NUMBER",2, 100, false, FieldFormatType.ALPHANUM, false),
	PROD_DESC                  			("PROD_DESC",1, 2500, false, FieldFormatType.KHADI_SPECIAL, false),
	
	//Wallet Load
	NUMERIC_SERIES                      ("NUMERIC_SERIES",1, 6, false, FieldFormatType.NUMBER, false),
	NUMERIC_SERIES_RECIEPT              ("NUMERIC_SERIES_RECIEPT",1, 7, false, FieldFormatType.NUMBER, false),
	NUMERIC_SERIES_BATCH                ("NUMERIC_SERIES_BATCH",1, 6, false, FieldFormatType.NUMBER, false),
	ALPHA_SERIES                        ("ALPHA_SERIES",1, 6, false, FieldFormatType.ALPHA, false),
	
	//P2P
	P2PTSP_UPI_LINK				 		 ("P2PTSP_UPI_LINK", 1, 2000, false, FieldFormatType.ALPHANUM, false),
	
	//MSCDCL 
	BU									("BU",1, 4, false, FieldFormatType.NUMBER, false),
	CCCODE								("CC_CODE",1, 2, false, FieldFormatType.NUMBER, false),
	Z_NAME								("Z_NAME",2, 250, false, FieldFormatType.SPECIAL, true),
	C_NAME								("C_NAME",2, 250, false, FieldFormatType.SPECIAL, true),
	D_NAME								("D_NAME",2, 250, false, FieldFormatType.SPECIAL, true),
	RECIEPT_NO							("RECIEPT_NO", 1, 7, false, FieldFormatType.ALPHANUM , false),
	BATCH_NO							("BATCH_NO", 1, 6, false, FieldFormatType.NUMBER, false),
	DUE_DATE							("DUE_DATE", 1, 11, false, FieldFormatType.SPECIAL, false),
	REGISTRATION_DATE                   ("REGISTRATION_DATE", 10, 19, false, FieldFormatType.SPECIAL, false),
	NOTE								("NOTE", 0, 150, false, FieldFormatType.ALPHASPACENUM, false),
	PURPOSE								("PURPOSE", 2, 10, false, FieldFormatType.ALPHA, false),
	UMN									("UMN", 5, 100,false, FieldFormatType.UPIADDRESS, false),
	SEQUENCE_NO							("SEQUENCE_NO", 1, 100,false, FieldFormatType.NUMBER, false),
	NOTIFICATION_DATE					("NOTIFICATION_DATE", 1, 11, false, FieldFormatType.SPECIAL, false),
	
	//Dasboard Fields
	TOTAL_SALE_SUCCESS             		("TOTAL_SALE_SUCCESS", 2, 36, false, FieldFormatType.NUMBER, false),
	TOTAL_REFUND_SUCCESS             	("TOTAL_REFUND_SUCCESS", 2, 36, false, FieldFormatType.NUMBER, false),
	TOTAL_SALE_FAILED             		("TOTAL_SALE_FAILED", 2, 36, false, FieldFormatType.NUMBER, false),
	TOTAL_REFUND_FAILED             	("TOTAL_REFUND_FAILED", 2, 36, false, FieldFormatType.NUMBER, false),
	TOTAL_SALE_REJECTED_DECLINED        ("TOTAL_SALE_REJECTED_DECLINED", 2, 36, false, FieldFormatType.NUMBER, false),
	TOTAL_REFUND_REJECTED_DECLINED      ("TOTAL_REFUND_REJECTED_DECLINED", 2, 36, false, FieldFormatType.NUMBER, false),
	TOTAL_SALE_DROPPED             		("TOTAL_SALE_DROPPED", 2, 36, false, FieldFormatType.NUMBER, false),
	TOTAL_REFUND_DROPPED             	("TOTAL_REFUND_DROPPED", 2, 36, false, FieldFormatType.NUMBER, false),
	TOTAL_SALE_CANCELLED             	("TOTAL_SALE_CANCELLED", 2, 36, false, FieldFormatType.NUMBER, false),
	TOTAL_REFUND_CANCELLED             	("TOTAL_REFUND_CANCELLED", 2, 36, false, FieldFormatType.NUMBER, false),
	TOTAL_SALE_FRAUD             		("TOTAL_SALE_FRAUD", 2, 36, false, FieldFormatType.NUMBER, false),
	TOTAL_REFUND_FRAUD             		("TOTAL_REFUND_FRAUD", 2, 36, false, FieldFormatType.NUMBER, false),
	TOTAL_SALE_INVALID             		("TOTAL_SALE_INVALID", 2, 36, false, FieldFormatType.NUMBER, false),
	TOTAL_REFUND_INVALID             	("TOTAL_REFUND_INVALID", 2, 36, false, FieldFormatType.NUMBER, false),
	TOTAL_SALE_AMOUNT					("TOTAL_SALE_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
	TOTAL_REFUND_AMOUNT					("TOTAL_REFUND_AMOUNT", 3, 12, false, FieldFormatType.AMOUNT, false),
    
	
	IS_PAYBLE_MERCHANT                   ("IS_PAYBLE_MERCHANT", 1, 1, false, FieldFormatType.ALPHA, false),
	AUTO_REFUND_FLAG                     ("AUTO_REFUND_FLAG", 1, 1, false, FieldFormatType.ALPHA, false),
	
	VPA                     			 ("VPA", 4, 100, false, FieldFormatType.SPECIAL, false),
	MIN_RANGE							 ("MIN_RANGE", 1, 3, false, FieldFormatType.NUMBER, false),
	MAX_RANGE							 ("MAX_RANGE", 1, 3, false, FieldFormatType.NUMBER, false),
	CREATED_BY							("CREATED_BY", 2, 36, false, FieldFormatType.NUMBER, false),
    //FreeCharge Fields
    FREECHARGE_FINAL_REQUEST             ("FREECHARGE_FINAL_REQUEST", 1, 1000, false, FieldFormatType.SPECIAL, false),
    FREECHARGE_RESPONSE_FIELD            ("FREECHARGE_RESPONSE_FIELD", 1, 1000, false, FieldFormatType.BOBSPECIAL, false),
	FILE_TYPE                     		 ("FILE_TYPE", 1, 10, false, FieldFormatType.ALPHA, false),
	VIRTUAL_ACC_NUM						 ("VIRTUAL_ACC_NUM", 1, 14, false, FieldFormatType.ALPHANUM, false),
	VIRTUAL_ACC_IFSC					 ("VIRTUAL_ACC_IFSC", 1, 12, false, FieldFormatType.ALPHANUM, false),
	VIRTUAL_VPA_NUM						 ("VIRTUAL_VPA_NUM", 1, 14, false, FieldFormatType.ALPHANUM, false),
	PAN						 			 ("PAN", 10, 10, false, FieldFormatType.ALPHANUM, false),
	DOB						 			 ("DOB", 10, 10, false, FieldFormatType.SPECIAL, false),
	AADHAR						 		 ("AADHAR", 12, 12, false, FieldFormatType.NUMBER, false), 
	PAYOUT_TOPUP_FLAG					 ("PAYOUT_TOPUP_FLAG", 1, 6, false, FieldFormatType.ALPHANUM, false),
	PAYOUT_STATEMENT_FLAG			     ("PAYOUT_STATEMENT_FLAG", 1, 6, false, FieldFormatType.ALPHANUM, false),
	// Complaint Fields
	COMPLAINT_ID             			 ("COMPLAINT_ID", 16, 16, false, FieldFormatType.SPECIAL, false),
	UPDATED_BY							 ("UPDATED_BY", 5, 30, false, FieldFormatType.SPECIAL, false),
	COMMENTS							 ("COMMENTS", 1, 1000000, false, FieldFormatType.ALPHA, false),
	COMMENTED_BY						 ("COMMENTED_BY", 5, 30, false, FieldFormatType.SPECIAL, false),
	COMPLAINT_FILE						 ("COMPLAINT_FILE", 1, 100000, false, FieldFormatType.SPECIAL, false),
	COMPLAINT_TYPE						 ("COMPLAINT_TYPE", 1, 100, false, FieldFormatType.ALPHA, false),
	COMPLAINT_RAISE_BY					 ("COMPLAINT_RAISE_BY", 1, 32, false, FieldFormatType.ALPHANUM, false),
	COMPLAINT_RAISE_FOR_USER_TYPE		 ("COMPLAINT_FOR_USER_TYPE", 1, 32, false, FieldFormatType.ALPHANUM, false),
	COMPLAINT_RAISE_FOR_EMAIL_ID		 ("COMPLAINT_RAISE_FOR_EMAIL_ID", 6, 120, false, FieldFormatType.EMAIL, true),
	// Refund Utility
	TOTAL_TXNS							 ("TOTAL_TXNS", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_FILE_ENTRY					 ("TOTAL_FILE_ENTRY", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_VALID_ENTRY					 ("TOTAL_VALID_ENTRY", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_INVALID_ENTRY					 ("TOTAL_INVALID_ENTRY", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_CAPTURED						 ("TOTAL_CAPTURED", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_CLOSED						 ("TOTAL_CLOSED", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_CREATED						 ("TOTAL_CREATED", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_REJECTED						 ("TOTAL_REJECTED", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_DECLINED						 ("TOTAL_DECLINED", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_ERROR							 ("TOTAL_ERROR", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_EXCEPTION						 ("TOTAL_EXCEPTION", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_DENIED						 ("TOTAL_DENIED", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_FAILED						 ("TOTAL_FAILED", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_INVALID						 ("TOTAL_INVALID", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_AUTHENTICATION_FAILED			 ("TOTAL_AUTHENTICATION_FAILED", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_ACQUIRER_DOWN					 ("TOTAL_ACQUIRER_DOWN", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_FAILED_AT_ACQUIRER			 ("TOTAL_FAILED_AT_ACQUIRER", 13, 19, false, FieldFormatType.NUMBER, false),
	TOTAL_ACQUIRER_TIMEOUT				 ("TOTAL_ACQUIRER_TIMEOUT", 13, 19, false, FieldFormatType.NUMBER, false), 
	TOTAL_BALANCE						 ("TOTAL_BALANCE", 0, 10000000, false, FieldFormatType.NUMBER, false),
	AVAILABLE_BALANCE					 ("AVAILABLE_BALANCE", 0, 10000000, false, FieldFormatType.NUMBER, false),
	FIXED_CHARGES	                     ("FIXED_CHARGES", 0, 10000000, false, FieldFormatType.NUMBER, false),
	PERCENTAGE_CHARGES	                 ("PERCENTAGE_CHARGES", 0, 10000000, false, FieldFormatType.NUMBER, false),
	CHARGEBACK_TYPE    					 ("CHARGEBACK_TYPE", 1, 19, false, FieldFormatType.ALPHA, false),
	
	PROCESSING_FEE					     ("PROCESSING_FEE", 0, 100, false, FieldFormatType.NUMBER, false), 
	SUB_WALLET_ID 						 ("SUB_WALLET_ID", 1, 100, false, FieldFormatType.SPECIAL, false), 
	PAYOUT_TRANSFER_TYPE				 ("PAYOUT_TRANSFER_TYPE", 1, 50, false, FieldFormatType.ALPHASPACE, false),
	VIRTUAL_BENEFICIARY_NAME			 ("VIRTUAL_BENEFICIARY_NAME", 1, 500, false, FieldFormatType.SPECIAL, false), 
	COMMISSION_AMOUNT 					 ("COMMISSION_AMOUNT", 1, 500, false, FieldFormatType.NUMBER, false), 
	EMANDATE_URL						 ("EMANDATE_URL", 0, 1000, false, FieldFormatType.SPECIAL, false),
	DEBIT_START_DATE					 ("DEBIT_START_DATE", 0, 20, false, FieldFormatType.SPECIAL, false),
	DEBIT_END_DATE						 ("DEBIT_END_DATE", 0, 20, false, FieldFormatType.SPECIAL, false),
	CHECKOUT_JS_FLAG					 ("CHECKOUT_JS_FLAG",1,1,false, FieldFormatType.ALPHA, false),
	//crypto
	CRYPTO_AMOUNT						 ("CRYPTO_AMOUNT", 1, 14, false, FieldFormatType.ALPHANUM, false),
	CRYPTO_AMOUNT_IN_USD				 ("CRYPTO_AMOUNT_IN_USD", 1, 14, false, FieldFormatType.ALPHANUM, false),
	CRYPTO_AMOUNT_IN_LOCAL_CURRENCY		 ("CRYPTO_AMOUNT_IN_LOCAL_CURRENCY", 1, 14, false, FieldFormatType.ALPHANUM, false),
	CRYPTO_TXNTYPE						 ("CRYPTO_TXNTYPE", 0, 50, false, FieldFormatType.ALPHANUM, false),
	
	INVOICE_URL							 ("INVOICE_URL", 0, 500, false, FieldFormatType.SPECIAL, false),
	UPDATE_STATUS                        ("UPDATE_STATUS", 5, 10, false, FieldFormatType.ALPHA, false),
	AIRPAY_FINAL_REQUEST				 ("AIRPAY_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	AIRPAY_RESPONSE_FIELD				 ("AIRPAY_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.BOBSPECIAL, false),
	QAICASH_FINAL_REQUEST				 ("QAICASH_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	QAICASH_RESPONSE_FIELD				 ("QAICASH_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.BOBSPECIAL, false),
	FLOXYPAY_FINAL_REQUEST				 ("FLOXYPAY_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	FLOXYPAY_RESPONSE_FIELD				 ("FLOXYPAY_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.BOBSPECIAL, false),
	DIGITALSOLUTION_FINAL_REQUEST		 ("DIGITALSOLUTION_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	DIGITALSOLUTION_RESPONSE_FIELD		 ("DIGITALSOLUTION_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.BOBSPECIAL, false),
	PAYIN247_FINAL_REQUEST				 ("PAYIN247_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	PAYIN247_RESPONSE_FIELD				 ("PAYIN247_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.BOBSPECIAL, false),
	GREZPAY_FINAL_REQUEST				 ("GREZPAY_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	GREZPAY_RESPONSE_FIELD				 ("GREZPAY_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.BOBSPECIAL, false),
	UPIGATEWAY_FINAL_REQUEST			 ("UPIGATEWAY_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	UPIGATEWAY_RESPONSE_FIELD			 ("UPIGATEWAY_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.BOBSPECIAL, false),
	TOSHANIDIGITAL_FINAL_REQUEST		 ("TOSHANIDIGITAL_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	TOSHANIDIGITAL_RESPONSE_FIELD		 ("TOSHANIDIGITAL_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.BOBSPECIAL, false),
	GLOBALPAY_FINAL_REQUEST				 ("GLOBALPAY_FINAL_REQUEST", 1, 2000, false, FieldFormatType.SPECIAL, false),
	GLOBALPAY_RESPONSE_FIELD		 	 ("GLOBALPAY_RESPONSE_FIELD", 1, 2000, false, FieldFormatType.BOBSPECIAL, false),
	// Razorpay
	RAZORPAY_RESPONSE_FIELD				 ("RAZORPAY_RESPONSE_FIELD", 1, 10000, false, FieldFormatType.SPECIAL, false),
	RAZORPAY_FINAL_REQUEST		  		 ("RAZORPAY_FINAL_REQUEST", 1, 10000, false, FieldFormatType.SPECIAL, false),
	CUSTOMER_CATEGORY 					 ("CUSTOMER_CATEGORY", 4, 90, false, FieldFormatType.ALPHA, false);
	
	
	private final String name;
	private final int minLength;
	private final int maxLength;
	private final boolean required;
	private final FieldFormatType type;
	private final String responseMessage;
	private final boolean isSpecialCharReplacementAllowed;
	
	private FieldType(String name, int minLength, int maxLength, boolean required, String responseMessage, FieldFormatType type, boolean isSpecialCharReplacementAllowed){
		this.name = name;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.required = required;
		this.responseMessage = responseMessage;
		this.type = type;
		this.isSpecialCharReplacementAllowed = isSpecialCharReplacementAllowed;
	}
	
	private FieldType(String name, int minLength, int maxLength, boolean required, FieldFormatType type, boolean isSpecialCharReplacementAllowed){
		this.name = name;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.required = required;
		this.responseMessage = "Invalid " + name;
		this.type = type;
		this.isSpecialCharReplacementAllowed = isSpecialCharReplacementAllowed;
	}
	
	public static Map<String, FieldType> getFieldsMap(){
		Map<String, FieldType> fields = new HashMap<String, FieldType>();
		
		FieldType[] fieldTypes = FieldType.values();
		for(FieldType fieldType: fieldTypes){
			fields.put(fieldType.getName(), fieldType);
		}
		
		return fields;
	}
	
	public static Map<String, FieldType> getMandatoryRequestFields(){
		Map<String, FieldType> fields = new HashMap<String, FieldType>();
		
		FieldType[] fieldTypes = FieldType.values();
		for(FieldType fieldType: fieldTypes){
			if(fieldType.isRequired()){
				fields.put(fieldType.getName(), fieldType);	
			}			
		}
		
		return fields;
	}
	
	public static Map<String, FieldType> getMandatorSupportFields(){
		Map<String, FieldType> fields = new HashMap<String, FieldType>();
		
		fields.put(FieldType.ORIG_TXN_ID.getName(), FieldType.ORIG_TXN_ID);
		fields.put(FieldType.TXNTYPE.getName(), FieldType.TXNTYPE);
		fields.put(FieldType.AMOUNT.getName(), FieldType.AMOUNT);
		fields.put(FieldType.PAY_ID.getName(), FieldType.PAY_ID);
		fields.put(FieldType.HASH.getName(), FieldType.HASH);
		
		return fields;
	}
	
	public static Map<String, FieldType> getMandatoryStatusRequestFields(){
		Map<String, FieldType> fields = new HashMap<String, FieldType>();
		
		fields.put(FieldType.ORDER_ID.getName(), FieldType.ORDER_ID);
		fields.put(FieldType.TXNTYPE.getName(), FieldType.TXNTYPE);
		fields.put(FieldType.AMOUNT.getName(), FieldType.AMOUNT);
		fields.put(FieldType.PAY_ID.getName(), FieldType.PAY_ID);
		fields.put(FieldType.CURRENCY_CODE.getName(), FieldType.CURRENCY_CODE);
		fields.put(FieldType.HASH.getName(), FieldType.HASH);
		
		return fields;
	}
	
	public static Map<String, FieldType> getMandatoryRecoRequestFields(){
		Map<String, FieldType> fields = new HashMap<String, FieldType>();
		
		fields.put(FieldType.ORIG_TXN_ID.getName(), FieldType.ORIG_TXN_ID);
		fields.put(FieldType.ORDER_ID.getName(), FieldType.ORDER_ID);
		fields.put(FieldType.TXNTYPE.getName(), FieldType.TXNTYPE);
		fields.put(FieldType.AMOUNT.getName(), FieldType.AMOUNT);
		fields.put(FieldType.PAY_ID.getName(), FieldType.PAY_ID);
		fields.put(FieldType.HASH.getName(), FieldType.HASH);
		
		return fields;
	}
	
	public String getName() {
		return name;
	}
	public int getMinLength() {
		return minLength;
	}
	public int getMaxLength() {
		return maxLength;
	}
	public boolean isRequired() {
		return required;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public FieldFormatType getType() {
		return type;
	}

	public boolean isSpecialCharReplacementAllowed() {
		return isSpecialCharReplacementAllowed;
	}
}
