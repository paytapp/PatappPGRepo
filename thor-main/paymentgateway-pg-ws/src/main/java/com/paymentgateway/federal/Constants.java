package com.paymentgateway.federal;

/**
 * @author Rahul
 *
 */
public class Constants {

	public static final String PIPE_SEPARATOR = "|";
	public static final String SEPARATOR = "&";
	public static final String EQUATOR = "=";
	public static final String PG_INSTANCE_ID = "pg_instance_id";
	public static final String MERCHANT_ID = "merchant_id";
	public static final String PERFORM = "perform";
	public static final String PAN = "pan";
	public static final String EXP_YEAR = "exp_date_yyyy";
	public static final String EXP_MONTH = "exp_date_mm";
	public static final String CVV = "cvv2";
	public static final String NAME_ON_CARD = "name_on_card";
	public static final String AMOUNT = "amount";
	public static final String MERCHNAT_REF_NO = "merchant_reference_no";
	public static final String ORDER_DESC = "order_desc";
	public static final String MPI_ID = "mpiId";
	public static final String STATUS = "status";
	public static final String ECI = "eci";
	public static final String XID = "xid";
	public static final String CAVV = "cavv";
	public static final String MERCHANT_HASH = "message_hash";
	public static final String ORIGINAL_TXN_ID = "orginal_transaction_id";
	public static final String ORIGINAL_MERCHANT_REF_NO = "original_merchant_reference_no";
	public static final String NEW_MERCHANT_REF_NO = "new_merchant_reference_no";
	public static final String LOGIN_ID = "login_id";
	public static final String REFUND_TYPE = "Refund Type";
	public static final String CURRENCY_CODE = "currency_code";
	public static final String TRANSACTION_TYPE = "transaction_type";
	public static final String CUSTOMER_DEVICE_ID = "customer_device_id";
	public static final String COLLECT_REQ_BODY_HEADER = "SERVICE-POSTMPI:17:";
	public static final String REFUND_REQ_BODY_HEADER = "MERCHANT-API-HTTPS:7:";
	public static final String ENQ_REQ_BODY_HEADER = "CURRENCY:7:";
	public static final String PAYMENT_GATEWAY_REFUND_FLAG = "R";
	public static final String REFUND_INCLUDING_SURCHARGE_FLAG = "RF";
	public static final String REFUND_TXN_AMOUNT_FLAG = "CN";
	public static final String TXN_ID = "transaction_id";
	public static final String PG_ERROR_CODE = "pg_error_code";
	public static final String PG_ERROR_DETAILS = "pg_error_detail";
	public static final String APPROVAL_CODE = "approval_code";
	public static final String ZERO_VALUE = "0";
	public static final String FEDERAL_ADD_SURCHARGE_FLAG = "FEDERAL_ADD_SURCHARGE";
	
	//UPI FIelds
	
	public static final String TRANSACTION_ID = "TransactionId";
	public static final String REMARKS = "Remarks";
	public static final String TRANSACTION_REFERENCE_ID = "TransactionReferenceId";
	public static final String CUSTOMER_REFERENCE = "CustomerReference";
	public static final String TXN_TYPE = "TransactionType";
	public static final String EXPIRY_TIME = "ExpiryTime";
	public static final String MERCHANT_CATEGORY_CODE = "MerchantCategoryCode";
	public static final String PAYER_ADDRESS = "PayerAddress";
	public static final String PAYER_NAME = "PayerName";
	public static final String PAYEE_NAME = "PayeeName";
	public static final String AMT = "Amount";
	public static final String PAYEE_ADDRESS = "PayeeAddress";
	public static final String PAYEE_TYPE = "PayeeType";
	public static final String PAYER_TYPE = "PayerType";
	public static final String SENDER_USER_ID = "SenderUserId";
	public static final String SENDER_PASSWORD = "SenderPassword";
	public static final String SENDER_CODE = "SenderCode";
	public static final String REQ_PAY = "ReqPay";
	public static final String MERCHANT_BODY = "MerchantBody";
	public static final String MERCHANT_HEADER = "MerchantHeader";
	public static final String UPI_PAYMENT_REQ = "UPIPaymentReq";
	public static final String REQ_VAL_ADD = "ReqValAdd";
	
	public static final String TRAN_ENQ_BODY = "TranEnqBody";
	public static final String TRAN_ENQ_HEADER = "TranEnqHeader";
	public static final String TRANSACTION_ENQ_REQ = "TransactionEnqReq";
	public static final String UPI_SUCCESS_CODE = "00";
	public static final String ACK = "ACK";
	public static final String RESP_VAL_ADD = "RespValAdd";
	public static final String RESPONSE = "Response";
	public static final String RESPONSE_CODE = "ResponseCode";
	public static final String PAYER_APPROVAL_NUM = "PayerApprovalNum";
	public static final String PAYEE_APPROVAL_NUM = "PayeeApprovalNum";
	public static final String VPA_VALIDATE_FLAG = "Y";
	public static final String REFUND_DESCRIPTION_MESSAGE ="Refund initated";
	
	public static final String JKS_PASSWORD = "changeit";
	
	// Properties fields
	public static final String FEDERAL_ENROLLMENT_URL = "FederalEnrollmentUrl";
	public static final String FEDERAL_SALE_PERFORM = "FederalSalePerform";
	public static final String FEDERAL_API_MERCHANT_ID = "FederalAPIMerchantId";
	public static final String FEDERAL_API_PG_INSTANCE_ID = "FederalAPIPGInstanceId";
	public static final String FEDERAL_API_DEVICE_ID = "FederalAPIDeviceId";
	public static final String FEDERAL_LOGIN_KEY = "FederalLoginKey";
	public static final String FEDERAL_REFUND_PERFORM = "FederalRefundPerform";
	public static final String FEDERAL_STATUS_ENQ_PERFORM = "FederalStatusEnquiryPerform";
	public static final String FEDERAL_HASH_ALGO = "FederalHash_algorithm";
	
	public static final String FEDERAL_UPI_SALE_URL = "FederalUPISaleUrl";
	public static final String FEDERAL_UPI_REFUND_URL = "FederalUPIRefundUrl";
	public static final String FEDERAL_UPI_STATUS_ENQ_URL = "FederalUPIStatusEnqUrl";
	public static final String FEDERAL_SALE_URL = "FederalSaleUrl";
	public static final String FEDERAL_REFUND_URL = "FederalRefundUrl";
	public static final String FEDERAL_STATUS_ENQ_URL = "FederalStatusEnqUrl";
	public static final String VPA_VALIDATOR_FLAG = "FederalUpiValidatorFlag";
	
	public static final String ICICI_MPGS_ENROLLMENT_URL = "IciciMpgsEnrollmentUrl";
	public static final String ICICI_MPGS_SALE_URL = "IciciMpgsSaleUrl";
	public static final String ICICI_MPGS_REFUND_URL = "IciciMpgsRefundUrl";
	public static final String ICICI_MPGS_STATUS_ENQ_URL = "IciciMpgsStatusEnqUrl";
	
	public static final String UPI_ENV_FLAG = "FederalUPIENVFLAG";
	public static final String UAT_ENV = "UAT";
	public static final String PROD_ENV = "PROD";
	public static final String UPI_UAT_PREFIX_FLAG = "FederalUPIUATPREFIX";
	public static final String UPI_PROD_PREFIX_FLAG = "FederalUPIPROPREFIX";
	
	public static final String PG_PROPERTIES_PATH = "PG_PROPS";
	public static final String JKS_FILE_NAME = "cacerts";
	public static final String PAYMENT_GATEWAY_JKS_FILE_NAME = "paymentGateway.jks";
	
}
