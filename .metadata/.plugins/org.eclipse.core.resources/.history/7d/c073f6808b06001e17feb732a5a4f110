package com.paymentgateway.phonePe;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RedirectInfoBean {

	

//	"redirectInfo":{"url":"https://mercury-uat.phonepe.com/transact/simulator?token=EsK1KHMD5RibgTPNRCk0KzXyGiFbmuGQp52GVhROqBV"
//	,"method":"GET"}}}}
	
	public String url;
	public String method;
	
	
	
	
	public RedirectInfoBean(String url, String method) {
		super();
		this.url = url;
		this.method = method;
	}

	@JsonGetter("data")
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	@JsonGetter("data")
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	
	
	
	
}
