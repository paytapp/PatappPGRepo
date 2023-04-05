package com.paymentgateway.commons.user;

import java.util.List;

public class PayoutAcquireMapping {

	private String bankName;
	private String userType;
	private String accountType;
	
	private String adfFields;
	
	private String response;
	private String responseMsg;
	
	private String payId;
	private String merchantName;
	
	private String subMerchantPayId;
	private String subMerchantName;
	
	private String van;
	private String vanIfsc;
	private String beneficiaryName;
	
	private String subWalletId;
	
	private String status;

	private List<PayoutAcquireMapping> merchantMappedData;

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getAdfFields() {
		return adfFields;
	}

	public void setAdfFields(String adfFields) {
		this.adfFields = adfFields;
	}

	public String getResponse() {
		return response;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getPayId() {
		return payId;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}


	public List<PayoutAcquireMapping> getMerchantMappedData() {
		return merchantMappedData;
	}

	public void setMerchantMappedData(List<PayoutAcquireMapping> merchantMappedData) {
		this.merchantMappedData = merchantMappedData;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getVan() {
		return van;
	}

	public String getVanIfsc() {
		return vanIfsc;
	}

	public void setVan(String van) {
		this.van = van;
	}

	public void setVanIfsc(String vanIfsc) {
		this.vanIfsc = vanIfsc;
	}

	public String getBeneficiaryName() {
		return beneficiaryName;
	}

	public void setBeneficiaryName(String beneficiaryName) {
		this.beneficiaryName = beneficiaryName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSubWalletId() {
		return subWalletId;
	}

	public void setSubWalletId(String subWalletId) {
		this.subWalletId = subWalletId;
	}

	public String getSubMerchantName() {
		return subMerchantName;
	}

	public void setSubMerchantName(String subMerchantName) {
		this.subMerchantName = subMerchantName;
	}
	
	
}
