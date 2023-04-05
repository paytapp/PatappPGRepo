package com.paymentgateway.commons.util;

public enum NotificationStatusType {
	
	READ				(0, "read"),
	UNREAD				(1, "unread");
	
	private final String name;
	private final int code;
	
	private NotificationStatusType(int code, String name){
		this.code = code;
		this.name = name;
	}
	public int getCode() {
		return code;
	}
	public String getName() {
		return name;
	}

	
}
