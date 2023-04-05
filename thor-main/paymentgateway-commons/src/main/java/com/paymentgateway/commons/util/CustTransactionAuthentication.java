package com.paymentgateway.commons.util;

public enum CustTransactionAuthentication {
	
	SUCCESS		(101,"Success"),              
	DECLINE		(102,"Decline"),        
	PENDING	    (103,"Pending");  

	private final int authenticationCode;
	private final String authenticationName;

	private CustTransactionAuthentication(int authenticationCode,String authenticationName){
		this.authenticationCode = authenticationCode;
		this.authenticationName = authenticationName;
	}

	public int getAuthenticationCode() {
		return authenticationCode;
	}

	public String getAuthenticationName() {
		return authenticationName;
	}
 
}
