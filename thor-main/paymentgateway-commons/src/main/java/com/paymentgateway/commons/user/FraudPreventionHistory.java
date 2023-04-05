package com.paymentgateway.commons.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.paymentgateway.commons.util.FraudRuleType;
import com.paymentgateway.commons.util.TDRStatus;

public class FraudPreventionHistory {

	private String id;
	private String payId;
	private String email;  
	private String ipAddress;  
	private String subnetMask;
	private String domainName;
	private String negativeBin;
	private String negativeCard;   
	private String minutesTxnLimit; 
	private String currency;
	private String minTransactionAmount;
	private String maxTransactionAmount;
	private String issuerCountry; 
	private String userCountry;
	private String whiteListIpAddress;
	@Column(name = "LoginIpAddress")
	private String LoginIpAddress;
	public void setLoginIpAddress(String loginIpAddress) {
		LoginIpAddress = loginIpAddress;
	}
	@Column(name = "LoginEmailId")
	private String LoginEmailId;
	
	@CreationTimestamp
	private Date createDate;
	@UpdateTimestamp
	private Date updateDate;
	@Enumerated(EnumType.STRING)
	private TDRStatus status;
	@Enumerated(EnumType.STRING)
	private FraudRuleType fraudType;
	private String perCardTransactionAllowed;
	private String noOfTransactionAllowed;
	@Transient
	private String ruleGroupId;
	@Transient
	private String currencyName;
	
	private String dateActiveFrom;
	private String dateActiveTo;
	private String startTime;
	private String endTime;
	private String repeatDays;
	private boolean alwaysOnFlag;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getSubnetMask() {
		return subnetMask;
	}
	public void setSubnetMask(String subnetMask) {
		this.subnetMask = subnetMask;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public String getNegativeBin() {
		return negativeBin;
	}
	public void setNegativeBin(String negativeBin) {
		this.negativeBin = negativeBin;
	}
	public String getNegativeCard() {
		return negativeCard;
	}
	public void setNegativeCard(String negativeCard) {
		this.negativeCard = negativeCard;
	}
	public String getMinutesTxnLimit() {
		return minutesTxnLimit;
	}
	public void setMinutesTxnLimit(String minutesTxnLimit) {
		this.minutesTxnLimit = minutesTxnLimit;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getMinTransactionAmount() {
		return minTransactionAmount;
	}
	public void setMinTransactionAmount(String minTransactionAmount) {
		this.minTransactionAmount = minTransactionAmount;
	}
	public String getMaxTransactionAmount() {
		return maxTransactionAmount;
	}
	public void setMaxTransactionAmount(String maxTransactionAmount) {
		this.maxTransactionAmount = maxTransactionAmount;
	}
	public String getIssuerCountry() {
		return issuerCountry;
	}
	public void setIssuerCountry(String issuerCountry) {
		this.issuerCountry = issuerCountry;
	}
	public String getUserCountry() {
		return userCountry;
	}
	public void setUserCountry(String userCountry) {
		this.userCountry = userCountry;
	}
	public String getWhiteListIpAddress() {
		return whiteListIpAddress;
	}
	public void setWhiteListIpAddress(String whiteListIpAddress) {
		this.whiteListIpAddress = whiteListIpAddress;
	}
	
	public String getLoginEmailId() {
		return LoginEmailId;
	}
	public void setLoginEmailId(String loginEmailId) {
		LoginEmailId = loginEmailId;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public TDRStatus getStatus() {
		return status;
	}
	public void setStatus(TDRStatus status) {
		this.status = status;
	}
	public FraudRuleType getFraudType() {
		return fraudType;
	}
	public void setFraudType(FraudRuleType fraudType) {
		this.fraudType = fraudType;
	}
	public String getPerCardTransactionAllowed() {
		return perCardTransactionAllowed;
	}
	public void setPerCardTransactionAllowed(String perCardTransactionAllowed) {
		this.perCardTransactionAllowed = perCardTransactionAllowed;
	}
	public String getNoOfTransactionAllowed() {
		return noOfTransactionAllowed;
	}
	public void setNoOfTransactionAllowed(String noOfTransactionAllowed) {
		this.noOfTransactionAllowed = noOfTransactionAllowed;
	}
	public String getRuleGroupId() {
		return ruleGroupId;
	}
	public void setRuleGroupId(String ruleGroupId) {
		this.ruleGroupId = ruleGroupId;
	}
	public String getCurrencyName() {
		return currencyName;
	}
	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}
	public String getDateActiveFrom() {
		return dateActiveFrom;
	}
	public void setDateActiveFrom(String dateActiveFrom) {
		this.dateActiveFrom = dateActiveFrom;
	}
	public String getDateActiveTo() {
		return dateActiveTo;
	}
	public void setDateActiveTo(String dateActiveTo) {
		this.dateActiveTo = dateActiveTo;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getRepeatDays() {
		return repeatDays;
	}
	public void setRepeatDays(String repeatDays) {
		this.repeatDays = repeatDays;
	}
	public boolean isAlwaysOnFlag() {
		return alwaysOnFlag;
	}
	public void setAlwaysOnFlag(boolean alwaysOnFlag) {
		this.alwaysOnFlag = alwaysOnFlag;
	}

}