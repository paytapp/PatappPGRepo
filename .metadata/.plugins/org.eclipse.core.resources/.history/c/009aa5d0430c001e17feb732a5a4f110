package com.paymentgateway.phonePe;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusCheckDataBean {
	
	public String merchantId;
	public String merchantTransactionId;
	public String transactionId;
	public String amount;
	public String state;
	public String responseCode;
	public StatusCheckPaymentInstrumentBean paymentInstrument;
	
	@JsonGetter("merchantId")
	public String getMerchantId() {
		return merchantId;
	}
	
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	
	@JsonGetter("merchantTransactionId")
	public String getMerchantTransactionId() {
		return merchantTransactionId;
	}
	
	public void setMerchantTransactionId(String merchantTransactionId) {
		this.merchantTransactionId = merchantTransactionId;
	}
	
	@JsonGetter("transactionId")
	public String getTransactionId() {
		return transactionId;
	}
	
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	
	@JsonGetter("amount")
	public String getAmount() {
		return amount;
	}
	
	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	@JsonGetter("state")
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	@JsonGetter("responseCode")
	public String getResponseCode() {
		return responseCode;
	}
	
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	
	@JsonGetter("paymentInstrument")
	public StatusCheckPaymentInstrumentBean getPaymentInstrument() {
		return paymentInstrument;
	}
	
	public void setPaymentInstrument(StatusCheckPaymentInstrumentBean paymentInstrument) {
		this.paymentInstrument = paymentInstrument;
	}

	
}
