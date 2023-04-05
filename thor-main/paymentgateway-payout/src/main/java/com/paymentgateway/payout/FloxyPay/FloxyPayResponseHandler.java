package com.paymentgateway.payout.FloxyPay;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class FloxyPayResponseHandler {
	
	private static Logger logger = LoggerFactory.getLogger(FloxyPayResponseHandler.class);

	public void handleTransactionResponse(String response, Fields fields) {
		logger.info("final Response FloxyPay transaction >> {} OrderId >> {}",response,fields.get(FieldType.ORDER_ID.getName()));
		try {
			if (StringUtils.isBlank(response)) {
				
				fields.put(FieldType.STATUS.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getCode());

			} else {
				String status =null;
				ErrorType errorType = null;
				
				Transaction transaction = new Transaction(response);
		
				if((transaction.getPgRespMsg().equalsIgnoreCase("Transfer completed successfully") || transaction.getPgRespMsg().equalsIgnoreCase("Successful..!")) && StringUtils.isNotBlank(transaction.getUtr())){
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;
					
				}else if(transaction.getPgRespMsg().equalsIgnoreCase("Transfer request pending at the bank")){
					status = StatusType.PROCESSING.getName();
					errorType = ErrorType.PROCESSING;
				}else{
					status = StatusType.FAILED.getName();
					errorType = ErrorType.FAILED;
				}

				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getCode());

				if (StringUtils.isNotBlank(transaction.getPgRespMsg())) {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getPgRespMsg());
				} else {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), errorType.getResponseMessage());
				}
				
				if (StringUtils.isNotBlank(transaction.getUtr())) {
					fields.put(FieldType.RRN.getName(), transaction.getUtr());
					fields.put(FieldType.UTR_NO.getName(), transaction.getUtr());
				}
				
				if (StringUtils.isNotBlank(transaction.getAcqId())) {
					fields.put(FieldType.ACQ_ID.getName(), transaction.getAcqId());
				}

			}
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));

		} catch (Exception e) {
			logger.info("Exception in handleTransactionResponse() ",e);
		}finally {
			fields.remove(FieldType.ADF1.getName());
			fields.remove(FieldType.ADF2.getName());
		}
		
	}

	public void handleStatusEnquiryTransactionResponse(String response, Fields fields) throws SystemException {
		try{
			if(StringUtils.isNotBlank(response)){
				
				String status =null;
				ErrorType errorType = null;
				
				Transaction transaction = new Transaction(response);

				if(StringUtils.isNotBlank(transaction.getStatus()) && transaction.getStatus().equalsIgnoreCase("SUCCESS")){
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;
					
				}else if((StringUtils.isNotBlank(transaction.getPgRespMsg()) && transaction.getPgRespMsg().equalsIgnoreCase("Transfer request pending at the bank")) 
						|| (StringUtils.isNotBlank(transaction.getStatus()) && transaction.getStatus().equalsIgnoreCase("PENDING"))){
					status = StatusType.PROCESSING.getName();
					errorType = ErrorType.PROCESSING;
				}else{
					status = StatusType.FAILED.getName();
					errorType = ErrorType.FAILED;
				}

				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getCode());

				if (StringUtils.isNotBlank(transaction.getPgRespMsg())) {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getPgRespMsg());
				}else{
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getStatus());
				}
				
				if (StringUtils.isNotBlank(transaction.getUtr())) {
					fields.put(FieldType.RRN.getName(), transaction.getUtr());
					fields.put(FieldType.UTR_NO.getName(), transaction.getUtr());
				}
				
				if (StringUtils.isNotBlank(transaction.getAcqId())) {
					fields.put(FieldType.ACQ_ID.getName(), transaction.getAcqId());
				}
				
				
			}else{
				logger.info("Empty Response Status Enquiry, FloxyPay handleStatusEnquiryTransactionResponse() {} ", response);
				throw new SystemException("Empty Response Status Enquiry, FloxyPay handleStatusEnquiryTransactionResponse()");
			}
			
			
		}catch (Exception e) {
			logger.info("Exception in handleStatusEnquiryTransactionResponse() ",e);
		}finally {
			fields.remove(FieldType.ADF1.getName());
			fields.remove(FieldType.ADF2.getName());
		}
		
	}

}
