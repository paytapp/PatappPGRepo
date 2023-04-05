package com.paymentgateway.firstdata;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class FirstDataTransformer {
	private Transaction transaction = null;

	public FirstDataTransformer(Transaction transaction) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public void updateResponse(Fields fields) {
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		String status = null;
		ErrorType errorType = null;
		if(txnType.equals(TransactionType.ENROLL.getName())){
		 status = getStatusType();
		 errorType = getResponse();
		} else
		{
		 status = getSaleStatusType();
		 errorType = getSaleResponse();
		}

		if (StatusType.REJECTED.getName().equals(status)) {
			// This is applicable when we sent invalid request to server
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED.getResponseMessage());
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
		} else {
			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
		}

		fields.put(FieldType.ACS_URL.getName(), transaction.getAcsUrl());
		fields.put(FieldType.PAREQ.getName(), transaction.getPAReq());
		fields.put(FieldType.PG_DATE_TIME.getName(), transaction.gettDateFormat());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getIpgTransactionId());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getError_code_tag());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getError_message());
		fields.put(FieldType.MD.getName(), transaction.getMD());
		fields.put(FieldType.TERM_URL.getName(), transaction.getTermURL());

	}

	public ErrorType getResponse() {
		String result = transaction.getApprovalCode();
		ErrorType errorType = null;

		FirstDataResultType firstDataResultType = FirstDataResultType.getInstance(result);
		switch (firstDataResultType) {
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
		case FIRST_DATA_0001:
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


	public String getStatusType() {
		String result = transaction.getApprovalCode();
		String status = null;

		if (result.equals("?:waiting 3dsecure")) {
			status = StatusType.ENROLLED.getName();
		} else if (result.equals("01")) {
			status = StatusType.CAPTURED.getName();
		} else if (result.equals("03")) {
			status = StatusType.TIMEOUT.getName();
		} else if (result.equals("10")) {
			status = StatusType.SETTLED.getName();
		} else if (result.equals("141")) {
			status = StatusType.CANCELLED.getName();
		} else if (result.equals("142")) {
			status = StatusType.CANCELLED.getName();
		} else {
			status = StatusType.REJECTED.getName();
		}

		return status.toString();
	}
	
	public String getSaleStatusType() {
		String result = transaction.getTransactionResult();
		String status = null;

		if (result.equals("ENROLLED")) {
			status = StatusType.ENROLLED.getName();
		} else if (result.equals("APPROVED")) {
			status = StatusType.CAPTURED.getName();
		} else if (result.equals("TIMEOUT")) {
			status = StatusType.TIMEOUT.getName();
		} else if (result.equals("10")) {
			status = StatusType.SETTLED.getName();
		} else if (result.equals("141")) {
			status = StatusType.CANCELLED.getName();
		} else if (result.equals("FAILED")) {
			status = StatusType.REJECTED.getName();
		} else {
			status = StatusType.REJECTED.getName();
		}

		return status.toString();
	}
	
	public ErrorType getSaleResponse() {
		String result = transaction.getTransactionResult();
		ErrorType errorType = null;

		FirstDataResultType firstDataResultType = FirstDataResultType.getInstance(result);
		switch (firstDataResultType) {
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
		case FIRST_DATA_0001:
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

}
