package com.paymentgateway.payout.apexPay;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.paymentgateway.commons.exception.SystemException;

public class Transaction {

	private String status;
	private String msg;
	private String rrn;

	public static Transaction toTransact(String response) throws SystemException {
		Transaction txn = new Transaction();

		JSONObject respObj = new JSONObject(response);

		if (respObj.has("STATUS") && respObj.get("STATUS").equals("SUCCESS")) {
			txn.setStatus((String) respObj.get("STATUS"));
			txn.setMsg((String) respObj.get("MSG"));
			txn.setRrn((String) respObj.get("RRN"));

		} else if (respObj.has("STATUS") && respObj.get("STATUS").equals("PENDING") || respObj.get("STATUS").equals("HOLD")) {
			txn.setStatus((String) respObj.get("STATUS"));

			if (respObj.has("MSG"))
				txn.setMsg((String) respObj.get("MSG"));
			if (respObj.has("RRN"))
				txn.setRrn((String) respObj.get("RRN"));

		} else if (respObj.has("STATUS") && respObj.get("STATUS").equals("FAILED")) {
			txn.setStatus((String) respObj.get("STATUS"));
			if (respObj.has("MSG")) {
				if (respObj.get("MSG").toString().contains("{")) {

					JSONObject msgObj = respObj.getJSONObject("MSG");
					Iterator<String> keys = msgObj.keys();

					if (keys.hasNext()) {
						JSONArray fieldMsg = msgObj.getJSONArray(keys.next());
						txn.setMsg(fieldMsg.getString(0));
					}

				} else {
					txn.setMsg((String) respObj.get("MSG"));
				}

			}

			if (respObj.has("RRN") && respObj.get("RRN").equals("NA"))
				txn.setRrn((String) respObj.get("RRN"));
		}

		return txn;
	}
	
	public static Transaction toStatus(String response) throws SystemException {
		Transaction txn = new Transaction();

		JSONObject respObj = new JSONObject(response);

		if (respObj.has("STATUS") && respObj.get("STATUS").equals("SUCCESS")) {
			txn.setStatus((String) respObj.get("STATUS"));
			txn.setMsg((String) respObj.get("MSG"));
			
			if (response.contains("opid")) {
				if (respObj.has("opid")) {
					if (respObj.get("opid") != null ) {
						txn.setRrn((String) respObj.get("opid"));
					}
				}
			}

		} else if (respObj.has("STATUS") && respObj.get("STATUS").equals("PENDING") || respObj.get("STATUS").equals("HOLD")) {
			txn.setStatus((String) respObj.get("STATUS"));

			if (respObj.has("MSG"))
				txn.setMsg((String) respObj.get("MSG"));
			
		} else if (respObj.has("STATUS") && respObj.get("STATUS").equals("FAILED")) {
			txn.setStatus((String) respObj.get("STATUS"));
			if (respObj.has("MSG")) {
				if (respObj.get("MSG").toString().contains("{")) {

					JSONObject msgObj = respObj.getJSONObject("MSG");
					Iterator<String> keys = msgObj.keys();

					if (keys.hasNext()) {
						JSONArray fieldMsg = msgObj.getJSONArray(keys.next());
						txn.setMsg(fieldMsg.getString(0));
					}

				} else {
					txn.setMsg((String) respObj.get("MSG"));
				}
			}

			if (response.contains("opid")) {
				if (respObj.has("opid") && respObj.get("opid").equals("NA")) {

					if (respObj.get("opid") != null ) {
						txn.setRrn((String) respObj.get("opid"));
					}
					
				} 
			}
			
		}else if(respObj.has("STATUS") && respObj.get("STATUS").equals("REFUND")) {
			txn.setStatus((String) respObj.get("STATUS"));
			
			if (response.contains("opid")) {
				if (respObj.has("opid") && respObj.get("opid").equals("NA")) {
					if (respObj.get("opid") != null ) {
						txn.setMsg((String) respObj.get("opid"));
					}
					
				}
			}
		}

		return txn;
	}

	
	public static Transaction toStatusForCallback(String response) throws SystemException {
		Transaction txn = new Transaction();

		JSONObject respObj = new JSONObject(response);

		if (respObj.has("STATUS") && respObj.get("STATUS").equals("SUCCESS")) {
			txn.setStatus((String) respObj.get("STATUS"));
			txn.setMsg((String) respObj.get("MSG"));
			
			if (response.contains("RRN")) {
				if (respObj.has("RRN")) {
					txn.setRrn((String) respObj.get("RRN"));
				}
			}

		} else if (respObj.has("STATUS") && respObj.get("STATUS").equals("PENDING") || respObj.get("STATUS").equals("HOLD")) {
			txn.setStatus((String) respObj.get("STATUS"));

			if (respObj.has("MSG"))
				txn.setMsg((String) respObj.get("MSG"));

		} else if (respObj.has("STATUS") && respObj.get("STATUS").equals("FAILED")) {
			
			txn.setStatus((String) respObj.get("STATUS"));
			txn.setMsg("FAILED");
			
		}else if(respObj.has("STATUS") && respObj.get("STATUS").equals("REFUND")) {
			txn.setStatus((String) respObj.get("STATUS"));
			txn.setMsg("FAILED");
		}

		return txn;
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

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

}
