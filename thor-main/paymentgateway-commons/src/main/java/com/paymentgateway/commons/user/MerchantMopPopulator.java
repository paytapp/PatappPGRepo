package com.paymentgateway.commons.user;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.paymentgateway.commons.util.TDRStatus;

public class MerchantMopPopulator {
	
	
	@Enumerated(EnumType.STRING)
	private TDRStatus status;

	private String paymentType;
	private String mopType;
	private String nbBank;
	private boolean sale;
	private boolean auth;
	private boolean refund;
	
	public TDRStatus getStatus() {
		return status;
	}
	public void setStatus(TDRStatus status) {
		this.status = status;
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
	public boolean isSale() {
		return sale;
	}
	public void setSale(boolean sale) {
		this.sale = sale;
	}
	public boolean isAuth() {
		return auth;
	}
	public void setAuth(boolean auth) {
		this.auth = auth;
	}
	public boolean isRefund() {
		return refund;
	}
	public void setRefund(boolean refund) {
		this.refund = refund;
	}
	public String getNbBank() {
		return nbBank;
	}
	public void setNbBank(String nbBank) {
		this.nbBank = nbBank;
	}
	
}
