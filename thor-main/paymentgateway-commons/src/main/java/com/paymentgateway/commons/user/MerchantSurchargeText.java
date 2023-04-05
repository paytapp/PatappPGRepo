/**
 * Dynamic text to be displayed on payment page based on merchant and specific payment type
 */
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

/**
 * @author Amitosh Aanand
 *
 */
@Entity
@Proxy(lazy = false)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MerchantSurchargeText implements Serializable {

	private static final long serialVersionUID = 3771731067154740751L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String payId;
	private String paymentType;
	private String paymentRegion;
	private String cardHolderType;
	private String mopType;
	private String minTxnAmount;
	private String maxTxnAmount;
	private String surchargeText;
	private String status;
	private String createdBy;
	private String updatedBy;
	private Date createdDate;
	private Date updatedDate;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getPaymentRegion() {
		return paymentRegion;
	}
	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}
	public String getCardHolderType() {
		return cardHolderType;
	}
	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}
	public String getMopType() {
		return mopType;
	}
	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
	public String getMinTxnAmount() {
		return minTxnAmount;
	}
	public void setMinTxnAmount(String minTxnAmount) {
		this.minTxnAmount = minTxnAmount;
	}
	public String getMaxTxnAmount() {
		return maxTxnAmount;
	}
	public void setMaxTxnAmount(String maxTxnAmount) {
		this.maxTxnAmount = maxTxnAmount;
	}
	public String getSurchargeText() {
		return surchargeText;
	}
	public void setSurchargeText(String surchargeText) {
		this.surchargeText = surchargeText;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
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
}