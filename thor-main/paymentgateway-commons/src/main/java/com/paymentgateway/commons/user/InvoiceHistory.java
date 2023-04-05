package com.paymentgateway.commons.user;

import java.io.Serializable;

public class InvoiceHistory implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2094670992695676285L;
	
	private String fileName;
	private String date;
	private long totalRecords;
	private long success;
	private long totalUnsent;
	private long totalPending;
	private String businessName;
	private String subMerchantbusinessName;
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public long getTotalRecords() {
		return totalRecords;
	}
	public void setTotalRecords(long totalRecords) {
		this.totalRecords = totalRecords;
	}
	public long getSuccess() {
		return success;
	}
	public void setSuccess(long success) {
		this.success = success;
	}
	public long getTotalUnsent() {
		return totalUnsent;
	}
	public void setTotalUnsent(long totalUnsent) {
		this.totalUnsent = totalUnsent;
	}
	public long getTotalPending() {
		return totalPending;
	}
	public void setTotalPending(long totalPending) {
		this.totalPending = totalPending;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public String getSubMerchantbusinessName() {
		return subMerchantbusinessName;
	}
	public void setSubMerchantbusinessName(String subMerchantbusinessName) {
		this.subMerchantbusinessName = subMerchantbusinessName;
	}
	
	
}
