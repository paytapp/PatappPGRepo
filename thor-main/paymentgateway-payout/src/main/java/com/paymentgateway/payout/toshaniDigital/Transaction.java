package com.paymentgateway.payout.toshaniDigital;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Transaction {

	private int result;
	private String message;
	private String orderId;
	private String txnId;
	private String status;
	private String utr;
	private String charges;
	private String tds;
	private String gst;
	private String txn_date;
	private String txnAmount;

	public Transaction(String response) {

		if (StringUtils.isNotBlank(response)) {
			JSONObject respObj = new JSONObject(response);

			if (respObj.has("message"))
				setMessage(respObj.getString("message"));

			if (respObj.has("result"))
				setResult(respObj.getInt("result"));

			if (respObj.has("data")) {
				JSONArray arrayData = respObj.getJSONArray("data");
				JSONObject dataObj = arrayData.getJSONObject(0);

				if (dataObj.has("order_id"))
					setOrderId(dataObj.get("order_id").toString());

				if (dataObj.has("status"))
					setStatus(dataObj.get("status").toString());

				if (dataObj.has("utr"))
					setUtr(dataObj.get("utr").toString());

				if (dataObj.has("txnAmount"))
					setTxnAmount(dataObj.get("txnAmount").toString());

				if (dataObj.has("charge"))
					setCharges(dataObj.get("charge").toString());

				if (dataObj.has("tds"))
					setTds(dataObj.get("tds").toString());

				if (dataObj.has("gst"))
					setGst(dataObj.get("gst").toString());

				if (dataObj.has("txn_date"))
					setTxn_date(dataObj.get("txn_date").toString());

			}

		}

	}

	public Transaction() {
		
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUtr() {
		return utr;
	}

	public void setUtr(String utr) {
		this.utr = utr;
	}

	public String getCharges() {
		return charges;
	}

	public void setCharges(String charges) {
		this.charges = charges;
	}

	public String getTds() {
		return tds;
	}

	public void setTds(String tds) {
		this.tds = tds;
	}

	public String getGst() {
		return gst;
	}

	public void setGst(String gst) {
		this.gst = gst;
	}

	public String getTxn_date() {
		return txn_date;
	}

	public void setTxn_date(String txn_date) {
		this.txn_date = txn_date;
	}

	public String getTxnAmount() {
		return txnAmount;
	}

	public void setTxnAmount(String txnAmount) {
		this.txnAmount = txnAmount;
	}

}
