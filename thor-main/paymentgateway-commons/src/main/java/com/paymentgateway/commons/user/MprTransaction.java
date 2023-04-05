package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.math.BigDecimal;

public class MprTransaction implements Serializable {
	
	
	private static final long serialVersionUID = -3044624475885118057L;
	
	
	private String acquirer;
	private String merchant;
	private String paymentType;
	private Integer saleTxn;
	private BigDecimal saleMprAmount;
	private Integer refundTxn;
	private BigDecimal refundMprAmount;
	private Integer netTxn;
	private BigDecimal netMprAmount;
	private BigDecimal amountCreditNodal;
	private String amountCreditDate;
	private BigDecimal amountNodalDifference;
	private String nodalAccount;
	
	
	public String getNodalAccount() {
		return nodalAccount;
	}

	public void setNodalAccount(String nodalAccount) {
		this.nodalAccount = nodalAccount;
	}

	public String getAcquirer() {
		return acquirer;
	}
	
	public BigDecimal getRefundMprAmount() {
		return refundMprAmount;
	}

	public void setRefundMprAmount(BigDecimal refundMprAmount) {
		this.refundMprAmount = refundMprAmount;
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

	public BigDecimal getSaleMprAmount() {
		return saleMprAmount;
	}




	public BigDecimal getNetMprAmount() {
		return netMprAmount;
	}

	public void setNetMprAmount(BigDecimal netMprAmount) {
		this.netMprAmount = netMprAmount;
	}

	public Integer getSaleTxn() {
		return saleTxn;
	}

	public void setSaleTxn(Integer saleTxn) {
		this.saleTxn = saleTxn;
	}

	public Integer getRefundTxn() {
		return refundTxn;
	}

	public void setRefundTxn(Integer refundTxn) {
		this.refundTxn = refundTxn;
	}

	public Integer getNetTxn() {
		return netTxn;
	}

	public void setNetTxn(Integer netTxn) {
		this.netTxn = netTxn;
	}

	public void setSaleMprAmount(BigDecimal saleMprAmount) {
		this.saleMprAmount = saleMprAmount;
	}
	

	
	public BigDecimal getAmountCreditNodal() {
		return amountCreditNodal;
	}

	public void setAmountCreditNodal(BigDecimal amountCreditNodal) {
		this.amountCreditNodal = amountCreditNodal;
	}

	public String getAmountCreditDate() {
		return amountCreditDate;
	}
	public void setAmountCreditDate(String amountCreditDate) {
		this.amountCreditDate = amountCreditDate;
	}

	public BigDecimal getAmountNodalDifference() {
		return amountNodalDifference;
	}

	public void setAmountNodalDifference(BigDecimal amountNodalDifference) {
		this.amountNodalDifference = amountNodalDifference;
	}

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}
	

	

}
