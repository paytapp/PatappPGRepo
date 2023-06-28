package com.paymentgateway.payu;

public enum PayuResultType {

	
	PAYU01           ("E001" ,  "004","Declined" , "Invalid Offer."),
	PAYU02           ("E002" ,  "004","Declined" , "Invalid Payment Method."),
	PAYU03           ("E000" ,  "000","Success" , "NO_ERROR"),
	PAYU04           ("0" , 	"007","Rejected" , "Refund Failed."),
	PAYU05           ("1" ,  	"000","Success" , "Refund Request Queued/Success."),
	PAYU07           ("E201" ,  "007","Rejected" , "BRAND_INVALID"),
	PAYU08           ("E202" ,  "004","Declined" , "TRANSACTION_INVALID"),
	PAYU09           ("E205" ,  "007","Rejected" , "CURL_ERROR_ENROLLED"),
	PAYU010          ("E206" ,  "007","Rejected" , "CUTOFF_ERROR"),
	PAYU011          ("E207" ,  "007","Rejected" , "INVALID_TRANSACTION_TYPE"),
	PAYU012          ("E208" ,  "007","Rejected" , "BANK_SERVER_ERROR"),
	PAYU013          ("E209" ,  "007","Rejected" , "NO_BANK_RESPONSE"),
	PAYU014          ("E210" ,  "007","Rejected" , "COMMUNICATION_ERROR"),
	PAYU015          ("E211" ,  "007","Rejected" , "NETWORK_ERROR"),
	PAYU016          ("E214" ,  "007","Rejected" , "CURL_CALL_FAILURE"),
	PAYU017          ("E216" ,  "007","Rejected" , "BATCH_ERROR"),
	PAYU018          ("E217" ,  "007","Rejected" , "TRANPORTAL_ID_ERROR"),
	PAYU019          ("E218" ,  "007","Rejected" , "CARD_ISSUER_TIMED_OUT"),
	PAYU020          ("E219" ,  "007","Rejected" , "INCOMPLETE_BANK_RESPONSE"),
	PAYU021          ("E300" ,  "004","Declined" , "SECURE_3D_PASSWORD_ERROR"),
	PAYU022          ("E301" ,  "004","Declined" , "SECURE_3D_INCORRECT"),
	PAYU023          ("E302" ,  "004","Declined" , "SECURE_3D_CANCELLED"),
	PAYU024          ("E303" ,  "004","Declined" , "AUTHENTICATION_ERROR"),
	PAYU025          ("E304" ,  "004","Declined" , "ADDRESS_INVALID"),
	PAYU026          ("E305" ,  "004","Declined" , "CARD_NUMBER_INVALID"),
	PAYU027          ("E306" ,  "007","Rejected" , "TRANSACTION_INVALID_PG"),
	PAYU028          ("E307" ,  "007","Rejected" , "RISK_DENIED_PG"),
	PAYU029          ("E308" ,  "007","Rejected" , "TRANSACTION_FAILED"),
	PAYU030          ("E309" ,  "007","Rejected" , "SYSTEM_ERROR_PG"),
	PAYU031          ("E310" ,  "004","Declined" , "LOST_CARD"),
	PAYU032          ("E311" ,  "004","Declined" , "EXPIRED_CARD"),
	PAYU033          ("E312" ,  "007","Rejected" , "BANK_DENIED"),
	PAYU034          ("E313" ,  "004","Declined" , "CVC_FAILURE"),
	PAYU035          ("E314" ,  "007","Rejected" , "ADDRESS_FAILURE"),
	PAYU036          ("E315" ,  "004","Declined" , "CVC_ADDRESS_FAILURE"),
	PAYU037          ("E316" ,  "004","Declined" , "SECURE_3D_NOT_ENROLLED"),
	PAYU038          ("E317" ,  "004","Declined" , "SECURE_3D_AUTHENTICATION_ERROR"),
	PAYU039          ("E318" ,  "004","Declined" , "SECURE_3D_NOT_SUPPORTED"),
	PAYU040          ("E319" ,  "004","Declined" , "SECURE_3D_FORMAT_ERROR"),
	PAYU041          ("E320" ,  "004","Declined" , "SECURE_3D_SIGNATURE_ERROR"),
	PAYU042          ("E321" ,  "004","Declined" , "SECURE_3D_SERVER_ERROR"),
	PAYU043          ("E322" ,  "004","Declined" , "SECURE_3D_CARD_TYPE"),
	PAYU044          ("E323" ,  "004","Declined" , "INVALID_EXPIRY_DATE"),
	PAYU045          ("E324" ,  "007","Rejected" , "CARD_FRAUD_SUSPECTED"),
	PAYU046          ("E325" ,  "007","Rejected" , "RESTRICTED_CARD"),
	PAYU047          ("E326" ,  "004","Declined" , "PASSWORD_ERROR"),
	PAYU048          ("E327" ,  "004","Declined" , "INVALID_LOGIN"),
	PAYU049          ("success","000","Success" , "TRANSACTION_SUCCESSFUL"),
	PAYU050          ("E328" ,  "007","Rejected" , "PARAMETERS_MISMATCH"),
	PAYU051          ("E329" ,  "004","Declined" , "ISSUER_DECLINED_LOW_FUNDS"),
	PAYU052          ("E330" ,  "004","Declined" , "PAYMENT_GATEWAY_VALIDATION_FAILURE"),
	PAYU053          ("E331" ,  "004","Declined" , "INVALID_EMAIL_ID"),
	PAYU054          ("E332" ,  "004","Declined" , "INVALID_FAX"),
	PAYU055          ("E333" ,  "004","Declined" , "INVALID_CONTACT"),
	PAYU056          ("E334" ,  "007","Rejected" , "AUTHENTICATION_SERVICE_UNAVAILABLE"),
	PAYU057          ("E335" ,  "007","Rejected" , "AUTHENTICATION_INCOMPLETE"),
	PAYU058          ("E336" ,  "004","Declined" , "EXPIRY_DATE_LOW_FUNDS"),
	PAYU059          ("E337" ,  "007","Rejected" , "NOT_CAPTURED"),
	PAYU060          ("E338" ,  "007","Rejected" , "RISK_RULE_FAILED"),
	PAYU062          ("E500" ,  "007","Rejected" , "UNKNOWN_ERROR_PG"),
	PAYU061          ("E501" ,  "007","Rejected" , "BANK_WAS_UNABLE_TO_AUTHENTICATE"),
	PAYU063          ("E502" ,  "004","Declined" , "TRANSACTION_ABORTED"),
	PAYU064          ("E504" ,  "007","Rejected" , "DUPLICATE_TRANSACTION"),
	PAYU065          ("E505" ,  "007","Rejected" , "AWAITING_PROCESSING"),
	PAYU066          ("E600" ,  "007","Rejected" , "PAYU_API_ERROR"),
	PAYU067          ("E700" ,  "007","Rejected" , "SECURE_HASH_FAILURE"),
	PAYU068          ("E702" ,  "007","Rejected" , "AMOUNT_DIFFERENCE"),
	PAYU069          ("E703" ,  "007","Rejected" , "TRANSACTION_NUMBER_ERROR"),
	PAYU070          ("E704" ,  "007","Rejected" , "RECEIPT_NUMBER_ERROR"),
	PAYU071          ("E705" ,  "004","Declined" , "USER_PROFILE_SETTINGS_ERROR"),
	PAYU072          ("E706" ,  "004","Declined" , "INSUFFICIENT_FUNDS"),
	PAYU073          ("E707" ,  "004","Declined" , "INVALID_PAN"),
	PAYU074          ("E708" ,  "004","Declined" , "PIN_RETRIES_EXCEEDED"),
	PAYU075          ("E709" ,  "004","Declined" , "INVALID_CARD_NAME"),
	PAYU076          ("E710" ,  "004","Declined" , "INVALID_PIN"),
	PAYU077          ("E711" ,  "004","Declined" , "INVALID_USER_DEFINED_DATA"),
	PAYU078          ("E712" ,  "004","Declined" , "INCOMPLETE_DATA"),
	PAYU079          ("E713" ,  "004","Declined" , "INSUFFICIENT_FUNDS_EXPIRY_INVALID"),
	PAYU080          ("E714" ,  "004","Declined" , "INVALID_ZIP"),
	PAYU081          ("E715" ,  "004","Declined" , "INVALID_AMOUNT"),
	PAYU082          ("E717" ,  "004","Declined" , "INVALID_ACCOUNT_NUMBER"),
	PAYU083          ("E718" ,  "004","Declined" , "INSUFFICIENT_FUNDS_INVALID_CVV"),
	PAYU084          ("E719" ,  "004","Declined" , "INSUFFICIENT_FUNDS_AUTHENTICATION_FAILURE"),
	PAYU086          ("E720" ,  "004","Declined" , "MAX_AMOUNT_EXCEEDED_FOR_PAYMENT_TYPE"),
	PAYU087          ("E800" ,  "007","Rejected" , "PREFERED_GATEWAY_NOT_SET"),
	PAYU088          ("E801" ,  "007","Rejected" , "NETBANKING_GATEWAY_DOWN"),
	PAYU089          ("E802" ,  "007","Rejected" , "CC_DC_ISSUING_BANK_DOWN"),
	PAYU090          ("E803" ,  "007","Rejected" , "NO_ELIGIBLE_PG"),
	PAYU091          ("E804" ,  "007","Rejected" , "DISABLE_PG_NOT_AVAILABLE_HANDLING"),
	PAYU092          ("E901" ,  "007","Rejected" , "RETRY_LIMIT_EXCEEDED"),
	PAYU093          ("E902" ,  "004","Declined" , "INVALID_CARD_TYPE"),
	PAYU094          ("E903" ,  "004","Declined" , "INTERNATIONAL_CARD_NOT_ALLOWED"),
	PAYU096          ("E905" ,  "004","Declined" , "USER_DECLINED"),
	PAYU097          ("E906" ,  "004","Declined" , "TIMER_EXPIRED"),
	PAYU098          ("E907" ,  "007","Rejected" , "WRONG_PAYMENT_METHOD"),
	PAYU099          ("E908" ,  "007","Rejected" , "UNKNOWN_BINS_NO_ACTIVE_PG_ASSIGNED"),
	PAYU0100         ("E1000" ,  "007","Declined" , "SECURE_3D_AUTHENTICATION_ERROR_S3A"),
	PAYU0101         ("E1001" ,  "007","Rejected" , "AUTHENTICATION_SERVICE_UNAVAILABLE_ASU"),
	PAYU0102         ("E1020" ,  "007","Rejected" , "INVALID_TRANSACTION_ID"),
	PAYU0103         ("E1101" ,  "007","Rejected" , "TXN_DETAIL_INVALID_REDIRECTING_TO_MERCHANT"),
	PAYU0104         ("E1201" ,  "007","Rejected" , "SERVICE_AUTHORIZATION_ERROR"),
	PAYU0105         ("E1202" ,  "007","Rejected" , "FACILITY_UNAVAILABLE"),
	PAYU0106         ("E1203" ,  "007","Rejected" , "LIMIT_EXCEED"),
	PAYU0107         ("E1204" ,  "007","Rejected" , "NETBANKING_AUTHENTICATION_ERROR"),
	PAYU0108         ("E1205" ,  "007","Rejected" , "REDIRECTED_BY_RETRY_LINK"),
	PAYU0109         ("E1206" ,  "007","Rejected" , "REDIRECTED_BY_BACK_BUTTON"),
	PAYU0110         ("E1208", "007","Rejected" , "INSUFFICIENT_FUNDS_INCORRECT_EXPIRY"),
	PAYU0111         ("100" ,  "000","Success" , "SUCCESS"),
	PAYU0112         ("101" ,  "007","Rejected" , "PENDING"),
	PAYU0113         ("102" ,  "007","Rejected" , "QUEUED"),
	PAYU0114         ("107" ,  "007","Rejected" , "FAILURE - Upgraded to refund"),
	PAYU0115         ("108" ,  "007","Rejected" , "FAILURE"),
	PAYU0116         ("103" ,  "007","Rejected" , "REJECT - Request rejected on reconfirmation"),
	PAYU0117         ("104" ,  "007","Rejected" , "RECONFIRM - Confirmation required"),
	PAYU0118         ("105" ,  "007","Rejected" , "FAILURE - Invalid amount"),
	PAYU0119         ("106" ,  "007","Rejected" , "FAILURE - Token already exists."),
	PAYU0120         ("109" ,  "007","Rejected" , "FAILURE - Request is already logged"),
	PAYU0121         ("110" ,  "007","Rejected" , "FAILURE - More than one partial refund of Maestro transactions are not allowed"),
	PAYU0122         ("111" ,  "007","Rejected" , "FAILURE - Invalid transaction status"),
	PAYU0123         ("115" ,  "007","Rejected" , "FAILURE - Invalid status to be updated"),
	PAYU0124         ("116" ,  "007","Rejected" , "FAILURE - Transaction Not Found"),
	PAYU0125         ("117" ,  "007","Rejected" , "FAILURE - Amount Does not Match"),
	PAYU0126         ("119" ,  "007","Rejected" , "FAILURE - No such Request Found"),
	PAYU0127         ("120" ,  "007","Rejected" , "FAILURE - Transaction lock could not be obtained."),
	PAYU0128         ("123" ,  "007","Rejected" , "FAILURE - Request set as pending - requires manual follow-up"),
	PAYU0129         ("225" ,  "007","Rejected" , "PENDING - Overdraft has occurred. Kindly recheck the status tomorrow."),
	PAYU0130         ("226" ,  "007","Rejected" , "PENDING - Capture has been initiated today. Please check for refund status tomorrow."),
	PAYU0131         ("501" ,  "000","Success"  , "Successfully Updated"),
	PAYU0132         ("502" ,  "007","Rejected" , "Failed to update"),
	PAYU0133         ("301" ,  "007","Rejected" , "FAILURE - Capture already successful for this transaction"),
	PAYU0134         ("302" ,  "007","Rejected" , "FAILURE - Please try after some time"),
	PAYU0135         ("424" ,  "007","Rejected" , "FAILURE - Transaction upgraded to capture/refund."),
	PAYU0136         ("500" ,  "007","Rejected" , "FAILURE - Some Exception Occured."),
	PAYU0137         ("E910" ,  "007","Rejected" , "FAILURE - EMI is not supported on this card "),
	PAYU0138         ("E1900" ,  "007","Rejected" , "Unable to open intent, switched to collect"),
	PAYU0139         ("E507" ,  "007","Rejected" , "Collect expired|Completed Using Callback"),
	PAYU0140         ("E408" ,  "007","Rejected" , "Marked bounced as transaction has timed out"),
	PAYU0141         ("E1605" ,  "004","Declined" , "Cancelled by user"),
	;
	
	
	private PayuResultType(String bankCode, String iPayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.iPayCode = iPayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static PayuResultType getInstanceFromName(String code) {
		PayuResultType[] statusTypes = PayuResultType.values();
		for (PayuResultType statusType : statusTypes) {
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