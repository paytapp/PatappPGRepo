package com.paymentgateway.razorpay;

import java.util.HashMap;
import java.util.Map;

enum RazorpayResultType {

	RAZOR01("BAD_REQUEST_ERROR", "010","Cancelled", "Payment processing cancelled by user.");

	private RazorpayResultType(String bankCode, String iPayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.iPayCode = iPayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	private static final Map<String, RazorpayResultType> razorpayResultTypes = new HashMap<>();
	static {
		for (RazorpayResultType statusType : RazorpayResultType.values()) {
			razorpayResultTypes.put(statusType.getBankCode().toUpperCase(), statusType);
		}
	}

	public static RazorpayResultType getInstanceFromCode(String code) {
		return razorpayResultTypes.get(code.toUpperCase());
	}

	private final String bankCode;
	private final String iPayCode;
	private final String statusCode;
	private final String message;

	public String getBankCode() {
		return bankCode;
	}

	public String getiPayCode() {
		return iPayCode;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}
}