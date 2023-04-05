package com.paymentgateway.commons.util;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;

@Entity
@Table
public class BinRangeEvent implements Serializable {

	
	private static final long serialVersionUID = 7434866432858668172L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String merchantCode;
	private String requestId;
	private String decision;
	private String responseCode;
	private String token;
		
	
	public BinRangeEvent() {
		
	}
	
	
	public BinRangeEvent(String merchantCode, String acqId, String decision, String responseCode,
			String token) {
		
	
		this.merchantCode = merchantCode;
		this.requestId = requestId;
		this.decision = decision;
		this.responseCode = responseCode;
		this.token = token;
	}




	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	
	
	
	public String getRequestId() {
		return requestId;
	}


	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}


	public String getMerchantCode() {
		return merchantCode;
	}
	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public String getDecision() {
		return decision;
	}
	public void setDecision(String decision) {
		this.decision = decision;
	}
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
	
	
	
	
}
