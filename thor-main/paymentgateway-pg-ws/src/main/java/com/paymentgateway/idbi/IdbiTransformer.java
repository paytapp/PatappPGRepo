package com.paymentgateway.idbi;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class IdbiTransformer {

	private static Logger logger = LoggerFactory.getLogger(IdbiTransformer.class.getName());

	private Transaction transaction = null;

	public IdbiTransformer(Transaction transaction) {
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
		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;
		if (txnType.equals(TransactionType.REFUND.getName())) {
			if ((StringUtils.isNotBlank(transaction.getStatusCode()))
					&& ((transaction.getStatusCode()).equalsIgnoreCase("S"))) {
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
			} else {
				status = StatusType.FAILED_AT_ACQUIRER.getName();
				errorType = ErrorType.getInstanceFromCode("022");
				pgTxnMsg = "Transaction failed at acquirer";
			}
		} else {
			String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
			if (paymentType.equals(PaymentType.NET_BANKING.getCode())) {
				String bankStatus = fields.get(FieldType.PG_TXN_STATUS.getName());
				if ((StringUtils.isNotBlank(bankStatus)) && (bankStatus.equalsIgnoreCase("S"))) {
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;
					pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

				} else {
					if (StringUtils.isNotBlank(bankStatus)) {
						IDBIResultType resultInstance = IDBIResultType.getInstanceFromName(bankStatus);
						if ((resultInstance != null)) {
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
				String bankCode = fields.get(FieldType.PG_RESP_CODE.getName());
				if ((StringUtils.isNotBlank(bankCode)) && (bankCode.equalsIgnoreCase("00"))) {
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;
					pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

				} else {
					if (StringUtils.isNotBlank(bankCode)) {
						IDBIResultType resultInstance = IDBIResultType.getInstanceFromName(bankCode);
						if ((resultInstance != null)) {
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

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		if (!txnType.equals(TransactionType.SALE.getName())) {
			fields.put(FieldType.ACQ_ID.getName(), transaction.getAcqId());
			fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

		}

	}

}
