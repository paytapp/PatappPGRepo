package com.paymentgateway.pgui.action.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class RetryTransactionProcessor {
	@Autowired
	private RetryTransaction retryTransaction;

	public boolean retryTransaction(Fields responseMap, HttpServletRequest sessionMap, User user)
			throws SystemException {
		// handling fraud txns
		if (routeFraudTransactions(responseMap)) {
			return false;
		}

		retryTransaction.retryPayment(responseMap, sessionMap, user);
		if (retryTransaction.isTransactionFailFlag() == true) {
			return true;
		}
		return false;
	}

	// to handle txn failed due by fraud prevention system
	private boolean routeFraudTransactions(Fields fields) throws SystemException {
		if (fields.get(FieldType.STATUS.getName()).equals(StatusType.DENIED_BY_FRAUD.getName())) {
			return true;
		}
		return false;
	}
}
