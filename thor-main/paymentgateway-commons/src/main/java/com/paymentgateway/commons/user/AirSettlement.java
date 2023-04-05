package com.paymentgateway.commons.user;

import java.io.Serializable;

/**
 * @author Chandan
 *
 */
public class AirSettlement implements Serializable {

	private static final long serialVersionUID = 6205005987490634345L;
	
	private String pgRefNum;
	private String amount;
	private String settlementDate;
	private String orderId;
	private String saleDate;
	
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
	public String getSettlementDate() {
		return settlementDate;
	}
	public void setSettlementDate(String settlementDate) {
		this.settlementDate = settlementDate;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getSaleDate() {
		return saleDate;
	}
	public void setSaleDate(String saleDate) {
		this.saleDate = saleDate;
	}

}
