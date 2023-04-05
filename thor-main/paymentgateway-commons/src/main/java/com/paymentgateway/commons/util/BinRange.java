package com.paymentgateway.commons.util;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;

import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;

/**
 * @author PG
 *
 */
@Entity
@Table
@Proxy(lazy = false)
public class BinRange implements Serializable {

	private static final long serialVersionUID = -9054276879893240789L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long Id;
	//@Column(unique = true)
	private String binCodeHigh;
	private String issuerBankName;
	@Enumerated(EnumType.STRING)
	private MopType mopType;
	// private String mopType;
	@Enumerated(EnumType.STRING)
	private PaymentType cardType;
	private String issuerCountry;
	private String productName;
	private String groupCode;
	private String rfu1;
	private String rfu2;
	private String binRangeHigh;
	private String binRangeLow;
	private String cardHolder;

	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}

	private String paymentRegion;
	//@Column(unique = true)
	private String binCodeLow;

	public BinRange() {

	}

	public BinRange(String binCodeHigh, String binCodeLow,String binRangeHigh, String binRangeLow, PaymentType cardType, String groupCode, String issuerBankName, String issuerCountry,
			MopType mopType, String productName, String rfu1, String rfu2) {

		this.binCodeHigh = binCodeHigh;
		this.binCodeLow = binCodeLow;
		this.binRangeHigh = binRangeHigh;
		this.binRangeLow = binRangeLow;
		this.issuerBankName = issuerBankName;
		this.mopType = mopType;
		this.cardType = cardType;
		this.issuerCountry = issuerCountry;
		this.productName = productName;
		this.groupCode = groupCode;
		this.rfu1 = rfu1;
		this.rfu2 = rfu2;

	}

	public String getBinRangeHigh() {
		return binRangeHigh;
	}

	public void setBinRangeHigh(String binRangeHigh) {
		this.binRangeHigh = binRangeHigh;
	}

	public String getBinRangeLow() {
		return binRangeLow;
	}

	public void setBinRangeLow(String binRangeLow) {
		this.binRangeLow = binRangeLow;
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}

	public String getBinCodeHigh() {
		return binCodeHigh;
	}

	public void setBinCodeHigh(String binCodeHigh) {
		this.binCodeHigh = binCodeHigh;
	}

	public String getBinCodeLow() {
		return binCodeLow;
	}

	public void setBinCodeLow(String binCodeLow) {
		this.binCodeLow = binCodeLow;
	}

	public String getIssuerBankName() {
		return issuerBankName;
	}

	public void setIssuerBankName(String issuerBankName) {
		this.issuerBankName = issuerBankName;
	}

	public MopType getMopType() {
		return mopType;
	}

	public void setMopType(MopType mopType) {
		this.mopType = mopType;
	}

	public String getIssuerCountry() {
		return issuerCountry;
	}

	public void setIssuerCountry(String issuerCountry) {
		this.issuerCountry = issuerCountry;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getGroupCode() {
		return groupCode;
	}

	public void setGroupCode(String groupCode) {
		this.groupCode = groupCode;
	}

	public String getRfu1() {
		return rfu1;
	}

	public void setRfu1(String rfu1) {
		this.rfu1 = rfu1;
	}

	public String getRfu2() {
		return rfu2;
	}

	public void setRfu2(String rfu2) {
		this.rfu2 = rfu2;
	}

	public PaymentType getCardType() {
		return cardType;
	}

	public void setCardType(PaymentType cardType) {
		this.cardType = cardType;
	}
	public String getCardHolder() {
		return cardHolder;
	}

	public void setCardHolder(String cardHolder) {
		this.cardHolder = cardHolder;
	}

	public String getPaymentRegion() {
		return paymentRegion;
	}

}
