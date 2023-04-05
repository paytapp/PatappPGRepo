package com.paymentgateway.payout.fonePaisa;

import org.json.JSONObject;

import com.paymentgateway.commons.exception.SystemException;

public class Transaction {
	private String status;
	private String pgRespMsg;
	private String pgRespCode;
	private String rrn;
	private String acqId;
	private String merchantRefNo;

	public Transaction(String response) throws SystemException {

		JSONObject responseJson = new JSONObject(response);

		if (responseJson.has("data")) {
			JSONObject dataJsonObj = responseJson.getJSONObject("data");

			if (dataJsonObj.has("status"))
				setStatus(dataJsonObj.getString("status"));

			if (dataJsonObj.has("internal_id") && !dataJsonObj.isNull("internal_id"))
				setAcqId(dataJsonObj.getString("internal_id"));

			if (dataJsonObj.has("code") && !dataJsonObj.isNull("code"))
				setPgRespCode(dataJsonObj.getString("code"));
			if (dataJsonObj.has("message") && !dataJsonObj.isNull("message"))
				setPgRespMsg(dataJsonObj.getString("message"));

			if (dataJsonObj.has("transaction_id") && !dataJsonObj.isNull("transaction_id")) {
				setRrn(dataJsonObj.getString("transaction_id"));
			}

			if (dataJsonObj.has("error_message") && !dataJsonObj.isNull("error_message")) {
				setPgRespMsg(dataJsonObj.getString("error_message"));
			}
		
			if (dataJsonObj.has("merchant_reference_number") && !dataJsonObj.isNull("merchant_reference_number")) {
				setMerchantRefNo(dataJsonObj.getString("merchant_reference_number"));
			}

		} else if (responseJson.has("error")) {

			if (responseJson.has("status") && !responseJson.isNull("status"))
				setStatus(responseJson.getString("status"));
			else
				setStatus("failed");

			if (responseJson.has("code") && !responseJson.isNull("code"))
				setPgRespCode(responseJson.getString("code"));
			if (responseJson.has("message") && !responseJson.isNull("message"))
				setPgRespMsg(responseJson.getString("message"));
			
			if (responseJson.has("merchant_reference_number") && !responseJson.isNull("merchant_reference_number")) {
				setMerchantRefNo(responseJson.getString("merchant_reference_number"));
			}

		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

	public String getPgRespMsg() {
		return pgRespMsg;
	}

	public void setPgRespMsg(String pgRespMsg) {
		this.pgRespMsg = pgRespMsg;
	}

	public String getPgRespCode() {
		return pgRespCode;
	}

	public void setPgRespCode(String pgRespCode) {
		this.pgRespCode = pgRespCode;
	}

	public String getAcqId() {
		return acqId;
	}

	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}

	public String getMerchantRefNo() {
		return merchantRefNo;
	}

	public void setMerchantRefNo(String merchantRefNo) {
		this.merchantRefNo = merchantRefNo;
	}

}
