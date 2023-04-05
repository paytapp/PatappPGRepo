package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
/**
 * Rahul
 */
@Entity
@Proxy(lazy = false)
@Table(indexes = { @Index(name = "IDX_MYIDX1", columnList = "merchantEmailId,resellerId") })
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PendingResellerMappingApproval implements Serializable{

	private static final long serialVersionUID = -4151373041505845399L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(nullable=false,unique=true)
	private long id;
	private String resellerId;
	private Date createDate;
	private Date updateDate;
	private String merchantEmailId;
	private String requestedBy;
	private String processedBy;
	private String requestStatus;
	private String businessName;
	private String merchantPayId;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getResellerId() {
		return resellerId;
	}
	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
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
	public String getRequestedBy() {
		return requestedBy;
	}
	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}
	public String getRequestStatus() {
		return requestStatus;
	}
	public void setRequestStatus(String requestStatus) {
		this.requestStatus = requestStatus;
	}
	public String getMerchantEmailId() {
		return merchantEmailId;
	}
	public void setMerchantEmailId(String merchantEmailId) {
		this.merchantEmailId = merchantEmailId;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public String getMerchantPayId() {
		return merchantPayId;
	}
	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}
	public String getProcessedBy() {
		return processedBy;
	}
	public void setProcessedBy(String processedBy) {
		this.processedBy = processedBy;
	}
	
	
}
