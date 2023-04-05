package com.paymentgateway.commons.util;

import java.io.Serializable;

public class LTCollectoinMSCDCLObject implements Serializable {

	/**
	 * Sandeep Sharma
	 */

	private static final long serialVersionUID = 6754722148625338401L;
	private String productCode;
	private String paymentMode;
	private String subPaymentMode;
	private String bill_type;
	private String pgRefNo;
	private String bankRefNo;
	private String consumerNo;
	private String bu;
	private String processingCycle;
	private String recieptType;
	private String billMonth;
	private String dueDate;
	private String reciept_no;
	private String txnDate;
	private String grossAmount;
	private String charges;
	private String gst;
	private String tds;
	private String misDate;
	private String zoneName;
	private String circleName;
	private String divisionName;
	private String ccCode;
	private String billerName;

	public Object[] myCsvMethodForLTColletionFile() {
		Object[] objectArray = new Object[24];

		objectArray[0] = productCode;
		objectArray[1] = paymentMode;
		objectArray[2] = subPaymentMode;
		objectArray[3] = bill_type;
		objectArray[4] = pgRefNo;
		objectArray[5] = bankRefNo;
		objectArray[6] = consumerNo;
		objectArray[7] = bu;
		objectArray[8] = processingCycle;
		objectArray[9] = recieptType;
		objectArray[10] = billMonth;
		objectArray[11] = dueDate;
		objectArray[12] = reciept_no;
		objectArray[13] = txnDate;
		objectArray[14] = grossAmount;
		objectArray[15] = charges;
		objectArray[16] = gst;
		objectArray[17] = tds;
		objectArray[18] = misDate;
		objectArray[19] = zoneName;
		objectArray[20] = circleName;
		objectArray[21] = divisionName;
		objectArray[22] = ccCode;
		objectArray[23] = billerName;

		return objectArray;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public String getSubPaymentMode() {
		return subPaymentMode;
	}

	public void setSubPaymentMode(String subPaymentMode) {
		this.subPaymentMode = subPaymentMode;
	}

	public String getPgRefNo() {
		return pgRefNo;
	}

	public void setPgRefNo(String pgRefNo) {
		this.pgRefNo = pgRefNo;
	}

	public String getBankRefNo() {
		return bankRefNo;
	}

	public void setBankRefNo(String bankRefNo) {
		this.bankRefNo = bankRefNo;
	}

	public String getConsumerNo() {
		return consumerNo;
	}

	public void setConsumerNo(String consumerNo) {
		this.consumerNo = consumerNo;
	}

	public String getBu() {
		return bu;
	}

	public void setBu(String bu) {
		this.bu = bu;
	}

	public String getProcessingCycle() {
		return processingCycle;
	}

	public void setProcessingCycle(String processingCycle) {
		this.processingCycle = processingCycle;
	}

	public String getRecieptType() {
		return recieptType;
	}

	public void setRecieptType(String recieptType) {
		this.recieptType = recieptType;
	}

	public String getBillMonth() {
		return billMonth;
	}

	public void setBillMonth(String billMonth) {
		this.billMonth = billMonth;
	}

	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	public String getTxnDate() {
		return txnDate;
	}

	public void setTxnDate(String txnDate) {
		this.txnDate = txnDate;
	}

	public String getGrossAmount() {
		return grossAmount;
	}

	public void setGrossAmount(String grossAmount) {
		this.grossAmount = grossAmount;
	}

	public String getCharges() {
		return charges;
	}

	public void setCharges(String charges) {
		this.charges = charges;
	}

	public String getGst() {
		return gst;
	}

	public void setGst(String gst) {
		this.gst = gst;
	}

	public String getTds() {
		return tds;
	}

	public void setTds(String tds) {
		this.tds = tds;
	}

	public String getMisDate() {
		return misDate;
	}

	public void setMisDate(String misDate) {
		this.misDate = misDate;
	}

	public String getZoneName() {
		return zoneName;
	}

	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}

	public String getCircleName() {
		return circleName;
	}

	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}

	public String getDivisionName() {
		return divisionName;
	}

	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}

	public String getCcCode() {
		return ccCode;
	}

	public void setCcCode(String ccCode) {
		this.ccCode = ccCode;
	}

	public String getBillerName() {
		return billerName;
	}

	public void setBillerName(String billerName) {
		this.billerName = billerName;
	}

	public String getBill_type() {
		return bill_type;
	}

	public void setBill_type(String bill_type) {
		this.bill_type = bill_type;
	}

	public String getReciept_no() {
		return reciept_no;
	}

	public void setReciept_no(String reciept_no) {
		this.reciept_no = reciept_no;
	}

}
