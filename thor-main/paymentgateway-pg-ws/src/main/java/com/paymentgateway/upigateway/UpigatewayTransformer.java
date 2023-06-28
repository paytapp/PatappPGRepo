package com.paymentgateway.upigateway;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class UpigatewayTransformer {

	private Transaction transaction = null;

	public UpigatewayTransformer(Transaction transaction) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public void updateResponse(Fields fields) {

		String status = "";
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if ((StringUtils.isNotBlank(transaction.getStatus()))
				&& ((transaction.getStatus()).equalsIgnoreCase("success"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

		} 
		else if ((StringUtils.isNotBlank(transaction.getStatus()))  && (StringUtils.isNotBlank(transaction.getRemark()))
				&& ((transaction.getStatus()).equalsIgnoreCase("scanning")) && ((transaction.getRemark()).equalsIgnoreCase("Transaction Timeout.")))  {
			status = StatusType.TIMEOUT.getName();
			errorType = ErrorType.TIMEOUT;
			pgTxnMsg = transaction.getRemark();
			
		}
		
		else if ((StringUtils.isNotBlank(transaction.getStatus()))
				&& ((transaction.getStatus()).equalsIgnoreCase("scanning"))) {
			status = StatusType.PROCESSING.getName();
			errorType = ErrorType.PROCESSING;
			pgTxnMsg = transaction.getStatus();
			
		}
		
		else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;

				if (StringUtils.isNotBlank(transaction.getMsg())) {
					pgTxnMsg = transaction.getMsg();
				} else {
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				}

		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.ACQ_ID.getName(), transaction.getId());
		fields.put(FieldType.PG_RESP_CODE.getName(), errorType.getResponseCode());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.RRN.getName(), transaction.getUpi_txn_id());
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getStatus());
		
		if (StringUtils.isNotBlank(transaction.getCustomer_vpa())) {
			fields.put(FieldType.CARD_MASK.getName(), transaction.getCustomer_vpa());
			fields.put(FieldType.PAYER_ADDRESS.getName(), transaction.getCustomer_vpa());
			
		}
		
		if (StringUtils.isNotBlank(transaction.getCustomer_name())) {
			fields.put(FieldType.CUST_NAME.getName(), transaction.getCustomer_name());
		}
		
		if (StringUtils.isNotBlank(transaction.getCustomer_email())) {
			fields.put(FieldType.CUST_EMAIL.getName(), transaction.getCustomer_email());
		}
		
		if (StringUtils.isNotBlank(transaction.getCustomer_mobile())) {
			fields.put(FieldType.CUST_PHONE.getName(), transaction.getCustomer_mobile());
		}

	}

}
