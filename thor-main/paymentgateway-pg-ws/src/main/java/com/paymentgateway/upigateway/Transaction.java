package com.paymentgateway.upigateway;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Shaiwal
 *
 */

@Service("upigatewayTransaction")
public class Transaction {

	private String key;
	private String client_txn_id;
	private String amount;
	private String p_info;
	private String customer_name;
	private String customer_email;
	private String customer_mobile;
	private String redirect_url;
	private String udf1;
	private String udf2;
	private String udf3;
	private String status;
	private String msg;
	private String data;
	private String order_id;
	private String payment_url;
	private String txn_date;
	private String id;
	private String customer_vpa;
	private String upi_txn_id;
	private String remark;
	private String Merchant;
	private String name;
	private String upi_id;

	public void setEnrollment(Fields fields) {
		setMerchantInformation(fields);
		setTxnDataDetails(fields);
	}

	public void setStatusFields(Fields fields) {
		setMerchantInformation(fields);
		setTxnStatusDataDetails(fields);
	}

	private void setMerchantInformation(Fields fields) {
		setKey(fields.get(FieldType.MERCHANT_ID.getName()));
	}

	private void setTxnStatusDataDetails(Fields fields) {

		try {
			setClient_txn_id(fields.get(FieldType.PG_REF_NUM.getName()));

			String date = fields.get(FieldType.CREATE_DATE.getName());
			SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			SimpleDateFormat format2 = new SimpleDateFormat("dd-MM-yyyy");

			Date date1 = format1.parse(date);
			String date2 = format2.format(date1);
			setTxn_date(date2);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void setTxnDataDetails(Fields fields) {

		setP_info("Online Payment");
		setClient_txn_id(fields.get(FieldType.PG_REF_NUM.getName()));
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
			setCustomer_name(fields.get(FieldType.CUST_NAME.getName()));
		} else {
			setCustomer_name(PropertiesManager.propertiesMap.get(Constants.UPIGATEWAYName));
		}

	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getClient_txn_id() {
		return client_txn_id;
	}

	public void setClient_txn_id(String client_txn_id) {
		this.client_txn_id = client_txn_id;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getP_info() {
		return p_info;
	}

	public void setP_info(String p_info) {
		this.p_info = p_info;
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

	public String getRedirect_url() {
		return redirect_url;
	}

	public void setRedirect_url(String redirect_url) {
		this.redirect_url = redirect_url;
	}

	public String getUdf1() {
		return udf1;
	}

	public void setUdf1(String udf1) {
		this.udf1 = udf1;
	}

	public String getUdf2() {
		return udf2;
	}

	public void setUdf2(String udf2) {
		this.udf2 = udf2;
	}

	public String getUdf3() {
		return udf3;
	}

	public void setUdf3(String udf3) {
		this.udf3 = udf3;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getOrder_id() {
		return order_id;
	}

	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}

	public String getPayment_url() {
		return payment_url;
	}

	public void setPayment_url(String payment_url) {
		this.payment_url = payment_url;
	}

	public String getTxn_date() {
		return txn_date;
	}

	public void setTxn_date(String txn_date) {
		this.txn_date = txn_date;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCustomer_vpa() {
		return customer_vpa;
	}

	public void setCustomer_vpa(String customer_vpa) {
		this.customer_vpa = customer_vpa;
	}

	public String getUpi_txn_id() {
		return upi_txn_id;
	}

	public void setUpi_txn_id(String upi_txn_id) {
		this.upi_txn_id = upi_txn_id;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getMerchant() {
		return Merchant;
	}

	public void setMerchant(String merchant) {
		Merchant = merchant;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUpi_id() {
		return upi_id;
	}

	public void setUpi_id(String upi_id) {
		this.upi_id = upi_id;
	}

}
