package com.paymentgateway.commons.user;

import java.math.BigDecimal;

public class ViewSurchargePopulator {

	private String paymentType;
	private String payId;
	private String acquirerName;
	private String onOff;	
	private String status;
	private String mopType;
	private String merchantIndustryType;
	private boolean allowOnOff;
	private BigDecimal bankSurchargeAmountOnCommercial;
	private BigDecimal bankSurchargeAmountOnCustomer;
	private BigDecimal bankSurchargePercentageOnCommercial;
	private BigDecimal bankSurchargePercentageOnCustomer;
	private BigDecimal bankSurchargeAmountOffCommercial;
	private BigDecimal bankSurchargeAmountOffCustomer;
	private BigDecimal bankSurchargePercentageOffCommercial;
	private BigDecimal bankSurchargePercentageOffCustomer;
	
	private AccountCurrencyRegion paymentsRegion;
	private CardHolderType cardHolderType;
	
	private String merchantName;
	private String requestedBy;
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getAcquirerName() {
		return acquirerName;
	}
	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}
	public String getOnOff() {
		return onOff;
	}
	public void setOnOff(String onOff) {
		this.onOff = onOff;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMopType() {
		return mopType;
	}
	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
	public String getMerchantIndustryType() {
		return merchantIndustryType;
	}
	public void setMerchantIndustryType(String merchantIndustryType) {
		this.merchantIndustryType = merchantIndustryType;
	}
	public boolean isAllowOnOff() {
		return allowOnOff;
	}
	public void setAllowOnOff(boolean allowOnOff) {
		this.allowOnOff = allowOnOff;
	}
	public BigDecimal getBankSurchargeAmountOnCommercial() {
		return bankSurchargeAmountOnCommercial;
	}
	public void setBankSurchargeAmountOnCommercial(BigDecimal bankSurchargeAmountOnCommercial) {
		this.bankSurchargeAmountOnCommercial = bankSurchargeAmountOnCommercial;
	}
	public BigDecimal getBankSurchargeAmountOnCustomer() {
		return bankSurchargeAmountOnCustomer;
	}
	public void setBankSurchargeAmountOnCustomer(BigDecimal bankSurchargeAmountOnCustomer) {
		this.bankSurchargeAmountOnCustomer = bankSurchargeAmountOnCustomer;
	}
	public BigDecimal getBankSurchargePercentageOnCommercial() {
		return bankSurchargePercentageOnCommercial;
	}
	public void setBankSurchargePercentageOnCommercial(BigDecimal bankSurchargePercentageOnCommercial) {
		this.bankSurchargePercentageOnCommercial = bankSurchargePercentageOnCommercial;
	}
	public BigDecimal getBankSurchargePercentageOnCustomer() {
		return bankSurchargePercentageOnCustomer;
	}
	public void setBankSurchargePercentageOnCustomer(BigDecimal bankSurchargePercentageOnCustomer) {
		this.bankSurchargePercentageOnCustomer = bankSurchargePercentageOnCustomer;
	}
	public BigDecimal getBankSurchargeAmountOffCommercial() {
		return bankSurchargeAmountOffCommercial;
	}
	public void setBankSurchargeAmountOffCommercial(BigDecimal bankSurchargeAmountOffCommercial) {
		this.bankSurchargeAmountOffCommercial = bankSurchargeAmountOffCommercial;
	}
	public BigDecimal getBankSurchargeAmountOffCustomer() {
		return bankSurchargeAmountOffCustomer;
	}
	public void setBankSurchargeAmountOffCustomer(BigDecimal bankSurchargeAmountOffCustomer) {
		this.bankSurchargeAmountOffCustomer = bankSurchargeAmountOffCustomer;
	}
	public BigDecimal getBankSurchargePercentageOffCommercial() {
		return bankSurchargePercentageOffCommercial;
	}
	public void setBankSurchargePercentageOffCommercial(BigDecimal bankSurchargePercentageOffCommercial) {
		this.bankSurchargePercentageOffCommercial = bankSurchargePercentageOffCommercial;
	}
	public BigDecimal getBankSurchargePercentageOffCustomer() {
		return bankSurchargePercentageOffCustomer;
	}
	public void setBankSurchargePercentageOffCustomer(BigDecimal bankSurchargePercentageOffCustomer) {
		this.bankSurchargePercentageOffCustomer = bankSurchargePercentageOffCustomer;
	}
	public AccountCurrencyRegion getPaymentsRegion() {
		return paymentsRegion;
	}
	public void setPaymentsRegion(AccountCurrencyRegion paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}
	public CardHolderType getCardHolderType() {
		return cardHolderType;
	}
	public void setCardHolderType(CardHolderType cardHolderType) {
		this.cardHolderType = cardHolderType;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getRequestedBy() {
		return requestedBy;
	}
	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}
	
	
	
	
}
