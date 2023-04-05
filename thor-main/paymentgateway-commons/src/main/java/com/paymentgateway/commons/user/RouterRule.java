/*
 * package com.paymentgateway.commons.user;
 * 
 * import java.io.Serializable; import java.util.Date;
 * 
 * import javax.persistence.Entity; import javax.persistence.EnumType; import
 * javax.persistence.Enumerated; import javax.persistence.GeneratedValue; import
 * javax.persistence.GenerationType; import javax.persistence.Id; import
 * javax.persistence.Table; import javax.persistence.Transient;
 * 
 * import org.hibernate.annotations.Proxy;
 * 
 * import com.paymentgateway.commons.util.TDRStatus;
 * 
 * @Entity
 * 
 * @Proxy(lazy= false)
 * 
 * @Table public class RouterRule implements Serializable{
 * 
 * private static final long serialVersionUID = 6451344616066902378L;
 * 
 * @Id
 * 
 * @GeneratedValue(strategy=GenerationType.AUTO) private Long id; private String
 * currency; private String paymentType; private String mopType;
 * 
 * private String transactionType;
 * 
 * @Enumerated(EnumType.STRING) private TDRStatus status;
 * 
 * private boolean onUsFlag;
 * 
 * private Date createdDate; private Date updatedDate;
 * 
 * private String merchant; private String requestedBy; private String
 * approvedBy;
 * 
 * @Enumerated(EnumType.STRING) private AccountCurrencyRegion paymentsRegion;
 * 
 * @Enumerated(EnumType.STRING) private CardHolderType cardHolderType;
 * 
 * 
 * @Transient private String payId;
 * 
 * @Transient private String merchantName;
 * 
 * 
 * private String otp; private String requestedAction; private double minAmount;
 * private double maxAmount; private String slabId;
 * 
 * public String getPayId() { return payId; }
 * 
 * public void setPayId(String payId) { this.payId = payId; }
 * 
 * private String acquirerMap;
 * 
 * public Long getId() { return id; }
 * 
 * public void setId(Long id) { this.id = id; }
 * 
 * public String getCurrency() { return currency; }
 * 
 * public void setCurrency(String currency) { this.currency = currency; }
 * 
 * public String getPaymentType() { return paymentType; }
 * 
 * public void setPaymentType(String paymentType) { this.paymentType =
 * paymentType; }
 * 
 * public String getMopType() { return mopType; }
 * 
 * public void setMopType(String mopType) { this.mopType = mopType; }
 * 
 * public String getTransactionType() { return transactionType; }
 * 
 * public void setTransactionType(String transactionType) { this.transactionType
 * = transactionType; }
 * 
 * public TDRStatus getStatus() { return status; }
 * 
 * public void setStatus(TDRStatus status) { this.status = status; }
 * 
 * public boolean isOnUsFlag() { return onUsFlag; }
 * 
 * public void setOnUsFlag(boolean onUsFlag) { this.onUsFlag = onUsFlag; }
 * 
 * public Date getCreatedDate() { return createdDate; }
 * 
 * public void setCreatedDate(Date createdDate) { this.createdDate =
 * createdDate; }
 * 
 * public Date getUpdatedDate() { return updatedDate; }
 * 
 * public void setUpdatedDate(Date updatedDate) { this.updatedDate =
 * updatedDate; }
 * 
 * public String getMerchant() { return merchant; }
 * 
 * public void setMerchant(String merchant) { this.merchant = merchant; }
 * 
 * public String getAcquirerMap() { return acquirerMap; }
 * 
 * public void setAcquirerMap(String acquirerMap) { this.acquirerMap =
 * acquirerMap; }
 * 
 * public AccountCurrencyRegion getPaymentsRegion() { return paymentsRegion; }
 * 
 * public void setPaymentsRegion(AccountCurrencyRegion paymentsRegion) {
 * this.paymentsRegion = paymentsRegion; }
 * 
 * public CardHolderType getCardHolderType() { return cardHolderType; }
 * 
 * public void setCardHolderType(CardHolderType cardHolderType) {
 * this.cardHolderType = cardHolderType; }
 * 
 * public String getRequestedBy() { return requestedBy; }
 * 
 * public void setRequestedBy(String requestedBy) { this.requestedBy =
 * requestedBy; }
 * 
 * public String getOtp() { return otp; }
 * 
 * public void setOtp(String otp) { this.otp = otp; }
 * 
 * public String getApprovedBy() { return approvedBy; }
 * 
 * public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy;
 * }
 * 
 * public String getMerchantName() { return merchantName; }
 * 
 * public void setMerchantName(String merchantName) { this.merchantName =
 * merchantName; }
 * 
 * public String getRequestedAction() { return requestedAction; }
 * 
 * public void setRequestedAction(String requestedAction) { this.requestedAction
 * = requestedAction; }
 * 
 * public double getMinAmount() { return minAmount; }
 * 
 * public void setMinAmount(double minAmount) { this.minAmount = minAmount; }
 * 
 * public double getMaxAmount() { return maxAmount; }
 * 
 * public void setMaxAmount(double maxAmount) { this.maxAmount = maxAmount; }
 * 
 * public String getSlabId() { return slabId; }
 * 
 * public void setSlabId(String slabId) { this.slabId = slabId; }
 * 
 * 
 * }
 */