package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;

@Entity
@Proxy(lazy= false)
@Table
public class GstRSaleReport implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7146560766443118537L;
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private String payId;
	private String gstNo;
	private String businessName;
	private String operationState;
	private String goodOrService;
	private String servicesDescription;
	private String hsn_code;
	private Double txn_value;
	private Double cGst;
	private Double SGst;
	private Double iGst;
	private Double cEss;
	private String tds;
	private String pgGstNo;
	private String address;
	private String city;
	private String state;
	private Double netAmt;
	private String invoiceNo;
	private Date createdDate;
	private String totalnetAmt;
	private String month;
	private String year;
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getGstNo() {
		return gstNo;
	}
	public void setGstNo(String gstNo) {
		this.gstNo = gstNo;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public String getOperationState() {
		return operationState;
	}
	public void setOperationState(String operationState) {
		this.operationState = operationState;
	}
	public String getGoodOrService() {
		return goodOrService;
	}
	public void setGoodOrService(String goodOrService) {
		this.goodOrService = goodOrService;
	}
	public String getServicesDescription() {
		return servicesDescription;
	}
	public void setServicesDescription(String servicesDescription) {
		this.servicesDescription = servicesDescription;
	}
	public String getHsn_code() {
		return hsn_code;
	}
	public void setHsn_code(String hsn_code) {
		this.hsn_code = hsn_code;
	}
	
	
	public Double getTxn_value() {
		return txn_value;
	}
	public void setTxn_value(Double txn_value) {
		this.txn_value = txn_value;
	}
	public String getTds() {
		return tds;
	}
	public void setTds(String tds) {
		this.tds = tds;
	}
	public String getPgGstNo() {
		return pgGstNo;
	}
	public void setPgGstNo(String pgGstNo) {
		this.pgGstNo = pgGstNo;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	
	public String getInvoiceNo() {
		return invoiceNo;
	}
	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getTotalnetAmt() {
		return totalnetAmt;
	}
	public void setTotalnetAmt(String totalnetAmt) {
		this.totalnetAmt = totalnetAmt;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Double getcGst() {
		return cGst;
	}
	public void setcGst(Double cGst) {
		this.cGst = cGst;
	}
	public Double getSGst() {
		return SGst;
	}
	public void setSGst(Double sGst) {
		SGst = sGst;
	}
	public Double getiGst() {
		return iGst;
	}
	public void setiGst(Double iGst) {
		this.iGst = iGst;
	}
	public Double getcEss() {
		return cEss;
	}
	public void setcEss(Double cEss) {
		this.cEss = cEss;
	}
	public Double getNetAmt() {
		return netAmt;
	}
	public void setNetAmt(Double netAmt) {
		this.netAmt = netAmt;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	
	
	
}
