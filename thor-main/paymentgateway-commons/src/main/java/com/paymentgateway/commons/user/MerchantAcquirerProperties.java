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

import org.hibernate.annotations.Proxy;

import com.paymentgateway.commons.util.TDRStatus;

@Entity
@Proxy(lazy= false)
@Table
public class MerchantAcquirerProperties implements Serializable{
	
	private static final long serialVersionUID = 1706143864870080234L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	private TDRStatus status;

	private String merchantPayId;
	private String acquirerCode;
	
	@Enumerated(EnumType.STRING)
	private AccountCurrencyRegion paymentsRegion;
	
	private Date createDate;
	private Date updateDate;
	
	
		
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
	public String getMerchantPayId() {
		return merchantPayId;
	}
	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}
	
	public String getAcquirerCode() {
		return acquirerCode;
	}
	public void setAcquirerCode(String acquirerCode) {
		this.acquirerCode = acquirerCode;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public AccountCurrencyRegion getPaymentsRegion() {
		return paymentsRegion;
	}
	public void setPaymentsRegion(AccountCurrencyRegion paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}
	
	
}
