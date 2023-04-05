package com.paymentgateway.commons.user;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.paymentgateway.commons.util.TDRStatus;

public class MerchantCurrencyPopulator {
	
	
	@Enumerated(EnumType.STRING)
	private TDRStatus status;

	private String acquirer;
	private String currency;
	private String merchantId;
	private String txnKey;
	private String password;
	private String businessType;
	private boolean non3ds;
	
	
	public String getAcquirer() {
		return acquirer;
	}
	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getTxnKey() {
		return txnKey;
	}
	public void setTxnKey(String txnKey) {
		this.txnKey = txnKey;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getBusinessType() {
		return businessType;
	}
	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}
	public boolean isNon3ds() {
		return non3ds;
	}
	public void setNon3ds(boolean non3ds) {
		this.non3ds = non3ds;
	}
	public TDRStatus getStatus() {
		return status;
	}
	public void setStatus(TDRStatus status) {
		this.status = status;
	}
	
}
