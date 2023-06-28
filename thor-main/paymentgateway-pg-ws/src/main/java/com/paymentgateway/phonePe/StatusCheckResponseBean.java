package com.paymentgateway.phonePe;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusCheckResponseBean {
	
	public String success;	
	public String code;
	public String message;
	public StatusCheckDataBean data;
	
	@JsonGetter("success")
	public String getSuccess() {
		return success;
	}
	
	public void setSuccess(String success) {
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
