package com.paymentgateway.icici.composite.api;

public enum IciciCompositeImpsResultType {
	
	ICICI001			("0" , "000" , "Captured" , "Transaction Successful"),
	ICICI004			("1", "021", "Invalid at acquirer", "Invalid Beneficiary Mobile No / MAS"),
	ICICI005			("2", "004", "Declined", "Amount limit exceeded for the customer"),
	ICICI006			("3", "004", "Declined", "Frozen Account"),
	ICICI007			("4", "004", "Declined", "NRE Account"),
	ICICI008			("5", "004", "Declined", "Closed Account"),
	ICICI009			("6", "004", "Declined", "NET Debit cap exceeded for member bank"),
	ICICI014			("7", "021", "Invalid at acquirer", "Transaction not permitted for this account type"),
	ICICI054			("8", "004", "Declined", "Transaction limit exceeded for this account type"),
	ICICI055			("9", "004", "Declined", "Transaction not allowed as this is non-reloadable card"),
	ICICI010			("10", "004", "Declined", "No routing for institution/network"),
	ICICI011			("11", "003", "Timeout", "Time out at NPCI"),
	ICICI056			("12", "004", "Declined", "Bank not live on IMPS account / IFSC based transfer"),
	ICICI012			("13", "021", "Invalid at acquirer", "Invalid amount"),
	ICICI057			("14", "004", "Declined", "Duplicate transaction"),
	ICICI058			("15", "004", "Declined", "Beneficiary is Merchant"),
	ICICI059			("16", "021", "Invalid at acquirer", "Format Error"),
	ICICI060			("17", "004", "Declined", "Transaction not found"),
	ICICI061			("18", "004", "Declined", "NPCI/Issuing bank is not connected or down"),
	ICICI062			("19", "004", "Declined", "Unconfigured RC"),
	ICICI063			("20", "004", "Declined", "Invalid response code"),
	ICICI064			("21", "004", "Declined", "Decline on verification"),
	ICICI065			("24", "004", "Declined", "Transaction not allowed - amount greated than 2 lacs"),
	ICICI066			("30", "003", "Timeout", "No transaction response from NPCI"),
	ICICI067			("31", "003", "Timeout", "No transaction response from CDCI"),
	ICICI068			("32", "003", "Timeout", "Time out at ICICI CBS for outward transaction (Beneficiary non-ICICI Customer)"),
	ICICI069			("33", "003", "Timeout", "Time out at ICICI CBS for intra transaction (Beneficiary ICICI customer)"),
	ICICI070			("34", "004", "Declined", "Suspect fraud"),
	ICICI071			("35", "004", "Declined", "Invalid Message"),
	ICICI072			("36", "004", "Declined", "Invalid Transaction"),
	ICICI073			("37", "021", "Invalid at acquirer", "Invalid Amount"),
	ICICI074			("38", "004", "Declined", "Transfer amount exceeds limit"),
	ICICI075			("41", "004", "Declined", "Transaction not processed as client txn date expired"),
	ICICI076			("51", "004", "Declined", "Transaction declined by beneficiary bank due to insufficient funds"),
	ICICI077			("52", "021", "Invalid at acquirer", "Invalid Account"),
	ICICI078			("60", "004", "Declined", "Reason Unknown (Technical decline by IMPS)"),
	ICICI079			("61", "004", "Declined", "Reason Unknown (Technical decline by CBS)"),
	ICICI080			("62", "004", "Declined", "Reason Unknown (Technical decline by NPCI)"),
	ICICI081			("63", "003", "Timeout", "No response from IMPS switch"),
	ICICI082			("65", "022", "Failed", "Exceed Amount limit"),
	ICICI083			("70", "022", "Failed", "OCH - Technical Failure"),
	ICICI084			("71", "021", "Invalid at acquirer", "OCH - Invalid amount"),
	ICICI085			("72", "021", "Invalid at acquirer", "OCH Invalid account number"),
	ICICI086			("73", "003", "Timeout", "OCH Timeout"),
	ICICI087			("74", "022", "Failed", "OCH Batch limit has been exhausted"),
	ICICI088			("75", "004", "Declined", "Duplicate transaction in OCH"),
	ICICI089			("76", "022", "Failed", "Account balance is below defined threshold value"),
	ICICI090			("77", "004", "Declined", "Balance Inquiry has been decline due to technical error"),
	ICICI091			("80", "004", "Declined", "Initial request is already been processed"),
	ICICI092			("96", "003", "Timeout", "Unable to process"),
	ICICI093			("101", "022", "Failed", "IMPS Switch Not reachable"),
	ICICI094			("102", "003", "Timeout", "Connectivity disconnected, transaction in processing mode"),
	ICICI095			("201", "021", "Invalid at acquirer", "Invalid Beneficiary IFSC/NBIN"),
	ICICI096			("202", "004", "Declined", "Customer Limit Exceeded"),
	ICICI097			("203", "004", "Declined", "Foreign inward remittance not allowed"),
	ICICI098			("204", "021", "Invalid at acquirer", "Transaction not allowed as invalid payment reference"),
	ICICI099			("205", "021", "Invalid at acquirer", "Transaction amount less than Rs.1"),
	ICICI100			("206", "021", "Invalid at acquirer", "Transaction not allowed as invalid remitter account"),
	ICICI101			("207", "004", "Declined", "Transaction not allowed as general error"),
	ICICI102			("208", "004", "Declined", "Foreign inward remittance for P2P only"),
	ICICI104			("403", "021", "Invalid at acquirer", "Unauthorized API Access"),
	ICICI105			("901", "021", "Invalid at acquirer", "Invalid BC"),
	ICICI106			("902", "021", "Invalid at acquirer", "Authentication failed"),
	ICICI107			("903", "021", "Invalid at acquirer", "Invalid BC Retailer"),
	ICICI108			("904", "022", "Failed", "No response from DCMS"),
	ICICI109			("905", "022", "Failed", "Card Validation failed"),
	ICICI110			("916", "022", "Failed", "BC Nodal Account not supported"),
	ICICI111			("918", "022", "Failed", "BC Retailer code mapping not Found"),
	ICICI112			("919", "022", "Failed", "BC monthly amount high"),
	ICICI113			("920", "021", "Invalid at acquirer", "BC min invalid amount"),
	ICICI114			("921", "021", "Invalid at acquirer", "BC max invalid amount"),
	ICICI115			("922", "022", "Failed", "Channel GST amount missing"),
	ICICI116			("923", "022", "Failed", "Channel service charge amount missing"),
	ICICI117			("924", "022", "Failed", "Channel retailer state missing"),
	ICICI145			("69" , "022" , "Failed" , "Funds reversals"),
	
	
	//IMPS BENE Addition
	ICICI118			("24036" , "021", "Invalid at acquirer" , "You cannot logon at this time. Please contact the bank for further information"),
	ICICI119			("111", "022", "Failed", "Connection timeout"),	
	ICICI120			("995109", "021", "Invalid at acquirer", "Registration Failed"),
	ICICI121			("106803", "021", "Invalid at acquirer at acquirer",  "Invalid at acquirer Login Credentials"),
	ICICI122			("9906", "021", "Invalid at acquirer", "Invalid Corp Id or User Id or Aggregator Id is passed"),
	ICICI123			("999032", "021", "Invalid at acquirer", "Customer Not Registered"),
	ICICI124			("100340", "022", "Failed", "Counterparty already exists. Please give a unique Bank id, Branch id, Account number combination"),
	ICICI125			("101436", "021", "Invalid at acquirer", "Incorrect IFSC entered"),
	ICICI126            ("999039", "021", "Invalid at acquirer", "Beneficiary not found"),
	ICICI144            ("100260", "022", "Failed", "Nickname already exists. Please give a unique Nickname"),
	ICICI035		    ("100046", "021", "Invalid at acquirer", "Invalid Account Number"),
	
	
	//API
	ICICI127			("500" , "022" , "Failed" , "Internal Server Error"),
	ICICI128			("401" , "022" , "Failed" , "Unauthorized"),
	ICICI129			("429" , "004" , "Declined" , "Too Many Requests"),
	ICICI130			("403" , "022" , "Failed" , "Forbidden"),
	ICICI131			("997" , "022" , "Failed" , "Internal timeout"),
	ICICI132			("1025" , "022" , "Failed" , "Bad request"),
	ICICI133			("8010" , "022" , "Failed" , "INTERNAL_SERVICE_FAILURE"),
	ICICI134			("8011" , "022" , "Failed" , "Host Not Found"),
	ICICI135			("8012" , "022" , "Failed" , "BACKEND_CONNECTION_TIMEOUT - Cannot connect to service"),
	ICICI136			("8013" , "022" , "Failed" , "BACKEND_READ_TIMEOUT - Cannot read from service"),
	ICICI137			("8014" , "022" , "Failed" , "Bad URL"),
	ICICI138			("8015" , "022" , "Failed" , "Digital Signature Validation Failure"),
	ICICI139			("8016" , "022" , "Failed" , "Request Decryption Failure"),
	ICICI140			("8017" , "022" , "Failed" , "Request Schema Validation Failure"),
	ICICI141			("8018" , "022" , "Failed" , "Response Schema Validation Failure"),
	ICICI142			("8019" , "022" , "Failed" , "Response Encryption Failure"),
	ICICI143			("8099" , "003" , "Timeout" , "Blank Response from Backend");

	
	private IciciCompositeImpsResultType(String bankCode, String paymentGatewayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.paymentGatewayCode = paymentGatewayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static IciciCompositeImpsResultType getInstanceFromName(String code) {
		IciciCompositeImpsResultType[] statusTypes = IciciCompositeImpsResultType.values();
		for (IciciCompositeImpsResultType statusType : statusTypes) {
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
	
	public String getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}

	public String getPaymentGatewayCode() {
		return paymentGatewayCode;
	}

}
