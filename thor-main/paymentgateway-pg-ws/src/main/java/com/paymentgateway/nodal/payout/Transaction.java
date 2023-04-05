package com.paymentgateway.nodal.payout;

import org.springframework.stereotype.Service;

@Service("settlementTransaction")
public class Transaction {

	private String uniqueResponseNo;
	private String attemptNo;
	private String statusCode;
	private String subStatusCode;
	private String responeMessage;
	private String bankReferenceNo;
	private String beneficiaryReferenceNo;
	private String transactionDate;
	private String transferAmount;
	private String status;
	private String rrn;

	public String getUniqueResponseNo() {
		return uniqueResponseNo;
	}

	public void setUniqueResponseNo(String uniqueResponseNo) {
		this.uniqueResponseNo = uniqueResponseNo;
	}

	public String getAttemptNo() {
		return attemptNo;
	}

	public void setAttemptNo(String attemptNo) {
		this.attemptNo = attemptNo;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getSubStatusCode() {
		return subStatusCode;
	}

	public void setSubStatusCode(String subStatusCode) {
		this.subStatusCode = subStatusCode;
	}

	public String getResponeMessage() {
		return responeMessage;
	}

	public void setResponeMessage(String responeMessage) {
		this.responeMessage = responeMessage;
	}

	public String getBankReferenceNo() {
		return bankReferenceNo;
	}

	public void setBankReferenceNo(String bankReferenceNo) {
		this.bankReferenceNo = bankReferenceNo;
	}

	public String getBeneficiaryReferenceNo() {
		return beneficiaryReferenceNo;
	}

	public void setBeneficiaryReferenceNo(String beneficiaryReferenceNo) {
		this.beneficiaryReferenceNo = beneficiaryReferenceNo;
	}

	public String getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}

	public String getTransferAmount() {
		return transferAmount;
	}

	public void setTransferAmount(String transferAmount) {
		this.transferAmount = transferAmount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

}
