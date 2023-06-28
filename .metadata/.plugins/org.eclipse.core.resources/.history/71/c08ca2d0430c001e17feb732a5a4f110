package com.paymentgateway.phonePe;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;

import com.paymentgateway.commons.user.MPAMerchant;

@Entity
@Table(name = "transaction", schema = "paytapp")
public class Transaction {
   
    private int id;
    private String purchaseId;
    private String userId;
    private BigDecimal transactionAmount;
    private boolean transactionSuccess;
    private PaymentResponse paymentResponseIdRef;
    private StatusCheckResponse statusCheckResponseIdRef;
    private String merchantId;
    private Date createdDate;
    private String createdBy;
    private Date updatedDate;
    private String updatedBy;
    

	

//	public Transaction(int id, String purchaseId, String userId, BigDecimal transactionAmount,
//			boolean transactionSuccess, PaymentResponse paymentResponseIdRef, StatusCheckResponse statusCheckResponseIdRef,
//			String merchantId, Date createdDate, String createdBy, Date updatedDate, String updatedBy) {
//		super();
//		this.id = id;
//		this.purchaseId = purchaseId;
//		this.userId = userId;
//		this.transactionAmount = transactionAmount;
//		this.transactionSuccess = transactionSuccess;
//		this.paymentResponseIdRef = paymentResponseIdRef;
//		this.statusCheckResponseIdRef = statusCheckResponseIdRef;
//		this.merchantId = merchantId;
//		this.createdDate = createdDate;
//		this.createdBy = createdBy;
//		this.updatedDate = updatedDate;
//		this.updatedBy = updatedBy;
//	}
//
//	public Transaction() {
//		
//	}

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
	
	@Column(name = "transactionSuccess", nullable = false)
	public boolean getTransactionSuccess() {
		return transactionSuccess;
	}

	public void setTransactionSuccess(boolean transactionSuccess) {
		this.transactionSuccess = transactionSuccess;
	}

	@OneToOne(mappedBy = "id", cascade = CascadeType.ALL)
	public PaymentResponse getPaymentResponseIdRef() {
		return paymentResponseIdRef;
	}

	public void setPaymentResponseIdRef(PaymentResponse paymentResponseIdRef) {
		this.paymentResponseIdRef = paymentResponseIdRef;
	}

	@OneToOne(mappedBy = "id", cascade = CascadeType.ALL)
	public StatusCheckResponse getStatusCheckResponseIdRef() {
		return statusCheckResponseIdRef;
	}

	public void setStatusCheckResponseIdRef(StatusCheckResponse statusCheckResponseIdRef) {
		this.statusCheckResponseIdRef = statusCheckResponseIdRef;
	}

	@OneToOne(mappedBy = "emailId", cascade = CascadeType.ALL)
	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
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
