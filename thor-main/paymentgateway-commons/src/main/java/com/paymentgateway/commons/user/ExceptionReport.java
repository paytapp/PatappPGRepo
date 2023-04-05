package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.math.BigInteger;

public class ExceptionReport implements Serializable {
	
	private static final long serialVersionUID = -469100930735471956L;

	private String srNo;
	private String merchant;
	private String pgRefNo;
	private String txnId;
	private String orderId;
	private String acqId;
	private String createdDate;
	private String status;
	private String exception;
	private String pgSettledAmount;
	private String acqSettledAmount;
	private String diffAmount;
	private String settledFlag;
	
	public String getPgRefNo() {
		return pgRefNo;
	}
	public void setPgRefNo(String pgRefNo) {
		this.pgRefNo = pgRefNo;
	}
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	public String getAcqId() {
		return acqId;
	}
	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getException() {
		return exception;
	}
	public void setException(String exception) {
		this.exception = exception;
	}
	public String getPgSettledAmount() {
		return pgSettledAmount;
	}
	public void setPgSettledAmount(String pgSettledAmount) {
		this.pgSettledAmount = pgSettledAmount;
	}
	public String getAcqSettledAmount() {
		return acqSettledAmount;
	}
	public void setAcqSettledAmount(String acqSettledAmount) {
		this.acqSettledAmount = acqSettledAmount;
	}
	public String getDiffAmount() {
		return diffAmount;
	}
	public void setDiffAmount(String diffAmount) {
		this.diffAmount = diffAmount;
	}
	public String getSettledFlag() {
		return settledFlag;
	}
	public void setSettledFlag(String settledFlag) {
		this.settledFlag = settledFlag;
	}
	public String getSrNo() {
		return srNo;
	}
	public void setSrNo(String srNo) {
		this.srNo = srNo;
	}
	public String getMerchant() {
		return merchant;
	}
	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}
	
	public Object[] downloadBankExceptionReport() {
		  Object[] objectArray = new Object[13];
		  
		  objectArray[0] = srNo;
		  objectArray[1] = txnId;
		  objectArray[2] = pgRefNo;
		  objectArray[3] = merchant;
		  objectArray[4] = acqId;
		  objectArray[5] = createdDate;
		  objectArray[6] = orderId;
		  objectArray[7] = settledFlag;
		  objectArray[8] = status;
		  objectArray[9] = pgSettledAmount;
		  objectArray[10] = acqSettledAmount;
		  objectArray[11] = diffAmount;
		  objectArray[12] = exception;
		  
		  return objectArray;
		}
	
	
}
