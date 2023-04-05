package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.hibernate.annotations.Proxy;

import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.onUsOffUs;

@Entity
@Proxy(lazy = false)

public class PendingBulkCharges implements Serializable{

	private static final long serialVersionUID = -4593033932850126769L;
	
	public PendingBulkCharges() {

	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String payId;
	
	@Lob
	private String allChargingDetail;
	
	private String slab;
	
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
	
	private String acquirerName;
	
	private Date createdDate;
	private Date updatedDate;
	private String updateBy;
	private String requestedBy;
	private String currency;
	
	@Transient
	private String businessName;
	
	
	public Long getId() {
		return id;
	}
	public String getPayId() {
		return payId;
	}
	public String getAllChargingDetail() {
		return allChargingDetail;
	}
	public String getSlab() {
		return slab;
	}
	public PaymentType getPaymentType() {
		return paymentType;
	}
	public TransactionType getTransactionType() {
		return transactionType;
	}
	public TDRStatus getStatus() {
		return status;
	}
	public AccountCurrencyRegion getPaymentsRegion() {
		return paymentsRegion;
	}
	public onUsOffUs getAcquiringMode() {
		return acquiringMode;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public String getUpdateBy() {
		return updateBy;
	}
	public String getRequestedBy() {
		return requestedBy;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public void setAllChargingDetail(String allChargingDetail) {
		this.allChargingDetail = allChargingDetail;
	}
	public void setSlab(String slab) {
		this.slab = slab;
	}
	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}
	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}
	public void setStatus(TDRStatus status) {
		this.status = status;
	}
	public void setPaymentsRegion(AccountCurrencyRegion paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}
	public void setAcquiringMode(onUsOffUs acquiringMode) {
		this.acquiringMode = acquiringMode;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getAcquirerName() {
		return acquirerName;
	}
	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	


}
