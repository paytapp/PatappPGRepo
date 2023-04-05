package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.math.BigInteger;

public class DeltaRefund implements Serializable {

	private static final long serialVersionUID = -4691009307354580956L;
	

	private String orderId;
	private String txnDate;
	private String pgRefNum;
	private String amount;
	private String status;
	private String refundPgRefNum;
	private String refundTxnDate;
	private String oid;
	private String arn;
	private String rrn;
		
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	public String getTxnDate() {
		return txnDate;
	}
	
	public void setTxnDate(String txnDate) {
		this.txnDate = txnDate;
	}
	
	public String getPgRefNum() {
		return pgRefNum;
	}
	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRefundPgRefNum() {
		return refundPgRefNum;
	}
	public void setRefundPgRefNum(String refundPgRefNum) {
		this.refundPgRefNum = refundPgRefNum;
	}
	public String getRefundTxnDate() {
		return refundTxnDate;
	}
	public void setRefundTxnDate(String refundTxnDate) {
		this.refundTxnDate = refundTxnDate;
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
