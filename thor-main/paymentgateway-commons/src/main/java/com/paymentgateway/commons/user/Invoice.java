package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Proxy;

@Entity
@Proxy(lazy= false)
public  class Invoice implements Serializable {

	private static final long serialVersionUID = 81100892505476449L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	
	private String srNo;
	private Long id;
	private String invoiceId;
	private String payId;
	private String businessName;
	private String subMerchantbusinessName;
	private String invoiceNo;
	private String name;
	private String city;
	private String country;
	private String state;
	private String zip;
	private String phone;
	private String email;
	private String address;
	private String productName;
	private String productDesc;
	private String quantity;
	private String amount;
	private String serviceCharge;
	private String totalAmount;
	private String currencyCode;
	private String expiresDay;
	private String expiresHour;
	private String createDate;
	private Date updateDate;
	private String txnId;
	private String txnType;
	
	@Column(columnDefinition = "TEXT", length = 65535)
	private String saltKey;
	
	private String returnUrl;
	private String recipientMobile;
	private String messageBody;
	private String shortUrl;
	private String longUrl;
	private String invoiceType;
	private String status;
	private String durationFrom;
	private String durationTo;
	private boolean emailStatus;
	private boolean smsStatus;
	private String fileName;
	private String subMerchantId;
	private String subUserId;
	private String mop;
	
	@Column(columnDefinition = "LONGTEXT", length = 65535000)
	private String qr;
	
	//UDF fields
	private String UDF11;
	private String UDF12;
	private String UDF13;
	private String UDF14;
	private String UDF15;
	private String UDF16;
	private String UDF17;
	private String UDF18;
	
	@Transient
	private String logo;
	
	public String getLogo() {
		return logo;
	}


	public void setLogo(String logo) {
		this.logo = logo;
	}


	public Invoice(){}
	
	
	public  Invoice(String email, String phone) {
	this.email=email;
	this.phone=phone;

	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public String getInvoiceNo() {
		return invoiceNo;
	}
	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getProductDesc() {
		return productDesc;
	}
	public void setProductDesc(String productDesc) {
		this.productDesc = productDesc;
	}
	public String getQuantity() {
		return quantity;
	}
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getServiceCharge() {
		return serviceCharge;
	}
	public void setServiceCharge(String serviceCharge) {
		this.serviceCharge = serviceCharge;
	}
	public String getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public String getExpiresDay() {
		return expiresDay;
	}
	public String getExpiresHour() {
		return expiresHour;
	}
	public void setExpiresHour(String expiresHour) {
		this.expiresHour = expiresHour;
	}
	public void setExpiresDay(String expiresDay) {
		this.expiresDay = expiresDay;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getCreateDate() {
		return createDate;
	}


	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}


	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getTxnId() {
		return txnId;
	}


	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}


	public String getTxnType() {
		return txnType;
	}


	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}


	public String getSaltKey() {
		return saltKey;
	}
	public void setSaltKey(String saltKey) {
		this.saltKey = saltKey;
	}
	public String getInvoiceId() {
		return invoiceId;
	}
	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}
	public String getReturnUrl() {
		return returnUrl;
	}
	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}
	public String getRecipientMobile() {
		return recipientMobile;
	}
	public void setRecipientMobile(String recipientMobile) {
		this.recipientMobile = recipientMobile;
	}
	public String getMessageBody() {
		return messageBody;
	}
	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public String getShortUrl() {
		return shortUrl;
	}
	public void setShortUrl(String shortUrl) {
		this.shortUrl = shortUrl;
	}

	public String getInvoiceType() {
		return invoiceType;
	}
	public void setInvoiceType(String invoiceType) {
		this.invoiceType = invoiceType;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDurationFrom() {
		return durationFrom;
	}
	public void setDurationFrom(String durationFrom) {
		this.durationFrom = durationFrom;
	}
	public String getDurationTo() {
		return durationTo;
	}
	public void setDurationTo(String durationTo) {
		this.durationTo = durationTo;
	}
	public boolean isEmailStatus() {
		return emailStatus;
	}
	public void setEmailStatus(boolean emailStatus) {
		this.emailStatus = emailStatus;
	}
	public boolean isSmsStatus() {
		return smsStatus;
	}
	public void setSmsStatus(boolean smsStatus) {
		this.smsStatus = smsStatus;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public String getQr() {
		return qr;
	}
	
	public void setQr(String qr) {
		this.qr = qr;
	}
	public Object[] myCsvMethodDownload() {
		  Object[] objectArray = new Object[19];
		  
		 
		  objectArray[0] = name;
		  objectArray[1] = phone;
		  objectArray[2] = email;
		  objectArray[3] = productName;
		  objectArray[4] = productDesc;
		  objectArray[5] = durationFrom;
		  objectArray[6] = durationTo;
		  objectArray[7] = expiresDay;
		  objectArray[8] = expiresHour;
		  objectArray[9] = currencyCode;
		  objectArray[10] = quantity;
		  objectArray[11] = amount;
		  objectArray[12] = serviceCharge;
		  objectArray[13] = address;
		  objectArray[14] = country;
		  objectArray[15] = state;
		  objectArray[16] = city;
		  objectArray[17] = zip;
		  objectArray[18] = status;

		  return objectArray;
		  
		}


	public String getSubMerchantId() {
		return subMerchantId;
	}
	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}
	public String getSubUserId() {
		return subUserId;
	}
	public void setSubUserId(String subUserId) {
		this.subUserId = subUserId;
	}
	public String getMop() {
		return mop;
	}


	public void setMop(String mop) {
		this.mop = mop;
	}


	public String getLongUrl() {
		return longUrl;
	}
	public void setLongUrl(String longUrl) {
		this.longUrl = longUrl;
	}
	public String getSubMerchantbusinessName() {
		return subMerchantbusinessName;
	}
	public void setSubMerchantbusinessName(String subMerchantbusinessName) {
		this.subMerchantbusinessName = subMerchantbusinessName;
	}


	public String getUDF11() {
		return UDF11;
	}


	public void setUDF11(String uDF11) {
		UDF11 = uDF11;
	}


	public String getUDF12() {
		return UDF12;
	}


	public void setUDF12(String uDF12) {
		UDF12 = uDF12;
	}


	public String getUDF13() {
		return UDF13;
	}


	public void setUDF13(String uDF13) {
		UDF13 = uDF13;
	}


	public String getUDF14() {
		return UDF14;
	}


	public void setUDF14(String uDF14) {
		UDF14 = uDF14;
	}


	public String getUDF15() {
		return UDF15;
	}


	public void setUDF15(String uDF15) {
		UDF15 = uDF15;
	}


	public String getUDF16() {
		return UDF16;
	}


	public void setUDF16(String uDF16) {
		UDF16 = uDF16;
	}


	public String getUDF17() {
		return UDF17;
	}


	public void setUDF17(String uDF17) {
		UDF17 = uDF17;
	}


	public String getUDF18() {
		return UDF18;
	}


	public void setUDF18(String uDF18) {
		UDF18 = uDF18;
	}
	
}
