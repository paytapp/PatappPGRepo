package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.List;

public enum PayoutPupose {
	//ICICI
	VENDOR_PAYOUTS		             ("Vendor Payouts","Vendor Payouts", "REIMBURSEMENT"),
	CASHBACKS                        ("Cashbacks","Cashbacks","BONUS"),
	REFUNDS                          ("Refunds","Refunds","REIMBURSEMENT"),
	MARKETING_CAMPAIGN               ("Marketing Campaign","Marketing Campaign","INCENTIVE"),
	LOYALTY_POINTS_REDEMPTION        ("Loyalty Points Redemption","Loyalty Points Redemption","OTHERS"),
	OTHERS							 ("Others","Others","OTHERS");
/*	//Paytm
	SALARY_DISBURSEMENT				("SALARY_DISBURSEMENT","Salary Disbursement","PAYTM"),
	REIMBURSEMENT					("REIMBURSEMENT","Reimbursement","PAYTM"),
	BONUS							("BONUS","Bonus","PAYTM"),
	INCENTIVE						("INCENTIVE","Incentive","PAYTM"),
	OTHERS							("OTHERS","Others","PAYTM");*/
	
	
	private String name;
	private String iciciCode;
	private String paytmCode;
	


	public String getIciciCode() {
		return iciciCode;
	}

	public String getPaytmCode() {
		return paytmCode;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIciciCode(String iciciCode) {
		this.iciciCode = iciciCode;
	}

	public void setPaytmCode(String paytmCode) {
		this.paytmCode = paytmCode;
	}

	public String getName() {
		return name;
	}
	
	

	private PayoutPupose(String name, String iciciCode, String paytmCode) {
		this.name = name;
		this.iciciCode = iciciCode;
		this.paytmCode = paytmCode;
	}
	
	public static PayoutPupose getInstanceByCode(String name) {
		for (PayoutPupose purposeType : PayoutPupose.values()) {
			if (purposeType.getName().equals(name)) {
				return purposeType;
			}
		}
		return null;
	}
	
	public static PayoutPupose getInstance(String name) {
		PayoutPupose[] payoutPurposeType = PayoutPupose.values();
		for (PayoutPupose purposeType : payoutPurposeType) {
			if (purposeType.getName().equals(name)) {
				return purposeType;
			}
		}
		return null;
	}
	
	public static PayoutPupose getInstanceByName(String name) {
		
		for (PayoutPupose purposeType : PayoutPupose.values()) {
			if (purposeType.getName().equals(name)) {
				return purposeType;
			}
		}
		return null;
	}
	
	public static List<PayoutPupose> getStatusType() {
		List<PayoutPupose> payoutPuposeTypes = new ArrayList<PayoutPupose>();
		for (PayoutPupose payoutType : PayoutPupose.values()) {
			payoutPuposeTypes.add(payoutType);
		}
		return payoutPuposeTypes;
	}

	
	

}
