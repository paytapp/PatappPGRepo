package com.paymentgateway.cyberSource;

import org.apache.commons.lang3.StringUtils;
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
public class CyberSourceTransformer {

	private Transaction transaction = null;

	public CyberSourceTransformer(Transaction transaction) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public void updateResponse(Fields fields) {

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;
		if (txnType.equals(TransactionType.ENROLL.getName())) {

			if ((StringUtils.isNotBlank(transaction.getAcsURL())) && ((transaction.getResponseCode()).equals("475"))
					&& ((transaction.getVeresEnrolled()).equalsIgnoreCase("Y"))) {
				status = StatusType.ENROLLED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();

			} else {
				if (StringUtils.isNotBlank(transaction.getResponseCode())) {
					CyberSourceResultType resultInstance = CyberSourceResultType
							.getInstanceFromName(transaction.getResponseCode());

					if (resultInstance != null) {
						status = resultInstance.getStatusName();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
						pgTxnMsg = resultInstance.getMessage();	
					}
					else {
						status = StatusType.FAILED_AT_ACQUIRER.getName();
						errorType = ErrorType.getInstanceFromCode("022");
						pgTxnMsg = "Transaction failed at acquirer";
					}
					
				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

				}
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
				fields.put(FieldType.TXNTYPE.getName(), fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
			}
		} else {

			if (transaction.getResponseCode().equals("100")) {
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
			} else {
				if (StringUtils.isNotBlank(transaction.getResponseCode())) {
					CyberSourceResultType resultInstance = CyberSourceResultType
							.getInstanceFromName(transaction.getResponseCode());

					if (resultInstance != null) {
						status = resultInstance.getStatusName();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
						pgTxnMsg = resultInstance.getMessage();
					}
					else {
						status = StatusType.FAILED_AT_ACQUIRER.getName();
						errorType = ErrorType.getInstanceFromCode("022");
						pgTxnMsg = "Transaction failed at acquirer";	
					}
					
				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				}
			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());
		fields.put(FieldType.ACS_URL.getName(), transaction.getAcsURL());
		fields.put(FieldType.PAREQ.getName(), transaction.getPaReq());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getRequestId());
		fields.put(FieldType.MD.getName(), transaction.getXid());
		fields.put(FieldType.RRN.getName(), transaction.getRecoId());

	}

}
