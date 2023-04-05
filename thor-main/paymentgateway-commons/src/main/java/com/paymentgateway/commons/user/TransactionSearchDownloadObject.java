package com.paymentgateway.commons.user;

import com.paymentgateway.commons.util.CardsType;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.TransactionType;

public class TransactionSearchDownloadObject {

	private String payId;
	private String transactionId;
	private String pgRefNum;
	private String transactionRegion;
	private String merchants;
	private String postSettledFlag;
	private String deltaRefundFlag;
	private String txnType;
	private String txnSettledType;
	private String transactionMode;
	private String acquirerType;
	private String paymentMethods;
	private String status;
	private String dateFrom;
	private String amount;
	private String orderId;
	private String totalAmount;
	private String acqId;
	private String rrn;
	private String srNo;
	private String refundOrderId;
	private String tdrOrSurcharge;
	private String gst;
	private String mopType;
	private String CardHolderType;
	private String acquirerMode;
	private String subMerchantId;
	private String acquirerTdrOrSurcharge;
	private String acquirerGST;
	private String pgTdrOrSurcharge;
	private String pgGST;
	private String currency;
	private String feeExclusiveTax;
	private String tax;
	private String refundAmount; //amount
	private String debitAmount; //amount
	private String creditAmount; //amount
	private String cardType;
	private String issuerName;
	private String paymentNotes; //CUST_NAME
	private String refundNotes;
	private String entityDiscription; // default value (Order Payment)
	private String customerMobile;
	private String desputeId;
	private String desputeCreatedAt;
	private String desputeReason;
	private String settlementId;
	private String settlementDateAt;
	private String utrNo;
	private String settlementBy;
	// for response message
	private String responseMessage;
	private String accqResponseMessage;
	private String cardNumber;
	private String UDF11;
	private String UDF12;
	private String UDF13;
	private String UDF14;
	private String UDF15;
	private String UDF16;
	private String UDF17;
	private String UDF18;
	private String resellerCharges;
	private String resellerGst;
	private String merchantTdrOrSc;
	private String merchantGst;
	
	private String sufTdr;
	private String sufGst;
	private String consumerNo;
	
	
	public String getSufTdr() {
		return sufTdr;
	}
	public void setSufTdr(String sufTdr) {
		this.sufTdr = sufTdr;
	}
	public String getSufGst() {
		return sufGst;
	}
	public void setSufGst(String sufGst) {
		this.sufGst = sufGst;
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
	
	public String getResellerCharges() {
		return resellerCharges;
	}
	public void setResellerCharges(String resellerCharges) {
		this.resellerCharges = resellerCharges;
	}
	public String getResellerGst() {
		return resellerGst;
	}
	public void setResellerGst(String resellerGst) {
		this.resellerGst = resellerGst;
	}
	
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	public String getRefundAmount() {
		return refundAmount;
	}
	public void setRefundAmount(String refundAmount) {
		this.refundAmount = refundAmount;
	}
	public String getDebitAmount() {
		return debitAmount;
	}
	public void setDebitAmount(String debitAmount) {
		this.debitAmount = debitAmount;
	}
	public String getCreditAmount() {
		return creditAmount;
	}
	public void setCreditAmount(String creditAmount) {
		this.creditAmount = creditAmount;
	}
	public String getIssuerName() {
		return issuerName;
	}
	public void setIssuerName(String issuerName) {
		this.issuerName = issuerName;
	}
	public String getPaymentNotes() {
		return paymentNotes;
	}
	public void setPaymentNotes(String paymentNotes) {
		this.paymentNotes = paymentNotes;
	}
	public String getRefundNotes() {
		return refundNotes;
	}
	public void setRefundNotes(String refundNotes) {
		this.refundNotes = refundNotes;
	}
	public String getEntityDiscription() {
		return entityDiscription;
	}
	public void setEntityDiscription(String entityDiscription) {
		this.entityDiscription = entityDiscription;
	}
	
	public String getCustomerMobile() {
		return customerMobile;
	}
	public void setCustomerMobile(String customerMobile) {
		this.customerMobile = customerMobile;
	}
	public String getDesputeId() {
		return desputeId;
	}
	public void setDesputeId(String desputeId) {
		this.desputeId = desputeId;
	}
	public String getDesputeCreatedAt() {
		return desputeCreatedAt;
	}
	public void setDesputeCreatedAt(String desputeCreatedAt) {
		this.desputeCreatedAt = desputeCreatedAt;
	}
	public String getDesputeReason() {
		return desputeReason;
	}
	public void setDesputeReason(String desputeReason) {
		this.desputeReason = desputeReason;
	}
	public String getSettlementId() {
		return settlementId;
	}
	public void setSettlementId(String settlementId) {
		this.settlementId = settlementId;
	}
	
	public String getSettlementDateAt() {
		return settlementDateAt;
	}
	public void setSettlementDateAt(String settlementDateAt) {
		this.settlementDateAt = settlementDateAt;
	}
	public String getUtrNo() {
		return utrNo;
	}
	public void setUtrNo(String utrNo) {
		this.utrNo = utrNo;
	}
	public String getSettlementBy() {
		return settlementBy;
	}
	public void setSettlementBy(String settlementBy) {
		this.settlementBy = settlementBy;
	}
	public String getFeeExclusiveTax() {
		return feeExclusiveTax;
	}
	public void setFeeExclusiveTax(String feeExclusiveTax) {
		this.feeExclusiveTax = feeExclusiveTax;
	}
	public String getTax() {
		return tax;
	}
	public void setTax(String tax) {
		this.tax = tax;
	}
	public String getAccqResponseMessage() {
		return accqResponseMessage;
	}
	public void setAccqResponseMessage(String accqResponseMessage) {
		this.accqResponseMessage = accqResponseMessage;
	}
	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	public String getSubMerchantId() {
		return subMerchantId;
	}
	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}
	public String getRefundOrderId() {
		return refundOrderId;
	}
	public void setRefundOrderId(String refundOrderId) {
		this.refundOrderId = refundOrderId;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getPgRefNum() {
		return pgRefNum;
	}
	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}
	public String getTransactionRegion() {
		return transactionRegion;
	}
	public void setTransactionRegion(String transactionRegion) {
		this.transactionRegion = transactionRegion;
	}
	public String getMerchants() {
		return merchants;
	}
	public void setMerchants(String merchants) {
		this.merchants = merchants;
	}
	public String getPostSettledFlag() {
		return postSettledFlag;
	}
	public void setPostSettledFlag(String postSettledFlag) {
		this.postSettledFlag = postSettledFlag;
	}
	public String getDeltaRefundFlag() {
		return deltaRefundFlag;
	}
	public void setDeltaRefundFlag(String deltaRefundFlag) {
		this.deltaRefundFlag = deltaRefundFlag;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public String getAcquirerType() {
		return acquirerType;
	}
	public void setAcquirerType(String acquirerType) {
		this.acquirerType = acquirerType;
	}
	public String getPaymentMethods() {
		return paymentMethods;
	}
	public void setPaymentMethods(String paymentMethods) {
		this.paymentMethods = paymentMethods;
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
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
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
	public String getSrNo() {
		return srNo;
	}
	public void setSrNo(String srNo) {
		this.srNo = srNo;
	}
	
	public String getTdrOrSurcharge() {
		return tdrOrSurcharge;
	}
	public void setTdrOrSurcharge(String tdrOrSurcharge) {
		this.tdrOrSurcharge = tdrOrSurcharge;
	}
	public String getGst() {
		return gst;
	}
	public void setGst(String gst) {
		this.gst = gst;
		
	}
	public String getMopType() {
		return mopType;
	}
	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
	
	public String getCardHolderType() {
		return CardHolderType;
	}
	public void setCardHolderType(String cardHolderType) {
		CardHolderType = cardHolderType;
	}
	public String getAcquirerMode() {
		return acquirerMode;
	}
	public void setAcquirerMode(String acquirerMode) {
		this.acquirerMode = acquirerMode;
	}
	public String getAcquirerTdrOrSurcharge() {
		return acquirerTdrOrSurcharge;
	}
	public void setAcquirerTdrOrSurcharge(String acquirerTdrOrSurcharge) {
		this.acquirerTdrOrSurcharge = acquirerTdrOrSurcharge;
	}
	public String getAcquirerGST() {
		return acquirerGST;
	}
	public void setAcquirerGST(String acquirerGST) {
		this.acquirerGST = acquirerGST;
	}
	public String getPgTdrOrSurcharge() {
		return pgTdrOrSurcharge;
	}
	public void setPgTdrOrSurcharge(String pgTdrOrSurcharge) {
		this.pgTdrOrSurcharge = pgTdrOrSurcharge;
	}
	public String getPgGST() {
		return pgGST;
	}
	public void setPgGST(String pgGST) {
		this.pgGST = pgGST;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
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
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getConsumerNo() {
		return consumerNo;
	}
	public void setConsumerNo(String consumerNo) {
		this.consumerNo = consumerNo;
	}
	public Object[] myCsvMethodDownloadPaymentsReport(User sessionUser) {
		
		int n=2;  
		Object[] objectArray = new Object[32+n];
		  
		 
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = merchants;
		 // objectArray[4] = acquirerType;
		 // objectArray[4] = acquirerMode;
		  objectArray[4] = dateFrom;
		  objectArray[5] = orderId;
		  objectArray[6] = paymentMethods;
		  objectArray[7] = mopType;
		  objectArray[8] = CardHolderType;
		  objectArray[9] = cardNumber;
		  objectArray[10] = txnType;
		  objectArray[11] = transactionMode;
		  objectArray[12] = status;
		  objectArray[13] = transactionRegion;
		  objectArray[14] = amount;
		  objectArray[15] = tdrOrSurcharge;
		  objectArray[16] = gst;
		  //add by vishal
		  objectArray[17] = sufTdr;
		  objectArray[18] = sufGst;
		  
		  objectArray[17+n] = totalAmount;
	//	  objectArray[17] = deltaRefundFlag;
		  objectArray[18+n] = acqId;
		  objectArray[19+n] = rrn;
		  objectArray[20+n] = txnSettledType;
		  objectArray[21+n] = refundOrderId;
		  objectArray[22+n] = responseMessage;
		  objectArray[23+n] = accqResponseMessage;
		  
		 	  objectArray[24+n] = UDF11;
			  objectArray[25+n] = UDF12;
			  objectArray[26+n] = UDF13;
			  objectArray[27+n] = UDF14;
			  objectArray[28+n] = UDF15;
			  objectArray[29+n] = UDF16;
			  objectArray[30+n] = UDF17;
			  objectArray[31+n] = UDF18;
		  return objectArray;
		  
		}
	
	public Object[] myCsvMethodDownloadPaymentsReportForSubMerchant(User sessionUser) {
		int n=2; 
		Object[] objectArray = new Object[33+n];
		  
		 
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = merchants;
		  objectArray[4] = subMerchantId;
		 // objectArray[4] = acquirerType;
		 // objectArray[4] = acquirerMode;
		  objectArray[5] = dateFrom;
		  objectArray[6] = orderId;
		  objectArray[7] = paymentMethods;
		  objectArray[8] = mopType;
		  objectArray[9] = CardHolderType;
		  objectArray[10] = cardNumber;
		  objectArray[11] = txnType;
		  objectArray[12] = transactionMode;
		  objectArray[13] = status;
		  objectArray[14] = transactionRegion;
		  objectArray[15] = amount;
		  objectArray[16] = tdrOrSurcharge;
		  objectArray[17] = gst;
		   // add by visha l
		  objectArray[18] = sufTdr;
		  objectArray[19] = sufGst;
		  objectArray[18+n] = totalAmount;
	//	  objectArray[18] = deltaRefundFlag;
		  objectArray[19+n] = acqId;
		  objectArray[20+n] = rrn;
		  objectArray[21+n] = txnSettledType;
		  objectArray[22+n] = refundOrderId;
		  objectArray[23+n] = responseMessage;
		  objectArray[24+n] = accqResponseMessage;
		  
		  
			  objectArray[25+n] = UDF11;
			  objectArray[26+n] = UDF12;
			  objectArray[27+n] = UDF13;
			  objectArray[28+n] = UDF14;
			  objectArray[29+n] = UDF15;
			  objectArray[30+n] = UDF16;
			  objectArray[31+n] = UDF17;
			  objectArray[32+n] = UDF18;
		  
		  return objectArray;
		  
		}
	
	public Object[] myCsvMethodDownloadPaymentsReportForAdminAndSubMerchant() {
		  int n =2;
		  Object[] objectArray = new Object[43+n];
		 
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = merchants;
		  objectArray[4] = subMerchantId;
		  objectArray[5] = payId;
		  objectArray[6] = acquirerType;
		  objectArray[7] = dateFrom;
		  objectArray[8] = orderId;
		  objectArray[9] = paymentMethods;
		  objectArray[10] = mopType;
		  objectArray[11] = CardHolderType;
		  objectArray[12] = cardNumber;
		  objectArray[13] = txnType;
		  objectArray[14] = transactionMode;
		  objectArray[15] = status;
		  objectArray[16] = transactionRegion;
		  objectArray[17] = amount;
		  objectArray[18] = merchantTdrOrSc;
		  objectArray[19] = merchantGst;
		  //add by vishal
		  objectArray[20] =sufTdr;
		  objectArray[21]= sufGst;
		 
		  objectArray[20+n] = acquirerTdrOrSurcharge;
		  objectArray[21+n] = acquirerGST;
		  objectArray[22+n] = pgTdrOrSurcharge;
		  objectArray[23+n] = pgGST;
		  objectArray[24+n] = resellerCharges;
		  objectArray[25+n] = resellerGst;
		  objectArray[26+n] = totalAmount;
		  objectArray[27+n] = acqId;
		  objectArray[28+n] = rrn;
		  objectArray[29+n] = txnSettledType;
		  objectArray[30+n] = refundOrderId;
		  objectArray[31+n] = responseMessage;
		  objectArray[32+n] = accqResponseMessage;
		  objectArray[33+n] = UDF11;
		  objectArray[34+n] = UDF12;
		  objectArray[35+n] = UDF13;
		  objectArray[36+n] = UDF14;
		  objectArray[37+n] = UDF15;
		  objectArray[38+n] = UDF16;
		  objectArray[39+n] = UDF17;
		  objectArray[40+n] = UDF18;
		  objectArray[41+n] = payId;
		  objectArray[42+n] = consumerNo;
		  
		  return objectArray;
		}
	
	public Object[] myCsvMethodDownloadPaymentsReportForAdmin() {
		int n = 2;
		  Object[] objectArray = new Object[40+n];
		  
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = merchants;
		  objectArray[4] = payId;
		  objectArray[5] = acquirerType;
		  objectArray[6] = dateFrom;
		  objectArray[7] = orderId;
		  objectArray[8] = paymentMethods;
		  objectArray[9] = mopType;
		  objectArray[10] = CardHolderType;
		  objectArray[11] = cardNumber;
		  objectArray[12] = txnType;
		  objectArray[13] = transactionMode;
		  objectArray[14] = status;
		  objectArray[15] = transactionRegion;
		  objectArray[16] = amount;
		  objectArray[17] = merchantTdrOrSc;
		  objectArray[18] = merchantGst;
		  //add by vishal
		  objectArray[19] =sufTdr;
		  objectArray[20]= sufGst;
		   
		  objectArray[19+n] = acquirerTdrOrSurcharge;
		  objectArray[20+n] = acquirerGST;
		  objectArray[21+n] = pgTdrOrSurcharge;
		  objectArray[22+n] = pgGST;
		  objectArray[23+n] = resellerCharges;
		  objectArray[24+n] = resellerGst;
		  objectArray[25+n] = totalAmount;
		  objectArray[26+n] = acqId;
		  objectArray[27+n] = rrn;
		  objectArray[28+n] = txnSettledType;
		  objectArray[29+n] = refundOrderId;
		  objectArray[30+n] = responseMessage;
		  objectArray[31+n] = accqResponseMessage;
		  objectArray[32+n] = UDF11;
		  objectArray[33+n] = UDF12;
		  objectArray[34+n] = UDF13;
		  objectArray[35+n] = UDF14;
		  objectArray[36+n] = UDF15;
		  objectArray[37+n] = UDF16;
		  objectArray[38+n] = UDF17;
		  objectArray[39+n] = UDF18;
		  
		  return objectArray;
		}
	
	public Object[] myCsvMethodDownloadCustomSettledReportsForKhadi(User sessionUser) {
		Object[] objectArray = new Object[30];

		objectArray[0] = txnType;
		objectArray[1] = pgRefNum;
		objectArray[2] = totalAmount;
		objectArray[3] = currency;
		objectArray[4] = feeExclusiveTax;
		objectArray[5] = tax;
		objectArray[6] = debitAmount;
		objectArray[7] = creditAmount;
		objectArray[8] = paymentMethods;
		objectArray[9] = cardType;
		objectArray[10] = issuerName;
		objectArray[11] = dateFrom;
		objectArray[12] = dateFrom;
		objectArray[13] = paymentNotes;
		objectArray[14] = refundNotes;
		objectArray[15] = rrn;
		objectArray[16] = entityDiscription;
		objectArray[17] = orderId;
		objectArray[18] = orderId;
		objectArray[19] = customerMobile;
		objectArray[20] = desputeId;
		objectArray[21] = desputeCreatedAt;
		objectArray[22] = desputeReason;
		objectArray[23] = settlementId;
		objectArray[24] = settlementDateAt;
		objectArray[25] = utrNo;
		objectArray[26] = settlementBy;

		return objectArray;

	}
	
	
	public Object[] myCsvMethodDownloadMprPaymentsReport(String Currentdate) {
		Object[] objectArray = new Object[35];

		objectArray[0] = "-";
		objectArray[1] = "-";
		if(txnType.equalsIgnoreCase(TransactionType.SALE.getName())) {
			objectArray[2] = "BAT";
			objectArray[3] = "15";
		} else if(txnType.equalsIgnoreCase(TransactionType.REFUND.getName())) {
			objectArray[2] = "CVD";
			objectArray[3] = "999";
		} else {
			objectArray[2] = "-";
			objectArray[3] = "-";				
		}			
		
		objectArray[4] = paymentMethods;			

		if(paymentMethods.equalsIgnoreCase(CardsType.CREDIT_CARD.getName()) 
				|| paymentMethods.equalsIgnoreCase(CardsType.DEBIT_CARD.getName())) {
			objectArray[5] = cardNumber;
		} else {
			objectArray[5] = "-";
		}
		
		objectArray[6] = DateCreater.commonDateFormat(dateFrom);			
		objectArray[7] = Currentdate;			
		objectArray[8] = "-";
		objectArray[9] = "-";
		objectArray[10] = totalAmount;
		objectArray[11] = acqId;
		objectArray[12] = "-";			
		objectArray[13] = pgRefNum;			
		objectArray[14] = "0";
		objectArray[15] = "0";
		objectArray[16] = "0";
		objectArray[17] = "0";
		objectArray[18] = "0";
		objectArray[19] = "0";
		objectArray[20] = "0";
		objectArray[21] = "0";
		
		objectArray[22] = acquirerTdrOrSurcharge;
		objectArray[23] = acquirerGST;
		objectArray[24] = String.valueOf(Double.parseDouble(totalAmount)-Double.parseDouble(acquirerTdrOrSurcharge)-Double.parseDouble(acquirerGST));
		
		objectArray[25] = "-";
		objectArray[26] = "-";
		objectArray[27] = "-";
		objectArray[28] = "-";
		objectArray[29] = "-";
		objectArray[30] = "-";
		objectArray[31] = "-";
		objectArray[32] = rrn;
		objectArray[33] = "-";
		objectArray[34] = "-";

		return objectArray;
	}
}

