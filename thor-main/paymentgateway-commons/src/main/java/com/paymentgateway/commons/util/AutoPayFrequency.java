package com.paymentgateway.commons.util;

public enum AutoPayFrequency {
	
	ONE_TIME("One Time", "OT"),
	DAILY("Daily", "DL"),
	WEEKLY("Weekly", "WK"),
	BI_MONTHLY("Bi-Monthly", "BM"),
	MONTHLY("Monthly", "MT"),
	QUARTERLY("Quarterly", "QT"), 
	SEMI_ANNUALLY("half yearly", "HY"), 
	YEARLY("Yearly", "YR"),
	AS_AND_WHEN_PRESENTED("As And When Presented", "AS");
	
	private final String name;
	private final String code;
	
	private AutoPayFrequency(String name, String code) {
		this.name = name;
		this.code = code;
	}

	public static String getAutoPayFrequencyName(String frequencyCode) {
		String frequencyName = null;
		if (null != frequencyCode) {
			for (AutoPayFrequency frequency : AutoPayFrequency.values()) {
				if (frequencyCode.equals(frequency.getCode().toString())) {
					frequencyName = frequency.getName();
					break;
				}
			}
		}
		return frequencyName;
	}
	
	public static String getAutoPayFrequencyCode(String frequencyName) {
		String frequencyCode = null;
		if (null != frequencyName) {
			for (AutoPayFrequency frequency : AutoPayFrequency.values()) {
				if (frequencyName.equals(frequency.getName())) {
					frequencyCode = frequency.getCode();
					break;
				}
			}
		}
		return frequencyCode;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCode() {
		return code;
	}
}
