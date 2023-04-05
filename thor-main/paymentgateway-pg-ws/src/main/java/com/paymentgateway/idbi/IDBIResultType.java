package com.paymentgateway.idbi;


public enum IDBIResultType {

	IDBI001			("01" , "004" , "Declined" , "Declined by Card Issuer for unspecified reason."),
	IDBI002			("02" , "004" , "Declined" , "Declined by Card Issuer for unspecified reason."),
	IDBI003			("03" , "004" , "Declined" , "Merchant record not found or contains incorrect parameters."),
	IDBI004			("04" , "004" , "Declined" , "Some technical issue with the card."),
	IDBI005			("05" , "004" , "Declined" , "Declined by Card Issuer for Security reason"),
	IDBI006			("06" , "022" , "Failed at Acquirer" , "Some technical issue at acquirer end."),
	IDBI007			("07" , "002" , "Denied by risk" , "Stolen or lost card"),
	IDBI008			("33" , "004" , "Declined" , "Expired Card"),
	IDBI009			("36" , "004" , "Declined" , "Restricted Card"),
	IDBI010			("41" , "002" , "Denied by risk" , "Lost card"),
	IDBI011			("43" , "002" , "Denied by risk" , "Stolen card"),
	IDBI012			("51" , "004" , "Declined" , "The account has insufficient funds to complete the transaction."),
	IDBI013			("54" , "004" , "Declined" , "Expired Card"),
	IDBI014			("56" , "004" , "Declined" , "Declined - Card issuer has no record of the card used."),
	IDBI015			("57" , "004" , "Declined" , "Transaction not permitted to card holder."),
	IDBI016			("59" , "002" , "Denied by risk" , "Suspected Fraud"),
	IDBI017			("60" , "002" , "Denied by risk" , "Card has been blocked."),
	IDBI018			("61" , "004" , "Declined" , "Exceeds purchase/sale amount limit"),
	IDBI019			("62" , "004" , "Declined" , "Restricted Card - Not allowed for international transaction."),
	IDBI020			("63" , "002" , "Denied by risk" , "Security violation"),
	IDBI021			("65" , "004" , "Declined" , "Declined - Daily transaction count exceeded for the card."),
	IDBI022			("80" , "022" , "Failed at Acquirer" , "Visa transactions: credit issuer unavailable."),
	IDBI023			("91" , "022" , "Failed at Acquirer" , "Issuer unavailable or Switch not operative."),
	IDBI024			("92" , "022" , "Failed at Acquirer" , "No routing available."),
	IDBI025			("93" , "022" , "Failed at Acquirer" , "Transaction could be completed. Compliance violation."),
	IDBI026			("96" , "022" , "Failed at Acquirer" , "Declined - A system error has occurred"),
	IDBI027			("N7" , "004" , "Declined" , "CVV2 failure."),
	IDBI028			("F" , "010" , "Cancelled" , "Transaction cancelled by user");
				

	private IDBIResultType(String bankCode, String paymentGatewayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.paymentGatewayCode = paymentGatewayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static IDBIResultType getInstanceFromName(String code) {
		IDBIResultType[] statusTypes = IDBIResultType.values();
		for (IDBIResultType statusType : statusTypes) {
			if (String.valueOf(statusType.getBankCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}

	private final String bankCode;
	private final String paymentGatewayCode;
	private final String statusCode;
	private final String message;

	public String getBankCode() {
		return bankCode;
	}

	public String getPaymentGatewayCode() {
		return paymentGatewayCode;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}
}