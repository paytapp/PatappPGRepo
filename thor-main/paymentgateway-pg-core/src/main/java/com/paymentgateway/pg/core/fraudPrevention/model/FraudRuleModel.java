package com.paymentgateway.pg.core.fraudPrevention.model;

import java.util.List;

/**
 * @author Harpreet, Rahul
 *
 */
public class FraudRuleModel {

	private String payId;
	private String email;  
	private String ipAddress;  
	private String subnetMask;
	private String domainName;
	private String status;
	private String negativeBin;
	private String negativeCard;   
	private String minutesTxnLimit; 
	private String currency;
	private String minTransactionAmount;
	private String maxTransactionAmount;
	private String issuerCountry; 
	private String userCountry;

	private String fraudType;
	private String perCardTransactionAllowed;
	private String noOfTransactionAllowed;
	
	private String responseCode;
	private String responseMsg;
	
	private String whiteListIpAddress;
	
	private String dateActiveFrom;
	private String dateActiveTo;
	private String startTime;
	private String endTime;
	private String repeatDays;
	private boolean alwaysOnFlag;
	private String paymentType;
	private String paymentRegionArray;
	private String paymentTypeArray;
	private String paymentRegion;
	private String timePeriod;
	private String amountAllowed;
	
	private String vpa;
	private String vpaTotalTransactionAllowed;

	

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

	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
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
	
	public String getFraudType() {
		return fraudType;
	}

	public void setFraudType(String fraudType) {
		this.fraudType = fraudType;
	}
	
	public String getPerCardTransactionAllowed() {
		return perCardTransactionAllowed;
	}
	
	public void setPerCardTransactionAllowed(String perCardTransactionAllowed) {
		this.perCardTransactionAllowed = perCardTransactionAllowed;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getWhiteListIpAddress() {
		return whiteListIpAddress;
	}

	public void setWhiteListIpAddress(String whiteListIpAddress) {
		this.whiteListIpAddress = whiteListIpAddress;
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

	public boolean isAlwaysOnFlag() {
		return alwaysOnFlag;
	}

	public void setAlwaysOnFlag(boolean alwaysOnFlag) {
		this.alwaysOnFlag = alwaysOnFlag;
	}

	public String getMinutesTxnLimit() {
		return minutesTxnLimit;
	}

	public void setMinutesTxnLimit(String minutesTxnLimit) {
		this.minutesTxnLimit = minutesTxnLimit;
	}

	public String getNoOfTransactionAllowed() {
		return noOfTransactionAllowed;
	}

	public void setNoOfTransactionAllowed(String noOfTransactionAllowed) {
		this.noOfTransactionAllowed = noOfTransactionAllowed;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getPaymentRegion() {
		return paymentRegion;
	}

	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}

	public String getTimePeriod() {
		return timePeriod;
	}

	public void setTimePeriod(String timePeriod) {
		this.timePeriod = timePeriod;
	}

	public String getAmountAllowed() {
		return amountAllowed;
	}

	public void setAmountAllowed(String amountAllowed) {
		this.amountAllowed = amountAllowed;
	}

	public String getPaymentRegionArray() {
		return paymentRegionArray;
	}

	public void setPaymentRegionArray(String paymentRegionArray) {
		this.paymentRegionArray = paymentRegionArray;
	}

	public String getPaymentTypeArray() {
		return paymentTypeArray;
	}

	public void setPaymentTypeArray(String paymentTypeArray) {
		this.paymentTypeArray = paymentTypeArray;
	}

	public String getVpa() {
		return vpa;
	}

	public void setVpa(String vpa) {
		this.vpa = vpa;
	}

	public String getVpaTotalTransactionAllowed() {
		return vpaTotalTransactionAllowed;
	}

	public void setVpaTotalTransactionAllowed(String vpaTotalTransactionAllowed) {
		this.vpaTotalTransactionAllowed = vpaTotalTransactionAllowed;
	}
	

	
}