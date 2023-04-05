package com.paymentgateway.crm.chargeback;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.TransactionDetailsService;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.TransactionHistory;
@Component
public class DefaultTransactionDetailProvider implements TransactionDetailProvider{
	@Autowired
	MongoInstance mongoInstance;

	public DefaultTransactionDetailProvider(){
		
	}

	@Override
	public List<TransactionHistory> getAllTransactionsFromDb(String txnId) throws SystemException {
		
		TransactionDetailsService transactionService = new TransactionDetailsService();
		return transactionService.getTransaction(txnId);		
	}
	
	@Override
	public void getCapturedTransactionsFromDb(String ordereId, String txnId) {
		
		
		
	}
}
