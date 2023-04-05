package com.paymentgateway.commons.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.paymentgateway.commons.util.TokenStatus;

@Entity
public class Token implements Serializable {

	private static final long serialVersionUID = -7871360544517257253L;
	
	@Id
	private String id;
	private String tokenId;
	private String payId;
	private String mopType;
	private String paymentType;
	@Enumerated(EnumType.STRING)
	private TokenStatus status;
	private String customerName;
	//private String email;
	private String cardMask;
	private String cardSaveParam;
	
	@Column(columnDefinition = "TEXT", length = 65535)
	private String cardNumber;
	
	@Column(columnDefinition = "TEXT", length = 65535)
	private String expiryDate;
	
	private String paymentsRegion;
	private String cardHolderType;
	
	private String keyId;
	private String cardIssuerCountry;
	private String cardIssuerBank;

	@Transient
	private String cardNumberString;
	@Transient
	private String expDateString;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	/*public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}*/
	public String getCardMask() {
		return cardMask;
	}
	public void setCardMask(String cardMask) {
		this.cardMask = cardMask;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public TokenStatus getStatus() {
		return status;
	}
	public void setStatus(TokenStatus status) {
		this.status = status;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getKeyId() {
		return keyId;
	}
	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}
	public String getCardIssuerCountry() {
		return cardIssuerCountry;
	}
	public void setCardIssuerCountry(String cardIssuerCountry) {
		this.cardIssuerCountry = cardIssuerCountry;
	}
	public String getCardIssuerBank() {
		return cardIssuerBank;
	}
	public void setCardIssuerBank(String cardIssuerBank) {
		this.cardIssuerBank = cardIssuerBank;
	}
	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	public String getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}
	public String getCardSaveParam() {
		return cardSaveParam;
	}
	public void setCardSaveParam(String cardSaveParam) {
		this.cardSaveParam = cardSaveParam;
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
	public String getCardNumberString() {
		return cardNumberString;
	}
	public void setCardNumberString(String cardNumberString) {
		this.cardNumberString = cardNumberString;
	}
	public String getExpDateString() {
		return expDateString;
	}
	public void setExpDateString(String expDateString) {
		this.expDateString = expDateString;
	}
	public String getTokenId() {
		return tokenId;
	}
	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}
}// Token
