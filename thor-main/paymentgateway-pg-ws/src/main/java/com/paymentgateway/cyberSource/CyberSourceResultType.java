package com.paymentgateway.cyberSource;

public enum CyberSourceResultType {

	CYBERSOURCE001			("100" , "007" , "Rejected" , "Card is not enrolled"),
	CYBERSOURCE002			("101" , "007" , "Rejected" , "Declined   The request is missing one or more fields"),
	CYBERSOURCE003			("102" , "007" , "Rejected" , "Declined   One or more fields in the request contains invalid data"),
	CYBERSOURCE004			("104" , "007" , "Rejected" , "Declined ThemerchantReferenceCode sent with this authorization request matches the merchantReferenceCode of another authorization request that you sent in the last 15 minutes."),
	CYBERSOURCE005			("110" , "007" , "Rejected" , "Partial amount was approved"),
	CYBERSOURCE006			("150" , "007" , "Rejected" , "Error   General system failure."),
	CYBERSOURCE007			("151" , "003" , "Timed out at Acquirer" , "Error   The request was received but there was a server timeout."),
	CYBERSOURCE008			("152" , "003" , "Timed out at Acquirer" , "Error The request was received, but a service did not finish running in time."),
	CYBERSOURCE009			("200" , "004" , "Declined" , "Soft Decline   The authorization request was approved by the issuing bank but declined by CyberSource."),
	CYBERSOURCE010			("201" , "004" , "Declined" , "Decline   The issuing bank has questions about the request."),
	CYBERSOURCE011			("202" , "004" , "Declined" , "Decline   Expired card."),
	CYBERSOURCE012			("203" , "004" , "Declined" , "Decline   General decline of the card."),
	CYBERSOURCE013			("204" , "004" , "Declined" , "Decline   Insufficient funds in the account."),
	CYBERSOURCE014			("205" , "004" , "Declined" , "Decline   Stolen or lost card."),
	CYBERSOURCE015			("207" , "004" , "Declined" , "Decline   Issuing bank unavailable."),
	CYBERSOURCE016			("208" , "004" , "Declined" , "Decline   Inactive card or card not authorized for card not present transactions."),
	CYBERSOURCE017			("209" , "004" , "Declined" , "Decline card verification number did not match."),
	CYBERSOURCE018			("210" , "004" , "Declined" , "Decline   The card has reached the credit limit."),
	CYBERSOURCE019			("211" , "004" , "Declined" , "Decline   Invalid Card Verification Number (CVN)."),
	CYBERSOURCE020			("220" , "004" , "Declined" , "Decline   Generic Decline."),
	CYBERSOURCE021			("221" , "004" , "Declined" , "Decline   The customer matched an entry on the processors negative file."),
	CYBERSOURCE022			("222" , "004" , "Declined" , "Decline   customer's account is frozen"),
	CYBERSOURCE023			("230" , "004" , "Declined" , "Soft Decline   The authorization request was approved by the issuing bank but declined by CyberSource because it did not pass the card verification number (CVN) check."),
	CYBERSOURCE024			("231" , "004" , "Declined" , "Decline   Invalid account number"),
	CYBERSOURCE025			("232" , "004" , "Declined" , "Decline   The card type is not accepted by the payment processor."),
	CYBERSOURCE026			("233" , "004" , "Declined" , "Decline   General decline by the processor."),
	CYBERSOURCE027			("234" , "007" , "Rejected" , "Decline   There is a problem with your CyberSource merchant configuration."),
	CYBERSOURCE028			("235" , "007" , "Rejected" , "Decline   The requested amount exceeds the originally authorized amount."),
	CYBERSOURCE029			("236" , "022" , "Failed at Acquirer" , "Decline   Processor failure."),
	CYBERSOURCE030			("237" , "007" , "Rejected" , "Decline   The authorization has already been reversed."),
	CYBERSOURCE031			("238" , "007" , "Rejected" , "Decline   The transaction has already been settled."),
	CYBERSOURCE032			("239" , "007" , "Rejected" , "Decline   The requested transaction amount must match the previous transaction amount."),
	CYBERSOURCE033			("240" , "021" , "Invalid" , "Decline   The card type sent is invalid or does not correlate with the credit card number."),
	CYBERSOURCE034			("241" , "007" , "Rejected" , "Decline   The referenced request id is invalid for all follow on transactions."),
	CYBERSOURCE035			("242" , "007" , "Rejected" , "Decline   The request ID is invalid."),
	CYBERSOURCE036			("243" , "007" , "Rejected" , "Decline   The transaction has already been settled or reversed."),
	CYBERSOURCE037			("246" , "007" , "Rejected" , "Decline   The capture or credit is not voidable because the capture or credit information has already been submitted to your processor. Or, you requested a void for a type of transaction that cannot be voided"),
	CYBERSOURCE038			("247" , "007" , "Rejected" , "Decline   You requested a credit for a capture that was previously voided."),
	CYBERSOURCE039			("248" , "004" , "Declined" , "Decline   The boleto request was declined by your processor."),
	CYBERSOURCE040			("250" , "003" , "Timed out at Acquirer" , "Error   The request was received, but there was a timeout at the payment processor."),
	CYBERSOURCE041			("251" , "004" , "Declined" , "Decline   The Pinless Debit card's use frequency or maximum amount per use has been exceeded."),
	CYBERSOURCE042			("254" , "004" , "Declined" ,"Decline   Account is prohibited from processing stand alone refunds."),
	CYBERSOURCE043			("400" , "012" , "Denied due to fraud" , "Soft Decline   Fraud score exceeds threshold."),
	CYBERSOURCE044			("450" , "002" , "Denied by risk" , "Apartment number missing or not found."),
	CYBERSOURCE045			("451" , "002" , "Denied by risk" , "Insufficient address information."),
	CYBERSOURCE046			("452" , "002" , "Denied by risk" , "House/Box number not found on street."),
	CYBERSOURCE047			("453" , "002" , "Denied by risk" , "Multiple address matches were found."),
	CYBERSOURCE048			("454" , "002" , "Denied by risk" , "P.O. Box identifier not found or out of range."),
	CYBERSOURCE049			("455" , "002" , "Denied by risk" , "Route service identifier not found or out of range."),
	CYBERSOURCE050			("456" , "002" , "Denied by risk" , "Street name not found in Postal code."),
	CYBERSOURCE051			("457" , "002" , "Denied by risk" , "Postal code not found in database."),
	CYBERSOURCE052			("458" , "002" , "Denied by risk" , "Unable to verify or correct address."),
	CYBERSOURCE053			("459" , "002" , "Denied by risk" , "Multiple addres matches were found (international)"),
	CYBERSOURCE054			("460" , "002" , "Denied by risk" , "Address match not found (no reason given)"),
	CYBERSOURCE055			("461" , "002" , "Denied by risk" , "Unsupported character set"),
	CYBERSOURCE057			("476" , "004" , "Declined" , "Encountered a Payer Authentication problem."),
	CYBERSOURCE058			("480" , "007" , "Rejected" , "The order is marked for review by Decision Manager"),
	CYBERSOURCE059			("481" , "007" , "Rejected" , "The order has been rejected by Decision Manager"),
	CYBERSOURCE060			("520" , "004" , "Declined" , "Soft Decline   The authorization request was approved by the issuing bank but declined by CyberSource based on your Smart Authorization settings."),
	CYBERSOURCE061			("700" , "002" , "Denied by risk" , "The customer matched the Denied Parties List"),
	CYBERSOURCE062			("701" , "002" , "Denied by risk" , "Export bill_country/ship_country match"),
	CYBERSOURCE063			("702" , "002" , "Denied by risk" , "Export email_country match"),
	CYBERSOURCE064			("703" , "002" , "Denied By risk" , "Export hostname_country/ip_country match");
	
	
	
	

	private CyberSourceResultType(String bankCode, String paymentGatewayCode, String statusName, String message) {
		this.bankCode = bankCode;
		this.paymentGatewayCode = paymentGatewayCode;
		this.statusName = statusName;
		this.message = message;
	}

	public static CyberSourceResultType getInstanceFromName(String code) {
		CyberSourceResultType[] statusTypes = CyberSourceResultType.values();
		for (CyberSourceResultType statusType : statusTypes) {
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
