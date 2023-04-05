package com.paymentgateway.crm.chargeback;

import java.util.List;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.TransactionHistory;

public interface TransactionDetailProvider {

	public  List<TransactionHistory> getAllTransactionsFromDb(String txnId)  throws SystemException;
	public void getCapturedTransactionsFromDb(String orderId, String txnId);
}
