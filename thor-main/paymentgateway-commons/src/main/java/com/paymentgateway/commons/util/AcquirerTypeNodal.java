package com.paymentgateway.commons.util;

/**
 * @author Shaiwal
 *
 */
public enum AcquirerTypeNodal {

	// Credit Card
	//BOB("BOB", "Bank of Baroda"), 
	YESBANKCB("YESBANKCB", "YES BANK CB");

	private final String code;
	private final String name;

	private AcquirerTypeNodal(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static AcquirerTypeNodal getInstancefromCode(String acquirerCode) {
		AcquirerTypeNodal acquirerType = null;

		for (AcquirerTypeNodal acquirer : AcquirerTypeNodal.values()) {

			if (acquirerCode.equals(acquirer.getCode().toString())) {
				acquirerType = acquirer;
				break;
			}
		}

		return acquirerType;
	}

	public static String getAcquirerName(String acquirerCode) {
		String acquirertype = null;
		if (null != acquirerCode) {
			for (AcquirerTypeNodal acquirer : AcquirerTypeNodal.values()) {
				if (acquirerCode.equals(acquirer.getCode().toString())) {
					acquirertype = acquirer.getName();
					break;
				}
			}
		}
		return acquirertype;
	}

	public static AcquirerTypeNodal getInstancefromName(String acquirerName) {
		AcquirerTypeNodal acquirerType = null;

		for (AcquirerTypeNodal acquirer : AcquirerTypeNodal.values()) {

			if (acquirerName.equals(acquirer.getName())) {
				acquirerType = acquirer;
				break;
			}
		}

		return acquirerType;
	}

}
