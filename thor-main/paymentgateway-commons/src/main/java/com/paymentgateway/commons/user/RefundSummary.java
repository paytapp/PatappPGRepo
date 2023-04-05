package com.paymentgateway.commons.user;

import java.io.Serializable;
/**
 * @author Vijaya
 *
 */
public class RefundSummary implements Serializable{

	private static final long serialVersionUID = -9088039031508070909L;

	private String acquirer;
	private String paymentType;
	private String mop;
	private String TxnInitiate;
	private String captured;
	private String rejected;
	private String declined;
	private String pending;
	private String error;
	private String timeout;
	private String failed;
	private String invalid;
	private String acqDown;
	private String failedAtAcq;
	private String acqTimeout;
	private String txnSettledType;
	
	
	public String getAcquirer() {
		return acquirer;
	}
	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getMop() {
		return mop;
	}
	public void setMop(String mop) {
		this.mop = mop;
	}
	public String getTxnInitiate() {
		return TxnInitiate;
	}
	public void setTxnInitiate(String txnInitiate) {
		TxnInitiate = txnInitiate;
	}
	public String getCaptured() {
		return captured;
	}
	public void setCaptured(String captured) {
		this.captured = captured;
	}
	public String getRejected() {
		return rejected;
	}
	public void setRejected(String rejected) {
		this.rejected = rejected;
	}
	public String getDeclined() {
		return declined;
	}
	public void setDeclined(String declined) {
		this.declined = declined;
	}
	public String getPending() {
		return pending;
	}
	public void setPending(String pending) {
		this.pending = pending;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getTimeout() {
		return timeout;
	}
	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}
	public String getFailed() {
		return failed;
	}
	public void setFailed(String failed) {
		this.failed = failed;
	}
	public String getInvalid() {
		return invalid;
	}
	public void setInvalid(String invalid) {
		this.invalid = invalid;
	}
	public String getAcqDown() {
		return acqDown;
	}
	public void setAcqDown(String acqDown) {
		this.acqDown = acqDown;
	}
	public String getFailedAtAcq() {
		return failedAtAcq;
	}
	public void setFailedAtAcq(String failedAtAcq) {
		this.failedAtAcq = failedAtAcq;
	}
	public String getAcqTimeout() {
		return acqTimeout;
	}
	public void setAcqTimeout(String acqTimeout) {
		this.acqTimeout = acqTimeout;
	}
	public String getTxnSettledType() {
		return txnSettledType;
	}
	public void setTxnSettledType(String txnSettledType) {
		this.txnSettledType = txnSettledType;
	}
	
	
}
