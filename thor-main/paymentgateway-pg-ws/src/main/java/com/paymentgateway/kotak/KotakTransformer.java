package com.paymentgateway.kotak;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class KotakTransformer {

	private Transaction transaction = null;

	public KotakTransformer(Transaction transaction) {
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
		String status = "";
		ErrorType errorType = null;
		String pgTxnMsg = null;
		if (txnType.equals(TransactionType.SALE.getName())) {

			if ((StringUtils.isNotBlank(transaction.getResponseCode()))
					&& ((transaction.getResponseCode()).equals("00"))) {
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

			} else {
				if (StringUtils.isNotBlank(transaction.getResponseCode())) {
					String mopType = fields.get(FieldType.MOP_TYPE.getName());
					if (mopType.equalsIgnoreCase(MopType.RUPAY.getName())) {
						KotakRupayResultType resultInstance = KotakRupayResultType
								.getInstanceFromName(transaction.getResponseCode());
						if ((resultInstance != null)) {
							status = resultInstance.getStatusName();
							errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
							pgTxnMsg = resultInstance.getMessage();
						} else {
							status = StatusType.FAILED_AT_ACQUIRER.getName();
							errorType = ErrorType.getInstanceFromCode("022");
							pgTxnMsg = "Transaction failed at acquirer";
						}
					} else {
						KotakVisaMasterResultType resultInstance = KotakVisaMasterResultType
								.getInstanceFromName(transaction.getResponseCode());
						if ((resultInstance != null)) {
							status = resultInstance.getStatusName();
							errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
							pgTxnMsg = resultInstance.getMessage();
						} else {
							status = StatusType.FAILED_AT_ACQUIRER.getName();
							errorType = ErrorType.getInstanceFromCode("022");
							pgTxnMsg = "Transaction failed at acquirer";
						}
					}

				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

				}
			}
		} else {

			if (transaction.getStatus().equalsIgnoreCase("Success")) {
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
			} else {
				if (StringUtils.isNotBlank(transaction.getResponseCode())) {
					String mopType = fields.get(FieldType.MOP_TYPE.getName());
					if (mopType.equalsIgnoreCase(MopType.RUPAY.getName())) {
						KotakRupayResultType resultInstance = KotakRupayResultType
								.getInstanceFromName(transaction.getStatus());
						if ((resultInstance != null)) {
							status = resultInstance.getStatusName();
							errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
							pgTxnMsg = resultInstance.getMessage();
						} else {
							status = StatusType.FAILED_AT_ACQUIRER.getName();
							errorType = ErrorType.getInstanceFromCode("022");
							pgTxnMsg = "Transaction failed at acquirer";
						}
					} else {
						KotakVisaMasterResultType resultInstance = KotakVisaMasterResultType
								.getInstanceFromName(transaction.getStatus());
						if ((resultInstance != null)) {
							status = resultInstance.getStatusName();
							errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
							pgTxnMsg = resultInstance.getMessage();
						} else {
							status = StatusType.FAILED_AT_ACQUIRER.getName();
							errorType = ErrorType.getInstanceFromCode("022");
							pgTxnMsg = "Transaction failed at acquirer";
						}
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

		fields.put(FieldType.AUTH_CODE.getName(), transaction.getAuthCode());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getAcqId());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.RRN.getName(), transaction.getAcqId());
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getStatus());

	}
}
