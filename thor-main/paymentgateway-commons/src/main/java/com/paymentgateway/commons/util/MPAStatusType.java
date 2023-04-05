package com.paymentgateway.commons.util;

/**
 * @author Amitosh Aanand
 *
 */
public enum MPAStatusType {

	PENDING				("PENDING", "Pending"), 
	REVIEW_REJECTED		("REVIEW_REJECTED", "Review Rejected"),
	REVIEW_APPROVED		("REVIEW_APPROVED", "Review Approved"), 
	APPROVED			("APPROVED", "Approved"),
	REJECTED			("REJECTED", "Rejected");

	private final String statusCode;
	private final String statusName;

	private MPAStatusType(String statusCode, String statusName) {
		this.statusCode = statusCode;
		this.statusName = statusName;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public String getStatusName() {
		return statusName;
	}
}
