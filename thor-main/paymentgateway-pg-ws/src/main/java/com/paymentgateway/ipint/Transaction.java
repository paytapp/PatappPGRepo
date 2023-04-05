package com.paymentgateway.ipint;

import org.springframework.stereotype.Service;

@Service("ipintTransaction")
public class Transaction {

	private String sessionId;
	private String invoiceId;
	private String invoiceCreationTime;
	private String transactionStatus;
	private String transactionHash;
	private String transactionOnClick;
	private String transactionTime;
	private String transactionCrypto;
	private String receivedCryptoAmount;
	private String receivedAmountInUsd;
	private String receivedAmountInLocalCurrency;
	private String walletAddress;
	private String blockchainTransactionStatus;
	private String blockchainConfirmations;
	private String companyName;
	private String merchantWebsite;
	private String resultCode;
	private String resultMsg;

	private String invoiceAmountInLocalCurrency;
	private String invoiceCryptoAmount;
	private String invoiceAmountInUsd;
	
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}

	

	public String getInvoiceCreationTime() {
		return invoiceCreationTime;
	}

	public void setInvoiceCreationTime(String invoiceCreationTime) {
		this.invoiceCreationTime = invoiceCreationTime;
	}

	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public String getTransactionHash() {
		return transactionHash;
	}

	public void setTransactionHash(String transactionHash) {
		this.transactionHash = transactionHash;
	}

	public String getTransactionOnClick() {
		return transactionOnClick;
	}

	public void setTransactionOnClick(String transactionOnClick) {
		this.transactionOnClick = transactionOnClick;
	}

	public String getTransactionTime() {
		return transactionTime;
	}

	public void setTransactionTime(String transactionTime) {
		this.transactionTime = transactionTime;
	}

	public String getTransactionCrypto() {
		return transactionCrypto;
	}

	public void setTransactionCrypto(String transactionCrypto) {
		this.transactionCrypto = transactionCrypto;
	}

	public String getInvoiceCryptoAmount() {
		return invoiceCryptoAmount;
	}

	public void setInvoiceCryptoAmount(String invoiceCryptoAmount) {
		this.invoiceCryptoAmount = invoiceCryptoAmount;
	}

	public String getInvoiceAmountInUsd() {
		return invoiceAmountInUsd;
	}

	public void setInvoiceAmountInUsd(String invoiceAmountInUsd) {
		this.invoiceAmountInUsd = invoiceAmountInUsd;
	}

	public String getInvoiceAmountInLocalCurrency() {
		return invoiceAmountInLocalCurrency;
	}

	public void setInvoiceAmountInLocalCurrency(String invoiceAmountInLocalCurrency) {
		this.invoiceAmountInLocalCurrency = invoiceAmountInLocalCurrency;
	}

	public String getReceivedCryptoAmount() {
		return receivedCryptoAmount;
	}

	public void setReceivedCryptoAmount(String receivedCryptoAmount) {
		this.receivedCryptoAmount = receivedCryptoAmount;
	}

	public String getReceivedAmountInUsd() {
		return receivedAmountInUsd;
	}

	public void setReceivedAmountInUsd(String receivedAmountInUsd) {
		this.receivedAmountInUsd = receivedAmountInUsd;
	}

	public String getReceivedAmountInLocalCurrency() {
		return receivedAmountInLocalCurrency;
	}

	public void setReceivedAmountInLocalCurrency(String receivedAmountInLocalCurrency) {
		this.receivedAmountInLocalCurrency = receivedAmountInLocalCurrency;
	}

	public String getWalletAddress() {
		return walletAddress;
	}

	public void setWalletAddress(String walletAddress) {
		this.walletAddress = walletAddress;
	}

	public String getBlockchainTransactionStatus() {
		return blockchainTransactionStatus;
	}

	public void setBlockchainTransactionStatus(String blockchainTransactionStatus) {
		this.blockchainTransactionStatus = blockchainTransactionStatus;
	}

	public String getBlockchainConfirmations() {
		return blockchainConfirmations;
	}

	public void setBlockchainConfirmations(String blockchainConfirmations) {
		this.blockchainConfirmations = blockchainConfirmations;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getMerchantWebsite() {
		return merchantWebsite;
	}

	public void setMerchantWebsite(String merchantWebsite) {
		this.merchantWebsite = merchantWebsite;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

	

}
