package com.paymentgateway.isgpay;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Rahul
 * 
 *
 */

@Service
public class ISGPayTransformer {

	private Transaction transaction = null;

	public ISGPayTransformer(Transaction transaction) {
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

		if (fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUND.getName())) {
			if ((StringUtils.isNotBlank(transaction.getResponseCode()))
					&& ((transaction.getResponseCode()).equalsIgnoreCase(ISGPayResultType.ISGPAY062.getBankCode())))

			{
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

			}

			else {
				if ((StringUtils.isNotBlank(transaction.getResponseCode()))) {

					ISGPayResultType resultInstance = ISGPayResultType
							.getInstanceFromName(transaction.getResponseCode());

					// Get processor response using parameter ccAuthReply_processorResponse if
					// response code is not mapped
					if (resultInstance == null) {
						resultInstance = ISGPayResultType
								.getInstanceFromName(transaction.getCcAuthReply_processorResponse());
					}

					if (resultInstance != null) {
						status = resultInstance.getStatusCode();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());

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
							pgTxnMsg = "Transaction rejected";
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
		} else {
			if ((StringUtils.isNotBlank(transaction.getResponseCode()))
					&& ((transaction.getResponseCode()).equalsIgnoreCase(ISGPayResultType.ISGPAY001.getBankCode())))

			{
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

			}

			else {
				if ((StringUtils.isNotBlank(transaction.getResponseCode()))) {

					ISGPayResultType resultInstance = ISGPayResultType
							.getInstanceFromName(transaction.getResponseCode());

					// Get processor response using parameter ccAuthReply_processorResponse if
					// response code is not mapped
					if (resultInstance == null) {
						resultInstance = ISGPayResultType
								.getInstanceFromName(transaction.getCcAuthReply_processorResponse());
					}

					if (resultInstance != null) {
						status = resultInstance.getStatusCode();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());

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
							pgTxnMsg = "Transaction rejected";
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
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		if (StringUtils.isNotBlank(transaction.getAuthCode())) {
			fields.put(FieldType.AUTH_CODE.getName(), transaction.getAuthCode());
		}

		else {
			fields.put(FieldType.AUTH_CODE.getName(), "0");
		}

		// Added by shaiwal for different fields in VISA , MasterCard and RUPAY
		if (fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.MASTERCARD.getCode())) {

			if (StringUtils.isNotBlank(transaction.getRetRefNo())) {
				fields.put(FieldType.ACQ_ID.getName(), transaction.getTxnId());
			}

			if (StringUtils.isNotBlank(transaction.getPgTxnId())) {
				fields.put(FieldType.RRN.getName(), transaction.getRetRefNo());
			}
		}

		else if (fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.VISA.getCode())) {

			if (StringUtils.isNotBlank(transaction.getRetRefNo())) {
				fields.put(FieldType.ACQ_ID.getName(), transaction.getRetRefNo());
			}

			if (StringUtils.isNotBlank(transaction.getPgTxnId())) {
				fields.put(FieldType.RRN.getName(), transaction.getPgTxnId());
			}
		}

		else {
			if (StringUtils.isNotBlank(transaction.getRetRefNo())) {
				fields.put(FieldType.ACQ_ID.getName(), transaction.getRetRefNo());
			}

			if (StringUtils.isNotBlank(transaction.getPgTxnId())) {
				fields.put(FieldType.RRN.getName(), transaction.getPgTxnId());
			}
		}

		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getStatus());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

	}

	public void updateVpaValidationResponse(Fields fields) {
		if ((StringUtils.isNotBlank(transaction.getResponseCode())
				&& ((transaction.getResponseCode()).equalsIgnoreCase(ISGPayResultType.ISGPAY001.getBankCode())))
				&& ((StringUtils.isNotBlank(transaction.getMessage())) && (transaction.getMessage())
						.equalsIgnoreCase("Transaction Collect request initiated successfully"))) {
			fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		} else {
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_VPA.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_VPA.getResponseMessage());
			fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());
			fields.put(FieldType.PG_RESPONSE_MSG.getName(), transaction.getMessage());
		}

	}

}
