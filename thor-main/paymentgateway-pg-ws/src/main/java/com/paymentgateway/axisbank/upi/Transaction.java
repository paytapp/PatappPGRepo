package com.paymentgateway.axisbank.upi;

import org.springframework.stereotype.Service;

@Service("axisBankUpiTransaction")
public class Transaction {

	private String customerVpa;
	private String merchId;
	private String merchChanId;
	private String checkSum;
	private String unqTxnId;
	private String unqCustId;
	private String amount;
	private String txnDtl;
	private String currency;

	private String orderId;
	private String expiry;
	private String sId;
	private String txnRefundId;
	private String mobNo;
	private String txnRefundAmount;

	private String refundReason;
	private String code;
	private String result;
	private String data;
	private String wCollectTxnId;
	private String merchTranId;

	private String merchantTransactionId;
	private String gatewayTransactionId;
	private String gatewayResponseCode;
	private String gatewayResponseMessage;
	private String rrn;
	
	
	private String refid;
	private String tranid;
	private String status;
	private String remarks;
	private String txnid;

	public String getCustomerVpa() {
		return customerVpa;
	}

	public void setCustomerVpa(String customerVpa) {
		this.customerVpa = customerVpa;
	}

	public String getMerchId() {
		return merchId;
	}

	public void setMerchId(String merchId) {
		this.merchId = merchId;
	}

	public String getMerchChanId() {
		return merchChanId;
	}

	public void setMerchChanId(String merchChanId) {
		this.merchChanId = merchChanId;
	}

	public String getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}

	public String getUnqTxnId() {
		return unqTxnId;
	}

	public void setUnqTxnId(String unqTxnId) {
		this.unqTxnId = unqTxnId;
	}

	public String getUnqCustId() {
		return unqCustId;
	}

	public void setUnqCustId(String unqCustId) {
		this.unqCustId = unqCustId;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getTxnDtl() {
		return txnDtl;
	}

	public void setTxnDtl(String txnDtl) {
		this.txnDtl = txnDtl;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	public String getsId() {
		return sId;
	}

	public void setsId(String sId) {
		this.sId = sId;
	}

	public String getTxnRefundId() {
		return txnRefundId;
	}

	public void setTxnRefundId(String txnRefundId) {
		this.txnRefundId = txnRefundId;
	}

	public String getMobNo() {
		return mobNo;
	}

	public void setMobNo(String mobNo) {
		this.mobNo = mobNo;
	}

	public String getTxnRefundAmount() {
		return txnRefundAmount;
	}

	public void setTxnRefundAmount(String txnRefundAmount) {
		this.txnRefundAmount = txnRefundAmount;
	}

	public String getRefundReason() {
		return refundReason;
	}

	public void setRefundReason(String refundReason) {
		this.refundReason = refundReason;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getwCollectTxnId() {
		return wCollectTxnId;
	}

	public void setwCollectTxnId(String wCollectTxnId) {
		this.wCollectTxnId = wCollectTxnId;
	}

	public String getMerchTranId() {
		return merchTranId;
	}

	public void setMerchTranId(String merchTranId) {
		this.merchTranId = merchTranId;
	}

	public String getMerchantTransactionId() {
		return merchantTransactionId;
	}

	public void setMerchantTransactionId(String merchantTransactionId) {
		this.merchantTransactionId = merchantTransactionId;
	}

	public String getGatewayTransactionId() {
		return gatewayTransactionId;
	}

	public void setGatewayTransactionId(String gatewayTransactionId) {
		this.gatewayTransactionId = gatewayTransactionId;
	}

	public String getGatewayResponseCode() {
		return gatewayResponseCode;
	}

	public void setGatewayResponseCode(String gatewayResponseCode) {
		this.gatewayResponseCode = gatewayResponseCode;
	}

	public String getGatewayResponseMessage() {
		return gatewayResponseMessage;
	}

	public void setGatewayResponseMessage(String gatewayResponseMessage) {
		this.gatewayResponseMessage = gatewayResponseMessage;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

	public String getRefid() {
		return refid;
	}

	public void setRefid(String refid) {
		this.refid = refid;
	}

	public String getTranid() {
		return tranid;
	}

	public void setTranid(String tranid) {
		this.tranid = tranid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getTxnid() {
		return txnid;
	}

	public void setTxnid(String txnid) {
		this.txnid = txnid;
	}

}
