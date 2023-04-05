package com.paymentgateway.payout.icici.composite;

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
	
//	//VPA
//	ICICI118			("999588", "021", "Failed", "Invalid VPA"),
//	ICICI119			("999584", "021", "Failed", "Invalid Customer details"),
//	ICICI120			("101324", "021", "Failed", "User ID is mandatory"),
//	ICICI121			("103024", "021", "Failed", "Corporate ID is mandatory"),
//	ICICI122			("999590", "021", "Failed", "Bene user already registered"),
//	ICICI123			("999586", "021", "Failed", "VPA null"),
//	ICICI124			("999587", "021", "Failed", "URN null"),
//	
//	
//	
//	
//	ICICI150		    ("100340", "000", "Failed", "Counterparty already exists. Please give a unique Bank id, Branch id, Account number combination"),
//	ICICI151		    ("999039", "021", "Failed", "Beneficiary not found"),
//	ICICI152		    ("999032", "021", "Failed", "Incorrect IFSC entered"),
////	ICICI153		    ("995109", "021", "Failed", "REGISTRATION FAILED.CORPORATE ALREADY REGISTERED"),
////	ICICI154		    ("995109", "021", "Failed", "REGISTRATION FAILED.URN IS MANDATORY"),
////	ICICI155		    ("995109", "021", "Failed", "REGISTRATION FAILED.AGGREGATOR ID IS MANDATORY"),
////	ICICI156		    ("995109", "021", "Failed", "REGISTRATION FAILED.AGGREGATOR NAME IS MANDATORY"),
////	ICICI157		    ("995109", "021", "Failed", "REGISTRATION FAILED.USERID IS MANDATORY"),
////	ICICI158		    ("995109", "021", "Failed", "REGISTRATION FAILED.CORPID IS MANDATORY"),
//	ICICI159		    ("101436", "021", "Failed", "Incorrect IFSC entered"),
	
	
	
	
	
	//CIB
	
	
//	ICICI017			("99274", "004", "Declined", "OTP validation Required"),
//	ICICI018			("101422", "022", "Failed at Acquirer", "Invalid Currency set up. Please contact Bank Administrator"),
//	ICICI019			("100019", "022", "Failed at Acquirer", "Debit Account is invalid"),
//	ICICI020			("106753", "021", "Invalid", "Transaction Type is mandatory"),
//	ICICI021			("103354", "021", "Invalid", "Invalid Bank/Branch Identifier or Network."),
//	ICICI022			("100249", "004", "Declined", "Transaction amount limit exceeded or no limit configured. Please call Customer Care/Branch"),
//	ICICI023			("103064", "003", "Timeout", "Transaction timed out at Beneficiary Bank. Please check with Bene Bank before initiating again"),
//	ICICI024			("100031", "022", "Failed at Acquirer", "Transaction failed during processing, due to Transaction not permitted"),
//	
//	ICICI026			("100030", "022", "Failed at Acquirer", "Transaction failed during processing, due to Insufficient funds"),
//	ICICI027			("14084", "022", "Failed at Acquirer", "Transaction failed during processing, due to Host not available"),
//	ICICI028			("995034", "022", "Failed at Acquirer", "NFS Host unreachable."),
//	ICICI029		    ("108220", "004", "Declined", "The transaction cannot be processed with the available networks. Contact the bank administrator"),
//	ICICI030		    ("8010", "022", "Failed at Acquirer", "Bank servers down"),
//	ICICI031		    ("995030", "004", "Declined", "Beneficiary Bank not available. Amount if debited, will be reversed"),
//	ICICI032		    ("106803", "004", "Declined", "Invalid Login Credentials"),
//	ICICI033		    ("107027", "004", "Declined", "Counterparty Account is a closed account"),
//	ICICI034		    ("101443", "022", "Failed at Acquirer", "Enter the valid amount in the range supported by the network"),
//	ICICI035		    ("100046", "021", "Invalid", "Invalid Account Number"),
//	ICICI036		    ("999050", "021", "Invalid", "Invalid Account Number.Please contact bank administrator"),
//	ICICI037		    ("100731", "008", "Duplicate", "Selected initiator and counterparty accounts are same. Please select different accounts"),
//	ICICI038		    ("8013", "003", "Timeout", "Read timeout"),
//	ICICI039		    ("995007", "021", "Invalid", "Invalid Beneficiary Mobile Number/MMID/Account number"),
//	ICICI040		    ("995011", "004", "Declined", "Beneficiary account is closed"),
//	ICICI041		    ("999353", "004", "Declined", "Unable to process your request, please call customer care or visit nearest ICICI bank branch"),
//	ICICI042		    ("106589", "004", "Declined", "Unable to process request"),
//	ICICI043		    ("995098", "004", "Declined", "You cannot initiate/modify this transaction as per the applicable workflow rule"),
//	ICICI044		    ("1294", "004", "Declined", "You do not have transaction access on this account"),
//	ICICI045		    ("8012", "003", "Timeout", "Connection timeout"),
//	ICICI046		    ("106803", "021", "Invalid", "Invalid Login Credentials"),
//	ICICI047		    ("9907", "004", "Declined", "User is not registered"),
//	ICICI049		    ("994006", "021", "Invalid", "OTP Validation Failed(If OTP enter is wrong)"),
//	ICICI050		    ("900", "003", "Timeout", "Please try after sometime as the NEFT post cutoff limits are crossed"),
//	ICICI051		    ("107889", "003", "Timeout", "OTP Validation Failed(if OTP enter after 15 Mins)"),
//	ICICI052		    ("100043", "008", "Duplicate", "The transaction has been submitted successfully. Please do not re-initiate, check the status of the transaction after 15 minutes"),
//	ICICI053		    ("999936", "008", "Duplicate", "Transactions already processed with same unique ID, please use exclusive unique id for each transaction");
//	
//	
	
	private IciciCompositeImpsResultType(String bankCode, String PaymentGatewayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.paymentGatewayCode = PaymentGatewayCode;
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
