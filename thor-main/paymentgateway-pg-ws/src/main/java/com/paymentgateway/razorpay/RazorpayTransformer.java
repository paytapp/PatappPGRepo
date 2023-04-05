package com.paymentgateway.razorpay;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
final class RazorpayTransformer {

	public void updateResponse(Fields fields, Transaction transaction) {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if ((StringUtils.isNotBlank(transaction.getRazorpay_payment_id()))
				&& (StringUtils.isNotBlank(transaction.getRazorpay_order_id()))
				&& fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())) {

			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		}

		else {
			if ((StringUtils.isNotBlank(transaction.getError_code()))) {

				String respCode = null;
				if (StringUtils.isNotBlank(transaction.getError_code())) {
					respCode = transaction.getError_code();
				}

				RazorpayResultType resultInstance = RazorpayResultType.getInstanceFromCode(respCode.toUpperCase());

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());

					if (StringUtils.isNotBlank(transaction.getError_description())) {
						pgTxnMsg = transaction.getError_description();
					}

					else {
						pgTxnMsg = resultInstance.getMessage();
					}

					pgTxnMsg = resultInstance.getMessage();

				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;

					if (StringUtils.isNotBlank(transaction.getError_description())) {
						pgTxnMsg = transaction.getError_description();
					}

					else {
						pgTxnMsg = ErrorType.REJECTED.toString();
					}

				}

			} else {
				status = StatusType.FAILED_AT_ACQUIRER.getName();
				errorType = ErrorType.FAILED;

				if (StringUtils.isNotBlank(transaction.getError_description())) {
					pgTxnMsg = transaction.getError_description();
				} else {
					pgTxnMsg = ErrorType.FAILED.toString();
				}

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.ACQ_ID.getName(), transaction.getRazorpay_payment_id());
		fields.put(FieldType.RRN.getName(), transaction.getRazorpay_order_id());
		fields.put(FieldType.PG_RESP_CODE.getName(), errorType.getResponseCode());
		fields.put(FieldType.PG_TXN_STATUS.getName(), errorType.getResponseCode());

		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

	}

	public void updateRefundResponse(Fields fields, Transaction transaction) {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if (fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getCode())) {

			if ((StringUtils.isNotBlank(transaction.getStatus()))
					&& ((transaction.getStatus()).equalsIgnoreCase(Constants.REFUND_SUCCESS_CODE)))

			{
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

			}
			
			else if ((StringUtils.isNotBlank(transaction.getStatus()))
					&& ((transaction.getStatus()).equalsIgnoreCase(Constants.REFUND_PENDING_CODE)))

			{
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

			}

			else {

				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;

				if (StringUtils.isNotBlank(transaction.getDescription())) {
					pgTxnMsg = transaction.getDescription();
				} else {
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				}

			}

			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

			if (StringUtils.isNotBlank(transaction.getOrder_id())) {
				fields.put(FieldType.ACQ_ID.getName(), transaction.getOrder_id());
			}

			if (StringUtils.isNotBlank(transaction.getPayment_id())) {
				fields.put(FieldType.RRN.getName(), transaction.getPayment_id());
			}

			if (StringUtils.isNotBlank(transaction.getStatus())) {
				fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getStatus());
				fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getStatus());
			} else {
				fields.put(FieldType.PG_TXN_STATUS.getName(), errorType.getResponseCode());
				fields.put(FieldType.PG_RESP_CODE.getName(), errorType.getResponseCode());
			}

			fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

		}

	}

}
