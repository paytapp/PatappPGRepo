package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.List;

public enum OrderIdType {

	NEVER								(0, "NEVER", false), 
	ALLOW_DUPLICATE_INDIVIDUALLY		(1, "ALLOW_DUPLICATE_INDIVIDUALLY", false), 
	ALLOW_DUPLICATE_ALWAYS				(2,	"ALLOW_DUPLICATE_ALWAYS", false);

	private final int code;
	private final String name;
	private final boolean isInternal;

	private OrderIdType(int code, String name, boolean isInternal) {
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

	public static List<OrderIdType> getOrderIdType() {
		List<OrderIdType> orderIdTypes = new ArrayList<OrderIdType>();
		for (OrderIdType orderIdType : OrderIdType.values()) {
			if (!orderIdType.isInternal())
				orderIdTypes.add(orderIdType);
		}
		return orderIdTypes;
	}

	public static OrderIdType getInstanceFromCode(String code) {
		OrderIdType[] orderIdTypes = OrderIdType.values();
		for (OrderIdType orderIdType : orderIdTypes) {
			if (String.valueOf(orderIdType.getName()).toUpperCase().equals(code)) {
				return orderIdType;
			}
		}
		return null;
	}
}
