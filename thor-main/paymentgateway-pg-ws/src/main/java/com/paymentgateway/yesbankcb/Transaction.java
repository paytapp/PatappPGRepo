package com.paymentgateway.yesbankcb;

import org.springframework.stereotype.Service;

@Service("yesBankCbTransaction")
public class Transaction {
	
	private String response;
	private String status;
	private String result;
	private String acq_id;
	private String rrn;
	private String auth_code;
	private String responseMessage;
	private String dateTime;
	private String merchantVpa;
	private String transactionMessage;
	private String payeeApprovalNum;
	private String ReferenceId;
	private String customerReference;
	private String transactionId;
	private String merchantVPA;
	private String collectCode;
	private String arn;
	
	public String getCollectCode() {
		return collectCode;
	}

	public void setCollectCode(String collectCode) {
		this.collectCode = collectCode;
	}

	public String getMerchantVPA() {
		return merchantVPA;
	}

	public void setMerchantVPA(String merchantVPA) {
		this.merchantVPA = merchantVPA;
	}

	public String getCustomerReference() {
		return customerReference;
	}

	public void setCustomerReference(String customerReference) {
		this.customerReference = customerReference;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	private String address;
    
	public String getReferenceId() {
		return ReferenceId;
	}

	public void setReferenceId(String referenceId) {
		ReferenceId = referenceId;
	}

	public String getPayeeApprovalNum() {
		return payeeApprovalNum;
	}

	public void setPayeeApprovalNum(String payeeApprovalNum) {
		this.payeeApprovalNum = payeeApprovalNum;
	}

	public String getTransactionMessage() {
		return transactionMessage;
	}

	public void setTransactionMessage(String transactionMessage) {
		this.transactionMessage = transactionMessage;
	}

	public String getAcq_id() {
		return acq_id;
	}

	public void setAcq_id(String acq_id) {
		this.acq_id = acq_id;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

	public String getAuth_code() {
		return auth_code;
	}

	public void setAuth_code(String auth_code) {
		this.auth_code = auth_code;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	public String getMerchantVpa() {
		return merchantVpa;
	}

	public void setMerchantVpa(String merchantVpa) {
		this.merchantVpa = merchantVpa;
	}

	public String getArn() {
		return arn;
	}

	public void setArn(String arn) {
		this.arn = arn;
	}

	
	

}
