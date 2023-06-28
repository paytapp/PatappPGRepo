package com.paymentgateway.hdfc.upi;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.hdfc.upi.Transaction;

/**
 * @author Rahul
 *
 */
@Service("hdfcUpiTransformer")
public class HdfcUpiTransformer {

	private Transaction transaction = null;

	public HdfcUpiTransformer(Transaction transactionResponse) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public void updateResponse(Fields fields, Transaction transactionResponse) {

		String status = null;
		ErrorType errorType = null;

		status = getStatusType(transactionResponse);
		errorType = getResponse(transactionResponse);

		if (StatusType.FAILED.getName().equals(status)) {
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED.getResponseMessage());
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
		} else {
			String txnType = fields.get(FieldType.TXNTYPE.getName());
			if (txnType.equals(TransactionType.ENQUIRY.getName())) {
				fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.FSS.getCode());
				fields.put(FieldType.PG_REF_NUM.getName(), transactionResponse.getTransactionId());
				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
				fields.put(FieldType.PG_RESP_CODE.getName(), transactionResponse.getResponse());
				fields.put(FieldType.PG_TXN_STATUS.getName(), transactionResponse.getStatus());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), transactionResponse.getResponseMessage());
				fields.put(FieldType.UDF1.getName(), transactionResponse.getMerchantVpa());
				fields.put(FieldType.ACQ_ID.getName(), transactionResponse.getPayeeApprovalNum());
				fields.put(FieldType.RRN.getName(), transactionResponse.getCustomerReference());
				fields.put(FieldType.PG_DATE_TIME.getName(), transactionResponse.getDateTime());
				fields.put(FieldType.AUTH_CODE.getName(), transactionResponse.getReferenceId());
				
			} else {
				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
				fields.put(FieldType.ACQ_ID.getName(), transactionResponse.getAcq_id());
				fields.put(FieldType.RRN.getName(), transactionResponse.getRrn());
				fields.put(FieldType.PG_RESP_CODE.getName(), transactionResponse.getResponse());
				fields.put(FieldType.PG_TXN_STATUS.getName(), transactionResponse.getStatus());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), transactionResponse.getResponseMessage());
				fields.put(FieldType.UDF1.getName(), transactionResponse.getMerchantVpa());
				fields.put(FieldType.PG_DATE_TIME.getName(), transactionResponse.getDateTime());
			}
		}
	}

	public ErrorType getResponse(Transaction transaction) {
		String result = transaction.getStatus();
		ErrorType errorType = null;

		if (result.equals(Constants.SUCCESS_REPONSE)) {
			errorType = ErrorType.SUCCESS;
		} else {
			errorType = ErrorType.REJECTED;
		}
		return errorType;
	}

	public String getStatusType(Transaction transaction) {
		String result = transaction.getStatus();
		String status = "";

		if (result.equals(Constants.SUCCESS_REPONSE)) {
			status = StatusType.CAPTURED.getName();
		} else {
			status = StatusType.FAILED.getName();
		}

		return status.toString();
	}

}
