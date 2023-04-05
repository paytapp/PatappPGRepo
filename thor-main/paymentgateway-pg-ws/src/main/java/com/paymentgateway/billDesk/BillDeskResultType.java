package com.paymentgateway.billDesk;


public enum BillDeskResultType {

	BILLDESK001			("0300" , "000" , "Captured" , "Success"),
	BILLDESK002			("0399" , "004" , "Declined" , "Failure"),
	BILLDESK003			("NA" , "021" , "Invalid" , "Txn not found/ Invalid checksum/ Invalid Request IP"),
	BILLDESK004			("0002" , "022" , "Failed at Acquirer" , "Pending/Abandoned"),
	BILLDESK005			("0001" , "022" , "Failed at Acquirer" , "Error at BillDesk"),
	BILLDESK006			("ERR_REF001" , "022" , "Failed at Acquirer" , "Transaction not found"),
	BILLDESK007			("ERR_REF002" , "021" , "Invalid" , "Invalid MerchantRefNo"),
	BILLDESK008			("ERR_REF003" , "021" , "Invalid" , "Invalid MerchantID"),
	BILLDESK009			("ERR_REF004" , "021" , "Invalid" , "Invalid CustomerID"),
	BILLDESK010			("ERR_REF005" , "004" , "Declined" , "Transaction is not successful"),
	BILLDESK011			("ERR_REF006" , "004" , "Declined" , "Invalid refund amount"),	
	BILLDESK012			("ERR_REF007" , "004" , "Declined" , "Invalid transaction date"),
	BILLDESK013			("ERR_REF008" , "004" , "Declined" , "Refund amount is greater than transaction amount"),
	BILLDESK014			("ERR_REF009" , "004" , "Declined" , "Cancel request already received"),
	BILLDESK015			("ERR_REF010" , "004" , "Declined" , "Refund amount greater than transaction amount"),
	BILLDESK016			("ERR_REF011" , "012" , "Denied by risk" , "Invalid source"),
	BILLDESK017			("ERR_REF012" , "012" , "Denied by risk" , "Invalid checksum"),
	BILLDESK018			("ERR_REF013" , "004" , "Declined" , "Cannot process request right now. Duplicate request"),
	BILLDESK019			("ERR_REF014" , "004" , "Declined" , "Invalid message"),
	BILLDESK020			("ERR_REF015" , "022" , "Failed at Acquirer" , "Processing error"),
	BILLDESK021			("ERROR_MSG001" , "004" , "Declined" , "Invalid message code"),
	BILLDESK022			("ERREXCEPTION1001 " , "022" , "Failed at Acquirer" , "Internal server error."),
	BILLDESK023			("ERREXCEPTION1002" , "022" , "Failed at Acquirer" , "Internal server error."),
	BILLDESK024			("ERREXCEPTION1003" , "022" , "Failed at Acquirer" , "Internal server error."),
	BILLDESK025			("ERREXCEPTION1004" , "022" , "Failed at Acquirer" , "Internal server error."),
	BILLDESK026			("HASHFAILED" , "012" , "Denied by risk" , "HASH Mismatch");
	
	
	private BillDeskResultType(String bankCode, String paymentGatewayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.paymentGatewayCode = paymentGatewayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static BillDeskResultType getInstanceFromName(String code) {
		BillDeskResultType[] statusTypes = BillDeskResultType.values();
		for (BillDeskResultType statusType : statusTypes) {
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

	public String getPaymentGatewayCode() {
		return paymentGatewayCode;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}
}