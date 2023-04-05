package com.paymentgateway.fss;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
@Service
public class FssTransformer {
	private Transaction transaction = null;
	
	public FssTransformer(Transaction transaction){
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	
	public void updateResponse(Fields fields){
		
		String status = getStatus();
		ErrorType errorType = getResponseCode();
		if(StatusType.REJECTED.getName().equals(status)){
			//This is applicable when we sent invalid request to server
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED.getResponseMessage());
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());			
		} else {
			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());	
		}
		
		fields.put(FieldType.ACS_URL.getName(), transaction.getUrl());
		fields.put(FieldType.PAREQ.getName(), transaction.getPAReq());
		fields.put(FieldType.PAYMENT_ID.getName(), transaction.getPaymentid());
		fields.put(FieldType.ECI.getName(), transaction.getEci());
		fields.put(FieldType.AUTH_CODE.getName(), transaction.getAuth());
		fields.put(FieldType.RRN.getName(), transaction.getRef());
		fields.put(FieldType.AVR.getName(), transaction.getAvr());
		fields.put(FieldType.PG_DATE_TIME.getName(), transaction.getPostdate());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getTranid());	
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getError_code_tag());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getResult());
	}
	
	public ErrorType getResponseCode(){
		String result = transaction.getResult();
		ErrorType errorType = null;
		
		FssResultType fssResulType = FssResultType.getInstance(result);
		switch(fssResulType){
		case APPROVED:
			errorType = ErrorType.SUCCESS;
			break;
		case CAPTURED:
			errorType = ErrorType.SUCCESS;
			break;
		case DENIED_BY_RISK:
			errorType = ErrorType.DENIED;
			break;
		case ENROLLED:
			errorType = ErrorType.SUCCESS;
			break;
		case FSS0001:
			errorType = ErrorType.AUTHENTICATION_UNAVAILABLE;
			break;
		case HOST_TIMEOUT:
			errorType = ErrorType.TIMEOUT;
			break;
		case NOT_APPROVED:
			errorType = ErrorType.DECLINED;
			break;
		case NOT_CAPTURED:
			errorType = ErrorType.DECLINED;
			break;
		case REJECTED:
			errorType = ErrorType.REJECTED;
			break;
		case NOT_ENROLED:
			errorType = ErrorType.PROCESSING;
			break;
		case INITIALIZED:
			errorType = ErrorType.SUCCESS;
			break;	
		default:
			errorType = ErrorType.ACQUIRER_ERROR;
			break;
		}
		
		return errorType;
	}
	
	public String getStatus(){
		String result = transaction.getResult();
		String status = "";
		
		FssResultType fssResulType = FssResultType.getInstance(result);
		switch(fssResulType){
		case APPROVED:
			status = StatusType.APPROVED.getName();
			break;
		case CAPTURED:
			status = StatusType.CAPTURED.getName();
			break;
		case DENIED_BY_RISK:
			status = StatusType.DENIED.getName();
			break;
		case ENROLLED:
			status = StatusType.ENROLLED.getName();
			break;
		case FSS0001:
			status = StatusType.AUTHENTICATION_FAILED.getName();
			break;
		case HOST_TIMEOUT:
			status = StatusType.TIMEOUT.getName();
			break;
		case NOT_APPROVED:
			status = StatusType.DECLINED.getName();
			break;
		case NOT_CAPTURED:
			status = StatusType.DECLINED.getName();
			break;
		case NOT_ENROLED:
			status = StatusType.PENDING.getName();
			break;
		case REJECTED:
			status = StatusType.REJECTED.getName();
			break;
		default:
			status = StatusType.ENROLLED.getName();
			break;
		}
		
		return status;
	}
}
