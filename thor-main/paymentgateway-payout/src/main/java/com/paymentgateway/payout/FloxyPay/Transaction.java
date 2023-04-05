package com.paymentgateway.payout.FloxyPay;

import org.json.JSONObject;

import com.paymentgateway.commons.exception.SystemException;

public class Transaction {
	private String status;
	private String pgRespMsg;
	private String utr;
	private String acqId;

	public Transaction(String response) throws SystemException {

		JSONObject responseJson = new JSONObject(response);

		if (responseJson.has("status")) {
			setStatus(responseJson.getString("status"));
		}

		if (responseJson.has("systemId")) {
			setAcqId(responseJson.getString("systemId"));
		}

		if (responseJson.has("message")) {
			setPgRespMsg(responseJson.getString("message"));
		}

		if (responseJson.has("utr"))
			setUtr(responseJson.getString("utr"));

		if (responseJson.has("orderid") && !responseJson.isNull("orderid"))
			setAcqId(responseJson.getString("orderid"));
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPgRespMsg() {
		return pgRespMsg;
	}

	public void setPgRespMsg(String pgRespMsg) {
		this.pgRespMsg = pgRespMsg;
	}

	public String getAcqId() {
		return acqId;
	}

	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}

	public String getUtr() {
		return utr;
	}

	public void setUtr(String utr) {
		this.utr = utr;
	}

	public String getStatus() {
		return status;
	}

}
