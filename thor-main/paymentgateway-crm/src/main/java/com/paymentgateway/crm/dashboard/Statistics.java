package com.paymentgateway.crm.dashboard;

import java.io.Serializable;

public class Statistics implements Serializable {

	/**
	 * @author Chandan
	 */
	private static final long serialVersionUID = -1126096529867414025L;

	private String totalSuccess;
	private String totalFailed;
	private String totalRefunded;
	private String totalRejectedDeclined;
	private String totalDropped;
	private String totalCancelled;
	private String refundedAmount;
	private String approvedAmount;
	private String totalFraud;
	private String totalInvalid;
	private String totalPending;
	// netbanking Summary
	private String totalTransaction;
	private String totalBaunced;
	private String transactionRatio;
	// Credit Summary
	private String totalCreditSuccess;
	private String totalCreditFailed;
	private String totalCreditTransaction;
	private String totalCreditBaunced;
	private String totalCreditCancelled;
	private String totalCreditDropped;
	
	private String totalVisa;
	private String totalSuccessVisa;
	private String totalFailedVisa;
	private String totalDroppedVisa;
	private String totalBauncedVisa;
	private String totalCancelledVisa;
	private String totalMaster;
	private String totalSuccessMaster;
	private String totalFailedMaster;
	private String totalDroppedMaster;
	private String totalBauncedMaster;
	private String totalCancelledMaster;
	private String totalAmex;
	private String totalSuccessAmex;
	private String totalFailedAmex;
	private String totalDroppedAmex;
	private String totalBauncedAmex;
	private String totalCancelledAmex;
	private String totalMestro;
	private String totalSuccessMestro;
	private String totalFailedMestro;
	private String totalDroppedMestro;
	private String totalBauncedMestro;
	private String totalCancelledMestro;
	private String totalDiner;
	private String totalSuccessDiner;
	private String totalFailedDiner;
	private String totalDroppedDiner;
	private String totalBauncedDiner;
	private String totalCancelledDiner;
	// Dedit Summary
	private String totalDebitSuccess;
	private String totalDebitFailed;
	private String totalDebitTransaction;
	private String totalDebitBaunced;
	private String totalDebitCancelled;
	private String totalDebitDropped;
	// NetBanking Summary
	private String totalNetBankTransaction;
	private String totalNetBankSuccess;
	private String totalNetBankFailed;
	private String totalNetBankDropped;
	private String totalNetBankCancelled;
	// wallet Summary
	private String totalWalletTransaction;
	private String totalWalletSuccess;
	private String totalWalletFailed;
	private String totalWalletDropped;
	private String totalWalletCancelled;
	private String totalGrossSuccess;
	private String grossApprovedAmount;
	private String totalAmount;
	private String totalCapturedAmount;

	public String getTotalSuccess() {
		return totalSuccess;
	}

	public void setTotalSuccess(String totalSuccess) {
		this.totalSuccess = totalSuccess;
	}

	public String getTotalFailed() {
		return totalFailed;
	}

	public void setTotalFailed(String totalFailed) {
		this.totalFailed = totalFailed;
	}

	public String getTotalRefunded() {
		return totalRefunded;
	}

	public void setTotalRefunded(String totalRefunded) {
		this.totalRefunded = totalRefunded;
	}

	public String getRefundedAmount() {
		return refundedAmount;
	}

	public void setRefundedAmount(String refundedAmount) {
		this.refundedAmount = refundedAmount;
	}

	public String getApprovedAmount() {
		return approvedAmount;
	}

	public void setApprovedAmount(String approvedAmount) {
		this.approvedAmount = approvedAmount;
	}

	public String getTotalTransaction() {
		return totalTransaction;
	}

	public void setTotalTransaction(String totalTransaction) {
		this.totalTransaction = totalTransaction;
	}

	public String getTotalBaunced() {
		return totalBaunced;
	}

	public void setTotalBaunced(String totalBaunced) {
		this.totalBaunced = totalBaunced;
	}

	public String getTotalCancelled() {
		return totalCancelled;
	}

	public void setTotalCancelled(String totalCancelled) {
		this.totalCancelled = totalCancelled;
	}

	public String getTotalDropped() {
		return totalDropped;
	}

	public void setTotalDropped(String totalDropped) {
		this.totalDropped = totalDropped;
	}

	public String getTotalCreditSuccess() {
		return totalCreditSuccess;
	}

	public void setTotalCreditSuccess(String totalCreditSuccess) {
		this.totalCreditSuccess = totalCreditSuccess;
	}

	public String getTotalCreditFailed() {
		return totalCreditFailed;
	}

	public void setTotalCreditFailed(String totalCreditFailed) {
		this.totalCreditFailed = totalCreditFailed;
	}

	public String getTotalCreditTransaction() {
		return totalCreditTransaction;
	}

	public void setTotalCreditTransaction(String totalCreditTransaction) {
		this.totalCreditTransaction = totalCreditTransaction;
	}

	public String getTotalCreditBaunced() {
		return totalCreditBaunced;
	}

	public void setTotalCreditBaunced(String totalCreditBaunced) {
		this.totalCreditBaunced = totalCreditBaunced;
	}

	public String getTotalCreditCancelled() {
		return totalCreditCancelled;
	}

	public void setTotalCreditCancelled(String totalCreditCancelled) {
		this.totalCreditCancelled = totalCreditCancelled;
	}

	public String getTotalCreditDropped() {
		return totalCreditDropped;
	}

	public void setTotalCreditDropped(String totalCreditDropped) {
		this.totalCreditDropped = totalCreditDropped;
	}

	public String getTotalDebitSuccess() {
		return totalDebitSuccess;
	}

	public void setTotalDebitSuccess(String totalDebitSuccess) {
		this.totalDebitSuccess = totalDebitSuccess;
	}

	public String getTotalDebitFailed() {
		return totalDebitFailed;
	}

	public void setTotalDebitFailed(String totalDebitFailed) {
		this.totalDebitFailed = totalDebitFailed;
	}

	public String getTotalDebitTransaction() {
		return totalDebitTransaction;
	}

	public void setTotalDebitTransaction(String totalDebitTransaction) {
		this.totalDebitTransaction = totalDebitTransaction;
	}

	public String getTotalDebitBaunced() {
		return totalDebitBaunced;
	}

	public void setTotalDebitBaunced(String totalDebitBaunced) {
		this.totalDebitBaunced = totalDebitBaunced;
	}

	public String getTotalDebitCancelled() {
		return totalDebitCancelled;
	}

	public void setTotalDebitCancelled(String totalDebitCancelled) {
		this.totalDebitCancelled = totalDebitCancelled;
	}

	public String getTotalDebitDropped() {
		return totalDebitDropped;
	}

	public void setTotalDebitDropped(String totalDebitDropped) {
		this.totalDebitDropped = totalDebitDropped;
	}

	public String getTransactionRatio() {
		return transactionRatio;
	}

	public void setTransactionRatio(String transactionRatio) {
		this.transactionRatio = transactionRatio;
	}

	public String getTotalVisa() {
		return totalVisa;
	}

	public void setTotalVisa(String totalVisa) {
		this.totalVisa = totalVisa;
	}

	public String getTotalSuccessVisa() {
		return totalSuccessVisa;
	}

	public void setTotalSuccessVisa(String totalSuccessVisa) {
		this.totalSuccessVisa = totalSuccessVisa;
	}

	public String getTotalFailedVisa() {
		return totalFailedVisa;
	}

	public void setTotalFailedVisa(String totalFailedVisa) {
		this.totalFailedVisa = totalFailedVisa;
	}

	public String getTotalDroppedVisa() {
		return totalDroppedVisa;
	}

	public void setTotalDroppedVisa(String totalDroppedVisa) {
		this.totalDroppedVisa = totalDroppedVisa;
	}

	public String getTotalBauncedVisa() {
		return totalBauncedVisa;
	}

	public void setTotalBauncedVisa(String totalBauncedVisa) {
		this.totalBauncedVisa = totalBauncedVisa;
	}

	public String getTotalCancelledVisa() {
		return totalCancelledVisa;
	}

	public void setTotalCancelledVisa(String totalCancelledVisa) {
		this.totalCancelledVisa = totalCancelledVisa;
	}

	public String getTotalMaster() {
		return totalMaster;
	}

	public void setTotalMaster(String totalMaster) {
		this.totalMaster = totalMaster;
	}

	public String getTotalSuccessMaster() {
		return totalSuccessMaster;
	}

	public void setTotalSuccessMaster(String totalSuccessMaster) {
		this.totalSuccessMaster = totalSuccessMaster;
	}

	public String getTotalFailedMaster() {
		return totalFailedMaster;
	}

	public void setTotalFailedMaster(String totalFailedMaster) {
		this.totalFailedMaster = totalFailedMaster;
	}

	public String getTotalDroppedMaster() {
		return totalDroppedMaster;
	}

	public void setTotalDroppedMaster(String totalDroppedMaster) {
		this.totalDroppedMaster = totalDroppedMaster;
	}

	public String getTotalBauncedMaster() {
		return totalBauncedMaster;
	}

	public void setTotalBauncedMaster(String totalBauncedMaster) {
		this.totalBauncedMaster = totalBauncedMaster;
	}

	public String getTotalCancelledMaster() {
		return totalCancelledMaster;
	}

	public void setTotalCancelledMaster(String totalCancelledMaster) {
		this.totalCancelledMaster = totalCancelledMaster;
	}

	public String getTotalAmex() {
		return totalAmex;
	}

	public void setTotalAmex(String totalAmex) {
		this.totalAmex = totalAmex;
	}

	public String getTotalSuccessAmex() {
		return totalSuccessAmex;
	}

	public void setTotalSuccessAmex(String totalSuccessAmex) {
		this.totalSuccessAmex = totalSuccessAmex;
	}

	public String getTotalFailedAmex() {
		return totalFailedAmex;
	}

	public void setTotalFailedAmex(String totalFailedAmex) {
		this.totalFailedAmex = totalFailedAmex;
	}

	public String getTotalDroppedAmex() {
		return totalDroppedAmex;
	}

	public void setTotalDroppedAmex(String totalDroppedAmex) {
		this.totalDroppedAmex = totalDroppedAmex;
	}

	public String getTotalBauncedAmex() {
		return totalBauncedAmex;
	}

	public void setTotalBauncedAmex(String totalBauncedAmex) {
		this.totalBauncedAmex = totalBauncedAmex;
	}

	public String getTotalCancelledAmex() {
		return totalCancelledAmex;
	}

	public void setTotalCancelledAmex(String totalCancelledAmex) {
		this.totalCancelledAmex = totalCancelledAmex;
	}

	public String getTotalMestro() {
		return totalMestro;
	}

	public void setTotalMestro(String totalMestro) {
		this.totalMestro = totalMestro;
	}

	public String getTotalSuccessMestro() {
		return totalSuccessMestro;
	}

	public void setTotalSuccessMestro(String totalSuccessMestro) {
		this.totalSuccessMestro = totalSuccessMestro;
	}

	public String getTotalFailedMestro() {
		return totalFailedMestro;
	}

	public void setTotalFailedMestro(String totalFailedMestro) {
		this.totalFailedMestro = totalFailedMestro;
	}

	public String getTotalDroppedMestro() {
		return totalDroppedMestro;
	}

	public void setTotalDroppedMestro(String totalDroppedMestro) {
		this.totalDroppedMestro = totalDroppedMestro;
	}

	public String getTotalBauncedMestro() {
		return totalBauncedMestro;
	}

	public void setTotalBauncedMestro(String totalBauncedMestro) {
		this.totalBauncedMestro = totalBauncedMestro;
	}

	public String getTotalCancelledMestro() {
		return totalCancelledMestro;
	}

	public void setTotalCancelledMestro(String totalCancelledMestro) {
		this.totalCancelledMestro = totalCancelledMestro;
	}

	public String getTotalDiner() {
		return totalDiner;
	}

	public void setTotalDiner(String totalDiner) {
		this.totalDiner = totalDiner;
	}

	public String getTotalSuccessDiner() {
		return totalSuccessDiner;
	}

	public void setTotalSuccessDiner(String totalSuccessDiner) {
		this.totalSuccessDiner = totalSuccessDiner;
	}

	public String getTotalFailedDiner() {
		return totalFailedDiner;
	}

	public void setTotalFailedDiner(String totalFailedDiner) {
		this.totalFailedDiner = totalFailedDiner;
	}

	public String getTotalDroppedDiner() {
		return totalDroppedDiner;
	}

	public void setTotalDroppedDiner(String totalDroppedDiner) {
		this.totalDroppedDiner = totalDroppedDiner;
	}

	public String getTotalBauncedDiner() {
		return totalBauncedDiner;
	}

	public void setTotalBauncedDiner(String totalBauncedDiner) {
		this.totalBauncedDiner = totalBauncedDiner;
	}

	public String getTotalCancelledDiner() {
		return totalCancelledDiner;
	}

	public void setTotalCancelledDiner(String totalCancelledDiner) {
		this.totalCancelledDiner = totalCancelledDiner;
	}

	public String getTotalNetBankTransaction() {
		return totalNetBankTransaction;
	}

	public void setTotalNetBankTransaction(String totalNetBankTransaction) {
		this.totalNetBankTransaction = totalNetBankTransaction;
	}

	public String getTotalNetBankSuccess() {
		return totalNetBankSuccess;
	}

	public void setTotalNetBankSuccess(String totalNetBankSuccess) {
		this.totalNetBankSuccess = totalNetBankSuccess;
	}

	public String getTotalNetBankFailed() {
		return totalNetBankFailed;
	}

	public void setTotalNetBankFailed(String totalNetBankFailed) {
		this.totalNetBankFailed = totalNetBankFailed;
	}

	public String getTotalNetBankDropped() {
		return totalNetBankDropped;
	}

	public void setTotalNetBankDropped(String totalNetBankDropped) {
		this.totalNetBankDropped = totalNetBankDropped;
	}

	public String getTotalNetBankCancelled() {
		return totalNetBankCancelled;
	}

	public void setTotalNetBankCancelled(String totalNetBankCancelled) {
		this.totalNetBankCancelled = totalNetBankCancelled;
	}

	public String getTotalWalletTransaction() {
		return totalWalletTransaction;
	}

	public void setTotalWalletTransaction(String totalWalletTransaction) {
		this.totalWalletTransaction = totalWalletTransaction;
	}

	public String getTotalWalletSuccess() {
		return totalWalletSuccess;
	}

	public void setTotalWalletSuccess(String totalWalletSuccess) {
		this.totalWalletSuccess = totalWalletSuccess;
	}

	public String getTotalWalletFailed() {
		return totalWalletFailed;
	}

	public void setTotalWalletFailed(String totalWalletFailed) {
		this.totalWalletFailed = totalWalletFailed;
	}

	public String getTotalWalletDropped() {
		return totalWalletDropped;
	}

	public void setTotalWalletDropped(String totalWalletDropped) {
		this.totalWalletDropped = totalWalletDropped;
	}

	public String getTotalWalletCancelled() {
		return totalWalletCancelled;
	}

	public void setTotalWalletCancelled(String totalWalletCancelled) {
		this.totalWalletCancelled = totalWalletCancelled;
	}

	public String getTotalRejectedDeclined() {
		return totalRejectedDeclined;
	}

	public void setTotalRejectedDeclined(String totalRejectedDeclined) {
		this.totalRejectedDeclined = totalRejectedDeclined;
	}

	public String getTotalFraud() {
		return totalFraud;
	}

	public void setTotalFraud(String totalFraud) {
		this.totalFraud = totalFraud;
	}

	public String getTotalInvalid() {
		return totalInvalid;
	}

	public void setTotalInvalid(String totalInvalid) {
		this.totalInvalid = totalInvalid;
	}

	public String getTotalGrossSuccess() {
		return totalGrossSuccess;
	}

	public void setTotalGrossSuccess(String totalGrossSuccess) {
		this.totalGrossSuccess = totalGrossSuccess;
	}

	public String getGrossApprovedAmount() {
		return grossApprovedAmount;
	}

	public void setGrossApprovedAmount(String grossApprovedAmount) {
		this.grossApprovedAmount = grossApprovedAmount;
	}

	public String getTotalPending() {
		return totalPending;
	}

	public void setTotalPending(String totalPending) {
		this.totalPending = totalPending;
	}

	public String getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getTotalCapturedAmount() {
		return totalCapturedAmount;
	}

	public void setTotalCapturedAmount(String totalCapturedAmount) {
		this.totalCapturedAmount = totalCapturedAmount;
	}

}
