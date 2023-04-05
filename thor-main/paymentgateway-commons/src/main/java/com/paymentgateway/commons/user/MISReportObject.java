package com.paymentgateway.commons.user;

public class MISReportObject {

	private String transactionId;
	private String pgRefNum;
	private String merchants;
	private String txnType;
	private String acquirerType;
	private String paymentMethods;
	private String dateFrom;
	private String orderId;
	private String srNo;
	private String payId;
	private String mopType;
	private String transactionDate;
	private String deltaRefundFlag;
	private String surchargeFlag;
	private String netMerchantPayableAmount;
	private String refundFlag;
	private String totalAmtPayable;
	private String accountNo;
	private String totalPayoutNodalAccount;
	private String aggregatorCommissionAMT;
	private String acquirerCommissionAMT;
	private String grossTransactionAmt;
	private String beneficiaryBankName;
	private String transactionRegion;
	private String cardHolderType;
	private String amount;
	private String totalAmount;
	private String acquirerMode;
	private String subMerchant;
	private String subMerchantPayId;
	private String refundOrderId;
	private String txnSettledType;
	private String sufCharges;
	
	
	
	public Object[] myCsvMethod() {
		  Object[] objectArray = new Object[25];
		  
		 
		  objectArray[0] = srNo;
		  objectArray[1] = merchants;
		  objectArray[2] = payId;
		  objectArray[3] = pgRefNum;
		  objectArray[4] = orderId;
		  objectArray[5] = transactionDate;
		  objectArray[6] = dateFrom;
		  objectArray[7] = txnType;
		  objectArray[8] = grossTransactionAmt;
		  objectArray[9] = aggregatorCommissionAMT;
		  objectArray[10] = sufCharges;
		  objectArray[11] = acquirerCommissionAMT;
		  objectArray[12] = totalAmtPayable;
		  objectArray[13] = totalPayoutNodalAccount;
		  objectArray[14] = acquirerType;
		  objectArray[15] = "";
		  objectArray[16] = "Payment Gateway";
		 // objectArray[16] = acquirerType;
		  objectArray[17] = acquirerMode;
		  objectArray[18] = refundFlag;
		  objectArray[19] = paymentMethods;
		  objectArray[20] = mopType;
		  objectArray[21] = cardHolderType;
		  objectArray[22] = transactionRegion;
		  objectArray[23] = refundOrderId;
		  objectArray[24] = txnSettledType;
		
		  return objectArray;
		}
	
	public Object[] myCsvMethodForSuperMerchant() {
		  Object[] objectArray = new Object[27];
		  
		 
		  objectArray[0] = srNo;
		  objectArray[1] = merchants;
		  objectArray[2] = payId;
		  objectArray[3] = subMerchant;
		  objectArray[4] = subMerchantPayId;
		  objectArray[5] = pgRefNum;
		  objectArray[6] = orderId;
		  objectArray[7] = transactionDate;
		  objectArray[8] = dateFrom;
		  objectArray[9] = txnType;
		  objectArray[10] = grossTransactionAmt;
		  objectArray[11] = aggregatorCommissionAMT;
		  objectArray[12] = sufCharges;
		  objectArray[13] = acquirerCommissionAMT;
		  objectArray[14] = totalAmtPayable;
		  objectArray[15] = totalPayoutNodalAccount;
		  objectArray[16] = acquirerType;
		  objectArray[17] = "";
		  objectArray[18] = "Payment Gateway";
		  objectArray[19] = acquirerMode;
		  objectArray[20] = refundFlag;
		  objectArray[21] = paymentMethods;
		  objectArray[22] = mopType;
		  objectArray[23] = cardHolderType;
		  objectArray[24] = transactionRegion;
		  objectArray[25] = refundOrderId;
		  objectArray[26] = txnSettledType;
		
		  return objectArray;
		}
	
	public String getNetMerchantPayableAmount() {
		return netMerchantPayableAmount;
	}
	public void setNetMerchantPayableAmount(String netMerchantPayableAmount) {
		this.netMerchantPayableAmount = netMerchantPayableAmount;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getPgRefNum() {
		return pgRefNum;
	}
	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}
	public String getMerchants() {
		return merchants;
	}
	public void setMerchants(String merchants) {
		this.merchants = merchants;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public String getAcquirerType() {
		return acquirerType;
	}
	public void setAcquirerType(String acquirerType) {
		this.acquirerType = acquirerType;
	}
	public String getPaymentMethods() {
		return paymentMethods;
	}
	public void setPaymentMethods(String paymentMethods) {
		this.paymentMethods = paymentMethods;
	}
	public String getDateFrom() {
		return dateFrom;
	}
	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getSrNo() {
		return srNo;
	}
	public void setSrNo(String srNo) {
		this.srNo = srNo;
	}
	public String getMopType() {
		return mopType;
	}
	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
	public String getDeltaRefundFlag() {
		return deltaRefundFlag;
	}
	public void setDeltaRefundFlag(String deltaRefundFlag) {
		this.deltaRefundFlag = deltaRefundFlag;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getSurchargeFlag() {
		return surchargeFlag;
	}
	public void setSurchargeFlag(String surchargeFlag) {
		this.surchargeFlag = surchargeFlag;
	}
	
	public String getTransactionDate() {
		return transactionDate;
	}
	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}
	public String getRefundFlag() {
		return refundFlag;
	}
	public void setRefundFlag(String refundFlag) {
		this.refundFlag = refundFlag;
	}
	public String getTotalAmtPayable() {
		return totalAmtPayable;
	}
	public void setTotalAmtPayable(String totalAmtPayable) {
		this.totalAmtPayable = totalAmtPayable;
	}
	public String getAccountNo() {
		return accountNo;
	}
	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}
	public String getTotalPayoutNodalAccount() {
		return totalPayoutNodalAccount;
	}
	public void setTotalPayoutNodalAccount(String totalPayoutNodalAccount) {
		this.totalPayoutNodalAccount = totalPayoutNodalAccount;
	}
	public String getAggregatorCommissionAMT() {
		return aggregatorCommissionAMT;
	}
	public void setAggregatorCommissionAMT(String aggregatorCommissionAMT) {
		this.aggregatorCommissionAMT = aggregatorCommissionAMT;
	}
	public String getAcquirerCommissionAMT() {
		return acquirerCommissionAMT;
	}
	public void setAcquirerCommissionAMT(String acquirerCommissionAMT) {
		this.acquirerCommissionAMT = acquirerCommissionAMT;
	}
	public String getGrossTransactionAmt() {
		return grossTransactionAmt;
	}
	public void setGrossTransactionAmt(String grossTransactionAmt) {
		this.grossTransactionAmt = grossTransactionAmt;
	}
	public String getBeneficiaryBankName() {
		return beneficiaryBankName;
	}
	public void setBeneficiaryBankName(String beneficiaryBankName) {
		this.beneficiaryBankName = beneficiaryBankName;
	}

	public String getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}

	public String getTransactionRegion() {
		return transactionRegion;
	}

	public void setTransactionRegion(String transactionRegion) {
		this.transactionRegion = transactionRegion;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}
	public String getAcquirerMode() {
		return acquirerMode;
	}

	public void setAcquirerMode(String acquirerMode) {
		this.acquirerMode = acquirerMode;
	}
	public String getSubMerchant() {
		return subMerchant;
	}

	public void setSubMerchant(String subMerchant) {
		this.subMerchant = subMerchant;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getRefundOrderId() {
		return refundOrderId;
	}

	public void setRefundOrderId(String refundOrderId) {
		this.refundOrderId = refundOrderId;
	}

	public String getTxnSettledType() {
		return txnSettledType;
	}

	public void setTxnSettledType(String txnSettledType) {
		this.txnSettledType = txnSettledType;
	}

	public String getSufCharges() {
		return sufCharges;
	}

	public void setSufCharges(String sufCharges) {
		this.sufCharges = sufCharges;
	}
	
	
}
