package com.paymentgateway.globalpay;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class GlobalpayTransformer {

	private Transaction transaction = null;

	public GlobalpayTransformer(Transaction transaction) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public void updateResponse(Fields fields) {
		String status = "";
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if ((StringUtils.isNotBlank(transaction.getPayment_status()))
				&& ((transaction.getPayment_status()).equalsIgnoreCase("Success"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		} else {
			if (StringUtils.isNotBlank(transaction.getPayment_status())) {

				GlobalpayResultType resultInstance = GlobalpayResultType
						.getInstanceFromName(transaction.getPayment_status());
				if ((resultInstance != null)) {
					status = resultInstance.getMessage();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getLetzPayCode());

					if (StringUtils.isNotBlank(transaction.getMessage())) {
						pgTxnMsg = transaction.getMessage();
					} else {
						pgTxnMsg = resultInstance.getMessage();
					}

				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.getInstanceFromCode("007");

					if (StringUtils.isNotBlank(transaction.getMessage())) {
						pgTxnMsg = transaction.getMessage();
					} else {
						pgTxnMsg = "Transaction failed at acquirer";
					}

				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;

				if (StringUtils.isNotBlank(transaction.getMessage())) {
					pgTxnMsg = transaction.getMessage();
				} else {
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				}

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.ACQ_ID.getName(), transaction.getTransaction_id());
		fields.put(FieldType.PG_RESP_CODE.getName(), errorType.getResponseCode());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.RRN.getName(), transaction.getBank_rrn());
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getPayment_status());

	}

	public void updateStatusResponse(Fields fields) {

		String status = "";
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if ((StringUtils.isNotBlank(transaction.getPayment_status()))
				&& ((transaction.getPayment_status()).equalsIgnoreCase("Success"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		} else {
			if (StringUtils.isNotBlank(transaction.getPayment_status())) {

				GlobalpayResultType resultInstance = GlobalpayResultType
						.getInstanceFromName(transaction.getPayment_status());
				if ((resultInstance != null)) {
					if (resultInstance.getBankCode().equalsIgnoreCase("PENDING") || resultInstance.getBankCode().equalsIgnoreCase("Initialized") || 
							resultInstance.getBankCode().equalsIgnoreCase("Not Attempted")) {
						status = StatusType.PROCESSING.getName();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getLetzPayCode());
						pgTxnMsg = resultInstance.getMessage();
					}
					else {
						status = StatusType.FAILED.getName();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getLetzPayCode());
						pgTxnMsg = resultInstance.getMessage();
					}
					
				} else {

					status = StatusType.REJECTED.getName();
					errorType = ErrorType.getInstanceFromCode("022");
					pgTxnMsg = "Transaction failed at acquirer";

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

		fields.put(FieldType.ACQ_ID.getName(), transaction.getTransaction_id());

		if (StringUtils.isNoneBlank(transaction.getStatus())) {
			fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getStatus());
		} else {
			errorType.getResponseCode();
		}

		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getPayment_status());

	}

}
