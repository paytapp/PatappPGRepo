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
public class RefundValidationDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7875817847212783430L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private Date createdDate;
	private Date updatedDate;
	private Date refundRequestDate;
	
	private String fileVersion;
	private String noOfTxns;
	private String versionType;
	private String fileName;
	private String payId;
	private String totalTxns;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public Date getRefundRequestDate() {
		return refundRequestDate;
	}
	public void setRefundRequestDate(Date refundRequestDate) {
		this.refundRequestDate = refundRequestDate;
	}
	public String getFileVersion() {
		return fileVersion;
	}
	public void setFileVersion(String fileVersion) {
		this.fileVersion = fileVersion;
	}
	public String getNoOfTxns() {
		return noOfTxns;
	}
	public void setNoOfTxns(String noOfTxns) {
		this.noOfTxns = noOfTxns;
	}
	public String getVersionType() {
		return versionType;
	}
	public void setVersionType(String versionType) {
		this.versionType = versionType;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getTotalTxns() {
		return totalTxns;
	}
	public void setTotalTxns(String totalTxns) {
		this.totalTxns = totalTxns;
	}
	
}
