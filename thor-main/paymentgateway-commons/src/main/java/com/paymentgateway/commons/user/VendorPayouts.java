package com.paymentgateway.commons.user;

import java.io.Serializable;

/**
 * @author Rajit
 */

public class VendorPayouts implements Serializable {

	private static final long serialVersionUID = -3564225046291279981L;

	public String srNo;
	
	//For Transaction Tab
	public String merchant;
	public String vendor;
	public String vendorPayId;
	public String txnType;
	public String txnId;
	public String pgRefNum;
	public String orderId;
	public String skuCode;
	public String categoryCode;
	public String dateTo;
	public String dateFrom;
	public String vendorPayoutDate;
	public String paymentMethod;
	public String paymentRegion;
	public String cardHolderType;
	public String cardMask;
	public String custName;
	public String custEmail;
	public String custMobile;
	public String paymentCycle;
	public String baseAmount;
	public String tdrSurcharge;
	public String gst;
	public String totalAmount;
	public String merchantAmount;
	public String date;
	public String vendorName;

	//For Payout Tab
	public String period;
	public String saleAmount;
	public String refundAmount;
	public String netPayout;	/*Sale Settled - Refund Settled*/
	
	public String getSaleAmount() {
		return saleAmount;
	}
	public void setSaleAmount(String saleAmount) {
		this.saleAmount = saleAmount;
	}
	public String getRefundAmount() {
		return refundAmount;
	}
	public void setRefundAmount(String refundAmount) {
		this.refundAmount = refundAmount;
	}
	public String getNetPayout() {
		return netPayout;
	}
	public void setNetPayout(String netPayout) {
		this.netPayout = netPayout;
	}
	public String getMerchant() {
		return merchant;
	}
	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public String getVendorPayId() {
		return vendorPayId;
	}
	public void setVendorPayId(String vendorPayId) {
		this.vendorPayId = vendorPayId;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
	public String getPgRefNum() {
		return pgRefNum;
	}
	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getSkuCode() {
		return skuCode;
	}
	public void setSkuCode(String skuCode) {
		this.skuCode = skuCode;
	}
	public String getCategoryCode() {
		return categoryCode;
	}
	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}
	public String getDateTo() {
		return dateTo;
	}
	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}
	public String getDateFrom() {
		return dateFrom;
	}
	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}
	public String getVendorPayoutDate() {
		return vendorPayoutDate;
	}
	public void setVendorPayoutDate(String vendorPayoutDate) {
		this.vendorPayoutDate = vendorPayoutDate;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public String getPaymentRegion() {
		return paymentRegion;
	}
	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}
	public String getCardHolderType() {
		return cardHolderType;
	}
	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}
	public String getCardMask() {
		return cardMask;
	}
	public void setCardMask(String cardMask) {
		this.cardMask = cardMask;
	}
	public String getCustName() {
		return custName;
	}
	public void setCustName(String custName) {
		this.custName = custName;
	}
	public String getCustEmail() {
		return custEmail;
	}
	public void setCustEmail(String custEmail) {
		this.custEmail = custEmail;
	}
	public String getCustMobile() {
		return custMobile;
	}
	public void setCustMobile(String custMobile) {
		this.custMobile = custMobile;
	}
	public String getPaymentCycle() {
		return paymentCycle;
	}
	public void setPaymentCycle(String paymentCycle) {
		this.paymentCycle = paymentCycle;
	}
	public String getBaseAmount() {
		return baseAmount;
	}
	public void setBaseAmount(String baseAmount) {
		this.baseAmount = baseAmount;
	}
	public String getTdrSurcharge() {
		return tdrSurcharge;
	}
	public void setTdrSurcharge(String tdrSurcharge) {
		this.tdrSurcharge = tdrSurcharge;
	}
	public String getGst() {
		return gst;
	}
	public void setGst(String gst) {
		this.gst = gst;
	}
	public String getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}
	public String getMerchantAmount() {
		return merchantAmount;
	}
	public void setMerchantAmount(String merchantAmount) {
		this.merchantAmount = merchantAmount;
	}
	public String getSrNo() {
		return srNo;
	}
	public void setSrNo(String srNo) {
		this.srNo = srNo;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}	
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public Object[] DownloadVendorPayoutTransactionReportForMerchant() {
		  Object[] objectArray = new Object[22];
		  
		  objectArray[0] = srNo;
		  objectArray[1] = vendorName;
		  objectArray[2] = vendorPayId;
		  objectArray[3] = txnType;
		  objectArray[4] = txnId;
		  objectArray[5] = pgRefNum;
		  objectArray[6] = orderId;
		  objectArray[7] = date;
		  objectArray[8] = vendorPayoutDate;
		  objectArray[9] = paymentMethod;  
		  objectArray[10] = paymentRegion;
		  objectArray[11] = cardHolderType;
		  objectArray[12] = cardMask;
		  objectArray[13] = custName;
		  objectArray[14] = custEmail;
		  objectArray[15] = custMobile;
		  objectArray[16] = paymentCycle;
		  objectArray[17] = baseAmount;
		  objectArray[18] = tdrSurcharge;
		  objectArray[19] = gst;
		  objectArray[20] = totalAmount;
		  objectArray[21] = merchantAmount;
		  
		return objectArray;
	}
	
	public Object[] DownloadVendorPayoutTransactionReport() {
		  Object[] objectArray = new Object[23];
		  
		  objectArray[0] = srNo;
		  objectArray[1] = merchant;
		  objectArray[2] = vendorName;
		  objectArray[3] = vendorPayId;
		  objectArray[4] = txnType;
		  objectArray[5] = txnId;
		  objectArray[6] = pgRefNum;
		  objectArray[7] = orderId;
		  objectArray[8] = date;
		  objectArray[9] = vendorPayoutDate;
		  objectArray[10] = paymentMethod;  
		  objectArray[11] = paymentRegion;
		  objectArray[12] = cardHolderType;
		  objectArray[13] = cardMask;
		  objectArray[14] = custName;
		  objectArray[15] = custEmail;
		  objectArray[16] = custMobile;
		  objectArray[17] = paymentCycle;
		  objectArray[18] = baseAmount;
		  objectArray[19] = tdrSurcharge;
		  objectArray[20] = gst;
		  objectArray[21] = totalAmount;
		  objectArray[22] = merchantAmount;
		  
		return objectArray;
	}
	
	public Object[] DownloadVendorPayoutReportForMerchant() {
		  Object[] objectArray = new Object[8];
		  
		  objectArray[0] = srNo;
		  objectArray[1] = vendor;
		  objectArray[2] = paymentCycle;
		  objectArray[3] = vendorPayoutDate;
		  objectArray[4] = period;
		  objectArray[5] = saleAmount;
		  objectArray[6] = refundAmount;
		  objectArray[7] = netPayout;
		  
		return objectArray;
	}
	
	public Object[] DownloadVendorPayoutReport() {
		  Object[] objectArray = new Object[9];
		  
		  objectArray[0] = srNo;
		  objectArray[1] = merchant;
		  objectArray[2] = vendor;
		  objectArray[3] = paymentCycle;
		  objectArray[4] = vendorPayoutDate;
		  objectArray[5] = period;
		  objectArray[6] = saleAmount;
		  objectArray[7] = refundAmount;
		  objectArray[8] = netPayout;
		  
		return objectArray;
	}
}
