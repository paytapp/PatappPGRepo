package com.paymentgateway.icici.composite.api;

import org.springframework.stereotype.Service;

@Service("iciciCompositeTransaction")
public class Transaction {
	
	private String actCode;
	private String response;
	private String rrn;
	private String tranRefNo;
	private String beneName;
	private boolean success;
	
	private String tranLogId;
	private String message;
	private String userProfile;
	private String mobileAppData;
	private String payerRespCode;
	private String PayeeRespCode;
	private String seqNo;
	
	private String bnfId;
	private String errorCode;
	
	private String status;
	private String utr;
	private String reqId;
	private String uniqueId;
	private String responseCode;
	
	
	public String getActCode() {
		return actCode;
	}
	public void setActCode(String actCode) {
		this.actCode = actCode;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public String getRrn() {
		return rrn;
	}
	public void setRrn(String rrn) {
		this.rrn = rrn;
	}
	public String getTranRefNo() {
		return tranRefNo;
	}
	public void setTranRefNo(String tranRefNo) {
		this.tranRefNo = tranRefNo;
	}
	public String getBeneName() {
		return beneName;
	}
	public void setBeneName(String beneName) {
		this.beneName = beneName;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getTranLogId() {
		return tranLogId;
	}
	public void setTranLogId(String tranLogId) {
		this.tranLogId = tranLogId;
	}

	public String getUserProfile() {
		return userProfile;
	}
	public void setUserProfile(String userProfile) {
		this.userProfile = userProfile;
	}
	public String getMobileAppData() {
		return mobileAppData;
	}
	public void setMobileAppData(String mobileAppData) {
		this.mobileAppData = mobileAppData;
	}
	public String getPayerRespCode() {
		return payerRespCode;
	}
	public void setPayerRespCode(String payerRespCode) {
		this.payerRespCode = payerRespCode;
	}
	public String getPayeeRespCode() {
		return PayeeRespCode;
	}
	public void setPayeeRespCode(String payeeRespCode) {
		PayeeRespCode = payeeRespCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getBnfId() {
		return bnfId;
	}
	public void setBnfId(String bnfId) {
		this.bnfId = bnfId;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(String seqNo) {
		this.seqNo = seqNo;
	}
	public String getStatus() {
		return status;
	}
	public String getUtr() {
		return utr;
	}
	public String getReqId() {
		return reqId;
	}
	public String getUniqueId() {
		return uniqueId;
	}
	public String getResponseCode() {
		return responseCode;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public void setUtr(String utr) {
		this.utr = utr;
	}
	public void setReqId(String reqId) {
		this.reqId = reqId;
	}
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	
	

}
