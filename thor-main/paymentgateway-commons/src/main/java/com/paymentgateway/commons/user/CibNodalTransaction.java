package com.paymentgateway.commons.user;

public class CibNodalTransaction {
	
	private String merchantName;
	private String subMerchantName;
	private String txnId;
	private String currency;
	private String payId;
	private String createDate;	
	private String bankAccountName;
	private String bankAccountNumber;
	private String bankIfsc;
	private String txnType;
	private String status;
	private String responseMsg;
	private String amount;
	private String remarks;
	private String utrNo;
	private String pgRefNo;
	private String capturedDateTo;
	private String capturedDateFrom;
	private String mopType;
	private String paymentType;
	private String srNo;
	
	// for download table
	private String dateFrom;
	private String dateTo;
	private String fileName;
	private String fileFor;
	
	
	public String getSrNo() {
		return srNo;
	}
	public void setSrNo(String srNo) {
		this.srNo = srNo;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getBankAccountName() {
		return bankAccountName;
	}
	public void setBankAccountName(String bankAccountName) {
		this.bankAccountName = bankAccountName;
	}
	public String getBankAccountNumber() {
		return bankAccountNumber;
	}
	public void setBankAccountNumber(String bankAccountNumber) {
		this.bankAccountNumber = bankAccountNumber;
	}
	public String getBankIfsc() {
		return bankIfsc;
	}
	public void setBankIfsc(String bankIfsc) {
		this.bankIfsc = bankIfsc;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
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
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getSubMerchantName() {
		return subMerchantName;
	}
	public void setSubMerchantName(String subMerchantName) {
		this.subMerchantName = subMerchantName;
	}
	
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
	
	
	public String getUtrNo() {
		return utrNo;
	}
	public void setUtrNo(String utrNo) {
		this.utrNo = utrNo;
	}
	 
	public String getPgRefNo() {
		return pgRefNo;
	}
	public void setPgRefNo(String pgRefNo) {
		this.pgRefNo = pgRefNo;
	}
	
	public String getCapturedDateTo() {
		return capturedDateTo;
	}
	public void setCapturedDateTo(String capturedDateTo) {
		this.capturedDateTo = capturedDateTo;
	}
	public String getCapturedDateFrom() {
		return capturedDateFrom;
	}
	public void setCapturedDateFrom(String capturedDateFrom) {
		this.capturedDateFrom = capturedDateFrom;
	}

	
	public String getDateFrom() {
		return dateFrom;
	}
	public String getDateTo() {
		return dateTo;
	}
	public String getFileName() {
		return fileName;
	}
	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}
	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getMopType() {
		return mopType;
	}
	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public Object[] csvMethodForDownloadFile() {
		  Object[] objectArray = new Object[16];
		  
		  
		  objectArray[0] = merchantName;
		  objectArray[1] = txnId;
		  objectArray[2] = createDate;
		  objectArray[3] = capturedDateFrom;
		  objectArray[4] = capturedDateTo;
		  objectArray[5] = utrNo;
		  objectArray[6] = payId;
		  objectArray[7] = bankAccountName;
		  objectArray[8] = bankAccountNumber;
		  objectArray[9] = bankIfsc;
		  objectArray[10] = currency;
		  objectArray[11] = txnType;
		  objectArray[12] = amount;
		  objectArray[13] = status;
		  objectArray[14] = responseMsg;
		  objectArray[15] = remarks;

		  return objectArray;
		}
	
	public Object[] csvMethodForSubMerchatDownloadFile() {
		  Object[] objectArray = new Object[17];
		  
		  objectArray[0] = merchantName;
		  objectArray[1] = subMerchantName;
		  objectArray[2] = txnId;
		  objectArray[3] = createDate;
		  objectArray[4] = capturedDateFrom;
		  objectArray[5] = capturedDateTo;
		  objectArray[6] = utrNo;
		  objectArray[7] = payId;
		  objectArray[8] = bankAccountName;
		  objectArray[9] = bankAccountNumber;
		  objectArray[10] = bankIfsc;
		  objectArray[11] = currency;
		  objectArray[12] = txnType;
		  objectArray[13] = amount;
		  objectArray[14] = status;
		  objectArray[15] = responseMsg;
		  objectArray[16] = remarks;
		  
		  return objectArray;
		}
	public String getFileFor() {
		return fileFor;
	}
	public void setFileFor(String fileFor) {
		this.fileFor = fileFor;
	}
	
	public Object[] methodForTopUpReportDownloadFile() {
		  Object[] objectArray = new Object[8];
		  
		  objectArray[0] = srNo;
		  objectArray[1] = merchantName;
		  objectArray[2] = subMerchantName;
		  objectArray[3] = paymentType;
		  objectArray[4] = mopType;
		  objectArray[5] = amount;
		  objectArray[6] = createDate;
		  objectArray[7] = status;
		  
		  return objectArray;
		}

}
