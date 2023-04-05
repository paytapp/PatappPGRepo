package com.paymentgateway.commons.util;

public enum Frequency {
	
	DAILY("Daily", "DAIL"), 
	WEEKLY("Weekly", "WEEK"), 
	MONTHLY("Monthly", "MNTH"), 
	QUATERLY("Quaterly", "QURT"), 
	SEMI_ANNUALLY("Semi Annually", "MIAN"), 
	YEARLY("Year", "YEAR"),
	BI_MONTHLY("Bi-Monthly", "BIMN"), 
	AS_AND_WHEN_PRESENTED("As And When Presented", "ADHO");

	
	private final String name;
	private final String code;
	
	private Frequency(String name, String code) {
		this.name = name;
		this.code = code;
	}

	public static String getFrequencyName(String frequencyCode) {
		String frequencyName = null;
		if (null != frequencyCode) {
			for (Frequency frequency : Frequency.values()) {
				if (frequencyCode.equals(frequency.getCode().toString())) {
					frequencyName = frequency.getName();
					break;
				}
			}
		}
		return frequencyName;
	}
	
	public static String getFrequencyCode(String frequencyName) {
		String frequencyCode = null;
		if (null != frequencyName) {
			for (Frequency frequency : Frequency.values()) {
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
