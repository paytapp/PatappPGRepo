package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Proxy;

@Entity
@Proxy(lazy = false)
@Table
public class Chargeback implements Serializable {

	private static final long serialVersionUID = -6035765912522135772L;

	/*
	 * @Id
	 * 
	 * @GeneratedValue(strategy=GenerationType.AUTO) private Long id;
	 */
	@Id
	@Column(nullable = false, unique = true)
	private String Id;
	private String orderId;
	private String custId;
	private Date createDate;
	private String payId;
	private String transactionId;
	private String cardNumber;
	private String mopType;
	private String paymentType;
	private String status;
	private String custEmail;
	private String internalCustIP;
	private String internalCustCountryName;
	private String internalCardIssusserBank;
	private String internalCardIssusserCountry;
	private String FileName;
	private String currencyCode;
	private String currencyNameCode;
	private String closeDate;
	private BigDecimal amount;

	// Refund service
	private String capturedAmount;
	private BigDecimal authorizedAmount;
	private BigDecimal fixedTxnFee;
	private BigDecimal tdr;
	private BigDecimal serviceTax;
	private BigDecimal chargebackAmount;
	private BigDecimal netAmount;
	private BigDecimal percentecServiceTax;
	private BigDecimal merchantTDR;
	private String merchantStatus;
	private String adminStatus;
	private BigDecimal otherAmount;
	private BigDecimal totalchargebackAmount;
	private String pgRefNum;
	private String caseId;
	@Transient
	private String createDateString;
	@Transient
	private String updateDateString;
	
	@Column(columnDefinition = "TEXT", length = 24,nullable = true)
	private String subMerchantId;
	private Date updateDate;
	private String targetDate;
	private String commentedBy;
	private String chargebackType;
	private String chargebackStatus;
	@Transient
	private String refundAvailable;
	@Transient
	private String refundedAmount;

	@Transient
	private String businessName;
	
	@Transient
	private String superMerchantName;
	@Transient
	private String subMerchantName;
	
	@Column(length = 65535, columnDefinition = "Text")
	private String comments;

	// @Transient
	private String documentId;
	
	@Transient
	private Boolean closeButtonFlag = false;
	
	@Column(columnDefinition = "boolean default false")
    private boolean holdAmountFlag= false;

	@OneToMany(targetEntity = ChargebackComment.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)

	private Set<ChargebackComment> chargebackComment = new HashSet<ChargebackComment>();

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCustEmail() {
		return custEmail;
	}

	public void setCustEmail(String custEmail) {
		this.custEmail = custEmail;
	}

	public String getInternalCustIP() {
		return internalCustIP;
	}

	public void setInternalCustIP(String internalCustIP) {
		this.internalCustIP = internalCustIP;
	}

	public String getInternalCustCountryName() {
		return internalCustCountryName;
	}

	public void setInternalCustCountryName(String internalCustCountryName) {
		this.internalCustCountryName = internalCustCountryName;
	}

	public String getInternalCardIssusserBank() {
		return internalCardIssusserBank;
	}

	public void setInternalCardIssusserBank(String internalCardIssusserBank) {
		this.internalCardIssusserBank = internalCardIssusserBank;
	}

	public String getInternalCardIssusserCountry() {
		return internalCardIssusserCountry;
	}

	public void setInternalCardIssusserCountry(String internalCardIssusserCountry) {
		this.internalCardIssusserCountry = internalCardIssusserCountry;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getCurrencyNameCode() {
		return currencyNameCode;
	}

	public void setCurrencyNameCode(String currencyNameCode) {
		this.currencyNameCode = currencyNameCode;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getAuthorizedAmount() {
		return authorizedAmount;
	}

	public void setAuthorizedAmount(BigDecimal authorizedAmount) {
		this.authorizedAmount = authorizedAmount;
	}

	public BigDecimal getFixedTxnFee() {
		return fixedTxnFee;
	}

	public void setFixedTxnFee(BigDecimal fixedTxnFee) {
		this.fixedTxnFee = fixedTxnFee;
	}

	public BigDecimal getTdr() {
		return tdr;
	}

	public void setTdr(BigDecimal tdr) {
		this.tdr = tdr;
	}

	public BigDecimal getServiceTax() {
		return serviceTax;
	}

	public void setServiceTax(BigDecimal serviceTax) {
		this.serviceTax = serviceTax;
	}

	public BigDecimal getChargebackAmount() {
		return chargebackAmount;
	}

	public void setChargebackAmount(BigDecimal chargebackAmount) {
		this.chargebackAmount = chargebackAmount;
	}

	public BigDecimal getNetAmount() {
		return netAmount;
	}

	public void setNetAmount(BigDecimal netAmount) {
		this.netAmount = netAmount;
	}

	public BigDecimal getPercentecServiceTax() {
		return percentecServiceTax;
	}

	public void setPercentecServiceTax(BigDecimal percentecServiceTax) {
		this.percentecServiceTax = percentecServiceTax;
	}

	public BigDecimal getMerchantTDR() {
		return merchantTDR;
	}

	public void setMerchantTDR(BigDecimal merchantTDR) {
		this.merchantTDR = merchantTDR;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public String getTargetDate() {
		return targetDate;
	}

	public void setTargetDate(String targetDate) {
		this.targetDate = targetDate;
	}

	public String getCommentedBy() {
		return commentedBy;
	}

	public void setCommentedBy(String commentedBy) {
		this.commentedBy = commentedBy;
	}

	public String getChargebackType() {
		return chargebackType;
	}

	public void setChargebackType(String chargebackType) {
		this.chargebackType = chargebackType;
	}

	public String getChargebackStatus() {
		return chargebackStatus;
	}

	public void setChargebackStatus(String chargebackStatus) {
		this.chargebackStatus = chargebackStatus;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getFileName() {
		return FileName;
	}

	public void setFileName(String fileName) {
		FileName = fileName;
	}

	/*
	 * public String getComments() { try{ return new
	 * String(comments.getBytes(1l, (int) comments.length())); } catch(Exception
	 * e){ return "Error"; }
	 * 
	 * } public void setComments(java.sql.Blob blob) { this.comments = blob; }
	 */
	public Set<ChargebackComment> getChargebackComments() {
		return chargebackComment;
	}

	public void setChargebackComments(Set<ChargebackComment> chargebackComment) {
		this.chargebackComment = chargebackComment;
	}

	public void addChargebackComments(ChargebackComment chargebackComment) {
		this.chargebackComment.add(chargebackComment);
	}

	public void removeChargebackComments(ChargebackComment chargebackComment) {
		this.chargebackComment.remove(chargebackComment);
	}

	public String getMerchantStatus() {
		return merchantStatus;
	}

	public String getAdminStatus() {
		return adminStatus;
	}

	public void setAdminStatus(String adminStatus) {
		this.adminStatus = adminStatus;
	}

	public void setMerchantStatus(String merchantStatus) {
		this.merchantStatus = merchantStatus;
	}

	public Set<ChargebackComment> getChargebackComment() {
		return chargebackComment;
	}

	public void setChargebackComment(Set<ChargebackComment> chargebackComment) {
		this.chargebackComment = chargebackComment;
	}

	public BigDecimal getOtherAmount() {
		return otherAmount;
	}

	public void setOtherAmount(BigDecimal otherAmount) {
		this.otherAmount = otherAmount;
	}

	public String getRefundAvailable() {
		return refundAvailable;
	}

	public void setRefundAvailable(String refundAvailable) {
		this.refundAvailable = refundAvailable;
	}

	public String getRefundedAmount() {
		return refundedAmount;
	}

	public void setRefundedAmount(String refundedAmount) {
		this.refundedAmount = refundedAmount;
	}

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public BigDecimal getTotalchargebackAmount() {
		return totalchargebackAmount;
	}

	public void setTotalchargebackAmount(BigDecimal totalchargebackAmount) {
		this.totalchargebackAmount = totalchargebackAmount;
	}
	public String getCapturedAmount() {
		return capturedAmount;
	}

	public void setCapturedAmount(String capturedAmount) {
		this.capturedAmount = capturedAmount;
	}
	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getSuperMerchantName() {
		return superMerchantName;
	}

	public void setSuperMerchantName(String superMerchantName) {
		this.superMerchantName = superMerchantName;
	}

	public String getSubMerchantName() {
		return subMerchantName;
	}

	public void setSubMerchantName(String subMerchantName) {
		this.subMerchantName = subMerchantName;
	}

	public String getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(String closeDate) {
		this.closeDate = closeDate;
	}

	public String getCreateDateString() {
		return createDateString;
	}

	public void setCreateDateString(String createDateString) {
		this.createDateString = createDateString;
	}

	public String getUpdateDateString() {
		return updateDateString;
	}

	public void setUpdateDateString(String updateDateString) {
		this.updateDateString = updateDateString;
	}

	public boolean isHoldAmountFlag() {
		return holdAmountFlag;
	}

	public void setHoldAmountFlag(boolean holdAmountFlag) {
		this.holdAmountFlag = holdAmountFlag;
	}
	
	public Boolean getCloseButtonFlag() {
		return closeButtonFlag;
	}

	public void setCloseButtonFlag(Boolean closeButtonFlag) {
		this.closeButtonFlag = closeButtonFlag;
	}

	public Object[] downloadCsvFields() {
		  Object[] objectArray = new Object[10];
		  
		  
		  objectArray[0] = caseId;
		  objectArray[1] = orderId;
		  objectArray[2] = superMerchantName;
		  objectArray[3] = businessName;
		  objectArray[4] = chargebackType;
		  objectArray[5] = status;
		  objectArray[6] = capturedAmount;
		  objectArray[7] = targetDate;
		  objectArray[8] = createDateString;
		  objectArray[9] = closeDate;
		  
		  return objectArray;
		}
	

}
