package com.paymentgateway.commons.user;

import java.io.Serializable;

public class CustomerQR implements Serializable{
	
	private static final long serialVersionUID = -4691009307357010956L;
	
	private String payId;
	private String merchantName;
	private String customerAccountNumber;
	private String customerId;
	private String status;
	private String date;
	private String upiQrCode;
	private String customerName;
	private String customerPhone;
	private String amount;
	private String vpa;
	private String companyName;
	private boolean batuwaMerchant;
	
	public boolean isBatuwaMerchant() {
		return batuwaMerchant;
	}
	public void setBatuwaMerchant(boolean batuwaMerchant) {
		this.batuwaMerchant = batuwaMerchant;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getCustomerAccountNumber() {
		return customerAccountNumber;
	}
	public void setCustomerAccountNumber(String customerAccountNumber) {
		this.customerAccountNumber = customerAccountNumber;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getUpiQrCode() {
		return upiQrCode;
	}
	public void setUpiQrCode(String upiQrCode) {
		this.upiQrCode = upiQrCode;
	}	
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getCustomerPhone() {
		return customerPhone;
	}
	public void setCustomerPhone(String customerPhone) {
		this.customerPhone = customerPhone;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getVpa() {
		return vpa;
	}
	public void setVpa(String vpa) {
		this.vpa = vpa;
	}
	
	public Object[] csvForCustomerQRMerchant() {
		Object[] objectArray = new Object[10];

		objectArray[0] = merchantName;
		objectArray[1] = payId;
		objectArray[2] = date;
		objectArray[3] = customerAccountNumber;
		objectArray[4] = customerId;
		objectArray[5] = customerName;
		objectArray[6] = customerPhone;
		objectArray[7] = amount;
		objectArray[8] = vpa;
		objectArray[9] = status;

		return objectArray;
	}
}
