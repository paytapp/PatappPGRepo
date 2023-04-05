package com.paymentgateway.payu;

import org.springframework.stereotype.Service;

@Service("payuTransaction")
public class Transaction {

	// SALE
	private String key;
	private String txnId;
	private String amount;
	private String productInfo;
	private String firstName;
	private String email;
	private String phone;
	private String surl;
	private String furl;
	private String pg;
	private String bankCode;
	private String ccnum;
	private String ccname;
	private String ccvv;
	private String ccExpMon;
	private String ccExpYr;
	private String consentShared;
	private String hash;
	private String salt;
	private String vpa;
	
	// Response

	private String status;
	private String responseCode;
	private String responseMsg;
	private String bankRefNum;
	private String mihPayuId;
	private String refundToken;
	private String refundAmount;
	private String refundId;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getProductInfo() {
		return productInfo;
	}
	public void setProductInfo(String productInfo) {
		this.productInfo = productInfo;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getSurl() {
		return surl;
	}
	public void setSurl(String surl) {
		this.surl = surl;
	}
	public String getFurl() {
		return furl;
	}
	public void setFurl(String furl) {
		this.furl = furl;
	}
	public String getPg() {
		return pg;
	}
	public void setPg(String pg) {
		this.pg = pg;
	}
	public String getBankCode() {
		return bankCode;
	}
	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	public String getCcnum() {
		return ccnum;
	}
	public void setCcnum(String ccnum) {
		this.ccnum = ccnum;
	}
	public String getCcname() {
		return ccname;
	}
	public void setCcname(String ccname) {
		this.ccname = ccname;
	}
	public String getCcvv() {
		return ccvv;
	}
	public void setCcvv(String ccvv) {
		this.ccvv = ccvv;
	}
	public String getCcExpMon() {
		return ccExpMon;
	}
	public void setCcExpMon(String ccExpMon) {
		this.ccExpMon = ccExpMon;
	}
	public String getCcExpYr() {
		return ccExpYr;
	}
	public void setCcExpYr(String ccExpYr) {
		this.ccExpYr = ccExpYr;
	}
	public String getConsentShared() {
		return consentShared;
	}
	public void setConsentShared(String consentShared) {
		this.consentShared = consentShared;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getResponseMsg() {
		return responseMsg;
	}
	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public String getMihPayuId() {
		return mihPayuId;
	}
	public void setMihPayuId(String mihPayuId) {
		this.mihPayuId = mihPayuId;
	}
	public String getBankRefNum() {
		return bankRefNum;
	}
	public void setBankRefNum(String bankRefNum) {
		this.bankRefNum = bankRefNum;
	}
	public String getRefundToken() {
		return refundToken;
	}
	public void setRefundToken(String refundToken) {
		this.refundToken = refundToken;
	}
	public String getRefundAmount() {
		return refundAmount;
	}
	public void setRefundAmount(String refundAmount) {
		this.refundAmount = refundAmount;
	}
	public String getRefundId() {
		return refundId;
	}
	public void setRefundId(String refundId) {
		this.refundId = refundId;
	}
	public String getVpa() {
		return vpa;
	}
	public void setVpa(String vpa) {
		this.vpa = vpa;
	}

	
}
