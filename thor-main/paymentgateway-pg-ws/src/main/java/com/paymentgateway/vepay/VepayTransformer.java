package com.paymentgateway.vepay;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class VepayTransformer {

	private static Logger logger = LoggerFactory.getLogger(VepayTransformer.class.getName());

	private Transaction transaction = null;

	public VepayTransformer(Transaction transaction) {
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
		 * String status = null; ErrorType errorType = null; String pgTxnMsg = null;
		 * 
		 * if ((StringUtils.isNotBlank(transaction.getRes_code())) &&
		 * (StringUtils.isNotBlank(transaction.getStatus())) &&
		 * ((transaction.getRes_code()).equalsIgnoreCase(Constants.SUCCESS_CODE)) &&
		 * ((transaction.getStatus()).equalsIgnoreCase(Constants.SUCCESS_MSG)) &&
		 * fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE
		 * .getName())) { status = StatusType.CAPTURED.getName(); errorType =
		 * ErrorType.SUCCESS;
		 * 
		 * if (StringUtils.isNotBlank(transaction.getRes_message())) { pgTxnMsg =
		 * transaction.getRes_message(); }
		 * 
		 * else { pgTxnMsg = ErrorType.SUCCESS.getResponseMessage(); }
		 * 
		 * }
		 * 
		 * else { if ((StringUtils.isNotBlank(transaction.getRes_code()))) {
		 * 
		 * String respCode = null; if
		 * (StringUtils.isNotBlank(transaction.getRes_code())) { respCode =
		 * transaction.getRes_code(); }
		 * 
		 * VepayResultType resultInstance =
		 * VepayResultType.getInstanceFromName(respCode);
		 * 
		 * if (resultInstance != null) { status = resultInstance.getStatusCode();
		 * errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());
		 * 
		 * if (StringUtils.isNotBlank(transaction.getRes_message())) { pgTxnMsg =
		 * transaction.getRes_message(); }
		 * 
		 * else { pgTxnMsg = resultInstance.getMessage(); }
		 * 
		 * } else { status = StatusType.FAILED_AT_ACQUIRER.getName(); errorType =
		 * ErrorType.FAILED;
		 * 
		 * if (StringUtils.isNotBlank(transaction.getRes_message())) { pgTxnMsg =
		 * transaction.getRes_message(); }
		 * 
		 * else { pgTxnMsg = ErrorType.FAILED.toString(); }
		 * 
		 * }
		 * 
		 * } else { status = StatusType.FAILED_AT_ACQUIRER.getName(); errorType =
		 * ErrorType.FAILED; if (StringUtils.isNotBlank(transaction.getRes_message())) {
		 * pgTxnMsg = transaction.getRes_message(); } else { pgTxnMsg =
		 * ErrorType.FAILED.toString(); }
		 * 
		 * } }
		 * 
		 * fields.put(FieldType.STATUS.getName(), status);
		 * fields.put(FieldType.RESPONSE_MESSAGE.getName(),
		 * errorType.getResponseMessage());
		 * fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
		 * 
		 * fields.put(FieldType.ACQ_ID.getName(), transaction.getAg_ref());
		 * fields.put(FieldType.RRN.getName(), transaction.getPg_ref());
		 * fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getRes_code());
		 * 
		 * if (StringUtils.isNotBlank(transaction.getStatus())) {
		 * fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getStatus()); }
		 * else { fields.put(FieldType.PG_TXN_STATUS.getName(),
		 * errorType.getResponseCode()); }
		 * 
		 * fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		 */

	}

	public void updateRefundResponse(Fields fields) {

		/*
		 * String status = null; ErrorType errorType = null; String pgTxnMsg = null;
		 * 
		 * if (fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equalsIgnoreCase(
		 * TransactionType.SALE.getCode())) {
		 * 
		 * if ((StringUtils.isNotBlank(transaction.getRes_code())) &&
		 * ((transaction.getRes_code()).equalsIgnoreCase(Constants.
		 * STATUS_ENQ_SUCCESS_CODE)))
		 * 
		 * { status = StatusType.CAPTURED.getName(); errorType = ErrorType.SUCCESS;
		 * 
		 * if (StringUtils.isNotBlank(transaction.getRes_message())) { pgTxnMsg =
		 * transaction.getRes_message(); }
		 * 
		 * else { pgTxnMsg = ErrorType.SUCCESS.getResponseMessage(); }
		 * 
		 * }
		 * 
		 * else { if ((StringUtils.isNotBlank(transaction.getRes_code()))) {
		 * 
		 * String respCode = null; if
		 * (StringUtils.isNotBlank(transaction.getRes_code())) { respCode =
		 * transaction.getRes_code(); }
		 * 
		 * VepayResultType resultInstance =
		 * VepayResultType.getInstanceFromName(respCode);
		 * 
		 * if (resultInstance != null) { status = resultInstance.getStatusCode();
		 * errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());
		 * 
		 * if (StringUtils.isNotBlank(transaction.getRes_message())) { pgTxnMsg =
		 * transaction.getRes_message(); }
		 * 
		 * else { pgTxnMsg = resultInstance.getMessage(); }
		 * 
		 * } else { status = StatusType.FAILED_AT_ACQUIRER.getName(); errorType =
		 * ErrorType.FAILED;
		 * 
		 * if (StringUtils.isNotBlank(transaction.getRes_message())) { pgTxnMsg =
		 * transaction.getRes_message(); }
		 * 
		 * else { pgTxnMsg = ErrorType.FAILED.toString(); }
		 * 
		 * }
		 * 
		 * } else { status = StatusType.FAILED_AT_ACQUIRER.getName(); errorType =
		 * ErrorType.FAILED; if (StringUtils.isNotBlank(transaction.getRes_message())) {
		 * pgTxnMsg = transaction.getRes_message(); } else { pgTxnMsg =
		 * ErrorType.FAILED.toString(); }
		 * 
		 * } }
		 * 
		 * fields.put(FieldType.STATUS.getName(), status);
		 * fields.put(FieldType.RESPONSE_MESSAGE.getName(),
		 * errorType.getResponseMessage());
		 * fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
		 * 
		 * fields.put(FieldType.AUTH_CODE.getName(), transaction.getRefund_ref());
		 * fields.put(FieldType.ACQ_ID.getName(), transaction.getPg_ref());
		 * fields.put(FieldType.RRN.getName(), transaction.getAg_ref());
		 * fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getRes_code());
		 * 
		 * if (StringUtils.isNotBlank(transaction.getStatus())) {
		 * fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getStatus()); }
		 * else { fields.put(FieldType.PG_TXN_STATUS.getName(),
		 * errorType.getResponseCode()); }
		 * 
		 * fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		 * 
		 * }
		 */

	}

}
