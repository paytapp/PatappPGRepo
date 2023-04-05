package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;

/**
 * @author Rahul
 *
 */

@Entity
@Proxy(lazy= false)
@Table
public class DispatchSlipDetails implements Serializable{
	
	private static final long serialVersionUID = -3417915047548722369L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private Date createdDate;
	private String processedBy;
	private String payId;
	private String orderId;
	private String invoiceId;
	private String courierServiceProvider;
	private String dispatchSlipNo;
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
	public String getProcessedBy() {
		return processedBy;
	}
	public void setProcessedBy(String processedBy) {
		this.processedBy = processedBy;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getInvoiceId() {
		return invoiceId;
	}
	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}
	public String getCourierServiceProvider() {
		return courierServiceProvider;
	}
	public void setCourierServiceProvider(String courierServiceProvider) {
		this.courierServiceProvider = courierServiceProvider;
	}
	public String getDispatchSlipNo() {
		return dispatchSlipNo;
	}
	public void setDispatchSlipNo(String dispatchSlipNo) {
		this.dispatchSlipNo = dispatchSlipNo;
	}
	
	
	

}
