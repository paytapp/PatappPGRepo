package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class TransactionSummaryReport implements Serializable {

	private static final long serialVersionUID = 7868515293781701587L;
	
	private String transactionId;
	private String txnDate;
	private String businessName;
	private String settleDate;
	private String approvedAmount;
	private String refundedAmount;
	private String tdr;
	private String serviceTax;
	private String chargebackAmount;
	private String netAmount;
	private String txnType;
	private String paymentMethod;
	private String acquirer;
	private String status;
	private String customerEmail;
	private String mopType;
	private String currencyName;
	private String orderId;
	private String responseMsg;
	private String customerName;
	private String cardNo;
	private String payId;
	private String currencyCode;
	private String countryName;
	private String custPhone;
	private String origTransactionId;
	private String origTxnDate;
	private String captureTxnId;
	private String refundDate;	
	private String productDesc;	
	private String internalRequestDesc;
	private String oid;
	private String internalTxnAuthentication;
	private String refundableAmount;
	private String authCode;
	private String acqId;
	private String internalUserEmail;
	private String internalCustIp;
	private String internalCardIssusserBank;
	private String internalCardIssusserCountry;
	private String rowNumber;
	private String pgTxnMessage;
	private String resellerId;
	private Date date;
	private String settlementStatus;
	private String totalAggregatorcommissionAmount;
	private String totalAmountPayToMerchant;
	private String totalPayoutToNodal;
	private String bankName;
	private String ifscCode;
	private String accountNumber;
	private String aggregatorName;
	private String nodalAccount;
	private String currentDate;
	private String bankRecieveFund;
	private String msgType;
	private BigDecimal totalNodalAmount;
	private String txnRefNumber;
	private String serialNumber;
	private String merchantFixCharge;
	private String detail;
	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private String benAddress1;
	private String benAddress2;
	private String benAddress3;
	private String benAddress4;
	private String sendertoRcvrInfo;
	

	public BigDecimal getTotalNodalAmount() {
		return totalNodalAmount;
	}
	public void setTotalNodalAmount(BigDecimal totalNodalAmount) {
		this.totalNodalAmount = totalNodalAmount;
	}
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public String getBankRecieveFund() {
		return bankRecieveFund;
	}
	public void setBankRecieveFund(String bankRecieveFund) {
		this.bankRecieveFund = bankRecieveFund;
	}
	
	public String getCurrentDate() {
		return currentDate;
	}
	public void setCurrentDate(String currentDate) {
		this.currentDate = currentDate;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getTxnDate() {
		return txnDate;
	}
	public void setTxnDate(String txnDate) {
		this.txnDate = txnDate;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public String getSettleDate() {
		return settleDate;
	}
	public void setSettleDate(String settleDate) {
		this.settleDate = settleDate;
	}
	
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}	
	public String getAcquirer() {
		return acquirer;
	}
	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMopType() {
		return mopType;
	}
	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public String getCurrencyName() {
		return currencyName;
	}
	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}
	public String getOrigTransactionId() {
		return origTransactionId;
	}
	public void setOrigTransactionId(String origTransactionId) {
		this.origTransactionId = origTransactionId;
	}
	public String getOrigTxnDate() {
		return origTxnDate;
	}
	public void setOrigTxnDate(String origTxnDate) {
		this.origTxnDate = origTxnDate;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getResponseMsg() {
		return responseMsg;
	}
	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getCustomerEmail() {
		return customerEmail;
	}
	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getApprovedAmount() {
		return approvedAmount;
	}
	public void setApprovedAmount(String approvedAmount) {
		this.approvedAmount = approvedAmount;
	}
	public String getRefundedAmount() {
		return refundedAmount;
	}
	public void setRefundedAmount(String refundedAmount) {
		this.refundedAmount = refundedAmount;
	}
	public String getTdr() {
		return tdr;
	}
	public void setTdr(String tdr) {
		this.tdr = tdr;
	}
	public String getServiceTax() {
		return serviceTax;
	}
	public void setServiceTax(String serviceTax) {
		this.serviceTax = serviceTax;
	}
	public String getChargebackAmount() {
		return chargebackAmount;
	}
	public void setChargebackAmount(String chargebackAmount) {
		this.chargebackAmount = chargebackAmount;
	}
	public String getNetAmount() {
		return netAmount;
	}
	public void setNetAmount(String netAmount) {
		this.netAmount = netAmount;
	}
	public String getCaptureTxnId() {
		return captureTxnId;
	}
	public void setCaptureTxnId(String captureTxnId) {
		this.captureTxnId = captureTxnId;
	}
	public String getRefundDate() {
		return refundDate;
	}
	public void setRefundDate(String refundDate) {
		this.refundDate = refundDate;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public String getCountryName() {
		return countryName;
	}
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}
	public String getProductDesc() {
		return productDesc;
	}
	public void setProductDesc(String productDesc) {
		this.productDesc = productDesc;
	}
	public String getInternalRequestDesc() {
		return internalRequestDesc;
	}
	public void setInternalRequestDesc(String internalRequestDesc) {
		this.internalRequestDesc = internalRequestDesc;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	public String getInternalTxnAuthentication() {
		return internalTxnAuthentication;
	}
	public void setInternalTxnAuthentication(String internalTxnAuthentication) {
		this.internalTxnAuthentication = internalTxnAuthentication;
	}
	public String getRefundableAmount() {
		return refundableAmount;
	}
	public void setRefundableAmount(String refundableAmount) {
		this.refundableAmount = refundableAmount;
	}
	public String getAuthCode() {
		return authCode;
	}
	public void setAuthCode(String authCode) {
		this.authCode = authCode;

	}
	public String getAcqId() {
		return acqId;
	}
	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}
	public String getInternalCustIp() {
		return internalCustIp;
	}
	public void setInternalCustIp(String internalCustIp) {
		this.internalCustIp = internalCustIp;
	}
	public String getInternalUserEmail() {
		return internalUserEmail;
	}
	public void setInternalUserEmail(String internalUserEmail) {
		this.internalUserEmail = internalUserEmail;
	}
	public String getInternalCardIssusserBank() {
		return internalCardIssusserBank;
	}
	public void setInternalCardIssusserBank(String internalCardIssusserBank) {
		this.internalCardIssusserBank = internalCardIssusserBank;
	}
	public String getInternalCardIssusserCountry() {
		return internalCardIssusserCountry;
	}
	public void setInternalCardIssusserCountry(String internalCardIssusserCountry) {
		this.internalCardIssusserCountry = internalCardIssusserCountry;
	}
	public String getRowNumber() {
		return rowNumber;
	}
	public void setRowNumber(String rowNumber) {
		this.rowNumber = rowNumber;
	}
	public String getPgTxnMessage() {
		return pgTxnMessage;
	}
	public void setPgTxnMessage(String pgTxnMessage) {
		this.pgTxnMessage = pgTxnMessage;
	}
	public String getResellerId() {
		return resellerId;
	}
	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
	}
	public String getCustPhone() {
		return custPhone;
	}
	public void setCustPhone(String custPhone) {
		this.custPhone = custPhone;
	}
	public String getSettlementStatus() {
		return settlementStatus;
	}
	public void setSettlementStatus(String settlementStatus) {
		this.settlementStatus = settlementStatus;
	}
	public String getTotalAggregatorcommissionAmount() {
		return totalAggregatorcommissionAmount;
	}
	public void setTotalAggregatorcommissionAmount(
			String totalAggregatorcommissionAmount) {
		this.totalAggregatorcommissionAmount = totalAggregatorcommissionAmount;
	}
	public String getTotalAmountPayToMerchant() {
		return totalAmountPayToMerchant;
	}
	public void setTotalAmountPayToMerchant(String totalAmountPayToMerchant) {
		this.totalAmountPayToMerchant = totalAmountPayToMerchant;
	}
	public String getTotalPayoutToNodal() {
		return totalPayoutToNodal;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getIfscCode() {
		return ifscCode;
	}
	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public void setTotalPayoutToNodal(String totalPayoutToNodal) {
		this.totalPayoutToNodal = totalPayoutToNodal;
	}
	public String getAggregatorName() {
		return aggregatorName;
	}
	public void setAggregatorName(String aggregatorName) {
		this.aggregatorName = aggregatorName;
	}
	public String getNodalAccount() {
		return nodalAccount;
	}
	public void setNodalAccount(String nodalAccount) {
		this.nodalAccount = nodalAccount;
	}
	public String getTxnRefNumber() {
		return txnRefNumber;
	}
	public void setTxnRefNumber(String txnRefNumber) {
		this.txnRefNumber = txnRefNumber;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public String getMerchantFixCharge() {
		return merchantFixCharge;
	}
	public void setMerchantFixCharge(String merchantFixCharge) {
		this.merchantFixCharge = merchantFixCharge;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getAddressLine1() {
		return addressLine1;
	}
	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}
	public String getAddressLine2() {
		return addressLine2;
	}
	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}
	public String getAddressLine3() {
		return addressLine3;
	}
	public void setAddressLine3(String addressLine3) {
		this.addressLine3 = addressLine3;
	}
	public String getBenAddress1() {
		return benAddress1;
	}
	public void setBenAddress1(String benAddress1) {
		this.benAddress1 = benAddress1;
	}
	public String getBenAddress2() {
		return benAddress2;
	}
	public void setBenAddress2(String benAddress2) {
		this.benAddress2 = benAddress2;
	}
	public String getBenAddress3() {
		return benAddress3;
	}
	public void setBenAddress3(String benAddress3) {
		this.benAddress3 = benAddress3;
	}
	public String getBenAddress4() {
		return benAddress4;
	}
	public void setBenAddress4(String benAddress4) {
		this.benAddress4 = benAddress4;
	}
	public String getSendertoRcvrInfo() {
		return sendertoRcvrInfo;
	}
	public void setSendertoRcvrInfo(String sendertoRcvrInfo) {
		this.sendertoRcvrInfo = sendertoRcvrInfo;
	}
	
	
		}
