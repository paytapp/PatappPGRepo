package com.paymentgateway.qaicash;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;

/**
 * @author Shaiwal
 *
 */

@Service("qaicashTransaction")
public class Transaction {

	private String merchantApiKey;
	private String merchantId;
	private String currency;
	private String orderId;
	private String amount;
	private String dateTime;
	private String language;
	private String depositorUserId;
	private String depositMethod;
	private String redirectUrl;
	private String callbackUrl;
	private String messageAuthenticationCode;
	private String transactionId;
	private String status;
	private String processingCurrency;
	private String processingAmount;
	private String paymentPageUrl;
	private String success;
	private String notes;
	private String failureReason;
	private String depositorName;
	
	public void setEnrollment(Fields fields) {
		setMerchantInformation(fields);
		setTxnDataDetails(fields);
	}
	
	public void setStatusFields(Fields fields) {
		setMerchantInformation(fields);
		setTxnStatusDataDetails(fields);
	}

	private void setMerchantInformation(Fields fields) {
		
		setMerchantId(fields.get(FieldType.MERCHANT_ID.getName()));
		setMerchantApiKey(fields.get(FieldType.TXN_KEY.getName()));
	}

	private void setTxnStatusDataDetails(Fields fields) {
	    setOrderId(fields.get(FieldType.PG_REF_NUM.getName()));
	}
	
	private void setTxnDataDetails(Fields fields) {

		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");  
	    String strDate= formatter.format(date);  
	    
	    
	    setCurrency("INR");
	    setOrderId(fields.get(FieldType.PG_REF_NUM.getName()));
		setDateTime(strDate);
		setLanguage("en-Us");
		
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			setDepositorUserId(fields.get(FieldType.CUST_EMAIL.getName()));
		}
		else {
			setDepositorUserId(fields.get(FieldType.PG_REF_NUM.getName()));
		}
		
		if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())) {
			setDepositMethod("UPI");
		}
		else if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
			setDepositMethod("BANK_TRANSFER");
			
			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))) {
				setDepositorName(fields.get(FieldType.CUST_NAME.getName()));
			}
			else {
				setDepositorName("John Doe");
			}
			
		}
		
	}
	
	
	public String getMerchantApiKey() {
		return merchantApiKey;
	}
	public void setMerchantApiKey(String merchantApiKey) {
		this.merchantApiKey = merchantApiKey;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getDateTime() {
		return dateTime;
	}
	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getDepositorUserId() {
		return depositorUserId;
	}
	public void setDepositorUserId(String depositorUserId) {
		this.depositorUserId = depositorUserId;
	}
	public String getDepositMethod() {
		return depositMethod;
	}
	public void setDepositMethod(String depositMethod) {
		this.depositMethod = depositMethod;
	}
	public String getRedirectUrl() {
		return redirectUrl;
	}
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
	public String getCallbackUrl() {
		return callbackUrl;
	}
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	public String getMessageAuthenticationCode() {
		return messageAuthenticationCode;
	}
	public void setMessageAuthenticationCode(String messageAuthenticationCode) {
		this.messageAuthenticationCode = messageAuthenticationCode;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getProcessingCurrency() {
		return processingCurrency;
	}
	public void setProcessingCurrency(String processingCurrency) {
		this.processingCurrency = processingCurrency;
	}
	public String getProcessingAmount() {
		return processingAmount;
	}
	public void setProcessingAmount(String processingAmount) {
		this.processingAmount = processingAmount;
	}
	public String getPaymentPageUrl() {
		return paymentPageUrl;
	}
	public void setPaymentPageUrl(String paymentPageUrl) {
		this.paymentPageUrl = paymentPageUrl;
	}
	public String getSuccess() {
		return success;
	}
	public void setSuccess(String success) {
		this.success = success;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getFailureReason() {
		return failureReason;
	}
	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public String getDepositorName() {
		return depositorName;
	}

	public void setDepositorName(String depositorName) {
		this.depositorName = depositorName;
	}

	
	
}
