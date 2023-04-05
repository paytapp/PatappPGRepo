package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;

/**
 * @author Amitosh
 *
 */
@Entity
@Proxy(lazy = false)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PaymentOptions implements Serializable {

	private static final long serialVersionUID = -237470353236459950L;

	public PaymentOptions() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String payId;
	private String merchantName;
	private String superMerchantId;
	private boolean creditCard;
	private boolean debitCard;
	private boolean international;
	private boolean netBanking;
	private boolean wallet;
	private boolean emi;
	private boolean recurringPayment;
	private boolean expressPay;
	
	
	@Column(nullable = true)
	private boolean saveVpa;
	private boolean upi;
	private boolean mqr;
	private boolean upiQr;
	private boolean prepaidCard;
	private boolean debitCardWithPin;
	private boolean cashOnDelivery;
	private boolean aamarPay;
	private boolean crypto;
	private Date createdDate;
	private Date updatedDate;
	private String updateBy;
	private String requestedBy;
	private String status;
	@Column(columnDefinition = "TEXT", length = 10000)
	private String mopTypeString;
	
	@Column(columnDefinition = "boolean default false")
	private boolean requestBySubAdmin;
	
	@Transient
	private String []mopTypeStringArray;
	
	@Transient
	private String paymentTypeString;

	@Transient
	private String superMerchantName;
	
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
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public boolean isCreditCard() {
		return creditCard;
	}
	public void setCreditCard(boolean creditCard) {
		this.creditCard = creditCard;
	}
	public boolean isDebitCard() {
		return debitCard;
	}
	public void setDebitCard(boolean debitCard) {
		this.debitCard = debitCard;
	}
	public boolean isInternational() {
		return international;
	}
	public void setInternational(boolean international) {
		this.international = international;
	}
	public boolean isNetBanking() {
		return netBanking;
	}
	public void setNetBanking(boolean netBanking) {
		this.netBanking = netBanking;
	}
	public boolean isWallet() {
		return wallet;
	}
	public void setWallet(boolean wallet) {
		this.wallet = wallet;
	}
	public boolean isEmi() {
		return emi;
	}
	public void setEmi(boolean emi) {
		this.emi = emi;
	}
	public boolean isRecurringPayment() {
		return recurringPayment;
	}
	public void setRecurringPayment(boolean recurringPayment) {
		this.recurringPayment = recurringPayment;
	}
	public boolean isExpressPay() {
		return expressPay;
	}
	public void setExpressPay(boolean expressPay) {
		this.expressPay = expressPay;
	}
	public boolean isSaveVpa() {
		return saveVpa;
	}
	public void setSaveVpa(boolean saveVpa) {
		this.saveVpa = saveVpa;
	}
	public boolean isUpi() {
		return upi;
	}
	public void setUpi(boolean upi) {
		this.upi = upi;
	}
	public boolean isUpiQr() {
		return upiQr;
	}
	public void setUpiQr(boolean upiQr) {
		this.upiQr = upiQr;
	}
	public boolean isPrepaidCard() {
		return prepaidCard;
	}
	public void setPrepaidCard(boolean prepaidCard) {
		this.prepaidCard = prepaidCard;
	}
	public boolean isDebitCardWithPin() {
		return debitCardWithPin;
	}
	public void setDebitCardWithPin(boolean debitCardWithPin) {
		this.debitCardWithPin = debitCardWithPin;
	}
	public boolean isCashOnDelivery() {
		return cashOnDelivery;
	}
	public void setCashOnDelivery(boolean cashOnDelivery) {
		this.cashOnDelivery = cashOnDelivery;
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
	public String getUpdateBy() {
		return updateBy;
	}
	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	public String getRequestedBy() {
		return requestedBy;
	}
	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSuperMerchantId() {
		return superMerchantId;
	}
	public void setSuperMerchantId(String superMerchantId) {
		this.superMerchantId = superMerchantId;
	}
	public String getMopTypeString() {
		return mopTypeString;
	}
	public void setMopTypeString(String mopTypeString) {
		this.mopTypeString = mopTypeString;
	}
	public String[] getMopTypeStringArray() {
		return mopTypeStringArray;
	}
	public void setMopTypeStringArray(String[] mopTypeStringArray) {
		this.mopTypeStringArray = mopTypeStringArray;
	}
	public boolean isCrypto() {
		return crypto;
	}
	public void setCrypto(boolean crypto) {
		this.crypto = crypto;
	}
	public boolean isRequestBySubAdmin() {
		return requestBySubAdmin;
	}
	public void setRequestBySubAdmin(boolean requestBySubAdmin) {
		this.requestBySubAdmin = requestBySubAdmin;
	}
	public String getPaymentTypeString() {
		return paymentTypeString;
	}
	public void setPaymentTypeString(String paymentTypeString) {
		this.paymentTypeString = paymentTypeString;
	}
	public String getSuperMerchantName() {
		return superMerchantName;
	}
	public void setSuperMerchantName(String superMerchantName) {
		this.superMerchantName = superMerchantName;
	}
	public boolean isAamarPay() {
		return aamarPay;
	}
	public void setAamarPay(boolean aamarPay) {
		this.aamarPay = aamarPay;
	}
	public boolean isMqr() {
		return mqr;
	}
	public void setMqr(boolean mqr) {
		this.mqr = mqr;
	}
	
	
	
}