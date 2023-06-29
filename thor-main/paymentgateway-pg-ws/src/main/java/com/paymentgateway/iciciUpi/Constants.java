package com.paymentgateway.iciciUpi;

/**
 * @author Rahul, Amitosh
 *
 */
public class Constants {

	public static final String ICICI_UPI_SALE_URL = "ICICI_UPI_SALE_URL";
	public static final String ICICI_UPI_REFUND_URL = "ICICI_UPI_REFUND_URL";
	public static final String ICICI_UPI_STATUS_ENQ_URL = "ICICI_UPI_STATUS_ENQ_URL";
	public static final String PAYER_VA="payerVa";
	public static final String PAYEE_VA="payeeVA";
	public static final String AMOUNT = "amount";
	public static final String NOTE = "note";
	public static final String COLLECT_PAY_REQUEST = "collect-pay-request";
	public static final String COLLECT_BY_DATE = "collectByDate";
	public static final String MERCHANT_ID = "merchantId";
	public static final String MERCHANT_NAME = "merchantName";
	public static final String SUB_MERCHANT_ID = "subMerchantId";
	public static final String SUB_MERCHANT_NAME = "subMerchantName";
	public static final String TERMINAL_ID = "terminalId";
	public static final String MERCHANT_TRANSACTION_ID = "merchantTranId";
	public static final String BILL_NUMBER = "billNumber";
	public static final String PG_PROPERTIES_PATH = "PG_PROPS";
	public static final String PUBLIC_KEY_FILE_NAME = System.getenv(Constants.PG_PROPERTIES_PATH) + "icici_upi_rsa_apikey.cer";
	public static final String PRIVATE_KEY_FILE_NAME = System.getenv(Constants.PG_PROPERTIES_PATH) + "PaymentGatewayPrivateKey.pfx";
	public static final String RESPONSE_CODE = "response";
	public static final String TRANSACTION_INITIATED_RESPONSE_CODE = "92";
	public static final String NO_RESSPONSE_RECEIVED = "NO_RESSPONSE_RECEIVED";
	public static final String BANK_RRN = "originalBankRRN";
	public static final String ORIGINAL_MERCHANT_TRANSACTION_ID = "originalmerchantTranId";
	public static final String REFUND_AMOUNT = "refundAmount";
	public static final String REFUND_NOTE = "refund-request";
	public static final String ONLINE_REFUND = "onlineRefund";
	public static final String Y = "Y";
	public static final String N = "N";
	public static final String SUCCESS_FLAG = "success";
	public static final String STATUS = "status";
	public static final String SUCCESS = "SUCCESS";
	public static final String RESPONSE = "response";
	public static final String MESSAGE = "message";
	public static final String ICICI_UPI_STATUS_ENQUIRY_URL = "ICICI_UPI_STATUS_ENQUIRY_URL";
	public static final String BANK_RRN1 = "OriginalBankRRN";
	public static final int COLLECT_REQUEST_TIMEOUT_MINUTES = 15;
	public static final String UPI = "upi";
	public static final String COLON = ":";
	public static final String SLASH_MARK = "/";
	public static final String PAY = "pay";
	public static final String QUESTION_MARK = "?";
	public static final String EQUATOR = "=";
	public static final String AMPERSAND = "&";
	public static final String PA = "pa";
	public static final String PN = "pn";
	public static final String TR = "tr"; 
	public static final String AM = "am";
	public static final String CU = "cu";
	public static final String MC = "mc";
	public static final String UPI_QR_PREFIX = "ICICI_UPI_QR_PREFIX";
	public static final int QR_SIZE = 200;
	public static final String QR_FILE_TYPE = "png";
	public static final String PNG_IMAGE_PREFIX = "data:image/png;base64, ";
}
