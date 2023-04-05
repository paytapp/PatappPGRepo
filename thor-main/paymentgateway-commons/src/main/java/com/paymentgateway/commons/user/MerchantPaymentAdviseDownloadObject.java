package com.paymentgateway.commons.user;

import java.io.Serializable;

public class MerchantPaymentAdviseDownloadObject implements Serializable {

	/**
	 * 	Rajit
	 */
	private static final long serialVersionUID = -5708916063236483900L;
	private String cardHolderType;
	private String paymentRegion;
	private String paymentType;
	private String acquirerMode;
	private String grossAmount;
	private String netAmount;
	private String tdr;
	private String gst;
	private String totalTranasction;
	private String cardNetwork;
	private String srNo;
	private String origTxnType;
	private String oid;
	private String payId;
	private String baseAmount;
	private String resellerCharges;
	private String resellerGst;
	private String utrNo;
	private String payOutDate;
	private String surcharge_flag;
	private String createDate;
	
	
	public String getBaseAmount() {
		return baseAmount;
	}
	public void setBaseAmount(String baseAmount) {
		this.baseAmount = baseAmount;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	public String getCardHolderType() {
		return cardHolderType;
	}
	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}
	public String getPaymentRegion() {
		return paymentRegion;
	}
	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getAcquirerMode() {
		return acquirerMode;
	}
	public void setAcquirerMode(String acquirerMode) {
		this.acquirerMode = acquirerMode;
	}
	public String getGrossAmount() {
		return grossAmount;
	}
	public void setGrossAmount(String grossAmount) {
		this.grossAmount = grossAmount;
	}
	public String getNetAmount() {
		return netAmount;
	}
	public void setNetAmount(String netAmount) {
		this.netAmount = netAmount;
	}
	public String getTotalTranasction() {
		return totalTranasction;
	}
	public void setTotalTranasction(String totalTranasction) {
		this.totalTranasction = totalTranasction;
	}
	public String getTdr() {
		return tdr;
	}
	public void setTdr(String tdr) {
		this.tdr = tdr;
	}
	public String getGst() {
		return gst;
	}
	public void setGst(String gst) {
		this.gst = gst;
	}
	public String getCardNetwork() {
		return cardNetwork;
	}
	public void setCardNetwork(String cardNetwork) {
		this.cardNetwork = cardNetwork;
	}
	public String getSrNo() {
		return srNo;
	}
	public void setSrNo(String srNo) {
		this.srNo = srNo;
	}
	public String getOrigTxnType() {
		return origTxnType;
	}
	public void setOrigTxnType(String origTxnType) {
		this.origTxnType = origTxnType;
	}
	public String getResellerCharges() {
		return resellerCharges;
	}
	public void setResellerCharges(String resellerCharges) {
		this.resellerCharges = resellerCharges;
	}
	public String getResellerGst() {
		return resellerGst;
	}
	public void setResellerGst(String resellerGst) {
		this.resellerGst = resellerGst;
	}
	public String getUtrNo() {
		return utrNo;
	}
	public void setUtrNo(String utrNo) {
		this.utrNo = utrNo;
	}
	public String getPayOutDate() {
		return payOutDate;
	}
	public void setPayOutDate(String payOutDate) {
		this.payOutDate = payOutDate;
	}
	public String getSurcharge_flag() {
		return surcharge_flag;
	}
	public void setSurcharge_flag(String surcharge_flag) {
		this.surcharge_flag = surcharge_flag;
	}
	public Object[] myCsvMethodDownloadPaymentsReportByView(boolean isFinalMap, User sessionUser) {
		  Object[] objectArray = new Object[4];
		 
		  objectArray[0] = grossAmount;
		  if(sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
			  objectArray[1] = resellerCharges;  
		  }else {
		  objectArray[1] = tdr;
		  }
		  
		  if(origTxnType.equalsIgnoreCase("REFUND") && isFinalMap == true) {
			  objectArray[2] = "-" + netAmount;
		  }else {
			  objectArray[2] = netAmount;
		  }
		  objectArray[3] = baseAmount;
		  
		  return objectArray;
		}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
}
