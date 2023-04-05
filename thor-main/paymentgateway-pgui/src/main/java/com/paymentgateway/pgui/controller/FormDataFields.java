package com.paymentgateway.pgui.controller;

import java.io.Serializable;

import org.json.JSONObject;

public class FormDataFields implements Serializable {

	private static final long serialVersionUID = 4845673961173373911L;
	private String PAY_ID;
	private String ORDER_ID;
	private String AMOUNT;
	private String TXNTYPE;
	private String CUST_NAME;
	private String CUST_EMAIL;

	@Override
	public String toString() {
		return "[PAY_ID=" + PAY_ID + ", ORDER_ID=" + ORDER_ID + ", AMOUNT=" + AMOUNT + ", TXNTYPE=" + TXNTYPE
				+ ", CUST_NAME=" + CUST_NAME + ", CUST_EMAIL=" + CUST_EMAIL + ", CUST_PHONE=" + CUST_PHONE
				+ ", CUST_ID=" + CUST_ID + ", CURRENCY_CODE=" + CURRENCY_CODE + ", RETURN_URL=" + RETURN_URL + ", HASH="
				+ HASH + "]";
	}

	public JSONObject toJsonData() {
		JSONObject jsonData = new JSONObject();
		jsonData.put("PAY_ID", PAY_ID);
		jsonData.put("ORDER_ID", ORDER_ID);
		jsonData.put("AMOUNT", AMOUNT);
		jsonData.put("CUST_NAME", CUST_NAME);
		jsonData.put("CURRENCY_CODE", CURRENCY_CODE);
		jsonData.put("RETURN_URL", RETURN_URL);

		return jsonData;
	}

	private String CUST_PHONE;
	private String CUST_ID;
	private String CURRENCY_CODE;
	private String RETURN_URL;
	private String HASH;

	public String getPAY_ID() {
		return PAY_ID;
	}

	public void setPAY_ID(String pAY_ID) {
		PAY_ID = pAY_ID;
	}

	public String getORDER_ID() {
		return ORDER_ID;
	}

	public void setORDER_ID(String oRDER_ID) {
		ORDER_ID = oRDER_ID;
	}

	public String getAMOUNT() {
		return AMOUNT;
	}

	public void setAMOUNT(String aMOUNT) {
		AMOUNT = aMOUNT;
	}

	public String getTXNTYPE() {
		return TXNTYPE;
	}

	public void setTXNTYPE(String tXNTYPE) {
		TXNTYPE = tXNTYPE;
	}

	public String getCUST_NAME() {
		return CUST_NAME;
	}

	public void setCUST_NAME(String cUST_NAME) {
		CUST_NAME = cUST_NAME;
	}

	public String getCUST_EMAIL() {
		return CUST_EMAIL;
	}

	public void setCUST_EMAIL(String cUST_EMAIL) {
		CUST_EMAIL = cUST_EMAIL;
	}

	public String getCUST_PHONE() {
		return CUST_PHONE;
	}

	public void setCUST_PHONE(String cUST_PHONE) {
		CUST_PHONE = cUST_PHONE;
	}

	public String getCUST_ID() {
		return CUST_ID;
	}

	public void setCUST_ID(String cUST_ID) {
		CUST_ID = cUST_ID;
	}

	public String getCURRENCY_CODE() {
		return CURRENCY_CODE;
	}

	public void setCURRENCY_CODE(String cURRENCY_CODE) {
		CURRENCY_CODE = cURRENCY_CODE;
	}

	public String getRETURN_URL() {
		return RETURN_URL;
	}

	public void setRETURN_URL(String rETURN_URL) {
		RETURN_URL = rETURN_URL;
	}

	public String getHASH() {
		return HASH;
	}

	public void setHASH(String hASH) {
		HASH = hASH;
	}

}
