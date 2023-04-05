package com.paymentgateway.payout.apexPay;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class ApexPayResponseHandler {

	private static final Logger logger = LoggerFactory.getLogger(ApexPayResponseHandler.class);

	@Autowired
	private ApexPayUtils apexPayUtils;

	public void handleTransactionResponse(String response, Fields fields) {
		logger.info("final Response Apex pay transaction "+response+" ORDER_ID "+fields.get(FieldType.ORDER_ID.getName()));
		try {
			if (StringUtils.isBlank(response)) {
				
				fields.put(FieldType.STATUS.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getCode());

			} else {
				Transaction transaction = Transaction.toTransact(response);

				String status;
				ErrorType errorType;

				if (transaction.getStatus().equalsIgnoreCase("SUCCESS")) {
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;

				} else if (transaction.getStatus().equalsIgnoreCase("PENDING")) {
					status = StatusType.PROCESSING.getName();
					errorType = ErrorType.PROCESSING;

				} else {
					status = StatusType.FAILED.getName();
					errorType = ErrorType.FAILED;
				}

				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getCode());

				if (StringUtils.isNotBlank(transaction.getMsg())) {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getMsg());
				} else {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), errorType.getResponseMessage());
				}

				if (StringUtils.isNotBlank(transaction.getRrn())) {
					fields.put(FieldType.RRN.getName(), transaction.getRrn());
					fields.put(FieldType.UTR_NO.getName(), transaction.getRrn());
				}

			}

			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			
			apexPayUtils.removeFields(fields);

		} catch (Exception e) {
			logger.info("Exception in handleTransactionResponse() ",e);
		}

	}
	
	
	public void handleStatusEnquiryResponse(String response, Fields fields) {
		logger.info("final Response Apex pay handleStatusEnquiryResponse "+response+" ORDER_ID "+fields.get(FieldType.ORDER_ID.getName()));
		try {
			if (StringUtils.isBlank(response)) {
				
				fields.put(FieldType.STATUS.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getCode());

				
			} else {
				Transaction transaction = Transaction.toStatus(response);

				String status;
				ErrorType errorType;

				if (transaction.getStatus().equalsIgnoreCase("SUCCESS")) {
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;

				} else if (transaction.getStatus().equalsIgnoreCase("PENDING") || transaction.getStatus().equalsIgnoreCase("HOLD")) {
					status = StatusType.PROCESSING.getName();
					errorType = ErrorType.PROCESSING;

				} else {
					status = StatusType.FAILED.getName();
					errorType = ErrorType.FAILED;
				}

				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getCode());

				if (StringUtils.isNotBlank(transaction.getMsg())) {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getMsg());
				} else {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), errorType.getResponseMessage());
				}

				if (StringUtils.isNotBlank(transaction.getRrn())) {
					fields.put(FieldType.RRN.getName(), transaction.getRrn());
					fields.put(FieldType.UTR_NO.getName(), transaction.getRrn());
				}

			}

			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			apexPayUtils.removeFields(fields);
		} catch (Exception e) {
			logger.info("Exception in handleTransactionResponse() ",e);
		}

	}
	
	public void handleApexCallbackResponse(String response, Fields fields) {
		logger.info("Response Received for Apexpay Callback "+response+" ORDER_ID "+fields.get(FieldType.ORDER_ID.getName()));
		try {
			if (StringUtils.isBlank(response)) {
				
				fields.put(FieldType.STATUS.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getCode());

				
			} else {
				Transaction transaction = Transaction.toStatusForCallback(response);

				String status;
				ErrorType errorType;

				if (transaction.getStatus().equalsIgnoreCase("SUCCESS")) {
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;

				} else if (transaction.getStatus().equalsIgnoreCase("PENDING") || transaction.getStatus().equalsIgnoreCase("HOLD")) {
					status = StatusType.PROCESSING.getName();
					errorType = ErrorType.PROCESSING;

				} else {
					status = StatusType.FAILED.getName();
					errorType = ErrorType.FAILED;
				}

				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getCode());

				if (StringUtils.isNotBlank(transaction.getMsg())) {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getMsg());
				} else {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), errorType.getResponseMessage());
				}

				if (StringUtils.isNotBlank(transaction.getRrn())) {
					fields.put(FieldType.RRN.getName(), transaction.getRrn());
					fields.put(FieldType.UTR_NO.getName(), transaction.getRrn());
				}

			}

			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			apexPayUtils.removeFields(fields);
		} catch (Exception e) {
			logger.info("Exception in handleTransactionResponse() ",e);
		}

	}

}
