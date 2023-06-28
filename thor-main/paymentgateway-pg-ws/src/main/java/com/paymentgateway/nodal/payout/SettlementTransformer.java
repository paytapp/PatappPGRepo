package com.paymentgateway.nodal.payout;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.AccountStatus;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.SettlementTransactionType;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Rahul
 *
 */
@Service
public class SettlementTransformer {

	private Transaction transaction = null;

	public SettlementTransformer(Transaction transaction) {
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
		String status = "";
		ErrorType errorType = null;
		if (txnType.equals(SettlementTransactionType.FUND_TRANSFER.getName())) {
			status = getStatus();
			errorType = getResponseCode();
		} else if (txnType.equals(SettlementTransactionType.STATUS.getName())) {
			status = getStatusEnqStatus();
			errorType = getStatusEnqResponseCode();
			// fields.put(FieldType.AMOUNT.getName(), transaction.getTransferAmount());
		} else if (txnType.equals(SettlementTransactionType.ADD_BENEFICIARY.getName())) {
			status = getAddBeneficiaryStatus();
			errorType = getAddBeneficiaryResponseCode();
		}
		if (StatusType.REJECTED.getName().equals(status)) {
			// This is applicable when we sent invalid request to server
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED.getResponseMessage());
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
		} else {
			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
		}
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getResponeMessage());
		fields.put(FieldType.RRN.getName(), transaction.getUniqueResponseNo());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getStatusCode());
		fields.put(FieldType.PG_DATE_TIME.getName(), transaction.getTransactionDate());

	}

	public ErrorType getResponseCode() {
		String result = transaction.getStatusCode();
		ErrorType errorType = null;
		if (StringUtils.isBlank(result)) {
			errorType = ErrorType.REJECTED;
			return errorType;
		}
		if (result.equals("AS")) {
			errorType = ErrorType.SUCCESS;
		} else if (result.equals("AD")) {
			errorType = ErrorType.DECLINED;
		} else {
			errorType = ErrorType.REJECTED;
		}
		return errorType;
	}

	public String getStatus() {
		String result = transaction.getStatusCode();
		String status = "";
		if (StringUtils.isBlank(result)) {
			status = StatusType.REJECTED.getName();
			return status;
		}
		if (result.equals("AS")) {
			status = StatusType.SENT_TO_BANK.getName();
		} else if (result.equals("AD")) {
			status = StatusType.INVALID.getName();
		} else {
			status = StatusType.REJECTED.getName();
		}
		return status;
	}

	public ErrorType getStatusEnqResponseCode() {
		String result = transaction.getStatusCode();
		ErrorType errorType = null;

		if (StringUtils.isBlank(result)) {
			errorType = ErrorType.REJECTED;
			return errorType;
		}
		if (result.equalsIgnoreCase("COMPLETED")) {
			errorType = ErrorType.SUCCESS;
		} else if (result.equalsIgnoreCase("SENT_TO_BENEFICIARY")) {
			errorType = ErrorType.SUCCESS;
		}  else if (result.equalsIgnoreCase("IN_PROCESS")) {
			errorType = ErrorType.SUCCESS;
		} else if (result.equalsIgnoreCase("FAILED")) {
			errorType = ErrorType.DECLINED;
		}else {
			errorType = ErrorType.REJECTED;
		}
		return errorType;
	}

	public String getStatusEnqStatus() {
		String result = transaction.getStatusCode();
		String status = "";

		if (StringUtils.isBlank(result)) {
			status = StatusType.REJECTED.getName();
			return status;
		}

		if (result.equalsIgnoreCase("COMPLETED")) {
			status = AccountStatus.SETTLED.getName();
		} else if (result.equalsIgnoreCase("SENT_TO_BENEFICIARY")) {
			status = AccountStatus.SENT_TO_BENEFICIARY.getName();
		} else if (result.equalsIgnoreCase("IN_PROCESS")) {
			status = AccountStatus.IN_PROCESS.getName();
		} else if (result.equalsIgnoreCase("FAILED")) {
			status = AccountStatus.FAILED.getName();
		} else {
			status = StatusType.REJECTED.getName();
		}
		return status;
	}

	public String getAddBeneficiaryStatus() {
		String result = transaction.getStatus();
		String status = "";
		if (StringUtils.isBlank(result)) {
			status = StatusType.REJECTED.getName();
			return status;
		}
		if (result.equals("SUCCESS")) {
			status = "SUCCESS";
		} else {
			status = StatusType.REJECTED.getName();
		}
		return status;
	}

	public ErrorType getAddBeneficiaryResponseCode() {
		String result = transaction.getStatus();
		ErrorType errorType = null;

		if (StringUtils.isBlank(result)) {
			errorType = ErrorType.REJECTED;
			return errorType;
		}
		if (result.equals("SUCCESS")) {
			errorType = ErrorType.SUCCESS;
		} else {
			errorType = ErrorType.REJECTED;
		}
		return errorType;
	}
}
