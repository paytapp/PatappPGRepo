package com.paymentgateway.apexPay;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class ApexPayTransformer {

	private static Logger logger = LoggerFactory.getLogger(ApexPayTransformer.class.getName());
	
	private Transaction transaction = null;

	public ApexPayTransformer(Transaction transaction) {
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

	   if ((StringUtils.isNotBlank(transaction.getResponseCode())) && (StringUtils.isNotBlank(transaction.getResponseCode())) &&
				((transaction.getStatus()).equalsIgnoreCase("Captured")) && ((transaction.getResponseCode()).equalsIgnoreCase("000")))

		{
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
		}
		
		
		else {
			if ((StringUtils.isNotBlank(transaction.getResponseCode()))) {

				String respCode = null;
				if (StringUtils.isNotBlank(transaction.getResponseCode())){
					respCode = transaction.getResponseCode();
				}
				
				ApexPayResultType resultInstance = ApexPayResultType.getInstanceFromName(respCode);
				
				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());
					pgTxnMsg = resultInstance.getMessage();
					
				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.getInstanceFromCode("007");
					pgTxnMsg = "Transaction Declined";
					
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
					pgTxnMsg = "Transaction Rejected";
			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.RRN.getName(), transaction.getRrn());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getAcqId());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());
		fields.put(FieldType.PG_TXN_STATUS.getName(), errorType.getResponseCode());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);

	}
	
	
	public void updateRefundResponse(Fields fields) {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;

	   if ((StringUtils.isNotBlank(transaction.getResponseCode())) && (StringUtils.isNotBlank(transaction.getResponseCode())) &&
				((transaction.getStatus()).equalsIgnoreCase("Captured")) && ((transaction.getResponseCode()).equalsIgnoreCase("000")))

		{
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
		}
		
		
		else {
			if ((StringUtils.isNotBlank(transaction.getResponseCode()))) {

				String respCode = null;
				if (StringUtils.isNotBlank(transaction.getResponseCode())){
					respCode = transaction.getResponseCode();
				}
				
				ApexPayResultType resultInstance = ApexPayResultType.getInstanceFromName(respCode);
				
				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getiPayCode());
					pgTxnMsg = resultInstance.getMessage();
					
				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.getInstanceFromCode("007");
					pgTxnMsg = "Transaction Declined";
					
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
					pgTxnMsg = "Transaction Rejected";
			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.RRN.getName(), transaction.getRrn());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getAcqId());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());
		fields.put(FieldType.PG_TXN_STATUS.getName(), errorType.getResponseCode());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
	}

}
