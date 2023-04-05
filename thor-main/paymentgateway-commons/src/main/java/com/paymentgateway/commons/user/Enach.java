package com.paymentgateway.commons.user;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * @author Rajit
 */
public class Enach implements Serializable  {

	private static final long serialVersionUID = -9075860847657858131L;
	
	private String orderId;
	
	private String payId;
	private String subMerchantPayId;
	private String merchantName;
	private String subMerchantName;
	private String resellerPayId;
	private String resellerName;
	private String merchantId;
	private String acquirerSalt;
	private String acquirerId;
	private String dateFrom;
	private String dateTo;
	private String currency;
	private String amount;
	private String maxAmount;
	private String totalAmount;
	private String paymentType;
	private String accountNumber;
	private String ifscCode;
	private String createDate;
	private String status;
	private String itemId;
	private String accountType;
	private String comAmt;
	private String deviceId;
	private String accountHolderName;
	private String custPhone;
	private String custEmail;
	private String amountType;
	private String frequency;
	private String pgRefNum;
	private String token;
	private String cardMask;
	private String expMonth;
	private String expYear;
	private String cardHolderName;
	private String bankName;
	private String tenure;
	private String returnUrl;
	private String umrnNumber;
	private String mopType;
	private String startDate;
	private String endDate;
	private String statusEnquiryHash;
	private String regPgRefNum;
	private String regDate;
	private String regStatus;
	private String srNo;
	private String debitDate;
	private String dueDate;
	private String settledDate;
	private String acquirerCharges;
	private String userType;
	private String responseMessage;
	private String eMandateUrl;
	
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
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
	public String getAcquirerCharges() {
		return acquirerCharges;
	}
	public void setAcquirerCharges(String acquirerCharges) {
		this.acquirerCharges = acquirerCharges;
	}
	public String getSettledDate() {
		return settledDate;
	}
	public void setSettledDate(String settledDate) {
		this.settledDate = settledDate;
	}
	public String getDueDate() {
		return dueDate;
	}
	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}
	public String getDebitDate() {
		return debitDate;
	}
	public void setDebitDate(String debitDate) {
		this.debitDate = debitDate;
	}
	public String getRegStatus() {
		return regStatus;
	}
	public void setRegStatus(String regStatus) {
		this.regStatus = regStatus;
	}
	public String getSrNo() {
		return srNo;
	}
	public void setSrNo(String srNo) {
		this.srNo = srNo;
	}
	public String getRegDate() {
		return regDate;
	}
	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}
	public String getRegPgRefNum() {
		return regPgRefNum;
	}
	public void setRegPgRefNum(String regPgRefNum) {
		this.regPgRefNum = regPgRefNum;
	}
	
	public String getStatusEnquiryHash() {
		return statusEnquiryHash;
	}
	public void setStatusEnquiryHash(String statusEnquiryHash) {
		this.statusEnquiryHash = statusEnquiryHash;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}
	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}
	public String getMopType() {
		return mopType;
	}
	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
	public String getUmrnNumber() {
		return umrnNumber;
	}
	public void setUmrnNumber(String umrnNumber) {
		this.umrnNumber = umrnNumber;
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
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getAcquirerSalt() {
		return acquirerSalt;
	}
	public void setAcquirerSalt(String acquirerSalt) {
		this.acquirerSalt = acquirerSalt;
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
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
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
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getIfscCode() {
		return ifscCode;
	}
	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
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
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public String getComAmt() {
		return comAmt;
	}
	public void setComAmt(String comAmt) {
		this.comAmt = comAmt;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getAccountHolderName() {
		return accountHolderName;
	}
	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
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
	public String getAmountType() {
		return amountType;
	}
	public void setAmountType(String amountType) {
		this.amountType = amountType;
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
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getCardMask() {
		return cardMask;
	}
	public void setCardMask(String cardMask) {
		this.cardMask = cardMask;
	}
	public String getExpMonth() {
		return expMonth;
	}
	public void setExpMonth(String expMonth) {
		this.expMonth = expMonth;
	}
	public String getExpYear() {
		return expYear;
	}
	public void setExpYear(String expYear) {
		this.expYear = expYear;
	}
	public String getCardHolderName() {
		return cardHolderName;
	}
	public void setCardHolderName(String cardHolderName) {
		this.cardHolderName = cardHolderName;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
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
	public String getSubMerchantName() {
		return subMerchantName;
	}
	public void setSubMerchantName(String subMerchantName) {
		this.subMerchantName = subMerchantName;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
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
	
	public Object[] methodDownloadEnachRegistrationReport() {
		Object[] objectArray = new Object[18];

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
		objectArray[11] = accountHolderName;
		objectArray[12] = custEmail;
		objectArray[13] = custPhone;
		objectArray[14] = amount;
		objectArray[15] = maxAmount;
		objectArray[16] = totalAmount;
		objectArray[17] = status;
		
		return objectArray;
	}
	
	public Object[] methodDownloadEnachRegistrationReportForSuperMerchant() {
		Object[] objectArray = new Object[20];

		objectArray[0] = srNo;
		objectArray[1] = merchantName;
		objectArray[2] = payId;
		objectArray[3] = subMerchantName;
		objectArray[4] = subMerchantPayId;
		objectArray[5] = orderId;
		objectArray[6] = regPgRefNum;
		objectArray[7] = pgRefNum;
		objectArray[8] = paymentType;
		objectArray[9] = regDate;
		objectArray[10] = createDate;
		objectArray[11] = debitDate;
		objectArray[12] = dueDate;
		objectArray[13] = accountHolderName;
		objectArray[14] = custEmail;
		objectArray[15] = custPhone;
		objectArray[16] = amount;
		objectArray[17] = maxAmount;
		objectArray[18] = totalAmount;
		objectArray[19] = status;
		
		return objectArray;
	}
	
	public Object[] methodDownloadEnachTransactionReport() {
		Object[] objectArray = new Object[18];

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
		//objectArray[10] = settledDate;
		objectArray[10] = accountHolderName;
		
		objectArray[11] = custEmail;
		objectArray[12] = custPhone;
		objectArray[13] = amount;
		objectArray[14] = maxAmount;
		objectArray[15] = totalAmount;
		objectArray[16] = status;
		
		return objectArray;
	}
	
	public Object[] methodDownloadEnachTransactionReportForSuperMerchant() {
		Object[] objectArray = new Object[20];

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
		//objectArray[12] = settledDate;
		objectArray[12] = accountHolderName;
		objectArray[13] = custEmail;
		objectArray[14] = custPhone;
		objectArray[15] = amount;
		objectArray[16] = maxAmount;
		objectArray[17] = totalAmount;
		objectArray[18] = status;
		
		return objectArray;
	}
	
	public Object[] methodDownloadEnachRegReportForSuperMerchant() {
		Object[] objectArray = new Object[18];

		objectArray[0] = srNo;
		objectArray[1] = merchantName;
		objectArray[2] = payId;
		objectArray[3] = subMerchantName;
		objectArray[4] = subMerchantPayId;
		objectArray[5] = orderId;
		objectArray[6] = pgRefNum;
		objectArray[7] = umrnNumber;
		objectArray[8] = paymentType;
		objectArray[9] = createDate;
		
		/*if (paymentType.equalsIgnoreCase("NB")) {*/
			objectArray[10] = accountHolderName;
		/*} else {
			objectArray[10] = cardHolderName;
		}*/
		objectArray[11] = custEmail;
		objectArray[12] = custPhone;
		objectArray[13] = amount;
		objectArray[14] = maxAmount;
		objectArray[15] = totalAmount;
		
		if (StringUtils.isNotBlank(userType)) {
			objectArray[16] = status;
		} else {
			objectArray[16] = acquirerCharges;
			objectArray[17] = status;
		}
		
		
		
		
		return objectArray;
	}
	
	public Object[] methodDownloadEnachRegReport() {
		Object[] objectArray = new Object[16];

		objectArray[0] = srNo;
		objectArray[1] = merchantName;
		objectArray[2] = payId;
		objectArray[3] = orderId;
		
		objectArray[4] = pgRefNum;
		objectArray[5] = umrnNumber;
		objectArray[6] = paymentType;
		objectArray[7] = createDate;
		
		//if (paymentType.equalsIgnoreCase("NB")) {
			objectArray[8] = accountHolderName;
		/*} else {
			objectArray[8] = cardHolderName;
		}*/
		
		objectArray[9] = custEmail;
		objectArray[10] = custPhone;
		objectArray[11] = amount;
		objectArray[12] = maxAmount;
		objectArray[13] = totalAmount;
		
		if (StringUtils.isNotBlank(userType)) {
			objectArray[14] = status;
		} else {
			objectArray[14] = acquirerCharges;
			objectArray[15] = status;
		}
		
		return objectArray;
	}
}
