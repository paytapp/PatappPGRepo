package com.paymentgateway.commons.user;

import java.io.Serializable;

public class PayoutVpa implements Serializable{

	private static final long serialVersionUID = -7933880662467875941L;
	
	private String payId;
	private String van;
	private String vanIfsc;
	private String vpa;
	private String vanBeneficiaryName;
	private String createDate;
	private String status;
	private String updateDate;
	private String subMerchantPayId;
	private String merchantName;
	
	private String bankName;
	private String userType;
	private String accountType;
	
	private String response;
	private String responseMsg;
	
	private String subWalletId;
	private String walletName;
	
	
	public String getPayId() {
		return payId;
	}
	public String getVan() {
		return van;
	}
	public String getVanIfsc() {
		return vanIfsc;
	}
	public String getCreateDate() {
		return createDate;
	}
	public String getStatus() {
		return status;
	}
	public String getUpdateDate() {
		return updateDate;
	}
	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public String getBankName() {
		return bankName;
	}
	public String getUserType() {
		return userType;
	}
	public String getAccountType() {
		return accountType;
	}
	public String getResponse() {
		return response;
	}
	public String getResponseMsg() {
		return responseMsg;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public void setVan(String van) {
		this.van = van;
	}
	public void setVanIfsc(String vanIfsc) {
		this.vanIfsc = vanIfsc;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}
	public String getVanBeneficiaryName() {
		return vanBeneficiaryName;
	}
	public void setVanBeneficiaryName(String vanBeneficiaryName) {
		this.vanBeneficiaryName = vanBeneficiaryName;
	}
	public String getVpa() {
		return vpa;
	}
	public void setVpa(String vpa) {
		this.vpa = vpa;
	}
	public String getSubWalletId() {
		return subWalletId;
	}
	public void setSubWalletId(String subWalletId) {
		this.subWalletId = subWalletId;
	}
	public String getWalletName() {
		return walletName;
	}
	public void setWalletName(String walletName) {
		this.walletName = walletName;
	}
	
	

}
