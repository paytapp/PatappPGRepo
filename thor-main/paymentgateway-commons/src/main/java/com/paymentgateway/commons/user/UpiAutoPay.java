package com.paymentgateway.commons.user;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

public class UpiAutoPay implements Serializable  {
	
	private static final long serialVersionUID = 1228993861273058512L;
	
	private String orderId;
	private String payId;
	private String subMerchantPayId;
	private String merchantName;
	private String subMerchantName;
	private String resellerPayId;
	private String resellerName;
	private String merchantId;
	private String acquirerId;
	private String dateFrom;
	private String dateTo;	
	private String amount;
	private String maxAmount;
	private String totalAmount;
	private String paymentType;
	private String createDate;
	private String status;
	private String custPhone;
	private String custEmail;
	private String frequency;
	private String pgRefNum;
	private String tenure;
	private String returnUrl;
	private String umnNumber;
	private String startDate;
	private String endDate;
	private String regPgRefNum;
	private String regDate;
	private String srNo;
	private String dueDate;
	private String acquirerCharges;
	private String payerAddress;;
	private String notificationDate;
	private String rrn;
	private String debitDate;
	private String userType;
	private String eMandateUrl;
	
	
	public String geteMandateUrl() {
		return eMandateUrl;
	}
	public void seteMandateUrl(String eMandateUrl) {
		this.eMandateUrl = eMandateUrl;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public String getDebitDate() {
		return debitDate;
	}
	public void setDebitDate(String debitDate) {
		this.debitDate = debitDate;
	}
	public String getPayerAddress() {
		return payerAddress;
	}
	public void setPayerAddress(String payerAddress) {
		this.payerAddress = payerAddress;
	}
	
	public String getRrn() {
		return rrn;
	}
	public void setRrn(String rrn) {
		this.rrn = rrn;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}
	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getSubMerchantName() {
		return subMerchantName;
	}
	public void setSubMerchantName(String subMerchantName) {
		this.subMerchantName = subMerchantName;
	}
	public String getResellerPayId() {
		return resellerPayId;
	}
	public void setResellerPayId(String resellerPayId) {
		this.resellerPayId = resellerPayId;
	}
	public String getResellerName() {
		return resellerName;
	}
	public void setResellerName(String resellerName) {
		this.resellerName = resellerName;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getAcquirerId() {
		return acquirerId;
	}
	public void setAcquirerId(String acquirerId) {
		this.acquirerId = acquirerId;
	}
	public String getDateFrom() {
		return dateFrom;
	}
	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}
	public String getDateTo() {
		return dateTo;
	}
	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getMaxAmount() {
		return maxAmount;
	}
	public void setMaxAmount(String maxAmount) {
		this.maxAmount = maxAmount;
	}
	public String getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCustPhone() {
		return custPhone;
	}
	public void setCustPhone(String custPhone) {
		this.custPhone = custPhone;
	}
	public String getCustEmail() {
		return custEmail;
	}
	public void setCustEmail(String custEmail) {
		this.custEmail = custEmail;
	}
	public String getFrequency() {
		return frequency;
	}
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	public String getPgRefNum() {
		return pgRefNum;
	}
	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}
	public String getTenure() {
		return tenure;
	}
	public void setTenure(String tenure) {
		this.tenure = tenure;
	}
	public String getReturnUrl() {
		return returnUrl;
	}
	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}
	public String getUmnNumber() {
		return umnNumber;
	}
	public void setUmnNumber(String umnNumber) {
		this.umnNumber = umnNumber;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getRegPgRefNum() {
		return regPgRefNum;
	}
	public void setRegPgRefNum(String regPgRefNum) {
		this.regPgRefNum = regPgRefNum;
	}
	public String getRegDate() {
		return regDate;
	}
	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}
	public String getSrNo() {
		return srNo;
	}
	public void setSrNo(String srNo) {
		this.srNo = srNo;
	}
	public String getDueDate() {
		return dueDate;
	}
	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}
	public String getAcquirerCharges() {
		return acquirerCharges;
	}
	public void setAcquirerCharges(String acquirerCharges) {
		this.acquirerCharges = acquirerCharges;
	}
	
	public String getNotificationDate() {
		return notificationDate;
	}
	public void setNotificationDate(String notificationDate) {
		this.notificationDate = notificationDate;
	}
	
	public Object[] methodDownloadAutoPayTransactionReportForSuperMerchant() {
		
		Object[] objectArray = new Object[20];
		
		objectArray[0] = srNo;
		objectArray[1] = merchantName;
		objectArray[2] = payId;
		objectArray[3] = subMerchantName;
		objectArray[4] = subMerchantPayId;
		objectArray[5] = orderId;
		objectArray[6] = pgRefNum;
		objectArray[7] = umnNumber;
		objectArray[8] = paymentType;
		objectArray[9] = regDate;
		objectArray[10] = notificationDate;
		objectArray[11] = createDate;
		objectArray[12] = debitDate;
		objectArray[13] = dueDate;
		objectArray[14] = custEmail;
		objectArray[15] = custPhone;
		objectArray[16] = amount;
		objectArray[17] = maxAmount;
		objectArray[18] = totalAmount;
		objectArray[19] = status;
		
		return objectArray;
	}
	
	public Object[] methodDownloadAutoPayTransactionReport() {
		
		Object[] objectArray = new Object[20];
		
		objectArray[0] = srNo;
		objectArray[1] = merchantName;
		objectArray[2] = payId;
		objectArray[3] = orderId;
		objectArray[4] = pgRefNum;
		objectArray[5] = umnNumber;
		objectArray[6] = paymentType;
		objectArray[7] = regDate;
		objectArray[8] = createDate;
		objectArray[9] = notificationDate;
		objectArray[10] = debitDate;
		objectArray[11] = dueDate;
		objectArray[12] = custEmail;
		objectArray[13] = custPhone;
		objectArray[14] = amount;
		objectArray[15] = maxAmount;
		objectArray[16] = totalAmount;
		objectArray[17] = status;
		
		return objectArray;
	}
	
	public Object[] methodDownloadAutoPayRegistrationReportForSuperMerchant() {
		
		Object[] objectArray = new Object[17];
		
		objectArray[0] = srNo;
		objectArray[1] = merchantName;
		objectArray[2] = payId;
		objectArray[3] = subMerchantName;
		objectArray[4] = subMerchantPayId;
		objectArray[5] = orderId;
		objectArray[6] = pgRefNum;
		objectArray[7] = umnNumber;
		objectArray[8] = paymentType;
		objectArray[9] = createDate;
		objectArray[10] = custEmail;
		objectArray[11] = custPhone;
		objectArray[12] = amount;
		objectArray[13] = maxAmount;
		objectArray[14] = totalAmount;
		if (StringUtils.isNotBlank(userType)) {
			objectArray[15] = status;
		} else {
			objectArray[15] = acquirerCharges;
			objectArray[16] = status;
		}
		
		return objectArray;
	}
	
	public Object[] methodDownloadRegistrationReport() {
		
		Object[] objectArray = new Object[15];
		
		objectArray[0] = srNo;
		objectArray[1] = merchantName;
		objectArray[2] = payId;
		objectArray[3] = orderId;
		objectArray[4] = pgRefNum;
		objectArray[5] = umnNumber;
		objectArray[6] = paymentType;
		objectArray[7] = createDate;
		objectArray[8] = custEmail;
		objectArray[9] = custPhone;
		objectArray[10] = amount;
		objectArray[11] = maxAmount;
		objectArray[12] = totalAmount;
		if (StringUtils.isNotBlank(userType)) {
			objectArray[13] = status;
		} else {
			objectArray[13] = acquirerCharges;
			objectArray[14] = status;
		}
		
		return objectArray;
	}
	
	public Object[] methodDownloadAutoPayCapturedRegistrationReport() {
		
		Object[] objectArray = new Object[19];
		
		objectArray[0] = srNo;
		objectArray[1] = merchantName;
		objectArray[2] = payId;
		objectArray[3] = orderId;
		objectArray[4] = regPgRefNum;
		objectArray[5] = pgRefNum;
		objectArray[6] = paymentType;
		objectArray[7] = regDate;
		objectArray[8] = createDate;
		objectArray[9] = debitDate;
		objectArray[10] = dueDate;
		objectArray[11] = umnNumber;
		objectArray[12] = custEmail;
		objectArray[13] = custPhone;
		objectArray[14] = amount;
		objectArray[15] = maxAmount;
		objectArray[16] = totalAmount;
		
		if (StringUtils.isNotBlank(userType)) {
			objectArray[17] = status;
		} else {
			//objectArray[17] = acquirerCharges;
			objectArray[17] = status;
		}
		
		return objectArray;
	}
	
	public Object[] methodDownloadAutoPayIndividualTransactionReport() {
		Object[] objectArray = new Object[16];

		objectArray[0] = srNo;
		objectArray[1] = merchantName;
		objectArray[2] = payId;
		objectArray[3] = orderId;
		objectArray[4] = pgRefNum;
		objectArray[5] = paymentType;
		objectArray[6] = regDate;
		objectArray[7] = createDate;
		objectArray[8] = debitDate;
		objectArray[9] = dueDate;		
		objectArray[10] = custEmail;
		objectArray[11] = custPhone;
		objectArray[12] = amount;
		objectArray[13] = maxAmount;
		objectArray[14] = totalAmount;
		objectArray[15] = status;
		
		return objectArray;
	}
	
	public Object[] methodDownloadAutoPayTransactionReportIndividualForSuperMerchant() {
		Object[] objectArray = new Object[18];

		objectArray[0] = srNo;
		objectArray[1] = merchantName;
		objectArray[2] = payId;
		objectArray[3] = subMerchantName;
		objectArray[4] = subMerchantPayId;
		objectArray[5] = orderId;
		objectArray[6] = pgRefNum;
		objectArray[7] = paymentType;
		objectArray[8] = regDate;
		objectArray[9] = createDate;
		objectArray[10] = debitDate;
		objectArray[11] = dueDate;
		objectArray[12] = custEmail;
		objectArray[13] = custPhone;
		objectArray[14] = amount;
		objectArray[15] = maxAmount;
		objectArray[16] = totalAmount;
		objectArray[17] = status;
		
		return objectArray;
	}
	
}

