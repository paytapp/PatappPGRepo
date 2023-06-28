package com.paymentgateway.federal;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Rahul
 *
 */
@Service
public class FederalUpiTransformer {
	
	private Transaction transaction = null;

	public FederalUpiTransformer(Transaction transaction) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	
	public void updateResponse(Fields fields) {
		
		String status = null;
		ErrorType errorType = null;
	
		 status = getStatusType();
		 errorType = getResponse();
		
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
		String result = transaction.getResponseCode();
		ErrorType errorType = null;
		
		FederalUpiResultType federalUpiResultType = FederalUpiResultType.getInstance(result);
		switch (federalUpiResultType) {
		case CAPTURED:
			errorType = ErrorType.SUCCESS;
			break;
		case INVALID_XML_FORMAT:
			errorType = ErrorType.INVALID_FIELD;
			break;
		case VALIDATION_ERROR:
			errorType = ErrorType.INVALID_FIELD;
			break;
		case DUPLICATE_REQUEST:
			errorType = ErrorType.DUPLICATE;
			break;
		case INVALID_SENDER_CREDENTIALS:
			errorType = ErrorType.TIMEOUT;
			break;
		case iNVALID_API_TYPE:
			errorType = ErrorType.DECLINED;
			break;
		case API_NOT_ALLOWED_FOR_MERCHANT:
			errorType = ErrorType.DECLINED;
			break;
		case INVALID_SENDER:
			errorType = ErrorType.REJECTED;
			break;
		case MAX_AMOUNT_LIMIT_EXCEED:
			errorType = ErrorType.PROCESSING;
			break;
		case INVALID_HANDLE:
			errorType = ErrorType.SUCCESS;
			break;
		case VALIDATION_ERROR_REQUEST:
			errorType = ErrorType.SUCCESS;
			break;
		case VIRTUAL_ID_EXIST:
			errorType = ErrorType.SUCCESS;
			break;
		case INVALID_OPERATION_TYPE:
			errorType = ErrorType.AUTHENTICATION_UNAVAILABLE;
			break;
		case INVALID_BANK_CODE:
			errorType = ErrorType.TIMEOUT;
			break;
		case INVALID_OPERATION:
			errorType = ErrorType.DECLINED;
			break;
		case INVALID_MERCHANT_CATEGORY_CODE:
			errorType = ErrorType.DECLINED;
			break;
		case INVALID_XML_SIGN:
			errorType = ErrorType.REJECTED;
			break;
		case ACCOUNT_NOT_lINKED:
			errorType = ErrorType.PROCESSING;
			break;
		case INVALID_VIRTUAL_ID:
			errorType = ErrorType.SUCCESS;
			break;	
		case ACCOUNT_NOT_EXIST:
			errorType = ErrorType.PROCESSING;
			break;
		case REJECTED:
			errorType = ErrorType.REJECTED;
			break;
		default:
			errorType = ErrorType.ACQUIRER_ERROR;
			break;
		}
		return errorType;
		
	}
	
	public String getStatusType() {
		String result = transaction.getResponseCode();
		String status = null;
		
		if (result.equals("00")) {
			status = StatusType.CAPTURED.getName();
		} else if (result.equals("F102")) {
			status = StatusType.FAILED.getName();
		} else if (result.equals("F101")) {
			status = StatusType.INVALID.getName();
		} else if (result.equals("F106")) {
			status = StatusType.FAILED.getName();
		} else if (result.equals("999")) {
			status = StatusType.CANCELLED.getName();
		} else if (result.equals("F113")) {
			status = StatusType.CANCELLED.getName();
		} else {
			status = StatusType.REJECTED.getName();
		}

		return status.toString();
	}

}
