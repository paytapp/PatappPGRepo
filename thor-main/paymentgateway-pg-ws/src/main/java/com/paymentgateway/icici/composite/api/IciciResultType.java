package com.paymentgateway.icici.composite.api;

public enum IciciResultType {
	
	ICICI001			("0" , "000" , "Captured" , "Transaction Successful"),
	ICICI002			("61", "022", "Failed at Acquirer", "Reason Unknown"),
	ICICI003			("31", "003", "Timeout", "No transaction response from CDCI"),
	ICICI004			("1", "004", "Declined", "Invalid Account Number"),
	ICICI005			("2", "004", "Declined", "Amount limit exceeded for the customer"),
	ICICI006			("3", "004", "Declined", "Frozen Account"),
	ICICI007			("4", "004", "Declined", "NRE Account"),
	ICICI008			("5", "004", "Declined", "Closed Account"),
	ICICI009			("6", "004", "Declined", "NET Debit cap exceeded for member bank"),
	ICICI010			("10", "004", "Declined", "Invalid Bank / NBIN"),
	ICICI011			("11", "003", "Timeout", "Time out at NPCI"),
	ICICI012			("13", "004", "Declined", "Invalid amount"),
	ICICI013			("80", "008", "Duplicate", "Initial original request is already been processed"),
	ICICI014			("16", "021", "Invalid", "Format Error"),
	ICICI015			("999033" , "008" , "Duplicate" , "Transaction ID is already used"),
	ICICI016			("9906", "022", "Failed at Acquirer", "Invalid Corp Id or User Id or Aggregator Id is passed"),
	ICICI017			("99274", "004", "Declined", "OTP validation Required"),
	ICICI018			("101422", "022", "Failed at Acquirer", "Invalid Currency set up. Please contact Bank Administrator"),
	ICICI019			("100019", "022", "Failed at Acquirer", "Debit Account is invalid"),
	ICICI020			("106753", "021", "Invalid", "Transaction Type is mandatory"),
	ICICI021			("103354", "021", "Invalid", "Invalid Bank/Branch Identifier or Network."),
	ICICI022			("100249", "004", "Declined", "Transaction amount limit exceeded or no limit configured. Please call Customer Care/Branch"),
	ICICI023			("103064", "003", "Timeout", "Transaction timed out at Beneficiary Bank. Please check with Bene Bank before initiating again"),
	ICICI024			("100031", "022", "Failed at Acquirer", "Transaction failed during processing, due to Transaction not permitted"),
	ICICI025			("995109", "008", "Duplicate", "Registration Failed Corporate Already Registerd"),
	ICICI026			("100030", "022", "Failed at Acquirer", "Transaction failed during processing, due to Insufficient funds"),
	ICICI027			("14084", "022", "Failed at Acquirer", "Transaction failed during processing, due to Host not available"),
	ICICI028			("995034", "022", "Failed at Acquirer", "NFS Host unreachable."),
	ICICI029		    ("108220", "004", "Declined", "The transaction cannot be processed with the available networks. Contact the bank administrator"),
	ICICI030		    ("8010", "022", "Failed at Acquirer", "Bank servers down"),
	ICICI031		    ("995030", "004", "Declined", "Beneficiary Bank not available. Amount if debited, will be reversed"),
	ICICI032		    ("106803", "004", "Declined", "Invalid Login Credentials"),
	ICICI033		    ("107027", "004", "Declined", "Counterparty Account is a closed account"),
	ICICI034		    ("101443", "022", "Failed at Acquirer", "Enter the valid amount in the range supported by the network"),
	ICICI035		    ("100046", "021", "Invalid", "Invalid Account Number"),
	ICICI036		    ("999050", "021", "Invalid", "Invalid Account Number.Please contact bank administrator"),
	ICICI037		    ("100731", "008", "Duplicate", "Selected initiator and counterparty accounts are same. Please select different accounts"),
	ICICI038		    ("8013", "003", "Timeout", "Read timeout"),
	ICICI039		    ("995007", "021", "Invalid", "Invalid Beneficiary Mobile Number/MMID/Account number"),
	ICICI040		    ("995011", "004", "Declined", "Beneficiary account is closed"),
	ICICI041		    ("999353", "004", "Declined", "Unable to process your request, please call customer care or visit nearest ICICI bank branch"),
	ICICI042		    ("106589", "004", "Declined", "Unable to process request"),
	ICICI043		    ("995098", "004", "Declined", "You cannot initiate/modify this transaction as per the applicable workflow rule"),
	ICICI044		    ("1294", "004", "Declined", "You do not have transaction access on this account"),
	ICICI045		    ("8012", "003", "Timeout", "Connection timeout"),
//	ICICI046		    ("106803", "021", "Invalid", "Invalid Login Credentials"),
	ICICI047		    ("9907", "004", "Declined", "User is not registered"),
	ICICI049		    ("994006", "021", "Invalid", "OTP Validation Failed(If OTP enter is wrong)"),
	ICICI050		    ("900", "003", "Timeout", "Please try after sometime as the NEFT post cutoff limits are crossed"),
	ICICI051		    ("107889", "003", "Timeout", "OTP Validation Failed(if OTP enter after 15 Mins)"),
	ICICI052		    ("100043", "008", "Duplicate", "The transaction has been submitted successfully. Please do not re-initiate, check the status of the transaction after 15 minutes"),
	ICICI053		    ("999936", "008", "Duplicate", "Transactions already processed with same unique ID, please use exclusive unique id for each transaction");
	
	
	
	private IciciResultType(String bankCode, String paymentGatewayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.paymentGatewayCode = paymentGatewayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static IciciResultType getInstanceFromName(String code) {
		IciciResultType[] statusTypes = IciciResultType.values();
		for (IciciResultType statusType : statusTypes) {
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
