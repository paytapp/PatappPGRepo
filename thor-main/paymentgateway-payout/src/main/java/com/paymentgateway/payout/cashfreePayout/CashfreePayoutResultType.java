package com.paymentgateway.payout.cashfreePayout;

public enum CashfreePayoutResultType {
	
	CASH001			("200" , "000" , "Captured" , "SUCCESS"),
	CASH002			("202" , "032" , "PENDING" , "Transfer request pending at the bank"),
	CASH003			("403", "004", "Declined", "Token is not valid"),
	CASH004			("412", "004", "Declined", "Token missing in the request"),
	CASH005			("422", "004", "Declined", "Invalid amount passed"),
	CASH006			("409", "004", "Declined", "Transfer Id already exists"),
	CASH007			("404", "004", "Declined", "Beneficiary does not exist"),
	CASH008			("422", "004", "Declined", "Beneficiary details not valid"),
	CASH009			("412", "004", "Declined", "Not enough available balance in the account"),
	CASH010			("400", "004", "Declined", "Transfer attempt failed at the bank"),
	CASH011			("520", "004", "Declined", "Transfer request triggered.No response from bank");
	
	private CashfreePayoutResultType(String bankCode, String paymentGatewayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.paymentGateway = paymentGatewayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static CashfreePayoutResultType getInstanceFromName(String code) {
		CashfreePayoutResultType[] statusTypes = CashfreePayoutResultType.values();
		for (CashfreePayoutResultType statusType : statusTypes) {
			if (String.valueOf(statusType.getBankCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}

	private final String bankCode;
	private final String paymentGateway;
	private final String statusCode;
	private final String message;

	public String getBankCode() {
		return bankCode;
	}
	
	public String getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}

	public String getPaymentGateway() {
		return paymentGateway;
	}

	


}
