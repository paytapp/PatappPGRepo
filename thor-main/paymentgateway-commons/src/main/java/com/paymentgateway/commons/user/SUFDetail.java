package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;

@Entity
@Proxy(lazy = false)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SUFDetail implements Serializable {

	private static final long serialVersionUID = -4651223232203755L;

	public SUFDetail() {

	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String merchantName;
	private String payId;
	private String txnType;
	private String paymentType;
	private String requestedBy;
	private String fixedCharge;
	private Date createDate;
	private Date updateDate;
	private String status;
	private String percentageAmount;
	private String mopType;
	private String paymentRegion;
	private String slab;
	private String subMerchantPayId;
	private String subMerchantName;
	
	
	/*private String minSlab;
	private String maxSlab;*/
	
	/*public String getMaxSlab() {
		return maxSlab;
	}
	public void setMaxSlab(String maxSlab) {
		this.maxSlab = maxSlab;
	}
	public String getMinSlab() {
		return minSlab;
	}
	public void setMinSlab(String minSlab) {
		this.minSlab = minSlab;
	}*/
	public String getPaymentRegion() {
		return paymentRegion;
	}
	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	
	public String getFixedCharge() {
		return fixedCharge;
	}
	public void setFixedCharge(String fixedCharge) {
		this.fixedCharge = fixedCharge;
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getRequestedBy() {
		return requestedBy;
	}
	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getPercentageAmount() {
		return percentageAmount;
	}
	public void setPercentageAmount(String percentageAmount) {
		this.percentageAmount = percentageAmount;
	}
	public String getMopType() {
		return mopType;
	}
	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
	public String getSlab() {
		return slab;
	}
	public void setSlab(String slab) {
		this.slab = slab;
	}
	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}
	public String getSubMerchantName() {
		return subMerchantName;
	}
	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}
	public void setSubMerchantName(String subMerchantName) {
		this.subMerchantName = subMerchantName;
	}
	
}
