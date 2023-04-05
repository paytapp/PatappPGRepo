package com.paymentgateway.razorpay;

public class Transaction {

	private String amount;
	private String currency;
	private String order_id;
	private String email;
	private String contact;
	private String method;
	private String id;
	
	// UPI
	private String vpa;
	
	// Card
	private String number;
	private String name;
	private String expiry_month;
	private String expiry_year;
	private String cvv;

	// NB
	private String bank;
	
	// Wallets
	private String wallet;
	
	private String callback_url;
	private String ip;
	private String referrer;
	private String user_agent;
	
	private String razorpay_payment_id;
	private String razorpay_order_id;
	private String razorpay_signature;
	
	
	private String error_code;
	private String error_description;
	private String error_source;
	private String error_step;
	private String error_reason;
	private String payment_id;
	private String status;
	private String code;
	private String description;
	private String captured;
	
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getOrder_id() {
		return order_id;
	}
	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getVpa() {
		return vpa;
	}
	public void setVpa(String vpa) {
		this.vpa = vpa;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getExpiry_month() {
		return expiry_month;
	}
	public void setExpiry_month(String expiry_month) {
		this.expiry_month = expiry_month;
	}
	public String getExpiry_year() {
		return expiry_year;
	}
	public void setExpiry_year(String expiry_year) {
		this.expiry_year = expiry_year;
	}
	public String getCvv() {
		return cvv;
	}
	public void setCvv(String cvv) {
		this.cvv = cvv;
	}
	public String getBank() {
		return bank;
	}
	public void setBank(String bank) {
		this.bank = bank;
	}
	public String getCallback_url() {
		return callback_url;
	}
	public void setCallback_url(String callback_url) {
		this.callback_url = callback_url;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getReferrer() {
		return referrer;
	}
	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}
	public String getUser_agent() {
		return user_agent;
	}
	public void setUser_agent(String user_agent) {
		this.user_agent = user_agent;
	}
	public String getWallet() {
		return wallet;
	}
	public void setWallet(String wallet) {
		this.wallet = wallet;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRazorpay_payment_id() {
		return razorpay_payment_id;
	}
	public void setRazorpay_payment_id(String razorpay_payment_id) {
		this.razorpay_payment_id = razorpay_payment_id;
	}
	public String getRazorpay_order_id() {
		return razorpay_order_id;
	}
	public void setRazorpay_order_id(String razorpay_order_id) {
		this.razorpay_order_id = razorpay_order_id;
	}
	public String getRazorpay_signature() {
		return razorpay_signature;
	}
	public void setRazorpay_signature(String razorpay_signature) {
		this.razorpay_signature = razorpay_signature;
	}
	public String getError_code() {
		return error_code;
	}
	public void setError_code(String error_code) {
		this.error_code = error_code;
	}
	public String getError_description() {
		return error_description;
	}
	public void setError_description(String error_description) {
		this.error_description = error_description;
	}
	public String getError_source() {
		return error_source;
	}
	public void setError_source(String error_source) {
		this.error_source = error_source;
	}
	public String getError_step() {
		return error_step;
	}
	public void setError_step(String error_step) {
		this.error_step = error_step;
	}
	public String getError_reason() {
		return error_reason;
	}
	public void setError_reason(String error_reason) {
		this.error_reason = error_reason;
	}
	public String getPayment_id() {
		return payment_id;
	}
	public void setPayment_id(String payment_id) {
		this.payment_id = payment_id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCaptured() {
		return captured;
	}
	public void setCaptured(String captured) {
		this.captured = captured;
	}

}
