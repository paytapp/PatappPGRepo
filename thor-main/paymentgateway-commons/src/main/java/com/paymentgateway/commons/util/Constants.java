package com.paymentgateway.commons.util;

/**
 * @author Sunil
 *
 */
public enum Constants {
	
	CRM_LOG_USER_PREFIX	("userId"),
	CRM_LOG_PREFIX		("CRM~"),
	PG_LOG_PREFIX		("PG~"),
	CARD_STARS			("******"),
	COMMA				(" , "),
	EQUATOR				("="),
	SEPARATOR			("~"),
	MAX_NUMBER_OF_KEYS	("100"),
	TRUE 				("1"),
	FALSE				("0"),
	FIELDS				("FIELDS"),
	TRANSACTIONSTATE_N	("INVALID REQUEST"),
	LAST_LOGIN          ("LAST_LOGIN"),
	LAST_LOGIN_IP       ("LAST_LOGIN_IP"),
	LAST_LOGIN_STATUS   ("LAST_LOGIN_STATUS"),
	USER				("USER"),
	USER_SETTINGS			("USER_SETTINGS"),
	USERTYPE			("USER_TYPE"),
	SALT_LENGTH         ("16"),
	BLANK_REPLACEMENT_STRING  (""),
	SALT_FILE_PATH_NAME ("salt.properties"),
	SALT_ENC_FILE_PATH_NAME ("saltEnc.properties"),
	PAYMENT_TYPE		("PAYMENT_TYPE"),
	PAYMENT_TYPE_MOP 	("PAYMENT_TYPE_MOP"),
	REGION_TYPE 		("REGION_TYPE"),
	CARD_PAYMENT_TYPE_MOP 	("CARD_PAYMENT_TYPE_MOP"),
	TOKEN 				("TOKEN"),
	NB_TOKEN                 ("NB_TOKEN"),
    WL_TOKEN                 ("WL_TOKEN"),
	VPA_TOKEN 				("VPA_TOKEN"),
    EXPRESS_PAY_FLAG	("EXPRESS_PAY_FLAG"),
    SAVE_VPA_FLAG		("SAVE_VPA_FLAG"),
    SAVE_NB_FLAG		("SAVE_NB_FLAG"),
    SAVE_WL_FLAG		("SAVE_WL_FLAG"),
	DIRECPAY_SEPARATOR  ("|"),
	YESBANK_SEPARATOR   ("&"),
	KOTAK_SEPARATOR   	("|"),
	CUSTOM_TOKEN  		("customToken"),
	QUOTES  			("'"),
	AMEX_SEPARATOR		("&"),
	IFRAME_PAYMENT_PAGE ("IFRAME_PAYMENT_PAGE"),
	TRANSACTION_COMPLETE_FLAG   ("INTERNAL_TXN_COMPLETE_FLAG"),
	Y_FLAG                      ("Y"),
	N_FLAG                      ("N"),
	USER_PERMISSION   	        ("USER_PERMISSION"),
	COUNT                       ("Count"),
	FSS_RETURN_URL_NAME         ("Request3DSURL"),
	RUPAY_RETURN_URL_NAME       ("RupayReturnUrl"),
	Y ("Y"),
	BILLING_COLLECTION  		("billingDetailCollectionName"),
	HIBERNATE_FILE_NAME			("hibernate.cfg.xml"),
	SERVER_ID					("PG_SERVER_ID"),
	DEFAULT_CURRENCY_CODE       ("356"),
	MERCHANT_PAYID				("MerchantPayID"),
	DISPATCH_SLIP_MERCHANT_PAYID	("DISPATCH_SLIP_MERCHANT_PAYID"),

	//internal service constants
	INTERNAL_WEBSERVICE_ALLOWED_IP  ("InternalWebserviceAllowedIP"),
	ALLOW							("allow"),
	INTERNAL_WEBSERVICE_URL         ("/process/payment"),
	INTERNAL_ICICI_IMPS_URL         ("/iciciIMPSProcessor"),
	INTERNAL_ICICI_CIB_URL          ("/iciciCibProcessor"),
	INTERNAL_WEBSERVICE_FILTER_NAME ("remoteAddressFilter"),
	
	//Acquirer Supported MOP
	CITRUS_NETBANKING_SUPPORTED_BANK ("CITRUSNB"),
	DIRECPAY_NETBANKING_SUPPORTED_BANK ("DIRECPAYNB"),
	
	//Recurring payments
	DAILY_CRON_STRING           	("DailyCronString"),
	RECURRING_PAYMENT_JOB			("recurringPaymentJob"),
	RECURRING_PAYMENT_JOB_TRIGGER  	("recurringPaymentJobTrigger"),
	CITRUSPAY						("citruspay"),
	CITRUSPAY_TRIGGER	         	("citruspayTrigger"),
	
	// CRM web service
	CRM_SERVICE_LOG_USER_PREFIX ("Log in from CRM web service: PayId"),
	CRM_APP_SALT_FILE_PATH_NAME ("CrmAppSaltFile"),
	SUBTRACTION_SIGN            ("-"),
	//help ticket
	HELPTICKET					("HelpTicket"),
	CONTANT_OK					("OK"),
	
	
	//Mongo db 
	DB_NAME								("dbName"),
	COLLECTION_NAME						("collectionName"),
	BIN_RANGE_COLLECTION_NAME   		("binRangeCollectionName"),
	FREEBIE_BIN_RANGE_COLLECTION_NAME   ("freebiebinRangeCollectionName"),
	DISPATCH_SLIP_COLLECTION_NAME   	("dispatchSlipCollectionName"),
	NODAL_SETTLEMENT_COLLECTION_NAME 	("nodalSettlement"),
	IMPS_SETTlEMENT_COLLECTION          ("ImpsSettlementCollection"),
	QR_COLLECTION          				("qrCollectionName"),
	DCC_COLLECTION          			("dccCollection"),
	UPI_QR_REQUEST_COLLECTION          	("upiQRRequestCollection"),
    NODAL_BENEFICIARY_COLLECTION_NAME   ("nodalBeneficiary"),
	EMI_BIN_RANGE_COLLECTION_NAME   	("emiBinRangeCollectionName"),
	SETTLEMENT_COLLECTION_NAME			("settlementCollectionName"),
	REPORTING_COLLECTION_NAME			("reportingCollectionName"),
	SETTLED_TRANSACTIONS_NAME			("settledTransactionsName"),
	TOKEN_COLLECTION_NAME       		("tokenCollectionName"),
	SESSION_PARAM_COLLECTION_NAME       ("sessionParamCollectionName"),
	SAVED_VPA_COLLECTION_NAME  	 		("vpaTokenCollectionName"),
	NB_TOKEN_COLLECTION_NAME            ("NBTokenCollectionName"),
    WL_TOKEN_COLLECTION_NAME            ("WLTokenCollectionName"),
	EVENT_PAGES_COLLECTION_NAME			("eventPagesCollectionName"),
	DELIVERY_STATUS_COLLECTION_NAME		("deliveryStatusCollectionName"),
	PART_SETTLE_LIMIT_COLLECTION 		("PartSettleDailyLimit"),
	BOOKING_COLLECTION 					("BookingCollection"),
	HOTEL_INV_COLLECTION 				("HotelInvCollection"),
	DISCOUNT_COLLECTION					("discountCollection"),
	STUDENT_COLLECTION					("studentCollection"),
	ORDER_ID_GENERATOR_COLLECTION		("orderIdGeneratorCollection"),
	FEE_COLLECTION					    ("feeCollection"),
	EVENT_INVOICE_COLLECTION	        ("EventInvoice"),
	E_COLLECTION                        ("E_Collection"),
	PROD_DESC_COLLECTION                ("productDescriptionCollection"),
	RECON_FILE_COLLECTION               ("reconFileCollection"),
	RECON_DATA_COLLECTION               ("reconDataCollection"),
	RECON_EXCEPTION_COLLECTION          ("reconExceptionCollection"),
	RECON_STATEMENT_COLLECTION          ("reconStatementCollection"),
	TRANSACTION_STATUS_COLLECTION		("transactionStatus"),
	TRANSACTION_STATUS_EXCEP_COLLECTION ("transactionStatusException"),
	ENACH_COLLECTION					("eNachCollection"),
	MSEDCL_DATA_COLLECTION  			("msedclDataCollection"),
	COMPOSITE_BENE_COLLECTION 			("compositeBeneCollection"),
	CLOSING_AMOUNT_COLLECTION     		("closingAmountCollection"),
	IMPS_UPI_BULK_COLLECTION        	("impsUpiBulkCollection"),
	BENE_VERIFICATION_COLLECTION        ("beneVerificationCollection"),
	UPI_AUTOPAY_COLLECTION				("upiAutoPayCollection"),
	CUST_QR_COLLECTION   	       		("customerQR"),
	CUST_ID_COLLECTION 					("customerIdGenerator"),
	TXN_REQUEST_COLLECTION				("transactionRequest"),
	TXN_HASH_COLLECTION					("transactionHash"),
	INVOICE_COLLECTION_NAME				("InvoiceCollectionName"),
	ROUTER_CONFIG_COLLECTION			("RouterConfigCollection"),
	PENDING_ROUTER_CONFIG_COLLECTION	("PendingRouterConfigCollection"),
	EPOS_TRANSACTION_COLLECTION			("EposTransaction"),
	MERCHANT_PAYOUT_COLLECTION			("merchantPayoutCollection"),
	PG_QR_REQUEST_COLLECTION      	    ("pgQRRequestCollection"),
	CALLBACK_RETRY_COLLECTION      	    ("callbackRetryCollection"),
	CALLBACK_DELAY_COLLECTION      	    ("callbackDelayCollection"),
	NET_SETTLED_ADJUSTMENT_COLLECTION   ("netSettledAdjustmentCollection"),
	REFUND_UTIL_COLLECTION   			("refundUtilCollection"),
	DASHBOARD_COLLECTION   				("dashboardCollection"),
	MERCHANT_P2M_COLLECTION   			("merchantP2MCollection"),
	MERCHANT_P2M_PAYOUT   				("merchantP2MPayout"),
	NET_SETTLED_FILE_COLLECTION         ("netSettledFileCollection"),
	CIB_ACCOUNT_STATEMENT_STATUS_COLLECTION   ("CIBAccountStatementFileStatus"),
	PAYOUT_ACQUIERE_MAPPING             ("payoutAcquirerMappingCollection"),
	PAYOUT_MERCHANT_MAPPING             ("payoutMerchantMappingCollection"),
	GENERATED_REPORT_FILE_COLLECTION    ("generatedReportFileCollection"),
    NODAL_TOPUP_TRANSACTON                ("nodalTopupTransactionCollection"),
    NODAL_TOPUP_BALANCE                    ("nodalTopupBalanceCollection"),
    CASHFREE_QRCODE						 	("cashFreeQRCode"),
    PAYOUT_VIRTUAL_ACCOUNT_DETAILS           ("payoutVirtualAccountDetails"),
    AXIS_UPI_KEY							("axisUpiKey.txt"),
    FRAUD_PREVENTION_COLLECTION			("fraudPreventionCollection"),
    FRAUD_PREVENTION_HISTORY_COLLECTION ("fraudPreventionHistoryCollection"),
    PARENT_MERCHANT_MAPPING						("parentMerchantMapping"),
    PG_PROPERTIES_PATH                      ("PG_PROPS"),
    
    COMPLAINT_COLLECTION				("complaintRaised"),
    COMPLAINT_FILE_COLLECTION			("complaintFiles"),
    USER_SETTINGS_COLLECTION         	("userSettings"),
  


	
	HOST						("host"),
	PORT						("port"),	
	MONGO_URI_PREFIX			("mongoURIprefix"),
	MONGO_URI_SUFFIX			("mongoURIsuffix"),
	MONGO_USERNAME				("username"),
	MONGO_PASSWORD				("password"),
	MONGO_URI					("mongoURI"),
	ENC_DATA                    ("encdata"),
	
	//fraud prevention module
	PG_FRAUD_TYPE				("FRAUD_TYPE"),
	
	//
	PG_ACQUIRER_ERROR			("Response not received from acquirer"),
	
	EPOS_MODE					("ePOS"),
	INVOICE_MODE				("Invoice"),
	EVENT_MODE					("Custom Page"),
	HTML_MODE					("Direct"),
	POST_CAPTURED_TXN			("Post Captured"),
	
	REAL_TIME_TXN				("Real-Time"),
	TXN_ENQUIRY					("TXN Enquiry"),
	CHARGEBACK_REFUND			("Chargeback Refund"),
	NORMAL_REFUND				("Normal Refund"),
	CHARGEBACK_CREATION			("Chargeback Creation"),
	CHARGEBACK_CLOSURE			("Chargeback Closure"),
	
	// email Builder
	 EMAIL_VALIDATORURL                    ("EmailValidatorUrl"),
	 EMAIL_VALIDATORURLBATUWA			   ("EmailValidatorUrlBatuwa"),
	 SMT_EMAIL_VALIDATORURL                ("SmtEmailValidatorUrl"),
	 EMAIL_TRANSACTION_AUTHENTICATION      ("EmailTransactionAuthenticationUrl"),
	 PG_DEMO_EMAIL                         ("support@paymentGateway.com"),
	 RESET_PASSWORD_URL                    ("ResetPasswordUrl"),
	 KHADI_PASSWORD_RESET_URL              ("KhadiPasswordResetUrl"),
	 PAYMENT_RECEIVED_ACKNOWLEDGEMENT      (": Payment Received Acknowledgement"),
	 REFUND_FOR_ODER_ID                    (": Refunded for Order Id :-"),
	 ACCOUNT_VALIDATION_EMAIL              ("Account Validation Email - "),
	 CRM_PASSWORD_CHANGE_ACKNOWLEDGEMENT   (": CRM password change Acknowledgement"),
	 PAYMENT_ATHENTICATION_ACKNOWLEDGEMENT (": Payment Authentication Acknowledgement -"),
	 REMITTANCE_PROCESSED                  (": Remittance Processed for :-"),
	 RESET_PASSWORD_EMAIL                  ("Reset Password Email - "),
	 INVOICE_LINK                          ( "Invoice Link"),
	 SUCCESS_CODE                          ( "000"),
	 SUCCESS                          ( "Success"),
	 FAIL                          ( "Fail"),
	// email service provider
	 ACCOUNT_VALIDATE_ID                   ( "accountValidationID"),
	 REVIEW_MPAMERCHANT_URL                ( "ReviewMPAMerchantUrl"),
	 MERCHANT_LOGO			               ( "MerchantLogo"),
	 EMAIL                                 ( "email"),
	 EMAILID                               ( "emailId"),
	 NAME                                  ( "name"),
	 URL                                   ( "url"),
	 UTR                                   ( "utr"),
	 PAYID                                 ( "payId"),
	 MERCHANT                              ( "merchant"),
	 DATE_FROM                             ( "datefrom"),
	 NET_AMOUNT                            ( "netAmount"),
	 REMITTED_DATE                         ( "remittedDate"),
	 REMITTED_AMOUNT                       ( "remittedAmount"),
	 STATUS                                ( "status"),
	 SUBJECT                               ( "subject"),
	 MESSAGE_BODY                          ( "messageBody"),
	 MESSAGE                               ( "message"),
	 NOTIFICATION_EMAIL_ID                 ( "notifierEmailId"),
	 VIEW_DATE                             ( "viewDate"),
	 CONCERNED_USER                        ( "concernedUser"),
	 SUBMITED_BY                           ( "submittedBy"),
	 ACCOUNT_VALIDATION_KEY                ( "accountValidationKey"),
	 UTF_8                                 ( "UTF-8"),
	 CONTENT_TYPE                          ( "content-type"),
	 APPLICATION_JSON                      ( "application/json"),
	EMAIL_ADD_USER                         ( "emailAddUser"),
	PASSWORD_CHANGE                         ( "passwordChange"),
	PASSWORD_RESET                         ( "passwordReset"),
	INVOICE_LINK_URL                       ( "invoiceLink"),
	REMITTANCE_PROCESS                     ( "remittanceProcess"),
	SEND_BULK_EMAIL_SERVICETAX             ( "sendBulkEmailServiceTax"),
	EMAIL_SEPARATOR		                   ("&"),
	EMAILVALIDATOR                         ( "emailValidator"),
	BUSINESS_NAME                          ( "businessName"),
	INVOICE_NO                             ( "invoiceNo"),
	CITY                                   ( "city"),
	COUNTRY                                ( "country"),
	STATE                                  ( "state"),
	ZIP                                    ( "zip"),
	PHONE                                  ( "phone"),
	ADDRESS                                ( "address"),
	PRODUCT_NAME                           ( "productName"),
	PRODUCT_DESC                           ( "productDesc"),
	QUANTITY                               ( "quantity"),
	INVOICE_AMOUNT                         ( "amount"),
	TOTAL_AMOUNT                           ( "totalAmount"),
	SERVICE_CHARGE                         ( "serviceCharge"),
	INVOICE_CURRENCY_CODE                  ( "currencyCode"),
	EXPIRES_DAY                            ( "expiresDay"),
	EXPIRES_HOUR                           ( "expiresHour"),
	CREATE_DATE                            ( "createDate"),
	UPDATE_DATE                            ( "updateDate"),
	SALT_KEY                               ( "saltKey"),
	RETURN_URL                             ( "returnUrl"),
	RECIPIENT_MOBILE                       ( "recipientMobile"),
	SHORT_URL                             ( "shortUrl"),
	INVOICE_TYPE                          ( "invoiceType"),
	SEND_PROMOSMS                         ( "sendPromoSMS"),
	UPDATE_REQUESTU_URL                   ( "updateRequestNotificationUrl"),
	UPDATE_APPROVED_REJECT_URL            ( "updateApproveRejectNotificationUrl"),
	BIN										( "bin"),
	INVOICEID                             ( "invoiceId"),
	EMAILID_LOGIN						  ("emailIdLogin"),
	FILE_NAME							  ("FileName"),
	COMPLAINT_MAIL_LIST					  ("complaintEmailList"),

	//pg-ws
	TXN_WS_INTERNAL							("TransactionWSInternal"),
	TXN_MIGS_PROCESSOR						("TransactionWSMigsProcessor"),
	TXN_WS_PAYMENT_GATEWAY_RETURN_URL		("TransactionWSPaymentGatewayReturnUrl"),
	TXN_WS_FEDERAL_RETURN_URL				("TransactionWSFederalReturnUrl"),
	TXN_WS_UPI_PROCESSOR					("TransactionWSUPITransactUrl"),
	TXN_WS_BOB_PROCESSOR					("TransactionWSBobProcessor"),
	TXN_WS_LYRA_PROCESSOR					("TransactionWSLyraProcessor"),
	TXN_WS_ICICIECOLLECTION_PROCESSOR		("TransactionWSIciciEcollectProcessor"),
	TXN_WS_KOTAK_PROCESSOR					("TransactionWSKotakProcessor"),
	TXN_WS_RUPAY_PROCESSOR					("TransactionWSRupayProcessor"),
	TXN_WS_GOOGLEPAY_PROCESSOR				("TransactionWSGooglePayProcessor"),
	TXN_WS_IDBI_PROCESSOR				    ("TransactionWSIdbiProcessor"),
	TXN_WS_FSS_PAY_PROCESSOR				("TransactionWSFssPayProcessor"),
	TXN_WS_BILLDESK_PROCESSOR				("TransactionWSBillDeskProcessor"),
	TXN_WS_ISGPAY_PROCESSOR					("TransactionWSIsgPayProcessor"),
    TXN_WS_HDFC_PROCESSOR                    ("TransactionWSHdfcProcessor"),
    TXN_WS_LYRA_DIRECT_PROCESSOR			("TransactionWSLyraDirectProcessor"),
    TXN_WS_LYRA_NB_PROCESSOR				("TransactionWSLyraNBProcessor"),
    TXN_WS_PAYPHI_PROCESSOR					("TransactionWSPayphiProcessor"),
    TXN_WS_ZAAKPAY_PROCESSOR				("TransactionWSZaakpayProcessor"),
    TXN_WS_SBI_PROCESSOR					("TransactionWSSbiProcessor"),
    TXN_WS_APEXPAY_PROCESSOR                ("TransactionWSApexPayProcessor"),
    TXN_WS_PAYU_PROCESSOR					("TransactionWSPayuProcessor"),
    TXN_WS_SBI_CARD_PROCESSOR				("TransactionWSSbiCardProcessor"),
    TXN_WS_FREECHARGE_PROCESSOR				("TransactionWSFreeChargeProcessor"),
    TXN_WS_PAYTM_PROCESSOR					("TransactionWSPaytmProcessor"),
    TXN_WS_IPINT_PROCESSOR					("TransactionWSIpintProcessor"),
    TXN_WS_CASHFREE_PROCESSOR				("TransactionWSCashfreeProcessor"),
    TXN_WS_AIRPAY_PROCESSOR					("TransactionWSAirPayProcessor"),
    TXN_WS_QAICASH_PROCESSOR				("TransactionWSQaicashProcessor"),
    TXN_WS_GLOBALPAY_PROCESSOR				("TransactionWSGlobalpayProcessor"),
    TXN_WS_FLOXYPAY_PROCESSOR				("TransactionWSFloxypayProcessor"),
    TXN_WS_PHONEPE_PROCESSOR				("TransactionWSPhonepeProcessor"),
    TXN_WS_PAYG_PROCESSOR					("TransactionWSPaygProcessor"),
    TXN_WS_RAZORPAY_PROCESSOR				("TransactionWSRAZORPAYProcessor"),
    TXN_WS_DIGITALSOLUTION_PROCESSOR		("TransactionWSDigitalSolutionProcessor"),
    TXN_WS_PAYIN247_PROCESSOR				("TransactionWSPayin247Processor"),
    TXN_WS_GREZPAY_PROCESSOR				("TransactionWSGrezpayProcessor"),
    TXN_WS_UPIGATEWAY_PROCESSOR				("TransactionWSUpigatewayProcessor"),
    TXN_WS_TOSHANIDIGITAL_PROCESSOR			("TransactionWSToshanidigitalProcessor"),
    CASHFREE_ADD_SURCHARGE					("CASHFREE_ADD_SURCHARGE"),
    PAYG_ADD_SURCHARGE						("PAYG_ADD_SURCHARGE"),
    AIRPAY_ADD_SURCHARGE					("AIRPAY_ADD_SURCHARGE"),
    RAZORPAY_ADD_SURCHARGE					("RAZORPAY_ADD_SURCHARGE"),
    QAICASH_ADD_SURCHARGE					("QAICASH_ADD_SURCHARGE"),
    FLOXYPAY_ADD_SURCHARGE					("FLOXYPAY_ADD_SURCHARGE"),
    DIGITALSOLUTION_ADD_SURCHARGE			("DIGITALSOLUTION_ADD_SURCHARGE"),
    PAYIN247_ADD_SURCHARGE					("PAYIN247_ADD_SURCHARGE"),
    GREZPAY_ADD_SURCHARGE					("GREZPAY_ADD_SURCHARGE"),
    UPIGATEWAY_ADD_SURCHARGE				("UPIGATEWAY_ADD_SURCHARGE"),
    TOSHANIDIGITAL_ADD_SURCHARGE			("TOSHANIDIGITAL_ADD_SURCHARGE"),
    GLOBALPAY_ADD_SURCHARGE					("GLOBALPAY_ADD_SURCHARGE"),
    P2PTSPMQR_ADD_SURCHARGE					("P2PTSPMQR_ADD_SURCHARGE"),
	//Federal
	FEDERAL_ECI								("eci"),
	FEDERAL_CAVV							("cavv"),
	FEDERAL_MPI_ERROR_CODE					("mpiErrorCode"),
	FEDERAL_XID								("xid"),
	FEDERAL_AMOUNT							("purchase_amount"),
	FEDERAL_MERCHANT_HASH					("message_hash"),
	FEDERAL_MD								("md"),
	FEDERAL_CURRENCY						("currency"),
	FEDERAL_ID								("ID"),
	FEDERAL_STATUS							("status"),
	
	//Federal Upi
	FED_UPI_RESPONSE_CODE					("ResponseCode"),
	FED_UPI_RESPONSE						("Response"),
	FED_UPI_CUST_REFERENCE					("CustomerReference"),
	FED_UPI_APPROVAL_TIME					("ApprovalTime"),
	FED_UPI_PAYEE_APPROVAL_NUM				("PayeeApprovalNum"),
	FED_UPI_PAYER_APPROVAL_NUM				("PayerApprovalNum"),
	FED_UPI_SUCCESS_CODE					("00"),
	FED_UPI_RESP_VAL_ADD					("RespValAdd"),
	INDIA									("INDIA"),
	//Yes Upi
	YES_UPI_SUCCESS_CODE					("00"),
	YES_UPI_RESPONSE						("S"),
	YESBANK_UPI_MERCHANT_KEY                ("YesBank_Upi_Merchant_Key"),
	
	// IDFC Upi
	IDFC_UPI_SUCCESS_CODE					("000"),
	IDFC_UPI_RESPONSE_MSG					("Approved"),
	IDFC_UPI_DECODE_CHAR                     ("256"),
	//KOTAK UPI
		KOTAK_UPI_SUCCESS_CODE					("00"),
		KOTAK_UPI_RESPONSE						("SUCCESS"),
		KOTAK_UPI_CHECKSUM_FAILURE_CODE			("009"),
		KOTAK_UPI_CHECKSUM_FAILURE_RESPONSE		("callBackCheckSumMismatch"),
		KOTAK_UPI_CHECKSUM                      ("checksum"),
		KOTAK_UPI_STATUS_CODE                   ("statusCode"),
		KOTAK_UPI_PAYEEVPA                      ("payeevpa"),
		KOTAK_UPI_STATUS                        ("status"),
		KOTAK_UPI_RRN                           ("rrn"),
		KOTAK_UPI_PAYERVPA                      ("payervpa"),
		KOTAK_UPI_TIMESTAMP                     ("transactionTimestamp"),
		KOTAK_UPI_REMARKS                       ("remarks"),
		KOTAK_UPI_ACQID                         ("transactionreferencenumber"),
		KOTAK_UPI_TRANSID                        ("transactionid"),
	//IDFC UPI
		KEK                                      ("KEK_IDFCUPI"),
		ENC_DEK                                  ("ENCRYPTED_DEK"),
	//Hdfc Upi
	HDFC_UPI_SUCCESS_CODE					("00"),
	HDFC_UPI_ERROR_RESPONSE_CODE			("U19"),
	HDFC_UPI_INVALID_REQUEST_FIELD_CODE     ("002"),
	HDFC_UPI_CANCELLED_CODE                 ("003"),
	HDFC_UPI_INTERNAL_SYSTEM_ERROR_CODE     ("004"),
	HDFC_UPI_MERCHANT_KEY                    ("HdfcUpiMerchantKey"),
	
	//Axis Upi
	AXISBANK_UPI_SUCCESS_CODE				("00"),
	AXISBANK_UPI_RESPONSE					("SUCCESS"),
	AXISBANK_UPI_MERCHANT_KEY                ("YesBank_Upi_Merchant_Key"),
		
	//add surcharge flags
	FSS_ADD_SURCHARGE						("FSS_ADD_SURCHARGE"),
	AKONTOPAY_ADD_SURCHARGE					("AKONTOPAY_ADD_SURCHARGE"),
	AAMARPAY_ADD_SURCHARGE					("AAMARPAY_ADD_SURCHARGE"),
	FSS_PAY_ADD_SURCHARGE					("FSS_PAY_ADD_SURCHARGE"),
	IPINT_ADD_SURCHARGE						("IPINT_ADD_SURCHARGE"),
	YESBANKCB_ADD_SURCHARGE					("YESBANKCB_ADD_SURCHARGE"),
	AXISBANKCB_ADD_SURCHARGE				("AXISBANKCB_ADD_SURCHARGE"),
	YESBANKCB_UPI_ADD_SURCHARGE             ("YESBANKCB_UPI_ADD_SURCHARGE"),
	FIRSTDATA_ADD_SURCHARGE					("FIRSTDATA_ADD_SURCHARGE"),
	IDFCUPI_ADD_SURCHARGE                   ("IDFCUPI_ADD_SURCHARGE"),
	ICICIUPI_ADD_SURCHARGE                  ("ICICIUPI_ADD_SURCHARGE"),
	FEDERAL_ADD_SURCHARGE					("FEDERAL_ADD_SURCHARGE"),
	AXIS_MIGS_ADD_SURCHARGE					("AXIS_MIGS_ADD_SURCHARGE"),
	BOB_ADD_SURCHARGE						("BOB_ADD_SURCHARGE"),
    FREECHARGE_ADD_SURCHARGE                ("FREECHARGE_ADD_SURCHARGE"),
	KOTAK_ADD_SURCHARGE						("KOTAK_ADD_SURCHARGE"),
	IDBIBANK_ADD_SURCHARGE					("IDBIBANK_ADD_SURCHARGE"),
	KOTAKUPI_ADD_SURCHARGE                  ("KOTAKUPI_ADD_SURCHARGE"),
	ICICI_MPGS_ADD_SURCHARGE                ("ICICI_MPGS_ADD_SURCHARGE"),
	BILLDESK_ADD_SURCHARGE					("BILLDESK_ADD_SURCHARGE"),
	NBIDFC_ADD_SURCHARGE					("NBIDFC_ADD_SURCHARGE"),
	ISGPAY_ADD_SURCHARGE					("ISGPAY_ADD_SURCHARGE"),
	LYRA_ADD_SURCHARGE						("LYRA_ADD_SURCHARGE"),
	PAYPHI_ADD_SURCHARGE					("PAYPHI_ADD_SURCHARGE"),
	VEPAY_ADD_SURCHARGE						("VEPAY_ADD_SURCHARGE"),
	SBI_ADD_SURCHARGE						("SBI_ADD_SURCHARGE"),
	PAYU_ADD_SURCHARGE						("PAYU_ADD_SURCHARGE"),
	EMERCHANTPAY_ADD_SURCHARGE				("EMERCHANTPAY_ADD_SURCHARGE"),
	PAYTM_ADD_SURCHARGE						("PAYTM_ADD_SURCHARGE"),
	ZAAKPAY_ADD_SURCHARGE					("ZAAKPAY_ADD_SURCHARGE"),
	TXN_WS_SAFEXPAY_PROCESSOR				("TransactionWSSAFEXPAYProcessor"),
	SAFEXPAY_ADD_SURCHARGE					("SAFEXPAY_ADD_SURCHARGE"),
	BOB_CARD_NUMBER							("card"),
	BOB_EXP_MONTH							("expmonth"),
	BOB_EXP_YEAR							("expyear"),
	BOB_CVV									("cvv2"),
	FED_CARD_NUMBER							("pan"),
	FED_EXP_MONTH_YEAR						("expiry"),
	FED_EXP_MONTH							("exp_date_mm"),
	FED_EXP_YEAR							("exp_date_yyyy"),
	FED_CVV									("cvv2"),
	MIGS_CARD_NUMBER						("vpc_CardNum"),
	MIGS_EXP								("vpc_CardExp"),
	MIGS_CVV								("vpc_CardSecurityCode"),
	FIRST_DATA_CARD							("CardNumber"),
	FIRST_DATA_EXP_MONTH					("ExpMonth"),
	FIRST_DATA_EXP_YEAR						("ExpYear"),
	FIRST_DATA_CVV							("CardCodeValue"),
	FREECHARGE_UPI_SUCCESS_CODE				("SUCCESS"),
	SAFEXPAY_CDC							("SAFEXPAY_CDC"),
	SAFEXPAY_UPI							("SAFEXPAY_UPI"),
	SAFEXPAY_WL								("SAFEXPAY_WL"),
	SAFEXPAY_NB								("SAFEXPAY_NB"),
	
	//For SMS Gateway
	//SMS_SENDER_USERNAME                     ("SMS_SENDER_USERNAME"),
	//SMS_SENDER_PASSWORD                     ("SMS_SENDER_PASSWORD"),
	//SMS_SOURCE								("SMS_SOURCE"),
	SMS_URL                       			("SMS_URL"),
	AUTH_KEY								("AUTH_KEY"),
	SENDER_ID								("SENDER_ID"),
	SMS_TO_COUNTRY							("SMS_TO_COUNTRY"),
	 
	
	
	//For supported Payment Type
	RUPAY									("RUPAY"),
	VISA									("VISA"),
	MASTERCARD								("MASTERCARD"),
	AMEX									("AMEX"),
	MAESTRO									("MAESTRO"),
	DINERS									("DINERS"),
	PAYMENT_TYPE_UP							("UP="),
	PAYMENT_TYPE_MQR						("MQR="),
	PAYMENT_TYPE_DP							("DP="),
	PAYMENT_TYPE_DC							("DC="),
	PAYMENT_TYPE_NB							("NB="),
	PAYMENT_TYPE_PC							("PC="),
	PAYMENT_TYPE_CD							("CD="),
	PAYMENT_TYPE_AP							("AP="),
	PAYMENT_TYPE_EM							("EM="),
	PAYMENT_TYPE_EM_CC						("EMCC="),
	PAYMENT_TYPE_EM_DC						("EMDC="),
	PAYMENT_TYPE_IN							("INTERNATIONAL"),
	PAYMENT_TYPE_CC							("CC="),
	PAYMENT_TYPE_AD							("AD="),
	MERCHANT_PAYMENT_NB						("NB"),
	PAYMENT_TYPE_WL							("WL="),
	NA										("NA"),
	MOP_COMMA								(","),
	TRUE_STRING								("true"),
	MOP_VISA								("VI"),
	MOP_MASTERCARD							("MC"),
	MOP_RUPAY								("RU"),
	MOP_MAESTRO								("MS"),
	MOP_AMEX								("AX"),
	MOP_DINNER								("DN"),
	MERCHANT_PAYMENT_AD						("AD"),
	MERCHANT_PAYMENT_CARD					("CARD"),
	MERCHANT_PAYMENT_CC						("CC"),
	MERCHANT_PAYMENT_DC						("DC"),
	COLLECT_AUTH							("COLLECT_AUTH"),
	MERCHANT_PAYMENT_UPI					("UPI"),
	MERCHANT_PAYMENT_MQR					("MQR"),
	MERCHANT_PAYMENT_WL						("WL"),
	MERCHANT_PAYMENT_COD					("CD"),
	MERCHANT_PAYMENT_EMI					("EM"),
	MERCHANT_PAYMENT_EMI_CC					("EMCC"),
	MERCHANT_PAYMENT_EMI_DC					("EMDC"),
	MERCHANT_PAYMENT_CRYPTO					("CR"),
	PAYMENT_TYPE_CRYPTO						("CR="),
	MERCHANT_PAYMENT_AAMARPAY				("AP"),
	PAYMENT_TYPE_AAMARPAY					("AP="),
	
	
	//Settlement API
	BENEFICIARY_CODE						("beneficiaryCode"),
	VERSION									("version"),
	UNIQUE_REQUEST_NO						("uniqueRequestNo"),
	APP_ID									("appID"),
	PURPOSE_CODE							("purposeCode"),
	CUSTOMER_ID								("customerID"),
	DEBIT_ACCOUNT_NO						("debitAccountNo"),
	BENEFICIARY								("beneficiary"),
	TRANSFER_TYPE							("transferType"),
	TRANSFER_CURRENCY_CODE					("transferCurrencyCode"),
	TRANSFER_AMOUNT							("transferAmount"),
	REMITTER_INFO							("remitterToBeneficiaryInfo"),
	START_TRANSFER							("startTransfer"),
	
	SWITCH_ACQUIRER_AMOUNT 					("SWITCH_ACQUIRER_AMOUNT"),
	MOP_GOOGLEPAY							("GP"),
	MOP_UPI									("UP"),
	MOP_GOOGLEPAY_PARAMETER					("GOOGLEPAY"),
	MOP_UPI_QR_PARAMETER					("UPI_QR"),
	PAYMENT_RECV							("PAYMENT_RECV"),
	USE_STATIC_DATA							("useStaticData"),
	USE_DATE_INDEX							("useDateIndex"),
	USE_PG_DATE_TIME_INDEX					("usePGDateTimeIndex"),
	
	//PrepareRequestParemeter
	PAYMENT_ADSIMG_URL              ("Payment_AdsImgUrl"),
	PAYMENT_ADSIMG_LINK_URL         ("Payment_AdsImglinkUrl"),
	ADS                              ("ads"),
	
	// For POS
	
	TERMINAL_ID				("TERMINAL_ID"),
	PAY_ID					("PAY_ID"),
	ORDER_ID				("ORDER_ID"),
	AMOUNT					("AMOUNT"),
	CUST_NAME				("CUST_NAME"),
	PG_REF_NUM				("PG_REF_NUM"), 
	
	MOBILE                  ("MOBILE"),

	//For Save Card
	SAVE_CARD_ADMIN_PAYID	("SAVE_CARD_ADMIN_PAYID"),
	
	//Default Pin
	DEFAULT_PIN				("123456"),
	
	//For Scheduler 
	RESPONSE_MESSAGE_TEXT						("responseMessage"), 
	PRECEDING_TIME								("precedingTime"),
	TIME_INTERVAL_SLOT							("timeIntervalSlot"), 
	ACQUIRER_TYPE								("acquirerType"), 
	TXNTYPE										("txnType"), 
	PAYMENT_TYPE1								("paymentType"),
	NONE										("none"), 
	ACTIVE_JOBS									("activeJob"),
	JOB_PARAMS									("jobParams"),
	AUTO_REFUND									("autoRefund"),
	CLOSED_BY_ADMIN								("Closed by Admin"),
	CLOSED_BY_SUBADMIN							("Closed by SubAdmin"),

	//for Checker Maker file location
	MAKER_FILES 			("makerFiles"),
	CHECKER_FILES 			("checkerFiles"),
	ADMIN_FILES 			("adminFiles"),
	BOOKING_ID				("bookingId"),
	
	//logo file location
	LOGO_FILE_UPLOAD_LOCATION   ("LOGO_FILE_UPLOAD_LOCATION"),
	AUTHORIZATION            ("authorization"),
	DOMAIN                    ("domain"),
	LONG_URL                   ("long_url"),
	CUSTOM_PAGE_LOGO_LOCATION	("CustomPageLogoUploadLocation"),
	CUSTOM_PAGE_TNC_FILES_LOCATION	("CustomPageTNCFilesUploadLocation"),
    ICICI_UPI_SUCCESS_RESPONSE_MSG  ("SUCCESS"), 
    KHADI_EMAIL_VALIDATORURL ("KhadiEmailValidatorUrl"),
    ICICI_ENACH_PAYMENT_GATEWAY_LOGO ("ICICI_ENACH_PAYMENT_GATEWAY_LOGO"),
    ICICI_ENACH_POP_LOGO ("ICICI_ENACH_POP_LOGO"),
   
    //For ICICI ENACH
    ICICI_ENACH_ACQUIRER_ID	("ICICI_ENACH_ACQUIRER_ID"),
    ICICI_ENACH_ACQUIRER_SALT	("ICICI_ENACH_ACQUIRER_SALT"),
    ICICI_ENACH_ITEM_ID		("ICICI_ENACH_ITEM_ID"),
    ICICI_ENACH_DEVICE_ID	("ICICI_ENACH_DEVICE_ID"),
    ICICI_ENACH_AMOUNT_TYPE		("ICICI_ENACH_AMOUNT_TYPE"),
	ICICI_ENACH_STATUS_ENQUIRY_URL	("ICICIEnachStatusEnquiryUrl"),
	ICICI_ENACH_SALE_TRANSACTION_URL  ("ICICIEnachSaleTransactionUrl"),
	ICICI_ENACH_SALE_TRANSACTION_STATUS_ENQUIRY_URL  ("ICICIEnachSaleTransactionStatusEnquiryUrl"),
	ICICI_ENACH_REPONSE		("ICICI_ENACH_REPONSE"),
	ICICI_ENACH_FORM_REPONSE	("ICICI_ENACH_FORM_REPONSE"),
	ICICI_ENACH_STATUS_ENQUIRY	("ICICI_ENACH_STATUS_ENQUIRY"),
	ICICI_ENACH_STOP			("ICICI_ENACH_STOP"),
	ICICI_ENACH_TRANSACTION_SCHEDULE	("ICICI_ENACH_TRANSACTION_SCHEDULE"),
	ICICI_ENACH_TRANSACTION_STATUS_ENQUIRY	("ICICI_ENACH_TRANSACTION_STATUS_ENQUIRY"),
	ICICI_ENACH_THROUGH_LINK_EMAIL			("ICICI_ENACH_THROUGH_LINK_EMAIL"),
	ICICI_ENACH_THROUGH_LINK_SMS			("ICICI_ENACH_THROUGH_LINK_SMS"),
	ICICI_ENACH_ACQUIRER_CHARGES			("ICICI_ENACH_ACQUIRER_CHARGES"),
	
	//UPI AUTOPAY
	UPI_AUTOPAY_MANDATE_SIGN		("UPI_AUTOPAY_MANDATE_SIGN"),
	UPI_AUTOPAY_TERMINAL_ID			("UPI_AUTOPAY_TERMINAL_ID"),
	UPI_AUTOPAY_MID					("UPI_AUTOPAY_MID"),
	UPI_AUTOPAY_API_KEY				("UPI_AUTOPAY_API_KEY"),
	UPI_AUTOPAY_MANDATE_URL			("UPI_AUTOPAY_CREATE_MANDATE_URL"),
	UPI_AUTOPAY_TRANSACTION_URL		("UPI_AUTOPAY_EXECUTE_URL"),
	UPI_AUTOPAY_NOTIFICATION_URL	("UPI_AUTOPAY_NOTIFICATION_URL"),
	UPI_AUTOPAY_TRANSACTION_STATUS_ENQUIRY_URL		("UPI_AUTOPAY_TRANSACTION_STATUS_ENQUIRY_URL"),
	UPI_AUTOPAY_TRANSACTION_STATUS_BY_CRITERIA_URL	("UPI_AUTOPAY_TRANSACTION_STATUS_BY_CRITERIA_URL"),
	UPI_AUTOPAY_AMOUNT_LIMIT		("UPI_AUTOPAY_AMOUNT_LIMIT"),
	UPI_AUTOPAY_MERCHANT_NAME		("UPI_AUTOPAY_MERCHANT_NAME"),
	UPI_AUTOPAY_SUB_MERCHANT_NAME		("UPI_AUTOPAY_SUB_MERCHANT_NAME"),
	UPI_AUTOPAY_DEBIT_TRANSACTION_URL	("UpiAutoPayTransaction"),
	UPI_AUTOPAY_DEBIT_NOTIFICATION_URL	("UpiAutoPayNotification"),
	UPI_AUTOPAY_STATUS_ENQUIRY_URL		("UpiAutoPayStatusEnquiry"),
	UPI_AUTOPAY_STATUS_ENQUIRY_CRITERIA_URL	("UpiAutoPayStatusEnquiryByCriteria"),
	UPI_AUTOPAY_ACQUIRER_CHARGES		("UPI_AUTOPAY_ACQUIRER_CHARGES"),
	UPI_AUTOPAY_THROUGH_LINK_EMAIL			("UPI_AUTOPAY_THROUGH_LINK_EMAIL"),
	UPI_AUTOPAY_THROUGH_LINK_SMS			("UPI_AUTOPAY_THROUGH_LINK_SMS"),
    
    SUB_ADMIN_ANALYTICS_PERMISSION 					("1"),
    SUB_ADMIN_MERCHANT_SETUP_PERMISSION 			("2"),
    SUB_ADMIN_MERCHANT_CONFIGURATIOMS_PERMISSION 	("3"),
    SUB_ADMIN_RESELLER_PERMISSION		 			("4"),
    SUB_ADMIN_VIEW_CONFIGURATION_PERMISSION 		("5"),
    SUB_ADMIN_QUICK_SEARCH_PERMISSION		 		("6"),
    SUB_ADMIN_REPORTING_PERMISSION			 		("7"),
    SUB_ADMIN_VENDOR_PAYOUT_PERMISSION		 		("8"),
    SUB_ADMIN_QUICK_PAY_PERMISSION			 		("9"),
    SUB_ADMIN_SCHOOL_FEE_MANAGER_PERMISSION 		("10"),
    SUB_ADMIN_BATCH_OPERATIONS_PERMISSION	 		("11"),
    SUB_ADMIN_DISBURSEMENTS_PERMISSION			 	("12"),
    SUB_ADMIN_FRAUD_PREVENTION_PERMISSION			("13"),
    SUB_ADMIN_MANAGE_USERS_PERMISSION		 		("14"),
    SUB_ADMIN_MANAGE_ACQUIRERS_PERMISSION		 	("15"),
    SUB_ADMIN_MANAGE_ISSUERS_PERMISSION		 		("16"),
    SUB_ADMIN_AGENT_ACCESS_PERMISSION		 		("17"),
    SUB_ADMIN_CHARGEBACK_PERMISSION			 		("18"),
    SUB_ADMIN_MSEDCL_PERMISSION			 			("19"),
    SUB_ADMIN_SUBSCRIPTION_PERMISSION				("20"),
    SUB_ADMIN_ACCOUNT_VERIFICATION_PERMISSION		("21"),
    
	SMS_INNUVISSOLUTION_URL                  ("SMS_INNUVISSOLUTION_URL"),
	SENDER_ID_INNUVISSOLUTION                ("SENDER_ID_INNUVISSOLUTION"),
	USER_INNUVISSOLUTION                     ("USER_INNUVISSOLUTION"),
	PASSWORD_INNUVISSOLUTION                 ("PASSWORD_INNUVISSOLUTION"),
	CHANNEL_INNUVISSOLUTION                  ("CHANNEL_INNUVISSOLUTION"),
	FLASHSMS                                ("FLASHSMS"),
	PEID                                    ("PEID"),
	DCS                                     ("DCS"),
	SMS_INNUVIS_SOLUTIONS                      ("SMS_INNUVIS_SOLUTIONS"),
	ICICI_DCC_TRANSACTION					("iciciDccTransaction"),
	RECIEPT_BATCH_GENERATOR_COLLECTION		("recieptBatchGeneratorCollection"),
	MONTHLY_INVOICE_FILE_STATUS_COLLECTION		("MonthlyInvoiceFileStatus"),
    MSEDCL_DATA_FILE_STATUS_COLLECTION        ("MSEDCLDataFileStatus"),
	PAYOUT_FILE_LOCATION                    ("PAYOUT_FILE_LOCATION"),
	NET_SETTLED_FILE_LOCATION_URL           ("NET_SETTLED_FILE_LOCATION_URL"),
	CRM_IMAGE_URL							("CRM_IMAGE_URL"),
	//New variable added for Kaleyra
	SMS_KALEYRA_URL                  ("SMS_KALEYRA_URL"),
	SENDER_ID_KALEYRA			     ("SENDER_ID_KALEYRA"),
	USER_KALEYRA                     ("USER_KALEYRASOLUTION"),
	PASSWORD_KALEYRA                 ("PASSWORD_KALEYRASOLUTION"),
	CHANNEL_KALEYRA                  ("CHANNEL_KALEYRASOLUTION"),
	SMS_KALEYRA_SHORTENURL 			("SMS_KALEYRA_SHORTENURL"),
	CALLBACK_KALEYRA_URL			("CALLBACK_KALEYRA_URL"),
	KALEYRA_TEST_KEY				("KALEYRA_TEST_KEY"),
	

	//AML
    RECORD_IDENTIFIER                       ("RECORD_IDENTIFIER"),
    PARENT_COMPANY                          ("PARENT_COMPANY"),
    SCREENING_CATEGORY                      ("SCREENING_CATEGORY"),
    AML_TOKEN                               ("AML_TOKEN"),
    AML_URL                                 ("AML_URL"),
    AML_PRIVATE_KEY_PASSWARD                ("AML_PRIVATE_KEY_PASSWARD"),
    AML_PRIVATE_CERT_LOCATION				("AML_PRIVATE_CERT_LOCATION"),
    AML_PUBLIC_CERT_LOCATION				("AML_PUBLIC_CERT_LOCATION"),
    AML_JKS_LOCATION						("AML_JKS_LOCATION"),
    AML_JKS_PASSWORD						("AML_JKS_PASSWORD"),
    AML_FILE_LOCATION                       ("AML_FILE_LOCATION"),
    
    //AWS
    AWS_PORT                                ("AWS_PORT"),
    AWS_HOST                                ("AWS_HOST"),
    AWS_CONFIGSET                           ("AWS_CONFIGSET"),
    SMTP_PASSWORD                           ("SMTP_PASSWORD"),
    SMTP_USERNAME                           ("SMTP_USERNAME"),
	VIRTUAL_ACCOUNT_NUM_GENERATOR_COLLECTION ("virtualAccountNoGeneratorCollection"),
	COINSWITCH_ACCOUNTS						 ("coinSwitchCustomerAccount"),
	COINSWITCH_TRANSACTION_DATA 			 ("coinSwitchCustomerTxnData"),
	REFUND_PENDING_TXN_DATA		 			 ("refundPendingTxnData"),
	REPORTS_FILE_LOCATION_URL           	("REPORTS_FILE_LOCATION_URL"),
	REFUND_UTIL_FILE_LOCATION_URL           ("REFUND_UTIL_FILE_LOCATION_URL"),
	REFUND_TXN_FILE_FOLDER           		("refundTxnFile"),
	CHARGEBACK_CLOSER_FILE_FOLDER           ("chargebackCloserFile"),
	CHARGEBACK_CREATION_FILE_FOLDER         ("chargebackCreationFile"),
	REFUND_URL_FOR_FILEDATA           		("RefundURLForFileData");
	

	
   private final String value; 
	
	private Constants(String value){
		this.value = value;
	}

	public String getValue() {
		return value;
	}	
}
