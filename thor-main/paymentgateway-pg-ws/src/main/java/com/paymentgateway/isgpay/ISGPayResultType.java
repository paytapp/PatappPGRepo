package com.paymentgateway.isgpay;

/**
 * @author Rahul
 *
 */

public enum ISGPayResultType {

	ISGPAY001			("00" , "000" , "Captured" , "Successful transaction."),
	ISGPAY002			("VER" , "004" , "Declined" , "Validation Error."),
	ISGPAY003			("HNM" , "007" , "Rejected" , "Hash Not Match."),
	ISGPAY004			("MNE" , "007" , "Rejected" , "Merchant Not Enrolled"),
	ISGPAY005			("STO" , "003" , "Timed out at Acquirer" , "Session Timeout at Acquirer"),
	ISGPAY006			("IER" , "022" , "Failed at Acquirer" , "Acquirer Internal Error"),
	ISGPAY007			("CAN" , "010" , "Cancelled" , "User pressed Cancel Button."),
	ISGPAY008			("RAE" , "004" , "Declined" , "Request Amount Exceeded."),
	ISGPAY009			("RNF" , "007" , "Rejected" , "Requested Transaction not found"),
	ISGPAY010			("RDF" , "007" , "Rejected" , "Duplicate Transaction Request found"),
	ISGPAY011			("DOI" , "007" , "Rejected" , "Duplicate order id sent by merchant."),
	ISGPAY012			("01" ,  "004" , "Declined" , "Card Issuer has indicated there is a problem with the card number. The customer should contact their bank or the customer should use an alternate card."),
	ISGPAY013			("NBF",  "004" , "Declined" , "When Net-Banking transaction is failed."),
	ISGPAY014			("03" ,  "004" , "Declined" , "This Error indicates that either your merchant is not available or the details entered are incorrect."),
	ISGPAY015			("04" ,  "004" , "Declined" , "Issuer Specific Decline The card has been reported lost or stolen."),
	ISGPAY016			("05" ,  "004" , "Declined" , "Issuer specific decline when CCV or Expiry Date does not Match."),
	ISGPAY017			("N7" ,  "004" , "Declined" , "Issuer specific decline when CCV or Expiry Date does not Match."),
	ISGPAY018			("06" ,  "007" , "Rejected" , "Issuer System Error."),
	ISGPAY019			("07" ,  "004" , "Declined" , "Issuer Specific Decline The card has been reported lost or stolen."),
	ISGPAY020			("08" ,  "003" , "Timed out at Acquirer" , "Transaction Timed Out"),
	ISGPAY021			("10" ,  "004" , "Declined" ,"Partial Approval."),
	ISGPAY022			("12" ,  "007" , "Rejected" ,  "Issuer has declined the transaction because of an invalid format or field. This indicates the card details were incorrect. Check card data entered and try again."),
	ISGPAY023			("IT" ,	 "007" , "Rejected" , "Issuer has declined the transaction because of an invalid format or field. This indicates the card details were incorrect. Check card data entered and try again."),
	ISGPAY024			("13" ,  "004" , "Declined" , "Issuer has declined the transaction because of an invalid Amount format or field."),
	ISGPAY025			("14" ,  "004" , "Declined" , "Issuer has declined the transaction as the credit card number is incorrectly entered, or does not exist. Check card details and try processing again."),
	ISGPAY026			("15" ,  "004" , "Declined" , "The customer  card issuer does not exist. Check the card information and try processing the transaction again."),
	ISGPAY027			("21" ,  "004" , "Declined" , "The customer card issuer has indicated there is a problem with the card number. The customer should contact their card issuer and/or use an alternate credit card."),
	ISGPAY028			("25" ,  "004" , "Declined" , "Issuer does not recognize the card details. The customer should check the card information and try processing the transaction again."),
	ISGPAY029			("30" ,  "007" , "Rejected" , "Issuer does not recognize the transaction details being entered. This is due to a Data format error."),
	ISGPAY030			("FE" ,  "007" , "Rejected" , "Issuer does not recognize the transaction details being entered. This is due to a Data format error."),
	ISGPAY031			("31" ,  "004" , "Declined" , "issuer has declined the transaction as BIN is Not Present or Not a valid Bin."),
	ISGPAY032			("32" ,  "004" , "Declined" , "Approved for Partial Amount  Reversal."),
	ISGPAY033			("34" ,  "002" , "Denied by risk" , "Issuer has declined the transaction as there is a suspected fraud on this Card number."),
	ISGPAY034			("59" ,  "002" , "Denied by risk" ,	"Issuer has declined the transaction as there is a suspected fraud on this Card number."),
	ISGPAY035			("41" ,  "002" , "Denied by risk" , "Issuer has declined the transaction as the card has been reported lost."),
	ISGPAY036			("43" ,  "002" , "Denied by risk" , "Card has been reported as Stolen."),
	ISGPAY037			("51" ,  "004" , "Declined" , "Insufficient Funds"),
	ISGPAY038			("52" ,  "004" , "Declined" , "Issuer has declined the transaction as the card number is associated to a cheque account that does not exist."),
	ISGPAY039			("53" ,  "004" , "Declined" , "Issuer has declined the transaction as the  card number is associated to a savings account that does not exist."),
	ISGPAY040			("54" ,  "004" , "Declined" ,  "Issuer has declined the transaction as the  card appears to have expired."),
	ISGPAY041			("57" ,  "004" , "Declined" , "Issuer has declined the transaction as this  card cannot be used for this type of transaction."),
	ISGPAY042			("58" ,  "004" , "Declined" , "Transaction Not Permitted To Acquirer Or Merchant."),
	ISGPAY043			("60" ,  "004" , "Declined" , "Bank has declined the transaction. The customer should contact their bank and retry the transaction."),
	ISGPAY044			("61" ,  "004" , "Declined" , "Issuer has declined the transaction as it will exceed the customer card limit."),
	ISGPAY045			("62" ,  "004" , "Declined" , "Issuer has declined the transaction as the card has some restrictions. The customer should contact their bank for further information.."),
	ISGPAY046			("63" ,  "004" , "Declined" , "Issuer has declined the transaction. The customer should use an alternate credit card and contact their card issuer if the problem persists."),
	ISGPAY047			("65" ,  "004" , "Declined" , "Issuer has declined the transaction as the customer has exceeded the withdrawal frequency limit."),
	ISGPAY048			("68" ,  "022" , "Failed at Acquirer" , "Transaction Response not receive in time."),
	ISGPAY049			("70" ,  "004" , "Declined" , "Issuer has declined the transaction. The customer should contact their bank and retry the transaction."),
	ISGPAY050			("91" ,  "022" , "Failed at Acquirer" , "Issuer is unable to be contacted to authorize the transaction."),
	ISGPAY051			("94" ,  "004" , "Declined" , "Issuer has declined the transaction as this transaction appears to be a duplicate transmission."),
	ISGPAY052			("96" ,  "022" , "Failed at Acquirer" , "Issuer was not able to process the transaction. The customer should attempt to process this transaction again."),
	ISGPAY053			("D1" ,  "022" , "Failed at Acquirer" , "Connection Timed Out While Connecting to Visa Master Directory Server."),
	ISGPAY054			("D2" ,  "022" , "Failed at Acquirer" , "Visa / MasterCard Directory server not responded within specified time duration."),
	ISGPAY055			("3DSF" ,  "004" , "Declined" , "3DS authentication failed."),
	ISGPAY056			("ACCU200" , "010" , "Cancelled" , "User pressed Cancel Button."),
	ISGPAY057			("RTO" , "004" , "Declined" , "NPCI Didnt send any data in specified time."),
	ISGPAY058			("203" , "004" , "Declined" , "Transaction Not Authorized By Issuer."),
	ISGPAY059			("N/A" , "004" , "Declined" , "Transaction is declined."),
	ISGPAY060			("204" , "004" , "Declined" , "Insufficient funds in the account."),
	ISGPAY061			("202" , "004" , "Declined" , "Invalid Card Expiry Date."),
	ISGPAY062			("RRA" , "000" , "Captured" , "Refund Request Accepted"),
	ISGPAY063			("93" , "007" , "Rejected" , "Transaction cannot be completed");
	
	private ISGPayResultType(String bankCode, String iPayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.iPayCode = iPayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static ISGPayResultType getInstanceFromName(String code) {
		ISGPayResultType[] statusTypes = ISGPayResultType.values();
		for (ISGPayResultType statusType : statusTypes) {
			if (String.valueOf(statusType.getBankCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}

	private final String bankCode;
	private final String iPayCode;
	private final String statusCode;
	private final String message;

	public String getBankCode() {
		return bankCode;
	}

	public String getiPayCode() {
		return iPayCode;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}
}