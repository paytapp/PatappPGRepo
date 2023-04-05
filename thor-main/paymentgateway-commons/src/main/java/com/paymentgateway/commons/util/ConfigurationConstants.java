package com.paymentgateway.commons.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Sunil
 *
 */
public enum ConfigurationConstants {

	//Database connection properties
	PAYMENT_PAGE_MANDATORY_FIELDS	("PaymentPageMandatoryFields"),
	DB_URL							("DBURL"),
	DB_USER							("DBUser"),
	DB_PASSWORD						("DBPassword"),
	DB_DRIVER						("DBDriver"),
	DB_ALLREQUESTSTRINGFIELDS		("DBAllRequestStringFields"),
	DB_FIELDS      					("DBFields"),
	CUSTOMER_INFO_FIELDS   			("CustomerInfoFields"),
	ECOLLECTION_RESPONSE_FIELDS     ("ECollectionResponseFields"),
	TRANSACTION_STATUS_FIELDS     	("TransactionStatusFields"),
	
	//Security Configurations
	HASHING_ALGORITHAM				("HashingAlgoritham"),
	FIELD_SEPARATOR					("FieldSeparator"),
	FIELD_EQUATOR					("FieldEquator"),
	REQUEST_FIELDS					("RequestFields"),
	INTERNAL_REQUEST_FIELDS			("InternalRequestFields"),
	INTERNAL_RESPONSE_FIELDS		("InternalResponseFields"),
	RESPONSE_FIELDS					("ResponseFields"),
	IS_DEBUG						("IsDebug"),
	SECURE_REQUEST_FIELDS			("SecureRequestFields"),
	DUPLICATE_ON_ORDER_ID			("AllowDuplicateOnOrderId"),
	ALLOW_FAILED_HASH				("AllowFailedHash"),

	//FSS fields
	FSS_MANDATORY_FIELDS_ENROLL		("FSSMandatoryFieldsEnroll"),
	FSS_ENROLLMENT_URL				("FSSEnrollmentUrl"),
	FSS_PARES_AUTHENTICATION_URL	("FSSParesAuthorizationUrl"),
	FSS_AUTHORIZATION_URL			("FSSAuthorizationUrl"),
	FSS_SUPPORT_TRANSACTION_URL		("FssSupportTransactionUrl"),
	
	//CitrusPay fields
	CITRUSPAY_TRANSACTION_URL		("CitruspayTransactionUrl"),
	CITRUSPAY_RETURN_URL			("CitruspayReturnUrl"),
	CITRUSPAY_ACCESS_KEY			("CitruspayAccessKey"),
	CITRUSPAY_MERCHANT_KEY			("CitruspayMerchantKey"),
	CITRUSPAY_REFUND_URL			("CitruspayRefundUrl"),
	CITRUSPAY_ENQUIRY_URL			("CitruspayEnquiryUrl"),
	CITRUSPAY_STATUTS_UPDATE_INTERVAL_TIME    ("CitrusStatusUpdateIntervalTime"),
	CITRUSPAY_STATUTS_UPDATE_TIME   		  ("CitrusStatutsUpdateTime"),
	CITRUSPAY_CREATE_SUBSCRIPTION_URL 		  ("citrusCreateSubscriptionUrl"),
	CITRUSPAY_SUBSCRIPTION_VAULT_TOKEN_URL	  ("citrusSubscriptionVaultTokenUrl"),
	
	
	//DIRECPAY fields
	DIRECPAY_TRANSACTION_URL		("DirecpayRequestUrl"),
	DIRECPAY_RETURN_URL				("DirecpayResponseURL"),
	DIRECPAY_STATUS_ENQUIRY			("DirecpayStatusEnquiry"),
	DIRECPAY_MID					("DirecpayMID"),
	DIRECPAY_MERCHANT_KEY			("DirecpayMerchantKey"),
	DIRECPAY_REFUND_URL				("DirecpayRefundUrl"),
	
	
	//General Configurations
	DEFAULT_CURRENCY					("DefaultCurrencyCode"),
	DEFAULT_RETURN_URL              	("DefaultReturnUrl"),
    LOCATE_COUNTRY_NAME					("LocateCountryName"),
    SEND_POSTBACK_FLAG					("SendPostBackFlag"),
    TASK_SCHEDULAR_RUNNING_DELAY        ("TaskSchedularRunningDelay"),
    TASK_SCHEDULAR_RUNNING_INTERVAL     ("TaskSchedularRunningInterval"),
    JOB_SCHEDULAR_RUNNING_ON_HOUR 		("JobSchedularRunningOnHour"),
    JOB_SCHEDULAR_RUNNING_ON_MINUTE 	("JobSchedularRunningOnMinute"),
    JOB_SCHEDULAR_CRON_EXPRESSION_FOR_MINUTE_INTERVAL 	("JobSchedularCronExpressionForMinuteInterval"),
    INTERNAL_WEBSERVICE_ALLOWED_IP      ("InternalWebserviceAllowedIP"),
    // BIN API
    BIN_RANGE_IDENTIFIER_URL		("BinRangeFinder"),
    BIN_RANGE_CITRUS_API		    ("CitrusBinFlag"),
    CITRUS_BIN_RANGE_IDENTIFIER_URL ("CitrusBinRangeFinder"),

	//Amex fields
	AMEX_TRANSACTION_URL            ("AmexvirtualPaymentClientURL"),
	AMEX_SUPPORT_URL                ("AmexSupportTransactionUrlL"),
	AMEX_VPC_VERSION                ("Amexvpc_Version"),
	AMEX_VPC_ACCESSCODE             ("Amexvpc_AccessCode"),
	AMEX_VPC_GATEWAY                ("Amexvpc_gateway"),
	AMEX_VPC_LOCALE                 ("Amexvpc_Locale"),
	AMEX_VPC_COMMAND                ("Amexvpc_Command"),
	AMEX_VPC_MERCHANT               ("Amexvpc_Merchant"),
	AMEX_VPC_USER                   ("Amexvpc_User"),
	AMEX_VPC_PASSWORD               ("Amexvpc_Password"),
	AMEX_SECURE_SECRET              ("AmexsecureSecret"),
	AMEX_SUPPORT_TXN_FIELDS         ("AmexSupportTxnDefaultFields"),
	AMEX_EZEE_CLICK_TRANSACTION_URL ("EzeeClickPaymentURL"),
	AMEX_EZEE_CLICK_RETURN_URL      ("EzeeClickReturnUrl"),
	AMEX_EZEE_CLICK_SUPPORT_TXN_FIELDS   ("EzeeClickSupportTxnDefaultFields"),
	AMEX_EZEE_CLICK_STATUS_TXN_URL       ("EzeeClickStatusUrl"),
	DIRECPAY_LIVE_FLAG                   ("DirecPayLiveFlag"),
	
	//AxisMigs fields
	AXIS_MIGS_TRANSACTION_URL            ("AxisMigsvirtualPaymentClientURL"),
	AXIS_MIGS_SUPPORT_URL                ("AxisMigsSupportTransactionUrl"),
	AXIS_MIGS_SUPPORT_TXN_FIELDS         ("AxisMigsSupportTxnDefaultFields"),
	
	//BOB fields
	BOB_SALE_REQUEST_URL				("BobSaleUrl"),
	BOB_RESPONSE_URL					("bobresponseURL"),
	//HDFC fields
	HDFC_SALE_REQUEST_URL				("HdfcSaleUrl"),
	HDFC_RESPONSE_URL					("HdfcresponseURL"),
	
	//FSSPAY fields
	FSS_PAY_SALE_REQUEST_URL				("FssPaySaleUrl"),
	FSS_PAY_RESPONSE_URL					("FssPayResponseURL"),
	
	//BILLDESK fields
	BILLDESK_SALE_REQUEST_URL				("billDeskSaleUrl"),
	BILLDESK_RESPONSE_URL					("billDeskReturnUrl"),
	
	//Lyra Fields
	LYRA_SALE_REQUEST_URL				("LyraApiUrl"),
	LYRA_RESPONSE_URL					("LyraResponseURL"),
	
	//Kotak fields
	KOTAK_SALE_URL						("KotakSaleUrl"),
	
	//FirstData fields
	FIRSTDATA_MANDATORY_FIELDS_ENROLL		("FirstDataMandatoryFieldsEnroll"),
	FIRSTDATA_ENROLLMENT_URL				("FirstDataEnrollmentUrl"),
	ICICI_AUTHENTICATION_ID					("IciciAuthenticationId"),
	ICICI_AUTHENTICATION_PASSWORD			("IciciAuthenticationPassword"),
	IDFC_AUTHENTICATION_ID					("IdfcAuthenticationId"),
	IDFC_AUTHENTICATION_PASSWORD			("IdfcAuthenticationPassword"),
	FIRSTDATA_SCHEMAS_V1					("FirstDataSchemasV1"),
	FIRSTDATA_SCHEMAS_API					("FirstDataSchemasAPI"),
	FIRSTDATA_SCHEMAS_A1					("FirstDataSchemasA1"),
	FIRSTDATA_PARES_AUTHENTICATION_URL		("FirstDataParesAuthorizationUrl"),
	FIRSTDATA_AUTHORIZATION_URL				("FirstDataAuthorizationUrl"),
	FIRSTDATA_SUPPORT_TRANSACTION_URL		("FirstDataSupportTransactionUrl"),
	
	//Email Expiry fields
	EMAIL_EXPIRED_HOUR						("emailExpiredInHour"),
	EMAIL_EXPIRED_MINUTE					("emailExpiredInMinute"),
	
	CRYPTO_DECRYPTION_SERVICE_URL			("CryptoDecryptionServiceUrl"),
	CRYPTO_ENCRYPTION_SERVICE_URL			("CryptoEncryptionServiceUrl"),
	HOSTED_CRYPTO_DECRYPTION_SERVICE_URL	("HostedCryptoDecryptionServiceUrl"),
	HOSTED_CRYPTO_ENCRYPTION_SERVICE_URL	("HostedCryptoEncryptionServiceUrl"),
	
	//Callback Fields
	CALLBACK_FIELDS							("CallBackFields"),
	ENACHDEBIT_CALLBACK_FIELDS				("eNachDebitTxnCallbackfields"),
	UPIAUTOPAYTXN_CALLBACK_FIELDS			("UpiAutoPayTxnCallbackfields"),
	//ThreadPool Configuration 
	THREAD_POOL_CORE_POOL_SIZE ("corePoolSize"),
	THREAD_POOL_MAX_POOL_SIZE ("maxPoolSize"),
	THREAD_POOL_MAX_THREAD_ALIVE_TIME ("maxThreadAliveTime"),
	
	//FreeCharge Fields
	FREECHARGE_SALE_REQUEST_URL				("FREECHARGESaleUrl");

	private final String value;
	
	private ConfigurationConstants(String key){		
		this.value = key;
	}

	public String getValue() {
		String ymlValue=PropertiesManager.propertiesMap.get(value);
        if(StringUtils.isNotBlank(ymlValue)){
            return ymlValue;
        }
        return new PropertiesManager().propertiesMap.get(value);
	}	
		
	public int getValues() {
		int intVal = Integer.parseInt(PropertiesManager.propertiesMap.get(value));
		return intVal ;
	}
}
