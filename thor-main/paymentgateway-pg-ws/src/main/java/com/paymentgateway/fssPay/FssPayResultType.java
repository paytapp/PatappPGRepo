package com.paymentgateway.fssPay;


public enum FssPayResultType {

	FSS001			("incorrect PIN" , "004" , "Declined" , "incorrect PIN"),
	FSS002			("Failure - Invalid expiration date" , "004" , "Declined" , "Failure - Invalid expiration date"),
	FSS003			("CAF status 0 or 9" , "022" , "Failed at Acquirer" , "CAF status 0 or 9"),
	FSS004			("not sufficient fund" , "004" , "Declined" , "not sufficient fund"),
	FSS005			("tran not permitted" , "022" , "Failed at Acquirer" , "tran not permitted"),
	FSS006			("invalid transaction" , "004" , "Declined" , "invalid transaction"),
	FSS007			("expired card" , "004" , "Declined" ,"expired card"),
	FSS008			("lost card" , "002" , "Denied by risk" , "lost card"),
	FSS009			("FAILURE - IPAY0100263 - Transaction details not available." , "004" , "Declined" , "FAILURE - IPAY0100263 - Transaction details not available."),
	FSS010			("GW00201-Transaction details not available." , "004" , "Declined" , "GW00201-Transaction details not available."),
	FSS011			("unauthorized usage" , "002" , "Denied by risk" , "unauthorized usage"),
	FSS012			("lost or stolen card (cap)" , "002" , "Denied by risk" , "lost or stolen card (cap)"),
	FSS013			("pin tries exceeded" , "002" , "Denied by risk" , "pin tries exceeded"),
	FSS014			("no card record" , "004" , "Declined" , "no card record"),
	FSS015			("invalid card number" , "004" , "Declined" , "invalid card number"),
	FSS016			("FAILURE" , "022" , "Failed at Acquirer" , "FAILURE"),
	FSS017			("Decline - Exceeds Merchant Limit" , "004" , "Declined" , "Decline - Exceeds Merchant Limit"),
	FSS018			("Reserved For Private Use" , "004" , "Declined" , "Reserved For Private Use"),
	FSS019			("FAILURE - lost card" , "002" , "Denied by risk" , "FAILURE - lost card"),
	FSS020			("FAILURE - not sufficient fund" , "004" , "Declined" , "FAILURE - not sufficient fund"),
	FSS021			("FAILURE(NOT CAPTURED)" , "022" , "Failed at Acquirer" ,"FAILURE(NOT CAPTURED)"),
	FSS022			("FAILURE - CAF status= 0 or 9" , "022" , "Failed at Acquirer" ,  "FAILURE - CAF status= 0 or 9"),
	FSS023			("Blank - Declined" , "004" , "Declined" , "Blank - Declined"),
	FSS024			("Blank - Processed" , "004" , "Declined" , "Blank - Processed"),
	FSS025			("Blank - Presented" , "004" , "Declined" , "Blank - Presented"),
	FSS026			("issuer down" , "004" , "Declined" , "issuer down"),
	FSS027			("no idf" , "022" , "Failed at Acquirer" , "no idf"),
	FSS028			("FAILURE - suspect fraud" , "002" , "Denied by risk" , "FAILURE - suspect fraud"),
	FSS029			("Blank - INITIALIZED" , "022" , "Failed at Acquirer" , "Blank - INITIALIZED"),
	FSS030			("NOT CAPTURED" , "022" , "Failed at Acquirer" , "NOT CAPTURED"),
	FSS031			("system failure" , "022" , "Failed at Acquirer" , "system failure"),
	FSS032			("FAILURE - IPAY0100203 - Problem occured while doing perform transaction" , "022" , "Failed at Acquirer" , "FAILURE - IPAY0100203 - Problem occured while doing perform transaction"),
	FSS033			("response received too late" , "022" , "Failed at Acquirer", "response received too late"),
	FSS034			("FAILURE - exceeds withdrawal frequency" , "004" , "Declined" ,"FAILURE - exceeds withdrawal frequency"),
	FSS035			("Bin range not enabled." , "004" , "Declined" , "Bin range not enabled."),
	FSS036			("FAILURE-Transaction time limit exceeds." , "004" , "Declined" , "FAILURE-Transaction time limit exceeds."),
	FSS037			("FAILURE-GW00881-Invalid card holder name." , "004" , "Declined" , "FAILURE-GW00881-Invalid card holder name."),
	FSS038			("FAILURE- AUTH ERROR" , "022" , "Failed at Acquirer" , "FAILURE- AUTH ERROR"),
	FSS039			("FAILURE-Payment Instrument not enabled." , "022" , "Failed at Acquirer" , "FAILURE-Payment Instrument not enabled."),
	FSS040			("CANCELED" , "022" , "Failed at Acquirer" ,  "CANCELED"),
	FSS041			("FAILURE-IPAY0100042 - Transaction time limit exceeds." , "004" , "Declined" , "FAILURE-IPAY0100042 - Transaction time limit exceeds."),
	FSS042			("FAILURE - reserved for private use" , "022" , "Failed at Acquirer" , "FAILURE - reserved for private use"),
	FSS043			("Blank - INITIALIZED - Hosted" , "022" , "Failed at Acquirer" , "Blank - INITIALIZED - Hosted"),
	FSS044			("Blank - INITIALIZED - Tranportal" , "022" , "Failed at Acquirer" , "Blank - INITIALIZED - Tranportal"),
	FSS045			("FAILURE-GW00852-Invalid card number." , "004" , "Declined" , "FAILURE-GW00852-Invalid card number."),
	FSS046			("FAILURE-Error while connecting Payment Gateway-" , "022" , "Failed at Acquirer" , "FAILURE-Error while connecting Payment Gateway-"),
	FSS047			("FAILURE(SUSPECT)" , "002" , "Denied by risk" , "FAILURE(SUSPECT)"),
	FSS048			("FAILURE-Bank ID is not enabled in Aggregator Terminal" , "022" , "Failed at Acquirer" , "FAILURE-Bank ID is not enabled in Aggregator Terminal"),
	FSS049			("FAILURE(DENIED BY RISK)" , "002" , "Denied by risk" , "FAILURE(DENIED BY RISK)"),
	FSS050			("FAILURE-IPAY0100008 - Terminal not enabled." , "002" , "Denied by risk" , "FAILURE-IPAY0100008 - Terminal not enabled."),
	FSS051			("FAILURE - over daily limit" , "004" , "Declined" , "FAILURE - over daily limit"),
	FSS052			("Transaction denied due to failed card check digit calculation." , "004" , "Declined" , "Transaction denied due to failed card check digit calculation."),
	FSS053			("FAILURE - bad track2" , "022" , "Failed at Acquirer" , "FAILURE - bad track2"),
	FSS054			("FAILURE-Bank ID is not available in the request or Invalid bank ID" , "022" , "Failed at Acquirer" , "FAILURE-Bank ID is not available in the request or Invalid bank ID"),
	FSS055			("FAILURE-Transaction timed-out" , "022" , "Failed at Acquirer" , "FAILURE-Transaction timed-out"),
	FSS056			("FAILURE-GW00859-Missing expiry month and year." , "004" , "Declined" , "FAILURE-GW00859-Missing expiry month and year."),
	FSS057			("FAILURE(HOST TIMEOUT)" , "004" , "Declined" , "FAILURE(HOST TIMEOUT)"),
	FSS058			("FAILURE-Invalid card expiry year." , "004" , "Declined" , "FAILURE-Invalid card expiry year."),
	FSS059			("FAILURE-Invalid card expiry month." , "004" , "Declined" , "FAILURE-Invalid card expiry month."),
	FSS060			("FAILURE-IPAY0100049-Card holder not registered for OTP" , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100049-Card holder not registered for OTP"),
	FSS061			("FAILURE-CANCELLED" , "022" , "Failed at Acquirer" , "FAILURE-CANCELLED"),
	FSS062			("FAILURE-GW00254-Transaction denied due to Risk : Maximum debit amount" , "002" , "Denied by risk" , "FAILURE-GW00254-Transaction denied due to Risk : Maximum debit amount"),
	FSS063			("FAILURE-GW00256-Transaction denied due to Risk : Maximum transaction count" , "002" , "Denied by risk" , "FAILURE-GW00256-Transaction denied due to Risk : Maximum transaction count"),
	FSS064			("FAILURE-GW00250-Denied by risk : Negative Card check - Fail" , "002" , "Denied by risk" , "FAILURE-GW00250-Denied by risk : Negative Card check - Fail"),
	FSS065			("FAILURE-GW00259-Denied by risk : Declined Card check - Fail" , "002" , "Denied by risk" , "FAILURE-GW00259-Denied by risk : Declined Card check - Fail"),
	FSS066			("FAILURE-IPAY0100049-OTP ATTEMPTS EXCEED" , "004" , "Declined" , "FAILURE-IPAY0100049-OTP ATTEMPTS EXCEED"),
	FSS067			("FAILURE-IPAY0100049-OTP RESEND ATTEMPTS EXCEED" , "004" , "Declined" , "FAILURE-IPAY0100049-OTP RESEND ATTEMPTS EXCEED"),
	FSS068			("FAILURE-GW00856-Invalid cvv." , "004" , "Declined" , "FAILURE-GW00856-Invalid cvv."),
	FSS069			("FAILURE-GW00160-Brand not enabled." , "022" , "Failed at Acquirer" , "FAILURE-GW00160-Brand not enabled."),
	FSS070			("FAILURE-Invalid card number." , "004" , "Declined" , "FAILURE-Invalid card number."),
	FSS071			("FAILURE-CM00002-EXTERNAL MESSAGE SYSTEM ERROR" , "022" , "Failed at Acquirer" , "FAILURE-CM00002-EXTERNAL MESSAGE SYSTEM ERROR"),
	FSS072			("FAILURE-IPAY0100180 - Authentication not available." , "004" , "Declined" , "FAILURE-IPAY0100180 - Authentication not available."),
	FSS073			("FAILURE-IPAY0100126 - Brand not enabled." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100126 - Brand not enabled."),
	FSS074			("FAILURE-IPAY0100106 - Invalid payment instrument" , "004" , "Declined" , "FAILURE-IPAY0100106 - Invalid payment instrument"),
	FSS075			("FAILURE-IPAY0100073 - Transaction denied due to invalid CVV." , "004" , "Declined" , "FAILURE-IPAY0100073 - Transaction denied due to invalid CVV."),
	FSS076			("FAILURE-GW00854-Invalid card expiry month." , "004" , "Declined" , "FAILURE-GW00854-Invalid card expiry month."),
	FSS077			("FAILURE-OTP Authentication Failed" , "004" , "Declined" , "FAILURE-OTP Authentication Failed"),
	FSS078			("FAILURE-GW00854-Invalid card expiry year." , "004" , "Declined" , "FAILURE-GW00854-Invalid card expiry year."),
	FSS079			("FAILURE-Maximum OTP Retry Exceeded" , "004" , "Declined" , "FAILURE-Maximum OTP Retry Exceeded"),
	FSS080			("FAILURE-Card Not enrolled" , "004" , "Declined" , "FAILURE-Card Not enrolled"),
	FSS081			("FAILURE-Problem while Connecting ACS" , "022" , "Failed at Acquirer" , "FAILURE-Problem while Connecting ACS"),
	FSS082			("FAILURE-CM90000-Problem occurred while updating payment details." , "022" , "Failed at Acquirer" , "FAILURE-CM90000-Problem occurred while updating payment details."),
	FSS083			("FAILURE-IPAY0100120 - Transaction denied due to invalid payment instrument for brand data." , "004" , "Declined" , "FAILURE-IPAY0100120 - Transaction denied due to invalid payment instrument for brand data."),
	FSS084			("FAILURE-IPAY0100159 - External message system error" , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100159 - External message system error"),
	FSS085			("FAILURE-IPAY0100293 - Transaction denied due to duplicate Merchant trackid" , "004" , "Declined" , "FAILURE-IPAY0100293 - Transaction denied due to duplicate Merchant trackid"),
	FSS086			("FAILURE-GW00314-Transaction denied due to Risk : Minimum Transaction Amount processing." , "022" , "Declined" , "FAILURE-GW00314-Transaction denied due to Risk : Minimum Transaction Amount processing."),
	FSS087			("Transaction denied due to invalid processing option action code" , "022" , "Failed at Acquirer" , "Transaction denied due to invalid processing option action code"),
	FSS088			("FAILURE(SUSPECT)-Transaction Details not Available" , "022" , "Failed at Acquirer" , "FAILURE(SUSPECT)-Transaction Details not Available"),
	FSS089			("expired card (cap)" , "004" , "Declined" , "expired card (cap)"),
	FSS090			("IPAY0100263 - Transaction details not available." , "022" , "Failed at Acquirer" , "IPAY0100263 - Transaction details not available."),
	FSS091			("FAILURE-Host is down" , "022" , "Failed at Acquirer" , "FAILURE-Host is down"),
	FSS092			("FAILURE-HOST TIMEOUT" , "022" , "Failed at Acquirer" , "FAILURE-HOST TIMEOUT"),
	FSS093			("FAILURE-IPAY0100274 - VERES message format is invalid" , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100274 - VERES message format is invalid"),
	FSS094			("FAILURE-IPAY0100247 - PARES message format is invalid" , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100247 - PARES message format is invalid"),
	FSS095			("FAILURE(SUSPECT)-Problem occured while connecting Amazon" , "022" , "Failed at Acquirer" , "FAILURE(SUSPECT)-Problem occured while connecting Amazon"),
	FSS096			("FAILURE--CM90002-Problem occurred during transaction." , "022" , "Failed at Acquirer" , "FAILURE--CM90002-Problem occurred during transaction."),
	FSS097			("FAILURE-Error while processing request!" , "022" , "Failed at Acquirer" , "FAILURE-Error while processing request!"),
	FSS098			("FAILURE-Error while connecting Payment Gateway" , "022" , "Failed at Acquirer" , "FAILURE-Error while connecting Payment Gateway"),
	FSS099			("FAILURE-IPAY0100158 - Host timeout" , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100158 - Host timeout"),
	FSS100			("FAILURE-AUTH ERROR" , "022" , "Failed at Acquirer" , "FAILURE-AUTH ERROR"),
	FSS101			("FAILURE-Problem occured while verifying amazon signature" , "004" , "Declined" , "FAILURE-Problem occured while verifying amazon signature"),
	FSS102			("FAILURE(SUSPECT)-Transaction not found" , "004" , "Declined" , "FAILURE(SUSPECT)-Transaction not found"),
	FSS103			("FAILURE-AUTHORIZE PARSE ERROR" , "004" , "Declined" , "FAILURE-AUTHORIZE PARSE ERROR"),
	FSS104			("FAILURE - IPAY0100073 - Transaction denied due to invalid CVV." , "004" , "Declined" , "FAILURE - IPAY0100073 - Transaction denied due to invalid CVV."),
	FSS105			("FAILURE-IPAY0100080 - Transaction denied due to invalid expiration date." , "004" , "Declined" , "FAILURE-IPAY0100080 - Transaction denied due to invalid expiration date."),
	FSS106			("FAILURE-IPAY0100119 - Transaction denied due to invalid card number" , "004" , "Declined" , "FAILURE-IPAY0100119 - Transaction denied due to invalid card number"),
	FSS107			("FAILURE-Declined due to duplicate transaction" , "004" , "Declined" , "FAILURE-Declined due to duplicate transaction"),
	FSS108			("IPAY0100008 - Terminal not enabled." , "022" , "Failed at Acquirer" , "IPAY0100008 - Terminal not enabled."),
	FSS109			("FAILURE-Bin Range is not enabled in Binrange table" , "004" , "Declined" , "FAILURE-Bin Range is not enabled in Binrange table"),
	FSS110			("FAILURE-IPAY0100049- IDFC WebService Connection Problem" , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100049- IDFC WebService Connection Problem"),
	FSS111			("FAILURE(SUSPECT)-WAITING-BANK-RESPONSE" , "022" , "Failed at Acquirer" , "FAILURE(SUSPECT)-WAITING-BANK-RESPONSE"),
	FSS112			("FAILURE-Missing transaction data" , "022" , "Failed at Acquirer" , "FAILURE-Missing transaction data"),
	FSS113			("FAILURE - no idf" , "022" , "Failed at Acquirer" , "FAILURE - no idf"),
	FSS114			("FAILURE(SUSPECT)-TRANSACTION-INCOMPLETE" , "022" , "Failed at Acquirer" , "FAILURE(SUSPECT)-TRANSACTION-INCOMPLETE"),
	FSS115			("FAILURE-NARR999-Mobile Number does not exist in DCMS or Technical Error" , "022" , "Failed at Acquirer" , "FAILURE-NARR999-Mobile Number does not exist in DCMS or Technical Error"),
	FSS116			("FAILURE-IPAY0100266 - Brand directory unavailable." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100266 - Brand directory unavailable."),
	FSS117			("Transaction failed" , "022" , "Failed at Acquirer" , "Transaction failed"),
	FSS118			("FAILURE-Empty card details." , "004" , "Declined" , "FAILURE-Empty card details."),
	FSS119			("FAILURE-IPAY0100265 - enstage response validation failed." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100265 - enstage response validation failed."),
	FSS120			("FAILURE-Error while connecting Payment Gateway--error occurred due to connect exception" , "022" , "Failed at Acquirer" , "FAILURE-Error while connecting Payment Gateway--error occurred due to connect exception"),
	FSS121			("IPAY0100254 - Merchant not enabled for performing transaction." , "004" , "Declined" , "IPAY0100254 - Merchant not enabled for performing transaction."),
	FSS122			("FAILURE-Rupay transaction details not available." , "004" , "Declined" , "FAILURE-Rupay transaction details not available."),
	FSS123			("FAILURE-IPAY0100107 - Instrument not enabled." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100107 - Instrument not enabled."),
	FSS124			("FAILURE-IPAY0100163 - Problem occured during transaction." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100163 - Problem occured during transaction."),
	FSS125			("FAILURE-IPAY0200041 - Problem occured while getting institution configuration." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0200041 - Problem occured while getting institution configuration."),
	FSS126			("FAILURE-IPAY0100254 - Merchant not enabled for performing transaction." , "004" , "Declined" , "FAILURE-IPAY0100254 - Merchant not enabled for performing transaction."),
	FSS127			("FAILURE-IPAY0100255 - External connection not enabled." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100255 - External connection not enabled."),
	FSS128			("FAILURE-IPAY0200012 - Problem occured while updating payment log ip details." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0200012 - Problem occured while updating payment log ip details."),
	FSS129			("FAILURE-IPAY0100257 - Brand rules not enabled." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100257 - Brand rules not enabled."),
	FSS130			("FAILURE-IPAY0200059 - Problem occured while updating vpas details." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0200059 - Problem occured while updating vpas details."),
	FSS131			("FAILURE-IPAY0100182 - Vpas merchant not enabled." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100182 - Vpas merchant not enabled."),
	FSS132			("FAILURE-IPAY0100207 - Bin range not enabled." , "004" , "Declined" , "FAILURE-IPAY0100207 - Bin range not enabled."),
	FSS133			("FAILURE-IPAY0100009 - Institution not enabled." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100009 - Institution not enabled."),
	FSS134			("FAILURE-Currency not enabled." , "022" , "Failed at Acquirer" , "FAILURE-Currency not enabled."),
	FSS135			("FAILURE-" , "022" , "Failed at Acquirer" , "FAILURE-"),
	FSS136			("FAILURE-IPAY0200056 - Problem occurred while getting brand details." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0200056 - Problem occurred while getting brand details."),
	FSS137			("FAILURE-IPAY0100058 - Transaction denied due to invalid instrument" , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100058 - Transaction denied due to invalid instrument"),
	FSS138			("IPAY0100054 - Payment details not available." , "004" , "Declined" , "IPAY0100054 - Payment details not available."),
	FSS139			("FAILURE-Transaction denied due to duplicate transaction" , "004" , "Declined" , "FAILURE-Transaction denied due to duplicate transaction"),
	FSS140			("FAILURE(SUSPECT)-FAILED-NO-RESPONSE" , "022" , "Failed at Acquirer" , "FAILURE(SUSPECT)-FAILED-NO-RESPONSE"),
	FSS141			("FAILURE-AUTH ERROR-Issuer Authentication Server failure" , "022" , "Failed at Acquirer" , "FAILURE-AUTH ERROR-Issuer Authentication Server failure"),
	FSS142			("FAILURE - tran not permitted" , "004" , "Declined" , "FAILURE - tran not permitted"),
	FSS143			("!ERROR!-GW00160-Bin range not enabled.-PY20010-Problem occurred during merchant response." , "022" , "Failed at Acquirer" , "!ERROR!-GW00160-Bin range not enabled.-PY20010-Problem occurred during merchant response."),
	FSS144			("!ERROR!-GW00201-Transaction not found." , "004" , "Declined" , "!ERROR!-GW00201-Transaction not found."),
	FSS145			("CANCELLED" , "022" , "Failed at Acquirer" , "CANCELLED"),
	FSS146			("DENIED BY RISK" , "002" , "Denied by risk" , "DENIED BY RISK"),
	FSS147			("FAILURE- AUTH ERROR " , "022" , "Failed at Acquirer" , "FAILURE- AUTH ERROR "),
	FSS148			("FAILURE-3D Secure Verification failed" , "004" , "Declined" , "FAILURE-3D Secure Verification failed"),
	FSS149			("FAILURE-APB Account Not present" , "022" , "Failed at Acquirer" , "FAILURE-APB Account Not present"),
	FSS150			("FAILURE-CM90004-CM90004-Duplicate Record" , "004" , "Declined" , "FAILURE-CM90004-CM90004-Duplicate Record"),
	FSS151			("FAILURE-GW00881-Missing card holder name." , "004" , "Declined" , "FAILURE-GW00881-Missing card holder name."),
	FSS152			("FAILURE-Inactive APB Account Status" , "022" , "Failed at Acquirer" , "FAILURE-Inactive APB Account Status"),
	FSS153			("FAILURE-Internal Server Error-Problem occured while verifying amazon signature" , "022" , "Failed at Acquirer" , "FAILURE-Internal Server Error-Problem occured while verifying amazon signature"),
	FSS154			("FAILURE-IPAY0100048 - CANCELLED-CANCELLED" , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100048 - CANCELLED-CANCELLED"),
	FSS155			("FAILURE-IPAY0100207 - Bin range not enabled" , "004" , "Declined" , "FAILURE-IPAY0100207 - Bin range not enabled"),
	FSS156			("FAILURE-IPAY0100264 - Signature validation failed." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0100264 - Signature validation failed."),
	FSS157			("FAILURE-Loadable balance is Low" , "004" , "Declined" , "FAILURE-Loadable balance is Low"),
	FSS158			("FAILURE-Merchant time out exceeded" , "022" , "Failed at Acquirer" , "FAILURE-Merchant time out exceeded"),
	FSS159			("FAILURE-OVD is not done" , "022" , "Failed at Acquirer" , "FAILURE-OVD is not done"),
	FSS160			("FAILURE-PY20010-PY20010-Problem occurred during merchant response.-PY20010-Problem occurred during merchant response." , "022" , "Failed at Acquirer" , "FAILURE-PY20010-PY20010-Problem occurred during merchant response.-PY20010-Problem occurred during merchant response."),
	FSS161			("FAILURE-Txn Failed" , "022" , "Failed at Acquirer" , "FAILURE-Txn Failed"),
	FSS162			("!ERROR!-GW00160-Bin range not enabled." , "004" , "Declined" , "!ERROR!-GW00160-Bin range not enabled."),
	FSS163			("Card number not registered" , "004" , "Declined" , "Cardnumber not registered"),
	FSS164			("FAILURE - format error" , "022" , "Failed at Acquirer" , "FAILURE - format error"),
	FSS165			("FAILURE - IPAY0100207 - Bin range not enabled." , "004" , "Declined" , "FAILURE - IPAY0100207 - Bin range not enabled."),
	FSS166			("FAILURE-Aggregator is down" , "022" , "Failed at Acquirer" , "FAILURE-Aggregator is down"),
	FSS167			("FAILURE-CM00030-Problem occured while getting external connection details." , "022" , "Failed at Acquirer" , "FAILURE-CM00030-Problem occured while getting external connection details."),
	FSS168			("FAILURE--CM90002-Problem occurred while Parsing the PG response" , "022" , "Failed at Acquirer" , "FAILURE--CM90002-Problem occurred while Parsing the PG response"),
	FSS169			("FAILURE-DENIED BY RISK" , "002" , "Denied by risk" , "FAILURE-DENIED BY RISK"),
	FSS170			("FAILURE-GW02016-!ERROR!-PAYMENT_ID_EXPIRED:GW02016-PaymentId Expired." , "022" , "Failed at Acquirer" , "FAILURE-GW02016-!ERROR!-PAYMENT_ID_EXPIRED:GW02016-PaymentId Expired."),
	FSS171			("FAILURE-Internal Server Error" , "022" , "Failed at Acquirer" , "FAILURE-Internal Server Error"),
	FSS172			("FAILURE-IPAY0101265 - enstage response validation failed." , "022" , "Failed at Acquirer" , "FAILURE-IPAY0101265 - enstage response validation failed."),
	FSS173			("FAILURE-Problem occurred while connecting PG" , "022" , "Failed at Acquirer" , "FAILURE-Problem occurred while connecting PG"),
	FSS174			("FAILURE-PY20010-PY20010-Problem occurred during merchant response." , "022" , "Failed at Acquirer" , "FAILURE-PY20010-PY20010-Problem occurred during merchant response."),
	FSS175			("FAILURE-Refund Not Supported For Axis Netbanking" , "004" , "Declined" , "FAILURE-Refund Not Supported For Axis Netbanking"),
	FSS176			("FAILURE-Top Up is disabled" , "004" , "Declined" , "FAILURE-Top Up is disabled"),
	FSS177			("IPAY0100049-Transaction declined due to exceeding OTP attempts" , "004" , "Declined" , "IPAY0100049-Transaction declined due to exceeding OTP attempts"),
	FSS178			("IPAY0100049-Transaction Declined Due To Exceeding OTP Resend Attempts","004","Declined","IPAY0100049-Transaction Declined Due To Exceeding OTP Resend Attempts"),
	FSS179			("IPAY0100146-Problem occurred while encrypting PIN","004","Declined","IPAY0100146-Problem occurred while encrypting PIN"),
	FSS180			("Problem occured while generating otp","022","Failed at Acquirer","Problem occured while generating otp");
	
		
	private FssPayResultType(String bankCode, String iPayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.iPayCode = iPayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static FssPayResultType getInstanceFromName(String code) {
		FssPayResultType[] statusTypes = FssPayResultType.values();
		for (FssPayResultType statusType : statusTypes) {
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