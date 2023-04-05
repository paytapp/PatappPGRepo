package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public enum StatusType {

	APPROVED						(0, "Approved", false), 
	DECLINED						(1, "Declined", false), 
	REJECTED						(2, "Rejected", false),
	PENDING							(3, "Pending", false), 
	CAPTURED						(4, "Captured", false), 
	ERROR							(5, "Error", false),
	TIMEOUT							(6, "Timeout", false), 
	SETTLED							(7, "Settled", false), 
	BROWSER_CLOSED					(8, "Browser Closed", false),
	CANCELLED						(9, "Cancelled", false), 
	DENIED							(10, "Denied by risk", false), 
	ENROLLED						(11, "Enrolled", false),
	DUPLICATE						(12, "Duplicate", false), 
	FAILED							(13, "Failed", false), 
	INVALID							(14, "Invalid", false),
	AUTHENTICATION_FAILED			(15, "Authentication Failed", false), 
	SENT_TO_BANK					(16, "Sent to Bank", false),
	DENIED_BY_FRAUD					(17, "Denied due to fraud", false), 
	RECONCILED						(18, "Reconciled", false),
	ACQUIRER_DOWN					(19, "Acquirer down", false), 
	PROCESSING						(20, "Processing", false),
	FAILED_AT_ACQUIRER				(21, "Failed at Acquirer", false), 
	ACQUIRER_TIMEOUT				(22, "Timed out at Acquirer", false),
	NODAL_SETTLED					(23, "Settled in Nodal", false), 
	NODAL_PAYOUT					(24, "Payout from Nodal", false),
	REJECTED_BY_PG					(25, "Rejected by PG", false),
	PROCESSED					    (26, "PROCESSED", false),
	VERIFIED					    (27, "Verified", false),
	NOTIFIED					    (28, "Notified", false),
	PENDING_AT_ACQUIRER				(29, "Pending at Acquirer", false),
	INITIATED						(30, "Initiated", false);

	private final int code;
	private final String name;
	private final boolean isInternal;

	private StatusType(int code, String name, boolean isInternal) {
		this.code = code;
		this.name = name;
		this.isInternal = isInternal;
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public boolean isInternal() {
		return isInternal;
	}

	public static List<StatusType> getStatusType() {
		List<StatusType> statusTypes = new ArrayList<StatusType>();
		for (StatusType statusType : StatusType.values()) {
			if (!statusType.isInternal())
				statusTypes.add(statusType);
		}
		return statusTypes;
	}

	public static StatusType getInstanceFromName(String name) {
		StatusType[] statusTypes = StatusType.values();
		for (StatusType statusType : statusTypes) {
			if (String.valueOf(statusType.getName()).toUpperCase().equals(name)) {
				return statusType;
			}
		}
		return null;
	}

	public static String toBasicStatus(String statusType) {
		if (StringUtils.isNotBlank(statusType)) {
			StatusType status = StatusType.getInstanceFromName(statusType.toUpperCase());
			switch (status) {
			case CAPTURED:
				return StatusType.CAPTURED.getName();
			case SETTLED:
				return StatusType.CAPTURED.getName();
			case RECONCILED:
				return StatusType.CAPTURED.getName();
			case CANCELLED:
				return StatusType.CANCELLED.getName();
			case PENDING:
				return StatusType.PENDING.getName();
			default:
				return StatusType.FAILED.getName();
			}
		} else {
			return StatusType.FAILED.getName();
		}
	}
}
