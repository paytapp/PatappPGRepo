package com.paymentgateway.crm.actionBeans;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.dao.TransactionDetailsService;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.TransactionHistory;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TdrCalculator;
import com.paymentgateway.commons.util.TransactionType;


@Component
public class RefundDetailsProvider  implements Serializable{

	@Autowired
	private TdrCalculator tdrCalculator;
	
	private static final long serialVersionUID = -3732059786389209314L;
	private List<TransactionHistory> oldTransactions = null;
	private int decimalPlaces;
	@Autowired
	private TransactionDetailsService transactionDetailsService;
	private TransactionHistory transDetails = new TransactionHistory();	
	
	public List<TransactionHistory> RefundProvider(String pgRefNum, String payId,String txnId) throws SystemException{
	
	/*	List<TransactionHistory> transactionList = (TransactionDetailFactory.getTransactionDetail().getAllTransactionsFromDb(txnId));*/
		List<TransactionHistory> transactionList =  transactionDetailsService.getTransaction(pgRefNum);
		 return oldTransactions = transactionList;
	}
	
	public void getAllTransactions(){	
		// set details to be displayed
		for (TransactionHistory transaction : oldTransactions) {
			TransactionType transactionType = TransactionType
					.getInstance(transaction.getTxnType());
			decimalPlaces = Currency.getNumberOfPlaces(transaction
					.getCurrencyCode());
			switch (transactionType) {
			case NEWORDER:// set details recieved at the time of new order
				transDetails.setNewOrderTransactionDetails(transaction);
				break;
			case AUTHORISE:
				if(transaction.getStatus().equals(StatusType.APPROVED.getName())){
					transDetails.setOriginalTransactionDetails(transaction);
				}
				break;
			case SALE:
				if(transaction.getStatus().equals(StatusType.CAPTURED.getName())){
					transDetails.setOriginalTransactionDetails(transaction);
				}
				break;
			case CAPTURE:
				if(transaction.getStatus().equals(StatusType.CAPTURED.getName())){
					transDetails.setCaptureTransactionDetails(transaction);
				}	
				break;
			case REFUND:
				transDetails.setRefundedTransactionDetails(transaction,decimalPlaces);
				break;
			default:
				break;
			}
		}
		
		removeOldTransactions();
      // transDetails = tdrCalculator.setTdrRefundDetails(transDetails,decimalPlaces);
	}
	
	public void removeOldTransactions(){
		// remove enroll and new order transaction; if exists
				Iterator<TransactionHistory> transItrator = oldTransactions.iterator();
				while (transItrator.hasNext()) {
					TransactionHistory transactionInstance = transItrator.next();
					if (transactionInstance.getTxnType().equals(
							TransactionType.NEWORDER.getName())
							|| transactionInstance.getTxnType().equals(
									TransactionType.ENROLL.getName())) {
						transItrator.remove();
					}
			}
	}
	
	public List<TransactionHistory> getOldTransactions() {
		return oldTransactions;
	}

	public void setOldTransactions(List<TransactionHistory> oldTransactions) {
		this.oldTransactions = oldTransactions;
	}

	public TransactionHistory getTransDetails() {
		return transDetails;
	}

	public void setTransDetails(TransactionHistory transDetails) {
		this.transDetails = transDetails;
	}
}
