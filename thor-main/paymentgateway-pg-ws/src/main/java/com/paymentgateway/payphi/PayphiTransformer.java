package com.paymentgateway.payphi;

import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.EPOSTransactionDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;

/**
 * @author Shaiwal
 * 
 *
 */

@Service
public class PayphiTransformer {

	private Transaction transaction = null;

	public PayphiTransformer(Transaction transaction) {
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

		if ((StringUtils.isNotBlank(transaction.getResponseCode()))
				&& ((transaction.getResponseCode()).equalsIgnoreCase("0000"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
		} else {
			if (StringUtils.isNotBlank(transaction.getResponseCode())) {
				PayphiResultType resultInstance = PayphiResultType.getInstanceFromName(transaction.getResponseCode());

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());
					if (StringUtils.isNotBlank(transaction.getRespDescription())) {
						pgTxnMsg = transaction.getRespDescription();
					} else {
						pgTxnMsg = resultInstance.getMessage();
					}

				} else {
					status = StatusType.DECLINED.getName();
					errorType = ErrorType.getInstanceFromCode("004");
					if (StringUtils.isNotBlank(transaction.getRespDescription())) {
						pgTxnMsg = transaction.getRespDescription();
					} else {
						pgTxnMsg = "Transaction Declined by acquirer";
					}
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				if (StringUtils.isNotBlank(transaction.getRespDescription())) {
					pgTxnMsg = transaction.getRespDescription();
				} else {
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				}

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.AUTH_CODE.getName(), transaction.getPaymentID());
		fields.put(FieldType.RRN.getName(), transaction.getPaymentID());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getTxnID());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getRespDescription());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

		if (!StringUtils.isEmpty(fields.get(FieldType.EPOS_MERCHANT.getName()))) {
			if ((Boolean.valueOf(fields.get(FieldType.EPOS_MERCHANT.getName())))
					&& (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName()))) {
				ExecutorService es = ThreadPoolProvider.getExecutorService();
				es.execute(new Runnable() {
					@Override
					public void run() {
						new EPOSTransactionDao().updateEposRefundTransaction(fields);
					}
				});
				es.shutdown();
			}
		}

	}

	public void updateStatusResponse(Fields fields) {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if ((StringUtils.isNotBlank(transaction.getTxnStatus()))
				&& (StringUtils.isNotBlank(transaction.getTxnResponseCode()))
				&& (StringUtils.isNotBlank(transaction.getResponseCode()))
				&& ((transaction.getTxnStatus()).equalsIgnoreCase("SUC"))
				&& ((transaction.getTxnResponseCode()).equalsIgnoreCase("0000"))
				&& ((transaction.getResponseCode()).equalsIgnoreCase("000"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;

			if (StringUtils.isNotBlank(transaction.getRespDescription())) {
				pgTxnMsg = transaction.getRespDescription();
			} else {
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
			}

		} else {
			if (StringUtils.isNotBlank(transaction.getResponseCode())
					&& StringUtils.isNotBlank(transaction.getTxnResponseCode())
					&& !((transaction.getResponseCode()).equalsIgnoreCase("000"))
					&& !((transaction.getTxnResponseCode()).equalsIgnoreCase("0000"))) {
				PayphiResultType resultInstance = PayphiResultType.getInstanceFromName(transaction.getResponseCode());

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());
					if (StringUtils.isNotBlank(transaction.getRespDescription())) {
						pgTxnMsg = transaction.getRespDescription();
					} else {
						pgTxnMsg = resultInstance.getMessage();
					}

				} else {
					status = StatusType.DECLINED.getName();
					errorType = ErrorType.getInstanceFromCode("004");
					if (StringUtils.isNotBlank(transaction.getRespDescription())) {
						pgTxnMsg = transaction.getRespDescription();
					} else {
						pgTxnMsg = "Transaction Declined by acquirer";
					}
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				if (StringUtils.isNotBlank(transaction.getRespDescription())) {
					pgTxnMsg = transaction.getRespDescription();
				} else {
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				}

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.AUTH_CODE.getName(), transaction.getTxnAuthID());
		fields.put(FieldType.RRN.getName(), transaction.getTxnAuthID());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getTxnID());
		
		if (StringUtils.isNotBlank(transaction.getTxnResponseCode())) {
			fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getTxnResponseCode());
		}
		else {
			fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());
		}
		
		if (StringUtils.isNotBlank(transaction.getTxnStatus())) {
			fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getTxnStatus());
		}
		else {
			fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getRespDescription());
		}
		
		if (StringUtils.isNotBlank(transaction.getTxnRespDescription())) {
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getTxnRespDescription());
		}
		else {
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getRespDescription());
		}
		

	}
	
	public void updateRefundResponse(Fields fields) {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if ((StringUtils.isNotBlank(transaction.getResponseCode()))
				&& ((transaction.getResponseCode()).equalsIgnoreCase("P1000"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;

			if (StringUtils.isNotBlank(transaction.getRespDescription())) {
				pgTxnMsg = transaction.getRespDescription();
			} else {
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
			}

		} else {
			if (StringUtils.isNotBlank(transaction.getResponseCode())) {
				PayphiResultType resultInstance = PayphiResultType.getInstanceFromName(transaction.getResponseCode());

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());
					if (StringUtils.isNotBlank(transaction.getRespDescription())) {
						pgTxnMsg = transaction.getRespDescription();
					} else {
						pgTxnMsg = resultInstance.getMessage();
					}

				} else {
					status = StatusType.DECLINED.getName();
					errorType = ErrorType.getInstanceFromCode("004");
					if (StringUtils.isNotBlank(transaction.getRespDescription())) {
						pgTxnMsg = transaction.getRespDescription();
					} else {
						pgTxnMsg = "Transaction Declined by acquirer";
					}
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				if (StringUtils.isNotBlank(transaction.getRespDescription())) {
					pgTxnMsg = transaction.getRespDescription();
				} else {
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				}

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.AUTH_CODE.getName(), transaction.getTxnID());
		fields.put(FieldType.RRN.getName(), transaction.getTxnID());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getTxnID());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getRespDescription());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

	}

}
