package com.paymentgateway.pgui.action.service;

public class Constants {

	public static final String PAYMENT_GATEWAY_SEPARATOR = "|";
	public static final String EQUATOR = "=";
	public static final String MERCHANT_CODE = "merchantCode";
	public static final String RESERVATION_ID = "reservationId";
	public static final String TXN_AMOUNT = "txnAmount";
	public static final String CURRENCY_TYPE = "currencyType";
	public static final String APP_CODE = "appCode";
	public static final String PAYMENT_MODE = "pymtMode";
	public static final String TXN_DATE = "txnDate";
	public static final String SECURITY_ID = "securityId";
	public static final String RESPONSE_URL = "RU";
	public static final String CHECKSUM = "checkSum";
	public static final String PAYMENT_GATEWAY_SECURITY_ID = "CRIS";
	public static final String PARAMETERS = "?encdata";
	public static final String CHECKSUM_LOGIC = "SHA-256";
	public static final String ENCODING = "UTF-8";
	
	public static final String SALE_RESERVATION_ID = "saleReservationId";
	public static final String REFUND_RESERVATION_ID = "refundReservationId";
	public static final String REFUND_FLAG = "refundFlag";
	public static final String REFUND_AMOUNT = "refundAmount";
	public static final String SALE_BANK_TXN_ID = "saleBankTxnId";
	public static final String CANCELLATION_DATE = "cancellationDate";
	public static final String REFUND_INITIATION_ID = "refundInitiationId";
	
	public static final String PAYMENT_GATEWAY_RESPONSE_SEPARATOR = "\\|";
	public static final String BANK_TXN_ID = "bankTxnId";
	public static final String STATUS = "status";
	public static final String STATUS_DESC = "statusDesc";
	public static final String SUCCESS = "Success";
	
	public static final String PAYMENT_GATEWAY_RETURN_URL = "PaymentGatewayIntegrationReturnUrl";
	public static final String PAYMENT_GATEWAY_REQUEST_URL_APPENDER = "_RequestUrl";
	public static final String PAYMENT_GATEWAY_ENQUIRY_URL_APPENDER = "_EnquiryUrl";
	public static final String PAYMENT_GATEWAY_REFUND_URL_APPENDER = "_RefundUrl";
}
