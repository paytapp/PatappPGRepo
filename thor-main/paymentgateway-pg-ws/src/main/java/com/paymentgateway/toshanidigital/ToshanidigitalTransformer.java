
package com.paymentgateway.toshanidigital;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class ToshanidigitalTransformer {

	private Transaction transaction = null;

	public ToshanidigitalTransformer(Transaction transaction) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	private static Logger logger = LoggerFactory.getLogger(ToshanidigitalTransformer.class.getName());
	
	
	public void updateResponse(Fields fields, Transaction transactionResponse) throws SystemException {

		try {

			String txnType = fields.get(FieldType.TXNTYPE.getName());
			String status = "";
			ErrorType errorType = null;
			String pgTxnMsg = "";
			if (txnType.equals(TransactionType.SALE.getName())) {


					if ( StringUtils.isNotBlank(transactionResponse.getPayments_status())
							&& transactionResponse.getPayments_status().equalsIgnoreCase("SUCCESS")) {
						fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
						status = StatusType.CAPTURED.getName();
						errorType = ErrorType.SUCCESS;

						if (StringUtils.isNotBlank(transactionResponse.getMessage())) {
							pgTxnMsg = transactionResponse.getMessage();
						} else {
							pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
						}


				} else  if (StringUtils.isNotBlank(transactionResponse.getPayments_status())
							&& transactionResponse.getPayments_status().equalsIgnoreCase("FAILED")) {
						fields.put(FieldType.STATUS.getName(), StatusType.FAILED_AT_ACQUIRER.getName());
						status = StatusType.FAILED_AT_ACQUIRER.getName();
						errorType = ErrorType.FAILED;

						if (StringUtils.isNotBlank(transactionResponse.getMessage())) {
							pgTxnMsg = transactionResponse.getMessage();
						} else {
							pgTxnMsg = ErrorType.FAILED.getResponseMessage();
						}

				}
				
				else if (StringUtils.isNotBlank(transactionResponse.getPayments_status())
							&& transactionResponse.getPayments_status().equalsIgnoreCase("PENDING")) {
						fields.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
						status = StatusType.PROCESSING.getName();
						errorType = ErrorType.PROCESSING;

						if (StringUtils.isNotBlank(transactionResponse.getMessage())) {
							pgTxnMsg = transactionResponse.getMessage();
						} else {
							pgTxnMsg = ErrorType.PROCESSING.getResponseMessage();
						}


				}
				
				else {

					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					if (StringUtils.isNotBlank(transactionResponse.getMessage())) {
						pgTxnMsg = transactionResponse.getMessage();
					} else {
						pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
					}

				}

				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage().replaceAll("_", ""));
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
				fields.put(FieldType.ACQ_ID.getName(), transactionResponse.getOrder_id());
				fields.put(FieldType.RRN.getName(), transactionResponse.getRrn());
				fields.put(FieldType.AUTH_CODE.getName(), transactionResponse.getQr_id());
				fields.put(FieldType.PG_RESP_CODE.getName(), transactionResponse.getResult());
				fields.put(FieldType.PG_TXN_STATUS.getName(), transactionResponse.getStatus());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

			}

		} catch (Exception e) {
			logger.error("Unknown Exception :", e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in UpdateResponse method for Toshani");
		}

	}
	
	public void updateStatusResponse(Fields fields, Transaction transactionResponse) throws SystemException {

		try {

			String txnType = fields.get(FieldType.TXNTYPE.getName());
			String status = "";
			ErrorType errorType = null;
			String pgTxnMsg = "";

				if (StringUtils.isNotBlank(transactionResponse.getResult())
						&& transactionResponse.getResult().equals("1")) {

					if (StringUtils.isNotBlank(transactionResponse.getStatus())
							&& StringUtils.isNotBlank(transactionResponse.getPayments_status())
							&& transactionResponse.getStatus().equalsIgnoreCase("SUCCESS")
							&& transactionResponse.getPayments_status().equalsIgnoreCase("SUCCESS")) {
						fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
						status = StatusType.CAPTURED.getName();
						errorType = ErrorType.SUCCESS;

						if (StringUtils.isNotBlank(transactionResponse.getMessage())) {
							pgTxnMsg = transactionResponse.getMessage();
						} else {
							pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
						}

					}

				} else  if (StringUtils.isNotBlank(transactionResponse.getResult())
						&& transactionResponse.getResult().equals("0")) {

					if (StringUtils.isNotBlank(transactionResponse.getStatus())
							&& StringUtils.isNotBlank(transactionResponse.getPayments_status())
							&& transactionResponse.getStatus().equalsIgnoreCase("FAILED")
							&& transactionResponse.getPayments_status().equalsIgnoreCase("FAILED")) {
						fields.put(FieldType.STATUS.getName(), StatusType.FAILED_AT_ACQUIRER.getName());
						status = StatusType.FAILED_AT_ACQUIRER.getName();
						errorType = ErrorType.FAILED;

						if (StringUtils.isNotBlank(transactionResponse.getMessage())) {
							pgTxnMsg = transactionResponse.getMessage();
						} else {
							pgTxnMsg = ErrorType.FAILED.getResponseMessage();
						}

					}
				}
				
				else if (StringUtils.isNotBlank(transactionResponse.getResult())
						&& transactionResponse.getResult().equals("2")) {

					if (StringUtils.isNotBlank(transactionResponse.getStatus())
							&& StringUtils.isNotBlank(transactionResponse.getPayments_status())
							&& transactionResponse.getStatus().equalsIgnoreCase("PENDING")
							&& transactionResponse.getPayments_status().equalsIgnoreCase("PENDING")) {
						fields.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
						status = StatusType.PROCESSING.getName();
						errorType = ErrorType.PROCESSING;

						if (StringUtils.isNotBlank(transactionResponse.getMessage())) {
							pgTxnMsg = transactionResponse.getMessage();
						} else {
							pgTxnMsg = ErrorType.PROCESSING.getResponseMessage();
						}

					} 

				}
				
				else {

					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					if (StringUtils.isNotBlank(transactionResponse.getMessage())) {
						pgTxnMsg = transactionResponse.getMessage();
					} else {
						pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
					}

				}

				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage().replaceAll("_", ""));
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
				fields.put(FieldType.ACQ_ID.getName(), transactionResponse.getOrder_id());
				fields.put(FieldType.RRN.getName(), transactionResponse.getRrn());
				fields.put(FieldType.AUTH_CODE.getName(), transactionResponse.getQr_id());
				fields.put(FieldType.PG_RESP_CODE.getName(), transactionResponse.getResult());
				fields.put(FieldType.PG_TXN_STATUS.getName(), transactionResponse.getStatus());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

		} catch (Exception e) {
			logger.error("Unknown Exception :", e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in UpdateResponse method for Toshani");
		}

	}

	public void updateCollectResponse(Fields fields, Transaction transactionResponse) throws SystemException {
		try {

			String txnType = fields.get(FieldType.TXNTYPE.getName());
			String status = "";
			ErrorType errorType = null;
			String pgTxnMsg = "";
			if (txnType.equals(TransactionType.SALE.getName())) {

				if (StringUtils.isNotBlank(transactionResponse.getResult())
						&& transactionResponse.getResult().equals("1")) {

					if (StringUtils.isNotBlank(transactionResponse.getStatus())
							&& StringUtils.isNotBlank(transactionResponse.getPayments_status())
							&& transactionResponse.getStatus().equalsIgnoreCase("Pending")
							&& transactionResponse.getPayments_status().equalsIgnoreCase("Pending")) {
						fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
						status = StatusType.SENT_TO_BANK.getName();
						errorType = ErrorType.SUCCESS;

						if (StringUtils.isNotBlank(transactionResponse.getMessage())) {
							pgTxnMsg = transactionResponse.getMessage();
						} else {
							pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
						}

					} else {
						status = StatusType.REJECTED.getName();
						errorType = ErrorType.REJECTED;
						if (StringUtils.isNotBlank(transactionResponse.getMessage())) {
							pgTxnMsg = transactionResponse.getMessage();
						} else {
							pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
						}
					}

				} else {

					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					if (StringUtils.isNotBlank(transactionResponse.getMessage())) {
						pgTxnMsg = transactionResponse.getMessage();
					} else {
						pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
					}

				}

				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage().replaceAll("_", ""));
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
				fields.put(FieldType.ACQ_ID.getName(), transactionResponse.getOrder_id());
				fields.put(FieldType.RRN.getName(), transactionResponse.getRrn());
				fields.put(FieldType.AUTH_CODE.getName(), transactionResponse.getQr_id());
				fields.put(FieldType.PG_RESP_CODE.getName(), transactionResponse.getResult());
				fields.put(FieldType.PG_TXN_STATUS.getName(), transactionResponse.getStatus());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

			}

		} catch (Exception e) {
			logger.error("Unknown Exception :", e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in UpdateResponse method for Toshani");
		}

	}

}
