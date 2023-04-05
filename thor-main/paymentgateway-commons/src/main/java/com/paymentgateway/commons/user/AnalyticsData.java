package com.paymentgateway.commons.user;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsData {

	private String totalTxnCount;
	private String totalCapturedTxnAmount;
	private String totalRejectedTxnPercent;
	
	private String successTxnCount;
	private String successTxnPercent;
	private String failedTxnCount;
	
	private String avgTkt;
	
	private String CCTxnPercent;
	private String CCSuccessRate;
	
	private String DCTxnPercent;
	private String DCSuccessRate;
	
	private String UPTxnPercent;
	private String UPSuccessRate;
	
	private String NBTxnPercent;
	private String NBSuccessRate;
	
	private String WLTxnPercent;
	private String WLSuccessRate;
	
	private String EMTxnPercent;
	private String EMSuccessRate;
	
	private String CDTxnPercent;
	private String CDSuccessRate;
	
	private String totalCCTxn;
	private String totalCCSuccessTxnPercent;
	private String totalCCFailedTxnPercent;
	private String totalCCCancelledTxnPercent;
	private String totalCCInvalidTxnPercent;
	private String totalCCFraudTxnPercent;
	private String totalCCDroppedTxnPercent;
	private String totalCCRejectedTxnPercent;
	private String totalCCDeclinedTxnPercent;
	private String totalCCPendingTxnPercent;
	private String totalCCAcqDownTxnPercent;
	private String totalCCFailedAtAcqTxnPercent;
	private String totalCCAcqTimeOutTxnPercent;
	
	private String totalDCTxn;
	private String totalDCSuccessTxnPercent;
	private String totalDCFailedTxnPercent;
	private String totalDCCancelledTxnPercent;
	private String totalDCInvalidTxnPercent;
	private String totalDCFraudTxnPercent;
	private String totalDCDroppedTxnPercent;
	private String totalDCRejectedTxnPercent;
	private String totalDCDeclinedTxnPercent;
	private String totalDCPendingTxnPercent;
	private String totalDCAcqDownTxnPercent;
	private String totalDCFailedAtAcqTxnPercent;
	private String totalDCAcqTimeOutTxnPercent;
	

	private String totalUPTxn;
	private String totalUPSuccessTxnPercent;
	private String totalUPFailedTxnPercent;
	private String totalUPCancelledTxnPercent;
	private String totalUPInvalidTxnPercent;
	private String totalUPFraudTxnPercent;
	private String totalUPDroppedTxnPercent;
	private String totalUPRejectedTxnPercent;
	private String totalUPDeclinedTxnPercent;
	private String totalUPPendingTxnPercent;
	private String totalUPAcqDownTxnPercent;
	private String totalUPFailedAtAcqTxnPercent;
	private String totalUPAcqTimeOutTxnPercent;
	
	private String totalNBTxn;
	private String totalNBSuccessTxnPercent;
	private String totalNBFailedTxnPercent;
	private String totalNBCancelledTxnPercent;
	private String totalNBInvalidTxnPercent;
	private String totalNBFraudTxnPercent;
	private String totalNBDroppedTxnPercent;
	private String totalNBRejectedTxnPercent;
	private String totalNBDeclinedTxnPercent;
	private String totalNBPendingTxnPercent;
	private String totalNBAcqDownTxnPercent;
	private String totalNBFailedAtAcqTxnPercent;
	private String totalNBAcqTimeOutTxnPercent;
	
	
	private String totalWLTxn;
	private String totalWLSuccessTxnPercent;
	private String totalWLFailedTxnPercent;
	private String totalWLCancelledTxnPercent;
	private String totalWLInvalidTxnPercent;
	private String totalWLFraudTxnPercent;
	private String totalWLDroppedTxnPercent;
	private String totalWLRejectedTxnPercent;
	private String totalWLDeclinedTxnPercent;
	private String totalWLPendingTxnPercent;
	private String totalWLAcqDownTxnPercent;
	private String totalWLFailedAtAcqTxnPercent;
	private String totalWLAcqTimeOutTxnPercent;
	
	private String totalEMTxn;
	private String totalEMSuccessTxnPercent;
	private String totalEMFailedTxnPercent;
	private String totalEMCancelledTxnPercent;
	private String totalEMInvalidTxnPercent;
	private String totalEMFraudTxnPercent;
	private String totalEMDroppedTxnPercent;
	private String totalEMRejectedTxnPercent;
	private String totalEMDeclinedTxnPercent;
	private String totalEMPendingTxnPercent;
	private String totalEMAcqDownTxnPercent;
	private String totalEMFailedAtAcqTxnPercent;
	private String totalEMAcqTimeOutTxnPercent;
	
	private String totalCDTxn;
	private String totalCDSuccessTxnPercent;
	private String totalCDFailedTxnPercent;
	private String totalCDCancelledTxnPercent;
	private String totalCDInvalidTxnPercent;
	private String totalCDFraudTxnPercent;
	private String totalCDDroppedTxnPercent;
	private String totalCDRejectedTxnPercent;
	private String totalCDDeclinedTxnPercent;
	private String totalCDPendingTxnPercent;
	private String totalCDAcqDownTxnPercent;
	private String totalCDFailedAtAcqTxnPercent;
	private String totalCDAcqTimeOutTxnPercent;
	
	private String totalCCTxnAmount;
	private String totalDCTxnAmount;
	private String totalUPTxnAmount;
	private String totalNBTxnAmount;
	private String totalWLTxnAmount;
	private String totalEMTxnAmount;
	private String totalCDTxnAmount;

	private String totalCCCapturedCount;
	private String totalDCCapturedCount;
	private String totalUPCapturedCount;
	private String totalNBCapturedCount;
	private String totalWLCapturedCount;
	private String totalEMCapturedCount;
	private String totalCDCapturedCount;	
	
	private String ccSettledPercentage;
	private String dcSettledPercentage;
	private String upSettledPercentage;
	private String nbSettledPercentage;
	private String wlSettledPercentage;
	private String emSettledPercentage;
	private String cdSettledPercentage;
	
	private String paymentGatewayProfitCumm;
	private String paymentGatewayProfitInclGstCumm;
	private String paymentGatewayProfitExcGstCumm;
	private String paymentGatewayProfit;
	private String paymentGatewayProfitInclGst;
	private String paymentGatewayProfitExcGst;
	private String paymentGatewayProfitAmount;
	
	private String dateSettled;
	private String dateCaptured;

	private String captured;
	private String failed;
	private String cancelled;
	private String invalid;
	private String fraud;
	private String dropped;
	private String rejected;
	private String declined;
	private String pending;
	private String acquirerDown;
	private String failedAtAcquirer;
	private String acquirerTimeOut;
	
	private String gst;
	
	
	private String capturedPercent;
	private String failedPercent;
	private String cancelledPercent;
	private String invalidPercent;
	private String fraudPercent;
	private String droppedPercent;
	private String rejectedPercent;
	private String declinedPercent;
	private String pendingPercent;
	private String acquirerDownPercent;
	private String failedAtAcquirerPercent;
	private String acquirerTimeOutPercent;
	
	private String unknownTxnCount;

	private String merchantPgRatio;
	private String acquirerPgRatio;

	private String avgSettlementAmount;
	private String merchantSaleSettledAmount;
	private String merchantRefundSettledAmount;

	private String totalProfit;
	private String postSettledTransactionCount;
	private String actualSettlementAmount;

	// For GST and Surcharge calculation
	private String surchargeFlag;
	private String payId;
	private String mopType;
	private String paymentType;
	private String txnType;
	private String paymentsRegion;
	private String cardHolderType;
	private String amount;
	private String paymentMethod;
	private String totalAmount;

	private String merchantName;
	private String saleCapturedAmount;
	private String saleCapturedCount;
	private String pgSaleSurcharge;
	private String acquirerSaleSurcharge;
	private String pgSaleGst;
	private String acquirerSaleGst;
	private String refundCapturedAmount;
	private String refundCapturedCount;
	private String pgRefundSurcharge;
	private String acquirerRefundSurcharge;
	private String pgRefundGst;
	private String acquirerRefundGst;
	private String totalMerchantAmount;
	private String acquirer;
	
	private String totalDomesticCapturedCount;
	private String totalIntenationalCapturedCount;
	private String totalDomesticCapturedPercentage;
	private String totalIntenationalCapturedPercentage;
	
	private String[] statusList; 
	private List performanceData = new ArrayList();
	
	
	
	public List getPerformanceData() {
		return performanceData;
	}
	public void setPerformanceData(List performanceData) {
		this.performanceData = performanceData;
	}
	public String[] getStatusList() {
		return statusList;
	}
	public void setStatusList(String[] statusList) {
		this.statusList = statusList;
	}
	public String getTotalTxnCount() {
		return totalTxnCount;
	}
	public void setTotalTxnCount(String totalTxnCount) {
		this.totalTxnCount = totalTxnCount;
	}
	public String getTotalCapturedTxnAmount() {
		return totalCapturedTxnAmount;
	}
	public void setTotalCapturedTxnAmount(String totalCapturedTxnAmount) {
		this.totalCapturedTxnAmount = totalCapturedTxnAmount;
	}
	public String getTotalRejectedTxnPercent() {
		return totalRejectedTxnPercent;
	}
	public void setTotalRejectedTxnPercent(String totalRejectedTxnPercent) {
		this.totalRejectedTxnPercent = totalRejectedTxnPercent;
	}
	public String getSuccessTxnCount() {
		return successTxnCount;
	}
	public void setSuccessTxnCount(String successTxnCount) {
		this.successTxnCount = successTxnCount;
	}
	public String getSuccessTxnPercent() {
		return successTxnPercent;
	}
	public void setSuccessTxnPercent(String successTxnPercent) {
		this.successTxnPercent = successTxnPercent;
	}
	public String getFailedTxnCount() {
		return failedTxnCount;
	}
	public void setFailedTxnCount(String failedTxnCount) {
		this.failedTxnCount = failedTxnCount;
	}
	public String getAvgTkt() {
		return avgTkt;
	}
	public void setAvgTkt(String avgTkt) {
		this.avgTkt = avgTkt;
	}
	public String getCCTxnPercent() {
		return CCTxnPercent;
	}
	public void setCCTxnPercent(String cCTxnPercent) {
		CCTxnPercent = cCTxnPercent;
	}
	public String getCCSuccessRate() {
		return CCSuccessRate;
	}
	public void setCCSuccessRate(String cCSuccessRate) {
		CCSuccessRate = cCSuccessRate;
	}
	public String getDCTxnPercent() {
		return DCTxnPercent;
	}
	public void setDCTxnPercent(String dCTxnPercent) {
		DCTxnPercent = dCTxnPercent;
	}
	public String getDCSuccessRate() {
		return DCSuccessRate;
	}
	public void setDCSuccessRate(String dCSuccessRate) {
		DCSuccessRate = dCSuccessRate;
	}
	public String getUPTxnPercent() {
		return UPTxnPercent;
	}
	public void setUPTxnPercent(String uPTxnPercent) {
		UPTxnPercent = uPTxnPercent;
	}
	public String getUPSuccessRate() {
		return UPSuccessRate;
	}
	public void setUPSuccessRate(String uPSuccessRate) {
		UPSuccessRate = uPSuccessRate;
	}
	public String getNBTxnPercent() {
		return NBTxnPercent;
	}
	public void setNBTxnPercent(String nBTxnPercent) {
		NBTxnPercent = nBTxnPercent;
	}
	public String getNBSuccessRate() {
		return NBSuccessRate;
	}
	public void setNBSuccessRate(String nBSuccessRate) {
		NBSuccessRate = nBSuccessRate;
	}
	public String getWLTxnPercent() {
		return WLTxnPercent;
	}
	public void setWLTxnPercent(String wLTxnPercent) {
		WLTxnPercent = wLTxnPercent;
	}
	public String getWLSuccessRate() {
		return WLSuccessRate;
	}
	public void setWLSuccessRate(String wLSuccessRate) {
		WLSuccessRate = wLSuccessRate;
	}
	public String getEMTxnPercent() {
		return EMTxnPercent;
	}
	public void setEMTxnPercent(String eMTxnPercent) {
		EMTxnPercent = eMTxnPercent;
	}
	public String getEMSuccessRate() {
		return EMSuccessRate;
	}
	public void setEMSuccessRate(String eMSuccessRate) {
		EMSuccessRate = eMSuccessRate;
	}
	public String getCDTxnPercent() {
		return CDTxnPercent;
	}
	public void setCDTxnPercent(String cDTxnPercent) {
		CDTxnPercent = cDTxnPercent;
	}
	public String getCDSuccessRate() {
		return CDSuccessRate;
	}
	public void setCDSuccessRate(String cDSuccessRate) {
		CDSuccessRate = cDSuccessRate;
	}
	public String getTotalCCTxn() {
		return totalCCTxn;
	}
	public void setTotalCCTxn(String totalCCTxn) {
		this.totalCCTxn = totalCCTxn;
	}
	public String getTotalCCSuccessTxnPercent() {
		return totalCCSuccessTxnPercent;
	}
	public void setTotalCCSuccessTxnPercent(String totalCCSuccessTxnPercent) {
		this.totalCCSuccessTxnPercent = totalCCSuccessTxnPercent;
	}
	public String getTotalCCFailedTxnPercent() {
		return totalCCFailedTxnPercent;
	}
	public void setTotalCCFailedTxnPercent(String totalCCFailedTxnPercent) {
		this.totalCCFailedTxnPercent = totalCCFailedTxnPercent;
	}
	public String getTotalCCCancelledTxnPercent() {
		return totalCCCancelledTxnPercent;
	}
	public void setTotalCCCancelledTxnPercent(String totalCCCancelledTxnPercent) {
		this.totalCCCancelledTxnPercent = totalCCCancelledTxnPercent;
	}
	public String getTotalCCInvalidTxnPercent() {
		return totalCCInvalidTxnPercent;
	}
	public void setTotalCCInvalidTxnPercent(String totalCCInvalidTxnPercent) {
		this.totalCCInvalidTxnPercent = totalCCInvalidTxnPercent;
	}
	public String getTotalCCFraudTxnPercent() {
		return totalCCFraudTxnPercent;
	}
	public void setTotalCCFraudTxnPercent(String totalCCFraudTxnPercent) {
		this.totalCCFraudTxnPercent = totalCCFraudTxnPercent;
	}
	public String getTotalCCDroppedTxnPercent() {
		return totalCCDroppedTxnPercent;
	}
	public void setTotalCCDroppedTxnPercent(String totalCCDroppedTxnPercent) {
		this.totalCCDroppedTxnPercent = totalCCDroppedTxnPercent;
	}
	public String getTotalCCRejectedTxnPercent() {
		return totalCCRejectedTxnPercent;
	}
	public void setTotalCCRejectedTxnPercent(String totalCCRejectedTxnPercent) {
		this.totalCCRejectedTxnPercent = totalCCRejectedTxnPercent;
	}
	
	public String getTotalCCDeclinedTxnPercent() {
		return totalCCDeclinedTxnPercent;
	}
	public void setTotalCCDeclinedTxnPercent(String totalCCDeclinedTxnPercent) {
		this.totalCCDeclinedTxnPercent = totalCCDeclinedTxnPercent;
	}
	public String getTotalCCPendingTxnPercent() {
		return totalCCPendingTxnPercent;
	}
	public void setTotalCCPendingTxnPercent(String totalCCPendingTxnPercent) {
		this.totalCCPendingTxnPercent = totalCCPendingTxnPercent;
	}
	public String getTotalCCAcqDownTxnPercent() {
		return totalCCAcqDownTxnPercent;
	}
	public void setTotalCCAcqDownTxnPercent(String totalCCAcqDownTxnPercent) {
		this.totalCCAcqDownTxnPercent = totalCCAcqDownTxnPercent;
	}
	public String getTotalCCFailedAtAcqTxnPercent() {
		return totalCCFailedAtAcqTxnPercent;
	}
	public void setTotalCCFailedAtAcqTxnPercent(String totalCCFailedAtAcqTxnPercent) {
		this.totalCCFailedAtAcqTxnPercent = totalCCFailedAtAcqTxnPercent;
	}
	public String getTotalCCAcqTimeOutTxnPercent() {
		return totalCCAcqTimeOutTxnPercent;
	}
	public void setTotalCCAcqTimeOutTxnPercent(String totalCCAcqTimeOutTxnPercent) {
		this.totalCCAcqTimeOutTxnPercent = totalCCAcqTimeOutTxnPercent;
	}
	
	public String getTotalDCTxn() {
		return totalDCTxn;
	}
	public void setTotalDCTxn(String totalDCTxn) {
		this.totalDCTxn = totalDCTxn;
	}
	public String getTotalDCSuccessTxnPercent() {
		return totalDCSuccessTxnPercent;
	}
	public void setTotalDCSuccessTxnPercent(String totalDCSuccessTxnPercent) {
		this.totalDCSuccessTxnPercent = totalDCSuccessTxnPercent;
	}
	public String getTotalDCFailedTxnPercent() {
		return totalDCFailedTxnPercent;
	}
	public void setTotalDCFailedTxnPercent(String totalDCFailedTxnPercent) {
		this.totalDCFailedTxnPercent = totalDCFailedTxnPercent;
	}
	public String getTotalDCCancelledTxnPercent() {
		return totalDCCancelledTxnPercent;
	}
	public void setTotalDCCancelledTxnPercent(String totalDCCancelledTxnPercent) {
		this.totalDCCancelledTxnPercent = totalDCCancelledTxnPercent;
	}
	public String getTotalDCInvalidTxnPercent() {
		return totalDCInvalidTxnPercent;
	}
	public void setTotalDCInvalidTxnPercent(String totalDCInvalidTxnPercent) {
		this.totalDCInvalidTxnPercent = totalDCInvalidTxnPercent;
	}
	public String getTotalDCFraudTxnPercent() {
		return totalDCFraudTxnPercent;
	}
	public void setTotalDCFraudTxnPercent(String totalDCFraudTxnPercent) {
		this.totalDCFraudTxnPercent = totalDCFraudTxnPercent;
	}
	public String getTotalDCDroppedTxnPercent() {
		return totalDCDroppedTxnPercent;
	}
	public void setTotalDCDroppedTxnPercent(String totalDCDroppedTxnPercent) {
		this.totalDCDroppedTxnPercent = totalDCDroppedTxnPercent;
	}
	public String getTotalDCRejectedTxnPercent() {
		return totalDCRejectedTxnPercent;
	}
	public void setTotalDCRejectedTxnPercent(String totalDCRejectedTxnPercent) {
		this.totalDCRejectedTxnPercent = totalDCRejectedTxnPercent;
	}
	public String getTotalUPTxn() {
		return totalUPTxn;
	}
	public void setTotalUPTxn(String totalUPTxn) {
		this.totalUPTxn = totalUPTxn;
	}
	public String getTotalUPSuccessTxnPercent() {
		return totalUPSuccessTxnPercent;
	}
	public void setTotalUPSuccessTxnPercent(String totalUPSuccessTxnPercent) {
		this.totalUPSuccessTxnPercent = totalUPSuccessTxnPercent;
	}
	public String getTotalUPFailedTxnPercent() {
		return totalUPFailedTxnPercent;
	}
	public void setTotalUPFailedTxnPercent(String totalUPFailedTxnPercent) {
		this.totalUPFailedTxnPercent = totalUPFailedTxnPercent;
	}
	public String getTotalUPCancelledTxnPercent() {
		return totalUPCancelledTxnPercent;
	}
	public void setTotalUPCancelledTxnPercent(String totalUPCancelledTxnPercent) {
		this.totalUPCancelledTxnPercent = totalUPCancelledTxnPercent;
	}
	public String getTotalUPInvalidTxnPercent() {
		return totalUPInvalidTxnPercent;
	}
	public void setTotalUPInvalidTxnPercent(String totalUPInvalidTxnPercent) {
		this.totalUPInvalidTxnPercent = totalUPInvalidTxnPercent;
	}
	public String getTotalUPFraudTxnPercent() {
		return totalUPFraudTxnPercent;
	}
	public void setTotalUPFraudTxnPercent(String totalUPFraudTxnPercent) {
		this.totalUPFraudTxnPercent = totalUPFraudTxnPercent;
	}
	public String getTotalUPDroppedTxnPercent() {
		return totalUPDroppedTxnPercent;
	}
	public void setTotalUPDroppedTxnPercent(String totalUPDroppedTxnPercent) {
		this.totalUPDroppedTxnPercent = totalUPDroppedTxnPercent;
	}
	public String getTotalUPRejectedTxnPercent() {
		return totalUPRejectedTxnPercent;
	}
	public void setTotalUPRejectedTxnPercent(String totalUPRejectedTxnPercent) {
		this.totalUPRejectedTxnPercent = totalUPRejectedTxnPercent;
	}
	public String getTotalNBTxn() {
		return totalNBTxn;
	}
	public void setTotalNBTxn(String totalNBTxn) {
		this.totalNBTxn = totalNBTxn;
	}
	public String getTotalNBSuccessTxnPercent() {
		return totalNBSuccessTxnPercent;
	}
	public void setTotalNBSuccessTxnPercent(String totalNBSuccessTxnPercent) {
		this.totalNBSuccessTxnPercent = totalNBSuccessTxnPercent;
	}
	public String getTotalNBFailedTxnPercent() {
		return totalNBFailedTxnPercent;
	}
	public void setTotalNBFailedTxnPercent(String totalNBFailedTxnPercent) {
		this.totalNBFailedTxnPercent = totalNBFailedTxnPercent;
	}
	public String getTotalNBCancelledTxnPercent() {
		return totalNBCancelledTxnPercent;
	}
	public void setTotalNBCancelledTxnPercent(String totalNBCancelledTxnPercent) {
		this.totalNBCancelledTxnPercent = totalNBCancelledTxnPercent;
	}
	public String getTotalNBInvalidTxnPercent() {
		return totalNBInvalidTxnPercent;
	}
	public void setTotalNBInvalidTxnPercent(String totalNBInvalidTxnPercent) {
		this.totalNBInvalidTxnPercent = totalNBInvalidTxnPercent;
	}
	public String getTotalNBFraudTxnPercent() {
		return totalNBFraudTxnPercent;
	}
	public void setTotalNBFraudTxnPercent(String totalNBFraudTxnPercent) {
		this.totalNBFraudTxnPercent = totalNBFraudTxnPercent;
	}
	public String getTotalNBDroppedTxnPercent() {
		return totalNBDroppedTxnPercent;
	}
	public void setTotalNBDroppedTxnPercent(String totalNBDroppedTxnPercent) {
		this.totalNBDroppedTxnPercent = totalNBDroppedTxnPercent;
	}
	public String getTotalNBRejectedTxnPercent() {
		return totalNBRejectedTxnPercent;
	}
	public void setTotalNBRejectedTxnPercent(String totalNBRejectedTxnPercent) {
		this.totalNBRejectedTxnPercent = totalNBRejectedTxnPercent;
	}
	public String getTotalWLTxn() {
		return totalWLTxn;
	}
	public void setTotalWLTxn(String totalWLTxn) {
		this.totalWLTxn = totalWLTxn;
	}
	public String getTotalWLSuccessTxnPercent() {
		return totalWLSuccessTxnPercent;
	}
	public void setTotalWLSuccessTxnPercent(String totalWLSuccessTxnPercent) {
		this.totalWLSuccessTxnPercent = totalWLSuccessTxnPercent;
	}
	public String getTotalWLFailedTxnPercent() {
		return totalWLFailedTxnPercent;
	}
	public void setTotalWLFailedTxnPercent(String totalWLFailedTxnPercent) {
		this.totalWLFailedTxnPercent = totalWLFailedTxnPercent;
	}
	public String getTotalWLCancelledTxnPercent() {
		return totalWLCancelledTxnPercent;
	}
	public void setTotalWLCancelledTxnPercent(String totalWLCancelledTxnPercent) {
		this.totalWLCancelledTxnPercent = totalWLCancelledTxnPercent;
	}
	public String getTotalWLInvalidTxnPercent() {
		return totalWLInvalidTxnPercent;
	}
	public void setTotalWLInvalidTxnPercent(String totalWLInvalidTxnPercent) {
		this.totalWLInvalidTxnPercent = totalWLInvalidTxnPercent;
	}
	public String getTotalWLFraudTxnPercent() {
		return totalWLFraudTxnPercent;
	}
	public void setTotalWLFraudTxnPercent(String totalWLFraudTxnPercent) {
		this.totalWLFraudTxnPercent = totalWLFraudTxnPercent;
	}
	public String getTotalWLDroppedTxnPercent() {
		return totalWLDroppedTxnPercent;
	}
	public void setTotalWLDroppedTxnPercent(String totalWLDroppedTxnPercent) {
		this.totalWLDroppedTxnPercent = totalWLDroppedTxnPercent;
	}
	public String getTotalWLRejectedTxnPercent() {
		return totalWLRejectedTxnPercent;
	}
	public void setTotalWLRejectedTxnPercent(String totalWLRejectedTxnPercent) {
		this.totalWLRejectedTxnPercent = totalWLRejectedTxnPercent;
	}
	public String getTotalEMTxn() {
		return totalEMTxn;
	}
	public void setTotalEMTxn(String totalEMTxn) {
		this.totalEMTxn = totalEMTxn;
	}
	public String getTotalEMSuccessTxnPercent() {
		return totalEMSuccessTxnPercent;
	}
	public void setTotalEMSuccessTxnPercent(String totalEMSuccessTxnPercent) {
		this.totalEMSuccessTxnPercent = totalEMSuccessTxnPercent;
	}
	public String getTotalEMFailedTxnPercent() {
		return totalEMFailedTxnPercent;
	}
	public void setTotalEMFailedTxnPercent(String totalEMFailedTxnPercent) {
		this.totalEMFailedTxnPercent = totalEMFailedTxnPercent;
	}
	public String getTotalEMCancelledTxnPercent() {
		return totalEMCancelledTxnPercent;
	}
	public void setTotalEMCancelledTxnPercent(String totalEMCancelledTxnPercent) {
		this.totalEMCancelledTxnPercent = totalEMCancelledTxnPercent;
	}
	public String getTotalEMInvalidTxnPercent() {
		return totalEMInvalidTxnPercent;
	}
	public void setTotalEMInvalidTxnPercent(String totalEMInvalidTxnPercent) {
		this.totalEMInvalidTxnPercent = totalEMInvalidTxnPercent;
	}
	public String getTotalEMFraudTxnPercent() {
		return totalEMFraudTxnPercent;
	}
	public void setTotalEMFraudTxnPercent(String totalEMFraudTxnPercent) {
		this.totalEMFraudTxnPercent = totalEMFraudTxnPercent;
	}
	public String getTotalEMDroppedTxnPercent() {
		return totalEMDroppedTxnPercent;
	}
	public void setTotalEMDroppedTxnPercent(String totalEMDroppedTxnPercent) {
		this.totalEMDroppedTxnPercent = totalEMDroppedTxnPercent;
	}
	public String getTotalEMRejectedTxnPercent() {
		return totalEMRejectedTxnPercent;
	}
	public void setTotalEMRejectedTxnPercent(String totalEMRejectedTxnPercent) {
		this.totalEMRejectedTxnPercent = totalEMRejectedTxnPercent;
	}
	public String getTotalCDTxn() {
		return totalCDTxn;
	}
	public void setTotalCDTxn(String totalCDTxn) {
		this.totalCDTxn = totalCDTxn;
	}
	public String getTotalCDSuccessTxnPercent() {
		return totalCDSuccessTxnPercent;
	}
	public void setTotalCDSuccessTxnPercent(String totalCDSuccessTxnPercent) {
		this.totalCDSuccessTxnPercent = totalCDSuccessTxnPercent;
	}
	public String getTotalCDFailedTxnPercent() {
		return totalCDFailedTxnPercent;
	}
	public void setTotalCDFailedTxnPercent(String totalCDFailedTxnPercent) {
		this.totalCDFailedTxnPercent = totalCDFailedTxnPercent;
	}
	public String getTotalCDCancelledTxnPercent() {
		return totalCDCancelledTxnPercent;
	}
	public void setTotalCDCancelledTxnPercent(String totalCDCancelledTxnPercent) {
		this.totalCDCancelledTxnPercent = totalCDCancelledTxnPercent;
	}
	public String getTotalCDInvalidTxnPercent() {
		return totalCDInvalidTxnPercent;
	}
	public void setTotalCDInvalidTxnPercent(String totalCDInvalidTxnPercent) {
		this.totalCDInvalidTxnPercent = totalCDInvalidTxnPercent;
	}
	public String getTotalCDFraudTxnPercent() {
		return totalCDFraudTxnPercent;
	}
	public void setTotalCDFraudTxnPercent(String totalCDFraudTxnPercent) {
		this.totalCDFraudTxnPercent = totalCDFraudTxnPercent;
	}
	public String getTotalCDDroppedTxnPercent() {
		return totalCDDroppedTxnPercent;
	}
	public void setTotalCDDroppedTxnPercent(String totalCDDroppedTxnPercent) {
		this.totalCDDroppedTxnPercent = totalCDDroppedTxnPercent;
	}
	public String getTotalCDRejectedTxnPercent() {
		return totalCDRejectedTxnPercent;
	}
	public void setTotalCDRejectedTxnPercent(String totalCDRejectedTxnPercent) {
		this.totalCDRejectedTxnPercent = totalCDRejectedTxnPercent;
	}
	public String getTotalCCTxnAmount() {
		return totalCCTxnAmount;
	}
	public void setTotalCCTxnAmount(String totalCCTxnAmount) {
		this.totalCCTxnAmount = totalCCTxnAmount;
	}
	public String getTotalDCTxnAmount() {
		return totalDCTxnAmount;
	}
	public void setTotalDCTxnAmount(String totalDCTxnAmount) {
		this.totalDCTxnAmount = totalDCTxnAmount;
	}
	public String getTotalUPTxnAmount() {
		return totalUPTxnAmount;
	}
	public void setTotalUPTxnAmount(String totalUPTxnAmount) {
		this.totalUPTxnAmount = totalUPTxnAmount;
	}
	public String getTotalNBTxnAmount() {
		return totalNBTxnAmount;
	}
	public void setTotalNBTxnAmount(String totalNBTxnAmount) {
		this.totalNBTxnAmount = totalNBTxnAmount;
	}
	public String getTotalWLTxnAmount() {
		return totalWLTxnAmount;
	}
	public void setTotalWLTxnAmount(String totalWLTxnAmount) {
		this.totalWLTxnAmount = totalWLTxnAmount;
	}
	public String getTotalEMTxnAmount() {
		return totalEMTxnAmount;
	}
	public void setTotalEMTxnAmount(String totalEMTxnAmount) {
		this.totalEMTxnAmount = totalEMTxnAmount;
	}
	public String getTotalCDTxnAmount() {
		return totalCDTxnAmount;
	}
	public void setTotalCDTxnAmount(String totalCDTxnAmount) {
		this.totalCDTxnAmount = totalCDTxnAmount;
	}
	public String getTotalCCCapturedCount() {
		return totalCCCapturedCount;
	}
	public void setTotalCCCapturedCount(String totalCCCapturedCount) {
		this.totalCCCapturedCount = totalCCCapturedCount;
	}
	public String getTotalDCCapturedCount() {
		return totalDCCapturedCount;
	}
	public void setTotalDCCapturedCount(String totalDCCapturedCount) {
		this.totalDCCapturedCount = totalDCCapturedCount;
	}
	public String getTotalUPCapturedCount() {
		return totalUPCapturedCount;
	}
	public void setTotalUPCapturedCount(String totalUPCapturedCount) {
		this.totalUPCapturedCount = totalUPCapturedCount;
	}
	public String getTotalNBCapturedCount() {
		return totalNBCapturedCount;
	}
	public void setTotalNBCapturedCount(String totalNBCapturedCount) {
		this.totalNBCapturedCount = totalNBCapturedCount;
	}
	public String getTotalWLCapturedCount() {
		return totalWLCapturedCount;
	}
	public void setTotalWLCapturedCount(String totalWLCapturedCount) {
		this.totalWLCapturedCount = totalWLCapturedCount;
	}
	public String getTotalEMCapturedCount() {
		return totalEMCapturedCount;
	}
	public void setTotalEMCapturedCount(String totalEMCapturedCount) {
		this.totalEMCapturedCount = totalEMCapturedCount;
	}
	public String getTotalCDCapturedCount() {
		return totalCDCapturedCount;
	}
	public void setTotalCDCapturedCount(String totalCDCapturedCount) {
		this.totalCDCapturedCount = totalCDCapturedCount;
	}
	public String getCcSettledPercentage() {
		return ccSettledPercentage;
	}
	public void setCcSettledPercentage(String ccSettledPercentage) {
		this.ccSettledPercentage = ccSettledPercentage;
	}
	public String getDcSettledPercentage() {
		return dcSettledPercentage;
	}
	public void setDcSettledPercentage(String dcSettledPercentage) {
		this.dcSettledPercentage = dcSettledPercentage;
	}
	public String getUpSettledPercentage() {
		return upSettledPercentage;
	}
	public void setUpSettledPercentage(String upSettledPercentage) {
		this.upSettledPercentage = upSettledPercentage;
	}
	public String getNbSettledPercentage() {
		return nbSettledPercentage;
	}
	public void setNbSettledPercentage(String nbSettledPercentage) {
		this.nbSettledPercentage = nbSettledPercentage;
	}
	public String getWlSettledPercentage() {
		return wlSettledPercentage;
	}
	public void setWlSettledPercentage(String wlSettledPercentage) {
		this.wlSettledPercentage = wlSettledPercentage;
	}
	public String getEmSettledPercentage() {
		return emSettledPercentage;
	}
	public void setEmSettledPercentage(String emSettledPercentage) {
		this.emSettledPercentage = emSettledPercentage;
	}
	public String getCdSettledPercentage() {
		return cdSettledPercentage;
	}
	public void setCdSettledPercentage(String cdSettledPercentage) {
		this.cdSettledPercentage = cdSettledPercentage;
	}
	public String getpaymentGatewayProfitCumm() {
		return paymentGatewayProfitCumm;
	}
	public void setpaymentGatewayProfitCumm(String paymentGatewayProfitCumm) {
		this.paymentGatewayProfitCumm = paymentGatewayProfitCumm;
	}
	public String getpaymentGatewayProfitInclGstCumm() {
		return paymentGatewayProfitInclGstCumm;
	}
	public void setpaymentGatewayProfitInclGstCumm(String paymentGatewayProfitInclGstCumm) {
		this.paymentGatewayProfitInclGstCumm = paymentGatewayProfitInclGstCumm;
	}
	public String getpaymentGatewayProfitExcGstCumm() {
		return paymentGatewayProfitExcGstCumm;
	}
	public void setpaymentGatewayProfitExcGstCumm(String paymentGatewayProfitExcGstCumm) {
		this.paymentGatewayProfitExcGstCumm = paymentGatewayProfitExcGstCumm;
	}
	public String getpaymentGatewayProfit() {
		return paymentGatewayProfit;
	}
	public void setpaymentGatewayProfit(String paymentGatewayProfit) {
		this.paymentGatewayProfit = paymentGatewayProfit;
	}
	public String getpaymentGatewayProfitInclGst() {
		return paymentGatewayProfitInclGst;
	}
	public void setpaymentGatewayProfitInclGst(String paymentGatewayProfitInclGst) {
		this.paymentGatewayProfitInclGst = paymentGatewayProfitInclGst;
	}
	public String getpaymentGatewayProfitExcGst() {
		return paymentGatewayProfitExcGst;
	}
	public void setpaymentGatewayProfitExcGst(String paymentGatewayProfitExcGst) {
		this.paymentGatewayProfitExcGst = paymentGatewayProfitExcGst;
	}
	public String getpaymentGatewayProfitAmount() {
		return paymentGatewayProfitAmount;
	}
	public void setpaymentGatewayProfitAmount(String paymentGatewayProfitAmount) {
		this.paymentGatewayProfitAmount = paymentGatewayProfitAmount;
	}
	public String getDateSettled() {
		return dateSettled;
	}
	public void setDateSettled(String dateSettled) {
		this.dateSettled = dateSettled;
	}
	public String getDateCaptured() {
		return dateCaptured;
	}
	public void setDateCaptured(String dateCaptured) {
		this.dateCaptured = dateCaptured;
	}
	public String getCaptured() {
		return captured;
	}
	public void setCaptured(String captured) {
		this.captured = captured;
	}
	public String getFailed() {
		return failed;
	}
	public void setFailed(String failed) {
		this.failed = failed;
	}
	public String getCancelled() {
		return cancelled;
	}
	public void setCancelled(String cancelled) {
		this.cancelled = cancelled;
	}
	public String getInvalid() {
		return invalid;
	}
	public void setInvalid(String invalid) {
		this.invalid = invalid;
	}
	public String getFraud() {
		return fraud;
	}
	public void setFraud(String fraud) {
		this.fraud = fraud;
	}
	public String getDropped() {
		return dropped;
	}
	public void setDropped(String dropped) {
		this.dropped = dropped;
	}
	public String getRejected() {
		return rejected;
	}
	public void setRejected(String rejected) {
		this.rejected = rejected;
	}
	
	public String getDeclined() {
		return declined;
	}
	public void setDeclined(String declined) {
		this.declined = declined;
	}
	public String getPending() {
		return pending;
	}
	public void setPending(String pending) {
		this.pending = pending;
	}
	public String getAcquirerDown() {
		return acquirerDown;
	}
	public void setAcquirerDown(String acquirerDown) {
		this.acquirerDown = acquirerDown;
	}
	public String getFailedAtAcquirer() {
		return failedAtAcquirer;
	}
	public void setFailedAtAcquirer(String failedAtAcquirer) {
		this.failedAtAcquirer = failedAtAcquirer;
	}
	public String getAcquirerTimeOut() {
		return acquirerTimeOut;
	}
	public void setAcquirerTimeOut(String acquirerTimeOut) {
		this.acquirerTimeOut = acquirerTimeOut;
	}
	public String getGst() {
		return gst;
	}
	public void setGst(String gst) {
		this.gst = gst;
	}
	public String getCapturedPercent() {
		return capturedPercent;
	}
	public void setCapturedPercent(String capturedPercent) {
		this.capturedPercent = capturedPercent;
	}
	public String getFailedPercent() {
		return failedPercent;
	}
	public void setFailedPercent(String failedPercent) {
		this.failedPercent = failedPercent;
	}
	public String getCancelledPercent() {
		return cancelledPercent;
	}
	public void setCancelledPercent(String cancelledPercent) {
		this.cancelledPercent = cancelledPercent;
	}
	public String getInvalidPercent() {
		return invalidPercent;
	}
	public void setInvalidPercent(String invalidPercent) {
		this.invalidPercent = invalidPercent;
	}
	public String getFraudPercent() {
		return fraudPercent;
	}
	public void setFraudPercent(String fraudPercent) {
		this.fraudPercent = fraudPercent;
	}
	public String getDroppedPercent() {
		return droppedPercent;
	}
	public void setDroppedPercent(String droppedPercent) {
		this.droppedPercent = droppedPercent;
	}
	public String getRejectedPercent() {
		return rejectedPercent;
	}
	public void setRejectedPercent(String rejectedPercent) {
		this.rejectedPercent = rejectedPercent;
	}
	public String getUnknownTxnCount() {
		return unknownTxnCount;
	}
	public void setUnknownTxnCount(String unknownTxnCount) {
		this.unknownTxnCount = unknownTxnCount;
	}
	public String getMerchantPgRatio() {
		return merchantPgRatio;
	}
	public void setMerchantPgRatio(String merchantPgRatio) {
		this.merchantPgRatio = merchantPgRatio;
	}
	public String getAcquirerPgRatio() {
		return acquirerPgRatio;
	}
	public void setAcquirerPgRatio(String acquirerPgRatio) {
		this.acquirerPgRatio = acquirerPgRatio;
	}
	public String getAvgSettlementAmount() {
		return avgSettlementAmount;
	}
	public void setAvgSettlementAmount(String avgSettlementAmount) {
		this.avgSettlementAmount = avgSettlementAmount;
	}
	public String getMerchantSaleSettledAmount() {
		return merchantSaleSettledAmount;
	}
	public void setMerchantSaleSettledAmount(String merchantSaleSettledAmount) {
		this.merchantSaleSettledAmount = merchantSaleSettledAmount;
	}
	public String getMerchantRefundSettledAmount() {
		return merchantRefundSettledAmount;
	}
	public void setMerchantRefundSettledAmount(String merchantRefundSettledAmount) {
		this.merchantRefundSettledAmount = merchantRefundSettledAmount;
	}
	public String getTotalProfit() {
		return totalProfit;
	}
	public void setTotalProfit(String totalProfit) {
		this.totalProfit = totalProfit;
	}
	public String getPostSettledTransactionCount() {
		return postSettledTransactionCount;
	}
	public void setPostSettledTransactionCount(String postSettledTransactionCount) {
		this.postSettledTransactionCount = postSettledTransactionCount;
	}
	public String getActualSettlementAmount() {
		return actualSettlementAmount;
	}
	public void setActualSettlementAmount(String actualSettlementAmount) {
		this.actualSettlementAmount = actualSettlementAmount;
	}
	public String getSurchargeFlag() {
		return surchargeFlag;
	}
	public void setSurchargeFlag(String surchargeFlag) {
		this.surchargeFlag = surchargeFlag;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getMopType() {
		return mopType;
	}
	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public String getPaymentsRegion() {
		return paymentsRegion;
	}
	public void setPaymentsRegion(String paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}
	public String getCardHolderType() {
		return cardHolderType;
	}
	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public String getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getSaleCapturedAmount() {
		return saleCapturedAmount;
	}
	public void setSaleCapturedAmount(String saleCapturedAmount) {
		this.saleCapturedAmount = saleCapturedAmount;
	}
	public String getSaleCapturedCount() {
		return saleCapturedCount;
	}
	public void setSaleCapturedCount(String saleCapturedCount) {
		this.saleCapturedCount = saleCapturedCount;
	}
	public String getPgSaleSurcharge() {
		return pgSaleSurcharge;
	}
	public void setPgSaleSurcharge(String pgSaleSurcharge) {
		this.pgSaleSurcharge = pgSaleSurcharge;
	}
	public String getAcquirerSaleSurcharge() {
		return acquirerSaleSurcharge;
	}
	public void setAcquirerSaleSurcharge(String acquirerSaleSurcharge) {
		this.acquirerSaleSurcharge = acquirerSaleSurcharge;
	}
	public String getPgSaleGst() {
		return pgSaleGst;
	}
	public void setPgSaleGst(String pgSaleGst) {
		this.pgSaleGst = pgSaleGst;
	}
	public String getAcquirerSaleGst() {
		return acquirerSaleGst;
	}
	public void setAcquirerSaleGst(String acquirerSaleGst) {
		this.acquirerSaleGst = acquirerSaleGst;
	}
	public String getRefundCapturedAmount() {
		return refundCapturedAmount;
	}
	public void setRefundCapturedAmount(String refundCapturedAmount) {
		this.refundCapturedAmount = refundCapturedAmount;
	}
	public String getRefundCapturedCount() {
		return refundCapturedCount;
	}
	public void setRefundCapturedCount(String refundCapturedCount) {
		this.refundCapturedCount = refundCapturedCount;
	}
	public String getPgRefundSurcharge() {
		return pgRefundSurcharge;
	}
	public void setPgRefundSurcharge(String pgRefundSurcharge) {
		this.pgRefundSurcharge = pgRefundSurcharge;
	}
	public String getAcquirerRefundSurcharge() {
		return acquirerRefundSurcharge;
	}
	public void setAcquirerRefundSurcharge(String acquirerRefundSurcharge) {
		this.acquirerRefundSurcharge = acquirerRefundSurcharge;
	}
	public String getPgRefundGst() {
		return pgRefundGst;
	}
	public void setPgRefundGst(String pgRefundGst) {
		this.pgRefundGst = pgRefundGst;
	}
	public String getAcquirerRefundGst() {
		return acquirerRefundGst;
	}
	public void setAcquirerRefundGst(String acquirerRefundGst) {
		this.acquirerRefundGst = acquirerRefundGst;
	}
	public String getTotalMerchantAmount() {
		return totalMerchantAmount;
	}
	public void setTotalMerchantAmount(String totalMerchantAmount) {
		this.totalMerchantAmount = totalMerchantAmount;
	}
	public String getAcquirer() {
		return acquirer;
	}
	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}
	public String getTotalDomesticCapturedCount() {
		return totalDomesticCapturedCount;
	}
	public void setTotalDomesticCapturedCount(String totalDomesticCapturedCount) {
		this.totalDomesticCapturedCount = totalDomesticCapturedCount;
	}
	public String getTotalIntenationalCapturedCount() {
		return totalIntenationalCapturedCount;
	}
	public void setTotalIntenationalCapturedCount(String totalIntenationalCapturedCount) {
		this.totalIntenationalCapturedCount = totalIntenationalCapturedCount;
	}
	public String getTotalDomesticCapturedPercentage() {
		return totalDomesticCapturedPercentage;
	}
	public void setTotalDomesticCapturedPercentage(String totalDomesticCapturedPercentage) {
		this.totalDomesticCapturedPercentage = totalDomesticCapturedPercentage;
	}
	public String getTotalIntenationalCapturedPercentage() {
		return totalIntenationalCapturedPercentage;
	}
	public void setTotalIntenationalCapturedPercentage(String totalIntenationalCapturedPercentage) {
		this.totalIntenationalCapturedPercentage = totalIntenationalCapturedPercentage;
	}
	public String getTotalDCDeclinedTxnPercent() {
		return totalDCDeclinedTxnPercent;
	}
	public void setTotalDCDeclinedTxnPercent(String totalDCDeclinedTxnPercent) {
		this.totalDCDeclinedTxnPercent = totalDCDeclinedTxnPercent;
	}
	public String getTotalDCPendingTxnPercent() {
		return totalDCPendingTxnPercent;
	}
	public void setTotalDCPendingTxnPercent(String totalDCPendingTxnPercent) {
		this.totalDCPendingTxnPercent = totalDCPendingTxnPercent;
	}
	public String getTotalDCAcqDownTxnPercent() {
		return totalDCAcqDownTxnPercent;
	}
	public void setTotalDCAcqDownTxnPercent(String totalDCAcqDownTxnPercent) {
		this.totalDCAcqDownTxnPercent = totalDCAcqDownTxnPercent;
	}
	public String getTotalDCFailedAtAcqTxnPercent() {
		return totalDCFailedAtAcqTxnPercent;
	}
	public void setTotalDCFailedAtAcqTxnPercent(String totalDCFailedAtAcqTxnPercent) {
		this.totalDCFailedAtAcqTxnPercent = totalDCFailedAtAcqTxnPercent;
	}
	public String getTotalDCAcqTimeOutTxnPercent() {
		return totalDCAcqTimeOutTxnPercent;
	}
	public void setTotalDCAcqTimeOutTxnPercent(String totalDCAcqTimeOutTxnPercent) {
		this.totalDCAcqTimeOutTxnPercent = totalDCAcqTimeOutTxnPercent;
	}
	public String getTotalUPDeclinedTxnPercent() {
		return totalUPDeclinedTxnPercent;
	}
	public void setTotalUPDeclinedTxnPercent(String totalUPDeclinedTxnPercent) {
		this.totalUPDeclinedTxnPercent = totalUPDeclinedTxnPercent;
	}
	public String getTotalUPPendingTxnPercent() {
		return totalUPPendingTxnPercent;
	}
	public void setTotalUPPendingTxnPercent(String totalUPPendingTxnPercent) {
		this.totalUPPendingTxnPercent = totalUPPendingTxnPercent;
	}
	public String getTotalUPAcqDownTxnPercent() {
		return totalUPAcqDownTxnPercent;
	}
	public void setTotalUPAcqDownTxnPercent(String totalUPAcqDownTxnPercent) {
		this.totalUPAcqDownTxnPercent = totalUPAcqDownTxnPercent;
	}
	public String getTotalUPFailedAtAcqTxnPercent() {
		return totalUPFailedAtAcqTxnPercent;
	}
	public void setTotalUPFailedAtAcqTxnPercent(String totalUPFailedAtAcqTxnPercent) {
		this.totalUPFailedAtAcqTxnPercent = totalUPFailedAtAcqTxnPercent;
	}
	public String getTotalUPAcqTimeOutTxnPercent() {
		return totalUPAcqTimeOutTxnPercent;
	}
	public void setTotalUPAcqTimeOutTxnPercent(String totalUPAcqTimeOutTxnPercent) {
		this.totalUPAcqTimeOutTxnPercent = totalUPAcqTimeOutTxnPercent;
	}
	public String getTotalNBDeclinedTxnPercent() {
		return totalNBDeclinedTxnPercent;
	}
	public void setTotalNBDeclinedTxnPercent(String totalNBDeclinedTxnPercent) {
		this.totalNBDeclinedTxnPercent = totalNBDeclinedTxnPercent;
	}
	public String getTotalNBPendingTxnPercent() {
		return totalNBPendingTxnPercent;
	}
	public void setTotalNBPendingTxnPercent(String totalNBPendingTxnPercent) {
		this.totalNBPendingTxnPercent = totalNBPendingTxnPercent;
	}
	public String getTotalNBAcqDownTxnPercent() {
		return totalNBAcqDownTxnPercent;
	}
	public void setTotalNBAcqDownTxnPercent(String totalNBAcqDownTxnPercent) {
		this.totalNBAcqDownTxnPercent = totalNBAcqDownTxnPercent;
	}
	public String getTotalNBFailedAtAcqTxnPercent() {
		return totalNBFailedAtAcqTxnPercent;
	}
	public void setTotalNBFailedAtAcqTxnPercent(String totalNBFailedAtAcqTxnPercent) {
		this.totalNBFailedAtAcqTxnPercent = totalNBFailedAtAcqTxnPercent;
	}
	public String getTotalNBAcqTimeOutTxnPercent() {
		return totalNBAcqTimeOutTxnPercent;
	}
	public void setTotalNBAcqTimeOutTxnPercent(String totalNBAcqTimeOutTxnPercent) {
		this.totalNBAcqTimeOutTxnPercent = totalNBAcqTimeOutTxnPercent;
	}
	public String getTotalWLDeclinedTxnPercent() {
		return totalWLDeclinedTxnPercent;
	}
	public void setTotalWLDeclinedTxnPercent(String totalWLDeclinedTxnPercent) {
		this.totalWLDeclinedTxnPercent = totalWLDeclinedTxnPercent;
	}
	public String getTotalWLPendingTxnPercent() {
		return totalWLPendingTxnPercent;
	}
	public void setTotalWLPendingTxnPercent(String totalWLPendingTxnPercent) {
		this.totalWLPendingTxnPercent = totalWLPendingTxnPercent;
	}
	public String getTotalWLAcqDownTxnPercent() {
		return totalWLAcqDownTxnPercent;
	}
	public void setTotalWLAcqDownTxnPercent(String totalWLAcqDownTxnPercent) {
		this.totalWLAcqDownTxnPercent = totalWLAcqDownTxnPercent;
	}
	public String getTotalWLFailedAtAcqTxnPercent() {
		return totalWLFailedAtAcqTxnPercent;
	}
	public void setTotalWLFailedAtAcqTxnPercent(String totalWLFailedAtAcqTxnPercent) {
		this.totalWLFailedAtAcqTxnPercent = totalWLFailedAtAcqTxnPercent;
	}
	public String getTotalWLAcqTimeOutTxnPercent() {
		return totalWLAcqTimeOutTxnPercent;
	}
	public void setTotalWLAcqTimeOutTxnPercent(String totalWLAcqTimeOutTxnPercent) {
		this.totalWLAcqTimeOutTxnPercent = totalWLAcqTimeOutTxnPercent;
	}
	public String getTotalEMDeclinedTxnPercent() {
		return totalEMDeclinedTxnPercent;
	}
	public void setTotalEMDeclinedTxnPercent(String totalEMDeclinedTxnPercent) {
		this.totalEMDeclinedTxnPercent = totalEMDeclinedTxnPercent;
	}
	public String getTotalEMPendingTxnPercent() {
		return totalEMPendingTxnPercent;
	}
	public void setTotalEMPendingTxnPercent(String totalEMPendingTxnPercent) {
		this.totalEMPendingTxnPercent = totalEMPendingTxnPercent;
	}
	public String getTotalEMAcqDownTxnPercent() {
		return totalEMAcqDownTxnPercent;
	}
	public void setTotalEMAcqDownTxnPercent(String totalEMAcqDownTxnPercent) {
		this.totalEMAcqDownTxnPercent = totalEMAcqDownTxnPercent;
	}
	public String getTotalEMFailedAtAcqTxnPercent() {
		return totalEMFailedAtAcqTxnPercent;
	}
	public void setTotalEMFailedAtAcqTxnPercent(String totalEMFailedAtAcqTxnPercent) {
		this.totalEMFailedAtAcqTxnPercent = totalEMFailedAtAcqTxnPercent;
	}
	public String getTotalEMAcqTimeOutTxnPercent() {
		return totalEMAcqTimeOutTxnPercent;
	}
	public void setTotalEMAcqTimeOutTxnPercent(String totalEMAcqTimeOutTxnPercent) {
		this.totalEMAcqTimeOutTxnPercent = totalEMAcqTimeOutTxnPercent;
	}
	public String getTotalCDDeclinedTxnPercent() {
		return totalCDDeclinedTxnPercent;
	}
	public void setTotalCDDeclinedTxnPercent(String totalCDDeclinedTxnPercent) {
		this.totalCDDeclinedTxnPercent = totalCDDeclinedTxnPercent;
	}
	public String getTotalCDPendingTxnPercent() {
		return totalCDPendingTxnPercent;
	}
	public void setTotalCDPendingTxnPercent(String totalCDPendingTxnPercent) {
		this.totalCDPendingTxnPercent = totalCDPendingTxnPercent;
	}
	public String getTotalCDAcqDownTxnPercent() {
		return totalCDAcqDownTxnPercent;
	}
	public void setTotalCDAcqDownTxnPercent(String totalCDAcqDownTxnPercent) {
		this.totalCDAcqDownTxnPercent = totalCDAcqDownTxnPercent;
	}
	public String getTotalCDFailedAtAcqTxnPercent() {
		return totalCDFailedAtAcqTxnPercent;
	}
	public void setTotalCDFailedAtAcqTxnPercent(String totalCDFailedAtAcqTxnPercent) {
		this.totalCDFailedAtAcqTxnPercent = totalCDFailedAtAcqTxnPercent;
	}
	public String getTotalCDAcqTimeOutTxnPercent() {
		return totalCDAcqTimeOutTxnPercent;
	}
	public void setTotalCDAcqTimeOutTxnPercent(String totalCDAcqTimeOutTxnPercent) {
		this.totalCDAcqTimeOutTxnPercent = totalCDAcqTimeOutTxnPercent;
	}
	public String getDeclinedPercent() {
		return declinedPercent;
	}
	public void setDeclinedPercent(String declinedPercent) {
		this.declinedPercent = declinedPercent;
	}
	public String getPendingPercent() {
		return pendingPercent;
	}
	public void setPendingPercent(String pendingPercent) {
		this.pendingPercent = pendingPercent;
	}
	public String getAcquirerDownPercent() {
		return acquirerDownPercent;
	}
	public void setAcquirerDownPercent(String acquirerDownPercent) {
		this.acquirerDownPercent = acquirerDownPercent;
	}
	public String getFailedAtAcquirerPercent() {
		return failedAtAcquirerPercent;
	}
	public void setFailedAtAcquirerPercent(String failedAtAcquirerPercent) {
		this.failedAtAcquirerPercent = failedAtAcquirerPercent;
	}
	public String getAcquirerTimeOutPercent() {
		return acquirerTimeOutPercent;
	}
	public void setAcquirerTimeOutPercent(String acquirerTimeOutPercent) {
		this.acquirerTimeOutPercent = acquirerTimeOutPercent;
	}
}
