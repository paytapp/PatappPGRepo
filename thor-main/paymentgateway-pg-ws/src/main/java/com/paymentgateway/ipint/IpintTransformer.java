package com.paymentgateway.ipint;

import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.bob.BobResultType;
import com.paymentgateway.commons.dao.EPOSTransactionDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;

@Service
public class IpintTransformer {

	@Autowired
	private Transaction transaction = null;

	public IpintTransformer(Transaction transaction) {
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
		
		if ((StringUtils.isNotBlank(transaction.getTransactionStatus()))&& ((transaction.getTransactionStatus()).equalsIgnoreCase("COMPLETED"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
		} else if((StringUtils.isNotBlank(transaction.getTransactionStatus()))&& ((transaction.getTransactionStatus()).equalsIgnoreCase("CHECKING") ||(transaction.getTransactionStatus()).equalsIgnoreCase("PROCESSING"))  ) {
			status = StatusType.PROCESSING.getName();
			errorType = ErrorType.PROCESSING;
			pgTxnMsg = ErrorType.PROCESSING.getResponseMessage();
		} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
		fields.put(FieldType.ACQ_ID.getName(), transaction.getInvoiceId());
		fields.put(FieldType.RRN.getName(), transaction.getInvoiceId());
		fields.put(FieldType.PG_DATE_TIME.getName(), transaction.getInvoiceCreationTime());
		fields.put(FieldType.PG_TXN_STATUS.getName(), transaction.getBlockchainTransactionStatus());
		//fields.put(FieldType.AMOUNT.getName(), transaction.getInvoiceAmountInLocalCurrency());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.CRYPTO_AMOUNT_IN_LOCAL_CURRENCY.getName(),transaction.getInvoiceAmountInLocalCurrency());
		fields.put(FieldType.CRYPTO_AMOUNT.getName(),transaction.getInvoiceCryptoAmount());
		fields.put(FieldType.CRYPTO_AMOUNT_IN_USD.getName(),transaction.getInvoiceAmountInUsd());
		fields.put(FieldType.CRYPTO_TXNTYPE.getName(), transaction.getTransactionCrypto());
		
		if (!StringUtils.isEmpty(fields.get(FieldType.EPOS_MERCHANT.getName()))) {
			if ((Boolean.valueOf(fields.get(FieldType.EPOS_MERCHANT.getName())))
					&& (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName()))) {
				ExecutorService es = ThreadPoolProvider.getExecutorService();
				es.execute(new Runnable() {
					@Override
					public void run() {
						new EPOSTransactionDao().updateEposRefundTransaction(fields);
					}
				});
				es.shutdown();
			}
		}
	
	}
	
	

	
}
