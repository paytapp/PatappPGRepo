package com.paymentgateway.commons.util;

/**
 * @author Amitosh Aanand
 *
 */
public enum JobType {

	DAILY_CAPTURED_SMS						("DAILY_CAPTURED_SMS", "Daily Captured SMS", "capturedData"), 
	DAILY_MERCHANT_DATA_SMS					("DAILY_MERCHANT_DATA_SMS", "Daily Merchant SMS", "merchantData"), 
	DAILY_SETTLED_DATA_STATUS				("DAILY_SETTLED_DATA_STATUS", "Daily Settled SMS", "settledData"),
	DAILY_PG_CAPTURED_DATA_SMS				("DAILY_PG_CAPTURED_DATA_SMS", "Daily PG Captured SMS", "paymentGatewayCapturedData"), 
	CHARGEBACK_FINAL_STATUS					("CHARGEBACK_FINAL_STATUS", "Chargeback Final Status", "chargebackFinalStatus"),
	TRANSACTION_STATUS_ENQUIRY				("TRANSACTION_STATUS_ENQUIRY", "Transaction Status Enquiry", "transactionStatusEnquiry");

	private final String code;
	private final String name;
	private final String identity;

	private JobType(String code, String name, String identity) {
		this.name = name;
		this.code = code;
		this.identity = identity;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getIdentity() {
		return identity;
	}

}
