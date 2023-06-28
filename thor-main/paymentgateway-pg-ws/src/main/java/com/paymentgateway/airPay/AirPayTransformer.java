package com.paymentgateway.airPay;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.EPOSTransactionDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class AirPayTransformer {

	@Autowired
	EPOSTransactionDao eposTransactionDao;

	private Transaction transaction = null;

	public AirPayTransformer(Transaction transaction) {
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

		if ((StringUtils.isNotBlank(transaction.getTRANSACTIONSTATUS()))
				&& (StringUtils.isNotBlank(transaction.getMESSAGE()))
				&& ((transaction.getTRANSACTIONSTATUS()).equals("200"))
				&& ((transaction.getMESSAGE()).equals("Success"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		} else {
			if (StringUtils.isNotBlank(transaction.getTRANSACTIONSTATUS())) {

				AirPayResultType resultInstance = AirPayResultType
						.getInstanceFromName(transaction.getTRANSACTIONSTATUS());
				if ((resultInstance != null)) {
					status = resultInstance.getMessage();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getLetzPayCode());

					if (StringUtils.isNotBlank(transaction.getMESSAGE())) {
						pgTxnMsg = transaction.getMESSAGE();
					} else {
						pgTxnMsg = resultInstance.getMessage();
					}

				} else {
					status = StatusType.FAILED_AT_ACQUIRER.getName();
					errorType = ErrorType.getInstanceFromCode("022");

					if (StringUtils.isNotBlank(transaction.getMESSAGE())) {
						pgTxnMsg = transaction.getMESSAGE();
					} else {
						pgTxnMsg = "Transaction failed at acquirer";
					}

				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;

				if (StringUtils.isNotBlank(transaction.getMESSAGE())) {
					pgTxnMsg = transaction.getMESSAGE();
				} else {
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				}

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.AUTH_CODE.getName(), transaction.getAPTRANSACTIONID());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getAPTRANSACTIONID());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getTRANSACTIONSTATUS());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.RRN.getName(), transaction.getAPTRANSACTIONID());
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getMESSAGE());

	}

	public void updateStatusResponse(Fields fields) {

		String status = "";
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if ((StringUtils.isNotBlank(transaction.getTRANSACTIONSTATUS()))
				&& (StringUtils.isNotBlank(transaction.getMESSAGE()))
				&& ((transaction.getTRANSACTIONSTATUS()).equals("200"))
				&& ((transaction.getMESSAGE()).equals("Success"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		} else {
			if (StringUtils.isNotBlank(transaction.getTRANSACTIONSTATUS())) {

				AirPayResultType resultInstance = AirPayResultType
						.getInstanceFromName(transaction.getTRANSACTIONSTATUS());
				if ((resultInstance != null)) {
					status = resultInstance.getMessage();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getLetzPayCode());
					pgTxnMsg = resultInstance.getMessage();
				} else {

					status = StatusType.FAILED.getName();
					errorType = ErrorType.getInstanceFromCode("022");
					pgTxnMsg = "Transaction failed at acquirer";

					if (StringUtils.isNoneBlank(transaction.getMESSAGE())) {
						pgTxnMsg = transaction.getMESSAGE();
					}
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				if (StringUtils.isNoneBlank(transaction.getMESSAGE())) {
					pgTxnMsg = transaction.getMESSAGE();
				}

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.AUTH_CODE.getName(), transaction.getAPTRANSACTIONID());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getAPTRANSACTIONID());

		if (StringUtils.isNoneBlank(transaction.getTRANSACTIONSTATUS())) {
			fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getTRANSACTIONSTATUS());
		} else {
			errorType.getResponseCode();
		}

		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.RRN.getName(), transaction.getAPTRANSACTIONID());
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getTRANSACTIONSTATUS());

	}

	public void updateRefundResponse(Fields fields) {

		String status = "";
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if ((StringUtils.isNotBlank(transaction.getSuccess()))
				&& ((transaction.getSuccess()).equalsIgnoreCase("true"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		} else {
			if (StringUtils.isNotBlank(transaction.getSuccess())) {

				status = StatusType.FAILED_AT_ACQUIRER.getName();
				errorType = ErrorType.getInstanceFromCode("022");
				pgTxnMsg = "Transaction failed at acquirer";

				if (StringUtils.isNoneBlank(transaction.getMessage())) {
					pgTxnMsg = transaction.getMessage();
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

		fields.put(FieldType.AUTH_CODE.getName(), transaction.getAPTRANSACTIONID());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getAPTRANSACTIONID());

		if (StringUtils.isNoneBlank(transaction.getSuccess())) {
			if ((transaction.getSuccess()).equalsIgnoreCase("true")) {
				fields.put(FieldType.PG_RESP_CODE.getName(), "000");
			} else {
				fields.put(FieldType.PG_RESP_CODE.getName(), "002");
			}
		} else {
			errorType.getResponseCode();
		}

		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.RRN.getName(), transaction.getAPTRANSACTIONID());
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getMESSAGE());
	}
}
