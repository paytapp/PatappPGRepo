package com.paymentgateway.vepay;

import org.springframework.stereotype.Service;

@Service("vepayTransaction")
public class Transaction {

	private String mid;
	private String password;
	private String curr_code;
	private String amount;
	private String merchant_order_token;
	private String customer_email;
	private String customer_mobile;
	private String customer_first_name;
	private String customer_last_name;
	private String success_url;
	private String fail_url;
	private String pg_cancel_url;
	private String api_key;
	
	private String payment_token;
	private String payment_method_details;
	private String status;
	private String data;
	private String transaction_id;
	private String message;
	private String status_code;
	private String ref_link;
	
	private String payment_link;
	private String payment_s2s;
	private String identifier;
	private String type;
	private String bank_code;
	private String vpa;
	private String provider;
	
	public String getCurr_code() {
		return curr_code;
	}
	public void setCurr_code(String curr_code) {
		this.curr_code = curr_code;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getMerchant_order_token() {
		return merchant_order_token;
	}
	public void setMerchant_order_token(String merchant_order_token) {
		this.merchant_order_token = merchant_order_token;
	}
	public String getCustomer_email() {
		return customer_email;
	}
	public void setCustomer_email(String customer_email) {
		this.customer_email = customer_email;
	}
	public String getCustomer_mobile() {
		return customer_mobile;
	}
	public void setCustomer_mobile(String customer_mobile) {
		this.customer_mobile = customer_mobile;
	}
	public String getCustomer_first_name() {
		return customer_first_name;
	}
	public void setCustomer_first_name(String customer_first_name) {
		this.customer_first_name = customer_first_name;
	}
	public String getCustomer_last_name() {
		return customer_last_name;
	}
	public void setCustomer_last_name(String customer_last_name) {
		this.customer_last_name = customer_last_name;
	}
	public String getSuccess_url() {
		return success_url;
	}
	public void setSuccess_url(String success_url) {
		this.success_url = success_url;
	}
	public String getFail_url() {
		return fail_url;
	}
	public void setFail_url(String fail_url) {
		this.fail_url = fail_url;
	}
	public String getPg_cancel_url() {
		return pg_cancel_url;
	}
	public void setPg_cancel_url(String pg_cancel_url) {
		this.pg_cancel_url = pg_cancel_url;
	}
	public String getApi_key() {
		return api_key;
	}
	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}
	public String getPayment_token() {
		return payment_token;
	}
	public void setPayment_token(String payment_token) {
		this.payment_token = payment_token;
	}
	public String getPayment_method_details() {
		return payment_method_details;
	}
	public void setPayment_method_details(String payment_method_details) {
		this.payment_method_details = payment_method_details;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getTransaction_id() {
		return transaction_id;
	}
	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getStatus_code() {
		return status_code;
	}
	public void setStatus_code(String status_code) {
		this.status_code = status_code;
	}
	public String getRef_link() {
		return ref_link;
	}
	public void setRef_link(String ref_link) {
		this.ref_link = ref_link;
	}
	public String getPayment_link() {
		return payment_link;
	}
	public void setPayment_link(String payment_link) {
		this.payment_link = payment_link;
	}
	public String getPayment_s2s() {
		return payment_s2s;
	}
	public void setPayment_s2s(String payment_s2s) {
		this.payment_s2s = payment_s2s;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getBank_code() {
		return bank_code;
	}
	public void setBank_code(String bank_code) {
		this.bank_code = bank_code;
	}
	public String getVpa() {
		return vpa;
	}
	public void setVpa(String vpa) {
		this.vpa = vpa;
	}
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	
}
