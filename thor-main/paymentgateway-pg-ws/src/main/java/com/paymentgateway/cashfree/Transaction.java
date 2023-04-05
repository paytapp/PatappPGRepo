package com.paymentgateway.cashfree;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.Fields;

@Service("cashfreeTransaction")
public class Transaction {
	
	private String appId;
	private String orderId;
	private String orderAmount;
	private String orderCurrency;
	private String customerName;
	private String customerEmail;
	private String customerPhone;
	private String paymentOption;
	private String paymentCode;
	private String referenceId;
	private String txStatus;
	private String paymentMode;
	private String txMsg;
	private String signature;
	private String message;
	private String refundId;
	private String status;
	private String orderStatus;
	
	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderAmount() {
		return orderAmount;
	}

	public void setOrderAmount(String orderAmount) {
		this.orderAmount = orderAmount;
	}

	public String getOrderCurrency() {
		return orderCurrency;
	}

	public void setOrderCurrency(String orderCurrency) {
		this.orderCurrency = orderCurrency;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public String getCustomerPhone() {
		return customerPhone;
	}

	public void setCustomerPhone(String customerPhone) {
		this.customerPhone = customerPhone;
	}

	public String getPaymentOption() {
		return paymentOption;
	}

	public void setPaymentOption(String paymentOption) {
		this.paymentOption = paymentOption;
	}

	public String getPaymentCode() {
		return paymentCode;
	}

	public void setPaymentCode(String paymentCode) {
		this.paymentCode = paymentCode;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getTxStatus() {
		return txStatus;
	}

	public void setTxStatus(String txStatus) {
		this.txStatus = txStatus;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public String getTxMsg() {
		return txMsg;
	}

	public void setTxMsg(String txMsg) {
		this.txMsg = txMsg;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getRefundId() {
		return refundId;
	}

	public void setRefundId(String refundId) {
		this.refundId = refundId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}
	
	/*
	 * private void setCardDetails(Fields fields) {
	 * setCardNumber(fields.get(FieldType.CARD_NUMBER.getName()));
	 * setCvv(fields.get(FieldType.CVV.getName())); String expDate =
	 * fields.get(FieldType.CARD_EXP_DT.getName()); String expYear =
	 * (expDate.substring(4, 6)); String expMonth = (expDate.substring(0, 2));
	 * setExpiryDate(expMonth.concat(expYear)); }
	 */
	
	/*
	 * private void setTxnType(Fields fields) { String txnType =
	 * fields.get(FieldType.TXNTYPE.getName());
	 * if(txnType.equals(TransactionType.SALE.toString())){ setTxnType("01"); }else
	 * if(txnType.equals(TransactionType.REFUND.toString())){ setTxnType("04");
	 * }else if(txnType.equals(TransactionType.ENQUIRY.toString())){
	 * setTxnType("05"); }else{
	 * 
	 * } }
	 */


}
