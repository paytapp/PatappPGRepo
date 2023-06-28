package com.paymentgateway.kotak;


public enum KotakRupayResultType {
	
	KOTAKRUPAY001		("412","022","Failed at Acquirer","IAS Error"),
	KOTAKRUPAY002		("5000","004","Declined","NO FUNDS REMAIN"),
	KOTAKRUPAY003		("5001","022","Failed at Acquirer","INVALID REFUND REASON CODE"),
	KOTAKRUPAY004		("5002","004","Declined","AMT EXCEEDS AVAIL FUNDS"),
	KOTAKRUPAY005		("5003","004","Declined","TRAN BUS DAY EXCEEDED"),
	KOTAKRUPAY006		("ACCU000","007","Rejected","PIN was successfully received"),		
	KOTAKRUPAY007		("ACCU200","010","Cancelled","Transaction was cancelled by the user"),
	KOTAKRUPAY008		("ACCU400","010","Cancelled","Transaction was cancelled by the user"),
	KOTAKRUPAY009		("ACCU600","022","Failed at Acquirer","Invalid data was posted to the PaySecure PIN Pad"),
	KOTAKRUPAY010		("ACCU800","022","Failed at Acquirer","Generic PaySecure error"),
	KOTAKRUPAY011		("ACCU999","007","Rejected","PIN Pad was successfully opened"),
	KOTAKRUPAY012		("AQ","007","Rejected","PIN Acquired"),
	KOTAKRUPAY013		("AU","007","Rejected","Authenticated"),
	KOTAKRUPAY014		("AZ","007","Rejected","Authorized"),
	KOTAKRUPAY015		("C","007","Rejected","Completed"),
	KOTAKRUPAY016		("DC","004","Declined","Declined"),
	KOTAKRUPAY017		("FD","002","Denied by risk","Fraud Decline"),
	KOTAKRUPAY018		("I","007","Rejected","Initiated"),
	KOTAKRUPAY019		("MR","007","Rejected","Merchant Refund"),
	KOTAKRUPAY020		("P","007","Rejected","Pending"),
	KOTAKRUPAY021		("PA","007","Rejected","PreAuthorized"),
	KOTAKRUPAY022		("PE","007","Rejected","Prior to EFT"),
	KOTAKRUPAY023		("RF","007","Rejected","Refunded"),
	KOTAKRUPAY024		("RV","007","Rejected","Reversed"),
	KOTAKRUPAY025		("V","007","Rejected","Voided"),
	KOTAKRUPAY026		("NA","010","Cancelled","User had closed the browser or Drop of Internet Connection"),
	KOTAKRUPAY027		("RNF","004","Declined","Request Not Found"),
	KOTAKRUPAY028		("90","007","Rejected","Cut-off is in process"),
	KOTAKRUPAY029		("95","007","Rejected","Reconcile error"),
	KOTAKRUPAY030		("E3","022","Failed at Acquirer","ARQC validation failed by Issuer"),
	KOTAKRUPAY031		("E4","022","Failed at Acquirer","TVR validation failed by Issuer"),
	KOTAKRUPAY032		("E5","022","Failed at Acquirer","CVR validation failed by Issuer"),
	KOTAKRUPAY033		("CI","022","Failed at Acquirer","Compliance error code for issuer"),
	KOTAKRUPAY034		("CA","022","Failed at Acquirer","Compliance error code for acquirer"),
	KOTAKRUPAY035		("M6","022","Failed at Acquirer","Compliance error code for LMM"),
	KOTAKRUPAY036		("ED","022","Failed at Acquirer","E-Commerce decline"),
	KOTAKRUPAY037		("STO","003","Timed out at Acquirer","Session Expired or Timed Out"),
	KOTAKRUPAY038		("VER","022","Failed at Acquirer","Validation Failed"),
	KOTAKRUPAY039		("HNM","004","Declined","Hash Not Matched"),
	KOTAKRUPAY040		("IER","022","Failed at Acquirer","Transaction could not Process by Acquiring System"),
	KOTAKRUPAY041		("CAN","010","Cancelled","User pressed cancel button on payment page"),
	KOTAKRUPAY042		("94","022","Failed at Acquirer","Duplicate transmission"),
	KOTAKRUPAY043		("00","000","Captured","Approved or completed Successfully"),
	KOTAKRUPAY044		("02","022","Failed at Acquirer","Invalid Command"),
	KOTAKRUPAY045		("03","022","Failed at Acquirer","Invalid merchant"),
	KOTAKRUPAY046		("04","022","Failed at Acquirer","Pick-up Card"),
	KOTAKRUPAY047		("05","004","Declined","Do not honour"),
	KOTAKRUPAY048		("06","004","Declined","Error"),
	KOTAKRUPAY049		("12","021","Invalid","Invalid transaction"),
	KOTAKRUPAY050		("13","004","Declined","Invalid amount"),
	KOTAKRUPAY051		("14","004","Declined","Invalid card number"),
	KOTAKRUPAY052		("15","004","Declined","No such issuer"),
	KOTAKRUPAY053		("17","010","Cancelled","Customer cancellation"),
	KOTAKRUPAY054		("21","022","Failed at Acquirer","No action taken"),
	KOTAKRUPAY055		("22","022","Failed at Acquirer","Suspected malfunction"),
	KOTAKRUPAY056		("30","007","Rejected","Switch ISO Format Error"),
	KOTAKRUPAY057		("31","007","Rejected","Bank not supported by switch"),
	KOTAKRUPAY058		("33","007","Rejected","Expired card, capture"),
	KOTAKRUPAY059		("34","007","Rejected","Suspected fraud, capture"),
	KOTAKRUPAY060		("36","007","Rejected","restricted card, capture"),
	KOTAKRUPAY061		("38","007","Rejected","Allowable PIN tries exceeded, capture"),
	KOTAKRUPAY062		("39","007","Rejected","No credit account"),
	KOTAKRUPAY063		("40","007","Rejected","Requested function not supported"),
	KOTAKRUPAY064		("41","007","Rejected","Lost card, capture"),
	KOTAKRUPAY065		("42","007","Rejected","No universal accoount"),
	KOTAKRUPAY066		("43","004","Declined","DECLINED (stolen)"),
	KOTAKRUPAY067		("51","004","Declined","Not sufficient funds"),
	KOTAKRUPAY068		("52","007","Rejected","No checking account"),
	KOTAKRUPAY069		("53","007","Rejected","No savings account"),
	KOTAKRUPAY070		("54","007","Rejected","Expired card"),
	KOTAKRUPAY071		("55","007","Rejected","incorrect personal identification number"),
	KOTAKRUPAY072		("56","007","Rejected","No card record (Make sure card number is correct)"),
	KOTAKRUPAY073		("57","007","Rejected","Transaction not permitted to Cardholder"),
	KOTAKRUPAY074		("58","007","Rejected","Transaction not permitted to terminal"),
	KOTAKRUPAY075		("59","012","Denied due to fraud","Suspected fraud"),
	KOTAKRUPAY076		("60","004","Declined","DECLINED (contact acquirer)"),
	KOTAKRUPAY077		("61","004","Declined","DECLINED (exceeds with)"),
	KOTAKRUPAY078		("62","004","Declined","DECLINED (restricted card)"),
	KOTAKRUPAY079		("63","002","Denied by risk","Security violation"),
	KOTAKRUPAY080		("65","004","Declined","DECLINED (exceeds frequency)"),
	KOTAKRUPAY081		("66","007","Rejected","Card acceptor calls acquirer's"),
	KOTAKRUPAY082		("67","007","Rejected","Hard capture (requires that card picked up at ATM)"),
	KOTAKRUPAY083		("68","003","Timed out at Acquirer","Acquirer time-out"),
	KOTAKRUPAY084		("74","002","Denied by risk","Transactions declined by issuer based on Risk Score"),
	KOTAKRUPAY085		("75","004","Declined","Allowable number of PIN tries exceeded, decline"),
	KOTAKRUPAY086		("81","002","Denied by risk","Cryptographic Error"),
	KOTAKRUPAY087		("01","002","Denied by risk","Missing Parameter"),
	KOTAKRUPAY088		("91","002","Denied by risk","Issuer Unavailable"),
	KOTAKRUPAY089		("92","022","Failed at Acquirer","NO ROUTING AVAILABLE"),
	KOTAKRUPAY090		("93","002","Denied by risk","VIOLATION"),
	KOTAKRUPAY091		("96","002","Denied by risk","SYSTEM ERROR"),
	KOTAKRUPAY092		("110","004","Declined","Issuer does not have this card number on record"),
	KOTAKRUPAY093		("120","002","Denied by risk","ACCT CLOSED"),
	KOTAKRUPAY094		("130","002","Denied by risk","FRAUD"),
	KOTAKRUPAY095		("200","002","Denied by risk","TRANSACTION DECLINED"),
	KOTAKRUPAY096		("303","007","Rejected","DCE INVALID DATA"),
	KOTAKRUPAY097		("403","002","Denied by risk","UNKNOWN COMMAND"),
	KOTAKRUPAY098		("400","002","Denied by risk","GENERAL"),
	KOTAKRUPAY099		("401","007","Rejected","COMMAND IS NULL OR EMPTY"),
	KOTAKRUPAY100		("402","002","Denied by risk","UNKNOWN COMMAND"),
	KOTAKRUPAY101		("399","002","Denied by risk","SYSTEM UNAVAILABLE"),
	KOTAKRUPAY102		("402","007","Rejected","XML IS NULL OR EMPTY"),
	KOTAKRUPAY103		("404","002","Denied by risk","SQL EXCEPTION"),
	KOTAKRUPAY104		("405","004","Declined","BAD CREDENTIALS"),
	KOTAKRUPAY105		("406","002","Denied by risk","NOT AUTHENTICATED"),
	KOTAKRUPAY106		("407","004","Declined","NOT AUTHORIZED ErrMsg details the problem"),
	KOTAKRUPAY107		("408","007","Rejected","XML DATA ERROR"),
	KOTAKRUPAY108		("409","007","Rejected","SHOPPER SERVICE ERROR"),
	KOTAKRUPAY109		("410","021","Invalid","INVALID BIN or ERROR WITH BIN CHECK"),
	KOTAKRUPAY110		("411","002","Denied by risk","INELIGIBLE PAN");
	

	

	private KotakRupayResultType(String bankCode, String paymentGatewayCode, String statusName, String message) {
		this.bankCode = bankCode;
		this.paymentGatewayCode = paymentGatewayCode;
		this.statusName = statusName;
		this.message = message;
	}

	public static KotakRupayResultType getInstanceFromName(String code) {
		KotakRupayResultType[] statusTypes = KotakRupayResultType.values();
		for (KotakRupayResultType statusType : statusTypes) {
			if (String.valueOf(statusType.getBankCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}

	private final String bankCode;
	private final String paymentGatewayCode;
	private final String message;
	private final String statusName;
	public String getBankCode() {
		return bankCode;
	}

	public String getPaymentGatewayCode() {
		return paymentGatewayCode;
	}


	public String getMessage() {
		return message;
	}

	public String getStatusName() {
		return statusName;
	}
}
