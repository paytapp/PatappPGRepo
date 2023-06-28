package com.paymentgateway.grezpay;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class GrezpayTransformer {

	private Transaction transaction = null;

	public GrezpayTransformer(Transaction transaction) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public void updateResponse(Fields fields) {
		/*
		 * String status = ""; ErrorType errorType = null; String pgTxnMsg = null;
		 * 
		 * if ((StringUtils.isNotBlank(transaction.getStatus())) &&
		 * ((transaction.getStatus()).equalsIgnoreCase("SUCCESS"))) { status =
		 * StatusType.CAPTURED.getName(); errorType = ErrorType.SUCCESS; pgTxnMsg =
		 * ErrorType.SUCCESS.getResponseMessage();
		 * 
		 * } else { if (StringUtils.isNotBlank(transaction.getStatus())) {
		 * 
		 * GrezpayResultType resultInstance = GrezpayResultType
		 * .getInstanceFromName(transaction.getStatus()); if ((resultInstance != null))
		 * { status = resultInstance.getMessage(); errorType =
		 * ErrorType.getInstanceFromCode(resultInstance.getLetzPayCode());
		 * 
		 * if (StringUtils.isNotBlank(transaction.getNotes())) { pgTxnMsg =
		 * transaction.getNotes(); } else { pgTxnMsg = resultInstance.getMessage(); }
		 * 
		 * } else { status = StatusType.FAILED_AT_ACQUIRER.getName(); errorType =
		 * ErrorType.getInstanceFromCode("022");
		 * 
		 * if (StringUtils.isNotBlank(transaction.getNotes())) { pgTxnMsg =
		 * transaction.getNotes(); } else { pgTxnMsg = "Transaction failed at acquirer";
		 * }
		 * 
		 * }
		 * 
		 * } else { status = StatusType.REJECTED.getName(); errorType =
		 * ErrorType.REJECTED;
		 * 
		 * if (StringUtils.isNotBlank(transaction.getNotes())) { pgTxnMsg =
		 * transaction.getNotes(); } else { pgTxnMsg =
		 * ErrorType.REJECTED.getResponseMessage(); }
		 * 
		 * } }
		 * 
		 * fields.put(FieldType.STATUS.getName(), status);
		 * fields.put(FieldType.RESPONSE_MESSAGE.getName(),
		 * errorType.getResponseMessage());
		 * fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
		 * 
		 * fields.put(FieldType.ACQ_ID.getName(), transaction.getTransactionId());
		 * fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getTransactionId());
		 * fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		 * fields.put(FieldType.RRN.getName(), transaction.getTransactionId());
		 * fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getNotes());
		 */
	}

	public void updateStatusResponse(Fields fields) {

		/*
		 * String status = ""; ErrorType errorType = null; String pgTxnMsg = null;
		 * 
		 * if ((StringUtils.isNotBlank(transaction.getStatus())) &&
		 * ((transaction.getStatus()).equalsIgnoreCase("SUCCESS"))) { status =
		 * StatusType.CAPTURED.getName(); errorType = ErrorType.SUCCESS; pgTxnMsg =
		 * ErrorType.SUCCESS.getResponseMessage();
		 * 
		 * } else { if (StringUtils.isNotBlank(transaction.getStatus())) {
		 * 
		 * GrezpayResultType resultInstance = GrezpayResultType
		 * .getInstanceFromName(transaction.getStatus()); if ((resultInstance != null))
		 * { if (resultInstance.getMessage().equalsIgnoreCase("PENDING")) { status =
		 * StatusType.PROCESSING.getName(); errorType =
		 * ErrorType.getInstanceFromCode(resultInstance.getLetzPayCode()); pgTxnMsg =
		 * resultInstance.getMessage(); } else { status = StatusType.FAILED.getName();
		 * errorType = ErrorType.getInstanceFromCode(resultInstance.getLetzPayCode());
		 * pgTxnMsg = resultInstance.getMessage(); }
		 * 
		 * } else {
		 * 
		 * status = StatusType.FAILED.getName(); errorType =
		 * ErrorType.getInstanceFromCode("022"); pgTxnMsg =
		 * "Transaction failed at acquirer";
		 * 
		 * }
		 * 
		 * } else { status = StatusType.REJECTED.getName(); errorType =
		 * ErrorType.REJECTED; pgTxnMsg = ErrorType.REJECTED.getResponseMessage(); } }
		 * 
		 * fields.put(FieldType.STATUS.getName(), status);
		 * fields.put(FieldType.RESPONSE_MESSAGE.getName(),
		 * errorType.getResponseMessage());
		 * fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
		 * 
		 * fields.put(FieldType.ACQ_ID.getName(), transaction.getTransactionId());
		 * 
		 * if (StringUtils.isNoneBlank(transaction.getStatus())) {
		 * fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getStatus()); } else
		 * { errorType.getResponseCode(); }
		 * 
		 * fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		 * fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getStatus());
		 */

	}

}
