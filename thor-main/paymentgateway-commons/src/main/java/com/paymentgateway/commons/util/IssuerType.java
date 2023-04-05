package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rahul
 *
 */
public enum IssuerType {

	KOTAK("KOTAK", "KOTAK Bank"),
	SBI("SBI", "State Bank of India"),
	AXIS("AXIS", "AXIS Bank");

	private final String code;
	private final String name;

	private IssuerType(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static IssuerType getInstancefromCode(String issuerCode) {
		IssuerType issuerType = null;

		for (IssuerType issuer : IssuerType.values()) {

			if (issuerCode.equals(issuer.getCode().toString())) {
				issuerType = issuer;
				break;
			}
		}

		return issuerType;
	}

	public static String getIssuerName(String issuerCode) {
		String issuerType = null;
		if (null != issuerCode) {
			for (IssuerType issuer : IssuerType.values()) {
				if (issuerCode.equals(issuer.getCode().toString())) {
					issuerType = issuer.getName();
					break;
				}
			}
		}
		return issuerType;
	}

	public static String getIssuerCode(String issuerName) {
		String issuerType = null;
		if (null != issuerName) {
			for (IssuerType issuer : IssuerType.values()) {
				if (issuerName.equals(issuer.name().toString())) {
					issuerType = issuer.getCode();
					break;
				}
			}
		}
		return issuerType;
	}

	public static IssuerType getInstancefromName(String issuerName) {
		IssuerType issuerType = null;

		for (IssuerType issuer : IssuerType.values()) {

			if (issuerName.equalsIgnoreCase(issuer.getName())) {
				issuerType = issuer;
				break;
			}
		}

		return issuerType;
	}
	
	public static String[] getAllIssuerName() {
		
		List<String> issuerList = new ArrayList<String>();
		for (IssuerType issuer : IssuerType.values()) {			
			issuerList.add(issuer.getName().toString());
		}
		
		String[] issuerArr = issuerList.toString().replace("[", "").replace("]", "").replaceAll(" ", "").split(",");
		return issuerArr;
	}

}
