package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;

import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.onUsOffUs;

@Entity
@Proxy(lazy = false)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ChargingDetails implements Serializable, Comparable<ChargingDetails> {

	private static final long serialVersionUID = 3440046069273849470L;

	public ChargingDetails() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Enumerated(EnumType.STRING)
	private MopType mopType;

	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;

	@Enumerated(EnumType.STRING)
	private TransactionType transactionType;

	@Enumerated(EnumType.STRING)
	private TDRStatus status;

	@Enumerated(EnumType.STRING)
	private AccountCurrencyRegion paymentsRegion;

	@Enumerated(EnumType.STRING)
	private onUsOffUs acquiringMode;

	@Enumerated(EnumType.STRING)
	private CardHolderType cardHolderType;

	// Ceiling for fix charge
	private double fixChargeLimit;
	private double minTxnAmount;
	private double maxTxnAmount;
	private double maxChargeAcquirer;
	private double maxChargeMerchant;
	private boolean allowFixCharge;

	// Bank charges
	private double bankServiceTax;
	private double bankTDR;
	private double bankFixCharge;
	private double bankFixChargeAFC;
	private double bankTDRAFC;

	// Total charges taken from merchant
	private double merchantTDR;
	private double merchantFixCharge;
	private double merchantServiceTax;
	private double merchantFixChargeAFC;
	private double merchantTDRAFC;

	// Charges by payment gateway
	private double pgTDR;
	private double pgFixCharge;
	private double pgServiceTax;
	private double pgFixChargeAFC;

	private double pgTDRAFC;
	
	// reseller charges
	private double resellerTDR;
	private double resellerFixCharge;
	private double resellerServiceTax;
	private double resellerFixChargeAFC;
	private double resellerTDRAFC;

	// User details
	private String acquirerName;
	private String payId;

	// relevent currency
	private String currency;
	
	//slab
	private String slabId;

	@Transient
	private String response;

	@Transient
	private String merchantName;

	@Transient
	private String businessName;

	private Date createdDate;
	private Date updatedDate;
	private String updateBy;
	private String requestedBy;
	@Column(columnDefinition = "boolean default false")
	private boolean requestBySubAdmin;
	
	@Transient
	private String merchantTdrString;
	@Transient
	private String merchantFixChargeString;
	@Transient
	private String bankTDRString;
	@Transient
	private String bankFixChargeString;
	@Transient
	private String resellerTDRString;
	@Transient
	private String resellerFixChargeString;
	@Transient
	private String slabString;
	@Transient
	private String idString;

	@Column(columnDefinition = "boolean default false")
	private boolean chargesFlag;
	
	@Override
	public int compareTo(ChargingDetails ChargingDetails) {
		if (transactionType == null) {
			String compareString = ChargingDetails.getCurrency() + (getMopType().toString());
			return ((this.currency + this.mopType.toString()).compareToIgnoreCase(compareString));
		}
		StringBuilder compareString = new StringBuilder();
		compareString.append(ChargingDetails.getCurrency());
		compareString.append(ChargingDetails.getMopType().getName());
		compareString.append(ChargingDetails.getTransactionType().getName());
		return (this.currency + this.mopType.getName() + this.transactionType.getName())
				.compareToIgnoreCase(compareString.toString());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public TDRStatus getStatus() {
		return status;
	}

	public void setStatus(TDRStatus status) {
		this.status = status;
	}

	public PaymentType getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public MopType getMopType() {
		return mopType;
	}

	public void setMopType(MopType mopType) {
		this.mopType = mopType;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public double getMerchantFixCharge() {
		return merchantFixCharge;
	}

	public void setMerchantFixCharge(double merchantFixCharge) {
		this.merchantFixCharge = merchantFixCharge;
	}

	public double getBankTDR() {
		return bankTDR;
	}

	public void setBankTDR(double bankTDR) {
		this.bankTDR = bankTDR;
	}

	public double getBankFixCharge() {
		return bankFixCharge;
	}

	public void setBankFixCharge(double bankFixCharge) {
		this.bankFixCharge = bankFixCharge;
	}

	public double getBankServiceTax() {
		return bankServiceTax;
	}

	public void setBankServiceTax(double bankServiceTax) {
		this.bankServiceTax = bankServiceTax;
	}

	public double getMerchantTDR() {
		return merchantTDR;
	}

	public void setMerchantTDR(double merchantTDR) {
		this.merchantTDR = merchantTDR;
	}

	public double getMerchantServiceTax() {
		return merchantServiceTax;
	}

	public void setMerchantServiceTax(double merchantServiceTax) {
		this.merchantServiceTax = merchantServiceTax;
	}

	public double getPgTDR() {
		return pgTDR;
	}

	public void setPgTDR(double pgTDR) {
		this.pgTDR = pgTDR;
	}

	public double getPgFixCharge() {
		return pgFixCharge;
	}

	public void setPgFixCharge(double pgFixCharge) {
		this.pgFixCharge = pgFixCharge;
	}

	public double getPgServiceTax() {
		return pgServiceTax;
	}

	public void setPgServiceTax(double pgServiceTax) {
		this.pgServiceTax = pgServiceTax;
	}

	public String getAcquirerName() {
		return acquirerName;
	}

	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}

	public boolean isAllowFixCharge() {
		return allowFixCharge;
	}

	public void setAllowFixCharge(boolean allowFixCharge) {
		this.allowFixCharge = allowFixCharge;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public double getBankFixChargeAFC() {
		return bankFixChargeAFC;
	}

	public void setBankFixChargeAFC(double bankFixChargeAFC) {
		this.bankFixChargeAFC = bankFixChargeAFC;
	}

	public double getBankTDRAFC() {
		return bankTDRAFC;
	}

	public void setBankTDRAFC(double bankTDRAFC) {
		this.bankTDRAFC = bankTDRAFC;
	}

	public double getMerchantFixChargeAFC() {
		return merchantFixChargeAFC;
	}

	public void setMerchantFixChargeAFC(double merchantFixChargeAFC) {
		this.merchantFixChargeAFC = merchantFixChargeAFC;
	}

	public double getMerchantTDRAFC() {
		return merchantTDRAFC;
	}

	public void setMerchantTDRAFC(double merchantTDRAFC) {
		this.merchantTDRAFC = merchantTDRAFC;
	}

	public double getPgChargeAFC() {
		return pgFixChargeAFC;
	}

	public void setPgFixChargeAFC(double pgFixChargeAFC) {
		this.pgFixChargeAFC = pgFixChargeAFC;
	}

	public double getPgTDRAFC() {
		return pgTDRAFC;
	}

	public void setPgTDRAFC(double pgTDRAFC) {
		this.pgTDRAFC = pgTDRAFC;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

	public double getPgFixChargeAFC() {
		return pgFixChargeAFC;
	}

	public AccountCurrencyRegion getPaymentsRegion() {
		return paymentsRegion;
	}

	public void setPaymentsRegion(AccountCurrencyRegion paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}

	public onUsOffUs getAcquiringMode() {
		return acquiringMode;
	}

	public void setAcquiringMode(onUsOffUs acquiringMode) {
		this.acquiringMode = acquiringMode;
	}

	public CardHolderType getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(CardHolderType cardHolderType) {
		this.cardHolderType = cardHolderType;
	}

	public double getFixChargeLimit() {
		return fixChargeLimit;
	}

	public void setFixChargeLimit(double fixChargeLimit) {
		this.fixChargeLimit = fixChargeLimit;
	}

	public double getMinTxnAmount() {
		return minTxnAmount;
	}

	public void setMinTxnAmount(double minTxnAmount) {
		this.minTxnAmount = minTxnAmount;
	}

	public double getMaxTxnAmount() {
		return maxTxnAmount;
	}

	public void setMaxTxnAmount(double maxTxnAmount) {
		this.maxTxnAmount = maxTxnAmount;
	}

	public double getMaxChargeAcquirer() {
		return maxChargeAcquirer;
	}

	public void setMaxChargeAcquirer(double maxChargeAcquirer) {
		this.maxChargeAcquirer = maxChargeAcquirer;
	}

	public double getMaxChargeMerchant() {
		return maxChargeMerchant;
	}

	public void setMaxChargeMerchant(double maxChargeMerchant) {
		this.maxChargeMerchant = maxChargeMerchant;
	}

	public String getSlabId() {
		return slabId;
	}

	public void setSlabId(String slabId) {
		this.slabId = slabId;
	}
	public double getResellerTDR() {
		return resellerTDR;
	}

	public void setResellerTDR(double resellerTDR) {
		this.resellerTDR = resellerTDR;
	}

	public double getResellerFixCharge() {
		return resellerFixCharge;
	}

	public void setResellerFixCharge(double resellerFixCharge) {
		this.resellerFixCharge = resellerFixCharge;
	}

	public double getResellerServiceTax() {
		return resellerServiceTax;
	}

	public void setResellerServiceTax(double resellerServiceTax) {
		this.resellerServiceTax = resellerServiceTax;
	}

	public double getResellerFixChargeAFC() {
		return resellerFixChargeAFC;
	}

	public void setResellerFixChargeAFC(double resellerFixChargeAFC) {
		this.resellerFixChargeAFC = resellerFixChargeAFC;
	}

	public double getResellerTDRAFC() {
		return resellerTDRAFC;
	}

	public void setResellerTDRAFC(double resellerTDRAFC) {
		this.resellerTDRAFC = resellerTDRAFC;
	}

	public boolean isChargesFlag() {
		return chargesFlag;
	}

	public void setChargesFlag(boolean chargesFlag) {
		this.chargesFlag = chargesFlag;
	}

	public String getMerchantTdrString() {
		return merchantTdrString;
	}

	public String getMerchantFixChargeString() {
		return merchantFixChargeString;
	}

	public String getBankTDRString() {
		return bankTDRString;
	}

	public String getBankFixChargeString() {
		return bankFixChargeString;
	}

	public String getResellerTDRString() {
		return resellerTDRString;
	}

	public String getResellerFixChargeString() {
		return resellerFixChargeString;
	}

	public void setMerchantTdrString(String merchantTdrString) {
		this.merchantTdrString = merchantTdrString;
	}

	public void setMerchantFixChargeString(String merchantFixChargeString) {
		this.merchantFixChargeString = merchantFixChargeString;
	}

	public void setBankTDRString(String bankTDRString) {
		this.bankTDRString = bankTDRString;
	}

	public void setBankFixChargeString(String bankFixChargeString) {
		this.bankFixChargeString = bankFixChargeString;
	}

	public void setResellerTDRString(String resellerTDRString) {
		this.resellerTDRString = resellerTDRString;
	}

	public void setResellerFixChargeString(String resellerFixChargeString) {
		this.resellerFixChargeString = resellerFixChargeString;
	}

	public String getSlabString() {
		return slabString;
	}

	public void setSlabString(String slabString) {
		this.slabString = slabString;
	}

	public String getIdString() {
		return idString;
	}

	public void setIdString(String idString) {
		this.idString = idString;
	}

	public boolean isRequestBySubAdmin() {
		return requestBySubAdmin;
	}

	public void setRequestBySubAdmin(boolean requestBySubAdmin) {
		this.requestBySubAdmin = requestBySubAdmin;
	}



}