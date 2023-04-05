package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
import com.paymentgateway.commons.util.onUsOffUs;

@Entity
@Proxy(lazy = false)
@Table
public class RatesDefault implements Serializable {

	private static final long serialVersionUID = 6451344616066902378L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Enumerated(EnumType.STRING)
	private TDRStatus status;

	@Enumerated(EnumType.STRING)
	private UserType userType;

	private String industryCategory;

	private BigDecimal acqTdr;
	private BigDecimal acqSuf;

	private BigDecimal merchantTdr;
	private BigDecimal merchantSuf;

	@Enumerated(EnumType.STRING)
	private CardHolderType cardHolderType;

	@Enumerated(EnumType.STRING)
	private AccountCurrencyRegion paymentsRegion;

	@Enumerated(EnumType.STRING)
	private onUsOffUs acquiringMode;

	private BigDecimal acquirerMaxCharge;
	private BigDecimal merchantMaxCharge;

	private String slab;
	private String acquirer;

	@Transient
	private String slabDef;

	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;

	
	@Transient
	private String paymentTypeName;
	
	@Transient
	private String paymentRegionName;
	
	@Transient
	private String acquiringModeName;
	
	@Enumerated(EnumType.STRING)
	private MopType mopType;

	boolean allowFc;

	private Date createdDate;
	private Date updatedDate;
	private String requestedBy;
	private String processedBy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TDRStatus getStatus() {
		return status;
	}

	public void setStatus(TDRStatus status) {
		this.status = status;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	public String getIndustryCategory() {
		return industryCategory;
	}

	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}

	public BigDecimal getAcqTdr() {
		return acqTdr;
	}

	public void setAcqTdr(BigDecimal acqTdr) {
		this.acqTdr = acqTdr;
	}

	public BigDecimal getAcqSuf() {
		return acqSuf;
	}

	public void setAcqSuf(BigDecimal acqSuf) {
		this.acqSuf = acqSuf;
	}

	public BigDecimal getMerchantTdr() {
		return merchantTdr;
	}

	public void setMerchantTdr(BigDecimal merchantTdr) {
		this.merchantTdr = merchantTdr;
	}

	public BigDecimal getMerchantSuf() {
		return merchantSuf;
	}

	public void setMerchantSuf(BigDecimal merchantSuf) {
		this.merchantSuf = merchantSuf;
	}

	public CardHolderType getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(CardHolderType cardHolderType) {
		this.cardHolderType = cardHolderType;
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

	public String getSlab() {
		return slab;
	}

	public void setSlab(String slab) {
		this.slab = slab;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
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

	public PaymentType getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}

	public MopType getMopType() {
		return mopType;
	}

	public void setMopType(MopType mopType) {
		this.mopType = mopType;
	}

	public boolean isAllowFc() {
		return allowFc;
	}

	public void setAllowFc(boolean allowFc) {
		this.allowFc = allowFc;
	}

	public String getSlabDef() {
		return slabDef;
	}

	public void setSlabDef(String slabDef) {
		this.slabDef = slabDef;
	}

	public BigDecimal getAcquirerMaxCharge() {
		return acquirerMaxCharge;
	}

	public void setAcquirerMaxCharge(BigDecimal acquirerMaxCharge) {
		this.acquirerMaxCharge = acquirerMaxCharge;
	}

	public BigDecimal getMerchantMaxCharge() {
		return merchantMaxCharge;
	}

	public void setMerchantMaxCharge(BigDecimal merchantMaxCharge) {
		this.merchantMaxCharge = merchantMaxCharge;
	}

	public String getPaymentTypeName() {
		return paymentTypeName;
	}

	public void setPaymentTypeName(String paymentTypeName) {
		this.paymentTypeName = paymentTypeName;
	}

	public String getPaymentRegionName() {
		return paymentRegionName;
	}

	public void setPaymentRegionName(String paymentRegionName) {
		this.paymentRegionName = paymentRegionName;
	}

	public String getAcquiringModeName() {
		return acquiringModeName;
	}

	public void setAcquiringModeName(String acquiringModeName) {
		this.acquiringModeName = acquiringModeName;
	}

}
