package com.paymentgateway.safexpay;

import org.springframework.stereotype.Service;

@Service("safexpayTransaction")
public class Transaction {

	private String ag_id;
	private String me_id;
	private String txnKey;
	private String order_no;
	private String amount;
	private String country;
	private String currency;
	private String txn_type;
	private String success_url;
	private String failure_url;
	private String channel;
	private String emi_months;
	
	private String pg_id;
	private String paymode;
	private String scheme;
	private String card_no;
	private String exp_month;
	private String exp_year;
	private String cvv;
	private String card_name;
	
	private String cust_name;
	private String email_id;
	private String mobile_no;
	private String unique_id;
	private String is_logged_in;
	
	private String bill_address;
	private String bill_city;
	private String bill_state;
	private String bill_country;
	private String bill_zip;
	
	private String ship_address;
	private String ship_city;
	private String ship_state;
	private String ship_country;
	private String ship_zip;
	private String ship_days;
	private String address_count;
	
	private String item_count;
	private String item_value;
	private String item_category;
	
	private String udf_1;
	private String udf_2;
	private String udf_3;
	private String udf_4;
	private String udf_5;

	private String vpa_address;
	
	
	private String txn_date;
	private String txn_time;
	private String ag_ref;
	private String pg_ref;
	private String res_code;
	private String res_message;
	private String pg_details;
	private String status;
	private String refund_ref;
	
	public String getAg_id() {
		return ag_id;
	}
	public void setAg_id(String ag_id) {
		this.ag_id = ag_id;
	}
	public String getMe_id() {
		return me_id;
	}
	public void setMe_id(String me_id) {
		this.me_id = me_id;
	}
	public String getOrder_no() {
		return order_no;
	}
	public void setOrder_no(String order_no) {
		this.order_no = order_no;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getTxn_type() {
		return txn_type;
	}
	public void setTxn_type(String txn_type) {
		this.txn_type = txn_type;
	}
	public String getSuccess_url() {
		return success_url;
	}
	public void setSuccess_url(String success_url) {
		this.success_url = success_url;
	}
	public String getFailure_url() {
		return failure_url;
	}
	public void setFailure_url(String failure_url) {
		this.failure_url = failure_url;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getPg_id() {
		return pg_id;
	}
	public void setPg_id(String pg_id) {
		this.pg_id = pg_id;
	}
	public String getPaymode() {
		return paymode;
	}
	public void setPaymode(String paymode) {
		this.paymode = paymode;
	}
	public String getCard_no() {
		return card_no;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public String getExp_month() {
		return exp_month;
	}
	public void setExp_month(String exp_month) {
		this.exp_month = exp_month;
	}
	public String getExp_year() {
		return exp_year;
	}
	public void setExp_year(String exp_year) {
		this.exp_year = exp_year;
	}
	public String getCvv() {
		return cvv;
	}
	public void setCvv(String cvv) {
		this.cvv = cvv;
	}
	public String getCard_name() {
		return card_name;
	}
	public void setCard_name(String card_name) {
		this.card_name = card_name;
	}
	public String getCust_name() {
		return cust_name;
	}
	public void setCust_name(String cust_name) {
		this.cust_name = cust_name;
	}
	public String getEmail_id() {
		return email_id;
	}
	public void setEmail_id(String email_id) {
		this.email_id = email_id;
	}
	public String getMobile_no() {
		return mobile_no;
	}
	public void setMobile_no(String mobile_no) {
		this.mobile_no = mobile_no;
	}
	public String getUnique_id() {
		return unique_id;
	}
	public void setUnique_id(String unique_id) {
		this.unique_id = unique_id;
	}
	public String getIs_logged_in() {
		return is_logged_in;
	}
	public void setIs_logged_in(String is_logged_in) {
		this.is_logged_in = is_logged_in;
	}
	public String getBill_address() {
		return bill_address;
	}
	public void setBill_address(String bill_address) {
		this.bill_address = bill_address;
	}
	public String getBill_city() {
		return bill_city;
	}
	public void setBill_city(String bill_city) {
		this.bill_city = bill_city;
	}
	public String getBill_state() {
		return bill_state;
	}
	public void setBill_state(String bill_state) {
		this.bill_state = bill_state;
	}
	public String getBill_country() {
		return bill_country;
	}
	public void setBill_country(String bill_country) {
		this.bill_country = bill_country;
	}
	public String getBill_zip() {
		return bill_zip;
	}
	public void setBill_zip(String bill_zip) {
		this.bill_zip = bill_zip;
	}
	public String getShip_address() {
		return ship_address;
	}
	public void setShip_address(String ship_address) {
		this.ship_address = ship_address;
	}
	public String getShip_city() {
		return ship_city;
	}
	public void setShip_city(String ship_city) {
		this.ship_city = ship_city;
	}
	public String getShip_state() {
		return ship_state;
	}
	public void setShip_state(String ship_state) {
		this.ship_state = ship_state;
	}
	public String getShip_country() {
		return ship_country;
	}
	public void setShip_country(String ship_country) {
		this.ship_country = ship_country;
	}
	public String getShip_zip() {
		return ship_zip;
	}
	public void setShip_zip(String ship_zip) {
		this.ship_zip = ship_zip;
	}
	public String getShip_days() {
		return ship_days;
	}
	public void setShip_days(String ship_days) {
		this.ship_days = ship_days;
	}
	public String getAddress_count() {
		return address_count;
	}
	public void setAddress_count(String address_count) {
		this.address_count = address_count;
	}
	public String getItem_count() {
		return item_count;
	}
	public void setItem_count(String item_count) {
		this.item_count = item_count;
	}
	public String getItem_value() {
		return item_value;
	}
	public void setItem_value(String item_value) {
		this.item_value = item_value;
	}
	public String getItem_category() {
		return item_category;
	}
	public void setItem_category(String item_category) {
		this.item_category = item_category;
	}
	public String getUdf_1() {
		return udf_1;
	}
	public void setUdf_1(String udf_1) {
		this.udf_1 = udf_1;
	}
	public String getUdf_2() {
		return udf_2;
	}
	public void setUdf_2(String udf_2) {
		this.udf_2 = udf_2;
	}
	public String getUdf_3() {
		return udf_3;
	}
	public void setUdf_3(String udf_3) {
		this.udf_3 = udf_3;
	}
	public String getUdf_4() {
		return udf_4;
	}
	public void setUdf_4(String udf_4) {
		this.udf_4 = udf_4;
	}
	public String getUdf_5() {
		return udf_5;
	}
	public void setUdf_5(String udf_5) {
		this.udf_5 = udf_5;
	}
	public String getTxn_date() {
		return txn_date;
	}
	public void setTxn_date(String txn_date) {
		this.txn_date = txn_date;
	}
	public String getTxn_time() {
		return txn_time;
	}
	public void setTxn_time(String txn_time) {
		this.txn_time = txn_time;
	}
	public String getAg_ref() {
		return ag_ref;
	}
	public void setAg_ref(String ag_ref) {
		this.ag_ref = ag_ref;
	}
	public String getPg_ref() {
		return pg_ref;
	}
	public void setPg_ref(String pg_ref) {
		this.pg_ref = pg_ref;
	}
	public String getRes_code() {
		return res_code;
	}
	public void setRes_code(String res_code) {
		this.res_code = res_code;
	}
	public String getRes_message() {
		return res_message;
	}
	public void setRes_message(String res_message) {
		this.res_message = res_message;
	}
	public String getPg_details() {
		return pg_details;
	}
	public void setPg_details(String pg_details) {
		this.pg_details = pg_details;
	}
	public String getScheme() {
		return scheme;
	}
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
	public String getTxnKey() {
		return txnKey;
	}
	public void setTxnKey(String txnKey) {
		this.txnKey = txnKey;
	}
	public String getEmi_months() {
		return emi_months;
	}
	public void setEmi_months(String emi_months) {
		this.emi_months = emi_months;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getVpa_address() {
		return vpa_address;
	}
	public void setVpa_address(String vpa_address) {
		this.vpa_address = vpa_address;
	}
	public String getRefund_ref() {
		return refund_ref;
	}
	public void setRefund_ref(String refund_ref) {
		this.refund_ref = refund_ref;
	}
	
	
	
}
