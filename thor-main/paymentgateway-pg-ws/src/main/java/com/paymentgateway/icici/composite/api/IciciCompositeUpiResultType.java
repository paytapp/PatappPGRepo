package com.paymentgateway.icici.composite.api;

public enum IciciCompositeUpiResultType {
	
	//VPA bene registration
	ICICI001			("999588", "021", "Invalid at acquirer", "Invalid VPA"),
	ICICI002			("999584", "021", "Invalid at acquirer", "Invalid Customer details"),
	ICICI003			("101324", "021", "Invalid at acquirer", "User ID is mandatory"),
	ICICI004			("103024", "021", "Invalid at acquirer", "Corporate ID is mandatory"),
	ICICI005			("999590", "021", "Failed", "Bene user already registered"),
	ICICI006			("999586", "021", "Invalid at acquirer", "VPA null"),
	ICICI007			("999587", "021", "Invalid at acquirer", "URN null"),
	
	
	ICICI008			("0" , "000" , "Captured" , "Success"),
	ICICI009			("11" , "022" , "Failed" , "invalid data"),
	ICICI010			("12" , "022" , "Failed" , "invalid device id"),
	ICICI011			("13" , "022" , "Failed" , "channel code not present in request"),
	ICICI012			("14" , "021" , "Invalid at acquirer" , "sequence no not present"),
	ICICI013			("37" , "021" , "Invalid at acquirer" , "Invalid Virtual address"),
	ICICI014			("39" , "021" , "Invalid at acquirer" , "duplicate seq no from channel"),
	ICICI015			("5" , 	"003" , "Timeout" , "UPI Server Internal Error"),
	ICICI016			("31" , "000" , "Failed" , "mobile no not present"),
	ICICI017			("91" , "003" , "Timeout" , "Transaction Initiated"),
	ICICI018			("9999" , "003" , "Timeout" , "UPI Technical Error"),
	ICICI019			("00XF" , "021" , "Invalid at acquirer" , "FORMAT ERROR (INVALID FORMAT) (REMITTER)"),
	ICICI020			("00XH" , "021" , "Invalid at acquirer" , "ACCOUNT DOES NOT EXIST (REMITTER)"),
	ICICI021			("00XJ" , "022" , "Failed" , "REQUESTED FUNCTION NOT SUPPORTED"),
	ICICI022			("00ZD" , "022" , "Failed" , "VALIDATION ERROR"),
	ICICI023			("00XP" , "004" , "Declined" , "TRANSACTION NOT PERMITTED TO CARDHOLDER (REMITTER)"),
	ICICI024			("00XV" , "022" , "Failed" , "TRANSACTION CANNOT BE COMPLETED. COMPLIANCE VIOLATION (REMITTER)"),
	ICICI025			("00YC" , "022" , "Failed" , "DO NOT HONOUR (REMITTER)"),
	ICICI026			("00YE" , "004" , "Declined" , "REMITTING ACCOUNT BLOCKED/FROZEN"),
	ICICI027			("00XK" , "004" , "Declined" , "REQUESTED FUNCTION NOT SUPPORTED"),
	ICICI028			("00XB" , "004" , "Timeout" , "INVALID TRANSACTION OR IF MEMBER IS NOT ABLE TO FIND ANY APPROPRIATE RESPONSE CODE (REMITTER)"),
	ICICI029			("00XC" , "004" , "Timeout" , "INVALID TRANSACTION OR IF MEMBER IS NOT ABLE TO FIND ANY APPROPRIATE RESPONSE CODE (BENEFICIARY)"),
	ICICI030			("00UT" , "003" , "Timeout" , "REMITTER/ISSUER UNAVAILABLE (TIMEOUT)"),
	ICICI031			("0U01" , "022" , "Failed" , "The request is duplicate"),
	ICICI032			("0U05" , "022" , "Failed" , "Formation is not proper"),
	ICICI033			("0U07" , "022" , "Failed" , "Validation error"),
	ICICI034			("0U08" , "022" , "Failed" , "System exception"),
	ICICI035			("0U10" , "004" , "Declined" , "Illegal operation"),
	ICICI036			("0U13" , "004" , "Declined" , "External error"),
	ICICI037			("0U16" , "004" , "Declined" , "Risk threshold exceeded"),
	ICICI038			("0U17" , "004" , "Declined" , "PSP is not registered"),
	ICICI039			("0U28" , "004" , "Timeout" , "PSP not available"),
	ICICI040			("0U51" , "004" , "Declined" , "PSP orgId not found"),
	ICICI041			("0U55" , "022" , "Failed" , "Message integrity failed due to orgid mismatch"),
	ICICI042			("0L05" , "003" , "Timeout" , "Technical Issue, please try after some time"),
	ICICI043			("0L16" , "003" , "Timeout" , "Unknown error occurred"),
	ICICI044			("71" , "022" , "Failed" , "signingKey cannot be null"),
	ICICI045			("72" , "003" , "Timeout" , "NPCI UPI Not available"),
	ICICI046			("73" , "003" , "Timeout" , "NPCI UPI Not available"),
	ICICI047			("74" , "003" , "Timeout" , "NPCI UPI Not available"),
	ICICI048			("0L06" , "004" , "Declined" , "Key Code has not been provided in input"),
	ICICI049			("0L07" , "004" , "Declined" , "Error while parsing Key Code from input"),
	ICICI050			("0L08" , "022" , "Failed" , "XML Payload has not been provided in input"),
	ICICI051			("0L09" , "022" , "Failed" , "Error while parsing XML Payload from input"),
	ICICI052			("0L10" , "022" , "Failed" , "Error while parsing Controls from input"),
	ICICI053			("0L11" , "022" , "Failed" , "Error while parsing Configuration from input"),
	ICICI054			("0L12" , "022" , "Failed" , "Salt has not been provided in input"),
	ICICI055			("0L13" , "022" , "Failed" , "Error while parsing Salt from input"),
	ICICI056			("0L14" , "022" , "Failed" , "Error while parsing Pay Info from input"),
	ICICI057			("0L15" , "022" , "Failed" , "Error while parsing Locale from input"),
	ICICI058			("0L17" , "022" , "Failed" , "Trust has not been provided"),
	ICICI059			("0L18" , "022" , "Failed" , "Mandatory salt values have not been provided"),
	ICICI060			("0L19" , "022" , "Failed" , "Error while parsing mandatory salt values"),
	ICICI061			("0L20" , "022" , "Failed" , "Trust is not valid"),
	ICICI062			("0U66" , "022" , "Failed" , "Device Fingerprint mismatch"),
	ICICI063			("0U48" , "021" , "Invalid at acquirer" , "Transaction is id not present"),
	ICICI064			("0U49" , "022" , "Failed" , "Request message id is not present"),
	ICICI065			("0U50" , "021" , "Invalid at acquirer" , "IFSC is not present"),
	ICICI066			("0U52" , "022" , "Failed" , "Request refund is not found"),
	ICICI067			("0U53" , "022" , "Failed" , "PSP Request Pay Debit Acknowledgement not received"),
	ICICI068			("0U54" , "022" , "Failed" , "Transaction Id or Amount in credential block does not match with that in ReqPay"),
	ICICI069		    ("0U56" , "022" , "Failed" , "Number of Payees differs from original request"),
	ICICI070			("0U57" , "022" , "Failed" , "Payee Amount differs from original request"),
	ICICI071			("0U58" , "022" , "Failed" , "Payer Amount differs from original request"),
	ICICI072			("0U59" , "022" , "Failed" , "Payee Address differs from original request"),
	ICICI073			("0U60" , "022" , "Failed" , "Payer Address differs from original request"),
	ICICI074			("0U61" , "022" , "Failed" , "Payee Info differs from original request"),
	ICICI075			("0U62" , "022" , "Failed" , "Payer Info differs from original request"),
	ICICI076			("0U63" , "022" , "Failed" , "Device registration failed in UPI"),
	ICICI077			("0U64" , "022" , "Failed" , "Data tag should contain 4 parts during device registration"),
	ICICI078			("0U65" , "022" , "Failed" , "Creds block should contain correct elements during device registration"),
	ICICI079			("0U27" , "003" , "Timeout" , "No response from PSP"),
	ICICI080			("F01" , "022" , "Failed" , "RegDetails must be present <ReqRegMob/>"),
	ICICI081			("F02" , "022" , "Failed" , "RegDetails.Detail must be present"),
	ICICI082			("F03" , "022" , "Failed" , "RegDetails.Detail name/value should be present name,value"),
	ICICI083			("F04" , "022" , "Failed" , "RegDetails.Detail name not valid"),
	ICICI084			("F05" , "022" , "Failed" , "RegDetails.Cred not present"),
	ICICI085			("F06" , "022" , "Failed" , "RegDetails.Cred data is wrong"),
	ICICI086			("F07" , "022" , "Failed" , "RegDetails.Cred.Otp must be present"),
	ICICI087			("F08" , "022" , "Failed" , "RegDetails.Cred.Pin must be present"),
	ICICI088			("F09" , "022" , "Failed" , "RegDetails.Cred.Data must be present"),
	ICICI089			("F10" , "022" , "Failed" , "RegDetails.Cred.Data encrypted authentication must be present"),
	ICICI090			("00ZM" , "022" , "Failed" , "INVALID MPIN"),
	ICICI091			("00XL" , "022" , "Failed" , "EXPIRED CARD, DECLINE (REMITTER)"),
	ICICI092			("00XN" , "022" , "Failed" , "NO CARD RECORD (REMITTER)"),
	ICICI093			("00XR" , "022" , "Failed" , "RESTRICTED CARD, DECLINE (REMITTER)"),
	ICICI094			("00XM" , "022" , "Failed" , "EXPIRED CARD, DECLINE (BENEFICIARY)"),
	ICICI095			("00XO" , "022" , "Failed" , "NO CARD RECORD (BENEFICIARY)"),
	ICICI096			("00XQ" , "022" , "Failed" , "TRANSACTION NOT PERMITTED TO CARDHOLDER (BENEFICIARY)"),
	ICICI097			("00XS" , "022" , "Failed" , "RESTRICTED CARD, DECLINE (BENEFICIARY)"),
	ICICI098			("00XU" , "022" , "Failed" , "CUT-OFF IS IN PROCESS (BENEFICIARY)"),
	ICICI099			("00XW" , "022" , "Failed" , "TRANSACTION CANNOT BE COMPLETED. COMPLIANCE VIOLATION (BENEFICIARY)"),
	ICICI100			("00Y1" , "022" , "Failed" , "BENEFICIARY CBS OFFLINE"),
	ICICI101		    ("00YB" , "022" , "Failed" , "LOST OR STOLEN CARD (BENEFICIARY)"),
	ICICI102			("00YD" , "022" , "Failed" , "DO NOT HONOUR (BENEFICIARY)"),
	ICICI103			("00YF" , "022" , "Failed" , "BENEFICIARY ACCOUNT BLOCKED/FROZEN"),
	ICICI104			("00RN" , "022" , "Failed" , "Registration is temporary blocked due to maximum no of attempts exceeded"),
	ICICI105			("0U11" , "022" , "Failed" , "Credentials is not present"),
	ICICI106			("00XY" , "003" , "Timeout", "REMITTER CBS OFFLINE"),
	ICICI107			("00AM" , "022" , "Failed" , "MPIN not set by customer"),
	ICICI108			("00ZA" , "022" , "Failed" , "collect Auth rejected by customer"),
	ICICI109			("00BT" , "003" , "Timeout" , "ACQUIRER/BENEFICIARY UNAVAILABLE(TIMEOUT)"),
	ICICI110			("00RB" , "022" , "Failed" , "CREDIT REVERSAL TIMEOUT(REVERSAL)"),
	ICICI111			("00RR" , "022" , "Failed" , "DEBIT REVERSAL TIMEOUT(REVERSAL)"),
	ICICI112			("00RP" , "022" , "Failed" , "PARTIAL DEBIT REVERSAL TIMEOUT"),
	ICICI113			("U29" , "022" , "Failed" , "Address resolution is failed"),	
	ICICI114			("U30" , "022" , "Failed" , "Debit has been failed"),
	ICICI115			("U31" , "022" , "Failed" , "Credit has been failed"),
	ICICI116			("U32" , "022" , "Failed" , "Credit revert has been failed"),
	ICICI117			("U33" , "022" , "Failed" , "Debit revert has been failed"),
	ICICI118			("U34" , "022" , "Failed" , "Reverted"),
	ICICI119			("U66" , "022" , "Failed" , "Device Fingerprint mismatch"),
	ICICI120			("U67" , "003" , "Timeout" , "Debit Timeout"),
	ICICI121			("U68" , "003" , "Timeout" , "Credit Timeout"),
	ICICI122			("U69" , "022" , "Failed" , "Collect Expired"),
	ICICI123			("U70" , "022" , "Failed" , "Received Late Response"),
	ICICI124			("1" , "022" , "Failed" , "User profile not found"),
	ICICI125			("2" , "022" , "Failed" , "OTP not matched"),
	ICICI126			("3" , "022" , "Failed" , "ICICI OTP engine not available"),
	ICICI127			("4" , "022" , "Failed" , "Response parsing error"),
	ICICI128			("5" , "022" , "Failed" , "Virtual address already exists"),
	ICICI129			("6" , "022" , "Failed" , "UPI user already present"),
	ICICI130			("7" , "022" , "Failed" , "OTP request rejected"),
	ICICI131			("8" , "022" , "Failed" , "Mobile registration rejected"),
	ICICI132			("9" , "004" , "Declined" , "Transaction rejected"),
	ICICI133			("10" , "022" , "Failed" , "Insufficient data"),
	ICICI134			("15" , "022" , "Failed" , "Original record not found"),
	ICICI135			("16" , "022" , "Failed" , "Record not found in va-account mapping"),
	ICICI136			("17" , "022" , "Failed" , "Key not present"),
	ICICI137			("18" , "003" , "Timeout" , "RIB request Failed"),
	ICICI138			("23" , "022" , "Failed" , "Input Format Error. Check Validation Summary array in JSON response"),
	ICICI139			("6" , "003" , "Timeout" , "UPI Switch Error ** Only for financial transaction"),
	ICICI140			("0038" , "022" , "Failed" , "Mobile number and profile Id doesn't belong to same User"),
	ICICI141			("0055" , "022" , "Failed" , "virtual address already exist"),
	ICICI142			("U09" , "003" , "Timeout" , "REQAUTH TIME OUT FOR PAY"),
	ICICI143			("U17" , "022" , "Failed" , "PSP IS NOT REGISTERED"),
	ICICI144			("U18" , "022" , "Failed" , "REQUEST AUTHORISATION ACKNOWLEDGEMENT IS NOT RECEIVED"),
	ICICI145		    ("U26" , "022" , "Failed" , "PSP REQUEST CREDIT PAY ACKNOWLEDGEMENT IS NOT RECEIVED"),
	ICICI146			("U28" , "022" , "Failed" , "PSP NOT AVAILABLE"),
	ICICI147			("U48" , "022" , "Failed" , "TRANSACTION ID IS NOT PRESENT"),
	ICICI148			("U80" , "021" , "Failed" , "UIDAI TIMEOUT"),
	ICICI149			("U88" , "003" , "Timeout" , "CONNECTION TIMEOUT IN REQPAY CREDIT"),
	ICICI150			("5" , "022" , "Failed" , "format error"),
	ICICI151			("27" , "022" , "Failed" , "IMPS Switch not connected"),
	ICICI152			("1025" , "021" , "Invalid at acquirer" , "Invalid payee account"),
	ICICI153			("101" , "003" , "Timeout" , "Transaction Processing Timeout"),
	ICICI154			("98" , "003" , "Timeout" , "ICICI CBS Host timed out"),
	ICICI155			("95" , "022" , "Timeout" , "cbs unreachable"),
	ICICI156			("92" , "003" , "Timeout" , "Transaction Initiated"),
	ICICI157			("91" , "003" , "Timeout" , "Timeout"),
	ICICI158			("Deemed" , "003" , "Timeout" , "Transactions has moved to deemed status (Status Check With Bank)"),
	ICICI159			("1033" , "003" , "Timeout" , "PENDING Initiate status check"),
	ICICI177			("94" , "003" , "Timeout" , "Timeout"),
	ICICI178			("99" , "003" , "Timeout" , "Timeout"),
	ICICI179			("T13" , "003" , "Timeout" , "Timeout"),
	ICICI180			("M3" , "003" , "Timeout" , "Timeout"),
	ICICI181			("M1" , "003" , "Failed" , "Failed"),
	
	//API
	ICICI160			("500" , "022" , "Failed" , "Internal Server Error"),
	ICICI161			("401" , "022" , "Failed" , "Unauthorized"),
	ICICI162			("429" , "004" , "Declined" , "Too Many Requests"),
	ICICI163			("403" , "022" , "Failed" , "Forbidden"),
	ICICI164			("997" , "003" , "Timeout" , "Internal timeout"),
	ICICI165			("1025" , "022" , "Failed" , "Bad request"),
	ICICI166			("8010" , "022" , "Failed" , "INTERNAL_SERVICE_FAILURE"),
	ICICI167			("8011" , "022" , "Failed" , "Host Not Found"),
	ICICI168			("8012" , "022" , "Failed" , "BACKEND_CONNECTION_TIMEOUT - Cannot connect to service"),
	ICICI169			("8013" , "022" , "Failed" , "BACKEND_READ_TIMEOUT - Cannot read from service"),
	ICICI170			("8014" , "022" , "Failed" , "Bad URL"),
	ICICI171			("8015" , "022" , "Failed" , "Digital Signature Validation Failure"),
	ICICI172			("8016" , "022" , "Failed" , "Request Decryption Failure"),
	ICICI173			("8017" , "022" , "Failed" , "Request Schema Validation Failure"),
	ICICI174			("8018" , "022" , "Failed" , "Response Schema Validation Failure"),
	ICICI175			("8019" , "022" , "Failed" , "Response Encryption Failure"),
	ICICI176			("8099" , "003" , "Timeout" , "Blank Response from Backend");
	
	
	
	private IciciCompositeUpiResultType(String bankCode, String paymentGatewayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.paymentGatewayCode = paymentGatewayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static IciciCompositeUpiResultType getInstanceFromName(String code) {
		IciciCompositeUpiResultType[] statusTypes = IciciCompositeUpiResultType.values();
		for (IciciCompositeUpiResultType statusType : statusTypes) {
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
