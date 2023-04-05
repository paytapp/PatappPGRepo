package com.paymentgateway.commons.user;

public class SummaryReportObject {

	private String transactionId;
	private String pgRefNum;
	private String transactionRegion;
	private String merchants;
	private String postSettledFlag;
	private String txnType;
	private String acquirerType;
	private String paymentMethods;
	private String dateFrom;
	private String amount;
	private String orderId;
	private String totalAmount;
	private String srNo;
	private String payId;
	private String mopType;
	private String currency;
	private String cardHolderType;
	private String captureDate;
	private String settlementDate;
	private String tdrScAcquirer;
	private String tdrScPg;
	private String tdrScPaymentGateway;
	private String gstScAcquirer;
	private String gstScPg;
	private String gstScPaymentGateway;
	private String merchantAmount;
	private String acqId;
	private String rrn;
	private String deltaRefundFlag;
	private String surchargeFlag;
	private String netMerchantPayableAmount;
	private String status;
	private String nodalDate;
	private String acquirerMode;
	private String sufCharge;
	private String partSettledFlag;
	private String subMerchantId;
	private String resellerCharges;
	private String resellerGst;
	private String merchantTdrOrSc;
	private String merchantGst;
	private String txnSettledType;
	private String refundOrderId;

	public Object[] myCsvMethodDownloadSummaryReport() {
		  Object[] objectArray = new Object[32];
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = paymentMethods;
		  objectArray[4] = mopType;
		  objectArray[5] = orderId;
		  objectArray[6] = merchants;
		  objectArray[7] = currency;
		  objectArray[8] = txnType;
		  objectArray[9] = captureDate;
		  objectArray[10] = dateFrom;
		  objectArray[11] = transactionRegion;
		  objectArray[12] = cardHolderType;
		  objectArray[13] = acquirerMode;
		  objectArray[14] = acquirerType;
		  objectArray[15] = totalAmount;
		  objectArray[16] = merchantTdrOrSc;
		  objectArray[17] = tdrScAcquirer;
		  objectArray[18] = tdrScPaymentGateway;
		  objectArray[19] = resellerCharges;
		  objectArray[20] = merchantGst;
		  objectArray[21] = gstScAcquirer;
		  objectArray[22] = gstScPaymentGateway;
		  objectArray[23] = resellerGst;
		  objectArray[24] = merchantAmount;
		  objectArray[25] = sufCharge;
		  objectArray[26] = acqId;
		  objectArray[27] = rrn;
		  objectArray[28] = txnSettledType;
//		  objectArray[29] = deltaRefundFlag;
		  objectArray[29] = partSettledFlag;
		  objectArray[30] = refundOrderId;
		  objectArray[31] = txnSettledType;
		  
		  return objectArray;
		}
	
	public Object[] myCsvMethodDownloadSummaryReportForSuperMerchant() {
		  Object[] objectArray = new Object[33];
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = paymentMethods;
		  objectArray[4] = mopType;
		  objectArray[5] = orderId;
		  objectArray[6] = merchants;
		  objectArray[7] = subMerchantId;
		  objectArray[8] = currency;
		  objectArray[9] = txnType;
		  objectArray[10] = captureDate;
		  objectArray[11] = dateFrom;
		  objectArray[12] = transactionRegion;
		  objectArray[13] = cardHolderType;
		  objectArray[14] = acquirerMode;
		  objectArray[15] = acquirerType;
		  objectArray[16] = totalAmount;
		  objectArray[17] = merchantTdrOrSc;
		  objectArray[18] = tdrScAcquirer;
		  objectArray[19] = tdrScPaymentGateway;
		  objectArray[20] = resellerCharges;
		  objectArray[21] = merchantGst;
		  objectArray[22] = gstScAcquirer;
		  objectArray[23] = gstScPaymentGateway;
		  objectArray[24] = resellerGst;
		  objectArray[25] = merchantAmount;
		  objectArray[26] = sufCharge;
		  objectArray[27] = acqId;
		  objectArray[28] = rrn;
		  objectArray[29] = txnSettledType;
//		  objectArray[30] = deltaRefundFlag;
		  objectArray[30] = partSettledFlag;
		  objectArray[31] = refundOrderId;
		  objectArray[32] = txnSettledType;
		  
		  return objectArray;
		}
	
	public Object[] downloadNodalSettlement() {
		  Object[] objectArray = new Object[30];
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = paymentMethods;
		  objectArray[4] = mopType;
		  objectArray[5] = orderId;
		  objectArray[6] = merchants;
		  objectArray[7] = currency;
		  objectArray[8] = txnType;
		  objectArray[9] = captureDate;
		  objectArray[10] = dateFrom;
		  objectArray[11] = transactionRegion;
		  objectArray[12] = cardHolderType;
		  objectArray[13] = acquirerType;
		  objectArray[14] = totalAmount;
		  objectArray[15] = tdrScAcquirer;
		  objectArray[16] = tdrScPg;
		  objectArray[17] = tdrScPaymentGateway;
		  objectArray[18] = gstScAcquirer;
		  objectArray[19] = gstScPg;
		  objectArray[20] = gstScPaymentGateway;
		  objectArray[21] = amount;
		  objectArray[22] = acqId;
		  objectArray[23] = rrn;
		  objectArray[24] = postSettledFlag;
		  objectArray[25] = deltaRefundFlag;
		  objectArray[26] = status;
		  objectArray[27] = nodalDate;
		  
		  return objectArray;
		  
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


	public String getTransactionRegion() {
		return transactionRegion;
	}


	public void setTransactionRegion(String transactionRegion) {
		this.transactionRegion = transactionRegion;
	}


	public String getMerchants() {
		return merchants;
	}


	public void setMerchants(String merchants) {
		this.merchants = merchants;
	}


	public String getPostSettledFlag() {
		return postSettledFlag;
	}


	public void setPostSettledFlag(String postSettledFlag) {
		this.postSettledFlag = postSettledFlag;
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


	public String getAmount() {
		return amount;
	}


	public void setAmount(String amount) {
		this.amount = amount;
	}


	public String getOrderId() {
		return orderId;
	}


	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}


	public String getTotalAmount() {
		return totalAmount;
	}


	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}


	public String getSrNo() {
		return srNo;
	}


	public void setSrNo(String srNo) {
		this.srNo = srNo;
	}


	public String getPayId() {
		return payId;
	}


	public void setPayId(String payId) {
		this.payId = payId;
	}


	public String getMopType() {
		return mopType;
	}


	public void setMopType(String mopType) {
		this.mopType = mopType;
	}


	public String getCurrency() {
		return currency;
	}


	public void setCurrency(String currency) {
		this.currency = currency;
	}


	public String getCardHolderType() {
		return cardHolderType;
	}


	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}


	public String getCaptureDate() {
		return captureDate;
	}


	public void setCaptureDate(String captureDate) {
		this.captureDate = captureDate;
	}


	public String getSettlementDate() {
		return settlementDate;
	}


	public void setSettlementDate(String settlementDate) {
		this.settlementDate = settlementDate;
	}


	public String getTdrScAcquirer() {
		return tdrScAcquirer;
	}


	public void setTdrScAcquirer(String tdrScAcquirer) {
		this.tdrScAcquirer = tdrScAcquirer;
	}


	public String getTdrScPg() {
		return tdrScPg;
	}


	public void setTdrScPg(String tdrScPg) {
		this.tdrScPg = tdrScPg;
	}


	public String getTdrScPaymentGateway() {
		return tdrScPaymentGateway;
	}


	public void setTdrScPaymentGateway(String tdrScPaymentGateway) {
		this.tdrScPaymentGateway = tdrScPaymentGateway;
	}


	public String getGstScAcquirer() {
		return gstScAcquirer;
	}


	public void setGstScAcquirer(String gstScAcquirer) {
		this.gstScAcquirer = gstScAcquirer;
	}


	public String getGstScPg() {
		return gstScPg;
	}


	public void setGstScPg(String gstScPg) {
		this.gstScPg = gstScPg;
	}


	public String getGstScPaymentGateway() {
		return gstScPaymentGateway;
	}


	public void setGstScPaymentGateway(String gstScPaymentGateway) {
		this.gstScPaymentGateway = gstScPaymentGateway;
	}


	public String getMerchantAmount() {
		return merchantAmount;
	}


	public void setMerchantAmount(String merchantAmount) {
		this.merchantAmount = merchantAmount;
	}


	public String getAcqId() {
		return acqId;
	}


	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}


	public String getRrn() {
		return rrn;
	}


	public void setRrn(String rrn) {
		this.rrn = rrn;
	}


	public String getDeltaRefundFlag() {
		return deltaRefundFlag;
	}


	public void setDeltaRefundFlag(String deltaRefundFlag) {
		this.deltaRefundFlag = deltaRefundFlag;
	}


	public String getSurchargeFlag() {
		return surchargeFlag;
	}


	public void setSurchargeFlag(String surchargeFlag) {
		this.surchargeFlag = surchargeFlag;
	}


	public String getNetMerchantPayableAmount() {
		return netMerchantPayableAmount;
	}


	public void setNetMerchantPayableAmount(String netMerchantPayableAmount) {
		this.netMerchantPayableAmount = netMerchantPayableAmount;
	}


	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getNodalDate() {
		return nodalDate;
	}
	public void setNodalDate(String nodalDate) {
		this.nodalDate = nodalDate;
	}
	public String getAcquirerMode() {
		return acquirerMode;
	}
	public void setAcquirerMode(String acquirerMode) {
		this.acquirerMode = acquirerMode;
	}
	
	public String getSufCharge() {
		return sufCharge;
	}
	public void setSufCharge(String sufCharge) {
		this.sufCharge = sufCharge;
	}
	public String getPartSettledFlag() {
		return partSettledFlag;
	}
	public void setPartSettledFlag(String partSettledFlag) {
		this.partSettledFlag = partSettledFlag;
	}
	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}
	public String getResellerCharges() {
		return resellerCharges;
	}

	public void setResellerCharges(String resellerCharges) {
		this.resellerCharges = resellerCharges;
	}

	public String getResellerGst() {
		return resellerGst;
	}

	public void setResellerGst(String resellerGst) {
		this.resellerGst = resellerGst;
	}
	public String getMerchantTdrOrSc() {
		return merchantTdrOrSc;
	}

	public void setMerchantTdrOrSc(String merchantTdrOrSc) {
		this.merchantTdrOrSc = merchantTdrOrSc;
	}

	public String getMerchantGst() {
		return merchantGst;
	}

	public void setMerchantGst(String merchantGst) {
		this.merchantGst = merchantGst;
	}

	public String getTxnSettledType() {
		return txnSettledType;
	}

	public void setTxnSettledType(String txnSettledType) {
		this.txnSettledType = txnSettledType;
	}

	public String getRefundOrderId() {
		return refundOrderId;
	}

	public void setRefundOrderId(String refundOrderId) {
		this.refundOrderId = refundOrderId;
	}
	
}
