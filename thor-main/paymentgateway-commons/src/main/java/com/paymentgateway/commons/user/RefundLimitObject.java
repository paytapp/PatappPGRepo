package com.paymentgateway.commons.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;

@Entity()
@Proxy(lazy= false)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class RefundLimitObject implements Serializable{

	/**
	 * @Mahboob Alam
	 */
	private static final long serialVersionUID = -3095661587174028662L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(nullable = false)
	private String payId;
	private String subMerchantId;
	private String merchantName;
	private String subMerchantName;
	private String date;
	private Float credit;
	private Float debit;
	private Float balance;
	private String assignorMobile;
	private String assignorEmail;
	private String assignorName;
	private String remarks;
	private String status;
	private String resellerId;
	private boolean extraRefundAmount;
	private boolean oneTimeRefundAmount;
	
	@Transient
	private float extraRefundLimit;
	@Transient
	private float oneTimeRefundLimit;
	@Transient
	private float refundLimitRemains;
	
	
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getSubMerchantId() {
		return subMerchantId;
	}
	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}
	
	public Float getCredit() {
		return credit;
	}
	public void setCredit(Float credit) {
		this.credit = credit;
	}
	public Float getDebit() {
		return debit;
	}
	public void setDebit(Float debit) {
		this.debit = debit;
	}
	public Float getBalance() {
		return balance;
	}
	public void setBalance(Float balance) {
		this.balance = balance;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getAssignorMobile() {
		return assignorMobile;
	}
	public void setAssignorMobile(String assignorMobile) {
		this.assignorMobile = assignorMobile;
	}
	public String getAssignorEmail() {
		return assignorEmail;
	}
	public void setAssignorEmail(String assignorEmail) {
		this.assignorEmail = assignorEmail;
	}
	public String getAssignorName() {
		return assignorName;
	}
	public void setAssignorName(String assignorName) {
		this.assignorName = assignorName;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getSubMerchantName() {
		return subMerchantName;
	}
	public void setSubMerchantName(String subMerchantName) {
		this.subMerchantName = subMerchantName;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public boolean isExtraRefundAmount() {
		return extraRefundAmount;
	}
	public void setExtraRefundAmount(boolean extraRefundAmount) {
		this.extraRefundAmount = extraRefundAmount;
	}
	public boolean isOneTimeRefundAmount() {
		return oneTimeRefundAmount;
	}
	public void setOneTimeRefundAmount(boolean oneTimeRefundAmount) {
		this.oneTimeRefundAmount = oneTimeRefundAmount;
	}
	public float getExtraRefundLimit() {
		return extraRefundLimit;
	}
	public void setExtraRefundLimit(float extraRefundLimit) {
		this.extraRefundLimit = extraRefundLimit;
	}
	public float getOneTimeRefundLimit() {
		return oneTimeRefundLimit;
	}
	public void setOneTimeRefundLimit(float oneTimeRefundLimit) {
		this.oneTimeRefundLimit = oneTimeRefundLimit;
	}
	public float getRefundLimitRemains() {
		return refundLimitRemains;
	}
	public void setRefundLimitRemains(float refundLimitRemains) {
		this.refundLimitRemains = refundLimitRemains;
	}
	public String getResellerId() {
		return resellerId;
	}
	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
	}
	
}
