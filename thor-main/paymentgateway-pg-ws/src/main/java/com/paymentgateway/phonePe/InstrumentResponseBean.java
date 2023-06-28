package com.paymentgateway.phonePe;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstrumentResponseBean {
	
//	"instrumentResponse":{"type":"PAY_PAGE",
//	"redirectInfo":{"url":"https://mercury-uat.phonepe.com/transact/simulator?token=EsK1KHMD5RibgTPNRCk0KzXyGiFbmuGQp52GVhROqBV"
//	,"method":"GET"}}}}
	
	
	
	public String type;
	public RedirectInfoBean redirectInfo;
	
	
	
	public InstrumentResponseBean(String type, RedirectInfoBean redirectInfo) {
		super();
		this.type = type;
		this.redirectInfo = redirectInfo;
	}

	@JsonGetter("type")
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	@JsonGetter("redirectInfo")
	public RedirectInfoBean getRedirectInfo() {
		return redirectInfo;
	}
	
	public void setRedirectInfo(RedirectInfoBean redirectInfo) {
		this.redirectInfo = redirectInfo;
	}
	
	
	
}
