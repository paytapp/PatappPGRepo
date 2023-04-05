package com.paymentgateway.commons.user;

import java.io.Serializable;



public class CustomerAddress implements Serializable {

	private static final long serialVersionUID = -213386584633098489L;

	private String custName;
	private String custPhone;
	private String custStreetAddress1;
	private String custStreetAddress2;
	private String custCity;
	private String custState;
	private String custCountry;
	private String custZip;
	private String custShipName;
	private String custShipStreetAddress1;
	private String custShipStreetAddress2;
	private String custShipCity;
	private String custShipState;
	private String custShipCountry;
	private String custShipZip;
	private String internalTxnAuthentication;
	private String durationTo;
	private String durationFrom;

	public CustomerAddress() {

	}

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public String getCustPhone() {
		return custPhone;
	}

	public void setCustPhone(String custPhone) {
		this.custPhone = custPhone;
	}

	public String getCustStreetAddress1() {
		return custStreetAddress1;
	}

	public void setCustStreetAddress1(String custStreetAddress1) {
		this.custStreetAddress1 = custStreetAddress1;
	}

	public String getCustStreetAddress2() {
		return custStreetAddress2;
	}

	public void setCustStreetAddress2(String custStreetAddress2) {
		this.custStreetAddress2 = custStreetAddress2;
	}

	public String getCustCity() {
		return custCity;
	}

	public void setCustCity(String custCity) {
		this.custCity = custCity;
	}

	public String getCustState() {
		return custState;
	}

	public void setCustState(String custState) {
		this.custState = custState;
	}

	public String getCustCountry() {
		return custCountry;
	}

	public void setCustCountry(String custCountry) {
		this.custCountry = custCountry;
	}

	public String getCustZip() {
		return custZip;
	}

	public void setCustZip(String custZip) {
		this.custZip = custZip;
	}

	public String getCustShipStreetAddress1() {
		return custShipStreetAddress1;
	}

	public void setCustShipStreetAddress1(String custShipStreetAddress1) {
		this.custShipStreetAddress1 = custShipStreetAddress1;
	}

	public String getCustShipStreetAddress2() {
		return custShipStreetAddress2;
	}

	public void setCustShipStreetAddress2(String custShipStreetAddress2) {
		this.custShipStreetAddress2 = custShipStreetAddress2;
	}

	public String getCustShipCity() {
		return custShipCity;
	}

	public void setCustShipCity(String custShipCity) {
		this.custShipCity = custShipCity;
	}

	public String getCustShipState() {
		return custShipState;
	}

	public void setCustShipState(String custShipState) {
		this.custShipState = custShipState;
	}

	public String getCustShipCountry() {
		return custShipCountry;
	}

	public void setCustShipCountry(String custShipCountry) {
		this.custShipCountry = custShipCountry;
	}

	public String getCustShipZip() {
		return custShipZip;
	}

	public void setCustShipZip(String custShipZip) {
		this.custShipZip = custShipZip;
	}

	public String getInternalTxnAuthentication() {
		return internalTxnAuthentication;
	}

	public void setInternalTxnAuthentication(String internalTxnAuthentication) {
		this.internalTxnAuthentication = internalTxnAuthentication;
	}

	public String getCustShipName() {
		return custShipName;
	}

	public void setCustShipName(String custShipName) {
		this.custShipName = custShipName;
	}

	public String getDurationTo() {
		return durationTo;
	}

	public void setDurationTo(String durationTo) {
		this.durationTo = durationTo;
	}

	public String getDurationFrom() {
		return durationFrom;
	}

	public void setDurationFrom(String durationFrom) {
		this.durationFrom = durationFrom;
	}
	

}
