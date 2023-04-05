package com.paymentgateway.commons.exception;

public enum ErrorType {
	//check 021, 022, 023
	SUCCESS 						("000", "SUCCESS"),
	ACQUIRER_ERROR					("001", "Acquirer Error"),
	DENIED							("002", "Denied"),
	TIMEOUT							("003", "Timeout"),
	DECLINED						("004", "Declined"),
	AUTHENTICATION_UNAVAILABLE 		("005", "Authentication not available"),
	PROCESSING						("006", "Transaction processing"),
	REJECTED						("007", "Rejected by acquirer"),
	DUPLICATE						("008", "Duplicate"),
	SIGNATURE_MISMATCH				("009","Response signature did not match"),
	CANCELLED						("010", "Cancelled by user"),	
	RECURRING_PAYMENT_UNSUCCESSFULL	("011", "Authorization success but error processing recurring payment"),
	DENIED_BY_FRAUD					("012", "Denied due to fraud detection"),
	REFUND_REJECTED					("013","Total refund amount greater than sale amount"),
	REFUND_DENIED 					("014","Total Refund Amount should be less than today's Captured Amount "),
	TRANSACTION_NOT_FOUND			("015", "Transaction not found"),
	REFUND_FLAG_AMOUNT_NOT_MATCH 	("016", "In case of Full Refund, Refund Amount shall be equal to the Sale Amount"),
	GOOGLEPAY_SERVER_DOWN 	        ("017", "In case if token is not generated from GPay server"),
	DUPLICATE_ORDER_ID				("018", "Duplicate Order ID"),		
	DUPLICATE_REFUND_ORDER_ID		("019", "Duplicate refund order Id"),
	DECLINED_BY_INSUFFICIENT_BALANCE("020", "Declined due to insufficient balance"),	
	INVALID_ACTIVITY				("021", "Invalid at acquirer"),
	FAILED							("022", "Failed at acquirer"),
	ENROLLED						("023", "The cardholder is enrolled in Payer Authentication"),
	CARD_NOT_ENROLLED				("024", "Card is not enrolled"),
	SURCHARGE_NOT_SET 				("025",	"Unable to fetch surcharge details"),
	DUPLICATE_SUBMISSION   			("027", "Duplicate submission on same order ID"), 
	PAYMENT_OPTIONS_NOT_CONFIGURED  ("028", "No payment options configured for the merchant"),
	REJECTED_BY_PG					("029", "Transaction rejected by payment gateway"),
	AUTHENTICATION_FAILED 			("030", "Authentication Failed"),
	REDIRECT_UPI					("031", "REDIRECT UPI response code"),
	PENDING							("032", "Pending"),
	NO_TRANSACTION_AVAILABLE		("033", "No Transaction Available"),
	PRODUCT_PRICE_SUM_NOT_EQUAL		("034", "Sum of Product prices and Total Amount Should be Equal"),
	
	
	USER_NOT_FOUND 					("100", "100", "Invalid Login Details", "Invalid Login Details!"),
	USER_PIN_INCORRECT 				("101", "100", "Invalid Login Details", "Invalid Login Details!" ),
	USER_INACTIVE 					("102", "100", "User inactive", "User is inactive!" ),
	INVALID_INPUT 					("103", "100", "Validation failed", "Incorrect User ID or Password!" ),
	NOT_APPROVED_FROM_ACQUIRER		("104", "105", "User not approved from any acquirer", "User not approved from any acquirer" ),
	ACQUIRER_NOT_FOUND				("105","106","Account not present for this acquirer","Account not present for this acquirer"),
	MAPPING_NOT_FETCHED				("105","107","Unable to fetch mapping from database","Unable to fetch mapping from database"),	
	MAPPING_NOT_SAVED	    		("106","108","Unable to save merchant mapping.","Unable to save merchant mapping."),	
	MAPPING_SAVED		   	 		("107","109","Request Accpted! Mapping Saved Successfully!","Request Accpted! Mapping Saved Successfully!"),	
	CHARGINGDETAIL_NOT_FETCHED		("108","110","Merchant detail not present for this acquirer","Mapping detail not present for this acquirer"),
	CHARGINGDETAIL_NOT_SAVED		("109","111","Charging detail not saved to database","Charging detail not saved to database"),
	CURRENCY_NOT_MAPPED	    		("110","112","Merchant not mapped for this currency","Merchant not mapped for this currency"),
	SUCCESSFULLY_SAVED     			("111","Details Successfully Saved"),
	USER_PASSWORD_NOT_SET   		("112", "100", "Password not set", "Incorrect User ID or Password!" ),
	OTP_NOT_SET   		            ("797", "797", "OTP not set", "Invalid OTP!" ),
	PAYMENT_OPTION_NOT_SUPPORTED    ("113", "Payment option not supported!!"),
	GST_DEATILS_NOT_AVAILABLE       ("114", "GST Details Not Set!!"),
	MAPPING_SAVED_FOR_APPROVAL		("115", "Mapping Request saved for approval"),
	MAPPING_REQUEST_ALREADY_PENDING	("116", "Mapping Request already pending for this merchant"),
	MAPPING_REQUEST_APPROVAL_DENIED	("117", "Mapping Request cannot be approved as user is not permitted"),
	MAPPING_REQUEST_NOT_FOUND		("118", "Mapping Request not found"),
	MAPPING_REQUEST_REJECTED		("119", "Mapping Request has been rejected"),
	SERVICE_TAX_DETAILS_SAVED		("120", "GST Details Saved!!"),
	SERVICE_TAX_REQUEST_ALREADY_PENDING		("121", "Service Tax Request already pending for this business type!!"),
	SERVICE_TAX_REQUEST_SAVED_FOR_APPROVAL		("122", "Service Tax Details Saved for approval!!"),
	MERCHANT_SURCHARGE_REQUEST_ALREADY_PENDING ("123", "Merchant Surcharge update request already pending !!"),
	MERCHANT_SURCHARGE_UPDATED ("124", "Merchant Surcharge updated !!"),
	MERCHANT_SURCHARGE_REQUEST_SENT_FOR_APPROVAL ("125", "Merchant Surcharge request saved for approval !!"),
	BANK_SURCHARGE_REQUEST_SENT_FOR_APPROVAL ("126", "Bank Surcharge request saved for approval !!"),
	BANK_SURCHARGE_UPDATED ("127", "Bank Surcharge updated !!"),
	BANK_SURCHARGE_REQUEST_ALREADY_PENDING ("128", "Bank Surcharge update request already pending !!"),
	BANK_SURCHARGE_REJECTED ("128", "Bank Surcharge request rejected !!"),
	TDR_REQUEST_ALREADY_PENDING ("129", "TDR request is already pending for this merchant !!"),
	CURRENCY_NOT_SUPPORTED    ("130", "Currency not supported!!"),
	CARD_NUMBER_NOT_SUPPORTED    ("131", "Unsupported card number"),
	ROUTER_CONFIGURATION_REQUEST_ALREADY_PENDING ("132", "Router Configuration update request already pending !!"),
	ROUTER_CONFIGURATION_REQUEST_SENT_FOR_APPROVAL ("133", "Router Configuration request saved for approval !!"),
	ROUTER_CONFIGURATION_REQUEST_ACCEPT	("134", "Router Configuration request Accepted and Router Configuration Saved Successfully !!"),
	ROUTER_CONFIGURATION_REQUEST_REJECT	("135", "Router Configuration request Rejected !!"),
	MERCHANT_ACCOUNT_EDIT_REQUEST_ACCEPT ("136", "Merchant Account edit Request Accepted Successfully and changes saved Successfully !!"),
	MERCHANT_ACCOUNT_EDIT_REQUEST_REJECT ("137", "Merchant Account edit Request Rejected !!"),
	CHARGING_DETAILS_REQUEST_ACCEPT ("136", "Charging details Request Accepted Successfully and changes saved Successfully !!"),
	CHARGING_DETAILS_REQUEST_REJECT ("137", "Charging details Request Rejected !!"),
	MERCHANT_REQUEST_ACCEPT	("138", "Merchant Request Accepted Successfully and saved Successfully"),
	MERCHANT_REQUEST_REJECT	("139", "Merchant Request Reject !!"),
	BULK_USER_REQUEST_ACCEPT ("136", "Bulk User Request Accepted Successfully and saved Successfully !!"),
	BULK_USER_REQUEST_REJECT ("137", "Bulk User Request Rejected !!"),
	BULK_USER_REQUEST_APPROVAL ("140", "Bulk User Request Successfully saved for Approval !!"),
	CHARGING_DETAILS_REQUEST_APPROVAL("141", "Charging details request Saved and Pending for Approval!"),
	ROUTER_CONFIGURATION_SAVE ("142", "Router Configuration updated successfully !!"),
	USER_ACCOUNT_LOCKED 	("143", "143", "Account Locked, login by OTP to Continue", "Account Locked, login by OTP to Continue" ),
	INVALID_PAYOUT_PURPOSE ("144", "Invalid Payout Purpose"),
	USER_ALREADY_EXISTS("145", "User Already Exists"),
	//User transaction operations
	ALREADY_CAPTURED_TRANSACTION 	("200","This transaction is already settled with Transaction Id: "),
	CAPTURE_SUCCESSFULL 			("201","Capture processed successfully order ID: "),
	CAPTURE_NOT_SUCCESSFULL 		("202","Capture not processed successfully for order ID: "),
	REFUND_SUCCESSFULL 				("203","Refund processed successfully order ID: "),
	REFUND_NOT_SUCCESSFULL 			("204","Refund not processed successfully order ID: "),
	VOID_NOT_SUCCESSFULL 			("205","Void not processed successfully order ID: "),
	VOID_SUCCESSFULL 				("206","Transaction void processed successfully order ID: "),
	REFUND_FAILED 					("207","Refund Amount should be less than today's Captured Amount "),
	REFUND_AMOUNT_MISMATCH 			("208","Refund request amount should be equal to sale amount for total refund"),
	SETTLEMENT_SUCCESSFULL 			("351","Settlement processed successfully order ID: "),
	SETTLEMENT_NOT_SUCCESSFULL 		("352","Settlement not processed successfully order ID: "),

	
	VALIDATION_FAILED		("300", "Invalid Request" ),
	BLACKLISTED_IP			("301", "300", "Blacklisted IP address", "Invalid Request"),
	NO_SUCH_TRANSACTION		("302", "No Such Transaction Found"),
	EMPTY_FIELD             ("303", "This field is mandatory"),
	EMPTY_FIELDS            ("304", "Please provide a valid value for mandatory fields"),
	INVALID_FIELD_VALUE     ("305", "Enter valid value"),
	USER_UNAVAILABLE        ("306", "This Email id is already existing"),
	USER_AVAILABLE          ("307", "USER ID is available proceed to signup!!"),
	PASSWORD_MISMATCH       ("308", "Password mismatch"),
	OLD_PASSWORD_MATCH      ("309", "Use a password which has not been used 4 recent times by you"),
	PASSWORD_CHANGED 	    ("310", "Password changed successfully, login to continue"),
	EMAIL_ERROR 	    	("311", "Error!! Unable to send email Emailer fail"),
	NEW_PASSWORD            ("312", "New password should not be blank!"),
	CONFIRM_NEW_PASSWORD    ("313", "Confirm new password should not be blank!"),
	PASSWORD_RESET 	        ("314", "Password reset successfully, login to continue"),
	ALREADY_PASSWORD_RESET 	("315", "You have already used this link, login to continue"),
	RESET_LINK_SENT         ("316", "Reset password link sent to your email id"),
	ALREADY_VALIDATE_EMAIL 	("317", "You have already validate this link, login to continue"),
	INVALID_EMAIL 			("318", "Please provide valid email to reset your account's password"),
	INVALID_CURRENCY_CODE   ("319","301","Invalid currency code","Invalid Request" ),
	INVALID_AMOUNT          ("320","Invalid Amount"),
	INVALID_ORDER_ID        ("321","Invalid Order ID"),
	INVALID_TXN_TYPE        ("322","Invalid Txn Type"),
	INVALID_HASH            ("323","Invalid Hash"),
	INVALID_PAYID_ATTEMPT   ("324","Request with invalid payId"),
	INVALID_RETURN_URL      ("325","Invalid Request"),
	INVALID_FIELD           ("326","Invalid value"),
	//Remittance 
	ACC_HOLDER_NAME           ("327","Invalid Account Holder Name"),
	ACCOUNT_NO                ("328","Invalid Account Number"),
	BANK_NAME                 ("329","Invalid Bank Name"),
	CURRENCY                  ("330","Invalid Currency"),
	DATE_FROM                 ("331","Invalid Transaction Date"),
	IFSC_CODE                 ("332","Invalid IFSC Code"),
	MERCHANT                  ("333","Invalid Merchant"),
	NET_AMOUNT                ("334","Invalid Net amount"),
	PAY_ID                    ("335","Invalid PayId"),
	STATUS                    ("336","Invalid Status"),
	UTR                       ("337","Invalid UTR"),

	
	//defaultCurrency Error
	INVALID_DEFAULT_CURRENCY	("338","Operation not successful, please try again later!!"),	
	UNMAPPED_CURRENCY_ERROR		("339","No currency mapped !!"),
	
	//fraud prevention sys Errors
			
	FRAUD_RULE_ALREADY_EXISTS	("340", "Fraud rule already exist"),
	FRAUD_RULE_NOT_EXIST		("341", "Fraud rule doesn't exist"),
	FRAUD_RULE_SUCCESS			("342", "Fraud Rule added successfully"),
	FRAUD_RULE_SINGLE_ENTRY_ERROR	("343", "You can add only one rule of this type"),
	COMMON_ERROR				("344", "Something went wrong."),
	
	//ticketing System
	TICKET_SUCCESSFULLY_SAVED	("345","Ticket create successfully!!"),
	COMMENT_SUCCESSFULLY_ADDED	("346","Comment successfully added!!"),
	
	TRANSACTION_FAILED				("347", "Transaction Failed."),
	NOTIFICATION_FAILED				("348", "Notification Failed."),
	
	//Surcharge Module
	SURCHARGEDETAIL_NOT_FETCHED	("351","351","Surcharge detail not present for this payment type","Surcharge detail not present for this payment type"),
	SURCHARGEDETAIL_NOT_SAVED	("352","352","Surcharge details not saved to database","Surcharge details not saved to database"),
	SERVICETAX_NOT_SET 			("353","353","Service tax not set for this industry type","Service tax not set for this industry type"),
	
	SUPER_MERCHANT_UNAVAILABLE   ("399", "This Super Merchant is not present / Inactive"),
	
	PERMISSION_DENIED		("400", "Permission Denied"),
	INTERNAL_SYSTEM_ERROR	("900", "900", "Internal System Error", "Operation could not be completed, please try again later!"),
	CRYPTO_ERROR 			("901", "900", "Crypto Issue", "Operation could not be completed, please try again later!" ),
	DATABASE_ERROR			("902", "900", "Database Error", "Operation could not be completed, please try again later!"),
	UNKNOWN 				("999", "Unknown Error"),
	WrongFormat            ("1000","WrongFormat"),
	
	JMS_EMAIL_ERROR 		("0001", "Jms Email error"),
	
	CSV_NOT_SUCCESSFULLY_UPLOAD	("354","entry not  saved successfully!!"),
	INVALID_EMAIL_ID  			("356", "Enter valid EmailId"),
	VERIFY_EMAIL_ID				("357","We now need to verify your email address"),
	ROUTER_RULES_NOT_UPDATED 	("358", "Rule not updated!!"),
	INVALID_CAPTCHA				("359","Invalid captcha code!!"),
	
	//Smart router 
	ROUTER_RULE_CREATED				("360", "Router rule successfully created"),
	ROUTER_RULE_CREATE_REQUEST_SENT	("370", "Router rule create request successfully sent to Admin"),
	ROUTER_RULE_CREATE_REQUEST_PENDING	("371", "Router rule create request already pending with Admin"),
	ROUTER_CONFIGURATION_ALREADYPENDING	("372", "Router configuration status is pending for this router rule."),
	ROUTER_RULE_UPDATE_DENIED		("373", "Router Rule update denied! No Permissions Found."),
	ROUTER_RULE_UPDATED				("361", "Router rule successfully updated"),
	ROUTER_RULE_UPDATED_PENDING			("373", "Router rule update request sent to Admin"),
	ROUTER_RULE_ALREADY_EXIST		("362", "This rule is already exist"),
	SOME_RULE_ALREADY_EXIST			("363", "Duplicate rule are not created"),
	ROUTER_RULE_NOT_FOUND			("364", "Router rule not available"),
	
	
	
	INVALID_REQUEST_FIELD			("365","Invalid Request fields"),
	INVALID_VPA						("366", "Invalid VPA address"),
	INVALID_KOTAKUPI_VPA            ("U17", "Invalid VPA address"),
	
	//Reco Refund Exeption User Type
	MERCHANT_EXCEPTION       		("367", "Merchant"),
	BANK_EXCEPTION       			("367", "Bank"),
	INACTIVE_OTP 		            ("697", "697", "Invalid OTP", "Invalid OTP!" ),
	EXPIRED_OTP 		            ("797", "797", "OTP has been expired!", "OTP has been expired!" ),
	INVALID_EMAILID 		        ("897", "897", "Invalid EmailId", "Invalid EmailId!"),
	
	ACUIRER_DOWN					("777", "Acquirer down"),
	FRAUD_RULE_UPDATE_SUCCESS		("778", "Fraud Rule Update successfully"),
	
	
	PIN_MISMATCH       				("368", "Incorrect Old PIN"),
	OLD_NEW_PIN_MATCH      			("369", "Use a PIN which has not been used 4 recent times by you"),
	PIN_CHANGED 	    			("370", "PIN changed successfully, login to continue"),
	INVALID_PHONE_NUMBER  			("374", "Enter valid Phone Number"),
	NEW_PIN            				("375", "New password should not be blank!"),
	CONFIRM_NEW_PIN    				("376", "Confirm new PIN can not be blank!"),
	USER_PIN_NOT_SET   				("903", "904", "PIN not set", "PIN not set !" ),
	//USER_PIN_INCORRECT 			("905", "906", "PIN incorrect", "Incorrect User ID or PIN!" ),
	VERIFY_PHONE_NUMBER				("377", "We now need to verify your email Phone Number"),
	ALREADY_PIN_RESET 				("378", "You have already used this link, login to continue"),
	PIN_RESET 	        			("379", "PIN reset successfully, login to continue"),
	USER_PHONE_UNAVAILABLE        	("380", "Phone Number already existing"),
	USER_PHONE_NUMBER_UNAVAILABLE   ("381", "Phone Number does not exist"),
	OLD_PIN_MATCHED   				("382", "Old PIN matched"),
	UNAUTHORIZED	   				("383", "You are not authorized to perform this action !"),
	RESELLER_INACTIVE	   			("384", "Your Account is Inactive. Please connect at support@PaymentGateWay.com for activation "),
	DAILY_LIMIT_EXCCED	   			("385", "Daily Limit Exceeded"),
	ACCESS_DENIED                   ("386", "Access Denied"),
	DUPLICATE_ACCOUNT_NUMBER        ("387", "Duplicate Account Number"),
	MIN_MAX_DATA_RANGE         		("388", "Data Range should not be greater Than 100"),
	MIN_MAX_RANGE_VALUE         	("389", "Value shouldn't be less than 1 for field "),
	INVALID_DATE_FORMAT         	("390", "Invalid Date Format "),
	DATE_RANGE			         	("391", "Invalid Date Range, It shouldn't be more than 31 days. "),
	TO_DATE_LIMIT			        ("392", "To date Shouldn't be less than from date. "),
	CURRENT_DATE_LIMIT			    ("393", "Date Shouldn't be Later than Today's date. "),
	NO_ORDER_ID						("394", "No Order ID Available"),
	NO_HASH							("395", "No HASH available"),
	
	//error codes and error msgs for coinswitch
	HASH_INVALID			        ("001", "Missing HASH or Invalid HASH or Invalid Field Value/Name"),
	REQUIRED_EMAIL			        ("002", "Email Id is missing or invalid Field Name/Value"),
	REQUIRED_PAN			        ("002", "PAN No is missing or invalid Field Name/Value"),
	REQUIRED_AADHAR			        ("002", "Aadhar No is missing or invalid Field Name/Value"),
	INVALID_PAN 		        	("002", "Invalid PAN"),
	INVALID_PHONE  					("002", "Enter valid Phone Number"),
	REQUIRED_CUST_NAME 		        ("002", "Customer Name is missing or invalid Field Name/Value"),
	REQUIRED_CUST_ID 		        ("002", "Customer Id is missing or invalid Field Name/Value"),
	REQUIRED_CUST_ADD 		        ("002", "Customer Address is missing or invalid Field Name/Value"),
	REQUIRED_CUST_DOB 		        ("002", "Customer DOB is missing or invalid Field Name/Value"),
	DUPLICATE_USER       			("002", "User already exist"),
	INVALID_USER       				("002", "User Not exist"),
	USER_NOT_ALLOWED   				("002", "User Not Allowed"),
	REQUIRED_VIRTUAL_ACCOUNT_NUM 	("002", "Virtual Account Number is missing or invalid Field Name/Value"),
	REQUIRED_VIRTUAL_VPA_NUM 		("002", "Virtual VPA Number is missing or invalid Field Name/Value"),
	REQUIRED_CUSTOMER_ACCOUNT_NO	("002", "Customer Account No is missing or invalid Field Name/Value"),
	REQUIRED_COMPANY_NAME			("003", "Company Name is missing or invalid Field Name/Value"),
	REQUIRED_STATUS 				("002", "Customer Status is missing or invalid Field Name/Value"),
	REQUIRED_DATEFROM 				("002", "Date From is missing or invalid Field Name/Value"),
	INVALID_ACC_HOLDER_NAME	        ("002", "Invalid Account Holder Name"),
	INVALID_BANK_NAME               ("002", "Invalid Bank Name"),
	REQUIRED_DATETO				    ("002", "Date To is missing or invalid Field Name/Value"),
	
	INVALID_HASH_UPI				("001", "Invalid HASH"),
	INVALID_CUST_EMAIL				("002", "Invalid CUST_EMAIL"),
	INVALID_CUST_MOBILE				("003", "Invalid CUST_MOBILE"),
	INVALID_PAY_ID					("004", "Invalid PAY_ID"),
	INVALID_TENURE					("005", "Invalid Tenure"),
	INVALID_MONTHLY_AMOUNT			("006", "Invalid Monthly Amount"),
	INVALID_FREQUENCY				("007", "Invalid Frequency"),
	INVALID_ORDERID        			("010", "Invalid Order ID"),
	MANDATE_NOT_EXIST				("009", "Mandate does not exist!"),
	UPI_AUTOPAY_ERROR				("008", "Something went wrong."),
	INVALID_PAYER_ADDRESS           ("001", "Invalid Payer Address"),
	SMS_ERROR						("401", "Failed to send sms"),
	FRAUD_RESPONSE					("007", "Fraud Response Recieved, Please contact with PaymentGateWay"),
	MIN_AMOUNT_ERROR				("007", "Minimum transaction amount is INR 200"),
	AMOUNT_MISMATCH 				("209","Amount in response should be equal to sale amount ");
	//Response code for user
	private final String responseCode;
	
	//This code contains more details about this error - it may be internal
	private final String code;
	
	//This message contains more details about the error - it may be internal
	private final String internalMessage;
	
	//message for displaying to user
	private final String responseMessage;
	
	private ErrorType(String code, String responseCode, String internalMessage, String userMessage){
		this.code = code;
		this.responseCode = responseCode;
		this.internalMessage = internalMessage;
		this.responseMessage = userMessage;
	}
	private ErrorType(String code, String userMessage){
		this.code = this.responseCode = code;
		this.internalMessage = this.responseMessage = userMessage;
	}
	private ErrorType(String code, String responseCode, String userMessage){
		this.code = code;
		this.responseCode = responseCode;
		this.internalMessage = this.responseMessage = userMessage;
	}

	public String getCode() {
		return code;
	}

	public String getInternalMessage() {
		return internalMessage;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public String getResponseCode() {
		return responseCode;
	}
	
	public static ErrorType getInstanceFromCode(String code){
		ErrorType[] errorTypes = ErrorType.values();
		for(ErrorType errorType : errorTypes){
			if(String.valueOf(errorType.getCode()).toUpperCase().equals(code)){
				return errorType;
			}
		}		
		return null;
	}
}
