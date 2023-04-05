package com.paymentgateway.commons.user;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Proxy;

@Entity
@Proxy(lazy=false)
public class NotificationEmailer implements Serializable {

	  /**
	 * @ Neeraj
	 */
	private static final long serialVersionUID = -7165663923341485955L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private String payId; 
	private String sendMultipleEmailer;
	private boolean transactionSmsFlag;
	private boolean expressPayFlag;
	private boolean merchantHostedFlag;
	private boolean iframePaymentFlag;
	private boolean transactionAuthenticationEmailFlag;
	private boolean transactionCustomerEmailFlag;
	private boolean refundTransactionCustomerEmailFlag;
	private boolean refundTransactionMerchantEmailFlag;
	private boolean retryTransactionCustomeFlag;
	private boolean surchargeFlag;
	private boolean transactionEmailerFlag;
	public String getSendMultipleEmailer() {
		return sendMultipleEmailer;
	}
	public void setSendMultipleEmailer(String sendMultipleEmailer) {
		this.sendMultipleEmailer = sendMultipleEmailer;
	}
	public boolean isTransactionSmsFlag() {
		return transactionSmsFlag;
	}
	public void setTransactionSmsFlag(boolean transactionSmsFlag) {
		this.transactionSmsFlag = transactionSmsFlag;
	}
	public boolean isExpressPayFlag() {
		return expressPayFlag;
	}
	public void setExpressPayFlag(boolean expressPayFlag) {
		this.expressPayFlag = expressPayFlag;
	}
	public boolean isMerchantHostedFlag() {
		return merchantHostedFlag;
	}
	public void setMerchantHostedFlag(boolean merchantHostedFlag) {
		this.merchantHostedFlag = merchantHostedFlag;
	}
	public boolean isIframePaymentFlag() {
		return iframePaymentFlag;
	}
	public void setIframePaymentFlag(boolean iframePaymentFlag) {
		this.iframePaymentFlag = iframePaymentFlag;
	}
	public boolean isTransactionAuthenticationEmailFlag() {
		return transactionAuthenticationEmailFlag;
	}
	public void setTransactionAuthenticationEmailFlag(boolean transactionAuthenticationEmailFlag) {
		this.transactionAuthenticationEmailFlag = transactionAuthenticationEmailFlag;
	}
	public boolean isTransactionCustomerEmailFlag() {
		return transactionCustomerEmailFlag;
	}
	public void setTransactionCustomerEmailFlag(boolean transactionCustomerEmailFlag) {
		this.transactionCustomerEmailFlag = transactionCustomerEmailFlag;
	}
	public boolean isRefundTransactionCustomerEmailFlag() {
		return refundTransactionCustomerEmailFlag;
	}
	public void setRefundTransactionCustomerEmailFlag(boolean refundTransactionCustomerEmailFlag) {
		this.refundTransactionCustomerEmailFlag = refundTransactionCustomerEmailFlag;
	}
	public boolean isRefundTransactionMerchantEmailFlag() {
		return refundTransactionMerchantEmailFlag;
	}
	public void setRefundTransactionMerchantEmailFlag(boolean refundTransactionMerchantEmailFlag) {
		this.refundTransactionMerchantEmailFlag = refundTransactionMerchantEmailFlag;
	}
	public boolean isRetryTransactionCustomeFlag() {
		return retryTransactionCustomeFlag;
	}
	public void setRetryTransactionCustomeFlag(boolean retryTransactionCustomeFlag) {
		this.retryTransactionCustomeFlag = retryTransactionCustomeFlag;
	}
	public boolean isSurchargeFlag() {
		return surchargeFlag;
	}
	public void setSurchargeFlag(boolean surchargeFlag) {
		this.surchargeFlag = surchargeFlag;
	}
	public boolean isTransactionEmailerFlag() {
		return transactionEmailerFlag;
	}
	public void setTransactionEmailerFlag(boolean transactionEmailerFlag) {
		this.transactionEmailerFlag = transactionEmailerFlag;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
}
