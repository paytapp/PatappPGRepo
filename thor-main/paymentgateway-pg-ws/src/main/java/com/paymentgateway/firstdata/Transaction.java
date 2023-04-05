package com.paymentgateway.firstdata;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;

@Service("firstDataTransaction")
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
	private String result;
	private String url;
	private String PAReq;
	private String paymentid;
	private String eci;
	private String PaRes;
	private String auth;
	private String avr;
	private String postdate;
	private String tranid;
	private String MD;
	private String TermURL;
	private String error_text;
	private String error_code_tag;
	private String error_service_tag;
	
	private String transid;
	private String mopType;
	private String orderId;
	
	private String acsUrl;
	private String error_message;
	private String approvalCode;
	private String transactionResult;
	private String ipgTransactionId;
	private String transactionTime;
	private String tDateFormat;
	private String processorApprovalCode;
	private String processorResponseCode;
	private String processorResponseMessage;
	private String terminalID;
	private String totalAmt;
	
	

	public void setRefund(Fields fields){
		setSupportTransaction(fields);
	}
	
	public void setStatusEnquiry(Fields fields){
		setSupportTransaction(fields);
	}
		
	public void setCapture(Fields fields){
		setSupportTransaction(fields);
	}
	
	public void setSupportTransaction(Fields fields){
		setMerchantInformation(fields);
		setOrderInformation(fields);
		setAction(fields);
		setPreviousTransactionReference(fields);
	}
	
	public void setPreviousTransactionReference(Fields fields){
		Fields previousFields = fields.getPrevious();
		if(null != previousFields){
			String acqId = previousFields.get(FieldType.ACQ_ID.getName());			
			setTranid(acqId);
			setTransId(acqId);
			
		}
	}
	
	public void setAuthorization(Fields fields){
		String paRes = fields.get(FieldType.PARES.getName());
		if(null == paRes || paRes.isEmpty()){
			setAuthorizationWithoutPares(fields);
		} else {
			setAuthorizationWithPares(fields);
		}
	}
	
	private void setAuthorizationWithPares(Fields fields){
		setPaymentid(fields.get(FieldType.MD.getName()));
		setPaRes(fields.get(FieldType.PARES.getName()));
		setMerchantInformation(fields);
	}
	
	private void setAuthorizationWithoutPares(Fields fields){
		
		//Calling enrollment function because, all mappings are same 
		setEnrollment(fields);
		
		//Setting ECI - According to FirstData, if ECI was not present in enrollment response then set 7
		String eci = fields.get(FieldType.ECI.getName());
		if(null == eci || eci.isEmpty()){
			eci = "7";
		}
		setEci(eci);
	}

	public void setEnrollment(Fields fields) {
		
		setCardDetails(fields);
		setAction(fields);
		setMerchantInformation(fields);
		setOrderInformation(fields);
		//optional fields for rupay card
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
		
		String firstDataTrackId = fields.get(FieldType.ORIG_TXN_ID.getName());
		if(null == firstDataTrackId){
			firstDataTrackId = fields.get(FieldType.TXN_ID.getName());
		}		
		setTrackId(firstDataTrackId);
		
		//Find out proper use of UDF4, and UDF5
		//UDF1 used for payId in case a TID used for multiple merchants
	
		setOrderId(fields.get(FieldType.ORDER_ID.getName()));
	}

	@SuppressWarnings("incomplete-switch")
	private void setAction(Fields fields) {
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if(txnType.equals(TransactionType.ENROLL.getName())){
			txnType = fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
		}
		
		String action = null;				
		switch(TransactionType.getInstance(txnType)){
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

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getAcsUrl() {
		return acsUrl;
	}

	public void setAcsUrl(String acsUrl) {
		this.acsUrl = acsUrl;
	}

	public String getError_message() {
		return error_message;
	}

	public void setError_message(String error_message) {
		this.error_message = error_message;
	}

	public String getApprovalCode() {
		return approvalCode;
	}

	public void setApprovalCode(String approvalCode) {
		this.approvalCode = approvalCode;
	}

	public String getTransactionResult() {
		return transactionResult;
	}

	public void setTransactionResult(String transactionResult) {
		this.transactionResult = transactionResult;
	}

	public String getIpgTransactionId() {
		return ipgTransactionId;
	}

	public void setIpgTransactionId(String ipgTransactionId) {
		this.ipgTransactionId = ipgTransactionId;
	}

	public String getTransactionTime() {
		return transactionTime;
	}

	public void setTransactionTime(String transactionTime) {
		this.transactionTime = transactionTime;
	}

	public String gettDateFormat() {
		return tDateFormat;
	}

	public void settDateFormat(String tDateFormat) {
		this.tDateFormat = tDateFormat;
	}

	public String getProcessorApprovalCode() {
		return processorApprovalCode;
	}

	public void setProcessorApprovalCode(String processorApprovalCode) {
		this.processorApprovalCode = processorApprovalCode;
	}

	public String getProcessorResponseCode() {
		return processorResponseCode;
	}

	public void setProcessorResponseCode(String processorResponseCode) {
		this.processorResponseCode = processorResponseCode;
	}

	public String getTerminalID() {
		return terminalID;
	}

	public void setTerminalID(String terminalID) {
		this.terminalID = terminalID;
	}

	public String getProcessorResponseMessage() {
		return processorResponseMessage;
	}

	public void setProcessorResponseMessage(String processorResponseMessage) {
		this.processorResponseMessage = processorResponseMessage;
	}

	public String getTotalAmt() {
		return totalAmt;
	}

	public void setTotalAmt(String totalAmt) {
		this.totalAmt = totalAmt;
	}
}
