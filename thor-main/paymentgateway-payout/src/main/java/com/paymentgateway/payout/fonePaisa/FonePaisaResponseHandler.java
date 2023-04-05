package com.paymentgateway.payout.fonePaisa;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class FonePaisaResponseHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(FonePaisaResponseHandler.class);

	public void handleTransactionResponse(String response, Fields fields) {
		logger.info("final Response Fone Paisa transaction "+response+" ORDER_ID "+fields.get(FieldType.ORDER_ID.getName()));
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
		
				if(transaction.getStatus().equalsIgnoreCase("SUCCESS")){
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;
					
				}else if(transaction.getStatus().equalsIgnoreCase("PROCESSING") || transaction.getStatus().equalsIgnoreCase("SENT_TO_BENEFICIARY")){
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
				
				if (StringUtils.isNotBlank(transaction.getPgRespCode())) {
					fields.put(FieldType.PG_RESP_CODE.getName(),transaction.getPgRespCode());
				}

				if (StringUtils.isNotBlank(transaction.getRrn())) {
					fields.put(FieldType.RRN.getName(), transaction.getRrn());
					fields.put(FieldType.UTR_NO.getName(), transaction.getRrn());
				}
				
				if (StringUtils.isNotBlank(transaction.getAcqId())) {
					fields.put(FieldType.ACQ_ID.getName(), transaction.getAcqId());
				}

			}
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));

		} catch (Exception e) {
			logger.info("Exception in handleTransactionResponse() ",e);
		}finally {
			fields.remove(FieldType.MERCHANT_ID.getName());
			fields.remove(FieldType.TXN_KEY.getName());
		}
		
	}
	
	
	

}
