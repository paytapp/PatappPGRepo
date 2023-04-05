package com.paymentgateway.fss;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.federal.Constants;

@Service("fssTransaction")
public class Transaction {
	private String id;
	private String password;
	private String action;
	private String amt;
	private String currencycode;
	private String trackId;
	private String card;
	private String expmonth;
	private String expyear;
	private String cvv2;
	private String member;
	private String udf1;
	private String udf2;
	private String udf3;
	private String udf4;
	private String udf5;
	private String result;
	private String url;
	private String PAReq;
	private String paymentid;
	private String eci;
	private String PaRes;
	private String auth;
	private String ref;
	private String avr;
	private String postdate;
	private String tranid;
	private String MD;
	private String TermURL;
	private String error_text;
	private String error_code_tag;
	private String error_service_tag;
	private String zip;
	private String addr;
	private String transid;
	private String mopType;
	private String totalAmt;

	public void setRefund(Fields fields) {
		setSupportTransaction(fields);
	}

	public void setStatusEnquiry(Fields fields) {
		setSupportTransaction(fields);
	}

	public void setCapture(Fields fields) {
		setSupportTransaction(fields);
	}

	public void setSupportTransaction(Fields fields) {
		setMerchantInformation(fields);
		setOrderInformation(fields);
		setAction(fields);
		setPreviousTransactionReference(fields);
		setRefundFlag(fields);
	}

	public void setPreviousTransactionReference(Fields fields) {
		Fields previousFields = fields.getPrevious();
		if (null != previousFields) {
			String acqId = previousFields.get(FieldType.ACQ_ID.getName());
			setTranid(acqId);
			setTransId(acqId);

			setMember(previousFields.get(FieldType.CUST_NAME.getName()));
		}
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.STATUS.getName())) {
			String acqId = fields.get(FieldType.ACQ_ID.getName());
			// setTranid(acqId);
			setTransId(acqId);
		}
	}

	public void setAuthorization(Fields fields) {
		String paRes = fields.get(FieldType.PARES.getName());
		if (null == paRes || paRes.isEmpty()) {
			setAuthorizationWithoutPares(fields);
		} else {
			setAuthorizationWithPares(fields);
		}
	}

	private void setAuthorizationWithPares(Fields fields) {
		setPaymentid(fields.get(FieldType.MD.getName()));
		setPaRes(fields.get(FieldType.PARES.getName()));
		setMerchantInformation(fields);
	}

	private void setAuthorizationWithoutPares(Fields fields) {

		// Calling enrollment function because, all mappings are same
		setEnrollment(fields);

		// Setting ECI - According to FSS, if ECI was not present in enrollment
		// response then set 7
		String eci = fields.get(FieldType.ECI.getName());
		if (null == eci || eci.isEmpty()) {
			eci = "7";
		}
		setEci(eci);
	}

	public void setEnrollment(Fields fields) {

		setCardDetails(fields);
		setAction(fields);
		setMerchantInformation(fields);
		setOrderInformation(fields);
		// optional fields for rupay card
		setMopType(fields.get(FieldType.MOP_TYPE.getName()));
	}

	private void setMerchantInformation(Fields fields) {
		setId(fields.get(FieldType.MERCHANT_ID.getName()));
		setPassword(fields.get(FieldType.PASSWORD.getName()));
	}

	private void setOrderInformation(Fields fields) {
		
		setAmt(fields.get(FieldType.AMOUNT.getName()));
		setTotalAmt(fields.get(FieldType.TOTAL_AMOUNT.getName()));
		setCurrencycode(fields.get(FieldType.CURRENCY_CODE.getName()));

		String fssTrackId = fields.get(FieldType.ORIG_TXN_ID.getName());
		if (null == fssTrackId) {
			fssTrackId = fields.get(FieldType.TXN_ID.getName());
		}
		setTrackId(fssTrackId);

		setZip(fields.get(FieldType.CUST_ZIP.getName()));

		// Find out proper use of UDF4, and UDF5
		// UDF1 used for payId in case a TID used for multiple merchants
		setUdf1(fields.get(FieldType.PAY_ID.getName()));
		setUdf2(fields.get(FieldType.CUST_EMAIL.getName()));
		setUdf3(fields.get(FieldType.CUST_PHONE.getName()));

		setUdf4(fields.get(FieldType.TXN_ID.getName()));
		setUdf5("TrackId");
	}

	@SuppressWarnings("incomplete-switch")
	private void setAction(Fields fields) {
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (txnType.equals(TransactionType.ENROLL.getName())) {
			txnType = fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
		}

		String action = null;
		switch (TransactionType.getInstance(txnType)) {
		case AUTHORISE:
			action = "4";
			break;
		case REFUND:
			action = "2";
			break;
		case SALE:
			action = "1";
			break;
		case CAPTURE:
			action = "5";
			break;
		case STATUS:
			action = "8";
			break;
		}

		setAction(action);
	}

	private void setCardDetails(Fields fields) {
		setCard(fields.get(FieldType.CARD_NUMBER.getName()));
		setCvv2(fields.get(FieldType.CVV.getName()));
		String expDate = fields.get(FieldType.CARD_EXP_DT.getName());
		setExpyear(expDate.substring(2, 6));
		setExpmonth(expDate.substring(0, 2));
		setMember(fields.get(FieldType.CUST_NAME.getName()));
	}
	
	private void setRefundFlag(Fields fields) {
		String refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
		if(refundFlag.equals(Constants.PAYMENT_GATEWAY_REFUND_FLAG)){
			setUdf4(Constants.REFUND_INCLUDING_SURCHARGE_FLAG);
		} else {
			setUdf4(Constants.REFUND_TXN_AMOUNT_FLAG);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getAmt() {
		return amt;
	}

	public void setAmt(String amt) {
		this.amt = amt;
	}

	public String getCurrencycode() {
		return currencycode;
	}

	public void setCurrencycode(String currencycode) {
		this.currencycode = currencycode;
	}

	public String getTrackId() {
		return trackId;
	}

	public void setTrackId(String trackId) {
		this.trackId = trackId;
	}

	public String getCard() {
		return card;
	}

	public void setCard(String card) {
		this.card = card;
	}

	public String getExpmonth() {
		return expmonth;
	}

	public void setExpmonth(String expmonth) {
		this.expmonth = expmonth;
	}

	public String getExpyear() {
		return expyear;
	}

	public void setExpyear(String expyear) {
		this.expyear = expyear;
	}

	public String getCvv2() {
		return cvv2;
	}

	public void setCvv2(String cvv2) {
		this.cvv2 = cvv2;
	}

	public String getMember() {
		return member;
	}

	public void setMember(String member) {
		this.member = member;
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

	public String getUdf4() {
		return udf4;
	}

	public void setUdf4(String udf4) {
		this.udf4 = udf4;
	}

	public String getUdf5() {
		return udf5;
	}

	public void setUdf5(String udf5) {
		this.udf5 = udf5;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPAReq() {
		return PAReq;
	}

	public void setPAReq(String pAReq) {
		PAReq = pAReq;
	}

	public String getPaymentid() {
		return paymentid;
	}

	public void setPaymentid(String paymentid) {
		this.paymentid = paymentid;
	}

	public String getEci() {
		return eci;
	}

	public void setEci(String eci) {
		this.eci = eci;
	}

	public String getPaRes() {
		return PaRes;
	}

	public void setPaRes(String paRes) {
		PaRes = paRes;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getAvr() {
		return avr;
	}

	public void setAvr(String avr) {
		this.avr = avr;
	}

	public String getPostdate() {
		return postdate;
	}

	public void setPostdate(String postdate) {
		this.postdate = postdate;
	}

	public String getTranid() {
		return tranid;
	}

	public void setTranid(String tranid) {
		this.tranid = tranid;
	}

	public String getMD() {
		return MD;
	}

	public void setMD(String mD) {
		MD = mD;
	}

	public String getTermURL() {
		return TermURL;
	}

	public void setTermURL(String termURL) {
		TermURL = termURL;
	}

	public String getError_text() {
		return error_text;
	}

	public void setError_text(String error_text) {
		this.error_text = error_text;
	}

	public String getError_code_tag() {
		return error_code_tag;
	}

	public void setError_code_tag(String error_code_tag) {
		this.error_code_tag = error_code_tag;
	}

	public String getError_service_tag() {
		return error_service_tag;
	}

	public void setError_service_tag(String error_service_tag) {
		this.error_service_tag = error_service_tag;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getTransId() {
		return transid;
	}

	public void setTransId(String transId) {
		this.transid = transId;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getTotalAmt() {
		return totalAmt;
	}

	public void setTotalAmt(String totalAmt) {
		this.totalAmt = totalAmt;
	}
}
