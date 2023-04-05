package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Proxy;

import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TDRStatus;

@Entity
@Proxy(lazy= false)
@Table
public class Surcharge implements Serializable{
	
	private static final long serialVersionUID = 6451344616066902378L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;
	
	private String payId;
	private String acquirerName;
	private String onOff;
	
	@Enumerated(EnumType.STRING)
	private TDRStatus status;
	
	@Enumerated(EnumType.STRING)
	private MopType mopType;
	
	@Column(nullable = false, columnDefinition = "TINYINT(1)")
	private boolean allowOnOff;
	
	private String merchantIndustryType;
	private BigDecimal bankSurchargeAmountCommercial;
	private BigDecimal bankSurchargeAmountCustomer;
	
	private BigDecimal bankSurchargePercentageCommercial;
	private BigDecimal bankSurchargePercentageCustomer;
	
	@javax.persistence.Transient
	private BigDecimal merchantSurchargeAmountCommercial;
	
	@javax.persistence.Transient
	private BigDecimal merchantSurchargeAmountCustomer;
	
	@Transient
	private BigDecimal merchantSurchargePercentageCommercial;
	
	@Transient
	private BigDecimal merchantSurchargePercentageCustomer;
	
	
	@Transient
	private BigDecimal pgSurchargeAmountCommercial;
	
	@Transient
	private BigDecimal pgSurchargeAmountCustomer;
	
	@Transient
	private BigDecimal pgSurchargePercentageCommercial;
	
	@Transient
	private BigDecimal pgSurchargePercentageCustomer;
	
	@Enumerated(EnumType.STRING)
	private AccountCurrencyRegion paymentsRegion;
	
	
	@Transient
	private BigDecimal serviceTax;
	private Date createdDate;
	private Date updatedDate;
	private transient String merchantName;
	
	private String requestedBy;
	private String processedBy;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public PaymentType getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getOnOff() {
		return onOff;
	}
	public void setOnOff(String onOff) {
		this.onOff = onOff;
	}
	public String getMerchantIndustryType() {
		return merchantIndustryType;
	}
	public void setMerchantIndustryType(String merchantIndustryType) {
		this.merchantIndustryType = merchantIndustryType;
	}
	public BigDecimal getServiceTax() {
		return serviceTax;
	}
	public void setServiceTax(BigDecimal serviceTax) {
		this.serviceTax = serviceTax;
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
	public MopType getMopType() {
		return mopType;
	}
	public void setMopType(MopType mopType) {
		this.mopType = mopType;
	}
	public TDRStatus getStatus() {
		return status;
	}
	public void setStatus(TDRStatus status) {
		this.status = status;
	}
	public String getAcquirerName() {
		return acquirerName;
	}
	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}
	public boolean isAllowOnOff() {
		return allowOnOff;
	}
	public void setAllowOnOff(boolean allowOnOff) {
		this.allowOnOff = allowOnOff;
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
	public String getProcessedBy() {
		return processedBy;
	}
	public void setProcessedBy(String processedBy) {
		this.processedBy = processedBy;
	}
	public AccountCurrencyRegion getPaymentsRegion() {
		return paymentsRegion;
	}
	public void setPaymentsRegion(AccountCurrencyRegion paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}
	public BigDecimal getBankSurchargeAmountCommercial() {
		return bankSurchargeAmountCommercial;
	}
	public void setBankSurchargeAmountCommercial(BigDecimal bankSurchargeAmountCommercial) {
		this.bankSurchargeAmountCommercial = bankSurchargeAmountCommercial;
	}
	public BigDecimal getBankSurchargeAmountCustomer() {
		return bankSurchargeAmountCustomer;
	}
	public void setBankSurchargeAmountCustomer(BigDecimal bankSurchargeAmountCustomer) {
		this.bankSurchargeAmountCustomer = bankSurchargeAmountCustomer;
	}
	public BigDecimal getBankSurchargePercentageCommercial() {
		return bankSurchargePercentageCommercial;
	}
	public void setBankSurchargePercentageCommercial(BigDecimal bankSurchargePercentageCommercial) {
		this.bankSurchargePercentageCommercial = bankSurchargePercentageCommercial;
	}
	public BigDecimal getBankSurchargePercentageCustomer() {
		return bankSurchargePercentageCustomer;
	}
	public void setBankSurchargePercentageCustomer(BigDecimal bankSurchargePercentageCustomer) {
		this.bankSurchargePercentageCustomer = bankSurchargePercentageCustomer;
	}
	public BigDecimal getMerchantSurchargeAmountCommercial() {
		return merchantSurchargeAmountCommercial;
	}
	public void setMerchantSurchargeAmountCommercial(BigDecimal merchantSurchargeAmountCommercial) {
		this.merchantSurchargeAmountCommercial = merchantSurchargeAmountCommercial;
	}
	public BigDecimal getMerchantSurchargeAmountCustomer() {
		return merchantSurchargeAmountCustomer;
	}
	public void setMerchantSurchargeAmountCustomer(BigDecimal merchantSurchargeAmountCustomer) {
		this.merchantSurchargeAmountCustomer = merchantSurchargeAmountCustomer;
	}
	public BigDecimal getMerchantSurchargePercentageCommercial() {
		return merchantSurchargePercentageCommercial;
	}
	public void setMerchantSurchargePercentageCommercial(BigDecimal merchantSurchargePercentageCommercial) {
		this.merchantSurchargePercentageCommercial = merchantSurchargePercentageCommercial;
	}
	public BigDecimal getMerchantSurchargePercentageCustomer() {
		return merchantSurchargePercentageCustomer;
	}
	public void setMerchantSurchargePercentageCustomer(BigDecimal merchantSurchargePercentageCustomer) {
		this.merchantSurchargePercentageCustomer = merchantSurchargePercentageCustomer;
	}
	public BigDecimal getPgSurchargeAmountCommercial() {
		return pgSurchargeAmountCommercial;
	}
	public void setPgSurchargeAmountCommercial(BigDecimal pgSurchargeAmountCommercial) {
		this.pgSurchargeAmountCommercial = pgSurchargeAmountCommercial;
	}
	public BigDecimal getPgSurchargeAmountCustomer() {
		return pgSurchargeAmountCustomer;
	}
	public void setPgSurchargeAmountCustomer(BigDecimal pgSurchargeAmountCustomer) {
		this.pgSurchargeAmountCustomer = pgSurchargeAmountCustomer;
	}
	public BigDecimal getPgSurchargePercentageCommercial() {
		return pgSurchargePercentageCommercial;
	}
	public void setPgSurchargePercentageCommercial(BigDecimal pgSurchargePercentageCommercial) {
		this.pgSurchargePercentageCommercial = pgSurchargePercentageCommercial;
	}
	public BigDecimal getPgSurchargePercentageCustomer() {
		return pgSurchargePercentageCustomer;
	}
	public void setPgSurchargePercentageCustomer(BigDecimal pgSurchargePercentageCustomer) {
		this.pgSurchargePercentageCustomer = pgSurchargePercentageCustomer;
	}

	
}
