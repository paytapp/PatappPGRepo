package com.paymentgateway.commons.util;

public enum StateCodes {
	
	JAMMU_AND_KASHMIR  							("JAMMU AND KASHMIR", "01"),
	HIMACHAL_PRADESH							("HIMACHAL PRADESH", "02"),
	PUNJAB										("PUNJAB", "03"),
	CHANDIGARH									("CHANDIGARH", "04"),
	UTTARAKHAND									("UTTARAKHAND", "05"),
	HARYANA										("HARYANA", "06"),
	DELHI										("DELHI", "07"),
	RAJASTHAN									("RAJASTHAN", "08"),
	UTTAR_PRADESH								("UTTAR PRADESH", "09"),
	BIHAR										("BIHAR","10"),
	SIKKIM										("SIKKIM", "11"),
	ARUNACHAL_PRADESH							("ARUNACHAL PRADESH", "12"),
	NAGALAND									("NAGALAND", "13"),
	MANIPUR										("MANIPUR", "14"),
	MIZORAM										("MIZORAM", "15"),
	TRIPURA										("TRIPURA", "16"),
	MEGHLAYA									("MEGHLAYA", "17"),
	ASSAM										("ASSAM", "18"),
	WEST_BENGAL									("WEST BENGAL", "19"),
	JHARKHAND									("JHARKHAND", "20"),
	ODISHA										("ODISHA", "21"),
	CHHATTISGARH								("CHHATTISGARH", "22"),
	MADHYA_PRADESH								("MADHYA PRADESH", "23"),
	GUJARAT										("GUJARAT", "24"),
	DAMAN_AND_DIU								("DAMAN AND DIU", "26"),
	MAHARASHTRA									("MAHARASHTRA", "27"),
	KARNATAKA									("KARNATAKA", "29"),
	GOA											("GOA", "30"),
	LAKSHWADEEP									("LAKSHWADEEP", "31"),
	KERALA										("KERALA", "32"),
	TAMIL_NADU									("TAMIL NADU", "33"),
	PUDUCHERRY									("PUDUCHERRY", "34"),
	ANDAMAN_AND_NICOBAR_ISLANDS					("ANDAMAN AND NICOBAR ISLANDS", "35"),
	TELANGANA									("TELANGANA", "36"),
	ANDHRA_PRADESH 								("ANDHRA PRADESH", "37"),
	LADAKH										("LADAKH", "38");
	
	
	private final String name;
	private final String code;

	private StateCodes(String name, String code) {
		this.name = name;
		this.code = code;

	}
	
	public static String getCodeUsingInstance(String state) {
		String statecode = null;
		if (null != state) {
			for (StateCodes states : StateCodes.values()) {
				if (state.equalsIgnoreCase(states.getName())) {
					statecode = states.getCode();
					break;
				}
			}
		}
		return statecode;
	}
	
	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}
}
