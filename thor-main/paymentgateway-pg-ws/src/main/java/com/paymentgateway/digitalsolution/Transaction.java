package com.paymentgateway.digitalsolution;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

/**
 * @author Shaiwal
 *
 */

@Service("digitalsolutionTransaction")
public class Transaction {

	private String token;
	private String clint_ref_id;
	private String amount;
	private String remark;
	private String surl;
	private String furl;
	private String status;
	private String easepayid;
	private String refno;
	
	public void setEnrollment(Fields fields) {
		setMerchantInformation(fields);
		setTxnDataDetails(fields);
	}

	public void setStatusFields(Fields fields) {
		setMerchantInformation(fields);
		setTxnStatusDataDetails(fields);
	}

	private void setMerchantInformation(Fields fields) {
		setToken(fields.get(FieldType.MERCHANT_ID.getName()));
	}

	private void setTxnStatusDataDetails(Fields fields) {
		setClint_ref_id(fields.get(FieldType.PG_REF_NUM.getName()));
	}

	private void setTxnDataDetails(Fields fields) {

		setClint_ref_id(fields.get(FieldType.PG_REF_NUM.getName()));
		setRemark("Online Payment for order");
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getClint_ref_id() {
		return clint_ref_id;
	}

	public void setClint_ref_id(String clint_ref_id) {
		this.clint_ref_id = clint_ref_id;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getSurl() {
		return surl;
	}

	public void setSurl(String surl) {
		this.surl = surl;
	}

	public String getFurl() {
		return furl;
	}

	public void setFurl(String furl) {
		this.furl = furl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEasepayid() {
		return easepayid;
	}

	public void setEasepayid(String easepayid) {
		this.easepayid = easepayid;
	}

	public String getRefno() {
		return refno;
	}

	public void setRefno(String refno) {
		this.refno = refno;
	}

}
