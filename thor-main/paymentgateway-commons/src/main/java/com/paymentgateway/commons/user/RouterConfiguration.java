package com.paymentgateway.commons.user;

import java.util.Date;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.paymentgateway.commons.util.TDRStatus;

public class RouterConfiguration  {

	private String id;
	private String currency;
	private String paymentType;
	private String mopType;
	private String transactionType;

	@Enumerated(EnumType.STRING)
	private TDRStatus status;

	private Date createdDate;
	private Date updatedDate;

	private String merchant;
	private int failureCount;
	private String identifier;
	private Date startTime;
	private Date endTime;
	private Date stopTime;
	private Date failoverStartTime;
	private Date failoverEndTime;
	private int allowedFailureCount;
	private boolean  onUsoffUs;
	private boolean  allowAmountBasedRouting;
	private boolean currentlyActive;
	private boolean alwaysOn;
	private boolean switchOnFail;
	private int loadPercentage;
	private boolean  isDown;
	private String mode;
	private String priority;
	private double minAmount;
	private double maxAmount;
	private String requestedBy;
	private String updatedBy;
	private String retryMinutes;
	private String slabId;
	
	@Enumerated(EnumType.STRING)
	private AccountCurrencyRegion paymentsRegion;
	
	@Enumerated(EnumType.STRING)
	private CardHolderType cardHolderType;
	
	private String payId;
	private String paymentTypeName;
	private String mopTypeName;
	private String statusName;
	private String onUsoffUsName;
	private String rulePriority;
	
	private Double acqTdr;
	
	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public int getLoadPercentage() {
		return loadPercentage;
	}

	public void setLoadPercentage(int loadPercentage) {
		this.loadPercentage = loadPercentage;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	private String acquirer;

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public TDRStatus getStatus() {
		return status;
	}

	public void setStatus(TDRStatus status) {
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

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}


	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}


	public int getFailureCount() {
		return failureCount;
	}

	public void setFailureCount(int failureCount) {
		this.failureCount = failureCount;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public boolean isCurrentlyActive() {
		return currentlyActive;
	}

	public void setCurrentlyActive(boolean currentlyActive) {
		this.currentlyActive = currentlyActive;
	}

	public boolean isAlwaysOn() {
		return alwaysOn;
	}

	public void setAlwaysOn(boolean alwaysOn) {
		this.alwaysOn = alwaysOn;
	}

	public boolean isSwitchOnFail() {
		return switchOnFail;
	}

	public void setSwitchOnFail(boolean switchOnFail) {
		this.switchOnFail = switchOnFail;
	}

	public String getPaymentTypeName() {
		return paymentTypeName;
	}

	public void setPaymentTypeName(String paymentTypeName) {
		this.paymentTypeName = paymentTypeName;
	}

	public String getMopTypeName() {
		return mopTypeName;
	}

	public void setMopTypeName(String mopTypeName) {
		this.mopTypeName = mopTypeName;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}


	public boolean isOnUsoffUs() {
		return onUsoffUs;
	}

	public void setOnUsoffUs(boolean onUsoffUs) {
		this.onUsoffUs = onUsoffUs;
	}

	public String getOnUsoffUsName() {
		return onUsoffUsName;
	}

	public void setOnUsoffUsName(String onUsoffUsName) {
		this.onUsoffUsName = onUsoffUsName;
	}

	public Date getFailoverStartTime() {
		return failoverStartTime;
	}

	public void setFailoverStartTime(Date failoverStartTime) {
		this.failoverStartTime = failoverStartTime;
	}

	public Date getFailoverEndTime() {
		return failoverEndTime;
	}

	public void setFailoverEndTime(Date failoverEndTime) {
		this.failoverEndTime = failoverEndTime;
	}

	public Date getStopTime() {
		return stopTime;
	}

	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}

	public int getAllowedFailureCount() {
		return allowedFailureCount;
	}

	public void setAllowedFailureCount(int allowedFailureCount) {
		this.allowedFailureCount = allowedFailureCount;
	}

	public boolean isDown() {
		return isDown;
	}

	public void setDown(boolean isDown) {
		this.isDown = isDown;
	}

	public double getMinAmount() {
		return minAmount;
	}

	public void setMinAmount(double minAmount) {
		this.minAmount = minAmount;
	}

	public double getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(double maxAmount) {
		this.maxAmount = maxAmount;
	}

	public boolean isAllowAmountBasedRouting() {
		return allowAmountBasedRouting;
	}

	public void setAllowAmountBasedRouting(boolean allowAmountBasedRouting) {
		this.allowAmountBasedRouting = allowAmountBasedRouting;
	}

	public String getRulePriority() {
		return rulePriority;
	}

	public void setRulePriority(String rulePriority) {
		this.rulePriority = rulePriority;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getRetryMinutes() {
		return retryMinutes;
	}

	public void setRetryMinutes(String retryMinutes) {
		this.retryMinutes = retryMinutes;
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

	public String getSlabId() {
		return slabId;
	}

	public void setSlabId(String slabId) {
		this.slabId = slabId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Double getAcqTdr() {
		return acqTdr;
	}

	public void setAcqTdr(Double acqTdr) {
		this.acqTdr = acqTdr;
	}


}
