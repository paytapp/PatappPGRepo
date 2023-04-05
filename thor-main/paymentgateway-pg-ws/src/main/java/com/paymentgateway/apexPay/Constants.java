package com.paymentgateway.apexPay;

public class Constants {

	
	public static final String APEXPAY_SALE_REQUEST_URL = "ApexPaySaleUrl";
	public static final String APEXPAY_REFUND_URL = "ApexPayRefundUrl";
	public static final String APEXPAY_STATUS_ENQ_URL = "ApexPayStatusEnqUrl";
	public static final String APEXPAY_RESPONSE_URL = "ApexPayReturnUrl";
	
	public static final String REF_TXN_TYPE = "REFUND";
	
	
	public static final String MERCHANT_ID = "merchantId";
	public static final String TRANSACTION_ID = "transactionId";
	public static final String MERCHANT_USER_ID = "merchantUserId";
	public static final String AMOUNT = "amount";
	public static final String MERCHANT_ORDER_ID = "merchantOrderId";
	public static final String MOBILE_NUMBER = "mobileNumber";
	public static final String MESSAGE = "message";
	public static final String SUB_MERCHANT = "subMerchant";
	public static final String EMAIL = "email";
	public static final String SHORT_NAME = "shortName";
	public static final String REQUEST = "request";
	public static final String ORIG_TXN_ID = "originalTransactionId";
	public static final String PROVIDER_REF_ID = "providerReferenceId";
	
	public static final String SUCCESS = "success";
	public static final String CODE = "code";
	public static final String STATUS = "status";
	public static final String PAY_RESP_CODE = "payResponseCode";
	
	public static final String SEPARATOR = "###";
	
	public static final String SALE_TXN_PREFIX = "/v4/debit";
	public static final String REFUND_TXN_PREFIX = "/v3/credit/backToSource";
	public static final String STATUS_TXN_PREFIX = "/v3/transaction/";
	
	
	public static final String REDIRECT_MODE = "POST";
}
