package com.paymentgateway.billDesk;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class BillDeskTransformer {

	private Transaction transaction = null;

	public BillDeskTransformer(Transaction transaction) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public void updateCardTxnResponse(Fields fields) {
		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;
		if (fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.ENROLL.getName())) {
			if ((StringUtils.isNotBlank(transaction.getPostParameters()))
					&& (StringUtils.isNotBlank(transaction.getAcsUrl()))) {
				status = StatusType.ENROLLED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.getInstanceFromCode("007");
				pgTxnMsg = "Rejected by acquirer";
			}
		} else if (fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.SALE.getCode())) {
			if ((StringUtils.isNotBlank(transaction.getResponseCode()))
					&& ((transaction.getResponseCode()).equalsIgnoreCase("0300"))) {
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

			} else if ((StringUtils.isNotBlank(transaction.getResponseCode()))
					&& ((transaction.getResponseCode()).equalsIgnoreCase("LP999"))) {
				status = StatusType.AUTHENTICATION_FAILED.getName();
				errorType = ErrorType.SIGNATURE_MISMATCH;
				pgTxnMsg = ErrorType.SIGNATURE_MISMATCH.getResponseMessage();

			} else {
				if (StringUtils.isNotBlank(transaction.getResponseCode())) {
					BillDeskResultType resultInstance = BillDeskResultType
							.getInstanceFromName(transaction.getResponseCode());

					if (resultInstance != null) {
						status = resultInstance.getStatusCode();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
						pgTxnMsg = resultInstance.getMessage();
					} else {
						status = StatusType.FAILED_AT_ACQUIRER.getName();
						errorType = ErrorType.getInstanceFromCode("022");
						pgTxnMsg = "Transaction failed at acquirer";
					}

				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

				}
			}

		} else {
			if ((StringUtils.isNotBlank(fields.get(FieldType.PG_TXN_STATUS.getName())))
					&& ((fields.get(FieldType.PG_TXN_STATUS.getName()).equalsIgnoreCase("Y")))) {
				if (fields.get(FieldType.PG_RESP_CODE.getName()).equals("0799") || fields.get(FieldType.PG_RESP_CODE.getName()).equals("0699")) {
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;
					pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
				} else {
					status = StatusType.FAILED.getName();
					errorType = ErrorType.FAILED;
					pgTxnMsg = ErrorType.FAILED.getResponseMessage();
				}

			} else {
				if (StringUtils.isNotBlank(fields.get(FieldType.PG_TXN_MESSAGE.getName()))) {
					BillDeskResultType resultInstance = BillDeskResultType
							.getInstanceFromName(fields.get(FieldType.PG_TXN_MESSAGE.getName()));

					if (resultInstance != null) {
						status = resultInstance.getStatusCode();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
						pgTxnMsg = resultInstance.getMessage();
					} else {
						status = StatusType.FAILED_AT_ACQUIRER.getName();
						errorType = ErrorType.getInstanceFromCode("022");
						pgTxnMsg = "Transaction failed at acquirer";
					}

				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

				}
			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
		fields.put(FieldType.BILLDESK_FINAL_REQUEST.getName(), transaction.getPostParameters());
		fields.put(FieldType.ACS_URL.getName(), transaction.getAcsUrl());
	}

	public void updateResponse(Fields fields) {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;
		if (fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.SALE.getCode())) {

			if ((StringUtils.isNotBlank(transaction.getResponseCode()))
					&& ((transaction.getResponseCode()).equalsIgnoreCase("0300"))) {
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

			} else {
				if (StringUtils.isNotBlank(transaction.getResponseCode())) {
					BillDeskResultType resultInstance = BillDeskResultType
							.getInstanceFromName(transaction.getResponseCode());

					if (resultInstance != null) {
						status = resultInstance.getStatusCode();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
						pgTxnMsg = resultInstance.getMessage();
					} else {
						status = StatusType.FAILED_AT_ACQUIRER.getName();
						errorType = ErrorType.getInstanceFromCode("022");
						pgTxnMsg = "Transaction failed at acquirer";
					}

				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

				}
			}
		} else {
			if ((StringUtils.isNotBlank(fields.get(FieldType.PG_TXN_STATUS.getName())))
					&& ((fields.get(FieldType.PG_TXN_STATUS.getName()).equalsIgnoreCase("Y")))) {
				if (fields.get(FieldType.PG_RESP_CODE.getName()).equals("0799") || fields.get(FieldType.PG_RESP_CODE.getName()).equals("0699")) {
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;
					pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
				} else {
					status = StatusType.FAILED.getName();
					errorType = ErrorType.FAILED;
					pgTxnMsg = ErrorType.FAILED.getResponseMessage();
				}

			} else {
				if (StringUtils.isNotBlank(fields.get(FieldType.PG_TXN_MESSAGE.getName()))) {
					BillDeskResultType resultInstance = BillDeskResultType
							.getInstanceFromName(fields.get(FieldType.PG_TXN_MESSAGE.getName()));

					if (resultInstance != null) {
						status = resultInstance.getStatusCode();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
						pgTxnMsg = resultInstance.getMessage();
					} else {
						status = StatusType.FAILED_AT_ACQUIRER.getName();
						errorType = ErrorType.getInstanceFromCode("022");
						pgTxnMsg = "Transaction failed at acquirer";
					}

				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

				}
			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

	}

	public void updateStausResponse(Fields fields) {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if ((StringUtils.isNotBlank(fields.get(FieldType.PG_RESP_CODE.getName())))
				&& ((fields.get(FieldType.PG_RESP_CODE.getName())).equalsIgnoreCase("0300"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		} else {
			if (StringUtils.isNotBlank(fields.get(FieldType.PG_RESP_CODE.getName()))) {
				BillDeskResultType resultInstance = BillDeskResultType
						.getInstanceFromName(fields.get(FieldType.PG_RESP_CODE.getName()));

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
					pgTxnMsg = resultInstance.getMessage();
				} else {
					status = StatusType.FAILED_AT_ACQUIRER.getName();
					errorType = ErrorType.getInstanceFromCode("022");
					pgTxnMsg = "Transaction failed at acquirer";
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

			}
		}

	}
}
