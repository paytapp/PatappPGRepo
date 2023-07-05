package com.paymentgateway.phonePe;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "transaction_details")
public class TransactionDetailsEntity {
	
    private int id;
    private String purchaseId;
    private String userId;
    private BigDecimal transactionAmount;
    private Boolean paymentRequestSuccess;
    private String merchantId;
    private String paymentType;
    private String paymentRequestJson;
    private String paymentResponseJson;
    private Date createdDate;
    private String createdBy;
    private Date updatedDate;
    private String updatedBy;  
    
    public TransactionDetailsEntity(int id, String purchaseId, String userId, BigDecimal transactionAmount,
			Boolean paymentRequestSuccess, String merchantId, String paymentType, String paymentRequestJson,
			String paymentResponseJson, Date createdDate, String createdBy, Date updatedDate, String updatedBy) {
		super();
		this.id = id;
		this.purchaseId = purchaseId;
		this.userId = userId;
		this.transactionAmount = transactionAmount;
		this.paymentRequestSuccess = paymentRequestSuccess;
		this.merchantId = merchantId;
		this.paymentType = paymentType;
		this.paymentRequestJson = paymentRequestJson;
		this.paymentResponseJson = paymentResponseJson;
		this.createdDate = createdDate;
		this.createdBy = createdBy;
		this.updatedDate = updatedDate;
		this.updatedBy = updatedBy;
	}

	public TransactionDetailsEntity() {
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public int getId() {
        return id;
    }
  
    public void setId(int id) {
        this.id = id;
    }
    
    @Column(name = "purchaseId", nullable = false)
    public String getPurchaseId() {
        return purchaseId;
    }
    
    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
    }
    
    @Column(name = "userId", nullable = false)
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    @Column(name = "transactionAmount", nullable = false)
    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }
    
    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }
    
    @Column(name = "paymentRequestSuccess", nullable = false)
    public boolean isPaymentRequestSuccess() {
        return paymentRequestSuccess;
    }
    
    public void setPaymentRequestSuccess(boolean paymentRequestSuccess) {
        this.paymentRequestSuccess = paymentRequestSuccess;
    }
    
    @Column(name = "merchantId")
    public String getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
    
    @Column(name = "paymentType")
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    @Column(name = "paymentRequestJson")
    public String getPaymentRequestJson() {
        return paymentRequestJson;
    }
    
    public void setPaymentRequestJson(String paymentRequestJson) {
        this.paymentRequestJson = paymentRequestJson;
    }
    
    @Column(name = "paymentResponseJson")
    public String getPaymentResponseJson() {
        return paymentResponseJson;
    }
    
    public void setPaymentResponseJson(String paymentResponseJson) {
        this.paymentResponseJson = paymentResponseJson;
    }
    
    @Column(name = "created_date")
    public Date getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    @Column(name = "created_by")
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    @Column(name = "updated_date")
    public Date getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    @Column(name = "updated_by")
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    
}
