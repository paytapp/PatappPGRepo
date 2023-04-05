package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;

import com.paymentgateway.commons.util.AccountStatus;

@Entity
@Proxy(lazy= false)
@Table
public class BeneficiaryAccounts implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8445502079628239540L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	private AccountStatus status;
	private Date createdDate;
	private Date updatedDate;
	private String requestedBy;
	private String processedBy;
		
	private String custId;
	private String beneficiaryCd;
	private String srcAccountNo;
	private String paymentType;
	private String beneName;
	private String beneType;
	private Date   beneExpiryDate;
	private String currencyCd;
	private String transactionLimit;
	private String bankName;
	private String ifscCode;
	private String beneAccountNo;
	private String upiHandle;
	private String mobileNo;
	private String emailId;
	private String address1;
	private String address2;
	private String swiftCode;
	private String actions;
	private String acquirer;
	private String rrn;   
	private String responseMessage;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public String getRequestedBy() {
		return requestedBy;
	}
	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}
	public String getProcessedBy() {
		return processedBy;
	}
	public void setProcessedBy(String processedBy) {
		this.processedBy = processedBy;
	}
	public AccountStatus getStatus() {
		return status;
	}
	public void setStatus(AccountStatus status) {
		this.status = status;
	}
	public String getCustId() {
		return custId;
	}
	public void setCustId(String custId) {
		this.custId = custId;
	}
	public String getBeneficiaryCd() {
		return beneficiaryCd;
	}
	public void setBeneficiaryCd(String beneficiaryCd) {
		this.beneficiaryCd = beneficiaryCd;
	}
	public String getSrcAccountNo() {
		return srcAccountNo;
	}
	public void setSrcAccountNo(String srcAccountNo) {
		this.srcAccountNo = srcAccountNo;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getBeneName() {
		return beneName;
	}
	public void setBeneName(String beneName) {
		this.beneName = beneName;
	}
	public String getBeneType() {
		return beneType;
	}
	public void setBeneType(String beneType) {
		this.beneType = beneType;
	}
	public Date getBeneExpiryDate() {
		return beneExpiryDate;
	}
	public void setBeneExpiryDate(Date beneExpiryDate) {
		this.beneExpiryDate = beneExpiryDate;
	}
	public String getCurrencyCd() {
		return currencyCd;
	}
	public void setCurrencyCd(String currencyCd) {
		this.currencyCd = currencyCd;
	}
	public String getTransactionLimit() {
		return transactionLimit;
	}
	public void setTransactionLimit(String transactionLimit) {
		this.transactionLimit = transactionLimit;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getIfscCode() {
		return ifscCode;
	}
	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}
	public String getBeneAccountNo() {
		return beneAccountNo;
	}
	public void setBeneAccountNo(String beneAccountNo) {
		this.beneAccountNo = beneAccountNo;
	}
	public String getUpiHandle() {
		return upiHandle;
	}
	public void setUpiHandle(String upiHandle) {
		this.upiHandle = upiHandle;
	}
	public String getMobileNo() {
		return mobileNo;
	}
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public String getAddress1() {
		return address1;
	}
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	public String getAddress2() {
		return address2;
	}
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
	public String getSwiftCode() {
		return swiftCode;
	}
	public void setSwiftCode(String swiftCode) {
		this.swiftCode = swiftCode;
	}
	public String getAcquirer() {
		return acquirer;
	}
	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}
	public String getActions() {
		return actions;
	}
	public void setActions(String actions) {
		this.actions = actions;
	}
	public String getRrn() {
		return rrn;
	}
	public void setRrn(String rrn) {
		this.rrn = rrn;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	
}
