package com.paymentgateway.commons.util;

import java.io.Serializable;

public class MonthlyInvoiceObject implements Serializable{

	/**
	 * @author Sandeep Sharma
	 */
	private static final long serialVersionUID = 1438439977263848821L;
	
	private String merchantName;
	private String subMerchantName;
	private String merchantPayId;
	private String subMerchantPayId;
	private String invoiceMonth;
	private String createDate;
	private String invoiceNo;
	private String hsnNo;
	private String fileName;
	
	
	public String getMerchantPayId() {
		return merchantPayId;
	}
	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}
	public String getInvoiceMonth() {
		return invoiceMonth;
	}
	public void setInvoiceMonth(String invoiceMonth) {
		this.invoiceMonth = invoiceMonth;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getInvoiceNo() {
		return invoiceNo;
	}
	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}
	public String getHsnNo() {
		return hsnNo;
	}
	public void setHsnNo(String hsnNo) {
		this.hsnNo = hsnNo;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getSubMerchantName() {
		return subMerchantName;
	}
	public void setSubMerchantName(String subMerchantName) {
		this.subMerchantName = subMerchantName;
	}
	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}
	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}
	

}
