package com.paymentgateway.cyberSource;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

@Service("cyberSourceTransaction")
public class Transaction {
	
	private String currency;
	private String xid;
	private String pares;
	private String responseCode;
	private String acsURL;
	private String paReq;
	private String requestId;
	private String recoId;
	private String expYear;
	private String expMonth;
	private String veresEnrolled;
	
	public void setCardDetails(Fields fields) {
		
		String expDate = fields.get(FieldType.CARD_EXP_DT.getName());
		setExpYear(expDate.substring(2, 6));
		setExpMonth(expDate.substring(0, 2));
	}
	
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getXid() {
		return xid;
	}
	public void setXid(String xid) {
		this.xid = xid;
	}
	public String getPares() {
		return pares;
	}
	public void setPares(String pares) {
		this.pares = pares;
	}
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public String getAcsURL() {
		return acsURL;
	}
	public void setAcsURL(String acsURL) {
		this.acsURL = acsURL;
	}
	public String getPaReq() {
		return paReq;
	}
	public void setPaReq(String paReq) {
		this.paReq = paReq;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getRecoId() {
		return recoId;
	}
	public void setRecoId(String recoId) {
		this.recoId = recoId;
	}

	public String getExpYear() {
		return expYear;
	}

	public void setExpYear(String expYear) {
		this.expYear = expYear;
	}

	public String getExpMonth() {
		return expMonth;
	}

	public void setExpMonth(String expMonth) {
		this.expMonth = expMonth;
	}

	public String getVeresEnrolled() {
		return veresEnrolled;
	}

	public void setVeresEnrolled(String veresEnrolled) {
		this.veresEnrolled = veresEnrolled;
	}
	

}
