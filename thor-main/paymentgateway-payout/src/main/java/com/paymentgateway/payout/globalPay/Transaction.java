package com.paymentgateway.payout.globalPay;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class Transaction {

	private String message;
	private String responseCode;
	private String orderId;
	private String status;
	private String status2;
	private String utr;
	private String rrn;

	public Transaction(String response) {

		if (StringUtils.isNotBlank(response)) {
			JSONObject respObj = new JSONObject(response);

			if (respObj.has("message"))
				setMessage(respObj.getString("message"));

			if (respObj.has("status"))
				setStatus(respObj.get("status").toString());

			if (respObj.has("response_code"))
				setResponseCode(respObj.get("response_code").toString());

			if (respObj.has("data")) {
				JSONObject dataObj = new JSONObject(respObj.get("data").toString());

				if (dataObj.has("ref_id"))
					setOrderId(dataObj.get("ref_id").toString());

				if (dataObj.has("payout_id"))
					setUtr(dataObj.get("payout_id").toString());
				
				if (dataObj.has("bank_rrn"))
					setRrn(dataObj.get("bank_rrn").toString());
				
				if (dataObj.has("payout_status"))
					setStatus2(dataObj.get("payout_status").toString());

			}

		}

	}

	public Transaction() {

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

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

	public String getStatus2() {
		return status2;
	}

	public void setStatus2(String status2) {
		this.status2 = status2;
	}

}
