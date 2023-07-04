package com.paymentgateway.phonePe;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusCheckDataBean {
	
	public String merchantId;
	public String merchantTransactionId;
	public String transactionId;
	public BigDecimal amount;
	public String state;
	public String responseCode;
	public StatusCheckPaymentInstrumentBean paymentInstrument;
	
	
	
	public StatusCheckDataBean(String merchantId, String merchantTransactionId, String transactionId, BigDecimal amount,
			String state, String responseCode, StatusCheckPaymentInstrumentBean paymentInstrument) {
		super();
		this.merchantId = merchantId;
		this.merchantTransactionId = merchantTransactionId;
		this.transactionId = transactionId;
		this.amount = amount;
		this.state = state;
		this.responseCode = responseCode;
		this.paymentInstrument = paymentInstrument;
	}

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
	public BigDecimal getAmount() {
		return amount;
	}
	
	public void setAmount(BigDecimal amount) {
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
