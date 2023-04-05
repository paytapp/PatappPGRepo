package com.paymentgateway.commons.util;

public class Discount {
	
	private String id;
	private String discountApplicableOn;
	private String discount;
	private String discountType;
	private String paymentType;
	private String issuerBank;
	private String mopType;
	private String paymentRegion;
	private String cardHolderType;
	private String slab;
	private String emiDuration;
	private String status;
	private String createDate;
	private String updateDate;
	private String requestedBy;
	private String updatedBy;
	private String pgName;
	private String fixedCharges;
	private String percentageCharges;

	public String getFixedCharges() {
		return fixedCharges;
	}
	public void setFixedCharges(String fixedCharges) {
		this.fixedCharges = fixedCharges;
	}
	public String getPercentageCharges() {
		return percentageCharges;
	}
	public void setPercentageCharges(String percentageCharges) {
		this.percentageCharges = percentageCharges;
	}
	public String getPgName() {
		return pgName;
	}
	public void setPgName(String pgName) {
		this.pgName = pgName;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	public String getRequestedBy() {
		return requestedBy;
	}
	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDiscountApplicableOn() {
		return discountApplicableOn;
	}
	public void setDiscountApplicableOn(String discountApplicableOn) {
		this.discountApplicableOn = discountApplicableOn;
	}
	public String getDiscount() {
		return discount;
	}
	public void setDiscount(String discount) {
		this.discount = discount;
	}
	public String getDiscountType() {
		return discountType;
	}
	public void setDiscountType(String discountType) {
		this.discountType = discountType;
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
	public String getCardHolderType() {
		return cardHolderType;
	}
	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}
	public String getSlab() {
		return slab;
	}
	public void setSlab(String slab) {
		this.slab = slab;
	}
	public String getEmiDuration() {
		return emiDuration;
	}
	public void setEmiDuration(String emiDuration) {
		this.emiDuration = emiDuration;
	}
	
	public String getIssuerBank() {
		return issuerBank;
	}
	public void setIssuerBank(String issuerBank) {
		this.issuerBank = issuerBank;
	}
	public String getPaymentRegion() {
		return paymentRegion;
	}
	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}
}
