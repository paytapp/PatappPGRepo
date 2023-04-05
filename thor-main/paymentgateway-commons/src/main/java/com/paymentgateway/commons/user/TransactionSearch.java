package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;

import javax.persistence.Transient;

/**
 * @author PG
 *
 */
public class TransactionSearch implements Serializable {

	private static final long serialVersionUID = -4691009307357010956L;

	private BigInteger transactionId;
	private String txnId;
	private String transactionIdString;
	private String payId;
	private String customerName;
	private String customerEmail;
	private String categoryCode;
	private String SKUCode;
	private String customerMobile;
	private String merchants;
	private String txnType;
	private String paymentMethods;
	private String cardNumber;
	private String status;
	private String dateFrom;
	private Date dateTo;
	private String amount;
	private String totalAmount;
	private String orderId;
	private String productName;
	private String ServiceCharge;
	private String quantity;
	private String productDesc;
	private String currency;
	private String mopType;
	private String internalCardIssusserBank;
	private String internalCardIssusserCountry;
	private String refundableAmount;
	private String approvedAmount;
	private String businessName;
	private String pgRefNum;
	private String acqId;
	private String rrn;
	private String subMerchantAccountNo;
	private String BeneficiaryBankName;
	private String BeneficiaryIfscCode;
	private String fundsReceived;
	private String grossTransactionAmt;
	private String aggregatorCommissionAMT;
	private String totalAmtPayable;
	private String totalPayoutNodalAccount;
	private String tDate;
	private String accountNo;
	private String nodalAccountNo;
	private String acquirerType;
	private String netAmountPayout;
	private String pgTxnMessage;
	private String acquirerMode;
	private String transactionRegion;
	private String cardHolderType;
	private String payOutDate;
	private String utrNo;
	private String responseMessage;
	private String accqResponseMessage;
	
	private String totalGstOnMerchant;
	private String totalGstOnAcquirer;
	private String netMerchantPayableAmount;
	private String merchantTdrCalculate;
	private String acquirerTdrCalculate;
	private String surchargeFlag;
	private String acquirerCommissionAMT;
	private String paymentRegion;
	private String doctor;
	private String glocal;
	private String partner;
	private String uniqueId;
	private boolean glocalFlag;
	private String subMerchantId;
	private String deliveryStatus;
	
	private String sufGst;
	private String sufTdr;

	// Refund Report
	private String origTxnId;
	private String refundFlag;
	private String origTxnDate;
	private BigInteger refundTxnId;
	private String refundDate;
	private String origAmount;
	private String refundAmount;
	private String refundStatus;
	private String oId;
	private String origTxnType;
	private int srNo;
	private int totalCount;

	// Summary Report
	private String acquirerSurchargeAmount;
	private String bankSurchargeAmount;
	private String pgSurchargeAmount;
	private String paymentGatewaySurchargeAmount;
	private String totalGstOnPg;
	private String totalGstOnPaymentGateway;
	private String deltaRefundFlag;
	private String transactionCaptureDate;
	private String postSettledFlag;
	private String txnSettledType;
	private String transactionMode;
	private String refundOrderId;
	private String tdr_Surcharge;
	private String gst_charge;
	private String partSettle;
	private String customFlag;

	// For Manual Refund
	private String refundBtnText;
	private String currencyCode;
	private String merchantName;
	private String refundedAmount;
	private String refundAvailable;
	@Transient
	private String btnchargebacktext;
	private String chargebackAmount;
	private String chargebackStatus;
	
	private String acquirerRefNo;
	private String merchantRefNo;

	// For Dispatch Slip
	private boolean dispatchSlipFlag;
	private String invoiceNo;
	private String dispatchSlipNo;
	private String courierServiceProvider;
	private boolean pdfDownloadFlag;

	private String refundDays;
	private String merchantAmount;
	private String deliveryCode;
	private String subMerchantName;
	private String productId;
	private String productPrice;
	private String refundCycle;
	private String vendorID;
	private String objectId;
	
	private String UDF11;
	private String UDF12;
	private String UDF13;
	private String UDF14;
	private String UDF15;
	

	private String UDF16;
	private String UDF17;
	private String UDF18;
	
	private boolean showRefundButton;
	
	//Reseller
	private String resellerCharges;
	private String resellerGST;
	
	private String country;
	private String merchantTdrOrSc;
	private String merchantGst;

	private String payerName;
	private String payeeAddress;
	

	public TransactionSearch() {

	}

	public TransactionSearch(String pgRefNum, String orderId, String totalAmount) {
		this.pgRefNum = pgRefNum;
		this.orderId = orderId;
		this.totalAmount = totalAmount;

	}

	public Object[] myCsvMethod() {
		Object[] objectArray = new Object[20];

		objectArray[0] = srNo;
		objectArray[1] = businessName;
		objectArray[2] = payId;
		objectArray[3] = pgRefNum;
		objectArray[4] = orderId;
		objectArray[5] = tDate;
		objectArray[6] = dateFrom;
		objectArray[7] = txnType;
		objectArray[8] = grossTransactionAmt;
		objectArray[9] = aggregatorCommissionAMT;
		objectArray[10] = acquirerCommissionAMT;
		objectArray[11] = totalAmtPayable;
		objectArray[12] = totalPayoutNodalAccount;
		objectArray[13] = acquirerType;
		objectArray[14] = "";
		objectArray[15] = "Letz Pay";
		objectArray[16] = acquirerType;
		objectArray[17] = refundFlag;
		objectArray[18] = paymentMethods;
		objectArray[19] = mopType;

		return objectArray;
	}

	public Object[] myCsvMethodDownloadPaymentsReport(UserType sessionUserType) {
		Object[] objectArray = new Object[17];
		if (sessionUserType.equals(UserType.MERCHANT)) {
			objectArray = new Object[14];
			objectArray[0] = srNo;
			objectArray[1] = transactionIdString;
			objectArray[2] = pgRefNum;
			objectArray[3] = dateFrom;
			objectArray[4] = orderId;
			objectArray[5] = paymentMethods;
			objectArray[6] = txnType;
			objectArray[7] = status;
			objectArray[8] = transactionRegion;
			objectArray[9] = amount;
			objectArray[10] = totalAmount;
			objectArray[11] = deltaRefundFlag;
			objectArray[12] = rrn;
			objectArray[13] = postSettledFlag;
		} else {
			objectArray[0] = srNo;
			objectArray[1] = transactionIdString;
			objectArray[2] = pgRefNum;
			objectArray[3] = merchants;
			objectArray[4] = acquirerType;
			objectArray[5] = dateFrom;
			objectArray[6] = orderId;
			objectArray[7] = paymentMethods;
			objectArray[8] = txnType;
			objectArray[9] = status;
			objectArray[10] = transactionRegion;
			objectArray[11] = amount;
			objectArray[12] = totalAmount;
			objectArray[13] = deltaRefundFlag;
			objectArray[14] = acqId;
			objectArray[15] = rrn;
			objectArray[16] = postSettledFlag;
		}

		return objectArray;
	}

	public Object[] myCsvMethodDownloadPaymentsReportByView() {
		Object[] objectArray = new Object[13];

		objectArray[0] = srNo;
		objectArray[1] = transactionIdString;
		objectArray[2] = pgRefNum;
		objectArray[3] = merchants;
		objectArray[4] = dateFrom;
		objectArray[5] = orderId;
		objectArray[6] = paymentMethods;
		objectArray[7] = txnType;
		objectArray[8] = status;
		objectArray[9] = transactionRegion;
		objectArray[10] = amount;
		objectArray[11] = totalAmount;
		objectArray[12] = postSettledFlag;
		return objectArray;
	}

	public Object[] myCsvMethodDownloadSummaryReport() {
		Object[] objectArray = new Object[26];
		BigDecimal divisor = new BigDecimal(2);

		objectArray[0] = srNo;
		objectArray[1] = transactionIdString;
		objectArray[2] = pgRefNum;
		objectArray[3] = paymentMethods;
		objectArray[4] = mopType;
		objectArray[5] = orderId;
		objectArray[6] = businessName;
		objectArray[7] = currency;
		objectArray[8] = txnType;
		objectArray[9] = transactionCaptureDate;
		objectArray[10] = dateFrom;
		objectArray[11] = transactionRegion;
		objectArray[12] = cardHolderType;
		objectArray[13] = acquirerType;
		objectArray[14] = totalAmount;
		objectArray[15] = acquirerSurchargeAmount;
		objectArray[16] = pgSurchargeAmount;
		objectArray[17] = paymentGatewaySurchargeAmount;
		objectArray[18] = totalGstOnAcquirer;
		objectArray[19] = totalGstOnPg;
		objectArray[20] = totalGstOnPaymentGateway;
		objectArray[21] = netMerchantPayableAmount;
		objectArray[22] = acqId;
		objectArray[23] = rrn;
		objectArray[24] = postSettledFlag;
		objectArray[25] = deltaRefundFlag;

		return objectArray;
	}
	public Object[] myCsvMethodForCapturedMerchant() {
		Object[] objectArray = new Object[23];

		objectArray[0] = srNo;
		objectArray[1] = transactionIdString;
		objectArray[2] = pgRefNum;
		objectArray[3] = merchants;
		objectArray[4] = transactionCaptureDate;
		objectArray[5] = dateFrom;
		objectArray[6] = orderId;
		objectArray[7] = rrn;
		objectArray[8] = paymentMethods;
		objectArray[9] = mopType;
		objectArray[10] = cardNumber;
		objectArray[11] = customerName;
		objectArray[12] = cardHolderType;
		objectArray[13] = txnType;
		objectArray[14] = status;
		objectArray[15] = paymentRegion;
		objectArray[16] = amount;
		objectArray[17] = totalAmount;
		objectArray[18] = tdr_Surcharge;
		objectArray[19] = gst_charge;
		objectArray[20] = totalAmtPayable;
		objectArray[21] = postSettledFlag;
		objectArray[22] = partSettle;
		

		return objectArray;
	}
	
	public Object[] myCsvMethodForCapturedSubMerchant() {
		Object[] objectArray = new Object[24];

		objectArray[0] = srNo;
		objectArray[1] = transactionIdString;
		objectArray[2] = pgRefNum;
		objectArray[3] = merchants;
		objectArray[4] = subMerchantId;
		objectArray[5] = transactionCaptureDate;
		objectArray[6] = dateFrom;
		objectArray[7] = orderId;
		objectArray[8] = rrn;
		objectArray[9] = paymentMethods;
		objectArray[10] = mopType;
		objectArray[11] = cardNumber;
		objectArray[12] = customerName;
		objectArray[13] = cardHolderType;
		objectArray[14] = txnType;
		objectArray[15] = status;
		objectArray[16] = paymentRegion;
		objectArray[17] = amount;
		objectArray[18] = totalAmount;
		objectArray[19] = tdr_Surcharge;
		objectArray[20] = gst_charge;
		objectArray[21] = totalAmtPayable;
		objectArray[22] = postSettledFlag;
		objectArray[23] = partSettle;
		

		return objectArray;
	}
	
	public Object[] myCsvMethodForFraudMerchant() {
		Object[] objectArray = new Object[11];

		objectArray[0] = merchants;
		objectArray[1] = orderId;
		objectArray[2] = pgRefNum;
		objectArray[3] = tDate;
		objectArray[4] = country;
		objectArray[5] = paymentRegion;
		objectArray[6] = paymentMethods;
		objectArray[7] = amount;
		objectArray[8] = totalAmount;
		objectArray[9] = status;
		objectArray[10] = pgTxnMessage;
	
		return objectArray;
	}
	
	public Object[] myCsvMethodForFraudSubMerchant() {
		Object[] objectArray = new Object[12];

		objectArray[0] = merchants;
		objectArray[1] = subMerchantId;
		objectArray[2] = orderId;
		objectArray[3] = pgRefNum;
		objectArray[4] = tDate;
		objectArray[5] = country;
		objectArray[6] = paymentRegion;
		objectArray[7] = paymentMethods;
		objectArray[8] = amount;
		objectArray[9] = totalAmount;
		objectArray[10] = status;
		objectArray[11] = pgTxnMessage;

		return objectArray;
	}
	
	public Object[] myCsvMethodForDashBoardAdminMerchant() {
		Object[] objectArray = new Object[23];

		objectArray[0] = transactionIdString;
		objectArray[1] = pgRefNum;
		objectArray[2] = merchants;
		objectArray[3] = acquirerType;
		objectArray[4] = dateFrom;
		objectArray[5] = orderId;
		objectArray[6] = paymentMethods;
		objectArray[7] = mopType;
		objectArray[8] = cardNumber;
		objectArray[9] = customerName;
		objectArray[10] = cardHolderType;
		objectArray[11] = txnType;
		objectArray[12] = transactionMode;
		objectArray[13] = status;
		objectArray[14] = paymentRegion;
		objectArray[15] = amount;
		objectArray[16] = totalAmount;
		objectArray[17] = tdr_Surcharge;
		objectArray[18] = gst_charge;
		objectArray[19] = totalAmtPayable;
		objectArray[20] = pgTxnMessage;
		objectArray[21] = responseMessage;
		objectArray[22] = txnSettledType;

		return objectArray;
	}
	
	public Object[] myCsvMethodForDashBoardAdminSubMerchant() {
		Object[] objectArray = new Object[24];

		objectArray[0] = transactionIdString;
		objectArray[1] = pgRefNum;
		objectArray[2] = merchants;
		objectArray[3] = subMerchantId;
		objectArray[4] = acquirerType;
		objectArray[5] = dateFrom;
		objectArray[6] = orderId;
		objectArray[7] = paymentMethods;
		objectArray[8] = mopType;
		objectArray[9] = cardNumber;
		objectArray[10] = customerName;
		objectArray[11] = cardHolderType;
		objectArray[12] = txnType;
		objectArray[13] = transactionMode;
		objectArray[14] = status;
		objectArray[15] = paymentRegion;
		objectArray[16] = amount;
		objectArray[17] = totalAmount;
		objectArray[18] = tdr_Surcharge;
		objectArray[19] = gst_charge;
		objectArray[20] = totalAmtPayable;	
		objectArray[21] = pgTxnMessage;
		objectArray[22] = responseMessage;
		objectArray[23] = txnSettledType;

		return objectArray;
	}
	
	public Object[] myCsvMethodForDashBoardResellerMerchant() {
		Object[] objectArray = new Object[22];

		objectArray[0] = transactionIdString;
		objectArray[1] = pgRefNum;
		objectArray[2] = merchants;
		objectArray[3] = dateFrom;
		objectArray[4] = orderId;
		objectArray[5] = paymentMethods;
		objectArray[6] = mopType;
		objectArray[7] = cardNumber;
		objectArray[8] = customerName;
		objectArray[9] = cardHolderType;
		objectArray[10] = txnType;
		objectArray[11] = transactionMode;
		objectArray[12] = status;
		objectArray[13] = paymentRegion;
		objectArray[14] = amount;
		objectArray[15] = totalAmount;
		objectArray[16] = tdr_Surcharge;
		objectArray[17] = gst_charge;
		objectArray[18] = totalAmtPayable;
		objectArray[19] = pgTxnMessage;
		objectArray[20] = responseMessage;
		objectArray[21] = txnSettledType;

		return objectArray;
	}
	
	public Object[] myCsvMethodForDashBoardResellerSubMerchant() {
		Object[] objectArray = new Object[23];

		objectArray[0] = transactionIdString;
		objectArray[1] = pgRefNum;
		objectArray[2] = merchants;
		objectArray[3] = subMerchantId;
		objectArray[4] = dateFrom;
		objectArray[5] = orderId;
		objectArray[6] = paymentMethods;
		objectArray[7] = mopType;
		objectArray[8] = cardNumber;
		objectArray[9] = customerName;
		objectArray[10] = cardHolderType;
		objectArray[11] = txnType;
		objectArray[12] = transactionMode;
		objectArray[13] = status;
		objectArray[14] = paymentRegion;
		objectArray[15] = amount;
		objectArray[16] = totalAmount;
		objectArray[17] = tdr_Surcharge;
		objectArray[18] = gst_charge;
		objectArray[19] = totalAmtPayable;	
		objectArray[20] = pgTxnMessage;
		objectArray[21] = responseMessage;
		objectArray[22] = txnSettledType;
		

		return objectArray;
	}
	
	public Object[] myCsvMethodForDashBoardMerchant() {
		Object[] objectArray = new Object[22];

		objectArray[0] = transactionIdString;
		objectArray[1] = pgRefNum;
		objectArray[2] = merchants;
		objectArray[3] = dateFrom;
		objectArray[4] = orderId;
		objectArray[5] = paymentMethods;
		objectArray[6] = mopType;
		objectArray[7] = cardNumber;
		objectArray[8] = customerName;
		objectArray[9] = cardHolderType;
		objectArray[10] = txnType;
		objectArray[11] = transactionMode;
		objectArray[12] = status;
		objectArray[13] = paymentRegion;
		objectArray[14] = amount;
		objectArray[15] = totalAmount;
		objectArray[16] = tdr_Surcharge;
		objectArray[17] = gst_charge;
		objectArray[18] = totalAmtPayable;		
		objectArray[19] = pgTxnMessage;
		objectArray[20] = responseMessage;
		objectArray[21] = txnSettledType;

		return objectArray;
	}
	
	public Object[] myCsvMethodForDashBoardSubMerchant() {
		Object[] objectArray = new Object[23];

		objectArray[0] = transactionIdString;
		objectArray[1] = pgRefNum;
		objectArray[2] = merchants;
		objectArray[3] = subMerchantId;
		objectArray[4] = dateFrom;
		objectArray[5] = orderId;
		objectArray[6] = paymentMethods;
		objectArray[7] = mopType;
		objectArray[8] = cardNumber;
		objectArray[9] = customerName;
		objectArray[10] = cardHolderType;
		objectArray[11] = txnType;
		objectArray[12] = transactionMode;
		objectArray[13] = status;
		objectArray[14] = paymentRegion;
		objectArray[15] = amount;
		objectArray[16] = totalAmount;
		objectArray[17] = tdr_Surcharge;
		objectArray[18] = gst_charge;
		objectArray[19] = totalAmtPayable;	
		objectArray[20] = pgTxnMessage;
		objectArray[21] = responseMessage;
		objectArray[22] = txnSettledType;

		return objectArray;
	}
	
	public Object[] myCsvMethodForP2MPayoutData() {
		Object[] objectArray = new Object[8];

		objectArray[0] = merchantName;
		objectArray[1] = transactionCaptureDate;
		objectArray[2] = rrn;
		objectArray[3] = orderId;
		objectArray[4] = payerName;
		objectArray[5] = payeeAddress;
		objectArray[6] = amount;
		objectArray[7] = status;

		return objectArray;
	}

	public Object[] csvMethodForPayOutDashBoardAdminMerchant() {
		Object[] objectArray = new Object[7];

		objectArray[0] = transactionIdString;
		objectArray[1] = merchants;
		objectArray[2] = orderId;
		objectArray[3] = status;
		objectArray[4] = amount;
		objectArray[5] = dateFrom;

		return objectArray;
	}
	
	public Object[] csvMethodForPayOutDashBoardAdminSubMerchant() {
		Object[] objectArray = new Object[8];

		objectArray[0] = transactionIdString;
		objectArray[1] = merchants;
		objectArray[2] = subMerchantId;
		objectArray[3] = orderId;
		objectArray[4] = status;
		objectArray[5] = amount;
		objectArray[6] = dateFrom;

		return objectArray;
	}
	
	public Object[] summaryPayoutReportCsv(String dataArray) {

		int arraySize = dataArray.split(";").length;
		String[] objectArrayString = dataArray.split(";");

		Object[] objectArray = new Object[arraySize];

		for (int i = 0; i < arraySize; i++) {
			objectArray[i] = objectArrayString[i];
		}

		return objectArray;
	}

	public BigInteger getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(BigInteger transactionId) {
		this.transactionId = transactionId;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getTransactionIdString() {
		return transactionIdString;
	}

	public void setTransactionIdString(String transactionIdString) {
		this.transactionIdString = transactionIdString;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
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

	public String getMerchants() {
		return merchants;
	}

	public void setMerchants(String merchants) {
		this.merchants = merchants;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(String paymentMethods) {
		this.paymentMethods = paymentMethods;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getProductDesc() {
		return productDesc;
	}

	public void setProductDesc(String productDesc) {
		this.productDesc = productDesc;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
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

	public String getRefundableAmount() {
		return refundableAmount;
	}

	public void setRefundableAmount(String refundableAmount) {
		this.refundableAmount = refundableAmount;
	}

	public String getApprovedAmount() {
		return approvedAmount;
	}

	public void setApprovedAmount(String approvedAmount) {
		this.approvedAmount = approvedAmount;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String getAcqId() {
		return acqId;
	}

	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

	public String getSubMerchantAccountNo() {
		return subMerchantAccountNo;
	}

	public void setSubMerchantAccountNo(String subMerchantAccountNo) {
		this.subMerchantAccountNo = subMerchantAccountNo;
	}

	public String getBeneficiaryBankName() {
		return BeneficiaryBankName;
	}

	public void setBeneficiaryBankName(String beneficiaryBankName) {
		BeneficiaryBankName = beneficiaryBankName;
	}

	public String getBeneficiaryIfscCode() {
		return BeneficiaryIfscCode;
	}

	public void setBeneficiaryIfscCode(String beneficiaryIfscCode) {
		BeneficiaryIfscCode = beneficiaryIfscCode;
	}

	public String getFundsReceived() {
		return fundsReceived;
	}

	public void setFundsReceived(String fundsReceived) {
		this.fundsReceived = fundsReceived;
	}

	public String getGrossTransactionAmt() {
		return grossTransactionAmt;
	}

	public void setGrossTransactionAmt(String grossTransactionAmt) {
		this.grossTransactionAmt = grossTransactionAmt;
	}

	public String getAggregatorCommissionAMT() {
		return aggregatorCommissionAMT;
	}

	public void setAggregatorCommissionAMT(String aggregatorCommissionAMT) {
		this.aggregatorCommissionAMT = aggregatorCommissionAMT;
	}

	public String getTotalAmtPayable() {
		return totalAmtPayable;
	}

	public void setTotalAmtPayable(String totalAmtPayable) {
		this.totalAmtPayable = totalAmtPayable;
	}

	public String getTotalPayoutNodalAccount() {
		return totalPayoutNodalAccount;
	}

	public void setTotalPayoutNodalAccount(String totalPayoutNodalAccount) {
		this.totalPayoutNodalAccount = totalPayoutNodalAccount;
	}

	public String gettDate() {
		return tDate;
	}

	public void settDate(String tDate) {
		this.tDate = tDate;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getNodalAccountNo() {
		return nodalAccountNo;
	}

	public void setNodalAccountNo(String nodalAccountNo) {
		this.nodalAccountNo = nodalAccountNo;
	}

	public String getAcquirerType() {
		return acquirerType;
	}

	public void setAcquirerType(String acquirerType) {
		this.acquirerType = acquirerType;
	}

	public String getNetAmountPayout() {
		return netAmountPayout;
	}

	public void setNetAmountPayout(String netAmountPayout) {
		this.netAmountPayout = netAmountPayout;
	}

	public String getPgTxnMessage() {
		return pgTxnMessage;
	}

	public void setPgTxnMessage(String pgTxnMessage) {
		this.pgTxnMessage = pgTxnMessage;
	}

	public String getTransactionRegion() {
		return transactionRegion;
	}

	public void setTransactionRegion(String transactionRegion) {
		this.transactionRegion = transactionRegion;
	}

	public String getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}

	public String getTotalGstOnMerchant() {
		return totalGstOnMerchant;
	}

	public void setTotalGstOnMerchant(String totalGstOnMerchant) {
		this.totalGstOnMerchant = totalGstOnMerchant;
	}

	public String getTotalGstOnAcquirer() {
		return totalGstOnAcquirer;
	}

	public void setTotalGstOnAcquirer(String totalGstOnAcquirer) {
		this.totalGstOnAcquirer = totalGstOnAcquirer;
	}

	public String getNetMerchantPayableAmount() {
		return netMerchantPayableAmount;
	}

	public void setNetMerchantPayableAmount(String netMerchantPayableAmount) {
		this.netMerchantPayableAmount = netMerchantPayableAmount;
	}

	public String getMerchantTdrCalculate() {
		return merchantTdrCalculate;
	}

	public void setMerchantTdrCalculate(String merchantTdrCalculate) {
		this.merchantTdrCalculate = merchantTdrCalculate;
	}

	public String getAcquirerTdrCalculate() {
		return acquirerTdrCalculate;
	}

	public void setAcquirerTdrCalculate(String acquirerTdrCalculate) {
		this.acquirerTdrCalculate = acquirerTdrCalculate;
	}

	public String getSurchargeFlag() {
		return surchargeFlag;
	}

	public void setSurchargeFlag(String surchargeFlag) {
		this.surchargeFlag = surchargeFlag;
	}

	public String getAcquirerCommissionAMT() {
		return acquirerCommissionAMT;
	}

	public void setAcquirerCommissionAMT(String acquirerCommissionAMT) {
		this.acquirerCommissionAMT = acquirerCommissionAMT;
	}

	public String getOrigTxnId() {
		return origTxnId;
	}

	public void setOrigTxnId(String origTxnId) {
		this.origTxnId = origTxnId;
	}

	public String getRefundFlag() {
		return refundFlag;
	}

	public void setRefundFlag(String refundFlag) {
		this.refundFlag = refundFlag;
	}

	public String getOrigTxnDate() {
		return origTxnDate;
	}

	public void setOrigTxnDate(String origTxnDate) {
		this.origTxnDate = origTxnDate;
	}

	public BigInteger getRefundTxnId() {
		return refundTxnId;
	}

	public void setRefundTxnId(BigInteger refundTxnId) {
		this.refundTxnId = refundTxnId;
	}

	public String getRefundDate() {
		return refundDate;
	}

	public void setRefundDate(String refundDate) {
		this.refundDate = refundDate;
	}

	public String getOrigAmount() {
		return origAmount;
	}

	public void setOrigAmount(String origAmount) {
		this.origAmount = origAmount;
	}

	public String getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(String refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(String refundStatus) {
		this.refundStatus = refundStatus;
	}

	public String getoId() {
		return oId;
	}

	public void setoId(String oId) {
		this.oId = oId;
	}

	public String getOrigTxnType() {
		return origTxnType;
	}

	public void setOrigTxnType(String origTxnType) {
		this.origTxnType = origTxnType;
	}

	public int getSrNo() {
		return srNo;
	}

	public void setSrNo(int srNo) {
		this.srNo = srNo;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public String getAcquirerSurchargeAmount() {
		return acquirerSurchargeAmount;
	}

	public void setAcquirerSurchargeAmount(String acquirerSurchargeAmount) {
		this.acquirerSurchargeAmount = acquirerSurchargeAmount;
	}

	
	public String getBankSurchargeAmount() {
		return bankSurchargeAmount;
	}

	public void setBankSurchargeAmount(String bankSurchargeAmount) {
		this.bankSurchargeAmount = bankSurchargeAmount;
	}

	public String getPgSurchargeAmount() {
		return pgSurchargeAmount;
	}

	public void setPgSurchargeAmount(String pgSurchargeAmount) {
		this.pgSurchargeAmount = pgSurchargeAmount;
	}

	public String getPaymentGatewaySurchargeAmount() {
		return paymentGatewaySurchargeAmount;
	}

	public void setPaymentGatewaySurchargeAmount(String paymentGatewaySurchargeAmount) {
		this.paymentGatewaySurchargeAmount = paymentGatewaySurchargeAmount;
	}

	public String getTotalGstOnPg() {
		return totalGstOnPg;
	}

	public void setTotalGstOnPg(String totalGstOnPg) {
		this.totalGstOnPg = totalGstOnPg;
	}

	public String getTotalGstOnPaymentGateway() {
		return totalGstOnPaymentGateway;
	}

	public void setTotalGstOnPaymentGateway(String totalGstOnPaymentGateway) {
		this.totalGstOnPaymentGateway = totalGstOnPaymentGateway;
	}

	public String getDeltaRefundFlag() {
		return deltaRefundFlag;
	}

	public void setDeltaRefundFlag(String deltaRefundFlag) {
		this.deltaRefundFlag = deltaRefundFlag;
	}

	public String getTransactionCaptureDate() {
		return transactionCaptureDate;
	}

	public void setTransactionCaptureDate(String transactionCaptureDate) {
		this.transactionCaptureDate = transactionCaptureDate;
	}

	public String getPostSettledFlag() {
		return postSettledFlag;
	}

	public void setPostSettledFlag(String postSettledFlag) {
		this.postSettledFlag = postSettledFlag;
	}

	public String getRefundOrderId() {
		return refundOrderId;
	}

	public void setRefundOrderId(String refundOrderId) {
		this.refundOrderId = refundOrderId;
	}

	public String getRefundBtnText() {
		return refundBtnText;
	}

	public void setRefundBtnText(String refundBtnText) {
		this.refundBtnText = refundBtnText;
	}

	public String getRefundedAmount() {
		return refundedAmount;
	}

	public void setRefundedAmount(String refundedAmount) {
		this.refundedAmount = refundedAmount;
	}

	public String getRefundAvailable() {
		return refundAvailable;
	}

	public void setRefundAvailable(String refundAvailable) {
		this.refundAvailable = refundAvailable;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getBtnchargebacktext() {
		return btnchargebacktext;
	}

	public void setBtnchargebacktext(String btnchargebacktext) {
		this.btnchargebacktext = btnchargebacktext;
	}

	public String getPaymentRegion() {
		return paymentRegion;
	}

	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}

	public String getTdr_Surcharge() {
		return tdr_Surcharge;
	}

	public void setTdr_Surcharge(String tdr_Surcharge) {
		this.tdr_Surcharge = tdr_Surcharge;
	}

	public String getGst_charge() {
		return gst_charge;
	}

	public void setGst_charge(String gst_charge) {
		this.gst_charge = gst_charge;
	}

	public String getAcquirerMode() {
		return acquirerMode;
	}

	public void setAcquirerMode(String acquirerMode) {
		this.acquirerMode = acquirerMode;
	}

	public String getPartSettle() {
		return partSettle;
	}

	public void setPartSettle(String partSettle) {
		this.partSettle = partSettle;
	}

	public String getDoctor() {
		return doctor;
	}

	public void setDoctor(String doctor) {
		this.doctor = doctor;
	}

	public String getGlocal() {
		return glocal;
	}

	public void setGlocal(String glocal) {
		this.glocal = glocal;
	}

	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public boolean isGlocalFlag() {
		return glocalFlag;
	}

	public void setGlocalFlag(boolean glocalFlag) {
		this.glocalFlag = glocalFlag;
	}

	public String getCustomerMobile() {
		return customerMobile;
	}

	public void setCustomerMobile(String customerMobile) {
		this.customerMobile = customerMobile;
	}

	public String getCustomFlag() {
		return customFlag;
	}

	public void setCustomFlag(String customFlag) {
		this.customFlag = customFlag;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getServiceCharge() {
		return ServiceCharge;
	}

	public void setServiceCharge(String serviceCharge) {
		ServiceCharge = serviceCharge;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}

	public String getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public String getDispatchSlipNo() {
		return dispatchSlipNo;
	}

	public void setDispatchSlipNo(String dispatchSlipNo) {
		this.dispatchSlipNo = dispatchSlipNo;
	}

	public String getCourierServiceProvider() {
		return courierServiceProvider;
	}

	public void setCourierServiceProvider(String courierServiceProvider) {
		this.courierServiceProvider = courierServiceProvider;
	}

	public boolean isDispatchSlipFlag() {
		return dispatchSlipFlag;
	}

	public void setDispatchSlipFlag(boolean dispatchSlipFlag) {
		this.dispatchSlipFlag = dispatchSlipFlag;
	}

	public boolean isPdfDownloadFlag() {
		return pdfDownloadFlag;
	}
	
	public String getResellerCharges() {
		return resellerCharges;
	}

	public void setResellerCharges(String resellerCharges) {
		this.resellerCharges = resellerCharges;
	}

	public void setPdfDownloadFlag(boolean pdfDownloadFlag) {
		this.pdfDownloadFlag = pdfDownloadFlag;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getSKUCode() {
		return SKUCode;
	}

	public void setSKUCode(String sKUCode) {
		SKUCode = sKUCode;
	}

	public String getRefundDays() {
		return refundDays;
	}

	public void setRefundDays(String refundDays) {
		this.refundDays = refundDays;
	}

	public String getMerchantAmount() {
		return merchantAmount;
	}

	public void setMerchantAmount(String merchantAmount) {
		this.merchantAmount = merchantAmount;
	}

	public String getDeliveryCode() {
		return deliveryCode;
	}

	public void setDeliveryCode(String deliveryCode) {
		this.deliveryCode = deliveryCode;
	}

	public String getSubMerchantName() {
		return subMerchantName;
	}

	public void setSubMerchantName(String subMerchantName) {
		this.subMerchantName = subMerchantName;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(String productPrice) {
		this.productPrice = productPrice;
	}

	public String getRefundCycle() {
		return refundCycle;
	}

	public void setRefundCycle(String refundCycle) {
		this.refundCycle = refundCycle;
	}

	public String getVendorID() {
		return vendorID;
	}

	public void setVendorID(String vendorID) {
		this.vendorID = vendorID;
	}

	public boolean isShowRefundButton() {
		return showRefundButton;
	}

	public void setShowRefundButton(boolean showRefundButton) {
		this.showRefundButton = showRefundButton;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getUDF11() {
		return UDF11;
	}

	public void setUDF11(String uDF11) {
		UDF11 = uDF11;
	}

	public String getUDF12() {
		return UDF12;
	}

	public void setUDF12(String uDF12) {
		UDF12 = uDF12;
	}

	public String getUDF13() {
		return UDF13;
	}

	public void setUDF13(String uDF13) {
		UDF13 = uDF13;
	}

	public String getUDF14() {
		return UDF14;
	}

	public void setUDF14(String uDF14) {
		UDF14 = uDF14;
	}

	public String getUDF15() {
		return UDF15;
	}

	public void setUDF15(String uDF15) {
		UDF15 = uDF15;
	}

	public String getUDF16() {
		return UDF16;
	}

	public void setUDF16(String uDF16) {
		UDF16 = uDF16;
	}

	public String getUDF17() {
		return UDF17;
	}

	public void setUDF17(String uDF17) {
		UDF17 = uDF17;
	}

	public String getUDF18() {
		return UDF18;
	}

	public void setUDF18(String uDF18) {
		UDF18 = uDF18;
	}

	public String getTxnSettledType() {
		return txnSettledType;
	}

	public void setTxnSettledType(String txnSettledType) {
		this.txnSettledType = txnSettledType;
	}

	public String getTransactionMode() {
		return transactionMode;
	}

	public void setTransactionMode(String transactionMode) {
		this.transactionMode = transactionMode;
	}

	public String getPayOutDate() {
		return payOutDate;
	}

	public void setPayOutDate(String payOutDate) {
		this.payOutDate = payOutDate;
	}

	public String getUtrNo() {
		return utrNo;
	}

	public void setUtrNo(String utrNo) {
		this.utrNo = utrNo;
	}
	
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getAccqResponseMessage() {
		return accqResponseMessage;
	}

	public void setAccqResponseMessage(String accqResponseMessage) {
		this.accqResponseMessage = accqResponseMessage;
	}
	
	public String getResellerGST() {
		return resellerGST;
	}

	public void setResellerGST(String resellerGST) {
		this.resellerGST = resellerGST;
	}

	public String getChargebackAmount() {
		return chargebackAmount;
	}

	public void setChargebackAmount(String chargebackAmount) {
		this.chargebackAmount = chargebackAmount;
	}

	public String getChargebackStatus() {
		return chargebackStatus;
	}

	public void setChargebackStatus(String chargebackStatus) {
		this.chargebackStatus = chargebackStatus;
	}
	public String getMerchantTdrOrSc() {
		return merchantTdrOrSc;
	}

	public void setMerchantTdrOrSc(String merchantTdrOrSc) {
		this.merchantTdrOrSc = merchantTdrOrSc;
	}

	public String getMerchantGst() {
		return merchantGst;
	}

	public void setMerchantGst(String merchantGst) {
		this.merchantGst = merchantGst;
	}

	public String getAcquirerRefNo() {
		return acquirerRefNo;
	}

	public void setAcquirerRefNo(String acquirerRefNo) {
		this.acquirerRefNo = acquirerRefNo;
	}

	public String getMerchantRefNo() {
		return merchantRefNo;
	}

	public void setMerchantRefNo(String merchantRefNo) {
		this.merchantRefNo = merchantRefNo;
	}

	public String getPayerName() {
		return payerName;
	}

	public void setPayerName(String payerName) {
		this.payerName = payerName;
	}

	public String getPayeeAddress() {
		return payeeAddress;
	}

	public void setPayeeAddress(String payeeAddress) {
		this.payeeAddress = payeeAddress;
	}

	public String getSufGst() {
		return sufGst;
	}

	public void setSufGst(String sufGst) {
		this.sufGst = sufGst;
	}

	public String getSufTdr() {
		return sufTdr;
	}

	public void setSufTdr(String sufTdr) {
		this.sufTdr = sufTdr;
	}
	
	
}
