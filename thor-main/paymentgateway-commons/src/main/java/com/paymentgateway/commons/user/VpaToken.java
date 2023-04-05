package com.paymentgateway.commons.user;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import com.paymentgateway.commons.util.TokenStatus;

@Entity
public class VpaToken implements Serializable{

	private static final long serialVersionUID = -7924750198674873863L;
	
	@Id
	private String id;
	private String tokenId;
	private String payId;
	private String mopType;
	private String paymentType;
	@Enumerated(EnumType.STRING)
	private TokenStatus status;
	private String payerName;
	//private String email;
	private String vpaMask;
	private String vpaSaveParam;
	private String vpa;
	
		
	private String paymentsRegion;
	private String cardHolderType;
	
	private String keyId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public TokenStatus getStatus() {
		return status;
	}

	public void setStatus(TokenStatus status) {
		this.status = status;
	}

	public String getVpaMask() {
		return vpaMask;
	}

	public void setVpaMask(String vpaMask) {
		this.vpaMask = vpaMask;
	}

	public String getVpaSaveParam() {
		return vpaSaveParam;
	}

	public void setVpaSaveParam(String vpaSaveParam) {
		this.vpaSaveParam = vpaSaveParam;
	}

	public String getVpa() {
		return vpa;
	}

	public void setVpa(String vpa) {
		this.vpa = vpa;
	}

	public String getPaymentsRegion() {
		return paymentsRegion;
	}

	public void setPaymentsRegion(String paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}

	public String getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}

	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	public String getPayerName() {
		return payerName;
	}

	public void setPayerName(String payerName) {
		this.payerName = payerName;
	}
	
	
	
	
}
