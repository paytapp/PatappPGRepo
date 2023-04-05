package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;

import javax.persistence.Transient;

/**
 * @author PG
 *
 */
public class TransactionReconSearch implements Serializable {

	private static final long serialVersionUID = -4691009307357010956L;

	private String createDate;
	private String reservationId;
	private String bankTxnId;
	private String sid;
	private String amount;
	private String uploadDate;
	private String status;
	private String txnType;
	private String responseMessage;
	private String settlementFlag;
	private String postSettledFlag;
	private String settlementDate;
	private String acquirer;
	private Date dateTo;
	private Date dateFrom;
	private String cancelDate;
	private String postSettledCapture;
	private String srNo;
	
	private String paymentDate;
	private String txnDate;
	private String fileName;
	private String fileType;
	private String fileAmount;
	private String mprAmount;
	
	private String saleCount;
	private String saleAmount;
	private String refundCount;
	private String refundAmount;
	private String totalAmount;
	private String statementAmount;
	private String narration;
	private String diffAmount;
	private String bankName;
	private String merchantName;
	
	public TransactionReconSearch() {

	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getReservationId() {
		return reservationId;
	}

	public void setReservationId(String reservationId) {
		this.reservationId = reservationId;
	}

	public String getBankTxnId() {
		return bankTxnId;
	}

	public void setBankTxnId(String bankTxnId) {
		this.bankTxnId = bankTxnId;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(String uploadDate) {
		this.uploadDate = uploadDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getSettlementFlag() {
		return settlementFlag;
	}

	public void setSettlementFlag(String settlementFlag) {
		this.settlementFlag = settlementFlag;
	}

	public String getPostSettledFlag() {
		return postSettledFlag;
	}

	public void setPostSettledFlag(String postSettledFlag) {
		this.postSettledFlag = postSettledFlag;
	}

	public String getSettlementDate() {
		return settlementDate;
	}

	public void setSettlementDate(String settlementDate) {
		this.settlementDate = settlementDate;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getCancelDate() {
		return cancelDate;
	}

	public void setCancelDate(String cancelDate) {
		this.cancelDate = cancelDate;
	}

	public String getPostSettledCapture() {
		return postSettledCapture;
	}

	public void setPostSettledCapture(String postSettledCapture) {
		this.postSettledCapture = postSettledCapture;
	}

	public String getSrNo() {
		return srNo;
	}

	public void setSrNo(String srNo) {
		this.srNo = srNo;
	}
	

	public String getTxnDate() {
		return txnDate;
	}

	public void setTxnDate(String txnDate) {
		this.txnDate = txnDate;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileAmount() {
		return fileAmount;
	}

	public void setFileAmount(String fileAmount) {
		this.fileAmount = fileAmount;
	}

	public String getMprAmount() {
		return mprAmount;
	}

	public void setMprAmount(String mprAmount) {
		this.mprAmount = mprAmount;
	}

	public Object[] myCsvMethod() {
		Object[] objectArray = new Object[12];

		objectArray[0] = srNo;
		objectArray[1] = reservationId;
		objectArray[2] = bankTxnId;
		objectArray[3] = sid;
		objectArray[4] = amount;
		objectArray[5] = txnType;
		objectArray[6] = status;
		objectArray[7] = acquirer;
		objectArray[8] = createDate;
		objectArray[9] = settlementDate;
		objectArray[10] = settlementFlag;
		objectArray[11] = postSettledFlag;

		return objectArray;
	}

	public String getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getSaleCount() {
		return saleCount;
	}

	public void setSaleCount(String saleCount) {
		this.saleCount = saleCount;
	}

	public String getSaleAmount() {
		return saleAmount;
	}

	public void setSaleAmount(String saleAmount) {
		this.saleAmount = saleAmount;
	}

	public String getRefundCount() {
		return refundCount;
	}

	public void setRefundCount(String refundCount) {
		this.refundCount = refundCount;
	}

	public String getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(String refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getNarration() {
		return narration;
	}

	public void setNarration(String narration) {
		this.narration = narration;
	}

	public String getStatementAmount() {
		return statementAmount;
	}

	public void setStatementAmount(String statementAmount) {
		this.statementAmount = statementAmount;
	}

	public String getDiffAmount() {
		return diffAmount;
	}

	public void setDiffAmount(String diffAmount) {
		this.diffAmount = diffAmount;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

}
