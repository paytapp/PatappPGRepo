package com.paymentgateway.payu;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
 
/**
 * @author Rahul
 *
 */
@Service
public class PayuTransformer {

	private static Logger logger = LoggerFactory.getLogger(PayuTransformer.class.getName());
	
	private Transaction transaction = null;

	public PayuTransformer(Transaction transaction) {
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
				&& ((transaction.getResponseCode()).equalsIgnoreCase(Constants.E000))
				&& fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;

			if (StringUtils.isNotBlank(transaction.getResponseMsg())) {
				pgTxnMsg = transaction.getResponseMsg();
			}

			else {
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
			}

		}

		else {
			if ((StringUtils.isNotBlank(transaction.getResponseCode()))) {

				String respCode = null;
				if (StringUtils.isNotBlank(transaction.getResponseCode())) {
					// For Sale
					respCode = transaction.getResponseCode();
				}

				PayuResultType resultInstance = PayuResultType.getInstanceFromName(respCode);

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());

					if (StringUtils.isNotBlank(transaction.getResponseMsg())) {
						pgTxnMsg = transaction.getResponseMsg();
					}

					else {
						pgTxnMsg = resultInstance.getMessage();
					}

				} else {
					status = StatusType.FAILED_AT_ACQUIRER.getName();
					errorType = ErrorType.getInstanceFromCode(Constants.ERROR022);

					if (StringUtils.isNotBlank(transaction.getResponseMsg())) {
						pgTxnMsg = transaction.getResponseMsg();
					}

					else {
						pgTxnMsg = ErrorType.FAILED.toString();
					}

				}

			} else {
				status = StatusType.FAILED_AT_ACQUIRER.getName();
				errorType = ErrorType.FAILED;
				if (StringUtils.isNotBlank(transaction.getResponseMsg())) {
					pgTxnMsg = transaction.getResponseMsg();
				} else {
					pgTxnMsg =  ErrorType.FAILED.toString();
				}

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.RRN.getName(), transaction.getBankRefNum());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getMihPayuId());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());

		if (StringUtils.isNotBlank(transaction.getStatus())) {
			fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getStatus());
		} else {
			fields.put(FieldType.PG_TXN_STATUS.getName(), errorType.getResponseCode());
		}

		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

	}

	public void updateRefundResponse(Fields fields) {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;

		// For Refund
		if ((StringUtils.isNotBlank(transaction.getStatus()) && ((transaction.getStatus()).equalsIgnoreCase("1"))
				&& fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.REFUND.getName())))

		{
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		}

		// For Sale
		else if ((StringUtils.isNotBlank(transaction.getResponseCode()))
				&& ((transaction.getResponseCode()).equalsIgnoreCase(Constants.E000))
				&& fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName()))

		{
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		}

		else {
			if ((StringUtils.isNotBlank(transaction.getResponseCode()))
					|| (StringUtils.isNotBlank(transaction.getStatus()))) {

				String respCode = null;
				if (StringUtils.isNotBlank(transaction.getResponseCode())) {

					// For Sale
					respCode = transaction.getResponseCode();
				} else {

					// For Refund
					respCode = transaction.getStatus();
				}

				PayuResultType resultInstance = PayuResultType.getInstanceFromName(respCode);

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());

					if (StringUtils.isNotBlank(transaction.getResponseMsg())) {
						pgTxnMsg = transaction.getResponseMsg();
					}

					else {
						pgTxnMsg = resultInstance.getMessage();
					}

				} else {
					status = StatusType.FAILED_AT_ACQUIRER.getName();
					errorType = ErrorType.getInstanceFromCode(Constants.ERROR022);

					if (StringUtils.isNotBlank(transaction.getResponseMsg())) {
						pgTxnMsg = transaction.getResponseMsg();
					}

					else {
						pgTxnMsg = ErrorType.FAILED.toString();
					}

				}

			} else {
				status = StatusType.FAILED_AT_ACQUIRER.getName();
				errorType = ErrorType.FAILED;
				if (StringUtils.isNotBlank(transaction.getResponseMsg())) {
					pgTxnMsg = transaction.getResponseMsg();
				} else {
					pgTxnMsg = ErrorType.FAILED.toString();
				}

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.RRN.getName(), transaction.getRefundToken());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getMihPayuId());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());

		if (StringUtils.isNotBlank(transaction.getStatus())) {
			fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getStatus());
		} else {
			fields.put(FieldType.PG_TXN_STATUS.getName(), errorType.getResponseCode());
		}

		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

	}
	
	 

}
