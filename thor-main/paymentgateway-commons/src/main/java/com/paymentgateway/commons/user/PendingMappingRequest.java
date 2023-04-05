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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Proxy;

import com.paymentgateway.commons.util.TDRStatus;

@Entity
@Proxy(lazy= false)
@Table
public class PendingMappingRequest implements Serializable{
	
	private static final long serialVersionUID = 6451344616066902378L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	private TDRStatus status;

	@Column(name="accountCurrencySet", columnDefinition="TEXT")
	private String accountCurrencySet;
	
	@Column(name="mapString", columnDefinition="TEXT")
	private String mapString;
	
	private String acquirer;
	private String merchantEmailId;
	
	private String requestedBy;
	private String processedBy;
	
	private Date createdDate;
	private Date updatedDate;
	
	@Column(columnDefinition = "boolean default false")
    private boolean requestBySubAdmin;
    
	@Transient
	private String currency;
	@Transient
	private String merchantId;
	@Transient
	private String txnKey;
	@Transient
	private String password;
	@Transient
	private String businessName;
	
	@Transient
	private String paymentTypeString;
	@Transient
	private String mopTypeString;
	@Transient
	private String adf1;
	@Transient
	private String adf2;
	@Transient
	private String adf3;
	@Transient
	private String adf4;
	@Transient
	private String adf5;
	@Transient
	private String adf6;
	@Transient
	private String adf7;
	@Transient
	private String adf8;
	@Transient
	private String adf9;
	@Transient
	private String adf10;
	@Transient
	private String adf11;

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
	public String getAccountCurrencySet() {
		return accountCurrencySet;
	}
	public void setAccountCurrencySet(String accountCurrencySet) {
		this.accountCurrencySet = accountCurrencySet;
	}
	public String getMapString() {
		return mapString;
	}
	public void setMapString(String mapString) {
		this.mapString = mapString;
	}
	public String getAcquirer() {
		return acquirer;
	}
	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}
	public String getMerchantEmailId() {
		return merchantEmailId;
	}
	public void setMerchantEmailId(String merchantEmailId) {
		this.merchantEmailId = merchantEmailId;
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
	public boolean isRequestBySubAdmin() {
		return requestBySubAdmin;
	}
	public void setRequestBySubAdmin(boolean requestBySubAdmin) {
		this.requestBySubAdmin = requestBySubAdmin;
	}
	public String getCurrency() {
		return currency;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public String getTxnKey() {
		return txnKey;
	}
	public String getPassword() {
		return password;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public void setTxnKey(String txnKey) {
		this.txnKey = txnKey;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getPaymentTypeString() {
		return paymentTypeString;
	}
	public String getMopTypeString() {
		return mopTypeString;
	}
	public void setPaymentTypeString(String paymentTypeString) {
		this.paymentTypeString = paymentTypeString;
	}
	public void setMopTypeString(String mopTypeString) {
		this.mopTypeString = mopTypeString;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getAdf1() {
		return adf1;
	}
	public String getAdf2() {
		return adf2;
	}
	public String getAdf3() {
		return adf3;
	}
	public String getAdf4() {
		return adf4;
	}
	public String getAdf5() {
		return adf5;
	}
	public String getAdf6() {
		return adf6;
	}
	public String getAdf7() {
		return adf7;
	}
	public String getAdf8() {
		return adf8;
	}
	public String getAdf9() {
		return adf9;
	}
	public String getAdf10() {
		return adf10;
	}
	public String getAdf11() {
		return adf11;
	}
	public void setAdf1(String adf1) {
		this.adf1 = adf1;
	}
	public void setAdf2(String adf2) {
		this.adf2 = adf2;
	}
	public void setAdf3(String adf3) {
		this.adf3 = adf3;
	}
	public void setAdf4(String adf4) {
		this.adf4 = adf4;
	}
	public void setAdf5(String adf5) {
		this.adf5 = adf5;
	}
	public void setAdf6(String adf6) {
		this.adf6 = adf6;
	}
	public void setAdf7(String adf7) {
		this.adf7 = adf7;
	}
	public void setAdf8(String adf8) {
		this.adf8 = adf8;
	}
	public void setAdf9(String adf9) {
		this.adf9 = adf9;
	}
	public void setAdf10(String adf10) {
		this.adf10 = adf10;
	}
	public void setAdf11(String adf11) {
		this.adf11 = adf11;
	}
	
}
