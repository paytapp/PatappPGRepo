package com.paymentgateway.phonePe;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseBean {

//	{"success":true,"code":"PAYMENT_INITIATED","message":"Payment initiated",
//	"data":{"merchantId":"GAMEZDADDYUAT","merchantTransactionId":"1006",
//	"transactionId":"T2306071554587550922237",
//	"instrumentResponse":{"type":"PAY_PAGE",
//	"redirectInfo":{"url":"https://mercury-uat.phonepe.com/transact/simulator?token=EsK1KHMD5RibgTPNRCk0KzXyGiFbmuGQp52GVhROqBV"
//	,"method":"GET"}}}}
	
	public boolean success;	
	public String code;
	public String message;
	public BigDecimal amount;
	public DataBean data;
	
	
	public ResponseBean(boolean success, String code, String message, DataBean data) {
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

	@JsonGetter("amount")
	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@JsonGetter("data")
	public DataBean getData() {
		return data;
	}

	public void setData(DataBean data) {
		this.data = data;
	}


	
	
	
}
