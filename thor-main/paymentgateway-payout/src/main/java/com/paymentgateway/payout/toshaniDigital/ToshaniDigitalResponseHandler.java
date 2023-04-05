package com.paymentgateway.payout.toshaniDigital;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class ToshaniDigitalResponseHandler {

	private static final Logger logger = LoggerFactory.getLogger(ToshaniDigitalResponseHandler.class);

	public void handleTransactionResponse(String response, Fields fields) {
		logger.info("final Response ToshaniDigital transaction " + response + " ORDER_ID "
				+ fields.get(FieldType.ORDER_ID.getName()));
		try {
			if (StringUtils.isBlank(response)) {

				fields.put(FieldType.STATUS.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getCode());

			} else {
				Transaction transaction = new Transaction(response);

				String status;
				ErrorType errorType;

				if (transaction.getResult() == 1) {
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;

				} else if (transaction.getResult() == 2) {
					status = StatusType.PROCESSING.getName();
					errorType = ErrorType.PROCESSING;

				} else {
					status = StatusType.FAILED.getName();
					errorType = ErrorType.FAILED;
				}

				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getCode());

				if (StringUtils.isNotBlank(transaction.getMessage())) {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getMessage());
				} else {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), errorType.getResponseMessage());
				}

				if (StringUtils.isNotBlank(transaction.getUtr())) {
					fields.put(FieldType.RRN.getName(), transaction.getUtr());
					fields.put(FieldType.UTR_NO.getName(), transaction.getUtr());
				}

				if (StringUtils.isNotBlank(transaction.getTxnId())) {
					fields.put(FieldType.ACQ_ID.getName(), transaction.getTxnId());
				}

				if (StringUtils.isNotBlank(transaction.getTxn_date())) {
					fields.put(FieldType.PG_DATE_TIME.getName(), transaction.getTxn_date());
				}

			}

			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));

		} catch (Exception e) {
			logger.info("Exception in handleTransactionResponse() ", e);
		} finally {
			fields.remove(FieldType.MERCHANT_ID.getName());
		}

	}

	public void handleStatusEnquiryResponse(String response, Fields fields) {
		logger.info("final Response ToshaniDigital handleStatusEnquiryResponse " + response + " ORDER_ID "
				+ fields.get(FieldType.ORDER_ID.getName()));
		try {
			if (StringUtils.isBlank(response)) {

				fields.put(FieldType.STATUS.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getCode());

			} else {
				Transaction transaction = null;
				try{
					transaction = new Transaction(response);
				}catch (Exception e) {
					logger.info("exception in handling Trasaction Response = {} ",response);
					transaction = new Transaction();
				}
				
				String status;
				ErrorType errorType;

				if (transaction.getResult() == 1) {
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;

				} else if (transaction.getResult() == 2) {
					status = StatusType.PROCESSING.getName();
					errorType = ErrorType.PROCESSING;

				} else {
					status = StatusType.FAILED.getName();
					errorType = ErrorType.FAILED;
				}

				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getCode());

				if (StringUtils.isNotBlank(transaction.getMessage())) {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getMessage());
				} else {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), errorType.getResponseMessage());
				}

				if (StringUtils.isNotBlank(transaction.getUtr())) {
					fields.put(FieldType.RRN.getName(), transaction.getUtr());
					fields.put(FieldType.UTR_NO.getName(), transaction.getUtr());
				}

				if (StringUtils.isNotBlank(transaction.getTxnId())) {
					fields.put(FieldType.ACQ_ID.getName(), transaction.getTxnId());
				}

				if (StringUtils.isNotBlank(transaction.getTxn_date())) {
					fields.put(FieldType.PG_DATE_TIME.getName(), transaction.getTxn_date());
				}
			}

			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
		} catch (Exception e) {
			logger.info("Exception in handleTransactionResponse() ", e);
		}

	}

}
