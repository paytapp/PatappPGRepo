package com.paymentgateway.crm.action;

import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.action.AbstractSecureAction;

public class StatusEnquiryParameters extends AbstractSecureAction {

	/**
	 * 
	 */
	@Autowired
	private CrmValidator validator;
	
	private static final long serialVersionUID = -6981816834351382941L;

	private String txnId;
	private String payId;
	private String origTxnId;
	private String amount;
	private String authCode;
	private String rrn;
	private String acqId;
	private String status;
	private String responseCode;
	private String responseMessage;
	private String pgTxnMessage;
	private String pgRespCode;
	private String pgRefNum;
	private String rfu1;
	private String rfu2;
	private String acquirerType;
	private String orderId;
	private String txnType;
	private String pgDateTime;
	private String currencyCode;
	

	public StatusEnquiryParameters(String txnId, String payId, String origTxnId, String amount, String authCode,
			String rrn, String acqId, String status, String responseCode, String responseMessage, String pgTxnMessage,
			String pgRespCode, String pgRefNum, String rfu1, String rfu2, String acquirerType, String orderId,
			String txnType, String pgDateTime,String currencyCode) {

		this.txnId = txnId;
		this.payId = payId;
		this.origTxnId = origTxnId;
		this.amount = amount;
		this.authCode = authCode;
		this.rrn = rrn;
		this.acqId = acqId;
		this.status = status;
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.pgTxnMessage = pgTxnMessage;
		this.pgRespCode = pgRespCode;
		this.pgRefNum = pgRefNum;
		this.rfu1 = rfu1;
		this.rfu2 = rfu2;
		this.acquirerType = acquirerType;
		this.orderId = orderId;
		this.txnType = txnType;
		this.pgDateTime = pgDateTime;
		this.currencyCode=currencyCode;

	}
	public void validate() {
	if ((validator.validateBlankField(getTxnId()))) {
		addFieldError(CrmFieldType.TXN_ID.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.TXN_ID,getTxnId()))) {
		addFieldError(CrmFieldType.TXN_ID.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getPayId()))) {
		addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.PAY_ID,getPayId()))) {
		addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getOrigTxnId()))) {
		addFieldError(CrmFieldType.TRANSACTION_ID.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.TRANSACTION_ID,getOrigTxnId()))) {
		addFieldError(CrmFieldType.TRANSACTION_ID.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getAmount()))) {
		addFieldError(CrmFieldType.AMOUNT.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.AMOUNT,getAmount()))) {
		addFieldError(CrmFieldType.AMOUNT.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getAuthCode()))) {
		addFieldError(CrmFieldType.AUTH_CODE.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.AUTH_CODE,getAuthCode()))) {
		addFieldError(CrmFieldType.AUTH_CODE.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getRrn()))) {
		addFieldError(CrmFieldType.RRN.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.RRN,getRrn()))) {
		addFieldError(CrmFieldType.RRN.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getAcqId()))) {
		addFieldError(CrmFieldType.ACQ_ID.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.ACQ_ID,getAcqId()))) {
		addFieldError(CrmFieldType.ACQ_ID.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getStatus()))) {
		addFieldError(CrmFieldType.STATUS.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.STATUS,getStatus()))) {
		addFieldError(CrmFieldType.STATUS.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getResponseCode()))) {
		addFieldError(CrmFieldType.RESPONSE_CODE.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.RESPONSE_CODE,getResponseCode()))) {
		addFieldError(CrmFieldType.RESPONSE_CODE.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getResponseMessage()))) {
		addFieldError(CrmFieldType.RESPONSE_MESSAGE.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.RESPONSE_MESSAGE,getResponseMessage()))) {
		addFieldError(CrmFieldType.RESPONSE_MESSAGE.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getPgTxnMessage()))) {
		addFieldError(CrmFieldType.PG_TXN_MESSAGE.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.PG_TXN_MESSAGE,getPgTxnMessage()))) {
		addFieldError(CrmFieldType.PG_TXN_MESSAGE.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getPgRespCode()))) {
		addFieldError(CrmFieldType.PG_RESP_CODE.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.PG_RESP_CODE,getPgRespCode()))) {
		addFieldError(CrmFieldType.PG_RESP_CODE.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getPgRefNum()))) {
		addFieldError(CrmFieldType.PG_REF_NUM.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.PG_REF_NUM,getPgRefNum()))) {
		addFieldError(CrmFieldType.PG_REF_NUM.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getRfu1()))) {
		addFieldError(CrmFieldType.RFU1.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.RFU1,getRfu1()))) {
		addFieldError(CrmFieldType.RFU1.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getRfu2()))) {
		addFieldError(CrmFieldType.RFU2.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.RFU2,getRfu2()))) {
		addFieldError(CrmFieldType.RFU2.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getAcquirerType()))) {
		addFieldError(CrmFieldType.ACQUIRER.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.ACQUIRER,getAcquirerType()))) {
		addFieldError(CrmFieldType.ACQUIRER.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getOrderId()))) {
		addFieldError(CrmFieldType.ORDER_ID.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.ORDER_ID,getOrderId()))) {
		addFieldError(CrmFieldType.ORDER_ID.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getTxnType()))) {
		addFieldError(CrmFieldType.TXNTYPE.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.TXNTYPE,getTxnType()))) {
		addFieldError(CrmFieldType.TXNTYPE.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getPgDateTime()))) {
		addFieldError(CrmFieldType.PG_DATE_TIME.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.PG_DATE_TIME,getPgDateTime()))) {
		addFieldError(CrmFieldType.PG_DATE_TIME.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getCurrencyCode()))) {
		addFieldError(CrmFieldType.CURRENCY.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.CURRENCY,getCurrencyCode()))) {
		addFieldError(CrmFieldType.CURRENCY.getName(), validator.getResonseObject().getResponseMessage());
	}
	
	}

	public String getAcquirerType() {
		return acquirerType;
	}

	public void setAcquirerType(String acquirerType) {
		this.acquirerType = acquirerType;
	}

	

	
	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getOrigTxnId() {
		return origTxnId;
	}

	public String getPgDateTime() {
		return pgDateTime;
	}

	public void setPgDateTime(String pgDateTime) {
		this.pgDateTime = pgDateTime;
	}

	public void setOrigTxnId(String origTxnId) {
		this.origTxnId = origTxnId;
	}

	public String getAmount() {
		return amount;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

	public String getAcqId() {
		return acqId;
	}

	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getPgTxnMessage() {
		return pgTxnMessage;
	}

	public void setPgTxnMessage(String pgTxnMessage) {
		this.pgTxnMessage = pgTxnMessage;
	}

	public String getPgRespCode() {
		return pgRespCode;
	}

	public void setPgRespCode(String pgRespCode) {
		this.pgRespCode = pgRespCode;
	}

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String getRfu1() {
		return rfu1;
	}

	public void setRfu1(String rfu1) {
		this.rfu1 = rfu1;
	}

	public String getRfu2() {
		return rfu2;
	}

	public void setRfu2(String rfu2) {
		this.rfu2 = rfu2;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

}
