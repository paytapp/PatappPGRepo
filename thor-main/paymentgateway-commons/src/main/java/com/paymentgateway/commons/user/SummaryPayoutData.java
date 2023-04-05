package com.paymentgateway.commons.user;

import java.util.List;

public class SummaryPayoutData {
	
	private String acquirerName;
	private String paymentype;
	private String noOfTxn;
	private List<String> merchantList;
	private String pgBaseAmt;
	private String gstPg;
	private String totalPgFee;
	private String acquirerBaseAmt;
	private String gstAcquirer;
	private String totalAcquirerFee;
	private String netPayoutFromNodal;
	private String merchantDiffAmt;
	private String businessName;
	
	public String getAcquirerName() {
		return acquirerName;
	}
	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}
	public String getPaymentype() {
		return paymentype;
	}
	public void setPaymentype(String paymentype) {
		this.paymentype = paymentype;
	}
	public String getNoOfTxn() {
		return noOfTxn;
	}
	public void setNoOfTxn(String noOfTxn) {
		this.noOfTxn = noOfTxn;
	}
	public List<String> getMerchantList() {
		return merchantList;
	}
	public void setMerchantList(List<String> merchantList) {
		this.merchantList = merchantList;
	}
	public String getAcquirerBaseAmt() {
		return acquirerBaseAmt;
	}
	public void setAcquirerBaseAmt(String acquirerBaseAmt) {
		this.acquirerBaseAmt = acquirerBaseAmt;
	}
	public String getGstAcquirer() {
		return gstAcquirer;
	}
	public void setGstAcquirer(String gstAcquirer) {
		this.gstAcquirer = gstAcquirer;
	}
	public String getTotalAcquirerFee() {
		return totalAcquirerFee;
	}
	public void setTotalAcquirerFee(String totalAcquirerFee) {
		this.totalAcquirerFee = totalAcquirerFee;
	}
	public String getNetPayoutFromNodal() {
		return netPayoutFromNodal;
	}
	public void setNetPayoutFromNodal(String netPayoutFromNodal) {
		this.netPayoutFromNodal = netPayoutFromNodal;
	}
	public String getMerchantDiffAmt() {
		return merchantDiffAmt;
	}
	public void setMerchantDiffAmt(String merchantDiffAmt) {
		this.merchantDiffAmt = merchantDiffAmt;
	}
	public String getPgBaseAmt() {
		return pgBaseAmt;
	}
	public void setPgBaseAmt(String pgBaseAmt) {
		this.pgBaseAmt = pgBaseAmt;
	}
	public String getGstPg() {
		return gstPg;
	}
	public void setGstPg(String gstPg) {
		this.gstPg = gstPg;
	}
	public String getTotalPgFee() {
		return totalPgFee;
	}
	public void setTotalPgFee(String totalPgFee) {
		this.totalPgFee = totalPgFee;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

}
