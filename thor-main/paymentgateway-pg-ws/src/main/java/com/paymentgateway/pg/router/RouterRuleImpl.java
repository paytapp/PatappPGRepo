package com.paymentgateway.pg.router;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Proxy;
import org.springframework.stereotype.Service;

@Entity
@Proxy(lazy=false)
@Service
public class RouterRuleImpl implements Serializable{

	private static final long serialVersionUID = -7094065881379444332L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private String transactionType;
	private String paymentType;
	private String mopType;
	private String acquirerName;
	private Integer currencyCode;
	private Double bankTdr;
	private String onUsOfUs;
	
	public String getOnUsOfUs() {
		return onUsOfUs;
	}
	public void setOnUsOfUs(String onUsOfUs) {
		this.onUsOfUs = onUsOfUs;
	}
	private String secondaryAcquirerName;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTransactionType() {
		return transactionType;
	}
	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getMopType() {
		return mopType;
	}
	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
	public String getAcquirerName() {
		return acquirerName;
	}
	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}
	public Integer getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(Integer currencyCode) {
		this.currencyCode = currencyCode;
	}
	public Double getBankTdr() {
		return bankTdr;
	}
	public void setBankTdr(Double bankTdr) {
		this.bankTdr = bankTdr;
	}

	public String getSecondaryAcquirerName() {
		return secondaryAcquirerName;
	}
	public void setSecondaryAcquirerName(String secondaryAcquirerName) {
		this.secondaryAcquirerName = secondaryAcquirerName;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	


}
