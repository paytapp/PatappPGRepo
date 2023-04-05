package com.paymentgateway.cashfree;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class CashfreeTransformer {

	private Transaction transaction = null;

	public CashfreeTransformer(Transaction transaction) {
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

		if ((StringUtils.isNotBlank(transaction.getTxStatus())) && ((transaction.getTxStatus()).equals("SUCCESS"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		} else {
			if (StringUtils.isNotBlank(transaction.getTxStatus())) {

				CashfreeResultType resultInstance = CashfreeResultType.getInstanceFromName(transaction.getTxStatus());
				if ((resultInstance != null)) {
					status = resultInstance.getStatusName();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());

					if (StringUtils.isNotBlank(transaction.getTxMsg())) {
						pgTxnMsg = transaction.getTxMsg();
					} else {
						pgTxnMsg = resultInstance.getMessage();
					}

				} else {
					status = StatusType.FAILED_AT_ACQUIRER.getName();
					errorType = ErrorType.getInstanceFromCode("022");

					if (StringUtils.isNotBlank(transaction.getTxMsg())) {
						pgTxnMsg = transaction.getTxMsg();
					} else {
						pgTxnMsg = "Transaction failed at acquirer";
					}

				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;

				if (StringUtils.isNotBlank(transaction.getTxMsg())) {
					pgTxnMsg = transaction.getTxMsg();
				} else {
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				}

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.AUTH_CODE.getName(), transaction.getReferenceId());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getReferenceId());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getTxStatus());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.RRN.getName(), transaction.getReferenceId());
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getTxStatus());

	}

	public void updateStatusResponse(Fields fields) {

		String status = "";
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if (((StringUtils.isNotBlank(transaction.getOrderStatus()))
				&& ((transaction.getOrderStatus()).equalsIgnoreCase("PAID")))
				&& ((StringUtils.isNotBlank(transaction.getStatus()))
						&& ((transaction.getStatus()).equalsIgnoreCase("OK")))
				&& ((StringUtils.isNotBlank(transaction.getTxStatus()))
						&& ((transaction.getTxStatus()).equalsIgnoreCase("SUCCESS")))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		} else {
			if (StringUtils.isNotBlank(transaction.getOrderStatus())) {

				CashfreeResultType resultInstance = CashfreeResultType
						.getInstanceFromName(transaction.getOrderStatus());
				if ((resultInstance != null)) {
					status = resultInstance.getStatusName();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());
					pgTxnMsg = resultInstance.getMessage();
				} else {

					status = StatusType.FAILED_AT_ACQUIRER.getName();
					errorType = ErrorType.getInstanceFromCode("022");
					pgTxnMsg = "Transaction failed at acquirer";

					if (StringUtils.isNoneBlank(transaction.getTxMsg())) {
						pgTxnMsg = transaction.getTxMsg();
					}
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				if (StringUtils.isNoneBlank(transaction.getTxMsg())) {
					pgTxnMsg = transaction.getTxMsg();
				}

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.AUTH_CODE.getName(), transaction.getReferenceId());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getReferenceId());

		if (StringUtils.isNoneBlank(transaction.getTxStatus())) {
			fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getTxStatus());
		} else {
			errorType.getResponseCode();
		}

		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.RRN.getName(), transaction.getReferenceId());
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getStatus());

	}

	public void updateRefundResponse(Fields fields) {

		String status = "";
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if ((StringUtils.isNotBlank(transaction.getStatus()))
						&& ((transaction.getStatus()).equalsIgnoreCase("OK"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		} else {
			if (StringUtils.isNotBlank(transaction.getStatus())) {

				CashfreeResultType resultInstance = CashfreeResultType
						.getInstanceFromName(transaction.getStatus());
				if ((resultInstance != null)) {
					status = resultInstance.getStatusName();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());
					pgTxnMsg = resultInstance.getMessage();
				} else {

					status = StatusType.FAILED_AT_ACQUIRER.getName();
					errorType = ErrorType.getInstanceFromCode("022");
					pgTxnMsg = "Transaction failed at acquirer";

					if (StringUtils.isNoneBlank(transaction.getMessage())) {
						pgTxnMsg = transaction.getMessage();
					}
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				if (StringUtils.isNoneBlank(transaction.getMessage())) {
					pgTxnMsg = transaction.getMessage();
				}

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.AUTH_CODE.getName(), transaction.getRefundId());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getRefundId());

		if (StringUtils.isNoneBlank(transaction.getStatus())) {
			fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getStatus());
		} else {
			errorType.getResponseCode();
		}

		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.RRN.getName(), transaction.getRefundId());
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getStatus());
	}

}
