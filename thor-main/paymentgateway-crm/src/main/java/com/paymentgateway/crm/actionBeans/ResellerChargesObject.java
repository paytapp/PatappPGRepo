package com.paymentgateway.crm.actionBeans;

public class ResellerChargesObject {
	
	private String cardHolderType;
	private String slab;
	private String resellerPercentage;
	private String resellerFixedCharge;
	private String pgPercentage;
	private String pgFixedCharge;
	private String maxChargeReseller;
	private String maxChargePg;
	private String gst;
	private boolean allowFC;
	private boolean select;
	
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
	public String getResellerPercentage() {
		return resellerPercentage;
	}
	public void setResellerPercentage(String resellerPercentage) {
		this.resellerPercentage = resellerPercentage;
	}
	public String getResellerFixedCharge() {
		return resellerFixedCharge;
	}
	public void setResellerFixedCharge(String resellerFixedCharge) {
		this.resellerFixedCharge = resellerFixedCharge;
	}
	public String getPgPercentage() {
		return pgPercentage;
	}
	public void setPgPercentage(String pgPercentage) {
		this.pgPercentage = pgPercentage;
	}
	public String getPgFixedCharge() {
		return pgFixedCharge;
	}
	public void setPgFixedCharge(String pgFixedCharge) {
		this.pgFixedCharge = pgFixedCharge;
	}
	public String getMaxChargeReseller() {
		return maxChargeReseller;
	}
	public void setMaxChargeReseller(String maxChargeReseller) {
		this.maxChargeReseller = maxChargeReseller;
	}
	public String getMaxChargePg() {
		return maxChargePg;
	}
	public void setMaxChargePg(String maxChargePg) {
		this.maxChargePg = maxChargePg;
	}
	public String getGst() {
		return gst;
	}
	public void setGst(String gst) {
		this.gst = gst;
	}
	public boolean isAllowFC() {
		return allowFC;
	}
	public void setAllowFC(boolean allowFC) {
		this.allowFC = allowFC;
	}
	public boolean isSelect() {
		return select;
	}
	public void setSelect(boolean select) {
		this.select = select;
	}
}
