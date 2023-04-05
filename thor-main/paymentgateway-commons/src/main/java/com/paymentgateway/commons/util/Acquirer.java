package com.paymentgateway.commons.util;

import java.io.Serializable;

import com.paymentgateway.commons.user.User;

public class Acquirer implements Serializable {

	private static final long serialVersionUID = -5371568875441496496L;

	public Acquirer() {

	}

	private String acquirerEmailId;
	private String acquirerFirstName;
	private String acquirerLastName;
	private String payId;
	private String acquirerBusinessName;
	private String acquirerAccountNo;
	private Boolean acquirerIsActive;

	public void setSubAdmin(User user) {
		setAcquirerEmailId(user.getEmailId());
		setAcquirerFirstName(user.getFirstName());
		setAcquirerLastName(user.getLastName());
		setPayId(user.getPayId());
		setAcquirerAccountNo(user.getAccountNo());
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getAcquirerEmailId() {
		return acquirerEmailId;
	}

	public void setAcquirerEmailId(String acquirerEmailId) {
		this.acquirerEmailId = acquirerEmailId;
	}

	public String getAcquirerFirstName() {
		return acquirerFirstName;
	}

	public void setAcquirerFirstName(String acquirerFirstName) {
		this.acquirerFirstName = acquirerFirstName;
	}

	public String getAcquirerLastName() {
		return acquirerLastName;
	}

	public void setAcquirerLastName(String acquirerLastName) {
		this.acquirerLastName = acquirerLastName;
	}

	public String getAcquirerBusinessName() {
		return acquirerBusinessName;
	}

	public void setAcquirerBusinessName(String acquirerBusinessName) {
		this.acquirerBusinessName = acquirerBusinessName;
	}

	public Boolean getAcquirerIsActive() {
		return acquirerIsActive;
	}

	public void setAcquirerIsActive(Boolean acquirerIsActive) {
		this.acquirerIsActive = acquirerIsActive;
	}

	public String getAcquirerAccountNo() {
		return acquirerAccountNo;
	}

	public void setAcquirerAccountNo(String acquirerAccountNo) {
		this.acquirerAccountNo = acquirerAccountNo;
	}


}
