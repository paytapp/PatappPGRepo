package com.paymentgateway.commons.user;

public enum PayoutAcquirer {
	ICICI ("ICICI"),
	PAYTM ("Paytm"),
	CASHFREE ("Cashfree"),
	APEXPAY ("ApexPay"),
	FONEPAISA ("Fone Paisa"),
	FLOXYPAY ("FloxyPay"),
	QAICASH ("QAICASH"),
	TOSHANIDIGITAL ("Toshani Digital"),
	GLOBALPAY ("GLOBALPAY");
	

	private PayoutAcquirer(String name){
		this.name = name;
	}
	
	private String name;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	
	public static String getCodeFromName(String name){
		PayoutAcquirer[] payoutAcq = PayoutAcquirer.values();
		for(PayoutAcquirer payoutAcquirer : payoutAcq){
			if(String.valueOf(payoutAcquirer.getName()).equals(name)){
				return payoutAcquirer.name();
			}
		}
		return null;
	}
	
	public static PayoutAcquirer getInstanceFromCode(String code){
		PayoutAcquirer[] payoutAcq = PayoutAcquirer.values();
		for(PayoutAcquirer payoutAcquirer : payoutAcq){
			if(String.valueOf(payoutAcquirer.name()).equals(code)){
				return payoutAcquirer;
			}
		}
		return null;
	}

}
