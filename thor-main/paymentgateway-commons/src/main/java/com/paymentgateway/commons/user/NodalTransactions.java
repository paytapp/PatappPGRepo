package com.paymentgateway.commons.user;

public class NodalTransactions {

	private Long id;

	private String status;
	private String requestedBy;
	private String processedBy;
	private String acquirer;
	private String amount;
	private String txnId;
	private String rrn;
	private String txnType;
	private String oid;
	private String customerId;
	private String srcAccNo;
	private String beneAccNo;
	private String currencyCode;
	private String responseCode;
	private String createdDate;
	private String pgRespCode;
	private String paymentType;
	private String comments;
	private String beneficiaryName;
	private String beneficiaryCode;

	private String srNo;
	private String merchantName;
	private String subMerchantName;
	private String payId;
	private String subMerchantId;
	private String captureFromDate;
	private String captureToDate;
	private String payOutDate;
	private String saleCaptureTxn;
	private String saleCaptureAmnt;
	private String refundCaptureTxn;
	private String refundCaptureAmnt;
	private String saleSettledTxn;
	private String saleSettledAmnt;
	private String refundSettledTxn;
	private String refundSettledAmnt;
	private String chargebackCr;
	private String chargebackDr;
	private String otherAdjustmentCr;
	private String otherAdjustmentDr;
	private String netSettled;
	private String fileName;

	
	public Object[] netSettledConsolidatedDownload() {
		Object[] objectArray = new Object[20];

		objectArray[0] = srNo;
		objectArray[1] = merchantName;
		objectArray[2] = subMerchantName;
		objectArray[3] = captureFromDate;
		objectArray[4] = captureToDate;
		objectArray[5] = payOutDate;
		objectArray[6] = saleCaptureTxn;
		objectArray[7] = saleCaptureAmnt;
		objectArray[8] = refundCaptureTxn;
		objectArray[9] = refundCaptureAmnt;
		objectArray[10] = saleSettledTxn;
		objectArray[11] = saleSettledAmnt;
		objectArray[12] = refundSettledTxn;
		objectArray[13] = refundSettledAmnt;
		objectArray[14] = chargebackCr;
		objectArray[15] = chargebackDr;
		objectArray[16] = otherAdjustmentCr;
		objectArray[17] = otherAdjustmentDr;
		objectArray[18] = netSettled;

		return objectArray;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

	public String getProcessedBy() {
		return processedBy;
	}

	public void setProcessedBy(String processedBy) {
		this.processedBy = processedBy;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getSrcAccNo() {
		return srcAccNo;
	}

	public void setSrcAccNo(String srcAccNo) {
		this.srcAccNo = srcAccNo;
	}

	public String getBeneAccNo() {
		return beneAccNo;
	}

	public void setBeneAccNo(String beneAccNo) {
		this.beneAccNo = beneAccNo;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getPgRespCode() {
		return pgRespCode;
	}

	public void setPgRespCode(String pgRespCode) {
		this.pgRespCode = pgRespCode;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getBeneficiaryName() {
		return beneficiaryName;
	}

	public void setBeneficiaryName(String beneficiaryName) {
		this.beneficiaryName = beneficiaryName;
	}

	public String getBeneficiaryCode() {
		return beneficiaryCode;
	}

	public void setBeneficiaryCode(String beneficiaryCode) {
		this.beneficiaryCode = beneficiaryCode;
	}

	public String getPayOutDate() {
		return payOutDate;
	}

	public void setPayOutDate(String payOutDate) {
		this.payOutDate = payOutDate;
	}

	public String getSaleCaptureTxn() {
		return saleCaptureTxn;
	}

	public void setSaleCaptureTxn(String saleCaptureTxn) {
		this.saleCaptureTxn = saleCaptureTxn;
	}

	public String getSaleCaptureAmnt() {
		return saleCaptureAmnt;
	}

	public void setSaleCaptureAmnt(String saleCaptureAmnt) {
		this.saleCaptureAmnt = saleCaptureAmnt;
	}

	public String getRefundCaptureTxn() {
		return refundCaptureTxn;
	}

	public void setRefundCaptureTxn(String refundCaptureTxn) {
		this.refundCaptureTxn = refundCaptureTxn;
	}

	public String getRefundCaptureAmnt() {
		return refundCaptureAmnt;
	}

	public void setRefundCaptureAmnt(String refundCaptureAmnt) {
		this.refundCaptureAmnt = refundCaptureAmnt;
	}

	public String getSaleSettledTxn() {
		return saleSettledTxn;
	}

	public void setSaleSettledTxn(String saleSettledTxn) {
		this.saleSettledTxn = saleSettledTxn;
	}

	public String getSaleSettledAmnt() {
		return saleSettledAmnt;
	}

	public void setSaleSettledAmnt(String saleSettledAmnt) {
		this.saleSettledAmnt = saleSettledAmnt;
	}

	public String getRefundSettledTxn() {
		return refundSettledTxn;
	}

	public void setRefundSettledTxn(String refundSettledTxn) {
		this.refundSettledTxn = refundSettledTxn;
	}

	public String getRefundSettledAmnt() {
		return refundSettledAmnt;
	}

	public void setRefundSettledAmnt(String refundSettledAmnt) {
		this.refundSettledAmnt = refundSettledAmnt;
	}

	public String getChargebackCr() {
		return chargebackCr;
	}

	public void setChargebackCr(String chargebackCr) {
		this.chargebackCr = chargebackCr;
	}

	public String getChargebackDr() {
		return chargebackDr;
	}

	public void setChargebackDr(String chargebackDr) {
		this.chargebackDr = chargebackDr;
	}

	public String getOtherAdjustmentCr() {
		return otherAdjustmentCr;
	}

	public void setOtherAdjustmentCr(String otherAdjustmentCr) {
		this.otherAdjustmentCr = otherAdjustmentCr;
	}

	public String getOtherAdjustmentDr() {
		return otherAdjustmentDr;
	}

	public void setOtherAdjustmentDr(String otherAdjustmentDr) {
		this.otherAdjustmentDr = otherAdjustmentDr;
	}

	public String getNetSettled() {
		return netSettled;
	}

	public void setNetSettled(String netSettled) {
		this.netSettled = netSettled;
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

	public String getCaptureFromDate() {
		return captureFromDate;
	}

	public void setCaptureFromDate(String captureFromDate) {
		this.captureFromDate = captureFromDate;
	}

	public String getCaptureToDate() {
		return captureToDate;
	}

	public void setCaptureToDate(String captureToDate) {
		this.captureToDate = captureToDate;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}
	public String getSrNo() {
		return srNo;
	}
	public void setSrNo(String srNo) {
		this.srNo = srNo;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
