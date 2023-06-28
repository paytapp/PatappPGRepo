package com.paymentgateway.grezpay;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Shaiwal
 *
 */

@Service("grezpayTransaction")
public class Transaction {

	private String curr_code;
	private String amount;
	private String desc;
	private String merchant_order_token;
	private String customer_email;
	private String customer_mobile;
	private String customer_first_name;
	private String customer_last_name;
	private String success_url;
	private String fail_url;
	private String pg_cancel_url;
	private String api_key;
	private String status;
	private String data;
	private String transaction_id;
	private String message;
	private String status_code;
	private String ref_link;
	private String identifier;
	private String customer_details;
	private String mid;
	private String password;

	public void setEnrollment(Fields fields) {
		setMerchantInformation(fields);
		setTxnDataDetails(fields);
	}

	public void setStatusFields(Fields fields) {
		setMerchantInformation(fields);
		setTxnStatusDataDetails(fields);
	}

	private void setMerchantInformation(Fields fields) {

		setMid(fields.get(FieldType.MERCHANT_ID.getName()));
		setApi_key(fields.get(FieldType.TXN_KEY.getName()));
		setPassword(fields.get(FieldType.PASSWORD.getName()));
	}

	private void setTxnStatusDataDetails(Fields fields) {
		setIdentifier(fields.get(FieldType.PG_REF_NUM.getName()));
	}

	private void setTxnDataDetails(Fields fields) {

		setCurr_code("INR");
		setMerchant_order_token(fields.get(FieldType.PG_REF_NUM.getName()));

		// Generate a random phone number if not provided by merchant
		int random = (int) Math.floor(Math.random() * (999999999 - 500000000 + 1) + 500000000);
//      String phoneNumber = "9" + ThreadLocalRandom.current().nextInt(500000000, 999999999);
		String phoneNumber = "9" + String.valueOf(random);
		
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			setCustomer_email(fields.get(FieldType.CUST_EMAIL.getName()));
		} else {
			setCustomer_email(phoneNumber.concat("@yahoo.com"));
		}

		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_PHONE.getName()))) {
			setCustomer_mobile(fields.get(FieldType.CUST_PHONE.getName()));
		} else {
			setCustomer_mobile(phoneNumber);
		}

		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))) {
			setCustomer_first_name(fields.get(FieldType.CUST_NAME.getName()));
		} else {
			setCustomer_first_name(PropertiesManager.propertiesMap.get(Constants.GREZPAYFirstName));
		}

		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))) {
			setCustomer_last_name(fields.get(FieldType.CUST_NAME.getName()));
		} else {
			setCustomer_last_name(PropertiesManager.propertiesMap.get(Constants.GREZPAYLastName));
		}

		setApi_key(fields.get(FieldType.TXN_KEY.getName()));

	}

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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getMerchant_order_token() {
		return merchant_order_token;
	}

	public void setMerchant_order_token(String merchant_order_token) {
		this.merchant_order_token = merchant_order_token;
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

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getCustomer_details() {
		return customer_details;
	}

	public void setCustomer_details(String customer_details) {
		this.customer_details = customer_details;
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

	public String getCustomer_email() {
		return customer_email;
	}

	public void setCustomer_email(String customer_email) {
		this.customer_email = customer_email;
	}

}
