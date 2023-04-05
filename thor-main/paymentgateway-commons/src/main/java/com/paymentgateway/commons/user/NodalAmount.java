package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.math.BigDecimal;
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
public class NodalAmount implements Serializable {

	private static final long serialVersionUID = 4333700276430269770L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String acquirer;
	private String paymentType;
	private BigDecimal nodalCreditAmount;
	private String reconDate;
	private Date createDate;
	

	public BigDecimal getNodalCreditAmount() {
		return nodalCreditAmount;
	}


	public void setNodalCreditAmount(BigDecimal nodalCreditAmount) {
		this.nodalCreditAmount = nodalCreditAmount;
	}


	public String getReconDate() {
		return reconDate;
	}


	public void setReconDate(String reconDate) {
		this.reconDate = reconDate;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getAcquirer() {
		return acquirer;
	}


	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}


	public String getPaymentType() {
		return paymentType;
	}


	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}




	public Date getCreateDate() {
		return createDate;
	}


	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}


		
	

	
	
}
