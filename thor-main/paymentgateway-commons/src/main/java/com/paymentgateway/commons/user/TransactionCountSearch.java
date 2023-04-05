package com.paymentgateway.commons.user;

import java.io.Serializable;

public class TransactionCountSearch implements Serializable {

	private static final long serialVersionUID = 4686158967332816200L;

	private String merchantName;
	private String saleSettledAmount;
	private String saleSettledCount;
	private String pgSaleSurcharge;
	private String acquirerSaleSurcharge;
	private String pgSaleGst;
	private String acquirerSaleGst;
	private String refundSettledAmount;
	private String refundSettledCount;
	private String pgRefundSurcharge;
	private String acquirerRefundSurcharge;
	private String pgRefundGst;
	private String acquirerRefundGst;
	private String totalMerchantAmount;
	private String acquirer;

	// For GST and Surcharge calculation
	private String surchargeFlag;
	private String payId;
	private String mopType;
	private String paymentType;
	private String txnType;
	private String paymentsRegion;
	private String cardHolderType;
	private String amount;
	private String paymentMethod;
	private String totalAmount;

	private String ccSettledPercentage;
	private String dcSettledPercentage;
	private String upSettledPercentage;
	private String nbSettledPercentage;
	private String wlSettledPercentage;
	private String emSettledPercentage;
	private String cdSettledPercentage;

	private String avgSettlementAmount;
	private String merchantSaleSettledAmount;
	private String merchantRefundSettledAmount;

	private String totalProfit;
	private String postSettledTransactionCount;
	private String actualSettlementAmount;
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getSaleSettledAmount() {
		return saleSettledAmount;
	}
	public void setSaleSettledAmount(String saleSettledAmount) {
		this.saleSettledAmount = saleSettledAmount;
	}
	public String getSaleSettledCount() {
		return saleSettledCount;
	}
	public void setSaleSettledCount(String saleSettledCount) {
		this.saleSettledCount = saleSettledCount;
	}
	public String getPgSaleSurcharge() {
		return pgSaleSurcharge;
	}
	public void setPgSaleSurcharge(String pgSaleSurcharge) {
		this.pgSaleSurcharge = pgSaleSurcharge;
	}
	public String getAcquirerSaleSurcharge() {
		return acquirerSaleSurcharge;
	}
	public void setAcquirerSaleSurcharge(String acquirerSaleSurcharge) {
		this.acquirerSaleSurcharge = acquirerSaleSurcharge;
	}
	public String getPgSaleGst() {
		return pgSaleGst;
	}
	public void setPgSaleGst(String pgSaleGst) {
		this.pgSaleGst = pgSaleGst;
	}
	public String getAcquirerSaleGst() {
		return acquirerSaleGst;
	}
	public void setAcquirerSaleGst(String acquirerSaleGst) {
		this.acquirerSaleGst = acquirerSaleGst;
	}
	public String getRefundSettledAmount() {
		return refundSettledAmount;
	}
	public void setRefundSettledAmount(String refundSettledAmount) {
		this.refundSettledAmount = refundSettledAmount;
	}
	public String getRefundSettledCount() {
		return refundSettledCount;
	}
	public void setRefundSettledCount(String refundSettledCount) {
		this.refundSettledCount = refundSettledCount;
	}
	public String getPgRefundSurcharge() {
		return pgRefundSurcharge;
	}
	public void setPgRefundSurcharge(String pgRefundSurcharge) {
		this.pgRefundSurcharge = pgRefundSurcharge;
	}
	public String getAcquirerRefundSurcharge() {
		return acquirerRefundSurcharge;
	}
	public void setAcquirerRefundSurcharge(String acquirerRefundSurcharge) {
		this.acquirerRefundSurcharge = acquirerRefundSurcharge;
	}
	public String getPgRefundGst() {
		return pgRefundGst;
	}
	public void setPgRefundGst(String pgRefundGst) {
		this.pgRefundGst = pgRefundGst;
	}
	public String getAcquirerRefundGst() {
		return acquirerRefundGst;
	}
	public void setAcquirerRefundGst(String acquirerRefundGst) {
		this.acquirerRefundGst = acquirerRefundGst;
	}
	public String getTotalMerchantAmount() {
		return totalMerchantAmount;
	}
	public void setTotalMerchantAmount(String totalMerchantAmount) {
		this.totalMerchantAmount = totalMerchantAmount;
	}
	public String getAcquirer() {
		return acquirer;
	}
	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}
	public String getSurchargeFlag() {
		return surchargeFlag;
	}
	public void setSurchargeFlag(String surchargeFlag) {
		this.surchargeFlag = surchargeFlag;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getMopType() {
		return mopType;
	}
	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public String getPaymentsRegion() {
		return paymentsRegion;
	}
	public void setPaymentsRegion(String paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}
	public String getCardHolderType() {
		return cardHolderType;
	}
	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public String getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}
	public String getCcSettledPercentage() {
		return ccSettledPercentage;
	}
	public void setCcSettledPercentage(String ccSettledPercentage) {
		this.ccSettledPercentage = ccSettledPercentage;
	}
	public String getDcSettledPercentage() {
		return dcSettledPercentage;
	}
	public void setDcSettledPercentage(String dcSettledPercentage) {
		this.dcSettledPercentage = dcSettledPercentage;
	}
	public String getUpSettledPercentage() {
		return upSettledPercentage;
	}
	public void setUpSettledPercentage(String upSettledPercentage) {
		this.upSettledPercentage = upSettledPercentage;
	}
	public String getNbSettledPercentage() {
		return nbSettledPercentage;
	}
	public void setNbSettledPercentage(String nbSettledPercentage) {
		this.nbSettledPercentage = nbSettledPercentage;
	}
	public String getWlSettledPercentage() {
		return wlSettledPercentage;
	}
	public void setWlSettledPercentage(String wlSettledPercentage) {
		this.wlSettledPercentage = wlSettledPercentage;
	}
	public String getEmSettledPercentage() {
		return emSettledPercentage;
	}
	public void setEmSettledPercentage(String emSettledPercentage) {
		this.emSettledPercentage = emSettledPercentage;
	}
	public String getCdSettledPercentage() {
		return cdSettledPercentage;
	}
	public void setCdSettledPercentage(String cdSettledPercentage) {
		this.cdSettledPercentage = cdSettledPercentage;
	}
	public String getAvgSettlementAmount() {
		return avgSettlementAmount;
	}
	public void setAvgSettlementAmount(String avgSettlementAmount) {
		this.avgSettlementAmount = avgSettlementAmount;
	}
	public String getMerchantSaleSettledAmount() {
		return merchantSaleSettledAmount;
	}
	public void setMerchantSaleSettledAmount(String merchantSaleSettledAmount) {
		this.merchantSaleSettledAmount = merchantSaleSettledAmount;
	}
	public String getMerchantRefundSettledAmount() {
		return merchantRefundSettledAmount;
	}
	public void setMerchantRefundSettledAmount(String merchantRefundSettledAmount) {
		this.merchantRefundSettledAmount = merchantRefundSettledAmount;
	}
	public String getTotalProfit() {
		return totalProfit;
	}
	public void setTotalProfit(String totalProfit) {
		this.totalProfit = totalProfit;
	}
	public String getPostSettledTransactionCount() {
		return postSettledTransactionCount;
	}
	public void setPostSettledTransactionCount(String postSettledTransactionCount) {
		this.postSettledTransactionCount = postSettledTransactionCount;
	}
	public String getActualSettlementAmount() {
		return actualSettlementAmount;
	}
	public void setActualSettlementAmount(String actualSettlementAmount) {
		this.actualSettlementAmount = actualSettlementAmount;
	}
	
	
}
