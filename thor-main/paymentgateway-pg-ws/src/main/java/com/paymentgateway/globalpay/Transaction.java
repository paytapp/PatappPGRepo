package com.paymentgateway.globalpay;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

/**
 * @author Shaiwal
 *
 */

@Service("globalpayTransaction")
public class Transaction {

	private String merchant_order_id;
	private String payment_amount;
	private String return_url;
	private String customer_id;
	private String customer_name;
	private String customer_email;
	private String customer_mobile;
	private String player_register_date;
	private String player_deposit_amount;
	private String player_deposit_count;
	private String message;
	private String checkout_url;
	private String transaction_id;
	private String status;
	private String merchant_key;
	private String merchant_id;
	private String data;
	private String payment_status;
	private String bank_rrn;

	public void setEnrollment(Fields fields) {
		setMerchantInformation(fields);
		setTxnDataDetails(fields);
	}

	public void setStatusFields(Fields fields) {
		setMerchantInformation(fields);
		setTxnStatusDataDetails(fields);
	}

	private void setMerchantInformation(Fields fields) {

		setMerchant_id(fields.get(FieldType.MERCHANT_ID.getName()));
		setMerchant_key(fields.get(FieldType.TXN_KEY.getName()));
	}

	private void setTxnStatusDataDetails(Fields fields) {
		setMerchant_order_id(fields.get(FieldType.PG_REF_NUM.getName()));
	}

	private void setTxnDataDetails(Fields fields) {

		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		String strDate = formatter.format(date);

		int random = (int) Math.floor(Math.random() * (999999999 - 500000000 + 1) + 500000000);
//      String phoneNumber = "9" + ThreadLocalRandom.current().nextInt(500000000, 999999999);
		String phoneNumber = "9" + String.valueOf(random);

		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_PHONE.getName()))) {
			setCustomer_mobile(fields.get(FieldType.CUST_PHONE.getName()));
		} else {
			setCustomer_mobile(phoneNumber);
		}

		setMerchant_order_id(fields.get(FieldType.PG_REF_NUM.getName()));
		setCustomer_id(fields.get(FieldType.PG_REF_NUM.getName()));

		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))) {
			setCustomer_name(fields.get(FieldType.CUST_NAME.getName()));
		} else {
			setCustomer_name("John Doe");
		}

		setPlayer_register_date(strDate);
		setPlayer_deposit_amount("1");
		setPlayer_deposit_count("1");

		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			setCustomer_email(fields.get(FieldType.CUST_EMAIL.getName()));
		} else {
			setCustomer_email(phoneNumber + "@gmail.com");
		}

	}

	public String getMerchant_order_id() {
		return merchant_order_id;
	}

	public void setMerchant_order_id(String merchant_order_id) {
		this.merchant_order_id = merchant_order_id;
	}

	public String getPayment_amount() {
		return payment_amount;
	}

	public void setPayment_amount(String payment_amount) {
		this.payment_amount = payment_amount;
	}

	public String getReturn_url() {
		return return_url;
	}

	public void setReturn_url(String return_url) {
		this.return_url = return_url;
	}

	public String getCustomer_id() {
		return customer_id;
	}

	public void setCustomer_id(String customer_id) {
		this.customer_id = customer_id;
	}

	public String getCustomer_name() {
		return customer_name;
	}

	public void setCustomer_name(String customer_name) {
		this.customer_name = customer_name;
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

	public String getPlayer_register_date() {
		return player_register_date;
	}

	public void setPlayer_register_date(String player_register_date) {
		this.player_register_date = player_register_date;
	}

	public String getPlayer_deposit_amount() {
		return player_deposit_amount;
	}

	public void setPlayer_deposit_amount(String player_deposit_amount) {
		this.player_deposit_amount = player_deposit_amount;
	}

	public String getPlayer_deposit_count() {
		return player_deposit_count;
	}

	public void setPlayer_deposit_count(String player_deposit_count) {
		this.player_deposit_count = player_deposit_count;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCheckout_url() {
		return checkout_url;
	}

	public void setCheckout_url(String checkout_url) {
		this.checkout_url = checkout_url;
	}

	public String getTransaction_id() {
		return transaction_id;
	}

	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMerchant_key() {
		return merchant_key;
	}

	public void setMerchant_key(String merchant_key) {
		this.merchant_key = merchant_key;
	}

	public String getMerchant_id() {
		return merchant_id;
	}

	public void setMerchant_id(String merchant_id) {
		this.merchant_id = merchant_id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getPayment_status() {
		return payment_status;
	}

	public void setPayment_status(String payment_status) {
		this.payment_status = payment_status;
	}

	public String getBank_rrn() {
		return bank_rrn;
	}

	public void setBank_rrn(String bank_rrn) {
		this.bank_rrn = bank_rrn;
	}

}
