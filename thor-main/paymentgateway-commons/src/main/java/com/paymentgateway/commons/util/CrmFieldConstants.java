package com.paymentgateway.commons.util;

public enum CrmFieldConstants {

	LAST_ACTION_NAME					("actionName"),
	ALL_MERCHANTS						("ALL MERCHANTS"),
	ALL_USERS							("ALL USERS"),
	ALL									("ALL"),
	SELECT_CURRENCY						("Select Currency"),
	SELECT_MERCHANT						("Select Merchant"),
	TOTAL_CREDIT_SUCCESS				("totalCreditSuccess"),
	TOTAL_CREDIT_FAILED					("totalCreditFailed"),
	TOTAL_CREDIT_TRANSCTION				("totalCreditTransaction"),
	TOTAL_CREDIT_BAUNCED				("totalCreditBaunced"),
	TOTAL_CREDIT_CANCELLED				("totalCreditCancelled"),
	TOTAL_CREDIT_DROPPED				("totalCreditDropped"),

	TOTAL_DEBIT_SUCCESS					("totalDebitSuccess"),
	TOTAL_DEBIT_FAILED					("totalDebitFailed"),
	TOTAL_DEBIT_TRANSCTION				("totalDebitTransaction"),
	TOTAL_DEBIT_BAUNCED					("totalDebitBaunced"),
	TOTAL_DEBIT_CANCELLED				("totalDebitCancelled"),
	TOTAL_DEBIT_DROPPED					("totalDebitDropped"),
	
	TOTAL_NET_BANK_TRANSACTION			("totalNetBankTransaction"),
	TOTAL_NET_BANK_SUCCESS				("totalNetBankSuccess"),
	TOTAL_NET_BANK_FAILED				("totalNetBankFailed"),
	TOTAL_NET_BANK_DROPPED				("totalNetBankDropped"),
	TOTAL_NET_BANK_CANCELLED			("totalNetBankCancelled"),
	
	TOTAL_WALLET_TRANSACTIONS			("totalWalletTransaction"),
	TOTAL_WALLET_SUCCESS				("totalWalletSuccess"),
	TOTAL_WALLET_FAILED				    ("totalWalletFailed"),
	TOTAL_WALLET_DROPPED				("totalWalletDropped"),
	TOTAL_WALLET_CANCELLED			    ("totalWalletCancelled"),
	
	//credit card mopType analytics Payments Method
	TOTAL_VISAS							("totalVisa"),
	TOTAL_SUCCESS_VISA					("totalSuccessVisa"),
	TOTAL_FAILED_VISA					("totalFailedVisa"),
	TOTAL_DROPPED_VISA					("totalDroppedVisa"),
	TOTAL_CANCELLED_VISA				("totalCancelledVisa"),
	TOTAL_AMEXS							("totalAmex"),
	TOTAL_SUCCESS_AMEX					("totalSuccessAmex"),
	TOTAL_FAILED_AMEX					("totalFailedAmex"),
	TOTAL_DROPPED_AMEX					("totalDroppedAmex"),
	TOTAL_CANCELLED_AMEX				("totalCancelledAmex"),
	TOTAL_MASTERS						("totalMaster"),
	TOTAL_SUCCESS_MASTER				("totalSuccessMaster"),
	TOTAL_FAILED_MASTER					("totalFailedMaster"),
	TOTAL_DROPPED_MASTER				("totalDroppedMaster"),
	TOTAL_CANCELLED_MASTER				("totalCancelledMaster"),
	TOTAL_MESTRO						("totalMestro"),
	TOTAL_SUCCESS_MESTRO				("totalSuccessMestro"),
	TOTAL_FAILED_MESTRO					("totalFailedMestro"),
	TOTAL_DROPPED_MESTRO				("totalDroppedMestro"),
	TOTAL_CANCELLED_MESTRO				("totalCancelledMestro"),


	TOTAL_DINER							("totalDiner"),
	TOTAL_SUCCESS_DINER					("totalSuccessDiner"),
	TOTAL_FAILED_DINER					("totalFailedDiner"),
	TOTAL_DROPPED_DINER					("totalDroppedDiner"),
	TOTAL_CANCELLED_DINER				("totalCancelledDiner"),
	

	TOTAL_NETBANKING_TRANSACTION		("totalNetBankingTransaction"),
	TOTAL_CREDITCARDS_TRANSACTION		("totalCreditCardsTransaction"),
	TOTAL_DEBIT_TRANSACTION				("totalDebitCardsTransaction"),
	TOTAL_WALLET_TRANSACTION			("totalWalletTransaction"),


	TOTAL_CANCELLED						("totalCancelled"),
	TOTAL_BAUNCED						("totalBaunced"),
	TOTAL_DROPPED						("totalDropped"),
	TOTAL_TRANSACTION					("totalTransaction"),
	TOTAL_TRANSACTIONS					("totalTxns"),
	TOTAL_PENDING						("totalPending"),
	TOTAL_SUCCESS						("totalSuccess"),
	TOTAL_FAILED						("totalFailed"),
	TOTAL_REFUNDED						("totalRefunded"),
	REFUNDED_AMOUNT						("refundedAmount"),
	PAYMENT_METHOD						("paymentMethod"),
	CAPTURED_AMOUNT						("CAPTURED_AMOUNT"),
	REFUND_AMOUNT						("REFUND_AMOUNT"),
	CHARGEBACK_AMOUNT					("CHARGEBACK_AMOUNT"),
	APPROVED_AMOUNT						("approvedAmount"),
	TXN_AMOUNT							("TXN_AMOUNT"),	
	CREATE_DATE							("CREATE_DATE"),
	TXN_DATE							("txndate"),
	REFUND_DATE							("REFUND_DATE"),
	CAPTURE_TXN_ID						("CAPTURE_TXN_ID"),
	REFUNDED							("Refunded"),
	INTERNAL_REQUEST_FIELDS				("INTERNAL_REQUEST_FIELDS"),
	INVOICE_URL							("invoiceURL"),
	BENE_VERIFICATION_LINK				("beneVerificationURL"),
	STATIC_PGQR_URL						("staticPgQrURL"),
	STATIC_PGQR_RETURN_URL				("staticPgQrReturnUrl"),
	STATIC_PGQR_REQUEST_URL				("staticPgQrRequestUrl"),
	KHADI_INVOICE_URL					("invoiceKhadiURL"),
	INVOICE_PROMOTIONAL_URL				("invoicePromotionalURL"),
	INVOICE_RETURN_URL					("invoiceReturnUrl"),
	CUSTOM_RETURN_URL					("customReturnUrl"),
	TOTAL_VISA							("visa"),
	TOTAL_MASTER						("mastercard"),
	TOTAL_AMEX							("amex"),
	TOTAL_NETBANKING					("net"),
	TOTAL_MESTRO_CARDS					("maestro"),
	TOTAL_EZEECLICK					    ("ezeeClick"),
	TOTAL_OTHER							("other"),
	TOTAL_CREDIT						("totalCredit"),
	TOTAL_DEBIT							("totalDebit"),
	REFUNDABLE_AMOUNT					("refundableAmount"),
	TOTAL_NEW_ORDER						("totalNewOder"),
	TOTAL_ENROLLED						("totalEnrolled"),
	MODE_TYPE							("modeType"),
	ACCOUNTS							("accounts"),
	EMAIL_VALIDATION_FLAG				("emailValidationFlag"),
	EXPRESS_PAY_FLAG					("expressPayFlag"),
	MERCHANT_HOSTED_FALAG				("merchantHostedFlag"),
	TRANSACTION_AUTHENTICATION_EMAIL_FLAG("transactionAuthenticationEmailFlag"),
	TRANSACTION_CUSTOMER_EMAIL_FLAG		("transactionCustomerEmailFlag"),
	REFUND_TXN_CUSTOMER_EMAIL_FLAG		("refundTransactionCustomerEmailFlag"),
	REFUND_TXN_MERCHANT_EMAIL_FLAG		("refundTransactionMerchantEmailFlag"),
	RETRY_TRANSACTION_FLAG				("retryTransactionCustomeFlag"),
	IFRAME_PAYMENT_FLAG					("iframePaymentFlag"),
	TRANSACTION_EMAILER_FLAG			("transactionEmailerFlag"),
	TRANSACTION_SMS_FLAG				("transactionSmsFlag"),
	USER_TYPE							("userType"),
	SURCHARGE_FLAG						("surchargeFlag"),

	//Request Fields
	SERVICE_USER_AGENT					("WebService"),
	USER_AGENT							("User-Agent"),
	CONFIRM_PASSWORD					("confirmPassword"),
	CONFIRM_PIN					        ("confirmPin"),
	OLD_PASSWORD						("oldPassword"),
	NEW_PASSWORD						("newPassword"),
	CONFIRM_NEW_PASSWORD				("confirmNewPassword"),
	OLD_PIN						        ("oldPin"),
	NEW_PIN						        ("newPin"),
	CONFIRM_NEW_PIN				        ("confirmNewPin"),
	MERCHANTS							("MERCHANTS"),
	USER_RESELLER_TYPE					("reseller"),
	SUPER_MERCHANT_TYPE					("superMerchant"),
	SUB_MERCHANT_TYPE					("subMerchant"),
	//General Configurations    
	RUN_SCHEDULAR_FALG					("RunSchedular"),


	//Action return strings
	SUPERADMIN                          ("superAdmin"),
	ADMIN								("admin"),
	NEW_USER							("newuser"),
	OFFLINE_MPA							("offlineMPA"),
	MERCHANT							("merchant"),
	SUBUSER								("subuser"),	
	SIGNUP_PROFILE						("signupProfile"),
	ACQUIRER							("acquirer"),
	SUBADMIN							("subAdmin"),
	RECONUSER							("reconUser"),
	AGENT								("agent"),
	ACQUIRER_SUBUSER					("acquirerSubuser"),
	SUBMERCHANT						 	("subMerchant"),
	ENACH_NEW_USER						("eNachNewUser"),
	PARENTMERCHANT						 ("parentMerchant"),

	//Messages
	RESELLER_DETAILS_UPDATED			("Reseller mapped successfully"),
	USER_DETAILS_UPDATED				("User details updated successfully"),
	STUDENT_DETAILS_UPDATED				("Student details updated successfully"),
	STUDENT_DETAILS_UPDATE_FAILED			("Student details update failed"),
	ACQUIRER_DETAILS_UPDATED			("Acquirer details updated successfully"),
	DETAILS_UPDATE_REQUEST              ("Update request submitted"),
	SETTLEMENT_NAMING_MESSAGE   ("Settlement Naming Convention Already Exist"),
	REFUND_VALIDATION_NAMING_MESSAGE ("Refund Naming Convention Already Exist"),
	SETTLEMENT_AND_REFUND_VALIDATION_NAMING_MESSAGE ("Settlement Naming Convention & Refund Naming Convention Should Not Be Blank"),
	PENDING_REQUEST_EXIST               ("Update request already exists!"),
	DETAILS_SAVED_SUCCESSFULLY			("Details saved successfully"),
	DETAILS_SUBADMAIN_SUCCESSFULLY      ("Sub-admin successfully created."),
	DETAILS_ACQUIRER_SUCCESSFULLY       ("Acquirer successfully created."),
	NODAL_AMOUNT_SUCCESSFULLY           ("Nodal Amount successfully Added."),
	DETAILS_AGENT_SUCCESSFULLY      	("Agent successfully created."),
	DETAILS_SUBUSER_SUCCESSFULLY        ("Sub-user successfully created."),
	PROCESS_INITIATED_SUCCESSFULLY		("Request processed successfully"),
	FILE_ALREADY_PRESENT				("File already present in repository."),
	MAIL_SENT_SUCCESSFULLY				("Mails sent successfully"),
	MAIL_NOT_SENT						("Mails Couldn't not be sent, Some thing went wrong"),
	UTR_ALREADY_EXCITED					("Sorry!! You can not proceed as UTR already exists"),
	AMOUNT_CAN_NOT_BE_BLANK				("Please try again, Amount can not be zero"),
	PLEASE_SELECT_MERCHANT				("Please select merchant name"),
	NETBANKING_VALIDATION				("Netbanking transactions can only be refunded after 2 days of authorisation !! "),
	FROMTO_DATE_VALIDATION				("From date must be before the To date"),
	DATE_RANGE							("No. of days can not be more than 31"),
	INVALID_CAPTCHA						("Invalid Captcha"),
	PLEASE_SAVE							("Please click Save to use default theme"),
	SELECT_PRIMARY_CARD					("Please select primary card"),
	SELECT_SALE							("Please select payment mode as SALE for Yes Bank"),
	SELECT_DIRECPAY_YES					("Please select Direcpay or YesBank as acquirer"),
	SELECT_ONLY_ONE						("Select only one acquirer as primary card"),
	SELECT_ONE_NETBANKING				("Please select primary netbanking"),
	PAY_NOW_VALIDATION					("Could'nt Proess Debit before 30 minutes of Registration"),
	
	//Status
	INITIATED							("INITIATED"),
	STATUS								("STATUS"),

	USER_CHARGINGDETAILS_NOT_SET_MSG	("Please set atleast one charging details for this merchant"),
	USER_SURCHARGE_NOT_SET_MSG			("Please set surcharge for this merchant"),
	USER_MERCHANT_MAPPING_NOT_SET       ("Please set Merchant Mapping for this merchant"),
 
	//Retry flag message
	RETRY_TRANSACTION					("Your transaction has been failed. Please retry"),

	//Date format
	INPUT_DATE_FORMAT					("dd-MM-yyyy"),
	OUTPUT_DATE_FORMAT					("yyyy-MM-dd"),
	OUTPUT_DATE_FORMAT_DB				("yyyy-MM-dd HH:mm:ss"),
	DATE_TIME_FORMAT					("dd-MM-yyyy HH:mm:ss"),
	INPUT_DATE_FOR_MIS_REPORT			("dd/MM/yyyy"),
	UPI_AUTOPAY_FORMAT					("dd/MM/yyyy hh:mm a"),
	COMMA								(","),
	TO_TIME_FORMAT						(" 00:00:00"),
	FROM_TIME_FORMAT					(" 23:59:59"),
	OUTPUT_DATE_FORMAT_REQUEST			("ddMMyyyy"),

	//Transaction fields
	ORIG_TXN_ID							("origTxnId"),
	RESPONSE							("response"),
	CURRENCY_CODE						("currencyCode"),
	NOT_AVAILABLE						("Not available"),
	NA									("NA"),

	//Website Signup
	WEBSITE_SIGNUP_PAYID				("websiteSignupPayId"),
	WEBSITE_SIGNUP_RETURNURL			("websiteSignupReturnUrl"),
	WEBSITE_PACKAGE_CURRENCY			("packageCurrency"),

	//Interceptor fields
	HTTP_POST_METHOD					("POST"),

	//Sub User Permission Fields
	VIEW_TRANSACTIONS					("View Transactions"),
	VIEW_REPORTS						("View Reports"),
	AGENT_SEARCH						("agent search"),
	VIEW_SURCHARGE						("view surcharge"),
	CREATE_INVOICE						("Create Invoice"),
	VIEW_INVOICE						("View Invoice"),
	VIEW_REMITTANCE						("View Remittance"),
	VIEW_MERCHANT_SETUP					("View MerchantSetup"),
	CREATE_MAPPING					    ("Create Mapping"),
	VIEW_ANALYTICS					    ("View Analytics"),
	VIEW_SEARCH_PAYMENT					("View SearchPayment"),
	VIEW_RECONCILIATION					("View Reconciliation"),
	CREATE_BATCH_OPERATION				("Create BatchOperation"),
	FRAUD_PREVENTION				    ("Fraud Prevention"),
	CREATE_BULK_EMAIL				    ("Create BulkEmail"),
	VIEW_CASHBACK				        ("View CashBack"),
	CREATE_HELPTIKECT				    ("Create HelpTicket"),
	VIEW_MERCHANT_BILLING				("View Merchant Billing"),
	//Customer Transaction Authentication
	COUNTRY_CODE						("CountryCode"),
	INDIA_REGION_CODE					("India"),

	//Yes bank Refund
	ROW_NUMBER							("rowNumber"),
	IFSC_CODE							("YESB"),

	//Currency Code
	INR									("356"),
	USD									("840"),
	AED									("784"),
	GBP									("826"),
	EUR									("978"),
	AUD									("036"),

	//default currency success message
	DEFAUL_CURRENCY_UPDATE				("Default currency updated successfully!!"),
	
	//Acquirer BankType for misReport
	FSS_BANK                            ("HDFC BANK LIMITED"),
	DIREC_PAY_BANK                      ("KOTAK BANK LIMITED"),
	AMEX_BANK                           ("AMERICAN EXPRESS"),
	MOBIKWIK_BANK                       ("ICICI BANK LTD"),
	CITRUS_PAY_BANK                     ("Yes Bank"),
	
	// transaction types
	WALLET                              ("Wallet"),
	NET_BANKING                         ("Net-Banking"),
		
	SETTLEMENT_PROCESSED  				("PROCESSED"),
	
	//Heading in transaction emailer
	MERCHANT_HEADING					("Merchant"),
	CUSTOMER_HEADING					("Customer"),
	MERCHANT_MESSAGE					("The payment has been successfully processed by the customer."),
	CUSTOMER_MESSAGE					("Thank you for paying with PaymentGateway. Your Payment has been successfully processed."),
	CUSTOMER							("CUSTOMER"),
	
	//Chargeback
	GENERATED_SUCCESSFULLY 				("Chargeback generated successfully."),


	//Ticketing System
	TICKET_ID							("Ticket id"),
	TICKET_MESSAGE						("Ticket Message"),
	TICKET_ASSIGNED_TO					("Ticket Asssigned To"),
	TICKET_STATUS						("Ticket Stauts"),
	TICKET_TYPE							("Ticket Type"),
	TICKET_MOBILE						("Mobile No"),
	ASSIGNED_TICKET_SUCCESS				("Agent assigned update successfully!!"),	
	ASSIGNED_TICKET_FAILED				("Agent assigned update failed!!"),
	TICKET_DETAILS_UPDATE				("Ticket details updated successfully"),
	TICKET_CREATE_SUCCESSFULLY			("Ticket generated successfully."),
	ADMIN_TICKET_HEADING				("Admin"),
	AGENT_TICKET_HEADING				("Agent"),
	MERCHANT_TICKET_MESSAGE				("TicketCreate successfully"),
	ADMIN_TICKET_MESSAGE				("TicketCreate successfully"),
	ADMIN_UPDATE_TICKET					("Ticket update successfully"),
	MESSAGE_SEND_SUCCESSFULLY			("Message send successfully"),	
	
	
	//URL SHORTENER
	GOOGLE_URL_SHORTENER				("GoogleURLShortner"),
	
	//Email BodyCreate 
	TXN_ID		         ("TXN_ID"),
	TXN_STATUS		     ("STATUS"),
	ORDER_ID		    ("ORDER_ID"),
	CURRENCYCODE		("CURRENCY_CODE"),
	AMOUNT				("AMOUNT"),
	RESPONSE_DATE_TIME	("RESPONSE_DATE_TIME"),
	CUST_NAME	        ("CUST_NAME"),
	CUST_EMAIL	        ("CUST_EMAIL"),
	AUTH_CODE	        ("AUTH_CODE"),
	RESPONSE_MESSAGE	("RESPONSE_MESSAGE"),
	
	//surchare module
	CREATE_SURCHARGE ("Create Surcharge"),

	USER_ALREADY_EXIST ("User Already Exist"),
	//Refund Flag
	REFUND_FLAG ("R") ;
	
	private final String value; 

	private CrmFieldConstants(String value){
		this.value = value;
	}

	public String getValue() {
		return value;
	}	
}
