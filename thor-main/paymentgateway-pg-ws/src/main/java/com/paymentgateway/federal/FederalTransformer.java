package com.paymentgateway.federal;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Rahul
 *
 */
@Service
public class FederalTransformer {
	
	private Transaction transaction = null;

	public FederalTransformer(Transaction transaction) {
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
				
		fields.put(FieldType.ACQ_ID.getName(), transaction.getTransaction_id());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RRN.getName(), transaction.getApproval_code());
		

	}

	public ErrorType getResponse() {
		String result = transaction.getStatus();
		ErrorType errorType = null;

		FederalResultType federalResultType = FederalResultType.getInstance(result);
		switch (federalResultType) {
		case APPROVED:
			errorType = ErrorType.SUCCESS;
			break;
		case CAPTURED:
			errorType = ErrorType.SUCCESS;
			break;
		case ENROLLED:
			errorType = ErrorType.SUCCESS;
			break;
		case FEDERAL0001:
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
		String result = transaction.getPg_error_code();
		String status = null;

		if (result.equals("000")) {
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
		String result = transaction.getPg_error_code();
		String status = null;

		if (result.equals("0")) {
			status = StatusType.CAPTURED.getName();
		} else if (result.equals("10024")) {
			status = StatusType.INVALID.getName();
		} else {
			status = StatusType.REJECTED.getName();
		}

		return status.toString();
	}
	
	public ErrorType getSaleResponse() {
		String result = transaction.getPg_error_code();
		ErrorType errorType = null;
		
		if (result.equals("0")) {
			errorType = ErrorType.SUCCESS;
		} else if (result.equals("10024")) {
			errorType = ErrorType.INVALID_FIELD;
		} else {
			errorType = ErrorType.REJECTED;
		}

		return errorType;
	}


}
