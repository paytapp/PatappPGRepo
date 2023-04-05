package com.paymentgateway.commons.user;

public class ImpsDownloadObject {

	private static final long serialVersionUID = 5899705456765089877L;

	private String merchant;
	private String merchantPayId;
	private String subMerchantPayId;
	private String subMerchant;
	private String impsRefNum;
	private String date;
	private String txnsCapturedFrom;
	private String txnsCapturedTo;
	private String systemSettlementDate;
	private String bankAccountName;
	private String bankAccountNumber;
	private String bankIFSC;
	private String amount;
	private String status;
	private String txnId;
	private String responseMsg;
	private String phoneNo;
	private String userType;
	private String orderId;
	private String currencyCode;
	private String bankAccountNameReq;
	private String commissionAmount;
	private String serviceTax;

	// MID

	private String virtualAccount;
	private String beneAccountName;
	private String payerAddress;
	private String payerName;
	private String remarks;
	private String txnType;
	private String rrn;
	private String openingBalance;
	private String totalCredit;
	private String totalDebit;
	private String closingBalance;
	private String beneRegistration;
	private String totalTopup;
	private String purpose;
	
	//VPA
	private String accountType;
	private String ownerType;
	
	private String finalStatus;
	
	private String acquirerName;


	public String getFinalStatus() {
		return finalStatus;
	}

	public void setFinalStatus(String finalStatus) {
		this.finalStatus = finalStatus;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getBeneAccountName() {
		return beneAccountName;
	}

	public void setBeneAccountName(String beneAccountName) {
		this.beneAccountName = beneAccountName;
	}

	public String getVirtualAccount() {
		return virtualAccount;
	}

	public void setVirtualAccount(String virtualAccount) {
		this.virtualAccount = virtualAccount;
	}

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public String getSubMerchant() {
		return subMerchant;
	}

	public void setSubMerchant(String subMerchant) {
		this.subMerchant = subMerchant;
	}

	public String getImpsRefNum() {
		return impsRefNum;
	}

	public void setImpsRefNum(String impsRefNum) {
		this.impsRefNum = impsRefNum;
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

	public String getBankIFSC() {
		return bankIFSC;
	}

	public void setBankIFSC(String bankIFSC) {
		this.bankIFSC = bankIFSC;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTxnsCapturedFrom() {
		return txnsCapturedFrom;
	}

	public void setTxnsCapturedFrom(String txnsCapturedFrom) {
		this.txnsCapturedFrom = txnsCapturedFrom;
	}

	public String getTxnsCapturedTo() {
		return txnsCapturedTo;
	}

	public void setTxnsCapturedTo(String txnsCapturedTo) {
		this.txnsCapturedTo = txnsCapturedTo;
	}

	public String getSystemSettlementDate() {
		return systemSettlementDate;
	}

	public void setSystemSettlementDate(String systemSettlementDate) {
		this.systemSettlementDate = systemSettlementDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

	public String getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(String openingBalance) {
		this.openingBalance = openingBalance;
	}

	public String getTotalCredit() {
		return totalCredit;
	}

	public void setTotalCredit(String totalCredit) {
		this.totalCredit = totalCredit;
	}

	public String getTotalDebit() {
		return totalDebit;
	}

	public void setTotalDebit(String totalDebit) {
		this.totalDebit = totalDebit;
	}

	public String getClosingBalance() {
		return closingBalance;
	}

	public void setClosingBalance(String closingBalance) {
		this.closingBalance = closingBalance;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}
	
	public String getPayerAddress() {
		return payerAddress;
	}

	public void setPayerAddress(String payerAddress) {
		this.payerAddress = payerAddress;
	}

	public String getPayerName() {
		return payerName;
	}

	public void setPayerName(String payerName) {
		this.payerName = payerName;
	}

	
	public String getBeneRegistration() {
		return beneRegistration;
	}

	public void setBeneRegistration(String beneRegistration) {
		this.beneRegistration = beneRegistration;
	}

	public String getTotalTopup() {
		return totalTopup;
	}

	public void setTotalTopup(String totalTopup) {
		this.totalTopup = totalTopup;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getAcquirerName() {
		return acquirerName;
	}

	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}
	

	public String getCommissionAmount() {
		return commissionAmount;
	}

	public String getServiceTax() {
		return serviceTax;
	}

	public void setCommissionAmount(String commissionAmount) {
		this.commissionAmount = commissionAmount;
	}

	public void setServiceTax(String serviceTax) {
		this.serviceTax = serviceTax;
	}

	public Object[] myCsvMethodDownloadImpsReportByView() {
		Object[] objectArray = new Object[17];

		objectArray[0] = merchant;
		objectArray[1] = txnId;
		objectArray[2] = merchantPayId;
		objectArray[3] = orderId;
		objectArray[4] = phoneNo;
		objectArray[5] = userType;
		objectArray[6] = impsRefNum;
		objectArray[7] = date;
		objectArray[8] = txnsCapturedFrom;
		objectArray[9] = txnsCapturedTo;
		objectArray[10] = systemSettlementDate;
		objectArray[11] = bankAccountName;
		objectArray[12] = bankAccountNumber;
		objectArray[13] = bankIFSC;
		objectArray[14] = amount;
		objectArray[15] = responseMsg;
		objectArray[16] = status;

		return objectArray;
	}

	public Object[] myCsvMethodDownloadImpsReportBySubMerchantView() {
		Object[] objectArray = new Object[18];

		objectArray[0] = merchant;
		objectArray[1] = subMerchant;
		objectArray[2] = txnId;
		objectArray[3] = merchantPayId;
		objectArray[4] = orderId;
		objectArray[5] = phoneNo;
		objectArray[6] = userType;
		objectArray[7] = impsRefNum;
		objectArray[8] = date;
		objectArray[9] = txnsCapturedFrom;
		objectArray[10] = txnsCapturedTo;
		objectArray[11] = systemSettlementDate;
		objectArray[12] = bankAccountName;
		objectArray[13] = bankAccountNumber;
		objectArray[14] = bankIFSC;
		objectArray[15] = amount;
		objectArray[16] = responseMsg;
		objectArray[17] = status;
		return objectArray;
	}

	public Object[] csvMethodDownloadForSubMerchantImpsReport() {
		Object[] objectArray = new Object[16];

		objectArray[0] = merchant;
		objectArray[1] = subMerchant;
		objectArray[2] = txnId;
		objectArray[3] = merchantPayId;
		objectArray[4] = orderId;
		objectArray[5] = phoneNo;
		objectArray[6] = userType;
		objectArray[7] = impsRefNum;
		objectArray[8] = date;
		objectArray[9] = systemSettlementDate;
		objectArray[10] = bankAccountName;
		objectArray[11] = bankAccountNumber;
		objectArray[12] = bankIFSC;
		objectArray[13] = amount;
		objectArray[14] = responseMsg;
		objectArray[15] = status;

		return objectArray;
	}

	public Object[] csvMethodDownloadForMerchantImpsReport() {
		Object[] objectArray = new Object[15];

		objectArray[0] = merchant;
		objectArray[1] = txnId;
		objectArray[2] = merchantPayId;
		objectArray[3] = orderId;
		objectArray[4] = phoneNo;
		objectArray[5] = userType;
		objectArray[6] = impsRefNum;
		objectArray[7] = date;
		objectArray[8] = systemSettlementDate;
		objectArray[9] = bankAccountName;
		objectArray[10] = bankAccountNumber;
		objectArray[11] = bankIFSC;
		objectArray[12] = amount;
		objectArray[13] = responseMsg;
		objectArray[14] = status;

		return objectArray;
	}

	public Object[] myCsvMethodDownloadPGInitiatedForSubMerchant() {
		Object[] objectArray = new Object[12];

		objectArray[0] = merchant;
		objectArray[1] = subMerchant;
		objectArray[2] = txnId;
		objectArray[3] = orderId;
		objectArray[4] = date;
		objectArray[5] = phoneNo;
		objectArray[6] = bankAccountName;
		objectArray[7] = bankAccountNumber;
		objectArray[8] = bankIFSC;
		objectArray[9] = amount;
		objectArray[10] = responseMsg;
		objectArray[11] = status;
		return objectArray;
	}

	public Object[] myCsvMethodDownloadPGInitiatedForMerchant() {
		Object[] objectArray = new Object[11];

		objectArray[0] = merchant;
		objectArray[1] = txnId;
		objectArray[2] = orderId;
		objectArray[3] = date;
		objectArray[4] = phoneNo;
		objectArray[5] = bankAccountName;
		objectArray[6] = bankAccountNumber;
		objectArray[7] = bankIFSC;
		objectArray[8] = amount;
		objectArray[9] = responseMsg;
		objectArray[10] = status;
		return objectArray;
	}

	public Object[] csvForBeneVerificationSuperMerchant() {
		Object[] objectArray = new Object[13];

		objectArray[0] = merchant;
		objectArray[1] = subMerchant;
		objectArray[2] = merchantPayId;
		objectArray[3] = txnId;
		objectArray[4] = orderId;
		objectArray[5] = bankAccountName;
		objectArray[6] = bankAccountNumber;
		objectArray[7] = bankIFSC;
		objectArray[8] = date;
		objectArray[9] = phoneNo;
		objectArray[10] = status;
		objectArray[11] = responseMsg;
		objectArray[12] = bankAccountNameReq;
		return objectArray;
	}

	public String getBankAccountNameReq() {
		return bankAccountNameReq;
	}

	public void setBankAccountNameReq(String bankAccountNameReq) {
		this.bankAccountNameReq = bankAccountNameReq;
	}

	public Object[] csvForBeneVerificationMerchant() {
		Object[] objectArray = new Object[12];

		objectArray[0] = merchant;
		objectArray[1] = merchantPayId;
		objectArray[2] = txnId;
		objectArray[3] = orderId;
		objectArray[4] = bankAccountName;
		objectArray[5] = bankAccountNumber;
		objectArray[6] = bankIFSC;
		objectArray[7] = date;
		objectArray[8] = phoneNo;
		objectArray[9] = status;
		objectArray[10] = responseMsg;
		objectArray[11] = bankAccountNameReq;

		return objectArray;
	}

	public Object[] myCsvMethodDownloadMerchantInitiateDirectSub() {
		Object[] objectArray = new Object[19];

		objectArray[0] = merchant;
		objectArray[1] = subMerchant;
		objectArray[2] = txnId;
		objectArray[3] = impsRefNum;
		objectArray[4] = merchantPayId;
		objectArray[5] = orderId;
		objectArray[6] = date;
		objectArray[7] = phoneNo;
		objectArray[8] = userType;
		objectArray[9] = rrn;
		objectArray[10] = beneAccountName;
		objectArray[11] = bankAccountName;
		objectArray[12] = bankAccountNumber;
		objectArray[13] = bankIFSC;
		objectArray[14] = amount;
		objectArray[15] = txnType;
		objectArray[16] = responseMsg;
		objectArray[17] = status;
		objectArray[18] = purpose;

		return objectArray;
	}

	public Object[] myCsvMethodDownloadMerchantInitiateDirectMerch() {
		Object[] objectArray = new Object[18];

		objectArray[0] = merchant;
		objectArray[1] = txnId;
		objectArray[2] = impsRefNum;
		objectArray[3] = merchantPayId;
		objectArray[4] = orderId;
		objectArray[5] = date;
		objectArray[6] = phoneNo;
		objectArray[7] = userType;
		objectArray[8] = rrn;
		objectArray[9] = beneAccountName;
		objectArray[10] = bankAccountName;
		objectArray[11] = bankAccountNumber;
		objectArray[12] = bankIFSC;
		objectArray[13] = amount;
		objectArray[14] = txnType;
		objectArray[15] = responseMsg;
		objectArray[16] = status;
		objectArray[17] = purpose;

		return objectArray;
	}

	public Object[] myCsvMethodDownloadMerchantInitiateDirectLedgerReportSub() {
		Object[] objectArray = new Object[7];

		objectArray[0] = merchant;
		objectArray[1] = subMerchant;
		objectArray[2] = date;
		objectArray[3] = openingBalance;
		objectArray[4] = totalCredit;
		objectArray[5] = totalDebit;
		objectArray[6] = closingBalance;

		return objectArray;
	}

	public Object[] myCsvMethodDownloadMerchantInitiateDirectLedgerReportMerch() {
		Object[] objectArray = new Object[6];

		objectArray[0] = merchant;
		objectArray[1] = date;
		objectArray[2] = openingBalance;
		objectArray[3] = totalCredit;
		objectArray[4] = totalDebit;
		objectArray[5] = closingBalance;

		return objectArray;
	}

	public Object[] myCsvMethodDownloadMerchantInitiateDirectLedgerSub() {
		Object[] objectArray = new Object[9];

		objectArray[0] = merchant;
		objectArray[1] = subMerchant;
		objectArray[2] = rrn;
		objectArray[3] = date;
		objectArray[4] = openingBalance;
		objectArray[5] = totalCredit;
		objectArray[6] = totalTopup;
		objectArray[7] = totalDebit;
		objectArray[8] = closingBalance;

		return objectArray;
	}

	public Object[] myCsvMethodDownloadMerchantInitiateDirectLedgerMerch() {
		Object[] objectArray = new Object[8];

		objectArray[0] = merchant;
		objectArray[1] = rrn;
		objectArray[2] = date;
		objectArray[3] = openingBalance;
		objectArray[4] = totalCredit;
		objectArray[5] = totalTopup;
		objectArray[6] = totalDebit;
		objectArray[7] = closingBalance;

		return objectArray;
	}
	
	public Object[] myXlsxMethodDownloadBeneRegistrationReportSub() {
		Object[] objectArray = new Object[9];

		objectArray[0] = merchant;
		objectArray[1] = subMerchant;
		objectArray[2] = orderId;
		objectArray[3] = txnType;
		objectArray[4] = phoneNo;
		objectArray[5] = beneAccountName;
		objectArray[6] = bankAccountNumber;
		objectArray[7] = bankIFSC;
		objectArray[8] = status;

		return objectArray;
	}
	
	public Object[] myXlsxMethodDownloadBeneRegistrationReportMer() {
		Object[] objectArray = new Object[8];

		objectArray[0] = merchant;
		objectArray[1] = orderId;
		objectArray[2] = txnType;
		objectArray[3] = phoneNo;
		objectArray[4] = beneAccountName;
		objectArray[5] = bankAccountNumber;
		objectArray[6] = bankIFSC;
		objectArray[7] = status;

		return objectArray;
	}
	
	public Object[] csvForVpaBeneVerificationSuperMerchant() {
		Object[] objectArray = new Object[12];

		objectArray[0] = merchant;
		objectArray[1] = subMerchant;
		objectArray[2] = merchantPayId;
		objectArray[3] = txnId;
		objectArray[4] = orderId;
		objectArray[5] = payerName;
		objectArray[6] = payerAddress;
		objectArray[7] = bankIFSC;
		objectArray[8] = date;
		objectArray[9] = phoneNo;
		objectArray[10] = status;
		objectArray[11] = responseMsg;

		return objectArray;
	}
	
	public Object[] csvForVpaBeneVerificationMerchant() {
		Object[] objectArray = new Object[12];

		objectArray[0] = merchant;
		objectArray[1] = merchantPayId;
		objectArray[2] = txnId;
		objectArray[3] = orderId;
		objectArray[4] = payerName;
		objectArray[5] = payerAddress;
		objectArray[6] = bankIFSC;
		objectArray[7] = date;
		objectArray[8] = phoneNo;
		objectArray[9] = status;
		objectArray[10] = responseMsg;

		return objectArray;
	}
	
	public Object[] csvForPayoutFailed() {
		Object[] objectArray = new Object[9];

		objectArray[0] = orderId;
		objectArray[1] = beneAccountName;
		objectArray[2] = bankAccountNumber;
		objectArray[3] = bankIFSC;
		objectArray[4] = bankAccountName;
		objectArray[5] = phoneNo;
		objectArray[6] = amount;
		objectArray[7] = purpose;
		objectArray[8] = remarks;

		return objectArray;
	}
	
	public Object[] csvForPayoutFailedUpi() {
		Object[] objectArray = new Object[7];

		objectArray[0] = orderId;
		objectArray[1] = payerAddress;
		objectArray[2] = payerName;
		objectArray[3] = phoneNo;
		objectArray[4] = amount;
		objectArray[5] = purpose;
		objectArray[6] = remarks;

		return objectArray;
	}

}
