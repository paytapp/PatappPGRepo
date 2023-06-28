package com.paymentgateway.phonePe;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataBean {

//	"data":{"merchantId":"GAMEZDADDYUAT","merchantTransactionId":"1006",
//	"transactionId":"T2306071554587550922237",
//	"instrumentResponse":{"type":"PAY_PAGE",
//	"redirectInfo":{"url":"https://mercury-uat.phonepe.com/transact/simulator?token=EsK1KHMD5RibgTPNRCk0KzXyGiFbmuGQp52GVhROqBV"
//	,"method":"GET"}}}}
	
	public String merchantId;
	public String merchantTransactionId;
	public String transactionId;	
	public InstrumentResponseBean instrumentResponse;

	
	
	
	public DataBean(String merchantId, String merchantTransactionId, String transactionId,
			InstrumentResponseBean instrumentResponse) {
		super();
		this.merchantId = merchantId;
		this.merchantTransactionId = merchantTransactionId;
		this.transactionId = transactionId;
		this.instrumentResponse = instrumentResponse;
	}

	@JsonGetter("merchantId")
	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	@JsonGetter("merchantTransactionId")
	public String getMerchantTransactionId() {
		return merchantTransactionId;
	}

	public void setMerchantTransactionId(String merchantTransactionId) {
		this.merchantTransactionId = merchantTransactionId;
	}

	@JsonGetter("transactionId")
	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	@JsonGetter("instrumentResponse")
	public InstrumentResponseBean getInstrumentResponse() {
		return instrumentResponse;
	}

	public void setInstrumentResponse(InstrumentResponseBean instrumentResponse) {
		this.instrumentResponse = instrumentResponse;
	}
	
	
	
}
