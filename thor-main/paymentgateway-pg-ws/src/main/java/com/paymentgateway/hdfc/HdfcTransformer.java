package com.paymentgateway.hdfc;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class HdfcTransformer {

	private Transaction transaction = null;

	public HdfcTransformer(Transaction transaction) {
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
		String pgTxnMsg = null;
		if ((StringUtils.isNotBlank(transaction.getResult()))
				&& ((transaction.getResult()).equalsIgnoreCase("CAPTURED"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = "CAPTURED";

		} else {
			if (StringUtils.isNotBlank(transaction.getResult())) {
				HdfcResultType resultInstance = HdfcResultType.getInstanceFromName(transaction.getResult());
				
				if (resultInstance == null) {
				
					if (transaction.getError_code_tag().length() >= 11 ) {
                        String errorCodeTag = transaction.getError_code_tag().substring(0,11);
                        resultInstance = HdfcResultType.getInstanceFromName(errorCodeTag);
                    }
                   
                    if (resultInstance == null) {
                        resultInstance = HdfcResultType.getInstanceFromName(transaction.getError_code_tag());
                    }
					
				}

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
					pgTxnMsg = resultInstance.getMessage();	
				}
				else {
					status = StatusType.DECLINED.getName();
					errorType = ErrorType.getInstanceFromCode("004");
					pgTxnMsg = "Transaction Declined by acquirer";
				}
				
			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.AUTH_CODE.getName(), transaction.getAuth());
		fields.put(FieldType.RRN.getName(), transaction.getRef());
		fields.put(FieldType.AVR.getName(), transaction.getAvr());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getTranId());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getError_code_tag());
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getResult());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.RRN.getName(), transaction.getPayId());
	}
	
	
	public void updateStatusResponse(Fields fields) {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;
		if ((StringUtils.isNotBlank(transaction.getResult()))
				&& ((transaction.getResult()).equalsIgnoreCase("SUCCESS"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		} else {
			if (StringUtils.isNotBlank(transaction.getResult())) {
				HdfcResultType resultInstance = HdfcResultType.getInstanceFromName(transaction.getResult());
				
				if (resultInstance == null) {
				
					if (StringUtils.isNotBlank(transaction.getError_code_tag())) {
						
						if (transaction.getError_code_tag().length() >= 11 ) {
							String errorCodeTag = transaction.getError_code_tag().substring(0,11);
							resultInstance = HdfcResultType.getInstanceFromName(errorCodeTag);
						}
						
					}
					
				}

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
					pgTxnMsg = resultInstance.getMessage();	
				}
				else {
					status = StatusType.DECLINED.getName();
					errorType = ErrorType.getInstanceFromCode("004");
					pgTxnMsg = "Transaction Declined by acquirer";
				}
				
			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.AUTH_CODE.getName(), transaction.getAuth());
		fields.put(FieldType.RRN.getName(), transaction.getRef());
		fields.put(FieldType.AVR.getName(), transaction.getAvr());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getTranId());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getError_code_tag());
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getResult());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.RRN.getName(), transaction.getPayId());
	}
}
