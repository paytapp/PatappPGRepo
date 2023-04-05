package com.paymentgateway.pg.core.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;

public class ProcessManager {

	private static Logger logger = LoggerFactory.getLogger(ProcessManager.class.getName());

	public static void flow(Processor processor, Fields fields, boolean invalidAllowed) {

		// Process security
		try {
			if (invalidAllowed) {
				processor.preProcess(fields);
				processor.process(fields);
				processor.postProcess(fields);
			} else {
				if (!fields.isValid()) {
					return;
				}
				processor.preProcess(fields);

				if (!fields.isValid()) {
					return;
				}
				processor.process(fields);

				if (!fields.isValid()) {
					return;
				}
				processor.postProcess(fields);
			}
		} catch (SystemException systemException) {
			fields.setValid(false);
			logger.error("inside the processmanager in systemExcption in catch block : ", systemException);
			String origTxnType = fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
			String previousOrigTxnId = fields.get(FieldType.ORIG_TXN_ID.getName());
			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.VALIDATION_FAILED.getResponseCode())) {
				String orderId = fields.get(FieldType.ORDER_ID.getName());
				if (StringUtils.isBlank(orderId)
						|| !(new CrmValidator().validateField(CrmFieldType.ORDER_ID, orderId))) {
					// Put order id as 000 if its invalid
					fields.put(FieldType.ORDER_ID.getName(), ErrorType.SUCCESS.getCode());
				}
				if (!StringUtils.isBlank(origTxnType)) {
					fields.put(FieldType.TXNTYPE.getName(), origTxnType);
				} else {
					fields.put(FieldType.ORIG_TXNTYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
					fields.put(FieldType.TXNTYPE.getName(), TransactionType.INVALID.getName());
				}
				if (!StringUtils.isBlank(fields.get(FieldType.INTERNAL_TXN_CHANNEL.getName()))) {
					fields.put(FieldType.INTERNAL_TXN_CHANNEL.getName(),
							fields.get(FieldType.INTERNAL_TXN_CHANNEL.getName()));
				} else {
					fields.put(FieldType.INTERNAL_TXN_CHANNEL.getName(), TransactionType.INVALID.getName());
				}
				if (!StringUtils.isBlank(systemException.getMessage())) {
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getMessage());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), systemException.getMessage());
				}
			}

			if (fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.ENROLL.getName())
					&& !StringUtils.isBlank(origTxnType)) {
				fields.put(FieldType.TXNTYPE.getName(), origTxnType);
			}
			if (fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.NEWORDER.getName())
					&& !StringUtils.isBlank(origTxnType)) {
				fields.put(FieldType.TXNTYPE.getName(), origTxnType);
			}
			fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
			String txnType = fields.get(FieldType.TXNTYPE.getName());
			if (!txnType.equals(TransactionType.REFUND.getName())) {
				fields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			}
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			if (systemException.getErrorType().getResponseCode().equals(ErrorType.DENIED_BY_FRAUD.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), fields.get(Constants.PG_FRAUD_TYPE.getValue()));
			}

			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.ACQUIRER_NOT_FOUND.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.PG_ACQUIRER_ERROR.getValue());
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				logger.error("inside the processmanager in systemExcption in catch block in acquirer not found : ",
						systemException);
			}
			if (systemException.getErrorType().getResponseCode().equals(ErrorType.ACUIRER_DOWN.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.PG_ACQUIRER_ERROR.getValue());
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				logger.error("inside the processmanager in systemExcption in catch block in ACUIRER_DOWN : ",
						systemException);
			}
			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.INTERNAL_SYSTEM_ERROR.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.PG_ACQUIRER_ERROR.getValue());
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
			}

			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.REFUND_AMOUNT_MISMATCH.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
			}

			if (systemException.getErrorType().getResponseCode().equals(ErrorType.REFUND_DENIED.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.REFUND.getName());
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), previousOrigTxnId);
			}

			if (systemException.getErrorType().getResponseCode().equals(ErrorType.REFUND_REJECTED.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.REFUND.getName());
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), previousOrigTxnId);
			}

			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.REFUND_FLAG_AMOUNT_NOT_MATCH.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.REFUND.getName());
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), previousOrigTxnId);
			}

			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.TRANSACTION_NOT_FOUND.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.REFUND.getName());
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			}

			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.DUPLICATE_ORDER_ID.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), previousOrigTxnId);
			}

			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.DUPLICATE_REFUND_ORDER_ID.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.REFUND.getName());
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), previousOrigTxnId);
			}

			if (systemException.getErrorType().getResponseCode().equals(ErrorType.DUPLICATE.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), previousOrigTxnId);
			}
			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.PERMISSION_DENIED.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
			}

			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.VALIDATION_FAILED.getResponseCode())) {
				String newId = TransactionManager.getNewTransactionId();
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), systemException.getMessage());
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));

				fields.put(FieldType.PG_REF_NUM.getName(), newId);
				fields.put(FieldType.ORIG_TXN_ID.getName(), newId);
				fields.put(FieldType.TXN_ID.getName(), newId);
				fields.put(FieldType.OID.getName(), fields.get(FieldType.INTERNAL_ORIG_TXN_ID.getName()));

				fields.put(FieldType.ORIG_TXNTYPE.getName(), fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				fields.put(FieldType.TXNTYPE.getName(), fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
			}
			
			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.MIN_AMOUNT_ERROR.getResponseCode())) {
				String newId = TransactionManager.getNewTransactionId();
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), systemException.getMessage());
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));

				fields.put(FieldType.PG_REF_NUM.getName(), newId);
				fields.put(FieldType.ORIG_TXN_ID.getName(), newId);
				fields.put(FieldType.TXN_ID.getName(), newId);
				fields.put(FieldType.OID.getName(), fields.get(FieldType.INTERNAL_ORIG_TXN_ID.getName()));

				fields.put(FieldType.ORIG_TXNTYPE.getName(), fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
				fields.put(FieldType.TXNTYPE.getName(), fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
			}


			logger.error("SystemException", systemException);

		} catch (Exception exception) {
			fields.setValid(false);

			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.UNKNOWN.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.UNKNOWN.getResponseMessage());

			logger.error("Exception", exception);
		}
	}
}
