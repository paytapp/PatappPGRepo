package com.paymentgateway.crm.action;

public class BulkChargesObject {

	private String cardHolderType;
	private String slab;
	private String pgTdr;
	private String pgSuf;
	private String acquirerTdr;
	private String acquirerSuf;
	private String resellerTdr;
	private String resellerFC;
	private String merchantTdr;
	private String merchantSuf;
	private String gst;
	private boolean allowFC;
	private boolean chargesFlag;
	private String maxChargeMerchant;
	private String maxChargeAcquirer;
	private boolean select;
	
	public String getCardHolderType() {
		return cardHolderType;
	}
	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}
	public String getPgTdr() {
		return pgTdr;
	}
	public void setPgTdr(String pgTdr) {
		this.pgTdr = pgTdr;
	}
	public String getAcquirerTdr() {
		return acquirerTdr;
	}
	public void setAcquirerTdr(String acquirerTdr) {
		this.acquirerTdr = acquirerTdr;
	}
	public String getAcquirerSuf() {
		return acquirerSuf;
	}
	public void setAcquirerSuf(String acquirerSuf) {
		this.acquirerSuf = acquirerSuf;
	}
	public String getMerchantTdr() {
		return merchantTdr;
	}
	public void setMerchantTdr(String merchantTdr) {
		this.merchantTdr = merchantTdr;
	}
	public String getMerchantSuf() {
		return merchantSuf;
	}
	public void setMerchantSuf(String merchantSuf) {
		this.merchantSuf = merchantSuf;
	}
	public String getGst() {
		return gst;
	}
	public void setGst(String gst) {
		this.gst = gst;
	}
	public String getMaxChargeMerchant() {
		return maxChargeMerchant;
	}
	public void setMaxChargeMerchant(String maxChargeMerchant) {
		this.maxChargeMerchant = maxChargeMerchant;
	}
	public String getMaxChargeAcquirer() {
		return maxChargeAcquirer;
	}
	public void setMaxChargeAcquirer(String maxChargeAcquirer) {
		this.maxChargeAcquirer = maxChargeAcquirer;
	}
	public boolean isSelect() {
		return select;
	}
	public void setSelect(boolean select) {
		this.select = select;
	}
	public String getSlab() {
		return slab;
	}
	public void setSlab(String slab) {
		this.slab = slab;
	}
	public String getPgSuf() {
		return pgSuf;
	}
	public void setPgSuf(String pgSuf) {
		this.pgSuf = pgSuf;
	}
	public boolean isAllowFC() {
		return allowFC;
	}
	public void setAllowFC(boolean allowFC) {
		this.allowFC = allowFC;
	}
	public String getResellerTdr() {
		return resellerTdr;
	}
	public void setResellerTdr(String resellerTdr) {
		this.resellerTdr = resellerTdr;
	}
	public String getResellerFC() {
		return resellerFC;
	}
	public void setResellerFC(String resellerFC) {
		this.resellerFC = resellerFC;
	}
	public boolean isChargesFlag() {
		return chargesFlag;
	}
	public void setChargesFlag(boolean chargesFlag) {
		this.chargesFlag = chargesFlag;
	}
}
