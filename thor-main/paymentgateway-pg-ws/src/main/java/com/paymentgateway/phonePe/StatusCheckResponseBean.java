package com.paymentgateway.phonePe;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusCheckResponseBean {
	
	public boolean success;	
	public String code;
	public String message;
	public StatusCheckDataBean data;
	
	public StatusCheckResponseBean(boolean success, String code, String message, StatusCheckDataBean data) {
		super();
		this.success = success;
		this.code = code;
		this.message = message;
		this.data = data;
	}

	@JsonGetter("success")
	public boolean getSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	@JsonGetter("code")
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	@JsonGetter("message")
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	@JsonGetter("data")
	public StatusCheckDataBean getData() {
		return data;
	}
	
	public void setData(StatusCheckDataBean data) {
		this.data = data;
	}
	
	
	

}
