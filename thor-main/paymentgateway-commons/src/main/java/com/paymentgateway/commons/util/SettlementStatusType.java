package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.List;

public enum SettlementStatusType {
	
	
	SUCCESS				(0, "Success",  false),
	FAILED				(1, "Failed",   false),
	SENT_TO_BANK		(2, "Sent to bank",   false),
	SENT_TO_BENEFICIARY	(3, "Sent to beneficiary",     false),
	IN_PROCESS			(4, "In process",       false),
	RETURED_TO_BENEFICIARY	(5, "Retured to beneficiary", false);	

	private final int code;
	private final String name;
	private final boolean isInternal;
	
	private SettlementStatusType(int code, String name, boolean isInternal){
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
		for(StatusType statusType:StatusType.values()){
			if(!statusType.isInternal())
			statusTypes.add(statusType);
		}
	  return statusTypes;
	}
	
	public static StatusType getInstanceFromName(String code){
		StatusType[] statusTypes = StatusType.values();
		for(StatusType statusType : statusTypes){
			if(String.valueOf(statusType.getName()).toUpperCase().equals(code)){
				return statusType;
			}
		}		
		return null;
	}

}
