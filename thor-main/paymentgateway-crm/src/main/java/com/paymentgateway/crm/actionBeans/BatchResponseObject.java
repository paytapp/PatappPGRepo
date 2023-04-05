package com.paymentgateway.crm.actionBeans;

import java.util.LinkedList;
import java.util.List;

import com.paymentgateway.commons.user.BatchTransactionObj;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.util.BinRange;
import com.paymentgateway.crm.action.StatusEnquiryParameters;

public class BatchResponseObject {
	private String responseMessage;
	private List<BatchTransactionObj> batchTransactionList = new LinkedList<BatchTransactionObj>();
	private List<StatusEnquiryParameters> statusEnquiryParamsList = new LinkedList<StatusEnquiryParameters>();
	private List<BinRange> binRangeResponseList = new LinkedList<BinRange>();
	private List<Invoice>  invoiceEmailList = new LinkedList<Invoice>();
	
	public List<BatchTransactionObj> getBatchTransactionList() {
		return batchTransactionList;
	}
	public void setBatchTransactionList(List<BatchTransactionObj> batchTransactionList) {
		this.batchTransactionList = batchTransactionList;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	public List<StatusEnquiryParameters> getStatusEnquiryParamsList() {
		return statusEnquiryParamsList;
	}
	public void setStatusEnquiryParamsList(List<StatusEnquiryParameters> statusEnquiryParamsList) {
		this.statusEnquiryParamsList = statusEnquiryParamsList;
	}
	public List<Invoice> getInvoiceEmailList() {
		return invoiceEmailList;
	}
	public void setInvoiceEmailList(List<Invoice> invoiceEmailList) {
		this.invoiceEmailList = invoiceEmailList;
	}
	public List<BinRange> getBinRangeResponseList() {
		return binRangeResponseList;
	}
	public void setBinRangeResponseList(List<BinRange> binRangeResponseList) {
		this.binRangeResponseList = binRangeResponseList;
	}

	
}
