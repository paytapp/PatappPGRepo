package com.paymentgateway.commons.user;

import java.io.Serializable;

public class RefundPreview implements Serializable {
	
	private static final long serialVersionUID = -4161683099209249497L;
	
	private String pgRefNo;
	private String refundFlag;
	private String amount;
	private String orderId;
	private String payId;
	private String saleDate;
	private String settledDate;
	private String oid;
	
	public String getPgRefNo() {
		return pgRefNo;
	}
	public void setPgRefNo(String pgRefNo) {
		this.pgRefNo = pgRefNo;
	}
	public String getRefundFlag() {
		return refundFlag;
	}
	public void setRefundFlag(String refundFlag) {
		this.refundFlag = refundFlag;
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
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getSaleDate() {
		return saleDate;
	}
	public void setSaleDate(String saleDate) {
		this.saleDate = saleDate;
	}
	public String getSettledDate() {
		return settledDate;
	}
	public void setSettledDate(String settledDate) {
		this.settledDate = settledDate;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
}
