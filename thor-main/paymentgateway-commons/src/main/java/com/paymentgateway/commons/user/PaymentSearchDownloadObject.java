package com.paymentgateway.commons.user;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentSearchDownloadObject {


	private String transactionId;
	private String acqId;
	private String pgRefNum;
	private User user;
	private String transactionRegion;
	private String merchants;
	private String postSettledFlag;
	private String txnType;
	private String acquirerType;
	private String paymentMethods;
	private String status;
	private String dateFrom;
	private String amount;
	private String orderId;
	private String totalAmount;
	private String srNo;
	private String tdrOrSurcharge;
	private String gst; 
	private String merchantAmount;
	private String moptype;
	private String cardHolderType;
	private String acquirerMode;
	private String cardMask;
	private String custName;
	private String settledDate;
	private String partSettle;
	private String doctor;
	private String glocal;
	private String partner;
	private String uniqueId;
	private String custMobile;
	private String custEmail;
	private String customFlag;
	private String rrn;
	private String subMerchantId;
	private String deliveryStatus;
	private String invoiceNo;
	private String dispatchSlipNo;
	private String courierServiceProvider;
	private String categoryCode;
	private String SKUCode;
	private String refundCycle;
	private String productPrice;
	private String vendorID;
	private String resellerCharges;
	private String txnSettledType;
	private String transactionMode;
	private String payOutDate;
	private String refundOrderId;
	
	private String utrNo;
	private String UDF11;
	private String UDF12;
	private String UDF13;
	private String UDF14;
	private String UDF15;
	private String UDF16;
	private String UDF17;
	private String UDF18;
	private String resellerGST;
	
	private String sufTdr;
	private String sufGst;
	private String refund_flag;
	
	
	public String getSufTdr() {
		return sufTdr;
	}
	public void setSufTdr(String sufTdr) {
		this.sufTdr = sufTdr;
	}
	public String getAcqId() {
		return acqId;
	}
	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}
	public String getSufGst() {
		return sufGst;
	}
	public void setSufGst(String sufGst) {
		this.sufGst = sufGst;
	}
	public String getRefundOrderId() {
		return refundOrderId;
	}
	public void setRefundOrderId(String refundOrderId) {
		this.refundOrderId = refundOrderId;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getRefundCycle() {
		return refundCycle;
	}
	public void setRefundCycle(String refundCycle) {
		this.refundCycle = refundCycle;
	}
	public String getProductPrice() {
		return productPrice;
	}
	public void setProductPrice(String productPrice) {
		this.productPrice = productPrice;
	}
	public String getVendorID() {
		return vendorID;
	}
	public void setVendorID(String vendorID) {
		this.vendorID = vendorID;
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
	
	public String getDeliveryStatus() {
		return deliveryStatus;
	}
	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}
	public String getSubMerchantId() {
		return subMerchantId;
	}
	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}
	public String getRrn() {
		return rrn;
	}
	public void setRrn(String rrn) {
		this.rrn = rrn;
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
	
	public String getCustomFlag() {
		return customFlag;
	}
	public void setCustomFlag(String customFlag) {
		this.customFlag = customFlag;
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
	public String getMerchantAmount() {
		return merchantAmount;
	}
	public void setMerchantAmount(String merchantAmount) {
		this.merchantAmount = merchantAmount;
	}
	
	public String getMoptype() {
		return moptype;
	}
	public void setMoptype(String moptype) {
		this.moptype = moptype;
	}
	public String getCardHolderType() {
		return cardHolderType;
	}
	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}
	public String getAcquirerMode() {
		return acquirerMode;
	}
	public void setAcquirerMode(String acquirerMode) {
		this.acquirerMode = acquirerMode;
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
	public String getSettledDate() {
		return settledDate;
	}
	public void setSettledDate(String settledDate) {
		this.settledDate = settledDate;
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
	public String getResellerCharges() {
		return resellerCharges;
	}
	public void setResellerCharges(String resellerCharges) {
		this.resellerCharges = resellerCharges;
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
	public String getResellerGST() {
		return resellerGST;
	}
	public void setResellerGST(String resellerGST) {
		this.resellerGST = resellerGST;
	}

	
	/**
	 * @return the refund_flag
	 */
	public String getRefund_flag() {
		return refund_flag;
	}
	/**
	 * @param refund_flag the refund_flag to set
	 */
	public void setRefund_flag(String refund_flag) {
		this.refund_flag = refund_flag;
	}
	@SuppressWarnings("unchecked")
	public Object[] myCsvMethodDownloadPaymentsReportByView(String merchantPayId, User sessionUser, User merchant, List<Merchants> resellerMerchants) {
		int n =2;  
		Object[] objectArray = new Object[43+n];
		  
		 
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = merchants;
		  objectArray[4] = dateFrom;
		  objectArray[5] = settledDate;
		  objectArray[6] = payOutDate;
		  objectArray[7] = utrNo;
		  objectArray[8] = orderId;
		  objectArray[9] = rrn;
		  objectArray[10] = paymentMethods;
		  objectArray[11] = moptype;
		  objectArray[12] = cardMask;
		  objectArray[13] = custName;
		  objectArray[14] = custEmail;
		  objectArray[15] = cardHolderType;  
		  objectArray[16] = txnType;
		  objectArray[17] = transactionMode;
		  objectArray[18] = status;
		  objectArray[19] = transactionRegion;
		  objectArray[20] = amount;
		  objectArray[21] = totalAmount;
		  
		  if(sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
			  objectArray[22] = tdrOrSurcharge;
			  objectArray[23] = gst;
			// add 2 columns by vishal 24 and 25 
			  objectArray[24] = sufTdr;
			  objectArray[25] = sufGst;
			  
			  objectArray[24+n] = resellerCharges;  
			  objectArray[25+n] = resellerGST;
			  objectArray[26+n] = merchantAmount;
			  objectArray[27+n] = txnSettledType;
			  objectArray[28+n] = partSettle;
			  objectArray[29+n] = refundOrderId;
			  
			  
				  objectArray[30+n] = UDF11;
				  objectArray[31+n] = UDF12;
				  objectArray[32+n] = UDF13;
				  objectArray[33+n] = UDF14;
				  objectArray[34+n] = UDF15;
				  objectArray[35+n] = UDF16;
				  objectArray[36+n] = UDF17;
				  objectArray[37+n] = UDF18;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  //objectArray = Arrays.copyOf(objectArray, 39);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						for(Merchants mrchnt : resellerMerchants) {
							if(mrchnt.isRetailMerchantFlag()) {
								objectArray[38+n] = categoryCode;
								  objectArray[39+n] = SKUCode;
								  objectArray[40+n] = refundCycle;
								  objectArray[41+n] = productPrice;
								  objectArray[42+n] = vendorID;
								break;
							}
						}
					}else {
						objectArray[38+n] = categoryCode;
						  objectArray[39+n] = SKUCode;
						  objectArray[40+n] = refundCycle;
						  objectArray[41+n] = productPrice;
						  objectArray[42+n] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							//objectArray = Arrays.copyOf(objectArray, 39);
							  
							objectArray[38+n] = categoryCode;
							  objectArray[39+n] = SKUCode;
							  objectArray[40+n] = refundCycle;
							  objectArray[41+n] = productPrice;
							  objectArray[42+n] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag()) {
							//objectArray = Arrays.copyOf(objectArray, 39);
							  
							objectArray[38+n] = categoryCode;
							  objectArray[39+n] = SKUCode;
							  objectArray[40+n] = refundCycle;
							  objectArray[41+n] = productPrice;
							  objectArray[42+n] = vendorID;
						}
						
					}
				}
			  
		  }else {
		  objectArray[22] = tdrOrSurcharge;
		  objectArray[23] = gst;
		  // add 2 columns by vishal 24 and 25 
		  objectArray[24] = sufTdr;
		  objectArray[25] = sufGst;
		  objectArray[24+n] = merchantAmount;
		  objectArray[25+n] = txnSettledType;
		  objectArray[26+n] = partSettle;
		  objectArray[27+n] = refundOrderId;
		  
		  
			  objectArray[28+n] = UDF11;
			  objectArray[29+n] = UDF12;
			  objectArray[30+n] = UDF13;
			  objectArray[31+n] = UDF14;
			  objectArray[32+n] = UDF15;
			  objectArray[33+n] = UDF16;
			  objectArray[34+n] = UDF17;
			  objectArray[35+n] = UDF18;
		  
		  if(merchantPayId.equalsIgnoreCase("ALL")) {
//			  objectArray = Arrays.copyOf(objectArray, 39);
			  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
					for(Merchants mrchnt : resellerMerchants) {
						if(mrchnt.isRetailMerchantFlag()) {
							objectArray[36+n] = categoryCode;
							  objectArray[37+n] = SKUCode;
							  objectArray[38+n] = refundCycle;
							  objectArray[39+n] = productPrice;
							  objectArray[40+n] = vendorID;
							break;
						}
					}
				}else {
					objectArray[36+n] = categoryCode;
					  objectArray[37+n] = SKUCode;
					  objectArray[38+n] = refundCycle;
					  objectArray[39+n] = productPrice;
					  objectArray[40+n] = vendorID;
					  
				}
			}else {
				if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
					
					if(merchant != null && merchant.isRetailMerchantFlag()) {
					//	objectArray = Arrays.copyOf(objectArray, 39);
						  
						objectArray[36+n] = categoryCode;
						  objectArray[37+n] = SKUCode;
						  objectArray[38+n] = refundCycle;
						  objectArray[39+n] = productPrice;
						  objectArray[40+n] = vendorID;
					}
				}else {
					
					if(sessionUser.isRetailMerchantFlag()) {
					//	objectArray = Arrays.copyOf(objectArray, 39);
						  
						objectArray[36+n] = categoryCode;
						  objectArray[37+n] = SKUCode;
						  objectArray[38+n] = refundCycle;
						  objectArray[39+n] = productPrice;
						  objectArray[40+n] = vendorID;
					}
					
				}
			}
		  }
		  
		 
		  return objectArray;
		}
	
	public Object[] myCsvMethodDownloadPaymentsReportByViewCaputured(String merchantPayId, User sessionUser, User merchant, List<Merchants> resellerMerchants) {
		  Object[] objectArray = new Object[32];
		  
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = merchants;
		  objectArray[4] = dateFrom;
		  objectArray[5] = orderId;
		  objectArray[6] = paymentMethods;
		  objectArray[7] = moptype;
		  objectArray[8] = cardMask;
		  objectArray[9] = custName;
		  objectArray[10] = custEmail;
		  objectArray[11] = cardHolderType;  
		  objectArray[12] = txnType;
		  objectArray[13] = transactionMode;
		  objectArray[14] = status;
		  objectArray[15] = refundOrderId;
		  objectArray[16] = transactionRegion;
		  objectArray[17] = amount;
		  objectArray[18] = totalAmount;
		  if(sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
			//  objectArray = Arrays.copyOf(objectArray, 29);
			  objectArray[19] = tdrOrSurcharge;
			  objectArray[20] = gst;
			  objectArray[21] = resellerCharges; 
			  objectArray[22] = resellerGST;
			  objectArray[23] = merchantAmount;
			  objectArray[24] = txnSettledType;
			  objectArray[25] = partSettle;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
			//	  objectArray = Arrays.copyOf(objectArray, 29);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						for(Merchants mrchnt : resellerMerchants) {
							if(mrchnt.isRetailMerchantFlag()) {
								objectArray[26] = categoryCode;
								  objectArray[27] = SKUCode;
								  objectArray[28] = refundCycle;
								  objectArray[29] = productPrice;
								  objectArray[30] = vendorID;
								  objectArray[31] = refund_flag;
								break;
							}
						}
						
					}else {
						objectArray[26] = categoryCode;
						  objectArray[27] = SKUCode;
						  objectArray[28] = refundCycle;
						  objectArray[29] = productPrice;
						  objectArray[30] = vendorID;
						  objectArray[31] = refund_flag;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
					//		objectArray = Arrays.copyOf(objectArray, 27);

							objectArray[26] = categoryCode;
							  objectArray[27] = SKUCode;
							  objectArray[28] = refundCycle;
							  objectArray[29] = productPrice;
							  objectArray[30] = vendorID;
							  objectArray[31] = refund_flag;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag()) {
					//		objectArray = Arrays.copyOf(objectArray, 27);

							objectArray[26] = categoryCode;
							  objectArray[27] = SKUCode;
							  objectArray[28] = refundCycle;
							  objectArray[29] = productPrice;
							  objectArray[30] = vendorID;
							  objectArray[31] = refund_flag;
						}
						
					}
				}
		  }else {
		  objectArray[19] = tdrOrSurcharge;
		  objectArray[20] = gst;
		  objectArray[21] = merchantAmount;
		  objectArray[22] = txnSettledType;
		  objectArray[23] = partSettle;
		  
		  if(merchantPayId.equalsIgnoreCase("ALL")) {
			// objectArray = Arrays.copyOf(objectArray, 27);
			  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
					for(Merchants mrchnt : resellerMerchants) {
						if(mrchnt.isRetailMerchantFlag()) {
							objectArray[24] = categoryCode;
							  objectArray[25] = SKUCode;
							  objectArray[26] = refundCycle;
							  objectArray[27] = productPrice;
							  objectArray[28] = vendorID;
							  objectArray[29] = refund_flag;
							break;
						}
					}
					
				}else {
					objectArray[24] = categoryCode;
					  objectArray[25] = SKUCode;
					  objectArray[26] = refundCycle;
					  objectArray[27] = productPrice;
					  objectArray[28] = vendorID;
					  objectArray[29] = refund_flag;
				}
			}else {
				if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
					
					if(merchant != null && merchant.isRetailMerchantFlag()) {
				//	objectArray = Arrays.copyOf(objectArray, 27);

						objectArray[24] = categoryCode;
						  objectArray[25] = SKUCode;
						  objectArray[26] = refundCycle;
						  objectArray[27] = productPrice;
						  objectArray[28] = vendorID;
						  objectArray[29] = refund_flag;
					}
				}else {
					
					if(sessionUser.isRetailMerchantFlag() && !sessionUser.isKhadiMerchant()) {
				//	objectArray = Arrays.copyOf(objectArray, 27);

						objectArray[24] = categoryCode;
						  objectArray[25] = SKUCode;
						  objectArray[26] = refundCycle;
						  objectArray[27] = productPrice;
						  objectArray[28] = vendorID;
						  objectArray[29] = refund_flag;
					}
					
				}
			}
		  }
		  
		  return objectArray;
		}
	
	public Object[] myCsvMethodDownloadPaymentsReportByViewCaputuredForSubMerchant(String merchantPayId, User sessionUser, User merchant, List<Merchants> resellerMerchants) {
		  Object[] objectArray = new Object[32];
		  
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = merchants;
		  objectArray[4] = subMerchantId;
		  objectArray[5] = dateFrom;
		  objectArray[6] = orderId;
		  objectArray[7] = paymentMethods;
		  objectArray[8] = moptype;
		  objectArray[9] = cardMask;
		  objectArray[10] = custName;
		  objectArray[11] = custEmail;
		  objectArray[12] = cardHolderType;  
		  objectArray[13] = txnType;
		  objectArray[14] = transactionMode;
		  objectArray[15] = status;
		  objectArray[16] = refundOrderId;
		  objectArray[17] = transactionRegion;
		  objectArray[18] = amount;
		  objectArray[19] = totalAmount;
		  if(sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
			  objectArray[20] = tdrOrSurcharge;
			  objectArray[21] = gst;
			  objectArray[22] = resellerCharges;
			  objectArray[23] = resellerGST;
			  objectArray[24] = merchantAmount;
			  objectArray[25] = txnSettledType;
			  objectArray[26] = partSettle;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  //objectArray = Arrays.copyOf(objectArray, 28);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						for(Merchants mrchnt : resellerMerchants) {
							if(mrchnt.isRetailMerchantFlag()) {
								objectArray[27] = categoryCode;
								  objectArray[28] = SKUCode;
								  objectArray[29] = refundCycle;
								  objectArray[30] = productPrice;
								  objectArray[31] = vendorID;
								break;
							}
						}
						
					}else {
						objectArray[27] = categoryCode;
						  objectArray[28] = SKUCode;
						  objectArray[29] = refundCycle;
						  objectArray[30] = productPrice;
						  objectArray[31] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							//objectArray = Arrays.copyOf(objectArray, 28);

							objectArray[27] = categoryCode;
							  objectArray[28] = SKUCode;
							  objectArray[29] = refundCycle;
							  objectArray[30] = productPrice;
							  objectArray[31] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag()) {
							//objectArray = Arrays.copyOf(objectArray, 28);

							objectArray[27] = categoryCode;
							  objectArray[28] = SKUCode;
							  objectArray[29] = refundCycle;
							  objectArray[30] = productPrice;
							  objectArray[31] = vendorID;
						}
						
					}
				}
		  }else {
		  objectArray[20] = tdrOrSurcharge;
		  objectArray[21] = gst;
		  objectArray[22] = merchantAmount;
		  objectArray[23] = txnSettledType;
		  objectArray[24] = partSettle;
		  
		  if(merchantPayId.equalsIgnoreCase("ALL")) {
			  //objectArray = Arrays.copyOf(objectArray, 28);
			  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
					for(Merchants mrchnt : resellerMerchants) {
						if(mrchnt.isRetailMerchantFlag()) {
							objectArray[25] = categoryCode;
							  objectArray[26] = SKUCode;
							  objectArray[27] = refundCycle;
							  objectArray[28] = productPrice;
							  objectArray[29] = vendorID;
							break;
						}
					}
					
				}else {
					objectArray[25] = categoryCode;
					  objectArray[26] = SKUCode;
					  objectArray[27] = refundCycle;
					  objectArray[28] = productPrice;
					  objectArray[29] = vendorID;
				}
			}else {
				if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
					
					if(merchant != null && merchant.isRetailMerchantFlag()) {
						//objectArray = Arrays.copyOf(objectArray, 28);

						objectArray[25] = categoryCode;
						  objectArray[26] = SKUCode;
						  objectArray[27] = refundCycle;
						  objectArray[28] = productPrice;
						  objectArray[29] = vendorID;
					}
				}else {
					
					if(sessionUser.isRetailMerchantFlag()) {
						//objectArray = Arrays.copyOf(objectArray, 28);

						objectArray[25] = categoryCode;
						  objectArray[26] = SKUCode;
						  objectArray[27] = refundCycle;
						  objectArray[28] = productPrice;
						  objectArray[29] = vendorID;
					}
					
				}
			}
		  }		  
		  
		  return objectArray;
		}
	
	public Object[] myCsvMethodDownloadPaymentsReportCapturedForSpecificMerchant(String merchantPayId, User sessionUser, User merchant, List<Merchants> resellerMerchants) {
		  Object[] objectArray = new Object[35];
		  
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = merchants;
		  objectArray[4] = dateFrom;
		  objectArray[5] = orderId;
		  objectArray[6] = paymentMethods;
		  objectArray[7] = moptype;
		  objectArray[8] = cardMask;
		  objectArray[9] = custName;
		  objectArray[10] = cardHolderType;  
		  objectArray[11] = txnType;
		  objectArray[12] = transactionMode;
		  objectArray[13] = status;
		  objectArray[14] = transactionRegion;
		  objectArray[15] = amount;
		  objectArray[16] = totalAmount;
		  objectArray[17] = tdrOrSurcharge;
		  objectArray[18] = gst;
		  objectArray[19] = doctor;
		  objectArray[20] = glocal;
		  objectArray[21] = partner;
		  objectArray[22] = uniqueId;
		  objectArray[23] = merchantAmount;
		  objectArray[24] = deliveryStatus;
		  objectArray[25] = txnSettledType;
		  objectArray[26] = partSettle;
		  objectArray[27] = UDF11;
		  objectArray[28] = UDF12;
		  objectArray[29] = UDF13;
		  objectArray[30] = UDF14;
		  objectArray[31] = UDF15;
		  objectArray[32] = UDF16;
		  objectArray[33] = UDF17;
		  objectArray[34] = UDF18;
		  
		  if(merchantPayId.equalsIgnoreCase("ALL")) {
			  objectArray = Arrays.copyOf(objectArray, 40);
			  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
					for(Merchants mrchnt : resellerMerchants) {
						if(mrchnt.isRetailMerchantFlag()) {
							objectArray[35] = categoryCode;
							  objectArray[36] = SKUCode;
							  objectArray[37] = refundCycle;
							  objectArray[38] = productPrice;
							  objectArray[39] = vendorID;
							break;
						}
					}
					
				}else {
					objectArray[35] = categoryCode;
					  objectArray[36] = SKUCode;
					  objectArray[37] = refundCycle;
					  objectArray[38] = productPrice;
					  objectArray[39] = vendorID;
				}
			}else {
				if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
					
					if(merchant != null && merchant.isRetailMerchantFlag()) {
						objectArray = Arrays.copyOf(objectArray, 40);

						objectArray[35] = categoryCode;
						  objectArray[36] = SKUCode;
						  objectArray[37] = refundCycle;
						  objectArray[38] = productPrice;
						  objectArray[39] = vendorID;
					}
				}else {
					
					if(sessionUser.isRetailMerchantFlag()) {
						objectArray = Arrays.copyOf(objectArray, 40);

						objectArray[35] = categoryCode;
						  objectArray[36] = SKUCode;
						  objectArray[37] = refundCycle;
						  objectArray[38] = productPrice;
						  objectArray[39] = vendorID;
					}
					
				}
			}
		  
		  return objectArray;
		}
	@SuppressWarnings("unchecked")
	public Object[] myCsvMethodDownloadPaymentsReportSaleCaptured(String merchantPayId, User sessionUser, User merchant, List<Merchants> resellerMerchants) {
		  int n =2;  
		  Object[] objectArray = new Object[38+n];
			  
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = merchants;
		  objectArray[4] = dateFrom;
		  objectArray[5] = orderId;
		  objectArray[6] = paymentMethods;
		  objectArray[7] = moptype;
		  objectArray[8] = cardMask;
		  objectArray[9] = custName;
		  objectArray[10] = custEmail;
		  objectArray[11] = cardHolderType;  
		  objectArray[12] = txnType;
		  objectArray[13] = transactionMode;
		  objectArray[14] = status;
		  objectArray[15] = transactionRegion;
		  objectArray[16] = amount;
		  objectArray[17] = totalAmount;
		  if(sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
			  objectArray[18] = tdrOrSurcharge;
			  objectArray[19] = gst;
			  //add to column by vishal 
			  objectArray[20] = sufTdr;
			  objectArray[21] = sufGst;
			  
			  objectArray[20+n] = resellerCharges;
			  objectArray[21+n] = resellerGST;
			  objectArray[22+n] = merchantAmount;
			  objectArray[23+n] = txnSettledType;
			  objectArray[24+n] = partSettle;
			  
			  
				  objectArray[25+n] = UDF11;
				  objectArray[26+n] = UDF12;
				  objectArray[27+n] = UDF13;
				  objectArray[28+n] = UDF14;
				  objectArray[29+n] = UDF15;
				  objectArray[30+n] = UDF16;
				  objectArray[31+n] = UDF17;
				  objectArray[32+n] = UDF18;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				 // objectArray = Arrays.copyOf(objectArray, 35);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						for(Merchants mrchnt : resellerMerchants) {
							if(mrchnt.isRetailMerchantFlag()) {
								objectArray[33+n] = categoryCode;
								  objectArray[34+n] = SKUCode;
								  objectArray[35+n] = refundCycle;
								  objectArray[36+n] = productPrice;
								  objectArray[37+n] = vendorID;
								break;
							}
						}
						
					}else {
						objectArray[33+n] = categoryCode;
						  objectArray[34+n] = SKUCode;
						  objectArray[35+n] = refundCycle;
						  objectArray[36+n] = productPrice;
						  objectArray[37+n] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							//objectArray = Arrays.copyOf(objectArray, 35);
							
							objectArray[33+n] = categoryCode;
							  objectArray[34+n] = SKUCode;
							  objectArray[35+n] = refundCycle;
							  objectArray[36+n] = productPrice;
							  objectArray[37+n] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag()) {
							//objectArray = Arrays.copyOf(objectArray, 35);
							
							objectArray[33+n] = categoryCode;
							  objectArray[34+n] = SKUCode;
							  objectArray[35+n] = refundCycle;
							  objectArray[36+n] = productPrice;
							  objectArray[37+n] = vendorID;
						}
						
					}
				}
			  
		  }else {
		      objectArray[18] = tdrOrSurcharge;
		      objectArray[19] = gst;
		    //add to column by vishal 
			  objectArray[20] = sufTdr;
			  objectArray[21] = sufGst;
			
			  objectArray[20+n] = merchantAmount;
			  objectArray[21+n] = txnSettledType;
			  objectArray[22+n] = partSettle;
			  
			  
				  objectArray[23+n] = UDF11;
				  objectArray[24+n] = UDF12;
				  objectArray[25+n] = UDF13;
				  objectArray[26+n] = UDF14;
				  objectArray[27+n] = UDF15;
				  objectArray[28+n] = UDF16;
				  objectArray[29+n] = UDF17;
				  objectArray[30+n] = UDF18;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  objectArray = Arrays.copyOf(objectArray, 36+n);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						for(Merchants mrchnt : resellerMerchants) {
							if(mrchnt.isRetailMerchantFlag()) {
								objectArray[31+n] = categoryCode;
								  objectArray[32+n] = SKUCode;
								  objectArray[33+n] = refundCycle;
								  objectArray[34+n] = productPrice;
								  objectArray[35+n] = vendorID;
								break;
							}
						}
						
					}else {
						objectArray[31+n] = categoryCode;
						  objectArray[32+n] = SKUCode;
						  objectArray[33+n] = refundCycle;
						  objectArray[34+n] = productPrice;
						  objectArray[35+n] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 36+n);
							
							objectArray[31+n] = categoryCode;
							  objectArray[32+n] = SKUCode;
							  objectArray[33+n] = refundCycle;
							  objectArray[34+n] = productPrice;
							  objectArray[35+n] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 36+n);
							
							objectArray[31+n] = categoryCode;
							  objectArray[32+n] = SKUCode;
							  objectArray[33+n] = refundCycle;
							  objectArray[34+n] = productPrice;
							  objectArray[35+n] = vendorID;
						}
						
					}
				}
			  
		  }
		  
		  
		  return objectArray;
		}
	@SuppressWarnings("unchecked")
	public Object[] myCsvMethodDownloadPaymentsReportCapturedForSubMerchant(String merchantPayId, User sessionUser, User merchant, List<Merchants> resellerMerchants) {
		 int n =2 ;
		  Object[] objectArray = new Object[39+n];
		  
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = merchants;
		  objectArray[4] = subMerchantId;
		  objectArray[5] = dateFrom;
		  objectArray[6] = orderId;
		  objectArray[7] = paymentMethods;
		  objectArray[8] = moptype;
		  objectArray[9] = cardMask;
		  objectArray[10] = custName;
		  objectArray[11] = custEmail;
		  objectArray[12] = cardHolderType;  
		  objectArray[13] = txnType;
		  objectArray[14] = transactionMode;
		  objectArray[15] = status;
		  objectArray[16] = transactionRegion;
		  objectArray[17] = amount;
		  objectArray[18] = totalAmount;
		  if(sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
			  objectArray[19] = sufTdr;
			  objectArray[20] = sufGst;
			  objectArray[19+n] = tdrOrSurcharge;
			  objectArray[20+n] = gst;
			  
			  objectArray[21+n] = resellerCharges; 
		      objectArray[22+n] = resellerGST;
			  objectArray[23+n] = merchantAmount;
			  objectArray[24+n] = txnSettledType;
			  objectArray[25+n] = partSettle;
			  
			 
				  objectArray[26+n] = UDF11;
				  objectArray[27+n] = UDF12;
				  objectArray[28+n] = UDF13;
				  objectArray[29+n] = UDF14;
				  objectArray[30+n] = UDF15;
				  objectArray[31+n] = UDF16;
				  objectArray[32+n] = UDF17;
				  objectArray[33+n] = UDF18;
				  
				  if(merchantPayId.equalsIgnoreCase("ALL")) {
					  //objectArray = Arrays.copyOf(objectArray, 36);
					  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
							for(Merchants mrchnt : resellerMerchants) {
								if(mrchnt.isRetailMerchantFlag()) {
									objectArray[34+n] = categoryCode;
									  objectArray[35+n] = SKUCode;
									  objectArray[36+n] = refundCycle;
									  objectArray[37+n] = productPrice;
									  objectArray[38+n] = vendorID;
									break;
								}
							}
							
						}else {
							objectArray[34+n] = categoryCode;
							  objectArray[35+n] = SKUCode;
							  objectArray[36+n] = refundCycle;
							  objectArray[37+n] = productPrice;
							  objectArray[38+n] = vendorID;
						}
					}else {
						if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
							
							if(merchant != null && merchant.isRetailMerchantFlag()) {
								//objectArray = Arrays.copyOf(objectArray, 36);
		
								objectArray[34+n] = categoryCode;
								  objectArray[35+n] = SKUCode;
								  objectArray[36+n] = refundCycle;
								  objectArray[37+n] = productPrice;
								  objectArray[38+n] = vendorID;
							}
						}else {
							
							if(sessionUser.isRetailMerchantFlag()) {
								//objectArray = Arrays.copyOf(objectArray, 36);
		
								objectArray[34+n] = categoryCode;
								  objectArray[35+n] = SKUCode;
								  objectArray[36+n] = refundCycle;
								  objectArray[37+n] = productPrice;
								  objectArray[38+n] = vendorID;
							}
							
						}
					}
				
		  }else {
			  objectArray[19] = sufTdr;
			  objectArray[20] = sufGst;
			  objectArray[19+n] = tdrOrSurcharge;
		      objectArray[20+n] = gst;
			  objectArray[21+n] = merchantAmount;
			  objectArray[22+n] = txnSettledType;
			  objectArray[23+n] = partSettle;
			  
			  
				  objectArray[24+n] = UDF11;
				  objectArray[25+n] = UDF12;
				  objectArray[26+n] = UDF13;
				  objectArray[27+n] = UDF14;
				  objectArray[28+n] = UDF15;
				  objectArray[29+n] = UDF16;
				  objectArray[30+n] = UDF17;
				  objectArray[31+n] = UDF18;
				  
				  if(merchantPayId.equalsIgnoreCase("ALL")) {
					  objectArray = Arrays.copyOf(objectArray, 37);
					  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
							for(Merchants mrchnt : resellerMerchants) {
								if(mrchnt.isRetailMerchantFlag()) {
									objectArray[32+n] = categoryCode;
									  objectArray[33+n] = SKUCode;
									  objectArray[34+n] = refundCycle;
									  objectArray[35+n] = productPrice;
									  objectArray[36+n] = vendorID;
									break;
								}
							}
							
						}else {
							objectArray[32+n] = categoryCode;
							  objectArray[33+n] = SKUCode;
							  objectArray[34+n] = refundCycle;
							  objectArray[35+n] = productPrice;
							  objectArray[36+n] = vendorID;
						}
					}else {
						if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
							
							if(merchant != null && merchant.isRetailMerchantFlag()) {
								objectArray = Arrays.copyOf(objectArray, 37);
		
								objectArray[32+n] = categoryCode;
								  objectArray[33+n] = SKUCode;
								  objectArray[34+n] = refundCycle;
								  objectArray[35+n] = productPrice;
								  objectArray[36+n] = vendorID;
							}
						}else {
							
							if(sessionUser.isRetailMerchantFlag()) {
								objectArray = Arrays.copyOf(objectArray, 37);
		
								objectArray[32+n] = categoryCode;
								  objectArray[33+n] = SKUCode;
								  objectArray[34+n] = refundCycle;
								  objectArray[35+n] = productPrice;
								  objectArray[36+n] = vendorID;
							}
							
						}
					}
				
		  }
		  
		 
		  return objectArray;
		}
	
	@SuppressWarnings("unchecked")
	public Object[] myCsvMethodDownloadPaymentsReportCapturedForSpecificSubMerchant(String merchantPayId, User sessionUser, User merchant, List<Merchants> resellerMerchants) {
		 int n =0;
		 Object[] objectArray = new Object[36+n];
		  
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = merchants;
		  objectArray[4] = subMerchantId;
		  objectArray[5] = dateFrom;
		  objectArray[6] = orderId;
		  objectArray[7] = paymentMethods;
		  objectArray[8] = moptype;
		  objectArray[9] = cardMask;
		  objectArray[10] = custName;
		  objectArray[11] = cardHolderType;  
		  objectArray[12] = txnType;
		  objectArray[13] = transactionMode;
		  objectArray[14] = status;
		  objectArray[15] = transactionRegion;
		  objectArray[16] = amount;
		  objectArray[17] = totalAmount;
		  objectArray[18] = tdrOrSurcharge;
		  //objectArray[19] = sufTdr;
		 // objectArray[20] = sufGst;
		  objectArray[19+n] = gst;
		  objectArray[20+n] = doctor;
		  objectArray[21+n] = glocal;
		  objectArray[22+n] = partner;
		  objectArray[23+n] = uniqueId;
		  objectArray[24+n] = merchantAmount;
		  objectArray[25+n] = deliveryStatus;
		  objectArray[26+n] = txnSettledType;
		  objectArray[27+n] = partSettle;
		  objectArray[28+n] = UDF11;
		  objectArray[29+n] = UDF12;
		  objectArray[30+n] = UDF13;
		  objectArray[31+n] = UDF14;
		  objectArray[32+n] = UDF15;
		  objectArray[33+n] = UDF16;
		  objectArray[34+n] = UDF17;
		  objectArray[35+n] = UDF18;
		  
		  if(merchantPayId.equalsIgnoreCase("ALL")) {
			  objectArray = Arrays.copyOf(objectArray, 41);
			  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
					for(Merchants mrchnt : resellerMerchants) {
						if(mrchnt.isRetailMerchantFlag()) {
							objectArray[36+n] = categoryCode;
							  objectArray[37+n] = SKUCode;
							  objectArray[38+n] = refundCycle;
							  objectArray[39+n] = productPrice;
							  objectArray[40+n] = vendorID;
							break;
						}
					}
					
				}else {
					objectArray[36+n] = categoryCode;
					  objectArray[37+n] = SKUCode;
					  objectArray[38+n] = refundCycle;
					  objectArray[39+n] = productPrice;
					  objectArray[40+n] = vendorID;
				}
			  
			}else {
				if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
					
					if(merchant != null && merchant.isRetailMerchantFlag()) {
						objectArray = Arrays.copyOf(objectArray, 41);
						
						objectArray[36+n] = categoryCode;
						  objectArray[37+n] = SKUCode;
						  objectArray[38+n] = refundCycle;
						  objectArray[39+n] = productPrice;
						  objectArray[40+n] = vendorID;
					}
				}else {
					
					if(sessionUser.isRetailMerchantFlag()) {
						objectArray = Arrays.copyOf(objectArray, 41);
						
						objectArray[36+n] = categoryCode;
						  objectArray[37+n] = SKUCode;
						  objectArray[38+n] = refundCycle;
						  objectArray[39+n] = productPrice;
						  objectArray[40+n] = vendorID;
					}
					
				}
			}
		  
		  return objectArray;
		}
	
	@SuppressWarnings("unchecked")
	public Object[] myCsvMethodemailPaymentsReportCapturedForSpecificSubMerchant() {
		  Object[] objectArray = new Object[36];
		  
		  objectArray[0] = srNo;
		  objectArray[1] = transactionId;
		  objectArray[2] = pgRefNum;
		  objectArray[3] = merchants;
		  objectArray[4] = dateFrom;
		  objectArray[5] = settledDate;
		  objectArray[6] = payOutDate;
		  objectArray[7] = utrNo;
		  objectArray[8] = orderId;
		  objectArray[9] = rrn;
		  objectArray[10] = paymentMethods;  
		  objectArray[11] = moptype;
		  objectArray[12] = cardMask;
		  objectArray[13] = custName;
		  objectArray[14] = cardHolderType;
		  objectArray[15] = txnType;
		  objectArray[16] = transactionMode;
		  objectArray[17] = status;
		  objectArray[18] = transactionRegion;
		  objectArray[19] = amount;
		  objectArray[20] = totalAmount;
		  objectArray[21] = tdrOrSurcharge;
		  objectArray[22] = gst;
		  objectArray[23] = txnSettledType;
		  objectArray[24] = partSettle;
		  objectArray[25] = UDF11;
		  objectArray[26] = UDF12;
		  objectArray[27] = UDF13;
		  objectArray[28] = UDF14;
		  objectArray[29] = UDF15;
		  objectArray[30] = UDF16;
		  objectArray[31] = UDF17;
		  objectArray[32] = UDF18;
		  
	
		  return objectArray;
	}

		@SuppressWarnings("unchecked")
		public Object[] myCsvMethodDownloadPaymentSettledReportForSpecificMerchant(String merchantPayId, User sessionUser, User merchant, List<Merchants> resellerMerchants) {
			  Object[] objectArray = new Object[45];
			  
				 
			  objectArray[0] = srNo;
			  objectArray[1] = transactionId;
			  objectArray[2] = pgRefNum;
			  objectArray[3] = merchants;
			  objectArray[4] = dateFrom;
			  objectArray[5] = settledDate;
			  objectArray[6] = payOutDate;
			  objectArray[7] = utrNo;
			  objectArray[8] = orderId;
			  objectArray[9] = rrn;
			  objectArray[10] = paymentMethods;
			  objectArray[11] = moptype;
			  objectArray[12] = cardMask;
			  objectArray[13] = custName;
			  objectArray[14] = cardHolderType;  
			  objectArray[15] = txnType;
			  objectArray[16] = transactionMode;
			  objectArray[17] = status;
			  objectArray[18] = transactionRegion;
			  objectArray[19] = amount;
			  objectArray[20] = totalAmount;
			  objectArray[21] = tdrOrSurcharge;
			  objectArray[22] = gst;
			  objectArray[23] = doctor;
			  objectArray[24] = glocal;
			  objectArray[25] = partner;
			  objectArray[26] = uniqueId;
			  objectArray[27] = merchantAmount;
			  objectArray[28] = deliveryStatus;
			  objectArray[29] = txnSettledType;
			  objectArray[30] = partSettle;
			  objectArray[31] = refundOrderId;
			  objectArray[32] = UDF11;
			  objectArray[33] = UDF12;
			  objectArray[34] = UDF13;
			  objectArray[35] = UDF14;
			  objectArray[36] = UDF15;
			  objectArray[37] = UDF16;
			  objectArray[38] = UDF17;
			  objectArray[39] = UDF18;
			  
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  objectArray = Arrays.copyOf(objectArray, 44);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						for(Merchants mrchnt : resellerMerchants) {
							if(mrchnt.isRetailMerchantFlag()) {
								objectArray[40] = categoryCode;
								  objectArray[41] = SKUCode;
								  objectArray[42] = refundCycle;
								  objectArray[43] = productPrice;
								  objectArray[44] = vendorID;
								break;
							}
						}
						
					}else {
						objectArray[40] = categoryCode;
						  objectArray[41] = SKUCode;
						  objectArray[42] = refundCycle;
						  objectArray[43] = productPrice;
						  objectArray[44] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 44);

							objectArray[40] = categoryCode;
							  objectArray[41] = SKUCode;
							  objectArray[42] = refundCycle;
							  objectArray[43] = productPrice;
							  objectArray[44] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 44);

							objectArray[40] = categoryCode;
							  objectArray[41] = SKUCode;
							  objectArray[42] = refundCycle;
							  objectArray[43] = productPrice;
							  objectArray[44] = vendorID;
						}
						
					}
				}
			  
			  return objectArray;
			}
		
		@SuppressWarnings("unchecked")
		public Object[] myCsvMethodDownloadPaymentSettledReportForSpecificSubMerchant(String merchantPayId, User sessionUser, User merchant, List<Merchants> resellerMerchants) {
			  Object[] objectArray = new Object[46];
			  
				 
			  objectArray[0] = srNo;
			  objectArray[1] = transactionId;
			  objectArray[2] = pgRefNum;
			  objectArray[3] = merchants;
			  objectArray[4] = subMerchantId;
			  objectArray[5] = dateFrom;
			  objectArray[6] = settledDate;
			  objectArray[7] = payOutDate;
			  objectArray[8] = utrNo;
			  objectArray[9] = orderId;
			  objectArray[10] = rrn;
			  objectArray[11] = paymentMethods;
			  objectArray[12] = moptype;
			  objectArray[13] = cardMask;
			  objectArray[14] = custName;
			  objectArray[15] = cardHolderType;  
			  objectArray[16] = txnType;
			  objectArray[17] = transactionMode;
			  objectArray[18] = status;
			  objectArray[19] = transactionRegion;
			  objectArray[20] = amount;
			  objectArray[21] = totalAmount;
			  objectArray[22] = tdrOrSurcharge;
			  objectArray[23] = gst;
			  objectArray[24] = doctor;
			  objectArray[25] = glocal;
			  objectArray[26] = partner;
			  objectArray[27] = uniqueId;
			  objectArray[28] = merchantAmount;
			  objectArray[29] = deliveryStatus;
			  objectArray[30] = txnSettledType;
			  objectArray[31] = partSettle;
			  objectArray[32] = refundOrderId;
			  objectArray[33] = UDF11;
			  objectArray[34] = UDF12;
			  objectArray[35] = UDF13;
			  objectArray[36] = UDF14;
			  objectArray[37] = UDF15;
			  objectArray[38] = UDF16;
			  objectArray[39] = UDF17;
			  objectArray[40] = UDF18;
			  
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				//  objectArray = Arrays.copyOf(objectArray, 45);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						for(Merchants mrchnt : resellerMerchants) {
							if(mrchnt.isRetailMerchantFlag()) {
								objectArray[41] = categoryCode;
								  objectArray[42] = SKUCode;
								  objectArray[43] = refundCycle;
								  objectArray[44] = productPrice;
								  objectArray[45] = vendorID;
								break;
							}
						}
						
					}else {
						objectArray[41] = categoryCode;
						  objectArray[42] = SKUCode;
						  objectArray[43] = refundCycle;
						  objectArray[44] = productPrice;
						  objectArray[45] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
						//	objectArray = Arrays.copyOf(objectArray, 45);

							objectArray[41] = categoryCode;
							  objectArray[42] = SKUCode;
							  objectArray[43] = refundCycle;
							  objectArray[44] = productPrice;
							  objectArray[45] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag()) {
						//	objectArray = Arrays.copyOf(objectArray, 45);

							objectArray[41] = categoryCode;
							  objectArray[42] = SKUCode;
							  objectArray[43] = refundCycle;
							  objectArray[44] = productPrice;
							  objectArray[45] = vendorID;
						}
						
					}
				}
			  
			  return objectArray;
			}
		
		@SuppressWarnings("unchecked")
	public Object[] myCsvMethodDownloadPaymentSettledReportForSubMerchant(String merchantPayId, User sessionUser,
			User merchant, List<Merchants> resellerMerchants) {
			int n=2;
		Object[] objectArray = new Object[44+n];

		objectArray[0] = srNo;
		objectArray[1] = transactionId;
		objectArray[2] = pgRefNum;
		objectArray[3] = merchants;
		objectArray[4] = subMerchantId;
		objectArray[5] = dateFrom;
		objectArray[6] = settledDate;
		objectArray[7] = payOutDate;
		objectArray[8] = utrNo;
		objectArray[9] = orderId;
		objectArray[10] = rrn;
		objectArray[11] = paymentMethods;
		objectArray[12] = moptype;
		objectArray[13] = cardMask;
		objectArray[14] = custName;
		objectArray[15] = custEmail;
		objectArray[16] = cardHolderType;
		objectArray[17] = txnType;
		objectArray[18] = transactionMode;
		objectArray[19] = status;
		objectArray[20] = transactionRegion;
		objectArray[21] = amount;
		objectArray[22] = totalAmount;
		objectArray[23] = sufTdr;
		objectArray[24] = sufGst;
		if (sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
			objectArray[23+n] = tdrOrSurcharge;
			objectArray[24+n] = gst;
			objectArray[25+n] = resellerCharges;
			objectArray[26+n] = resellerGST;
			objectArray[27+n] = merchantAmount;
			objectArray[28+n] = txnSettledType;
			objectArray[29+n] = partSettle;
			objectArray[30+n] = refundOrderId;
			
			if ((sessionUser.isKhadiMerchant() || sessionUser.isSmtMerchant())) {

				if (sessionUser.isRetailMerchantFlag() && !sessionUser.isKhadiMerchant()) {
					//objectArray = Arrays.copyOf(objectArray, 32);
					objectArray[31+n] = categoryCode;
					objectArray[32+n] = SKUCode;
					objectArray[33+n] = refundCycle;
					objectArray[34+n] = productPrice;
					objectArray[35+n] = vendorID;
				} else {

				}
			} else {
				objectArray[31+n] = UDF11;
				objectArray[32+n] = UDF12;
				objectArray[33+n] = UDF13;
				objectArray[34+n] = UDF14;
				objectArray[35+n] = UDF15;
				objectArray[36+n] = UDF16;
				objectArray[37+n] = UDF17;
				objectArray[38+n] = UDF18;

				if (merchantPayId.equalsIgnoreCase("ALL")) {
				//	objectArray = Arrays.copyOf(objectArray, 40);
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						for (Merchants mrchnt : resellerMerchants) {
							if (mrchnt.isRetailMerchantFlag()) {
								objectArray[39+n] = categoryCode;
								objectArray[40+n] = SKUCode;
								objectArray[41+n] = refundCycle;
								objectArray[42+n] = productPrice;
								objectArray[43+n] = vendorID;
								break;
							}
						}

					} else {
						objectArray[39+n] = categoryCode;
						objectArray[40+n] = SKUCode;
						objectArray[41+n] = refundCycle;
						objectArray[42+n] = productPrice;
						objectArray[43+n] = vendorID;
					}
				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {

						if (merchant != null && merchant.isRetailMerchantFlag()) {
							//objectArray = Arrays.copyOf(objectArray, 40);

							objectArray[39+n] = categoryCode;
							objectArray[40+n] = SKUCode;
							objectArray[41+n] = refundCycle;
							objectArray[42+n] = productPrice;
							objectArray[43+n] = vendorID;
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							//objectArray = Arrays.copyOf(objectArray, 40);

							objectArray[39+n] = categoryCode;
							objectArray[40+n] = SKUCode;
							objectArray[41+n] = refundCycle;
							objectArray[42+n] = productPrice;
							objectArray[43+n] = vendorID;
						}

					}
				}
			}
		} else {
			objectArray[23+n] = tdrOrSurcharge;
			objectArray[24+n] = gst;
			objectArray[25+n] = merchantAmount;
			objectArray[26+n] = txnSettledType;
			objectArray[27+n] = partSettle;
			objectArray[28+n] = refundOrderId;
			
			if ((sessionUser.isKhadiMerchant() || sessionUser.isSmtMerchant())) {

				if (sessionUser.isRetailMerchantFlag() && !sessionUser.isKhadiMerchant()) {
				//	objectArray = Arrays.copyOf(objectArray, 32);
					objectArray[29+n] = categoryCode;
					objectArray[30+n] = SKUCode;
					objectArray[31+n] = refundCycle;
					objectArray[32+n] = productPrice;
					objectArray[33+n] = vendorID;
				} else {

				}
			} else {
				objectArray[29+n] = UDF11;
				objectArray[30+n] = UDF12;
				objectArray[31+n] = UDF13;
				objectArray[32+n] = UDF14;
				objectArray[33+n] = UDF15;
				objectArray[34+n] = UDF16;
				objectArray[35+n] = UDF17;
				objectArray[36+n] = UDF18;

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					//objectArray = Arrays.copyOf(objectArray, 40);
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						for (Merchants mrchnt : resellerMerchants) {
							if (mrchnt.isRetailMerchantFlag()) {
								objectArray[37+n] = categoryCode;
								objectArray[38+n] = SKUCode;
								objectArray[39+n] = refundCycle;
								objectArray[40+n] = productPrice;
								objectArray[41+n] = vendorID;
								break;
							}
						}

					} else {
						objectArray[37+n] = categoryCode;
						objectArray[38+n] = SKUCode;
						objectArray[39+n] = refundCycle;
						objectArray[40+n] = productPrice;
						objectArray[41+n] = vendorID;
					}
				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {

						if (merchant != null && merchant.isRetailMerchantFlag()) {
						//	objectArray = Arrays.copyOf(objectArray, 40);

							objectArray[37+n] = categoryCode;
							objectArray[38+n] = SKUCode;
							objectArray[39+n] = refundCycle;
							objectArray[40+n] = productPrice;
							objectArray[41+n] = vendorID;
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
					//		objectArray = Arrays.copyOf(objectArray, 40);

							objectArray[37+n] = categoryCode;
							objectArray[38+n] = SKUCode;
							objectArray[39+n] = refundCycle;
							objectArray[40+n] = productPrice;
							objectArray[41+n] = vendorID;
						}

					}
				}
			}

		}
	
		
		return objectArray;
	}
		
		public Object[] myCsvMethodDownloadBookingPaymentsReportCapturedForSubMerchantWithGlocal(String merchantPayId, User sessionUser, User merchant) {
			  Object[] objectArray = new Object[34];
			  
			  objectArray[0] = srNo;
			  objectArray[1] = transactionId;
			  objectArray[2] = pgRefNum;
			  objectArray[3] = merchants;
			  objectArray[4] = subMerchantId;
			  objectArray[5] = dateFrom;
			  objectArray[6] = orderId;
			  objectArray[7] = paymentMethods;
			  objectArray[8] = moptype;
			  objectArray[9] = cardMask;
			  objectArray[10] = custName;
			  objectArray[11] = custMobile;
			  objectArray[12] = custEmail;
			  objectArray[13] = cardHolderType;  
			  objectArray[14] = txnType;
			  objectArray[15] = transactionMode;
			  objectArray[16] = status;
			  objectArray[17] = transactionRegion;
			  objectArray[18] = amount;
			  objectArray[19] = totalAmount;
			  objectArray[10] = tdrOrSurcharge;
			  objectArray[21] = gst;
			  objectArray[22] = doctor;
			  objectArray[23] = glocal;
			  objectArray[24] = partner;
			  objectArray[25] = uniqueId;
			  objectArray[26] = merchantAmount;
			  objectArray[27] = txnSettledType;
			  objectArray[28] = partSettle;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  objectArray = Arrays.copyOf(objectArray, 34);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						if(sessionUser.isRetailMerchantFlag()){
							objectArray[29] = categoryCode;
							  objectArray[30] = SKUCode;
							  objectArray[31] = refundCycle;
							  objectArray[32] = productPrice;
							  objectArray[33] = vendorID;
						}
						
					}else {
						objectArray[29] = categoryCode;
						  objectArray[30] = SKUCode;
						  objectArray[31] = refundCycle;
						  objectArray[32] = productPrice;
						  objectArray[33] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 34);

							objectArray[29] = categoryCode;
							  objectArray[30] = SKUCode;
							  objectArray[31] = refundCycle;
							  objectArray[32] = productPrice;
							  objectArray[33] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag() && !sessionUser.isKhadiMerchant()) {
							objectArray = Arrays.copyOf(objectArray, 34);

							objectArray[29] = categoryCode;
							  objectArray[30] = SKUCode;
							  objectArray[31] = refundCycle;
							  objectArray[32] = productPrice;
							  objectArray[33] = vendorID;
						}
						
					}
				}			  
			  
			 // objectArray[27] = customFlag;
			  return objectArray;
			}
		
		public Object[] myCsvMethodDownloadBookingPaymentsReportCaptured(String merchantPayId, User sessionUser, User merchant) {
			  Object[] objectArray = new Object[24];
			  
			  objectArray[0] = srNo;
			  objectArray[1] = transactionId;
			  objectArray[2] = pgRefNum;
			  objectArray[3] = merchants;
			  objectArray[4] = dateFrom;
			  objectArray[5] = orderId;
			  objectArray[6] = paymentMethods;
			  objectArray[7] = moptype;
			  objectArray[8] = cardMask;
			  objectArray[9] = custName;
			  objectArray[10] = custMobile;
			  objectArray[11] = custEmail;
			  objectArray[12] = cardHolderType;  
			  objectArray[13] = txnType;
			  objectArray[14] = transactionMode;
			  objectArray[15] = status;
			  objectArray[16] = transactionRegion;
			  objectArray[17] = amount;
			  objectArray[18] = totalAmount;
			  objectArray[19] = tdrOrSurcharge;
			  objectArray[20] = gst;
			  objectArray[21] = merchantAmount;
			  objectArray[22] = txnSettledType;
			  objectArray[23] = partSettle;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  objectArray = Arrays.copyOf(objectArray, 29);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						if(sessionUser.isRetailMerchantFlag()){
							objectArray[24] = categoryCode;
							  objectArray[25] = SKUCode;
							  objectArray[26] = refundCycle;
							  objectArray[27] = productPrice;
							  objectArray[28] = vendorID;
						}
						
					}else {
						objectArray[24] = categoryCode;
						  objectArray[25] = SKUCode;
						  objectArray[26] = refundCycle;
						  objectArray[27] = productPrice;
						  objectArray[28] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 29);
							  
							objectArray[24] = categoryCode;
							  objectArray[25] = SKUCode;
							  objectArray[26] = refundCycle;
							  objectArray[27] = productPrice;
							  objectArray[28] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag() && !sessionUser.isKhadiMerchant()) {
							objectArray = Arrays.copyOf(objectArray, 29);
							  
							objectArray[24] = categoryCode;
							  objectArray[25] = SKUCode;
							  objectArray[26] = refundCycle;
							  objectArray[27] = productPrice;
							  objectArray[28] = vendorID;
						}
						
					}
				}
			  
			  //objectArray[23] = customFlag;
			  return objectArray;
			}
		
		public Object[] myCsvMethodDownloadBookingPaymentsReportCapturedAndOrderIdOrPgRefNum(String merchantPayId, User sessionUser, User merchant) {
			  Object[] objectArray = new Object[25];
			  
			  objectArray[0] = srNo;
			  objectArray[1] = transactionId;
			  objectArray[2] = pgRefNum;
			  objectArray[3] = merchants;
			  objectArray[4] = subMerchantId;
			  objectArray[5] = dateFrom;
			  objectArray[6] = orderId;
			  objectArray[7] = paymentMethods;
			  objectArray[8] = moptype;
			  objectArray[9] = cardMask;
			  objectArray[10] = custName;
			  objectArray[11] = custMobile;
			  objectArray[12] = custEmail;
			  objectArray[13] = cardHolderType;  
			  objectArray[14] = txnType;
			  objectArray[15] = transactionMode;
			  objectArray[16] = status;
			  objectArray[17] = transactionRegion;
			  objectArray[18] = amount;
			  objectArray[19] = totalAmount;
			  objectArray[20] = tdrOrSurcharge;
			  objectArray[21] = gst;
			  objectArray[22] = merchantAmount;
			  objectArray[23] = txnSettledType;
			  objectArray[24] = partSettle;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  objectArray = Arrays.copyOf(objectArray, 30);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						if(sessionUser.isRetailMerchantFlag()){
							objectArray[25] = categoryCode;
							  objectArray[26] = SKUCode;
							  objectArray[27] = refundCycle;
							  objectArray[28] = productPrice;
							  objectArray[29] = vendorID;
						}
						
					}else {
						objectArray[25] = categoryCode;
						  objectArray[26] = SKUCode;
						  objectArray[27] = refundCycle;
						  objectArray[28] = productPrice;
						  objectArray[29] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 30);

							objectArray[25] = categoryCode;
							  objectArray[26] = SKUCode;
							  objectArray[27] = refundCycle;
							  objectArray[28] = productPrice;
							  objectArray[29] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 30);

							objectArray[25] = categoryCode;
							  objectArray[26] = SKUCode;
							  objectArray[27] = refundCycle;
							  objectArray[28] = productPrice;
							  objectArray[29] = vendorID;
						}
						
					}
				}
			  
			  return objectArray;
			}
		
		public Object[] myCsvMethodDownloadBookingPaymentsReportCapturedForGlocal(String merchantPayId, User sessionUser, User merchant) {
			  Object[] objectArray = new Object[28];
			  
			  objectArray[0] = srNo;
			  objectArray[1] = transactionId;
			  objectArray[2] = pgRefNum;
			  objectArray[3] = merchants;
			  objectArray[4] = dateFrom;
			  objectArray[5] = orderId;
			  objectArray[6] = paymentMethods;
			  objectArray[7] = moptype;
			  objectArray[8] = cardMask;
			  objectArray[9] = custName;
			  objectArray[10] = custMobile;
			  objectArray[11] = custEmail;
			  objectArray[12] = cardHolderType;  
			  objectArray[13] = txnType;
			  objectArray[14] = transactionMode;
			  objectArray[15] = status;
			  objectArray[16] = transactionRegion;
			  objectArray[17] = amount;
			  objectArray[18] = totalAmount;
			  objectArray[19] = tdrOrSurcharge;
			  objectArray[20] = gst;
			  objectArray[21] = doctor;
			  objectArray[22] = glocal;
			  objectArray[23] = partner;
			  objectArray[24] = uniqueId;
			  objectArray[25] = merchantAmount;
			  objectArray[26] = txnSettledType;
			  objectArray[27] = partSettle;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  objectArray = Arrays.copyOf(objectArray, 33);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						if(sessionUser.isRetailMerchantFlag()){
							objectArray[28] = categoryCode;
							  objectArray[29] = SKUCode;
							  objectArray[30] = refundCycle;
							  objectArray[31] = productPrice;
							  objectArray[32] = vendorID;
						}
						
					}else {
						objectArray[28] = categoryCode;
						  objectArray[29] = SKUCode;
						  objectArray[30] = refundCycle;
						  objectArray[31] = productPrice;
						  objectArray[32] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 33);

							objectArray[28] = categoryCode;
							  objectArray[29] = SKUCode;
							  objectArray[30] = refundCycle;
							  objectArray[31] = productPrice;
							  objectArray[32] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag() && !sessionUser.isKhadiMerchant()) {
							objectArray = Arrays.copyOf(objectArray, 33);

							objectArray[28] = categoryCode;
							  objectArray[29] = SKUCode;
							  objectArray[30] = refundCycle;
							  objectArray[31] = productPrice;
							  objectArray[32] = vendorID;
						}
						
					}
				}
			  //objectArray[23] = customFlag;
			  return objectArray;
			}
		
		public Object[] myCsvMethodDownloadBookingPaymentsReportCapturedForGlocalAndOrderIdOrPgRefNum(String merchantPayId, User sessionUser, User merchant) {
			  Object[] objectArray = new Object[29];
			  
			  objectArray[0] = srNo;
			  objectArray[1] = transactionId;
			  objectArray[2] = pgRefNum;
			  objectArray[3] = merchants;
			  objectArray[4] = subMerchantId;
			  objectArray[5] = dateFrom;
			  objectArray[6] = orderId;
			  objectArray[7] = paymentMethods;
			  objectArray[8] = moptype;
			  objectArray[9] = cardMask;
			  objectArray[10] = custName;
			  objectArray[11] = custMobile;
			  objectArray[12] = custEmail;
			  objectArray[13] = cardHolderType;  
			  objectArray[14] = txnType;
			  objectArray[15] = transactionMode;
			  objectArray[16] = status;
			  objectArray[17] = transactionRegion;
			  objectArray[18] = amount;
			  objectArray[19] = totalAmount;
			  objectArray[20] = tdrOrSurcharge;
			  objectArray[21] = gst;
			  objectArray[22] = doctor;
			  objectArray[23] = glocal;
			  objectArray[24] = partner;
			  objectArray[25] = uniqueId;
			  objectArray[26] = merchantAmount;
			  objectArray[27] = txnSettledType;
			  objectArray[28] = partSettle;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  objectArray = Arrays.copyOf(objectArray, 34);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						if(sessionUser.isRetailMerchantFlag()){
							objectArray[29] = categoryCode;
							  objectArray[30] = SKUCode;
							  objectArray[31] = refundCycle;
							  objectArray[32] = productPrice;
							  objectArray[33] = vendorID;
						}
						
					}else {
						objectArray[29] = categoryCode;
						  objectArray[30] = SKUCode;
						  objectArray[31] = refundCycle;
						  objectArray[32] = productPrice;
						  objectArray[33] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 34);

							objectArray[29] = categoryCode;
							  objectArray[30] = SKUCode;
							  objectArray[31] = refundCycle;
							  objectArray[32] = productPrice;
							  objectArray[33] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 34);

							objectArray[29] = categoryCode;
							  objectArray[30] = SKUCode;
							  objectArray[31] = refundCycle;
							  objectArray[32] = productPrice;
							  objectArray[33] = vendorID;
						}
						
					}
				}
			  
			  return objectArray;
			}
		
		public Object[] myCsvMethodDownloadBookingPaymentsReportCapturedForSubMerchant(String merchantPayId, User sessionUser, User merchant) {
			  Object[] objectArray = new Object[25];
			 
			  objectArray[0] = srNo;
			  objectArray[1] = transactionId;
			  objectArray[2] = pgRefNum;
			  objectArray[3] = merchants;
			  objectArray[4] = subMerchantId;
			  objectArray[5] = dateFrom;
			  objectArray[6] = orderId;
			  objectArray[7] = paymentMethods;
			  objectArray[8] = moptype;
			  objectArray[9] = cardMask;
			  objectArray[10] = custName;
			  objectArray[11] = custMobile;
			  objectArray[12] = custEmail;
			  objectArray[13] = cardHolderType;  
			  objectArray[14] = txnType;
			  objectArray[15] = transactionMode;
			  objectArray[16] = status;
			  objectArray[17] = transactionRegion;
			  objectArray[18] = amount;
			  objectArray[19] = totalAmount;
			  objectArray[20] = tdrOrSurcharge;
			  objectArray[21] = gst;
			  objectArray[22] = merchantAmount;
			  objectArray[23] = txnSettledType;
			  objectArray[24] = partSettle;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  objectArray = Arrays.copyOf(objectArray, 30);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						if(sessionUser.isRetailMerchantFlag()){
							objectArray[25] = categoryCode;
							  objectArray[26] = SKUCode;
							  objectArray[27] = refundCycle;
							  objectArray[28] = productPrice;
							  objectArray[29] = vendorID;
						}
						
					}else {
						objectArray[25] = categoryCode;
						  objectArray[26] = SKUCode;
						  objectArray[27] = refundCycle;
						  objectArray[28] = productPrice;
						  objectArray[29] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 30);

							objectArray[25] = categoryCode;
							  objectArray[26] = SKUCode;
							  objectArray[27] = refundCycle;
							  objectArray[28] = productPrice;
							  objectArray[29] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag() && !sessionUser.isKhadiMerchant()) {
							objectArray = Arrays.copyOf(objectArray, 30);

							objectArray[25] = categoryCode;
							  objectArray[26] = SKUCode;
							  objectArray[27] = refundCycle;
							  objectArray[28] = productPrice;
							  objectArray[29] = vendorID;
						}
						
					}
				}
			  
			  return objectArray;
			}
		public Object[] myCsvMethodDownloadBookingPaymentsReportCapturedForSubMerchantWithGlocalAndDispatchSlip(String merchantPayId, User sessionUser, User merchant) {
			  Object[] objectArray = new Object[32];
			  
			  objectArray[0] = srNo;
			  objectArray[1] = transactionId;
			  objectArray[2] = pgRefNum;
			  objectArray[3] = merchants;
			  objectArray[4] = subMerchantId;
			  objectArray[5] = invoiceNo;
			  objectArray[6] = dispatchSlipNo;
			  objectArray[7] = courierServiceProvider;
			  objectArray[8] = dateFrom;
			  objectArray[9] = orderId;
			  objectArray[10] = paymentMethods;
			  objectArray[11] = moptype;
			  objectArray[12] = cardMask;
			  objectArray[13] = custName;
			  objectArray[14] = custMobile;
			  objectArray[15] = custEmail;
			  objectArray[16] = cardHolderType;  
			  objectArray[17] = txnType;
			  objectArray[18] = transactionMode;
			  objectArray[19] = status;
			  objectArray[20] = transactionRegion;
			  objectArray[21] = amount;
			  objectArray[22] = totalAmount;
			  objectArray[23] = tdrOrSurcharge;
			  objectArray[24] = gst;
			  objectArray[25] = doctor;
			  objectArray[26] = glocal;
			  objectArray[27] = partner;
			  objectArray[28] = uniqueId;
			  objectArray[29] = merchantAmount;
			  objectArray[30] = txnSettledType;
			  objectArray[31] = partSettle;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  objectArray = Arrays.copyOf(objectArray, 37);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						if(sessionUser.isRetailMerchantFlag()){
							 objectArray[32] = categoryCode;
							  objectArray[33] = SKUCode;
							  objectArray[34] = refundCycle;
							  objectArray[35] = productPrice;
							  objectArray[36] = vendorID;
						}
						
					}else {
						objectArray[32] = categoryCode;
						  objectArray[33] = SKUCode;
						  objectArray[34] = refundCycle;
						  objectArray[35] = productPrice;
						  objectArray[36] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 37);

							objectArray[32] = categoryCode;
							  objectArray[33] = SKUCode;
							  objectArray[34] = refundCycle;
							  objectArray[35] = productPrice;
							  objectArray[36] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag() && !sessionUser.isKhadiMerchant()) {
							objectArray = Arrays.copyOf(objectArray, 37);

							objectArray[32] = categoryCode;
							  objectArray[33] = SKUCode;
							  objectArray[34] = refundCycle;
							  objectArray[35] = productPrice;
							  objectArray[36] = vendorID;
						}
						
					}
				}
			  
			  return objectArray;
			}
		public Object[] myCsvMethodDownloadBookingPaymentsReportCapturedForSubMerchantAndDispatchSlip(String merchantPayId, User sessionUser, User merchant) {
			  Object[] objectArray = new Object[28];
			 
			  objectArray[0] = srNo;
			  objectArray[1] = transactionId;
			  objectArray[2] = pgRefNum;
			  objectArray[3] = merchants;
			  objectArray[4] = subMerchantId;
			  objectArray[5] = invoiceNo;
			  objectArray[6] = dispatchSlipNo;
			  objectArray[7] = courierServiceProvider;
			  objectArray[8] = dateFrom;
			  objectArray[9] = orderId;
			  objectArray[10] = paymentMethods;
			  objectArray[11] = moptype;
			  objectArray[12] = cardMask;
			  objectArray[13] = custName;
			  objectArray[14] = custMobile;
			  objectArray[15] = custEmail;
			  objectArray[16] = cardHolderType;  
			  objectArray[17] = txnType;
			  objectArray[18] = transactionMode;
			  objectArray[19] = status;
			  objectArray[20] = transactionRegion;
			  objectArray[21] = amount;
			  objectArray[22] = totalAmount;
			  objectArray[23] = tdrOrSurcharge;
			  objectArray[24] = gst;
			  objectArray[25] = merchantAmount;
			  objectArray[26] = txnSettledType;
			  objectArray[27] = partSettle;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  objectArray = Arrays.copyOf(objectArray, 33);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						if(sessionUser.isRetailMerchantFlag()){
							objectArray[28] = categoryCode;
							  objectArray[29] = SKUCode;
							  objectArray[30] = refundCycle;
							  objectArray[31] = productPrice;
							  objectArray[32] = vendorID;
						}
						
					}else {
						objectArray[28] = categoryCode;
						  objectArray[29] = SKUCode;
						  objectArray[30] = refundCycle;
						  objectArray[31] = productPrice;
						  objectArray[32] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 33);

							objectArray[28] = categoryCode;
							  objectArray[29] = SKUCode;
							  objectArray[30] = refundCycle;
							  objectArray[31] = productPrice;
							  objectArray[32] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag() && !sessionUser.isKhadiMerchant()) {
							objectArray = Arrays.copyOf(objectArray, 33);

							objectArray[28] = categoryCode;
							  objectArray[29] = SKUCode;
							  objectArray[30] = refundCycle;
							  objectArray[31] = productPrice;
							  objectArray[32] = vendorID;
						}
						
					}
				}
			 
			  return objectArray;
			}
		
		public Object[] myCsvMethodDownloadBookingPaymentsReportCapturedForDispatchSlip(String merchantPayId, User sessionUser, User merchant) {
			  Object[] objectArray = new Object[27];
			  
			  objectArray[0] = srNo;
			  objectArray[1] = transactionId;
			  objectArray[2] = pgRefNum;
			  objectArray[3] = merchants;
			  objectArray[4] = invoiceNo;
			  objectArray[5] = dispatchSlipNo;
			  objectArray[6] = courierServiceProvider;			  
			  objectArray[7] = dateFrom;
			  objectArray[8] = orderId;
			  objectArray[9] = paymentMethods;
			  objectArray[10] = moptype;
			  objectArray[11] = cardMask;
			  objectArray[12] = custName;
			  objectArray[13] = custMobile;
			  objectArray[14] = custEmail;
			  objectArray[15] = cardHolderType;  
			  objectArray[16] = txnType;
			  objectArray[17] = transactionMode;
			  objectArray[18] = status;
			  objectArray[19] = transactionRegion;
			  objectArray[20] = amount;
			  objectArray[21] = totalAmount;
			  objectArray[22] = tdrOrSurcharge;
			  objectArray[23] = gst;
			  objectArray[24] = merchantAmount;
			  objectArray[25] = txnSettledType;
			  objectArray[26] = partSettle;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  objectArray = Arrays.copyOf(objectArray, 32);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						if(sessionUser.isRetailMerchantFlag()){
							objectArray[27] = categoryCode;
							  objectArray[28] = SKUCode;
							  objectArray[29] = refundCycle;
							  objectArray[30] = productPrice;
							  objectArray[31] = vendorID;
						}
						
					}else {
						objectArray[27] = categoryCode;
						  objectArray[28] = SKUCode;
						  objectArray[29] = refundCycle;
						  objectArray[30] = productPrice;
						  objectArray[31] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 32);

							objectArray[27] = categoryCode;
							  objectArray[28] = SKUCode;
							  objectArray[29] = refundCycle;
							  objectArray[30] = productPrice;
							  objectArray[31] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag() && !sessionUser.isKhadiMerchant()) {
							objectArray = Arrays.copyOf(objectArray, 32);

							objectArray[27] = categoryCode;
							  objectArray[28] = SKUCode;
							  objectArray[29] = refundCycle;
							  objectArray[30] = productPrice;
							  objectArray[31] = vendorID;
						}
						
					}
				}
			  
			  return objectArray;
			}
		
		public Object[] myCsvMethodDownloadBookingPaymentsReportCapturedForDispatchSlipAndOrderIdOrPgRefNum(String merchantPayId, User sessionUser, User merchant) {
			  Object[] objectArray = new Object[28];
			  
			  objectArray[0] = srNo;
			  objectArray[1] = transactionId;
			  objectArray[2] = pgRefNum;
			  objectArray[3] = merchants;
			  objectArray[4] = subMerchantId;
			  objectArray[5] = invoiceNo;
			  objectArray[6] = dispatchSlipNo;
			  objectArray[7] = courierServiceProvider;			  
			  objectArray[8] = dateFrom;
			  objectArray[9] = orderId;
			  objectArray[10] = paymentMethods;
			  objectArray[11] = moptype;
			  objectArray[12] = cardMask;
			  objectArray[13] = custName;
			  objectArray[14] = custMobile;
			  objectArray[15] = custEmail;
			  objectArray[16] = cardHolderType;  
			  objectArray[17] = txnType;
			  objectArray[18] = transactionMode;
			  objectArray[19] = status;
			  objectArray[20] = transactionRegion;
			  objectArray[21] = amount;
			  objectArray[22] = totalAmount;
			  objectArray[23] = tdrOrSurcharge;
			  objectArray[24] = gst;
			  objectArray[25] = merchantAmount;
			  objectArray[26] = txnSettledType;
			  objectArray[27] = partSettle;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  objectArray = Arrays.copyOf(objectArray, 33);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						if(sessionUser.isRetailMerchantFlag()){
							objectArray[28] = categoryCode;
							  objectArray[29] = SKUCode;
							  objectArray[30] = refundCycle;
							  objectArray[31] = productPrice;
							  objectArray[32] = vendorID;
						}
						
					}else {
						objectArray[28] = categoryCode;
						  objectArray[29] = SKUCode;
						  objectArray[30] = refundCycle;
						  objectArray[31] = productPrice;
						  objectArray[32] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 33);

							objectArray[28] = categoryCode;
							  objectArray[29] = SKUCode;
							  objectArray[30] = refundCycle;
							  objectArray[31] = productPrice;
							  objectArray[32] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 33);

							objectArray[28] = categoryCode;
							  objectArray[29] = SKUCode;
							  objectArray[30] = refundCycle;
							  objectArray[31] = productPrice;
							  objectArray[32] = vendorID;
						}
						
					}
				}
			  
			  return objectArray;
			}
		
		public Object[] myCsvMethodDownloadBookingPaymentsReportCapturedForGlocalAndDispatchSlip(String merchantPayId, User sessionUser, User merchant) {
			  Object[] objectArray = new Object[31];
			  
			  objectArray[0] = srNo;
			  objectArray[1] = transactionId;
			  objectArray[2] = pgRefNum;
			  objectArray[3] = merchants;
			  objectArray[4] = invoiceNo;
			  objectArray[5] = dispatchSlipNo;
			  objectArray[6] = courierServiceProvider;			  
			  objectArray[7] = dateFrom;
			  objectArray[8] = orderId;
			  objectArray[9] = paymentMethods;
			  objectArray[10] = moptype;
			  objectArray[11] = cardMask;
			  objectArray[12] = custName;
			  objectArray[13] = custMobile;
			  objectArray[14] = custEmail;
			  objectArray[15] = cardHolderType;  
			  objectArray[16] = txnType;
			  objectArray[17] = transactionMode;
			  objectArray[18] = status;
			  objectArray[19] = transactionRegion;
			  objectArray[20] = amount;
			  objectArray[21] = totalAmount;
			  objectArray[22] = tdrOrSurcharge;
			  objectArray[23] = gst;
			  objectArray[24] = doctor;
			  objectArray[25] = glocal;
			  objectArray[26] = partner;
			  objectArray[27] = uniqueId;
			  objectArray[28] = merchantAmount;
			  objectArray[29] = txnSettledType;
			  objectArray[30] = partSettle;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  objectArray = Arrays.copyOf(objectArray, 36);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						if(sessionUser.isRetailMerchantFlag()){
							objectArray[31] = categoryCode;
							  objectArray[32] = SKUCode;
							  objectArray[33] = refundCycle;
							  objectArray[34] = productPrice;
							  objectArray[35] = vendorID;
						}
						
					}else {
						objectArray[31] = categoryCode;
						  objectArray[32] = SKUCode;
						  objectArray[33] = refundCycle;
						  objectArray[34] = productPrice;
						  objectArray[35] = vendorID;
					}
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 36);

							objectArray[31] = categoryCode;
							  objectArray[32] = SKUCode;
							  objectArray[33] = refundCycle;
							  objectArray[34] = productPrice;
							  objectArray[35] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag() && !sessionUser.isKhadiMerchant()) {
							objectArray = Arrays.copyOf(objectArray, 36);

							objectArray[31] = categoryCode;
							  objectArray[32] = SKUCode;
							  objectArray[33] = refundCycle;
							  objectArray[34] = productPrice;
							  objectArray[35] = vendorID;
						}
						
					}
				}
			  
			  return objectArray;
			}
		
		public Object[] myCsvMethodDownloadBookingPaymentsReportCapturedForGlocalAndDispatchSlipAndOrderIdOrPgRefNum(String merchantPayId, User sessionUser, User merchant) {
			  Object[] objectArray = new Object[32];
			  
			  objectArray[0] = srNo;
			  objectArray[1] = transactionId;
			  objectArray[2] = pgRefNum;
			  objectArray[3] = merchants;
			  objectArray[4] = subMerchantId;
			  objectArray[5] = invoiceNo;
			  objectArray[6] = dispatchSlipNo;
			  objectArray[7] = courierServiceProvider;			  
			  objectArray[8] = dateFrom;
			  objectArray[9] = orderId;
			  objectArray[10] = paymentMethods;
			  objectArray[11] = moptype;
			  objectArray[12] = cardMask;
			  objectArray[13] = custName;
			  objectArray[14] = custMobile;
			  objectArray[15] = custEmail;
			  objectArray[16] = cardHolderType;  
			  objectArray[17] = txnType;
			  objectArray[18] = transactionMode;
			  objectArray[19] = status;
			  objectArray[20] = transactionRegion;
			  objectArray[21] = amount;
			  objectArray[22] = totalAmount;
			  objectArray[23] = tdrOrSurcharge;
			  objectArray[24] = gst;
			  objectArray[25] = doctor;
			  objectArray[26] = glocal;
			  objectArray[27] = partner;
			  objectArray[28] = uniqueId;
			  objectArray[29] = merchantAmount;
			  objectArray[30] = txnSettledType;
			  objectArray[31] = partSettle;
			  
			  if(merchantPayId.equalsIgnoreCase("ALL")) {
				  objectArray = Arrays.copyOf(objectArray, 37);
				  if(sessionUser.getUserType().equals(UserType.RESELLER)) {
						if(sessionUser.isRetailMerchantFlag()){
							objectArray[32] = categoryCode;
							  objectArray[33] = SKUCode;
							  objectArray[34] = refundCycle;
							  objectArray[35] = productPrice;
							  objectArray[36] = vendorID;
						}
						
					}else {
						objectArray[32] = categoryCode;
						  objectArray[33] = SKUCode;
						  objectArray[34] = refundCycle;
						  objectArray[35] = productPrice;
						  objectArray[36] = vendorID;
					}
				  
				}else {
					if(sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)){
						
						if(merchant != null && merchant.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 37);

							objectArray[32] = categoryCode;
							  objectArray[33] = SKUCode;
							  objectArray[34] = refundCycle;
							  objectArray[35] = productPrice;
							  objectArray[36] = vendorID;
						}
					}else {
						
						if(sessionUser.isRetailMerchantFlag()) {
							objectArray = Arrays.copyOf(objectArray, 37);

							objectArray[32] = categoryCode;
							  objectArray[33] = SKUCode;
							  objectArray[34] = refundCycle;
							  objectArray[35] = productPrice;
							  objectArray[36] = vendorID;
						}
						
					}
				}
			  
			  return objectArray;
			}
		
		public Object[] myCsvMethodDownloadUnsettledReport(String merchantPayId, User sessionUser, User merchant,
				List<Merchants> resellerMerchants) {
			Object[] objectArray = new Object[34];

			objectArray[0] = srNo;
			objectArray[1] = transactionId;
			objectArray[2] = pgRefNum;
			objectArray[3] = merchants;
			objectArray[4] = acquirerType;
			objectArray[5] = rrn;
			objectArray[6] = acqId;
			objectArray[7] = dateFrom;
			objectArray[8] = orderId;
			objectArray[9] = paymentMethods;
			objectArray[10] = moptype;
			objectArray[11] = cardMask;
			objectArray[12] = custName;
			objectArray[13] = custEmail;
			objectArray[14] = cardHolderType;
			objectArray[15] = txnType;
			objectArray[16] = transactionMode;
			objectArray[17] = status;
			objectArray[18] = refundOrderId;
			objectArray[19] = transactionRegion;
			objectArray[20] = amount;
			objectArray[21] = totalAmount;
			if (sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
				// objectArray = Arrays.copyOf(objectArray, 29);
				objectArray[22] = tdrOrSurcharge;
				objectArray[23] = gst;
				objectArray[24] = resellerCharges;
				objectArray[25] = resellerGST;
				objectArray[26] = merchantAmount;
				objectArray[27] = txnSettledType;
				objectArray[28] = partSettle;

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					// objectArray = Arrays.copyOf(objectArray, 29);
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						for (Merchants mrchnt : resellerMerchants) {
							if (mrchnt.isRetailMerchantFlag()) {
								objectArray[29] = categoryCode;
								objectArray[30] = SKUCode;
								objectArray[31] = refundCycle;
								objectArray[32] = productPrice;
								objectArray[33] = vendorID;
								break;
							}
						}

					} else {
						objectArray[29] = categoryCode;
						objectArray[30] = SKUCode;
						objectArray[31] = refundCycle;
						objectArray[32] = productPrice;
						objectArray[33] = vendorID;
					}
				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {

						if (merchant != null && merchant.isRetailMerchantFlag()) {
							// objectArray = Arrays.copyOf(objectArray, 27);

							objectArray[29] = categoryCode;
							objectArray[30] = SKUCode;
							objectArray[31] = refundCycle;
							objectArray[32] = productPrice;
							objectArray[33] = vendorID;
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							// objectArray = Arrays.copyOf(objectArray, 27);

							objectArray[29] = categoryCode;
							objectArray[30] = SKUCode;
							objectArray[31] = refundCycle;
							objectArray[32] = productPrice;
							objectArray[33] = vendorID;
						}

					}
				}
			} else {
				objectArray[22] = tdrOrSurcharge;
				objectArray[23] = gst;
				objectArray[24] = merchantAmount;
				objectArray[25] = txnSettledType;
				objectArray[26] = partSettle;

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					// objectArray = Arrays.copyOf(objectArray, 27);
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						for (Merchants mrchnt : resellerMerchants) {
							if (mrchnt.isRetailMerchantFlag()) {
								objectArray[27] = categoryCode;
								objectArray[28] = SKUCode;
								objectArray[29] = refundCycle;
								objectArray[30] = productPrice;
								objectArray[31] = vendorID;
								break;
							}
						}

					} else {
						objectArray[27] = categoryCode;
						objectArray[28] = SKUCode;
						objectArray[29] = refundCycle;
						objectArray[30] = productPrice;
						objectArray[31] = vendorID;
					}
				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {

						if (merchant != null && merchant.isRetailMerchantFlag()) {
							// objectArray = Arrays.copyOf(objectArray, 27);

							objectArray[27] = categoryCode;
							objectArray[28] = SKUCode;
							objectArray[29] = refundCycle;
							objectArray[30] = productPrice;
							objectArray[31] = vendorID;
						}
					} else {

						if (sessionUser.isRetailMerchantFlag() && !sessionUser.isKhadiMerchant()) {
							// objectArray = Arrays.copyOf(objectArray, 27);

							objectArray[27] = categoryCode;
							objectArray[28] = SKUCode;
							objectArray[29] = refundCycle;
							objectArray[30] = productPrice;
							objectArray[31] = vendorID;
						}

					}
				}
			}

			return objectArray;
		}

		public Object[] myCsvMethodDownloadUnsettledReportForSubMerchant(String merchantPayId, User sessionUser,
				User merchant, List<Merchants> resellerMerchants) {
			Object[] objectArray = new Object[35];

			objectArray[0] = srNo;
			objectArray[1] = transactionId;
			objectArray[2] = pgRefNum;
			objectArray[3] = merchants;
			objectArray[4] = acquirerType;
			objectArray[5] = rrn;
			objectArray[6] = acqId;
			objectArray[7] = subMerchantId;
			objectArray[8] = dateFrom;
			objectArray[9] = orderId;
			objectArray[10] = paymentMethods;
			objectArray[11] = moptype;
			objectArray[12] = cardMask;
			objectArray[13] = custName;
			objectArray[14] = custEmail;
			objectArray[15] = cardHolderType;
			objectArray[16] = txnType;
			objectArray[17] = transactionMode;
			objectArray[18] = status;
			objectArray[19] = refundOrderId;
			objectArray[20] = transactionRegion;
			objectArray[21] = amount;
			objectArray[22] = totalAmount;
			if (sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {
				objectArray[23] = tdrOrSurcharge;
				objectArray[24] = gst;
				objectArray[25] = resellerCharges;
				objectArray[26] = resellerGST;
				objectArray[27] = merchantAmount;
				objectArray[28] = txnSettledType;
				objectArray[29] = partSettle;

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					// objectArray = Arrays.copyOf(objectArray, 28);
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						for (Merchants mrchnt : resellerMerchants) {
							if (mrchnt.isRetailMerchantFlag()) {
								objectArray[30] = categoryCode;
								objectArray[31] = SKUCode;
								objectArray[32] = refundCycle;
								objectArray[33] = productPrice;
								objectArray[34] = vendorID;
								break;
							}
						}

					} else {
						objectArray[30] = categoryCode;
						objectArray[31] = SKUCode;
						objectArray[32] = refundCycle;
						objectArray[33] = productPrice;
						objectArray[34] = vendorID;
					}
				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {

						if (merchant != null && merchant.isRetailMerchantFlag()) {
							// objectArray = Arrays.copyOf(objectArray, 28);

							objectArray[30] = categoryCode;
							objectArray[31] = SKUCode;
							objectArray[32] = refundCycle;
							objectArray[33] = productPrice;
							objectArray[34] = vendorID;
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							// objectArray = Arrays.copyOf(objectArray, 28);

							objectArray[30] = categoryCode;
							objectArray[31] = SKUCode;
							objectArray[32] = refundCycle;
							objectArray[33] = productPrice;
							objectArray[34] = vendorID;
						}

					}
				}
			} else {
				objectArray[23] = tdrOrSurcharge;
				objectArray[24] = gst;
				objectArray[25] = merchantAmount;
				objectArray[26] = txnSettledType;
				objectArray[27] = partSettle;

				if (merchantPayId.equalsIgnoreCase("ALL")) {
					// objectArray = Arrays.copyOf(objectArray, 28);
					if (sessionUser.getUserType().equals(UserType.RESELLER)) {
						for (Merchants mrchnt : resellerMerchants) {
							if (mrchnt.isRetailMerchantFlag()) {
								objectArray[28] = categoryCode;
								objectArray[29] = SKUCode;
								objectArray[30] = refundCycle;
								objectArray[31] = productPrice;
								objectArray[32] = vendorID;
								break;
							}
						}

					} else {
						objectArray[28] = categoryCode;
						objectArray[29] = SKUCode;
						objectArray[30] = refundCycle;
						objectArray[31] = productPrice;
						objectArray[32] = vendorID;
					}
				} else {
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {

						if (merchant != null && merchant.isRetailMerchantFlag()) {
							// objectArray = Arrays.copyOf(objectArray, 28);

							objectArray[28] = categoryCode;
							objectArray[29] = SKUCode;
							objectArray[30] = refundCycle;
							objectArray[31] = productPrice;
							objectArray[32] = vendorID;
						}
					} else {

						if (sessionUser.isRetailMerchantFlag()) {
							// objectArray = Arrays.copyOf(objectArray, 28);

							objectArray[28] = categoryCode;
							objectArray[29] = SKUCode;
							objectArray[30] = refundCycle;
							objectArray[31] = productPrice;
							objectArray[32] = vendorID;
						}

					}
				}
			}

			return objectArray;
		}
}
