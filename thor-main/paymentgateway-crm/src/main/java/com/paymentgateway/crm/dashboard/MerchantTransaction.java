package com.paymentgateway.crm.dashboard;

import java.io.Serializable;

public class MerchantTransaction  implements Serializable {


	private static final long serialVersionUID = 4996233635896958522L;
	
	private String merchantPayId;
	private String subMerchantPayid;
	private String businessName;
	private String subMerchantBusiness;
	private String totalTransactionAmount;
	private String totalTransactionVolume;
	private String txnVolumeMerchantBusinessName;
	private String txnVolumeSuperMerchantBusinessName;
	private String txnAmountMerchantBusinessName;
	private String txnAmountSuperMerchantBusinessName;

	private String totalFirstTransactionAmount;
	private String totalFirstTransactionVolume;
	private String txnFirstVolumeMerchantBusinessName;
	private String txnFirstVolumeSuperMerchantBusinessName;
	private String txnFirstAmountMerchantBusinessName;
	private String txnFirstAmountSuperMerchantBusinessName;
	
	private String totalSecondTransactionAmount;
	private String totalSecondTransactionVolume;
	private String txnSecondVolumeMerchantBusinessName;
	private String txnSecondVolumeSuperMerchantBusinessName;
	private String txnSecondAmountMerchantBusinessName;
	private String txnSecondAmountSuperMerchantBusinessName;

	private String totalThirdTransactionAmount;
	private String totalThirdTransactionVolume;
	private String txnThirdVolumeMerchantBusinessName;
	private String txnThirdVolumeSuperMerchantBusinessName;
	private String txnThirdAmountMerchantBusinessName;
	private String txnThirdAmountSuperMerchantBusinessName;

	private String totalFourthTransactionAmount;
	private String totalFourthTransactionVolume;
	private String txnFourthVolumeMerchantBusinessName;
	private String txnFourthVolumeSuperMerchantBusinessName;
	private String txnFourthAmountMerchantBusinessName;
	private String txnFourthAmountSuperMerchantBusinessName;

	private String totalFifthTransactionAmount;
	private String totalFifthTransactionVolume;
	private String txnFifthVolumeMerchantBusinessName;
	private String txnFifthVolumeSuperMerchantBusinessName;
	private String txnFifthAmountMerchantBusinessName;
	private String txnFifthAmountSuperMerchantBusinessName;
	
	private boolean subMerchantFlag;
	public String getMerchantPayId() {
		return merchantPayId;
	}
	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}
	public String getSubMerchantPayid() {
		return subMerchantPayid;
	}
	public void setSubMerchantPayid(String subMerchantPayid) {
		this.subMerchantPayid = subMerchantPayid;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public String getSubMerchantBusiness() {
		return subMerchantBusiness;
	}
	public void setSubMerchantBusiness(String subMerchantBusiness) {
		this.subMerchantBusiness = subMerchantBusiness;
	}
	public String getTotalTransactionAmount() {
		return totalTransactionAmount;
	}
	public void setTotalTransactionAmount(String totalTransactionAmount) {
		this.totalTransactionAmount = totalTransactionAmount;
	}
	public String getTotalTransactionVolume() {
		return totalTransactionVolume;
	}
	public void setTotalTransactionVolume(String totalTransactionVolume) {
		this.totalTransactionVolume = totalTransactionVolume;
	}
	public boolean isSubMerchantFlag() {
		return subMerchantFlag;
	}
	public void setSubMerchantFlag(boolean subMerchantFlag) {
		this.subMerchantFlag = subMerchantFlag;
	}
	
	public String getTxnAmountMerchantBusinessName() {
		return txnAmountMerchantBusinessName;
	}
	public void setTxnAmountMerchantBusinessName(String txnAmountMerchantBusinessName) {
		this.txnAmountMerchantBusinessName = txnAmountMerchantBusinessName;
	}
	public String getTxnAmountSuperMerchantBusinessName() {
		return txnAmountSuperMerchantBusinessName;
	}
	public void setTxnAmountSuperMerchantBusinessName(String txnAmountSuperMerchantBusinessName) {
		this.txnAmountSuperMerchantBusinessName = txnAmountSuperMerchantBusinessName;
	}
	public String getTotalSecondTransactionAmount() {
		return totalSecondTransactionAmount;
	}
	public void setTotalSecondTransactionAmount(String totalSecondTransactionAmount) {
		this.totalSecondTransactionAmount = totalSecondTransactionAmount;
	}
	public String getTotalSecondTransactionVolume() {
		return totalSecondTransactionVolume;
	}
	public void setTotalSecondTransactionVolume(String totalSecondTransactionVolume) {
		this.totalSecondTransactionVolume = totalSecondTransactionVolume;
	}
	
	public String getTxnSecondAmountMerchantBusinessName() {
		return txnSecondAmountMerchantBusinessName;
	}
	public void setTxnSecondAmountMerchantBusinessName(String txnSecondAmountMerchantBusinessName) {
		this.txnSecondAmountMerchantBusinessName = txnSecondAmountMerchantBusinessName;
	}
	public String getTxnSecondAmountSuperMerchantBusinessName() {
		return txnSecondAmountSuperMerchantBusinessName;
	}
	public void setTxnSecondAmountSuperMerchantBusinessName(String txnSecondAmountSuperMerchantBusinessName) {
		this.txnSecondAmountSuperMerchantBusinessName = txnSecondAmountSuperMerchantBusinessName;
	}
	public String getTotalThirdTransactionAmount() {
		return totalThirdTransactionAmount;
	}
	public void setTotalThirdTransactionAmount(String totalThirdTransactionAmount) {
		this.totalThirdTransactionAmount = totalThirdTransactionAmount;
	}
	public String getTotalThirdTransactionVolume() {
		return totalThirdTransactionVolume;
	}
	public void setTotalThirdTransactionVolume(String totalThirdTransactionVolume) {
		this.totalThirdTransactionVolume = totalThirdTransactionVolume;
	}
	
	public String getTxnThirdAmountMerchantBusinessName() {
		return txnThirdAmountMerchantBusinessName;
	}
	public void setTxnThirdAmountMerchantBusinessName(String txnThirdAmountMerchantBusinessName) {
		this.txnThirdAmountMerchantBusinessName = txnThirdAmountMerchantBusinessName;
	}
	public String getTxnThirdAmountSuperMerchantBusinessName() {
		return txnThirdAmountSuperMerchantBusinessName;
	}
	public void setTxnThirdAmountSuperMerchantBusinessName(String txnThirdAmountSuperMerchantBusinessName) {
		this.txnThirdAmountSuperMerchantBusinessName = txnThirdAmountSuperMerchantBusinessName;
	}
	public String getTotalFourthTransactionAmount() {
		return totalFourthTransactionAmount;
	}
	public void setTotalFourthTransactionAmount(String totalFourthTransactionAmount) {
		this.totalFourthTransactionAmount = totalFourthTransactionAmount;
	}
	public String getTotalFourthTransactionVolume() {
		return totalFourthTransactionVolume;
	}
	public void setTotalFourthTransactionVolume(String totalFourthTransactionVolume) {
		this.totalFourthTransactionVolume = totalFourthTransactionVolume;
	}
	
	public String getTxnFourthAmountMerchantBusinessName() {
		return txnFourthAmountMerchantBusinessName;
	}
	public void setTxnFourthAmountMerchantBusinessName(String txnFourthAmountMerchantBusinessName) {
		this.txnFourthAmountMerchantBusinessName = txnFourthAmountMerchantBusinessName;
	}
	public String getTxnFourthAmountSuperMerchantBusinessName() {
		return txnFourthAmountSuperMerchantBusinessName;
	}
	public void setTxnFourthAmountSuperMerchantBusinessName(String txnFourthAmountSuperMerchantBusinessName) {
		this.txnFourthAmountSuperMerchantBusinessName = txnFourthAmountSuperMerchantBusinessName;
	}
	public String getTotalFifthTransactionAmount() {
		return totalFifthTransactionAmount;
	}
	public void setTotalFifthTransactionAmount(String totalFifthTransactionAmount) {
		this.totalFifthTransactionAmount = totalFifthTransactionAmount;
	}
	public String getTotalFifthTransactionVolume() {
		return totalFifthTransactionVolume;
	}
	public void setTotalFifthTransactionVolume(String totalFifthTransactionVolume) {
		this.totalFifthTransactionVolume = totalFifthTransactionVolume;
	}
	
	public String getTxnFifthAmountMerchantBusinessName() {
		return txnFifthAmountMerchantBusinessName;
	}
	public void setTxnFifthAmountMerchantBusinessName(String txnFifthAmountMerchantBusinessName) {
		this.txnFifthAmountMerchantBusinessName = txnFifthAmountMerchantBusinessName;
	}
	public String getTxnFifthAmountSuperMerchantBusinessName() {
		return txnFifthAmountSuperMerchantBusinessName;
	}
	public void setTxnFifthAmountSuperMerchantBusinessName(String txnFifthAmountSuperMerchantBusinessName) {
		this.txnFifthAmountSuperMerchantBusinessName = txnFifthAmountSuperMerchantBusinessName;
	}
	public String getTotalFirstTransactionAmount() {
		return totalFirstTransactionAmount;
	}
	public void setTotalFirstTransactionAmount(String totalFirstTransactionAmount) {
		this.totalFirstTransactionAmount = totalFirstTransactionAmount;
	}
	public String getTotalFirstTransactionVolume() {
		return totalFirstTransactionVolume;
	}
	public void setTotalFirstTransactionVolume(String totalFirstTransactionVolume) {
		this.totalFirstTransactionVolume = totalFirstTransactionVolume;
	}
	public String getTxnFirstAmountMerchantBusinessName() {
		return txnFirstAmountMerchantBusinessName;
	}
	public void setTxnFirstAmountMerchantBusinessName(String txnFirstAmountMerchantBusinessName) {
		this.txnFirstAmountMerchantBusinessName = txnFirstAmountMerchantBusinessName;
	}
	public String getTxnFirstAmountSuperMerchantBusinessName() {
		return txnFirstAmountSuperMerchantBusinessName;
	}
	public void setTxnFirstAmountSuperMerchantBusinessName(String txnFirstAmountSuperMerchantBusinessName) {
		this.txnFirstAmountSuperMerchantBusinessName = txnFirstAmountSuperMerchantBusinessName;
	}
	public String getTxnVolumeMerchantBusinessName() {
		return txnVolumeMerchantBusinessName;
	}
	public void setTxnVolumeMerchantBusinessName(String txnVolumeMerchantBusinessName) {
		this.txnVolumeMerchantBusinessName = txnVolumeMerchantBusinessName;
	}
	public String getTxnVolumeSuperMerchantBusinessName() {
		return txnVolumeSuperMerchantBusinessName;
	}
	public void setTxnVolumeSuperMerchantBusinessName(String txnVolumeSuperMerchantBusinessName) {
		this.txnVolumeSuperMerchantBusinessName = txnVolumeSuperMerchantBusinessName;
	}
	public String getTxnFirstVolumeMerchantBusinessName() {
		return txnFirstVolumeMerchantBusinessName;
	}
	public void setTxnFirstVolumeMerchantBusinessName(String txnFirstVolumeMerchantBusinessName) {
		this.txnFirstVolumeMerchantBusinessName = txnFirstVolumeMerchantBusinessName;
	}
	public String getTxnFirstVolumeSuperMerchantBusinessName() {
		return txnFirstVolumeSuperMerchantBusinessName;
	}
	public void setTxnFirstVolumeSuperMerchantBusinessName(String txnFirstVolumeSuperMerchantBusinessName) {
		this.txnFirstVolumeSuperMerchantBusinessName = txnFirstVolumeSuperMerchantBusinessName;
	}
	public String getTxnSecondVolumeMerchantBusinessName() {
		return txnSecondVolumeMerchantBusinessName;
	}
	public void setTxnSecondVolumeMerchantBusinessName(String txnSecondVolumeMerchantBusinessName) {
		this.txnSecondVolumeMerchantBusinessName = txnSecondVolumeMerchantBusinessName;
	}
	public String getTxnSecondVolumeSuperMerchantBusinessName() {
		return txnSecondVolumeSuperMerchantBusinessName;
	}
	public void setTxnSecondVolumeSuperMerchantBusinessName(String txnSecondVolumeSuperMerchantBusinessName) {
		this.txnSecondVolumeSuperMerchantBusinessName = txnSecondVolumeSuperMerchantBusinessName;
	}
	public String getTxnThirdVolumeMerchantBusinessName() {
		return txnThirdVolumeMerchantBusinessName;
	}
	public void setTxnThirdVolumeMerchantBusinessName(String txnThirdVolumeMerchantBusinessName) {
		this.txnThirdVolumeMerchantBusinessName = txnThirdVolumeMerchantBusinessName;
	}
	public String getTxnThirdVolumeSuperMerchantBusinessName() {
		return txnThirdVolumeSuperMerchantBusinessName;
	}
	public void setTxnThirdVolumeSuperMerchantBusinessName(String txnThirdVolumeSuperMerchantBusinessName) {
		this.txnThirdVolumeSuperMerchantBusinessName = txnThirdVolumeSuperMerchantBusinessName;
	}
	public String getTxnFourthVolumeMerchantBusinessName() {
		return txnFourthVolumeMerchantBusinessName;
	}
	public void setTxnFourthVolumeMerchantBusinessName(String txnFourthVolumeMerchantBusinessName) {
		this.txnFourthVolumeMerchantBusinessName = txnFourthVolumeMerchantBusinessName;
	}
	public String getTxnFourthVolumeSuperMerchantBusinessName() {
		return txnFourthVolumeSuperMerchantBusinessName;
	}
	public void setTxnFourthVolumeSuperMerchantBusinessName(String txnFourthVolumeSuperMerchantBusinessName) {
		this.txnFourthVolumeSuperMerchantBusinessName = txnFourthVolumeSuperMerchantBusinessName;
	}
	public String getTxnFifthVolumeMerchantBusinessName() {
		return txnFifthVolumeMerchantBusinessName;
	}
	public void setTxnFifthVolumeMerchantBusinessName(String txnFifthVolumeMerchantBusinessName) {
		this.txnFifthVolumeMerchantBusinessName = txnFifthVolumeMerchantBusinessName;
	}
	public String getTxnFifthVolumeSuperMerchantBusinessName() {
		return txnFifthVolumeSuperMerchantBusinessName;
	}
	public void setTxnFifthVolumeSuperMerchantBusinessName(String txnFifthVolumeSuperMerchantBusinessName) {
		this.txnFifthVolumeSuperMerchantBusinessName = txnFifthVolumeSuperMerchantBusinessName;
	}	
	
}
