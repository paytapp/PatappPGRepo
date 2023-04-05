package com.paymentgateway.crm.dashboard;

import java.io.Serializable;

public class PieChart implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -957731054268666487L;
	private String visa;
	private String mastercard;
	private String rupay;
	private String diner;
	private String jcb;
	private String amex;
	private String net;
	private String maestro;
	private String ezeeClick;
	private String totalSuccess;
	private String totalFailed;
	private String totalPending;
	private String totalCredit;
	private String totalDebit;
	private String txndate;
	private String totalRefunded;
	private String totalNewOder;
	private String totalEnrolled;
	private String inr;
	private String usd;
	private String aed;
	private String gbp;
	private String eur;
	private String aud;
	private String totalNetBankingTransaction;
	private String totalCreditCardsTransaction;
	private String totalDebitCardsTransaction;
	private String totalUpiTransaction;
	private String totalWalletTransaction;
	private String totalEmiTransaction;
	private String totalCodTransaction;
	private String totalInternationalTransaction;
	private String totalNetBankingTransactionPercentage;
	private String totalCreditCardsTransactionPercentage;
	private String totalDebitCardsTransactionPercentage;
	private String totalUpiTransactionPercentage;
	private String totalWalletTransactionPercentage;
	private String totalEmiTransactionPercentage;
	private String totalCodTransactionPercentage;
	private String totalInternationalTransactionPercentage;
	private String totalNetBankingTxnAmount;
	private String totalCreditCardsTxnAmount;
	private String totalDebitCardsTxnAmount;
	private String totalUpiTxnAmount;
	private String totalWalletTxnAmount;
	private String totalEmiTxnAmount;
	private String totalCodTxnAmount;
	private String totalInternationalTxnAmount;
	private String totalNetBankingTxnAmountPercentage;
	private String totalCreditCardsTxnAmountPercentage;
	private String totalDebitCardsTxnAmountPercentage;
	private String totalUpiTxnAmountPercentage;
	private String totalWalletTxnAmountPercentage;
	private String totalEmiTxnAmountPercentage;
	private String totalCodTxnAmountPercentage;
	private String totalInternationalTxnAmountPercentage;
	
	private String cc;
	private String dc;
	private String nb;
	private String upi;
	private String wl;
	private String em;
	private String cd;
	private String in;
	private String other;
	private String createDate;
	
	private String totalAmountSuccess;
	private String totalAmountFailed;
	
	public String getTotalAmountSuccess() {
		return totalAmountSuccess;
	}
	public String getTotalAmountFailed() {
		return totalAmountFailed;
	}
	public void setTotalAmountSuccess(String totalAmountSuccess) {
		this.totalAmountSuccess = totalAmountSuccess;
	}
	public void setTotalAmountFailed(String totalAmountFailed) {
		this.totalAmountFailed = totalAmountFailed;
	}
	public String getVisa() {
		return visa;
	}
	public void setVisa(String visa) {
		this.visa = visa;
	}
	public String getMastercard() {
		return mastercard;
	}
	public void setMastercard(String mastercard) {
		this.mastercard = mastercard;
	}
	public String getRupay() {
		return rupay;
	}
	public void setRupay(String rupay) {
		this.rupay = rupay;
	}
	public String getDiner() {
		return diner;
	}
	public void setDiner(String diner) {
		this.diner = diner;
	}
	public String getJcb() {
		return jcb;
	}
	public void setJcb(String jcb) {
		this.jcb = jcb;
	}
	public String getAmex() {
		return amex;
	}
	public void setAmex(String amex) {
		this.amex = amex;
	}
	public String getNet() {
		return net;
	}
	public void setNet(String net) {
		this.net = net;
	}
	public String getMaestro() {
		return maestro;
	}
	public void setMaestro(String maestro) {
		this.maestro = maestro;
	}
	public String getEzeeClick() {
		return ezeeClick;
	}
	public void setEzeeClick(String ezeeClick) {
		this.ezeeClick = ezeeClick;
	}
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
	public String getTotalPending() {
		return totalPending;
	}
	public void setTotalPending(String totalPending) {
		this.totalPending = totalPending;
	}
	public String getTotalCredit() {
		return totalCredit;
	}
	public void setTotalCredit(String totalCredit) {
		this.totalCredit = totalCredit;
	}
	public String getTotalDebit() {
		return totalDebit;
	}
	public void setTotalDebit(String totalDebit) {
		this.totalDebit = totalDebit;
	}
	public String getTxndate() {
		return txndate;
	}
	public void setTxndate(String txndate) {
		this.txndate = txndate;
	}
	public String getTotalRefunded() {
		return totalRefunded;
	}
	public void setTotalRefunded(String totalRefunded) {
		this.totalRefunded = totalRefunded;
	}
	public String getTotalNewOder() {
		return totalNewOder;
	}
	public void setTotalNewOder(String totalNewOder) {
		this.totalNewOder = totalNewOder;
	}
	public String getTotalEnrolled() {
		return totalEnrolled;
	}
	public void setTotalEnrolled(String totalEnrolled) {
		this.totalEnrolled = totalEnrolled;
	}
	public String getInr() {
		return inr;
	}
	public void setInr(String inr) {
		this.inr = inr;
	}
	public String getUsd() {
		return usd;
	}
	public void setUsd(String usd) {
		this.usd = usd;
	}
	public String getAed() {
		return aed;
	}
	public void setAed(String aed) {
		this.aed = aed;
	}
	public String getGbp() {
		return gbp;
	}
	public void setGbp(String gbp) {
		this.gbp = gbp;
	}
	public String getEur() {
		return eur;
	}
	public void setEur(String eur) {
		this.eur = eur;
	}
	public String getAud() {
		return aud;
	}
	public void setAud(String aud) {
		this.aud = aud;
	}
	public String getTotalNetBankingTransaction() {
		return totalNetBankingTransaction;
	}
	public void setTotalNetBankingTransaction(String totalNetBankingTransaction) {
		this.totalNetBankingTransaction = totalNetBankingTransaction;
	}
	public String getTotalCreditCardsTransaction() {
		return totalCreditCardsTransaction;
	}
	public void setTotalCreditCardsTransaction(String totalCreditCardsTransaction) {
		this.totalCreditCardsTransaction = totalCreditCardsTransaction;
	}
	public String getTotalDebitCardsTransaction() {
		return totalDebitCardsTransaction;
	}
	public void setTotalDebitCardsTransaction(String totalDebitCardsTransaction) {
		this.totalDebitCardsTransaction = totalDebitCardsTransaction;
	}
	public String getTotalWalletTransaction() {
		return totalWalletTransaction;
	}
	public void setTotalWalletTransaction(String totalWalletTransaction) {
		this.totalWalletTransaction = totalWalletTransaction;
	}
	public String getTotalEmiTransaction() {
		return totalEmiTransaction;
	}
	public void setTotalEmiTransaction(String totalEmiTransaction) {
		this.totalEmiTransaction = totalEmiTransaction;
	}
	public String getTotalCodTransaction() {
		return totalCodTransaction;
	}
	public void setTotalCodTransaction(String totalCodTransaction) {
		this.totalCodTransaction = totalCodTransaction;
	}
	public String getCc() {
		return cc;
	}
	public void setCc(String cc) {
		this.cc = cc;
	}
	public String getDc() {
		return dc;
	}
	public void setDc(String dc) {
		this.dc = dc;
	}
	public String getNb() {
		return nb;
	}
	public void setNb(String nb) {
		this.nb = nb;
	}
	public String getWl() {
		return wl;
	}
	public void setWl(String wl) {
		this.wl = wl;
	}
	public String getEm() {
		return em;
	}
	public void setEm(String em) {
		this.em = em;
	}
	public String getCd() {
		return cd;
	}
	public void setCd(String cd) {
		this.cd = cd;
	}
	public String getOther() {
		return other;
	}
	public void setOther(String other) {
		this.other = other;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getTotalInternationalTransaction() {
		return totalInternationalTransaction;
	}
	public void setTotalInternationalTransaction(String totalInternationalTransaction) {
		this.totalInternationalTransaction = totalInternationalTransaction;
	}
	public String getIn() {
		return in;
	}
	public void setIn(String in) {
		this.in = in;
	}
	public String getUpi() {
		return upi;
	}
	public void setUpi(String upi) {
		this.upi = upi;
	}
	public String getTotalUpiTransaction() {
		return totalUpiTransaction;
	}
	public void setTotalUpiTransaction(String totalUpiTransaction) {
		this.totalUpiTransaction = totalUpiTransaction;
	}
	public String getTotalNetBankingTxnAmount() {
		return totalNetBankingTxnAmount;
	}
	public void setTotalNetBankingTxnAmount(String totalNetBankingTxnAmount) {
		this.totalNetBankingTxnAmount = totalNetBankingTxnAmount;
	}
	public String getTotalCreditCardsTxnAmount() {
		return totalCreditCardsTxnAmount;
	}
	public void setTotalCreditCardsTxnAmount(String totalCreditCardsTxnAmount) {
		this.totalCreditCardsTxnAmount = totalCreditCardsTxnAmount;
	}
	public String getTotalDebitCardsTxnAmount() {
		return totalDebitCardsTxnAmount;
	}
	public void setTotalDebitCardsTxnAmount(String totalDebitCardsTxnAmount) {
		this.totalDebitCardsTxnAmount = totalDebitCardsTxnAmount;
	}
	public String getTotalUpiTxnAmount() {
		return totalUpiTxnAmount;
	}
	public void setTotalUpiTxnAmount(String totalUpiTxnAmount) {
		this.totalUpiTxnAmount = totalUpiTxnAmount;
	}
	public String getTotalWalletTxnAmount() {
		return totalWalletTxnAmount;
	}
	public void setTotalWalletTxnAmount(String totalWalletTxnAmount) {
		this.totalWalletTxnAmount = totalWalletTxnAmount;
	}
	public String getTotalEmiTxnAmount() {
		return totalEmiTxnAmount;
	}
	public void setTotalEmiTxnAmount(String totalEmiTxnAmount) {
		this.totalEmiTxnAmount = totalEmiTxnAmount;
	}
	public String getTotalCodTxnAmount() {
		return totalCodTxnAmount;
	}
	public void setTotalCodTxnAmount(String totalCodTxnAmount) {
		this.totalCodTxnAmount = totalCodTxnAmount;
	}
	public String getTotalInternationalTxnAmount() {
		return totalInternationalTxnAmount;
	}
	public void setTotalInternationalTxnAmount(String totalInternationalTxnAmount) {
		this.totalInternationalTxnAmount = totalInternationalTxnAmount;
	}
	public String getTotalNetBankingTransactionPercentage() {
		return totalNetBankingTransactionPercentage;
	}
	public void setTotalNetBankingTransactionPercentage(String totalNetBankingTransactionPercentage) {
		this.totalNetBankingTransactionPercentage = totalNetBankingTransactionPercentage;
	}
	public String getTotalCreditCardsTransactionPercentage() {
		return totalCreditCardsTransactionPercentage;
	}
	public void setTotalCreditCardsTransactionPercentage(String totalCreditCardsTransactionPercentage) {
		this.totalCreditCardsTransactionPercentage = totalCreditCardsTransactionPercentage;
	}
	public String getTotalDebitCardsTransactionPercentage() {
		return totalDebitCardsTransactionPercentage;
	}
	public void setTotalDebitCardsTransactionPercentage(String totalDebitCardsTransactionPercentage) {
		this.totalDebitCardsTransactionPercentage = totalDebitCardsTransactionPercentage;
	}
	public String getTotalUpiTransactionPercentage() {
		return totalUpiTransactionPercentage;
	}
	public void setTotalUpiTransactionPercentage(String totalUpiTransactionPercentage) {
		this.totalUpiTransactionPercentage = totalUpiTransactionPercentage;
	}
	public String getTotalWalletTransactionPercentage() {
		return totalWalletTransactionPercentage;
	}
	public void setTotalWalletTransactionPercentage(String totalWalletTransactionPercentage) {
		this.totalWalletTransactionPercentage = totalWalletTransactionPercentage;
	}
	public String getTotalEmiTransactionPercentage() {
		return totalEmiTransactionPercentage;
	}
	public void setTotalEmiTransactionPercentage(String totalEmiTransactionPercentage) {
		this.totalEmiTransactionPercentage = totalEmiTransactionPercentage;
	}
	public String getTotalCodTransactionPercentage() {
		return totalCodTransactionPercentage;
	}
	public void setTotalCodTransactionPercentage(String totalCodTransactionPercentage) {
		this.totalCodTransactionPercentage = totalCodTransactionPercentage;
	}
	public String getTotalInternationalTransactionPercentage() {
		return totalInternationalTransactionPercentage;
	}
	public void setTotalInternationalTransactionPercentage(String totalInternationalTransactionPercentage) {
		this.totalInternationalTransactionPercentage = totalInternationalTransactionPercentage;
	}
	public String getTotalNetBankingTxnAmountPercentage() {
		return totalNetBankingTxnAmountPercentage;
	}
	public void setTotalNetBankingTxnAmountPercentage(String totalNetBankingTxnAmountPercentage) {
		this.totalNetBankingTxnAmountPercentage = totalNetBankingTxnAmountPercentage;
	}
	public String getTotalCreditCardsTxnAmountPercentage() {
		return totalCreditCardsTxnAmountPercentage;
	}
	public void setTotalCreditCardsTxnAmountPercentage(String totalCreditCardsTxnAmountPercentage) {
		this.totalCreditCardsTxnAmountPercentage = totalCreditCardsTxnAmountPercentage;
	}
	public String getTotalDebitCardsTxnAmountPercentage() {
		return totalDebitCardsTxnAmountPercentage;
	}
	public void setTotalDebitCardsTxnAmountPercentage(String totalDebitCardsTxnAmountPercentage) {
		this.totalDebitCardsTxnAmountPercentage = totalDebitCardsTxnAmountPercentage;
	}
	public String getTotalUpiTxnAmountPercentage() {
		return totalUpiTxnAmountPercentage;
	}
	public void setTotalUpiTxnAmountPercentage(String totalUpiTxnAmountPercentage) {
		this.totalUpiTxnAmountPercentage = totalUpiTxnAmountPercentage;
	}
	public String getTotalWalletTxnAmountPercentage() {
		return totalWalletTxnAmountPercentage;
	}
	public void setTotalWalletTxnAmountPercentage(String totalWalletTxnAmountPercentage) {
		this.totalWalletTxnAmountPercentage = totalWalletTxnAmountPercentage;
	}
	public String getTotalEmiTxnAmountPercentage() {
		return totalEmiTxnAmountPercentage;
	}
	public void setTotalEmiTxnAmountPercentage(String totalEmiTxnAmountPercentage) {
		this.totalEmiTxnAmountPercentage = totalEmiTxnAmountPercentage;
	}
	public String getTotalCodTxnAmountPercentage() {
		return totalCodTxnAmountPercentage;
	}
	public void setTotalCodTxnAmountPercentage(String totalCodTxnAmountPercentage) {
		this.totalCodTxnAmountPercentage = totalCodTxnAmountPercentage;
	}
	public String getTotalInternationalTxnAmountPercentage() {
		return totalInternationalTxnAmountPercentage;
	}
	public void setTotalInternationalTxnAmountPercentage(String totalInternationalTxnAmountPercentage) {
		this.totalInternationalTxnAmountPercentage = totalInternationalTxnAmountPercentage;
	}
	
	
	
}
