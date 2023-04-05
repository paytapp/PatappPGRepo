package com.paymentgateway.floxypay;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Shaiwal
 *
 */

@Service("floxypayTransaction")
public class Transaction {

	private String xkey;
	private String xsecret;
	private String amount;
	private String orderid;
	private String customerName	;
	private String customerMobile;
	private String CustomerEmail;
	private String currency;
	private String method;
	private String status;
	private String systemid;
	
	public void setEnrollment(Fields fields,UserSettingData userSettingData) {
		setMerchantInformation(fields);
		setTxnDataDetails(fields,userSettingData);
	}
	
	public void setStatusFields(Fields fields) {
		setMerchantInformation(fields);
		setTxnStatusDataDetails(fields);
	}

	private void setMerchantInformation(Fields fields) {
		
		setXkey(fields.get(FieldType.MERCHANT_ID.getName()));
		setXsecret(fields.get(FieldType.TXN_KEY.getName()));
	}

	private void setTxnStatusDataDetails(Fields fields) {
	    setOrderid(fields.get(FieldType.PG_REF_NUM.getName()));
	}
	
	private void setTxnDataDetails(Fields fields, UserSettingData userSettingData) {

		StringBuilder custName = new StringBuilder();
		setOrderid(fields.get(FieldType.PG_REF_NUM.getName()));
		
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))) {
			custName.append(fields.get(FieldType.CUST_NAME.getName()));
		}
		else {
		    custName.append(PropertiesManager.propertiesMap.get("FLOXYPAY_Cust_Name"));
		}
		
		custName.append("&");
		custName.append(userSettingData.getBusinessName());
		setCustomerName(custName.toString());
		
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_PHONE.getName()))) {
			setCustomerMobile(fields.get(FieldType.CUST_PHONE.getName()));
		}
		else {
			setCustomerMobile(PropertiesManager.propertiesMap.get("FLOXYPAY_Cust_Mobile"));
		}
		
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			setCustomerEmail(fields.get(FieldType.CUST_EMAIL.getName()));
		}
		else {
			setCustomerEmail(PropertiesManager.propertiesMap.get("FLOXYPAY_Cust_Email"));
		}
	}

	public String getXkey() {
		return xkey;
	}

	public void setXkey(String xkey) {
		this.xkey = xkey;
	}

	public String getXsecret() {
		return xsecret;
	}

	public void setXsecret(String xsecret) {
		this.xsecret = xsecret;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomerMobile() {
		return customerMobile;
	}

	public void setCustomerMobile(String customerMobile) {
		this.customerMobile = customerMobile;
	}

	public String getCustomerEmail() {
		return CustomerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		CustomerEmail = customerEmail;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSystemid() {
		return systemid;
	}

	public void setSystemid(String systemid) {
		this.systemid = systemid;
	}
	
}
