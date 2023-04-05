/*
 * package com.paymentgateway.commons.user;
 * 
 * import java.io.Serializable; import java.util.Date;
 * 
 * import javax.persistence.Entity; import javax.persistence.EnumType; import
 * javax.persistence.Enumerated; import javax.persistence.GeneratedValue; import
 * javax.persistence.GenerationType; import javax.persistence.Id;
 * 
 * import org.hibernate.annotations.Cache; import
 * org.hibernate.annotations.CacheConcurrencyStrategy; import
 * org.hibernate.annotations.Proxy;
 * 
 * import com.paymentgateway.commons.util.MopType; import
 * com.paymentgateway.commons.util.PaymentType; import
 * com.paymentgateway.commons.util.TransactionType;
 * 
 *//**
	 * @author Amitosh
	 *
	 *//*
		 * 
		 * @Entity
		 * 
		 * @Proxy(lazy = false)
		 * 
		 * @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE) public class
		 * PaymentCombinationDetails implements Serializable {
		 * 
		 * private static final long serialVersionUID = 6028946576870132362L;
		 * 
		 * public PaymentCombinationDetails(){
		 * 
		 * }
		 * 
		 * @Id
		 * 
		 * @GeneratedValue(strategy = GenerationType.AUTO) private Long id;
		 * 
		 * @Enumerated(EnumType.STRING) private MopType mopType;
		 * 
		 * @Enumerated(EnumType.STRING) private PaymentType paymentType;
		 * 
		 * @Enumerated(EnumType.STRING) private TransactionType transactionType;
		 * 
		 * private String acquirerName; private String payId; private String currency;
		 * private String status; private Date createdDate; private Date updatedDate;
		 * private String updateBy; private String requestedBy;
		 * 
		 * public Long getId() { return id; } public void setId(Long id) { this.id = id;
		 * } public MopType getMopType() { return mopType; } public void
		 * setMopType(MopType mopType) { this.mopType = mopType; } public PaymentType
		 * getPaymentType() { return paymentType; } public void
		 * setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }
		 * public TransactionType getTransactionType() { return transactionType; }
		 * public void setTransactionType(TransactionType transactionType) {
		 * this.transactionType = transactionType; } public String getAcquirerName() {
		 * return acquirerName; } public void setAcquirerName(String acquirerName) {
		 * this.acquirerName = acquirerName; } public String getPayId() { return payId;
		 * } public void setPayId(String payId) { this.payId = payId; } public String
		 * getStatus() { return status; } public void setStatus(String status) {
		 * this.status = status; } public Date getCreatedDate() { return createdDate; }
		 * public void setCreatedDate(Date createdDate) { this.createdDate =
		 * createdDate; } public Date getUpdatedDate() { return updatedDate; } public
		 * void setUpdatedDate(Date updatedDate) { this.updatedDate = updatedDate; }
		 * public String getUpdateBy() { return updateBy; } public void
		 * setUpdateBy(String updateBy) { this.updateBy = updateBy; } public String
		 * getRequestedBy() { return requestedBy; } public void setRequestedBy(String
		 * requestedBy) { this.requestedBy = requestedBy; } public String getCurrency()
		 * { return currency; } public void setCurrency(String currency) { this.currency
		 * = currency; }
		 * 
		 * }
		 */