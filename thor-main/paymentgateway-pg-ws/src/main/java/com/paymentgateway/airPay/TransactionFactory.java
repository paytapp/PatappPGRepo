package com.paymentgateway.airPay;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

@Service("airPayFactory")
public class TransactionFactory {

	@SuppressWarnings("incomplete-switch")
	public Transaction getInstance(Fields fields) {

		Transaction transaction = new Transaction();
		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case REFUND:
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
			break;
		case SALE:
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			break;
		case STATUS:
			break;
		}

		return transaction;
	}

}
