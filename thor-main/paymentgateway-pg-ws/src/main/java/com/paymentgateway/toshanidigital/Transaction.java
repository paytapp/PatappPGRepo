package com.paymentgateway.toshanidigital;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Shaiwal
 *
 */

@Service("toshanidigitalTransaction")
public class Transaction {

	private String access_token;
	private String payment_amount;
	private String name;
	private String vpa;
	private String orderid;
	private String result;
	private String message;
	private String data;
	private String order_id;
	private String entity;
	private String qr_id;
	private String image_url;
	private String status;
	private String txn_date;
	private String number;
	private String payments_status;
	private String rrn;

	public void setEnrollment(Fields fields) {
		setMerchantInformation(fields);
		setTxnDataDetails(fields);
	}

	public void setStatusFields(Fields fields) {
		setMerchantInformation(fields);
		setTxnStatusDataDetails(fields);
	}

	private void setMerchantInformation(Fields fields) {
		setAccess_token(fields.get(FieldType.MERCHANT_ID.getName()));
	}

	private void setTxnStatusDataDetails(Fields fields) {

		try {
			setOrderid(fields.get(FieldType.ACQ_ID.getName()));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void setTxnDataDetails(Fields fields) {

		int random = (int) Math.floor(Math.random() * (999999999 - 500000000 + 1) + 500000000);
//      String phoneNumber = "9" + ThreadLocalRandom.current().nextInt(500000000, 999999999);
		String phoneNumber = "9" + String.valueOf(random);

		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_PHONE.getName()))) {
			setNumber(fields.get(FieldType.CUST_PHONE.getName()));
		} else {
			setNumber(phoneNumber);
		}

		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))) {
			setName(fields.get(FieldType.CUST_NAME.getName()));
		} else {
			setName(PropertiesManager.propertiesMap.get(Constants.TOSHANIDIGITALName));
		}

	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getPayment_amount() {
		return payment_amount;
	}

	public void setPayment_amount(String payment_amount) {
		this.payment_amount = payment_amount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVpa() {
		return vpa;
	}

	public void setVpa(String vpa) {
		this.vpa = vpa;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getQr_id() {
		return qr_id;
	}

	public void setQr_id(String qr_id) {
		this.qr_id = qr_id;
	}

	public String getImage_url() {
		return image_url;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTxn_date() {
		return txn_date;
	}

	public void setTxn_date(String txn_date) {
		this.txn_date = txn_date;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getPayments_status() {
		return payments_status;
	}

	public void setPayments_status(String payments_status) {
		this.payments_status = payments_status;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

}
