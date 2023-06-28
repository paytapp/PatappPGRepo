package com.paymentgateway.pg.router;

public class RoutingRuleAction {//extends AbstractSecureAction { TODO......decide changes
	
	private String acquirerName;

	public String getAcquirerName() {
		return acquirerName;
	}

	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}
	
	
	public String execute(){
		
		System.out.println("=========insdie action================"+getAcquirerName());
		
		return null;
	}
	
	
	
	
	
	
}
