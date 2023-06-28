package com.paymentgateway.kotak;


public enum KotakVisaMasterResultType {

	KOTAKVISAMASTER001			("DOI" , "022" , "Failed at Acquirer" , "Duplicate Order ID Identify"),
	KOTAKVISAMASTER002			("D1" , "003" , "Timed out at Acquirer" , "Connecton Timed Out While Connecting to Visa/Master Directory Server"),
	KOTAKVISAMASTER003			("D2" , "022" , "Failed at Acquirer" , "Visa/master Card Directory Server didn’t Send any Response in Specified Time"),
	KOTAKVISAMASTER004			("D3" , "022" , "Failed at Acquirer" , "Unable to Connect to Directory Server through Proxy"),
	KOTAKVISAMASTER005			("UC" , "022" , "Failed at Acquirer" , "Unable to Connect to Card Network"),
	KOTAKVISAMASTER006			("TO" , "003" , "Timed out at Acquirer" , "Issuer Transaction TimeOut"),
	KOTAKVISAMASTER007			("IV" , "022" , "Failed at Acquirer" , "Invalid Issuer Format"),
	KOTAKVISAMASTER008			("PNR" , "022" , "Failed at Acquirer" , "Paresponse Not Received from Issuer"),
	KOTAKVISAMASTER009			("U" , "022" , "Failed at Acquirer" , "Unable to verify"),
	KOTAKVISAMASTER010			("UE" , "022" , "Failed at Acquirer" , "Internal Server Exception"),
	KOTAKVISAMASTER011			("8" , "003" , "Timed out at Acquirer" , "TransactionTimed Out"),
	KOTAKVISAMASTER012			("75" , "004" , "Declined" , "Allowable number of PIN tries exceeded Decline"),
	KOTAKVISAMASTER013			("76" , "004" , "Declined" , "Invalid/nonexistent To Account specified Decline"),
	KOTAKVISAMASTER014			("77" , "004" , "Declined" , "Invalid/nonexistent From Account specified Decline"),
	KOTAKVISAMASTER015			("78" , "004" , "Declined" , "Invalid/nonexistent account specified (general) Decline"),
	KOTAKVISAMASTER016			("84", "004" , "Declined" , "Invalid Authorization Life Cycle Decline"),
	KOTAKVISAMASTER017			("87" , "004" , "Declined" , "Purchase Amount Only, No Cash Back Allowed Approve"),
	KOTAKVISAMASTER018			("00" , "000" , "Captured" , "Success"),
 	KOTAKVISAMASTER019			("VER" , "021" , "Invalid" , "Validation Error"),
 	KOTAKVISAMASTER020			("HNM" , "021" , "Invalid" , "Hash Not Match"),
 	KOTAKVISAMASTER021			("MNE" , "022" , "Failed at Acquirer" , "Merchant Not Enrolled"),
 	KOTAKVISAMASTER022			("STO" , "003" , "Timed out at Acquirer" , "Session Timeout"),
 	KOTAKVISAMASTER023			("IER" , "022" , "Failed at Acquirer" , "Transaction could not Process by Acquiring System"),
 	KOTAKVISAMASTER024			("CAN" , "022" , "Failed at Acquirer" , "Cancel"),
 	KOTAKVISAMASTER025			("01" , "022" , "Failed at Acquirer" , "Refer To Card Issuer"),
 	KOTAKVISAMASTER026			("03" , "022" , "Failed at Acquirer" , "Invalid Merchant Details"),
 	KOTAKVISAMASTER027			("04" , "022" , "Failed at Acquirer" , "Capture Card Or Hotlisted Card"),
 	KOTAKVISAMASTER028			("05" , "004" , "Declined" , "Do Not Honour"),
 	KOTAKVISAMASTER029			("N7" , "004" , "Declined" , "Do Not Honour"),
 	KOTAKVISAMASTER030			("06" , "022" , "Failed at Acquirer" , "Issuer System Error"),
 	KOTAKVISAMASTER031			("07" , "004" , "Declined" , "Pickup Card"),
 	KOTAKVISAMASTER032			("08" , "007" , "Rejected" , "Honour With ID"),
 	KOTAKVISAMASTER033			("10" , "007" , "Rejected" , "Partial Approval"),
 	KOTAKVISAMASTER034			("12" , "021" , "Invalid" , "Invalid Transaction"),
 	KOTAKVISAMASTER035			("IT" , "021" , "Invalid" , "Invalid Transaction"),
 	KOTAKVISAMASTER036			("13" , "021" , "Invalid" , "Invalid Amount"),
 	KOTAKVISAMASTER037			("14" , "021" , "Invalid" , "Invalid Card Number"),
 	KOTAKVISAMASTER038			("15" , "021" , "Invalid" , "Invalid Issuer"),
 	KOTAKVISAMASTER039			("17" , "010" , "Cancelled" ,"Customer Cancellation"),
 	KOTAKVISAMASTER040			("19" , "007" , "Rejected" , "Re-enter Transaction"),
 	KOTAKVISAMASTER041			("21" , "022" , "Failed at Acquirer" , "No Action Taken"),
 	KOTAKVISAMASTER042			("25" , "007" , "Rejected" , "Unable To Locate Record In File"),
 	KOTAKVISAMASTER043			("30" , "007" , "Rejected" , "Switch ISO Format Error (Invalid Acquirer Institue ID)"),
 	KOTAKVISAMASTER044			("FE" , "007" , "Rejected" , "Switch ISO Format Error (Merchant Request Data Format Issue)"),
 	KOTAKVISAMASTER045			("31" , "007" , "Rejected" , "Invalid BIN"),
 	KOTAKVISAMASTER046			("32" , "007" , "Rejected" , "Partial Reversal"),
 	KOTAKVISAMASTER047			("34" , "012" , "Denied due to fraud" , "Suspected Fraud"),
 	KOTAKVISAMASTER048			("59" , "012" , "Denied due to fraud" , "Suspected Fraud"),
 	KOTAKVISAMASTER049			("41" , "002" , "Denied by risk" , "Lost Card"),
 	KOTAKVISAMASTER050			("43" , "002" , "Denied by risk" , "Stolen Card"),
 	KOTAKVISAMASTER051			("51" , "004" , "Declined" , "Insufficient Funds"),
 	KOTAKVISAMASTER052			("52" , "004" , "Declined" , "No Checking Account"),
 	KOTAKVISAMASTER053			("53" , "004" , "Declined" , "No Savings Account"),
 	KOTAKVISAMASTER054			("54" , "004" , "Declined" , "Expired Card"),
 	KOTAKVISAMASTER055			("55" , "004" , "Declined" , "Invalid PIN"),
 	KOTAKVISAMASTER056			("57" , "004" , "Declined" , "Transaction Not Permitted To Issuer Or Cardholder"),
 	KOTAKVISAMASTER057			("58" , "004" , "Declined" , "Transaction Not Permitted To Acquirer Or Merchant"),
 	KOTAKVISAMASTER058			("60" , "004" , "Declined" , "Contact Card Acquirer"),
 	KOTAKVISAMASTER059			("61" , "004" , "Declined" ,"Exceeds Withdrawal Amount Limit"),
 	KOTAKVISAMASTER060			("62" , "004" , "Declined" , "Restricted Card"),
 	KOTAKVISAMASTER061			("63" , "004" , "Declined" , "Security Violation"),
 	KOTAKVISAMASTER062			("65" , "004" , "Declined" , "Exceeds Withdrawal Count Limit"),
 	KOTAKVISAMASTER063			("68" , "007" , "Rejected" , "Response Received Late"),
 	KOTAKVISAMASTER064			("70" , "004" , "Declined" , "Contact Card Issuer"),
 	KOTAKVISAMASTER065			("71" , "007" , "Rejected" , "PIN Not Changed"),
 	KOTAKVISAMASTER066			("85" , "007" , "Rejected" , "Account Verification Transaction"),
 	KOTAKVISAMASTER067			("91" , "022" , "Failed at Acquirer" , "Issuer Unavailable"),
 	KOTAKVISAMASTER068			("92" , "022" , "Failed at Acquirer" , "Unable To Route Transaction"),
 	KOTAKVISAMASTER069			("94" , "022" , "Failed at Acquirer" , "Duplicate Transmission Detected"),
 	KOTAKVISAMASTER070			("96" , "022" , "Failed at Acquirer" , "Issuer System Failure"),
 	KOTAKVISAMASTER071			("RE" , "004" , "Declined" , "Refund Amount Exceeded"),
 	KOTAKVISAMASTER072			("CE" , "007" , "Rejected" , "Capture Amount Exceeded"),
 	KOTAKVISAMASTER073			("DE" , "007" , "Rejected" , "Switch Insert Failed");

	private KotakVisaMasterResultType(String bankCode, String paymentGatewayCode, String statusName, String message) {
		this.bankCode = bankCode;
		this.paymentGatewayCode = paymentGatewayCode;
		this.statusName = statusName;
		this.message = message;
	}

	public static KotakVisaMasterResultType getInstanceFromName(String code) {
		KotakVisaMasterResultType[] statusTypes = KotakVisaMasterResultType.values();
		for (KotakVisaMasterResultType statusType : statusTypes) {
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
