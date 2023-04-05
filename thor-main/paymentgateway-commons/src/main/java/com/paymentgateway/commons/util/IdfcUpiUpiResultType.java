package com.paymentgateway.commons.util;

/**
 * @author VJ
 *
 */

public enum IdfcUpiUpiResultType {
	
	MERCHANT_NOT_REACHABLE                               ("X7", "022", "Failed at Acquirer",  "MERCHANT NOT REACHABLE"),
	ACCOUNT_DOES_NOT_EXIST_REMITTER                      ("XH", "004",  "Declined",           "ACCOUNT DOES NOT EXIST REMITTER"),
	ACCOUNT_DOES_NOT_EXIST_BENEFICIARY                   ("XI", "004",  "Declined",           "ACCOUNT DOES NOT EXIST BENEFICIARY"),
	LOST_OR_STOLEN_CARD_BENEFICIARY                      ("YB", "022",  "Failed at Acquirer", "LOST OR STOLEN CARD  BENEFICIARY"),
	DO_NOT_HONOUR_REMITTER                               ("YC", "004",  "Declined",           "DO NOT HONOUR  REMITTER "),
	DO_NOT_HONOUR_BENEFICIARY                            ("YD", "004",  "Declined",           "DO NOT HONOUR  BENEFICIARY"),
    INVALID_BENEFICIARY_CREDENTIALS                      ("Z5", "004",   "Declined",          "INVALID BENEFICIARY CREDENTIALS"),
    INSUFFICIENT_FUNDS_IN_CUSTOMER_REMITTER_ACCOUNT      ("Z9", "004",   "Declined",          "INSUFFICIENT FUNDS IN CUSTOMER  REMITTER ACCOUNT"),
    INVALID_MPIN                                         ("ZM", "004",   "Declined",          "INVALID MPIN"),
    REVERSAL_FAILURE                                     ("96", "022",   "Failed at Acquirer", "REVERSAL FAILURE"),
    ORIGINAL_CREDIT_NOT_FOUND                            ("OC", "022",   "Failed at Acquirer", "ORIGINAL CREDIT NOT FOUND"),
    ORIGINAL_DEBIT_NOT_FOUND                             ("OD", "022",   "Failed at Acquirer", "ORIGINAL DEBIT NOT FOUND"),
    CREDIT_NOT_DONE                                      ("NC", "022",   "Failed at Acquirer", "CREDIT NOT DONE"),
    DEBIT_NOT_DONE                                       ("ND", "022",   "Failed at Acquirer",  "DEBIT NOT DONE"),
    EXPIRED_VIRTUAL_ADDRESS                              ("UX", "004",    "Declined",           "EXPIRED VIRTUAL ADDRESS"),
    TRANSACTION_DECLINED_BY_CUSTOMER                     ("ZA", "010",   "Cancelled",           "TRANSACTION DECLINED BY CUSTOMER"),
    REQUEST_AUTHORISATION_IS_DECLINED                    ("U19","004",   "Declined",            "REQUEST AUTHORISATION IS DECLINED"),
    DEBIT_HAS_BEEN_FAILED                                ("U30", "022",  "Failed at Acquirer",  "DEBIT HAS BEEN FAILED"),
    BENEFICIARY_BANK_OFFLINE                             ("U78", "022",  "Failed at Acquirer",  "BENEFICIARY BANK OFFLINE"),
	BANK_INTERNAL_ERROR                                  ("MC06","003" , "Timed out at Acquirer","REQUEST NOT RECEIVED BY SERVER"),
	MERCHANT_NOT_FOUND                                   ("MC02", "004",  "Rejected",             "Merchant not found"),
	IDFCUPI_FAILURE_RES_042                              ("042", "004",  "Rejected",             "Don't live in the past. Please enter a valid expiry date/ time."),
	IDFCUPI_FAILURE_RES_016                              ("016", "004",  "Rejected",             "That's an unregistered mobile number. You'll need to create your profile to proceed further."),
	COLLECT_FAILURE_RES                                  ("154", "004",  "Rejected",             "Duplicate TransactionId"),
	REFUND_FAILURE_RES                                   ("511", "002",  "Denied due to fraud",    "Invalid Original Transaction ID."),
	IDFCUPI_FAILURE_RES_U17                              ("U17", "022",  "Failed at Acquirer",    "PSP is not registered\",\"TimeStamp"),
	IDFCUPI_FAILURE_RES_410                              ("410", "022",  "Failed at Acquirer",    "Invalid Payer/Payee handle."),
	COLLECT_EXPIRED			                             ("U69", "004",  "Rejected",    "Collect Expired"),
	AUTH_FAILED                                          ("205", "002",   "Denied due to fraud",   "Sorry! Authentication failed.");

	
	
	private final String bankCode;
	private final String paymentGatewayCode;
	private final String message;
	private final String statusName;
	
	
	private IdfcUpiUpiResultType(String bankCode, String paymentGatewayCode, String statusName, String message) {
		this.bankCode = bankCode;
		this.paymentGatewayCode = paymentGatewayCode;
		this.statusName = statusName;
		this.message = message;
	}
	
	public static IdfcUpiUpiResultType getInstanceFromName(String code) {
		IdfcUpiUpiResultType[] statusTypes = IdfcUpiUpiResultType.values();
		for (IdfcUpiUpiResultType statusType : statusTypes) {
			if (String.valueOf(statusType.getBankCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}
	
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
