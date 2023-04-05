package com.paymentgateway.commons.user;

import java.io.Serializable;

public class RefundValidation implements Serializable {

	private static final long serialVersionUID = -4691009307574520956L;
	
	private String orderId;
	private String refundTag;
	private String refundAmt;
	private String BankTxnId;
	private String refundStatus;
	private String refundProcessDate;
	private String refundBankTxnId;
	private String cancelTxnDate;
	private String bookingTxnAmt;
	private String cancelTxnId;
	private String remarks;
	private String oid;
	private String arn;
	private String rrn;
	
	
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getRefundTag() {
		return refundTag;
	}
	public void setRefundTag(String refundTag) {
		this.refundTag = refundTag;
	}
	public String getRefundAmt() {
		return refundAmt;
	}
	public void setRefundAmt(String refundAmt) {
		this.refundAmt = refundAmt;
	}
	public String getBankTxnId() {
		return BankTxnId;
	}
	public void setBankTxnId(String bankTxnId) {
		BankTxnId = bankTxnId;
	}
	public String getRefundStatus() {
		return refundStatus;
	}
	public void setRefundStatus(String refundStatus) {
		this.refundStatus = refundStatus;
	}
	public String getRefundProcessDate() {
		return refundProcessDate;
	}
	public void setRefundProcessDate(String refundProcessDate) {
		this.refundProcessDate = refundProcessDate;
	}
	public String getRefundBankTxnId() {
		return refundBankTxnId;
	}
	public void setRefundBankTxnId(String refundBankTxnId) {
		this.refundBankTxnId = refundBankTxnId;
	}
	public String getCancelTxnDate() {
		return cancelTxnDate;
	}
	public void setCancelTxnDate(String cancelTxnDate) {
		this.cancelTxnDate = cancelTxnDate;
	}
	public String getBookingTxnAmt() {
		return bookingTxnAmt;
	}
	public void setBookingTxnAmt(String bookingTxnAmt) {
		this.bookingTxnAmt = bookingTxnAmt;
	}
	public String getCancelTxnId() {
		return cancelTxnId;
	}
	public void setCancelTxnId(String cancelTxnId) {
		this.cancelTxnId = cancelTxnId;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	public String getArn() {
		return arn;
	}
	public void setArn(String arn) {
		this.arn = arn;
	}
	public String getRrn() {
		return rrn;
	}
	public void setRrn(String rrn) {
		this.rrn = rrn;
	}
	
}
