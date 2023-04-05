package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author PG
 *
 */
public class SearchTransaction implements Serializable {

	private static final long serialVersionUID = 5899705456765089877L;

	private BigInteger transactionId;
	private String txnId;
	private String payId;
	private String merchant;
	private String txnType;
	private String paymentType;
	private String status;
	private String amount;
	private String totalAmount;
	private String orderId;
	private String mopType;
	private String pgRefNum;
	private String tDate;
	private String custName;
	private String rrn;
	private String acqId;
	private String pgResponseMessage;
	private String acquirerTxnMessage;
	private String responseCode;
	private String cardNum;
	private String cardMask;
	private String refund_txn_id;
	private String paymentGatewayResponseMessage;
	private String ACQUIRER_TDR_SC;
	private String ACQUIRER_GST;
	private String PG_GST;
	private String PG_TDR_SC;
	private String acquirerType;
	private String payment_Region;
	private String card_Holder_Type;
	private String acquirerMode;
	private String totalGst;
	private String totalTdrSc;
	private String subMerchantId;
	private String resellerCharges;
	private String resellerGst;
	private String totalChargeTdrSc;
	private String txnSettledType;
	private String srNo;
	private String consumerNo;
	private String udf10;

	
	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getTotalChargeTdrSc() {
		return totalChargeTdrSc;
	}

	public void setTotalChargeTdrSc(String totalChargeTdrSc) {
		this.totalChargeTdrSc = totalChargeTdrSc;
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
	
	public String getAcquirerMode() {
		return acquirerMode;
	}

	public void setAcquirerMode(String acquirerMode) {
		this.acquirerMode = acquirerMode;
	}

	public BigInteger getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(BigInteger transactionId) {
		this.transactionId = transactionId;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String gettDate() {
		return tDate;
	}

	public void settDate(String tDate) {
		this.tDate = tDate;
	}

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

	public String getAcqId() {
		return acqId;
	}

	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}

	public String getPgResponseMessage() {
		return pgResponseMessage;
	}

	public void setPgResponseMessage(String pgResponseMessage) {
		this.pgResponseMessage = pgResponseMessage;
	}

	public String getAcquirerTxnMessage() {
		return acquirerTxnMessage;
	}

	public void setAcquirerTxnMessage(String acquirerTxnMessage) {
		this.acquirerTxnMessage = acquirerTxnMessage;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getCardNum() {
		return cardNum;
	}

	public void setCardNum(String cardNum) {
		this.cardNum = cardNum;
	}

	public String getCardMask() {
		return cardMask;
	}

	public void setCardMask(String cardMask) {
		this.cardMask = cardMask;
	}

	public String getRefund_txn_id() {
		return refund_txn_id;
	}

	public void setRefund_txn_id(String refund_txn_id) {
		this.refund_txn_id = refund_txn_id;
	}

	public String getPaymentGatewayResponseMessage() {
		return paymentGatewayResponseMessage;
	}

	public void setPaymentGatewayResponseMessage(String paymentGatewayResponseMessage) {
		this.paymentGatewayResponseMessage = paymentGatewayResponseMessage;
	}

	public String getACQUIRER_TDR_SC() {
		return ACQUIRER_TDR_SC;
	}

	public void setACQUIRER_TDR_SC(String aCQUIRER_TDR_SC) {
		ACQUIRER_TDR_SC = aCQUIRER_TDR_SC;
	}

	public String getACQUIRER_GST() {
		return ACQUIRER_GST;
	}

	public void setACQUIRER_GST(String aCQUIRER_GST) {
		ACQUIRER_GST = aCQUIRER_GST;
	}

	public String getPG_GST() {
		return PG_GST;
	}

	public void setPG_GST(String pG_GST) {
		PG_GST = pG_GST;
	}

	public String getPG_TDR_SC() {
		return PG_TDR_SC;
	}

	public void setPG_TDR_SC(String pG_TDR_SC) {
		PG_TDR_SC = pG_TDR_SC;
	}

	public String getAcquirerType() {
		return acquirerType;
	}

	public void setAcquirerType(String acquirerType) {
		this.acquirerType = acquirerType;
	}

	public String getPayment_Region() {
		return payment_Region;
	}

	public void setPayment_Region(String payment_Region) {
		this.payment_Region = payment_Region;
	}

	public String getCard_Holder_Type() {
		return card_Holder_Type;
	}

	public void setCard_Holder_Type(String card_Holder_Type) {
		this.card_Holder_Type = card_Holder_Type;
	}

	public String getTotalGst() {
		return totalGst;
	}

	public void setTotalGst(String totalGst) {
		this.totalGst = totalGst;
	}

	public String getTotalTdrSc() {
		return totalTdrSc;
	}

	public void setTotalTdrSc(String totalTdrSc) {
		this.totalTdrSc = totalTdrSc;
	}
	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public String getTxnSettledType() {
		return txnSettledType;
	}

	public void setTxnSettledType(String txnSettledType) {
		this.txnSettledType = txnSettledType;
	}
	
	
	public String getSrNo() {
		return srNo;
	}

	public void setSrNo(String srNo) {
		this.srNo = srNo;
	}

	public Object[] downloadAgentSearchReport(User sessionUser) {
		
		  Object[] objectArray = new Object[37];
		  
		  objectArray[0] = srNo;
		  objectArray[1] = payId;
		  objectArray[2] = txnId;
		  objectArray[3] = pgRefNum;
		  objectArray[4] = merchant;
		  objectArray[5] = subMerchantId;
		  objectArray[6] = orderId;
		  objectArray[7] = refund_txn_id;
		  objectArray[8] = tDate;
		  objectArray[9] = txnType;
		  objectArray[10] = txnSettledType;
		  objectArray[11] = status;
		  objectArray[12] = acquirerType;
		  objectArray[13] = acquirerMode;
		  objectArray[14] = paymentType;
		  objectArray[15] = mopType;
		  objectArray[16] = payment_Region;
		  objectArray[17] = card_Holder_Type;
		  objectArray[18] =cardNum;
		  objectArray[19]= custName;
		   
		  objectArray[20] = amount;
		  objectArray[21] = totalAmount;
		  if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			  objectArray[22] = totalChargeTdrSc;
		  } else {
			  objectArray[22] = totalTdrSc;
		  }
		  objectArray[23] = totalGst;
		  objectArray[24] = PG_TDR_SC;
		  objectArray[25] = PG_GST;
		  objectArray[26] = ACQUIRER_TDR_SC;
		  objectArray[27] = ACQUIRER_GST;
		  objectArray[28] = resellerCharges;
		  objectArray[29] = resellerGst;
		  objectArray[30] = pgResponseMessage;
		  objectArray[31] = responseCode;
		  objectArray[32] = rrn;
		  objectArray[33] = acqId;
		  objectArray[34] = acquirerTxnMessage;
		  objectArray[35] = consumerNo;
		  objectArray[36] = udf10;
		  
		  return objectArray;
		}

	public String getConsumerNo() {
		return consumerNo;
	}

	public void setConsumerNo(String consumerNo) {
		this.consumerNo = consumerNo;
	}

	public String getUdf10() {
		return udf10;
	}

	public void setUdf10(String udf10) {
		this.udf10 = udf10;
	}


}
