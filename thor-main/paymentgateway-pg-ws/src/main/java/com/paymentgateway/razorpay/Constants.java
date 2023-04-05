package com.paymentgateway.razorpay;

final class Constants {

	private Constants() {

	}

	static final String RAZORPAY_ORDER_GEN_URL = "RAZORPAYOrderGenUrl";
	static final String RAZORPAY_SALE_REQUEST_URL = "RAZORPAYSaleUrl";
	static final String RAZORPAY_REFUND_URL = "RAZORPAYRefundUrl";
	static final String RAZORPAY_STATUS_ENQ_URL = "RAZORPAYStatusEnqUrl";
	static final String RAZORPAY_RESPONSE_URL = "RAZORPAYReturnUrl";

	static final String STATUS_ENQ_SUCCESS_CODE = "true";
	static final String REFUND_SUCCESS_CODE = "processed";
	static final String REFUND_PENDING_CODE = "pending";
	
	static final String NET_BANKING = "netbanking";
	static final String CARD = "card";
	static final String WALLET = "wallet";
	static final String UPI = "upi";
	static final String INR = "INR";
}
