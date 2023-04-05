/**
 * 
 */
package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;

import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.onUsOffUs;

/**
 * @author Amitosh Aanand
 *
 */
@Entity
@Proxy(lazy = false)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ResellerCharges implements Serializable {

	private static final long serialVersionUID = 6373356150889155995L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Enumerated(EnumType.STRING)
	private MopType mopType;

	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;

	@Enumerated(EnumType.STRING)
	private TransactionType transactionType;

	@Enumerated(EnumType.STRING)
	private AccountCurrencyRegion paymentsRegion;

	@Enumerated(EnumType.STRING)
	private CardHolderType cardHolderType;

	private String merchantPayId;
	private String resellerId;
	private String currency;
	private String slabId;
	private String existingTdr;
	private String existingSuf;
	private String resellerPercentage;
	private String resellerFixedCharge;
	private String pgPercentage;
	private String pgFixedCharge;
	private String gst;
	private String status;
	private String createdBy;
	private Date createdDate;
	private String updatedBy;
	private Date updatedDate;
	private String chargeFrom;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public MopType getMopType() {
		return mopType;
	}
	public void setMopType(MopType mopType) {
		this.mopType = mopType;
	}
	public PaymentType getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}
	public TransactionType getTransactionType() {
		return transactionType;
	}
	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}
	public AccountCurrencyRegion getPaymentsRegion() {
		return paymentsRegion;
	}
	public void setPaymentsRegion(AccountCurrencyRegion paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}
	public CardHolderType getCardHolderType() {
		return cardHolderType;
	}
	public void setCardHolderType(CardHolderType cardHolderType) {
		this.cardHolderType = cardHolderType;
	}
	public String getMerchantPayId() {
		return merchantPayId;
	}
	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}
	public String getResellerId() {
		return resellerId;
	}
	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getSlabId() {
		return slabId;
	}
	public void setSlabId(String slabId) {
		this.slabId = slabId;
	}
	public String getExistingTdr() {
		return existingTdr;
	}
	public void setExistingTdr(String existingTdr) {
		this.existingTdr = existingTdr;
	}
	public String getExistingSuf() {
		return existingSuf;
	}
	public void setExistingSuf(String existingSuf) {
		this.existingSuf = existingSuf;
	}
	public String getResellerPercentage() {
		return resellerPercentage;
	}
	public void setResellerPercentage(String resellerPercentage) {
		this.resellerPercentage = resellerPercentage;
	}
	public String getResellerFixedCharge() {
		return resellerFixedCharge;
	}
	public void setResellerFixedCharge(String resellerFixedCharge) {
		this.resellerFixedCharge = resellerFixedCharge;
	}
	public String getPgPercentage() {
		return pgPercentage;
	}
	public void setPgPercentage(String pgPercentage) {
		this.pgPercentage = pgPercentage;
	}
	public String getPgFixedCharge() {
		return pgFixedCharge;
	}
	public void setPgFixedCharge(String pgFixedCharge) {
		this.pgFixedCharge = pgFixedCharge;
	}
	public String getGst() {
		return gst;
	}
	public void setGst(String gst) {
		this.gst = gst;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	public String getChargeFrom() {
		return chargeFrom;
	}
	public void setChargeFrom(String chargeFrom) {
		this.chargeFrom = chargeFrom;
	}
}