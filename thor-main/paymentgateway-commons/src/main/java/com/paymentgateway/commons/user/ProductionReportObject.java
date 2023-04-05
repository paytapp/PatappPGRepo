package com.paymentgateway.commons.user;

import java.io.Serializable;

public class ProductionReportObject implements Serializable{
 
	private static final long serialVersionUID = 5899705456765089877L;
	
	private String merchant;
	private String payId;
	private String merchantId;
	private String txnKey;
	private String password;
	private String adf1;
	private String adf2;
	private String adf3;
	private String adf4;
	private String adf5;
	private String adf6;
	private String adf7;
	private String adf8;
	private String adf9;
	private String adf10;
	private String adf11;
	
	private String mop;
	private String paymentType;
	private String code;
	
	public String getMerchant() {
		return merchant;
	}
	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
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
	public String getAdf1() {
		return adf1;
	}
	public void setAdf1(String adf1) {
		this.adf1 = adf1;
	}
	public String getAdf2() {
		return adf2;
	}
	public void setAdf2(String adf2) {
		this.adf2 = adf2;
	}
	public String getAdf3() {
		return adf3;
	}
	public void setAdf3(String adf3) {
		this.adf3 = adf3;
	}
	public String getAdf4() {
		return adf4;
	}
	public void setAdf4(String adf4) {
		this.adf4 = adf4;
	}
	public String getAdf5() {
		return adf5;
	}
	public void setAdf5(String adf5) {
		this.adf5 = adf5;
	}
	public String getAdf6() {
		return adf6;
	}
	public void setAdf6(String adf6) {
		this.adf6 = adf6;
	}
	public String getAdf7() {
		return adf7;
	}
	public void setAdf7(String adf7) {
		this.adf7 = adf7;
	}
	public String getAdf8() {
		return adf8;
	}
	public void setAdf8(String adf8) {
		this.adf8 = adf8;
	}
	public String getAdf9() {
		return adf9;
	}
	public void setAdf9(String adf9) {
		this.adf9 = adf9;
	}
	public String getAdf10() {
		return adf10;
	}
	public void setAdf10(String adf10) {
		this.adf10 = adf10;
	}
	public String getAdf11() {
		return adf11;
	}
	public void setAdf11(String adf11) {
		this.adf11 = adf11;
	}
	public String getMop() {
		return mop;
	}
	public void setMop(String mop) {
		this.mop = mop;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	public Object[] myCsvMethodDownloadMappingByAdminView() {
		  Object[] objectArray = new Object[5];
		  
		 
		  
		  objectArray[0] = merchant;
		  objectArray[1] = payId;
		  objectArray[2] = mop;
		  objectArray[3] = paymentType;
		  objectArray[4] = code;
		  return objectArray;
		}
	
	
}
