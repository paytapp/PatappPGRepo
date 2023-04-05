package com.paymentgateway.commons.user;

import java.io.Serializable;

public class ECollectionObject implements Serializable {

	private static final long serialVersionUID = -4691009307357010956L;
	
	private String merchant;
	private String subMerchant;
	private String payId;
	private String merchantVirtualAccountNumber;
	private String transactionDate;
	private String paymentGatewayCode;
	private String paymentGatewayAccountNumber;
	private String paymentMode;
	private String status;
	private String payeeName;
	private String payeeAccountNumber;
	private String payeeBankIFSC;
	private String bankTxnNumber;
	private String amount;
	private String senderRemark;
	private String txnType;
	private String reseller;
	private String virtualAccountFlag;
	private String percentageCharges;
	private String fixedCharges;
	private String pgGst;
	private String totalAmount;
	private String totalPaCommission;

	
	public String getVirtualAccountFlag() {
		return virtualAccountFlag;
	}
	public void setVirtualAccountFlag(String virtualAccountFlag) {
		this.virtualAccountFlag = virtualAccountFlag;
	}
	public String getMerchant() {
		return merchant;
	}
	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}
	public String getSubMerchant() {
		return subMerchant;
	}
	public void setSubMerchant(String subMerchant) {
		this.subMerchant = subMerchant;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getMerchantVirtualAccountNumber() {
		return merchantVirtualAccountNumber;
	}
	public void setMerchantVirtualAccountNumber(String merchantVirtualAccountNumber) {
		this.merchantVirtualAccountNumber = merchantVirtualAccountNumber;
	}
	public String getTransactionDate() {
		return transactionDate;
	}
	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}
	public String getpaymentGatewayCode() {
		return paymentGatewayCode;
	}
	public void setpaymentGatewayCode(String paymentGatewayCode) {
		this.paymentGatewayCode = paymentGatewayCode;
	}
	public String getpaymentGatewayAccountNumber() {
		return paymentGatewayAccountNumber;
	}
	public void setpaymentGatewayAccountNumber(String paymentGatewayAccountNumber) {
		this.paymentGatewayAccountNumber = paymentGatewayAccountNumber;
	}
	public String getPaymentMode() {
		return paymentMode;
	}
	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}
	public String getPayeeName() {
		return payeeName;
	}
	public void setPayeeName(String payeeName) {
		this.payeeName = payeeName;
	}
	public String getPayeeAccountNumber() {
		return payeeAccountNumber;
	}
	public void setPayeeAccountNumber(String payeeAccountNumber) {
		this.payeeAccountNumber = payeeAccountNumber;
	}
	public String getPayeeBankIFSC() {
		return payeeBankIFSC;
	}
	public void setPayeeBankIFSC(String payeeBankIFSC) {
		this.payeeBankIFSC = payeeBankIFSC;
	}
	public String getBankTxnNumber() {
		return bankTxnNumber;
	}
	public void setBankTxnNumber(String bankTxnNumber) {
		this.bankTxnNumber = bankTxnNumber;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getSenderRemark() {
		return senderRemark;
	}
	public void setSenderRemark(String senderRemark) {
		this.senderRemark = senderRemark;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public String getReseller() {
		return reseller;
	}
	public void setReseller(String reseller) {
		this.reseller = reseller;
	}
	public String getPercentageCharges() {
		return percentageCharges;
	}
	public String getFixedCharges() {
		return fixedCharges;
	}
	public String getPgGst() {
		return pgGst;
	}
	public void setPercentageCharges(String percentageCharges) {
		this.percentageCharges = percentageCharges;
	}
	public void setFixedCharges(String fixedCharges) {
		this.fixedCharges = fixedCharges;
	}
	public void setPgGst(String pgGst) {
		this.pgGst = pgGst;
	}
	public String getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}
	
	
	

	public String getTotalPaCommission() {
		return totalPaCommission;
	}
	public void setTotalPaCommission(String totalPaCommission) {
		this.totalPaCommission = totalPaCommission;
	}
	public Object[] myCsvMethodDownloadECollectionReportByAdminView() {
		  Object[] objectArray = new Object[17];
		  
		  objectArray[0] = merchant;
		  objectArray[1] = payId;
		  objectArray[2] = merchantVirtualAccountNumber;
		  objectArray[3] = transactionDate;
		  objectArray[4] = paymentGatewayCode;
		  objectArray[5] = paymentGatewayAccountNumber;
		  objectArray[6] = paymentMode;
		  objectArray[7] = txnType;
		  objectArray[8] = payeeName;
		  objectArray[9] = payeeAccountNumber;
		  objectArray[10] = payeeBankIFSC;
		  objectArray[11] = bankTxnNumber;
		  objectArray[12] = totalPaCommission;
		  objectArray[13] = amount;
		  objectArray[14] = totalAmount;
		  objectArray[15] = status;
		  objectArray[16] = senderRemark;
		  
		  
		  return objectArray;
		}
	
	
	public Object[] myCsvMethodDownloadECollectionReportByAdminViewSubMerchant() {
		  Object[] objectArray = new Object[18];
		  
		  objectArray[0] = merchant;
		  objectArray[1] = subMerchant;
		  objectArray[2] = payId;
		  objectArray[3] = merchantVirtualAccountNumber;
		  objectArray[4] = transactionDate;
		  objectArray[5] = paymentGatewayCode;
		  objectArray[6] = paymentGatewayAccountNumber;
		  objectArray[7] = paymentMode;
		  objectArray[8] = txnType;
		  objectArray[9] = payeeName;
		  objectArray[10] = payeeAccountNumber;
		  objectArray[11] = payeeBankIFSC;
		  objectArray[12] = bankTxnNumber;
		  objectArray[13] = totalPaCommission;
		  objectArray[14] = amount;
		  objectArray[15] = totalAmount;
		  objectArray[16] = status;
		  objectArray[17] = senderRemark;
		  return objectArray;
		}
	
	public Object[] myCsvMethodDownloadECollectionReportByMerchantView() {
		  Object[] objectArray = new Object[15];
		  
		 
		  
		  objectArray[0] = merchant;
		  objectArray[1] = payId;
		  objectArray[2] = merchantVirtualAccountNumber;
		  objectArray[3] = transactionDate;
		  objectArray[4] = paymentMode;
		  objectArray[5] = txnType;
		  objectArray[6] = payeeName;
		  objectArray[7] = payeeAccountNumber;
		  objectArray[8] = payeeBankIFSC;
		  objectArray[9] = bankTxnNumber;
		  objectArray[10] = totalPaCommission;
		  objectArray[11] = amount;
		  objectArray[12] = totalAmount;
		  objectArray[13] = status;
		  objectArray[14] = senderRemark;
		  return objectArray;
		}
	
	public Object[] myCsvMethodDownloadECollectionReportBySubMerchantView() {
		  Object[] objectArray = new Object[16];
		  
		 
		  
		  objectArray[0] = merchant;
		  objectArray[1] = subMerchant;
		  objectArray[2] = payId;
		  objectArray[3] = merchantVirtualAccountNumber;
		  objectArray[4] = transactionDate;
		  objectArray[5] = paymentMode;
		  objectArray[6] = txnType;
		  objectArray[7] = payeeName;
		  objectArray[8] = payeeAccountNumber;
		  objectArray[9] = payeeBankIFSC;
		  objectArray[10] = bankTxnNumber;
		  objectArray[11] = totalPaCommission;
		  objectArray[12] = amount;
		  objectArray[13] = totalAmount;
		  objectArray[14] = status;
		  objectArray[15] = senderRemark;
		  return objectArray;
		}
	
	public Object[] myCsvMethodDownloadVAListByAdminView(boolean resellerFlag, boolean superMerchant) {
		  Object[] objectArray = new Object[5];
		  if(resellerFlag && superMerchant) {
			  objectArray[0] = reseller;
			  objectArray[1] = merchant;
			  objectArray[2] = subMerchant;
			  objectArray[3] = merchantVirtualAccountNumber;
			  objectArray[4] = virtualAccountFlag;
		  } else if(resellerFlag){
			  objectArray[0] = reseller;
			  objectArray[1] = merchant;
			  objectArray[2] = merchantVirtualAccountNumber;
			  objectArray[3] = virtualAccountFlag;
		  } else if(superMerchant){
			  objectArray[0] = merchant;
			  objectArray[1] = subMerchant;
			  objectArray[2] = merchantVirtualAccountNumber;
			  objectArray[3] = virtualAccountFlag;
		  } else {
			  objectArray[0] = merchant;
			  objectArray[1] = merchantVirtualAccountNumber;
			  objectArray[2] = virtualAccountFlag;
		  }
		  
		  return objectArray;
		}

	
	
}
